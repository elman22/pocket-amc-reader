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

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.DocumentsContract.Document;
import android.util.Log;

import com.holdingscythe.pocketamcreader.S;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Fast, cached access to the content of a folder granted through the Storage Access Framework
 * (ACTION_OPEN_DOCUMENT_TREE).
 * <p>
 * DocumentFile issues one provider query per file attribute, which is far too slow for catalogs with thousands of
 * pictures. This class lists every directory with a single query and resolves relative paths from the cache.
 * Names are matched case sensitively first, then case insensitively (catalogs usually come from Windows).
 */
public class TreeIndex {

    public static final class Entry {
        public final String docId;
        public final String name;
        public final String mime;
        public final long size;
        public final long lastModified;

        Entry(String docId, String name, String mime, long size, long lastModified) {
            this.docId = docId;
            this.name = name;
            this.mime = mime;
            this.size = size;
            this.lastModified = lastModified;
        }

        public boolean isDirectory() {
            return Document.MIME_TYPE_DIR.equals(mime);
        }
    }

    private static final class Directory {
        final List<Entry> all = new ArrayList<>();
        final Map<String, Entry> exact = new HashMap<>();
        final Map<String, Entry> lower = new HashMap<>();
    }

    private static final String[] COLUMNS = {
            Document.COLUMN_DOCUMENT_ID,
            Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_MIME_TYPE,
            Document.COLUMN_SIZE,
            Document.COLUMN_LAST_MODIFIED
    };

    /**
     * Lists the children of a directory (the only part which needs a content provider)
     */
    interface DirectoryLister {
        List<Entry> list(String dirDocId);
    }

    private final ContentResolver mResolver;
    private final Uri mTreeUri;
    private final String mRootDocId;
    private final DirectoryLister mLister;
    private final Map<String, Directory> mCache = new HashMap<>();

    public TreeIndex(@NonNull ContentResolver resolver, @NonNull Uri treeUri) {
        mResolver = resolver;
        mTreeUri = treeUri;
        mRootDocId = null;
        mLister = this::query;
    }

    /**
     * For tests: no content provider, directories come from the lister
     */
    TreeIndex(@NonNull String rootDocId, @NonNull DirectoryLister lister) {
        mResolver = null;
        mTreeUri = null;
        mRootDocId = rootDocId;
        mLister = lister;
    }

    /**
     * Document id of the granted folder
     */
    public String rootDocId() {
        return mRootDocId != null ? mRootDocId : DocumentsContract.getTreeDocumentId(mTreeUri);
    }

    /**
     * Children of the given directory (null for the granted folder itself)
     */
    public List<Entry> children(@Nullable String dirDocId) {
        return directory(dirDocId == null ? rootDocId() : dirDocId).all;
    }

    /**
     * Uri which can be opened with ContentResolver or passed to other applications (with read grant flag)
     */
    public Uri uriOf(Entry entry) {
        return DocumentsContract.buildDocumentUriUsingTree(mTreeUri, entry.docId);
    }

    /**
     * Resolve a path relative to the granted folder, e.g. "Pictures/Movie.jpg" (both separators are accepted).
     *
     * @return file entry or null when it does not exist or the path tries to leave the granted folder
     */
    @Nullable
    public Entry resolve(@Nullable String relativePath) {
        if (relativePath == null)
            return null;

        String[] parts = relativePath.replace('\\', '/').split("/");
        String dirId = rootDocId();
        Entry current = null;

        for (String part : parts) {
            if (part.isEmpty() || part.equals("."))
                continue;
            if (part.equals(".."))
                return null;

            // A file can only be the last segment
            if (current != null) {
                if (!current.isDirectory())
                    return null;
                dirId = current.docId;
            }

            Directory dir = directory(dirId);
            Entry found = dir.exact.get(part);
            if (found == null)
                found = dir.lower.get(part.toLowerCase(Locale.ROOT));
            if (found == null)
                return null;

            current = found;
        }

        return current != null && !current.isDirectory() ? current : null;
    }

    private Directory directory(String dirDocId) {
        Directory dir = mCache.get(dirDocId);
        if (dir != null)
            return dir;

        dir = new Directory();

        List<Entry> entries = mLister.list(dirDocId);
        if (entries != null) {
            for (Entry entry : entries) {
                dir.all.add(entry);
                if (!dir.exact.containsKey(entry.name))
                    dir.exact.put(entry.name, entry);
                String lowerName = entry.name.toLowerCase(Locale.ROOT);
                if (!dir.lower.containsKey(lowerName))
                    dir.lower.put(lowerName, entry);
            }
        }

        mCache.put(dirDocId, dir);
        return dir;
    }

    /**
     * One query lists the whole directory
     */
    private List<Entry> query(String dirDocId) {
        List<Entry> entries = new ArrayList<>();
        Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(mTreeUri, dirDocId);

        try (Cursor c = Objects.requireNonNull(mResolver).query(childrenUri, COLUMNS, null, null, null)) {
            if (c != null) {
                while (c.moveToNext()) {
                    String name = c.getString(1);
                    if (name == null)
                        continue;

                    entries.add(new Entry(
                            c.getString(0),
                            name,
                            c.getString(2),
                            c.isNull(3) ? -1 : c.getLong(3),
                            c.isNull(4) ? 0 : c.getLong(4)));
                }
            }
        } catch (Exception e) {
            if (S.WARN)
                Log.w(S.TAG, "Couldn't list folder " + dirDocId, e);
        }

        return entries;
    }
}
