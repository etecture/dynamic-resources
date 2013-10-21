/*
 * This file is part of the ETECTURE Open Source Community Projects.
 *
 * Copyright (c) 2013 by:
 *
 * ETECTURE GmbH
 * Darmstädter Landstraße 112
 * 60598 Frankfurt
 * Germany
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the author nor the names of its contributors may be
 *    used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package de.etecture.opensource.dynamicresources.extension;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author rhk
 */
public class VersionNumberRangeExpressionTest {

    @Test
    public void testConcreteParsing() {
        isEqualTo("1", 1, 0, 0, true, 1, 0, 0, true);
        isEqualTo("1.0", 1, 0, 0, true, 1, 0, 0, true);
        isEqualTo("1.1", 1, 1, 0, true, 1, 1, 0, true);
        isEqualTo("1.0.0", 1, 0, 0, true, 1, 0, 0, true);
        isError("");
        isError("1.");
        isError("1.0.");
        isError("1.0.0.");
        isError("A");
        isError("1.a");
        isError("1.0.a");
        isError("1.0.0.b");
        isError("1.aaa");
    }

    @Test
    public void testInclusiveRangeParsing() {
        isEqualTo("[1,2]", 1, 0, 0, true, 2, Integer.MAX_VALUE,
                Integer.MAX_VALUE, true);
        isEqualTo("[1.1,2]", 1, 1, 0, true, 2, Integer.MAX_VALUE,
                Integer.MAX_VALUE, true);
        isEqualTo("[1.1.1,2]", 1, 1, 1, true, 2, Integer.MAX_VALUE,
                Integer.MAX_VALUE, true);
        isEqualTo("[1,2.0]", 1, 0, 0, true, 2, 0, Integer.MAX_VALUE,
                true);
        isEqualTo("[1.1,2.1.0]", 1, 1, 0, true, 2, 1, 0, true);
        isEqualTo("[1.1.1,2.1.1]", 1, 1, 1, true, 2, 1, 1, true);
    }

    @Test
    public void testInfiniteRangeParsing() {
        isEqualTo("(,)", 0, 0, 0, false, Integer.MAX_VALUE,
                0, 0, false);
    }

    @Test
    public void testInfiniteLowerRangeParsing() {
        isEqualTo("(,2]", 0, 0, 0, false, 2, Integer.MAX_VALUE,
                Integer.MAX_VALUE, true);
        isEqualTo("(,2.0]", 0, 0, 0, false, 2, 0, Integer.MAX_VALUE,
                true);
        isEqualTo("(,2.1.0]", 0, 0, 0, false, 2, 1, 0, true);
        isEqualTo("(,2.1.1]", 0, 0, 0, false, 2, 1, 1, true);
        isEqualTo("(,2)", 0, 0, 0, false, 2, 0,
                0, false);
        isEqualTo("(,2.0)", 0, 0, 0, false, 2, 0, 0,
                false);
        isEqualTo("(,2.1.0)", 0, 0, 0, false, 2, 1, 0, false);
        isEqualTo("(,2.1.1)", 0, 0, 0, false, 2, 1, 1, false);
    }

    @Test
    public void testInfiniteUpperRangeParsing() {
        isEqualTo("[1,)", 1, 0, 0, true, Integer.MAX_VALUE,
                0, 0, false);
        isEqualTo("[1.1,)", 1, 1, 0, true, Integer.MAX_VALUE,
                0, 0, false);
        isEqualTo("[1.1.1,)", 1, 1, 1, true, Integer.MAX_VALUE,
                0, 0, false);
        isEqualTo("(1,)", 1, 0, 0, false, Integer.MAX_VALUE,
                0, 0, false);
        isEqualTo("(1.1,)", 1, 1, 0, false, Integer.MAX_VALUE,
                0, 0, false);
        isEqualTo("(1.1.1,)", 1, 1, 1, false, Integer.MAX_VALUE,
                0, 0, false);
    }

    @Test
    public void testExclusiveRangeParsing() {
        isEqualTo("]1,2[", 1, 0, 0, false, 2, 0, 0, false);
        isEqualTo("]1.1,2[", 1, 1, 0, false, 2, 0, 0, false);
        isEqualTo("]1.1.1,2[", 1, 1, 1, false, 2, 0, 0, false);
        isEqualTo("]1,2.0[", 1, 0, 0, false, 2, 0, 0,
                false);
        isEqualTo("]1.1,2.1.0[", 1, 1, 0, false, 2, 1, 0, false);
        isEqualTo("]1.1.1,2.1.1[", 1, 1, 1, false, 2, 1, 1, false);
    }

    @Test
    public void testMixedRangeParsing() {
        isEqualTo("]1,2]", 1, 0, 0, false, 2, Integer.MAX_VALUE,
                Integer.MAX_VALUE, true);
        isEqualTo("]1.1,2]", 1, 1, 0, false, 2, Integer.MAX_VALUE,
                Integer.MAX_VALUE, true);
        isEqualTo("]1.1.1,2]", 1, 1, 1, false, 2, Integer.MAX_VALUE,
                Integer.MAX_VALUE, true);
        isEqualTo("]1,2.0]", 1, 0, 0, false, 2, 0, Integer.MAX_VALUE,
                true);
        isEqualTo("]1.1,2.1.0]", 1, 1, 0, false, 2, 1, 0, true);
        isEqualTo("]1.1.1,2.1.1]", 1, 1, 1, false, 2, 1, 1, true);

        isEqualTo("[1,2[", 1, 0, 0, true, 2, 0, 0, false);
        isEqualTo("[1.1,2[", 1, 1, 0, true, 2, 0, 0, false);
        isEqualTo("[1.1.1,2[", 1, 1, 1, true, 2, 0, 0, false);
        isEqualTo("[1,2.0[", 1, 0, 0, true, 2, 0, 0, false);
        isEqualTo("[1.1,2.1.0[", 1, 1, 0, true, 2, 1, 0, false);
        isEqualTo("[1.1.1,2.1.1[", 1, 1, 1, true, 2, 1, 1, false);
    }

    @Test
    public void testConcreteMatching() {
        doesMatch("1", "1");
        doesMatch("1", "1.0");
        doesMatch("1", "1.0.0");

        doesMatch("1.0", "1");
        doesMatch("1.0", "1.0");
        doesMatch("1.0", "1.0.0");

        doesMatch("1.0.0", "1");
        doesMatch("1.0.0", "1.0");
        doesMatch("1.0.0", "1.0.0");

        doesNotMatch("2", "1");
        doesNotMatch("2", "1.0");
        doesNotMatch("2", "1.0.0");

        doesNotMatch("1.1", "1");
        doesNotMatch("1.1", "1.0");
        doesNotMatch("1.1", "1.0.0");

        doesNotMatch("1.0.1", "1");
        doesNotMatch("1.0.1", "1.0");
        doesNotMatch("1.0.1", "1.0.0");
    }

    @Test
    public void testRangeMatching() {
        doesMatch("(,)", "1");
        doesMatch("(,)", "1.0");
        doesMatch("(,)", "1.0.0");

        doesMatch("[1,)", "1");
        doesMatch("[1,)", "1.0");
        doesMatch("[1,)", "1.0.0");

        doesNotMatch("]1,)", "1");
        doesNotMatch("]1,)", "1.0");
        doesNotMatch("]1,)", "1.0.0");

        doesMatch("]1,)", "2");
        doesMatch("]1,)", "1.1");
        doesMatch("]1,)", "1.0.1");

        doesMatch("[1.1,)", "2");
        doesMatch("[1.1,)", "1.1");
        doesMatch("[1.1,)", "1.1.5");

        doesNotMatch("]1.1,)", "1");
        doesNotMatch("]1.1,)", "1.1");
        doesNotMatch("]1.1,)", "1.1.0");

        doesMatch("]1.1,)", "2");
        doesMatch("]1.1,)", "1.2");
        doesMatch("]1.1,)", "1.1.1");

        doesMatch("[1,3[", "2");
        doesMatch("[1,3[", "1.1");
        doesMatch("[1,3[", "1.0.1");

        doesNotMatch("[1,3[", "3");
        doesNotMatch("[1,3.0[", "3.0");
        doesNotMatch("[1,3.0[", "3.0.1");
        doesNotMatch("[1,3.1.2[", "3.1.2");
        doesNotMatch("[1,3.1.2[", "3.1.3");

        doesMatch("[1,3.1[", "3.0.9999");
        doesMatch("[1,3.1.1[", "3.1.0");
    }

    private void doesMatch(String expression, String version) {
        VersionNumberRangeExpression exp = new VersionNumberRangeExpression(
                expression);
        assertTrue(String.format("%s does not match expression %s", version,
                expression), exp.includes(version));
    }

    private void doesNotMatch(String expression, String version) {
        VersionNumberRangeExpression exp = new VersionNumberRangeExpression(
                expression);
        assertFalse(String.format("%s does errornous match expression %s",
                version, expression), exp.includes(version));
    }

    private void isEqualTo(String expression, int lowerMajor,
            int lowerMinor, int lowerRelease, boolean lowerInclusive,
            int upperMajor, int upperMinor, int upperRelease,
            boolean upperInclusive) {
        VersionNumberRangeExpression exp = new VersionNumberRangeExpression(
                expression);
        assertThat(exp.lowerMajor, equalTo(lowerMajor));
        assertThat(exp.lowerMinor, equalTo(lowerMinor));
        assertThat(exp.lowerRelease, equalTo(lowerRelease));
        assertThat(exp.lowerInclusive, equalTo(lowerInclusive));
        assertThat(exp.upperMajor, equalTo(upperMajor));
        assertThat(exp.upperMinor, equalTo(upperMinor));
        assertThat(exp.upperRelease, equalTo(upperRelease));
        assertThat(exp.upperInclusive, equalTo(upperInclusive));
    }

    private void isError(String expression) {
        try {
            VersionNumberRangeExpression exp = new VersionNumberRangeExpression(
                    expression);
            fail("must throw IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            // good!
        }
    }
}
