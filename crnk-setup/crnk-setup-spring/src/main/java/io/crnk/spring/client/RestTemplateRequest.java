package io.crnk.spring.client;

import io.crnk.client.http.HttpAdapterListener;
import io.crnk.client.http.HttpAdapterRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class RestTemplateRequest implements HttpAdapterRequest {

    private static final MediaType CONTENT_TYPE =
            MediaType.parseMediaType(io.crnk.core.engine.http.HttpHeaders.JSONAPI_CONTENT_TYPE_AND_CHARSET);

    private final RestTemplate template;

    private final String requestBody;

    private final io.crnk.core.engine.http.HttpMethod method;

    private final String url;

    private final List<HttpAdapterListener> listeners;

    private HttpHeaders headers;

    public RestTemplateRequest(RestTemplate template, String url, io.crnk.core.engine.http.HttpMethod method,
                               String requestBody, List<HttpAdapterListener> listeners) {
        this.template = template;
        this.requestBody = requestBody;
        this.url = url;
        this.method = method;
        this.listeners = listeners;

        headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(CONTENT_TYPE));
        if (requestBody != null) {
            headers.setContentType(CONTENT_TYPE);
        }
    }

    @Override
    public void header(String name, String value) {
        headers.set(name, value);
    }

    @Override
    public RestTemplateResponse execute() {
        try {
            try {
                java.net.URL url = new java.net.URL(this.url);
                listeners.stream().forEach(it -> it.onRequest(this));
                HttpEntity<String> entityReq = new HttpEntity<>(requestBody, headers);
                ResponseEntity<String> response = template.exchange(url.toURI(), HttpMethod.valueOf(method.name()), entityReq, String.class);
                RestTemplateResponse adapterResponse = new RestTemplateResponse(response);
                listeners.stream().forEach(it -> it.onResponse(this, adapterResponse));
                return adapterResponse;
            } catch (MalformedURLException e) {
                throw new IllegalStateException(e);
            } catch (URISyntaxException e) {
                throw new IllegalStateException(e);
            }
        } catch (HttpClientErrorException e) {
            return new RestTemplateResponse(e.getStatusCode().value(), HttpStatus.valueOf(e.getStatusCode().value()).getReasonPhrase(), e.getResponseBodyAsString
                    (), e.getResponseHeaders());
        }
    }

    @Override
    public String getBody() {
        return requestBody;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public io.crnk.core.engine.http.HttpMethod getHttpMethod() {
        return method;
    }

    @Override
    public Set<String> getHeadersNames() {
        return headers.keySet();
    }

    @Override
    public String getHeaderValue(String name) {
        return headers.getFirst(name);
    }

}
