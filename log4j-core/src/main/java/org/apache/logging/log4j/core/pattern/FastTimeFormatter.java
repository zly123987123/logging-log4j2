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
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.logging.log4j.core.util.Assert;

/**
 * TODO doc
 */
public class FastTimeFormatter implements DatePatternConverter.Formatter {

    // cached value is non-volatile on purpose
    private DatePart cached;
    private final AtomicReference<DatePart> safe = new AtomicReference<DatePart>();
    private final String pattern;
    private final Charset charset;

    private static class DatePart {
        final long midnightToday;
        final long midnightTomorrow;
        final String dateTodayString;
        final byte[] dateTodayBytes;

        public DatePart(final String pattern, final Charset charset) {
            final Calendar cal = midnightToday();
            final Date today = cal.getTime();
            midnightToday = cal.getTimeInMillis();

            cal.add(Calendar.DATE, 1);
            midnightTomorrow = cal.getTimeInMillis();

            final String datePart = extractDatePart(pattern);
            if (datePart != null) {
                dateTodayString = new SimpleDateFormat(datePart).format(today);
                dateTodayBytes = dateTodayString.getBytes(charset);
            } else {
                dateTodayString = null;
                dateTodayBytes = null;
            }
        }

        /**
         * Returns the date part of a date+time format string, or {@code null} if the specified string does not contain
         * a date part.
         */
        private static String extractDatePart(final String pattern) {
            final int offset = pattern.indexOf(DatePatternConverter.ABSOLUTE_TIME_PATTERN);
            if (offset <= 0) {
                return null;
            }
            return pattern.substring(0, offset);
        }

        private static Calendar midnightToday() {
            final Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal;
        }

        public long millisSinceMidnight(final long now) {
            return now - midnightToday;
        }
    }

    public FastTimeFormatter(final String pattern, final Charset charset) {
        this.pattern = Assert.requireNonNull(pattern, "pattern is null");
        this.charset = Assert.requireNonNull(charset, "charset is null");
        cached = new DatePart(pattern, charset);
        safe.set(cached);
    }

    private DatePart datePart(final long now) {
        DatePart result = cached; // postpone reading from volatile field
        if (now >= result.midnightTomorrow) {
            result = safe.get();
            if (now >= result.midnightTomorrow) {
                result = new DatePart(pattern, charset);
                cached = result;
                safe.set(result); // store barrier; ensures cached field update is also visible to other threads
            }
        }
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.logging.log4j.core.pattern.DatePatternConverter.Formatter#format(long,
     * org.apache.logging.log4j.core.pattern.BinaryBuffer, java.nio.charset.Charset)
     */
    public void format(final long time, final BinaryBuffer buffer, final Charset charset) {
        format(time, buffer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.logging.log4j.core.pattern.DatePatternConverter.Formatter#format(java.util.Date,
     * org.apache.logging.log4j.core.pattern.Buffer)
     */
    @Override
    public void format(Date time, Buffer buffer) {
        format(time.getTime(), buffer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.logging.log4j.core.pattern.DatePatternConverter.Formatter#format(long,
     * org.apache.logging.log4j.core.pattern.Buffer)
     */
    @Override
    public void format(long time, Buffer buffer) {
        // Calculate values by getting the ms values first and do then
        // shave off the hour minute and second values with multiplications
        // and bit shifts instead of simple but expensive divisions.

        // Get daytime in ms which does fit into an int
        // int ms = (int) (time % 86400000);
        DatePart stamp = datePart(time);
        int ms = (int) stamp.millisSinceMidnight(time);

        if (stamp.dateTodayBytes != null) {
            buffer.append(stamp.dateTodayBytes);
        }

        // well ... it works
        final int hour = (int) (((ms >> 7) * 9773437L) >> 38); // hours = ms / 3600000;
        ms -= 3600000 * hour;

        final int minute = (int) (((ms >> 5) * 2290650L) >> 32); // minutes = ms / 60000;
        ms -= 60000 * minute;

        final int second = ((ms >> 3) * 67109) >> 23; // seconds = ms / 1000;
        ms -= 1000 * second;

        // Hour
        // 13/128 is nearly the same as /10 for values up to 65
        int temp = (hour * 13) >> 7;
        buffer.append((byte) (temp + '0'));

        // Do subtract to get remainder instead of doing % 10
        buffer.append((byte) (hour - 10 * temp + '0'));
        buffer.append((byte) ':');

        // Minute
        // 13/128 is nearly the same as /10 for values up to 65
        temp = (minute * 13) >> 7;
        buffer.append((byte) (temp + '0'));

        // Do subtract to get remainder instead of doing % 10
        buffer.append((byte) (minute - 10 * temp + '0'));
        buffer.append((byte) ':');

        // Second
        // 13/128 is nearly the same as /10 for values up to 65
        temp = (second * 13) >> 7;
        buffer.append((byte) (temp + '0'));
        buffer.append((byte) (second - 10 * temp + '0'));
        buffer.append((byte) ',');

        // Millisecond
        // 41/4096 is nearly the same as /100
        temp = (ms * 41) >> 12;
        buffer.append((byte) (temp + '0'));

        ms -= 100 * temp;
        temp = (ms * 205) >> 11; // 205/2048 is nearly the same as /10
        buffer.append((byte) (temp + '0'));

        ms -= 10 * temp;
        buffer.append((byte) (ms + '0'));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.logging.log4j.core.pattern.DatePatternConverter.Formatter#toPattern()
     */
    @Override
    public String toPattern() {
        return pattern;
    }

}
