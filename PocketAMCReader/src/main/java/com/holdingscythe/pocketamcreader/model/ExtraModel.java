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
 * Data Model for Extra
 * Created by Elman on 30. 12. 2015.
 */
public class ExtraModel {
    private String EPicture;
    private boolean EChecked;
    private String ETag;
    private String ETitle;
    private String ECategory;
    private String EURL;
    private String EDescription;
    private String EComments;
    private String ECreatedBy;

    /**
     * @param EPicture
     * @param EChecked
     * @param ETag
     * @param ETitle
     * @param ECategory
     * @param EURL
     * @param EDescription
     * @param EComments
     * @param ECreatedBy
     */
    public ExtraModel(String EPicture, boolean EChecked, String ETag, String ETitle, String ECategory, String EURL,
                      String EDescription, String EComments, String ECreatedBy) {
        this.EPicture = EPicture;
        this.EChecked = EChecked;
        this.ETag = ETag;
        this.ETitle = ETitle;
        this.ECategory = ECategory;
        this.EURL = EURL;
        this.EDescription = EDescription;
        this.EComments = EComments;
        this.ECreatedBy = ECreatedBy;
    }

    public ExtraModel(String EPicture) {
        this.EPicture = EPicture;
    }

    public boolean isEChecked() {
        return this.EChecked;
    }

    public void setEChecked(boolean EChecked) {
        this.EChecked = EChecked;
    }

    public String getETag() {
        return this.ETag;
    }

    public void setETag(String ETag) {
        this.ETag = ETag;
    }

    public String getETitle() {
        return this.ETitle;
    }

    public void setETitle(String ETitle) {
        this.ETitle = ETitle;
    }

    public String getECategory() {
        return this.ECategory;
    }

    public void setECategory(String ECategory) {
        this.ECategory = ECategory;
    }

    public String getEURL() {
        return this.EURL;
    }

    public void setEURL(String EURL) {
        this.EURL = EURL;
    }

    public String getEDescription() {
        return this.EDescription;
    }

    public void setEDescription(String EDescription) {
        this.EDescription = EDescription;
    }

    public String getEComments() {
        return this.EComments;
    }

    public void setEComments(String EComments) {
        this.EComments = EComments;
    }

    public String getECreatedBy() {
        return this.ECreatedBy;
    }

    public void setECreatedBy(String ECreatedBy) {
        this.ECreatedBy = ECreatedBy;
    }

    public String getEPicture() {
        return this.EPicture;
    }

    public void setEPicture(String EPicture) {
        this.EPicture = EPicture;
    }
}
