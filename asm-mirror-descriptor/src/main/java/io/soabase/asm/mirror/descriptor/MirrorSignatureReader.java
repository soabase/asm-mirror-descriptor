package io.soabase.asm.mirror.descriptor;

import org.objectweb.asm.signature.SignatureWriter;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class MirrorSignatureReader {
    private final ProcessingEnvironment processingEnv;

    public enum Mode {
        DESCRIPTOR,
        SIGNATURE
    }

    public MirrorSignatureReader(ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
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
        // TODO mode
        Util.toBaseType(type.getKind())
                .filter(c -> {
                    writer.visitBaseType(c);
                    return true;
                })
                .orElseGet(() -> {
                    buildType(writer, type);
                    return null;
                });
    }

    private void buildType(SignatureWriter writer, TypeMirror type) {
        if (type.getKind() == TypeKind.ARRAY) {
            // TODO
        }

        // TODO asSignature
        writer.visitClassType(Util.toSlash(type.toString()));
        writer.visitEnd();
    }
}
