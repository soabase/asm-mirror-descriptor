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

import java.util.List;

@DescriptorTest
public class ComplexExtends<T extends List<String>> extends BaseClass<T> implements MixinA<T>, MixinB<T> {
    @Override
    public void setT(T x) {
    }

    @Override
    public T getA() {
        return null;
    }

    @Override
    public T getB() {
        return null;
    }
}
