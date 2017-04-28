/**
 * Pocket AMC Reader
 * Created by elman on 27.4.2017.
 */

package com.holdingscythe.pocketamcreader.settings;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.holdingscythe.pocketamcreader.R;
import com.holdingscythe.pocketamcreader.S;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SettingsListFieldsActivity extends FragmentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_list_fields);

        // Set default font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(S.DEFAULT_FONT)
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        // Display the fragment as the main content.
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.list_fields_wrapper, new SettingsListFieldsFragment())
                .commit();
    }

    /**
     * Wrap the Activity Context for Calligraphy
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

}