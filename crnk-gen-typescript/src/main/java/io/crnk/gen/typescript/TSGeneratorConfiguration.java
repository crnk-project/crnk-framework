package io.crnk.gen.typescript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.crnk.gen.typescript.model.libraries.ExpressionLibrary;
import io.crnk.gen.typescript.processor.TSEmptyObjectFactoryProcessor;
import io.crnk.gen.typescript.processor.TSExpressionObjectProcessor;
import io.crnk.gen.typescript.processor.TSImportProcessor;
import io.crnk.gen.typescript.processor.TSIndexFileProcessor;
import io.crnk.gen.typescript.processor.TSSourceProcessor;
import io.crnk.gen.typescript.transform.TSMetaDataObjectTransformation;
import io.crnk.gen.typescript.transform.TSMetaEnumTypeTransformation;
import io.crnk.gen.typescript.transform.TSMetaPrimitiveTypeTransformation;
import io.crnk.gen.typescript.transform.TSMetaResourceRepositoryTransformation;
import io.crnk.gen.typescript.writer.TSCodeStyle;

public class TSGeneratorConfiguration {

	private Map<String, String> npmPackageMapping = new HashMap<>();

	private String npmPackageName;

	private String npmPackageVersion;

	private TSCodeStyle codeStyle = new TSCodeStyle();

	private List<TSSourceProcessor> sourceProcessors = new ArrayList<>();

	private List<String> metaTransformationClassNames = new ArrayList<>();

	private String metaResolverClassName = "io.crnk.gen.runtime.deltaspike.DeltaspikeMetaResolver";

	private boolean generateExpressions = false;

	private String sourceDirectoryName = "src";

	private String npmLicense = null;

	private String npmDescription = null;

	private Map<String, String> npmPeerDependencies = new HashMap<>();

	private Map<String, String> npmDevDependencies = new HashMap<>();

	private Set<String> includes = new HashSet<>();

	private Set<String> excludes = new HashSet<>();

	// TODO setup crnk frontend library project
	private String expressionLibrary;

	public TSGeneratorConfiguration() {
		sourceProcessors.add(new TSExpressionObjectProcessor());
		sourceProcessors.add(new TSImportProcessor());
		sourceProcessors.add(new TSIndexFileProcessor());
		sourceProcessors.add(new TSEmptyObjectFactoryProcessor());

		// classes are loaded by the application class loader
		metaTransformationClassNames.add(TSMetaDataObjectTransformation.class.getName());
		metaTransformationClassNames.add(TSMetaEnumTypeTransformation.class.getName());
		metaTransformationClassNames.add(TSMetaPrimitiveTypeTransformation.class.getName());
		metaTransformationClassNames.add(TSMetaResourceRepositoryTransformation.class.getName());

		String crnkNpm = "@crnk/core";
		npmPackageMapping.put("io.crnk.meta", crnkNpm);
		npmPackageMapping.put("io.crnk.meta.resource", crnkNpm);
		npmPackageMapping.put("io.crnk.jpa", crnkNpm);
		npmPackageMapping.put("io.crnk.core.resource.links", crnkNpm);

		npmPeerDependencies.put("ngrx-json-api", "1.2.0");
		npmPeerDependencies.put("rxjs", "5.2.0");
		npmPeerDependencies.put("lodash", "4.17.4");

		npmDevDependencies.put("typescript", "2.2.0");
		npmDevDependencies.put("ncp", "2.0.0");
		npmDevDependencies.put("rimraf", "2.5.4");
	}

	/**
	 * @return list of meta ids (or prefixes there of) that should be generated. An empty list generates everything.
	 */
	public Set<String> getIncludes() {
		return includes;
	}

	public void setIncludes(Set<String> includes) {
		this.includes = includes;
	}

	/**
	 * TODO internal use only, crnk-ngrx frontend library needs to be setup
	 */
	@Deprecated
	public String getExpressionLibrary() {
		return expressionLibrary;
	}

	public void setExpressionLibrary(String expressionLibrary) {
		this.expressionLibrary = expressionLibrary;

		ExpressionLibrary.EXPRESSION_SOURCE.setNpmPackage(expressionLibrary + "/binding/expression");
		ExpressionLibrary.JSONAPI_SOURCE.setNpmPackage(expressionLibrary + "/binding/jsonapi");
	}

	/**
	 * @return list of meta ids (or prefixes there of) that should not be exported to typescript
	 */
	public Set<String> getExcludes() {
		return excludes;
	}

	public void setExcludes(Set<String> excludes) {
		this.excludes = excludes;
	}

	public boolean getGenerateExpressions() {
		return generateExpressions;
	}

	public void setGenerateExpressions(boolean generateExpressions) {
		this.generateExpressions = generateExpressions;
	}

	/**
	 * @return Maps meta ID prefixes to NPM package names. Frequently, the meta ID roughly correspond to Java package names if
	 * not configured otherwise.
	 */
	public Map<String, String> getNpmPackageMapping() {
		return npmPackageMapping;
	}

	public void setNpmPackageMapping(Map<String, String> packageMapping) {
		this.npmPackageMapping = packageMapping;
	}

	/**
	 * @return npmPackageName for publishing.
	 */
	public String getNpmPackageName() {
		return npmPackageName;
	}

	public void setNpmPackageName(String npmPackageName) {
		this.npmPackageName = npmPackageName;
	}

	/**
	 * @return code style to use for generated TypeScript sources.
	 */
	public TSCodeStyle getCodeStyle() {
		return codeStyle;
	}

	/**
	 * @return TSSourceProcessor instances to use during generation.
	 */
	public List<TSSourceProcessor> getSourceProcessors() {
		List<TSSourceProcessor> enabledSourceProcessors = new ArrayList<>();
		for (TSSourceProcessor sourceProcessor : sourceProcessors) {
			if (generateExpressions || !(sourceProcessor instanceof TSExpressionObjectProcessor)) {
				enabledSourceProcessors.add(sourceProcessor);
			}
		}
		return enabledSourceProcessors;
	}

	/**
	 * @return TSMetaTransformation class names to instantiate and use.
	 */
	public List<String> getMetaTransformationClassNames() {
		return metaTransformationClassNames;
	}


	public String getMetaResolverClassName() {
		return metaResolverClassName;
	}

	public void setMetaResolverClassName(String metaResolverClassName) {
		this.metaResolverClassName = metaResolverClassName;
	}

	/**
	 * @return version to use for the npm package, by default matches the version of this build.
	 */
	public String getNpmPackageVersion() {
		return npmPackageVersion;
	}

	public void setNpmPackageVersion(String npmPackageVersion) {
		this.npmPackageVersion = npmPackageVersion;
	}

	public String getSourceDirectoryName() {
		return sourceDirectoryName;
	}

	public void setSourceDirectoryName(String sourceDirectoryName) {
		this.sourceDirectoryName = sourceDirectoryName;
	}

	public String getNpmDescription() {
		return npmDescription;
	}

	public String getNpmLicense() {
		return npmLicense;
	}

	public void setNpmLicense(String npmLicense) {
		this.npmLicense = npmLicense;
	}

	public void setNpmDescription(String npmDescription) {
		this.npmDescription = npmDescription;
	}


	/**
	 * @Deprecated use npmPeerDependencies
	 */
	@Deprecated
	public Map<String, String> getNpmDependencies() {
		return npmPeerDependencies;
	}

	/**
	 * @Deprecated use npmPeerDependencies
	 */
	@Deprecated
	public void setNpmDependencies(Map<String, String> npmDependencies) {
		this.npmPeerDependencies = npmDependencies;
	}

	public Map<String, String> getPeerNpmDependencies() {
		return npmPeerDependencies;
	}

	public void setPeerNpmDependencies(Map<String, String> npmPeerDependencies) {
		this.npmPeerDependencies = npmPeerDependencies;
	}

	public Map<String, String> getNpmDevDependencies() {
		return npmDevDependencies;
	}

	public void setNpmDevDependencies(Map<String, String> npmDevDependencies) {
		this.npmDevDependencies = npmDevDependencies;
	}
}
