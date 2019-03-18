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
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestClassVisitor extends ClassVisitor {
    private VisitDetails classDetails;
    private final List<VisitDetails> fieldDetails = new ArrayList<>();
    private final List<VisitDetails> methodDetails = new ArrayList<>();

    public TestClassVisitor() {
        super(Opcodes.ASM7);
    }

    public VisitDetails getClassDetails() {
        return classDetails;
    }

    public List<VisitDetails> getMethodDetails() {
        return methodDetails;
    }

    public List<VisitDetails> getFieldDetails() {
        return fieldDetails;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("CLASS\n=====\n").append(classDetails).append("\n\n");

        str.append("FIELDS\n======\n");
        fieldDetails.forEach(v -> str.append(v.toString()).append("\n\n"));
        str.append('\n');

        str.append("METHODS\n=======\n");
        methodDetails.forEach(v -> str.append(v.toString()).append("\n\n"));

        return str.toString().trim();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        classDetails = new VisitDetails(name, asAccess(access), signature, superName, Arrays.toString(interfaces));
    }

    private String asAccess(int access) {
        return "access:" + access;
    }

    @Override
    public void visitSource(String source, String debug) {
    }

    @Override
    public ModuleVisitor visitModule(String name, int access, String version) {
        return null;
    }

    @Override
    public void visitNestHost(String nestHost) {
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return super.visitAnnotation(descriptor, visible);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return super.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
    }

    @Override
    public void visitNestMember(String nestMember) {
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        fieldDetails.add(new VisitDetails(name, asAccess(access), descriptor, signature, String.valueOf(value)));
        return null;    // TODO
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        methodDetails.add(new VisitDetails(name, asAccess(access), descriptor, signature, Arrays.toString(exceptions)));
        return null;
    }

    @Override
    public void visitEnd() {
    }
}
