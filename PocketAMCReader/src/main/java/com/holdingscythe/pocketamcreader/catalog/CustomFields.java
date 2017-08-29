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

package com.holdingscythe.pocketamcreader.catalog;

import android.app.Activity;
import android.database.Cursor;
import android.text.util.Linkify;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.holdingscythe.pocketamcreader.R;
import com.holdingscythe.pocketamcreader.S;
import com.holdingscythe.pocketamcreader.model.CustomFieldsModel;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;

import java.util.Date;

/**
 * Add custom fields to base movie detail layout
 */
public class CustomFields {
    private Activity mActivity;

    /**
     * Instantiates a new Custom fields.
     *
     * @param cursor   the cursor
     * @param view     the view
     * @param activity the activity
     */
    public CustomFields(Cursor cursor, View view, Activity activity) {
        mActivity = activity;

        if (cursor.getCount() > 0) {
            LinearLayout wrapperLayout = view.findViewById(R.id.CustomFieldsWrapper);
            wrapperLayout.setVisibility(View.VISIBLE);

            TextView textViewSection = view.findViewById(R.id.customTitle);
            textViewSection.setVisibility(View.VISIBLE);

            // add all extras from DB to the array list
            try {
                do {
                    // Fill model
                    CustomFieldsModel customField = new CustomFieldsModel(
                            cursor.getString(cursor.getColumnIndex(Movies.C_TYPE)),
                            cursor.getString(cursor.getColumnIndex(Movies.C_NAME)),
                            cursor.getString(cursor.getColumnIndex(Movies.C_VALUE))
                    );

                    // Inflate layout, because this is the only way to set correct theme
                    LinearLayout customLayout = (LinearLayout) activity.getLayoutInflater().inflate(R.layout
                            .details_custom_field, null);

                    TextView customLabel = customLayout.findViewById(R.id.customLabel);
                    customLabel.setText(customField.getCName());

                    TextView customValue = customLayout.findViewById(R.id.customValue);

                    // If custom field is of type URL, make it clickable
                    if (customField.getCType().equals(CustomFieldsModel.CFT_URL)) {
                        customValue.setAutoLinkMask(Linkify.ALL);
                    }

                    customValue.setText(FormatValue(customField.getCType(), customField.getCValue()));

                    wrapperLayout.addView(customLayout);
                } while (cursor.moveToNext());

                if (S.DEBUG)
                    Log.d(S.TAG, "Total custom fields found: " + String.valueOf(cursor.getCount()));

            } catch (Exception e) {
                if (S.ERROR)
                    Log.e(S.TAG, "Custom fields couldn't be loaded from DB: " + e.toString());
            }
        }
    }

    /**
     * Format value according to the type
     * If updating formatting, be sure to also check formatting in @Movie.java
     *
     * @param type  custom field type
     * @param value custom field value
     * @return String
     */
    private String FormatValue(String type, String value) {
        switch (type) {
            case CustomFieldsModel.CFT_BOOLEAN:
                if (value.equals("True"))
                    value = mActivity.getString(R.string.details_boolean_true);
                else
                    value = mActivity.getString(R.string.details_boolean_false);
                break;
            case CustomFieldsModel.CFT_DATE:
                try {
                    Date parsedDate = SharedObjects.getInstance().dateAddedFormat.parse(value);
                    value = SharedObjects.getInstance().dateFormat.format(parsedDate);
                } catch (Exception e) {
                    // don't do anything, keep date as is
                }
                break;
            case CustomFieldsModel.CFT_REAL:
            case CustomFieldsModel.CFT_REAL1:
            case CustomFieldsModel.CFT_REAL2:
                // Don't use locale unless rating, user rating and framerate use locale
                // value = NumberFormat.getInstance().format(Float.valueOf(value));
                break;
        }

        return value;
    }

}