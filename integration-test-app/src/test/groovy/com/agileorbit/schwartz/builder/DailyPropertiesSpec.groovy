package com.agileorbit.schwartz.builder

import org.quartz.TimeOfDay

class DailyPropertiesSpec extends PropertiesSpec {

	void testPropertyNames() {
		expect:
		dailyProperties.propertyNames() == ['dailyEnd', 'dailyEndAfterCount', 'dailyStart', 'everyDay',
		                                    'mondayThroughFriday', 'saturdayAndSunday']

		dailyProperties.propertyNames() == namesFromProperties(dailyProperties)

		dailyProperties.allNames() == ['dailyEnd', 'dailyEndAfterCount', 'dailyStart', 'days', 'everyDay', 'interval',
		                               'intervalInHours', 'intervalInMinutes', 'intervalInSeconds', 'misfireHandling',
		                               'mondayThroughFriday', 'repeatCount', 'saturdayAndSunday', 'unit']
	}

	void 'modifiedDayProperties'() {
		given:
		def data = [days: [1,2,4], everyDay: true, mondayThroughFriday: true, saturdayAndSunday: true]

		when:
		reset()
		for (name in data.keySet().take(count)) {
			factory[name] = data[name]
		}

		then:
		count == dailyProperties.modifiedDaysProperties().size()

		cleanup:
		reset()

		where:
		count << (0..4)
	}

	void 'modifiedIntervalProperties'() {
		given:
		def data = [intervalInSeconds: 11, intervalInMinutes: 22, intervalInHours: 33]

		when:
		reset()
		for (name in data.keySet().take(count)) {
			factory[name] = data[name]
		}

		then:
		count == dailyProperties.modifiedIntervalProperties().size()

		cleanup:
		reset()

		where:
		count << (0..3)
	}

	void 'verify set/get for properties'() {
		given:
		TimeOfDay dailyEnd = new TimeOfDay(1, 2, 3)
		int dailyEndAfterCount = 13
		TimeOfDay dailyStart = new TimeOfDay(2, 3, 4)

		when:
		reset()
		dailyProperties.dailyEnd = dailyEnd

		then:
		dailyProperties.dailyEnd == dailyEnd
		factory.dailyEnd == dailyEnd

		when:
		reset()
		dailyProperties.dailyEnd dailyEnd

		then:
		dailyProperties.dailyEnd == dailyEnd
		factory.dailyEnd == dailyEnd

		when:
		reset()
		dailyProperties.dailyEndAfterCount = dailyEndAfterCount

		then:
		dailyProperties.dailyEndAfterCount == dailyEndAfterCount
		factory.dailyEndAfterCount == dailyEndAfterCount

		when:
		reset()
		dailyProperties.dailyEndAfterCount dailyEndAfterCount

		then:
		dailyProperties.dailyEndAfterCount == dailyEndAfterCount
		factory.dailyEndAfterCount == dailyEndAfterCount

		when:
		reset()
		dailyProperties.dailyStart = dailyStart

		then:
		dailyProperties.dailyStart == dailyStart
		factory.dailyStart == dailyStart

		when:
		reset()
		dailyProperties.dailyStart dailyStart

		then:
		dailyProperties.dailyStart == dailyStart
		factory.dailyStart == dailyStart

		when:
		reset()
		dailyProperties.everyDay = true

		then:
		dailyProperties.everyDay
		factory.everyDay

		when:
		reset()
		dailyProperties.everyDay()

		then:
		dailyProperties.everyDay
		factory.everyDay

		when:
		reset()
		dailyProperties.mondayThroughFriday = true

		then:
		dailyProperties.mondayThroughFriday
		factory.mondayThroughFriday

		when:
		reset()
		dailyProperties.mondayThroughFriday()

		then:
		dailyProperties.mondayThroughFriday
		factory.mondayThroughFriday

		when:
		reset()
		dailyProperties.saturdayAndSunday = true

		then:
		dailyProperties.saturdayAndSunday
		factory.saturdayAndSunday

		when:
		reset()
		dailyProperties.saturdayAndSunday()

		then:
		dailyProperties.saturdayAndSunday
		factory.saturdayAndSunday

		cleanup:
		reset()
	}
}
