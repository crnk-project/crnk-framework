package io.crnk.gen.typescript.internal;

import java.io.File;

import io.crnk.gen.typescript.TSGeneratorConfig;


public interface TSGeneratorRuntimeContext {

	void setOutputDir(File outputDir);

	void setConfig(TSGeneratorConfig config);
}