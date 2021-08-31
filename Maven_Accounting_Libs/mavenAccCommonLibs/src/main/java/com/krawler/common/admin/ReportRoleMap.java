/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class ReportRoleMap {

    private String ID;
    private ReportMaster reportid;
    private Rolelist roleid;

    public Rolelist getRoleid() {     //Mapping with Rolelist class instead of roll
        return roleid;
    }

    public void setRoleid(Rolelist roleid) {
        this.roleid = roleid;
    }
    private User userid;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public ReportMaster getReportid() {
        return reportid;
    }

    public void setReportid(ReportMaster reportid) {
        this.reportid = reportid;
    }

    public User getUserid() {
        return userid;
    }

    public void setUserid(User userid) {
        this.userid = userid;
    }
}
