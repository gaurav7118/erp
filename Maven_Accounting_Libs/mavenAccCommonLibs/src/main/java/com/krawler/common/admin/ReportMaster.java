/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class ReportMaster {

    /**
     *
     * @author krawler
     */
    private String ID;
    private String name;
    private String description;
    private String methodName;
    private String GroupedUnder;
    private String moduleid;
    private Country countryid;// Used for INDIA country
    private String widgetURL;
    private boolean isWidgetReady; //Set to true if widget view is ready.
    private String helpText;
    private boolean showInReportBuilder;
    private User usersByUpdatedbyid;
    private User usersByCreatedbyid;
    private String reportuniquename;
    private Character summaryflag;
    private Long createdon;
    private Byte deleteflag;
    private ModuleCategory reportmodulecategory;
    private String reportjson;
    private String filterjson;
    private Character groupflag;
    private boolean ispivot;
    private boolean isdefault;
    private boolean eWayReport;
    private String reportsql;
    private Long updatedon;
    private String companyId;
    private String parentreportid;
    private boolean isCustomWidgetReady;//Is implementation done for Custom widget report or not.
    private boolean isShowasQuickLinks; // Show Custom build report in Statutory as Quick links
    
    public  ReportMaster() {
       
    }
    
    public ReportMaster(ReportMaster reportDetails) {
        this.methodName = reportDetails.methodName;
        this.GroupedUnder = reportDetails.GroupedUnder;
        this.moduleid = reportDetails.moduleid;
        this.countryid = reportDetails.countryid;// Used for INDIA country
        this.widgetURL = reportDetails.widgetURL;
        this.isWidgetReady = reportDetails.isWidgetReady; //Set to true if widget view is ready.
        this.helpText = reportDetails.helpText;
        this.showInReportBuilder = reportDetails.showInReportBuilder;
        this.reportuniquename = reportDetails.reportuniquename;
        this.summaryflag = reportDetails.summaryflag;
        this.deleteflag = reportDetails.deleteflag;
        this.reportmodulecategory = reportDetails.reportmodulecategory;
        this.reportjson = reportDetails.reportjson;
        this.filterjson = reportDetails.filterjson;
        this.groupflag = reportDetails.groupflag;
        this.ispivot = reportDetails.ispivot;
        this.isdefault = reportDetails.isdefault;
        this.eWayReport = reportDetails.eWayReport;
        this.reportsql = reportDetails.reportsql;
        this.companyId = reportDetails.companyId;
        this.parentreportid = reportDetails.parentreportid;
        this.isCustomWidgetReady = reportDetails.isCustomWidgetReady;
        this.isShowasQuickLinks = reportDetails.isShowasQuickLinks; 

    }
    

    public String getParentreportid() {
        return parentreportid;
    }

    public void setParentreportid(String parentreportid) {
        this.parentreportid = parentreportid;
    }

    public boolean isShowInReportBuilder() {
        return showInReportBuilder;
    }

    public void setShowInReportBuilder(boolean showInReportBuilder) {
        this.showInReportBuilder = showInReportBuilder;
    }

    public ModuleCategory getReportmodulecategory() {
        return reportmodulecategory;
    }

    public void setReportmodulecategory(ModuleCategory reportmodulecategory) {
        this.reportmodulecategory = reportmodulecategory;
    }

    public User getUsersByUpdatedbyid() {
        return usersByUpdatedbyid;
    }

    public void setUsersByUpdatedbyid(User usersByUpdatedbyid) {
        this.usersByUpdatedbyid = usersByUpdatedbyid;
    }

    public User getUsersByCreatedbyid() {
        return usersByCreatedbyid;
    }

    public void setUsersByCreatedbyid(User usersByCreatedbyid) {
        this.usersByCreatedbyid = usersByCreatedbyid;
    }

    public String getReportuniquename() {
        return reportuniquename;
    }

    public void setReportuniquename(String reportuniquename) {
        this.reportuniquename = reportuniquename;
    }

    public Character getSummaryflag() {
        return summaryflag;
    }

    public void setSummaryflag(Character summaryflag) {
        this.summaryflag = summaryflag;
    }

    public Long getCreatedon() {
        return createdon;
    }

    public void setCreatedon(Long createdon) {
        this.createdon = createdon;
    }

    public Byte getDeleteflag() {
        return deleteflag;
    }

    public void setDeleteflag(Byte deleteflag) {
        this.deleteflag = deleteflag;
    }

    public String getReportjson() {
        return reportjson;
    }

    public void setReportjson(String reportjson) {
        this.reportjson = reportjson;
    }

    public String getFilterjson() {
        return filterjson;
    }

    public void setFilterjson(String filterjson) {
        this.filterjson = filterjson;
    }

    public Character getGroupflag() {
        return groupflag;
    }

    public void setGroupflag(Character groupflag) {
        this.groupflag = groupflag;
    }

    public boolean isIspivot() {
        return ispivot;
    }

    public void setIspivot(boolean ispivot) {
        this.ispivot = ispivot;
    }

    public boolean isIsdefault() {
        return isdefault;
    }

    public void setIsdefault(boolean isdefault) {
        this.isdefault = isdefault;
    }
    
    public boolean iseWayReport() {
        return eWayReport;
    }

    public void seteWayReport(boolean eWayReport) {
        this.eWayReport = eWayReport;
    }
    
    public String getReportsql() {
        return reportsql;
    }

    public void setReportsql(String reportsql) {
        this.reportsql = reportsql;
    }

    public Long getUpdatedon() {
        return updatedon;
    }

    public void setUpdatedon(Long updatedon) {
        this.updatedon = updatedon;
    }

    public String getCompanyId() {
        return companyId;
    }

    public void setCompanyId(String companyId) {
        this.companyId = companyId;
    }
    public void setGroupedUnder(String GroupedUnder) {
        this.GroupedUnder = GroupedUnder;
    }

    public String getGroupedUnder() {
        return GroupedUnder;
    }
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    public String getModuleid() {
        return moduleid;
    }

    public void setModuleid(String moduleid) {
        this.moduleid = moduleid;
    }

    public Country getCountryid() {
        return countryid;
    }

    public void setCountryid(Country countryid) {
        this.countryid = countryid;
    }

    public boolean isIsWidgetReady() {
        return isWidgetReady;
    }

    public void setIsWidgetReady(boolean isWidgetReady) {
        this.isWidgetReady = isWidgetReady;
    }

    public String getWidgetURL() {
        return widgetURL;
    }

    public void setWidgetURL(String widgetURL) {
        this.widgetURL = widgetURL;
    }

    public String getHelpText() {
        return helpText;
}

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    public boolean isIsCustomWidgetReady() {
        return isCustomWidgetReady;
    }

    public void setIsCustomWidgetReady(boolean isCustomWidgetReady) {
        this.isCustomWidgetReady = isCustomWidgetReady;
    }

    public boolean isIsShowasQuickLinks() {
        return isShowasQuickLinks;
    }

    public void setIsShowasQuickLinks(boolean isShowasQuickLinks) {
        this.isShowasQuickLinks = isShowasQuickLinks;
    }
}
