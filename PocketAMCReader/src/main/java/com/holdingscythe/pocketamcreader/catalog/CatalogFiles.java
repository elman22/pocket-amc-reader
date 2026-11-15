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

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.holdingscythe.pocketamcreader.S;
import com.holdingscythe.pocketamcreader.utils.TreeIndex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Private copy of catalog pictures.
 * <p>
 * Android doesn't allow reading arbitrary files anymore, only the folder the user granted. Instead of resolving every
 * picture through the Storage Access Framework while scrolling the list, pictures are copied to app storage during
 * import (only new and changed ones) and the rest of the app keeps working with plain files.
 */
public final class CatalogFiles {

    private static final String DIRECTORY = "catalog_files";
    private static final int BUFFER_SIZE = 64 * 1024;
    private static final long MODIFIED_TOLERANCE_MS = 2000;

    public interface Progress {
        /**
         * @throws IOException to cancel the copy
         */
        void onProgress(int percent) throws IOException;
    }

    private CatalogFiles() {
    }

    public static File root(@NonNull Context context) {
        return new File(context.getFilesDir(), DIRECTORY);
    }

    /**
     * Value for the "pictures folder" setting: all other code just prepends it to relative picture paths
     */
    public static String rootPrefix(@NonNull Context context) {
        return root(context).getAbsolutePath() + "/";
    }

    public static void clear(@NonNull Context context) {
        deleteRecursively(root(context));
    }

    /**
     * Copy referenced pictures from the granted folder and remove pictures which are not referenced anymore.
     *
     * @param relativePaths picture paths exactly as stored in the catalog (relative to the catalog folder)
     * @throws IOException when canceled by the progress callback
     */
    public static void sync(@NonNull Context context, @NonNull Uri treeUri, @NonNull Collection<String> relativePaths,
                            @Nullable Progress progress) throws IOException {

        File root = root(context);
        if (!root.isDirectory() && !root.mkdirs())
            throw new IOException("Couldn't create " + root);

        String rootCanonical = root.getCanonicalPath() + File.separator;
        ContentResolver resolver = context.getContentResolver();
        TreeIndex index = new TreeIndex(resolver, treeUri);
        Set<String> keep = new HashSet<>();

        int total = relativePaths.size();
        int done = 0;
        int failed = 0;

        for (String relativePath : relativePaths) {
            if (progress != null)
                progress.onProgress(total == 0 ? 100 : done * 100 / total);
            done++;

            String normalized = normalize(relativePath);
            if (normalized == null) {
                failed++;
                continue;
            }

            File destination = new File(root, normalized);
            String destinationCanonical = destination.getCanonicalPath();
            if (!destinationCanonical.startsWith(rootCanonical)) {
                // never write outside our folder
                failed++;
                continue;
            }

            TreeIndex.Entry source = index.resolve(normalized);
            if (source == null) {
                if (S.VERBOSE)
                    Log.v(S.TAG, "Missing picture: " + normalized);
                failed++;
                continue;
            }

            keep.add(destinationCanonical);

            if (isUpToDate(destination, source))
                continue;

            if (!copy(resolver, index.uriOf(source), destination, source.lastModified))
                failed++;
        }

        if (progress != null)
            progress.onProgress(100);

        prune(root, keep);

        if (S.INFO)
            Log.i(S.TAG, "Pictures synchronized: " + total + " referenced, " + failed + " missing or failed.");

    }

    /**
     * Backslashes to slashes, no empty or "." segments, null for paths with "../"
     */
    @Nullable
    static String normalize(@Nullable String path) {
        if (path == null)
            return null;

        StringBuilder result = new StringBuilder();
        for (String part : path.replace('\\', '/').trim().split("/")) {
            if (part.isEmpty() || part.equals("."))
                continue;
            if (part.equals(".."))
                return null;
            if (result.length() > 0)
                result.append('/');
            result.append(part);
        }

        return result.length() == 0 ? null : result.toString();
    }

    private static boolean isUpToDate(File destination, TreeIndex.Entry source) {
        if (!destination.isFile())
            return false;
        if (source.size >= 0 && destination.length() != source.size)
            return false;
        return source.lastModified <= 0
                || Math.abs(destination.lastModified() - source.lastModified) < MODIFIED_TOLERANCE_MS;
    }

    private static boolean copy(ContentResolver resolver, Uri source, File destination, long lastModified) {
        File parent = destination.getParentFile();
        if (parent != null && !parent.isDirectory() && !parent.mkdirs())
            return false;

        File temporary = new File(parent, destination.getName() + ".part");

        try (InputStream in = resolver.openInputStream(source);
             OutputStream out = new FileOutputStream(temporary)) {
            if (in == null)
                return false;

            byte[] buffer = new byte[BUFFER_SIZE];
            int read;
            while ((read = in.read(buffer)) != -1)
                out.write(buffer, 0, read);
        } catch (IOException | RuntimeException e) {
            if (S.WARN)
                Log.w(S.TAG, "Couldn't copy picture " + destination.getName(), e);
            //noinspection ResultOfMethodCallIgnored
            temporary.delete();
            return false;
        }

        if ((destination.exists() && !destination.delete()) || !temporary.renameTo(destination)) {
            //noinspection ResultOfMethodCallIgnored
            temporary.delete();
            return false;
        }

        if (lastModified > 0)
            //noinspection ResultOfMethodCallIgnored
            destination.setLastModified(lastModified);

        return true;
    }

    /**
     * Delete files that are not referenced anymore (and empty folders)
     */
    private static void prune(File directory, Set<String> keep) {
        File[] files = directory.listFiles();
        if (files == null)
            return;

        for (File file : files) {
            if (file.isDirectory()) {
                prune(file, keep);
                String[] left = file.list();
                if (left != null && left.length == 0)
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
            } else {
                String canonical;
                try {
                    canonical = file.getCanonicalPath();
                } catch (IOException e) {
                    canonical = file.getAbsolutePath();
                }
                if (!keep.contains(canonical))
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
            }
        }
    }

    private static void deleteRecursively(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children)
                deleteRecursively(child);
        }
        //noinspection ResultOfMethodCallIgnored
        file.delete();
    }
}
