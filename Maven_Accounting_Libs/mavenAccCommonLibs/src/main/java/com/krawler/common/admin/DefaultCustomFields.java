package com.krawler.common.admin;

public class DefaultCustomFields {

    private String id;
    private Country countryid;
    private State stateid;
    private int maxlength;
    private int fieldtype;
    private String fieldlabel;
    private String moduleid;
    private int customfield;
    private int customcolumn;
//    private boolean isformultientity;
    private int insertsequence;
    private int isessential;
    private boolean isAutoPopulateDefaultValue;//SDP-5276 - To auto-populate default value in document entry form.
    private int GSTMappingColnum; 
//    private boolean isForGSTRuleMapping;
    private int GSTConfigType; 
    private int relatedModuleIsAllowEdit;
    private String relatedmoduleid;
    private String fieldtooltip;
    private String defaultValue;

    public int getRelatedModuleIsAllowEdit() {
        return relatedModuleIsAllowEdit;
    }    
    public void setRelatedModuleIsAllowEdit(int relatedModuleIsAllowEdit) {
        this.relatedModuleIsAllowEdit = relatedModuleIsAllowEdit;
    }
    public String getRelatedmoduleid() {
        return relatedmoduleid;
    }
    public void setRelatedmoduleid(String relatedmoduleid) {
        this.relatedmoduleid = relatedmoduleid;
    }   
    public String getFieldtooltip() {
        return fieldtooltip;
    }

    public void setFieldtooltip(String fieldtooltip) {
        this.fieldtooltip = fieldtooltip;
    }
    public int getGSTConfigType() {
        return GSTConfigType;
    }

    public void setGSTConfigType(int GSTConfigType) {
        this.GSTConfigType = GSTConfigType;
    }

    public Country getCountryid() {
        return countryid;
    }

    public void setCountryid(Country countryid) {
        this.countryid = countryid;
    }

    public int getCustomcolumn() {
        return customcolumn;
    }

    public void setCustomcolumn(int customcolumn) {
        this.customcolumn = customcolumn;
    }

    public int getCustomfield() {
        return customfield;
    }

    public void setCustomfield(int customfield) {
        this.customfield = customfield;
    }

    public String getFieldlabel() {
        return fieldlabel;
    }

    public void setFieldlabel(String fieldlabel) {
        this.fieldlabel = fieldlabel;
    }

    public int getFieldtype() {
        return fieldtype;
    }

    public void setFieldtype(int fieldtype) {
        this.fieldtype = fieldtype;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getMaxlength() {
        return maxlength;
    }

    public void setMaxlength(int maxlength) {
        this.maxlength = maxlength;
    }

    public String getModuleid() {
        return moduleid;
    }

    public void setModuleid(String moduleid) {
        this.moduleid = moduleid;
    }

    public State getStateid() {
        return stateid;
    }

    public void setStateid(State stateid) {
        this.stateid = stateid;
    }

//    public boolean isIsformultientity() {
//        return isformultientity;
//    }
//
//    public void setIsformultientity(boolean isformultientity) {
//        this.isformultientity = isformultientity;
//    }

    public int getInsertsequence() {
        return insertsequence;
    }

    public void setInsertsequence(int insertsequence) {
        this.insertsequence = insertsequence;
    }

    public int getIsessential() {
        return isessential;
    }

    public void setIsessential(int isessential) {
        this.isessential = isessential;
    }

    public boolean isIsAutoPopulateDefaultValue() {
        return isAutoPopulateDefaultValue;
    }

    public void setIsAutoPopulateDefaultValue(boolean isAutoPopulateDefaultValue) {
        this.isAutoPopulateDefaultValue = isAutoPopulateDefaultValue;
    }
    public int getGSTMappingColnum() {
        return GSTMappingColnum;
    }

    public void setGSTMappingColnum(int GSTMappingColnum) {
        this.GSTMappingColnum = GSTMappingColnum;
    }
//    public boolean isIsForGSTRuleMapping() {
//        return isForGSTRuleMapping;
//    }
//
//    public void setIsForGSTRuleMapping(boolean isForGSTRuleMapping) {
//        this.isForGSTRuleMapping = isForGSTRuleMapping;
//    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
}
