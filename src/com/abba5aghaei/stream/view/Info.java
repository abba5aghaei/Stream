package com.abba5aghaei.stream.view;

public class Info {

    private String property;
    private String value;

    public Info(String property, String value) {
        this.property = property;
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }
}
