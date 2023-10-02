package io.crnk.gen.java;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.crnk.core.queryspec.AbstractPathSpec;
import io.crnk.core.queryspec.PathSpec;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.queryspec.internal.typed.PrimitivePathSpec;
import io.crnk.core.queryspec.internal.typed.ResourcePathSpec;
import io.crnk.core.queryspec.internal.typed.TypedQuerySpec;
import io.crnk.core.resource.annotations.JsonApiEmbeddable;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;

import jakarta.annotation.Generated;
import jakarta.annotation.processing.AbstractProcessor;
import jakarta.annotation.processing.Messager;
import jakarta.annotation.processing.ProcessingEnvironment;
import jakarta.annotation.processing.RoundEnvironment;
import jakarta.lang.model.SourceVersion;
import jakarta.lang.model.element.AnnotationMirror;
import jakarta.lang.model.element.Element;
import jakarta.lang.model.element.ElementKind;
import jakarta.lang.model.element.ExecutableElement;
import jakarta.lang.model.element.Modifier;
import jakarta.lang.model.element.TypeElement;
import jakarta.lang.model.type.PrimitiveType;
import jakarta.lang.model.type.TypeMirror;
import jakarta.tools.Diagnostic;
import jakarta.tools.JavaFileObject;
import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Generates type-safe {@link QuerySpec} and {@link PathSpec} classes by inspecting compiled resource classes.
 */
public class CrnkProcessor extends AbstractProcessor {

    private static final String PATH_SUFFIX = "PathSpec";

    private static final String QUERY_SUFFIX = "QuerySpec";

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnv) {
        Messager messager = processingEnv.getMessager();

        Set<? extends Element> resourceElements = roundEnv.getElementsAnnotatedWith(JsonApiResource.class);
        Set<? extends Element> embeddableElements = roundEnv.getElementsAnnotatedWith(JsonApiEmbeddable.class);
        Set<? extends Element> elements = new HashSet<>();
        elements.addAll((Set) resourceElements);
        elements.addAll((Set) embeddableElements);

        Set<String> resourceNames = new HashSet<>();
        for (Element element : elements) {
            TypeElement typeElement = (TypeElement) element;
            resourceNames.add(typeElement.getQualifiedName().toString());
        }

        for (Element element : elements) {
            if (element.getKind() != ElementKind.CLASS) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Can be applied to class.");
                return true;
            }
            TypeElement resourceType = (TypeElement) element;
            buildPathType(resourceType);
            buildQueryType(resourceType);
        }
        return true;
    }

    private void buildQueryType(TypeElement resourceType) {
        AnnotationSpec annotationSpec = createGeneratedAnnotation();

        String packageName = getPackageName(resourceType);

        ParameterizedTypeName superType = ParameterizedTypeName.get(
                ClassName.get(TypedQuerySpec.class),
                ClassName.get(packageName, resourceType.getSimpleName().toString()),
                ClassName.get(packageName, getSimpleName(resourceType, false))
        );

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(getSimpleName(resourceType, true))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(annotationSpec)
                .superclass(superType);

        addQueryConstructors(resourceType, typeBuilder);

        write(resourceType, typeBuilder, true);
    }

    private void addQueryConstructors(TypeElement resourceType, TypeSpec.Builder typeBuilder) {
        String pathClassName = getSimpleName(resourceType.getSimpleName().toString(), false);

        MethodSpec.Builder defaultConstructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement(String.format("super(%s.class, new %s())", resourceType.getQualifiedName(), pathClassName));
        typeBuilder.addMethod(defaultConstructor.build());
    }

    private void addBindMethod(TypeElement resourceType, TypeSpec.Builder typeBuilder) {
        String pathClassName = getSimpleName(resourceType, false);
        String packageName = getPackageName(resourceType);

        MethodSpec.Builder method = MethodSpec.methodBuilder("bindSpec")
                .addModifiers(Modifier.PROTECTED)
                .addParameter(AbstractPathSpec.class, "spec")
                .returns(ClassName.get(packageName, pathClassName))
                .addStatement(String.format("return new %s(spec)", pathClassName));
        typeBuilder.addMethod(method.build());
    }

    private void buildPathType(TypeElement resourceType) {
        AnnotationSpec annotationSpec = createGeneratedAnnotation();

        TypeSpec.Builder typeBuilder = TypeSpec.classBuilder(getSimpleName(resourceType, false))
                .addModifiers(Modifier.PUBLIC)
                .addAnnotation(annotationSpec)
                .superclass(ResourcePathSpec.class);

        addPathConstants(resourceType, typeBuilder);
        addPathConstructors(typeBuilder);
        addPathFields(resourceType, typeBuilder);
        addBindMethod(resourceType, typeBuilder);
        write(resourceType, typeBuilder, false);
    }

    private AnnotationSpec createGeneratedAnnotation() {
        return AnnotationSpec.builder(Generated.class).addMember("value", "\"Generated by Crnk annotation processor\"").build();
    }

    private void addPathConstants(TypeElement resourceType, TypeSpec.Builder typeBuilder) {
        TypeName typeName = ClassName.bestGuess(getSimpleName(resourceType, false));
        String name = firstToLower(getSimpleName(resourceType.getSimpleName().toString(), false));

        FieldSpec.Builder fieldBuilder = FieldSpec.builder(typeName, name, Modifier.STATIC, Modifier.PUBLIC);
        fieldBuilder.initializer("new " + typeName.toString() + "()");
        typeBuilder.addField(fieldBuilder.build());
    }

    private String firstToLower(String value) {
        return Character.toLowerCase(value.charAt(0)) + value.substring(1);
    }


    private void addPathConstructors(TypeSpec.Builder typeBuilder) {
        MethodSpec.Builder defaultConstructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement(String.format("super(PathSpec.empty())"));
        typeBuilder.addMethod(defaultConstructor.build());

        MethodSpec.Builder pathConstructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addParameter(PathSpec.class, "pathSpec")
                .addStatement(String.format("super(pathSpec)"));
        typeBuilder.addMethod(pathConstructor.build());

        MethodSpec.Builder specConstructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PROTECTED)
                .addParameter(AbstractPathSpec.class, "spec")
                .addStatement(String.format("super(spec)"));
        typeBuilder.addMethod(specConstructor.build());
    }

    private String getSimpleName(Element resourceType, boolean queryClass) {
        return getSimpleName(resourceType.getSimpleName().toString(), queryClass);
    }

    private String getPackageName(Element resourceType) {
        return processingEnv.getElementUtils().getPackageOf(resourceType).getQualifiedName().toString();
    }

    private String getQualifiedName(Element resourceType, boolean query) {
        String packageName = getPackageName(resourceType);
        return packageName + (packageName.isEmpty() ? "" : ".") + getSimpleName(resourceType, query);
    }

    private void write(Element element, TypeSpec.Builder typeBuilder, boolean query) {
        try {
            String packageName = getPackageName(element);
            String qualifiedName = getQualifiedName(element, query);
            JavaFileObject sourceFile = processingEnv.getFiler().createSourceFile(qualifiedName, element);
            try (Writer writer = sourceFile.openWriter()) {
                JavaFile.builder(packageName, typeBuilder.build())
                        .indent(" ")
                        .build()
                        .writeTo(writer);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private void addPathFields(TypeElement typeElement, TypeSpec.Builder typeBuilder) {
        List<? extends Element> elements = typeElement.getEnclosedElements();

        Set<String> relationships = new HashSet<>();
        for (Element member : elements) {
            String memberName = getMemberName(member);
            if (memberName != null && isRelationship(member)) {
                relationships.add(memberName);
            }
        }

        for (Element member : elements) {
            String memberName = getMemberName(member);
            if (memberName != null && member.getKind() == ElementKind.METHOD) {
                ExecutableElement executableElement = (ExecutableElement) member;
                TypeMirror memberType = executableElement.getReturnType();

                boolean embeddable = isEmbeddable(member);
                boolean isRelationship = relationships.contains(memberName);

                if (memberType instanceof PrimitiveType) {
                    memberType = processingEnv.getTypeUtils().boxedClass((PrimitiveType) memberType).asType();
                }

                TypeName pathImpl = ParameterizedTypeName.get(
                        ClassName.get(PrimitivePathSpec.class),
                        TypeName.get(memberType)
                );
                if (isRelationship || embeddable) {
                    pathImpl = getRelationshipType(memberType);
                }
                MethodSpec.Builder main = MethodSpec.methodBuilder(memberName)
                        .addModifiers(Modifier.PUBLIC)
                        .addStatement(String.format("PathSpec updatedPath = append(\"%s\")", memberName))
                        .addStatement(String.format("return boundSpec != null ? new %s(boundSpec) : new %s(updatedPath)", pathImpl.toString(), pathImpl.toString()))
                        .returns(pathImpl);
                typeBuilder.addMethod(main.build());
            }
        }

    }

    private boolean isRelationship(Element member) {
        for (AnnotationMirror mirror : member.getAnnotationMirrors()) {
            String name = mirror.getAnnotationType().toString();
            if (name.equals("jakarta.persistence.OneToMany") ||
                    name.equals("jakarta.persistence.ManyToOne") ||
                    name.equals("jakarta.persistence.OneToOne") ||
                    name.equals("jakarta.persistence.ManyToMany")) {
                return true;
            }
        }
        return member.getAnnotation(JsonApiRelation.class) != null;
    }

    private boolean isEmbeddable(Element member) {
        ExecutableElement executableElement = (ExecutableElement) member;
        TypeMirror returnType = executableElement.getReturnType();

        String propertyTypeName = returnType.toString();
        int sep = propertyTypeName.indexOf("<");
        if (sep != -1) {
            propertyTypeName = propertyTypeName.substring(sep + 1, propertyTypeName.length() - 1).trim();
        }

        TypeElement propertyType = processingEnv.getElementUtils().getTypeElement(propertyTypeName);
        if (propertyType == null) {
            //  processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING, "property type not found: " + propertyTypeName);
            return false;
        } else {
            JsonApiEmbeddable annotation = propertyType.getAnnotation(JsonApiEmbeddable.class);
            return annotation != null;
        }
    }

    private TypeName getRelationshipType(TypeMirror memberType) {
        String memberTypeStr = memberType.toString();
        int sep = memberTypeStr.indexOf("<");
        if (sep != -1) {
            memberTypeStr = memberTypeStr.substring(sep + 1, memberTypeStr.lastIndexOf(">"));
        }
        if (memberTypeStr.startsWith("()")) {
            memberTypeStr = memberTypeStr.substring(2);
        }
        memberTypeStr = getSimpleName(memberTypeStr, false);
        return ClassName.bestGuess(memberTypeStr);
    }

    private String getSimpleName(String name, boolean queryClass) {
        return normalizeName(name) + (queryClass ? QUERY_SUFFIX : PATH_SUFFIX);
    }

    private String normalizeName(String name) {
        if (name.endsWith("Entity")) {
            name = name.substring(0, name.length() - 6);
        }
        if (name.endsWith("Resource")) {
            name = name.substring(0, name.length() - 8);
        }
        return name;
    }

    private String getMemberName(Element member) {
        String memberName = member.getSimpleName().toString();
        if (member.getKind() == ElementKind.METHOD) {
            boolean isGetter = memberName.startsWith("get");
            boolean isBoolean = memberName.startsWith("is");
            if (isGetter || isBoolean) {
                String fieldName = memberName.substring(isGetter ? 3 : 2);
                return firstToLower(fieldName);
            }
            return null;
        } else if (member.getKind() == ElementKind.FIELD) {
            return memberName;
        }
        return null;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> set = new HashSet<>();
        set.add(JsonApiResource.class.getName());
        return set;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.RELEASE_8;
    }
}