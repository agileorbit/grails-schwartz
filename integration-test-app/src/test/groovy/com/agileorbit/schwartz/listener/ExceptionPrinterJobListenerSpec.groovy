package com.agileorbit.schwartz.listener

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.LoggingEvent
import org.quartz.JobExecutionException

class ExceptionPrinterJobListenerSpec extends AbstractListenerSpec {

	private ExceptionPrinterJobListener listener = new ExceptionPrinterJobListener()

	void testJobToBeExecuted() {
		when:
		listener.jobToBeExecuted context

		then:
		!logbackCapture.events
	}

	void testJobExecutionVetoed() {
		when:
		listener.jobExecutionVetoed context

		then:
		!logbackCapture.events
	}

	void testJobWasExecuted_success() {
		when:
		listener.jobWasExecuted context, null

		then:
		!logbackCapture.events
	}

	void testJobWasExecuted_exception() {
		when:
		listener.jobWasExecuted context, new JobExecutionException('boom')

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.ERROR
		event.message.contains 'Exception occurred'
		event.argumentArray.length == 1
		event.argumentArray[0] == jobDetail.key
		event.throwableProxy
		event.throwableProxy.className == JobExecutionException.name
	}
}
