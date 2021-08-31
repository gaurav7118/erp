/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class WidgetReportMaster {
    private String ID;
    private ReportMaster report;
    private Company company;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public ReportMaster getReport() {
        return report;
    }

    public void setReport(ReportMaster report) {
        this.report = report;
    }

    
}
