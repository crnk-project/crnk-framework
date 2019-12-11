package io.crnk.test.mock;

import io.crnk.core.engine.information.NamingStrategy;

public class TestNamingStrategy implements NamingStrategy {

	@Override
	public String adaptPath(String path) {
		if ("taskOldPath".equals(path)) {
			return "taskNewPath";
		}
		return path;
	}
}
