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

import android.content.Context;
import android.util.Log;

import com.holdingscythe.pocketamcreader.S;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * A utility to convert catalog XML files to UTF-8.
 * <p>
 * The file is converted as a character stream, so multi-byte characters (e.g. in CP932, GBK or UTF-16 catalogs)
 * are never split on buffer boundaries.
 */
public class FileEncoder {

    private static final int BUFFER_SIZE = 64 * 1024;

    /**
     * Encodings
     * <a href="http://download.oracle.com/javase/1.3/docs/guide/intl/encoding.doc.html">...</a>
     *
     * @return path of the converted file or null on failure
     */
    public static String encodeToUtf(Context context, String pInputFilePath, String pOutputFileName, String encoding,
                                     boolean removeBadChars) {

        File inputFile = new File(pInputFilePath);
        String outputFilePath = context.getFilesDir().getAbsolutePath() + pOutputFileName;
        File outputFile = new File(outputFilePath);
        boolean success = false;

        try (Reader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(inputFile), Charset.forName(encoding)), BUFFER_SIZE);
             Writer writer = new BufferedWriter(
                     new OutputStreamWriter(new FileOutputStream(outputFile), StandardCharsets.UTF_8), BUFFER_SIZE)) {

            char[] buffer = new char[BUFFER_SIZE];
            boolean first = true;
            int read;

            while ((read = reader.read(buffer)) != -1) {
                int start = 0;

                // Drop byte order mark, parser is told explicitly that data is UTF-8
                if (first) {
                    first = false;
                    if (read > 0 && buffer[0] == '\uFEFF')
                        start = 1;
                }

                if (removeBadChars) {
                    // Based on http://mickaelvanneufville.online.fr/AMCU/scripts/RemoveBadChars.ifs
                    // Removes [\x00-\x09\x0B\x0C\x0E-\x1F\x7F]
                    int out = 0;
                    for (int i = start; i < read; i++) {
                        char c = buffer[i];
                        if (!isBadChar(c))
                            buffer[out++] = c;
                    }
                    writer.write(buffer, 0, out);
                } else {
                    writer.write(buffer, start, read - start);
                }
            }

            success = true;

            if (S.INFO)
                Log.i(S.TAG, "File " + pInputFilePath + " converted to " + outputFilePath + ".");

            return outputFilePath;

        } catch (Exception e) {
            if (S.ERROR)
                Log.e(S.TAG, "Couldn't convert file to UTF-8.", e);
            return null;
        } finally {
            // Don't leave half-written temporary file behind
            if (!success && outputFile.exists() && !outputFile.delete() && S.WARN)
                Log.w(S.TAG, "Couldn't delete incomplete temporary file.");
        }
    }

    private static boolean isBadChar(char c) {
        return c <= 0x09 || c == 0x0B || c == 0x0C || (c >= 0x0E && c <= 0x1F) || c == 0x7F;
    }
}
