package io.crnk.ui.presentation;

import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.MetaModule;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;

import java.util.List;

/**
 * Represents a service the crnk-ui is connected to. Can either be a local instance by providing
 * {@link MetaModule} in the constructor or any repository for {@link MetaResource}, either local or remote
 * based on crnk-client.
 */
public class PresentationService {

    private ResourceRepository<MetaResource, String> repository;

    private String serviceName;

    private MetaLookup metaLookup;

    private String path;

    /**
     * Make use of local in-memory crnk instance
     */
    public PresentationService(String name, String path, MetaLookup metaLookup) {
        this.serviceName = name;
        this.path = path;
        this.metaLookup = metaLookup;
    }

    /**
     * Make use of a remove crnk instance.
     */
    public PresentationService(String name, String path, ResourceRepository<MetaResource, String> repository) {
        this.serviceName = name;
        this.path = path;
        this.repository = repository;
    }

    /**
     * @return name of the service
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * @return path to the service
     */
    public String getPath() {
        return path;
    }

    public MetaLookup getLookup() {
        if (metaLookup == null) {
            metaLookup = new RemoteMetaLookup(repository);
        }
        return metaLookup;
    }

    class RemoteMetaLookup implements MetaLookup {

        private final ResourceRepository<MetaResource, String> repository;

        public RemoteMetaLookup(ResourceRepository<MetaResource, String> repository) {
            this.repository = repository;
        }

        @Override
        public <T extends MetaElement> T findElement(Class<T> metaType, String id) {
            PreconditionUtil.verify(metaType.equals(MetaResource.class), "can only query resources");
            return (T) repository.findOne(id, createQuerySpec());
        }

        @Override
        public <T extends MetaElement> List<T> findElements(Class<T> metaType) {
            PreconditionUtil.verify(metaType.equals(MetaResource.class), "can only query resources");
            return (List<T>) repository.findAll(createQuerySpec());
        }

        @Override
        public <T extends MetaElement> T findElement(Class<T> metaType, Class<?> implementationClass) {
            throw new UnsupportedOperationException();
        }

        private QuerySpec createQuerySpec() {
            QuerySpec querySpec = new QuerySpec(MetaResource.class);
            querySpec.setLimit(1000L);
            querySpec.includeRelation(PathSpec.of("attributes"));

            QuerySpec typeSpec = new QuerySpec(MetaDataObject.class);
            typeSpec.includeRelation(PathSpec.of("attributes"));
            querySpec.putRelatedSpec(MetaDataObject.class, typeSpec);

            QuerySpec attrSpec = new QuerySpec(MetaAttribute.class);
            attrSpec.includeRelation(PathSpec.of("type"));
            querySpec.putRelatedSpec(MetaAttribute.class, attrSpec);

            QuerySpec fieldSpec = new QuerySpec(MetaResourceField.class);
            fieldSpec.includeRelation(PathSpec.of("type"));
            querySpec.putRelatedSpec(MetaResourceField.class, fieldSpec);
            return querySpec;
        }
    }
}