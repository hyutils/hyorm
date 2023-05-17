package com.hyutils.core.extension.selectorder;

public class SelectOrder {
    private String key;
    private String order;

    public SelectOrder() {
    }

    public SelectOrder(String key, String order) {
        this.key = key;
        this.order = order;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }
}
