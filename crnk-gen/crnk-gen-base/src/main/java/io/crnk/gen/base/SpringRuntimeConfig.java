package io.crnk.gen.base;

import java.util.Properties;

public class SpringRuntimeConfig {

    private String profile = "default";

    private String configuration;

    private String initializerMethod;

    private Properties defaultProperties = new Properties();

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

    public String getInitializerMethod() {
        return initializerMethod;
    }

    public void setInitializerMethod(String initializerMethod) {
        this.initializerMethod = initializerMethod;
    }

    public Properties getDefaultProperties() {
        return defaultProperties;
    }

    public void setDefaultProperties(Properties defaultProperties) {
        this.defaultProperties = defaultProperties;
    }
}
