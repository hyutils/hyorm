package com.hyutils.core.extension.selectfield;

import java.util.ArrayList;
import java.util.List;

public class SelectFields {
    private List<String> fields;

    public SelectFields() {
        this.fields = new ArrayList<>();
    }

    public List<String> getFields() {
        return fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public SelectFields select(String field){
        this.fields.add(field);
        return this;
    }
}
