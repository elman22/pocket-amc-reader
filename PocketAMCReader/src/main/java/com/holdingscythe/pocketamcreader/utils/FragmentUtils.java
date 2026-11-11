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

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

public final class FragmentUtils {

    private FragmentUtils() {
    }

    /**
     * Replacement of the deprecated Fragment.onActivityCreated(): run the action when the activity is created
     * (its onCreate() finished, so the whole layout of the activity can be used). Call it from onViewCreated().
     * When the activity was created already the action runs at once. It is skipped when the fragment lost its view
     * or was removed meanwhile.
     */
    public static void runWhenActivityCreated(@NonNull Fragment fragment, @NonNull Runnable action) {
        fragment.requireActivity().getLifecycle().addObserver(new DefaultLifecycleObserver() {
            @Override
            public void onCreate(@NonNull LifecycleOwner owner) {
                owner.getLifecycle().removeObserver(this);

                if (fragment.isAdded() && fragment.getView() != null) {
                    action.run();
                }
            }
        });
    }
}
