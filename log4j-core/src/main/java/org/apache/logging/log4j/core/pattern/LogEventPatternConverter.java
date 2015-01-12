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
import org.apache.logging.log4j.core.util.Assert;

/**
 * LogEventPatternConverter is a base class for pattern converters that can format information from instances of
 * LoggingEvent.
 */
public abstract class LogEventPatternConverter extends AbstractPatternConverter {

    /**
     * Constructs an instance of LogEventPatternConverter.
     *
     * @param name name of converter.
     * @param style CSS style for output.
     * @param formattingInfo the formatting info (must be non-{@code null})
     */
    protected LogEventPatternConverter(final String name, final String style, final FormattingInfo formattingInfo) {
        super(name, style, Assert.requireNonNull(formattingInfo, "formattingInfo"));
    }

    /**
     * Formats an event into the specified text buffer.
     *
     * @param event event to format, may not be null.
     * @param toAppendTo text buffer to which the formatted event will be appended. May not be null.
     */
    public abstract void format(final LogEvent event, final TextBuffer toAppendTo);

    /**
     * Formats an event into the specified binary buffer.
     *
     * @param event event to format, may not be null.
     * @param toAppendTo binary buffer to which the formatted event will be appended. May not be null.
     * @param charset the Charset to use when converting text to bytes
     */
    public abstract void format(final LogEvent event, final BinaryBuffer toAppendTo, final Charset charset);

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final Object obj, final Buffer output) {
        if (obj instanceof LogEvent) {
            if (output instanceof TextBuffer) {
                format((LogEvent) obj, (TextBuffer) output);
            } else {
                format((LogEvent) obj, (BinaryBuffer) output, getCharset());
            }
        }
    }

    /**
     * Normally pattern converters are not meant to handle Exceptions although few pattern converters might.
     * <p>
     * By examining the return values for this method, the containing layout will determine whether it handles
     * throwables or not.
     * </p>
     *
     * @return true if this PatternConverter handles throwables
     */
    public boolean handlesThrowable() {
        return false;
    }
}
