package io.crnk.gen.typescript.internal;

import java.io.File;
import java.io.IOException;

import io.crnk.gen.runtime.GeneratorTrigger;
import io.crnk.gen.typescript.TSGeneratorConfiguration;
import io.crnk.meta.MetaLookup;


public class TSGeneratorRuntimeContextImpl implements GeneratorTrigger, TSGeneratorRuntimeContext {

	private File outputDir;

	private TSGeneratorConfiguration config;

	@Override
	public void generate(Object meta) throws IOException {
		MetaLookup metaLookup = (MetaLookup) meta;

		TSGenerator gen = new TSGenerator(outputDir, metaLookup, config);
		gen.run();
	}

	@Override
	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}

	@Override
	public void setConfig(TSGeneratorConfiguration config) {
		this.config = config;
	}
}