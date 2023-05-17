package com.hyutils.core.syntaxtree;

public class WhereNodeSentence {
    private String name;
    private String operate = "=";
    private Object value;

    public WhereNodeSentence(String name, String operate, Object value) {
        this.name = name;
        this.operate = operate;
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public String getOperate() {
        return operate;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setOperate(String operate) {
        this.operate = operate;
    }
}
