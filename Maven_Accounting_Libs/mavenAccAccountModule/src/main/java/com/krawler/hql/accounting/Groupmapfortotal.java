/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.hql.accounting;

/**
 *
 * @author krawler
 */
public class Groupmapfortotal {

    private String ID;
    private String action;
    LayoutGroup groupidtotal;
    LayoutGroup groupid;

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

    public LayoutGroup getGroupid() {
        return groupid;
    }

    public void setGroupid(LayoutGroup groupid) {
        this.groupid = groupid;
    }

    public LayoutGroup getGroupidtotal() {
        return groupidtotal;
    }

    public void setGroupidtotal(LayoutGroup groupidtotal) {
        this.groupidtotal = groupidtotal;
    }
}
