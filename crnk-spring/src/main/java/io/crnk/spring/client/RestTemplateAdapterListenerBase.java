package io.crnk.spring.client;


import org.springframework.web.client.RestTemplate;

public class RestTemplateAdapterListenerBase implements RestTemplateAdapterListener {

	@Override
	public void onBuild(RestTemplate template) {
		// nothing to do
	}
}
