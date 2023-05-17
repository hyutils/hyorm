package com.hyutils.core.extension.log;

import java.time.LocalDateTime;

public class BaseLog {

    private Long id;

    private String tableName;
    private String operateType;
    private String source;
    private Object businessId;

    // 基础字段
    private Long createdId;
    private String createdName;
    private LocalDateTime createdTime;
    private Long modifiedId;
    private String modifiedName;
    private LocalDateTime modifiedTime;
    private Long deletedId;
    private String deletedName;
    private LocalDateTime deletedTime;
    private Boolean deleteMark;

    public Object getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Object businessId) {
        this.businessId = businessId;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public void setOperateType(String operateType) {
        this.operateType = operateType;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setCreatedId(Long createdId) {
        this.createdId = createdId;
    }

    public void setCreatedName(String createdName) {
        this.createdName = createdName;
    }

    public void setCreatedTime(LocalDateTime createdDt) {
        this.createdTime = createdDt;
    }

    public void setModifiedId(Long modifiedId) {
        this.modifiedId = modifiedId;
    }

    public void setModifiedName(String modifiedName) {
        this.modifiedName = modifiedName;
    }

    public void setModifiedDt(LocalDateTime modifiedDt) {
        this.modifiedTime = modifiedDt;
    }

    public void setDeletedId(Long deletedId) {
        this.deletedId = deletedId;
    }

    public void setDeletedName(String deletedName) {
        this.deletedName = deletedName;
    }

    public void setDeletedDt(LocalDateTime deletedDt) {
        this.modifiedTime = deletedDt;
    }

    public void setDeleteMark(Boolean deleteMark) {
        this.deleteMark = deleteMark;
    }

    public Long getId() {
        return id;
    }

    public String getTableName() {
        return tableName;
    }

    public String getOperateType() {
        return operateType;
    }

    public String getSource() {
        return source;
    }

    public Long getCreatedId() {
        return createdId;
    }

    public String getCreatedName() {
        return createdName;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public Long getModifiedId() {
        return modifiedId;
    }

    public String getModifiedName() {
        return modifiedName;
    }

    public LocalDateTime getModifiedDt() {
        return modifiedTime;
    }

    public Long getDeletedId() {
        return deletedId;
    }

    public String getDeletedName() {
        return deletedName;
    }

    public LocalDateTime getDeletedDt() {
        return deletedTime;
    }

    public Boolean getDeleteMark() {
        return deleteMark;
    }
}
