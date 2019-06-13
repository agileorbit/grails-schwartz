import org.quartz.core.QuartzSchedulerResources
import org.quartz.impl.DefaultThreadExecutor
import org.quartz.impl.jdbcjobstore.StdJDBCDelegate
import org.quartz.simpl.SimpleInstanceIdGenerator
import org.quartz.simpl.SimpleThreadPool

dataSource {
	dbCreate = 'create-drop'
	driverClassName = 'org.h2.Driver'
	password = ''
	pooled = true
	url = 'jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE'
	username = 'sa'
}

grails {
	controllers.defaultScope = 'singleton'
	converters.encoding = 'UTF-8'
	mime {
		disable.accept.header.userAgents = ['Gecko', 'WebKit', 'Presto', 'Trident']
		types = [
			all          : '*/*',
			atom         : 'application/atom+xml',
			css          : 'text/css',
			csv          : 'text/csv',
			form         : 'application/x-www-form-urlencoded',
			html         : ['text/html', 'application/xhtml+xml'],
			js           : 'text/javascript',
			json         : ['application/json', 'text/json'],
			multipartForm: 'multipart/form-data',
			pdf          : 'application/pdf',
			rss          : 'application/rss+xml',
			text         : 'text/plain',
			hal          : ['application/hal+json', 'application/hal+xml'],
			xml          : ['text/xml', 'application/xml']
		]
	}
	spring.transactionManagement.proxies = false
	urlmapping.cache.maxsize = 1000
	views.default.codec = 'html'
	views {
		gsp {
			codecs {
				expression = 'html'
				scriptlets = 'html'
				staticparts = 'none'
				taglib = 'none'
			}
			encoding = 'UTF-8'
			htmlcodec = 'xml'
		}
	}
}

hibernate {
	cache {
		queries = false
		use_second_level_cache = true
		use_query_cache = false
		region.factory_class = 'org.hibernate.cache.ehcache.EhCacheRegionFactory'
	}
}

spring.groovy.template.'check-template-location' = false

quartz {
	autoStartup = false // default is true
	clearQuartzDataOnStartup = false
	exposeSchedulerInRepository = false
	jdbcStore = false
	jdbcStoreDataSource = 'dataSource'
	purgeQuartzTablesOnStartup = false
	startupDelay = 0
	waitForJobsToCompleteOnShutdown = false

	properties {
		// RAMJobStore
		jobStore {
			misfireThreshold = 60000
		}
		// JDBC
		/*
		jobStore {
			acquireTriggersWithinLock = false // should be true if batchTriggerAcquisitionMaxCount > 1
			clusterCheckinInterval = 7500
			dontSetAutoCommitFalse = false
			dontSetNonManagedTXConnectionAutoCommitFalse = false
			driverDelegateClass = StdJDBCDelegate.name
			driverDelegateInitString = null
			isClustered = false
			lockHandler.class = null // if null Quartz will determine which to use
			maxMisfiresToHandleAtATime = 20
			misfireThreshold = 60000
			selectWithLockSQL = 'SELECT * FROM {0}LOCKS WHERE SCHED_NAME = {1} AND LOCK_NAME = ? FOR UPDATE'
			tablePrefix = 'QRTZ_'
			txIsolationLevelReadCommitted = false
			txIsolationLevelSerializable = false
			useProperties = false
		}
		*/
		managementRESTService {
			bind = '0.0.0.0:9889'
			enabled = false
		}
		scheduler {
			batchTriggerAcquisitionFireAheadTimeWindow = 0
			batchTriggerAcquisitionMaxCount = 1
			classLoadHelper.class = null // Quartz default is CascadingClassLoadHelper but SchedulerFactoryBean
			                             // configures a ResourceLoaderClassLoadHelper if the property isn't set
			dbFailureRetryInterval = 15000
			idleWaitTime = 30000
			instanceId = 'NON_CLUSTERED'
			instanceIdGenerator.class = SimpleInstanceIdGenerator.name
			instanceName = 'QuartzScheduler'
			interruptJobsOnShutdown = false
			interruptJobsOnShutdownWithWait = false
			jmx {
				export = true // default is false
				objectName = null // if null Quartz will generate with QuartzSchedulerResources.generateJMXObjectName()
				proxy = false
				proxy.class = null
			}
			makeSchedulerThreadDaemon = false
			rmi {
				bindName = null // if null Quartz will generate with QuartzSchedulerResources.getUniqueIdentifier()
				createRegistry = QuartzSchedulerResources.CREATE_REGISTRY_NEVER // 'never'
				export = false
				proxy = false
				registryHost = 'localhost'
				registryPort = 1099
				serverPort = -1 // random
			}
			skipUpdateCheck = true
			threadName = instanceName + '_QuartzSchedulerThread'
			threadsInheritContextClassLoaderOfInitializer = false
			userTransactionURL = null
			wrapJobExecutionInUserTransaction = false
		}
		threadExecutor.class = DefaultThreadExecutor.name
		threadPool.class = SimpleThreadPool.name
		threadPool {
			makeThreadsDaemons = false
			threadCount	= 10
			threadPriority = Thread.NORM_PRIORITY // 5
			threadsInheritContextClassLoaderOfInitializingThread = false
			threadsInheritGroupOfInitializingThread = true
		}
	}
}
