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
import org.objectweb.asm.TypePath;
import org.objectweb.asm.TypeReference;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeParameterElement;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Similar to {@link org.objectweb.asm.ClassReader} but only for Mirror annotations. Used internally
 * by {@link MirrorClassReader} but available for reading individual annotations if needed.
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
        /**
         * Visits an annotation of the class.
         *
         * @param descriptor the class descriptor of the annotation class.
         * @param visible {@literal true} if the annotation is visible at runtime.
         * @return a visitor to visit the annotation values, or {@literal null} if this visitor is not
         *     interested in visiting this annotation.
         */
        AnnotationVisitor visit(String descriptor, boolean visible);
    }

    @FunctionalInterface
    public interface VisitAnnotationTypeProc {
        /**
         * Visits an annotation on a type in the method signature.
         *
         * @param typeRef a reference to the annotated type. The sort of this type reference must be
         *     {@link TypeReference#METHOD_TYPE_PARAMETER}, {@link
         *     TypeReference#METHOD_TYPE_PARAMETER_BOUND}, {@link TypeReference#METHOD_RETURN}, {@link
         *     TypeReference#METHOD_RECEIVER}, {@link TypeReference#METHOD_FORMAL_PARAMETER} or {@link
         *     TypeReference#THROWS}. See {@link TypeReference}.
         * @param typePath the path to the annotated type argument, wildcard bound, array element type, or
         *     static inner type within 'typeRef'. May be {@literal null} if the annotation targets
         *     'typeRef' as a whole.
         * @param descriptor the class descriptor of the annotation class.
         * @param visible {@literal true} if the annotation is visible at runtime.
         * @return a visitor to visit the annotation values, or {@literal null} if this visitor is not
         *     interested in visiting this annotation.
         */
        AnnotationVisitor visit(int typeRef, TypePath typePath, String descriptor, boolean visible);
    }

    @FunctionalInterface
    public interface VisitParameterAnnotationProc {
        /**
         * Visits an annotation of a parameter this method.
         *
         * @param parameter the parameter index. This index must be strictly smaller than the number of
         *     parameters in the method descriptor.
         * @param descriptor the class descriptor of the annotation class.
         * @param visible {@literal true} if the annotation is visible at runtime.
         * @return a visitor to visit the annotation values, or {@literal null} if this visitor is not
         *     interested in visiting this annotation.
         */
        AnnotationVisitor visit(int parameter, String descriptor, boolean visible);
    }

    /**
     * Return the {@link Target}s for the given annotation.
     *
     * @param annotation annotation
     * @return list of Targets or empty array
     */
    public ElementType[] getAnnotationTargets(AnnotationMirror annotation) {
        Element element = processingEnv.getTypeUtils().asElement(annotation.getAnnotationType());
        Target target = element.getAnnotation(Target.class);
        return (target != null) ? target.value() : new ElementType[0];
    }

    /**
     * Return true if the given annotation is "visible" i.e. it's retention is {@link RetentionPolicy#RUNTIME}.
     *
     * @param annotation annotation
     * @return true/false
     */
    public boolean isVisibleAnnotation(AnnotationMirror annotation) {
        Element element = processingEnv.getTypeUtils().asElement(annotation.getAnnotationType());
        Retention retention = element.getAnnotation(Retention.class);
        return (retention != null) && (retention.value() == RetentionPolicy.RUNTIME);
    }

    /**
     * Makes the given visitor visit any type annotations on any of the given elements
     *
     * @param elements elements to check
     * @param sortType the {@link TypeReference} value that corresponds to the location of the elements
     * @param visitAnnotationTypeProc visitor
     */
    public void readTypeAnnotations(List<? extends TypeParameterElement> elements, int sortType, VisitAnnotationTypeProc visitAnnotationTypeProc) {
        IntStream.range(0, elements.size()).forEach(index -> {
            TypeParameterElement element = elements.get(index);
            int typeRef = TypeReference.newTypeParameterReference(sortType, index).getValue();
            element.getAnnotationMirrors().forEach(annotation -> readAnnotationValue(annotation, (descriptor, visible) -> visitAnnotationTypeProc.visit(typeRef, null, descriptor, visible)));
        });
    }

    /**
     * Makes the given visitor visit the given type annotation
     *
     * @param annotation annotation to read
     * @param sortType the {@link TypeReference} value that corresponds to the location of the annotation
     * @param visitAnnotationTypeProc visitor
     */
    public void readAnnotationTypeValue(AnnotationMirror annotation, int sortType, VisitAnnotationTypeProc visitAnnotationTypeProc) {
        int typeRef = TypeReference.newTypeReference(sortType).getValue();
        readAnnotationValue(annotation, (descriptor, visible) -> visitAnnotationTypeProc.visit(typeRef, null, descriptor, visible));
    }

    /**
     * Makes the given visitor visit the given parameter annotation
     *
     * @param annotation annotation to read
     * @param parameter parameter's index
     * @param visitParameterAnnotationProc visitor
     */
    public void readParameterAnnotationValue(AnnotationMirror annotation, int parameter, VisitParameterAnnotationProc visitParameterAnnotationProc) {
        readAnnotationValue(annotation, (descriptor, visible) -> visitParameterAnnotationProc.visit(parameter, descriptor, visible));
    }

    /**
     * Makes the given visitor visit the given parameter type annotation
     *
     * @param annotation annotation to read
     * @param parameter parameter's index
     * @param sortType the {@link TypeReference} value that corresponds to the location of the parameter annotation
     * @param visitAnnotationTypeProc visitor
     */
    public void readParameterAnnotationTypeValue(AnnotationMirror annotation, int parameter, int sortType, VisitAnnotationTypeProc visitAnnotationTypeProc) {
        int typeRef = TypeReference.newTypeParameterReference(sortType, parameter).getValue();
        readAnnotationValue(annotation, (descriptor, visible) -> visitAnnotationTypeProc.visit(typeRef, null, descriptor, visible));
    }

    /**
     * Makes the given visitor visit the given annotation
     *
     * @param annotation annotation to read
     * @param visitAnnotationProc visitor
     */
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
