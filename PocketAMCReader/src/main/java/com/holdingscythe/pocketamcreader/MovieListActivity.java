/*
    This file is part of Pocket AMC Reader.
    Copyright © 2010-2017 Elman <holdingscythe@zoznam.sk>

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

package com.holdingscythe.pocketamcreader;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;

import com.holdingscythe.pocketamcreader.settings.SettingsActivity;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * An activity representing a list of Movies. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MovieDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 * <p/>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link MovieListFragment} and the item details
 * (if present) is a {@link MovieDetailFragment}.
 * <p/>
 * This activity also implements the required
 * {@link MovieListFragment.Callbacks} interface
 * to listen for item selections.
 */
public class MovieListActivity extends AppCompatActivity implements MovieListFragment.Callbacks {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet device.
     */
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (S.STRICT) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .penaltyDeath()
                    .build());
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        // Set default font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(S.DEFAULT_FONT)
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        Toolbar mToolbar = (Toolbar) findViewById(R.id.quick_return_toolbar);
        setSupportActionBar(mToolbar);

        if (savedInstanceState == null) {
            // Create the list fragment and add it to the activity using a fragment transaction.
            MovieListFragment fragment = new MovieListFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.movie_list_container, fragment)
                    .commit();
        }

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts (res/values-large and
            // res/values-sw600dp). If this view is present, then the activity should be in two-pane mode.
            mTwoPane = SharedObjects.getInstance().twoPane = true;

            // In two-pane mode, list items should be given the 'activated' state when touched.
            ((MovieListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.movie_list_container))
                    .setActivateOnItemClick(true);
        }

        // Create global configuration and initialize ImageLoader with this configuration
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .build();
        ImageLoader.getInstance().init(config);

    }

    /**
     * Wrap the Activity Context for Calligraphy
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    /**
     * Callback method from {@link MovieListFragment.Callbacks}
     * indicating that the item with the given ID was selected.
     */
    @Override
    public void onItemSelected(String id) {
        if (mTwoPane) {
            // In two-pane mode, show the detail view in this activity by adding or replacing the detail fragment
            // using a fragment transaction.
            Bundle arguments = new Bundle();
            if (!id.equals("")) {
                arguments.putString(MovieDetailFragment.ARG_MOVIE_ID, id);
            }
            MovieDetailFragmentHost fragment = new MovieDetailFragmentHost();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment)
                    .commit();
        } else {
            // In single-pane mode, simply start the detail activity for the selected item ID.
            Intent detailIntent = new Intent(this, MovieDetailActivity.class);
            detailIntent.putExtra(MovieDetailFragment.ARG_MOVIE_ID, id);
            startActivity(detailIntent);
        }
    }

    /**
     * Catch menu clicked actions
     */
    @SuppressLint("StringFormatMatches")
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Filters and Sorting are handled in MovieListFragment
        switch (item.getItemId()) {
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                return true;
            case R.id.menu_about:
                // Get version
                String versionName = "";
                try {
                    versionName = getApplicationContext().getPackageManager().getPackageInfo(getPackageName(),
                            0).versionName;
                } catch (PackageManager.NameNotFoundException e) {
                    if (S.ERROR)
                        Log.e(S.TAG, "Could not extract Application Version");
                }

                // Translators
                String t_ru = getString(R.string.locale_ru_local) + getString(R.string.locale_translation_separator)
                        + getString(R.string.locale_ru_translator);
                String t_de = getString(R.string.locale_de_local) + getString(R.string.locale_translation_separator)
                        + getString(R.string.locale_de_translator);
                String t_fr = getString(R.string.locale_fr_local) + getString(R.string.locale_translation_separator)
                        + getString(R.string.locale_fr_translator);
                String t_sk = getString(R.string.locale_sk_local) + getString(R.string.locale_translation_separator)
                        + getString(R.string.locale_sk_translator);
                String t_pl = getString(R.string.locale_pl_local) + getString(R.string.locale_translation_separator)
                        + getString(R.string.locale_pl_translator);

                // Prepare alert dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.app_name));
                builder.setMessage(String.format(getString(R.string.about), getString(R.string.app_name), versionName,
                        getString(R.string.copyright_year), t_ru + "\n" + t_de + "\n" + t_fr + "\n" + t_sk + "\n" + t_pl,
                        getString(R.string.used_libraries)));
                builder.setNeutralButton(getString(R.string.dialog_positive), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
