package io.crnk.spring.setup.boot.mvc;

import java.lang.reflect.Method;

import io.crnk.core.engine.internal.utils.ClassUtils;
import io.crnk.core.module.Module;
import io.crnk.core.module.ModuleExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/**
 * Module to register the Spring exception mappers with Crnk.
 */
public class SpringMvcModule implements Module {

	@Autowired
	private RequestMappingHandlerMapping handlerMapping;


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
				Method method = clazz.getMethod("create",
						RequestMappingHandlerMapping.class);
				ModuleExtension homeExtension = (ModuleExtension) method.invoke(clazz, handlerMapping);
				context.addExtension(homeExtension);
			}
			catch (Exception e) {
				throw new IllegalStateException(e);
			}
		}
	}
}
