package com.holdingscythe.pocketamcreader.filters;

import com.holdingscythe.pocketamcreader.catalog.Movies;
import com.holdingscythe.pocketamcreader.utils.SharedObjects;

import java.util.Date;

public class Filter {
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
