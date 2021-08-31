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
package com.krawler.spring.accounting.costCenter;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author krawler
 */
public interface AccCostCenterDAO {

    KwlReturnObject getCostCenter(HashMap<String, Object> requestParams) throws ServiceException;

    Object saveCostCenter(HashMap<String, Object> dataMap) throws ServiceException;

    KwlReturnObject deleteCostCenter(String ccid, String companyid) throws ServiceException;

    KwlReturnObject checkUniqueCostCenter(String id, String ccid, String name, String companyid) throws ServiceException;

    Object saveSalesCommission(HashMap<String, Object> dataMap) throws ServiceException;

    KwlReturnObject deleteSalesCommission(String companyid) throws ServiceException;

    KwlReturnObject getSalesCommission(HashMap<String, Object> requestParams) throws ServiceException;

    KwlReturnObject deleteApprovalRules(String companyid, String ruleid) throws ServiceException;

    KwlReturnObject getApprovalRules(HashMap<String, Object> requestParams) throws ServiceException;

    KwlReturnObject saveApprovalRules(HashMap<String, Object> approvalRulesMap) throws ServiceException;

    KwlReturnObject editApprovalRules(HashMap<String, Object> approvalRulesMap) throws ServiceException;

    public List getProductName(String productid) throws ServiceException;
    
    public KwlReturnObject getProductCategoryNameByID(String productCategoryId, String companyId) throws ServiceException;
    
    public List getCustomerNameByID(HashMap<String, Object> dataMap) throws ServiceException;
    
    public List getPaymentTermNameByID(String termid,String companyid) throws ServiceException;
    
    public List getAccountNameByID(String companyId, String accid) throws ServiceException;
}
