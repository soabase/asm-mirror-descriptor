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
package io.soabase.asm.mirror.descriptor.test;

import io.soabase.asm.mirror.descriptor.test.processor.DescriptorTest;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@DescriptorTest
public class ComplexGeneric<T extends List<Map<String, List<T>>>, U extends ComplexGeneric<T, U, V>, V> {
    @Resource(name = "hey", shareable = false)
    private transient volatile T theT;
    private transient volatile U theU;

    public static <Y extends List<Map<String, List<Y>>>, Z extends ComplexGeneric<Y, Z, A>, A> ComplexGeneric<Y, Z, A> builder(Y theT, Z theU) {
        return new ComplexGeneric<>(theT, theU);
    }

    public ComplexGeneric(T theT, U theU) {
        this.theT = theT;
        this.theU = theU;
    }

    public T getTheT() {
        return theT;
    }

    public void setTheT(T theT) {
        this.theT = theT;
    }

    public U getTheU() {
        return theU;
    }

    public void setTheU(U theU) {
        this.theU = theU;
    }

    public void takeIt(Collection<? extends T> collection) {

    }

    public Collection<? super T> leaveIt() {
        return null;
    }

    public void something(Collection<?> hey) {
    }

    public <X extends Map<X, List<Y>>, Y> void somethingElse(Collection<? super X> hey) {
    }
}
