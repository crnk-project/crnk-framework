package io.crnk.spring.client;

import io.crnk.client.http.HttpAdapterRequest;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Arrays;

public class RestTemplateRequest implements HttpAdapterRequest {

	private static final MediaType CONTENT_TYPE =
			MediaType.parseMediaType(io.crnk.core.engine.http.HttpHeaders.JSONAPI_CONTENT_TYPE_AND_CHARSET);

	private final RestTemplate template;

	private final String requestBody;

	private final io.crnk.core.engine.http.HttpMethod method;

	private final String url;

	private HttpHeaders headers;

	public RestTemplateRequest(RestTemplate template, String url, io.crnk.core.engine.http.HttpMethod method,
							   String requestBody) {
		this.template = template;
		this.requestBody = requestBody;
		this.url = url;
		this.method = method;

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
		HttpEntity<String> entityReq = new HttpEntity<>(requestBody, headers);
		try {
			try {
				java.net.URL url = new java.net.URL(this.url);
				ResponseEntity<String> response = template.exchange(url.toURI(), HttpMethod.resolve(method.name()), entityReq, String.class);
				return new RestTemplateResponse(response);
			} catch (MalformedURLException e) {
				throw new IllegalStateException(e);
			} catch (URISyntaxException e) {
				throw new IllegalStateException(e);
			}
		} catch (HttpClientErrorException e) {
			return new RestTemplateResponse(e.getRawStatusCode(), e.getStatusCode().getReasonPhrase(), e.getResponseBodyAsString
					(), e.getResponseHeaders());
		}
	}

}
