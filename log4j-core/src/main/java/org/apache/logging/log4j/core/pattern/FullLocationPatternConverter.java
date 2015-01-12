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
 * Format the event's line location information.
 */
@Plugin(name = "FullLocationPatternConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({ "l", "location" })
public final class FullLocationPatternConverter extends LogEventPatternConverter {

    /**
     * Private constructor.
     */
    private FullLocationPatternConverter(final FormattingInfo formattingInfo) {
        super("Full Location", "fullLocation", formattingInfo);
    }

    /**
     * Obtains an instance of pattern converter.
     *
     * @param options options, may be null.
     * @return instance of pattern converter.
     */
    public static FullLocationPatternConverter newInstance(final String[] options, final FormattingInfo formattingInfo) {
        return new FullLocationPatternConverter(formattingInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final TextBuffer output) {
        final StackTraceElement element = event.getSource();
        if (element != null) {
            output.append(getCachedFormattedString(element));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final BinaryBuffer toAppendTo, final Charset charset) {
        final StackTraceElement element = event.getSource();
        if (element != null) {
            toAppendTo.append(getCachedFormattedBytes(element, charset));
        }
    }
}
