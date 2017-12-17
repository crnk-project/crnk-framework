package io.crnk.gen.typescript;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class TSNpmConfiguration {


	private Map<String, String> packageMapping = new HashMap<>();

	private String packageName;

	private String packageVersion;

	private String license = "UNLICENSED";

	private String description = null;

	private Map<String, String> peerDependencies = new HashMap<>();

	private Map<String, String> devDependencies = new HashMap<>();

	private String gitRepository;

	private File outputDir = null;

	private boolean packagingEnabled = true;

	public TSNpmConfiguration() {
		String crnkNpm = "@crnk/angular-ngrx";
		packageMapping.put("io.crnk.meta", crnkNpm);
		packageMapping.put("io.crnk.meta.resource", crnkNpm);
		packageMapping.put("io.crnk.jpa", crnkNpm);
		packageMapping.put("io.crnk.core.resource.links", crnkNpm);

		peerDependencies.put("ngrx-json-api", ">=2.2.0");
		peerDependencies.put("rxjs", ">=5.2.0");
		peerDependencies.put("lodash", ">=4.17.4");
		peerDependencies.put("@crnk/angular-ngrx", ">=2.0.0");
		peerDependencies.put("@ngrx/store", ">=4.0.0");
		peerDependencies.put("@ngrx/effects", ">=4");

		devDependencies.putAll(peerDependencies);
		devDependencies.put("typescript", "2.4.0");
		devDependencies.put("ncp", "2.0.0");
		devDependencies.put("rimraf", "2.5.4");
		devDependencies.put("@angular/core", "^4.0.0");
		devDependencies.put("@angular/forms", "^4.0.0");
		devDependencies.put("@angular/http", "^4.0.0");
		devDependencies.put("@angular/common", "^4.0.0");
	}

	/**
	 * @return true if npm package if generated (package.json, tsconfig, etc.). Otherwise the generator will only
	 * output the generated sources.
	 */
	public boolean isPackagingEnabled() {
		return packagingEnabled;
	}

	public void setPackagingEnabled(boolean packagingEnabled) {
		this.packagingEnabled = packagingEnabled;
	}

	/**
	 * @return directory where the compiled npm package is placed. Defaults to ${buildDir}/npm.
	 */
	public File getOutputDir() {
		return outputDir;
	}

	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}

	public Map<String, String> getPackageMapping() {
		return packageMapping;
	}

	public void setPackageMapping(Map<String, String> packageMapping) {
		this.packageMapping = packageMapping;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getPackageVersion() {
		return packageVersion;
	}

	public void setPackageVersion(String packageVersion) {
		this.packageVersion = packageVersion;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Map<String, String> getPeerDependencies() {
		return peerDependencies;
	}

	public void setPeerDependencies(Map<String, String> peerDependencies) {
		this.peerDependencies = peerDependencies;
	}

	public Map<String, String> getDevDependencies() {
		return devDependencies;
	}

	public void setDevDependencies(Map<String, String> devDependencies) {
		this.devDependencies = devDependencies;
	}

	public String getGitRepository() {
		return gitRepository;
	}

	public void setGitRepository(String gitRepository) {
		this.gitRepository = gitRepository;
	}
}
