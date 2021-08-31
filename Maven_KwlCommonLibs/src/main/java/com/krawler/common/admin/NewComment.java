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
import java.util.Date;
import com.krawler.common.admin.User;

public class NewComment implements java.io.Serializable {

    private String cid;
    private User userId;
    private Comment commentid;

    public NewComment() {
    }

    public NewComment(String cid, User userId, Comment commentid) {
        this.cid = cid;
        this.userId = userId;
        this.commentid = commentid;

    }

    public String getCid() {
        return this.cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public User getuserId() {
        return this.userId;
    }

    public void setuserId(User userId) {
        this.userId = userId;
    }

    public Comment getCommentid() {
        return this.commentid;
    }

    public void setCommentid(Comment commentid) {
        this.commentid = commentid;
    }
}
