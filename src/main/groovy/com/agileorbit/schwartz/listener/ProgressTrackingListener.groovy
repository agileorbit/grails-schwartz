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
import groovy.transform.ToString
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.JobKey
import org.quartz.JobListener
import org.quartz.Trigger
import org.quartz.Trigger.CompletedExecutionInstruction
import org.quartz.TriggerKey
import org.quartz.TriggerListener

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * Inspired by QuartzMonitorJobFactory/QuartzDisplayJob in quartz-monitor.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class ProgressTrackingListener extends AbstractListener implements JobListener, TriggerListener {

	public static final ConcurrentMap<String, JobRunInfo> jobRuns = new ConcurrentHashMap<String, JobRunInfo>()

	void triggerFired(Trigger trigger, JobExecutionContext context) {
		findOrCreate(keyFor(trigger)).init context
	}

	void jobExecutionVetoed(JobExecutionContext context) {
		lookup(context.trigger, 'jobExecutionVetoed')?.status = JobStatus.Vetoed
	}

	void jobToBeExecuted(JobExecutionContext context) {
		lookup(context.trigger, 'jobToBeExecuted')?.status = JobStatus.ToBeExecuted
	}

	void triggerMisfired(Trigger trigger) {
		lookup(trigger, 'triggerMisfired')?.status = JobStatus.Misfired
	}

	void jobWasExecuted(JobExecutionContext context, JobExecutionException jobException) {
		JobRunInfo jobRunInfo = lookup(context.trigger, 'jobWasExecuted')
		if (!jobRunInfo) return

		if (jobException) {
			jobRunInfo.status = JobStatus.Error
			jobRunInfo.jobException = jobException.toString()
		}
		else {
			jobRunInfo.status = JobStatus.Executed
		}
	}

	void triggerComplete(Trigger trigger, JobExecutionContext context, CompletedExecutionInstruction cei) {
		JobRunInfo jobRunInfo = lookup(context.trigger, 'triggerComplete')
		if (jobRunInfo) {
			jobRunInfo.jobRunTime = context.jobRunTime
			jobRunInfo.status = JobStatus.Complete
		}
	}

	boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) { false }

	protected String keyFor(Trigger trigger) {
		trigger.key.toString()
	}

	protected JobRunInfo findOrCreate(String key) {
		log.debug 'Initializing for Trigger {}', key
		JobRunInfo jobRunInfo = jobRuns[key]
		if (!jobRunInfo) {
			jobRuns.putIfAbsent key, new JobRunInfo()
			jobRunInfo = jobRuns[key]
		}
		jobRunInfo
	}

	protected JobRunInfo lookup(Trigger trigger, String where) {
		String key = keyFor(trigger)
		JobRunInfo jobRunInfo = jobRuns[key]
		if (!jobRunInfo) {
			log.error 'No JobRunInfo found for Trigger {} in {}', key, where
		}
		jobRunInfo
	}

	@CompileStatic
	@ToString(includeNames=true, includePackage=false)
	static class JobRunInfo {
		JobKey jobKey
		TriggerKey triggerKey
		String description
		JobStatus status
		long scheduledFireTime
		long fireTime
		Long jobRunTime
		String jobException
		Map extra = [:]

		void init(JobExecutionContext context) {
			jobKey = context.jobDetail.key
			triggerKey = context.trigger.key
			description = context.jobDetail.description
			status = JobStatus.Fired
			scheduledFireTime = context.scheduledFireTime.time
			fireTime = context.fireTime.time
			jobRunTime = null
			jobException = null
			extra.clear()
		}
	}

	@CompileStatic
	static enum JobStatus {
		Fired,
		Vetoed,
		ToBeExecuted,
		Misfired,
		Executed,
		Error,
		Complete
	}
}
