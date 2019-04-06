package io.crnk.gen.runtime.spring;

import io.crnk.gen.runtime.RuntimeContext;
import io.crnk.gen.runtime.RuntimeMetaResolver;

import java.lang.reflect.Method;

public class SpringMetaResolver implements RuntimeMetaResolver {

    @Override
    public void run(RuntimeContext context, ClassLoader classLoader) {
        try {
            Class<?> runnerClass = classLoader.loadClass(SpringRunner.class.getName());
            Object runner = runnerClass.newInstance();
            Method method = runnerClass.getMethod("run", RuntimeContext.class);
            method.invoke(runner, context);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
