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
package io.soabase.asm.mirror.test.processor.visitor;

import org.objectweb.asm.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class TypeVisitor {
    private final Map<String, List<String>> values = new TreeMap<>();

    public void accept(String label, Type type) {
        accept(label, Collections.singletonList(type));
    }

    public void accept(String label, List<Type> type) {
        List<String> values = type.stream().map(Type::toString).sorted().collect(Collectors.toList());
        this.values.put(label, values);
    }

    @Override
    public String toString() {
        return values.entrySet().stream()
                .map(entry -> entry.getKey() + "\n" + String.join("\n", entry.getValue()) + "\n")
                .collect(Collectors.joining("\n"));
    }
}
