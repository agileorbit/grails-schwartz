package com.agileorbit.schwartz.listener

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.LoggingEvent
import org.quartz.JobExecutionException

class LoggingJobListenerSpec extends AbstractListenerSpec {

	private LoggingJobListener listener = new LoggingJobListener()

	void testJobToBeExecuted() {
		when:
		listener.jobToBeExecuted context

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.INFO
		event.message.contains 'fired (by trigger'
		event.argumentArray.length == 3
		event.argumentArray[0] == jobDetail.key
		event.argumentArray[1] == trigger.key
		event.argumentArray[2].contains(formattedDay())
	}

	void testJobExecutionVetoed() {
		when:
		listener.jobExecutionVetoed context

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.INFO
		event.message.contains 'was vetoed'
		event.argumentArray.length == 3
		event.argumentArray[0] == jobDetail.key
		event.argumentArray[1] == trigger.key
		event.argumentArray[2].contains(formattedDay())
	}

	void testJobWasExecuted_success() {
		when:
		context.result = 42
		listener.jobWasExecuted context, null

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.INFO
		event.message.contains 'execution complete'
		event.argumentArray.length == 3
		event.argumentArray[0] == jobDetail.key
		event.argumentArray[1].contains(formattedDay())
		event.argumentArray[2] == 42
	}

	void testJobWasExecuted_exception() {
		when:
		context.result = 42
		listener.jobWasExecuted context, new JobExecutionException('boom')

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.WARN
		event.message.contains 'execution failed'
		event.argumentArray.length == 3
		event.argumentArray[0] == jobDetail.key
		event.argumentArray[1].contains(formattedDay())
		event.argumentArray[2] == 'boom'
		event.throwableProxy
		event.throwableProxy.className == JobExecutionException.name
	}
}
