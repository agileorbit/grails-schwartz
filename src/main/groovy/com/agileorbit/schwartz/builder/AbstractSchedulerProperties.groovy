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
 * Abstract base class for all ScheduleBuilder properties classes.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
abstract class AbstractSchedulerProperties extends AbstractProperties {

	protected static final Map<Class, List<String>> COMMON
	protected static final Map<Class, List<String>> ALL
	protected static final Map<Class, Map<String, List<String>>> GROUPS
	static {
		Map<Class, List<String>> common = [
			(CommonProperties)  : Collections.emptyList(),
			(CalendarProperties): ['interval', 'intervalInHours', 'intervalInMinutes',
			                       'intervalInSeconds', 'misfireHandling', 'timeZone', 'unit'],
			(CronProperties)    : ['days', 'misfireHandling', 'timeZone'],
			(DailyProperties)   : ['days', 'interval', 'intervalInHours', 'intervalInMinutes',
			                       'intervalInSeconds', 'misfireHandling', 'repeatCount', 'unit'],
			(SimpleProperties)  : ['intervalInHours', 'intervalInMinutes', 'intervalInSeconds',
			                       'misfireHandling', 'repeatCount'],
			(TriggerProperties) : Collections.emptyList()
		] as Map
		COMMON = mapAndListsAsImmutable(common)

		Map<Class, List<String>> all = [:]
		for (Class key in PROPERTIES.keySet()) {
			List<String> combined
			all[key] = combined = []
			combined.addAll PROPERTIES[key]
			combined.addAll common[key]
			combined.sort()
		}
		ALL = mapAndListsAsImmutable(all)

		Map<Class, Map<String, List<String>>> groups = [
			(CalendarProperties): [
				interval:   ['intervalInSeconds', 'intervalInMinutes', 'intervalInHours',
				             'intervalInDays', 'intervalInWeeks', 'intervalInMonths',
				             'intervalInYears']
			],
			(CronProperties): [
				day:        ['day', 'days', 'hour', 'hourAndMinuteMode', 'minute'],
				expression: ['cronExpression', 'cronSchedule', 'cronScheduleNonvalidated']
			],
			(DailyProperties): [
				days:       ['days', 'everyDay', 'mondayThroughFriday', 'saturdayAndSunday'],
				interval:   ['intervalInSeconds', 'intervalInMinutes', 'intervalInHours']
			],
			(SimpleProperties): [
				interval:   ['intervalInHours', 'intervalInMillis', 'intervalInMinutes', 'intervalInSeconds'],
				repeat:     ['hours', 'minutes', 'seconds']
			]
		] as Map

		for (Class key in copyAsSet(groups.keySet())) {
			groups[key] = mapAndListsAsImmutable(groups[key])
		}
		GROUPS = groups.asImmutable()
	}

	protected final CommonProperties commonProperties

	protected AbstractSchedulerProperties(CommonProperties common, BuilderFactory factory) {
		super(factory)
		commonProperties = common
	}

	protected Collection<String> commonPropertyNames() { COMMON[getClass()] }

	protected Map<String, ?> modifiedProperties(String propertyGroup) {
		modifiedProperties GROUPS[getClass()][propertyGroup]
	}

	protected propertyValue(String name) {
		(name in commonPropertyNames() ? commonProperties : this)[name]
	}

	Collection<String> allNames() { ALL[getClass()] }
}
