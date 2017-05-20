package io.crnk.meta.provider;

import io.crnk.meta.model.MetaElement;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface MetaProvider {

	void init(MetaProviderContext context);

	Collection<MetaProvider> getDependencies();

	Set<Class<? extends MetaElement>> getMetaTypes();

	boolean accept(Type type, Class<? extends MetaElement> requestedMetaClass);

	MetaElement createElement(Type type);

	void discoverElements();

	void onInitializing(MetaElement element);

	void onInitialized(MetaElement element);

	Map<? extends String, ? extends String> getIdMappings();

}
