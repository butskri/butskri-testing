package be.butskri.testing;

import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public interface StatementFactory {

	public Statement createStatement(FrameworkMethod frameworkMethod);

}
