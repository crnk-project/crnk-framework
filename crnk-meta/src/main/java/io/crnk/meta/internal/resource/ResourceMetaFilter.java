package io.crnk.meta.internal.resource;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import io.crnk.core.engine.filter.FilterBehavior;
import io.crnk.core.engine.filter.ResourceFilterDirectory;
import io.crnk.core.engine.http.HttpMethod;
import io.crnk.core.engine.information.bean.BeanAttributeInformation;
import io.crnk.core.engine.information.bean.BeanInformation;
import io.crnk.core.engine.information.resource.ResourceField;
import io.crnk.core.engine.information.resource.ResourceFieldType;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.engine.information.resource.ResourceInformationProvider;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.engine.registry.RegistryEntry;
import io.crnk.core.engine.registry.ResourceRegistry;
import io.crnk.core.module.Module;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaCollectionType;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaListType;
import io.crnk.meta.model.MetaPrimaryKey;
import io.crnk.meta.model.MetaSetType;
import io.crnk.meta.model.resource.MetaJsonObject;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceBase;
import io.crnk.meta.model.resource.MetaResourceField;
import io.crnk.meta.provider.MetaFilter;
import io.crnk.meta.provider.MetaProviderContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceMetaFilter implements MetaFilter {

	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceMetaFilter.class);

	private final MetaProviderContext context;

	private final ResourceMetaParitition partition;


	public ResourceMetaFilter(ResourceMetaParitition partition, MetaProviderContext context) {
		PreconditionUtil.assertNotNull("must not be null", context);
		this.context = context;
		this.partition = partition;
	}


	@Override
	public MetaElement adjustForRequest(MetaElement element, QueryContext queryContext) {
		if (element instanceof MetaResource) {
			MetaResource metaResource = (MetaResource) element;
			return adjustResourceForRequest(metaResource, queryContext);
		}
		else if (element instanceof MetaResourceField && element.getParent() instanceof MetaResource) {
			// TODO also cover MetaResourceBase by expending InformationModel accordingly
			MetaResourceField field = (MetaResourceField) element;
			return adjustFieldForRequest(field, queryContext);
		}
		return element;
	}

	private MetaElement adjustFieldForRequest(MetaResourceField field, QueryContext queryContext) {
		MetaResource metaResource = (MetaResource) field.getParent();

		Module.ModuleContext moduleContext = context.getModuleContext();
		RegistryEntry entry = moduleContext.getResourceRegistry().getEntry(metaResource.getResourceType());
		ResourceInformation resourceInformation = entry.getResourceInformation();
		ResourceField fieldInformation = resourceInformation.findFieldByUnderlyingName(field.getUnderlyingName());

		ResourceFilterDirectory filterBehaviorProvider = moduleContext.getResourceFilterDirectory();
		boolean readable =
				metaResource.isReadable()
						&& filterBehaviorProvider.get(fieldInformation, HttpMethod.GET, queryContext) == FilterBehavior.NONE;
		boolean insertable = metaResource.isInsertable()
				&& filterBehaviorProvider.get(fieldInformation, HttpMethod.POST, queryContext) == FilterBehavior.NONE;
		boolean updatable = metaResource.isUpdatable()
				&& filterBehaviorProvider.get(fieldInformation, HttpMethod.PATCH, queryContext) == FilterBehavior.NONE;

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

	private MetaElement adjustResourceForRequest(MetaResource metaResource, QueryContext queryContext) {
		Module.ModuleContext moduleContext = context.getModuleContext();
		RegistryEntry entry = moduleContext.getResourceRegistry().getEntry(metaResource.getResourceType());
		ResourceInformation resourceInformation = entry.getResourceInformation();

		ResourceFilterDirectory filterBehaviorProvider = moduleContext.getResourceFilterDirectory();
		boolean readable = metaResource.isReadable()
				&& filterBehaviorProvider.get(resourceInformation, HttpMethod.GET, queryContext) == FilterBehavior.NONE;
		boolean insertable = metaResource.isInsertable()
				&& filterBehaviorProvider.get(resourceInformation, HttpMethod.POST, queryContext) == FilterBehavior.NONE;
		boolean updatable = metaResource.isUpdatable()
				&& filterBehaviorProvider.get(resourceInformation, HttpMethod.PATCH, queryContext) == FilterBehavior.NONE;
		boolean deletable = metaResource.isDeletable()
				&& filterBehaviorProvider.get(resourceInformation, HttpMethod.DELETE, queryContext) == FilterBehavior.NONE;

		// hide element if no permission
		if (!readable && !insertable && !updatable && !deletable) {
			return null;
		}

		// update element if necessary
		if (metaResource.isReadable() != readable || metaResource.isUpdatable() != updatable
				|| metaResource.isInsertable() != insertable || metaResource.isDeletable() != deletable) {
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
				MetaAttribute attr = metaResource.getAttribute(field.getJsonName());
				if (field.getOppositeName() != null) {
					String oppositeType = field.getOppositeResourceType();

					String oppositeTypeId = partition.getId(oppositeType);
					if(oppositeType != null) {
						MetaResource oppositeMeta = (MetaResource) context.getMetaElement(oppositeTypeId).get();
						ResourceInformation oppositeInformation = getResourceInformation(oppositeMeta, false);
						ResourceField oppositeField = oppositeInformation.findFieldByUnderlyingName(field.getOppositeName());
						PreconditionUtil.verify(oppositeField != null, "opposite field %s.%s not found",
								field.getResourceInformation().getResourceType(), field.getOppositeName());
						try {
							MetaAttribute oppositeAttr = oppositeMeta.getAttribute(oppositeField.getJsonName());
							PreconditionUtil.assertNotNull(attr.getId() + " opposite not found", oppositeAttr);
							attr.setOppositeAttribute(oppositeAttr);
							if (field.isMappedBy()) {
								oppositeAttr.setOwner(true);
							}
						}
						catch (IllegalStateException e) {
							throw new IllegalStateException(
									"failed to resolve opposite for field=" + field + ", oppositeTypeId=" + oppositeTypeId, e);
						}
					}
				}
				else {
					attr.setOwner(true);
				}
			}

			ResourceField idField = information.getIdField();
			if (idField != null) {
				MetaAttribute idAttr = metaResource.getAttribute(idField.getJsonName());
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
			ResourceField field = information.findFieldByUnderlyingName(attr.getUnderlyingName());
			PreconditionUtil.assertNotNull(attr.getName(), field);

			if (field.getResourceFieldType() == ResourceFieldType.RELATIONSHIP) {
				String oppositeType = field.getOppositeResourceType();
				String oppositeId = partition.getId(oppositeType);
				Optional<MetaElement> optOppositeMeta = oppositeId != null ? context.getMetaElement(oppositeId) : Optional.empty();
				if (optOppositeMeta.isPresent()) {

					MetaResource oppositeMeta = (MetaResource) optOppositeMeta.get();

					if (field.isCollection()) {
						boolean isSet = Set.class.isAssignableFrom(field.getType());
						String suffix = (isSet ? "$set" : "$list");
						Optional<MetaElement> optMetaCollection = context.getMetaElement(oppositeId + suffix);
						MetaCollectionType metaCollection;
						if (optMetaCollection.isPresent()) {
							metaCollection = (MetaCollectionType) optMetaCollection.get();
						}
						else {
							metaCollection = isSet ? new MetaSetType() : new MetaListType();
							metaCollection.setId(oppositeMeta.getId() + suffix);
							metaCollection.setName(oppositeMeta.getName() + suffix);
							metaCollection.setImplementationType(field.getGenericType());
							metaCollection.setElementType(oppositeMeta);
							partition.addElement(null, metaCollection);
						}
						attr.setType(metaCollection);

					}
					else {
						attr.setType(oppositeMeta);
					}
				}
				else {
					LOGGER.info("opposite meta element '{}' for element '{}' not found", oppositeId, element.getId());
				}
			}
			else {
				Type implementationType = field.getGenericType();
				MetaElement metaType = partition.allocateMetaElement(implementationType).get();
				attr.setType(metaType.asType());
			}
		}
		else if (element instanceof MetaAttribute && element.getParent() instanceof MetaJsonObject) {
			MetaAttribute attr = (MetaAttribute) element;
			MetaDataObject parent = attr.getParent();

			BeanInformation parentBeanInfo = BeanInformation.get(parent.getImplementationClass());
			BeanAttributeInformation attrInformation = parentBeanInfo.getAttributeByJsonName(attr.getName());
			if (attrInformation == null) {
				throw new IllegalStateException(
						"attribute not found " + parent.getImplementationClass().getName() + "." + attr.getName());
			}

			Type implementationType = attrInformation.getType();
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
