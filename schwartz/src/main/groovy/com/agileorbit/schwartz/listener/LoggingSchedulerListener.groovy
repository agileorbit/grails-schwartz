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
package com.agileorbit.schwartz.listener

import groovy.transform.CompileStatic
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.SchedulerException
import org.quartz.SchedulerListener
import org.quartz.Trigger
import org.quartz.TriggerKey

/**
 * Logs information about for each of the scheduler event callback methods.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class LoggingSchedulerListener extends AbstractListener implements SchedulerListener {

	void jobScheduled(Trigger trigger) {
		log.debug 'jobScheduled: {}', trigger.key
	}

	void jobUnscheduled(TriggerKey triggerKey) {
		log.debug 'jobUnscheduled: {}', triggerKey
	}

	void triggerFinalized(Trigger trigger) {
		log.debug 'triggerFinalized: {}', trigger.key
	}

	void triggerPaused(TriggerKey triggerKey) {
		log.debug 'triggerPaused: {}', triggerKey
	}

	void triggersPaused(String triggerGroup) {
		log.debug 'triggersPaused: {}', triggerGroup
	}

	void triggerResumed(TriggerKey triggerKey) {
		log.debug 'triggerResumed: {}', triggerKey
	}

	void triggersResumed(String triggerGroup) {
		log.debug 'triggersResumed: {}', triggerGroup
	}

	void jobAdded(JobDetail jobDetail) {
		log.debug 'jobAdded: {}', jobDetail.key
	}

	void jobDeleted(JobKey jobKey) {
		log.debug 'jobDeleted: {}', jobKey
	}

	void jobPaused(JobKey jobKey) {
		log.debug 'jobPaused: {}', jobKey
	}

	void jobsPaused(String jobGroup) {
		log.debug 'jobsPaused: {}', jobGroup
	}

	void jobResumed(JobKey jobKey) {
		log.debug 'jobResumed: {}', jobKey
	}

	void jobsResumed(String jobGroup) {
		log.debug 'jobsResumed: {}', jobGroup
	}

	void schedulerError(String msg, SchedulerException cause) {
		log.error msg, cause
	}

	void schedulerInStandbyMode() {
		log.debug 'schedulerInStandbyMode()'
	}

	void schedulerStarted() {
		log.debug 'schedulerStarted()'
	}

	void schedulerStarting() {
		log.debug 'schedulerStarting()'
	}

	void schedulerShutdown() {
		log.debug 'schedulerShutdown()'
	}

	void schedulerShuttingdown() {
		log.debug 'schedulerShuttingdown()'
	}

	void schedulingDataCleared() {
		log.debug 'schedulingDataCleared()'
	}
}
