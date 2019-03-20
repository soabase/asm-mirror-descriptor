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
package io.soabase.asm.mirror.test.types;

import io.soabase.asm.mirror.test.processor.DescriptorTest;

import java.io.EOFException;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Arrays;

@DescriptorTest
public class SimplePojo implements Serializable {
    private static final int QTY = 10;
    private String str;
    private int value;
    private double[] tab;
    private BigInteger[] bigIntegers;

    public SimplePojo() {
        this("", 0, null, null);
    }

    public SimplePojo(String str, int value, double[] tab, BigInteger[] bigIntegers) {
        this.str = str;
        this.value = value;
        this.tab = tab;
        this.bigIntegers = bigIntegers;
    }

    public String getStr() {
        return str;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public double[] getTab() {
        return tab;
    }

    public void setTab(double[] tab) {
        this.tab = tab;
    }

    public BigInteger[] getBigIntegers() {
        return bigIntegers;
    }

    public void setBigIntegers(BigInteger[] bigIntegers) {
        this.bigIntegers = bigIntegers;
    }

    public short throwSomething(char c, int i, byte b, short s, double d, float f, long l) throws IOException, EOFException {
        return 0;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimplePojo that = (SimplePojo) o;

        if (value != that.value) return false;
        if (str != null ? !str.equals(that.str) : that.str != null) return false;
        if (!Arrays.equals(tab, that.tab)) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(bigIntegers, that.bigIntegers);
    }

    @Override
    public int hashCode() {
        int result = str != null ? str.hashCode() : 0;
        result = 31 * result + value;
        result = 31 * result + Arrays.hashCode(tab);
        result = 31 * result + Arrays.hashCode(bigIntegers);
        return result;
    }
}
