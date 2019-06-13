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

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.quartz.CalendarIntervalScheduleBuilder
import org.quartz.CronScheduleBuilder
import org.quartz.DailyTimeIntervalScheduleBuilder
import org.quartz.JobDataMap
import org.quartz.ScheduleBuilder
import org.quartz.SimpleScheduleBuilder
import org.quartz.TriggerBuilder
import org.quartz.spi.MutableTrigger

import static com.agileorbit.schwartz.builder.BuilderType.calendar
import static com.agileorbit.schwartz.builder.BuilderType.cron
import static com.agileorbit.schwartz.builder.BuilderType.daily
import static com.agileorbit.schwartz.builder.BuilderType.simple
import static com.agileorbit.schwartz.builder.HourAndMinuteMode.DailyAt
import static com.agileorbit.schwartz.builder.HourAndMinuteMode.DaysOfWeek
import static com.agileorbit.schwartz.builder.HourAndMinuteMode.Monthly
import static com.agileorbit.schwartz.builder.HourAndMinuteMode.Weekly
import static com.agileorbit.schwartz.builder.RepeatMode.Hours
import static com.agileorbit.schwartz.builder.RepeatMode.Minutes
import static com.agileorbit.schwartz.builder.RepeatMode.Seconds
import static org.quartz.CalendarIntervalScheduleBuilder.calendarIntervalSchedule
import static org.quartz.CronScheduleBuilder.atHourAndMinuteOnGivenDaysOfWeek
import static org.quartz.CronScheduleBuilder.cronSchedule
import static org.quartz.CronScheduleBuilder.cronScheduleNonvalidatedExpression
import static org.quartz.CronScheduleBuilder.dailyAtHourAndMinute
import static org.quartz.CronScheduleBuilder.monthlyOnDayAndHourAndMinute
import static org.quartz.CronScheduleBuilder.weeklyOnDayAndHourAndMinute
import static org.quartz.DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule
import static org.quartz.Scheduler.DEFAULT_GROUP
import static org.quartz.SimpleScheduleBuilder.repeatHourlyForTotalCount
import static org.quartz.SimpleScheduleBuilder.repeatHourlyForever
import static org.quartz.SimpleScheduleBuilder.repeatMinutelyForTotalCount
import static org.quartz.SimpleScheduleBuilder.repeatMinutelyForever
import static org.quartz.SimpleScheduleBuilder.repeatSecondlyForTotalCount
import static org.quartz.SimpleScheduleBuilder.repeatSecondlyForever
import static org.quartz.SimpleScheduleBuilder.simpleSchedule
import static org.quartz.TriggerBuilder.newTrigger

/**
 * Aggregates all properties used to configure the standard Quartz Triggers
 * (SimpleTrigger, CalendarIntervalTrigger, DailyTimeIntervalTrigger, CronTrigger).
 * Properties can be set using regular setter methods (e.g.
 * <code>void setStartAt(Date)</code>) or Groovy property access (e.g.
 * <code>factory.startAt = ...</code>), and each property also has a variant
 * that sets the value and retuns the factory instance to support method
 * chaining (e.g. <code>BuilderFactory startAt(Date)</code>). Properties can
 * also be set in bulk from a Map (similar to using the Groovy map constructor).
 *
 * The property names are identical to or very similar to those used in the
 * schedule builder classes (CalendarIntervalScheduleBuilder, CronScheduleBuilder,
 * DailyTimeIntervalScheduleBuilder, and SimpleScheduleBuilder) and TriggerBuilder.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
@Slf4j
class BuilderFactory {

	protected static final String newline = System.getProperty('line.separator')

	protected static final Map<Class, BuilderType> typesByClass = [
			(CalendarProperties): calendar,
			(CronProperties):     cron,
			(DailyProperties):    daily,
			(SimpleProperties):   simple]

	protected List<AbstractSchedulerProperties> scheduleBuilderInstances = []
	protected List<AbstractProperties> allInstances = []

	@Delegate(excludes=['allNames', 'propertyNames', 'modifiedProperties'])
	protected CommonProperties commonProperties = new CommonProperties(this)

	@Delegate(excludes=['allNames', 'propertyNames', 'modifiedProperties', 'modifiedIntervalProperties'])
	protected CalendarProperties calendarProperties = createSchedulerProperties(CalendarProperties)

	@Delegate(excludes=['allNames', 'propertyNames', 'modifiedProperties', 'modifiedDayProperties', 'modifiedExpressionProperties'])
	protected CronProperties cronProperties = createSchedulerProperties(CronProperties)

	@Delegate(excludes=['allNames', 'propertyNames', 'modifiedProperties', 'modifiedDaysProperties', 'modifiedIntervalProperties'])
	protected DailyProperties dailyProperties = createSchedulerProperties(DailyProperties)

	@Delegate(excludes=['allNames', 'propertyNames', 'modifiedProperties', 'modifiedIntervalProperties', 'modifiedRepeatProperties'])
	protected SimpleProperties simpleProperties = createSchedulerProperties(SimpleProperties)

	@Delegate(excludes=['allNames', 'propertyNames', 'modifiedProperties'])
	protected TriggerProperties triggerProperties = new TriggerProperties(this)

	protected <S extends AbstractSchedulerProperties> S createSchedulerProperties(Class<S> clazz) {
		S schedulerProperties = (S) clazz.newInstance(commonProperties, this)
		scheduleBuilderInstances << schedulerProperties
		schedulerProperties
	}

	/**
	 * Creates a factory instance, sets its property values from the provided
	 * map, invokes the applicable ScheduleBuilder and TriggerBuilder methods
	 * for the provided data, and calls <code>build()</code> on the
	 * TriggerBuilder to build the Trigger.
	 *
	 * @param values schedule and trigger builder data
	 * @return the trigger
	 */
	static MutableTrigger build(Map<String, ?> values) {
		new BuilderFactory(values).build()
	}

	/**
	 * Creates a factory instance initialized with the trigger name and group.
	 * Set the remaining trigger values with setters or the chainable mutator
	 * methods, and call build() to create te Trigger.
	 *
	 * @param triggerName trigger name
	 * @param triggerGroup trigger group
	 * @return the factory
	 */
	static BuilderFactory builder(String triggerName = null, String triggerGroup = DEFAULT_GROUP) {
		new BuilderFactory().name(triggerName).group(triggerGroup)
	}

	BuilderType builderType
	ScheduleBuilder scheduleBuilder
	TriggerBuilder triggerBuilder

	/**
	 * Default constructor.
	 */
	BuilderFactory() {
		allInstances.addAll scheduleBuilderInstances
		allInstances << triggerProperties
	}

	/**
	 * Creates an instance and sets property values from the map.
	 *
	 * @param values ScheduleBuilder and TriggerBuilder data
	 */
	BuilderFactory(Map<String, ?> values) {
		this()

		log.debug 'Initializing from values: {}', values

		validateKeys values

		values.each { String key, value ->
			this[key] = value
			if (log.debugEnabled) {
				if (this[key] != value && key != 'days') {
					log.debug 'property {} was specified as <{}> but is <{}>', key, value, this[key]
				}
			}
		}
	}

	protected void validateKeys(Map<String, ?> values) {
		Set<String> invalidNames = []
		invalidNames.addAll values.keySet()
		invalidNames.remove 'builderType'
		for (AbstractProperties props in allInstances) {
			invalidNames.removeAll props.allNames()
		}

		if (invalidNames) {
			throw new IllegalArgumentException('Invalid propert' +
					(invalidNames.size() == 1 ? 'y' : 'ies') + ': ' +
					invalidNames.sort().join(', ') + '. Supported properties are:' + newline +
					allInstances.collect { (it.getClass().simpleName - 'Properties') + ': ' +
							it.allNames() }.join(newline))
		}
	}

	/**
	 * Sets the builder type using a chainable mutator, for cases where there aren't enough
	 * values set to determine it automatically.
	 *
	 * @param type the type
	 * @return the factory
	 */
	BuilderFactory builderType(BuilderType type) {
		builderType = type
		this
	}

	/**
	 * Determines which ScheduleBuilder type to use based on which values have
	 * been set, performs validation checks, and configures a ScheduleBuilder
	 * and a TriggerBuilder to build the Trigger.
	 *
	 * @return the trigger
	 */
	MutableTrigger build() {

		log.debug 'Building from values:\n  common: {}\ncalendar: {}\n    ' +
				'cron: {}\n   daily: {}\n  simple: {}\n trigger: {}',
				commonProperties.modifiedProperties(), calendarProperties.modifiedProperties(),
				cronProperties.modifiedProperties(), dailyProperties.modifiedProperties(),
				simpleProperties.modifiedProperties(), triggerProperties.modifiedProperties()

		if (!builderType) determineType()
		validate()

		switch (builderType) {
			case calendar: scheduleBuilder = calendarBuilder(); break
			case cron:     scheduleBuilder = cronBuilder(); break
			case daily:    scheduleBuilder = dailyBuilder(); break
			case simple:   scheduleBuilder = simpleBuilder(); break
		}

		triggerBuilder = triggerBuilder()

		if (scheduleBuilder) {
			if (misfireHandling) {
				applyMisfireHandling misfireHandling, scheduleBuilder
			}
			triggerBuilder.withSchedule scheduleBuilder
		}

		triggerBuilder.build()
	}

	protected CalendarIntervalScheduleBuilder calendarBuilder() {
		CalendarIntervalScheduleBuilder builder = calendarIntervalSchedule()

		if (timeZone)                       builder.inTimeZone timeZone
		if (preserveHour != null)           builder.preserveHourOfDayAcrossDaylightSavings preserveHour
		if (skipDay != null)                builder.skipDayIfHourDoesNotExist skipDay

		if (unit && interval != null)       builder.withInterval interval, unit
		else if (intervalInSeconds != null) builder.withIntervalInSeconds intervalInSeconds
		else if (intervalInMinutes != null) builder.withIntervalInMinutes intervalInMinutes
		else if (intervalInHours != null)   builder.withIntervalInHours intervalInHours
		else if (intervalInDays != null)    builder.withIntervalInDays intervalInDays
		else if (intervalInWeeks != null)   builder.withIntervalInWeeks intervalInWeeks
		else if (intervalInMonths != null)  builder.withIntervalInMonths intervalInMonths
		else if (intervalInYears != null)   builder.withIntervalInYears intervalInYears

		builder
	}

	protected CronScheduleBuilder cronBuilder() {
		CronScheduleBuilder builder = initialCronBuilder()

		if (timeZone) builder.inTimeZone timeZone

		builder
	}

	protected CronScheduleBuilder initialCronBuilder() {
		if (cronExpression) {
			cronSchedule cronExpression
		}
		else if (cronSchedule) {
			cronSchedule cronSchedule
		}
		else if (cronScheduleNonvalidated) {
			cronScheduleNonvalidatedExpression cronScheduleNonvalidated
		}
		else {
			switch (hourAndMinuteMode) {
				case DailyAt:    dailyAtHourAndMinute(hour, minute); break
				case DaysOfWeek: atHourAndMinuteOnGivenDaysOfWeek(hour, minute, days); break
				case Weekly:     weeklyOnDayAndHourAndMinute(day, hour, minute); break
				case Monthly:    monthlyOnDayAndHourAndMinute(day, hour, minute); break
			}
		}
	}

	protected DailyTimeIntervalScheduleBuilder dailyBuilder() {
		DailyTimeIntervalScheduleBuilder builder = dailyTimeIntervalSchedule()

		if (days)                           builder.onDaysOfTheWeek days
		else if (everyDay)                  builder.onEveryDay()
		else if (mondayThroughFriday)       builder.onMondayThroughFriday()
		else if (saturdayAndSunday)         builder.onSaturdayAndSunday()

		if (unit && interval != null)       builder.withInterval interval, unit
		else if (intervalInSeconds != null) builder.withIntervalInSeconds intervalInSeconds
		else if (intervalInHours != null)   builder.withIntervalInHours intervalInHours
		else if (intervalInMinutes != null) builder.withIntervalInMinutes intervalInMinutes

		if (repeatCount != null)            builder.withRepeatCount repeatCount

		// count, interval and startTimeOfDay have to be set before setting endTimeOfDay
		if (dailyStart)                     builder.startingDailyAt dailyStart
		if (dailyEndAfterCount)             builder.endingDailyAfterCount dailyEndAfterCount
		else if (dailyEnd)                  builder.endingDailyAt dailyEnd

		builder
	}

	protected SimpleScheduleBuilder simpleBuilder() {
		SimpleScheduleBuilder builder = initialSimpleBuilder()

		if (repeatMode == null) {
			if (intervalInMillis != null)        builder.withIntervalInMilliseconds intervalInMillis
			else if (intervalInSeconds != null)  builder.withIntervalInSeconds intervalInSeconds
			else if (intervalInMinutes != null)  builder.withIntervalInMinutes intervalInMinutes
			else if (intervalInHours != null)    builder.withIntervalInHours intervalInHours

			if (repeatCount != null)             builder.withRepeatCount repeatCount
			else                                 builder.repeatForever()
		}

		builder
	}

	protected SimpleScheduleBuilder initialSimpleBuilder() {
		if (repeatMode == null) {
			simpleSchedule()
		}
		else {
			switch (repeatMode) {
				case Seconds:
					if (totalCount == null) {
						if (seconds == null) repeatSecondlyForever()
						else                 repeatSecondlyForever(seconds)
					}
					else {
						if (seconds == null) repeatSecondlyForTotalCount(totalCount)
						else                 repeatSecondlyForTotalCount(totalCount, seconds)
					}
					break
				case Minutes:
					if (totalCount == null) {
						if (minutes == null) repeatMinutelyForever()
						else                 repeatMinutelyForever(minutes)
					}
					else {
						if (minutes == null) repeatMinutelyForTotalCount(totalCount)
						else                 repeatMinutelyForTotalCount(totalCount, minutes)
					}
					break
				case Hours:
					if (totalCount == null) {
						if (hours == null)   repeatHourlyForever()
						else                 repeatHourlyForever(hours)
					}
					else {
						if (hours == null)   repeatHourlyForTotalCount(totalCount)
						else                 repeatHourlyForTotalCount(totalCount, hours)
					}
					break
			}
		}
	}

	protected TriggerBuilder triggerBuilder() {
		TriggerBuilder builder = newTrigger()

		if (endAt)                    builder.endAt endAt

		if (job)                      builder.forJob job.jobKey
		else if (jobDetail)           builder.forJob jobDetail
		else if (jobKey)              builder.forJob jobKey
		else if (jobName && jobGroup) builder.forJob jobName, jobGroup
		else if (jobName)             builder.forJob jobName

		if (calendarName)             builder.modifiedByCalendar calendarName

		if (startAt)                  builder.startAt startAt

		if (jobDataMap)               builder.usingJobData jobDataMap
		else if (jobData)             builder.usingJobData new JobDataMap(jobData)

		if (description)              builder.withDescription description

		if (key)                      builder.withIdentity key
		else if (name && group)       builder.withIdentity name, group
		else if (name)                builder.withIdentity name

		if (priority != null)         builder.withPriority priority

		builder
	}

	protected void validate() {
		switch(builderType) {
			case calendar: validateCalendar(); break
			case cron:     validateCron(); break
			case daily:    validateDaily(); break
			case simple:   validateSimple(); break
		}
	}

	protected void validateCalendar() {
		if (unit && interval == null) {
			die 'IntervalUnit specified without time interval'
		}

		int intervalInCount = calendarProperties.modifiedIntervalProperties().size()

		if (intervalInCount > 1) {
			die 'Cannot set more than one "intervalIn" property'
		}

		if (intervalInCount && unit) {
			die 'Cannot set a "intervalIn" property and IntervalUnit with time interval'
		}

		validateMisfireHandling misfireHandling, CalendarIntervalScheduleBuilder
	}

	protected void validateCron() {
		int expressionCount = cronProperties.modifiedExpressionProperties().size()
		int dayPropertyCount = cronProperties.modifiedDayProperties().size()

		if (!expressionCount && !dayPropertyCount) {
			die 'Unable to determine builder method for CronScheduleBuilder ' +
					'without a cron expression or date/time properties'
		}

		if (expressionCount && dayPropertyCount) {
			die 'Cannot specify both a cron expression and date/time properties'
		}

		if (expressionCount > 1) {
			die 'Cannot specify more than one cron expression property'
		}

		if (expressionCount == 1) return

		if (hour == null) die '"hour" is required for HourAndMinuteMode'

		if (minute == null) die '"minute" is required for HourAndMinuteMode'

		if (day != null && days) {
			die 'Cannot specify both "day" and "days"'
		}

		switch (hourAndMinuteMode) {
			case Monthly:
			case Weekly:
				if (day == null) die '"day" is required for HourAndMinuteMode.Monthly ' +
						'and HourAndMinuteMode.Weekly'
				break
			case DaysOfWeek:
				if (!days) {
					die '"days" is required for for HourAndMinuteMode.DaysOfWeek'
				}
				break
		}

		validateMisfireHandling misfireHandling, CronScheduleBuilder
	}

	protected void validateDaily() {
		if (unit && interval == null) {
			die 'IntervalUnit specified without time interval'
		}

		int intervalInCount = dailyProperties.modifiedIntervalProperties().size()

		if (intervalInCount > 1) {
			die 'Cannot set more than one "intervalIn" property'
		}

		if (intervalInCount && unit && interval != null) {
			die 'Cannot set a "intervalIn" property and IntervalUnit with time interval'
		}

		if (dailyEndAfterCount != null && dailyEnd != null) {
			die 'Cannot specify both "dailyEndAfterCount" and "dailyEnd"'
		}

		if (dailyProperties.modifiedDaysProperties().size() > 1) {
			die 'Cannot specify more than one of "onDaysOfWeek", "everyDay", ' +
					'"mondayThroughFriday", or "saturdayAndSunday"'
		}

		validateMisfireHandling misfireHandling, DailyTimeIntervalScheduleBuilder
	}

	protected void validateSimple() {

		if (simpleProperties.modifiedIntervalProperties().size() > 1) {
			die 'Cannot set more than one "intervalIn" property'
		}

		if (simpleProperties.modifiedRepeatProperties().size() > 1) {
			die 'Cannot set more than one of "seconds", "minutes", or "hours"'
		}

		if (repeatForever && repeatCount != null) {
			die 'Cannot specify both "repeatForever" and "triggerRepeatCount"'
		}

		validateMisfireHandling misfireHandling, SimpleScheduleBuilder
	}

	protected void validateMisfireHandling(MisfireHandling misfireHandling, Class scheduleBuilderType) {
		if (misfireHandling && !misfireHandling.supports(builderType)) {
			die 'Misfire handling instruction ' + misfireHandling +
					' is not valid with ' + scheduleBuilderType.simpleName
		}
	}

	protected void die(String message) {
		throw new IllegalStateException(message)
	}

	protected void determineType() {
		Map<Class, Map<String, ?>> allModified = [:]
		for (AbstractSchedulerProperties properties in scheduleBuilderInstances) {
			Map<String, ?> modified = properties.modifiedProperties()
			if (modified) allModified[properties.getClass()] = modified
		}

		if (allModified.size() > 1) {
			die 'Unable to determine builder type, properties are set for multiple schedule builders:\n' +
					allModified.entrySet().collect { it.key.simpleName + ': ' + it.value }.join('\n')
		}

		if (allModified.size() == 1) {
			builderType = typesByClass[allModified.keySet().first()]
		}
		else {
			// only common properties were specified, but there are some easy special cases when no cron expression is set
			if (commonProperties.days) {
				builderType = daily
			}
			else if (commonProperties.timeZone) {
				builderType = calendar
			}
			else {
				// default
				builderType = simple
			}
		}

		log.debug 'Using builder type {}', builderType
	}

	@CompileDynamic
	protected void applyMisfireHandling(Enum misfireHandling, builder) {
		builder."withMisfireHandlingInstruction${misfireHandling.name()}"()
	}
}
