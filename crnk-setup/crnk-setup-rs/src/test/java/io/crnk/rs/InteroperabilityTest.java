package io.crnk.rs;

import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.filter.DocumentFilter;
import io.crnk.core.engine.filter.DocumentFilterChain;
import io.crnk.core.engine.filter.DocumentFilterContext;
import io.crnk.core.engine.internal.dispatcher.path.ActionPath;
import io.crnk.core.module.SimpleModule;
import io.crnk.rs.controller.SampleControllerWithPrefix;
import io.crnk.test.JerseyTestBase;
import io.crnk.test.mock.TestModule;
import io.crnk.test.mock.repository.ScheduleRepositoryImpl;
import io.crnk.test.mock.repository.TaskRepository;
import io.restassured.RestAssured;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.test.jetty.JettyTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

public class InteroperabilityTest extends JerseyTestBase {

    private static DocumentFilter filter;

    private static CrnkFeature feature;


    @Override
    protected TestContainerFactory getTestContainerFactory() {
        return new JettyTestContainerFactory();
    }


    @Override
    protected Application configure() {
        return new TestApplication();
    }


    @After
    public void tearDown() throws Exception {
        super.tearDown();
        TaskRepository.clear();
        ScheduleRepositoryImpl.clear();
    }


    @ApplicationPath("/")
    private static class TestApplication extends ResourceConfig {


        public TestApplication() {
            register(SampleControllerWithPrefix.class);

            feature = new CrnkFeature();
            feature.addModule(new TestModule());
            register(feature);

            register(new JsonApiResponseFilter(feature));
            register(new JsonapiExceptionMapperBridge(feature));

            filter = Mockito.spy(new DocumentFilter() {

                @Override
                public Response filter(DocumentFilterContext filterRequestContext, DocumentFilterChain chain) {
                    return chain.doFilter(filterRequestContext);
                }
            });
            SimpleModule testModule = new SimpleModule("testFilter");
            testModule.addFilter(filter);
            feature.addModule(testModule);
        }
    }


    @Test
    public void testInvokeRepositoryActionWithResourceResult() {
        // resources should be received in json api format
        String url = getBaseUri() + "schedules/repositoryActionWithResourceResult?msg=hello";
        io.restassured.response.Response res = RestAssured.get(url);
        Assert.assertEquals(200, res.getStatusCode());
        res.then().assertThat().body("data.attributes.name", Matchers.equalTo("hello"));

        // check filters
        ArgumentCaptor<DocumentFilterContext> contexts = ArgumentCaptor.forClass(DocumentFilterContext.class);
        Mockito.verify(filter, Mockito.times(1)).filter(contexts.capture(), Mockito.any(DocumentFilterChain.class));
        DocumentFilterContext actionContext = contexts.getAllValues().get(0);
        Assert.assertEquals("GET", actionContext.getMethod());
        Assert.assertTrue(actionContext.getJsonPath() instanceof ActionPath);
    }

    @Test
    public void testInvokeRepositoryActionWithException() {
        // resources should be received in json api format
        String url = getBaseUri() + "schedules/repositoryActionWithException?msg=hello";
        io.restassured.response.Response res = RestAssured.get(url);
        Assert.assertEquals(403, res.getStatusCode());

        res.then().assertThat().body("errors[0].status", Matchers.equalTo("403"));

        // check filters
        ArgumentCaptor<DocumentFilterContext> contexts = ArgumentCaptor.forClass(DocumentFilterContext.class);
        Mockito.verify(filter, Mockito.times(1)).filter(contexts.capture(), Mockito.any(DocumentFilterChain.class));
        DocumentFilterContext actionContext = contexts.getAllValues().get(0);
        Assert.assertEquals("GET", actionContext.getMethod());
        Assert.assertTrue(actionContext.getJsonPath() instanceof ActionPath);
    }

    @Test
    public void testUnknownExceptionsGetMappedToInternalServerException() {
        JsonapiExceptionMapperBridge bridge = new JsonapiExceptionMapperBridge(feature);
        javax.ws.rs.core.Response response = bridge.toResponse(new CustomException());
        Assert.assertEquals(500, response.getStatus());
        Assert.assertTrue(response.getEntity() instanceof Document);
    }

    class CustomException extends RuntimeException {

    }

}
