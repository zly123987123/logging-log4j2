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

import java.nio.charset.Charset;

import org.apache.logging.log4j.core.LogEvent;


/**
 *
 */
public class PatternFormatter {
    private final LogEventPatternConverter converter;

    public PatternFormatter(final LogEventPatternConverter converter) {
        this.converter = converter;
    }

    public void format(final LogEvent event, final TextBuffer buf) {
        converter.format(event, buf);
    }

    public void format(final LogEvent event, final BinaryBuffer buf, Charset charset) {
        converter.format(event, buf, charset);
    }

    public LogEventPatternConverter getConverter() {
        return converter;
    }

    /**
     * Normally pattern formatters are not meant to handle Exceptions although few pattern formatters might.
     * <p>
     * By examining the return values for this method, the containing layout will determine whether it handles
     * throwables or not.
     * </p>
     *
     * @return true if this PatternConverter handles throwables
     */
    public boolean handlesThrowable() {
        return converter.handlesThrowable();
    }

    /**
     * Returns a String suitable for debugging.
     *
     * @return a String suitable for debugging.
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append(super.toString());
        sb.append("[converter=");
        sb.append(converter);
//        sb.append(", field=");
//        sb.append(field);
        sb.append(']');
        return sb.toString();
    }
}
