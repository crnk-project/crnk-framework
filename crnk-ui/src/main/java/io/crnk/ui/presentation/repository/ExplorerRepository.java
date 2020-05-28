package io.crnk.ui.presentation.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.ui.presentation.PresentationManager;
import io.crnk.ui.presentation.element.ExplorerElement;

public class ExplorerRepository extends ResourceRepositoryBase<ExplorerElement, String> {

    private final PresentationManager manager;

    public ExplorerRepository(PresentationManager manager) {
        super(ExplorerElement.class);
        this.manager = manager;
    }

    @Override
    public ExplorerElement findOne(String id, QuerySpec querySpec) {
        return manager.getExplorer(id);
    }

    @Override
    public ResourceList<ExplorerElement> findAll(QuerySpec querySpec) {
        return querySpec.apply(manager.getExplorers().values());
    }
}
