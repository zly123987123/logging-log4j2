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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.util.Charsets;
import org.apache.logging.log4j.core.util.Patterns;

/**
 * Returns the event's level in a StringBuilder.
 */
@Plugin(name = "LevelPatternConverter", category = PatternConverter.CATEGORY)
@ConverterKeys({ "p", "level" })
public final class LevelPatternConverter extends LogEventPatternConverter {
    private static final String OPTION_LENGTH = "length";
    private static final String OPTION_LOWER = "lowerCase";

    private final Map<Level, String> levelTextMap;
    private final Map<Level, byte[]> levelBinaryMap = new HashMap<Level, byte[]>();

    /**
     * Private constructor.
     */
    private LevelPatternConverter(final Map<Level, String> map, final FormattingInfo formattingInfo) {
        super("Level", "level", formattingInfo);
        this.levelTextMap = map;
    }

    /**
     * Obtains an instance of pattern converter.
     *
     * @param options options, may be null. May contain a list of level names and The value that should be displayed for
     *            the Level.
     * @return instance of pattern converter.
     */
    public static LevelPatternConverter newInstance(final String[] options, final FormattingInfo formattingInfo) {
        if (options == null || options.length == 0) {
            return createDefaultLevelPatternConverter(formattingInfo);
        }
        final StringBuilder sb = new StringBuilder(64);
        final Map<Level, String> levelMap = new HashMap<Level, String>();
        int length = Integer.MAX_VALUE; // More than the longest level name.
        boolean lowerCase = false;
        final String[] definitions = options[0].split(Patterns.COMMA_SEPARATOR);
        for (final String def : definitions) {
            final String[] pair = def.split("=");
            if (pair == null || pair.length != 2) {
                LOGGER.error("Invalid option {}", def);
                continue;
            }
            final String key = pair[0].trim();
            final String value = pair[1].trim();
            if (OPTION_LENGTH.equalsIgnoreCase(key)) {
                length = Integer.parseInt(value);
            } else if (OPTION_LOWER.equalsIgnoreCase(key)) {
                lowerCase = Boolean.parseBoolean(value);
            } else {
                final Level level = Level.toLevel(key, null);
                if (level == null) {
                    LOGGER.error("Invalid Level {}", key);
                } else {
                    levelMap.put(level, format(formattingInfo, sb, value));
                }
            }
        }
        if (levelMap.isEmpty() && length == Integer.MAX_VALUE && !lowerCase) {
            return createDefaultLevelPatternConverter(formattingInfo);
        }
        for (final Level level : Level.values()) {
            if (!levelMap.containsKey(level)) {
                final String left = left(level, length);
                final String value = lowerCase ? left.toLowerCase(Locale.US) : left;
                levelMap.put(level, format(formattingInfo, sb, value));
            }
        }
        return new LevelPatternConverter(levelMap, formattingInfo);
    }

    private static LevelPatternConverter createDefaultLevelPatternConverter(final FormattingInfo formattingInfo) {
        final FormattingInfo fi = formattingInfo;
        return new LevelPatternConverter(createTextLevelMap(fi), fi);
    }

    private static Map<Level, String> createTextLevelMap(final FormattingInfo formattingInfo) {
        if (formattingInfo == null || formattingInfo == FormattingInfo.getDefault()) {
            return null; // indicates no formatting
        }
        final Map<Level, String> result = new HashMap<Level, String>();
        final StringBuilder sb = new StringBuilder(64);
        for (final Level level : Level.values()) {
            final String formatted = format(formattingInfo, sb, level.name());
            result.put(level, formatted);
        }
        return result;
    }    

    private static Map<Level, byte[]> createBinaryLevelMap(final FormattingInfo formattingInfo, final Charset charset) {
        final Map<Level, byte[]> result = new HashMap<Level, byte[]>();
        final StringBuilder sb = new StringBuilder(64);
        for (final Level level : Level.values()) {
            final String formatted = format(formattingInfo, sb, level.name());
            result.put(level, Charsets.getBytes(formatted, charset));
        }
        return result;
    }

    private static String format(final FormattingInfo formattingInfo, final StringBuilder sb, final String raw) {
        sb.setLength(0);
        sb.append(raw);
        formattingInfo.format(0, sb);
        return sb.toString();
    }

    /**
     * Returns the leftmost chars of the level name for the given level.
     *
     * @param level The level
     * @param length How many chars to return
     * @return The abbreviated level name, or the whole level name if the {@code length} is greater than the level name
     *         length,
     */
    private static String left(final Level level, final int length) {
        final String string = level.toString();
        if (length >= string.length()) {
            return string;
        }
        return string.substring(0, length);
    }
    
    @Override
    public void setCharset(final Charset charset) {
        super.setCharset(charset);
        if (this.levelTextMap == null) {
            this.levelBinaryMap.putAll(createBinaryLevelMap(getFormattingInfo(), charset));
        } else {
            for (final Level level : levelTextMap.keySet()) {
                final String value = levelTextMap.get(level);
                levelBinaryMap.put(level, Charsets.getBytes(value, charset));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final TextBuffer output) {
        output.append(levelTextMap == null ? event.getLevel().toString() : levelTextMap.get(event.getLevel()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void format(final LogEvent event, final BinaryBuffer output, final Charset charset) {
        output.append(levelBinaryMap.get(event.getLevel()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getStyleClass(final Object e) {
        if (e instanceof LogEvent) {
            return "level " + ((LogEvent) e).getLevel().name().toLowerCase(Locale.ENGLISH);
        }
        return "level";
    }
}
