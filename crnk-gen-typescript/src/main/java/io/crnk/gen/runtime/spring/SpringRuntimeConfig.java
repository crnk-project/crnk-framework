package io.crnk.gen.runtime.spring;

public class SpringRuntimeConfig {

	private String profile;

	private String configuration;

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public String getConfiguration() {
		return configuration;
	}

	public void setConfiguration(String configurationClassName) {
		this.configuration = configurationClassName;
	}
}
