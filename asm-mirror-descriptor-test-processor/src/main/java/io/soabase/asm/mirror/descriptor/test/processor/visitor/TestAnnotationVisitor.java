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

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

import static io.soabase.asm.mirror.descriptor.test.processor.visitor.Format.asDescriptorValue;

public class TestAnnotationVisitor extends AnnotationVisitor {
    private final VisitDetails details;
    private final AnnotationDetails annotationDetails;

    public TestAnnotationVisitor(VisitDetails details, String descriptor, int typeRef, TypePath typePath, int parameter) {
        super(Opcodes.ASM7);
        this.details = details;
        annotationDetails = new AnnotationDetails(descriptor, typeRef, typePath, parameter);
    }

    @Override
    public void visit(String name, Object value) {
        annotationDetails.add(name, String.valueOf(value));
    }

    @Override
    public void visitEnum(String name, String descriptor, String value) {
        annotationDetails.add(name, asDescriptorValue(descriptor, value));
    }

    @Override
    public AnnotationVisitor visitArray(String name) {
        return new GatheringAnnotationVisitor("", values -> annotationDetails.add(name, values.toString()));
    }

    @Override
    public AnnotationVisitor visitAnnotation(String name, String descriptor) {
        return new GatheringAnnotationVisitor("", values -> annotationDetails.add(name, asDescriptorValue(descriptor, values)));
    }

    @Override
    public void visitEnd() {
        details.addAnnotation(annotationDetails);
    }
}
