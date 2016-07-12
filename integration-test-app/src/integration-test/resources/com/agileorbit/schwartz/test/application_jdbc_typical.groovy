quartz {
	jdbcStore = true
	jdbcStoreDataSource = 'clusterDataSource'
	properties {
		scheduler {
			instanceId = 'jdbc10'
			instanceName = 'TypicalJDBC'
		}
	}
}
