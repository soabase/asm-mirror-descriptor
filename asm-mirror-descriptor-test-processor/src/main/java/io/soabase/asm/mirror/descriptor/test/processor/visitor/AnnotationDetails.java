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
package io.soabase.asm.mirror.descriptor.test.processor.visitor;

import org.objectweb.asm.TypePath;

import java.util.HashMap;
import java.util.Map;

public class AnnotationDetails {
    private final String descriptor;
    private final int typeRef;
    private final TypePath typePath;
    private final int parameter;
    private final Map<String, String> values = new HashMap<>();

    public AnnotationDetails(String descriptor, int typeRef, TypePath typePath, int parameter) {
        this.descriptor = descriptor;
        this.typeRef = typeRef;
        this.typePath = typePath;
        this.parameter = parameter;
    }

    public void add(String name, String value) {
        values.put(name, value);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("    descriptor: ").append(descriptor).append('\n');
        str.append("    typeref:    ").append(typeRef).append('\n');
        str.append("    typepath:   ").append(typePath).append('\n');
        str.append("    parameter:  ").append(parameter).append('\n');
        values.forEach((name, value) -> str.append("    ").append(name).append(":").append(space(name.length() + 1, 12)).append(value).append('\n'));
        return str.toString();
    }

    private static String space(int length, int max) {
        int needed = max - length;
        StringBuilder str = new StringBuilder();
        while (needed-- > 0) {
            str.append(' ');
        }
        return str.toString();
    }
}
