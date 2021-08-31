/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.handler;


import com.krawler.common.admin.User;
import java.util.Date;

public class widgetManagement implements java.io.Serializable {
    private String id;
    private String widgetstate;
//    private Date modifiedon;
    private Long modifiedOn;
    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(Long modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    public Date getModifiedon() {
        if(modifiedOn!=null)
            return new Date(modifiedOn);
        return null;
    }

    public void setModifiedon(Date modifiedon) {
        this.modifiedOn = modifiedon.getTime();
    }

    public String getWidgetstate() {
        return widgetstate;
    }

    public void setWidgetstate(String widgetstate) {
        this.widgetstate = widgetstate;
    }
}
