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
 * Formats a logger name.
 */
@Plugin(name = "LoggerPatternConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({ "c", "logger" })
public final class LoggerPatternConverter extends NamePatternConverter {

    /**
     * Private constructor.
     *
     * @param options options, may be null.
     */
    private LoggerPatternConverter(final String[] options, final FormattingInfo formattingInfo) {
        super("Logger", "logger", options, formattingInfo);
    }

    /**
     * Obtains an instance of pattern converter.
     *
     * @param options options, may be null.
     * @return instance of pattern converter.
     */
    public static LoggerPatternConverter newInstance(final String[] options, final FormattingInfo formattingInfo) {
        if (options == null || options.length == 0) {
            return new LoggerPatternConverter(null, formattingInfo);
        }

        return new LoggerPatternConverter(options, formattingInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final TextBuffer toAppendTo) {
        toAppendTo.append(abbreviate(event.getLoggerName()));
    }

    /**
     * Format a logging event.
     *
     * @param event event to format.
     * @param toAppendTo buffer to which class name will be appended.
     */
    @Override
    public void format(final LogEvent event, final BinaryBuffer toAppendTo, final Charset charset) {
        toAppendTo.append(abbreviateToBinary(event.getLoggerName(), charset));
    }
}
