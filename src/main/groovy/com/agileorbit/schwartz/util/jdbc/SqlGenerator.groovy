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
import org.grails.orm.hibernate.SessionFactoryProxy
import org.hibernate.SessionFactory
import org.hibernate.cfg.Configuration
import org.hibernate.cfg.Mappings
import org.hibernate.dialect.Dialect
import org.hibernate.internal.SessionFactoryImpl
import org.hibernate.mapping.Column
import org.hibernate.mapping.ForeignKey
import org.hibernate.mapping.Index
import org.hibernate.mapping.PrimaryKey
import org.hibernate.mapping.SimpleValue
import org.hibernate.mapping.Table
import org.springframework.core.InfrastructureProxy

/**
 * Helper class for CreateJdbcSqlCommand.
 *
 * Creates in-memory Hibernate model objects and returns the SQL generated from
 * those; this is similar to the process used by the schema-export script.
 *
 * @author <a href='mailto:burt@agileorbit.com'>Burt Beckwith</a>
 */
@CompileStatic
class SqlGenerator extends AbstractGenerator {

	final SessionFactory sessionFactory

	SqlGenerator(QuartzService quartzService, SessionFactory sessionFactory) {
		super(quartzService)
		this.sessionFactory = sessionFactory
	}

	/**
	 * Creates in-memory tables, columns, indexes, etc. and generates their SQL.
	 *
	 * @return the SQL
	 */
	String generate() {

		Dialect dialect = findSessionFactoryImpl(sessionFactory).dialect

		Configuration configuration = new Configuration() {
			protected void secondPassCompileForeignKeys(Table table, Set<ForeignKey> done) {
				// not needed and fails because of incomplete configuration
			}
		}

		Mappings mappings = configuration.createMappings()

		String prefix = tableNamePrefix()

		Map<String, Table> tables = [:]

		for (tableData in data) {
			Table table = mappings.addTable(null, null, prefix + tableData.name, null, false)
			tables[table.name] = table
			table.primaryKey = new PrimaryKey(table: table)
			for (ColumnData col in tableData.pk) {
				addVarchar table, col.name, col.length, false, mappings
			}
			table.primaryKey.addColumns table.columnIterator

			for (ColumnData col in tableData.columns) {
				boolean nullable = col.nullable
				switch (col.type) {
					case Type.BIGINT:
					case Type.BLOB:
					case Type.BOOLEAN:
					case Type.INTEGER:
					case Type.SMALLINT: addSimple  table, col.type, col.name, nullable, mappings; break
					case Type.DECIMAL:  addDecimal table, col.name, col.precision, col.scale, nullable, mappings; break
					case Type.STRING:   addVarchar table, col.name, col.length, nullable, mappings; break
				}
			}

			if (tableData.fk) {
				Table otherTable = tables[prefix + tableData.fk]
				ForeignKey fk = table.createForeignKey(null, otherTable.primaryKey.columnIterator.findAll() as List, null)
				fk.referencedTable = otherTable
				fk.alignColumns()
			}

			for (IndexData indexData in tableData.indexes) {
				Index index = new Index(name: indexData.name, table: table)
				index.table.addIndex index
				for (columnName in indexData.columnNames) {
					index.addColumn((Column) index.table.columnIterator.find { ((Column)it).name == columnName })
				}
				assert index.columnSpan == indexData.columnNames.size()
			}
		}

		configuration.generateSchemaCreationScript(dialect).join('\n\n')
	}

	protected SessionFactoryImpl findSessionFactoryImpl(sessionFactory) {
		if (sessionFactory instanceof InfrastructureProxy) {
			return findSessionFactoryImpl(((InfrastructureProxy) sessionFactory).wrappedObject)
		}

		if (sessionFactory instanceof SessionFactoryProxy) {
			return findSessionFactoryImpl(((SessionFactoryProxy) sessionFactory).currentSessionFactory)
		}

		(SessionFactoryImpl) sessionFactory
	}

	protected Column addVarchar(Table table, String name, int length, boolean nullable, Mappings mappings) {
		Column column = addSimple(table, Type.STRING, name, nullable, mappings)
		column.length = length
		column
	}

	protected Column addDecimal(Table table, String name, int precision, int scale, boolean nullable, Mappings mappings) {
		Column column = addSimple(table, Type.DECIMAL, name, nullable, mappings)
		column.precision = precision
		column.scale = scale
		column
	}

	protected Column addSimple(Table table, Type type, String name, boolean nullable, Mappings mappings) {
		SimpleValue value = new SimpleValue(mappings, table)
		switch (type) {
			case Type.STRING:
			case Type.BLOB:
			case Type.BOOLEAN:
			case Type.INTEGER:
				value.typeName = type.name().toLowerCase()
				break
			case Type.BIGINT:
				value.typeName = 'long'
				break
			case Type.SMALLINT:
				value.typeName = 'short'
				break
			case Type.DECIMAL:
				value.typeName = 'big_decimal'
				break
		}
		Column column = new Column(name: name, value: value, nullable: nullable)
		value.addColumn column
		table.addColumn column
		column
	}
}
