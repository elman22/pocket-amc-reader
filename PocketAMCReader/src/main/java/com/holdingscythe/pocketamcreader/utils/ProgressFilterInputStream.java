/*
    This file is part of Pocket AMC Reader.
    Copyright © 2010-2020 Elman <holdingscythe@zoznam.sk>

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

import android.util.Log;

import com.holdingscythe.pocketamcreader.ImportFragment;
import com.holdingscythe.pocketamcreader.S;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

import androidx.annotation.NonNull;

/**
 * Reads file input stream and returns bytes read back to AsyncTask.
 */
public class ProgressFilterInputStream extends FilterInputStream {

    private long mBytesRead = 0;
    private ImportFragment.CatalogImportTask mCatalogImportTask;

    public ProgressFilterInputStream(InputStream in, ImportFragment.CatalogImportTask cit) {
        super(in);
        mCatalogImportTask = cit;
    }

    @Override
    public int read(@NonNull byte[] buffer, int offset, int count) throws IOException {
        mBytesRead += count;
        mCatalogImportTask.publishExternalProgress(mBytesRead);

        if (S.VERBOSE)
            Log.v(S.TAG, "Bytes read: " + mBytesRead);

        return super.read(buffer, offset, count);
    }
}