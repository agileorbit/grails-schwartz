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

import groovy.transform.CompileStatic
import org.quartz.TimeOfDay

/**
 * Properties for configuring the schedule-related values of a
 * DailyTimeIntervalTrigger using DailyTimeIntervalScheduleBuilder.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class DailyProperties extends AbstractSchedulerProperties {

	protected DailyProperties(CommonProperties common, BuilderFactory factory) {
		super(common, factory)
	}

	Map<String, ?> modifiedDaysProperties() { modifiedProperties 'days' }
	Map<String, ?> modifiedIntervalProperties() { modifiedProperties 'interval' }

	TimeOfDay dailyEnd
	Integer dailyEndAfterCount
	TimeOfDay dailyStart
	Boolean everyDay
	Boolean mondayThroughFriday
	Boolean saturdayAndSunday

	// from common
	//	def days
	//	Integer interval
	//	Integer intervalInHours
	//	Integer intervalInMinutes
	//	Integer intervalInSeconds
	//	MisfireHandling misfireHandling
	//	Integer repeatCount
	//	IntervalUnit unit

	// fluent chaining mutator methods
	BuilderFactory dailyEnd(TimeOfDay _)                 { dailyEnd = _;            factory }
	BuilderFactory dailyEndAfterCount(int _)             { dailyEndAfterCount = _;  factory }
	BuilderFactory dailyStart(TimeOfDay _)               { dailyStart = _;          factory }
	BuilderFactory everyDay(boolean _ = true)            { everyDay = _;            factory }
	BuilderFactory mondayThroughFriday(boolean _ = true) { mondayThroughFriday = _; factory }
	BuilderFactory saturdayAndSunday(boolean _ = true)   { saturdayAndSunday = _;   factory }
}
