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
import org.apache.logging.log4j.core.util.Constants;

/**
 * Formats a line separator.
 */
@Plugin(name = "LineSeparatorPatternConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({ "n" })
public final class LineSeparatorPatternConverter extends LogEventPatternConverter {

    /**
     * Line separator.
     */
    private final String lineSep;
    private final byte[] lineSepBytes;

    /**
     * Private constructor.
     */
    private LineSeparatorPatternConverter(final FormattingInfo formattingInfo) {
        super("Line Sep", "lineSep", formattingInfo);
        lineSep = Constants.LINE_SEPARATOR;
        lineSepBytes = lineSep.getBytes(); // Charset does not matter for this particular string
    }

    /**
     * Obtains an instance of pattern converter.
     *
     * @param options options, may be null.
     * @return instance of pattern converter.
     */
    public static LineSeparatorPatternConverter newInstance(final String[] options, final FormattingInfo formattingInfo) {
        return new LineSeparatorPatternConverter(formattingInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final TextBuffer toAppendTo) {
        // note: this converter ignores FormattingInfo on purpose
        toAppendTo.append(lineSep);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final BinaryBuffer toAppendTo, final Charset charset) {
        // note: this converter ignores FormattingInfo on purpose
        toAppendTo.append(lineSepBytes);
    }
}
