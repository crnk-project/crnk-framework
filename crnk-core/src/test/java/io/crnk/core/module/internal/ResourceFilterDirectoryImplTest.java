package io.crnk.core.module.internal;

import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.ResourceFilter;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.http.HttpRequestContext;
import io.crnk.core.engine.http.HttpRequestContextBase;
import io.crnk.core.engine.http.HttpRequestContextProvider;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.http.HttpRequestContextBaseAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.engine.result.ImmediateResultFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

public class ResourceFilterDirectoryImplTest {

    private HttpRequestContextProvider requestContextProvider;

    private List<ResourceFilter> filters = new ArrayList<>();

    private ResourceFilter filter;

    private ResourceFilterDirectoryImpl directory;

    private ResourceInformation resourceInformation;

    private ResourceField resourceField;

    private ResourceRegistry resourceRegistry;

    private QueryContext queryContext;

    private ImmediateResultFactory resultFactory;

    @Before
    public void setup() {
        queryContext = new QueryContext();

        resultFactory = new ImmediateResultFactory();
        requestContextProvider = new HttpRequestContextProvider(() -> resultFactory);

        filter = Mockito.mock(ResourceFilter.class);
        filters.add(filter);

        resourceInformation = Mockito.mock(ResourceInformation.class);
        resourceField = Mockito.mock(ResourceField.class);
        Mockito.when(resourceField.getAccess()).thenReturn(new ResourceFieldAccess(true, true, true, true, true, true));
        resourceRegistry = Mockito.mock(ResourceRegistry.class);

        directory = new ResourceFilterDirectoryImpl(filters, requestContextProvider, resourceRegistry);
    }


    @Test
    public void checkMergeResourceFilterWithNoneNone() {
        ResourceFilter filter2 = Mockito.mock(ResourceFilter.class);
        filters.add(filter2);

        setResourceBehavior(HttpMethod.GET, FilterBehavior.NONE);
        Mockito.when(filter2.filterResource(Mockito.eq(resourceInformation), Mockito.eq(HttpMethod.GET))).thenReturn(FilterBehavior.NONE);

        Assert.assertEquals(FilterBehavior.NONE, directory.get(resourceInformation, HttpMethod.GET, queryContext));
        Mockito.verify(filter, Mockito.times(1)).filterResource(Mockito.eq(resourceInformation), Mockito.any(HttpMethod.class));
        Mockito.verify(filter2, Mockito.times(1)).filterResource(Mockito.eq(resourceInformation), Mockito.any(HttpMethod.class));
    }

    @Test
    public void checkMergeResourceFilterWithNoneForbidden() {
        ResourceFilter filter2 = Mockito.mock(ResourceFilter.class);
        filters.add(filter2);

        setResourceBehavior(HttpMethod.GET, FilterBehavior.NONE);
        Mockito.when(filter2.filterResource(Mockito.eq(resourceInformation), Mockito.eq(HttpMethod.GET))).thenReturn(FilterBehavior.FORBIDDEN);

        Assert.assertEquals(FilterBehavior.FORBIDDEN, directory.get(resourceInformation, HttpMethod.GET, queryContext));
        Mockito.verify(filter, Mockito.times(1)).filterResource(Mockito.eq(resourceInformation), Mockito.any(HttpMethod.class));
        Mockito.verify(filter2, Mockito.times(1)).filterResource(Mockito.eq(resourceInformation), Mockito.any(HttpMethod.class));
    }

    @Test
    public void checkMergeResourceFilterWithIgnoredForbidden() {
        ResourceFilter filter2 = Mockito.mock(ResourceFilter.class);
        filters.add(filter2);

        setResourceBehavior(HttpMethod.GET, FilterBehavior.IGNORED);
        Mockito.when(filter2.filterResource(Mockito.eq(resourceInformation), Mockito.eq(HttpMethod.GET))).thenReturn(FilterBehavior.FORBIDDEN);

        Assert.assertEquals(FilterBehavior.FORBIDDEN, directory.get(resourceInformation, HttpMethod.GET, queryContext));
        Mockito.verify(filter, Mockito.times(1)).filterResource(Mockito.eq(resourceInformation), Mockito.any(HttpMethod.class));
        Mockito.verify(filter2, Mockito.times(1)).filterResource(Mockito.eq(resourceInformation), Mockito.any(HttpMethod.class));
    }

    @Test
    public void checkMergeResourceFilterWithIgnoredNone() {
        ResourceFilter filter2 = Mockito.mock(ResourceFilter.class);
        filters.add(filter2);

        setResourceBehavior(HttpMethod.GET, FilterBehavior.IGNORED);
        Mockito.when(filter2.filterResource(Mockito.eq(resourceInformation), Mockito.eq(HttpMethod.GET))).thenReturn(FilterBehavior.NONE);

        Assert.assertEquals(FilterBehavior.IGNORED, directory.get(resourceInformation, HttpMethod.GET, queryContext));
        Mockito.verify(filter, Mockito.times(1)).filterResource(Mockito.eq(resourceInformation), Mockito.any(HttpMethod.class));
        Mockito.verify(filter2, Mockito.times(1)).filterResource(Mockito.eq(resourceInformation), Mockito.any(HttpMethod.class));
    }

    @Test
    public void checkMergeResourceFilterWithForbiddenNone() {
        ResourceFilter filter2 = Mockito.mock(ResourceFilter.class);
        filters.add(filter2);

        setResourceBehavior(HttpMethod.GET, FilterBehavior.FORBIDDEN);
        Mockito.when(filter2.filterResource(Mockito.eq(resourceInformation), Mockito.eq(HttpMethod.GET))).thenReturn(FilterBehavior.NONE);

        Assert.assertEquals(FilterBehavior.FORBIDDEN, directory.get(resourceInformation, HttpMethod.GET, queryContext));
        Mockito.verify(filter, Mockito.times(1)).filterResource(Mockito.eq(resourceInformation), Mockito.any(HttpMethod.class));
        // loop breaks after first filter as FORBIDDEN is hardest filter
        Mockito.verify(filter2, Mockito.times(0)).filterResource(Mockito.eq(resourceInformation), Mockito.any(HttpMethod.class));
    }


    @Test
    public void checkMergeFieldFilterWithNoneNone() {
        ResourceFilter filter2 = Mockito.mock(ResourceFilter.class);
        filters.add(filter2);

        setFieldBehavior(HttpMethod.GET, FilterBehavior.NONE);
        Mockito.when(filter2.filterField(Mockito.eq(resourceField), Mockito.eq(HttpMethod.GET))).thenReturn(FilterBehavior.NONE);

        Assert.assertEquals(FilterBehavior.NONE, directory.get(resourceField, HttpMethod.GET, queryContext));
        Mockito.verify(filter, Mockito.times(1)).filterField(Mockito.eq(resourceField), Mockito.any(HttpMethod.class));
        Mockito.verify(filter2, Mockito.times(1)).filterField(Mockito.eq(resourceField), Mockito.any(HttpMethod.class));
    }

    @Test
    public void checkMergeFieldFilterWithNoneForbidden() {
        ResourceFilter filter2 = Mockito.mock(ResourceFilter.class);
        filters.add(filter2);

        setFieldBehavior(HttpMethod.GET, FilterBehavior.NONE);
        Mockito.when(filter2.filterField(Mockito.eq(resourceField), Mockito.eq(HttpMethod.GET))).thenReturn(FilterBehavior.FORBIDDEN);

        Assert.assertEquals(FilterBehavior.FORBIDDEN, directory.get(resourceField, HttpMethod.GET, queryContext));
        Mockito.verify(filter, Mockito.times(1)).filterField(Mockito.eq(resourceField), Mockito.any(HttpMethod.class));
        Mockito.verify(filter2, Mockito.times(1)).filterField(Mockito.eq(resourceField), Mockito.any(HttpMethod.class));
    }

    @Test
    public void checkMergeFieldFilterWithIgnoredForbidden() {
        ResourceFilter filter2 = Mockito.mock(ResourceFilter.class);
        filters.add(filter2);

        setFieldBehavior(HttpMethod.GET, FilterBehavior.IGNORED);
        Mockito.when(filter2.filterField(Mockito.eq(resourceField), Mockito.eq(HttpMethod.GET))).thenReturn(FilterBehavior.FORBIDDEN);

        Assert.assertEquals(FilterBehavior.FORBIDDEN, directory.get(resourceField, HttpMethod.GET, queryContext));
        Mockito.verify(filter, Mockito.times(1)).filterField(Mockito.eq(resourceField), Mockito.any(HttpMethod.class));
        Mockito.verify(filter2, Mockito.times(1)).filterField(Mockito.eq(resourceField), Mockito.any(HttpMethod.class));
    }

    @Test
    public void checkMergeFieldFilterWithIgnoredNone() {
        ResourceFilter filter2 = Mockito.mock(ResourceFilter.class);
        filters.add(filter2);

        setFieldBehavior(HttpMethod.GET, FilterBehavior.IGNORED);
        Mockito.when(filter2.filterField(Mockito.eq(resourceField), Mockito.eq(HttpMethod.GET))).thenReturn(FilterBehavior.NONE);

        Assert.assertEquals(FilterBehavior.IGNORED, directory.get(resourceField, HttpMethod.GET, queryContext));
        Mockito.verify(filter, Mockito.times(1)).filterField(Mockito.eq(resourceField), Mockito.any(HttpMethod.class));
        Mockito.verify(filter2, Mockito.times(1)).filterField(Mockito.eq(resourceField), Mockito.any(HttpMethod.class));
    }

    @Test
    public void checkMergeFieldFilterWithForbiddenNone() {
        ResourceFilter filter2 = Mockito.mock(ResourceFilter.class);
        filters.add(filter2);

        setFieldBehavior(HttpMethod.GET, FilterBehavior.FORBIDDEN);
        Mockito.when(filter2.filterField(Mockito.eq(resourceField), Mockito.eq(HttpMethod.GET))).thenReturn(FilterBehavior.NONE);

        Assert.assertEquals(FilterBehavior.FORBIDDEN, directory.get(resourceField, HttpMethod.GET, queryContext));
        Mockito.verify(filter, Mockito.times(1)).filterField(Mockito.eq(resourceField), Mockito.any(HttpMethod.class));
        // loop breaks after first filter as FORBIDDEN is hardest filter
        Mockito.verify(filter2, Mockito.times(0)).filterField(Mockito.eq(resourceField), Mockito.any(HttpMethod.class));
    }

    @Test
    public void checkForbiddenRelationshipsField() {
        ResourceInformation projectsInformation = Mockito.mock(ResourceInformation.class);
        Mockito.when(filter.filterField(Mockito.eq(resourceField), Mockito.any(HttpMethod.class))).thenReturn(FilterBehavior.NONE);

        Mockito.when(resourceField.getResourceFieldType()).thenReturn(ResourceFieldType.RELATIONSHIP);
        Mockito.when(resourceField.getOppositeResourceType()).thenReturn("projects");

        RegistryEntry projectsEntry = Mockito.mock(RegistryEntry.class);
        Mockito.when(projectsEntry.getResourceInformation()).thenReturn(projectsInformation);
        Mockito.when(resourceRegistry.getEntry(Mockito.eq("projects"))).thenReturn(projectsEntry);

        // forbid related resource
        Mockito.when(filter.filterResource(Mockito.eq(projectsInformation), Mockito.any(HttpMethod.class))).thenReturn(FilterBehavior.FORBIDDEN);
        Assert.assertEquals(FilterBehavior.FORBIDDEN, directory.get(resourceField, HttpMethod.GET, queryContext));

        // allow related resource
        invalidateCache();
        Mockito.when(filter.filterResource(Mockito.eq(projectsInformation), Mockito.any(HttpMethod.class))).thenReturn(FilterBehavior.NONE);
        Assert.assertEquals(FilterBehavior.NONE, directory.get(resourceField, HttpMethod.GET, queryContext));
    }


    @Test
    public void testFieldCaching() {
        setFieldBehavior(HttpMethod.GET, FilterBehavior.NONE);

        Assert.assertEquals(FilterBehavior.NONE, directory.get(resourceField, HttpMethod.GET, queryContext));
        Mockito.verify(filter, Mockito.times(1)).filterField(Mockito.eq(resourceField), Mockito.eq(HttpMethod.GET));

        // second call is cached, no change
        Assert.assertEquals(FilterBehavior.NONE, directory.get(resourceField, HttpMethod.GET, queryContext));
        Mockito.verify(filter, Mockito.times(1)).filterField(Mockito.eq(resourceField), Mockito.eq(HttpMethod.GET));

        // caching not impacted when changing filter behavior
        setFieldBehavior(HttpMethod.GET, FilterBehavior.FORBIDDEN);
        Assert.assertEquals(FilterBehavior.NONE, directory.get(resourceField, HttpMethod.GET, queryContext));
        Mockito.verify(filter, Mockito.times(1)).filterField(Mockito.eq(resourceField), Mockito.eq(HttpMethod.GET));

        // start new request, get new filter behavior
        invalidateCache();
        Assert.assertEquals(FilterBehavior.FORBIDDEN, directory.get(resourceField, HttpMethod.GET, queryContext));
        Mockito.verify(filter, Mockito.times(2)).filterField(Mockito.eq(resourceField), Mockito.eq(HttpMethod.GET));
    }

    @Test
    public void testResourceCaching() {
        requestContextProvider.onRequestStarted(newRewRequestContext());
        setResourceBehavior(HttpMethod.GET, FilterBehavior.NONE);

        Assert.assertEquals(FilterBehavior.NONE, directory.get(resourceInformation, HttpMethod.GET, queryContext));
        Mockito.verify(filter, Mockito.times(1)).filterResource(Mockito.eq(resourceInformation), Mockito.eq(HttpMethod.GET));

        // second call is cached, no change
        Assert.assertEquals(FilterBehavior.NONE, directory.get(resourceInformation, HttpMethod.GET, queryContext));
        Mockito.verify(filter, Mockito.times(1)).filterResource(Mockito.eq(resourceInformation), Mockito.eq(HttpMethod.GET));

        // caching not impacted when changing filter behavior
        setResourceBehavior(HttpMethod.GET, FilterBehavior.FORBIDDEN);
        Assert.assertEquals(FilterBehavior.NONE, directory.get(resourceInformation, HttpMethod.GET, queryContext));
        Mockito.verify(filter, Mockito.times(1)).filterResource(Mockito.eq(resourceInformation), Mockito.eq(HttpMethod.GET));

        // start new request, get new filter behavior
        invalidateCache();
        requestContextProvider.onRequestFinished();
        requestContextProvider.onRequestStarted(newRewRequestContext());
        Assert.assertEquals(FilterBehavior.FORBIDDEN, directory.get(resourceInformation, HttpMethod.GET, queryContext));
        Mockito.verify(filter, Mockito.times(2)).filterResource(Mockito.eq(resourceInformation), Mockito.eq(HttpMethod.GET));
    }


    @Test
    public void checkPatchableField() {
        Mockito.when(filter.filterField(Mockito.any(ResourceField.class), Mockito.any(HttpMethod.class))).thenReturn(FilterBehavior.NONE);
        ResourceField field = Mockito.mock(ResourceField.class);
        Mockito.when(field.getAccess()).thenReturn(new ResourceFieldAccess(true, true, true, true, true, true));
        Assert.assertTrue(directory.canAccess(field, HttpMethod.PATCH, queryContext, true));
    }

    @Test
    public void checkNonPatchableField() {
        Mockito.when(filter.filterField(Mockito.any(ResourceField.class), Mockito.any(HttpMethod.class))).thenReturn(FilterBehavior.NONE);
        ResourceField field = Mockito.mock(ResourceField.class);
        Mockito.when(field.getAccess()).thenReturn(new ResourceFieldAccess(true, true, false, true, true, true));
        Assert.assertFalse(directory.canAccess(field, HttpMethod.PATCH, queryContext, true));
    }


    @Test
    public void checkPostableField() {
        Mockito.when(filter.filterField(Mockito.any(ResourceField.class), Mockito.any(HttpMethod.class))).thenReturn(FilterBehavior.NONE);
        ResourceField field = Mockito.mock(ResourceField.class);
        Mockito.when(field.getAccess()).thenReturn(new ResourceFieldAccess(true, true, true, true, true, true));
        Assert.assertTrue(directory.canAccess(field, HttpMethod.POST, queryContext, true));
    }

    @Test
    public void checkNonPostableField() {
        Mockito.when(filter.filterField(Mockito.any(ResourceField.class), Mockito.any(HttpMethod.class))).thenReturn(FilterBehavior.NONE);
        ResourceField field = Mockito.mock(ResourceField.class);
        Mockito.when(field.getAccess()).thenReturn(new ResourceFieldAccess(true, false, true, true, true, true));
        Assert.assertFalse(directory.canAccess(field, HttpMethod.POST, queryContext, true));
    }

    @Test
    public void checkDeletableField() {
        Mockito.when(filter.filterField(Mockito.any(ResourceField.class), Mockito.any(HttpMethod.class))).thenReturn(FilterBehavior.NONE);
        ResourceField field = Mockito.mock(ResourceField.class);
        Mockito.when(field.getAccess()).thenReturn(new ResourceFieldAccess(true, true, true, true, true, true));
        Assert.assertTrue(directory.canAccess(field, HttpMethod.DELETE, queryContext, true));
    }

    @Test
    public void checkNonDeletableField() {
        Mockito.when(filter.filterField(Mockito.any(ResourceField.class), Mockito.any(HttpMethod.class))).thenReturn(FilterBehavior.NONE);
        ResourceField field = Mockito.mock(ResourceField.class);
        Mockito.when(field.getAccess()).thenReturn(new ResourceFieldAccess(true, true, true, false, true, true));
        Assert.assertFalse(directory.canAccess(field, HttpMethod.DELETE, queryContext, true));
    }

    private void invalidateCache() {
        queryContext.getAttributes().clear();
    }

    private HttpRequestContext newRewRequestContext() {
        return new HttpRequestContextBaseAdapter(Mockito.mock(HttpRequestContextBase.class));
    }

    private void setFieldBehavior(HttpMethod method, FilterBehavior behavior) {
        Mockito.when(filter.filterField(Mockito.eq(resourceField), Mockito.eq(method))).thenReturn(behavior);
    }

    private void setResourceBehavior(HttpMethod method, FilterBehavior behavior) {
        Mockito.when(filter.filterResource(Mockito.eq(resourceInformation), Mockito.eq(method))).thenReturn(behavior);
    }
}
