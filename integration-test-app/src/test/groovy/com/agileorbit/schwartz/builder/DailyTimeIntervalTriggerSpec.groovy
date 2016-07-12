package com.agileorbit.schwartz.builder

import org.quartz.JobDataMap
import org.quartz.TimeOfDay
import org.quartz.impl.triggers.DailyTimeIntervalTriggerImpl

import static com.agileorbit.schwartz.builder.BuilderFactory.builder
import static com.agileorbit.schwartz.builder.BuilderType.daily
import static com.agileorbit.schwartz.builder.MisfireHandling.DoNothing
import static com.agileorbit.schwartz.builder.MisfireHandling.FireAndProceed
import static com.agileorbit.schwartz.builder.MisfireHandling.IgnoreMisfires
import static org.quartz.DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule
import static org.quartz.DateBuilder.IntervalUnit.MINUTE
import static org.quartz.DateBuilder.MONDAY
import static org.quartz.DateBuilder.WEDNESDAY
import static org.quartz.Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY

class DailyTimeIntervalTriggerSpec extends AbstractBuilderSpec {

	private static final int count = 9
	private static final int interval = 78
	private static final int intervalInHours = 34
	private static final int intervalInMinutes = 45
	private static final int intervalInSeconds = 56
	private static final int repeatCount = 67
	private static final Integer[] days = [MONDAY, WEDNESDAY]
	private static final TimeOfDay dailyStartTime = new TimeOfDay(10, 45, 30)
	private static final TimeOfDay dailyEndTime = new TimeOfDay(23, 30, 15)

	void 'from DailyTimeIntervalTriggerImplSerializationTest'() {
		when:
		Date start = new Date(0)
		Date end = new Date(10000)

		usingQuartz = new DailyTimeIntervalTriggerImpl('triggerName', 'triggerGroup', 'jobName', 'jobGroup',
				start, end, dailyStartTime, dailyEndTime, MINUTE,  5)
		usingQuartz.setDescription('A Trigger')
		usingQuartz.setJobDataMap(new JobDataMap(foo: 'bar'))
		usingQuartz.setCalendarName('calendarName')
		usingQuartz.setMisfireInstruction(MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY)
		usingQuartz.setPriority(3)

		usingPluginBuilder = builder('triggerName', 'triggerGroup')
				.jobName('jobName').jobGroup('jobGroup')
				.startAt(start).endAt(end)
				.dailyStart(dailyStartTime).dailyEnd(dailyEndTime)
				.intervalInMinutes(5)
				.description('A Trigger')
				.jobData(foo: 'bar')
				.calendarName('calendarName')
				.misfireHandling(IgnoreMisfires)
				.priority(3)
				.build()

		then:
		assertEqualDaily()
	}

	void 'endingDailyAfterCount, withInterval(timeInterval, IntervalUnit)'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(dailyTimeIntervalSchedule()
					.withInterval(interval, MINUTE)
					.startingDailyAt(dailyStartTime)
					.endingDailyAfterCount(count))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.interval(interval)
				.unit(MINUTE)
				.dailyStart(dailyStartTime)
				.dailyEndAfterCount(count)

		build()

		then:
		assertEqualDaily()
	}

	void 'endingDailyAt, withIntervalInHours'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(dailyTimeIntervalSchedule()
					.withIntervalInHours(intervalInHours)
					.endingDailyAt(dailyEndTime))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.intervalInHours(intervalInHours)
				.dailyEnd(dailyEndTime)

		build()

		then:
		assertEqualDaily()
	}

	void 'onDaysOfTheWeek, withIntervalInMinutes'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(dailyTimeIntervalSchedule()
					.withIntervalInMinutes(intervalInMinutes)
					.onDaysOfTheWeek(days))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.intervalInMinutes(intervalInMinutes)
				.days(days)

		build()

		then:
		assertEqualDaily()
	}

	void 'onEveryDay, withIntervalInSeconds'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(dailyTimeIntervalSchedule()
					.withIntervalInSeconds(intervalInSeconds)
					.onEveryDay())

		factory
				.key(triggerKey)
				.startAt(startDate)
				.intervalInSeconds(intervalInSeconds)
				.everyDay()

		build()

		then:
		assertEqualDaily()
	}

	void 'onMondayThroughFriday, withMisfireHandlingInstructionDoNothing'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(dailyTimeIntervalSchedule()
					.onMondayThroughFriday()
					.withMisfireHandlingInstructionDoNothing())

		factory
				.key(triggerKey)
				.startAt(startDate)
				.mondayThroughFriday()
				.misfireHandling(DoNothing)

		build()

		then:
		assertEqualDaily()
	}

	void 'onSaturdayAndSunday, withMisfireHandlingInstructionFireAndProceed'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(dailyTimeIntervalSchedule()
					.onSaturdayAndSunday()
					.withMisfireHandlingInstructionFireAndProceed())

		factory
				.key(triggerKey)
				.startAt(startDate)
				.saturdayAndSunday()
				.misfireHandling(FireAndProceed)

		build()

		then:
		assertEqualDaily()
	}

	void 'startingDailyAt, withMisfireHandlingInstructionIgnoreMisfires'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(dailyTimeIntervalSchedule()
				.startingDailyAt(dailyStartTime).withMisfireHandlingInstructionIgnoreMisfires())

		factory
				.key(triggerKey)
				.startAt(startDate)
				.dailyStart(dailyStartTime)
				.misfireHandling(IgnoreMisfires)

		build()

		then:
		assertEqualDaily()
	}

	void 'withRepeatCount'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(dailyTimeIntervalSchedule().withRepeatCount(repeatCount))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.repeatCount(repeatCount)
				.builderType(daily)

		build()

		then:
		assertEqualDaily()
	}
}
