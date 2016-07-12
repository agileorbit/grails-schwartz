package com.agileorbit.schwartz.builder

import com.agileorbit.schwartz.SimpleSchwartzJob
import groovy.transform.CompileStatic
import org.quartz.Job
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.JobExecutionContext
import org.quartz.JobKey
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.quartz.TriggerKey
import spock.lang.Specification

import static org.junit.Assert.fail
import static org.quartz.DateBuilder.IntervalUnit.DAY
import static org.quartz.DateBuilder.futureDate
import static org.quartz.JobBuilder.newJob
import static org.quartz.JobKey.jobKey
import static org.quartz.Trigger.DEFAULT_PRIORITY
import static org.quartz.TriggerBuilder.newTrigger
import static org.quartz.TriggerKey.triggerKey

abstract class AbstractBuilderSpec extends Specification {

	protected static final Collection<String> COMMON_TRIGGER_NAMES = [
			'calendarName', 'description', 'endTime', 'jobDataMap', 'jobKey', 'key',
			'misfireInstruction', 'priority', 'startTime']

	protected static final Collection<String> CALENDAR_INTERVAL_TRIGGER_NAMES = [
			'preserveHourOfDayAcrossDaylightSavings', 'repeatInterval', 'repeatIntervalUnit',
			'skipDayIfHourDoesNotExist', 'timeZone'] + COMMON_TRIGGER_NAMES

	protected static final Collection<String> CRON_TRIGGER_NAMES =
			['cronExpression', 'timeZone'] + COMMON_TRIGGER_NAMES

	protected static final Collection<String> DAILY_TIME_INTERVAL_TRIGGER_NAMES = [
			'daysOfWeek', 'endTimeOfDay', 'repeatCount', 'repeatInterval',
			'repeatIntervalUnit', 'startTimeOfDay'] + COMMON_TRIGGER_NAMES

	protected static final Collection<String> SIMPLE_TRIGGER_NAMES =
			['repeatCount', 'repeatInterval'] + COMMON_TRIGGER_NAMES

	protected static final SimpleSchwartzJob schwartzJob = new SimpleSchwartzJob()

	protected static final String calendarName = '_calendarName_'
	protected static final String description = '_description_'
	protected static final Date endTime = futureDate(12, DAY)
	protected static final Map jobData = [a: 1, b: 2].asImmutable()
	protected static final JobDataMap jobDataMap = new JobDataMap(jobData)
	protected static final JobDetail jobDetail = newJob(SimpleJob).withIdentity(schwartzJob.jobName).build()
	protected static final JobKey jobKey = jobKey('_jobname_', '_jobgroup_')
	protected static final int priority = DEFAULT_PRIORITY + 2
	protected static final Date startDate = futureDate(10, DAY)
	protected static final TimeZone timeZone = TimeZone.getTimeZone('America/New_York')
	protected static final TriggerKey triggerKey = triggerKey('_triggername_', '_triggergroup_')

	protected TriggerBuilder builder = newTrigger()
	protected Trigger usingQuartz

	protected BuilderFactory factory = new BuilderFactory()
	protected Trigger usingPluginBuilder

	protected void build() {
		usingQuartz = builder.build()
		usingPluginBuilder = factory.build()
	}

	protected boolean assertSameTypeAndEqualValues(Collection<String> names) {
		assert usingPluginBuilder.getClass() == usingQuartz.getClass()
		assertEqualValues names
	}

	protected boolean assertEqualCalendar() {
		assertSameTypeAndEqualValues CALENDAR_INTERVAL_TRIGGER_NAMES
	}

	protected boolean assertEqualCron() {
		assertSameTypeAndEqualValues CRON_TRIGGER_NAMES
	}

	protected boolean assertEqualDaily() {
		assertSameTypeAndEqualValues DAILY_TIME_INTERVAL_TRIGGER_NAMES
	}

	protected boolean assertEqualSimple(boolean allowNegligibleStartDateDifference = false) {
		if (allowNegligibleStartDateDifference) {
			Collection<String> names = (Collection<String>)SIMPLE_TRIGGER_NAMES.clone()
			names.remove 'startTime'
			assertSameTypeAndEqualValues names
			assert Math.abs(usingQuartz.startTime.time - usingPluginBuilder.startTime.time) < 50
			true
		}
		else {
			assertSameTypeAndEqualValues SIMPLE_TRIGGER_NAMES
		}
	}

	protected boolean assertEqualValues(Collection<String> names) {
		Set<String> fails = []
		Set<String> unexpectedNulls = []
		Set<String> unexpectedNonNulls = []

		Map<String, ?> triggerDataQuartz = triggerValues(usingQuartz, names)
		Map<String, ?> triggerDataPlugin = triggerValues(usingPluginBuilder, names)

		for (name in names) {
			def fromQuartzValue = triggerDataQuartz[name]
			def fromPluginValue = triggerDataPlugin[name]
			if (name == 'jobDataMap') {
				fromQuartzValue = ((JobDataMap) fromQuartzValue)?.getWrappedMap()
				fromPluginValue = ((JobDataMap) fromPluginValue)?.getWrappedMap()
			}
			if (fromQuartzValue == null && fromPluginValue == null) {
				continue
			}

			if (fromQuartzValue != null && fromPluginValue != null) {
				if (!fromQuartzValue.is(fromPluginValue) && fromQuartzValue != fromPluginValue) {
					fails << name
				}
			}
			else {
				if (fromQuartzValue == null && fromPluginValue != null) unexpectedNonNulls << name
				else unexpectedNulls << name // fromPluginValue == null
			}
		}

		def errors = []

		if (fails) {
			errors << 'Invalid value(s) in the trigger:\n\n\t' + subMapString(triggerDataPlugin, fails) +
					'\n\nexpected:\n\n\t' + subMapString(triggerDataQuartz, fails) + '\n'
		}

		if (unexpectedNulls) errors << 'Invalid null trigger value(s): ' + unexpectedNulls.join(', ')
		if (unexpectedNonNulls) errors << 'Invalid non-null trigger value(s): ' + unexpectedNonNulls.join(', ')

		if (errors) {
			fail errors.join('\n')
		}

		true
	}

	protected static Map<String, ?> triggerValues(trigger, Collection<String> names) {
		def data = [:]
		for (name in names) {
			data[name] = trigger[name]
		}
		data
	}

	private static String subMapString(Map map, Collection<String> names) {
		map.subMap(names).entrySet().join('\n\t')
	}
}

@CompileStatic
class SimpleJob implements Job {
	void execute(JobExecutionContext context) {}
}
