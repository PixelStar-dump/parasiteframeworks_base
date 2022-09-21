/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.processor

import android.processor.immutability.IMMUTABLE_ANNOTATION_NAME
import android.processor.immutability.ImmutabilityProcessor
import android.processor.immutability.MessageUtils
import com.google.common.truth.Expect
import com.google.testing.compile.CompilationSubject.assertThat
import com.google.testing.compile.Compiler.javac
import com.google.testing.compile.JavaFileObjects
import org.junit.Rule
import org.junit.Test
import javax.tools.JavaFileObject

class ImmutabilityProcessorTest {

    companion object {
        private const val PACKAGE_PREFIX = "android.processor.immutability"
        private const val DATA_CLASS_NAME = "DataClass"
        private val ANNOTATION = JavaFileObjects.forSourceString(IMMUTABLE_ANNOTATION_NAME,
            /* language=JAVA */ """
                package $PACKAGE_PREFIX;

                import java.lang.annotation.Retention;
                import java.lang.annotation.RetentionPolicy;

                @Retention(RetentionPolicy.SOURCE)
                public @interface Immutable {
                    @Retention(RetentionPolicy.SOURCE)
                    @interface Ignore {}
                }
            """.trimIndent()
        )
    }

    @get:Rule
    val expect = Expect.create()

    @Test
    fun validInterface() = test(
        JavaFileObjects.forSourceString("$PACKAGE_PREFIX.$DATA_CLASS_NAME",
            /* language=JAVA */ """
                package $PACKAGE_PREFIX;

                import $IMMUTABLE_ANNOTATION_NAME;
                import java.util.ArrayList;
                import java.util.Collections;
                import java.util.List;

                @Immutable
                public interface $DATA_CLASS_NAME {
                    InnerInterface DEFAULT = new InnerInterface() {
                        @Override
                        public String getValue() {
                            return "";
                        }
                        @Override
                        public List<String> getArray() {
                            return Collections.emptyList();
                        }
                    };

                    String getValue();
                    ArrayList<String> getArray();
                    InnerInterface getInnerInterface();

                    @Immutable
                    interface InnerInterface {
                        String getValue();
                        List<String> getArray();
                    }
                }
                """.trimIndent()
        ), errors = emptyList())

    @Test
    fun abstractClass() = test(
        JavaFileObjects.forSourceString("$PACKAGE_PREFIX.$DATA_CLASS_NAME",
            /* language=JAVA */ """
                package $PACKAGE_PREFIX;

                import $IMMUTABLE_ANNOTATION_NAME;
                import java.util.Map;

                @Immutable
                public abstract class $DATA_CLASS_NAME {
                    public static final String IMMUTABLE = "";
                    public static final InnerClass NOT_IMMUTABLE = null;
                    public static InnerClass NOT_FINAL = null;

                    // Field finality doesn't matter, methods are always enforced so that future
                    // field compaction or deprecation is possible
                    private final String fieldFinal = "";
                    private String fieldNonFinal;
                    public abstract void sideEffect();
                    public abstract String[] getArray();
                    public abstract InnerClass getInnerClassOne();
                    public abstract InnerClass getInnerClassTwo();
                    @Immutable.Ignore
                    public abstract InnerClass getIgnored();
                    public abstract InnerInterface getInnerInterface();

                    public abstract Map<String, String> getValidMap();
                    public abstract Map<InnerClass, InnerClass> getInvalidMap();

                    public static final class InnerClass {
                        public String innerField;
                        public String[] getArray() { return null; }
                    }

                    public interface InnerInterface {
                        String[] getArray();
                        InnerClass getInnerClass();
                    }
                }
                """.trimIndent()
        ), errors = listOf(
            nonInterfaceClassFailure(line = 7),
            nonInterfaceReturnFailure(line = 9),
            staticNonFinalFailure(line = 10),
            nonInterfaceReturnFailure(line = 10),
            memberNotMethodFailure(line = 14),
            memberNotMethodFailure(line = 15),
            voidReturnFailure(line = 16),
            arrayFailure(line = 17),
            nonInterfaceReturnFailure(line = 18),
            nonInterfaceReturnFailure(line = 19),
            nonInterfaceReturnFailure(line = 25,  prefix = "Key InnerClass"),
            nonInterfaceReturnFailure(line = 25,  prefix = "Value InnerClass"),
            classNotImmutableFailure(line = 27, className = "InnerClass"),
            nonInterfaceClassFailure(line = 27),
            memberNotMethodFailure(line = 28),
            arrayFailure(line = 29),
            classNotImmutableFailure(line = 22, className = "InnerInterface"),
            arrayFailure(line = 33),
            nonInterfaceReturnFailure(line = 34),
        ))

    private fun test(source: JavaFileObject, errors: List<CompilationError>) {
        val compilation = javac()
            .withProcessors(ImmutabilityProcessor())
            .compile(listOf(source) + ANNOTATION)
        errors.forEach {
            try {
                assertThat(compilation)
                    .hadErrorContaining(it.message)
                    .inFile(source)
                    .onLine(it.line)
            } catch (e: AssertionError) {
                // Wrap the exception so that the line number is logged
                val wrapped = AssertionError("Expected $it, ${e.message}").apply {
                    stackTrace = e.stackTrace
                }

                // Wrap again with Expect so that all errors are reported. This is very bad code
                // but can only be fixed by updating compile-testing with a better Truth Subject
                // implementation.
                expect.that(wrapped).isNull()
            }
        }

        try {
            assertThat(compilation).hadErrorCount(errors.size)
        } catch (e: AssertionError) {
            if (expect.hasFailures()) {
                expect.that(e).isNull()
            } else throw e
        }
    }

    private fun classNotImmutableFailure(line: Long, className: String) =
        CompilationError(line = line, message = MessageUtils.classNotImmutableFailure(className))

    private fun nonInterfaceClassFailure(line: Long) =
        CompilationError(line = line, message = MessageUtils.nonInterfaceClassFailure())

    private fun nonInterfaceReturnFailure(line: Long) =
        CompilationError(line = line, message = MessageUtils.nonInterfaceReturnFailure())

    private fun nonInterfaceReturnFailure(line: Long, prefix: String, index: Int = -1) =
        CompilationError(
            line = line,
            message = MessageUtils.nonInterfaceReturnFailure(prefix = prefix, index = index)
        )

    private fun memberNotMethodFailure(line: Long) =
        CompilationError(line = line, message = MessageUtils.memberNotMethodFailure())

    private fun voidReturnFailure(line: Long) =
        CompilationError(line = line, message = MessageUtils.voidReturnFailure())

    private fun staticNonFinalFailure(line: Long) =
        CompilationError(line = line, message = MessageUtils.staticNonFinalFailure())

    private fun arrayFailure(line: Long) =
        CompilationError(line = line, message = MessageUtils.arrayFailure())

    data class CompilationError(
        val line: Long,
        val message: String,
    )
}