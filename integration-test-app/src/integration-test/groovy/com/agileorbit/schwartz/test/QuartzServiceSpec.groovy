package com.agileorbit.schwartz.test

import com.agileorbit.schwartz.QuartzService
import com.agileorbit.schwartz.util.JobSnapshot
import com.agileorbit.schwartz.util.SchedulerSnapshot
import grails.config.Config
import org.quartz.CronTrigger
import org.quartz.Scheduler
import org.quartz.SchedulerMetaData
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import org.quartz.impl.matchers.GroupMatcher

import java.lang.reflect.Method

import static org.quartz.TriggerKey.triggerKey

class QuartzServiceSpec extends AbstractQuartzSpec {

	private QuartzServiceMocks mocks

	Scheduler quartzScheduler
	SimpleJobService simpleJobService
	SimpleStatefulJobService simpleStatefulJobService

	void setup() {
		mocks = new QuartzServiceMocks(dataSource: clusterDataSource)
	}

	void 'purgeTables must be enabled in the config when called from init()'() {
		given:
		String name = 'scheduler1'
		String instanceName = 'instance1'
		String jobName = 'job1'
		String jobGroup = 'jgroup1'
		String jobClassName = 'classname'
		String triggerGroup = 'tgroup1'
		String triggerState = 'triggerstate1'
		boolean durable = true
		boolean nonconcurrent = true
		boolean updateData = true
		boolean recovery = true

		expect:
		!totalDbRowCount

		when:
		sql.executeInsert INSERT_JOB_DETAIL, [name, jobName, jobGroup, jobClassName,
		                                      durable, nonconcurrent, updateData, recovery]

		sql.executeInsert INSERT_TRIGGER, [name, 'trigger1', triggerGroup, jobName,
		                                   jobGroup, triggerState, 'type1', 1]

		sql.executeInsert INSERT_CRON_TRIGGER, [name, 'trigger1', triggerGroup, 'cron1']

		sql.executeInsert INSERT_TRIGGER, [name, 'trigger2', triggerGroup, jobName,
		                                   jobGroup, triggerState, 'type2', 2]

		sql.executeInsert INSERT_SIMPLE_TRIGGER, [name, 'trigger2', triggerGroup, 1, 2, 3]

		sql.executeInsert INSERT_TRIGGER, [name, 'trigger3', triggerGroup, jobName,
		                                   jobGroup, triggerState, 'type3', 3]

		sql.executeInsert INSERT_SIMPROP, [name, 'trigger3', triggerGroup]

		sql.executeInsert INSERT_BLOB_TRIGGER, [name, 'trigger4', triggerGroup]

		sql.executeInsert INSERT_CALENDAR, [name, 'calendar1', 'calendar1'.bytes]

		sql.executeInsert INSERT_FIRED, [name, 'entry1', 'trigger1', triggerGroup,
		                                 instanceName, 1, 2, 'state1', jobName,
		                                 jobGroup, 2]

		sql.executeInsert INSERT_LOCK, [name, 'lock1']

		sql.executeInsert INSERT_PAUSED_TRIGGER_GROUP, [name, triggerGroup]

		sql.executeInsert INSERT_SCHEDULER_STATE, [name, instanceName, 1, 2]

		then:
		13 == totalDbRowCount

		when:
		boolean persistent = false
		int invokeCount = 0
		mocks.jobStoreInvocationHandler = { Method method, Object[] args ->
			if ('supportsPersistence' == method.name) {
				invokeCount++
				persistent
			}
			else if ('getJobKeys' == method.name) {
				[] as Set
			}
		}

		QuartzService service = mocks.service
		Config config = mocks.config

		config.quartz.pluginEnabled = true
		config.quartz.autoStartup = false

		service.init()

		then:
		!service.quartzScheduler.started
		!invokeCount
		13 == totalDbRowCount

		when:
		config.quartz.purgeQuartzTablesOnStartup = true
		service.purgeTables()

		then:
		!service.quartzScheduler.started
		1 == invokeCount
		13 == totalDbRowCount

		when:
		persistent = true
		service.purgeTables()

		then:
		!service.quartzScheduler.started
		2 == invokeCount
		!totalDbRowCount
	}

	void 'clearData must be enabled in the config when called from init()'() {
		given:
		int invokeCount = 0

		Scheduler scheduler = [clear: { -> invokeCount++ }, getJobKeys: { GroupMatcher gm -> [] as Set }] as Scheduler
		mocks.scheduler = scheduler
		QuartzService service = mocks.service
		Config config = mocks.config

		config.quartz.pluginEnabled = true
		config.quartz.autoStartup = false

		when:
		service.init()

		then:
		!invokeCount

		when:
		config.quartz.clearQuartzDataOnStartup = true
		service.clearData()

		then:
		1 == invokeCount
	}

	void 'pluginEnabled and autoStartup'() {
		given:
		int invokeCount = 0
		Scheduler scheduler = [start: { -> invokeCount++ },
		                       getMetaData: { -> new SchedulerMetaData(
				                       null, null, null, false, false, false, false,
				                       null, 0, null, false, false, null, 0, null)
		                       },
		                       getJobKeys: { GroupMatcher gm -> [] as Set }] as Scheduler
		mocks.scheduler = scheduler
		QuartzService service = mocks.service
		Config config = mocks.config

		config.quartz.pluginEnabled = false
		config.quartz.autoStartup = false

		when:
		service.init()

		then:
		!invokeCount

		when:
		config.quartz.pluginEnabled = true
		service.init()

		then:
		!invokeCount

		when:
		config.quartz.autoStartup = true
		service.init()

		then:
		1 == invokeCount
	}

	void 'check auto-scheduling for Spring beans'() {
		when:
		SchedulerSnapshot snapshot = SchedulerSnapshot.build(quartzScheduler)
		Collection<JobSnapshot> jobs = snapshot.jobs

		then:
		jobs.size() == 2

		when:
		JobSnapshot job = jobs.find { it.jobDetail.key.name == 'SimpleJobService' }

		then:
		job
		job.jobDetail.key == simpleJobService.jobKey
		job.jobDetail.jobClass == simpleJobService.getClass()
		job.jobDetail.description == simpleJobService.description
		job.jobDetail.durable == simpleJobService.durable
		job.jobDetail.requestsRecovery() == simpleJobService.requestsRecovery
		!job.jobDetail.concurrentExectionDisallowed // stateless
		!job.jobDetail.persistJobDataAfterExecution // stateless
		job.triggers.size() == 1

		when:
		Trigger trigger = job.triggers.keySet().first()

		then:
		trigger
		trigger.key == triggerKey('SimpleJobService_simple')
		trigger.misfireInstruction == Trigger.MISFIRE_INSTRUCTION_SMART_POLICY // default
		trigger.priority == Trigger.DEFAULT_PRIORITY
		trigger instanceof SimpleTrigger
		((SimpleTrigger)trigger).repeatCount == -1 // forever, the default
		((SimpleTrigger)trigger).repeatInterval == 1000

		when:
		job = jobs.find { it.jobDetail.key.name == 'SimpleStatefulJobService' }

		then:
		job
		job.jobDetail.key == simpleStatefulJobService.jobKey
		job.jobDetail.jobClass == simpleStatefulJobService.getClass()
		job.jobDetail.description == simpleStatefulJobService.description
		job.jobDetail.durable == simpleStatefulJobService.durable
		job.jobDetail.requestsRecovery() == simpleStatefulJobService.requestsRecovery
		job.jobDetail.concurrentExectionDisallowed // stateful
		job.jobDetail.persistJobDataAfterExecution // stateful
		job.triggers.size() == 3

		when:
		Map<String, Trigger> statefulTriggersByName = job.triggers.keySet()
				.collectEntries { new MapEntry(it.key.name, it) }
		trigger = statefulTriggersByName.SimpleStatefulJobService_cron

		then:
		trigger
		trigger.key == triggerKey('SimpleStatefulJobService_cron')
		trigger.misfireInstruction == Trigger.MISFIRE_INSTRUCTION_SMART_POLICY // default
		trigger.priority == Trigger.DEFAULT_PRIORITY
		trigger instanceof CronTrigger
		((CronTrigger)trigger).cronExpression == '0 0 * * * ?' // -1 // forever, the default

		when:
		trigger = statefulTriggersByName.SimpleStatefulJobService_simple

		then:
		trigger
		trigger.key == triggerKey('SimpleStatefulJobService_simple')
		trigger.misfireInstruction == Trigger.MISFIRE_INSTRUCTION_SMART_POLICY // default
		trigger.priority == Trigger.DEFAULT_PRIORITY
		trigger instanceof SimpleTrigger
		((SimpleTrigger)trigger).repeatCount == -1 // forever, the default
		((SimpleTrigger)trigger).repeatInterval == 1000

		when:
		trigger = statefulTriggersByName.SimpleStatefulJobService_startat

		then:
		trigger
		trigger.key == triggerKey('SimpleStatefulJobService_startat')
		trigger.misfireInstruction == Trigger.MISFIRE_INSTRUCTION_SMART_POLICY // default
		trigger.priority == Trigger.DEFAULT_PRIORITY
		trigger instanceof SimpleTrigger
		!((SimpleTrigger)trigger).repeatCount
		!((SimpleTrigger)trigger).repeatInterval

		Calendar calendar = Calendar.instance
		calendar.set Calendar.HOUR_OF_DAY, 23
		calendar.set Calendar.MINUTE, 59
		calendar.set Calendar.SECOND, 59
		calendar.set Calendar.MILLISECOND, 0
		trigger.startTime.time == calendar.time.time
		trigger.finalFireTime.time == trigger.startTime.time
	}

	private int getTotalDbRowCount() {
		(int) tableNames.sum { String name ->
			sql.firstRow('select count(*) count from QRTZ_' + name).count
		}
	}

	private static final String INSERT_BLOB_TRIGGER = '''\
		INSERT INTO qrtz_blob_triggers
		       (sched_name, trigger_name, trigger_group)
		VALUES (?, ?, ?)'''

	private static final String INSERT_CALENDAR = '''\
		INSERT INTO qrtz_calendars (sched_name, calendar_name, calendar)
		VALUES (?, ?, ?)'''

	private static final String INSERT_CRON_TRIGGER = '''\
		INSERT INTO qrtz_cron_triggers
		       (sched_name, trigger_name, trigger_group, cron_expression)
		VALUES (?, ?, ?, ?)'''

	private static final String INSERT_FIRED = '''\
		INSERT INTO qrtz_fired_triggers
		       (sched_name, entry_id, trigger_name, trigger_group, instance_name,
		       fired_time, sched_time, state, job_name, job_group, priority)
		VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)'''

	private static final String INSERT_JOB_DETAIL = '''\
		INSERT INTO qrtz_job_details
		       (sched_name, job_name, job_group, job_class_name, is_durable,
		       is_nonconcurrent, is_update_data, requests_recovery)
		VALUES (?, ?, ?, ?, ?, ?, ?, ?)'''

	private static final String INSERT_LOCK = '''\
		INSERT INTO qrtz_locks(sched_name, lock_name)
		VALUES (?, ?)'''

	private static final String INSERT_PAUSED_TRIGGER_GROUP ='''\
		INSERT INTO qrtz_paused_trigger_grps (sched_name, trigger_group) VALUES(?, ?)'''

	private static final String INSERT_SCHEDULER_STATE = '''\
		INSERT INTO qrtz_scheduler_state (sched_name, instance_name, last_checkin_time, checkin_interval)
		VALUES(?, ?, ?, ?)'''

	private static final String INSERT_SIMPLE_TRIGGER = '''\
		INSERT INTO qrtz_simple_triggers (sched_name, trigger_name, trigger_group, repeat_count,
		                                  repeat_interval, times_triggered)
		VALUES(?, ?, ?, ?, ?, ?)'''

	private static final String INSERT_SIMPROP = '''\
		INSERT INTO qrtz_simprop_triggers (sched_name, trigger_name, trigger_group) VALUES(?, ?, ?)'''

	private static final String INSERT_TRIGGER = '''\
		INSERT INTO qrtz_triggers (sched_name, trigger_name, trigger_group, job_name, job_group,
		                           trigger_state, trigger_type, start_time)
		VALUES(?, ?, ?, ?, ?, ?, ?, ?)'''
}
