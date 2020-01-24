package io.crnk.gen.runtime.spring;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.gen.base.SpringRuntimeConfig;
import io.crnk.gen.runtime.RuntimeContext;
import io.crnk.meta.MetaLookupImpl;
import io.crnk.meta.MetaModule;
import io.crnk.meta.MetaModuleConfig;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

public class SpringRunner {

    public void run(RuntimeContext context) {
        SpringRuntimeConfig springConfig = context.getConfig().getRuntime().getSpring();
        if (springConfig.getConfiguration() == null) {
            throw new IllegalStateException("typescriptGen.runtime.spring.configuration not specified");
        }
        if (springConfig.getProfile() == null) {
            throw new IllegalStateException("typescriptGen.runtime.spring.profile not specified");
        }
        ConfigurableApplicationContext applicationContext = null;
        try {
            Class configurationClass = getClass().getClassLoader().loadClass(springConfig.getConfiguration());

            String initializerMethodName = springConfig.getInitializerMethod();
            if (initializerMethodName != null) {
                Method initializerMethod = configurationClass.getMethod(initializerMethodName);
                initializerMethod.invoke(configurationClass);
            }

            // Spring Boot 2 broke contract with 1 (Class[] instead of Object[])
            Stream<Constructor<?>> constructors = Arrays.asList(SpringApplication.class.getConstructors()).stream();
            Constructor<?> constructor = constructors.filter(it -> it.getParameterCount() == 1).findFirst().get();
            Object constructorParam = constructor.getParameterTypes()[0] == Object.class ?
                    new Object[]{configurationClass} : new Class[]{configurationClass};

            SpringApplication application = (SpringApplication) constructor.newInstance(constructorParam);
            application.setWebApplicationType(WebApplicationType.NONE);
            application.setAdditionalProfiles(springConfig.getProfile());
            application.setDefaultProperties(springConfig.getDefaultProperties());

            applicationContext = application.run();

            CrnkBoot boot = applicationContext.getBean(CrnkBoot.class);

            MetaLookupImpl lookup;
            try {
                lookup = (MetaLookupImpl) applicationContext.getBean(MetaModule.class).getLookup();
            } catch (NoSuchBeanDefinitionException e) {
                MetaModuleConfig lookupConfig = new MetaModuleConfig();
                lookup = new MetaLookupImpl();
                lookup.addProvider(new ResourceMetaProvider());
                lookupConfig.apply(lookup);
                lookup.setModuleContext(boot.getModuleRegistry().getContext());
                lookup.initialize();
            }
            context.generate(lookup);
        } catch (IOException | IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                InstantiationException | ClassNotFoundException e) {
            throw new IllegalStateException(e);
        } finally {
            if (applicationContext != null) {
                applicationContext.close();
            }
        }
    }
}
