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

public class Docmap implements Serializable {

    private String id;
    private String recid;
    private Docs docid;
    //@@@ - Need to check with Accounting and HRMS wheather relatedto field is required.
    private String relatedto;

    public String getId() {
        if (this.id == null) {
            this.id = "";
        }
        return this.id;
    }

    public void setId(String id) {
        if ((id == null && this.id != null)
                || (id != null && this.id == null)
                || (id != null && this.id != null && !id.equals(this.id))) {
            this.id = id;
        }
    }

    public String getRecid() {
        if (this.recid == null) {
            this.recid = "";
        }
        return this.recid;
    }

    public void setRecid(String recid) {
        if ((recid == null && this.recid != null)
                || (recid != null && this.recid == null)
                || (recid != null && this.recid != null && !recid.equals(this.recid))) {
            this.recid = recid;
        }
    }

    public Docs getDocid() {
        return this.docid;
    }

    public void setDocid(Docs docid) {
        if (docid != this.docid) {
            this.docid = docid;
        }
    }

    public String getRelatedto() {
        if (this.relatedto == null) {
            this.relatedto = "";
        }
        return this.relatedto;
    }

    public void setRelatedto(String relatedto) {
        if ((relatedto == null && this.relatedto != null)
                || (relatedto != null && this.relatedto == null)
                || (relatedto != null && this.relatedto != null && !relatedto.equals(this.relatedto))) {
            this.relatedto = relatedto;
        }
    }
}
