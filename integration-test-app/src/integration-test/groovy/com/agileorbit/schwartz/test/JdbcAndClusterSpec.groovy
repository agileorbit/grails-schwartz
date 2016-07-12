package com.agileorbit.schwartz.test

import com.agileorbit.schwartz.QuartzService
import com.agileorbit.schwartz.SchwartzJob
import com.agileorbit.schwartz.StatefulSchwartzJob
import com.agileorbit.schwartz.util.QuartzSchedulerObjects
import com.agileorbit.schwartz.util.Utils
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import org.quartz.JobExecutionException
import org.quartz.Trigger
import org.quartz.TriggerKey
import org.quartz.TriggerListener
import org.quartz.impl.StdScheduler
import org.quartz.impl.calendar.DailyCalendar
import org.quartz.impl.jdbcjobstore.StdJDBCDelegate
import org.quartz.listeners.TriggerListenerSupport
import org.springframework.scheduling.quartz.LocalDataSourceJobStore

import static org.quartz.TimeOfDay.hourAndMinuteAndSecondFromDate
import static org.quartz.Trigger.TriggerState.PAUSED
import static org.quartz.TriggerKey.triggerKey
import static org.quartz.impl.matchers.GroupMatcher.groupEquals
import static org.quartz.impl.matchers.GroupMatcher.triggerGroupEquals

class JdbcAndClusterSpec extends AbstractQuartzSpec {

	private static final List<String> TABLE_NAMES =
			([] + Utils.ALL_TABLE_NAMES - ['FIRED_TRIGGERS', 'LOCKS']).sort()
	private static final String SQL_TRIGGER_STATE =
			'select trigger_name, trigger_group ' +
			'from qrtz_triggers where sched_name=? and trigger_state=?'

	private Map<String, Integer> previousRowCounts = [:]
	private Map<String, Integer> rowCounts = [:]
	private Map<String, Integer> rowCountDeltas = [:]
	private Map<String, Integer> deltasWithValues = [:]

	void 'JDBC JobStore but not clustered'() {
		when:
		for (tableName in TABLE_NAMES) {
			previousRowCounts[tableName] = 0
			rowCounts[tableName] = 0
			rowCountDeltas[tableName] = 0
		}

		QuartzSchedulerObjects quartz = buildScheduler('application_jdbc_typical')

		//	logTheDatabase 'startup'
		countRows()

		then:
		!deltasWithValues

		quartz.scheduler instanceof StdScheduler
		quartz.jdbcJobStore
		quartz.jdbcDelegate instanceof StdJDBCDelegate

		quartz.schedulerInstanceId == 'jdbc10'
		quartz.schedulerName == 'TypicalJDBC'
		quartz.jobStorePersistent
		!quartz.jobStoreClustered
		quartz.jdbcJobStore instanceof LocalDataSourceJobStore

		when:
		quartz.scheduler.addCalendar('daily_calendar',
				new DailyCalendar(1, 1, 1, 1, 3, 3, 3, 3), true, true)

		// logTheDatabase 'after add calendar'
		countRows()

		then:
		deltasWithValues.remove('CALENDARS') == 1
		!deltasWithValues

		when:
		QuartzServiceMocks mocks = new QuartzServiceMocks()
		mocks.scheduler = quartz.scheduler

		QuartzService service = mocks.service

		SchwartzJob job = new StatelessJob()
		job.buildTriggers()
		service.scheduleJob job

		// logTheDatabase 'after StatelessJob'
		countRows()

		then:
		deltasWithValues.remove('JOB_DETAILS') == 1
		deltasWithValues.remove('CRON_TRIGGERS') == 1
		deltasWithValues.remove('SIMPLE_TRIGGERS') == 1
		deltasWithValues.remove('SIMPROP_TRIGGERS') == 2
		deltasWithValues.remove('TRIGGERS') == 4
		!deltasWithValues

		when:
		JobDataMap dataMap = new JobDataMap()
		dataMap.put('b', true)
		dataMap.put('i', 123I)
		dataMap.put('l', 123L)
		dataMap.put('f', 123.4F)
		dataMap.put('d', 1234.23D)
		dataMap.put('s', 'a string value')
		dataMap.put('obj', [1, 2, 5])

		job = new StatefulJdbcJob()
		job.buildTriggers()
		service.scheduleJob job, dataMap

		//logTheDatabase 'after StatefulJdbcJob'
		countRows()

		then:
		deltasWithValues.remove('JOB_DETAILS') == 1
		deltasWithValues.remove('CRON_TRIGGERS') == 1
		deltasWithValues.remove('SIMPLE_TRIGGERS') == 1
		deltasWithValues.remove('SIMPROP_TRIGGERS') == 2
		deltasWithValues.remove('TRIGGERS') == 4
		!deltasWithValues

		when:
		def findPausedTriggers = { ->
			List<TriggerKey> keys = []
			for (row in sql.rows(SQL_TRIGGER_STATE, [quartz.scheduler.schedulerName, PAUSED.toString()])) {
				keys << triggerKey((String)row.TRIGGER_NAME, (String)row.TRIGGER_GROUP)
			}
			keys
		}

		quartz.scheduler.start()

		quartz.scheduler.deleteCalendar 'daily_calendar'

		// logTheDatabase 'after calendar delete'
		countRows()
		List<TriggerKey> paused = findPausedTriggers()

		then:
		deltasWithValues.remove('CALENDARS') == -1
		!deltasWithValues
		!paused

		when:
		quartz.scheduler.pauseJobs groupEquals('LotsaState')

		sleep 200
		// logTheDatabase 'after pause LotsaState'
		countRows('after pause LotsaState')
		paused = findPausedTriggers()

		then:
		// TODO investigate why this is always 4 when the test runs by itself but is 3 occasionally when all tests run
		// paused.size() == 4
		paused.every { it.name.endsWith(':StatefulJdbcJob') }
		!deltasWithValues

		when:
		quartz.scheduler.pauseTriggers triggerGroupEquals('CRON_AND_CALENDAR')

		sleep 100
		// logTheDatabase 'after pause CRON_AND_CALENDAR'
		countRows('after pause CRON_AND_CALENDAR')
		paused = findPausedTriggers()
		paused.size() == 6
		paused.removeAll { it.name.endsWith(':StatefulJdbcJob') }
		paused.size() == 2
		paused.contains triggerKey('calendar_interval:StatelessJob', 'CRON_AND_CALENDAR')
		paused.contains triggerKey('cron_every_second:StatelessJob', 'CRON_AND_CALENDAR')

		then:
		deltasWithValues.remove('PAUSED_TRIGGER_GRPS') == 1
		!deltasWithValues

		when:
		sleep 1000

		quartz.scheduler.resumeJobs groupEquals('LotsaState')

		sleep 100
		// logTheDatabase 'after resume LotsaState'
		countRows('after resume LotsaState')
		paused = findPausedTriggers()

		then:
		paused.size() == 2
		paused.contains triggerKey('calendar_interval:StatelessJob', 'CRON_AND_CALENDAR')
		paused.contains triggerKey('cron_every_second:StatelessJob', 'CRON_AND_CALENDAR')
		!deltasWithValues

		when:
		quartz.scheduler.resumeTriggers triggerGroupEquals('CRON_AND_CALENDAR')

		sleep 100
		// logTheDatabase 'after resume CRON_AND_CALENDAR'
		countRows('after resume CRON_AND_CALENDAR')
		paused = findPausedTriggers()

		then:
		!paused
		deltasWithValues.remove('PAUSED_TRIGGER_GRPS') == -1
		!deltasWithValues

		when:
		sleep 5000

		quartz.shutdown()

		sleep 100
		// logTheDatabase 'after shutdown'
		countRows('after shutdown')

		then:
		!deltasWithValues
	}

	void 'JDBC storage and two clustered instances'() {
		when:
		QuartzSchedulerObjects quartz1 = buildScheduler('application_cluster_node1')

		then:
		quartz1.scheduler instanceof StdScheduler
		quartz1.jdbcJobStore
		quartz1.jdbcDelegate instanceof StdJDBCDelegate

		quartz1.schedulerInstanceId == 'node1'
		quartz1.schedulerName == 'ClusterSpec'
		quartz1.jobStorePersistent
		quartz1.jobStoreClustered
		quartz1.jdbcJobStore instanceof LocalDataSourceJobStore

		when:
		QuartzSchedulerObjects quartz2 = buildScheduler('application_cluster_node2')

		then:
		quartz2.scheduler instanceof StdScheduler
		quartz2.jdbcJobStore
		quartz2.jdbcDelegate instanceof StdJDBCDelegate

		quartz2.schedulerInstanceId == 'node2'
		quartz2.schedulerName == 'ClusterSpec'
		quartz2.jobStorePersistent
		quartz2.jobStoreClustered
		quartz2.jdbcJobStore instanceof LocalDataSourceJobStore

		when:
		QuartzServiceMocks mocks1 = new QuartzServiceMocks()
		mocks1.scheduler = quartz1.scheduler

		QuartzService service1 = mocks1.service

		// schedule on #1, will listen on both

		SchwartzJob job = new StatelessJob()
		job.buildTriggers()
		service1.scheduleJob job

		job = new StatefulJdbcJob()
		job.buildTriggers()
		service1.scheduleJob job, new JobDataMap(a: 1, b: 5)

		QuartzServiceMocks mocks2 = new QuartzServiceMocks()
		mocks2.scheduler = quartz2.scheduler

		/*QuartzService service2 =*/ mocks2.service

		List<FiredTriggerData> firedData = [].asSynchronized()
		TriggerListener triggerListener = new TriggerListenerSupport() {
			String getName() { 'JdbcAndClusterSpec_cluster' }

			void triggerFired(Trigger trigger, JobExecutionContext context) {
				firedData << new FiredTriggerData(trigger, context)
			}
		}
		quartz1.scheduler.listenerManager.addTriggerListener triggerListener
		quartz2.scheduler.listenerManager.addTriggerListener triggerListener

		quartz2.scheduler.start()
		quartz1.scheduler.start()

		sleep 10000

		quartz1.shutdown()
		quartz2.shutdown()

		def firedOn1 = firedData.findAll { it.instanceId == 'node1' }
		def firedOn2 = firedData.findAll { it.instanceId == 'node2' }

		then:
		// check that the work is split between the instances
		firedData.size() > 30
		firedOn1.size() + firedOn2.size() == firedData.size()
		firedOn1
		firedOn2

		when:
		def cronEverySecondStateful1  = firedOn1.findAll { it.key.name == 'cron_every_second:StatefulJdbcJob' }
		def cronEverySecondStateful2  = firedOn2.findAll { it.key.name == 'cron_every_second:StatefulJdbcJob' }
		def cronEverySecondStateless1 = firedOn1.findAll { it.key.name == 'cron_every_second:StatelessJob' }
		def cronEverySecondStateless2 = firedOn2.findAll { it.key.name == 'cron_every_second:StatelessJob' }

		def dailyIntervalStateful1  = firedOn1.findAll { it.key.name == 'daily_interval:StatefulJdbcJob' }
		def dailyIntervalStateful2  = firedOn2.findAll { it.key.name == 'daily_interval:StatefulJdbcJob' }
		def dailyIntervalStateless1 = firedOn1.findAll { it.key.name == 'daily_interval:StatelessJob' }
		def dailyIntervalStateless2 = firedOn2.findAll { it.key.name == 'daily_interval:StatelessJob' }

		def simpleTriggerStateful1  = firedOn1.findAll { it.key.name == 'simple_trigger:StatefulJdbcJob' }
		def simpleTriggerStateful2  = firedOn2.findAll { it.key.name == 'simple_trigger:StatefulJdbcJob' }
		def simpleTriggerStateless1 = firedOn1.findAll { it.key.name == 'simple_trigger:StatelessJob' }
		def simpleTriggerStateless2 = firedOn2.findAll { it.key.name == 'simple_trigger:StatelessJob' }

		then:
		// check that individual triggers run on both instances
		cronEverySecondStateful1
		cronEverySecondStateful2
		cronEverySecondStateless1
		cronEverySecondStateless2

		// Quartz doesn't try to load balance work, so these checks can fail since the total number is small and sometimes all
		// of the triggers for a job fire on one instance; the previous cron checks validate that work is split between the two
//		dailyIntervalStateful1
//		dailyIntervalStateful2
//		dailyIntervalStateless1
//		dailyIntervalStateless2
//
//		simpleTriggerStateful1
//		simpleTriggerStateful2
//		simpleTriggerStateless1
//		simpleTriggerStateless2
	}

	private DatabaseContents previous

	private void countRows(String where = null) {
		for (tableName in TABLE_NAMES) {
			previousRowCounts[tableName] = rowCounts[tableName]
			rowCounts[tableName] = (int)sql.firstRow('select count(*) count from QRTZ_' + tableName).count
			int delta = rowCounts[tableName] - previousRowCounts[tableName]
			rowCountDeltas[tableName] = delta
			if (delta) {
				deltasWithValues[tableName] = delta
			}
			else {
				deltasWithValues.remove tableName
			}
		}
	}

	private void logTheDatabase(String label) {
		def contents = new DatabaseContents(sql, tableNames)
		if (contents.empty) {
			log.debug 'Database state at {}: no data', label
		}
		else {
			log.debug 'Database state at {}:{}', label,
					new DatabaseContents(sql, tableNames)
		}

		if (previous == null) {
			log.debug 'no previous results'
		}
		else {
			log.debug 'Delta\n' + contents.diff(previous)
		}
		previous = contents
	}
}

@CompileStatic
@Slf4j
class StatefulJdbcJob extends StatelessJob implements StatefulSchwartzJob {
	protected int calDelta()    { (int) (super.calData()     / 2) }
	protected int cronDelta()   { (int) (super.cronDelta()   / 2) }
	protected int dailyDelta()  { (int) (super.dailyDelta()  / 2) }
	protected int simpleDelta() { (int) (super.simpleDelta() / 2) }

	String getJobGroup() { 'LotsaState' }
}

@CompileStatic
@Slf4j
class StatelessJob implements SchwartzJob {

	protected int calData()     {  500 }
	protected int cronDelta()   {  750 }
	protected int dailyDelta()  { 1000 }
	protected int simpleDelta() { 1250 }

	String getJobGroup() { 'NoState' }
	boolean getSessionRequired() { false }

	void execute(JobExecutionContext context) throws JobExecutionException {
		log.info 'Executing trigger {} at {} ( {} )',
				context.trigger.key, new Date(),
				System.currentTimeMillis()
	}

	void buildTriggers() {

		String suffix = getClass().simpleName

		triggers << factory('calendar_interval:' + suffix)
				.group('CRON_AND_CALENDAR')
				.intervalInDays(1)
				.startDelay(calData())
				.build()

		Calendar when = Calendar.instance

		when.timeInMillis += (long)cronDelta()

		triggers << factory('cron_every_second:' + suffix)
				.group('CRON_AND_CALENDAR')
				.cronSchedule('* * * * * ?')
				.startDelay(cronDelta())
				.build()

		when.timeInMillis += (long) dailyDelta()

		triggers << factory('daily_interval:' + suffix)
				.group('DAILY_AND_SIMPLE')
				.dailyStart(hourAndMinuteAndSecondFromDate(when.time))
				.intervalInSeconds(1).build()

		triggers << factory('simple_trigger:' + suffix)
				.group('DAILY_AND_SIMPLE')
				.intervalInSeconds(1)
				.startDelay(simpleDelta())
				.build()
	}
}

@CompileStatic
class FiredTriggerData {

	final TriggerKey key
	final long fireTime
	final String schedulerName
	final String instanceId

	FiredTriggerData(Trigger trigger, JobExecutionContext context) {
		key = trigger.key
		fireTime = context.fireTime.time
		schedulerName = context.scheduler.schedulerName
		instanceId = context.scheduler.schedulerInstanceId
	}
}
