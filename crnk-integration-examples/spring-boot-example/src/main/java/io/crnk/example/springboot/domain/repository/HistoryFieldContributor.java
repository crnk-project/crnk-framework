package io.crnk.example.springboot.domain.repository;

import java.util.Arrays;
import java.util.List;

import com.google.common.reflect.TypeToken;
import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.contributor.ResourceFieldContributor;
import io.crnk.core.engine.information.contributor.ResourceFieldContributorContext;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccessor;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.example.springboot.domain.model.History;
import org.springframework.stereotype.Component;

// tag::docs[]
@Component
public class HistoryFieldContributor implements ResourceFieldContributor {

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

			@Override
			public Class getImplementationClass() {
				return List.class;
			}
		});
		return Arrays.asList(fieldBuilder.build());
	}
}
// end::docs[]