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
package com.krawler.customFieldMaster;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class fieldDataManagerDAOImpl extends BaseDAO implements fieldDataManagerDAO {

    private static final String CUSTOM_COLUMN_FORMULAE_QUERY = "select formulae,fieldname from CustomColumnFormulae where companyid.companyID=? and moduleid=? ";

    /*
     * (non-Javadoc) @see
     * com.krawler.customFieldMaster.fieldDataManagerDAO#setCustomData(java.util.HashMap)
     */
    public KwlReturnObject setCustomData(HashMap<String, Object> requestParams) throws ServiceException {
        List<Object> list = new ArrayList<Object>();
        String customdataclasspath = (String) requestParams.get("customdataclasspath");
        String moduleprimarykey = (String) requestParams.get("moduleprimarykey");
        requestParams.remove("customdataclasspath");
        requestParams.remove("moduleprimarykey");
        boolean success = false;
        try {
            Object user = setterMethod(requestParams, customdataclasspath, moduleprimarykey);
            list.add(user);
            success = true;
        } catch (Exception e) {
            success = false;
            System.out.println("Error is " + e);
        } finally {
            return new KwlReturnObject(success, "Field Data added successfully", "-1", list, list.size());
        }
    }

    @Override
    public List getCustomColumnFormulae(Object[] params) {
        try {
            return executeQuery(CUSTOM_COLUMN_FORMULAE_QUERY, params);
        } catch (ServiceException ex) {
            Logger.getLogger(fieldDataManagerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
