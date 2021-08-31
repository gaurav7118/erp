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
package com.krawler.spring.authHandler;

import com.krawler.common.service.ServiceException;
import com.krawler.common.util.DataInvalidateException;
import com.krawler.spring.common.KwlReturnObject;
import java.awt.List;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author Karthik
 */
public interface authHandlerDAO {

    public String getCompanyAddress(String companyid);

    public KwlReturnObject verifyLogin(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getPreferences(HashMap<String, Object> requestParams) throws ServiceException;

    public DateFormat getUserDateFormatter(String formatid, String diff, boolean onlydate);

    @Deprecated
    public String getFormattedCurrency(double value, String currencyid);
    
    public String getFormattedCurrency(double value, String currencyid, String companyid);
    
    public String getFormattedCurrencyWithSign(double value, String currencyid);

    public String getCurrency(String currencyid);

    public String getSysEmailIdByCompanyID(String companyid);
    
    public String getFormattedCurrency(double value, String currencyid,boolean isCurrencyCode);
    
    public String getCurrency(String currencyid,boolean isCurrencyCode);
   
    public String getRoleIdByRoleName(HashMap<String, Object> requestParams) throws ServiceException;
     
    public String gefeateatureIdByFeatureName(String permissionGroup) throws ServiceException;

    public String getActivityIdByActivityName(String roleName,String featureID) throws ServiceException;

    public KwlReturnObject setRolePermissions(HashMap<String, Object> requestParams, String featureid, Long permissionCode) throws ServiceException;
    
    public void checkLockDatePeroid(Date orderDate, String companyId) throws ServiceException,DataInvalidateException;
}