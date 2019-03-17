package io.soabase.asm.mirror.descriptor.test;

import io.soabase.asm.mirror.descriptor.Util;
import io.soabase.asm.mirror.descriptor.test.classes.ComplexGeneric;
import io.soabase.asm.mirror.descriptor.test.classes.SimpleGeneric;
import io.soabase.asm.mirror.descriptor.test.classes.SimplePojo;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureWriter;
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

    @Test
    public void testComplexGeneric() {
        String asmGenerated = getAsmGenerated(ComplexGeneric.class);
        new SignatureReader("<T::Ljava/util/List<Ljava/util/Map<Ljava/lang/String;Ljava/util/List<TT;>;>;>;U:Lio/soabase/asm/mirror/descriptor/test/classes/ComplexGeneric<TT;TU;TV;>;V:Ljava/lang/Object;>Ljava/lang/Object;").accept(new SignatureWriter());
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
