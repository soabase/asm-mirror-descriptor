package io.soabase.asm.mirror.descriptor.test;

import io.soabase.asm.mirror.descriptor.Util;
import io.soabase.asm.mirror.descriptor.test.classes.ComplexGeneric;
import io.soabase.asm.mirror.descriptor.test.classes.SimpleGeneric;
import io.soabase.asm.mirror.descriptor.test.classes.SimplePojo;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.stream.Collectors;

import static org.objectweb.asm.ClassReader.SKIP_CODE;
import static org.objectweb.asm.ClassReader.SKIP_DEBUG;

public class TestOutput {
    @Test
    public void testSimplePojo() {
        String asmGenerated = getAsmGenerated(SimplePojo.class);
    }

    @Test
    public void testSimpleGeneric() {
        String asmGenerated = getAsmGenerated(SimpleGeneric.class);
    }

    @Test
    public void testComplexGeneric() {
        String asmGenerated = getAsmGenerated(ComplexGeneric.class);
        String soabaseGenerated = getSoabaseGenerated(ComplexGeneric.class);
        Assert.assertEquals(asmGenerated, soabaseGenerated);
    }

    private String getAsmGenerated(Class clazz) {
        try {
            ClassReader reader = new ClassReader(Util.toSlash(clazz.getName()));
            StringWriter stringWriter = new StringWriter();
            TraceClassVisitor traceClassVisitor = new TraceClassVisitor(new PrintWriter(stringWriter));
            reader.accept(traceClassVisitor, SKIP_CODE | SKIP_DEBUG);
            return stringWriter.toString().trim();
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
