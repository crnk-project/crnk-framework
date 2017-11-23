package io.crnk.gen.typescript.internal;

import java.io.File;
import java.io.IOException;

import io.crnk.gen.runtime.GeneratorTrigger;
import io.crnk.gen.typescript.TSGeneratorConfig;
import io.crnk.meta.MetaLookup;


public class TSGeneratorRuntimeContextImpl implements GeneratorTrigger, TSGeneratorRuntimeContext {

	private File outputDir;

	private TSGeneratorConfig config;

	private ClassLoader classloader;

	@Override
	public void generate(Object meta) throws IOException {
		MetaLookup metaLookup = (MetaLookup) meta;

		TSGenerator gen = new TSGenerator(outputDir, metaLookup, config);
		gen.run();
	}

	@Override
	public ClassLoader getClassLoader() {
		if (classloader == null) {
			throw new IllegalStateException();
		}
		return classloader;
	}

	@Override
	public void setClassLoader(ClassLoader classloader) {
		this.classloader = classloader;
	}

	@Override
	public void setOutputDir(File outputDir) {
		this.outputDir = outputDir;
	}

	@Override
	public void setConfig(TSGeneratorConfig config) {
		this.config = config;
	}
}