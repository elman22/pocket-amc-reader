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

package com.holdingscythe.pocketamcreader.model;

/**
 * Data Model for CustomFields
 * Created by Elman on 26. 07. 2017.
 */
public class CustomFieldsModel {
    private String CType;
    private String CName;
    private String CValue;

    // Fields mapping custom fields
    public static final String CFT_BOOLEAN = "ftBoolean";
    public static final String CFT_DATE = "ftDate";
    public static final String CFT_INTEGER = "ftInteger";
    public static final String CFT_LIST = "ftList";
    public static final String CFT_REAL = "ftReal";
    public static final String CFT_REAL1 = "ftReal1";
    public static final String CFT_REAL2 = "ftReal2";
    public static final String CFT_STRING = "ftString";
    public static final String CFT_TEXT = "ftText";
    public static final String CFT_URL = "ftUrl";
    public static final String CFT_VIRTUAL = "ftVirtual";
    public static final String CFT_NULL = "ftNull";

    /**
     * @param CType  Type of custom field
     * @param CName  Name of custom field
     * @param CValue Value of custom field
     */
    public CustomFieldsModel(String CType, String CName, String CValue) {
        this.CType = CType;
        this.CName = CName;
        this.CValue = CValue;
    }

    public String getCType() {
        return this.CType;
    }

    public void setCType(String CType) {
        this.CType = CType;
    }

    public String getCName() {
        return this.CName;
    }

    public void setCName(String CName) {
        this.CName = CName;
    }

    public String getCValue() {
        return this.CValue;
    }

    public void setCValue(String CValue) {
        this.CValue = CValue;
    }
}
