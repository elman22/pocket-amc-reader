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

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.annotation.Nullable;

public final class BitmapUtils {

    private BitmapUtils() {
    }

    /**
     * The biggest power of two by which the sides of a picture can be divided and the longer side is still not
     * longer than maxSide (1 if it fits, or if the size is not known).
     */
    public static int sampleSize(int width, int height, int maxSide) {
        int longest = Math.max(width, height);
        int sample = 1;

        while (longest / sample > maxSide) {
            sample *= 2;
        }

        return sample;
    }

    /**
     * Decode a picture from a file, scaled down so that its longer side is at most maxSide pixels. Pictures of the
     * catalog can be photos of many megapixels, decoding them in full size needs a lot of memory.
     *
     * @return the picture, null if the file can't be decoded
     */
    @Nullable
    public static Bitmap decodeFile(String path, int maxSide) {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bounds);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, maxSide);

        return BitmapFactory.decodeFile(path, options);
    }
}
