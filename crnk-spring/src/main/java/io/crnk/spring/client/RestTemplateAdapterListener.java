package io.crnk.spring.client;

import org.springframework.web.client.RestTemplate;

public interface RestTemplateAdapterListener {

	void onBuild(RestTemplate template);

}
