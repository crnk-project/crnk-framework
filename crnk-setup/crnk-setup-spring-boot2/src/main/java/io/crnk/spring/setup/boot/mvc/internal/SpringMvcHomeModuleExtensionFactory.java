package io.crnk.spring.setup.boot.mvc.internal;

import io.crnk.home.HomeModuleExtension;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class SpringMvcHomeModuleExtensionFactory {

	public static HomeModuleExtension create(String pathPrefix, Collection<RequestMappingHandlerMapping> mappings) {
		HomeModuleExtension ext = new HomeModuleExtension();

		for (RequestMappingHandlerMapping mapping : mappings) {
			Map<RequestMappingInfo, HandlerMethod> handlerMethods = mapping.getHandlerMethods();
			for (RequestMappingInfo info : handlerMethods.keySet()) {
				Set<String> patterns = info.getPatternsCondition().getPatterns();
				for (String pattern : patterns) {
					if (pattern.equals("/error") || pattern.contains("*")) {
						continue;
					}

					if (pathPrefix == null) {
						ext.addPath(pattern);
					} else if (pattern.startsWith(pathPrefix)) {
						ext.addPath(pattern.substring(pathPrefix.length()));
					}
				}
			}
		}

		return ext;
	}
}
