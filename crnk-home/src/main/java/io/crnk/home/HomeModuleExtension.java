package io.crnk.home;

import io.crnk.core.module.Module;
import io.crnk.core.module.ModuleExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeModuleExtension implements ModuleExtension {

	private List<String> paths = new ArrayList<>();

	@Override
	public Class<? extends Module> getTargetModule() {
		return HomeModule.class;
	}

	@Override
	public boolean isOptional() {
		return true;
	}

	public void addPath(String path) {
		paths.add(path);
	}

	protected List<String> getPaths() {
		return Collections.unmodifiableList(paths);
	}
}
