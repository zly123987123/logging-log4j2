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
import org.apache.logging.log4j.core.config.plugins.Plugin;


/**
 * Returns the event's line location information in a StringBuilder.
 */
@Plugin(name = "MethodLocationPatternConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({ "M", "method" })
public final class MethodLocationPatternConverter extends LogEventPatternConverter {

    /**
     * Private constructor.
     */
    private MethodLocationPatternConverter(final FormattingInfo formattingInfo) {
        super("Method", "method", formattingInfo);
    }

    /**
     * Obtains an instance of MethodLocationPatternConverter.
     *
     * @param options options, may be null.
     * @return instance of MethodLocationPatternConverter.
     */
    public static MethodLocationPatternConverter newInstance(final String[] options, final FormattingInfo formattingInfo) {
        return new MethodLocationPatternConverter(formattingInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final TextBuffer toAppendTo) {
        final StackTraceElement element = event.getSource();
        if (element != null) {
            toAppendTo.append(getCachedFormattedString(element.getMethodName()));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final BinaryBuffer toAppendTo, final Charset charset) {
        final StackTraceElement element = event.getSource();
        if (element != null) {
            toAppendTo.append(getCachedFormattedBytes(element.getMethodName(), charset));
        }
    }
}
