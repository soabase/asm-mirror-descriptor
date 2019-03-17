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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Util {
    private static final Map<Modifier, Integer> modifierFlags;
    static {
        Map<Modifier, Integer> map = new HashMap<>();
        map.put(Modifier.PUBLIC, Opcodes.ACC_PUBLIC);
        map.put(Modifier.PROTECTED, Opcodes.ACC_PROTECTED);
        map.put(Modifier.PRIVATE, Opcodes.ACC_PRIVATE);
        map.put(Modifier.ABSTRACT, Opcodes.ACC_ABSTRACT);
        map.put(Modifier.DEFAULT, 0);   // TODO
        map.put(Modifier.STATIC, Opcodes.ACC_STATIC);
        map.put(Modifier.FINAL, Opcodes.ACC_FINAL);
        map.put(Modifier.TRANSIENT, Opcodes.ACC_TRANSIENT);
        map.put(Modifier.VOLATILE, Opcodes.ACC_VOLATILE);
        map.put(Modifier.SYNCHRONIZED, Opcodes.ACC_SYNCHRONIZED);
        map.put(Modifier.NATIVE, Opcodes.ACC_NATIVE);
        map.put(Modifier.STRICTFP, Opcodes.ACC_STRICT);
        modifierFlags = Collections.unmodifiableMap(map);
    }

    private static final Map<TypeKind, Character> baseTypes;
    static {
        Map<TypeKind, Character> map = new HashMap<>();
        map.put(TypeKind.BOOLEAN, 'Z');
        map.put(TypeKind.DOUBLE, 'D');
        map.put(TypeKind.FLOAT, 'F');
        map.put(TypeKind.CHAR, 'C');
        map.put(TypeKind.INT, 'I');
        map.put(TypeKind.SHORT, 'S');
        map.put(TypeKind.LONG, 'J');
        map.put(TypeKind.BYTE, 'B');
        map.put(TypeKind.VOID, 'V');
        baseTypes = Collections.unmodifiableMap(map);
    }

    public static int modifiersToAccessFlags(Collection<Modifier> modifiers) {
        int flags = 0;
        for (Modifier modifier : modifiers) {
            flags |= modifierFlags.getOrDefault(modifier, 0);
        }
        return flags;
    }

    public static String toSlash(String fqn) {
        return fqn.replace('.', '/');
    }

    public static Character toBaseType(TypeKind kind) {
        return baseTypes.get(kind);
    }

    public static boolean isObject(ProcessingEnvironment processingEnv, Element element) {
        return processingEnv.getElementUtils().getTypeElement("java.lang.Object").equals(element);
    }

    public static boolean hasTypeArguments(Element element) {
        switch (element.getKind()) {
            case METHOD:
            case CONSTRUCTOR:
                return hasTypeArguments((ExecutableElement) element);

            case PARAMETER:
                return hasTypeArguments(element.asType());

            // TODO
        }
        return false;
    }

    public static boolean hasTypeArguments(ExecutableElement element) {
        if (!element.getTypeParameters().isEmpty()) {
            return true;
        }
        if (hasTypeArguments(element.getReturnType())) {
            return true;
        }
        return element.getParameters().stream().anyMatch(Util::hasTypeArguments);
    }

    public static boolean hasTypeArguments(TypeMirror type) {
        return (type.getKind() == TypeKind.DECLARED) && hasTypeArguments((DeclaredType) type);
    }

    public static boolean hasTypeArguments(DeclaredType type) {
        return !type.getTypeArguments().isEmpty();
    }

    public static boolean isInterface(TypeMirror type) {
        return (type.getKind() == TypeKind.DECLARED) && (((DeclaredType) type).asElement().getKind() == ElementKind.INTERFACE);
    }

    private Util() {
    }
}
