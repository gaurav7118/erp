/*
 * Copyright (C) 2017  Krawler Information Systems Pvt Ltd
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
package com.krawler.spring.accounting.handler;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;
/**
 *
 * @author krawler
 */
public class AccountingHandlerServiceImpl implements AccountingHandlerService{

    private AccountingHandlerDAO accountingHandlerDAOobj;
    
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    
    @Override
    public Map<String, Object> getCustomerAddressDetailsMap(HashMap<String, Object> addrRequestParams) throws ServiceException {
        Map<String, Object> customerAddressDetailsMap = new HashMap<String, Object>();
        try {
            KwlReturnObject result = accountingHandlerDAOobj.getCustomerAddressDetailsMap(addrRequestParams);
            
            List<Object[]> list = result.getEntityList();
            
            if(list != null && list.size() > 0) {
                for(Object[] row : list) {
                    Object[] addressDetails = {row[1], row[2], row[3], row[4], row[5], row[6]}; // row[5] is County
                    String customerid = row[0] != null ? row[0].toString() : "";
                    if(!StringUtil.isNullOrEmpty(customerid)) {
                        customerAddressDetailsMap.put(customerid, addressDetails);
                    }
                }
            }            
        } catch (Exception ex) {
            Logger.getLogger(AccountingHandlerDAOImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return customerAddressDetailsMap;
    }
                    }
