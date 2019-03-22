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
package io.soabase.asm.mirror.test.mirrortype;

import io.soabase.asm.mirror.test.processor.DescriptorTest;
import io.soabase.asm.mirror.test.processor.MirrorTypeTest;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.function.Function;
import java.util.stream.Stream;

@MirrorTypeTest
@DescriptorTest
public class MirrorTypeCheck<T> extends InputStream implements Observer, Comparable<MirrorTypeCheck<T>> {
    private final List<T> list;

    public MirrorTypeCheck() {
        this(new ArrayList<T>());
    }

    public MirrorTypeCheck(List<T> list) {
        this.list = list;
    }

    public T get(int i) {
        return list.get(i);
    }

    public <X> X maker(int i, X hey) {
        return null;
    }

    public <U> Stream<U> map(Function<T, U> mapper) {
        return list.stream().map(mapper);
    }

    @Override
    public void update(Observable o, Object arg) {
    }

    @Override
    public int read() throws IOException {
        return 0;
    }

    @Override
    public int compareTo(MirrorTypeCheck<T> o) {
        return 0;
    }
}
