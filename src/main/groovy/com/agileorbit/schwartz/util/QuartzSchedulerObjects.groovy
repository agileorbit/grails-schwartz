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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.quartz.SchedulerMetaData
import org.quartz.core.JobRunShellFactory
import org.quartz.core.QuartzScheduler
import org.quartz.core.QuartzSchedulerResources as Resources
import org.quartz.impl.jdbcjobstore.JobStoreSupport
import org.quartz.impl.jdbcjobstore.StdJDBCDelegate
import org.quartz.simpl.RAMJobStore
import org.quartz.spi.JobStore
import org.quartz.spi.SchedulerPlugin
import org.quartz.spi.ThreadExecutor
import org.quartz.spi.ThreadPool

/**
 * Created when using InstantiateInterceptSchedulerFactory; contains the
 * QuartzScheduler created by the factory along with its internal Scheduler
 * and other reachable objects and properties that are otherwise impractical
 * to access.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class QuartzSchedulerObjects {

	private static final Map<String, QuartzSchedulerObjects> instances = [:].asSynchronized()

	final QuartzScheduler qs
	final Scheduler scheduler
	final Resources resources

	final long batchTimeWindow
	final boolean interruptJobsOnShutdown
	final boolean interruptJobsOnShutdownWithWait
	final boolean jmxExport
	final JobRunShellFactory jobRunShellFactory
	final JobStore jobStore
	final boolean jobStoreClustered
	final boolean jobStorePersistent
	final boolean makeSchedulerThreadDaemon
	final int maxBatchSize
	final boolean runUpdateCheck
	final String schedulerInstanceId
	final String schedulerName
	final List<SchedulerPlugin> schedulerPlugins
	final ThreadExecutor threadExecutor
	final String threadName
	final ThreadPool threadPool
	final boolean threadsInheritInitializersClassLoadContext
	final String uniqueIdentifier // schedName + '_$_' + schedInstId

	final RAMJobStore ramJobStore
	final JobStoreSupport jdbcJobStore

	private QuartzSchedulerObjects(QuartzScheduler qs, Scheduler scheduler,
	                               Resources resources) throws SchedulerException {
		this.qs = qs
		this.scheduler = scheduler
		this.resources = resources

		batchTimeWindow = resources.batchTimeWindow
		schedulerInstanceId = resources.instanceId
		interruptJobsOnShutdown = resources.interruptJobsOnShutdown
		interruptJobsOnShutdownWithWait = resources.interruptJobsOnShutdownWithWait
		jmxExport = resources.JMXExport // object name isn't available here but is available later via resources.JMXObjectName
		jobRunShellFactory = resources.jobRunShellFactory
		jobStore = resources.jobStore
		makeSchedulerThreadDaemon = resources.makeSchedulerThreadDaemon
		maxBatchSize = resources.maxBatchSize
		runUpdateCheck = resources.runUpdateCheck
		schedulerName = resources.name
		schedulerPlugins = ([] + resources.schedulerPlugins).asImmutable()
		threadExecutor = resources.threadExecutor
		threadName = resources.threadName
		threadPool = resources.threadPool
		threadsInheritInitializersClassLoadContext =
				resources.threadsInheritInitializersClassLoadContext
		uniqueIdentifier = resources.uniqueIdentifier

		SchedulerMetaData metaData = scheduler.metaData
		jobStoreClustered = metaData.jobStoreClustered
		jobStorePersistent = metaData.jobStoreSupportsPersistence

		if (jobStore instanceof RAMJobStore) {
			ramJobStore = (RAMJobStore) jobStore
		}
		else if (jobStore instanceof JobStoreSupport) {
			jdbcJobStore = (JobStoreSupport) jobStore
		}
	}

	@CompileDynamic
	StdJDBCDelegate getJdbcDelegate() {
		// not available in create() because classLoadHelper isn't set at that point
		def jdbcDelegate = jdbcJobStore?.getDelegate()
		if (jdbcDelegate instanceof StdJDBCDelegate) {
			jdbcDelegate
		}
	}

	/**
	 * Use this to stop the scheduler and remove this instance from the cache
	 * in one call, or call removeInstance() explicitly.
	 *
	 * @throws SchedulerException
	 */
	void shutdown() throws SchedulerException {
		scheduler.shutdown()
		removeInstance()
	}

	void removeInstance() {
		removeInstance this
	}

	/**
	 * Called from SchwartzSchedulerFactory to register a new instance.
	 *
	 * @param qs the QuartzScheduler
	 * @param scheduler the scheduler
	 * @param resources scheduler resources
	 * @throws SchedulerException
	 */
	static void create(QuartzScheduler qs, Scheduler scheduler,
	                   Resources resources) throws SchedulerException {
		def quartz = new QuartzSchedulerObjects(qs, scheduler, resources)
		instances[quartz.uniqueIdentifier] = quartz
	}

	static Map<String, QuartzSchedulerObjects> getInstances() {
		instances.asImmutable()
	}

	static QuartzSchedulerObjects getInstance(Scheduler scheduler) {
		instances[Resources.getUniqueIdentifier(
				scheduler.schedulerName,
				scheduler.schedulerInstanceId)]
	}

	static void removeInstance(QuartzSchedulerObjects instance) {
		instances.remove instance.uniqueIdentifier
	}
}
