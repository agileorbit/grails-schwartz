package com.agileorbit.schwartz.builder

class CalendarPropertiesSpec extends PropertiesSpec {

	void testPropertyNames() {
		expect:
		calendarProperties.propertyNames() == ['intervalInDays', 'intervalInMonths', 'intervalInWeeks',
		                                       'intervalInYears', 'preserveHour', 'skipDay']

		calendarProperties.propertyNames() == namesFromProperties(calendarProperties)

		calendarProperties.allNames() == ['interval', 'intervalInDays', 'intervalInHours', 'intervalInMinutes',
		                                  'intervalInMonths', 'intervalInSeconds', 'intervalInWeeks',
		                                  'intervalInYears', 'misfireHandling', 'preserveHour', 'skipDay',
		                                  'timeZone', 'unit']
	}

	void 'modifiedIntervalProperties'() {
		given:
		def data = [intervalInSeconds: 11, intervalInMinutes: 22, intervalInHours: 33, intervalInDays: 44,
		            intervalInWeeks  : 55, intervalInMonths: 66, intervalInYears: 77]

		when:
		reset()
		for (name in data.keySet().take(count)) {
			factory[name] = data[name]
		}

		then:
		count == calendarProperties.modifiedIntervalProperties().size()

		cleanup:
		reset()

		where:
		count << (0..7)
	}

	void 'verify set/get for properties'() {
		given:
		int intervalInDays = 1
		int intervalInMonths = 2
		int intervalInWeeks = 3
		int intervalInYears = 4

		when:
		reset()
		calendarProperties.intervalInDays = intervalInDays

		then:
		calendarProperties.intervalInDays == intervalInDays
		factory.intervalInDays == intervalInDays

		when:
		reset()
		calendarProperties.intervalInDays intervalInDays

		then:
		calendarProperties.intervalInDays == intervalInDays
		factory.intervalInDays == intervalInDays

		when:
		reset()
		calendarProperties.intervalInMonths = intervalInMonths

		then:
		calendarProperties.intervalInMonths == intervalInMonths
		factory.intervalInMonths == intervalInMonths

		when:
		reset()
		calendarProperties.intervalInMonths intervalInMonths

		then:
		calendarProperties.intervalInMonths == intervalInMonths
		factory.intervalInMonths == intervalInMonths

		when:
		reset()
		calendarProperties.intervalInWeeks = intervalInWeeks

		then:
		calendarProperties.intervalInWeeks == intervalInWeeks
		factory.intervalInWeeks == intervalInWeeks

		when:
		reset()
		calendarProperties.intervalInWeeks intervalInWeeks

		then:
		calendarProperties.intervalInWeeks == intervalInWeeks
		factory.intervalInWeeks == intervalInWeeks

		when:
		reset()
		calendarProperties.intervalInYears = intervalInYears

		then:
		calendarProperties.intervalInYears == intervalInYears
		factory.intervalInYears == intervalInYears

		when:
		reset()
		calendarProperties.intervalInYears intervalInYears

		then:
		calendarProperties.intervalInYears == intervalInYears
		factory.intervalInYears == intervalInYears

		when:
		reset()
		calendarProperties.preserveHour = true

		then:
		calendarProperties.preserveHour
		factory.preserveHour

		when:
		reset()
		calendarProperties.preserveHour()

		then:
		calendarProperties.preserveHour
		factory.preserveHour

		when:
		reset()
		calendarProperties.skipDay = true

		then:
		calendarProperties.skipDay
		factory.skipDay

		when:
		reset()
		calendarProperties.skipDay()

		then:
		calendarProperties.skipDay
		factory.skipDay
	}
}
