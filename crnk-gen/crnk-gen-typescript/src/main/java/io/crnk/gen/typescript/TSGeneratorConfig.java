package io.crnk.gen.typescript;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.crnk.gen.base.GeneratorModuleConfigBase;
import io.crnk.gen.typescript.model.writer.TSCodeStyle;
import io.crnk.gen.typescript.processor.TSEmptyObjectFactoryProcessor;
import io.crnk.gen.typescript.processor.TSExpressionObjectProcessor;
import io.crnk.gen.typescript.processor.TSImportProcessor;
import io.crnk.gen.typescript.processor.TSIndexFileProcessor;
import io.crnk.gen.typescript.processor.TSSourceProcessor;
import io.crnk.gen.typescript.transform.TSMetaDataObjectTransformation;
import io.crnk.gen.typescript.transform.TSMetaEnumTypeTransformation;
import io.crnk.gen.typescript.transform.TSMetaPrimitiveTypeTransformation;
import io.crnk.gen.typescript.transform.TSMetaResourceRepositoryTransformation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TSGeneratorConfig extends GeneratorModuleConfigBase {

    private TSCodeStyle codeStyle = new TSCodeStyle();

    protected List<String> metaTransformationClassNames = new ArrayList<>();

    private boolean expressions = false;

    private Set<String> includes = new HashSet<>();

    private Set<String> excludes = new HashSet<>();

    private TSNpmConfiguration npm = new TSNpmConfiguration();

    private String sourceDirectoryName = "src";

    private File genDir = null;

    private File buildDir = null;

    private TSResourceFormat format = TSResourceFormat.NGRX;


    @JsonIgnore
    private List<TSSourceProcessor> sourceProcessors = new ArrayList<>();

    public TSGeneratorConfig() {
        sourceProcessors.add(new TSExpressionObjectProcessor());
        sourceProcessors.add(new TSImportProcessor());
        sourceProcessors.add(new TSIndexFileProcessor());
        sourceProcessors.add(new TSEmptyObjectFactoryProcessor());

        // classes are loaded by the application class loader
        metaTransformationClassNames.add(TSMetaDataObjectTransformation.class.getName());
        metaTransformationClassNames.add(TSMetaEnumTypeTransformation.class.getName());
        metaTransformationClassNames.add(TSMetaPrimitiveTypeTransformation.class.getName());
        metaTransformationClassNames.add(TSMetaResourceRepositoryTransformation.class.getName());
    }


    /**
     * @return location where the generated sources are placed.
     */
    public File getGenDir() {
        if (genDir == null) {
            return new File(buildDir, "generated/sources/typescript/");
        }
        return genDir;
    }

    public void setGenDir(File genDir) {
        this.genDir = genDir;
    }

    public TSNpmConfiguration getNpm() {
        return npm;
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
     * @return list of meta ids (or prefixes there of) that should not be exported to typescript
     */
    public Set<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(Set<String> excludes) {
        this.excludes = excludes;
    }

    public boolean getExpressions() {
        return expressions && getFormat() != TSResourceFormat.PLAINJSON;
    }

    public void setExpressions(boolean expressions) {
        this.expressions = expressions;
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
            if (expressions || !(sourceProcessor instanceof TSExpressionObjectProcessor)) {
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

    /**
     * Only to be used with packagingEnabled is true.
     */
    public String getSourceDirectoryName() {
        return sourceDirectoryName;
    }

    public void setSourceDirectoryName(String sourceDirectoryName) {
        this.sourceDirectoryName = sourceDirectoryName;
    }

    public File getBuildDir() {
        return buildDir;
    }

    public void setBuildDir(File buildDir) {
        this.buildDir = buildDir;
    }

    public TSResourceFormat getFormat() {
        return format;
    }

    public void setFormat(TSResourceFormat format) {
        this.format = format;
    }
}
