package com.agileorbit.schwartz.test

import com.agileorbit.schwartz.SchwartzJobFactory
import com.agileorbit.schwartz.SchwartzSchedulerFactoryBean
import com.agileorbit.schwartz.util.QuartzSchedulerObjects
import org.apache.tomcat.jdbc.pool.DataSource as TomcatDataSource
import org.quartz.JobKey
import org.quartz.JobListener
import org.quartz.ListenerManager
import org.quartz.Matcher
import org.quartz.Scheduler
import org.quartz.SchedulerListener
import org.quartz.SchedulerMetaData
import org.quartz.TriggerKey
import org.quartz.TriggerListener
import org.quartz.core.QuartzScheduler
import org.quartz.core.QuartzSchedulerResources
import org.quartz.core.QuartzSchedulerThread
import org.quartz.ee.jta.JTAAnnotationAwareJobRunShellFactory
import org.quartz.ee.jta.JTAJobRunShellFactory
import org.quartz.impl.DefaultThreadExecutor
import org.quartz.impl.StdScheduler
import org.quartz.impl.jdbcjobstore.JobStoreSupport
import org.quartz.impl.jdbcjobstore.StdJDBCDelegate
import org.quartz.impl.jdbcjobstore.StdRowLockSemaphore
import org.quartz.impl.matchers.EverythingMatcher
import org.quartz.simpl.RAMJobStore
import org.quartz.simpl.SimpleThreadPool
import org.quartz.spi.ClassLoadHelper
import org.quartz.utils.PropertiesParser
import org.springframework.scheduling.quartz.LocalDataSourceJobStore
import org.springframework.scheduling.quartz.ResourceLoaderClassLoadHelper

import javax.sql.DataSource

import static org.quartz.impl.StdSchedulerFactory.*

class ConfigValidationSpec extends AbstractQuartzSpec {

	Scheduler quartzScheduler

	void 'check SchedulerMetaData'() {
		given:
		SchedulerMetaData meta = quartzScheduler.metaData

		expect:
		 meta.inStandbyMode
		 meta.jobStoreClass == RAMJobStore
		!meta.jobStoreClustered
		!meta.jobStoreSupportsPersistence
		!meta.numberOfJobsExecuted
		!meta.runningSince
		 meta.schedulerClass.name == StdScheduler.name
		 meta.schedulerInstanceId == 'NON_CLUSTERED'
		 meta.schedulerName == 'QuartzScheduler'
		!meta.schedulerRemote
		!meta.shutdown
		!meta.started
		 meta.threadPoolClass.name == SimpleThreadPool.name
		 meta.threadPoolSize == 10
		 meta.version == '2.2.3'
	}

	void 'check configured global listeners'() {
		given:
		ListenerManager listenerManager = quartzScheduler.listenerManager

		List<JobListener> jobListeners = [] + listenerManager.jobListeners
		List<TriggerListener> triggerListeners = [] + listenerManager.triggerListeners

		// remove all that aren't global listeners (SessionBinderJobListener)
		for (String name in jobListeners*.name) {
			List<Matcher<JobKey>> matchers = listenerManager.getJobListenerMatchers(name)
			assert matchers
			if (!(matchers.any { it instanceof EverythingMatcher })) {
				jobListeners.remove(jobListeners.find { it.name == name })
			}
		}
		for (String name in triggerListeners*.name) {
			List<Matcher<TriggerKey>> matchers = listenerManager.getTriggerListenerMatchers(name)
			assert matchers
			if (!(matchers.any { it instanceof EverythingMatcher })) {
				triggerListeners.remove(triggerListeners.find { it.name == name })
			}
		}

		List<SchedulerListener> schedulerListeners = listenerManager.schedulerListeners

		expect:
		jobListeners.size() == 3
		jobListeners*.name.sort() == ['ExceptionPrinterJobListener', 'LoggingJobListener', 'ProgressTrackingListener']

		triggerListeners.size() == 2
		triggerListeners*.name.sort() == ['LoggingTriggerListener', 'ProgressTrackingListener']

		schedulerListeners.size() == 1
		schedulerListeners[0].getClass().simpleName == 'LoggingSchedulerListener'
	}

	void testDefaultValues() {
		when:
		SchwartzSchedulerFactoryBean factoryBean = createFactoryBean('application_default_values')
		PropertiesParser cfg = new PropertiesParser(factoryBean['quartzProperties'] as Properties)

		then:
		'QuartzScheduler' == cfg.getStringProperty(PROP_SCHED_INSTANCE_NAME, 'QuartzScheduler')
		'QuartzScheduler_QuartzSchedulerThread' == cfg.getStringProperty(PROP_SCHED_THREAD_NAME,
				cfg.getStringProperty(PROP_SCHED_INSTANCE_NAME) + '_QuartzSchedulerThread')
		'NON_CLUSTERED' == cfg.getStringProperty(PROP_SCHED_INSTANCE_ID, DEFAULT_INSTANCE_ID)
		'org.quartz.simpl.SimpleInstanceIdGenerator' == cfg.getStringProperty(PROP_SCHED_INSTANCE_ID_GENERATOR_CLASS,
				'org.quartz.simpl.SimpleInstanceIdGenerator')
		!cfg.getStringProperty(PROP_SCHED_USER_TX_URL, null)
		'org.quartz.simpl.CascadingClassLoadHelper' == cfg.getStringProperty(PROP_SCHED_CLASS_LOAD_HELPER_CLASS,
				'org.quartz.simpl.CascadingClassLoadHelper')
		!cfg.getBooleanProperty(PROP_SCHED_WRAP_JOB_IN_USER_TX, false)
		!cfg.getStringProperty(PROP_SCHED_JOB_FACTORY_CLASS, null)
		30000L == cfg.getLongProperty(PROP_SCHED_IDLE_WAIT_TIME, -1)
		15000L == cfg.getLongProperty(PROP_SCHED_DB_FAILURE_RETRY_INTERVAL, 15000)
		!cfg.getBooleanProperty(PROP_SCHED_MAKE_SCHEDULER_THREAD_DAEMON)
		!cfg.getBooleanProperty(PROP_SCHED_SCHEDULER_THREADS_INHERIT_CONTEXT_CLASS_LOADER_OF_INITIALIZING_THREAD)
		cfg.getBooleanProperty(PROP_SCHED_SKIP_UPDATE_CHECK, true)
		0L == cfg.getLongProperty(PROP_SCHED_BATCH_TIME_WINDOW, 0L)
		1 == cfg.getIntProperty(PROP_SCHED_MAX_BATCH_SIZE, 1)
		!cfg.getBooleanProperty(PROP_SCHED_INTERRUPT_JOBS_ON_SHUTDOWN, false)
		!cfg.getBooleanProperty(PROP_SCHED_INTERRUPT_JOBS_ON_SHUTDOWN_WITH_WAIT, false)
		!cfg.getBooleanProperty(PROP_SCHED_JMX_EXPORT)
		!cfg.getStringProperty(PROP_SCHED_JMX_OBJECT_NAME)
		!cfg.getBooleanProperty(PROP_SCHED_JMX_PROXY)
		!cfg.getStringProperty(PROP_SCHED_JMX_PROXY_CLASS)
		!cfg.getBooleanProperty(PROP_SCHED_RMI_EXPORT, false)
		!cfg.getBooleanProperty(PROP_SCHED_RMI_PROXY, false)
		'localhost' == cfg.getStringProperty(PROP_SCHED_RMI_HOST, 'localhost')
		1099 == cfg.getIntProperty(PROP_SCHED_RMI_PORT, 1099)
		-1 == cfg.getIntProperty(PROP_SCHED_RMI_SERVER_PORT, -1)
		'never' == cfg.getStringProperty(PROP_SCHED_RMI_CREATE_REGISTRY, QuartzSchedulerResources.CREATE_REGISTRY_NEVER)
		!cfg.getStringProperty(PROP_SCHED_RMI_BIND_NAME)
		!cfg.getBooleanProperty(MANAGEMENT_REST_SERVICE_ENABLED, false)
		'0.0.0.0:9889' == cfg.getStringProperty(MANAGEMENT_REST_SERVICE_HOST_PORT, '0.0.0.0:9889')
		!cfg.getPropertyGroup(PROP_SCHED_CONTEXT_PREFIX, true)
		'org.quartz.simpl.SimpleThreadPool' == cfg.getStringProperty(PROP_THREAD_POOL_CLASS, SimpleThreadPool.name)
		validatePropertyGroup cfg, PROP_THREAD_POOL_PREFIX,
				[threadCount: '10', threadPriority: '5', class: 'org.quartz.simpl.SimpleThreadPool']
		'org.quartz.simpl.RAMJobStore' == cfg.getStringProperty(PROP_JOB_STORE_CLASS, RAMJobStore.name)
		validatePropertyGroup cfg, PROP_JOB_STORE_PREFIX, [:], [PROP_JOB_STORE_LOCK_HANDLER_PREFIX] as String[]
		!cfg.getStringProperty(PROP_JOB_STORE_LOCK_HANDLER_CLASS)
		validatePropertyGroup cfg, PROP_JOB_STORE_LOCK_HANDLER_PREFIX, [:]
		'org.quartz.impl.DefaultThreadExecutor' == cfg.getStringProperty(PROP_THREAD_EXECUTOR_CLASS)
		validatePropertyGroup cfg, PROP_THREAD_EXECUTOR, [class: 'org.quartz.impl.DefaultThreadExecutor']
	}

	void testNonDefaultValues() {
		when:
		SchwartzSchedulerFactoryBean factoryBean = createFactoryBean('application_nondefault_values')
		PropertiesParser cfg = new PropertiesParser(factoryBean['quartzProperties'] as Properties)

		then:
		'_testing_' == cfg.getStringProperty(PROP_SCHED_INSTANCE_NAME, 'QuartzScheduler')
		'FredThread' == cfg.getStringProperty(PROP_SCHED_THREAD_NAME,
				cfg.getStringProperty(PROP_SCHED_INSTANCE_NAME) + '_QuartzSchedulerThread')
		'foo' == cfg.getStringProperty(PROP_SCHED_INSTANCE_ID, DEFAULT_INSTANCE_ID)
		'org.quartz.simpl.HostnameInstanceIdGenerator' == cfg.getStringProperty(PROP_SCHED_INSTANCE_ID_GENERATOR_CLASS,
				'org.quartz.simpl.SimpleInstanceIdGenerator')
		!cfg.getStringProperty(PROP_SCHED_USER_TX_URL, null)
		'org.quartz.simpl.CascadingClassLoadHelper' == cfg.getStringProperty(PROP_SCHED_CLASS_LOAD_HELPER_CLASS,
				'org.quartz.simpl.CascadingClassLoadHelper')
		cfg.getBooleanProperty(PROP_SCHED_WRAP_JOB_IN_USER_TX, false)
		!cfg.getStringProperty(PROP_SCHED_JOB_FACTORY_CLASS, null)
		1234L == cfg.getLongProperty(PROP_SCHED_IDLE_WAIT_TIME, -1)
		123L == cfg.getLongProperty(PROP_SCHED_DB_FAILURE_RETRY_INTERVAL, 15000)
		cfg.getBooleanProperty(PROP_SCHED_MAKE_SCHEDULER_THREAD_DAEMON)
		cfg.getBooleanProperty(PROP_SCHED_SCHEDULER_THREADS_INHERIT_CONTEXT_CLASS_LOADER_OF_INITIALIZING_THREAD)
		!cfg.getBooleanProperty(PROP_SCHED_SKIP_UPDATE_CHECK, true)
		0L == cfg.getLongProperty(PROP_SCHED_BATCH_TIME_WINDOW, 0L)
		1 == cfg.getIntProperty(PROP_SCHED_MAX_BATCH_SIZE, 1)
		cfg.getBooleanProperty(PROP_SCHED_INTERRUPT_JOBS_ON_SHUTDOWN, false)
		cfg.getBooleanProperty(PROP_SCHED_INTERRUPT_JOBS_ON_SHUTDOWN_WITH_WAIT, false)
		cfg.getBooleanProperty(PROP_SCHED_JMX_EXPORT)
		!cfg.getStringProperty(PROP_SCHED_JMX_OBJECT_NAME)
		!cfg.getBooleanProperty(PROP_SCHED_JMX_PROXY)
		!cfg.getStringProperty(PROP_SCHED_JMX_PROXY_CLASS)
		!cfg.getBooleanProperty(PROP_SCHED_RMI_EXPORT, false)
		!cfg.getBooleanProperty(PROP_SCHED_RMI_PROXY, false)
		'localhost' == cfg.getStringProperty(PROP_SCHED_RMI_HOST, 'localhost')
		1099 == cfg.getIntProperty(PROP_SCHED_RMI_PORT, 1099)
		-1 == cfg.getIntProperty(PROP_SCHED_RMI_SERVER_PORT, -1)
		'never' == cfg.getStringProperty(PROP_SCHED_RMI_CREATE_REGISTRY, QuartzSchedulerResources.CREATE_REGISTRY_NEVER)
		!cfg.getStringProperty(PROP_SCHED_RMI_BIND_NAME)
		!cfg.getBooleanProperty(MANAGEMENT_REST_SERVICE_ENABLED, false)
		'0.0.0.0:9889' == cfg.getStringProperty(MANAGEMENT_REST_SERVICE_HOST_PORT, '0.0.0.0:9889')
		!cfg.getPropertyGroup(PROP_SCHED_CONTEXT_PREFIX, true)
		'org.quartz.simpl.SimpleThreadPool' == cfg.getStringProperty(PROP_THREAD_POOL_CLASS, SimpleThreadPool.name)
		validatePropertyGroup cfg, PROP_THREAD_POOL_PREFIX,
				[threadCount: '2', threadPriority: '2', class: 'org.quartz.simpl.SimpleThreadPool']
		'org.quartz.simpl.RAMJobStore' == cfg.getStringProperty(PROP_JOB_STORE_CLASS, RAMJobStore.name)
		validatePropertyGroup cfg, PROP_JOB_STORE_PREFIX, [:], [PROP_JOB_STORE_LOCK_HANDLER_PREFIX] as String[]
		!cfg.getStringProperty(PROP_JOB_STORE_LOCK_HANDLER_CLASS)
		validatePropertyGroup cfg, PROP_JOB_STORE_LOCK_HANDLER_PREFIX, [:]
		'org.quartz.impl.DefaultThreadExecutor' == cfg.getStringProperty(PROP_THREAD_EXECUTOR_CLASS)
		validatePropertyGroup cfg, PROP_THREAD_EXECUTOR, [class: 'org.quartz.impl.DefaultThreadExecutor']
	}

	void testConfiguredQuartzObjects() {
		when:
		QuartzSchedulerObjects quartz = buildScheduler('application_nondefault_values')

		then:
		quartz.scheduler instanceof StdScheduler

		when:
		QuartzScheduler qs = quartz.qs

		then:
		qs.jobFactory instanceof SchwartzJobFactory

		qs.schedulerContext.getWrappedMap().size() == 1
		qs.schedulerContext.getWrappedMap().applicationContext == grailsApplication.mainContext

		qs.internalJobListeners*.name.sort() ==
				['QuartzSchedulerMBeanImpl.listener', 'org.quartz.core.ExecutingJobsManager']
		qs.internalSchedulerListeners*.getClass().name.sort() ==
				['org.quartz.core.ErrorLogger', 'org.quartz.core.QuartzSchedulerMBeanImpl']
		!qs.internalTriggerListeners

		quartz.schedulerName == '_testing_'
		quartz.schedulerInstanceId == 'foo'
		!quartz.resources.RMIRegistryHost

		quartz.ramJobStore

		quartz.jobRunShellFactory instanceof JTAJobRunShellFactory // wrapJobExecutionInUserTransaction
		!quartz.schedulerPlugins
		quartz.makeSchedulerThreadDaemon
		quartz.threadsInheritInitializersClassLoadContext
		quartz.jmxExport
		quartz.runUpdateCheck
		quartz.threadExecutor instanceof DefaultThreadExecutor
		quartz.interruptJobsOnShutdown
		quartz.interruptJobsOnShutdownWithWait

		QuartzSchedulerThread schedulerThread = findFieldValue(qs, 'schedThread', QuartzSchedulerThread)
		findFieldValue(schedulerThread, 'idleWaitTime', long) == 1234L
		schedulerThread.name == 'FredThread'
		schedulerThread.daemon

		quartz.threadPool instanceof SimpleThreadPool
		quartz.threadPool.poolSize == 2
		((SimpleThreadPool)quartz.threadPool).threadPriority == 2
		((SimpleThreadPool)quartz.threadPool).threadsInheritContextClassLoaderOfInitializingThread
	}

	void testConfiguredQuartzObjectsCluster() {
		when:
		QuartzSchedulerObjects quartz = buildScheduler('application_cluster')

		then:
		quartz.scheduler instanceof StdScheduler

		when:
		QuartzScheduler qs = quartz.qs
		QuartzSchedulerResources resources = quartz.resources

		then:
		qs.jobFactory instanceof SchwartzJobFactory

		qs.schedulerContext.getWrappedMap().size() == 1
		qs.schedulerContext.getWrappedMap().applicationContext == grailsApplication.mainContext

		qs.internalJobListeners*.name.sort() == ['org.quartz.core.ExecutingJobsManager']
		qs.internalSchedulerListeners*.getClass().name.sort() == ['org.quartz.core.ErrorLogger']
		!qs.internalTriggerListeners

		quartz.schedulerName == 'UsingJDBC'
		quartz.schedulerInstanceId == 'jdbc1'
		!resources.RMIRegistryHost

		quartz.jobRunShellFactory instanceof JTAAnnotationAwareJobRunShellFactory // !wrapJobExecutionInUserTransaction
		!quartz.schedulerPlugins
		!quartz.makeSchedulerThreadDaemon
		!quartz.threadsInheritInitializersClassLoadContext
		!quartz.jmxExport
		!quartz.runUpdateCheck
		quartz.threadExecutor instanceof DefaultThreadExecutor
		!quartz.interruptJobsOnShutdown
		!quartz.interruptJobsOnShutdownWithWait

		QuartzSchedulerThread schedulerThread = findFieldValue(qs, 'schedThread', QuartzSchedulerThread)
		findFieldValue(schedulerThread, 'idleWaitTime', long) == 30000L
		schedulerThread.name == 'UsingJDBC_QuartzSchedulerThread'
		!schedulerThread.daemon

		quartz.threadPool instanceof SimpleThreadPool
		quartz.threadPool.poolSize == 10
		((SimpleThreadPool)quartz.threadPool).threadPriority == 5
		!((SimpleThreadPool)quartz.threadPool).threadsInheritContextClassLoaderOfInitializingThread

		quartz.jobStoreClustered
		quartz.jdbcDelegate?.getClass() == StdJDBCDelegate
		quartz.jdbcJobStore

		when:
		JobStoreSupport jdbcJobStore = quartz.jdbcJobStore

		then:
		jdbcJobStore.acquireTriggersWithinLock
		findFieldValue(jdbcJobStore, 'classLoadHelper', ClassLoadHelper) instanceof ResourceLoaderClassLoadHelper
		jdbcJobStore.clusterCheckinInterval == 5000
		jdbcJobStore.dbRetryInterval == 15000
		jdbcJobStore.dontSetAutoCommitFalse
		jdbcJobStore.doubleCheckLockMisfireHandler
		!jdbcJobStore.driverDelegateInitString
		findFieldValue(jdbcJobStore, 'lockHandler') instanceof StdRowLockSemaphore
		jdbcJobStore.lockOnInsert
		!jdbcJobStore.makeThreadsDaemons
		jdbcJobStore.maxMisfiresToHandleAtATime == 2
		jdbcJobStore.misfireThreshold == 50000
		jdbcJobStore.selectWithLockSQL == 'select * from {0}LOCKS where SCHED_NAME = {1} and LOCK_NAME = ? for update'
		jdbcJobStore.tablePrefix == 'QUARTZ_'
		jdbcJobStore.threadExecutor instanceof DefaultThreadExecutor
		!jdbcJobStore.threadsInheritInitializersClassLoadContext
		jdbcJobStore.txIsolationLevelSerializable
		jdbcJobStore.useDBLocks
		jdbcJobStore.canUseProperties()

		jdbcJobStore instanceof LocalDataSourceJobStore

		when:
		LocalDataSourceJobStore dsJobStore = (LocalDataSourceJobStore)jdbcJobStore

		then:
		DataSource dataSource = findFieldValue(dsJobStore, 'dataSource', DataSource)
		dataSource instanceof TomcatDataSource
		((TomcatDataSource)dataSource).url == 'jdbc:h2:mem:clusterDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE'
		dsJobStore.dontSetNonManagedTXConnectionAutoCommitFalse
		dsJobStore.txIsolationLevelReadCommitted
	}

	private boolean validatePropertyGroup(PropertiesParser cfg, String name, Map expected, String[] excludedPrefixes = null) {
		Properties tProps = cfg.getPropertyGroup(name, true, excludedPrefixes)
		Properties expectedProperties = expected as Properties
		assert expectedProperties == tProps
		true
	}
}
