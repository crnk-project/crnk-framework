package io.crnk.gen.typescript.writer;

import org.junit.Assert;
import org.junit.Test;

public class TSCodeStyleTest {

	@Test
	public void test() {
		TSCodeStyle style = new TSCodeStyle();
		Assert.assertEquals("\t", style.getIndentation());
		Assert.assertEquals("\n", style.getLineSeparator());
		style.setIndentation("a");
		style.setLineSeparator("b");
		Assert.assertEquals("a", style.getIndentation());
		Assert.assertEquals("b", style.getLineSeparator());
	}
}
