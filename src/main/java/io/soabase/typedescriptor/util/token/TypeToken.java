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

import io.soabase.typedescriptor.util.FormatUtil;

import java.util.List;

public class TypeToken implements Token {
    private final String classFqn;
    private final List<Token> typeArguments;

    public TypeToken(String classFqn, List<Token> typeArguments) {
        this.classFqn = classFqn;
        this.typeArguments = typeArguments;
    }

    @Override
    public void appendTo(StringBuilder str) {
        str.append('L').append(FormatUtil.toSlashFormat(classFqn)).append(';');
    }

    @Override
    public void appendToForSignature(StringBuilder str) {
        str.append('L').append(FormatUtil.toSlashFormat(classFqn));
        typeArguments.forEach(token -> {
            str.append('<');
            token.appendToForSignature(str);
            str.append(">;");
        });
    }

    @Override
    public boolean hasTypeArguments() {
        return !typeArguments.isEmpty();
    }
}
