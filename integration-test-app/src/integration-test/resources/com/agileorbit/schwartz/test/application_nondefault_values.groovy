import org.quartz.impl.DefaultThreadExecutor
import org.quartz.simpl.HostnameInstanceIdGenerator
import org.quartz.simpl.SimpleThreadPool

quartz {
	properties {
		scheduler {
			dbFailureRetryInterval = 123
			idleWaitTime = 1234
			instanceId = 'foo'
			instanceIdGenerator.class = HostnameInstanceIdGenerator.name
			instanceName = '_testing_'
			interruptJobsOnShutdown = true
			interruptJobsOnShutdownWithWait = true
			jmx.export = true
			makeSchedulerThreadDaemon = true
			skipUpdateCheck = false
			threadName = 'FredThread'
			threadsInheritContextClassLoaderOfInitializer = true
			wrapJobExecutionInUserTransaction = true
		}
		threadExecutor.class = DefaultThreadExecutor.name
		threadPool.class = SimpleThreadPool.name
		threadPool {
			threadCount	= 2
			threadPriority = 2
		}
	}
}
