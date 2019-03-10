package io.crnk.gen.runtime;

/**
 * Responsible to provide a MetaLookup holding all the meta-data of an application.
 * Once it is available, RuntimeContext is called to trigger the code generation.
 * <p>
 * Usually to get hold onto a MetaLookup, the application needs to be started. Deltaspike
 * can be used for for JEE applications, an ApplicationContext for Spring applications.
 */
public interface RuntimeMetaResolver { // NOSONAR, not a functional interface

	/**
	 * @param trigger            to call once the MetaLookup is obtained to trigger the actual generation.
	 * @param runtimeClassLoader to use giving access to application classes.
	 */
	void run(GeneratorTrigger trigger, ClassLoader runtimeClassLoader);
}