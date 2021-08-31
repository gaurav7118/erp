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
package com.krawler.spring.accounting.customer;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.LoanRules;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.UOBReceivingDetails;
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
public interface accCustomerDAO {

//    public KwlReturnObject getCustomerObj(String customerid);
    public KwlReturnObject addCustomer(HashMap request);

    public KwlReturnObject deleteCustomer(String accountid, String companyid) throws ServiceException;
    
    /**
     * Description : Below Method is used to Activate or Deactivate Customers
     * @param <request> used to get activate or deactivate flag and customer details
     * @return :KwlReturnObject 
     */
    public KwlReturnObject activateDeactivateCustomers(HashMap request) throws ServiceException;
    
    public KwlReturnObject checkCustomerExist(String crmaccountid, String companyid) throws ServiceException;

    public KwlReturnObject checkCustomerExistbyCode(String accountid, String companyid, String exceptCustomerId) throws ServiceException;
    
    public KwlReturnObject checkCustomerExistbyCode(String accountid, String companyid) throws ServiceException;

    public KwlReturnObject updateCustomer(HashMap request);

    public KwlReturnObject getCustomer(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getCustomerIds(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getDefaultCreditTermForCustomer(String companyid) throws ServiceException;

    public KwlReturnObject getInactiveCustomer(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getCustomerByCode(String customerCode, String companyId) throws ServiceException;
    
    public KwlReturnObject getCustomerByCode(String customerCode, String companyId, String crmAccountId) throws ServiceException;

    public KwlReturnObject getCustomerByName(String customername, String companyId) throws ServiceException;
    
    public KwlReturnObject getCustomerByName(String customerName, String companyId,String crmAccountId) throws ServiceException;
    
    public KwlReturnObject getCustomerByCodeOrName(String customerCode, String companyId) throws ServiceException;
    
    public KwlReturnObject getTermIdByDays(int days, String companyId) throws ServiceException;
    
    public ArrayList getCustomerArrayList(List list, HashMap<String, Object> requestParams, boolean quickSearchFlag, boolean noactivityAcc) throws ServiceException;

    public KwlReturnObject getCustomersForCombo(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getCustomerList(HashMap<String, Object> requestParams) throws ServiceException;
    
    public JSONObject getLoanConfirmation(String customerId,String companyId) throws ServiceException;
    
    public JSONObject checkPaymentStausIsPaid(String customerId,String companyId,String loanRefNumber) throws ServiceException;

    public KwlReturnObject getNewCustomerList(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getCustomerTransactionDetail(HashMap<String, Object> requestParams) throws ServiceException;

    public KwlReturnObject getCustomerCategoryIDs(String customerid) throws ServiceException;
    
    public KwlReturnObject getMultiSalesPersonIDs(String customerid) throws ServiceException;

    public String[] getMultiSalesPerson(String customerid) throws ServiceException;

    public KwlReturnObject getContractForDO(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getCustomerForAgedReceivable(HashMap request) throws ServiceException;

    public KwlReturnObject getCustomer_Dashboard(String companyid, boolean isnull, String orderby, int start, int limit) throws ServiceException;

    public KwlReturnObject getQuotationFromAccount(String accountid, String companyid) throws ServiceException;
    
    public KwlReturnObject getDeliveryOrderFromAccount(String accountid, String companyid) throws ServiceException;

    public void saveCustomizedAgedDuration(String companyId, int fromDuration, int toDuration) throws ServiceException;

    public void updateCustomizedAgedDuration(String companyId, String id, int fromDuration, int toDuration) throws ServiceException;

    public KwlReturnObject deleteCustomizedAgedDuration(String companyId, String id) throws ServiceException;

    public KwlReturnObject getCustomizedAgedDuration(String companyId) throws ServiceException;

    public KwlReturnObject getLastTransactionCustomer(String id, boolean isBilling) throws ServiceException;

    public KwlReturnObject saveCustomerCategoryMapping(String customerid, String customerCategory) throws ServiceException;

    public KwlReturnObject saveSalesPersonMapping(String customerid, String salesPerson) throws ServiceException;

    public KwlReturnObject deleteCustomerCategoryMappingDtails(String customerid) throws ServiceException;
    
    public KwlReturnObject deletecustomervendormapping(String customerid) throws ServiceException;
    
    public KwlReturnObject deleteSalesPersonMappingDtails(String customerid) throws ServiceException;
    
    public KwlReturnObject resetCustomerDefaultSalesPerson(String customerid, String salesPersons[]) throws ServiceException;

    public KwlReturnObject getCategorytByName(String companyId, String categoryName, String groupid) throws ServiceException;
    public KwlReturnObject getSalesPersonByName(String companyId, String salesPersonName) throws ServiceException;
    
    public KwlReturnObject getCustomerForTax(String taxid, String companyid) throws ServiceException;
      
    public KwlReturnObject addCustomerWarehouses(HashMap<String, Object> mitemmap) throws ServiceException;
    
    public KwlReturnObject addCustomerWarehouseMapping(String warehouse,String customerId,boolean isEdit,String itemID) throws ServiceException;
    public KwlReturnObject addCustomerWarehouseMapping(String warehouse,String customerId,boolean isEdit,String itemID,boolean isDeafault) throws ServiceException;
    
    public KwlReturnObject getCustomerWarehouses(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getCustomerWarehousesMap(HashMap<String, Object> requestParams ,Company company) throws ServiceException;
    
    public KwlReturnObject getCustomerDefaultWarehousesMap(HashMap<String, Object> requestParams ,Company company) throws ServiceException;
    
    public void updateCustomersDefaultWarehouse(String warehouseMapId, String warehouseId,String customerId,boolean isDefault) throws ServiceException;
    
  public String getCustomerWarehousesMapById(String mapingId ,Company company) throws ServiceException;
    public KwlReturnObject deleteCustomerFromWarehouseMap(String accountid,String[] customerIds, String companyid) throws ServiceException;
    
    public KwlReturnObject deleteCustomerWarehouses(String itemid) throws ServiceException;
    
    public KwlReturnObject getBatchForAsset(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject getTitleForCustomer(String companyid,String value) throws ServiceException;
    
    public String updateCustomerNumber(Map<String, Object> seqNumberMap);
    
    public KwlReturnObject customerNumberCount(String customerNo, String companyId) throws ServiceException;
    
    public String updateCustomerEntryNumberForNA(String prid, String entrynumber);
    
    public String getDeliveryorderId(String companyid, String dodetailsid) throws ServiceException;

    public KwlReturnObject getCustomerWithCurrencyForAgedReceivables(HashMap request) throws ServiceException;
    
    public void saveLoanDisburementRule(LoanRules loanRules) throws ServiceException;
    
//    public int checkConsignmentApprovalRules(String ruleId, String requestorid, String warehouseid, String locations, String approverid, String companyid) throws ServiceException;
    
    KwlReturnObject getLoanRules(HashMap<String, Object> requestParams) throws ServiceException;
    
    KwlReturnObject deleteLoanRule(String companyid, String ruleid) throws ServiceException;
    
    public KwlReturnObject getMasterItemByNameorID(String companyid, String value, String masterGroupID,String fetchColumn,String conditionColumn)throws ServiceException;

    public KwlReturnObject getChildCustomerCount(HashMap<String, Object> customerRequestParams)throws ServiceException;

    public KwlReturnObject checkDuplicateCustomerForEdit(String entryNumber, String companyid, String vendorid) throws ServiceException;
    
    public KwlReturnObject getCustomerCount(String vendorno, String companyid) throws ServiceException;
    
    public KwlReturnObject getCustomerWithPartNumber(String partnumber, String companyid) throws ServiceException;
    
    public KwlReturnObject UpdateCustomerVendorMapping(String customerid) throws ServiceException;
    
    public KwlReturnObject saveCustomerCheckIn(JSONObject paramJobj)throws SessionExpiredException;
    
    public KwlReturnObject saveCustomerCheckOut(JSONObject paramJobj) throws SessionExpiredException; 
    
    public KwlReturnObject getCustomerCheckIn(JSONObject jobj) throws SessionExpiredException,ServiceException;
    
    public int getCustomerCount(HashMap<String, Object> requestParams) throws ServiceException;
    
    public KwlReturnObject saveUOBReceivingBankDetails(HashMap dataMap) throws ServiceException;

    public KwlReturnObject getUOBReceivingBankDetails(HashMap dataMap) throws ServiceException;
    
    public KwlReturnObject deleteExistingReceivingData(HashMap<String, Object> datamap) throws ServiceException;
    
    public boolean isIBGDetailsUsedInTransaction(HashMap<String,Object> datamap) throws ServiceException;
    
    public KwlReturnObject getGiroFileGenerationHistory(HashMap dataMap) throws ServiceException;
    
    public KwlReturnObject updateStatusOfExistingBankAccountType(HashMap<String, Object> datamap) throws ServiceException;
    
    public KwlReturnObject getCustomFieldsValuesForCustomer(String customerID, String companyID, List<String> colNamesList) throws ServiceException;
    
    public int getUniqueCase(JSONObject object);
    public KwlReturnObject saveGstCustomerHistory(Map<String, Object> reqMap) throws ServiceException;
    
    public List getGstCustomerHistory(Map<String, Object> reqMap) throws ServiceException;
    
    public List getGstCustomerUsedHistory(Map<String, Object> reqMap) throws ServiceException;
    
    public KwlReturnObject deleteGstCustomerHistory(String customerid) throws ServiceException;
    
}
