package io.crnk.core.queryspec.internal;

import io.crnk.core.engine.document.ErrorData;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.core.engine.information.bean.BeanInformation;
import io.crnk.core.engine.information.resource.AnyResourceFieldAccessor;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.MultivaluedMap;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.internal.utils.StringUtils;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.queryspec.mapper.QueryPathResolver;
import io.crnk.core.queryspec.mapper.QueryPathSpec;
import io.crnk.core.queryspec.mapper.QuerySpecUrlContext;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

// TODO eventually ResourceInformation should alo provide information abaout nested non-resource types to make use of reflection here unncessary
public class DefaultQueryPathResolver implements QueryPathResolver {

    private QuerySpecUrlContext ctx;

    private boolean allowUnknownAttributes = false;

    private boolean mapJsonNames = true;

    @Override
    public void init(QuerySpecUrlContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public QueryPathSpec resolve(ResourceInformation sourceResourceInformation, List<String> attributePath, NamingType sourceNamingType, String sourceParameter, QueryContext queryContext) {
        if (attributePath == null) {
            // no attribute specified, query string expected, use String
            return new QueryPathSpec(String.class, null, null);
        }

        ResourceInformation resourceInformation = sourceResourceInformation;

        ResourceRegistry resourceRegistry = ctx.getResourceRegistry();
        Type valueType = resourceInformation.getResourceClass();
        AnyResourceFieldAccessor anyFieldAccessor = resourceInformation.getAnyFieldAccessor();

        SubTypeMap subTypeMap = null;

        List<String> targetPath = new ArrayList<>();
        List<ResourceField> fields = new ArrayList<>();

        for (String sourceAttributePath : attributePath) {
            if (resourceInformation != null) {
                ResourceField field = sourceNamingType == NamingType.JSON ? resourceInformation.findFieldByJsonName(sourceAttributePath, queryContext.getRequestVersion()) : resourceInformation.findFieldByUnderlyingName(sourceAttributePath);
                if (field == null) {
                    // search subtypes for field if not available on main resource
                    if (subTypeMap == null) {
                        subTypeMap = new SubTypeMap(resourceRegistry);
                    }
                    List<ResourceInformation> subTypes = subTypeMap.findSubTypes(resourceInformation.getResourceType());
                    for (ResourceInformation subType : subTypes) {
                        field = sourceNamingType == NamingType.JSON ? subType.findFieldByJsonName(sourceAttributePath, queryContext.getRequestVersion()) : subType.findFieldByUnderlyingName(sourceAttributePath);
                        if (field != null) {
                            break;
                        }
                    }
                }

                fields.add(field);

                if (field != null) {
                    if (field.getResourceFieldType() == ResourceFieldType.RELATIONSHIP) {
                        RegistryEntry entry = resourceRegistry.getEntry(field.getOppositeResourceType());
                        PreconditionUtil.verify(entry != null, "resourceType=%s not found for field=%s", field.getOppositeResourceType(), field.getUnderlyingName());
                        resourceInformation = entry.getResourceInformation();
                        valueType = resourceInformation.getResourceClass();
                    } else {
                        resourceInformation = null;
                        valueType = field.getElementType();
                    }
                    targetPath.add(sourceNamingType == NamingType.JSON ? field.getUnderlyingName() : field.getJsonName());
                    continue;
                }
            } else {
                resourceInformation = null;
                fields.add(null);
                if (valueType == Object.class) {
                    targetPath.add(sourceAttributePath);
                    continue;
                } else if (Map.class.isAssignableFrom(ClassUtils.getRawType(valueType))) {
                    if (valueType instanceof ParameterizedType) {
                        valueType = ((ParameterizedType) valueType).getActualTypeArguments()[1];
                    } else {
                        valueType = Object.class;
                    }
                    targetPath.add(sourceAttributePath);
                    continue;
                } else {
                    BeanInformation beanInformation = BeanInformation.get(ClassUtils.getRawType(valueType));
                    BeanAttributeInformation attribute = sourceNamingType == NamingType.JSON ? beanInformation.getAttributeByJsonName(sourceAttributePath) : beanInformation.getAttribute(sourceAttributePath);
                    if (attribute != null) {
                        valueType = attribute.getImplementationType();
                        targetPath.add(sourceNamingType == NamingType.JSON ? attribute.getName() : attribute.getJsonName());
                        continue;
                    }
                }
            }

            if (allowUnknownAttributes || anyFieldAccessor != null) {
                targetPath.add(sourceAttributePath);
                valueType = Object.class;
            } else {
                ErrorData errorData = ErrorData.builder()
                        .setCode("UNKNOWN_PARAMETER")
                        .setTitle("unknown parameter")
                        .setDetail("Failed to resolve path to field '" + StringUtils.join(".", attributePath) + "' from " + resourceInformation.getResourceType())
                        .setSourceParameter(sourceParameter)
                        .setStatus(String.valueOf(HttpStatus.BAD_REQUEST_400)).build();
                throw new BadRequestException(HttpStatus.BAD_REQUEST_400, errorData);
            }
        }

        List<String> path = mapJsonNames ? targetPath : attributePath;
        return new QueryPathSpec(valueType, path, fields);
    }

    @Override
    public boolean getAllowUnknownAttributes() {
        return allowUnknownAttributes;
    }

    @Override
    public void setAllowUnknownAttributes(boolean allowUnknownAttributes) {
        this.allowUnknownAttributes = allowUnknownAttributes;
    }

    @Override
    public boolean getMapJsonNames() {
        return mapJsonNames;
    }

    @Override
    public void setMapJsonNames(boolean mapJsonNames) {
        this.mapJsonNames = mapJsonNames;
    }

    class SubTypeMap {
        private MultivaluedMap<String, ResourceInformation> mapping = new MultivaluedMap();

        public SubTypeMap(ResourceRegistry resourceRegistry) {

            Collection<RegistryEntry> entries = resourceRegistry.getEntries();
            for (RegistryEntry entry : entries) {
                ResourceInformation resourceInformation = entry.getResourceInformation();
                String superResourceType = resourceInformation.getSuperResourceType();
                if (superResourceType != null) {
                    mapping.add(superResourceType, resourceInformation);
                }
            }
        }

        public List<ResourceInformation> findSubTypes(String resourceType) {
            List<ResourceInformation> results = new ArrayList<>();
            findSubTypes(results, resourceType);
            return results;
        }

        private void findSubTypes(List<ResourceInformation> results, String resourceType) {
            if (mapping.containsKey(resourceType)) {
                List<ResourceInformation> children = mapping.getList(resourceType);
                // add direct descendants first
                for (ResourceInformation child : children) {
                    results.add(child);
                }
                // only after add transitive children
                for (ResourceInformation child : children) {
                    findSubTypes(results, child.getResourceType());
                }
            }
        }
    }


}
