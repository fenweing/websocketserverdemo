package com.parrer.websocketserverdemo.cfind.controller;

public enum ReferenceTypeEnum {
    HTML("html", "html类型"),
    FILE("file", "file类型"),
    MD("md", "md类型"),
    UNKNOWN("unknown", "未知类型");
    private String key;
    private String value;

    ReferenceTypeEnum(String key, String value) {
        this.key = key;
        this.value = value;
    }

    public Boolean isKey(String key) {
        return this.key.equals(key);
    }

    public Boolean isValue(String value) {
        return this.value.equals(value);
    }

    public static ReferenceTypeEnum getByKey(String key) {
        for (ReferenceTypeEnum value : values()) {
            if (value.getKey().equals(key)) {
                return value;
            }
        }
        return UNKNOWN;
    }

    public static ReferenceTypeEnum getByValue(String value) {
        for (ReferenceTypeEnum item : values()) {
            if (item.value.equals(value)) {
                return item;
            }
        }
        return UNKNOWN;
    }

    public static String getValueByKey(String key) {
        return getByKey(key).value;
    }

    public static String getKeyByValue(String value) {
        return getByValue(value).key;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
