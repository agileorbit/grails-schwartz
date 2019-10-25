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
package com.agileorbit.schwartz

import com.agileorbit.schwartz.listener.SessionBinderJobListener
import com.agileorbit.schwartz.util.Utils
import grails.core.GrailsApplication
import grails.gorm.transactions.Transactional
import groovy.sql.Sql
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.quartz.JobBuilder
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.ListenerManager
import org.quartz.ObjectAlreadyExistsException
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.quartz.Trigger
import org.quartz.TriggerKey
import org.quartz.impl.matchers.KeyMatcher
import org.quartz.spi.JobStore
import org.springframework.beans.factory.annotation.Autowired

import javax.sql.DataSource

import static org.quartz.impl.matchers.GroupMatcher.anyGroup

/**
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
@Slf4j
@Transactional
class QuartzService {

	@Autowired(required=false) protected DataSource dataSource
	@Autowired protected GrailsApplication grailsApplication
	@Autowired protected Scheduler quartzScheduler
	@Autowired protected SessionBinderJobListener sessionBinderJobListener
	@Autowired(required=false) protected SchwartzJob[] jobs

	/**
	 * Called from doWithApplicationContext() to do startup initialization tasks;
	 * should not be called otherwise.
	 *
	 * @throws SchedulerException
	 */
	void init() throws SchedulerException {
		if (!quartzConfig('pluginEnabled', Boolean, true)) {
			log.info 'Not initializing, quartz.pluginEnabled is false'
			return
		}

		if (!validateTables()) {
			return
		}

		if (quartzConfig('purgeQuartzTablesOnStartup', Boolean, false)) {
			purgeTables()
		}

		if (quartzConfig('clearQuartzDataOnStartup', Boolean, false)) {
			clearData()
		}

		validateExistingJobs()

		scheduleJobs()

		if (!quartzConfig('autoStartup', Boolean, true)) {
			log.debug 'Not auto-starting, quartz.autoStartup is false'
			return
		}

		quartzScheduler.start()
		log.debug 'Started Quartz scheduler: {}', quartzScheduler.metaData
	}

	/**
	 * Checks that the required tables exist if JDBC storage is enabled.
	 *
	 * @return true if not using JDBC storage or if all tables exist
	 */
	boolean validateTables() {

		if (!quartzConfig('jdbcStore', Boolean, false)) return true

		String tablePrefix = tableNamePrefix()

		Sql sql = new Sql(dataSource)
		Collection<String> missingTables = []
		try {
			for (name in Utils.ALL_TABLE_NAMES) {
				try {
					sql.firstRow('select count(*) from ' + tablePrefix + name)
				}
				catch (ignored) {
					missingTables << name
				}
			}
		}
		finally {
			sql.close()
		}

		if (!missingTables) return true

		if (missingTables == Utils.ALL_TABLE_NAMES) {
			log.error 'Unable to start; jdbcStore is true but none of the Quartz tables have been created (' +
					Utils.ALL_TABLE_NAMES.toSorted().collect { tablePrefix + it }.join(', ') + ')'
		}
		else {
			log.error 'Unable to start; jdbcStore is true but at least one of the Quartz tables is missing: {}',
					missingTables.sort().collect { tablePrefix + it }
		}

		false
	}

	/**
	 * Checks that all JobDetails can be retrieved at startup to catch problems
	 * early such as deleted/moved/renamed Job classes.
	 *
	 * @throws SchedulerException
	 */
	void validateExistingJobs() throws SchedulerException {
		for (JobKey jobKey in quartzScheduler.getJobKeys(anyGroup())) {
			try {
				quartzScheduler.getJobDetail jobKey
				continue
			}
			catch (SchedulerException e) {
				log.warn 'Problem with JobDetail {}, pausing all triggers: {}', jobKey, e.toString()
			}
			catch (e) {
				log.warn 'Problem with JobDetail {}, pausing all triggers: {}', jobKey, e.message
			}
			quartzScheduler.pauseJob jobKey
		}
	}

	/**
	 * Registers a JobDetail and schedules triggers for each Spring bean that
	 * implements SchwartzJob; called at startup and should not be called otherwise.
	 *
	 * @throws SchedulerException
	 */
	void scheduleJobs() throws SchedulerException {
		for (SchwartzJob job in jobs) {
			scheduleJob job
		}
	}

	/**
	 * Builds and registers a JobDetail with values from the job instance and
	 * job data map, and schedules the job's triggers.
	 *
	 * @param job the job
	 * @param jobData optional job data
	 * @param overwriteExisting if true, any registered triggers  with
	 * the same key as one here will be unscheduled and replaced
	 * @return the next fire time for each trigger; the list will be the same
	 * size as the number of triggers but individual elements may be null
	 * depending on what's returned from the Scheduler.
	 * @throws SchedulerException
	 */
	List<Date> scheduleJob(SchwartzJob job, Map jobData = null,
	                       boolean overwriteExisting = false) throws SchedulerException {
		JobBuilder builder = job.jobBuilder()
		if (jobData) builder.usingJobData new JobDataMap(jobData)
		JobDetail jobDetail = builder.build()

		log.debug 'Registering JobDetail and triggers for SchwartzJob {} ({})',
				jobDetail.key, job.getClass().name

		scheduleJob jobDetail, job.triggers, job.sessionRequired, overwriteExisting
	}

	/**
	 * Adds the JobDetail in the Scheduler and schedules the job's triggers.
	 *
	 * @param jobDetail the job data
	 * @param triggers the triggers
	 * @param sessionRequired if true a listener will ensure a session is active
	 * while the job executes, and will close it when the job finishes
	 * @return the next fire time for each trigger; the list will be the same
	 * size as the number of triggers but individual elements may be null
	 * depending on what's returned from the Scheduler.
	 * @throws SchedulerException
	 */
	List<Date> scheduleJob(JobDetail jobDetail, Collection<? extends Trigger> triggers,
	                       boolean sessionRequired, boolean overwriteExisting = false) throws SchedulerException {

		quartzScheduler.addJob jobDetail, true, true
		log.debug 'Added JobDetail to Scheduler: {}', Utils.describeJobDetail(jobDetail)

		if (sessionRequired) {
			if (sessionBinderJobListener) {
				ListenerManager listenerManager = quartzScheduler.listenerManager
				KeyMatcher<JobKey> matcher = KeyMatcher.keyEquals(jobDetail.key)
				if (listenerManager.getJobListener(sessionBinderJobListener.name)) {
					listenerManager.addJobListenerMatcher sessionBinderJobListener.name, matcher
				}
				else {
					listenerManager.addJobListener sessionBinderJobListener, matcher
				}
			}
			else {
				log.error 'Job {} requires a session but SessionBinderJobListener is not available',
						jobDetail.key
			}
		}

		triggers.collect { Trigger trigger -> scheduleTrigger(trigger, overwriteExisting) }
	}

	/**
	 * Replaces data for an existing JobDetail with new values.
	 *
	 * @param jobDetail an instance with new values
	 * @throws SchedulerException
	 */
	void updateJobDetail(JobDetail jobDetail) throws SchedulerException {
		quartzScheduler.addJob jobDetail, true
	}

	/**
	 * Retrieves the JobDetail stored for the job if one exists.
	 *
	 * @param jobKey the job key
	 * @return the detail (possibly null)
	 * @throws SchedulerException
	 */
	JobDetail getStoredJobDetail(JobKey jobKey) throws SchedulerException {
		quartzScheduler.getJobDetail jobKey
	}

	/**
	 * Trigger the JobDetail with the specified job key to run immediately, with
	 * job data to be available during execution.
	 *
	 * @param jobKey the job key
	 * @param jobData optional data to be associated with the trigger that fires
	 * the job immediately (will be used to create a JobDataMap instance)
	 * @throws SchedulerException
	 */
	void triggerJob(JobKey jobKey, Map jobData = null) throws SchedulerException {
		quartzScheduler.triggerJob jobKey, jobData == null ? null : new JobDataMap(jobData)
	}

	/**
	 * Retrieve the triggers scheduled for the JobDetail with the specified job
	 * key. Note that the instances returned are cloned from the originals, so
	 * modifying them will have no effect on the stored instances.
	 *
	 * @param jobKey the job key
	 * @return the triggers (possibly empty)
	 * @throws SchedulerException
	 */
	List<? extends Trigger> getTriggers(JobKey jobKey) throws SchedulerException {
		quartzScheduler.getTriggersOfJob jobKey
	}

	/**
	 * Register the trigger in the Scheduler. If one is found with the same key
	 * and overwriteExisting is true, the previous one will be unsheduled and
	 * replaced with this one, otherwise the previous will remain scheduled.
	 *
	 * @param trigger the trigger
	 * @param overwriteExisting whether to replace existing triggers with
	 * the same key
	 * @return the next fire date
	 * @throws SchedulerException
	 */
	Date scheduleTrigger(Trigger trigger, boolean overwriteExisting = false) throws SchedulerException {
		TriggerKey key = trigger.key
		log.debug 'Adding Trigger to Scheduler: {}', Utils.describeTrigger(trigger)

		Date fireTime

		if (quartzScheduler.checkExists(key)) {
			if (overwriteExisting) {
				log.debug 'Trigger exists for key {}, rescheduling with new trigger', key
				fireTime = quartzScheduler.rescheduleJob(key, trigger)
			}
			else {
				log.debug 'Trigger exists for key {} but not rescheduling, ' +
						'overwriteExisting==false', key
				return
			}
		}
		else {
			try {
				fireTime = quartzScheduler.scheduleJob(trigger)
			}
			catch (ObjectAlreadyExistsException e) {
				log.debug 'Unexpectedly found existing trigger, assumably due to ' +
						'cluster race condition: {} - can safely be ignored', e.message
				fireTime = quartzScheduler.rescheduleJob(key, trigger)
			}
		}

		log.debug '{} {} will run {}; next fire time is {} ({})',
				(Utils.TRIGGER_TYPES.find { trigger in it } ?: trigger.getClass()).simpleName,
				key, Utils.describeNextFireTime(fireTime), fireTime, fireTime?.time

		fireTime
	}

	/**
	 * Deletes data from all database tables; called at startup if purgeQuartzTablesOnStartup is true.
	 *
	 * Based on grails.plugins.quartz.cleanup.JdbcCleanup.
	 */
	void purgeTables() {

		JobStore jobStore = Utils.getJobStore(quartzScheduler)
		if (!jobStore.supportsPersistence()) {
			log.info 'quartz.purgeQuartzTablesOnStartup is true but the JobStore ' +
					'is not persistent, not dropping tables'
			return
		}

		log.info 'Purging Quartz tables....'

		String prefix = tableNamePrefix()
		Sql sql
		try {
			sql = new Sql(dataSource)
			for (name in Utils.ALL_TABLE_NAMES) {
				String query = 'DELETE FROM ' + prefix + name
				log.info 'Executing {}', query
				sql.executeUpdate query
			}
		}
		catch (e) {
			log.error e.message, e
		}
		finally {
			sql?.close()
		}
	}

	/**
	 * Uses standard Quartz functionality to clear job and trigger data for the
	 * current scheduler (all tables except QRTZ_FIRED_TRIGGERS, QRTZ_LOCKS, and
	 * QRTZ_SCHEDULER_STATE, and filtered by scheduler name).
	 *
	 * Called at startup if clearQuartzDataOnStartup is true.
	 *
	 * @throws SchedulerException
	 */
	void clearData() throws SchedulerException {
		log.info 'Clearing Quartz data'
		quartzScheduler.clear()
	}

	/**
	 * The table name prefix for database tables when using JDBC persistence. The
	 * default is 'QRTZ_' but can be overridden in the config with jobStore.tablePrefix.
	 *
	 * @return the prefix
	 */
	String tableNamePrefix() {
		SchwartzSchedulerFactoryBean factoryBean = grailsApplication.mainContext.getBean(
				'&quartzScheduler', SchwartzSchedulerFactoryBean)
		Properties quartzProperties = factoryBean.finalQuartzProperties
		quartzProperties.getProperty('org.quartz.jobStore.tablePrefix', 'QRTZ_')
	}

	private <T> T quartzConfig(String key, Class<T> targetType, T defaultValue) {
		grailsApplication.config.getProperty 'quartz.' + key, targetType, defaultValue
	}
}
