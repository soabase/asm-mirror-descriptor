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
package io.soabase.asm.mirror;

import io.soabase.asm.mirror.util.MirrorSignatures;
import io.soabase.asm.mirror.util.Util;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypeReference;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * Corollary to {@link org.objectweb.asm.ClassReader} but for {@link TypeMirror}s/{@link Element}s.
 * A parser to make a {@link ClassVisitor} visit a TypeMirror/Element instance. Calls the
 * appropriate visit methods of a given {@link ClassVisitor} for each field, method and field encountered.
 * Note: there is not bytecode in mirrors so those visitor methods are never called.
 */
public class MirrorClassReader {
    private final TypeElement mainElement;
    private final MirrorSignatures mirrorSignatures;
    private final MirrorAnnotationReader annotationReader;
    private final MirrorMethodReader methodReader;
    private final MirrorFieldReader fieldReader;
    private final int classVersion;
    private final int extraAccessFlags;

    /**
     * New class reader for the given element. {@link ClassVisitor}s will be called
     * with class version {@link Opcodes#V1_8} and {@link Opcodes#ACC_SUPER} will be
     * added to the access flags.
     *
     * @param processingEnv current processing environment
     * @param element element
     */
    public MirrorClassReader(ProcessingEnvironment processingEnv, TypeElement element) {
        this(processingEnv, element, Opcodes.V1_8, Opcodes.ACC_SUPER);
    }

    /**
     * New class reader for the given element. {@link ClassVisitor}s will be called
     * with the given class version and extraAccessFlags will be
     * added to the access flags.
     *
     * @param processingEnv current processing environment
     * @param element element
     * @param classVersion class version to use
     * @param extraAccessFlags extra access flags to add or 0
     */
    public MirrorClassReader(ProcessingEnvironment processingEnv, TypeElement element, int classVersion, int extraAccessFlags) {
        this.mainElement = element;
        mirrorSignatures = new MirrorSignatures(processingEnv);
        annotationReader = new MirrorAnnotationReader(processingEnv, mirrorSignatures);
        methodReader = new MirrorMethodReader(mirrorSignatures, annotationReader);
        fieldReader = new MirrorFieldReader(mirrorSignatures, annotationReader);
        this.classVersion = classVersion;
        this.extraAccessFlags = extraAccessFlags;
    }

    /**
     * Returns the class's access flags (see {@link Opcodes}).
     *
     * @return the class access flags.
     */
    public int getAccess() {
        return Util.modifiersToAccessFlags(mainElement.getModifiers());
    }

    /**
     * Returns the internal name of the class (see {@link Type#getInternalName()}).
     *
     * @return the internal class name.
     */
    public String getClassName() {
        return Util.toSlash(mainElement.getQualifiedName().toString());
    }

    /**
     * Returns the internal of name of the super class (see {@link Type#getInternalName()}). For
     * interfaces, the super class is {@link Object}.
     *
     * @return the internal name of the super class
     */
    public String getSuperName() {
        if (mainElement.getSuperclass().getKind() == TypeKind.NONE) {
            return null;
        }
        Element element = ((DeclaredType) mainElement.getSuperclass()).asElement();
        return Util.toSlash(((TypeElement) element).getQualifiedName().toString());
    }

    /**
     * Returns the internal names of the implemented interfaces (see {@link Type#getInternalName()}).
     *
     * @return the internal names of the directly implemented interfaces.
     */
    public String[] getInterfaces() {
        return mainElement.getInterfaces().stream()
                .map(type -> {
                    Element element = ((DeclaredType) type).asElement();
                    return Util.toSlash(((TypeElement) element).getQualifiedName().toString());
                })
                .toArray(String[]::new);
    }

    /**
     * Makes the given visitor visit the Mirror/Element passed to the constructor of this
     * {@link MirrorClassReader}.
     *
     * @param classVisitor the visitor that must visit this class.
     */
    public void accept(ClassVisitor classVisitor) {
        int accessFlags = getAccess() | extraAccessFlags;
        String thisClass = getClassName();
        String superClass = getSuperName();
        String[] interfaces = getInterfaces();
        String signature = Util.hasTypeArguments(mainElement) ? mirrorSignatures.classSignature(mainElement.asType()) : null;
        classVisitor.visit(classVersion, accessFlags, thisClass, signature, superClass, interfaces);

        mainElement.getAnnotationMirrors().forEach(annotation -> annotationReader.readAnnotationValue(annotation, classVisitor::visitAnnotation));
        annotationReader.readTypeAnnotations(mainElement.getTypeParameters(), TypeReference.CLASS_TYPE_PARAMETER, classVisitor::visitTypeAnnotation);

        mainElement.getEnclosedElements().forEach(enclosed -> {
            switch (enclosed.getKind()) {
                case FIELD: {
                    fieldReader.readField(classVisitor, (VariableElement) enclosed);
                    break;
                }

                case CONSTRUCTOR:
                case METHOD: {
                    methodReader.readMethod(classVisitor, (ExecutableElement) enclosed);
                    break;
                }
            }
        });

        classVisitor.visitEnd();
    }
}
