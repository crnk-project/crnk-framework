package io.crnk.meta;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldNameTransformer;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.information.resource.AnnotationResourceInformationBuilder;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.module.InitializingModule;
import io.crnk.core.module.Module;
import io.crnk.core.module.discovery.ResourceLookup;
import io.crnk.legacy.registry.DefaultResourceInformationBuilderContext;
import io.crnk.meta.internal.MetaRelationshipRepository;
import io.crnk.meta.internal.MetaResourceRepositoryImpl;
import io.crnk.meta.model.*;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.provider.MetaProvider;

public class MetaModule implements Module, InitializingModule {

	private MetaLookup lookup = new MetaLookup();

	private ModuleContext context;

	// make protected for CDI in the future and remove deprecation
	@Deprecated
	public MetaModule(){
	}

	public static MetaModule create() {
		return new MetaModule();
	}

	@Override
	public String getModuleName() {
		return "meta";
	}

	public void putIdMapping(String packageName, String idPrefix) {
		lookup.putIdMapping(packageName, idPrefix);
	}

	public void putIdMapping(String packageName, Class<? extends MetaElement> type, String idPrefix) {
		lookup.putIdMapping(packageName, type, idPrefix);
	}

	public void addMetaProvider(MetaProvider provider) {
		PreconditionUtil.assertNull("module is already initialized and cannot be changed anymore", context);
		lookup.addProvider(provider);
	}

	@Override
	public void setupModule(ModuleContext context) {
		this.context = context;

		lookup.setModuleContext(context);

		final Set<Class<? extends MetaElement>> metaClasses = new HashSet<>();
		metaClasses.add(MetaArrayType.class);
		metaClasses.add(MetaAttribute.class);
		metaClasses.add(MetaCollectionType.class);
		metaClasses.add(MetaDataObject.class);
		metaClasses.add(MetaElement.class);
		metaClasses.add(MetaEnumType.class);
		metaClasses.add(MetaInterface.class);
		metaClasses.add(MetaKey.class);
		metaClasses.add(MetaListType.class);
		metaClasses.add(MetaLiteral.class);
		metaClasses.add(MetaMapType.class);
		metaClasses.add(MetaPrimaryKey.class);
		metaClasses.add(MetaPrimitiveType.class);
		metaClasses.add(MetaSetType.class);
		metaClasses.add(MetaType.class);
		for (MetaProvider provider : lookup.getProviders()) {
			metaClasses.addAll(provider.getMetaTypes());
		}

		AnnotationResourceInformationBuilder informationBuilder = new AnnotationResourceInformationBuilder(
				new ResourceFieldNameTransformer());
		informationBuilder.init(new DefaultResourceInformationBuilderContext(informationBuilder, context.getTypeParser()));

		for (Class<? extends MetaElement> metaClass : metaClasses) {
			if (context.isServer()) {
				context.addRepository(new MetaResourceRepositoryImpl<>(lookup, metaClass));

				HashSet<Class<? extends MetaElement>> targetResourceClasses = new HashSet<>();
				ResourceInformation information = informationBuilder.build(metaClass);
				for (ResourceField relationshipField : information.getRelationshipFields()) {
					if (!MetaElement.class.isAssignableFrom(relationshipField.getElementType())) {
						throw new IllegalStateException("only MetaElement relations supported, got " + relationshipField);
					}
					targetResourceClasses.add((Class<? extends MetaElement>) relationshipField.getElementType());
				}
				for (Class<? extends MetaElement> targetResourceClass : targetResourceClasses) {
					context.addRepository(new MetaRelationshipRepository(lookup, metaClass, targetResourceClass));
				}
			}
		}

		context.addResourceLookup(new ResourceLookup() {

			@SuppressWarnings("unchecked")
			@Override
			public Set<Class<?>> getResourceClasses() {
				return (Set) metaClasses;
			}

			@Override
			public Set<Class<?>> getResourceRepositoryClasses() {
				return Collections.emptySet();
			}
		});
	}

	@Override
	public void init() {
		lookup.initialize();
	}

	public MetaLookup getLookup() {
		return lookup;
	}
}
