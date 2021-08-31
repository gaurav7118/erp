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

import com.krawler.common.admin.User;
import java.util.Date;

/**
 *
 * @author Shashank Bhosale
 */
public class SpreadSheetConfig {

    private String cid;
    private String rules;
    private String state;
    private String module;
    private int deleteflag;
    private Date updatedon;
    private User user;

    public void setCid(String cid) {
        this.cid = cid;
    }

    public void setRules(String rules) {
        this.rules = rules;
    }

    public void setState(String state) {
        this.state = state;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public void setDeleteflag(int deleteflag) {
        this.deleteflag = deleteflag;
    }

    public void setUpdatedon(Date updatedon) {
        this.updatedon = updatedon;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCid() {
        return cid;
    }

    public String getRules() {
        return rules;
    }

    public String getState() {
        return state;
    }

    public String getModule() {
        return module;
    }

    public int getDeleteflag() {
        return deleteflag;
    }

    public Date getUpdatedon() {
        return updatedon;
    }

    public User getUser() {
        return user;
    }
}
