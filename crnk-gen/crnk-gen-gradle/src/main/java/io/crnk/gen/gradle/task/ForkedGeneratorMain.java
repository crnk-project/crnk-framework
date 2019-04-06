package io.crnk.gen.gradle.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.gen.base.GeneratorConfig;
import io.crnk.gen.base.GeneratorModule;
import io.crnk.gen.base.GeneratorModuleConfigBase;
import io.crnk.gen.runtime.RuntimeContext;
import io.crnk.gen.runtime.RuntimeMetaResolver;

import java.io.File;

public class ForkedGeneratorMain {

    public static void main(String[] args) {
        File configFile = new File(args[0]);
        File moduleConfigFile = new File(args[1]);
        Class moduleClass = null;
        String metaResolverClassName = null;
        try {
            moduleClass = Class.forName(args[2]);

            GeneratorModule module = (GeneratorModule) moduleClass.newInstance();

            ObjectMapper mapper = new ObjectMapper();
            GeneratorConfig config = mapper.readerFor(GeneratorConfig.class).readValue(configFile);
            GeneratorModuleConfigBase moduleConfig = mapper.readerFor(module.getConfig().getClass()).readValue(moduleConfigFile);
            module.setConfig(moduleConfig);

            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            RuntimeContext context = new RuntimeContext(module, classLoader, config);
            metaResolverClassName = config.computeMetaResolverClassName();

            RuntimeMetaResolver runtime = (RuntimeMetaResolver) Class.forName(metaResolverClassName).newInstance();
            runtime.run(context, classLoader);
            System.exit(0);
        } catch (Throwable e) {
            throw new IllegalStateException("failed to generate " + moduleClass + ", source=" + metaResolverClassName, e);
        }
    }
}
