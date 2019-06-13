package com.agileorbit.schwartz.test

import com.agileorbit.schwartz.SchwartzSchedulerFactoryBean
import com.agileorbit.schwartz.util.InstantiateInterceptSchedulerFactory
import com.agileorbit.schwartz.util.QuartzSchedulerObjects
import grails.config.Config
import grails.core.GrailsApplication
import grails.testing.mixin.integration.Integration
import groovy.sql.Sql
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.grails.config.NavigableMap
import org.grails.config.NavigableMapPropertySource
import org.grails.config.PropertySourcesConfig
import org.junit.Rule
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.env.MutablePropertySources
import org.springframework.util.ReflectionUtils
import spock.lang.Specification

import javax.sql.DataSource
import java.lang.reflect.Field
import java.sql.Connection
import java.sql.ResultSet

@CompileStatic
@Integration
abstract class AbstractQuartzSpec extends Specification {

	private static final String CREATE_TABLE = 'CREATE TABLE '

	protected static final List<String> tableNames = [
			'CRON_TRIGGERS', 'SIMPLE_TRIGGERS', 'SIMPROP_TRIGGERS', 'TRIGGERS', 'BLOB_TRIGGERS', 'CALENDARS',
			'FIRED_TRIGGERS', 'JOB_DETAILS', 'LOCKS', 'PAUSED_TRIGGER_GRPS', 'SCHEDULER_STATE'].asImmutable()

	protected final Logger log = LoggerFactory.getLogger(getClass())

	@Rule TestWatcher watcher = new TestWatcher() {
		private String name(Description description) {
			'"' + description.methodName + '"(' + description.testClass.simpleName + ')'
		}
		protected void starting(Description description) {
			println "Test ${name(description)} starting"
		}
		protected void succeeded(Description description) {
			println "Test ${name(description)} succeeded"
		}
		protected void failed(Throwable e, Description description) {
			println "Test ${name(description)} failed, ${e.getClass().name} : ${e.message}"
		}
	}

	protected Collection<QuartzSchedulerObjects> quartzInstances = []
	protected Sql sql

	DataSource clusterDataSource
	GrailsApplication grailsApplication

	void setup() {
		sql = new Sql(clusterDataSource)
		createTables()
	}

	void cleanup() {
		quartzInstances*.shutdown()
		dropTables()
		sql.close()
	}

	protected SchwartzSchedulerFactoryBean createFactoryBean(Config config) {
		def factoryBean = new SchwartzSchedulerFactoryBean([getConfig: { -> config }] as GrailsApplication)
		factoryBean.schedulerFactoryClass = InstantiateInterceptSchedulerFactory
		factoryBean
	}

	protected SchwartzSchedulerFactoryBean createFactoryBean(String filename) {
		def propertySource = new NavigableMap()
		propertySource.merge new ConfigSlurper().parse(getClass().getResource(filename + '.groovy'))

		MutablePropertySources mps = new MutablePropertySources()
		mps.addLast new NavigableMapPropertySource(filename, propertySource)
		createFactoryBean new PropertySourcesConfig(mps)
	}

	protected QuartzSchedulerObjects buildScheduler(String configFileName) {
		SchwartzSchedulerFactoryBean factoryBean = createFactoryBean(configFileName)
		factoryBean.applicationContext = grailsApplication.mainContext
		factoryBean.resourceLoader = grailsApplication.mainContext
		factoryBean.afterPropertiesSet()

		QuartzSchedulerObjects quartz = QuartzSchedulerObjects.getInstance(factoryBean.scheduler)
		quartzInstances << quartz
		quartz
	}

	protected <T> T findFieldValue(instance, String name, Class<T> type = null) {
		Field field = ReflectionUtils.findField(instance.getClass(), name, type)
		ReflectionUtils.makeAccessible field
		(T) field.get(instance)
	}

	protected Collection<String> allTableNames() {
		Connection connection
		ResultSet rs

		try {
			connection = clusterDataSource.connection;
			rs = connection.metaData.getTables(null, null, null, ['TABLE'] as String[])
			def tableNames = []
			while (rs.next()) {
				tableNames << rs.getString('TABLE_NAME')
			}
			tableNames.sort()
		}
		finally {
			try { rs?.close() } catch (ignored) {}
			try { connection?.close() } catch (ignored) {}
		}
	}

	protected void createTables() {
		// log.debug 'tables before createTables(): {}', allTableNames()

		for (String ddl in tableStatements()) {

			int index = ddl.indexOf(CREATE_TABLE)
			if (index > -1) {
				String table = ddl.substring(index + CREATE_TABLE.length(),
						ddl.indexOf(' ', index + CREATE_TABLE.length() + 1))
				// log.debug '{}{}', CREATE_TABLE, table
			}

			sql.executeUpdate ddl
		}

		// log.debug 'tables after createTables(): {}', allTableNames()
	}

	protected void dropTables() {
		// log.debug 'tables before dropTables(): {}', allTableNames()
		for (name in tableNames) {
			sql.executeUpdate 'DROP TABLE QRTZ_' + name
		}
		// log.debug 'tables after dropTables(): {}', allTableNames()
	}

	protected List<String> tableStatements() {
		getClass().getResourceAsStream('tables_h2.sql').text
				.toUpperCase()
				.replaceAll('\n', ' ')
				.replaceAll('\\s+', ' ')
				.split(';')*.trim()
				.findAll()
	}
}
