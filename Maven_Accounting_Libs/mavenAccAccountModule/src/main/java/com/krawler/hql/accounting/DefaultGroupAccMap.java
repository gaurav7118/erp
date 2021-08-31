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
public class DefaultGroupAccMap {
    private String ID;
    private DefaultLayoutGroup defaultlayoutgroup;
    private String accountname;
    private String groupname;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public DefaultLayoutGroup getDefaultlayoutgroup() {
        return defaultlayoutgroup;
    }

    public void setDefaultlayoutgroup(DefaultLayoutGroup defaultlayoutgroup) {
        this.defaultlayoutgroup = defaultlayoutgroup;
    }

    public String getAccountname() {
        return accountname;
    }

    public void setAccountname(String accountname) {
        this.accountname = accountname;
    }

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }
}
