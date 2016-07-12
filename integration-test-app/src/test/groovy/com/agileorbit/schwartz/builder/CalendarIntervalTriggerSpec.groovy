package com.agileorbit.schwartz.builder

import org.quartz.DateBuilder.IntervalUnit
import org.quartz.JobDataMap
import org.quartz.impl.triggers.CalendarIntervalTriggerImpl

import static com.agileorbit.schwartz.builder.BuilderType.calendar
import static com.agileorbit.schwartz.builder.MisfireHandling.DoNothing
import static com.agileorbit.schwartz.builder.MisfireHandling.FireAndProceed
import static com.agileorbit.schwartz.builder.MisfireHandling.IgnoreMisfires
import static org.quartz.CalendarIntervalScheduleBuilder.calendarIntervalSchedule
import static org.quartz.DateBuilder.IntervalUnit.MILLISECOND
import static org.quartz.DateBuilder.IntervalUnit.MINUTE
import static org.quartz.Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY

class CalendarIntervalTriggerSpec extends AbstractBuilderSpec {

	private static final int interval = 123
	private static final int intervalInDays = 2
	private static final int intervalInHours = 4
	private static final int intervalInMinutes = 6
	private static final int intervalInMonths = 8
	private static final int intervalInSeconds = 10
	private static final int intervalInWeeks = 12
	private static final int intervalInYears = 14
	private static final boolean preserveHour = true
	private static final boolean skipDay = true
	private static final IntervalUnit unit = MILLISECOND

	void 'based on CalendarIntervalTriggerImplSerializationTest'() {
		when:
		Date start = new Date(0)
		Date end = new Date(10000)
		String triggerName = 'triggerName'
		String triggerGroup = 'triggerGroup'
		String jobName = 'jobName'
		String jobGroup = 'jobGroup'
		int minutes = 5
		String description = 'A Trigger'
		JobDataMap jobDataMap = new JobDataMap(foo: 'bar')
		String calendarName = 'calendarName'
		int priority = 3

		usingQuartz = new CalendarIntervalTriggerImpl(
				triggerName, triggerGroup, jobName, jobGroup,
				start, end, MINUTE, minutes)
		usingQuartz.setTimeZone(timeZone)
		usingQuartz.setPreserveHourOfDayAcrossDaylightSavings(true)
		usingQuartz.setSkipDayIfHourDoesNotExist(true)
		usingQuartz.setDescription(description)
		usingQuartz.setJobDataMap(jobDataMap)
		usingQuartz.setCalendarName(calendarName)
		usingQuartz.setMisfireInstruction(MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY)
		usingQuartz.setPriority(priority)

		factory
				.name(triggerName)
				.group(triggerGroup)
				.jobName(jobName)
				.jobGroup(jobGroup)
				.startAt(start)
				.endAt(end)
				.intervalInMinutes(minutes)
				.timeZone(timeZone)
				.preserveHour()
				.skipDay()
				.description(description)
				.jobDataMap(jobDataMap)
				.calendarName(calendarName)
				.priority(priority)
				.misfireHandling(IgnoreMisfires)
		usingPluginBuilder = factory.build()

		then:
		assertEqualCalendar()

		when:
		usingPluginBuilder = BuilderFactory.build(
				name: triggerName, group: triggerGroup, jobName: jobName, jobGroup: jobGroup,
				startAt: start, endAt: end, intervalInMinutes: minutes, timeZone: timeZone,
				preserveHour: true, skipDay: true, description: description, jobDataMap: jobDataMap,
				calendarName: calendarName, misfireHandling: IgnoreMisfires, priority: priority)

		then:
		assertEqualCalendar()
	}

	void 'withInterval(timeInterval, unit)'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(calendarIntervalSchedule().withInterval(interval, unit))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.interval(interval)
				.unit(unit)
				.builderType(calendar)

		build()

		then:
		assertEqualCalendar()
	}

	void 'withIntervalInSeconds'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(calendarIntervalSchedule().withIntervalInSeconds(intervalInSeconds))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.intervalInSeconds(intervalInSeconds)
				.builderType(calendar)

		build()

		then:
		assertEqualCalendar()
	}

	void 'withIntervalInMinutes'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(calendarIntervalSchedule().withIntervalInMinutes(intervalInMinutes))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.intervalInMinutes(intervalInMinutes)
				.builderType(calendar)

		build()

		then:
		assertEqualCalendar()
	}

	void 'withIntervalInHours'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(calendarIntervalSchedule().withIntervalInHours(intervalInHours))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.intervalInHours(intervalInHours)
				.builderType(calendar)

		build()

		then:
		assertEqualCalendar()
	}

	void 'withIntervalInDays'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(calendarIntervalSchedule().withIntervalInDays(intervalInDays))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.intervalInDays(intervalInDays)

		build()

		then:
		assertEqualCalendar()
	}

	void 'withIntervalInWeeks'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(calendarIntervalSchedule().withIntervalInWeeks(intervalInWeeks))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.intervalInWeeks(intervalInWeeks)

		build()

		then:
		assertEqualCalendar()
	}

	void 'withIntervalInMonths'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(calendarIntervalSchedule().withIntervalInMonths(intervalInMonths))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.intervalInMonths(intervalInMonths)

		build()

		then:
		assertEqualCalendar()
	}

	void 'withIntervalInYears'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(calendarIntervalSchedule().withIntervalInYears(intervalInYears))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.intervalInYears(intervalInYears)

		build()

		then:
		assertEqualCalendar()
	}

	void 'timeZone, preserveHourOfDayAcrossDaylightSavings, skipDay'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(calendarIntervalSchedule()
					.withIntervalInYears(intervalInYears)
					.inTimeZone(timeZone)
					.preserveHourOfDayAcrossDaylightSavings(preserveHour)
					.skipDayIfHourDoesNotExist(skipDay))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.intervalInYears(intervalInYears)
				.timeZone(timeZone)
				.preserveHour(preserveHour)
				.skipDay(skipDay)

		build()

		then:
		assertEqualCalendar()
	}

	void 'withMisfireHandlingInstructionDoNothing'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(calendarIntervalSchedule()
					.withIntervalInYears(intervalInYears)
					.withMisfireHandlingInstructionDoNothing())

		factory
				.key(triggerKey)
				.startAt(startDate)
				.intervalInYears(intervalInYears)
				.misfireHandling(DoNothing)

		build()

		then:
		assertEqualCalendar()
	}

	void 'withMisfireHandlingInstructionFireAndProceed'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(calendarIntervalSchedule()
					.withIntervalInYears(intervalInYears)
					.withMisfireHandlingInstructionFireAndProceed())

		factory
				.key(triggerKey)
				.startAt(startDate)
				.intervalInYears(intervalInYears)
				.misfireHandling(FireAndProceed)

		build()

		then:
		assertEqualCalendar()
	}

	void 'withMisfireHandlingInstructionIgnoreMisfires'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(calendarIntervalSchedule()
					.withIntervalInYears(intervalInYears)
					.withMisfireHandlingInstructionIgnoreMisfires())

		factory
				.key(triggerKey)
				.startAt(startDate)
				.intervalInYears(intervalInYears)
				.misfireHandling(IgnoreMisfires)

		build()

		then:
		assertEqualCalendar()
	}
}
