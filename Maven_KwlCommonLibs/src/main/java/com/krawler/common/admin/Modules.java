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

/**
 *
 * @author krawler
 */
public class Modules {

    private String id;
    private String moduleName;
    private String pojoClassPathFull;
    private String primaryKey_MethodName;
    private String uniqueKey_MethodName;
    private String uniqueKey_HbmName;
    private String moduleCategory;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getPojoClassPathFull() {
        return pojoClassPathFull;
    }

    public void setPojoClassPathFull(String pojoClassPathFull) {
        this.pojoClassPathFull = pojoClassPathFull;
    }

    public String getPrimaryKey_MethodName() {
        return primaryKey_MethodName;
    }

    public void setPrimaryKey_MethodName(String primaryKey_MethodName) {
        this.primaryKey_MethodName = primaryKey_MethodName;
    }

    public String getUniqueKey_MethodName() {
        return uniqueKey_MethodName;
    }

    public void setUniqueKey_MethodName(String uniqueKey_MethodName) {
        this.uniqueKey_MethodName = uniqueKey_MethodName;
    }

    public String getUniqueKey_HbmName() {
        return uniqueKey_HbmName;
    }

    public void setUniqueKey_HbmName(String uniqueKey_HbmName) {
        this.uniqueKey_HbmName = uniqueKey_HbmName;
    }
    
    public String getModuleCategory() {
        return moduleCategory;
    }

    public void setModuleCategory(String moduleCategory) {
        this.moduleCategory = moduleCategory;
    }
}
