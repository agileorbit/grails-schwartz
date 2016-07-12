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

/**
 * Properties for configuring the schedule-related values of a
 * CalendarIntervalTrigger using CalendarIntervalScheduleBuilder.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class CalendarProperties extends AbstractSchedulerProperties {

	protected CalendarProperties(CommonProperties common, BuilderFactory factory) {
		super(common, factory)
	}

	Map<String, ?> modifiedIntervalProperties() { modifiedProperties 'interval' }

	Integer intervalInDays
	Integer intervalInMonths
	Integer intervalInWeeks
	Integer intervalInYears
	Boolean preserveHour
	Boolean skipDay

	// from common
	//	Integer interval
	//	Integer intervalInHours
	//	Integer intervalInMinutes
	//	Integer intervalInSeconds
	//	MisfireHandling misfireHandling
	//	TimeZone timeZone
	//	IntervalUnit unit

	// fluent chaining mutator methods
	BuilderFactory intervalInDays(int _)          { intervalInDays = _;   factory }
	BuilderFactory intervalInMonths(int _)        { intervalInMonths = _; factory }
	BuilderFactory intervalInWeeks(int _)         { intervalInWeeks = _;  factory }
	BuilderFactory intervalInYears(int _)         { intervalInYears = _;  factory }
	BuilderFactory preserveHour(boolean _ = true) { preserveHour = _;     factory }
	BuilderFactory skipDay(boolean _ = true)      { skipDay = _;          factory }
}
