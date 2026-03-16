package io.crnk.core.queryspec.pagingspec;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.core.engine.dispatcher.RepositoryRequestSpec;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.information.resource.ResourceFieldImpl;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.mock.models.Task;
import io.crnk.core.module.ModuleRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.QuerySpecAdapter;
import io.crnk.core.queryspec.mapper.DefaultQuerySpecUrlMapper;
import org.mockito.Mockito;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PagingSpecUrlBuilderTest {

    @Mock
    private RepositoryRequestSpec repositoryRequestSpec;

    @Mock
    private ResourceRegistry resourceRegistry;

    private ModuleRegistry moduleRegistry;

    @Before
    public void setUp() {
        ResourceInformation resourceInformation = mock(ResourceInformation.class);
        RegistryEntry registryEntry = mock(RegistryEntry.class);
        Mockito.when(resourceInformation.getImplementationClass()).thenReturn((Class) Task.class);
        when(registryEntry.getResourceInformation()).thenReturn(resourceInformation);
        when(resourceRegistry.getResourceUrl(any(QueryContext.class), any(ResourceInformation.class), any())).thenReturn("tasks");
        when(resourceRegistry.getEntry(any(Class.class))).thenReturn(registryEntry);
        when(repositoryRequestSpec.getId()).thenReturn(1);
        ResourceFieldImpl relationshipField = new ResourceFieldImpl("any", "any", ResourceFieldType.ATTRIBUTE, String.class, String.class, null);
        relationshipField.setResourceInformation(resourceInformation);
        when(repositoryRequestSpec.getRelationshipField()).thenReturn(relationshipField);

        moduleRegistry = new ModuleRegistry();
        moduleRegistry.setUrlMapper(new DefaultQuerySpecUrlMapper());
        moduleRegistry.setResourceRegistry(resourceRegistry);
        moduleRegistry.init(new ObjectMapper());
    }

    @Test
    public void testSelfLink() {
        QueryContext queryContext = new QueryContext();
        queryContext.setBaseUrl("http://localhost");
        queryContext.setRequestPath("/relationships/self");
        QuerySpecAdapter adapter = new QuerySpecAdapter(new QuerySpec(Task.class), resourceRegistry, queryContext);

        PagingSpecUrlBuilder urlBuilder = new PagingSpecUrlBuilder(moduleRegistry, repositoryRequestSpec);
        Assert.assertEquals("tasks/relationships/any", urlBuilder.build(adapter));
    }

    @Test
    public void testRelatedLink() {
        QueryContext queryContext = new QueryContext();
        queryContext.setBaseUrl("http://localhost");
        queryContext.setRequestPath("/related");
        QuerySpecAdapter adapter = new QuerySpecAdapter(new QuerySpec(Task.class), resourceRegistry, queryContext);

        PagingSpecUrlBuilder urlBuilder = new PagingSpecUrlBuilder(moduleRegistry, repositoryRequestSpec);
        Assert.assertEquals("tasks/any", urlBuilder.build(adapter));
    }
}
