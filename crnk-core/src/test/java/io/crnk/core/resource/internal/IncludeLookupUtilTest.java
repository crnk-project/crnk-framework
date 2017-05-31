package io.crnk.core.resource.internal;

import io.crnk.core.boot.CrnkProperties;
import io.crnk.core.engine.internal.document.mapper.IncludeLookupUtil;
import io.crnk.core.engine.properties.PropertiesProvider;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class IncludeLookupUtilTest extends AbstractDocumentMapperTest {


	@Test
	public void checkDefaultLookupIncludeBehavior() {
		Assert.assertEquals(LookupIncludeBehavior.NONE, IncludeLookupUtil.getDefaultLookupIncludeBehavior(null));

		PropertiesProvider propertiesProvider = Mockito.mock(PropertiesProvider.class);
		Assert.assertEquals(LookupIncludeBehavior.NONE, IncludeLookupUtil.getDefaultLookupIncludeBehavior(propertiesProvider));

		Mockito.when(propertiesProvider.getProperty(CrnkProperties.INCLUDE_AUTOMATICALLY)).thenReturn("true");
		Assert.assertEquals(LookupIncludeBehavior.AUTOMATICALLY_WHEN_NULL,
				IncludeLookupUtil.getDefaultLookupIncludeBehavior(propertiesProvider));

		Mockito.when(propertiesProvider.getProperty(CrnkProperties.INCLUDE_AUTOMATICALLY)).thenReturn("false");
		Assert.assertEquals(LookupIncludeBehavior.NONE, IncludeLookupUtil.getDefaultLookupIncludeBehavior(propertiesProvider));

		Mockito.when(propertiesProvider.getProperty(CrnkProperties.INCLUDE_AUTOMATICALLY_OVERWRITE)).thenReturn("true");
		Assert.assertEquals(LookupIncludeBehavior.AUTOMATICALLY_ALWAYS,
				IncludeLookupUtil.getDefaultLookupIncludeBehavior(propertiesProvider));

		Mockito.when(propertiesProvider.getProperty(CrnkProperties.INCLUDE_AUTOMATICALLY_OVERWRITE)).thenReturn("false");
		Assert.assertEquals(LookupIncludeBehavior.NONE, IncludeLookupUtil.getDefaultLookupIncludeBehavior(propertiesProvider));
	}

}
