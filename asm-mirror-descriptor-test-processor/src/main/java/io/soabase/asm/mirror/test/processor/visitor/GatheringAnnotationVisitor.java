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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class GatheringAnnotationVisitor extends AnnotationVisitor {
    private final List<String> values = new ArrayList<>();
    private final String parentName;
    private final Consumer<List<String>> consumer;

    public GatheringAnnotationVisitor(String parentName, Consumer<List<String>> consumer) {
        super(Opcodes.ASM7);
        this.parentName = parentName.isEmpty() ? "" : (parentName + ": ");
        this.consumer = consumer;
    }

    @Override
    public void visit(String name, Object value) {
        if (parentName.isEmpty() && (name == null)) {
            values.add(String.valueOf(value));
        } else {
            values.add(parentName + name + ": " + value);
        }
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
        values.add(parentName + name + ": " + Format.asDescriptorValue(descriptor, value));
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        return new GatheringAnnotationVisitor(parentName + name + "[]", values::addAll);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        return new GatheringAnnotationVisitor(parentName + Format.asNameDescriptor(name, descriptor), values::addAll);
    }

    @Override
    public void visitEnd() {
        consumer.accept(values);
    }
}
