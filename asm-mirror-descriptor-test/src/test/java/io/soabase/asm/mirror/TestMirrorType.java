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
package io.soabase.asm.mirror;

import io.soabase.asm.mirror.test.mirrortype.MirrorTypeCheck;
import io.soabase.asm.mirror.test.processor.visitor.TypeVisitor;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Type;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TestMirrorType {
    @Test
    public void testMirrorTypeCheck() {
        String asmGenerated = getAsmGenerated(MirrorTypeCheck.class);
        String soaGenerated = TestOutput.getSoabaseGenerated(MirrorTypeCheck.class, "mirror");
        Assert.assertEquals(asmGenerated, soaGenerated);
    }

    private String getAsmGenerated(Class clazz) {
        TypeVisitor typeVisitor = new TypeVisitor();

        typeVisitor.accept("== 01 class ==", Type.getType(clazz));
        typeVisitor.accept("== 02 ctors ==", Stream.of(clazz.getDeclaredConstructors())
                .map(Type::getType)
                .collect(Collectors.toList()));
        typeVisitor.accept("== 03 methods ==", Stream.of(clazz.getDeclaredMethods())
                .map(Type::getType)
                .collect(Collectors.toList()));
        typeVisitor.accept("== 04 returns ==", Stream.of(clazz.getDeclaredMethods())
                .map(Type::getReturnType)
                .collect(Collectors.toList()));

        return typeVisitor.toString().trim();
    }
}
