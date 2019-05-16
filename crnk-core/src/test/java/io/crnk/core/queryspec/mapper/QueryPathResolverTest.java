package io.crnk.core.queryspec.mapper;

import io.crnk.core.CoreTestContainer;
import io.crnk.core.CoreTestModule;
import io.crnk.core.engine.http.HttpStatus;
import io.crnk.core.engine.information.resource.ResourceInformation;
import io.crnk.core.exception.BadRequestException;
import io.crnk.core.mock.models.Project;
import io.crnk.core.mock.models.Schedule;
import io.crnk.core.mock.models.Task;
import io.crnk.core.queryspec.AbstractQuerySpecTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class QueryPathResolverTest extends AbstractQuerySpecTest {

    private QueryPathResolver resolver;


    @Before
    public void setup() {
        super.setup();

        DefaultQuerySpecUrlMapper urlMapper = (DefaultQuerySpecUrlMapper) container.getBoot().getUrlMapper();
        resolver = urlMapper.getPathResolver();
    }

    @Override
    protected void setup(CoreTestContainer container) {
        container.getBoot().addModule(new CoreTestModule());
    }

    @Test
    public void checkPrimitiveAttribute() {
        ResourceInformation resourceInformation = resourceRegistry.getEntry(Task.class).getResourceInformation();
        List<String> jsonPath = Arrays.asList("name");

        QueryPathSpec spec = resolver.resolve(resourceInformation, jsonPath, QueryPathResolver.NamingType.JAVA, "test");
        Assert.assertEquals(String.class, spec.getValueType());
        Assert.assertEquals(jsonPath, spec.getAttributePath());
    }

    @Test
    public void checkIdAttribute() {
        ResourceInformation resourceInformation = resourceRegistry.getEntry(Task.class).getResourceInformation();
        List<String> jsonPath = Arrays.asList("id");

        QueryPathSpec spec = resolver.resolve(resourceInformation, jsonPath, QueryPathResolver.NamingType.JAVA, "test");
        Assert.assertEquals(Long.class, spec.getValueType());
        Assert.assertEquals(jsonPath, spec.getAttributePath());
    }


    @Test
    public void checkNestedAttribute() {
        ResourceInformation resourceInformation = resourceRegistry.getEntry(Project.class).getResourceInformation();
        List<String> jsonPath = Arrays.asList("data", "data");

        QueryPathSpec spec = resolver.resolve(resourceInformation, jsonPath, QueryPathResolver.NamingType.JAVA, "test");
        Assert.assertEquals(String.class, spec.getValueType());
        Assert.assertEquals(jsonPath, spec.getAttributePath());
    }


    @Test
    public void checkMapAttribute() {
        ResourceInformation resourceInformation = resourceRegistry.getEntry(Project.class).getResourceInformation();
        List<String> jsonPath = Arrays.asList("data", "priorities", "foo");

        QueryPathSpec spec = resolver.resolve(resourceInformation, jsonPath, QueryPathResolver.NamingType.JAVA, "test");
        Assert.assertEquals(Integer.class, spec.getValueType());
        Assert.assertEquals(jsonPath, spec.getAttributePath());
    }

    @Test
    public void checkRelation() {
        ResourceInformation resourceInformation = resourceRegistry.getEntry(Task.class).getResourceInformation();
        List<String> jsonPath = Arrays.asList("project");

        QueryPathSpec spec = resolver.resolve(resourceInformation, jsonPath, QueryPathResolver.NamingType.JAVA, "test");
        Assert.assertEquals(Project.class, spec.getValueType());
        Assert.assertEquals(jsonPath, spec.getAttributePath());
    }

    @Test
    public void checkUnknownAttributesFailsByDefault() {
        ResourceInformation resourceInformation = resourceRegistry.getEntry(Task.class).getResourceInformation();
        List<String> jsonPath = Arrays.asList("doesNotExists");

        try {
            resolver.resolve(resourceInformation, jsonPath, QueryPathResolver.NamingType.JAVA, "test");
            Assert.fail();
        } catch (BadRequestException e) {
            // ok
            Assert.assertEquals(HttpStatus.BAD_REQUEST_400, e.getHttpStatus());
            Assert.assertEquals("test", e.getErrorData().getSourceParameter());
        }
    }

    @Test
    public void checkUnknownAttributesIgnoredIfAllowed() {
        ResourceInformation resourceInformation = resourceRegistry.getEntry(Task.class).getResourceInformation();
        resolver.setAllowUnknownAttributes(true);
        List<String> jsonPath = Arrays.asList("doesNotExists");

        QueryPathSpec spec = resolver.resolve(resourceInformation, jsonPath, QueryPathResolver.NamingType.JAVA, "test");
        Assert.assertEquals(jsonPath, spec.getAttributePath());
        Assert.assertEquals(Object.class, spec.getValueType());
    }

    @Test
    public void checkJsonNameMapping() {
        ResourceInformation resourceInformation = resourceRegistry.getEntry(Schedule.class).getResourceInformation();
        List<String> jsonPath = Arrays.asList("description");

        QueryPathSpec spec = resolver.resolve(resourceInformation, jsonPath, QueryPathResolver.NamingType.JSON, "test");
        Assert.assertEquals(Arrays.asList("desc"), spec.getAttributePath());
        Assert.assertEquals(String.class, spec.getValueType());
    }

    @Test
    public void checkJavaNameMapping() {
        ResourceInformation resourceInformation = resourceRegistry.getEntry(Schedule.class).getResourceInformation();
        List<String> jsonPath = Arrays.asList("desc");

        QueryPathSpec spec = resolver.resolve(resourceInformation, jsonPath, QueryPathResolver.NamingType.JAVA, "test");
        Assert.assertEquals(Arrays.asList("description"), spec.getAttributePath());
        Assert.assertEquals(String.class, spec.getValueType());
    }

    @Test(expected = BadRequestException.class)
    public void checkJavaNameNotAccessibleIfJsonNameDiffers() {
        ResourceInformation resourceInformation = resourceRegistry.getEntry(Schedule.class).getResourceInformation();
        List<String> jsonPath = Arrays.asList("desc");

        resolver.resolve(resourceInformation, jsonPath, QueryPathResolver.NamingType.JSON, "test");
    }

    @Test
    public void checkRelationAttr() {
        ResourceInformation resourceInformation = resourceRegistry.getEntry(Task.class).getResourceInformation();
        List<String> jsonPath = Arrays.asList("project", "id");

        QueryPathSpec spec = resolver.resolve(resourceInformation, jsonPath, QueryPathResolver.NamingType.JAVA, "test");
        Assert.assertEquals(Long.class, spec.getValueType());
        Assert.assertEquals(jsonPath, spec.getAttributePath());
    }

    @Test
    public void checkNestedRelations() {
        List<String> jsonPath = Arrays.asList("project", "includedTask");
        ResourceInformation resourceInformation = resourceRegistry.getEntry(Task.class).getResourceInformation();

        QueryPathSpec spec = resolver.resolve(resourceInformation, jsonPath, QueryPathResolver.NamingType.JAVA, "test");
        Assert.assertEquals(Task.class, spec.getValueType());
        Assert.assertEquals(jsonPath, spec.getAttributePath());
    }


}
