package io.crnk.core.queryspec.mapper;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.queryspec.PathSpec;

import java.lang.reflect.Type;
import java.util.List;

public class QueryPathSpec {

    private Type valueType;

    private List<String> attributePath;

    private List<ResourceField> fields;

    public QueryPathSpec(Type valueType, List<String> attributePath, List<ResourceField> fields) {
        this.valueType = valueType;
        this.attributePath = attributePath;
        this.fields = fields;
    }

    public Type getValueType() {
        return valueType;
    }

    public List<String> getAttributePath() {
        return attributePath;
    }

    /**
     * @return elements of path, either a {@link ResourceField} or null in case of unknown or embedded fields.
     * Support for the later added in the future.
     */
    public List<ResourceField> getFields() {
        return fields;
    }

    public void verifyFilterable() {
        if (fields != null) {
            for (ResourceField field : fields) {
                if (field != null && !field.getAccess().isFilterable()) {
                    throw new BadRequestException("path " + attributePath + " is not filterable");
                }
            }
        }
    }

    public void verifySortable() {
        if (fields != null) {
            for (ResourceField field : fields) {
                if (field != null && !field.getAccess().isSortable()) {
                    throw new BadRequestException("path " + attributePath + " is not sortable");
                }
            }
        }
    }

    public PathSpec toPathSpec() {
        return PathSpec.of(getAttributePath());
    }
}
