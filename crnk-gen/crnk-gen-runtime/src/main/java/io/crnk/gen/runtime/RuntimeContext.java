package io.crnk.gen.runtime;

import java.io.IOException;

import io.crnk.gen.base.GeneratorConfig;
import io.crnk.gen.base.GeneratorModule;

public class RuntimeContext {

	private final GeneratorModule module;

	private final GeneratorConfig config;

	private final ClassLoader classloader;

	public RuntimeContext(GeneratorModule module, ClassLoader classloader, GeneratorConfig config) {
		this.config = config;
		this.module = module;
		this.classloader = classloader;
	}

	public GeneratorConfig getConfig() {
		return config;
	}

	/**
	 * @param lookup provided as java.lang.Object to minimize generator class loading contract
	 * @throws IOException
	 */
	public void generate(Object lookup) throws IOException {
		module.generate(lookup);
	}

	public ClassLoader getClassLoader() {
		return classloader;
	}
}
