package io.crnk.servlet;

import io.crnk.core.boot.CrnkBoot;

// tag::docs[]
public class SampleCrnkFilter extends CrnkFilter {

	@Override
	protected void initCrnk(CrnkBoot boot) {
		// do your configuration here
	}
}
// end::docs[]