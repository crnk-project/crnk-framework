package io.crnk.gen.typescript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.crnk.gen.typescript.processor.TSImportProcessor;
import io.crnk.gen.typescript.processor.TSIndexFileProcessor;
import io.crnk.gen.typescript.processor.TSExpressionObjectProcessor;
import io.crnk.gen.typescript.processor.TSSourceProcessor;
import io.crnk.gen.typescript.transform.TSMetaDataObjectTransformation;
import io.crnk.gen.typescript.transform.TSMetaEnumTypeTransformation;
import io.crnk.gen.typescript.transform.TSMetaPrimitiveTypeTransformation;
import io.crnk.gen.typescript.writer.TSCodeStyle;

public class TSGeneratorConfiguration {

	private Map<String, String> npmPackageMapping = new HashMap<>();

	private String npmPackageName;

	private TSCodeStyle codeStyle = new TSCodeStyle();

	private List<TSSourceProcessor> sourceProcessors = new ArrayList<>();

	private List<String> metaTransformationClassNames = new ArrayList<>();

	private String runtimeClassName = "io.crnk.gen.runtime.deltaspike.DeltaspikeIntegration";

	private boolean generateExpressions = false;

	public TSGeneratorConfiguration() {
		sourceProcessors.add(new TSExpressionObjectProcessor());
		sourceProcessors.add(new TSImportProcessor());
		sourceProcessors.add(new TSIndexFileProcessor());

		// classes are loaded by the application class loader
		metaTransformationClassNames.add(TSMetaDataObjectTransformation.class.getName());
		metaTransformationClassNames.add(TSMetaEnumTypeTransformation.class.getName());
		metaTransformationClassNames.add(TSMetaPrimitiveTypeTransformation.class.getName());

		String crnkNpm = "@crnk/meta";
		npmPackageMapping.put("io.crnk.meta", crnkNpm);
		npmPackageMapping.put("io.crnk.meta.resource", crnkNpm);
		npmPackageMapping.put("io.crnk.jpa", crnkNpm);
	}

	public boolean getGenerateExpressions(){
		return generateExpressions;
	}

	public void setGenerateExpressions(boolean generateExpressions){
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

	public String getNpmPackageName() {
		return npmPackageName;
	}

	public void setNpmPackageName(String npmPackageName) {
		this.npmPackageName = npmPackageName;
	}

	public TSCodeStyle getCodeStyle() {
		return codeStyle;
	}

	public List<TSSourceProcessor> getSourceProcessors() {
		List<TSSourceProcessor> enabledSourceProcessors = new ArrayList<>();
		for(TSSourceProcessor sourceProcessor : sourceProcessors){
			if(generateExpressions || !(sourceProcessor instanceof TSExpressionObjectProcessor)){
				enabledSourceProcessors.add(sourceProcessor);
			}
		}
		return enabledSourceProcessors;
	}

	public List<String> getMetaTransformationClassNames() {
		return metaTransformationClassNames;
	}

	public String getRuntimeClassName() {
		return runtimeClassName;
	}

	public void setRuntimeClassName(String runtimeClassName) {
		this.runtimeClassName = runtimeClassName;
	}
}
