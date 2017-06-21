package com.holdingscythe.pocketamcreader.utils;

import android.content.Context;
import android.util.Log;

import com.holdingscythe.pocketamcreader.S;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * A utility to convert catalog XML files to UTF-8.
 */
public class FileEncoder {

    /**
     * Encodings
     * http://download.oracle.com/javase/1.3/docs/guide/intl/encoding.doc.html
     */
    public static String encodeToUtf(Context context, String pInputFilePath, String pOutputFileName, String encoding,
                                     Boolean removeBadChars) {

        int bufferSize = 131072;

        try {
            File inputFile = new File(pInputFilePath);
            String outputFilePath = context.getFilesDir().getAbsolutePath() + pOutputFileName;
            FileInputStream fis = new FileInputStream(inputFile);
            FileOutputStream fos = new FileOutputStream(outputFilePath);

            byte[] contents = new byte[bufferSize];

            int readBytes = 0;
            while (readBytes < inputFile.length()) {
                if (readBytes + bufferSize > inputFile.length()) {
                    bufferSize = (int) (inputFile.length() - readBytes);
                    contents = new byte[bufferSize];
                }

                if (fis.read(contents, 0, bufferSize) == 0) {
                    throw new IndexOutOfBoundsException();
                }

                String asString = new String(contents, encoding);
                if (removeBadChars) {
                    // Based on http://mickaelvanneufville.online.fr/AMCU/scripts/RemoveBadChars.ifs
                    asString = asString.replaceAll("[\\x00-\\x09\\x0B\\x0C\\x0E-\\x1F\\x7F]", "");
                }
                byte[] newBytes = asString.getBytes("UTF8");
                fos.write(newBytes);

                readBytes += bufferSize;
            }

            fis.close();
            fos.close();

            if (S.INFO)
                Log.i(S.TAG, "File " + pInputFilePath + " converted to " + outputFilePath + ".");

            return outputFilePath;

        } catch (IndexOutOfBoundsException e) {
            if (S.ERROR)
                Log.e(S.TAG, "Couldn't convert file to UTF-8.");
            return null;
        } catch (Exception e) {
            if (S.ERROR)
                Log.e(S.TAG, "Couldn't convert file to UTF-8.");
            return null;
        }
    }
}
