package io.crnk.ui.presentation.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.ui.presentation.PresentationManager;
import io.crnk.ui.presentation.element.EditorElement;

public class EditorRepository extends ResourceRepositoryBase<EditorElement, String> {

	private final PresentationManager manager;

	public EditorRepository(PresentationManager manager) {
		super(EditorElement.class);
		this.manager = manager;
	}

	@Override
	public EditorElement findOne(String id, QuerySpec querySpec) {
		return manager.getEditor(id);
	}

	@Override
	public ResourceList<EditorElement> findAll(QuerySpec querySpec) {
		return querySpec.apply(manager.getEditors().values());
	}
}
