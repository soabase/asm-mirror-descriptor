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

import org.objectweb.asm.signature.SignatureWriter;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;

public class ElementDescriptorWriter {
    private final ProcessingEnvironment environment;

    public ElementDescriptorWriter(ProcessingEnvironment environment) {
        this.environment = environment;
    }

    public String executableElementDescriptor(ExecutableElement element) {
        SignatureWriter writer = new SignatureWriter();
        element.getParameters().forEach(parameter -> {
            TypeMirror type = parameter.asType();
            TypeMirror erasure = environment.getTypeUtils().erasure(type);
            writer.visitParameterType().visitClassBound().visitClassType(erasure.toString());
        });
        return writer.toString();
    }
}
