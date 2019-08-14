package io.crnk.gen.openapi;

import io.crnk.gen.base.GeneratorModule;
import io.crnk.gen.base.GeneratorModuleConfigBase;
import io.crnk.meta.MetaLookup;
import io.crnk.meta.model.resource.MetaResource;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.integration.GenericOpenApiContext;
import io.swagger.v3.oas.integration.OpenApiConfigurationException;
import io.swagger.v3.oas.integration.SwaggerConfiguration;
import io.swagger.v3.oas.integration.api.OpenAPIConfiguration;
import io.swagger.v3.oas.integration.api.OpenApiContext;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
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
		MetaLookup metaLookup = (MetaLookup) meta;
		LOGGER.info(metaLookup.toString());
		List<MetaResource> resources = getResources(metaLookup);
		LOGGER.info(resources.toString());

		for (MetaResource resource : resources) {
			if (resource.getSuperType() == null) {
				LOGGER.info(resource.getResourceType() + " : /" + resource.getResourcePath());

				// write subtypes as nested entries
				List<MetaResource> subTypes = resource.getSubTypes().stream()
						.filter(it -> it instanceof MetaResource)
						.map(it -> (MetaResource) it)
						.sorted(Comparator.comparing(MetaResource::getResourceType)).collect(Collectors.toList());
				for (MetaResource subType : subTypes) {
					LOGGER.info("\t" + subType.getResourceType());
				}
			}

//			builder.appendCell(builder.getDescription(resource));
		}

		OpenAPIConfiguration config = new SwaggerConfiguration()
				.openAPI(new OpenAPI()
						.info(new Info()
								.description("TEST INFO DESC")
								.title("TEST INFO TITLE")
								.version("0.1.0")
						));

		try {
			OpenApiContext ctx = new GenericOpenApiContext()
					.openApiConfiguration(config)
					.init();
			OpenAPI openApi = ctx.read();

			write("openapi", Yaml.pretty(openApi));
		} catch (OpenApiConfigurationException ignore) {
		}
	}

	private List<MetaResource> getResources(MetaLookup metaLookup) {
		LOGGER.debug("find resources");
		return metaLookup.findElements(MetaResource.class).stream()
				.sorted(Comparator.comparing(MetaResource::getResourceType))
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