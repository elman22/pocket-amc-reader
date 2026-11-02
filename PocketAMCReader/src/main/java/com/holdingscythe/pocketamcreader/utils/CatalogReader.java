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

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

/**
 * Cleans up catalog characters on the fly, so the catalog doesn't have to be converted to a temporary UTF-8 file:
 * drops byte order mark and optionally characters which are not allowed in XML.
 */
public class CatalogReader extends FilterReader {

    private final boolean mRemoveBadChars;
    private boolean mFirst = true;

    public CatalogReader(Reader in, boolean removeBadChars) {
        super(in);
        mRemoveBadChars = removeBadChars;
    }

    @Override
    public int read(char[] buffer, int offset, int length) throws IOException {
        if (length == 0)
            return 0;

        while (true) {
            int read = in.read(buffer, offset, length);
            if (read <= 0)
                return read;

            int end = offset + read;
            int out = offset;

            for (int i = offset; i < end; i++) {
                char c = buffer[i];

                if (mFirst) {
                    mFirst = false;
                    // byte order mark, parser is not interested in it
                    if (c == '\uFEFF')
                        continue;
                }

                if (mRemoveBadChars && isBadChar(c))
                    continue;

                buffer[out++] = c;
            }

            // Never return 0, that would look like a stalled stream
            if (out > offset)
                return out - offset;
        }
    }

    @Override
    public int read() throws IOException {
        char[] one = new char[1];
        return read(one, 0, 1) == -1 ? -1 : one[0];
    }

    @Override
    public long skip(long n) throws IOException {
        char[] buffer = new char[(int) Math.min(Math.max(n, 0), 4096)];
        long skipped = 0;
        while (skipped < n) {
            int read = read(buffer, 0, (int) Math.min(buffer.length, n - skipped));
            if (read == -1)
                break;
            skipped += read;
        }
        return skipped;
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    /**
     * Based on <a href="http://mickaelvanneufville.online.fr/AMCU/scripts/RemoveBadChars.ifs">...</a>
     * Characters [\x00-\x09\x0B\x0C\x0E-\x1F\x7F]
     */
    private static boolean isBadChar(char c) {
        return c <= 0x09 || c == 0x0B || c == 0x0C || (c >= 0x0E && c <= 0x1F) || c == 0x7F;
    }
}
