package io.crnk.gen.typescript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

	private Map<String, String> npmDependencies = new HashMap<>();

	private Map<String, String> npmDevDependencies = new HashMap<>();

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

		npmDependencies.put("ngrx-json-api", ">=1.2.0");
		npmDependencies.put("rxjs", ">=5.2.0");

		npmDevDependencies.put("typescript", ">=2.1.5");
		npmDevDependencies.put("ncp", ">=2.0.0");
		npmDevDependencies.put("rimraf", ">=2.5.4");
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


	public Map<String, String> getNpmDependencies() {
		return npmDependencies;
	}

	public void setNpmDependencies(Map<String, String> npmDependencies) {
		this.npmDependencies = npmDependencies;
	}

	public Map<String, String> getNpmDevDependencies() {
		return npmDevDependencies;
	}

	public void setNpmDevDependencies(Map<String, String> npmDevDependencies) {
		this.npmDevDependencies = npmDevDependencies;
	}
}
