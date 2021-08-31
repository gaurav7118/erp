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
package com.krawler.spring.profileHandler;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Karthik
 */
public class profileHandler {

    public static String getUserFullName(List ll) throws ServiceException {
        String name = "";
        try {
            Iterator ite = ll.iterator();
            if (ite.hasNext()) {
                Object[] row = (Object[]) ite.next();
                name = (StringUtil.isNullOrEmpty((String) row[0]) ? "" : row[0]) + " " + (StringUtil.isNullOrEmpty((String) row[1]) ? "" : row[1]);
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("profileHandler.getUserFullName", e);
        }
        return name;
    }
}
