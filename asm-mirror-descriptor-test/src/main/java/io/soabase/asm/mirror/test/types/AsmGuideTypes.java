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
public class AsmGuideTypes<E, K, V> {
    public List<E> l1;
    public List<?> l2;
    public List<? extends Number> l3;
    public List<? super Integer> l4;
    public List<List<String>[]> l5;
    public OuterClass<K, V>.InnerClass<K>.InnerInnerClass<V> o1;
}
