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
package com.krawler.spring.importFunctionality;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ImportFileDetails;
import com.krawler.common.admin.ImportLog;
import com.krawler.common.admin.Modules;
import com.krawler.common.dao.BaseDAO;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.DataInvalidateException;
import com.krawler.common.util.IndiaComplianceConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.hibernate.SQLQuery;

/**
 *
 * @author krawler
 */
public class ImportImpl extends BaseDAO implements ImportDAO {

    @Override
    public Object saveRecord(HashMap<String, Object> requestParams, HashMap<String, Object> dataMap, Object csvReader, String modeName, String classPath, String primaryKey, Object extraObj, JSONArray customfield) throws ServiceException,DataInvalidateException {
        try {
            if (requestParams.containsKey("isCurrencyCode")) {
                boolean isCurrencyCode = Boolean.parseBoolean(requestParams.get("isCurrencyCode").toString());
                if (isCurrencyCode && dataMap.containsKey("currencyCode")) {
                    dataMap.put("Currency", dataMap.get("currencyCode"));
                    dataMap.remove("currencyCode");
                }
            }
            return setterMethod(dataMap, classPath, primaryKey);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ImportImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(ImportImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ImportImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(ImportImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(ImportImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ImportImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(ImportImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Object saveImportLog(HashMap<String, Object> dataMap) throws ServiceException,DataInvalidateException {
        try {
            return setterMethod(dataMap, ImportLog.class.getName(), "Id");
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ImportImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            Logger.getLogger(ImportImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(ImportImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(ImportImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(ImportImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(ImportImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(ImportImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    @Override
    public void updateImportLog(HashMap<String, Object> dataMap) throws ServiceException {
        String id = "";
        String fileName = "";
        String storageName = "";
        String log = "";
        String type = "";
        String failureFileType = "";
        String companyid = "";
        String conditionQuery = "";
        int totalRecord = 0;
        int rejectedRecord = 0;
        List params = new ArrayList();
        try {
            if (dataMap.containsKey("FileName") && dataMap.get("FileName") != null) {
                fileName = (String) dataMap.get("FileName");
                params.add(fileName);
            }
            if (dataMap.containsKey("StorageName") && dataMap.get("StorageName") != null) {
                storageName = (String) dataMap.get("StorageName");
                params.add(storageName);
            }
            if (dataMap.containsKey("Log") && dataMap.get("Log") != null) {
                log = (String) dataMap.get("Log");
                params.add(log);
            }
            if (dataMap.containsKey("Type") && dataMap.get("Type") != null) {
                type = (String) dataMap.get("Type");
                params.add(type);
            }
            if (dataMap.containsKey("FailureFileType") && dataMap.get("FailureFileType") != null) {
                failureFileType = (String) dataMap.get("FailureFileType");
                params.add(failureFileType);
            }
            if (dataMap.containsKey("TotalRecs") && dataMap.get("TotalRecs") != null) {
                totalRecord = (Integer) dataMap.get("TotalRecs");
                params.add(totalRecord);
            }
            if (dataMap.containsKey("Rejected") && dataMap.get("Rejected") != null) {
                rejectedRecord = (Integer) dataMap.get("Rejected");
                params.add(rejectedRecord);
            }

            if (dataMap.containsKey("Id") && dataMap.get("Id") != null) {
                id = (String) dataMap.get("Id");
                conditionQuery = " where id=?";
                params.add(id);
            }
            if (dataMap.containsKey("Company") && dataMap.get("Company") != null) {
                companyid = (String) dataMap.get("Company");
                conditionQuery += " and company =?";
                params.add(companyid);
            }
            String query = "update importlog set filename=?, storagename=?, log=?,type=? ,failurefiletype=?,totalrecs=?,rejected=?" + conditionQuery;
            int affectedRows = executeSQLUpdate(query, params.toArray());

        } catch (Exception ex) {
            throw ServiceException.FAILURE("ImportImpl. : updateImportLog" + ex.getMessage(), ex);
        }
    }

    @Override
    public KwlReturnObject getImportLog(HashMap<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        String condition = "";
        List list = null;
        int count = 0;
        params.add(requestParams.get("startdate"));
        params.add(requestParams.get("enddate"));
        params.add(requestParams.get("companyid"));
        try {
            if (requestParams.containsKey("ss") && requestParams.get("ss") != null) {
                String ss = requestParams.get("ss").toString().trim();
                String[] searchcol = new String[]{"fileName"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 1); 
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery;
            }
            String query = "from ImportLog where (importDate>=? and importDate<=?) and company.companyID = ?"+ condition +" order by importDate desc";
            list = executeQuery( query, params.toArray());
            count = list.size();
            int start = Integer.parseInt(requestParams.get("start").toString());
            int limit = Integer.parseInt(requestParams.get("limit").toString());
            list = executeQueryPaging( query, params.toArray(), new Integer[]{start, limit});

        } catch (Exception ex) {
            throw ServiceException.FAILURE("ImportImpl. : getImportLog" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }
    @Override
    public KwlReturnObject getImportLogForDate(HashMap<String, Object> requestParams) throws ServiceException {
        ArrayList params = new ArrayList();
        String condition = "";
        List list = null;
        int count = 0;
        params.add(requestParams.get("startdate"));
       // params.add(requestParams.get("enddate"));
        params.add(requestParams.get("companyid"));
        try {
            if (requestParams.containsKey("ss") && requestParams.get("ss") != null) {
                String ss = requestParams.get("ss").toString().trim();
                String[] searchcol = new String[]{"fileName"};
                Map SearchStringMap = StringUtil.insertParamSearchStringMap(params, ss, 1); 
                StringUtil.insertParamSearchString(SearchStringMap);
                String searchQuery = StringUtil.getSearchString(ss, "and", searchcol);
                condition += searchQuery;
            }
            String query = "from ImportLog where (importDate>=?) and company.companyID = ?"+ condition +" order by importDate desc";
            list = executeQuery( query, params.toArray());
            count = list.size();
            int start = Integer.parseInt(requestParams.get("start").toString());
            int limit = Integer.parseInt(requestParams.get("limit").toString());
            list = executeQueryPaging( query, params.toArray(), new Integer[]{start, limit});

        } catch (Exception ex) {
            throw ServiceException.FAILURE("ImportImpl. : getImportLog" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }

    @Override
    public List getRefModuleData(HashMap<String, Object> requestParams, String module, String fetchColumn, String comboConfigid, ArrayList<String> filterNames, ArrayList<Object> filterValues) throws ServiceException, DataInvalidateException {
        if (filterNames.size() != filterValues.size()) {
            throw new DataInvalidateException("Count of Filternames and Filterparams are not same for module " + module);
        }
        String query = "select " + fetchColumn + " from " + module;
        try {
            String filter = StringUtil.filterQuery(filterNames, "where");
                if(module.equals("Account") && requestParams.get("modName") !=null && requestParams.get("modName").equals("Assembly Product"))   //ERP-11837
                {
                    filter+=" and mastertypevalue!=2 and mastertypevalue!=3 and mastertypevalue!=4 ";
                }
              
            return executeQuery( query + filter, filterValues.toArray());
        } catch (Exception e) {
            throw ServiceException.FAILURE("ImportImpl. : getRefModuleData" + e.getMessage(), e);
        }
    }

    @Override
    public List getRefModuleDataWithPrimaryKeyValue(HashMap<String, Object> requestParams, String module, String fetchColumn, String comboConfigid, ArrayList<String> filterNames, ArrayList<Object> filterValues) throws ServiceException, DataInvalidateException {
        if (filterNames.size() != filterValues.size()) {
            throw new DataInvalidateException("Count of Filternames and Filterparams are not same for module " + module);
        }
        String query = "select " + fetchColumn +",id from " + module;
        try {
            String filter = StringUtil.filterQuery(filterNames, "where");
            return executeQuery( query + filter, filterValues.toArray());
        } catch (Exception e) {
            throw ServiceException.FAILURE("ImportImpl. : getRefModuleData" + e.getMessage(), e);
        }
    }
    
    public boolean isAccountHavingTransactions(String accountId, String companyId) throws ServiceException, DataInvalidateException {

        String query = "select id from account where parent=? and company=? "
                
                + "UNION select id from customer where account=? and company=? "
                
                + "UNION select id from vendor where account=? and company=? "
                
                + "UNION select id from jedetail where account=? and company=? "
                
                + "UNION select id from product where purchaseAccount=? or salesAccount=? or "
                + "depreciationglaccount=? or depreciationprovisionglaccount=? or "
                + "sellassetglaccount=? or salesrevenuerecognitionaccount=? and company=? "
                
                + "UNION select id from compaccpreferences where discountGiven=? or discountReceived=? "
                + "or shippingCharges=? or cashAccount=? or foreignexchange=? "
                + "or unrealisedgainloss = ? or depereciationAccount=? or expenseAccount=? "
                + "or customerdefaultaccount=? or vendordefaultaccount=? "
                + "or roundingDifferenceAccount=? or liabilityAccount=? and id=? "
                
                + "UNION select id from paymentmethod where account=? and company=? "
                
                + "UNION select id from tax where account=? and company=?";

        List params = new ArrayList();
        params.add(accountId);
        params.add(companyId);

        params.add(accountId);
        params.add(companyId);

        params.add(accountId);
        params.add(companyId);

        params.add(accountId);
        params.add(companyId);

        params.add(accountId);
        params.add(accountId);
        params.add(accountId);
        params.add(accountId);
        params.add(accountId);
        params.add(accountId);
        params.add(companyId);

        params.add(accountId);
        params.add(accountId);
        params.add(accountId);
        params.add(accountId);
        params.add(accountId);
        params.add(accountId);
        params.add(accountId);
        params.add(accountId);
        params.add(accountId);
        params.add(accountId);
        params.add(accountId);
        params.add(accountId);
        params.add(companyId);

        params.add(accountId);
        params.add(companyId);

        params.add(accountId);
        params.add(companyId);

        List transactionIds = executeSQLQuery( query, params.toArray());

        boolean isAccountHavingTransactions = false;

        if (!transactionIds.isEmpty()) {
            isAccountHavingTransactions = true;
        }

        return isAccountHavingTransactions;
    }

        /**
         *Method added to check the account is mapped with Payment Method Or Tax
         *ERP-41444:[Dot Com Smoke Testing] [Payment Method] Account Name are not showing.
         */
    @Override
    public boolean isAccountMappedWithPaymentMethodOrTax(String accountId, String companyId) throws ServiceException, DataInvalidateException {

        String query = "select id from paymentmethod where account=? and company=? "
                + "UNION select id from tax where account=? and company=?";

        List params = new ArrayList();
        params.add(accountId);
        params.add(companyId);
        params.add(accountId);
        params.add(companyId);

        List transactionIds = executeSQLQuery( query, params.toArray());

        boolean isAccountHavingTransactions = false;
        if (!transactionIds.isEmpty()) {
            isAccountHavingTransactions = true;
        }
        return isAccountHavingTransactions;
    }
    
    @Override
    public List getCustomComboID(String fetchColumn, ArrayList filterNames, ArrayList filterValues) throws ServiceException, DataInvalidateException {
        try {
//            "SELECT id FROM fieldComboData where name = ? and fieldid = ?"
            String query = "SELECT " + fetchColumn + " FROM fieldcombodata ";
            String filter = StringUtil.filterQuery(filterNames, "where");
            query += filter;
//            SQLQuery sql = getSessionFactory().getCurrentSession().createSQLQuery(query);
            List res=executeSQLQuery(query,filterValues.toArray());
//            if (filterValues != null) {
//                for (int i = 0; i < filterValues.size(); i++) {
//                    sql.setParameter(i, filterValues.get(i));
//                }
//            }
            return res;
        } catch (Exception e) {
            throw ServiceException.FAILURE("ImportImpl. : getCustomComboID" + e.getMessage(), e);
        }
    }

    @Override
    public List getModuleColumnConfig(Map<String, Object> params) throws ServiceException {

        String moduleId = "", companyid = "", isdocumentimport = "", countryid = "";
        int subModuleFlag = 0;
        boolean isExpenseInvoiceImport=false;
        boolean isCashExpenseInvoiceTrans=false;
        if (params.containsKey("moduleId") && params.get("moduleId") != null) {
            moduleId = (String) params.get("moduleId");
        }
        if (params.containsKey("companyid") && params.get("companyid") != null) {
            companyid = (String) params.get("companyid");
        }
        if (params.containsKey("countryid") && params.get("countryid") != null) {
            countryid = (String) params.get("countryid");
        }
        if (params.containsKey("isdocumentimport") && params.get("isdocumentimport") != null) {
            isdocumentimport = (String) params.get("isdocumentimport");
        }
        if (params.containsKey("subModuleFlag") && params.get("subModuleFlag") != null) {
            subModuleFlag = ((Integer) params.get("subModuleFlag")).intValue();
        }
        if (params.containsKey("isExpenseInvoiceImport") && params.get("isExpenseInvoiceImport") != null) {
            isExpenseInvoiceImport = (Boolean) params.get("isExpenseInvoiceImport");
        }
        if (params.containsKey("incash") && params.get("incash") != null) {
            isCashExpenseInvoiceTrans = (Boolean) params.get("incash");
        }
        
        
        String orderby = " order by ismandatory desc, isconditionalmandetory desc, defaultHeader asc ";

        
        if (moduleId.equalsIgnoreCase("35")) {    //ERP-9883
            orderby = "";
        }
        String sqlCondition = " (dh.submoduleflag=0 or dh.submoduleflag=?) ";
        
        if (isExpenseInvoiceImport) {
            if (isCashExpenseInvoiceTrans) {
                sqlCondition = " ((dh.islineitem='F' && dh.submoduleflag in ('1','0')) OR (dh.submoduleflag=? && dh.islineitem='T')) and dh.isbatchdetail='F' and dh.defaultHeader not in ('Consignment Number','Generate Goods Receipt','Goods Receipt No') ";
            } else {
                sqlCondition = " ((dh.islineitem='F' && dh.submoduleflag ='0') OR (dh.submoduleflag=? && dh.islineitem='T')) and dh.isbatchdetail='F' and dh.defaultHeader not in ('Consignment Number','Generate Goods Receipt','Goods Receipt No') ";
            }
        }

        String query = "select * from ( select dh.id, dh.defaultHeader, dh.ismandatory, dh.isconditionalmandetory, dh.dataindex, dh.renderertype from default_header dh "
                + "inner join modules mo on mo.id = dh.module "
                + "where dh.module=? and (countryid = '0' OR  countryid = ? ) " //country id check zero represent all common default header entry for all contry
                + "and (dh.allowimport = ? or dh.allowimport = ?) and dh.isdocumentimport=? and " + sqlCondition
                + " union  "
                + "select dh.id, dh.defaultHeader, dh.ismandatory, dh.isconditionalmandetory, dh.dataindex, dh.renderertype from default_header dh "
                + "inner join modules mo on mo.id = dh.module "
                + "where dh.module=?  and (countryid = '0' OR  countryid = ?)  and (dh.allowimport = ? or dh.allowimport = ?) " // For UNION Query country id check is not given so For country wise data not present in result. This problem is also present in Core ERP also
                + "and (dh.customflag = ? or dh.customflag = ?)  and dh.isdocumentimport=? and " + sqlCondition + ") "
                + "as temp " + orderby;
        return executeSQLQuery(query,
                //                new Object[]{moduleId, companyid, "T", "1", moduleId, "T", "1", "F", "0"});
                new Object[]{moduleId, countryid, "T", "1", isdocumentimport, subModuleFlag, moduleId, countryid, "T", "1", "F", "0", isdocumentimport, subModuleFlag});
    }
    
    @Override
    public List getModuleNonEditableFields(String moduleId) throws ServiceException {
        String query = "select id from default_header where isreadonly = true and module=?";

        return executeSQLQuery( query, new Object[]{moduleId});
    }
    
    /**
     * Method check whether Location/Batch/Serial/Warehouse is activated for the product associated with given ID.
     * @param id
     * @return
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getProductIdIfLocationWarehouseBatchSerialIsActivated(String id) throws ServiceException {
        String query = "select id from product where "
                + "(islocationforproduct = 'T' or iswarehouseforproduct = 'T' or isBatchForProduct = 'T' or isSerialForProduct = 'T') "
                + "and id =?";
        List list = executeSQLQuery(query, new Object[]{id});
        int totalCount = list.size();
        KwlReturnObject result = new KwlReturnObject(true, null, null, list, totalCount);
        return result;
    }
    
    
    @Override
    public List getCustomModuleColumnConfig(String moduleId, String companyid , boolean isExport) throws ServiceException {
        int moduleid = 0;
        if (!StringUtil.isNullOrEmpty(moduleId)) {
            moduleid = Integer.parseInt(moduleId);
        }
        if ((isExport && (moduleid == Constants.Account_Statement_ModuleId ||  moduleid == Constants.Acc_Customer_ModuleId)) || moduleid == Constants.Acc_Product_Master_ModuleId || moduleid == Constants.Acc_GENERAL_LEDGER_ModuleId ||  moduleid == Constants.Acc_Vendor_ModuleId || moduleid==Constants.Acc_FixedAssets_Details_ModuleId || moduleid == Constants.Acc_Sales_Order_ModuleId || moduleid == Constants.Acc_opening_Sales_Invoice || moduleid == Constants.Acc_opening_Customer_CreditNote|| moduleid == Constants.Acc_opening_Customer_DebitNote
                || moduleid == Constants.Acc_opening_Receipt || moduleid == Constants.Acc_opening_Prchase_Invoice || moduleid == Constants.Acc_opening_Payment || moduleid == Constants.Acc_opening_Vendor_CreditNote || moduleid == Constants.Acc_opening_Vendor_DebitNote 
                || moduleid == Constants.Acc_Customer_Quotation_ModuleId || moduleid == Constants.Acc_Vendor_Quotation_ModuleId || moduleid == Constants.Acc_Purchase_Order_ModuleId || moduleid == Constants.Acc_Vendor_Invoice_ModuleId || moduleid == Constants.Acc_Invoice_ModuleId || moduleid == Constants.Acc_Delivery_Order_ModuleId || moduleid == Constants.Acc_Goods_Receipt_ModuleId || moduleid == Constants.Acc_Cash_Sales_ModuleId ||moduleid == Constants.Acc_Credit_Note_ModuleId||moduleid==Constants.Acc_Receive_Payment_ModuleId||moduleid==Constants.Acc_Make_Payment_ModuleId||moduleid==Constants.GSTModule) {
            if(moduleid == Constants.Acc_opening_Sales_Invoice){
                moduleId = Constants.Acc_Invoice_ModuleId+"";
            } else if(moduleid == Constants.Acc_opening_Prchase_Invoice){
                moduleId = Constants.Acc_Vendor_Invoice_ModuleId+"";
            } else if(moduleid == Constants.Acc_opening_Receipt){
                moduleId = Constants.Acc_Receive_Payment_ModuleId+"";
            } else if(moduleid == Constants.Acc_opening_Customer_DebitNote || moduleid == Constants.Acc_opening_Vendor_DebitNote){
                moduleId = Constants.Acc_Debit_Note_ModuleId+"";
            } else if(moduleid == Constants.Acc_opening_Customer_CreditNote || moduleid == Constants.Acc_opening_Vendor_CreditNote){
                moduleId = Constants.Acc_Credit_Note_ModuleId+"";
            } else if(moduleid == Constants.Acc_opening_Payment){
                moduleId = Constants.Acc_Make_Payment_ModuleId+"";
            }
            String query = "select id,isessential,maxlength,fieldtype,fieldlabel,customcolumn,fieldname,refcolnum,colnum,relatedmoduleid,gstconfigtype from fieldparams where moduleid=? and companyid=? and isactivated=?";
            return executeSQLQuery( query, new Object[]{moduleId, companyid, 1});
        } else {
            String query = "select id,isessential,maxlength,fieldtype,fieldlabel,customcolumn,fieldname,refcolnum,colnum,relatedmoduleid from fieldparams where moduleid=? and companyid=? and customfield=? and isactivated=?";
            return executeSQLQuery( query, new Object[]{moduleId, companyid, 1, 1});
        }

    }

    @Override
    public List getModuleObject(String moduleName) throws ServiceException {
        String query = "from Modules where modulename=?";
        return executeQuery( query, moduleName);
    }
    
    @Override
    public List getGSTTermDetails(HashMap<String, Object> params) throws ServiceException {
        List list = new ArrayList();
        if (params.containsKey("companyid")) {
            String companyid = (String) params.get("companyid");
            list.add(companyid);
        }
        if (params.containsKey("termType")) {
            int termType = (int) params.get("termType");
            list.add(termType);
        }
        if (params.containsKey("subModuleFlag")) {
            int subModuleFlag = (int) params.get("subModuleFlag");
            if (subModuleFlag == 1) {
                list.add(true);
            } else {
                list.add(false);
            }
        }
        String query = "select id,term from linelevelterms where company = ? and termType=? and  salesOrPurchase=?";
        return executeSQLQuery(query, list.toArray());
    }
    /**
     * Get GST rule details if present 
     * @param reqMap
     * @return
     * @throws ServiceException
     */
    public List getGSTRuleDetails(Map<String, Object> reqMap) throws ServiceException {
        List params = new ArrayList();
        String condition = "";
        String subquery = "";
        subquery = " select etr.id from prodcategorygstmapping petr inner join entitybasedlineleveltermsrate etr on"
                + "  etr.id=petr.entitytermrate where  ";
        if (reqMap.containsKey("prodcategory") && reqMap.get("prodcategory") != null) {
            params.add((String) reqMap.get("prodcategory"));
            condition += " petr.prodcategory=?";
        } 
        if (reqMap.containsKey("term") && reqMap.get("term") != null) {
            params.add((String) reqMap.get("term"));
            condition += " and etr.linelevelterms=?";
        }
        if (reqMap.containsKey("entity") && reqMap.get("entity") != null) {
            params.add((String) reqMap.get("entity"));
            condition += " and etr.entity=?";
        }
        if (reqMap.containsKey("shiplocation1") && reqMap.get("shiplocation1") != null && !StringUtil.isNullOrEmpty(reqMap.get("shiplocation1").toString())) {
            params.add((String) reqMap.get("shiplocation1"));
            condition += " and etr.shippedloc1=?";
        }
        if (reqMap.containsKey("shiplocation2") && reqMap.get("shiplocation2") != null && !StringUtil.isNullOrEmpty(reqMap.get("shiplocation2").toString())) {
            params.add((String) reqMap.get("shiplocation2"));
            condition += " and etr.shippedloc2=?";
        }
        if (reqMap.containsKey("shiplocation3") && reqMap.get("shiplocation3") != null && !StringUtil.isNullOrEmpty(reqMap.get("shiplocation3").toString())) {
            params.add((String) reqMap.get("shiplocation3"));
            condition += " and etr.shippedloc3=?";
        }
        if (reqMap.containsKey("shiplocation4") && reqMap.get("shiplocation4") != null && !StringUtil.isNullOrEmpty(reqMap.get("shiplocation4").toString())) {
            params.add((String) reqMap.get("shiplocation4"));
            condition += " and etr.shippedloc4=?";
        }
        if (reqMap.containsKey("shiplocation5") && reqMap.get("shiplocation5") != null && !StringUtil.isNullOrEmpty(reqMap.get("shiplocation5").toString())) {
            params.add((String) reqMap.get("shiplocation5"));
            condition += " and etr.shippedloc5=?";
        }
        if (reqMap.containsKey("type") && reqMap.get("type") != null) {
            params.add((Integer) reqMap.get("type"));
            condition += " and etr.taxtype=?";
        }
        if (reqMap.containsKey("applieddate")) {
            condition += " and etr.applieddate= ?";
            params.add((Date) reqMap.get("applieddate"));
        }
        if (reqMap.containsKey(Constants.isMerchantExporter) && reqMap.get(Constants.isMerchantExporter) != null) {
            params.add((Boolean) reqMap.get(Constants.isMerchantExporter));
            condition += " and etr.ismerchantexporter=?";
        }
        String query = subquery + condition;
        List list = executeSQLQuery(query, params.toArray());
        return list;
    }
    /**
     * Check GST rules used in transaction or not
     * @param mapData
     * @return
     * @throws ServiceException
     */
    public KwlReturnObject isGSTRuleusedInTransaction(Map<String, Object> mapData) throws ServiceException {
        List<Object> listData = new ArrayList<Object>();
        StringBuilder query = new StringBuilder();
        List<String> listTables = (List) mapData.get(Constants.tableName);
        String groupByUnion = " group by entityterm having count > 0 UNION ";
        String conditions = " WHERE ";
        StringBuilder defaultQuery = new StringBuilder();
        Map forModuleNames = (Map) mapData.get(Constants.modulename);
        List<String> conditionsList = new ArrayList<String>();
        if (mapData.containsKey(Constants.id1)) {
            conditions += " entityterm=? ";
            conditionsList.add(mapData.get(Constants.id1).toString());
        }
        for (String tableName : listTables) {
            defaultQuery.setLength(0);
            defaultQuery.insert(0, "select count(entityterm) as count,? as moduleName from " + tableName + " ");
            listData.add(forModuleNames.get(tableName));
            listData.addAll(conditionsList);
            query.append(defaultQuery.toString()).append(conditions).append(groupByUnion);
        }
        String q = query.substring(0, query.lastIndexOf(" UNION "));
        List<Object[]> res = executeSQLQuery(q, listData.toArray());
        if (res.size() > 0) {
            StringBuilder val = new StringBuilder();
            for (Object[] row : res) {
                if (val.indexOf(row[1].toString()) == -1) {
                    val.append(row[1].toString()).append(", ");
                }
            }
            return new KwlReturnObject(false, val.substring(0, val.lastIndexOf(", ")), null, res, res.size());
        }
        return new KwlReturnObject(true, "", null, res, res.size());
    }
    /**
     * Get State dimension details from fieldcombodata.
     * this method called only for INDIA
     * @param jSONObject
     * @return
     * @throws ServiceException 
     */
    public List getStatesFromFieldCombodata(JSONObject jSONObject) throws ServiceException {
        List list = new ArrayList();
        list.add(jSONObject.optString(Constants.fieldlabel));
        list.add(jSONObject.optInt(Constants.moduleid));
        list.add(jSONObject.optString(Constants.companyid));
        list.add(jSONObject.optString("entityStateid"));
        /**
         * Don't take empty/None result for combodata value
         * this query return result for Import GST rule import if State is "Other" for INDIA GST
         * Result will not considered empty and none value
         */
        String query = " select fcd.id,fcd.value from fieldcombodata fcd inner join fieldparams fp on fcd.fieldid=fp.id where "
                + " fp.fieldlabel=? and fp.moduleid=? and fp.companyid=? and fcd.value!='' and fcd.value!='None' and fcd.value is not null "
                + " and fcd.id!=? ";
        List returnList = executeSQLQuery(query, list.toArray());
        return returnList;
    }
    /**
     * Get custom table column number from fieldparams
     * @param fieldLabel
     * @param companyId
     * @param module
     * @param customColumn
     * @return
     * @throws ServiceException 
     */
     @Override 
    public int getColumnFromFieldParams(String fieldLabel, String companyId, int module,int customColumn) throws ServiceException {
        int column = 0;
        List list = null;
        ArrayList params = new ArrayList();
        params.add(fieldLabel);
        params.add(module);
        params.add(companyId);
        params.add(customColumn);
        String hql = "select colnum from FieldParams where fieldlabel=? and moduleid= ? and company.companyID= ? and customcolumn=?";
        list = executeQuery(hql, params.toArray());
        if (list!=null && !list.isEmpty()) {
            column = Integer.parseInt(list.get(0).toString());
        }
        return column;
    }
    public String getTableName(String fileName) {
        fileName = fileName.trim();
        int startIndex = fileName.contains("/") ? (fileName.lastIndexOf("/") + 1) : 0;
        int endIndex = fileName.contains(".") ? fileName.lastIndexOf(".") : fileName.length();
        String tablename = fileName.substring(startIndex, endIndex);
        tablename = tablename.replaceAll("\\.", "_");
        tablename = tablename.replaceAll("\\'", "_");
        tablename = "IL_" + tablename;
        return tablename;
    }

    @Override
    public int createFileTable(String tablename, int cols) throws ServiceException {
        if (cols == 0) {
            return 0;
        }
        try {
            String query = "", columns = "";
            query = "DROP TABLE IF EXISTS  `" + tablename + "`";
            executeSQLUpdate( query);

            for (int i = 0; i < cols; i++) {
                columns += "`col" + i + "` TEXT DEFAULT NULL,";
            }
            query = "create table `" + tablename + "` ("
                    + "id INT NOT NULL AUTO_INCREMENT,"
                    + columns
                    + "isvalid INT(1) DEFAULT 1,"
                    + "invalidcolumns VARCHAR(255) DEFAULT NULL,"
                    + "validatelog VARCHAR(1000) DEFAULT NULL,"
                    + "PRIMARY KEY (id)"
                    + ")ENGINE=InnoDB DEFAULT CHARSET=utf8";

            return executeSQLUpdate( query);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("createFileTable:" + ex.getMessage(), ex);
        }
    }

    public int removeFileTable(String tablename) throws ServiceException {
        try {
            String query = "DROP TABLE IF EXISTS `" + tablename + "`";
            return executeSQLUpdate( query);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("removeFileTable:" + ex.getMessage(), ex);
        }
    }

    public int removeAllFileTables() throws ServiceException {
        int cnt = 0;
        try {
            String getQuery = "show tables like 'IL_%'";
            List list = executeSQLQuery( getQuery);

            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                String tablename = (String) itr.next();
                String query = "DROP TABLE `" + tablename + "`";
                executeSQLUpdate( query);
                cnt++;
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("removeAllFileTables:" + ex.getMessage(), ex);
        }
        return cnt;
    }

    @Override
    public int dumpFileRow(String tablename, Object[] dataArray) throws ServiceException {
        if (dataArray.length == 0) {
            return 0;
        }
        try {
            String columns = ") values (";
            for (int i = dataArray.length - 1; i >= 0; i--) {
                columns = ",col" + i + columns + "?,";
            }
            String query = "insert into `" + tablename + "` (" + (columns.substring(1, columns.length() - 1)) + ")";
            return executeSQLUpdate( query, dataArray);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("dumpFileRow:" + ex.getMessage(), ex);
        }
    }

    public int makeUploadedFileEntry(String filename, String onlyfilename, String tablename, String companyid) throws ServiceException {
        String query = "insert into uploadedfiles (id,filename,filepathname,tablename,company) values (UUID(), ?,?,?,?)";
        return executeSQLUpdate( query, new Object[]{onlyfilename, filename, tablename, companyid});
    }

    public int markRecordValidation(String tablename, int id, int isvalid, String validateLog, String invalidColumns) throws ServiceException {
        int affectedRows = 0;
        try {
            ArrayList params = new ArrayList();
            
            if (validateLog != null && validateLog.length() > 1000) {//refer ticket ERP-18490 and here, 1000 is the column length
                validateLog = validateLog.substring(0, 999);
            }
            params.add(validateLog);
            params.add(invalidColumns);
            params.add(isvalid);

            String condition = "";
            if (id != -1) { // if id==-1 then update all else update respective record
                condition = " where id=?";
                params.add(id);
            }

            String query = "update `" + tablename + "` set validatelog=?, invalidcolumns=?, isvalid=? " + condition;
            affectedRows = executeSQLUpdate( query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("markRecordValidation:" + ex.getMessage(), ex);
        }
        return affectedRows;
    }

    public KwlReturnObject getFileData(String tablename, HashMap<String, Object> filterParams) throws ServiceException {
        List list = null;
        int count = 0;
        try {
            String condition = "";
            ArrayList params = new ArrayList();
            if (filterParams.containsKey("isvalid")) {
                condition = (condition.length() == 0 ? " where " : " and ") + " isvalid=? ";
                params.add(filterParams.get("isvalid"));
            }

            String query = "select * from `" + tablename + "` " + condition;
            list = executeSQLQuery( query, params.toArray());
            count = list.size();

            if (filterParams.containsKey("start") && filterParams.containsKey("limit")) {
                condition = " limit ?,?";
                int start = Integer.parseInt(filterParams.get("start").toString());
                int limit = Integer.parseInt(filterParams.get("limit").toString());
                params.add(start);
                params.add(limit);
                list = executeSQLQuery( query, params.toArray());
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getFileData:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, null, null, list, count);
    }

    @Override
    public KwlReturnObject getMaxDisplayOrderOfGroup() throws ServiceException {
        String sql = "SELECT Max(displayorder) FROM accgroup";
        List list = executeSQLQuery( sql);
        return new KwlReturnObject(true, null, null, list, list.size());
    }
    
    @Override
    public int getNatureByGroupID(String groupID, String companyID) throws ServiceException {
        int natureValue = -1;
        try {
            ArrayList params = new ArrayList();
            params.add(groupID);
            params.add(companyID);

            String query = " select nature from Group where ID = ? and company.companyID = ? ";
            List list = executeQuery( query, params.toArray());

            if (!list.isEmpty() && list.get(0) != null) {
                natureValue = (Integer) list.get(0);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getNatureByGroupID:" + ex.getMessage(), ex);
        }
        return natureValue;
    }
    
    public int updateManualJePostSettingForAccount(String accountID, String usedIn) throws ServiceException {
        int affectedRows = 0;
        try {
            ArrayList params = new ArrayList();
            params.add(usedIn);

            String condition = "";
            if (!StringUtil.isNullOrEmpty(accountID)) {
                condition = " where id=?";
                params.add(accountID);
            }

            String query = "update account set usedin=? " + condition;
            affectedRows = executeSQLUpdate( query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("updateManualJePostSettingForAccount:" + ex.getMessage(), ex);
        }
        return affectedRows;
    }

    @Override
    public KwlReturnObject getSequenceFormat(Map<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
        String query = "select id,name,prefix,suffix,isdatebeforeprefix,dateformatinprefix,showdateformataftersuffix,dateformataftersuffix,startfrom,isdateafterprefix,dateformatafterprefix from sequenceformat where deleted='F'";

        if (filterParams.containsKey("companyid")) {
            condition += " and company=?";
            params.add(filterParams.get("companyid"));
        }
        if (filterParams.containsKey("moduleid")) {
            condition += " and moduleid=?";
            params.add(filterParams.get("moduleid"));
        }
        query += condition;
        returnList = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    
    @Override
    public KwlReturnObject getMaxSequenceFormatNUmber(Map<String, Object> filterParams) throws ServiceException {
        List list=Collections.EMPTY_LIST;
        ArrayList params = new ArrayList();
        String companyid = (String) filterParams.get("companyid");
        String formatid = (String) filterParams.get("formatid");
        String sqltablename = (String) filterParams.get("sqltablename");
        if (!StringUtil.isNullOrEmpty(companyid) && !StringUtil.isNullOrEmpty(formatid) && !StringUtil.isNullOrEmpty(sqltablename)) {
            String query = "select max(seqnumber) from "+sqltablename+" where seqformat = ? and company =  ?";
            params.add(formatid);
            params.add(companyid);
            list = executeSQLQuery( query, params.toArray());
        }
        
        return new KwlReturnObject(true, "", null, list, list.size());
    }

    @Override
    public KwlReturnObject getTaxbyIDorName(String companyId, String fetchColumnName, String conditionColumnName, String data) throws ServiceException {
        List list = null;
        try {
            String query = "select " + fetchColumnName + " from tax where company=? and " + conditionColumnName + "=?";
            ArrayList params = new ArrayList();
            params.add(companyId);
            params.add(data);
            list = executeSQLQuery( query, params.toArray());;
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getTaxbyIDorName:" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", "", list, list.size());
    }
    public boolean isAssetIDPresent(String assetID,String companyID){
        int count=0;
        boolean isPresent=false;
        try{
            String selQuery = "Select count(assetid) from assetdetail where assetid = ? and company = ? ";
            List<BigInteger> list = executeSQLQuery( selQuery, new Object[]{assetID,companyID});
            if (!list.isEmpty() && list.size() > 0) {
                count = (Integer) list.get(0).intValue();
            }
        }catch(Exception ex){
            Logger.getLogger(ImportImpl.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            if(count>0){
                isPresent=true;
            }
            return isPresent;
        }
    }

    @Override
    public List getProductLevelActiveItems(String companyId, String productCode) throws ServiceException {
        List list = null;
        if(productCode.endsWith("”")){
            productCode=productCode.substring(0,productCode.length()-1)+'"';
        }
        String query = "SELECT id, iswarehouseforproduct, islocationforproduct, isrowforproduct, israckforproduct, isbinforproduct, isBatchForProduct, "
                + " isSerialForProduct "
                +" FROM Product WHERE company.companyID = ? and productid = '"+productCode+"'";
        ArrayList params = new ArrayList();
        params.add(companyId);
//        params.add(productCode);
        list = executeQuery( query, params.toArray());

        return list;
    }
    
    @Override
    public boolean isProductUsedInTransaction(String companyId, String productId) throws ServiceException {

        boolean isUsed = false;
        String fetchInvIdQry = "SELECT product from in_stockadjustment  where product=? AND company=?"
                + " UNION SELECT product from in_interstoretransfer where product=? AND company=?"
                + " UNION SELECT product from in_goodsrequest where product=? AND company=?"
//                + " UNION SELECT product from  podetails where product=? AND company=?"
                + " UNION SELECT product from sodetails where product=? AND company=? and lockquantity > 0 " //if quantity is locked in SO then we can't allow to update intial quantity"
                + " UNION SELECT product from grodetails where product=? AND company=?"
                + " UNION SELECT product from  dodetails where product=? AND company=?"
                + " UNION SELECT product from srdetails where product=? AND company=?"
                + " UNION SELECT product from prdetails where product=? AND company=?"
                + " UNION SELECT inventory.product FROM	grdetails INNER JOIN inventory ON grdetails.id = inventory.id WHERE inventory.product =? AND grdetails.company=? ";
//                + " UNION SELECT product from  vendorquotationdetails where product=? AND company=?";
        List prm = new ArrayList();

        prm.add(productId);
        prm.add(companyId);
        prm.add(productId);
        prm.add(companyId);
        prm.add(productId);
        prm.add(companyId);
        prm.add(productId);
        prm.add(companyId);
        prm.add(productId);
        prm.add(companyId);
        prm.add(productId);
        prm.add(companyId);
        prm.add(productId);
        prm.add(companyId);
        prm.add(productId);
        prm.add(companyId);
        prm.add(productId);
        prm.add(companyId);

        List ftchList = executeSQLQuery( fetchInvIdQry, prm.toArray());
        if (ftchList != null && !ftchList.isEmpty()) {
            isUsed = true;
        }
        return isUsed;
    }
    
    /**
     * Method to get initial quantity and price.
     * @param companyId
     * @param productId
     * @return
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getOpeningQuantityAndInitialPrice(String companyId, String productId) throws ServiceException {
        List list = new ArrayList();
        String sqlQuery = "select inv.quantity, pl.price from inventory inv   left \n"
                + "join  ( select p1.*, IF(uom.name = 'N/A', ' ', uom.name) as productuomname, prodtype.name as producttypename from product p1  left \n"
                + "join uom on uom.id = p1.unitOfMeasure  left \n"
                + "join producttype prodtype on prodtype.id = p1.producttype where p1.company = ? )  as p on p.id = inv.product   left \n"
                + "join pricelist pl on pl.product = p.id and pl.affecteduser = '-1' and pl.carryin='T' and pl.uomid = p.unitOfMeasure  and pl.currency=p.currency and (pl.applydate =(select max(applydate) as ld from pricelist where pricelist.product=inv.product and pricelist.carryin='T'  and pricelist.uomid = p.unitOfMeasure  and pricelist.currency=pl.currency and  pricelist.affecteduser='-1'  and pricelist.applydate<=inv.updatedate and inv.company=?  and p.deleteflag = 'F' and p.isasset!='1' and p.producttype NOT IN ('f071cf84-515c-102d-8de6-001cc0794cfa','4efb0286-5627-102d-8de6-001cc0794cfa','a6a350c4-7646-11e6-9648-14dda97925bd','a839448c-7646-11e6-9648-14dda97925bd')  group by inv.product))   left \n"
                + "join locationbatchdocumentmapping lbdm on p.id=lbdm.documentid  inner \n"
                + "join newproductbatch npb on lbdm.batchmapid=npb.id where inv.newinv='T' and inv.defective='F' and inv.carryin='T' and inv.company = ? and inv.quantity!=0  and p.deleteflag = 'F' and p.isasset!='1' and p.producttype NOT IN ('f071cf84-515c-102d-8de6-001cc0794cfa','4efb0286-5627-102d-8de6-001cc0794cfa','a6a350c4-7646-11e6-9648-14dda97925bd','a839448c-7646-11e6-9648-14dda97925bd') and inv.product = ?";
        list = executeSQLQuery(sqlQuery, new Object[]{companyId, companyId, companyId, productId});
        return new KwlReturnObject(true, "", "", list, list.size());
    }
    @Override
    public boolean isPartNumberActivated(String companyId) throws ServiceException {
        boolean isPartNumberActivated = false;
        String query = "select partNumber from CompanyAccountPreferences where company.companyID=?";
        List prm = new ArrayList();
        prm.add(companyId);

        List ftchList = executeQuery( query, prm.toArray());
        if (ftchList != null && !ftchList.isEmpty()) {
            isPartNumberActivated = (Boolean) ftchList.get(0);
}
        return isPartNumberActivated;
    }
    
    @Override
    public boolean isPerpetualInventory(String companyId) throws ServiceException {
        boolean isPerpetualInventory = false;
        String query = "select inventoryValuationType from CompanyAccountPreferences where company.companyID=?";
        List prm = new ArrayList();
        prm.add(companyId);

        List ftchList = executeQuery(query, prm.toArray());
        if (ftchList != null && !ftchList.isEmpty()) {
            int value = (Integer) ftchList.get(0);
            if (value == Constants.PERPETUAL_VALUATION_METHOD) {
                isPerpetualInventory = true;
            }
        }
        return isPerpetualInventory;
    }
    
    @Override
    public boolean isMinMaxOrdering(String companyId) throws ServiceException {
        boolean isMinMaxOrdering = false;
        String query = "select minMaxOrdering from ExtraCompanyPreferences where company.companyID=?";
        List prm = new ArrayList();
        prm.add(companyId);

        List ftchList = executeQuery(query, prm.toArray());
        if (ftchList != null && !ftchList.isEmpty()) {
            isMinMaxOrdering = (Boolean) ftchList.get(0);
        }
        return isMinMaxOrdering;
    }
    @Override
    public boolean isStockUomMatchWithStockUomOfUomSchema(String uomSchema, String stockUOM,  String companyId) throws ServiceException {
        boolean isMatched = false;
        String query = "from UOMschemaType where (stockuom.ID=? or stockuom.name=?) and name=? and company.companyID=?";
        List prm = new ArrayList();
        prm.add(stockUOM);
        prm.add(stockUOM);
        prm.add(uomSchema);
        prm.add(companyId);

        List ftchList = executeQuery( query, prm.toArray());
        if (ftchList != null && !ftchList.isEmpty()) {
            isMatched = true;
}
        return isMatched;
    }
    @Override
    public KwlReturnObject getProductByProductCode(String companyId, String productCode) throws ServiceException {
        List list = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            params.add(productCode);
            params.add(companyId);
            String query = "select ID, isSerialForProduct from Product p where p.productid=? and p.company.companyID=? ";
            list = executeQuery( query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("ImportImpl.getProductByProductCode", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getMasterItem(String companyId, String categoryName, String groupid) throws ServiceException {
        List list = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            params.add(categoryName);
            params.add(groupid);
            params.add(companyId);
            String query = "select ID from MasterItem mst where mst.value=? and mst.masterGroup.ID=? and mst.company.companyID=? ";
            list = executeQuery(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("ImportImpl.getMasterItem", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public KwlReturnObject getTaxByCode(String companyId, String taxCode, boolean isSales) throws ServiceException {
        List list = new ArrayList();
        try {
            String query = "select ID from Tax where company.companyID=? and taxCode=? ";
            if(isSales){
                query+="and taxtype!=1 ";
            } else {
                query+="and taxtype!=2 ";
            }
            ArrayList params = new ArrayList();
            params.add(companyId);
            params.add(taxCode);
            list = executeQuery(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("ImportImpl.getTaxByCode", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getTax(Map requestMap) throws ServiceException {
        List list = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            String condition = " where tax.company.companyID = ? ";
            String companyId = (String) requestMap.get(Constants.companyKey);
            params.add(companyId);

            if (!StringUtil.isNullOrEmpty((String) requestMap.get(Constants.TAXID))) {
                condition += " and tax.ID = ? ";
                params.add((String) requestMap.get(Constants.TAXID));
            }
            if (!StringUtil.isNullOrEmpty((String) requestMap.get(Constants.TAXNAME))) {
                condition += " and tax.name = ? ";
                params.add((String) requestMap.get(Constants.TAXNAME));
            }
            if (!StringUtil.isNullOrEmpty((String) requestMap.get(Constants.TAXCODE))) {
                condition += " and tax.taxCode = ? ";
                params.add((String) requestMap.get(Constants.TAXCODE));
            }
            if (requestMap.containsKey(Constants.TAXTYPE) && requestMap.get(Constants.TAXTYPE) != null) {
                condition += " and (tax.taxtype = ? or tax.taxtype = 0) ";
                params.add((int) requestMap.get(Constants.TAXTYPE));
            }
            if (requestMap.containsKey(Constants.ACTIVATED) && requestMap.get(Constants.ACTIVATED) != null) {
                condition += " and tax.activated = ? ";
                params.add((boolean) requestMap.get(Constants.ACTIVATED));
            }
            String query = "select tax,tax.ID,tax.activated,tax.taxtype from Tax tax " + condition;
            list = executeQuery(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("ImportImpl.getTax :" + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public boolean isTaxActivated(String companyId, String taxCode) throws ServiceException {
        boolean isActivated = false;
        try {
            String query = "select activated from Tax where company.companyID=? and taxCode=? and activated = ? ";
            ArrayList params = new ArrayList();
            params.add(companyId);
            params.add(taxCode);
            params.add(true);
            List list = executeQuery(query, params.toArray());
            if (!list.isEmpty() && list.size() > 0) {
                isActivated = (boolean) list.get(0);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("ImportImpl.isTaxActivated", ex);
        }
        return isActivated;
    }
    
    @Override
    public KwlReturnObject getAccountByName(String companyId, String accountName, int groupNature) throws ServiceException {
        List list = new ArrayList();
        try {
            String query = "select ID from Account where company.companyID=? and name=? and group.nature=? and deleted=?";
            ArrayList params = new ArrayList();
            params.add(companyId);
            params.add(accountName);
            params.add(groupNature);
            params.add(false);
            list = executeQuery(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("ImportImpl.getAccountByName", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public void createFailureXlsFiles(String filename, List failureArr, String ext, List failureColumnArr) {
        try {
            int rownum = 0;
            int cellnum = 0;
            if (StringUtil.isNullOrEmpty(ext)) {
                ext = filename.substring(filename.lastIndexOf("."));
            }
            filename = filename.substring(0, filename.lastIndexOf("."));

            String destinationDirectory = storageHandlerImpl.GetDocStorePath() + "xlsfiles";
            Workbook wb = new HSSFWorkbook();
            Sheet sheet = wb.createSheet("Sheet-1");
            Cell cell = null;

            CellStyle style = wb.createCellStyle();
            Font font = wb.createFont();

            for (int rowCnt = 0; rowCnt < failureArr.size(); rowCnt++) {
                List recarr = (List) failureArr.get(rowCnt);
                Map<Integer, Object> failureColumnMap = (Map<Integer, Object>) failureColumnArr.get(rowCnt);
                Row headerRow = sheet.createRow(rownum++);
                cellnum = 0;

                for (int cellCnt = 0; cellCnt < recarr.size(); cellCnt++) {
                    cell = headerRow.createCell(cellnum++);
                    if (recarr.get(cellCnt) instanceof Cell) {
                        Cell givenCell = (Cell) recarr.get(cellCnt);

                        switch (givenCell.getCellType()) {
                            case Cell.CELL_TYPE_NUMERIC:
                                cell.setCellValue(givenCell.getNumericCellValue());
                                break;
                            case Cell.CELL_TYPE_STRING:
                                cell.setCellValue(givenCell.getStringCellValue());
                                break;
                            default:
                                cell.setCellValue("");
                                break;
                        }
                    } else {
                        cell.setCellValue((String) recarr.get(cellCnt));
                    }
                    
                    if (failureColumnMap.containsKey(cellCnt) && failureColumnMap.get(cellCnt).toString().equalsIgnoreCase("Invalid")) {
                        font.setColor(HSSFColor.RED.index);
                        style.setFont(font);
                        cell.setCellStyle(style);
                    }
                }
            }

            FileOutputStream fos = new FileOutputStream(destinationDirectory + "/" + filename + ImportLog.failureTag + ext);
            wb.write(fos);
            fos.flush();
            fos.close();
        } catch (Exception ex) {
            System.out.println("\nError file write [success/failed] " + ex);
        }
    }
    
    public KwlReturnObject getExcDetailID(Map request, String currencyid, Date transactiondate)  {
        List list = new ArrayList();
        try {
            String conditionForToDate = "";
            String appDate = "";
            String companyid=(String)request.get("companyid");
            String gcurrencyid=(String)request.get("gcurrencyid");
            boolean isActivateToDateforExchangeRates = false;
            if(request.containsKey("isActivateToDateforExchangeRates") && !StringUtil.isNullObject(request.get("isActivateToDateforExchangeRates"))){
                isActivateToDateforExchangeRates=Boolean.parseBoolean(request.get("isActivateToDateforExchangeRates").toString());
            }

            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(gcurrencyid);
            params.add(currencyid);
            params.add(transactiondate);
            String applyDateQuery = "";
            
            if (!isActivateToDateforExchangeRates) {
                List applyDateList = new ArrayList();
                applyDateQuery = "select erd.applyDate from ExchangeRateDetails erd where erd.company.companyID=? and  erd.exchangeratelink.fromCurrency.currencyID = ? and erd.exchangeratelink.toCurrency.currencyID=? and  erd.applyDate <= ?  ORDER BY erd.applyDate DESC, erd.exchangeorder DESC ";
                applyDateList = executeQuery(applyDateQuery, params.toArray());
                if (applyDateList != null && !applyDateList.isEmpty()) {
                    appDate = applyDateList.get(0).toString();
                }
            }
            
            params = new ArrayList();
            params.add(companyid);
            params.add(gcurrencyid);
            params.add(currencyid);
            if (isActivateToDateforExchangeRates) {
                params.add(transactiondate);
                params.add(transactiondate);
                conditionForToDate = " and erd.toDate >= ? and erd.applyDate <= ? ";
            } 
            String erdIDQuery = "";
            if (isActivateToDateforExchangeRates) {
                erdIDQuery = "select ID from ExchangeRateDetails erd where erd.company.companyID=? and erd.exchangeratelink.fromCurrency.currencyID = ? and erd.exchangeratelink.toCurrency.currencyID=? " + conditionForToDate;
            } else {
                erdIDQuery = "select ID from ExchangeRateDetails erd where erd.applyDate='" + appDate + "' and erd.company.companyID=? and erd.exchangeratelink.fromCurrency.currencyID = ? and erd.exchangeratelink.toCurrency.currencyID=?";
            }
            list = executeQuery(erdIDQuery, params.toArray());
        } catch (ServiceException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return new KwlReturnObject(true, null, null, list, list.size());
        }
    }
    
    @Override
    public KwlReturnObject getAccountFromName(String companyid, String payee) throws ServiceException {
        List list = new ArrayList();
        try {
            String query = "select id,usedin,wanttopostje,activate from account where company=? and name=? and deleteflag='F'";
            ArrayList params = new ArrayList();
            params.add(companyid);
                params.add(payee);
            list = executeSQLQuery(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("ImportImpl.getAccountByName", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getCustomerFromName(String companyid, String payee) throws ServiceException {
        List list = new ArrayList();
        try {
            String query = "select ID from Customer where company.companyID=? and name=? ";
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(payee);
            list = executeQuery(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("ImportImpl.getCustomerFromName", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getVendorFromName(String companyid, String payee) throws ServiceException {
        List list = new ArrayList();
        try {
            String query = "select ID from Vendor where company.companyID=? and name=? ";
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(payee);
            list = executeQuery(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("ImportImpl.getVendorFromName", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getVendorFromVendorCode(String companyid, String code) throws ServiceException {
        List list = new ArrayList();
        try {
            String query = "select ID from Vendor where company.companyID=? and acccode=? ";
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(code);
            list = executeQuery(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("ImportImpl.getVendorFromVendorCode", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getPaymentMethodDetail(String paymentMethodID,String companyid) throws ServiceException {
        List list = new ArrayList();
        try {
            String query = "select detailtype,account from paymentmethod where id=? and company=? ";
            ArrayList params = new ArrayList();
            params.add(paymentMethodID);
            params.add(companyid);
            list = executeSQLQuery(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("ImportImpl.getPaymentMethodDetailType", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getBankNameMasterItemName(String companyid, String bankname) throws ServiceException{
        List list = new ArrayList();
        try {
            String query = "from MasterItem where company.companyID=? and value=? and masterGroup.ID=? ";
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(bankname);
            params.add("2");
            list = executeQuery(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("ImportImpl.getBankNameMasterItemName", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    /**
     * Get master item and its default master item id
     * @param companyid
     * @param value
     * @param groupid
     * @return
     * @throws ServiceException 
     */
    @Override
    public KwlReturnObject getMasterItemDetails(String companyid, String value, String groupid) throws ServiceException{
        List list = new ArrayList();
        try {
            String query = "select id ,defaultMasterItem.id from MasterItem where company.companyID=? and value=? and masterGroup.ID=? ";
            ArrayList params = new ArrayList();
            params.add(companyid);
            params.add(value);
            params.add(groupid);
            list = executeQuery(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("ImportImpl.getBankNameMasterItemName", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    @Override
    public String getMasterTypeID(String companyid, String value,String masterGroup) throws ServiceException{
        List list = new ArrayList();
        String masterItemId="-1";
        try {
            ArrayList QueryParams = new ArrayList();
            String queryDefaultItemsDT = "select id FROM masteritem mi WHERE mi.company=? AND mi.value = ? AND mi.masterGroup=?";
            QueryParams.add(companyid);
            QueryParams.add(value);
            QueryParams.add(masterGroup);
            list = executeSQLQuery(queryDefaultItemsDT, QueryParams.toArray());
            if (list.size() > 0) {
                if (list.get(0) != null) {
                    masterItemId = list.get(0).toString();
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("ImportImpl.getDeducteeType", ex);
        }
        return masterItemId;
    }
    @Override
    public String getMasterTypeID(String companyid, String value,String masterGroup,String code) throws ServiceException{
        List list = new ArrayList();
        String masterItemId="-1";
        try {
            ArrayList QueryParams = new ArrayList();
            String queryDefaultItemsDT = "select id FROM masteritem mi WHERE mi.company=? AND mi.code = ? AND mi.masterGroup=?";
            QueryParams.add(companyid);
            QueryParams.add(code);
            QueryParams.add(masterGroup);
            list = executeSQLQuery(queryDefaultItemsDT, QueryParams.toArray());
            if (list.size() > 0) {
                if (list.get(0) != null) {
                    masterItemId = list.get(0).toString();
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("ImportImpl.getDeducteeType", ex);
        }
        return masterItemId;
    }
    
    @Override
    public KwlReturnObject getProductTypeOfProduct(String product, String companyID) throws ServiceException {
        List list = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            params.add(companyID);
            params.add(product);
            params.add(product);
            
            String query = "select producttype.ID from Product where company.companyID=? and (productid=? or ID=?)";
            
            list = executeQuery(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("ImportImpl.getBankNameMasterItemName", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getProductCategoryMappingCount(String productID, String productCategoryID) throws ServiceException {
        KwlReturnObject result = null;
        if (!StringUtil.isNullOrEmpty(productID) && !StringUtil.isNullOrEmpty(productCategoryID)) {
            ArrayList params = new ArrayList();
            params.add(productID);
            params.add(productID);
            params.add(productCategoryID);
            String query = "select ID from ProductCategoryMapping where ( productID.ID= ? or productID.productid= ? )  and productCategory.value= ? ";
            List list = executeQuery( query, params.toArray());
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        }
        return result;
    }
    @Override
    public KwlReturnObject getCustomerAddressDetailsForCustomerByAliasName(String customerID, String aliasName, boolean isBillingAddress, String companyID) throws ServiceException {
        List list = new ArrayList();
        try {
            ArrayList params = new ArrayList();
            params.add(companyID);
            params.add(aliasName);
            params.add(isBillingAddress);
            params.add(customerID);
            
            String query = "select ID from CustomerAddressDetails where company.companyID=? and aliasName=? and isBillingAddress=? and customerID=?";
            
            list = executeQuery(query, params.toArray());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("ImportImpl.getCustomerAddressDetailsForCustomerByAliasName", ex);
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getCustomerCategoryMappingCount(String customerID, String customerCategoryID) throws ServiceException {
        KwlReturnObject result = null;
        if (!StringUtil.isNullOrEmpty(customerID) && !StringUtil.isNullOrEmpty(customerCategoryID)) {
            ArrayList params = new ArrayList();
            params.add(customerID);
            params.add(customerID);
            params.add(customerCategoryID);
            String query = "select ID from CustomerCategoryMapping where ( customerID.ID= ? or customerID.acccode= ? )  and customerCategory.value= ? ";
            List list = executeQuery(query, params.toArray());
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        }
        return result;
    }
    
    @Override
    public KwlReturnObject getVendorCategoryMappingCount(String vendorID, String vendorCategoryID) throws ServiceException {
        KwlReturnObject result = null;
        if (!StringUtil.isNullOrEmpty(vendorID) && !StringUtil.isNullOrEmpty(vendorCategoryID)) {
            ArrayList params = new ArrayList();
            params.add(vendorID);
            params.add(vendorID);
            params.add(vendorCategoryID);
            String query = "select ID from VendorCategoryMapping where ( vendorID.ID = ? or vendorID.acccode = ? )  and vendorCategory.value = ? ";
            List list = executeQuery(query, params.toArray());
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        }
        return result;
    }
    /**
     *  While importing product check Input/ Output columns Data present or not in system if Line level terms as Tax check ON
     * @param requestParams
     * @return
     * @throws ServiceException 
     */
    @Override
    public List<String> getLineLevelTermPresentByName(Map<String, Object> requestParams) throws ServiceException {
        List<String> LineLevelTerms = new ArrayList<String>();
        try {
            if (requestParams.containsKey("colData") && !StringUtil.isNullOrEmpty(requestParams.get("companyid").toString())) {

                // Check Sales and Purchas tax name in separate.
                Boolean isOutputTax = null;
                if (requestParams.containsKey("isOutputTax") && requestParams.get("isOutputTax") !=null && !StringUtil.isNullOrEmpty(requestParams.get("isOutputTax").toString())) {
                    isOutputTax = (Boolean) requestParams.get("isOutputTax");
                }

                String[] termSplits = requestParams.get("colData").toString().split(","); // Input/ Output terms data present in seperated by coma. getting this data to check exists of terms
                for (int i = 0; i < termSplits.length; i++) {
                    List<Object> queryPrams = new ArrayList<Object>();
                    String termSplit = termSplits[i];
                    String selQuery = "select id from LineLevelTerms where company.companyID=? and term=?";
                    queryPrams.add(requestParams.get("companyid"));
                    queryPrams.add(termSplit);
                    if (isOutputTax != null) {
                        selQuery += " and salesOrPurchase=? ";
                        queryPrams.add(isOutputTax);
                    }
                    List returnList = executeQuery(selQuery, queryPrams.toArray());
                    if (returnList.isEmpty()) { //IF given input name is not presenet then add this into list for making current import product record invlaid
                        LineLevelTerms.add(termSplit);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("get Line Level Term Present in System : " + ex.getMessage(), ex);
        }
        return LineLevelTerms;
    }
    
    @Override
    public KwlReturnObject getGoodsReceiptOrderCount(String orderno, String companyid) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String q = "from GoodsReceiptOrder where goodsReceiptOrderNumber=? and company.companyID=?";
        list = executeQuery(q, new Object[]{orderno, companyid});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject getDeliveryOrderCount(String orderno, String companyid) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String q = "from DeliveryOrder where deliveryOrderNumber=? and company.companyID=?";
        list = executeQuery(q, new Object[]{orderno, companyid});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
     @Override
    public KwlReturnObject getVATCommodityCodeByName(Map<String,String> mapData) throws ServiceException { //for selecting a locked quantity of inventory product used in workorder
        
        List returnList = new ArrayList();
        String vatcommodityname=mapData.get("vatcommodityname"),company=mapData.get("company");
        try {
            String query = "SELECT id FROM masteritem WHERE mastergroup='42' AND value = ? AND company = ? ";
            if(!StringUtil.isNullOrEmpty(vatcommodityname) && !StringUtil.isNullOrEmpty(company)){
             returnList = executeSQLQuery(query, new Object[]{vatcommodityname,company});
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getWOLockQuantity : " + ex.getMessage(), ex);
        }
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    
    @Override
      public KwlReturnObject getFinancialYearDates(Map<String, Object> filterParams) throws ServiceException {
        List returnList = new ArrayList();
        ArrayList params = new ArrayList();
        String condition = "";
       
        /*
         Taken fyfrom instead of firstfyfrom becuase of "fyfrom" used most of the time in whole project
        */
        String query = " select fyfrom,bbfrom from compaccpreferences ";

        if (filterParams.containsKey("id")) {
            condition += (condition.length() == 0 ? " where " : " and ") + "id=?";
            params.add(filterParams.get("id"));
        }
        query += condition;
            returnList = executeSQLQuery( query, params.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    
     @Override
    public KwlReturnObject getStoreMasters(HashMap<String, Object> requestParams) throws ServiceException {
        String query = "from StoreMaster";
        return buildNExecuteQuery(query, requestParams);
    }

    @Override
    public boolean isExciseActivated(String companyId) throws ServiceException {
        boolean isExciseActivated = false;
        String query = "select exciseApplicable from ExtraCompanyPreferences where company.companyID=?";
        List prm = new ArrayList();
        prm.add(companyId);

        List ftchList = executeQuery( query, prm.toArray());
        if (ftchList != null && !ftchList.isEmpty()) {
            isExciseActivated = (Boolean) ftchList.get(0);
}
        return isExciseActivated;
    }
    /**
     * Method to check whether the transaction falls between the active date range period set for the company.
     *
     * @param orderDate Transaction Date
     * @param companyId
     * @return isInActiveDatePeriod 
     * @throws ServiceException
     */
    @Override
    public boolean checkActiveDateRange(Date orderDate, String companyId) throws ServiceException {
        List ll = new ArrayList();
        boolean isInActiveDatePeriod = true;
        try {
            String sql = "select if(activefromdate <=? and activetodate >= ?  ,'true','false') as isactive from extracompanypreferences where id = ? and activefromdate is not null";
            ll = executeSQLQuery( sql, new Object[]{orderDate,orderDate, companyId});
            if (ll.size() > 0 && ll.get(0) !=null) {
                Object obj = (Object) ll.get(0);
                String inActiveDatePeriod = (String) obj;
                isInActiveDatePeriod = Boolean.parseBoolean(inActiveDatePeriod);
            }
        } catch (Exception e) {
            Logger.getLogger(ImportImpl.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("ImportImpl.checkActiveDateRange", e);
        }
        return isInActiveDatePeriod;
    }
    
    /**
     * Method to get Column Mapping data for import with script
     *
     * @param paramJobj
     * @return KwlReturnObject with list of ImportFileColumnMapping class objects
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getImportFileColumnMapping(JSONObject paramJobj) throws ServiceException {
        StringBuilder conditionQuery = new StringBuilder("from ImportFileColumnMapping ifcm where ");
        List<Object> paramList = new ArrayList<>();
        conditionQuery.append(" ifcm.company.companyID = ? ");
        paramList.add(paramJobj.optString(Constants.companyKey));
        if (paramJobj.has(Constants.moduleid)) {
            String moduleid = paramJobj.optString(Constants.moduleid);
            conditionQuery.append(" and ifcm.module.id = ? ");
            paramList.add(moduleid);
        }
        if (paramJobj.has("fileHeader")) {
            String fileHeader = paramJobj.optString("fileHeader");
            conditionQuery.append(" and ifcm.fileHeader = ? ");
            paramList.add(fileHeader);
        }
        //Please refer to class ImportFileColumnMapping.java for reason behind the logic below
        List<Integer> fieldMappingTypesList = new ArrayList();
        if (paramJobj.optBoolean("isForFieldsMapping", false)) {
            fieldMappingTypesList.add(0);
            fieldMappingTypesList.add(1);
        }
        if (paramJobj.optBoolean("isForDefaultValues", false)) {
            fieldMappingTypesList.add(2);
            fieldMappingTypesList.add(3);
        }
        if (paramJobj.optBoolean("isForDateFormat", false)) {
            fieldMappingTypesList.add(4);
        }
        if (!fieldMappingTypesList.isEmpty()) {
            conditionQuery.append(" and ifcm.fieldMappingType in (");
            conditionQuery.append(StringUtil.join(",", fieldMappingTypesList));
            conditionQuery.append(") ");
        }
        List returnList = executeQuery(conditionQuery.toString(), paramList.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    
    /**
     * Method to get details of file for import with script
     *
     * @param paramJobj
     * @return KwlReturnObject with list of ImportFileDetails class objects
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject getImportFileDetails(JSONObject paramJobj) throws ServiceException {
        StringBuilder conditionQuery = new StringBuilder("from ImportFileDetails ifd where ");
        List<Object> paramList = new ArrayList<>();
        conditionQuery.append(" ifd.company.companyID = ? ");
        paramList.add(paramJobj.optString(Constants.companyKey));
        if (paramJobj.has(Constants.moduleid)) {
            String moduleid = paramJobj.optString(Constants.moduleid);
            conditionQuery.append(" and ifd.module.id = ? ");
            paramList.add(moduleid);
        }
        List returnList = executeQuery(conditionQuery.toString(), paramList.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    
    /**
     * Method to save or update details of file for import with script
     *
     * @param paramJobj
     * @throws ServiceException
     */
    @Override
    public void saveOrUpdateImportFileDetails(JSONObject paramJobj) throws ServiceException {
        ImportFileDetails importFileDetails = new ImportFileDetails();
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("ID"))) {
            importFileDetails = (ImportFileDetails) get(ImportFileDetails.class, paramJobj.optString("ID"));
        }
        if (paramJobj.has(Constants.companyKey)) {
            Company company = (Company) get(Company.class, paramJobj.optString(Constants.companyKey));
            importFileDetails.setCompany(company);
        }
        if (paramJobj.has(Constants.moduleid)) {
            Modules module = (Modules) get(Modules.class, paramJobj.optString(Constants.moduleid));
            importFileDetails.setModule(module);
        }
        if (paramJobj.has("fileName")) {
            importFileDetails.setFileName(paramJobj.optString("fileName"));
        }
        if (paramJobj.has("fileNameSuffixDateFormat")) {
            importFileDetails.setFileNameSuffixDateFormat(paramJobj.optString("fileNameSuffixDateFormat"));
        }
        if (paramJobj.has("serverUrl")) {
            importFileDetails.setServerUrl(paramJobj.optString("serverUrl"));
        }
        if (paramJobj.has("serverPort")) {
            importFileDetails.setServerPort(paramJobj.optInt("serverPort"));
        }
        if (paramJobj.has("subDirectory")) {
            importFileDetails.setSubDirectory(paramJobj.optString("subDirectory"));
        }
        if (paramJobj.has("userName")) {
            importFileDetails.setUserName(paramJobj.optString("userName"));
        }
        if (paramJobj.has("passKey")) {
            importFileDetails.setPassKey(paramJobj.optString("passKey"));
        }
        saveOrUpdate(importFileDetails);
    }
    /*
     * this method returns fieldcomboID when  companyId,moduleId,fieldValue,fieldName from fieldParams is provided.
     */
    public String getValuesForLinkedRecords(JSONObject paramJobj) throws JSONException,ServiceException{
        
         List list = null;
         String companyId="";
         String fieldValue="";
         String moduleId = "",fieldName="";
         String result="";
         ArrayList params = new ArrayList();
        if(paramJobj.has("companyId")){
            companyId=paramJobj.getString("companyId");            
        }
        if(paramJobj.has("moduleId")){
            moduleId=paramJobj.getString("moduleId");            
        }
        if(paramJobj.has("fieldValue")){
            fieldValue=paramJobj.getString("fieldValue");            
        }
        if(paramJobj.has("fieldName")){
            fieldName=paramJobj.getString("fieldName");            
        }
        String sqlQuery="SELECT fc.id FROM fieldcombodata fc INNER JOIN fieldparams fp on fp.id=fc.fieldid WHERE fp.moduleid=? AND fp.companyid=? AND fp.fieldname=? and fc.`value`=?";
        params.add(moduleId);
        params.add(companyId);
        params.add(fieldName);
        params.add(fieldValue);
        list = executeSQLQuery( sqlQuery, params.toArray());                            
        if (list != null && !list.isEmpty()) {
            result = list.get(0).toString();
        }
        return result;
    }
    
    /*
     * this method returns custom data values for "Entity" from multientitydimesioncustomdata
     */
    public String getEntityCustomData(JSONObject entityDetails) throws ServiceException,JSONException{    
        String comboId="",result="",colNum="",companyID="",sqlQuery="";        
        int xType=0;
        List list = null;
        ArrayList params = new ArrayList();
        if(entityDetails.has("comboId")){
            comboId=entityDetails.getString("comboId");
        }
        if(entityDetails.has(String.valueOf("xType"))){
            xType=Integer.parseInt(entityDetails.getString("xType"));
        }
        if(entityDetails.has("colNum")){
            colNum="col"+entityDetails.getString("colNum");
        }
        if(entityDetails.has("companyId")){
            companyID=entityDetails.getString("companyId");
        }
        if(xType==4){
            sqlQuery = "select fc.`value` from fieldcombodata fc inner join multientitydimesioncustomdata mde on mde."+colNum+"=fc.id where mde.fcdid=? and mde.company=?";    
        }else{
            sqlQuery = "select "+colNum+" from multientitydimesioncustomdata where fcdid=? and company=?";
        }
        params.add(comboId);
        params.add(companyID);    
        list = executeSQLQuery( sqlQuery, params.toArray());                            
        if (list != null && !list.isEmpty()) {
            result = list.get(0).toString();
        }
        return  result;
    }
    
    /**
     * Method to Check weather the data entered in custom column in excel is
     * present in fieldcombodata table.
     *
     * @param paramsJobj
     * @return KwlReturnObject with list of count
     * @throws ServiceException
     */
    @Override
    public KwlReturnObject isCustomDataPresent(JSONObject paramsJobj) throws ServiceException {
        String conditionQuery = " SELECT count(fcb.id) FROM fieldcombodata AS fcb INNER JOIN fieldparams AS fp ON fp.id = fcb.fieldid "
                + " WHERE fp.fieldlabel = ? AND fcb.value = ? AND fp.companyid = ? ";
        List<Object> paramList = new ArrayList<>();
        paramList.add(paramsJobj.optString("fieldlabel", ""));
        paramList.add(paramsJobj.optString("fieldvalue", ""));
        paramList.add(paramsJobj.optString(Constants.companyKey, ""));
        List returnList = executeSQLQuery(conditionQuery, paramList.toArray());
        return new KwlReturnObject(true, "", null, returnList, returnList.size());
    }
    
    /**
     * Method to get product currency name
     *
     * @param companyId
     * @param productId
     * @return currency name
     * @throws ServiceException
     */
    @Override
    public String getProductCurrencyName(String companyId,String productId) throws ServiceException {
        String query = " SELECT p.currency.name FROM Product p WHERE p.ID=? AND p.company.companyID=?";
        List<Object> paramList = new ArrayList<>();

        paramList.add(productId);
        paramList.add(companyId);

        List list = executeQuery(query, paramList.toArray());
        return (String) list.get(0);
    }
    
    @Override
    public boolean isValidDisplayUOM(String uomSchemaType, String displayUOM, String companyId) throws ServiceException{
        boolean result = false;
        if (!StringUtil.isNullOrEmpty(uomSchemaType) && !StringUtil.isNullOrEmpty(displayUOM)) {
            ArrayList params = new ArrayList();
            params.add(companyId);
            params.add(uomSchemaType);
            params.add(displayUOM);
            params.add(displayUOM);
            String query = "from UOMSchema us LEFT JOIN us.purchaseuom LEFT JOIN us.salesuom where us.company.companyID=? and us.uomschematype.name=? and ((us.purchaseuom.name=? AND us.purchaseuom.ID<>us.baseuom.ID) OR (us.salesuom.name=? AND us.salesuom.ID<>us.baseuom.ID))";
            List list = executeQuery( query, params.toArray());
            result = list.size()>0;
        }
        return result;
    }
    
    @Override
    public KwlReturnObject getAccountDetailsFromAccountId(String accountid) throws ServiceException{
        String query = "select groupname, activate, usedin, wanttopostje,currency,mastertypeid from account where id = ?";
        List list = executeSQLQuery(query, accountid);
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getDocumentDetailsFromDocumentNo(String documentNo,String companyId,String mode,String doctype)throws ServiceException{
        List list = null;
        ArrayList params = new ArrayList();
        String query = "";
        if(mode.equalsIgnoreCase(Constants.IMPORT_PAYMENT)){
            if(doctype.equalsIgnoreCase(Constants.IMPORT_INVOICE)){
                query = "select currency from goodsreceipt where company = ? and grnumber = ?";
            } else if(doctype.equalsIgnoreCase(Constants.IMPORT_CN)){
                query = "select currency from creditnote where company = ? and cnnumber = ?";
            }
        } else if (mode.equalsIgnoreCase(Constants.IMPORT_RECEIPT)){
            if(doctype.equalsIgnoreCase(Constants.IMPORT_INVOICE)){
                query = "select currency from invoice where company = ? and invoicenumber = ?";
            } else if(doctype.equalsIgnoreCase(Constants.IMPORT_DN)){
                query = "select currency from debitnote where company = ? and dnnumber = ?";
            }
        }
        params.add(companyId);
        params.add(documentNo);
        if(!StringUtil.isNullOrEmpty(query)){
            list = executeSQLQuery(query, params.toArray());
        }
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    /**
     * Get Extra company pref. 
     * @param companyId
     * @return
     * @throws ServiceException 
     */
    @Override
    public List getExtraCompanyPref(String companyId) throws ServiceException {
        List list = null;
        ArrayList params = new ArrayList();
        String query = " select isnewgst, columnpref from extracompanypreferences where id= ? ";
        params.add(companyId);
        if(!StringUtil.isNullOrEmpty(query)){
            list = executeSQLQuery(query, params.toArray());
        }
        return list;
    }
     public void saveorupdateObject(ImportLog implog) throws ServiceException{
        saveOrUpdate(implog);
    }
    /**
     * * Function to get GST fields used in documents
     *
     * @param reqMap
     * @return
     * @throws ServiceException
     */
    public List getGstCustomerUsedHistory(Map<String, Object> reqMap) throws ServiceException {
        List params = new ArrayList();
        String SIselectcol = " select gdh.id,MAX(inv.creationdate) as invmaxdate from gstdocumenthistory gdh ";
        String SIjoinsql = " inner join invoice inv on inv.id=gdh.refdocid "
                + " inner join customer c on c.id=inv.customer ";
        String SIcondition = "";
       
        if (reqMap.containsKey("customerid")) {
            params.add((String) reqMap.get("customerid"));
            SIcondition += " where c.id=? ";
        }
        if (reqMap.containsKey("companyid")) {
            params.add((String) reqMap.get("companyid"));
            SIcondition += " and inv.company=? ";
        }
        if (reqMap.containsKey("applyDate") && reqMap.get("applyDate") != null) {
            Date applyDate = (Date) reqMap.get("applyDate");
            SIcondition += " and DATE(inv.creationdate)>=DATE(?) ";
            params.add(applyDate);
        }
        String having=" having invmaxdate is not null ";
        String SIquery = SIselectcol + SIjoinsql + SIcondition + having;
        String CNselectcol = " select gdh.id,MAX(cn.creationdate) as invmaxdate from gstdocumenthistory gdh ";
        String CNjoinsql = " inner join creditnote cn on cn.id=gdh.refdocid inner join customer c on c.id=cn.customer ";
        String CNcondition = "";
       
        if (reqMap.containsKey("customerid")) {
            params.add((String) reqMap.get("customerid"));
            CNcondition += " where c.id=? ";
        }
        if (reqMap.containsKey("companyid")) {
            params.add((String) reqMap.get("companyid"));
            CNcondition += " and cn.company=? ";
        }
        if (reqMap.containsKey("applyDate") && reqMap.get("applyDate") != null) {
            Date applyDate = (Date) reqMap.get("applyDate");
            CNcondition += " and DATE(cn.creationdate)>=DATE(?) ";
            params.add(applyDate);
        }   
        String CNquery = CNselectcol + CNjoinsql + CNcondition + having;
        String DNselectcol = " select gdh.id,MAX(dn.creationdate) as invmaxdate from gstdocumenthistory gdh ";
        String DNjoinsql = " inner join debitnote dn on dn.id=gdh.refdocid inner join customer c on c.id=dn.customer ";
        String DNcondition = "";
       
        if (reqMap.containsKey("customerid")) {
            params.add((String) reqMap.get("customerid"));
            DNcondition += " where c.id=? ";
        }
        if (reqMap.containsKey("companyid")) {
            params.add((String) reqMap.get("companyid"));
            DNcondition += " and dn.company=? ";
        }
        if (reqMap.containsKey("applyDate") && reqMap.get("applyDate") != null) {
            Date applyDate = (Date) reqMap.get("applyDate");
            DNcondition += " and DATE(dn.creationdate)>=DATE(?) ";
            params.add(applyDate);
        }   
        String DNquery = DNselectcol + DNjoinsql + DNcondition + having;
        String SOselectcol = " select gdh.id,MAX(so.orderdate) as invmaxdate from gstdocumenthistory gdh ";
        String SOjoinsql = " inner join salesorder so on so.id=gdh.refdocid inner join customer c on c.id=so.customer ";
        String SOcondition = "";
       
        if (reqMap.containsKey("customerid")) {
            params.add((String) reqMap.get("customerid"));
            SOcondition += " where c.id=? ";
        }
        if (reqMap.containsKey("companyid")) {
            params.add((String) reqMap.get("companyid"));
            SOcondition += " and so.company=? ";
        }
        if (reqMap.containsKey("applyDate") && reqMap.get("applyDate") != null) {
            Date applyDate = (Date) reqMap.get("applyDate");
            SOcondition += " and DATE(so.orderdate)>=DATE(?) ";
            params.add(applyDate);
        }   
        String SOquery = SOselectcol + SOjoinsql + SOcondition + having;
        String RPselectcol = " select gdh.id,MAX(r.creationdate) as invmaxdate from gstdocumenthistory gdh ";
        String RPjoinsql = " inner join receipt r on r.id=gdh.refdocid inner join customer c on c.id=r.customer ";
        String RPcondition = "";
       
        if (reqMap.containsKey("customerid")) {
            params.add((String) reqMap.get("customerid"));
            RPcondition += " where c.id=? ";
        }
        if (reqMap.containsKey("companyid")) {
            params.add((String) reqMap.get("companyid"));
            RPcondition += " and r.company=? ";
        }
        if (reqMap.containsKey("applyDate") && reqMap.get("applyDate") != null) {
            Date applyDate = (Date) reqMap.get("applyDate");
            RPcondition += " and DATE(r.creationdate)>=DATE(?) ";
            params.add(applyDate);
        }   
        String RPquery = RPselectcol + RPjoinsql + RPcondition + having;
        String orderby=" order by invmaxdate DESC ";
        String union =" union ";    
        String allModuleQurey= SIquery + union + CNquery + union + DNquery + union + SOquery + union + RPquery + orderby; 
        List list = executeSQLQuery(allModuleQurey, params.toArray());
        return list;
    }
    /**
     * * Function to get GST fields used in documents
     *
     * @param reqMap
     * @return
     * @throws ServiceException
     */
    public List getGstVendorUsedHistory(Map<String, Object> reqMap) throws ServiceException {
        List params = new ArrayList();
        String PIselectcol = " select gdh.id,MAX(gr.creationdate) as invmaxdate from gstdocumenthistory gdh ";
        String PIjoinsql = " inner join goodsreceipt gr on gr.id=gdh.refdocid "
                + " inner join vendor v on v.id=gr.vendor ";
        String PIcondition = "";
        if (reqMap.containsKey("vendorid")) {
            params.add((String) reqMap.get("vendorid"));
            PIcondition += " where v.id=? ";
        }
        if (reqMap.containsKey("companyid")) {
            params.add((String) reqMap.get("companyid"));
            PIcondition += " and gr.company=? ";
        }
        if (reqMap.containsKey("applyDate") && reqMap.get("applyDate") != null) {
            Date applyDate = (Date) reqMap.get("applyDate");
            PIcondition += " and DATE(gr.creationdate)>=DATE(?) ";
            params.add(applyDate);
        }
        String having=" having invmaxdate is not null ";
        String PIquery = PIselectcol + PIjoinsql + PIcondition + having;
        String CNselectcol = " select gdh.id,MAX(cn.creationdate) as invmaxdate from gstdocumenthistory gdh ";
        String CNjoinsql = " inner join creditnote cn on cn.id=gdh.refdocid inner join vendor v on v.id=cn.vendor ";
        String CNcondition = "";    
        if (reqMap.containsKey("vendorid")) {
            params.add((String) reqMap.get("vendorid"));
            CNcondition += " where v.id=? ";
        }
        if (reqMap.containsKey("companyid")) {
            params.add((String) reqMap.get("companyid"));
            CNcondition += " and cn.company=? ";
        }
        if (reqMap.containsKey("applyDate") && reqMap.get("applyDate") != null) {
            Date applyDate = (Date) reqMap.get("applyDate");
            CNcondition += " and DATE(cn.creationdate)>=DATE(?) ";
            params.add(applyDate);
        }   
        String CNquery = CNselectcol + CNjoinsql + CNcondition + having;
        String DNselectcol = " select gdh.id,MAX(dn.creationdate) as invmaxdate from gstdocumenthistory gdh ";
        String DNjoinsql = " inner join debitnote dn on dn.id=gdh.refdocid inner join vendor v on v.id=dn.vendor ";
        String DNcondition = "";    
        if (reqMap.containsKey("vendorid")) {
            params.add((String) reqMap.get("vendorid"));
            DNcondition += " where v.id=? ";
        }
        if (reqMap.containsKey("companyid")) {
            params.add((String) reqMap.get("companyid"));
            DNcondition += " and dn.company=? ";
        }
        if (reqMap.containsKey("applyDate") && reqMap.get("applyDate") != null) {
            Date applyDate = (Date) reqMap.get("applyDate");
            DNcondition += " and DATE(dn.creationdate)>=DATE(?) ";
            params.add(applyDate);
        }   
        String DNquery = DNselectcol + DNjoinsql + DNcondition + having;
        String POselectcol = " select gdh.id,MAX(po.orderdate) as invmaxdate from gstdocumenthistory gdh ";
        String POjoinsql = " inner join purchaseorder po on po.id=gdh.refdocid inner join vendor v on v.id=po.vendor ";
        String POcondition = ""; 
        if (reqMap.containsKey("vendorid")) {
            params.add((String) reqMap.get("vendorid"));
            POcondition += " where v.id=? ";
        }
        if (reqMap.containsKey("companyid")) {
            params.add((String) reqMap.get("companyid"));
            POcondition += " and po.company=? ";
        }
        if (reqMap.containsKey("applyDate") && reqMap.get("applyDate") != null) {
            Date applyDate = (Date) reqMap.get("applyDate");
            POcondition += " and DATE(po.orderdate)>=DATE(?) ";
            params.add(applyDate);
        }   
        String POquery = POselectcol + POjoinsql + POcondition + having;
        String MPselectcol = " select gdh.id,MAX(p.creationdate) as invmaxdate from gstdocumenthistory gdh ";
        String MPjoinsql = " inner join payment p on p.id=gdh.refdocid inner join vendor v on v.id=p.vendor ";
        String MPcondition = "";       
        if (reqMap.containsKey("vendorid")) {
            params.add((String) reqMap.get("vendorid"));
            MPcondition += " where v.id=? ";
        }
        if (reqMap.containsKey("companyid")) {
            params.add((String) reqMap.get("companyid"));
            MPcondition += " and p.company=? ";
        }
        if (reqMap.containsKey("applyDate") && reqMap.get("applyDate") != null) {
            Date applyDate = (Date) reqMap.get("applyDate");
            MPcondition += " and DATE(p.creationdate)>=DATE(?) ";
            params.add(applyDate);
        }   
        String MPquery = MPselectcol + MPjoinsql + MPcondition + having;
        String orderby=" order by invmaxdate DESC ";
        String union =" union ";    
        String allModuleQurey= PIquery + union + CNquery + union + DNquery + union + POquery + union + MPquery + orderby; 
        List list = executeSQLQuery(allModuleQurey, params.toArray());
        return list;
    }

    /**
     * method checks the cheque number is already in database for given sequence format or not.
     * @param hm
     * @return
     * @throws com.krawler.common.service.ServiceException
     */
    @Override
    public boolean isChequeNumberAvailable(Map hm) throws ServiceException {
        boolean isChequeNumberAvailable = false;
        JSONObject jobj = new JSONObject();
        String condition = "";
        ArrayList params = new ArrayList();
        try {
            if (hm.containsKey("companyId")) {
                String companyId = (String) hm.get("companyId");
                condition += " and ch.company=? ";
                params.add(companyId);
            }
            if (hm.containsKey("bankAccountId")) {
                String bankAccountId = (String) hm.get("bankAccountId");
                condition += " and ch.bankaccount=? ";
                params.add(bankAccountId);
            }
            if (hm.containsKey("nextChequeNumber")) {
                String nextChequeNumber = (String) hm.get("nextChequeNumber");
                params.add(nextChequeNumber);
                condition += " and ch.chequeno=? ";
            }

            if (hm.containsKey("chequesequenceformatid") && hm.get("chequesequenceformatid") != null && !StringUtil.isNullOrEmpty(hm.get("chequesequenceformatid").toString())) {
                String sequenceformatid = (String) hm.get("chequesequenceformatid");
                params.add(sequenceformatid);
                condition += " and ( ch.seqformat=? ) ";
            }

            String query = "select chequeno,sequencenumber from cheque ch "
                    + "WHERE (ch.createdfrom=1 or ch.createdfrom=3) and ch.deleteflag=false" + condition;

            List list = executeSQLQuery( query, params.toArray());
            if (list != null && !list.isEmpty() && list.get(0) != null) {
                isChequeNumberAvailable = true;
            }
        } catch (ServiceException e) {
            throw ServiceException.FAILURE("ImportImpl.isChequeNumberAvailable", e);
        }
        return isChequeNumberAvailable;
    }

    @Override
    public KwlReturnObject getCompanyAccountPreferences(String companyId) throws ServiceException {
        KwlReturnObject result = null;
        if (!StringUtil.isNullOrEmpty(companyId)) {
            ArrayList params = new ArrayList();
            params.add(companyId);
            String query = "select chequeNoDuplicate from compaccpreferences where id=?";
            List list = executeSQLQuery(query, params.toArray());
            int totalCount = list.size();
            result = new KwlReturnObject(true, null, null, list, totalCount);
        }
        return result;
        
    }
    
    @Override
    public KwlReturnObject getGoodsReceiptData(String GRNumber, String companyid, String vendorId) throws ServiceException{
        String query = "SELECT creationDate FROM GoodsReceipt WHERE goodsReceiptNumber=? and company.companyID=? and vendor.ID = ? and approvestatuslevel=11 and deleted=false";
        List list = executeQuery(query, new Object[]{GRNumber, companyid, vendorId});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getInvoiceData(String invoiceNumber, String companyid, String customerID) throws ServiceException{
        String query = "SELECT creationDate FROM Invoice WHERE invoiceNumber=? and company.companyID=? and customer.ID=? and approvestatuslevel=11 and deleted=false";
        List list = executeQuery(query, new Object[]{invoiceNumber, companyid, customerID});
        return new KwlReturnObject(true, "", null, list, list.size());
    }
    
    @Override
    public KwlReturnObject getRefundNameCountForPayment(String refundNo, String companyid, String customerID) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String q = "SELECT id, receipt.currency.currencyCode, amount,amountDue, receipt.creationDate FROM ReceiptAdvanceDetail WHERE receipt.receiptNumber=? AND receipt.company.companyID =? AND receipt.customer.ID =? AND amountDue > 0 AND receipt.approvestatuslevel=11 and receipt.deleted=false";
        list = executeQuery(q, new Object[]{refundNo, companyid, customerID});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject getRefundNameCountForReceipt(String refundNo, String companyid,String vendorId) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String q = "SELECT id,payment.currency.currencyCode,amount,amountDue,payment.creationDate FROM AdvanceDetail WHERE payment.paymentNumber=? and payment.company.companyID=? and payment.vendor.ID=? and amountDue>0 AND payment.approvestatuslevel=11 and payment.deleted=false";
        list = executeQuery( q, new Object[]{refundNo, companyid,vendorId});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject getCustomerCreditNoCount(String creditNoteNo, String companyid, String customerId) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String q = "SELECT creationDate FROM CreditNote WHERE creditNoteNumber=? and company.companyID=? and customer.ID=? and approvestatuslevel=11 and deleted=false";
        list = executeQuery(q, new Object[]{creditNoteNo, companyid, customerId});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }

    @Override
    public KwlReturnObject getVendorCreditNoCount(String creditNoteNo, String companyid, String vendorId) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String q = "SELECT creationDate FROM CreditNote WHERE creditNoteNumber=? and company.companyID=? and vendor.ID=? and approvestatuslevel=11 and deleted=false";
        list = executeQuery(q, new Object[]{creditNoteNo, companyid, vendorId});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject getCustomerDebitNoCount(String debitNo, String companyid,String customerId ) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String q = "SELECT creationDate FROM DebitNote WHERE debitNoteNumber=? and company.companyID=? and customer.ID=? and approvestatuslevel=11 and deleted=false";
        list = executeQuery( q, new Object[]{debitNo, companyid,customerId});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject getVendorDebitNoCount(String debitNo, String companyid,String vendorId ) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String q = "SELECT creationDate FROM DebitNote WHERE debitNoteNumber=? and company.companyID=? and vendor.ID=? and approvestatuslevel=11 and deleted=false";
        list = executeQuery( q, new Object[]{debitNo, companyid,vendorId});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
    
    @Override
    public KwlReturnObject getStoreDetails(String storeId) throws ServiceException {
        List list = new ArrayList();
        int count = 0;
        String q = "SELECT type FROM in_storemaster WHERE id=?";
        list = executeSQLQuery( q, new Object[]{storeId});
        count = list.size();
        return new KwlReturnObject(true, "", null, list, count);
    }
}
