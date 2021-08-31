/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.common.admin;

import java.util.Set;

public class UsersGroup {

    private String ID;
    private String Name;
    private Company company;
    private Set<UsersGroupMapping> usersGroupMappings;

    public Set<UsersGroupMapping> getUsersGroupMappings() {
        return usersGroupMappings;
    }

    public void setUsersGroupMappings(Set<UsersGroupMapping> usersGroupMappings) {
        this.usersGroupMappings = usersGroupMappings;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

}
