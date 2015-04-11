package com.holdingscythe.pocketamcreader.filters;

public class FilterOperator {
    public String sql;
    public int resId;
    public String valueWrapLeft;
    public String valueWrapRight;

    public FilterOperator(String sql, int resId, String valueWrapLeft, String valueWrapRight) {
        this.sql = sql;
        this.resId = resId;
        this.valueWrapLeft = valueWrapLeft;
        this.valueWrapRight = valueWrapRight;
    }
}
