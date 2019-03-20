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

import io.soabase.asm.mirror.MirrorType;
import io.soabase.asm.mirror.test.processor.visitor.TypeVisitor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.stream.Collectors;

@SupportedAnnotationTypes("io.soabase.asm.mirror.test.processor.MirrorTypeTest")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class TestMirrorTypeProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment environment) {
        annotations.forEach(annotation -> {
            Set<? extends Element> elementsAnnotatedWith = environment.getElementsAnnotatedWith(annotation);
            elementsAnnotatedWith.forEach(element -> {
                if (element.getKind() == ElementKind.CLASS) {
                    TypeVisitor typeVisitor = new TypeVisitor();

                    typeVisitor.accept("== 01 class ==", MirrorType.getType(processingEnv, element.asType()));
                    typeVisitor.accept("== 02 ctors ==", element.getEnclosedElements().stream()
                            .filter(enclosed -> enclosed.getKind() == ElementKind.CONSTRUCTOR)
                            .map(enclosed -> MirrorType.getType(processingEnv, enclosed))
                            .collect(Collectors.toList()));
                    typeVisitor.accept("== 03 methods ==", element.getEnclosedElements().stream()
                            .filter(enclosed -> enclosed.getKind() == ElementKind.METHOD)
                            .map(enclosed -> MirrorType.getType(processingEnv, enclosed))
                            .collect(Collectors.toList()));
                    typeVisitor.accept("== 04 returns ==", element.getEnclosedElements().stream()
                            .filter(enclosed -> enclosed.getKind() == ElementKind.METHOD)
                            .map(enclosed -> MirrorType.getReturnType(processingEnv, (ExecutableElement) enclosed))
                            .collect(Collectors.toList()));

                    try (Writer out = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "mirror", element.getSimpleName().toString() + ".txt").openWriter()) {
                        out.write(typeVisitor.toString().trim());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        });
        return true;
    }
}
