package com.agileorbit.schwartz.builder

import org.quartz.CronExpression
import org.quartz.JobDataMap
import org.quartz.impl.triggers.CronTriggerImpl
import org.quartz.spi.MutableTrigger

import static com.agileorbit.schwartz.builder.HourAndMinuteMode.DailyAt
import static com.agileorbit.schwartz.builder.HourAndMinuteMode.DaysOfWeek
import static com.agileorbit.schwartz.builder.HourAndMinuteMode.Monthly
import static com.agileorbit.schwartz.builder.HourAndMinuteMode.Weekly
import static com.agileorbit.schwartz.builder.MisfireHandling.DoNothing
import static com.agileorbit.schwartz.builder.MisfireHandling.FireAndProceed
import static com.agileorbit.schwartz.builder.MisfireHandling.IgnoreMisfires
import static org.quartz.CronScheduleBuilder.atHourAndMinuteOnGivenDaysOfWeek
import static org.quartz.CronScheduleBuilder.cronSchedule
import static org.quartz.CronScheduleBuilder.cronScheduleNonvalidatedExpression
import static org.quartz.CronScheduleBuilder.dailyAtHourAndMinute
import static org.quartz.CronScheduleBuilder.monthlyOnDayAndHourAndMinute
import static org.quartz.CronScheduleBuilder.weeklyOnDayAndHourAndMinute
import static org.quartz.DateBuilder.SUNDAY
import static org.quartz.Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY

class CronTriggerSpec extends AbstractBuilderSpec {

	private static final String cron = '0/6 * 15 * * ?'
	private static final CronExpression cronEx = new CronExpression(cron)

	void 'from Quartz example3'() {
		when:
		String cron = '0,30 * * ? * SAT,SUN'
		String triggerName = 'trigger1'
		usingQuartz = (MutableTrigger) builder.withIdentity(triggerName)
				.forJob(schwartzJob.jobName, schwartzJob.jobGroup)
				.withSchedule(cronSchedule(cron))
				.build()

		factory
				.name(triggerName)
				.job(schwartzJob)
				.cronSchedule('0,30 * * ? * SAT,SUN')

		usingPluginBuilder = factory.build()

		then:
		assertEqualCron()
	}

	void 'from CronTriggerImplSerializationTest'() {
		when:
		Date start = new Date(0)
		Date end = new Date(10000)


		String triggerName = 'triggerName'
		String triggerGroup = 'triggerGroup'
		String jobName = 'jobName'
		String jobGroup = 'jobGroup'
		String cron = '0 0 12 * * ?'
		String description = 'A Trigger'
		JobDataMap jobDataMap = new JobDataMap(foo: 'bar')
		String calendarName = 'calendarName'
		int priority = 3

		usingQuartz = new CronTriggerImpl(triggerName, triggerGroup, jobName, jobGroup, start,
				end, cron, timeZone)
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
				.cronSchedule(cron)
				.timeZone(timeZone)
				.description(description)
				.jobDataMap(jobDataMap)
				.calendarName(calendarName)
				.priority(priority)
				.misfireHandling(IgnoreMisfires)
		usingPluginBuilder = factory.build()

		then:
		assertEqualCron()

		when:
		usingPluginBuilder = BuilderFactory.build(
				name: triggerName, group: triggerGroup, jobName: jobName, jobGroup: jobGroup,
				startAt: start, endAt: end, cronSchedule: cron, timeZone: timeZone,
				description: description, jobDataMap: jobDataMap, calendarName: calendarName,
				misfireHandling: IgnoreMisfires, priority: priority)

		then:
		assertEqualCron()
	}

	void 'cronSchedule String'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(cronSchedule(cron))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.cronSchedule(cron)

		build()

		then:
		assertEqualCron()
	}

	void 'cronScheduleNonvalidatedExpression'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(cronScheduleNonvalidatedExpression(cron))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.cronScheduleNonvalidated(cron)

		build()

		then:
		assertEqualCron()
	}

	void 'CronExpression'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(cronSchedule(cronEx))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.cronExpression(cronEx)

		build()

		then:
		assertEqualCron()
	}

	void 'TimeZone'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(cronSchedule(cronEx).inTimeZone(timeZone))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.cronExpression(cronEx)
				.timeZone(timeZone)

		build()

		then:
		assertEqualCron()
	}

	void 'withMisfireHandlingInstructionDoNothing'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(cronSchedule(cronEx)
					.withMisfireHandlingInstructionDoNothing())

		factory
				.key(triggerKey)
				.startAt(startDate)
				.cronExpression(cronEx)
				.misfireHandling(DoNothing)

		build()

		then:
		assertEqualCron()
	}

	void 'withMisfireHandlingInstructionFireAndProceed'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(cronSchedule(cronEx)
					.withMisfireHandlingInstructionFireAndProceed())

		factory
				.key(triggerKey)
				.startAt(startDate)
				.cronExpression(cronEx)
				.misfireHandling(FireAndProceed)

		build()

		then:
		assertEqualCron()
	}

	void 'withMisfireHandlingInstructionIgnoreMisfires'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(cronSchedule(cronEx)
					.withMisfireHandlingInstructionIgnoreMisfires())

		factory
				.key(triggerKey)
				.startAt(startDate)
				.cronExpression(cronEx)
				.misfireHandling(IgnoreMisfires)

		build()

		then:
		assertEqualCron()
	}

	void 'atHourAndMinuteOnGivenDaysOfWeek'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(atHourAndMinuteOnGivenDaysOfWeek(1, 1, SUNDAY))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.hourAndMinuteMode(DaysOfWeek).hour(1).minute(1).days(SUNDAY)

		build()

		then:
		assertEqualCron()
	}

	void 'dailyAtHourAndMinute'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(dailyAtHourAndMinute(1, 1))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.hourAndMinuteMode(DailyAt).hour(1).minute(1)

		build()

		then:
		assertEqualCron()
	}

	void 'weeklyOnDayAndHourAndMinute'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(weeklyOnDayAndHourAndMinute(1, 1, 1))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.hourAndMinuteMode(Weekly).day(1).hour(1).minute(1)

		build()

		then:
		assertEqualCron()
	}

	void 'monthlyOnDayAndHourAndMinute'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(monthlyOnDayAndHourAndMinute(1, 1, 1))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.hourAndMinuteMode(Monthly).day(1).hour(1).minute(1)

		build()

		then:
		assertEqualCron()
	}
}
