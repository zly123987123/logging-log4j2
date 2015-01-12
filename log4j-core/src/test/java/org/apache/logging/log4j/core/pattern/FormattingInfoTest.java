/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache license, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the license for the specific language governing permissions and
 * limitations under the license.
 */

package org.apache.logging.log4j.core.pattern;

import java.nio.ByteBuffer;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the FormattingInfo class.
 */
public class FormattingInfoTest {

    @Test
    public void testFormattingInfoValidConstructorParams() {
        int[][] valid = new int[][] { //
        //
                new int[] { 0, 10 }, //
                new int[] { 0, Integer.MAX_VALUE }, //
                new int[] { 10, 10 }, // min=max
                new int[] { 10, 11 }, //
        };
        for (int i = 0; i < valid.length; i++) {
            new FormattingInfo(true, valid[i][0], valid[i][1]);
        }
        for (int i = 0; i < valid.length; i++) {
            new FormattingInfo(false, valid[i][0], valid[i][1]);
        }
    }

    @Test
    public void testFormattingInfoInvalidConstructorParams() {
        int[][] invalid = new int[][] { //
        //
                new int[] { -1, 10 }, // negative min
                new int[] { 10, -1 }, // negative max
                new int[] { -1, -1 }, // negative max
                new int[] { Integer.MIN_VALUE, Integer.MIN_VALUE }, // negative max
                // new int[] { 11, 10 }, // min > max
        };
        for (int i = 0; i < invalid.length; i++) {
            try {
                new FormattingInfo(true, invalid[i][0], invalid[i][1]);
            } catch (IllegalArgumentException ok) {
                // constructor validation worked as expected
                continue;
            }
            fail("Should have failed at index " + i);
        }
        for (int i = 0; i < invalid.length; i++) {
            try {
                new FormattingInfo(false, invalid[i][0], invalid[i][1]);
            } catch (IllegalArgumentException ok) {
                // constructor validation worked as expected
                continue;
            }
            fail("Should have failed at index " + i);
        }
    }

    @Test
    public void testDefaultValues() {
        assertFalse(FormattingInfo.getDefault().isLeftAligned());
        assertEquals("min", 0, FormattingInfo.getDefault().getMinLength());
        assertEquals("max", Integer.MAX_VALUE, FormattingInfo.getDefault().getMaxLength());
    }

    @Test
    public void testGetDefaultIsSingleton() {
        assertSame(FormattingInfo.getDefault(), FormattingInfo.getDefault());
    }

    @Test
    public void testIsLeftAlignedReturnsConstructorValue() {
        assertFalse(new FormattingInfo(false, 0, 1).isLeftAligned());
        assertTrue(new FormattingInfo(true, 0, 1).isLeftAligned());
    }

    @Test
    public void testGetMinLengthReturnsConstructorValue() {
        assertEquals(0, new FormattingInfo(false, 0, 1).getMinLength());
        assertEquals(11, new FormattingInfo(false, 11, 12).getMinLength());
    }

    @Test
    public void testGetMaxLengthReturnsConstructorValue() {
        assertEquals(1, new FormattingInfo(false, 0, 1).getMaxLength());
        assertEquals(12, new FormattingInfo(false, 11, 12).getMaxLength());
    }

    @Test
    public void testFormatIntStringBuilder() {
        assertText(2, "12XYZ", true, 7, 12, "12XYZ    ");
        assertText(2, "12XYZ", false, 7, 12, "12    XYZ");
        assertText(2, "12XYZABC", true, 0, 4, "12ZABC");
        assertText(2, "12XYZABC", false, 0, 4, "12ZABC");
        assertText(2, "12XYZABC", false, 0, Integer.MAX_VALUE, "12XYZABC");
    }

    private void assertText(int offset, String input, boolean leftAlign, int min, int max, String expected) {
        StringBuilder sb = new StringBuilder(input);
        FormattingInfo info = new FormattingInfo(leftAlign, min, max);
        info.format(offset, sb);
        assertEquals(info.toString(), expected, sb.toString());
    }

    @Test
    public void testFormatIntByteBuffer() {
        assertBinary(2, "12XYZ", true, 7, 12, "12XYZ    ");
        assertBinary(2, "12XYZ", false, 7, 12, "12    XYZ");
        assertBinary(2, "12XYZABC", true, 0, 4, "12ZABC");
        assertBinary(2, "12XYZABC", false, 0, 4, "12ZABC");
        assertBinary(2, "12XYZABC", false, 0, Integer.MAX_VALUE, "12XYZABC");
    }

    private void assertBinary(int offset, String input, boolean leftAlign, int min, int max, String expected) {
        ByteBuffer buff = ByteBuffer.allocate(512);
        buff.put(input.getBytes());
        FormattingInfo info = new FormattingInfo(leftAlign, min, max);
        info.format(offset, buff);
        assertEquals(expected.length(), buff.position()); // positioned at end of buffer
        String actual = new String(buff.array(), 0, buff.position());
        assertEquals(info.toString(), expected, actual);
    }
}
