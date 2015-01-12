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
import org.apache.logging.log4j.core.impl.Log4jLogEvent;

/**
 * Formats the event thread name.
 */
@Plugin(name = "ThreadPatternConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({ "t", "thread" })
public final class ThreadPatternConverter extends LogEventPatternConverter {

    /**
     * Private constructor.
     */
    private ThreadPatternConverter(final FormattingInfo formattingInfo) {
        super("Thread", "thread", formattingInfo);
    }

    /**
     * Obtains an instance of ThreadPatternConverter.
     *
     * @param options options, currently ignored, may be null.
     * @return instance of ThreadPatternConverter.
     */
    public static ThreadPatternConverter newInstance(final String[] options, final FormattingInfo formattingInfo) {
        return new ThreadPatternConverter(formattingInfo);
    }
    
    @Override
    protected String convert(final Object thread) {
        if (thread instanceof Thread) {
            return ((Thread) thread).getName();
        }
        return thread.toString();
    }
    
    private Object extractThreadOrThreadName(final LogEvent event) {
        final boolean useEvent = event instanceof Log4jLogEvent && ((Log4jLogEvent) event).hasThreadName();
        return useEvent ? event.getThreadName() : Thread.currentThread();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final TextBuffer toAppendTo) {
        toAppendTo.append(getCachedFormattedString(extractThreadOrThreadName(event)));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final BinaryBuffer toAppendTo, final Charset charset) {
        toAppendTo.append(getCachedFormattedBytes(extractThreadOrThreadName(event), charset));
    }
}
