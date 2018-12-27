package io.crnk.core.engine.internal.dispatcher.controller;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldAccess;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.properties.NullPropertiesProvider;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.result.Result;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class BaseControllerTest {

    private BaseController controller = new BaseController() {
        @Override
        public boolean isAcceptable(JsonPath jsonPath, String method) {
            return false;
        }

        @Override
        public Result<Response> handleAsync(JsonPath jsonPath, QueryAdapter queryAdapter, Document requestDocument) {
            return null;
        }
    };

    private QueryContext queryContext = Mockito.mock(QueryContext.class);

    @Before
    public void setup() {
        ControllerContext context = Mockito.mock(ControllerContext.class);
        ResourceFilterDirectory filterDirectory = Mockito.mock(ResourceFilterDirectory.class);
        Mockito.when(filterDirectory.get(Mockito.any(ResourceField.class), Mockito.any(HttpMethod.class), Mockito.eq(queryContext))).thenReturn(FilterBehavior.NONE);
        Mockito.when(context.getResourceFilterDirectory()).thenReturn(filterDirectory);
        Mockito.when(context.getPropertiesProvider()).thenReturn(new NullPropertiesProvider());
        controller.init(context);
    }

}
