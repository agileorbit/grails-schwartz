import org.quartz.impl.jdbcjobstore.StdJDBCDelegate

quartz {
	jdbcStore = true
	jdbcStoreDataSource = 'clusterDataSource'
	properties {
		jobStore {
			clusterCheckinInterval = 500 // set low to ensure it happens at least once
			driverDelegateClass = StdJDBCDelegate.name
			isClustered = true
		}
		scheduler {
			instanceId = 'node1'
			instanceName = 'ClusterSpec'
		}
	}
}
