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

import com.appolition.classifiable_processor.writers.IOExceptionThrowingJavaFileWriter;
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ClassifiableProcessorTests {
    private static final String NEW_LINE = "\n";

    @Test
    public void pairsEqual() {
        ClassifiableProcessor.Pair pair1 = new ClassifiableProcessor.Pair(null, "foo");
        ClassifiableProcessor.Pair pair2 = new ClassifiableProcessor.Pair(null, "foo");

        assertEquals("Pairs not equal", pair1, pair2);
    }

    @Test
    public void pairsNotEqual() {
        ClassifiableProcessor.Pair pair1 = new ClassifiableProcessor.Pair(null, "foo");
        ClassifiableProcessor.Pair pair2 = new ClassifiableProcessor.Pair(null, "bar");

        assertNotEquals("Pairs equal", pair1, pair2);
    }

    @Test
    public void pairAndStringNotEqual() {
        ClassifiableProcessor.Pair pair1 = new ClassifiableProcessor.Pair(null, "foo");

        assertNotEquals("Pairs equal", "bar", pair1);
    }

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
    public void failingJavaFileWriter() {
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

        Truth.assertAbout(JavaSourcesSubjectFactory.javaSources())
                .that(Arrays.asList(input))
                .processedWith(new ClassifiableProcessor(new IOExceptionThrowingJavaFileWriter()))
                .failsToCompile();
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
