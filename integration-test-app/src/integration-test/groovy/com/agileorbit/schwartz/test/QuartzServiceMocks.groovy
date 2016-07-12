package com.agileorbit.schwartz.test

import com.agileorbit.schwartz.QuartzService
import com.agileorbit.schwartz.SchwartzSchedulerFactoryBean
import grails.config.Config
import grails.core.GrailsApplication
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.config.NavigableMap
import org.grails.config.NavigableMapPropertySource
import org.grails.config.PropertySourcesConfig
import org.quartz.Scheduler
import org.quartz.core.QuartzScheduler
import org.quartz.core.QuartzSchedulerResources
import org.quartz.impl.StdScheduler
import org.quartz.spi.JobStore
import org.quartz.spi.ThreadExecutor
import org.springframework.context.ApplicationContext
import org.springframework.core.env.MutablePropertySources
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionDefinition
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.SimpleTransactionStatus

import javax.sql.DataSource
import java.lang.reflect.Method
import java.lang.reflect.Proxy

@CompileStatic
class QuartzServiceMocks {

	private boolean initialized

	QuartzService service

	Config config
	DataSource dataSource
	GrailsApplication grailsApplication
	JobStore jobStore
	Closure jobStoreInvocationHandler
	QuartzSchedulerResources resources
	Scheduler scheduler
	PlatformTransactionManager transactionManager

	QuartzService getService() {
		if (!initialized) initialize()
		service
	}

	protected void initialize() {
		if (!service) service = new QuartzService()

		if (dataSource) service['dataSource'] = dataSource

		if (!config) {
			MutablePropertySources mps = new MutablePropertySources()
			mps.addLast new NavigableMapPropertySource('new empty Map', new NavigableMap())
			config = new PropertySourcesConfig(mps)
		}

		if (!grailsApplication) {
			ApplicationContext ctx = [getBean: { String name, Class type ->
				if (type == SchwartzSchedulerFactoryBean) {
					SchwartzSchedulerFactoryBean factoryBean = newSchwartzSchedulerFactoryBean()
					factoryBean.finalQuartzProperties = [:] as Properties
					factoryBean
				}
			}] as ApplicationContext
			grailsApplication = [getConfig: { -> config }, getMainContext: { -> ctx }] as GrailsApplication
		}
		service['grailsApplication'] = grailsApplication

		if (!transactionManager) {
			transactionManager = new PlatformTransactionManager() {
				void commit(TransactionStatus status) {}
				void rollback(TransactionStatus status) {}
				TransactionStatus getTransaction(TransactionDefinition definition) {
					new SimpleTransactionStatus()
				}
			}
		}
		service.transactionManager = transactionManager

		if (!jobStore) {
			if (jobStoreInvocationHandler) {
				jobStore = (JobStore) Proxy.newProxyInstance(
						JobStore.classLoader,
						[JobStore] as Class[]) { proxy, Method method, Object[] args ->

					int maxParams = jobStoreInvocationHandler.maximumNumberOfParameters
					if (maxParams == 2) {
						jobStoreInvocationHandler.call method, args
					}
					else if (maxParams == 3) {
						jobStoreInvocationHandler.call proxy, method, args
					}
					else {
						// TODO
					}
				}
			}
		}

		if (!scheduler) {
			resources = new QuartzSchedulerResources(threadExecutor: new ThreadExecutor() {
				void execute(Thread thread) {}
				void initialize() {}
			}, threadName: 'thread', jobStore: jobStore)

			scheduler = new StdScheduler(new QuartzScheduler(resources, 0, 0))
		}
		service['quartzScheduler'] = scheduler

		initialized = true
	}

	@CompileDynamic
	protected SchwartzSchedulerFactoryBean newSchwartzSchedulerFactoryBean() {
		new SchwartzSchedulerFactoryBean()
	}
}
