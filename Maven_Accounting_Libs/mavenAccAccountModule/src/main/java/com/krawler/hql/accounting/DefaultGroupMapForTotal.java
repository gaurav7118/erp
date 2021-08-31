/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

/**
 *
 * @author krawler
 */
public class DefaultGroupMapForTotal {
    
    private String ID;
    private String action;
    DefaultLayoutGroup groupidtotal;
    DefaultLayoutGroup groupid;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public DefaultLayoutGroup getGroupidtotal() {
        return groupidtotal;
    }

    public void setGroupidtotal(DefaultLayoutGroup groupidtotal) {
        this.groupidtotal = groupidtotal;
    }

    public DefaultLayoutGroup getGroupid() {
        return groupid;
    }

    public void setGroupid(DefaultLayoutGroup groupid) {
        this.groupid = groupid;
    }
    
}
