package com.agileorbit.schwartz.builder

class SimplePropertiesSpec extends PropertiesSpec {

	void testPropertyNames() {
		expect:
		simpleProperties.propertyNames() == ['hours', 'intervalInMillis', 'minutes', 'repeatForever',
		                                     'repeatMode', 'seconds', 'totalCount']

		simpleProperties.propertyNames() == namesFromProperties(simpleProperties)

		simpleProperties.allNames() == ['hours', 'intervalInHours', 'intervalInMillis', 'intervalInMinutes',
		                                'intervalInSeconds', 'minutes', 'misfireHandling', 'repeatCount',
		                                'repeatForever', 'repeatMode', 'seconds', 'totalCount']
	}

	void 'modifiedIntervalProperties'() {
		given:
		def data = [intervalInSeconds: 11, intervalInMinutes: 22, intervalInHours: 33, intervalInMillis: 44]

		when:
		reset()
		for (name in data.keySet().take(count)) {
			factory[name] = data[name]
		}

		then:
		count == simpleProperties.modifiedIntervalProperties().size()

		cleanup:
		reset()

		where:
		count << (0..4)
	}

	void 'modifiedRepeatProperties'() {
		given:
		def data = [hours: 3, minutes: 2, seconds: 1]

		when:
		reset()
		for (name in data.keySet().take(count)) {
			factory[name] = data[name]
		}

		then:
		count == simpleProperties.modifiedRepeatProperties().size()

		cleanup:
		reset()

		where:
		count << (0..3)
	}

	void 'verify set/get for properties'() {
		given:
		int hours = 14
		int minutes = 15
		int seconds = 16
		RepeatMode repeatMode
		int totalCount = 17
		long intervalInMillis = 18

		when:
		reset()
		simpleProperties.hours = hours

		then:
		simpleProperties.hours == hours
		factory.hours == hours

		when:
		reset()
		simpleProperties.hours hours

		then:
		simpleProperties.hours == hours
		factory.hours == hours

		when:
		reset()
		simpleProperties.minutes = minutes

		then:
		simpleProperties.minutes == minutes
		factory.minutes == minutes

		when:
		reset()
		simpleProperties.minutes minutes

		then:
		simpleProperties.minutes == minutes
		factory.minutes == minutes

		when:
		reset()
		simpleProperties.seconds = seconds

		then:
		simpleProperties.seconds == seconds
		factory.seconds == seconds

		when:
		reset()
		simpleProperties.seconds seconds

		then:
		simpleProperties.seconds == seconds
		factory.seconds == seconds

		when:
		reset()
		simpleProperties.repeatForever = true

		then:
		simpleProperties.repeatForever
		factory.repeatForever

		when:
		reset()
		simpleProperties.repeatForever()

		then:
		simpleProperties.repeatForever
		factory.repeatForever

		when:
		reset()
		simpleProperties.repeatMode = repeatMode

		then:
		simpleProperties.repeatMode == repeatMode
		factory.repeatMode == repeatMode

		when:
		reset()
		simpleProperties.repeatMode repeatMode

		then:
		simpleProperties.repeatMode == repeatMode
		factory.repeatMode == repeatMode

		when:
		reset()
		simpleProperties.totalCount = totalCount

		then:
		simpleProperties.totalCount == totalCount
		factory.totalCount == totalCount

		when:
		reset()
		simpleProperties.totalCount totalCount

		then:
		simpleProperties.totalCount == totalCount
		factory.totalCount == totalCount

		when:
		reset()
		simpleProperties.intervalInMillis = intervalInMillis

		then:
		simpleProperties.intervalInMillis == intervalInMillis
		factory.intervalInMillis == intervalInMillis

		when:
		reset()
		simpleProperties.intervalInMillis intervalInMillis

		then:
		simpleProperties.intervalInMillis == intervalInMillis
		factory.intervalInMillis == intervalInMillis
	}
}
