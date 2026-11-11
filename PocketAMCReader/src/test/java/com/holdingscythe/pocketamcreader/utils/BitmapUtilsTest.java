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

import static org.junit.Assert.assertEquals;

public class BitmapUtilsTest {

    @Test
    public void picturesWhichFitAreNotScaled() {
        assertEquals(1, BitmapUtils.sampleSize(800, 600, 2048));
        assertEquals(1, BitmapUtils.sampleSize(2048, 100, 2048));
    }

    @Test
    public void sizeIsDividedByPowersOfTwoUntilTheLongerSideFits() {
        assertEquals(2, BitmapUtils.sampleSize(4000, 3000, 2048));
        assertEquals(4, BitmapUtils.sampleSize(8000, 6000, 2048));
        assertEquals(8, BitmapUtils.sampleSize(16000, 100, 2048));
    }

    @Test
    public void theLongerSideDecides() {
        assertEquals(2, BitmapUtils.sampleSize(100, 4097, 4096));
        assertEquals(1, BitmapUtils.sampleSize(100, 4096, 4096));
    }

    @Test
    public void unknownSizeIsNotScaled() {
        // BitmapFactory reports -1 when it can't read the file
        assertEquals(1, BitmapUtils.sampleSize(-1, -1, 2048));
        assertEquals(1, BitmapUtils.sampleSize(0, 0, 2048));
    }
}
