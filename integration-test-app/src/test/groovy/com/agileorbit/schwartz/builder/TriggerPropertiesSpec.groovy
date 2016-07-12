package com.agileorbit.schwartz.builder

import com.agileorbit.schwartz.SchwartzJob
import com.agileorbit.schwartz.SimpleSchwartzJob
import org.quartz.JobDataMap
import org.quartz.JobDetail
import org.quartz.JobKey
import org.quartz.TriggerKey
import org.quartz.impl.JobDetailImpl

import static org.quartz.JobKey.jobKey
import static org.quartz.TriggerKey.triggerKey

class TriggerPropertiesSpec extends PropertiesSpec {

	void testPropertyNames() {
		expect:
		triggerProperties.propertyNames() == ['calendarName', 'description', 'endAt', 'group', 'job', 'jobData',
		                                      'jobDataMap', 'jobDetail', 'jobGroup', 'jobKey', 'jobName', 'key',
		                                      'name', 'priority', 'startAt']

		triggerProperties.propertyNames() == namesFromProperties(triggerProperties)

		triggerProperties.allNames() == triggerProperties.propertyNames()
	}

	void 'utility methods'() {
		setup:
		reset()
		int delay = 10000

		when:
		triggerProperties.startDelay = delay

		then:
		assertNegligibleTimeDifference triggerProperties.startAt, delay

		when:
		reset()
		triggerProperties.startDelay delay * 2

		then:
		assertNegligibleTimeDifference triggerProperties.startAt, delay * 2

		when:
		triggerProperties.startNow()

		then:
		assertNegligibleTimeDifference triggerProperties.startAt, 0

		cleanup:
		reset()
	}

	void 'verify set/get for properties'() {
		given:
		String calendarName = 'a'
		String description = 'b'
		Date endAt = new Date() + 2
		String group = 'c'
		SchwartzJob job = new SimpleSchwartzJob()
		Map<String, ?> jobData = [a: 5, b: 2000]
		JobDataMap jobDataMap = new JobDataMap(c: 'see', d: new Date())
		JobDetail jobDetail = new JobDetailImpl(name: 'x', group: 'y')
		String jobGroup = 'd'
		JobKey jobKey = jobKey('e', 'f')
		String jobName = 'g'
		TriggerKey key = triggerKey('h', 'i')
		String name = 'j'
		int priority = 3
		Date startAt = new Date() + 200

		when:
		reset()
		triggerProperties.calendarName = calendarName

		then:
		triggerProperties.calendarName == calendarName
		factory.calendarName == calendarName

		when:
		reset()
		triggerProperties.calendarName calendarName

		then:
		triggerProperties.calendarName == calendarName
		factory.calendarName == calendarName

		when:
		reset()
		triggerProperties.description = description

		then:
		triggerProperties.description == description
		factory.description == description

		when:
		reset()
		triggerProperties.description description

		then:
		triggerProperties.description == description
		factory.description == description

		when:
		reset()
		triggerProperties.endAt = endAt

		then:
		triggerProperties.endAt == endAt
		factory.endAt == endAt

		when:
		reset()
		triggerProperties.endAt endAt

		then:
		triggerProperties.endAt == endAt
		factory.endAt == endAt

		when:
		reset()
		triggerProperties.group = group

		then:
		triggerProperties.group == group
		factory.group == group

		when:
		reset()
		triggerProperties.group group

		then:
		triggerProperties.group == group
		factory.group == group

		when:
		reset()
		triggerProperties.job = job

		then:
		triggerProperties.job.is job
		factory.job.is job

		when:
		reset()
		triggerProperties.job job

		then:
		triggerProperties.job.is job
		factory.job.is job

		when:
		reset()
		triggerProperties.jobData = jobData

		then:
		triggerProperties.jobData == jobData
		factory.jobData == jobData

		when:
		reset()
		triggerProperties.jobData jobData

		then:
		triggerProperties.jobData == jobData
		factory.jobData == jobData

		when:
		reset()
		triggerProperties.jobDataMap = jobDataMap

		then:
		triggerProperties.jobDataMap == jobDataMap
		factory.jobDataMap == jobDataMap

		when:
		reset()
		triggerProperties.jobDataMap jobDataMap

		then:
		triggerProperties.jobDataMap == jobDataMap
		factory.jobDataMap == jobDataMap

		when:
		reset()
		triggerProperties.jobDetail = jobDetail

		then:
		triggerProperties.jobDetail == jobDetail
		factory.jobDetail == jobDetail

		when:
		reset()
		triggerProperties.jobDetail jobDetail

		then:
		triggerProperties.jobDetail == jobDetail
		factory.jobDetail == jobDetail

		when:
		reset()
		triggerProperties.jobGroup = jobGroup

		then:
		triggerProperties.jobGroup == jobGroup
		factory.jobGroup == jobGroup

		when:
		reset()
		triggerProperties.jobGroup jobGroup

		then:
		triggerProperties.jobGroup == jobGroup
		factory.jobGroup == jobGroup

		when:
		reset()
		triggerProperties.jobKey = jobKey

		then:
		triggerProperties.jobKey == jobKey
		factory.jobKey == jobKey

		when:
		reset()
		triggerProperties.jobKey jobKey

		then:
		triggerProperties.jobKey == jobKey
		factory.jobKey == jobKey

		when:
		reset()
		triggerProperties.jobName = jobName

		then:
		triggerProperties.jobName == jobName
		factory.jobName == jobName

		when:
		reset()
		triggerProperties.jobName jobName

		then:
		triggerProperties.jobName == jobName
		factory.jobName == jobName

		when:
		reset()
		triggerProperties.key = key

		then:
		triggerProperties.key == key
		factory.key == key

		when:
		reset()
		triggerProperties.key key

		then:
		triggerProperties.key == key
		factory.key == key

		when:
		reset()
		triggerProperties.name = name

		then:
		triggerProperties.name == name
		factory.name == name

		when:
		reset()
		triggerProperties.name name

		then:
		triggerProperties.name == name
		factory.name == name

		when:
		reset()
		triggerProperties.priority = priority

		then:
		triggerProperties.priority == priority
		factory.priority == priority

		when:
		reset()
		triggerProperties.priority priority

		then:
		triggerProperties.priority == priority
		factory.priority == priority

		when:
		reset()
		triggerProperties.startAt = startAt

		then:
		triggerProperties.startAt == startAt
		factory.startAt == startAt

		when:
		reset()
		triggerProperties.startAt startAt

		then:
		triggerProperties.startAt == startAt
		factory.startAt == startAt
	}
}
