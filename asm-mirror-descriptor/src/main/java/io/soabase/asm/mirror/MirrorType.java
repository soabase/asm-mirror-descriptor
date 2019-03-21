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
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Corollary to {@link Type}. A Java field or method type. This class can be used to make it easier to manipulate type and
 * method descriptors.
 */
public class MirrorType {
    private static final Map<TypeKind, Type> primitiveMirrorTypes;
    static {
        Map<TypeKind, Type> map = new HashMap<>();
        map.put(TypeKind.BOOLEAN, Type.BOOLEAN_TYPE);
        map.put(TypeKind.BYTE, Type.BYTE_TYPE);
        map.put(TypeKind.SHORT, Type.SHORT_TYPE);
        map.put(TypeKind.INT, Type.INT_TYPE);
        map.put(TypeKind.LONG, Type.LONG_TYPE);
        map.put(TypeKind.CHAR, Type.CHAR_TYPE);
        map.put(TypeKind.FLOAT, Type.FLOAT_TYPE);
        map.put(TypeKind.DOUBLE, Type.DOUBLE_TYPE);
        primitiveMirrorTypes = Collections.unmodifiableMap(map);
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
     * Returns the {@link Type} corresponding to the given type descriptor.
     *
     * @param spec a field or method type descriptor or type signature
     * @return the {@link Type} corresponding to the given type descriptor.
     */
    public static Type getType(String spec) {
        return Type.getType(spec);
    }

    /**
     * Returns the {@link Type} corresponding to the type mirror.
     *
     * @param typeMirror type mirror
     * @return the {@link Type} corresponding to the given type mirror.
     */
    public static Type getType(ProcessingEnvironment processingEnv, TypeMirror typeMirror) {
        Type primitiveMirrorType = primitiveMirrorTypes.get(typeMirror.getKind());
        if (primitiveMirrorType != null) {
            return primitiveMirrorType;
        }
        return getType(getDescriptor(processingEnv, typeMirror));
    }

    /**
     * Returns the {@link Type} corresponding to the element
     *
     * @param element element
     * @return the {@link Type} corresponding to the given type element or null if element is invalid
     */
    public static Type getType(ProcessingEnvironment processingEnv, Element element) {
        MirrorSignatures mirrorSignatures = new MirrorSignatures(processingEnv);
        switch (element.getKind()) {
            case ENUM:
            case CLASS:
            case INTERFACE: {
                return getType(mirrorSignatures.typeDescriptor(element.asType()));
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
                return getType(mirrorSignatures.methodTypeDescriptor(typeParameters, parameters, executableElement.getReturnType()));
            }
        }
        return null;
    }

    /**
     * Returns the {@link Type} corresponding to the return type of the given method.
     *
     * @param method a method.
     * @return the {@link Type} corresponding to the return type of the given method.
     */
    public static Type getReturnType(ProcessingEnvironment processingEnv, ExecutableElement method) {
        MirrorSignatures mirrorSignatures = new MirrorSignatures(processingEnv);
        return getType(mirrorSignatures.returnTypeDescriptor(method.getReturnType()));
    }

    private MirrorType() {
    }
}
