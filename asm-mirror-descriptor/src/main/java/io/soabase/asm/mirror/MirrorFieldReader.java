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
import org.objectweb.asm.TypeReference;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * Corollary to {@link org.objectweb.asm.ClassReader} but for {@link TypeMirror}s/{@link Element}s.
 * A parser to make a {@link ClassVisitor} visit a TypeMirror/Element instance. Calls the
 * appropriate visit methods of a given {@link ClassVisitor} for each field, method and field encountered.
 * Note: there is not bytecode in mirrors so those visitor methods are never called.
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
    public interface VisitField {
        FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value);
    }

    public void readField(ClassVisitor classVisitor, VariableElement field) {
        readField(classVisitor::visitField, field);
    }

    public void readField(VisitField visitField, VariableElement field) {
        int accessFlags = Util.modifiersToAccessFlags(field.getModifiers());
        String name = field.getSimpleName().toString();
        TypeMirror type = field.asType();
        String descriptor = mirrorSignatures.typeDescriptor(type);
        String signature = Util.hasTypeArguments(field) ? mirrorSignatures.typeSignature(type) : null;
        Object constantValue = field.getConstantValue();
        FieldVisitor fieldVisitor = visitField.visitField(accessFlags, name, descriptor, signature, constantValue);
        if (fieldVisitor != null) {
            field.getAnnotationMirrors().forEach(annotation -> annotationReader.readAnnotationValue(annotation, fieldVisitor::visitAnnotation));
            field.asType().getAnnotationMirrors().forEach(annotation -> annotationReader.readAnnotationTypeValue(annotation, TypeReference.FIELD, fieldVisitor::visitTypeAnnotation));
        }
    }
}
