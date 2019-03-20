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
package io.soabase.asm.mirror.util;

import org.objectweb.asm.signature.SignatureWriter;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.type.WildcardType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static io.soabase.asm.mirror.util.MirrorSignatures.Mode.*;

public class MirrorSignatures {
    private final ProcessingEnvironment processingEnv;

    protected enum Mode {
        DESCRIPTOR_EXCEPTION,
        DESCRIPTOR,
        SIGNATURE_WITH_TYPE_BOUNDS,
        SIGNATURE,
        SIGNATURE_SIMPLE
    }

    public MirrorSignatures(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    public String classSignature(TypeMirror type) {
        type = unwrapType(type);

        if (type.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) type;
            TypeElement typeElement = (TypeElement) declaredType.asElement();
            SignatureWriter writer = new SignatureWriter();
            for (TypeMirror typeArgument : declaredType.getTypeArguments()) {
                internalType(writer, typeArgument, SIGNATURE_WITH_TYPE_BOUNDS);
            }
            writer.visitSuperclass();
            internalType(writer, typeElement.getSuperclass(), SIGNATURE);
            for (TypeMirror interfaceType : typeElement.getInterfaces()) {
                internalType(writer, interfaceType, SIGNATURE);
            }
            return writer.toString();
        }
        return null;
    }

    public String methodTypeDescriptor(TypeMirror[] typeParameters, TypeMirror[] parameters, TypeMirror returnType) {
        return methodType(typeParameters, parameters, returnType, DESCRIPTOR);
    }

    public String methodTypeSignature(TypeMirror[] typeParameters, TypeMirror[] parameters, TypeMirror returnType) {
        return methodType(typeParameters, parameters, returnType, SIGNATURE);
    }

    public String parametersTypeDescriptor(TypeMirror[] parameters) {
        return parametersType(parameters, DESCRIPTOR);
    }

    public String parametersTypeSignature(TypeMirror[] parameters) {
        return parametersType(parameters, SIGNATURE);
    }

    public String returnTypeDescriptor(TypeMirror type) {
        return returnType(type, DESCRIPTOR);
    }

    public String returnTypeSignature(TypeMirror type) {
        return returnType(type, SIGNATURE);
    }

    public String typeDescriptor(TypeMirror type) {
        return type(type, DESCRIPTOR);
    }

    public String typeSignature(TypeMirror type) {
        return type(type, SIGNATURE);
    }

    public String exception(TypeMirror type) {
        type = unwrapType(type);

        SignatureWriter writer = new SignatureWriter();
        buildType(writer, type, DESCRIPTOR_EXCEPTION);
        return writer.toString().substring(1);  // work around bug where the exception has the "L" prefix
    }

    private String type(TypeMirror type, Mode mode) {
        type = unwrapType(type);

        SignatureWriter writer = new SignatureWriter();
        internalType(writer, type, mode);
        return writer.toString();
    }

    private String returnType(TypeMirror type, Mode mode) {
        type = unwrapType(type);

        SignatureWriter writer = new SignatureWriter();
        internalType(writer, type, mode);
        return writer.toString();
    }

    private String parametersType(TypeMirror[] parameters, Mode mode) {
        parameters = unwrapType(parameters);

        SignatureWriter writer = new SignatureWriter();
        internalParametersType(writer, parameters, mode);
        return writer.toString();
    }

    private String methodType(TypeMirror[] typeParameters, TypeMirror[] parameters, TypeMirror returnType, Mode mode) {
        typeParameters = unwrapType(typeParameters);
        parameters = unwrapType(parameters);
        returnType = unwrapType(returnType);

        SignatureWriter writer = new SignatureWriter();
        switch (mode) {
            case DESCRIPTOR: {
                internalParametersType(writer, parameters, mode);
                internalReturnType(writer, returnType, mode);
                break;
            }

            case SIGNATURE_WITH_TYPE_BOUNDS:
            case SIGNATURE_SIMPLE:
            case SIGNATURE: {
                internalTypeParametersType(writer, typeParameters, SIGNATURE_WITH_TYPE_BOUNDS);
                internalParametersType(writer, parameters, mode);
                internalReturnType(writer, returnType, mode);
                break;
            }
        }
        return writer.toString();
    }

    private void internalTypeParametersType(SignatureWriter writer, TypeMirror[] parameters, Mode mode) {
        for (TypeMirror parameter : parameters) {
            internalType(writer, parameter, mode);
        }
    }

    private void internalParametersType(SignatureWriter writer, TypeMirror[] parameters, Mode mode) {
        for (TypeMirror parameter : parameters) {
            writer.visitParameterType();
            internalType(writer, parameter, mode);
        }
    }

    private void internalReturnType(SignatureWriter writer, TypeMirror type, Mode mode) {
        writer.visitReturnType();
        internalType(writer, type, mode);
    }

    private void internalType(SignatureWriter writer, TypeMirror type, Mode mode) {
        Character baseType = Util.toBaseType(type.getKind());
        if (baseType != null) {
            writer.visitBaseType(baseType);
        } else {
            buildType(writer, type, mode);
        }
    }

    private void buildType(SignatureWriter writer, TypeMirror type, Mode mode) {
        switch (type.getKind()) {
            case ARRAY: {
                ArrayType arrayType = (ArrayType) type;
                writer.visitArrayType();
                internalType(writer, arrayType.getComponentType(), mode);
                break;
            }

            case DECLARED: {
                switch (mode) {
                    case DESCRIPTOR_EXCEPTION: {
                        writer.visitClassType(Util.toSlash(type.toString()));
                        break;
                    }

                    case DESCRIPTOR: {
                        type = processingEnv.getTypeUtils().erasure(type);
                        buildDeclaredTypeDescriptor(writer, type);
                        break;
                    }

                    case SIGNATURE:
                    case SIGNATURE_SIMPLE:
                    case SIGNATURE_WITH_TYPE_BOUNDS: {
                        buildDeclaredTypeSignature(writer, type, mode);
                        break;
                    }
                }
                break;
            }

            case TYPEVAR: {
                switch (mode) {
                    case SIGNATURE_WITH_TYPE_BOUNDS: {
                        TypeVariable typeVariable = (TypeVariable) type;
                        TypeParameterElement typeParameterElement = (TypeParameterElement) typeVariable.asElement();
                        writer.visitFormalTypeParameter(typeParameterElement.getSimpleName().toString());
                        if (Util.isInterface(typeVariable.getUpperBound())) {
                            writer.visitInterfaceBound();
                        }
                        internalType(writer, typeVariable.getUpperBound(), SIGNATURE);
                        if (typeVariable.getLowerBound().getKind() != TypeKind.NULL) {
                            // TODO
                        }
                        break;
                    }

                    case SIGNATURE_SIMPLE:
                    case SIGNATURE: {
                        TypeVariable typeVariable = (TypeVariable) type;
                        TypeParameterElement typeParameterElement = (TypeParameterElement) typeVariable.asElement();
                        writer.visitTypeVariable(typeParameterElement.getSimpleName().toString());
                        break;
                    }

                    case DESCRIPTOR: {
                        type = processingEnv.getTypeUtils().erasure(type);
                        buildDeclaredTypeDescriptor(writer, type);
                        break;
                    }
                }
                break;
            }

            case WILDCARD: {
                switch (mode) {
                    case SIGNATURE_SIMPLE:
                    case SIGNATURE:
                    case SIGNATURE_WITH_TYPE_BOUNDS: {
                        WildcardType wildcardType = (WildcardType) type;
                        if (wildcardType.getSuperBound() != null) {
                            writer.visitTypeArgument('-');
                            internalType(writer, wildcardType.getSuperBound(), SIGNATURE);
                        } else if (wildcardType.getExtendsBound() != null) {
                            writer.visitTypeArgument('+');
                            internalType(writer, wildcardType.getExtendsBound(), SIGNATURE);
                        } else {
                            writer.visitTypeArgument();
                        }
                        break;
                    }
                }
                break;
            }
        }
    }

    private void buildDeclaredTypeSignature(SignatureWriter writer, TypeMirror type, Mode mode) {
        DeclaredType declaredType = (DeclaredType) type;
        if (declaredType.getEnclosingType().getKind() != TypeKind.NONE) {
            buildDeclaredTypeSignaturePortions(writer, declaredType, mode);
        } else {
            buildDeclaredTypeSignatureStandard(writer, declaredType, mode);
        }
    }

    private void buildDeclaredTypeSignaturePortions(SignatureWriter writer, DeclaredType declaredType, Mode mode) {
        List<String> portions = new ArrayList<>();
        buildPortionsSignature(declaredType, portions);
        writer.visitClassType(Util.toDot(portions));
        writer.visitEnd();
    }

    private void buildDeclaredTypeSignatureStandard(SignatureWriter writer, DeclaredType declaredType, Mode mode) {
        if (mode == SIGNATURE_SIMPLE) {
            writer.visitInnerClassType(declaredType.asElement().getSimpleName().toString());
        } else {
            TypeMirror erasedType = processingEnv.getTypeUtils().erasure(declaredType);
            writer.visitClassType(Util.toSlash(erasedType.toString()));
        }
        if (Util.hasTypeArguments(declaredType)) {
            writer.visitTypeArgument('=');
            for (TypeMirror typeArgument : declaredType.getTypeArguments()) {
                internalType(writer, typeArgument, mode);
            }
        }
        writer.visitEnd();
    }

    private void buildDeclaredTypeDescriptor(SignatureWriter writer, TypeMirror type) {
        List<String> portions = new ArrayList<>();
        buildPortionsDescriptor((DeclaredType) type, portions);
        writer.visitClassType(Util.toSlash(portions));
        writer.visitEnd();
    }

    private void buildPortionsDescriptor(DeclaredType type, List<String> portions) {
        TypeMirror enclosingType = type.getEnclosingType();
        if (enclosingType.getKind() != TypeKind.NONE) {
            buildPortionsDescriptor((DeclaredType) enclosingType, portions);
            portions.add(type.asElement().getSimpleName().toString());
        } else {
            portions.add(Util.toSlash(type.toString()));
        }
    }

    private void buildPortionsSignature(DeclaredType type, List<String> portions) {
        SignatureWriter tempWriter = new SignatureWriter();
        TypeMirror enclosingType = type.getEnclosingType();
        if (enclosingType.getKind() != TypeKind.NONE) {
            buildPortionsSignature((DeclaredType) enclosingType, portions);
            buildDeclaredTypeSignatureStandard(tempWriter, type, SIGNATURE_SIMPLE);
        } else {
            buildDeclaredTypeSignatureStandard(tempWriter, type, SIGNATURE);
        }
        portions.add(tempWriter.toString());
    }

    private TypeMirror[] unwrapType(TypeMirror[] types) {
        return Stream.of(types)
                .map(this::unwrapType)
                .toArray(TypeMirror[]::new);
    }

    private TypeMirror unwrapType(TypeMirror type) {
        if (type.getKind() == TypeKind.DECLARED) {
            if (!type.getAnnotationMirrors().isEmpty()) {
                // for some reason, annotated types are an internal compound "annotation type" - this normalizes that
                Element element = ((DeclaredType) type).asElement();
                Element typeElement = processingEnv.getTypeUtils().asElement(element.asType());
                if (typeElement != null) {
                    type = typeElement.asType();
                }
            }
        }
        return type;
    }
}
