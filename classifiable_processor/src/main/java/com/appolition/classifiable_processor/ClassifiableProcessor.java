package com.appolition.classifiable_processor;

import com.appolition.classifiable_annotation.Classifiable;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@AutoService(Processor.class)
public class ClassifiableProcessor extends AbstractProcessor {
    private static final String SUFFIX = "Classifiers";

    private ProcessingEnvironment processingEnvironment;

    private Messager messager;
    private Filer filer;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        this.processingEnvironment = processingEnvironment;

        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        Collection<? extends Element> annotatedElements = roundEnvironment.getElementsAnnotatedWith(Classifiable.class);

        if (!validateUsage(annotatedElements)) {
            return false;
        }

        return generateCode(divideClasses(annotatedElements));
    }

    private boolean validateUsage(Collection<? extends Element> annotatedElements) {
        for (Element element : annotatedElements) {
            PackageElement packageElement = processingEnvironment.getElementUtils().getPackageOf(element);

            String packageName = packageElement.getQualifiedName().toString();

            String className = element.getEnclosingElement().getSimpleName().toString();

            String methodName = element.getSimpleName().toString();

            //TODO: expand error handling output as check for class field or parameter
            if (element.getKind() != ElementKind.METHOD) {
                messager.printMessage(Diagnostic.Kind.ERROR, String .format("%s.%s: only methods may be annotated with Classifiable", packageName, element.getSimpleName().toString()));
                return false;
            }

            if (!element.getModifiers().contains(Modifier.PUBLIC)){
                messager.printMessage(Diagnostic.Kind.ERROR,String.format("%s.%s.%s(): only public methods may be annotated with Classifiable", packageName, className, methodName));
                return false;
            }

            if (element.getModifiers().contains(Modifier.ABSTRACT)){
                messager.printMessage(Diagnostic.Kind.ERROR,String.format("%s.%s.%s(): only non abstract methods may be annotated with Classifiable", packageName, className, methodName));
                return false;
            }
        }

        return true;
    }

    private Map<Pair, List<Element>> divideClasses(Collection<? extends Element> annotatedElements) {
        Map<Pair, List<Element>> elements = new HashMap<>();

        for (Element element : annotatedElements) {
            PackageElement packageElement = processingEnvironment.getElementUtils().getPackageOf(element);

            String packageName = packageElement.getQualifiedName().toString();

            String className = element.getEnclosingElement().getSimpleName().toString();

            String qualifiedClassName = String.format("%s.%s", packageName, className);

            Pair pair = new Pair(element, qualifiedClassName);

            List<Element> classElements = elements.get(pair);

            if (classElements == null) {
                classElements = new ArrayList<>();

                elements.put(pair, classElements);
            }

            classElements.add(element);
        }

       return elements;
    }

    private boolean generateCode(Map<Pair, List<Element>> elements) {
        for (Map.Entry<Pair, List<Element>> entry : elements.entrySet()) {
            Element enclosing = entry.getKey().element.getEnclosingElement();

            String className = String.format("%s%s", enclosing.getSimpleName().toString(), SUFFIX);

            TypeSpec.Builder classBuilder = TypeSpec.classBuilder(className)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                    .addMethod(MethodSpec.constructorBuilder().addModifiers(Modifier.PRIVATE).build());

            int i = 0;

            classBuilder.addField(FieldSpec.builder(int.class, "_all", Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$L", i)
                    .build());

            for (Element element : entry.getValue()) {
                String fieldName = element.getSimpleName().toString().replaceAll("(.)(\\p{Upper})", "$1_$2").toLowerCase().replace("get_", "");

                classBuilder.addField(FieldSpec.builder(int.class, fieldName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
                        .initializer("$L", ++i)
                        .build());
            }

            PackageElement packageElement = processingEnvironment.getElementUtils().getPackageOf(enclosing);
            String packageName = packageElement.getQualifiedName().toString();

            try {
                JavaFile.builder(packageName, classBuilder.build())
                        .indent("    ")
                        .build().writeTo(filer);

            } catch (IOException excpt) {
                messager.printMessage(Diagnostic.Kind.ERROR, String .format("Unable to write %s.%s to a file", packageName, className));
                messager.printMessage(Diagnostic.Kind.ERROR, String.format("\t%s", excpt.getMessage()));

                return false;
            }
        }

        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedAnnotations = new HashSet<>();

        supportedAnnotations.add(Classifiable.class.getCanonicalName());

        return supportedAnnotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private static final class Pair {
        Element element;
        String qualifiedName;

        public Pair(Element element, String qualifiedName) {
            this.element = element;
            this.qualifiedName = qualifiedName;
        }

        @Override
        public int hashCode() {
            return qualifiedName.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Pair)) {
                return false;
            }

            return qualifiedName.equals(((Pair) obj).qualifiedName);
        }

        @Override
        public String toString() {
            return qualifiedName;
        }
    }
}
