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
import org.quartz.CalendarIntervalTrigger
import org.quartz.CronTrigger
import org.quartz.DailyTimeIntervalTrigger
import org.quartz.DateBuilder.IntervalUnit
import org.quartz.JobKey
import org.quartz.SimpleTrigger
import org.quartz.TimeOfDay
import org.quartz.Trigger
import org.quartz.TriggerKey
import org.quartz.spi.OperableTrigger

/**
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class TriggerVO extends AbstractVO {

	// Trigger
	final boolean mayFireAgain
	final TriggerKey key
	final JobKey jobKey
	final String description
	final String calendarName
	final Map jobDataMap
	final int priority
	final Long startTime
	final Long endTime
	final Long nextFireTime
	final Long previousFireTime
	final Long finalFireTime
	final int misfireInstruction

	// CalendarIntervalTrigger
	// final IntervalUnit repeatIntervalUnit
	// final int repeatInterval
	// final int timesTriggered
	// final String timeZone
	final Boolean preserveHourOfDayAcrossDaylightSavings
	final Boolean skipDayIfHourDoesNotExist

	// CronTrigger
	final String cronExpression
	final String timeZone
	final String expressionSummary

	// DailyTimeIntervalTrigger
	final IntervalUnit repeatIntervalUnit
	final Integer repeatCount
	final Long repeatInterval
	final TimeOfDay startTimeOfDay
	final TimeOfDay endTimeOfDay
	final Set<Integer> daysOfWeek
	final Integer timesTriggered

	// OperableTrigger
	final String fireInstanceId

	// SimpleTrigger
	// final int repeatCount
	// final long repeatInterval
	// final int timesTriggered

	TriggerVO(Trigger trigger, JobEvent event = JobEvent.NO_EVENT) {
		this.event = event
		key = trigger.key
		jobKey = trigger.jobKey
		description = trigger.description
		calendarName = trigger.calendarName
		jobDataMap = ([:] + trigger.jobDataMap.getWrappedMap()).asImmutable()
		priority = trigger.priority
		startTime = trigger.startTime?.time
		endTime = trigger.endTime?.time
		nextFireTime = trigger.nextFireTime?.time
		previousFireTime = trigger.previousFireTime?.time
		finalFireTime = trigger.finalFireTime?.time
		misfireInstruction = trigger.misfireInstruction
		mayFireAgain = trigger.mayFireAgain()

		if (trigger instanceof DailyTimeIntervalTrigger || trigger instanceof SimpleTrigger) {
			repeatCount = (int) trigger['repeatCount']
		}

		if (trigger instanceof CalendarIntervalTrigger ||
				trigger instanceof DailyTimeIntervalTrigger ||
				trigger instanceof SimpleTrigger) {
			repeatInterval = (int) trigger['repeatInterval']
		}

		if (trigger instanceof CalendarIntervalTrigger || trigger instanceof DailyTimeIntervalTrigger) {
			repeatIntervalUnit = (IntervalUnit) trigger['repeatIntervalUnit']
		}

		if (trigger instanceof CalendarIntervalTrigger ||
				trigger instanceof DailyTimeIntervalTrigger ||
				trigger instanceof SimpleTrigger) {
			timesTriggered = (int) trigger['timesTriggered']
		}

		if (trigger instanceof CalendarIntervalTrigger || trigger instanceof CronTrigger) {
			timeZone = ((TimeZone) trigger['timeZone']).ID
		}

		if (trigger instanceof CalendarIntervalTrigger) {
			repeatIntervalUnit = trigger.repeatIntervalUnit
			repeatInterval = trigger.repeatInterval
			timesTriggered = trigger.timesTriggered
			timeZone = trigger.timeZone.ID
			preserveHourOfDayAcrossDaylightSavings = trigger.preserveHourOfDayAcrossDaylightSavings
			skipDayIfHourDoesNotExist = trigger.skipDayIfHourDoesNotExist
		}

		if (trigger instanceof CronTrigger) {
			cronExpression = trigger.cronExpression
			timeZone = trigger.timeZone.ID
			// expressionSummary = trigger.expressionSummary
		}

		if (trigger instanceof DailyTimeIntervalTrigger) {
			repeatIntervalUnit = trigger.repeatIntervalUnit
			repeatInterval = trigger.repeatInterval
			startTimeOfDay = trigger.startTimeOfDay
			endTimeOfDay = trigger.endTimeOfDay
			daysOfWeek = (([] + trigger.daysOfWeek) as Set).asImmutable()
			timesTriggered = trigger.timesTriggered
		}

		if (trigger instanceof SimpleTrigger) {
			repeatInterval = trigger.repeatInterval
			timesTriggered = trigger.timesTriggered
		}

		if (trigger instanceof OperableTrigger) {
			fireInstanceId = trigger.fireInstanceId
		}
	}

	String toString() {
		event.indent++
		def tabs = '\t' * event.indent
		String s = '\n' + tabs + properties().entrySet().join(',\n' + tabs)
		event.indent--
		s
	}
}
