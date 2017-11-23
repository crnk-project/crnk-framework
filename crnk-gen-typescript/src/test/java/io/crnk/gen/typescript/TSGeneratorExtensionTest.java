package io.crnk.gen.typescript;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import groovy.lang.Closure;
import org.gradle.api.Project;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class TSGeneratorExtensionTest {

	@Test
	public void test() {
		Project project = Mockito.mock(Project.class);
		TSGeneratorExtension config = new TSGeneratorExtension(project, null);

		Set<String> includes = new HashSet<>();
		config.setIncludes(includes);
		Assert.assertSame(includes, config.getIncludes());

		Set<String> excludes = new HashSet<>();
		config.setExcludes(excludes);
		Assert.assertSame(excludes, config.getExcludes());

		Assert.assertFalse(config.getGenerateExpressions());
		config.setGenerateExpressions(true);
		Assert.assertTrue(config.getGenerateExpressions());

		Assert.assertEquals("src", config.getSourceDirectoryName());
		config.setSourceDirectoryName("testDir");
		Assert.assertEquals("testDir", config.getSourceDirectoryName());

		Assert.assertEquals("UNLICENSED", config.getNpm().getLicense());
		config.getNpm().setLicense("someLicense");
		Assert.assertEquals("someLicense", config.getNpm().getLicense());

		Assert.assertEquals(null, config.getNpm().getGitRepository());
		config.getNpm().setGitRepository("git");
		Assert.assertEquals("git", config.getNpm().getGitRepository());

		Assert.assertEquals(null, config.getNpm().getDescription());
		config.getNpm().setDescription("desc");
		Assert.assertEquals("desc", config.getNpm().getDescription());

		Map packageMapping = new HashMap();
		config.getNpm().setPackageMapping(packageMapping);
		Assert.assertSame(packageMapping, config.getNpm().getPackageMapping());

		Map peerDep = new HashMap();
		config.getNpm().setPeerDependencies(peerDep);
		Assert.assertSame(peerDep, config.getNpm().getPeerDependencies());

		Map devDep = new HashMap();
		config.getNpm().setDevDependencies(devDep);
		Assert.assertSame(devDep, config.getNpm().getDevDependencies());

		Closure closure = Mockito.mock(Closure.class);
		config.npm(closure);
		Mockito.verify(project, Mockito.times(1)).configure(Mockito.anyObject(), Mockito.eq(closure));
	}
}
