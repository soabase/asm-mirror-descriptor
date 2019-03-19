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

import java.util.Arrays;

class Format {
    private Format() {
    }

    static String asAccess(int access) {
        return "access:      " + access;
    }

    static String asConstant(Object constant) {
        return "const:       " + constant;
    }

    static String asName(String name) {
        return "name:        " + name;
    }

    static String asType(String type) {
        return "type:        " + type;
    }

    static String asDescriptor(String descriptor) {
        return "description: " + descriptor;
    }

    static String asSignature(String signature) {
        return "signature:   " + signature;
    }

    static String asExceptions(String[] exceptions) {
        return "exceptions:  " + Arrays.toString(exceptions);
    }

    static String asInterfaces(String[] interfaces) {
        return "interfaces:  " + Arrays.toString(interfaces);
    }

    static String asSuper(String name) {
        return "super:       " + name;
    }

    static String asValue(Object value) {
        return "value:       " + value;
    }

    static String asDescriptorValue(String descriptor, Object value) {
        return descriptor + "=" + value;
    }

    static String asNameDescriptor(String name, String descriptor) {
        return name + "@" + descriptor;
    }
}
