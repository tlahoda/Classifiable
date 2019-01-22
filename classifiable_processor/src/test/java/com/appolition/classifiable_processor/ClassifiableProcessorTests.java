package com.appolition.classifiable_processor;

import com.google.common.base.Joiner;
import com.google.common.truth.Truth;
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourcesSubjectFactory;

import org.junit.Test;

import java.util.Arrays;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class ClassifiableProcessorTests {
    private static final String NEW_LINE = "\n";

    @Test
    public void methodsAnnotated() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "com.appolition.Foo",
                Joiner.on(NEW_LINE).join(
                        "package com.appolition;",
                        "",
                        "import com.appolition.classifiable_annotation.Classifiable;",
                        "",
                        "public class Foo {",
                        "    private String bar;",
                        "    private String baz;",
                        "",
                        "    @Classifiable",
                        "    public String getBar() {",
                        "        return bar;",
                        "    }",
                       "}"));

        final JavaFileObject output = JavaFileObjects.forSourceString(
                "com.appolition.FooClassifiers",
                Joiner.on(NEW_LINE).join(
                        "package com.appolition;",
                        "",
                        "public enum FooClassifiers {",
                        "    _ALL,",
                        "",
                        "    BAR,",
                        "}"));

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new ClassifiableProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(output);
    }

    @Test
    public void privateMethodAnnotated() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "com.appolition.Foo",
                Joiner.on(NEW_LINE).join(
                        "package com.appolition;",
                        "",
                        "import com.appolition.classifiable_annotation.Classifiable;",
                        "",
                        "public class Foo {",
                        "    private String bar;",
                        "    private String baz;",
                        "",
                        "    @Classifiable",
                        "    private String getBar() {",
                        "        return bar;",
                        "    }",
                        "}"));

        Compilation compilation = javac()
                .withProcessors(new ClassifiableProcessor())
                .compile(input);

        assertThat(compilation).failed();

        assertThat(compilation)
                .hadErrorContaining("only public methods may be annotated with Classifiable");
    }

    @Test
    public void abstractMethodAnnotated() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "com.appolition.Foo",
                Joiner.on(NEW_LINE).join(
                        "package com.appolition;",
                        "",
                        "import com.appolition.classifiable_annotation.Classifiable;",
                        "",
                        "public class Foo {",
                        "    private String bar;",
                        "    private String baz;",
                        "",
                        "    @Classifiable",
                        "    public abstract String getBar() {",
                        "        return bar;",
                        "    }",
                        "}"));

        Compilation compilation = javac()
                .withProcessors(new ClassifiableProcessor())
                .compile(input);

        assertThat(compilation).failed();

        assertThat(compilation)
                .hadErrorContaining("only non-abstract methods may be annotated with Classifiable");
    }

    @Test
    public void staticMethodAnnotated() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "com.appolition.Foo",
                Joiner.on(NEW_LINE).join(
                        "package com.appolition;",
                        "",
                        "import com.appolition.classifiable_annotation.Classifiable;",
                        "",
                        "public class Foo {",
                        "    private String bar;",
                        "    private String baz;",
                        "",
                        "    @Classifiable",
                        "    public static String getBar() {",
                        "        return bar;",
                        "    }",
                        "}"));

        Compilation compilation = javac()
                .withProcessors(new ClassifiableProcessor())
                .compile(input);

        assertThat(compilation).failed();

        assertThat(compilation)
                .hadErrorContaining("only non-static methods may be annotated with Classifiable");
    }
}
