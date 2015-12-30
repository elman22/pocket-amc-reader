package com.holdingscythe.pocketamcreader.model;

public class Extra {
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
    public Extra(String EPicture, boolean EChecked, String ETag, String ETitle, String ECategory, String EURL,
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

    public Extra(String EPicture) {
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
