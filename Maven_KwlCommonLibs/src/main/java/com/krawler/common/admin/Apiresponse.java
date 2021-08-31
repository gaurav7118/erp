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

public class Apiresponse {

    private String apiid;
    private Company companyid;
    private String apirequest;
    private String apiresponse;
    private int status;
    private Date apitimestamp;

    public Company getCompanyid() {
        return companyid;
    }

    public void setCompanyid(Company companyid) {
        this.companyid = companyid;
    }

    public String getApiid() {
        return apiid;
    }

    public void setApiid(String apiid) {
        this.apiid = apiid;
    }

    public String getApirequest() {
        return apirequest;
    }

    public void setApirequest(String apirequest) {
        this.apirequest = apirequest;
    }

    public String getApiresponse() {
        return apiresponse;
    }

    public void setApiresponse(String apiresponse) {
        this.apiresponse = apiresponse;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Date getApitimestamp() {
        return apitimestamp;
    }

    public void setApitimestamp(Date apitimestamp) {
        this.apitimestamp = apitimestamp;
    }
}