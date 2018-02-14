package io.crnk.meta.internal.resource;

import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.ResourceInformationProvider;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.internal.utils.PropertyUtils;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.Module;
import java.util.Optional;
import io.crnk.meta.model.*;
import io.crnk.meta.model.resource.MetaJsonObject;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceBase;
import io.crnk.meta.model.resource.MetaResourceField;
import io.crnk.meta.provider.MetaFilter;
import io.crnk.meta.provider.MetaProviderContext;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Set;

public class ResourceMetaFilter implements MetaFilter {

	private final MetaProviderContext context;
	private final ResourceMetaParitition partition;


	public ResourceMetaFilter(ResourceMetaParitition partition, MetaProviderContext context) {
		PreconditionUtil.assertNotNull("must not be null", context);
		this.context = context;
		this.partition = partition;
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
		boolean readable = metaResource.isReadable() && filterBehaviorProvider.get(fieldInformation, HttpMethod.GET) == FilterBehavior.NONE;
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
	public void onInitializing(MetaElement element) {

	}

	@Override
	public void onInitialized(MetaElement element) {
		if (element instanceof MetaResourceBase) {
			MetaResourceBase metaResource = (MetaResourceBase) element;

			ResourceInformation information = getResourceInformation(metaResource, true);

			PreconditionUtil.assertNotNull(information.getResourceType(), metaResource);
			for (ResourceField field : information.getRelationshipFields()) {
				if (field.getOppositeName() != null) {
					String oppositeType = field.getOppositeResourceType();
					MetaResource oppositeMeta = (MetaResource) context.getMetaElement(partition.getId(oppositeType)).get();
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
					partition.addElement(null, primaryKey);
				}
			}
		}

		if (element instanceof MetaAttribute && element.getParent() instanceof MetaResourceBase) {
			MetaAttribute attr = (MetaAttribute) element;
			MetaResourceBase parent = (MetaResourceBase) attr.getParent();

			ResourceInformation information = getResourceInformation(parent, true);
			ResourceField field = information.findFieldByUnderlyingName(attr.getName());
			PreconditionUtil.assertNotNull(attr.getName(), field);

			if (field.getResourceFieldType() == ResourceFieldType.RELATIONSHIP) {
				String oppositeType = field.getOppositeResourceType();
				String oppositeId = partition.getId(oppositeType);
				MetaResource oppositeMeta = (MetaResource) context.getMetaElement(oppositeId).get();

				if (field.isCollection()) {
					boolean isSet = Set.class.isAssignableFrom(field.getType());
					String suffix = (isSet ? "$set" : "$list");
					Optional<MetaElement> optMetaCollection = context.getMetaElement(oppositeId + suffix);
					MetaCollectionType metaCollection;
					if (optMetaCollection.isPresent()) {
						metaCollection = (MetaCollectionType) optMetaCollection.get();
					} else {
						metaCollection = isSet ? new MetaSetType() : new MetaListType();
						metaCollection.setId(oppositeMeta.getId() + suffix);
						metaCollection.setName(oppositeMeta.getName() + suffix);
						metaCollection.setImplementationType(field.getGenericType());
						metaCollection.setElementType(oppositeMeta);
						partition.addElement(null, metaCollection);
					}
					attr.setType(metaCollection);

				} else {
					attr.setType(oppositeMeta);
				}
			} else {
				Type implementationType = field.getGenericType();
				MetaElement metaType = partition.allocateMetaElement(implementationType).get();
				attr.setType(metaType.asType());
			}
		} else if (element instanceof MetaAttribute && element.getParent() instanceof MetaJsonObject) {
			MetaAttribute attr = (MetaAttribute) element;
			MetaDataObject parent = attr.getParent();
			Type implementationType = PropertyUtils.getPropertyType(parent.getImplementationClass(), attr.getName());
			MetaElement metaType = partition.allocateMetaElement(implementationType).get();
			attr.setType(metaType.asType());
		}
	}

	private ResourceInformation getResourceInformation(MetaResourceBase meta, boolean allowNull) {
		ResourceRegistry resourceRegistry = context.getModuleContext().getResourceRegistry();
		if (meta instanceof MetaResource) {
			RegistryEntry entry = resourceRegistry.getEntry(((MetaResource) meta).getResourceType());
			if (entry != null) {
				return entry.getResourceInformation();
			}
		}

		Class<?> resourceClass = meta.getImplementationClass();
		ResourceInformationProvider infoBuilder = context.getModuleContext().getResourceInformationBuilder();
		if (infoBuilder.accept(resourceClass)) {
			return infoBuilder.build(resourceClass);
		}

		if (allowNull) {
			return null;
		}

		throw new IllegalStateException("failed to get information for " + resourceClass.getName());
	}
}
