package com.hyutils.core.extension.visible;


import java.util.ArrayList;
import java.util.List;

public class VisibleUser {
    private Long id = 0L;
    private String userName;
    private String realName;
    private String orgName;
    private String orgCode;
    private Integer visible = 2;
    private List<Long> orgIds = new ArrayList<>();
    private Long organizationId = 0L;


    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getOrgCode() {
        return orgCode;
    }

    public void setOrgCode(String orgCode) {
        this.orgCode = orgCode;
    }

    public List<Long> getOrgIds() {
        return orgIds;
    }

    public void setOrgIds(List<Long> orgIds) {
        this.orgIds = orgIds;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
    }

    public Integer getVisible() {
        return visible;
    }

    public void setVisible(Integer visible) {
        this.visible = visible;
    }
    public static Builder builder() {
        return new Builder();
    }


    public static class Builder {
        private VisibleUser target;

        public Builder() {
            this.target = new VisibleUser();
        }

        public VisibleUser build() {
            return target;
        }

        public Builder id(Long id){
            this.target.setId(id);
            return this;
        }

        public Builder visible(Integer visible){
            this.target.setVisible(visible);
            return this;
        }

        public Builder organizationId(Long orgId){
            this.target.setOrganizationId(orgId);
            return this;
        }

        public Builder orgIds(List<Long> orgIds){
            this.target.setOrgIds(orgIds);
            return this;
        }

        public Builder realName(String realName) {
            this.target.realName=realName;
            return this;
        }
        public Builder orgName(String orgName) {
            this.target.orgName=orgName;
            return this;
        }
        public Builder orgCode(String orgCode) {
            this.target.orgCode=orgCode;
            return this;
        }
        public Builder userName(String userName) {
            this.target.userName=userName;
            return this;
        }

    }

}
