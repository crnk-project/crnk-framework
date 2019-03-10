package io.crnk.gen.java;

import com.google.common.collect.ImmutableList;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import io.crnk.core.engine.internal.utils.IOUtils;
import io.crnk.core.queryspec.Direction;
import io.crnk.core.queryspec.FilterOperator;
import io.crnk.core.queryspec.FilterSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.SortSpec;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.JavaFileObject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class CrnkProcessorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(CrnkProcessorTest.class);

    @Test
    public void test() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        JavaFileObject projectSource = JavaFileObjects.forResource("Project.java");
        JavaFileObject userSource = JavaFileObjects.forResource("UserEntity.java");
        JavaFileObject addressSource = JavaFileObjects.forResource("UserAddress.java");
        JavaFileObject testSource = JavaFileObjects.forResource("TypedQuerySpecTest.java");

        Compilation compilation = javac()
                .withProcessors(new CrnkProcessor())
                .compile(projectSource, userSource, addressSource, testSource);
        assertThat(compilation).succeededWithoutWarnings();

        ImmutableList<JavaFileObject> generatedFiles = compilation.generatedFiles();
        Assert.assertEquals(3 + 3 + 9 + 1, generatedFiles.size());

        URLClassLoader classLoader = toClassLoader(generatedFiles);
        Class<?> testClass = classLoader.loadClass("test.TypedQuerySpecTest");
        Object testObject = testClass.newInstance();

        JavaFileObject sourceFile = compilation.generatedSourceFile("test.UserPathSpec").get();
        LOGGER.debug(sourceFile.getCharContent(true).toString());

        QuerySpec querySpec = (QuerySpec) testClass.getMethod("createQuerySpec").invoke(testObject);
        FilterSpec filterSpec = (FilterSpec) testClass.getMethod("createFilterSpec").invoke(testObject);
        SortSpec sortSpec = (SortSpec) testClass.getMethod("createSortSpec").invoke(testObject);

        Assert.assertEquals(1, querySpec.getFilters().size());
        Assert.assertEquals(1, querySpec.getSort().size());
        Assert.assertEquals(1, querySpec.getIncludedFields().size());
        Assert.assertEquals(1, querySpec.getIncludedRelations().size());

        FilterSpec queryFilterSpec = querySpec.getFilters().get(0);
        Assert.assertEquals(FilterOperator.EQ, queryFilterSpec.getOperator());
        Assert.assertEquals("projects.id", queryFilterSpec.getPath().toString());
        Assert.assertEquals((Integer) 12, queryFilterSpec.getValue());

        SortSpec querySortSpec = querySpec.getSort().get(0);
        Assert.assertEquals(Direction.DESC, querySortSpec.getDirection());
        Assert.assertEquals("loginId", querySortSpec.getPath().toString());

        Assert.assertEquals("projects", querySpec.getIncludedRelations().get(0).getPath().toString());
        Assert.assertEquals("loginId", querySpec.getIncludedFields().get(0).getPath().toString());

        Assert.assertEquals(FilterOperator.EQ, filterSpec.getOperator());
        Assert.assertEquals("projects.id", filterSpec.getPath().toString());
        Assert.assertEquals((Integer) 12, filterSpec.getValue());

        Assert.assertEquals(Direction.DESC, sortSpec.getDirection());
        Assert.assertEquals("projects.id", sortSpec.getPath().toString());
    }

    private URLClassLoader toClassLoader(ImmutableList<JavaFileObject> generatedFiles) throws IOException {
        Path tempDir = Files.createTempDirectory("test");

        generatedFiles.stream().filter(it -> it.getKind() == JavaFileObject.Kind.CLASS).forEach(it -> save(it, tempDir));
        return new URLClassLoader(new URL[]{tempDir.toUri().toURL()}, getClass().getClassLoader());
    }

    private void save(JavaFileObject it, Path tempDir) {
        String name = it.getName();
        File file = new File(tempDir.toFile(), name.substring("CLASS_OUTPUT/".length()));
        file.getParentFile().mkdirs();
        try (FileOutputStream out = new FileOutputStream(file)) {
            byte[] bytes = IOUtils.readFully(it.openInputStream());
            out.write(bytes);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
