package io.crnk.gen.typescript.internal;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.module.discovery.EmptyServiceDiscovery;
import io.crnk.gen.typescript.TSGeneratorExtension;
import io.crnk.gen.typescript.transform.TSMetaTransformationContext;
import io.crnk.gen.typescript.transform.TSMetaTransformationOptions;
import io.crnk.meta.MetaModule;
import io.crnk.meta.MetaModuleConfig;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import io.crnk.test.mock.models.Task;
import io.crnk.test.mock.models.types.ProjectData;
import org.gradle.api.Project;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

import java.io.File;

public class TSGeneratorTest {


	@Rule
	public TemporaryFolder testProjectDir = new TemporaryFolder();

	private TSGenerator generator;
	private MetaModule metaModule;
	private TSGeneratorExtension config;

	@Before
	public void setup() {
		Project project = Mockito.mock(Project.class);
		config = new TSGeneratorExtension(project, null);
		File outputDir = testProjectDir.getRoot();

		MetaModuleConfig metaConfig = new MetaModuleConfig();
		metaConfig.addMetaProvider(new ResourceMetaProvider());
		metaModule = MetaModule.createServerModule(metaConfig);

		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscovery(new EmptyServiceDiscovery());
		boot.addModule(metaModule);
		boot.boot();

		generator = new TSGenerator(outputDir, metaModule.getLookup(), config);
	}

	@Test
	public void checkMetaExcludedByDefault() {
		Project project = Mockito.mock(Project.class);
		config = new TSGeneratorExtension(project, null);
		Assert.assertTrue(config.getExcludes().contains("resources.meta"));
	}

	@Test(expected = UnsupportedOperationException.class)
	public void throwExceptionWhenMetaElementNotMappedToNpmPackage() {
		TSMetaTransformationContext transformationContext = generator.createMetaTransformationContext();
		MetaElement metaElement = Mockito.mock(MetaElement.class);
		metaElement.setId("does.not.exist");
		transformationContext.getNpmPackage(metaElement);
	}

	@Test(expected = UnsupportedOperationException.class)
	public void throwExceptionWhenMetaElementNotMappedToDirectory() {
		TSMetaTransformationContext transformationContext = generator.createMetaTransformationContext();
		MetaElement metaElement = Mockito.mock(MetaElement.class);
		metaElement.setId("does.not.exist");
		transformationContext.getDirectory(metaElement);
	}

	@Test(expected = IllegalStateException.class)
	public void throwExceptionWhenTransformingUnknownMetaElement() {
		MetaElement metaElement = Mockito.mock(MetaElement.class);
		metaElement.setId("does.not.exist");

		TSMetaTransformationOptions options = Mockito.mock(TSMetaTransformationOptions.class);
		generator.transform(metaElement, options);
	}


	@Test
	public void testResourcesMappedToRootDirectory() {
		MetaResource element = new MetaResource();
		element.setImplementationType(Task.class);
		element.setId("resources.task");

		TSMetaTransformationContext context = generator.createMetaTransformationContext();
		Assert.assertEquals("", context.getDirectory(element));
	}

	@Test
	public void testDataObjectsMappedToRootDirectoryByDefault() {
		MetaResource element = new MetaResource();
		element.setImplementationType(ProjectData.class);
		element.setId("sometehing.task");

		TSMetaTransformationContext context = generator.createMetaTransformationContext();
		Assert.assertEquals("", context.getDirectory(element));
		Assert.assertEquals("@packageNameNotSpecified", context.getNpmPackage(element));
	}

	@Test
	public void testDirectoryMapping() {
		MetaResource element = new MetaResource();
		element.setImplementationType(ProjectData.class);
		element.setId("a.b.task");

		config.getNpm().getDirectoryMapping().put("a", "x");

		TSMetaTransformationContext context = generator.createMetaTransformationContext();
		Assert.assertEquals("/x/b", context.getDirectory(element));
		Assert.assertEquals("@packageNameNotSpecified", context.getNpmPackage(element));
	}

	@Test
	public void testNestedDirectoryMapping() {
		MetaResource element = new MetaResource();
		element.setImplementationType(ProjectData.class);
		element.setId("a.b.task");

		config.getNpm().getDirectoryMapping().put("a", "x/y");

		TSMetaTransformationContext context = generator.createMetaTransformationContext();
		Assert.assertEquals("/x/y/b", context.getDirectory(element));
		Assert.assertEquals("@packageNameNotSpecified", context.getNpmPackage(element));
	}

	@Test
	public void testNestedDirectoryMappingIgnoresLeadingSlash() {
		MetaResource element = new MetaResource();
		element.setImplementationType(ProjectData.class);
		element.setId("a.b.task");

		config.getNpm().getDirectoryMapping().put("a", "/x/y");

		TSMetaTransformationContext context = generator.createMetaTransformationContext();
		Assert.assertEquals("/x/y/b", context.getDirectory(element));
		Assert.assertEquals("@packageNameNotSpecified", context.getNpmPackage(element));
	}

	@Test
	public void testNestedDirectoryMappingIgnoresTrailingSlash() {
		MetaResource element = new MetaResource();
		element.setImplementationType(ProjectData.class);
		element.setId("a.b.task");

		config.getNpm().getDirectoryMapping().put("a", "x/y/");

		TSMetaTransformationContext context = generator.createMetaTransformationContext();
		Assert.assertEquals("/x/y/b", context.getDirectory(element));
		Assert.assertEquals("@packageNameNotSpecified", context.getNpmPackage(element));
	}

	@Test
	public void testEmptyDirectoryMapping() {
		MetaResource element = new MetaResource();
		element.setImplementationType(ProjectData.class);
		element.setId("a.b.task");

		config.getNpm().getDirectoryMapping().put("a", "");

		TSMetaTransformationContext context = generator.createMetaTransformationContext();
		Assert.assertEquals("/b", context.getDirectory(element));
		Assert.assertEquals("@packageNameNotSpecified", context.getNpmPackage(element));
	}


	@Test
	public void testRootDirectoryMapping() {
		MetaResource element = new MetaResource();
		element.setImplementationType(ProjectData.class);
		element.setId("a.b.task");

		config.getNpm().getDirectoryMapping().put("a.b", "");

		TSMetaTransformationContext context = generator.createMetaTransformationContext();
		Assert.assertEquals("/", context.getDirectory(element));
		Assert.assertEquals("@packageNameNotSpecified", context.getNpmPackage(element));
	}

	@Test
	public void testRootDirectoryMappingIgnoresSlash() {
		MetaResource element = new MetaResource();
		element.setImplementationType(ProjectData.class);
		element.setId("a.b.task");

		config.getNpm().getDirectoryMapping().put("a.b", "/");

		TSMetaTransformationContext context = generator.createMetaTransformationContext();
		Assert.assertEquals("/", context.getDirectory(element));
		Assert.assertEquals("@packageNameNotSpecified", context.getNpmPackage(element));
	}
}
