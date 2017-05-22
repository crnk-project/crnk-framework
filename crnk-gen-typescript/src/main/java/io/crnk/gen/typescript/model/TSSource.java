package io.crnk.gen.typescript.model;

import java.util.ArrayList;
import java.util.List;

public class TSSource extends TSContainerElementBase implements TSElement {

	private List<TSImport> imports = new ArrayList<>();

	private List<TSExport> exports = new ArrayList<>();

	private String npmPackage;

	private String directory;

	private String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public void setNpmPackage(String npmPackage) {
		this.npmPackage = npmPackage;
	}

	public String getNpmPackage() {
		return npmPackage;
	}

	@Override
	public void accept(TSVisitor visitor) {
		visitor.accept(this);
	}

	public List<TSImport> getImports() {
		return imports;
	}

	public TSImport getImport(String path) {
		for (TSImport element : imports) {
			if (path.equals(element.getPath())) {
				return element;
			}
		}
		return null;
	}

	public List<TSExport> getExports() {
		return exports;
	}

	@Override
	public void setParent(TSElement parent) {
		throw new UnsupportedOperationException("sources have no parents");
	}

}
