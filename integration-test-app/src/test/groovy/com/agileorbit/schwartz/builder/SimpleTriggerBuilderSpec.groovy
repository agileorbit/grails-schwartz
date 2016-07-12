package com.agileorbit.schwartz.builder

import org.quartz.DateBuilder.IntervalUnit
import org.quartz.JobDataMap
import org.quartz.impl.triggers.SimpleTriggerImpl
import org.quartz.spi.MutableTrigger

import static com.agileorbit.schwartz.builder.BuilderFactory.builder
import static com.agileorbit.schwartz.builder.MisfireHandling.FireNow
import static com.agileorbit.schwartz.builder.MisfireHandling.IgnoreMisfires
import static com.agileorbit.schwartz.builder.MisfireHandling.NextWithExistingCount
import static com.agileorbit.schwartz.builder.MisfireHandling.NextWithRemainingCount
import static com.agileorbit.schwartz.builder.MisfireHandling.NowWithExistingCount
import static com.agileorbit.schwartz.builder.MisfireHandling.NowWithRemainingCount
import static com.agileorbit.schwartz.builder.RepeatMode.Hours
import static com.agileorbit.schwartz.builder.RepeatMode.Minutes
import static com.agileorbit.schwartz.builder.RepeatMode.Seconds
import static org.quartz.DateBuilder.dateOf
import static org.quartz.DateBuilder.futureDate
import static org.quartz.DateBuilder.nextGivenSecondDate
import static org.quartz.SimpleScheduleBuilder.repeatHourlyForTotalCount
import static org.quartz.SimpleScheduleBuilder.repeatHourlyForever
import static org.quartz.SimpleScheduleBuilder.repeatMinutelyForTotalCount
import static org.quartz.SimpleScheduleBuilder.repeatMinutelyForever
import static org.quartz.SimpleScheduleBuilder.repeatSecondlyForTotalCount
import static org.quartz.SimpleScheduleBuilder.repeatSecondlyForever
import static org.quartz.SimpleScheduleBuilder.simpleSchedule
import static org.quartz.SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT
import static org.quartz.TriggerBuilder.newTrigger

class SimpleTriggerBuilderSpec extends AbstractBuilderSpec {

	private static final int intervalInHours = 2
	private static final int intervalInMinutes = 3
	private static final int intervalInSeconds = 2
	private static final int intervalInMillis = 2
	private static final int repeatCount = 42
	private static final int totalCount = 24
	private static final int hours = 33
	private static final int minutes = 44
	private static final int seconds = 55

	void 'from Quartz example2'() {
		given:
		Date startTime = nextGivenSecondDate(null, 15)
		int count = 1
		String triggerName

		when:
		triggerName = 'trigger_' + count++
		usingQuartz = newTrigger().withIdentity(triggerName)
				.forJob(schwartzJob.jobName, schwartzJob.jobGroup)
				.startAt(startTime)
				.build()

		usingPluginBuilder = builder(triggerName)
				.job(schwartzJob)
				.startAt(startTime)
				.repeatCount(0)
				.build()

		then:
		assertEqualSimple()

		when:
		triggerName = 'trigger_' + count++
		usingQuartz = newTrigger().withIdentity(triggerName)
				.forJob(schwartzJob.jobName, schwartzJob.jobGroup)
				.startAt(startTime)
				.withSchedule(simpleSchedule().withIntervalInSeconds(10).withRepeatCount(10))
				.build()

		usingPluginBuilder = builder(triggerName)
				.job(schwartzJob)
				.startAt(startTime)
				.intervalInSeconds(10)
				.repeatCount(10)
				.build()

		then:
		assertEqualSimple()

		when:
		Date inFiveMinutes = futureDate(5, IntervalUnit.MINUTE)
		triggerName = 'trigger_' + count++
		usingQuartz = newTrigger().withIdentity(triggerName)
				.forJob(schwartzJob.jobName, schwartzJob.jobGroup)
				.startAt(inFiveMinutes)
				.build()

		usingPluginBuilder = builder(triggerName)
				.job(schwartzJob)
				.startAt(inFiveMinutes)
				.repeatCount(0)
				.build()

		then:
		assertEqualSimple()

		when:
		triggerName = 'trigger_' + count++
		usingQuartz = newTrigger().withIdentity(triggerName)
				.forJob(schwartzJob.jobName, schwartzJob.jobGroup)
				.startAt(startTime)
				.withSchedule(simpleSchedule().withIntervalInSeconds(40).repeatForever())
				.build()

		usingPluginBuilder = builder(triggerName)
				.job(schwartzJob)
				.startAt(startTime)
				.intervalInSeconds(40)
				.build()

		then:
		assertEqualSimple()

		when:
		triggerName = 'trigger_' + count++
		usingQuartz = newTrigger().withIdentity(triggerName)
				.forJob(schwartzJob.jobName, schwartzJob.jobGroup)
				.startAt(startTime)
				.withSchedule(simpleSchedule().withIntervalInMinutes(5).withRepeatCount(20))
				.build()

		usingPluginBuilder = builder(triggerName)
				.job(schwartzJob)
				.startAt(startTime)
				.intervalInMinutes(5)
				.repeatCount(20)
				.build()

		then:
		assertEqualSimple()
	}

	void 'from Quartz example5'() {
		given:
		Date startTime = nextGivenSecondDate(null, 15)

		when:
		builder
				.withIdentity('trigger1')
				.forJob(schwartzJob.jobName, schwartzJob.jobGroup)
				.startAt(startTime)
				.withSchedule(simpleSchedule().withIntervalInSeconds(3).repeatForever()
				.withMisfireHandlingInstructionNowWithExistingCount())

		factory
				.name('trigger1')
				.job(schwartzJob)
				.startAt(startTime)
				.intervalInSeconds(3)
				.misfireHandling(NowWithExistingCount)

		build()

		then:
		assertEqualSimple()
	}

	void 'from Quartz example8'() {
		given:
		Date runDate = dateOf(0, 0, 10, 31, 10)
		String calendarName = 'holidays'

		when:
		builder
				.withIdentity('trigger1')
				.forJob(schwartzJob.jobName, schwartzJob.jobGroup)
				.startAt(runDate)
				.withSchedule(simpleSchedule().withIntervalInHours(1).repeatForever())
				.modifiedByCalendar(calendarName)

		factory
				.name('trigger1')
				.job(schwartzJob)
				.startAt(runDate)
				.intervalInHours(1)
				.calendarName(calendarName)

		build()

		then:
		assertEqualSimple()
	}

	void 'from Quartz example13'() {
		given:
		int count = 1
		String triggerName
		Date startTime = futureDate(1, IntervalUnit.SECOND)

		when:
		triggerName = 'trigger_' + count++
		usingQuartz = newTrigger().withIdentity(triggerName)
				.forJob(schwartzJob.jobName, schwartzJob.jobGroup)
				.startAt(startTime)
				.withSchedule(simpleSchedule().withRepeatCount(20).withIntervalInSeconds(5))
				.build()

		usingPluginBuilder = builder(triggerName)
				.job(schwartzJob)
				.startAt(startTime)
				.repeatCount(20)
				.intervalInSeconds(5)
				.build()

		then:
		assertEqualSimple()

		when:
		triggerName = 'trigger_' + count++
		usingQuartz = newTrigger().withIdentity(triggerName)
				.forJob(schwartzJob.jobName, schwartzJob.jobGroup)
				.startAt(startTime)
				.withSchedule(simpleSchedule().withRepeatCount(20).withIntervalInMilliseconds(4500L))
				.build()

		usingPluginBuilder = builder(triggerName)
				.job(schwartzJob)
				.startAt(startTime)
				.repeatCount(20)
				.intervalInMillis(4500)
				.build()

		then:
		assertEqualSimple()
	}

	void 'from Quartz example14'() {
		given:
		Date startTime = futureDate(5, IntervalUnit.SECOND)

		when:
		usingQuartz = newTrigger().withIdentity('Priority1Trigger5SecondRepeat')
				.startAt(startTime)
				.withSchedule(simpleSchedule().withRepeatCount(1).withIntervalInSeconds(5))
				.withPriority(1).forJob(jobDetail).build()

		usingPluginBuilder = builder('Priority1Trigger5SecondRepeat')
				.startAt(startTime)
				.repeatCount(1)
				.intervalInSeconds(5)
				.priority(1)
				.jobDetail(jobDetail)
				.build()

		then:
		assertEqualSimple()

		when:
		usingQuartz = newTrigger().withIdentity('Priority10Trigger15SecondRepeat')
				.startAt(startTime)
				.withSchedule(simpleSchedule().withRepeatCount(1).withIntervalInSeconds(15))
				.withPriority(10).forJob(jobDetail)
				.build()

		usingPluginBuilder = builder('Priority10Trigger15SecondRepeat')
				.startAt(startTime)
				.repeatCount(1)
				.intervalInSeconds(15)
				.priority(10)
				.jobDetail(jobDetail)
				.build()

		then:
		assertEqualSimple()
	}

	void 'from SimpleTriggerImplSerializationTest'() {
		when:
		Date start = new Date(0)
		Date end = new Date(10000)
		usingQuartz = new SimpleTriggerImpl('triggerName', 'triggerGroup', 'jobName', 'jobGroup',
				start, end, 4, 100L)
		((MutableTrigger)usingQuartz).setDescription('A Trigger')
		((MutableTrigger)usingQuartz).setJobDataMap(new JobDataMap(foo: 'bar'))
		((MutableTrigger)usingQuartz).setCalendarName('calendarName')
		((MutableTrigger)usingQuartz).setMisfireInstruction(MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT)
		((MutableTrigger)usingQuartz).setPriority(3)

		usingPluginBuilder = builder('triggerName', 'triggerGroup')
				.jobName('jobName').jobGroup('jobGroup')
				.startAt(start).endAt(end)
				.repeatCount(4)
				.intervalInMillis(100)
				.description('A Trigger')
				.jobData(foo: 'bar')
				.calendarName('calendarName')
				.misfireHandling(NextWithRemainingCount)
				.priority(3)
				.build()

		then:
		assertEqualSimple()
	}

	void 'repeatHourlyForever, withMisfireHandlingInstructionFireNow'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(repeatHourlyForever()
					.withMisfireHandlingInstructionFireNow())

		factory
				.key(triggerKey)
				.startAt(startDate)
				.repeatMode(Hours)
				.misfireHandling(FireNow)

		build()

		then:
		assertEqualSimple()
	}

	void 'repeatHourlyForever(hours), withMisfireHandlingInstructionIgnoreMisfires'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(repeatHourlyForever(hours)
					.withMisfireHandlingInstructionIgnoreMisfires())

		factory
				.key(triggerKey)
				.startAt(startDate)
				.repeatMode(Hours).hours(hours)
				.misfireHandling(IgnoreMisfires)

		build()

		then:
		assertEqualSimple()
	}

	void 'repeatMinutelyForever, withMisfireHandlingInstructionNextWithExistingCount'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(repeatMinutelyForever()
					.withMisfireHandlingInstructionNextWithExistingCount())

		factory
				.key(triggerKey)
				.startAt(startDate)
				.repeatMode(Minutes)
				.misfireHandling(NextWithExistingCount)

		build()

		then:
		assertEqualSimple()
	}

	void 'repeatMinutelyForever(minutes), withMisfireHandlingInstructionNextWithRemainingCount'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(repeatMinutelyForever(minutes)
					.withMisfireHandlingInstructionNextWithRemainingCount())

		factory
				.key(triggerKey)
				.startAt(startDate)
				.repeatMode(Minutes).minutes(minutes)
				.misfireHandling(NextWithRemainingCount)

		build()

		then:
		assertEqualSimple()
	}

	void 'repeatSecondlyForever, withMisfireHandlingInstructionNowWithExistingCount'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(repeatSecondlyForever()
					.withMisfireHandlingInstructionNowWithExistingCount())

		factory
				.key(triggerKey)
				.startAt(startDate)
				.repeatMode(Seconds)
				.misfireHandling(NowWithExistingCount)

		build()

		then:
		assertEqualSimple()
	}

	void 'repeatSecondlyForever(seconds), withMisfireHandlingInstructionNowWithRemainingCount'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(repeatSecondlyForever(seconds)
					.withMisfireHandlingInstructionNowWithRemainingCount())

		factory
				.key(triggerKey)
				.startAt(startDate)
				.repeatMode(Seconds).seconds(seconds)
				.misfireHandling(NowWithRemainingCount)

		build()

		then:
		assertEqualSimple()
	}

	void 'repeatHourlyForTotalCount(totalCount)'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(repeatHourlyForTotalCount(totalCount))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.repeatMode(Hours).totalCount(totalCount)

		build()

		then:
		assertEqualSimple()
	}

	void 'repeatHourlyForTotalCount(totalCount, hours)'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(repeatHourlyForTotalCount(totalCount, hours))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.repeatMode(Hours).totalCount(totalCount).hours(hours)

		build()

		then:
		assertEqualSimple()
	}

	void 'repeatMinutelyForTotalCount(totalCount)'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(repeatMinutelyForTotalCount(totalCount))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.repeatMode(Minutes).totalCount(totalCount)

		build()

		then:
		assertEqualSimple()
	}

	void 'repeatMinutelyForTotalCount(totalCount, minutes)'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(repeatMinutelyForTotalCount(totalCount, minutes))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.repeatMode(Minutes).totalCount(totalCount).minutes(minutes)

		build()

		then:
		assertEqualSimple()
	}

	void 'repeatSecondlyForTotalCount(totalCount)'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(repeatSecondlyForTotalCount(totalCount))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.repeatMode(Seconds).totalCount(totalCount)


		build()

		then:
		assertEqualSimple()
	}

	void 'repeatSecondlyForTotalCount(totalCount, seconds)'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(repeatSecondlyForTotalCount(totalCount, seconds))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.repeatMode(Seconds).totalCount(totalCount).seconds(seconds)

		build()

		then:
		assertEqualSimple()
	}

	void 'withIntervalInMilliseconds(intervalInMillis), withRepeatCount(repeatCount)'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(simpleSchedule()
					.withIntervalInMilliseconds(intervalInMillis)
					.withRepeatCount(repeatCount))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.intervalInMillis(intervalInMillis)
				.repeatCount(repeatCount)

		build()

		then:
		assertEqualSimple()
	}

	void 'withIntervalInSeconds(intervalInSeconds), withRepeatCount(repeatCount)'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(simpleSchedule()
					.withIntervalInSeconds(intervalInSeconds)
					.withRepeatCount(repeatCount))

		factory
				.key(triggerKey)
				.startAt(startDate)
				.intervalInSeconds(intervalInSeconds)
				.repeatCount(repeatCount)

		build()

		then:
		assertEqualSimple()
	}

	void 'withIntervalInMinutes(intervalInMinutes), repeatForever()'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(simpleSchedule()
					.withIntervalInMinutes(intervalInMinutes)
					.repeatForever())

		factory
				.key(triggerKey)
				.startAt(startDate)
				.intervalInMinutes(intervalInMinutes)

		build()

		then:
		assertEqualSimple()
	}

	void 'withIntervalInMilliseconds(intervalInMillis), repeatForever()'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)
				.withSchedule(simpleSchedule()
					.withIntervalInHours(intervalInHours)
					.repeatForever())

		factory
				.key(triggerKey)
				.startAt(startDate)
				.intervalInHours(intervalInHours)

		build()

		then:
		assertEqualSimple()
	}

	void 'one-off, no repeat'() {
		when:
		builder
				.withIdentity(triggerKey)
				.startAt(startDate)

		factory
				.key(triggerKey)
				.startAt(startDate)
				.noRepeat()

		build()

		then:
		assertEqualSimple()
	}
}
