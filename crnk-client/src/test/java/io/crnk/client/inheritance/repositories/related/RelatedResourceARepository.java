package io.crnk.client.inheritance.repositories.related;

import io.crnk.client.inheritance.repositories.RepositoryData;
import io.crnk.client.inheritance.resources.related.RelatedResourceA;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;

/*
 * @author syri.
 */
public class RelatedResourceARepository extends ResourceRepositoryBase<RelatedResourceA, Long> {

    public RelatedResourceARepository() {
        super(RelatedResourceA.class);
    }

    @Override
    public ResourceList<RelatedResourceA> findAll(QuerySpec querySpec) {
        return querySpec.apply(RepositoryData.RESOURCE_A_LIST);
    }
}
