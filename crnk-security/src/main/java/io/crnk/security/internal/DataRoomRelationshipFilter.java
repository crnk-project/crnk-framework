package io.crnk.security.internal;

import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ManyRelationshipRepository;
import io.crnk.core.repository.MatchedRelationshipRepository;
import io.crnk.core.repository.OneRelationshipRepository;
import io.crnk.core.repository.RelationshipMatcher;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class DataRoomRelationshipFilter implements ManyRelationshipRepository, OneRelationshipRepository {

    private final DataRoomMatcher matcher;

    private final Object wrappedRepository;


    public DataRoomRelationshipFilter(Object wrappedRepository, DataRoomMatcher matcher) {
        this.matcher = matcher;
        this.wrappedRepository = wrappedRepository;
    }

    @Override
    public void setRelation(Object source, Object targetId, String fieldName) {
        matcher.verifyMatch(source, HttpMethod.POST);
        checkTarget(Collections.singleton(targetId));

        ((OneRelationshipRepository) wrappedRepository).setRelation(source, targetId, fieldName);
    }

    @Override
    public Map findOneRelations(Collection sourceIds, String fieldName, QuerySpec querySpec) {
        QuerySpec dataroomQuerySpec = matcher.filter(querySpec, HttpMethod.GET);
        return ((OneRelationshipRepository) wrappedRepository).findOneRelations(sourceIds, fieldName, dataroomQuerySpec);
    }


    @Override
    public RelationshipMatcher getMatcher() {
        return ((MatchedRelationshipRepository) wrappedRepository).getMatcher();
    }

    @Override
    public void setRelations(Object source, Collection targetIds, String fieldName) {
        matcher.verifyMatch(source, HttpMethod.POST);
        checkTarget(targetIds);

        ((ManyRelationshipRepository) wrappedRepository).setRelations(source, targetIds, fieldName);
    }

    @Override
    public void addRelations(Object source, Collection targetIds, String fieldName) {
        matcher.verifyMatch(source, HttpMethod.POST);
        checkTarget(targetIds);

        ((ManyRelationshipRepository) wrappedRepository).addRelations(source, targetIds, fieldName);
    }

    @Override
    public void removeRelations(Object source, Collection targetIds, String fieldName) {
        matcher.verifyMatch(source, HttpMethod.DELETE);
        checkTarget(targetIds);

        ((ManyRelationshipRepository) wrappedRepository).removeRelations(source, targetIds, fieldName);
    }

    @Override
    public Map findManyRelations(Collection sourceIds, String fieldName, QuerySpec querySpec) {
        QuerySpec dataroomQuerySpec = matcher.filter(querySpec, HttpMethod.GET);
        return ((ManyRelationshipRepository) wrappedRepository).findManyRelations(sourceIds, fieldName, dataroomQuerySpec);
    }

    private void checkTarget(Iterable targetIds) {
        // we do not check targets for performance reasons
        // DataRoomFilter must properly filter both sides of a relationship
    }


}
