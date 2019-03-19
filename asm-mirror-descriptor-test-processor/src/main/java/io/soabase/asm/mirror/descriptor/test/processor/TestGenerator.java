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
package io.soabase.asm.mirror.descriptor.test.processor;

import io.soabase.asm.mirror.descriptor.ClassMirrorReader;
import io.soabase.asm.mirror.descriptor.Util;
import io.soabase.asm.mirror.descriptor.test.processor.visitor.TestClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureVisitor;
import org.objectweb.asm.signature.SignatureWriter;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

@SupportedAnnotationTypes("io.soabase.asm.mirror.descriptor.test.processor.GeneratorTest")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class TestGenerator extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment environment) {
        annotations.forEach(annotation -> {
            Set<? extends Element> elementsAnnotatedWith = environment.getElementsAnnotatedWith(annotation);
            elementsAnnotatedWith.forEach(element -> {
                if (element.getKind() == ElementKind.INTERFACE) {
                    TypeElement typeElement = (TypeElement) element;

                    String addValueDescriptor;
                    String ctorDescriptor;
                    try {
                        addValueDescriptor = Type.getMethodDescriptor(GeneratorBase.class.getMethod("addValue", String.class));
                        ctorDescriptor = Type.getConstructorDescriptor(GeneratorBase.class.getConstructor());
                    } catch (NoSuchMethodException e) {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage());
                        return;
                    }

                    String packageName = typeElement.getEnclosingElement().toString();
                    String className = typeElement.getSimpleName().toString() + "Gen";
                    String classFqn = packageName + "." + className;
                    TestClassWriter classWriter = new TestClassWriter() {
                        @Override
                        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                            access = access & ~Opcodes.ACC_ABSTRACT;
                            interfaces = new String[]{Util.toSlash(typeElement.getQualifiedName().toString())};
                            superName = Util.toSlash(GeneratorBase.class.getName());
                            super.visit(version, access, Util.toSlash(classFqn), signature, superName, interfaces);

                            MethodVisitor methodVisitor = super.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
                            methodVisitor.visitCode();
                            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                            methodVisitor.visitMethodInsn(Opcodes.INVOKESPECIAL, Util.toSlash(GeneratorBase.class.getName()), "<init>", ctorDescriptor, false);
                            methodVisitor.visitInsn(Opcodes.RETURN);
                            methodVisitor.visitMaxs(1, 1);
                            methodVisitor.visitEnd();
                        }

                        @Override
                        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                            access = access & ~Opcodes.ACC_ABSTRACT;
                            MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                            methodVisitor.visitCode();
                            methodVisitor.visitVarInsn(Opcodes.ALOAD, 0);
                            methodVisitor.visitLdcInsn("This is " + name);
                            methodVisitor.visitMethodInsn(Opcodes.INVOKEVIRTUAL, Util.toSlash(classFqn), "addValue", addValueDescriptor, false);
                            methodVisitor.visitInsn(Opcodes.RETURN);
                            methodVisitor.visitMaxs(1, 1);
                            methodVisitor.visitEnd();
                            return methodVisitor;
                        }
                    };
                    ClassMirrorReader reader = new ClassMirrorReader(processingEnv, typeElement);
                    reader.accept(classWriter);

                    try (OutputStream out = processingEnv.getFiler().createClassFile(classFqn).openOutputStream()) {
                        out.write(classWriter.toBytes());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        });
        return true;
    }
}
