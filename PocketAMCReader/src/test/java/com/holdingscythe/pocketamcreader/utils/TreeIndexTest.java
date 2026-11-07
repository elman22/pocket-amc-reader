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

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class TreeIndexTest {

    private static final String DIR = "vnd.android.document/directory";

    private final Map<String, List<TreeIndex.Entry>> tree = new HashMap<>();
    private final List<String> listed = new ArrayList<>();
    private TreeIndex index;

    private static TreeIndex.Entry file(String id, String name) {
        return new TreeIndex.Entry(id, name, "application/octet-stream", 10, 1000);
    }

    private static TreeIndex.Entry dir(String id, String name) {
        return new TreeIndex.Entry(id, name, DIR, -1, 0);
    }

    @Before
    public void setUp() {
        // root
        // |- catalog.xml, Other.XML, notes.txt, Movie.MKV, folder.xml (a directory)
        // |- pictures
        // |  |- Poster One.jpg, Dup.jpg, dup.jpg
        // |  |- sub
        // |     |- deep.PNG
        tree.put("root", Arrays.asList(
                file("f-catalog", "catalog.xml"),
                file("f-other", "Other.XML"),
                file("f-notes", "notes.txt"),
                file("f-movie", "Movie.MKV"),
                dir("d-folderxml", "folder.xml"),
                dir("d-pictures", "pictures")));
        tree.put("d-pictures", Arrays.asList(
                file("f-poster", "Poster One.jpg"),
                file("f-dup-upper", "Dup.jpg"),
                file("f-dup-lower", "dup.jpg"),
                dir("d-sub", "sub")));
        tree.put("d-sub", Arrays.asList(file("f-deep", "deep.PNG")));

        index = new TreeIndex("root", dirDocId -> {
            listed.add(dirDocId);
            return tree.get(dirDocId);
        });
    }

    private void assertResolves(String expectedDocId, String path) {
        TreeIndex.Entry entry = index.resolve(path);
        assertNotNull("not found: " + path, entry);
        assertEquals(expectedDocId, entry.docId);
    }

    @Test
    public void resolvesFileInRoot() {
        assertResolves("f-movie", "Movie.MKV");
    }

    @Test
    public void resolvesNestedFileWithForwardSlashes() {
        assertResolves("f-poster", "pictures/Poster One.jpg");
        assertResolves("f-deep", "pictures/sub/deep.PNG");
    }

    @Test
    public void resolvesNestedFileWithBackslashes() {
        // Catalogs created on Windows
        assertResolves("f-poster", "pictures\\Poster One.jpg");
        assertResolves("f-deep", "pictures\\sub\\deep.PNG");
    }

    @Test
    public void ignoresEmptyAndCurrentDirectorySegments() {
        assertResolves("f-deep", "./pictures//sub/./deep.PNG");
        assertResolves("f-movie", "/Movie.MKV");
    }

    @Test
    public void fallsBackToCaseInsensitiveMatch() {
        assertResolves("f-poster", "PICTURES/poster one.JPG");
        assertResolves("f-deep", "Pictures/SUB/Deep.png");
    }

    @Test
    public void prefersExactMatchOverCaseInsensitiveOne() {
        assertResolves("f-dup-upper", "pictures/Dup.jpg");
        assertResolves("f-dup-lower", "pictures/dup.jpg");
    }

    @Test
    public void returnsNullForMissingFiles() {
        assertNull(index.resolve("missing.jpg"));
        assertNull(index.resolve("pictures/missing.jpg"));
        assertNull(index.resolve("nowhere/Poster One.jpg"));
    }

    @Test
    public void returnsNullForDirectories() {
        assertNull(index.resolve("pictures"));
        assertNull(index.resolve("pictures/sub"));
        assertNull(index.resolve("pictures/"));
    }

    @Test
    public void returnsNullWhenPathGoesThroughAFile() {
        assertNull(index.resolve("catalog.xml/anything.jpg"));
    }

    @Test
    public void neverLeavesTheGrantedFolder() {
        assertNull(index.resolve("../catalog.xml"));
        assertNull(index.resolve("pictures/../catalog.xml"));
        assertNull(index.resolve("pictures/sub/.."));
    }

    @Test
    public void returnsNullForNullAndEmptyPaths() {
        assertNull(index.resolve(null));
        assertNull(index.resolve(""));
        assertNull(index.resolve("///"));
    }

    @Test
    public void listsEveryDirectoryOnlyOnce() {
        index.resolve("pictures/Poster One.jpg");
        index.resolve("pictures/Dup.jpg");
        index.resolve("pictures/sub/deep.PNG");
        index.resolve("pictures/sub/missing.png");
        index.resolve("Movie.MKV");

        assertEquals(Arrays.asList("root", "d-pictures", "d-sub"), listed);
    }

    @Test
    public void providerReturningNothingMeansEmptyDirectory() {
        TreeIndex empty = new TreeIndex("root", dirDocId -> null);

        assertNull(empty.resolve("anything.jpg"));
        assertEquals(0, empty.children(null).size());
    }

    @Test
    public void childrenOfRootAreListedInProviderOrder() {
        List<TreeIndex.Entry> children = index.children(null);

        assertEquals(6, children.size());
        assertEquals("catalog.xml", children.get(0).name);
        assertEquals("pictures", children.get(5).name);
    }

    @Test
    public void entryKnowsWhetherItIsADirectory() {
        assertEquals(true, dir("x", "x").isDirectory());
        assertEquals(false, file("y", "y").isDirectory());
    }

    @Test
    public void xmlFilesInRootAreSortedAndDirectoriesIgnored() {
        List<TreeIndex.Entry> xml = SafHelper.listXmlFiles(index);

        assertEquals(2, xml.size());
        assertEquals("catalog.xml", xml.get(0).name);
        assertEquals("Other.XML", xml.get(1).name);
    }
}
