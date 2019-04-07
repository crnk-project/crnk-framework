package io.crnk.gen.asciidoc.internal;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaDocModel {

    private Map<String, ClassDocModel> docMap = new HashMap<>();


    public void loadFile(InputStream inputStream) {
        try {
            Document document = createDom(inputStream);
            Element root = document.getDocumentElement();
            List<Element> nodeList = getChildren(root, "package");
            for (Element packageElement : nodeList) {
                loadPackage(packageElement);
            }
        } catch (Exception e) {
            throw new IllegalStateException("failed to parse JavaDoc XML files", e);
        }
    }

    private void loadPackage(Element packageNode) {
        String packageName = packageNode.getAttributes().getNamedItem("name").getNodeValue();

        List<Element> list = getChildren(packageNode, "class");
        for (Element classElement : list) {
            loadClass(packageName, classElement);
        }
    }

    private void loadClass(String packageName, Element classNode) {
        Node nameNode = classNode.getAttributes().getNamedItem("name");
        if (nameNode != null) {
            String className = nameNode.getNodeValue();

            ClassDocModel classDoc = new ClassDocModel();
            classDoc.setText(getComment(classNode));
            String qualifiedName = packageName + "." + className;

            List<Element> fieldElements = getChildren(classNode, "field");
            for (Element fieldElement : fieldElements) {
                loadField(classDoc, fieldElement);
            }
            docMap.put(qualifiedName, classDoc);
        }
    }

    private void loadField(ClassDocModel classDoc, Element fieldNode) {
        String fieldName = fieldNode.getAttributes().getNamedItem("name").getNodeValue();
        String comment = getComment(fieldNode);
        classDoc.setAttributeText(fieldName, comment);
    }

    private String getComment(Element fieldNode) {
        List<Element> comments = getChildren(fieldNode, "comment");
        if (comments.size() > 0) {
            return comments.get(0).getTextContent();
        }
        return null;
    }

    private List<Element> getChildren(Element fieldNode, String name) {
        List<Element> elements = new ArrayList<>();
        NodeList childNodes = fieldNode.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item instanceof Element && ((Element) item).getNodeName().equals(name)) {
                elements.add((Element) item);
            }
        }
        return elements;
    }

    public ClassDocModel getClassModel(String className) {
        ClassDocModel doc = docMap.get(className);
        return doc != null ? doc : new ClassDocModel();
    }

    private Document createDom(InputStream in) throws ParserConfigurationException, IOException, SAXException {
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        return dBuilder.parse(in);
    }
}
