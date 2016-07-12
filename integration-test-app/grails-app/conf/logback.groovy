import grails.util.BuildSettings
import grails.util.Environment

String defaultPattern = '%.-2level %relative %logger{1} - %message%n'

File targetDir = BuildSettings.TARGET_DIR

appender('STDOUT', ConsoleAppender) {
	encoder(PatternLayoutEncoder) {
		pattern = defaultPattern
	}
}

appender 'file', FileAppender, {
	file = "$targetDir/file.log"
	append = false
	encoder(PatternLayoutEncoder) {
		pattern = defaultPattern
	}
}

root ERROR, ['file']

logger 'com.agileorbit.schwartz', TRACE
logger 'org.quartz', DEBUG
logger 'org.quartz.impl.jdbcjobstore.StdRowLockSemaphore', WARN

if (Environment.developmentMode && targetDir) {

	appender('FULL_STACKTRACE', FileAppender) {
		file = "$targetDir/stacktrace.log"
		append = true
		encoder(PatternLayoutEncoder) {
			pattern = defaultPattern
		}
	}

	logger 'StackTrace', ERROR, ['FULL_STACKTRACE'], false
}
