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
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.ModuleVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.TypePath;

public class TestClassWriter extends ClassVisitor {
    protected final ClassWriter classWriter;

    public TestClassWriter() {
        super(Opcodes.ASM7);
        classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    }

    public byte[] toBytes() {
        return classWriter.toByteArray();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        classWriter.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public void visitSource(String source, String debug) {
        classWriter.visitSource(source, debug);
    }

    @Override
    public ModuleVisitor visitModule(String name, int access, String version) {
        return classWriter.visitModule(name, access, version);
    }

    @Override
    public void visitNestHost(String nestHost) {
        classWriter.visitNestHost(nestHost);
    }

    @Override
    public void visitOuterClass(String owner, String name, String descriptor) {
        classWriter.visitOuterClass(owner, name, descriptor);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
        return classWriter.visitAnnotation(descriptor, visible);
    }

    @Override
    public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String descriptor, boolean visible) {
        return classWriter.visitTypeAnnotation(typeRef, typePath, descriptor, visible);
    }

    @Override
    public void visitAttribute(Attribute attribute) {
        classWriter.visitAttribute(attribute);
    }

    @Override
    public void visitNestMember(String nestMember) {
        classWriter.visitNestMember(nestMember);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        classWriter.visitInnerClass(name, outerName, innerName, access);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
        return classWriter.visitField(access, name, descriptor, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        return classWriter.visitMethod(access, name, descriptor, signature, exceptions);
    }

    @Override
    public void visitEnd() {
        classWriter.visitEnd();
    }
}
