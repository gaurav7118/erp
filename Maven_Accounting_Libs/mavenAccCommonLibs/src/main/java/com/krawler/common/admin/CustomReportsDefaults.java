package com.krawler.common.admin;

public class CustomReportsDefaults {

    private String id;
    private Country countryid;
    private String moduleid;
    private String name;    
    private Long createdon;
    private Long updatedon;
    private Byte deleteflag;
    private String defaultjson;
    private boolean isShowasQuickLinks; // Show Custom build report in Statutory as Quick links

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModuleid() {
        return moduleid;
    }

    public void setModuleid(String moduleid) {
        this.moduleid = moduleid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getCreatedon() {
        return createdon;
    }

    public void setCreatedon(Long createdon) {
        this.createdon = createdon;
    }

    public Long getUpdatedon() {
        return updatedon;
    }

    public void setUpdatedon(Long updatedon) {
        this.updatedon = updatedon;
    }

    public Byte getDeleteflag() {
        return deleteflag;
    }

    public void setDeleteflag(Byte deleteflag) {
        this.deleteflag = deleteflag;
    }

    public String getDefaultjson() {
        return defaultjson;
    }

    public void setDefaultjson(String defaultjson) {
        this.defaultjson = defaultjson;
    }
    
    
    public Country getCountryid() {
        return countryid;
    }

    public void setCountryid(Country countryid) {
        this.countryid = countryid;
    }

    public boolean isIsShowasQuickLinks() {
        return isShowasQuickLinks;
    }

    public void setIsShowasQuickLinks(boolean isShowasQuickLinks) {
        this.isShowasQuickLinks = isShowasQuickLinks;
    }
}