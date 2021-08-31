/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class NotifictionRulesRecurringDetail {

    private String ID;
    private int repeatTime; //a repeated period after which mail has to be sent repeateadly (Repeat Every-...)
    private int repeatTimeType; // 1 for days, 2 for week, 3 for month
    private int endType; // 1 never end, 2 end after a fixed recurred mail count
    private int endInterval; // number of recurring mail
    private Company company;

    
    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public int getEndInterval() {
        return endInterval;
    }

    public void setEndInterval(int endInterval) {
        this.endInterval = endInterval;
    }

    public int getEndType() {
        return endType;
    }

    public void setEndType(int endType) {
        this.endType = endType;
    }

    public int getRepeatTime() {
        return repeatTime;
    }

    public void setRepeatTime(int repeatTime) {
        this.repeatTime = repeatTime;
    }

    public int getRepeatTimeType() {
        return repeatTimeType;
    }

    public void setRepeatTimeType(int repeatTimeType) {
        this.repeatTimeType = repeatTimeType;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }
    
}
