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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.util.Assert;

/**
 * Convert and format the event's date in a StringBuilder.
 */
@Plugin(name = "DatePatternConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({ "d", "date" })
public final class DatePatternConverter extends LogEventPatternConverter implements ArrayPatternConverter {

    /**
     * ADT for formatter helpers.
     */
    public abstract static interface Formatter {
        void format(long time, Buffer buffer);

        void format(Date time, Buffer buffer);

        void format(long timestamp, BinaryBuffer output, Charset charset);

        String toPattern();
    }

    private static class PatternFormatter implements Formatter {
        private ThreadLocal<SimpleDateFormat> simpleDateFormat = new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                final SimpleDateFormat result = new SimpleDateFormat(pattern);
                if (timeZone != null) {
                    result.setTimeZone(timeZone);
                }
                return result;
            }
        };
        private final String pattern;
        private final TimeZone timeZone;
        private final boolean useFastFormat;

        PatternFormatter(final String pattern, final TimeZone timeZone) {
            this.pattern = Assert.requireNonNull(pattern, "pattern is null");
            this.timeZone = timeZone;
            this.useFastFormat = ABSOLUTE_TIME_PATTERN.equals(pattern);
        }

        @Override
        public void format(final long time, final Buffer buffer) {
            format(new Date(time), buffer);
        }

        @Override
        public void format(final Date time, final Buffer buffer) {
            if (useFastFormat) {

            } else {
                final String formatted = simpleDateFormat.get().format(time);
                buffer.append(formatted);
            }
        }

        @Override
        public void format(final long timestamp, final BinaryBuffer output, final Charset charset) {
            if (useFastFormat) {

            } else {
                format(new Date(timestamp), output); // use SimpleDateFormat
            }
        }

        @Override
        public String toPattern() {
            return pattern;
        }
    }

    private static class UnixFormatter implements Formatter {
        @Override
        public void format(final long time, final Buffer buffer) {
            buffer.append(time / 1000);
        }

        @Override
        public void format(final Date time, final Buffer buffer) {
            format(time.getTime(), buffer);
        }

        @Override
        public void format(final long timestamp, final BinaryBuffer output, final Charset charset) {
            output.append(timestamp / 1000);
        }

        @Override
        public String toPattern() {
            return null;
        }
    }

    private static class UnixMillisFormatter implements Formatter {
        @Override
        public void format(final long time, final Buffer buffer) {
            buffer.append(time);
        }

        @Override
        public void format(final Date time, final Buffer buffer) {
            format(time.getTime(), buffer);
        }

        @Override
        public void format(final long timestamp, final BinaryBuffer output, final Charset charset) {
            output.append(timestamp);
        }

        @Override
        public String toPattern() {
            return null;
        }
    }

    /**
     * ABSOLUTE string literal.
     */
    private static final String ABSOLUTE_FORMAT = "ABSOLUTE";

    /**
     * SimpleTimePattern for ABSOLUTE.
     */
    static final String ABSOLUTE_TIME_PATTERN = "HH:mm:ss,SSS";

    /**
     * COMPACT string literal.
     */
    private static final String COMPACT_FORMAT = "COMPACT";

    /**
     * SimpleTimePattern for COMPACT.
     */
    private static final String COMPACT_PATTERN = "yyyyMMddHHmmssSSS";

    /**
     * DATE string literal.
     */
    private static final String DATE_AND_TIME_FORMAT = "DATE";

    /**
     * SimpleTimePattern for DATE.
     */
    private static final String DATE_AND_TIME_PATTERN = "dd MMM yyyy HH:mm:ss,SSS";

    /**
     * DEFAULT string literal.
     */
    private static final String DEFAULT_FORMAT = "DEFAULT";

    /**
     * SimpleTimePattern for DEFAULT.
     */
    // package private for unit tests
    static final String DEFAULT_PATTERN = "yyyy-MM-dd HH:mm:ss,SSS";

    /**
     * ISO8601_BASIC string literal.
     */
    private static final String ISO8601_BASIC_FORMAT = "ISO8601_BASIC";

    /**
     * SimpleTimePattern for ISO8601_BASIC.
     */
    private static final String ISO8601_BASIC_PATTERN = "yyyyMMdd'T'HHmmss,SSS";

    /**
     * ISO8601 string literal.
     */
    // package private for unit tests
    static final String ISO8601_FORMAT = "ISO8601";

    /**
     * SimpleTimePattern for ISO8601.
     */
    // package private for unit tests
    static final String ISO8601_PATTERN = "yyyy-MM-dd'T'HH:mm:ss,SSS";

    /**
     * UNIX formatter in seconds (standard).
     */
    private static final String UNIX_FORMAT = "UNIX";

    /**
     * UNIX formatter in milliseconds
     */
    private static final String UNIX_MILLIS_FORMAT = "UNIX_MILLIS";

    /**
     * Obtains an instance of pattern converter.
     *
     * @param options options, may be null.
     * @return instance of pattern converter.
     */
    public static DatePatternConverter newInstance(final String[] options, final FormattingInfo formattingInfo) {
        return new DatePatternConverter(options, formattingInfo);
    }

    private Formatter formatter;

    /**
     * Private constructor.
     *
     * @param options options, may be null.
     */
    private DatePatternConverter(final String[] options, final FormattingInfo formattingInfo) {
        super("Date", "date", formattingInfo);

        // null patternOption is OK.
        final String patternOption = options != null && options.length > 0 ? options[0] : null;

        String pattern = null;
        Formatter tempFormatter = null;

        if (patternOption == null || patternOption.equalsIgnoreCase(DEFAULT_FORMAT)) {
            pattern = DEFAULT_PATTERN;
        } else if (patternOption.equalsIgnoreCase(ISO8601_FORMAT)) {
            pattern = ISO8601_PATTERN;
        } else if (patternOption.equalsIgnoreCase(ISO8601_BASIC_FORMAT)) {
            pattern = ISO8601_BASIC_PATTERN;
        } else if (patternOption.equalsIgnoreCase(ABSOLUTE_FORMAT)) {
            pattern = ABSOLUTE_TIME_PATTERN;
        } else if (patternOption.equalsIgnoreCase(DATE_AND_TIME_FORMAT)) {
            pattern = DATE_AND_TIME_PATTERN;
        } else if (patternOption.equalsIgnoreCase(COMPACT_FORMAT)) {
            pattern = COMPACT_PATTERN;
        } else if (patternOption.equalsIgnoreCase(UNIX_FORMAT)) {
            tempFormatter = new UnixFormatter();
        } else if (patternOption.equalsIgnoreCase(UNIX_MILLIS_FORMAT)) {
            tempFormatter = new UnixMillisFormatter();
        } else {
            pattern = patternOption;
        }

        if (pattern != null) {
            try {
                new SimpleDateFormat(pattern);
            } catch (final IllegalArgumentException e) {
                LOGGER.warn("Could not instantiate SimpleDateFormat with pattern " + patternOption, e);

                // default to the DEFAULT format
                pattern = DEFAULT_PATTERN;
            }

            // if the option list contains a TZ option, then set it.
            final TimeZone tz = (options != null && options.length > 1) ? TimeZone.getTimeZone(options[1]) : null;
            tempFormatter = new PatternFormatter(pattern, tz);
        }
        formatter = tempFormatter;
    }

    @Override
    public void setCharset(Charset charset) {
        super.setCharset(charset);
        if (formatter instanceof PatternFormatter) {
            PatternFormatter paf = (PatternFormatter) formatter;
            
            // FastTimeFormat does not support time zones
            if (paf.timeZone == null && paf.pattern.endsWith(ABSOLUTE_TIME_PATTERN)) {
                formatter = new FastTimeFormatter(paf.pattern, charset);
            }
        }
    }

    /**
     * Append formatted date to string buffer.
     *
     * @param date date
     * @param toAppendTo buffer to which formatted date is appended.
     */
    public void format(final Date date, final Buffer toAppendTo) {
        formatter.format(date, toAppendTo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final TextBuffer output) {
        formatter.format(event.getTimeMillis(), output);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final BinaryBuffer output, final Charset charset) {
        formatter.format(event.getTimeMillis(), output, charset);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final Object obj, final Buffer output) {
        if (obj instanceof Date) {
            format((Date) obj, output);
        }
        super.format(obj, output);
    }

    @Override
    public void format(final Buffer toAppendTo, final Object... objects) {
        for (final Object obj : objects) {
            if (obj instanceof Date) {
                format(obj, toAppendTo);
                break;
            }
        }
    }

    /**
     * Gets the pattern string describing this date format.
     *
     * @return the pattern string describing this date format.
     */
    public String getPattern() {
        return formatter.toPattern();
    }

}
