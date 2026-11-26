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

import com.holdingscythe.pocketamcreader.S;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class UtilsTest {

    @Test
    public void arrayToStringJoinsValues() {
        assertEquals("a, b, c", Utils.arrayToString(new String[]{"a", "b", "c"}, ", "));
    }

    @Test
    public void arrayToStringSkipsNullAndEmptyValues() {
        assertEquals("a|c", Utils.arrayToString(new String[]{"a", null, "", "c"}, "|"));
        assertEquals("", Utils.arrayToString(new String[]{null, ""}, "|"));
        assertEquals("", Utils.arrayToString(new String[0], "|"));
    }

    @Test
    public void arrayToStringOfNullArrayIsNull() {
        assertNull(Utils.arrayToString((String[]) null, ", "));
    }

    @Test
    public void arrayToStringAcceptsLists() {
        assertEquals("x; y", Utils.arrayToString(new ArrayList<>(Arrays.asList("x", "", "y")), "; "));
    }

    @Test
    public void joinArraysKeepsOrder() {
        assertArrayEquals(new String[]{"a", "b", "c", "d"},
                Utils.joinArrays(new String[]{"a", "b"}, new String[]{}, new String[]{"c", "d"}));
        assertEquals(0, Utils.joinArrays().length);
    }

    @Test
    public void ratingStringRepeatsTheSymbol() {
        assertEquals("***", Utils.getRatingString("*", 3));
        assertEquals("", Utils.getRatingString("*", 0));
    }

    @Test
    public void safeLongToIntKeepsValuesInRange() {
        assertEquals(0, Utils.safeLongToInt(0));
        assertEquals(Integer.MAX_VALUE, Utils.safeLongToInt(Integer.MAX_VALUE));
        assertEquals(Integer.MIN_VALUE, Utils.safeLongToInt(Integer.MIN_VALUE));
    }

    @Test
    public void safeLongToIntRejectsValuesOutOfRange() {
        assertThrows(IllegalArgumentException.class, () -> Utils.safeLongToInt(Integer.MAX_VALUE + 1L));
        assertThrows(IllegalArgumentException.class, () -> Utils.safeLongToInt(Integer.MIN_VALUE - 1L));
    }

    @Test
    public void coalesceReturnsFirstNonNullValue() {
        //noinspection ObviousNullCheck
        assertEquals("one", Utils.coalesce("one", "two"));
        assertEquals("two", Utils.coalesce(null, "two"));
        //noinspection ConstantValue
        assertNull(Utils.coalesce(null, null));
        String same = "same";
        assertSame(same, Utils.coalesce(same, "other"));
    }

    @Test
    public void fileExtensionWorksWithBothSeparators() {
        // Regression: videos\sample2.mkv and videos/sample2.mkv must give the same result
        assertEquals("mkv", Utils.getFileExtension("videos/sample2.mkv"));
        assertEquals("mkv", Utils.getFileExtension("videos\\sample2.mkv"));
        assertEquals("mkv", Utils.getFileExtension("C:\\Movies\\sample2.mkv"));
    }

    @Test
    public void fileExtensionIsLowerCaseAndAllowsSpaces() {
        assertEquals("mkv", Utils.getFileExtension("My Movie (2020).MKV"));
        assertEquals("gz", Utils.getFileExtension("archive.tar.gz"));
    }

    @Test
    public void fileExtensionIgnoresDotsInFolderNames() {
        assertEquals("", Utils.getFileExtension("folder.v2/movie"));
        assertEquals("", Utils.getFileExtension("folder.v2\\movie"));
    }

    @Test
    public void fileExtensionIsEmptyWithoutExtension() {
        assertEquals("", Utils.getFileExtension("movie"));
        assertEquals("", Utils.getFileExtension("movie."));
        assertEquals("", Utils.getFileExtension(""));
    }

    @Test
    public void defaultAndUnknownColorTagsAreNotCustom() {
        assertFalse(Utils.isCustomColorTagSet(null));
        assertFalse(Utils.isCustomColorTagSet(""));
        assertFalse(Utils.isCustomColorTagSet(S.COLOR_TAG_DEFAULT));
        assertFalse(Utils.isCustomColorTagSet("no-such-tag"));
    }

    @Test
    public void everyOtherKnownColorTagIsCustomAndHasItsOwnColor() {
        int custom = 0;
        for (String tag : S.COLOR_TAGS.keySet()) {
            if (tag.equals(S.COLOR_TAG_DEFAULT))
                continue;
            custom++;
            assertTrue(tag, Utils.isCustomColorTagSet(tag));
            assertEquals(tag, (int) S.COLOR_TAGS.get(tag), Utils.getColorFromColorTag(tag));
        }
        assertTrue(custom > 0);
    }

    @Test
    public void missingColorTagFallsBackToDefaultColor() {
        int defaultColor = S.COLOR_TAGS.get(S.COLOR_TAG_DEFAULT);

        assertEquals(defaultColor, Utils.getColorFromColorTag(null));
        assertEquals(defaultColor, Utils.getColorFromColorTag("no-such-tag"));
        assertEquals(defaultColor, Utils.getColorFromColorTag(S.COLOR_TAG_DEFAULT));
    }
}
