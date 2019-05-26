package io.crnk.ui;


import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.TestModule;
import io.crnk.ui.presentation.element.DataTableElement;
import io.crnk.ui.presentation.element.ExplorerElement;
import io.crnk.ui.presentation.element.TableColumnElement;
import io.crnk.ui.presentation.element.TableColumnsElement;
import io.crnk.ui.presentation.repository.ExplorerRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ExplorerRepositoryTest {


    private UIModule uiModule;

    @Before
    public void setup() {
        uiModule = UIModule.create(new UIModuleConfig());
        CrnkBoot boot = new CrnkBoot();
        boot.addModule(uiModule);
        boot.addModule(new TestModule());
        boot.boot();

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
        ExplorerElement explorer = repository.findOne("local-resources.tasks", new QuerySpec(ExplorerElement.class));
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
        Assert.assertEquals("display", idColumn.getComponent().getComponentId());
    }
}
