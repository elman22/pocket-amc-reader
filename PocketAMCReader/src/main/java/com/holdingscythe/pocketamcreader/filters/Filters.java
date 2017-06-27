package com.holdingscythe.pocketamcreader.filters;

import android.content.Context;
import android.util.Log;

import com.holdingscythe.pocketamcreader.R;
import com.holdingscythe.pocketamcreader.S;
import com.holdingscythe.pocketamcreader.catalog.Movies;
import com.holdingscythe.pocketamcreader.utils.Utils;

import java.util.ArrayList;
import java.util.HashSet;

public class Filters {
    private ArrayList<Filter> mFilters = new ArrayList<Filter>();
    private Context mContext;

    public Filters(Context c) {
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

        ArrayList<String> conditions = new ArrayList<String>();
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

        sqlWhere = Utils.arrayToString(conditions.toArray(new String[conditions.size()]), " and ");

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

        HashSet<String> dbFields = new HashSet<String>();

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

        ArrayList<String> values = new ArrayList<String>();

        for (Filter f : mFilters) {
            String value = f.getSQLValue();
            if (value != null)
                values.add(f.getValueWrapLeft() + value + f.getValueWrapRight());
        }

        // Log event
        if (S.DEBUG)
            Log.d(S.TAG, "Filter values: " + Utils.arrayToString(values.toArray(new String[values.size()]), ", "));

        return values.toArray(new String[values.size()]);
    }

    /**
     * Return human info for all filters as string
     */
    public String getFiltersHumanInfo() {
        if (mFilters.size() == 0)
            return mContext.getString(R.string.filter_none);

        String[] info = new String[this.getCount()];
        int i = 0;
        for (Filter f : mFilters) {
            info[i] = this.getFilterHumanInfo(f);
            i++;
        }

        return Utils.arrayToString(info, " " + mContext.getString(R.string.filter_join_and) + "\n");
    }

    /**
     * Return human info for all filters as array
     */
    public ArrayList<String> getFiltersHumanInfoList() {
        ArrayList<String> info = new ArrayList<String>();

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
        ArrayList<String> availableFilters = new ArrayList<String>();
        for (FilterField f : Movies.availableFilterFields) {
            availableFilters.add(getFilterFieldHumanName(f.resId));
        }
        return availableFilters.toArray(new String[availableFilters.size()]);
    }

    /**
     * Get names for all operators for given field in wizard
     */
    public String[] getAvailableOperators(FilterField field) {
        ArrayList<String> availableOperators = new ArrayList<String>();
        for (FilterOperator o : field.type.operators) {
            availableOperators.add(mContext.getString(o.resId));
        }
        return availableOperators.toArray(new String[availableOperators.size()]);
    }

    /**
     * Get names for all filter for wizard
     */
    public String getFilterFieldHumanName(int resId) {
        return mContext.getString(resId);
    }

}
