package com.agileorbit.schwartz.builder

import org.quartz.DateBuilder.IntervalUnit

import static com.agileorbit.schwartz.builder.MisfireHandling.FireAndProceed
import static org.quartz.DateBuilder.FRIDAY
import static org.quartz.DateBuilder.IntervalUnit.MONTH
import static org.quartz.DateBuilder.TUESDAY

class CommonPropertiesSpec extends PropertiesSpec {

	void testPropertyNames() {
		expect:
		commonProperties.propertyNames() == ['days', 'interval', 'intervalInHours', 'intervalInMinutes',
		                                     'intervalInSeconds', 'misfireHandling', 'repeatCount',
		                                     'timeZone', 'unit']

		commonProperties.propertyNames() == namesFromProperties(commonProperties)

		commonProperties.allNames() == commonProperties.propertyNames()
	}

	void 'utility methods'() {
		when:
		reset()
		commonProperties.noRepeat()

		then:
		commonProperties.repeatCount == 0
		factory.repeatCount == 0

		cleanup:
		reset()
	}

	void 'verify set/get for properties'() {
		given:
		int day = TUESDAY
		Integer[] dayArray = [day]
		Integer[] daysArray = [day, FRIDAY]
		List<Integer> daysList = [day, FRIDAY]
		int interval = 5
		int intervalInHours = 6
		int intervalInMinutes = 7
		int intervalInSeconds = 8
		MisfireHandling misfireHandling = FireAndProceed
		int repeatCount = 9
		TimeZone timeZone = TimeZone.getTimeZone('America/Honolulu')
		IntervalUnit unit = MONTH

		when:
		reset()
		commonProperties.days = day

		then:
		commonProperties.days == dayArray
		factory.days == dayArray

		when:
		reset()
		commonProperties.days day

		then:
		commonProperties.days == dayArray
		factory.days == dayArray

		when:
		reset()
		commonProperties.days = daysArray

		then:
		commonProperties.days == daysArray
		factory.days == daysArray

		when:
		reset()
		commonProperties.days daysArray

		then:
		commonProperties.days == daysArray
		factory.days == daysArray

		when:
		reset()
		commonProperties.days = daysList

		then:
		commonProperties.days == daysArray
		factory.days == daysArray

		when:
		reset()
		commonProperties.days daysList

		then:
		commonProperties.days == daysArray
		factory.days == daysArray

		when:
		reset()
		commonProperties.interval = interval

		then:
		commonProperties.interval == interval
		factory.interval == interval

		when:
		reset()
		commonProperties.interval interval

		then:
		commonProperties.interval == interval
		factory.interval == interval

		when:
		reset()
		commonProperties.intervalInHours = intervalInHours

		then:
		commonProperties.intervalInHours == intervalInHours
		factory.intervalInHours == intervalInHours

		when:
		reset()
		commonProperties.intervalInHours intervalInHours

		then:
		commonProperties.intervalInHours == intervalInHours
		factory.intervalInHours == intervalInHours

		when:
		reset()
		commonProperties.intervalInMinutes = intervalInMinutes

		then:
		commonProperties.intervalInMinutes == intervalInMinutes
		factory.intervalInMinutes == intervalInMinutes

		when:
		reset()
		commonProperties.intervalInMinutes intervalInMinutes

		then:
		commonProperties.intervalInMinutes == intervalInMinutes
		factory.intervalInMinutes == intervalInMinutes

		when:
		reset()
		commonProperties.intervalInSeconds = intervalInSeconds

		then:
		commonProperties.intervalInSeconds == intervalInSeconds
		factory.intervalInSeconds == intervalInSeconds

		when:
		reset()
		commonProperties.intervalInSeconds intervalInSeconds

		then:
		commonProperties.intervalInSeconds == intervalInSeconds
		factory.intervalInSeconds == intervalInSeconds

		when:
		reset()
		commonProperties.misfireHandling = misfireHandling

		then:
		commonProperties.misfireHandling == misfireHandling
		factory.misfireHandling == misfireHandling

		when:
		reset()
		commonProperties.misfireHandling misfireHandling

		then:
		commonProperties.misfireHandling == misfireHandling
		factory.misfireHandling == misfireHandling

		when:
		reset()
		commonProperties.repeatCount = repeatCount

		then:
		commonProperties.repeatCount == repeatCount
		factory.repeatCount == repeatCount

		when:
		reset()
		commonProperties.repeatCount repeatCount

		then:
		commonProperties.repeatCount == repeatCount
		factory.repeatCount == repeatCount

		when:
		reset()
		commonProperties.timeZone = timeZone

		then:
		commonProperties.timeZone.is timeZone
		factory.timeZone.is timeZone

		when:
		reset()
		commonProperties.timeZone timeZone

		then:
		commonProperties.timeZone.is timeZone
		factory.timeZone.is timeZone

		when:
		reset()
		commonProperties.unit = unit

		then:
		commonProperties.unit == unit
		factory.unit == unit

		when:
		reset()
		commonProperties.unit unit

		then:
		commonProperties.unit == unit
		factory.unit == unit
	}
}
