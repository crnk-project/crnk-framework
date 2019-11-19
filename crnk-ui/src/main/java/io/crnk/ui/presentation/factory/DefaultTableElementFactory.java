package io.crnk.ui.presentation.factory;

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.stream.Collectors;

import io.crnk.core.queryspec.PathSpec;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaType;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import io.crnk.ui.presentation.PresentationEnvironment;
import io.crnk.ui.presentation.PresentationType;
import io.crnk.ui.presentation.element.DataTableElement;
import io.crnk.ui.presentation.element.PresentationElement;
import io.crnk.ui.presentation.element.TableColumnElement;
import io.crnk.ui.presentation.element.TableColumnsElement;

public class DefaultTableElementFactory implements PresentationElementFactory {

	@Override
	public boolean accepts(PresentationEnvironment env) {
		return env.getAcceptedTypes().contains(PresentationType.TABLE);
	}

	@Override
	public DataTableElement create(PresentationEnvironment env) {
		boolean sortingEnabled = true;
		boolean filtersEnabled = true;

		MetaResource resource = (MetaResource) env.getElement();

		DataTableElement table = new DataTableElement();
		table.setEditable(env.isEditable());
		table.getPagination().setEnabled(true);
		table.setComponentId("table");

		for (MetaAttribute attribute : resource.getAttributes()) {
			buildColumn(env, table.getColumns(), resource, filtersEnabled, sortingEnabled, new ArrayDeque(Arrays.asList(attribute)));
		}
		return table;
	}

	private void buildColumn(PresentationEnvironment env, TableColumnsElement columns, MetaResource resource, boolean filtersEnabled, boolean sortingEnabled,
			ArrayDeque<MetaAttribute> attributePath) {
		MetaAttribute lastAttribute = attributePath.getLast();
		MetaType type = lastAttribute.getType();
		if (type == null) {
			return; // TODO support e.g. from other services
		}

		if (isIgnored(attributePath, env) || type.isCollection()) {
			return;
		}

		String label = PresentationBuilderUtils.getLabel(attributePath);

		if (type instanceof MetaDataObject && !lastAttribute.isAssociation()) {
			for (MetaAttribute nestedAttribute : type.asDataObject().getAttributes()) {
				if (!attributePath.contains(nestedAttribute)) {
					ArrayDeque nestedPath = new ArrayDeque();
					nestedPath.addAll(attributePath);
					nestedPath.add(nestedAttribute);
					buildColumn(env, columns, resource, filtersEnabled, sortingEnabled, nestedPath);
				}
			}
		} else {
			PresentationEnvironment cellEnv = env.clone();
			cellEnv.setAttributePath(attributePath);
			cellEnv.setAcceptedTypes(Arrays.asList(PresentationType.CELL, PresentationType.DISPLAY));
			cellEnv.setType(type);

			PresentationElement cellElement = env.createElement(cellEnv);


			boolean sortable = sortingEnabled && lastAttribute.isSortable();
			PathSpec pathSpec = PathSpec.of(attributePath.stream().map(it -> it.getName()).collect(Collectors.toList()));

			//String valuePath =  PresentationBuilderUtils.getValuePath(attributePath);
			String id = pathSpec.toString(); //valuePath.join(valuePath, '.');

			TableColumnElement column = new TableColumnElement();
			column.setId(id);
			column.setComponentId("column");
			column.setLabel(label);
			column.setAttributePath(pathSpec);
			column.setEditable(env.isEditable());
			column.setComponent(cellElement);
			//column.setEditComponent();
			// column.setFilter
			column.setSortable(sortable);
			// column.setWidth
			// column.setTyleClass
			columns.add(column);
		}


	}

	private boolean isIgnored(ArrayDeque<MetaAttribute> attributePath, PresentationEnvironment env) {
		MetaAttribute last = attributePath.getLast();
		return last.isVersion() || last instanceof MetaResourceField && !((MetaResourceField) last).getVersionRange().contains(env.getRequestVersion());
	}
}
