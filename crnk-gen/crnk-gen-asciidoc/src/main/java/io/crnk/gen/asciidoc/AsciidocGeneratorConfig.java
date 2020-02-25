package io.crnk.gen.asciidoc;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import io.crnk.gen.base.GeneratorModuleConfigBase;

public class AsciidocGeneratorConfig extends GeneratorModuleConfigBase {

	private Set<String> excludes = new HashSet<>();

	private File genDir = null;

	private File buildDir = null;

	private String title = "API Reference";

	private int baseDepth;

	private Set<File> docletPaths = new HashSet<>();

	private boolean graphEnabled = true;

	/**
	 * @return base depth of sections, e.g. baseDepth=0 will lead to new chapters with "# title", whereas baseDepth=2 will lead to "### title"
	 */
	public int getBaseDepth() {
		return baseDepth;
	}

	public void setBaseDepth(int baseDepth) {
		this.baseDepth = baseDepth;
	}

	public boolean isGraphEnabled() {
		return graphEnabled;
	}

	public void setGraphEnabled(boolean graphEnabled) {
		this.graphEnabled = graphEnabled;
	}

	/**
	 * @return location where the generated sources are placed.
	 */
	public File getGenDir() {
		if (genDir == null) {
			return new File(buildDir, "generated/sources/asciidoc/");
		}
		return genDir;
	}

	public void setGenDir(File genDir) {
		this.genDir = genDir;
	}

	/**
	 * @return list of prefixes that should not be exported to generated
	 */
	public Set<String> getExcludes() {
		return excludes;
	}

	public void setExcludes(Set<String> excludes) {
		this.excludes = excludes;
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

	public Set<File> getDocletPaths() {
		return docletPaths;
	}

	public void setDocletPaths(Set<File> docletPaths) {
		this.docletPaths = docletPaths;
	}
}
