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


/**
 * Abstract base class for other pattern converters which can return only parts of their name.
 */
public abstract class NamePatternConverter extends LogEventPatternConverter {
    /**
     * Abbreviator.
     */
    private final NameAbbreviator abbreviator;

    /**
     * Constructor.
     *
     * @param name    name of converter.
     * @param style   style name for associated output.
     * @param options options, may be null, first element will be interpreted as an abbreviation pattern.
     */
    protected NamePatternConverter(final String name, final String style, final String[] options, final FormattingInfo formattingInfo) {
        super(name, style, formattingInfo);

        if (options != null && options.length > 0) {
            abbreviator = NameAbbreviator.getAbbreviator(options[0]);
        } else {
            abbreviator = NameAbbreviator.getDefaultAbbreviator();
        }
    }
    
    /**
     * Abbreviates specified name.
     *
     * @param name the name to abbreviate.
     * @return The abbreviated name.
     */
    @Override
    protected String convert(final Object name) {
        return abbreviator.abbreviate(name.toString());
    }

    /**
     * Abbreviates specified name.
     *
     * @param name the name to abbreviate.
     * @return The abbreviated name.
     */
    protected final String abbreviate(final String name) {
        if (abbreviator == NameAbbreviator.getDefaultAbbreviator() && !hasFormattingInfo) {
            return name; // no formatting to do
        }
        return getCachedFormattedString(name);
    }

    /**
     * Abbreviates specified name and returns the encoded result.
     *
     * @param name the name to abbreviate.
     * @param charset the charset to use when converting text to bytes
     * @return The abbreviated name as a byte array.
     */
    protected final byte[] abbreviateToBinary(final String name, final Charset charset) {
        return getCachedFormattedBytes(name, charset);
    }
}
