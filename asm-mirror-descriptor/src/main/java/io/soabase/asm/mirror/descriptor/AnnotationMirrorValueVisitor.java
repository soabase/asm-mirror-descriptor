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

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.AnnotationValueVisitor;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import java.util.List;

import static io.soabase.asm.mirror.descriptor.MirrorSignatureReader.Mode.DESCRIPTOR;

public class AnnotationMirrorValueVisitor implements AnnotationValueVisitor<Void, AnnotationVisitor> {
    private final String name;
    private final MirrorSignatureReader signatureReader;

    public AnnotationMirrorValueVisitor(String name, MirrorSignatureReader signatureReader) {
        this.name = name;
        this.signatureReader = signatureReader;
    }

    @Override
    public Void visit(AnnotationValue av, AnnotationVisitor visitor) {
        return null;
    }

    @Override
    public Void visit(AnnotationValue av) {
        return null;
    }

    @Override
    public Void visitBoolean(boolean b, AnnotationVisitor visitor) {
        visitor.visit(name, b);
        return null;
    }

    @Override
    public Void visitByte(byte b, AnnotationVisitor visitor) {
        visitor.visit(name, b);
        return null;
    }

    @Override
    public Void visitChar(char c, AnnotationVisitor visitor) {
        visitor.visit(name, c);
        return null;
    }

    @Override
    public Void visitDouble(double d, AnnotationVisitor visitor) {
        visitor.visit(name, d);
        return null;
    }

    @Override
    public Void visitFloat(float f, AnnotationVisitor visitor) {
        visitor.visit(name, f);
        return null;
    }

    @Override
    public Void visitInt(int i, AnnotationVisitor visitor) {
        visitor.visit(name, i);
        return null;
    }

    @Override
    public Void visitLong(long i, AnnotationVisitor visitor) {
        visitor.visit(name, i);
        return null;
    }

    @Override
    public Void visitShort(short s, AnnotationVisitor visitor) {
        visitor.visit(name, s);
        return null;
    }

    @Override
    public Void visitString(String s, AnnotationVisitor visitor) {
        visitor.visit(name, s);
        return null;
    }

    @Override
    public Void visitType(TypeMirror t, AnnotationVisitor visitor) {
        return null;
    }

    @Override
    public Void visitEnumConstant(VariableElement c, AnnotationVisitor visitor) {
        String descriptor = signatureReader.type(c.asType(), DESCRIPTOR);
        visitor.visitEnum(name, descriptor, c.getSimpleName().toString());
        return null;
    }

    @Override
    public Void visitAnnotation(AnnotationMirror a, AnnotationVisitor visitor) {
        String descriptor = signatureReader.type(a.getAnnotationType(), DESCRIPTOR);
        AnnotationVisitor visitAnnotation = visitor.visitAnnotation(name, descriptor);
        if (visitAnnotation != null) {
            a.getElementValues().forEach((element, value) -> {
                AnnotationMirrorValueVisitor visitAnnotationValues = new AnnotationMirrorValueVisitor(element.getSimpleName().toString(), signatureReader);
                value.accept(visitAnnotationValues, visitAnnotation);
            });
            visitAnnotation.visitEnd();
        }
        return null;
    }

    @Override
    public Void visitArray(List<? extends AnnotationValue> vals, AnnotationVisitor visitor) {
        AnnotationVisitor visitArray = visitor.visitArray(name);
        if (visitArray != null) {
            AnnotationMirrorValueVisitor visitArrayValues = new AnnotationMirrorValueVisitor(null, signatureReader);
            vals.forEach(v -> v.accept(visitArrayValues, visitArray));
            visitArray.visitEnd();
        }
        return null;
    }

    @Override
    public Void visitUnknown(AnnotationValue av, AnnotationVisitor visitor) {
        return null;
    }
}
