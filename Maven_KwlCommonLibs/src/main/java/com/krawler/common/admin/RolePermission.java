/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.krawler.common.admin;

import java.io.Serializable;

/**
 *
 * @author krawler
 */
public class RolePermission implements Serializable  {
    
    private Rolelist role;
    private ProjectFeature feature;
    private Company company;
    private long permissionCode;
    
    public Company getCompany() {
        return company;
    }
   
     public void setCompany(Company company) {
        this.company = company;
    }


    public Rolelist getRole() {
        return role;
    }

    public void setRole(Rolelist role) {
        this.role = role;
    }

    public ProjectFeature getFeature() {
        return feature;
    }

    public void setFeature(ProjectFeature feature) {
        this.feature = feature;
    }

    public long getPermissionCode() {
        return permissionCode;
    }

    public void setPermissionCode(long permissionCode) {
        this.permissionCode = permissionCode;
    }

}
