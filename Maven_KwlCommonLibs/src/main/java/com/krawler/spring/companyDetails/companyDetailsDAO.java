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
package com.krawler.spring.companyDetails;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Karthik
 */
public interface companyDetailsDAO {

    public KwlReturnObject getCompanyInformation(HashMap<String, Object> requestParams, ArrayList filter_names, ArrayList filter_params) throws ServiceException;

    public KwlReturnObject getCompanyHolidays(HashMap<String, Object> requestParams, ArrayList filter_names, ArrayList filter_params) throws ServiceException;

     /**
     * Description :Below Method is used to Update Company Currency,Country Id etc.. 
     * @param <hmcompany> used get Company Currency,Country Id etc 
     * @return :void
     */
    public void updateCompany(HashMap hm) throws ServiceException;

    public void deleteCompany(HashMap<String, Object> requestParams) throws ServiceException;

    public String getSubDomain(String companyid) throws ServiceException;

    public String getCompanyid(String domain) throws ServiceException;

    public List getChildCompanies(String companyid) throws ServiceException;
    
    public boolean IsChildCompany(String parentSubdomain, String currentSubdomain) throws ServiceException;
    
    public KwlReturnObject getSingaporeCompaniesWithDifferentCurrency() throws ServiceException;
    
    public List getAllCompanyList(JSONObject json) throws ServiceException;
    
    public KwlReturnObject getCompanyProductList(JSONObject json) throws ServiceException;
}
