/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.common.admin;

/**
 *
 * @author krawler
 */
public class UserPreferences {
    
    private String userid;
    private String preferencesJSON;
    private User user;
    
    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getPreferencesJSON() {
        return preferencesJSON;
    }

    public void setPreferencesJSON(String preferencesJSON) {
        this.preferencesJSON = preferencesJSON;
    }
    
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
