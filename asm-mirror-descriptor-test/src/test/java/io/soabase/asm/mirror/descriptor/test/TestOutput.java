package io.soabase.asm.mirror.descriptor.test;

import io.soabase.asm.mirror.descriptor.Util;
import io.soabase.asm.mirror.descriptor.test.classes.SimpleGeneric;
import io.soabase.asm.mirror.descriptor.test.classes.SimplePojo;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

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

    private String getAsmGenerated(Class clazz) {
        try {
            ClassReader reader = new ClassReader(Util.toSlash(clazz.getName()));
            StringWriter stringWriter = new StringWriter();
            TraceClassVisitor traceClassVisitor = new TraceClassVisitor(new PrintWriter(stringWriter));
            reader.accept(traceClassVisitor, SKIP_CODE | SKIP_DEBUG);
            return stringWriter.toString();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }
}
