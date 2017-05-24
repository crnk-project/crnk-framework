package io.crnk.gen.typescript;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.crnk.gen.typescript.model.TSElement;
import io.crnk.gen.typescript.model.TSSource;
import io.crnk.gen.typescript.processor.TSSourceProcessor;
import io.crnk.gen.typescript.transform.TSMetaTransformation;
import io.crnk.gen.typescript.transform.TSMetaTransformationContext;
import io.crnk.gen.typescript.writer.TSWriter;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.resource.MetaResource;

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
		for (String className : config.getMetaTransformationClassNames()) {
			try {
				transformations.add((TSMetaTransformation) getClass().getClassLoader().loadClass(className).newInstance());
			}
			catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
				throw new IllegalArgumentException("failed to load transformation implementation " + className, e);
			}
		}
	}

	public void run() {
		build();
		transform();
		write();
	}

	private void write() {
		if (config.getNpmPackageName() == null) {
			throw new IllegalStateException("no typescriptGen.npmPackageName configured");
		}

		for (TSSource fileSource : sources) {
			TSWriter writer = new TSWriter(config.getCodeStyle());
			fileSource.accept(writer);

			File file = getFile(fileSource);
			String source = writer.toString();
			write(file, source);
		}
	}

	private static void write(File file, String source) {
		file.getParentFile().mkdirs();
		try (FileWriter writer = new FileWriter(file)) {
			writer.write(source);
		}
		catch (IOException e) {
			throw new IllegalStateException("failed to write " + file.getAbsolutePath(), e);
		}
	}

	private File getFile(TSSource sourceFile) {
		File dir = new File(outputDir, sourceFile.getDirectory());
		return new File(dir, sourceFile.getName() + ".ts");
	}

	public void build() {
		Collection<MetaElement> elements = lookup.getMetaById().values();
		for (MetaElement element : elements) {
			if (isRoot(element)) {
				transform(element);
			}
		}
	}

	public void transform() {
		for (TSSourceProcessor processor : config.getSourceProcessors()) {
			sources = processor.process(sources);
		}
	}

	private TSElement transform(MetaElement element) {
		if (elementSourceMap.containsKey(element)) {
			return elementSourceMap.get(element);
		}
		for (TSMetaTransformation transformation : transformations) {
			if (transformation.accepts(element)) {
				return transformation.transform(element, new TSMetaTransformationContextImpl());
			}
		}
		throw new IllegalStateException("unexpected element: " + element);
	}

	private static boolean isRoot(MetaElement element) {
		return element instanceof MetaResource;
	}

	class TSMetaTransformationContextImpl implements TSMetaTransformationContext {

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
		public TSElement transform(MetaElement metaElement) {
			return TSGenerator.this.transform(metaElement);
		}

		@Override
		public MetaElement getMeta(Class<?> implClass) {
			return lookup.getMeta(implClass);
		}
	}

}
