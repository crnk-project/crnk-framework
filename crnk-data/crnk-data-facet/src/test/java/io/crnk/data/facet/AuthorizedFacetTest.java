package io.crnk.data.facet;

import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.ResourceFilter;
import io.crnk.core.engine.filter.ResourceFilterBase;
import io.crnk.core.engine.filter.ResourceFilterContext;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.module.SimpleModule;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.resource.list.ResourceList;
import io.crnk.data.facet.setup.FacetTestSetup;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class AuthorizedFacetTest {

    @Test
    public void matchNotAuthorized() {
        check(false);
    }

    @Test
    public void matchAuthorized() {
        check(true);
    }

    private void check(boolean authorized) {
        ResourceFilter resourceFilter = Mockito.spy(new ResourceFilterBase());
        ResourceFilterContext anyFiltercontext = Mockito.any(ResourceFilterContext.class);
        Mockito.when(resourceFilter.filterResource(anyFiltercontext, Mockito.any(ResourceInformation.class), Mockito.any(HttpMethod.class)))
                .thenReturn(authorized ? FilterBehavior.NONE : FilterBehavior.FORBIDDEN);
        SimpleModule module = new SimpleModule("autz");
        module.addResourceFilter(resourceFilter);

        FacetTestSetup setup = new FacetTestSetup();
        setup.getBoot().addModule(module);
        setup.boot();

        QuerySpec querySpec = new QuerySpec(FacetResource.class);
        ResourceList<FacetResource> list = setup.getRepository().findAll(querySpec);
        Assert.assertEquals(authorized ? 2 : 0, list.size());
    }

}
