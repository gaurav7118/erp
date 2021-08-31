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

public class Docs implements Serializable {

    private String id;
    private String docname;
    private String docsize;
    private String doctype;
    private java.util.Date uploadedon;
    private String storename;
    private int storageindex;
    private User user;
    private String moduleId;
    private String recordId;
    private Company company;
    private int deleteflag;
    private String tags;
    private String comments;
    private int isshared;

    public int getIsshared() {
        return isshared;
    }

    public void setIsshared(int isshared) {
        this.isshared = isshared;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
        
   
    public String getDocname() {
        if (this.docname == null) {
            this.docname = "";
        }
        return this.docname;
    }

    public void setDocname(String docname) {
        if ((docname == null && this.docname != null)
                || (docname != null && this.docname == null)
                || (docname != null && this.docname != null && !docname.equals(this.docname))) {
            this.docname = docname;
        }
    }

    public String getDocsize() {
        if (this.docsize == null) {
            this.docsize = "";
        }
        return this.docsize;
    }

    public void setDocsize(String docsize) {
        if ((docsize == null && this.docsize != null)
                || (docsize != null && this.docsize == null)
                || (docsize != null && this.docsize != null && !docsize.equals(this.docsize))) {
            this.docsize = docsize;
        }
    }

    public String getDoctype() {
        if (this.doctype == null) {
            this.doctype = "";
        }
        return this.doctype;
    }

    public void setDoctype(String doctype) {
        if ((doctype == null && this.doctype != null)
                || (doctype != null && this.doctype == null)
                || (doctype != null && this.doctype != null && !doctype.equals(this.doctype))) {
            this.doctype = doctype;
        }
    }

    public java.util.Date getUploadedon() {
        return this.uploadedon;
    }

    public void setUploadedon(java.util.Date uploadedon) {
        if (uploadedon != this.uploadedon) {
            this.uploadedon = uploadedon;
        }
    }

    public String getStorename() {
        if (this.storename == null) {
            this.storename = "";
        }
        return this.storename;
    }

    public void setStorename(String storename) {
        if ((storename == null && this.storename != null)
                || (storename != null && this.storename == null)
                || (storename != null && this.storename != null && !storename.equals(this.storename))) {
            this.storename = storename;
        }
    }

    public int getStorageindex() {
        return this.storageindex;
    }

    public void setStorageindex(int storageindex) {
        if (storageindex != this.storageindex) {
            this.storageindex = storageindex;
        }
    }

    public String getModuleId() {
        return moduleId;
    }

    public void setModuleId(String moduleId) {
        this.moduleId = moduleId;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public int getDeleteflag() {
        return this.deleteflag;
    }

    public void setDeleteflag(int deleteflag) {
        this.deleteflag = deleteflag;
    }

    public Company getCompany() {
        return this.company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public String getTags() {
        if (this.tags == null) {
            this.tags = "";
        }
        return this.tags;
    }

    public void setTags(String tags) {
        if ((tags == null && this.tags != null)
                || (tags != null && this.tags == null)
                || (tags != null && this.tags != null && !tags.equals(this.tags))) {
            this.tags = tags;
        }
    }

    public String getComments() {
        if (this.comments == null) {
            this.comments = "";
        }
        return this.comments;
    }

    public void setComments(String comments) {
        if ((comments == null && this.comments != null)
                || (comments != null && this.comments == null)
                || (comments != null && this.comments != null && !comments.equals(this.comments))) {
            this.comments = comments;
        }
    }
}
