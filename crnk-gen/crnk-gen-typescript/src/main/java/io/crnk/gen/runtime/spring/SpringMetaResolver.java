package io.crnk.gen.runtime.spring;

import io.crnk.gen.runtime.GeneratorTrigger;
import io.crnk.gen.typescript.RuntimeMetaResolver;

import java.lang.reflect.Method;

public class SpringMetaResolver implements RuntimeMetaResolver {

	@Override
	public void run(GeneratorTrigger context, ClassLoader classLoader) {
		try {
			Class<?> runnerClass = classLoader.loadClass(SpringRunner.class.getName());
			Object runner = runnerClass.newInstance();
			Method method = runnerClass.getMethod("run", GeneratorTrigger.class);
			method.invoke(runner, context);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
}
