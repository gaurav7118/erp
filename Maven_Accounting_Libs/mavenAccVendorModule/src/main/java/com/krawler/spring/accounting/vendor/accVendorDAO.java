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
package com.krawler.spring.accounting.vendor;

import com.krawler.common.service.ServiceException;
import com.krawler.hql.accounting.Vendor;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author krawler
 */
public interface accVendorDAO {

    public KwlReturnObject deleteVendor(String accountid, String companyid) throws ServiceException;
//    public KwlReturnObject saveVendor(accAccountDAO accAccountDAOobj,HashMap request, String companyid);
    public KwlReturnObject activateDeactivateVendors(HashMap request) throws ServiceException;
    
    public KwlReturnObject addVendor(HashMap request);

    public KwlReturnObject get1099EligibleVendor(String string, String ss) throws ServiceException;

    public KwlReturnObject updateVendor(HashMap request);

    public ArrayList getVendorArrayList(List list, HashMap<String, Object> requestParams, boolean quickSearchFlag, boolean noactivityAcc) throws ServiceException;

    public KwlReturnObject getVendor(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getVendorByCode(String vendorCode, String companyId) throws ServiceException;
    
    public KwlReturnObject getVendorByName(String vendorname, String companyId) throws ServiceException;

    public KwlReturnObject getVendorsForCombo(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getVendorList(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getNewVendorList(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getVendorCategoryIDs(String vendorid) throws ServiceException;
//    public KwlReturnObject getVendorObject(String vendorid);

    public KwlReturnObject getVendorForAgedPayable(HashMap request) throws ServiceException;

    public KwlReturnObject getVendor_Dashboard(String companyid, boolean isnull, String orderby, int start, int limit) throws ServiceException;

    public KwlReturnObject getLastTransactionVendor(String id, boolean isBilling) throws ServiceException;

    public KwlReturnObject getQuotationFromAccount(String accountid, String companyid) throws ServiceException;

    public KwlReturnObject saveVendorCategoryMapping(String vendorid, String vendorCategory) throws ServiceException;

    public KwlReturnObject deleteVendorCategoryMappingDtails(String vendorid) throws ServiceException;

    public KwlReturnObject saveIBGReceivingBankDetails(HashMap dataMap) throws ServiceException;

    public KwlReturnObject getIBGReceivingBankDetails(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject deleteIBGReceivingBankDetails(HashMap<String, Object> requestMap) throws ServiceException;

    public KwlReturnObject getVendorForTax(String taxid, String companyid) throws ServiceException;
    
    public KwlReturnObject getCustomerVendorMapping(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject deleteVendorAddresses(String vendorID, String companyid) throws ServiceException;
    
    public String updateVendorNumber(Map<String, Object> seqNumberMap);
    
    public KwlReturnObject vendorNumberCount(String customerNo, String companyId) throws ServiceException;
    
    public String updateVendorEntryNumberForNA(String prid, String entrynumber);

    public void saveVendorAgentMapping(Vendor vendor, String[] agents)throws ServiceException;

    public int deleteVendorAgentMapping(String vendorID) throws ServiceException;
    
    public KwlReturnObject updatePreferedVendorinproduct(String vendorID,String companyID) throws ServiceException;
    
    public KwlReturnObject saveAgentMapping(String vendorid, String agentid) throws ServiceException;
    
    public KwlReturnObject getVendorAndCurrencyDetailsForAgedPayable(HashMap request) throws ServiceException ;
    
    public KwlReturnObject getVendorAndCurrencyDetailsForParentAgedPayable(HashMap request) throws ServiceException;
    
    public KwlReturnObject getinvoiceDocuments(HashMap<String, Object> dataMap) throws ServiceException;

    public KwlReturnObject getChildVendors(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject checkDuplicateVendorForEdit(String entryNumber, String companyid, String vendorid) throws ServiceException;
    
    public KwlReturnObject getVendorCount(String vendorno, String companyid) throws ServiceException;
    
    public KwlReturnObject saveCIMBReceivingBankDetails(HashMap dataMap) throws ServiceException;
    
    public KwlReturnObject deleteCIMBReceivingBankDetails(HashMap<String, Object> requestMap) throws ServiceException;
    
    public KwlReturnObject getCIMBReceivingBankDetails(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject saveOCBCReceivingBankDetails(JSONObject paramsObj) throws ServiceException;
    
    public KwlReturnObject getOCBCReceivingBankDetails(JSONObject paramsObj) throws ServiceException;
    
    public KwlReturnObject deleteOCBCReceivingBankDetails(JSONObject paramsObj) throws ServiceException;
    
    public KwlReturnObject getAllVendorsOfCompany(String companyID) throws ServiceException;
    
    public KwlReturnObject getPaymentsWithCimb(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject UpdateCustomerVendorMapping(String vendorid) throws ServiceException;
    
    public KwlReturnObject getMasterItemFromDefaultMasterItem(String defaultId,String company) throws ServiceException;
    
    public KwlReturnObject getVendorForTDSPayment(String payment,String company) throws ServiceException;
    
    public Map<String, Double> getVendorGRAmtMap(String isexpensetype,String company) throws ServiceException;
    
    public String[] getMultiAgents(String vendorid) throws ServiceException;
    
    public KwlReturnObject saveGstVendorHistory(Map<String, Object> reqMap) throws ServiceException;
    
    public List getGstVendorHistory(Map<String, Object> reqMap) throws ServiceException;
    
    public List getGstVendorUsedHistory(Map<String, Object> reqMap) throws ServiceException;
    
    public KwlReturnObject deleteGstVendorHistory(String vendorid) throws ServiceException;
}
