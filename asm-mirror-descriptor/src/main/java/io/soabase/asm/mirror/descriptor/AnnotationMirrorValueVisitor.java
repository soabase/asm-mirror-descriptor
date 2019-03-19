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

import org.objectweb.asm.AnnotationVisitor;

import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.VariableElement;
import java.util.List;
import java.util.stream.Stream;

import static io.soabase.asm.mirror.descriptor.MirrorSignatureReader.Mode.DESCRIPTOR;

public class AnnotationMirrorValueVisitor {
    private final String name;
    private final MirrorSignatureReader signatureReader;
    private final AnnotationVisitor visitor;

    public AnnotationMirrorValueVisitor(String name, MirrorSignatureReader signatureReader, AnnotationVisitor visitor) {
        this.name = name;
        this.signatureReader = signatureReader;
        this.visitor = visitor;
    }

    public void visit(Object value) {
        if (value != null) {
            if (value instanceof VariableElement) {
                VariableElement variableElement = (VariableElement) value;
                switch (variableElement.getKind()) {
                    case ENUM_CONSTANT: {
                        visitor.visitEnum(name, signatureReader.type(variableElement.asType(), DESCRIPTOR), variableElement.getSimpleName().toString());
                        break;
                    }

                    case ANNOTATION_TYPE: {
                         break;
                    }

                    default: {
                        visitor.visit(name, variableElement.getSimpleName().toString());
                        break;
                    }
                }
            } else if (value instanceof AnnotationValue) {
                visit(((AnnotationValue)value).getValue());
            } else {
                Class<?> clazz = value.getClass();
                if (clazz.isArray()) {
                    visitArray(Stream.of());
                } else if (List.class.isAssignableFrom(clazz)) {
                    //noinspection unchecked
                    visitArray(((List) value).stream());
                } else {
                    visitor.visit(name, value);
                }
            }
        }
    }

    private void visitArray(Stream<Object> values) {
        AnnotationVisitor visitArray = visitor.visitArray(name);
        if (visitArray != null) {
            AnnotationMirrorValueVisitor visitArrayMirror = new AnnotationMirrorValueVisitor(null, signatureReader, visitArray);
            values.forEach(visitArrayMirror::visit);
            visitArray.visitEnd();
        }
    }

    @SuppressWarnings("RedundantCast")
    private void visitArray(Object value) {
        if (value instanceof byte[]) {
            visitArray(Stream.of((byte[])value));
        } else if (value instanceof boolean[]) {
            visitArray(Stream.of((boolean[])value));
        } else if (value instanceof short[]) {
            visitArray(Stream.of((short[])value));
        } else if (value instanceof char[]) {
            visitArray(Stream.of((char[])value));
        } else if (value instanceof int[]) {
            visitArray(Stream.of((int[])value));
        } else if (value instanceof long[]) {
            visitArray(Stream.of((long[])value));
        } else if (value instanceof float[]) {
            visitArray(Stream.of((float[])value));
        } else if (value instanceof double[]) {
            visitArray(Stream.of((double[])value));
        } else {
            visitArray(Stream.of((Object[])value));
        }
    }
}
