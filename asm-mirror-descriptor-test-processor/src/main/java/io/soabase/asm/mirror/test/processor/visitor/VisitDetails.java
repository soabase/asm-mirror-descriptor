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
package io.soabase.asm.mirror.test.processor.visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class VisitDetails implements Comparable<VisitDetails> {
    private final String[] values;
    private final List<AnnotationDetails> annotations = new ArrayList<>();

    public VisitDetails(String... values) {
        this.values = values;
    }

    public static String toString(String heading, List<VisitDetails> details) {
        List<VisitDetails> copy = new ArrayList<>(details);
        Collections.sort(copy);
        String base = (heading + copy.stream().map(VisitDetails::toString).collect(Collectors.joining("\n", "\n", ""))).trim();
        return base + "\n\n";
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append(String.join("\n", values));
        if (values.length > 0) {
            str.append("\n");
        }
        if (!annotations.isEmpty()) {
            List<AnnotationDetails> copy = new ArrayList<>(annotations);
            Collections.sort(copy);
            str.append("annotations:\n");
            str.append(copy.stream().map(AnnotationDetails::toString).collect(Collectors.joining("\n", "", "")));
        }
        return str.toString();
    }

    public void addAnnotation(AnnotationDetails annotation) {
        annotations.add(annotation);
    }

    public String firstValue() {
        return (values.length > 0) ? values[0] : "";
    }

    @Override
    public int compareTo(VisitDetails o) {
        return firstValue().compareTo(o.firstValue());
    }
}
