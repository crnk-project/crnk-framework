package io.crnk.gen.typescript.internal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.engine.internal.utils.ExceptionUtil;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.gen.typescript.TSGeneratorConfiguration;
import io.crnk.gen.typescript.model.TSElement;
import io.crnk.gen.typescript.model.TSSource;
import io.crnk.gen.typescript.processor.TSSourceProcessor;
import io.crnk.gen.typescript.transform.TSMetaTransformation;
import io.crnk.gen.typescript.transform.TSMetaTransformationContext;
import io.crnk.gen.typescript.transform.TSMetaTransformationOptions;
import io.crnk.gen.typescript.writer.TSWriter;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaElement;

public class TSGenerator {

	private File outputDir;

	private MetaLookup lookup;

	private TSGeneratorConfiguration config;

	private Map<MetaElement, TSElement> elementSourceMap = new HashMap<>();

	private Set<TSSource> sources = new HashSet<>();

	private ArrayList<TSMetaTransformation> transformations;

	public TSGenerator(File outputDir, MetaLookup lookup, TSGeneratorConfiguration config) {
		this.outputDir = outputDir;
		this.lookup = lookup;
		this.config = config;

		transformations = new ArrayList<>();
		for (final String className : config.getMetaTransformationClassNames()) {
			ExceptionUtil.wrapCatchedExceptions(new Callable<Object>() {
				@Override
				public Object call() throws Exception {
					transformations.add((TSMetaTransformation) getClass().getClassLoader().loadClass(className).newInstance());
					return null;
				}
			}, "failed to load transformation implementation {}", className);
		}
	}

	public void run() {
		writePackaging();
		writeTypescriptConfig();
		transformMetaToTypescript();
		runProcessors();
		writeSources();
	}

	private void writeTypescriptConfig() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT);
			ObjectNode root = mapper.createObjectNode();
			ObjectNode compilerOptions = root.putObject("compilerOptions");
			compilerOptions.put("baseUrl", "");
			compilerOptions.put("declaration", false);
			compilerOptions.put("emitDecoratorMetadata", true);
			compilerOptions.put("experimentalDecorators", true);
			compilerOptions.put("module", "es6");
			compilerOptions.put("moduleResolution", "node");
			compilerOptions.put("outDir", "../../../../npm/");
			compilerOptions.put("sourceMap", true);
			compilerOptions.put("inlineSources", true);
			compilerOptions.put("target", "es5");
			ArrayNode typeArrays = compilerOptions.putArray("typeRoots");
			typeArrays.add("node_modules/@types");
			ArrayNode libs = compilerOptions.putArray("lib");
			libs.add("es6");
			libs.add("dom");

			File outputSourceDir = new File(outputDir, config.getSourceDirectoryName());
			File file = new File(outputSourceDir, "tsconfig.json");
			file.getParentFile().mkdirs();
			mapper.writer().writeValue(file, root);
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}


	protected void writePackaging() {
		try {
			ObjectMapper mapper = new ObjectMapper();
			mapper.enable(SerializationFeature.INDENT_OUTPUT);

			ObjectNode packageJson = mapper.createObjectNode();

			packageJson.put("name", config.getNpmPackageName());
			packageJson.put("version", config.getNpmPackageVersion());
			packageJson.put("description", config.getNpmDescription());
			packageJson.put("license", config.getNpmLicense());
			if (config.getGitRepository() != null) {
				ObjectNode repository = packageJson.putObject("repository");
				repository.put("type", "git");
				repository.put("url", config.getGitRepository());
			}


			ObjectNode peerDependencies = packageJson.putObject("peerDependencies");
			ObjectNode devDependencies = packageJson.putObject("devDependencies");
			for (Map.Entry<String, String> entry : config.getPeerNpmDependencies().entrySet()) {
				peerDependencies.put(entry.getKey(), entry.getValue());
				devDependencies.put(entry.getKey(), entry.getValue());
			}
			for (Map.Entry<String, String> entry : config.getNpmDevDependencies().entrySet()) {
				devDependencies.put(entry.getKey(), entry.getValue());
			}

			File packageJsonFile = new File(outputDir, "package.json");
			packageJsonFile.getParentFile().mkdirs();

			ObjectNode scripts = packageJson.putObject("scripts");
			scripts.put("build", "npm run build:js && npm run copyPackageJson && rimraf ../../../npm/node_modules");
			scripts.put("copyPackageJson", "ncp package.json ../../../npm/package.json");
			scripts.put("build:js", "tsc -p ./src --declaration");
			packageJson.put("name", config.getNpmPackageName());
			packageJson.put("version", config.getNpmPackageVersion());

			// match what is expected by NPM. Otherwise NPM will reformat it and up-to-date checking will fail
			// the first time (also platform specific)
			String text = mapper.writer().writeValueAsString(packageJson);
			text = text.replace(" : ", ": ");
			text = text + "\n";
			text = text.replace(System.lineSeparator(), "\n");

			try (FileWriter writer = new FileWriter(packageJsonFile)) {
				writer.write(text);
			}
		}
		catch (IOException e) {
			throw new IllegalStateException(e);
		}

	}

	protected void writeSources() {
		PreconditionUtil.assertNotNull("no typescriptGen.npmPackageName configured", config.getNpmPackageName());

		for (TSSource fileSource : sources) {
			TSWriter writer = new TSWriter(config.getCodeStyle());
			fileSource.accept(writer);

			File file = getFile(fileSource);
			String source = writer.toString();
			write(file, source);
		}
	}

	protected static void write(File file, String source) {
		file.getParentFile().mkdirs();
		try (FileWriter writer = new FileWriter(file)) {
			writer.write(source);
		}
		catch (IOException e) {
			throw new IllegalStateException("failed to write " + file.getAbsolutePath(), e);
		}
	}

	protected File getFile(TSSource sourceFile) {
		File srcOutputDir = outputDir;
		if (config.getSourceDirectoryName() != null) {
			srcOutputDir = new File(outputDir, config.getSourceDirectoryName());
		}
		File dir = new File(srcOutputDir, sourceFile.getDirectory());
		return new File(dir, sourceFile.getName() + ".ts");
	}

	public void transformMetaToTypescript() {
		Collection<MetaElement> elements = lookup.getMetaById().values();
		for (MetaElement element : elements) {
			if (isRoot(element) && isGenerated(element)) {
				transform(element, TSMetaTransformationOptions.EMPTY);
			}
		}
	}

	private boolean isGenerated(MetaElement element) {
		Set<String> includes = config.getIncludes();
		Set<String> excludes = config.getExcludes();
		return (includes.isEmpty() || matches(element.getId(), includes))
				&& !matches(element.getId(), excludes);
	}

	private boolean matches(String id, Set<String> set) {
		for (String element : set) {
			if (id.startsWith(element)) {
				return true;
			}
		}
		return false;
	}


	public void runProcessors() {
		for (TSSourceProcessor processor : config.getSourceProcessors()) {
			sources = processor.process(sources);
		}
	}

	private TSElement transform(MetaElement element, TSMetaTransformationOptions options) {
		if (elementSourceMap.containsKey(element)) {
			return elementSourceMap.get(element);
		}
		for (TSMetaTransformation transformation : transformations) {
			if (transformation.accepts(element)) {
				return transformation.transform(element, new TSMetaTransformationContextImpl(), options);
			}
		}
		throw new IllegalStateException("unexpected element: " + element);
	}

	private boolean isRoot(MetaElement element) {
		for (TSMetaTransformation transformation : transformations) {
			if (transformation.accepts(element)) {
				return transformation.isRoot(element);
			}
		}
		return false;
	}

	protected class TSMetaTransformationContextImpl implements TSMetaTransformationContext {

		private boolean isGenerated(TSSource source) {
			return source.getNpmPackage().equals(config.getNpmPackageName());
		}

		@Override
		public String getDirectory(MetaElement meta) {
			String idPath = meta.getId().substring(0, meta.getId().lastIndexOf('.'));
			String prefix = idPath;
			while (true) {
				String npmName = config.getNpmPackageMapping().get(prefix);
				if (npmName != null) {
					return idPath.substring(prefix.length()).replace('.', '/');
				}
				int sep = prefix.lastIndexOf('.');
				if (sep == -1) {
					throw new IllegalStateException("failed to determine NPM package name for " + meta.getId()
							+ ", configure plugin accordingly with typescriptGen.npmPackageMapping");
				}
				prefix = prefix.substring(0, sep);
			}
		}

		@Override
		public String getNpmPackage(MetaElement meta) {
			String idPath = meta.getId().substring(0, meta.getId().lastIndexOf('.'));
			String prefix = idPath;
			while (true) {
				String npmName = config.getNpmPackageMapping().get(prefix);
				if (npmName != null) {
					return npmName;
				}
				int sep = prefix.lastIndexOf('.');
				if (sep == -1) {
					throw new IllegalStateException("failed to determine NPM package name for " + meta.getId() + " of type "
							+ meta.getClass().getSimpleName()
							+ ", configure plugin accordingly with typescriptGen.npmPackageMapping");
				}
				prefix = prefix.substring(0, sep);
			}
		}

		@Override
		public void addSource(TSSource source) {
			if (isGenerated(source)) {
				sources.add(source);
			}
		}

		@Override
		public void putMapping(MetaElement metaElement, TSElement tsElement) {
			elementSourceMap.put(metaElement, tsElement);
		}

		@Override
		public TSElement transform(MetaElement metaElement, TSMetaTransformationOptions options) {
			return TSGenerator.this.transform(metaElement, options);
		}

		@Override
		public MetaElement getMeta(Class<?> implClass) {
			return lookup.getMeta(implClass);
		}
	}

}
