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
package io.soabase.asm.mirror;

import io.soabase.asm.mirror.util.AnnotationMirrorValueVisitor;
import io.soabase.asm.mirror.util.MirrorSignatures;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.TypeReference;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Corollary to {@link org.objectweb.asm.ClassReader} but for {@link TypeMirror}s/{@link Element}s.
 * A parser to make a {@link ClassVisitor} visit a TypeMirror/Element instance. Calls the
 * appropriate visit methods of a given {@link ClassVisitor} for each field, method and field encountered.
 * Note: there is not bytecode in mirrors so those visitor methods are never called.
 */
public class MirrorAnnotationReader {
    private final ProcessingEnvironment processingEnv;
    private final MirrorSignatures mirrorSignatures;

    public MirrorAnnotationReader(ProcessingEnvironment processingEnv) {
        this(processingEnv, new MirrorSignatures(processingEnv));
    }

    public MirrorAnnotationReader(ProcessingEnvironment processingEnv, MirrorSignatures mirrorSignatures) {
        this.processingEnv = processingEnv;
        this.mirrorSignatures = mirrorSignatures;
    }

    @FunctionalInterface
    public interface VisitAnnotationProc {
        AnnotationVisitor visit(String descriptor, boolean visible);
    }

    @FunctionalInterface
    public interface VisitAnnotationTypeProc {
        AnnotationVisitor visit(int typeRef, TypePath typePath, String descriptor, boolean visible);
    }

    @FunctionalInterface
    public interface VisitParameterAnnotationProc {
        AnnotationVisitor visit(int parameter, String descriptor, boolean visible);
    }

    public ElementType[] getAnnotationTargets(AnnotationMirror annotation) {
        Element element = processingEnv.getTypeUtils().asElement(annotation.getAnnotationType());
        Target target = element.getAnnotation(Target.class);
        return (target != null) ? target.value() : new ElementType[0];
    }

    public boolean isVisibleAnnotation(AnnotationMirror annotation) {
        Element element = processingEnv.getTypeUtils().asElement(annotation.getAnnotationType());
        Retention retention = element.getAnnotation(Retention.class);
        return (retention != null) && (retention.value() == RetentionPolicy.RUNTIME);
    }

    public void readTypeAnnotations(List<? extends TypeParameterElement> elements, int sortType, VisitAnnotationTypeProc visitAnnotationTypeProc) {
        IntStream.range(0, elements.size()).forEach(index -> {
            TypeParameterElement element = elements.get(index);
            int typeRef = TypeReference.newTypeParameterReference(sortType, index).getValue();
            element.getAnnotationMirrors().forEach(annotation -> readAnnotationValue(annotation, (descriptor, visible) -> visitAnnotationTypeProc.visit(typeRef, null, descriptor, visible)));
        });
    }

    public void readAnnotationTypeValue(AnnotationMirror annotation, int sortType, VisitAnnotationTypeProc visitAnnotationTypeProc) {
        int typeRef = TypeReference.newTypeReference(sortType).getValue();
        readAnnotationValue(annotation, (descriptor, visible) -> visitAnnotationTypeProc.visit(typeRef, null, descriptor, visible));
    }

    public void readParameterAnnotationValue(AnnotationMirror annotation, int parameter, VisitParameterAnnotationProc visitParameterAnnotationProc) {
        readAnnotationValue(annotation, (descriptor, visible) -> visitParameterAnnotationProc.visit(parameter, descriptor, visible));
    }

    public void readParameterAnnotationTypeValue(AnnotationMirror annotation, int parameter, int sortType, VisitAnnotationTypeProc visitAnnotationTypeProc) {
        int typeRef = TypeReference.newTypeParameterReference(sortType, parameter).getValue();
        readAnnotationValue(annotation, (descriptor, visible) -> visitAnnotationTypeProc.visit(typeRef, null, descriptor, visible));
    }

    public void readAnnotationValue(AnnotationMirror annotation, VisitAnnotationProc visitAnnotationProc) {
        String annotationDescriptor = mirrorSignatures.typeDescriptor(annotation.getAnnotationType());
        AnnotationVisitor annotationVisitor = visitAnnotationProc.visit(annotationDescriptor, isVisibleAnnotation(annotation));
        if (annotationVisitor != null) {
            annotation.getElementValues().forEach((element, annotationValue) -> {
                AnnotationMirrorValueVisitor mirrorValueVisitor = new AnnotationMirrorValueVisitor(element.getSimpleName().toString(), mirrorSignatures);
                annotationValue.accept(mirrorValueVisitor, annotationVisitor);
            });
            annotationVisitor.visitEnd();
        }
    }
}
