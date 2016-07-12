package com.agileorbit.schwartz.builder

import org.quartz.CronExpression

import static com.agileorbit.schwartz.builder.HourAndMinuteMode.Monthly

class CronPropertiesSpec extends PropertiesSpec {

	void testPropertyNames() {
		expect:
		cronProperties.propertyNames() == ['cronExpression', 'cronSchedule', 'cronScheduleNonvalidated',
		                                   'day', 'hour', 'hourAndMinuteMode', 'minute']

		cronProperties.propertyNames() == namesFromProperties(cronProperties)

		cronProperties.allNames() == ['cronExpression', 'cronSchedule', 'cronScheduleNonvalidated', 'day', 'days',
		                              'hour', 'hourAndMinuteMode', 'minute', 'misfireHandling', 'timeZone']
	}

	void 'modifiedDayProperties'() {
		given:
		def data = [day: 1, days: 2, hour: 3, hourAndMinuteMode: Monthly, minute: 4]

		when:
		reset()
		for (name in data.keySet().take(count)) {
			factory[name] = data[name]
		}

		then:
		count == cronProperties.modifiedDayProperties().size()

		cleanup:
		reset()

		where:
		count << (0..5)
	}

	void 'modifiedExpressionProperties'() {
		given:
		def data = [cronExpression: new CronExpression('0 10,44 14 ? 3 WED'),
		            cronSchedule: 'abc',
		            cronScheduleNonvalidated: 'def']

		when:
		reset()
		for (name in data.keySet().take(count)) {
			factory[name] = data[name]
		}

		then:
		count == cronProperties.modifiedExpressionProperties().size()

		cleanup:
		reset()

		where:
		count << (0..3)
	}

	void 'verify set/get for properties'() {
		given:
		CronExpression cronExpression = new CronExpression('0 15 10 ? * *')
		String cronSchedule = '0 10,44 14 ? 3 WED'
		String cronScheduleNonvalidated = '0 0 12 1/5 * ?'
		int day = 10
		int hour = 11
		HourAndMinuteMode hourAndMinuteMode = Monthly
		int minute = 12

		when:
		reset()
		cronProperties.cronExpression = cronExpression

		then:
		cronProperties.cronExpression.is cronExpression
		factory.cronExpression.is cronExpression

		when:
		reset()
		cronProperties.cronExpression cronExpression

		then:
		cronProperties.cronExpression.is cronExpression
		factory.cronExpression.is cronExpression

		when:
		reset()
		cronProperties.cronSchedule = cronSchedule

		then:
		cronProperties.cronSchedule == cronSchedule
		factory.cronSchedule == cronSchedule

		when:
		reset()
		cronProperties.cronSchedule cronSchedule

		then:
		cronProperties.cronSchedule == cronSchedule
		factory.cronSchedule == cronSchedule

		when:
		reset()
		cronProperties.cronScheduleNonvalidated = cronScheduleNonvalidated

		then:
		cronProperties.cronScheduleNonvalidated == cronScheduleNonvalidated
		factory.cronScheduleNonvalidated == cronScheduleNonvalidated

		when:
		reset()
		cronProperties.cronScheduleNonvalidated cronScheduleNonvalidated

		then:
		cronProperties.cronScheduleNonvalidated == cronScheduleNonvalidated
		factory.cronScheduleNonvalidated == cronScheduleNonvalidated

		when:
		reset()
		cronProperties.day = day

		then:
		cronProperties.day == day
		factory.day == day

		when:
		reset()
		cronProperties.day day

		then:
		cronProperties.day == day
		factory.day == day

		when:
		reset()
		cronProperties.hour = hour

		then:
		cronProperties.hour == hour
		factory.hour == hour

		when:
		reset()
		cronProperties.hour hour

		then:
		cronProperties.hour == hour
		factory.hour == hour

		when:
		reset()
		cronProperties.hourAndMinuteMode = hourAndMinuteMode

		then:
		cronProperties.hourAndMinuteMode == hourAndMinuteMode
		factory.hourAndMinuteMode == hourAndMinuteMode

		when:
		reset()
		cronProperties.hourAndMinuteMode hourAndMinuteMode

		then:
		cronProperties.hourAndMinuteMode == hourAndMinuteMode
		factory.hourAndMinuteMode == hourAndMinuteMode

		when:
		reset()
		cronProperties.minute = minute

		then:
		cronProperties.minute == minute
		factory.minute == minute

		when:
		reset()
		cronProperties.minute minute

		then:
		cronProperties.minute == minute
		factory.minute == minute
	}
}
