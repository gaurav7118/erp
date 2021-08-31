/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.common.admin;

public class UsersGroupMapping {

    private String ID;
    private UsersGroup usersGroup;
    private User user;

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

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
