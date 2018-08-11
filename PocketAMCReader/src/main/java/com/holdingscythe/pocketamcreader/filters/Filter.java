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

package com.holdingscythe.pocketamcreader.filters;

import com.holdingscythe.pocketamcreader.catalog.Movies;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;

import java.io.Serializable;
import java.util.Date;

public class Filter implements Serializable {
    private FilterField mField;
    private FilterOperator mOperator;
    private String mValue;
    private String mBoolTrueValue;

    public Filter(FilterField field, FilterOperator operator) {
        mField = field;
        mOperator = operator;
        mValue = null;
    }

    public Filter(FilterField field, FilterOperator operator, String value) {
        mField = field;
        mOperator = operator;
        mValue = value;
    }

    public Filter(FilterField field, FilterOperator operator, String value, String boolTrueValue) {
        mField = field;
        mOperator = operator;
        mValue = value;
        mBoolTrueValue = boolTrueValue;
    }

    public String getSQLField() {
        return mField.sql;
    }

    public int getHumanFieldResourceId() {
        return mField.resId;
    }

    public String getSQLOperator() {
        return mOperator.sql;
    }

    public int getHumanOperatorResourceId() {
        return mOperator.resId;
    }

    public String getSQLValue() {
        if (mField.type == Movies.FILTER_TYPE_BOOLEAN) {
            return mValue.equals(String.valueOf(mBoolTrueValue)) ? "True" : "False";
        } else {
            return mValue;
        }
    }

    public String getHumanValue() {
        if (mField.type == Movies.FILTER_TYPE_DATE) {
            try {
                Date date = SharedObjects.getInstance().dateAddedFormat.parse(mValue);
                return SharedObjects.getInstance().dateFormat.format(date);
            } catch (Exception e) {
                // don't do anything, use default value
            }
        }
        return mValue;
    }

    public String getValueWrapLeft() {
        return mOperator.valueWrapLeft;
    }

    public String getValueWrapRight() {
        return mOperator.valueWrapRight;
    }

    public FilterType getFilterType() {
        return mField.type;
    }

    public FilterOperator getFilterOperator() {
        return mOperator;
    }

}
