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
package com.krawler.esp.handlers;

import com.krawler.common.admin.Apiresponse;
import com.krawler.common.admin.Company;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.utils.json.base.JSONObject;
/**
 *
 * @author Krawler
 */

public class APICallHandlerDAOImpl extends BaseDAO implements APICallHandlerDAO {
    
        /*
     * (non-Javadoc) @see
     * com.krawler.spring.common.kwlCommonTablesDAO#getClassObject(java.lang.String,
     * java.lang.String)
     */
    @Override
    public Object getClassObject(String classpath, String id) {
        Object obj = null;
        try {
            Class cls = Class.forName(classpath);
            obj = get(cls, id);
        } catch (ClassNotFoundException ex) {
            ex.getMessage();
        }
        return obj;
    }
   
    @Override
    public void addAPIResponse(String uid, String action, JSONObject jData, String companyid) throws ServiceException {
        //Please refer ticket ERP-21353 for more details
        String apiRequest = "action=" + action + "&data=" + jData.toString();
        apiRequest = apiRequest.length() > Constants.TEXT_MAX_LENGHT ? apiRequest.substring(0, Constants.TEXT_MAX_LENGHT-1) : apiRequest;
        
        Apiresponse apires = new Apiresponse();
        apires.setApiid(uid);
        apires.setCompanyid((Company) get(Company.class, companyid));
        apires.setApirequest(apiRequest);
        apires.setStatus(0);
        save(apires);
    }

    @Override
    public void updateAPIResponse(String uid, String res) throws ServiceException {
        //Please refer ticket ERP-21353 for more details        
        res = res.length() > Constants.TEXT_MAX_LENGHT ? res.substring(0, Constants.TEXT_MAX_LENGHT-1) : res;
        
        Apiresponse apires = (Apiresponse) load(Apiresponse.class, uid);
        apires.setApiresponse(res);
        apires.setStatus(1);
        save(apires);
    }
}