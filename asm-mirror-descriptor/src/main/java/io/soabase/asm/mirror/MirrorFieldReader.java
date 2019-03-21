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

import io.soabase.asm.mirror.util.MirrorSignatures;
import io.soabase.asm.mirror.util.Util;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypeReference;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Similar to {@link org.objectweb.asm.ClassReader} but only for Mirror fields. Used internally
 * by {@link MirrorClassReader} but available for reading individual fields if needed.
 */
public class MirrorFieldReader {
    private final MirrorSignatures mirrorSignatures;
    private final MirrorAnnotationReader annotationReader;

    public MirrorFieldReader(ProcessingEnvironment processingEnv) {
        this(new MirrorSignatures(processingEnv), new MirrorAnnotationReader(processingEnv));
    }

    public MirrorFieldReader(MirrorSignatures mirrorSignatures, MirrorAnnotationReader annotationReader) {
        this.mirrorSignatures = mirrorSignatures;
        this.annotationReader = annotationReader;
    }

    @FunctionalInterface
    public interface VisitFieldProc {
        /**
         * Visits a field of the class.
         *
         * @param access the field's access flags (see {@link Opcodes}). This parameter also indicates if
         *     the field is synthetic and/or deprecated.
         * @param name the field's name.
         * @param descriptor the field's descriptor (see {@link Type}).
         * @param signature the field's signature. May be {@literal null} if the field's type does not use
         *     generic types.
         * @param value the field's initial value. This parameter, which may be {@literal null} if the
         *     field does not have an initial value, must be an {@link Integer}, a {@link Float}, a {@link
         *     Long}, a {@link Double} or a {@link String} (for {@code int}, {@code float}, {@code long}
         *     or {@code String} fields respectively). <i>This parameter is only used for static
         *     fields</i>. Its value is ignored for non static fields, which must be initialized through
         *     bytecode instructions in constructors or methods.
         * @return a visitor to visit field annotations and attributes, or {@literal null} if this class
         *     visitor is not interested in visiting these annotations and attributes.
         */
        FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value);
    }

    /**
     * Makes the given visitor visit the given field.
     *
     * @param classVisitor the visitor that must visit this field. Only {@link ClassVisitor#visitField(int, String, String, String, Object)}
     *                     will be called
     * @param field the field
     */
    public void readField(ClassVisitor classVisitor, VariableElement field) {
        readField(classVisitor::visitField, field);
    }

    /**
     * Makes the given visitor visit the given field.
     *
     * @param visitFieldProc visit field proc
     * @param field the field
     */
    public void readField(VisitFieldProc visitFieldProc, VariableElement field) {
        int accessFlags = Util.modifiersToAccessFlags(field.getModifiers());
        String name = field.getSimpleName().toString();
        TypeMirror type = field.asType();
        String descriptor = mirrorSignatures.typeDescriptor(type);
        String signature = Util.hasTypeArguments(field) ? mirrorSignatures.typeSignature(type) : null;
        Object constantValue = field.getConstantValue();
        FieldVisitor fieldVisitor = visitFieldProc.visitField(accessFlags, name, descriptor, signature, constantValue);
        if (fieldVisitor != null) {
            field.getAnnotationMirrors().forEach(annotation -> annotationReader.readAnnotationValue(annotation, fieldVisitor::visitAnnotation));
            field.asType().getAnnotationMirrors().forEach(annotation -> annotationReader.readAnnotationTypeValue(annotation, TypeReference.FIELD, fieldVisitor::visitTypeAnnotation));
        }
    }
}
