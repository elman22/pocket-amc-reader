/*
    This file is part of Pocket AMC Reader.
    Copyright © 2010-2020 Elman <holdingscythe@zoznam.sk>

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

package com.holdingscythe.pocketamcreader.filters;

import android.content.Context;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.util.Log;

import com.holdingscythe.pocketamcreader.R;
import com.holdingscythe.pocketamcreader.S;
import com.holdingscythe.pocketamcreader.catalog.Movies;
import com.holdingscythe.pocketamcreader.utils.Utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;

public class Filters implements Serializable {
    private ArrayList<Filter> mFilters = new ArrayList<>();
    private transient Context mContext;

    public Filters() {
    }

    public void setContext(Context c) {
        mContext = c;
    }

    /**
     * Add new filter to collection
     */
    public void addFilter(Filter filter) {
        mFilters.add(filter);
    }

    /**
     * Return filter fields to SQL command
     */
    public String getFilterFields() {
        if (mFilters.size() == 0)
            return null;

        ArrayList<String> conditions = new ArrayList<>();
        String sqlWhere;

        for (Filter f : mFilters) {
            if (f.getFilterOperator() == Movies.FILTER_OPERATOR_IS_NULL_NOT) {
                conditions.add(f.getSQLField() + " is not null");
            } else if (f.getFilterOperator() == Movies.FILTER_OPERATOR_IS_NULL || f.getSQLValue() == null) {
                conditions.add(f.getSQLField() + " is null");
            } else {
                conditions.add(f.getSQLField() + " " + f.getSQLOperator() + " ?");
            }
        }

        sqlWhere = Utils.arrayToString(conditions.toArray(new String[0]), " and ");

        // Log event
        if (S.DEBUG)
            Log.d(S.TAG, "Filter where: " + sqlWhere);

        return sqlWhere;
    }

    /**
     * Return filter fields DB names in array
     */
    public HashSet<String> getFilterFieldsDB() {
        if (mFilters.size() == 0)
            return null;

        HashSet<String> dbFields = new HashSet<>();

        for (Filter f : mFilters) {
            dbFields.add(f.getSQLField());
        }

        return dbFields;
    }

    /**
     * Return filter values to SQL command
     */
    public String[] getFilterValues() {
        if (mFilters.size() == 0)
            return null;

        ArrayList<String> values = new ArrayList<>();

        for (Filter f : mFilters) {
            String value = f.getSQLValue();
            if (value != null)
                values.add(f.getValueWrapLeft() + value + f.getValueWrapRight());
        }

        // Log event
        if (S.DEBUG)
            Log.d(S.TAG, "Filter values: " + Utils.arrayToString(values.toArray(new String[0]), ", "));

        return values.toArray(new String[0]);
    }

    /**
     * Return human info for all filters as string
     */
    public Spanned getFiltersHumanInfo() {
        if (mFilters.size() == 0)
            return new SpannableString(mContext.getString(R.string.filter_none));

        String[] info = new String[this.getCount()];
        int i = 0;
        for (Filter f : mFilters) {
            info[i] = this.getFilterHumanInfo(f);
            i++;
        }

        return Html.fromHtml(Utils.arrayToString(info, "&nbsp;<i>" + mContext.getString(R.string.filter_join_and)
                + "</i><br/>"));
    }

    /**
     * Return human info for all filters as array
     */
    public ArrayList<String> getFiltersHumanInfoList() {
        ArrayList<String> info = new ArrayList<>();

        for (Filter f : mFilters) {
            info.add(this.getFilterHumanInfo(f));
        }

        return info;
    }

    /**
     * Return human info for single filter
     */
    private String getFilterHumanInfo(Filter f) {
        String info;
        String value = f.getHumanValue();
        String fieldLabel = getFilterFieldHumanName(f.getHumanFieldResourceId());
        FilterType type = f.getFilterType();
        FilterOperator operator = f.getFilterOperator();

        if (value == null) {
            if (operator == Movies.FILTER_OPERATOR_IS_NULL || operator == Movies.FILTER_OPERATOR_IS_NULL_NOT) {
                info = fieldLabel + " " + mContext.getString(f.getHumanOperatorResourceId());
            } else {
                info = fieldLabel + " " + mContext.getString(R.string.filter_value_null_operator) + " " +
                        mContext.getString(R.string.filter_value_null);
            }
        } else if (type == Movies.FILTER_TYPE_BOOLEAN || type == Movies.FILTER_TYPE_NUMBER ||
                type == Movies.FILTER_TYPE_DATE) {
            info = fieldLabel + " " + mContext.getString(f.getHumanOperatorResourceId()) + " " + value;
        } else {
            info = fieldLabel + " " + mContext.getString(f.getHumanOperatorResourceId()) + " " +
                    mContext.getString(R.string.filter_value_enclosing) + value +
                    mContext.getString(R.string.filter_value_enclosing);
        }
        return info;
    }

    /**
     * Get filter count
     */
    public int getCount() {
        return mFilters.size();
    }

    /**
     * Remove filter by index
     */
    public void removeFilter(int index) {
        if (index >= 0 && index < mFilters.size())
            mFilters.remove(index);
    }

    /**
     * Remove all filters
     */
    public void removeAllFilters() {
        mFilters.clear();
    }

    /**
     * Get names for all filter for wizard
     */
    public String[] getAvailableFilters() {
        ArrayList<String> availableFilters = new ArrayList<>();
        for (FilterField f : Movies.availableFilterFields) {
            availableFilters.add(getFilterFieldHumanName(f.resId));
        }
        return availableFilters.toArray(new String[0]);
    }

    /**
     * Get names for all operators for given field in wizard
     */
    public String[] getAvailableOperators(FilterField field) {
        ArrayList<String> availableOperators = new ArrayList<>();
        for (FilterOperator o : field.type.operators) {
            availableOperators.add(mContext.getString(o.resId));
        }
        return availableOperators.toArray(new String[0]);
    }

    /**
     * Get names for all filter for wizard
     */
    public String getFilterFieldHumanName(int resId) {
        return mContext.getString(resId);
    }

}
