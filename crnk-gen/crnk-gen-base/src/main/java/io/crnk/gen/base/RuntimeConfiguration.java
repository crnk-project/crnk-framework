package io.crnk.gen.base;


public class RuntimeConfiguration {

    private String configuration = "integrationTest";

    private SpringRuntimeConfig spring = new SpringRuntimeConfig();

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public SpringRuntimeConfig getSpring() {
        return spring;
    }

    public void setSpring(SpringRuntimeConfig spring) {
        this.spring = spring;
    }
}
