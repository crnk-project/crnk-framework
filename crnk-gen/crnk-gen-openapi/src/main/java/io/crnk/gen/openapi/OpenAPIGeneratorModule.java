package io.crnk.gen.openapi;

import io.crnk.gen.base.GeneratorModule;
import io.crnk.gen.base.GeneratorModuleConfigBase;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.*;
import io.crnk.meta.model.resource.MetaJsonObject;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceBase;
import io.crnk.meta.model.resource.MetaResourceField;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.integration.GenericOpenApiContext;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;
import io.swagger.v3.oas.integration.api.OpenApiContext;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;


public class OpenAPIGeneratorModule implements GeneratorModule {

	private static final Logger LOGGER = LoggerFactory.getLogger(OpenAPIGeneratorModule.class);
	public static final String NAME = "openapi";
	private OpenAPIGeneratorConfig config = new OpenAPIGeneratorConfig();
	private ClassLoader classloader;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public void generate(Object meta) throws IOException {
		LOGGER.info("performing openapi generation");
		OpenAPI openApi = new OpenAPI()
				.info(new Info()
						.description("TEST INFO DESC")
						.title("TEST INFO TITLE")
						.version("0.1.0")
				)
				.paths(new Paths());

		MetaLookup metaLookup = (MetaLookup) meta;
		List<MetaElement> elements = getElements(metaLookup);
		for (MetaElement element : elements) {
			if (element instanceof MetaResource) {
				MetaResource metaResource = (MetaResource) element;
				if (metaResource.toString().contains("[name=Meta")) {
					continue;
				}

				PathItem pathItem = new PathItem();
				pathItem.setDescription(metaResource.toString());

				if (metaResource.isInsertable()) {
					Operation operation = new Operation();
					operation.setDescription("Create a " + metaResource.getResourceType());
					pathItem.setPost(operation);
					openApi.getPaths().addPathItem(metaResource.getResourcePath(), pathItem);
				}
				if (metaResource.isReadable()) {
					Operation operation = new Operation();
					operation.setDescription("Read a " + metaResource.getResourceType());
					pathItem.setGet(operation);
					openApi.getPaths().addPathItem(metaResource.getResourcePath(), pathItem);
				}
				if (metaResource.isUpdatable()) {
					Operation operation = new Operation();
					operation.setDescription("Update a " + metaResource.getResourceType());
					pathItem.setPatch(operation);
					openApi.getPaths().addPathItem(metaResource.getResourcePath(), pathItem);
				}
				if (metaResource.isDeletable()) {
					Operation operation = new Operation();
					operation.setDescription("Delete a " + metaResource.getResourceType());
					pathItem.setDelete(operation);
					openApi.getPaths().addPathItem(metaResource.getResourcePath(), pathItem);
				}
			}
		}

		write("openapi", Yaml.pretty(openApi));
	}

	private String dumpResource(MetaResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append(resource.toString() + "\n");
		sb.append(resource.getId() + "\n");
		sb.append(resource.getResourceType() + "\n");
		sb.append(resource.getAttributes().toString() + "\n");
//		sb.append(resource.getAttributes().toString() + "\n");

		return sb.toString();
	}

	private List<MetaResource> getResources(MetaLookup metaLookup) {
		LOGGER.debug("find resources");
		return metaLookup.findElements(MetaResource.class).stream()
				.sorted(Comparator.comparing(MetaResource::getResourceType))
				.collect(Collectors.toList());
    }

	private List<MetaElement> getElements(MetaLookup metaLookup) {
		LOGGER.debug("find elements");
		return metaLookup.findElements(MetaElement.class).stream()
				.sorted(Comparator.comparing(MetaElement::getName))
				.collect(Collectors.toList());
    }

	@Override
	public ClassLoader getClassLoader() {
		return classloader;
	}

	@Override
	public void setClassLoader(ClassLoader classloader) {
		this.classloader = classloader;
	}

	@Override
	public void initDefaults(File buildDir) {
		config.setBuildDir(buildDir);
	}

	@Override
	public File getGenDir() {
		return config.getGenDir();
	}

	@Override
	public Collection<Class> getConfigClasses() {
		return Arrays.asList(OpenAPIGeneratorConfig.class);
	}

	public OpenAPIGeneratorConfig getConfig() {
		return config;
	}

	@Override
	public void setConfig(GeneratorModuleConfigBase config) {
		this.config = (OpenAPIGeneratorConfig) config;
	}


	private File write(String fileName, String source) throws IOException {
		File file = new File(config.getGenDir(), fileName + ".yaml");
		file.getParentFile().mkdirs();
		try (FileWriter writer = new FileWriter(file)) {
			writer.write(source);
		}
		return file;
	}


}