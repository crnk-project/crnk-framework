package io.crnk.gen.typescript.model;

import java.util.ArrayList;
import java.util.List;

public abstract class TSImportExportBase extends TSElementBase {

	private List<String> typeNames = new ArrayList<>();

	private String path;

	public List<String> getTypeNames() {
		return typeNames;
	}

	public void addTypeName(String typeName) {
		if (!typeNames.contains(typeName)) {
			typeNames.add(typeName);
		}
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		if(path.contains("default.paged.links.informati")){
			System.out.println("WSTF");
		}
		this.path = path;
	}
}
