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
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.holdingscythe.pocketamcreader.R;
import com.holdingscythe.pocketamcreader.S;
import com.holdingscythe.pocketamcreader.model.CustomFieldsModel;

/**
 * Serializable object with custom fields
 */
// TODO CLEAN UP
public class CustomFields {
//    private ArrayList<CustomFieldsModel> mCustomFields;
    private LinearLayout mCustomLayout;

    public CustomFields(Cursor cursor, View view, Activity activity) {
//        mCustomFields = new ArrayList<>();
//        Context context = activity.getBaseContext();

        if (cursor.getCount() > 0) {
            mCustomLayout = view.findViewById(R.id.CustomFieldsWrapper);
            mCustomLayout.setVisibility(View.VISIBLE);

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
                    LinearLayout customLayout = (LinearLayout)activity.getLayoutInflater().inflate(R.layout
                            .details_custom_field, null);

                    TextView customLabel = customLayout.findViewById(R.id.customLabel);
                    customLabel.setText(customField.getCName());

                    TextView customValue = customLayout.findViewById(R.id.customValue);
                    customValue.setText(FormatValue(customField.getCType(), customField.getCValue()));

                    mCustomLayout.addView(customLayout);
                } while (cursor.moveToNext());

                if (S.DEBUG)
                    Log.d(S.TAG, "Total custom fields found: " + String.valueOf(cursor.getCount()));

            } catch (Exception e) {
                if (S.ERROR)
                    Log.e(S.TAG, "Custom fields couldn't be loaded from DB: " + e.toString());
            }
        }
    }

    private String FormatValue(String type, String value) {
        switch (type) {
            case CustomFieldsModel.CFT_BOOLEAN:
                // TODO
                break;
            case CustomFieldsModel.CFT_DATE:
                // TODO
                break;
            case CustomFieldsModel.CFT_REAL:
            case CustomFieldsModel.CFT_REAL1:
            case CustomFieldsModel.CFT_REAL2:
                // TODO
                break;
            case CustomFieldsModel.CFT_URL:
                // TODO
                break;
        }

        return value;
    }

    /**
     * Return array list of all custom fields
     */
//    public ArrayList<CustomFieldsModel> getCustomFieldsList() {
//        return this.mCustomFields;
//    }
}