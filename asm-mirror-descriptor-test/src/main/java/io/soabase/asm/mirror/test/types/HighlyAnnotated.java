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
package io.soabase.asm.mirror.test.types;

import io.soabase.asm.mirror.test.processor.DescriptorTest;

@DescriptorTest
@TestAnnotation(value = "class", choice = TestEnum.TWO, list = {"a", "b", "c"})
public class HighlyAnnotated<
        @TestAnnotation("type-param-t") T,
        @TestAnnotation("type-param-u") U extends /* @TestAnnotation("extends-type-param") */ T // NOTE I reported https://bugs.java.com/bugdatabase/view_bug.do?bug_id=9059851
        > {
    @TestAnnotation(value = "field", choice = TestEnum.ONE, list = {"x", "y"})
    private String s;

    @TestAnnotation(value = "ctor", choice = TestEnum.THREE, list = "d")
    public HighlyAnnotated() {
    }

    @TestNestedAnnotation(nested = @TestAnnotation(value = "methodTwo", choice = TestEnum.TWO))
    public void methodTwo() {
    }

    @TestAnnotation(value = "methodOne")
    public <@TestAnnotation(value = "methodOne-T") X> String methodOne(
            @TestAnnotation(value = "methodOne-param1")
            int param1,

            @TestAnnotation(value = "methodOne-param2")
            String param2
    ) {
        return null;
    }
}
