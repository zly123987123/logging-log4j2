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
package org.apache.logging.log4j.android;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.MessageFactory;
import org.apache.logging.log4j.spi.AbstractLogger;

import android.util.Log;
import org.apache.logging.log4j.util.PropertiesUtil;

/**
 *
 */
public class AndroidLogger extends AbstractLogger {

    private Level level;

    public AndroidLogger(final String name, final MessageFactory messageFactory, final PropertiesUtil props,
                         final Level defaultLevel) {
        super(name, messageFactory);
        final String lvl = props.getStringProperty(AndroidLoggerContext.SYSTEM_PREFIX + name + ".level");
        this.level = Level.toLevel(lvl, defaultLevel);
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, Message message, Throwable t) {
        return Log.isLoggable(this.getName(), getLevel(level));
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, CharSequence message, Throwable t) {
        return Log.isLoggable(this.getName(), getLevel(level));
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, Object message, Throwable t) {
        return Log.isLoggable(this.getName(), getLevel(level));
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Throwable t) {
        return Log.isLoggable(this.getName(), getLevel(level));
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message) {
        return Log.isLoggable(this.getName(), getLevel(level));
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object... params) {
        return Log.isLoggable(this.getName(), getLevel(level));
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0) {
        return Log.isLoggable(this.getName(), getLevel(level));
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1) {
        return Log.isLoggable(this.getName(), getLevel(level));
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2) {
        return Log.isLoggable(this.getName(), getLevel(level));
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3) {
        return Log.isLoggable(this.getName(), getLevel(level));
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4) {
        return Log.isLoggable(this.getName(), getLevel(level));
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5) {
        return Log.isLoggable(this.getName(), getLevel(level));
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6) {
        return Log.isLoggable(this.getName(), getLevel(level));
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7) {
        return Log.isLoggable(this.getName(), getLevel(level));
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8) {
        return Log.isLoggable(this.getName(), getLevel(level));
    }

    @Override
    public boolean isEnabled(Level level, Marker marker, String message, Object p0, Object p1, Object p2, Object p3, Object p4, Object p5, Object p6, Object p7, Object p8, Object p9) {
        return Log.isLoggable(this.getName(), getLevel(level));
    }

    @Override
    public void logMessage(String fqcn, Level level, Marker marker, Message message, Throwable t) {
        switch (level.getStandardLevel()) {
            case TRACE:
                Log.v(this.getName(), message.getFormattedMessage(), t);
                break;
            case DEBUG:
                Log.d(this.getName(), message.getFormattedMessage(), t);
                break;
            case INFO:
                Log.i(this.getName(), message.getFormattedMessage(), t);
                break;
            case WARN:
                Log.w(this.getName(), message.getFormattedMessage(), t);
                break;
            case ERROR:case FATAL:
                Log.e(this.getName(), message.getFormattedMessage(), t);
                break;

        }
    }

    @Override
    public Level getLevel() {
        return level;
    }

    private int getLevel(Level level) {
        switch (level.getStandardLevel()) {
            case TRACE:
                return Log.VERBOSE;
            case DEBUG:
                return Log.DEBUG;
            case INFO:
                return Log.INFO;
            case WARN:
                return Log.WARN;
            default:
                return Log.ERROR;
        }
    }
}
