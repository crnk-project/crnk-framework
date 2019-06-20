package io.crnk.ui;


import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.InMemoryResourceRepository;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.test.mock.TestModule;
import io.crnk.ui.presentation.element.EditorElement;
import io.crnk.ui.presentation.element.FormContainerElement;
import io.crnk.ui.presentation.element.FormElement;
import io.crnk.ui.presentation.element.FormElements;
import io.crnk.ui.presentation.element.PlainTextElement;
import io.crnk.ui.presentation.repository.EditorRepository;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EditorRepositoryTest {


	private UIModule uiModule;

	@Before
	public void setup() {
		uiModule = UIModule.create(new UIModuleConfig());

		SimpleModule module = new SimpleModule("presentationTest");
		module.addRepository(new InMemoryResourceRepository<>(PresentationTask.class));
		module.addRepository(new InMemoryResourceRepository<>(PresentationProject.class));

		CrnkBoot boot = new CrnkBoot();
		boot.addModule(module);
		boot.addModule(uiModule);
		boot.addModule(new TestModule());
		boot.boot();

	}

	@Test
	public void checkFindAll() {
		EditorRepository repository = uiModule.getEditorRepository();
		ResourceList<EditorElement> editors = repository.findAll(new QuerySpec(EditorElement.class));
		Assert.assertNotNull(editors);
		Assert.assertTrue(editors.size() > 0);
	}


	@Test
	public void checkFindOne() {
		EditorRepository repository = uiModule.getEditorRepository();
		EditorElement editor = repository.findOne("local-resources.tasks", new QuerySpec(EditorElement.class));
		Assert.assertNotNull(editor);

		Assert.assertEquals("tasks", editor.getBaseQuery().getResourceType());

		FormContainerElement form = editor.getForm();

		FormElements elements = form.getElements();
		Assert.assertNotEquals(0, elements.getElementIds().size());

		FormElement idFormElement = elements.getElements().get("id");
		Assert.assertEquals("id", idFormElement.getId());
		Assert.assertEquals("id", idFormElement.getLabel());
		Assert.assertFalse(idFormElement.isEditable());
		Assert.assertEquals(PathSpec.of("id"), idFormElement.getAttributePath());
		PlainTextElement idComponent = (PlainTextElement) idFormElement.getComponent();
		Assert.assertEquals("number", idComponent.getComponentId());
	}
}
