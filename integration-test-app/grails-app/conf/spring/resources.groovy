import org.apache.tomcat.jdbc.pool.DataSource

beans = {
	clusterDataSource(DataSource) {
		driverClassName = 'org.h2.Driver'
		password = ''
		url = 'jdbc:h2:mem:clusterDb;MVCC=TRUE;LOCK_TIMEOUT=10000;DB_CLOSE_ON_EXIT=FALSE'
		username = 'sa'
	}
}
