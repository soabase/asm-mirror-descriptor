package io.soabase.asm.mirror.descriptor;

import org.objectweb.asm.signature.SignatureWriter;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;

public class MirrorSignatureReader {
    private final ProcessingEnvironment processingEnv;

    public enum Mode {
        DESCRIPTOR,
        SIGNATURE
    }

    public MirrorSignatureReader(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    public String classSignature(TypeMirror type) {
        if (type.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) type;
            if (!declaredType.getTypeArguments().isEmpty()) {
                SignatureWriter writer = new SignatureWriter();
                for (TypeMirror typeArgument : declaredType.getTypeArguments()) {
                    internalType(writer, typeArgument, Mode.SIGNATURE);
                }
                writer.visitSuperclass();
                internalType(writer, ((TypeElement) declaredType.asElement()).getSuperclass(), Mode.SIGNATURE);
                return writer.toString();
            }
        }
        return null;
    }

    public String methodType(TypeMirror[] parameters, TypeMirror returnType, Mode mode) {
        SignatureWriter writer = new SignatureWriter();
        internalParametersType(writer, parameters, mode);
        internalReturnType(writer, returnType, mode);
        return writer.toString();
    }

    public String parametersType(TypeMirror[] parameters, Mode mode) {
        SignatureWriter writer = new SignatureWriter();
        internalParametersType(writer, parameters, mode);
        return writer.toString();
    }

    public String returnType(TypeMirror type, Mode mode) {
        SignatureWriter writer = new SignatureWriter();
        internalReturnType(writer, type, mode);
        return writer.toString();
    }

    public String type(TypeMirror type, Mode mode) {
        SignatureWriter writer = new SignatureWriter();
        internalType(writer, type, mode);
        return writer.toString();
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
        if (mode == Mode.DESCRIPTOR) {
            type = processingEnv.getTypeUtils().erasure(type);
        }
        switch (type.getKind()) {
            case ARRAY: {
                ArrayType arrayType = (ArrayType) type;
                writer.visitArrayType();
                internalType(writer, arrayType.getComponentType(), mode);
                break;
            }

            case DECLARED: {
                if (mode == Mode.SIGNATURE) {
                    buildDeclaredTypeSignature(writer, type);
                } else {
                    buildDeclaredTypeDescriptor(writer, type);
                }
                break;
            }

            case TYPEVAR: {
                if (mode == Mode.SIGNATURE) {
                    TypeVariable typeVariable = (TypeVariable) type;
                    TypeParameterElement typeParameterElement = (TypeParameterElement) typeVariable.asElement();
                    writer.visitFormalTypeParameter(typeParameterElement.getSimpleName().toString());
                    internalType(writer, typeVariable.getUpperBound(), Mode.SIGNATURE);
                    if (typeVariable.getLowerBound().getKind() != TypeKind.NULL) {
                        // TODO
                    }
                }
                break;
            }
        }
    }

    private void buildDeclaredTypeSignature(SignatureWriter writer, TypeMirror type) {
        DeclaredType declaredType = (DeclaredType) type;
        TypeMirror erasedType = processingEnv.getTypeUtils().erasure(type);
        writer.visitClassType(Util.toSlash(erasedType.toString()));
        if (!declaredType.getTypeArguments().isEmpty()) {
            writer.visitTypeArgument('=');
            for (TypeMirror typeArgument : declaredType.getTypeArguments()) {
                internalType(writer, typeArgument, Mode.SIGNATURE);
            }
        }
        writer.visitEnd();
    }

    private void buildDeclaredTypeDescriptor(SignatureWriter writer, TypeMirror type) {
        writer.visitClassType(Util.toSlash(type.toString()));
        writer.visitEnd();
    }
}
