/*
 * Copyright (C) 2012  Krawler Information Systems Pvt Ltd
 * All rights reserved.
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.krawler.common.admin;

import java.io.Serializable;

/**
 *
 * @author krawler-user
 */
public class UserPermission implements Serializable {

    private String id;
    private Rolelist role;
    private ProjectFeature feature;
    private long permissionCode;
    private RoleUserMapping roleUserMapping;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    
    public RoleUserMapping getRoleUserMapping() {
        return roleUserMapping;
    }

    public void setRoleUserMapping(RoleUserMapping roleUserMapping) {
        this.roleUserMapping = roleUserMapping;
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

    public Rolelist getRole() {
        return role;
    }

    public void setRole(Rolelist role) {
        this.role = role;
    }
}
