package io.crnk.spring.client;

import io.crnk.client.http.HttpAdapterResponse;
import io.crnk.core.engine.internal.utils.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Set;

public class RestTemplateResponse implements HttpAdapterResponse {

    private String body;

    private int status;

    private String message;

    private HttpHeaders headers;


    public RestTemplateResponse(ResponseEntity<String> response) {
        this.body = response.getBody();
        this.status = response.getStatusCode().value();
        this.message = HttpStatus.valueOf(response.getStatusCode().value()).getReasonPhrase();
        this.headers = response.getHeaders();
    }

    public RestTemplateResponse(int status, String message, String body, HttpHeaders headers) {
        this.body = body;
        this.status = status;
        this.message = message;
        this.headers = headers;
    }

    @Override
    public boolean isSuccessful() {
        return status >= 200 && status < 300;
    }

    @Override
    public String body() {
        return body;
    }

    @Override
    public int code() {
        return status;
    }

    @Override
    public String message() {
        return message;
    }

    @Override
    public String getResponseHeader(String name) {
        List<String> values = headers.get(name);
        return values != null ? StringUtils.join(",", values) : null;
    }

    @Override
    public Set<String> getHeaderNames() {
        return headers.keySet();
    }

}
