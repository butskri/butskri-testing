package be.butskri.testing;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(BlockJUnit4ClassRunnerRetryingPingPongers.class)
public class MyTest {

	@PingPong(maxNumberOfRetries = 2)
	@Test
	public void iWillSucceedWhenSuccessWithinThreeTries() {
		// ...
	}
}
