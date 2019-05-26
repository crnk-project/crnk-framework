package io.crnk.ui;


import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.module.Module;
import io.crnk.core.module.ModuleExtension;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.MetaLookupImpl;
import io.crnk.meta.MetaModule;
import io.crnk.meta.MetaModuleConfig;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.ui.internal.UIHttpRequestProcessor;
import io.crnk.ui.presentation.PresentationManager;
import io.crnk.ui.presentation.PresentationService;
import io.crnk.ui.presentation.repository.ExplorerRepository;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class UIModule implements Module {


    private final UIModuleConfig config;

    private ExplorerRepository explorerRepository;

    private ModuleContext context;

    // protected for CDI
    protected UIModule() {
        config = null;
    }

    protected UIModule(UIModuleConfig config) {
        this.config = config;
    }

    public static UIModule create(UIModuleConfig config) {
        return new UIModule(config);
    }

    public String getModuleName() {
        return "ui";
    }

    private MetaLookup metaLookup;

    private MetaLookup initMetaModule() {
        if (metaLookup == null) {
            Optional<MetaModule> optMetaModule = context.getModuleRegistry().getModule(MetaModule.class);
            if (optMetaModule.isPresent()) {
                metaLookup = optMetaModule.get().getLookup();
            } else {
                MetaModuleConfig config = new MetaModuleConfig();
                config.addMetaProvider(new ResourceMetaProvider());

                MetaLookupImpl impl = new MetaLookupImpl();
                impl.setModuleContext(context);
                config.apply(impl);
                impl.initialize();
                metaLookup = impl;
            }
        }
        return metaLookup;
    }

    @Override
    public void setupModule(ModuleContext context) {
        this.context = context;
        context.addHttpRequestProcessor(new UIHttpRequestProcessor(config));
        setupHomeExtension(context);

        if (config != null) {
            Supplier<List<PresentationService>> servicesSupplier = config.getServices();
            if (servicesSupplier == null) {
                servicesSupplier = () -> Arrays.asList(new PresentationService("local", null, initMetaModule()));
            }

            PresentationManager manager = new PresentationManager(servicesSupplier);
            explorerRepository = new ExplorerRepository(manager);
            context.addRepository(explorerRepository);
        }
    }

    public ExplorerRepository getExplorerRepository() {
        return explorerRepository;
    }

    public UIModuleConfig getConfig() {
        return config;
    }

    private void setupHomeExtension(ModuleContext context) {
        if (ClassUtils.existsClass("io.crnk.home.HomeModuleExtension")) {
            try {
                Class clazz = Class.forName("io.crnk.ui.internal.UiHomeModuleExtensionFactory");
                Method method = clazz.getMethod("create", UIModuleConfig.class);
                ModuleExtension homeExtension = (ModuleExtension) method.invoke(clazz, config);
                context.addExtension(homeExtension);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
