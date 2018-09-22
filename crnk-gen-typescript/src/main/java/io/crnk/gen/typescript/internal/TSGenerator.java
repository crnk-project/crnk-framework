package io.crnk.gen.typescript.internal;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.crnk.core.engine.internal.utils.ExceptionUtil;
import io.crnk.core.engine.internal.utils.PreconditionUtil;
import io.crnk.gen.typescript.TSGeneratorConfig;
import io.crnk.gen.typescript.TSNpmConfiguration;
import io.crnk.gen.typescript.model.TSElement;
import io.crnk.gen.typescript.model.TSSource;
import io.crnk.gen.typescript.processor.TSSourceProcessor;
import io.crnk.gen.typescript.transform.TSMetaTransformation;
import io.crnk.gen.typescript.transform.TSMetaTransformationContext;
import io.crnk.gen.typescript.transform.TSMetaTransformationOptions;
import io.crnk.gen.typescript.writer.TSWriter;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.internal.resource.ResourceMetaParitition;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public class TSGenerator {

	private static final Logger LOGGER = LoggerFactory.getLogger(TSGenerator.class);

	private ResourceMetaParitition resourveMetaPartition;

	private File outputDir;

	private MetaLookup lookup;

	private TSGeneratorConfig config;

	private Map<MetaElement, TSElement> elementSourceMap = new HashMap<>();

	private List<TSSource> sources = new ArrayList<>();

	private ArrayList<TSMetaTransformation> transformations;

	private List<TSElement> transformedElements = new ArrayList<>();

	public TSGenerator(File outputDir, MetaLookup lookup, TSGeneratorConfig config) {
		this.outputDir = outputDir;
		this.lookup = lookup;
		this.config = config;

		this.resourveMetaPartition = lookup.getPartition(ResourceMetaParitition.class);

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

	public void run() throws IOException {
		if (config.getNpm().isPackagingEnabled()) {
			writePackaging();
			writeTypescriptConfig();
		}
		transformMetaToTypescript();
		runProcessors();
		writeSources();
	}

	private void writeTypescriptConfig() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);
		ObjectNode root = mapper.createObjectNode();
		ObjectNode compilerOptions = root.putObject("compilerOptions");
		compilerOptions.put("baseUrl", "");
		compilerOptions.put("declaration", true);
		compilerOptions.put("emitDecoratorMetadata", true);
		compilerOptions.put("experimentalDecorators", true);
		compilerOptions.put("module", "es6");
		compilerOptions.put("moduleResolution", "node");
		compilerOptions.put("sourceMap", true);
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


	protected void writePackaging() throws IOException {
		ObjectMapper mapper = new ObjectMapper();
		mapper.enable(SerializationFeature.INDENT_OUTPUT);

		ObjectNode packageJson = mapper.createObjectNode();

		TSNpmConfiguration npm = config.getNpm();

		packageJson.put("name", npm.getPackageName());
		packageJson.put("version", npm.getPackageVersion());
		packageJson.put("description", npm.getDescription());
		packageJson.put("license", npm.getLicense());
		if (npm.getGitRepository() != null) {
			ObjectNode repository = packageJson.putObject("repository");
			repository.put("type", "git");
			repository.put("url", npm.getGitRepository());
		}

		ObjectNode peerDependencies = packageJson.putObject("peerDependencies");
		ObjectNode devDependencies = packageJson.putObject("devDependencies");
		for (Map.Entry<String, String> entry : npm.getPeerDependencies().entrySet()) {
			peerDependencies.put(entry.getKey(), entry.getValue());
			devDependencies.put(entry.getKey(), entry.getValue());
		}
		for (Map.Entry<String, String> entry : npm.getDevDependencies().entrySet()) {
			devDependencies.put(entry.getKey(), entry.getValue());
		}

		File packageJsonFile = new File(outputDir, "package.json");
		packageJsonFile.getParentFile().mkdirs();

		ObjectNode scripts = packageJson.putObject("scripts");
		scripts.put("build", "npm run build:js");
		scripts.put("build:js", "tsc -p ./src --declaration");
		packageJson.put("name", npm.getPackageName());
		packageJson.put("version", npm.getPackageVersion());

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

	protected void writeSources() throws IOException {
		for (TSSource fileSource : sources) {
			TSWriter writer = new TSWriter(config.getCodeStyle());
			fileSource.accept(writer);

			File file = getFile(fileSource);
			String source = writer.toString();
			write(file, source);
		}
	}

	protected static void write(File file, String source) throws IOException {
		file.getParentFile().mkdirs();
		try (FileWriter writer = new FileWriter(file)) {
			writer.write(source);
		}
	}

	protected File getFile(TSSource sourceFile) {
		File srcOutputDir = outputDir;
		if (config.getNpm().isPackagingEnabled() && config.getSourceDirectoryName() != null) {
			srcOutputDir = new File(outputDir, config.getSourceDirectoryName());
		}
		File dir = new File(srcOutputDir, sourceFile.getDirectory());
		return new File(dir, sourceFile.getName() + ".ts");
	}

	private boolean postProcessing = false;

	public void transformMetaToTypescript() {
		Collection<MetaElement> elements = lookup.getMetaById().values();
		LOGGER.debug("transforming {} elements", elements.size());
		for (MetaElement element : elements) {
			boolean isRoot = isRoot(element);
			boolean isGenerated = isGenerated(element);
			if (isRoot && isGenerated) {
				LOGGER.debug("transforming {}", element.getId());
				transform(element, TSMetaTransformationOptions.EMPTY);
			} else {
				LOGGER.debug("ignoring {}, root={}, generated={}", element.getId(), isRoot, isGenerated);
			}
		}

		try {
			postProcessing = true;
			for (TSElement transformedElement : new ArrayList<>(transformedElements)) {
				for (TSMetaTransformation transformation : transformations) {
					transformation.postTransform(transformedElement, createMetaTransformationContext());
				}
			}
		} finally {
			postProcessing = false;
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


	protected TSElement transform(MetaElement element, TSMetaTransformationOptions options) {
		if (elementSourceMap.containsKey(element)) {
			return elementSourceMap.get(element);
		}
		if (postProcessing) {
			throw new IllegalStateException("cannot add further element while post processing: " + element.getId());
		}
		for (TSMetaTransformation transformation : transformations) {
			if (transformation.accepts(element)) {
				LOGGER.debug("transforming type {} of type {} with {}", element.getId(), element.getClass().getSimpleName(), transformation);
				TSElement tsElement = transformation.transform(element, createMetaTransformationContext(), options);
				transformedElements.add(tsElement);
				return tsElement;
			}
		}
		throw new IllegalStateException("unexpected element: " + element);
	}

	protected TSMetaTransformationContext createMetaTransformationContext() {
		return new TSMetaTransformationContextImpl();
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
			PreconditionUtil.verify(source != null, "no source provided");
			PreconditionUtil.verify(source.getNpmPackage() != null, "no package name specified for %s", source.getName());
			return source.getNpmPackage().equals(config.getNpm().getPackageName());
		}

		@Override
		public String getDirectory(MetaElement meta) {
			String idPath = meta.getId().substring(0, meta.getId().lastIndexOf('.'));
			if (idPath.startsWith(ResourceMetaProvider.DEFAULT_ID_PREFIX)) {
				return idPath.substring(Math.min(ResourceMetaProvider.DEFAULT_ID_PREFIX.length() + 1, idPath.length()));
			}
			String prefix = idPath;
			while (true) {
				String npmName = config.getNpm().getPackageMapping().get(prefix);
				if (npmName != null) {
					return idPath.substring(prefix.length()).replace('.', '/');
				}

				String directoryMapping = config.getNpm().getDirectoryMapping().get(prefix);
				if (directoryMapping != null) {
					String suffix = idPath.substring(prefix.length()).replace('.', '/');
					String directory = normalize(directoryMapping) + "/" + normalize(suffix);
					return directory.startsWith("/") ? directory : "/" + directory;
				}

				int sep = prefix.lastIndexOf('.');
				if (sep == -1) {
					// return to root directory by default
					return "";
				}
				prefix = prefix.substring(0, sep);
			}
		}

		private String normalize(String directoryMapping) {
			if (directoryMapping.endsWith("/")) {
				directoryMapping = directoryMapping.substring(0, directoryMapping.length() - 1);
			}
			if (directoryMapping.startsWith("/")) {
				return directoryMapping.substring(1);
			}
			return directoryMapping;
		}

		@Override
		public String getNpmPackage(MetaElement meta) {
			String idPath = meta.getId().substring(0, meta.getId().lastIndexOf('.'));
			if (idPath.startsWith(ResourceMetaProvider.DEFAULT_ID_PREFIX)) {
				return config.getNpm().getPackageName();
			}
			String prefix = idPath;
			while (true) {
				String npmName = config.getNpm().getPackageMapping().get(prefix);
				if (npmName != null) {
					return npmName;
				}
				int sep = prefix.lastIndexOf('.');
				if (sep == -1) {
					// by default add to local package
					return config.getNpm().getPackageName();
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
			return resourveMetaPartition.getMeta(implClass);
		}

		@Override
		public MetaElement getMeta(String metaId) {
			return lookup.getMetaById().get(metaId);
		}
	}


}
