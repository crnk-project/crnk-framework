package io.crnk.meta.internal.resource;

import io.crnk.core.engine.information.repository.RepositoryAction;
import io.crnk.core.engine.information.repository.ResourceRepositoryInformation;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.internal.repository.ResourceRepositoryAdapter;
import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.engine.internal.utils.ExceptionUtil;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.BulkResourceRepository;
import io.crnk.core.repository.ReadOnlyResourceRepositoryBase;
import io.crnk.core.repository.ResourceRepository;
import io.crnk.core.resource.annotations.SerializeType;
import io.crnk.core.resource.links.LinksInformation;
import io.crnk.core.resource.list.ResourceListBase;
import io.crnk.core.resource.meta.MetaInformation;
import io.crnk.meta.internal.MetaIdProvider;
import io.crnk.meta.internal.typed.TypedMetaPartitionBase;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaType;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceAction;
import io.crnk.meta.model.resource.MetaResourceBase;
import io.crnk.meta.model.resource.MetaResourceField;
import io.crnk.meta.model.resource.MetaResourceRepository;
import io.crnk.meta.provider.MetaPartitionContext;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

public class ResourceMetaParitition extends TypedMetaPartitionBase {


	private final String idPrefix;

	private final MetaIdProvider idProvider;

	public ResourceMetaParitition(String idPrefix, MetaIdProvider idProvider) {
		super();
		this.idPrefix = idPrefix;
		this.idProvider = idProvider;

		this.idProvider.putIdMapping("io.crnk.core.resource.links", idPrefix + "information");

		this.addFactory(new JsonObjectMetaFactory());
	}

	@Override
	public void init(MetaPartitionContext context) {
		super.init(context);
		this.parent = context.getBasePartition();
	}

	@Override
	protected Optional<MetaElement> addElement(Type type, MetaElement element) {
		if (element instanceof MetaType) {
			MetaType typeElement = element.asType();
			if (!element.hasId()) {
				element.setId(
						computeId(typeElement)); //idProvider.computeIdPrefixFromPackage(implClass, element) + element.getName
				// ());
			}
		}
		return super.addElement(type, element);
	}

	private String computeId(MetaType element) {
		Type implementationType = element.getImplementationType();
		Class<?> rawType = ClassUtils.getRawType(implementationType);

		Class<?> enclosingClass = rawType.getEnclosingClass();
		boolean isLinks = LinksInformation.class.isAssignableFrom(rawType);
		boolean isMeta = MetaInformation.class.isAssignableFrom(rawType);
		ResourceRegistry resourceRegistry = context.getModuleContext().getResourceRegistry();
		if (enclosingClass != null && (isLinks || isMeta)) {
			RegistryEntry entry = resourceRegistry.getEntry(enclosingClass);
			if (entry != null) {
				String id = getId(entry.getResourceInformation().getResourceType());
				if (isMeta) {
					return id + "$meta";
				} else {
					return id + "$links";
				}
			}
		}
		if (!element.hasId()) {
			PreconditionUtil.assertNotNull("must have package", rawType.getPackage());
			String packageName = rawType.getPackage().getName();

			String closedPackageName = null;
			String closedResourceType = null;
			for (RegistryEntry entry : resourceRegistry.getEntries()) {
				ResourceInformation resourceInformation = entry.getResourceInformation();
				Class<?> resourceClass = resourceInformation.getResourceClass();
				String resourcePackageName = resourceClass.getPackage().getName();
				if (packageName.startsWith(resourcePackageName) && (closedPackageName == null
						|| closedPackageName.length() < resourcePackageName.length())) {
					closedPackageName = resourcePackageName;
					closedResourceType = resourceInformation.getResourceType();
				}

				Object resourceRepository = entry.getResourceRepository().getImplementation();
				Package resourcePackage = resourceRepository.getClass().getPackage();
				resourcePackageName = resourcePackage != null ? resourcePackage.getName() : resourceClass.getPackage().getName();
				if (packageName.startsWith(resourcePackageName) && (closedPackageName == null
						|| closedPackageName.length() < resourcePackageName.length())) {
					closedPackageName = resourcePackageName;
					closedResourceType = resourceInformation.getResourceType();
				}
			}
			if (closedResourceType != null) {
				String resourceId = getId(closedResourceType);
				String basePath = resourceId.substring(0, resourceId.lastIndexOf('.'));
				String relativePath = packageName.substring(closedPackageName.length());
				return basePath + relativePath + "." + element.getName().toLowerCase();
			}
		}
		return idProvider.computeIdPrefixFromPackage(rawType, element) + element.getName().toLowerCase();
	}


	@Override
	public void discoverElements() {
		ResourceRegistry resourceRegistry = context.getModuleContext().getResourceRegistry();

		// enforce setup of meta data
		Collection<RegistryEntry> entries = resourceRegistry.getEntries();
		for (RegistryEntry entry : entries) {
			ResourceInformation resourceInformation = entry.getResourceInformation();
			MetaResource metaResource = discoverResource(resourceInformation);

			ResourceRepositoryInformation repositoryInformation = entry.getRepositoryInformation();
			ResourceRepositoryAdapter resourceRepository = entry.getResourceRepository();
			if (repositoryInformation != null) {
				MetaResourceRepository metaRepository = discoverRepository(repositoryInformation, metaResource,
						resourceRepository);
				context.addElement(metaRepository);

				metaResource.setRepository(metaRepository);
			}
		}
	}

	private MetaResource discoverResource(ResourceInformation information) {
		String id = getId(information.getResourceType());

		// check if already done (as super types get setup recursively)
		Optional<MetaElement> existingElement = context.getMetaElement(id);
		if (existingElement.isPresent()) {
			return (MetaResource) existingElement.get();
		}

		String superResourceType = information.getSuperResourceType();
		MetaResource superMeta = null;
		ResourceInformation superInformation = null;
		if (superResourceType != null) {
			superInformation =
					context.getModuleContext().getResourceRegistry().getEntry(superResourceType).getResourceInformation();
			superMeta = discoverResource(superInformation);
		}


		String resourceType = information.getResourceType();
		MetaResource resource = new MetaResource();
		resource.setId(id);
		resource.setElementType(resource);
		resource.setImplementationType(information.getResourceClass());
		resource.setName(getName(information));
		resource.setResourceType(resourceType);
		resource.setResourcePath(information.getResourcePath());
		resource.setVersionRange(information.getVersionRange());
		resource.setReadable(information.getAccess().isReadable());
		resource.setUpdatable(information.getAccess().isPatchable());
		resource.setInsertable(information.getAccess().isPostable());
		resource.setDeletable(information.getAccess().isDeletable());
		if (superMeta != null) {
			resource.setSuperType(superMeta);
			if (superMeta != null) {
				superMeta.addSubType(resource);
			}
		}

		ResourceRegistry resourceRegistry = context.getModuleContext().getResourceRegistry();
		RegistryEntry entry = resourceRegistry.getEntry(information.getResourceType());
		if (entry != null) {
			boolean readOnlyImpl =
					entry.getResourceRepository().getImplementation() instanceof ReadOnlyResourceRepositoryBase;
			resource.setUpdatable(resource.isUpdatable() && !readOnlyImpl);
			resource.setInsertable(resource.isInsertable() && !readOnlyImpl);
			resource.setDeletable(resource.isDeletable() && !readOnlyImpl);
		}


		List<ResourceField> fields = information.getFields();
		for (ResourceField field : fields) {
			if (superInformation == null || superInformation.findFieldByUnderlyingName(field.getUnderlyingName()) == null) {
				// TODO check whether overriden and changed
				addAttribute(resource, field);
			}
		}

		Class<?> resourceClass = information.getResourceClass();

		addElement(resourceClass, resource);

		return resource;
	}

	protected String getId(String resourceType) {
		ResourceRegistry resourceRegistry = this.context.getModuleContext().getResourceRegistry();
		if(!resourceRegistry.hasEntry(resourceType)){
			return null;
		}
		RegistryEntry entry = resourceRegistry.getEntry(resourceType);
		if (idPrefix != null) {
			return idPrefix + resourceType.replace('/', '.');
		} else {
			Class<?> resourceClass = entry.getResourceInformation().getResourceClass();
			return resourceClass.getName();
		}
	}

	private String getName(ResourceInformation information) {
		String resourceType = information.getResourceType();
		StringBuilder name = new StringBuilder();
		for (int i = 0; i < resourceType.length(); i++) {
			if (i == 0 || resourceType.charAt(i - 1) == '/') {
				name.append(Character.toUpperCase(resourceType.charAt(i)));
			} else if (resourceType.charAt(i) != '/') {
				name.append(resourceType.charAt(i));
			}
		}
		return name.toString();
	}

	private MetaResourceRepository discoverRepository(ResourceRepositoryInformation repositoryInformation,
			MetaResource metaResource, ResourceRepositoryAdapter resourceRepository) {

		MetaResourceRepository meta = new MetaResourceRepository();
		meta.setResourceType(metaResource);
		meta.setExposed(repositoryInformation.isExposed());
		meta.setName(metaResource.getName() + "$repository");
		meta.setId(metaResource.getId() + "$repository");

		for (RepositoryAction action : repositoryInformation.getActions().values()) {
			MetaResourceAction metaAction = new MetaResourceAction();
			metaAction.setName(action.getName());
			metaAction.setActionType(MetaResourceAction.MetaRepositoryActionType.valueOf(action.getActionType().toString()));
			metaAction.setParent(meta, true);
		}

		// TODO avoid use of ResourceRepositoryAdapter by enriching ResourceRepositoryInformation
		Object repository = resourceRepository.getImplementation();
		if (repository instanceof ResourceRepository) {
			setListInformationTypes(repository, meta);
		}
		meta.setBulk(repository instanceof BulkResourceRepository);
		return meta;
	}

	private void setListInformationTypes(final Object repository, final MetaResourceRepository meta) {
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
						MetaDataObject listMetaType = (MetaDataObject) allocateMetaElement(metaType).get();
						meta.setListMetaType(listMetaType);
					}
					if (!linksType.equals(LinksInformation.class)) {
						MetaDataObject listLinksType = (MetaDataObject) allocateMetaElement(linksType).get();
						meta.setListLinksType(listLinksType);
					}
				}
				return null;
			}
		});
	}


	private void addAttribute(MetaResourceBase resource, ResourceField field) {
		MetaResourceField attr = new MetaResourceField();

		attr.setUnderlyingName(field.getUnderlyingName());

		attr.setParent(resource, true);
		attr.setId(resource.getId() + "." + field.getUnderlyingName());
		attr.setName(field.getJsonName());
		attr.setAssociation(field.getResourceFieldType() == ResourceFieldType.RELATIONSHIP);
		attr.setFieldType(field.getResourceFieldType());
		attr.setVersionRange(field.getVersionRange());
		attr.setDerived(false);

		attr.setLazy(field.getSerializeType() == SerializeType.LAZY);
		attr.setSortable(field.getAccess().isSortable());
		attr.setFilterable(field.getAccess().isFilterable());
		attr.setInsertable(field.getAccess().isPostable() && resource.isInsertable());
		attr.setUpdatable(field.getAccess().isPatchable() && resource.isUpdatable());
		attr.setReadable(field.getAccess().isReadable() && resource.isReadable());

		boolean isPrimitive = ClassUtils.isPrimitiveType(field.getType());
		boolean isId = field.getResourceFieldType() == ResourceFieldType.ID;
		attr.setNullable(!isPrimitive && !isId);
	}
}