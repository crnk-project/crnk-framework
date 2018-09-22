package io.crnk.gen.typescript.internal;

import io.crnk.gen.typescript.TSGeneratorConfig;

import java.io.File;


public interface TSGeneratorRuntimeContext {

	void setOutputDir(File outputDir);

	void setConfig(TSGeneratorConfig config);
}