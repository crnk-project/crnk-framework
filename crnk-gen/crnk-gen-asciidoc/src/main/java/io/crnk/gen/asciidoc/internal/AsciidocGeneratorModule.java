package io.crnk.gen.asciidoc.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.crnk.gen.asciidoc.AsciidocGeneratorConfig;
import io.crnk.gen.base.GeneratorModule;
import io.crnk.gen.base.GeneratorModuleConfigBase;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaEnumType;
import io.crnk.meta.model.MetaType;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class AsciidocGeneratorModule implements GeneratorModule {

	private static final Logger LOGGER = LoggerFactory.getLogger(AsciidocGeneratorModule.class);

	public static final String NAME = "asciidoc";

	public static final String ATTRIBUTES_FILE = "attributes.adoc";

	public static final String LITERALS_FILE = "literals.adoc";

	public static final String DESCRIPTION_FILE = "description.adoc";

	public static final String INDEX_FILE = "index.adoc";

	public static final String RELATIONSHIP_FILE = "relationships.adoc";

	public static final String EXAMPLES_FILE = "examples.adoc";


	private AsciidocGeneratorConfig config = new AsciidocGeneratorConfig();

	private ClassLoader classloader;

	private JavaDocModel javaDoc;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void generate(Object meta) throws IOException {
		LOGGER.debug("performing asciidoc generation");
		MetaLookup metaLookup = (MetaLookup) meta;
		List<MetaResource> resources = getResources(metaLookup);

		File resourcesFile = writeResources(resources);
		File typesFile = writeTypes(resources);
		writeIndex(resourcesFile, typesFile);
	}

	private File writeTypes(List<MetaResource> resources) throws IOException {
		LOGGER.debug("writeTypes");

		Set<MetaType> typeSet = new HashSet<>();
		for (MetaResource resource : resources) {
			for (MetaResourceField attribute : resource.getDeclaredAttributes()) {
				if (!attribute.isAssociation() && !attribute.isLinks() && !attribute.isMeta()) {
					MetaType elementType = attribute.getType().getElementType();
					collectTypes(typeSet, elementType);
				}
			}
		}
		LOGGER.debug("collected {} types", typeSet);

		List<MetaType> types = typeSet.stream().sorted(Comparator.comparing(MetaType::getName)).collect(Collectors.toList());
		if (types.isEmpty()) {
			return null;
		}

		AsciidocBuilder indexBuilder = newBuilder(Collections.emptyList());
		writeTypeListing(types);

		indexBuilder.startSection("Types");
		indexBuilder.writeInclude("type_listing");

		for (MetaType type : types) {
			String name = type.getName();
			indexBuilder.writeInclude(name + "/index");

			File outputDir = getOutputDir(type);

			List<String> resourceAnchor = Arrays.asList("types", AsciidocUtils.toAnchor(name));

			if (type instanceof MetaDataObject) {
				AsciidocBuilder attrBuilder = newBuilder(resourceAnchor);
				attrBuilder.appendFields(type.asDataObject());
				attrBuilder.write(new File(outputDir, ATTRIBUTES_FILE));
			}
			else {
				AsciidocBuilder literalsBuilder = newBuilder(resourceAnchor);
				literalsBuilder.appendLiterals((MetaEnumType) type);
				literalsBuilder.write(new File(outputDir, LITERALS_FILE));
			}

			AsciidocBuilder descriptionBuilder = newBuilder(resourceAnchor);
			descriptionBuilder.write(new File(outputDir, DESCRIPTION_FILE));

			AsciidocBuilder resourceIndexBuilder = newBuilder(Arrays.asList("types"));
			resourceIndexBuilder.appendOverview(type, outputDir);
			resourceIndexBuilder.write(new File(outputDir, INDEX_FILE));
		}
		indexBuilder.endSection();
		return write("types", indexBuilder.toString());
	}

	private void collectTypes(Set<MetaType> types, MetaType type) {
		if (!types.contains(type) && !(type instanceof MetaResource)) {
			if (type instanceof MetaDataObject) {
				types.add(type);
				MetaDataObject dataType = type.asDataObject();
				for (MetaAttribute attr : dataType.getAttributes()) {
					collectTypes(types, attr.getType().getElementType());
				}
			}
			else if (type instanceof MetaEnumType) {
				types.add(type);
			}
		}
	}

	private void writeIndex(File resourcesFile, File typesFile) throws IOException {
		AsciidocBuilder indexBuilder = newBuilder(Collections.emptyList());

		indexBuilder.appendLine(":sectnums:");
		indexBuilder.appendLine(":toc: left");
		indexBuilder.appendLine(":toclevels: 3");
		indexBuilder.appendLine("# " + config.getTitle());
		indexBuilder.appendLine(":leveloffset: 1");

		if (resourcesFile != null && resourcesFile.exists()) {
			indexBuilder.writeInclude(resourcesFile.getName());
		}

		if (typesFile != null && typesFile.exists()) {
			indexBuilder.writeInclude(typesFile.getName());
		}

		write("index", indexBuilder.toString());
	}

	private File writeResources(List<MetaResource> resources) throws IOException {
		LOGGER.debug("write resources");
		writeResourceListing(resources);

		AsciidocBuilder indexBuilder = newBuilder(Collections.emptyList());
		indexBuilder.startSection("Resources");

		if(config.isGraphEnabled()) {
			writeGraph(resources);
			indexBuilder.writeInclude("graph");
		}

		indexBuilder.writeInclude("resource_listing");

		for (MetaResource resource : resources) {
			if (resource.getSuperType() == null) {
				writeResource(indexBuilder, resource, 0);
			}
		}
		indexBuilder.endSection();
		return write("resources", indexBuilder.toString());
	}


	private void writeResource(AsciidocBuilder indexBuilder, MetaResource resource, int depth) throws IOException {
		String resourceType = resource.getResourceType();
		indexBuilder.writeInclude(resourceType + "/index");

		File outputDir = getOutputDir(resource);

		LOGGER.debug("write resource {}", resourceType);

		List<String> resourceAnchor = Arrays.asList("resources", AsciidocUtils.toAnchor(resourceType));

		AsciidocBuilder attrBuilder = newBuilder(resourceAnchor, depth);
		attrBuilder.appendFields(resource, false);
		attrBuilder.write(new File(outputDir, ATTRIBUTES_FILE));

		AsciidocBuilder relBuilder = newBuilder(resourceAnchor, depth);
		relBuilder.appendFields(resource, true);
		relBuilder.write(new File(outputDir, RELATIONSHIP_FILE));

		AsciidocBuilder descriptionBuilder = newBuilder(resourceAnchor, depth);
		descriptionBuilder.appendDescription(resource);
		descriptionBuilder.write(new File(outputDir, DESCRIPTION_FILE));

		List<String> exampleAnchor = new ArrayList<>(resourceAnchor);
		exampleAnchor.add("examples");
		AsciidocBuilder exampleBuilder = newBuilder(exampleAnchor);
		exampleBuilder.appendExamples(outputDir);
		exampleBuilder.write(new File(outputDir, EXAMPLES_FILE));

		AsciidocBuilder resourceIndexBuilder = newBuilder(Arrays.asList("resources"), depth);
		resourceIndexBuilder.appendOverview(resource, outputDir);
		resourceIndexBuilder.write(new File(outputDir, INDEX_FILE));

		// write subtypes as nexted entries
		List<MetaResource> subTypes = resource.getSubTypes().stream()
				.filter(it -> it instanceof MetaResource)
				.map(it -> (MetaResource) it)
				.sorted(Comparator.comparing(MetaResource::getResourceType)).collect(Collectors.toList());
		for (MetaResource subType : subTypes) {
			writeResource(indexBuilder, subType, depth + 1);
		}
	}

	private List<MetaResource> getResources(MetaLookup metaLookup) {
		LOGGER.debug("find resources");
		return metaLookup.findElements(MetaResource.class).stream()
				.sorted(Comparator.comparing(MetaResource::getResourceType))
				.filter(it -> isIncluded(it)).collect(Collectors.toList());
	}

	private File getOutputDir(MetaResource resource) {
		File file = new File(config.getGenDir(), resource.getResourceType());
		file.mkdirs();
		return file;
	}

	private File getOutputDir(MetaType type) {
		File file = new File(config.getGenDir(), type.getName());
		file.mkdirs();
		return file;
	}

	private JavaDocModel getJavaDoc() throws IOException {
		if (javaDoc == null) {

			Set<File> docletPaths = config.getDocletPaths();
			docletPaths.add(new File(config.getBuildDir(), "crnk-xml-docs/javadoc.xml"));

			javaDoc = new JavaDocModel();
			for (File docletFile : docletPaths) {
				if (docletFile.isDirectory()) {
					docletFile = new File(docletFile, "build/crnk-xml-docs/javadoc.xml");
				}
				if (docletFile.exists()) {
					try (FileInputStream in = new FileInputStream(docletFile)) {
						javaDoc.loadFile(in);
					}
				}
			}
		}
		return javaDoc;
	}

	private void writeGraph(List<MetaResource> resources) throws IOException {
		LOGGER.debug("write graph");
		String fileName = "images/graph.svg";
		File graphFile = new File(config.getGenDir(), fileName);
		GraphBuilder graphBuilder = new GraphBuilder();
		graphBuilder.generate(resources, graphFile);

		AsciidocBuilder builder = newBuilder(Arrays.asList("resources"));
		builder.appendLine("image:" + fileName + "[width=90%, pdfwidth=90%, align=center]");
		builder.write(new File(config.getGenDir(), "graph.adoc"));
	}

	private AsciidocBuilder newBuilder(List<String> anchors) throws IOException {
		return newBuilder(anchors, 0);
	}

	private AsciidocBuilder newBuilder(List<String> anchors, int depth) throws IOException {
		AsciidocBuilder builder = new AsciidocBuilder(getJavaDoc(), config.getBaseDepth() + depth + anchors.size());
		builder.getAnchors().addAll(anchors);
		return builder;
	}

	private void writeResourceListing(List<MetaResource> resources) throws IOException {
		LOGGER.debug("write resource listing");
		AsciidocBuilder builder = newBuilder(Arrays.asList("resources"));
		builder.startTable("1,5", "header");
		builder.appendCell("Resource");
		builder.appendCell("Description");
		for (MetaResource resource : resources) {
			String anchor = AsciidocUtils.getAnchor(resource);
			builder.startCell();
			builder.appendAnchor(anchor, resource.getResourceType());
			builder.endCell();
			builder.appendCell(builder.getDescription(resource));
		}
		builder.endTable();
		builder.write(new File(config.getGenDir(), "resource_listing.adoc"));
	}

	private void writeTypeListing(List<MetaType> types) throws IOException {
		AsciidocBuilder builder = newBuilder(Arrays.asList("types"));
		builder.startTable("1,5", "header");
		builder.appendCell("Type");
		builder.appendCell("Description");
		for (MetaType type : types) {
			String anchor = AsciidocUtils.getAnchor(type);
			builder.startCell();
			builder.appendAnchor(anchor, type.getName());
			builder.endCell();
			builder.appendCell(builder.getDescription(type));
		}
		builder.endTable();
		builder.write(new File(config.getGenDir(), "type_listing.adoc"));
	}

	private boolean isIncluded(MetaResource resource) {
		return !config.getExcludes().stream()
				.filter(it -> resource.getImplementationClass().getName().startsWith(it) || resource.getResourceType().startsWith(it))
				.findFirst().isPresent();
	}

	private File write(String fileName, String source) throws IOException {
		File file = new File(config.getGenDir(), fileName + ".adoc");
		write(file, source);
		return file;
	}

	protected static void write(File file, String source) throws IOException {
		file.getParentFile().mkdirs();
		try (FileWriter writer = new FileWriter(file)) {
			writer.write(source);
		}
	}


	@Override
	public ClassLoader getClassLoader() {
		return classloader;
	}

	@Override
	public void setClassLoader(ClassLoader classloader) {
		this.classloader = classloader;
	}

	public AsciidocGeneratorConfig getConfig() {
		return config;
	}

	@Override
	public void initDefaults(File buildDir) {
		config.setBuildDir(buildDir);
		config.getExcludes().add("meta/");
	}

	@Override
	public File getGenDir() {
		return config.getGenDir();
	}

	@Override
	public Collection<Class> getConfigClasses() {
		return Arrays.asList(AsciidocGeneratorConfig.class);
	}

	@Override
	public void setConfig(GeneratorModuleConfigBase config) {
		this.config = (AsciidocGeneratorConfig) config;
	}


}