package com.hyutils.core.syntaxtree;

public class WhereSyntaxNode {
    private String name;
    private String operate = "=";
    private Object value;
    private String setName;
    private Boolean valueContainBracket;
    private Boolean listValueIsObject; // 列表数据是否是复杂对象，复杂对象需要做特殊处理

    public WhereSyntaxNode(String name, String operate, Object value) {
        this.name = name;
        this.operate = operate;
        this.value = value;
        this.setName = name;
        this.valueContainBracket = false;
        this.listValueIsObject = false;
    }

    public WhereSyntaxNode(String name, String operate, Boolean valueContainBracket, Object value) {
        this.valueContainBracket = valueContainBracket;
        this.name = name;
        this.operate = operate;
        this.setName = name;
        this.value = value;
    }

    public WhereSyntaxNode(String name, String operate, Boolean valueContainBracket, Boolean listValueIsObject, Object value) {
        this.value = value;
        this.name = name;
        this.setName = name;
        this.operate = operate;
        this.valueContainBracket = valueContainBracket;
        this.listValueIsObject = listValueIsObject;
    }

    public Boolean getValueContainBracket() {
        return valueContainBracket;
    }

    public void setValueContainBracket(Boolean valueContainBracket) {
        this.valueContainBracket = valueContainBracket;
    }

    public Boolean getListValueIsObject() {
        return listValueIsObject;
    }

    public void setListValueIsObject(Boolean listValueIsObject) {
        this.listValueIsObject = listValueIsObject;
    }

    public String getSetName() {
        return setName;
    }

    public void setSetName(String setName) {
        this.setName = setName;
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
