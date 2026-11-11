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

import java.io.Serial;
import java.io.Serializable;

public class FilterOperator implements Serializable {
    public final String sql;
    public final int resId;
    public final String valueWrapLeft;
    public final String valueWrapRight;

    public FilterOperator(String sql, int resId, String valueWrapLeft, String valueWrapRight) {
        this.sql = sql;
        this.resId = resId;
        this.valueWrapLeft = valueWrapLeft;
        this.valueWrapRight = valueWrapRight;
    }

    /**
     * Filters are compared with the operators defined in {@link Movies} by identity. A deserialized copy (filters
     * survive a screen rotation in saved instance state) has to be replaced by the shared instance.
     */
    @Serial
    private Object readResolve() {
        for (FilterOperator operator : Movies.availableFilterOperators) {
            if (operator.resId == resId && operator.sql.equals(sql)) {
                return operator;
            }
        }
        return this;
    }
}
