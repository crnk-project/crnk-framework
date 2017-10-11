package io.crnk.meta.internal;

import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.resource.MetaJsonObject;
import io.crnk.meta.provider.MetaProvider;

import java.lang.reflect.Type;
import java.util.Collection;

public class JsonObjectMetaProvider extends MetaDataObjectProvider {


	private final ResourceMetaProviderImpl resourceMetaProvider;

	public JsonObjectMetaProvider(ResourceMetaProviderImpl resourceMetaProvider) {
		this.resourceMetaProvider = resourceMetaProvider;
	}

	@Override
	public MetaElement createElement(Type type) {
		MetaJsonObject element = (MetaJsonObject) super.createElement(type);

		Type implementationType = element.getImplementationType();
		Class<?> rawType = ClassUtils.getRawType(implementationType);

		Class<?> enclosingClass = rawType.getEnclosingClass();
		boolean isLinks = LinksInformation.class.isAssignableFrom(rawType);
		boolean isMeta = MetaInformation.class.isAssignableFrom(rawType);
		if (enclosingClass != null && (isLinks || isMeta)) {
			RegistryEntry entry = context.getModuleContext().getResourceRegistry().getEntry(enclosingClass);
			if (entry != null) {
				String id = resourceMetaProvider.getId(entry.getResourceInformation().getResourceType());
				if (isMeta) {
					element.setId(id + "$meta");
				} else {
					element.setId(id + "$links");
				}
			}
		}
		return element;
	}

	@Override
	public boolean accept(Type type, Class<? extends MetaElement> metaClass) {
		// no marker available to decide what makes up a MetaJsonObject.
		// So check whether any other provider
		// matches

		Class rawType = ClassUtils.getRawType(type);
		boolean hasResourceAnnotation = rawType.getAnnotation(JsonApiResource.class) != null;

		// TODO move collections and primitive types into regular providers
		boolean collection = Collection.class.isAssignableFrom(rawType) || rawType.isArray();
		boolean primitive = context.getLookup().isPrimitiveType(rawType);
		boolean isEnum = Enum.class.isAssignableFrom(rawType);

		if (type instanceof Class && !isEnum && !primitive && !collection && metaClass == MetaJsonObject.class && !hasResourceAnnotation) {
			for (MetaProvider provider : this.context.getLookup().getProviders()) {
				if (provider != this) {
					if (provider.accept(type, metaClass)) {
						return false;
					}
				}
			}
			return true;
		}
		return false;
	}

	@Override
	protected MetaDataObject newDataObject() {
		return new MetaJsonObject();
	}

	@Override
	protected Class<? extends MetaElement> getMetaClass() {
		return MetaJsonObject.class;
	}
}
