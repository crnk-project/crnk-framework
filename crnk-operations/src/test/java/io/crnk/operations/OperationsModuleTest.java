package io.crnk.operations;

import io.crnk.operations.server.OperationFilter;
import io.crnk.operations.server.OperationsModule;
import io.crnk.operations.server.order.OperationOrderStrategy;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class OperationsModuleTest extends AbstractOperationsTest {

	private OperationsModule module = OperationsModule.create();

	@Test
	public void testName() {
		Assert.assertEquals("operations", module.getModuleName());
	}

	@Test
	public void testRemoveFilter() {
		OperationFilter filter = Mockito.mock(OperationFilter.class);

		module.addFilter(filter);
		Assert.assertEquals(1, module.getFilters().size());
		module.removeFilter(filter);
		Assert.assertEquals(0, module.getFilters().size());
	}

	@Test
	public void testSetOrderStrategy() {
		OperationOrderStrategy strategy = Mockito.mock(OperationOrderStrategy.class);
		module.setOrderStrategy(strategy);
		Assert.assertSame(strategy, module.getOrderStrategy());
	}

}
