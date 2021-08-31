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
package com.krawler.spring.accounting.groupcompany;

import com.krawler.common.service.ServiceException;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface AccGroupCompanyDAO {

    public KwlReturnObject fetchMultiCompanyDetails(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject fetchCustomerVendorDetails(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject fetchTaxMappingDetails(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject fetchTermMappingDetails(Map<String, Object> requestParams) throws ServiceException;

    public JSONObject saveDocumentTransactionsid(JSONObject paramJobj) throws ServiceException, JSONException;

    public KwlReturnObject fetchTransactionMappingDetails(Map<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject updateSubdomain(Map<String, Object> requestParams) throws ServiceException;

    public JSONObject deleteTransactionMappingRecord(Map<String, Object> requestParams) throws ServiceException, JSONException;

    public KwlReturnObject fetchSubdomains(JSONObject paramJobj) throws ServiceException;

    public JSONObject deleteExistingRecordsofTable(JSONObject paramJobj, Map<String, Object> requestParams) throws ServiceException, JSONException;

    public JSONObject insertRecordsInTable(String mappingDetails, Map<String, Object> requestParams, JSONObject paramJobj) throws ServiceException, JSONException;
    
    public boolean isSubdomainpresent(JSONObject paramJobj) throws ServiceException ;
    
    public String fetchDetailsid(JSONObject paramJobj, Map<String, Object> requestParams) throws ServiceException ;
    
    public String fetchlinkedDetailsid(Map<String,Object> valuesParams,String destinationmoduledetailstablename,String destinationmoduletablename,String sourcemoduledetailstablename) throws ServiceException;
    
    public Map<String, Object> getDetailsProductsid(JSONObject paramJobj, Map<String, Object> requestParams) throws ServiceException ;
    
    public Map<String, Object> checkEntryForDeliveryOrderinGRO(String deliveryorderid,String companyid) throws ServiceException;
    
    public Map<String, Object> checkEntryForSalesOrderInPO(String salesorderIds,String companyid) throws ServiceException ;
    
    public JSONObject getSourceCompanyPOTransactionid(JSONObject paramJObj, String companyid, String poDetailIds) throws ServiceException ;
    
    public JSONObject getSOdetailid(JSONObject paramJObj, String companyid, String poDetailIds) throws ServiceException;
    
    public JSONObject getSourceCompanyGROTranasctionId(JSONObject paramJObj, String companyid, String poDetailIds) throws ServiceException;
}
