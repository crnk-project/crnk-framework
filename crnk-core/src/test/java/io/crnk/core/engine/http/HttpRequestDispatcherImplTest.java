package io.crnk.core.engine.http;

import io.crnk.core.CoreTestContainer;
import io.crnk.core.CoreTestModule;
import io.crnk.core.engine.dispatcher.RequestDispatcher;
import io.crnk.core.engine.dispatcher.Response;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.filter.AbstractDocumentFilter;
import io.crnk.core.engine.filter.DocumentFilter;
import io.crnk.core.engine.filter.DocumentFilterChain;
import io.crnk.core.engine.filter.DocumentFilterContext;
import io.crnk.core.engine.information.repository.RepositoryAction;
import io.crnk.core.engine.information.repository.RepositoryInformation;
import io.crnk.core.engine.information.repository.RepositoryInformationProvider;
import io.crnk.core.engine.information.repository.RepositoryInformationProviderContext;
import io.crnk.core.engine.information.repository.RepositoryMethodAccess;
import io.crnk.core.engine.information.resource.ResourceAction;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.dispatcher.ControllerRegistry;
import io.crnk.core.engine.internal.dispatcher.controller.CollectionGetController;
import io.crnk.core.engine.internal.dispatcher.controller.RelationshipsResourceGetController;
import io.crnk.core.engine.internal.dispatcher.path.ActionPath;
import io.crnk.core.engine.internal.dispatcher.path.JsonPath;
import io.crnk.core.engine.internal.exception.ExceptionMapperRegistryTest;
import io.crnk.core.engine.internal.http.HttpRequestContextBaseAdapter;
import io.crnk.core.engine.internal.http.HttpRequestDispatcherImpl;
import io.crnk.core.engine.internal.http.JsonApiRequestProcessor;
import io.crnk.core.engine.internal.information.repository.ResourceRepositoryInformationImpl;
import io.crnk.core.engine.query.QueryAdapter;
import io.crnk.core.engine.result.ImmediateResult;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.module.Module;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiResource;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class HttpRequestDispatcherImplTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    private DocumentFilter documentFilter = Mockito.spy(AbstractDocumentFilter.class);

    private JsonApiRequestProcessor requestProcessor;

    private CoreTestContainer container;

    @Before
    public void prepare() {
        container = new CoreTestContainer();
        container.addModule(new CoreTestModule());
        container.addModule(new ActionTestModule());
        container.boot();
        requestProcessor = (JsonApiRequestProcessor) container.getModuleRegistry().getHttpRequestProcessors().get(0);
    }

    @JsonApiResource(type = "actionResource")
    public static class ActionResource {

        @JsonApiId
        private String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    /**
     * Testing for a repository with actions (currently the only actual implementation is with JAXRS).
     */
    class ActionTestModule implements Module {

        @Override
        public String getModuleName() {
            return "actionTest";
        }

        @Override
        public void setupModule(ModuleContext context) {

            final ResourceRepository mockRepository = Mockito.mock(ResourceRepository.class);
            Mockito.when(mockRepository.getResourceClass()).thenReturn(ActionResource.class);

            context.addFilter(documentFilter);
            context.addRepository(mockRepository);

            context.addRepositoryInformationBuilder(new RepositoryInformationProvider() {
                @Override
                public boolean accept(Class<?> repositoryClass) {
                    return false;
                }

                @Override
                public boolean accept(Object repository) {
                    return repository == mockRepository;
                }

                @Override
                public RepositoryInformation build(Object repository, RepositoryInformationProviderContext context) {

                    ResourceInformation resourceInformation = context.getResourceInformationBuilder().build(ActionResource
                            .class);

                    HashMap<String, RepositoryAction> actions = new HashMap<>();
                    ResourceAction action = Mockito.mock(ResourceAction.class);
                    Mockito.when(action.getActionType()).thenReturn(RepositoryAction.RepositoryActionType.RESOURCE);
                    actions.put("someAction", action);
                    RepositoryInformation repositoryInformation = new ResourceRepositoryInformationImpl("actionResource",
                            resourceInformation, actions, RepositoryMethodAccess.ALL, true);
                    return repositoryInformation;

                }

                @Override
                public RepositoryInformation build(Class<?> repositoryClass, RepositoryInformationProviderContext context) {
                    return null;
                }
            });
        }
    }


    @Test
    public void checkProcess() throws IOException {
        HttpRequestContextBase requestContextBase = Mockito.mock(HttpRequestContextBase.class);
        HttpRequestContextBaseAdapter requestContext = new HttpRequestContextBaseAdapter(requestContextBase);

        Mockito.when(requestContextBase.getMethod()).thenReturn("GET");
        Mockito.when(requestContextBase.getPath()).thenReturn("/tasks/");
        Mockito.when(requestContextBase.getRequestHeader("Accept")).thenReturn("*");

        CollectionGetController controller = mock(CollectionGetController.class);
        when(controller.isAcceptable(any(JsonPath.class), eq("GET"))).thenCallRealMethod();

        Response expectedResponse = new Response(null, 200);
        when(controller.handleAsync(any(JsonPath.class), any(QueryAdapter.class), any(Document.class))).thenReturn(new ImmediateResult<>(expectedResponse));

        ControllerRegistry controllerRegistry = container.getBoot().getControllerRegistry();
        controllerRegistry.getControllers().clear();
        controllerRegistry.addController(controller);

        RequestDispatcher sut = new HttpRequestDispatcherImpl(container.getModuleRegistry(), null);
        sut.process(requestContext);

        verify(controller, times(1))
                .handleAsync(any(JsonPath.class), any(QueryAdapter.class), any(Document.class));
    }

    @Test
    public void onGivenPathAndRequestTypeControllerShouldHandleRequest() {
        // GIVEN
        String path = "/tasks/";
        String requestType = "GET";

        CollectionGetController controller = mock(CollectionGetController.class);
        ControllerRegistry controllerRegistry = container.getBoot().getControllerRegistry();
        controllerRegistry.getControllers().clear();
        controllerRegistry.addController(controller);

        RequestDispatcher sut = new HttpRequestDispatcherImpl(container.getModuleRegistry(), null);

        // WHEN
        when(controller.isAcceptable(any(JsonPath.class), eq(requestType))).thenCallRealMethod();
        when(controller.handleAsync(any(JsonPath.class), any(QueryAdapter.class),
                any(Document.class))).thenReturn(new ImmediateResult<>(null));

        Map<String, Set<String>> parameters = new HashMap<>();
        sut.dispatchRequest(path, requestType, parameters, null);

        // THEN
        verify(controller, times(1))
                .handleAsync(any(JsonPath.class), any(QueryAdapter.class), any(Document.class));
    }

    @Test
    public void shouldHandleRelationshipRequest() {
        // GIVEN
        String path = "/tasks/1/relationships/project";
        String requestType = "GET";

        RelationshipsResourceGetController controller = mock(RelationshipsResourceGetController.class);
        ControllerRegistry controllerRegistry = container.getBoot().getControllerRegistry();
        controllerRegistry.getControllers().clear();
        controllerRegistry.addController(controller);

        RequestDispatcher sut = new HttpRequestDispatcherImpl(container.getModuleRegistry(), null);

        // WHEN
        when(controller.isAcceptable(any(JsonPath.class), eq(requestType))).thenCallRealMethod();
        when(controller.handleAsync(any(JsonPath.class), any(QueryAdapter.class),
                any(Document.class))).thenReturn(new ImmediateResult<>(null));

        Map<String, Set<String>> parameters = new HashMap<>();
        sut.dispatchRequest(path, requestType, parameters, null);

        // THEN
        verify(controller, times(1))
                .handleAsync(any(JsonPath.class), any(QueryAdapter.class), any(Document.class));
    }

    @Ignore // FIXME reasonable action contributions
    @Test
    public void shouldNotifyWhenActionIsExeecuted() {
        // GIVEN
        String path = "/actionResource/1/someAction";
        String requestType = "GET";

        RequestDispatcher sut = new HttpRequestDispatcherImpl(container.getModuleRegistry(), null);

        // WHEN
        Map<String, Set<String>> parameters = new HashMap<>();
        sut.dispatchAction(path, "GET", parameters);

        // THEN

        ArgumentCaptor<DocumentFilterContext> filterContextCaptor = ArgumentCaptor.forClass(DocumentFilterContext.class);

        Mockito.verify(documentFilter, Mockito.times(1)).filter(filterContextCaptor.capture(), Mockito.any
                (DocumentFilterChain.class));
        DocumentFilterContext filterContext = filterContextCaptor.getValue();
        Assert.assertEquals("GET", filterContext.getMethod());
        Assert.assertTrue(filterContext.getJsonPath() instanceof ActionPath);
    }


    @Test
    public void shouldMapExceptionToErrorResponseIfMapperIsAvailable() {
        ControllerRegistry controllerRegistry = mock(ControllerRegistry.class);
        // noinspection unchecked
        when(controllerRegistry.getController(any(JsonPath.class), anyString())).thenThrow(new BadRequestException("test"));
        container.getModuleRegistry().setControllerRegistry(controllerRegistry);

        RequestDispatcher requestDispatcher = new HttpRequestDispatcherImpl(container.getModuleRegistry(),
                ExceptionMapperRegistryTest.exceptionMapperRegistry);

        Map<String, Set<String>> params = new HashMap<>();
        Response response = requestDispatcher.dispatchRequest("tasks", HttpMethod.GET.toString(), params, null);
        assertThat(response).isNotNull();
        assertThat(response.getHttpStatus()).isEqualTo(HttpStatus.BAD_REQUEST_400);
    }

    @Test
    public void shouldProcessUnknownExceptionsAsInternalServerError() {
        ControllerRegistry controllerRegistry = mock(ControllerRegistry.class);
        // noinspection unchecked
        when(controllerRegistry.getController(any(JsonPath.class), anyString())).thenThrow(ArithmeticException.class);
        container.getModuleRegistry().setControllerRegistry(controllerRegistry);

        RequestDispatcher requestDispatcher =
                new HttpRequestDispatcherImpl(container.getModuleRegistry(), ExceptionMapperRegistryTest.exceptionMapperRegistry);

        Map<String, Set<String>> params = new HashMap<>();
        Response response = requestDispatcher.dispatchRequest("tasks", HttpMethod.GET.toString(), params, null);
        Assert.assertEquals(HttpStatus.INTERNAL_SERVER_ERROR_500, response.getHttpStatus().intValue());
    }
}
