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

package com.holdingscythe.pocketamcreader.catalog;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Picture paths come from the catalog XML (often created on Windows) and are used to write files in app storage
 */
public class CatalogFilesTest {

    @Test
    public void backslashesBecomeSlashes() {
        assertEquals("Pictures/Movie.jpg", CatalogFiles.normalize("Pictures\\Movie.jpg"));
    }

    @Test
    public void slashesAreKept() {
        assertEquals("Pictures/Movie.jpg", CatalogFiles.normalize("Pictures/Movie.jpg"));
    }

    @Test
    public void leadingAndRepeatedSeparatorsAreRemoved() {
        assertEquals("Pictures/a.jpg", CatalogFiles.normalize("/Pictures//a.jpg"));
        assertEquals("Pictures/a.jpg", CatalogFiles.normalize("\\Pictures\\\\a.jpg"));
    }

    @Test
    public void currentDirectorySegmentsAreRemoved() {
        assertEquals("a/b.jpg", CatalogFiles.normalize("./a/./b.jpg"));
    }

    @Test
    public void surroundingWhitespaceIsTrimmedButNamesKeepTheirSpaces() {
        assertEquals("dir/file.jpg", CatalogFiles.normalize("  dir/file.jpg "));
        assertEquals("My Movie (2020)/poster 1.jpg", CatalogFiles.normalize("My Movie (2020)/poster 1.jpg"));
    }

    @Test
    public void pathsLeavingTheFolderAreRejected() {
        assertNull(CatalogFiles.normalize("../x.jpg"));
        assertNull(CatalogFiles.normalize("a/../b.jpg"));
        assertNull(CatalogFiles.normalize("a/b/.."));
        assertNull(CatalogFiles.normalize("..\\x.jpg"));
    }

    @Test
    public void emptyPathsAreRejected() {
        assertNull(CatalogFiles.normalize(null));
        assertNull(CatalogFiles.normalize(""));
        assertNull(CatalogFiles.normalize("   "));
        assertNull(CatalogFiles.normalize("."));
        assertNull(CatalogFiles.normalize("/"));
        assertNull(CatalogFiles.normalize("\\"));
    }
}
