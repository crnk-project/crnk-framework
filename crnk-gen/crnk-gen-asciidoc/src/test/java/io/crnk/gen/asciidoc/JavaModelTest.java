package io.crnk.gen.asciidoc;

import io.crnk.gen.asciidoc.internal.ClassDocModel;
import io.crnk.gen.asciidoc.internal.JavaDocModel;
import org.junit.Assert;
import org.junit.Test;

public class JavaModelTest {

    @Test
    public void test() {
        JavaDocModel model = new JavaDocModel();
        model.loadFile(getClass().getClassLoader().getResourceAsStream("javadoc.xml"));

        ClassDocModel classModel = model.getClassModel("io.crnk.example.service.relationship.dynamic.AttributeChange");
        Assert.assertNotNull(classModel.getAttributeText("newValue"));
    }
}
