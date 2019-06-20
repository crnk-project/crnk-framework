package io.crnk.ui;


import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.module.Module;
import io.crnk.core.module.ModuleExtension;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.MetaLookupImpl;
import io.crnk.meta.MetaModule;
import io.crnk.meta.MetaModuleConfig;
import io.crnk.meta.MetaModuleExtension;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.provider.MetaFilter;
import io.crnk.meta.provider.MetaFilterBase;
import io.crnk.meta.provider.MetaProviderBase;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.ui.internal.UIHttpRequestProcessor;
import io.crnk.ui.presentation.PresentationManager;
import io.crnk.ui.presentation.PresentationService;
import io.crnk.ui.presentation.annotation.PresentationFullTextSearchable;
import io.crnk.ui.presentation.annotation.PresentationLabel;
import io.crnk.ui.presentation.repository.EditorRepository;
import io.crnk.ui.presentation.repository.ExplorerRepository;

public class UIModule implements Module {


	private final UIModuleConfig config;

	private ExplorerRepository explorerRepository;

	private EditorRepository editorRepository;

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

	private MetaProviderBase presentationMetaProvider = new MetaProviderBase() {

		private Set<MetaFilter> metaFilters = Collections.singleton(new PresentationMetaFilter());

		@Override
		public Collection<MetaFilter> getFilters() {
			return metaFilters;
		}
	};

	private MetaLookup initMetaModule() {
		if (metaLookup == null) {
			Optional<MetaModule> optMetaModule = context.getModuleRegistry().getModule(MetaModule.class);
			if (optMetaModule.isPresent()) {
				metaLookup = optMetaModule.get().getLookup();
			}
			else {
				MetaModuleConfig config = new MetaModuleConfig();
				config.addMetaProvider(new ResourceMetaProvider());
				config.addMetaProvider(presentationMetaProvider);

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
			editorRepository = new EditorRepository(manager);
			context.addRepository(editorRepository);


			MetaModuleExtension metaExtension = new MetaModuleExtension();
			metaExtension.addProvider(presentationMetaProvider);
			context.addExtension(metaExtension);
		}
	}

	class PresentationMetaFilter extends MetaFilterBase {

		@Override
		public void onInitializing(MetaElement element) {
			if (element instanceof MetaAttribute) {
				MetaAttribute attribute = (MetaAttribute) element;
				PresentationFullTextSearchable annotation = attribute.getAnnotation(PresentationFullTextSearchable.class);
				if (annotation != null) {
					attribute.getNatures().add(PresentationFullTextSearchable.META_ELEMENT_NATURE);
				}
				PresentationLabel labelAnnotation = attribute.getAnnotation(PresentationLabel.class);
				if (labelAnnotation != null) {
					attribute.getNatures().add(PresentationLabel.META_ELEMENT_NATURE);
				}
			}
		}
	}

	public ExplorerRepository getExplorerRepository() {
		return explorerRepository;
	}


	public EditorRepository getEditorRepository() {
		return editorRepository;
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
			}
			catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
	}
}
