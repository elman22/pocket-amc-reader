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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;

/**
 * Reports bytes actually read. The listener may throw an IOException to abort reading (e.g. when canceled).
 */
public class ProgressFilterInputStream extends FilterInputStream {

    public interface Listener {
        void onBytesRead(long totalBytesRead) throws IOException;
    }

    private long mBytesRead = 0;
    private final Listener mListener;

    public ProgressFilterInputStream(InputStream in, Listener listener) {
        super(in);
        mListener = listener;
    }

    @Override
    public int read() throws IOException {
        int b = super.read();
        if (b >= 0)
            report(1);
        return b;
    }

    @Override
    public int read(@NonNull byte[] buffer, int offset, int count) throws IOException {
        int n = super.read(buffer, offset, count);
        if (n > 0)
            report(n);
        return n;
    }

    @Override
    public long skip(long count) throws IOException {
        long skipped = super.skip(count);
        if (skipped > 0)
            report(skipped);
        return skipped;
    }

    private void report(long bytes) throws IOException {
        mBytesRead += bytes;
        mListener.onBytesRead(mBytesRead);
    }
}
