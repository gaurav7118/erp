/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

import java.util.Date;

/**
 *
 * @author krawler
 */
public class CroneSchedule {

    private String id;
    private String croneName;
    private Date lastHit;

    public String getCroneName() {
        return croneName;
    }

    public void setCroneName(String croneName) {
        this.croneName = croneName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getLastHit() {
        return lastHit;
    }

    public void setLastHit(Date lastHit) {
        this.lastHit = lastHit;
    }
}
