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
import java.util.HashSet;
import java.util.Set;

public class MirrorSignatureReader {
    private final ProcessingEnvironment processingEnv;

    public enum Mode {
        UNTERMINATED_DESCRIPTOR,
        DESCRIPTOR,
        SIGNATURE
    }

    private static class SignatureContext {
        final Set<TypeMirror> visitedTypes = new HashSet<>();

        boolean checkVisited(ProcessingEnvironment processingEnv, TypeMirror type) {
            if (visitedTypes.stream().anyMatch(t -> processingEnv.getTypeUtils().isSameType(t, type))) {
                return false;
            }
            visitedTypes.add(type);
            return true;
        }
    }

    public MirrorSignatureReader(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    public String classSignature(TypeMirror type) {
        if (type.getKind() == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) type;
            if (Util.hasTypeArguments(declaredType)) {
                SignatureWriter writer = new SignatureWriter();
                SignatureContext signatureContext = new SignatureContext();
                for (TypeMirror typeArgument : declaredType.getTypeArguments()) {
                    internalType(writer, typeArgument, Mode.SIGNATURE, signatureContext);
                }
                writer.visitSuperclass();
                internalType(writer, ((TypeElement) declaredType.asElement()).getSuperclass(), Mode.SIGNATURE, signatureContext);
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
        internalType(writer, type, mode, new SignatureContext());
        return writer.toString();
    }

    private void internalParametersType(SignatureWriter writer, TypeMirror[] parameters, Mode mode) {
        SignatureContext signatureContext = new SignatureContext();
        for (TypeMirror parameter : parameters) {
            writer.visitParameterType();
            internalType(writer, parameter, mode, signatureContext);
        }
    }

    private void internalReturnType(SignatureWriter writer, TypeMirror type, Mode mode) {
        writer.visitReturnType();
        internalType(writer, type, mode, new SignatureContext());
    }

    private void internalType(SignatureWriter writer, TypeMirror type, Mode mode, SignatureContext signatureContext) {
        Character baseType = Util.toBaseType(type.getKind());
        if (baseType != null) {
            writer.visitBaseType(baseType);
        } else {
            buildType(writer, type, mode, signatureContext);
        }
    }

    private void buildType(SignatureWriter writer, TypeMirror type, Mode mode, SignatureContext signatureContext) {
        if (mode != Mode.SIGNATURE) {
            type = processingEnv.getTypeUtils().erasure(type);
        }
        switch (type.getKind()) {
            case ARRAY: {
                ArrayType arrayType = (ArrayType) type;
                writer.visitArrayType();
                internalType(writer, arrayType.getComponentType(), mode, signatureContext);
                break;
            }

            case DECLARED: {
                switch (mode) {
                    case UNTERMINATED_DESCRIPTOR: {
                        writer.visitClassType(Util.toSlash(type.toString()));
                        break;
                    }

                    case DESCRIPTOR: {
                        buildDeclaredTypeDescriptor(writer, type);
                        break;
                    }

                    case SIGNATURE: {
                        buildDeclaredTypeSignature(writer, type, signatureContext);
                        break;
                    }
                }
                break;
            }

            case TYPEVAR: {
                if (mode == Mode.SIGNATURE) {
                    TypeVariable typeVariable = (TypeVariable) type;
                    TypeParameterElement typeParameterElement = (TypeParameterElement) typeVariable.asElement();
                    if (signatureContext.checkVisited(processingEnv, typeVariable.getUpperBound())) {
                        writer.visitFormalTypeParameter(typeParameterElement.getSimpleName().toString());
                        if (Util.isInterface(typeVariable.getUpperBound())) {
                            writer.visitInterfaceBound();
                        }
                        internalType(writer, typeVariable.getUpperBound(), Mode.SIGNATURE, signatureContext);
                        if (typeVariable.getLowerBound().getKind() != TypeKind.NULL) {
                            // TODO
                        }
                    } else {
                        writer.visitTypeVariable(typeParameterElement.getSimpleName().toString());
                    }
                }
                break;
            }
        }
    }

    private void buildDeclaredTypeSignature(SignatureWriter writer, TypeMirror type, SignatureContext signatureContext) {
        DeclaredType declaredType = (DeclaredType) type;
        TypeMirror erasedType = processingEnv.getTypeUtils().erasure(type);
        writer.visitClassType(Util.toSlash(erasedType.toString()));
        if (Util.hasTypeArguments(declaredType)) {
            writer.visitTypeArgument('=');
            for (TypeMirror typeArgument : declaredType.getTypeArguments()) {
                internalType(writer, typeArgument, Mode.SIGNATURE, signatureContext);
            }
        }
        writer.visitEnd();
    }

    private void buildDeclaredTypeDescriptor(SignatureWriter writer, TypeMirror type) {
        writer.visitClassType(Util.toSlash(type.toString()));
        writer.visitEnd();
    }
}
