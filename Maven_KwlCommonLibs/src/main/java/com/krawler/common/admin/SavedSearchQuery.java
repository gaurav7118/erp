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

import java.util.Date;

/**
 *
 * @author Somnath Jadhav
 */
public class SavedSearchQuery {

    private String searchId;
    private User user;
    private int moduleid;
    private String searchName;
    private String searchquery;
    private int deleteFlag;
    private Long updatedOn;
    private int filterAppend; // 0- OR    1- AND
    private String templateid;//For Custom Layout templates
    private boolean iscustomlayout;//To identify whether the search was saved from Custom Layout or Report List
    private String customReportId;//To identify whether the search was saved from Custom report(Report Builder) or Report List
    private String templatetitle;

    public Long getUpdatedOn() {
        return updatedOn;
    }

    public SavedSearchQuery() {
    }
    
    public SavedSearchQuery(SavedSearchQuery searchdetails) {
        if(searchdetails!=null){
        this.moduleid = searchdetails.moduleid;
        this.searchName = searchdetails.searchName;
        this.searchquery = searchdetails.searchquery;
        this.deleteFlag = searchdetails.deleteFlag;
        this.filterAppend = searchdetails.filterAppend;
        this.templateid = searchdetails.templateid;
        this.iscustomlayout = searchdetails.iscustomlayout;
        this.templatetitle = searchdetails.templatetitle;
        }
    }

    public String getCustomReportId() {
        return customReportId;
    }

    public void setCustomReportId(String customReportId) {
        this.customReportId = customReportId;
    }

    public void setUpdatedOn(Long updatedOn) {
        this.updatedOn = updatedOn;
    }

    public Date getUpdatedon() {
        if (this.updatedOn != null) {
            return new Date(this.updatedOn);
        }
        return null;
    }

    public void setUpdatedon(Date updatedon) {
        this.updatedOn = updatedon.getTime();
    }

    public int getDeleteFlag() {
        return deleteFlag;
    }

    public void setDeleteFlag(int deleteFlag) {
        this.deleteFlag = deleteFlag;
    }

    public int getModuleid() {
        return moduleid;
    }

    public void setModuleid(int moduleid) {
        this.moduleid = moduleid;
    }

    public String getSearchId() {
        return searchId;
    }

    public void setSearchId(String searchId) {
        this.searchId = searchId;
    }

    public String getSearchName() {
        return searchName;
    }

    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    public String getSearchquery() {
        return searchquery;
    }

    public void setSearchquery(String searchquery) {
        this.searchquery = searchquery;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getFilterAppend() {
        return filterAppend;
    }

    public void setFilterAppend(int filterAppend) {
        this.filterAppend = filterAppend;
    }
    
    public String getTemplateid() {
        return templateid;
    }

    public void setTemplateid(String templateid) {
        this.templateid = templateid;
    }
    
    public boolean isIscustomlayout() {
        return iscustomlayout;
    }

    public void setIscustomlayout(boolean iscustomlayout) {
        this.iscustomlayout = iscustomlayout;
    }

    public String getTemplatetitle() {
        return templatetitle;
    }

    public void setTemplatetitle(String templatetitle) {
        this.templatetitle = templatetitle;
    }
}
