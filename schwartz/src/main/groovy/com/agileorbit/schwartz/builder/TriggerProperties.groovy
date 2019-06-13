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
package com.agileorbit.schwartz.builder

import com.agileorbit.schwartz.SchwartzJob
import groovy.transform.CompileStatic
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.TriggerKey

import static org.quartz.DateBuilder.IntervalUnit.MILLISECOND
import static org.quartz.DateBuilder.futureDate

/**
 * The other properties classes work with schedule-related values and this one
 * configures the remaining, used to finish building each of the supported
 * trigger types using TriggerBuilder.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class TriggerProperties extends AbstractProperties {

	protected TriggerProperties(BuilderFactory factory) { super(factory) }

	String calendarName
	String description
	Date endAt
	String group
	SchwartzJob job
	Map<String, ?> jobData
	JobDataMap jobDataMap
	JobDetail jobDetail
	String jobGroup
	JobKey jobKey
	String jobName
	TriggerKey key
	String name
	Integer priority
	Date startAt

	// fluent chaining mutator methods
	BuilderFactory calendarName(String _)    { calendarName = _; factory }
	BuilderFactory description(String _)     { description  = _; factory }
	BuilderFactory endAt(Date _)             { endAt        = _; factory }
	BuilderFactory group(String _)           { group        = _; factory }
	BuilderFactory job(SchwartzJob _)        { job          = _; factory }
	BuilderFactory jobData(Map<String, ?> _) { jobData      = _; factory }
	BuilderFactory jobDataMap(JobDataMap _)  { jobDataMap   = _; factory }
	BuilderFactory jobDetail(JobDetail _)    { jobDetail    = _; factory }
	BuilderFactory jobGroup(String _)        { jobGroup     = _; factory }
	BuilderFactory jobKey(JobKey _)          { jobKey       = _; factory }
	BuilderFactory jobName(String _)         { jobName      = _; factory }
	BuilderFactory key(TriggerKey _)         { key          = _; factory }
	BuilderFactory name(String _)            { name         = _; factory }
	BuilderFactory priority(int _)           { priority     = _; factory }
	BuilderFactory startAt(Date _)           { startAt      = _; factory }

	// utility methods

	/**
	 * Set <code>startAt</code> to the Date at the specified number of milliseconds from now.
	 *
	 * @param millis number of milliseconds
	 * @return the factory for method chaining
	 */
	BuilderFactory startDelay(int millis) {
		startAt futureDate(millis, MILLISECOND)
	}

	/**
	 * Setter variant of startDelay().
	 *
	 * @param millis number of milliseconds
	 */
	void setStartDelay(int millis) {
		startDelay millis
	}

	/**
	 * Essentially a no-op because <code>startTime</code> defaults to <code>new Date()</code>
	 * in TriggerBuilder, but can be used to make code more descriptive.
	 *
	 * @return the factory for method chaining
	 */
	BuilderFactory startNow() {
		startAt new Date()
	}
}
