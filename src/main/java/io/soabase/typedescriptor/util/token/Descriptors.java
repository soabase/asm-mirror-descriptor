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
package io.soabase.typedescriptor.util.token;

import java.util.List;

public class Descriptors {
    public static String toDescription(Token token) {
        StringBuilder str = new StringBuilder();
        token.appendTo(str);
        return str.toString();
    }

    public static String toDescription(List<Token> tokens) {
        StringBuilder str = new StringBuilder();
        tokens.forEach(t -> t.appendTo(str));
        return str.toString();
    }

    public static String toSignature(Token token) {
        if (token.hasTypeArguments()) {
            StringBuilder str = new StringBuilder();
            token.appendToForSignature(str);
            return str.toString();
        }
        return null;
    }

    public static String toSignature(List<Token> tokens) {
        boolean[] hasTypeArguments = new boolean[]{false};
        StringBuilder str = new StringBuilder();
        tokens.forEach(t -> {
            t.appendToForSignature(str);
            if (t.hasTypeArguments()) {
                hasTypeArguments[0] = true;
            }
        });
        return hasTypeArguments[0] ? str.toString() : null;
    }

    private Descriptors() {
    }
}
