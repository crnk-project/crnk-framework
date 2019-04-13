package io.crnk.activiti.example.approval;


import com.google.common.collect.Lists;
import io.crnk.activiti.resource.ProcessInstanceResource;
import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.contributor.ResourceFieldContributor;
import io.crnk.core.engine.information.contributor.ResourceFieldContributorContext;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import io.crnk.core.engine.information.resource.ResourceFieldAccessor;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.registry.ResourceRegistryAware;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.OneRelationshipRepositoryBase;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.list.ResourceList;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ApprovalRelationshipRepository<R, P extends ProcessInstanceResource> extends OneRelationshipRepositoryBase<R,
        Serializable, P, String> implements ResourceRegistryAware, ResourceFieldContributor {

    private static final String RESOURCE_ID_FIELD = "resourceId";

    private static final String RESOURCE_TYPE_FIELD = "resourceType";

    private final Class<R> resourceClass;

    private final Class<P> processInfoClass;

    private final List<FilterSpec> baseFilters;

    private ResourceRegistry resourceRegistry;

    private String relationshipName;

    private String oppositeResourceType;

    public ApprovalRelationshipRepository(Class<R> resourceClass, Class<P> processInfoClass, String relationshipName,
                                          String oppositeResourceType, List<FilterSpec> baseFilters) {
        this.resourceClass = resourceClass;
        this.processInfoClass = processInfoClass;
        this.relationshipName = relationshipName;
        this.oppositeResourceType = oppositeResourceType;
        this.baseFilters = baseFilters;
    }

    @Override
    public RelationshipMatcher getMatcher() {
        RelationshipMatcher matcher = new RelationshipMatcher();
        matcher.rule().field(relationshipName).add();
        return matcher;
    }

    @Override
    public List<ResourceField> getResourceFields(ResourceFieldContributorContext context) {
        if (context.getResourceInformation().getImplementationClass().equals(resourceClass)) {
            InformationBuilder.FieldInformationBuilder fieldBuilder = context.getInformationBuilder().createResourceField();
            fieldBuilder.name(relationshipName);
            fieldBuilder.oppositeResourceType(oppositeResourceType);
            fieldBuilder.lookupIncludeBehavior(LookupIncludeBehavior.AUTOMATICALLY_ALWAYS);
            fieldBuilder.fieldType(ResourceFieldType.RELATIONSHIP);
            fieldBuilder.access(new ResourceFieldAccess(true, false, false, false, false, false));
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
                    return processInfoClass;
                }
            });
            fieldBuilder.type(processInfoClass);
            fieldBuilder.genericType(processInfoClass);
            return Lists.newArrayList(fieldBuilder.build());
        }
        return Collections.emptyList();
    }

    @Override
    public Map<Serializable, P> findOneRelations(Collection<Serializable> sourceIds, String fieldName, QuerySpec querySpec) {
        if (relationshipName.equals(fieldName)) {
            Map<Serializable, P> map = new HashMap<>();
            for (Serializable sourceId : sourceIds) {
                RegistryEntry resourceEntry = resourceRegistry.getEntry(resourceClass);
                RegistryEntry processEntry = resourceRegistry.getEntry(processInfoClass);
                String resourceType = resourceEntry.getResourceInformation().getResourceType();

                ResourceRepository processRepository =
                        (ResourceRepository) processEntry.getResourceRepository().getResourceRepository();

                QuerySpec processQuerySpec = querySpec.duplicate();
                processQuerySpec.addFilter(new FilterSpec(Arrays.asList(RESOURCE_ID_FIELD), FilterOperator.EQ, sourceId.toString()));
                processQuerySpec.addFilter(new FilterSpec(Arrays.asList(RESOURCE_TYPE_FIELD), FilterOperator.EQ, resourceType));
                baseFilters.forEach(processQuerySpec::addFilter);

                ResourceList list = processRepository.findAll(querySpec);
                PreconditionUtil.assertTrue("unique result expected", list.size() <= 1);
                map.put(sourceId, list.isEmpty() ? null : (P) list.get(0));
            }
            return map;
        } else {
            throw new UnsupportedOperationException("unknown fieldName '" + fieldName + "'");
        }

    }

    @Override
    public void setResourceRegistry(ResourceRegistry resourceRegistry) {
        this.resourceRegistry = resourceRegistry;
    }


}
