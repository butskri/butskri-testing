package be.butskri.testing;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class StatementWrapperForRetryingPingPongers extends Statement {

	private StatementFactory statementFactory;
	private FrameworkMethod frameworkMethod;

	public StatementWrapperForRetryingPingPongers(FrameworkMethod frameworkMethod, StatementFactory statementFactory) {
		this.frameworkMethod = frameworkMethod;
		this.statementFactory = statementFactory;
	}

	@Override
	public void evaluate() throws Throwable {
		tryToEvaluate(frameworkMethod, maxNumberOfRetries());
	}

	private void tryToEvaluate(FrameworkMethod frameworkMethod, int numberOfRetriesLeft) throws Throwable {
		try {
			runTest();
		} catch (Throwable e) {
			if (numberOfRetriesLeft < 1) {
				throw e;
			}
			logFailure(frameworkMethod, numberOfRetriesLeft, e);
			tryToEvaluate(frameworkMethod, numberOfRetriesLeft - 1);
		}
	}

	private void runTest() throws Throwable {
		statementFactory.createStatement(frameworkMethod).evaluate();
	}

	private String retryMessage(FrameworkMethod frameworkMethod, int numberOfRetriesLeft) {
		return "pingpong test " + frameworkMethod + " failed. Retrying... Number of retries left: " + numberOfRetriesLeft;
	}

	private int maxNumberOfRetries() {
		PingPong pingPong = getPingPongAnnotation();
		if (pingPong == null) {
			return 0;
		} else if (pingPong.maxNumberOfRetries() < 0) {
			throw new IllegalArgumentException("illegal value for maxNumberOfRetries: " + pingPong.maxNumberOfRetries());
		}
		return pingPong.maxNumberOfRetries();
	}

	private PingPong getPingPongAnnotation() {
		PingPong pingPong = frameworkMethod.getAnnotation(PingPong.class);
		if (pingPong == null) {
			pingPong = frameworkMethod.getMethod().getDeclaringClass().getAnnotation(PingPong.class);
		}
		return pingPong;
	}

	private void logFailure(FrameworkMethod frameworkMethod, int numberOfRetriesLeft, Throwable e) {
		System.out.println(retryMessage(frameworkMethod, numberOfRetriesLeft));
		e.printStackTrace();
	}

}
