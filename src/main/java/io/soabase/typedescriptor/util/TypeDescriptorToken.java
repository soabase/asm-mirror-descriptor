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
package io.soabase.typedescriptor.util;

public enum TypeDescriptorToken {
    BOOLEAN() {
        @Override
        public String value(String component) {
            return "Z";
        }
    },

    BYTE() {
        @Override
        public String value(String component) {
            return "B";
        }
    },

    SHORT() {
        @Override
        public String value(String component) {
            return "S";
        }
    },

    INT() {
        @Override
        public String value(String component) {
            return "I";
        }
    },

    LONG() {
        @Override
        public String value(String component) {
            return "J";
        }
    },

    CHAR() {
        @Override
        public String value(String component) {
            return "C";
        }
    },

    FLOAT() {
        @Override
        public String value(String component) {
            return "F";
        }
    },

    DOUBLE() {
        @Override
        public String value(String component) {
            return "D";
        }
    },

    ARRAY() {
        @Override
        public String value(String component) {
            return "[" + component;
        }
    },

    TYPE() {
        @Override
        public String value(String component) {
            return FormatUtil.toTypeFormat(component, true);
        }
    }

    ;

    public abstract String value(String component);

    public String value() {
        return value("");
    }
}
