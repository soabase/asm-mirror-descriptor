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

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Tokenizer {
    private static final Map<TypeKind, Function<TypeMirror, Token>> tokensMap;

    static {
        Map<TypeKind, Function<TypeMirror, Token>> map = new HashMap<>();
        map.put(TypeKind.BOOLEAN, __ -> BooleanToken.instance);
        map.put(TypeKind.BYTE, __ -> ByteToken.instance);
        map.put(TypeKind.SHORT, __ -> ShortToken.instance);
        map.put(TypeKind.INT, __ -> IntToken.instance);
        map.put(TypeKind.LONG, __ -> LongToken.instance);
        map.put(TypeKind.CHAR, __ -> CharToken.instance);
        map.put(TypeKind.FLOAT, __ -> FloatToken.instance);
        map.put(TypeKind.DOUBLE, __ -> DoubleToken.instance);
        map.put(TypeKind.ARRAY, Tokenizer::arrayToken);
        map.put(TypeKind.DECLARED, Tokenizer::typeToken);
        tokensMap = Collections.unmodifiableMap(map);
    }

    public static List<Token> buildFromElements(List<? extends Element> elements) {
        return elements.stream()
                .map(Element::asType)
                .map(Tokenizer::build)
                .collect(Collectors.toList());
    }

    public static List<Token> build(List<TypeMirror> types) {
        return types.stream().map(Tokenizer::build).collect(Collectors.toList());
    }

    public static Token build(TypeMirror type) {
        Function<TypeMirror, Token> proc = tokensMap.get(type.getKind());
        if (proc == null) {
            throw new Error();  // TODO
        }
        return proc.apply(type);
    }

    private static Token arrayToken(TypeMirror type) {
        // TODO
        return new ArrayToken();
    }

    private static Token typeToken(TypeMirror type) {
        DeclaredType declaredType = (DeclaredType) type;
        TypeElement typeElement = (TypeElement) declaredType.asElement();
        List<Token> typeArguments;
        if (declaredType.getTypeArguments().isEmpty()) {
            typeArguments = Collections.emptyList();
        } else {
            typeArguments = declaredType.getTypeArguments().stream()
                    .map(Tokenizer::build)
                    .collect(Collectors.toList());
        }
        return new TypeToken(typeElement.getQualifiedName().toString(), typeArguments);
    }

    private Tokenizer() {
    }
}
