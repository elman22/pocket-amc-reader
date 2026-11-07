/*
    This file is part of Pocket AMC Reader.
    Copyright © 2010-2026 Elman <holdingscythe@zoznam.sk>

    Pocket AMC Reader is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    Pocket AMC Reader is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with Pocket AMC Reader.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.holdingscythe.pocketamcreader.utils;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class ProgressFilterInputStreamTest {

    private final List<Long> reported = new ArrayList<>();

    private ProgressFilterInputStream stream(String content) {
        return new ProgressFilterInputStream(new ByteArrayInputStream(content.getBytes()), reported::add);
    }

    @Test
    public void singleByteReadsAreCounted() throws IOException {
        ProgressFilterInputStream in = stream("abc");

        assertEquals('a', in.read());
        assertEquals('b', in.read());
        assertEquals('c', in.read());

        assertEquals(Arrays.asList(1L, 2L, 3L), reported);
    }

    @Test
    public void endOfStreamIsNotReported() throws IOException {
        ProgressFilterInputStream in = stream("a");

        in.read();
        assertEquals(-1, in.read());

        assertEquals(Arrays.asList(1L), reported);
    }

    @Test
    public void bufferedReadsReportTheBytesActuallyRead() throws IOException {
        ProgressFilterInputStream in = stream("hello world"); // 11 bytes
        byte[] buffer = new byte[4];

        assertEquals(4, in.read(buffer));
        assertEquals(4, in.read(buffer));
        assertEquals(3, in.read(buffer)); // less than the buffer size
        assertEquals(-1, in.read(buffer));

        assertEquals(Arrays.asList(4L, 8L, 11L), reported);
    }

    @Test
    public void dataIsPassedThroughUnchanged() throws IOException {
        ProgressFilterInputStream in = stream("hello");
        byte[] buffer = new byte[5];

        assertEquals(5, in.read(buffer, 0, 5));

        assertArrayEquals("hello".getBytes(), buffer);
    }

    @Test
    public void skippedBytesAreCounted() throws IOException {
        ProgressFilterInputStream in = stream("0123456789");

        assertEquals(5, in.skip(5));
        assertEquals('5', in.read());

        assertEquals(Arrays.asList(5L, 6L), reported);
    }

    @Test
    public void listenerCanAbortReading() {
        ProgressFilterInputStream in = new ProgressFilterInputStream(new ByteArrayInputStream(new byte[100]),
                total -> {
                    if (total > 10)
                        throw new InterruptedIOException("Import cancelled");
                });
        byte[] buffer = new byte[8];

        assertThrows(InterruptedIOException.class, () -> {
            //noinspection InfiniteLoopStatement
            while (true) {
                in.read(buffer);
            }
        });
    }
}
