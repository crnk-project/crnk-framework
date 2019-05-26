package io.crnk.ui;

import io.crnk.ui.presentation.PresentationService;

import java.util.List;
import java.util.function.Supplier;

public class UIModuleConfig {

    private String path = "/browse/";

    private Supplier<List<PresentationService>> services;

    private boolean presentationRepositoriesEnabled;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Supplier<List<PresentationService>> getServices() {
        return services;
    }

    public void setServices(Supplier<List<PresentationService>> services) {
        this.services = services;
    }
}
