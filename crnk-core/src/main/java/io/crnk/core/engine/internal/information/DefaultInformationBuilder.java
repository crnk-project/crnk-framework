package io.crnk.core.engine.internal.information;

import io.crnk.core.engine.information.InformationBuilder;
import io.crnk.core.engine.information.repository.RelationshipRepositoryInformation;
import io.crnk.core.engine.information.repository.RepositoryAction;
import io.crnk.core.engine.information.repository.RepositoryMethodAccess;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.EmbeddableInformation;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import io.crnk.core.engine.information.resource.ResourceFieldAccessor;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.ResourceValidator;
import io.crnk.core.engine.information.resource.VersionRange;
import io.crnk.core.engine.internal.information.repository.RelationshipRepositoryInformationImpl;
import io.crnk.core.engine.internal.information.repository.ResourceRepositoryInformationImpl;
import io.crnk.core.engine.internal.information.resource.ResourceFieldImpl;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.parser.StringMapper;
import io.crnk.core.engine.parser.TypeParser;
import io.crnk.core.queryspec.pagingspec.PagingSpec;
import io.crnk.core.repository.RelationshipMatcher;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.JsonIncludeStrategy;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import io.crnk.core.resource.annotations.PatchStrategy;
import io.crnk.core.resource.annotations.RelationshipRepositoryBehavior;
import io.crnk.core.resource.annotations.SerializeType;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultInformationBuilder implements InformationBuilder {

    private final TypeParser typeParser;

    @Override
    public FieldInformationBuilder createResourceField() {
        return new DefaultField();
    }

    @Override
    public RelationshipRepositoryInformationBuilder createRelationshipRepository(String sourceResourceType, String targetResourceType) {
        RelationshipMatcher matcher = new RelationshipMatcher();
        matcher.rule().target(targetResourceType).source(sourceResourceType).add();
        return createRelationshipRepository(matcher);
    }

    @Override
    public RelationshipRepositoryInformationBuilder createRelationshipRepository(RelationshipMatcher matcher) {
        DefaultRelationshipRepository repository = new DefaultRelationshipRepository();
        repository.matcher = matcher;
        return repository;
    }


    @Override
    public ResourceRepositoryInformationBuilder createResourceRepository() {
        return new DefaultResourceRepository();
    }

    @Override
    public ResourceInformationBuilder createResource(Class<?> resourceClass, String resourceType) {
        return createResource(resourceClass, resourceType, null);
    }

    @Override
    public ResourceInformationBuilder createResource(Class<?> resourceClass, String resourceType, String resourcePath) {
        DefaultResource resource = new DefaultResource();
        resource.implementationType(resourceClass);
        resource.resourceType(resourceType);
        resource.resourcePath(resourcePath);
        return resource;
    }

    public class DefaultRelationshipRepository implements RelationshipRepositoryInformationBuilder {

        private RelationshipMatcher matcher;

        private RepositoryMethodAccess access = new RepositoryMethodAccess(true, true, true, true);

        public void setAccess(RepositoryMethodAccess access) {
            this.access = access;
        }

        @Override
        public RelationshipRepositoryInformation build() {
            return new RelationshipRepositoryInformationImpl(matcher, access);
        }
    }

    public class DefaultResourceRepository implements ResourceRepositoryInformationBuilder {

        private ResourceInformation resourceInformation;

        private Map<String, RepositoryAction> actions = new HashMap<>();

        private RepositoryMethodAccess access = new RepositoryMethodAccess(true, true, true, true);

        private boolean exposed = true;

        @Override
        public void from(ResourceRepositoryInformation information) {
            actions.putAll(information.getActions());
            access = information.getAccess();
            exposed = information.isExposed();
            if (information.getResourceInformation().isPresent()) {
                resourceInformation = information.getResourceInformation().get();
            }
        }

        @Override
        public void setResourceInformation(ResourceInformation resourceInformation) {
            this.resourceInformation = resourceInformation;
        }

        @Override
        public void setAccess(RepositoryMethodAccess access) {
            this.access = access;
        }

        @Override
        public void setExposed(boolean exposed) {
            this.exposed = exposed;
        }

        public ResourceRepositoryInformation build() {
            return new ResourceRepositoryInformationImpl(resourceInformation.getResourceType(),
                    resourceInformation, actions, access, exposed);
        }
    }

    public class DefaultResource implements ResourceInformationBuilder {

        private List<DefaultField> fields = new ArrayList<>();

        private Type implementationType;

        private String resourceType;

        private String resourcePath;

        private String superResourceType;

        private StringMapper idStringMapper;

        private ResourceValidator validator;

        private Class<? extends PagingSpec> pagingSpecType;

        private VersionRange versionRange = VersionRange.UNBOUNDED;

        private ResourceFieldAccess access = new ResourceFieldAccess(true, true, true, true, true, true);

        @Override
        public void from(ResourceInformation information) {
            implementationType = information.getImplementationType();
            resourceType = information.getResourceType();
            resourcePath = information.getResourcePath();
            superResourceType = information.getSuperResourceType();
            idStringMapper = information.getIdStringMapper();
            validator = information.getValidator();
            access = information.getAccess();
            versionRange = information.getVersionRange();
            for (ResourceField fromField : information.getFields()) {
                DefaultField field = new DefaultField();
                field.from(fromField);
                fields.add(field);
            }
            pagingSpecType = information.getPagingSpecType();
        }

        @Override
        public void setAccess(ResourceFieldAccess access) {
            this.access = access;
        }

        @Override
        public DefaultField addField() {
            DefaultField field = new DefaultField();
            fields.add(field);
            return field;
        }

        @Override
        public DefaultField addField(String name, ResourceFieldType type, Class<?> clazz) {
            DefaultField field = new DefaultField();
            field.jsonName(name);
            field.underlyingName(name);
            field.type(clazz);
            field.genericType(clazz);
            field.fieldType(type);
            fields.add(field);
            return field;
        }

        @Override
        public DefaultResource implementationType(Type implementationType) {
            this.implementationType = implementationType;
            return this;
        }

        @Override
        public DefaultResource resourceType(String resourceType) {
            this.resourceType = resourceType;
            return this;
        }

        @Override
        public DefaultResource resourcePath(String resourcePath) {
            this.resourcePath = resourcePath;
            return this;
        }

        @Override
        public DefaultResource superResourceType(String superResourceType) {
            this.superResourceType = superResourceType;
            return this;
        }

        public ResourceInformationBuilder pagingSpecType(Class<? extends PagingSpec> pagingSpecType) {
            this.pagingSpecType = pagingSpecType;
            return this;
        }

        @Override
        public ResourceInformationBuilder versionRange(VersionRange versionRange) {
            this.versionRange = versionRange;
            return this;
        }

        public ResourceInformation build() {

            List<ResourceField> fieldImpls = new ArrayList<>();
            for (DefaultField field : fields) {
                fieldImpls.add(field.build());
            }

            ResourceInformation information =
                    new ResourceInformation(typeParser, implementationType, resourceType, resourcePath, superResourceType,
                            fieldImpls, pagingSpecType);
            information.setAccess(access);
            information.setVersionRange(versionRange);
            if (validator != null) {
                information.setValidator(validator);
            }
            if (idStringMapper != null) {
                information.setIdStringMapper(idStringMapper);
            }
            return information;
        }
    }

    public class DefaultEmbeddableInformation implements EmbeddableInformationBuilder {

        private Type implementationType;

        private List<DefaultField> fields = new ArrayList<>();

        public EmbeddableInformation build() {
            EmbeddableInformation information = new EmbeddableInformation(implementationType, fields.stream().map(it -> it.build()).collect(Collectors.toList()));
            return information;
        }

        @Override
        public void from(EmbeddableInformation information) {
            implementationType = information.getImplementationType();
            for (ResourceField fromField : information.getFields()) {
                DefaultField field = new DefaultField();
                field.from(fromField);
                fields.add(field);
            }
        }

        @Override
        public DefaultField addField() {
            DefaultField field = new DefaultField();
            fields.add(field);
            return field;
        }

        @Override
        public DefaultField addField(String name, ResourceFieldType type, Class<?> clazz) {
            DefaultField field = new DefaultField();
            field.jsonName(name);
            field.underlyingName(name);
            field.type(clazz);
            field.genericType(clazz);
            field.fieldType(type);
            fields.add(field);
            return field;
        }

        @Override
        public EmbeddableInformationBuilder implementationType(Type implementationType) {
            this.implementationType = implementationType;
            return this;
        }
    }

    public class DefaultField implements FieldInformationBuilder {

        private String jsonName;

        private String underlyingName;

        private Class<?> type;

        private Type genericType;

        private String oppositeResourceType = null;

        private LookupIncludeBehavior lookupIncludeBehavior = LookupIncludeBehavior.DEFAULT;

        private ResourceFieldType fieldType = ResourceFieldType.ATTRIBUTE;

        private SerializeType serializeType = SerializeType.LAZY;

        private JsonIncludeStrategy jsonIncludeStrategy = JsonIncludeStrategy.DEFAULT;

        private VersionRange versionRange = VersionRange.UNBOUNDED;

        private String oppositeName;

        private ResourceFieldAccessor accessor;

        private String idName;

        private Class idType;

        private ResourceFieldAccessor idAccessor;

        private ResourceFieldAccess access = new ResourceFieldAccess(true, true, true, true, true, true);

        private RelationshipRepositoryBehavior relationshipRepositoryBehavior = RelationshipRepositoryBehavior.DEFAULT;

        private PatchStrategy patchStrategy = PatchStrategy.DEFAULT;

        private boolean mappedBy;

        private DefaultEmbeddableInformation embeddedTypeBuilder;

        @Override
        public void from(ResourceField field) {
            jsonName = field.getJsonName();
            underlyingName = field.getUnderlyingName();
            type = field.getType();
            genericType = field.getGenericType();
            fieldType = field.getResourceFieldType();
            accessor = field.getAccessor();
            access = field.getAccess();
            serializeType = field.getSerializeType();
            jsonIncludeStrategy = field.getJsonIncludeStrategy();
            versionRange = field.getVersionRange();
            mappedBy = field.isMappedBy();
            embeddedTypeBuilder = toBuilder(field.getEmbeddedType());
            if (fieldType == ResourceFieldType.RELATIONSHIP) {
                relationshipRepositoryBehavior = field.getRelationshipRepositoryBehavior();
                oppositeResourceType = field.getOppositeResourceType();
                lookupIncludeBehavior = field.getLookupIncludeBehavior();
                oppositeName = field.getOppositeName();
                if (field.hasIdField()) {
                    idName = field.getIdName();
                    idType = field.getIdType();
                    idAccessor = field.getIdAccessor();
                }
            }
            patchStrategy = field.getPatchStrategy();
        }

        private DefaultEmbeddableInformation toBuilder(EmbeddableInformation type) {
            if (type == null) {
                return null;
            }
            embeddedTypeBuilder = new DefaultEmbeddableInformation();
            embeddedTypeBuilder.implementationType(type.getImplementationType());
            for(ResourceField field : type.getFields()){
                embeddedTypeBuilder.addField().from(field);
            }
            return embeddedTypeBuilder;
        }


        public ResourceField build() {
            if (oppositeResourceType == null && fieldType == ResourceFieldType.RELATIONSHIP) {
                // TODO consider separating informationBuilder from resourceType extraction
                Class<?> elementType = ClassUtils.getRawType(ClassUtils.getElementType(genericType));
                JsonApiResource annotation = elementType.getAnnotation(JsonApiResource.class);
                if (annotation != null) {
                    oppositeResourceType = annotation.type();
                }
            }

            ResourceFieldImpl impl = new ResourceFieldImpl(jsonName, underlyingName, fieldType, type,
                    genericType, oppositeResourceType, oppositeName, serializeType, jsonIncludeStrategy,
                    lookupIncludeBehavior,
                    access, idName, idType, idAccessor, relationshipRepositoryBehavior, this.patchStrategy);
            impl.setMappedBy(mappedBy);
            impl.setVersionRange(versionRange);
            if (embeddedTypeBuilder != null) {
                impl.setEmbeddedType(embeddedTypeBuilder.build());
            }
            if (accessor != null) {
                impl.setAccessor(accessor);
            }
            return impl;
        }

        @Override
        public DefaultField name(String name) {
            this.jsonName = name;
            this.underlyingName = name;
            return this;
        }

        @Override
        public DefaultField relationshipRepositoryBehavior(
                RelationshipRepositoryBehavior relationshipRepositoryBehavior) {
            this.relationshipRepositoryBehavior = relationshipRepositoryBehavior;
            return this;
        }


        @Override
        public DefaultField jsonName(String jsonName) {
            this.jsonName = jsonName;
            return this;
        }

        @Override
        public DefaultField underlyingName(String underlyingName) {
            this.underlyingName = underlyingName;
            return this;
        }

        @Override
        public DefaultField type(Class<?> type) {
            this.type = type;
            if (this.genericType == null) {
                this.genericType = type;
            }
            return this;
        }

        @Override
        public EmbeddableInformationBuilder embeddedType(Class<?> type) {
        	if(genericType == null) {
				genericType(type);
			}
            if (embeddedTypeBuilder == null) {
                embeddedTypeBuilder = new DefaultEmbeddableInformation();
                embeddedTypeBuilder.implementationType(type);
            }
            return embeddedTypeBuilder;
        }

        @Override
        public DefaultField genericType(Type genericType) {
            this.genericType = genericType;
            if (type == null) {
                type = ClassUtils.getRawType(genericType);
            }
            return this;
        }

        @Override
        public DefaultField serializeType(SerializeType serializeType) {
            this.serializeType = serializeType;
            return this;
        }

        @Override
        public FieldInformationBuilder jsonIncludeStrategy(JsonIncludeStrategy jsonIncludeStrategy) {
            this.jsonIncludeStrategy = jsonIncludeStrategy;
            return this;
        }

        @Override
        public DefaultField oppositeResourceType(String oppositeResourceType) {
            this.oppositeResourceType = oppositeResourceType;
            return this;
        }

        @Override
        public DefaultField lookupIncludeBehavior(LookupIncludeBehavior lookupIncludeBehavior) {
            this.lookupIncludeBehavior = lookupIncludeBehavior;
            return this;
        }

        @Override
        public DefaultField fieldType(ResourceFieldType fieldType) {
            this.fieldType = fieldType;
            return this;
        }

        @Override
        public DefaultField oppositeName(String oppositeName) {
            this.oppositeName = oppositeName;
            return this;
        }

        @Override
        public DefaultField accessor(ResourceFieldAccessor accessor) {
            this.accessor = accessor;
            return this;
        }

        @Override
        public DefaultField idAccessor(ResourceFieldAccessor idAccessor) {
            this.idAccessor = idAccessor;
            return this;
        }

        @Override
        public DefaultField idName(String idName) {
            this.idName = idName;
            return this;
        }

        @Override
        public DefaultField idType(Class idType) {
            this.idType = idType;
            return this;
        }

        @Override
        public FieldInformationBuilder patchStrategy(PatchStrategy patchStrategy) {
            this.patchStrategy = patchStrategy;
            return this;
        }

        @Override
        public FieldInformationBuilder setMappedBy(boolean mappedBy) {
            this.mappedBy = mappedBy;
            return this;
        }

        @Override
        public FieldInformationBuilder versionRange(VersionRange versionRange) {
            this.versionRange = versionRange;
            return this;
        }

        @Override
        public DefaultField access(ResourceFieldAccess access) {
            this.access = access;
            return this;
        }
    }


    public DefaultInformationBuilder(TypeParser typeParser) {
        this.typeParser = typeParser;
    }
}
