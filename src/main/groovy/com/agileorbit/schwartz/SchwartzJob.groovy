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

import com.agileorbit.schwartz.builder.BuilderFactory
import grails.util.Holders
import groovy.transform.CompileStatic
import org.quartz.InterruptableJob
import org.quartz.Job
import org.quartz.JobBuilder
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.Scheduler
import org.quartz.SchedulerException
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.UnableToInterruptJobException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Lazy

import static org.quartz.JobBuilder.newJob
import static org.quartz.JobKey.jobKey

/**
 * Implements {@link Job} and {@link InterruptableJob} and includes methods that define {@link JobDetail} and {@link Trigger}
 * values to use when registering the class as a job in the {@link Scheduler}, along with other utility methods
 * for scheduling and job management.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
trait SchwartzJob implements InitializingBean, InterruptableJob {

	/**
	 * Dependency injection for Quartz service to make it available when
	 * building triggers, when executing jobs, etc.
	 */
	@Lazy @Autowired QuartzService quartzService

	// Methods defining values and behaviors

	/**
	 * Uniquely identifies the Job/JobDetail within a job group, and paired with
	 * the group name establishes a unique name within the Scheduler.
	 *
	 * @return the job name
	 */
	String getJobName() { getClass().simpleName }

	/**
	 * An optional way to partition the jobs registered in a Scheduler. All jobs
	 * without an value specified for group name are in the 'DEFAULT' group.
	 *
	 * @return the group name
	 */
	String getJobGroup() { Scheduler.DEFAULT_GROUP }

	/**
	 * Convenience method to create a JobKey from the jobName and jobGroup values.
	 *
	 * @return a JobKey from the name and group properties
	 */
	JobKey getJobKey() { jobKey(jobName, jobGroup) }

	/**
	 * An optional property, defaults to null. It's typically used to help
	 * differentiate instances when there are many configured jobs and triggers.
	 * To provide the description for the JobDetail that's created for a
	 * SchwartzJob, override this method in that class to return a fixed value,
	 * or perhaps one that's customized by various factors, e.g. when the method
	 * is in a base class in a hierarchy.
	 *
	 * @return the description
	 */
	String getDescription() {}

	/**
	 * Specifies whether a GORM session should be kept open when this job's
	 * triggers fire (similar to the open-session-in-view pattern used in
	 * controllers).
	 *
	 * @return true to run jobs with an active session
	 */
	boolean getSessionRequired() { true }

	/**
	 * If a job is non-durable, it is automatically deleted from the scheduler
	 * once there are no longer any active triggers associated with it, i.e
	 * it has a life span bounded by the existence of its triggers.
	 */
	boolean getDurable() { true }

	/**
	 * If a job "requests recovery", and it is executing during the time of a
	 * 'hard shutdown' of the scheduler (i.e. the process it is running within
	 * crashes, or the machine is shut off), then it is re-executed when the
	 * scheduler is started again.
	 */
	boolean getRequestsRecovery() { false }

	// override this empty InterruptableJob implementation to support being
	// interrupted, and implement the logic for how to proceed when it occurs
	void interrupt() throws UnableToInterruptJobException {}

	// Methods for managing the JobDetail and Triggers

	/**
	 * Called from afterPropertiesSet() at startup to let you build all the
	 * triggers to be scheduled for this job. The 'quartzService' bean will be
	 * available at this point if needed.
	 *
	 * Add your trigger instances to the 'triggers' list and they will be
	 * registered with the Scheduler. Note that it's not required to have any
	 * triggers, and you can schedule them yourself later.
	 */
	abstract void buildTriggers()

	/**
	 * Non-persistent collection of triggers, used as a collection point at
	 * startup to create all of the job's triggers to be registered in the
	 * scheduler at startup. It is otherwise unused and never modified by the
	 * plugin.
	 */
	List<Trigger> triggers = []

	/**
	 * Creates a TriggerBuilder with the job and group names set.
	 *
	 * @return the builder
	 */
	TriggerBuilder builder() {
		TriggerBuilder.newTrigger().forJob(jobKey)
	}

	/**
	 * Creates a TriggerBuilder with the job and group names set, and with the
	 * specified trigger name set.
	 *
	 * @param triggerName the name
	 * @return the builder
	 */
	TriggerBuilder builder(String triggerName) {
		builder().withIdentity(triggerName)
	}

	/**
	 * Creates a BuilderFactory with the job and group names set.
	 *
	 * @return the factory
	 */
	BuilderFactory factory() {
		BuilderFactory.builder().job(this)
	}

	/**
	 * Creates a BuilderFactory with the job and group names set, and with the
	 * specified trigger name set.
	 *
	 * @param triggerName the name
	 * @return the factory
	 */
	BuilderFactory factory(String triggerName) {
		factory().name(triggerName)
	}

	/**
	 * Creates a JobBuilder configured from current values from the class (
	 * all values except a JobDataMap). You can invoke any of the methods again
	 * to override a value and/or provide values for the JobDataMap using any
	 * of those builder methods.
	 *
	 * This is called to build the initial JobDetails to register as a Job in
	 * the scheduler, so override this method in the unlikely case that you
	 * always want to override the values it uses.
	 *
	 * @return a builder with initial values
	 */
	JobBuilder jobBuilder() {
		newJob((Class<? extends SchwartzJob>) getClass())
				.withIdentity(jobKey)
				.storeDurably(durable)
				.requestRecovery(requestsRecovery)
				.withDescription(description)
	}

	/**
	 * Updates the data in the Scheduler for this job using the current values
	 * from the class. This has no effect on registered triggers.
	 *
	 * @throws SchedulerException
	 */
	void updateJobDetail() throws SchedulerException {
		updateJobDetail jobBuilder().build()
	}

	/**
	 * Updates the data in the Scheduler for this job using the values from the
	 * JobDetail instance. This has no effect on registered triggers.
	 *
	 * @param jobDetail the new values
	 * @throws SchedulerException
	 */
	void updateJobDetail(JobDetail jobDetail) throws SchedulerException {
		quartzService.updateJobDetail jobDetail
	}

	/**
	 * Retrieve the current stored job detail for this class.
	 *
	 * @return the stored detail
	 * @throws SchedulerException
	 */
	JobDetail getStoredJobDetail() throws SchedulerException {
		quartzService.getStoredJobDetail jobKey
	}

	/**
	 * Retrieves all triggers registered for this job.
	 *
	 * @return the triggers
	 * @throws SchedulerException
	 */
	List<? extends Trigger> getStoredTriggers() throws SchedulerException {
		quartzService.getTriggers jobKey
	}

	// Methods for scheduling

	/**
	 * Registers the trigger in the scheduler (if there isn't one already) with
	 * the same trigger name and group, and replaces the existing trigger if
	 * one is registered.
	 *
	 * @param trigger the trigger
	 * @return the next fire time
	 * @throws SchedulerException
	 */
	Date schedule(Trigger trigger) throws SchedulerException {
		quartzService.scheduleTrigger trigger, true
	}

	/**
	 * Trigger this job (i.e. the JobDetail stored for this class) now.
	 *
	 * @throws SchedulerException
	 */
	void triggerJob() throws SchedulerException {
		quartzService.triggerJob jobKey
	}

	/**
	 * Trigger this job (i.e. the JobDetail stored for this class) now, with the
	 * specified job data to be available during execution.
	 *
	 * @param data data to be associated with the trigger that fires the job
	 * immediately (will be used to create a JobDataMap instance)
	 * @throws SchedulerException
	 */
	void triggerJob(Map jobData) throws SchedulerException {
		quartzService.triggerJob jobKey, jobData
	}

	// Miscellaneous Methods

	/**
	 * This is a no-op for jobs that are registered as Spring beans as they'll
	 * have the service dependency-injected at startup. But for non-bean jobs
	 * it lazily retrieves the service from the ApplicationContext the first
	 * time it's called if the instance isn't set. This allows non-bean job
	 * classes to call all of the methods that delegate to the service.
	 *
	 * @return the service
	 */
	QuartzService getQuartzService() {
		if (quartzService == null) {
			// will be null if not registered as a Spring bean
			quartzService = Holders.applicationContext.getBean(
					'quartzService', QuartzService)
		}
		quartzService
	}

	void afterPropertiesSet() {
		try {
			buildTriggers()
		}
		catch (e) {
			LoggerFactory.getLogger(getClass()).error(
					'Problem building triggers: {}', e.message, e)
		}
	}
}
