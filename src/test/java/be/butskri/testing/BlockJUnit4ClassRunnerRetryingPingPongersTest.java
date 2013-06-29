package be.butskri.testing;

import static junit.framework.Assert.assertEquals;
import static org.fest.assertions.Assertions.assertThat;
import static org.junit.runner.JUnitCore.runClasses;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runner.notification.Failure;
import org.junit.runners.BlockJUnit4ClassRunner;

@RunWith(BlockJUnit4ClassRunner.class)
public class BlockJUnit4ClassRunnerRetryingPingPongersTest {

	@Test
	public void pingPongAnnotationWorksAtMethodLevel() throws Throwable {
		Result result = runClasses(TestClassForTestingTestRunner.class);
		assertResult(result, 7, 4);
	}

	@Test
	public void pingPongAnnotationAlsoWorkAtClassLevel() throws Throwable {
		Result result = runClasses(TestClassForTestingPingPongAnnotationAtClassLevel.class);
		assertResult(result, 7, 2);
	}

	private void assertResult(Result result, int runCount, int failureCount) {
		assertThat(result.getRunCount()).isEqualTo(runCount);
		assertThat(result.getFailureCount()).isEqualTo(failureCount);
		assertFailureHeaders(result.getFailures());
	}

	private void assertFailureHeaders(List<Failure> failures) {
		for (Failure failure : failures) {
			assertThat(failure.getTestHeader()).startsWith("iWillFail");
			assertThat(failure.getTestHeader()).excludes("iWillSucceed");
		}
	}

	@RunWith(BlockJUnit4ClassRunnerRetryingPingPongers.class)
	public static class TestClassForTestingTestRunner {
		private static Object lastRunTest;
		private static Counters counters = new Counters();

		@Before
		public void checkThatTestInstanceIsNotReused() {
			if (lastRunTest == this) {
				throw new RuntimeException("rerunning test that was already run:" + this);
			}
		}

		@After
		public void keepLastRunTestInstance() {
			lastRunTest = this;
		}

		@Test
		public void iWillFailWhenNotAnnotatedWithPingPongBecauseOnlyRetriedOnce() {
			counters.increment("NOT_ANNOTATED_WITH_PINGPONG");
			assertThat(counters.getValue("NOT_ANNOTATED_WITH_PINGPONG")).isEqualTo(2);
		}

		@Test
		@PingPong
		public void iWillSucceedWhenRunSecondTimeBecauseOfPingPongAnnotationWithDefaultMaxNumberOfRetries1() {
			counters.increment("WILL_WORK_SECOND_RUN");
			assertThat(counters.getValue("WILL_WORK_SECOND_RUN")).isEqualTo(2);
		}

		@Test
		@PingPong
		public void iWillFailWhenRunSecondTimeBecauseOfPingPongAnnotationWithDefaultMaxNumberOfRetries1() {
			counters.increment("WILL_FAIL_BECAUSE_NO_THIRD_RUN");
			assertThat(counters.getValue("WILL_FAIL_BECAUSE_NO_THIRD_RUN")).isEqualTo(3);
		}

		@Test
		@PingPong(maxNumberOfRetries = 2)
		public void iWillSucceedWhenRunThirdTimeBecauseOfPingPongAnnotationWithMaxNumberOfRetries2() {
			counters.increment("WILL_WORK_THIRD_RUN");
			assertThat(counters.getValue("WILL_WORK_THIRD_RUN")).isEqualTo(3);
		}

		@Test
		@PingPong(maxNumberOfRetries = 2)
		public void iWillFailBecauseOnlyTwoRetriesWillBeExecutedAndThreeNeeded() {
			counters.increment("WILL_STILL_FAIL_AFTER_2_RETRIES");
			assertThat(counters.getValue("WILL_STILL_FAIL_AFTER_2_RETRIES")).isEqualTo(4);
		}

		@Test
		@PingPong(maxNumberOfRetries = 3)
		public void iWillSucceedAfter3Retries() {
			counters.increment("WILL_SUCCEED_AFTER_3_RETRIES");
			assertThat(counters.getValue("WILL_SUCCEED_AFTER_3_RETRIES")).isEqualTo(4);
		}

		@Test
		@PingPong(maxNumberOfRetries = 3)
		public void iWillFailOnlyAfter3FailedRetries() {
			counters.increment("WILL_STILL_FAIL_AFTER_3_RETRIES");
			assertThat(counters.getValue("WILL_STILL_FAIL_AFTER_3_RETRIES")).isEqualTo(5);
		}

	}

	@PingPong(maxNumberOfRetries = 3)
	@RunWith(BlockJUnit4ClassRunnerRetryingPingPongers.class)
	public static class TestClassForTestingPingPongAnnotationAtClassLevel {
		private static Object lastRunTest;
		private static Counters counters = new Counters();

		@Before
		public void checkThatTestInstanceIsNotReused() {
			if (lastRunTest == this) {
				throw new RuntimeException("rerunning test that was already run:" + this);
			}
		}

		@After
		public void keepLastRunTestInstance() {
			lastRunTest = this;
		}

		@Test
		public void iWillSucceedWhenOnTheFirstRun() {
			counters.increment("WILL_SUCCEED_ON_FIRST_RUN");
			assertEquals(1, counters.getValue("WILL_SUCCEED_ON_FIRST_RUN"));
		}

		@Test
		public void iWillSucceedOnMySecondRunBecauseOnly1RetryNecessaryOfMaxNumberOf3RetriesDefinedAtClassLevel() {
			counters.increment("WILL_SUCCEED_ON_SECOND_RUN");
			assertEquals(2, counters.getValue("WILL_SUCCEED_ON_SECOND_RUN"));
		}

		@Test
		public void iWillSucceedOnMyThirdRunBecauseOnly2RetriesNecessaryOfMaxNumberOf3RetriesDefinedAtClassLevel() {
			counters.increment("WILL_SUCCEED_ON_THIRD_RUN");
			assertEquals(3, counters.getValue("WILL_SUCCEED_ON_THIRD_RUN"));
		}

		@Test
		public void iWillSucceedOnMyFourthRunBecauseAll3RetriesNecessaryDefinedAtClassLevel() {
			counters.increment("WILL_SUCCEED_ON_FOURTH_RUN");
			assertEquals(4, counters.getValue("WILL_SUCCEED_ON_FOURTH_RUN"));
		}

		@Test
		public void iWillFailOnMyFifthRunBecauseMoreThan3RetriesNecessaryDefinedAtClassLevel() {
			counters.increment("WILL_FAIL_BECAUSE_ONLY_THREE_RETRIES");
			assertEquals(5, counters.getValue("WILL_FAIL_BECAUSE_ONLY_THREE_RETRIES"));
		}

		@Test
		@PingPong(maxNumberOfRetries = 2)
		public void iWillFailOnMyFourthRunBecausePingPongAnnotationAtMethodLevelBeatsPingPongAnnotationAtClassLevel() {
			counters.increment("WILL_FAIL_BECAUSE_ONLY_TWO_RETRIES");
			assertEquals(4, counters.getValue("WILL_FAIL_BECAUSE_ONLY_TWO_RETRIES"));
		}

		@Test
		@PingPong(maxNumberOfRetries = 2)
		public void iWillSucceedOnMyThirdRunBecausePingPongAnnotationAtMethodLevelAllows2Retries() {
			counters.increment("WILL_SUCCEED_BECAUSE_OF_TWO_RETRIES");
			assertEquals(3, counters.getValue("WILL_SUCCEED_BECAUSE_OF_TWO_RETRIES"));
		}
	}

	private static class Counters {
		private Map<String, Integer> map = new HashMap<String, Integer>();

		public void increment(String key) {
			int newValue = getValue(key) + 1;
			map.put(key, newValue);
		}

		public int getValue(String key) {
			Integer value = map.get(key);
			if (value == null) {
				return 0;
			}
			return value;
		}
	}
}
