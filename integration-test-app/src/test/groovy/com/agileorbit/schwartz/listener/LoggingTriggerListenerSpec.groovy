package com.agileorbit.schwartz.listener

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.spi.LoggingEvent

import static org.quartz.Trigger.CompletedExecutionInstruction.NOOP

class LoggingTriggerListenerSpec extends AbstractListenerSpec {

	private LoggingTriggerListener listener = new LoggingTriggerListener()

	void testTriggerFired() {
		when:
		listener.triggerFired trigger, context

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.INFO
		event.message.contains ' fired job '
		event.argumentArray.length == 3
		event.argumentArray[0] == trigger.key
		event.argumentArray[1] == jobDetail.key
		event.argumentArray[2].contains(formattedDay())
	}

	void testTriggerMisfired() {
		when:
		listener.triggerMisfired trigger

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.INFO
		event.message.contains ' misfired job '
		event.argumentArray.length == 4
		event.argumentArray[0] == trigger.key
		event.argumentArray[1] == jobDetail.key
		event.argumentArray[2].contains(formattedDay())
		event.argumentArray[3].contains(formattedDay(new Date() + 1))
	}

	void testTriggerComplete() {
		when:
		listener.triggerComplete trigger, context, NOOP

		then:
		logbackCapture.events.size() == 1

		when:
		LoggingEvent event = logbackCapture.events[0]

		then:
		event.level == Level.INFO
		event.message.contains ' completed firing job '
		event.argumentArray.length == 4
		event.argumentArray[0] == trigger.key
		event.argumentArray[1] == jobDetail.key
		event.argumentArray[2].contains(formattedDay())
		event.argumentArray[3] == NOOP
	}

	void testVetoJobExecution() {
		when:
		boolean veto = listener.vetoJobExecution(trigger, context)

		then:
		!veto
		!logbackCapture.events
	}
}
