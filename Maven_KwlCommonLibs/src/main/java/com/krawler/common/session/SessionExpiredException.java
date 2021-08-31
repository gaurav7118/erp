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
package com.krawler.common.session;

public class SessionExpiredException extends Exception {

    public static final String USERID_NULL = "session.USERID_NULL";
    public static final String USERNAME_NULL = "session.USERNAME_NULL";
    public static final String COMPANYID_NULL = "session.COMPANYID_NULL";
    public static final String TRIPROLEID_NULL = "session.TRIPROLEID_NULL";
    public static final String TRIPROLENAME_NULL = "session.TRIPROLENAME_NULL";
    public static final String LEAVEROLEID_NULL = "session.LEAVEROLEID_NULL";
    public static final String LEAVEROLENAME_NULL = "session.LEAVEROLENAME_NULL";
    public static final String ROLEPERM_NULL = "session.ROLEPERM_NULL";
    public static final String USERFULLNAME_NULL = "session.USERFULLNAME_NULL";
    public static final String COMPANYOBJECT_NULL = "session.COMPANYOBJECT_NULL";
    private static final long serialVersionUID = 8091202304180634894L;
    protected String mCode;

    public SessionExpiredException(String message, String code) {
        super(message);
        mCode = code;
    }
}
