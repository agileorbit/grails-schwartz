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
package com.agileorbit.schwartz.test.listener

import groovy.transform.CompileStatic
import groovy.transform.PackageScope
import org.quartz.JobDetail
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.JobKey
import org.quartz.SchedulerException
import org.quartz.Trigger
import org.quartz.Trigger.CompletedExecutionInstruction
import org.quartz.TriggerKey

/**
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class JobEvent extends AbstractVO {

	final JobExecutionContextVO context
	final JobDetailVO jobDetail
	final JobExecutionException jobExecutionException
	final String jobGroup
	final JobKey jobKey
	final String listenerMethod
	final SchedulerException schedulerException
	final String schedulerExceptionMessage
	final long timeCreated = System.currentTimeMillis()
	final TriggerVO trigger
	final String triggerGroup
	final CompletedExecutionInstruction triggerInstructionCode
	final TriggerKey triggerKey

	protected int indent

	static final JobEvent NO_EVENT = new JobEvent()

	@PackageScope JobEvent() {}

	JobEvent(String listenerMethod, Map<String, ?> values) {
		this.listenerMethod = listenerMethod
		context = values.context ? new JobExecutionContextVO((JobExecutionContext)values.context, this) : null
		jobDetail = values.jobDetail ? new JobDetailVO((JobDetail)values.jobDetail, this) : null
		jobExecutionException = (JobExecutionException) values.jobExecutionException
		jobGroup = (String) values.jobGroup
		jobKey = (JobKey) values.jobKey
		schedulerException = (SchedulerException) values.schedulerException
		schedulerExceptionMessage = (String) values.schedulerExceptionMessage
		trigger = values.trigger ? new TriggerVO((Trigger)values.trigger, this) : null
		triggerGroup = (String) values.triggerGroup
		triggerInstructionCode = (CompletedExecutionInstruction) values.triggerInstructionCode
		triggerKey = (TriggerKey) values.triggerKey
	}

	String toString() {
		def props = properties()
		props.remove 'NO_EVENT'
		props.remove 'listenerMethod'
		props.remove 'timeCreated'
		if (context?.trigger) {
			props.remove 'trigger' // don't print twice
		}
		indent = 0
		"$listenerMethod @ $timeCreated\n${props.entrySet().join(',\n\t')}"
	}
}
