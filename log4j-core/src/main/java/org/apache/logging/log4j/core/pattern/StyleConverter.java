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
import java.util.List;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.util.Patterns;

/**
 * Style pattern converter. Adds ANSI color styling to the result of the enclosed pattern.
 */
@Plugin(name = "style", category = PatternConverter.CATEGORY)
@ConverterKeys({ "style" })
public final class StyleConverter extends LogEventPatternConverter implements AnsiConverter {

    /**
     * Gets an instance of the class.
     *
     * @param config The current Configuration.
     * @param options pattern options, may be null. If first element is "short", only the first line of the throwable
     *            will be formatted.
     * @return instance of class.
     */
    public static StyleConverter newInstance(final Configuration config, final String[] options,
            final FormattingInfo formattingInfo) {
        if (options.length < 1) {
            LOGGER.error("Incorrect number of options on style. Expected at least 1, received " + options.length);
            return null;
        }
        if (options[0] == null) {
            LOGGER.error("No pattern supplied on style");
            return null;
        }
        if (options[1] == null) {
            LOGGER.error("No style attributes provided");
            return null;
        }
        final PatternParser parser = PatternLayout.createPatternParser(config);
        final List<PatternFormatter> formatters = parser.parse(options[0]);
        final String style = AnsiEscape.createSequence(options[1].split(Patterns.COMMA_SEPARATOR));
        final boolean noConsoleNoAnsi = options.length > 2
                && (PatternParser.NO_CONSOLE_NO_ANSI + "=true").equals(options[2]);
        final boolean hideAnsi = noConsoleNoAnsi && System.console() == null;
        return new StyleConverter(formatters, style, hideAnsi, formattingInfo);
    }

    private final List<PatternFormatter> patternFormatters;

    private final boolean noAnsi;

    private final String style;

    private final String defaultStyle;

    /**
     * Constructs the converter.
     *
     * @param patternFormatters The PatternFormatters to generate the text to manipulate.
     * @param style The style that should encapsulate the pattern.
     * @param noAnsi If true, do not output ANSI escape codes.
     */
    private StyleConverter(final List<PatternFormatter> patternFormatters, final String style, final boolean noAnsi,
            final FormattingInfo formattingInfo) {
        super("style", "style", formattingInfo);
        this.patternFormatters = patternFormatters;
        this.style = style;
        this.defaultStyle = AnsiEscape.getDefaultStyle();
        this.noAnsi = noAnsi;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final TextBuffer toAppendTo) {
        format0(event, toAppendTo, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final BinaryBuffer toAppendTo, final Charset charset) {
        format0(event, toAppendTo, charset);
    }

    private void format0(final LogEvent event, final Buffer toAppendTo, final Charset charset) {
        formatNested(event, toAppendTo, charset, noAnsi, patternFormatters, style, defaultStyle);
    }

    public static void formatNested(final LogEvent event, final Buffer toAppendTo, final Charset charset,
            final boolean noAnsi, final List<PatternFormatter> nested, final String style, final String defaultStyle) {
        if (noAnsi) {
            appendNested(nested, event, toAppendTo, charset);
            return;
        }
        final int startLengthForUndo = toAppendTo.length();
        toAppendTo.append(style);
        final int beforeAddingNested = toAppendTo.length();

        appendNested(nested, event, toAppendTo, charset);

        if (toAppendTo.length() == beforeAddingNested) {
            toAppendTo.setLength(startLengthForUndo); // remove style
        } else {
            toAppendTo.append(defaultStyle);
        }
    }

    private static void appendNested(final List<PatternFormatter> formatters, final LogEvent event,
            final Buffer toAppendTo, final Charset charset) {
        if (toAppendTo instanceof TextBuffer) {
            for (final PatternFormatter formatter : formatters) {
                formatter.format(event, (TextBuffer) toAppendTo);
            }
        } else {
            for (final PatternFormatter formatter : formatters) {
                formatter.format(event, (BinaryBuffer) toAppendTo, charset);
            }
        }
    }

    @Override
    public boolean handlesThrowable() {
        for (final PatternFormatter formatter : patternFormatters) {
            if (formatter.handlesThrowable()) {
                return true;
            }
        }
        return false;
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
        sb.append("[style=");
        sb.append(style);
        sb.append(", defaultStyle=");
        sb.append(defaultStyle);
        sb.append(", patternFormatters=");
        sb.append(patternFormatters);
        sb.append(", noAnsi=");
        sb.append(noAnsi);
        sb.append(']');
        return sb.toString();
    }

}
