package io.crnk.gen.typescript.processor;

import io.crnk.gen.typescript.model.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

public class TSImportProcessorTest {

	private HashSet<TSSource> sources;

	private TSSource interfaceSource;

	private TSClassType classType;

	private TSSource classSource;

	private TSInterfaceType interfaceType;

	private TSImportProcessor processor;

	@Before
	public void setup() {
		processor = new TSImportProcessor();

		interfaceType = new TSInterfaceType();
		interfaceType.setName("SomeInterface");
		interfaceSource = new TSSource();
		interfaceSource.addElement(interfaceType);
		interfaceSource.setNpmPackage("@crnk/test");
		interfaceSource.setDirectory("someDir");
		interfaceSource.setName("some-interface");

		classType = new TSClassType();
		classType.setName("SomeClass");
		classType.getImplementedInterfaces().add(interfaceType);
		classSource = new TSSource();
		classSource.setNpmPackage("@crnk/test");
		classSource.setDirectory("someDir");
		classSource.setName("some-class");
		classSource.addElement(classType);

		sources = new HashSet<>();
		sources.add(classSource);
		sources.add(interfaceSource);
	}

	@Test
	public void sameDirectoryImport() {
		Set<TSSource> updatedSources = processor.process(sources);
		Assert.assertEquals(sources.size(), updatedSources.size());

		Assert.assertEquals(1, classSource.getImports().size());
		TSImport tsImport = classSource.getImports().get(0);
		Assert.assertEquals("./some-interface", tsImport.getPath());
	}

	@Test
	public void childDirectoryImport() {
		interfaceSource.setDirectory("someDir/child-dir");

		processor.process(sources);
		TSImport tsImport = classSource.getImports().get(0);
		Assert.assertEquals("./child-dir/some-interface", tsImport.getPath());
	}

	@Test
	public void parentDirectoryImport() {
		interfaceSource.setDirectory(null);

		processor.process(sources);
		TSImport tsImport = classSource.getImports().get(0);
		Assert.assertEquals("../some-interface", tsImport.getPath());
	}

	@Test
	public void siblingDirectoryImport() {
		interfaceSource.setDirectory("other-dir");

		processor.process(sources);
		TSImport tsImport = classSource.getImports().get(0);
		Assert.assertEquals("../other-dir/some-interface", tsImport.getPath());
	}


	@Test
	public void checkArrayElementTypeImported() {
		TSField field = new TSField();
		field.setName("someField");
		field.setType(new TSArrayType(interfaceType));
		classType.getImplementedInterfaces().clear();
		classType.addDeclaredMember(field);

		processor.process(sources);
		TSImport tsImport = classSource.getImports().get(0);
		Assert.assertEquals("./some-interface", tsImport.getPath());
	}

	@Test
	public void checkParameterizedTypeImported() {
		TSInterfaceType parameterType = new TSInterfaceType();
		parameterType.setName("ParamInterface");
		TSSource paramSource = new TSSource();
		paramSource.addElement(parameterType);
		paramSource.setNpmPackage("@crnk/test");
		paramSource.setDirectory("someDir");
		paramSource.setName("some-param");
		sources.add(paramSource);

		TSField field = new TSField();
		field.setName("someField");
		field.setType(new TSParameterizedType(interfaceType, parameterType));
		classType.getImplementedInterfaces().clear();
		classType.addDeclaredMember(field);

		processor.process(sources);
		Assert.assertEquals(2, classSource.getImports().size());
		Assert.assertEquals("./some-interface", classSource.getImports().get(0).getPath());
		Assert.assertEquals("./some-param", classSource.getImports().get(1).getPath());
	}
}
