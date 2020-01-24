package io.crnk.example.springboot.domain.repository;

import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ManyRelationshipRepositoryBase;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.example.springboot.domain.model.History;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Generic repository that introduces a history relationship for project and task resource without touching
 * those resources.
 */
// tag::docs[]
@Component
public class HistoryRelationshipRepository extends ManyRelationshipRepositoryBase<Object, Serializable, History, UUID> {

    @Override
    public RelationshipMatcher getMatcher() {
        return new RelationshipMatcher().rule().target(History.class).add();
    }

    @Override
    public Map<Serializable, ResourceList<History>> findManyRelations(Collection<Serializable> sourceIds, String fieldName, QuerySpec querySpec) {
        Map<Serializable, ResourceList<History>> map = new HashMap<>();
        for (Serializable sourceId : sourceIds) {
            DefaultResourceList list = new DefaultResourceList();
            for (int i = 0; i < 10; i++) {
                History history = new History();
                history.setId(UUID.nameUUIDFromBytes(("historyElement" + i).getBytes()));
                history.setName("historyElement" + i);
                list.add(history);
            }
            map.put(sourceId, list);
        }
        return map;
    }
}
// end::docs[]