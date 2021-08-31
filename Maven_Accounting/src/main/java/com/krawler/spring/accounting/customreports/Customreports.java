/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.customreports;

import com.krawler.common.admin.Modules;
import com.krawler.common.admin.User;
import com.krawler.common.admin.ModuleCategory;

/**
 *
 * @author krawler
 */
public class Customreports implements java.io.Serializable {

    private String reportno;
    private User usersByUpdatedbyid;
    private User usersByCreatedbyid;
    private String reportname;
    private String reportuniquename;
    private Character summaryflag;
    private Long createdon;
    private Byte deleteflag;
    private ModuleCategory reportmodulecategory;
    private Modules reportmodule;
    private String reportjson;
    private String  filterjson;
    private Character groupflag;
    private boolean ispivot;
    private boolean isdefault;
    private String reportsql;
    private String reportdescription;
    private Long updatedon;
    private String companyId;
    private String widgetURL;
    private String parentreportid;

    public String getParentreportid() {
        return parentreportid;
    }

    public void setParentreportid(String parentreportid) {
        this.parentreportid = parentreportid;
    }

    public String getWidgetURL() {
        return widgetURL;
    }

    public void setWidgetURL(String widgetURL) {
        this.widgetURL = widgetURL;
    }

    public Customreports() {
    }

    public Customreports(String reportno) {
        this.reportno = reportno;
    }

    public Customreports(String reportno, User usersByUpdatedbyid, User usersByCreatedbyid, String reportname, String reportuniquename, Character summaryflag, Long createdon, Byte deleteflag, ModuleCategory reportmodulecategory, Modules reportmodule, String reportjson, Character groupflag, String reportsql, String reportdescription, Long updatedon, String companyid) {
        this.reportno = reportno;
        this.usersByUpdatedbyid = usersByUpdatedbyid;
        this.usersByCreatedbyid = usersByCreatedbyid;
        this.reportname = reportname;
        this.reportuniquename = reportuniquename;
        this.summaryflag = summaryflag;
        this.createdon = createdon;
        this.deleteflag = deleteflag;
        this.reportmodulecategory = reportmodulecategory;
        this.reportmodule = reportmodule;
        this.reportjson = reportjson;
        this.groupflag = groupflag;
        this.reportsql = reportsql;
        this.reportdescription = reportdescription;
        this.updatedon = updatedon;
        this.companyId = companyid;
    }

    public String getReportno() {
        return this.reportno;
    }

    public void setReportno(String reportno) {
        this.reportno = reportno;
    }

    public User getUsersByUpdatedbyid() {
        return this.usersByUpdatedbyid;
    }

    public void setUsersByUpdatedbyid(User usersByUpdatedbyid) {
        this.usersByUpdatedbyid = usersByUpdatedbyid;
    }

    public User getUsersByCreatedbyid() {
        return this.usersByCreatedbyid;
    }

    public void setUsersByCreatedbyid(User usersByCreatedbyid) {
        this.usersByCreatedbyid = usersByCreatedbyid;
    }

    public String getReportname() {
        return this.reportname;
    }

    public void setReportname(String reportname) {
        this.reportname = reportname;
    }

    public String getReportuniquename() {
        return this.reportuniquename;
    }

    public void setReportuniquename(String reportuniquename) {
        this.reportuniquename = reportuniquename;
    }

    public Character getSummaryflag() {
        return this.summaryflag;
    }

    public void setSummaryflag(Character summaryflag) {
        this.summaryflag = summaryflag;
    }

    public Long getCreatedon() {
        return this.createdon;
    }

    public void setCreatedon(Long createdon) {
        this.createdon = createdon;
    }

    public Byte getDeleteflag() {
        return this.deleteflag;
    }

    public void setDeleteflag(Byte deleteflag) {
        this.deleteflag = deleteflag;
    }

    public ModuleCategory getReportmodulecategory() {
        return this.reportmodulecategory;
    }

    public void setReportmodulecategory(ModuleCategory reportmodulecategory) {
        this.reportmodulecategory = reportmodulecategory;
    }

    public Modules getReportmodule() {
        return this.reportmodule;
    }

    public void setReportmodule(Modules reportmodule) {
        this.reportmodule = reportmodule;
    }

    public String getReportjson() {
        return this.reportjson;
    }

    public void setReportjson(String reportjson) {
        this.reportjson = reportjson;
    }

    public Character getGroupflag() {
        return this.groupflag;
    }

    public void setGroupflag(Character groupflag) {
        this.groupflag = groupflag;
    }

    public String getReportsql() {
        return this.reportsql;
    }

    public void setReportsql(String reportsql) {
        this.reportsql = reportsql;
    }

    public String getReportdescription() {
        return this.reportdescription;
    }

    public void setReportdescription(String reportdescription) {
        this.reportdescription = reportdescription;
    }

    public Long getUpdatedon() {
        return this.updatedon;
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

    public String getFilterjson() {
        return filterjson;
    }

    public void setFilterjson(String filterjson) {
        this.filterjson = filterjson;
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
    
}
