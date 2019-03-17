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

import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeKind;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    public static Optional<Character> toBaseType(TypeKind kind) {
        return Optional.ofNullable(baseTypes.get(kind));
    }

    private Util() {
    }
}
