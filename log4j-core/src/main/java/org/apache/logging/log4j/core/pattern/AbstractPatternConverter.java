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

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.Charsets;
import org.apache.logging.log4j.core.util.ConcurrentLRUCache;
import org.apache.logging.log4j.status.StatusLogger;

/**
 * AbstractPatternConverter is an abstract class that provides the formatting functionality that derived classes need.
 * <p>
 * Conversion specifiers in a conversion patterns are parsed to individual PatternConverters. Each of which is
 * responsible for converting an object in a converter specific manner.
 * </p>
 */
public abstract class AbstractPatternConverter implements PatternConverter {
    /**
     * Allow subclasses access to the status logger.
     */
    protected static final Logger LOGGER = StatusLogger.getLogger();

    /**
     * Converter name.
     */
    private final String name;

    /**
     * Converter style name.
     */
    private final String style;

    private final FormattingInfo formattingInfo;
    protected final boolean hasFormattingInfo;    
    protected Charset charset;
    private volatile ConcurrentLRUCache<Object, String> textCache;
    private volatile ConcurrentLRUCache<Object, byte[]> binaryCache;
    protected ThreadLocal<StringBuilder> formattingBuffer = new ThreadLocal<StringBuilder>() {
        @Override
        protected StringBuilder initialValue() {
            return new StringBuilder(128);
        };
    };

    /**
     * Create a new pattern converter.
     *
     * @param name name for pattern converter.
     * @param style CSS style for formatted output.
     */
    protected AbstractPatternConverter(final String name, final String style, final FormattingInfo formattingInfo) {
        this.name = name;
        this.style = style;
        this.formattingInfo = formattingInfo;
        this.hasFormattingInfo = formattingInfo != null && formattingInfo != FormattingInfo.getDefault();
    }

    /**
     * This method returns the name of the conversion pattern.
     * <p>
     * The name can be useful to certain Layouts such as HtmlLayout.
     * </p>
     *
     * @return the name of the conversion pattern
     */
    @Override
    public final String getName() {
        return name;
    }

    /**
     * This method returns the CSS style class that should be applied to the LoggingEvent passed as parameter, which can
     * be null.
     * <p>
     * This information is currently used only by HtmlLayout.
     * </p>
     *
     * @param e null values are accepted
     * @return the name of the conversion pattern
     */
    @Override
    public String getStyleClass(final Object e) {
        return style;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FormattingInfo getFormattingInfo() {
        return formattingInfo;
    }
    
    protected String applyFormattingInfo(final String unformatted) {
        if (!hasFormattingInfo) {
            return unformatted;
        }
        final FormattingInfo info = formattingInfo;
        final StringBuilder buffer = formattingBuffer.get();
        buffer.setLength(0);
        buffer.append(unformatted);
        info.format(0, buffer);
        return buffer.toString();
    }
    
    protected String getCachedFormattedString(final String unformatted) {
        if (!hasFormattingInfo) {
            return unformatted; // no need to even access the cache
        }
        return getCachedFormattedString((Object) unformatted);
    }
    
    protected String getCachedFormattedString(final Object object) {        
        // first see if we already have the formatted value cached
        final ConcurrentLRUCache<Object, String> cache = getTextCache();
        final String cachedResult = cache.get(object);
        if (cachedResult != null) {
            return cachedResult; // if we have it cached we're done
        }
        // if not in cache we need to create the formatted String
        final String formatted = applyFormattingInfo(convert(object));
        cache.set(object, formatted); // cache the result for next time
        return formatted;
    }
    
    protected String convert(final Object object) {
        return object.toString();
    }
    
    protected byte[] getCachedFormattedBytes(final Object unformatted, final Charset charset) {
        // first see if we already have the formatted value cached
        final ConcurrentLRUCache<Object, byte[]> cache = getBinaryCache();
        final byte[] cachedResult = cache.get(unformatted);
        if (cachedResult != null) {
            return cachedResult;
        }
        // if not in cache we need to create the formatted String and get its bytes
        final String formatted = applyFormattingInfo(convert(unformatted));
        final byte[] result = Charsets.getBytes(formatted, charset);
        cache.set(unformatted, result);
        return result;
    }

    protected ConcurrentLRUCache<Object, String> getTextCache() {
        if (textCache == null) {
            textCache = new ConcurrentLRUCache<Object, String>(128);
        }
        return textCache;
    }

    protected ConcurrentLRUCache<Object, byte[]> getBinaryCache() {
        if (binaryCache == null) {
            binaryCache = new ConcurrentLRUCache<Object, byte[]>(128);
        }
        return binaryCache;
    }
    
    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder(256);
        result.append(getClass().getSimpleName()).append('[');
        result.append(getName());
        if (style != null) {
            result.append(", style=").append(style);
        }
        if (formattingInfo != null) {
            result.append(", ").append(formattingInfo);
        }
        result.append(']');
        return result.toString();
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }
}
