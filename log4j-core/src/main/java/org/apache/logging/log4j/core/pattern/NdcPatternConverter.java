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
 * Returns the event's NDC in a StringBuilder.
 */
@Plugin(name = "NdcPatternConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({ "x", "NDC" })
public final class NdcPatternConverter extends LogEventPatternConverter {

    /**
     * Private constructor.
     */
    private NdcPatternConverter(final FormattingInfo formattingInfo) {
        super("NDC", "ndc", formattingInfo);
    }

    /**
     * Obtains an instance of NdcPatternConverter.
     * 
     * @param options options, may be null.
     * @return instance of NdcPatternConverter.
     */
    public static NdcPatternConverter newInstance(final String[] options, final FormattingInfo formattingInfo) {
        return new NdcPatternConverter(formattingInfo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final TextBuffer toAppendTo) {
        toAppendTo.append(event.getContextStack());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final BinaryBuffer toAppendTo, final Charset charset) {
        toAppendTo.append(event.getContextStack());
    }
}
