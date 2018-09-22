package io.crnk.spring.setup.boot.mvc;

import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.module.Module;
import io.crnk.core.module.ModuleExtension;
import io.crnk.spring.setup.boot.core.CrnkCoreProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Module to register the Spring exception mappers with Crnk.
 */
public class SpringMvcModule implements Module {

	@Autowired(required = false)
	private Collection<RequestMappingHandlerMapping> handlerMappings;

	@Autowired
	private CrnkCoreProperties properties;

	@Override
	public String getModuleName() {
		return "spring.mvc";
	}

	@Override
	public void setupModule(ModuleContext context) {
		setupHomeExtension(context);
	}

	private void setupHomeExtension(ModuleContext context) {
		if (ClassUtils.existsClass("io.crnk.home.HomeModuleExtension")) {
			try {
				Class clazz = Class.forName("io.crnk.spring.setup.boot.mvc.internal.SpringMvcHomeModuleExtensionFactory");
				Method method = clazz.getMethod("create", String.class,
						Collection.class);

				String pathPrefix = properties.getPathPrefix();

				ModuleExtension homeExtension = (ModuleExtension) method.invoke(clazz, pathPrefix, handlerMappings);
				context.addExtension(homeExtension);
			} catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
	}
}
