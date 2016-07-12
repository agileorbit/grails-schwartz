package com.agileorbit.schwartz.listener

import grails.persistence.support.PersistenceContextInterceptor

class SessionBinderJobListenerSpec extends AbstractListenerSpec {

	private SessionBinderJobListener listener = new SessionBinderJobListener()
	private MockPersistenceContextInterceptor persistenceInterceptor = new MockPersistenceContextInterceptor()

	void 'jobToBeExecuted, no interceptor'() {
		when:
		listener.jobToBeExecuted context

		then:
		!persistenceInterceptor.initCount
		!persistenceInterceptor.flushCount
		!persistenceInterceptor.clearCount
		!persistenceInterceptor.destroyCount
	}

	void 'jobToBeExecuted, with interceptor'() {
		when:
		listener.persistenceInterceptor = persistenceInterceptor
		listener.jobToBeExecuted context

		then:
		1 == persistenceInterceptor.initCount
		!persistenceInterceptor.flushCount
		!persistenceInterceptor.clearCount
		!persistenceInterceptor.destroyCount
	}

	void 'jobExecutionVetoed, no interceptor'() {
		when:
		listener.jobExecutionVetoed context

		then:
		!persistenceInterceptor.initCount
		!persistenceInterceptor.flushCount
		!persistenceInterceptor.clearCount
		!persistenceInterceptor.destroyCount
	}

	void 'jobExecutionVetoed, with interceptor'() {
		when:
		listener.persistenceInterceptor = persistenceInterceptor
		listener.jobExecutionVetoed context

		then:
		!persistenceInterceptor.initCount
		!persistenceInterceptor.flushCount
		!persistenceInterceptor.clearCount
		!persistenceInterceptor.destroyCount
	}

	void 'jobWasExecuted, no interceptor'() {
		when:
		listener.jobWasExecuted context, null

		then:
		!persistenceInterceptor.initCount
		!persistenceInterceptor.flushCount
		!persistenceInterceptor.clearCount
		!persistenceInterceptor.destroyCount
	}

	void 'jobWasExecuted, with interceptor'() {
		when:
		listener.persistenceInterceptor = persistenceInterceptor
		listener.jobWasExecuted context, null

		then:
		!persistenceInterceptor.initCount
		1 == persistenceInterceptor.flushCount
		1 == persistenceInterceptor.clearCount
		1 == persistenceInterceptor.destroyCount
	}

	void 'jobWasExecuted, with interceptor and exception'() {
		when:
		listener.persistenceInterceptor = persistenceInterceptor
		persistenceInterceptor.throwExceptionInFlush = true

		listener.jobWasExecuted context, null

		then:
		!persistenceInterceptor.initCount
		1 == persistenceInterceptor.flushCount
		!persistenceInterceptor.clearCount
		1 == persistenceInterceptor.destroyCount
	}
}

class MockPersistenceContextInterceptor implements PersistenceContextInterceptor {

	boolean throwExceptionInFlush

	int initCount
	int flushCount
	int clearCount
	int destroyCount

	void init() { initCount++ }

	void flush() {
		flushCount++
		if (throwExceptionInFlush) throw new RuntimeException('!')
	}

	void clear() { clearCount++ }

	void destroy() { destroyCount++ }

	void disconnect() {}
	void reconnect() {}
	void setReadOnly() {}
	void setReadWrite() {}
	boolean isOpen() { true }
}
