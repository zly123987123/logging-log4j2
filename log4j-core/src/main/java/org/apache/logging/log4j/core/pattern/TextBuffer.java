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

/**
 * Buffer implementation that internally tracks the appended data as text.
 */
public class TextBuffer implements Buffer {
    private StringBuilder buffer = new StringBuilder(1024);

    @Override
    public TextBuffer append(Object object) {
        return append(String.valueOf(object));
    }

    @Override
    public TextBuffer append(String text) {
        buffer.append(text);
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.logging.log4j.core.pattern.Buffer#append(byte[])
     */
    @Override
    public TextBuffer append(byte[] data) {
        final String text = new String(data); // don't specify Charset: avoid StringDecoder instantiation
        buffer.append(text);
        return this;
    }

    @Override
    public TextBuffer append(char ch) {
        buffer.append(ch);
        return this;
    }

    @Override
    public TextBuffer append(int number) {
        buffer.append(number);
        return this;
    }

    @Override
    public TextBuffer append(long number) {
        buffer.append(number);
        return this;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.logging.log4j.core.pattern.Buffer#length()
     */
    @Override
    public int length() {
        return buffer.length();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.logging.log4j.core.pattern.Buffer#setLength(int)
     */
    @Override
    public void setLength(int length) {
        buffer.setLength(length);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.logging.log4j.core.pattern.Buffer#hasTrailingWhitespace()
     */
    @Override
    public boolean hasTrailingWhitespace() {
        final int len = buffer.length();
        return len > 0 && !Character.isWhitespace(buffer.charAt(len - 1));
    }

    /**
     * Returns the char value in this buffer at the specified index. The first char value is at index 0, the next at
     * index 1, and so on, as in array indexing.
     * <p>
     * The index argument must be greater than or equal to 0, and less than the length of this buffer.
     * 
     * @param index the index of the desired char value
     * @return the char value at the specified index.
     * @throws IndexOutOfBoundsException if index is negative or greater than or equal to length().
     */
    public char charAt(int index) {
        return buffer.charAt(index);
    }

    /**
     * Returns the contents of this {@code Buffer} as a string.
     */
    @Override
    public String toString() {
        return buffer.toString();
    }
}
