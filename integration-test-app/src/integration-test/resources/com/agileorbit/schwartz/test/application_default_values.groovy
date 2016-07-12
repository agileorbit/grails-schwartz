import org.quartz.impl.DefaultThreadExecutor
import org.quartz.simpl.SimpleInstanceIdGenerator
import org.quartz.simpl.SimpleThreadPool

quartz {
	properties {
		scheduler {
			dbFailureRetryInterval = 15000
			idleWaitTime = 30000
			instanceId = 'NON_CLUSTERED'
			instanceIdGenerator.class = SimpleInstanceIdGenerator.name
			instanceName = 'QuartzScheduler'
			interruptJobsOnShutdown = false
			interruptJobsOnShutdownWithWait = false
			jmx.export = false
			makeSchedulerThreadDaemon = false
			skipUpdateCheck = true
			threadName = instanceName + '_QuartzSchedulerThread'
			threadsInheritContextClassLoaderOfInitializer = false
			wrapJobExecutionInUserTransaction = false
		}
		threadExecutor.class = DefaultThreadExecutor.name
		threadPool.class = SimpleThreadPool.name
		threadPool {
			threadCount	= 10
			threadPriority = Thread.NORM_PRIORITY
		}
	}
}
