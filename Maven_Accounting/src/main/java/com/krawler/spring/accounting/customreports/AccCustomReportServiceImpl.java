/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.customreports;

import com.krawler.acc.savedsearch.dao.SavedSearchDAO;
import com.krawler.common.admin.ModuleCategory;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.creditnote.accCreditNoteServiceCMN;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignerConstants;
import com.krawler.spring.accounting.debitnote.accDebitNoteService;
import com.krawler.spring.accounting.goodsreceipt.AccGoodsReceiptServiceHandler;
import com.krawler.hql.accounting.companypreferenceservice.CompanyReportConfigurationServiceImpl;
import com.krawler.spring.accounting.goodsreceipt.AccGoodsReceiptServiceDAO;
import com.krawler.utils.json.base.XML;
import com.lowagie.text.pdf.PdfName;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.xml.crypto.dsig.keyinfo.KeyValue;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptCMN;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.invoice.AccInvoiceServiceDAO;
import com.krawler.spring.accounting.invoice.accInvoiceCMN;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.purchaseorder.AccPurchaseOrderServiceDAO;
import com.krawler.spring.accounting.salesorder.AccSalesOrderServiceDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.term.accTermDAO;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.jdbc.InvalidResultSetAccessException;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;

/**
 *
 * @author krawler
 */
public class AccCustomReportServiceImpl implements AccCustomReportService {

    private AccCustomReportsDAO accCustomerReportServiceDao;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accGoodsReceiptCMN accGoodsReceiptCMN;
    private accInvoiceCMN accInvoiceCommon;
    private AccPurchaseOrderServiceDAO accPurchaseOrderServiceDAOobj;
    private fieldDataManager fieldDataManager;
    private permissionHandlerDAO permissionHandlerDAOObj;
    private AccInvoiceServiceDAO accInvoiceServiceDAO;
    private AccSalesOrderServiceDAO accSalesOrderServiceDAOobj;
    private accInvoiceDAO accInvoiceDAOobj;
    private accTermDAO accTermObj;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAO;
    private AccGoodsReceiptServiceHandler accGoodsReceiptServiceHandler;
    private accCurrencyDAO accCurrencyDAOobj;
    private accAccountDAO accAccountDAOobj;
    private accTaxDAO accTaxObj;
    
    private SavedSearchDAO saveSearchDAOobj;
    
    public void setAccGoodsReceiptServiceHandler(AccGoodsReceiptServiceHandler accGoodsReceiptServiceHandler) {
        this.accGoodsReceiptServiceHandler = accGoodsReceiptServiceHandler;
    }
    
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }

    public void setAccGoodsReceiptServiceDAO(AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAO) {
        this.accGoodsReceiptServiceDAO = accGoodsReceiptServiceDAO;
    }
        
    public void setAccCustomerReportServiceDao(AccCustomReportsDAO accCustomerReportServiceDao) {
        this.accCustomerReportServiceDao = accCustomerReportServiceDao;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setaccGoodsReceiptCMN(accGoodsReceiptCMN accGoodsReceiptCMN) {
        this.accGoodsReceiptCMN = accGoodsReceiptCMN;
    }

    public void setAccInvoiceCommon(accInvoiceCMN accInvoiceCommon) {
        this.accInvoiceCommon = accInvoiceCommon;
    }

    public void setaccPurchaseOrderServiceDAO(AccPurchaseOrderServiceDAO accPurchaseOrderServiceDAOobj) {
        this.accPurchaseOrderServiceDAOobj = accPurchaseOrderServiceDAOobj;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManager) {
        this.fieldDataManager = fieldDataManager;
    }

    public void setpermissionHandlerDAO(permissionHandlerDAO permissionHandlerDAOObj) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj;
    }

    public void setaccInvoiceServiceDAO(AccInvoiceServiceDAO accInvoiceServiceDAO) {
        this.accInvoiceServiceDAO = accInvoiceServiceDAO;
    }

    public void setAccSalesOrderServiceDAOobj(AccSalesOrderServiceDAO accSalesOrderServiceDAOobj) {
        this.accSalesOrderServiceDAOobj = accSalesOrderServiceDAOobj;
    }
    
    public void setAccInvoiceDAOobj(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
    
    public void setaccTermDAO(accTermDAO accTermObj) {
        this.accTermObj = accTermObj;
    }

    public void setAccGoodsReceiptobj(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }
    
    public void setSavedSearchDAO(SavedSearchDAO saveSearchDAOobj) {
        this.saveSearchDAOobj = saveSearchDAOobj;
    }
    
    /**
     * This method returns the accounts from Account table.
     *
     * @param companyId
     * @return KwlReturnObject.
     * @exception ServiceException .
     * @see ServiceException
     */
    @Override
    public JSONObject getModuleCategories(Map<String,Object> requestParams) throws ServiceException {
        JSONObject jresult = new JSONObject();
        String companyId = (String)requestParams.get("companyID");
        boolean isPivot = false;
        if(!StringUtil.isNullOrEmpty((String)requestParams.get("isPivot"))) {
            isPivot = Boolean.parseBoolean((String)requestParams.get("isPivot"));
        }
        KwlReturnObject moduleCategories = accCustomerReportServiceDao.getModuleCategories(companyId, isPivot);
        if (moduleCategories != null) {
            List<ModuleCategory> moduleCategoryList = moduleCategories.getEntityList();
            try {
                for (ModuleCategory moduleCat : moduleCategoryList) {
                    JSONObject jobj = new JSONObject();
                    jobj.put("moduleCatId", moduleCat.getModuleCatId());
                    jobj.put("moduleCatName", moduleCat.getModuleCatName());
                    jresult.append("data", jobj);
                }
            } catch (JSONException ex) {
                Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return jresult;
    }

    /**
     * This method returns the module id and module name for the selected module
     * category
     *
     * @param moduleCategoryID
     * @return KwlReturnObject.
     * @exception ServiceException .
     * @see ServiceException
     */
    @Override
    public JSONObject getModules(String moduleCatID, String moduleCatName) throws ServiceException {

        KwlReturnObject result = null;
        JSONObject jresult = new JSONObject();
        if(moduleCatName.equals(CustomReportConstants.Reports_ModuleCategoryName)) {
            result = accCustomerReportServiceDao.getReportsFromReportList(moduleCatID);
        } else {
            result = accCustomerReportServiceDao.getModules(moduleCatID);
        }
        if (result != null) {
            List lst = result.getEntityList();
            Iterator ite = lst.iterator();
            while (ite.hasNext()) {
                try {
                    Object[] row = (Object[]) ite.next();
                    String id = row[0].toString();
                    String moduleOrReportName = row[1].toString();
                    if (!moduleCatName.equals(CustomReportConstants.Reports_ModuleCategoryName)) { //this UUID is ID of 'Reports' category which refers to reports from Report-List
                        if (id.equalsIgnoreCase(String.valueOf(Constants.Acc_Goods_Receipt_ModuleId))) {
                            moduleOrReportName = Constants.Goods_Receipt;
                        } else if (id.equalsIgnoreCase(String.valueOf(Constants.Acc_Make_Payment_ModuleId)) || id.equalsIgnoreCase(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {
                            moduleOrReportName = Constants.moduleID_NameMap.get(Integer.parseInt(id));
                        }
                    }
                    JSONObject jobj = new JSONObject();
                    jobj.put("id", id);
                    jobj.put("modulename", moduleOrReportName);
                    jresult.append("data", jobj);
                } catch (JSONException ex) {
                    Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return jresult;
    }

    /**
     * This method returns the fields for the selected module category
     *
     * @param moduleID
     * @param companyID
     * @param userId
     * @return KwlReturnObject.
     * @exception ServiceException .
     * @see ServiceException
     */
    @Override
    public JSONObject getFields(Map<String,Object> requestMap) throws ServiceException {
        String moduleID = null, companyID = null, userid = null;
        String moduleCategory = null;
        int xtype = 0;
        Boolean isforformulabuilder=false;  
        if (requestMap.containsKey(Constants.isforformulabuilder) && requestMap.get(Constants.isforformulabuilder) != null) {
            isforformulabuilder = Boolean.parseBoolean((String)requestMap.get(Constants.isforformulabuilder));
        }
        if (requestMap.containsKey(Constants.xtype) && requestMap.get(Constants.xtype) != null) {
            xtype = Integer.parseInt((String) requestMap.get(Constants.xtype));
        }
        if (requestMap.containsKey(Constants.companyKey) && requestMap.get(Constants.companyKey) != null) {
            companyID = (String) requestMap.get(Constants.companyKey);
        }
        if (requestMap.containsKey(Constants.useridKey) && requestMap.get(Constants.useridKey) != null) {
            userid = (String) requestMap.get(Constants.useridKey);
        }
        if (requestMap.containsKey(Constants.moduleid) && requestMap.get(Constants.moduleid) != null) {
            moduleID = (String) requestMap.get(Constants.moduleid);
        }
        if (requestMap.containsKey("moduleCategory") && requestMap.get("moduleCategory") != null) {
            moduleCategory = (String) requestMap.get("moduleCategory");
        }

        KwlReturnObject result = null;
        JSONObject jresult = new JSONObject();
        String mainTable = null;
        String moduleList = "";
        String extrainnerjoin = "";
        //Get Default Headers from Default Header Field Table
        HashMap<String, Object> map = new HashMap<String, Object>();
        Map<String, List<String>> moduleMap = new HashMap<String, List<String>>();
        String countryid = "0";
        KwlReturnObject companyresult = accountingHandlerDAOobj.getObject(Company.class.getName(), String.valueOf(requestMap.get("companyid")));
        Company companyObj = (Company) companyresult.getEntityList().get(0);
        if (companyObj.getCountry() != null) {
            countryid = countryid + "," + companyObj.getCountry().getID()  ;
        }
        
        KwlReturnObject companyPreference = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), String.valueOf(requestMap.get("companyid")));
        ExtraCompanyPreferences extraCompanyAccPref = (ExtraCompanyPreferences) companyPreference.getEntityList().get(0);
        boolean isNewGst = extraCompanyAccPref.isIsNewGST();
        
        companyObj=null;
        map.put(Constants.filter_names, Arrays.asList("module", "iscustomreport","INcountryid", "ISNULLrefmoduleid"));
        map.put(Constants.filter_values, Arrays.asList(moduleID, true,countryid));
        countryid=null;
        map.put("order_by", Arrays.asList("defaultHeader"));
        map.put("order_type", Arrays.asList("asc"));
        if (xtype != 0) {
            map.put(Constants.xtype, xtype);
            map.put(Constants.isforformulabuilder, isforformulabuilder);
        }
        KwlReturnObject dhEmptyRefModule = accCustomerReportServiceDao.getDefaultFields(map);
        if (dhEmptyRefModule != null) {
            List<DefaultHeader> dh = dhEmptyRefModule.getEntityList();
            for (DefaultHeader header : dh) {
                //TO DO : remove comparision with solinking condition as dbtablename in DB contains element(last)
                if (header.getDbTableName() != null && !(CustomDesignerConstants.solinking).equalsIgnoreCase(header.getDbTableName()) && !header.isIslineitem()) {
                    mainTable = header.getDbTableName();
                    break;
                }
            }
        }
        if (dhEmptyRefModule != null) {
            List<DefaultHeader> allHeadersEmptyRefModule = dhEmptyRefModule.getEntityList();
            try {
                for (DefaultHeader header : allHeadersEmptyRefModule) {
                    if(header.getDefaultHeader().trim().equals("Tax Percent") && isNewGst && (mainTable.equals("creditnote") || mainTable.equals("debitnote") )){
                       continue;
                    }
                    JSONObject jobj = new JSONObject();
                    String dbTableName= header.getDbTableName() !=null ? header.getDbTableName():"";
                    jobj.put("id", header.getId());
                    jobj.put("defaultHeader", header.getDefaultHeader().trim());
                    jobj.put("displayName", header.getDefaultHeader());

                    if (header.isIslineitem()) {
                        jobj.put("columntype", "Line Items");
                        jobj.put("isLineItem", true);
                    } else {
                        jobj.put("columntype", "Default Fields");
                    }
                    //condition of Linking case
                    if (dbTableName.contains(CustomDesignerConstants.linking)) {
                        String linkinginnerjoin = "";
                        linkinginnerjoin = " left join " + dbTableName + " on " + dbTableName + ".docid = " + mainTable + "." + header.getReftablefk() + " ";
                        jobj.put("extrainnerjoin", linkinginnerjoin);
                        jobj.put("linkingjoinflag", true);
                    }
                    jobj.put("dbtablename", dbTableName);
                    jobj.put("dbcolumnname", header.getDbcolumnname());
                    jobj.put("reftablename", header.getReftablename());
                    jobj.put("reftabledatacolumn", header.getReftabledatacolumn());
                    jobj.put("reftablefk", header.getReftablefk());
                    jobj.put("xtype", header.getXtype() != null ? Integer.parseInt(header.getXtype()) : 1); //TODO advance serch on combo,number,date fields
                    jobj.put("customfield", false);
                    jobj.put("isMeasureItem", false);
                    jobj.put("iscustomextrajoin", false);
                    jobj.put("mainTable", mainTable);
                    jobj.put("isDataIndex", header.isIsDataIndex());
                    jobj.put("moduleName", moduleCategory.equals(CustomReportConstants.Reports_ModuleCategoryName) ? accCustomerReportServiceDao.getReportNameFromReportListByReportId(moduleID) : getModuleNameForModuleID(moduleID));
                    jobj.put("properties", getFieldsProperties(jobj));
                    jresult.append("data", jobj);
                    //Fetch DefaultFieldsMapping Data for the selected Default Field
                    fetchDefaultFieldHeaderMappings(mainTable, header, jobj);
                }
            } catch (JSONException ex) {
                Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        map = new HashMap<String, Object>();
        map.put(Constants.filter_names, Arrays.asList("module", "iscustomreport", "IS!NULLrefmoduleid"));
        map.put(Constants.filter_values, Arrays.asList(moduleID, true));
        map.put("order_by", Arrays.asList("defaultHeader"));
        map.put("order_type", Arrays.asList("asc"));
        if (xtype != 0) {
            map.put(Constants.xtype, xtype);
            map.put(Constants.isforformulabuilder, isforformulabuilder);
        }
        KwlReturnObject dhNonEmptyRefModule = accCustomerReportServiceDao.getDefaultFields(map);

        List<DefaultHeader> headers = dhNonEmptyRefModule.getEntityList();
        StringBuilder modules = new StringBuilder();
        if (dhNonEmptyRefModule.getEntityList().size() > 0) {
            for (DefaultHeader header : headers) {
                modules.append("'" + header.getRefModuleId() + "',");
                List<String> metadeta = new ArrayList<String>();
                metadeta.add(header.getDbTableName());
                metadeta.add(header.getDbcolumnname());
                metadeta.add(header.getReftablename());
                metadeta.add(header.getReftabledatacolumn());
                metadeta.add(header.getReftablefk());
                moduleMap.put(header.getRefModuleId(), metadeta);
                extrainnerjoin = " left join " + header.getReftablename() + " on " + header.getDbTableName() + "." + header.getReftablename() + " = " + header.getReftablename() + "." + header.getReftablefk() + " ";

            }
            if (modules.lastIndexOf(",") > -1) {
                moduleList = modules.substring(0, modules.lastIndexOf(","));
            }
            map = new HashMap<String, Object>();
            map.put(Constants.filter_names, Arrays.asList("INmodule", "iscustomreport"));
            map.put(Constants.filter_values, Arrays.asList(moduleList, true));
            map.put("order_by", Arrays.asList("defaultHeader"));
            map.put("order_type", Arrays.asList("asc"));
            if (xtype != 0) {
                map.put(Constants.xtype, xtype);
                map.put(Constants.isforformulabuilder, isforformulabuilder);
            }
            KwlReturnObject dhINModule = accCustomerReportServiceDao.getDefaultFields(map);
            List<DefaultHeader> headerstemp = dhINModule.getEntityList();
            for (DefaultHeader header1 : headerstemp) {
                //result_query1.getEntityList().add(header1);
                JSONObject jobj = new JSONObject();
                try {
                    jobj.put("id", header1.getId());
                    jobj.put("defaultHeader", header1.getDefaultHeader().trim());
                    jobj.put("displayName", header1.getDefaultHeader());
                    if (moduleID.equals(String.valueOf(Constants.Acc_Sales_Order_ModuleId)) || moduleID.equals(String.valueOf(Constants.Acc_Invoice_ModuleId)) || moduleID.equals(String.valueOf(Constants.Acc_Sales_Return_ModuleId)) || moduleID.equals(String.valueOf(Constants.Acc_Delivery_Order_ModuleId))|| moduleID.equals(String.valueOf(Constants.Acc_Customer_Quotation_ModuleId)) || moduleID.equals(String.valueOf(Constants.Acc_FixedAssets_DisposalInvoice_ModuleId)) || moduleID.equals(String.valueOf(Constants.Acc_FixedAssets_Sales_Return_ModuleId))) {
                        jobj.put("columntype", CustomReportConstants.Acc_Sales_Order_ColumnType);
                    } else if (moduleID.equals(String.valueOf(Constants.Acc_Purchase_Order_ModuleId)) || moduleID.equals(String.valueOf(Constants.Acc_Purchase_Return_ModuleId)) || moduleID.equals(String.valueOf(Constants.Acc_Goods_Receipt_ModuleId)) || moduleID.equals(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId)) || moduleID.equals(String.valueOf(Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId)) || moduleID.equals(String.valueOf(Constants.Acc_FixedAssets_Purchase_Return_ModuleId))) {
                        jobj.put("columntype", CustomReportConstants.Acc_Purchase_Order_ColumnType);
                    }
                    jobj.put("dbtablename", header1.getDbTableName() != null ? header1.getDbTableName() : mainTable);
                    jobj.put("dbcolumnname", header1.getDbcolumnname() != null ? header1.getDbcolumnname() : "");
                    jobj.put("reftablename", header1.getReftablename() != null ? header1.getReftablename() : "");
                    //jobj.put("reftabledatacolumn", header1.getReftabledatacolumn() != null ? header1.getReftabledatacolumn() : moduleMap.get(header1.getModule().getId()).get(3));
                    if (header1.getReftabledatacolumn() != null) {
                        jobj.put("reftabledatacolumn", header1.getReftabledatacolumn());
                    } else if (header1.getDbcolumnname() != null) {
                        jobj.put("reftabledatacolumn", header1.getDbcolumnname());
                    } else {
                        jobj.put("reftabledatacolumn", moduleMap.get(header1.getModule().getId()).get(3));
                    }
                    jobj.put("reftablefk", header1.getReftablefk() != null ? header1.getReftablefk() : "");
                    jobj.put("xtype", header1.getXtype() != null ? Integer.parseInt(header1.getXtype()) : 1); //TODO advance serch on combo,number,date fields
                    jobj.put("customfield", false);
                    jobj.put("mainTable", mainTable);
                    jobj.put("extrainnerjoin", extrainnerjoin);
                    jobj.put("iscustomervendorfieldsflag", true);
                    jobj.put("isMeasureItem", false);
                    jobj.put("iscustomextrajoin", false);
                    jobj.put("properties", getFieldsProperties(jobj));
                    jobj.put("isDataIndex", header1.isIsDataIndex());
                    jobj.put("moduleName", moduleCategory.equals(CustomReportConstants.Reports_ModuleCategoryName) ? accCustomerReportServiceDao.getReportNameFromReportListByReportId(moduleID) : getModuleNameForColumnType(moduleID));
                    //Fetch DefaultFieldsMapping Data for the selected Default Field
                    fetchDefaultFieldHeaderMappings(mainTable, header1, jobj);
                    jresult.append("data", jobj);

                } catch (JSONException ex) {
                    Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
        // Custom field data
        result = null;
        KwlReturnObject mappingCustomResult = null;
        KwlReturnObject dhProductNameEntity = null;
        map = new HashMap<String, Object>();
        if (!moduleCategory.equals(CustomReportConstants.Reports_ModuleCategoryName)) {
            map.put(Constants.filter_names, Arrays.asList("module"));
            map.put(Constants.filter_values, Arrays.asList(moduleID));
            if (!StringUtil.isNullOrEmpty(moduleID)) {
                map.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, CustomReportConstants.IsActivated_Flag));
                if(moduleID.equals(Constants.CUSTOMER_MODULE_UUID)){
                    map.put(Constants.filter_values, Arrays.asList(companyID, Constants.Acc_Customer_ModuleId, 1));
                }else{
                    map.put(Constants.filter_values, Arrays.asList(companyID, Integer.parseInt(moduleID), 1));
                }
            }
            map.put("order_by", Arrays.asList("sequence"));
            map.put("order_type", Arrays.asList("asc"));
            if (xtype != 0) {
                map.put(Constants.xtype, xtype);
                map.put(Constants.isforformulabuilder, isforformulabuilder);
            }
            result = accCustomerReportServiceDao.getCustomFieldsData(map);
        }
        if (result != null) {
            List<FieldParams> fieldParams = result.getEntityList();
            // firstValue is a counter for maintaing the DB hit only at first iteration only ,susequent value will be read from map
            int firstValue = 1;
            if(fieldParams!=null) {
                try {
                    for (FieldParams fieldParam : fieldParams) { //these are header of main module
                        JSONObject jobj = new JSONObject();
                        jobj.put("id", fieldParam.getId());
                        jobj.put("defaultHeader", fieldParam.getFieldlabel());
                        jobj.put("displayName", fieldParam.getFieldlabel());
                        boolean isCustomExtrajoin = false;
                        if (fieldParam.getCustomcolumn() == 1) {
//                        jobj.put("islineItem", true);
                            jobj.put("isLineItem", true);
                            jobj.put("columntype", "Line Items");
                            jobj.put("reftablename", getmoduledataRefNameForCustomField(fieldParam.getModuleid()));
                            String refTablePK = accCustomerReportServiceDao.getmoduledataRefPKColName(getmoduledataRefNameForCustomField(fieldParam.getModuleid()));
                            jobj.put("reftablefk", refTablePK);
                            if (firstValue == 1) {
                                map = new HashMap<String, Object>();
                                map.put(Constants.filter_names, Arrays.asList("module", "iscustomreport", CustomReportConstants.DBCOLUMN_NAME));
                                map.put(Constants.filter_values, Arrays.asList(moduleID, true, Constants.Acc_Product_modulename.toLowerCase()));
                                map.put("order_by", Arrays.asList("defaultHeader"));
                                map.put("order_type", Arrays.asList("asc"));
                                dhProductNameEntity = accCustomerReportServiceDao.getDefaultFields(map);
                                firstValue++;
                            }
                            if (dhProductNameEntity != null) {
                                List<DefaultHeader> productNameDHList = dhProductNameEntity.getEntityList();
                                if (productNameDHList.size() > 0) {
                                    for (DefaultHeader productNameHeader : productNameDHList) {
                                        jobj.put("dbtablename", productNameHeader.getDbTableName() != null ? productNameHeader.getDbTableName() : "");
                                    }
                                }
                            }
                            // Extra Join for Line item custom field 
                            if (moduleID.equals(String.valueOf(Constants.Acc_Invoice_ModuleId)) || moduleID.equals(String.valueOf(Constants.Acc_FixedAssets_DisposalInvoice_ModuleId)) || moduleID.equals(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId)) || moduleID.equals(String.valueOf(Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId)) || moduleID.equals(String.valueOf(Constants.Acc_Debit_Note_ModuleId)) || moduleID.equals(String.valueOf(Constants.Acc_Credit_Note_ModuleId))) {
                                JSONObject customFieldsExtraJoin = new JSONObject();
                                JSONArray customFieldsExtraJoinArray = new JSONArray();
                                isCustomExtrajoin = true;
                                mappingCustomResult = accCustomerReportServiceDao.getCustomReportsCustomFieldsMapping(moduleID, true, "");
                                if (mappingCustomResult != null) {
                                    List<AccCustomReportsCustomFieldsMapping> list = mappingCustomResult.getEntityList();
                                    //list.get(0).getDefaultheaderid();
                                    for (int i = 0; i < list.size(); i++) {
                                        customFieldsExtraJoin = new JSONObject();
                                        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(DefaultHeader.class.getName(), list.get(i).getDefaultheaderid());
                                        DefaultHeader mappingDH = (DefaultHeader) objItr.getEntityList().get(0);
                                        extrainnerjoin = " left join " + mappingDH.getReftablename() + " on " + mappingDH.getReftablename().trim() + "." + mappingDH.getReftablefk().trim() + " = " + mappingDH.getDbTableName().trim() + "." + mappingDH.getDbcolumnname() + " ";
                                        customFieldsExtraJoin.put(mappingDH.getReftablename(), extrainnerjoin);
                                        customFieldsExtraJoinArray.put(customFieldsExtraJoin);
                                    }
                                    jobj.put("customfieldsextrajoin", customFieldsExtraJoinArray);
                                }
                            }
                        } else {
                            jobj.put("columntype", "Custom Fields");
                            jobj.put("reftablename", fieldParam.getModuleid() == 30 ? getCrossModuleCustomFieldTableName(fieldParam.getModuleid()) : StringUtil.getmoduledataRefName(fieldParam.getModuleid()).toLowerCase());
                            String refTablePK = accCustomerReportServiceDao.getmoduledataRefPKColName(fieldParam.getModuleid() == 30 ? getCrossModuleCustomFieldTableName(fieldParam.getModuleid()) :  StringUtil.getmoduledataRefName(fieldParam.getModuleid()).toLowerCase());
                            jobj.put("reftablefk", refTablePK);
                            if (moduleID.equals(String.valueOf(Constants.Acc_Invoice_ModuleId)) || moduleID.equals(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId)) || moduleID.equals(String.valueOf(Constants.Acc_Debit_Note_ModuleId)) || moduleID.equals(String.valueOf(Constants.Acc_Credit_Note_ModuleId))) {// Extra Join for Global custom field 
                                JSONObject customFieldsExtraJoin = new JSONObject();
                                JSONArray customFieldsExtraJoinArray = new JSONArray();
                                isCustomExtrajoin = true;
                            mappingCustomResult = accCustomerReportServiceDao.getCustomReportsCustomFieldsMapping(moduleID, false,"");
                                if (mappingCustomResult != null) {
                                    List<AccCustomReportsCustomFieldsMapping> list = mappingCustomResult.getEntityList();
                                    list.get(0).getDefaultheaderid();
                                    for (int i = 0; i < list.size(); i++) {
                                        customFieldsExtraJoin = new JSONObject();
                                        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(DefaultHeader.class.getName(), list.get(i).getDefaultheaderid());
                                        DefaultHeader mappingDH = (DefaultHeader) objItr.getEntityList().get(0);
                                        extrainnerjoin = " left join " + mappingDH.getReftablename() + " on " + mappingDH.getReftablename().trim() + "." + mappingDH.getReftablefk().trim() + " = " + mappingDH.getDbTableName().trim() + "." + mappingDH.getDbcolumnname() + " ";
                                        customFieldsExtraJoin.put(mappingDH.getReftablename(), extrainnerjoin);
                                        customFieldsExtraJoinArray.put(customFieldsExtraJoin);
                                    }
                                    jobj.put("customfieldsextrajoin", customFieldsExtraJoinArray);
                                }
                            }

                        }
                        if (moduleID.equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId)) || (moduleID.equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId)))|| moduleID.equals(String.valueOf(Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId)) || moduleID.equals(String.valueOf(Constants.Acc_FixedAssets_DisposalInvoice_ModuleId))) {
                            if ( (moduleID.equals(String.valueOf(Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId)) || moduleID.equals(String.valueOf(Constants.Acc_FixedAssets_DisposalInvoice_ModuleId)) ) && jobj.optBoolean("isLineItem")) {
                                jobj.put("dbtablename", "jedetail");
                            } else {
                                jobj.put("dbtablename", "journalentry");
                            }
                        }
                        jobj.put("dbcolumnname", accCustomerReportServiceDao.getmoduledataRefPKColName(mainTable));
                        jobj.put("reftabledatacolumn", CustomReportConstants.COL_NAME_PREFIX + fieldParam.getColnum());
                        jobj.put("xtype", fieldParam.getFieldtype());
                        jobj.put("customfield", true);
                        jobj.put("mainTable", mainTable);
                        jobj.put("iscustomextrajoin", isCustomExtrajoin);
                        jobj.put("isMeasureItem", false);
                        jobj.put("properties", getFieldsProperties(jobj));
                        jobj.put("moduleName", getModuleNameForModuleID(moduleID));
                        if (moduleID.equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId)) || moduleID.equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId)) && fieldParam.getCustomcolumn() == 1) {
                            jobj.put("dbcolumnname", "id");
                            jobj.put("reftablename", "accjedetailcustomdata");
                            jobj.put("reftablefk", "recdetailId");
                            jobj.put("iscustomextrajoin", true);

                            String document = "'advancepayment','creditnote','debitnote','gl', 'invoice', 'refund', 'loan'";

                            JSONObject customFieldsExtraJoin = new JSONObject();
                            JSONArray customFieldsExtraJoinArray = new JSONArray();
                        JSONObject receiptJobj=null;

                            mappingCustomResult = accCustomerReportServiceDao.getCustomReportsCustomFieldsMapping(moduleID, true, document);
                            if (mappingCustomResult != null) {
                                List<AccCustomReportsCustomFieldsMapping> list = mappingCustomResult.getEntityList();
                                for (int i = 0; i < list.size(); i++) {
                                    receiptJobj = new JSONObject(jobj.toString());;

                                String id= receiptJobj.getString("id")+i;
                                receiptJobj.put("id",id);
                                    receiptJobj.put("defaultHeader", getDocumentName(list.get(i).getMappingFor()) + " " + fieldParam.getFieldlabel());
                                    receiptJobj.put("displayName", getDocumentName(list.get(i).getMappingFor()) + " " + fieldParam.getFieldlabel());

                                    if (moduleID.equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {
                                        if (receiptJobj.optInt("xtype") == 4) {
                                            if (list.get(i).getMappingFor().equals("refund")) {
                                                receiptJobj.put("reftabledatacolumn", "if(isnull(receiptadvancedetail.receipt),NULL,fieldcombodata.value)");
                                            } else if (list.get(i).getMappingFor().equals("advancepayment")) {
                                                receiptJobj.put("reftabledatacolumn", "if(isnull(receiptadvancedetail.advancedetailid),fieldcombodata.value,NULL)");
                                            }
                                        } else {
                                            if (list.get(i).getMappingFor().equals("refund")) {
                                                receiptJobj.put("reftabledatacolumn", "if(isnull(receiptadvancedetail.receipt),NULL,accjedetailcustomdata." + jobj.getString("reftabledatacolumn").trim() + ")");
                                            } else if (list.get(i).getMappingFor().equals("advancepayment")) {
                                                receiptJobj.put("reftabledatacolumn", "if(isnull(receiptadvancedetail.advancedetailid),accjedetailcustomdata." + jobj.getString("reftabledatacolumn").trim() + ",NULL)");
                                            }
                                        }
                                    } else if (moduleID.equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId))) {
                                        if (receiptJobj.optInt("xtype") == 4) {
                                            if (list.get(i).getMappingFor().equals("refund")) {
                                                receiptJobj.put("reftabledatacolumn", "if(payment.paymentwindowtype=1,NULL,fieldcombodata.value)");
                                            } else if (list.get(i).getMappingFor().equals("advancepayment")) {
                                                receiptJobj.put("reftabledatacolumn", "if(payment.paymentwindowtype=1,fieldcombodata.value,NULL)");
                                            }
                                        } else {
                                            if (list.get(i).getMappingFor().equals("refund")) {
                                                receiptJobj.put("reftabledatacolumn", "if(payment.paymentwindowtype=1,NULL,accjedetailcustomdata." + jobj.getString("reftabledatacolumn").trim() + ")");
                                            } else if (list.get(i).getMappingFor().equals("advancepayment")) {
                                                receiptJobj.put("reftabledatacolumn", "if(payment.paymentwindowtype=1,accjedetailcustomdata." + jobj.getString("reftabledatacolumn").trim() + ",NULL)");
                                            }
                                        }
                                    }

                                    customFieldsExtraJoin = new JSONObject();
                                    customFieldsExtraJoinArray = new JSONArray();

                                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(DefaultHeader.class.getName(), list.get(i).getDefaultheaderid());
                                    DefaultHeader mappingDH = (DefaultHeader) objItr.getEntityList().get(0);
                                    receiptJobj.put("dbtablename", mappingDH.getReftablename());

                                    extrainnerjoin = " left join " + mappingDH.getReftablename() + " on " + mappingDH.getReftablename().trim() + "." + mappingDH.getReftablefk().trim() + " = " + mappingDH.getDbTableName().trim() + "." + mappingDH.getDbcolumnname() + " ";
                                    customFieldsExtraJoin.put(mappingDH.getReftablename(), extrainnerjoin);
                                    customFieldsExtraJoinArray.put(customFieldsExtraJoin);
                                    if(jobj.optInt("xtype") == 4){
                                        receiptJobj.put("colnum", jobj.getString("reftabledatacolumn").trim());
                                    }
                                    receiptJobj.remove("customfieldsextrajoin");
                                    receiptJobj.put("customfieldsextrajoin", customFieldsExtraJoinArray);
                                    jobj.put("properties", getFieldsProperties(receiptJobj));
                                    jresult.append("data", receiptJobj);
                                }
                            }
                        } else {
                            jresult.append("data", jobj);
                        }
                    }
                } catch (JSONException ex) {
                    Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

        // Measure field data
        result = null;
        KwlReturnObject mappingResult = null;
        if (!moduleCategory.equals(CustomReportConstants.Reports_ModuleCategoryName)) {
            result = accCustomerReportServiceDao.getMeasureFields(moduleID);
        }
        if (result != null) {
            List<AccCustomReportsMeasuresFields> list = result.getEntityList();
            try {
                for (AccCustomReportsMeasuresFields crmf : list) {
                    JSONObject jobj = new JSONObject();
                    jobj.put("isMeasureItem", true);
                    jobj.put("columntype", "Measures");
                    jobj.put("defaultHeader", crmf.getMeasurefieldname());
                    jobj.put("displayName", crmf.getMeasurefielddisplayname());
                    jobj.put("xtype", Integer.parseInt(crmf.getXtype()));
                    jobj.put("mainTable", mainTable);
                    jobj.put("customfield", false);
                    jobj.put("iscustomextrajoin", false);
                    jobj.put("id", crmf.getMeasurefieldid());
                    mappingResult = accCustomerReportServiceDao.getMeasureFieldMappings(crmf.getMeasurefieldid());
                    if (mappingResult != null) {
                        List<AccCustomReportsMeasuresFieldsMapping> crmfm = mappingResult.getEntityList();
                        Iterator crmfm_itr = crmfm.iterator();
                        while (crmfm_itr.hasNext()) {
                            JSONObject mappingJSONObj = new JSONObject();
                            String joinTableName = "";
                            AccCustomReportsMeasuresFieldsMapping obj = (AccCustomReportsMeasuresFieldsMapping) crmfm_itr.next();
                            mappingJSONObj.put("id", obj.getId());
                            mappingJSONObj.put("defaultheaderid", obj.getDefaultheaderid());
                            mappingJSONObj.put("measurefieldid", obj.getMeasurefieldid());
                            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(DefaultHeader.class.getName(), obj.getDefaultheaderid());
                            DefaultHeader mappingDH = (DefaultHeader) objItr.getEntityList().get(0);
                            mappingJSONObj.put("dbcolumnname", mappingDH.getDbcolumnname());
                            mappingJSONObj.put("dbtabletame", mappingDH.getDbTableName());
                            mappingJSONObj.put("reftabledatacolumn", mappingDH.getReftabledatacolumn());
                            mappingJSONObj.put("reftablename", mappingDH.getReftablename());
                            mappingJSONObj.put("reftablefk", mappingDH.getReftablefk());
                            //mappingJSONObj.put("defaultHeader", mappingDH.getReftablename()+"."+mappingDH.getReftablefk());
                            mappingJSONObj.put("defaultHeader", mappingDH.getReftablename() + "." + mappingDH.getReftabledatacolumn());
                            mappingJSONObj.put("isDataIndex", obj.getIsDataIndex());
                            if (mappingDH.getReftablename() == null || mainTable.equalsIgnoreCase(mappingDH.getReftablename().trim())) {
                                joinTableName = mappingDH.getDbTableName().trim();
                            } else {
                                joinTableName = mappingDH.getReftablename().trim();
                            }
                            //extrainnerjoin = " left join " + joinTableName + " on " + mappingDH.getDbTableName().trim() + "." + mappingDH.getReftablename().trim() + " = " + mappingDH.getReftablename().trim() + "." + mappingDH.getReftablefk().trim() + " ";
                            //extrainnerjoin = " left join " + joinTableName + " on " + mappingDH.getReftablename().trim() + "." + mappingDH.getReftablefk().trim() + " = " + mappingDH.getDbTableName().trim() + "." + mappingDH.getReftablename().trim()  +" ";
                            if (!mainTable.equalsIgnoreCase(joinTableName)) {
                                extrainnerjoin = " left join " + joinTableName + " on " + mappingDH.getReftablename().trim() + "." + mappingDH.getReftablefk().trim() + " = " + mappingDH.getDbTableName().trim() + "." + mappingDH.getDbcolumnname().trim() + " ";
                            }
                            mappingJSONObj.put("extrainnerjoin", extrainnerjoin);
                            jobj.append("mappingJSONObj", mappingJSONObj);
                        }
                        jobj.put("properties", getFieldsProperties(jobj));
                        jobj.put("moduleName", getModuleNameForModuleID(moduleID));
                        jresult.append("data", jobj);
                    }
                }
            } catch (JSONException ex) {
                Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }


        // Cross Module Fields Data 
        if (!moduleCategory.equals(CustomReportConstants.Reports_ModuleCategoryName) && !(moduleID.equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId))) && !(moduleID.equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId)))) {
            result = null;
            KwlReturnObject linkedModules = accCustomerReportServiceDao.getLinkedModules(moduleID);
            List<CrossModuleLinkingDetails> linkedModuleIDS = linkedModules.getEntityList();
            if (linkedModuleIDS != null && linkedModuleIDS.size() > 0) {
                for (CrossModuleLinkingDetails linkedMoudleID : linkedModuleIDS) {
                    // Default Fields
                    HashMap<String, Object> depModuleMap = new HashMap<String, Object>();
                    depModuleMap.put(Constants.filter_names, Arrays.asList("module", "iscustomreport", "allowcrossmodule", "ISNULLrefmoduleid"));
                    depModuleMap.put(Constants.filter_values, Arrays.asList(linkedMoudleID.getLinkedmodule(), true, true));
                    depModuleMap.put("order_by", Arrays.asList("defaultHeader"));
                    depModuleMap.put("order_type", Arrays.asList("asc"));
                    if (xtype != 0) {
                        depModuleMap.put(Constants.xtype, xtype);
                        depModuleMap.put(Constants.isforformulabuilder, isforformulabuilder);
                    }
                    KwlReturnObject depModuleDHEmptyRefModule = accCustomerReportServiceDao.getDefaultFields(depModuleMap);
                    if (depModuleDHEmptyRefModule != null) {
                        List<DefaultHeader> allHeadersEmptyRefModule = depModuleDHEmptyRefModule.getEntityList();
                        try {
                            for (DefaultHeader header : allHeadersEmptyRefModule) {
                                JSONObject jobj = new JSONObject();
                                String dbTableName = header.getDbTableName() != null ? header.getDbTableName() : "";
                                  if(header.isAllowcrossmodule()) {
                                    jobj.put("id", header.getId()+linkedMoudleID.getLinkedmodule());
                                }
                                jobj.put("defaultHeader", header.getDefaultHeader().trim());
                                jobj.put("displayName", header.getDefaultHeader());
//                                jobj.put("columntype", getCrossModuleNameForModuleID(linkedMoudleID.getLinkedmodule(),"Default Fields"));
//                                jobj.put("moduleName", getCrossModuleNameForModuleID(linkedMoudleID.getLinkedmodule(),"Default Fields"));
                                if (header.isIslineitem()) {
//                                    jobj.put("columntype", "Line Items");
                                    jobj.put("isLineItem", true);
                                    jobj.put("columntype", getCrossModuleNameForModuleID(linkedMoudleID.getLinkedmodule(), "Line Items"));
                                    jobj.put("moduleName", getCrossModuleNameForModuleID(linkedMoudleID.getLinkedmodule(), "Line Items"));
                                } else {
                                    jobj.put("columntype", getCrossModuleNameForModuleID(linkedMoudleID.getLinkedmodule(), "Default Fields"));
                                    jobj.put("moduleName", getCrossModuleNameForModuleID(linkedMoudleID.getLinkedmodule(), "Default Fields"));
//                                    jobj.put("isLineItem", false);

                                }
                                //condition of Linking case
                                if (dbTableName.contains(CustomDesignerConstants.linking)) {
                                    String linkinginnerjoin = "";
                                    linkinginnerjoin = " left join " + dbTableName + " on " + dbTableName + ".docid = " + mainTable + "." + header.getReftablefk() + " ";
                                    jobj.put("extrainnerjoin", linkinginnerjoin);
                                    jobj.put("linkingjoinflag", true);
                                }
                                jobj.put("dbtablename", dbTableName);
                                jobj.put("dbcolumnname", header.getDbcolumnname());
                                jobj.put("reftablename", header.getReftablename());
                                jobj.put("reftabledatacolumn", header.getReftabledatacolumn());
                                jobj.put("reftablefk", header.getReftablefk());
                                jobj.put("xtype", header.getXtype() != null ? Integer.parseInt(header.getXtype()) : 1); //TODO advance serch on combo,number,date fields
                                jobj.put("customfield", false);
                                jobj.put("isMeasureItem", false);
                                jobj.put("iscustomextrajoin", false);
                                jobj.put("crossJoinMainTable", getModuleMainTable(getModuleNameForModuleID(linkedMoudleID.getLinkedmodule())));
                                jobj.put("crossJoinModuleId", Integer.parseInt(linkedMoudleID.getLinkedmodule()));
                                jobj.put("mainTable", mainTable);
                                jobj.put("isDataIndex", header.isIsDataIndex());
                                jobj.put("properties", getFieldsProperties(jobj));
                                jobj.put("allowcrossmodule", header.isAllowcrossmodule());
                                jobj.put("linkedmodule", header.getModule().getId());
                                jresult.append("data", jobj);
                                //Fetch DefaultFieldsMapping Data for the selected Default Field
                                fetchDefaultFieldHeaderMappings(getModuleMainTable(getModuleNameForModuleID(linkedMoudleID.getLinkedmodule())), header, jobj);
                            }
                        } catch (JSONException ex) {
                            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                    // Field's for which IS!NULLrefmoduleid 

                    map = new HashMap<String, Object>();
                    map.put(Constants.filter_names, Arrays.asList("module", "iscustomreport", "IS!NULLrefmoduleid"));
                    map.put(Constants.filter_values, Arrays.asList(linkedMoudleID.getLinkedmodule(), true));
                    map.put("order_by", Arrays.asList("defaultHeader"));
                    map.put("order_type", Arrays.asList("asc"));
                    if (xtype != 0) {
                        map.put(Constants.xtype, xtype);
                        map.put(Constants.isforformulabuilder, isforformulabuilder);
                    }
                    dhNonEmptyRefModule = accCustomerReportServiceDao.getDefaultFields(map);

                    headers = dhNonEmptyRefModule.getEntityList();
                    modules = new StringBuilder();
                    if (dhNonEmptyRefModule.getEntityList().size() > 0) {
                        for (DefaultHeader header : headers) {
                            modules.append("'" + header.getRefModuleId() + "',");
                            List<String> metadeta = new ArrayList<String>();
                            metadeta.add(header.getDbTableName());
                            metadeta.add(header.getDbcolumnname());
                            metadeta.add(header.getReftablename());
                            metadeta.add(header.getReftabledatacolumn());
                            metadeta.add(header.getReftablefk());
                            moduleMap.put(header.getRefModuleId(), metadeta);
                            extrainnerjoin = " left join " + header.getReftablename() + " on " + header.getDbTableName() + "." + header.getReftablename() + " = " + header.getReftablename() + "." + header.getReftablefk() + " ";

                        }
                        if (modules.lastIndexOf(",") > -1) {
                            moduleList = modules.substring(0, modules.lastIndexOf(","));
                        }
                        map = new HashMap<String, Object>();
                        map.put(Constants.filter_names, Arrays.asList("INmodule", "iscustomreport", "allowcrossmodule"));
                        map.put(Constants.filter_values, Arrays.asList(moduleList, true, true));
                        map.put("order_by", Arrays.asList("defaultHeader"));
                        map.put("order_type", Arrays.asList("asc"));
                        if (xtype != 0) {
                            map.put(Constants.xtype, xtype);
                            map.put(Constants.isforformulabuilder, isforformulabuilder);
                        }
                        KwlReturnObject dhINModule = accCustomerReportServiceDao.getDefaultFields(map);
                        List<DefaultHeader> headerstemp = dhINModule.getEntityList();
                        for (DefaultHeader header1 : headerstemp) {
                            //result_query1.getEntityList().add(header1);
                            JSONObject jobj = new JSONObject();
                            try {
                                if(header1.isAllowcrossmodule()) {
                                    jobj.put("id", header1.getId()+linkedMoudleID.getLinkedmodule());
                                }
//                                jobj.put("id", header1.getId());
                                jobj.put("defaultHeader", header1.getDefaultHeader().trim());
                                jobj.put("displayName", header1.getDefaultHeader());
                                jobj.put("dbtablename", header1.getDbTableName() != null ? header1.getDbTableName() : "");
                                jobj.put("dbcolumnname", header1.getDbcolumnname() != null ? header1.getDbcolumnname() : "");
                                jobj.put("reftablename", header1.getReftablename() != null ? header1.getReftablename() : "");
                                if (header1.getReftabledatacolumn() != null) {
                                    jobj.put("reftabledatacolumn", header1.getReftabledatacolumn());
                                } else if (header1.getDbcolumnname() != null) {
                                    jobj.put("reftabledatacolumn", header1.getDbcolumnname());
                                } else {
                                    jobj.put("reftabledatacolumn", moduleMap.get(header1.getModule().getId()).get(3));
                                }
                                jobj.put("reftablefk", header1.getReftablefk() != null ? header1.getReftablefk() : "");
                                jobj.put("xtype", header1.getXtype() != null ? Integer.parseInt(header1.getXtype()) : 1); //TODO advance serch on combo,number,date fields
                                jobj.put("customfield", false);
                                jobj.put("crossJoinMainTable", getModuleMainTable(getModuleNameForModuleID(linkedMoudleID.getLinkedmodule())));
                                jobj.put("crossJoinModuleId", Integer.parseInt(linkedMoudleID.getLinkedmodule()));
                                jobj.put("mainTable", mainTable);
                                jobj.put("extrainnerjoin", extrainnerjoin);
                                jobj.put("iscustomervendorfieldsflag", true);
                                jobj.put("isMeasureItem", false);
                                jobj.put("iscustomextrajoin", false);
                                jobj.put("properties", getFieldsProperties(jobj));
                                jobj.put("isDataIndex", header1.isIsDataIndex());
                                jobj.put("columntype", getCrossModuleNameForModuleID(linkedMoudleID.getLinkedmodule(), "Default Fields"));
                                jobj.put("moduleName", getCrossModuleNameForModuleID(linkedMoudleID.getLinkedmodule(), "Default Fields"));
                                jobj.put("allowcrossmodule", header1.isAllowcrossmodule());
                                //Fetch DefaultFieldsMapping Data for the selected Default Field
                                fetchDefaultFieldHeaderMappings(mainTable, header1, jobj);
                                jresult.append("data", jobj);

                            } catch (JSONException ex) {
                                Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                            }

                        }
                    }

                    // Custom Fields - Global Only
                    result = null;
                    map = new HashMap<String, Object>();
                    map.put(Constants.filter_names, Arrays.asList("module"));
                    map.put(Constants.filter_values, Arrays.asList(linkedMoudleID.getLinkedmodule()));
                    if (!StringUtil.isNullOrEmpty(linkedMoudleID.getLinkedmodule())) {
                        map.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid,  CustomReportConstants.IsActivated_Flag));
                        map.put(Constants.filter_values, Arrays.asList(companyID, Integer.parseInt(linkedMoudleID.getLinkedmodule()), 1));
                    }
                    map.put("order_by", Arrays.asList("sequence"));
                    map.put("order_type", Arrays.asList("asc"));
                    if (xtype != 0) {
                        map.put(Constants.xtype, xtype);
                        map.put(Constants.isforformulabuilder, isforformulabuilder);
                    }
                    result = accCustomerReportServiceDao.getCustomFieldsData(map);
                    if (result != null) {
                        List<FieldParams> fieldParams = result.getEntityList();
                        try {
                            /*    for (FieldParams fieldParam : fieldParams) {
                             JSONObject jobj = new JSONObject();
                             jobj.put("id", fieldParam.getId());
                             jobj.put("defaultHeader", fieldParam.getFieldlabel());
                             jobj.put("displayName", fieldParam.getFieldlabel());
                             boolean isCustomExtrajoin = false;
                             //                                
                             jobj.put("columntype", getCrossModuleNameForModuleID(linkedMoudleID.getLinkedmodule(), "Custom Fields"));
                             jobj.put("reftablename", StringUtil.getmoduledataRefName(fieldParam.getModuleid()));
                             String refTablePK = accCustomerReportServiceDao.getmoduledataRefPKColName(StringUtil.getmoduledataRefName(fieldParam.getModuleid()));
                             jobj.put("reftablefk", refTablePK);
                             if (Integer.parseInt(linkedMoudleID.getLinkedmodule()) == Constants.Acc_Invoice_ModuleId || Integer.parseInt(linkedMoudleID.getLinkedmodule()) == Constants.Acc_Vendor_Invoice_ModuleId || Integer.parseInt(linkedMoudleID.getLinkedmodule()) == Constants.Acc_Debit_Note_ModuleId || Integer.parseInt(linkedMoudleID.getLinkedmodule()) == Constants.Acc_Credit_Note_ModuleId) {// Extra Join for Global custom field 
                             JSONObject customFieldsExtraJoin = new JSONObject();
                             JSONArray customFieldsExtraJoinArray = new JSONArray();
                             isCustomExtrajoin = true;
                             mappingCustomResult = accCustomerReportServiceDao.getCustomReportsCustomFieldsMapping(linkedMoudleID.getLinkedmodule(), false, "");
                             if (mappingCustomResult != null) {
                             List<AccCustomReportsCustomFieldsMapping> list = mappingCustomResult.getEntityList();
                             list.get(0).getDefaultheaderid();
                             for (int i = 0; i < list.size(); i++) {
                             customFieldsExtraJoin = new JSONObject();
                             KwlReturnObject objItr = accountingHandlerDAOobj.getObject(DefaultHeader.class.getName(), list.get(i).getDefaultheaderid());
                             DefaultHeader mappingDH = (DefaultHeader) objItr.getEntityList().get(0);
                             extrainnerjoin = " left join " + mappingDH.getReftablename() + " on " + mappingDH.getReftablename().trim() + "." + mappingDH.getReftablefk().trim() + " = " + mappingDH.getDbTableName().trim() + "." + mappingDH.getDbcolumnname() + " ";
                             customFieldsExtraJoin.put(mappingDH.getReftablename(), extrainnerjoin);
                             customFieldsExtraJoinArray.put(customFieldsExtraJoin);
                             }
                             jobj.put("customfieldsextrajoin", customFieldsExtraJoinArray);
                             }
                             }

                             if (Integer.parseInt(linkedMoudleID.getLinkedmodule()) == Constants.Acc_Make_Payment_ModuleId || Integer.parseInt(linkedMoudleID.getLinkedmodule()) == Constants.Acc_Receive_Payment_ModuleId) {
                             jobj.put("dbtablename", "journalentry");
                             }
                             jobj.put("dbtablename", getModuleMainTable(getModuleNameForModuleID(linkedMoudleID.getLinkedmodule())));
                             jobj.put("dbcolumnname", accCustomerReportServiceDao.getmoduledataRefPKColName(mainTable));
                             jobj.put("reftabledatacolumn", CustomReportConstants.COL_NAME_PREFIX + fieldParam.getColnum());
                             jobj.put("xtype", fieldParam.getFieldtype());
                             jobj.put("customfield", true);
                             jobj.put("crossJoinMainTable", getModuleMainTable(getModuleNameForModuleID(linkedMoudleID.getLinkedmodule())));
                             jobj.put("mainTable", mainTable);
                             jobj.put("iscustomextrajoin", isCustomExtrajoin);
                             jobj.put("isMeasureItem", false);
                             jobj.put("properties", getFieldsProperties(jobj));
                             jobj.put("allowcrossmodule", true);
                             jobj.put("moduleName", getCrossModuleNameForModuleID(linkedMoudleID.getLinkedmodule(), "Custom Fields"));
                             if ((Integer.parseInt(linkedMoudleID.getLinkedmodule()) == Constants.Acc_Receive_Payment_ModuleId || Integer.parseInt(linkedMoudleID.getLinkedmodule()) == Constants.Acc_Make_Payment_ModuleId) && fieldParam.getCustomcolumn() == 1) {
                             jobj.put("dbcolumnname", "id");
                             jobj.put("reftablename", "accjedetailcustomdata");
                             jobj.put("reftablefk", "recdetailId");
                             jobj.put("iscustomextrajoin", true);

                             String document = "'advancepayment','creditnote','debitnote','gl', 'invoice', 'refund', 'loan'";

                             JSONObject customFieldsExtraJoin = new JSONObject();
                             JSONArray customFieldsExtraJoinArray = new JSONArray();
                             JSONObject receiptJobj = null;

                             mappingCustomResult = accCustomerReportServiceDao.getCustomReportsCustomFieldsMapping(linkedMoudleID.getLinkedmodule(), true, document);
                             if (mappingCustomResult != null) {
                             List<AccCustomReportsCustomFieldsMapping> list = mappingCustomResult.getEntityList();
                             for (int i = 0; i < list.size(); i++) {
                             receiptJobj = new JSONObject(jobj.toString());;

                             String id = receiptJobj.getString("id") + i;
                             receiptJobj.put("id", id);
                             receiptJobj.put("defaultHeader", getDocumentName(list.get(i).getMappingFor()) + " " + fieldParam.getFieldlabel());
                             receiptJobj.put("displayName", getDocumentName(list.get(i).getMappingFor()) + " " + fieldParam.getFieldlabel());

                             if (Integer.parseInt(linkedMoudleID.getLinkedmodule()) == Constants.Acc_Receive_Payment_ModuleId) {
                             if (list.get(i).getMappingFor().equals("refund")) {
                             receiptJobj.put("reftabledatacolumn", "if(isnull(receiptadvancedetail.advancedetailid),NULL,accjedetailcustomdata." + jobj.getString("reftabledatacolumn").trim() + ")");
                             } else if (list.get(i).getMappingFor().equals("advancepayment")) {
                             receiptJobj.put("reftabledatacolumn", "if(isnull(receiptadvancedetail.advancedetailid),accjedetailcustomdata." + jobj.getString("reftabledatacolumn").trim() + ",NULL)");
                             }
                             } else if (Integer.parseInt(linkedMoudleID.getLinkedmodule()) == Constants.Acc_Make_Payment_ModuleId) {
                             if (list.get(i).getMappingFor().equals("refund")) {
                             receiptJobj.put("reftabledatacolumn", "if(payment.paymentwindowtype=1,NULL,accjedetailcustomdata." + jobj.getString("reftabledatacolumn").trim() + ")");
                             } else if (list.get(i).getMappingFor().equals("advancepayment")) {
                             receiptJobj.put("reftabledatacolumn", "if(payment.paymentwindowtype=1,accjedetailcustomdata." + jobj.getString("reftabledatacolumn").trim() + ",NULL)");
                             }
                             }

                             customFieldsExtraJoin = new JSONObject();
                             customFieldsExtraJoinArray = new JSONArray();

                             KwlReturnObject objItr = accountingHandlerDAOobj.getObject(DefaultHeader.class.getName(), list.get(i).getDefaultheaderid());
                             DefaultHeader mappingDH = (DefaultHeader) objItr.getEntityList().get(0);
                             receiptJobj.put("dbtablename", mappingDH.getReftablename());

                             extrainnerjoin = " left join " + mappingDH.getReftablename() + " on " + mappingDH.getReftablename().trim() + "." + mappingDH.getReftablefk().trim() + " = " + mappingDH.getDbTableName().trim() + "." + mappingDH.getDbcolumnname() + " ";
                             customFieldsExtraJoin.put(mappingDH.getReftablename(), extrainnerjoin);
                             customFieldsExtraJoinArray.put(customFieldsExtraJoin);
                             receiptJobj.remove("customfieldsextrajoin");
                             receiptJobj.put("customfieldsextrajoin", customFieldsExtraJoinArray);
                             jobj.put("properties", getFieldsProperties(receiptJobj));
                             jresult.append("data", receiptJobj);
                             }
                             }
                             } else {
                             jresult.append("data", jobj);
                             }
                             } */

                            
                            int firstValue = 1;
                            for (FieldParams fieldParam : fieldParams) {
                                JSONObject jobj = new JSONObject();
                                jobj.put("id", fieldParam.getId());
                                jobj.put("defaultHeader", fieldParam.getFieldlabel());
                                jobj.put("displayName", fieldParam.getFieldlabel());
                                boolean isCustomExtrajoin = false;
                                if (fieldParam.getCustomcolumn() == 1) {
//                        jobj.put("islineItem", true);
                                    jobj.put("isLineItem", true);
                                    jobj.put("columntype", getCrossModuleNameForModuleID(linkedMoudleID.getLinkedmodule(), "Line Items"));
                                    jobj.put("moduleName", getCrossModuleNameForModuleID(linkedMoudleID.getLinkedmodule(), "Line Items"));
                                    jobj.put("reftablename", getmoduledataRefNameForCustomField(fieldParam.getModuleid()));
                                    String refTablePK = accCustomerReportServiceDao.getmoduledataRefPKColName(getmoduledataRefNameForCustomField(fieldParam.getModuleid()));
                                    jobj.put("reftablefk", refTablePK);
                                    if (firstValue == 1) {
                                        map = new HashMap<String, Object>();
                                        map.put(Constants.filter_names, Arrays.asList("module", "iscustomreport", CustomReportConstants.DBCOLUMN_NAME));
                                        map.put(Constants.filter_values, Arrays.asList(linkedMoudleID.getLinkedmodule(), true, Constants.Acc_Product_modulename.toLowerCase()));
                                        map.put("order_by", Arrays.asList("defaultHeader"));
                                        map.put("order_type", Arrays.asList("asc"));
                                        dhProductNameEntity = accCustomerReportServiceDao.getDefaultFields(map);
                                        firstValue++;
                                    }
                                    if (dhProductNameEntity != null) {
                                        List<DefaultHeader> productNameDHList = dhProductNameEntity.getEntityList();
                                        if (productNameDHList.size() > 0) {
                                            for (DefaultHeader productNameHeader : productNameDHList) {
                                                jobj.put("dbtablename", productNameHeader.getDbTableName() != null ? productNameHeader.getDbTableName() : "");
                                            }
                                        } else if(linkedMoudleID.getLinkedmodule().equalsIgnoreCase(String.valueOf(Constants.Acc_Product_Master_ModuleId))){ // For creating valid metadata as only custom fields of product master are added as of now, to be removed when default fields are added
                                           jobj.put("dbtablename", CustomReportConstants.product);
                                        }
                                    }
                                    // Extra Join for Line item custom field 
                                    if (linkedMoudleID.getLinkedmodule().equals(String.valueOf(Constants.Acc_Invoice_ModuleId)) || linkedMoudleID.getLinkedmodule().equals(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId)) || linkedMoudleID.getLinkedmodule().equals(String.valueOf(Constants.Acc_Debit_Note_ModuleId)) || linkedMoudleID.getLinkedmodule().equals(String.valueOf(Constants.Acc_Credit_Note_ModuleId))) {
                                        JSONObject customFieldsExtraJoin = new JSONObject();
                                        JSONArray customFieldsExtraJoinArray = new JSONArray();
                                        isCustomExtrajoin = true;
                                        mappingCustomResult = accCustomerReportServiceDao.getCustomReportsCustomFieldsMapping(linkedMoudleID.getLinkedmodule(), true, "");
                                        if (mappingCustomResult != null) {
                                            List<AccCustomReportsCustomFieldsMapping> list = mappingCustomResult.getEntityList();
                                            //list.get(0).getDefaultheaderid();
                                            for (int i = 0; i < list.size(); i++) {
                                                customFieldsExtraJoin = new JSONObject();
                                                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(DefaultHeader.class.getName(), list.get(i).getDefaultheaderid());
                                                DefaultHeader mappingDH = (DefaultHeader) objItr.getEntityList().get(0);
                                                extrainnerjoin = " left join " + mappingDH.getReftablename() + " on " + mappingDH.getReftablename().trim() + "." + mappingDH.getReftablefk().trim() + " = " + mappingDH.getDbTableName().trim() + "." + mappingDH.getDbcolumnname() + " ";
                                                customFieldsExtraJoin.put(mappingDH.getReftablename(), extrainnerjoin);
                                                customFieldsExtraJoinArray.put(customFieldsExtraJoin);
                                            }
                                            jobj.put("customfieldsextrajoin", customFieldsExtraJoinArray);
                                        }
                                    }
                                } else {
                                    jobj.put("columntype", getCrossModuleNameForModuleID(linkedMoudleID.getLinkedmodule(), "Custom Fields"));
                                    jobj.put("moduleName", getCrossModuleNameForModuleID(linkedMoudleID.getLinkedmodule(), "Custom Fields"));
                                    jobj.put("reftablename", fieldParam.getModuleid() == 30 ? getCrossModuleCustomFieldTableName(fieldParam.getModuleid()) : StringUtil.getmoduledataRefName(fieldParam.getModuleid()).toLowerCase());
                                    jobj.put("dbtablename", getModuleMainTable(getModuleNameForModuleID(linkedMoudleID.getLinkedmodule())));
                                    String refTablePK = accCustomerReportServiceDao.getmoduledataRefPKColName(fieldParam.getModuleid() == 30 ? getCrossModuleCustomFieldTableName(fieldParam.getModuleid()) : StringUtil.getmoduledataRefName(fieldParam.getModuleid()).toLowerCase());
                                    jobj.put("reftablefk", refTablePK);
                                    if (linkedMoudleID.getLinkedmodule().equals(String.valueOf(Constants.Acc_Invoice_ModuleId)) || linkedMoudleID.getLinkedmodule().equals(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId)) || linkedMoudleID.getLinkedmodule().equals(String.valueOf(Constants.Acc_Debit_Note_ModuleId)) || linkedMoudleID.getLinkedmodule().equals(String.valueOf(Constants.Acc_Credit_Note_ModuleId))) {// Extra Join for Global custom field 
                                        JSONObject customFieldsExtraJoin = new JSONObject();
                                        JSONArray customFieldsExtraJoinArray = new JSONArray();
                                        isCustomExtrajoin = true;
                                        mappingCustomResult = accCustomerReportServiceDao.getCustomReportsCustomFieldsMapping(linkedMoudleID.getLinkedmodule(), false, "");
                                        if (mappingCustomResult != null) {
                                            List<AccCustomReportsCustomFieldsMapping> list = mappingCustomResult.getEntityList();
                                            list.get(0).getDefaultheaderid();
                                            for (int i = 0; i < list.size(); i++) {
                                                customFieldsExtraJoin = new JSONObject();
                                                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(DefaultHeader.class.getName(), list.get(i).getDefaultheaderid());
                                                DefaultHeader mappingDH = (DefaultHeader) objItr.getEntityList().get(0);
                                                extrainnerjoin = " left join " + mappingDH.getReftablename() + " on " + mappingDH.getReftablename().trim() + "." + mappingDH.getReftablefk().trim() + " = " + mappingDH.getDbTableName().trim() + "." + mappingDH.getDbcolumnname() + " ";
                                                customFieldsExtraJoin.put(mappingDH.getReftablename(), extrainnerjoin);
                                                customFieldsExtraJoinArray.put(customFieldsExtraJoin);
                                            }
                                            jobj.put("customfieldsextrajoin", customFieldsExtraJoinArray);
                                        }
                                    }

                                }
                                if (linkedMoudleID.getLinkedmodule().equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId)) || (linkedMoudleID.getLinkedmodule().equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId)))) {
                                    jobj.put("dbtablename", "journalentry");
                                }
                                jobj.put("dbcolumnname", accCustomerReportServiceDao.getmoduledataRefPKColName(mainTable));
                                jobj.put("reftabledatacolumn", CustomReportConstants.COL_NAME_PREFIX + fieldParam.getColnum());
                                jobj.put("xtype", fieldParam.getFieldtype());
                                jobj.put("customfield", true);
                                jobj.put("mainTable", mainTable);
                                jobj.put("crossJoinMainTable", getModuleMainTable(getModuleNameForModuleID(linkedMoudleID.getLinkedmodule())));
                                jobj.put("crossJoinModuleId", Integer.parseInt(linkedMoudleID.getLinkedmodule()));
                                jobj.put("allowcrossmodule", true);
                                jobj.put("iscustomextrajoin", isCustomExtrajoin);
                                jobj.put("isMeasureItem", false);
                                jobj.put("properties", getFieldsProperties(jobj));
                                if (linkedMoudleID.getLinkedmodule().equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId)) || linkedMoudleID.getLinkedmodule().equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId)) && fieldParam.getCustomcolumn() == 1) {
                                    jobj.put("dbcolumnname", "id");
                                    jobj.put("reftablename", "accjedetailcustomdata");
                                    jobj.put("reftablefk", "recdetailId");
                                    jobj.put("iscustomextrajoin", true);

                                    String document = "'advancepayment','creditnote','debitnote','gl', 'invoice', 'refund', 'loan'";

                                    JSONObject customFieldsExtraJoin = new JSONObject();
                                    JSONArray customFieldsExtraJoinArray = new JSONArray();
                                    JSONObject receiptJobj = null;

                                    mappingCustomResult = accCustomerReportServiceDao.getCustomReportsCustomFieldsMapping(linkedMoudleID.getLinkedmodule(), true, document);
                                    if (mappingCustomResult != null) {
                                        List<AccCustomReportsCustomFieldsMapping> list = mappingCustomResult.getEntityList();
                                        for (int i = 0; i < list.size(); i++) {
                                            receiptJobj = new JSONObject(jobj.toString());;

                                            String id = receiptJobj.getString("id") + i;
                                            receiptJobj.put("id", id);
                                            receiptJobj.put("defaultHeader", getDocumentName(list.get(i).getMappingFor()) + " " + fieldParam.getFieldlabel());
                                            receiptJobj.put("displayName", getDocumentName(list.get(i).getMappingFor()) + " " + fieldParam.getFieldlabel());

                                            if (linkedMoudleID.getLinkedmodule().equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {
                                                if (list.get(i).getMappingFor().equals("refund")) {
                                                    receiptJobj.put("reftabledatacolumn", "if(isnull(receiptadvancedetail.advancedetailid),NULL,accjedetailcustomdata." + jobj.getString("reftabledatacolumn").trim() + ")");
                                                } else if (list.get(i).getMappingFor().equals("advancepayment")) {
                                                    receiptJobj.put("reftabledatacolumn", "if(isnull(receiptadvancedetail.advancedetailid),accjedetailcustomdata." + jobj.getString("reftabledatacolumn").trim() + ",NULL)");
                                                }
                                            } else if (linkedMoudleID.getLinkedmodule().equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId))) {
                                                if (list.get(i).getMappingFor().equals("refund")) {
                                                    receiptJobj.put("reftabledatacolumn", "if(payment.paymentwindowtype=1,NULL,accjedetailcustomdata." + jobj.getString("reftabledatacolumn").trim() + ")");
                                                } else if (list.get(i).getMappingFor().equals("advancepayment")) {
                                                    receiptJobj.put("reftabledatacolumn", "if(payment.paymentwindowtype=1,accjedetailcustomdata." + jobj.getString("reftabledatacolumn").trim() + ",NULL)");
                                                }
                                            }

                                            customFieldsExtraJoin = new JSONObject();
                                            customFieldsExtraJoinArray = new JSONArray();

                                            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(DefaultHeader.class.getName(), list.get(i).getDefaultheaderid());
                                            DefaultHeader mappingDH = (DefaultHeader) objItr.getEntityList().get(0);
                                            receiptJobj.put("dbtablename", mappingDH.getReftablename());

                                            extrainnerjoin = " left join " + mappingDH.getReftablename() + " on " + mappingDH.getReftablename().trim() + "." + mappingDH.getReftablefk().trim() + " = " + mappingDH.getDbTableName().trim() + "." + mappingDH.getDbcolumnname() + " ";
                                            customFieldsExtraJoin.put(mappingDH.getReftablename(), extrainnerjoin);
                                            customFieldsExtraJoinArray.put(customFieldsExtraJoin);
                                            receiptJobj.remove("customfieldsextrajoin");
                                            receiptJobj.put("customfieldsextrajoin", customFieldsExtraJoinArray);
                                            jobj.put("properties", getFieldsProperties(receiptJobj));
                                            jresult.append("data", receiptJobj);
                                        }
                                    }
                                } else {
                                    jresult.append("data", jobj);
                                }
                            }

                         
                        
                            
                            
                        
                        
                        } catch (JSONException ex) {
                            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }


                    // Measure field data
                    result = null;
                    mappingResult = null;
                    result = accCustomerReportServiceDao.getMeasureFields(linkedMoudleID.getLinkedmodule());
                    if (result != null) {
                        List<AccCustomReportsMeasuresFields> list = result.getEntityList();
                        try {
                            for (AccCustomReportsMeasuresFields crmf : list) {
                                JSONObject jobj = new JSONObject();
                                jobj.put("isMeasureItem", true);
                                jobj.put("columntype", getCrossModuleNameForModuleID(linkedMoudleID.getLinkedmodule(), "Measures"));
                                jobj.put("crossJoinMainTable", getModuleMainTable(getModuleNameForModuleID(linkedMoudleID.getLinkedmodule())));
                                jobj.put("crossJoinModuleId", Integer.parseInt(linkedMoudleID.getLinkedmodule()));
                                jobj.put("defaultHeader", crmf.getMeasurefieldname());
                                jobj.put("displayName", crmf.getMeasurefielddisplayname());
                                jobj.put("xtype", Integer.parseInt(crmf.getXtype()));
                                jobj.put("mainTable", mainTable);
                                jobj.put("customfield", false);
                                jobj.put("iscustomextrajoin", false);
                                jobj.put("allowcrossmodule", true);
                                jobj.put("id", crmf.getMeasurefieldid());

                                mappingResult = accCustomerReportServiceDao.getMeasureFieldMappings(crmf.getMeasurefieldid());
                                if (mappingResult != null) {
                                    List<AccCustomReportsMeasuresFieldsMapping> crmfm = mappingResult.getEntityList();
                                    Iterator crmfm_itr = crmfm.iterator();
                                    while (crmfm_itr.hasNext()) {
                                        JSONObject mappingJSONObj = new JSONObject();
                                        String joinTableName = "";
                                        AccCustomReportsMeasuresFieldsMapping obj = (AccCustomReportsMeasuresFieldsMapping) crmfm_itr.next();
                                        mappingJSONObj.put("id", obj.getId());
                                        mappingJSONObj.put("defaultheaderid", obj.getDefaultheaderid());
                                        mappingJSONObj.put("measurefieldid", obj.getMeasurefieldid());
                                        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(DefaultHeader.class.getName(), obj.getDefaultheaderid());
                                        DefaultHeader mappingDH = (DefaultHeader) objItr.getEntityList().get(0);
                                        mappingJSONObj.put("dbcolumnname", mappingDH.getDbcolumnname());
                                        mappingJSONObj.put("dbtabletame", mappingDH.getDbTableName());
                                        mappingJSONObj.put("reftabledatacolumn", mappingDH.getReftabledatacolumn());
                                        mappingJSONObj.put("reftablename", mappingDH.getReftablename());
                                        mappingJSONObj.put("reftablefk", mappingDH.getReftablefk());
                                        //mappingJSONObj.put("defaultHeader", mappingDH.getReftablename()+"."+mappingDH.getReftablefk());
                                        mappingJSONObj.put("defaultHeader", mappingDH.getReftablename() + "." + mappingDH.getReftabledatacolumn());
                                        mappingJSONObj.put("isDataIndex", obj.getIsDataIndex());
                                        if (mappingDH.getReftablename() == null || getModuleMainTable(getModuleNameForModuleID(linkedMoudleID.getLinkedmodule())).equalsIgnoreCase(mappingDH.getReftablename().trim())) {
                                            joinTableName = mappingDH.getDbTableName().trim();
                                        } else {
                                            joinTableName = mappingDH.getReftablename().trim();
                                        }
                                        //extrainnerjoin = " left join " + joinTableName + " on " + mappingDH.getDbTableName().trim() + "." + mappingDH.getReftablename().trim() + " = " + mappingDH.getReftablename().trim() + "." + mappingDH.getReftablefk().trim() + " ";
                                        //extrainnerjoin = " left join " + joinTableName + " on " + mappingDH.getReftablename().trim() + "." + mappingDH.getReftablefk().trim() + " = " + mappingDH.getDbTableName().trim() + "." + mappingDH.getReftablename().trim()  +" ";
                                        if (!getModuleMainTable(getModuleNameForModuleID(linkedMoudleID.getLinkedmodule())).equalsIgnoreCase(joinTableName)) {
                                            extrainnerjoin = " left join " + joinTableName + " on " + mappingDH.getReftablename().trim() + "." + mappingDH.getReftablefk().trim() + " = " + mappingDH.getDbTableName().trim() + "." + mappingDH.getDbcolumnname().trim() + " ";
                                        }
                                        mappingJSONObj.put("extrainnerjoin", extrainnerjoin);
                                        jobj.append("mappingJSONObj", mappingJSONObj);
                                    }
                                    jobj.put("properties", getFieldsProperties(jobj));
                                    jobj.put("moduleName", getCrossModuleNameForModuleID(linkedMoudleID.getLinkedmodule(),"Measures"));
                                    jresult.append("data", jobj);
                                }
                            }
                        } catch (JSONException ex) {
                            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                }
            }

        }
        try {
            if (moduleCategory.equals(CustomReportConstants.Reports_ModuleCategoryName)) {
                String reportUrl = accCustomerReportServiceDao.getReportURLFromReportList(moduleID);
                jresult.put("reportUrl", reportUrl);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jresult;

    }

    /**
     * This method saves the created report category
     *
     * @param selectedRowsJSONData
     * @param valueMap
     * @return KwlReturnObject.
     * @exception ServiceException .
     * @see ServiceException
     */
    @Override
    public JSONObject saveOrUpdateCustomReport(Map valueMap, JSONArray filterArray) throws ServiceException {

        KwlReturnObject result = null;
        JSONObject jresult = new JSONObject();
        ArrayList paramList = new ArrayList();

        try {
            String selectedRows = (String) valueMap.get("selectedRows");
            boolean isPivot = (Boolean) valueMap.get("isPivot");
            boolean isEWayReport = (Boolean) valueMap.get("isEWayReport");
            String reportNo = (String) valueMap.get("reportNo");
            String moduleCatName = (String) valueMap.get("moduleCatName");
            String reportUrl = (String) valueMap.get("reportUrl");
            String parentReportId = (String) valueMap.get("parentReportId");
            JSONArray selectedRowsJSONData = new JSONArray();

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyID", (String) valueMap.get("companyID"));
            requestParams.put("userId", (String) valueMap.get("userId"));
            requestParams.put("reportNo", reportNo);
            requestParams.put("reportName", (String) valueMap.get("reportName"));
            requestParams.put("isEdit",(Boolean) valueMap.get("isEdit"));
            boolean isReportNameExists = false;
            boolean isReportWithNameExists = false;
            isReportNameExists = isCustomReportNameExists(requestParams);
            isReportWithNameExists = isCustomReportExists(requestParams);

            ReportMaster accCustomReport = new ReportMaster();
            if (isReportWithNameExists) {
                KwlReturnObject accCustomReportKwlObj = accCustomerReportServiceDao.getCustomReportByReportNoAndCompanyId(requestParams);
                accCustomReport = (ReportMaster) accCustomReportKwlObj.getEntityList().get(0);
            } else {
                accCustomReport.setName((String) valueMap.get("reportName"));
                accCustomReport.setDescription((String) valueMap.get("reportDesc"));
            }
            if (isReportWithNameExists) {
                KwlReturnObject accCustomReportKwlObj = accCustomerReportServiceDao.getCustomReportByReportNoAndCompanyId(requestParams);
                accCustomReport = (ReportMaster) accCustomReportKwlObj.getEntityList().get(0);
            } else {
                accCustomReport.setID(UUID.randomUUID().toString());
                accCustomReport.setName((String) valueMap.get("reportName"));
                accCustomReport.setDescription((String) valueMap.get("reportDesc"));
                accCustomReport.setIspivot(isPivot);
                accCustomReport.seteWayReport(isEWayReport);
            }
            
            if (moduleCatName.equals(CustomReportConstants.Reports_ModuleCategoryName)) {
                accCustomReport.setWidgetURL(reportUrl);
                accCustomReport.setParentreportid(parentReportId);
            } else {
                accCustomReport.setModuleid((String) valueMap.get("moduleID"));          //if category is not Report then set Module otherwise Null will be set by default      
            }

            JSONArray columnConfigJSON = new JSONArray();
            JSONObject pivotConfigJSON = null;
            JSONObject selectedRowsJSON = new JSONObject(selectedRows);
            columnConfigJSON = selectedRowsJSON.getJSONArray("columnConfig");
            if (isPivot) {//In case of pivot report, selectedRows consists of columnConfig and pivotConfig, otherwise only columnConfig is present
                pivotConfigJSON = selectedRowsJSON.getJSONObject("pivotConfig");
            }
            JSONObject reportColumnsJobj = updateFormulaFieldIDsInReportJson(columnConfigJSON, pivotConfigJSON);//To replace formula field's ID's with UUID in column config and pivot config
            columnConfigJSON = reportColumnsJobj.optJSONArray("columnConfig");
            pivotConfigJSON = reportColumnsJobj.optJSONObject("pivotConfig");

            if(isPivot) {
                selectedRowsJSON.put("columnConfig", columnConfigJSON);
                selectedRowsJSON.put("pivotConfig", pivotConfigJSON);
                accCustomReport.setReportjson(selectedRowsJSON.toString());
            } else {//In case of non-pivot report only column config is stored into reportJson as pivot config is null
                selectedRowsJSON.put("columnConfig", columnConfigJSON);
                accCustomReport.setReportjson(selectedRowsJSON.toString());
            }

            if (filterArray.length() > 0) {
                accCustomReport.setFilterjson(filterArray.toString());
            } else {
                accCustomReport.setFilterjson(null);
            }
            ModuleCategory modCat = new ModuleCategory();
            modCat.setModuleCatId((String) valueMap.get("moduleCatId"));
            accCustomReport.setReportmodulecategory(modCat);
            Modules module = null;
            if (!((String) valueMap.get("moduleCatName")).equals(CustomReportConstants.Reports_ModuleCategoryName)) {  //if category is not Report then set Module otherwise set Null
                module = new Modules();
                module.setId((String) valueMap.get("moduleID"));
            }
            Boolean isEdit = (Boolean) valueMap.get("isEdit");
            accCustomReport.setModuleid((String) valueMap.get("moduleID"));
            accCustomReport.setCompanyId((String) valueMap.get("companyID"));
            if (!isEdit) {
                long createdon = new java.util.Date().getTime();
                accCustomReport.setCreatedon(createdon);
            } else {
                long updatedon = new java.util.Date().getTime();
                accCustomReport.setUpdatedon(updatedon);
            }
            String sqlquery = " ";
            String moduleId ="";
            if(!moduleCatName.equals(CustomReportConstants.Reports_ModuleCategoryName)) {
                moduleId = (String) valueMap.get("moduleID");
            }

            if(!moduleCatName.equals(CustomReportConstants.Reports_ModuleCategoryName)) {
                Map querymap = buildCustomReportSqlQuery(columnConfigJSON, String.valueOf(moduleId), false, valueMap, paramList,new JSONObject());
                if (querymap.get(CustomReportConstants.sqlquery) != null) {
                    sqlquery = String.valueOf(querymap.get(CustomReportConstants.sqlquery));
                }
            }
            //saving filter sqlmap
            try {
                if (filterArray.length() > 0) {
                    JSONObject dataIndexObject = builddataIndexObject(columnConfigJSON, String.valueOf(moduleId));
                    Map<String, Object> sqlqueryMap = new HashMap<String, Object>();
                    sqlqueryMap.put(CustomReportConstants.sqlquery, sqlquery);
                    sqlqueryMap.put("userId", (String) valueMap.get("userId"));
                    Map filterquery = getFilterQuery(sqlqueryMap, filterArray, dataIndexObject);
                    sqlquery = String.valueOf(filterquery.get(CustomReportConstants.sqlquery));
                }

            } catch (Exception ex) {
                Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

            accCustomReport.setReportsql(sqlquery);
            if (isReportWithNameExists) {   //report exists and name is equal to reportName
                result = accCustomerReportServiceDao.saveOrUpdateCustomReport(accCustomReport, valueMap);   //updating existing report
                jresult.put("reportNo", reportNo);
                jresult.put("success", true);
                jresult.put(CustomReportConstants.MESSAGE_KEY, "acc.CustomReport.ReportUpdationSuccess");
            } else {
                if (!isReportNameExists) { //report does not exists and reportName deos not exists, in this case report will be saved
                    result = accCustomerReportServiceDao.saveOrUpdateCustomReport(accCustomReport, valueMap);   //saving report as a new entry
                    List resultList = result.getEntityList();
                    reportNo = (String) resultList.get(0);
                    jresult.put("reportNo", reportNo);
                    jresult.put("success", true);
                    jresult.put(CustomReportConstants.MESSAGE_KEY, "acc.CustomReport.ReportCreationSuccess");
                } else {    //in this case user will be prompted to provide new name for reportName already exists
                    jresult.put("reportNo", reportNo);
                    jresult.put("success", false);
                    jresult.put(CustomReportConstants.MESSAGE_KEY, "acc.CustomReport.dupReportNameEdit");
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }


        return jresult;

    }

    //To replace IDs of formula fields with UUID before saving the report
    public JSONObject updateFormulaFieldIDsInReportJson(JSONArray columnConfigJSON, JSONObject pivotConfigJSON) {
        JSONObject reportJSON = new JSONObject();
        try {
            JSONArray newColumnConfigJSON = new JSONArray();
            JSONObject newPivotConfigJSON = new JSONObject();
            JSONArray pivotLeftAxisJArr = null;
            JSONArray pivotTopAxisJArr = null;
            JSONArray pivotAggregateJArr = null;
            if (pivotConfigJSON != null) {
                pivotLeftAxisJArr = pivotConfigJSON != null ? pivotConfigJSON.optJSONArray("leftAxis") : null;
                pivotTopAxisJArr = pivotConfigJSON != null ? pivotConfigJSON.optJSONArray("topAxis") : null;
                pivotAggregateJArr = pivotConfigJSON != null ? pivotConfigJSON.optJSONArray("aggregate") : null;
            }
            for (int j = 0; j < columnConfigJSON.length(); j++) {
                JSONObject jsonobj = columnConfigJSON.getJSONObject(j);
                if (jsonobj.has(Constants.isforformulabuilder) && jsonobj.optBoolean(Constants.isforformulabuilder, false) == true) {
                    String uuid = UUID.randomUUID().toString();
                    jsonobj.put("id", uuid);
                    if(pivotLeftAxisJArr != null) {
                        pivotLeftAxisJArr = updateFormulaFieldIDsInPivotColumns(pivotLeftAxisJArr, jsonobj);
                    }
                    if(pivotTopAxisJArr != null) {
                        pivotTopAxisJArr = updateFormulaFieldIDsInPivotColumns(pivotTopAxisJArr, jsonobj);
                    }
                    if(pivotAggregateJArr != null) {
                        pivotAggregateJArr = updateFormulaFieldIDsInPivotColumns(pivotAggregateJArr, jsonobj);
                    }
                }
                newColumnConfigJSON.put(jsonobj);
            }
            if (pivotConfigJSON != null) {
                newPivotConfigJSON.put("leftAxis", pivotLeftAxisJArr);
                newPivotConfigJSON.put("topAxis", pivotTopAxisJArr);
                newPivotConfigJSON.put("aggregate", pivotAggregateJArr);
            }
            reportJSON.put("columnConfig", columnConfigJSON);
            reportJSON.put("pivotConfig", newPivotConfigJSON);
        } catch (JSONException ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return reportJSON;
    }

    //To replace IDs of formula fields with their new IDs 
    public JSONArray updateFormulaFieldIDsInPivotColumns (JSONArray pivotColumns, JSONObject column) {
        JSONArray newPivotColumns = new JSONArray();
        try {
            for (int k = 0; k < pivotColumns.length(); k++) {
                JSONObject columnObj = pivotColumns.optJSONObject(k);
                if (columnObj != null && columnObj.has(Constants.isforformulabuilder) && columnObj.optBoolean(Constants.isforformulabuilder, false) == true && StringUtil.equal(columnObj.getString("defaultHeader"), column.getString("defaultHeader"))) {
                    String id = column.optString("id");
                    columnObj.put("id", id);
                    columnObj.put("sortIndex", id);
                    columnObj.put("dataIndex", id);
                }
                newPivotColumns.put(columnObj);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return newPivotColumns;
    }

    public String getDocumentName(String mappingFor){
        String document="";
        switch (mappingFor) {
            case "advancepayment":
                document = CustomReportConstants.Acc_Make_Payment_Advance_Payment;
                break;
            case CustomReportConstants.Acc_Debit_Note:
                document = CustomReportConstants.Acc_Make_Payment_Debit_Note;
                break;
            case "gl":
                document = CustomReportConstants.Acc_Make_Payment_GL;
                break;
            case CustomReportConstants.Acc_Sales_Invoice:
                document = CustomReportConstants.Acc_Make_Payment_Invoice;
                break;
            case "refund":
                document = CustomReportConstants.Acc_Make_Payment_Refund;
                break;
            case "loan":
                document = CustomReportConstants.Acc_Receive_Payment_Loan;
                break;
            case CustomReportConstants.Acc_Credit_Note:
                document = CustomReportConstants.Acc_Make_Payment_Credit_Note;
                break;
        }
        return document;
    }
    
    public String getDocumentDBTable(String mappingFor){
        String document="";
        switch (mappingFor) {
            case "advancepayment":
                document = "receiptadvancedetail";
                break;
            case "debitnote":
                document = "debitnotepayment";
                break;
            case "gl":
                document = "receiptdetailotherwise";
                break;
            case "invoice":
                document = "receiptdetails";
                break;
            case "refund":
                document = "receiptadvancedetail";
                break;
            case "loan":
                document = "receiptdetailsloan";
                break;
            case "linkeddebitnote":
                document = "linkdetailreceipttodebitnote";
                break;
            case "linkedinvoice":
                document = "linkdetailreceipt";
                break;
            case "linkedrefund":
                document = "linkdetailreceipttoadvancepayment";
                break;
        }
        return document;
    }
    //For product module table name was given by earlier generic function was not right. Hence this function to be called all such modules
    private String getCrossModuleCustomFieldTableName(int moduleid){
        String tablename=" ";
        switch (moduleid) {
            case 30:
                tablename = "accproductcustomdata";
                break;
            default:
                tablename=" ";
        }
        return tablename;
    }
    /**
     * This method builds the select query while saving the report
     *
     * @param JSON
     * @param moduleid
     * @return finalQuery
     *
     */
    public Map buildCustomReportSqlQuery(JSONArray json, String moduleid, boolean isTotalCount, Map valueMap, ArrayList paramList,JSONObject dataIndexJSONObject) throws ServiceException, JSONException {
        String finalQuery = "";
        String conditionsToQuery = "";
        String groupByClause = "";
        String countQuery = "", billid = "",crossJoinMainTable="",linkedDocBillId = "",crossJoinModuleID="",selectCountQuery="";
        ArrayList<String> refTableList = new ArrayList<String>();
        ArrayList<String> crossModuleRefTableList = new ArrayList<String>();
        StringBuilder corssFielStringBuilder = new StringBuilder();
        ArrayList<String> dbTableList = new ArrayList<String>();
        LinkedHashMap<String, String> joinMap = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> dataIndexList = new LinkedHashMap<String, String>();
//        ArrayList<String> dupList = new ArrayList<String>();
        ArrayList<String> fieldDupList = new ArrayList<String>();
        LinkedHashMap<String, ArrayList<String>> dataIndexListDup = new LinkedHashMap<String, ArrayList<String>>();
        HashMap<String, Object> queryMap = new HashMap<String, Object>();
        JSONArray jArr = reshuffledSelectedField(json);
        String mainTable = null, table = null,crossModuleAsAlias = "", crossModuleTable = "", lineItemDiscountSelectColumn = "",crossModuleAsAliasKey = "";
        int cntVal = 0,crossJoinIndexVal=0;
        Map<String,String>crossModuleAsAliasMap=new HashMap<String,String>();
        boolean isDiscount = false,hasAccount=false,isCrossDiscount=false;
        boolean isDeleted = Boolean.parseBoolean((String) valueMap.get("deleted"));
        boolean isPivot = (valueMap.get("isPivot") !=null) ? (Boolean)(valueMap.get("isPivot")):false;
        boolean isPendingapproval = Boolean.parseBoolean((String) valueMap.get("pendingapproval"));
        boolean isLeaseSO = (Boolean) valueMap.get("isLeaseFixedAsset");
        boolean showGLFlag = valueMap.containsKey("showGLFlag") ? (Boolean) valueMap.get("showGLFlag") : true;
        boolean showExpenseTypeTransactionsFlag = valueMap.containsKey("showExpenseTypeTransactionsFlag") ? (Boolean) valueMap.get("showExpenseTypeTransactionsFlag") : true;
        boolean lineCustRepeat = false,isFieldComboDataValueAdded=false,isCrossJoinLineItemPresent = false;
        boolean globalCustRepeat = false,isCrossModuleExtraJoinAdded2=false,iscustomFieldsExtraJoinAdded=false;
        boolean isGSTField=false;
        boolean isGSTSales=false;
        boolean appendDistinct=true;
        boolean setProductDifferentiator= true;
        boolean isFirstLineCustomForAsset= true;
        boolean appendExtraInnerJoinForLineLevelCustom= false;
        String eWayFilter = valueMap.containsKey("eWayFilter") ? (String) valueMap.get("eWayFilter") : "";
        String productDifferentiator="";
        StringBuilder stbCrossModuleSelectClause = new StringBuilder();
        StringBuilder stbCrossModuleJoinClause = new StringBuilder();
        ArrayList<String> addedColType = new ArrayList<String>();
        LinkedHashMap<String, String> stbCrossModuleSelectMap = new LinkedHashMap<String, String>();
        LinkedHashMap<String, String> stbCrossModuleJoinMap = new LinkedHashMap<String, String>();
        Set<String> defaultMappingSet = new HashSet<>();
//        HashMap<String, String> mainTableMap = new HashMap<String, String>();
        boolean customerBillingAddr = false;
        boolean customerShippingAddr = false;
        boolean billingAddrForPayment = false;
        boolean paymentHSN = false;
        boolean applyGrouping = false;
        String paymentGroupingTable="";

        for (int cnt = 0; cnt < jArr.length(); cnt++) {
            try {

                //logic to check if selected fields are of billing shipping for customer module
                //this logic is used to append the condition query later in code if the fields are from billing shipping customer module
                String header = jArr.getJSONObject(cnt).getString("defaultHeader");
                if (moduleid.equals(Constants.CUSTOMER_MODULE_UUID)) {
                    if (header.toLowerCase().contains("billing")) {
                        customerBillingAddr = true;
                    }
                    if (header.toLowerCase().contains("shipping")) {
                        customerShippingAddr = true;
                    }
                } else if (header.contains(CustomReportConstants.Acc_CGST) || header.contains(CustomReportConstants.Acc_SGST) || header.contains(CustomReportConstants.Acc_IGST) || header.contains(CustomReportConstants.Acc_UTGST) || header.contains(CustomReportConstants.Acc_CESS)) {
                    isGSTField = true;
                }
                if (moduleid.equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId))) {
                    if (header.trim().equalsIgnoreCase(CustomReportConstants.PAYEE_ADDRESS.trim()) || header.trim().equalsIgnoreCase(CustomReportConstants.EMAIL.trim())) {
                        billingAddrForPayment = true;
                    }
                }
                // Iterate over rows
                boolean isCreatedAliase = false;
                JSONObject jObj = jArr.getJSONObject(cnt);

                //To decide whether to add distinct or not in query to avoid duplicacy issues
                if(jObj.optBoolean("isLineItem",false)){
                    appendDistinct=false;
                }
                
                if(moduleid.equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))){
                    if (jObj.optBoolean("customfield") && !applyGrouping) {
                        paymentGroupingTable = jObj.optString("dbtablename");
                        applyGrouping=true;
                    }
                }
                
                if(jObj.optBoolean("isLineItem",false) && jObj.optBoolean("customfield", false) && !jObj.optBoolean("allowcrossmodule", false) && (moduleid.equals(String.valueOf(Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_FixedAssets_DisposalInvoice_ModuleId))) && isFirstLineCustomForAsset){
                    appendExtraInnerJoinForLineLevelCustom = true;
                }

                if (!jObj.optBoolean(Constants.isforformulabuilder, false)) {//avoid case in case of building for formula builder

                    ArrayList<String> dupList = new ArrayList<String>();
                    mainTable = jObj.optString("mainTable", "");
                    boolean iscustomervendorfieldsflag = jObj.optBoolean("iscustomervendorfieldsflag", false);
                    boolean linkingjoinflag = jObj.optBoolean("linkingjoinflag", false);
                    boolean isLineItem = jObj.optBoolean("isLineItem", false);
                    boolean customfield = jObj.optBoolean("customfield", false);
                    boolean isMeasureItem = jObj.optBoolean("isMeasureItem", false);
                    boolean isCustomExtraJoin = jObj.optBoolean("iscustomextrajoin", false);
                    boolean iscrossmodule = jObj.optBoolean("allowcrossmodule", false);

                    if ((moduleid.equals(String.valueOf(Constants.Acc_Credit_Note_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_Debit_Note_ModuleId))) && jObj.optString("defaultHeader", "").equals(Constants.Acc_Account_modulename)) {
                        hasAccount = true;
                    }
                    if (cnt == 0) {
                        StringBuilder neededJoinQuery = fetchDefaultFieldHeaderNeededMappings(mainTable, moduleid, joinMap, cntVal);
                        defaultMappingSet.add(neededJoinQuery.toString());
                    }
                if(!iscrossmodule) {
                        if (!isMeasureItem) {
                            if (!StringUtil.isNullOrEmpty(jObj.optString("dbcolumnname", ""))) {//&& !jObj.optBoolean("customfield", false)) {
                                String refTable = jObj.optString("reftablename", "");
                                String refTable1 = jObj.optString("reftablename", "");
                                String dbtablename = jObj.optString("dbtablename", "");
                                String reftablefk = jObj.optString("reftablefk", "");
                                String reftabledatacolumn = jObj.optString("reftabledatacolumn", "");
                                String innerjoin = jObj.optString("extrainnerjoin", "");
                                String dbcolumnname = jObj.optString("dbcolumnname", "");
                                String defaultHeaderId = jObj.optString("id", "");

                                if (StringUtil.isNullOrEmpty(refTable)) {
                                    refTable = jObj.getString("dbtablename");
                                    refTable1 = jObj.getString("dbtablename");
                                }
                                if (reftabledatacolumn.equals("discount")) {
                                    isDiscount = true;
                                    table = refTable;
                                    if (mainTable.equalsIgnoreCase(CustomReportConstants.Acc_Sales_Order)) {
                                        dataIndexList.put("((IF(sodetails.discountispercent ='1',(sodetails.discount/100)*(sodetails.baseuomquantity*sodetails.rate),sodetails.discount)) )", "#flatdiscount#");
                                    } else if (mainTable.equalsIgnoreCase(CustomReportConstants.Acc_Purchase_Order)) {
                                        dataIndexList.put("((IF(podetails.discountispercent ='1',(podetails.discount/100)*(podetails.baseuomquantity*podetails.rate),podetails.discount)) )", "#flatdiscount#");
                                    } else if (mainTable.equalsIgnoreCase(CustomReportConstants.Acc_Sales_Invoice)) {
                                        dataIndexList.put("((IF(discount.inpercent='T',(discount.discount/100)*(origamount),discount.discount)) )", "#flatdiscount#");
                                    } else if (mainTable.equalsIgnoreCase(CustomReportConstants.Acc_Vendor_Invoice)) {
                                        dataIndexList.put("((IF(discount.inpercent='T',(discount.discount/100)*(origamount),discount.discount)) )", "#flatdiscount#");
                                    }else if(mainTable.equalsIgnoreCase(CustomReportConstants.Acc_Quotation)){
                                        dataIndexList.put("((IF(quotationdetails.discountispercent ='1',(quotationdetails.discount/100)*(quotationdetails.baseuomquantity*quotationdetails.rate),quotationdetails.discount)) )", "#flatdiscount#");
                                    }

                                }
                                if (!StringUtil.isNullOrEmpty(jObj.optString("dbtablename", "")) && !jObj.has("defaultmappingJSONObj")) {
                                    if (!jObj.getString("dbtablename").equals(mainTable)) {//This is called when linking other tables rather than maintable
                                        refTableList.add(jObj.getString("dbtablename"));
                                        dbTableList.add(jObj.getString("dbtablename"));
                                        if (!StringUtil.isNullOrEmpty(jObj.optString("reftablefk", ""))) {// if reftablefk value is present then use join using this field
                                            if (iscustomervendorfieldsflag || linkingjoinflag) {//linking case and case of third table in picture
                                                joinMap.put(jObj.getString("dbtablename"), innerjoin);
                                            } else if (isLineItem) {
                                        if(!(moduleid.equals(String.valueOf(Constants.Acc_Debit_Note_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_Credit_Note_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) && !(jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_ID) || jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_Name) || jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_Type) || jObj.optString("defaultHeader").startsWith(CustomReportConstants.Rate_of_Depriciation) || jObj.optString("defaultHeader").startsWith(CustomReportConstants.Product_Tax_Class) || jObj.optString("defaultHeader").startsWith("Product Brand"))) {
                                                    if (!customfield) {
                                                        if(!jObj.optString("defaultHeader").equalsIgnoreCase(CustomReportConstants.Display_UOM_Mapping)){
//                                                            if (!(moduleid.equals(String.valueOf(Constants.Acc_Invoice_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId)))) {
                                                                joinMap.put(jObj.getString("dbtablename"), " left join " + jObj.getString("dbtablename") + " on " + jObj.getString("dbtablename") + "." + mainTable + " = " + mainTable + "." + jObj.optString("reftablefk") + " ");
//                                                            } else {
//                                                                joinMap.put(jObj.getString("dbtablename"), " left join " + jObj.getString("dbtablename") + " on " + jObj.getString("dbtablename") + "." + "id" + " = " + mainTable + "." + jObj.optString("dbcolumnname") + " ");
//                                                            }
                                                        }
                                                    } else {
                                                        if (!(moduleid.equals(String.valueOf(Constants.Acc_Invoice_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId)))) {
                                                            joinMap.put(jObj.getString("dbtablename"), " left join " + jObj.getString("dbtablename") + " on " + jObj.getString("dbtablename") + "." + mainTable + " = " + mainTable + "." + jObj.optString("dbcolumnname") + " ");
                                                        } else {
                                                            if(joinMap.containsKey(getDetailsTableForMainTable(mainTable))){
                                                            joinMap.put(jObj.getString("dbtablename"), " left join " + jObj.getString("dbtablename") + " on " + jObj.getString("dbtablename") + "." + "id" + " = " + getDetailsTableForMainTable(mainTable) + "." + jObj.optString("dbcolumnname") + " ");
                                                            }else{
                                                                
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                if (joinMap.containsKey(refTable)) {
                                                    //joinMap.remove(mapjObj.getString("reftablename"));
                                                    String duprefTable1 = refTable + cnt;
                                                    joinMap.put(duprefTable1, " left join " + jObj.getString("reftablename") + " as " + duprefTable1 + " on " + duprefTable1 + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " ");
                                                } else {
//                                            joinMap.put(jObj.getString("dbtablename"), " left join " + jObj.getString("dbtablename") + " on " + jObj.getString("dbtablename") + "." + mainTable + " = " + mainTable + "." + jObj.getString("dbcolumnname") + " ");
                                                    joinMap.put(refTable, " left join " + jObj.getString("reftablename") + " on " + refTable + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " ");
                                                }
                                            }
                                        } else { // otherwise fetch join constraint from constant variable
                                            if (iscustomervendorfieldsflag || linkingjoinflag) {
                                                joinMap.put(jObj.getString("dbtablename"), innerjoin);
                                                defaultMappingSet.add(innerjoin);
                                            } else if (isLineItem) {
                                                joinMap.put(jObj.getString("dbtablename"), " left join " + jObj.getString("dbtablename") + " on " + jObj.getString("dbtablename") + "." + mainTable + " = " + mainTable + "." + jObj.optString("reftablefk") + " ");
                                            } else {
                                                joinMap.put(refTable, " left join " + refTable + " on " + refTable + "." + jObj.getString("dbcolumnname") + " ");
                                            }
                                        }
                                    }
                                }
                                if ((!refTableList.contains(refTable) && !refTable.equals(mainTable)) || (customfield && isLineItem && (moduleid.equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))))) {
                                    refTableList.add(refTable);
                                    if (!StringUtil.isNullOrEmpty(jObj.optString("reftablefk", ""))) {// if reftablefk value is present then use join using this field
                                        String joinTable = !StringUtil.isNullOrEmpty(jObj.optString("dbtablename", "")) ? jObj.optString("dbtablename", "") : mainTable;
                                        if (iscustomervendorfieldsflag || linkingjoinflag && !jObj.has("defaultmappingJSONObj")) {
                                            if (joinMap.containsKey(jObj.getString("reftablename"))) {
                                                joinMap.put(refTable+cnt, " left join " + refTable + " as " + refTable + cnt +  " on " + dbtablename + "." + dbcolumnname + " = " + refTable + cnt + "." + reftablefk + " ");
                                            } else {
                                                joinMap.put(refTable, " left join " + refTable + " on " + dbtablename + "." + dbcolumnname + " = " + refTable + "." + reftablefk + " ");
                                            }
                                        } else if (isCustomExtraJoin && !lineCustRepeat && isLineItem) {//Custom Fields Extra join
                                            JSONArray customFieldsExtraJoin = jObj.optJSONArray("customfieldsextrajoin");
                                    if(customFieldsExtraJoin!=null) {
                                                for (int arrayCnt = 0; arrayCnt < customFieldsExtraJoin.length(); arrayCnt++) {
                                                    JSONObject objectInArray = customFieldsExtraJoin.getJSONObject(arrayCnt);
                                                    Iterator<?> keys = objectInArray.keys();
                                                    while (keys.hasNext()) {
                                                        String key = (String) keys.next();
                                                        String value = objectInArray.getString(key);
                                                        joinMap.put(key, value);
                                                        defaultMappingSet.add(value);
                                                    }
                                                }
                                            }
                                            if (!(isLineItem && (moduleid.equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId)) || moduleid.equals(Constants.Acc_Receive_Payment_ModuleId)))) {
                                                lineCustRepeat = true;
                                            }
                                        } else if (isCustomExtraJoin && !globalCustRepeat && !isLineItem) {
                                            JSONArray customFieldsExtraJoin = jObj.optJSONArray("customfieldsextrajoin");
                                    if(customFieldsExtraJoin!=null) {
                                                for (int arrayCnt = 0; arrayCnt < customFieldsExtraJoin.length(); arrayCnt++) {
                                                    JSONObject objectInArray = customFieldsExtraJoin.getJSONObject(arrayCnt);
                                                    Iterator<?> keys = objectInArray.keys();
                                                    while (keys.hasNext()) {
                                                        String key = (String) keys.next();
                                                        String value = objectInArray.getString(key);
                                                        joinMap.put(key, value);
                                                    }
                                                }
                                            }
                                            globalCustRepeat = true;
                                        } else {
                                            if (joinMap.containsKey(refTable)) {
                                                //joinMap.remove(mapjObj.getString("reftablename"));
                                                String duprefTable1 = refTable + cnt;
//                                                refTable = duprefTable1;
                                                if (joinMap.containsKey(jObj.getString("reftablename")) && !defaultMappingSet.contains(" left join " + jObj.getString("reftablename") + " on " + jObj.getString("reftablename") + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " ")) {
                                                    if (!defaultMappingSet.contains(" left join " + jObj.getString("reftablename") + " on " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " = " + jObj.getString("reftablename") + "." + jObj.getString("reftablefk") + " ")) {
                                                    joinMap.put(duprefTable1, " left join " + jObj.getString("reftablename") + " as " + duprefTable1 + " on " + duprefTable1 + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " ");
                                                    defaultMappingSet.add(" left join " + jObj.getString("reftablename") + " on " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " = " + jObj.getString("reftablename") + "." + jObj.getString("reftablefk") + " ");
                                                        jObj.put("reftablename", duprefTable1);
                                                }
                                                }
                                            } else {
                                                if (!((jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_ID) || jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_Name) || jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_Type)) && jObj.optBoolean("allowcrossmodule")) && jObj.optString("dbtablename").equals(getDetailsTableForMainTable(mainTable))) {
                                                    if (jObj.optString("dbtablename").equals(getDetailsTableForMainTable(mainTable)) && !joinMap.containsKey(getDetailsTableForMainTable(mainTable))) {
                                                        String crossJoinMainTableForeignKeyInDetailTable = accCustomerReportServiceDao.getFKofCrossJoinMainTable(mainTable, jObj.getString("dbtablename"));
                                                        String detailTableMissingJoin = " left join " + jObj.getString("dbtablename") + " on " + jObj.getString("dbtablename") + "." + crossJoinMainTableForeignKeyInDetailTable + " = " + mainTable + ".id ";
                                                            joinMap.put(jObj.getString("dbtablename"), detailTableMissingJoin);
                                                            defaultMappingSet.add(detailTableMissingJoin);
                                                        }
                                                    if (joinMap.containsKey(joinTable) || mainTable.equals(joinTable)) {
                                                        if (stbCrossModuleSelectMap.containsKey(refTable)) {
                                                            if (setProductDifferentiator) {
                                                                productDifferentiator = refTable + cnt;
                                                                setProductDifferentiator = false;
                                                            }
                                                            joinMap.put(refTable + cnt, " left join " + refTable + " as " + productDifferentiator + " on " + productDifferentiator + "." + jObj.getString("reftablefk") + " = " + joinTable + "." + jObj.getString("dbcolumnname") + " ");
                                                        } else {
                                                            joinMap.put(refTable, " left join " + refTable + " on " + refTable + "." + jObj.getString("reftablefk") + " = " + joinTable + "." + jObj.getString("dbcolumnname") + " ");
                                                        }
                                                    }
                                                } else if(joinMap.containsKey(joinTable) || mainTable.equals(joinTable)) {
                                                    if (stbCrossModuleSelectMap.containsKey(refTable)) {
                                                        if (setProductDifferentiator) {
                                                            productDifferentiator = refTable + cnt;
                                                            setProductDifferentiator=false;
                                                        }
                                                        joinMap.put(refTable+cnt, " left join " + refTable +" as " + productDifferentiator + " on " +productDifferentiator + "." + jObj.getString("reftablefk") + " = " + joinTable + "." + jObj.getString("dbcolumnname") + " ");
                                                    } else {
                                                        joinMap.put(refTable, " left join " + refTable + " on " + refTable + "." + jObj.getString("reftablefk") + " = " + joinTable + "." + jObj.getString("dbcolumnname") + " ");
                                                        defaultMappingSet.add(" left join " + refTable + " on " + refTable + "." + jObj.getString("reftablefk") + " = " + joinTable + "." + jObj.getString("dbcolumnname") + " ");
                                                    }
                                                }
                                            }
                                        }

                                        if (customfield && isLineItem && (moduleid.equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_FixedAssets_DisposalInvoice_ModuleId))) ) {
                                            if (joinMap.containsKey(refTable)) {
                                                refTable = refTable + cnt;
                                                joinMap.put(refTable, " left join " + jObj.getString("reftablename") + " as " + refTable + " on " + refTable + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " ");
                                            } else {
                                                joinMap.put(refTable, " left join " + refTable + " on " + refTable + "." + jObj.getString("reftablefk") + " = " + joinTable + "." + jObj.getString("dbcolumnname") + " ");
                                            }
                                            if((moduleid.equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) && jObj.has("colnum") && reftabledatacolumn.contains("(") && reftabledatacolumn.contains(")") && reftabledatacolumn.contains(",")){
                                                String fieldComboJoin = " ";
                                                if (joinMap.containsKey("fieldcombodata.id")) {
                                                    fieldComboJoin = " left join fieldcombodata as fieldcombodata" + cnt + " on fieldcombodata" + cnt + ".id = " + jObj.optString("reftablename") + cnt + "." + jObj.optString("colnum");
                                                    jObj.put("reftabledatacolumn", jObj.optString("reftabledatacolumn").replaceAll("fieldcombodata.", "fieldcombodata" + cnt + "."));
                                                    reftabledatacolumn = jObj.optString("reftabledatacolumn");
                                                    paymentHSN = true;
                                                    joinMap.put("fieldcombodata" + cnt + ".id", fieldComboJoin);
                                                } else {
                                                    fieldComboJoin = " left join fieldcombodata on fieldcombodata.id = " + jObj.optString("reftablename") + "." + jObj.optString("colnum")+" ";
                                                    paymentHSN = true;
                                                    joinMap.put("fieldcombodata.id", fieldComboJoin);
                                                }
                                                defaultMappingSet.add(fieldComboJoin);
                                            }
                                            refTableList.remove(refTable);
                                    lineCustRepeat =false;
                                        }

                                    } else { // otherwise fetch join constraint from constant variable
                                        joinMap.put(refTable, " left join " + refTable + " on " + refTable + "." + jObj.getString("dbcolumnname") + " ");
                                    }
                                } else if (refTableList.contains(refTable)) {//Creating alias
                                    String colname1 = jObj.optString("reftabledatacolumn", "").length() > 0 ? jObj.optString("reftabledatacolumn", "") : jObj.getString("dbcolumnname");
                            if(!jObj.optString("defaultHeader", "").equals(CustomReportConstants.TYPE))
                            {
                                        colname1 = refTable + "." + colname1;
                            } else  {
                                        colname1 = colname1;
                                    }
                                    if ((dbTableList.contains(dbtablename) || dbtablename.equals(mainTable)) && Boolean.valueOf(jObj.optString("isDataIndex", "false"))) {


                                        refTable1 = refTable + cnt;
                                        refTableList.add(refTable);
                                        String joinTable = !StringUtil.isNullOrEmpty(jObj.optString("dbtablename", "")) ? jObj.optString("dbtablename", "") : mainTable;

                                        if (!StringUtil.isNullOrEmpty(jObj.optString("reftablefk", ""))) {// if reftablefk value is present then use join using this field

                                            String value = joinTable + "." + jObj.getString("dbcolumnname");
                                            boolean isJoinPresent = false;

                                            for (String key : joinMap.keySet()) {
                                        if (joinMap.get(key).contains(value) && joinMap.get(key).contains(refTable+"."+jObj.getString("reftablefk"))) {
                                                    refTable1 = key;
                                                    isCreatedAliase = true;
                                                    isJoinPresent = true;
                                                    break;
                                                }
                                            }

                                            if (!isJoinPresent && !joinTable.equalsIgnoreCase(refTable)) {
                                                if(dataIndexJSONObject.has(defaultHeaderId)){
                                                    dataIndexJSONObject.getJSONObject(defaultHeaderId).put(CustomReportConstants.filtercolumnname,refTable1+"."+reftabledatacolumn);
                                                }
                                                joinMap.put(refTable1, " left join " + refTable + " as " + refTable1 + " on " + refTable1 + "." + jObj.getString("reftablefk") + " = " + joinTable + "." + jObj.getString("dbcolumnname") + " ");
                                                isCreatedAliase = true;
                                            }
                                        }
                                    }

                                    if (dataIndexList.containsKey(colname1)) {
                                        refTable1 = refTable + cnt;
                                        refTableList.add(refTable);
                                        String joinTable = !StringUtil.isNullOrEmpty(jObj.optString("dbtablename", "")) ? jObj.optString("dbtablename", "") : mainTable;
                                        if (!StringUtil.isNullOrEmpty(jObj.optString("reftablefk", ""))) {// if reftablefk value is present then use join using this field
                                                joinMap.put(refTable1, " left join " + refTable + " as " + refTable1 + " on " + refTable1 + "." + jObj.getString("reftablefk") + " = " + joinTable + "." + jObj.getString("dbcolumnname") + " ");
                                            isCreatedAliase = true;
                                        } else {
                                            joinMap.put(refTable, " left join " + refTable + " on " + refTable + "." + jObj.getString("dbcolumnname") + " ");
                                        }

                                    }
                                }

                                //dataIndexReftableMap.put("#" + jObj.getString("defaultHeader") + "#", refTable);
                                if (isCreatedAliase) {
                                    String colname = jObj.optString("reftabledatacolumn", "").length() > 0 ? jObj.optString("reftabledatacolumn", "") : jObj.getString("dbcolumnname");

                                    if (jObj.optString("defaultHeader", "").equals(CustomReportConstants.AMOUNT) && isLineItem) {
                                if(!(moduleid.equals(String.valueOf(Constants.Acc_Debit_Note_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_Credit_Note_ModuleId)))) {
                                            if (fieldDupList.contains(colname)) {
                                                dataIndexList.put(colname, "#" + jObj.getString("defaultHeader") + cnt + "#");
                                            } else {
                                                dataIndexList.put(colname, "#" + jObj.getString("defaultHeader") + "#");
                                                fieldDupList.add(colname);
                                            }
                                        } else {
                                    if (dataIndexList.containsKey(refTable+"."+colname)) {
                                                dupList.add("#" + jObj.getString("defaultHeader") + "#");
                                        dataIndexListDup.put(refTable+ cnt + "." +colname,dupList );
                                            } else {
                                        dataIndexList.put(refTable+"."+colname, "#" + jObj.getString("defaultHeader") + "#");
                                            }
                                        }

                                    } else if ((jObj.optString("defaultHeader", "").equals(CustomReportConstants.TERM_AMOUNT)) || (colname.contains("(") && colname.contains(")") && colname.contains(","))) { // Added for Term Amount Field to constuct the SQL query correctly
                                        if (dataIndexList.containsKey(colname)) {
                                            dupList.add("#" + jObj.getString("defaultHeader") + "#");
                                            dataIndexListDup.put(colname, dupList);
                                        } else {
                                            dataIndexList.put(colname, "#" + jObj.getString("defaultHeader") + "#");
                                        }
//                                dataIndexList.put(colname, "#" + jObj.getString("defaultHeader") + "#");
                                    } else {
                                if(!jObj.optString("defaultHeader", "").equals(CustomReportConstants.TYPE)) {
                                            dataIndexList.put(refTable1.concat(".").concat(colname), "#" + jObj.getString("defaultHeader") + "#");
                                        } else {
                                            dataIndexList.put(colname, "#" + jObj.getString("defaultHeader") + "#");
                                        }
                                    }

                                    isCreatedAliase = false;
                                } else {
                                    //String colname = jObj.optString("reftabledatacolumn", "").length() > 0 ? jObj.optString("reftabledatacolumn", "") : jObj.getString("dbcolumnname");
                                    if (!StringUtil.isNullOrEmpty(jObj.optString("reftabledatacolumn", ""))) {
                                        if (jObj.optString("defaultHeader", "").equals(CustomReportConstants.AMOUNT) && isLineItem) {
                                    if(!(moduleid.equals(String.valueOf(Constants.Acc_Debit_Note_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_Credit_Note_ModuleId)))) {
                                                dataIndexList.put(reftabledatacolumn, "#" + jObj.getString("defaultHeader") + "#");
                                            } else {
                                            dataIndexList.put(refTable+"."+reftabledatacolumn, "#" + jObj.getString("defaultHeader") + "#");
                                            }
                                        } else if (jObj.optString("defaultHeader", "").equals(CustomReportConstants.TERM_AMOUNT)) { // Added for Term Amount Field to constuct the SQL query correctly
                                            dataIndexList.put(reftabledatacolumn, "#" + jObj.getString("defaultHeader") + "#");
                                        } else if(jObj.optString("defaultHeader", "").startsWith(CustomReportConstants.Acc_Product_Name)){
                                            if (setProductDifferentiator) {
                                            dataIndexList.put(refTable+"."+reftabledatacolumn, "#" + jObj.getString("defaultHeader") + "#");
                                            } else {
                                                dataIndexList.put(productDifferentiator+"."+reftabledatacolumn, "#" + jObj.getString("defaultHeader") + "#");
                                            }
                                        }else {
                                            if (Boolean.parseBoolean(jObj.optString("isDataIndex", "true"))) {
                                                if (dataIndexList.containsKey(refTable + "." + reftabledatacolumn)) {
                                                    if (dataIndexListDup.containsKey(refTable + "." + reftabledatacolumn)) {
                                                        dupList = dataIndexListDup.get(refTable + "." + reftabledatacolumn);
                                                    }
                                                    dupList.add("#" + jObj.getString("defaultHeader") + "#");
                                                    if (refTable.equals(mainTable)) {
                                                        dataIndexListDup.put(refTable + "." + reftabledatacolumn, dupList);
                                                    } else {
                                                        dataIndexListDup.put(refTable + cnt + "." + reftabledatacolumn, dupList);
                                                    }
                                                } else {
                                                    if (dataIndexList.containsValue("#" + jObj.getString("defaultHeader") + "#")) {
                                                        // Iterate Linked Hash Map
                                                        Set<String> keys = dataIndexList.keySet();
                                                        for (String k : keys) {
                                                            if (("#" + jObj.getString("defaultHeader") + "#").equalsIgnoreCase(dataIndexList.get(k))) {
                                                                dataIndexList.remove(k);
                                                                break;
                                                            }
                                                        }
                                                        dataIndexList.put(refTable + "." + reftabledatacolumn, "#" + jObj.getString("defaultHeader") + "#");
                                                        //dataIndexList.put(mapjObj.getString("defaultHeader"), "#" + jObj.getString("defaultHeader")+cnt + "#");
                                                    }
                                                    if (!jObj.optString("defaultHeader", "").equals(CustomReportConstants.TYPE) && !reftabledatacolumn.contains("(") && !reftabledatacolumn.contains(")") && !reftabledatacolumn.contains(",")) {
                                                        if(customfield && jObj.getInt("xtype")==4) { // Added join with FieldComboData in order to sort the result in the query itself
                                                            if(dataIndexList.containsKey("fieldcombodata.value") || paymentHSN) {
                                                                dataIndexList.put("fieldcombodata"+cnt+".value", "#" + jObj.getString("defaultHeader") + "#");
                                                            } else {
                                                                dataIndexList.put("fieldcombodata.value", "#" + jObj.getString("defaultHeader") + "#");
                                                            }
                                                            if(joinMap.containsKey("fieldcombodata.id")) {
                                                                joinMap.put("fieldcombodata"+cnt+".id", " left join fieldcombodata as fieldcombodata"+cnt+" on fieldcombodata"+cnt+".id "+"="+ (joinMap.containsKey(jObj.getString("reftablename")+cnt)? jObj.getString("reftablename")+cnt:jObj.getString("reftablename"))+ "." + reftabledatacolumn + " ");
                                                            } else {
                                                                joinMap.put("fieldcombodata.id", " left join fieldcombodata on fieldcombodata.id "+"="+ jObj.getString("reftablename") + "." + reftabledatacolumn + " ");
                                                            }
                                                        } else {
                                                            if (stbCrossModuleSelectMap.containsKey(refTable)) {
                                                                dataIndexList.put(productDifferentiator + "." + reftabledatacolumn, "#" + jObj.getString("defaultHeader") + "#");
                                                            } else {
                                                                dataIndexList.put((jObj.getString("reftablename").equals("") ? jObj.optString("dbtablename") : jObj.getString("reftablename")) + "." + reftabledatacolumn, "#" + jObj.getString("defaultHeader") + "#");
                                                            }
                                                        }
                                                    } else {
                                                        if (joinMap.containsKey(refTable)) {
                                                                reftabledatacolumn = reftabledatacolumn.replace(jObj.getString("reftablename").trim() + ".", refTable.trim() + ".");
                                                            }
                                                        dataIndexList.put(reftabledatacolumn, "#" + jObj.getString("defaultHeader") + "#");
                                                    }
                                                }
                                            }
                                            //dataIndexList.put(refTable + "." + reftabledatacolumn, "#" + jObj.getString("defaultHeader") + "#");
                                        }
                                    } else if ((jObj.optString("defaultHeader", "").equals(CustomReportConstants.CLOSED_MANUALLY)) || (dbcolumnname.contains("(") && dbcolumnname.contains(")") && dbcolumnname.contains(","))) { // Added for Closed Manually Field to constuct the SQL query correctly
                                        dataIndexList.put(dbcolumnname, "#" + jObj.getString("defaultHeader") + "#");
                                    } else if (!StringUtil.isNullOrEmpty(jObj.optString("dbcolumnname", ""))) {
                                        if (dataIndexList.containsKey(refTable + "." + dbcolumnname)) {
                                    if(dataIndexListDup.containsKey(refTable + "." + dbcolumnname)){
                                                dupList = dataIndexListDup.get(refTable + "." + dbcolumnname);
                                            }
                                            dupList.add("#" + jObj.getString("defaultHeader") + "#");
                                            dataIndexListDup.put(refTable + "." + jObj.getString("dbcolumnname"), dupList);
                                        } else {
                                                if (jObj.optString("defaultHeader", "").contains(CustomReportConstants.Acc_CGST) || jObj.optString("defaultHeader", "").contains(CustomReportConstants.Acc_SGST) || jObj.optString("defaultHeader", "").contains(CustomReportConstants.Acc_IGST) || jObj.optString("defaultHeader", "").contains(CustomReportConstants.Acc_UTGST) || jObj.optString("defaultHeader", "").contains(CustomReportConstants.Acc_CESS)||jObj.optString("defaultHeader", "").equals(CustomReportConstants.Acc_E_Way_TAX_Rate) || jObj.optString("dbcolumnname").contains("(")) {
                                                    if(!dataIndexList.containsValue("#" + CustomReportConstants.Acc_CGST + "#") || !dataIndexList.containsValue("#" + CustomReportConstants.Acc_SGST + "#") || !dataIndexList.containsValue("#" + CustomReportConstants.Acc_IGST + "#") || !dataIndexList.containsValue("#" + CustomReportConstants.Acc_UTGST + "#") || !dataIndexList.containsValue("#" + CustomReportConstants.Acc_CESS + "#")|| !dataIndexList.containsValue("#" + CustomReportConstants.Acc_E_Way_TAX_Rate + "#")){
                                                        dataIndexList.put(dbcolumnname, "#" + jObj.getString("defaultHeader") + "#");
                                                    }
                                            } else {
                                            dataIndexList.put(refTable + "." + jObj.getString("dbcolumnname"), "#" + jObj.getString("defaultHeader") + "#");
                                        }
                                    }
                                }
                                    }

                                // Add Joins for extra mapping for default fields
                                JSONArray defaultMapjarrayObj = jObj.optJSONArray("defaultmappingJSONObj");
                                if (defaultMapjarrayObj != null) {
                                    for (int mapCount = 0; mapCount < defaultMapjarrayObj.length(); mapCount++) {
                                        String duprefTable1 = "";
                                        JSONObject mapjObj = defaultMapjarrayObj.getJSONObject(mapCount);
                                        if (!defaultMappingSet.contains(mapjObj.getString("defaultextrainnerjoin")) && !defaultMappingSet.contains(" left join " + mapjObj.optString("reftablename") + " on " + mapjObj.optString("reftablename") + "." + mapjObj.optString("dbtabletame") + " = " + mapjObj.optString("dbtabletame") + "." + mapjObj.optString("dbcolumnname") + " ")) {
                                            if (mainTable.equalsIgnoreCase(mapjObj.getString("reftablename").trim())) {
                                                joinMap.put(mapjObj.getString("dbtabletame"), mapjObj.getString("defaultextrainnerjoin"));
                                            } else {
                                                if (joinMap.containsKey(mapjObj.getString("reftablename"))) {
                                                    duprefTable1 = mapjObj.getString("reftablename") + cnt;
                                                    /*
                                                     * Checks for multiple join on same table 
                                                     */
                                                    String oldJoin = joinMap.get(mapjObj.getString("reftablename"));
                                                    String defaultextrainnerjoin = mapjObj.optString("defaultextrainnerjoin", "");
                                                    if (!joinMap.containsKey(mapjObj.getString("reftablename"))) {
                                                        joinMap.put(duprefTable1, " left join " + mapjObj.getString("reftablename") + " as " + duprefTable1 + " on " + duprefTable1 + "." + mapjObj.getString("reftablefk") + " = " + mapjObj.getString("dbtabletame") + "." + mapjObj.getString("dbcolumnname") + " ");
                                                        defaultextrainnerjoin = defaultextrainnerjoin.replace(mapjObj.getString("reftablename"), duprefTable1);
                                                    }
                                                    if (!StringUtil.isNullOrEmpty(defaultextrainnerjoin) && !joinMap.containsValue("defaultextrainnerjoin")) {
                                                        String dataTable = defaultextrainnerjoin.substring(0, defaultextrainnerjoin.indexOf(" on "));
                                                        String joinCondition = " " + defaultextrainnerjoin.substring(defaultextrainnerjoin.indexOf(" on "), defaultextrainnerjoin.length());
                                                        dataTable = dataTable.replace("left join", "").trim();
                                                        joinCondition = joinCondition.replace(" on ", "").trim();

                                                        String joinInvert[] = joinCondition.split("=");

                                                        String joinCondition1 = joinInvert[1] + " = " + joinInvert[0];

                                                        if (joinMap.containsKey(dataTable) && !joinMap.get(dataTable).contains(joinCondition) && !joinMap.get(dataTable).contains(joinCondition1) && !defaultMappingSet.contains(mapjObj.getString("defaultextrainnerjoin"))) {
                                                            joinCondition = joinCondition.replace(" " + dataTable + ".", " " + dataTable + cnt + ".");
                                                            defaultextrainnerjoin = " left join " + dataTable + " as " + dataTable + cnt + " on " + joinCondition + " ";
                                                            joinMap.put(dataTable + cnt, defaultextrainnerjoin);
                                                            defaultMappingSet.add(mapjObj.getString("defaultextrainnerjoin"));
                                                        }
                                                    }
                                                } else {
                                                    if (mapjObj.getString("dbtabletame").equals(getDetailsTableForMainTable(mainTable)) && !joinMap.containsKey(getDetailsTableForMainTable(mainTable))) {
                                                        String crossJoinMainTableForeignKeyInDetailTable = accCustomerReportServiceDao.getFKofCrossJoinMainTable(mainTable, mapjObj.getString("dbtabletame"));
                                                        String detailTableMissingJoin = " left join " + mapjObj.getString("dbtabletame") + " on " + mapjObj.getString("dbtabletame") + "." + crossJoinMainTableForeignKeyInDetailTable + " = " + mainTable + ".id ";
                                                        joinMap.put(mapjObj.getString("dbtabletame"), detailTableMissingJoin);
                                                        defaultMappingSet.add(detailTableMissingJoin);
                                                    }
                                                    if (stbCrossModuleSelectMap.containsKey(mapjObj.getString("reftablename"))) {
                                                        if (StringUtil.isNullOrEmpty(productDifferentiator)) {
                                                            if (setProductDifferentiator) {
                                                                productDifferentiator = mapjObj.getString("reftablename") + cnt;
                                                                setProductDifferentiator = false;
                                                            }
                                                        }
                                                        String productJoin = mapjObj.getString("defaultextrainnerjoin").replace("left join " + mapjObj.getString("reftablename"), "left join " + mapjObj.getString("reftablename") + " as " + productDifferentiator + " ");
                                                        productJoin = productJoin.replace(mapjObj.getString("reftablename") + ".", productDifferentiator + ".");
                                                        productJoin = productJoin.replace(mapjObj.getString("reftablename") + ".", productDifferentiator + ".");
                                                        joinMap.put(productDifferentiator, productJoin);
                                                    } else {
                                                        if (moduleid.equals(String.valueOf(Constants.Acc_Sales_Order_ModuleId)) && mapjObj.getString("reftablename").equalsIgnoreCase("uomschema")) {
                                                            joinMap.put(mapjObj.getString("reftablename"), mapjObj.getString("defaultextrainnerjoin") + " and uomschema.salesuom = uom.id  ");
                                                        } else {
                                                            String defaultJoin = mapjObj.getString("defaultextrainnerjoin");
                                                            if (!setProductDifferentiator && productDifferentiator.contains(mapjObj.optString("dbtabletame"))) {
                                                                defaultJoin = mapjObj.getString("defaultextrainnerjoin").replace(mapjObj.optString("dbtabletame")+".", productDifferentiator+".");
                                                        }
                                                            joinMap.put(mapjObj.getString("reftablename"), defaultJoin);
                                                            defaultMappingSet.add(defaultJoin);
                                                    }
                                                }
                                            }
                                        }
                                        }
                                        if (Boolean.valueOf(mapjObj.optString("isSelectDataIndex", "false"))) {

                                            // If required column is already present in dataIndexList with other refTable and reftable data column then remove it.
                                            if (dataIndexList.containsValue("#" + mapjObj.getString("defaultHeaderParentName") + "#")) {
                                                removeOldMappingForDataIndex(dataIndexList, mapjObj, moduleid);
                                            }

                                            String mappingDefaultHeader = mapjObj.getString("defaultHeader");
                                            if (!StringUtil.isNullOrEmpty(duprefTable1) && mappingDefaultHeader.contains(mapjObj.getString("reftablename")) && joinMap.containsKey(duprefTable1)) {
                                                mappingDefaultHeader = mappingDefaultHeader.replace(mapjObj.getString("reftablename").trim() + ".", duprefTable1.trim() + ".");
                                            }

                                            if (dataIndexList.containsKey(mappingDefaultHeader)) {

                                                if (dataIndexListDup.containsKey(mappingDefaultHeader)) {
                                                    dupList = dataIndexListDup.get(mappingDefaultHeader);
                                                }
                                                dupList.add("#" + jObj.getString("defaultHeader") + "#");
                                                dataIndexListDup.put(mappingDefaultHeader, dupList);
                                            } else {
                                                dataIndexList.put(mappingDefaultHeader, "#" + jObj.getString("defaultHeader") + "#");
                                            }
                                        }
                                    }
                                    if (jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_ID) || jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_Name) || jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_Type) || jObj.optString("defaultHeader").startsWith(CustomReportConstants.Rate_of_Depriciation) || jObj.optString("defaultHeader").startsWith(CustomReportConstants.Product_Tax_Class)) {
                                        if (jObj.getString("dbtablename").equals("product") && stbCrossModuleSelectMap.containsKey("product")) {
                                            joinMap.put(refTable, " left join " + refTable + " on " + refTable + "." + jObj.getString("reftablefk") + " = " + productDifferentiator + "." + jObj.getString("dbcolumnname") + " ");
                                        } else if (refTable.equals("product") && stbCrossModuleSelectMap.containsKey("product")) {
                                            if (setProductDifferentiator) {
                                                productDifferentiator = refTable + cnt;
                                                setProductDifferentiator = false;
                                            }
                                            joinMap.put(productDifferentiator, " left join " + refTable + " as " + productDifferentiator + " on " + productDifferentiator + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " ");
                                        } else {
                                            joinMap.put(refTable, " left join " + refTable + " on " + refTable + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " ");
                                        }
                                    } else if (!StringUtil.isNullOrEmptyWithTrim(jObj.optString("reftablefk", "")) && !joinMap.containsKey(refTable)){ // More join cases to be handled
                                        String joinString = " left join " + jObj.optString("reftablename") + " on " + jObj.optString("reftablename") + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " ";
                                        String joinStringAliased = " left join " + refTable + " on " + refTable + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " ";
                                        if (!joinMap.containsValue(joinString)) {
                                            joinMap.put(refTable, joinStringAliased);
                                    }
                                }
                                    }
                                // end

                            }
                            if (jObj.optString("defaultHeader").equals(CustomReportConstants.Acc_GRO_Invoice_Number) && jObj.optString("moduleName").equals(CustomReportConstants.goodsReceipt)) {
                                joinMap.put("goodsreceipt", " LEFT JOIN (SELECT goodsreceipt.grnumber, goodsreceiptorderlinking.linkeddocid, goodsreceiptorderlinking.docid FROM goodsreceipt INNER JOIN goodsreceiptorderlinking ON goodsreceipt.id = goodsreceiptorderlinking.linkeddocid) AS goodsreceipt ON goodsreceipt.docid = grorder.id ");
                            } else if (jObj.optString("defaultHeader").equals(CustomReportConstants.Acc_GRO_Invoice_Number) && jObj.optString("moduleName").equals(CustomReportConstants.deliveryOrder)) {
                                joinMap.put("invoice", " LEFT JOIN ( SELECT invoice.invoicenumber, dolinking.linkeddocid, dolinking.docid FROM invoice INNER JOIN dolinking ON invoice.id = dolinking.linkeddocid) AS invoice ON invoice.docid = deliveryorder.id");
                            }
                            
                            if (isFirstLineCustomForAsset && appendExtraInnerJoinForLineLevelCustom && (moduleid.equals(String.valueOf(Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId)))) {
                                isFirstLineCustomForAsset = false;
//                                if (!joinMap.containsKey("grdetails")) {
                                    joinMap.put("grdetails", " INNER JOIN grdetails ON goodsreceipt.id = grdetails.goodsreceipt and grdetails.purchasejedid = jedetail.id ");
//                                } else {
//                                    joinMap.put("grdetails" + cnt, " INNER JOIN grdetails as  grdetails" + cnt + " ON goodsreceipt.id = grdetails" + cnt + ".goodsreceipt and grdetails" + cnt + ".purchasejedid = jedetail.id ");
//                                }
                            } else if (isFirstLineCustomForAsset && appendExtraInnerJoinForLineLevelCustom && moduleid.equals(String.valueOf(Constants.Acc_FixedAssets_DisposalInvoice_ModuleId))) {
                                isFirstLineCustomForAsset = false;
                                if (!joinMap.containsKey("invoicedetails")) {
                                    joinMap.put("invoicedetails", " INNER JOIN invoicedetails ON invoice.id = invoicedetails.invoice and invoicedetails.salesjedid = jedetail.id ");
                                } else {
                                    joinMap.put("invoicedetails" + cnt, " INNER JOIN invoicedetails as  invoicedetails" + cnt + " ON invoice.id = invoicedetails" + cnt + ".invoice and invoicedetails" + cnt + ".salesjedid = jedetail.id ");
                                }
                            }
                        } else {
                            JSONArray mapjarrayObj = jObj.getJSONArray("mappingJSONObj");
                            //dataIndexList.put(jObj.getString("defaultHeader"),"#" + jObj.getString("defaultHeader") + "#");
                            for (int mapCount = 0; mapCount < mapjarrayObj.length(); mapCount++) {
                                JSONObject mapjObj = mapjarrayObj.getJSONObject(mapCount);

                                //dataIndexList.put(mapjObj.getString("defaultHeader"),"#"+jObj.getString("defaultHeader")+"#");
                                //if(!joinMap.containsKey(mapjObj.getString("extrainnerjoin"))){
                                //joinMap.put(mapjObj.getString("reftablename"),mapjObj.getString("extrainnerjoin"));
                                if (mainTable.equalsIgnoreCase(mapjObj.getString("reftablename").trim()) && !defaultMappingSet.contains(mapjObj.getString("extrainnerjoin"))) {
                                    joinMap.put(mapjObj.getString("dbtabletame"), mapjObj.getString("extrainnerjoin"));
                                } else {
                                    String defaultextrainnerjoin = mapjObj.optString("extrainnerjoin", "");
                                    String dataTable = defaultextrainnerjoin.substring(0, defaultextrainnerjoin.indexOf(" on "));
                                    String joinCondition = " " + defaultextrainnerjoin.substring(defaultextrainnerjoin.indexOf(" on "), defaultextrainnerjoin.length());
                                    dataTable = dataTable.replace("left join", "").trim();
                                    joinCondition = joinCondition.replace(" on ", "").trim();

                                    String joinInvert[] = joinCondition.split("=");

                                    String joinCondition1 = " left join "+dataTable+" on "+joinInvert[1].trim() + " = " + joinInvert[0].trim()+" ";
                                    
                                    if (!defaultMappingSet.contains(mapjObj.getString("extrainnerjoin")) && !defaultMappingSet.contains(joinCondition1)) {
                                        if (joinMap.containsKey(mapjObj.getString("reftablename"))) {
                                            //joinMap.remove(mapjObj.getString("reftablename"));
                                            String duprefTable1 = mapjObj.getString("reftablename") + cnt;
                                            joinMap.put(duprefTable1, " left join " + mapjObj.getString("reftablename") + " as " + duprefTable1 + " on " + duprefTable1 + "." + mapjObj.getString("reftablefk") + " = " + mapjObj.getString("dbtabletame") + "." + mapjObj.getString("dbcolumnname") + " ");
                                        } else {
                                            joinMap.put(mapjObj.getString("reftablename"), mapjObj.getString("extrainnerjoin"));
                                            defaultMappingSet.add(mapjObj.getString("extrainnerjoin"));
                                        }
                                    }
                                }

                                //if(mapCount==0) {
                                if (Boolean.valueOf(mapjObj.optString("isDataIndex", "false"))) {
                                    if (dataIndexList.containsKey(mapjObj.getString("defaultHeader"))) {
                                        String key = mapjObj.getString("defaultHeader");

                                        if (joinMap.containsKey(mapjObj.getString("reftablename") + cnt)) {
                                            key = mapjObj.getString("reftablename") + cnt + "." + mapjObj.getString("reftabledatacolumn");
                                        }

                                        if (dataIndexListDup.containsKey(key)) {
                                            dupList = dataIndexListDup.get(key);
                                        }
                                        dupList.add("#" + jObj.getString("defaultHeader") + "#");
                                        dataIndexListDup.put(mapjObj.getString("defaultHeader"), dupList);
                                    } else {
                                        dataIndexList.put(mapjObj.getString("defaultHeader"), "#" + jObj.getString("defaultHeader") + "#");
                                    }
                                }
                                //}
                            }
                        }
                    } else {
                        valueMap.put("crossJoinMainTable",jObj.optString("crossJoinMainTable", ""));
                        crossJoinMainTable = jObj.optString("crossJoinMainTable", "");
                        crossJoinModuleID = StringUtil.isNullOrEmpty(jObj.optString("linkedmodule", ""))?jObj.optString("crossJoinModuleId", ""):jObj.optString("linkedmodule", "");    
                        if (!isMeasureItem) {
                            /* In case of vendor Invoice Date*/
                            String reftablename = jObj.optString("reftablename", "").trim();
                            String refforeignkey = jObj.optString("reftablefk", "").trim();
//                            String reftabledatacolumnname = jObj.optString("reftabledatacolumn", "").trim();
                            boolean isdateflag = false;
                            if (!StringUtil.isNullOrEmpty(refforeignkey) && jObj.optInt("xtype") == Constants.DATEFIELD) {// when Vendor Invoice Date (Vi linked in PO Scenario)
                                isdateflag = true;
                            }
                            if(!jObj.optString("dbtablename").equals(CustomReportConstants.Acc_CUSTOMER)){
                                if (dataIndexList.containsKey(jObj.optString("dbtablename", "") + "." + jObj.optString("dbcolumnname", ""))) {
                                    if (dataIndexListDup.containsKey(jObj.optString("dbtablename", "") + "." + jObj.optString("dbcolumnname", ""))) {
                                        dupList = dataIndexListDup.get(jObj.optString("dbtablename", "") + "." + jObj.optString("dbcolumnname", ""));
                                    }
                            if(!customfield) {
//                                    dupList.add("#" + jObj.getString("defaultHeader") + "#");
                                        dupList.add(dataIndexList.get(jObj.optString("dbtablename", "") + "." + jObj.optString("dbcolumnname", "")));
                                    }
                                    dataIndexListDup.put(jObj.optString("dbtablename", "") + "." + jObj.optString("dbcolumnname", ""), dupList);
                                } else {
                            if(!customfield)
                            {
                                        if ((jObj.optString("dbcolumnname", "").contains("(") && jObj.optString("dbcolumnname", "").contains(")") && jObj.optString("dbcolumnname", "").contains(","))) {
                                    dataIndexList.put((jObj.optString("dbtablename").equals("") ? crossJoinMainTable: jObj.optString("dbtablename"))+ "." + "InvoiceAmount"+cnt, "#" + jObj.getString("defaultHeader") + "#");
                                        } else {
                                            dataIndexList.put(jObj.optString("dbtablename", "") + "." + jObj.optString("dbcolumnname", ""), "#" + jObj.getString("defaultHeader") + "#");
                                        }

                                    }
                                }
                        }else{
                                if(!dataIndexList.containsKey(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("dbcolumnname", ""))){
                                    dataIndexList.put(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("dbcolumnname", ""), "#" + jObj.getString("defaultHeader") + "#");
                                }else{
                                    dataIndexList.put(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("dbcolumnname", "")+cnt, "#" + jObj.getString("defaultHeader") + "#");
                                }
                            }
                            if (addedColType.contains(jObj.optString("columntype", "").substring(jObj.optString("columntype", "").indexOf("-"), jObj.optString("columntype", "").length()))) {
                                if ((jObj.optInt("xtype") == 4 && !jObj.optString("defaultHeader", " ").equalsIgnoreCase(CustomReportConstants.Product_Tax)) || customfield) {
                                    if (fieldDupList.contains(jObj.optString("reftabledatacolumn", ""))) {
                                        stbCrossModuleSelectClause.append(jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + " as " + jObj.optString("reftabledatacolumn", "") + cnt + ",");
                                    } else {
                                    if (stbCrossModuleSelectClause.toString().contains("fieldcombodata.value") && isFieldComboDataValueAdded == false ) { 
                                            stbCrossModuleSelectClause.append(jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + ",");
                                            isFieldComboDataValueAdded = true;
                                          } else if(fieldDupList.contains(jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", ""))){
                                            if (stbCrossModuleJoinClause.toString().contains("join " + jObj.optString("reftablename"))) {
                                                stbCrossModuleSelectClause.append(jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + " as " + jObj.optString("reftabledatacolumn", "") + cnt + " ,");
                                            } else {
                                                stbCrossModuleSelectClause.append(jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + " as " + jObj.optString("reftabledatacolumn", "") + cnt + " ,");
                                            }
                                        } else {
                                            if (stbCrossModuleJoinClause.toString().contains("join " + jObj.optString("reftablename"))) {
                                                stbCrossModuleSelectClause.append(jObj.optString("reftablename", "")+ "." + jObj.optString("reftabledatacolumn", "") + ",");
                                            } else {
                                                if (stbCrossModuleSelectClause.toString().contains("." + jObj.optString("reftabledatacolumn"))) {
                                                    stbCrossModuleSelectClause.append(jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + " as " + jObj.optString("reftabledatacolumn", "") + cnt + " ,");
                                                } else {
                                                    stbCrossModuleSelectClause.append(jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + ",");
                                                }

                                            }
                                            fieldDupList.add(jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", ""));
                                        }
                                    }
                                    fieldDupList.add(jObj.optString("reftabledatacolumn", ""));
                                } else {
                                    if (!jObj.optBoolean("isMeasureItem", false) && jObj.optString("defaultHeader").contains("Discount") && jObj.optString("defaultHeader").length()<10 && jObj.optBoolean("isLineItem", false) ) {
                                        isCrossDiscount = true;
                                        table = jObj.optString("crossJoinMainTable", "");
                                        if (jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.Acc_Sales_Order)) {
                                            stbCrossModuleSelectClause.append("((IF(sodetails.discountispercent ='1',(sodetails.discount/100)*(sodetails.baseuomquantity*sodetails.rate),sodetails.discount)))  as  '#flatdiscount#' ,");
                                        } else if (jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.Acc_Purchase_Order)) {
                                            stbCrossModuleSelectClause.append("((IF(podetails.discountispercent ='1',(podetails.discount/100)*(podetails.baseuomquantity*podetails.rate),podetails.discount)))  as  '#flatdiscount#' ,");
                                        } else if (jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.Acc_Sales_Invoice)) {
                                            stbCrossModuleSelectClause.append("((IF(discount.inpercent='T',(discount.discount/100)*(origamount),discount.discount)))  as  '#flatdiscount#'  ,");
                                        } else if (jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.Acc_Vendor_Invoice)) {
                                            stbCrossModuleSelectClause.append("((IF(discount.inpercent='T',(discount.discount/100)*(origamount),discount.discount)))   as  '#flatdiscount#'  ,");
                                        } else if (jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.Acc_Delivery_Order)) {
                                            stbCrossModuleSelectClause.append("((IF(dodetails.discountispercent='T',(dodetails.discount/100)*(rate),dodetails.discount)))   as  '#flatdiscount#'  ,");
                                        } else if (jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.Acc_Quotation)) {
                                            stbCrossModuleSelectClause.append("((IF(quotationdetails.discountispercent ='1',(quotationdetails.discount/100)*(quotationdetails.baseuomquantity*quotationdetails.rate),quotationdetails.discount))) as  '#flatdiscount#' ,");
//                                        dataIndexList.put("((IF(quotationdetails.discountispercent ='1',(quotationdetails.discount/100)*(quotationdetails.baseuomquantity*quotationdetails.rate),quotationdetails.discount)) )", "#flatdiscount#");
                                        } else if (jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.Acc_Goods_Receipt)) {
                                            stbCrossModuleSelectClause.append("((IF(grodetails.discountispercent ='1',(grodetails.discount/100)*(grodetails.baseuomquantity*grodetails.rate),grodetails.discount)))  as  '#flatdiscount#' ,");
                                        } else if (jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.Acc_Purchase_Return)) {
                                            stbCrossModuleSelectClause.append("((IF(prdetails.discountispercent ='1',(prdetails.discount/100)*(prdetails.baseuomquantity*prdetails.rate),prdetails.discount)))  as  '#flatdiscount#' ,");
                                        } else if (jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.Acc_Sales_Return)) {
                                            stbCrossModuleSelectClause.append("((IF(srdetails.discountispercent ='1',(srdetails.discount/100)*(srdetails.baseuomquantity*srdetails.rate),srdetails.discount)))  as  '#flatdiscount#' ,");
                                        }

                                        if (isCrossDiscount) {
                                            if (jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.Acc_Sales_Invoice)||jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.Acc_Vendor_Invoice)) {
                                                stbCrossModuleSelectClause.append(jObj.optString("reftablename") + ".inpercent ,");
                                            } else {
                                                stbCrossModuleSelectClause.append(jObj.optString("reftablename") + ".discountispercent ,");
                                            }
                                            lineItemDiscountSelectColumn = ", " + jObj.optString("crossJoinMainTable") + ".`#flatdiscount#`";
                                        }
                                    }
                                    if (fieldDupList.contains(jObj.optString("dbcolumnname", ""))) {
//                                        stbCrossModuleSelectClause.append(jObj.optString("dbtablename", "") + "." + jObj.optString("dbcolumnname", "") + " as " + jObj.optString("dbcolumnname", "") + cnt + " ,");
                                        if (!jObj.optBoolean("isLineItem", false)) {
                                            if (reftablename.equals("billingshippingaddresses")) {
                                                stbCrossModuleSelectClause.append("billingshippingaddresses" + "." + jObj.optString("reftabledatacolumn", "") + ",");
                                            } else {
                                                stbCrossModuleSelectClause.append(jObj.optString("dbtablename", "") + "." + jObj.optString("dbcolumnname", "") + " as " + jObj.optString("dbcolumnname", "") + cnt + " ,");
                                            }
                                        } else if (jObj.optString("reftabledatacolumn", "").contains("(") && jObj.optString("reftabledatacolumn", "").contains(")") && (jObj.optString("defaultHeader", "").contains("Amount") || jObj.optString("defaultHeader", "").contains(CustomReportConstants.CLOSED_MANUALLY) || jObj.optString("defaultHeader", "").contains(CustomReportConstants.UNIT_PRICE_INCLUDING_GST) || jObj.optString("defaultHeader", "").contains(CustomReportConstants.BaseQuantity)) && jObj.optBoolean("isLineItem", false)) {
                                            stbCrossModuleSelectClause.append(jObj.optString("reftabledatacolumn", "") + " as lineAmount" + cnt + ",");
                                        } else if ((stbCrossModuleSelectClause.toString().contains("." + jObj.optString("reftabledatacolumn", "")) && (jObj.optString("reftabledatacolumn", "").equals("discount") 
                                                )|| (stbCrossModuleSelectClause.toString().contains("." + jObj.optString("reftabledatacolumn", "")) && jObj.optString("reftabledatacolumn", "").equals("product")))) {
                                            stbCrossModuleSelectClause.append(jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + " as " + jObj.optString("reftabledatacolumn", "") + cnt + ",");
                                        } else if (stbCrossModuleSelectClause.toString().contains("." + jObj.optString("reftabledatacolumn"))) {
                                            stbCrossModuleSelectClause.append(jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + " as " + jObj.optString("reftabledatacolumn", "") + cnt + ",");
                                            jObj.put("reftabledatacolumn",jObj.optString("reftabledatacolumn", "") + cnt);
                                        } else if (jObj.optString("defaultHeader", "").contains(CustomReportConstants.BaseUOMQuantity)) {
                                            stbCrossModuleSelectClause.append(jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + " as baseUOMQty" + ",");
                                        } else {
                                            stbCrossModuleSelectClause.append(jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + ",");
                                        }

                                    } else {
                                        if (jObj.optString("defaultHeader", "").equals(CustomReportConstants.TERM_AMOUNT)) { // Added for Term Amount Field to constuct the SQL query correctly
                                            stbCrossModuleSelectClause.append(jObj.optString("reftabledatacolumn", "") + " as " + jObj.optString("reftablename", "") + " , ");
                                            if (stbCrossModuleJoinMap.containsKey(jObj.optString("crossJoinMainTable", ""))) {
                                                stbCrossModuleJoinMap.put(jObj.optString("crossJoinMainTable", ""), (stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")) + " left join " + jObj.getString("reftablename") + " on " + jObj.getString("reftablename") + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " "));
                                            } else {
                                                stbCrossModuleJoinMap.put(jObj.optString("crossJoinMainTable", ""), " left join " + jObj.getString("reftablename") + " on " + jObj.getString("reftablename") + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " ");
                                            }
                                            fieldDupList.add(jObj.optString("reftabledatacolumn", ""));
                                        } else {
                                            if ((jObj.optString("dbcolumnname", "").contains("(") && jObj.optString("dbcolumnname", "").contains(")") && jObj.optString("dbcolumnname", "").contains(","))) {
                                                stbCrossModuleSelectClause.append(jObj.optString("dbcolumnname", "") + " as InvoiceAmount" + cnt + ",");
                                            } else if(reftablename.equals("billingshippingaddresses")){
                                                stbCrossModuleSelectClause.append("billingshippingaddresses" + "." + jObj.optString("reftabledatacolumn", "") + ",");
                                            } else {
                                                if (!jObj.optBoolean("isLineItem", false)) {
                                                    stbCrossModuleSelectClause.append(jObj.optString("dbtablename", "") + "." + jObj.optString("dbcolumnname", "") + ",");
                                                } else {
                                                    if (!isCrossJoinLineItemPresent) {
                                                        // added to have corresponding details table and alias in select and sub query
                                                        if (CustomReportConstants.Acc_Purchase_Order_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) || CustomReportConstants.Acc_Purchase_Order.equalsIgnoreCase(jObj.optString("dbtablename", ""))) {
                                                            crossModuleTable = CustomReportConstants.Acc_Purchase_Order_Details;
                                                            crossModuleAsAliasKey = accCustomerReportServiceDao.getmoduledataRefPKColName(crossModuleTable);
                                                        } else if (CustomReportConstants.Acc_Quotation_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) || CustomReportConstants.Acc_Quotation.equalsIgnoreCase(jObj.optString("dbtablename", ""))) {
                                                            crossModuleTable = CustomReportConstants.Acc_Quotation_Details;
                                                            crossModuleAsAliasKey = accCustomerReportServiceDao.getmoduledataRefPKColName(crossModuleTable);
                                                        } else if (CustomReportConstants.Acc_Sales_Order_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) || CustomReportConstants.Acc_Sales_Order.equalsIgnoreCase(jObj.optString("dbtablename", "")) || (CustomReportConstants.inventory.equalsIgnoreCase(jObj.optString("dbtablename", "")) && CustomReportConstants.Acc_Sales_Order.equals(jObj.optString("crossJoinMainTable")))) {
                                                            crossModuleTable = CustomReportConstants.Acc_Sales_Order_Details;
                                                            crossModuleAsAliasKey = accCustomerReportServiceDao.getmoduledataRefPKColName(crossModuleTable);
                                                        } else if (CustomReportConstants.Acc_Vendor_Invoice_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) || CustomReportConstants.Acc_Vendor_Invoice.equalsIgnoreCase(jObj.optString("dbtablename", "")) || (jObj.optInt("linkedmodule") == 6 && jObj.optString("dbtablename", "").equals(CustomReportConstants.inventory)) ) {
                                                            crossModuleTable = CustomReportConstants.Acc_Vendor_Invoice_Details;
                                                            //crossModuleAsAliasKey = accCustomerReportServiceDao.getmoduledataRefPKColName(crossModuleTable);
                                                            if (CustomReportConstants.Acc_Purchase_Order.equalsIgnoreCase(jObj.optString("mainTable", ""))) {
                                                                crossModuleAsAliasKey = CustomReportConstants.Acc_PO_TO_VI_Column_Reference;
                                                            } else if (CustomReportConstants.Acc_Debit_Note.equalsIgnoreCase(jObj.optString("mainTable", ""))) {
                                                                crossModuleAsAliasKey = CustomReportConstants.Acc_Vendor_Invoice;
                                                            }
                                                        } else if (CustomReportConstants.Acc_Goods_Receipt_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) || CustomReportConstants.Acc_Goods_Receipt.equalsIgnoreCase(jObj.optString("dbtablename", ""))) {
                                                            crossModuleTable = CustomReportConstants.Acc_Goods_Receipt_Details;
                                                            if (CustomReportConstants.Acc_Purchase_Order.equalsIgnoreCase(jObj.optString("mainTable", ""))) {
                                                                crossModuleAsAliasKey = CustomReportConstants.Acc_Purchase_Order_Details;
                                                            } else if (CustomReportConstants.Acc_Vendor_Invoice.equalsIgnoreCase(jObj.optString("mainTable", ""))) {
                                                                crossModuleAsAliasKey = "id";
                                                            }
                                                        } else if (CustomReportConstants.Acc_Sales_Invoice_Details.equalsIgnoreCase(jObj.optString("dbtablename", jObj.optString("crossJoinMainTable", ""))) || CustomReportConstants.Acc_Sales_Invoice.equalsIgnoreCase(jObj.optString("dbtablename", jObj.optString("crossJoinMainTable", ""))) || (CustomReportConstants.inventory.equalsIgnoreCase(jObj.optString("dbtablename", "")) && CustomReportConstants.Acc_Sales_Invoice.equals(jObj.optString("crossJoinMainTable")))) {
                                                            crossModuleTable = CustomReportConstants.Acc_Sales_Invoice_Details;
                                                            if (CustomReportConstants.Acc_Sales_Order.equalsIgnoreCase(jObj.optString("mainTable", ""))) {
                                                                crossModuleAsAliasKey = CustomReportConstants.Acc_SI_TO_SO_Column_Reference;
                                                            } else if (CustomReportConstants.Acc_Credit_Note.equalsIgnoreCase(jObj.optString("mainTable", ""))) {
                                                                crossModuleAsAliasKey = CustomReportConstants.Acc_Sales_Invoice;
                                                            }
                                                        } else if (CustomReportConstants.Acc_Delivery_Order_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) || CustomReportConstants.Acc_Delivery_Order.equalsIgnoreCase(jObj.optString("dbtablename", ""))) {
                                                            crossModuleTable = CustomReportConstants.Acc_Delivery_Order_Details;
                                                            if (CustomReportConstants.Acc_Sales_Order.equalsIgnoreCase(jObj.optString("mainTable", ""))) {
                                                                crossModuleAsAliasKey = CustomReportConstants.Acc_Sales_Order_Details;
                                                            } else if (CustomReportConstants.Acc_Sales_Invoice.equalsIgnoreCase(jObj.optString("mainTable", ""))) {
                                                                crossModuleAsAliasKey = "id";
                                                            }
                                                        } else if (CustomReportConstants.Acc_Purchase_Return_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) || CustomReportConstants.Acc_Purchase_Return.equalsIgnoreCase(jObj.optString("dbtablename", ""))) {
                                                            crossModuleTable = CustomReportConstants.Acc_Purchase_Return_Details;
                                                            crossModuleAsAliasKey = CustomReportConstants.Acc_GR_TO_VI_Column_Reference;
                                                        } else if (CustomReportConstants.Acc_Sales_Return_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) || CustomReportConstants.Acc_Sales_Return.equalsIgnoreCase(jObj.optString("dbtablename", ""))) {
                                                            crossModuleTable = CustomReportConstants.Acc_Sales_Return_Details;
                                                            crossModuleAsAliasKey = CustomReportConstants.Acc_CI_TO_SR_Column_Reference;
                                                        }

                                                        if (!StringUtil.isNullOrEmpty(crossModuleTable) && !StringUtil.isNullOrEmpty(crossModuleAsAliasKey)) {
                                                            crossModuleAsAlias = crossModuleTable + crossModuleAsAliasKey + cnt;
                                                            crossModuleAsAliasMap.put(jObj.optString("crossJoinMainTable", ""),crossModuleAsAlias);
                                                            stbCrossModuleSelectClause.append(crossModuleTable + "." + crossModuleAsAliasKey + " as " + crossModuleAsAlias + " ,");
                                                        }
                                                        if (crossModuleTable.equals("podetails")) {
                                                            stbCrossModuleSelectClause.append(crossModuleTable + ".salesorderdetailid , ");
                                                        } else if (crossModuleTable.equals("sodetails")) {
                                                            stbCrossModuleSelectClause.append(crossModuleTable + ".purchaseorderdetailid , ");
                                                        }
                                                        
                                                        if (CustomReportConstants.Acc_Sales_Invoice.equalsIgnoreCase(jObj.optString("mainTable", "")) && (CustomReportConstants.Acc_Delivery_Order_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) || CustomReportConstants.Acc_Delivery_Order.equalsIgnoreCase(jObj.optString("dbtablename", "")) )) {
                                                            stbCrossModuleSelectClause.append(crossModuleTable + ".cidetails , ");
                                                        } else if (CustomReportConstants.Acc_Goods_Receipt_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) && CustomReportConstants.Acc_Vendor_Invoice.equalsIgnoreCase(jObj.optString("mainTable", ""))) {
                                                            stbCrossModuleSelectClause.append(crossModuleTable + ".videtails , ");
                                                        } else if((CustomReportConstants.Acc_Sales_Invoice_Details.equalsIgnoreCase(jObj.optString("dbtablename","")) || CustomReportConstants.inventory.equalsIgnoreCase(jObj.optString("dbtablename",""))) && mainTable.equals(CustomReportConstants.Acc_Sales_Order) ){
                                                            stbCrossModuleSelectClause.append(crossModuleTable + ".salesorderdetail , ");
                                                        }
                                                    }
                                                    //stbCrossModuleSelectClause.append(jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + "," + jObj.optString("dbtablename", "") + "." + accCustomerReportServiceDao.getmoduledataRefPKColName(jObj.optString("dbtablename", ""))  + ",");
                                                    if(jObj.optString("reftabledatacolumn", "").contains("(") && jObj.optString("reftabledatacolumn", "").contains(")") && (jObj.optString("defaultHeader", "").contains("Amount")|| jObj.optString("defaultHeader", "").contains(CustomReportConstants.CLOSED_MANUALLY)|| jObj.optString("defaultHeader", "").contains(CustomReportConstants.UNIT_PRICE_INCLUDING_GST)|| jObj.optString("defaultHeader", "").contains(CustomReportConstants.BaseQuantity)) && jObj.optBoolean("isLineItem", false)){
                                                     stbCrossModuleSelectClause.append(jObj.optString("reftabledatacolumn","")+" as lineAmount"+ cnt + ",");   
                                                    } else if(stbCrossModuleSelectClause.toString().contains("." + jObj.optString("reftabledatacolumn", ""))) {
                                                        stbCrossModuleSelectClause.append(jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + " as "+ jObj.optString("reftabledatacolumn", "") + cnt +",");
                                                    } else {
                                                        stbCrossModuleSelectClause.append(jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") +",");
                                                        fieldDupList.add(jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", ""));
                                                    }
                                                    //stbCrossModuleSelectClause.append(jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + "," + jObj.optString("dbtablename", "") + "." + accCustomerReportServiceDao.getmoduledataRefPKColName(jObj.optString("dbtablename", ""))  + ",");
//                                                    if (!(stbCrossModuleJoinClause.toString().contains(" left join " + jObj.getString("dbtablename") + " on " + jObj.getString("dbtablename") + "." + jObj.optString("crossJoinMainTable", "") + " = " + jObj.optString("crossJoinMainTable", "") + "." + accCustomerReportServiceDao.getmoduledataRefPKColName(jObj.optString("crossJoinMainTable", "")) + " "))) {
//                                                        stbCrossModuleJoinClause.append(" left join " + jObj.getString("dbtablename") + " on " + jObj.getString("dbtablename") + "." + jObj.optString("crossJoinMainTable", "") + " = " + jObj.optString("crossJoinMainTable", "") + "." + accCustomerReportServiceDao.getmoduledataRefPKColName(jObj.optString("crossJoinMainTable", "")) + " ");
//                                                    }
//                                                    if (!(stbCrossModuleJoinClause.toString().contains(" left join " + jObj.getString("reftablename") + " on " + jObj.getString("reftablename") + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " "))) {
//                                                        stbCrossModuleJoinClause.append(" left join " + jObj.getString("reftablename") + " on " + jObj.getString("reftablename") + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " ");
//                                                    }
                                                    if (!jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(jObj.getString("dbtablename")) && (!(jObj.optString("defaultHeader").equals(CustomReportConstants.Acc_Product_ID) || jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_Name)) && !jObj.optBoolean("allowcrossmodule"))) {
                                                        if (!(stbCrossModuleJoinClause.toString().contains(" left join " + jObj.getString("dbtablename") + " on " + jObj.getString("dbtablename") + "." + jObj.optString("crossJoinMainTable", "") + " = " + jObj.optString("crossJoinMainTable", "") + "." + accCustomerReportServiceDao.getmoduledataRefPKColName(jObj.optString("crossJoinMainTable", "")) + " "))) {
                                                            stbCrossModuleJoinClause.append(" left join " + jObj.getString("dbtablename") + " on " + jObj.getString("dbtablename") + "." + jObj.optString("crossJoinMainTable", "") + " = " + jObj.optString("crossJoinMainTable", "") + "." + accCustomerReportServiceDao.getmoduledataRefPKColName(jObj.optString("crossJoinMainTable", "")) + " ");
                                                        }
                                                    }
                                                    if (!(stbCrossModuleJoinClause.toString().contains(" left join " + jObj.getString("reftablename") + " on " + jObj.getString("reftablename") + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " ")) && (!(jObj.optString("defaultHeader").equals(CustomReportConstants.Acc_Product_ID) || jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_Name)) && !jObj.optBoolean("allowcrossmodule"))) {
                                                        if (jObj.optString("defaultHeader").equals("Tax Name")) {
                                                            stbCrossModuleJoinClause.append(" left join " + jObj.getString("reftablename") + " on " + jObj.getString("reftablename") + "." + jObj.getString("reftablefk") + " = " + getDetailsTableForMainTable(jObj.optString("crossJoinMainTable")) + "." + jObj.getString("dbcolumnname") + " ");
                                                        } else {
                                                            if (stbCrossModuleJoinClause.toString().contains(" left join " + jObj.getString("reftablename") + " on ")) {
                                                                stbCrossModuleJoinClause.append(" left join " + jObj.getString("reftablename")+" as "+jObj.getString("reftablename")+cnt + " on " + jObj.getString("reftablename")+ cnt + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " ");
                                                            } else {
                                                                stbCrossModuleJoinClause.append(" left join " + jObj.getString("reftablename") + " on " + jObj.getString("reftablename") + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " ");
                                                            }
                                                        }
                                                        stbCrossModuleJoinMap.put(jObj.optString("crossJoinMainTable", ""), stbCrossModuleJoinClause.toString());
                                                    }
                                                    //stbCrossModuleJoinClause.append(" left join " + jObj.getString("reftablename") + " on " + jObj.getString("reftablename") + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " ");
                                                    isCrossJoinLineItemPresent = true;
                                                }
                                            }
                                            fieldDupList.add(jObj.optString("dbcolumnname", ""));
                                        }
                                    }

                                }
                                JSONArray customFieldsExtraJoin = jObj.optJSONArray("customfieldsextrajoin");
                                if (customFieldsExtraJoin != null && !iscustomFieldsExtraJoinAdded) {
                                    for (int arrayCnt = 0; arrayCnt < customFieldsExtraJoin.length(); arrayCnt++) {
                                        JSONObject objectInArray = customFieldsExtraJoin.getJSONObject(arrayCnt);
                                        Iterator<?> keys = objectInArray.keys();
                                        while (keys.hasNext()) {
                                            String key = (String) keys.next();
                                            String value = objectInArray.getString(key);
                                            if (!stbCrossModuleJoinClause.toString().contains("left join "+key) && !stbCrossModuleJoinClause.toString().contains(value)) {
                                                stbCrossModuleJoinClause.append(value);
                                            }
                                        }
                                    }
                                    iscustomFieldsExtraJoinAdded = true;
                                }
//                                stbCrossModuleJoinMap.put(jObj.optString("crossJoinMainTable", ""), stbCrossModuleJoinClause.toString());
                                if (stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", ""))!=null && !stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")).contains("accjecustomdata")) {
                                    if (!stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")).contains("vendor")) {
                                        String tempStr = "";
                                        if (stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")).length() <= stbCrossModuleJoinClause.length()) {
//                                            tempStr = stbCrossModuleJoinClause.substring(stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")).length(), stbCrossModuleJoinClause.length());
                                            tempStr = mergeLeftJoins(stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")),stbCrossModuleJoinClause);
                                        }
                                        stbCrossModuleJoinClause.setLength(0);
                                        String mergedJoin="";
                                        if(stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")).indexOf("left join")>-1){
                                            mergedJoin=stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")).substring(0, stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")).indexOf("left join"));
                                        }
//                                        stbCrossModuleJoinClause.append(test + tempStr);
                                        if(!StringUtil.isNullOrEmpty(tempStr) && !StringUtil.isNullOrEmpty(mergedJoin)){
                                            stbCrossModuleJoinClause.append(mergedJoin+tempStr);
                                        }
                                        else {
                                            stbCrossModuleJoinClause.append(stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")) + tempStr);
                                        }
//                                        stbCrossModuleJoinClause.append(stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")) + tempStr);
                                        stbCrossModuleJoinMap.put(jObj.optString("crossJoinMainTable", ""), stbCrossModuleJoinClause.toString());
                                    }
                                }

                                stbCrossModuleSelectMap.put(jObj.optString("crossJoinMainTable", ""),stbCrossModuleSelectClause.toString() );
                                if (stbCrossModuleJoinMap.containsKey(jObj.optString("crossJoinMainTable", ""))) {
                                    if (!stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")).contains(jObj.optString("extrainnerjoin", ""))) {
                                        stbCrossModuleJoinMap.put(jObj.optString("crossJoinMainTable", ""), (stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")) + jObj.optString("extrainnerjoin", "")));
                                        stbCrossModuleJoinClause.setLength(0);
                                        stbCrossModuleJoinClause.append(stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")));
                                    }
                                    if(!jObj.optString("dbtablename","").equals(CustomReportConstants.Acc_CUSTOMER)){
                                        if (dataIndexListDup.containsKey(jObj.optString("dbtablename", "") + "." + jObj.optString("dbcolumnname", ""))) {
                                            dupList = dataIndexListDup.get(jObj.optString("dbtablename", "") + "." + jObj.optString("dbcolumnname", ""));
                                            if (jObj.optString("defaultHeader", "").equals(CustomReportConstants.TERM_AMOUNT)) {
                                                if (dupList.contains("#" + jObj.getString("defaultHeader") + "#")) {
                                                    dupList.remove("#" + jObj.getString("defaultHeader") + "#");
                                                    ArrayList<String> newdupList = new ArrayList<String>();
                                                    newdupList.add("#" + jObj.getString("defaultHeader") + "#");
                                                    dataIndexListDup.put(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("reftablename", ""), newdupList);
                                                }

                                            }
                                        } else {
                                            dataIndexList.remove(jObj.optString("dbtablename", "") + "." + jObj.optString("dbcolumnname", ""));
                                            if (!dataIndexList.containsKey(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("dbcolumnname", ""))) {
                                                if (jObj.optString("defaultHeader", "").equals(CustomReportConstants.TERM_AMOUNT)) {
                                                    dataIndexList.put(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("reftablename", ""), "#" + jObj.getString("defaultHeader") + "#");
                                                } else {
                                                    if (!(jObj.optString("dbcolumnname", "").contains("(") && jObj.optString("dbcolumnname", "").contains(")") && jObj.optString("dbcolumnname", "").contains(","))) {
                                                        if (!jObj.optBoolean("isLineItem", false) && !jObj.optBoolean("customfield", false)) {
                                                            dataIndexList.put(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("dbcolumnname", ""), "#" + jObj.getString("defaultHeader") + "#");
                                                        } else {
                                                            if (dataIndexList.containsKey(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("reftabledatacolumn", ""))) {
                                                                dataIndexList.put(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("reftabledatacolumn", "") + cnt, "#" + jObj.getString("defaultHeader") + "#");
                                                            } else {
                                                                if (jObj.optString("reftabledatacolumn", "").contains("(") && jObj.optString("reftabledatacolumn", "").contains(")") && (jObj.optString("defaultHeader", "").contains("Amount") || jObj.optString("defaultHeader", "").contains(CustomReportConstants.CLOSED_MANUALLY) || jObj.optString("defaultHeader", "").contains(CustomReportConstants.UNIT_PRICE_INCLUDING_GST) || jObj.optString("defaultHeader", "").contains(CustomReportConstants.BaseQuantity)) && jObj.optBoolean("isLineItem", false)) {
                                                                    dataIndexList.put(jObj.optString("crossJoinMainTable", "") + "." + "lineAmount" + cnt, "#" + jObj.getString("defaultHeader") + "#");
                                                                } else if (stbCrossModuleSelectClause.toString().contains("." + jObj.optString("reftabledatacolumn", "")) && jObj.optString("reftabledatacolumn", "").equals("discount")) {
                                                                    dataIndexList.put(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("reftabledatacolumn", "") + cnt, "#" + jObj.getString("defaultHeader") + "#");
                                                                } else if (jObj.optString("defaultHeader", "").contains(CustomReportConstants.BaseUOMQuantity)) {
                                                                    dataIndexList.put(jObj.optString("crossJoinMainTable", "") + "." + "baseUOMQty", "#" + jObj.getString("defaultHeader") + "#");
                                                            } else if(!jObj.optString("defaultHeader").equalsIgnoreCase("Tax Name")){
                                                                    dataIndexList.put(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("reftabledatacolumn", ""), "#" + jObj.getString("defaultHeader") + "#");
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                dataIndexList.put(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("dbcolumnname", "") + cnt, "#" + jObj.getString("defaultHeader") + "#");
                                            }
                                            if (isdateflag) {
                                            if(!stbCrossModuleJoinClause.toString().contains(" left join " + jObj.getString("reftablename"))) {
                                                    stbCrossModuleJoinClause.append(" left join " + jObj.getString("reftablename") + " on " + jObj.getString("reftablename") + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " ");
                                                }
                                                if (jObj.optString("reftabledatacolumn", "").contains("COALESCE")) {
                                                    replaceAll(stbCrossModuleSelectClause, jObj.optString("dbtablename", "") + "." + jObj.optString("dbcolumnname", "") + ",", jObj.optString("reftabledatacolumn", "") + " as InvoiceDate ,");
                                                } else {
                                                    replaceAll(stbCrossModuleSelectClause, jObj.optString("dbtablename", "") + "." + jObj.optString("dbcolumnname", "") + ",", jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + ",");
                                                }
                                                stbCrossModuleJoinMap.put(jObj.optString("crossJoinMainTable", ""), stbCrossModuleJoinClause.toString());
                                                stbCrossModuleSelectMap.put(jObj.optString("crossJoinMainTable", ""), stbCrossModuleSelectClause.toString());
                                                if (dataIndexList.containsKey(jObj.optString("crossJoinMainTable", "") + ".journalentry")) {
                                                    dataIndexList.remove(jObj.optString("crossJoinMainTable", "") + ".journalentry");
                                                    if (jObj.optString("reftabledatacolumn", "").contains("COALESCE")) {
                                                        dataIndexList.put(jObj.optString("crossJoinMainTable", "") + ".InvoiceDate", "#" + jObj.getString("defaultHeader") + "#");
                                                    } else {
                                                        dataIndexList.put(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("reftabledatacolumn", ""), "#" + jObj.getString("defaultHeader") + "#");
                                                    }
                                                }
                                            }




                                        }
                                    }

                                } else if (stbCrossModuleJoinMap.containsKey(jObj.optString("crossJoinMainTable", ""))) {
                                    stbCrossModuleJoinMap.put(jObj.optString("crossJoinMainTable", ""), (stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")) + jObj.optString("extrainnerjoin", "")));
                                }
                                if (!jObj.optString("reftablename", "").equals("") && (!stbCrossModuleJoinClause.toString().contains(" left join " + jObj.optString("reftablename") + " on " + jObj.optString("reftablename") + "." + jObj.optString("reftablefk") + " = " + jObj.optString("dbtablename") + "." + jObj.optString("dbcolumnname")) && !stbCrossModuleJoinClause.toString().contains(" left join " + jObj.optString("reftablename") + " on " + jObj.optString("dbtablename") + "." + jObj.optString("dbcolumnname") + " = " + jObj.optString("reftablename") + "." + jObj.optString("reftablefk")))) {
                                    if (!stbCrossModuleJoinClause.toString().contains("join " + jObj.getString("reftablename"))) {
                                        if (jObj.getString("dbtablename").equals(getDetailsTableForMainTable(jObj.optString("crossJoinMainTable"))) && !stbCrossModuleJoinClause.toString().contains(" left join " + getDetailsTableForMainTable(jObj.optString("crossJoinMainTable")))) {

                                            String crossJoinMainTableForeignKeyInDetailTable = accCustomerReportServiceDao.getFKofCrossJoinMainTable(jObj.optString("crossJoinMainTable"), jObj.getString("dbtablename"));
                                            String detailTableMissingJoin = " left join " + jObj.getString("dbtablename") + " on " + jObj.getString("dbtablename") + "." + crossJoinMainTableForeignKeyInDetailTable + " = " + jObj.optString("crossJoinMainTable") + ".id ";
                                            stbCrossModuleJoinClause.append(detailTableMissingJoin);
                                        }
                                        if (!StringUtil.isNullOrEmpty(jObj.optString("reftablefk", "")) && (!jObj.has("defaultmappingJSONObj") && !stbCrossModuleJoinClause.toString().contains("left join "+jObj.getString("reftablename")))) {
                                            stbCrossModuleJoinClause.append(" left join " + jObj.getString("reftablename") + " on " + jObj.getString("reftablename") + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " ");
                                        }
                                        stbCrossModuleJoinMap.put(jObj.optString("crossJoinMainTable", ""), stbCrossModuleJoinClause.toString());
                                    }
                                }
                                JSONArray defaultMapjarrayObj = jObj.optJSONArray("defaultmappingJSONObj");
                                if (defaultMapjarrayObj != null) {
                                    for (int mapCount = 0; mapCount < defaultMapjarrayObj.length(); mapCount++) {
                                        JSONObject mapjObj = defaultMapjarrayObj.getJSONObject(mapCount);
                                        if (stbCrossModuleJoinMap.containsKey(jObj.optString("crossJoinMainTable", ""))) {
                                            String defaultMappingJoin;
                                            if(!stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "").trim()).contains(mapjObj.getString("defaultextrainnerjoin").trim())){
                                                if ((stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")).contains("join "+mapjObj.optString("reftablename", "")+" "))) {
                                                    defaultMappingJoin = mapjObj.optString("defaultextrainnerjoin", "").replaceAll(mapjObj.optString("reftablename", ""), mapjObj.optString("reftablename", "") + mapCount);
                                                    defaultMappingJoin = defaultMappingJoin.replaceFirst("left join " + mapjObj.optString("reftablename", "") + mapCount, "left join " + mapjObj.optString("reftablename", "") + " as " + mapjObj.optString("reftablename", "") + mapCount);
                                                    stbCrossModuleJoinMap.put(jObj.optString("crossJoinMainTable", ""), (stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")) +defaultMappingJoin + " "));
                                                } else {
                                                    if (!stbCrossModuleJoinClause.toString().contains("join " + jObj.optString("reftablename"))) {
                                                        if (jObj.getString("dbtablename").equals(getDetailsTableForMainTable(jObj.optString("crossJoinMainTable"))) && !stbCrossModuleJoinClause.toString().contains(" left join " + getDetailsTableForMainTable(jObj.optString("crossJoinMainTable")))) {

                                                            String crossJoinMainTableForeignKeyInDetailTable = accCustomerReportServiceDao.getFKofCrossJoinMainTable(jObj.optString("crossJoinMainTable"),jObj.getString("dbtablename"));
                                                            String detailTableMissingJoin = " left join "+jObj.getString("dbtablename")+" on " +jObj.getString("dbtablename")+"."+crossJoinMainTableForeignKeyInDetailTable+" = "+jObj.optString("crossJoinMainTable")+".id ";
                                                            stbCrossModuleJoinClause.append(detailTableMissingJoin);
                                                        }else if(mapjObj.getString("dbtabletame").equals(getDetailsTableForMainTable(jObj.optString("crossJoinMainTable"))) && !stbCrossModuleJoinClause.toString().contains(" left join " + getDetailsTableForMainTable(jObj.optString("crossJoinMainTable")))){
                                                            String crossJoinMainTableForeignKeyInDetailTable = accCustomerReportServiceDao.getFKofCrossJoinMainTable(jObj.optString("crossJoinMainTable"),mapjObj.getString("dbtabletame"));
                                                            String detailTableMissingJoin = " left join "+mapjObj.getString("dbtabletame")+" on " +mapjObj.getString("dbtabletame")+"."+crossJoinMainTableForeignKeyInDetailTable+" = "+jObj.optString("crossJoinMainTable")+".id ";
                                                            stbCrossModuleJoinClause.append(detailTableMissingJoin);
                                                        }
                                                      
                                                    }
                                                    if (!stbCrossModuleJoinClause.toString().contains(mapjObj.optString("defaultextrainnerjoin"))) {
                                                        stbCrossModuleJoinClause.append(" " + mapjObj.optString("defaultextrainnerjoin"));
                                                    }
                                                    stbCrossModuleJoinMap.put(jObj.optString("crossJoinMainTable", ""), (stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")) + mapjObj.getString("defaultextrainnerjoin") + " "));
                                                }
                                            }
                                            if ((!(jObj.optString("defaultHeader").equals(CustomReportConstants.Acc_Product_ID) || jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_Name)) && !jObj.optBoolean("allowcrossmodule"))) {
                                                if (stbCrossModuleSelectClause.toString().contains("." + mapjObj.optString("reftabledatacolumn"))) {
                                                replaceAll(stbCrossModuleSelectClause, jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + ",", mapjObj.optString("reftablename", "") + "." + mapjObj.optString("reftabledatacolumn", "") + " as " + mapjObj.optString("reftabledatacolumn", "") + cnt +",");
                                                } else {
                                                    replaceAll(stbCrossModuleSelectClause, jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + ",", mapjObj.optString("reftablename", "") + "." + mapjObj.optString("reftabledatacolumn", "") + ",");
                                                }
//                                            replaceAll(stbCrossModuleSelectClause, jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + ",", mapjObj.optString("reftablename", "") + "." + mapjObj.optString("reftabledatacolumn", "") + ",");
                                                stbCrossModuleSelectMap.put(jObj.optString("crossJoinMainTable", ""), stbCrossModuleSelectClause.toString());
                                            }
                                        }
                                    }
                                    if(!(stbCrossModuleJoinClause.toString().contains(" left join " + jObj.getString("reftablename") + " on " + jObj.getString("reftablename") + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " "))){
                                    stbCrossModuleJoinClause.append(" left join " + jObj.getString("reftablename") + " on " + jObj.getString("reftablename") + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " ");
                                    }
                                    stbCrossModuleJoinMap.put(jObj.optString("crossJoinMainTable", ""), stbCrossModuleJoinClause.toString());
                                }

                            } else {
                                stbCrossModuleSelectClause.setLength(0);
                                stbCrossModuleJoinClause.setLength(0);
                                if ((jObj.optInt("xtype") == 4 && !isLineItem) || customfield) {
                                    // for implementing filter on combobox of sales person as the filter query searched for "tablename.salesperson"
                                    if(jObj.getString("dbcolumnname").equals("salesperson")){
                                        stbCrossModuleSelectClause.append(jObj.optString("reftablename", "") + "." + jObj.optString("reftablefk", "") + " as salesperson ,");
                                    }
                                    stbCrossModuleSelectClause.append(jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + ",");
                                    fieldDupList.add(jObj.optString("reftabledatacolumn", ""));
                                } else {
                                    if (!jObj.optBoolean("isMeasureItem", false) && jObj.optString("defaultHeader").contains("Discount") && jObj.optString("defaultHeader").length()<10 && jObj.optBoolean("isLineItem", false)) {
                                        isCrossDiscount = true;
                                        table = jObj.optString("crossJoinMainTable", "");
                                        if (jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.Acc_Sales_Order)) {
                                            stbCrossModuleSelectClause.append("((IF(sodetails.discountispercent ='1',(sodetails.discount/100)*(sodetails.baseuomquantity*sodetails.rate),sodetails.discount)))  as  '#flatdiscount#' ,");
                                        } else if (jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.Acc_Purchase_Order)) {
                                            stbCrossModuleSelectClause.append("((IF(podetails.discountispercent ='1',(podetails.discount/100)*(podetails.baseuomquantity*podetails.rate),podetails.discount)))  as  '#flatdiscount#'  ,");
                                        } else if (jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.Acc_Sales_Invoice)) {
                                            stbCrossModuleSelectClause.append("((IF(discount.inpercent='T',(discount.discount/100)*(origamount),discount.discount)))  as  '#flatdiscount#'  ,");
                                        } else if (jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.Acc_Vendor_Invoice)) {
                                            stbCrossModuleSelectClause.append("((IF(discount.inpercent='T',(discount.discount/100)*(origamount),discount.discount)))   as  '#flatdiscount#'  ,");
                                        } else if (jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.Acc_Delivery_Order)) {
                                            stbCrossModuleSelectClause.append("((IF(dodetails.discountispercent='T',(dodetails.discount/100)*(rate),dodetails.discount)))  as  '#flatdiscount#'  ,");
                                        } else if (jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.Acc_Quotation)) {
                                            stbCrossModuleSelectClause.append("((IF(quotationdetails.discountispercent ='1',(quotationdetails.discount/100)*(quotationdetails.baseuomquantity*quotationdetails.rate),quotationdetails.discount))) as  '#flatdiscount#' ,");
//                                        dataIndexList.put("((IF(quotationdetails.discountispercent ='1',(quotationdetails.discount/100)*(quotationdetails.baseuomquantity*quotationdetails.rate),quotationdetails.discount)) )", "#flatdiscount#");
                                        } else if (jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.Acc_Goods_Receipt)) {
                                            stbCrossModuleSelectClause.append("((IF(grodetails.discountispercent ='1',(grodetails.discount/100)*(grodetails.baseuomquantity*grodetails.rate),grodetails.discount)))  as  '#flatdiscount#' ,");
                                        } else if (jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.Acc_Purchase_Return)) {
                                            stbCrossModuleSelectClause.append("((IF(prdetails.discountispercent ='1',(prdetails.discount/100)*(prdetails.baseuomquantity*prdetails.rate),prdetails.discount)))  as  '#flatdiscount#' ,");
                                        } else if (jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.Acc_Sales_Return)) {
                                            stbCrossModuleSelectClause.append("((IF(srdetails.discountispercent ='1',(srdetails.discount/100)*(srdetails.baseuomquantity*srdetails.rate),srdetails.discount)))  as  '#flatdiscount#' ,");
                                        }
                                        if (isCrossDiscount) {
                                           if (jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.Acc_Sales_Invoice)||jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.Acc_Vendor_Invoice)) {
                                                stbCrossModuleSelectClause.append( "discount.inpercent ,");
                                            } else {
                                                stbCrossModuleSelectClause.append(jObj.optString("reftablename") +".discountispercent ,");
                                            }
                                            lineItemDiscountSelectColumn = ", " + jObj.optString("crossJoinMainTable") + ".`#flatdiscount#`";

                                        }
                                    }
                                    if (jObj.optString("defaultHeader", "").equals(CustomReportConstants.TERM_AMOUNT)) { // Added for Term Amount Field to constuct the SQL query correctly
                                        stbCrossModuleSelectClause.append(jObj.optString("reftabledatacolumn", "") + " as " + jObj.optString("dbcolumnname", "") + ",");
                                        if (stbCrossModuleJoinMap.containsKey(jObj.optString("crossJoinMainTable", ""))) {
                                            stbCrossModuleJoinMap.put(jObj.optString("crossJoinMainTable", ""), (stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")) + " left join " + jObj.getString("reftablename") + " on " + jObj.getString("reftablename") + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " "));
                                        } else {
                                            stbCrossModuleJoinMap.put(jObj.optString("crossJoinMainTable", ""), " left join " + jObj.getString("reftablename") + " on " + jObj.getString("reftablename") + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " ");
                                        }
                                        fieldDupList.add(jObj.optString("reftabledatacolumn", ""));
                                    } else {
                                        if ((jObj.optString("dbcolumnname", "").contains("(") && jObj.optString("dbcolumnname", "").contains(")") && jObj.optString("dbcolumnname", "").contains(","))) {
                                            stbCrossModuleSelectClause.append(jObj.optString("dbcolumnname", "") + " as InvoiceAmount" + cnt + ",");
                                        } else if(reftablename.equals("billingshippingaddresses")){
                                            stbCrossModuleSelectClause.append("billingshippingaddresses" + "." + jObj.optString("reftabledatacolumn", "") + ",");
                                        } else {
                                            if (!jObj.optBoolean("isLineItem", false)) {
                                                stbCrossModuleSelectClause.append(jObj.optString("dbtablename", "") + "." + jObj.optString("dbcolumnname", "") + ",");
                                            } else {
                                                if (!isCrossJoinLineItemPresent) {
                                                    // added to have corresponding details table and alias in select and sub query
                                                    if (CustomReportConstants.Acc_Purchase_Order_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) || CustomReportConstants.Acc_Purchase_Order.equalsIgnoreCase(jObj.optString("dbtablename", "")) || (jObj.optInt("linkedmodule") == 18 && (jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_ID) || jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_Name) || jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_Type)))) {
                                                        crossModuleTable = CustomReportConstants.Acc_Purchase_Order_Details;
                                                        crossModuleAsAliasKey = accCustomerReportServiceDao.getmoduledataRefPKColName(crossModuleTable);
                                                    } else if (CustomReportConstants.Acc_Quotation_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) || CustomReportConstants.Acc_Quotation.equalsIgnoreCase(jObj.optString("dbtablename", ""))) {
                                                        crossModuleTable = CustomReportConstants.Acc_Quotation_Details;
                                                        crossModuleAsAliasKey = accCustomerReportServiceDao.getmoduledataRefPKColName(crossModuleTable);
                                                    } else if (CustomReportConstants.Acc_Sales_Order_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) || CustomReportConstants.Acc_Sales_Order.equalsIgnoreCase(jObj.optString("dbtablename", ""))) {
                                                        crossModuleTable = CustomReportConstants.Acc_Sales_Order_Details;
                                                        crossModuleAsAliasKey = accCustomerReportServiceDao.getmoduledataRefPKColName(crossModuleTable);
                                                    } else if (CustomReportConstants.Acc_Vendor_Invoice_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) || CustomReportConstants.Acc_Vendor_Invoice.equalsIgnoreCase(jObj.optString("dbtablename", "")) || (jObj.optInt("linkedmodule") == 6 && (jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_ID) || jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_Name) || jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_Type)))) {
                                                        crossModuleTable = CustomReportConstants.Acc_Vendor_Invoice_Details;
                                                        //crossModuleAsAliasKey = accCustomerReportServiceDao.getmoduledataRefPKColName(crossModuleTable);
                                                        if (CustomReportConstants.Acc_Purchase_Order.equalsIgnoreCase(jObj.optString("mainTable", ""))) {
                                                            crossModuleAsAliasKey = CustomReportConstants.Acc_PO_TO_VI_Column_Reference;
                                                        } else if (CustomReportConstants.Acc_Debit_Note.equalsIgnoreCase(jObj.optString("mainTable", ""))) {
                                                            crossModuleAsAliasKey = CustomReportConstants.Acc_Vendor_Invoice;
                                                        }
                                                    } else if (CustomReportConstants.Acc_Goods_Receipt_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) || CustomReportConstants.Acc_Goods_Receipt.equalsIgnoreCase(jObj.optString("dbtablename", ""))) {
                                                        crossModuleTable = CustomReportConstants.Acc_Goods_Receipt_Details;
                                                        if (CustomReportConstants.Acc_Purchase_Order.equalsIgnoreCase(jObj.optString("mainTable", ""))) {
                                                            crossModuleAsAliasKey = CustomReportConstants.Acc_Purchase_Order_Details;
                                                        } else if (CustomReportConstants.Acc_Vendor_Invoice.equalsIgnoreCase(jObj.optString("mainTable", ""))) {
                                                            crossModuleAsAliasKey = "id";
                                                        }
                                                    } else if (CustomReportConstants.Acc_Sales_Invoice_Details.equalsIgnoreCase(jObj.optString("dbtablename", jObj.optString("crossJoinMainTable", ""))) || CustomReportConstants.Acc_Sales_Invoice.equalsIgnoreCase(jObj.optString("dbtablename", jObj.optString("crossJoinMainTable", ""))) || (jObj.optInt("linkedmodule") == 2 && (jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_ID) || jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_Name) || jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_Type)))) {
                                                        crossModuleTable = CustomReportConstants.Acc_Sales_Invoice_Details;
                                                        if (CustomReportConstants.Acc_Sales_Order.equalsIgnoreCase(jObj.optString("mainTable", ""))) {
                                                            crossModuleAsAliasKey = CustomReportConstants.Acc_SI_TO_SO_Column_Reference;
                                                        } else if (CustomReportConstants.Acc_Credit_Note.equalsIgnoreCase(jObj.optString("mainTable", ""))) {
                                                            crossModuleAsAliasKey = CustomReportConstants.Acc_Sales_Invoice;
                                                        }
                                                    } else if (CustomReportConstants.Acc_Delivery_Order_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) || CustomReportConstants.Acc_Delivery_Order.equalsIgnoreCase(jObj.optString("dbtablename", ""))) {
                                                        crossModuleTable = CustomReportConstants.Acc_Delivery_Order_Details;
                                                        if (CustomReportConstants.Acc_Sales_Order.equalsIgnoreCase(jObj.optString("mainTable", ""))) {
                                                            crossModuleAsAliasKey = CustomReportConstants.Acc_Sales_Order_Details;
                                                        } else if (CustomReportConstants.Acc_Sales_Invoice.equalsIgnoreCase(jObj.optString("mainTable", ""))) {
                                                            crossModuleAsAliasKey = "id";
                                                        }
                                                    } else if (CustomReportConstants.Acc_Purchase_Return_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) || CustomReportConstants.Acc_Purchase_Return.equalsIgnoreCase(jObj.optString("dbtablename", ""))) {
                                                        crossModuleTable = CustomReportConstants.Acc_Purchase_Return_Details;
                                                        crossModuleAsAliasKey = CustomReportConstants.Acc_GR_TO_VI_Column_Reference;
                                                    } else if (CustomReportConstants.Acc_Sales_Return_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) || CustomReportConstants.Acc_Sales_Return.equalsIgnoreCase(jObj.optString("dbtablename", ""))) {
                                                        crossModuleTable = CustomReportConstants.Acc_Sales_Return_Details;
                                                        crossModuleAsAliasKey = CustomReportConstants.Acc_CI_TO_SR_Column_Reference;
                                                    }
                                                    crossModuleAsAlias = crossModuleTable + crossModuleAsAliasKey + cnt;
                                                    stbCrossModuleSelectClause.append(crossModuleTable + "." + crossModuleAsAliasKey + " as " + crossModuleAsAlias + " ,");
                                                    isCrossJoinLineItemPresent = true;
                                                    if (crossModuleTable.equals("podetails")) {
                                                        stbCrossModuleSelectClause.append(crossModuleTable + ".salesorderdetailid , ");
                                                    } else if (crossModuleTable.equals("sodetails")) {
                                                        stbCrossModuleSelectClause.append(crossModuleTable + ".purchaseorderdetailid , ");
                                                    } else if (crossModuleTable.equals("invoicedetails")) {
                                                        stbCrossModuleSelectClause.append(crossModuleTable + ".salesorderdetail , ");
                                                    }
                                                    crossModuleAsAliasMap.put(jObj.optString("crossJoinMainTable", ""),crossModuleAsAlias);
                                                    if (CustomReportConstants.Acc_Sales_Invoice.equalsIgnoreCase(jObj.optString("mainTable", "")) && (CustomReportConstants.Acc_Delivery_Order_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) || CustomReportConstants.Acc_Delivery_Order.equalsIgnoreCase(jObj.optString("dbtablename", "")) )) {
                                                            stbCrossModuleSelectClause.append(crossModuleTable + ".cidetails , ");
                                                        } else if (CustomReportConstants.Acc_Goods_Receipt_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) && CustomReportConstants.Acc_Vendor_Invoice.equalsIgnoreCase(jObj.optString("mainTable", ""))) {
                                                            stbCrossModuleSelectClause.append(crossModuleTable + ".videtails , ");
                                                        } 

                                                }
                                                //stbCrossModuleSelectClause.append(jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + "," + jObj.optString("dbtablename", "") + "." + accCustomerReportServiceDao.getmoduledataRefPKColName(jObj.optString("dbtablename", ""))  + ",");
                                                if (jObj.optString("reftabledatacolumn", "").contains("(") && jObj.optString("reftabledatacolumn", "").contains(")") && (jObj.optString("defaultHeader", "").contains("Amount") || jObj.optString("defaultHeader", "").contains(CustomReportConstants.CLOSED_MANUALLY) || jObj.optString("defaultHeader", "").contains(CustomReportConstants.UNIT_PRICE_INCLUDING_GST) || jObj.optString("defaultHeader", "").contains(CustomReportConstants.BaseQuantity)) && jObj.optBoolean("isLineItem", false)) {
                                                    stbCrossModuleSelectClause.append(jObj.optString("reftabledatacolumn", "") + " as lineAmount" + cnt + ",");
                                                } else if (stbCrossModuleSelectClause.toString().contains("." + jObj.optString("reftabledatacolumn", "")) && jObj.optString("reftabledatacolumn", "").equals("discount")) {
                                                    stbCrossModuleSelectClause.append(jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + " as " + jObj.optString("reftabledatacolumn", "") + cnt + ",");
                                                } else if (jObj.optString("defaultHeader", "").contains(CustomReportConstants.BaseUOMQuantity)) {
                                                    stbCrossModuleSelectClause.append(jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + " as baseUOMQty" + ",");
                                                } else {
                                                    stbCrossModuleSelectClause.append(jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + ",");
                                                }
                                            }
                                        }
                                        fieldDupList.add(jObj.optString("dbcolumnname", ""));
                                    }

                                }
                                addedColType.add(jObj.optString("columntype", "").substring(jObj.optString("columntype", "").indexOf("-"), jObj.optString("columntype", "").length()));
                                if (StringUtil.isNullOrEmpty(jObj.optString("extrainnerjoin", ""))) {
                                    if (!jObj.optBoolean("isLineItem", false) && !jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.product) && !jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.PRODUCTCATEGORY)) {
                                            stbCrossModuleJoinClause.append(getLinkedTableNameForModuleID(moduleid) + ".linkeddocid " + "," + getLinkedTableNameForModuleID(moduleid) + ".docid"
                                                    + " from " + jObj.optString("dbtablename", "") + " inner join " + getLinkedTableNameForModuleID(moduleid) + " on " + jObj.optString("dbtablename", "") + "." + accCustomerReportServiceDao.getmoduledataRefPKColName(jObj.optString("dbtablename", "")) + " = " + getLinkedTableNameForModuleID(moduleid) + ".linkeddocid"
                                                    + "  ");
                                    } else {
                                        if(!jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.product) && !jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.PRODUCTCATEGORY)) {
                                        stbCrossModuleJoinClause.append(getLinkedTableNameForModuleID(moduleid) + ".linkeddocid " + "," + getLinkedTableNameForModuleID(moduleid) + ".docid"
                                                + " from " + jObj.optString("crossJoinMainTable", "") + " inner join " + getLinkedTableNameForModuleID(moduleid) + " on " + jObj.optString("crossJoinMainTable", "") + "." + accCustomerReportServiceDao.getmoduledataRefPKColName(jObj.optString("crossJoinMainTable", "")) + " = " + getLinkedTableNameForModuleID(moduleid) + ".linkeddocid"
                                                + "  ");
                                        }

                                        if (!jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(jObj.optString("dbtablename",jObj.optString("crossJoinMainTable", ""))) && !jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.PRODUCTCATEGORY) && !(jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_ID) || jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_Name) || jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_Type)) && !(jObj.optString("dbcolumnname").contains(",") && jObj.optString("dbcolumnname").contains("(") && jObj.optString("dbcolumnname").contains(")") && !jObj.has("reftablename")) ) {
                                            stbCrossModuleJoinClause.append(" left join " + jObj.optString("dbtablename",jObj.optString("crossJoinMainTable", "")) + " on " + jObj.optString("dbtablename",jObj.optString("crossJoinMainTable", "")) + "." + jObj.optString("crossJoinMainTable", "") + " = " + jObj.optString("crossJoinMainTable", "") + "." + accCustomerReportServiceDao.getmoduledataRefPKColName(jObj.optString("crossJoinMainTable", "")) + " ");
                                        }
                                        //stbCrossModuleJoinClause.append(" left join " + jObj.getString("dbtablename") + " on " + jObj.getString("dbtablename") + "." + jObj.optString("crossJoinMainTable", "") + " = " + jObj.optString("crossJoinMainTable", "") + "." + accCustomerReportServiceDao.getmoduledataRefPKColName(jObj.optString("crossJoinMainTable", "")) + " ");
                                        if (!(jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_ID) || jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_Name) || jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_Type)) && !(jObj.optString("dbcolumnname").contains(",") && jObj.optString("dbcolumnname").contains("(") && jObj.optString("dbcolumnname").contains(")") && !jObj.has("reftablename"))) {
                                            stbCrossModuleJoinClause.append(" left join " + jObj.getString("reftablename") + " on " + jObj.getString("reftablename") + "." + jObj.getString("reftablefk") + " = " + jObj.optString("dbtablename", jObj.optString("crossJoinMainTable", "")) + "." + jObj.getString("dbcolumnname") + " ");
                                        }
                                        if(jObj.optString("crossJoinMainTable", "").equals(CustomReportConstants.PRODUCTCATEGORY))    {
                                           isCrossJoinLineItemPresent = false;
                                        }

                                        if (!isCrossJoinLineItemPresent) {
                                            if(!jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.product) && !jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.PRODUCTCATEGORY))  {
                                            // added to have corresponding details table and alias in select and sub query
                                            if (CustomReportConstants.Acc_Purchase_Order_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) || CustomReportConstants.Acc_Purchase_Order.equalsIgnoreCase(jObj.optString("dbtablename", ""))) {
                                                crossModuleTable = CustomReportConstants.Acc_Purchase_Order_Details;
                                                crossModuleAsAliasKey = accCustomerReportServiceDao.getmoduledataRefPKColName(crossModuleTable);
                                            } else if (CustomReportConstants.Acc_Quotation_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) || CustomReportConstants.Acc_Quotation.equalsIgnoreCase(jObj.optString("dbtablename", ""))) {
                                                crossModuleTable = CustomReportConstants.Acc_Quotation_Details;
                                                crossModuleAsAliasKey = accCustomerReportServiceDao.getmoduledataRefPKColName(crossModuleTable);
                                            } else if (CustomReportConstants.Acc_Sales_Order_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) || CustomReportConstants.Acc_Sales_Order.equalsIgnoreCase(jObj.optString("dbtablename", ""))) {
                                                crossModuleTable = CustomReportConstants.Acc_Sales_Order_Details;
                                                crossModuleAsAliasKey = accCustomerReportServiceDao.getmoduledataRefPKColName(crossModuleTable);
                                            } else if (CustomReportConstants.Acc_Vendor_Invoice_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) || CustomReportConstants.Acc_Vendor_Invoice.equalsIgnoreCase(jObj.optString("dbtablename", ""))) {
                                                crossModuleTable = CustomReportConstants.Acc_Vendor_Invoice_Details;
                                                //crossModuleAsAliasKey = accCustomerReportServiceDao.getmoduledataRefPKColName(crossModuleTable);
                                                if (CustomReportConstants.Acc_Purchase_Order.equalsIgnoreCase(jObj.optString("mainTable", ""))) {
                                                    crossModuleAsAliasKey = CustomReportConstants.Acc_PO_TO_VI_Column_Reference;
                                                } else if (CustomReportConstants.Acc_Debit_Note.equalsIgnoreCase(jObj.optString("mainTable", ""))) {
                                                    crossModuleAsAliasKey = CustomReportConstants.Acc_Vendor_Invoice;
                                                }
                                            } else if (CustomReportConstants.Acc_Goods_Receipt_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) || CustomReportConstants.Acc_Goods_Receipt.equalsIgnoreCase(jObj.optString("dbtablename", ""))) {
                                                crossModuleTable = CustomReportConstants.Acc_Goods_Receipt_Details;
                                                if (CustomReportConstants.Acc_Purchase_Order.equalsIgnoreCase(jObj.optString("mainTable", ""))) {
                                                    crossModuleAsAliasKey = CustomReportConstants.Acc_Purchase_Order_Details;
                                                } else if (CustomReportConstants.Acc_Vendor_Invoice.equalsIgnoreCase(jObj.optString("mainTable", ""))) {
                                                    crossModuleAsAliasKey = "id";
                                                }
//                                            } else if (CustomReportConstants.Acc_Sales_Invoice_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) || CustomReportConstants.Acc_Sales_Invoice.equalsIgnoreCase(jObj.optString("dbtablename", ""))) {
                                            } else if (CustomReportConstants.Acc_Sales_Invoice_Details.equalsIgnoreCase(jObj.optString("dbtablename", jObj.optString("crossJoinMainTable", ""))) || CustomReportConstants.Acc_Sales_Invoice.equalsIgnoreCase(jObj.optString("dbtablename", jObj.optString("crossJoinMainTable", ""))) || (jObj.optInt("linkedmodule")==2)) {
                                                crossModuleTable = CustomReportConstants.Acc_Sales_Invoice_Details;
                                                if (CustomReportConstants.Acc_Sales_Order.equalsIgnoreCase(jObj.optString("mainTable", ""))) {
                                                    crossModuleAsAliasKey = CustomReportConstants.Acc_SI_TO_SO_Column_Reference;
                                                } else if (CustomReportConstants.Acc_Credit_Note.equalsIgnoreCase(jObj.optString("mainTable", ""))) {
                                                    crossModuleAsAliasKey = CustomReportConstants.Acc_Sales_Invoice;
                                                }
                                            } else if (CustomReportConstants.Acc_Delivery_Order_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) || CustomReportConstants.Acc_Delivery_Order.equalsIgnoreCase(jObj.optString("dbtablename", ""))) {
                                                crossModuleTable = CustomReportConstants.Acc_Delivery_Order_Details;
                                                if (CustomReportConstants.Acc_Sales_Order.equalsIgnoreCase(jObj.optString("mainTable", ""))) {
                                                    crossModuleAsAliasKey = CustomReportConstants.Acc_Sales_Order_Details;
                                                } else if (CustomReportConstants.Acc_Sales_Invoice.equalsIgnoreCase(jObj.optString("mainTable", ""))) {
                                                    crossModuleAsAliasKey = "id";
                                                }
                                            } else if (CustomReportConstants.Acc_Purchase_Return_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) || CustomReportConstants.Acc_Purchase_Return.equalsIgnoreCase(jObj.optString("dbtablename", ""))) {
                                                crossModuleTable = CustomReportConstants.Acc_Purchase_Return_Details;
                                                crossModuleAsAliasKey = CustomReportConstants.Acc_GR_TO_VI_Column_Reference;
                                            } else if (CustomReportConstants.Acc_Sales_Return_Details.equalsIgnoreCase(jObj.optString("dbtablename", "")) || CustomReportConstants.Acc_Sales_Return.equalsIgnoreCase(jObj.optString("dbtablename", ""))) {
                                                crossModuleTable = CustomReportConstants.Acc_Sales_Return_Details;
                                                crossModuleAsAliasKey = CustomReportConstants.Acc_CI_TO_SR_Column_Reference;
                                            }
                                            crossModuleAsAlias = crossModuleTable + crossModuleAsAliasKey + cnt;
                                            stbCrossModuleSelectClause.append(crossModuleTable + "." + crossModuleAsAliasKey + " as " + crossModuleAsAlias + " ,");
                                            } else {
                                                crossModuleTable = jObj.optString("crossJoinMainTable", "");
                                                if(crossModuleTable.equals(CustomReportConstants.PRODUCTCATEGORY))
                                                {
                                                    crossModuleTable=CustomReportConstants.PRODUCTCATEGORYMAPPING;
                                                    crossModuleAsAliasKey=Constants.productid;
                                                    crossModuleAsAlias=jObj.optString("crossJoinMainTable", "")+cnt;
                                                    stbCrossModuleSelectClause.append(crossModuleTable+"."+jObj.optString("crossJoinMainTable", "")+" as "+jObj.optString("crossJoinMainTable", "")+ " ,");
                                                    
                                                }
                                                else{
                                                crossModuleAsAliasKey= "id";
                                                crossModuleAsAlias = crossModuleTable + crossModuleAsAliasKey + cnt;
                                                
                                                }
                                                
                                                stbCrossModuleSelectClause.append(crossModuleTable + "." + crossModuleAsAliasKey + " as " + crossModuleAsAlias + " ,");
                                        }
                                                crossModuleAsAliasMap.put(jObj.optString("crossJoinMainTable", ""),crossModuleAsAlias);
                                        }
                                        if(!crossModuleTable.equals(CustomReportConstants.PRODUCTCATEGORYMAPPING))    {
                                        isCrossJoinLineItemPresent = true;
                                        }
                                        if (jObj.has("defaultmappingJSONObj") ) {
                                            String stbCrossModuleJoinClauseString=stbCrossModuleJoinClause.toString();
                                            JSONArray defaultMapjarrayObj = jObj.optJSONArray("defaultmappingJSONObj");
                                            if (defaultMapjarrayObj != null) {
                                                for (int mapCount = 0; mapCount < defaultMapjarrayObj.length(); mapCount++) {
                                                    JSONObject mapjObj = defaultMapjarrayObj.getJSONObject(mapCount);
                                                    String defaultMappingJoin;
                                                    if (!stbCrossModuleJoinClauseString.contains(mapjObj.getString("defaultextrainnerjoin").trim())) {
                                                        if (mapjObj.optString("dbtabletame").equals(getDetailsTableForMainTable(jObj.optString("crossJoinMainTable"))) && !stbCrossModuleJoinClause.toString().contains(" left join " + mapjObj.optString("dbtablename"))) {
                                                            String crossJoinMainTableForeignKeyInDetailTable = accCustomerReportServiceDao.getFKofCrossJoinMainTable(jObj.optString("crossJoinMainTable"), mapjObj.getString("dbtabletame"));
                                                            String detailTableMissingJoin = " left join " + mapjObj.getString("dbtabletame") + " on " + mapjObj.getString("dbtabletame") + "." + crossJoinMainTableForeignKeyInDetailTable + " = " + jObj.optString("crossJoinMainTable") + ".id ";
                                                            stbCrossModuleJoinClause.append(detailTableMissingJoin);
                                                        }
                                                        if (!stbCrossModuleJoinClause.toString().contains((mapjObj.getString("defaultextrainnerjoin")))) {
                                                            stbCrossModuleJoinClause.append(mapjObj.getString("defaultextrainnerjoin") + " ");
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        if (jObj.optBoolean("isLineItem") && jObj.optBoolean("allowcrossmodule") && (!stbCrossModuleJoinClause.toString().contains(" left join " + jObj.optString("reftablename")+ " on " + jObj.optString("reftablename") + "." + jObj.optString("reftablefk") + " = " + jObj.optString("dbtablename") + "." + jObj.optString("dbcolumnname"))) && !(jObj.optString("dbcolumnname").contains(",") && jObj.optString("dbcolumnname").contains("(") && jObj.optString("dbcolumnname").contains(")") && !jObj.has("reftablename"))) {
                                            if (stbCrossModuleJoinClause.toString().contains(" left join " + jObj.optString("reftablename"))) {
                                                stbCrossModuleJoinClause.append(" left join " + jObj.optString("reftablename") + " as " + jObj.optString("reftablename") + cnt + " on " + jObj.optString("reftablename") + cnt + "." + jObj.optString("reftablefk") + " = " + jObj.optString("dbtablename") + "." + jObj.optString("dbcolumnname") + " ");
                                            } else {
                                                if (jObj.optString("dbtablename").equals(getDetailsTableForMainTable(jObj.optString("crossJoinMainTable"))) && !stbCrossModuleJoinClause.toString().contains(" left join " + getDetailsTableForMainTable(jObj.optString("crossJoinMainTable")))) {
                                                    String crossJoinMainTableForeignKeyInDetailTable = accCustomerReportServiceDao.getFKofCrossJoinMainTable(jObj.optString("crossJoinMainTable"), jObj.getString("dbtablename"));
                                                    String detailTableMissingJoin = " left join " + jObj.getString("dbtablename") + " on " + jObj.getString("dbtablename") + "." + crossJoinMainTableForeignKeyInDetailTable + " = " + jObj.optString("crossJoinMainTable") + ".id ";
                                                    stbCrossModuleJoinClause.append(detailTableMissingJoin);
                                                }
                                                stbCrossModuleJoinClause.append(" left join " + jObj.optString("reftablename") + " on " + jObj.optString("reftablename") + "." + jObj.optString("reftablefk") + " = " + jObj.optString("dbtablename") + "." + jObj.optString("dbcolumnname") + " ");
                                            }
                                        }
                                        dataIndexList.remove(jObj.optString("dbtablename", "") + "." + jObj.optString("dbcolumnname", ""));
                                        
                                        if (jObj.optBoolean("isLineItem") && jObj.optBoolean("allowcrossmodule") || (jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_ID) || jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_Name) || jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_Type) || jObj.optString("defaultHeader").startsWith("Product Description"))) {
                                            if (jObj.optString("reftabledatacolumn", "").contains("(") && jObj.optString("reftabledatacolumn", "").contains(")") && (jObj.optString("defaultHeader", "").contains("Amount") || jObj.optString("defaultHeader", "").contains(CustomReportConstants.CLOSED_MANUALLY) || jObj.optString("defaultHeader", "").contains(CustomReportConstants.UNIT_PRICE_INCLUDING_GST) || jObj.optString("defaultHeader", "").contains(CustomReportConstants.BaseQuantity)) && jObj.optBoolean("isLineItem", false)) {
                                                dataIndexList.put(jObj.optString("crossJoinMainTable", "") + "." + "lineAmount" + cnt, "#" + jObj.getString("defaultHeader") + "#");
                                            } else if (jObj.optString("defaultHeader", "").contains(CustomReportConstants.BaseUOMQuantity)) {
                                                dataIndexList.put(jObj.optString("crossJoinMainTable", "") + "." + "baseUOMQty", "#" + jObj.getString("defaultHeader") + "#");
                                            } else if (jObj.optString("reftabledatacolumn", "").contains("(") && jObj.optString("reftabledatacolumn", "").contains(")") && (jObj.optString("defaultHeader", "").contains("Amount") || jObj.optString("defaultHeader", "").contains(CustomReportConstants.CLOSED_MANUALLY) || jObj.optString("defaultHeader", "").contains(CustomReportConstants.UNIT_PRICE_INCLUDING_GST) || jObj.optString("defaultHeader", "").contains(CustomReportConstants.BaseQuantity)) && jObj.optBoolean("isLineItem", false)) {
                                                dataIndexList.put(jObj.optString("crossJoinMainTable", "") + "." + "lineAmount" + cnt, "#" + jObj.getString("defaultHeader") + "#");
                                            } else if (stbCrossModuleSelectClause.toString().contains("." + jObj.optString("reftabledatacolumn", "")) && jObj.optString("reftabledatacolumn", "").equals("discount")) {
                                                dataIndexList.put(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("reftabledatacolumn", "") + cnt, "#" + jObj.getString("defaultHeader") + "#");
                                            } else {
                                                if(!jObj.optString("reftabledatacolumn").equals("")){
                                                dataIndexList.put(jObj.optString("crossJoinMainTable") + "." + jObj.optString("reftabledatacolumn"), "#" + jObj.getString("defaultHeader") + "#");
                                                }
                                            }
                                        }else if(dataIndexList.containsKey(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("reftabledatacolumn", ""))) {
                                            dataIndexList.put(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("reftabledatacolumn", "") + cnt, "#" + jObj.getString("defaultHeader") + "#");
                                        } else {
                                            if (jObj.optString("reftabledatacolumn", "").contains("(") && jObj.optString("reftabledatacolumn", "").contains(")") && (jObj.optString("defaultHeader", "").contains("Amount") || jObj.optString("defaultHeader", "").contains(CustomReportConstants.CLOSED_MANUALLY)|| jObj.optString("defaultHeader", "").contains(CustomReportConstants.UNIT_PRICE_INCLUDING_GST)|| jObj.optString("defaultHeader", "").contains(CustomReportConstants.BaseQuantity)) && jObj.optBoolean("isLineItem", false)) {
                                                dataIndexList.put(jObj.optString("crossJoinMainTable", "") + "." + "lineAmount" + cnt, "#" + jObj.getString("defaultHeader") + "#");
                                            } else if (stbCrossModuleSelectClause.toString().contains("." + jObj.optString("reftabledatacolumn", "")) && jObj.optString("reftabledatacolumn", "").equals("discount")) {
                                                dataIndexList.put(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("reftabledatacolumn", "") + cnt, "#" + jObj.getString("defaultHeader") + "#");
                                            } else if (jObj.optString("defaultHeader", "").contains(CustomReportConstants.BaseUOMQuantity)) {
                                                dataIndexList.put(jObj.optString("crossJoinMainTable", "") + "." + "baseUOMQty", "#" + jObj.getString("defaultHeader") + "#");
                                            } else if(!jObj.optString("defaultHeader").contains(CustomReportConstants.Product_Name) && !jObj.optString("defaultHeader").contains(CustomReportConstants.Product_Tax) && !!jObj.optString("defaultHeader").contains(CustomReportConstants.Acc_Tax_Name)){
                                                dataIndexList.put(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("reftabledatacolumn", ""), "#" + jObj.getString("defaultHeader") + "#");
                                            }
                                        }
                                    }
                                    stbCrossModuleSelectMap.put(jObj.optString("crossJoinMainTable", ""), stbCrossModuleSelectClause.toString());
                                    JSONArray customFieldsExtraJoin = jObj.optJSONArray("customfieldsextrajoin");
                                    if (customFieldsExtraJoin != null) {
                                        for (int arrayCnt = 0; arrayCnt < customFieldsExtraJoin.length(); arrayCnt++) {
                                            JSONObject objectInArray = customFieldsExtraJoin.getJSONObject(arrayCnt);
                                            Iterator<?> keys = objectInArray.keys();
                                            while (keys.hasNext()) {
                                                String key = (String) keys.next();
                                                String value = objectInArray.getString(key);
//                                                if(!stbCrossModuleJoinClause.toString().contains(value)){
//                                                stbCrossModuleJoinClause.append(value);
                                                if (!stbCrossModuleJoinClause.toString().contains(value)) {
                                                    if(!stbCrossModuleJoinClause.toString().contains("left join " + key)) {
                                                        stbCrossModuleJoinClause.append(value);
                                                    } else {
                                                        replaceAll(stbCrossModuleJoinClause, " left join " + jObj.getString("reftablename") + " on " + jObj.getString("reftablename") + "." + jObj.getString("reftablefk") + " = " + jObj.optString("dbtablename",jObj.optString("crossJoinMainTable", "")) + "." + jObj.getString("dbcolumnname") + " ", " left join " + jObj.getString("reftablename") + " as "+ key+cnt +" on " + jObj.getString("reftablename")+ cnt + "." + jObj.getString("reftablefk") + " = " + jObj.optString("dbtablename",jObj.optString("crossJoinMainTable", "")) + "." + jObj.getString("dbcolumnname") + " ");
                                                        stbCrossModuleJoinClause.append(value);
                                                    }

                                                }
//                                                }
                                            }
                                        }
                                        iscustomFieldsExtraJoinAdded = true;
                                    }
                                    if(isdateflag){
                                        if (!stbCrossModuleJoinClause.toString().contains(" left join " + jObj.getString("reftablename"))) {
                                            stbCrossModuleJoinClause.append(" left join " + jObj.getString("reftablename") + " on " + jObj.getString("reftablename") + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " ");
                                        }
//                                        stbCrossModuleJoinClause.append(" left join " + jObj.getString("reftablename") + " on " + jObj.getString("reftablename") + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " ");
                                        if(jObj.optString("reftabledatacolumn", "").contains("COALESCE")) {
                                            replaceAll(stbCrossModuleSelectClause,jObj.optString("dbtablename", "") + "." + jObj.optString("dbcolumnname", "") + ",",jObj.optString("reftabledatacolumn", "") + " as InvoiceDate ,");
                                        } else {
                                            replaceAll(stbCrossModuleSelectClause,jObj.optString("dbtablename", "") + "." + jObj.optString("dbcolumnname", "") + ",",jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + ",");
                                        }
                                        stbCrossModuleSelectMap.put(jObj.optString("crossJoinMainTable", ""),stbCrossModuleSelectClause.toString() );
                                        if (dataIndexList.containsKey(jObj.optString("crossJoinMainTable", "") + ".journalentry")) {
                                            dataIndexList.remove(jObj.optString("crossJoinMainTable", "") + ".journalentry");
                                            if (jObj.optString("reftabledatacolumn", "").contains("COALESCE")) {
                                                dataIndexList.put(jObj.optString("crossJoinMainTable", "") + ".InvoiceDate"  , "#" + jObj.getString("defaultHeader") + "#");
                                            } else {
                                                dataIndexList.put(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("reftabledatacolumn", ""), "#" + jObj.getString("defaultHeader") + "#");
                                            }
                                        }
                                    }
                                    stbCrossModuleJoinMap.put(jObj.optString("crossJoinMainTable", ""), stbCrossModuleJoinClause.toString());
                                } else {
                                    stbCrossModuleJoinClause.append(getLinkedTableNameForModuleID(moduleid) + ".linkeddocid " + "," + getLinkedTableNameForModuleID(moduleid) + ".docid"
                                            + " from " + jObj.optString("crossJoinMainTable", "") + " inner join " + getLinkedTableNameForModuleID(moduleid) + " on " + jObj.optString("crossJoinMainTable", "") + "." + accCustomerReportServiceDao.getmoduledataRefPKColName(jObj.optString("crossJoinMainTable", "")) + " = " + getLinkedTableNameForModuleID(moduleid) + ".linkeddocid"
                                            + "  ");
                                    stbCrossModuleSelectMap.put(jObj.optString("crossJoinMainTable", ""),stbCrossModuleSelectClause.toString() );
                                    stbCrossModuleJoinMap.put(jObj.optString("crossJoinMainTable", ""), stbCrossModuleJoinClause.toString());
                                    if (!StringUtil.isNullOrEmpty(jObj.optString("extrainnerjoin", ""))) {
                                        stbCrossModuleJoinMap.put(jObj.optString("crossJoinMainTable", ""), (stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")) + jObj.optString("extrainnerjoin", "")));
                                    }
                                    dataIndexList.remove(jObj.optString("dbtablename", "") + "." + jObj.optString("dbcolumnname", ""));
                                    dataIndexList.put(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("dbcolumnname", ""), "#" + jObj.getString("defaultHeader") + "#");
                                }

                            }
                            if ((jObj.optInt("xtype") == 4 || customfield || reftablename.contains("billing") || (jObj.optString("defaultHeader").equals(CustomReportConstants.Product_Name))) && (!jObj.getString("defaultHeader").startsWith(CustomReportConstants.UOM) || (jObj.getString("defaultHeader").startsWith(CustomReportConstants.UOM) && jObj.getString("reftablename").equals(CustomReportConstants.inventory)))) {
                                if (stbCrossModuleJoinMap.containsKey(jObj.optString("crossJoinMainTable", ""))) {
                                    String mapValue = stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", ""));
                                    if (!mapValue.contains(" left join " + jObj.getString("reftablename") + " on " + jObj.getString("reftablename") + "." + jObj.getString("reftablefk") + " = " + jObj.optString("dbtablename") + "." + jObj.getString("dbcolumnname") + " ") && jObj.optJSONArray("customfieldsextrajoin") == null) {
                                        if (!stbCrossModuleJoinClause.toString().contains("join " + jObj.getString("reftablename"))) {
                                            if (jObj.optString("dbtablename").equals(getDetailsTableForMainTable(jObj.optString("crossJoinMainTable"))) && !stbCrossModuleJoinClause.toString().contains(" left join " + getDetailsTableForMainTable(jObj.optString("crossJoinMainTable")))) {
                                                String crossJoinMainTableForeignKeyInDetailTable = accCustomerReportServiceDao.getFKofCrossJoinMainTable(jObj.optString("crossJoinMainTable"), jObj.getString("dbtablename"));
                                                String detailTableMissingJoin = " left join " + jObj.getString("dbtablename") + " on " + jObj.getString("dbtablename") + "." + crossJoinMainTableForeignKeyInDetailTable + " = " + jObj.optString("crossJoinMainTable") + ".id ";
                                                stbCrossModuleJoinClause.append(detailTableMissingJoin);
                                            }
                                            stbCrossModuleJoinClause.append(" left join " + jObj.getString("reftablename") + " on " + jObj.getString("reftablename") + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " ");
                                            stbCrossModuleJoinMap.put(jObj.optString("crossJoinMainTable", ""), stbCrossModuleJoinClause.toString());
                                        } else {
                                            if (!stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")).toString().contains(" left join " + jObj.getString("reftablename") + " as " + jObj.getString("reftablename") + cnt + " on " + jObj.getString("reftablename") + cnt + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " ")) {
                                                stbCrossModuleJoinMap.put(jObj.optString("crossJoinMainTable", ""), (stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")) + " left join " + jObj.getString("reftablename") + " as " + jObj.getString("reftablename") + cnt + " on " + jObj.getString("reftablename") + cnt + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + "." + jObj.getString("dbcolumnname") + " "));
                                            }
                                        }
                                    }
                                   
                                }
                                if (jObj.optBoolean("allowcrossmodule") && jObj.optBoolean("customfield") && jObj.optInt("xtype") == 4 ){
                                    String replaceString = "";
                                    if (stbCrossModuleSelectMap.containsKey(jObj.optString("crossJoinMainTable", ""))) {
                                        if (stbCrossModuleSelectMap.get(jObj.optString("crossJoinMainTable", "")).contains("fieldcombodata.value")) {
                                            replaceString = " fieldcombodata" + cnt + ".value as value" + cnt + " ,";
                                        } else {
                                            if (stbCrossModuleSelectMap.get(jObj.optString("crossJoinMainTable", "")).contains("masteritem.value")) {
                                                replaceString = " fieldcombodata.value as value" + cnt + " ,";
                                            } else {
                                                replaceString = " fieldcombodata.value as value " + " ,";
                                            }
                                        }
                                        replaceAll(stbCrossModuleSelectClause, jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + ",", replaceString);
                                        stbCrossModuleSelectMap.put(jObj.optString("crossJoinMainTable", ""), stbCrossModuleSelectClause.toString());

                                    }
                                    if (stbCrossModuleJoinMap.containsKey(jObj.optString("crossJoinMainTable", ""))) {

                                        if (stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")).contains("fieldcombodata")) {
                                            stbCrossModuleJoinMap.put(jObj.optString("crossJoinMainTable", ""), (stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")) + " left join fieldcombodata as fieldcombodata" + cnt + " on fieldcombodata" + cnt + ".id " + "=" + jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + " " + " "));
                                        } else {
                                            stbCrossModuleJoinMap.put(jObj.optString("crossJoinMainTable", ""), (stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")) + " left join fieldcombodata on fieldcombodata.id " + "=" + jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + " "));
                                        }
                                        stbCrossModuleJoinClause.setLength(0);
                                        stbCrossModuleJoinClause.append(stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")));
                                    }
                                     if(jObj.optString("dbtablename").equals(CustomReportConstants.product)){
                                        stbCrossModuleSelectClause.append(jObj.optString("reftablename")+"."+jObj.optString("reftabledatacolumn")+" ,");
                                        stbCrossModuleSelectMap.put(jObj.optString("crossJoinMainTable", ""), stbCrossModuleSelectClause.toString());
                                    }
                                }
                                 if(!(jObj.optBoolean("isLineItem") && jObj.optBoolean("allowcrossmodule") && (jObj.optString("defaultHeader").startsWith(CustomReportConstants.Acc_Product_Name)))){
                                    if (!dataIndexList.containsKey(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("dbcolumnname", ""))) {
                                        dataIndexList.remove(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("dbcolumnname", ""));
                                    } else {
                                    if(dataIndexList.containsKey(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("dbcolumnname", ""))) {
                                            if (!dataIndexList.containsKey(jObj.optString("crossJoinMainTable", "") + ".id") && !dataIndexList.get(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("dbcolumnname", "")).equals("#Discount#")) {
                                                dataIndexList.remove(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("dbcolumnname", ""));
                                            }
                                        } else {
                                            dataIndexList.remove(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("dbcolumnname", "") + cnt);
                                        }

                                    }
//                                dataIndexList.remove(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("dbcolumnname", ""));
                                if(dataIndexList.containsKey(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("reftabledatacolumn", "")+ cnt)){
                                        dataIndexList.remove(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("reftabledatacolumn", "") + cnt);
                                    } else {
                                    if (!dataIndexList.containsKey(jObj.optString("crossJoinMainTable", "")+".id") && !dataIndexList.containsKey(jObj.optString("crossJoinMainTable", "")+".name")){
                                            dataIndexList.remove(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("reftabledatacolumn", ""));
                                        }
                                    }
                                    if (dataIndexList.containsKey(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("reftabledatacolumn", "")) && !jObj.optString("defaultHeader").startsWith("Product Tax")) {
                                        if (stbCrossModuleSelectClause.toString().contains(jObj.optString("reftabledatacolumn", "") + cnt)) {
                                            dataIndexList.put(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("reftabledatacolumn", "") + cnt, "#" + jObj.getString("defaultHeader") + "#");
                                        }
                                    } else {
//                                    if (fieldDupList.contains(jObj.optString("reftabledatacolumn", ""))) {
//                                        dataIndexList.put(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("reftabledatacolumn", "") + cnt , "#" + jObj.getString("defaultHeader") + "#");
//                                    } else {

                                        JSONArray defaultMapjarrayObj = jObj.optJSONArray("defaultmappingJSONObj");
                                        if (defaultMapjarrayObj != null) {
                                            for (int mapCount = 0; mapCount < defaultMapjarrayObj.length(); mapCount++) {
                                                JSONObject mapjObj = defaultMapjarrayObj.getJSONObject(mapCount);
                                                if (Boolean.valueOf(mapjObj.optString("isSelectDataIndex", "false"))) {
                                                    String mappingDefaultHeader = mapjObj.getString("defaultHeader");
                                                    if (dataIndexList.containsKey(mappingDefaultHeader)) {
                                                        if (dataIndexListDup.containsKey(mappingDefaultHeader)) {
                                                            dupList = dataIndexListDup.get(mappingDefaultHeader);
                                                        }
                                                        dupList.add("#" + jObj.getString("defaultHeader") + "#");
                                                        dataIndexListDup.put(mappingDefaultHeader, dupList);
                                                    } else {
                                                        if (dataIndexList.containsKey(jObj.optString("crossJoinMainTable", "") + "." + mapjObj.optString("reftabledatacolumn", ""))) {
                                                            dataIndexList.put(jObj.optString("crossJoinMainTable", "") + "." + mapjObj.optString("reftabledatacolumn", "") + cnt, "#" + jObj.getString("defaultHeader") + "#");
                                                        } else {
                                                            dataIndexList.put(jObj.optString("crossJoinMainTable", "") + "." + mapjObj.optString("reftabledatacolumn", ""), "#" + jObj.getString("defaultHeader") + "#");
                                                        }

                                                    }
                                                }
                                                if (stbCrossModuleSelectClause.toString().contains("." + mapjObj.optString("reftabledatacolumn"))) {
                                                replaceAll(stbCrossModuleSelectClause, jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + ",", mapjObj.optString("reftablename", "") + "." + mapjObj.optString("reftabledatacolumn", "") + " as " + mapjObj.optString("reftabledatacolumn", "") + cnt +",");
                                                } else {
                                                    replaceAll(stbCrossModuleSelectClause, jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + ",", mapjObj.optString("reftablename", "") + "." + mapjObj.optString("reftabledatacolumn", "") + ",");
                                                }
//                                            replaceAll(stbCrossModuleSelectClause, jObj.optString("reftablename", "") + "." + jObj.optString("reftabledatacolumn", "") + ",", mapjObj.optString("reftablename", "") + "." + mapjObj.optString("reftabledatacolumn", "") + ",");
                                                stbCrossModuleSelectMap.put(jObj.optString("crossJoinMainTable", ""), stbCrossModuleSelectClause.toString());
                                            }
                                        } else {
                                            dataIndexList.put(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("reftabledatacolumn", ""), "#" + jObj.getString("defaultHeader") + "#");
                                        }
//                                    }

                                        if (jObj.optInt("xtype") == 4 && !jObj.optString("defaultHeader", "").equalsIgnoreCase(CustomReportConstants.Acc_Tax_Name) && !jObj.optString("defaultHeader", "").equalsIgnoreCase(CustomReportConstants.Acc_Sales_Person) && !jObj.optString("defaultHeader", "").equalsIgnoreCase(CustomReportConstants.Acc_Agent) && (!jObj.optBoolean("isLineItem") || jObj.optBoolean("customfield"))) {
                                            if (stbCrossModuleSelectMap.containsKey(jObj.optString("crossJoinMainTable", ""))) {
                                                if (stbCrossModuleSelectMap.get(jObj.optString("crossJoinMainTable", "")).contains("fieldcombodata.value")) {
//                                                if (fieldDupList.contains(jObj.optString("reftabledatacolumn", ""))) {
//                                                    dataIndexList.remove(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("reftabledatacolumn", "")+cnt);
//                                                } else {
                                                    dataIndexList.remove(jObj.optString("crossJoinMainTable", "") + "." + jObj.optString("reftabledatacolumn", ""));
//                                                }
                                                    if (dataIndexList.containsKey(jObj.optString("crossJoinMainTable", "") + ".value")) {
                                                        dataIndexList.put(jObj.optString("crossJoinMainTable", "") + ".value" + cnt, "#" + jObj.getString("defaultHeader") + "#");
                                                    } else {
                                                        if (stbCrossModuleSelectMap.get(jObj.optString("crossJoinMainTable", "")).contains(".value as value" + cnt)) {
                                                            dataIndexList.put(jObj.optString("crossJoinMainTable", "") + ".value" + cnt, "#" + jObj.getString("defaultHeader") + "#");
                                                        } else {
                                                            dataIndexList.put(jObj.optString("crossJoinMainTable", "") + ".value", "#" + jObj.getString("defaultHeader") + "#");
                                                        }
                                                    }

                                                }

                                            }
                                        }
                                    }
                                }
                            }

                            if (!isCrossModuleExtraJoinAdded2) {
//                                KwlReturnObject linkedModulesJoinDetails = accCustomerReportServiceDao.getLinkedModuleJoinDetails(Integer.toString(moduleid), jObj.optString("linkedmodule", ""));
//                                List<CrossModuleJoinDetails> linkedModuleJoinDetail = linkedModulesJoinDetails.getEntityList();
//                                if (linkedModuleJoinDetail != null && linkedModuleJoinDetail.size() > 0) {
//                                    // Default Fields
//                                    for (CrossModuleJoinDetails linkedMoudleJoin : linkedModuleJoinDetail) {
//                                        crossJoinIndexVal = buildCustomFieldExtraJoins(linkedMoudleJoin.getFromTable(), linkedMoudleJoin.getJoinTable(), stbCrossModuleSelectMap, stbCrossModuleJoinMap, dataIndexList, jObj, cnt, crossModuleRefTableList, corssFielStringBuilder);
//                                    }
//                                    isCrossModuleExtraJoinAdded2 = true;
//                                }
                            } else {
                                if (!(jObj.optString("reftablename", "").equalsIgnoreCase(jObj.optString("dbtablename", "")))) {
                                    String refTable = jObj.optString("reftablename", "");
                                    if ((!crossModuleRefTableList.contains(refTable) && !refTable.equals(jObj.optString("mainTable", "")))) {
                                        crossModuleRefTableList.add(refTable);
                                        if (!StringUtil.isNullOrEmpty(jObj.optString("reftablefk", ""))) {
                                            String joinTable1 = !StringUtil.isNullOrEmpty(jObj.optString("dbtablename", "")) ? jObj.optString("dbtablename", "") : jObj.optString("mainTable", "");
                                            stbCrossModuleJoinMap.put(refTable, " left join " + refTable + " on " + refTable + "." + jObj.getString("reftablefk") + " = " + joinTable1 + "." + jObj.getString("dbcolumnname") + " ");
                                            stbCrossModuleSelectMap.put(refTable, "NO_SELECT");
                                            corssFielStringBuilder.append(refTable + "." + jObj.getString("reftabledatacolumn") + ",");
                                        }
                                    } else {
                                        if (stbCrossModuleJoinMap.containsKey(refTable)) {
                                            String duprefTable1 = refTable + cnt;
                                            refTable = duprefTable1;
                                            stbCrossModuleJoinMap.put(duprefTable1, " left join " + jObj.getString("reftablename") + " as " + duprefTable1 + " on " + duprefTable1 + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + cnt + "." + jObj.getString("dbcolumnname") + " ");
                                            stbCrossModuleSelectMap.put(refTable, "NO_SELECT");
                                            corssFielStringBuilder.append(duprefTable1 + "." + jObj.getString("reftabledatacolumn") + ",");
                                        }
                                    }
                                    if (dataIndexList.containsValue("#" + jObj.getString("defaultHeader") + "#")) {
                                        Set<String> keys = dataIndexList.keySet();
                                        for (String k : keys) {
                                            if (dataIndexList.get(k).equalsIgnoreCase("#" + jObj.getString("defaultHeader") + "#")) {
                                                if (corssFielStringBuilder.lastIndexOf(",") > 0) {
                                                    dataIndexList.put("COALESCE(" + corssFielStringBuilder.toString().substring(0, corssFielStringBuilder.lastIndexOf(",")) + ")", dataIndexList.get(k));
                                                    dataIndexList.remove(k);
                                                    break;
                                                }
                                            }
                                        }

                                    }

                                }

                            }
                        } else { // For measure item                        
                            if (addedColType.contains(jObj.optString("columntype", "").substring(jObj.optString("columntype", "").indexOf("-"), jObj.optString("columntype", "").length()))) {
                                JSONArray mapjarrayObj = jObj.optJSONArray("mappingJSONObj");
                                for (int mapCount = 0; mapCount < mapjarrayObj.length(); mapCount++) {
                                    JSONObject mapjObj = mapjarrayObj.getJSONObject(mapCount);

                                    if (Boolean.valueOf(mapjObj.optString("isDataIndex", "false"))) {
                                        if (dataIndexList.containsKey(mapjObj.getString("defaultHeader"))) {
                                            String key = mapjObj.getString("defaultHeader");

                                            if (stbCrossModuleJoinMap.containsKey(mapjObj.getString("reftablename") + cnt)) {
                                                key = mapjObj.getString("reftablename") + cnt + "." + mapjObj.getString("reftabledatacolumn");
                                            }

                                            if (dataIndexListDup.containsKey(key)) {
                                                dupList = dataIndexListDup.get(key);
                                            }
                                            dupList.add("#" + jObj.getString("defaultHeader") + "#");
                                            dataIndexListDup.put(mapjObj.getString("defaultHeader"), dupList);
                                        } else {
                                            if (stbCrossModuleSelectClause.indexOf(mapjObj.optString("defaultHeader", "")) < 0) {
                                                stbCrossModuleSelectClause.append(mapjObj.optString("defaultHeader", "") + ",");
                                            } else if(dataIndexList.containsValue("#"+CustomReportConstants.TERM_AMOUNT+"#")) {
                                                stbCrossModuleSelectClause.append(mapjObj.optString("defaultHeader", "") + ",");
                                            }
                                            if (dataIndexListDup.size() > 0) {
                                                Set<String> dupKeys = dataIndexListDup.keySet();
                                                for (String fieldname : dupKeys) {
                                                    ArrayList<String> listEntry = dataIndexListDup.get(fieldname);
                                                    for (int dupCnt = 0; dupCnt < listEntry.size(); dupCnt++) {
                                                        String dupVal = listEntry.get(dupCnt);
                                                        dupVal = dupVal.replaceAll("#", "");
                                                        if (dupVal.matches(jObj.getString("defaultHeader").substring(0, jObj.getString("defaultHeader").length() - 1))) {
//                                                        dupList.add("#" + jObj.getString("defaultHeader") + "#");
                                                            ArrayList<String> listEntry2 = dataIndexListDup.get(mapjObj.getString("defaultHeader"));
                                                            if (listEntry2 != null) {
                                                                listEntry2.addAll(dupList);
                                                                listEntry2.add("#" + jObj.getString("defaultHeader") + "#");
                                                                dataIndexListDup.put(mapjObj.getString("defaultHeader"), listEntry2);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            dataIndexList.put(mapjObj.getString("defaultHeader"), "#" + jObj.getString("defaultHeader") + "#");
                                        }
                                    }

                                }
                                stbCrossModuleSelectMap.put(jObj.optString("crossJoinMainTable", ""),stbCrossModuleSelectClause.toString() );

                            } else {
                                stbCrossModuleSelectClause.setLength(0);
                                stbCrossModuleJoinClause.setLength(0);
                                addedColType.add(jObj.optString("columntype", "").substring(jObj.optString("columntype", "").indexOf("-"), jObj.optString("columntype", "").length()));

                                JSONArray mapjarrayObj = jObj.getJSONArray("mappingJSONObj");
                                for (int mapCount = 0; mapCount < mapjarrayObj.length(); mapCount++) {
                                    JSONObject mapjObj = mapjarrayObj.getJSONObject(mapCount);
                                    if (Boolean.valueOf(mapjObj.optString("isDataIndex", "false"))) {
                                        stbCrossModuleSelectClause.append(mapjObj.optString("defaultHeader", "") + ",");
                                    }
                                    if (mapCount == 0) {
                                        stbCrossModuleJoinClause.append(getLinkedTableNameForModuleID(moduleid) + ".linkeddocid " + "," + getLinkedTableNameForModuleID(moduleid) + ".docid"
                                                + " from " + jObj.optString("crossJoinMainTable", "") + " inner join " + getLinkedTableNameForModuleID(moduleid) + " on " + jObj.optString("crossJoinMainTable", "") + "." + accCustomerReportServiceDao.getmoduledataRefPKColName(jObj.optString("crossJoinMainTable", "")) + " = " + getLinkedTableNameForModuleID(moduleid) + ".linkeddocid"
                                                + "  ");
                                        stbCrossModuleJoinMap.put(jObj.optString("crossJoinMainTable", ""), stbCrossModuleJoinClause.toString());
                                        if (!StringUtil.isNullOrEmpty(mapjObj.optString("extrainnerjoin", ""))) {
                                            stbCrossModuleJoinMap.put(jObj.optString("crossJoinMainTable", ""), (stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")) + mapjObj.optString("extrainnerjoin", "")));
                                            stbCrossModuleJoinClause.setLength(0);
                                            stbCrossModuleJoinClause.append(stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")));
                                        }

                                    } else {
                                        if (!StringUtil.isNullOrEmpty(mapjObj.optString("extrainnerjoin", ""))) {
                                            stbCrossModuleJoinMap.put(jObj.optString("crossJoinMainTable", ""), (stbCrossModuleJoinMap.get(jObj.optString("crossJoinMainTable", "")) + mapjObj.optString("extrainnerjoin", "")));
                                        }
                                    }
                                    stbCrossModuleSelectMap.put(jObj.optString("crossJoinMainTable", ""), stbCrossModuleSelectClause.toString());
                                    if (mainTable.equalsIgnoreCase(mapjObj.getString("reftablename").trim())) {
                                        stbCrossModuleJoinMap.put(mapjObj.getString("dbtabletame"), mapjObj.getString("extrainnerjoin"));
                                    } else {
                                        if (!stbCrossModuleJoinMap.containsKey(mapjObj.getString("reftablename"))) {
                                            stbCrossModuleJoinMap.put(mapjObj.getString("reftablename"), mapjObj.getString("extrainnerjoin"));
                                        }
                                    }

                                    if (Boolean.valueOf(mapjObj.optString("isDataIndex", "false"))) {
                                        if (dataIndexList.containsKey(mapjObj.getString("defaultHeader"))) {
                                            String key = mapjObj.getString("defaultHeader");

                                            if (stbCrossModuleJoinMap.containsKey(mapjObj.getString("reftablename") + cnt)) {
                                                key = mapjObj.getString("reftablename") + cnt + "." + mapjObj.getString("reftabledatacolumn");
                                            }

                                            if (dataIndexListDup.containsKey(key)) {
                                                dupList = dataIndexListDup.get(key);
                                            }
                                            dupList.add("#" + jObj.getString("defaultHeader") + "#");
                                            dataIndexListDup.put(mapjObj.getString("defaultHeader"), dupList);
                                        } else {
                                            if (dataIndexListDup.size() > 0) {
                                                Set<String> dupKeys = dataIndexListDup.keySet();
                                                for (String fieldname : dupKeys) {
                                                    ArrayList<String> listEntry = dataIndexListDup.get(fieldname);
                                                    for (int dupCnt = 0; dupCnt < listEntry.size(); dupCnt++) {
                                                        String dupVal = listEntry.get(dupCnt);
                                                        dupVal = dupVal.replaceAll("#", "");
                                                        if (dupVal.matches(jObj.getString("defaultHeader").substring(0, jObj.getString("defaultHeader").length() - 1))) {
                                                            dupList.add("#" + jObj.getString("defaultHeader") + "#");
                                                            dataIndexListDup.put(mapjObj.getString("defaultHeader"), dupList);
                                                        }
                                                    }
                                                }
                                            }
                                            dataIndexList.put(mapjObj.getString("defaultHeader"), "#" + jObj.getString("defaultHeader") + "#");
                                        }
                                    }

                                }

                            }
                        } //measure item end                    
                        // Added below code for adding cross join for main module details table when line item for main module is not selected 
                        if (iscrossmodule) {
                            if (CustomReportConstants.Acc_Sales_Order.equalsIgnoreCase(jObj.optString("mainTable", "")) && !joinMap.containsKey(CustomReportConstants.Acc_Sales_Order_Details)) {
                                joinMap.put(CustomReportConstants.Acc_Sales_Order_Details, " left join sodetails on sodetails.salesorder = salesorder.id ");
                                defaultMappingSet.add(" left join sodetails on sodetails.salesorder = salesorder.id ");
                                if (jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.product)) {
                                    joinMap.put("inventory", "  left join inventory on inventory.id  = sodetails.id ");
                                    defaultMappingSet.add("  left join inventory on inventory.id  = sodetails.id ");
                                }
                            } else if (CustomReportConstants.Acc_Sales_Invoice.equalsIgnoreCase(jObj.optString("mainTable", "")) && !joinMap.containsKey(CustomReportConstants.Acc_Sales_Invoice_Details)) {
                                joinMap.put(CustomReportConstants.Acc_Sales_Invoice_Details, "  left join invoicedetails on invoicedetails.invoice = invoice.id ");
                                defaultMappingSet.add(" left join invoicedetails on invoicedetails.invoice = invoice.id ");
                                    if (jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.product) || jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.PRODUCTCATEGORY)) {
                                    joinMap.put("inventory", " left join inventory on inventory.id  = invoicedetails.id ");
                                    defaultMappingSet.add(" left join inventory on inventory.id = invoicedetails.id ");
                                }
                            } else if (CustomReportConstants.Acc_Credit_Note.equalsIgnoreCase(jObj.optString("mainTable", "")) && !joinMap.containsKey(CustomReportConstants.Acc_Credit_Note_Details)) {
                                joinMap.put(CustomReportConstants.Acc_Credit_Note_Details, "  left join cndetails on cndetails.creditNote = creditnote.id ");
                                defaultMappingSet.add("  left join cndetails on cndetails.creditNote = creditnote.id ");
                            } else if (CustomReportConstants.Acc_Debit_Note.equalsIgnoreCase(jObj.optString("mainTable", "")) && !joinMap.containsKey(CustomReportConstants.Acc_Debit_Note_Details)) {
                                joinMap.put(CustomReportConstants.Acc_Debit_Note_Details, "  left join dndetails on dndetails.debitNote = debitnote.id ");
                                defaultMappingSet.add("  left join dndetails on dndetails.debitNote = debitnote.id ");
                            } else if (CustomReportConstants.Acc_Goods_Receipt.equalsIgnoreCase(jObj.optString("mainTable", "")) && !joinMap.containsKey(CustomReportConstants.Acc_Goods_Receipt_Details)) {
                                joinMap.put(CustomReportConstants.Acc_Goods_Receipt_Details, "  left join grodetails on grodetails.grorder = grorder.id ");
                                defaultMappingSet.add(" left join grodetails on grodetails.grorder = grorder.id ");
                            } else if (CustomReportConstants.Acc_Purchase_Order.equalsIgnoreCase(jObj.optString("mainTable", "")) && !joinMap.containsKey(CustomReportConstants.Acc_Purchase_Order_Details)) {
                                joinMap.put(CustomReportConstants.Acc_Purchase_Order_Details, "  left join podetails on podetails.purchaseorder = purchaseorder.id ");
                                defaultMappingSet.add("  left join podetails on podetails.purchaseorder = purchaseorder.id ");
                            } else if (CustomReportConstants.Acc_Vendor_Invoice.equalsIgnoreCase(jObj.optString("mainTable", "")) && !joinMap.containsKey(CustomReportConstants.Acc_Vendor_Invoice_Details)) {
                                if (!joinMap.containsValue("  left join grdetails on grdetails.goodsreceipt = goodsreceipt.id ")) {
                                    joinMap.put("jedetail", " left join jedetail on jedetail.journalentry = goodsreceipt.journalentry ");
                                    joinMap.put(CustomReportConstants.Acc_Vendor_Invoice_Details, "  left join grdetails on grdetails.goodsreceipt = goodsreceipt.id ");
                                    defaultMappingSet.add("  left join jedetail on jedetail.journalentry = goodsreceipt.journalenty ");
                                    defaultMappingSet.add("  left join grdetails on grdetails.goodsreceipt = goodsreceipt.id ");
                                }
                                if (jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.product)) {
                                    joinMap.put("inventory", "  left join inventory on inventory.id = grdetails.id  ");
                                    defaultMappingSet.add("  left join inventory on inventory.id = grdetails.id  ");
                                }
                            } else if (CustomReportConstants.Acc_Sales_Return.equalsIgnoreCase(jObj.optString("mainTable", "")) && !joinMap.containsKey(CustomReportConstants.Acc_Sales_Return_Details)) {
                                joinMap.put(CustomReportConstants.Acc_Sales_Return_Details, "  left join srdetails on srdetails.salesreturn = salesreturn.id ");
                                defaultMappingSet.add("  left join srdetails on srdetails.salesreturn = salesreturn.id ");
                                if (jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.product) && !joinMap.containsKey(CustomReportConstants.inventory)) {
                                    joinMap.put("inventory", "  left join inventory on inventory.id = srdetails.id  ");
                                    defaultMappingSet.add("  left join inventory on inventory.id = srdetails.id  ");
                                }
                            } else if (CustomReportConstants.Acc_Purchase_Return.equalsIgnoreCase(jObj.optString("mainTable", "")) && !joinMap.containsKey(CustomReportConstants.Acc_Purchase_Return_Details)) {
                                joinMap.put(CustomReportConstants.Acc_Purchase_Return_Details, "  left join prdetails on prdetails.purchasereturn = purchasereturn.id ");
                                defaultMappingSet.add("  left join prdetails on prdetails.purchasereturn = purchasereturn.id ");
                                if (jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.product) && !joinMap.containsKey(CustomReportConstants.inventory)) {
                                    joinMap.put("inventory", "  left join inventory on inventory.id = prdetails.id  ");
                                    defaultMappingSet.add("  left join inventory on inventory.id = prdetails.id  ");
                                }
                            } else if (CustomReportConstants.Acc_Delivery_Order.equalsIgnoreCase(jObj.optString("mainTable", "")) && !joinMap.containsKey(CustomReportConstants.Acc_Delivery_Order_Details)) {
                                if(!defaultMappingSet.contains(" left join dodetails on dodetails.deliveryorder = deliveryorder.id")){
                                joinMap.put(CustomReportConstants.Acc_Delivery_Order_Details, " left join dodetails on dodetails.deliveryorder = deliveryorder.id");
                                defaultMappingSet.add(" left join dodetails on dodetails.deliveryorder = deliveryorder.id");
                                }
                                if (jObj.optString("crossJoinMainTable", "").equalsIgnoreCase(CustomReportConstants.product) && !joinMap.containsKey(CustomReportConstants.inventory)) {
                                    joinMap.put("inventory", "  left join inventory on inventory.id = dodetails.id  ");
                                    defaultMappingSet.add("  left join inventory on inventory.id = dodetails.id  ");
                            }
                        }
                    }
                    }
                }//end of isforformulabuilder check
                
            } catch (JSONException ex) {
                Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            cntVal = cnt;
        }
        String joinString = "";
        for (Map.Entry<String, String> entry : stbCrossModuleSelectMap.entrySet()) {
            String key = entry.getKey();
            String temp = mainTable;
//            int tempCnt =0;
            if (key != null) {
                for (int tempCnt = 1; tempCnt <= crossJoinIndexVal; tempCnt++) {
                    if (key.equalsIgnoreCase("deliveryorder" + tempCnt)) {
                        mainTable = "invoice";
                        break;
                    }
                    if (key.equalsIgnoreCase("invoice" + tempCnt)) {
                        mainTable = "deliveryorder";
                        break;
                    }
                }
//                for (Map.Entry<String, String> mainTableMapEntry : mainTableMap.entrySet()) {
//                    if(key.equalsIgnoreCase(mainTableMapEntry.getKey()+((tempCnt==0)?"":tempCnt))){
//                        mainTable = mainTableMapEntry.getValue();
//                        break;
//                    }
//                    tempCnt++;
//                }
                if ("NO_SELECT".equalsIgnoreCase(entry.getValue())) {
                    joinString = stbCrossModuleJoinMap.get(key);
                } else {
                    if(!key.equalsIgnoreCase(CustomReportConstants.product) && !key.equalsIgnoreCase(CustomReportConstants.PRODUCTCATEGORY)) {
                        joinString = " left join ( select " + stbCrossModuleSelectMap.get(key) + stbCrossModuleJoinMap.get(key) + subQueryCondition(crossJoinModuleID, crossJoinMainTable,key)+ " ) as " + key + " on " + key + ".docid = " + mainTable + "." + accCustomerReportServiceDao.getmoduledataRefPKColName(mainTable) + " ";
                    } else {
                        String subQuery = "";
                        if (stbCrossModuleSelectMap.get(key).lastIndexOf(",") > -1) {
                            subQuery = stbCrossModuleSelectMap.get(key).substring(0, stbCrossModuleSelectMap.get(key).lastIndexOf(","));
                }
                        if(mainTable.equalsIgnoreCase(CustomReportConstants.Acc_Sales_Order)){
                            joinString = " left join ( select " + subQuery + " from " + key + stbCrossModuleJoinMap.get(key) + subQueryCondition(crossJoinModuleID, crossJoinMainTable,key)+ " ) as " + key + " on " + key + "." + crossModuleAsAliasMap.get(key) + "= " + "sodetails" + "." + CustomReportConstants.product + " ";   
                        } else if(mainTable.equalsIgnoreCase(CustomReportConstants.Acc_Goods_Receipt)){
                            joinString = " left join ( select " + subQuery + " from " + key + stbCrossModuleJoinMap.get(key) + subQueryCondition(crossJoinModuleID, crossJoinMainTable,key)+ " ) as " + key + " on " + key + "." + crossModuleAsAliasMap.get(key) + "= " + "grodetails" + "." + CustomReportConstants.product + " ";   
                        } else if(mainTable.equalsIgnoreCase(CustomReportConstants.Acc_Delivery_Order)){
                            joinString = " left join ( select " + subQuery + " from " + key + stbCrossModuleJoinMap.get(key) + subQueryCondition(crossJoinModuleID, crossJoinMainTable,key)+ " ) as " + key + " on " + key + "." + crossModuleAsAliasMap.get(key) + "= " + "dodetails" + "." + CustomReportConstants.product + " ";   
                        } else if(mainTable.equalsIgnoreCase(CustomReportConstants.Acc_Sales_Invoice) && key.equalsIgnoreCase(CustomReportConstants.PRODUCTCATEGORY)) {
                            joinString = " left join ( select " + subQuery + " from " + CustomReportConstants.PRODUCTCATEGORYMAPPING + stbCrossModuleJoinMap.get(key) + subQueryCondition(crossJoinModuleID, crossJoinMainTable,key)+ " ) as " + key + " on " + key + "." + crossModuleAsAliasMap.get(key) + "= " + "inventory" + "." + CustomReportConstants.product + " ";   
                        }else {
                            if (joinMap.containsKey(key)) {
                                joinString = " left join ( select " + subQuery + " from " + key + stbCrossModuleJoinMap.get(key) + subQueryCondition(crossJoinModuleID, crossJoinMainTable,key) + " ) as " + key + " on " + key + "." + crossModuleAsAliasMap.get(key) + "= " + "inventory" + "." + CustomReportConstants.product + " ";
                            } else {
                                joinString = " left join ( select " + subQuery + " from " + key + stbCrossModuleJoinMap.get(key) + subQueryCondition(crossJoinModuleID, crossJoinMainTable,key) + " ) as " + key + " on " + key + "." + crossModuleAsAliasMap.get(key) + "= " + "inventory" + "." + CustomReportConstants.product + " ";
                            }
                        }
                        
                    }
                    if(joinMap.containsKey(getDetailsTableForMainTable(mainTable)) && !StringUtil.isNullOrEmptyWithTrim(crossModuleAsAliasMap.get(key)) && !(mainTable.equals("creditnote") || mainTable.equals("debitnote"))){
                        if (mainTable.equals("salesorder") && key.equals("purchaseorder")) {
                            joinString += "AND sodetails.purchaseorderdetailid = purchaseorder." + crossModuleAsAliasMap.get(key) + " OR sodetails.id = purchaseorder.salesorderdetailid ";
                        } else if (mainTable.equals("purchaseorder") && key.equals("salesorder")) {
                            joinString += "AND podetails.salesorderdetailid = salesorder." + crossModuleAsAliasMap.get(key) + " OR podetails.id = salesorder.purchaseorderdetailid ";
                        } else {
                            String crossmoduleFK = accCustomerReportServiceDao.getFKofCrossJoinMainTable(getDetailsTableForMainTable(key), getDetailsTableForMainTable(mainTable));
                            if (crossmoduleFK.equals("")) {
                                crossmoduleFK = "id";
                            }
                            if ((mainTable.equals("invoice") || mainTable.equals("goodsreceipt")) && (key.equals("product"))) { //ERP-38733 & ERP-38792
                                joinString += " and " + getDetailsTableForMainTable(mainTable) + ".id = " + "inventory" + "." + "id" + " ";
                            } else if(mainTable.equalsIgnoreCase(CustomReportConstants.Acc_Sales_Invoice) && key.equalsIgnoreCase(CustomReportConstants.PRODUCTCATEGORY) && joinMap.containsKey("inventory")){
                                joinString += " and " + getDetailsTableForMainTable(mainTable) + "." + crossmoduleFK + " = " + CustomReportConstants.inventory+"."+crossmoduleFK+" ";
                            }else {
                                joinString += " and " + getDetailsTableForMainTable(mainTable) + "." + crossmoduleFK + " = " + key + "." + crossModuleAsAliasMap.get(key) + " ";
                            }
                            if (((mainTable.equals("salesorder") || mainTable.equals("purchaseorder")) && (key.equals("salesorder") || key.equals("purchaseorder") || key.equals("invoice"))) || ((mainTable.equals("invoice") || mainTable.equals("goodsreceipt")) && (key.equals("deliveryorder") || key.equals("grorder")))) {
                                joinString += " or " + getDetailsTableForMainTable(mainTable) + ".id = " + key + "." + accCustomerReportServiceDao.getFKofCrossJoinMainTable(getDetailsTableForMainTable(mainTable), getDetailsTableForMainTable(key)) + " ";
                            }
                        }
                    }
                }
                if (!StringUtil.isNullOrEmpty(joinString)) {
                    joinMap.put(key, joinString);
                }
                mainTable = temp;
            }
        }

        if (!dataIndexList.isEmpty()) {
//            StringBuilder selectQuery = buildSelectQuery(dataIndexList, dataIndexListDup);
            StringBuilder selectQuery = buildCrossModuleSelectQuery(dataIndexList, dataIndexListDup, jArr);
            if (isDiscount && (moduleid.equals(String.valueOf(Constants.Acc_Invoice_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId)) ||moduleid.equals(String.valueOf(Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId)) ) ) {
                if (isCrossDiscount) {
                    selectQuery.append("," + table + ".discountispercent as `#discountispercent#`");
                } else {
                    selectQuery.append("," + table + ".inpercent as `#discountispercent#`");
                }
            } else if (isDiscount || isCrossDiscount) {
                if(CustomReportConstants.Acc_Sales_Invoice.equalsIgnoreCase(table)||CustomReportConstants.Acc_Vendor_Invoice.equalsIgnoreCase(table)) {
                    selectQuery.append("," + table + ".inpercent as `#discountispercent#`");
                } else {
                    selectQuery.append("," + table + ".discountispercent as `#discountispercent#`");
                }
                if (isCrossJoinLineItemPresent) {
                    selectQuery.append(lineItemDiscountSelectColumn);
                }
            }
            if (!StringUtil.isNullOrEmpty(crossJoinMainTable) && !CustomReportConstants.product.equalsIgnoreCase(crossJoinMainTable) && !CustomReportConstants.PRODUCTCATEGORY.equalsIgnoreCase(crossJoinMainTable)) {
                selectQuery.append("," + crossJoinMainTable + ".linkeddocid");
            }
            selectQuery.append("," + mainTable + ".id");
            StringBuilder joinQuery = buildJoinQuery(mainTable, joinMap);
//            StringBuilder neededJoinQuery = fetchDefaultFieldHeaderNeededMappings(mainTable, moduleid, joinMap ,cntVal);
//            joinQuery.append(neededJoinQuery);
            String companyID = (String) valueMap.get("companyID");

            List childCompanyList = accCompanyPreferencesObj.getMappedCompanies(companyID);  //fetching list of child companies

            if(!childCompanyList.isEmpty() && isPivot) {                    //checking if company has child-company or not
                String childCompanyListString = "'" +companyID + "'";
                for(Object obj: childCompanyList) {             //converting list if ID's into comma-separated string to append into query
                    Object[] obj1 = (Object[])obj;
                    childCompanyListString += ", '" + (String)obj1[0] + "'";
                }
                if (isDeleted) {
                    conditionsToQuery += " where " + mainTable.trim() + ".deleteflag='T' " + mainTable.trim() + ".company in (" + childCompanyListString + ")";
//                    paramList.add((String) valueMap.get("companyID"));
                } else {
                    conditionsToQuery += " where " + mainTable.trim() + ".company in (" + childCompanyListString + ")";
//                    paramList.add((String) valueMap.get("companyID"));
                }
            }else {
                if (isDeleted) {
                    conditionsToQuery += " where " + mainTable.trim() + ".deleteflag='T' " + mainTable.trim() + ".company = ?";
                    paramList.add((String) valueMap.get("companyID"));
                } else {
                    conditionsToQuery += " where " + mainTable.trim() + ".company = ? ";
                    paramList.add((String) valueMap.get("companyID"));
                }

            }
            if (!moduleid.equals(String.valueOf(Constants.Acc_FixedAssets_Sales_Return_ModuleId)) && !moduleid.equals(String.valueOf(Constants.Acc_FixedAssets_Purchase_Return_ModuleId)) && !moduleid.equals(String.valueOf(Constants.Acc_Sales_Return_ModuleId)) && !moduleid.equals(String.valueOf(Constants.Acc_Purchase_Return_ModuleId)) && !moduleid.equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))&& !moduleid.equals(String.valueOf(Constants.CUSTOMER_MODULE_UUID))) {
                if (isPendingapproval) {
                    paramList.add(11);
                    conditionsToQuery += " and " + mainTable.trim() + ".approvestatuslevel != ? ";
                } else {
                    paramList.add(11);
                    conditionsToQuery += " and " + mainTable.trim() + ".approvestatuslevel = ? ";
                }
            }

            if (!isLeaseSO && moduleid.equals(String.valueOf(Constants.Acc_Sales_Order_ModuleId))) {
                conditionsToQuery += " and ( " + mainTable.trim() + ".leaseOrMaintenanceSO=0 or " + mainTable.trim() + ".leaseOrMaintenanceSO=2) and "+mainTable.trim() +".istemplate != 2 and "+mainTable.trim() +".isdraft = 'false' ";
            }else if (moduleid.equals(String.valueOf(Constants.Acc_Purchase_Order_ModuleId))) {
                conditionsToQuery += " and ( " + mainTable.trim() + ".isfixedassetpo = false and " + mainTable.trim() + ".isconsignment='F' and " + mainTable.trim() + ".ismrpjobworkout = 'F' and "+mainTable.trim() +".istemplate != 2 ";
                if(!showExpenseTypeTransactionsFlag){
                    conditionsToQuery += " and "+ mainTable.trim() + ".isexpensetype = 'F' )";
                } else {
                    conditionsToQuery += " )";
                }
            }else if (moduleid.equals(String.valueOf(Constants.Acc_Sales_Return_ModuleId))) {
                conditionsToQuery += " and ( " + mainTable.trim() + ".isfixedasset = false and " + mainTable.trim() + ".isconsignment='F' and " + mainTable.trim() + ".isleasesalesreturn=false) ";
            }else if (moduleid.equals(String.valueOf(Constants.Acc_Purchase_Return_ModuleId))) {
                conditionsToQuery += " and ( " + mainTable.trim() + ".isfixedasset = false and " + mainTable.trim() + ".isconsignment='F' ) ";
            }else if (moduleid.equals(String.valueOf(Constants.Acc_Goods_Receipt_ModuleId))) {
                conditionsToQuery += " and ( " + mainTable.trim() + ".isfixedAssetgro = false and " + mainTable.trim() + ".isconsignment='F' ) ";
            }else if (moduleid.equals(String.valueOf(Constants.Acc_Delivery_Order_ModuleId))) {
                conditionsToQuery += " and ( " + mainTable.trim() + ".isfixedassetdo = false and " + mainTable.trim() + ".isleasedo = false and "+ mainTable.trim() +".isconsignment = 'F')";
            } else if (moduleid.equals(String.valueOf(Constants.Acc_Invoice_ModuleId))) {//check on deleteflag to exclude temporarily deleted Invoices
                conditionsToQuery +=  " and ( " + mainTable.trim() + ".isfixedassetinvoice = false and " + mainTable.trim() + ".isfixedassetleaseinvoice = false and " + mainTable.trim() + ".istemplate != 2 and "+ mainTable.trim() + ".isdraft = false and "+ mainTable.trim() +".isconsignment = 'F' and   "  + mainTable.trim() + ".deleteflag = 'F' ) ";
            }else if(moduleid.equals(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId))) {
                conditionsToQuery +=  " and ( " + mainTable.trim() + ".isfixedassetinvoice = false and "  + mainTable.trim() + ".istemplate != 2 and "+ mainTable.trim() +".isconsignment = 'F' ";
                if(!showExpenseTypeTransactionsFlag){
                    conditionsToQuery += " and "+ mainTable.trim() + ".isexpensetype = 'F' )";
                } else {
                    conditionsToQuery += " )";
                }
                if(!appendDistinct){
                    
                    if(!joinMap.containsKey("paydetail")){
                        joinQuery.append(" left join paydetail on paydetail.id = goodsreceipt.paydetail  ");
                    }
                    if(!joinMap.containsKey("paymentmethod")){
                        joinQuery.append(" left join paymentmethod on paymentmethod.id = paydetail.paymentMethod ");
                    }
                    if(!joinMap.containsKey("jedetail")){
                        joinQuery.append(" left join jedetail on journalentry.id = jedetail.journalEntry ");
                    }
                       conditionsToQuery += " and (jedetail.account = goodsreceipt.account  OR jedetail.account= paymentmethod.account) " ;
                       conditionsToQuery += " and jedetail.isseparated='F' ";                  
                }
            }else if (moduleid.equals(String.valueOf(Constants.Acc_Credit_Note_ModuleId))) {
                if(!appendDistinct){
                    // Added this for not including tax(4) as a account and excluding CustomerEntry Accounts.
                    if(!joinMap.containsKey("jedetail")){
                        joinQuery.append(" left join jedetail on journalentry.id = jedetail.journalEntry ");
                    }
                    if(!joinMap.containsKey("account")){
                        joinQuery.append(" left join account on account.id = jedetail.account ");
                    }
                    conditionsToQuery += " and account.mastertypeid !=4 and creditnote.centry!=jedetail.id " ;
                    conditionsToQuery += " and jedetail.isseparated='F' and jedetail.debit='T'  " ;
                }
            }else if (moduleid.equals(String.valueOf(Constants.Acc_Debit_Note_ModuleId))) {
                if(!appendDistinct){
                    if(!joinMap.containsKey("jedetail")){
                        joinQuery.append(" left join jedetail on journalentry.id = jedetail.journalEntry ");
                    }
                    if(!joinMap.containsKey("account")){
                        joinQuery.append(" left join account on account.id = jedetail.account ");
                    }
                    // Added this for not including tax(4) as a account and excluding VendorEntry Accounts.
                    conditionsToQuery += " and account.mastertypeid !=4 and debitnote.centry!=jedetail.id " ;
//                    conditionsToQuery += " and jedetail.isseparated='F' and jedetail.debit='F'  " ;
                }
            }
            else if (moduleid.equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId))) {
//                conditionsToQuery += " and ( jedetail.debit='T' ) ";
                conditionsToQuery += "  ";
            } else if (moduleid.equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {//to exclude GL account Receipts and to exclude temporarily deleted Receipts
                conditionsToQuery += " and  "  + mainTable.trim() + ".deleteflag = 'F' ";
                if (!showGLFlag) {
                    conditionsToQuery += " and  "  + mainTable.trim() + ".paymentwindowtype != 3";
                }
            }
            // Added as fix for ERP-36114
            else if (moduleid.equals(String.valueOf(Constants.Acc_Customer_Quotation_ModuleId))) {
                conditionsToQuery += " and  "  + mainTable.trim() + ".istemplate != 2 and " + mainTable.trim() + ".archieve = 0 and " + mainTable.trim() + ".isleasequotation = 0 and " + mainTable.trim() + ".isdraft = false " ;
            }else if(moduleid.equals(String.valueOf(Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId))){
                conditionsToQuery += " and "  + mainTable.trim() + ".isfixedassetinvoice='1'";
            }else if(moduleid.equals(String.valueOf(Constants.Acc_FixedAssets_DisposalInvoice_ModuleId))){
                conditionsToQuery += " and "  + mainTable.trim() + ".isfixedassetinvoice='1'";
            } else if( moduleid.equals(String.valueOf(Constants.Acc_FixedAssets_Sales_Return_ModuleId))){
                conditionsToQuery += " and ( " + mainTable.trim() + ".isfixedasset = true and " + mainTable.trim() + ".isconsignment='F' and " + mainTable.trim() + ".isleasesalesreturn=false) ";
            } else if( moduleid.equals(String.valueOf(Constants.Acc_FixedAssets_Purchase_Return_ModuleId))){
                conditionsToQuery += " and ( " + mainTable.trim() + ".isfixedasset = true and " + mainTable.trim() + ".isconsignment='F' ) ";
            }

            if(moduleid.equals(Constants.CUSTOMER_MODULE_UUID)){

                if(customerShippingAddr==false && customerBillingAddr==true){
                     conditionsToQuery +=" and ( isbillingaddress is null  OR isbillingaddress='T') ";
                }else if(customerShippingAddr==true && customerBillingAddr==false){
                     conditionsToQuery +="and ( isbillingaddress is null  OR isbillingaddress='F')";
                }
            }
            if(moduleid.equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId))){
                if(billingAddrForPayment == true){
                     conditionsToQuery +=" and ( vendoraddressdetails.isbillingaddress='T' or customeraddressdetails.isbillingaddress='T') ";
                }
            }

            if (valueMap.containsKey("billid") && valueMap.get("billid") != null) {
                billid = (String) valueMap.get("billid");
                conditionsToQuery += " and " + mainTable.trim() + ".id in (" + billid + ")";
            }
            
            if (!StringUtil.isNullOrEmpty(crossJoinMainTable)) {
                if (valueMap.containsKey("linkedbillid") && valueMap.get("linkedbillid") != null) {
                    linkedDocBillId = (String) valueMap.get("linkedbillid");
                    if (moduleid.equals(String.valueOf(Constants.Acc_Credit_Note_ModuleId)) || (moduleid.equals(String.valueOf(Constants.Acc_Debit_Note_ModuleId))) || (moduleid.equals(String.valueOf(Constants.Acc_Sales_Order_ModuleId))) || (moduleid.equals(String.valueOf(Constants.Acc_Purchase_Order_ModuleId))) || (moduleid.equals(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId))) ||(moduleid.equals(String.valueOf(Constants.Acc_Invoice_ModuleId))) ) {
                        conditionsToQuery += " and " + crossJoinMainTable.trim() + ".linkeddocid in (" + linkedDocBillId + ") ";
                    }
                }
            }//&& !mainTable.equals("goodsreceipt") condition to be avoided in goodsreceipt
            if (joinMap.containsKey("jedetail") && !mainTable.equals("receipt") && !mainTable.equals("creditnote")) {
                conditionsToQuery += " and jedetail.isseparated='F' and jedetail.debit='F'  ";
            }
            if (!StringUtil.isNullOrEmptyWithTrim(eWayFilter) && !eWayFilter.equals("All")) {
                conditionsToQuery += " and " + mainTable + ".isewayexported='" + eWayFilter + "' ";
            }
            if (valueMap.containsKey("showRowLevelFieldsflag") && !(Boolean) valueMap.get("showRowLevelFieldsflag")) {
                finalQuery = (appendDistinct ? "select distinct " : "select ") + selectQuery.append(joinQuery) + conditionsToQuery;
            } else {
                if (moduleid.equals(String.valueOf(Constants.Acc_Debit_Note_ModuleId)) && mainTable.equalsIgnoreCase(CustomReportConstants.Acc_Debit_Note)) {
                    if (valueMap.containsKey("showRowLevelFieldsflag") && !(Boolean) valueMap.get("showRowLevelFieldsflag")) {
                        conditionsToQuery += " and " + mainTable.trim() + ".centry !=" + "accjedetailcustomdata.jedetailId and account.mastertypeid != 4";
                    }
                }

//                if (moduleid.equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_Credit_Note_ModuleId)) || (moduleid.equals(String.valueOf(Constants.Acc_Debit_Note_ModuleId)))) {
//                    finalQuery = "select " + selectQuery.append(joinQuery) + conditionsToQuery;
//                } else {
                finalQuery = (appendDistinct ? "select distinct " : "select ") + selectQuery.append(joinQuery) + conditionsToQuery;
//                }
            }
            if (isGSTField) {
                applyGrouping=false;
                if (moduleid.equals(String.valueOf(Constants.Acc_Invoice_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_FixedAssets_DisposalInvoice_ModuleId))) {
                    groupByClause = " group by invoicedetails.id ";
                } else if (moduleid.equals(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId))) {
                    groupByClause = " group by grdetails.id ";
                } else if (moduleid.equals(String.valueOf(Constants.Acc_Purchase_Return_ModuleId))|| moduleid.equals(String.valueOf(Constants.Acc_Purchase_Return_ModuleId))) {
                    groupByClause = " group by prdetails.id ";
                } else if (moduleid.equals(String.valueOf(Constants.Acc_Sales_Return_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_FixedAssets_Sales_Return_ModuleId))) {
                    groupByClause = " group by srdetails.id ";
                } else if (moduleid.equals(String.valueOf(Constants.Acc_Delivery_Order_ModuleId))) {
                    groupByClause = " group by dodetails.id ";
                } else if (moduleid.equals(String.valueOf(Constants.Acc_Goods_Receipt_ModuleId))) {
                    groupByClause = " group by grodetails.id ";
                } else if (moduleid.equals(String.valueOf(Constants.Acc_Sales_Order_ModuleId))) {
                    groupByClause = " group by sodetails.id ";
                } else if (moduleid.equals(String.valueOf(Constants.Acc_Purchase_Order_ModuleId))) {
                    groupByClause = " group by podetails.id ";
                } else if (moduleid.equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId))) {
                    groupByClause = " group by advancedetail.id ";
                } else if (moduleid.equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {
                    groupByClause = " group by receiptadvancedetail.id ";
                }
            }
            if(applyGrouping){
                groupByClause = " group by "+paymentGroupingTable+".id ";
                
            }
            countQuery = (appendDistinct? "select distinct ":"select ")+mainTable.trim()+".id as total " + joinQuery + conditionsToQuery;
            selectCountQuery=(appendDistinct? "select distinct ":"select ")+mainTable.trim()+".id as total " + joinQuery;// Added to recreate countquery same as reportquery in Advance Search query 
            queryMap.put(CustomReportConstants.sqlquery, finalQuery);
            queryMap.put(CustomReportConstants.JOINQUERY, joinQuery);
            queryMap.put(CustomReportConstants.SELECTQUERY, (appendDistinct? "select distinct ":"select ") + selectQuery);
            queryMap.put(CustomReportConstants.CONDITIONQUERY, conditionsToQuery);
            queryMap.put(CustomReportConstants.GROUPBYCLAUSE, groupByClause);
            queryMap.put("selectCountQuery",selectCountQuery);
            queryMap.put(CustomReportConstants.countquery, countQuery);
            queryMap.put("JoinMapKeySet",joinMap.keySet());
            queryMap.put("cntVal", cntVal);
            if (valueMap.containsKey("userId") && valueMap.get("userId") != null) {
                queryMap.put("userId", valueMap.get("userId"));
                
            }
//            System.out.println("finalQuery = " + finalQuery);

//            queryMap.put(CustomReportConstants.countquery, countQuery);
        }
        return queryMap;
    }

    public  JSONArray reshuffledSelectedField(JSONArray selectedRowsJSON1) {
        JSONArray newSelectedRecords = new JSONArray();
        try {
            List measureList = new LinkedList();
            List lineList = new LinkedList();
            List remainingFields = new LinkedList();

            for (int iterateSelected = 0; iterateSelected < selectedRowsJSON1.length(); iterateSelected++) {
                JSONObject jo = selectedRowsJSON1.getJSONObject(iterateSelected);
                if (jo.optBoolean("allowcrossmodule", false) && jo.optBoolean("isMeasureItem", false)) {
                    measureList.add((JSONObject) selectedRowsJSON1.get(iterateSelected));
                } else if (jo.optBoolean("allowcrossmodule", false) && (jo.optBoolean("customfield", false) || jo.optBoolean("custom", false))) {
                    newSelectedRecords.put((JSONObject) selectedRowsJSON1.get(iterateSelected));
                } else if (jo.optBoolean("allowcrossmodule", false) && jo.optBoolean("isLineItem", false)) {
                    lineList.add((JSONObject) selectedRowsJSON1.get(iterateSelected));
                } else {
                    remainingFields.add(jo);
                }

            }

            if (!remainingFields.isEmpty()) {
                for (int i = 0; i <= remainingFields.size() - 1; i++) {
                    newSelectedRecords.put(remainingFields.get(i));
                }
            }
            if (!lineList.isEmpty()) {
                for (int i = 0; i <= lineList.size() - 1; i++) {
                    newSelectedRecords.put(lineList.get(i));
                }
            }
            if (!measureList.isEmpty()) {
                for (int i = 0; i <= measureList.size() - 1; i++) {
                    newSelectedRecords.put(measureList.get(i));
                }
            }
            measureList = null;
            lineList = null;
            remainingFields = null;
//            selectedRowsJSON = newSelectedRecords;

        } catch (JSONException ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return newSelectedRecords;
    }

    /**
     * Returns Map of chart specific queries
     * 
     * @param moduleId
     * @param isTotalCount
     * @param requestParam
     * @param paramList
     * @return queryMap
     * @throws ServiceException
     * @throws JSONException
     */
    
    public Map getCustomChartSqlQuery(String moduleId, boolean isTotalCount, Map requestParam, ArrayList paramList) throws ServiceException, JSONException {
        Map<String, Object> queryMap = new HashMap<String, Object>();
        try {
            String titleField = requestParam.get("titleField") != null ? (String) requestParam.get("titleField") : "{}";
            String valueField = requestParam.get("valueField") != null ? (String) requestParam.get("valueField") : "{}";
            JSONObject titleJobj = new JSONObject(titleField);
            JSONObject valueJobj = new JSONObject(valueField);
            
            JSONArray jarrColumns = new JSONArray();
            jarrColumns.put(titleJobj);
            jarrColumns.put(valueJobj);
            
            queryMap = buildCustomReportSqlQuery(jarrColumns, moduleId, false, requestParam, paramList,new JSONObject());
            
            if(titleJobj.optBoolean("isLineItem") || valueJobj.optBoolean("isLineItem")) {
                if (queryMap.get(CustomReportConstants.SELECTQUERY) != null && queryMap.get(CustomReportConstants.sqlquery) != null) {
                    String selectQuery = String.valueOf(queryMap.get(CustomReportConstants.SELECTQUERY));
                    String sqlQuery = String.valueOf(queryMap.get(CustomReportConstants.sqlquery));
                    
                    queryMap.put(CustomReportConstants.SELECTQUERY, selectQuery.replace("distinct", ""));
                    queryMap.put(CustomReportConstants.sqlquery, sqlQuery.replace("distinct", ""));
                }
            }
            
        } catch (Exception ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return queryMap;
    }

    /**
     * This method builds the query for chart using report query
     *
     * @param requestParam
     * @param jarrColumns
     * @param reportSQLQuery
     * @return String
     * @throws ServiceException
     * @throws JSONException
     */
    public String buildCustomReportChartSqlQuery(Map<String, Object> requestParam, JSONArray jarrColumns, String reportSQLQuery) throws ServiceException, JSONException {
        String chartSQLQuery = "";
        try {
            String titleField = requestParam.get("titleField") != null ? (String) requestParam.get("titleField") : "{}";
            String valueField = requestParam.get("valueField") != null ? (String) requestParam.get("valueField") : "{}";
            String groupingFn = requestParam.get("groupby") != null ? (String) requestParam.get("groupby") : "";
            JSONObject titleJobj = new JSONObject(titleField);
            JSONObject valueJobj = new JSONObject(valueField);
            JSONObject tempJobj = null;
            String columnLabel = "";
            String chartSelectQuery = "SELECT ";
            String groupBy = "";
            String orderBy = "";
            for (int i = 0; i < jarrColumns.length(); i++) {
                tempJobj = jarrColumns.optJSONObject(i);
                columnLabel = tempJobj.optString("defaultHeader");

                if (tempJobj.optString("id").equalsIgnoreCase(titleJobj.optString("id"))) {
                    chartSelectQuery += "`#" + columnLabel + "#`,";
                    groupBy = " GROUP BY " + "`#" + columnLabel + "#`";
                }

                if (tempJobj.optString("id").equalsIgnoreCase(valueJobj.optString("id"))) {
                    chartSelectQuery += groupingFn + "(`#" + columnLabel + "#`) " + "`#" + columnLabel + "#`" + ",";
                    orderBy = " ORDER BY `#" + columnLabel + "#` DESC ";
                }
            }

            chartSelectQuery = chartSelectQuery.substring(0, chartSelectQuery.length() - 1);
            String subQuery = " FROM ( " + reportSQLQuery + " ) AS temp ";
            
            //Number of data rows used to build chart
            String limitQuery  = " LIMIT " + CustomReportConstants.CHART_DATA_LIMIT;
            
            chartSQLQuery = chartSelectQuery + subQuery + groupBy + orderBy + limitQuery;
        } catch (Exception ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return chartSQLQuery;
    }
    /**
     * This method returns the select clause while building the query
     *
     * @param dataIndexList
     * @return selectQuery
     *
     */
    public static StringBuilder buildSelectQuery(LinkedHashMap<String, String> dataIndexList, LinkedHashMap<String, ArrayList<String>> dataIndexListDup) {
        StringBuilder selectQuery = new StringBuilder();
        Set<String> keys = dataIndexList.keySet();
        Set<String> dupKeys = dataIndexListDup.keySet();
        boolean isFirst = true;
        for (String fieldname : keys) {
            {

                if (isFirst) {
                    selectQuery.append(" ").append(fieldname).append(" as `").append(dataIndexList.get(fieldname)).append("` ");
                    isFirst = false;
                } else {
                    selectQuery.append(", ").append(fieldname).append(" as `").append(dataIndexList.get(fieldname)).append("` ");
                }
            }
        }
        for (String fieldname : dupKeys) {
            ArrayList<String> listEntry = dataIndexListDup.get(fieldname);
            for (int dupCnt = 0; dupCnt < listEntry.size(); dupCnt++) {
                selectQuery.append(", ").append(fieldname).append(" as `").append(listEntry.get(dupCnt)).append("` ");
            }
        }
        return selectQuery;
    }

    /**
     * This method returns the join clause while building the query
     *
     * @param mainTable
     * @param joinMap
     * @return joinQuery
     *
     */
    public static StringBuilder buildJoinQuery(String mainTable, HashMap<String, String> joinMap) {
        StringBuilder joinQuery = new StringBuilder();
        joinQuery.append(" from ").append(mainTable).append(" ");
        Set<String> keys = joinMap.keySet();
        for (String fieldname : keys) {
            if (!StringUtil.isNullOrEmpty(fieldname)) {
                joinQuery.append(joinMap.get(fieldname));
            }
        }
        return joinQuery;
    }

    /**
     * Get the list of reports created by the user
     *
     * @param moduleID
     * @param companyID
     * @param userId
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    @Override
    public JSONObject getCustomReportList(JSONObject paramJobj, DateFormat df) throws ServiceException, SessionExpiredException {
        JSONObject jresult = new JSONObject();
        try {

            String companyID = paramJobj.optString(Constants.companyKey);
            String filter = paramJobj.optString("filter", "[]");
            String browsertz = paramJobj.optString(Constants.browsertz);

            JSONArray filterarray = new JSONArray(filter);
            String filterQuery = "";
            if (filterarray.length() > 0) {
                for (int i = 0; i < filterarray.length(); i++) {
                    JSONObject filterObj = filterarray.getJSONObject(i);
                    String filterColumn = filterObj.optString(CustomReportConstants.FILTER_PROPERTY, "");
                    String operator = filterObj.optString(CustomReportConstants.FILTER_OPERATOR, "");
                    String value = filterObj.optString(CustomReportConstants.FILTER_VALUE, "");
                    String end_value = "";

                    if (filterColumn.equalsIgnoreCase("reportmaster.createdon") || filterColumn.equalsIgnoreCase("reportmaster.updatedon")) {
                        value += " 00:00:00";
                        end_value = value.replace("00:00:00", "23:59:59");
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        Date startD = formatter.parse(value);
                        Date endD = formatter.parse(end_value);
                        value = String.valueOf(startD.getTime());
                        end_value = "'" + String.valueOf(endD.getTime()) + "'";                 //If filter is applied on "On" condition then calculate long value from date +"00:00:00" to date+"23:59:00"

                        if (operator.equalsIgnoreCase("gt")) {
                            value = String.valueOf(endD.getTime());
                        }
                    }

                    if (!StringUtil.isNullOrEmpty(filterColumn) && !StringUtil.isNullOrEmpty(operator) && !StringUtil.isNullOrEmpty(value) && !value.equals("[]")) {
                        if (operator.equalsIgnoreCase(CustomReportConstants.FILTER_gt)) {
                            operator = CustomReportConstants.FILTER_gtSymbol;
                            value = "'" + value + "'";
                        } else if (operator.equalsIgnoreCase(CustomReportConstants.FILTER_lt)) {
                            operator = CustomReportConstants.FILTER_ltSymbol;
                            value = "'" + value + "'";
                        } else if (operator.equalsIgnoreCase(CustomReportConstants.FILTER_eq)) {
                            operator = CustomReportConstants.FILTER_eqSymbol;
                            value = "'" + value + "'";
                        } else if (operator.equalsIgnoreCase(CustomReportConstants.FILTER_like)) {
                            value = "'%" + value + "%'";
                        } else if (operator.equalsIgnoreCase(CustomReportConstants.FILTER_in)) {
                            value = value.replace("[", "(");
                            value = value.replace("]", ")");
                        }

                        if ((filterColumn.equalsIgnoreCase("reportmaster.createdon") || filterColumn.equalsIgnoreCase("reportmaster.updatedon")) && operator.equalsIgnoreCase(CustomReportConstants.FILTER_eqSymbol)) {
                            filterQuery += " and (" + filterColumn + " BETWEEN " + value + " and " + end_value + " )";      //If filter is applied on "On" condition then calculate long value from date +"00:00:00" to date+"23:59:00"
                        } else {
                            filterQuery += " and (" + filterColumn + " " + operator + " " + value + ")";
                        }

                    }
                }
            }
            //List<Customreports> list = null;
            paramJobj.put("filterQuery",filterQuery);
            KwlReturnObject result = accCustomerReportServiceDao.getCustomReportList(paramJobj);
            //list = result.getEntityList();
            List list = result.getEntityList();
            Iterator itr = list.iterator();
            try {
                //            for (Customreports customreport : list) {
                //                JSONObject jobj = new JSONObject();
                //                jobj.put("id", customreport.getReportno());
                //                jobj.put("name", customreport.getReportname());
                //                jobj.put("reportjson", customreport.getReportjson());
                //                jobj.put("reportsql", customreport.getReportsql());
                //                jobj.put("moduleCat", customreport.getReportmodulecategory().getModuleCatId());
                //                jobj.put("moduleId", customreport.getReportmodule().getId());
                //                jobj.put("reportdesc", customreport.getReportdescription());
                //                jresult.append("data", jobj);
                //            }
                while (itr.hasNext()) {
                    JSONObject jobj = new JSONObject();
                    Object[] obj = (Object[]) itr.next();
                    jobj.put("id", obj[0]);
                    jobj.put("name", obj[1]);
                    jobj.put("description", obj[2]);
                    jobj.put("reportjson", obj[3]);
                    jobj.put("reportsql", obj[4]);
                    jobj.put("modulecategory.modulecatname", obj[5]);
                    jobj.put("moduleId", obj[6]);
                    jobj.put("modules.modulename", obj[7]);
                    if (browsertz != null && !StringUtil.isNullOrEmpty(browsertz)) {
                        df.setTimeZone(TimeZone.getTimeZone("GMT" + browsertz));
                    }
                    if (obj[8] != null) {
                        long dateValueLong = ((BigInteger) obj[8]).longValue();
                        jobj.put("reportmaster.createdon", df.format(new java.util.Date(dateValueLong)));
                    } else {
                        jobj.put("reportmaster.createdon", obj[8]);
                    }
                    if (obj[9] != null) {
                        long dateValueLong = ((BigInteger) obj[9]).longValue();
                        jobj.put("reportmaster.updatedon", df.format(new java.util.Date(dateValueLong)));
                    } else {
                        jobj.put("reportmaster.updatedon", obj[9]);
                    }
                    jobj.put("isPivot", obj[10]);
                    jobj.put("isDefault",obj[11]);
//                    jobj.put("addedTowidget",obj[12]);
                    jobj.put("reportUrl", obj[12]);
                    jobj.put("parentreportid", obj[13]);
                    jobj.put("createdby", obj[14]);
                    jobj.put("filterJson", obj[15]);
                    jobj.put("savedSearch", obj[16]);
                    jobj.put("filterAppend", obj[17]);
                    jobj.put("savedSearchId", obj[18]);
                    jobj.put("isEWayReport", obj[19]);
                    jobj.put("isShowasQuickLinks", (obj !=null && obj.length>20 && obj[20] != null) ? obj[20] : "F"); // Show Custom build report in Statutory as Quick links

                    jobj.put("charts", getChartDetails(new JSONObject().put("reportId", obj[0])).optJSONArray("data"));
                    jresult.append("data", jobj);
                }

                getCompanyExtraPreferences(jresult,paramJobj);

                jresult.put("totalCount", result.getRecordTotalCount());
            } catch (JSONException ex) {
                Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (ParseException ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jresult;
    }

    private void getCompanyExtraPreferences(JSONObject jresult, JSONObject paramJobj)throws ServiceException,SessionExpiredException {
        ExtraCompanyPreferences extrapref = null;
        boolean showPivotInCustomReports = false;
        try {
            String companyID = paramJobj.optString(Constants.companyKey);
            String userid = paramJobj.optString(Constants.userid);

            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyID);
            if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                showPivotInCustomReports = extrapref.isShowPivotInCustomReports();
            }

            jresult.put("countryid",paramJobj.opt("countryid"));
            JSONObject userPref = getFinancialYear(companyID);

            jresult.put("showPivotInCustomReports", showPivotInCustomReports);
            jresult.put("userPref", userPref);

            JSONObject rjobj = new JSONObject();
            JSONObject ujobj = new JSONObject();
            KwlReturnObject kmsg = null;
            JSONObject roleJson = new JSONObject();
            /**
             * ERP-ERP-41687 Change in Method parameter Old Parameter companyid
             * new parameter JSONObject.
             */
            kmsg = permissionHandlerDAOObj.getRoleList(paramJobj);
            Iterator ite = kmsg.getEntityList().iterator();
            int inc = 0;
            while (ite.hasNext()) {
                Object row = (Object) ite.next();
                String rname = ((Rolelist) row).getRolename();
                rjobj.put(rname, (int) Math.pow(2, inc));
                inc++;
            }
            kmsg = permissionHandlerDAOObj.getRoleofUser(userid);
            ite = kmsg.getEntityList().iterator();
            if (ite.hasNext()) {
                Object[] row = (Object[]) ite.next();
                ujobj.put("roleid", row[0].toString());
            }
            roleJson.put("Role", rjobj);
            roleJson.put("URole", ujobj);
            jresult.put("role", roleJson);
        } catch (JSONException | ParseException ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private JSONObject getFinancialYear(String companyid) throws ServiceException, SessionExpiredException, ParseException, JSONException {
        JSONObject userPref = new JSONObject();

        String fromdate = "";
        String todate = "";
        Date toDate = new Date();
        DateFormat df1 = authHandler.getOnlyDateFormat();
        Map<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("id", companyid);
        KwlReturnObject result = accCompanyPreferencesObj.getCompanyPreferences(requestParams);
        CompanyAccountPreferences pref = (CompanyAccountPreferences) result.getEntityList().get(0);
        Date financialYear = pref.getFinancialYearFrom();
//        requestParam.put("companyAccPref", pref);


        Date currentDate = new Date();

        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);
        int currentyear = cal.get(Calendar.YEAR);

        cal.setTime(financialYear);
        int financialmonth = cal.get(Calendar.MONTH);
        int financialdate = cal.get(Calendar.DATE);

        String financialYearMonth = String.valueOf(financialmonth).length() == 1 ? "0" + String.valueOf(financialmonth + 1) : String.valueOf(financialmonth + 1);
        String monthDateStr = financialYearMonth + "-" + financialdate;
        Date fd = df1.parse(currentyear + "-" + monthDateStr);
        if (currentDate.compareTo(fd) < 0) {
            fd = df1.parse((currentyear - 1) + "-" + monthDateStr);
        }
        financialYear = fd;
        fromdate = df1.format(financialYear);
        cal.setTime(financialYear);
        cal.add(Calendar.YEAR, 1);
        cal.add(Calendar.DATE, -1);
        toDate = cal.getTime();
        todate = df1.format(toDate);

        userPref.put("fromdate", fromdate);
        userPref.put("todate", todate);
        return userPref;
    }

    /**
     * Execute the selected report
     *
     * @param requestParam
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     * @throws com.krawler.utils.json.base.JSONException
     */
    @Override
    public JSONObject executeCustomReport(Map<String, Object> requestParam) throws ServiceException, ParseException,IOException,SessionExpiredException{

        JSONObject jresult = new JSONObject();
        Map<String, Object> jsonArrayMap = new HashMap<String, Object>();
        String reportSQLQuery = null;
        String countSQLQuery = null;
        String fromdate = "";
        String todate = "";
        Date toDate = new Date();
        String mainTable = null;
        int totalCount = 0;
        String reportJSON = null,filterJSON=null;
        boolean isPivot = false;
        JSONObject commData = new JSONObject();
        JSONObject jMeta = new JSONObject();
        JSONObject reportJSONObj = new JSONObject();
        JSONArray jarrColumns = new JSONArray();
        JSONArray returnJArray = new JSONArray();
        JSONArray dataJArr = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jarrRecordsObject = new JSONObject();
        JSONObject userPreferences = new JSONObject();
        JSONObject dataIndexObject = new JSONObject();
        Map<String, Object> queryMap = new HashMap<String, Object>();
        ArrayList paramList = new ArrayList();
        String currencyid = (String) requestParam.get("gcurrencyid");
        boolean isCustomWidgetReport = requestParam.get("isCustomWidgetReport") !=null ?(Boolean) requestParam.get("isCustomWidgetReport"):false;
        boolean isChartRequest = requestParam.get("isChartRequest") != null ? (Boolean) requestParam.get("isChartRequest") : false;
        boolean executeQuery = true,isclearfilter=false;
        boolean forExport= requestParam.get("forExport") == null ? false : (Boolean) requestParam.get("forExport");
        boolean exportInCaps= requestParam.get("exportInCaps") == null ? false : (Boolean) requestParam.get("exportInCaps");
        boolean isWholeData= requestParam.get("isWholeData") == null ? false : (Boolean) requestParam.get("isWholeData");

        ReportMaster customreport = accCustomerReportServiceDao.fetchCustomReportDetails((String) requestParam.get("reportID"));
        boolean isDiscountPresent = false, isLineItem = false,isAnyQunatityPresent = false,isQtyLineItem = false,isDisplayUOMPresent=false;
        String dataIndexTableName = "";

        HashMap<String, String> columnmap = new HashMap<String, String>();
        JSONObject properties = new JSONObject();

        if (customreport != null) {
            reportSQLQuery = customreport.getReportsql();
            reportJSON = customreport.getReportjson();
            filterJSON = customreport.getFilterjson();
            isPivot = customreport.isIspivot();
            requestParam.put("isPivot", isPivot);
            String moduleId = customreport.getModuleid();
            boolean hasUOM =false;
            DateFormat df1 = (DateFormat) requestParam.get("df1");
            String start = String.valueOf(requestParam.get("start"));
            String limit = String.valueOf(requestParam.get("limit"));
            boolean showRowLevelFieldsflag = (requestParam.containsKey("showRowLevelFieldsflag") && requestParam.get("showRowLevelFieldsflag") != null) ? (Boolean) requestParam.get("showRowLevelFieldsflag") : false;

            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("id", requestParam.get("companyID"));
            requestParam.put(Constants.moduleid,moduleId);

            KwlReturnObject result = accCompanyPreferencesObj.getCompanyPreferences(requestParams);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) result.getEntityList().get(0);
            Date financialYear = pref.getFinancialYearFrom();
            requestParam.put("companyAccPref",pref);

            if (!StringUtil.isNullOrEmpty((String) requestParam.get("fromDate")) && !StringUtil.isNullOrEmpty((String) requestParam.get("toDate"))) {
                fromdate = (String) requestParam.get("fromDate");
                todate = (String) requestParam.get("toDate");
            } else {
                Date currentDate = new Date();

                Calendar cal = Calendar.getInstance();
                cal.setTime(currentDate);
                int currentyear = cal.get(Calendar.YEAR);

                cal.setTime(financialYear);
                int financialmonth = cal.get(Calendar.MONTH);
                int financialdate = cal.get(Calendar.DATE);

                String financialYearMonth = String.valueOf(financialmonth).length() == 1 ? "0" + String.valueOf(financialmonth + 1) : String.valueOf(financialmonth + 1);
                String monthDateStr = financialYearMonth + "-" + financialdate;
                Date fd = df1.parse(currentyear + "-" + monthDateStr);
                if (currentDate.compareTo(fd) < 0) {
                    fd = df1.parse((currentyear - 1) + "-" + monthDateStr);
                }
                financialYear = fd;
                fromdate = df1.format(financialYear);
                cal.setTime(financialYear);
                cal.add(Calendar.YEAR, 1);
                cal.add(Calendar.DATE, -1);
                toDate = cal.getTime();
                todate = df1.format(toDate);
            }
            try {
                JSONArray filterarray = new JSONArray((String) requestParam.get("filter"));
                boolean isreportloaded=false;
                if (requestParam.get("isreportloaded") != null) {
                    isreportloaded = (Boolean) requestParam.get("isreportloaded");
                }
                if (requestParam.get("isclearfilter") != null) {
                    isclearfilter = (Boolean) requestParam.get("isclearfilter");
                }
                if(filterarray.length()==0 && !StringUtil.isNullOrEmpty(filterJSON) && isreportloaded){//reportloaded when clicking on reportname
                   filterarray=new JSONArray(filterJSON);
                } 
                if(isclearfilter && isreportloaded){
                    filterarray=new JSONArray();
                } else if(filterarray.length()> 0 && !StringUtil.isNullOrEmpty(filterJSON) && isreportloaded){
                   JSONArray storedFilterarray = new JSONArray(filterJSON);
                   filterarray = concatJSONArrayWithoutDuplicate(filterarray,storedFilterarray);
                            }
                JSONArray reportJSONArray = new JSONArray();
                reportJSONObj = new JSONObject(reportJSON);
                reportJSONArray = reportJSONObj.optJSONArray("columnConfig");
                if (StringUtil.equal(customreport.getModuleid(), String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {
                    requestParam.put("showGLFlag", reportJSONObj.get("showGLFlag"));
                } else if (StringUtil.equal(customreport.getModuleid(), String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId)) || StringUtil.equal(customreport.getModuleid(), String.valueOf(Constants.Acc_Purchase_Order_ModuleId))) {
                    requestParam.put("showExpenseTypeTransactionsFlag", reportJSONObj.optBoolean("showExpenseTypeTransactionsFlag",false));
                }
                jsonArrayMap = showRowLevelFieldsJSONArray(reportJSONArray, showRowLevelFieldsflag);

                if (jsonArrayMap.containsKey("jarrColumns")) {
                    reportJSONArray = (JSONArray) jsonArrayMap.get("jarrColumns");
                }

                JSONArray measureFieldsJsonArray = new JSONArray();
                JSONArray jarrColumnsIncludingMeasureFields = new JSONArray();
                for (int cnt = 0; cnt < reportJSONArray.length(); cnt++) {
                    JSONObject jObj = reportJSONArray.getJSONObject(cnt);
                    Map returnMap = buildcolumnDetailsCustomReport(jObj, columnmap, cnt, dataIndexObject, jarrColumnsIncludingMeasureFields, properties, filterarray, String.valueOf(moduleId), dataIndexTableName, isDiscountPresent, isLineItem, isAnyQunatityPresent, isQtyLineItem,isDisplayUOMPresent);
                    cnt = (Integer) returnMap.get("cnt");
                    isLineItem = (Boolean) returnMap.get("isLineItem");
                    isDiscountPresent = (Boolean) returnMap.get("isDiscountPresent");
                    isQtyLineItem = (Boolean) returnMap.get("isQtyLineItem");
                    isAnyQunatityPresent = (Boolean) returnMap.get("isAnyQunatityPresent");
                    isDisplayUOMPresent= (Boolean) returnMap.get("isDisplayUOMPresent");
                    dataIndexObject = (JSONObject) returnMap.get("dataIndexObject");
                    dataIndexTableName = (String) returnMap.get("dataIndexTableName");
                    jarrColumnsIncludingMeasureFields = (JSONArray) returnMap.get("jarrColumns");
                    columnmap = (HashMap<String, String>) returnMap.get("columnmap");
                    properties = (JSONObject) returnMap.get("properties");
                    if(jObj.optString("defaultHeader").startsWith("UOM")){
                        hasUOM = true;
                    }

                    if (jObj.optBoolean(Constants.isforformulabuilder, false) == true) {
                        measureFieldsJsonArray.put(jObj);
                    }
                }

                for (int z = 0; z < jarrColumnsIncludingMeasureFields.length(); z++) {
                    JSONObject jObjTemp = jarrColumnsIncludingMeasureFields.getJSONObject(z);
                    if (jObjTemp.optBoolean(Constants.isforformulabuilder,false)==false) {
                        jarrColumns.put(jObjTemp);
                    }
                }

                if (isDiscountPresent && isLineItem) {
                    JSONObject jobjTemp1 = new JSONObject();
                    jobjTemp1.put("defaultHeader", "discountispercent");
                    jobjTemp1.put("displayName", "Discountispersent");
                    jobjTemp1.put("dataIndex", "discountispercent");
                    jobjTemp1.put("dataIndexTableName", dataIndexTableName);
                    jobjTemp1.put("width", 150);
                    jobjTemp1.put("pdfwidth", 150);
                    jobjTemp1.put("isLineItem", isLineItem);
                    jobjTemp1.put("xtype", "1");
                    jobjTemp1.put("properties", properties);
                    jobjTemp1.put("custom", "false");
                    jarrColumns.put(jobjTemp1);
                    if (moduleId.equals(String.valueOf(Constants.Acc_Sales_Order_ModuleId)) || moduleId.equals(String.valueOf(Constants.Acc_Purchase_Order_ModuleId)) || moduleId.equals(String.valueOf(Constants.Acc_Invoice_ModuleId)) || moduleId.equals(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId))) {
                        jobjTemp1 = new JSONObject();
                        jobjTemp1.put("defaultHeader", "flatdiscount");
                        jobjTemp1.put("displayName", "flatdiscount");
                        jobjTemp1.put("dataIndex", "flatdiscount");
                        //jobjTemp1.put("dataIndexTableName", dataIndexTableName);
                        jobjTemp1.put("width", 150);
                        jobjTemp1.put("pdfwidth", 150);
                        jobjTemp1.put("isLineItem", isLineItem);
                        jobjTemp1.put("xtype", "2");
                        jobjTemp1.put("custom", "false");
                        jarrColumns.put(jobjTemp1);
                    }
                }
                if (isAnyQunatityPresent && isQtyLineItem && !hasUOM) {
                    HashMap<String, Object> uomRequestMap = new HashMap<String, Object>();
                    uomRequestMap.put(Constants.filter_names, Arrays.asList("module", "iscustomreport", "defaultHeader"));
                    uomRequestMap.put(Constants.filter_values, Arrays.asList(moduleId, true, CustomReportConstants.UOM));
                    uomRequestMap.put("order_by", Arrays.asList("defaultHeader"));
                    uomRequestMap.put("order_type", Arrays.asList("asc"));
                    KwlReturnObject uomEntity = accCustomerReportServiceDao.getDefaultFields(uomRequestMap);
                    if (uomEntity != null) {
                        List<DefaultHeader> uomDHList = uomEntity.getEntityList();
                        if (uomDHList.size() > 0) {
                            for (DefaultHeader uomHeader : uomDHList) {
                                JSONObject uomObj = new JSONObject();
                                uomObj.put("defaultHeader", CustomReportConstants.UOM);
                                uomObj.put("isDisplayUOM", true);
                                uomObj.put("id", uomHeader.getId());
                                uomObj.put("mainTable", getMainTableForModule(moduleId));
                                uomObj.put("dbtablename", uomHeader.getDbTableName());
                                uomObj.put("dbcolumnname", uomHeader.getDbcolumnname());
                                uomObj.put("reftablename", uomHeader.getReftablename());
                                uomObj.put("reftabledatacolumn", uomHeader.getReftabledatacolumn());
                                uomObj.put("reftablefk", uomHeader.getReftablefk());
                                uomObj.put("xtype", uomHeader.getXtype() != null ? Integer.parseInt(uomHeader.getXtype()) : 1); //TODO advance serch on combo,number,date fields
                                uomObj.put("customfield", false);
                                uomObj.put("isMeasureItem", false);
                                uomObj.put("isLineItem", isQtyLineItem);
                                uomObj.put("custom", "false");
                                uomObj.put("iscustomextrajoin", false);
                                uomObj.put("isDataIndex", uomHeader.isIsDataIndex());
                                uomObj.put("dataIndex", uomHeader.getId());
                                uomObj.put("properties", getFieldsProperties(uomObj));
                                fetchDefaultFieldHeaderMappings(uomHeader.getDbTableName(), uomHeader, uomObj);
                                jarrColumns.put(uomObj);
                                reportJSONArray.put(uomObj);
                            }
                        }
                    }
                }
                 if (isDisplayUOMPresent) {
                    HashMap<String, Object> displayUOMRequestMap = new HashMap<String, Object>();
                    displayUOMRequestMap.put(Constants.filter_names, Arrays.asList("module", "iscustomreport", "defaultHeader"));
                    displayUOMRequestMap.put(Constants.filter_values, Arrays.asList(moduleId, false, CustomReportConstants.Display_UOM_Mapping));
                    displayUOMRequestMap.put("order_by", Arrays.asList("defaultHeader"));
                    displayUOMRequestMap.put("order_type", Arrays.asList("asc"));
                    KwlReturnObject displayUOMMappingEntity = accCustomerReportServiceDao.getDefaultFields(displayUOMRequestMap);
                    if (displayUOMMappingEntity != null) {
                        List<DefaultHeader> displayUOMDHList = displayUOMMappingEntity.getEntityList();
                        if (displayUOMDHList.size() > 0) {
                            for (DefaultHeader displayUOMHeader : displayUOMDHList) {
                                JSONObject uomObj = new JSONObject();
                                uomObj.put("defaultHeader", CustomReportConstants.Display_UOM_Mapping);
                                uomObj.put("isDisplayUOM", true);
                                uomObj.put("id", displayUOMHeader.getId());
                                uomObj.put("mainTable", getMainTableForModule(moduleId));
                                uomObj.put("dbtablename", displayUOMHeader.getDbTableName());
                                uomObj.put("dbcolumnname", displayUOMHeader.getDbcolumnname());
                                uomObj.put("reftablename", displayUOMHeader.getReftablename());
                                uomObj.put("reftabledatacolumn", displayUOMHeader.getReftabledatacolumn());
                                uomObj.put("reftablefk", displayUOMHeader.getReftablefk());
                                uomObj.put("xtype", displayUOMHeader.getXtype() != null ? Integer.parseInt(displayUOMHeader.getXtype()) : 1); //TODO advance serch on combo,number,date fields
                                uomObj.put("customfield", false);
                                uomObj.put("isMeasureItem", false);
                                uomObj.put("isLineItem", isQtyLineItem);
                                uomObj.put("custom", "false");
                                uomObj.put("iscustomextrajoin", false);
                                uomObj.put("isDataIndex", displayUOMHeader.isIsDataIndex());
                                uomObj.put("dataIndex", displayUOMHeader.getId());
                                uomObj.put("properties", getFieldsProperties(uomObj));
//                            fetchDefaultFieldHeaderMappings(displayUOMHeader.getDbTableName(), displayUOMHeader, uomObj);
                                jarrColumns.put(uomObj);
                                reportJSONArray.put(uomObj);
                            }
                        }
                    }
                }
                Map sortClause=new HashMap<String, Object>();
                //if it has jsonarray then execute the query else not
                if (reportJSONArray.length() > 0) {
                    Map querymap = null;
                    if(isChartRequest) {
                        //builds the chart specific query
                        querymap = getCustomChartSqlQuery(moduleId, false, requestParam, paramList);
                    } else {
                        querymap = buildCustomReportSqlQuery(reportJSONArray, moduleId, false, requestParam, paramList,dataIndexObject);
                    }

                    if (querymap.get(CustomReportConstants.sqlquery) != null) {
                        reportSQLQuery = String.valueOf(querymap.get(CustomReportConstants.sqlquery));
                        }
                    if (querymap.get(CustomReportConstants.countquery) != null) {
                        countSQLQuery = String.valueOf(querymap.get(CustomReportConstants.countquery));
                    }
                    Map<String, Object> sqlqueryMap = new HashMap<String, Object>();
                    sqlqueryMap.put(CustomReportConstants.sqlquery, reportSQLQuery);
                    sqlqueryMap.put(CustomReportConstants.countquery, countSQLQuery);
                    if (requestParam.get("userId") != null) {
                        sqlqueryMap.put("userId", requestParam.get("userId"));
                    }
                    Map filterquery = getFilterQuery(sqlqueryMap, filterarray, dataIndexObject);
                    reportSQLQuery = String.valueOf(filterquery.get(CustomReportConstants.sqlquery));
                    countSQLQuery = String.valueOf(filterquery.get(CustomReportConstants.countquery));

                    if (!querymap.isEmpty()) { //in case of measure fields--this is empty
                        String reportJOINQuery = String.valueOf(querymap.get(CustomReportConstants.JOINQUERY));
                        String selectQuery = String.valueOf(querymap.get(CustomReportConstants.SELECTQUERY));
                        String conditionQuery = String.valueOf(querymap.get(CustomReportConstants.CONDITIONQUERY));
                        String cntVal = String.valueOf(querymap.get("cntVal"));
                        String selectCountQuery = String.valueOf(querymap.get("selectCountQuery"));
                        mainTable = reportJOINQuery.substring(reportJOINQuery.indexOf("from") + 4, reportJOINQuery.indexOf(" ", reportJOINQuery.indexOf("from") + 5));

                        if (requestParam.containsKey("searchJson") && requestParam.get("searchJson") != null) {
                            requestParam.put("paramList", paramList);
                            requestParam.put(CustomReportConstants.SELECTQUERY, selectQuery);
                            requestParam.put(CustomReportConstants.JOINQUERY, reportJOINQuery);
                            requestParam.put(CustomReportConstants.CONDITIONQUERY, conditionQuery);
                            requestParam.put("reportSQLQuery", reportSQLQuery);
                            requestParam.put("selectCountQuery",selectCountQuery);
                            requestParam.put("reportCountSQLQuery", countSQLQuery);
                            requestParam.put("mainTable", mainTable);

                            requestParam.put("JoinMapKeySet",querymap.get("JoinMapKeySet"));

                            if (isCustomWidgetReport) {
                                String Searchjson = requestParam.get("searchJson").toString();
                                String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
                                if (requestParam.containsKey("filterConjuctionCriteria") && requestParam.get("filterConjuctionCriteria") != null) {
                                    filterConjuctionCriteria = requestParam.get("filterConjuctionCriteria").toString().toLowerCase();
                                }
                                if (!StringUtil.isNullOrEmpty(Searchjson)) {
                                    HashMap<String, Object> reqPar1 = new HashMap<String, Object>();
                                    reqPar1.put(Constants.companyKey, requestParam.get("companyID"));
                                    reqPar1.put("searchJson", Searchjson);
                                    reqPar1.put(Constants.Filter_Criteria, filterConjuctionCriteria);
                                    reqPar1.put("moduleid", moduleId);
                                    reqPar1.put("isActivated", 1);
                                    String searchJSON = fieldDataManager.getSearchJsonByModuleID(reqPar1);
                                    JSONObject searchJobj = new JSONObject(searchJSON);

                                    requestParam.remove("searchJson");
                                    requestParam.put("searchJson", searchJSON);
                                    JSONObject searchJobjForModule = getSearchJsonForModuleID(requestParam);
                                    int searchJobjModuleLength =searchJobjForModule.optJSONArray("root").length();
                                    int searchJobjLength =searchJobj.optJSONArray("root").length();

                                    if ((searchJobjModuleLength == 0) ||((searchJobjLength > searchJobjModuleLength)&& filterConjuctionCriteria.equalsIgnoreCase("and")) ) {
                                        executeQuery = false;
                                    }

                                    requestParam.remove("searchJson");
                                    requestParam.put("searchJson", searchJobjForModule.toString());
                                }
                            }
                            Map advanceSearchQueryMap = accCustomerReportServiceDao.getAdvanceSearchQuery(requestParam);

                            reportSQLQuery = (String) advanceSearchQueryMap.get("reportSQLQuery");
                            countSQLQuery = (String) advanceSearchQueryMap.get("reportCountSQLQuery");

                            paramList = (ArrayList) advanceSearchQueryMap.get("paramList");
                        }
                        requestParam.put("moduleID", moduleId);
                        queryMap.put(CustomReportConstants.sqlquery, reportSQLQuery);
                        queryMap.put(CustomReportConstants.countquery, countSQLQuery);
                        if (requestParam.get("userId") != null) {
                            queryMap.put("userId", requestParam.get("userId"));
                        }
                        queryMap = getFilterQuery(queryMap, filterarray, dataIndexObject);
                        reportSQLQuery = String.valueOf(queryMap.get(CustomReportConstants.sqlquery));
                        countSQLQuery = String.valueOf(queryMap.get(CustomReportConstants.countquery));
                        String conditionsToQuery = "";
                        int cnt = Integer.parseInt(cntVal) + 1;
                        if (!showRowLevelFieldsflag ) {
                            if (moduleId.equals(String.valueOf(Constants.Acc_Invoice_ModuleId))) {//In case of sales invoice records fetch according to Journal entry date and invoice creation date
                                reportSQLQuery = reportSQLQuery + " and ((" + CustomReportConstants.JournalEntryTable + "." + CustomReportConstants.EntryDate + " BETWEEN '" + fromdate + "' and '" + todate + "' ) or (" + getDateColumn(mainTable.trim()) + " between '" + fromdate + "' and '" + todate + "' ))" + conditionsToQuery;
                                countSQLQuery = countSQLQuery + " and ((" + CustomReportConstants.JournalEntryTable + "." + CustomReportConstants.EntryDate + " BETWEEN '" + fromdate + "' and '" + todate + "' ) or (" + getDateColumn(mainTable.trim()) + " between '" + fromdate + "' and '" + todate + "' ))" + conditionsToQuery;
                            } else if (moduleId.equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {//In case of Receive Payment journal entry is NULL for Opening balance Inovices
                                reportSQLQuery = reportSQLQuery + " and ((" + mainTable.trim() + "." + CustomReportConstants.CreationDate + " BETWEEN '" + fromdate + "' and '" + todate + "' ) or (" + getDateColumn(mainTable.trim()) + " between '" + fromdate + "' and '" + todate + "' ))" + conditionsToQuery;
                                countSQLQuery = countSQLQuery + " and ((" + mainTable.trim() + "." + CustomReportConstants.CreationDate + " BETWEEN '" + fromdate + "' and '" + todate + "' ) or (" + getDateColumn(mainTable.trim()) + " between '" + fromdate + "' and '" + todate + "' ))" + conditionsToQuery;
                            } else {
                                reportSQLQuery = reportSQLQuery + " and " + getDateColumn(mainTable.trim()) + " between '" + fromdate + "' and '" + todate + "' " + conditionsToQuery;
                                countSQLQuery = countSQLQuery + " and " + getDateColumn(mainTable.trim()) + " between '" + fromdate + "' and '" + todate + "' " + conditionsToQuery;
                            }
                        }
                        if(querymap.containsKey(CustomReportConstants.GROUPBYCLAUSE))
                        {
                            String groupByClause = String.valueOf(querymap.get(CustomReportConstants.GROUPBYCLAUSE));
                            reportSQLQuery= reportSQLQuery+" "+groupByClause;
                            countSQLQuery= countSQLQuery+" "+groupByClause;
                        }
//                        countSQLQuery = reportSQLQuery;
                        sortClause = getSortSequenceForQuery(reportJSONArray);
                        if (isChartRequest) {
                            reportSQLQuery = buildCustomReportChartSqlQuery(requestParam, jarrColumns, reportSQLQuery);
                        } else if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit) && !showRowLevelFieldsflag  && !isWholeData) {
                            reportSQLQuery = reportSQLQuery + " " + sortClause.get("sortClause") + " " + " LIMIT " + limit + " OFFSET " + start;
                        }
                    }//end of querymap check
                        JSONArray allColumns = new JSONArray(jarrColumns.toString());
                        JSONArray allRows = new JSONArray();
                    if (executeQuery) {
                        SqlRowSet reportDataResultSet = null;
                        if (showRowLevelFieldsflag && (moduleId.equalsIgnoreCase(String.valueOf(Constants.Acc_Make_Payment_ModuleId)) || moduleId.equalsIgnoreCase(String.valueOf(Constants.Acc_Receive_Payment_ModuleId)))) {
                            JSONArray invoiceColumns = new JSONArray();
                            JSONArray linkedInvoiceColumns = new JSONArray();
                            JSONArray invoiceJarColumns = new JSONArray();
                            JSONArray linkedInvoiceJarColumns = new JSONArray();

                            JSONArray GLColumns = new JSONArray();
                            JSONArray GLJarColumns = new JSONArray();

                            JSONArray refundColumns = new JSONArray();
                            JSONArray linkedRefundColumns = new JSONArray();
                            JSONArray refundJarColumns = new JSONArray();
                            JSONArray linkedRefundJarColumns = new JSONArray();

                            JSONArray creditNoteColumns = new JSONArray();
                            JSONArray linkedCreditNoteColumns = new JSONArray();
                            JSONArray creditNoteJarColumns = new JSONArray();
                            JSONArray linkedCreditNoteJarColumns = new JSONArray();

                            JSONArray advancePaymentColumns = new JSONArray();
                            JSONArray advancePaymentJarColumns = new JSONArray();

                            JSONArray loanColumns = new JSONArray();
                            JSONArray loanJarColumns = new JSONArray();
                            
                            JSONArray dataArray = new JSONArray();
                            JSONObject dataJobj = new JSONObject();
                            JSONObject columns = new JSONObject();

                            if (reportJSONArray.length() > 0) {
                                for (int columnCnt = 0; columnCnt < reportJSONArray.length(); columnCnt++) {
                                    String defaultHeader = reportJSONArray.getJSONObject(columnCnt).optString("defaultHeader");
                                    boolean iscustomField = reportJSONArray.getJSONObject(columnCnt).optBoolean("customfield");
//                                if (!iscustomField) {
                                    if (defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_Linked_Credit_Note) > -1 || defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_Linked_Debit_Note) > -1) {
                                        linkedCreditNoteColumns.put(reportJSONArray.getJSONObject(columnCnt));
                                    } else if (defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_Linked_Invoice) > -1) {
                                        linkedInvoiceColumns.put(reportJSONArray.getJSONObject(columnCnt));
                                    } else if (defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_Linked_Refund) > -1) {
                                        linkedRefundColumns.put(reportJSONArray.getJSONObject(columnCnt));
                                    } else if (defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_Invoice) > -1) {
                                        invoiceColumns.put(reportJSONArray.getJSONObject(columnCnt));
                                    } else if (defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_Credit_Note) > -1 || defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_Debit_Note) > -1) {
                                        creditNoteColumns.put(reportJSONArray.getJSONObject(columnCnt));
                                    } else if (defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_Advance_Payment) > -1 || defaultHeader.equals(CustomReportConstants.Product_Tax_Class)) {
                                        advancePaymentColumns.put(reportJSONArray.getJSONObject(columnCnt));
                                    } else if (defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_GL) > -1) {
                                        GLColumns.put(reportJSONArray.getJSONObject(columnCnt));
                                    } else if (defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_Refund) > -1) {
                                        refundColumns.put(reportJSONArray.getJSONObject(columnCnt));
                                    } else if (defaultHeader.indexOf(CustomReportConstants.Acc_Receive_Payment_Loan) > -1) {
                                        loanColumns.put(reportJSONArray.getJSONObject(columnCnt));
                                    }
//                                }
                                }

                                for (int columnCnt = 0; columnCnt < jarrColumns.length(); columnCnt++) {
                                    String defaultHeader = jarrColumns.getJSONObject(columnCnt).optString("defaultHeaderOrig");
                                    boolean iscustomField = jarrColumns.getJSONObject(columnCnt).optBoolean("custom");
//                                if (!iscustomField) {
                                    if (defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_Linked_Credit_Note) > -1 || defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_Linked_Debit_Note) > -1) {
                                        linkedCreditNoteJarColumns.put(jarrColumns.getJSONObject(columnCnt));
                                    } else if (defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_Linked_Invoice) > -1) {
                                        linkedInvoiceJarColumns.put(jarrColumns.getJSONObject(columnCnt));
                                    } else if (defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_Linked_Refund) > -1) {
                                        linkedRefundJarColumns.put(jarrColumns.getJSONObject(columnCnt));
                                    } else if (defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_Invoice) > -1) {
                                        invoiceJarColumns.put(jarrColumns.getJSONObject(columnCnt));
                                    } else if (defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_Credit_Note) > -1 || defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_Debit_Note) > -1) {
                                        creditNoteJarColumns.put(jarrColumns.getJSONObject(columnCnt));
                                    } else if (defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_Advance_Payment) > -1  || defaultHeader.equals(CustomReportConstants.Product_Tax_Class)) {
                                        advancePaymentJarColumns.put(jarrColumns.getJSONObject(columnCnt));
                                    } else if (defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_GL) > -1) {
                                        GLJarColumns.put(jarrColumns.getJSONObject(columnCnt));
                                    } else if (defaultHeader.indexOf(CustomReportConstants.Acc_Make_Payment_Refund) > -1) {
                                        refundJarColumns.put(jarrColumns.getJSONObject(columnCnt));
                                    } else if (defaultHeader.indexOf(CustomReportConstants.Acc_Receive_Payment_Loan) > -1) {
                                        loanJarColumns.put(jarrColumns.getJSONObject(columnCnt));
                                    }
//                                }
                                }

                                if (invoiceColumns.length() > 0) {
                                    paramList.clear();
                                    reportSQLQuery = String.valueOf(buildCustomReportSqlQuery(invoiceColumns, moduleId, false, requestParam, paramList, dataIndexObject).get(CustomReportConstants.sqlquery));
                                    if (querymap.containsKey(CustomReportConstants.GROUPBYCLAUSE)) {
                                        String groupByClause = String.valueOf(querymap.get(CustomReportConstants.GROUPBYCLAUSE));
                                       if (moduleId.equalsIgnoreCase(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {
                                            reportSQLQuery = reportSQLQuery + " group by receiptdetails.id";
                                        } else if (moduleId.equalsIgnoreCase(String.valueOf(Constants.Acc_Make_Payment_ModuleId))) {
                                            reportSQLQuery = reportSQLQuery + " group by paymentdetail.id";
                                        }
                                    }
                                    reportDataResultSet = accCustomerReportServiceDao.executeCustomReportSQL(reportSQLQuery, paramList);
                                    processResultSetToJSON(moduleId, reportDataResultSet, currencyid, requestParam, invoiceJarColumns, dataArray);

                                    dataJobj.put("invoiceData", dataArray);
                                    allRows = StringUtil.concatJSONArray(allRows,dataArray);
                                }

                                if (creditNoteColumns.length() > 0) {
                                    paramList.clear();
                                    dataArray = new JSONArray();
                                    reportSQLQuery = String.valueOf(buildCustomReportSqlQuery(creditNoteColumns, moduleId, false, requestParam, paramList, dataIndexObject).get(CustomReportConstants.sqlquery));
                                    if (moduleId.equalsIgnoreCase(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {
                                        reportSQLQuery = reportSQLQuery + " group by debitnotepayment.id";
                                    } else if (moduleId.equalsIgnoreCase(String.valueOf(Constants.Acc_Make_Payment_ModuleId))) {
                                        reportSQLQuery = reportSQLQuery + " group by creditnotpayment.id";
                                    }
                                    reportDataResultSet = accCustomerReportServiceDao.executeCustomReportSQL(reportSQLQuery, paramList);
                                    processResultSetToJSON(moduleId, reportDataResultSet, currencyid, requestParam, creditNoteJarColumns, dataArray);

                                    dataJobj.put("creditNoteData", dataArray);
                                    allRows = StringUtil.concatJSONArray(allRows,dataArray);
                                }

                                if (advancePaymentColumns.length() > 0) {
                                    paramList.clear();
                                    dataArray = new JSONArray();
                                    reportSQLQuery = String.valueOf(buildCustomReportSqlQuery(advancePaymentColumns, moduleId, false, requestParam, paramList, dataIndexObject).get(CustomReportConstants.sqlquery));
                                    if (moduleId.equalsIgnoreCase(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {
                                        reportSQLQuery = reportSQLQuery + " group by receiptadvancedetail.id";
                                    } else if (moduleId.equalsIgnoreCase(String.valueOf(Constants.Acc_Make_Payment_ModuleId))) {
                                        reportSQLQuery = reportSQLQuery + " group by advancedetail.id";
                                    }
                                    reportDataResultSet = accCustomerReportServiceDao.executeCustomReportSQL(reportSQLQuery, paramList);
                                    processResultSetToJSON(moduleId, reportDataResultSet, currencyid, requestParam, advancePaymentJarColumns, dataArray);

                                    dataJobj.put("advancePaymentData", dataArray);
                                    allRows = StringUtil.concatJSONArray(allRows,dataArray);
                                }

                                if (GLColumns.length() > 0) {
                                    paramList.clear();
                                    dataArray = new JSONArray();
                                    reportSQLQuery = String.valueOf(buildCustomReportSqlQuery(GLColumns, moduleId, false, requestParam, paramList, dataIndexObject).get(CustomReportConstants.sqlquery));
                                    if (moduleId.equalsIgnoreCase(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {
                                        reportSQLQuery = reportSQLQuery + " group by receiptdetailotherwise.id";
                                    } else if (moduleId.equalsIgnoreCase(String.valueOf(Constants.Acc_Make_Payment_ModuleId))) {
                                        reportSQLQuery = reportSQLQuery + " group by paymentdetailotherwise.id";
                                    }
                                    reportDataResultSet = accCustomerReportServiceDao.executeCustomReportSQL(reportSQLQuery, paramList);
                                    processResultSetToJSON(moduleId, reportDataResultSet, currencyid, requestParam, GLJarColumns, dataArray);

                                    dataJobj.put("GLData", dataArray);
                                    allRows = StringUtil.concatJSONArray(allRows,dataArray);
                                }

                                if (refundColumns.length() > 0) {
                                    paramList.clear();
                                    dataArray = new JSONArray();
                                    reportSQLQuery = String.valueOf(buildCustomReportSqlQuery(refundColumns, moduleId, false, requestParam, paramList, dataIndexObject).get(CustomReportConstants.sqlquery));
                                        if (moduleId.equalsIgnoreCase(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {
                                        reportSQLQuery = reportSQLQuery + " group by receiptadvancedetail.id";
                                    } else if (moduleId.equalsIgnoreCase(String.valueOf(Constants.Acc_Make_Payment_ModuleId))) {
                                        reportSQLQuery = reportSQLQuery + " group by advancedetail.id";
                                    }
                                    reportDataResultSet = accCustomerReportServiceDao.executeCustomReportSQL(reportSQLQuery, paramList);
                                    processResultSetToJSON(moduleId, reportDataResultSet, currencyid, requestParam, refundJarColumns, dataArray);

                                    dataJobj.put("refundData", dataArray);
                                    allRows = StringUtil.concatJSONArray(allRows,dataArray);
                                }

                                if (linkedInvoiceColumns.length() > 0) {
                                    paramList.clear();
                                    dataArray = new JSONArray();
                                    reportSQLQuery = String.valueOf(buildCustomReportSqlQuery(linkedInvoiceColumns, moduleId, false, requestParam, paramList, dataIndexObject).get(CustomReportConstants.sqlquery));
                                    if (moduleId.equalsIgnoreCase(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {
                                        reportSQLQuery = reportSQLQuery + " group by linkdetailreceipt.id";
                                    } else if (moduleId.equalsIgnoreCase(String.valueOf(Constants.Acc_Make_Payment_ModuleId))) {
                                        reportSQLQuery = reportSQLQuery + " group by linkdetailpayment.id";
                                    }
                                    reportDataResultSet = accCustomerReportServiceDao.executeCustomReportSQL(reportSQLQuery, paramList);
                                    processResultSetToJSON(moduleId, reportDataResultSet, currencyid, requestParam, linkedInvoiceJarColumns, dataArray);

                                    dataJobj.put("linkedInvoiceData", dataArray);
                                    allRows = StringUtil.concatJSONArray(allRows,dataArray);
                                }

                                if (linkedCreditNoteColumns.length() > 0) {
                                    paramList.clear();
                                    dataArray = new JSONArray();
                                    reportSQLQuery = String.valueOf(buildCustomReportSqlQuery(linkedCreditNoteColumns, moduleId, false, requestParam, paramList, dataIndexObject).get(CustomReportConstants.sqlquery));
                                    if (moduleId.equalsIgnoreCase(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {
                                        reportSQLQuery = reportSQLQuery + " group by linkdetailreceipttodebitnote.id";
                                    } else if (moduleId.equalsIgnoreCase(String.valueOf(Constants.Acc_Make_Payment_ModuleId))) {
                                        reportSQLQuery = reportSQLQuery + " group by linkdetailpaymenttocreditnote.id";
                                    }
                                    reportDataResultSet = accCustomerReportServiceDao.executeCustomReportSQL(reportSQLQuery, paramList);
                                    processResultSetToJSON(moduleId, reportDataResultSet, currencyid, requestParam, linkedCreditNoteJarColumns, dataArray);

                                    dataJobj.put("linkedCreditNoteData", dataArray);
                                    allRows = StringUtil.concatJSONArray(allRows,dataArray);
                                }

                                if (linkedRefundColumns.length() > 0) {
                                    paramList.clear();
                                    dataArray = new JSONArray();
                                    reportSQLQuery = String.valueOf(buildCustomReportSqlQuery(linkedRefundColumns, moduleId, false, requestParam, paramList, dataIndexObject).get(CustomReportConstants.sqlquery));
                                    if (querymap.containsKey(CustomReportConstants.GROUPBYCLAUSE)) {
                                        String groupByClause = String.valueOf(querymap.get(CustomReportConstants.GROUPBYCLAUSE));
                                        reportSQLQuery = reportSQLQuery + " " + groupByClause;
                                    }
                                    reportDataResultSet = accCustomerReportServiceDao.executeCustomReportSQL(reportSQLQuery, paramList);
                                    processResultSetToJSON(moduleId, reportDataResultSet, currencyid, requestParam, linkedRefundJarColumns, dataArray);

                                    dataJobj.put("linkedRefundData", dataArray);
                                    allRows = StringUtil.concatJSONArray(allRows,dataArray);
                                }

                                if (loanColumns.length() > 0) {
                                    paramList.clear();
                                    dataArray = new JSONArray();
                                    reportSQLQuery = String.valueOf(buildCustomReportSqlQuery(loanColumns, moduleId, false, requestParam, paramList, dataIndexObject).get(CustomReportConstants.sqlquery));
                                    reportSQLQuery = reportSQLQuery + " group by receiptdetailsloan.id";
                                    reportDataResultSet = accCustomerReportServiceDao.executeCustomReportSQL(reportSQLQuery, paramList);
                                    processResultSetToJSON(moduleId, reportDataResultSet, currencyid, requestParam, loanJarColumns, dataArray);

                                    dataJobj.put("loanData", dataArray);
                                }

                                columns.put("invoiceColumns", invoiceColumns);
                                columns.put("creditNoteColumns", creditNoteColumns);
                                columns.put("advancePaymentColumns", advancePaymentColumns);
                                columns.put("GLColumns", GLColumns);
                                columns.put("refundColumns", refundColumns);
                                columns.put("linkedInvoiceColumns", linkedInvoiceColumns);
                                columns.put("linkedCreditNoteColumns", linkedCreditNoteColumns);
                                columns.put("linkedRefundColumns", linkedRefundColumns);
                                columns.put("loanColumns", loanColumns);
                                columns.put("length", reportJSONArray.length());

                                jarrRecords = new JSONArray();
                                jarrRecords.put(dataJobj);
                                jarrColumns =new JSONArray();
                                jarrColumns.put(columns);
                            }
                    } else if(showRowLevelFieldsflag && (moduleId.equals(String.valueOf(Constants.Acc_Debit_Note_ModuleId)) || moduleId.equals(String.valueOf(Constants.Acc_Credit_Note_ModuleId)))) {
                            boolean iscrossmodule = false;
                            JSONObject dataJobj = new JSONObject();
                            JSONArray dataArray = new JSONArray();
                            JSONObject filteredColumns = null;
                            JSONArray accountJarColumns = new JSONArray();
                            JSONArray invoiceJarColumns = new JSONArray();
                            JSONObject columns = new JSONObject();
                            if (reportJSONArray.length() > 0) {
                                for (int columnCnt = 0; columnCnt < reportJSONArray.length(); columnCnt++) {
                                    iscrossmodule = reportJSONArray.getJSONObject(columnCnt).optBoolean("allowcrossmodule", false);
                                    if (iscrossmodule) {
                                        break;
                                    }
                                }
                            }
                            jsonArrayMap = showRowLevelFieldsJSONArray(reportJSONArray, showRowLevelFieldsflag);
                            if (jsonArrayMap.containsKey("jarrColumns")) {
                                reportJSONArray = (JSONArray) jsonArrayMap.get("jarrColumns");
                            }
                            filteredColumns = filterAccountsAndInvoiceColumns(reportJSONArray);
                            JSONArray accountColumns = null, invoiceColumns = null;
                            JSONObject returnJobj = null;

                            for (int columnCnt = 0; columnCnt < jarrColumns.length(); columnCnt++) {
                                String defaultHeader = jarrColumns.getJSONObject(columnCnt).optString("defaultHeaderOrig");
                                boolean iscustomField = jarrColumns.getJSONObject(columnCnt).optBoolean("custom");
                                if (defaultHeader.indexOf(CustomReportConstants.Acc_Account) > -1
                                        || defaultHeader.indexOf(CustomReportConstants.TYPE) > -1
                                        || defaultHeader.indexOf(CustomReportConstants.Acc_Tax_Percent) > -1
                                        || (defaultHeader.indexOf(CustomReportConstants.Acc_Sales_Order_Measure_Total_Tax) > -1 && !jarrColumns.getJSONObject(columnCnt).optBoolean("allowcrossmodule", false))
                                        || (defaultHeader.indexOf(CustomReportConstants.AMOUNT) > -1 && !jarrColumns.getJSONObject(columnCnt).optBoolean("allowcrossmodule", false))
                                        || defaultHeader.indexOf(CustomReportConstants.Acc_Description) > -1
                                        || defaultHeader.indexOf(CustomReportConstants.Acc_Amount_Excluding_GST) > -1
                                        || defaultHeader.contains(CustomReportConstants.Product_Tax_Class)) {
                                    accountJarColumns.put(jarrColumns.getJSONObject(columnCnt));
                                } else if (defaultHeader.indexOf(CustomReportConstants.Acc_Invoice_Number) > -1
                                        || defaultHeader.indexOf(CustomReportConstants.Acc_Creation_Date) > -1
                                        || defaultHeader.indexOf(CustomReportConstants.Acc_Due_Date) > -1
                                        || defaultHeader.indexOf(CustomReportConstants.Acc_Linking_Date) > -1
                                        || defaultHeader.indexOf(CustomReportConstants.Acc_Invoice_Amount) > -1
                                        || defaultHeader.indexOf(CustomReportConstants.Acc_Debit_Note_Amount_Due) > -1
                                        || defaultHeader.indexOf(CustomReportConstants.TYPE) > -1) {
                                    invoiceJarColumns.put(jarrColumns.getJSONObject(columnCnt));
                                } else if (jarrColumns.getJSONObject(columnCnt).optBoolean("allowcrossmodule", false)) {
                                    invoiceJarColumns.put(jarrColumns.getJSONObject(columnCnt));
                                }
                            }
                            if (filteredColumns != null && filteredColumns.optJSONArray("accountColumns").length() > 0) {
                                accountColumns = filteredColumns.getJSONArray("accountColumns");
                            }
                            if (accountColumns != null && accountColumns.length() > 0) {
                                paramList.clear();
                                dataArray = new JSONArray();
                                reportSQLQuery = String.valueOf(buildCustomReportSqlQuery(accountColumns, moduleId, false, requestParam, paramList, dataIndexObject).get(CustomReportConstants.sqlquery));
                                if (querymap.containsKey(CustomReportConstants.GROUPBYCLAUSE)) {
                                    String groupByClause = String.valueOf(querymap.get(CustomReportConstants.GROUPBYCLAUSE));
                                    reportSQLQuery = reportSQLQuery + " " + groupByClause;
                                }
                                reportDataResultSet = accCustomerReportServiceDao.executeCustomReportSQL(reportSQLQuery, paramList);
                                processResultSetToJSON(moduleId, reportDataResultSet, currencyid, requestParam, accountJarColumns, dataArray);
                                dataJobj.put("accountData", dataArray);
                            }
                            if (filteredColumns != null && filteredColumns.optJSONArray("invoiceColumns").length() > 0) {
                                invoiceColumns = filteredColumns.getJSONArray("invoiceColumns");
                            }
                            if (invoiceColumns != null && invoiceColumns.length() > 0) {
                                paramList.clear();
                                dataArray = new JSONArray();
                                reportSQLQuery = String.valueOf(buildCustomReportSqlQuery(invoiceColumns, moduleId, false, requestParam, paramList, dataIndexObject).get(CustomReportConstants.sqlquery));
                                if (querymap.containsKey(CustomReportConstants.GROUPBYCLAUSE)) {
                                    String groupByClause = String.valueOf(querymap.get(CustomReportConstants.GROUPBYCLAUSE));
                                    reportSQLQuery = reportSQLQuery + " " + groupByClause;
                                }
                                reportDataResultSet = accCustomerReportServiceDao.executeCustomReportSQL(reportSQLQuery, paramList);
                                processResultSetToJSON(moduleId, reportDataResultSet, currencyid, requestParam, invoiceJarColumns, dataArray);
                                dataJobj.put("invoiceData", dataArray);
                            }
                            columns.put("accountColumns", accountColumns);
                            columns.put("invoiceColumns", invoiceColumns);
                            jarrRecords.put(dataJobj);
                            jarrColumns = new JSONArray();
                            jarrColumns.put(columns);
                    }else {
                        if (!StringUtil.isNullOrEmpty(reportSQLQuery)&& jarrColumns.length()>0) {
                            reportDataResultSet = accCustomerReportServiceDao.executeCustomReportSQL(reportSQLQuery, paramList);
//                            System.out.println("sqlquery = " + reportSQLQuery);
                                processResultSetToJSON(moduleId, reportDataResultSet, currencyid, requestParam, jarrColumns, jarrRecords);
                                allRows = StringUtil.concatJSONArray(allRows,jarrRecords);
                                if (isAnyQunatityPresent && isQtyLineItem && isDisplayUOMPresent) {
                                    removeJSONObject(jarrColumns, "isDisplayUOM");
                                }
                                if (!hasUOM) {
                                    removeJSONObject(jarrColumns, "isDisplayUOM");
                                }
                                totalCount = accCustomerReportServiceDao.executeCustomReportCountSQL(countSQLQuery, paramList);
                            }
                        }

                        //For measurable fields
                        for (int z = 0; z < measureFieldsJsonArray.length(); z++) {
                            JSONArray mergedJSONArray = new JSONArray();
                            JSONObject invidualmeasurejsonobj = measureFieldsJsonArray.getJSONObject(z);
                            boolean isDiscountPresentnewflag = false, isLineItemnewflag = false, isAnyQunatityPresentnewflag = false, isQtyLineItemnewflag = false;

                            JSONObject newdataIndexObject = new JSONObject();
                            dataIndexTableName = "";
                            columnmap = new HashMap<String, String>();
                            paramList = new ArrayList();
                            properties = new JSONObject();
                            JSONArray measurejarrRecords = new JSONArray();//all the data will be present in this jsonarray
                            JSONArray measuresjsonArray = new JSONArray(invidualmeasurejsonobj.optString("measurefieldjsonArray", "[]"));
                            String measuresqlquery = null;
                            String measureCountSqlQuery = null;
                            JSONArray newjarrColumns = new JSONArray();

                            for (int x = 0; x < measuresjsonArray.length(); x++) {
                                JSONObject jObj = measuresjsonArray.getJSONObject(x);
                                Map returnMap = buildcolumnDetailsCustomReport(jObj, columnmap, x, newdataIndexObject, newjarrColumns, properties, filterarray, moduleId, dataIndexTableName, isDiscountPresentnewflag, isLineItemnewflag, isAnyQunatityPresentnewflag, isQtyLineItemnewflag,isDisplayUOMPresent);
                                x = (Integer) returnMap.get("cnt");
                                isLineItemnewflag = (Boolean) returnMap.get("isLineItem");
                                isDiscountPresentnewflag = (Boolean) returnMap.get("isDiscountPresent");
                                isQtyLineItemnewflag = (Boolean) returnMap.get("isQtyLineItem");
                                isAnyQunatityPresentnewflag = (Boolean) returnMap.get("isAnyQunatityPresent");
                                newdataIndexObject = (JSONObject) returnMap.get("dataIndexObject");
                                dataIndexTableName = (String) returnMap.get("dataIndexTableName");
                                newjarrColumns = (JSONArray) returnMap.get("jarrColumns");
                                columnmap = (HashMap<String, String>) returnMap.get("columnmap");
                                properties = (JSONObject) returnMap.get("properties");
                            }

                            Map measquerymap = buildCustomReportSqlQuery(measuresjsonArray, moduleId, false, requestParam, paramList,dataIndexObject);
//                        measquerymap = getFilterQuery(measquerymap, filterarray, newdataIndexObject);
                            measuresqlquery = String.valueOf(measquerymap.get(CustomReportConstants.sqlquery));
                            if (!showRowLevelFieldsflag ) {
                                if (moduleId.equals(String.valueOf(Constants.Acc_Invoice_ModuleId))) {//In case of sales invoice records fetch according to Journal entry date and invoice creation date
                                measuresqlquery = measuresqlquery + " and ((" + CustomReportConstants.JournalEntryTable + "." + CustomReportConstants.EntryDate + " BETWEEN '" + fromdate + "' and '" + todate + "' ) or (" + getDateColumn(mainTable.trim()) + " between '" + fromdate + "' and '" + todate + "' ))" ;
                                } else if (moduleId.equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {//In case of Receive Payment journal entry is NULL for Opening balance Inovices
                                measuresqlquery = measuresqlquery + " and ((" + mainTable.trim() + "." + CustomReportConstants.CreationDate + " BETWEEN '" + fromdate + "' and '" + todate + "' ) or (" + getDateColumn(mainTable.trim()) + " between '" + fromdate + "' and '" + todate + "' ))" ;
                                } else {
                                measuresqlquery = measuresqlquery + " and " + getDateColumn(mainTable.trim()) + " between '" + fromdate + "' and '" + todate + "' " ;
                                }
                            }
                            measureCountSqlQuery = measuresqlquery;
                            if(!showRowLevelFieldsflag && !StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit) && !isWholeData) {//Because there is no paging in pivot reports
                                measuresqlquery = measuresqlquery + " LIMIT " + requestParam.get("limit") + " OFFSET " + requestParam.get("start");
                            }
                            reportDataResultSet = accCustomerReportServiceDao.executeCustomReportSQL(measuresqlquery, paramList);
                            processResultSetToJSON(moduleId, reportDataResultSet, currencyid, requestParam, newjarrColumns, measurejarrRecords);
                            int measureCount = accCustomerReportServiceDao.executeCustomReportCountSQL(measureCountSqlQuery, paramList);
                            totalCount = measureCount;

                            //Executing the expressions
                            String expression = invidualmeasurejsonobj.optString("expression");
                            int precision = invidualmeasurejsonobj.getJSONObject("properties").getJSONObject("source").optInt("precision",2);
                            JSONObject returnjobj = CustomReportHandler.executeRegularExpression(measurejarrRecords, expression, invidualmeasurejsonobj.optString(Constants.Acc_id),precision);
                            measurejarrRecords = returnjobj.getJSONArray(Constants.data);

                            if (jarrRecords.length() > 0) {//if measures field are inserted and not other fields
                                for (int k = 0; k < jarrRecords.length(); k++) {
                                    JSONObject colsrec = jarrRecords.getJSONObject(k);
                                    for (int p = 0; p < measurejarrRecords.length(); p++) {
                                        JSONObject mobj = measurejarrRecords.getJSONObject(p);
                                        if (!StringUtil.isNullOrEmpty(mobj.optString(Constants.billid)) && !StringUtil.isNullOrEmpty(colsrec.optString(Constants.billid)) && mobj.optString(Constants.billid).equalsIgnoreCase(colsrec.optString(Constants.billid))) {//                                kobj
                                            String key = invidualmeasurejsonobj.optString(Constants.Acc_id);
                                            colsrec.put(key, mobj.get(key));
                                            break;
                                        }
                                    }
                                    mergedJSONArray.put(colsrec);
                                }
                                jarrRecords = mergedJSONArray;//at the end assigning merged data to final array
                            } else {
                                jarrRecords = measurejarrRecords;//at the end assigning merged data to final array
                            }
                        }//end of for measureFieldsJsonArray

                        if (measureFieldsJsonArray.length() > 0) { //assging the jarrcolumns which has measure field also
                            jarrColumns = jarrColumnsIncludingMeasureFields;
                        }

                        Map<String, JSONObject> measureFieldSortMap = (TreeMap<String, JSONObject>) sortClause.get("measureFieldSortMap");
                        if (measureFieldSortMap != null && measureFieldSortMap.size() > 0) {
                            Set<String> measureFieldSortMapKeys = measureFieldSortMap.keySet();
                            for (String fieldname : measureFieldSortMapKeys) {
                                JSONObject measureSortObj = measureFieldSortMap.get(fieldname);
                            jarrRecords = sortJsonArrayOnTransaction(jarrRecords,(String)measureSortObj.get("measureFieldID"),(String)measureSortObj.get("sortOrder"));                            
                            }
                        }
                    }
                    userPreferences.put("fromdate", fromdate);
                    userPreferences.put("todate", todate);

                    if (isCustomWidgetReport) {
                        JSONObject storeRec = createStoreRec(jarrColumns);
                        commData.put("storerec", storeRec);
                    }

                    commData.put("success", true);
                    if (forExport) {
                        commData.put("columns", allColumns);
                        jarrRecordsObject.put("reportdata", allRows);
                    } else {
                        commData.put("columns", jarrColumns);
                        jarrRecordsObject.put("reportdata", jarrRecords);
                    }
                    jMeta.append("metaData", commData);
                    jresult.append("data", jMeta);
                    jresult.append("data", jarrRecordsObject);
                    jresult.append("data", totalCount);
                    jresult.append("data", userPreferences);
                    jresult.put("sortConfigArray", sortClause.get("sortConfigArray"));
                    jresult.append("data", returnJArray != null ? returnJArray : new JSONArray());
                    jresult.append("billid", (String) requestParam.get("billid"));//if any record is not there in expander getting expander id from main row. This is done for expander only.
                    jresult.put("filter", filterarray);
                    jresult.put("pivotConfig", reportJSONObj.optJSONObject("pivotConfig"));

                } else {
                    commData.put("success", false);
                    jMeta.append("metaData", commData);
                    jresult.append("data", jMeta);
                }

            } catch (JSONException | UnsupportedEncodingException | ServiceException ex) {
                try {
                    jresult.put("Exception", ex.getCause());
                } catch (JSONException ex1) {
                    Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex1);
                }
                Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            try {
                jresult.put("success", false);
                jresult.put(CustomReportConstants.MESSAGE_KEY, "acc.CustomReport.ReportNotFound");
            } catch (JSONException ex) {
                Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jresult;

    }

    /**
     * Execute the selected report fields for preview
     *
     * @param selectedRowsJSONData
     * @param valueMap
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    @Override
    public JSONObject executeCustomReportPreview(JSONArray selectedRowsJSONData, Map valueMap) throws ServiceException, JSONException, IOException, SessionExpiredException, ParseException {
        String sqlquery = "";
        String countquery = "";
        String groupByClause = "";
        int totalCount = 0;
        String moduleId = (String) valueMap.get("moduleID");
        String userTimeFormat = (String) valueMap.get("userTimeFormat");
        String currencyid = (String) valueMap.get("gcurrencyid");
        Boolean hasUOM=false;
        JSONArray jarrColumns = new JSONArray();
        JSONArray jarrRecords = new JSONArray();
        JSONObject jresult = new JSONObject();
        ArrayList paramList = new ArrayList();
        //JSONArray dataIndexJSONArray = new JSONArray();
        JSONObject dataIndexJSONObject = new JSONObject();
        JSONObject dataIndexObject = new JSONObject();
        JSONObject jobjTemp;
        HashMap<String, Object> reuqestParams = new HashMap<>();
        reuqestParams.put("timezoneid", (String) valueMap.get("timezoneid"));
        reuqestParams.put("dateformatid", (String) valueMap.get("dateformatid"));
        reuqestParams.put("currencyid", (String) valueMap.get("currencyid"));

        boolean isDiscountPresent = false, isLineItem = false,isAnyQunatityPresent = false,isQtyLineItem = false,isDisplayUOMPresent=false;
        String dataIndexTableName = "";

        Map<String, Object> compPrefRequestParams = new HashMap<String, Object>();
        compPrefRequestParams.put("id", valueMap.get("companyID"));
        KwlReturnObject result = accCompanyPreferencesObj.getCompanyPreferences(compPrefRequestParams);
        CompanyAccountPreferences pref = (CompanyAccountPreferences) result.getEntityList().get(0);
        valueMap.put("companyAccPref", pref);

        HashMap<String, String> columnmap = new HashMap<String, String>();
        try {
            JSONArray filterarray = new JSONArray((String) valueMap.get("filter"));
            JSONArray measureFieldsJsonArray = new JSONArray();

            for (int cnt = 0; cnt < selectedRowsJSONData.length(); cnt++) {
                JSONObject jObj = selectedRowsJSONData.getJSONObject(cnt);

                if (!jObj.optBoolean(Constants.isforformulabuilder, false)) {//escaping formula builder
                    Map returnMap = buildcolumnDetailsCustomReportPreview(jObj, columnmap, cnt, dataIndexObject, jarrColumns, moduleId, dataIndexTableName, isDiscountPresent, isLineItem, isAnyQunatityPresent, isQtyLineItem,isDisplayUOMPresent);
                    cnt = (Integer) returnMap.get("cnt");
                    isLineItem = (Boolean) returnMap.get("isLineItem");
                    isDiscountPresent = (Boolean) returnMap.get("isDiscountPresent");
                    isQtyLineItem = (Boolean) returnMap.get("isQtyLineItem");
                    isAnyQunatityPresent = (Boolean) returnMap.get("isAnyQunatityPresent");
                    isDisplayUOMPresent= (Boolean) returnMap.get("isDisplayUOMPresent");
                    dataIndexObject = (JSONObject) returnMap.get("dataIndexObject");
                    dataIndexTableName = (String) returnMap.get("dataIndexTableName");
                    jarrColumns = (JSONArray) returnMap.get("jarrColumns");
                    if(jObj.optString("defaultHeader").startsWith("UOM")){
                        hasUOM=true;
                    }
                    //For ERP-33659 :   For Reshuffling logic to be used while processResultSetToJSON method, below two essential flags were missing in jarrcolumns Hence added these two flags.
                    if (selectedRowsJSONData.getJSONObject(cnt).optBoolean("allowcrossmodule")) {
                        jarrColumns.getJSONObject(cnt).put("allowcrossmodule", selectedRowsJSONData.getJSONObject(cnt).optString("allowcrossmodule"));
                    }
                    if (selectedRowsJSONData.getJSONObject(cnt).optBoolean("customfield")) {
                        jarrColumns.getJSONObject(cnt).put("customfield", selectedRowsJSONData.getJSONObject(cnt).optString("customfield"));
                    }
                    columnmap = (HashMap<String, String>) returnMap.get("columnmap");

                } else {//end of isformula builder 
                    measureFieldsJsonArray.put(jObj);
                }
            }//end of for loop 
            if (isDiscountPresent && isLineItem) {
                JSONObject jobjTemp1 = new JSONObject();
                jobjTemp1.put("defaultHeader", "discountispercent");
                jobjTemp1.put("displayName", "Discountispersent");
                jobjTemp1.put("dataIndex", "discountispercent");
                jobjTemp1.put("dataIndexTableName", dataIndexTableName);
                jobjTemp1.put("width", 150);
                jobjTemp1.put("pdfwidth", 150);
                jobjTemp1.put("isLineItem", isLineItem);
                jobjTemp1.put("xtype", "1");
                jobjTemp1.put("custom", "false");
                jarrColumns.put(jobjTemp1);
                if (moduleId.equalsIgnoreCase(String.valueOf(Constants.Acc_Sales_Order_ModuleId)) || moduleId.equalsIgnoreCase(String.valueOf(Constants.Acc_Purchase_Order_ModuleId)) || moduleId.equalsIgnoreCase(String.valueOf(Constants.Acc_Invoice_ModuleId)) || moduleId.equalsIgnoreCase(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId))) {
                    jobjTemp1 = new JSONObject();
                    jobjTemp1.put("defaultHeader", "flatdiscount");
                    jobjTemp1.put("displayName", "flatdiscount");
                    jobjTemp1.put("dataIndex", "flatdiscount");
                    //jobjTemp1.put("dataIndexTableName", dataIndexTableName);
                    jobjTemp1.put("width", 150);
                    jobjTemp1.put("pdfwidth", 150);
                    jobjTemp1.put("isLineItem", isLineItem);
                    jobjTemp1.put("xtype", "2");
                    jobjTemp1.put("custom", "false");
                    jarrColumns.put(jobjTemp1);
                }
            }

            if (isAnyQunatityPresent && isQtyLineItem && !hasUOM) {
                HashMap<String, Object> uomRequestMap = new HashMap<String, Object>();
                uomRequestMap.put(Constants.filter_names, Arrays.asList("module", "iscustomreport", "defaultHeader"));
                uomRequestMap.put(Constants.filter_values, Arrays.asList(moduleId, true, CustomReportConstants.UOM));
                uomRequestMap.put("order_by", Arrays.asList("defaultHeader"));
                uomRequestMap.put("order_type", Arrays.asList("asc"));
                KwlReturnObject uomEntity = accCustomerReportServiceDao.getDefaultFields(uomRequestMap);
                if (uomEntity != null) {
                    List<DefaultHeader> uomDHList = uomEntity.getEntityList();
                    if (uomDHList.size() > 0) {
                        for (DefaultHeader uomHeader : uomDHList) {
                            JSONObject uomObj = new JSONObject();
                            uomObj.put("defaultHeader", CustomReportConstants.UOM);
                            uomObj.put("isDisplayUOM", true);
                            uomObj.put("id", uomHeader.getId());
                            uomObj.put("mainTable", getMainTableForModule(moduleId));
                            uomObj.put("dbtablename", uomHeader.getDbTableName());
                            uomObj.put("dbcolumnname", uomHeader.getDbcolumnname());
                            uomObj.put("reftablename", uomHeader.getReftablename());
                            uomObj.put("reftabledatacolumn", uomHeader.getReftabledatacolumn());
                            uomObj.put("reftablefk", uomHeader.getReftablefk());
                            uomObj.put("xtype", uomHeader.getXtype() != null ? Integer.parseInt(uomHeader.getXtype()) : 1); //TODO advance serch on combo,number,date fields
                            uomObj.put("customfield", false);
                            uomObj.put("isMeasureItem", false);
                            uomObj.put("isLineItem", isQtyLineItem);
                            uomObj.put("custom", "false");
                            uomObj.put("iscustomextrajoin", false);
                            uomObj.put("isDataIndex", uomHeader.isIsDataIndex());
                            uomObj.put("dataIndex", uomHeader.getId());
                            uomObj.put("properties", getFieldsProperties(uomObj));
                            fetchDefaultFieldHeaderMappings(uomHeader.getDbTableName(), uomHeader, uomObj);
                            jarrColumns.put(uomObj);
                            selectedRowsJSONData.put(uomObj);
                         }
                    }
                }
            }
            
            if (isDisplayUOMPresent) {
                HashMap<String, Object> displayUOMRequestMap = new HashMap<String, Object>();
                displayUOMRequestMap.put(Constants.filter_names, Arrays.asList("module", "iscustomreport", "defaultHeader"));
                displayUOMRequestMap.put(Constants.filter_values, Arrays.asList(moduleId, false, CustomReportConstants.Display_UOM_Mapping));
                displayUOMRequestMap.put("order_by", Arrays.asList("defaultHeader"));
                displayUOMRequestMap.put("order_type", Arrays.asList("asc"));
                KwlReturnObject displayUOMMappingEntity = accCustomerReportServiceDao.getDefaultFields(displayUOMRequestMap);
                if (displayUOMMappingEntity != null) {
                    List<DefaultHeader> displayUOMDHList = displayUOMMappingEntity.getEntityList();
                    if (displayUOMDHList.size() > 0) {
                        for (DefaultHeader displayUOMHeader : displayUOMDHList) {
                            JSONObject uomObj = new JSONObject();
                            uomObj.put("defaultHeader", CustomReportConstants.Display_UOM_Mapping);
                            uomObj.put("isDisplayUOM", true);
                            uomObj.put("id", displayUOMHeader.getId());
                            uomObj.put("mainTable", getMainTableForModule(moduleId));
                            uomObj.put("dbtablename", displayUOMHeader.getDbTableName());
                            uomObj.put("dbcolumnname", displayUOMHeader.getDbcolumnname());
                            uomObj.put("reftablename", displayUOMHeader.getReftablename());
                            uomObj.put("reftabledatacolumn", displayUOMHeader.getReftabledatacolumn());
                            uomObj.put("reftablefk", displayUOMHeader.getReftablefk());
                            uomObj.put("xtype", displayUOMHeader.getXtype() != null ? Integer.parseInt(displayUOMHeader.getXtype()) : 1); //TODO advance serch on combo,number,date fields
                            uomObj.put("customfield", false);
                            uomObj.put("isMeasureItem", false);
                            uomObj.put("isLineItem", isQtyLineItem);
                            uomObj.put("custom", "false");
                            uomObj.put("iscustomextrajoin", false);
                            uomObj.put("isDataIndex", displayUOMHeader.isIsDataIndex());
                            uomObj.put("dataIndex", displayUOMHeader.getId());
                            uomObj.put("properties", getFieldsProperties(uomObj));
//                            fetchDefaultFieldHeaderMappings(displayUOMHeader.getDbTableName(), displayUOMHeader, uomObj);
                            jarrColumns.put(uomObj);
                            selectedRowsJSONData.put(uomObj);
                        }
                    }
                }
            }

             Map querymap = buildCustomReportSqlQuery(selectedRowsJSONData, moduleId, false, valueMap, paramList,dataIndexObject);
             if(querymap.containsKey(CustomReportConstants.GROUPBYCLAUSE)){
                      groupByClause = String.valueOf(querymap.get(CustomReportConstants.GROUPBYCLAUSE));
                        }
            querymap = getFilterQuery(querymap, filterarray, dataIndexObject);

            sqlquery = String.valueOf(querymap.get(CustomReportConstants.sqlquery));
           
            sqlquery= sqlquery+" "+groupByClause;
            Map sortClause = getSortSequenceForQuery(selectedRowsJSONData);
            SqlRowSet reportDataResultSet=null;

            if (!StringUtil.isNullOrEmpty(sqlquery)) {
               countquery = sqlquery ;
                //Set the limit 
                sqlquery = sqlquery + " " + sortClause.get("sortClause") + " " + " LIMIT " + valueMap.get("limit") + " OFFSET " + valueMap.get("start");
//                System.out.println("sqlquery = " + sqlquery);
                reportDataResultSet = accCustomerReportServiceDao.executeCustomReportSQL(sqlquery, paramList);
                processResultSetToJSON(moduleId, reportDataResultSet, currencyid, valueMap, jarrColumns, jarrRecords);

                Map<String, JSONObject> measureFieldSortMap = (TreeMap<String, JSONObject>) sortClause.get("measureFieldSortMap");
                if (measureFieldSortMap != null && measureFieldSortMap.size() > 0) {
                    Set<String> measureFieldSortMapKeys = measureFieldSortMap.keySet();
                    for (String fieldname : measureFieldSortMapKeys) {
                        JSONObject measureSortObj = measureFieldSortMap.get(fieldname);
                        jarrRecords = sortJsonArrayOnTransaction(jarrRecords,(String)measureSortObj.get("measureFieldID"),(String)measureSortObj.get("sortOrder"));
                    }
                }
                totalCount = accCustomerReportServiceDao.executeCustomReportCountSQL(countquery, paramList);
            }
            //For measurable fields
            for (int z = 0; z < measureFieldsJsonArray.length(); z++) {
                JSONArray mergedJSONArray = new JSONArray();
                JSONObject invidualmeasurejsonobj = measureFieldsJsonArray.getJSONObject(z);

                JSONArray measurejarrRecords = new JSONArray();//all the data will be present in this jsonarray
                JSONArray measuresjsonArray = new JSONArray(invidualmeasurejsonobj.optString("measurefieldjsonArray", "[]"));
                String measuresqlquery = null;
                boolean isDiscountPresentnewflag = false, isLineItemnewflag = false, isAnyQunatityPresentnewflag = false, isQtyLineItemnewflag = false;
                jarrColumns = new JSONArray();
                JSONObject newdataIndexObject = new JSONObject();
                dataIndexTableName = "";
                columnmap = new HashMap<String, String>();
                paramList = new ArrayList();

                for (int x = 0; x < measuresjsonArray.length(); x++) {
                    JSONObject jObj = measuresjsonArray.getJSONObject(x);
                    Map returnMap = buildcolumnDetailsCustomReportPreview(jObj, columnmap, x, newdataIndexObject, jarrColumns, moduleId, dataIndexTableName, isDiscountPresentnewflag, isLineItemnewflag, isAnyQunatityPresentnewflag, isQtyLineItemnewflag,isDisplayUOMPresent);
                    x = (Integer) returnMap.get("cnt");
                    isLineItemnewflag = (Boolean) returnMap.get("isLineItem");
                    isDiscountPresentnewflag = (Boolean) returnMap.get("isDiscountPresent");
                    isQtyLineItemnewflag = (Boolean) returnMap.get("isQtyLineItem");
                    isAnyQunatityPresentnewflag = (Boolean) returnMap.get("isAnyQunatityPresent");
                    newdataIndexObject = (JSONObject) returnMap.get("dataIndexObject");
                    dataIndexTableName = (String) returnMap.get("dataIndexTableName");
                    jarrColumns = (JSONArray) returnMap.get("jarrColumns");
                    columnmap = (HashMap<String, String>) returnMap.get("columnmap");
                }
                int precision;
                if (measuresjsonArray.length() != 0) {
                    Map measquerymap = buildCustomReportSqlQuery(measuresjsonArray, moduleId, false, valueMap, paramList,dataIndexObject);
                    //                measquerymap = getFilterQuery(measquerymap, filterarray, newdataIndexObject);
                    measuresqlquery = String.valueOf(measquerymap.get(CustomReportConstants.sqlquery));
                    countquery = measuresqlquery ;
                    measuresqlquery = measuresqlquery + " LIMIT " + valueMap.get("limit") + " OFFSET " + valueMap.get("start");
                    reportDataResultSet = accCustomerReportServiceDao.executeCustomReportSQL(measuresqlquery, paramList);
                    processResultSetToJSON(moduleId, reportDataResultSet, currencyid, valueMap, jarrColumns, measurejarrRecords);
                    int measureCount = accCustomerReportServiceDao.executeCustomReportCountSQL(countquery, paramList);
                    totalCount=measureCount;

                    String expression = invidualmeasurejsonobj.optString("expression");
                    precision = invidualmeasurejsonobj.getJSONObject("properties").getJSONObject("source").optInt("precision",2);
                    JSONObject returnjobj = CustomReportHandler.executeRegularExpression(measurejarrRecords, expression, invidualmeasurejsonobj.optString(Constants.Acc_id),precision);
                    measurejarrRecords = returnjobj.getJSONArray(Constants.data);

                    if (jarrRecords.length() > 0) {//if measures field are inserted and not other fields
                        for (int k = 0; k < jarrRecords.length(); k++) {
                            JSONObject colsrec = jarrRecords.getJSONObject(k);
                            for (int p = 0; p < measurejarrRecords.length(); p++) {
                                JSONObject mobj = measurejarrRecords.getJSONObject(p);
                                if (!StringUtil.isNullOrEmpty(mobj.optString(Constants.billid)) && !StringUtil.isNullOrEmpty(colsrec.optString(Constants.billid)) && mobj.optString(Constants.billid).equalsIgnoreCase(colsrec.optString(Constants.billid))) {//                                kobj
                                    String key = invidualmeasurejsonobj.optString(Constants.Acc_id);
                                    colsrec.put(key, mobj.get(key));
                                    break;
                                }
                            }
                            mergedJSONArray.put(colsrec);
                        }
                    } else {
                        mergedJSONArray = measurejarrRecords;
                    }
                } else {
                    precision = invidualmeasurejsonobj.getJSONObject("properties").getJSONObject("source").optInt("precision",2);
                    String expression = invidualmeasurejsonobj.optString("expression");
                    String measureID = invidualmeasurejsonobj.optString(Constants.Acc_id);
                    CustomReportHandler.executeRegularExpression(jarrRecords, expression, measureID,precision);
                    mergedJSONArray = jarrRecords;
                }

                jarrRecords = mergedJSONArray;//at the end assigning merged data to final array
            }//end of for measureFieldsJsonArray

            //jresult.put("dataIndexArray",dataIndexJSONArray); 
            jresult.put("success", true);
            jresult.put("dataIndexObject", dataIndexObject);
            jresult.put("totalCount", totalCount);
            jresult.put("data", jarrRecords);
            jresult.put("sortConfigArray", sortClause.get("sortConfigArray"));
        }catch (ParseException | JSONException | UnsupportedEncodingException | ServiceException  ex ) {
            jresult.put("Exception", ex.getCause());
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jresult;
    }

    @Override
    /*Create JsONARRAY On the basis of showing rowlevel or not*/
    public Map showRowLevelFieldsJSONArray(JSONArray selectedRowsJSONData, boolean showRowLevelFieldsflag) throws ServiceException {
        JSONArray jarrColumns = new JSONArray();
        Map<String, Object> valuesmap = new HashMap<String, Object>();
        try {
            if (showRowLevelFieldsflag) {
                for (int cnt = 0; cnt < selectedRowsJSONData.length(); cnt++) {
                    JSONObject jObj = selectedRowsJSONData.getJSONObject(cnt);
                    if (jObj.optBoolean("showasrowexpander", false) == false) {
                        continue;
                    } else {
                        jarrColumns.put(jObj);
                    }
                }
            } else {
                for (int cnt = 0; cnt < selectedRowsJSONData.length(); cnt++) {
                    JSONObject jObj = selectedRowsJSONData.getJSONObject(cnt);
                    if (jObj.optBoolean("showasrowexpander", false) == true) {
                        continue;
                    } else {
                        jarrColumns.put(jObj);
                    }
                }
            }
        } catch (Exception e) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            valuesmap.put("jarrColumns", jarrColumns);
        }
        return valuesmap;
    }

    @Override
    public JSONArray getCustomReportMeasureFieldJsonArray(JSONArray ColumnConfigArr, String moduleid,JSONObject paramJobj) throws ServiceException {
        JSONArray jarrGlobaldefaultFieldColumns = new JSONArray();
        JSONArray jarrLineItemColumnFields = new JSONArray();
        JSONObject response = new JSONObject();
        try {
            boolean isUnitPricenotActivated = false;
            ExtraCompanyPreferences extraCompanyPreferences = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), paramJobj.getString(Constants.companyKey));
            if (extraprefresult != null && !extraprefresult.getEntityList().isEmpty() && extraprefresult.getEntityList().get(0) != null) {
                extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            }

            boolean unitPriceinDO = extraCompanyPreferences.isUnitPriceInDO();
            boolean unitPriceinGR = extraCompanyPreferences.isUnitPriceInGR();
            boolean unitPriceinSR = extraCompanyPreferences.isUnitPriceInSR();
            boolean unitPriceinPR = extraCompanyPreferences.isUnitPriceInPR();

            if ((moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Sales_Return_ModuleId)) && !unitPriceinSR) || (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Delivery_Order_ModuleId)) && !unitPriceinDO)
                    || (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Goods_Receipt_ModuleId)) && !unitPriceinGR)
                    || (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Purchase_Return_ModuleId)) && !unitPriceinPR)) {
                isUnitPricenotActivated = true;
            }

            for (int i = 0; i < ColumnConfigArr.length(); i++) {
                JSONObject jobj = ColumnConfigArr.getJSONObject(i);
                jarrGlobaldefaultFieldColumns = jobj.getJSONArray(Constants.globalFields);
                jarrLineItemColumnFields = jobj.getJSONArray(Constants.lineItemFields);
                KwlReturnObject result = null;
                HashMap<String, Object> map = new HashMap<String, Object>();
                map.put(Constants.filter_names, Arrays.asList("measurefieldmodule.id", "allowinotherapplication"));
                map.put(Constants.filter_values, Arrays.asList(moduleid, true));
                result = accCustomerReportServiceDao.getMeasureFields(map);
                if (result != null) {
                    List<AccCustomReportsMeasuresFields> list = result.getEntityList();
                    try {
                        for (AccCustomReportsMeasuresFields crmf : list) {
                            if (isUnitPricenotActivated && Constants.EnableDisableUnitPriceFields.contains(crmf.getMeasurefieldid())) {
                                continue;
                            } else {
                                JSONObject jobject = new JSONObject();
                                jobject.put(Constants.Acc_fieldid, crmf.getMeasurefieldid());
                                jobject.put(Constants.fieldtype, crmf.getXtype() != null ? Integer.parseInt(crmf.getXtype()) : 1); //TODO advance serch on combo,number,date fields
                                jobject.put(Constants.fieldlabel, crmf.getMeasurefielddisplayname());
                                jobject.put(Constants.islineitem, false);
                                jobject.put(Constants.iscustomflag, false);
                                jobject.put(Constants.moduleid, crmf.getMeasurefieldmodule().getId());
                                jobject.put(Constants.xtype, crmf.getXtype() != null ? Integer.parseInt(crmf.getXtype()) : 1);
                                jobject.put(Constants.dataindex, crmf.getDataIndex() != null ? crmf.getDataIndex() : "");
                                jobject.put(Constants.isreadonly, crmf.isIsreadonly());
                                jobject.put(Constants.ismandatory, false);
                                jarrGlobaldefaultFieldColumns.put(jobject);
                            }
                        }
                    } catch (JSONException ex) {
                        Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        } catch (Exception e) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            try {
                response.put(Constants.globalFields, jarrGlobaldefaultFieldColumns);
                response.put(Constants.lineItemFields, jarrLineItemColumnFields);
                ColumnConfigArr = new JSONArray();
                ColumnConfigArr.put(response);
            } catch (JSONException ex) {
            }

        }
        return ColumnConfigArr;
    }

    /**
     * Get the preferences for Date/TimeZone-format/Currency
     *
     * @param request
     * @param userTimeFormat
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    @Override
    public JSONObject getPreferences(HashMap<String, Object> request, String userTimeFormat) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String dateformat = "";
        KwlReturnObject list = accCustomerReportServiceDao.getPreferences(request);

        List list1 = list.getEntityList();
        KWLTimeZone timeZone = (KWLTimeZone) list1.get(0);
        KWLDateFormat dateFormat = (KWLDateFormat) list1.get(1);
        KWLCurrency currency = (KWLCurrency) list1.get(2);
        if (userTimeFormat.equals("1")) {
            dateformat = dateFormat.getScriptForm().replace('H', 'h');
            if (!dateformat.equals(dateFormat.getScriptForm())) {
                dateformat += " T";
            }
        } else {
            dateformat = dateFormat.getJavaForm();
        }
        try {
            jobj.put("Timezone", timeZone.getName());
            jobj.put("Timezoneid", timeZone.getTimeZoneID());
            jobj.put("Timezonediff", timeZone.getDifference());
            jobj.put("DateFormat", dateformat);
            jobj.put("DateFormatid", dateFormat.getFormatID());
            jobj.put("seperatorpos", dateFormat.getScriptSeperatorPosition());
            jobj.put("Currency", currency.getHtmlcode());
            jobj.put("CurrencyName", currency.getName());
            jobj.put("CurrencySymbol", currency.getSymbol());
            jobj.put("Currencyid", currency.getCurrencyID());
        } catch (JSONException e) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return jobj;
    }

    /**
     * Check if the Report with the given name already exists or not
     *
     * @param requestParams
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    @Override
    public boolean isCustomReportNameExists(HashMap<String, Object> requestParams) throws ServiceException {

        boolean isReportNameExists = false;
        try {
            KwlReturnObject kRetObj = accCustomerReportServiceDao.getCustomReportByNameAndCompanyId(requestParams);
            if (kRetObj.getRecordTotalCount() > 0) {
                isReportNameExists = true;
            }
        } catch (Exception e) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return isReportNameExists;

    }

    /**
     * Check if the Report with the given report no already exists or not so
     * that report can be saved or updated accordingly
     *
     * @param requestParams
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     */
    @Override
    public boolean isCustomReportExists(HashMap<String, Object> requestParams) throws ServiceException {

        boolean isReportExists = false;
        try {
            KwlReturnObject kRetObj = accCustomerReportServiceDao.getCustomReportByReportNoAndCompanyId(requestParams);
            if (kRetObj.getRecordTotalCount() > 0) {
                if (requestParams.containsKey("reportName")) {
                    ReportMaster accCustomReports = (ReportMaster) kRetObj.getEntityList().get(0);
                    if (accCustomReports.getName().equals((String) requestParams.get("reportName"))) {
                        isReportExists = true;
                    } else {
                        isReportExists = false;
                    }
                } else {
                    isReportExists = true;
                }
            }
        } catch (ServiceException e) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return isReportExists;

    }

    /**
     * This method deletes saved reports
     *
     * @param valueMap
     * @return boolean
     * @exception ServiceException .
     * @see ServiceException
     */
    @Override
    public boolean deleteCustomReport(HashMap<String, Object> valueMap) throws ServiceException {

        boolean isReportDeleted = false;
        String reportIDs = (String) valueMap.get("reportIds");
        String reportIDsWithQuotes = "";
        if (reportIDs != null) {
            String[] reportIDArray = reportIDs.split(",");
            if (reportIDArray.length > 0) {
                for (int i = 0; i < reportIDArray.length; i++) {
                    reportIDsWithQuotes += "'" + reportIDArray[i] + "',";
                }
                if (reportIDsWithQuotes.lastIndexOf(",") > -1) {
                    reportIDsWithQuotes = reportIDsWithQuotes.substring(0, reportIDsWithQuotes.lastIndexOf(","));
                }
            }
            valueMap.put("reportIds", reportIDsWithQuotes);
        }
        try {
            isReportDeleted = accCustomerReportServiceDao.deleteCustomReport(valueMap);

        } catch (Exception e) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return isReportDeleted;

    }

    @Override
    public String getDateColumn(String mainTable) throws ServiceException {

        String dateColumn = "";
        try {
            switch (mainTable) {
                case CustomReportConstants.Acc_Sales_Order:
                    dateColumn = mainTable + "." + CustomReportConstants.OrderDate;
                    break;
                case CustomReportConstants.Acc_Purchase_Order:
                    dateColumn = mainTable + "." + CustomReportConstants.OrderDate;
                    break;
                case CustomReportConstants.Acc_Sales_Invoice:
                    dateColumn = mainTable + "." + CustomReportConstants.CreationDate;
                    break;
                case CustomReportConstants.Acc_Vendor_Invoice:
                    //dateColumn = mainTable + "." + CustomReportConstants.CreationDate;
                    dateColumn = CustomReportConstants.JournalEntryTable + "." + CustomReportConstants.EntryDate;
                    break;
                case CustomReportConstants.Acc_Goods_Receipt:
                    dateColumn = mainTable + "." + CustomReportConstants.GRORDERDATE;
                    break;
                case CustomReportConstants.Acc_Debit_Note:
                case CustomReportConstants.Acc_Credit_Note:
                    //dateColumn = CustomReportConstants.JournalEntryTable+((intCntVal==0)?"":(Integer.parseInt(cntVal)+1)) + "." + CustomReportConstants.EntryDate;
                    dateColumn = CustomReportConstants.JournalEntryTable + "." + CustomReportConstants.EntryDate;
                    break;
                case CustomReportConstants.Acc_Payment:
                case CustomReportConstants.Acc_Receipt:
                    dateColumn = CustomReportConstants.JournalEntryTable + "." + CustomReportConstants.EntryDate;
                    break;
                case CustomReportConstants.Acc_Quotation:
                    dateColumn = mainTable + "." + CustomReportConstants.Acc_Quotation_Date;
                    break;

                case CustomReportConstants.Acc_CUSTOMER:
                    dateColumn = mainTable + "." + CustomReportConstants.Acc_Customer_CreatedDate;
                    break;
                case CustomReportConstants.Acc_Purchase_Requisition:
                    dateColumn = mainTable + "." + CustomReportConstants.Acc_Purchase_Requisition_Date;
                    break;
                default:
                    dateColumn = mainTable + "." + CustomReportConstants.OrderDate;
                    break;
            }
        } catch (Exception e) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return dateColumn;
    }

    public static String getmoduledataRefNameForCustomField(int moduleid) {
        String module = "";
        switch (moduleid) {
            case Constants.Acc_Sales_Order_ModuleId:
                module = CustomReportConstants.SalesOrder_DetailCustomData_Query;
                break;
            case Constants.Acc_Purchase_Order_ModuleId:
                module = CustomReportConstants.PurchaseOrder_DetailCustomData_Query;
                break;
            case Constants.Acc_Invoice_ModuleId:
                module = CustomReportConstants.Invoice_DetailCustomData_Query;
                break;
            case Constants.Acc_Sales_Return_ModuleId:
                module = CustomReportConstants.SalesReturn_DetailCustomData_Query;
                break;
            case Constants.Acc_Purchase_Return_ModuleId:
                module = CustomReportConstants.PurchaseReturn_DetailCustomData_Query;
                break;
            case Constants.Acc_Goods_Receipt_ModuleId:
                module = CustomReportConstants.GoodsReceipt_DetailCustomData_Query;
                break;
            case Constants.Acc_Delivery_Order_ModuleId:
                module = CustomReportConstants.DeliveryOrder_DetailCustomData_Query;
                break;
            case Constants.Acc_Debit_Note_ModuleId:
                module = CustomReportConstants.Invoice_DetailCustomData_Query;
                break;
            case Constants.Acc_Credit_Note_ModuleId:
                module = CustomReportConstants.Invoice_DetailCustomData_Query;
                break;
            case Constants.Acc_Vendor_Invoice_ModuleId:
                module = CustomReportConstants.Invoice_DetailCustomData_Query;
                break;
            case Constants.Acc_Make_Payment_ModuleId:
            case Constants.Acc_Receive_Payment_ModuleId:
                module = CustomReportConstants.Invoice_DetailCustomData_Query;
                break;
            case Constants.Acc_Customer_ModuleId:
                module = CustomReportConstants.Customer_CustomData_Query;
                break;
            case Constants.Acc_Customer_Quotation_ModuleId:
                module = CustomReportConstants.Quotation_CustomData_Query;
                break;
            case Constants.Acc_Product_Master_ModuleId:
                module = "accproductcustomdata";
                break;
            case Constants.Acc_Purchase_Requisition_ModuleId:
                module = CustomReportConstants.PurchaseRequisition_DetailCustomData_Query;
                break;
            case Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId:
                module= CustomReportConstants.Invoice_DetailCustomData_Query;
                break;
            case Constants.Acc_FixedAssets_DisposalInvoice_ModuleId:
                module= CustomReportConstants.Invoice_DetailCustomData_Query;
                break; 
            case Constants.Acc_FixedAssets_Sales_Return_ModuleId:
                module = CustomReportConstants.SalesReturn_DetailCustomData_Query;
                break;
            case Constants.Acc_FixedAssets_Purchase_Return_ModuleId:
                module = CustomReportConstants.PurchaseReturn_DetailCustomData_Query;
                break;
        }
        return module;
    }

    public String getFieldComboDataValue(String value) throws ServiceException {
        String fieldComboValue = "";
        if (value != null) {
            String[] values = value.split(",");
            if (values.length > 0) {
                for (int valcnt = 0; valcnt < values.length; valcnt++) {
                    KwlReturnObject rdresult = accCustomerReportServiceDao.getObject(FieldComboData.class.getName(), values[valcnt]);
                    FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                    if (fieldComboData != null) {
                        fieldComboValue += fieldComboData.getValue() + ",";
                    }

                }
                if (fieldComboValue.lastIndexOf(",") > 0) {
                    fieldComboValue = fieldComboValue.substring(0, fieldComboValue.lastIndexOf(","));
                }
            }
        }
        return fieldComboValue;

    }

    public Map getFilterQuery(Map querymap, JSONArray filterarray, JSONObject dataIndexObject) throws ServiceException, JSONException, ParseException {

        HashMap<String, Object> queryMap = new HashMap<String, Object>();
//        String sqlquery = String.valueOf(querymap.get(CustomReportConstants.sqlquery)).toLowerCase();
        String sqlquery =querymap.get(CustomReportConstants.sqlquery)!=null? String.valueOf(querymap.get(CustomReportConstants.sqlquery)):"";
        String countquery = querymap.get(CustomReportConstants.countquery)!=null? String.valueOf(querymap.get(CustomReportConstants.countquery)):"";
        for (int cnt = 0; cnt < filterarray.length(); cnt++) {
            JSONObject filterObj = filterarray.getJSONObject(cnt);
            boolean isCustom = dataIndexObject.getJSONObject(filterObj.getString(CustomReportConstants.FILTER_PROPERTY)).optBoolean("iscustom", false);
            boolean allowCrossModuleFlag = filterObj.optBoolean("allowcrossmodule", false);
            String crossJoinMainTable = filterObj.optString("crossJoinMainTable", "");
            String moduleId = dataIndexObject.getJSONObject(filterObj.getString(CustomReportConstants.FILTER_PROPERTY)).getString("moduleId");
            String xtype = dataIndexObject.getJSONObject(filterObj.getString(CustomReportConstants.FILTER_PROPERTY)).getString("xtype");
            String filterColumn = dataIndexObject.getJSONObject(filterObj.getString(CustomReportConstants.FILTER_PROPERTY)).getString(CustomReportConstants.filtercolumnname);
            if (allowCrossModuleFlag) {
                String[] filterColumnArray = filterColumn.split("\\.");
                if (filterColumnArray != null && filterColumnArray.length > 0) {

                    filterColumn = crossJoinMainTable + "." + filterColumnArray[1];
                }
            }
            String operator = filterObj.getString(CustomReportConstants.FILTER_OPERATOR);
            String value = filterObj.optString(CustomReportConstants.FILTER_VALUE, "");
            String end_value = "";
            if(value.contains(Constants.CURRENT_USER)){
                value = value.replace(Constants.CURRENT_USER, (String)querymap.get("userId"));
                        
            }
            //get Db column name for default combos from main table
            if (!isCustom && xtype.equals(CustomReportConstants.ComboFieldId)) {
                filterColumn = accCustomerReportServiceDao.getDefaultFieldDBColumnName(filterObj.getString(CustomReportConstants.FILTER_PROPERTY), moduleId);
            }
            //Reverted isCustom Condfition as Custom Fields are now converted into dates and not storing time in ms
            if (!isCustom && xtype.equals(CustomReportConstants.DateFieldId) && (filterColumn.contains("createdon")||filterColumn.contains("updatedon")) && !dataIndexObject.getJSONObject(filterObj.getString(CustomReportConstants.FILTER_PROPERTY)).optString("moduleId","").equals(Constants.CUSTOMER_MODULE_UUID)) {   // claculate long value for custom date fields.
                value += " 00:00:00";
                end_value = value.replace("00:00:00", "23:59:59");
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date startD = formatter.parse(value);
                Date endD = formatter.parse(end_value);
                value = String.valueOf(startD.getTime());
                end_value = "'" + String.valueOf(endD.getTime()) + "'"; //If filter is applied on "On" condition then calculate long value from date +"00:00:00" to date+"23:59:00"

                if (operator.equalsIgnoreCase("gt")) {
                    value = String.valueOf(endD.getTime());
                }
            }

            if (!StringUtil.isNullOrEmpty(value) && !value.equals("[]")) {

                switch (operator) {
                    case CustomReportConstants.FILTER_gt:
                        operator = CustomReportConstants.FILTER_gtSymbol;
                        if (xtype.equals(CustomReportConstants.NumberFieldId)) {
                            value = value + " and " + filterColumn + " !=\"\"";
                        } else {
                            value = "'" + value + "'";
                        }
                        break;
                    case CustomReportConstants.FILTER_lt:
                        operator = CustomReportConstants.FILTER_ltSymbol;
                        if (xtype.equals(CustomReportConstants.NumberFieldId)) {
                            value = value + " and " + filterColumn + " !=\"\"";
                        } else {
                            value = "'" + value + "'";
                        }
                        break;
                    case CustomReportConstants.FILTER_eq:
                        operator = CustomReportConstants.FILTER_eqSymbol;
                        if (xtype.equals(CustomReportConstants.NumberFieldId)) {
                            value = value + " and " + filterColumn + " !=\"\"";
                        } else {
                            value = "'" + value + "'";
                        }
                        break;
                    case CustomReportConstants.FILTER_eqSymbol:
                        if (value.equalsIgnoreCase("false")) {
                            if(!isCustom) {
                                value = "'" + (value.equalsIgnoreCase("false")?"F":"T") + "'" + " or " + filterColumn + " is null ";   
                            } else {
                            value = "'" + value + "'" + " or " + filterColumn + " is null ";    //get records having value as false as well as records which doesn't have value for searching records for false value. Records which doesn't have values are treated as false value for checkbox column
                            }
                        } else {
                            //Added this code as Customer Module has creation date ended with timestamp(00:00:00)
                            if(dataIndexObject.getJSONObject(filterObj.getString(CustomReportConstants.FILTER_PROPERTY)).optString("moduleId","").equals(Constants.CUSTOMER_MODULE_UUID)) {
                                value = "'" + value + "%'";
                            } else {
                                if (!isCustom) {
                                    value = "'" + (value.equalsIgnoreCase("false") ? "F" : "T") + "'" ;
                                } else {
                                value = "'" + value + "'";
                            }
                        }
                        }
                        break;
                    case CustomReportConstants.FILTER_like:
                        value = "'%" + value + "%'";
                        break;
                    case CustomReportConstants.FILTER_in:
                        value = value.replace("[", "");
                        value = value.replace("]", "");
                        String searchValues[] = value.split(",");
                        String tempValue = "";
                        for (int i = 0; i < searchValues.length; i++) {
                            String searchValue = searchValues[i].replaceAll("\"", "");
                            if (i == searchValues.length - 1) {
                                tempValue += filterColumn + " like '%" + searchValue + "%'  ";
                            } else {
                                tempValue += filterColumn + " like '%" + searchValue + "%' or ";
                            }
                        }
                        value = tempValue;
                        break;
                }

                if(StringUtil.isNullOrEmpty(end_value)){
                    end_value=value;
                }
                    
                if (!isCustom && xtype.equals(CustomReportConstants.DateFieldId) && operator.equals(CustomReportConstants.FILTER_eqSymbol) && !dataIndexObject.getJSONObject(filterObj.getString(CustomReportConstants.FILTER_PROPERTY)).optString("moduleId","").equals(Constants.CUSTOMER_MODULE_UUID)) {
                    sqlquery += " and (" + filterColumn + " BETWEEN " + value + " and " + end_value + " )";                //If filter is applied on date field for "On" condition then calculate long value from date +"00:00:00" to date+"23:59:00"
                    countquery += " and (" + filterColumn + " BETWEEN " + value + " and " + end_value + " )"; 
                } else if (operator.equalsIgnoreCase(CustomReportConstants.FILTER_in)) {
                        sqlquery += " and (" + value + " )";
                        countquery += " and (" + value + " )";
                } else {
                    sqlquery += " and (" + filterColumn + Constants.space + operator + Constants.space + value + ")";
                    countquery += " and (" + filterColumn + Constants.space + operator + Constants.space + value + ")";
                }
            }
        }
        queryMap.put(CustomReportConstants.sqlquery, sqlquery);
        queryMap.put(CustomReportConstants.countquery, countquery);
        return queryMap;
    }

    @Override
    public JSONObject updateCustomReportNameAndDescription(HashMap<String, Object> valueMap) throws ServiceException {

        KwlReturnObject result = null;
        JSONObject jresult = new JSONObject();
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        ReportMaster accCustomReport = accCustomerReportServiceDao.fetchCustomReportDetails((String) valueMap.get("reportNo"));
        if (accCustomReport != null) {
            //accCustomReport = (Customreports)result.getEntityList().get(0);
            //accCustomReport.setReportno((String) valueMap.get("reportNo"));
            boolean isreportNameFieldEdited = Boolean.parseBoolean((String) valueMap.get("isreportNameFieldEdited"));
            DateFormat df2 = new SimpleDateFormat((String) valueMap.get("userDateFormat"));
            requestParams.put("reportName", (String) valueMap.get("reportNewName"));
            requestParams.put("companyID", (String) valueMap.get("companyID"));
            requestParams.put("userId", (String) valueMap.get("userId"));
            //paramList.add((String) valueMap.get("companyID"));
            try {
                if (isreportNameFieldEdited && isCustomReportNameExists(requestParams)) {
                    jresult.put("success", false);
                    jresult.put(CustomReportConstants.MESSAGE_KEY, "acc.CustomReport.dupReportNameEdit");
                } else {
                    accCustomReport.setName((String) valueMap.get("reportNewName"));
                    accCustomReport.setDescription((String) valueMap.get("reportNewDesc"));
                    long updatedDate = new java.util.Date().getTime();
                    accCustomReport.setUpdatedon(updatedDate);
                    if (valueMap.containsKey("browsertz") && valueMap.get("browsertz") != null && !StringUtil.isNullOrEmpty(valueMap.get("browsertz").toString())) {
                        df2.setTimeZone(TimeZone.getTimeZone("GMT" + valueMap.get("browsertz")));
                    }
                    result = accCustomerReportServiceDao.updateCustomReportNameAndDescription(accCustomReport, valueMap);
                    jresult.put("success", true);
                    jresult.put("updatedon", df2.format(new java.util.Date(updatedDate)));
                    if (isreportNameFieldEdited) {
                        jresult.put(CustomReportConstants.MESSAGE_KEY, "acc.CustomReport.reportNameEditSuccess");
                    } else {
                        jresult.put(CustomReportConstants.MESSAGE_KEY, "acc.CustomReport.reportDescEditSuccess");
                    }

                }
            } catch (JSONException ex) {
                Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jresult;

    }

    public JSONObject getFieldsProperties(JSONObject jobj) throws ServiceException, JSONException {
        JSONObject retArr = new JSONObject();
        JSONObject sourceObj = new JSONObject();
        JSONObject tempObj = new JSONObject();
        JSONObject sourceConfig = new JSONObject();
        String xtype = jobj.getString("xtype");
        String defaultHeader = jobj.getString("defaultHeader");
        String columnType = jobj.optString("columntype");
        boolean isCustomField = jobj.optBoolean("customfield",false);
        //  Default Properties For all types of fields
        sourceObj.put("(Column Name)", jobj.getString("defaultHeader"));

        String alignProperty = "Left";
        switch (xtype) {
            case CustomReportConstants.NumberFieldId:
                alignProperty = "Right";
                break;
            case CustomReportConstants.DateFieldId:
                alignProperty = "Center";
                break;
        }
        sourceObj.put("align", alignProperty);
        tempObj = new JSONObject();
        tempObj.put("displayName", "Align Column");
        tempObj.put("editor", "{\"xtype\": \"combobox\","
                + "\"store\": [\"Left\", \"Center\", \"Right\"],"
                + "\"forceSelection\": true,"
                + "\"allowBlank\": false"
                + "}");
        sourceConfig.put("align", tempObj);

        if (xtype.equals(CustomReportConstants.NumberFieldId)) {                      //Properties for number fields

//            sourceObj.put("precision", isCustomField ? "0" : "2");
            sourceObj.put("precision", "2");        //ERP-30836
            tempObj = new JSONObject();
            tempObj.put("type", "number");
            tempObj.put("displayName", "Precision");
            tempObj.put("editor", "{\"xtype\": \"numberfield\",\"minValue\": 0,\"maxValue\":11}");
//           tempObj.put("renderer","function(value){return \"<div style='color:red;'>\"+value+\"</div>\";}");             
            sourceConfig.put("precision", tempObj);

            if (!defaultHeader.equals(CustomReportConstants.Quantity) && !defaultHeader.equals(CustomReportConstants.BaseQuantity) && !defaultHeader.equals(CustomReportConstants.BALANCE_QUANTITY)&& !defaultHeader.contains(CustomReportConstants.TAX_PERCENT) ) {
                sourceObj.put("renderer", "Number");
                tempObj = new JSONObject();
                tempObj.put("displayName", "Renderer");
                tempObj.put("editor", "{\"xtype\": \"combobox\","
                        + "\"store\": [\"Number\", \"Transaction Currency\", \"Base Currency\"],"
                        + "\"forceSelection\": true,"
                        + "\"allowBlank\": false"
                        + "}");
                sourceConfig.put("renderer", tempObj);
            }
        } else if (xtype.equals(CustomReportConstants.TextFieldId) && columnType.equalsIgnoreCase("Default Fields")) {
            sourceObj.put("renderer", "None");
            tempObj = new JSONObject();
            tempObj.put("displayName", "Renderer");
            tempObj.put("editor", "{\"xtype\": \"combobox\","
                    + "\"store\": [\"None\", \"Link Renderer\"],"
                    + "\"forceSelection\": true,"
                    + "\"allowBlank\": false"
                    + "}");
            sourceConfig.put("renderer", tempObj);
        }
        //Added the Property for Sorting Order and Sequence
        tempObj = new JSONObject();
        tempObj.put("displayName", "Sorting Order");
        tempObj.put("editor", "{\"xtype\": \"combobox\","
                + "\"store\": [\"None\", \"ASC\", \"DESC\"],"
                + "\"forceSelection\": true,"
                + "\"allowBlank\": false"
                + "}");
        sourceConfig.put("sortOrder", tempObj);
        sourceObj.put("sortOrder", "None");

        retArr.put("source", sourceObj);
        retArr.put("sourceConfig", sourceConfig);
        return retArr;
    }

    public KWLCurrency getCurrencyValue(String currencyid) throws ServiceException {
        KWLCurrency kwlcurrency = null;
        if (currencyid != null) {
            KwlReturnObject curresult = accCustomerReportServiceDao.getObject(KWLCurrency.class.getName(), currencyid);
            kwlcurrency = (KWLCurrency) curresult.getEntityList().get(0);

        }
        return kwlcurrency;

    }

    @Override
    public double getTaxPercent(String companyid, Date transactiondate, String taxid) throws ServiceException {
        KwlReturnObject perresult = accCustomerReportServiceDao.getTaxPercent(companyid, transactiondate, taxid);
        double rowTaxPercent = (Double) perresult.getEntityList().get(0);
        return rowTaxPercent;
    }

    public double getCurrencyToBaseAmount(Map requestParams, Double amount, String currencyid, Date transactiondate, double rate) throws ServiceException {
        KwlReturnObject result = accCustomerReportServiceDao.getCurrencyToBaseAmount(requestParams, amount, currencyid, transactiondate, rate);
        double amountInBase = (Double) result.getEntityList().get(0);
        return amountInBase;
    }
    public String getPaymentVendorNames(String companyid, String paymentid) throws ServiceException, UnsupportedEncodingException {
        KwlReturnObject result = accCustomerReportServiceDao.getPaymentVendorNames(companyid, paymentid);
        List vNameList = result.getEntityList();
        Iterator vNamesItr = vNameList.iterator();
        String vendorNames = "";
        while (vNamesItr.hasNext()) {
            String tempName = URLEncoder.encode((String) vNamesItr.next(), "UTF-8");
            vendorNames += tempName;
            vendorNames += ",";
        }
        vendorNames = vendorNames.substring(0, Math.max(0, vendorNames.length() - 1));
        return vendorNames;
    }

    public String getReceiptCustomerNames(String companyid, String receiptid) throws ServiceException, UnsupportedEncodingException {
        KwlReturnObject result = accCustomerReportServiceDao.getReceiptCustomerNames(companyid, receiptid);
        List cNameList = result.getEntityList();
        String customerNames = "";
        for (Object object1 : cNameList) {
            String tempName = URLEncoder.encode((String) object1, "UTF-8");
            customerNames += tempName;
            customerNames += ",";
        }
        customerNames = customerNames.substring(0, Math.max(0, customerNames.length() - 1));
        return customerNames;
    }

    public List getRepaymentSheduleDetails(Map mapForRepaymentDetails) throws ServiceException {
        KwlReturnObject repaymentdetailresult = accCustomerReportServiceDao.getRepaymentSheduleDetails(mapForRepaymentDetails);
        List<RepaymentDetails> RDS = repaymentdetailresult.getEntityList();
        return RDS;
    }

    @Override
    public KwlReturnObject getProductPrice(String productid, boolean isPurchase, Date transactiondate, String affecteduser, String forCurrency) throws ServiceException {
        KwlReturnObject prdPriceRes = accCustomerReportServiceDao.getProductPrice(productid, isPurchase, transactiondate, affecteduser, forCurrency);
        return prdPriceRes;
    }

    @Override
    public KwlReturnObject getTax(Map<String, Object> requestParams) throws ServiceException {
        KwlReturnObject result = accCustomerReportServiceDao.getTax(requestParams);
        return result;
    }

    public void processResultSetToJSON(String moduleId, SqlRowSet reportDataResultSet, String currencyid, Map valueMap, JSONArray jarrColumns, JSONArray jarrRecords) throws ServiceException, JSONException, UnsupportedEncodingException, SessionExpiredException, ParseException {
        int count = 1;
        int recordCount = 1;
        Map processedData = new HashMap();
        StringBuilder termMappingsStb = null;
        Map<String, KwlReturnObject> lineLevelTermsMap = new HashMap<>();
        JSONArray reshuffledJarrColumns = new JSONArray();
        reshuffledJarrColumns = reshuffledSelectedField(jarrColumns);
        boolean isProcessValue = true, checkallowcrossmodule = false;
        String companyID = String.valueOf(valueMap.get("companyID"));
        DateFormat df2 = new SimpleDateFormat((String) valueMap.get("userDateFormat"));
        DateFormat udf = new SimpleDateFormat((String) valueMap.get("userDateFormat"));
        String crossJoinMainTable = String.valueOf(valueMap.get("crossJoinMainTable"));
        boolean isChartRequest = valueMap.get("isChartRequest") != null ? (Boolean) valueMap.get("isChartRequest") : false;
        boolean isEWayReportValidation = valueMap.get("isEWayReportValidation") != null ? (Boolean) valueMap.get("isEWayReportValidation") : false;
        Boolean exportInCaps = (valueMap.get("exportInCaps") == null) ? false : (Boolean) valueMap.get("exportInCaps");
//        boolean forExport = valueMap.get("forExport") != null ? (Boolean) valueMap.get("forExport") : false;
        String defaultPrecision = Integer.toString(Constants.AMOUNT_DIGIT_AFTER_DECIMAL);
        KWLCurrency kwlcurrency = getCurrencyValue(currencyid);
        String[] columnNames = reportDataResultSet.getMetaData().getColumnNames();
        for (int colNameLength = 0; colNameLength < columnNames.length; colNameLength++) {
            columnNames[colNameLength] = columnNames[colNameLength].toLowerCase().trim();
        }
        List<String> stringList = new ArrayList<String>(Arrays.asList(columnNames));
        List<String> processedinvoiceIds = new ArrayList<String>();
        JSONArray dataJArr = new JSONArray();
        if (!isResultSetEmpty(reportDataResultSet)) {
            int rowNum = 0;
//            String gstFieldColLabel =null; 
            List<String> lineLevelTerms = null;
             List<CustomReportsGSTFieldsLineLevelTermsMapping>gstFieldMappingObjList=new ArrayList<>();
            KwlReturnObject gstFieldMappingResult = null;
            JSONObject paramObj = new JSONObject();
            paramObj.put("companyid", companyID);
//            if (moduleId.equals(String.valueOf(Constants.Acc_Purchase_Return_ModuleId)) || moduleId.equals(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId)) || moduleId.equals(String.valueOf(Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId)) || moduleId.equals(String.valueOf(Constants.Acc_Purchase_Order_ModuleId))) {
//                paramObj.put("isInput", false);
//            } else if (moduleId.equals(String.valueOf(Constants.Acc_Sales_Return_ModuleId)) || moduleId.equals(String.valueOf(Constants.Acc_Invoice_ModuleId)) || moduleId.equals(String.valueOf(Constants.Acc_FixedAssets_DisposalInvoice_ModuleId))|| moduleId.equals(String.valueOf(Constants.Acc_Sales_Order_ModuleId))) {
//                paramObj.put("isInput", true);
//            }
            paramObj.put("onlyTermTaxName", true);
            KwlReturnObject kwlReturnObject = accTermObj.getGSTTermDetails(paramObj);
            lineLevelTerms = kwlReturnObject.getEntityList();
            SqlRowSetMetaData rsmd = reportDataResultSet.getMetaData();
            Map<String, String> gstColumnsMap = null;
            while (reportDataResultSet.next()) {
                rowNum++;
                JSONObject jarrRecord = new JSONObject();
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                boolean isCustomfield = false,allowcrossmodule=false;
                    //jarrRecord = new JSONObject();
                    String colLabel = rsmd.getColumnLabel(i);
                    if(gstColumnsMap == null && (colLabel.contains(CustomReportConstants.Acc_CGST+"#") || colLabel.contains(CustomReportConstants.Acc_SGST+"#") || colLabel.contains(CustomReportConstants.Acc_IGST+"#") || colLabel.contains(CustomReportConstants.Acc_UTGST+"#") || colLabel.contains(CustomReportConstants.Acc_CESS+"#")||colLabel.contains(CustomReportConstants.Acc_E_Way_TAX_Rate+"#"))){
                        gstColumnsMap = getGstColumnsMap(jarrColumns, rsmd);
                        termMappingsStb = new StringBuilder();
//                        gstFieldColLabel = colLabel;
//                        JSONObject paramObj = new JSONObject();
//                        paramObj.put("companyid", companyID);                        
//                        if (moduleId.equals(String.valueOf(Constants.Acc_Purchase_Return_ModuleId)) || moduleId.equals(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId))|| moduleId.equals(String.valueOf(Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId))) {
//                            paramObj.put("isInput", false);
//                        } else if (moduleId.equals(String.valueOf(Constants.Acc_Sales_Return_ModuleId)) || moduleId.equals(String.valueOf(Constants.Acc_Invoice_ModuleId))|| moduleId.equals(String.valueOf(Constants.Acc_FixedAssets_DisposalInvoice_ModuleId))) {
//                            paramObj.put("isInput", true);
//                        }                        
//                        paramObj.put("onlyTermTaxName", true);
//                        KwlReturnObject kwlReturnObject = accTermObj.getGSTTermDetails(paramObj);
//                        lineLevelTerms = kwlReturnObject.getEntityList();
                        for (int cntIndex = 0; cntIndex < jarrColumns.length(); cntIndex++) {
                            if (jarrColumns.getJSONObject(cntIndex).getString("defaultHeader").contains(CustomReportConstants.Acc_CGST) || jarrColumns.getJSONObject(cntIndex).getString("defaultHeader").contains(CustomReportConstants.Acc_SGST) || jarrColumns.getJSONObject(cntIndex).getString("defaultHeader").contains(CustomReportConstants.Acc_IGST) || jarrColumns.getJSONObject(cntIndex).getString("defaultHeader").contains(CustomReportConstants.Acc_UTGST) || jarrColumns.getJSONObject(cntIndex).getString("defaultHeader").contains(CustomReportConstants.Acc_CESS)||jarrColumns.getJSONObject(cntIndex).getString("defaultHeader").equalsIgnoreCase(CustomReportConstants.Acc_E_Way_TAX_Rate)) {
                                termMappingsStb.append("'").append(jarrColumns.getJSONObject(cntIndex).getString("id")).append("',");
                            }
                        }
                                gstFieldMappingResult = accCustomerReportServiceDao.getCustomReportsGSTFieldsLineLevelTermsMapping(termMappingsStb.substring(0, termMappingsStb.length()-1));
                                CustomReportsGSTFieldsLineLevelTermsMapping gstFieldMappingObj = null;
                                if (gstFieldMappingResult != null) {
                                    List<CustomReportsGSTFieldsLineLevelTermsMapping> gstfm = gstFieldMappingResult.getEntityList();
                                    Iterator gstfm_itr = gstfm.iterator();
                                    while (gstfm_itr.hasNext()) {
                                        gstFieldMappingObj = (CustomReportsGSTFieldsLineLevelTermsMapping) gstfm_itr.next();
                                         gstFieldMappingObjList.add(gstFieldMappingObj);
                                        JSONObject gstTermDetailsParamObj = new JSONObject();
                                        gstTermDetailsParamObj.put("companyid", companyID);
                                        gstTermDetailsParamObj.put("defaultTermId", gstFieldMappingObj.getDefaultTermsId());
                                        KwlReturnObject termMappingResult = null;
                                        if(lineLevelTermsMap.containsKey(gstFieldMappingObj.getDefaultTermsId())){
                                            termMappingResult = lineLevelTermsMap.get(gstFieldMappingObj.getDefaultTermsId());
                                        } else {
                                            termMappingResult = accTermObj.getGSTTermDetails(gstTermDetailsParamObj);
                                            lineLevelTermsMap.put(gstFieldMappingObj.getDefaultTermsId(), termMappingResult);
                                    }
                                }
                            }
                        }
                    String colType = rsmd.getColumnTypeName(i).replaceAll("#", "");
                    String columnLabel = rsmd.getColumnLabel(i).replaceAll("#", "");
                    String dbtablename = rsmd.getTableName(i).replaceAll("1", "");
                    KWLCurrency currency = null;
//                try {
                    if (count == (rsmd.getColumnCount() - 1) && !isChartRequest && checkallowcrossmodule && colLabel.equals("linkeddocid")) {
                        String linkedbillid = (String) reportDataResultSet.getObject(colLabel);
                        jarrRecord.put("linkedbillid", linkedbillid);
                    }
                    if (count == rsmd.getColumnCount() && !isChartRequest) {
                        String billid = (String) reportDataResultSet.getObject(colLabel);
                        jarrRecord.put("billid", billid);
                        jarrRecord.put("reportID", (String) valueMap.get("reportID"));
                        jarrRecord.put("recordCount", recordCount);
                        count = 1;
                        if (moduleId.equals(String.valueOf(Constants.Acc_Sales_Order_ModuleId))) {
                            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), billid);
                            SalesOrder salesOrder = (SalesOrder) objItr.getEntityList().get(0);
                            if (salesOrder.getCurrency() != null) {
                                currency = salesOrder.getCurrency();
                            } else {
                                currency = salesOrder.getCustomer().getAccount().getCurrency() == null ? kwlcurrency : salesOrder.getCustomer().getAccount().getCurrency();
                            }
                        } else if (moduleId.equals(String.valueOf(Constants.Acc_Purchase_Order_ModuleId))) {
                            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), billid);
                            PurchaseOrder purchaseOrder = (PurchaseOrder) objItr.getEntityList().get(0);
                            if (purchaseOrder.getCurrency() != null) {
                                currency = purchaseOrder.getCurrency();
                            } else {
                                currency = purchaseOrder.getVendor().getAccount().getCurrency() == null ? kwlcurrency : purchaseOrder.getVendor().getAccount().getCurrency();
                            }
                        } else if (moduleId.equals(String.valueOf(Constants.Acc_Invoice_ModuleId))) {
                            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Invoice.class.getName(), billid);
                            Invoice invoice = (Invoice) objItr.getEntityList().get(0);
                            if (invoice.getCurrency() != null) {
                                currency = invoice.getCurrency();
                            } else {
                                currency = invoice.getCustomer().getAccount().getCurrency() == null ? kwlcurrency : invoice.getCustomer().getAccount().getCurrency();
                            }
                        }else if (moduleId.equals(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId)) || moduleId.equals(String.valueOf(Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId))) {
                            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), billid);
                            GoodsReceipt goodsReceipt = (GoodsReceipt) objItr.getEntityList().get(0);
                            if (goodsReceipt.getCurrency() != null) {
                                currency = goodsReceipt.getCurrency();
                            } else {
                                currency = goodsReceipt.getVendor().getAccount().getCurrency() == null ? kwlcurrency : goodsReceipt.getVendor().getAccount().getCurrency();
                            }
                        } else if (moduleId.equals(String.valueOf(Constants.Acc_Sales_Return_ModuleId))) {
                            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(SalesReturn.class.getName(), billid);
                            SalesReturn salesReturn = (SalesReturn) objItr.getEntityList().get(0);
                            currency = salesReturn.getCurrency();
                        } else if (moduleId.equals(String.valueOf(Constants.Acc_Purchase_Return_ModuleId))) {
                            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(PurchaseReturn.class.getName(), billid);
                            PurchaseReturn purchaseReturn = (PurchaseReturn) objItr.getEntityList().get(0);
                            currency = purchaseReturn.getCurrency();
                        } else if (moduleId.equals(String.valueOf(Constants.Acc_Goods_Receipt_ModuleId))) {
                            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceiptOrder.class.getName(), billid);
                            GoodsReceiptOrder grOrder = (GoodsReceiptOrder) objItr.getEntityList().get(0);
                            if (grOrder.getCurrency() != null) {
                                currency = grOrder.getCurrency();
                            }
                        } else if (moduleId.equals(String.valueOf(Constants.Acc_Delivery_Order_ModuleId))) {
                            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), billid);
                            DeliveryOrder deliveryOrder = (DeliveryOrder) objItr.getEntityList().get(0);
                            if (deliveryOrder.getCurrency() != null) {
                                currency = deliveryOrder.getCurrency();
                            }
                        } else if (moduleId.equals(String.valueOf(Constants.Acc_Debit_Note_ModuleId))) {
                            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), billid);
                            DebitNote debitNote = (DebitNote) objItr.getEntityList().get(0);
                            if (debitNote.getCurrency() != null) {
                                currency = debitNote.getCurrency();
                            } else {
                                currency = debitNote.getCustomer().getAccount().getCurrency() == null ? kwlcurrency : debitNote.getCustomer().getAccount().getCurrency();
                            }
                        } else if (moduleId.equals(String.valueOf(Constants.Acc_Credit_Note_ModuleId))) {
                            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), billid);
                            CreditNote creditNote = (CreditNote) objItr.getEntityList().get(0);
                            if (creditNote.getCurrency() != null) {
                                currency = creditNote.getCurrency();
                            } else {
                                currency = creditNote.getCustomer().getAccount().getCurrency() == null ? kwlcurrency : creditNote.getCustomer().getAccount().getCurrency();
                            }
                        }else if (moduleId.equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId))) {
                            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Payment.class.getName(), billid);
                            Payment payment = (Payment) objItr.getEntityList().get(0);
                            if (payment.getCurrency() != null) {
                                currency = payment.getCurrency();
                            }
                        }else if (moduleId.equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {
                            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), billid);
                            Receipt payment = (Receipt) objItr.getEntityList().get(0);
                            if (payment.getCurrency() != null) {
                                currency = payment.getCurrency();
                            }
                        }else if (moduleId.equals(String.valueOf(Constants.Acc_Customer_Quotation_ModuleId))) {
                            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Quotation.class.getName(), billid);
                            Quotation payment = (Quotation) objItr.getEntityList().get(0);
                            if (payment.getCurrency() != null) {
                                currency = payment.getCurrency();
                            }
                        }
                        jarrRecord.put("currencysymbol", currency != null ? currency.getSymbol() : "");
                        jarrRecord.put("currencycode", currency != null ? currency.getCurrencyCode() : "");
                        continue;
                    } else {
                        for (int j = 0; j < jarrColumns.length(); j++) {
                            if (jarrColumns.getJSONObject(j).getString("defaultHeader").equalsIgnoreCase(columnLabel)) {
                            String value = "";
                            String month="";
                            int day=0;
                            int year=0;
                            String dateForSortIndex = null;     //to be used as sortIndex for date fields in pivot configuration
                            int index = 0;
                            int precision = 2;
                            //int xType = StringUtil.getInteger(jarrColumns.getJSONObject(i - 1).getString("xtype"));
                            //isCustomfield = StringUtil.getBoolean(jarrColumns.getJSONObject(i - 1).getString("custom"));
                            // ERP-33659 && ERP-33664 Measure fields need transaction ids to get the values from them, After reshuffling index of measure fields in user selected sequence
                            //was differing from that in resultset( reshuffled sequence). Hence, to get the transaction id for the measure fields below logic is written
                            int measureFieldReshuffledIndex = 0;
                            for (measureFieldReshuffledIndex = 0; measureFieldReshuffledIndex <= jarrColumns.length(); measureFieldReshuffledIndex++) {
                                if (jarrColumns.getJSONObject(j).getString("defaultHeader").equals(reshuffledJarrColumns.getJSONObject(measureFieldReshuffledIndex).getString("defaultHeader"))) {
                                    break;
                                }
                            }
                            int xType = StringUtil.getInteger(jarrColumns.getJSONObject(j).getString("xtype"));
                            isCustomfield = StringUtil.getBoolean(jarrColumns.getJSONObject(j).getString("custom"));
                            allowcrossmodule = StringUtil.getBoolean(jarrColumns.getJSONObject(j).optString("allowcrossmodule","false"));
                            if (!checkallowcrossmodule) {
                                checkallowcrossmodule = StringUtil.getBoolean(jarrColumns.getJSONObject(j).optString("allowcrossmodule", "false"));
                            }
                            if (jarrColumns.optJSONObject(j) != null) {
                                precision = StringUtil.getInteger(jarrColumns.getJSONObject(j).optString("precision", defaultPrecision));
                            }
                            if (!jarrColumns.getJSONObject(j).optBoolean("isMeasureItem", false)) {
                                colLabel = "#" + jarrColumns.getJSONObject(j).getString("defaultHeader") + "#";
                                index = stringList.indexOf(colLabel.toLowerCase().trim());
                                if (index >= 0) {
                                    colType = rsmd.getColumnTypeName(index + 1).replaceAll("#", "");
                                }
                            } else {
                                if (j != measureFieldReshuffledIndex) {
                                    colLabel = stringList.get(measureFieldReshuffledIndex);
                                } else {
                                    if (stringList.size() == jarrColumns.length()) {
                                        colLabel = stringList.get(j);
                                    } else {
                                        colLabel = stringList.get(stringList.indexOf("#" + jarrColumns.getJSONObject(j).getString("defaultHeader").toLowerCase() + "#"));
                                    }
                                }
                            }
                            isProcessValue = true;
                            if (isChartRequest && !stringList.contains(colLabel.toLowerCase())) {
                                isProcessValue = false;
                            }

                            if(isProcessValue){
                                switch (xType) {
                                    // For Simple Text and Text Area
                                    case 1:
                                    case 13:
                                        if (colLabel.equals("#discountispercent#")) {
                                            value = reportDataResultSet.getObject(colLabel) != null ? reportDataResultSet.getObject(colLabel).toString() : "";
                                    } else if(reportDataResultSet.getObject(colLabel) != null){
                                            value = String.valueOf(reportDataResultSet.getObject(colLabel));
                                        }
                                         String tempString1 = (String) reportDataResultSet.getObject(colLabel);
                                        if (colLabel.equals("#" + CustomReportConstants.Acc_E_Way_TAX_Rate+ "#")) {
                                            value = getEwayGSTTaxRatesString(tempString1, lineLevelTerms);
                                        } else {
                                            value = tempString1;
                                        }
                                        if (("#" + CustomReportConstants.Acc_Sales_Order_Approval_Status + "#").equalsIgnoreCase(colLabel.toLowerCase())) {
                                            if (!StringUtil.isNullOrEmpty(value)) {
                                                HashMap<String, Object> approvalStatusRequestParams = new HashMap();
                                                String billid = (String) reportDataResultSet.getObject(rsmd.getColumnLabel(columnNames.length));
                                                approvalStatusRequestParams.put("billid", billid);
                                                approvalStatusRequestParams.put("companyid", valueMap.get("companyID"));
                                                approvalStatusRequestParams.put("moduleID", valueMap.get("moduleID"));
                                            if(!moduleId.equals(String.valueOf(Constants.Acc_Customer_ModuleId))){
                                                    value = getApprovalStatus(approvalStatusRequestParams);
                                                }
                                            }
                                        }
                                        if (("#" + CustomReportConstants.Acc_Payment_Method + "#").equalsIgnoreCase(colLabel.toLowerCase())) {//Get Payment Method for sales invoice
                                            if (!StringUtil.isNullOrEmpty(value)) {
                                                String billid = (String) reportDataResultSet.getObject(rsmd.getColumnLabel(columnNames.length));
                                                if (moduleId.equals(String.valueOf(Constants.Acc_Invoice_ModuleId))) {
                                                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Invoice.class.getName(), billid);
                                                    Invoice invoice = (Invoice) objItr.getEntityList().get(0);
                                                    if (invoice != null) {
                                                        if (invoice.isCashtransaction()) {
                                                            PayDetail payDetail = invoice.getPayDetail();
                                                            if (payDetail != null) {
                                                                PaymentMethod paymentMethod = invoice.getPayDetail().getPaymentMethod();
                                                                value = paymentMethod.getMethodName();
                                                            }
                                                        } else {
                                                            value = "NA";
                                                        }
                                                    }
                                                }
                                                if (moduleId.equals(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId))) {//Payment Method
                                                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), billid);
                                                    GoodsReceipt goodsReceipt = (GoodsReceipt) objItr.getEntityList().get(0);
                                                    if (goodsReceipt != null) {
                                                        if (goodsReceipt.isCashtransaction()) {
                                                            PayDetail payDetail = goodsReceipt.getPayDetail();
                                                            if (payDetail != null) {
                                                                PaymentMethod paymentMethod = goodsReceipt.getPayDetail().getPaymentMethod();
                                                                value = paymentMethod.getMethodName();
                                                            }
                                                        } else {
                                                            value = "NA";
                                                        }
                                                    }
                                                }

                                            }
                                        }

                                        if (("#" + CustomReportConstants.Acc_Make_Payment_Customer_Vendor_Name + "#").equalsIgnoreCase(colLabel.toLowerCase()) && moduleId.equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId))) {
                                            if (!StringUtil.isNullOrEmpty(value)) {
                                                String vendorNames = getPaymentVendorNames(companyID, value);
                                                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Payment.class.getName(), value);
                                                Payment payment = (Payment) objItr.getEntityList().get(0);
                                                Customer customer = null;
                                                if (payment.getCustomer() != null) {
                                                    KwlReturnObject custResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), payment.getCustomer());
                                                    customer = (Customer) custResult.getEntityList().get(0);
                                                }
                                            value =  StringUtil.DecodeText((payment.getVendor() == null && customer == null) ? vendorNames : ((payment.getVendor() != null) ? payment.getVendor().getName() : ( customer != null) ?  customer.getName() : ""));
                                            }
                                        }
                                        if (("#" + CustomReportConstants.Acc_Make_Payment_Customer_Vendor_Name + "#").equalsIgnoreCase(colLabel.toLowerCase()) && moduleId.equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {
                                            if (!StringUtil.isNullOrEmpty(value)) {
                                                String vendorNames = getReceiptCustomerNames(companyID, value);
                                                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), value);
                                                Receipt receipt = (Receipt) objItr.getEntityList().get(0);
                                                Vendor vendor = null;
                                                if (receipt.getVendor() != null) {
                                                    KwlReturnObject custResult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), receipt.getVendor());
                                                    vendor = (Vendor) custResult.getEntityList().get(0);
                                                }
                                                value = StringUtil.DecodeText((receipt.getCustomer() == null && vendor == null) ? vendorNames : ((receipt.getCustomer() != null) ? receipt.getCustomer().getName() : (vendor != null) ? vendor.getName() : ""));
                                            }
                                        }

                                        if (("#" + CustomReportConstants.Acc_Make_Payment_Journal_Entry_No + "#").equalsIgnoreCase(colLabel.toLowerCase()) && moduleId.equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId))) {
                                            if (!StringUtil.isNullOrEmpty(value)) {
                                                KwlReturnObject rdresult1 = accCustomerReportServiceDao.getObject(Payment.class.getName(), value);
                                                Payment payment = (Payment) rdresult1.getEntityList().get(0);
                                            String jeNumber = payment.getJournalEntry()!=null ? payment.getJournalEntry().getEntryNumber():"";
                                                if (payment.getJournalEntryForBankCharges() != null) {
                                                    jeNumber += ", " + payment.getJournalEntryForBankCharges().getEntryNumber();
                                                }
                                                if (payment.getJournalEntryForBankInterest() != null) {
                                                    jeNumber += ", " + payment.getJournalEntryForBankInterest().getEntryNumber();
                                                }
                                                HashMap<String, Object> map = new HashMap<>();
                                                map.put("companyid", payment.getCompany().getCompanyID());
                                                map.put("badDebtType", 0);
                                                if (payment.getLinkDetailPayments() != null && !payment.getLinkDetailPayments().isEmpty()) {
                                                    Set<LinkDetailPayment> linkedDetailPayList = payment.getLinkDetailPayments();
                                                    for (LinkDetailPayment ldprow : linkedDetailPayList) {
                                                        if (!StringUtil.isNullOrEmpty(ldprow.getLinkedGainLossJE())) {
                                                            KwlReturnObject linkedje = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), ldprow.getLinkedGainLossJE());
                                                            JournalEntry linkedJEObject = (JournalEntry) linkedje.getEntityList().get(0);
                                                            jeNumber += ", " + linkedJEObject.getEntryNumber();
                                                        }
                                                    }
                                                }
                                                value = jeNumber;
                                            }
                                        }

                                        if (("#" + CustomReportConstants.Acc_Make_Payment_Journal_Entry_No + "#").equalsIgnoreCase(colLabel.toLowerCase()) && moduleId.equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {
                                            if (!StringUtil.isNullOrEmpty(value)) {
                                                KwlReturnObject rdresult1 = accCustomerReportServiceDao.getObject(Receipt.class.getName(), value);
                                                Receipt receipt = (Receipt) rdresult1.getEntityList().get(0);
                                            String jeNumber = receipt.getJournalEntry()!=null? receipt.getJournalEntry().getEntryNumber():"";
                                                if (receipt.getJournalEntryForBankCharges() != null) {
                                                    jeNumber += "," + receipt.getJournalEntryForBankCharges().getEntryNumber();
                                                }
                                                if (receipt.getJournalEntryForBankInterest() != null) {
                                                    jeNumber += "," + receipt.getJournalEntryForBankInterest().getEntryNumber();
                                                }
                                                HashMap<String, Object> map = new HashMap<>();
                                                map.put("companyid", receipt.getCompany().getCompanyID());
                                                map.put("badDebtType", 0);
                                                if (receipt.getLinkDetailReceipts() != null && !receipt.getLinkDetailReceipts().isEmpty()) {
                                                    Set<LinkDetailReceipt> linkedDetailReceiptList = receipt.getLinkDetailReceipts();
                                                    for (LinkDetailReceipt ldprow : linkedDetailReceiptList) {
                                                        if (!StringUtil.isNullOrEmpty(ldprow.getLinkedGainLossJE())) {
                                                            KwlReturnObject linkedje = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), ldprow.getLinkedGainLossJE());
                                                            JournalEntry linkedJEObject = (JournalEntry) linkedje.getEntityList().get(0);
                                                            jeNumber += "," + linkedJEObject.getEntryNumber();
                                                        }
                                                    }
                                                }
                                                value = jeNumber;
                                            }
                                        }

                                        if (!StringUtil.isNullOrEmpty(value)) {
                                            if (moduleId.equals(String.valueOf(Constants.Acc_Invoice_ModuleId)) || moduleId.equals(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId)) || crossJoinMainTable.equalsIgnoreCase(CustomReportConstants.Acc_Sales_Invoice) || crossJoinMainTable.equalsIgnoreCase(CustomReportConstants.Acc_Vendor_Invoice)) {
                                                KwlReturnObject rdresult1 = accCustomerReportServiceDao.getObject(Product.class.getName(), value);
                                                Product fieldComboProductData = (Product) rdresult1.getEntityList().get(0);
                                                if (fieldComboProductData != null && colLabel.contains("#Product ID")) {
                                                    value = fieldComboProductData.getProductid();
                                                } else if (fieldComboProductData != null && colLabel.contains("#Product Type")) {
                                                    value = fieldComboProductData.getProducttype().getName();
                                                } else if (("#" + CustomReportConstants.Acc_Debit_Note_Reason + "#").equalsIgnoreCase(colLabel.toLowerCase()) && moduleId.equals(String.valueOf(Constants.Acc_Credit_Note_ModuleId))) {
                                                    String reason = "";
                                                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), value);
                                                    CreditNote creditMemo = (CreditNote) objItr.getEntityList().get(0);
                                                    if (creditMemo != null) {
                                                        Set<CreditNoteTaxEntry> cnTaxEntryDetails = creditMemo.getCnTaxEntryDetails();
                                                        if (cnTaxEntryDetails != null && !cnTaxEntryDetails.isEmpty()) {
                                                            for (CreditNoteTaxEntry noteTaxEntry : cnTaxEntryDetails) {
                                                                reason += ((noteTaxEntry.getReason() != null) ? noteTaxEntry.getReason().getValue() : "") + ",";
                                                            }
                                                            if (!StringUtil.isNullOrEmpty(reason)) {
                                                                reason = reason.substring(0, reason.length() - 1);
                                                            }
                                                        }
                                                    }
                                                    value = reason;
                                                } else if (("#" + CustomReportConstants.Acc_Debit_Note_Reason + "#").equalsIgnoreCase(colLabel.toLowerCase()) && moduleId.equals(String.valueOf(Constants.Acc_Debit_Note_ModuleId))) {
                                                    String reason = "";
                                                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), value);
                                                    DebitNote debitMemo = (DebitNote) objItr.getEntityList().get(0);
                                                    if (debitMemo != null) {
                                                        Set<DebitNoteTaxEntry> dnTaxEntryDetails = debitMemo.getDnTaxEntryDetails();
                                                        if (dnTaxEntryDetails != null && !dnTaxEntryDetails.isEmpty()) {
                                                            for (DebitNoteTaxEntry noteTaxEntry : dnTaxEntryDetails) {
                                                                reason += ((noteTaxEntry.getReason() != null) ? noteTaxEntry.getReason().getValue() : "") + ",";
                                                            }
                                                            if (!StringUtil.isNullOrEmpty(reason)) {
                                                                reason = reason.substring(0, reason.length() - 1);
                                                            }
                                                        }
                                                    }
                                                    value = reason;
                                                }
                                            }  else if (moduleId.equals(String.valueOf(Constants.Acc_Debit_Note_ModuleId))) {
                                                if (("#" + CustomReportConstants.Acc_Debit_Note_Reason + "#").equalsIgnoreCase(colLabel.toLowerCase())) {
                                                    String reason = "";
                                                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), value);
                                                    DebitNote debitMemo = (DebitNote) objItr.getEntityList().get(0);
                                                    if (debitMemo != null) {
                                                        Set<DebitNoteTaxEntry> dnTaxEntryDetails = debitMemo.getDnTaxEntryDetails();
                                                        if (dnTaxEntryDetails != null && !dnTaxEntryDetails.isEmpty()) {
                                                            for (DebitNoteTaxEntry noteTaxEntry : dnTaxEntryDetails) {
                                                                reason += ((noteTaxEntry.getReason() != null) ? noteTaxEntry.getReason().getValue() : "") + ",";
                                                            }
                                                            if (!StringUtil.isNullOrEmpty(reason)) {
                                                                reason = reason.substring(0, reason.length() - 1);
                                                            }
                                                        }
                                                    }
                                                    value = reason;
                                                } else if (("#" + CustomReportConstants.Acc_Debit_Note_Alias_Name + "#").equalsIgnoreCase(colLabel.toLowerCase())) {
                                                    String aliasname = "";
                                                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), value);
                                                    DebitNote debitMemo = (DebitNote) objItr.getEntityList().get(0);
                                                    if (debitMemo != null) {
                                                        Customer customer = debitMemo.getCustomer();
                                                    if (customer != null ) {
                                                            aliasname = customer.getAliasname();
                                                        }
                                                        Vendor vendor = debitMemo.getVendor();
                                                        if (StringUtil.isNullOrEmpty(aliasname) && vendor != null) {
                                                            aliasname = vendor.getAliasname();
                                                        }
                                                    }
                                                    value = aliasname;
                                                }

                                            } else if (moduleId.equals(String.valueOf(Constants.Acc_Credit_Note_ModuleId))) {
                                                if (("#" + CustomReportConstants.Acc_Debit_Note_Reason + "#").equalsIgnoreCase(colLabel.toLowerCase())) {
                                                    String reason = "";
                                                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), value);
                                                    CreditNote creditMemo = (CreditNote) objItr.getEntityList().get(0);
                                                    if (creditMemo != null) {
                                                        Set<CreditNoteTaxEntry> cnTaxEntryDetails = creditMemo.getCnTaxEntryDetails();
                                                        if (cnTaxEntryDetails != null && !cnTaxEntryDetails.isEmpty()) {
                                                            for (CreditNoteTaxEntry noteTaxEntry : cnTaxEntryDetails) {
                                                                reason += ((noteTaxEntry.getReason() != null) ? noteTaxEntry.getReason().getValue() : "") + ",";
                                                            }
                                                            if (!StringUtil.isNullOrEmpty(reason)) {
                                                                reason = reason.substring(0, reason.length() - 1);
                                                            }
                                                        }
                                                    }
                                                    value = reason;
                                                } else if (("#" + CustomReportConstants.Acc_Debit_Note_Alias_Name + "#").equalsIgnoreCase(colLabel.toLowerCase())) {
                                                    String aliasname = "";
                                                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), value);
                                                    CreditNote creditMemo = (CreditNote) objItr.getEntityList().get(0);
                                                    if (creditMemo != null) {
                                                        Customer customer = creditMemo.getCustomer();
                                                    if (customer != null ) {
                                                            aliasname = customer.getAliasname();
                                                        }
                                                        Vendor vendor = creditMemo.getVendor();
                                                    if (StringUtil.isNullOrEmpty(aliasname) && vendor != null ) {
                                                            aliasname = vendor.getAliasname();
                                                        }
                                                    }
                                                    value = aliasname;
                                                }
                                            } else if (moduleId.equals(String.valueOf(Constants.Acc_Purchase_Order_ModuleId))) {
                                                if (("#" + CustomReportConstants.Acc_Sales_Order_Status + "#").equalsIgnoreCase(colLabel.toLowerCase())) {
                                                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), value);
                                                    PurchaseOrder purchaseOrder = (PurchaseOrder) objItr.getEntityList().get(0);
                                                    String status = accPurchaseOrderServiceDAOobj.getPurchaseOrderStatus(purchaseOrder);
                                                    //ERP-28247
                                                double purchaseOrderBalanceQty =0.0;
                                                    for (PurchaseOrderDetail pod : purchaseOrder.getRows()) {
                                                        purchaseOrderBalanceQty += pod.getBalanceqty();
                                                    }
                                                status = (purchaseOrderBalanceQty==0)?"Closed":(purchaseOrder.isIsPOClosed()?"Closed":"Open");
                                                    value = status;
                                                }
                                            }else if (moduleId.equals(String.valueOf(Constants.Acc_Customer_Quotation_ModuleId))) {
                                                if (("#" + CustomReportConstants.Acc_Sales_Order_Status + "#").equalsIgnoreCase(colLabel.toLowerCase())) {
                                                    JSONArray jsonA = new JSONArray();
                                                    String tempString = (String) reportDataResultSet.getObject(colLabel);
                                                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                                                    JSONArray DataJArr = new JSONArray();
                                                    List<String> quotationList = new ArrayList<String>();
                                                    quotationList.add(tempString);
                                                    requestParams.put("gcurrencyid", valueMap.get("gcurrencyid"));
                                                    requestParams.put("companyid", valueMap.get("companyID"));
                                                    SimpleDateFormat sdf = new SimpleDateFormat((String) valueMap.get("userDateFormat"));
                                                    requestParams.put("userdf", sdf);
                                                    requestParams.put("df", valueMap.get("df"));
                                                    jsonA = accSalesOrderServiceDAOobj.getQuotationsJson(requestParams, quotationList, DataJArr);
                                                    value = jsonA.getJSONObject(0).getString("status").toString();
                                                }
                                            } else if (moduleId.equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId))) {
                                                if (("#" + CustomReportConstants.IsTransactionLinked + "#").equalsIgnoreCase(colLabel.toLowerCase())) {
                                                    String billid = (String) reportDataResultSet.getObject(rsmd.getColumnLabel(columnNames.length));
                                                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Payment.class.getName(), billid);
                                                    Payment payment = (Payment) objItr.getEntityList().get(0);
                                                    if ((payment.getLinkDetailPayments() != null && !payment.getLinkDetailPayments().isEmpty()) || (payment.getLinkDetailPaymentToCreditNote() != null && !payment.getLinkDetailPaymentToCreditNote().isEmpty()) || (payment.getLinkDetailPaymentsToAdvancePayment() != null && !payment.getLinkDetailPaymentsToAdvancePayment().isEmpty())) {
                                                        value = "Yes";
                                                    } else {
                                                        value = "No";
                                                    }
                                                }
                                            } else if (moduleId.equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {
                                                if (("#" + CustomReportConstants.IsTransactionLinked + "#").equalsIgnoreCase(colLabel.toLowerCase())) {
                                                    String billid = (String) reportDataResultSet.getObject(rsmd.getColumnLabel(columnNames.length));
                                                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), billid);
                                                    Receipt receipt = (Receipt) objItr.getEntityList().get(0);
                                                    if (((receipt.getLinkDetailReceipts() != null) && (!receipt.getLinkDetailReceipts().isEmpty()) || ((receipt.getLinkDetailReceiptsToDebitNote() != null) && (!receipt.getLinkDetailReceiptsToDebitNote().isEmpty())) || receipt.isIsWrittenOff())) {
                                                        value = "Yes";
                                                    } else {
                                                        value = "No";
                                                    }
                                                }
                                            }else if (moduleId.equals(String.valueOf(Constants.Acc_Purchase_Requisition_ModuleId))) {
                                                if (("#" + CustomReportConstants.Acc_Sales_Order_Status + "#").equalsIgnoreCase(colLabel.toLowerCase())) {
                                                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(PurchaseRequisition.class.getName(), value);
                                                    PurchaseRequisition purchaseRequistion = (PurchaseRequisition) objItr.getEntityList().get(0);
                                                    String status = accPurchaseOrderServiceDAOobj.getPurchaseRequisitionStatus(purchaseRequistion);
                                                    KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyID);
                                                    ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
                                                    JSONObject columnprefObj = new JSONObject();
                                                    if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                                                        columnprefObj = new JSONObject((String) extraCompanyPreferences.getColumnPref());
                                                    }
                                                    boolean statusOfRequisitionForPO = false;
                                                    if (columnprefObj.has("statusOfRequisitionForPO") && columnprefObj.get("statusOfRequisitionForPO") != null && (Boolean) columnprefObj.get("statusOfRequisitionForPO") != false) {
                                                        statusOfRequisitionForPO = true;
                                                    }

                                                    if (statusOfRequisitionForPO) {
                                                        status = purchaseRequistion.isIsOpenInPO() ? "Open" : "Closed";
                                                    }
                                                    value = status;
                                                }
                                            }
                                            KwlReturnObject rdresult = accCustomerReportServiceDao.getObject(Producttype.class.getName(), value);
                                            Producttype fieldComboData = (Producttype) rdresult.getEntityList().get(0);
                                            if (fieldComboData != null) {
                                                value = fieldComboData.getName();
                                            }
                                        }
                                        break;
                                    // For Number Field
                                    case 2:
                                        double tempValue = 0;
                                        double tempValueForMargin = 0;
                                        boolean isEmpty = false;
                                        if (moduleId.equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId))) {
                                            CurrencySettingsForPayment(jarrRecord, reportDataResultSet, currency, colLabel, jarrColumns.getJSONObject(j), companyID, processedData, rowNum);
                                        }
//                                    int precision = 2;
//                                    if (jarrColumns.optJSONObject(i - 1) != null) {
//                                        precision = StringUtil.getInteger(jarrColumns.getJSONObject(i - 1).optString("precision", defaultPrecision));
//                                    }

                                        if (moduleId.equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {
                                            CurrencySettingsForReceipt(jarrRecord, reportDataResultSet, currency, colLabel, jarrColumns.getJSONObject(j), companyID, processedData, rowNum);
                                        }
                                        try {
                                            if (reportDataResultSet.getObject(colLabel) != null) {
                                                if (reportDataResultSet.getObject(colLabel) instanceof String) {
                                                    String tempString = (String) reportDataResultSet.getObject(colLabel);
                                                    if (!StringUtil.isNullOrEmpty(tempString)) {
                                                        if (!jarrColumns.getJSONObject(j).getBoolean("isMeasureItem")) {
                                                        if(!allowcrossmodule) {
                                                                if (CustomReportConstants.Acc_Sales_Order_Balance_Amount.trim().equalsIgnoreCase(jarrColumns.getJSONObject(j).getString("defaultHeaderOrig").trim()) && moduleId.equals(String.valueOf(Constants.Acc_Sales_Order_ModuleId)) || moduleId.equals(String.valueOf(Constants.Acc_Purchase_Order_ModuleId))) {
                                                            double balanceAmount = 0.0,rowTaxAmount=0.0;
                                                                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), tempString);
                                                                    SalesOrderDetail sod = (SalesOrderDetail) objItr.getEntityList().get(0);
                                                                    if (sod != null) {
                                                                        rowTaxAmount += sod.getRowTaxAmount();
                                                                        double discountValueForExcel = (sod.getDiscountispercent() == 1) ? authHandler.round(((sod.getRate() * sod.getQuantity()) * sod.getDiscount() / 100), companyID) : sod.getDiscount();
                                                                        double amountForExcelFile = (sod.getRate() * sod.getQuantity()) - discountValueForExcel;
                                                                        amountForExcelFile = amountForExcelFile + rowTaxAmount;
                                                                        balanceAmount = authHandler.round((amountForExcelFile / sod.getQuantity()) * sod.getBalanceqty(), companyID);
                                                                    }
                                                                    tempValue = balanceAmount;
                                                                } else if (moduleId.equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId)) && (CustomReportConstants.Acc_Make_Payment_Invoice_Amount_Due.trim().equalsIgnoreCase(jarrColumns.getJSONObject(j).getString("defaultHeaderOrig").trim()) || CustomReportConstants.Acc_Make_Payment_Linked_Invoice_Amount_Due.trim().equalsIgnoreCase(jarrColumns.getJSONObject(j).getString("defaultHeaderOrig").trim()))) {

                                                                    double amountdue = 0.0, totalamount = 0.0, amountDueOriginal = 0.0;
                                                                    HashMap requestParams = new HashMap<String, Object>();
                                                                    requestParams.put("companyid", valueMap.get("companyID"));
                                                                    requestParams.put("gcurrencyid", valueMap.get("gcurrencyid"));
                                                                    if (CustomReportConstants.Acc_Make_Payment_Invoice_Amount_Due.trim().equalsIgnoreCase(jarrColumns.getJSONObject(j).getString("defaultHeaderOrig").trim())) {
                                                                        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(PaymentDetail.class.getName(), tempString);
                                                                        PaymentDetail paymentDetail = (PaymentDetail) objItr.getEntityList().get(0);
                                                                        if (paymentDetail.getGoodsReceipt().isNormalInvoice()) {
                                                                            List ll;
                                                                            if (paymentDetail.getGoodsReceipt().isIsExpenseType()) {
                                                                                ll = accGoodsReceiptCMN.getExpGRAmountDue(requestParams, paymentDetail.getGoodsReceipt());
                                                                            } else {
                                                                                if (Constants.InvoiceAmountDueFlag) {
                                                                                    ll = accGoodsReceiptCMN.getInvoiceDiscountAmountInfo(requestParams, paymentDetail.getGoodsReceipt());
                                                                                } else {
                                                                                    ll = accGoodsReceiptCMN.getGRAmountDue(requestParams, paymentDetail.getGoodsReceipt());
                                                                                }
                                                                            }
                                                                            amountdue = (ll.isEmpty() ? 0 : (Double) ll.get(1));
                                                                        }
                                                                    } else {
                                                                        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(LinkDetailPayment.class.getName(), tempString);
                                                                        LinkDetailPayment paymentDetail = (LinkDetailPayment) objItr.getEntityList().get(0);
                                                                        if (paymentDetail.getGoodsReceipt().isNormalInvoice()) {
                                                                            List ll;
                                                                            if (paymentDetail.getGoodsReceipt().isIsExpenseType()) {
                                                                                ll = accGoodsReceiptCMN.getExpGRAmountDue(requestParams, paymentDetail.getGoodsReceipt());
                                                                            } else {
                                                                                if (Constants.InvoiceAmountDueFlag) {
                                                                                    ll = accGoodsReceiptCMN.getInvoiceDiscountAmountInfo(requestParams, paymentDetail.getGoodsReceipt());
                                                                                } else {
                                                                                    ll = accGoodsReceiptCMN.getGRAmountDue(requestParams, paymentDetail.getGoodsReceipt());
                                                                                }
                                                                            }
                                                                            amountdue = (ll.isEmpty() ? 0 : (Double) ll.get(1));
                                                                        }
                                                                    }

                                                                    tempValue = amountdue;
                                                                } else if (moduleId.equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId)) && (CustomReportConstants.Acc_Make_Payment_Invoice_Amount_Due.trim().equalsIgnoreCase(jarrColumns.getJSONObject(j).getString("defaultHeaderOrig").trim()) || CustomReportConstants.Acc_Make_Payment_Linked_Invoice_Amount_Due.trim().equalsIgnoreCase(jarrColumns.getJSONObject(j).getString("defaultHeaderOrig").trim()))) {

                                                                    double amountdue = 0.0, totalamount = 0.0, amountDueOriginal = 0.0;
                                                                    HashMap requestParams = new HashMap<String, Object>();
                                                                    requestParams.put("companyid", valueMap.get("companyID"));
                                                                    requestParams.put("gcurrencyid", valueMap.get("gcurrencyid"));
                                                                    if (CustomReportConstants.Acc_Make_Payment_Invoice_Amount_Due.trim().equalsIgnoreCase(jarrColumns.getJSONObject(j).getString("defaultHeaderOrig").trim())) {
                                                                        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(ReceiptDetail.class.getName(), tempString);
                                                                        ReceiptDetail receiptDetail = (ReceiptDetail) objItr.getEntityList().get(0);
                                                                        if (receiptDetail.getInvoice().isNormalInvoice()) {
                                                                            if (Constants.InvoiceAmountDueFlag) {
                                                                                List ll = accInvoiceCommon.getInvoiceDiscountAmountInfo(requestParams, receiptDetail.getInvoice());
                                                                                amountdue = (Double) ll.get(0);
                                                                            } else {
                                                                                amountdue = accInvoiceCommon.getAmountDue(requestParams, receiptDetail.getInvoice());
                                                                            }
                                                                        }
                                                                    } else {
                                                                        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(LinkDetailReceipt.class.getName(), tempString);
                                                                        LinkDetailReceipt receiptDetail = (LinkDetailReceipt) objItr.getEntityList().get(0);
                                                                        if (receiptDetail.getInvoice().isNormalInvoice()) {
                                                                            if (Constants.InvoiceAmountDueFlag) {
                                                                                List ll = accInvoiceCommon.getInvoiceDiscountAmountInfo(requestParams, receiptDetail.getInvoice());
                                                                                amountdue = (Double) ll.get(0);
                                                                            } else {
                                                                                amountdue = accInvoiceCommon.getAmountDue(requestParams, receiptDetail.getInvoice());
                                                                            }
                                                                        }
                                                                    }

                                                                    tempValue = amountdue;
                                                                } else if (moduleId.equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId)) && CustomReportConstants.Acc_Receive_Payment_Loan_Amount_Due.trim().equalsIgnoreCase(jarrColumns.getJSONObject(j).getString("defaultHeaderOrig").trim())) {

                                                                    double amountDueOfDisbursement = 0.0;


                                                                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(ReceiptDetailLoan.class.getName(), tempString);
                                                                    ReceiptDetailLoan row = (ReceiptDetailLoan) objItr.getEntityList().get(0);
                                                                    RepaymentDetails RD = row.getRepaymentDetail();
                                                                    Disbursement disbursement = RD.getDisbursement();
                                                                    amountDueOfDisbursement = disbursement.getLoanAmount();

                                                                    Map<String, Object> mapForRepaymentDetails = new HashMap<>();
                                                                    mapForRepaymentDetails.put("paymentStatus", "1");
                                                                    mapForRepaymentDetails.put("companyid", companyID);
                                                                    mapForRepaymentDetails.put("df", df2);
                                                                    mapForRepaymentDetails.put("disbursementId", disbursement.getID());

                                                                    List<RepaymentDetails> RDS = getRepaymentSheduleDetails(mapForRepaymentDetails);
                                                                    for (RepaymentDetails RPD : RDS) {
                                                                        amountDueOfDisbursement -= RPD.getPrinciple();
                                                                    }
                                                                    amountDueOfDisbursement = authHandler.round(amountDueOfDisbursement, companyID);

                                                                    tempValue = amountDueOfDisbursement;
                                                                } else {
                                                                    if(colLabel.contains(CustomReportConstants.Acc_CGST+"#") || colLabel.contains(CustomReportConstants.Acc_SGST+"#")|| colLabel.contains(CustomReportConstants.Acc_IGST+"#")|| colLabel.contains(CustomReportConstants.Acc_UTGST+"#")|| colLabel.contains(CustomReportConstants.Acc_CESS+"#")|| colLabel.equals("#" + CustomReportConstants.Acc_E_Way_TAX_Rate+ "#")){
                                                                        if(colLabel.equals("#" + CustomReportConstants.Acc_E_Way_TAX_Rate+ "#")){
                                                                            value = getEwayGSTTaxRatesString(tempString, lineLevelTerms);
                                                                        }   else { 
                                                                            tempValue = getIndividualGSTTaxes(companyID,tempString, jarrColumns.getJSONObject(j).getString("id").trim(), lineLevelTerms,lineLevelTermsMap);                                                                            
                                                                        }
                                                                    } else {
                                                                    tempValue = Double.parseDouble(tempString);
                                                                }
                                                                }
                                                            } else {
                                                                if ((moduleId.equals(String.valueOf(Constants.Acc_Purchase_Order_ModuleId)) || moduleId.equals(String.valueOf(Constants.Acc_Invoice_ModuleId))) && CustomReportConstants.Acc_Sales_Order_Balance_Amount.trim().equalsIgnoreCase(jarrColumns.getJSONObject(j).getString("defaultHeaderOrig").trim())) {
                                                                    double balanceAmount = 0.0, rowTaxAmount = 0.0;
                                                                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(SalesOrderDetail.class.getName(), tempString);
                                                                    SalesOrderDetail sod = (SalesOrderDetail) objItr.getEntityList().get(0);
                                                                    if (sod != null) {
                                                                        rowTaxAmount += sod.getRowTaxAmount();
                                                                        double discountValueForExcel = (sod.getDiscountispercent() == 1) ? authHandler.round(((sod.getRate() * sod.getQuantity()) * sod.getDiscount() / 100), companyID) : sod.getDiscount();
                                                                        double amountForExcelFile = (sod.getRate() * sod.getQuantity()) - discountValueForExcel;
                                                                        amountForExcelFile = amountForExcelFile + rowTaxAmount;
                                                                        balanceAmount = authHandler.round((amountForExcelFile / sod.getQuantity()) * sod.getBalanceqty(), companyID);
                                                                    }
                                                                    tempValue = balanceAmount;
                                                                } else {
                                                                    tempValue = Double.parseDouble(tempString);
                                                                }
                                                            }
                                                        } else if (moduleId.equals(String.valueOf(Constants.Acc_Customer_Quotation_ModuleId))) {
                                                        if(!allowcrossmodule) {
                                                                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                                                                JSONArray DataJArr = new JSONArray();
                                                                List<String> quotationList = new ArrayList<String>();
                                                                quotationList.add(tempString);
                                                                requestParams.put("gcurrencyid", valueMap.get("gcurrencyid"));
                                                            requestParams.put("companyid",valueMap.get("companyID"));
                                                            SimpleDateFormat sdf = new SimpleDateFormat((String)valueMap.get("userDateFormat"));                                                            
                                                                requestParams.put("userdf", sdf);
                                                                requestParams.put("df", valueMap.get("df"));
                                                            tempValue=calculateCustomerQuotationMeasures(requestParams,quotationList,DataJArr,jarrColumns.getJSONObject(j));

                                                            }
                                                        } else if (moduleId.equals(String.valueOf(Constants.Acc_Sales_Order_ModuleId))) {
                                                        if(!allowcrossmodule) {
                                                                tempValue = calculateSalesOrderMeasures(tempString, valueMap, jarrColumns.getJSONObject(j));
                                                            } else {
                                                                if (jarrColumns.getJSONObject(j).getInt("crossJoinModuleId") == Constants.Acc_Purchase_Order_ModuleId) {
                                                                    tempValue = calculatePurchaseOrderMeasures(tempString, currency, currencyid, valueMap, jarrColumns.getJSONObject(j));
                                                                } else if (jarrColumns.getJSONObject(j).getInt("crossJoinModuleId") == Constants.Acc_Delivery_Order_ModuleId) {
                                                                    tempValue = calculateDeliveryOrderMeasures(tempString, valueMap, jarrColumns.getJSONObject(j), companyID);
                                                                } else if (jarrColumns.getJSONObject(j).getInt("crossJoinModuleId") == Constants.Acc_Invoice_ModuleId) {
                                                                tempValue = calculateSalesInvoiceMeasures(tempString, valueMap, jarrColumns.getJSONObject(j),processedinvoiceIds,dataJArr);
                                                                } else if (jarrColumns.getJSONObject(j).getInt("crossJoinModuleId") == Constants.Acc_Customer_Quotation_ModuleId) {
                                                                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                                                                    JSONArray DataJArr = new JSONArray();
                                                                    List<String> quotationList = new ArrayList<String>();
                                                                    quotationList.add(tempString);
                                                                    requestParams.put("gcurrencyid", valueMap.get("gcurrencyid"));
                                                                    requestParams.put("companyid", valueMap.get("companyID"));
                                                                    SimpleDateFormat sdf = new SimpleDateFormat((String) valueMap.get("userDateFormat"));
                                                                    requestParams.put("userdf", sdf);
                                                                    requestParams.put("df", valueMap.get("df"));
                                                                    tempValue = calculateCustomerQuotationMeasures(requestParams, quotationList, DataJArr, jarrColumns.getJSONObject(j));
                                                                }
                                                            }
                                                        } else if (moduleId.equals(String.valueOf(Constants.Acc_Invoice_ModuleId)) || moduleId.equals(String.valueOf(Constants.Acc_FixedAssets_DisposalInvoice_ModuleId))) {
                                                            if (!allowcrossmodule) {
                                                            tempValue = calculateSalesInvoiceMeasures(tempString, valueMap, jarrColumns.getJSONObject(j),processedinvoiceIds,dataJArr);

                                                            } else {
                                                                if (jarrColumns.getJSONObject(j).getInt("crossJoinModuleId") == Constants.Acc_Sales_Order_ModuleId) {
                                                                    tempValue = calculateSalesOrderMeasures(tempString, valueMap, jarrColumns.getJSONObject(j));
                                                                } else if (jarrColumns.getJSONObject(j).getInt("crossJoinModuleId") == Constants.Acc_Delivery_Order_ModuleId) {
                                                                    tempValue = calculateDeliveryOrderMeasures(tempString, valueMap, jarrColumns.getJSONObject(j), companyID);
                                                                } else if (jarrColumns.getJSONObject(j).getInt("crossJoinModuleId") == Constants.Acc_Sales_Return_ModuleId) {
                                                                    tempValue = calculateSalesReturnMeasures(tempString, valueMap, jarrColumns.getJSONObject(j), companyID);
                                                                } else if (jarrColumns.getJSONObject(j).getInt("crossJoinModuleId") == Constants.Acc_Customer_Quotation_ModuleId) {
                                                                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                                                                    JSONArray DataJArr = new JSONArray();
                                                                    List<String> quotationList = new ArrayList<String>();
                                                                    quotationList.add(tempString);
                                                                    requestParams.put("gcurrencyid", valueMap.get("gcurrencyid"));
                                                                    requestParams.put("companyid", valueMap.get("companyID"));
                                                                    SimpleDateFormat sdf = new SimpleDateFormat((String) valueMap.get("userDateFormat"));
                                                                    requestParams.put("userdf", sdf);
                                                                    requestParams.put("df", valueMap.get("df"));
                                                                    tempValue = calculateCustomerQuotationMeasures(requestParams, quotationList, DataJArr, jarrColumns.getJSONObject(j));
                                                                }
                                                            }
                                                        } else if (moduleId.equals(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId)) || moduleId.equals(String.valueOf(Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId))) {
                                                            if (!allowcrossmodule) {
                                                                if (moduleId.equals(String.valueOf(Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId))) {
                                                                    valueMap.put("isFixedAsset", true);
                                                                } else {
                                                                    valueMap.put("isFixedAsset", false);
                                                                }
                                                                tempValue = calculatePurchaseInvoiceMeasures(tempString, valueMap, jarrColumns.getJSONObject(j));
                                                            } else {
                                                                if (jarrColumns.getJSONObject(j).getInt("crossJoinModuleId") == Constants.Acc_Purchase_Order_ModuleId) {
                                                                    tempValue = calculatePurchaseOrderMeasures(tempString, currency, currencyid, valueMap, jarrColumns.getJSONObject(j));
                                                                } else if (jarrColumns.getJSONObject(j).getInt("crossJoinModuleId") == Constants.Acc_Goods_Receipt_ModuleId) {
                                                                    tempValue = calculateGoodsReceiptMeasures(tempString, valueMap, jarrColumns.getJSONObject(j));
                                                                } else if (jarrColumns.getJSONObject(j).getInt("crossJoinModuleId") == Constants.Acc_Purchase_Return_ModuleId) {
                                                                    tempValue = calculatePurchaseReturnMeasures(tempString, valueMap, jarrColumns.getJSONObject(j), companyID);
                                                                }
                                                            }
                                                        }else if (moduleId.equals(String.valueOf(Constants.Acc_Purchase_Order_ModuleId))) {
                                                            if (!allowcrossmodule) {
                                                                tempValue = calculatePurchaseOrderMeasures(tempString, currency, currencyid, valueMap, jarrColumns.getJSONObject(j));
                                                            } else {
                                                                if (jarrColumns.getJSONObject(j).getInt("crossJoinModuleId") == Constants.Acc_Sales_Order_ModuleId) {
                                                                    tempValue = calculateSalesOrderMeasures(tempString, valueMap, jarrColumns.getJSONObject(j));
                                                                } else if (jarrColumns.getJSONObject(j).getInt("crossJoinModuleId") == Constants.Acc_Goods_Receipt_ModuleId) {
                                                                    tempValue = calculateGoodsReceiptMeasures(tempString, valueMap, jarrColumns.getJSONObject(j));
                                                                } else if (jarrColumns.getJSONObject(j).getInt("crossJoinModuleId") == Constants.Acc_Vendor_Invoice_ModuleId) {
                                                                    tempValue = calculatePurchaseInvoiceMeasures(tempString, valueMap, jarrColumns.getJSONObject(j));
                                                                }
                                                            }
                                                            //}

                                                        } else if (moduleId.equals(String.valueOf(Constants.Acc_Sales_Return_ModuleId)) || moduleId.equals(String.valueOf(Constants.Acc_FixedAssets_Sales_Return_ModuleId))) {
                                                            tempValue = calculateSalesReturnMeasures(tempString, valueMap, jarrColumns.getJSONObject(j), companyID);
                                                        } else if (moduleId.equals(String.valueOf(Constants.Acc_Purchase_Return_ModuleId)) || moduleId.equals(String.valueOf(Constants.Acc_FixedAssets_Purchase_Return_ModuleId)) ) {
                                                            tempValue = calculatePurchaseReturnMeasures(tempString, valueMap, jarrColumns.getJSONObject(j), companyID);
                                                        } else if (moduleId.equals(String.valueOf(Constants.Acc_Goods_Receipt_ModuleId))) {
                                                            if (!allowcrossmodule) {
                                                                tempValue = calculateGoodsReceiptMeasures(tempString, valueMap, jarrColumns.getJSONObject(j));
                                                            } else {
                                                                if (jarrColumns.getJSONObject(j).getInt("crossJoinModuleId") == Constants.Acc_Purchase_Order_ModuleId) {
                                                                    tempValue = calculatePurchaseOrderMeasures(tempString, currency, currencyid, valueMap, jarrColumns.getJSONObject(j));
                                                                } else if (jarrColumns.getJSONObject(j).getInt("crossJoinModuleId") == Constants.Acc_Vendor_Invoice_ModuleId) {
                                                                    tempValue = calculatePurchaseInvoiceMeasures(tempString, valueMap, jarrColumns.getJSONObject(j));
                                                                }
                                                            }
                                                        } else if (moduleId.equals(String.valueOf(Constants.Acc_Delivery_Order_ModuleId))) {
                                                            if (!allowcrossmodule) {
                                                                tempValue = calculateDeliveryOrderMeasures(tempString, valueMap, jarrColumns.getJSONObject(j), companyID);
                                                            } else {
                                                                if (jarrColumns.getJSONObject(j).getInt("crossJoinModuleId") == Constants.Acc_Sales_Order_ModuleId) {
                                                                    tempValue = calculateSalesOrderMeasures(tempString, valueMap, jarrColumns.getJSONObject(j));
                                                                } else if (jarrColumns.getJSONObject(j).getInt("crossJoinModuleId") == Constants.Acc_Invoice_ModuleId) {
                                                                tempValue = calculateSalesInvoiceMeasures(tempString, valueMap, jarrColumns.getJSONObject(j),processedinvoiceIds,dataJArr);
                                                                }
                                                            }
                                                        } else if (moduleId.equals(String.valueOf(Constants.Acc_Debit_Note_ModuleId))) {
                                                        if(!allowcrossmodule) {
                                                                tempValue = calculateDebitNoteMeasures(tempString, valueMap, jarrColumns.getJSONObject(j));
                                                        } else{
                                                                if (jarrColumns.getJSONObject(j).getInt("crossJoinModuleId") == Constants.Acc_Vendor_Invoice_ModuleId) {
                                                                    tempValue = calculatePurchaseInvoiceMeasures(tempString, valueMap, jarrColumns.getJSONObject(j));
                                                                }
                                                            }
                                                        } else if (moduleId.equals(String.valueOf(Constants.Acc_Credit_Note_ModuleId))) {
                                                        if(!allowcrossmodule) {
                                                                tempValue = calculateCreditNoteMeasures(tempString, valueMap, jarrColumns.getJSONObject(j));
                                                        } else{
                                                                if (jarrColumns.getJSONObject(j).getInt("crossJoinModuleId") == Constants.Acc_Invoice_ModuleId) {
                                                                tempValue = calculateSalesInvoiceMeasures(tempString, valueMap, jarrColumns.getJSONObject(j),processedinvoiceIds,dataJArr);
                                                                }
                                                            }
                                                    }else if (moduleId.equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {
                                                            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), tempString);
                                                            Receipt receipt = (Receipt) objItr.getEntityList().get(0);
                                                            if (receipt != null) {
                                                                double amount = 0, linkedAmountDue = 0, totaltaxamount = 0, amountDueInBase = 0;
                                                                amount = receipt.getDepositAmount();

                                                                Set<ReceiptDetailOtherwise> receiptDetailsOtherwise = receipt.getReceiptDetailOtherwises();
                                                                for (ReceiptDetailOtherwise RDO : receiptDetailsOtherwise) { // Tax amount of payment against GL is added.
                                                                    if (RDO.getTaxamount() != 0) {
                                                                        totaltaxamount += RDO.getTaxamount();
                                                                    }
                                                                }

                                                                if (receipt.getReceiptAdvanceDetails() != null && !receipt.getReceiptAdvanceDetails().isEmpty()) {
                                                                    for (ReceiptAdvanceDetail advanceDetail : receipt.getReceiptAdvanceDetails()) {
                                                                        linkedAmountDue += advanceDetail.getAmountDue();
                                                                        HashMap requestParams = new HashMap<String, Object>();
                                                                        requestParams.put("companyid", valueMap.get("companyID"));
                                                                        requestParams.put("gcurrencyid", valueMap.get("gcurrencyid"));
//                                                                        amountDueInBase = getCurrencyToBaseAmount(requestParams, advanceDetail.getAmountDue(), receipt.getCurrency().getCurrencyID(), receipt.getJournalEntry().getEntryDate(), receipt.getJournalEntry().getExternalCurrencyRate());
                                                                        amountDueInBase = getCurrencyToBaseAmount(requestParams, advanceDetail.getAmountDue(), receipt.getCurrency().getCurrencyID(), receipt.getCreationDate(), receipt.getJournalEntry().getExternalCurrencyRate());
                                                                    }
                                                                }

                                                                totaltaxamount = authHandler.round(totaltaxamount, companyID);
                                                                if (CustomReportConstants.Acc_Sales_Order_Measure_Total_Tax.trim().equalsIgnoreCase(jarrColumns.getJSONObject(j).getString("defaultHeaderOrig").trim())) {
                                                                    tempValue = totaltaxamount;
                                                                }
                                                                if (CustomReportConstants.Acc_Sales_Order_Measure_AmtWithoutTax.trim().equalsIgnoreCase(jarrColumns.getJSONObject(j).getString("defaultHeaderOrig").trim())) {
                                                                    tempValue = amount - totaltaxamount;
                                                                }
                                                                if (CustomReportConstants.Acc_Debit_Note_Amount_Due.trim().equalsIgnoreCase(jarrColumns.getJSONObject(j).getString("defaultHeaderOrig").trim())) {
                                                                    tempValue = linkedAmountDue;
                                                                }
                                                                if (CustomReportConstants.Acc_Make_Payment_Amount_Due_InBase.trim().equalsIgnoreCase(jarrColumns.getJSONObject(j).getString("defaultHeaderOrig").trim())) {
                                                                    tempValue = amountDueInBase;
                                                                }
                                                            }
                                                    }else if (moduleId.equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId))) {
                                                            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Payment.class.getName(), tempString);
                                                            Payment payment = (Payment) objItr.getEntityList().get(0);
                                                            double totalDiscount = 0;
                                                            if (payment != null) {
                                                                double amount = 0, linkedAmountDue = 0, totaltaxamount = 0, amountDueInBase = 0;
                                                                amount = payment.getDepositAmount();

                                                                if (payment.getBankChargesAmount() > 0 && payment.getJournalEntryForBankCharges() == null) {
                                                                    amount -= payment.getBankChargesAmount();
                                                                }
                                                                if (payment.getBankInterestAmount() > 0 && payment.getJournalEntryForBankInterest() == null) {
                                                                    amount -= payment.getBankInterestAmount();
                                                                }

                                                                Set<PaymentDetailOtherwise> paymentDetailsOtherwise = payment.getPaymentDetailOtherwises();
                                                                for (PaymentDetailOtherwise PDO : paymentDetailsOtherwise) { // Tax amount of payment against GL is added.
                                                                    if (PDO.getTaxamount() != 0) {
                                                                        totaltaxamount += PDO.getTaxamount();
                                                                    }
                                                                }

                                                                if (payment.getAdvanceDetails() != null && !payment.getAdvanceDetails().isEmpty()) {
                                                                    for (AdvanceDetail advanceDetail : payment.getAdvanceDetails()) {
                                                                        linkedAmountDue += advanceDetail.getAmountDue();
                                                                        HashMap requestParams = new HashMap<String, Object>();
                                                                        requestParams.put("companyid", valueMap.get("companyID"));
                                                                        requestParams.put("gcurrencyid", valueMap.get("gcurrencyid"));
//                                                                        amountDueInBase = getCurrencyToBaseAmount(requestParams, advanceDetail.getAmountDue(), payment.getCurrency().getCurrencyID(), payment.getJournalEntry().getEntryDate(), payment.getJournalEntry().getExternalCurrencyRate());
                                                                        amountDueInBase = getCurrencyToBaseAmount(requestParams, advanceDetail.getAmountDue(), payment.getCurrency().getCurrencyID(), payment.getCreationDate(), payment.getJournalEntry().getExternalCurrencyRate());
                                                                    }
                                                                }

                                                                totaltaxamount = authHandler.round(totaltaxamount, companyID);
                                                                if (CustomReportConstants.Acc_Sales_Order_Measure_Total_Tax.trim().equalsIgnoreCase(jarrColumns.getJSONObject(j).getString("defaultHeaderOrig").trim())) {
                                                                    tempValue = totaltaxamount;
                                                                }
                                                                if (CustomReportConstants.Acc_Sales_Order_Measure_AmtWithoutTax.trim().equalsIgnoreCase(jarrColumns.getJSONObject(j).getString("defaultHeaderOrig").trim())) {
                                                                    tempValue = amount - totaltaxamount;
                                                                }
                                                                if (CustomReportConstants.Acc_Debit_Note_Amount_Due.trim().equalsIgnoreCase(jarrColumns.getJSONObject(j).getString("defaultHeaderOrig").trim())) {
                                                                    tempValue = linkedAmountDue;
                                                                }
                                                                if (CustomReportConstants.Acc_Make_Payment_Amount_Due_InBase.trim().equalsIgnoreCase(jarrColumns.getJSONObject(j).getString("defaultHeaderOrig").trim())) {
                                                                    tempValue = amountDueInBase;
                                                                }
                                                            }
                                                        }else if (moduleId.equals(String.valueOf(Constants.Acc_Purchase_Requisition_ModuleId))) {
                                                            if (!allowcrossmodule) {
                                                                tempValue = calculatePurchaseRequisitionMeasures(tempString, valueMap, jarrColumns.getJSONObject(j), companyID);
                                                            } 
                                                        }
                                                    } else {
                                                        isEmpty = true;
                                                    }
                                                } else {
                                                    // Added below condition for ERP-23449
                                                    if (reportDataResultSet.getObject(colLabel) != null) {
                                                        Double dBValue = Double.valueOf(String.valueOf(reportDataResultSet.getObject(colLabel)));
                                                        tempValue = dBValue.doubleValue();
                                                    }
                                                }
                                            //tempValue = Double.parseDouble((String) reportDataResultSet.getObject(colLabel));
                                                //tempValue = dBValue.doubleValue();
                                            } else {
                                                isEmpty = true;
                                            }
                                    } catch ( JSONException | ServiceException | NumberFormatException ex) {
                                            //tempValue = (Double) reportDataResultSet.getObject(colLabel);
                                            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                                        }
                                        if ((!isEmpty) || (isEmpty && !isCustomfield)) {
                                            value = authHandler.formattingdecimal(tempValue, precision);
                                            if (precision == 0) {
                                                value = value.substring(0, value.length() - 1);
                                            }
                                        }
                                        break;
                                    // For Date Field    
                                    case 3:
                                        //DateTimeFormatter dtf = DateTimeFormat.forPattern(userPrefObj.getString("DateFormat"));
                                        if (reportDataResultSet.getObject(colLabel) != null) {
                                            if (isCustomfield) {
                                           //if (!StringUtil.isNullOrEmpty((String) reportDataResultSet.getObject(colLabel))) {
                                                //long dateValueLong = Long.parseLong((String) reportDataResultSet.getObject(colLabel).);
                                                    java.sql.Date dateValue=(java.sql.Date)reportDataResultSet.getObject(colLabel);
//                                                    if (valueMap.containsKey("browsertz") && valueMap.get("browsertz") != null && !StringUtil.isNullOrEmpty(valueMap.get("browsertz").toString())) {
//                                                        df2.setTimeZone(TimeZone.getTimeZone("GMT" + valueMap.get("browsertz")));
//                                                    }
                                                if (isEWayReportValidation) {
                                                    value = new SimpleDateFormat("dd/MM/yyyy").format(dateValue);
                                                } else {

                                                value = df2.format(dateValue);
                                                }
                                                    //value = df2.format(dateFromDB);


                                            //}
                                                //Date date = new Date(dateValueLong);
                                                //value = udf.format(date);
                                                //value = udfwtf.format(dateValueLong);
                                                //jarrRecord.put("dateValue", value);
                                            } else {
                                                try {
                                                    if (!CustomReportConstants.COL_TYPE_DATE.equalsIgnoreCase(colType)) {
                                                        if (CustomReportConstants.COL_TYPE_DATETIME.equalsIgnoreCase(colType)) {
                                                            value = udf.format((java.sql.Timestamp) reportDataResultSet.getObject(colLabel));
                                                        } else {
                                                            Long dateValue = (Long) reportDataResultSet.getObject(colLabel);
                                                            long dateValueLong = dateValue.longValue();
                                                            if (valueMap.containsKey("browsertz") && valueMap.get("browsertz") != null && !StringUtil.isNullOrEmpty(valueMap.get("browsertz").toString())) {
                                                                df2.setTimeZone(TimeZone.getTimeZone("GMT" + valueMap.get("browsertz")));
                                                            }
                                                            // Added check for dateValueLong as in case of debit/credir note if its normal debit/credit note then creationdate is fetch from JE
                                                        if(dateValueLong!=0)
                                                                value = df2.format(new java.util.Date(dateValueLong));
                                                        else {
                                                                if (moduleId.equals(String.valueOf(Constants.Acc_Debit_Note_ModuleId))) {
                                                                    JournalEntryDetail details = null;
                                                                    KwlReturnObject dnObjList = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), (String) reportDataResultSet.getObject("id"));
                                                                    DebitNote debitMemo = (DebitNote) dnObjList.getEntityList().get(0);
                                                                    if (debitMemo != null) {
                                                                        KwlReturnObject jeForDebitNote = accCustomerReportServiceDao.getJEForDebitNote(debitMemo.getID());
                                                                        if (jeForDebitNote != null) {
                                                                            List lst = jeForDebitNote.getEntityList();
                                                                            Iterator ite = lst.iterator();
                                                                            while (ite.hasNext()) {
                                                                                String row = (String) ite.next();
                                                                                if (!StringUtil.isNullOrEmpty(row)) {
                                                                                    KwlReturnObject jeObjList = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), row);
                                                                                    JournalEntry jeDetail = (JournalEntry) jeObjList.getEntityList().get(0);
                                                                                    if (jeDetail != null) {
                                                                                        value = df2.format(jeDetail.getEntryDate());
                                                                                    }

                                                                                }
                                                                            }

                                                                        }
                                                                    }

                                                                } else if (moduleId.equals(String.valueOf(Constants.Acc_Credit_Note_ModuleId))) {
                                                                    JournalEntryDetail details = null;
                                                                    KwlReturnObject dnObjList = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), (String) reportDataResultSet.getObject("id"));
                                                                    CreditNote creditMemo = (CreditNote) dnObjList.getEntityList().get(0);
                                                                    if (creditMemo != null) {
                                                                        KwlReturnObject jeForCreditNote = accCustomerReportServiceDao.getJEForCreditNote(creditMemo.getID());
                                                                        if (jeForCreditNote != null) {
                                                                            List lst = jeForCreditNote.getEntityList();
                                                                            Iterator ite = lst.iterator();
                                                                            while (ite.hasNext()) {
                                                                                String row = (String) ite.next();
                                                                                if (!StringUtil.isNullOrEmpty(row)) {
                                                                                    KwlReturnObject jeObjList = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), row);
                                                                                    JournalEntry jeDetail = (JournalEntry) jeObjList.getEntityList().get(0);
                                                                                    if (jeDetail != null) {
                                                                                        Set<JournalEntryDetail> jeDetails = jeDetail.getDetails();
                                                                                        if (jeDetails != null) {
                                                                                            value = df2.format(jeDetail.getEntryDate());
                                                                                        }
                                                                                    }

                                                                                }
                                                                            }

                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        //Date date = new Date(dateValueLong);
                                                            //value = udf.format(date);
                                                        }
                                                    } else {
                                                        dateForSortIndex = new SimpleDateFormat("YYYY-MM-d").format((java.sql.Date) reportDataResultSet.getObject(colLabel));
                                                        if (isEWayReportValidation) {
                                                            value = new SimpleDateFormat("dd/MM/yyyy").format((Date) reportDataResultSet.getObject(colLabel));
                                                        } else {
                                                        value = udf.format((java.sql.Date) reportDataResultSet.getObject(colLabel));
                                                        }
                                                        Calendar cal = Calendar.getInstance();
                                                        cal.setTime((java.sql.Date) reportDataResultSet.getObject(colLabel));
                                                        //month = cal.get(Calendar.MONTH);
                                                        month = new SimpleDateFormat("MMM").format(cal.getTime());
                                                        day = cal.get(Calendar.DAY_OF_MONTH);
                                                        year = cal.get(Calendar.YEAR);
                                                    }
                                                } catch (InvalidResultSetAccessException ex) {
                                                    //value = udf.format(reportDataResultSet.getObject(colLabel));
                                                    Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                                                }
                                                //jarrRecord.put("dateValue", value);
                                            }
                                        }
                                        break;
                                    // For Drop Down Single Select
                                    case 4:
                                        value = (String) reportDataResultSet.getObject(colLabel);
                                        if (isCustomfield) {
                                            //String actualValue = getFieldComboDataValue(value);
                                            String actualValue = value;
                                            value = actualValue;

                                        }
//                                    if (!StringUtil.isNullOrEmpty(value)) {
//                                        if (moduleId.equals(String.valueOf(Constants.Acc_Invoice_ModuleId)) || moduleId.equals(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId))) {//Product Name and UOM's xtype is 4
//                                            if (colLabel.equalsIgnoreCase("#Product Name#")) {
//                                                KwlReturnObject rdresult1 = accCustomerReportServiceDao.getObject(Product.class.getName(), value);
//                                                Product fieldComboProductData = (Product) rdresult1.getEntityList().get(0);
//                                                value = fieldComboProductData != null ? fieldComboProductData.getName() : "";
//                                            }
//                                            if (colLabel.equalsIgnoreCase("#UOM#")) {
//                                                KwlReturnObject rdresultUOM = accCustomerReportServiceDao.getObject(UnitOfMeasure.class.getName(), value);
//                                                UnitOfMeasure fieldUOM = (UnitOfMeasure) rdresultUOM.getEntityList().get(0);
//                                                value = fieldUOM != null ? fieldUOM.getName() : "";
//                                            }
//                                        }
//                                    }
                                        //Added Below to Check Sales Order is Replacement SO or Not,if not then set the value of replacement string to empty
                                        if (CustomReportConstants.Acc_Sales_Order_Rep_Number_Field.equalsIgnoreCase(colLabel.toLowerCase())) {
                                            if (!StringUtil.isNullOrEmpty(value)) {
                                                if (moduleId.equals(String.valueOf(Constants.Acc_Sales_Order_ModuleId))) {
                                                    String billid = (String) reportDataResultSet.getObject(rsmd.getColumnLabel(columnNames.length));
                                                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), billid);
                                                    SalesOrder salesOrder = (SalesOrder) objItr.getEntityList().get(0);
                                                    if (salesOrder != null) {
                                                        if (!salesOrder.isIsReplacementSO()) {
                                                            value = "";
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        break;
                                    // For Drop Down Multi Select
                                    case 7:
                                        value = (String) reportDataResultSet.getObject(colLabel);
                                        if (isCustomfield) {
                                            String actualValue = getFieldComboDataValue(value);
                                            value = actualValue;
                                        }
                                        break;
                                    // For Check Box   
                                    case 11:
                                        value = (String) reportDataResultSet.getObject(colLabel);
                                         if (moduleId.equals(String.valueOf(Constants.Acc_Delivery_Order_ModuleId)) || moduleId.equals(String.valueOf(Constants.Acc_Sales_Return_ModuleId)) || moduleId.equals(String.valueOf(Constants.Acc_Purchase_Return_ModuleId)) || moduleId.equals(String.valueOf(Constants.Acc_Goods_Receipt_ModuleId))) {
                                            if (("#" + "Eway Export Status" + "#").equalsIgnoreCase(colLabel)) {
                                                if (value.equals("T")) {
                                                    value = "Generated";
                                                } else if (value.equals("F")) {
                                                    value = "Pending";
                                        }

                                            }
                                        }
//                                        if (value != null && !value.equalsIgnoreCase("")) {
//                                            value = value.toUpperCase();
//                                        }
                                        if (StringUtil.isNullOrEmpty(value)) {
                                            value = "FALSE";
                                        }
                                        break;
                                    // For Check List
                                    case 12:
                                        value = (String) reportDataResultSet.getObject(colLabel);
                                        if (isCustomfield) {
                                            String actualValue = getFieldComboDataValue(value);
                                            value = actualValue;
                                        }
                                        break;
                                    default:
                                        value = (String) reportDataResultSet.getObject(colLabel);
                                        break;

                                }
                                if(exportInCaps && value != null && xType!=2 ){
                                    value = value.toUpperCase();
                                }
                                if (jarrColumns.getJSONObject(j).getString("defaultHeader").equalsIgnoreCase(columnLabel) /*&& jarrColumns.getJSONObject(j).getString("dataIndexTableName").equalsIgnoreCase(dbtablename)*/) {
                                if(jarrColumns.getJSONObject(j).getString("defaultHeader").equalsIgnoreCase(CustomReportConstants.UOM) && (jarrColumns.getJSONObject(j).optBoolean("isDisplayUOM",false))){
                                        jarrRecord.put("displayUOM", value);
                                    } else if(jarrColumns.getJSONObject(j).getString("defaultHeader").equalsIgnoreCase(CustomReportConstants.Display_UOM_Mapping) ){
                                        jarrRecord.put("displayUOMMapping", value);
                                    }
                                else {
                                    if(xType == 2) {
                                        if(!StringUtil.isNullOrEmpty(value)) {
//                                            if (forExport) {
//                                                jarrRecord.put(jarrColumns.getJSONObject(j).getString("dataIndex"), jarrRecord.optString("currencysymbol_"+jarrColumns.getJSONObject(j).getString("dataIndex").replaceAll("-", ""))+" "+ Double.parseDouble(value));
//                                            } else {
                                                jarrRecord.put(jarrColumns.getJSONObject(j).getString("dataIndex"), Double.parseDouble(value));
                                                 break;
                                            }
//                                        }
                                        } else if (xType == 3) {
                                            jarrRecord.put(jarrColumns.getJSONObject(j).getString("dataIndex"), value);
                                            jarrRecord.put("date_" + jarrColumns.getJSONObject(j).getString("dataIndex"), (dateForSortIndex != null) ? dateForSortIndex : "");
                                    } else  {
                                            jarrRecord.put(jarrColumns.getJSONObject(j).getString("dataIndex"), value);
                                            break;
                                        }
                                        if (xType == 3) {
                                            if (year != 0) {
                                            jarrRecord.put("year_" + jarrColumns.getJSONObject(j).getString("dataIndex"), year !=0 ? year : null);
                                            }
                                            if (!StringUtil.isNullOrEmpty(month)) {
                                                jarrRecord.put("month_" + jarrColumns.getJSONObject(j).getString("dataIndex"), month);
                                            }
                                            if (day != 0) {
                                                jarrRecord.put("day_" + jarrColumns.getJSONObject(j).getString("dataIndex"), day != 0 ? day : null);
                                            }
                                        }
                                    }

                                } else if (isCustomfield && jarrColumns.getJSONObject(j).getString("defaultHeader").equalsIgnoreCase(columnLabel)) {//Custom Field
                                    jarrRecord.put(jarrColumns.getJSONObject(j).getString("dataIndex"), value);
                                } else if (jarrColumns.getJSONObject(j).getString("defaultHeader").equalsIgnoreCase(columnLabel) && columnLabel.equalsIgnoreCase(CustomReportConstants.AMOUNT) && jarrColumns.getJSONObject(j).optBoolean("isLineItem", false) == true) {
                                    jarrRecord.put(jarrColumns.getJSONObject(j).getString("dataIndex"), value);
                                }
                            }
                        } else if (gstColumnsMap != null && gstColumnsMap.containsKey(jarrColumns.getJSONObject(j).getString("defaultHeader")) && (jarrColumns.getJSONObject(j).getString("defaultHeader").contains(CustomReportConstants.Acc_CGST)|| jarrColumns.getJSONObject(j).getString("defaultHeader").contains(CustomReportConstants.Acc_SGST)|| jarrColumns.getJSONObject(j).getString("defaultHeader").contains(CustomReportConstants.Acc_IGST)|| jarrColumns.getJSONObject(j).getString("defaultHeader").contains(CustomReportConstants.Acc_UTGST)|| jarrColumns.getJSONObject(j).getString("defaultHeader").contains(CustomReportConstants.Acc_CESS)|| jarrColumns.getJSONObject(j).getString("defaultHeader").contains(CustomReportConstants.Acc_SGST)|| jarrColumns.getJSONObject(j).getString("defaultHeader").contains(CustomReportConstants.Acc_IGST)|| jarrColumns.getJSONObject(j).getString("defaultHeader").contains(CustomReportConstants.Acc_UTGST)|| jarrColumns.getJSONObject(j).getString("defaultHeader").contains(CustomReportConstants.Acc_E_Way_TAX_Rate))){
                            String gstFieldColLabel = gstColumnsMap.get(jarrColumns.getJSONObject(j).getString("defaultHeader"));
                                String value = "";
                                int precision = 2;
                                int xType = StringUtil.getInteger(jarrColumns.getJSONObject(j).getString("xtype"));
                                isCustomfield = StringUtil.getBoolean(jarrColumns.getJSONObject(j).getString("custom"));
                                if (jarrColumns.optJSONObject(j) != null) {
                                    precision = StringUtil.getInteger(jarrColumns.getJSONObject(j).optString("precision", defaultPrecision));
                        }
                                colLabel = gstFieldColLabel;                       
                                isProcessValue = true;
                                if (isChartRequest && !stringList.contains(colLabel.toLowerCase())) {
                                    isProcessValue = false;
                        }
                                if (isProcessValue) {
                                    switch (xType) {
                                        // For Number Field
                                        case 2:
                                        case 1:
                                            double tempValue = 0;
                                            boolean isEmpty = false;
                                            try {
                                                if (reportDataResultSet.getObject(colLabel) != null) {
                                                    if (reportDataResultSet.getObject(colLabel) instanceof String) {
                                                        String tempString = (String) reportDataResultSet.getObject(colLabel);
                                                        if (!StringUtil.isNullOrEmpty(tempString)) {
                                                            if (!jarrColumns.getJSONObject(j).getBoolean("isMeasureItem")) {                                                         
                                                                if (colLabel.contains(CustomReportConstants.Acc_CGST + "#") || colLabel.contains(CustomReportConstants.Acc_SGST + "#") || colLabel.contains(CustomReportConstants.Acc_IGST + "#") || colLabel.contains(CustomReportConstants.Acc_UTGST + "#") || colLabel.contains(CustomReportConstants.Acc_CESS + "#")||colLabel.equals("#" + CustomReportConstants.Acc_E_Way_TAX_Rate+ "#")) {
                                                                     if(jarrColumns.getJSONObject(j).optString("defaultHeader").equalsIgnoreCase(CustomReportConstants.Acc_E_Way_TAX_Rate)){
                                                                            value = getEwayGSTTaxRatesString(tempString, lineLevelTerms);
                                                                            }   else { 
                                                                            tempValue = getIndividualGSTTaxes(companyID,tempString, jarrColumns.getJSONObject(j).getString("id").trim(), lineLevelTerms,lineLevelTermsMap);
                                                                            }
                                                                } else {
                                                                    tempValue = Double.parseDouble(tempString);
                                                                }
                                                            }
                                                        } else {
                                                            isEmpty = true;
                                                        }
                                                    }
                                                } else {
                                                    isEmpty = true;
                                                }
                                            } catch (JSONException | NumberFormatException ex) {                                                
                                                Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                            if (((!isEmpty) || (isEmpty && !isCustomfield)) && !jarrColumns.getJSONObject(j).optString("defaultHeader").equalsIgnoreCase(CustomReportConstants.Acc_E_Way_TAX_Rate)) {
                                                value = authHandler.formattingdecimal(tempValue, precision);
                                                if (precision == 0) {
                                                    value = value.substring(0, value.length() - 1);
                                                }
                                            }
                                            break;

                                        default:
                                            value = (String) reportDataResultSet.getObject(colLabel);
                                            break;

                                    }
                                    if (exportInCaps && value != null && xType != 2) {
                                        value = value.toUpperCase();
                                    }
                                    if (!StringUtil.isNullOrEmpty(value)) {
                                        if (value.contains("+")) {
                                            jarrRecord.put(jarrColumns.getJSONObject(j).getString("dataIndex"), value);
                                        } else {
                                        jarrRecord.put(jarrColumns.getJSONObject(j).getString("dataIndex"), Double.parseDouble(value));
                                    }                                
                                    }                                
                                    if (isCustomfield && jarrColumns.getJSONObject(j).getString("defaultHeader").equalsIgnoreCase(columnLabel)) {//Custom Field
                                        jarrRecord.put(jarrColumns.getJSONObject(j).getString("dataIndex"), value);
                                    } else if (jarrColumns.getJSONObject(j).getString("defaultHeader").equalsIgnoreCase(columnLabel) && columnLabel.equalsIgnoreCase(CustomReportConstants.AMOUNT) && jarrColumns.getJSONObject(j).optBoolean("isLineItem", false) == true) {
                                        jarrRecord.put(jarrColumns.getJSONObject(j).getString("dataIndex"), value);
                                    }
                                }

                            }
                        }
                        count++;
                    }
//                }  catch (InvalidResultSetAccessException | JSONException | ServiceException | NumberFormatException | ClassCastException ex) {
//                    Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
//                }
                }
                jarrRecords.put(jarrRecord);
                recordCount++;
            }
        }
    }
    
    /**
     * 
     * @param jarrColumns
     * @param rsmd
     * @returnd
     * @throws JSONException 
     */
    private Map getGstColumnsMap(JSONArray jarrColumns, SqlRowSetMetaData rsmd) throws JSONException {
        Map gstColumnsMap = new HashMap<String, String>();
        for (int i = 0; i < jarrColumns.length(); i++) {
            JSONObject columnJobj = jarrColumns.getJSONObject(i);
            String columnDefaultHeader = columnJobj.getString("defaultHeader");
            boolean isGstColumnHeader = columnDefaultHeader.contains(CustomReportConstants.Acc_CGST) || columnDefaultHeader.contains(CustomReportConstants.Acc_SGST) || columnDefaultHeader.contains(CustomReportConstants.Acc_IGST) || columnDefaultHeader.contains(CustomReportConstants.Acc_UTGST) || columnDefaultHeader.contains(CustomReportConstants.Acc_CESS) || columnDefaultHeader.contains(CustomReportConstants.Acc_E_Way_TAX_Rate);
            if (isGstColumnHeader) {
                for (int j = 1; j < rsmd.getColumnCount(); j++) {
                    String resultSetColumnName = rsmd.getColumnLabel(j);
                    boolean isGstResultSetColumn = resultSetColumnName.contains(CustomReportConstants.Acc_CGST + "#") || resultSetColumnName.contains(CustomReportConstants.Acc_SGST + "#") || resultSetColumnName.contains(CustomReportConstants.Acc_IGST + "#") || resultSetColumnName.contains(CustomReportConstants.Acc_UTGST + "#") || resultSetColumnName.contains(CustomReportConstants.Acc_CESS + "#")|| resultSetColumnName.contains(CustomReportConstants.Acc_E_Way_TAX_Rate + "#");
                    if (isGstResultSetColumn) {
                        boolean isSingleGstColumnInResultSet = columnDefaultHeader.startsWith(CustomReportConstants.Acc_CGST) || columnDefaultHeader.startsWith(CustomReportConstants.Acc_SGST) || columnDefaultHeader.startsWith(CustomReportConstants.Acc_IGST) || columnDefaultHeader.startsWith(CustomReportConstants.Acc_UTGST) || columnDefaultHeader.startsWith(CustomReportConstants.Acc_CESS)|| columnDefaultHeader.startsWith(CustomReportConstants.Acc_E_Way_TAX_Rate);
                        if (isSingleGstColumnInResultSet || (StringUtil.equal(resultSetColumnName.substring(1, resultSetColumnName.indexOf(" ")), columnDefaultHeader.substring(0, columnDefaultHeader.indexOf(" "))))) {
                            gstColumnsMap.put(columnDefaultHeader, resultSetColumnName);
                            break;
                        }
                    }
                }
            }
        }
        return gstColumnsMap;
    }
    
    
    /**
     * This method fetch the default field mappings data
     *
     * @param mainTable
     * @param header
     * @param jObj
     * @throws com.krawler.common.service.ServiceException
     * @throws com.krawler.utils.json.base.JSONException
     *
     */
    
    public void fetchDefaultFieldHeaderMappings(String mainTable, DefaultHeader header, JSONObject jObj) throws ServiceException, JSONException {

        KwlReturnObject defaultFieldMappingResult = null;
        String joinTableName;
        if (header.isIsDefaultFieldMappings()) {
            defaultFieldMappingResult = accCustomerReportServiceDao.getDefaultFieldMappings(header.getId());
            if (defaultFieldMappingResult != null) {
                List<AccCustomReportsDefaultFieldsMapping> crDefaultFM = defaultFieldMappingResult.getEntityList();
                Iterator crDefaultFM_itr = crDefaultFM.iterator();
                while (crDefaultFM_itr.hasNext()) {
                    JSONObject defaultmappingJSONObj = new JSONObject();
                    joinTableName = "";
                    AccCustomReportsDefaultFieldsMapping obj = (AccCustomReportsDefaultFieldsMapping) crDefaultFM_itr.next();
                    defaultmappingJSONObj.put("id", obj.getId());
                    defaultmappingJSONObj.put("defaultheaderid", obj.getDefaultheaderid());
                    defaultmappingJSONObj.put("measurefieldid", obj.getDefaultfieldid());
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(DefaultHeader.class.getName(), obj.getDefaultheaderid());
                    DefaultHeader mappingDH = (DefaultHeader) objItr.getEntityList().get(0);
                    defaultmappingJSONObj.put("dbcolumnname", mappingDH.getDbcolumnname());
                    defaultmappingJSONObj.put("dbtabletame", mappingDH.getDbTableName());
                    defaultmappingJSONObj.put("reftabledatacolumn", mappingDH.getReftabledatacolumn());
                    defaultmappingJSONObj.put("reftablename", mappingDH.getReftablename());
                    defaultmappingJSONObj.put("reftablefk", mappingDH.getReftablefk());
                    //mappingJSONObj.put("defaultHeader", mappingDH.getReftablename()+"."+mappingDH.getReftablefk());
                    String mappingDefaultHeader = mappingDH.getReftablename() + "." + mappingDH.getReftabledatacolumn();
                    if(mappingDH.getReftabledatacolumn() != null && mappingDH.getReftabledatacolumn().contains("(") && mappingDH.getReftabledatacolumn().contains(")")){
                        mappingDefaultHeader = mappingDH.getReftabledatacolumn();
                    }
                    defaultmappingJSONObj.put("defaultHeader", mappingDefaultHeader);
                    defaultmappingJSONObj.put("isSelectDataIndex", obj.isIsSelectDataIndex());
                    if (obj.isIsSelectDataIndex()) {
                        defaultmappingJSONObj.put("defaultHeaderParentName", header.getDefaultHeader());
                    }
                    if (mainTable.equalsIgnoreCase(mappingDH.getReftablename().trim())) {
                        joinTableName = mappingDH.getDbTableName().trim();
                    } else {
                        joinTableName = mappingDH.getReftablename().trim();
                    }
                    String defaultextrainnerjoin = " left join " + joinTableName + " on " + mappingDH.getDbTableName().trim() + "." + mappingDH.getDbcolumnname().trim() + " = " + mappingDH.getReftablename().trim() + "." + mappingDH.getReftablefk().trim() + " ";
                    defaultmappingJSONObj.put("defaultextrainnerjoin", defaultextrainnerjoin);
                    jObj.append("defaultmappingJSONObj", defaultmappingJSONObj);
                }
            }
        }
    }

    @Override
    public String getApprovalStatus(Map<String, Object> requestParams) throws ServiceException {
        int approvalStatusLevel =0;
        String approvalStatus = "";
        HashMap<String, Object> qdDataMap = new HashMap<String, Object>();
        if (requestParams != null) {
            String billid = (String) requestParams.get("billid");
            if(!StringUtil.isNullOrEmpty((String)requestParams.get("moduleID"))) {
                int module = Integer.parseInt((String)requestParams.get("moduleID"));
                if(Constants.Acc_Sales_Order_ModuleId == module){
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), billid);
                    SalesOrder salesOrder = (SalesOrder) objItr.getEntityList().get(0);
                    if (salesOrder != null) {
                        approvalStatusLevel = salesOrder.getApprovestatuslevel();
                        qdDataMap.put("moduleid", Constants.Acc_Sales_Order_ModuleId);
                    }
                }
                else if(Constants.Acc_Purchase_Order_ModuleId == module){
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), billid);
                    PurchaseOrder purchaseOrder = (PurchaseOrder) objItr.getEntityList().get(0);
                    if (purchaseOrder != null) {
                        approvalStatusLevel = purchaseOrder.getApprovestatuslevel();
                        qdDataMap.put("moduleid", Constants.Acc_Purchase_Order_ModuleId);
                    }
                }
                
                else if(Constants.Acc_Goods_Receipt_ModuleId == module){
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceiptOrder.class.getName(), billid);
                    GoodsReceiptOrder grOrder = (GoodsReceiptOrder) objItr.getEntityList().get(0);
                    if (grOrder != null) {
                        approvalStatusLevel = grOrder.getApprovestatuslevel();
                        qdDataMap.put("moduleid", Constants.Acc_Goods_Receipt_ModuleId);
                    }
                }
                
                else if(Constants.Acc_Delivery_Order_ModuleId == module){
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), billid);
                    DeliveryOrder deliveryOrder = (DeliveryOrder) objItr.getEntityList().get(0);
                    if (deliveryOrder != null) {
                        approvalStatusLevel = deliveryOrder.getApprovestatuslevel();
                        qdDataMap.put("moduleid", Constants.Acc_Delivery_Order_ModuleId);
                    }
                } else if (Constants.Acc_Make_Payment_ModuleId == module) {
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Payment.class.getName(), billid);
                    Payment payment = (Payment) objItr.getEntityList().get(0);
                    if (payment != null) {
                        approvalStatusLevel = payment.getApprovestatuslevel();
                        qdDataMap.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                    }
                } else if (Constants.Acc_Receive_Payment_ModuleId == module) {
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), billid);
                    Receipt receipt = (Receipt) objItr.getEntityList().get(0);
                    if (receipt != null) {
                        approvalStatusLevel = receipt.getApprovestatuslevel();
                        qdDataMap.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                    }
                } else if (Constants.Acc_Purchase_Requisition_ModuleId == module) {
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(PurchaseRequisition.class.getName(), billid);
                    PurchaseRequisition purchaseReq = (PurchaseRequisition) objItr.getEntityList().get(0);
                    if (purchaseReq != null) {
                        approvalStatusLevel = purchaseReq.getApprovestatuslevel();
                        qdDataMap.put("moduleid", Constants.Acc_Purchase_Requisition_ModuleId);
                    }
                }

                if(Constants.Acc_Invoice_ModuleId == module || Constants.Acc_FixedAssets_DisposalInvoice_ModuleId == module ){//Approval Status
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Invoice.class.getName(), billid);
                    Invoice invoice = (Invoice) objItr.getEntityList().get(0);
                    if (invoice != null) {
                        approvalStatusLevel = invoice.getApprovestatuslevel();
                        qdDataMap.put("moduleid", Constants.Acc_Invoice_ModuleId);
                    }
                }

                if(Constants.Acc_Vendor_Invoice_ModuleId == module || Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId == module ){//Approval Status
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), billid);
                    GoodsReceipt goodsReceipt = (GoodsReceipt) objItr.getEntityList().get(0);
                    if (goodsReceipt != null) {
                        approvalStatusLevel = goodsReceipt.getApprovestatuslevel();
                        qdDataMap.put("moduleid", Constants.Acc_Vendor_Invoice_ModuleId);
                    }
                }
            }


            if (approvalStatusLevel < 0) {
                approvalStatus = "Rejected";
            } else if (approvalStatusLevel < Constants.MaximumLimitOfLevelsInMultilevelApproval) {
                String ruleid = "", userRoleName = "";

                qdDataMap.put("companyid", requestParams.get("companyid"));
                qdDataMap.put("level", approvalStatusLevel);

                KwlReturnObject flowresult = accCustomerReportServiceDao.getMultiApprovalRuleData(qdDataMap);
                List<Object[]> ruleList = flowresult.getEntityList();
                for (Object[] rulerow : ruleList) {
                    ruleid = rulerow[0].toString();
                }
                if (!StringUtil.isNullOrEmpty(ruleid)) {
                    qdDataMap.put("ruleid", ruleid);
                    KwlReturnObject userResult = accCustomerReportServiceDao.getApprovalRuleTargetUsers(qdDataMap);
                    List<Object[]> useritr = userResult.getEntityList();
                    for (Object[] userrow : useritr) {
                        String userId = userrow[0].toString();
                        String userName = userrow[1].toString();
                        KwlReturnObject kmsg = null;
                        String roleName = "Company User";
                        kmsg = accCustomerReportServiceDao.getRoleofUser(userId);
                        Iterator ite2 = kmsg.getEntityList().iterator();
                        while (ite2.hasNext()) {
                            Object[] row = (Object[]) ite2.next();
                            roleName = row[1].toString();
                        }
                        userRoleName += roleName + " " + userName + ",";
                    }
                }
                if (!StringUtil.isNullOrEmpty(userRoleName)) {
                    userRoleName = userRoleName.substring(0, userRoleName.length() - 1);
                }
                approvalStatus = "Pending Approval" + (StringUtil.isNullOrEmpty(userRoleName) ? "" : " by " + userRoleName) + " at Level - " + approvalStatusLevel;
            } else {
                approvalStatus = "Approved";
            }

        }
        return approvalStatus;
    }


    public StringBuilder fetchDefaultFieldHeaderNeededMappings(String mainTable, String moduleID,Map<String, String> joins, int cntVal) throws ServiceException, JSONException {
        StringBuilder defaultextrainnerjoin = new StringBuilder();
        JSONObject jObj = new JSONObject();
        String joinTableName;
        boolean isJoinPresent = false;
        KwlReturnObject mappingDefaultFieldsNeededResult = accCustomerReportServiceDao.getCustomReportsDefaultFieldsNeededMapping(moduleID, true);
        if (mappingDefaultFieldsNeededResult != null) {
            List<AccCustomReportsCustomFieldsMapping> list = mappingDefaultFieldsNeededResult.getEntityList();
            if (list != null) {
                Iterator crDefaultFM_itr = list.iterator();
                //list.get(0).getDefaultheaderid();
                int i = cntVal + 1;
                while (crDefaultFM_itr.hasNext()) {
                    JSONObject defaultmappingJSONObj = new JSONObject();
                    isJoinPresent = false;
                    AccCustomReportsCustomFieldsMapping obj = (AccCustomReportsCustomFieldsMapping) crDefaultFM_itr.next();
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(DefaultHeader.class.getName(), obj.getDefaultheaderid());
                    DefaultHeader mappingDH = (DefaultHeader) objItr.getEntityList().get(0);
                    //defaultextrainnerjoin = " left join " + mappingDH.getReftablename() + " on " + mappingDH.getReftablename().trim() + "." + mappingDH.getReftablefk().trim() + " = " + mappingDH.getDbTableName().trim() + "." + mappingDH.getDbcolumnname() + " ";
                    defaultmappingJSONObj.put("id", obj.getId());
                    defaultmappingJSONObj.put("defaultheaderid", obj.getDefaultheaderid());
                    defaultmappingJSONObj.put("dbcolumnname", mappingDH.getDbcolumnname());
                    defaultmappingJSONObj.put("dbtabletame", mappingDH.getDbTableName());
                    defaultmappingJSONObj.put("reftabledatacolumn", mappingDH.getReftabledatacolumn());
                    defaultmappingJSONObj.put("reftablename", mappingDH.getReftablename());
                    defaultmappingJSONObj.put("reftablefk", mappingDH.getReftablefk());
                    //mappingJSONObj.put("defaultHeader", mappingDH.getReftablename()+"."+mappingDH.getReftablefk());
                    defaultmappingJSONObj.put("defaultHeader", mappingDH.getReftablename() + "." + mappingDH.getReftabledatacolumn());

                    if (mainTable.equalsIgnoreCase(mappingDH.getReftablename().trim())) {
                        joinTableName = mappingDH.getDbTableName().trim();
                    } else {
                        joinTableName = mappingDH.getReftablename().trim();
                    }
                    if(joins.containsKey(joinTableName)){
                        cntVal =1;
                    }
                    for (String key : joins.keySet()) {
                        if (joins.get(key).contains(mappingDH.getDbTableName().trim() + "." + mappingDH.getDbcolumnname().trim()) && joins.get(key).contains(mappingDH.getReftablefk().trim()) && joins.get(key).contains(joinTableName)) {
                            isJoinPresent = true;
                            break;
                        }
                    }
                    if (!isJoinPresent) {
                        if (joins.containsKey(joinTableName)) {
                            defaultextrainnerjoin.append(" left join " + joinTableName + " as " + joinTableName + (cntVal == 0 ? "" : i) + " on " + joinTableName + (cntVal == 0 ? "" : i) + "." + mappingDH.getReftablefk().trim() + " = " + mappingDH.getDbTableName().trim() + "." + mappingDH.getDbcolumnname().trim() + " ");
                            joins.put(joinTableName + (cntVal == 0 ? "" : i), " left join " + joinTableName + " as " + joinTableName + (cntVal == 0 ? "" : i) + " on " + joinTableName + (cntVal == 0 ? "" : i) + "." + mappingDH.getReftablefk().trim() + " = " + mappingDH.getDbTableName().trim() + "." + mappingDH.getDbcolumnname().trim() + " ");
                        } else {
                            defaultextrainnerjoin.append(" left join " + joinTableName + " on " + joinTableName + "." + mappingDH.getReftablefk().trim() + " = " + mappingDH.getDbTableName().trim() + "." + mappingDH.getDbcolumnname().trim() + " ");
                            joins.put(joinTableName," left join " + joinTableName + " on " + joinTableName + "." + mappingDH.getReftablefk().trim() + " = " + mappingDH.getDbTableName().trim() + "." + mappingDH.getDbcolumnname().trim() + " ");
                        }
                    }
                    //defaultextrainnerjoin = " left join " + joinTableName + " on " + mappingDH.getReftablename().trim() + "." + mappingDH.getReftablefk().trim() + " = " + mappingDH.getDbTableName().trim() + "." + mappingDH.getDbcolumnname().trim() + " ";
                    defaultmappingJSONObj.put("defaultextrainnerjoin", defaultextrainnerjoin);
                    jObj.append("defaultmappingJSONObj", defaultmappingJSONObj);
                    i++;
                }
            }
        }
        return defaultextrainnerjoin;

    }

    @Override
    public JSONArray mapDataArrToSelectedRows(JSONArray selectedRowsJSON, JSONArray dataJArr, Map valueMap) throws ServiceException {
        JSONArray jArray = new JSONArray();
//        JSONObject jobj = new JSONObject();
        JSONObject dataIndexDNobj = new JSONObject();
        JSONObject accountDataIndexObj = new JSONObject();
        JSONArray accountDataIndexArr = new JSONArray();
        JSONObject invoiceDataIndexobj = new JSONObject();
        JSONArray invoiceDataIndexArr = new JSONArray();
        JSONObject expanderObj = null;
        String[] selectedRowsDHName = null;
        StringBuilder stb = new StringBuilder();
        List<String> selectedRowsDHList = null;
        List<String> selectedRowsCustomDHList = null;
        Map<String, String> customDHMap = new HashMap<String, String>();
        DateFormat dateFormat = new SimpleDateFormat((String) valueMap.get("userDateFormat"));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Constants.MMMMdyyyy);
        //SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormatPattern);;
        //List<String> selectedRowsCustomDHOrigList = null;
        String amount_Excluding_GST_HeaderFiledID="";
        if (selectedRowsJSON != null) {
            selectedRowsDHName = new String[selectedRowsJSON.length()];
            try {
                for (int selectedRowsJSONCnt = 0; selectedRowsJSONCnt < selectedRowsJSON.length(); selectedRowsJSONCnt++) {
                    JSONObject selectedRowsJSONElement = selectedRowsJSON.getJSONObject(selectedRowsJSONCnt);
                    if (selectedRowsJSONElement.optBoolean("customfield", false)) {
                        stb.append(("Custom_" + selectedRowsJSONElement.optString("defaultHeader", "")).trim() + ",");
                        customDHMap.put(selectedRowsJSONElement.getString("id"), selectedRowsJSONElement.optString("defaultHeader", ""));
                        accountDataIndexArr.put(selectedRowsJSONElement.getString("id"));
                    } else {
                        selectedRowsDHName[selectedRowsJSONCnt] = selectedRowsJSONElement.optString("defaultHeader", "").trim();
                        if (selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.Acc_Account)
                                || selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.TYPE)
                                || selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.Acc_Tax_Percent)
                                || selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.Acc_Sales_Order_Measure_Total_Tax)
                                || selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.AMOUNT)
                                || selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.Acc_Description)
                                || selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.Acc_Amount_Excluding_GST)) {
                            accountDataIndexArr.put(selectedRowsJSONElement.getString("id"));
                            //Below line is added to get the id for "Amount Excluding GST" field and pass it as key with the value for totalamountforaccount to work with export report as xls option(exportSalesOrderforCustomReportBuilder)
                            if(selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.Acc_Amount_Excluding_GST)){
                                amount_Excluding_GST_HeaderFiledID = selectedRowsJSONElement.getString("id");
                            }
                        } else if (selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.Acc_Invoice_Number)
                                || selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.Acc_Creation_Date)
                                || selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.Acc_Due_Date)
                                || selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.Acc_Linking_Date)
                                || selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.Acc_Invoice_Amount)
                                || selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.Acc_Debit_Note_Amount_Due)) {
                            invoiceDataIndexArr.put(selectedRowsJSONElement.getString("id"));

                        }
                    }
                }
            } catch (JSONException ex) {
                Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            selectedRowsDHList = new LinkedList<String>(Arrays.asList(selectedRowsDHName));
            selectedRowsCustomDHList = new LinkedList<String>(Arrays.asList(stb.toString().split(",")));
        }

        if (dataJArr != null && selectedRowsDHList != null) {
            try {
                //jobj.put("dataIndexDNobj", dataIndexDNobj);
                JSONArray dataResultArray = dataJArr;
                JSONObject jobj = null;
                for (int dataResultArrayCnt = 0; dataResultArrayCnt < dataResultArray.length(); dataResultArrayCnt++) {
                    expanderObj = new JSONObject();
                    jobj = new JSONObject();
                    JSONObject dataResultArrayElement = dataResultArray.getJSONObject(dataResultArrayCnt);

                    for (int i = 0; i < jArray.length(); ++i) {
                        JSONObject rec = jArray.getJSONObject(i);

                        if (rec.getString("billid").equals(dataResultArrayElement.getString("billid"))) {
                            jobj = rec;
                            break;
                        }
                    }

                    jobj.put("billid", dataResultArrayElement.get("billid"));
                    expanderObj.put("currencysymbol", dataResultArrayElement.get("currencysymbol"));

                    if (dataResultArrayElement.has("isaccountdetails")) {
                        if (selectedRowsDHList.indexOf(CustomReportConstants.Acc_Account.trim()) >= 0) {
                            expanderObj.put(CustomReportConstants.Acc_Account, dataResultArrayElement.get("accountname"));
                            accountDataIndexObj.put(CustomReportConstants.Acc_Account, selectedRowsJSON.getJSONObject(selectedRowsDHList.indexOf(CustomReportConstants.Acc_Account.trim())).get("id"));
                        }
                        if (selectedRowsDHList.indexOf(CustomReportConstants.TYPE.trim()) >= 0) {
                            expanderObj.put(CustomReportConstants.TYPE, dataResultArrayElement.get("debit"));
                            accountDataIndexObj.put(CustomReportConstants.TYPE, selectedRowsJSON.getJSONObject(selectedRowsDHList.indexOf(CustomReportConstants.TYPE.trim())).get("id"));
                        }
                        if (selectedRowsDHList.indexOf(CustomReportConstants.Acc_Tax_Percent.trim()) >= 0) {
                            if(dataResultArrayElement.get("taxpercent") instanceof Integer){
                              expanderObj.put(CustomReportConstants.Acc_Tax_Percent, ((Integer)dataResultArrayElement.get("taxpercent")>0?(Integer)dataResultArrayElement.get("taxpercent"):0));
                            } else if(dataResultArrayElement.get("taxpercent") instanceof Double) {
                              expanderObj.put(CustomReportConstants.Acc_Tax_Percent, ((Double)dataResultArrayElement.get("taxpercent")>0?(Double)dataResultArrayElement.get("taxpercent"):0));
                            }
                            accountDataIndexObj.put(CustomReportConstants.Acc_Tax_Percent, selectedRowsJSON.getJSONObject(selectedRowsDHList.indexOf(CustomReportConstants.Acc_Tax_Percent.trim())).get("id"));
                        }
                        if (selectedRowsDHList.indexOf(CustomReportConstants.Acc_Sales_Order_Measure_Total_Tax.trim()) >= 0) {
                            expanderObj.put(CustomReportConstants.Acc_Sales_Order_Measure_Total_Tax, dataResultArrayElement.get("taxamount"));
                            accountDataIndexObj.put(CustomReportConstants.Acc_Sales_Order_Measure_Total_Tax, selectedRowsJSON.getJSONObject(selectedRowsDHList.indexOf(CustomReportConstants.Acc_Sales_Order_Measure_Total_Tax.trim())).get("id"));
                        }
                        if (selectedRowsDHList.indexOf(CustomReportConstants.AMOUNT.trim()) >= 0) {
                            expanderObj.put(CustomReportConstants.AMOUNT, dataResultArrayElement.get("totalamount"));
                            accountDataIndexObj.put(CustomReportConstants.AMOUNT, selectedRowsJSON.getJSONObject(selectedRowsDHList.indexOf(CustomReportConstants.AMOUNT.trim())).get("id"));
                        }
                        if (selectedRowsDHList.indexOf(CustomReportConstants.Acc_Description.trim()) >= 0) {
                            expanderObj.put(CustomReportConstants.Acc_Description, dataResultArrayElement.get("description"));
                            accountDataIndexObj.put(CustomReportConstants.Acc_Description, selectedRowsJSON.getJSONObject(selectedRowsDHList.indexOf(CustomReportConstants.Acc_Description.trim())).get("id"));
                        }
                        if (selectedRowsCustomDHList != null) {
                            for (int selectedRowsCustomDHListCnt = 0; selectedRowsCustomDHListCnt < selectedRowsCustomDHList.size(); selectedRowsCustomDHListCnt++) {
                                String customDH = selectedRowsCustomDHList.get(selectedRowsCustomDHListCnt);
                                //customDH = customDH.replaceAll("Custom_", "");
                                KwlReturnObject result = null;
                                HashMap<String, Object> map = new HashMap<String, Object>();
                                map.put(Constants.filter_names, Arrays.asList("module"));
                                map.put(Constants.filter_values, Arrays.asList(valueMap.get("moduleID")));
                                if (!StringUtil.isNullOrEmpty((String) valueMap.get("moduleID"))) {
                                    map.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, "fieldname"));
                                    map.put(Constants.filter_values, Arrays.asList((String) valueMap.get("companyID"), Integer.parseInt((String) valueMap.get("moduleID")), customDH));
                                }
                                result = accCustomerReportServiceDao.getCustomFieldsData(map);
                                int cuxtomfieldXtype = 0;
                                if (result != null) {
                                    List<FieldParams> fieldParams = result.getEntityList();
                                    for (FieldParams fieldParam : fieldParams) {
                                        cuxtomfieldXtype = fieldParam.getFieldtype();
                                    }
                                }
                                customDH = customDH.replaceAll("Custom_", "");
                                if (dataResultArrayElement.has(selectedRowsCustomDHList.get(selectedRowsCustomDHListCnt))) {
                                    switch (cuxtomfieldXtype) {
                                        case 3:
                                            long customVal = dataResultArrayElement.getLong(selectedRowsCustomDHList.get(selectedRowsCustomDHListCnt));
                                            DateFormat udf = new SimpleDateFormat((String) valueMap.get("userDateFormat"));
                                            expanderObj.put(selectedRowsCustomDHList.get(selectedRowsCustomDHListCnt), udf.format(new java.util.Date(customVal)));
                                            dataResultArrayElement.put(selectedRowsCustomDHList.get(selectedRowsCustomDHListCnt),udf.format(new java.util.Date(customVal)));
                                            break;
                                        case 4:
                                            expanderObj.put(selectedRowsCustomDHList.get(selectedRowsCustomDHListCnt), getFieldComboDataValue((String) dataResultArrayElement.get(selectedRowsCustomDHList.get(selectedRowsCustomDHListCnt))));
                                            dataResultArrayElement.put(selectedRowsCustomDHList.get(selectedRowsCustomDHListCnt),getFieldComboDataValue((String) dataResultArrayElement.get(selectedRowsCustomDHList.get(selectedRowsCustomDHListCnt))));
                                            break;
                                        case 7:
                                            expanderObj.put(selectedRowsCustomDHList.get(selectedRowsCustomDHListCnt),getFieldComboDataValue((String) dataResultArrayElement.get(selectedRowsCustomDHList.get(selectedRowsCustomDHListCnt))));
                                            dataResultArrayElement.put(selectedRowsCustomDHList.get(selectedRowsCustomDHListCnt),getFieldComboDataValue((String) dataResultArrayElement.get(selectedRowsCustomDHList.get(selectedRowsCustomDHListCnt))));
                                            break;
                                        default:
                                            expanderObj.put(selectedRowsCustomDHList.get(selectedRowsCustomDHListCnt), (String) dataResultArrayElement.get(selectedRowsCustomDHList.get(selectedRowsCustomDHListCnt)));
                                            dataResultArrayElement.put(selectedRowsCustomDHList.get(selectedRowsCustomDHListCnt),(String) dataResultArrayElement.get(selectedRowsCustomDHList.get(selectedRowsCustomDHListCnt)));
                                            break;
                                    }
                                    accountDataIndexObj.put(selectedRowsCustomDHList.get(selectedRowsCustomDHListCnt), customDHMap.containsValue(customDH) ? getKeyFromValue(customDHMap, customDH) : "");
                                } else {
                                    expanderObj.put(selectedRowsCustomDHList.get(selectedRowsCustomDHListCnt), "");
                                    accountDataIndexObj.put(selectedRowsCustomDHList.get(selectedRowsCustomDHListCnt), customDHMap.containsValue(customDH) ? getKeyFromValue(customDHMap, customDH) : "");

                                }
                            }
                        }
//                        expanderObj.put("currencysymbol", dataResultArrayElement.get("currencysymbol"));
                        jobj.append("accountDetails", expanderObj);
//                        dataIndexDNobj.put("accountDataIndexObj", accountDataIndexObj);
                    } else {

                        if (selectedRowsDHList.indexOf(CustomReportConstants.Acc_Invoice_Number.trim()) >= 0) {
                            expanderObj.put(CustomReportConstants.Acc_Invoice_Number, dataResultArrayElement.get("transectionno"));
                            invoiceDataIndexobj.put(CustomReportConstants.Acc_Invoice_Number, selectedRowsJSON.getJSONObject(selectedRowsDHList.indexOf(CustomReportConstants.Acc_Invoice_Number.trim())).get("id"));
                        }
                        if (selectedRowsDHList.indexOf(CustomReportConstants.Acc_Creation_Date.trim()) >= 0) {
                            Date tempDate = simpleDateFormat.parse((String) dataResultArrayElement.get("invcreationdate"));
                            String convertedDate = dateFormat.format(tempDate);
                            expanderObj.put(CustomReportConstants.Acc_Creation_Date, convertedDate);
                            dataResultArrayElement.put("invcreationdate", convertedDate);
                            invoiceDataIndexobj.put(CustomReportConstants.Acc_Creation_Date, selectedRowsJSON.getJSONObject(selectedRowsDHList.indexOf(CustomReportConstants.Acc_Creation_Date.trim())).get("id"));
                        }
                        if (selectedRowsDHList.indexOf(CustomReportConstants.Acc_Due_Date.trim()) >= 0) {
                            Date tempDate = simpleDateFormat.parse((String) dataResultArrayElement.get("invduedate"));
                            String convertedDate = dateFormat.format(tempDate);
                            expanderObj.put(CustomReportConstants.Acc_Due_Date, convertedDate);
                            dataResultArrayElement.put("invduedate",convertedDate);
                            invoiceDataIndexobj.put(CustomReportConstants.Acc_Due_Date, selectedRowsJSON.getJSONObject(selectedRowsDHList.indexOf(CustomReportConstants.Acc_Due_Date.trim())).get("id"));
                        }
                        if (selectedRowsDHList.indexOf(CustomReportConstants.Acc_Linking_Date.trim()) >= 0) {
                            Date tempDate = simpleDateFormat.parse((String) dataResultArrayElement.get("grlinkdate"));
                            String convertedDate = dateFormat.format(tempDate);
                            expanderObj.put(CustomReportConstants.Acc_Linking_Date, convertedDate);
                            dataResultArrayElement.put("grlinkdate",convertedDate);
                            invoiceDataIndexobj.put(CustomReportConstants.Acc_Linking_Date, selectedRowsJSON.getJSONObject(selectedRowsDHList.indexOf(CustomReportConstants.Acc_Linking_Date.trim())).get("id"));
                        }
                        if (selectedRowsDHList.indexOf(CustomReportConstants.Acc_Invoice_Amount.trim()) >= 0) {
                            expanderObj.put(CustomReportConstants.Acc_Invoice_Amount, dataResultArrayElement.get("invamount"));
                            invoiceDataIndexobj.put(CustomReportConstants.Acc_Invoice_Amount, selectedRowsJSON.getJSONObject(selectedRowsDHList.indexOf(CustomReportConstants.Acc_Invoice_Amount.trim())).get("id"));
                        }
                        if (selectedRowsDHList.indexOf(CustomReportConstants.Acc_Debit_Note_Amount_Due.trim()) >= 0) {
                            expanderObj.put(CustomReportConstants.Acc_Debit_Note_Amount_Due, dataResultArrayElement.get("invamountdue"));
                            invoiceDataIndexobj.put(CustomReportConstants.Acc_Debit_Note_Amount_Due, selectedRowsJSON.getJSONObject(selectedRowsDHList.indexOf(CustomReportConstants.Acc_Debit_Note_Amount_Due.trim())).get("id"));
                        }
//                        expanderObj.put("currencysymbol", dataResultArrayElement.get("currencysymbol"));
                        jobj.append("invoiceDetails", expanderObj);
//                                dataIndexDNobj.put("invoiceDataIndexobj",invoiceDataIndexobj);
                    }
                    if (!jobj.has("accountColumns") && !jobj.has("invoiceColumns")) {
                        jobj.put("accountColumns", accountDataIndexArr);
                        jobj.put("invoiceColumns", invoiceDataIndexArr);
                    }
                    jArray.put(jobj);
                }
//                jobj.put("dataIndexDNobj", dataIndexDNobj);
//                jobj.put("accountColumns", accountDataIndexArr);
//                jobj.put("invoiceColumns", invoiceDataIndexArr);
//                jArray.put(jobj);
//                jArray.put(accountDataIndexArr);
//                jArray.put(invoiceDataIndexArr);

            } catch (JSONException ex) {
                Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParseException ex) {
                Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return jArray;
    }

    public Object getKeyFromValue(Map hm, Object value) {
        for (Object o : hm.keySet()) {
            if (hm.get(o).equals(value)) {
                return o;
            }
        }
        return null;
    }

    @Override
    public Object getTimzeZoneClassObject(String classpath, String id) throws ServiceException {

        Object result = accCustomerReportServiceDao.getTimzeZoneClassObject(classpath, id);
        return result;
    }

    public void removeOldMappingForDataIndex(LinkedHashMap<String, String> dataIndexList, JSONObject mapjObj , String moduleid ) {
        Set<String> keys = dataIndexList.keySet();
        try {
            for (String k : keys) {
                if ((moduleid.equals(String.valueOf(Constants.Acc_Debit_Note_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_Credit_Note_ModuleId))) && k.equalsIgnoreCase(mapjObj.getString("defaultHeader"))) {
                    if (("#" + mapjObj.getString("defaultHeaderParentName") + "#").equalsIgnoreCase(dataIndexList.get(k))) {
                        dataIndexList.remove(k);
                        break;
                    }
                } else {
                    if (("#" + mapjObj.getString("defaultHeaderParentName") + "#").equalsIgnoreCase(dataIndexList.get(k))) {
                        dataIndexList.remove(k);
                        break;
                    }
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isResultSetEmpty(SqlRowSet reportDataResultSet) {
        SqlRowSetMetaData rsmd = reportDataResultSet.getMetaData();
        reportDataResultSet.last();
        int totalRecords = reportDataResultSet.getRow();
        reportDataResultSet.beforeFirst();

         if (totalRecords == 1){
            reportDataResultSet.next();
            for (int i = 1; i < rsmd.getColumnCount(); i++) {
                 if(reportDataResultSet.getObject(i) != null){
                    reportDataResultSet.beforeFirst();
                    return false;
                }
            }
         }else{
            return false;
        }
        return true;
    }

    public String getMainTableForModule(String moduleid) {
        String module = "";

        if(moduleid.equals(String.valueOf(Constants.Acc_Sales_Order_ModuleId))){
            module = CustomReportConstants.Acc_Sales_Order;
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Purchase_Order_ModuleId))){
            module = CustomReportConstants.Acc_Purchase_Order;
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Invoice_ModuleId))|| moduleid.equals(String.valueOf(Constants.Acc_FixedAssets_DisposalInvoice_ModuleId))){
            module = CustomReportConstants.Acc_Sales_Invoice;
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Sales_Return_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_FixedAssets_Sales_Return_ModuleId))){
            module = CustomReportConstants.Acc_Sales_Return;
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Purchase_Return_ModuleId))|| moduleid.equals(String.valueOf(Constants.Acc_FixedAssets_Purchase_Return_ModuleId)) ){
            module = CustomReportConstants.Acc_Purchase_Return;
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Goods_Receipt_ModuleId))){
            module = CustomReportConstants.Acc_Goods_Receipt;
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Delivery_Order_ModuleId))){
            module = CustomReportConstants.Acc_Delivery_Order;
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Debit_Note_ModuleId))){
            module = CustomReportConstants.Acc_Debit_Note;
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Credit_Note_ModuleId))){
            module = CustomReportConstants.Acc_Credit_Note;
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId))){
            module = CustomReportConstants.Acc_Vendor_Invoice;
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Customer_ModuleId))){
            module = CustomReportConstants.Acc_CUSTOMER;
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Purchase_Requisition_ModuleId))){
            module = CustomReportConstants.Acc_Purchase_Requisition;
        } else if (moduleid.equals(String.valueOf(Constants.Acc_Delivery_Order_ModuleId))) {
            module = CustomReportConstants.Acc_Delivery_Order;
        }

        return module;
    }

    public void removeJSONObject(JSONArray array, String key) throws JSONException {
        for (int i = 0, len = array.length(); i < len; i++) {
            JSONObject obj = array.getJSONObject(i);
            if (obj.optBoolean(key, false)) {
                array.remove(i);
                len = array.length();
                i--;
            }
        }
    }

    public String getModuleNameForModuleID(String moduleid) {
        String module = " ";
        if(moduleid.equals(String.valueOf(Constants.Acc_Sales_Order_ModuleId))){
            module = "Sales Order";
        } else if(moduleid.equals(String.valueOf(Constants.Acc_Purchase_Order_ModuleId))){
            module = "Purchase Order";
        } else if(moduleid.equals(String.valueOf(Constants.Acc_Invoice_ModuleId))){
            module = "Customer Invoice";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Sales_Return_ModuleId))){
            module = "Sales Return";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Purchase_Return_ModuleId))){
            module = "Purchase Return";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Goods_Receipt_ModuleId))){
            module = "Goods Receipt";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Delivery_Order_ModuleId))){
            module = "Delivery Order";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Debit_Note_ModuleId))){
            module = "Debit Note";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Credit_Note_ModuleId))){
            module = "Credit Note";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId))){
            module = "Vendor Invoice";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Customer_Quotation_ModuleId))){
            module = "Customer Quotation";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId))){
            module = "Make Payment";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))){
            module = "Receive Payment";
        }else if(moduleid.equals(String.valueOf(Constants.CUSTOMER_MODULE_UUID))){ 
            module = "Customer";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Product_Master_ModuleId))){ 
            module = "Product Master";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Purchase_Requisition_ModuleId))){ 
            module = "Purchase Requisition";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId))){ 
            module = "Asset Purchase Invoice";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_FixedAssets_DisposalInvoice_ModuleId))){ 
            module = "Asset Sales Invoice";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_FixedAssets_Sales_Return_ModuleId))){ 
            module = "Asset Sales Return";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_FixedAssets_Purchase_Return_ModuleId))){ 
            module = "Asset Purchase Return";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Product_Category_ModuleId))){ 
            module = "Product Category";
        }

        return module;
    }
    
    //ERP-38655
    public String getModuleNameForColumnType(String moduleid) {
        String module = " ";
        if(moduleid.equals(String.valueOf(Constants.Acc_Sales_Order_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_Invoice_ModuleId))|| moduleid.equals(String.valueOf(Constants.Acc_FixedAssets_Sales_Return_ModuleId))|| moduleid.equals(String.valueOf(Constants.Acc_Delivery_Order_ModuleId))|| moduleid.equals(String.valueOf(Constants.Acc_Customer_Quotation_ModuleId))|| moduleid.equals(String.valueOf(Constants.Acc_Sales_Return_ModuleId))){
            module = CustomReportConstants.Acc_Sales_Order_ColumnType;
        } else if(moduleid.equals(String.valueOf(Constants.Acc_Purchase_Order_ModuleId))||moduleid.equals(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId))||moduleid.equals(String.valueOf(Constants.Acc_Goods_Receipt_ModuleId))||moduleid.equals(String.valueOf(Constants.Acc_Purchase_Return_ModuleId))||moduleid.equals(String.valueOf(Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId))||moduleid.equals(String.valueOf(Constants.Acc_FixedAssets_Purchase_Return_ModuleId))){
            module = CustomReportConstants.Acc_Purchase_Order_ColumnType;
        }
        return module;
    }

    public String getCrossModuleNameForModuleID(String moduleid, String fieldType) {
        String module = "";

        if(moduleid.equals(String.valueOf(Constants.Acc_Sales_Order_ModuleId))){
            module = fieldType + " - Sales Order";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Purchase_Order_ModuleId))){
            module = fieldType + " - Purchase Order";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Invoice_ModuleId))){
            module = fieldType + " - Customer Invoice";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Sales_Return_ModuleId))){
            module = fieldType + " - Sales Return";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Purchase_Return_ModuleId))){
            module = fieldType + " - Purchase Return";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Goods_Receipt_ModuleId))){
            module = fieldType + " - Goods Receipt";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Delivery_Order_ModuleId))){
            module = fieldType + " - Delivery Order";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Debit_Note_ModuleId))){
            module = fieldType + " - Debit Note";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Credit_Note_ModuleId))){
            module = fieldType + " - Credit Note";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId))){
            module = fieldType + " - Vendor Invoice";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Customer_Quotation_ModuleId))){
            module = fieldType + " - Customer Quotation";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId))){
            module = fieldType + " - Make Payment";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))){
            module = fieldType + " - Receive Payment";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Product_Master_ModuleId))){
            module = fieldType + " - Product Master";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Purchase_Requisition_ModuleId))){ 
            module = fieldType + " - Purchase Requisition";
        }else if(moduleid.equals(String.valueOf(Constants.Acc_Product_Category_ModuleId))){ 
            module = fieldType + " - Product Category";
        }
        return module;
    }

    public String getLinkedTableNameForModuleID(String moduleid) {
        String module = "";
        if (moduleid.equals(String.valueOf(Constants.Acc_Sales_Order_ModuleId))) {
            module = "solinking";
        } else if (moduleid.equals(String.valueOf(Constants.Acc_Purchase_Order_ModuleId))) {
            module = "polinking";
        } else if (moduleid.equals(String.valueOf(Constants.Acc_Invoice_ModuleId))) {
            module = "invoicelinking";
        } else if (moduleid.equals(String.valueOf(Constants.Acc_Sales_Return_ModuleId))) {
            module = "salesreturnlinking";
        } else if (moduleid.equals(String.valueOf(Constants.Acc_Purchase_Return_ModuleId))) {
            module = "purchasereturnlinking";
        } else if (moduleid.equals(String.valueOf(Constants.Acc_Goods_Receipt_ModuleId))) {
            module = "goodsreceiptorderlinking";
        } else if (moduleid.equals(String.valueOf(Constants.Acc_Delivery_Order_ModuleId))) {
            module = "dolinking";
        } else if (moduleid.equals(String.valueOf(Constants.Acc_Debit_Note_ModuleId))) {
            module = "debitnotelinking";
        } else if (moduleid.equals(String.valueOf(Constants.Acc_Credit_Note_ModuleId))) {
            module = "creditnotelinking";
        } else if (moduleid.equals(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId))) {
            module = "goodsreceiptlinking";
        } else if (moduleid.equals(String.valueOf(Constants.Acc_Customer_Quotation_ModuleId))) {
            module = "cqlinking";
        } else if (moduleid.equals(String.valueOf(Constants.Acc_Make_Payment_ModuleId))) {
            module = "";
        } else if (moduleid.equals(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {
            module = "";
        }
        return module;
    }

    public int buildCustomFieldExtraJoins(String fromTable, String joinTable, HashMap<String, String> stbCrossModuleSelectMap, HashMap<String, String> stbCrossModuleJoinMap, LinkedHashMap<String, String> dataIndexList, JSONObject jObj, int index, ArrayList<String> crossModuleRefTableList, StringBuilder crossFieldSelectKey) throws ServiceException, JSONException {

        StringBuilder customFieldExtraJoin = null;
        customFieldExtraJoin = new StringBuilder();
        if (!stbCrossModuleSelectMap.containsKey(fromTable)) {
            customFieldExtraJoin.append(
                    joinTable + ".linkeddocid " + "," + joinTable + ".docid" + " from " + fromTable + " inner join " + joinTable + " on " + fromTable + "." + accCustomerReportServiceDao.getmoduledataRefPKColName(fromTable) + " = " + joinTable + ".linkeddocid"
                    + " ) as ");
            stbCrossModuleSelectMap.put(fromTable, fromTable + "." + accCustomerReportServiceDao.getmoduledataRefPKColName(fromTable));
            stbCrossModuleJoinMap.put(fromTable, customFieldExtraJoin.toString());
        } else {
            customFieldExtraJoin.append(
                    joinTable + ".linkeddocid " + "," + joinTable + ".docid" + " from " + fromTable + " inner join " + joinTable + " on " + fromTable + "." + accCustomerReportServiceDao.getmoduledataRefPKColName(fromTable) + " = " + joinTable + ".linkeddocid"
                    + " ) as ");
            stbCrossModuleSelectMap.put(fromTable + index, stbCrossModuleSelectMap.get(fromTable));
            stbCrossModuleJoinMap.put(fromTable + index, customFieldExtraJoin.toString());
            String selValue = dataIndexList.get(jObj.optString("dbtablename", "") + "." + jObj.optString("dbcolumnname", ""));
            if (!StringUtil.isNullOrEmpty(selValue)) {
                String newKey = "COALESCE(" + jObj.optString("dbtablename", "") + "." + jObj.optString("dbcolumnname", "") + "," + fromTable + index + "." + jObj.optString("dbcolumnname", "") + ")";
                dataIndexList.put(newKey, selValue);
                dataIndexList.remove(jObj.optString("dbtablename", "") + "." + jObj.optString("dbcolumnname", ""));
            }
        }
        if (!(jObj.optString("reftablename", "").equalsIgnoreCase(jObj.optString("dbtablename", "")))) {
            String refTable = jObj.optString("reftablename", "");
            if ((!crossModuleRefTableList.contains(refTable) && !refTable.equals(jObj.optString("mainTable", "")))) {
                crossModuleRefTableList.add(refTable);
                if (!StringUtil.isNullOrEmpty(jObj.optString("reftablefk", ""))) {
                    String joinTable1 = !StringUtil.isNullOrEmpty(jObj.optString("dbtablename", "")) ? jObj.optString("dbtablename", "") : jObj.optString("mainTable", "");
                    stbCrossModuleJoinMap.put(refTable, " left join " + refTable + " on " + refTable + "." + jObj.getString("reftablefk") + " = " + joinTable1 + "." + jObj.getString("dbcolumnname") + " ");
                    stbCrossModuleSelectMap.put(refTable, "NO_SELECT");
                    crossFieldSelectKey.append(refTable + "." + jObj.getString("reftabledatacolumn") + ",");
                    //String newKey = "COALESCE(" + refTable + "." + jObj.optString("reftabledatacolumn", "") + "," + fromTable + index + "." + jObj.optString("dbcolumnname", "") + ")";                          
                }
            } else {
                if (stbCrossModuleJoinMap.containsKey(refTable)) {
                    //joinMap.remove(mapjObj.getString("reftablename"));
                    String duprefTable1 = refTable + index;
                    refTable = duprefTable1;
                    stbCrossModuleJoinMap.put(duprefTable1, " left join " + jObj.getString("reftablename") + " as " + duprefTable1 + " on " + duprefTable1 + "." + jObj.getString("reftablefk") + " = " + jObj.getString("dbtablename") + index + "." + jObj.getString("dbcolumnname") + " ");
                    stbCrossModuleSelectMap.put(refTable, "NO_SELECT");
                    crossFieldSelectKey.append(duprefTable1 + "." + jObj.getString("reftabledatacolumn") + ",");
                }
            }
            if (dataIndexList.containsValue("#" + jObj.getString("defaultHeader") + "#")) {
                Set<String> keys = dataIndexList.keySet();
                for (String k : keys) {
                    if (dataIndexList.get(k).equalsIgnoreCase("#" + jObj.getString("defaultHeader") + "#")) {
                        if (crossFieldSelectKey.lastIndexOf(",") > 0) {
                            dataIndexList.put("COALESCE(" + crossFieldSelectKey.toString().substring(0, crossFieldSelectKey.lastIndexOf(",")) + ")", dataIndexList.get(k));
                            dataIndexList.remove(k);
                            break;
                        }
                    }
                }

            }

        } else {
            crossModuleRefTableList.add(jObj.optString("reftablename", ""));
        }
        return index;
    }

    //to build dataIndexObject 
    public JSONObject builddataIndexObject(JSONArray selectedRowsJSONData, String moduleId) throws ServiceException, JSONException {

        JSONObject dataIndexJSONObject = new JSONObject();
        JSONObject dataIndexObject = new JSONObject();
        JSONObject jobjTemp;
        String dataIndexTableName = "";
        HashMap<String, String> columnmap = new HashMap<String, String>();

        for (int cnt = 0; cnt < selectedRowsJSONData.length(); cnt++) {
            JSONObject jObj = selectedRowsJSONData.getJSONObject(cnt);
            String defaultHeaderOrig = jObj.getString("defaultHeader");
            jobjTemp = new JSONObject();
            dataIndexJSONObject = new JSONObject();
            if (!jObj.optBoolean("isMeasureItem", false)) {
                String defaultHeader = jObj.getString("defaultHeader");
                String defautlHeaderId = jObj.getString("id");
                if (!StringUtil.isNullOrEmpty(jObj.optString("dbcolumnname", ""))) {//&& !jObj.optBoolean("customfield", false)) {
                    String refTable = jObj.optString("reftablename", "");
                    if (StringUtil.isNullOrEmpty(refTable)) {
                        refTable = jObj.getString("dbtablename");
                    }
                    String colname = jObj.optString("reftabledatacolumn", "").length() > 0 ? jObj.optString("reftabledatacolumn", "") : jObj.getString("dbcolumnname");
                    if (columnmap.containsValue(defaultHeader)) {      // Check for duplicate value for default header. If duplicate found then update that default header.
                        //defaultHeader = jObj.optString("columntype","") + defaultHeader + cnt;
                        defaultHeader = defaultHeader + cnt;
                    }
                    columnmap.put(defaultHeader, defaultHeader);       // Put value of default header in cloumn map.
                    jObj.put("defaultHeader", defaultHeader);
                    jobjTemp.put("defaultHeaderOrig", defaultHeaderOrig);
                    jobjTemp.put("defaultHeader", defaultHeader);
                    jobjTemp.put("displayName", defaultHeader);
                    jobjTemp.put("dataIndex", defautlHeaderId);
                    jobjTemp.put("dataIndexTableName", refTable);
                    jobjTemp.put("allowcrossmodule", jObj.optBoolean("allowcrossmodule", false));

                    dataIndexJSONObject.put("dataIndex", defautlHeaderId);
                    if (jObj.optString("defaultHeader", "").equals(CustomReportConstants.TERM_AMOUNT) || jObj.optString("defaultHeader", "").equals(CustomReportConstants.AMOUNT) || (jObj.optString("defaultHeader", "").contains("(") && jObj.optString("defaultHeader", "").contains(")"))) {
                        dataIndexJSONObject.put(CustomReportConstants.filtercolumnname, colname);
                    } else if (jObj.optString("defaultHeader", "").equals(CustomReportConstants.CLOSED_MANUALLY)) { // Added for Closed Manually Field to constuct the SQL query correctly
                        //dataIndexJSONObject.put(dbcolumnname, "#" + jObj.getString("defaultHeader") + "#");
                        dataIndexJSONObject.put(CustomReportConstants.filtercolumnname, jObj.optString("dbcolumnname", ""));
                    } else if (jObj.optString("defaultHeader", "").equals(CustomReportConstants.TYPE)) {
                        dataIndexJSONObject.put(CustomReportConstants.filtercolumnname, jObj.optString("reftabledatacolumn", ""));
                    } else if (colname.contains("(") && colname.contains(")") && colname.contains(",")) {
                        dataIndexJSONObject.put(CustomReportConstants.filtercolumnname, colname);
                    } else {
                        dataIndexJSONObject.put(CustomReportConstants.filtercolumnname, refTable.concat(".").concat(colname));
                    }

                }
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jobjTemp.put("xtype", jObj.getString("xtype"));
                jobjTemp.put("isLineItem", jObj.optBoolean("isLineItem", false));
                // TO DO : Add Code to get it from request params through Property Grid
                if (Integer.parseInt(jObj.getString("xtype")) == 2) {
                    jobjTemp.put("precision", "2");
                    if (jObj.has("properties") && jObj.getJSONObject("properties").has("source") && jObj.getJSONObject("properties").getJSONObject("source").has("precision")) {
                        jobjTemp.put("precision", jObj.getJSONObject("properties").getJSONObject("source").getString("precision"));
                    }
                }
                if (!StringUtil.isNullOrEmpty(jObj.optString("customfield", ""))) {
                    String isCustom = jObj.optString("customfield", "");
                    if ("true".equalsIgnoreCase(isCustom)) {
                        jobjTemp.put("custom", "true");
                    } else {
                        jobjTemp.put("custom", "false");
                    }
                    dataIndexJSONObject.put("iscustom", isCustom.equals("true"));
                }
                dataIndexJSONObject.put("moduleId", moduleId);
                dataIndexJSONObject.put("xtype", jObj.getString("xtype"));
                dataIndexObject.put(defautlHeaderId, dataIndexJSONObject);
                jobjTemp.put("isMeasureItem", jObj.optBoolean("isMeasureItem",false));
                if (defaultHeader.equals("Discount")) {
                    dataIndexTableName = jObj.optString("reftablename", "");
                }
                if (defaultHeader.trim().equals(CustomReportConstants.Quantity) || defaultHeader.equals(CustomReportConstants.BaseQuantity)
                        || defaultHeader.equals(CustomReportConstants.BALANCE_QUANTITY) || defaultHeader.equals(CustomReportConstants.ACTUAL_QUANTITY)
                        || defaultHeader.equals(CustomReportConstants.RECEIVED_QUANTITY) || defaultHeader.equals(CustomReportConstants.RETURN_QUANTITY)
                        || defaultHeader.equals(CustomReportConstants.DELIVERED_QUANTITY)) {
                    dataIndexTableName = jObj.optString("reftablename", "");
                }
            } else {
                String defaultHeader = jObj.getString("defaultHeader");
                String defautlHeaderId = jObj.getString("id");

                jobjTemp.put("displayName", defaultHeader);      // Put Original default Header in display name because we are using this in UI for headers.
                if (columnmap.containsValue(defaultHeader)) {      // Check for duplicate value for default header. If duplicate found then update that default header.
                    //defaultHeader = jObj.optString("columntype","") + defaultHeader + cnt;
                    defaultHeader = defaultHeader + cnt;
                }
                columnmap.put(defaultHeader, defaultHeader);
                jObj.put("defaultHeader", defaultHeader);         // Put updated value in main json object to build select sql query. Note that there is no effect of this default header on UI , because we use displayName to show header in UI.
                jobjTemp.put("defaultHeader", defaultHeader);
                jobjTemp.put("defaultHeaderOrig", defaultHeaderOrig);
                jobjTemp.put("dataIndex", defautlHeaderId);
                jobjTemp.put("id", defautlHeaderId);
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jobjTemp.put("xtype", jObj.getString("xtype"));
                jobjTemp.put("isLineItem", jObj.optBoolean("isLineItem", false));
                dataIndexTableName = jObj.optString("reftablename", "");
                jobjTemp.put("dataIndexTableName", dataIndexTableName);
                // TO DO : Add Code to get it from request params through Property Grid
                if (Integer.parseInt(jObj.getString("xtype")) == 2) {
                    jobjTemp.put("precision", "2");
                    if (jObj.has("properties") && jObj.getJSONObject("properties").has("source") && jObj.getJSONObject("properties").getJSONObject("source").has("precision")) {
                        jobjTemp.put("precision", jObj.getJSONObject("properties").getJSONObject("source").getString("precision"));
                    }
                }
                if (!StringUtil.isNullOrEmpty(jObj.optString("customfield", ""))) {
                    String isCustom = jObj.optString("customfield", "");
                    if ("true".equalsIgnoreCase(isCustom)) {
                        jobjTemp.put("custom", "true");
                    } else {
                        jobjTemp.put("custom", "false");
                    }
                    dataIndexJSONObject.put("iscustom", isCustom.equals("true"));
                }
                dataIndexJSONObject.put("moduleId", moduleId);
                dataIndexJSONObject.put("xtype", jObj.getString("xtype"));
                jobjTemp.put("isMeasureItem", jObj.optBoolean("isMeasureItem",false));
                dataIndexObject.put(defautlHeaderId, dataIndexJSONObject);
                if (defaultHeader.equals("Discount")) {
                    dataIndexTableName = jObj.optString("reftablename", "");
                }
            }
        }
        return dataIndexObject;
    }

    //to build dataIndexObject 
    public JSONArray getFilterType(JSONArray filterArray,String dataIndex) throws ServiceException, JSONException {
        JSONArray individualfilterArray = new JSONArray();
        try {
            for (int i = 0; i < filterArray.length(); i++) {
                JSONObject jobj = filterArray.getJSONObject(i);
                if (jobj.optString("property").equalsIgnoreCase(dataIndex)) {
                    individualfilterArray.put(jobj);
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }


        return individualfilterArray;
    }

    public String getModuleMainTable(String moduleName) {
        String modulemaintable = "";
        switch (moduleName) {
            case "Sales Order":
                modulemaintable = "salesorder";
                break;
            case "Purchase Order":
                modulemaintable = "purchaseorder";
                break;
            case "Goods Receipt":
                modulemaintable = "grorder";
                break;
            case "Delivery Order":
                modulemaintable = "deliveryorder";
                break;
            case "Customer Invoice":
                modulemaintable = "invoice";
                break;
            case "Vendor Invoice":
                modulemaintable = "goodsreceipt";
                break;
            case "Sales Return":
                modulemaintable = "salesreturn";
                break;
            case "Purchase Return":
                modulemaintable = "purchasereturn";
                break;
            case "Customer Quotation":
                modulemaintable ="quotation";
                break;
            case "Product Master":
                modulemaintable ="product";
                break;                
            case "Product Category":
                modulemaintable ="productcategory";
                break; 
        }
        return modulemaintable;

    }

    private double calculatePurchaseOrderMeasures(String purchaseOrderId, KWLCurrency currency, String currencyid, Map valueMap, JSONObject selectedJSONObj) throws ServiceException, JSONException {

        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), purchaseOrderId);
        KWLCurrency kwlcurrency = getCurrencyValue(currencyid);
        PurchaseOrder purchaseOrder = (PurchaseOrder) objItr.getEntityList().get(0);
        double calculatedMeasureValue = 0;        
        String companyid = (String) valueMap.get("companyID");
        JSONArray purchaseOrderData = new JSONArray();
        HashMap<String, Object> paramJObj = new HashMap<String, Object>();
        paramJObj.put(Constants.companyKey, companyid);
        List<Object[]> ojList = new ArrayList<>();
        Object[] oj = new Object[objItr.getEntityList().size() + 1];
        oj[0] = purchaseOrder.getID();
        oj[1] = true;
        ojList.add(oj);
        Iterator it = valueMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            paramJObj.put((String) pair.getKey(), pair.getValue());
        }
        purchaseOrderData = accPurchaseOrderServiceDAOobj.getPurchaseOrdersJsonMerged(paramJObj, ojList, purchaseOrderData);
        if (purchaseOrderData.length() > 0) {
            if (CustomReportConstants.Acc_Purchase_Order_Measure_Total_Purchase_Price.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = Double.parseDouble(purchaseOrderData.getJSONObject(0).getString("productTotalAmount")); 
            }
            if (CustomReportConstants.Acc_Purchase_Order_Measure_Total_Purchase_Price_With_Tax.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = Double.parseDouble(purchaseOrderData.getJSONObject(0).getString("orderamount")); 
            }
            if (CustomReportConstants.Acc_Sales_Order_Measure_Total_Tax.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = Double.parseDouble(purchaseOrderData.getJSONObject(0).getString("taxamount"));
            }
            if (CustomReportConstants.Acc_Sales_Order_Measure_Total_Amount.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = Double.parseDouble(purchaseOrderData.getJSONObject(0).getString("amount"));
            }
            if (CustomReportConstants.Acc_Sales_Order_Measure_Amount_Before_GST.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = Double.parseDouble(purchaseOrderData.getJSONObject(0).getString("amountbeforegst"));;
            }
            if (CustomReportConstants.Amount_Before_Tax.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = Double.parseDouble(purchaseOrderData.getJSONObject(0).getString("amountBeforeTax"));;
            }
            if (CustomReportConstants.Sub_Total.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = Double.parseDouble(purchaseOrderData.getJSONObject(0).getString("subtotal"));;
            }

        }
        return calculatedMeasureValue;

    }

    
    private double calculateSalesOrderMeasures(String salesOrderId, Map valueMap, JSONObject selectedJSONObj) throws ServiceException, JSONException, SessionExpiredException, ParseException {

        double calculatedMeasureValue = 0;
        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), salesOrderId);
        String companyid = (String) valueMap.get("companyID");
        SalesOrder salesOrder = (SalesOrder) objItr.getEntityList().get(0);
        JSONArray salesOrderData = new JSONArray();
        JSONObject salesOrderRowsData = new JSONObject();
        JSONObject paramJObj = new JSONObject();
        paramJObj.put(Constants.companyKey, companyid);
        List<Object[]> ojList = new ArrayList<>();
        Object[] oj = new Object[objItr.getEntityList().size() + 1];
        oj[0] = salesOrder.getID();
        oj[1] = true;
        ojList.add(oj);
        Iterator it = valueMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            paramJObj.put((String) pair.getKey(), pair.getValue());
        }
        paramJObj.put("isLeaseFixedAsset", "false");
        salesOrderData = accSalesOrderServiceDAOobj.getSalesOrdersJsonMerged(paramJObj, ojList, salesOrderData);
        if (salesOrder != null) {
            double sellingPrice = 0, productCost = 0, taxAmount = 0,rowTaxAmount = 0, totalSellingPrice = 0, totalRowTaxAmount = 0, totalProductPrice = 0, margin = 0, totalSellingPriceWithtax = 0;
            paramJObj.put(Constants.billid, salesOrder.getID());
            salesOrderRowsData = accSalesOrderServiceDAOobj.getSalesOrderRows(paramJObj);
            String taxAmountStr = salesOrderData.getJSONObject(0).optString("taxamount");
            if (StringUtil.isNullOrEmpty(taxAmountStr)) {
                taxAmount = 0.0;
            } else {
                taxAmount = Double.parseDouble(taxAmountStr);
            }            
            for (int i = 0; i < salesOrderRowsData.getJSONArray("data").length(); i++) {
                String tempSP = salesOrderRowsData.getJSONArray("data").getJSONObject(i).optString("amount", "");
                if (StringUtil.isNullOrEmpty(tempSP)) {
                    sellingPrice = 0.0;
                } else {
                    sellingPrice = Double.parseDouble(tempSP);
                }
                String tempPC = salesOrderRowsData.getJSONArray("data").getJSONObject(i).optString("totalcost", "");
                if (StringUtil.isNullOrEmpty(tempPC)) {
                    productCost = 0.0;
                } else {
                    productCost = Double.parseDouble(tempPC);
                }
                String tempRowTax = salesOrderRowsData.getJSONArray("data").getJSONObject(i).optString("rowTaxAmount", "");
                if (StringUtil.isNullOrEmpty(tempRowTax)) {
                    rowTaxAmount = 0.0;
                } else {
                    rowTaxAmount = Double.parseDouble(tempRowTax);
                }
                totalSellingPrice += sellingPrice;
                totalProductPrice += productCost;
                totalRowTaxAmount += rowTaxAmount;
            }
            margin = totalSellingPrice - totalProductPrice;
            totalSellingPriceWithtax = totalSellingPrice + taxAmount;

              if (CustomReportConstants.Acc_Sales_Order_Measure_AmtWithoutTax.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = Double.parseDouble(salesOrderData.getJSONObject(0).getString("amountbeforegst"));
            }
            if (CustomReportConstants.Acc_Sales_Order_Measure_AmtWithTax.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
//                calculatedMeasureValue = Double.parseDouble(salesOrderData.getJSONObject(0).getString("orderamount"));
                calculatedMeasureValue = totalSellingPriceWithtax;
            }
            if (CustomReportConstants.Acc_Sales_Order_Measure_Margin.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = margin;
            }
            if (CustomReportConstants.Acc_Sales_Order_Measure_Total_Selling_Price.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = totalSellingPrice;
            }
            if (CustomReportConstants.Acc_Sales_Order_Measure_Total_Purchase_Cost.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = totalProductPrice;
            }
            if (CustomReportConstants.Acc_Sales_Order_Measure_Total_Selling_Price_With_Tax.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = totalSellingPriceWithtax;
            }
            if (CustomReportConstants.Acc_Sales_Order_Measure_Total_Tax.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = Double.parseDouble(salesOrderData.getJSONObject(0).getString("taxamount"));
            }
            if (CustomReportConstants.Acc_Sales_Order_Measure_Total_Amount.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = Double.parseDouble(salesOrderData.getJSONObject(0).getString("orderamountwithTax")); //ERP-31211
//                calculatedMeasureValue = totalSellingPriceWithtax;
            }
            if (CustomReportConstants.Acc_Sales_Order_Measure_Amount_Before_GST.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = Double.parseDouble(salesOrderData.getJSONObject(0).getString("amountbeforegst"));
            }
            if (CustomReportConstants.Sub_Total.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = Double.parseDouble(salesOrderData.getJSONObject(0).getString("subtotal"));
            }
            if (CustomReportConstants.Amount_Before_Tax.equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = Double.parseDouble(salesOrderData.getJSONObject(0).getString("amountBeforeTax"));
            }

        }

        return calculatedMeasureValue;
    }

    private double calculateGoodsReceiptMeasures(String goodReceiptId, Map valueMap, JSONObject selectedJSONObj) throws ServiceException, JSONException {
        
        JSONObject requestJSON = new JSONObject();
        requestJSON.put("companyid", valueMap.get("companyID"));
        requestJSON.put("userdf", (String) valueMap.get("userDateFormat"));
        Object[] obj = new Object[1];
        obj[0]= goodReceiptId;
        List list = new ArrayList();
        list.add(obj);
        JSONArray resultJSONArray = accGoodsReceiptServiceHandler.getGoodsReceiptOrdersJsonMerged(requestJSON, list);
        double calculatedMeasureValue = 0;

            if (CustomReportConstants.Acc_Delivery_Order_Measure_Sub_Total.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = resultJSONArray.getJSONObject(0).optDouble("subtotal");
            }
            if (CustomReportConstants.Acc_GRO_Measure_Discount.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = resultJSONArray.getJSONObject(0).optDouble("discount");
            }
            if (CustomReportConstants.Amount_Before_Tax.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = resultJSONArray.getJSONObject(0).optDouble("amountBeforeTax");
            }
            if (CustomReportConstants.Acc_Sales_Order_Measure_Total_Tax.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = resultJSONArray.getJSONObject(0).optDouble("taxamount");
            }
            if (CustomReportConstants.Acc_Sales_Order_Measure_Total_Amount.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = resultJSONArray.getJSONObject(0).optDouble("amount");
            }
            if (CustomReportConstants.Total_Amount_In_Base.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = resultJSONArray.getJSONObject(0).optDouble("amountinbase");
            }
        return calculatedMeasureValue;
    }

    private double calculateCustomerQuotationMeasures(HashMap<String, Object> requestParams, List<String> quotationList, JSONArray DataJArr, JSONObject selectedJSONObj) throws ServiceException, JSONException {
        double calculatedMeasureValue = 0;
        JSONArray jsonA = new JSONArray();
        jsonA = accSalesOrderServiceDAOobj.getQuotationsJson(requestParams, quotationList, DataJArr);
        if (CustomReportConstants.Acc_Sales_Order_Measure_Total_Tax.equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
            calculatedMeasureValue = Double.parseDouble(jsonA.getJSONObject(0).getString("taxamount"));

        } else if (CustomReportConstants.Acc_Sales_Order_Measure_Total_Amount.equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
            calculatedMeasureValue = Double.parseDouble(jsonA.getJSONObject(0).getString("orderamountwithTax"));
        } else if (CustomReportConstants.Acc_GRO_Measure_Discount.equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
            calculatedMeasureValue = Double.parseDouble(jsonA.getJSONObject(0).getString("discount"));
        } else if(CustomReportConstants.Acc_Invoice_Amount_Without_VAT.equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())){
            calculatedMeasureValue = Double.parseDouble(jsonA.getJSONObject(0).getString("amountbeforegst"));
        } else if(CustomReportConstants.Sub_Total.equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())){
            calculatedMeasureValue = Double.parseDouble(jsonA.getJSONObject(0).getString("subtotal"));
        } else if(CustomReportConstants.Amount_Before_Tax.equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())){
            calculatedMeasureValue = Double.parseDouble(jsonA.getJSONObject(0).getString("amountBeforeTax"));
        }

        return calculatedMeasureValue;
    }

    
    public static StringBuilder buildCrossModuleSelectQuery(LinkedHashMap<String, String> dataIndexList, LinkedHashMap<String, ArrayList<String>> dataIndexListDup,JSONArray jsonArray) {
        StringBuilder selectQuery = new StringBuilder();
        Set<String> keys = dataIndexList.keySet();
        Set<String> dupKeys = dataIndexListDup.keySet();
        boolean isFirst = true;


        for (int cnt = 0; cnt < jsonArray.length(); cnt++) {
            try {
                JSONObject anObj = jsonArray.getJSONObject(cnt);
                for (String fieldname : keys) {
                    {
                        if (("#" + anObj.getString("defaultHeader") + "#").equalsIgnoreCase(dataIndexList.get(fieldname))) {
                            if (isFirst) {
                                selectQuery.append(" ").append(fieldname).append(" as `").append(dataIndexList.get(fieldname)).append("` ");
                                isFirst = false;
                            } else {
                                selectQuery.append(", ").append(fieldname).append(" as `").append(dataIndexList.get(fieldname)).append("` ");
                                }
                            }

                    }
                }

                for (String fieldname : dupKeys) {
                    ArrayList<String> listEntry = dataIndexListDup.get(fieldname);
                    for (int dupCnt = 0; dupCnt < listEntry.size(); dupCnt++) {
                        if (("#" + anObj.getString("defaultHeader") + "#").equalsIgnoreCase(listEntry.get(dupCnt))) {
                            if (isFirst) {
                                selectQuery.append(" ").append(fieldname).append(" as `").append(listEntry.get(dupCnt)).append("` ");
                                isFirst = false;
                            } else {
                                selectQuery.append(", ").append(fieldname).append(" as `").append(listEntry.get(dupCnt)).append("` ");
                            }
                        }
                    }
                }

            } catch (JSONException exp) {
                Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, exp);
            }
        }
        // Added for flatdiscount to have it in SQL query if Discount - Line Item is selected
        for (String fieldname : keys) {
            {
                if (("#flatdiscount#").equalsIgnoreCase(dataIndexList.get(fieldname))) {
                    selectQuery.append(", ").append(fieldname).append(" as `").append(dataIndexList.get(fieldname)).append("` ");
                }

            }
        }


        return selectQuery;
    }

    public Map buildcolumnDetailsCustomReportPreview(JSONObject jObj, HashMap<String, String> columnmap, int cnt, JSONObject dataIndexObject,
            JSONArray jarrColumns, String moduleId, String dataIndexTableName, boolean isDiscountPresent, boolean isLineItem, boolean isAnyQunatityPresent, boolean isQtyLineItem,boolean isDisplayUOMPresent) {

        JSONObject dataIndexJSONObject = new JSONObject();
        JSONObject jobjTemp = new JSONObject();
        Map<String, Object> returnMap = new HashMap<String, Object>();        

        try {
            String defaultHeaderOrig = jObj.getString("defaultHeader");
            jobjTemp = new JSONObject();
            dataIndexJSONObject = new JSONObject();
            if (!jObj.getBoolean("isMeasureItem")) {
                String defaultHeader = jObj.getString("defaultHeader");
                String defautlHeaderId = jObj.getString("id");
                if (!StringUtil.isNullOrEmpty(jObj.optString("dbcolumnname", ""))) {//&& !jObj.optBoolean("customfield", false)) {
                    String refTable = jObj.optString("reftablename", "");
                    if (StringUtil.isNullOrEmpty(refTable)) {
                        refTable = jObj.getString("dbtablename");
                    }
                    String colname = jObj.optString("reftabledatacolumn", "").length() > 0 ? jObj.optString("reftabledatacolumn", "") : jObj.getString("dbcolumnname");
                    if (columnmap.containsValue(defaultHeader)&& !jObj.optBoolean(Constants.isforformulabuilder,false)) {      // Check for duplicate value for default header. If duplicate found then update that default header.
                        //defaultHeader = jObj.optString("columntype","") + defaultHeader + cnt;
                        defaultHeader = defaultHeader + cnt;
                    }
                    columnmap.put(defaultHeader, defaultHeader);       // Put value of default header in cloumn map.
                    jObj.put("defaultHeader", defaultHeader);
                    jobjTemp.put("defaultHeaderOrig", defaultHeaderOrig);
                    jobjTemp.put("defaultHeader", defaultHeader);
                    jobjTemp.put("id", jObj.getString("id"));
                    jobjTemp.put("displayName", defaultHeader);
                    jobjTemp.put("dataIndex", defautlHeaderId);
                    jobjTemp.put("dataIndexTableName", refTable);
                    jobjTemp.put("allowcrossmodule", jObj.optBoolean("allowcrossmodule", false));

                    dataIndexJSONObject.put("dataIndex", defautlHeaderId);
                    if (jObj.optString("defaultHeader", "").equals(CustomReportConstants.TERM_AMOUNT) || jObj.optString("defaultHeader", "").equals(CustomReportConstants.AMOUNT) || (jObj.optString("defaultHeader", "").contains("(") && jObj.optString("defaultHeader", "").contains(")"))) {
                        dataIndexJSONObject.put(CustomReportConstants.filtercolumnname, colname);
                    } else if (jObj.optString("defaultHeader", "").equals(CustomReportConstants.CLOSED_MANUALLY)) { // Added for Closed Manually Field to constuct the SQL query correctly
                        //dataIndexJSONObject.put(dbcolumnname, "#" + jObj.getString("defaultHeader") + "#");
                        dataIndexJSONObject.put(CustomReportConstants.filtercolumnname, jObj.optString("dbcolumnname", ""));
                    } else if (jObj.optString("defaultHeader", "").equals(CustomReportConstants.TYPE)) {
                        dataIndexJSONObject.put(CustomReportConstants.filtercolumnname, jObj.optString("reftabledatacolumn", ""));
                    } else if (colname.contains("(") && colname.contains(")") && colname.contains(",")) {
                        dataIndexJSONObject.put(CustomReportConstants.filtercolumnname, colname);
                    } else {
                        dataIndexJSONObject.put(CustomReportConstants.filtercolumnname, refTable.concat(".").concat(colname));
                    }

                }
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jobjTemp.put("xtype", jObj.getString("xtype"));
                jobjTemp.put("isLineItem", jObj.optBoolean("isLineItem", false));
                // TO DO : Add Code to get it from request params through Property Grid
                if (Integer.parseInt(jObj.getString("xtype")) == 2) {
                    jobjTemp.put("precision", "2");
                    if (jObj.has("properties") && jObj.getJSONObject("properties").has("source") && jObj.getJSONObject("properties").getJSONObject("source").has("precision")) {
                        jobjTemp.put("precision", jObj.getJSONObject("properties").getJSONObject("source").getString("precision"));
                    }
                }
                if (!StringUtil.isNullOrEmpty(jObj.optString("customfield", ""))) {
                    String isCustom = jObj.optString("customfield", "");
                    if ("true".equalsIgnoreCase(isCustom)) {
                        jobjTemp.put("custom", "true");
                    } else {
                        jobjTemp.put("custom", "false");
                    }
                    dataIndexJSONObject.put("iscustom", isCustom.equals("true"));
                }
                dataIndexJSONObject.put("moduleId", moduleId);
                dataIndexJSONObject.put("xtype", jObj.getString("xtype"));
                dataIndexObject.put(defautlHeaderId, dataIndexJSONObject);
                jobjTemp.put("isMeasureItem", jObj.getBoolean("isMeasureItem"));
                if (defaultHeader.equals("Discount")) {
                    dataIndexTableName = jObj.optString("reftablename", "");
                    isLineItem = jObj.optBoolean("isLineItem", false);
                    isDiscountPresent = true;
                }
                if (defaultHeader.trim().equals(CustomReportConstants.Quantity) || defaultHeader.equals(CustomReportConstants.BaseQuantity)
                        || defaultHeader.equals(CustomReportConstants.BALANCE_QUANTITY) || defaultHeader.equals(CustomReportConstants.ACTUAL_QUANTITY)
                        || defaultHeader.equals(CustomReportConstants.RECEIVED_QUANTITY) || defaultHeader.equals(CustomReportConstants.RETURN_QUANTITY)
                        || defaultHeader.equals(CustomReportConstants.DELIVERED_QUANTITY)) {
                    dataIndexTableName = jObj.optString("reftablename", "");
                    isQtyLineItem = jObj.optBoolean("isLineItem", false);
                    isAnyQunatityPresent = true;
                }
                if (defaultHeader.trim().equals(CustomReportConstants.Display_UOM)) {
                    isDisplayUOMPresent = true;
                    isQtyLineItem = jObj.optBoolean("isLineItem", false);
                }
                jobjTemp.put(Constants.isforformulabuilder, jObj.optBoolean(Constants.isforformulabuilder, false));
                jarrColumns.put(jobjTemp);
            } else {
                JSONArray mapjarrayObj = jObj.getJSONArray("mappingJSONObj");
                String defaultHeader = jObj.getString("defaultHeader");
                String displayName = jObj.getString("displayName");
                String defautlHeaderId = jObj.getString("id");

                jobjTemp.put("displayName", defaultHeader);      // Put Original default Header in display name because we are using this in UI for headers.
                if (columnmap.containsValue(defaultHeader)) {      // Check for duplicate value for default header. If duplicate found then update that default header.
                    //defaultHeader = jObj.optString("columntype","") + defaultHeader + cnt;
                    defaultHeader = defaultHeader + cnt;
                }
                columnmap.put(defaultHeader, defaultHeader);
                jObj.put("defaultHeader", defaultHeader);         // Put updated value in main json object to build select sql query. Note that there is no effect of this default header on UI , because we use displayName to show header in UI.
                jobjTemp.put("defaultHeader", defaultHeader);
                jobjTemp.put("defaultHeaderOrig", defaultHeaderOrig);
                jobjTemp.put("dataIndex", defautlHeaderId);
                jobjTemp.put("id", defautlHeaderId);
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jobjTemp.put("xtype", jObj.getString("xtype"));
                jobjTemp.put("isLineItem", jObj.optBoolean("isLineItem", false));
                dataIndexTableName = jObj.optString("reftablename", "");
                jobjTemp.put("dataIndexTableName", dataIndexTableName);
                jobjTemp.put("allowcrossmodule", jObj.optBoolean("allowcrossmodule", false));
                jobjTemp.put("crossJoinModuleId", jObj.optString("crossJoinModuleId", ""));
                // TO DO : Add Code to get it from request params through Property Grid
                if (Integer.parseInt(jObj.getString("xtype")) == 2) {
                    jobjTemp.put("precision", "2");
                    if (jObj.has("properties") && jObj.getJSONObject("properties").has("source") && jObj.getJSONObject("properties").getJSONObject("source").has("precision")) {
                        jobjTemp.put("precision", jObj.getJSONObject("properties").getJSONObject("source").getString("precision"));
                    }
                }
                if (!StringUtil.isNullOrEmpty(jObj.optString("customfield", ""))) {
                    String isCustom = jObj.optString("customfield", "");
                    if ("true".equalsIgnoreCase(isCustom)) {
                        jobjTemp.put("custom", "true");
                    } else {
                        jobjTemp.put("custom", "false");
                    }
                    dataIndexJSONObject.put("iscustom", isCustom.equals("true"));
                }
                dataIndexJSONObject.put("moduleId", moduleId);
                dataIndexJSONObject.put("xtype", jObj.getString("xtype"));
                jobjTemp.put("isMeasureItem", jObj.getBoolean("isMeasureItem"));
                dataIndexObject.put(defautlHeaderId, dataIndexJSONObject);
                if (defaultHeader.equals("Discount")) {
                    dataIndexTableName = jObj.optString("reftablename", "");
                    isLineItem = jObj.optBoolean("isLineItem", false);
                    isDiscountPresent = true;
                }
                jobjTemp.put(Constants.isforformulabuilder, jObj.optBoolean(Constants.isforformulabuilder, false));
                jarrColumns.put(jobjTemp);
            }
        } catch (Exception ex) {
        } finally {
            returnMap.put("cnt", cnt);
            returnMap.put("isLineItem", isLineItem);
            returnMap.put("isDiscountPresent", isDiscountPresent);
            returnMap.put("isAnyQunatityPresent", isAnyQunatityPresent);
            returnMap.put("isQtyLineItem", isQtyLineItem);
            returnMap.put("isDisplayUOMPresent", isDisplayUOMPresent);
            returnMap.put("dataIndexJSONObject", dataIndexJSONObject);
            returnMap.put("dataIndexObject", dataIndexObject);
            returnMap.put("dataIndexTableName", dataIndexTableName);
            returnMap.put("jarrColumns", jarrColumns);
            returnMap.put("columnmap", columnmap);
        }
        return returnMap;
    }

    public Map buildcolumnDetailsCustomReport(JSONObject jObj, HashMap<String, String> columnmap, int cnt, JSONObject dataIndexObject,
            JSONArray jarrColumns,JSONObject properties,JSONArray filterarray, String moduleId, String dataIndexTableName, boolean isDiscountPresent, boolean isLineItem, boolean isAnyQunatityPresent, boolean isQtyLineItem,boolean isDisplayUOMPresent) {

        JSONObject dataIndexJSONObject = new JSONObject();
        JSONObject jobjTemp = new JSONObject();
        Map<String, Object> returnMap = new HashMap<String, Object>();

        try {
            jobjTemp = new JSONObject();
            dataIndexJSONObject = new JSONObject();
            if (!jObj.optBoolean("isMeasureItem", false)) {
                String defaultHeader = jObj.getString("defaultHeader");
                String defautlHeaderId = jObj.getString("id");
                String defaultHeaderOrig = jObj.getString("defaultHeader");
                jobjTemp.put("defaultHeaderOrig", defaultHeaderOrig);
                if (!StringUtil.isNullOrEmpty(jObj.optString("dbcolumnname", ""))) {//&& !jObj.optBoolean("customfield", false)) {
                    String refTable = jObj.optString("reftablename", "");
                    if (StringUtil.isNullOrEmpty(refTable)) {
                        refTable = jObj.getString("dbtablename");
                    }
                    String colname = jObj.optString("reftabledatacolumn", "").length() > 0 ? jObj.optString("reftabledatacolumn", "") : jObj.getString("dbcolumnname");
                    if (columnmap.containsValue(defaultHeader)) {      // Check for duplicate value for default header. If duplicate found then update that default header.
                        //defaultHeader = jObj.optString("columntype","") + defaultHeader + cnt;
                        defaultHeader = defaultHeader + cnt;
                    }
                    columnmap.put(defaultHeader, defaultHeader);       // Put value of default header in cloumn map.
                    jObj.put("defaultHeader", defaultHeader);          // Put updated value in main json object to build select sql query. Note that there is no effect of this default header on UI , because we use displayName to show header in UI.
                    jobjTemp.put("defaultHeader", defaultHeader);
                    jobjTemp.put("displayName", jObj.getString("displayName"));
                    jobjTemp.put("dataIndex", defautlHeaderId);
                    jobjTemp.put("id", defautlHeaderId);
                    jobjTemp.put("dataIndexTableName", refTable);
                    jobjTemp.put("allowcrossmodule", jObj.optBoolean("allowcrossmodule", false));
                    jobjTemp.put("isgrouping", jObj.optBoolean("isgrouping", false));
                    jobjTemp.put("isLineItem", jObj.optBoolean("isLineItem", false));
                    jobjTemp.put("summaryType", jObj.optString("summaryType", "").toLowerCase());
                    if (!StringUtil.isNullOrEmpty(jObj.optString("crossJoinMainTable"))) {  //for filters on cross module fields
                        jobjTemp.put("crossJoinMainTable", jObj.optString("crossJoinMainTable"));
                    }
                    if (jObj.optString("defaultHeader", "").equals(CustomReportConstants.TERM_AMOUNT) || jObj.optString("defaultHeader", "").equals(CustomReportConstants.AMOUNT) || (jObj.optString("defaultHeader", "").contains("(") && jObj.optString("defaultHeader", "").contains(")"))) {
                        dataIndexJSONObject.put(CustomReportConstants.filtercolumnname, colname);
                    } else if (jObj.optString("defaultHeader", "").equals(CustomReportConstants.CLOSED_MANUALLY)) { // Added for Closed Manually Field to constuct the SQL query correctly
                        dataIndexJSONObject.put(CustomReportConstants.filtercolumnname, jObj.optString("dbcolumnname", ""));
                    } else if (jObj.optString("defaultHeader", "").equals(CustomReportConstants.TYPE)) {
                        dataIndexJSONObject.put(CustomReportConstants.filtercolumnname, jObj.optString("reftabledatacolumn", ""));
                    } else if (colname.contains("(") && colname.contains(")") && colname.contains(",")) {
                        dataIndexJSONObject.put(CustomReportConstants.filtercolumnname, colname);
                    } else {
                        dataIndexJSONObject.put(CustomReportConstants.filtercolumnname, refTable.concat(".").concat(colname));
                    }
                }
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jobjTemp.put("xtype", jObj.getString("xtype"));
                jobjTemp.put("properties", jObj.getJSONObject("properties"));
                // TO DO : Add Code to get it from request params through Property Grid
                if (Integer.parseInt(jObj.getString("xtype")) == 2) {
                    jobjTemp.put("precision", Constants.AMOUNT_DIGIT_AFTER_DECIMAL);
                    if (jObj.has("properties") && jObj.getJSONObject("properties").has("source") && jObj.getJSONObject("properties").getJSONObject("source").has("precision")) {
                        jobjTemp.put("precision", jObj.getJSONObject("properties").getJSONObject("source").getString("precision"));
                    }
                }
                if (!StringUtil.isNullOrEmpty(jObj.optString("customfield", ""))) {
                    String isCustom = jObj.optString("customfield", "");
                    if ("true".equalsIgnoreCase(isCustom)) {
                        jobjTemp.put("custom", "true");
                    } else {
                        jobjTemp.put("custom", "false");
                    }
                    dataIndexJSONObject.put("iscustom", isCustom.equals("true"));
                }
                dataIndexJSONObject.put("moduleId", moduleId);
                dataIndexJSONObject.put("xtype", jobjTemp.getString("xtype"));
                dataIndexObject.put(defautlHeaderId, dataIndexJSONObject);
                jobjTemp.put("isMeasureItem", jObj.optBoolean("isMeasureItem", false));
                if (defaultHeader.equals("Discount")) {
                    dataIndexTableName = jObj.optString("reftablename", "");
                    isLineItem = jObj.optBoolean("isLineItem", false);
                    properties = jObj.getJSONObject("properties");
                    isDiscountPresent = true;
                }
                if (defaultHeader.trim().equals(CustomReportConstants.Quantity) || defaultHeader.equals(CustomReportConstants.BaseQuantity)
                        || defaultHeader.equals(CustomReportConstants.BALANCE_QUANTITY) || defaultHeader.equals(CustomReportConstants.ACTUAL_QUANTITY)
                        || defaultHeader.equals(CustomReportConstants.RECEIVED_QUANTITY) || defaultHeader.equals(CustomReportConstants.RETURN_QUANTITY)
                        || defaultHeader.equals(CustomReportConstants.DELIVERED_QUANTITY)) {
                    dataIndexTableName = jObj.optString("reftablename", "");
                    isQtyLineItem = jObj.optBoolean("isLineItem", false);
                    isAnyQunatityPresent = true;
                }
                if (defaultHeader.trim().equals(CustomReportConstants.Display_UOM)) {
                    isDisplayUOMPresent = true;
                    isQtyLineItem = jObj.optBoolean("isLineItem", false);
                }
                jobjTemp.put("filter", getFilterType(filterarray, defautlHeaderId));
                jobjTemp.put(Constants.isforformulabuilder, jObj.optBoolean(Constants.isforformulabuilder, false));

                jarrColumns.put(jobjTemp);

            } else {
                //JSONArray mapjarrayObj = jObj.getJSONArray("mappingJSONObj");
                String defaultHeader = jObj.getString("defaultHeader");
                String displayName = jObj.getString("displayName");
                String defautlHeaderId = jObj.getString("id");
                String defaultHeaderOrig = jObj.getString("defaultHeader");
                jobjTemp.put("defaultHeaderOrig", defaultHeaderOrig);
                if (columnmap.containsValue(defaultHeader)) {      // Check for duplicate value for default header. If duplicate found then update that default header.
                    //defaultHeader = jObj.optString("columntype","") + defaultHeader + cnt;
                    defaultHeader = defaultHeader + cnt;
                }
                columnmap.put(defaultHeader, defaultHeader);       // Put value of default header in cloumn map.
                jObj.put("defaultHeader", defaultHeader);          // Put updated value in main json object to build select sql query. Note that there is no effect of this default header on UI , because we use displayName to show header in UI.
                jobjTemp.put("defaultHeader", defaultHeader);

                jobjTemp.put("displayName", jObj.getString("displayName"));
                jobjTemp.put("dataIndex", defautlHeaderId);
                jobjTemp.put("id", defautlHeaderId);
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jobjTemp.put("xtype", jObj.getString("xtype"));
                jobjTemp.put("isLineItem", jObj.optBoolean("isLineItem", false));
                dataIndexTableName = jObj.optString("reftablename", "");
                jobjTemp.put("properties", jObj.getJSONObject("properties"));
                jobjTemp.put("dataIndexTableName", dataIndexTableName);
                jobjTemp.put("isgrouping", jObj.optBoolean("isgrouping", false));
                jobjTemp.put("summaryType", jObj.optString("summaryType", "").toLowerCase());
                jobjTemp.put("allowcrossmodule", jObj.optBoolean("allowcrossmodule", false));
                jobjTemp.put("crossJoinModuleId", jObj.optString("crossJoinModuleId", ""));
                if (!StringUtil.isNullOrEmpty(jObj.optString("crossJoinMainTable"))) {  //for filters on cross module fields
                    jobjTemp.put("crossJoinMainTable", jObj.optString("crossJoinMainTable"));
                }
                // TO DO : Add Code to get it from request params through Property Grid
                if (Integer.parseInt(jObj.getString("xtype")) == 2) {
                    jobjTemp.put("precision", "2");
                    if (jObj.has("properties") && jObj.getJSONObject("properties").has("source") && jObj.getJSONObject("properties").getJSONObject("source").has("precision")) {
                        jobjTemp.put("precision", jObj.getJSONObject("properties").getJSONObject("source").getString("precision"));
                    }
                }
                if (!StringUtil.isNullOrEmpty(jObj.optString("customfield", ""))) {
                    String isCustom = jObj.optString("customfield", "");
                    if ("true".equalsIgnoreCase(isCustom)) {
                        jobjTemp.put("custom", "true");
                    } else {
                        jobjTemp.put("custom", "false");
                    }
                    dataIndexJSONObject.put("iscustom", isCustom.equals("true"));
                }
                dataIndexJSONObject.put("moduleId", moduleId);
                dataIndexJSONObject.put("xtype", jObj.getString("xtype"));
                jobjTemp.put("isMeasureItem", jObj.optBoolean("isMeasureItem", false));
                dataIndexObject.put(defautlHeaderId, dataIndexJSONObject);
                jobjTemp.put("filter", getFilterType(filterarray, defautlHeaderId));
                jobjTemp.put(Constants.isforformulabuilder, jObj.optBoolean(Constants.isforformulabuilder, false));
                jarrColumns.put(jobjTemp);
            }
        } catch (Exception ex) {
        } finally {
            returnMap.put("cnt", cnt);
            returnMap.put("isLineItem", isLineItem);
            returnMap.put("isDiscountPresent", isDiscountPresent);
            returnMap.put("isAnyQunatityPresent", isAnyQunatityPresent);
            returnMap.put("isQtyLineItem", isQtyLineItem);
            returnMap.put("isDisplayUOMPresent", isDisplayUOMPresent);
            returnMap.put("dataIndexJSONObject", dataIndexJSONObject);
            returnMap.put("dataIndexObject", dataIndexObject);
            returnMap.put("dataIndexTableName", dataIndexTableName);
            returnMap.put("jarrColumns", jarrColumns);
            returnMap.put("columnmap", columnmap);
            returnMap.put("properties", properties);
        }
        return returnMap;
    }

    public Map getSortSequenceForQuery(JSONArray selectedRows) throws ServiceException, JSONException, ParseException {

        StringBuilder sortQueryOrderBuilder = new StringBuilder();
        String sortQueryOrder = "";
        TreeMap<String, JSONObject> measureFieldSortMap = new TreeMap<String, JSONObject>();
        JSONArray sortConfigArray = new JSONArray();
        HashMap<String, Object> sortDataInfoMap = new HashMap<String, Object>();
        TreeMap<String, String> sortMap = new TreeMap<String, String>();
        if (selectedRows != null && selectedRows.length() > 0) {
            for (int cnt = 0; cnt < selectedRows.length(); cnt++) {
                JSONObject row = selectedRows.getJSONObject(cnt);
                JSONObject sortConfigObj = new JSONObject();
                String sortOrder = row.getJSONObject("properties").getJSONObject("source").optString("sortOrder","");
                String sortSeq = row.getJSONObject("properties").getJSONObject("source").optString("sortSequence");
                sortQueryOrderBuilder.setLength(0);
                if (!"None".equalsIgnoreCase(sortOrder) && !StringUtil.isNullOrEmpty(sortSeq)) {
                    String sname = row.getJSONObject("properties").getJSONObject("source").getString("(Column Name)");
                    sortQueryOrderBuilder.append(" " + "`#" + sname + "#`" + " " + sortOrder + " " + ",");
                    sortMap.put(sortSeq, sortQueryOrderBuilder.toString());
                    sortConfigObj.put("property", row.getString("id"));
                    sortConfigObj.put("direction", sortOrder);
                    sortConfigObj.put("sortedfield", sname);
                    sortConfigObj.put("isMeasureItem", row.optBoolean("isMeasureItem", false));
                    sortConfigObj.put("sortSeq", sortSeq);
                    sortConfigArray.put(sortConfigObj);

                    if(row.optBoolean("isMeasureItem", false)) {
                        JSONObject measureSortObj = new JSONObject();
                        measureSortObj.put("measureFieldID", row.getString("id"));
                        measureSortObj.put("sortOrder", sortOrder);
                        measureFieldSortMap.put(sortSeq,measureSortObj);
                    }
                }
            }
        }

        if (sortMap.size() > 0) {
            for (Map.Entry<String, String> entry : sortMap.entrySet()) {
                String value = entry.getValue();
                sortQueryOrder += value;
            }
            if (sortQueryOrder.lastIndexOf(",") > -1) {
                sortQueryOrder = " ORDER BY " + sortQueryOrder.substring(0, sortQueryOrder.lastIndexOf(","));
            }

        }
        sortConfigArray = sortJsonArrayOnTransaction(sortConfigArray,"sortSeq","");
        sortDataInfoMap.put("sortConfigArray", sortConfigArray);
        sortDataInfoMap.put("measureFieldSortMap", measureFieldSortMap);
        sortDataInfoMap.put("sortClause", sortQueryOrder);
        return sortDataInfoMap;
    }

     public JSONArray sortJsonArrayOnTransaction(JSONArray array,final String id,final String sOrder) throws JSONException {
        List<JSONObject> jsons = new ArrayList<JSONObject>();
        try {
            for (int i = 0; i < array.length(); i++) {
                jsons.add(array.getJSONObject(i));
            }
            Collections.sort(jsons, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject lhs, JSONObject rhs) {
                    Double lid = 0.00, rid = 0.00;
                    try {
                        lid = lhs.getDouble(id);
                        rid = rhs.getDouble(id);
                    } catch (JSONException ex) {
                        Logger.getLogger(AccCustomReportController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if("ASC".equalsIgnoreCase(sOrder)) {
                        return lid.compareTo(rid);
                    } else if("DESC".equalsIgnoreCase(sOrder)){
                        return rid.compareTo(lid);
                    } else {
                        return lid.compareTo(rid);
                    }

                }
            });
        } catch (JSONException ex) {
            Logger.getLogger(AccCustomReportController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(AccCustomReportController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new JSONArray(jsons);
    }

    
    private double calculateSalesReturnMeasures(String srid, Map valueMap, JSONObject selectedJSONObj, String companyID) throws ServiceException, JSONException {
        double calculatedMeasureValue = 0;
        double amount = 0, amountwithouttax = 0, ordertaxamount = 0;
        if (srid != null) {
            List<Object[]> list = new ArrayList<Object[]>();
            Object[] invoiceidArray = new Object[4];
            invoiceidArray[0] = srid;
            invoiceidArray[1] = false;
            KwlReturnObject result = accCustomerReportServiceDao.getSalesReturnCreationAndEntryDates(srid);
            if (result != null) {
                List lst = result.getEntityList();
                Iterator ite = lst.iterator();
                while (ite.hasNext()) {
                    Object[] row = (Object[]) ite.next();
                    invoiceidArray[2] = row[0];
                    invoiceidArray[3] = row[1];
                }
            }
            list.add(invoiceidArray);
            JSONObject paramJobj = new JSONObject();
            paramJobj.put(Constants.companyKey, valueMap.get("companyID"));
            paramJobj.put("userdateformat", (String) valueMap.get("userDateFormat"));
            paramJobj.put(Constants.globalCurrencyKey, valueMap.get("gcurrencyid"));
            paramJobj.put(Constants.currencyKey, valueMap.get("gcurrencyid"));
            JSONArray dataJSONArr = accInvoiceServiceDAO.getSalesReturnJson(paramJobj, list);
            if (dataJSONArr != null && dataJSONArr.length() > 0) {
                int dataJArrIndex = dataJSONArr.length() - 1;
                ordertaxamount = dataJSONArr.getJSONObject(dataJArrIndex).getDouble("totaltaxamount");
                amount = dataJSONArr.getJSONObject(dataJArrIndex).getDouble("amount");
                amountwithouttax = dataJSONArr.getJSONObject(dataJArrIndex).getDouble("amountwithouttax");
            }
            if (CustomReportConstants.Acc_Sales_Order_Measure_Total_Tax.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = ordertaxamount;
            }
            if (CustomReportConstants.Acc_Sales_Return_Measure_AmtBeforeTax.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = amountwithouttax;
            }
            if (CustomReportConstants.Acc_Sales_Order_Measure_Total_Amount.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = amount;
            }
        }
        return calculatedMeasureValue;
    }

    
    private double calculatePurchaseReturnMeasures(String prid, Map valueMap, JSONObject selectedJSONObj, String companyID) throws ServiceException, JSONException {
        double calculatedMeasureValue = 0;
        List list = new ArrayList();
        list.add(prid);
        valueMap.put("companyid", companyID);
        valueMap.put("isReportBuilder", true);
        JSONArray resultArray = accGoodsReceiptServiceDAO.getPurchaseReturnJson(valueMap, list);

        if (CustomReportConstants.Acc_Sales_Order_Measure_Total_Tax.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
            calculatedMeasureValue = resultArray.getJSONObject(0).optDouble("amountBeforeTax");
        }
        if (CustomReportConstants.Acc_Sales_Return_Measure_AmtBeforeTax.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
            calculatedMeasureValue = resultArray.getJSONObject(0).optDouble("amountBeforeTax");
        }
        if (CustomReportConstants.Acc_Sales_Order_Measure_Total_Amount.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
            calculatedMeasureValue = resultArray.getJSONObject(0).optDouble("amount");
        }
        return calculatedMeasureValue;
    }

    private double calculateSalesInvoiceMeasures(String invoiceid, Map valueMap, JSONObject selectedJSONObj, List<String> processedinvoiceIds, JSONArray dataJSONArr) throws ServiceException, JSONException {

        double calculatedMeasureValue = 0;
        double totalTaxAmount = 0.0, amountWithVAT = 0.0, amountWithoutVAT = 0.0, totalTermAmount = 0.0, amountWithWTH = 0.0, amountBeforeTax=0.0, subtotal=0.0;
        if (invoiceid != null) {
            if (!processedinvoiceIds.contains(invoiceid)) {
                List<Object[]> list = new ArrayList<Object[]>();
                Object[] invoiceidArray = new Object[4];
                invoiceidArray[0] = invoiceid;
                invoiceidArray[1] = false;
                KwlReturnObject result = accCustomerReportServiceDao.getCustomerInvoiceCreationAndEntryDates(invoiceid);
                if (result != null) {
                    List lst = result.getEntityList();
                    Iterator ite = lst.iterator();
                    while (ite.hasNext()) {
                        Object[] row = (Object[]) ite.next();
                        invoiceidArray[2] = row[0];
                        invoiceidArray[3] = row[1];
                    }
                }
                list.add(invoiceidArray);
                JSONObject paramJobj = new JSONObject();
                paramJobj.put(Constants.companyKey, valueMap.get("companyID"));
                paramJobj.put("userdateformat", (String) valueMap.get("userDateFormat"));
                paramJobj.put(Constants.globalCurrencyKey, valueMap.get("gcurrencyid"));
                paramJobj.put(Constants.currencyKey, valueMap.get("gcurrencyid"));
                try {
                    dataJSONArr = accInvoiceServiceDAO.getInvoiceJsonMergedJson(paramJobj, list, dataJSONArr);
                    processedinvoiceIds.add(invoiceid);
                    if (dataJSONArr != null && dataJSONArr.length() > 0) {
                        int dataJArrIndex = dataJSONArr.length() - 1;
                        amountWithoutVAT = dataJSONArr.getJSONObject(dataJArrIndex).getDouble("amountbeforegst");
                        totalTaxAmount = dataJSONArr.getJSONObject(dataJArrIndex).getDouble("taxamount");
                        amountWithVAT = dataJSONArr.getJSONObject(dataJArrIndex).getDouble("amount");
                        amountWithWTH = dataJSONArr.getJSONObject(dataJArrIndex).getDouble("amount");
                        totalTermAmount = dataJSONArr.getJSONObject(dataJArrIndex).getDouble("termamount");
                        amountBeforeTax = dataJSONArr.getJSONObject(dataJArrIndex).getDouble("amountBeforeTax");
                        subtotal = dataJSONArr.getJSONObject(dataJArrIndex).getDouble("subtotal");
                    }
                } catch (SessionExpiredException ex) {
                    Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                }

            } else {
                if(dataJSONArr.length() != 0){
                    int dataJArrIndex = dataJSONArr.length() - 1;
                    amountWithoutVAT = dataJSONArr.getJSONObject(dataJArrIndex).getDouble("amountbeforegst");
                    totalTaxAmount = dataJSONArr.getJSONObject(dataJArrIndex).getDouble("taxamount");
                    amountWithVAT = dataJSONArr.getJSONObject(dataJArrIndex).getDouble("amount");
                    amountWithWTH = dataJSONArr.getJSONObject(dataJArrIndex).getDouble("amount");
                    totalTermAmount = dataJSONArr.getJSONObject(dataJArrIndex).getDouble("termamount");
                    amountBeforeTax = dataJSONArr.getJSONObject(dataJArrIndex).getDouble("amountBeforeTax");
                    subtotal = dataJSONArr.getJSONObject(dataJArrIndex).getDouble("subtotal");
                }

            }
            if (CustomReportConstants.Acc_Invoice_Vat_Amount.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim()) || CustomReportConstants.GROSS_Tax.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim()) ) {
                calculatedMeasureValue = totalTaxAmount;
            }
            if (CustomReportConstants.Acc_Invoice_Amount_Without_VAT.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = amountWithoutVAT;
            }
            if (CustomReportConstants.Acc_Invoice_Amount_With_VAT.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = amountWithVAT;
            }
            if (CustomReportConstants.Acc_Invoice_WHT_Amount.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = totalTermAmount;
            }
            if (CustomReportConstants.Acc_Invoice_Amount_Without_WHT.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = amountWithoutVAT;
            }
            if (CustomReportConstants.Acc_Invoice_Amount_With_WHT.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = amountWithWTH;
            }
            if (CustomReportConstants.Acc_Sales_Order_Measure_Total_Tax.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = totalTaxAmount;
            }
            if (CustomReportConstants.Amount_Before_Tax.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = amountBeforeTax;
            }
            if (CustomReportConstants.Sub_Total.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = subtotal;
            }
        }
        return calculatedMeasureValue;
    }

    private double calculatePurchaseInvoiceMeasures(String invoiceid, Map valueMap, JSONObject selectedJSONObj) throws ServiceException, JSONException, SessionExpiredException {

        double calculatedMeasureValue = 0;
        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invoiceid);
        String companyid = (String) valueMap.get("companyID");
        GoodsReceipt goodsReceipt = (GoodsReceipt) objItr.getEntityList().get(0);
        double rowTermAmount=0;
        HashMap<String, Object> request = new HashMap<String, Object>();
        request.put("companyid", companyid);
        request.put("gcurrencyid", (String) valueMap.get("gcurrencyid"));
        request.put("billid", invoiceid);
        request.put("isFixedAsset", valueMap.containsKey("isFixedAsset") ? valueMap.get("isFixedAsset") : false);
        request.put("dateformat", (DateFormat) authHandler.getDateOnlyFormat());
        JSONArray DataJArr = new JSONArray();
        KwlReturnObject resultObj = null;
        resultObj = accGoodsReceiptobj.getGoodsReceiptsMerged(request);
         DataJArr = accGoodsReceiptServiceHandler.getGoodsReceiptsJsonMerged(request, resultObj.getEntityList(), DataJArr, accountingHandlerDAOobj, accCurrencyDAOobj, accGoodsReceiptobj, accAccountDAOobj, accGoodsReceiptCMN, accTaxObj);
        if (goodsReceipt != null) {

            if (CustomReportConstants.Acc_Purchase_Order_Measure_Gross_Total_Amount.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = DataJArr.getJSONObject(0).optDouble("productTotalAmount");
            }
            if (CustomReportConstants.Acc_Delivery_Order_Measure_Sub_Total.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = DataJArr.getJSONObject(0).optDouble("subtotal");
            }
            if (CustomReportConstants.Acc_Sales_Order_Measure_Total_Tax.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                if(DataJArr.length() > 0){
                calculatedMeasureValue = DataJArr.getJSONObject(0).optDouble("taxamount");
            }
            }
            if (CustomReportConstants.Amount_Before_Tax.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = DataJArr.getJSONObject(0).optDouble("amountBeforeTax");
            }
        }
        return calculatedMeasureValue;
    }


    private double calculateDeliveryOrderMeasures(String doid, Map valueMap, JSONObject selectedJSONObj,String companyID ) throws ServiceException, JSONException {
        
        JSONObject paramObj = new  JSONObject();
        paramObj.put("companyid", companyID);
        Object[] oj = new Object[2];
        oj[0]=doid;
        oj[1]=0;
        List doList = new ArrayList();
        doList.add(oj);
        JSONArray resultArray = accInvoiceServiceDAO.getDeliveryOrdersJsonMerged(paramObj, doList);
        double calculatedMeasureValue = 0;

            if (CustomReportConstants.Acc_Delivery_Order_Measure_Sub_Total.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = resultArray.getJSONObject(0).optDouble("subtotal");
            }
            
            if (CustomReportConstants.Acc_GRO_Measure_Discount.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = resultArray.getJSONObject(0).optDouble("discount");
            }
            if (CustomReportConstants.Amount_Before_Tax.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = resultArray.getJSONObject(0).optDouble("amountBeforeTax");
            }
            if (CustomReportConstants.Acc_Sales_Order_Measure_Total_Tax.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = resultArray.getJSONObject(0).optDouble("taxamount");
            }
            if (CustomReportConstants.Acc_Sales_Order_Measure_Total_Amount.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = resultArray.getJSONObject(0).optDouble("amount");
            }
            if (CustomReportConstants.Total_Amount_In_Base.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = resultArray.getJSONObject(0).optDouble("amountinbase");
            }
        return calculatedMeasureValue;
    }

    private double calculateDebitNoteMeasures(String dnid, Map valueMap, JSONObject selectedJSONObj) throws ServiceException, JSONException {
        double calculatedMeasureValue = 0;
        JournalEntryDetail details = null;
        String companyid = (String) valueMap.get("companyID");
        KwlReturnObject dnObjList = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), dnid);
        DebitNote debitMemo = (DebitNote) dnObjList.getEntityList().get(0);
        if (debitMemo != null) {
            KwlReturnObject jeForDebitNote = accCustomerReportServiceDao.getJEForDebitNote(debitMemo.getID());
            if (jeForDebitNote != null) {
                List lst = jeForDebitNote.getEntityList();
                Iterator ite = lst.iterator();
                while (ite.hasNext()) {
                    String row = (String) ite.next();
                    if (!StringUtil.isNullOrEmpty(row)) {
                        KwlReturnObject jeObjList = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), row);
                        JournalEntry jeDetail = (JournalEntry) jeObjList.getEntityList().get(0);
                        if (jeDetail != null) {
                            Set<JournalEntryDetail> jeDetails = jeDetail.getDetails();
                            if (jeDetails != null) {
                                Iterator iterator = jeDetails.iterator();
                                while (iterator.hasNext()) {
                                    details = (JournalEntryDetail) iterator.next();
                                }
                            }
                        }

                    }
                }
            }
        }
        // For measure fields - begin
        double amountdue = debitMemo.isOtherwise() ? debitMemo.getDnamountdue() : 0;
        double paidAmount = debitMemo.isOtherwise() ? debitMemo.getDnamount() : details.getAmount();
        double amountDueOriginal = debitMemo.isOtherwise() ? debitMemo.getDnamountdue() : 0;
        Set<DebitNoteTaxEntry> dnTaxEntryDetails = debitMemo.getDnTaxEntryDetails();
        String reason = "";
        double totalCnTax = 0, taxamountinbase = 0, taxamount = 0, amount = 0;
        if (dnTaxEntryDetails != null && !dnTaxEntryDetails.isEmpty()) {

            for (DebitNoteTaxEntry noteTaxEntry : dnTaxEntryDetails) {
                reason += ((noteTaxEntry.getReason() != null) ? noteTaxEntry.getReason().getValue() : "") + ",";
                if (noteTaxEntry.isDebitForMultiCNDN()) {
                    totalCnTax -= noteTaxEntry.getTaxamount();
                } else {
                    totalCnTax += noteTaxEntry.getTaxamount();
                }
            }
        }
        amountdue = (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid);
        //obj.put("taxamount", authHandler.round(totalCnTax, Constants.AMOUNT_DIGIT_AFTER_DECIMAL));
        taxamount = totalCnTax;
        //KwlReturnObject taxAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, authHandler.round(totalCnTax, Constants.AMOUNT_DIGIT_AFTER_DECIMAL), transactionCurrencyId, debitNoteDate, externalCurrencyRate);
        taxamountinbase = totalCnTax;
        //obj.put("taxamountinbase", authHandler.round((Double) taxAmt.getEntityList().get(0), 2));
        double cnTotalAmount = debitMemo.isOtherwise() ? debitMemo.getDnamount() : ((details != null) ? details.getAmount() : 0);
        //obj.put("amountbeforegst", authHandler.round(cnTotalAmount - totalCnTax, Constants.AMOUNT_DIGIT_AFTER_DECIMAL));
        double amountbeforegst = 0.0;
        amountbeforegst = authHandler.round(cnTotalAmount - totalCnTax, companyid);
        // End
        if (CustomReportConstants.Acc_Sales_Order_Measure_Amount_Before_GST.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
            calculatedMeasureValue = amountbeforegst;
        }
        if (CustomReportConstants.Acc_Sales_Order_Measure_Total_Tax.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
            calculatedMeasureValue = taxamount;
        }
        if (CustomReportConstants.Acc_Debit_Note_Amount_Due.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
            calculatedMeasureValue = amountdue;
        }
        if (CustomReportConstants.Acc_Debit_Note_Amount.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
            calculatedMeasureValue = paidAmount;
        }
        return calculatedMeasureValue;
    }

    private double calculateCreditNoteMeasures(String cnid, Map valueMap, JSONObject selectedJSONObj) throws ServiceException, JSONException {
        double calculatedMeasureValue = 0;
        JournalEntryDetail details = null;
        String companyid = (String) valueMap.get("companyID");
        KwlReturnObject dnObjList = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), cnid);
        CreditNote creditMemo = (CreditNote) dnObjList.getEntityList().get(0);
        if (creditMemo != null) {
            KwlReturnObject jeForCreditNote = accCustomerReportServiceDao.getJEForCreditNote(creditMemo.getID());
            if (jeForCreditNote != null) {
                List lst = jeForCreditNote.getEntityList();
                Iterator ite = lst.iterator();
                while (ite.hasNext()) {
                    String row = (String) ite.next();
                    if (!StringUtil.isNullOrEmpty(row)) {
                        KwlReturnObject jeObjList = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), row);
                        JournalEntry jeDetail = (JournalEntry) jeObjList.getEntityList().get(0);
                        if (jeDetail != null) {
                            Set<JournalEntryDetail> jeDetails = jeDetail.getDetails();
                            if (jeDetails != null) {
                                Iterator iterator = jeDetails.iterator();
                                while (iterator.hasNext()) {
                                    details = (JournalEntryDetail) iterator.next();
                                }
                            }
                        }

                    }
                }

            }
        }
        double amountdue = creditMemo.isOtherwise() ? creditMemo.getCnamountdue() : 0;
        double paidAmount = creditMemo.isOtherwise() ? creditMemo.getCnamount() : details.getAmount();
        amountdue = (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid);
        Set<CreditNoteTaxEntry> cnTaxEntryDetails = creditMemo.getCnTaxEntryDetails();
        String reason = "";
        double totalCnTax = 0, taxamount = 0;
        if (cnTaxEntryDetails != null && !cnTaxEntryDetails.isEmpty()) {
            for (CreditNoteTaxEntry noteTaxEntry : cnTaxEntryDetails) {
                reason += ((noteTaxEntry.getReason() != null) ? noteTaxEntry.getReason().getValue() : "") + ",";
                if (noteTaxEntry.isDebitForMultiCNDN()) {
                    totalCnTax += noteTaxEntry.getTaxamount();
                } else {
                    totalCnTax -= noteTaxEntry.getTaxamount();
                }
            }
        }
        taxamount = authHandler.round(totalCnTax, companyid);
        double cnTotalAmount = creditMemo.isOtherwise() ? creditMemo.getCnamount() : details.getAmount();
        double amountbeforegst = 0.0;
        amountbeforegst = authHandler.round(cnTotalAmount - totalCnTax, companyid);

        if (CustomReportConstants.Acc_Sales_Order_Measure_Total_Tax.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
            calculatedMeasureValue = taxamount;
        }
        if (CustomReportConstants.Acc_Sales_Order_Measure_Amount_Before_GST.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
            calculatedMeasureValue = amountbeforegst;
        }
        if (CustomReportConstants.Acc_Debit_Note_Amount_Due.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
            calculatedMeasureValue = amountdue;
        }
        if (CustomReportConstants.Acc_Debit_Note_Amount.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
            calculatedMeasureValue = paidAmount;
        }
        return calculatedMeasureValue;
    }
    
       private double calculatePurchaseRequisitionMeasures(String purchaseRequisitionID, Map valueMap, JSONObject selectedJSONObj, String companyID) throws ServiceException, JSONException {

        double calculatedMeasureValue = 0;
        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(PurchaseRequisition.class.getName(), purchaseRequisitionID);
        PurchaseRequisition purchaseRequstion = (PurchaseRequisition) objItr.getEntityList().get(0);
        if (purchaseRequstion != null) {
            Iterator itrRow = purchaseRequstion.getRows().iterator();
            double amount = 0, amountinbase = 0, totalDiscount = 0, discountPrice = 0;
            while (itrRow.hasNext()) {
                PurchaseRequisitionDetail sod = (PurchaseRequisitionDetail) itrRow.next();
                double rowTaxPercent = 0;
                if (sod.getTax() != null) {
                    rowTaxPercent = getTaxPercent(companyID, purchaseRequstion.getRequisitionDate(), sod.getTax().getID());

                }
                double quotationPrice = authHandler.round(sod.getQuantity() * sod.getRate(), purchaseRequstion.getCompany().getCompanyID());
                if (sod.getDiscountispercent() == 1) {
                    discountPrice = (quotationPrice) - (quotationPrice * sod.getDiscount() / 100);
                } else {
                    discountPrice = quotationPrice - sod.getDiscount();
                }
                amount += discountPrice + (discountPrice * rowTaxPercent / 100);
            }

            if (purchaseRequstion.getDiscount() != 0) {
                if (purchaseRequstion.isPerDiscount()) {
                    totalDiscount = amount * purchaseRequstion.getDiscount() / 100;
                    amount = amount - totalDiscount;
                } else {
                    amount = amount - purchaseRequstion.getDiscount();
                    totalDiscount = purchaseRequstion.getDiscount();
                }

            }
            if (purchaseRequstion.getTax() != null) {
                double TaxPercent = getTaxPercent(companyID, purchaseRequstion.getRequisitionDate(), purchaseRequstion.getTax().getID());
                amountinbase = amount + amount * TaxPercent / 100;
            }
            if (purchaseRequstion.getTax() != null) {
                calculatedMeasureValue = amountinbase;
            } else {
                HashMap requestParams = new HashMap<String, Object>();
                requestParams.put("companyid", valueMap.get("companyID"));
                requestParams.put("gcurrencyid", valueMap.get("gcurrencyid"));
                amountinbase = getCurrencyToBaseAmount(requestParams, amount, purchaseRequstion.getCurrency().getCurrencyID(), purchaseRequstion.getRequisitionDate(), 0);
                calculatedMeasureValue = amountinbase;

            }
            if (CustomReportConstants.Acc_Sales_Order_Measure_Total_Amount.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = amount;
            }
            if (CustomReportConstants.Total_Amount_In_Base.trim().equalsIgnoreCase(selectedJSONObj.getString("defaultHeaderOrig").trim())) {
                calculatedMeasureValue = amountinbase;
            }
        }

        return calculatedMeasureValue;
    }

    @Override
    public JSONObject saveCustomWidgetReports(JSONObject paramJobj) throws ServiceException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject resultObj = null;
        try {


            resultObj = accCustomerReportServiceDao.getCustomWidgetReports(paramJobj);
            List<CustomWidgetReports> reportList = resultObj.getEntityList();
            resultObj = null;

            if (reportList.size() > 0) {
                jobj.put(CustomReportConstants.MESSAGE_KEY, "acc.CustomReport.dupReport");
                jobj.put("success", false);
                jobj.put("valid", true);

            } else {
                long createdon = System.currentTimeMillis();
                long updatedon = createdon;
                paramJobj.put("createdon", createdon);
                paramJobj.put("updatedon", updatedon);

                resultObj = accCustomerReportServiceDao.saveCustomWidgetReports(paramJobj);
                List<CustomWidgetReports> list = resultObj.getEntityList();

                if (resultObj.getEntityList().size() > 0) {
                    jobj.put("id", list.get(0).getID());
                    jobj.put("success", true);
                    jobj.put("valid", true);
                } else {
                    jobj.put("success", false);
                    jobj.put("valid", true);
                }
            }

        } catch (JSONException ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    
    @Override
    public JSONObject getCustomWidgetReports(JSONObject paramJobj) throws JSONException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray dataArr = new JSONArray();
        JSONObject dataObj = null;

        KwlReturnObject resultObj = null;
        KwlReturnObject objItr = null;
        resultObj = accCustomerReportServiceDao.getCustomWidgetReports(paramJobj);
        List<CustomWidgetReports> list = resultObj.getEntityList();
        JSONObject reportJobj = new JSONObject();

        for (CustomWidgetReports report : list) {
            dataObj = new JSONObject();
            String selectedReports =  report.getCustomReports();
            JSONArray selectedReportsArr = new JSONArray(selectedReports);
            JSONArray selectedReportsNewArr = new JSONArray();
            for (int cnt = 0; cnt < selectedReportsArr.length(); cnt++) {
                objItr = accountingHandlerDAOobj.getObject(ReportMaster.class.getName(), selectedReportsArr.optJSONObject(cnt).optString("id"));
                ReportMaster reportMaster = (ReportMaster) objItr.getEntityList().get(0);

                if (reportMaster != null) {
                    reportJobj = new JSONObject();
                    reportJobj.put("id", reportMaster.getID());
                    reportJobj.put("moduleid", reportMaster.getModuleid());
                    reportJobj.put("reportname", reportMaster.getName());
                    reportJobj.put("methodName", reportMaster.getMethodName());
                    reportJobj.put("isdefault", reportMaster.isIsdefault());
                    selectedReportsNewArr.put(reportJobj);
                } else {
                    selectedReportsNewArr.put(selectedReportsArr.optJSONObject(cnt));
                }
            }

            dataObj.put("id", report.getID());
            dataObj.put("reportname", report.getReportName());
            dataObj.put("customreports", selectedReportsNewArr.toString());
            dataObj.put("searchcriteria", report.getSearchCriteria());
            dataObj.put("createdby", report.getCreatedby().getFullName());
            dataObj.put("createdon", report.getCreatedon());
            dataObj.put("filterappend", report.getFilterAppend());
            dataObj.put("deleted", report.isDeleted());
            dataArr.put(dataObj);
        }

        jobj.put(Constants.RES_success, true);
        jobj.put(Constants.RES_data, dataArr);
        jobj.put("count", resultObj.getRecordTotalCount());
        return jobj;
    }

    @Override
    public boolean deleteCustomWidgetReport(JSONObject paramJobj) throws ServiceException {

        boolean isReportDeleted = false;
        try {
            isReportDeleted = accCustomerReportServiceDao.deleteCustomWidgetReport(paramJobj);

        } catch (Exception e) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return isReportDeleted;

    }

    private JSONObject createStoreRec(JSONArray columnArray) {
        JSONObject metaData = new JSONObject();
        try {
            metaData.put("totalProperty", "totalCount");
            metaData.put("root", "data");
            JSONObject fieldsJobj = new JSONObject();
            JSONArray fieldsArray = new JSONArray();

            fieldsJobj.put("name","currencysymbol");
            fieldsArray.put(fieldsJobj);

            fieldsJobj = new JSONObject();
            fieldsJobj.put("name","billid");
            fieldsArray.put(fieldsJobj);

            for (int i = 0; i < columnArray.length(); i++) {
                if (!columnArray.optJSONObject(i).optString("id").contains(".")) {
                    fieldsJobj = new JSONObject();
                    fieldsJobj.put("name", columnArray.optJSONObject(i).optString("id"));
                    fieldsArray.put(fieldsJobj);
                }
            }
            metaData.put("fields", fieldsArray);

        } catch (JSONException ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return metaData;
    }

    private JSONObject getSearchJsonForModuleID(Map requestParams) throws SessionExpiredException, UnsupportedEncodingException, ServiceException {
        JSONObject resultObj = null;
        JSONArray dataJArrObj = new JSONArray();
        String Searchjson = requestParams.get(Constants.Acc_Search_Json).toString();
        String moduleid = requestParams.get(Constants.moduleid) != null ? requestParams.get(Constants.moduleid).toString() : "0";
        if(moduleid.equalsIgnoreCase(Constants.CUSTOMER_MODULE_UUID)){
            moduleid = String.valueOf(Constants.Acc_Customer_ModuleId);
        }

        try {
            JSONObject jObj = new JSONObject(Searchjson);
            if (!StringUtil.isNullOrEmpty(Searchjson) && !moduleid.equals("0")) {
                int count = jObj.getJSONArray(Constants.root).length();
                for (int i = 0; i < count; i++) {
                    JSONObject jobj1 = jObj.getJSONArray(Constants.root).getJSONObject(i);
                    if (jobj1.optString(Constants.moduleid).equalsIgnoreCase(moduleid)) {
                        dataJArrObj.put(jobj1);
                    }
                }
                jObj.put(Constants.root, dataJArrObj);
            } else {
                jObj = new JSONObject(Searchjson);
            }
            resultObj = jObj;
        } catch (JSONException ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resultObj;
    }

    public void replaceAll(StringBuilder builder, String from, String to) {
        int index = builder.indexOf(from);
        while (index != -1) {
            builder.replace(index, index + from.length(), to);
            index += to.length(); // Move to the end of the replacement
            index = builder.indexOf(from, index);
        }
    }

    @Override
    public JSONArray processReportDataFromReportListService(JSONArray reportJArr, Map<String, Object> valueMap) throws ServiceException {
        try {
            String parentReportId = (String) valueMap.get("parentReportId");
            KwlReturnObject dhDetailsKwlObj = accCustomerReportServiceDao.getDefaultHeaderDetailsForReportFromReportList(parentReportId);
            List<Object[]> dhDetailsList = dhDetailsKwlObj.getEntityList();
            if (dhDetailsList != null && !dhDetailsList.isEmpty() && reportJArr.length() > 0) {
                for (int i = 0; i < reportJArr.length(); i++) {
                    JSONObject jObj = reportJArr.optJSONObject(i);
                    for (Object[] dhDetails : dhDetailsList) {
                        String defaultHeaderId = (String) dhDetails[0];
                        String dhDummyValue = (String) dhDetails[1];
                        String xtype = (String) dhDetails[2];
                        if (jObj.has(dhDummyValue)) {
                            jObj.put(defaultHeaderId, jObj.get(dhDummyValue));
                            if (xtype.equals("3")) {

                                DateFormat udf = new SimpleDateFormat("MMMM d, yyyy", Locale.ENGLISH);
                                Date date = udf.parse((String) jObj.get(defaultHeaderId));
                                Calendar cal = Calendar.getInstance();
                                cal.setTime(date);

                                String dateForSortIndex = new SimpleDateFormat("YYYY-MM-d").format(cal.getTime());    //to be used as sortIndex for date fields in pivot configuration
                                String month = new SimpleDateFormat("MMM").format(cal.getTime());
                                int day = cal.get(Calendar.DAY_OF_MONTH);
                                int year = cal.get(Calendar.YEAR);

                                jObj.put("date_" + defaultHeaderId, (dateForSortIndex!=null) ? dateForSortIndex : "");
                                if (year != 0) {
                                    jObj.put("year_" + defaultHeaderId, year);
                                }
                                if (!StringUtil.isNullOrEmpty(month)) {
                                    jObj.put("month_" + defaultHeaderId, month);
                                }
                                if (day != 0) {
                                    jObj.put("day_" + defaultHeaderId, day);
                                }
                            }
                        }
                    }
                    reportJArr.put(i, jObj);
                }
            }
        } catch (JSONException | ParseException ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return reportJArr;
    }

    @Override
    public JSONObject processCustomizedReportColumnsData(JSONArray columnsJArr, Map<String, Object> valueMap) throws ServiceException {
        JSONObject resultColumnsJobj = new JSONObject();
        try {
//            String parentReportId = (String) valueMap.get("parentReportId");
            if (columnsJArr.length() > 0) {
                for(int i = 0; i<columnsJArr.length();i++) {
                    JSONObject columnJobj = columnsJArr.optJSONObject(i);
                    JSONObject columnJobjNew = new JSONObject();
//                    columnJobjNew.put("moduleId", parentReportId);
                    columnJobjNew.put("filtercolumnname", "");
                    columnJobjNew.put("xtype", columnJobj.optInt("xtype"));
                    columnJobjNew.put("dataIndex", columnJobj.optString("id"));
                    columnJobjNew.put("iscustom", columnJobj.optBoolean("customfield", false));
                    resultColumnsJobj.put(columnJobj.optString("id"), columnJobjNew);
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resultColumnsJobj;
    }

    @Override
    public JSONArray processCustomizedColumnsDataForReport(JSONArray columnsJArr) throws ServiceException {
        try {
            if (columnsJArr.length() > 0) {
                for(int i = 0; i<columnsJArr.length();i++) {
                    JSONObject columnJobj = columnsJArr.optJSONObject(i);
                    columnJobj.put("dataIndex", columnJobj.optString("id"));
                    columnJobj.put("width", 150);
                    columnJobj.put("pdfwidth", 150);
                    columnJobj.put("custom", columnJobj.optBoolean("customfield", false));
                    columnJobj.put("filter", new JSONArray());
                    columnJobj.put("isLineItem", false);
                    columnJobj.put("summaryType", "");
                    columnsJArr.put(i, columnJobj);
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return columnsJArr;
    }

    @Override
    public JSONObject getCustomizedReportURLandParams(HashMap<String, Object> requestParams) throws ServiceException {
        JSONObject resultJobj = new JSONObject();
        JSONObject params = new JSONObject();
        try {
            String reportUrl = accCustomerReportServiceDao.getReportURLFromReportList((String) requestParams.get("parentReportId"));
            resultJobj.put("reportUrl", reportUrl);
            resultJobj.put("params", params);

        } catch (ServiceException | JSONException e) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return resultJobj;

    }

    @Override
    public JSONObject fetchCustomReportDetails(Map<String, Object> requestParams) throws ServiceException {
        JSONObject resultJobj = new JSONObject();
        try {
            ReportMaster accCustomReport = accCustomerReportServiceDao.fetchCustomReportDetails((String) requestParams.get("reportID"));
            String reportJsonString = accCustomReport.getReportjson();
            boolean isPivot = accCustomReport.isIspivot();
            if(isPivot) {
                JSONObject reportJson = new JSONObject(reportJsonString);
                resultJobj.put("reportJson", reportJson);
                resultJobj.put("isPivot", isPivot);
            } else {
                JSONArray reportJson = new JSONArray(reportJsonString);
                resultJobj.put("reportJson", reportJson);
                resultJobj.put("isPivot", isPivot);
            }
        } catch (ServiceException | JSONException e) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return resultJobj;
    }

    @Override
    public JSONObject executeCustomizedReportPreview(JSONArray reportJArr,JSONArray columnsJArr, HashMap<String, Object> valueMap) throws ServiceException {
        JSONObject resultJobj = new JSONObject();
        try {
            reportJArr = processReportDataFromReportListService(reportJArr, valueMap);
            JSONObject columnsJObj = processCustomizedReportColumnsData(columnsJArr, valueMap);
            JSONArray sortConfigArray = new JSONArray();
            resultJobj.put("reportJArr", reportJArr);
            resultJobj.put("columnsJObj", columnsJObj);
            resultJobj.put("sortConfigArray", sortConfigArray);
        } catch (ServiceException | JSONException e) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return resultJobj;
    }

    @Override
    public JSONObject executeCustomizedReport(JSONArray reportJArr, HashMap<String, Object> valueMap) throws ServiceException {
        JSONObject resultJobj = new JSONObject();
        try {
            JSONObject userPreferences = new JSONObject();
            userPreferences.put("fromdate", valueMap.get("fromDate"));
            userPreferences.put("todate", valueMap.get("toDate"));

            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("reportID", valueMap.get("reportID"));
            JSONObject reportDetailsJobj = fetchCustomReportDetails(requestParams);
            boolean isPivot = reportDetailsJobj.optBoolean("isPivot", false);
            valueMap.put("isPivot", isPivot);
            reportJArr = processReportDataFromReportListService(reportJArr, valueMap);
            JSONObject columnsJobj = new JSONObject();
            JSONObject pivotConfig = new JSONObject();
            JSONArray columnsJArr = new JSONArray();
            if(isPivot) {
                columnsJobj = reportDetailsJobj.optJSONObject("reportJson");
                columnsJArr = columnsJobj.optJSONArray("columnConfig");
                pivotConfig = columnsJobj.optJSONObject("pivotConfig");
            } else {
                columnsJArr = reportDetailsJobj.optJSONArray("reportJson");
            }
            columnsJArr = processCustomizedColumnsDataForReport(columnsJArr);
            JSONArray sortConfigArray = new JSONArray();
            JSONObject filterJson = new JSONObject();
            resultJobj.put("sortConfigArray", sortConfigArray);
            resultJobj.put("columnsJArr", columnsJArr);
            resultJobj.put("reportJArr", reportJArr);
            resultJobj.put("userPreferences", userPreferences);
            resultJobj.put("filterJson", filterJson);
            resultJobj.put("isPivot", isPivot);
            resultJobj.put("pivotConfig", pivotConfig);
        } catch (ServiceException | JSONException e) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        return resultJobj;
    }

    public String getDetailsTableForMainTable(String moduleMainTableName) {
        String module = "";

        if (moduleMainTableName.equals(String.valueOf(CustomReportConstants.Acc_Sales_Order))) {
            module = CustomReportConstants.Acc_Sales_Order_Details;
        } else if (moduleMainTableName.equals(String.valueOf(CustomReportConstants.Acc_Purchase_Order))) {
            module = CustomReportConstants.Acc_Purchase_Order_Details;
        } else if (moduleMainTableName.equals(String.valueOf(CustomReportConstants.Acc_Sales_Invoice))) {
            module = CustomReportConstants.Acc_Sales_Invoice_Details;
        } else if (moduleMainTableName.equals(String.valueOf(CustomReportConstants.Acc_Sales_Return))) {
            module = CustomReportConstants.Acc_Sales_Return_Details;
        } else if (moduleMainTableName.equals(String.valueOf(CustomReportConstants.Acc_Purchase_Return))) {
            module = CustomReportConstants.Acc_Purchase_Return_Details;
        } else if (moduleMainTableName.equals(String.valueOf(CustomReportConstants.Acc_Vendor_Invoice))) {
            module = CustomReportConstants.Acc_Vendor_Invoice_Details;
        } else if (moduleMainTableName.equals(String.valueOf(CustomReportConstants.Acc_Delivery_Order))) {
            module = CustomReportConstants.Acc_Delivery_Order_Details;
        } else if (moduleMainTableName.equals(String.valueOf(CustomReportConstants.Acc_Debit_Note))) {
            module = CustomReportConstants.Acc_Debit_Note_Details;
        } else if (moduleMainTableName.equals(String.valueOf(CustomReportConstants.Acc_Credit_Note))) {
            module = CustomReportConstants.Acc_Credit_Note_Details;
        } else if (moduleMainTableName.equals(String.valueOf(CustomReportConstants.Acc_Goods_Receipt))) {
            module = CustomReportConstants.Acc_Goods_Receipt_Details;
        } else if (moduleMainTableName.equals(String.valueOf(CustomReportConstants.Acc_CUSTOMER))) {
            module = CustomReportConstants.Acc_CUSTOMER_ADDRESS_DETAILS;
        } else if (moduleMainTableName.equals(String.valueOf(CustomReportConstants.Acc_Quotation))) {
            module = CustomReportConstants.Acc_Quotation_Details;
        } else if(moduleMainTableName.equals(String.valueOf(CustomReportConstants.Acc_purchaseRequisition))){
            module = CustomReportConstants.Acc_purchaseRequisition_Details;
        } else if(moduleMainTableName.equals(String.valueOf(CustomReportConstants.product))){
            module = CustomReportConstants.product;
        }

        return module;
    }

    public String getCrossLinkingDetailsTableForMainTable(String moduleMainTableName, String crossModuleTable) {
        String module = "";

        if (moduleMainTableName.equals(String.valueOf(CustomReportConstants.Acc_Sales_Order))) {
            if (crossModuleTable.equalsIgnoreCase(CustomReportConstants.Acc_Purchase_Order_Details)) {
                module = CustomReportConstants.Acc_SO_TO_PO_Column_Reference;
            } else if (crossModuleTable.equalsIgnoreCase(CustomReportConstants.Acc_Quotation_Details)) {
                module = CustomReportConstants.Acc_SI_TO_CQ_Column_Reference;
            } else if (crossModuleTable.equalsIgnoreCase(CustomReportConstants.Acc_Sales_Invoice_Details) || crossModuleTable.equalsIgnoreCase(CustomReportConstants.Acc_Delivery_Order_Details)) {
                module = CustomReportConstants.Acc_SO_TO_SI_DO_Column_Reference;
            }
        } else if (moduleMainTableName.equals(String.valueOf(CustomReportConstants.Acc_Purchase_Order))) {
            if (crossModuleTable.equalsIgnoreCase(CustomReportConstants.Acc_Sales_Order_Details)) {
                module = CustomReportConstants.Acc_PO_TO_SO_Column_Reference;
            } else if (crossModuleTable.equalsIgnoreCase(CustomReportConstants.Acc_Vendor_Invoice_Details)) {
                module = "id";
            } else if (crossModuleTable.equalsIgnoreCase(CustomReportConstants.Acc_Goods_Receipt_Details)) {
                module = "id";
            }
        } else if (moduleMainTableName.equals(String.valueOf(CustomReportConstants.Acc_Sales_Invoice))) {
            if (crossModuleTable.equalsIgnoreCase(CustomReportConstants.Acc_Sales_Order_Details)) {
                module = CustomReportConstants.Acc_SI_TO_SO_Column_Reference;
            } else if (crossModuleTable.equalsIgnoreCase(CustomReportConstants.Acc_Quotation_Details)) {
                module = CustomReportConstants.Acc_SI_TO_CQ_Column_Reference;
            } else if (crossModuleTable.equalsIgnoreCase(CustomReportConstants.Acc_Delivery_Order_Details)) {
                module = CustomReportConstants.Acc_SI_TO_DO_Column_Reference;
            } else if (crossModuleTable.equalsIgnoreCase(CustomReportConstants.Acc_Sales_Return_Details)) {
                module = CustomReportConstants.Acc_SI_TO_SR_Column_Reference;
            }
        } else if (moduleMainTableName.equals(String.valueOf(CustomReportConstants.Acc_Sales_Return))) {
            module = ""; // TO DO Mapping for cross join other module with sales return
        } else if (moduleMainTableName.equals(String.valueOf(CustomReportConstants.Acc_Purchase_Return))) {
            module = ""; // TO DO Mapping for cross join other module with purchase return
        } else if (moduleMainTableName.equals(String.valueOf(CustomReportConstants.Acc_Vendor_Invoice))) {
            if (crossModuleTable.equalsIgnoreCase(CustomReportConstants.Acc_Purchase_Order_Details)) {
                module = CustomReportConstants.Acc_PO_TO_VI_Column_Reference;
            } else if (crossModuleTable.equalsIgnoreCase(CustomReportConstants.Acc_Goods_Receipt_Details)) {
                module = CustomReportConstants.Acc_PO_TO_GR_Column_Reference;
            } else if (crossModuleTable.equalsIgnoreCase(CustomReportConstants.Acc_Purchase_Return_Details)) {
                module = CustomReportConstants.Acc_PO_TO_PR_Column_Reference;
            }
        } else if (moduleMainTableName.equals(String.valueOf(CustomReportConstants.Acc_Delivery_Order))) {
            module = ""; // TO DO Mapping for cross join other module with delivery order
        } else if (moduleMainTableName.equals(String.valueOf(CustomReportConstants.Acc_Debit_Note))) {
            if (crossModuleTable.equalsIgnoreCase(CustomReportConstants.Acc_Vendor_Invoice_Details)) {
                module = CustomReportConstants.Acc_Vendor_Invoice;
            }
        } else if (moduleMainTableName.equals(String.valueOf(CustomReportConstants.Acc_Credit_Note))) {
            if (crossModuleTable.equalsIgnoreCase(CustomReportConstants.Acc_Sales_Invoice_Details)) {
                module = CustomReportConstants.Acc_Sales_Invoice;
            }
        }
        else if(moduleMainTableName.equals(String.valueOf(CustomReportConstants.Acc_Goods_Receipt))){
            if (crossModuleTable.equalsIgnoreCase(CustomReportConstants.Acc_Purchase_Order_Details)) {
                module = CustomReportConstants.Acc_Purchase_Order_Details;
            } else if (crossModuleTable.equalsIgnoreCase(CustomReportConstants.Acc_Vendor_Invoice_Details)) {
                module = CustomReportConstants.Acc_GR_TO_VI_Column_Reference;
            }
        } else if (moduleMainTableName.equals(String.valueOf(CustomReportConstants.Acc_CUSTOMER))) {
            module = ""; // TO DO Mapping for cross join other module with customer
        }

        return module;
    }

    public String mergeLeftJoins(String str1, StringBuilder str2) {
        String leftJoin = "",s1="";
        String[] strArry1= null;
        String[] strArry2= null;
        Set<String> set = new LinkedHashSet<String>();
        if(!StringUtil.isNullOrEmpty(str1) && !StringUtil.isNullOrEmpty(str2.toString()) && !str1.trim().equalsIgnoreCase(str2.toString().trim())) {
            if (!StringUtil.isNullOrEmpty(str1) && str1.indexOf("left join") > -1) {
                strArry1 = str1.split("left join");
            }
            if (str2 != null && !StringUtil.isNullOrEmpty(str2.toString()) && str2.indexOf("left join") > -1) {
                strArry2 = str2.toString().split("left join");
            }
            if (strArry1 != null) {
                for (int i = 0; i < strArry1.length; i++) {
                    strArry1[i] = strArry1[i].trim();
                }
                set.addAll(Arrays.asList(strArry1));
            }
            if (strArry2 != null) {
                for (int i = 0; i < strArry2.length; i++) {
                    strArry2[i] = strArry2[i].trim();
                }
                set.addAll(Arrays.asList(strArry2));
            }

            String[] strArry3 = set.toArray(new String[set.size()]);
            if (strArry3 != null && strArry3.length > 0) {
//                if (!leftJoin.contains(strArry3[0])) {
//                    leftJoin += strArry3[0];
//                }
                for (int i = 1; i < strArry3.length; i++) {
                    s1 += " left join " + strArry3[i];
                }
                leftJoin += leftJoin + s1;
            }
        }
        return leftJoin;

    }

    @Override
    public JSONObject saveOrUpdateChartDetails(JSONObject paramObj) throws ServiceException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject resultObj = null;
        try {
            long curTime = System.currentTimeMillis();

            paramObj.put("createdon", curTime);
            paramObj.put("modifiedon", curTime);

            resultObj = accCustomerReportServiceDao.saveOrUpdateChartDetails(paramObj);
            if (resultObj.isSuccessFlag()) {
                jobj.put("msg", resultObj.getMsg());
                jobj.put("count", resultObj.getRecordTotalCount());
                jobj.put("success", true);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }

    @Override
    public JSONObject getChartDetails(JSONObject paramObj) throws ServiceException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject resultObj = null;
        try {
            resultObj = accCustomerReportServiceDao.getChartDetails(paramObj);
            List<CustomReportCharts> list = resultObj.getEntityList();
            JSONArray jArr = new JSONArray();
            for(CustomReportCharts chartDetails : list) {
                JSONObject temp = new JSONObject();

                if(!StringUtil.isNullOrEmpty(chartDetails.getID())) {
                    temp.put("id", chartDetails.getID());
                }

                if(!StringUtil.isNullOrEmpty(chartDetails.getReportID())) {
                    temp.put(CustomReportConstants.REPORT_ID, chartDetails.getReportID());
                }

                if(!StringUtil.isNullOrEmpty(chartDetails.getChartName())) {
                    temp.put(CustomReportConstants.CHART_NAME, chartDetails.getChartName());
                }

                if(!StringUtil.isNullOrEmpty(chartDetails.getChartType())) {
                    temp.put(CustomReportConstants.CHART_TYPE, chartDetails.getChartType());
                }

                if(!StringUtil.isNullOrEmpty(chartDetails.getTitleField())) {
                    temp.put(CustomReportConstants.TITLE_FIELD, chartDetails.getTitleField());
                }

                if(!StringUtil.isNullOrEmpty(chartDetails.getValueField())) {
                    temp.put(CustomReportConstants.VALUE_FIELD, chartDetails.getValueField());
                }

                if(!StringUtil.isNullOrEmpty(chartDetails.getGroupby())) {
                    temp.put(CustomReportConstants.GROUP_BY, chartDetails.getGroupby());
                }

                if(!StringUtil.isNullOrEmpty(chartDetails.getProperties())) {
                    temp.put("properties", chartDetails.getProperties());
                }

                if(!StringUtil.isNullOrEmpty(chartDetails.getCreatedby().getUserID())) {
                    temp.put("createdby", chartDetails.getCreatedby().getUserID());
                }

                if(!StringUtil.isNullOrEmpty(chartDetails.getModifiedby().getUserID())) {
                    temp.put("modifiedby", chartDetails.getModifiedby().getUserID());
                }

                if(!StringUtil.isNullOrEmpty(chartDetails.getCompany().getCompanyID())) {
                    temp.put(Constants.companyKey, chartDetails.getCompany().getCompanyID());
                }

                temp.put("createdon", chartDetails.getCreatedOn());
                temp.put("modifiedon", chartDetails.getModifiedOn());

                jArr.put(temp);
            }
            jobj.put(Constants.data, jArr);
        } catch (JSONException ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }

    @Override
    public JSONObject deleteChartDetails(JSONObject paramObj) throws ServiceException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject resultObj = null;
        try {
            resultObj = accCustomerReportServiceDao.deleteChartDetails(paramObj);
            if (resultObj.isSuccessFlag()) {
                jobj.put("success", true);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }

    @Override
    public JSONObject filterAccountsAndInvoiceColumns(JSONArray selectedRowsJSON) throws ServiceException {
        JSONArray accountColumns = new JSONArray();
        JSONArray invoiceColumns = new JSONArray();
        String[] selectedRowsDHName = null;
        JSONObject jobj = new JSONObject();;
        String amount_Excluding_GST_HeaderFiledID = "";
        if (selectedRowsJSON != null) {
            selectedRowsDHName = new String[selectedRowsJSON.length()];
            try {
                for (int selectedRowsJSONCnt = 0; selectedRowsJSONCnt < selectedRowsJSON.length(); selectedRowsJSONCnt++) {
                    JSONObject selectedRowsJSONElement = selectedRowsJSON.getJSONObject(selectedRowsJSONCnt);

                    if (selectedRowsJSONElement.optBoolean("customfield", false) && !selectedRowsJSONElement.optBoolean("allowcrossmodule", false)) {
                        accountColumns.put(selectedRowsJSONElement);
                    } else {
                        selectedRowsDHName[selectedRowsJSONCnt] = selectedRowsJSONElement.optString("defaultHeader", "").trim();
                        if (selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.Acc_Account)
                                || selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.TYPE)
                                || selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.Acc_Tax_Percent)
                                || (selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.Acc_Sales_Order_Measure_Total_Tax) && !selectedRowsJSONElement.optBoolean("allowcrossmodule", false))
                                || (selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.AMOUNT) && !selectedRowsJSONElement.optBoolean("allowcrossmodule", false))
                                || selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.Acc_Description)
                                || selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.Acc_Amount_Excluding_GST)
                                || selectedRowsJSONElement.optString("defaultHeader", "").trim().contains(CustomReportConstants.Acc_CGST)
                                || selectedRowsJSONElement.optString("defaultHeader", "").trim().contains(CustomReportConstants.Acc_SGST)
                                || selectedRowsJSONElement.optString("defaultHeader", "").trim().contains(CustomReportConstants.Acc_IGST)
                                || selectedRowsJSONElement.optString("defaultHeader", "").trim().contains(CustomReportConstants.Acc_UTGST)
                                || selectedRowsJSONElement.optString("defaultHeader", "").trim().contains(CustomReportConstants.Acc_CESS)
                                || selectedRowsJSONElement.optString("defaultHeader", "").trim().contains(CustomReportConstants.Product_Tax_Class)) {
                            accountColumns.put(selectedRowsJSONElement);
                            //Below line is added to get the id for "Amount Excluding GST" field and pass it as key with the value for totalamountforaccount to work with export report as xls option(exportSalesOrderforCustomReportBuilder)
                            if (selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.Acc_Amount_Excluding_GST)) {
                                amount_Excluding_GST_HeaderFiledID = selectedRowsJSONElement.getString("id");
                            }
                        } else if (selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.Acc_Invoice_Number)
                                || selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.Acc_Creation_Date)
                                || selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.Acc_Due_Date)
                                || selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.Acc_Linking_Date)
                                || selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.Acc_Invoice_Amount)
                                || selectedRowsJSONElement.optString("defaultHeader", "").trim().equalsIgnoreCase(CustomReportConstants.Acc_Debit_Note_Amount_Due)) {
                            invoiceColumns.put(selectedRowsJSONElement);
                        } else if (selectedRowsJSONElement.optBoolean("allowcrossmodule", false)) {
                            invoiceColumns.put(selectedRowsJSONElement);
                        }
                    }
                }
                if (!jobj.has("accountColumns") && !jobj.has("invoiceColumns")) {
                    jobj.put("accountColumns", accountColumns);
                    jobj.put("invoiceColumns", invoiceColumns);
                }
            } catch (JSONException ex) {
                Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return jobj;
    }

    public JSONObject CurrencySettingsForPayment(JSONObject jarrRecord, SqlRowSet reportDataResultSet, KWLCurrency currency, String colLabel, JSONObject jarrColumns, String companyID, Map processedData, int rowNum) throws ServiceException, JSONException {

        String cBillId = (String) reportDataResultSet.getObject("id");
        List paymentDetailsRecord = new LinkedList();
        KwlReturnObject paymentDetailId = null;
        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Payment.class.getName(), cBillId);
        Payment payment = (Payment) objItr.getEntityList().get(0);
        if (payment.getCurrency() != null) {
            currency = payment.getCurrency();
        }
        JSONObject paramObj = new JSONObject();
        KwlReturnObject dbResultSet;
        if (colLabel.startsWith("#Linked ") && colLabel.contains("Amount")) {
            paramObj.put("payment", payment.getID());
            if (colLabel.startsWith("#Linked Invoice")) {
                dbResultSet = accCustomerReportServiceDao.getLinkedInvoiceFromPayment(paramObj);
                paymentDetailsRecord = dbResultSet.getEntityList();
                if (paymentDetailsRecord.size() > 0) {
                    for (int i = 0; i < paymentDetailsRecord.size(); i++) {
                        paymentDetailId = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), String.valueOf(paymentDetailsRecord.get(i)));
                        GoodsReceipt grPayment = (GoodsReceipt) paymentDetailId.getEntityList().get(0);
                        if (processedData.containsKey(payment.getPaymentNumber() + "_" + grPayment.getGoodsReceiptNumber())
                                && rowNum != (int) processedData.get(payment.getPaymentNumber() + "_" + grPayment.getGoodsReceiptNumber())) {
                            continue;
                        } else {
                            if (grPayment != null) {
                                KWLCurrency grCurrency = grPayment.getCurrency();
                                jarrRecord.put(("currencycode_" + jarrColumns.get("dataIndex")).replaceAll("-", ""), grCurrency.getCurrencyCode());
                                jarrRecord.put(("currencysymbol_" + jarrColumns.get("dataIndex")).replaceAll("-", ""), grCurrency.getSymbol());
                                processedData.put(payment.getPaymentNumber() + "_" + grPayment.getGoodsReceiptNumber(), rowNum);
                                break;
                            }
                        }
                    }
                }
            } else if (colLabel.startsWith("#Linked Credit")) {
                dbResultSet = accCustomerReportServiceDao.getLinkedCreditNoteFromPayment(paramObj);
                paymentDetailsRecord = dbResultSet.getEntityList();
                if (paymentDetailsRecord.size() > 0) {
                    for (int i = 0; i < paymentDetailsRecord.size(); i++) {
                        paymentDetailId = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), String.valueOf(paymentDetailsRecord.get(i)));
                        CreditNote cnPayment = (CreditNote) paymentDetailId.getEntityList().get(0);
                        if (processedData.containsKey(payment.getPaymentNumber() + "_" + cnPayment.getCreditNoteNumber())
                                && rowNum != (int) processedData.get(payment.getPaymentNumber() + "_" + cnPayment.getCreditNoteNumber())) {
                            continue;
                        } else {
                            if (cnPayment != null) {
                                KWLCurrency cnCurrency = cnPayment.getCurrency();
                                jarrRecord.put(("currencycode_" + jarrColumns.get("dataIndex")).replaceAll("-", ""), cnCurrency.getCurrencyCode());
                                jarrRecord.put(("currencysymbol_" + jarrColumns.get("dataIndex")).replaceAll("-", ""), cnCurrency.getSymbol());
                                processedData.put(payment.getPaymentNumber() + "_" + cnPayment.getCreditNoteNumber(), rowNum);
                                break;
                            }
                        }
                    }
                }
            }
        } else if (colLabel.startsWith("#Invoice") && colLabel.contains("Amount")) {
            paramObj.put("payment", payment.getID());
            paramObj.put("companyid", companyID);
            dbResultSet = accCustomerReportServiceDao.getInvoiceForPayment(paramObj);
            paymentDetailsRecord = dbResultSet.getEntityList();
            if (paymentDetailsRecord.size() > 0) {
                for (int i = 0; i < paymentDetailsRecord.size(); i++) {
                    paymentDetailId = accountingHandlerDAOobj.getObject(PaymentDetail.class.getName(), String.valueOf(paymentDetailsRecord.get(i)));
                    PaymentDetail paymentDetail = (PaymentDetail) paymentDetailId.getEntityList().get(0);
                    GoodsReceipt grPayment = paymentDetail.getGoodsReceipt();
                    if (processedData.containsKey(payment.getPaymentNumber() + "_" + grPayment.getGoodsReceiptNumber())
                            && rowNum != (int) processedData.get(payment.getPaymentNumber() + "_" + grPayment.getGoodsReceiptNumber())) {
                        continue;
                    } else {
                        if (grPayment != null) {
                            KWLCurrency grCurrency = grPayment.getCurrency();
                            jarrRecord.put(("currencycode_" + jarrColumns.get("dataIndex")).replaceAll("-", ""), grCurrency.getCurrencyCode());
                            jarrRecord.put(("currencysymbol_" + jarrColumns.get("dataIndex")).replaceAll("-", ""), grCurrency.getSymbol());
                            processedData.put(payment.getPaymentNumber() + "_" + grPayment.getGoodsReceiptNumber(), rowNum);
                            break;
                        }
                    }
                }
            }

        } else if (colLabel.startsWith("#Credit") && colLabel.contains("Amount")) {
            paramObj.put("payment", payment.getID());
            paramObj.put("companyid", companyID);
            dbResultSet = accCustomerReportServiceDao.getCreditNoteLinkingForPayment(paramObj);
            paymentDetailsRecord = dbResultSet.getEntityList();
            if (paymentDetailsRecord.size() > 0) {
                for (int i = 0; i < paymentDetailsRecord.size(); i++) {
                    paymentDetailId = accountingHandlerDAOobj.getObject(CreditNote.class.getName(), String.valueOf(paymentDetailsRecord.get(i)));
                    CreditNote creditNote = (CreditNote) paymentDetailId.getEntityList().get(0);
                    if (processedData.containsKey(payment.getPaymentNumber() + "_" + creditNote.getCreditNoteNumber())
                            && rowNum != (int) processedData.get(payment.getPaymentNumber() + "_" + creditNote.getCreditNoteNumber())) {
                        continue;
                    } else {
                        KWLCurrency cnCurrency = creditNote.getCurrency();
                        jarrRecord.put(("currencycode_" + jarrColumns.get("dataIndex")).replaceAll("-", ""), cnCurrency.getCurrencyCode());
                        jarrRecord.put(("currencysymbol_" + jarrColumns.get("dataIndex")).replaceAll("-", ""), cnCurrency.getSymbol());
                        processedData.put(payment.getPaymentNumber() + "_" + creditNote.getCreditNoteNumber(), rowNum);
                        break;
                    }
                }
            }

        } else {
            jarrRecord.put(("currencycode_" + jarrColumns.get("dataIndex")).replaceAll("-", ""), currency.getCurrencyCode());
            jarrRecord.put(("currencysymbol_" + jarrColumns.get("dataIndex")).replaceAll("-", ""), currency.getSymbol());
        }

        return jarrRecord;
    }

    public JSONObject CurrencySettingsForReceipt(JSONObject jarrRecord, SqlRowSet reportDataResultSet, KWLCurrency currency, String colLabel, JSONObject jarrColumns, String companyID, Map processedData, int rowNum) throws ServiceException, JSONException {

        String cBillId = (String) reportDataResultSet.getObject("id");
        List receiptDetailsRecord = new LinkedList();
        KwlReturnObject receiptDetailId = null;
        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), cBillId);
        Receipt receipt = (Receipt) objItr.getEntityList().get(0);

        if (receipt.getCurrency() != null) {
            currency = receipt.getCurrency();
        }
        JSONObject paramObj = new JSONObject();
        KwlReturnObject dbResultSet;
        if (colLabel.startsWith("#Linked ") && colLabel.contains("Amount")) {
            paramObj.put("receipt", receipt.getID());
            if (colLabel.startsWith("#Linked Invoice")) {
                dbResultSet = accCustomerReportServiceDao.getLinkedInvoiceFromReceipt(paramObj);
                receiptDetailsRecord = dbResultSet.getEntityList();
                if (receiptDetailsRecord.size() > 0) {
                    for (int i = 0; i < receiptDetailsRecord.size(); i++) {
                        receiptDetailId = accountingHandlerDAOobj.getObject(Invoice.class.getName(), String.valueOf(receiptDetailsRecord.get(i)));
                        Invoice invoice = (Invoice) receiptDetailId.getEntityList().get(0);
                        if (processedData.containsKey(receipt.getReceiptNumber() + "_" + invoice.getInvoiceNumber())
                                && rowNum != (int) processedData.get(receipt.getReceiptNumber() + "_" + invoice.getInvoiceNumber())) {
                            continue;
                        } else {

                            if (invoice != null) {
                                KWLCurrency iCurrency = invoice.getCurrency();
                                jarrRecord.put(("currencycode_" + jarrColumns.get("dataIndex")).replaceAll("-", ""), iCurrency.getCurrencyCode());
                                jarrRecord.put(("currencysymbol_" + jarrColumns.get("dataIndex")).replaceAll("-", ""), iCurrency.getSymbol());
                                processedData.put(receipt.getReceiptNumber() + "_" + invoice.getInvoiceNumber(), rowNum);
                                break;
                            }
                        }
                    }
                }
            } else if (colLabel.startsWith("#Linked Debit")) {
                dbResultSet = accCustomerReportServiceDao.getLinkedDebitNoteFromReceipt(paramObj);
                receiptDetailsRecord = dbResultSet.getEntityList();
                if (receiptDetailsRecord.size() > 0) {
                    for (int i = 0; i < receiptDetailsRecord.size(); i++) {
                        receiptDetailId = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), String.valueOf(receiptDetailsRecord.get(i)));
                        DebitNote dnPayment = (DebitNote) receiptDetailId.getEntityList().get(0);
                        if (processedData.containsKey(receipt.getReceiptNumber() + "_" + dnPayment.getDebitNoteNumber())
                                && rowNum != (int) processedData.get(receipt.getReceiptNumber() + "_" + dnPayment.getDebitNoteNumber())) {
                            continue;
                        } else {
                            if (dnPayment != null) {
                                KWLCurrency dnCurrency = dnPayment.getCurrency();
                                jarrRecord.put(("currencycode_" + jarrColumns.get("dataIndex")).replaceAll("-", ""), dnCurrency.getCurrencyCode());
                                jarrRecord.put(("currencysymbol_" + jarrColumns.get("dataIndex")).replaceAll("-", ""), dnCurrency.getSymbol());
                                processedData.put(receipt.getReceiptNumber() + "_" + dnPayment.getDebitNoteNumber(), rowNum);
                                break;
                            }
                        }
                    }
                }
            }
        } else if (colLabel.startsWith("#Invoice") && colLabel.contains("Amount")) {
            paramObj.put("receipt", receipt.getID());
            paramObj.put("companyid", companyID);
            dbResultSet = accCustomerReportServiceDao.getInvoiceForReceipt(paramObj);
            receiptDetailsRecord = dbResultSet.getEntityList();
            if (receiptDetailsRecord.size() > 0) {
                for (int i = 0; i < receiptDetailsRecord.size(); i++) {
                    receiptDetailId = accountingHandlerDAOobj.getObject(ReceiptDetail.class.getName(), String.valueOf(receiptDetailsRecord.get(0)));
                    ReceiptDetail receiptDetail = (ReceiptDetail) receiptDetailId.getEntityList().get(0);
                    Invoice invoice = receiptDetail.getInvoice();
                    if (processedData.containsKey(receipt.getReceiptNumber() + "_" + invoice.getInvoiceNumber())
                            && rowNum != (int) processedData.get(receipt.getReceiptNumber() + "_" + invoice.getInvoiceNumber())) {
                        continue;
                    } else {
                        if (invoice != null) {
                            KWLCurrency grCurrency = invoice.getCurrency();
                            jarrRecord.put(("currencycode_" + jarrColumns.get("dataIndex")).replaceAll("-", ""), grCurrency.getCurrencyCode());
                            jarrRecord.put(("currencysymbol_" + jarrColumns.get("dataIndex")).replaceAll("-", ""), grCurrency.getSymbol());
                            processedData.put(receipt.getReceiptNumber() + "_" + invoice.getInvoiceNumber(), rowNum);
                            break;
                        }
                    }
                }
            }
        } else if (colLabel.startsWith("#Debit") && colLabel.contains("Amount")) {
            paramObj.put("receipt", receipt.getID());
            paramObj.put("companyid", companyID);
            dbResultSet = accCustomerReportServiceDao.getDebitNoteForReceipt(paramObj);
            receiptDetailsRecord = dbResultSet.getEntityList();
            if (receiptDetailsRecord.size() > 0) {
                for (int i = 0; i < receiptDetailsRecord.size(); i++) {
                    receiptDetailId = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), String.valueOf(receiptDetailsRecord.get(0)));
                    DebitNote debitNote = (DebitNote) receiptDetailId.getEntityList().get(0);
                    if (processedData.containsKey(receipt.getReceiptNumber() + "_" + debitNote.getDebitNoteNumber())
                            && rowNum != (int) processedData.get(receipt.getReceiptNumber() + "_" + debitNote.getDebitNoteNumber())) {
                        continue;
                    } else {
                        if (debitNote != null) {
                            KWLCurrency dnCurrency = debitNote.getCurrency();
                            jarrRecord.put(("currencycode_" + jarrColumns.get("dataIndex")).replaceAll("-", ""), dnCurrency.getCurrencyCode());
                            jarrRecord.put(("currencysymbol_" + jarrColumns.get("dataIndex")).replaceAll("-", ""), dnCurrency.getSymbol());
                            processedData.put(receipt.getReceiptNumber() + "_" + debitNote.getDebitNoteNumber(), rowNum);
                            break;
                        }
                    }
                }
            }
        } else {
            jarrRecord.put(("currencycode_" + jarrColumns.get("dataIndex")).replaceAll("-", ""), currency.getCurrencyCode());
            jarrRecord.put(("currencysymbol_" + jarrColumns.get("dataIndex")).replaceAll("-", ""), currency.getSymbol());
        }

        return jarrRecord;
    }
    /**
     * save grid state config of custom report for current user
     * @param paramObj
     * @return JSONObject
     * @throws ServiceException 
     */
    @Override
    public JSONObject saveGridConfig(JSONObject paramObj) throws ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            Company company = null;
            User user = null;
            GridConfig gridConfig = null;
            String companyid = paramObj.has("companyid") ? paramObj.getString("companyid") : null;
            String userid = paramObj.has("userid") ? paramObj.getString("userid") : null;
            String cid = paramObj.has("cid") ? paramObj.getString("cid") : null;
            String reportid = paramObj.has("reportid") ? paramObj.getString("reportid") : null;
            String state = paramObj.has("state") ? paramObj.getString("state") : null;
            
            if(StringUtil.isNullOrEmpty(cid) && !StringUtil.isNullOrEmpty(reportid) && !StringUtil.isNullOrEmpty(companyid) && !StringUtil.isNullOrEmpty(userid)) {
                company = (Company) accountingHandlerDAOobj.getObject(Company.class.getName(), companyid).getEntityList().get(0);
                user = (User) accountingHandlerDAOobj.getObject(User.class.getName(), userid).getEntityList().get(0);
                
                gridConfig = new GridConfig();
                gridConfig.setCid(java.util.UUID.randomUUID().toString());
                gridConfig.setModuleid(reportid);
                gridConfig.setCompany(company);
                gridConfig.setUser(user);
            } else {
                gridConfig = (GridConfig) accountingHandlerDAOobj.getObject(GridConfig.class.getName(), cid).getEntityList().get(0);
            }
            
            if(!StringUtil.isNullObject(gridConfig)) {
                gridConfig.setState(state);
                gridConfig.setUpdatedOn(System.currentTimeMillis());
            }
            
            if(accCustomerReportServiceDao.saveGridConfig(gridConfig).isSuccessFlag()) {
                jobj.put("cid", gridConfig.getCid());
                jobj.put("reportid", gridConfig.getModuleid());
                jobj.put("state", gridConfig.getState());
                jobj.put("success", true);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    /**
     * get saved grid state config of custom report for current user
     * @param paramObj
     * @return JSONObject
     * @throws ServiceException 
     */
    @Override
    public JSONObject getGridConfig(JSONObject paramObj) throws ServiceException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject resultObj = null;
        try {
            Company company = null;
            User user = null;
            String companyid = paramObj.has("companyid") ? paramObj.getString("companyid") : null;
            String userid = paramObj.has("userid") ? paramObj.getString("userid") : null;
            String reportid = paramObj.has("reportid") ? paramObj.getString("reportid") : null;
            
            HashMap<String, Object> params = new HashMap();
            params.put("moduleid", reportid);
            if(!StringUtil.isNullOrEmpty(companyid)) {
                company = (Company) accountingHandlerDAOobj.getObject(Company.class.getName(), companyid).getEntityList().get(0);
                params.put("company", company);
            }

            if(!StringUtil.isNullOrEmpty(userid)) {
                user = (User) accountingHandlerDAOobj.getObject(User.class.getName(), userid).getEntityList().get(0);
                params.put("user", user);
            }
            
            resultObj = accCustomerReportServiceDao.getGridConfig(params);
            if(resultObj.getRecordTotalCount() > 0) {
                GridConfig gridConfig = (GridConfig) resultObj.getEntityList().get(0);
                jobj.put("cid", gridConfig.getCid());
                jobj.put("reportid", gridConfig.getModuleid());
                jobj.put("state", gridConfig.getState());
                jobj.put("success", true);
            } else {
                jobj.put("reportid", reportid);
                jobj.put("success", false);
            }
        } catch (JSONException ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(AccCustomReportServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
    
    public String subQueryCondition(String crossJoinModuleId, String crossJoinMainTable,String key) {

        StringBuilder subQueryconditionStb = new StringBuilder();
        if (crossJoinModuleId.equals(String.valueOf(Constants.Acc_Sales_Order_ModuleId)) && crossJoinMainTable.equals(key)) {
            subQueryconditionStb.append(" where ( " + crossJoinMainTable.trim() + ".leaseOrMaintenanceSO=0 or " + crossJoinMainTable.trim() + ".leaseOrMaintenanceSO=2)  and "+crossJoinMainTable.trim() +".istemplate != 2 and "+crossJoinMainTable.trim() +".isdraft = 'false' " );
        } else if (crossJoinModuleId.equals(String.valueOf(Constants.Acc_Purchase_Order_ModuleId)) && crossJoinMainTable.equals(key)) {
            subQueryconditionStb.append(" where ( " + crossJoinMainTable.trim() + ".isfixedassetpo = false and " + crossJoinMainTable.trim() + ".isconsignment='F' and " + crossJoinMainTable.trim() + ".ismrpjobworkout = 'F' and " + crossJoinMainTable.trim() + ".istemplate != 2) ");
        } else if (crossJoinModuleId.equals(String.valueOf(Constants.Acc_Sales_Return_ModuleId)) && crossJoinMainTable.equals(key)) {
            subQueryconditionStb.append(" where ( " + crossJoinMainTable.trim() + ".isfixedasset = false and " + crossJoinMainTable.trim() + ".isconsignment='F' and " + crossJoinMainTable.trim() + ".isleasesalesreturn=false) ");
        } else if (crossJoinModuleId.equals(String.valueOf(Constants.Acc_Purchase_Return_ModuleId)) && crossJoinMainTable.equals(key)) {
            subQueryconditionStb.append(" where ( " + crossJoinMainTable.trim() + ".isfixedasset = false and " + crossJoinMainTable.trim() + ".isconsignment='F' ) ");
        } else if (crossJoinModuleId.equals(String.valueOf(Constants.Acc_Goods_Receipt_ModuleId)) && crossJoinMainTable.equals(key)) {
            subQueryconditionStb.append(" where ( " + crossJoinMainTable.trim() + ".isfixedAssetgro = false and " + crossJoinMainTable.trim() + ".isconsignment='F' ) ");
        } else if (crossJoinModuleId.equals(String.valueOf(Constants.Acc_Delivery_Order_ModuleId)) && crossJoinMainTable.equals(key)) {
            subQueryconditionStb.append(" where ( " + crossJoinMainTable.trim() + ".isfixedassetdo = false and " + crossJoinMainTable.trim() + ".isleasedo = false and " + crossJoinMainTable.trim() + ".isconsignment = 'F')");
        } else if (crossJoinModuleId.equals(String.valueOf(Constants.Acc_Invoice_ModuleId)) && crossJoinMainTable.equals(key)) {
            subQueryconditionStb.append(" where ( " + crossJoinMainTable.trim() + ".isfixedassetinvoice = false and " + crossJoinMainTable.trim() + ".isfixedassetleaseinvoice = false and " + crossJoinMainTable.trim() + ".istemplate != 2 and " + crossJoinMainTable.trim() + ".isdraft = false and " + crossJoinMainTable.trim() + ".isconsignment = 'F' and   " + crossJoinMainTable.trim() + ".deleteflag = 'F' ) ");
        } else if (crossJoinModuleId.equals(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId)) && crossJoinMainTable.equals(key)) {
            subQueryconditionStb.append(" where ( " + crossJoinMainTable.trim() + ".isfixedassetinvoice = false and " + crossJoinMainTable.trim() + ".istemplate != 2 and " + crossJoinMainTable.trim() + ".isconsignment = 'F')");
        } else if (crossJoinModuleId.equals(String.valueOf(Constants.Acc_Customer_Quotation_ModuleId)) && crossJoinMainTable.equals(key)) {
            subQueryconditionStb.append(" where ( " + crossJoinMainTable.trim() + ".istemplate != 2 and " + crossJoinMainTable.trim() + ".archieve = 0 and " + crossJoinMainTable.trim() + ".isleasequotation = 0 and " + crossJoinMainTable.trim() + ".isdraft = false )");
        }
        return subQueryconditionStb.toString();
    }
    
     public String getEwayGSTTaxRatesString(String tempString,  List<String> lineLevelTerms) throws ServiceException, JSONException {

         String[] gstAmounts = null;
         String gstIndidualAmountsStr = null;
         double individualTaxValue = 0;
         String individualTaxRates = "";
         String CGST, SGST, IGST, CESS;
         CGST = SGST = IGST = CESS = "0";

         LineLevelTerms lltObj = null;
         Map<String, KwlReturnObject> lineLevelTermsMap = new HashMap<>();
         if (lineLevelTerms != null && !StringUtil.isNullOrEmpty(tempString)) {
             gstAmounts = tempString.split(",");
             for (int llt = 0; llt < lineLevelTerms.size(); llt++) {
                 if (gstAmounts != null) {
                     for (int m = 0; m < gstAmounts.length; m++) {
                         gstIndidualAmountsStr = gstAmounts[m];
                         String[] gstIndidualAmounts = gstIndidualAmountsStr.split(":");
                         if (gstIndidualAmounts != null && Arrays.asList(gstIndidualAmounts).contains(lineLevelTerms.get(llt).trim())) {

                             KwlReturnObject termMappingResult = null;
                             if (gstIndidualAmounts[0].equalsIgnoreCase(lineLevelTerms.get(llt).trim())) {
                                 individualTaxValue = Double.parseDouble(gstIndidualAmounts[1]);
                                 String term = lineLevelTerms.get(llt).trim();

                                 switch (term) {
                                     case CustomReportConstants.Acc_E_Way_Input_CGST:
                                     case CustomReportConstants.Acc_E_Way_Output_CGST:
                                                                                        CGST = gstIndidualAmounts[2];
                                                                                        break;

                                     case CustomReportConstants.Acc_E_Way_Input_SGST:
                                     case CustomReportConstants.Acc_E_Way_Output_SGST:
                                     case CustomReportConstants.Acc_E_Way_Input_UTGST:
                                     case CustomReportConstants.Acc_E_Way_Output_UTGST:
                                                                                        SGST = gstIndidualAmounts[2];
                                                                                        break;

                                     case CustomReportConstants.Acc_E_Way_Input_IGST:
                                     case CustomReportConstants.Acc_E_Way_Output_IGST:
                                                                                        IGST = gstIndidualAmounts[2];
                                                                                        break;

                                     case CustomReportConstants.Acc_E_Way_CESS:
                                                                                        CESS = gstIndidualAmounts[2];
                                                                                        break;

                                 }

                                 break;
                             }
                         }
                     }
                 }
             }
         }
         individualTaxRates = SGST + "+" + CGST + "+" + IGST + "+" + CESS;
         return individualTaxRates;
     }
   public double getIndividualGSTTaxes(String companyID, String tempString, String defaultHeaderValue, List<String> lineLevelTerms, Map<String, KwlReturnObject> lineLevelTermsMap) throws ServiceException, JSONException {
       String[] gstAmounts = null;
       String gstIndidualAmountsStr = null;
       double individualTaxValue = 0;
       Map<String, CustomReportsGSTFieldsLineLevelTermsMapping> customReportLineLevelTermsMap = new HashMap<>();
       LineLevelTerms lltObj = null;
       if (lineLevelTerms != null && !StringUtil.isNullOrEmpty(tempString)) {
           gstAmounts = tempString.split(",");
               if (gstAmounts != null) {
                   for (int m = 0; m < gstAmounts.length; m++) {
                       gstIndidualAmountsStr = gstAmounts[m];
                       String[] gstIndidualAmounts = gstIndidualAmountsStr.split(":");
                       int index = lineLevelTerms.indexOf(gstIndidualAmounts[0]);
                       if (gstIndidualAmounts != null && lineLevelTermsMap != null && Arrays.asList(gstIndidualAmounts).contains(lineLevelTerms.get(index).trim())) {
                               KwlReturnObject termMappingResult = null;
                               CustomReportsGSTFieldsLineLevelTermsMapping gstFieldMappingObj2 = null;
                               KwlReturnObject gstFieldMappingResult2 = accCustomerReportServiceDao.getCustomReportsGSTFieldsLineLevelTermsMapping("'"+defaultHeaderValue+"'");
                               List<CustomReportsGSTFieldsLineLevelTermsMapping> gstfm2 = gstFieldMappingResult2.getEntityList();
                               Iterator gstfm_itr2 = gstfm2.iterator();
                                    while (gstfm_itr2.hasNext()) {
                                         gstFieldMappingObj2 = (CustomReportsGSTFieldsLineLevelTermsMapping) gstfm_itr2.next();
                                         customReportLineLevelTermsMap.put(gstFieldMappingObj2.getDefaultTermsId(), gstFieldMappingObj2);
                                         
                               }
                                
                               for (Map.Entry<String, CustomReportsGSTFieldsLineLevelTermsMapping> entry : customReportLineLevelTermsMap.entrySet()) {
                                   termMappingResult = lineLevelTermsMap.get(entry.getKey());
                                   CustomReportsGSTFieldsLineLevelTermsMapping ll1 = entry.getValue();
                               if (termMappingResult != null) {
                                   List<LineLevelTerms> lltfm = termMappingResult.getEntityList();
                                   Iterator lltm_itr = lltfm.iterator();
                                   while (lltm_itr.hasNext()) {
                                       lltObj = (LineLevelTerms) lltm_itr.next();
                                   }
                               }
                               if (gstIndidualAmounts[0].equalsIgnoreCase(lineLevelTerms.get(index).trim()) && (defaultHeaderValue.equalsIgnoreCase(ll1.getDefaultHeaderId())) && (lltObj !=null && gstIndidualAmounts[0].equalsIgnoreCase(lltObj.getTerm().trim()))) {
                                   individualTaxValue = Double.parseDouble(gstIndidualAmounts[1]);
                                   break;
                               }
                                }
                           }
                       }
                   }
               }
       return individualTaxValue;
   }
    
    public static JSONArray concatJSONArrayWithoutDuplicate(JSONArray Array1, JSONArray Array2) throws JSONException {
        for (int i = 0; i < Array2.length(); i++) {
            if (Array2.optJSONObject(i) != null) {
                Array1.put(Array2.optJSONObject(i));
            }
        }
        Set<String> property = new HashSet<String>();
        JSONArray tempArray = new JSONArray();
        for (int i = 0; i < Array1.length(); i++) {
            String stationCode = Array1.getJSONObject(i).getString(CustomReportConstants.FILTER_PROPERTY);
            if (property.contains(stationCode)) {
                continue;
            } else {
                property.add(stationCode);
                tempArray.put(Array1.getJSONObject(i));
            }
        }
        Array1 = tempArray; //assign temp to original
        return Array1;
    }
      JSONArray tempArray = new JSONArray();    

    @Override
     public Map validateEWayBillReport(JSONObject dataObject, HashMap requestParams) throws JSONException, ServiceException, ParseException {
        Map returnMap = new HashMap();
        Map validationMap = null;
        Map keyValue = null;
        Map resultMap = null;
        List eWayUnits = getUnitValues();
        JSONArray validRecords = new JSONArray();
        JSONArray invalidRecords = new JSONArray();
        Set invalidDocuments = new HashSet();
        Map separatedRecords = new HashMap();
        boolean isWholeData = requestParams.get("isWholeData") == null ? false : (Boolean) requestParams.get("isWholeData");
        boolean exportValidRecordsToJSON = requestParams.get("exportValidRecordsToJSON") == null ? false : (Boolean) requestParams.get("exportValidRecordsToJSON");
        boolean exportEWayInvalidRecords = requestParams.get("exportEWayInvalidRecords") == null ? false : (Boolean) requestParams.get("exportEWayInvalidRecords");
        String moduleid = requestParams.get("moduleID") == null ? "" : (String) requestParams.get("moduleID");
        String companyName = requestParams.get("companyName") == null ? "" : (String) requestParams.get("companyName");
        Map entityDetails = getEntitydetails(requestParams);
        JSONArray columns = dataObject.optJSONArray("columns");
        boolean removeSpecialCharachter = true;
        List columnList = getMandatoryReportColumn(moduleid);
        int validRecordsCount = 0;
        String entityCity = (String) entityDetails.get("city");
        String entityGSTIN = (String) entityDetails.get("gstin");
        String entityState = (String) entityDetails.get("state");
        String entityPinCode = (String) entityDetails.get("pinCode");
        String entityAddress = (String) entityDetails.get("description");
        resultMap = checkIfReportHasAllColumns(columnList, columns);
        validationMap = (HashMap) resultMap.get("validationMap");
        keyValue = (HashMap) resultMap.get("keyValue");
        JSONObject jobj = new JSONObject();
        //Get All Dropdown column Values List
        if (columnList.isEmpty()) {
            Map stateMap = getDefaultStates();
            Map stateToCode = (HashMap) stateMap.get("stateToCode");
            Map codeToState = (HashMap) stateMap.get("codeToState");
            String filePath = "/report/template/EWayValidation.xml";
            try (InputStream is = getClass().getResourceAsStream(filePath);
                    Reader fileReader = new InputStreamReader(is);
                    BufferedReader bufReader = new BufferedReader(fileReader);) {
                StringBuilder sb = new StringBuilder();
                String line = bufReader.readLine();
                while (line != null) {
                    sb.append(line).append("\n");
                    line = bufReader.readLine();
}
                String xml2String = sb.toString();
                jobj = XML.toJSONObject(xml2String);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(CompanyReportConfigurationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                throw ServiceException.FAILURE(ex.getMessage(), ex);
            } catch (IOException ex) {
                Logger.getLogger(CompanyReportConfigurationServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            } 

            
            removeSpecialCharachter = jobj.optJSONObject("report").optBoolean("removeSpecialCharachters");
            JSONArray dataArray = dataObject.optJSONArray("data");
            for (int itr = 0; itr < dataArray.length(); itr++) {
                JSONObject obj = dataArray.getJSONObject(itr);
                Iterator it = validationMap.entrySet().iterator();
                String reason = "";
                String reasonDescription = "";
                Date docDate = null;

                if (moduleid.equals(String.valueOf(Constants.Acc_Delivery_Order_ModuleId))) {
                    docDate = new SimpleDateFormat("dd/MM/yyyy").parse(obj.optString((String) keyValue.get("Delivery Order Date")));
                } else if (moduleid.equals(String.valueOf(Constants.Acc_Goods_Receipt_ModuleId))) {
                    docDate = new SimpleDateFormat("dd/MM/yyyy").parse(obj.optString((String) keyValue.get("Goods Receipt Date")));
                } else if (moduleid.equals(String.valueOf(Constants.Acc_Sales_Return_ModuleId))) {
                    docDate = new SimpleDateFormat("dd/MM/yyyy").parse(obj.optString((String) keyValue.get("Sales Return Date")));
                } else if (moduleid.equals(String.valueOf(Constants.Acc_Purchase_Return_ModuleId))) {
                    docDate = new SimpleDateFormat("dd/MM/yyyy").parse(obj.optString((String) keyValue.get("Purchase Return Date")));
                }

                if (StringUtil.isNullOrEmptyWithTrim(obj.optString((String) keyValue.get("Transporter ID")))) {
                    if (obj.optString((String) keyValue.get("Transport Mode")).equals("Road")) {
                        if (StringUtil.isNullOrEmptyWithTrim(obj.optString((String) keyValue.get("Vehicle No")))) {
                            reason += "Invalid Transport ID";
                            reasonDescription += "Provide valid Transporter ID/GSTIN.";
                        }
                    }
                }
                if (StringUtil.isNullOrEmptyWithTrim(obj.optString((String) keyValue.get("Transporter Doc No")))) {
                    if (obj.optString((String) keyValue.get("Transport Mode")).equals("Rail") || obj.optString((String) keyValue.get("Transport Mode")).equals("Air") || obj.optString((String) keyValue.get("Transport Mode")).equals("Ship")) {
                        if (StringUtil.isNullOrEmptyWithTrim(obj.optString((String) keyValue.get("Transporter Doc No")))) {
                            reason += "Invalid Transporter Doc No ";
                            reasonDescription += "Provide valid Transporter Document Number.";
                        }
                    }
                }
                if (StringUtil.isNullOrEmptyWithTrim(obj.optString((String) keyValue.get("Transportation Date")))) {
                    if (obj.optString((String) keyValue.get("Transport Mode")).equals("Rail") || obj.optString((String) keyValue.get("Transport Mode")).equals("Air") || obj.optString((String) keyValue.get("Transport Mode")).equals("Ship")) {
                        if (StringUtil.isNullOrEmptyWithTrim(obj.optString((String) keyValue.get("Transportation Date")))) {
                            reason += "Invalid Transportation Date ";
                        }
                    }
                }

                if (StringUtil.isNullOrEmptyWithTrim(obj.optString((String) keyValue.get("Vehicle No")))) {
                    if (obj.optString((String) keyValue.get("Transport Mode")).equals("Road")) {
                        if (StringUtil.isNullOrEmptyWithTrim(obj.optString((String) keyValue.get("Transportation Date")))) {
                            reason += "Invalid Vehicle No ";
                            reasonDescription += "Provide valid Vehicle Number without special characters except # / , . &";
                        }
                    }
                }
                if (StringUtil.isNullOrEmptyWithTrim(obj.optString((String) keyValue.get("Vehicle Type"))) || obj.optString((String) keyValue.get("Vehicle Type")).equalsIgnoreCase("None")) {
                    if (obj.optString((String) keyValue.get("Transport Mode")).equals("Road")) {
                        reason += "Invalid Vehicle Type ";
                        reasonDescription += "<br>Provide vehicle Type (Select vehicle type in document level).";
                    }
                }

                if (!StringUtil.isNullOrEmptyWithTrim(obj.optString((String) keyValue.get("GSTIN/UIN")))) {
                    String gstin = obj.optString((String) keyValue.get("GSTIN/UIN"));
                    try {
                        int gstinStateCode = Integer.parseInt(gstin.substring(0, 2));
                        JSONObject paramObj = new JSONObject();
                        paramObj.put("gstinStateCode", gstinStateCode);
                        paramObj.put("country", Constants.indian_country_id);
                        List stateList = accCustomerReportServiceDao.getDefaultStateValue(paramObj);
                        if (moduleid.equals(String.valueOf(Constants.Acc_Sales_Return_ModuleId)) || moduleid.equals(String.valueOf(Constants.Acc_Purchase_Return_ModuleId))) {

                            if (!(stateList != null && stateList.size() > 0 && !StringUtil.isNullOrEmptyWithTrim(obj.optString((String) keyValue.get("Billing State")))
                                    && stateList.get(0).equals(obj.optString((String) keyValue.get("Billing State"))))) {
                                reason += "Invalid GSTIN & Billing Address State Combination";
                                reasonDescription += "<br>Please give valid GSTIN & Billing Address State Combination.";
                            }
                        } else {

                            if (!(stateList != null && stateList.size() > 0 && !StringUtil.isNullOrEmptyWithTrim(obj.optString((String) keyValue.get("Billing Address State")))
                                    && stateList.get(0).equals(obj.optString((String) keyValue.get("Billing Address State"))))) {
                                reason += "Invalid GSTIN & Billing Address State Combination";
                                reasonDescription += "<br>Please give valid GSTIN & Billing Address State Combination.";
                            }
                        }
                    } catch (Exception ex) {
                        reason += "Invalid GSTIN/UIN. ";
                        reasonDescription += "<br>Please give valid GSTIN & Billing Address State Combination.";
                    }
                }

                if (!StringUtil.isNullOrEmptyWithTrim(entityGSTIN)) {
                    try {
                        int gstinStateCode = Integer.parseInt(entityGSTIN.substring(0, 2));
                        JSONObject paramObj = new JSONObject();
                        paramObj.put("gstinStateCode", gstinStateCode);
                        paramObj.put("country", Constants.indian_country_id);
                        List stateList = accCustomerReportServiceDao.getDefaultStateValue(paramObj);
                        if (!(stateList != null && stateList.size() > 0 && !StringUtil.isNullOrEmptyWithTrim(entityState)
                                && stateList.get(0).equals(entityState))) {
                            reasonDescription += "<br>Please give valid GSTIN & State Combination in Entity.";
                            reason += "Invalid GSTIN & State Combination in Entity";
                        }
                    } catch (Exception ex) {
                        reason += "Invalid GSTIN/UIN in Entity ";
                        reasonDescription += "<br>Provide valid GSTIN/UIN alongwith valid State in Entity. ";
                    }
                }

                if (!StringUtil.isNullOrEmptyWithTrim(entityState)) {
                    obj.put("entityStateCode", stateToCode.get(entityState.toUpperCase()));
                }

                Object sInt = null;
                Pattern p = null;
                Matcher m = null;

                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    //                    System.out.println(pair.getKey() + " = " + pair.getValue());
                    String stringValue = "";
                    // boolean b = m.matches();
                    switch ((String) pair.getValue()) {
                        case "Dispatch State":
                            stringValue = obj.optString((String) pair.getKey());
                            if (!StringUtil.isNullOrEmptyWithTrim(stringValue) && !stringValue.equalsIgnoreCase("None")) {
                                obj.put("actualFromStateCode", stateToCode.get(stringValue.toUpperCase()));
                            } else {
                                if (!StringUtil.isNullOrEmptyWithTrim(reason)) {
                                    reason += ", ";
                                }
                                reason += "Invalid " + pair.getValue();
                                reasonDescription += "<br>Provide valid value of Dispatch State";
                            }
                            break;
                        case "Ship to State":
                            stringValue = obj.optString((String) pair.getKey());
                            if (!StringUtil.isNullOrEmptyWithTrim(stringValue) && !stringValue.equalsIgnoreCase("None")) {
                                obj.put("actualToStateCode", stateToCode.get(stringValue.toUpperCase()));
                            } else {
                                if (!StringUtil.isNullOrEmptyWithTrim(reason)) {
                                    reason += ", ";
                                }
                                reason += "Invalid " + pair.getValue();
                                reasonDescription += "<br>Provide valid value of Ship to State.";
                            }
                            break;
                        case "Supply Type":
                            stringValue = obj.optString((String) pair.getKey());
                            if (StringUtil.isNullOrEmptyWithTrim(stringValue) || !(stringValue.equalsIgnoreCase("Inward") || stringValue.equalsIgnoreCase("Outward"))) {
                                if (!StringUtil.isNullOrEmptyWithTrim(reason)) {
                                    reason += ", ";
                                }
                                reason += "Invalid " + pair.getValue();
                                reasonDescription += "<br>Provide valid value of " + pair.getValue() + " (Inward/Outward).";
                            }
                            break;
                        case "Sub Type":
                            stringValue = obj.optString((String) pair.getKey());
                            if (StringUtil.isNullOrEmptyWithTrim(stringValue) || !(getSubSupplyTypeValues().contains(stringValue))) {
                                if (!StringUtil.isNullOrEmptyWithTrim(reason)) {
                                    reason += ", ";
                                }
                                reason += "Invalid " + pair.getValue();
                                reasonDescription += "<br>Provide valid value of " + pair.getValue() + " (Select value from drop down field).";
                            }
                            break;
                        case "Document Type":
                            stringValue = obj.optString((String) pair.getKey());
                            if (StringUtil.isNullOrEmptyWithTrim(stringValue) || !(getDocumentTypeValues().contains(stringValue))) {
                                if (!StringUtil.isNullOrEmptyWithTrim(reason)) {
                                    reason += ", ";
                                    reasonDescription += ", ";
                                }
                                reason += "Invalid " + pair.getValue();
                                reasonDescription += "<br>Provide valid value of Document type (Select value from Drop down field).";
                            }
                            break;
                        case "Customer Name":
                        case "Vendor Name":
                            stringValue = obj.optString((String) pair.getKey());
                            if (removeSpecialCharachter) {
                                stringValue = stringValue.replaceAll("\\W", " ").trim();
                            }
                            obj.put((String) pair.getKey(), stringValue.toUpperCase());
                            String patternString = jobj.optJSONObject("CustomerName").optString("regex");
                            //                            p = Pattern.compile("^[a-zA-Z ]+$");
                            p = Pattern.compile(patternString);
                            if (!StringUtil.isNullOrEmptyWithTrim(stringValue)) {
                                m = p.matcher(stringValue);
                                boolean isMatch = m.find();
                                if (!isMatch) {
                                    if (!StringUtil.isNullOrEmptyWithTrim(reason)) {
                                        reason += ", ";
                                    }
                                    reason += "Invalid " + pair.getValue();
                                    reasonDescription += "<br>Provide valid name of " + pair.getValue() + ", special characters (except , . / # &) not allowed.";
                                }
                            } else {
                                if (!StringUtil.isNullOrEmptyWithTrim(reason)) {
                                    reason += ", ";
                                    reasonDescription += ", ";
                                }
                                reason += "Invalid " + pair.getValue();
                                reasonDescription += "<br>Provide valid name of " + pair.getValue() + ", special characters (except , . / # &) not allowed.";
                            }
                            break;
                        case "GSTIN/UIN":
                        case "Transporter ID":
                            stringValue = obj.optString((String) pair.getKey());
                            if (!StringUtil.isNullOrEmptyWithTrim(stringValue)) {
                                if (!StringUtil.isGSTINValid(stringValue)) {
                                    if (!StringUtil.isNullOrEmptyWithTrim(reason)) {
                                        reason += ", ";
                                    }
                                    reason += "Invalid " + pair.getValue();
                                    if (pair.getValue().equals("Transporter ID")) {
                                        reasonDescription += "<br>Provide valid Transporter ID/GSTIN.";
                                    } else if (pair.getValue().equals("GSTIN/UIN")) {
                                        if (moduleid.equals(Constants.Acc_Sales_Return_ModuleId) || moduleid.equals(String.valueOf(Constants.Acc_Delivery_Order_ModuleId))) {
                                            reasonDescription += "<br>Provide valid Customer GSTIN.";
                                        } else if (moduleid.equals(Constants.Acc_Goods_Receipt_ModuleId) || moduleid.equals(String.valueOf(Constants.Acc_Purchase_Return_ModuleId))) {
                                            reasonDescription += "<br>Provide valid Vendor GSTIN.";
                                        }
                                    }
                                }
                            }
                            break;
                        case "Goods Receipt Number":
                        case "Delivery Order Number":
                        case "Delivery Order Date":
                        case "Purchase Return Date":
                        case "Sales Return Date":
                        case "Goods Receipt Date":
                        case "Billing Address State":
                        case "Billing State":
                            stringValue = obj.optString((String) pair.getKey());
                            obj.put((String) pair.getKey(), obj.optString((String) pair.getKey()).toUpperCase());
                            if (StringUtil.isNullOrEmptyWithTrim(stringValue)) {
                                if (!StringUtil.isNullOrEmptyWithTrim(reason)) {
                                    reason += ", ";
                                }
                                reason += "Invalid " + pair.getValue();
                                reasonDescription += "<br>Please give valid " + pair.getValue() + ".";
                            } else {
                                if (pair.getValue().equals("Billing Address State") || pair.getValue().equals("Billing State")) {
                                    obj.put("billingAddressStateCode", stateToCode.get(stringValue.toUpperCase()));
                                }
                                obj.put((String) pair.getKey(), stringValue.toUpperCase());
                            }
                            break;
                        case "Billing Address Postal Code":
                        case "Billing Postal Code":
                            sInt = obj.optInt((String) pair.getKey());
                            int postalCodeLength = jobj.optJSONObject("Pincode").optInt("length");
                            if ((sInt != null) && (!(sInt instanceof Integer) || sInt.toString().length() != postalCodeLength)) {
                                if (!StringUtil.isNullOrEmptyWithTrim(reason)) {
                                    reason += ", ";
                                }
                                reason += "Invalid " + pair.getValue();
                                reasonDescription += "<br>Provide 6 digit numeric value for " + pair.getValue();
                                if (moduleid.equals(Constants.Acc_Sales_Return_ModuleId) || moduleid.equals(String.valueOf(Constants.Acc_Purchase_Return_ModuleId))) {
                                    reasonDescription += " (Provide value at Master Level).";
                                } else {
                                    reasonDescription += " (Provide value at Document Level).";
                                }
                            }
                            break;
                        case "HSN/SAC Code":
                            sInt = obj.optInt((String) pair.getKey());
                            stringValue = obj.optString((String) pair.getKey());
                            int HSNLength = jobj.optJSONObject("HSNSAC").optInt("length");
                            p = Pattern.compile("^[0-9]+$");
                            m = p.matcher(stringValue);
                            boolean isMatch = m.find();
                            if ((sInt != null) && (!(sInt instanceof Integer) && stringValue.toString().length() != HSNLength) || !isMatch) {
                                if (!StringUtil.isNullOrEmptyWithTrim(reason)) {
                                    reason += ", ";
                                }
                                reason += "Invalid  " + pair.getValue();
                                reasonDescription += "<br>Provide valid HSN/SAC code (only 8 digit numeric value allowed).";
                            }
                            break;
                        case "E-Way Unit":
                            stringValue = obj.optString((String) pair.getKey());
                            //                            obj.put((String) pair.getKey(), obj.optString((String) pair.getKey()).toUpperCase());
                            if (StringUtil.isNullOrEmptyWithTrim(stringValue) || !(eWayUnits.contains(stringValue.toUpperCase()))) {
                                if (!StringUtil.isNullOrEmptyWithTrim(reason)) {
                                    reason += ", ";
                                }
                                reason += "Invalid " + pair.getValue();
                                reasonDescription += "<br>Please give valid " + pair.getValue() + ".";
                            }
                            break;
                        case "Delivered Quantity":
                        case "Received Quantity":
                        case "Return Quantity":
                            break;
                        case "Amount":
                            break;
                        case "E Way Tax Rate":
                            break;
                        case "SGST Type":
                            break;
                        case "CGST Amount":
                            break;
                        case "IGST Amount":
                            break;
                        case "UTGST Amount":
                            break;
                        case "CESS Amount":
                            break;
                        case "Transport Mode":
                            stringValue = obj.optString((String) pair.getKey());
                            if (StringUtil.isNullOrEmptyWithTrim(stringValue) || !(getTransportationModeValues().contains(stringValue))) {
                                if (!StringUtil.isNullOrEmptyWithTrim(reason)) {
                                    reason += ", ";
                                }
                                reason += "Invalid " + pair.getValue();
                                reasonDescription += "<br>Provide valid value of " + pair.getValue() + ", choose from (Rail/Ship/Road/Air).";
                            }
                            break;
                        case "Distance level (Km)":
                            sInt = obj.optInt((String) pair.getKey());
                            if ((sInt != null) && !((sInt instanceof Double) || (sInt instanceof Integer))) {
                                if (!StringUtil.isNullOrEmptyWithTrim(reason)) {
                                    reason += ", ";
                                }
                                reason += "Invalid " + pair.getValue();
                                reasonDescription += "<br>Provide valid value of " + pair.getValue() + " (only numbers allowed).";
                            }
                            break;
                        case "Transporter Doc No":
                            break;
                        case "Transportation Date":
                            if (!StringUtil.isNullOrEmptyWithTrim(obj.optString((String) keyValue.get("Transportation Date")))) {
                                Date transDate = new SimpleDateFormat("dd/MM/yyyy").parse(obj.optString((String) keyValue.get("Transportation Date")));
                                if (transDate.before(docDate)) {
                                    if (!StringUtil.isNullOrEmptyWithTrim(reason)) {
                                        reason += ", ";
                                    }
                                    reason += "Invalid " + pair.getValue();
                                    reasonDescription += "<br>Provide valid " + pair.getValue() + " , Transportation Date should be equal to or greater than the document date.";
                                }
                            }
                            break;
                        case "Product Name":
                        case "Product Description":
                        case "Billing Address City":
                        case "Billing City":
                        case "Billing Address":
                        case "Transporter Name":
                            stringValue = obj.optString((String) pair.getKey());
                            stringValue = stringValue.toUpperCase();
                            obj.put((String) pair.getKey(), stringValue);
                            if (removeSpecialCharachter) {
                                stringValue = stringValue.replaceAll("\\W", " ");
                                stringValue = stringValue.replaceAll("_", " ");
                            }

                            p = Pattern.compile("^[\\-\\#\\/\\,\\&a-zA-Z0-9\n ]+$");
                            if (!StringUtil.isNullOrEmptyWithTrim(stringValue)) {
                                m = p.matcher(stringValue);
                                isMatch = m.find();
                                if (!isMatch) {
                                    if (!StringUtil.isNullOrEmptyWithTrim(reason)) {
                                        reason += ", ";
                                    }
                                    obj.put((String) pair.getKey(), stringValue.trim());
                                    reason += "Invalid " + pair.getValue();
                                    reasonDescription += "<br>Provide valid " + pair.getValue() + " without special characters except # / , . &";
                                } else {
                                    stringValue = stringValue.replaceAll("\n", "\r\n");
                                    obj.put((String) pair.getKey(), stringValue.trim());
                                }
                            }
                            break;
                        case "Vehicle No":
                            stringValue = obj.optString((String) pair.getKey());
                            p = Pattern.compile("^[a-zA-z0-9]+$");
                            if (!StringUtil.isNullOrEmptyWithTrim(stringValue)) {
                                m = p.matcher(stringValue);
                                isMatch = m.find();
                                if (!isMatch) {
                                    if (!StringUtil.isNullOrEmptyWithTrim(reason)) {
                                        reason += ", ";
                                    }
                                    reason += "Invalid " + pair.getValue();
                                    reasonDescription += "<br>Provide valid " + pair.getValue() + " without special characters except -";
                                }
                            }
                            break;

                    }
//                     it.remove(); // avoids a ConcurrentModificationException
                }
                if (StringUtil.isNullOrEmptyWithTrim(reason)) {
                    validRecordsCount++;
                }
                obj.put("reason", reason);
                if (StringUtil.isNullOrEmptyWithTrim(reasonDescription)) {
                    obj.put("reasonDescription", "Selected Record is Valid");
                } else {
                    reasonDescription = reasonDescription.replaceFirst("<br>", "");
                    if (exportEWayInvalidRecords) {
                        obj.put("reasonDescription", reasonDescription.replaceAll("<br>", "\n"));
                    } else {
                        obj.put("reasonDescription", reasonDescription);
                    }
                }
                obj.put("entityState", !StringUtil.isNullOrEmpty(entityState) ? entityState.toUpperCase() : "");
                obj.put("entityCity", !StringUtil.isNullOrEmpty(entityCity) ? entityCity.toUpperCase() : "");
                obj.put("entityPinCode", entityPinCode);
                obj.put("entityAddress", !StringUtil.isNullOrEmpty(entityAddress) ? entityAddress.toUpperCase() : "");
                obj.put("entityGSTIN", entityGSTIN);
                obj.put("companyName", !StringUtil.isNullOrEmpty(companyName) ? companyName.toUpperCase() : "");
                if (StringUtil.isNullOrEmptyWithTrim(obj.optString("reason"))) {
                    validRecords.put(obj);
                } else {
                    invalidRecords.put(obj);
                    invalidDocuments.add(obj.optString("billid"));
                }
            }
 
            dataObject.put("validRecordsCount", validRecordsCount);
            dataObject.put("invalidRecordsCount", dataArray.length() - validRecordsCount);
            dataObject.put("validationTotalCount", dataArray.length());
            separatedRecords.put("validRecords", validRecords);
            separatedRecords.put("invalidRecords", invalidRecords);
            separatedRecords.put("invalidDocuments", invalidDocuments);
            returnMap.put("separatedRecords", separatedRecords);
            dataObject.put("success", true);
            dataObject.put("msg", "");
            if (exportEWayInvalidRecords || exportValidRecordsToJSON) {
                returnMap.put("keyValue", keyValue);
            }
            if (isWholeData) {
                JSONArray pagedArray = StringUtil.getPagedJSON(dataArray, 0, 25);
                dataObject.put("data", pagedArray);
            }
            returnMap.put("dataObject", dataObject);
            returnMap.put("removeSpecialCharachter",removeSpecialCharachter);
            return returnMap;
        } else {
            dataObject.put("success", false);
            dataObject.put("msg", "Below column(s) is missing in this report and it is mandatory column for generating JSON, <br>Please go back and add these columns in the report. <br>" + columnList.toString());
            returnMap.put("dataObject", dataObject);
            return returnMap;
        }
    }

    public List getMandatoryReportColumn(String moduleid) {
        List columnList = new ArrayList();
        if (moduleid.equals(String.valueOf(Constants.Acc_Delivery_Order_ModuleId))) {
            columnList.add("Delivery Order Number");
            columnList.add("Delivery Order Date");
            columnList.add("Delivered Quantity");
            columnList.add("Customer Name");
            columnList.add("Billing Address");
            columnList.add("Billing Address City");
            columnList.add("Billing Address Postal Code");
            columnList.add("Billing Address State");
        } else if (moduleid.equals(String.valueOf(Constants.Acc_Goods_Receipt_ModuleId))) {
            columnList.add("Goods Receipt Number");
            columnList.add("Goods Receipt Date");
            columnList.add("Received Quantity");
            columnList.add("Vendor Name");
            columnList.add("Billing Address");
            columnList.add("Billing Address City");
            columnList.add("Billing Address Postal Code");
            columnList.add("Billing Address State");
        } else if (moduleid.equals(String.valueOf(Constants.Acc_Sales_Return_ModuleId))) {
            columnList.add("Sales Return Number");
            columnList.add("Sales Return Date");
            columnList.add("Return Quantity");
            columnList.add("Customer Name");
            columnList.add("Billing Address");
            columnList.add("Billing City");
            columnList.add("Billing Postal Code");
            columnList.add("Billing State");
        } else if (moduleid.equals(String.valueOf(Constants.Acc_Purchase_Return_ModuleId))) {
            columnList.add("Purchase Return Number");
            columnList.add("Purchase Return Date");
            columnList.add("Return Quantity");
            columnList.add("Vendor Name");
            columnList.add("Billing Address");
            columnList.add("Billing City");
            columnList.add("Billing Postal Code");
            columnList.add("Billing State");
        }
        columnList.add("Supply Type");
        columnList.add("Entity");
        columnList.add("Sub Type");
        columnList.add("Document Type");
        columnList.add("GSTIN/UIN");
        columnList.add("Product Name");
        columnList.add("Product Description");
        columnList.add("HSN/SAC Code");
        columnList.add("E-Way Unit");
        columnList.add("Amount");
        columnList.add("Total Amount");
        columnList.add("E Way Tax Rate");
        columnList.add("Ship to State");
        columnList.add("Dispatch State");
        columnList.add("SGST Amount");
        columnList.add("CGST Amount");
        columnList.add("UTGST Amount");
        columnList.add("IGST Amount");
        columnList.add("CESS Amount");
        columnList.add("Transport Mode");
        columnList.add("Distance level (Km)");
        columnList.add("Transporter Name");
        columnList.add("Transporter ID");
        columnList.add("Transporter Doc No");
        columnList.add("Transportation Date");
        columnList.add("Vehicle No");
        columnList.add("Vehicle Type");

        return columnList;
    }

    public Map checkIfReportHasAllColumns(List columnList, JSONArray columns) throws JSONException {
        Map<String, Map> returnMap = new HashMap<String, Map>();
        Map validationMap = new HashMap();
        Map keyValue = new HashMap();
        for (int itr = 0; itr < columns.length(); itr++) {
            if (columnList.contains(columns.getJSONObject(itr).optString("defaultHeader"))) {
                if (columns.getJSONObject(itr).optString("defaultHeader").equals("Delivery Order Date") || columns.getJSONObject(itr).optString("defaultHeader").equals("Transportation Date")) {
                    validationMap.put(columns.getJSONObject(itr).optString("defaultHeader"), columns.getJSONObject(itr).optString("dataIndex"));
                }
                validationMap.put(columns.getJSONObject(itr).optString("dataIndex"), columns.getJSONObject(itr).optString("defaultHeader"));
                keyValue.put(columns.getJSONObject(itr).optString("defaultHeader"), columns.getJSONObject(itr).optString("dataIndex"));
                columnList.remove(columns.getJSONObject(itr).optString("defaultHeader"));
            }
        }
        returnMap.put("validationMap", validationMap);
        returnMap.put("keyValue", keyValue);
        return returnMap;
    }

    private List getSubSupplyTypeValues() {
        List subSupplyTypeValues = new ArrayList();
        subSupplyTypeValues.add("Supply");
        subSupplyTypeValues.add("Import");
        subSupplyTypeValues.add("Export");
        subSupplyTypeValues.add("Job Work");
        subSupplyTypeValues.add("For Own Use");
        subSupplyTypeValues.add("Job work Returns");
        subSupplyTypeValues.add("Sales Return");
        subSupplyTypeValues.add("Others");
        subSupplyTypeValues.add("SKD/CKD");
        subSupplyTypeValues.add("Line Sales");
        subSupplyTypeValues.add("Recipient  Not Known");
        subSupplyTypeValues.add("Exhibition or Fairs");
        return subSupplyTypeValues;
    }

    private List getDocumentTypeValues() {
        List documentTypeValues = new ArrayList();
        documentTypeValues.add("Tax Invoice");
        documentTypeValues.add("Bill of Supply");
        documentTypeValues.add("Bill of Entry");
        documentTypeValues.add("Delivery Challan");
        documentTypeValues.add("Credit Note");
        documentTypeValues.add("Others");
        return documentTypeValues;
    }

    private List getTransportationModeValues() {
        List transportationModeValues = new ArrayList();
        transportationModeValues.add("Road");
        transportationModeValues.add("Rail");
        transportationModeValues.add("Air");
        transportationModeValues.add("Ship");
        return transportationModeValues;
    }

    private List getUnitValues() {
        List unitValues = new ArrayList();
        unitValues.add("BAGS");
        unitValues.add("BUNDLES");
        unitValues.add("BOX");
        unitValues.add("CENTI METERS");
        unitValues.add("DOZENS");
        unitValues.add("GRAMS");
        unitValues.add("KILOGRAMS");
        unitValues.add("KILOMETRE");
        unitValues.add("KILOLITRE");
        unitValues.add("MILILITRE");
        unitValues.add("METERS");
        unitValues.add("NUMBERS");
        unitValues.add("PAIRS");
        unitValues.add("OTHERS");
        unitValues.add("TONNES");
        unitValues.add("SQUARE FEET");
        unitValues.add("CUBIC METERS");
        unitValues.add("SQUARE METERS");
        unitValues.add("BALE");
        unitValues.add("BUCKLES");
        unitValues.add("BILLION OF UNITS");
        unitValues.add("BOTTLES");
        unitValues.add("BUNCHES");
        unitValues.add("CANS");
        unitValues.add("CUBIC CENTIMETERS");
        unitValues.add("CARTONS");
        unitValues.add("METRIC TON");
        unitValues.add("DRUMS");
        unitValues.add("GREAT GROSS");
        unitValues.add("GRAMMES");
        unitValues.add("GROSS");
        unitValues.add("GROSS YARDS");
        unitValues.add("PACKS");
        unitValues.add("PIECES");
        unitValues.add("QUINTAL");
        unitValues.add("ROLLS");
        unitValues.add("SETS");
        unitValues.add("SQUARE YARDS");
        unitValues.add("TABLETS");
        unitValues.add("TEN GROSS");
        unitValues.add("THOUSANDS");
        unitValues.add("TUBES");
        unitValues.add("US GALLONS");
        unitValues.add("UNITS");
        unitValues.add("YARDS");
        return unitValues;
    }

//    @Override
//    public Map separateValidInvalidEWayRecords(JSONObject resultJSON) throws JSONException {
//        JSONArray dataArray = resultJSON.optJSONArray("data");
//        Map returnObject = new HashMap();
//        JSONArray validRecords = new JSONArray();
//        JSONArray invalidRecords = new JSONArray();
//        Set invalidDocuments = new HashSet();
//        for (int itr = 0; itr < dataArray.length(); itr++) {
//            JSONObject obj = dataArray.optJSONObject(itr);
//            if (StringUtil.isNullOrEmptyWithTrim(obj.optString("reason"))) {
//                validRecords.put(obj);
//            } else {
//                invalidRecords.put(obj);
//                invalidDocuments.add(obj.optString("billid"));
//            }
//        }
//        returnObject.put("validRecords", validRecords);
//        returnObject.put("invalidRecords", invalidRecords);
//        returnObject.put("invalidDocuments", invalidDocuments);
//        return returnObject;
//    }

    @Override
     public JSONObject getJSONtoExport(Map separatedRecords) throws JSONException, ServiceException {
        JSONObject jsonToExport = new JSONObject();
        Set invalidDocuments = (HashSet) separatedRecords.get("invalidDocuments");
        String moduleId = (String) separatedRecords.get("moduleId");
        Map keyValue = (HashMap) separatedRecords.get("keyValue");
        keyValue.put("moduleId", moduleId);
        boolean removeSpecialCharachter = separatedRecords.get("removeSpecialCharachter") != null ? (Boolean) separatedRecords.get("removeSpecialCharachter") : true;
        JSONArray validRecords = (JSONArray) separatedRecords.get("validRecords");
        JSONArray billLists = new JSONArray();
        Map<String, EWayRecord> eWayRecordsMap = new HashMap<String, EWayRecord>();
        jsonToExport.put("version", "1.0.0618");
        Map eWayUnitcodesMap = getEWayUnitCodeMap();
        for (int itr = 0; validRecords.length() > itr; itr++) {
            JSONObject obj = validRecords.getJSONObject(itr);
            if (!invalidDocuments.contains(obj.opt("billid"))) {
                if (!eWayRecordsMap.containsKey(obj.opt("billid"))) {
                    EWayRecord record = new EWayRecord();

                    String companyName = obj.optString("companyName");
                    companyName = companyName.replace("Workspace - Accounting Custom Report", " ");
                    companyName = companyName.replace("WORKSPACE - ACCOUNTING CUSTOM REPORT", " ");
                    if (removeSpecialCharachter) {
                        companyName = companyName.replaceAll("\\W", " ");
                    }
                    companyName = companyName.trim();

                    if (moduleId.equals(String.valueOf(Constants.Acc_Delivery_Order_ModuleId))) {
                        record.setDocNo(obj.optString((String) keyValue.get("Delivery Order Number")));
                        record.setDocDate(obj.optString((String) keyValue.get("Delivery Order Date")));
                        record.setToTrdName(obj.optString((String) keyValue.get("Customer Name")));
                        record.setFromTrdName(companyName);
                    } else if (moduleId.equals(String.valueOf(Constants.Acc_Purchase_Return_ModuleId))) {
                        record.setDocNo(obj.optString((String) keyValue.get("Purchase Return Number")));
                        record.setDocDate(obj.optString((String) keyValue.get("Purchase Return Date")));
                        record.setToTrdName(obj.optString((String) keyValue.get("Vendor Name")));
                        record.setFromTrdName(companyName);
                    } else if (moduleId.equals(String.valueOf(Constants.Acc_Sales_Return_ModuleId))) {
                        record.setDocNo(obj.optString((String) keyValue.get("Sales Return Number")));
                        record.setDocDate(obj.optString((String) keyValue.get("Sales Return Date")));
                        record.setToTrdName(companyName);
                        record.setFromTrdName(obj.optString((String) keyValue.get("Customer Name")));
                    } else if (moduleId.equals(String.valueOf(Constants.Acc_Goods_Receipt_ModuleId))) {
                        record.setDocNo(obj.optString((String) keyValue.get("Goods Receipt Number")));
                        record.setDocDate(obj.optString((String) keyValue.get("Goods Receipt Date")));
                        record.setToTrdName(companyName);
                        record.setFromTrdName(obj.optString((String) keyValue.get("Vendor Name")));
                    }

                    if (obj.optString((String) keyValue.get("Supply Type")).equalsIgnoreCase("Outward")) {
                        record.setSupplyType("O");
                    } else if (obj.optString((String) keyValue.get("Supply Type")).equalsIgnoreCase("Inward")) {
                        record.setSupplyType("I");
                    }

                    if (obj.optString((String) keyValue.get("Sub Type")).equalsIgnoreCase("Supply")) {
                        record.setSubSupplyType(1);
                    } else if (obj.optString((String) keyValue.get("Sub Type")).equalsIgnoreCase("Import")) {
                        record.setSubSupplyType(2);
                    } else if (obj.optString((String) keyValue.get("Sub Type")).equalsIgnoreCase("Export")) {
                        record.setSubSupplyType(3);
                    } else if (obj.optString((String) keyValue.get("Sub Type")).equalsIgnoreCase("Job Work")) {
                        record.setSubSupplyType(4);
                    } else if (obj.optString((String) keyValue.get("Sub Type")).equalsIgnoreCase("For Own Use")) {
                        record.setSubSupplyType(5);
                    } else if (obj.optString((String) keyValue.get("Sub Type")).equalsIgnoreCase("Job work Returns")) {
                        record.setSubSupplyType(6);
                    } else if (obj.optString((String) keyValue.get("Sub Type")).equalsIgnoreCase("Sales Return")) {
                        record.setSubSupplyType(7);
                    } else if (obj.optString((String) keyValue.get("Sub Type")).equalsIgnoreCase("Others")) {
                        record.setSubSupplyType(8);
                    } else if (obj.optString((String) keyValue.get("Sub Type")).equalsIgnoreCase("SKD/CKD")) {
                        record.setSubSupplyType(9);
                    } else if (obj.optString((String) keyValue.get("Sub Type")).equalsIgnoreCase("Line Sales")) {
                        record.setSubSupplyType(10);
                    } else if (obj.optString((String) keyValue.get("Sub Type")).equalsIgnoreCase("Recipient Not Known")) {
                        record.setSubSupplyType(11);
                    } else if (obj.optString((String) keyValue.get("Sub Type")).equalsIgnoreCase("Exhibition or Fairs")) {
                        record.setSubSupplyType(12);
                    }

                    if (obj.optString((String) keyValue.get("Document Type")).equalsIgnoreCase("Tax Invoice")) {
                        record.setDocType("INV");
                    } else if (obj.optString((String) keyValue.get("Document Type")).equalsIgnoreCase("Bill of Supply")) {
                        record.setDocType("BIL");
                    } else if (obj.optString((String) keyValue.get("Document Type")).equalsIgnoreCase("Bill of Entry")) {
                        record.setDocType("BOE");
                    } else if (obj.optString((String) keyValue.get("Document Type")).equalsIgnoreCase("Delivery Challan")) {
                        record.setDocType("CHL");
                    } else if (obj.optString((String) keyValue.get("Document Type")).equalsIgnoreCase("Credit Note")) {
                        record.setDocType("CNT");
                    } else if (obj.optString((String) keyValue.get("Document Type")).equalsIgnoreCase("Others")) {
                        record.setDocType("OTH");
                    }

                    if (obj.optString((String) keyValue.get("Vehicle Type")).equalsIgnoreCase("Regular")) {
                        record.setVehicleType("R");
                    } else if (obj.optString((String) keyValue.get("Vehicle Type")).equalsIgnoreCase("ODC")) {
                        record.setVehicleType("O");
                    } else {
                        record.setVehicleType("");
                    }

                    if (obj.optString((String) keyValue.get("Transport Mode")).equalsIgnoreCase("Road")) {
                        record.setTransMode(1);
                    } else if (obj.optString((String) keyValue.get("Transport Mode")).equalsIgnoreCase("Rail")) {
                        record.setTransMode(2);
                    } else if (obj.optString((String) keyValue.get("Transport Mode")).equalsIgnoreCase("Air")) {
                        record.setTransMode(3);
                    } else if (obj.optString((String) keyValue.get("Transport Mode")).equalsIgnoreCase("Ship")) {
                        record.setTransMode(4);
                    }
                    
                    String entityCity = obj.optString("entityCity");
                    if (!StringUtil.isNullOrEmptyWithTrim(obj.optString("entityCity")) && removeSpecialCharachter) {
                        entityCity = entityCity.replace("\\W", " ");
                    }
                    String entityAddress = obj.optString("entityAddress");
                    if (!StringUtil.isNullOrEmptyWithTrim(obj.optString("entityAddress")) && removeSpecialCharachter) {
                        entityAddress = entityAddress.replace("\\W", " ");
                    }

                    record.setActualFromStateCode(obj.optInt("actualFromStateCode"));
                    record.setActualToStateCode(obj.optInt("actualToStateCode"));
                    if (moduleId.equals(String.valueOf(Constants.Acc_Delivery_Order_ModuleId)) || moduleId.equals(String.valueOf(Constants.Acc_Purchase_Return_ModuleId))) {
                        record.setFromGstin(obj.optString("entityGSTIN"));
                        record.setUserGstin(obj.optString("entityGSTIN"));
                        record.setFromPincode(obj.optInt("entityPinCode"));
                        record.setFromAddr1(entityAddress);
                        record.setFromAddr2("");
                        record.setFromPlace(entityCity);
                        record.setFromStateCode(obj.optInt("entityStateCode"));
                        if (moduleId.equals(String.valueOf(Constants.Acc_Delivery_Order_ModuleId))) {
                            record.setToPlace(obj.optString((String) keyValue.get("Billing Address City")));
                            record.setToPincode(obj.optInt((String) keyValue.get("Billing Address Postal Code")));
                        } else {
                            record.setToPlace(obj.optString((String) keyValue.get("Billing City")));
                            record.setToPincode(obj.optInt((String) keyValue.get("Billing Postal Code")));
                        }

                        record.setToGstin(obj.optString((String) keyValue.get("GSTIN/UIN")));
                        record.setToAddr1(obj.optString((String) keyValue.get("Billing Address")));
                        record.setToAddr2("");
                        record.setToStateCode(obj.optInt("billingAddressStateCode"));
                    } else if (moduleId.equals(String.valueOf(Constants.Acc_Sales_Return_ModuleId)) || moduleId.equals(String.valueOf(Constants.Acc_Goods_Receipt_ModuleId))) {
                        record.setToGstin(obj.optString("entityGSTIN"));
                        record.setUserGstin(obj.optString("entityGSTIN"));
                        record.setToPincode(obj.optInt("entityPinCode"));
                        record.setToAddr1(entityAddress);
                        record.setToAddr2("");
                        record.setToPlace(entityCity);
                        record.setToStateCode(obj.optInt("entityStateCode"));
                        if (moduleId.equals(String.valueOf(Constants.Acc_Sales_Return_ModuleId))) {
                            record.setFromPincode(obj.optInt((String) keyValue.get("Billing Postal Code")));
                            record.setFromPlace(obj.optString((String) keyValue.get("Billing City")));
                        }else if(moduleId.equals(String.valueOf(Constants.Acc_Goods_Receipt_ModuleId))){
                            record.setFromPincode(obj.optInt((String) keyValue.get("Billing Address Postal Code")));
                            record.setFromPlace(obj.optString((String) keyValue.get("Billing Address City")));
                        }
                        record.setFromAddr1(obj.optString((String) keyValue.get("Billing Address")));
                        record.setFromGstin(obj.optString((String) keyValue.get("GSTIN/UIN")));
                        record.setFromAddr2("");
                        record.setFromStateCode(obj.optInt("billingAddressStateCode"));
                    }

                    record.setTransporterName(obj.optString((String) keyValue.get("Transporter Name")));
                    record.setTransporterId(obj.optString((String) keyValue.get("Transporter ID")));
                    record.setTransDocNo(obj.optString((String) keyValue.get("Transporter Doc No")));
                    record.setTransDocDate(obj.optString((String) keyValue.get("Transportation Date")));
                    record.setVehicleNo(obj.optString((String) keyValue.get("Vehicle No")));
                    record.setTransDistance(obj.optInt((String) keyValue.get("Distance level (Km)")));
                    EWayRecordDetails recordDetail = setEWayRecordDetails(obj, keyValue, eWayUnitcodesMap);
                    record.setMainHsnCode(String.valueOf(recordDetail.getHsnCode()));
                    if (obj.optDouble((String) keyValue.get("SGST Amount")) > 0) {
                        record.setSgstValue(obj.optDouble((String) keyValue.get("SGST Amount")));
                    } else {
                        record.setSgstValue(obj.optDouble((String) keyValue.get("UTGST Amount")));
                    }
                    record.setCessValue(obj.optDouble((String) keyValue.get("CESS Amount")));
                    record.setIgstValue(obj.optDouble((String) keyValue.get("IGST Amount")));
                    record.setCgstValue(obj.optDouble((String) keyValue.get("CGST Amount")));
                    record.setTotalValue(obj.optDouble((String) keyValue.get("Amount")));
                    record.setTotInvValue(obj.optDouble((String) keyValue.get("Total Amount")));
                    List<EWayRecordDetails> recordList = new LinkedList();
                    recordDetail.setItemNo(1);
                    recordList.add(recordDetail);
                    record.setItemList(recordList);
                    eWayRecordsMap.put(obj.optString("billid"), record);
                } else {
                    EWayRecord record = eWayRecordsMap.get(obj.opt("billid"));
                    List<EWayRecordDetails> itemList = record.getItemList();
                    EWayRecordDetails recordDetail = setEWayRecordDetails(obj, keyValue, eWayUnitcodesMap);
                    if (obj.optDouble((String) keyValue.get("SGST Amount")) > 0) {
                        record.setSgstValue(record.getSgstValue() + obj.optDouble((String) keyValue.get("SGST Amount")));
                    } else {
                        record.setSgstValue(record.getSgstValue() + obj.optDouble((String) keyValue.get("UTGST Amount")));
                    }
                    record.setCessValue(record.getCessValue() + obj.optDouble((String) keyValue.get("CESS Amount")));
                    record.setIgstValue(record.getIgstValue() + obj.optDouble((String) keyValue.get("IGST Amount")));
                    record.setCgstValue(record.getCgstValue() + obj.optDouble((String) keyValue.get("CGST Amount")));
                    record.setTotalValue(record.getTotalValue() + obj.optDouble((String) keyValue.get("Amount")));
                    recordDetail.setItemNo(itemList.size() + 1);
                    itemList.add(recordDetail);
                    record.setItemList(itemList);
                    eWayRecordsMap.put(obj.optString("billid"), record);
                }

            }

        }
        List updateDocumentList = new LinkedList();
        for (String key : eWayRecordsMap.keySet()) {
            billLists.put(new JSONObject(eWayRecordsMap.get(key).toString()));
            updateDocumentList.add(key);
        }
        Map requestParam = new HashMap();
        requestParam.put("setStatus", "'T'");
        requestParam.put("tableName", getMainTableForModule(moduleId));
        int i = accCustomerReportServiceDao.updateEwayJSONExportFlag(updateDocumentList, requestParam);

        jsonToExport.put("billLists", billLists);
        return jsonToExport;
    }

    public EWayRecordDetails setEWayRecordDetails(JSONObject obj, Map keyValue, Map eWayUnitcodesMap) {
        String moduleId = keyValue.get("moduleId") != null ? (String) keyValue.get("moduleId") : " ";
        EWayRecordDetails recordDetail = new EWayRecordDetails();
        if (moduleId.equals(String.valueOf(Constants.Acc_Delivery_Order_ModuleId))) {
            recordDetail.setQuantity(obj.optInt((String) keyValue.get("Delivered Quantity")));
        } else if (moduleId.equals(String.valueOf(Constants.Acc_Purchase_Return_ModuleId))) {
            recordDetail.setQuantity(obj.optInt((String) keyValue.get("Return Quantity")));
        } else if (moduleId.equals(String.valueOf(Constants.Acc_Sales_Return_ModuleId))) {
            recordDetail.setQuantity(obj.optInt((String) keyValue.get("Return Quantity")));
        } else if (moduleId.equals(String.valueOf(Constants.Acc_Goods_Receipt_ModuleId))) {
            recordDetail.setQuantity(obj.optInt((String) keyValue.get("Received Quantity")));
        }
        recordDetail.setProductName(obj.optString((String) keyValue.get("Product Name")));
        recordDetail.setProductDesc(obj.optString((String) keyValue.get("Product Description")));
        recordDetail.setHsnCode(obj.optInt((String) keyValue.get("HSN/SAC Code")));
        String eWayUnitCode = (String) eWayUnitcodesMap.get(obj.optString(((String) keyValue.get("E-Way Unit"))).toUpperCase());
        recordDetail.setQtyUnit(eWayUnitCode);
        recordDetail.setTaxableAmount(obj.optInt((String) keyValue.get("Amount")));
        String[] taxRateStrings = obj.optString((String) keyValue.get("E Way Tax Rate")).split("\\+");
        recordDetail.setSgstRate(Double.parseDouble(taxRateStrings[0]));
        recordDetail.setCgstRate(Double.parseDouble(taxRateStrings[1]));
        recordDetail.setIgstRate(Double.parseDouble(taxRateStrings[2]));
        recordDetail.setCessRate(Double.parseDouble(taxRateStrings[3]));
        return recordDetail;
    }
    
    private Map getEWayUnitCodeMap() {
        Map unitCodeMapping = new HashMap();
        unitCodeMapping.put("BAGS", "BAG");
        unitCodeMapping.put("BALE", "BAL");
        unitCodeMapping.put("BILLION OF UNITS", "BOU");
        unitCodeMapping.put("BOTTLES", "BTL");
        unitCodeMapping.put("BOX", "BOX");
        unitCodeMapping.put("BUCKLES", "BKL");
        unitCodeMapping.put("BUNCHES", "BUN");
        unitCodeMapping.put("BUNDLES", "BDL");
        unitCodeMapping.put("CANS", "CAN");
        unitCodeMapping.put("CARTONS", "CTN");
        unitCodeMapping.put("CENTIMETERS", "CMS");
        unitCodeMapping.put("CUBIC CENTIMETERS", "CCM");
        unitCodeMapping.put("CUBIC METERS", "CBM");
        unitCodeMapping.put("DOZENS", "DOZ");
        unitCodeMapping.put("DRUMS", "DRM");
        unitCodeMapping.put("GRAMMES", "GMS");
        unitCodeMapping.put("GREAT GROSS", "GGK");
        unitCodeMapping.put("GROSS", "GRS");
        unitCodeMapping.put("GROSS YARDS", "GYD");
        unitCodeMapping.put("KILOGRAMS", "KGS");
        unitCodeMapping.put("KILOLITRE", "KLR");
        unitCodeMapping.put("KILOMETRE", "KME");
        unitCodeMapping.put("METERS", "MTR");
        unitCodeMapping.put("METRIC TON", "MTS");
        unitCodeMapping.put("MILILITRE", "MLT");
        unitCodeMapping.put("NUMBERS", "NOS");
        unitCodeMapping.put("OTHERS", "OTH");
        unitCodeMapping.put("PACKS", "PAC");
        unitCodeMapping.put("PAIRS", "PRS");
        unitCodeMapping.put("PIECES", "PCS");
        unitCodeMapping.put("QUINTAL", "QTL");
        unitCodeMapping.put("ROLLS", "ROL");
        unitCodeMapping.put("SETS", "SET");
        unitCodeMapping.put("SQUARE FEET", "SQF");
        unitCodeMapping.put("SQUARE METERS", "SQM");
        unitCodeMapping.put("SQUARE YARDS", "SQY");
        unitCodeMapping.put("TABLETS", "TBS");
        unitCodeMapping.put("TEN GROSS", "TGM");
        unitCodeMapping.put("THOUSANDS", "THD");
        unitCodeMapping.put("TONNES", "TON");
        unitCodeMapping.put("TUBES", "TUB");
        unitCodeMapping.put("UNITS", "UNT");
        unitCodeMapping.put("US GALLONS", "UGS");
        unitCodeMapping.put("YARDS", "YDS");
        return unitCodeMapping;
    }

    @Override
    public JSONObject revertEWayStatus(Map requestParam) throws ServiceException {
        JSONObject returnObject = new JSONObject();
        String moduleId = requestParam.containsKey("moduleId") ? (String) requestParam.get("moduleId") : " ";
        String reportIds = requestParam.containsKey("reportIds") ? (String) requestParam.get("reportIds") : " ";
        String tableName = getMainTableForModule(moduleId);
        requestParam.put("tableName", tableName);
        requestParam.put("setStatus", "'F'");
        String reportidArray[] = reportIds.split(",");
        List updateDocumentList = new LinkedList();
        for (String reportId : reportidArray) {
            updateDocumentList.add(reportId);
        }
        int result = accCustomerReportServiceDao.updateEwayJSONExportFlag(updateDocumentList, requestParam);
        return returnObject;
    }

    @Override
    public JSONObject isEntityFilterApplied(JSONArray searchJSONArray, String moduleid) throws JSONException {
        boolean isModuleEntityFilterApplied = false;
        String entityName = "";
        JSONObject returnObject = new JSONObject();
        for (int itr = 0; searchJSONArray.length() > itr; itr++) {
            JSONObject obj = searchJSONArray.getJSONObject(itr);
            String field = obj.optString("columnheader");
            if (field.equalsIgnoreCase("Entity")) {
                if (obj.optString("modulename").contains(getModuleNameForModuleID(moduleid))) {
                    isModuleEntityFilterApplied = true;
                    entityName = obj.optString("combosearch");
                    break;
                }
            }
        }
        returnObject.put("isModuleEntityFilterApplied", isModuleEntityFilterApplied);
        returnObject.put("entityName", entityName);
        return returnObject;
    }

    private Map getEntitydetails(Map requestParams) throws ServiceException {
        Map returnMap = new HashMap();
        returnMap = accCustomerReportServiceDao.getEntityDetails(requestParams);
        return returnMap;
    }

    private Map getDefaultStates() throws ServiceException {
        Map returnMap = new HashMap();
        Map stateToCode = new HashMap();
        Map codeToState = new HashMap();
        SqlRowSet rs = accCustomerReportServiceDao.getDefaultStates();
        while (rs.next()) {
            codeToState.put(rs.getString(1), rs.getString(2));
            stateToCode.put(rs.getString(2).toUpperCase(), rs.getString(1));
        }
        returnMap.put("codeToState", codeToState);
        returnMap.put("stateToCode", stateToCode);
        return returnMap;
    }
    
     /**
     * SetUp Default Custom Reports for the selected country and module
     *
     * @param requestParam
     * @return JSONObject
     * @throws com.krawler.common.service.ServiceException
     * @throws com.krawler.utils.json.base.JSONException
     */
    @Override
    public boolean setUpCustomReportsDefaultsForNewCompany(Map<String, Object> requestParam) throws ServiceException, JSONException {
        HashMap<String, String> requesttemp = new HashMap<String, String>();
        String companyId = (String) requestParam.get("companyid");
        String  countryId = (String) requestParam.get("country");
        boolean isEwayReport = countryId.equalsIgnoreCase(Constants.INDIA_COUNTRYID);//If country is India then Eway Report.
        requesttemp.put("countryid", (String) requestParam.get("country"));
        JSONObject jsonObj = null;
        boolean isReportCreated = false;
        JSONObject updatedDefaultJSONObj = null;
        JSONArray updatedColumnConfigJSON = null;
        KwlReturnObject resultObjTemp = accCustomerReportServiceDao.getCustomReportsDefaults(requesttemp);
        if (resultObjTemp != null  && resultObjTemp.getEntityList() != null && !resultObjTemp.getEntityList().isEmpty()) {
            List<CustomReportsDefaults> defaultEwayBillList = resultObjTemp.getEntityList();
            String uid = (String) requestParam.get("userId");
            User userObj = (User) accountingHandlerDAOobj.getObject(User.class.getName(), uid).getEntityList().get(0);
            if (defaultEwayBillList != null) {
                for (CustomReportsDefaults defaultEwayBill : defaultEwayBillList) {
                    String defaultJSONStr = defaultEwayBill.getDefaultjson();
                    if (!StringUtil.isNullOrEmpty(defaultJSONStr)) {
                        updatedDefaultJSONObj = new JSONObject();
                        updatedColumnConfigJSON = new JSONArray();
                        JSONObject defaultJSONObj = new JSONObject(defaultJSONStr);
                        JSONArray columnConfigJSON = defaultJSONObj.optJSONArray("columnConfig");
                        for (int j = 0; j < columnConfigJSON.length(); j++) {
                            jsonObj = columnConfigJSON.getJSONObject(j);
                            if (jsonObj.optBoolean("customfield", false)) {                                
                                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                                HashMap<String, Object> params = new HashMap<String, Object>();
                                filter_names = new ArrayList();
                                filter_params = new ArrayList();
                                filter_names.add("companyid");
                                filter_names.add("fieldname");
                                filter_names.add("moduleid");
                                filter_params.add(companyId);
                                filter_params.add("Custom_" + jsonObj.getString("defaultHeader"));
                                if (jsonObj.optBoolean("allowcrossmodule", false)) {
                                    filter_params.add(Integer.parseInt(jsonObj.getString("crossJoinModuleId")));
                                } else {
                                    filter_params.add(Integer.parseInt(defaultEwayBill.getModuleid()));
                                }
                                params.put("filter_names", filter_names);
                                params.put("filter_values", filter_params);
                                KwlReturnObject fieldparams = accAccountDAOobj.getFieldParams(params);
                                List fieldParamsList = fieldparams.getEntityList();
                                if (fieldparams != null && fieldParamsList.size() > 0) {
                                    for (int cnt = 0; cnt < fieldParamsList.size(); cnt++) {
                                        FieldParams fieldParamsObj = (FieldParams) fieldParamsList.get(cnt);
                                        jsonObj.put("id", fieldParamsObj.getId());
                                        jsonObj.put("columnid", fieldParamsObj.getId());
                                        jsonObj.put("reftabledatacolumn", "col" + fieldParamsObj.getColnum());
                                    }
                                    updatedColumnConfigJSON.put(jsonObj);
                                }
                            } else {
                                updatedColumnConfigJSON.put(jsonObj);
                            }
                        }
                        updatedDefaultJSONObj.put("columnConfig", updatedColumnConfigJSON);
                    }
                    HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
                    requestParams1.put("reportName", defaultEwayBill.getName());
                    requestParams1.put("companyID", companyId);
                    requestParams1.put("userId", (String) requestParam.get("userId"));
                    if (!isCustomReportNameExists(requestParams1)) {
                        ReportMaster accDefaultReport = new ReportMaster();
                        accDefaultReport.setID(UUID.randomUUID().toString());
                        accDefaultReport.setName(defaultEwayBill.getName());
                        accDefaultReport.setDescription(defaultEwayBill.getName());
                        accDefaultReport.setCreatedon(new java.util.Date().getTime());
                        accDefaultReport.setUpdatedon(new java.util.Date().getTime());
                        accDefaultReport.setUsersByCreatedbyid(userObj);
                        String moduleCatID = "";
                       KwlReturnObject moduleCatResult = accCustomerReportServiceDao.getModuleCategoryForModule(defaultEwayBill.getModuleid());
                        if (moduleCatResult != null) {
                            List lst = moduleCatResult.getEntityList();
                            Iterator ite = lst.iterator();
                            while (ite.hasNext()) {
                                moduleCatID = String.valueOf(ite.next());
                           }
                        }
                        accDefaultReport.setReportmodulecategory((ModuleCategory) accountingHandlerDAOobj.getObject(ModuleCategory.class.getName(), moduleCatID).getEntityList().get(0));
                        accDefaultReport.setReportjson(updatedDefaultJSONObj.toString());
                        accDefaultReport.setIspivot(false);
                        accDefaultReport.setIsdefault(false);
                        accDefaultReport.setCompanyId(companyId);
                        accDefaultReport.setModuleid(defaultEwayBill.getModuleid());
                        accDefaultReport.setCountryid(defaultEwayBill.getCountryid());
                        accDefaultReport.seteWayReport(isEwayReport);
                        /**
                         * Flag to add report in Statutory list tree panel
                         */
                        accDefaultReport.setIsShowasQuickLinks(defaultEwayBill.isIsShowasQuickLinks());
                        KwlReturnObject result1 = accCustomerReportServiceDao.saveOrUpdateCustomReport(accDefaultReport, requestParam);   //saving report as a new entry
                        if (result1.getEntityList().size() > 0) {
                            isReportCreated = true;
                        }

                    }

                }
            }
        }
        return isReportCreated;
    }
    public boolean copyCustomReport(Map valueMap) throws ServiceException,JSONException,IOException, SessionExpiredException, ParseException{
        
        String reportId= valueMap.get("reportId")!= null ? (String)valueMap.get("reportId") :"";
        String userid= valueMap.get("userId")!= null ? (String) valueMap.get("userId"): "";
        String reportDescription= valueMap.get("reportDescription")!= null ? (String) valueMap.get("reportDescription") : "";
        String reportName= valueMap.get("reportName")!= null ? (String) valueMap.get("reportName") : "";
         
        
        Boolean success=false;
        
        User userObj = (User) accountingHandlerDAOobj.getObject(User.class.getName(), userid).getEntityList().get(0);
        if(!StringUtil.isNullOrEmpty(reportId))
        {
            ReportMaster reportDetails = accCustomerReportServiceDao.fetchCustomReportDetails(reportId);
            ReportMaster copyReportDetails = new ReportMaster(reportDetails);

            copyReportDetails.setID(UUID.randomUUID().toString());
            copyReportDetails.setName(reportName);
            copyReportDetails.setDescription(reportDescription);

            copyReportDetails.setUsersByCreatedbyid(userObj);
            copyReportDetails.setUsersByUpdatedbyid(userObj);

            long createdon = new java.util.Date().getTime();
            copyReportDetails.setCreatedon(createdon);
            long updatedon = new java.util.Date().getTime();
            copyReportDetails.setUpdatedon(updatedon);

            KwlReturnObject result = accCustomerReportServiceDao.saveOrUpdateCustomReport(copyReportDetails, valueMap);
            if(!StringUtil.isNullObject(result)){
            SavedSearchQuery searchDetails = accCustomerReportServiceDao.getSaved_Search_Query(reportId);
            SavedSearchQuery copySearchDetails = new SavedSearchQuery(searchDetails);
            copySearchDetails.setCustomReportId(copyReportDetails.getID());
            copySearchDetails.setUser(userObj);
            copySearchDetails.setUpdatedOn(updatedon);
            
            SavedSearchQuery SearchDetails =saveSearchDAOobj.saveSearchQuery(copySearchDetails);
            if (!StringUtil.isNullObject(result) && !StringUtil.isNullObject(SearchDetails)) {
                success = true;
            }
            }
        }
        return success;
    }

    @Override
    public boolean getJSONExportButtonStatus(JSONArray validRecords, Set invalidDocuments) throws JSONException {
        boolean status = false;
        for (int itr = 0; itr < validRecords.length(); itr++) {
            if (!invalidDocuments.contains(validRecords.getJSONObject(itr).optString("billid"))) {
                status = true;
                break;
            }
        }
        return status;
    }

}
