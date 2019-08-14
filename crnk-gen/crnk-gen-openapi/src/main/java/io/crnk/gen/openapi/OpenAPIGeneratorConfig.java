package io.crnk.gen.openapi;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import io.crnk.gen.base.GeneratorModuleConfigBase;

public class OpenAPIGeneratorConfig extends GeneratorModuleConfigBase {
	private File genDir = null;
	private File buildDir = null;
	private String title = "OpenAPI Reference";

	/**
	 * @return location where the generated sources are placed.
	 */
	public File getGenDir() {
		if (genDir == null) {
			return new File(buildDir, "generated/source/openapi/");
		}
		return genDir;
	}
	public void setGenDir(File genDir) {
		this.genDir = genDir;
	}

	public File getBuildDir() {
		return buildDir;
	}
	public void setBuildDir(File buildDir) {
		this.buildDir = buildDir;
	}

	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
}
