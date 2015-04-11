package com.holdingscythe.pocketamcreader.filters;

public class FilterField {
    public FilterType type;
    public String sql;
    public int resId;

    public FilterField(FilterType type, String sql, int resId) {
        this.type = type;
        this.sql = sql;
        this.resId = resId;
    }
}
