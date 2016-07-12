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
 * Abstract base class for all properties classes.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
abstract class AbstractProperties {

	protected static final Map<Class, List<String>> PROPERTIES
	static {
		Map<Class, List<String>> props = [
			(CommonProperties)  : ['days', 'interval', 'intervalInHours', 'intervalInMinutes',
			                       'intervalInSeconds', 'misfireHandling', 'repeatCount',
			                       'timeZone', 'unit'],

			(CalendarProperties): ['intervalInDays', 'intervalInMonths', 'intervalInWeeks',
			                       'intervalInYears', 'preserveHour', 'skipDay'],

			(CronProperties)    : ['cronExpression', 'cronSchedule', 'cronScheduleNonvalidated',
			                       'day', 'hour', 'hourAndMinuteMode', 'minute'],

			(DailyProperties)   : ['dailyEnd', 'dailyEndAfterCount', 'dailyStart', 'everyDay',
			                       'mondayThroughFriday', 'saturdayAndSunday'],

			(SimpleProperties)  : ['hours', 'intervalInMillis', 'minutes', 'repeatForever',
			                       'repeatMode', 'seconds', 'totalCount'],

			(TriggerProperties) : ['calendarName', 'description', 'endAt', 'group', 'job', 'jobData',
			                       'jobDataMap', 'jobDetail', 'jobGroup', 'jobKey', 'jobName',
			                       'key', 'name', 'priority', 'startAt']
		] as Map

		PROPERTIES = mapAndListsAsImmutable(props)
	}

	protected static <T> Set<T> copyAsSet(Collection<T> c) {
		Set<T> copy = [] as Set
		copy.addAll c
		copy
	}

	protected static Map mapAndListsAsImmutable(Map map) {
		for (key in copyAsSet(map.keySet())) {
			map[key] = ((List<String>) map[key]).asImmutable()
		}
		map.asImmutable()
	}

	protected final BuilderFactory factory

	protected AbstractProperties(BuilderFactory builderFactory) {
		factory = builderFactory
	}

	protected propertyValue(String name) {
		this[name]
	}

	protected Map<String, ?> modifiedProperties(Collection<String> propertyGroupNames) {
		def modified = [:]
		for (String name in propertyGroupNames) {
			def value = propertyValue(name)
			if (value != null) modified[name] = value
		}
		modified
	}

	Collection<String> propertyNames() { PROPERTIES[getClass()] }
	Collection<String> allNames()      { propertyNames() }

	Map<String, ?> modifiedProperties() {
		modifiedProperties propertyNames()
	}
}
