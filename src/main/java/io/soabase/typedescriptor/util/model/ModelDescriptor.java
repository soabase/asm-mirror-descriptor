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
package io.soabase.typedescriptor.util.model;

import io.soabase.typedescriptor.util.FormatUtil;
import io.soabase.typedescriptor.util.TypeDescriptorToken;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.stream.Collectors;

public class ModelDescriptor {
    private final String value;
    private final Mode mode;
    private final boolean hasSignature;

    public enum Mode {
        DESCRIPTOR,
        SIGNATURE
    }

    public ModelDescriptor(Mode mode, Element e) {
        this(mode, e.asType());
    }

    public ModelDescriptor(Mode mode, TypeMirror type) {
        this.mode = mode;
        boolean[] hasSignatureBuffer = new boolean[1];
        value = toDescriptor(type, hasSignatureBuffer);
        hasSignature = hasSignatureBuffer[0];
    }

    public String getValue() {
        return value;
    }

    public boolean hasSignature() {
        return hasSignature;
    }

    private String toDescriptor(TypeMirror type, boolean[] hasSignatureBuffer) {
        switch (type.getKind()) {
            case BOOLEAN:
                return TypeDescriptorToken.BOOLEAN.value();
            case BYTE:
                return TypeDescriptorToken.BYTE.value();
            case SHORT:
                return TypeDescriptorToken.SHORT.value();
            case INT:
                return TypeDescriptorToken.INT.value();
            case LONG:
                return TypeDescriptorToken.LONG.value();
            case CHAR:
                return TypeDescriptorToken.CHAR.value();
            case FLOAT:
                return TypeDescriptorToken.FLOAT.value();
            case DOUBLE:
                return TypeDescriptorToken.DOUBLE.value();
            case ARRAY: {
                ArrayType arrayType = (ArrayType) type;
                return TypeDescriptorToken.ARRAY.value(toDescriptor(arrayType.getComponentType(), hasSignatureBuffer));
            }
            case DECLARED: {
                DeclaredType declaredType = (DeclaredType) type;
                TypeElement typeElement = (TypeElement) declaredType.asElement();
                if ((mode == Mode.SIGNATURE) && !declaredType.getTypeArguments().isEmpty()) {
                    hasSignatureBuffer[0] = true;
                    String rawValue = FormatUtil.toTypeFormat(typeElement.getQualifiedName().toString(), false);
                    return rawValue + declaredType.getTypeArguments().stream()
                            .map(e -> "<" + toDescriptor(e, hasSignatureBuffer) + ">;")
                            .collect(Collectors.joining());
                }
                return TypeDescriptorToken.TYPE.value(typeElement.getQualifiedName().toString());
            }
            default: {
                throw new Error();  // TODO
            }
        }
    }
}
