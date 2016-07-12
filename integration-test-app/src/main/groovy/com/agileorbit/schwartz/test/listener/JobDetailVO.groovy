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
import org.quartz.JobDetail
import org.quartz.JobKey

/**
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class JobDetailVO extends AbstractVO {
	final JobKey jobKey
	final String description
	final String jobClassName
	final Map jobDataMap
	final boolean durable
	final boolean persistJobDataAfterExecution
	final boolean concurrentExectionDisallowed
	final boolean requestsRecovery

	JobDetailVO(JobDetail jobDetail, JobEvent event = JobEvent.NO_EVENT) {
		this.event = event
		jobKey = jobDetail.key
		description = jobDetail.description
		jobClassName = jobDetail.jobClass.name
		jobDataMap = [:] + jobDetail.jobDataMap.getWrappedMap()
		durable = jobDetail.durable
		persistJobDataAfterExecution = jobDetail.persistJobDataAfterExecution
		concurrentExectionDisallowed = jobDetail.concurrentExectionDisallowed
		requestsRecovery = jobDetail.requestsRecovery()
	}

	String toString() {
		event.indent++
		def tabs = '\t' * event.indent
		String s = '\n' + tabs + properties().entrySet().join(',\n' + tabs)
		event.indent--
		s
	}
}
