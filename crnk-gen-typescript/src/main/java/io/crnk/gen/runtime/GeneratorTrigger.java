package io.crnk.gen.runtime;

public interface GeneratorTrigger {

	/**
	 * Triggers the generation of the provided meta-data. To be invoked by RuntimeIntegration once ready.
	 *
	 * @param lookup of type io.katharsis.meta.MetaLookup. Not that the MetaLookup class is not available here.
	 */
	void generate(Object lookup);
}