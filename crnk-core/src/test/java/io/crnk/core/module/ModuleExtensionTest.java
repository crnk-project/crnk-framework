package io.crnk.core.module;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.crnk.core.engine.internal.CoreModule;
import io.crnk.core.engine.internal.jackson.JacksonModule;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mockito;

import java.util.List;

public class ModuleExtensionTest {

	private boolean optional;

	private ModuleRegistry moduleRegistry;

	private ModuleExtensionTest.ExtensionConsumerModule consumerModule;

	private ModuleExtensionTest.ExtensionProviderModule providerModule;

	private ObjectMapper objectMapper = new ObjectMapper();

	@Before
	public void setup() {
		optional = false;

		moduleRegistry = new ModuleRegistry();
		moduleRegistry.addModule(new CoreModule());
		moduleRegistry.addModule(new JacksonModule(objectMapper));

		consumerModule = Mockito.spy(new ExtensionConsumerModule());
		providerModule = Mockito.spy(new ExtensionProviderModule());
	}


	@Test
	public void checkInorderInitializationWithInOrderRegistration() {
		moduleRegistry.addModule(providerModule);
		moduleRegistry.addModule(consumerModule);
		moduleRegistry.init(objectMapper, null, null);
		heckInorderInitialization();
	}

	@Test
	public void checkInorderInitializationWithReverseRegistration() {
		moduleRegistry.addModule(consumerModule);
		moduleRegistry.addModule(providerModule);
		moduleRegistry.init(objectMapper, null, null);
		heckInorderInitialization();
	}

	@Test(expected = IllegalStateException.class)
	public void checkMissingMandatoryDependencyFailsInit() {
		moduleRegistry.addModule(consumerModule);
		moduleRegistry.init(objectMapper, null, null);
	}

	@Test
	public void checkMissingOptionalDependencyIsIgnored() {
		optional = true;
		moduleRegistry.addModule(consumerModule);
		moduleRegistry.init(objectMapper, null, null);

		Mockito.verify(providerModule, Mockito.times(0)).init();
		Mockito.verify(consumerModule, Mockito.times(1)).init();
	}

	@Test
	public void checkProviderAbleToGetExtensions() {
		moduleRegistry.addModule(consumerModule);
		moduleRegistry.addModule(providerModule);
		moduleRegistry.init(objectMapper, null, null);

		InOrder inOrder = Mockito.inOrder(providerModule);
		inOrder.verify(providerModule).setExtensions(Mockito.anyList());
		inOrder.verify(providerModule).init();

		ArgumentCaptor<List> extensionsCaptor = ArgumentCaptor.forClass(List.class);
		Mockito.verify(providerModule, Mockito.times(1)).setExtensions(extensionsCaptor.capture());
		List extensions = extensionsCaptor.getValue();
		Assert.assertEquals(1, extensions.size());
		Assert.assertTrue(extensions.get(0) instanceof TestModuleExtension);
	}

	@Test
	public void checkSimpleModule() {
		SimpleModule simpleModule = new SimpleModule("simple");
		simpleModule.addExtension(new TestModuleExtension());
		moduleRegistry.addModule(simpleModule);
		moduleRegistry.addModule(providerModule);
		moduleRegistry.init(objectMapper, null, null);

		ArgumentCaptor<List> extensionsCaptor = ArgumentCaptor.forClass(List.class);
		Mockito.verify(providerModule, Mockito.times(1)).setExtensions(extensionsCaptor.capture());
		List extensions = extensionsCaptor.getValue();
		Assert.assertEquals(1, extensions.size());
		Assert.assertTrue(extensions.get(0) instanceof TestModuleExtension);
	}


	private void heckInorderInitialization() {
		InOrder inOrder = Mockito.inOrder(providerModule, consumerModule);
		inOrder.verify(providerModule).init();
		inOrder.verify(consumerModule).init();

		Mockito.verify(providerModule, Mockito.times(1)).init();
		Mockito.verify(consumerModule, Mockito.times(1)).init();
	}


	class TestModuleExtension implements ModuleExtension {

		@Override
		public Class<? extends Module> getTargetModule() {
			return ExtensionProviderModule.class;
		}

		@Override
		public boolean isOptional() {
			return optional;
		}
	}

	class ExtensionProviderModule implements ModuleExtensionAware {

		@Override
		public void setExtensions(List extensions) {

		}

		@Override
		public void init() {

		}

		@Override
		public String getModuleName() {
			return "provider";
		}

		@Override
		public void setupModule(ModuleContext context) {
		}
	}

	class ExtensionConsumerModule implements InitializingModule {

		@Override
		public String getModuleName() {
			return "consumer";
		}

		@Override
		public void setupModule(ModuleContext context) {
			context.addExtension(new TestModuleExtension());
		}

		@Override
		public void init() {
		}
	}
}
