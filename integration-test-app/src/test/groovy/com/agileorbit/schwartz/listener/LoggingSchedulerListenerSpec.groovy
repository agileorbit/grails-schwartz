package com.agileorbit.schwartz.listener

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.LoggingEvent
import org.quartz.JobExecutionException
import org.quartz.SchedulerException

class LoggingSchedulerListenerSpec extends AbstractListenerSpec {

	private LoggingSchedulerListener listener = new LoggingSchedulerListener()

	void testJobScheduled() {
		when:
		listener.jobScheduled trigger

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.DEBUG
		event.message.contains 'jobScheduled: '
		event.argumentArray.length == 1
		event.argumentArray[0] == trigger.key
	}

	void testJobUnscheduled() {
		when:
		listener.jobUnscheduled trigger.key

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.DEBUG
		event.message.contains 'jobUnscheduled: '
		event.argumentArray.length == 1
		event.argumentArray[0] == trigger.key
	}

	void testTriggerFinalized() {
		when:
		listener.triggerFinalized trigger

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.DEBUG
		event.message.contains 'triggerFinalized: '
		event.argumentArray.length == 1
		event.argumentArray[0] == trigger.key
	}

	void testTriggerPaused() {
		when:
		listener.triggerPaused trigger.key

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.DEBUG
		event.message.contains 'triggerPaused: '
		event.argumentArray.length == 1
		event.argumentArray[0] == trigger.key
	}

	void testTriggersPaused() {
		when:
		listener.triggersPaused 'a trigger group'

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.DEBUG
		event.message.contains 'triggersPaused: '
		event.argumentArray.length == 1
		event.argumentArray[0] == 'a trigger group'
	}

	void testTriggerResumed() {
		when:
		listener.triggerResumed trigger.key

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.DEBUG
		event.message.contains 'triggerResumed: '
		event.argumentArray.length == 1
		event.argumentArray[0] == trigger.key
	}

	void testTriggersResumed() {
		when:
		listener.triggersResumed 'a trigger group'

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.DEBUG
		event.message.contains 'triggersResumed: '
		event.argumentArray.length == 1
		event.argumentArray[0] == 'a trigger group'
	}

	void testJobAdded() {
		when:
		listener.jobAdded jobDetail

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.DEBUG
		event.message.contains 'jobAdded: '
		event.argumentArray.length == 1
		event.argumentArray[0] == jobDetail.key
	}

	void testJobDeleted() {
		when:
		listener.jobDeleted jobDetail.key

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.DEBUG
		event.message.contains 'jobDeleted: '
		event.argumentArray.length == 1
		event.argumentArray[0] == jobDetail.key
	}

	void testJobPaused() {
		when:
		listener.jobPaused jobDetail.key

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.DEBUG
		event.message.contains 'jobPaused: '
		event.argumentArray.length == 1
		event.argumentArray[0] == jobDetail.key
	}

	void testJobsPaused() {
		when:
		listener.jobsPaused 'the job group'

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.DEBUG
		event.message.contains 'jobsPaused: '
		event.argumentArray.length == 1
		event.argumentArray[0] == 'the job group'
	}

	void testJobResumed() {
		when:
		listener.jobResumed jobDetail.key

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.DEBUG
		event.message.contains 'jobResumed: '
		event.argumentArray.length == 1
		event.argumentArray[0] == jobDetail.key
	}

	void testJobsResumed() {
		when:
		listener.jobsResumed 'the group'

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.DEBUG
		event.message.contains 'jobsResumed: '
		event.argumentArray.length == 1
		event.argumentArray[0] == 'the group'
	}

	void testSchedulerError() {
		given:
		SchedulerException e
		when:
		listener.schedulerError 'oh no', new JobExecutionException('something bad happened')

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.ERROR
		event.message == 'oh no'
		!event.argumentArray
		event.throwableProxy
		event.throwableProxy.className == JobExecutionException.name
	}

	void testSchedulerInStandbyMode() {
		given:
		SchedulerException e
		when:
		listener.schedulerInStandbyMode()

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.DEBUG
		event.message == 'schedulerInStandbyMode()'
		!event.argumentArray
	}

	void testSchedulerStarted() {
		given:
		SchedulerException e
		when:
		listener.schedulerStarted()

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.DEBUG
		event.message == 'schedulerStarted()'
		!event.argumentArray
	}

	void testSchedulerStarting() {
		given:
		SchedulerException e
		when:
		listener.schedulerStarting()

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.DEBUG
		event.message == 'schedulerStarting()'
		!event.argumentArray
	}

	void testSchedulerShutdown() {
		given:
		SchedulerException e
		when:
		listener.schedulerShutdown()

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.DEBUG
		event.message == 'schedulerShutdown()'
		!event.argumentArray
	}

	void testSchedulerShuttingdown() {
		given:
		SchedulerException e
		when:
		listener.schedulerShuttingdown()

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.DEBUG
		event.message == 'schedulerShuttingdown()'
		!event.argumentArray
	}

	void testSchedulingDataCleared() {
		given:
		SchedulerException e
		when:
		listener.schedulingDataCleared()

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.DEBUG
		event.message == 'schedulingDataCleared()'
		!event.argumentArray
	}
}
