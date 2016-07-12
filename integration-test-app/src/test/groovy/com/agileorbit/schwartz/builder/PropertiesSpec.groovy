package com.agileorbit.schwartz.builder

import com.agileorbit.schwartz.util.Utils
import spock.lang.Specification

abstract class PropertiesSpec extends Specification {

	protected static final List<String> allNames = [
			'calendarName', 'cronExpression', 'cronSchedule', 'cronScheduleNonvalidated', 'dailyEnd', 'dailyEndAfterCount',
			'dailyStart', 'day', 'days', 'description', 'endAt', 'everyDay', 'group', 'hour', 'hourAndMinuteMode', 'hours',
			'interval', 'intervalInDays', 'intervalInHours', 'intervalInMillis', 'intervalInMinutes', 'intervalInMonths',
			'intervalInSeconds', 'intervalInWeeks', 'intervalInYears', 'job', 'jobData', 'jobDataMap', 'jobDetail',
			'jobGroup', 'jobKey', 'jobName', 'key', 'minute', 'minutes', 'misfireHandling', 'mondayThroughFriday', 'name',
			'preserveHour', 'priority', 'repeatCount', 'repeatForever', 'repeatMode', 'saturdayAndSunday', 'seconds',
			'skipDay', 'startAt', 'timeZone', 'totalCount', 'unit']

	protected BuilderFactory factory = new BuilderFactory()
	protected CommonProperties commonProperties = factory.commonProperties
	protected CalendarProperties calendarProperties = factory.calendarProperties
	protected CronProperties cronProperties = factory.cronProperties
	protected DailyProperties dailyProperties = factory.dailyProperties
	protected SimpleProperties simpleProperties = factory.simpleProperties
	protected TriggerProperties triggerProperties = factory.triggerProperties

	protected List<String> namesFromProperties(props) {
		def allNames = []
		allNames.addAll props.properties.keySet()
		allNames.remove 'class'
		allNames.sort()
	}

	protected void reset() {
		for (name in allNames) {
			factory[name] = null
		}

		assert !Utils.values(factory)
		assert !Utils.values(commonProperties)
		assert !Utils.values(calendarProperties)
		assert !Utils.values(cronProperties)
		assert !Utils.values(dailyProperties)
		assert !Utils.values(simpleProperties)
		assert !Utils.values(triggerProperties)
	}

	protected void assertNegligibleTimeDifference(Date date, long delta) {
		assert Math.abs(date.time - delta - System.currentTimeMillis()) < 30
		true
	}
}
