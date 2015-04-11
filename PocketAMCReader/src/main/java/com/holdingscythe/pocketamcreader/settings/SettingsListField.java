package com.holdingscythe.pocketamcreader.settings;

// TODO: cleanup

public class SettingsListField {
    private String databaseField;
    private Integer resource;
    private String displayText;

    public SettingsListField(String f, Integer r) {
        this.databaseField = f;
        this.resource = r;
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
