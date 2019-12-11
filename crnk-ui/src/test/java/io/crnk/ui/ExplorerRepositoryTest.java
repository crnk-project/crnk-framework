package io.crnk.ui;


import java.util.Arrays;
import java.util.List;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.http.HttpRequestContextBase;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.internal.http.HttpRequestContextBaseAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.exception.ResourceNotFoundException;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.TestModule;
import io.crnk.ui.presentation.element.DataTableElement;
import io.crnk.ui.presentation.element.ExplorerElement;
import io.crnk.ui.presentation.element.LabelElement;
import io.crnk.ui.presentation.element.PlainTextElement;
import io.crnk.ui.presentation.element.TableColumnElement;
import io.crnk.ui.presentation.element.TableColumnsElement;
import io.crnk.ui.presentation.repository.ExplorerRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class ExplorerRepositoryTest {


	private UIModule uiModule;

	private QueryContext queryContext;

	@Before
	public void setup() {
		SimpleModule module = new SimpleModule("presentationTest");
		module.addRepository(new InMemoryResourceRepository<>(PresentationTask.class));
		module.addRepository(new InMemoryResourceRepository<>(PresentationProject.class));

		uiModule = UIModule.create(new UIModuleConfig());
		CrnkBoot boot = new CrnkBoot();
		boot.addModule(module);
		boot.addModule(uiModule);
		boot.addModule(new TestModule());
		boot.boot();

		HttpRequestContextProvider httpRequestContextProvider = boot.getModuleRegistry().getHttpRequestContextProvider();
		httpRequestContextProvider.onRequestStarted(new HttpRequestContextBaseAdapter(Mockito.mock(HttpRequestContextBase.class)));
		queryContext = httpRequestContextProvider.getRequestContext().getQueryContext();
		queryContext.setRequestVersion(2);
	}

	@Test
	public void checkFindAll() {
		ExplorerRepository repository = uiModule.getExplorerRepository();
		ResourceList<ExplorerElement> explorers = repository.findAll(new QuerySpec(ExplorerElement.class));
		Assert.assertNotNull(explorers);
		Assert.assertTrue(explorers.size() > 0);
	}

	@Test
	public void checkFindOne() {
		ExplorerRepository repository = uiModule.getExplorerRepository();
		ExplorerElement explorer = repository.findOne("local-tasks", new QuerySpec(ExplorerElement.class));
		Assert.assertNotNull(explorer);

		Assert.assertEquals("tasks", explorer.getBaseQuery().getResourceType());

		DataTableElement table = explorer.getTable();

		TableColumnsElement columns = table.getColumns();
		Assert.assertNotEquals(0, columns.getElementIds().size());

		TableColumnElement idColumn = columns.getElements().get("id");
		Assert.assertEquals("id", idColumn.getId());
		Assert.assertEquals("id", idColumn.getLabel());
		Assert.assertFalse(idColumn.isEditable());
		Assert.assertTrue(idColumn.isSortable());
		Assert.assertEquals(PathSpec.of("id"), idColumn.getAttributePath());
		Assert.assertEquals("number", idColumn.getComponent().getComponentId());
	}

	@Test
	public void checkFullTextSearch() {
		ExplorerRepository repository = uiModule.getExplorerRepository();
		ExplorerElement explorer = repository.findOne("local-presentationTask", new QuerySpec(ExplorerElement.class));

		List<PathSpec> fullTextSearchPaths = explorer.getFullTextSearchPaths();
		Assert.assertEquals(1, fullTextSearchPaths.size());
		Assert.assertEquals(PathSpec.of("name"), fullTextSearchPaths.get(0));
	}

	@Test
	public void checkId() {
		ExplorerRepository repository = uiModule.getExplorerRepository();
		ExplorerElement explorer = repository.findOne("local-presentationTask", new QuerySpec(ExplorerElement.class));
		Assert.assertEquals("local-presentationTask", explorer.getId());
	}

	@Test
	public void checkLabels() {
		ExplorerRepository repository = uiModule.getExplorerRepository();
		ExplorerElement explorer = repository.findOne("local-presentationTask", new QuerySpec(ExplorerElement.class));

		TableColumnElement projectColumn = explorer.getTable().getColumns().getElements().get("project");
		LabelElement element = (LabelElement) projectColumn.getComponent();
		List<PathSpec> labelAttributes = element.getLabelAttributes();
		Assert.assertEquals(1, labelAttributes.size());
		Assert.assertEquals(PathSpec.of("name"), labelAttributes.get(0));
		Assert.assertEquals("local-presentationProject", element.getViewerId());
	}

	@Test
	public void checkNumberColumn() {
		ExplorerRepository repository = uiModule.getExplorerRepository();
		ExplorerElement explorer = repository.findOne("local-presentationTask", new QuerySpec(ExplorerElement.class));

		TableColumnElement projectColumn = explorer.getTable().getColumns().getElements().get("priority");
		PlainTextElement element = (PlainTextElement) projectColumn.getComponent();
		Assert.assertEquals("number", element.getComponentId());
	}


	@Test(expected = ResourceNotFoundException.class)
	public void checkResourceVersionOutOfRange() {
		queryContext.setRequestVersion(0);

		ExplorerRepository repository = uiModule.getExplorerRepository();
		repository.findOne("local-presentationProject", new QuerySpec(ExplorerElement.class));
	}

	@Test
	public void checkResourceVersionInRange() {
		queryContext.setRequestVersion(1);

		ExplorerRepository repository = uiModule.getExplorerRepository();
		ExplorerElement explorer = repository.findOne("local-presentationProject", new QuerySpec(ExplorerElement.class));
		Assert.assertNotNull(explorer);
	}

	@Test
	public void checkFieldVersionOutOfRange() {
		queryContext.setRequestVersion(1);
		ExplorerRepository repository = uiModule.getExplorerRepository();
		ExplorerElement explorer = repository.findOne("local-presentationProject", new QuerySpec(ExplorerElement.class));

		TableColumnsElement elements = explorer.getTable().getColumns();
		Assert.assertEquals(Arrays.asList("id", "name"), elements.getElementIds());
	}

	@Test
	public void checkFieldVersionInRange() {
		queryContext.setRequestVersion(2);
		ExplorerRepository repository = uiModule.getExplorerRepository();
		ExplorerElement explorer = repository.findOne("local-presentationProject", new QuerySpec(ExplorerElement.class));

		TableColumnsElement elements = explorer.getTable().getColumns();
		Assert.assertEquals(Arrays.asList("id", "name", "description"), elements.getElementIds());
	}
}
