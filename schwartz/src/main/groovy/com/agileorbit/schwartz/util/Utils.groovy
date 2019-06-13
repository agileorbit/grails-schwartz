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
package com.agileorbit.schwartz.util

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.quartz.CalendarIntervalTrigger
import org.quartz.CronTrigger
import org.quartz.DailyTimeIntervalTrigger
import org.quartz.JobDetail
import org.quartz.Scheduler
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import org.quartz.Trigger.TriggerState
import org.quartz.spi.JobStore
import org.springframework.core.Constants

/**
 * Utility methods.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class Utils {

	private static final int[] mods = [1000, 60, 60, 24] as int[]

	/**
	 * The names of the database tables used when JDBC storage is enabled (without
	 * the prefix which is added at runtime based on the config), ordered based
	 * on foreign keys to ensure that QuartzService.purgeTables() deletes data
	 * in the correct order.
	 */
	public static final List<String> ALL_TABLE_NAMES = [
			'FIRED_TRIGGERS', 'PAUSED_TRIGGER_GRPS', 'SCHEDULER_STATE', 'LOCKS',
			'SIMPLE_TRIGGERS', 'SIMPROP_TRIGGERS', 'CRON_TRIGGERS', 'BLOB_TRIGGERS',
			'TRIGGERS', 'JOB_DETAILS', 'CALENDARS'].asImmutable()

	/**
	 * Names of properties in the standard trigger types; used internally by describeTrigger().
	 */
	public static final Map<String, List<String>> TRIGGER_PROPERTY_NAMES = [
		shared : ['calendarName', 'description', 'endTime', 'finalFireTime',
		          'jobKey', 'key', 'misfireInstruction', 'priority',
		          'startTime'].asImmutable(),
		(CalendarIntervalTrigger.simpleName) : [
				'preserveHourOfDayAcrossDaylightSavings', 'repeatInterval',
				'repeatIntervalUnit', 'skipDayIfHourDoesNotExist',
				'timeZone'].asImmutable(),
		(CronTrigger.simpleName) : ['cronExpression', 'timeZone'].asImmutable(),
		(DailyTimeIntervalTrigger.simpleName): [
				'daysOfWeek', 'endTimeOfDay', 'repeatCount', 'repeatInterval',
				'repeatIntervalUnit', 'startTimeOfDay'].asImmutable(),
		(SimpleTrigger.simpleName) : ['repeatCount', 'repeatInterval'].asImmutable()
	].asImmutable()

	/**
	 * Interfaces of the four standard trigger types in Quartz.
	 */
	public static final List<Class> TRIGGER_TYPES = [
			CalendarIntervalTrigger, CronTrigger,
			DailyTimeIntervalTrigger, SimpleTrigger].asImmutable()

	/**
	 * The default date format used throughout the plugin.
	 */
	public static final String DATE_FORMAT = 'YYYY-MM-dd HH:mm:ss.SSS'

	/**
	 * A replacement for toString() that includes more information.
	 *
	 * @param detail a JobDetail
	 * @return the description
	 */
	static String describeJobDetail(JobDetail detail) {
		boolean notConcurrent = detail.concurrentExectionDisallowed
		boolean persist = detail.persistJobDataAfterExecution
		Map props = [key: detail.key, jobClass: detail.jobClass?.name,
		             description: "'" + detail.description + "'",
		             durable: detail.durable,
		             requestsRecovery: detail.requestsRecovery(),
		             concurrentExectionDisallowed: notConcurrent,
		             persistJobDataAfterExecution: persist]
		mapToString(props, false) + ' (' +
				(notConcurrent && persist ? 'stateful' : 'stateless') + ')'
	}

	/**
	 * A replacement for toString() that includes more information.
	 *
	 * @param trigger a trigger
	 * @param triggerState optional trigger state
	 * @return the description
	 */
	static String describeTrigger(Trigger trigger, TriggerState triggerState = null) {
		Collection<String> names = []
		names.addAll TRIGGER_PROPERTY_NAMES.shared

		Constants constants
		Class type = TRIGGER_TYPES.find { trigger in it }
		if (type) {
			names.addAll TRIGGER_PROPERTY_NAMES[type.simpleName]
			constants = new Constants(type)
		}
		else {
			constants = new Constants(Trigger)
		}

		Map props = removeNullValues(trigger.properties.subMap(names))

		if (props.timeZone instanceof TimeZone) {
			props.timeZone = ((TimeZone)props.timeZone).ID
		}

		for (name in ([] + props.keySet())) {
			if (props[name] instanceof Date) {
				props[name] = ((Date)props[name]).format(DATE_FORMAT)
			}
		}

		try {
			props.misfireInstruction = constants.toCode(props.misfireInstruction,
					'MISFIRE_INSTRUCTION_') - 'MISFIRE_INSTRUCTION_'
		}
		catch (ignored) {}

		if (triggerState) props.triggerState = triggerState

		String className = trigger.getClass().name
		(type ? type.simpleName + ' (' + className + ')' : className) + ': ' + mapToString(props, true)
	}

	/**
	 * Removes all entries that have a null value.
	 *
	 * @param map a map
	 * @return the map after updating it
	 */
	static Map removeNullValues(Map map) {
		map.keySet().retainAll { map[it] != null }
		map
	}

	/**
	 * A human-readable description of the time delta between now and the
	 * specified date for use when the date is the value returned from Quartz
	 * for the next fire time of a trigger.
	 *
	 * @param fireTime the date
	 * @return the description of the time delta
	 */
	static String describeNextFireTime(Date fireTime) {
		long delta = fireTime.time - System.currentTimeMillis()
		if (delta < 1) {
			return 'immediately'
		}

		String when = 'in ' + delta + 'ms'
		if (delta > 60000) when += ' (' + dhms(delta) + ')'
		when
	}

	/**
	 * An alternate to toString() for a map, optionally sorting keys first.
	 *
	 * @param map a map
	 * @param sort whether to sort keys
	 * @return the description
	 */
	static String mapToString(Map map, boolean sort) {
		Collection<String> names = map.keySet()
		if (sort) names = names.sort()
		names.collect { String name -> name + ': ' + map[name] }.join(', ')
	}

	/**
	 * Formats milliseconds as days, hours, minutes, seconds, and milliseconds.
	 * Always displays minutes, seconds, and milliseconds as XX:XX.XXX, and
	 * displays hours if greater than 0 (XX:XX:XX.XXX), and days if greater
	 * than 0 (XX:XX:XX:XX.XXX). All sections are padded with 0, so for example
	 * 2 hours, 3 minutes, and 14 milliseconds would result in 02:03:014.
	 *
	 * @param milliseconds the number of milliseconds
	 * @return the formatted time
	 */
	static String dhms(long milliseconds) {
		List values = [milliseconds]
		for (int m in mods) values.add 0, (long)(values[0] / m)
		StringBuilder s = new StringBuilder()
		for (int i = 0; i < 5; i++) {
			long value = values[i] % mods[-i]
			if (i < 2 && !s && !value) continue
			if (i == 4 && value < 100) s << '0'
			if (value < 10) s << '0'
			s << value << (i == 4 ? '' : i == 3 ? '.' : ':')
		}
		s.toString()
	}

	/**
	 * The string returned from <code>toString()</code> if <code>toString()</code>
	 * and <code>hashCode()</code> from <code>Object</code> are not overridden,
	 * i.e. the full class name including package followed by '@' followed by the
	 * default hash code in hex format.
	 *
	 * @param object any object
	 * @return the description
	 */
	static String defaultObjectToString(object) {
		object == null ? 'null' : object.getClass().name + '@' +
				Integer.toHexString(System.identityHashCode(object))
	}

	/**
	 * A simple approach to getting the property names and values from an object,
	 * using Groovy's <code>getProperties()</code> method (with the property
	 * for the class removed).
	 *
	 * @param object any object
	 * @return the properties map
	 */
	static Map values(object) {
		Map properties = removeNullValues(object.properties)
		properties.remove 'class'
		properties
	}

	/**
	 * Uses non-static property access to get the <code>JobStore</code> from
	 * the <code>Scheduler</code>, necessary because the intermediate methods
	 * are not public.
	 *
	 * @param quartzScheduler the scheduler
	 * @return the job store
	 */
	@CompileDynamic
	static JobStore getJobStore(Scheduler quartzScheduler) {
		quartzScheduler.sched.resources.jobStore
	}
}
