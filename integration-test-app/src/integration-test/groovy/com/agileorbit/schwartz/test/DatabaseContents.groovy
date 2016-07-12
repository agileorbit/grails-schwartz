package com.agileorbit.schwartz.test

import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import groovy.transform.CompileStatic

@CompileStatic
class DatabaseContents {

	protected static final Map<String, List<String>> primaryKeys = [
			JOB_DETAILS:         ['SCHED_NAME', 'JOB_NAME', 'JOB_GROUP'],
			TRIGGERS:            ['SCHED_NAME', 'TRIGGER_NAME', 'TRIGGER_GROUP'],
			SIMPLE_TRIGGERS:     ['SCHED_NAME', 'TRIGGER_NAME', 'TRIGGER_GROUP'],
			CRON_TRIGGERS:       ['SCHED_NAME', 'TRIGGER_NAME', 'TRIGGER_GROUP'],
			SIMPROP_TRIGGERS:    ['SCHED_NAME', 'TRIGGER_NAME', 'TRIGGER_GROUP'],
			BLOB_TRIGGERS:       ['SCHED_NAME', 'TRIGGER_NAME', 'TRIGGER_GROUP'],
			CALENDARS:           ['SCHED_NAME', 'CALENDAR_NAME'],
			PAUSED_TRIGGER_GRPS: ['SCHED_NAME', 'TRIGGER_GROUP'],
			FIRED_TRIGGERS:      ['SCHED_NAME', 'ENTRY_ID'],
			SCHEDULER_STATE:     ['SCHED_NAME', 'INSTANCE_NAME'],
			LOCKS:               ['SCHED_NAME', 'LOCK_NAME'],
	]

	protected static final Map<String, List<String>> columns = [
			JOB_DETAILS:      ['DESCRIPTION', 'JOB_CLASS_NAME', 'IS_DURABLE',
			                   'IS_NONCONCURRENT', 'IS_UPDATE_DATA', 'REQUESTS_RECOVERY'],
			TRIGGERS:         ['JOB_NAME', 'JOB_GROUP', 'DESCRIPTION', 'NEXT_FIRE_TIME',
			                   'PREV_FIRE_TIME', 'PRIORITY', 'TRIGGER_STATE',
			                   'TRIGGER_TYPE', 'START_TIME', 'END_TIME',
			                   'CALENDAR_NAME', 'MISFIRE_INSTR'],
			SIMPLE_TRIGGERS:  ['REPEAT_COUNT', 'REPEAT_INTERVAL', 'TIMES_TRIGGERED'],
			CRON_TRIGGERS:    ['CRON_EXPRESSION', 'TIME_ZONE_ID'],
			SIMPROP_TRIGGERS: ['STR_PROP_1', 'STR_PROP_2', 'STR_PROP_3', 'INT_PROP_1',
			                   'INT_PROP_2', 'LONG_PROP_1', 'LONG_PROP_2',
			                   'DEC_PROP_1', 'DEC_PROP_2', 'BOOL_PROP_1', 'BOOL_PROP_2'],
			BLOB_TRIGGERS: [],
			CALENDARS: [],
			PAUSED_TRIGGER_GRPS: [],
			FIRED_TRIGGERS:   ['TRIGGER_NAME', 'TRIGGER_GROUP', 'INSTANCE_NAME',
			                   'FIRED_TIME', 'SCHED_TIME', 'PRIORITY', 'STATE',
			                   'JOB_NAME', 'JOB_GROUP', 'IS_NONCONCURRENT', 'REQUESTS_RECOVERY'],
			SCHEDULER_STATE: ['LAST_CHECKIN_TIME', 'CHECKIN_INTERVAL'],
			LOCKS: []
	]

	final Map<String, List<Row>> data = [:]
	final Collection<String> tablesWithoutData = []

	DatabaseContents(Sql sql, Collection<String> tableNames) {
		for (table in tableNames) {
			List<GroovyRowResult> rows = sql.rows('select * from QRTZ_' + table)
			if (rows) {
				data[table] = rows.collect { new Row(table, it) }
			}
			else {
				tablesWithoutData << table
			}
		}
	}

	boolean isEmpty() {
		!data
	}

	String toString() {
		StringBuilder sb = new StringBuilder()
		for (name in data.keySet().toSorted()) {
			sb << '\nData for ' << name << ':'
			appendRows data[name], sb
		}
		sb.toString()
	}

	String diff(DatabaseContents previous) {

		def contentsSubsetToString = { Collection<String> keys, DatabaseContents contents ->
			keys.collect { key -> key + ': ' + contents.data[key] }.join('\n') << '\n'
		}

		StringBuilder delta = new StringBuilder()

		Map<String, List<Row>> previousData = previous.data
		Collection<String> commonTables = previousData.keySet().intersect(data.keySet())

		Collection<String> notInCommon = data.keySet() - commonTables
		if (notInCommon) delta <<
				'New tables with results: ' <<
				contentsSubsetToString(notInCommon, this)

		notInCommon = previousData.keySet() - commonTables
		if (notInCommon) delta <<
				'Old tables no longer in current results: ' <<
				contentsSubsetToString(notInCommon, previous)

		for (tableName in commonTables) {

			List<Row> pksMatch = data[tableName].intersect(previousData[tableName])

			List<Row> newRows = data[tableName] - pksMatch
			if (newRows) {
				delta << 'New results in table ' << tableName << ':'
				appendRows(newRows, delta)
				delta << '\n'
			}
			List<Row> deletedRows = previousData[tableName] - pksMatch
			if (deletedRows) {
				delta << 'Deleted results from table ' << tableName << ':'
				appendRows(deletedRows, delta)
				delta << '\n'
			}

			def rowDeltas = []
			List<Row> currentSorted = pksMatch.sort()
			List<Row> previousSorted = (previousData[tableName].intersect(data[tableName])).sort()
			for (int r = 0; r < currentSorted.size(); r++) {
				Row row = currentSorted[r]
				Row prev =  previousSorted[r]
				if (row.values == prev.values) continue

				List<String> columnNames = columns[tableName]
				for (int c = 0; c < columnNames.size(); c++) {
					if (row.values[c] != prev.values[c]) {
						rowDeltas << 'Table ' + tableName + ', PK ' +
							primaryKeys[tableName].join('/') + ' Current "' +
							row.values[c] + '", previous: "' + prev.values[c] + '"'
					}
				}
			}

			if (rowDeltas) {
				appendRows rowDeltas, delta
			}
		}

		delta.toString()
	}

	protected void appendRows(Iterable rows, StringBuilder sb) {
		sb << '\n\t' << rows.join('\n\t')
	}
}

@CompileStatic
class Row implements Comparable<Row> {
	final String tableName
	final List<String> pkValues
	final List values

	Row(String name, GroovyRowResult result) {
		tableName = name
		pkValues = DatabaseContents.primaryKeys[tableName].collect { (String) result[it] }
		values = DatabaseContents.columns[tableName].collect { result[it] }
	}

	boolean equals(other) {
		if (other instanceof Row) {
			tableName == other.tableName && pkValues == other.pkValues
		}
	}

	int hashCode() {
		int constant = 37
		int total = 17

		total = total * constant + tableName.hashCode()
		for (String pkValue in pkValues) {
			total = total * constant + pkValue.hashCode()
		}

		total
	}

	String toString() {
		def map = [:]
		List names = DatabaseContents.primaryKeys[tableName]
		for (int i = 0; i < names.size(); i++) {
			map[names[i]] = pkValues[i]
		}
		names = DatabaseContents.columns[tableName]
		for (int i = 0; i < names.size(); i++) {
			map[names[i]] = values[i]
		}
		map
	}

	int compareTo(Row other) {
		tableName == other.tableName && pkValues == other.pkValues ? 0 : 1
	}
}
