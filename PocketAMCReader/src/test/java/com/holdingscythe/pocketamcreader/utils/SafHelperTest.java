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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SafHelperTest {

    @Test
    public void contentUrisAreRecognized() {
        assertTrue(SafHelper.isContentUri("content://com.android.externalstorage.documents/tree/primary%3AAMC"));
    }

    @Test
    public void plainPathsAndEmptyValuesAreNotContentUris() {
        // paths selected by versions before 3.0.0 have to be selected again
        assertFalse(SafHelper.isContentUri("/storage/emulated/0/AMC/catalog.xml"));
        assertFalse(SafHelper.isContentUri("file:///storage/emulated/0/AMC/catalog.xml"));
        assertFalse(SafHelper.isContentUri(""));
        assertFalse(SafHelper.isContentUri(null));
    }
}
