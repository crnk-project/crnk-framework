package io.crnk.spring.setup.boot.mvc.internal;

import java.util.Map;
import java.util.Set;

import io.crnk.home.HomeModuleExtension;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

public class SpringMvcHomeModuleExtensionFactory {

	public static HomeModuleExtension create(RequestMappingHandlerMapping mapping) {
		HomeModuleExtension ext = new HomeModuleExtension();

		Map<RequestMappingInfo, HandlerMethod> handlerMethods = mapping.getHandlerMethods();
		for (RequestMappingInfo info : handlerMethods.keySet()) {
			Set<String> patterns = info.getPatternsCondition().getPatterns();
			for (String pattern : patterns) {
				if(pattern.equals("/error"))
					continue;
				ext.addPath(pattern);
			}
		}

		return ext;
	}
}
