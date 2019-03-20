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
import org.objectweb.asm.Type;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

public class SignatureMirrorType {
    private final String signature;

    /**
     * Returns the descriptor corresponding to the type mirror.
     *
     * @param typeMirror type mirror
     * @return the descriptor corresponding to the given class.
     */
    public static String getSignature(ProcessingEnvironment processingEnv, TypeMirror typeMirror) {
        MirrorSignatures mirrorSignatures = new MirrorSignatures(processingEnv);
        return mirrorSignatures.typeSignature(typeMirror);
    }

    /**
     * Returns the {@link SignatureMirrorType} corresponding to the given type descriptor or type signature.
     *
     * @param signature a field or method type descriptor or type signature
     * @return the {@link SignatureMirrorType} corresponding to the given type descriptor or type signature.
     */
    public static SignatureMirrorType getType(String signature) {
        return new SignatureMirrorType(signature);
    }

    /**
     * Returns the {@link SignatureMirrorType} corresponding to the type mirror.
     *
     * @param typeMirror type mirror
     * @return the {@link SignatureMirrorType} corresponding to the given type mirror.
     */
    public static SignatureMirrorType getType(ProcessingEnvironment processingEnv, TypeMirror typeMirror) {
        return getType(getSignature(processingEnv, typeMirror));
    }

    /**
     * Returns the {@link SignatureMirrorType} corresponding to the element
     *
     * @param element element
     * @return the {@link SignatureMirrorType} corresponding to the given type element or null if element is invalid
     */
    public static SignatureMirrorType getType(ProcessingEnvironment processingEnv, Element element) {
        MirrorSignatures mirrorSignatures = new MirrorSignatures(processingEnv);
        switch (element.getKind()) {
            case ENUM:
            case CLASS:
            case INTERFACE: {
                return getType(mirrorSignatures.classSignature(element.asType()));
            }

            case METHOD:
            case CONSTRUCTOR: {
                ExecutableElement executableElement = (ExecutableElement) element;
                TypeMirror[] typeParameters = executableElement.getTypeParameters().stream()
                        .map(Element::asType)
                        .toArray(TypeMirror[]::new);
                TypeMirror[] parameters = executableElement.getParameters().stream()
                        .map(Element::asType)
                        .toArray(TypeMirror[]::new);
                return getType(mirrorSignatures.methodTypeSignature(typeParameters, parameters, executableElement.getReturnType()));
            }
        }
        return null;
    }

    /**
     * Returns the {@link SignatureMirrorType} corresponding to the return type of the given method.
     *
     * @param method a method.
     * @return the {@link SignatureMirrorType} corresponding to the return type of the given method.
     */
    public static SignatureMirrorType getReturnType(ProcessingEnvironment processingEnv, ExecutableElement method) {
        MirrorSignatures mirrorSignatures = new MirrorSignatures(processingEnv);
        return getType(mirrorSignatures.returnTypeSignature(method.getReturnType()));
    }

    public String getSignature() {
        return signature;
    }

    @Override
    public String toString() {
        return signature;
    }

    private SignatureMirrorType(String signature) {
        this.signature = signature;
    }
}
