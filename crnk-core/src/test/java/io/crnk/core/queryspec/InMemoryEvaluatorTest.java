package io.crnk.core.queryspec;

public class InMemoryEvaluatorTest extends InMemoryEvaluatorTestBase {


	@Override
	protected InMemoryEvaluator getEvaluator() {
		return new InMemoryEvaluator();
	}
}
