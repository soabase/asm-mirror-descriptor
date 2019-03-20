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

import io.soabase.asm.mirror.test.generate.Model;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;

public class TestGenerated {
    @Test
    public void testModelGen() throws Exception {
        Model model = (Model) Class.forName("io.soabase.asm.mirror.test.generate.ModelGen").getConstructor().newInstance();
        model.itsMe();
        model.itsYou();
        Assert.assertEquals(model.toString(), Arrays.asList("This is itsMe", "This is itsYou").toString());
    }
}
