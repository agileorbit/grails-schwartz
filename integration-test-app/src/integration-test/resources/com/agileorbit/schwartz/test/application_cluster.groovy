import org.quartz.impl.jdbcjobstore.StdJDBCDelegate

quartz {
	jdbcStore = true
	jdbcStoreDataSource = 'clusterDataSource'
	properties {
		jobStore {
			acquireTriggersWithinLock = true
			clusterCheckinInterval = 5000
			dontSetAutoCommitFalse = true
			dontSetNonManagedTXConnectionAutoCommitFalse = true
			driverDelegateClass = StdJDBCDelegate.name
			isClustered = true
			maxMisfiresToHandleAtATime = 2
			misfireThreshold = 50000
			selectWithLockSQL = 'select * from {0}LOCKS where SCHED_NAME = {1} and LOCK_NAME = ? for update'
			tablePrefix = 'QUARTZ_'
			txIsolationLevelReadCommitted = true
			txIsolationLevelSerializable = true
			useProperties = true
		}
		scheduler {
			instanceId = 'jdbc1'
			instanceName = 'UsingJDBC'
		}
	}
}
