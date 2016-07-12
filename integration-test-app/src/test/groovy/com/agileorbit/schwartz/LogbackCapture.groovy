package com.agileorbit.schwartz

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.core.Appender
import groovy.transform.CompileStatic
import org.junit.rules.ExternalResource
import org.slf4j.LoggerFactory

@CompileStatic
class LogbackCapture extends ExternalResource {

	private Logger schwartzLogger = (Logger)LoggerFactory.getLogger('com.agileorbit.schwartz')
	private Level level

	private final Appender appender = [doAppend: { LoggingEvent e ->
		events << e
	}] as Appender

	final List<LoggingEvent> events = []

	protected void before() {
		level = schwartzLogger.level
		schwartzLogger.level = Level.TRACE
		schwartzLogger.addAppender appender
	}

	protected void after() {
		schwartzLogger.level = level
		schwartzLogger.detachAppender appender
	}
}
