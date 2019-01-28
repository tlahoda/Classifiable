/**Copyright 2019 Thomas Lahoda
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.appolition.classifiable_processor;

import com.appolition.classifiable_annotation.Classifiable;
import com.appolition.classifiable_processor.writers.JavaFileWriter;
import com.appolition.classifiable_processor.writers.ProcessingEnvironmentJavaFileWriter;
import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * An annotation processor for generating enums to use as classifiers
 */
@AutoService(Processor.class)
@SupportedAnnotationTypes({"com.appolition.classifiable_annotation.Classifiable"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class ClassifiableProcessor extends AbstractProcessor {
    /**
     * The suffix of the generated enum
     */
    private static final String SUFFIX = "Classifiers";

    /**
     * The ProcessingEnvironment, used the get the package name of an element
     */
    private ProcessingEnvironment processingEnvironment;

    /**
     * Writes message to the console
     */
    private Messager messager;

    /**
     * Writes a file
     */
    private Filer filer;

    private final JavaFileWriter javaFileWriter;

    public ClassifiableProcessor() {
        this.javaFileWriter = new ProcessingEnvironmentJavaFileWriter();
    }

    public ClassifiableProcessor(JavaFileWriter javaFileWriter) {
        this.javaFileWriter = javaFileWriter;
    }

    /**
     * Initializes the processor with the processing environment
     *
     * @param processingEnvironment, environment for facilities the tool framework provides to the processor
     */
    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);

        this.processingEnvironment = processingEnvironment;

        messager = processingEnvironment.getMessager();
        filer = processingEnvironment.getFiler();
    }

    /**
     * Processes a set of annotation types on type elements originating from the prior round
     *
     * @param annotations, the annotation types requested to be processed
     * @param roundEnvironment, environment for information about the current and prior round
     *
     * @return boolean, true if the annotation types are claimed and subsequent processors will not be asked to process them,
     *                  false if the annotation types are unclaimed and subsequent processors may be asked to process them
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnvironment) {
        Collection<? extends Element> annotatedElements = roundEnvironment.getElementsAnnotatedWith(Classifiable.class);

        if (!validateUsage(annotatedElements)) {
            return false;
        }

        generateCode(divideClasses(annotatedElements));

        return true;
    }

    /**
     * Ensures correct usage of the annotation
     *
     * @param annotatedElements, the collection of elements to check
     *
     * @return boolean, true if all elements in the list are valid
     */
    private boolean validateUsage(Collection<? extends Element> annotatedElements) {
        for (Element element : annotatedElements) {
            PackageElement packageElement = processingEnvironment.getElementUtils().getPackageOf(element);

            String packageName = packageElement.getQualifiedName().toString();

            String className = element.getEnclosingElement().getSimpleName().toString();

            String methodName = element.getSimpleName().toString();

            if (!element.getModifiers().contains(Modifier.PUBLIC)) {
                messager.printMessage(Diagnostic.Kind.ERROR,String.format("%s.%s.%s(): only public methods may be annotated with Classifiable", packageName, className, methodName));
                return false;
            }

            if (element.getModifiers().contains(Modifier.ABSTRACT)) {
                messager.printMessage(Diagnostic.Kind.ERROR,String.format("%s.%s.%s(): only non-abstract methods may be annotated with Classifiable", packageName, className, methodName));
                return false;
            }

            if (element.getModifiers().contains(Modifier.STATIC)) {
                messager.printMessage(Diagnostic.Kind.ERROR,String.format("%s.%s.%s(): only non-static methods may be annotated with Classifiable", packageName, className, methodName));
                return false;
            }
        }

        return true;
    }

    /**
     * Divides the provided collection of elements by class
     *
     * @param annotatedElements, the elements to divide by class
     *
     * @return Map<Pair, List<Element>>, the lists of elenets indexed by class
     */
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

    /**
     * Generates an enum for the specified elements
     *
     * @param elements, the elements for which to generate an enum
     */
    private void generateCode(Map<Pair, List<Element>> elements) {
        for (Map.Entry<Pair, List<Element>> entry : elements.entrySet()) {
            Element enclosing = entry.getKey().element.getEnclosingElement();

            String enumName = String.format("%s%s", enclosing.getSimpleName().toString(), SUFFIX);

            TypeSpec.Builder enumBuilder = TypeSpec.enumBuilder(enumName)
                    .addModifiers(Modifier.PUBLIC)
                    .addEnumConstant("_ALL");

            for (Element element : entry.getValue()) {
                String enumConstantName = prepareEnumConstantName(element.getSimpleName().toString());

                enumBuilder.addEnumConstant(enumConstantName);
            }

            PackageElement packageElement = processingEnvironment.getElementUtils().getPackageOf(enclosing);
            String packageName = packageElement.getQualifiedName().toString();

            try {
                javaFileWriter.writeTo(packageName, enumBuilder, filer);

            } catch (IOException excpt) {
                messager.printMessage(Diagnostic.Kind.ERROR, String .format("Unable to write %s.%s to a file", packageName, enumName));
                messager.printMessage(Diagnostic.Kind.ERROR, String.format("\t%s", excpt.getMessage()));

                return;
            }
        }
    }

    /**
     * Adjust the provided name for use as an enum constant
     *
     * @param name, the name to prepare
     *
     * @return String, the enum constant name
     */
    private String prepareEnumConstantName(String name) {
        return name.replaceAll("(.)(\\p{Upper})", "$1_$2").replace("get_", "").toUpperCase();
    }

    /**
     * The key type to use for the EnumMap
     */
    @VisibleForTesting
    public static final class Pair {
        /**
         * The element to process
         */
        final Element element;

        /**
         * The fully qualified name of the class containing element
         */
        final String qualifiedName;

        /**
         * Constructs a pair
         *
         * @param element, the element to process
         * @param qualifiedName, the fully qualified name of the class containing element
         */
        public Pair(Element element, String qualifiedName) {
            this.element = element;
            this.qualifiedName = qualifiedName;
        }

        /**
         * Calculates the hash code
         *
         * @return int, the hash code
         */
        @Override
        public int hashCode() {
            return qualifiedName.hashCode();
        }

        /**
         * Checks if two pairs are logically equal
         *
         * @param obj, the object for which to check equality
         *
         * @return boolean, true if the pairs are logically equal, false otherwise
         */
        @Override
        public boolean equals(Object obj) {
            return (obj instanceof Pair) && qualifiedName.equals(((Pair) obj).qualifiedName);
        }
    }
}
