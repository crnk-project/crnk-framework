package io.crnk.meta.provider;

import io.crnk.meta.model.MetaElement;

import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

public class MetaProviderBase implements MetaProvider {

	protected MetaProviderContext context;

	@Override
	public boolean accept(Type type, Class<? extends MetaElement> metaClass) {
		return false;
	}

	@Override
	public MetaElement createElement(Type type) {
		// does not accept anything, so does not need to create anything
		throw new UnsupportedOperationException();
	}

	@Override
	public Collection<MetaProvider> getDependencies() {
		return Collections.emptySet();
	}

	@Override
	public void discoverElements() {
		// nothing to do
	}

	@Override
	public void onInitializing(MetaElement element) {
		// nothing to do
	}

	@Override
	public void onInitialized(MetaElement element) {
		// nothing to do
	}

	@Override
	public Set<Class<? extends MetaElement>> getMetaTypes() {
		return Collections.emptySet();
	}

	@Override
	public Map<? extends String, ? extends String> getIdMappings() {
		return Collections.emptyMap();
	}

	@Override
	public MetaElement adjustForRequest(MetaElement element) {
		return element;
	}

	@Override
	public void init(MetaProviderContext context) {
		this.context = context;
	}
}
