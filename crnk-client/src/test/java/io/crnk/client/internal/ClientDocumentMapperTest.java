package io.crnk.client.internal;

import io.crnk.core.boot.CrnkBoot;
import io.crnk.core.engine.document.Document;
import io.crnk.core.engine.document.ErrorDataBuilder;
import io.crnk.core.engine.properties.NullPropertiesProvider;
import io.crnk.core.engine.query.QueryContext;
import io.crnk.core.utils.Nullable;
import io.crnk.test.mock.TestModule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

public class ClientDocumentMapperTest {


    private CrnkBoot boot;

    private ClientDocumentMapper documentMapper;

    private QueryContext queryContext = new QueryContext();

    @Before
    public void setup() {
        queryContext.setRequestVersion(0);

        boot = new CrnkBoot();
        boot.addModule(new TestModule());
        boot.boot();

        NullPropertiesProvider properties = new NullPropertiesProvider();
        documentMapper = new ClientDocumentMapper(boot.getModuleRegistry(), boot.getObjectMapper(), properties);
    }


    @Test
    public void testNullData() {
        Document doc = new Document();
        doc.setData(Nullable.nullValue());
        Assert.assertNull(documentMapper.fromDocument(doc, false, queryContext));
    }

    @Test
    public void testNoData() {
        Document doc = new Document();
        doc.setData(Nullable.empty());
        Assert.assertNull(documentMapper.fromDocument(doc, false, queryContext));
    }

    @Test(expected = IllegalStateException.class)
    public void testCannotHaveErrors() {
        Document doc = new Document();
        doc.setErrors(Arrays.asList(new ErrorDataBuilder().build()));
        doc.setData(Nullable.nullValue());
        documentMapper.fromDocument(doc, false, queryContext);
    }
}
