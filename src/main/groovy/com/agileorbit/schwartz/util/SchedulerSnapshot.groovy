/* Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agileorbit.schwartz.util

import groovy.transform.CompileStatic
import groovy.transform.Immutable
import org.quartz.JobDetail
import org.quartz.JobExecutionContext
import org.quartz.JobListener
import org.quartz.ListenerManager
import org.quartz.Scheduler
import org.quartz.SchedulerContext
import org.quartz.SchedulerException
import org.quartz.SchedulerListener
import org.quartz.SchedulerMetaData
import org.quartz.Trigger
import org.quartz.Trigger.TriggerState
import org.quartz.TriggerListener
import org.quartz.core.QuartzScheduler
import org.quartz.core.QuartzSchedulerResources
import org.quartz.impl.StdScheduler
import org.quartz.impl.jdbcjobstore.JobStoreSupport
import org.quartz.impl.triggers.AbstractTrigger
import org.quartz.spi.JobStore
import org.quartz.spi.SchedulerPlugin
import org.springframework.util.ReflectionUtils

import java.lang.reflect.Field

import static org.quartz.impl.matchers.GroupMatcher.anyGroup

/**
 * Captures data from several areas of Quartz for use with testing or debugging.
 * Most of the source objects (the Scheduler, the JobStore, etc.) aren't
 * retained - a relevant subset of the available information is stored here.
 * Most everything is copied/cloned and/or immutable, but modifying anything
 * should avoided and the data should be considered read-only.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
@Immutable
class SchedulerSnapshot {

	private static final List<String> names = [
			'calendarNames', 'context', 'currentlyExecutingJobs', 'inStandbyMode',
			'jobGroupNames', 'pausedTriggerGroups', 'schedulerInstanceId',
			'schedulerName', 'shutdown', 'started', 'triggerGroupNames'].asImmutable()
	private static final List<String> listenerNames = [
			'triggerListeners', 'schedulerListeners', 'jobListeners'].asImmutable()
	private static final List<String> metaDataNames = [
			'schedulerClass', 'runningSince', 'numberOfJobsExecuted', 'summary',
			'schedulerRemote', 'jobStoreClass', 'jobStoreSupportsPersistence',
			'jobStoreClustered', 'threadPoolClass', 'threadPoolSize', 'version'].asImmutable()
	private static final List<String> resourcesNames = [
			'batchTimeWindow', 'interruptJobsOnShutdown', 'interruptJobsOnShutdownWithWait',
			'JMXExport', 'JMXObjectName', 'makeSchedulerThreadDaemon', 'maxBatchSize',
			'runUpdateCheck','schedulerPlugins', 'threadName',
			'threadsInheritInitializersClassLoadContext'].asImmutable()
	private static final List<String> jobStoreNames = [
			'acquireTriggersWithinLock', 'clusterCheckinInterval', 'dataSource',
			'dbRetryInterval', 'driverDelegateClass', 'driverDelegateInitString',
			'dontSetAutoCommitFalse', 'doubleCheckLockMisfireHandler',
			'lockOnInsert', 'makeThreadsDaemons', 'maxMisfiresToHandleAtATime',
			'misfireThreshold', 'selectWithLockSQL', 'tablePrefix',
			'txIsolationLevelSerializable', 'useDBLocks'].asImmutable()
	private static final List<String> allNames =
			(['jobs'] + names + listenerNames + metaDataNames +
					resourcesNames + jobStoreNames).sort().asImmutable()

	// from Scheduler
	List<String> calendarNames
	SchedulerContext context
	List<JobExecutionContext> currentlyExecutingJobs
	Boolean inStandbyMode
	List<String> jobGroupNames
	Set<String> pausedTriggerGroups
	String schedulerInstanceId
	String schedulerName
	Boolean shutdown
	Boolean started
	List<String> triggerGroupNames

	// listeners
	List<JobListener> jobListeners
	List<SchedulerListener> schedulerListeners
	List<TriggerListener> triggerListeners

	// from SchedulerMetaData
	Class<?> schedulerClass
	Date runningSince
	Integer numberOfJobsExecuted
	Boolean schedulerRemote
	Class<?> jobStoreClass
	Boolean jobStoreSupportsPersistence
	Boolean jobStoreClustered
	Class<?> threadPoolClass
	Integer threadPoolSize
	String version
	String summary

	// from QuartzSchedulerResources
	Long batchTimeWindow
	Boolean interruptJobsOnShutdown
	Boolean interruptJobsOnShutdownWithWait
	Boolean JMXExport
	String JMXObjectName
	Boolean makeSchedulerThreadDaemon
	Integer maxBatchSize
	Boolean runUpdateCheck
	List<SchedulerPlugin> schedulerPlugins
	String threadName
	Boolean threadsInheritInitializersClassLoadContext

	// JobStoreSupport
	// only available if using JDBC and the JobStore extends JobStoreSupport
	Boolean acquireTriggersWithinLock
	Long clusterCheckinInterval
	String dataSource
	Long dbRetryInterval
	String driverDelegateClass
	String driverDelegateInitString
	Boolean dontSetAutoCommitFalse
	Boolean doubleCheckLockMisfireHandler
	Boolean lockOnInsert
	Boolean makeThreadsDaemons
	Integer maxMisfiresToHandleAtATime
	Long misfireThreshold
	String selectWithLockSQL
	String tablePrefix
	Boolean txIsolationLevelSerializable
	Boolean useDBLocks

	Collection<JobSnapshot> jobs

	/**
	 * Use this to create a new instance from the Scheduler and its helper classes.
	 *
	 * @param scheduler the Scheduler (the 'quartzScheduler' Spring bean)
	 * @return a new instance
	 * @throws SchedulerException
	 */
	static SchedulerSnapshot build(Scheduler scheduler) throws SchedulerException {

		Map<String, Object> values = [:]

		def copyFrom = { source, List<String> names ->
			for (name in names) {
				def value = source[name]
				if (value != null) values[name] = value
			}
		}

		def findFieldValue = { source, String name ->
			Field field = ReflectionUtils.findField(source.getClass(), name)
			ReflectionUtils.makeAccessible field
			field.get source
		}

		copyFrom scheduler, names

		ListenerManager listenerManager = scheduler.listenerManager
		copyFrom listenerManager, listenerNames

		SchedulerMetaData metaData = scheduler.metaData
		copyFrom metaData, metaDataNames

		if (scheduler instanceof StdScheduler) {
			QuartzScheduler qs = (QuartzScheduler) findFieldValue(scheduler, 'sched')

			QuartzSchedulerResources resources = (QuartzSchedulerResources) findFieldValue(qs, 'resources')
			copyFrom resources, resourcesNames

			JobStore jobStore = (JobStore) findFieldValue(resources, 'jobStore')
			if (jobStore instanceof JobStoreSupport) {
				copyFrom jobStore, jobStoreNames
			}
		}

		Collection<JobSnapshot> jobs = []
		values.jobs = jobs

		for (jobKey in scheduler.getJobKeys(anyGroup())) {
			JobDetail jobDetail = scheduler.getJobDetail(jobKey)
			if (!jobDetail) continue

			Map<Trigger, TriggerState> triggers = [:]
			scheduler.getTriggersOfJob(jobDetail.key).collectEntries(triggers) { trigger ->
				new MapEntry(((AbstractTrigger)trigger).clone(),
						       scheduler.getTriggerState(trigger.key))
			}

			jobs << new JobSnapshot(
					jobDetail: (JobDetail)jobDetail.clone(),
					triggers: triggers.asImmutable())
		}

		new SchedulerSnapshot(values)
	}

	String toString() {

		def map = [:]
		for (name in allNames) {
			def value = this[name]
			if (value == null) continue

			if (value instanceof Class) {
				value = ((Class)value).name
			}
			else {
				switch (name) {
					case 'context':
						value = [:] + (Map)value
						def applicationContext = ((Map)value).applicationContext
						if (applicationContext) {
							((Map)value).applicationContext = applicationContext.getClass().name
						}
						break
					case 'currentlyExecutingJobs':
						value = ((List<JobExecutionContext>)value).collect { JobExecutionContext jec ->
							def data = [calendar: jec.calendar,
							            fireInstanceId: jec.fireInstanceId,
							            fireTime: jec.fireTime,
							            job: jec.jobDetail.key,
							            nextFireTime: jec.nextFireTime,
							            previousFireTime: jec.previousFireTime,
							            recovering: jec.recovering,
							            refireCount: jec.refireCount,
							            scheduledFireTime: jec.scheduledFireTime,
							            trigger: jec.trigger.key] as Map
							if (jec.jobRunTime != -1) data.jobRunTime = jec.jobRunTime
							if (jec.recovering) data.recoveringTriggerKey = jec.recoveringTriggerKey
							Utils.removeNullValues data

							for (Map.Entry entry in data.entrySet()) {
								if (entry.value instanceof Date) {
									entry.value = ((Date)entry.value).format(Utils.DATE_FORMAT)
								}
							}

							'[' + Utils.mapToString(data, true) + ']'
						}.join(', ')
						value = '[' + value + ']'
						break
					case 'JMXExport':
					case 'JMXObjectName':
						name = 'jmx' + name.substring(3)
						break
					case 'jobListeners':
						value = ((List<JobListener>)value).collect {
							Utils.defaultObjectToString(it) + ' (' + it.name + ')' }
						break
					case 'schedulerListeners':
					case 'schedulerPlugins':
						value = ((List)value).collect { Utils.defaultObjectToString(it) }
						break
					case 'summary': value = '"' + ((String)value).trim() + '"'; break
					case 'triggerListeners':
						value = ((List<TriggerListener>)value).collect {
							Utils.defaultObjectToString(it) + ' (' + it.name + ')' }
						break
				}
			}
			map[name] = value
		}

		'[' + Utils.defaultObjectToString(this) + ' ' + Utils.mapToString(map, false) + ']'
	}
}
