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
import org.objectweb.asm.Type;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public class MirrorType {
    private final Type type;

    private static final Map<TypeKind, MirrorType> primitiveMirrorTypes;
    static {
        Map<TypeKind, MirrorType> map = new HashMap<>();
        map.put(TypeKind.BOOLEAN, get(Type.BOOLEAN_TYPE));
        map.put(TypeKind.BYTE, get(Type.BYTE_TYPE));
        map.put(TypeKind.SHORT, get(Type.SHORT_TYPE));
        map.put(TypeKind.INT, get(Type.INT_TYPE));
        map.put(TypeKind.LONG, get(Type.LONG_TYPE));
        map.put(TypeKind.CHAR, get(Type.CHAR_TYPE));
        map.put(TypeKind.FLOAT, get(Type.FLOAT_TYPE));
        map.put(TypeKind.DOUBLE, get(Type.DOUBLE_TYPE));
        primitiveMirrorTypes = Collections.unmodifiableMap(map);
    }

    public static MirrorType get(Type type) {
        return new MirrorType(type);
    }

    /**
     * Returns the descriptor corresponding to the type mirror.
     *
     * @param typeMirror type mirror
     * @return the descriptor corresponding to the given class.
     */
    public static String getDescriptor(ProcessingEnvironment processingEnv, TypeMirror typeMirror) {
        MirrorSignatures mirrorSignatures = new MirrorSignatures(processingEnv);
        return mirrorSignatures.typeDescriptor(typeMirror);
    }

    /**
     * Returns the signature corresponding to the type mirror.
     *
     * @param typeMirror type mirror
     * @return the signature corresponding to the given class.
     */
    public static String getSignature(ProcessingEnvironment processingEnv, TypeMirror typeMirror) {
        MirrorSignatures mirrorSignatures = new MirrorSignatures(processingEnv);
        return mirrorSignatures.typeSignature(typeMirror);
    }

    /**
     * Returns the {@link MirrorType} corresponding to the given type descriptor or type signature.
     *
     * @param spec a field or method type descriptor or type signature
     * @return the {@link MirrorType} corresponding to the given type descriptor or type signature.
     */
    public static MirrorType getType(String spec) {
        return get(Type.getType(spec));
    }

    /**
     * Returns the {@link MirrorType} corresponding to the type mirror.
     *
     * @param typeMirror type mirror
     * @return the {@link MirrorType} corresponding to the given type mirror.
     */
    public static MirrorType getType(ProcessingEnvironment processingEnv, TypeMirror typeMirror) {
        return internalGetType(processingEnv, typeMirror, MirrorType::getDescriptor);
    }

    /**
     * Returns the {@link MirrorType} corresponding to the type mirror as a signature
     *
     * @param typeMirror type mirror
     * @return the {@link MirrorType} corresponding to the given type mirror as a signature
     */
    public static MirrorType getTypeSignature(ProcessingEnvironment processingEnv, TypeMirror typeMirror) {
        return internalGetType(processingEnv, typeMirror, MirrorType::getSignature);
    }

    /**
     * Returns the {@link MirrorType} corresponding to the element
     *
     * @param element element
     * @return the {@link MirrorType} corresponding to the given type element or null if element is invalid
     */
    public static MirrorType getType(ProcessingEnvironment processingEnv, ExecutableElement element) {
        MirrorSignatures mirrorSignatures = new MirrorSignatures(processingEnv);
        return internalGetType(processingEnv, element, mirrorSignatures::typeDescriptor, mirrorSignatures::methodTypeDescriptor);
    }

    /**
     * Returns the {@link MirrorType} corresponding to the element as a signature
     *
     * @param element element
     * @return the {@link MirrorType} corresponding to the given type element as a signature or null if element is invalid
     */
    public static MirrorType getTypeSignature(ProcessingEnvironment processingEnv, ExecutableElement element) {
        MirrorSignatures mirrorSignatures = new MirrorSignatures(processingEnv);
        return internalGetType(processingEnv, element, mirrorSignatures::classSignature, mirrorSignatures::methodTypeSignature);
    }

    /**
     * Returns the {@link MirrorType} corresponding to the return type of the given method.
     *
     * @param method a method.
     * @return the {@link MirrorType} corresponding to the return type of the given method.
     */
    public static MirrorType getReturnType(ProcessingEnvironment processingEnv, ExecutableElement method) {
        MirrorSignatures mirrorSignatures = new MirrorSignatures(processingEnv);
        return getType(mirrorSignatures.returnTypeDescriptor(method.getReturnType()));
    }

    /**
     * Returns the {@link MirrorType} corresponding to the return type of the given method as a signature
     *
     * @param method a method.
     * @return the {@link MirrorType} corresponding to the return type of the given method as a signature
     */
    public static MirrorType getReturnTypeSignature(ProcessingEnvironment processingEnv, ExecutableElement method) {
        MirrorSignatures mirrorSignatures = new MirrorSignatures(processingEnv);
        return getType(mirrorSignatures.returnTypeSignature(method.getReturnType()));
    }

    public Type getType() {
        return type;
    }

    private static MirrorType internalGetType(ProcessingEnvironment processingEnv, TypeMirror typeMirror, BiFunction<ProcessingEnvironment, TypeMirror, String> proc) {
        MirrorType primitiveMirrorType = primitiveMirrorTypes.get(typeMirror.getKind());
        if (primitiveMirrorType != null) {
            return primitiveMirrorType;
        }
        return getType(proc.apply(processingEnv, typeMirror));
    }

    @FunctionalInterface
    private interface MethodTypeDescriptorProc {
        String apply(TypeMirror[] typeParameters, TypeMirror[] parameters, TypeMirror returnType);
    }

    private static MirrorType internalGetType(ProcessingEnvironment processingEnv, Element element, Function<TypeMirror, String> classProc, MethodTypeDescriptorProc methodProc) {
        switch (element.getKind()) {
            case ENUM:
            case CLASS:
            case INTERFACE: {
                return getType(classProc.apply(element.asType()));
            }

            case METHOD:
            case CONSTRUCTOR: {
                ExecutableElement executableElement = (ExecutableElement) element;
                TypeMirror[] typeParameters = executableElement.getTypeParameters().stream()
                        .map(Element::asType)
                        .toArray(TypeMirror[]::new);
                TypeMirror[] parameters = executableElement.getParameters().stream()
                        .map(Element::asType)
                        .toArray(TypeMirror[]::new);
                return getType(methodProc.apply(typeParameters, parameters, executableElement.getReturnType()));
            }
        }
        return null;
    }

    private MirrorType(Type type) {
        this.type = type;
    }
}
