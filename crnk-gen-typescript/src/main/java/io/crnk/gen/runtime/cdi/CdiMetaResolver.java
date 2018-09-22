package io.crnk.gen.runtime.cdi;

import io.crnk.gen.runtime.GeneratorTrigger;
import io.crnk.gen.typescript.RuntimeMetaResolver;

import java.lang.reflect.Method;

/**
 * It is quite difficult to setup a JEE application locally, so going the Deltaspike way seems the simplest approach.
 * Executed the code generation as a Deltaspike test, which is/should already be setup by the project.
 */
public class CdiMetaResolver implements RuntimeMetaResolver {

	@Override
	public void run(GeneratorTrigger context, ClassLoader classLoader) {
		try {
			Class<?> runnerClass = classLoader.loadClass(CdiRunner.class.getName());
			Object runner = runnerClass.newInstance();
			Method method = runnerClass.getMethod("run", GeneratorTrigger.class);
			method.invoke(runner, context);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
