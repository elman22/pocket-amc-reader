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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class CatalogReaderTest {

    /**
     * Returns the text in chunks of the given size, like a stream which delivers data in parts
     */
    private static class ChunkReader extends Reader {
        private final String text;
        private final int chunk;
        private int position = 0;

        ChunkReader(String text, int chunk) {
            this.text = text;
            this.chunk = chunk;
        }

        @Override
        public int read(char[] buffer, int offset, int length) {
            if (position >= text.length())
                return -1;
            int count = Math.min(Math.min(chunk, length), text.length() - position);
            text.getChars(position, position + count, buffer, offset);
            position += count;
            return count;
        }

        @Override
        public void close() {
        }
    }

    private static String readAll(Reader reader) throws IOException {
        StringBuilder result = new StringBuilder();
        char[] buffer = new char[5];
        int read;
        while ((read = reader.read(buffer, 0, buffer.length)) != -1) {
            result.append(buffer, 0, read);
        }
        return result.toString();
    }

    @Test
    public void dropsByteOrderMarkAtTheStart() throws IOException {
        assertEquals("<a/>", readAll(new CatalogReader(new StringReader("\uFEFF<a/>"), false)));
    }

    @Test
    public void keepsByteOrderMarkLikeCharacterInsideText() throws IOException {
        assertEquals("a\uFEFFb", readAll(new CatalogReader(new StringReader("a\uFEFFb"), true)));
    }

    @Test
    public void dropsOnlyTheFirstCharacterEvenWhenItIsAloneInItsChunk() throws IOException {
        assertEquals("\uFEFFx", readAll(new CatalogReader(new ChunkReader("\uFEFF\uFEFFx", 1), false)));
    }

    @Test
    public void leavesBadCharactersWhenNotRequested() throws IOException {
        String text = "a\u0001b\tc\u007Fd";
        assertEquals(text, readAll(new CatalogReader(new StringReader(text), false)));
    }

    @Test
    public void removesCharactersNotAllowedInXml() throws IOException {
        // \u0000-	, \u000B, \u000C, \u000E-\u001F and \u007F (the tab is removed too)
        String text = "a\u0000b\tc\u000Bd\u000Ce\u000Ef\u001Fg\u007Fh";
        assertEquals("abcdefgh", readAll(new CatalogReader(new StringReader(text), true)));
    }

    @Test
    public void keepsLineBreaksAndSpaces() throws IOException {
        String text = "a\nb\r\nc d";
        assertEquals(text, readAll(new CatalogReader(new StringReader(text), true)));
    }

    @Test
    public void keepsNonAsciiTextAndSurrogatePairs() throws IOException {
        String text = "Příliš žluťoučký Привет \uD83C\uDFAC";
        assertEquals(text, readAll(new CatalogReader(new StringReader(text), true)));
    }

    @Test
    public void neverReturnsZeroWhenAWholeChunkIsRemoved() throws IOException {
        Reader reader = new CatalogReader(new ChunkReader("\u0001\u0002\u0003x", 3), true);
        char[] buffer = new char[8];

        // The first chunk consists only of bad characters, the reader has to continue with the next one
        assertEquals(1, reader.read(buffer, 0, buffer.length));
        assertEquals('x', buffer[0]);
        assertEquals(-1, reader.read(buffer, 0, buffer.length));
    }

    @Test
    public void emptyInputIsEndOfStream() throws IOException {
        assertEquals(-1, new CatalogReader(new StringReader(""), true).read());
    }

    @Test
    public void singleCharacterReadSkipsRemovedCharacters() throws IOException {
        Reader reader = new CatalogReader(new StringReader("ab\u0001c"), true);

        assertEquals('a', reader.read());
        assertEquals('b', reader.read());
        assertEquals('c', reader.read());
        assertEquals(-1, reader.read());
    }

    @Test
    public void skipCountsOnlyCharactersWhichAreReturned() throws IOException {
        Reader reader = new CatalogReader(new StringReader("ab\u0001cd"), true);

        assertEquals(3, reader.skip(3)); // a, b and c
        assertEquals('d', reader.read());
    }

    @Test
    public void zeroLengthReadReturnsZero() throws IOException {
        assertEquals(0, new CatalogReader(new StringReader("abc"), true).read(new char[4], 0, 0));
    }

    @Test
    public void doesNotSupportMark() {
        assertFalse(new CatalogReader(new StringReader("abc"), true).markSupported());
    }
}
