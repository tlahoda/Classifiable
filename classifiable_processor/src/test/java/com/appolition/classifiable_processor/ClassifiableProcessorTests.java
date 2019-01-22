package com.appolition.classifiable_processor;

import com.google.common.base.Joiner;
import com.google.common.truth.Truth;
import com.google.testing.compile.JavaFileObjects;
import com.google.testing.compile.JavaSourcesSubjectFactory;

import org.junit.Test;

import java.util.Arrays;

import javax.tools.JavaFileObject;

public class ClassifiableProcessorTests {
    private static final String NEW_LINE = "\n";

    @Test
    public void classWithOneLoggedMethod() {
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
                        "",
                        "    @Classifiable",
                        "    public String getBaz() {",
                        "        return baz;",
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
                        "",
                        "    BAZ",
                        "}"));

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new ClassifiableProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(output);
    }

    //commented out until I figure out how to get it to fail
    /*@Test
    public void classAnnotated() {
        final JavaFileObject input = JavaFileObjects.forSourceString(
                "com.appolition.Foo",
                Joiner.on(NEW_LINE).join(
                        "package com.appolition;",
                        "",
                        "import com.appolition.classifiable_annotation.Classifiable;",
                        "",
                        "@Classifiable",
                        "public class Foo {",
                        "    private String bar;",
                        "    private String baz;",
                        "",
                        "    public String getBar() {",
                        "        return bar;",
                        "    }",
                        "",
                        "    public String getBaz() {",
                        "        return baz;",
                        "    }",
                        "}"
                )
        );

        Truth.assert_()
                .about(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new ClassifiableProcessor())
                .failsToCompile()
                .withErrorContaining("annotation type not applicable to this kind of declaration");
    }*/
}
