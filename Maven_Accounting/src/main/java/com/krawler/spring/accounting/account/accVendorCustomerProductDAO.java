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
package com.krawler.spring.accounting.account;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author krawler
 */
public interface accVendorCustomerProductDAO {
    public KwlReturnObject saveVendorProductMapping(String vendorid, String vendorCategory, String jsonString) throws ServiceException;
    public KwlReturnObject getProductsByVendor(String productid,String ss, String jsonstringFilter) throws ServiceException;  
    public KwlReturnObject getProductsVendorabsence(HashMap<String, Object> requestParams,List listvendorproducts) throws ServiceException;
    public KwlReturnObject deleteVendorProductMapped(String vendorid,String productid) throws ServiceException;
    public KwlReturnObject deleteCustomerProductMapped(String customerid) throws ServiceException;
    public KwlReturnObject saveCustomerProductMapping(String customerid, String customerCategory, String jsonString) throws ServiceException;
    public KwlReturnObject getProductsByCustomer(String productid,String ss, String jsonstringFilter) throws ServiceException;  
    public KwlReturnObject getProductsCustomerabsence(HashMap<String, Object> requestParams,List listcustomerproducts) throws ServiceException;
    public KwlReturnObject getProductByProductCode(String companyId, String string)throws ServiceException;
    public KwlReturnObject deleteCustomer(String customerId,String companyId)throws ServiceException;
    public KwlReturnObject deleteVendor(String vendorId,String companyId)throws ServiceException;  
    public KwlReturnObject getCustomerCategoryIDs(String customerid) throws ServiceException;
    public KwlReturnObject getProductsByVendorFunction(String companyId,String ss,int start,int limit) throws ServiceException; 
    public KwlReturnObject getProductsByCustomerfunction(String companyId,String ss,int start,int limit) throws ServiceException; 

    public KwlReturnObject getProductByNameorID(String companyid, String value, String masterGroupID, String fetchColumn, String conditionColumn) throws ServiceException;
    
}
