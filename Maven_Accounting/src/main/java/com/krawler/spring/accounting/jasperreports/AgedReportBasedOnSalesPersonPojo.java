/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.jasperreports;

/**
 *
 * @author krawler
 */
public class AgedReportBasedOnSalesPersonPojo {
    private String salesPersonName;
    private String currency;
    private String current;
    private String days_1to30;
    private String days_31to60;
    private String days_61to90;
    private String days_91to120;
    private String days_121to150;
    private String days_151to180;
    private String days_Over180;
    private String totalInBaseCurrency;
    private String baseCurrency;

    public AgedReportBasedOnSalesPersonPojo() {
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }

    public String getDays_121to150() {
        return days_121to150;
    }

    public void setDays_121to150(String days_121to150) {
        this.days_121to150 = days_121to150;
    }

    public String getDays_151to180() {
        return days_151to180;
    }

    public void setDays_151to180(String days_151to180) {
        this.days_151to180 = days_151to180;
    }

    public String getDays_1to30() {
        return days_1to30;
    }

    public void setDays_1to30(String days_1to30) {
        this.days_1to30 = days_1to30;
    }

    public String getDays_31to60() {
        return days_31to60;
    }

    public void setDays_31to60(String days_31to60) {
        this.days_31to60 = days_31to60;
    }

    public String getDays_61to90() {
        return days_61to90;
    }

    public void setDays_61to90(String days_61to90) {
        this.days_61to90 = days_61to90;
    }

    public String getDays_91to120() {
        return days_91to120;
    }

    public void setDays_91to120(String days_91to120) {
        this.days_91to120 = days_91to120;
    }

    public String getDays_Over180() {
        return days_Over180;
    }

    public void setDays_Over180(String days_Over180) {
        this.days_Over180 = days_Over180;
    }

    public String getSalesPersonName() {
        return salesPersonName;
    }

    public void setSalesPersonName(String salesPersonName) {
        this.salesPersonName = salesPersonName;
    }

    public String getTotalInBaseCurrency() {
        return totalInBaseCurrency;
    }

    public void setTotalInBaseCurrency(String totalInBaseCurrency) {
        this.totalInBaseCurrency = totalInBaseCurrency;
    }

    public String getBaseCurrency() {
        return baseCurrency;
    }

    public void setBaseCurrency(String baseCurrency) {
        this.baseCurrency = baseCurrency;
    }
}
