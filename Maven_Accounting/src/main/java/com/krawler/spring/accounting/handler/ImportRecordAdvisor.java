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

package com.krawler.spring.accounting.handler;

import com.krawler.common.admin.CustomerAddressDetails;
import com.krawler.common.admin.State;
import com.krawler.common.admin.User;
import com.krawler.common.admin.VendorAddressDetails;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.DataInvalidateException;
import com.krawler.common.util.IndiaComplianceConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.customFieldMaster.fieldDataManager;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.account.accCusVenMapDAO;
import com.krawler.spring.accounting.account.accVendorCustomerProductDAO;
import com.krawler.spring.accounting.customer.accCustomerControllerCMNService;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.vendor.accVendorControllerCMNService;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

/**
 *
 * @author krawler
 */
public class ImportRecordAdvisor extends BaseDAO implements MethodInterceptor {
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accVendorCustomerProductDAO accVendorCustomerProductDAOobj;
    private accCustomerDAO accCustomerDAOobj;
    private accVendorDAO accVendorDAOobj;
    private accAccountDAO accAccountDAOobj;
    private accCusVenMapDAO accCusVenMapDAOObj;
    private fieldDataManager fieldDataManager;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private accCustomerControllerCMNService accCustomerControllerCMNServiceObj;
    private accVendorControllerCMNService accVendorcontrollerCMNService;
    
    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }
    
    public void setaccVendorDAO(accVendorDAO accVendorDAOobj) {
        this.accVendorDAOobj = accVendorDAOobj;
    }
    public void setaccCustomerDAO(accCustomerDAO accCustomerDAOobj) {
        this.accCustomerDAOobj = accCustomerDAOobj;
    }

    public void setAccVendorCustomerProductDAOobj(accVendorCustomerProductDAO accVendorCustomerProductDAOobj) {
        this.accVendorCustomerProductDAOobj = accVendorCustomerProductDAOobj;
    }
    
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    
    public void setAccCusVenMapDAOObj(accCusVenMapDAO accCusVenMapDAOObj) {
        this.accCusVenMapDAOObj = accCusVenMapDAOObj;
    }
    
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManager = fieldDataManagercntrl;
    }
    public void setaccCustomerControllerCMNServiceObj(accCustomerControllerCMNService accCustomerControllerCMNServiceObj) {
        this.accCustomerControllerCMNServiceObj = accCustomerControllerCMNServiceObj;
    }
    public void setaccVendorcontrollerCMNService(accVendorControllerCMNService accVendorcontrollerCMNService) {
        this.accVendorcontrollerCMNService = accVendorcontrollerCMNService;
    }

    @Override
    public Object invoke(MethodInvocation mi) throws Throwable {
        Object result = null;
        boolean proceed = true;
        String methodName = mi.getMethod().getName();

        if(methodName.equals("saveRecord")) {
            proceed = BeforeSaveRecord(mi);
            // Throw DataInvalidateException : To stop execution and to log Invalidate data message or any other error
            // Return false : To stop execution without any error log
            // Return true  : To continue the execution
            if(proceed) {
                result = mi.proceed(); //Execute main method
                AfterSaveRecord(mi, result);
            }
        } else if(methodName.equals("getRefModuleData")) {
            BeforeGetRefModuleData(mi);
            result = mi.proceed(); //Execute main method
            result = AfterGetRefModuleData(mi, result);
        } else {
            result = mi.proceed();
        }

        return result;
    }

    private KwlReturnObject getSundryAccount(String companyId, boolean isVendor) throws ServiceException {
        String query = "select acc.id from "+ (isVendor ? "vendor v " : "customer v ") +"  right join account acc  ON v.id = acc.id where acc.company =  ?  and v.ID is  null  and acc.name =  ? ";
        ArrayList params = new ArrayList();
        params.add(companyId);
        params.add(isVendor ? Constants.SUNDRY_VENDOR : Constants.SUNDRY_CUSTOMER);
        List list = executeSQLQuery( query, params.toArray());;
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
    }

    private KwlReturnObject getVendorCustomerDefaultAccounts(String  companyId) throws ServiceException {
       String query = "select customerdefaultaccount,vendordefaultaccount from compaccpreferences where id=?";
        ArrayList params = new ArrayList();
        params.add(companyId);
        List list = executeSQLQuery( query, params.toArray());;
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
    }
    private KwlReturnObject getTax(String companyId,String  taxName) throws ServiceException {
        String query = "select id,taxcode from tax where company=? and name=?";
        ArrayList params = new ArrayList();
        params.add(companyId);
        params.add(taxName);
        List list = executeSQLQuery( query, params.toArray());;
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
    }        
    private KwlReturnObject getMasterItemId(String companyId,String value,String groupid) throws ServiceException {
       String query = "select id from masteritem where company=? and value=? and masterGroup=?";
        ArrayList params = new ArrayList();
        params.add(companyId);
        params.add(value);
        params.add(groupid);
        List list = executeSQLQuery( query, params.toArray());;
        int count = list.size();
        return new KwlReturnObject(true, "", "", list, count);
    }

    private boolean BeforeSaveRecord(MethodInvocation mi) throws DataInvalidateException {
        boolean proceed = true;
        //public Object saveRecord(HttpServletRequest request, HashMap<String, Object> dataMap, Object csvReader, String modeName, String classPath, String primaryKey, Object extraObj, JSONArray customfield) throws DataInvalidateException;
         Object arguments[] = mi.getArguments();
        String mode = (String) arguments[3];
        HashMap<String, Object> hmap = (HashMap<String, Object>) arguments[0];
        String pref = (String) hmap.get("masterPreference");
        
        boolean updateExistingRecordFlag = false;
        if (hmap.containsKey("updateExistingRecordFlag") && hmap.get("updateExistingRecordFlag") != null) {
            updateExistingRecordFlag = (Boolean) hmap.get("updateExistingRecordFlag");
        }
        
        try {
            if (mode.equalsIgnoreCase("customer") || mode.equalsIgnoreCase("vendor")) {
                HashMap<String, Object> addressMap = new HashMap<String, Object>();
                HashMap<String, Object> dataMap = (HashMap<String, Object>) arguments[1];
                String companyId = (String) dataMap.get("Company");
                JSONArray arry = (JSONArray) arguments[7];
                
                JSONArray customfield = arry;
                arry = new JSONArray();
//                dataMap.remove("Parent");
                dataMap.remove("DepreciationAccont");
                dataMap.remove("OpeningBalance");
                dataMap.remove("CustomerAddresses");

                JSONObject obj = new JSONObject();
                if (dataMap.containsKey("Category")) {
                    String cotegoryName = (String) dataMap.get("Category");
                    obj.put("Category", cotegoryName != null ? cotegoryName : "");
                    dataMap.remove("Category");
                }
                if (dataMap.containsKey("SalesPerson")) {
                    String salesPersonName = (String) dataMap.get("SalesPerson");
                    obj.put("SalesPerson", salesPersonName != null ? salesPersonName : "");
                    dataMap.remove("SalesPerson");
                }
                 if (dataMap.containsKey("Agent")) {
                    String agents = (String) dataMap.get("Agent");
                    obj.put("Agent", agents != null ? agents : "");
                    dataMap.remove("Agent");
                }
                if (dataMap.containsKey("Products")) {
                    String ProductsCodes = (String) dataMap.get("Products");
                    obj.put("Products", ProductsCodes != null ? ProductsCodes : "");
                    dataMap.remove("Products");
                }
                if (dataMap.containsKey("PaymentCriteria") && dataMap.get("PaymentCriteria")!=null) {
                    String paymentCriteria = (String) dataMap.get("PaymentCriteria");
                    dataMap.remove("PaymentCriteria");
                    if(paymentCriteria.equalsIgnoreCase("LIFO")){
                        dataMap.put("PaymentCriteria", 2);
                    }else if(paymentCriteria.equalsIgnoreCase("FIFO")){
                        dataMap.put("PaymentCriteria", 3);
                    }else if(paymentCriteria.equalsIgnoreCase("NA")){
                        dataMap.put("PaymentCriteria", 1);
                    } else {
                        dataMap.put("PaymentCriteria", 0);
                    }
                }
                if (!dataMap.containsKey("CreatedOn") || (dataMap.containsKey("CreatedOn") && dataMap.get("CreatedOn")==null)) {
                    if(!updateExistingRecordFlag){
                        dataMap.put("CreatedOn",new Date());
                    }
                }
                if (dataMap.containsKey("Mapcustomervendor") && dataMap.get("Mapcustomervendor")!=null) {
                    boolean mapCustomerVendor = Boolean.parseBoolean(dataMap.get("Mapcustomervendor").toString());
                    if(mapCustomerVendor){
                        if(mode.equalsIgnoreCase("Customer")){
                            if (dataMap.containsKey("VendorAcccode") && dataMap.get("VendorAcccode")!=null) {                                
                                String vendorAcccode = (String) dataMap.get("VendorAcccode");
                                if(!StringUtil.isNullOrEmpty(vendorAcccode)){
                                    obj.put("VendorAcccode", vendorAcccode);
                                }
                            }
                            if (dataMap.containsKey("VendorAccount") && dataMap.get("VendorAccount")!=null) {
                                String vendorAccount = (String) dataMap.get("VendorAccount");
                                if(!StringUtil.isNullOrEmpty(vendorAccount)){
                                    obj.put("VendorAccount", vendorAccount);
                                }
                            }
                            if (dataMap.containsKey("ManufacturerType") && dataMap.get("ManufacturerType")!=null) {
                                String manufacturerType = (String) dataMap.get("ManufacturerType");
                                if(!StringUtil.isNullOrEmpty(manufacturerType)){
                                    obj.put("ManufacturerType", manufacturerType);
                                }
                            }
                            if (dataMap.containsKey("VendorID") && dataMap.get("VendorID")!=null) {
                                String vendorID = (String) dataMap.get("VendorID");
                                if(!StringUtil.isNullOrEmpty(vendorID)){
                                    obj.put("VendorID", vendorID);
                                }
                            }
                        }else if(mode.equalsIgnoreCase("Vendor")){
                            if (dataMap.containsKey("CustomerAcccode") && dataMap.get("CustomerAcccode")!=null) {
                                String customerAcccode = (String) dataMap.get("CustomerAcccode");
                                if(!StringUtil.isNullOrEmpty(customerAcccode)){
                                    obj.put("CustomerAcccode", customerAcccode);
                                }
                            }
                            if (dataMap.containsKey("CustomerAccount") && dataMap.get("CustomerAccount")!=null) {
                                String customerAccount = (String) dataMap.get("CustomerAccount");
                                if(!StringUtil.isNullOrEmpty(customerAccount)){
                                    obj.put("CustomerAccount", customerAccount);
                                }
                            }
                            if (dataMap.containsKey("CustomerID") && dataMap.get("CustomerID")!=null) {
                                String customerID = (String) dataMap.get("CustomerID");
                                if(!StringUtil.isNullOrEmpty(customerID)){
                                    obj.put("CustomerID", customerID);
                                }
                            }
                        }
                    }
                }
                
                if (mode.equalsIgnoreCase("Customer")) {
                    dataMap.remove("VendorAcccode");
                    dataMap.remove("VendorAccount");
                    dataMap.remove("ManufacturerType");
                    dataMap.remove("VendorID");
                } else if (mode.equalsIgnoreCase("Vendor")) {
                    dataMap.remove("CustomerAcccode");
                    dataMap.remove("CustomerAccount");
                    dataMap.remove("CustomerID");
                }
               
                arry.put(0, obj);  //putting object by index, so that I can access it by same index in afterave method   
               
                JSONObject addrObj = new JSONObject();
                String str = "";
                if (dataMap.containsKey("BillingAliasName")) {
                    str = (String) dataMap.get("BillingAliasName");
                    addrObj.put("billingAliasName", str);
                    dataMap.remove("BillingAliasName");
                }
                if (dataMap.containsKey("BillingAddress")) {
                    str = (String) dataMap.get("BillingAddress");
                    addrObj.put("billingAddress", str);
                    dataMap.remove("BillingAddress");
                }
                if (dataMap.containsKey("BillingCounty")) {
                    str = (String) dataMap.get("BillingCounty");
                    addrObj.put("billingCounty", str);
                    dataMap.remove("BillingCounty");
                }
                if (dataMap.containsKey("BillingCity")) {
                    str = (String) dataMap.get("BillingCity");
                    addrObj.put("billingCity", str);
                    dataMap.remove("BillingCity");
                }
                if (dataMap.containsKey("BillingState")) {
                    str = (String) dataMap.get("BillingState");
                    addrObj.put("billingState", str);
                    dataMap.remove("BillingState");
                }
                if (dataMap.containsKey("BillingCountry")) {
                    str = (String) dataMap.get("BillingCountry");
                    addrObj.put("billingCountry", str);
                    dataMap.remove("BillingCountry");
                }
                if (dataMap.containsKey("BillingPostalCode")) {
                    str = (String) dataMap.get("BillingPostalCode");
                    addrObj.put("billingPostal", str);
                    dataMap.remove("BillingPostalCode");
                }
                if (dataMap.containsKey("BillingPhone")) {
                    str = (String) dataMap.get("BillingPhone");
                    addrObj.put("billingPhone", str);
                    dataMap.remove("BillingPhone");
                }
                if (dataMap.containsKey("BillingMobile")) {
                    str = (String) dataMap.get("BillingMobile");
                    addrObj.put("billingMobile", str);
                    dataMap.remove("BillingMobile");
                }
                if (dataMap.containsKey("BillingFax")) {
                    str = (String) dataMap.get("BillingFax");
                    addrObj.put("billingFax", str);
                    dataMap.remove("BillingFax");
                }
                if (dataMap.containsKey("BillingEmail")) {
                    str = (String) dataMap.get("BillingEmail");
                    addrObj.put("billingEmail", str);
                    dataMap.remove("BillingEmail");
                }
                if (dataMap.containsKey("BillingRecipientName")) {
                    str = (String) dataMap.get("BillingRecipientName");
                    addrObj.put("billingRecipientName", str);
                    dataMap.remove("BillingRecipientName");
                }
                if (dataMap.containsKey("BillingContactPerson")) {
                    str = (String) dataMap.get("BillingContactPerson");
                    addrObj.put("billingContactPerson", str);
                    dataMap.remove("BillingContactPerson");
                }
                if (dataMap.containsKey("BillingContactPersonNumber")) {
                    str = (String) dataMap.get("BillingContactPersonNumber");
                    addrObj.put("billingContactPersonNumber", str);
                    dataMap.remove("BillingContactPersonNumber");
                }
                if (dataMap.containsKey("BillingContactPersonDesignation")) {
                    str = (String) dataMap.get("BillingContactPersonDesignation");
                    addrObj.put("billingContactPersonDesignation", str);
                    dataMap.remove("BillingContactPersonDesignation");
                }
                if (dataMap.containsKey("BillingWebsite")) {
                    str = (String) dataMap.get("BillingWebsite");
                    addrObj.put("billingWebsite", str);
                    dataMap.remove("BillingWebsite");
                }
                if (dataMap.containsKey("ShippingAliasName")) {
                    str = (String) dataMap.get("ShippingAliasName");
                    addrObj.put("shippingAliasName", str);
                    dataMap.remove("ShippingAliasName");
                }
                if (dataMap.containsKey("ShippingAddress")) {
                    str = (String) dataMap.get("ShippingAddress");
                    addrObj.put("shippingAddress", str);
                    dataMap.remove("ShippingAddress");
                }
                if (dataMap.containsKey("ShippingCounty")) {
                    str = (String) dataMap.get("ShippingCounty");
                    addrObj.put("shippingCounty", str);
                    dataMap.remove("ShippingCounty");
                }
                if (dataMap.containsKey("ShippingCity")) {
                    str = (String) dataMap.get("ShippingCity");
                    addrObj.put("shippingCity", str);
                    dataMap.remove("ShippingCity");
                }
                if (dataMap.containsKey("ShippingState")) {
                    str = (String) dataMap.get("ShippingState");
                    addrObj.put("shippingState", str);
                    dataMap.remove("ShippingState");
                }
                if (dataMap.containsKey("ShippingCountry")) {
                    str = (String) dataMap.get("ShippingCountry");
                    addrObj.put("shippingCountry", str);
                    dataMap.remove("ShippingCountry");
                }
                if (dataMap.containsKey("ShippingPostalCode")) {
                    str = (String) dataMap.get("ShippingPostalCode");
                    addrObj.put("shippingPostal", str);
                    dataMap.remove("ShippingPostalCode");
                }
                if (dataMap.containsKey("ShippingPhone")) {
                    str = (String) dataMap.get("ShippingPhone");
                    addrObj.put("shippingPhone", str);
                    dataMap.remove("ShippingPhone");
                }
                if (dataMap.containsKey("ShippingMobile")) {
                    str = (String) dataMap.get("ShippingMobile");
                    addrObj.put("shippingMobile", str);
                    dataMap.remove("ShippingMobile");
                }
                if (dataMap.containsKey("ShippingFax")) {
                    str = (String) dataMap.get("ShippingFax");
                    addrObj.put("shippingFax", str);
                    dataMap.remove("ShippingFax");
                }
                if (dataMap.containsKey("ShippingEmail")) {
                    str = (String) dataMap.get("ShippingEmail");
                    addrObj.put("shippingEmail", str);
                    dataMap.remove("ShippingEmail");
                }
                if (dataMap.containsKey("ShippingContactPerson")) {
                    str = (String) dataMap.get("ShippingContactPerson");
                    addrObj.put("shippingContactPerson", str);
                    dataMap.remove("ShippingContactPerson");
                }
                if (dataMap.containsKey("ShippingContactPersonDesignation")) {
                    str = (String) dataMap.get("ShippingContactPersonDesignation");
                    addrObj.put("shippingContactPersonDesignation", str);
                    dataMap.remove("ShippingContactPersonDesignation");
                } 
                if (dataMap.containsKey("ShippingContactPersonNumber")) {
                    str = (String) dataMap.get("ShippingContactPersonNumber");
                    addrObj.put("shippingContactPersonNumber", str);
                    dataMap.remove("ShippingContactPersonNumber");
                } 
                if (dataMap.containsKey("ShippingRecipientName")) {
                    str = (String) dataMap.get("ShippingRecipientName");
                    addrObj.put("shippingRecipientName", str);
                    dataMap.remove("ShippingRecipientName");
                } 
                if (dataMap.containsKey("ShippingWebsite")) {
                    str = (String) dataMap.get("ShippingWebsite");
                    addrObj.put("shippingWebsite", str);
                    dataMap.remove("ShippingWebsite");
                } 
                if (dataMap.containsKey("ShippingRoute")) {
                    String shippingRoute = (String) dataMap.get("ShippingRoute");
                    if (!StringUtil.isNullOrEmpty(shippingRoute)) {
                        if (pref.equalsIgnoreCase("0") || pref.equalsIgnoreCase("1")) {//0= most restricted case, 1= export with empty data if invalid
                            addrObj.put("shippingRoute", shippingRoute);
                        } else if (pref.equalsIgnoreCase("2")) {//create new and add if not present in system
                            String groupID = String.valueOf(28);
                            KwlReturnObject result = getMasterItemId(companyId, shippingRoute, groupID);
                            if (result.getEntityList().size() > 0 && result.getEntityList().get(0) != null) {
                                addrObj.put("shippingRoute", result.getEntityList().get(0).toString());
                            } else {// If not present then create new 
                                HashMap<String, Object> requestMap = new HashMap<String, Object>();
                                requestMap.put("name", shippingRoute);
                                requestMap.put("groupid", String.valueOf(28));
                                requestMap.put("companyid", companyId);
                                KwlReturnObject masterResult = accMasterItemsDAOobj.addMasterItem(requestMap);
                                MasterItem master = (MasterItem) masterResult.getEntityList().get(0);
                                if (master != null) {
                                    addrObj.put("shippingRoute", master.getID());
                                }
                            }
                        }
                    }
                    dataMap.remove("ShippingRoute");
                }     
             
                arry.put(1, addrObj); //putting object by index, so that I can access it by same index in afterave method   
                arry.put(2, customfield); // putting object by index, so that I can access it by same index in afterave method for custom felds
                arguments[7] = arry;


//                String accountName = null;                
//                if (dataMap.containsKey("Account")) {
//                    accountName = (String) dataMap.get("Account");
//                }

                // As done code analysis : below code under the if case not executing in any case.
                // Account column is madatory to map and the value of account cannot be empty so it is never possible to have Account empty or null
                // So for commenting below code, once it get tested will remove this code
          /*      
                if (StringUtil.isNullOrEmpty(accountName)) {
                    KwlReturnObject accResult = getVendorCustomerDefaultAccounts(companyId);
                    if (mode.equalsIgnoreCase("vendor")) {
                        if (accResult.getEntityList().size() > 0 && accResult.getEntityList().get(0) != null) {
                            List ls = accResult.getEntityList();
                            int i = 1;
                            Iterator<Object[]> itr1 = ls.iterator();
                            while (itr1.hasNext()) {

                                Object[] row = (Object[]) itr1.next();
                                if (row[1] != null && i == 1) {
                                    accountName = row[1].toString();
                                }
                                i++;
                            }
                        }
                    } else if (mode.equalsIgnoreCase("customer")) {
                        if (accResult.getEntityList().size() > 0 && accResult.getEntityList().get(0) != null) {
                            List ls = accResult.getEntityList();
                            Iterator<Object[]> itr1 = ls.iterator();
                            int i = 1;
                            while (itr1.hasNext()) {

                                Object[] row = (Object[]) itr1.next();
                                if (row[0] != null && i == 1) {
                                    accountName = row[0].toString();
                                }
                            }
                            i++;
                        }

                    }
                }*/
//                dataMap.put("Account", accountName);
                
                if (dataMap.containsKey("Title") && !StringUtil.isNullOrEmpty((String) dataMap.get("Title")) && pref.equalsIgnoreCase("2")) {//for pref 0 and 1 already Id is fetched at the time on validating
                    String groupID = String.valueOf(6);
                    String titleName = (String) dataMap.get("Title");
                    KwlReturnObject result = getMasterItemId(companyId, titleName, groupID);
                    if (result.getEntityList().size() > 0 && result.getEntityList().get(0) != null) {
                        dataMap.put("Title", result.getEntityList().get(0).toString());
                    } else {
                        HashMap<String, Object> requestMap = new HashMap<String, Object>();
                        requestMap.put("name", titleName);
                        requestMap.put("groupid", String.valueOf(6));
                        requestMap.put("companyid", companyId);
                        KwlReturnObject masterResult = accMasterItemsDAOobj.addMasterItem(requestMap);
                        MasterItem master = (MasterItem) masterResult.getEntityList().get(0);
                        if (master != null) {
                            dataMap.put("Title", master.getID());
                        }
                    }
                }
                if (dataMap.containsKey("DeducteeType") && mode.equalsIgnoreCase("vendor")) {
                    if (StringUtil.isNullOrEmpty(dataMap.get("DeducteeType").toString())) {
                        dataMap.put("DeducteeCode", "");
                    } else {
                        String deducteeType = dataMap.get("DeducteeType").toString();
                        KwlReturnObject masterResult = accMasterItemsDAOobj.getMasterItem(deducteeType);
                        MasterItem master = (MasterItem) masterResult.getEntityList().get(0);
                        if (master != null) {
                            if (!master.getDefaultMasterItem().getID().equals(IndiaComplianceConstants.DEDUCTEETYPE_UNKNOWN_ID)) {
                                if (master.getCode().equals("0")) {// Corporate
                                    dataMap.put("DeducteeCode", "1");
                                } else {//Non-Corporate
                                    dataMap.put("DeducteeCode", "2");
                                }
                            } else {
                                dataMap.put("DeducteeCode", "");
                            }
                        }
                    }
                }
            } else if (mode.equalsIgnoreCase("Group")) {
                HashMap<String, Object> dataMap = (HashMap<String, Object>) arguments[1];

                int nature = getNatureValue("Nature", dataMap.get("Nature"));
                dataMap.remove("Nature");
                dataMap.put("Nature", nature);
            } else if (mode.equalsIgnoreCase("Accounts")) {
                HashMap<String, Object> dataMap = (HashMap<String, Object>) arguments[1];
                JSONArray arry = (JSONArray) arguments[7];
                boolean isActivateIBG = (boolean) hmap.get("isActivateIBG");
                boolean isIBGDetails = false;
                if (dataMap.containsKey("IBGBank") && dataMap.get("IBGBank") != null) {
                    isIBGDetails = (boolean) dataMap.get("IBGBank");
                }
                
                JSONArray customfield = arry;
                arry = new JSONArray();

                boolean isAccountHavingTransactions = false;

                if (dataMap.containsKey("isAccountHavingTransactions") && dataMap.get("isAccountHavingTransactions") != null) {
                    isAccountHavingTransactions = (Boolean) dataMap.get("isAccountHavingTransactions");
                    dataMap.remove("isAccountHavingTransactions");
                }

                if (!isAccountHavingTransactions) {//if selected account id has transactions then no need to update following data. This flag will be true only if user has selected update existing record option and Account has transactions associated with it.

                    if (!dataMap.containsKey("Currency") || (dataMap.containsKey("Currency") && StringUtil.isNullOrEmpty((String) dataMap.get("Currency")))) { //if import file does not contain currency then puting gcurrency as default currency
                        String gcurrencyid = (String) hmap.get(Constants.globalCurrencyKey);
                        dataMap.put("Currency", gcurrencyid);
                    }
                    
                    if (!dataMap.containsKey("CreationDate") || (dataMap.containsKey("CreationDate") && (dataMap.get("CreationDate")==null || StringUtil.isNullOrEmpty(dataMap.get("CreationDate").toString())) )) {
                        dataMap.put("CreationDate", hmap.get("bookBeginningDate"));
                    }

                    String balanceType = "";
                    String grpname = (String) dataMap.get("Group");
                    String companyid = (String) hmap.get(Constants.companyid);
                    int nature = accountingHandlerDAOobj.getNature(grpname, companyid);
                    String bal = "";
                    if(dataMap.containsKey("orignalOpeningBalance") && dataMap.get("orignalOpeningBalance")!=null){ //SDP-12621
                        bal = dataMap.get("orignalOpeningBalance").toString();                        
                    }
                    boolean isNegative = false;
                    int pos = bal.indexOf("(");
                    int pos1 = bal.indexOf("-");
                    if (pos >= 0 || pos1 >= 0) {
                        isNegative = true;
                    }
                    //  bal=bal.replaceAll("[a-zA-Z]+", "");
                    //  bal = bal.replaceAll(",", "");
                    String action = bal.replaceAll("[^.0-9]", "");
                    double balance = 0.0;

                    balance = StringUtil.getDouble(action);
                    if (isNegative) {
                        balance = -(balance);
                    }
                    if (balance < 0) {
                        if (nature == 1 || nature == 2) {   //NATURE_ASSET = 1,NATURE_EXPENSES=2
                            balanceType = "Credit";
                        } else {
                            balanceType = "Debit";
                        }
                    } else {
                        if (nature == 1 || nature == 2) {   //NATURE_LIABILITY = 0,NATURE_INCOME = 3
                            balanceType = "Debit";
                        } else {
                            balanceType = "Credit";
                        }
                    }

                    if (StringUtil.equal(balanceType, "Credit") && dataMap.containsKey("OpeningBalance") && dataMap.get("OpeningBalance")!=null) {    //In case of credit OpeningBalance will be negative 
                        Object dataValue = "-" + dataMap.get("OpeningBalance");
                        double openBalance = StringUtil.getDouble(dataValue.toString());
                        dataMap.put("OpeningBalance", openBalance);
                    }

                }

                if (dataMap.containsKey("orignalOpeningBalance")) {
                    dataMap.remove("orignalOpeningBalance");
                }
                
                // to set account as activate while creating
                if (!updateExistingRecordFlag) {
                    dataMap.put("Activate", (dataMap.containsKey("Activate") && dataMap.get("Activate")!=null) ? dataMap.get("Activate"):true);
                }
                
                JSONObject ibgObj = new JSONObject();
                if (dataMap.containsKey("IbgbankName")) {
                    int ibgBankVal = dataMap.get("IbgbankName") != null?(Integer) dataMap.get("IbgbankName"):0;
                    ibgObj.put(Constants.IBG_BANK, ibgBankVal);
                    dataMap.put("IbgBankType", ibgBankVal);
                    dataMap.remove("IbgbankName");
                }
                if (dataMap.containsKey("BankCode")) {
                    String bankCodeVal = dataMap.get("BankCode") != null ?dataMap.get("BankCode").toString():"";
                    ibgObj.put(Constants.BANK_CODE, bankCodeVal);
                    dataMap.remove("BankCode");
                }
                if (dataMap.containsKey("BranchCode")) {
                    String branchCodeVal = dataMap.get("BranchCode") != null ?(String) dataMap.get("BranchCode").toString():"";
                    ibgObj.put(Constants.BRANCH_CODE, branchCodeVal);
                    dataMap.remove("BranchCode");
                }
                if (dataMap.containsKey("AccountNumber")) {
                    String accountNumberVal = dataMap.get("AccountNumber") != null ?(String) dataMap.get("AccountNumber").toString():"";
                    ibgObj.put(Constants.ACCOUNT_NUMBER, accountNumberVal);
                    dataMap.remove("AccountNumber");
                }
                if (dataMap.containsKey("AccountName")) {
                    String accountNameVal = dataMap.get("AccountName") != null ?(String) dataMap.get("AccountName"):"";
                    ibgObj.put(Constants.ACCOUNT_NAME, accountNameVal);
                    dataMap.remove("AccountName");
                }
                if (dataMap.containsKey("SendersCompanyID")) {
                    String sendersCompanyIDVal =dataMap.get("SendersCompanyID") != null ? (String) dataMap.get("SendersCompanyID"):"";
                    ibgObj.put(Constants.SENDERS_COMPANYID, sendersCompanyIDVal);
                    dataMap.remove("SendersCompanyID");
                }
                if (dataMap.containsKey("BankDailyLimit")) {
                    double bankDailyLimitVal = (dataMap.get("BankDailyLimit") != null && !StringUtil.isNullOrEmpty(dataMap.get("BankDailyLimit").toString()))? Double.parseDouble(dataMap.get("BankDailyLimit").toString()) : 0;
                    ibgObj.put(Constants.BANK_DAILY_LIMIT, bankDailyLimitVal);
                    dataMap.remove("BankDailyLimit");
                }
                if (dataMap.containsKey("ServiceCode")) {
                    String serviceCode = dataMap.get("ServiceCode") != null ? dataMap.get("ServiceCode").toString() : "";
                    ibgObj.put(Constants.SERVICE_CODE, serviceCode);
                    dataMap.remove("ServiceCode");
                }
                if (dataMap.containsKey("BankAccountNumber")) {
                    String bankAccountNumber = dataMap.get("BankAccountNumber") != null ? dataMap.get("BankAccountNumber").toString() : "";
                    ibgObj.put(Constants.BANK_Account_Number, bankAccountNumber);
                    dataMap.remove("BankAccountNumber");
                }
                if (dataMap.containsKey("OrdererName")) {
                    String ordererName = dataMap.get("OrdererName") != null ? dataMap.get("OrdererName").toString() : "";
                    ibgObj.put(Constants.ORDERER_NAME, ordererName);
                    dataMap.remove("OrdererName");
                }
                if (dataMap.containsKey("CurrencyCode")) {
                    dataMap.remove("CurrencyCode");
                }
                if (isActivateIBG && isIBGDetails && dataMap.containsKey("SettelementMode")) {
                    int settelementMode = (dataMap.get("SettelementMode") != null && !StringUtil.isNullOrEmpty(dataMap.get("SettelementMode").toString()))? Integer.parseInt(dataMap.get("SettelementMode").toString()) : 0;
                    ibgObj.put(Constants.SETTELEMENT_MODE, settelementMode);
                    dataMap.remove("SettelementMode");
                } else {
                    dataMap.remove("SettelementMode");
                }
                if (isActivateIBG && isIBGDetails && dataMap.containsKey("PostingIndicator")) {
                    int postingIndicator = (dataMap.get("PostingIndicator") != null && !StringUtil.isNullOrEmpty(dataMap.get("PostingIndicator").toString()))? Integer.parseInt(dataMap.get("PostingIndicator").toString()) : 0;
                    ibgObj.put(Constants.POSTING_INDICATOR, postingIndicator);
                    dataMap.remove("PostingIndicator");
                } else {
                    dataMap.remove("PostingIndicator");
                    
                }
                if (dataMap.containsKey("Branchstate") && dataMap.get("Branchstate") != null && !StringUtil.isNullOrEmpty(dataMap.get("Branchstate").toString())) {
                    String branchstate = dataMap.get("Branchstate").toString();
                    dataMap.remove("Branchstate");
                    KwlReturnObject result=accMasterItemsDAOobj.getStateIdByName(branchstate);
                    if(result != null && result.getEntityList().size() > 0){
                    State state=(State)result.getEntityList().get(0);
                    dataMap.put("Branchstate",state.getID());
                }
                }
                if (dataMap.containsKey("User")) {
                    String user = (dataMap.get("User") != null && !StringUtil.isNullOrEmpty(dataMap.get("User").toString()))? dataMap.get("User").toString():"";
                    dataMap.remove("User");
                    String companyid = (String) hmap.get(Constants.companyid);
                    KwlReturnObject result=accMasterItemsDAOobj.getUserIdByName(user,companyid);
                    if (result.getEntityList().size() > 0) {
                        User userObj = (User) result.getEntityList().get(0);
                        dataMap.put("User", userObj.getUserID());
                    }
                }                
                                
                arry.put(0, ibgObj); // putting object by index, so that I can access it by same index in afterave method
                arry.put(1, customfield); // putting object by index, so that I can access it by same index in afterave method for custom felds
                arguments[7] = arry;
            } else if (mode.equalsIgnoreCase("Customer Address Details")) { // for updating default address
                HashMap<String, Object> dataMap = (HashMap<String, Object>) arguments[1];
                boolean isDefaultAddress = (Boolean) dataMap.get("IsDefaultAddress");
                boolean isBillingAddress = (Boolean) dataMap.get("IsBillingAddress");
                String customerID = (String) dataMap.get("CustomerID");

                if (isDefaultAddress) {
                    accountingHandlerDAOobj.updateCustomerAddressDefaultValueToFalse(customerID, isBillingAddress);
                }
            }


            /*
             * String pojoClassForsave="com.krawler.hql.accounting.Vendor";
             * if(mode.equalsIgnoreCase("customer")){
             * pojoClassForsave="com.krawler.hql.accounting.Customer"; } //
             * HttpServletRequest request = (HttpServletRequest) arguments[0];
             * HashMap<String, Object> dataMap = (HashMap<String, Object>)
             * arguments[1]; // Object csvReader = arguments[2]; // String
             * classPath = (String) arguments[4]; // String primaryKey =
             * (String) arguments[5]; String parentid=(String)
             * dataMap.get("Parent"); if(StringUtil.isNullOrEmpty(parentid)){
             * KwlReturnObject accResult =
             * getSundryAccount((String)dataMap.get("Company"),
             * mode.equalsIgnoreCase("vendor"));
             * if(accResult.getEntityList().size() > 0 &&
             * accResult.getEntityList().get(0) != null) parentid = (String)
             * accResult.getEntityList().get(0); else parentid=null; }
             * HashMap<String, Object> accountDataMap = new HashMap<String,
             * Object>(); // accountDataMap.put("Life", 10.0); //
             * accountDataMap.put("Salvage", 0.0); //
             * accountDataMap.put("Group",
             * mode.equalsIgnoreCase("customer")?Group.ACCOUNTS_RECEIVABLE:Group.ACCOUNTS_PAYABLE);
             * accountDataMap.put("Name", dataMap.get("Name"));
             * accountDataMap.put("Acccode", dataMap.get("Acccode"));
             * accountDataMap.put("Parent", parentid); //
             * accountDataMap.put("OpeningBalance",
             * dataMap.get("OpeningBalance")==null?0.0:dataMap.get("OpeningBalance"));
             * // accountDataMap.put("DepreciationAccont",
             * dataMap.get("DepreciationAccont")); //
             * accountDataMap.put("Currency", dataMap.get("Currency"));
             *
             * accountDataMap.put("Company", dataMap.get("Company")); //
             * accountDataMap.put("Category", dataMap.get("Category"));
             * accountDataMap.put("CreationDate", dataMap.get("CreatedOn")); if
             * (mode.equalsIgnoreCase("customer")) {
             * accountDataMap.put("Account",
             * "f48c542543956e5401439580cd20002b"); Customer customer =
             * (Customer) objectSetterMethod(
             * accountDataMap, pojoClassForsave, "ID"); } else {
             * accountDataMap.put("Account",
             * "f48c542543956e5401439580cd20002b"); Vendor vendor = (Vendor)
             * objectSetterMethod(
             * accountDataMap, pojoClassForsave, "ID"); }
             *
             * // Add new account to customer/vendor // dataMap.put("Account",
             * account.getID());
             *
             * // Remove Account related data from datamap
             * dataMap.remove("Parent"); dataMap.remove("DepreciationAccont");
             * dataMap.remove("Currency"); dataMap.remove("OpeningBalance");
             * dataMap.remove("Category"); }
             */
        } catch (Exception ex) {
            Logger.getLogger(ImportRecordAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            throw new DataInvalidateException("Failed to create account for " + mode + ": " + ex.getMessage());
        }

        return proceed;
    }

    private int getNatureValue(String datakey, Object dataValue) throws Exception {
        int natureValue = 0;
        if (dataValue.toString().equalsIgnoreCase("Liability")) {
            natureValue = 0;
        } else if (dataValue.toString().equalsIgnoreCase("Asset")) {
            natureValue = 1;
        } else if (dataValue.toString().equalsIgnoreCase("Expenses")) {
            natureValue = 2;
        } else if (dataValue.toString().equalsIgnoreCase("Income")) {
            natureValue = 3;
        } else {
            throw new Exception("Ambiguous value found for column " + datakey);
        }
        return natureValue;
    }
    
    private void AfterSaveRecord(MethodInvocation mi, Object result) throws DataInvalidateException, Throwable {
        Object arguments[] = mi.getArguments();
        String mode = (String) arguments[3];//Used for module name
        HashMap<String, Object> hmap = (HashMap<String, Object>) arguments[0];
        String pref = (String) hmap.get("masterPreference");
        boolean updateExistingRecordFlag = false;
        if (hmap.containsKey("updateExistingRecordFlag") && hmap.get("updateExistingRecordFlag") != null) {
            updateExistingRecordFlag = (Boolean) hmap.get("updateExistingRecordFlag");
        }
        try {
            if (mode.equalsIgnoreCase("customer") || mode.equalsIgnoreCase("vendor")) {

                HashMap<String, Object> dataMap = (HashMap<String, Object>) arguments[1];
                String companyId = (String) dataMap.get("Company");
                JSONArray arry = (JSONArray) arguments[7];
                JSONObject obj = arry.getJSONObject(0);
                JSONObject addrObj = arry.getJSONObject(1);
                boolean isProductExist = false, isCategoryExist = false,isSalesPersonExist = false,isAgentExist=false,isCustomerVendorAlreadyExist=false;
                if (updateExistingRecordFlag && dataMap.containsKey("ID") && dataMap.get("ID") != null && !StringUtil.isNullOrEmpty(dataMap.get("ID").toString())) {// if update Existing Record Checkbox is true and the record is exist already in db(only in this case dataMap will contain primary Key Value i.e "id" value).
                   isCustomerVendorAlreadyExist=true; 
                }
                String productCode = "", categoryName = "",salesPersonName="",agentName="";
                String[] productMapping = StringUtil.isNullOrEmpty(obj.optString("Products", ""))?new String [0]:obj.optString("Products", "").split(",");
                String[] categoryMapping = StringUtil.isNullOrEmpty(obj.optString("Category", ""))?new String [0]:obj.optString("Category", "").split(",");
                String[] salesPersonMapping = StringUtil.isNullOrEmpty(obj.optString("SalesPerson", ""))?new String [0]:obj.optString("SalesPerson", "").split(",");
                String[] agentVendorMapping = StringUtil.isNullOrEmpty(obj.optString("Agent", ""))?new String [0]:obj.optString("Agent", "").split(",");
                HashMap<String, Object> billingAddrMap = new HashMap<String, Object>();
                HashMap<String, Object> shippingAddrMap = new HashMap<String, Object>();               
                
                String billingAliasName = StringUtil.isNullOrEmpty(addrObj.optString("billingAliasName", "Billing Address1").trim()) ? "Billing Address1" : addrObj.optString("billingAliasName", "Billing Address1");
                boolean isBillingDefaultAddress = true;
                if (isCustomerVendorAlreadyExist) { //If customer or vendor already exist then we need to update address. 
                    //We are updating those address for which alias name maches. 
                    //If alias name not matches then we adding those address instead of update
                    HashMap<String, Object> addressDetailsMap = new HashMap<String, Object>();
                    boolean isBillingAddress = true;
                    String addressid = "";
                    String customerVendorID = (String) dataMap.get("ID");
                    if (mode.equalsIgnoreCase("customer")) {
                        addressDetailsMap = getExistingCustomerAddressDetailsInfo(billingAliasName, isBillingAddress, customerVendorID, companyId);
                    } else if (mode.equalsIgnoreCase("vendor")) {
                        addressDetailsMap = getExistingVendorAddressDetailsInfo(billingAliasName, isBillingAddress, customerVendorID, companyId);
                    }
                    isBillingDefaultAddress = addressDetailsMap.containsKey("isDefault") ? (Boolean) addressDetailsMap.get("isDefault") : false;
                    addressid = addressDetailsMap.containsKey("addressid") ? (String) addressDetailsMap.get("addressid") : "";
                    if (!StringUtil.isNullOrEmpty(addressid)) {
                        billingAddrMap.put("addressid", addressid);
                    }
                }
                billingAddrMap.put("aliasName", billingAliasName);
                if (addrObj.has("billingAddress")) {
                    billingAddrMap.put("address", addrObj.optString("billingAddress", ""));
                }
                if (addrObj.has("billingCounty")) {
                    billingAddrMap.put("county", addrObj.optString("billingCounty", ""));
                }
                if (addrObj.has("billingCity")) {
                    billingAddrMap.put("city", addrObj.optString("billingCity", ""));
                }
                if (addrObj.has("billingState")) {
                    billingAddrMap.put("state", addrObj.optString("billingState", ""));
                }
                if (addrObj.has("billingCountry")) {
                    billingAddrMap.put("country", addrObj.optString("billingCountry", ""));
                }
                if (addrObj.has("billingPostal")) {
                    billingAddrMap.put("postalCode", addrObj.optString("billingPostal", ""));
                }
                if (addrObj.has("billingPhone")) {
                    billingAddrMap.put("phone", addrObj.optString("billingPhone", ""));
                }
                if (addrObj.has("billingMobile")) {
                    billingAddrMap.put("mobileNumber", addrObj.optString("billingMobile", ""));
                }
                if (addrObj.has("billingFax")) {
                    billingAddrMap.put("fax", addrObj.optString("billingFax", ""));
                }
                if (addrObj.has("billingEmail")) {
                    billingAddrMap.put("emailID", addrObj.optString("billingEmail", ""));
                }
                if (addrObj.has("billingContactPerson")) {
                    billingAddrMap.put("contactPerson", addrObj.optString("billingContactPerson", ""));
                }
                if (addrObj.has("billingContactPersonNumber")) {
                    billingAddrMap.put("contactPersonNumber", addrObj.optString("billingContactPersonNumber", ""));
                }
                if (addrObj.has("billingContactPersonDesignation")) {
                    billingAddrMap.put("contactPersonDesignation", addrObj.optString("billingContactPersonDesignation", ""));
                }
                if (addrObj.has("billingWebsite")) {
                    billingAddrMap.put("website", addrObj.optString("billingWebsite", ""));
                }
                if (addrObj.has("billingRecipientName")) {
                    billingAddrMap.put("recipientName", addrObj.optString("billingRecipientName", ""));
                }
                billingAddrMap.put("isBillingAddress", true);
                billingAddrMap.put("isDefaultAddress", isBillingDefaultAddress);

                String shippingAliasName = StringUtil.isNullOrEmpty(addrObj.optString("shippingAliasName", "Shipping Address1").trim()) ? "Shipping Address1" : addrObj.optString("shippingAliasName", "Shipping Address1");
                boolean isShippingDefaultAddress = true;
                if (isCustomerVendorAlreadyExist) { //If customer or vendor already exist then we need to update address. 
                    //We are updating those address for which alias name maches. 
                    //If alias name not matches then we adding those address instead of update
                    HashMap<String, Object> addressDetailsMap = new HashMap<String, Object>();
                    boolean isBillingAddress = false;
                    String addressid = "";
                    String customerVendorID = (String) dataMap.get("ID");
                    if (mode.equalsIgnoreCase("customer")) {
                        addressDetailsMap = getExistingCustomerAddressDetailsInfo(shippingAliasName, isBillingAddress, customerVendorID, companyId);
                    } else if (mode.equalsIgnoreCase("vendor")) {
                        addressDetailsMap = getExistingVendorAddressDetailsInfo(shippingAliasName, isBillingAddress, customerVendorID, companyId);
                    }
                    isShippingDefaultAddress = addressDetailsMap.containsKey("isDefault") ? (Boolean) addressDetailsMap.get("isDefault") : false;
                    addressid = addressDetailsMap.containsKey("addressid") ? (String) addressDetailsMap.get("addressid") : "";
                    if (!StringUtil.isNullOrEmpty(addressid)) {
                        shippingAddrMap.put("addressid", addressid);
                    }
                }
                
                shippingAddrMap.put("aliasName", shippingAliasName);
                if (addrObj.has("shippingAddress")) {
                    shippingAddrMap.put("address", addrObj.optString("shippingAddress", ""));
                }
                if (addrObj.has("shippingCounty")) {
                    shippingAddrMap.put("county", addrObj.optString("shippingCounty", ""));
                }
                if (addrObj.has("shippingCity")) {
                    shippingAddrMap.put("city", addrObj.optString("shippingCity", ""));
                }
                if (addrObj.has("shippingState")) {
                    shippingAddrMap.put("state", addrObj.optString("shippingState", ""));
                }
                if (addrObj.has("shippingCountry")) {
                    shippingAddrMap.put("country", addrObj.optString("shippingCountry", ""));
                }
                if (addrObj.has("shippingPostal")) {
                    shippingAddrMap.put("postalCode", addrObj.optString("shippingPostal", ""));
                }
                if (addrObj.has("shippingPhone")) {
                    shippingAddrMap.put("phone", addrObj.optString("shippingPhone", ""));
                }
                if (addrObj.has("shippingMobile")) {
                    shippingAddrMap.put("mobileNumber", addrObj.optString("shippingMobile", ""));
                }
                if (addrObj.has("shippingFax")) {
                    shippingAddrMap.put("fax", addrObj.optString("shippingFax", ""));
                }
                if (addrObj.has("shippingEmail")) {
                    shippingAddrMap.put("emailID", addrObj.optString("shippingEmail", ""));
                }
                if (addrObj.has("shippingContactPerson")) {
                    shippingAddrMap.put("contactPerson", addrObj.optString("shippingContactPerson", ""));
                }
                if (addrObj.has("shippingContactPersonNumber")) {
                    shippingAddrMap.put("contactPersonNumber", addrObj.optString("shippingContactPersonNumber", ""));
                }
                if (addrObj.has("shippingContactPersonDesignation")) {
                    shippingAddrMap.put("contactPersonDesignation", addrObj.optString("shippingContactPersonDesignation", ""));
                }
                if (addrObj.has("shippingWebsite")) {
                    shippingAddrMap.put("website", addrObj.optString("shippingWebsite", ""));
                }
                if (addrObj.has("shippingRecipientName")) {
                    shippingAddrMap.put("recipientName", addrObj.optString("shippingRecipientName", ""));
                }
                if (addrObj.has("shippingRoute")) {
                    shippingAddrMap.put("shippingRoute", addrObj.optString("shippingRoute", ""));
                }
                shippingAddrMap.put("isBillingAddress", false);
                shippingAddrMap.put("isDefaultAddress", isShippingDefaultAddress);

                if (mode.equalsIgnoreCase("customer")) {
                    JSONArray customfield = arry.getJSONArray(2);
                    Customer customer = (Customer) result;
                    if (obj.has("Products") && productMapping.length > 0) {
                        //below code used to mapping preferred product to customer
                        for (int j = 0; j < productMapping.length; j++) {
                            String productID = productMapping[j];
                            accVendorCustomerProductDAOobj.saveCustomerProductMapping(customer.getID(), productID,"");
                        }
                    }
                    if (obj.has("Category")) {
                        if (categoryMapping.length>0) {//below code used to mapping Category customer
                            accCustomerDAOobj.deleteCustomerCategoryMappingDtails(customer.getID());
                            for (int j = 0; j < categoryMapping.length; j++) {
                                String category = categoryMapping[j];//for pref 0 and 1 it will be id and for 2 it will be name
                                
                                if(pref.equalsIgnoreCase("0") || pref.equalsIgnoreCase("1")){
                                    accCustomerDAOobj.saveCustomerCategoryMapping(customer.getID(), category);
                                } else if(pref.equalsIgnoreCase("2")){//if not present then create new and then export 
                                    String groupid = String.valueOf(7); //7 is groupid for Customercategory in mastergroup table     
                                    KwlReturnObject returnObject = accCustomerDAOobj.getCategorytByName(companyId, category, groupid);
                                    if (returnObject.getEntityList().size() > 0) {
                                        MasterItem master = (MasterItem) returnObject.getEntityList().get(0);
                                        accCustomerDAOobj.saveCustomerCategoryMapping(customer.getID(), master.getID());
                                    } else {
                                        HashMap<String, Object> requestMap = new HashMap<String, Object>();
                                        requestMap.put("name", category);
                                        requestMap.put("groupid", groupid);
                                        requestMap.put("companyid", companyId);
                                        KwlReturnObject masterResult = accMasterItemsDAOobj.addMasterItem(requestMap);
                                        MasterItem master = (MasterItem) masterResult.getEntityList().get(0);
                                        if (master != null) {
                                            accCustomerDAOobj.saveCustomerCategoryMapping(customer.getID(), master.getID());
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (obj.has("SalesPerson")) {
                        if (salesPersonMapping.length>0) {//below code used to mapping  customer Sales Person
                            accCustomerDAOobj.deleteSalesPersonMappingDtails(customer.getID());//For add/edit previous mappings are deleted
                            for (int j = 0; j < salesPersonMapping.length; j++) {
                                String salesPersonID = salesPersonMapping[j];
                                accCustomerDAOobj.saveSalesPersonMapping(customer.getID(), salesPersonID);
                            }
                        }
                    }
                    KwlReturnObject custAddrobject = null;
                    if(billingAddrMap.containsKey("address") || billingAddrMap.containsKey("addressid")){
                        billingAddrMap.put("customerid", customer.getID());
                        custAddrobject = accountingHandlerDAOobj.saveCustomerAddressesDetails(billingAddrMap, companyId);
                    }
                    if(shippingAddrMap.containsKey("address") || shippingAddrMap.containsKey("addressid")){
                        shippingAddrMap.put("customerid", customer.getID());
                        custAddrobject = accountingHandlerDAOobj.saveCustomerAddressesDetails(shippingAddrMap, companyId);
                    }

                    if(dataMap.containsKey("Mapcustomervendor") && dataMap.get("Mapcustomervendor")!=null){
                        boolean mapCustomerVendor = Boolean.parseBoolean(dataMap.get("Mapcustomervendor").toString());
                        if(mapCustomerVendor){
                            if(obj.has("VendorAcccode")){
                            dataMap.put("Acccode", obj.opt("VendorAcccode"));
                        }
                            if(obj.has("VendorAccount")){
                            dataMap.put("Account", obj.opt("VendorAccount"));
                        }
                            if(obj.has("ManufacturerType")){
                            dataMap.put("ManufacturerType", obj.opt("ManufacturerType"));
                        }
                            dataMap.remove("ID"); // ERP-36641 & ERP-38020 Remove and Replace Customer ID with Vendor ID - to check and update/create vendor from customer
                            if(obj.has("VendorID")){
                                dataMap.put("ID", obj.opt("VendorID"));
                            }
                            arguments[3]="Vendor";
                            arguments[4]=Vendor.class.getName();
                        dataMap.remove("Parent");
                        dataMap.remove("PricingBandMaster");
                        dataMap.remove("MappingSalesPerson");
                        dataMap.remove("TaxIDNumber");
                        dataMap.remove("Taxid");
                        dataMap.remove("TaxNo");
                        if (dataMap.containsKey("CreditTerm") && dataMap.get("CreditTerm") != null) {
                            dataMap.put("DebitTerm", dataMap.get("CreditTerm"));
                            dataMap.remove("CreditTerm");
                        }
                        if (dataMap.containsKey("Creditlimit") && dataMap.get("Creditlimit") != null) {
                            dataMap.put("Debitlimit", dataMap.get("Creditlimit"));
                            dataMap.remove("Creditlimit");
                        }
                        if (dataMap.containsKey("IsCusotmerAvailableOnlyToSalespersons") && dataMap.get("IsCusotmerAvailableOnlyToSalespersons") != null) {
                            dataMap.remove("IsCusotmerAvailableOnlyToSalespersons");
                        }
                            Object object= mi.proceed();
                            Vendor vendor = (Vendor)object;
                        JSONObject jobjaccount = new JSONObject();
                        jobjaccount.put("customeraccountid", customer.getID());
                        jobjaccount.put("vendoraccountid", vendor.getID());
                        jobjaccount.put("mappingflag", true);
                        KwlReturnObject resultCustVendMap = accCusVenMapDAOObj.saveUpdateCustomerVendorMapping(jobjaccount);
                        KwlReturnObject venAddrobject = null;
                        if(billingAddrMap.containsKey("address") || billingAddrMap.containsKey("addressid")){
                            billingAddrMap.put("vendorid", vendor.getID());
                            venAddrobject = accountingHandlerDAOobj.saveVendorAddressesDetails(billingAddrMap, companyId);
                        }
                        if(shippingAddrMap.containsKey("address") || shippingAddrMap.containsKey("addressid")){
                            shippingAddrMap.put("vendorid", vendor.getID());
                            venAddrobject = accountingHandlerDAOobj.saveVendorAddressesDetails(shippingAddrMap, companyId);
                        }
                    }
                    }
                    if (customfield.length() > 0 ) {
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", customfield);
                        customrequestParams.put("modulename", Constants.Acc_Customer_modulename);
                        customrequestParams.put("moduleprimarykey", Constants.Acc_CustomerId);
                        customrequestParams.put("modulerecid", customer.getID());
                        customrequestParams.put("moduleid", Constants.Acc_Customer_ModuleId);
                        customrequestParams.put("companyid", companyId);
                        customrequestParams.put("customdataclasspath", Constants.Acc_Customer_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManager.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            HashMap<String, Object> requestParams = new HashMap<String, Object>();
                            requestParams.put("acccustomercustomdataref", customer.getID());
                            requestParams.put("accid", customer.getID());
                            KwlReturnObject accresult = accCustomerDAOobj.updateCustomer(requestParams);;
                        }
                }
                    /**
                     * Save Customer GST history for India.
                     */
                    if (customer.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                        Map<String, Object> requestParams = (Map)arguments[0];
                        Date applyDate = null;
                        
                        if (requestParams.containsKey("ApplyDate") && requestParams.get("ApplyDate") != null) {
                            applyDate= (Date) requestParams.get("ApplyDate");
                        } else {
                            applyDate = customer.getCreatedOn();
                        }
                        
                        requestParams.put("applyDate", applyDate);
                        requestParams.put("customerid", customer.getID());
                        requestParams.put("GSTINRegistrationTypeId", customer.getGSTRegistrationType() == null ? "" : customer.getGSTRegistrationType().getID());
                        requestParams.put("CustomerVendorTypeId", customer.getGSTCustomerType() == null ? "" : customer.getGSTCustomerType().getID());
                        requestParams.put("gstin", customer.getGSTIN());
                        List histList = accCustomerDAOobj.getGstCustomerHistory(requestParams);
                        if (!histList.isEmpty() && histList.get(0) != null) {
                            /**
                             * If history present for input date then need to
                             * update it.
                             */
                            requestParams.put("gstcustomerhistoryid", (String) histList.get(0));
                        }
                        /**
                         * Save Customer GST history Audit Trail entry
                         */
                        if (arguments != null && arguments[0] != null) {
                            HashMap<String, Object> reqParamsDetails = (HashMap<String, Object>) arguments[0];
                            JSONObject paramJObj = new JSONObject();
                            paramJObj.put(Constants.customerid, customer.getID());
                            paramJObj.put(Constants.customerName, customer.getName());
                            DateFormat df = authHandler.getDateOnlyFormat();
                            paramJObj.put("gstapplieddate", df.format(applyDate));
                            paramJObj.put("GSTINRegistrationTypeId", customer.getGSTRegistrationType() == null ? "" : customer.getGSTRegistrationType().getID());
                            paramJObj.put("CustomerVendorTypeId", customer.getGSTCustomerType() == null ? "" : customer.getGSTCustomerType().getID());
                            paramJObj.put("gstin", customer.getGSTIN());
                            if(reqParamsDetails.containsKey(Constants.reqHeader) && reqParamsDetails.get(Constants.reqHeader)!=null){
                                paramJObj.put(Constants.reqHeader,reqParamsDetails.get(Constants.reqHeader).toString());
                            }
                            if(reqParamsDetails.containsKey(Constants.remoteIPAddress) && reqParamsDetails.get(Constants.remoteIPAddress)!=null){
                                paramJObj.put(Constants.remoteIPAddress,reqParamsDetails.get(Constants.remoteIPAddress).toString());
                            }
                            if(reqParamsDetails.containsKey(Constants.useridKey) && reqParamsDetails.get(Constants.useridKey)!=null){
                                paramJObj.put(Constants.useridKey,reqParamsDetails.get(Constants.useridKey).toString());
                            }
                            if(reqParamsDetails.containsKey(Constants.userfullname) && reqParamsDetails.get(Constants.userfullname)!=null){
                                paramJObj.put(Constants.userfullname,reqParamsDetails.get(Constants.userfullname).toString());
                            }
                            accCustomerControllerCMNServiceObj.saveCustomerGSTHistoryAuditTrail(paramJObj);
                        }
                        
                        accCustomerDAOobj.saveGstCustomerHistory(requestParams);
                    }
                    // Added Customer Default Account in usedin field for customer account.
                    if (customer != null && customer.getAccount() != null) {
                        Account account = (Account) get(Account.class, customer.getAccount().getID());
                        if (account != null) {
                            String usedin = StringUtil.isNullOrEmpty(account.getUsedIn()) ? "" : account.getUsedIn();
                            account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Customer_Default_Account));
                        }
                    }
                } else if (mode.equalsIgnoreCase("vendor")) {
                    JSONArray customfield = arry.getJSONArray(2);
                    Vendor vendor = (Vendor) result;
                    if (obj.has("Products") && productMapping.length>0) {
                        for (int j = 0; j < productMapping.length; j++) {
                            String productID = productMapping[j];
                            accVendorCustomerProductDAOobj.saveVendorProductMapping(vendor.getID(), productID,"");
                        }
                    }
                    if (obj.has("Category")) {
                        if (categoryMapping.length>0) {//below code used to mapping Vendor Category 
                            for (int j = 0; j < categoryMapping.length; j++) {
                                String category = categoryMapping[j]; //for pref 0 and 1 it will be id and for 2 it will be name
                                if (pref.equalsIgnoreCase("0") || pref.equalsIgnoreCase("1")) {
                                    accVendorDAOobj.saveVendorCategoryMapping(vendor.getID(), category);
                                } else if (pref.equalsIgnoreCase("2")) {//when preference value is 2 then add new entry option is selected
                                    String groupid = String.valueOf(8); //8 is groupid for Vendor category in mastergroup table 
                                    KwlReturnObject returnObject = accCustomerDAOobj.getCategorytByName(companyId, category, groupid);
                                    if (returnObject.getEntityList().size() > 0) {
                                        MasterItem master = (MasterItem) returnObject.getEntityList().get(0);
                                        accVendorDAOobj.saveVendorCategoryMapping(vendor.getID(), master.getID());
                                    } else {
                                    HashMap<String, Object> requestMap = new HashMap<String, Object>();
                                    requestMap.put("name", category);
                                    requestMap.put("groupid", groupid);
                                    requestMap.put("companyid", companyId);
                                    KwlReturnObject masterResult = accMasterItemsDAOobj.addMasterItem(requestMap);
                                    MasterItem master = (MasterItem) masterResult.getEntityList().get(0);
                                    if (master != null) {
                                        accVendorDAOobj.saveVendorCategoryMapping(vendor.getID(), master.getID());
                                    }
                                }
                            }
                        }
                    }
                    }
                    
                    if (obj.has("Agent")) {
                        if (agentVendorMapping.length>0) {//below code used to mapping  agent and vendor
                            int numRows=accVendorDAOobj.deleteVendorAgentMapping(vendor.getID());//For add/edit previous mappings are deleted
                            accVendorDAOobj.saveVendorAgentMapping(vendor, agentVendorMapping);
                        }
                    }
                    
                    KwlReturnObject custAddrobject = null;
                    if(billingAddrMap.containsKey("address") || billingAddrMap.containsKey("addressid")){
                        billingAddrMap.put("vendorid", vendor.getID());
                        custAddrobject = accountingHandlerDAOobj.saveVendorAddressesDetails(billingAddrMap, companyId);
                    }
                    if(shippingAddrMap.containsKey("address") || shippingAddrMap.containsKey("addressid")){
                        shippingAddrMap.put("vendorid", vendor.getID());
                        custAddrobject = accountingHandlerDAOobj.saveVendorAddressesDetails(shippingAddrMap, companyId);
                    }

                    if(dataMap.containsKey("Mapcustomervendor") && dataMap.get("Mapcustomervendor")!=null){
                        boolean mapCustomerVendor = Boolean.parseBoolean(dataMap.get("Mapcustomervendor").toString());
                        if(mapCustomerVendor){
                            if(obj.has("CustomerAcccode")){
                            dataMap.put("Acccode", obj.opt("CustomerAcccode"));
                        }
                            if(obj.has("CustomerAccount")){
                            dataMap.put("Account", obj.opt("CustomerAccount"));
                        }
                            dataMap.remove("ID"); // ERP-36626 & ERP-37984 Remove and Replace Vendor ID with Customer ID - to check and update/create customer from vendor
                            if(obj.has("CustomerID")){
                                dataMap.put("ID", obj.opt("CustomerID"));
                            }
                            arguments[3]="Customer";
                            arguments[4]=Customer.class.getName();
                        dataMap.remove("Parent");
                        dataMap.remove("PricingBandMaster");
                        dataMap.remove("MappingSalesPerson");
                        dataMap.remove("TaxIDNumber");
                        dataMap.remove("Taxid");
                        dataMap.remove("TaxNo");
                        dataMap.remove("ManufacturerType");
                        if (dataMap.containsKey("DebitTerm") && dataMap.get("DebitTerm") != null) {
                            dataMap.put("CreditTerm", dataMap.get("DebitTerm"));
                            dataMap.remove("DebitTerm");
                        }
                        if (dataMap.containsKey("Debitlimit") && dataMap.get("Debitlimit") != null) {
                            dataMap.put("Creditlimit", dataMap.get("Debitlimit"));
                            dataMap.remove("Debitlimit");
                        }
                            Object object= mi.proceed();
                            Customer customer = (Customer)object;
                        JSONObject jobjaccount = new JSONObject();
                        jobjaccount.put("customeraccountid", customer.getID());
                        jobjaccount.put("vendoraccountid", vendor.getID());
                        jobjaccount.put("mappingflag", true);
                        KwlReturnObject resultCustVendMap = accCusVenMapDAOObj.saveUpdateCustomerVendorMapping(jobjaccount);
                        if(billingAddrMap.containsKey("address") || billingAddrMap.containsKey("addressid")){
                            billingAddrMap.put("customerid", customer.getID());
                            custAddrobject = accountingHandlerDAOobj.saveCustomerAddressesDetails(billingAddrMap, companyId);
                        }
                        if(shippingAddrMap.containsKey("address") || shippingAddrMap.containsKey("addressid")){
                            shippingAddrMap.put("customerid", customer.getID());
                            custAddrobject = accountingHandlerDAOobj.saveCustomerAddressesDetails(shippingAddrMap, companyId);
                        }
                    }
                    }
                    if (customfield.length() > 0) {
                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        customrequestParams.put("customarray", customfield);
                        customrequestParams.put("modulename", Constants.Acc_Vendor_modulename);
                        customrequestParams.put("moduleprimarykey", Constants.Acc_VendorId);
                        customrequestParams.put("modulerecid", vendor.getID());
                        customrequestParams.put("moduleid", Constants.Acc_Vendor_ModuleId);
                        customrequestParams.put("companyid", companyId);
                        customrequestParams.put("customdataclasspath", Constants.Acc_Vendor_custom_data_classpath);
                        KwlReturnObject customDataresult = fieldDataManager.setCustomData(customrequestParams);
                        if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                            HashMap<String, Object> requestParams = new HashMap<String, Object>();
                            requestParams.put("accid", vendor.getID());
                            requestParams.put("accvendorcustomdataref", vendor.getID());
                            KwlReturnObject accresult = accVendorDAOobj.updateVendor(requestParams);
                }
                    }
                    /**
                     * Save Vendor GST history.
                     */
                    if (vendor.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                        Date applyDate = vendor.getCreatedOn();
                        Map<String, Object> requestParams = (Map)arguments[0];
                        Date applydate = null;
                        
                        if (requestParams.containsKey("ApplyDate") && requestParams.get("ApplyDate") != null) {
                            applydate= (Date) requestParams.get("ApplyDate");
                        } else {
                            applydate = vendor.getCreatedOn();
                        }                       
                        requestParams.put("applyDate", applydate);
                        requestParams.put("vendorid", vendor.getID());
                        requestParams.put("GSTINRegistrationTypeId", vendor.getGSTRegistrationType() != null ? vendor.getGSTRegistrationType().getID() : "");
                        requestParams.put("CustomerVendorTypeId", vendor.getGSTVendorType() != null ? vendor.getGSTVendorType().getID() : "");
                        requestParams.put("gstin", vendor.getGSTIN());
                        List histList = accVendorDAOobj.getGstVendorHistory(requestParams);
                        if (!histList.isEmpty() && histList.get(0) != null) {
                            /**
                             * Need to update history for same date.
                             */
                            requestParams.put("gstvendorhistoryid", (String) histList.get(0));
                        }
                        /**
                         * Save Vendor GST history Audit Trail
                         */
                        if (arguments != null && arguments[0] != null) {
                            HashMap<String, Object> reqParamsDetails = (HashMap<String, Object>) arguments[0];
                            JSONObject paramJObj = new JSONObject();
                            paramJObj.put(Constants.vendorid, vendor.getID());
                            paramJObj.put(Constants.VendorName, vendor.getName());
                            DateFormat df = authHandler.getDateOnlyFormat();
                            paramJObj.put("gstapplieddate", df.format(applyDate));
                            paramJObj.put("GSTINRegistrationTypeId", vendor.getGSTRegistrationType() != null ? vendor.getGSTRegistrationType().getID() : "");
                            paramJObj.put("CustomerVendorTypeId", vendor.getGSTVendorType() != null ? vendor.getGSTVendorType().getID() : "");
                            paramJObj.put("gstin", vendor.getGSTIN());
                            if (reqParamsDetails.containsKey(Constants.reqHeader) && reqParamsDetails.get(Constants.reqHeader) != null) {
                                paramJObj.put(Constants.reqHeader, reqParamsDetails.get(Constants.reqHeader).toString());
                            }
                            if (reqParamsDetails.containsKey(Constants.remoteIPAddress) && reqParamsDetails.get(Constants.remoteIPAddress) != null) {
                                paramJObj.put(Constants.remoteIPAddress, reqParamsDetails.get(Constants.remoteIPAddress).toString());
                            }
                            if (reqParamsDetails.containsKey(Constants.useridKey) && reqParamsDetails.get(Constants.useridKey) != null) {
                                paramJObj.put(Constants.useridKey, reqParamsDetails.get(Constants.useridKey).toString());
                            }
                            if (reqParamsDetails.containsKey(Constants.userfullname) && reqParamsDetails.get(Constants.userfullname) != null) {
                                paramJObj.put(Constants.userfullname, reqParamsDetails.get(Constants.userfullname).toString());
                            }
                            accVendorcontrollerCMNService.saveVendorGSTHistoryAuditTrail(paramJObj);
                        }
                        
                        accVendorDAOobj.saveGstVendorHistory(requestParams);
                    }
                    // Added Vendor Default Account in usedin field for vendor account.
                    if (vendor != null && vendor.getAccount() != null) {
                        Account account = (Account) get(Account.class, vendor.getAccount().getID());
                        if (account != null) {
                            String usedin = StringUtil.isNullOrEmpty(account.getUsedIn()) ? "" : account.getUsedIn();
                            account.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.Vendor_Default_Account));
                        }
                    }
                }
            }
            
            if (mode.equalsIgnoreCase("Accounts")) {
                HashMap<String, Object> dataMap = (HashMap<String, Object>) arguments[1];
                JSONArray arry = (JSONArray) arguments[7];
                JSONObject obj = arry.getJSONObject(0);
                JSONArray customfield = arry.getJSONArray(1);
                String companyId = (String) dataMap.get("Company");
                Account account = (Account) result;

                if (account.isIBGBank()) {//If IBG is not true then no need to save below data. These data required only when IBG option is true in file. 
                    int ibgBankType = obj.optInt(Constants.IBG_BANK, 0);
                    if (ibgBankType == Constants.DBS_BANK_Type) {  // DBS bank
                        HashMap<String, Object> ibgBankDetailParams = new HashMap<String, Object>();
                        ibgBankDetailParams.put(Constants.IBG_BANK, ibgBankType);
                        ibgBankDetailParams.put(Constants.BANK_CODE, obj.optString(Constants.BANK_CODE, ""));
                        ibgBankDetailParams.put(Constants.BRANCH_CODE, obj.optString(Constants.BRANCH_CODE, ""));
                        ibgBankDetailParams.put(Constants.ACCOUNT_NUMBER, obj.optString(Constants.ACCOUNT_NUMBER, ""));
                        ibgBankDetailParams.put(Constants.ACCOUNT_NAME, obj.optString(Constants.ACCOUNT_NAME, ""));
                        ibgBankDetailParams.put(Constants.SENDERS_COMPANYID, obj.optString(Constants.SENDERS_COMPANYID, ""));
                        ibgBankDetailParams.put(Constants.BANK_DAILY_LIMIT, obj.optDouble(Constants.BANK_DAILY_LIMIT, 0));
                        ibgBankDetailParams.put(Constants.IBG_BANK_DETAIL_ID, "");
                        ibgBankDetailParams.put(Constants.Acc_Accountid, account.getID());
                        ibgBankDetailParams.put(Constants.companyid, companyId);

                        KwlReturnObject ibgDetailResult = accAccountDAOobj.getIBGDetailsForAccount(account.getID(), companyId);
                        if (!ibgDetailResult.getEntityList().isEmpty() && ibgDetailResult.getEntityList().get(0) != null) {
                            IBGBankDetails ibgDetails = (IBGBankDetails) ibgDetailResult.getEntityList().get(0);
                            ibgBankDetailParams.put(Constants.IBG_BANK_DETAIL_ID, ibgDetails.getID());
                        }

                        accAccountDAOobj.saveOrupdateIBGBankDetail(ibgBankDetailParams);
                    } else if (ibgBankType == Constants.CIMB_BANK_Type) {
                        HashMap<String, Object> cimbBankDetailParams = new HashMap<String, Object>();
                        cimbBankDetailParams.put(Constants.SERVICE_CODE, obj.optString(Constants.SERVICE_CODE, ""));
                        cimbBankDetailParams.put(Constants.BANK_Account_Number, obj.optString(Constants.BANK_Account_Number, ""));
                        cimbBankDetailParams.put(Constants.ORDERER_NAME, obj.optString(Constants.ORDERER_NAME, ""));
                        cimbBankDetailParams.put(Constants.SETTELEMENT_MODE, obj.optInt(Constants.SETTELEMENT_MODE, 1));
                        cimbBankDetailParams.put(Constants.POSTING_INDICATOR, obj.optInt(Constants.POSTING_INDICATOR, 1));
                        cimbBankDetailParams.put(Constants.Acc_Accountid, account.getID());
                        cimbBankDetailParams.put(Constants.companyid, companyId);
                        String cimbDetailID = "";
                        KwlReturnObject ibgDetailResult = accAccountDAOobj.getCIMBDetailsForAccount(account.getID(), companyId);
                        if (!ibgDetailResult.getEntityList().isEmpty() && ibgDetailResult.getEntityList().get(0) != null) {
                            CIMBBankDetails cibmDetails = (CIMBBankDetails) ibgDetailResult.getEntityList().get(0);
                            cimbDetailID = cibmDetails != null ? cibmDetails.getID() : "";
                        }
                        cimbBankDetailParams.put(Constants.CIMB_BANK_DETAIL_ID, cimbDetailID);
                        accAccountDAOobj.saveOrupdateCIMBBankDetail(cimbBankDetailParams);
                    }
                }
                
                if (customfield.length() > 0) {
                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", customfield);
                    customrequestParams.put("modulename", Constants.Acc_Account_modulename);
                    customrequestParams.put("moduleprimarykey", Constants.Acc_Accountid);
                    customrequestParams.put("modulerecid", account.getID());
                    customrequestParams.put("moduleid", Constants.Account_Statement_ModuleId);
                    customrequestParams.put("companyid", companyId);
                    customrequestParams.put("customdataclasspath", Constants.Acc_Account_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManager.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        JSONObject accjson = new JSONObject();
                        accjson.put("accaccountcustomdataref", account.getID());
                        accjson.put("accountid", account.getID());
                        accjson.put("acccode", account.getAcccode());
                        if (account.getParent() != null) {
                            accjson.put("parentid", account.getParent().getID());
                        }
                        KwlReturnObject accresult1 = accAccountDAOobj.updateAccount(accjson);
                    }
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(ImportRecordAdvisor.class.getName()).log(Level.SEVERE, null, ex);
            throw new DataInvalidateException(ex.getMessage());
        }
    }

    private void BeforeGetRefModuleData(MethodInvocation mi) throws DataInvalidateException {
        //public List getRefModuleData(HttpServletRequest request, String module, String fetchColumn, String comboConfigid, ArrayList<String> filterNames, ArrayList<Object> filterValues) throws ServiceException, DataInvalidateException;
        Object arguments[] = mi.getArguments();
        String module = (String) arguments[1]; //This variable keeps value of Name of Reference Module Name, In other word we can say Reference Table Name
        try {
            HashMap<String, Object> requestParams = (HashMap<String, Object>) arguments[0];
            String companyid = requestParams.get("companyid").toString();
            String mode = requestParams.get("modName").toString();//This variable keeps value of Name of Module for which data is getting imported
            
            ArrayList<String> filterNames = (ArrayList<String>) arguments[4];
            ArrayList<Object> filterValues = (ArrayList<Object>) arguments[5];
            if (module.equalsIgnoreCase("Term") || module.equalsIgnoreCase("PaymentMethod") || module.equalsIgnoreCase("MasterItem") || module.equalsIgnoreCase("PricingBandMaster") || module.equalsIgnoreCase("CostCenter") || module.equalsIgnoreCase("UnitOfMeasure") || module.equalsIgnoreCase("FieldParams") || module.equalsIgnoreCase("GoodsReceipt") || module.equalsIgnoreCase("invoice") || module.equalsIgnoreCase("User") || module.equalsIgnoreCase("Department")) {
                filterValues.add(companyid);
                filterNames.add("company.companyID");
            } else if(module.equalsIgnoreCase("Vendor") || module.equalsIgnoreCase("com.krawler.hql.accounting.Vendor")
                    || module.equalsIgnoreCase("Customer") || module.equalsIgnoreCase("com.krawler.hql.accounting.Customer") ) {
                filterValues.add(companyid);
                filterNames.add("company.companyID");
                filterValues.add(false);
                filterNames.add("account.deleted");
            } else if (module.equalsIgnoreCase("Account") || module.equalsIgnoreCase("com.krawler.hql.accounting.Account")
                    || module.equalsIgnoreCase("Group") || module.equalsIgnoreCase("com.krawler.hql.accounting.Group")
                    || module.equalsIgnoreCase("Product") || module.equalsIgnoreCase("Tax")) {
                filterValues.add(companyid);
                filterNames.add("company.companyID");
                filterValues.add(false);
                filterNames.add("deleted");
            }else if(module.equalsIgnoreCase("FieldComboData")){
                 filterValues.add(companyid);
                filterNames.add("field.company.companyID");
            } else if (module.equalsIgnoreCase("SequenceFormat")) {
                filterValues.add(companyid);
                filterNames.add("company.companyID");
            } else if (module.equalsIgnoreCase("InventoryWarehouse") || module.equalsIgnoreCase("InventoryLocation")) {
                filterNames.add("company.companyID");
                filterValues.add(companyid);
            } else if (module.equalsIgnoreCase("InvoiceTermsSales")) {
                filterNames.add("company.companyID");
                filterValues.add(companyid);
                if (mode.equalsIgnoreCase("Customer Invoices")) {// while importing sales invoice we need only sales type of invoice term
                    filterNames.add("salesOrPurchase");
                    filterValues.add(true);
                } 
            }
        }catch(Exception ex){

        }
    }

    private Object AfterGetRefModuleData(MethodInvocation mi, Object result) throws DataInvalidateException {
        if(result!=null) {
            List masterList = (List) result;
            if(masterList.size()==0) {
                Object arguments[] = mi.getArguments();
                String module = (String) arguments[1];
                if(ImportHandler.isMasterTable(module)) { //Check for referencing to master
                    try {
                        HashMap<String, Object> requestParams = (HashMap<String, Object>) arguments[0];
                        String companyid = requestParams.get("companyid").toString();
                        String doAction = requestParams.get("doAction").toString();
                        String pref = (String) requestParams.get("masterPreference"); //0:Skip Record, 1:Skip Column, 2:Add new
                        if(doAction.compareToIgnoreCase("import")==0 && pref!=null && pref.compareToIgnoreCase("2")==0){
                            String comboConfigid = (String) arguments[3];
                            ArrayList<Object> filterValues = (ArrayList<Object>) arguments[5];
                            if(module.equalsIgnoreCase("MasterItem") || module.equalsIgnoreCase("com.krawler.hql.accounting.MasterItem")) {
                                HashMap<String, Object> addParams = new HashMap<String, Object>();
                                String masterName = filterValues.get(0)!=null?filterValues.get(0).toString():"";
                                masterName = masterName.length() > 50 ? masterName.substring(0, 50) : masterName; //Maxlength for value is 50 so truncate extra string
                                addParams.put("Company", companyid);
                                addParams.put("Value", masterName);
                                addParams.put("MasterGroup", comboConfigid);

                                MasterItem mItem = (MasterItem) setterMethod( addParams, "com.krawler.hql.accounting.MasterItem", "ID");
                                masterList.add(mItem.getID());
                            }
                        }
                    }catch(Exception e){
                        System.out.println("A(( AfterGetRefModuleData.InsertMasterModuleEntry: "+e.getMessage());
                    }
                    // Now we can copy all account group from one company to other through import functionality. So Commenting below code as this is not usefull
//                } else if(module.equalsIgnoreCase("Group")) { //Accounting specific code
//                    try {
//                        List resultList = (List) result;
//                        if(resultList.size()==0) { //If not found then search group in Global list i.e. companyid=NULL
//                            String fetchColumn = (String) arguments[2];
//                            ArrayList<Object> filterNames = (ArrayList<Object>) arguments[4];
//                            ArrayList<Object> filterValues = (ArrayList<Object>) arguments[5];
//                            String masterValue = filterValues.get(0)!=null?filterValues.get(0).toString():"";
//                            String masterColumnName = filterNames.get(0)!=null?filterNames.get(0).toString():"";
//
//                            String query = "select "+fetchColumn+" from Group where deleted=false and company.companyID is null and "+masterColumnName+"=?";
//                            result = executeQuery( query, new Object[]{masterValue});
//                        }
//                    } catch(Exception ex) {
//                        System.out.println("A(( AfterGetRefModuleData.FetchGroup: "+ex.getMessage());
//                    }
                }
            }
        }
        return result;
    }

    public HashMap getExistingCustomerAddressDetailsInfo(String aliasName, boolean isBillingAddress, String customerID, String companyId) throws ServiceException {
        boolean isDefault=true; 
        String addressID="";        
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("customerid", customerID);
        requestParams.put("isBillingAddress", isBillingAddress);
        requestParams.put("companyid", companyId);
        KwlReturnObject result=accountingHandlerDAOobj.getCustomerAddressDetails(requestParams);
        List list = result.getEntityList();
        if (!list.isEmpty()) {
            // Here if this condition is true it mens that Existing customer have 1 or more Billing address. so making default value false
            isDefault = false;
            Iterator addrItr = list.iterator();
            while (addrItr.hasNext()) {
                CustomerAddressDetails tempDetails = (CustomerAddressDetails) addrItr.next();
                if (tempDetails != null && tempDetails.getAliasName() != null && tempDetails.getAliasName().equalsIgnoreCase(aliasName)) {
                    addressID = tempDetails.getID();
                    isDefault = tempDetails.isIsDefaultAddress();
                    break;
                }
            }
        }
        hashMap.put("addressid", addressID);
        hashMap.put("isDefault", isDefault);
        return hashMap;
    }

    public HashMap getExistingVendorAddressDetailsInfo(String aliasName, boolean isBillingAddress, String vendorID, String companyId) throws ServiceException {
        boolean isDefault = true;
        String addressID = "";
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("vendorid", vendorID);
        requestParams.put("isBillingAddress", isBillingAddress);
        requestParams.put("companyid", companyId);
        KwlReturnObject result = accountingHandlerDAOobj.getVendorAddressDetails(requestParams);
        List list = result.getEntityList();
        if (!list.isEmpty()) {
            // Here if this condition is true it mens that Existing customer have 1 or more Billing address. so making default value false
            isDefault = false;
            Iterator addrItr = list.iterator();
            while (addrItr.hasNext()) {
                VendorAddressDetails tempDetails = (VendorAddressDetails) addrItr.next();
                if (tempDetails != null && tempDetails.getAliasName() != null && tempDetails.getAliasName().equalsIgnoreCase(aliasName)) {
                    addressID = tempDetails.getID();
                    isDefault = tempDetails.isIsDefaultAddress();
                    break;
                }
            }
        }
        hashMap.put("addressid", addressID);
        hashMap.put("isDefault", isDefault);
        return hashMap;
    }
}
