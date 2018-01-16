package io.crnk.gen.runtime.spring;

public class SpringRuntimeConfig {

	private String profile;

	private String configurationClassName;

	public String getProfile() {
		return profile;
	}

	public void setProfile(String profile) {
		this.profile = profile;
	}

	public String getConfiguration() {
		return configurationClassName;
	}

	public void setConfiguration(String configurationClassName) {
		this.configurationClassName = configurationClassName;
	}
}
