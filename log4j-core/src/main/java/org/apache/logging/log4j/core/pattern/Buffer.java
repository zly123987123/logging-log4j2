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
 * Buffer class.
 */
public interface Buffer {

    Buffer append(Object object);

    Buffer append(String text);

    Buffer append(byte[] data);

    Buffer append(byte byt);

    Buffer append(char ch);

    Buffer append(int value);

    Buffer append(long value);
    
    /**
     * Appends the specified long to this buffer and applies the specified alignment and width adjustments.
     * 
     * @param value the value to append (before applying alignment and width adjustments)
     * @param formattingInfo can apply alignment and width adjustments
     */
    Buffer append(long value, FormattingInfo formattingInfo);

    /**
     * Appends the specified text to this buffer and applies the specified alignment and width adjustments.
     * 
     * @param unformatted the text to append (before applying alignment and width adjustments)
     * @param formattingInfo can apply alignment and width adjustments
     */
    Buffer append(String value, FormattingInfo formattingInfo);

    int length();

    void setLength(int length);

    boolean hasTrailingWhitespace();
}
