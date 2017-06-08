package io.crnk.gen.typescript;

import io.crnk.gen.runtime.GeneratorTrigger;

/**
 * Deltaspike, Spring-context, etc. necessary to setup the application
 * and fetch its meta-data.
 */
public interface RuntimeMetaResolver {

	void run(GeneratorTrigger trigger, ClassLoader classLoader);
}
