package io.crnk.meta.internal;

import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.repository.RepositoryAction;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.ResourceInformationProvider;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.ExceptionUtil;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.Module;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.repository.ResourceRepositoryV2;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.SerializeType;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.list.ResourceListBase;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaPrimaryKey;
import io.crnk.meta.model.resource.*;
import io.crnk.meta.model.resource.MetaResourceAction.MetaRepositoryActionType;
import io.crnk.meta.provider.MetaProviderBase;
import io.crnk.meta.provider.MetaProviderContext;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.Callable;

public class ResourceMetaProviderImpl extends MetaProviderBase {

	private boolean useResourceRegistry;

	public ResourceMetaProviderImpl(boolean useResourceRegistry) {
		this.useResourceRegistry = useResourceRegistry;
	}

	@Override
	public Set<Class<? extends MetaElement>> getMetaTypes() {
		return new HashSet<>(
				Arrays.asList(MetaResource.class, MetaJsonObject.class, MetaResourceBase.class, MetaResourceField.class));
	}


	@Override
	public MetaElement adjustForRequest(MetaElement element) {
		if (element instanceof MetaResource) {
			MetaResource metaResource = (MetaResource) element;
			return adjustResourceForRequest(metaResource);
		} else if (element instanceof MetaResourceField && element.getParent() instanceof MetaResource) {
			// TODO also cover MetaResourceBase by expending InformationModel accordingly
			MetaResourceField field = (MetaResourceField) element;
			return adjustFieldForRequest(field);
		}
		return element;
	}

	private MetaElement adjustFieldForRequest(MetaResourceField field) {
		MetaResource metaResource = (MetaResource) field.getParent();

		Module.ModuleContext moduleContext = context.getModuleContext();
		RegistryEntry entry = moduleContext.getResourceRegistry().getEntry(metaResource.getResourceType());
		ResourceInformation resourceInformation = entry.getResourceInformation();
		ResourceField fieldInformation = resourceInformation.findFieldByUnderlyingName(field.getName());

		ResourceFilterDirectory filterBehaviorProvider = moduleContext.getResourceFilterDirectory();
		boolean readable = metaResource.isInsertable() && filterBehaviorProvider.get(fieldInformation, HttpMethod.GET) == FilterBehavior.NONE;
		boolean insertable = metaResource.isInsertable() && filterBehaviorProvider.get(fieldInformation, HttpMethod.POST) == FilterBehavior.NONE;
		boolean updatable = metaResource.isUpdatable() && filterBehaviorProvider.get(fieldInformation, HttpMethod.PATCH) == FilterBehavior.NONE;

		// hide element if no permission
		if (!readable && !insertable && !updatable) {
			return null;
		}

		if (field.isUpdatable() != updatable || field.isInsertable() != insertable) {
			MetaResourceField clone = (MetaResourceField) field.duplicate();
			clone.setInsertable(insertable);
			clone.setUpdatable(updatable);
			return clone;
		}
		return field;
	}

	private MetaElement adjustResourceForRequest(MetaResource metaResource) {
		Module.ModuleContext moduleContext = context.getModuleContext();
		RegistryEntry entry = moduleContext.getResourceRegistry().getEntry(metaResource.getResourceType());
		ResourceInformation resourceInformation = entry.getResourceInformation();

		ResourceFilterDirectory filterBehaviorProvider = moduleContext.getResourceFilterDirectory();
		boolean readable = metaResource.isReadable() && filterBehaviorProvider.get(resourceInformation, HttpMethod.GET) == FilterBehavior.NONE;
		boolean insertable = metaResource.isInsertable() && filterBehaviorProvider.get(resourceInformation, HttpMethod.POST) == FilterBehavior.NONE;
		boolean updatable = metaResource.isUpdatable() && filterBehaviorProvider.get(resourceInformation, HttpMethod.PATCH) == FilterBehavior.NONE;
		boolean deletable = metaResource.isDeletable() && filterBehaviorProvider.get(resourceInformation, HttpMethod.DELETE) == FilterBehavior.NONE;

		// hide element if no permission
		if (!readable && !insertable && !updatable && !deletable) {
			return null;
		}

		// update element if necessary
		if (metaResource.isReadable() != readable || metaResource.isUpdatable() != updatable || metaResource.isInsertable() != insertable || metaResource.isDeletable() != deletable) {
			MetaResource clone = (MetaResource) metaResource.duplicate();
			clone.setReadable(readable);
			clone.setInsertable(insertable);
			clone.setUpdatable(updatable);
			clone.setDeletable(deletable);
			return clone;
		}
		return metaResource;
	}


	@Override
	public boolean accept(Type type, Class<? extends MetaElement> metaClass) {
		if (metaClass != MetaResource.class && metaClass != MetaElement.class && metaClass != MetaResourceBase.class
				&& metaClass != MetaJsonObject.class) {
			return false;
		}

		// note that the resourceRegistry might also contain none JSON API objects with a custom
		// information builder, so only accept if a MetaResource was explicitly requested
		if (this.useResourceRegistry) {
			ResourceRegistry resourceRegistry = context.getModuleContext().getResourceRegistry();
			if (resourceRegistry != null && (metaClass == MetaResource.class)) {
				Class<?> clazz = ClassUtils.getRawType(type);
				if (resourceRegistry.getEntryForClass(clazz) != null) {
					return true;
				}
			}
		}

		// always accept if json api annotations are found
		return metaClass == MetaResourceBase.class || ClassUtils.getRawType(type).getAnnotation(JsonApiResource.class) != null;
	}

	@Override
	public MetaElement createElement(Type type) {
		boolean allowNonResourceBaseClass = type != MetaResource.class;
		ResourceInformation information = getResourceInformation(ClassUtils.getRawType(type), allowNonResourceBaseClass);
		if (information == null) {
			// non resource base classes unknown to resource registry and as
			// such hidden from meta model
			return null;
		}

		Class<?> resourceClass = information.getResourceClass();

		Class<?> superClass = resourceClass.getSuperclass();

		MetaDataObject superMeta = null;
		if (superClass != Object.class) {
			// super type is either MetaResource or MetaResourceBase
			superMeta = context.getLookup().getMeta(superClass, MetaResourceBase.class, true);
		}

		String resourceType = information.getResourceType();
		MetaResourceBase resource = resourceType != null ? new MetaResource() : new MetaResourceBase();
		resource.setElementType(resource);
		resource.setImplementationType(resourceClass);
		resource.setSuperType(superMeta);
		if (superMeta != null) {
			superMeta.addSubType(resource);
		}
		resource.setName(resourceClass.getSimpleName());

		if (resourceType != null) {
			((MetaResource) resource).setResourceType(resourceType);
		}

		if (useResourceRegistry) {
			ResourceRegistry resourceRegistry = context.getModuleContext().getResourceRegistry();
			RegistryEntry entry = resourceRegistry.getEntry(information.getResourceType());
			if (entry != null) {
				boolean readOnlyImpl = entry.getResourceRepository().getResourceRepository() instanceof ReadOnlyResourceRepositoryBase;
				resource.setUpdatable(!readOnlyImpl);
				resource.setInsertable(!readOnlyImpl);
			}
		}

		List<ResourceField> fields = information.getFields();
		for (ResourceField field : fields) {
			addAttribute(resource, field);
		}

		return resource;
	}

	@Override
	public void discoverElements() {
		if (useResourceRegistry) {
			ResourceRegistry resourceRegistry = context.getModuleContext().getResourceRegistry();

			// enforce setup of meta data
			Collection<RegistryEntry> entries = resourceRegistry.getResources();
			for (RegistryEntry entry : entries) {
				ResourceInformation information = entry.getResourceInformation();
				MetaResource metaResource = context.getLookup().getMeta(information.getResourceClass(), MetaResource.class);
				ResourceRepositoryInformation repositoryInformation = entry.getRepositoryInformation();
				ResourceRepositoryAdapter<?, Serializable> resourceRepository = entry.getResourceRepository(null);
				if (resourceRepository != null) {
					MetaResourceRepository repository = discoverRepository(repositoryInformation, metaResource,
							resourceRepository, context);
					context.add(repository);
				}
			}
		}
	}

	private MetaResourceRepository discoverRepository(ResourceRepositoryInformation repositoryInformation,
													  MetaResource metaResource, ResourceRepositoryAdapter<?, Serializable> resourceRepository,
													  MetaProviderContext context) {

		MetaResourceRepository meta = new MetaResourceRepository();
		meta.setResourceType(metaResource);
		meta.setName(metaResource.getName() + "$Repository");
		meta.setId(metaResource.getId() + "$Repository");

		for (RepositoryAction action : repositoryInformation.getActions().values()) {
			MetaResourceAction metaAction = new MetaResourceAction();
			metaAction.setName(action.getName());
			metaAction.setActionType(MetaRepositoryActionType.valueOf(action.getActionType().toString()));
			metaAction.setParent(meta, true);
		}

		// TODO avoid use of ResourceRepositoryAdapter by enriching ResourceRepositoryInformation
		Object repository = resourceRepository.getResourceRepository();
		if (repository instanceof ResourceRepositoryV2) {
			setListInformationTypes(repository, context, meta);
		}
		return meta;
	}

	private void setListInformationTypes(final Object repository, final MetaProviderContext context,
										 final MetaResourceRepository meta) {
		ExceptionUtil.wrapCatchedExceptions(new Callable<Object>() {

			@Override
			public Object call() throws Exception {
				Method findMethod = repository.getClass().getMethod("findAll", QuerySpec.class);
				Class<?> listType = findMethod.getReturnType();

				if (ResourceListBase.class.equals(listType.getSuperclass())
						&& listType.getGenericSuperclass() instanceof ParameterizedType) {
					ParameterizedType genericSuperclass = (ParameterizedType) listType.getGenericSuperclass();

					Class<?> metaType = ClassUtils.getRawType(genericSuperclass.getActualTypeArguments()[1]);
					Class<?> linksType = ClassUtils.getRawType(genericSuperclass.getActualTypeArguments()[2]);
					if (!metaType.equals(MetaInformation.class)) {
						MetaDataObject listMetaType = context.getLookup().getMeta(metaType, MetaJsonObject.class);
						meta.setListMetaType(listMetaType);
					}
					if (!linksType.equals(LinksInformation.class)) {
						MetaDataObject listLinksType = context.getLookup().getMeta(linksType, MetaJsonObject.class);
						meta.setListLinksType(listLinksType);
					}
				}
				return null;
			}
		});
	}

	@Override
	public void onInitialized(MetaElement element) {
		if (element instanceof MetaResourceBase) {
			MetaResourceBase metaResource = (MetaResourceBase) element;

			ResourceInformation information = getResourceInformation(metaResource.getImplementationClass(), true);
			PreconditionUtil.assertNotNull(information.getResourceType(), metaResource);
			for (ResourceField field : information.getRelationshipFields()) {
				if (field.getOppositeName() != null) {
					Class<?> oppositeType = ClassUtils.getRawType(field.getElementType());
					MetaResource oppositeMeta = context.getLookup().getMeta(oppositeType, MetaResource.class);
					MetaAttribute attr = metaResource.getAttribute(field.getUnderlyingName());
					MetaAttribute oppositeAttr = oppositeMeta.getAttribute(field.getOppositeName());
					PreconditionUtil.assertNotNull(attr.getId() + " opposite not found", oppositeAttr);
					attr.setOppositeAttribute(oppositeAttr);
				}
			}

			ResourceField idField = information.getIdField();
			if (idField != null) {
				MetaAttribute idAttr = metaResource.getAttribute(idField.getUnderlyingName());
				idAttr.setPrimaryKeyAttribute(true);

				if (metaResource.getSuperType() == null || metaResource.getSuperType().getPrimaryKey() == null) {
					MetaPrimaryKey primaryKey = new MetaPrimaryKey();
					primaryKey.setName(metaResource.getName() + "$primaryKey");
					primaryKey.setName(metaResource.getId() + "$primaryKey");
					primaryKey.setElements(Arrays.asList(idAttr));
					primaryKey.setUnique(true);
					primaryKey.setParent(metaResource, true);
					metaResource.setPrimaryKey(primaryKey);
					context.add(primaryKey);
				}
			}
		}

		if (element instanceof MetaAttribute && element.getParent() instanceof MetaResourceBase) {
			MetaAttribute attr = (MetaAttribute) element;
			MetaResourceBase parent = (MetaResourceBase) attr.getParent();

			ResourceInformation information = getResourceInformation(parent.getImplementationClass(), true);
			ResourceField field = information.findFieldByUnderlyingName(attr.getName());
			PreconditionUtil.assertNotNull(attr.getName(), field);
			Type implementationType = field.getGenericType();

			MetaElement metaType = context.getLookup().getMeta(implementationType, MetaJsonObject.class, true);
			if (metaType == null) {
				metaType = context.getLookup().getMeta(implementationType);
			}
			attr.setType(metaType.asType());
		}

	}

	private ResourceInformation getResourceInformation(Class<?> resourceClass, boolean allowNull) {
		if (useResourceRegistry) {
			ResourceRegistry resourceRegistry = context.getModuleContext().getResourceRegistry();
			RegistryEntry entry = resourceRegistry.getEntryForClass(resourceClass);
			if (entry != null) {
				PreconditionUtil.assertNotNull(resourceClass.getName(), entry);
				return entry.getResourceInformation();
			}
		}

		ResourceInformationProvider infoBuilder = context.getModuleContext().getResourceInformationBuilder();
		if (infoBuilder.accept(resourceClass)) {
			return infoBuilder.build(resourceClass);
		}

		if (allowNull) {
			return null;
		}

		throw new IllegalStateException("failed to get information for " + resourceClass.getName());
	}

	private void addAttribute(MetaResourceBase resource, ResourceField field) {
		if (resource.getSuperType() != null && resource.getSuperType().hasAttribute(field.getUnderlyingName())) {
			return; // nothing to do
		}

		MetaResourceField attr = new MetaResourceField();

		attr.setParent(resource, true);
		attr.setName(field.getUnderlyingName());
		attr.setAssociation(field.getResourceFieldType() == ResourceFieldType.RELATIONSHIP);
		attr.setMeta(field.getResourceFieldType() == ResourceFieldType.META_INFORMATION);
		attr.setLinks(field.getResourceFieldType() == ResourceFieldType.LINKS_INFORMATION);
		attr.setDerived(false);

		attr.setLazy(field.getSerializeType() == SerializeType.LAZY);
		attr.setSortable(field.getAccess().isSortable());
		attr.setFilterable(field.getAccess().isFilterable());
		attr.setInsertable(field.getAccess().isPostable());
		attr.setUpdatable(field.getAccess().isPatchable());

		boolean isPrimitive = ClassUtils.isPrimitiveType(field.getType());
		boolean isId = field.getResourceFieldType() == ResourceFieldType.ID;
		attr.setNullable(!isPrimitive && !isId);
	}
}
