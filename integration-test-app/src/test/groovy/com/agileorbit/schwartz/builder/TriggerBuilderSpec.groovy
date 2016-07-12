package com.agileorbit.schwartz.builder

import org.quartz.SimpleTrigger

import static com.agileorbit.schwartz.builder.RepeatMode.Hours
import static org.quartz.CalendarIntervalScheduleBuilder.calendarIntervalSchedule
import static org.quartz.CronScheduleBuilder.cronSchedule
import static org.quartz.DailyTimeIntervalScheduleBuilder.dailyTimeIntervalSchedule
import static org.quartz.SimpleScheduleBuilder.repeatHourlyForTotalCount

// most properties are checked in the ScheduleBuilder tests, so only the remaining few are tested here.
class TriggerBuilderSpec extends AbstractBuilderSpec {

	private static final String cron = '0/6 * 15 * * ?'
	private static final int hours = 10
	private static final int intervalInDays = 200
	private static final String jobGroup = 'jGroup'
	private static final String jobName = 'jName'
	private static final int totalCount = 3
	private static final String triggerGroup = 'tGroup'
	private static final String triggerName = 'tName'

	void 'trigger properties only'() {
		when:
		builder
				.modifiedByCalendar(calendarName)
				.withDescription(description)
				.endAt(endTime)
				.usingJobData(jobDataMap)
				.forJob(jobKey)
				.withIdentity(triggerKey)
				.withPriority(priority)
				.startAt(startDate)

		factory
				.calendarName(calendarName)
				.description(description)
				.endAt(endTime)
				.jobDataMap(jobDataMap)
				.jobKey(jobKey)
				.key(triggerKey)
				.priority(priority)
				.startAt(startDate)

		build()

		then:
		usingQuartz instanceof SimpleTrigger
		usingPluginBuilder instanceof SimpleTrigger
		assertEqualValues COMMON_TRIGGER_NAMES
	}

	void 'name and jobName; calendar'() {
		when:
		builder
				.withIdentity(triggerName)
				.forJob(jobName)
				.startAt(startDate)
				.withSchedule(calendarIntervalSchedule().withIntervalInDays(intervalInDays))

		factory
				.name(triggerName)
				.jobName(jobName)
				.startAt(startDate)
				.intervalInDays(intervalInDays)

		build()

		then:
		assertEqualCalendar()
	}

	void 'name and group, jobName and jobGroup; calendar'() {
		when:
		builder
				.startAt(startDate)
				.withIdentity(triggerName, triggerGroup)
				.forJob(jobName, jobGroup)
				.withSchedule(calendarIntervalSchedule().withIntervalInDays(intervalInDays))

		factory
				.startAt(startDate)
				.name(triggerName).group(triggerGroup)
				.jobName(jobName).jobGroup(jobGroup)
				.intervalInDays(intervalInDays)

		build()

		then:
		assertEqualCalendar()
	}

	void 'jobDetail; calendar'() {
		when:
		builder
				.forJob(jobDetail)
				.withIdentity(triggerName)
				.startAt(startDate)
				.withSchedule(calendarIntervalSchedule().withIntervalInDays(intervalInDays))

		factory
				.jobDetail(jobDetail)
				.name(triggerName)
				.startAt(startDate)
				.intervalInDays(intervalInDays)

		build()

		then:
		assertEqualCalendar()
	}

	void 'name and jobName; cron'() {
		when:
		builder
				.withIdentity(triggerName)
				.forJob(jobName)
				.startAt(startDate)
				.withSchedule(cronSchedule(cron))

		factory
				.name(triggerName)
				.jobName(jobName)
				.startAt(startDate)
				.cronSchedule(cron)

		build()

		then:
		assertEqualCron()
	}

	void 'name and group, jobName and jobGroup; cron'() {
		when:
		builder
				.startAt(startDate)
				.withIdentity(triggerName, triggerGroup)
				.forJob(jobName, jobGroup)
				.withSchedule(cronSchedule(cron))

		factory
				.startAt(startDate)
				.name(triggerName).group(triggerGroup)
				.jobName(jobName).jobGroup(jobGroup)
				.cronSchedule(cron)

		build()

		then:
		assertEqualCron()
	}

	void 'jobDetail; cron'() {
		when:
		builder
				.forJob(jobDetail)
				.withIdentity(triggerName)
				.startAt(startDate)
				.withSchedule(cronSchedule(cron))

		factory
				.jobDetail(jobDetail)
				.name(triggerName)
				.startAt(startDate)
				.cronSchedule(cron)

		build()

		then:
		assertEqualCron()
	}

	void 'name and jobName; daily'() {
		when:
		builder
				.withIdentity(triggerName)
				.forJob(jobName)
				.startAt(startDate)
				.withSchedule(dailyTimeIntervalSchedule().onMondayThroughFriday())

		factory
				.name(triggerName)
				.jobName(jobName)
				.startAt(startDate)
				.mondayThroughFriday()

		build()

		then:
		assertEqualDaily()
	}

	void 'name and group, jobName and jobGroup; daily'() {
		when:
		builder
				.startAt(startDate)
				.withIdentity(triggerName, triggerGroup)
				.forJob(jobName, jobGroup)
				.withSchedule(dailyTimeIntervalSchedule().onMondayThroughFriday())

		factory
				.startAt(startDate)
				.name(triggerName).group(triggerGroup)
				.jobName(jobName).jobGroup(jobGroup)
				.mondayThroughFriday()

		build()

		then:
		assertEqualDaily()
	}

	void 'jobDetail; daily'() {
		when:
		builder
				.forJob(jobDetail)
				.withIdentity(triggerName)
				.startAt(startDate)
				.withSchedule(dailyTimeIntervalSchedule().onMondayThroughFriday())

		factory
				.jobDetail(jobDetail)
				.name(triggerName)
				.startAt(startDate)
				.mondayThroughFriday()

		build()

		then:
		assertEqualDaily()
	}

	void 'name and jobName; simple'() {
		when:
		builder
				.withIdentity(triggerName)
				.forJob(jobName)
				.startAt(startDate)
				.withSchedule(repeatHourlyForTotalCount(totalCount, hours))

		factory
				.name(triggerName)
				.jobName(jobName)
				.startAt(startDate)
				.repeatMode(Hours).totalCount(totalCount).hours(hours)

		build()

		then:
		assertEqualSimple()
	}

	void 'name and group, jobName and jobGroup; simple'() {
		when:
		builder
				.startAt(startDate)
				.withIdentity(triggerName, triggerGroup)
				.forJob(jobName, jobGroup)
				.withSchedule(repeatHourlyForTotalCount(totalCount, hours))

		factory
				.startAt(startDate)
				.name(triggerName).group(triggerGroup)
				.jobName(jobName).jobGroup(jobGroup)
				.repeatMode(Hours).totalCount(totalCount).hours(hours)

		build()

		then:
		assertEqualSimple()
	}

	void 'jobDetail; simple'() {
		when:
		builder
				.forJob(jobDetail)
				.withIdentity(triggerName)
				.startAt(startDate)
				.withSchedule(repeatHourlyForTotalCount(totalCount, hours))

		factory
				.jobDetail(jobDetail)
				.name(triggerName)
				.startAt(startDate)
				.repeatMode(Hours).totalCount(totalCount).hours(hours)

		build()

		then:
		assertEqualSimple()
	}

	void 'startDelay'() {
		when:
		builder
				.forJob(jobDetail)
				.withIdentity(triggerName)
				.startAt(new Date(System.currentTimeMillis() + 2500))

		factory
				.jobDetail(jobDetail)
				.name(triggerName)
				.startDelay(2500)
				.noRepeat()

		build()

		then:
		assertEqualSimple(true)
	}

	void 'startNow'() {
		when:
		builder
				.forJob(jobDetail)
				.withIdentity(triggerName)

		factory
				.jobDetail(jobDetail)
				.name(triggerName)
				.noRepeat()
				.startNow()

		build()

		then:
		assertEqualSimple(true)
	}
}
