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
 * Formats the class name of the site of the logging request.
 */
@Plugin(name = "ClassNamePatternConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({ "C", "class" })
public final class ClassNamePatternConverter extends NamePatternConverter {

    private static final char NA = '?';

    /**
     * Private constructor.
     *
     * @param options options, may be null.
     */
    private ClassNamePatternConverter(final String[] options, final FormattingInfo formattingInfo) {
        super("Class Name", "class name", options, formattingInfo);
    }

    /**
     * Gets an instance of ClassNamePatternConverter.
     *
     * @param options options, may be null.
     * @return instance of pattern converter.
     */
    public static ClassNamePatternConverter newInstance(final String[] options, final FormattingInfo formattingInfo) {
        return new ClassNamePatternConverter(options, formattingInfo);
    }

    /**
     * Format a logging event.
     *
     * @param event event to format.
     * @param toAppendTo buffer to which class name will be appended.
     */
    @Override
    public void format(final LogEvent event, final TextBuffer toAppendTo) {
        final StackTraceElement element = event.getSource();
        if (element == null) {
            toAppendTo.append(NA);
        } else {
            toAppendTo.append(abbreviate(element.getClassName()));
        }
    }

    /**
     * Format a logging event.
     *
     * @param event event to format.
     * @param toAppendTo buffer to which class name will be appended.
     */
    @Override
    public void format(final LogEvent event, final BinaryBuffer toAppendTo, final Charset charset) {
        final StackTraceElement element = event.getSource();
        if (element == null) {
            toAppendTo.append((byte) NA);
        } else {
            toAppendTo.append(abbreviateToBinary(element.getClassName(), charset));
        }
    }
}
