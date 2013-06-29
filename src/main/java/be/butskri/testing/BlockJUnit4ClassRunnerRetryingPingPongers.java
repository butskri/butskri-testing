package be.butskri.testing;

import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

public class BlockJUnit4ClassRunnerRetryingPingPongers extends BlockJUnit4ClassRunner implements StatementFactory {

	public BlockJUnit4ClassRunnerRetryingPingPongers(Class<?> aClass) throws InitializationError {
		super(aClass);
	}

	@Override
	protected Statement methodBlock(FrameworkMethod frameworkMethod) {
		return new StatementWrapperForRetryingPingPongers(frameworkMethod, this);
	}

	@Override
	public Statement createStatement(FrameworkMethod frameworkMethod) {
		return super.methodBlock(frameworkMethod);
	}

}
