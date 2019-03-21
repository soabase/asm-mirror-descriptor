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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.TypeReference;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.ElementType;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Corollary to {@link org.objectweb.asm.ClassReader} but for {@link TypeMirror}s/{@link Element}s.
 * A parser to make a {@link ClassVisitor} visit a TypeMirror/Element instance. Calls the
 * appropriate visit methods of a given {@link ClassVisitor} for each field, method and field encountered.
 * Note: there is not bytecode in mirrors so those visitor methods are never called.
 */
public class MirrorMethodReader {
    private final MirrorSignatures mirrorSignatures;
    private final MirrorAnnotationReader annotationReader;

    public MirrorMethodReader(ProcessingEnvironment processingEnv) {
        this(new MirrorSignatures(processingEnv), new MirrorAnnotationReader(processingEnv));
    }

    public MirrorMethodReader(MirrorSignatures mirrorSignatures, MirrorAnnotationReader annotationReader) {
        this.mirrorSignatures = mirrorSignatures;
        this.annotationReader = annotationReader;
    }

    @FunctionalInterface
    public interface VisitMethod {
        MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions);
    }

    public void readMethod(ClassVisitor classVisitor, ExecutableElement method) {
        readMethod(classVisitor::visitMethod, method);
    }

    public void readMethod(VisitMethod visitMethod, ExecutableElement method) {
        int accessFlags = Util.modifiersToAccessFlags(method.getModifiers());
        boolean isConstructor = Util.isConstructor(method);
        String methodName = method.getSimpleName().toString();
        TypeMirror[] parameters = method.getParameters().stream()
                .map(Element::asType)
                .toArray(TypeMirror[]::new);
        TypeMirror[] typeParameters = method.getTypeParameters().stream()
                .map(Element::asType)
                .toArray(TypeMirror[]::new);
        String descriptor = mirrorSignatures.methodTypeDescriptor(typeParameters, parameters, method.getReturnType());
        String signature = Util.hasTypeArguments(method) ? mirrorSignatures.methodTypeSignature(typeParameters, parameters, method.getReturnType()) : null;
        String[] exceptions = readExceptions(method);
        MethodVisitor methodVisitor = visitMethod.visitMethod(accessFlags, methodName, descriptor, signature, exceptions);
        if (methodVisitor != null) {
            method.getAnnotationMirrors().forEach(annotation -> {
                annotationReader.readAnnotationValue(annotation, methodVisitor::visitAnnotation);
                if (isConstructor) {
                    // javac seems to infer this
                    if (Arrays.asList(annotationReader.getAnnotationTargets(annotation)).contains(ElementType.TYPE_USE)) {
                        annotationReader.readAnnotationTypeValue(annotation, TypeReference.METHOD_RETURN, methodVisitor::visitTypeAnnotation);
                    }
                }
            });
            method.getReturnType().getAnnotationMirrors().forEach(annotation -> annotationReader.readAnnotationTypeValue(annotation, TypeReference.METHOD_RETURN, methodVisitor::visitTypeAnnotation));
            annotationReader.readTypeAnnotations(method.getTypeParameters(), TypeReference.METHOD_TYPE_PARAMETER, methodVisitor::visitTypeAnnotation);

            IntStream.range(0, method.getParameters().size()).forEach(parameter -> {
                VariableElement parameterElement = method.getParameters().get(parameter);
                parameterElement.asType().getAnnotationMirrors().forEach(annotation -> annotationReader.readParameterAnnotationTypeValue(annotation, parameter, TypeReference.METHOD_FORMAL_PARAMETER, methodVisitor::visitTypeAnnotation));
                parameterElement.getAnnotationMirrors().forEach(annotation -> annotationReader.readParameterAnnotationValue(annotation, parameter, methodVisitor::visitParameterAnnotation));
            });
        }
    }

    private String[] readExceptions(ExecutableElement method) {
        if (method.getThrownTypes().isEmpty()) {
            return null;
        }
        return method.getThrownTypes().stream()
                .map(mirrorSignatures::exception)
                .toArray(String[]::new);
    }
}
