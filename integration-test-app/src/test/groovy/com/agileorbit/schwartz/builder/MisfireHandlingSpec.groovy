package com.agileorbit.schwartz.builder

import spock.lang.Specification

import static com.agileorbit.schwartz.builder.BuilderType.calendar
import static com.agileorbit.schwartz.builder.BuilderType.cron
import static com.agileorbit.schwartz.builder.BuilderType.daily
import static com.agileorbit.schwartz.builder.BuilderType.none
import static com.agileorbit.schwartz.builder.BuilderType.simple
import static com.agileorbit.schwartz.builder.MisfireHandling.DoNothing
import static com.agileorbit.schwartz.builder.MisfireHandling.FireAndProceed
import static com.agileorbit.schwartz.builder.MisfireHandling.FireNow
import static com.agileorbit.schwartz.builder.MisfireHandling.IgnoreMisfires
import static com.agileorbit.schwartz.builder.MisfireHandling.NextWithExistingCount
import static com.agileorbit.schwartz.builder.MisfireHandling.NextWithRemainingCount
import static com.agileorbit.schwartz.builder.MisfireHandling.NowWithExistingCount
import static com.agileorbit.schwartz.builder.MisfireHandling.NowWithRemainingCount

class MisfireHandlingSpec extends Specification {

	void testSupports() {
		when:
		def builderTypes = BuilderType.values() as List
		builderTypes.remove none
		def combinations = GroovyCollections.combinations(MisfireHandling.values(), builderTypes)

		then:
		combinations.size() == 32

		assertSupports DoNothing, calendar, combinations
		assertSupports DoNothing, cron, combinations
		assertSupports DoNothing, daily, combinations

		assertSupports FireAndProceed, calendar, combinations
		assertSupports FireAndProceed, cron, combinations
		assertSupports FireAndProceed, daily, combinations

		assertSupports FireNow, simple, combinations

		assertSupports IgnoreMisfires, calendar, combinations
		assertSupports IgnoreMisfires, cron, combinations
		assertSupports IgnoreMisfires, daily, combinations
		assertSupports IgnoreMisfires, simple, combinations

		assertSupports NextWithExistingCount, simple, combinations

		assertSupports NextWithRemainingCount, simple, combinations

		assertSupports NowWithExistingCount, simple, combinations

		assertSupports NowWithRemainingCount, simple, combinations

		assertSupportsFalse combinations
	}

	private boolean assertSupports(MisfireHandling misfireHandling, BuilderType type, List combinations) {
		assert misfireHandling.supports(type)
		assert combinations.remove([misfireHandling, type])
		true
	}

	private boolean assertSupportsFalse(List<List> combinations) {
		for (List pair in combinations) {
			MisfireHandling misfireHandling = pair[0]
			BuilderType type = pair[1]
			assert !misfireHandling.supports(type)
		}
		true
	}
}
