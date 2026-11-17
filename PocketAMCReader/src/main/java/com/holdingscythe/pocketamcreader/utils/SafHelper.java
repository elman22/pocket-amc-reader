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
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.util.Log;

import com.holdingscythe.pocketamcreader.S;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

/**
 * Helpers for the Storage Access Framework (scoped storage compatible access to the catalog).
 */
public final class SafHelper {

    private SafHelper() {
    }

    public static boolean isContentUri(@Nullable String value) {
        return value != null && value.startsWith(S.INTENT_SCHEME_CONTENT);
    }

    /**
     * Keep read access to the Uri after the app restarts.
     *
     * @param context application or activity context
     * @param uri     Uri of the folder or of the file returned by the picker
     * @return true when the permission was persisted, false when Android did not grant it
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean takeReadPermission(@NonNull Context context, @NonNull Uri uri) {
        try {
            context.getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            return true;
        } catch (SecurityException | IllegalArgumentException e) {
            if (S.WARN)
                Log.w(S.TAG, "Couldn't persist permission for " + uri, e);
            return false;
        }
    }

    /**
     * Give up access we don't need anymore (the number of persisted permissions per app is limited)
     */
    public static void releasePermission(@NonNull Context context, @Nullable String uriString) {
        if (!isContentUri(uriString))
            return;

        try {
            context.getContentResolver().releasePersistableUriPermission(Uri.parse(uriString),
                    Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException | IllegalArgumentException e) {
            if (S.DEBUG)
                Log.d(S.TAG, "Couldn't release permission for " + uriString, e);
        }
    }

    /**
     * Do we still hold access to the catalog (granted either for the folder or for the file itself)?
     */
    public static boolean hasReadPermission(@NonNull Context context, @NonNull Uri catalogUri,
                                            @Nullable Uri treeUri) {
        for (UriPermission permission : context.getContentResolver().getPersistedUriPermissions()) {
            if (!permission.isReadPermission())
                continue;

            Uri granted = permission.getUri();
            if (granted.equals(catalogUri) || (granted.equals(treeUri)))
                return true;
        }
        return false;
    }

    /**
     * All XML files in the root of the granted folder, sorted by name
     */
    @NonNull
    public static List<TreeIndex.Entry> listXmlFiles(@NonNull TreeIndex index) {
        List<TreeIndex.Entry> result = new ArrayList<>();

        for (TreeIndex.Entry entry : index.children(null)) {
            if (!entry.isDirectory() && entry.name.toLowerCase(Locale.ROOT).endsWith(".xml"))
                result.add(entry);
        }

        result.sort((a, b) -> a.name.compareToIgnoreCase(b.name));
        return result;
    }

    /**
     * Find a file referenced from the catalog (e.g. in the "file path" field) and return Uri which can be handed to
     * other applications together with Intent.FLAG_GRANT_READ_URI_PERMISSION.
     * <ul>
     * <li>relative paths are looked up in the folder granted by the user,</li>
     * <li>absolute paths work only when Android still lets the app read them.</li>
     * </ul>
     *
     * @return null when the file can't be found or accessed
     */
    @Nullable
    public static Uri resolveFileLink(@NonNull Context context, @NonNull String path, @Nullable Uri treeUri) {
        String normalized = path.replace('\\', '/');

        if (normalized.startsWith("/")) {
            File file = new File(normalized);
            if (file.isFile() && file.canRead()) {
                try {
                    return FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                } catch (IllegalArgumentException e) {
                    if (S.WARN)
                        Log.w(S.TAG, "File can't be shared: " + normalized, e);
                }
            }
            return null;
        }

        if (treeUri == null)
            return null;

        TreeIndex index = new TreeIndex(context.getContentResolver(), treeUri);
        TreeIndex.Entry entry = index.resolve(normalized);
        return entry == null ? null : index.uriOf(entry);
    }
}
