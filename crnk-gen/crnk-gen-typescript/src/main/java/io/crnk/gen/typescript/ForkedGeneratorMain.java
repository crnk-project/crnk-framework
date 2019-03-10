package io.crnk.gen.typescript;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.crnk.gen.runtime.GeneratorTrigger;
import io.crnk.gen.typescript.internal.TSGeneratorRuntimeContext;
import io.crnk.gen.typescript.internal.TSGeneratorRuntimeContextImpl;

import java.io.File;

public class ForkedGeneratorMain {

	public static void main(String[] args) {
		try {
			File configFile = new File(args[0]);

			ObjectMapper mapper = new ObjectMapper();
			TSGeneratorConfig config = mapper.readerFor(TSGeneratorConfig.class).readValue(configFile);

			File outputDir = config.getGenDir();

			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			GeneratorTrigger context = new TSGeneratorRuntimeContextImpl();
			context.setClassLoader(classLoader);

			TSGeneratorRuntimeContext genContext = (TSGeneratorRuntimeContext) context;
			genContext.setOutputDir(outputDir);
			genContext.setConfig(config);

			RuntimeMetaResolver runtime = (RuntimeMetaResolver) Class.forName(config.computeMetaResolverClassName()).newInstance();
			runtime.run(context, classLoader);
			System.exit(0);
		} catch (Throwable e) {
			throw new IllegalStateException(e);
		}
	}
}
