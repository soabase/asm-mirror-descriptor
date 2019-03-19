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
package io.soabase.asm.mirror.descriptor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypeReference;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static io.soabase.asm.mirror.descriptor.MirrorSignatureReader.Mode.DESCRIPTOR;
import static io.soabase.asm.mirror.descriptor.MirrorSignatureReader.Mode.SIGNATURE;

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

        mainElement.getAnnotationMirrors().forEach(annotation -> {
            String annotationDescriptor = signatureReader.type(annotation.getAnnotationType(), DESCRIPTOR);
            readAnnotationValue(annotation, classVisitor.visitAnnotation(annotationDescriptor, isVisibleAnnotation(annotation)));
        });

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

    public void readMethod(ClassVisitor classVisitor, ExecutableElement method) {
        int accessFlags = Util.modifiersToAccessFlags(method.getModifiers());
        String methodName = method.getSimpleName().toString();
        TypeMirror[] parameters = method.getParameters().stream()
                .map(this::unwrapType)
                .toArray(TypeMirror[]::new);
        TypeMirror[] typeParameters = method.getTypeParameters().stream()
                .map(this::unwrapType)
                .toArray(TypeMirror[]::new);
        String descriptor = signatureReader.methodType(typeParameters, parameters, unwrapType(method.getReturnType()), DESCRIPTOR);
        String signature = Util.hasTypeArguments(method) ? signatureReader.methodType(typeParameters, parameters, method.getReturnType(), SIGNATURE) : null;
        String[] exceptions = readExceptions(method);
        MethodVisitor methodVisitor = classVisitor.visitMethod(accessFlags, methodName, descriptor, signature, exceptions);
        if (methodVisitor != null) {
            method.getAnnotationMirrors().forEach(annotation -> {
                String annotationDescriptor = signatureReader.type(annotation.getAnnotationType(), DESCRIPTOR);
                AnnotationVisitor annotationVisitor = methodVisitor.visitAnnotation(annotationDescriptor, isVisibleAnnotation(annotation));
                readAnnotationValue(annotation, annotationVisitor);
            });
            method.asType().getAnnotationMirrors().forEach(annotation -> {
                String annotationDescriptor = signatureReader.type(annotation.getAnnotationType(), DESCRIPTOR);
                AnnotationVisitor annotationVisitor = methodVisitor.visitTypeAnnotation(TypeReference.newTypeReference(TypeReference.METHOD_FORMAL_PARAMETER).getValue(), null, annotationDescriptor, isVisibleAnnotation(annotation));
                readAnnotationValue(annotation, annotationVisitor);
            });
        }
    }

    public void readField(ClassVisitor classVisitor, VariableElement field) {
        int accessFlags = Util.modifiersToAccessFlags(field.getModifiers());
        String name = field.getSimpleName().toString();
        TypeMirror type = unwrapType(field);
        String descriptor = signatureReader.type(type, DESCRIPTOR);
        String signature = Util.hasTypeArguments(field) ? signatureReader.type(type, SIGNATURE) : null;
        Object constantValue = field.getConstantValue();
        FieldVisitor fieldVisitor = classVisitor.visitField(accessFlags, name, descriptor, signature, constantValue);
        if (fieldVisitor != null) {
            field.getAnnotationMirrors().forEach(annotation -> {
                String annotationDescriptor = signatureReader.type(annotation.getAnnotationType(), DESCRIPTOR);
                AnnotationVisitor annotationVisitor = fieldVisitor.visitAnnotation(annotationDescriptor, isVisibleAnnotation(annotation));
                readAnnotationValue(annotation, annotationVisitor);
            });
            field.asType().getAnnotationMirrors().forEach(annotation -> {
                String annotationDescriptor = signatureReader.type(annotation.getAnnotationType(), DESCRIPTOR);
                AnnotationVisitor annotationVisitor = fieldVisitor.visitTypeAnnotation(TypeReference.newTypeReference(TypeReference.FIELD).getValue(), null, annotationDescriptor, isVisibleAnnotation(annotation));
                readAnnotationValue(annotation, annotationVisitor);
            });
        }
    }

    private TypeMirror unwrapType(TypeMirror type) {
        if (type.getKind() == TypeKind.DECLARED) {
            return unwrapType(((DeclaredType) type).asElement());
        }
        return type;
    }

    private TypeMirror unwrapType(Element element) {
        TypeMirror type;
        if (element.getAnnotationMirrors().isEmpty()) {
            type = element.asType();
        } else {
            // for some reason, field.asType() is an internal "annotation type" when the field is annotated
            Element typeElement = processingEnv.getTypeUtils().asElement(element.asType());
            type = (typeElement != null) ? typeElement.asType() : element.asType();
        }
        return type;
    }

    private boolean isVisibleAnnotation(AnnotationMirror annotation) {
        Element element = processingEnv.getTypeUtils().asElement(annotation.getAnnotationType());
        Retention retention = element.getAnnotation(Retention.class);
        return (retention != null) && (retention.value() == RetentionPolicy.RUNTIME);
    }

    private void readAnnotationValue(AnnotationMirror annotation, AnnotationVisitor annotationVisitor) {
        if (annotationVisitor != null) {
            annotation.getElementValues().forEach((element, annotationValue) -> {
                AnnotationMirrorValueVisitor mirrorValueVisitor = new AnnotationMirrorValueVisitor(element.getSimpleName().toString(), signatureReader);
                annotationValue.accept(mirrorValueVisitor, annotationVisitor);
            });
            annotationVisitor.visitEnd();
        }
    }

    private String[] readExceptions(ExecutableElement method) {
        if (method.getThrownTypes().isEmpty()) {
            return null;
        }
        return method.getThrownTypes().stream()
                .map(signatureReader::exception)
                .toArray(String[]::new);
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
        return Util.toSlash(((TypeElement) element).getQualifiedName().toString());
    }
}
