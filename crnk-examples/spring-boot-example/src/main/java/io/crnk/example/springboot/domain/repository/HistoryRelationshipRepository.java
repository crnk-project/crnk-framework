package io.crnk.example.springboot.domain.repository;

import com.google.common.reflect.TypeToken;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.contributor.ResourceFieldContributor;
import io.crnk.core.engine.information.contributor.ResourceFieldContributorContext;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccessor;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyRelationshipRepositoryBase;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.list.DefaultResourceList;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.example.springboot.domain.model.History;
import org.springframework.stereotype.Component;

/**
 * Generic repository that introduces a history relationship for project and task resource without touching
 * those resources.
 */
// tag::docs[]
@Component
public class HistoryRelationshipRepository extends ReadOnlyRelationshipRepositoryBase<Object, Serializable, History, UUID>
		implements ResourceFieldContributor {

	@Override
	public List<ResourceField> getResourceFields(ResourceFieldContributorContext context) {
		// this method could be omitted if the history field is added regularly to Project and Task resource. This would be
		// simpler and recommended, but may not always be possible. Here we demonstrate doing it dynamically.
		InformationBuilder.Field fieldBuilder = context.getInformationBuilder().createResourceField();
		fieldBuilder.name("history");
		fieldBuilder.genericType(new TypeToken<List<History>>() {
		}.getType());
		fieldBuilder.oppositeResourceType("history");
		fieldBuilder.fieldType(ResourceFieldType.RELATIONSHIP);

		// field values are "null" on resource and we make use of automated lookup to the relationship repository
		// instead:
		fieldBuilder.lookupIncludeBehavior(LookupIncludeBehavior.AUTOMATICALLY_ALWAYS);
		fieldBuilder.accessor(new ResourceFieldAccessor() {
			@Override
			public Object getValue(Object resource) {
				return null;
			}

			@Override
			public void setValue(Object resource, Object fieldValue) {
			}
		});
		return Arrays.asList(fieldBuilder.build());
	}

	@Override
	public RelationshipMatcher getMatcher() {
		return new RelationshipMatcher().rule().target(History.class).add();
	}

	@Override
	public ResourceList<History> findManyTargets(Serializable sourceId, String fieldName, QuerySpec querySpec) {
		DefaultResourceList list = new DefaultResourceList();
		for (int i = 0; i < 10; i++) {
			History history = new History();
			history.setId(UUID.nameUUIDFromBytes(("historyElement" + i).getBytes()));
			history.setName("historyElement" + i);
			list.add(history);
		}
		return list;
	}
}
// end::docs[]