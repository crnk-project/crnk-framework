package io.crnk.core.engine.internal.document.mapper.lookup;

import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.internal.document.mapper.AbstractDocumentMapperTest;
import io.crnk.core.engine.internal.document.mapper.IncludeLookupUtil;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class IncludeLookupUtilTest extends AbstractDocumentMapperTest {


    @Test
    public void checkLegacyDefaultLookupIncludeBehavior() {
        Assert.assertEquals(LookupIncludeBehavior.DEFAULT, IncludeLookupUtil.getGlobalLookupIncludeBehavior(null));

        PropertiesProvider propertiesProvider = Mockito.mock(PropertiesProvider.class);
        Assert.assertEquals(LookupIncludeBehavior.DEFAULT, IncludeLookupUtil.getGlobalLookupIncludeBehavior
                (propertiesProvider));
    }

    @Test
    public void checkDefaultLookupIncludeBehavior() {
        Assert.assertEquals(LookupIncludeBehavior.DEFAULT, IncludeLookupUtil.getGlobalLookupIncludeBehavior(null));

        PropertiesProvider propertiesProvider = Mockito.mock(PropertiesProvider.class);
        Mockito.when(propertiesProvider.getProperty(CrnkProperties.DEFAULT_LOOKUP_BEHAVIOR))
                .thenReturn(LookupIncludeBehavior.AUTOMATICALLY_ALWAYS.toString());
        Assert.assertEquals(LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
                IncludeLookupUtil.getGlobalLookupIncludeBehavior(propertiesProvider));
    }

}
