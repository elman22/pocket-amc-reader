package com.holdingscythe.pocketamcreader.utils;

import android.util.Log;

import com.holdingscythe.pocketamcreader.ImportFragment;
import com.holdingscythe.pocketamcreader.S;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads file input stream and returns bytes read back to AsyncTask.
 */
public class ProgressFilterInputStream extends FilterInputStream {

    private int mBytesRead = 0;
    private ImportFragment.CatalogImportTask mCatalogImportTask;

    public ProgressFilterInputStream(InputStream in, ImportFragment.CatalogImportTask cit) {
        super(in);
        mCatalogImportTask = cit;
    }

    @Override
    public int read(byte[] buffer, int offset, int count) throws IOException {
        mBytesRead += count;
        mCatalogImportTask.publishExternalProgress(mBytesRead);

        if (S.VERBOSE)
            Log.v(S.TAG, "Bytes read: " + String.valueOf(mBytesRead));

        return super.read(buffer, offset, count);
    }
}