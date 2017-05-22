package io.crnk.gen.runtime.deltaspike;

import java.io.File;
import java.lang.reflect.Method;

import io.crnk.gen.typescript.RuntimeIntegration;
import io.crnk.gen.typescript.TSGeneratorConfiguration;

/**
 * It is quite difficult to setup a JEE application locally, so going the Deltaspike way seems the simplest approach.
 * Executed the code generation as a Deltaspike test, which is/should already be setup by the project.
 */
public class DeltaspikeIntegration implements RuntimeIntegration {

	@Override
	public void run(File outputDir, TSGeneratorConfiguration config, ClassLoader classLoader) {

		try {
			Class<?> runnerClass = classLoader.loadClass(DeltaspikeRunner.class.getName());
			Object runner = runnerClass.newInstance();
			Method method = runnerClass.getMethod("run", File.class, TSGeneratorConfiguration.class);
			method.invoke(runner, outputDir, config);
		}
		catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
