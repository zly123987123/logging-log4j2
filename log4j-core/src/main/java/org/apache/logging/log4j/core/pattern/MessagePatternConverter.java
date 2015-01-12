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
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.util.Charsets;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MultiformatMessage;

/**
 * Returns the event's rendered message in a StringBuilder.
 */
@Plugin(name = "MessagePatternConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({ "m", "msg", "message" })
public final class MessagePatternConverter extends LogEventPatternConverter {

    private final String[] formats;

    private final Configuration config;

    /**
     * Private constructor.
     * 
     * @param options options, may be null.
     */
    private MessagePatternConverter(final Configuration config, final String[] options,
            final FormattingInfo formattingInfo) {
        super("Message", "message", formattingInfo);
        formats = options;
        this.config = config;
    }

    /**
     * Obtains an instance of pattern converter.
     *
     * @param config The Configuration.
     * @param options options, may be null.
     * @return instance of pattern converter.
     */
    public static MessagePatternConverter newInstance(final Configuration config, final String[] options,
            final FormattingInfo formattingInfo) {
        return new MessagePatternConverter(config, options, formattingInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final TextBuffer toAppendTo) {
        final Message msg = event.getMessage();
        if (msg != null) {
            final String msgString = messageString(event, msg);
            toAppendTo.append(msgString, getFormattingInfo());
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
        final Message msg = event.getMessage();
        if (msg != null) {
            final String msgString = messageString(event, msg);
            final String formatted = applyFormattingInfo(msgString);
            toAppendTo.append(Charsets.getBytes(formatted, charset));
        }
    }

    private String messageString(final LogEvent event, final Message msg) {
        String result;
        if (msg instanceof MultiformatMessage) {
            result = ((MultiformatMessage) msg).getFormattedMessage(formats);
        } else {
            result = msg.getFormattedMessage();
        }
        if (result == null) {
            return "null";
        }
        return (config != null && result.contains("${")) ? config.getStrSubstitutor().replace(event, result) : result;
    }
}
