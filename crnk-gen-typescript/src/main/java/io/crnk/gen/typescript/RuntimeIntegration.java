package io.crnk.gen.typescript;

import java.io.File;

/**
 * Deltaspike, Spring-context, etc. necessary to setup the application
 * and fetch its meta-data.
 *
 * TODO remo: separate meta retrieval from generation.
 */
public interface RuntimeIntegration {

	public void run(File outputDir, TSGeneratorConfiguration config, ClassLoader classLoader);
}
