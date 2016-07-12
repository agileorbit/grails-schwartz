package com.agileorbit.schwartz.builder

class AbstractPropertiesSpec extends PropertiesSpec {

	void 'check duplicates inside property lists and across lists'() {
		expect:
		!duplicates(AbstractProperties.PROPERTIES[CommonProperties])
		!duplicates(AbstractProperties.PROPERTIES[CalendarProperties])
		!duplicates(AbstractProperties.PROPERTIES[CronProperties])
		!duplicates(AbstractProperties.PROPERTIES[DailyProperties])
		!duplicates(AbstractProperties.PROPERTIES[SimpleProperties])
		!duplicates(AbstractProperties.PROPERTIES[TriggerProperties])

		!duplicates(AbstractProperties.PROPERTIES.values().sum())

		// sanity check duplicates()
		[] == duplicates(['apple', 'pear', 'banana'])
		['banana'] == duplicates(['apple', 'banana', 'pear', 'banana'] )
	}

	void 'BuilderFactory properties include those from all of the delegate instances'() {
		when:
		List<String> factoryPropertyNames = namesFromProperties(factory)
		factoryPropertyNames.remove 'builderType'
		factoryPropertyNames.remove 'scheduleBuilder'
		factoryPropertyNames.remove 'triggerBuilder'

		then:
		factoryPropertyNames == allNames
	}

	private List<String> duplicates(List<String> names) {
		List<String> copy = [] + names
		// Collection.removeAll() and DGM.minus() both remove everything
		for (value in names.toUnique()) copy.remove value
		copy.unique()
	}
}
