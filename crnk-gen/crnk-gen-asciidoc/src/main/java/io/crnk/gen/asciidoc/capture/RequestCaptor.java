package io.crnk.gen.asciidoc.capture;

import io.crnk.client.http.HttpAdapterRequest;
import io.crnk.client.http.HttpAdapterResponse;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.utils.PreconditionUtil;

import java.util.concurrent.Callable;

public class RequestCaptor {

    private final ThreadLocal<RequestCaptor> threadLocal;

    private String key;

    private String description;

    private HttpAdapterRequest request;

    private HttpAdapterResponse response;

    private ResourceInformation resourceInformation;

    private String title;

    protected RequestCaptor(ThreadLocal<RequestCaptor> threadLocal) {
        this.threadLocal = threadLocal;
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public void call(Runnable run) {
        try {
            threadLocal.set(this);
            run.run();
        } finally {
            threadLocal.remove();
        }
    }

    public <V> V call(Callable<V> run) {
        try {
            threadLocal.set(this);
            return run.call();
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("failed to call", e);
        } finally {
            threadLocal.remove();
        }
    }

    protected RequestCaptor setKey(String key) {
        PreconditionUtil.verify(this.key == null, "key already set");
        this.key = key;
        return this;
    }

    protected RequestCaptor setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public RequestCaptor setDescription(String description) {
        PreconditionUtil.verify(this.description == null, "description already set");
        this.description = description;
        return this;
    }


    public HttpAdapterRequest getRequest() {
        return request;
    }

    protected void setRequest(HttpAdapterRequest request) {
        PreconditionUtil.verify(this.request == null, "request already set");
        this.request = request;
    }

    public HttpAdapterResponse getResponse() {
        return response;
    }

    protected void setResponse(HttpAdapterResponse response) {
        PreconditionUtil.verify(this.response == null, "response already set");
        this.response = response;
    }

    protected void setResourceInformation(ResourceInformation resourceInformation) {
        PreconditionUtil.verify(this.resourceInformation == null, "resourceInformation already set");
        this.resourceInformation = resourceInformation;
    }
}
