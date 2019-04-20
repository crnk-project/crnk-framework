package io.crnk.gen.asciidoc.internal;

import io.crnk.core.engine.internal.utils.IOUtils;
import io.crnk.meta.model.MetaAttribute;
import io.crnk.meta.model.MetaDataObject;
import io.crnk.meta.model.MetaElement;
import io.crnk.meta.model.MetaEnumType;
import io.crnk.meta.model.MetaLiteral;
import io.crnk.meta.model.MetaType;
import io.crnk.meta.model.resource.MetaResource;
import io.crnk.meta.model.resource.MetaResourceField;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class AsciidocBuilder {

    private final JavaDocModel javaDoc;

    private StringBuilder builder = new StringBuilder();

    private int depth;

    private Stack<String> anchors = new Stack<>();

    public AsciidocBuilder(JavaDocModel javaDoc, int depth) {
        this.javaDoc = javaDoc;
        this.depth = depth;
    }

    public void appendLineBreak() {
        builder.append("\n");
    }

    public void appendLine(String line) {
        builder.append(line);
        appendLineBreak();
    }

    public void startSection(String title) {
        String anchor = AsciidocUtils.toAnchor(title);
        startSection(title, anchor);
    }

    public void startSection(String title, String anchor) {
        depth++;
        anchors.push(anchor);

        appendLineBreak();

        StringBuilder anchorBuilder = new StringBuilder();
        anchorBuilder.append("anchor:");
        for (int i = 0; i < anchors.size(); i++) {
            anchorBuilder.append(anchors.get(i));
            if (i < anchors.size() - 1) {
                anchorBuilder.append("_");
            }
        }
        anchorBuilder.append("[]");
        builder.append(anchorBuilder);

        appendLineBreak();
        appendLineBreak();
        for (int i = 0; i < depth; i++) {
            builder.append("#");
        }
        appendLine(" " + title);
        appendLineBreak();
    }

    public void endSection() {
        depth--;
        anchors.pop();
        appendLineBreak();
        appendLineBreak();
    }


    public void startTable(String cols, String options) {
        appendLine(String.format("[cols=\"%s\", options=\"%s\"]", cols, options));
        appendLine("|===");
    }

    public void endTable() {
        appendLine("|===");
    }

    public void appendDescription(MetaType type) {
        appendLine(getDescription(type));

    }

    public void appendOverview(MetaType type, File outputDir) {
        startSection(type.getName());
        appendLine(getDescription(type));

        File descriptionFile = new File(outputDir, AsciidocGeneratorModule.DESCRIPTION_FILE);
        if (descriptionFile.exists()) {
            writeInclude(descriptionFile.getName());
        }

        File attributesFile = new File(outputDir, AsciidocGeneratorModule.ATTRIBUTES_FILE);
        if (attributesFile.exists()) {
            startSection("Attributes");
            writeInclude(attributesFile.getName());
            endSection();
        }

        File literalsFile = new File(outputDir, AsciidocGeneratorModule.LITERALS_FILE);
        if (literalsFile.exists()) {
            startSection("Literals");
            writeInclude(literalsFile.getName());
            endSection();
        }

        File relationshipsFile = new File(outputDir, AsciidocGeneratorModule.RELATIONSHIP_FILE);
        if (relationshipsFile.exists()) {
            startSection("Relationships");
            writeInclude(relationshipsFile.getName());
            endSection();
        }

        File examplesFile = new File(outputDir, AsciidocGeneratorModule.EXAMPLES_FILE);
        if (examplesFile.exists()) {
            startSection("Examples");
            writeInclude(examplesFile.getName());
            endSection();
        }
        endSection();
    }

    public void appendExamples(File outputDir) {
        List<CapturedExample> examples = findExamples(outputDir);
        if (!examples.isEmpty()) {
            for (CapturedExample example : examples) {
                startSection(example.title);
                if (example.descriptionFile.exists()) {
                    writeInclude(example.descriptionFile.getName());
                }

                startSection("Request");
                writeInclude(example.requestFile.getName());
                endSection();

                if (example.responseFile.exists()) {
                    startSection("Response");
                    writeInclude(example.responseFile.getName());
                    endSection();
                }
                endSection();
            }
        }
    }


    public List<CapturedExample> findExamples(File outputDir) {
        return Arrays.asList(outputDir.listFiles()).stream()
                .filter(it -> it.getName().startsWith("captured_"))
                .map(it -> it.getName())
                .map(it -> it.substring("captured_".length(), it.lastIndexOf("_")))
                .sorted()
                .distinct()
                .map(it -> new CapturedExample(outputDir, it))
                .collect(Collectors.toList());

    }

    public void appendAnchor(String anchor, String resourceType) {
        builder.append("<<" + anchor + "," + resourceType + ">>");
    }

    public void startCell() {
        builder.append("| ");
    }

    public void endCell() {
        appendLineBreak();
    }

    public Collection<String> getAnchors() {
        return anchors;
    }

    class CapturedExample {

        private final String key;

        private final File outputDir;

        private final File requestFile;

        private final File responseFile;

        private final File descriptionFile;

        private String title;

        private File urlFile;

        public CapturedExample(File outputDir, String key) {
            this.key = key;
            this.outputDir = outputDir;
            this.urlFile = getFile("url.adoc");
            this.requestFile = getFile("request.adoc");
            this.responseFile = getFile("response.adoc");
            this.title = readFile(getFile("title.txt"));
            this.descriptionFile = getFile("description.adoc");
        }

        private String readFile(File file) {
            try (FileInputStream in = new FileInputStream(file)) {
                return new String(IOUtils.readFully(in), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        private File getFile(String name) {
            return new File(outputDir, "captured_" + key + "_" + name);
        }
    }


    public void appendFields(MetaResource resource, boolean relationships) {
        List<MetaResourceField> fields = resource.getDeclaredAttributes();
        List<MetaResourceField> attributes = fields.stream().filter(it -> relationships == it.isAssociation()).collect(Collectors.toList());
        if (!attributes.isEmpty()) {
            startTable(relationships ? "2,4,2,1,1,1,1" : "2,4,2,1,1,1,1", "header");
            appendCell("Name");
            appendCell("Description");
            if (relationships) {
                appendCell("Opposite");
            } else {
                appendCell("Type");
            }
            appendCell("Post");
            appendCell("Patch");
            appendCell("Sort");
            appendCell("Filter");
            for (MetaResourceField attribute : attributes) {
                appendCell(attribute.getName());
                appendCell(getDescription(attribute));

                startCell();
                if (relationships) {
                    MetaType elementType = attribute.getType().getElementType();
                    if (elementType instanceof MetaResource) {
                        MetaResource oppositeType = (MetaResource) elementType;
                        appendAnchor(oppositeType);
                    }
                    MetaAttribute oppositeAttribute = attribute.getOppositeAttribute();
                    if (oppositeAttribute != null) {
                        append(".");
                        append(oppositeAttribute.getName());
                    }
                } else {
                    // consider move to non-JAVA typing?
                    Class<?> implClass = attribute.getType().getImplementationClass();
                    append(implClass.getSimpleName());
                }
                endCell();

                appendCell(attribute.isInsertable());
                appendCell(attribute.isUpdatable());
                appendCell(attribute.isSortable());
                appendCell(attribute.isFilterable());
                appendLineBreak();
            }
            endTable();
        }
    }

    public void appendFields(MetaDataObject metaDataObject) {
        List<? extends MetaAttribute> fields = metaDataObject.getDeclaredAttributes();
        if (!fields.isEmpty()) {
            startTable("2,4,2", "Header");
            appendCell("Name");
            appendCell("Description");
            appendCell("Type");
            for (MetaAttribute attribute : fields) {
                appendCell(attribute.getName());
                appendCell(getDescription(attribute));
                startCell();

                // consider move to non-JAVA typing?
                Class<?> implClass = attribute.getType().getImplementationClass();
                append(implClass.getSimpleName());

                appendLineBreak();
            }
            endTable();
        }
    }

    public void appendLiterals(MetaEnumType enumType) {
        startTable("2,6", "Header");
        appendCell("Name");
        appendCell("Description");
        for (MetaElement attribute : enumType.getChildren()) {
            appendCell(attribute.getName());
            appendCell(getDescription((MetaLiteral) attribute));
        }
        endTable();
    }

    private void appendAnchor(MetaResource resource) {
        appendAnchor(AsciidocUtils.getAnchor(resource), resource.getResourceType());
    }


    private String getDescription(MetaAttribute attribute) {
        MetaDataObject parent = attribute.getParent();
        if (parent.getImplementationClass() != null) {
            String className = parent.getImplementationClass().getName();
            ClassDocModel classDoc = javaDoc.getClassModel(className);
            return AsciidocUtils.fromHtml(classDoc.getAttributeText(attribute.getUnderlyingName()));
        }
        return null;
    }

    private String getDescription(MetaLiteral literal) {
        MetaEnumType parent = (MetaEnumType) literal.getParent();
        if (parent.getImplementationClass() != null) {
            String className = parent.getImplementationClass().getName();
            ClassDocModel classDoc = javaDoc.getClassModel(className);
            return AsciidocUtils.fromHtml(classDoc.getAttributeText(literal.getName()));
        }
        return null;
    }


    public String getDescription(MetaType type) {
        if (type.getImplementationClass() != null) {
            String className = type.getImplementationClass().getName();
            ClassDocModel classDoc = javaDoc.getClassModel(className);
            return AsciidocUtils.fromHtml(classDoc.getText());
        }
        return null;
    }

    public void appendCell(String cell) {
        builder.append("| ");
        appendLine(cell);
    }


    private void appendCell(boolean value) {
        builder.append("| ");
        // https://www.fileformat.info/info/unicode/char/2713/index.htm
        appendLine(value ? "&#10003;" : "");
    }


    @Override
    public String toString() {
        return builder.toString();
    }

    public void writeInclude(String name) {
        String fileName = name.endsWith(".adoc") ? name : name + ".adoc";
        appendLine(String.format("include::%s[]", fileName));
    }

    public void startSource() {
        appendLine("[source]");
        appendLine("----");
    }

    public void endSource() {
        appendLine("----");
    }

    public void append(String text) {
        builder.append(text);
    }

    public void write(File file) {
        String text = builder.toString();
        if (text.trim().length() > 0) {
            IOUtils.writeFile(file, text);
        }
    }
}