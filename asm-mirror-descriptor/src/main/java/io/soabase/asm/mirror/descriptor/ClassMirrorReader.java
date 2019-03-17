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

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

public class ClassMirrorReader {
    private final ProcessingEnvironment processingEnv;
    private final TypeElement type;
    private final MirrorSignatureReader signatureReader;

    public ClassMirrorReader(ProcessingEnvironment processingEnv, DeclaredType type) {
        this(processingEnv, (TypeElement) type.asElement());
    }

    public ClassMirrorReader(ProcessingEnvironment processingEnv, TypeElement type) {
        this.processingEnv = processingEnv;
        this.type = type;
        signatureReader = new MirrorSignatureReader(processingEnv);
    }

    public void accept(ClassVisitor classVisitor) {
        int accessFlags = Util.modifiersToAccessFlags(type.getModifiers());
        String thisClass = Util.toSlash(type.getQualifiedName().toString());
        String superClass = getSuperClass();
        String[] interfaces = getInterfaces();
        String signature = null;    // TODO
        classVisitor.visit(0, accessFlags, thisClass, signature, superClass, interfaces);

        type.getEnclosedElements().forEach(enclosed -> {
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
        String descriptor = signatureReader.methodType(parameters, method.getReturnType(), MirrorSignatureReader.Mode.DESCRIPTOR);
        String signature = null;    // TODO
        String[] exceptions = null;  // TODO
        MethodVisitor methodVisitor = classVisitor.visitMethod(accessFlags, methodName, descriptor, signature, exceptions);
    }

    private void readField(ClassVisitor classVisitor, VariableElement field) {
        int accessFlags = Util.modifiersToAccessFlags(field.getModifiers());
        String name = field.getSimpleName().toString();
        String descriptor = signatureReader.type(field.asType(), MirrorSignatureReader.Mode.DESCRIPTOR);
        String signature = null;    // TODO
        Object constantValue = field.getConstantValue();
        FieldVisitor fieldVisitor = classVisitor.visitField(accessFlags, name, descriptor, signature, constantValue);
    }

    private String[] getInterfaces() {
        return type.getInterfaces().stream()
                .map(type -> {
                    Element element = ((DeclaredType) type).asElement();
                    return Util.toSlash(((TypeElement) element).getQualifiedName().toString());
                })
                .toArray(String[]::new);
    }

    private String getSuperClass() {
        Element element = ((DeclaredType) type.getSuperclass()).asElement();
        if (element.equals(processingEnv.getElementUtils().getTypeElement("java.lang.Object"))) {
            return null;
        }
        return Util.toSlash(((TypeElement) element).getQualifiedName().toString());
    }
}
