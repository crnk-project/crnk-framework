package io.crnk.servlet.resource.repository;


import io.crnk.servlet.resource.model.Locale;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocaleRepository extends AbstractRepo<Locale, Long> {

	private static Map<Long, Locale> LOCALE_REPO = new ConcurrentHashMap<>();

	@Override
	protected Map<Long, Locale> getRepo() {
		return LOCALE_REPO;
	}
}
