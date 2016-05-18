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

package org.apache.logging.log4j.perf.jmh;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.AbstractStringLayout;
import org.apache.logging.log4j.core.layout.ByteBufferDestination;
import org.apache.logging.log4j.core.layout.Encoder;
import org.apache.logging.log4j.message.Message;
import org.apache.logging.log4j.message.SimpleMessage;
import org.apache.logging.log4j.util.StringBuilderFormattable;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * Benchmarks the two different getBytes() methods in {@link org.apache.logging.log4j.core.layout.AbstractStringLayout}.
 */
// HOW TO RUN THIS TEST
// java -jar target/benchmarks.jar AbstractStringLayoutStringEncodingBenchmark -f 1 -i 5 -wi 5 -bm sample -tu ns
@State(Scope.Thread)
public class AbstractStringLayoutStringEncodingBenchmark {
    private static final String MESSAGE =
            "This is rather long and chatty log message with quite some interesting information and a bit of fun in it which is suitable here";

    private byte[] bytes;

    private Layout usAsciiStringLayout;
    private Layout iso8859_1StringLayout;
    private Layout utf8StringLayout;
    private Layout utf16StringLayout;

    private Layout usAsciiCseqLayout;
    private Layout iso8859_1CseqLayout;
    private Layout utf8CseqLayout;
    private Layout utf16CseqLayout;

    private Layout usAsciiEncodingLayout;
    private Layout iso8859_1EncodingLayout;
    private Layout utf8EncodingLayout;
    private Layout utf16EncodingLayout;

    private LogEvent logEvent;

    private Destination destination;

    @Setup
    public void setUp() {
        bytes = new byte[128];
        for (int i = 0; i<bytes.length; i++) {
            bytes[i] = (byte)i;
        }

        usAsciiStringLayout = new StringLayout(Charset.forName("US-ASCII"));
        iso8859_1StringLayout = new StringLayout(Charset.forName("ISO-8859-1"));
        utf8StringLayout = new StringLayout(Charset.forName("UTF-8"));
        utf16StringLayout = new StringLayout(Charset.forName("UTF-16"));

        usAsciiCseqLayout = new CseqLayout(Charset.forName("US-ASCII"));
        iso8859_1CseqLayout = new CseqLayout(Charset.forName("ISO-8859-1"));
        utf8CseqLayout = new CseqLayout(Charset.forName("UTF-8"));
        utf16CseqLayout = new CseqLayout(Charset.forName("UTF-16"));

        usAsciiEncodingLayout = new EncodingLayout(Charset.forName("US-ASCII"));
        iso8859_1EncodingLayout = new EncodingLayout(Charset.forName("ISO-8859-1"));
        utf8EncodingLayout = new EncodingLayout(Charset.forName("UTF-8"));
        utf16EncodingLayout = new EncodingLayout(Charset.forName("UTF-16"));

        StringBuilder msg = new StringBuilder();
        msg.append(MESSAGE);

        logEvent = createLogEvent(new SimpleMessage(msg));

        destination = new Destination();
    }

    private static LogEvent createLogEvent(Message message) {
        final Marker marker = null;
        final String fqcn = "com.mycom.myproject.mypackage.MyClass";
        final org.apache.logging.log4j.Level level = org.apache.logging.log4j.Level.DEBUG;
        final Throwable t = null;
        final Map<String, String> mdc = null;
        final ThreadContext.ContextStack ndc = null;
        final String threadName = null;
        final StackTraceElement location = null;
        final long timestamp = 12345678;

        return Log4jLogEvent.newBuilder() //
            .setLoggerName("name(ignored)") //
            .setMarker(marker) //
            .setLoggerFqcn(fqcn) //
            .setLevel(level) //
            .setMessage(message) //
            .setThrown(t) //
            .setContextMap(mdc) //
            .setContextStack(ndc) //
            .setThreadName(threadName) //
            .setSource(location) //
            .setTimeMillis(timestamp) //
            .build();
    }

    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Benchmark
    public void baseline() {
        consume(bytes);
    }

    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Benchmark
    public void usAsciiString() {
        consume(usAsciiStringLayout.toByteArray(logEvent));
    }

    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Benchmark
    public void usAsciiCseq() {
        consume(usAsciiCseqLayout.toByteArray(logEvent));
    }

    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Benchmark
    public void usAsciiEncoding() {
        usAsciiEncodingLayout.encode(logEvent, destination);
    }

    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Benchmark
    public void iso8859_1String() {
        consume(iso8859_1StringLayout.toByteArray(logEvent));
    }

    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Benchmark
    public void iso8859_1Cseq() {
        consume(iso8859_1CseqLayout.toByteArray(logEvent));
    }

    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Benchmark
    public void iso8859_1Encoding() {
        iso8859_1EncodingLayout.encode(logEvent, destination);
    }

    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Benchmark
    public void utf8String() {
        consume(utf8StringLayout.toByteArray(logEvent));
    }

    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Benchmark
    public void utf8Cseq() {
        consume(utf8CseqLayout.toByteArray(logEvent));
    }

    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Benchmark
    public void utf8Encoding() {
        utf8EncodingLayout.encode(logEvent, destination);
    }

    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Benchmark
    public void utf16String() {
        consume(utf16StringLayout.toByteArray(logEvent));
    }

    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Benchmark
    public void utf16Cseq() {
        consume(utf16CseqLayout.toByteArray(logEvent));
    }

    @BenchmarkMode(Mode.Throughput)
    @OutputTimeUnit(TimeUnit.MILLISECONDS)
    @Benchmark
    public void utf16Encoding() {
        utf16EncodingLayout.encode(logEvent, destination);
    }

    private static long consume(byte[] bytes) {
        long checksum = 0;
        for (byte b : bytes) checksum += b;
        return checksum;
    }

    private static long consume(byte[] bytes, int offset, int length) {
        long checksum = 0;
        for (int i = offset; i < length; i++) {
            checksum += bytes[i];
        }
        return checksum;
    }

    private static class StringLayout extends AbstractStringLayout {
        public StringLayout(Charset charset) {
            super(charset);
        }

        @Override
        public String toSerializable(LogEvent event) {
            return null;
        }

        @Override
        public byte[] toByteArray(LogEvent event) {
            StringBuilder sb = getStringBuilder();
            Message message = event.getMessage();
            if (message instanceof CharSequence)
                sb.append((CharSequence)message);
            else
                sb.append(message.getFormattedMessage());
            return getBytes(sb.toString());
        }
    }

    private static class CseqLayout extends AbstractStringLayout {
        public CseqLayout(Charset charset) {
            super(charset);
        }

        @Override
        public String toSerializable(LogEvent event) {
            return null;
        }

        @Override
        public byte[] toByteArray(LogEvent event) {
            StringBuilder sb = getStringBuilder();
            Message message = event.getMessage();
            if (message instanceof CharSequence)
                sb.append((CharSequence)message);
            else
                sb.append(message.getFormattedMessage());
            return getBytes(sb);
        }
    }

    private static class EncodingLayout extends AbstractStringLayout {
        public EncodingLayout(Charset charset) {
            super(charset);
        }

        @Override
        public String toSerializable(LogEvent event) {
            return null;
        }

        @Override
        public byte[] toByteArray(LogEvent event) {
            return null;
        }

        @Override
        public void encode(final LogEvent event, final ByteBufferDestination destination) {
            StringBuilder sb = getStringBuilder();
            ((StringBuilderFormattable) event.getMessage()).formatTo(sb);
            final Encoder<StringBuilder> helper = getStringBuilderEncoder();
            helper.encode(sb, destination);
        }
    }

    private static class Destination implements ByteBufferDestination {
        ByteBuffer buffer = ByteBuffer.wrap(new byte[512]);

        @Override
        public ByteBuffer getByteBuffer() {
            return buffer;
        }

        @Override
        public ByteBuffer drain(ByteBuffer buf) {
            buf.flip();
            consume(buf.array(), buf.position(), buf.limit());
            buf.clear();
            return buf;
        }
    }

}
