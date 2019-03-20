/**
 * Copyright 2019 Jordan Zimmerman
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.soabase.asm.mirror.test.processor;

import io.soabase.asm.mirror.MirrorClassReader;
import io.soabase.asm.mirror.test.processor.visitor.TestClassVisitor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

@SupportedAnnotationTypes("io.soabase.asm.mirror.test.processor.DescriptorTest")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class TestProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment environment) {
        annotations.forEach(annotation -> {
            Set<? extends Element> elementsAnnotatedWith = environment.getElementsAnnotatedWith(annotation);
            elementsAnnotatedWith.forEach(element -> {
                if (element.getKind() == ElementKind.CLASS) {
                    TypeElement typeElement = (TypeElement) element;
                    MirrorClassReader reader = new MirrorClassReader(processingEnv, typeElement);
                    TestClassVisitor testClassVisitor = new TestClassVisitor();
                    reader.accept(testClassVisitor);

                    try (Writer out = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "test", typeElement.getSimpleName().toString() + ".txt").openWriter()) {
                        out.write(testClassVisitor.toString());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        });
        return true;
    }
}
