/*
    This file is part of Pocket AMC Reader.
    Copyright © 2010-2026 Elman <holdingscythe@zoznam.sk>

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

import com.holdingscythe.pocketamcreader.S;
import com.holdingscythe.pocketamcreader.catalog.Movies;

import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * SQL built from filters (selection and its arguments must stay in sync)
 */
public class FiltersTest {

    private static Filters filters(Filter... filters) {
        Filters result = new Filters();
        for (Filter filter : filters) {
            result.addFilter(filter);
        }
        return result;
    }

    @Test
    public void noFiltersMeansNoSelection() {
        Filters empty = new Filters();

        assertNull(empty.getFilterFields());
        assertNull(empty.getFilterValues());
        assertNull(empty.getFilterFieldsDB());
        assertEquals(0, empty.getCount());
    }

    @Test
    public void containsWrapsTheValueInPercentSigns() {
        Filters f = filters(new Filter(Movies.FILTER_FORMATTED_TITLE, Movies.FILTER_OPERATOR_CONTAINS, "ring"));

        assertEquals(Movies.FORMATTED_TITLE + " like ?", f.getFilterFields());
        assertArrayEquals(new String[]{"%ring%"}, f.getFilterValues());
    }

    @Test
    public void beginsWithAndEndsWithWrapOneSideOnly() {
        assertArrayEquals(new String[]{"ring%"}, filters(
                new Filter(Movies.FILTER_FORMATTED_TITLE, Movies.FILTER_OPERATOR_BEGINS_WITH, "ring"))
                .getFilterValues());
        assertArrayEquals(new String[]{"%ring"}, filters(
                new Filter(Movies.FILTER_FORMATTED_TITLE, Movies.FILTER_OPERATOR_ENDS_WITH, "ring"))
                .getFilterValues());
    }

    @Test
    public void containsNotUsesNotLike() {
        Filters f = filters(new Filter(Movies.FILTER_DIRECTOR, Movies.FILTER_OPERATOR_CONTAINS_NOT, "x"));

        assertEquals(Movies.DIRECTOR + " not like ?", f.getFilterFields());
    }

    @Test
    public void numbersAreComparedWithoutWrapping() {
        Filters f = filters(new Filter(Movies.FILTER_RATING, Movies.FILTER_OPERATOR_LARGER, "7"));

        assertEquals(Movies.RATING + " > ?", f.getFilterFields());
        assertArrayEquals(new String[]{"7"}, f.getFilterValues());
    }

    @Test
    public void conditionsAreJoinedWithAndAndValuesKeepTheOrder() {
        Filters f = filters(
                new Filter(Movies.FILTER_NUMBER, Movies.FILTER_OPERATOR_EQUALS, "5"),
                new Filter(Movies.FILTER_FORMATTED_TITLE, Movies.FILTER_OPERATOR_CONTAINS, "a"));

        assertEquals(Movies.NUMBER + " = ? and " + Movies.FORMATTED_TITLE + " like ?", f.getFilterFields());
        assertArrayEquals(new String[]{"5", "%a%"}, f.getFilterValues());
        assertEquals(2, f.getCount());
    }

    @Test
    public void isNullFiltersHaveNoArguments() {
        Filters f = filters(
                new Filter(Movies.FILTER_DIRECTOR, Movies.FILTER_OPERATOR_IS_NULL),
                new Filter(Movies.FILTER_PRODUCER, Movies.FILTER_OPERATOR_IS_NULL_NOT));

        assertEquals(Movies.DIRECTOR + " is null and " + Movies.PRODUCER + " is not null", f.getFilterFields());
        assertEquals(0, f.getFilterValues().length);
    }

    @Test
    public void filterWithoutValueIsTreatedAsIsNull() {
        Filters f = filters(new Filter(Movies.FILTER_RATING, Movies.FILTER_OPERATOR_LARGER, null));

        assertEquals(Movies.RATING + " is null", f.getFilterFields());
        assertEquals(0, f.getFilterValues().length);
    }

    @Test
    public void booleanFilterUsesCatalogValues() {
        Filters checked = filters(new Filter(Movies.FILTER_CHECKED, Movies.FILTER_OPERATOR_EQUALS, "yes", "yes"));
        Filters notChecked = filters(new Filter(Movies.FILTER_CHECKED, Movies.FILTER_OPERATOR_EQUALS, "no", "yes"));

        assertArrayEquals(new String[]{S.CATALOG_TRUE}, checked.getFilterValues());
        assertArrayEquals(new String[]{S.CATALOG_FALSE}, notChecked.getFilterValues());
    }

    @Test
    public void databaseFieldsAreReported() {
        Filters f = filters(
                new Filter(Movies.FILTER_NUMBER, Movies.FILTER_OPERATOR_EQUALS, "5"),
                new Filter(Movies.FILTER_NUMBER, Movies.FILTER_OPERATOR_SMALLER, "9"),
                new Filter(Movies.FILTER_SOURCE, Movies.FILTER_OPERATOR_IS, "DVD"));

        Set<String> fields = f.getFilterFieldsDB();

        assertEquals(2, fields.size());
        assertTrue(fields.contains(Movies.NUMBER));
        assertTrue(fields.contains(Movies.SOURCE));
    }
}
