/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.common.admin;

public class UserGroupFieldComboMapping {

    private String ID;
    private UsersGroup usersGroup;
    private FieldComboData fieldComboData;
    private int colnum;
    private int moduleid;
    private Company company;

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public UsersGroup getUsersGroup() {
        return usersGroup;
    }

    public void setUsersGroup(UsersGroup usersGroup) {
        this.usersGroup = usersGroup;
    }

    public FieldComboData getFieldComboData() {
        return fieldComboData;
    }

    public void setFieldComboData(FieldComboData fieldComboData) {
        this.fieldComboData = fieldComboData;
    }

    public int getColnum() {
        return colnum;
    }

    public void setColnum(int colnum) {
        this.colnum = colnum;
    }

    public int getModuleid() {
        return moduleid;
    }

    public void setModuleid(int moduleid) {
        this.moduleid = moduleid;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

}
