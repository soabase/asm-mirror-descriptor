/**
 * Copyright 2019 Jordan Zimmerman
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.soabase.asm.mirror.descriptor;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import static io.soabase.asm.mirror.descriptor.MirrorSignatureReader.Mode.*;

public class ClassMirrorReader {
    private final ProcessingEnvironment processingEnv;
    private final TypeElement mainElement;
    private final MirrorSignatureReader signatureReader;
    private final int classVersion;
    private final int extraAccessFlags;

    public ClassMirrorReader(ProcessingEnvironment processingEnv, DeclaredType mainElement) {
        this(processingEnv, (TypeElement) mainElement.asElement());
    }

    public ClassMirrorReader(ProcessingEnvironment processingEnv, TypeElement mainElement) {
        this(processingEnv, mainElement, Opcodes.V1_8, Opcodes.ACC_SUPER);
    }

    public ClassMirrorReader(ProcessingEnvironment processingEnv, TypeElement mainElement, int classVersion, int extraAccessFlags) {
        this.processingEnv = processingEnv;
        this.mainElement = mainElement;
        signatureReader = new MirrorSignatureReader(processingEnv);
        this.classVersion = classVersion;
        this.extraAccessFlags = extraAccessFlags;
    }

    public void accept(ClassVisitor classVisitor) {
        int accessFlags = Util.modifiersToAccessFlags(mainElement.getModifiers()) | extraAccessFlags;
        String thisClass = Util.toSlash(mainElement.getQualifiedName().toString());
        String superClass = getSuperClass();
        String[] interfaces = getInterfaces();
        String signature = Util.hasTypeArguments(mainElement) ? signatureReader.classSignature(mainElement.asType()) : null;
        classVisitor.visit(classVersion, accessFlags, thisClass, signature, superClass, interfaces);

        mainElement.getEnclosedElements().forEach(enclosed -> {
            switch (enclosed.getKind()) {
                case FIELD: {
                    readField(classVisitor, (VariableElement) enclosed);
                    break;
                }

                case CONSTRUCTOR:
                case METHOD: {
                    readMethod(classVisitor, (ExecutableElement) enclosed);
                    break;
                }
            }
        });

        classVisitor.visitEnd();    // TODO
    }

    private void readMethod(ClassVisitor classVisitor, ExecutableElement method) {
        int accessFlags = Util.modifiersToAccessFlags(method.getModifiers());
        String methodName = method.getSimpleName().toString();
        TypeMirror[] parameters = method.getParameters().stream()
                .map(VariableElement::asType)
                .toArray(TypeMirror[]::new);
        TypeMirror[] typeParameters = method.getTypeParameters().stream()
                .map(TypeParameterElement::asType)
                .toArray(TypeMirror[]::new);
        String descriptor = signatureReader.methodType(typeParameters, parameters, method.getReturnType(), DESCRIPTOR);
        String signature = Util.hasTypeArguments(method) ? signatureReader.methodType(typeParameters, parameters, method.getReturnType(), SIGNATURE) : null;
        String[] exceptions = readExceptions(method);
        MethodVisitor methodVisitor = classVisitor.visitMethod(accessFlags, methodName, descriptor, signature, exceptions);
    }

    private String[] readExceptions(ExecutableElement method) {
        return method.getThrownTypes().stream()
                .map(exceptionType -> signatureReader.type(exceptionType, DESCRIPTOR_UNTERMINATED))
                .toArray(String[]::new);
    }

    private void readField(ClassVisitor classVisitor, VariableElement field) {
        int accessFlags = Util.modifiersToAccessFlags(field.getModifiers());
        String name = field.getSimpleName().toString();
        String descriptor = signatureReader.type(field.asType(), DESCRIPTOR);
        String signature = Util.hasTypeArguments(field) ? signatureReader.type(field.asType(), SIGNATURE) : null;
        Object constantValue = field.getConstantValue();
        FieldVisitor fieldVisitor = classVisitor.visitField(accessFlags, name, descriptor, signature, constantValue);
    }

    private String[] getInterfaces() {
        return mainElement.getInterfaces().stream()
                .map(type -> {
                    Element element = ((DeclaredType) type).asElement();
                    return Util.toSlash(((TypeElement) element).getQualifiedName().toString());
                })
                .toArray(String[]::new);
    }

    private String getSuperClass() {
        Element element = ((DeclaredType) mainElement.getSuperclass()).asElement();
        if (Util.isObject(processingEnv, element)) {
            return null;
        }
        return Util.toSlash(((TypeElement) element).getQualifiedName().toString());
    }
}
