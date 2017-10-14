package io.crnk.meta.internal;

import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.meta.model.MetaElement;

import java.util.HashMap;
import java.util.Map;

public class MetaIdProvider {

	private Map<String, String> packageIdMapping = new HashMap<>();


	public String computeIdPrefixFromPackage(Class<?> implClass, MetaElement element) {
		Package implPackage = implClass.getPackage();
		if (implPackage == null && implClass.isArray()) {
			implPackage = implClass.getComponentType().getPackage();
		}
		PreconditionUtil.verify(implPackage != null, "%s does not belong to a package", implClass.getName());

		String packageName = implPackage.getName();
		StringBuilder idInfix = new StringBuilder(".");
		while (true) {

			String idPrefix = packageIdMapping.get(packageName);
			if (idPrefix != null) {
				return idPrefix + idInfix;
			}
			int sep = packageName.lastIndexOf('.');
			if (sep == -1) {
				break;
			}
			idInfix.append(packageName.substring(sep + 1));
			idInfix.append(".");
			packageName = packageName.substring(0, sep);
		}
		return implPackage.getName() + ".";
	}


	public void putIdMapping(String packageName, String idPrefix) {
		packageIdMapping.put(packageName, idPrefix);
	}



}
