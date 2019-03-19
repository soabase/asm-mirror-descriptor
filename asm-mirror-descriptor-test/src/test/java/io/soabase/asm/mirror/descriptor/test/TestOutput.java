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

import io.soabase.asm.mirror.descriptor.Util;
import io.soabase.asm.mirror.descriptor.test.classes.AsmGuideTypes;
import io.soabase.asm.mirror.descriptor.test.classes.ComplexExtends;
import io.soabase.asm.mirror.descriptor.test.classes.ComplexGeneric;
import io.soabase.asm.mirror.descriptor.test.classes.HighlyAnnotated;
import io.soabase.asm.mirror.descriptor.test.classes.SimpleGeneric;
import io.soabase.asm.mirror.descriptor.test.classes.SimplePojo;
import io.soabase.asm.mirror.descriptor.test.processor.visitor.TestClassVisitor;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.objectweb.asm.ClassReader.SKIP_CODE;
import static org.objectweb.asm.ClassReader.SKIP_DEBUG;

public class TestOutput {
    @Test
    public void testSimplePojo() {
        internalTest(SimplePojo.class);
    }

    @Test
    public void testSimpleGeneric() {
        internalTest(SimpleGeneric.class);
    }

    @Test
    public void testHighlyAnnotated() {
        internalTest(HighlyAnnotated.class);
    }

    @Test
    public void testComplexGeneric() {
        internalTest(ComplexGeneric.class);
    }

    @Test
    public void testAsmGuideTypes() {
        internalTest(AsmGuideTypes.class);
    }

    @Test
    public void testComplexExtends() {
        internalTest(ComplexExtends.class);
    }

    private void internalTest(Class clazz) {
        String asmGenerated = getAsmGenerated(clazz);
        String soabaseGenerated = getSoabaseGenerated(clazz);
        Assert.assertEquals(asmGenerated, soabaseGenerated);
    }

    private String getAsmGenerated(Class clazz) {
        try {
            ClassReader reader = new ClassReader(Util.toSlash(clazz.getName()));
            TestClassVisitor testClassVisitor = new TestClassVisitor();
            reader.accept(testClassVisitor, SKIP_CODE | SKIP_DEBUG);
            return testClassVisitor.toString();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    private String getSoabaseGenerated(Class clazz) {
        String name = "test/" + clazz.getSimpleName() + ".txt";
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(name);
        if (inputStream == null) {
            throw new AssertionError("Not found: " + name);
        }
        try (BufferedReader buffer = new BufferedReader(new InputStreamReader(inputStream))) {
            return buffer.lines().collect(Collectors.joining("\n")).trim();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
