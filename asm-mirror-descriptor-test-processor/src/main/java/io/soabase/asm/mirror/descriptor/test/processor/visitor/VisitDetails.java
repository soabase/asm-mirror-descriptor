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

import java.util.ArrayList;
import java.util.List;

public class VisitDetails {
    private final String[] values;
    private final List<AnnotationDetails> annotations = new ArrayList<>();

    public VisitDetails(String... values) {
        this.values = values;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(String.join("\n", values));
        if (!annotations.isEmpty()) {
            str.append("\nannotations:\n");
            annotations.forEach(a -> str.append(a.toString()).append('\n'));
        }
        return str.toString();
    }

    public void addAnnotation(AnnotationDetails annotation) {
        annotations.add(annotation);
    }
}
