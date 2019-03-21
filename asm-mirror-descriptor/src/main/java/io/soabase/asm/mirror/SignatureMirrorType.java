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

/**
 * There is no version of this in the ASM library. This is similar to {@link Type} but
 * builds parameterized type signatures.
 */
public class SignatureMirrorType {
    /**
     * Returns the signature corresponding to the type mirror.
     *
     * @param typeMirror type mirror
     * @return the signature corresponding to the given class.
     */
    public static String getSignature(ProcessingEnvironment processingEnv, TypeMirror typeMirror) {
        MirrorSignatures mirrorSignatures = new MirrorSignatures(processingEnv);
        return mirrorSignatures.typeSignature(typeMirror);
    }

    /**
     * Returns the signature corresponding to the type mirror.
     *
     * @param typeMirror type mirror
     * @return the signature corresponding to the given type mirror.
     */
    public static String getType(ProcessingEnvironment processingEnv, TypeMirror typeMirror) {
        return getSignature(processingEnv, typeMirror);
    }

    /**
     * Returns the signature corresponding to the element
     *
     * @param element element
     * @return the signature corresponding to the given type element or null if element is invalid
     */
    public static String getType(ProcessingEnvironment processingEnv, Element element) {
        MirrorSignatures mirrorSignatures = new MirrorSignatures(processingEnv);
        switch (element.getKind()) {
            case ENUM:
            case CLASS:
            case INTERFACE: {
                return mirrorSignatures.classSignature(element.asType());
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
                return mirrorSignatures.methodTypeSignature(typeParameters, parameters, executableElement.getReturnType());
            }
        }
        return null;
    }

    /**
     * Returns the signature corresponding to the return type of the given method.
     *
     * @param method a method.
     * @return the signature corresponding to the return type of the given method.
     */
    public static String getReturnType(ProcessingEnvironment processingEnv, ExecutableElement method) {
        MirrorSignatures mirrorSignatures = new MirrorSignatures(processingEnv);
        return mirrorSignatures.returnTypeSignature(method.getReturnType());
    }

    private SignatureMirrorType() {
    }
}
