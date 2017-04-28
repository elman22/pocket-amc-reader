package com.holdingscythe.pocketamcreader.settings;

public class SettingsListField {
    private Long id;
    private String databaseField;
    private Integer resource;
    private String displayText;

    public SettingsListField(int id, String f, Integer r) {
        this.id = (long) id;
        this.databaseField = f;
        this.resource = r;
    }

    public Long getId() {
        return this.id;
    }

    public String getDatabaseField() {
        return this.databaseField;
    }

    public Integer getResource() {
        return this.resource;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
    }
}
