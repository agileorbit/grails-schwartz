/* Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.agileorbit.schwartz.util.jdbc

import com.agileorbit.schwartz.QuartzService
import groovy.transform.CompileStatic

/**
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
abstract class AbstractGenerator {

	protected final QuartzService quartzService

	protected AbstractGenerator(QuartzService quartzService) {
		this.quartzService = quartzService
	}

	protected String tableNamePrefix() {
		quartzService.tableNamePrefix()
	}

	protected static final List<TableData> data = [

		new TableData(
			name: 'JOB_DETAILS',
			pk: [new ColumnData('SCHED_NAME', 120),
			     new ColumnData('JOB_NAME',   200),
			     new ColumnData('JOB_GROUP',  200)],
			columns: [new ColumnData('DESCRIPTION',       Type.STRING,  true,  250),
			          new ColumnData('JOB_CLASS_NAME',    Type.STRING,  false, 250),
			          new ColumnData('IS_DURABLE',        Type.BOOLEAN, false),
			          new ColumnData('IS_NONCONCURRENT',  Type.BOOLEAN, false),
			          new ColumnData('IS_UPDATE_DATA',    Type.BOOLEAN, false),
			          new ColumnData('REQUESTS_RECOVERY', Type.BOOLEAN, false),
			          new ColumnData('JOB_DATA',          Type.BLOB,    true)],
			indexes: [new IndexData('idx_qrtz_j_req_recovery', 'SCHED_NAME', 'REQUESTS_RECOVERY'),
			          new IndexData('idx_qrtz_j_grp',          'SCHED_NAME', 'JOB_GROUP')]),

		new TableData(
			name: 'TRIGGERS', fk: 'JOB_DETAILS',
			pk: [new ColumnData('SCHED_NAME',    120),
			     new ColumnData('TRIGGER_NAME',  200),
			     new ColumnData('TRIGGER_GROUP', 200)],
			columns: [new ColumnData('JOB_NAME',       Type.STRING,   false,  200),
			          new ColumnData('JOB_GROUP',      Type.STRING,   false,  200),
			          new ColumnData('DESCRIPTION',    Type.STRING,    true,  250),
			          new ColumnData('NEXT_FIRE_TIME', Type.BIGINT,    true),
			          new ColumnData('PREV_FIRE_TIME', Type.BIGINT,    true),
			          new ColumnData('PRIORITY',       Type.INTEGER,   true),
			          new ColumnData('TRIGGER_STATE',  Type.STRING,   false,   16),
			          new ColumnData('TRIGGER_TYPE',   Type.STRING,   false,    8),
			          new ColumnData('START_TIME',     Type.BIGINT,   false),
			          new ColumnData('END_TIME',       Type.BIGINT,    true),
			          new ColumnData('CALENDAR_NAME',  Type.STRING,    true,  200),
			          new ColumnData('MISFIRE_INSTR',  Type.SMALLINT,  true),
			          new ColumnData('JOB_DATA',       Type.BLOB,      true)],
			indexes: [new IndexData('idx_qrtz_t_j',
					'SCHED_NAME', 'JOB_NAME', 'JOB_GROUP'),
			          new IndexData('idx_qrtz_t_jg',
				          'SCHED_NAME', 'JOB_GROUP'),
			          new IndexData('idx_qrtz_t_c',
				          'SCHED_NAME', 'CALENDAR_NAME'),
			          new IndexData('idx_qrtz_t_g',
				          'SCHED_NAME', 'TRIGGER_GROUP'),
			          new IndexData('idx_qrtz_t_state',
				          'SCHED_NAME', 'TRIGGER_STATE'),
			          new IndexData('idx_qrtz_t_n_state',
				          'SCHED_NAME', 'TRIGGER_NAME', 'TRIGGER_GROUP', 'TRIGGER_STATE'),
			          new IndexData('idx_qrtz_t_n_g_state',
				          'SCHED_NAME', 'TRIGGER_GROUP', 'TRIGGER_STATE'),
			          new IndexData('idx_qrtz_t_next_fire_time',
				          'SCHED_NAME', 'NEXT_FIRE_TIME'),
			          new IndexData('idx_qrtz_t_nft_st',
				          'SCHED_NAME', 'TRIGGER_STATE', 'NEXT_FIRE_TIME'),
			          new IndexData('idx_qrtz_t_nft_misfire',
				          'SCHED_NAME', 'MISFIRE_INSTR', 'NEXT_FIRE_TIME'),
			          new IndexData('idx_qrtz_t_nft_st_misfire',
				          'SCHED_NAME', 'MISFIRE_INSTR', 'NEXT_FIRE_TIME', 'TRIGGER_STATE'),
			          new IndexData('idx_qrtz_t_nft_st_misfire_grp',
				          'SCHED_NAME', 'MISFIRE_INSTR', 'NEXT_FIRE_TIME', 'TRIGGER_GROUP', 'TRIGGER_STATE')]),

		new TableData(
			name: 'SIMPLE_TRIGGERS', fk: 'TRIGGERS',
			pk: [new ColumnData('SCHED_NAME',    120),
			     new ColumnData('TRIGGER_NAME',  200),
			     new ColumnData('TRIGGER_GROUP', 200)],
			columns: [new ColumnData('REPEAT_COUNT',    Type.BIGINT, false),
			          new ColumnData('REPEAT_INTERVAL', Type.BIGINT, false),
			          new ColumnData('TIMES_TRIGGERED', Type.BIGINT, false)]),

		new TableData(
			name: 'CRON_TRIGGERS', fk: 'TRIGGERS',
			pk: [new ColumnData('SCHED_NAME',    120),
			     new ColumnData('TRIGGER_NAME',  200),
			     new ColumnData('TRIGGER_GROUP', 200)],
			columns: [new ColumnData('CRON_EXPRESSION', Type.STRING, false, 120),
			          new ColumnData('TIME_ZONE_ID',    Type.STRING,  true,  80)]),

		new TableData(
			name: 'SIMPROP_TRIGGERS', fk: 'TRIGGERS',
			pk: [new ColumnData('SCHED_NAME',    120),
			     new ColumnData('TRIGGER_NAME',  200),
			     new ColumnData('TRIGGER_GROUP', 200)],
			columns: [new ColumnData('STR_PROP_1',  Type.STRING,  true, 512),
			          new ColumnData('STR_PROP_2',  Type.STRING,  true, 512),
			          new ColumnData('STR_PROP_3',  Type.STRING,  true, 512),
			          new ColumnData('INT_PROP_1',  Type.INTEGER, true),
			          new ColumnData('INT_PROP_2',  Type.INTEGER, true),
			          new ColumnData('LONG_PROP_1', Type.BIGINT,  true),
			          new ColumnData('LONG_PROP_2', Type.BIGINT,  true),
			          new ColumnData('DEC_PROP_1',                true, 13, 2),
			          new ColumnData('DEC_PROP_2',                true, 13, 2),
			          new ColumnData('BOOL_PROP_1', Type.BOOLEAN, true),
			          new ColumnData('BOOL_PROP_2', Type.BOOLEAN, true)]),

		new TableData(
			name: 'BLOB_TRIGGERS', fk: 'TRIGGERS',
			pk: [new ColumnData('SCHED_NAME',    120),
			     new ColumnData('TRIGGER_NAME',  200),
			     new ColumnData('TRIGGER_GROUP', 200)],
			columns: [new ColumnData('BLOB_DATA', Type.BLOB, true)]),

		new TableData(
			name: 'CALENDARS',
			pk: [new ColumnData('SCHED_NAME',    120),
			     new ColumnData('CALENDAR_NAME', 200)],
			columns: [new ColumnData('CALENDAR', Type.BLOB, false)]),

		new TableData(
			name: 'PAUSED_TRIGGER_GRPS',
			pk: [new ColumnData('SCHED_NAME',    120),
			     new ColumnData('TRIGGER_GROUP', 200)]),

		new TableData(
			name: 'FIRED_TRIGGERS',
			pk: [new ColumnData('SCHED_NAME', 120),
			     new ColumnData('ENTRY_ID',    95)],
			columns: [new ColumnData('TRIGGER_NAME',      Type.STRING,  false, 200),
			          new ColumnData('TRIGGER_GROUP',     Type.STRING,  false, 200),
			          new ColumnData('INSTANCE_NAME',     Type.STRING,  false, 200),
			          new ColumnData('FIRED_TIME',        Type.BIGINT,  false),
			          new ColumnData('SCHED_TIME',        Type.BIGINT,  false),
			          new ColumnData('PRIORITY',          Type.INTEGER, false),
			          new ColumnData('STATE',             Type.STRING,  false,  16),
			          new ColumnData('JOB_NAME',          Type.STRING,   true, 200),
			          new ColumnData('JOB_GROUP',         Type.STRING,   true, 200),
			          new ColumnData('IS_NONCONCURRENT',  Type.BOOLEAN,  true),
			          new ColumnData('REQUESTS_RECOVERY', Type.BOOLEAN,  true)],
			indexes: [new IndexData('idx_qrtz_ft_trig_inst_name',
					'SCHED_NAME', 'INSTANCE_NAME'),
			          new IndexData('idx_qrtz_ft_inst_job_req_rcvry',
				          'SCHED_NAME', 'INSTANCE_NAME', 'REQUESTS_RECOVERY'),
			          new IndexData('idx_qrtz_ft_j_g',
				          'SCHED_NAME', 'JOB_NAME', 'JOB_GROUP'),
			          new IndexData('idx_qrtz_ft_jg',
				          'SCHED_NAME', 'JOB_GROUP'),
			          new IndexData('idx_qrtz_ft_t_g',
				          'SCHED_NAME', 'TRIGGER_NAME', 'TRIGGER_GROUP'),
			          new IndexData('idx_qrtz_ft_tg',
				          'SCHED_NAME', 'TRIGGER_GROUP')]),

		new TableData(
			name: 'SCHEDULER_STATE',
			pk: [new ColumnData('SCHED_NAME',    120),
			     new ColumnData('INSTANCE_NAME', 200)],
			columns: [new ColumnData('LAST_CHECKIN_TIME', Type.BIGINT, false),
			          new ColumnData('CHECKIN_INTERVAL',  Type.BIGINT, false)]),

		new TableData(
			name: 'LOCKS',
			pk: [new ColumnData('SCHED_NAME', 120),
			     new ColumnData('LOCK_NAME',   40)])
	]
}
