package io.crnk.gen.typescript.internal;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.module.discovery.EmptyServiceDiscovery;
import io.crnk.gen.typescript.TSGeneratorConfiguration;
import io.crnk.gen.typescript.transform.TSMetaTransformationContext;
import io.crnk.gen.typescript.transform.TSMetaTransformationOptions;
import io.crnk.meta.MetaModule;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.provider.resource.ResourceMetaProvider;
import org.gradle.api.Project;
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

	@Before
	public void setup() {
		Project project = Mockito.mock(Project.class);
		TSGeneratorConfiguration config = new TSGeneratorConfiguration(project);
		File outputDir = testProjectDir.getRoot();

		MetaModule metaModule = MetaModule.create();
		metaModule.addMetaProvider(new ResourceMetaProvider());

		CrnkBoot boot = new CrnkBoot();
		boot.setServiceDiscovery(new EmptyServiceDiscovery());
		boot.addModule(metaModule);
		boot.boot();

		generator = new TSGenerator(outputDir, metaModule.getLookup(), config);
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


}
