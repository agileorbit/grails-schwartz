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

import com.agileorbit.schwartz.util.Utils
import groovy.transform.CompileStatic
import org.quartz.Calendar
import org.quartz.JobExecutionContext
import org.quartz.TriggerKey

/**
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class JobExecutionContextVO extends AbstractVO {

	final Calendar calendar
	final String fireInstanceId
	final long fireTime
	final JobDetailVO jobDetail
	final String jobInstance
	final long jobRunTime
	final Map mergedJobDataMap
	final Long nextFireTime
	final Long previousFireTime
	final TriggerKey recoveringTriggerKey
	final int refireCount
	final result
	final Long scheduledFireTime
	final TriggerVO trigger

	JobExecutionContextVO(JobExecutionContext context, JobEvent event = JobEvent.NO_EVENT) {
		this.event = event
		calendar = context.calendar
		fireInstanceId = context.fireInstanceId
		fireTime = context.fireTime.time
		jobDetail = new JobDetailVO(context.jobDetail, event)
		jobInstance = Utils.defaultObjectToString(context.jobInstance)
		jobRunTime = context.jobRunTime
		mergedJobDataMap = [:] + context.mergedJobDataMap.getWrappedMap()
		nextFireTime = context.nextFireTime?.time
		previousFireTime = context.previousFireTime?.time
		recoveringTriggerKey = context.recovering ? context.recoveringTriggerKey : null
		refireCount = context.refireCount
		result = context.result
		scheduledFireTime = context.scheduledFireTime.time
		trigger = new TriggerVO(context.trigger, event)
	}

	String toString() {
		event.indent++
		def tabs = '\t' * event.indent
		String s = '\n' + tabs + properties().entrySet().join(',\n' + tabs)
		event.indent--
		s
	}
}
