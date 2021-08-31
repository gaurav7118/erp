/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.vendor;

import com.krawler.common.admin.CustomizeReportMapping;
import com.krawler.common.admin.FieldParams;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.MessageSource;
import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.Company;
import com.krawler.common.util.DataInvalidateException;
import com.krawler.common.util.IndiaComplianceConstants;
import com.krawler.spring.accounting.account.accCusVenMapDAO;
import com.krawler.spring.accounting.account.accVendorCustomerProductDAO;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.companyDetails.companyDetailsDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import java.text.SimpleDateFormat;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 *
 * @author krawler
 */
public class accVendorControllerCMNServiceImpl implements accVendorControllerCMNService{
    private com.krawler.spring.common.fieldDataManager fieldmatamanager;
    private AccountingHandlerDAO accountingHandler;
    private accJournalEntryDAO accJournalEntryobj;
    private accAccountDAO accaccountDAO;
    private MessageSource messageSource;
    private HibernateTransactionManager txnManager;
    private accVendorDAO accVendorDAOobj;
    private accCustomerDAO accCustomerDAOobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private auditTrailDAO auditTrailObj;
    private com.krawler.customFieldMaster.fieldDataManager fieldDataManagercntrl;
    private accCusVenMapDAO accCusVenMapDAOObj;
    private accVendorCustomerProductDAO accVendorCustomerProductDAOobj;
    private accVendorControllerService accVendorControllerServiceObj;
    private companyDetailsDAO companyDetailsDAOObj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private ImportHandler importHandler;
    private String auditMsg="",auditID="";
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private accPaymentDAO accPaymentDAOobj;
    private accTaxDAO accTaxObj;
    private accProductDAO accProductObj;
    
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    
    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }
    
    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    
    public void setAccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setcompanyDetailsDAO(companyDetailsDAO companyDetailsDAOObj1) {
        this.companyDetailsDAOObj = companyDetailsDAOObj1;
    }

    public void setAccVendorCustomerProductDAOobj(accVendorCustomerProductDAO accVendorCustomerProductDAOobj) {
        this.accVendorCustomerProductDAOobj = accVendorCustomerProductDAOobj;
    }

    public accVendorCustomerProductDAO getAccVendorCustomerProductDAOobj() {
        return accVendorCustomerProductDAOobj;
    }

    public void setAccCusVenMapDAOObj(accCusVenMapDAO accCusVenMapDAOObj) {
        this.accCusVenMapDAOObj = accCusVenMapDAOObj;
    }

    public void setFieldDataManager(com.krawler.customFieldMaster.fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setaccCustomerDAO(accCustomerDAO accCustomerDAOobj) {
        this.accCustomerDAOobj = accCustomerDAOobj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setaccVendorControllerService(accVendorControllerService accVendorControllerServiceObj) {
        this.accVendorControllerServiceObj = accVendorControllerServiceObj;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccVendorDAO(accVendorDAO accVendorDAOobj) {
        this.accVendorDAOobj = accVendorDAOobj;
    }

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }
    
    public void setAccaccountDAO(accAccountDAO accaccountDAO) {
        this.accaccountDAO = accaccountDAO;
    }
    
    public void setFieldmatamanager(fieldDataManager fieldmatamanager) {
        this.fieldmatamanager = fieldmatamanager;
    }
    
    public void setAccountingHandler(AccountingHandlerDAO accountingHandler) {
        this.accountingHandler = accountingHandler;
    }

    public void setAccJournalEntryobj(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }
    
       public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }
    
    public JSONArray getRecordsForStore(HashMap<String, Object> requestParams,String modules, JSONArray jarrRecords) throws SessionExpiredException, ServiceException {
        try {
            String moduleids[]=modules.split(",");
            JSONObject jobjTemp = new JSONObject();
            jobjTemp.put("name", "transactionDate");
            jarrRecords.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "documentno");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "vendorName");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "amount");
            jarrRecords.put(jobjTemp);
            
            if (StringUtil.isNullOrEmpty(modules) || Arrays.asList(moduleids).contains(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId))) {
                jobjTemp = new JSONObject();
                jobjTemp.put("name", "amountdue");
                jarrRecords.put(jobjTemp);
            }
            
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "status");
            jarrRecords.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "moduleid");
            jarrRecords.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "cntype");
            jarrRecords.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "isOpeningBalanceTransaction");
            jarrRecords.put(jobjTemp);
            
            HashMap hashMap = new HashMap();
            hashMap.put("companyId", requestParams.get(Constants.companyid).toString());
            hashMap.put("reportId", Integer.parseInt(requestParams.get("reportId").toString()));
            if (!StringUtil.isNullOrEmpty(modules)){
                hashMap.put("moduleId", modules);
            }
            KwlReturnObject customizeReportResult = accountingHandler.getCustomizeReportViewMappingField(hashMap);
            List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
            List arrayList = new ArrayList();
            for (CustomizeReportMapping customizeReportMapping : customizeReportList) {
                String column = "Custom_" + customizeReportMapping.getDataIndex();
                if (!arrayList.contains(customizeReportMapping.getDataIndex())) {
                    jobjTemp = new JSONObject();
                    jobjTemp.put("name", column);
                    jarrRecords.put(jobjTemp);
                    arrayList.add(customizeReportMapping.getDataIndex());
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getRecordsForStore : " + ex.getMessage(), ex);
        }
        return jarrRecords;
    }
    
    public JSONArray getColumnsForGrid(HashMap<String, Object> requestParams,String modules, JSONArray jarrColumns) throws SessionExpiredException, ServiceException {
        Locale locale= null;
        if(requestParams.containsKey("locale")){
        locale = (Locale) requestParams.get("locale");
        }
        try {
            String moduleids[]=modules.split(",");
            String modulids=Constants.Acc_Vendor_Invoice_ModuleId+","+Constants.Acc_Purchase_Order_ModuleId+","+Constants.Acc_Purchase_Return_ModuleId+","+Constants.Acc_Vendor_Quotation_ModuleId+","+Constants.Acc_Goods_Receipt_ModuleId+","+Constants.Acc_Debit_Note_ModuleId+","+Constants.Acc_Credit_Note_ModuleId+","+Constants.Acc_Make_Payment_ModuleId+","+Constants.Acc_Receive_Payment_ModuleId;
            JSONObject jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.inventoryList.date", null, locale));
            jobjTemp.put("dataIndex", "transactionDate");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.field.DocumentNo", null, locale));
            jobjTemp.put("dataIndex", "documentno");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header",messageSource.getMessage("acc.ven.name", null, locale));
            jobjTemp.put("dataIndex", "vendorName");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header",messageSource.getMessage("acc.invoiceList.totAmtHome", null, locale) +" ("+requestParams.get("currencyname").toString()+")");
            jobjTemp.put("dataIndex", "amount");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            
            if (StringUtil.isNullOrEmpty(modules) || Arrays.asList(moduleids).contains(String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId))) {
                jobjTemp = new JSONObject();
                jobjTemp.put("header",messageSource.getMessage("acc.agedPay.gridAmtDueHomeCurrency", null, locale) +" ("+requestParams.get("currencyname").toString()+")");
                jobjTemp.put("dataIndex", "amountdue");
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jarrColumns.put(jobjTemp);
            }
            
            jobjTemp = new JSONObject();
            jobjTemp.put("header", messageSource.getMessage("acc.invoiceList.status", null, locale));
            jobjTemp.put("dataIndex", "status");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jarrColumns.put(jobjTemp);
            HashMap hashMap = new HashMap();
            hashMap.put("companyId", requestParams.get(Constants.companyid).toString());
            hashMap.put("reportId", Integer.parseInt(requestParams.get("reportId").toString()));
            if (!StringUtil.isNullOrEmpty(modules)){
                hashMap.put("moduleId", modules);
            }
            KwlReturnObject customizeReportResult = accountingHandler.getCustomizeReportViewMappingField(hashMap);
            List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
            List arrayList = new ArrayList();
            for (CustomizeReportMapping customizeReportMapping : customizeReportList) {
                int fieldType = 0;
                String header = customizeReportMapping.getDataHeader();
                HashMap<String, Object> requestParam = new HashMap<String, Object>();
                requestParam.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.fieldlabel, Constants.moduleid));
                requestParam.put(Constants.filter_values, Arrays.asList(requestParams.get(Constants.companyid).toString(),  StringUtil.DecodeText(header), modulids));
                KwlReturnObject fieldParamsResult = accJournalEntryobj.getFieldParameters(requestParam);
                FieldParams params = (FieldParams) fieldParamsResult.getEntityList().get(0);
                if (params.getFieldtype() == 3) {
                    fieldType = 3;
                }
                String column = "Custom_" + customizeReportMapping.getDataIndex();
                if (!arrayList.contains(customizeReportMapping.getDataIndex())) {
                    jobjTemp = new JSONObject();
                    jobjTemp.put("header", customizeReportMapping.getDataHeader());
                    jobjTemp.put("dataIndex", column);
                    jobjTemp.put("width", 150);
                    jobjTemp.put("pdfwidth", 150);
                    if (fieldType == 3) {
                        jobjTemp.put("fieldType", 3);
                    }
                    jobjTemp.put("custom", "true");
                    jarrColumns.put(jobjTemp);
                    arrayList.add(customizeReportMapping.getDataIndex());
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getColumnsForGrid : " + ex.getMessage(), ex);
        }/* catch (UnsupportedEncodingException ex) {
            Logger.getLogger(accVendorControllerCMNServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }*/
        return jarrColumns;
    }
    
    public JSONArray getPurchaseInvoiceInformation(HashMap<String, Object> requestParams, List<Object []> invoices, JSONArray DataJArr) throws SessionExpiredException, ServiceException {
        DateFormat df=(DateFormat)requestParams.get(Constants.df);
        try {
            KwlReturnObject custumObjresult = null;
            String customid = "";
            HashMap hashMap = new HashMap();
            hashMap.put("companyId", requestParams.get(Constants.companyid).toString());
            hashMap.put("reportId", requestParams.get("reportId").toString());
            hashMap.put("moduleId", Constants.Acc_Vendor_Invoice_ModuleId);
            KwlReturnObject customizeReportResult = accountingHandler.getCustomizeReportViewMappingField(hashMap);
            List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(requestParams.get(Constants.companyid).toString(), Constants.Acc_Vendor_Invoice_ModuleId));
            HashMap<String, Integer> FieldMap = accaccountDAO.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            for(Object oj[]:invoices){
                String invid = oj[0].toString();
                KwlReturnObject inv = accountingHandler.getObject(GoodsReceipt.class.getName(), invid);
                GoodsReceipt GR = (GoodsReceipt) inv.getEntityList().get(0);
                JSONObject newJobj = new JSONObject();
                if(!GR.isIsOpeningBalenceInvoice()){
//                    newJobj.put("transactionDate", df.format(GR.getJournalEntry().getEntryDate()));
                    newJobj.put("transactionDate", df.format(GR.getCreationDate()));
                    newJobj.put("amount", GR.getInvoiceAmountInBase());
                    newJobj.put("amountdue", GR.getInvoiceAmountDueInBase());
                    newJobj.put("isOpeningBalanceTransaction", false);
                    newJobj.put("status", GR.getInvoiceAmountDueInBase()!=0?Constants.openStatus:Constants.closedStatus);
                }else{
                    newJobj.put("transactionDate", df.format(GR.getCreationDate()));
                    newJobj.put("amount", GR.getOriginalOpeningBalanceBaseAmount());
                    newJobj.put("amountdue", GR.getOpeningBalanceBaseAmountDue());
                    newJobj.put("isOpeningBalanceTransaction", true);
                    newJobj.put("status", GR.getOpeningBalanceBaseAmountDue()!=0?Constants.openStatus:Constants.closedStatus);
                }
                if(!requestParams.get("status").toString().equals("all") && !requestParams.get("status").toString().equals(newJobj.get("status").toString())){
                    continue;
                }
                newJobj.put("documentno", GR.getGoodsReceiptNumber());
                newJobj.put("vendorName", GR.getVendor().getName());
                newJobj.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);
                if (!customizeReportList.isEmpty()) {
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    if (GR.getJournalEntry() != null) {
                        customid = GR.getJournalEntry().getID();
                        custumObjresult = accountingHandler.getObject(AccJECustomData.class.getName(), customid);
                    } else {
                        customid = GR.getOpeningBalanceVendorInvoiceCustomData().getOpeningBalanceVendorInvoiceId();
                        custumObjresult = accountingHandler.getObject(OpeningBalanceVendorInvoiceCustomData.class.getName(), customid);
                    }
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        AccJECustomData jeDetailCustom = null;
                        OpeningBalanceVendorInvoiceCustomData OBVICustomData = null;
                        if (GR.getJournalEntry() != null) {
                            jeDetailCustom = (AccJECustomData) custumObjresult.getEntityList().get(0);
                            if (jeDetailCustom != null) {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                            }
                        } else {
                            OBVICustomData = (OpeningBalanceVendorInvoiceCustomData) custumObjresult.getEntityList().get(0);
                            if (OBVICustomData != null) {
                                AccountingManager.setCustomColumnValues(OBVICustomData, FieldMap, replaceFieldMap, variableMap);
                            }
                        }
                        JSONObject params = new JSONObject();
                        params.put(Constants.companyKey, requestParams.get(Constants.companyid).toString());
                        params.put(Constants.isExport, Boolean.parseBoolean(requestParams.get(Constants.isExport).toString()));
                        if(requestParams.containsKey("browsertimezone") && requestParams.get("browsertimezone")!=null){
                            params.put(Constants.browsertz, requestParams.get("browsertimezone").toString());
                        }
                        /*
                         * To print Combo list and check list value in vendor
                         * registry report
                         */
                        params.put("isReturnDropdownCheckListVal", true);
                        fieldmatamanager.addCustomData(variableMap, customFieldMap, customDateFieldMap, newJobj, params);
                    }
                }
                DataJArr.put(newJobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getPurchaseInvoiceInformation : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }
    
    public JSONArray getPurchaseOrdersInformation(HashMap<String, Object> requestParams, List<Object []> orders, JSONArray DataJArr) throws SessionExpiredException, ServiceException {
        DateFormat df=(DateFormat)requestParams.get(Constants.df);
        try {
            HashMap hashMap = new HashMap();
            hashMap.put("companyId", requestParams.get(Constants.companyid).toString());
            hashMap.put("reportId", requestParams.get("reportId").toString());
            hashMap.put("moduleId", Constants.Acc_Purchase_Order_ModuleId);
            KwlReturnObject customizeReportResult = accountingHandler.getCustomizeReportViewMappingField(hashMap);
            List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(requestParams.get(Constants.companyid).toString(), Constants.Acc_Purchase_Order_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
            FieldMap = accaccountDAO.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            KwlReturnObject custumObjresult = null;
            for(Object oj[]:orders){
                String orderid = oj[0].toString();
                KwlReturnObject order = accountingHandler.getObject(PurchaseOrder.class.getName(), orderid);
                PurchaseOrder purchaseorder = (PurchaseOrder) order.getEntityList().get(0);
                JSONObject newJobj = new JSONObject();
                newJobj.put("transactionDate", df.format(purchaseorder.getOrderDate()));
                newJobj.put("documentno", purchaseorder.getPurchaseOrderNumber());
                newJobj.put("vendorName", purchaseorder.getVendor().getName());
                newJobj.put("amount", purchaseorder.getTotalamountinbase());
                newJobj.put("status", purchaseorder.isIsOpen()?Constants.openStatus:Constants.closedStatus);
                newJobj.put(Constants.moduleid, Constants.Acc_Purchase_Order_ModuleId);
                newJobj.put("isOpeningBalanceTransaction", false);
                if(!requestParams.get("status").toString().equals("all") && !requestParams.get("status").toString().equals(newJobj.get("status").toString())){
                    continue;
                }
                if (purchaseorder.getPoCustomData() != null && !customizeReportList.isEmpty()) {
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    custumObjresult = accountingHandler.getObject(PurchaseOrderCustomData.class.getName(), purchaseorder.getPoCustomData().getPoID());
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        PurchaseOrderCustomData poCustomData = (PurchaseOrderCustomData) custumObjresult.getEntityList().get(0);
                        if (poCustomData != null) {
                            AccountingManager.setCustomColumnValues(poCustomData, FieldMap, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put(Constants.companyKey, requestParams.get(Constants.companyid).toString());
                            params.put(Constants.isExport, Boolean.parseBoolean(requestParams.get(Constants.isExport).toString()));
                            /*
                             * To print Combo list and check list value in vendor registry report
                             */
                            params.put("isReturnDropdownCheckListVal", true);
                            if(requestParams.containsKey("browsertimezone") && requestParams.get("browsertimezone")!=null){
                                params.put(Constants.browsertz, requestParams.get("browsertimezone").toString());
                            }
                            fieldmatamanager.addCustomData(variableMap, customFieldMap, customDateFieldMap, newJobj, params);
                        }
                    }
                }
                DataJArr.put(newJobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getPurchaseOrdersInformation : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }
    
    public JSONArray getPurchaseReturnInformation(HashMap<String, Object> requestParams, List<Object []> returns, JSONArray DataJArr) throws SessionExpiredException, ServiceException {
        DateFormat df=(DateFormat)requestParams.get(Constants.df);
        try {
            HashMap hashMap = new HashMap();
            hashMap.put("companyId", requestParams.get(Constants.companyid).toString());
            hashMap.put("reportId", requestParams.get("reportId").toString());
            hashMap.put("moduleId", Constants.Acc_Purchase_Return_ModuleId);
            KwlReturnObject customizeReportResult = accountingHandler.getCustomizeReportViewMappingField(hashMap);
            List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(requestParams.get(Constants.companyid).toString(), Constants.Acc_Purchase_Return_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
            FieldMap = accaccountDAO.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            KwlReturnObject custumObjresult = null;
            for(Object oj[]:returns){
                String returnid = oj[0].toString();
                KwlReturnObject preturn = accountingHandler.getObject(PurchaseReturn.class.getName(), returnid);
                PurchaseReturn purchasereturn = (PurchaseReturn) preturn.getEntityList().get(0);
                JSONObject newJobj = new JSONObject();
                newJobj.put("transactionDate", df.format(purchasereturn.getOrderDate()));
                newJobj.put("documentno", purchasereturn.getPurchaseReturnNumber());
                newJobj.put("vendorName", purchasereturn.getVendor().getName());
                newJobj.put(Constants.moduleid, Constants.Acc_Purchase_Return_ModuleId);
                newJobj.put("isOpeningBalanceTransaction", false);
                newJobj.put("amount", purchasereturn.getTotalamountinbase());
                if(purchasereturn.isIsNoteAlso()){
                    newJobj.put("status", Constants.closedStatus);
                }else{
                    newJobj.put("status", Constants.openStatus);
                }
                if(!requestParams.get("status").toString().equals("all") && !requestParams.get("status").toString().equals(newJobj.get("status").toString())){
                    continue;
                }
                if (purchasereturn.getPurchaseReturnCustomData()!=null && !customizeReportList.isEmpty()) {
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    custumObjresult = accountingHandler.getObject(PurchaseReturnCustomData.class.getName(), purchasereturn.getPurchaseReturnCustomData().getPurchaseReturnId());
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        PurchaseReturnCustomData prCustomData = (PurchaseReturnCustomData) custumObjresult.getEntityList().get(0);
                        if (prCustomData != null) {
                            AccountingManager.setCustomColumnValues(prCustomData, FieldMap, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put(Constants.companyKey, requestParams.get(Constants.companyid).toString());
                            params.put(Constants.isExport, Boolean.parseBoolean(requestParams.get(Constants.isExport).toString()));
                            if(requestParams.containsKey("browsertimezone") && requestParams.get("browsertimezone")!=null){
                                params.put(Constants.browsertz, requestParams.get("browsertimezone").toString());
                            }
                            /*
                             * To print Combo list and check list value in vendor registry report
                             */
                            params.put("isReturnDropdownCheckListVal", true);
                            fieldmatamanager.addCustomData(variableMap, customFieldMap, customDateFieldMap, newJobj, params);
                        }
                    }
                }
                DataJArr.put(newJobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getPurchaseReturnInformation : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }
    
    public JSONArray getQuotationsInformation(HashMap<String, Object> requestParams, List<Object> vquotations, JSONArray DataJArr) throws SessionExpiredException, ServiceException, ParseException {
        DateFormat df=(DateFormat)requestParams.get(Constants.df);
        try {
            HashMap hashMap = new HashMap();
            hashMap.put("companyId", requestParams.get(Constants.companyid).toString());
            hashMap.put("reportId", requestParams.get("reportId").toString());
            hashMap.put("moduleId", Constants.Acc_Vendor_Quotation_ModuleId);
            KwlReturnObject customizeReportResult = accountingHandler.getCustomizeReportViewMappingField(hashMap);
            List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(requestParams.get(Constants.companyid).toString(), Constants.Acc_Vendor_Quotation_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
            FieldMap = accaccountDAO.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            KwlReturnObject custumObjresult = null;
            for(Object obj:vquotations){
                String vquotationid = obj.toString();
                KwlReturnObject quotations = accountingHandler.getObject(VendorQuotation.class.getName(), vquotationid);
                VendorQuotation vendorquotation = (VendorQuotation) quotations.getEntityList().get(0);
                JSONObject newJobj = new JSONObject();
                newJobj.put("transactionDate", df.format(vendorquotation.getQuotationDate()));
                newJobj.put("documentno", vendorquotation.getQuotationNumber());
                newJobj.put("vendorName", vendorquotation.getVendor().getName());
                newJobj.put("amount", vendorquotation.getQuotationamountinbase());
                newJobj.put(Constants.moduleid, Constants.Acc_Vendor_Quotation_ModuleId);
                newJobj.put("isOpeningBalanceTransaction", false);
                Date currentdate=authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(new Date()));
                Date validtilldate=authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(vendorquotation.getValiddate()));
                if(vendorquotation.getLinkflag()==2 || vendorquotation.getLinkflag()==1){
                    newJobj.put("status", Constants.closedStatus);
                }else if(vendorquotation.getLinkflag()==0 && validtilldate.before(currentdate)){
                    newJobj.put("status", Constants.expiredStatus);
                }else{
                    newJobj.put("status", Constants.openStatus);
                }
                if(!requestParams.get("status").toString().equals("all") && !requestParams.get("status").toString().equals(newJobj.get("status").toString())){
                    continue;
                }
                if (vendorquotation.getVendorQuotationCustomData()!=null && !customizeReportList.isEmpty()) {
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    custumObjresult = accountingHandler.getObject(VendorQuotationCustomData.class.getName(), vendorquotation.getVendorQuotationCustomData().getVendorQuotationId());
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        VendorQuotationCustomData vqCustomData = (VendorQuotationCustomData) custumObjresult.getEntityList().get(0);
                        if (vqCustomData != null) {
                            AccountingManager.setCustomColumnValues(vqCustomData, FieldMap, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put(Constants.companyKey, requestParams.get(Constants.companyid).toString());
                            params.put(Constants.isExport, Boolean.parseBoolean(requestParams.get(Constants.isExport).toString()));
                            if(requestParams.containsKey("browsertimezone") && requestParams.get("browsertimezone")!=null){
                                params.put(Constants.browsertz, requestParams.get("browsertimezone").toString());
                            }
                            /*
                             * To print Combo list and check list value in vendor registry report
                             */
                            params.put("isReturnDropdownCheckListVal", true);
                            fieldmatamanager.addCustomData(variableMap, customFieldMap, customDateFieldMap, newJobj, params);
                        }
                    }
                }
                DataJArr.put(newJobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getQuotationsInformation : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }
    
    public JSONArray getGoodsReceiptInformation(HashMap<String, Object> requestParams, List<Object []> greceipts, JSONArray DataJArr) throws SessionExpiredException, ServiceException {
        DateFormat df=(DateFormat)requestParams.get(Constants.df);
        try {
            HashMap hashMap = new HashMap();
            hashMap.put("companyId", requestParams.get(Constants.companyid).toString());
            hashMap.put("reportId", requestParams.get("reportId").toString());
            hashMap.put("moduleId", Constants.Acc_Goods_Receipt_ModuleId);
            KwlReturnObject customizeReportResult = accountingHandler.getCustomizeReportViewMappingField(hashMap);
            List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(requestParams.get(Constants.companyid).toString(), Constants.Acc_Goods_Receipt_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
            FieldMap = accaccountDAO.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            KwlReturnObject custumObjresult = null;
            for(Object obj[]:greceipts){
                String grid = obj[0].toString();
                KwlReturnObject grorder = accountingHandler.getObject(GoodsReceiptOrder.class.getName(), grid);
                GoodsReceiptOrder goodsreceiptorder = (GoodsReceiptOrder) grorder.getEntityList().get(0);
                JSONObject newJobj = new JSONObject();
                newJobj.put("transactionDate", df.format(goodsreceiptorder.getOrderDate()));
                newJobj.put("documentno", goodsreceiptorder.getGoodsReceiptOrderNumber());
                newJobj.put("vendorName", goodsreceiptorder.getVendor().getName());
                newJobj.put("amount", goodsreceiptorder.getTotalamountinbase());
                newJobj.put(Constants.moduleid, Constants.Acc_Goods_Receipt_ModuleId);
                newJobj.put("isOpeningBalanceTransaction", false);
                String status=Constants.openStatus;
                HashSet<String> invids=new HashSet<String>();
                for(GoodsReceiptOrderDetails grd:goodsreceiptorder.getRows()){
                    if(grd.getVidetails()!=null){
                        if(invids.contains(grd.getVidetails().getGoodsReceipt().getID())){
                            continue;
                        }
                        invids.add(grd.getVidetails().getGoodsReceipt().getID());
                        if(!grd.getVidetails().getGoodsReceipt().isIsOpenInGR()){
                            status=Constants.closedStatus;
                        }else{
                            status=Constants.openStatus;
                            break;
                        }
                    }
                }
                if(!goodsreceiptorder.isIsOpenInPI() || !goodsreceiptorder.isIsOpenInPR()){
                    status=Constants.closedStatus;
                }
                newJobj.put("status", status);
                if(!requestParams.get("status").toString().equals("all") && !requestParams.get("status").toString().equals(newJobj.get("status").toString())){
                    continue;
                }
                if (goodsreceiptorder.getGoodsReceiptOrderCustomData()!=null && !customizeReportList.isEmpty()) {
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    custumObjresult = accountingHandler.getObject(GoodsReceiptOrderCustomData.class.getName(), goodsreceiptorder.getGoodsReceiptOrderCustomData().getGoodsReceiptOrderId());
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        GoodsReceiptOrderCustomData grCustomData = (GoodsReceiptOrderCustomData) custumObjresult.getEntityList().get(0);
                        if (grCustomData != null) {
                            AccountingManager.setCustomColumnValues(grCustomData, FieldMap, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put(Constants.companyKey, requestParams.get(Constants.companyid).toString());
                            params.put(Constants.isExport, Boolean.parseBoolean(requestParams.get(Constants.isExport).toString()));
                            if(requestParams.containsKey("browsertimezone") && requestParams.get("browsertimezone")!=null){
                                params.put(Constants.browsertz, requestParams.get("browsertimezone").toString());
                            }
                            /*
                             * To print Combo list and check list value in vendor registry report
                             */
                            params.put("isReturnDropdownCheckListVal", true);
                            fieldmatamanager.addCustomData(variableMap, customFieldMap, customDateFieldMap, newJobj, params);
                        }
                    }
                }
                DataJArr.put(newJobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getGoodsReceiptInformation : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }
    
    public JSONArray getDebitNoteInformation(HashMap<String, Object> requestParams, List<Object []> dnotes, JSONArray DataJArr) throws SessionExpiredException, ServiceException {
        DateFormat df=(DateFormat)requestParams.get(Constants.df);
        try {
            HashMap hashMap = new HashMap();
            String companyid = (String) requestParams.get(Constants.companyKey);
            hashMap.put("companyId", requestParams.get(Constants.companyid).toString());
            hashMap.put("reportId", requestParams.get("reportId").toString());
            hashMap.put("moduleId", Constants.Acc_Debit_Note_ModuleId);
            KwlReturnObject customizeReportResult = accountingHandler.getCustomizeReportViewMappingField(hashMap);
            List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(requestParams.get(Constants.companyid).toString(), Constants.Acc_Debit_Note_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
            FieldMap = accaccountDAO.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            KwlReturnObject custumObjresult = null;
            for(Object obj[]:dnotes){
                String dnid = obj[1].toString();
                KwlReturnObject note = accountingHandler.getObject(DebitNote.class.getName(), dnid);
                DebitNote debitnote = (DebitNote) note.getEntityList().get(0);
                JSONObject newJobj = new JSONObject();
//                newJobj.put("transactionDate", df.format(debitnote.getJournalEntry().getEntryDate()));
                newJobj.put("transactionDate", df.format(debitnote.getCreationDate()));
                newJobj.put("documentno", debitnote.getDebitNoteNumber());
                newJobj.put("vendorName", debitnote.getVendor().getName());
                newJobj.put("amount", authHandler.round(debitnote.getDnamountinbase(), companyid));
                newJobj.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
                newJobj.put("cntype", debitnote.getDntype());
                newJobj.put("isOpeningBalanceTransaction", false);
                if(debitnote.getDnamountdue()==0){
                    newJobj.put("status", Constants.closedStatus);
                }else{
                    newJobj.put("status", Constants.openStatus);
                }
                if(!requestParams.get("status").toString().equals("all") && !requestParams.get("status").toString().equals(newJobj.get("status").toString())){
                    continue;
                }
                if (!customizeReportList.isEmpty()) {
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    custumObjresult = accountingHandler.getObject(AccJECustomData.class.getName(), debitnote.getJournalEntry().getID());
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        AccJECustomData jeCustomData = (AccJECustomData) custumObjresult.getEntityList().get(0);
                        if (jeCustomData != null) {
                            AccountingManager.setCustomColumnValues(jeCustomData, FieldMap, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put(Constants.companyKey, requestParams.get(Constants.companyid).toString());
                            params.put(Constants.isExport, Boolean.parseBoolean(requestParams.get(Constants.isExport).toString()));
                            if(requestParams.containsKey("browsertimezone") && requestParams.get("browsertimezone")!=null){
                                params.put(Constants.browsertz, requestParams.get("browsertimezone").toString());
                            }
                            fieldmatamanager.addCustomData(variableMap, customFieldMap, customDateFieldMap, newJobj, params);
                        }
                    }
                }
                DataJArr.put(newJobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getDebitNoteInformation : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }
    
    public JSONArray getOpeningDebitNoteInformation(HashMap<String, Object> requestParams, List<Object []> dnotes, JSONArray DataJArr) throws SessionExpiredException, ServiceException {
        DateFormat df=(DateFormat)requestParams.get(Constants.df);
        try {
            HashMap hashMap = new HashMap();
            String companyid = (String) requestParams.get(Constants.companyKey);
            hashMap.put("companyId", requestParams.get(Constants.companyid).toString());
            hashMap.put("reportId", requestParams.get("reportId").toString());
            hashMap.put("moduleId", Constants.Acc_Debit_Note_ModuleId);
            KwlReturnObject customizeReportResult = accountingHandler.getCustomizeReportViewMappingField(hashMap);
            List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(requestParams.get(Constants.companyid).toString(), Constants.Acc_Debit_Note_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
            FieldMap = accaccountDAO.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            KwlReturnObject custumObjresult = null;
            for(Object obj:dnotes){
                DebitNote debitnote = (DebitNote) obj;
                JSONObject newJobj = new JSONObject();
                newJobj.put("transactionDate", df.format(debitnote.getCreationDate()));
                newJobj.put("documentno", debitnote.getDebitNoteNumber());
                newJobj.put("vendorName", debitnote.getVendor().getName());
                newJobj.put("amount", authHandler.round(debitnote.getOriginalOpeningBalanceBaseAmount(), companyid));
                newJobj.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
                newJobj.put("cntype", debitnote.getDntype());
                newJobj.put("isOpeningBalanceTransaction", true);
                if(debitnote.getOpeningBalanceAmountDue()==0){
                    newJobj.put("status", Constants.closedStatus);
                }else{
                    newJobj.put("status", Constants.openStatus);
                }
                if(!requestParams.get("status").toString().equals("all") && !requestParams.get("status").toString().equals(newJobj.get("status").toString())){
                    continue;
                }
                if (debitnote.getOpeningBalanceDebitNoteCustomData() != null && !customizeReportList.isEmpty()) {
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    custumObjresult = accountingHandler.getObject(OpeningBalanceDebitNoteCustomData.class.getName(), debitnote.getOpeningBalanceDebitNoteCustomData().getOpeningBalanceDebitNoteId());
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        OpeningBalanceDebitNoteCustomData OBDebitNoteCustomData = (OpeningBalanceDebitNoteCustomData) custumObjresult.getEntityList().get(0);
                        if (OBDebitNoteCustomData != null) {
                            AccountingManager.setCustomColumnValues(OBDebitNoteCustomData, FieldMap, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put(Constants.companyKey, requestParams.get(Constants.companyid).toString());
                            params.put(Constants.isExport, Boolean.parseBoolean(requestParams.get(Constants.isExport).toString()));                            
                            if (requestParams.containsKey("browsertimezone") && requestParams.get("browsertimezone") != null) {
                                params.put(Constants.browsertz, requestParams.get("browsertimezone").toString());
                            }
                            /*
                             * To print Combo list and check list value in vendor registry report
                             */
                            params.put("isReturnDropdownCheckListVal", true);
                            fieldmatamanager.addCustomData(variableMap, customFieldMap, customDateFieldMap, newJobj, params);
                        }
                    }
                }
                DataJArr.put(newJobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getOpeningDebitNoteInformation : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }
    
    public JSONArray getCreditNoteInformation(HashMap<String, Object> requestParams, List<Object []> cnotes, JSONArray DataJArr) throws SessionExpiredException, ServiceException {
        DateFormat df=(DateFormat)requestParams.get(Constants.df);
        try {
            HashMap hashMap = new HashMap();
            String companyid = (String) requestParams.get(Constants.companyKey);
            hashMap.put("companyId", requestParams.get(Constants.companyid).toString());
            hashMap.put("reportId", requestParams.get("reportId").toString());
            hashMap.put("moduleId", Constants.Acc_Credit_Note_ModuleId);
            KwlReturnObject customizeReportResult = accountingHandler.getCustomizeReportViewMappingField(hashMap);
            List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(requestParams.get(Constants.companyid).toString(), Constants.Acc_Credit_Note_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
            FieldMap = accaccountDAO.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            KwlReturnObject custumObjresult = null;
            for(Object obj[]:cnotes){
                String doid = obj[1].toString();
                KwlReturnObject notes = accountingHandler.getObject(CreditNote.class.getName(), doid);
                CreditNote creditnote = (CreditNote) notes.getEntityList().get(0);
                JSONObject newJobj = new JSONObject();
//                newJobj.put("transactionDate", df.format(creditnote.getJournalEntry().getEntryDate()));
                newJobj.put("transactionDate", df.format(creditnote.getCreationDate()));
                newJobj.put("documentno", creditnote.getCreditNoteNumber());
                newJobj.put("vendorName", creditnote.getVendor().getName());
                newJobj.put("amount", authHandler.round(creditnote.getCnamountinbase(), companyid));
                newJobj.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                newJobj.put("cntype", creditnote.getCntype());
                newJobj.put("isOpeningBalanceTransaction", false);
                if(creditnote.getCnamountdue()==0){
                    newJobj.put("status", Constants.closedStatus);
                }else{
                    newJobj.put("status", Constants.openStatus);
                }
                if(!requestParams.get("status").toString().equals("all") && !requestParams.get("status").toString().equals(newJobj.get("status").toString())){
                    continue;
                }
                if (!customizeReportList.isEmpty()) {
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    custumObjresult = accountingHandler.getObject(AccJECustomData.class.getName(), creditnote.getJournalEntry().getID());
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        AccJECustomData jeCustomData = (AccJECustomData) custumObjresult.getEntityList().get(0);
                        if (jeCustomData != null) {
                            AccountingManager.setCustomColumnValues(jeCustomData, FieldMap, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put(Constants.companyKey, requestParams.get(Constants.companyid).toString());
                            params.put(Constants.isExport, Boolean.parseBoolean(requestParams.get(Constants.isExport).toString()));                            
                            if (requestParams.containsKey("browsertimezone") && requestParams.get("browsertimezone") != null) {
                                params.put(Constants.browsertz, requestParams.get("browsertimezone").toString());
                            }
                            fieldmatamanager.addCustomData(variableMap, customFieldMap, customDateFieldMap, newJobj, params);
                        }
                    }
                }
                DataJArr.put(newJobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCreditNoteInformation : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }
    
    public JSONArray getOpeningCreditNoteInformation(HashMap<String, Object> requestParams, List<Object []> cnotes, JSONArray DataJArr) throws SessionExpiredException, ServiceException {
        DateFormat df=(DateFormat)requestParams.get(Constants.df);
        try {
            HashMap hashMap = new HashMap();
            String companyid = (String) requestParams.get(Constants.companyKey);
            hashMap.put("companyId", requestParams.get(Constants.companyid).toString());
            hashMap.put("reportId", requestParams.get("reportId").toString());
            hashMap.put("moduleId", Constants.Acc_Credit_Note_ModuleId);
            KwlReturnObject customizeReportResult = accountingHandler.getCustomizeReportViewMappingField(hashMap);
            List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(requestParams.get(Constants.companyid).toString(), Constants.Acc_Credit_Note_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
            FieldMap = accaccountDAO.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            KwlReturnObject custumObjresult = null;
            for(Object obj:cnotes){
                CreditNote creditnote = (CreditNote) obj;
                JSONObject newJobj = new JSONObject();
                newJobj.put("transactionDate", df.format(creditnote.getCreationDate()));
                newJobj.put("documentno", creditnote.getCreditNoteNumber());
                newJobj.put("vendorName", creditnote.getVendor().getName());
                newJobj.put("amount", authHandler.round(creditnote.getOriginalOpeningBalanceBaseAmount(), companyid));
                newJobj.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                newJobj.put("cntype", creditnote.getCntype());
                newJobj.put("isOpeningBalanceTransaction", true);
                if(creditnote.getOpeningBalanceAmountDue()==0){
                    newJobj.put("status", Constants.closedStatus);
                }else{
                    newJobj.put("status", Constants.openStatus);
                }
                if(!requestParams.get("status").toString().equals("all") && !requestParams.get("status").toString().equals(newJobj.get("status").toString())){
                    continue;
                }
                if (creditnote.getOpeningBalanceCreditNoteCustomData() != null && !customizeReportList.isEmpty()) {
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    custumObjresult = accountingHandler.getObject(OpeningBalanceCreditNoteCustomData.class.getName(), creditnote.getOpeningBalanceCreditNoteCustomData().getOpeningBalanceCreditNoteId());
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        OpeningBalanceCreditNoteCustomData OBCreditNoteCustomData = (OpeningBalanceCreditNoteCustomData) custumObjresult.getEntityList().get(0);
                        if (OBCreditNoteCustomData != null) {
                            AccountingManager.setCustomColumnValues(OBCreditNoteCustomData, FieldMap, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put(Constants.companyKey, requestParams.get(Constants.companyid).toString());
                            params.put(Constants.isExport, Boolean.parseBoolean(requestParams.get(Constants.isExport).toString()));                            
                            if (requestParams.containsKey("browsertimezone") && requestParams.get("browsertimezone") != null) {
                                params.put(Constants.browsertz, requestParams.get("browsertimezone").toString());
                            }
                            /*
                             * To print Combo list and check list value in vendor registry report
                             */
                            params.put("isReturnDropdownCheckListVal", true);
                            fieldmatamanager.addCustomData(variableMap, customFieldMap, customDateFieldMap, newJobj, params);
                        }
                    }
                }
                DataJArr.put(newJobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getOpeningCreditNoteInformation : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }
    
    public JSONArray getMadePaymentsInformation(HashMap<String, Object> requestParams, List<Object []> payments, JSONArray DataJArr) throws SessionExpiredException, ServiceException {
        DateFormat df=(DateFormat)requestParams.get(Constants.df);
        String companyid = "";
        try {
            HashMap hashMap = new HashMap();
            if(requestParams.containsKey(Constants.companyKey)){
                companyid = (String) requestParams.get(Constants.companyKey);
            }
            hashMap.put("companyId", requestParams.get(Constants.companyid).toString());
            hashMap.put("reportId", requestParams.get("reportId").toString());
            hashMap.put("moduleId", Constants.Acc_Make_Payment_ModuleId);
            KwlReturnObject customizeReportResult = accountingHandler.getCustomizeReportViewMappingField(hashMap);
            List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(requestParams.get(Constants.companyid).toString(), Constants.Acc_Make_Payment_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
            FieldMap = accaccountDAO.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            KwlReturnObject custumObjresult = null;
            for(Object obj[]:payments){
                Payment payment = (Payment) obj[0];
                JSONObject newJobj = new JSONObject();
//                newJobj.put("transactionDate", df.format(payment.getJournalEntry().getEntryDate()));
                newJobj.put("transactionDate", df.format(payment.getCreationDate()));
                newJobj.put("documentno", payment.getPaymentNumber());
                newJobj.put("vendorName", payment.getVendor().getName());
                newJobj.put("amount", authHandler.formattedAmount(payment.getDepositamountinbase(), companyid));
                newJobj.put("status", Constants.closedStatus);
                if (payment.getAdvanceDetails() != null && !payment.getAdvanceDetails().isEmpty() && payment.getPaymentWindowType()==1) {
                    for (AdvanceDetail detailOfPayment : payment.getAdvanceDetails()) {
                        if (detailOfPayment.getAmountDue() != 0) {
                            newJobj.put("status", Constants.openStatus);
                            break;
                        }
                    }
                }
                if(!requestParams.get("status").toString().equals("all") && !requestParams.get("status").toString().equals(newJobj.get("status").toString())){
                    continue;
                }
                newJobj.put(Constants.moduleid, Constants.Acc_Make_Payment_ModuleId);
                newJobj.put("isOpeningBalanceTransaction", false);
                if (!customizeReportList.isEmpty()) {
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    custumObjresult = accountingHandler.getObject(AccJECustomData.class.getName(), payment.getJournalEntry().getID());
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        AccJECustomData jeCustomData = (AccJECustomData) custumObjresult.getEntityList().get(0);
                        if (jeCustomData != null) {
                            AccountingManager.setCustomColumnValues(jeCustomData, FieldMap, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put(Constants.companyKey, requestParams.get(Constants.companyid).toString());
                            params.put(Constants.isExport, Boolean.parseBoolean(requestParams.get(Constants.isExport).toString()));                            
                            if (requestParams.containsKey("browsertimezone") && requestParams.get("browsertimezone") != null) {
                                params.put(Constants.browsertz, requestParams.get("browsertimezone").toString());
                            }
                            /*
                             * To print Combo list and check list value in vendor registry report
                             */
                            params.put("isReturnDropdownCheckListVal", true);
                            fieldmatamanager.addCustomData(variableMap, customFieldMap, customDateFieldMap, newJobj, params);
                        }
                    }
                }
                DataJArr.put(newJobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getMadePaymentsInformation : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }
  
  @Override  
    public JSONArray getOpeningMadePaymentsInformation(HashMap<String, Object> requestParams, List<Object []> payments, JSONArray DataJArr) throws SessionExpiredException, ServiceException {
        DateFormat df=(DateFormat)requestParams.get(Constants.df);
        String companyid = "";
        try {
            if(requestParams.containsKey(Constants.companyKey)){
                companyid = (String) requestParams.get(Constants.companyKey);
            }
            HashMap hashMap = new HashMap();
            hashMap.put("companyId", requestParams.get(Constants.companyid).toString());
            hashMap.put("reportId", requestParams.get("reportId").toString());
            hashMap.put("moduleId", Constants.Acc_Make_Payment_ModuleId);
            KwlReturnObject customizeReportResult = accountingHandler.getCustomizeReportViewMappingField(hashMap);
            List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(requestParams.get(Constants.companyid).toString(), Constants.Acc_Make_Payment_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
            FieldMap = accaccountDAO.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            KwlReturnObject custumObjresult = null;
            for(Object obj:payments){
                Payment payment = (Payment) obj;
                JSONObject newJobj = new JSONObject();
                newJobj.put("transactionDate", df.format(payment.getCreationDate()));
                newJobj.put("documentno", payment.getPaymentNumber());
                newJobj.put("vendorName", payment.getVendor().getName());
                newJobj.put("amount", authHandler.formattedAmount(payment.getOriginalOpeningBalanceBaseAmount(), companyid));
                newJobj.put("status", Constants.closedStatus);
                if(payment.getOpeningBalanceAmountDue()!=0){
                    newJobj.put("status", Constants.openStatus);
                }
                if(!requestParams.get("status").toString().equals("all") && !requestParams.get("status").toString().equals(newJobj.get("status").toString())){
                    continue;
                }
                newJobj.put(Constants.moduleid, Constants.Acc_Make_Payment_ModuleId);
                newJobj.put("isOpeningBalanceTransaction", true);
                if (payment.getOpeningBalanceMakePaymentCustomData()!= null && !customizeReportList.isEmpty()) {
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    custumObjresult = accountingHandler.getObject(OpeningBalanceMakePaymentCustomData.class.getName(), payment.getOpeningBalanceMakePaymentCustomData().getOpeningBalanceMakePaymentId());
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        OpeningBalanceMakePaymentCustomData OBPaymentCustomData = (OpeningBalanceMakePaymentCustomData) custumObjresult.getEntityList().get(0);
                        if (OBPaymentCustomData != null) {
                            AccountingManager.setCustomColumnValues(OBPaymentCustomData, FieldMap, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put(Constants.companyKey, requestParams.get(Constants.companyid).toString());
                            params.put(Constants.isExport, Boolean.parseBoolean(requestParams.get(Constants.isExport).toString()));                            
                            if (requestParams.containsKey("browsertimezone") && requestParams.get("browsertimezone") != null) {
                                params.put(Constants.browsertz, requestParams.get("browsertimezone").toString());
                            }
                            /*
                             * To print Combo list and check list value in vendor registry report
                             */
                            params.put("isReturnDropdownCheckListVal", true);
                            fieldmatamanager.addCustomData(variableMap, customFieldMap, customDateFieldMap, newJobj, params);
                        }
                    }
                }
                DataJArr.put(newJobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getOpeningMadePaymentsInformation : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }
    
    public JSONArray getReceivedPaymentsInformation(HashMap<String, Object> requestParams, List<Object []> receipts, JSONArray DataJArr) throws SessionExpiredException, ServiceException {
        DateFormat df=(DateFormat)requestParams.get(Constants.df);
        String companyid = "";
        try {
            if(requestParams.containsKey(Constants.companyKey)){
                companyid = (String) requestParams.get(Constants.companyKey);
            }
            HashMap hashMap = new HashMap();
            hashMap.put("companyId", requestParams.get(Constants.companyid).toString());
            hashMap.put("reportId", requestParams.get("reportId").toString());
            hashMap.put("moduleId", Constants.Acc_Receive_Payment_ModuleId);
            KwlReturnObject customizeReportResult = accountingHandler.getCustomizeReportViewMappingField(hashMap);
            List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(requestParams.get(Constants.companyid).toString(), Constants.Acc_Receive_Payment_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = new HashMap<String, Integer>();
            FieldMap = accaccountDAO.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            KwlReturnObject custumObjresult = null;
            for(Object obj[]:receipts){
                Receipt receipt = (Receipt) obj[0];
                JSONObject newJobj = new JSONObject();
//                newJobj.put("transactionDate", df.format(receipt.getJournalEntry().getEntryDate()));
                newJobj.put("transactionDate", df.format(receipt.getCreationDate()));
                newJobj.put("documentno", receipt.getReceiptNumber());
                KwlReturnObject vendResult = accountingHandler.getObject(Vendor.class.getName(), receipt.getVendor());
                Vendor vendor = (Vendor) vendResult.getEntityList().get(0);
                newJobj.put("vendorName", vendor.getName());
                newJobj.put("amount", authHandler.formattedAmount(receipt.getDepositamountinbase(), companyid));
                newJobj.put("status", Constants.closedStatus);
                if (receipt.getReceiptAdvanceDetails() != null && !receipt.getReceiptAdvanceDetails().isEmpty() && receipt.getPaymentWindowType()==1) {
                    for (ReceiptAdvanceDetail detailOfReceipt : receipt.getReceiptAdvanceDetails()) {
                        if (detailOfReceipt.getAmountDue() != 0) {
                            newJobj.put("status", Constants.openStatus);
                            break;
                        }
                    }
                }
                if(!requestParams.get("status").toString().equals("all") && !requestParams.get("status").toString().equals(newJobj.get("status").toString())){
                    continue;
                }
                newJobj.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                newJobj.put("isOpeningBalanceTransaction", false);
                if (!customizeReportList.isEmpty()) {
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    custumObjresult = accountingHandler.getObject(AccJECustomData.class.getName(), receipt.getJournalEntry().getID());
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        AccJECustomData jeCustomData = (AccJECustomData) custumObjresult.getEntityList().get(0);
                        if (jeCustomData != null) {
                            AccountingManager.setCustomColumnValues(jeCustomData, FieldMap, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put(Constants.companyKey, requestParams.get(Constants.companyid).toString());
                            params.put(Constants.isExport, Boolean.parseBoolean(requestParams.get(Constants.isExport).toString()));     
                            /*
                             * To print Combo list and check list value in vendor registry report
                             */
                            params.put("isReturnDropdownCheckListVal", true);
                            if (requestParams.containsKey("browsertimezone") && requestParams.get("browsertimezone") != null) {
                                params.put(Constants.browsertz, requestParams.get("browsertimezone").toString());
                            }
                            fieldmatamanager.addCustomData(variableMap, customFieldMap, customDateFieldMap, newJobj, params);
                        }
                    }
                }
                DataJArr.put(newJobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getReceivedPaymentsInformation : " + ex.getMessage(), ex);
        }
        return DataJArr;
    }

    @Override
    //Call from Web-application  
    public JSONObject saveVendor(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        boolean isDuplicateNoExe = false;
        boolean isTaxDeactivated = false;
        String vendorID = "", msg = "";
        String sequenceformat = paramJobj.optString(Constants.sequenceformat);
        String sequenceformatCust = paramJobj.optString("sequenceformatvencus");
        int fromCust = Integer.parseInt(paramJobj.optString("fromVenCus"));
        boolean isEdit = StringUtil.isNullOrEmpty(paramJobj.optString("isEdit")) ? false : Boolean.parseBoolean(paramJobj.optString("isEdit"));
        String customerNo = "";
        Customer customer = new Customer();
        int from = paramJobj.optString("from", null) == null ? -1 : Integer.parseInt(paramJobj.optString("from"));
        String vendorid = paramJobj.optString("accid", null);
        String vendorNumber = paramJobj.optString("acccode", null);
        String customerNumber = paramJobj.optString("custorvenacccode").trim();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Vendor_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        int customervendor = 0;
        TransactionStatus status = null;
        String companyid = "";
        Map<String, String> deleteparam = null;
        boolean isAlreadyCustomerMappedVendor=false;
        try {
            companyid = paramJobj.optString(Constants.companyKey);
            boolean mapcustomervendorFlag = paramJobj.optString("mapcustomervendor", null) != null;
            KwlReturnObject count = null;
            CustomerVendorMapping customervendorma = accCusVenMapDAOObj.checkVendorMappingExists(vendorid);
            String customerid = (customervendorma == null) ? "" : customervendorma.getCustomeraccountid().getID();
            deleteparam = new HashMap<String, String>();
            deleteparam.put("vendorno", vendorNumber);
            deleteparam.put("customerno", customerNumber);
            deleteparam.put(Constants.companyKey, companyid);
            if (mapcustomervendorFlag) {
                deleteparam.put("isalsocustomer", paramJobj.optString("mapcustomervendor"));
            }
             if(!StringUtil.isNullOrEmpty(vendorid)){
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("ID",vendorid);
            paramMap.put("company.companyID",  companyid);
            Object prefObject = kwlCommonTablesDAOObj.getRequestedObjectFields(Vendor.class, new String[]{"mapcustomervendor"}, paramMap);
             isAlreadyCustomerMappedVendor= (boolean)prefObject;
            }  //code to check mandatory fields
//            HashMap returnMap= accFieldSetUpServiceDAOObj.getMandatoryFieldsDetails(paramJobj.optString(Constants.moduleid),null, companyid,false,paramJobj);
            
            //Checks duplicate number in edit case
            if (isEdit) {
                if (sequenceformat.equals("NA")) {
                    count = accVendorDAOobj.checkDuplicateVendorForEdit(vendorNumber, companyid, vendorid);
                    if (count.getRecordTotalCount() > 0) {
                        customervendor = Constants.Acc_Vendor_ModuleId;
                        isDuplicateNoExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.vendor.vendorcode", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + vendorNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }
                }
                //Checks duplicate number in case vendor can be customer
                if (mapcustomervendorFlag && sequenceformatCust.equals("NA")) {
                    count = accCustomerDAOobj.checkDuplicateCustomerForEdit(customerNumber, companyid, customerid);
                    if (count.getRecordTotalCount() > 0) {
                        customervendor = Constants.Acc_Customer_ModuleId;
                        isDuplicateNoExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.customer.customercode", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + customerNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }
                }
            } else {//Checks duplicate number in add case
                if (sequenceformat.equals("NA")) {
                    count = accVendorDAOobj.getVendorCount(vendorNumber, companyid);
                    if (count.getRecordTotalCount() > 0) {
                        customervendor = Constants.Acc_Vendor_ModuleId;
                        isDuplicateNoExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.vendor.vendorcode", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + vendorNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }
                }
                if (mapcustomervendorFlag && sequenceformatCust.equals("NA")) {
                    count = accCustomerDAOobj.getCustomerCount(customerNumber, companyid);
                    if (count.getRecordTotalCount() > 0) {
                        customervendor = Constants.Acc_Customer_ModuleId;
                        isDuplicateNoExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.customer.customercode", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + customerNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }
                }
                //Check deactivate tax
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("taxId", null)) && !accaccountDAO.isTaxActivated(companyid, paramJobj.optString("taxId"))) {
                    isTaxDeactivated = true;
                    throw ServiceException.FAILURE(messageSource.getMessage("acc.tax.deactivated.tax.saveAlert", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))), "", false);
                }
            }

            //Checks duplicate number for simultaneous transactions
            synchronized (this) {
                status = txnManager.getTransaction(def);
                KwlReturnObject resultInv = accCommonTablesDAO.getTransactionInTemp(vendorNumber, companyid, Constants.Acc_Vendor_ModuleId);
                if (resultInv.getRecordTotalCount() > 0 && sequenceformat.equals("NA")) {
                    customervendor = Constants.Acc_Vendor_ModuleId;
                    isDuplicateNoExe = true;
                    throw new AccountingException(messageSource.getMessage("acc.vendor.selectedvendorcode", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + vendorNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                } else {
                    accCommonTablesDAO.insertTransactionInTemp(vendorNumber, companyid, Constants.Acc_Vendor_ModuleId);
                }

                if (mapcustomervendorFlag && sequenceformatCust.equals("NA")) {
                    resultInv = accCommonTablesDAO.getTransactionInTemp(customerNumber, companyid, Constants.Acc_Customer_ModuleId);
                    if (resultInv.getRecordTotalCount() > 0) {
                        customervendor = Constants.Acc_Customer_ModuleId;
                        isDuplicateNoExe = true;
                        throw new AccountingException(messageSource.getMessage("acc.customer.selectedcustomercode", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + customerNumber + messageSource.getMessage("acc.field.alreadyinprocess.", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    } else {
                        accCommonTablesDAO.insertTransactionInTemp(customerNumber, companyid, Constants.Acc_Customer_ModuleId);
                    }
                }
                txnManager.commit(status);
            }
            status = txnManager.getTransaction(def);
//            Vendor vendor = saveVendor(request);
            Vendor vendor = saveVendorJSON(paramJobj);
            vendorID = vendor.getID();
            String vendorNo = "";
            CustomerVendorMapping customervendormapping = accCusVenMapDAOObj.checkVendorMappingExists(vendorID);
            String mappingid = (customervendormapping == null) ? "" : customervendormapping.getId();
            if (mapcustomervendorFlag) {
                if (StringUtil.isNullOrEmpty(mappingid)) {
                    paramJobj.put("copyflag", true);
//                    customer = saveCustomer(request); 
                    customer = saveCustomer(paramJobj);
                    JSONObject jobjaccount = new JSONObject();
                    jobjaccount.put("customeraccountid", customer.getID());
                    jobjaccount.put("vendoraccountid", vendorID);
                    jobjaccount.put("mappingflag", true);
                    KwlReturnObject result = accCusVenMapDAOObj.saveUpdateCustomerVendorMapping(jobjaccount);
                } else {
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("accid", customervendormapping.getCustomeraccountid().getID());
                    requestParams.put("mapcustomervendor", true);
                    requestParams.put("acccode", paramJobj.optString("custorvenacccode", null));
                    requestParams.put("accountid", paramJobj.optString("mappingcusaccid", null));
                    accCustomerDAOobj.updateCustomer(requestParams);

                    JSONObject jobjaccount = new JSONObject();
                    jobjaccount.put("id", mappingid);
                    jobjaccount.put("mappingflag", true);
                    KwlReturnObject result = accCusVenMapDAOObj.saveUpdateCustomerVendorMapping(jobjaccount);
                }

            } else {
                if (!StringUtil.isNullOrEmpty(mappingid)) {
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("accid", customervendormapping.getCustomeraccountid().getID());
                    requestParams.put("mapcustomervendor", false);
                    accCustomerDAOobj.updateCustomer(requestParams);

                    JSONObject jobjaccount = new JSONObject();
                    jobjaccount.put("id", mappingid);
                    jobjaccount.put("mappingflag", false);
                    KwlReturnObject result = accCusVenMapDAOObj.saveUpdateCustomerVendorMapping(jobjaccount);
                }

            }
            txnManager.commit(status);
            status = null;
            TransactionStatus AutoNoStatus = null;
            try {
                synchronized (this) {
                    DefaultTransactionDefinition def1 = new DefaultTransactionDefinition();
                    def1.setName("AutoNum_Tx");
                    def1.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
                    AutoNoStatus = txnManager.getTransaction(def1);

                    if (!StringUtil.isNullOrEmpty(sequenceformat) && !sequenceformat.equalsIgnoreCase("NA") && StringUtil.isNullOrEmpty(vendorid) && !isEdit) {
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, from, sequenceformat, false, vendor.getCreatedOn());
                        seqNumberMap.put(Constants.DOCUMENTID, vendorID);
                        seqNumberMap.put(Constants.companyKey, companyid);
                        seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformat);
                        vendorNo = accVendorDAOobj.updateVendorNumber(seqNumberMap);
                    }
                    if (mapcustomervendorFlag) {
                        if (StringUtil.isNullOrEmpty(mappingid)) {
                            if ((!StringUtil.isNullOrEmpty(sequenceformatCust) && !sequenceformatCust.equalsIgnoreCase("NA") && !isEdit)|| (!StringUtil.isNullOrEmpty(sequenceformatCust) && !sequenceformatCust.equalsIgnoreCase("NA") && !isAlreadyCustomerMappedVendor)) {
                                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, fromCust, sequenceformatCust, false, customer.getCreatedOn());
                                seqNumberMap.put(Constants.DOCUMENTID, customer.getID());
                                seqNumberMap.put(Constants.companyKey, companyid);
                                seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformatCust);
                                customerNo = accCustomerDAOobj.updateCustomerNumber(seqNumberMap);
                            }
                        }
                    }
                    txnManager.commit(AutoNoStatus);
                }
            } catch (Exception ex) {
                if (AutoNoStatus != null) {
                    txnManager.rollback(AutoNoStatus);
                }
                //Delete entries in temporary table
                deleteEntryInTemp(deleteparam);
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
            }

            //*****************************************Propagate customer In child companies**************************
            String auditID = "";
            boolean propagateTOChildCompaniesFalg = false;
            boolean isPropagatedPersonalDetails = false;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("ispropagatetochildcompanyflag", null))) {
                propagateTOChildCompaniesFalg = Boolean.parseBoolean(paramJobj.optString("ispropagatetochildcompanyflag"));
            }
            Map<String, Object> insertLogParams = new HashMap<String, Object>();
            insertLogParams.put(Constants.reqHeader, (paramJobj.has(Constants.reqHeader) && paramJobj.get(Constants.reqHeader) != null) ? paramJobj.optString(Constants.reqHeader) : paramJobj.optString(Constants.remoteIPAddress));
            insertLogParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
            insertLogParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
            if (propagateTOChildCompaniesFalg) {
                try {
                    String parentcompanyid = companyid;
                    Map<String, Object> parentdataMap = new HashMap<>();
//                    Map<String, Object> requestMap = request.getParameterMap();
                    Map<String, Object> requestMap = (Map<String, Object>) paramJobj.get("requestMap");
                    Set set = requestMap.entrySet();
                    for (Object obj : set) {
                        Map.Entry<String, Object> entry = (Map.Entry<String, Object>) obj;
                        String[] value = (String[]) entry.getValue();
                        parentdataMap.put(entry.getKey(), value[0]);
                    }
                    String parentCompanyVendorID = vendor.getID();
                    parentdataMap.put("parentCompanyVendorID", parentCompanyVendorID);
                    List childCompaniesList = companyDetailsDAOObj.getChildCompanies(parentcompanyid);
                    String childCompanyName = "";

                    if (!isEdit) {
                        auditID = AuditAction.VENDOR_ADDED;
                        for (Object childObj : childCompaniesList) {

                            try {
                                status = txnManager.getTransaction(def);
                                Object[] childdataOBj = (Object[]) childObj;
                                String childCompanyID = (String) childdataOBj[0];
                                childCompanyName = (String) childdataOBj[1];
//                                saveVendorInChildCompanies(request, isEdit, parentdataMap, parentcompanyid, childCompanyID);
                                saveVendorInChildCompanies(paramJobj, isEdit, parentdataMap, parentcompanyid, childCompanyID);
                                isPropagatedPersonalDetails = true;
                                txnManager.commit(status);
                                status = null;
//                                auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.optString(Constants.userfullname)  + " has propagated(added) vendor " + vendor.getName()+" ( "+vendor.getAcccode()+" ) "+ " to child company " + childCompanyName, request, vendor.getID());
                                auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.optString(Constants.userfullname) + " has propagated(added) vendor " + vendor.getName() + " ( " + vendor.getAcccode() + " ) " + " to child company " + childCompanyName, insertLogParams, vendor.getID());
                            } catch (Exception ex) {
                                txnManager.rollback(status);
//                                auditTrailObj.insertAuditLog(auditID, "Vendor " +  vendor.getName()+" ( "+vendor.getAcccode()+" ) "+ " could not be propagated(added) to child company " + childCompanyName, request, vendor.getID());
                                auditTrailObj.insertAuditLog(auditID, "Vendor " + vendor.getName() + " ( " + vendor.getAcccode() + " ) " + " could not be propagated(added) to child company " + childCompanyName, insertLogParams, vendor.getID());
                            }
                        }
                    } else {
                        auditID = AuditAction.VENDOR_UPDATED;
                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
                        requestParams.put("parentCompanyVendorID", parentCompanyVendorID);
                        KwlReturnObject result = accVendorDAOobj.getChildVendors(requestParams);
                        List childCompaniesVendorList = result.getEntityList();
                        for (Object childObj : childCompaniesVendorList) {
                            try {
                                Vendor vend = (Vendor) childObj;
                                if (vend != null) {
                                    status = txnManager.getTransaction(def);
                                    String childcompanysVendorid = vend.getID();
                                    String childCompanyID = vend.getCompany().getCompanyID();
                                    childCompanyName = vend.getCompany().getSubDomain();
                                    parentdataMap.put("childvendorrid", childcompanysVendorid);
//                                    saveVendorInChildCompanies(request, isEdit, parentdataMap, parentcompanyid, childCompanyID);
                                    saveVendorInChildCompanies(paramJobj, isEdit, parentdataMap, parentcompanyid, childCompanyID);
                                    isPropagatedPersonalDetails = true;
                                    txnManager.commit(status);
                                    status = null;
//                                    auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has propagated(updated) vendor " + vendor.getName()+" ( "+vendor.getAcccode()+" ) " + " to child company " + childCompanyName, request, vendor.getID());
                                    auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.optString(Constants.userfullname) + " has propagated(updated) vendor " + vendor.getName() + " ( " + vendor.getAcccode() + " ) " + " to child company " + childCompanyName, insertLogParams, vendor.getID());
                                }
                            } catch (Exception ex) {
                                txnManager.rollback(status);
//                                auditTrailObj.insertAuditLog(auditID, "Vendor " +  vendor.getName()+" ( "+vendor.getAcccode()+" ) " + " could not be propagated(udated) to child company " + childCompanyName, request, vendor.getID());
                                auditTrailObj.insertAuditLog(auditID, "Vendor " + vendor.getName() + " ( " + vendor.getAcccode() + " ) " + " could not be propagated(udated) to child company " + childCompanyName, insertLogParams, vendor.getID());
                            }
                        }

                    }
                    jobj.put("isPropagatedPersonalDetails", isPropagatedPersonalDetails);
                } catch (Exception ex) {
                    if (status != null) {
                        txnManager.rollback(status);
//                        auditTrailObj.insertAuditLog(auditID, "Vendor " +  vendor.getName()+" ( "+vendor.getAcccode()+" ) " + " could not be propagated(added) to child company " + vendor.getCompany().getSubDomain(), request, vendor.getID());
                        auditTrailObj.insertAuditLog(auditID, "Vendor " + vendor.getName() + " ( " + vendor.getAcccode() + " ) " + " could not be propagated(added) to child company " + vendor.getCompany().getSubDomain(), insertLogParams, vendor.getID());
                    }
                }
            }
            //*****************************************Propagate customer In child companies**************************
            issuccess = true;
            msg = messageSource.getMessage("acc.ven.save", null, Locale.forLanguageTag(paramJobj.getString(Constants.language)));   //"Vendor information has been saved successfully";
            status = txnManager.getTransaction(def);
            //Delete entries in temporary table
            deleteEntryInTemp(deleteparam);
            txnManager.commit(status);
        } catch (AccountingException ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            //Delete entries in temporary table
            deleteEntryInTemp(deleteparam);
            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } //        catch (SessionExpiredException ex) {
        //            if (status != null) {
        //                txnManager.rollback(status);
        //            }
        //            //Delete entries in temporary table
        //            deleteEntryInTemp(deleteparam);
        //            msg = ex.getMessage();
        //            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        //        }
        catch (Exception ex) {
            if (status != null) {
                txnManager.rollback(status);
            }
            //Delete entries in temporary table
            deleteEntryInTemp(deleteparam);
            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                jobj.put("perAccID", vendorID);
                jobj.put("isDuplicateExe", isDuplicateNoExe);
                jobj.put("customervendor", customervendor);
                jobj.put("isTaxDeactivated", isTaxDeactivated);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }

    public Vendor saveVendorJSON(JSONObject paramJobj) throws ServiceException, SessionExpiredException, AccountingException {
        Vendor vendor = null;
        String auditMsg = "", auditID = "";
        try {
            String companyid = paramJobj.optString(Constants.companyKey);
            boolean mapcustomervendorFlag = !StringUtil.isNullOrEmpty(paramJobj.optString("mapcustomervendor", null));//true when copy vendor checkbox is checked in customer creation.
            String vendorid = paramJobj.optString("accid", null);
            String vendorNumber = paramJobj.optString("acccode", null);
            String sequenceformat = paramJobj.optString(Constants.sequenceformat, null);
            boolean isEdit = StringUtil.isNullOrEmpty(paramJobj.optString("isEdit", null)) ? false : Boolean.parseBoolean(paramJobj.optString("isEdit"));
            int from = paramJobj.optString("from", null) == null ? -1 : Integer.parseInt(paramJobj.optString("from"));
            String accountName = paramJobj.optString("accname", null);
            String aliasname = paramJobj.optString("aliasname", null);
            String parentName = paramJobj.optString("parentname", null);
            String customfield = paramJobj.optString("customfield", null);
            String taxIDNumber = paramJobj.optString("taxidnumber", null);
            String mappingaccid = paramJobj.optString("mappingvenaccid", null);
            String contactperson = (paramJobj.optString("contactperson", null) == null) ? "" : paramJobj.optString("contactperson");
            String currencyid = (paramJobj.optString("currencyid", null) == null ? paramJobj.optString(Constants.globalCurrencyKey) : paramJobj.optString("currencyid"));
            boolean intercompanyflag = paramJobj.optString("intercompanyflag", null) != null;
            String intercompanytype = paramJobj.optString("intercompanytype", null);
            String natureOfPayment = StringUtil.isNullOrEmpty(paramJobj.optString("natureofpayment", null)) ? "" : paramJobj.optString("natureofpayment");
            boolean isTDSapplicableonvendor = StringUtil.isNullOrEmpty(paramJobj.optString("isTDSapplicableonvendor", null)) ? false : Boolean.parseBoolean(paramJobj.optString("isTDSapplicableonvendor"));
            boolean considerExemptLimit = StringUtil.isNullOrEmpty(paramJobj.optString("considerExemptLimit", null)) ? false : Boolean.parseBoolean(paramJobj.optString("considerExemptLimit"));
            String tdsInterestPayableAccount = StringUtil.isNullOrEmpty(paramJobj.optString("tdsinterestpayableaccountid", null)) ? "" : paramJobj.optString("tdsinterestpayableaccountid");
            String taxId = "";
            String mappingReceivedFromId = "";
            boolean autogen = false;
            String defaultAgentMapping = "";

            boolean isActivateIBG = false;
            boolean DBSbank = false;         // For DBS bank
            boolean CIMBbank = false;        // For CIMB bank
            boolean isvendoravailabletoagent = false;
            boolean vendorForTDSbeforeEdit = isTDSapplicableonvendor; 
            if(!StringUtil.isNullOrEmpty(vendorid)){
                 KwlReturnObject kwlVendorTDS = null;   
                 kwlVendorTDS = accountingHandler.getObject(Vendor.class.getName(),vendorid);
                 Vendor vendorForTDS = (Vendor) kwlVendorTDS.getEntityList().get(0);
                 vendorForTDSbeforeEdit = vendorForTDS!=null?vendorForTDS.isIsTDSapplicableonvendor():isTDSapplicableonvendor;
            }
            
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("isvendoravailabletoagent", null))) {
                isvendoravailabletoagent = Boolean.parseBoolean(paramJobj.optString("isvendoravailabletoagent"));
            }

            if (!StringUtil.isNullOrEmpty(paramJobj.optString("mapreceivedfrom", null))) {
                mappingReceivedFromId = paramJobj.optString("mapreceivedfrom");
            }

            if (!StringUtil.isNullOrEmpty(paramJobj.optString("activateIBG", null))) {
                isActivateIBG = Boolean.parseBoolean(paramJobj.optString("activateIBG"));
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("DBSbank", null))) {
                    DBSbank = Boolean.parseBoolean(paramJobj.optString("DBSbank"));
                }
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("CIMBbank", null))) {
                    CIMBbank = Boolean.parseBoolean(paramJobj.optString("CIMBbank"));
                }
            }

            JSONObject ibgDetailsObj = new JSONObject();

            if (isActivateIBG && !StringUtil.isNullOrEmpty(paramJobj.optString("ibgReceivingDetails", null))) {
                ibgDetailsObj = new JSONObject(paramJobj.optString("ibgReceivingDetails"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("defaultagentmapping", null))) {
                defaultAgentMapping = paramJobj.optString("defaultagentmapping");
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("taxId", null))) {
                taxId = paramJobj.optString("taxId", null);
            }
            boolean issub = paramJobj.optString("issub", null) != null;
            boolean copyflag = paramJobj.optString("copyflag", null) != null;//true when copy vendor checkbox is checked in customer creation.
            boolean debitType = StringUtil.getBoolean(paramJobj.optString("debitType"));
            debitType = (copyflag) ? !debitType : debitType;
            boolean taxEligible = StringUtil.getBoolean(paramJobj.optString("taxeligible"));
            double openBalance = StringUtil.getDouble(paramJobj.optString("openbalance"));
            openBalance = debitType ? openBalance : -openBalance;
            String parentid = (copyflag) ? null : paramJobj.optString("parentid", null);
            if (!issub) {
                KwlReturnObject accResult = accaccountDAO.getSundryAccount(companyid, true);
                if (accResult.getEntityList().size() > 0 && accResult.getEntityList().get(0) != null) {
                    parentid = (String) accResult.getEntityList().get(0);
                } else {
                    parentid = null;
                }
            }
            DateFormat df = authHandler.getDateOnlyFormat();
            double life = StringUtil.getDouble(paramJobj.optString("life"));
            double salvage = StringUtil.getDouble(paramJobj.optString("salvage"));
            //Convert New Date into User's Timezone
            String creationdate = authHandler.getDateFormatter(paramJobj).format(new Date());
            Date createdate = authHandler.getDateOnlyFormat().parse(creationdate);
            Date creationDate = paramJobj.optString("creationDate", null) == null ? createdate : authHandler.getDateOnlyFormat().parse(paramJobj.optString("creationDate"));
            int paymentCriteria = (StringUtil.isNullOrEmpty(paramJobj.optString("paymentCriteria", null)) ? 1 : Integer.parseInt(paramJobj.optString("paymentCriteria")));  // paymentCriteria = '1' - NA; '2'-LIFO; '3'-FIFo
            String minpricevalueforvendor = (StringUtil.isNullOrEmpty(paramJobj.optString("minpricevalueforvendor", null)) ? "" : (paramJobj.optString("minpricevalueforvendor")));
            String pricingBand = (StringUtil.isNullOrEmpty(paramJobj.optString("pricingBand", null)) ? "" : paramJobj.optString("pricingBand"));  // to set vendor specific pricingBand
            boolean interStateParty = !StringUtil.isNullOrEmpty(paramJobj.optString("interstateparty", null));
            boolean cFromApplicable = !StringUtil.isNullOrEmpty(paramJobj.optString("cformapplicable", null));
            boolean gtaapplicable = !StringUtil.isNullOrEmpty(paramJobj.optString("gtaapplicable", null));
            Date vatRegDate = StringUtil.isNullOrEmpty(paramJobj.optString("vatregdate", null)) ? null : authHandler.getDateOnlyFormat().parse(paramJobj.optString("vatregdate"));
            Date cstRegDate = StringUtil.isNullOrEmpty(paramJobj.optString("cstregdate", null)) ? null : authHandler.getDateOnlyFormat().parse(paramJobj.optString("cstregdate"));
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("accid", vendorid);
            if (sequenceformat.equals("NA") || isEdit) {
                requestParams.put("acccode", vendorNumber);
            } else {
                requestParams.put("acccode", "");
            }
            requestParams.put("accname", accountName);
            requestParams.put("aliasname", aliasname);
            requestParams.put("parentid", parentid);
            requestParams.put("defaultagentmapping", defaultAgentMapping);
            requestParams.put("issub", paramJobj.optString("issub", null));
            requestParams.put("debitType", paramJobj.optString("debitType", null));
            requestParams.put("openbalance", paramJobj.optString("openbalance", null));
            requestParams.put("title", paramJobj.optString("title", null));
            requestParams.put("bankaccountno", paramJobj.optString("bankaccountno", null));
            requestParams.put("termid", paramJobj.optString("termid", null));
            requestParams.put("other", paramJobj.optString("other", null));
            requestParams.put("taxidmailon", paramJobj.optString("taxidmailon", null));
            requestParams.put(Constants.companyKey, companyid);
            requestParams.put("taxeligible", taxEligible);
            requestParams.put("taxidnumber", taxIDNumber);
            requestParams.put("debitLimit", paramJobj.optString("limit", null));
            requestParams.put("contactperson", contactperson);
            requestParams.put("mapcustomervendor", mapcustomervendorFlag);
            requestParams.put("mappingReceivedFromId", mappingReceivedFromId);
            requestParams.put("mappingPaidTo", (!StringUtil.isNullOrEmpty(paramJobj.optString("mappingPaidTo", null)) ? paramJobj.optString("mappingPaidTo") : ""));
            requestParams.put("taxId", taxId);
            requestParams.put("creationDate", creationDate);
            requestParams.put("uenno", paramJobj.optString("uenno", null));
            requestParams.put("vattinno", !StringUtil.isNullOrEmpty(paramJobj.optString("vattinno", null)) ? paramJobj.optString("vattinno") : "");
            requestParams.put("csttinno", !StringUtil.isNullOrEmpty(paramJobj.optString("csttinno", null)) ? paramJobj.optString("csttinno") : "");
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("companycountry", null)) && paramJobj.optString("companycountry").equals("106")) {
                //NPWP no. is only for Indonesia but in backend it is saved as PAN NO.
                requestParams.put("panno", !StringUtil.isNullOrEmpty(paramJobj.optString("npwp", null)) ? paramJobj.optString("npwp") : "");
            } else {
                requestParams.put("panno", !StringUtil.isNullOrEmpty(paramJobj.optString("panno", null)) ? paramJobj.optString("panno") : "");
            }
            requestParams.put("vendorbranch", !StringUtil.isNullOrEmpty(paramJobj.optString("vendorbranch", null)) ? paramJobj.optString("vendorbranch") : "");
            requestParams.put("servicetaxno", !StringUtil.isNullOrEmpty(paramJobj.optString("servicetaxno", null)) ? paramJobj.optString("servicetaxno") : "");
            requestParams.put("tanno", !StringUtil.isNullOrEmpty(paramJobj.optString("tanno", null)) ? paramJobj.optString("tanno") : "");
            requestParams.put("eccno", !StringUtil.isNullOrEmpty(paramJobj.optString("eccno", null)) ? paramJobj.optString("eccno") : "");
            requestParams.put("deducteetype", !StringUtil.isNullOrEmpty(paramJobj.optString("deducteeTypeId", null)) ? paramJobj.optString("deducteeTypeId") : "");
            requestParams.put("deducteeCode", !StringUtil.isNullOrEmpty(paramJobj.optString("deducteeCode", null)) ? paramJobj.optString("deducteeCode") : "");
            requestParams.put("residentialstatus", !StringUtil.isNullOrEmpty(paramJobj.optString("residentialstatus", null)) ? Integer.parseInt(paramJobj.optString("residentialstatus")) : 0); //By Default 0-residensial  
            requestParams.put("incometaxno", !StringUtil.isNullOrEmpty(paramJobj.optString("itno", null)) ? paramJobj.optString("itno") : "");
            requestParams.put("panstatus", !StringUtil.isNullOrEmpty(paramJobj.optString("panStatusId", null)) ? paramJobj.optString("panStatusId") : "");
            requestParams.put("natureOfPayment", natureOfPayment);
            requestParams.put("tdsInterestPayableAccount", tdsInterestPayableAccount);
            requestParams.put("intercompanyflag", intercompanyflag);
            requestParams.put("intercompanytype", intercompanytype);
            requestParams.put("currencyid", currencyid);
            requestParams.put("isActivateIBG", isActivateIBG);
            requestParams.put("paymentCriteria", paymentCriteria);
            requestParams.put("companyRegistrationNumber", paramJobj.optString("companyRegistrationNumber", null));
            requestParams.put("gstRegistrationNumber", paramJobj.optString("gstRegistrationNumber", null));
            requestParams.put("rmcdApprovalNumber", paramJobj.optString("rmcdApprovalNumber", null));
            requestParams.put("pricingBand", pricingBand);
            requestParams.put("minpricevalueforvendor", minpricevalueforvendor);
            requestParams.put("isvendoravailabletoagent", isvendoravailabletoagent);
            requestParams.put("interstateparty", interStateParty);
            requestParams.put("isTDSapplicableonvendor", isTDSapplicableonvendor);
            requestParams.put("considerExemptLimit", considerExemptLimit);
            requestParams.put("cformapplicable", cFromApplicable);
            requestParams.put("gtaapplicable", gtaapplicable);
            requestParams.put("dealertype", !StringUtil.isNullOrEmpty(paramJobj.optString("dealertype", null)) ? paramJobj.optString("dealertype") : "");
            requestParams.put("vatregdate", vatRegDate);
            /*
             * CST Registration date in Vendor Master required for Form 402
             */
            requestParams.put("cstregdate", cstRegDate);
            requestParams.put("defaultnatureofpurchase", !StringUtil.isNullOrEmpty(paramJobj.optString("defaultnatureofpurchase", null)) ? paramJobj.optString("defaultnatureofpurchase") : "");
            requestParams.put("importereccno", !StringUtil.isNullOrEmpty(paramJobj.optString("importereccno", null)) ? paramJobj.optString("importereccno") : "");
            requestParams.put("iecno", !StringUtil.isNullOrEmpty(paramJobj.optString("iecno", null)) ? paramJobj.optString("iecno") : "");
            requestParams.put("range", !StringUtil.isNullOrEmpty(paramJobj.optString("range", null)) ? paramJobj.optString("range") : "");
            requestParams.put("division", !StringUtil.isNullOrEmpty(paramJobj.optString("division", null)) ? paramJobj.optString("division") : "");
            requestParams.put("commissionerate", !StringUtil.isNullOrEmpty(paramJobj.optString("commissionerate")) ? paramJobj.optString("commissionerate") : "");
            requestParams.put("manufacturerType", !StringUtil.isNullOrEmpty(paramJobj.optString("manufacturerType", null)) ? paramJobj.optString("manufacturerType") : "");
            /*
             * For India Compliace - GST related fields - START
             */
            requestParams.put("gstin", !StringUtil.isNullOrEmpty(paramJobj.optString("gstin", null)) ? paramJobj.optString("gstin") : "");
            requestParams.put("GSTINRegistrationTypeId", paramJobj.optString("GSTINRegistrationTypeId", null));
            requestParams.put("CustomerVendorTypeId", paramJobj.optString("CustomerVendorTypeId", null));
            /*
             * For India Compliace - GST related fields - END
             */

            // For India Compliace TDS-- Start
            double DTAASpeialRate = 0.0;
            double higherTDSRate = 0.0;
            double lowerRate = 0.0;
            String nonLowerDedutionApplicable = "";
            String deductionReason = "";
            String certificateNo = "";
            Date deductionFromDate = null;
            Date deductionToDate = null;
            Date dtaaFromDate = null;
            Date dtaaToDate = null;
            String declareRefNo = "";
            String dtaaApplicable = "";
            KwlReturnObject comp = accountingHandler.getObject(Company.class.getName(), companyid);
            Company company = (Company) comp.getEntityList().get(0);
            if (company.getCountry() != null && Integer.parseInt(company.getCountry().getID()) == Constants.indian_country_id) {
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("residentialstatus", null)) && Integer.parseInt(paramJobj.optString("residentialstatus")) == 1) {
                    dtaaApplicable = !StringUtil.isNullOrEmpty(paramJobj.optString("DTAAApplicable", null)) ? paramJobj.optString("DTAAApplicable") : "";
                    if (!StringUtil.isNullOrEmpty(paramJobj.optString("DTAAApplicable", null)) && paramJobj.optString("DTAAApplicable").equals("1")) {
                        dtaaFromDate = StringUtil.isNullOrEmpty(paramJobj.optString("DTAAFromDate", null)) ? null : authHandler.getDateOnlyFormat().parse(paramJobj.optString("DTAAFromDate"));
                        dtaaToDate = StringUtil.isNullOrEmpty(paramJobj.optString("DTAAToDate", null)) ? null : authHandler.getDateOnlyFormat().parse(paramJobj.optString("DTAAToDate"));
                        DTAASpeialRate = (StringUtil.isNullOrEmpty(paramJobj.optString("DTAASpecialRate", null)) ? 0.0 : Double.parseDouble(paramJobj.optString("DTAASpecialRate")));
                    }
                } else {
                    if (!StringUtil.isNullOrEmpty(paramJobj.optString("panno", null))) {
                        if (!StringUtil.isNullOrEmpty(paramJobj.optString("nonLowerDedutionApplicable", null))) {
                            nonLowerDedutionApplicable = paramJobj.optString("nonLowerDedutionApplicable", null);
                            if (Integer.parseInt(paramJobj.optString("nonLowerDedutionApplicable")) == 1) {
                                deductionReason = paramJobj.optString("deductionReason", null);
                                if (!StringUtil.isNullOrEmpty(paramJobj.optString("deductionReason", null)) && Integer.parseInt(paramJobj.optString("deductionReason")) == 1) {
                                    certificateNo = StringUtil.isNullOrEmpty(paramJobj.optString("CertificateNo", null)) ? "" : paramJobj.optString("CertificateNo");
                                    deductionFromDate = StringUtil.isNullOrEmpty(paramJobj.optString("certiFromDate", null)) ? null : authHandler.getDateOnlyFormat().parse(paramJobj.optString("certiFromDate"));
                                    deductionToDate = StringUtil.isNullOrEmpty(paramJobj.optString("certiToDate", null)) ? null : authHandler.getDateOnlyFormat().parse(paramJobj.optString("certiToDate"));
                                    lowerRate = (StringUtil.isNullOrEmpty(paramJobj.optString("lowerRate", null)) ? 0.0 : Double.parseDouble(paramJobj.optString("lowerRate")));
                                } else if (!StringUtil.isNullOrEmpty(paramJobj.optString("deductionReason", null)) && Integer.parseInt(paramJobj.optString("deductionReason")) == 2) {
                                    deductionFromDate = StringUtil.isNullOrEmpty(paramJobj.optString("declareFromDate", null)) ? null : authHandler.getDateOnlyFormat().parse(paramJobj.optString("declareFromDate"));
                                    deductionToDate = StringUtil.isNullOrEmpty(paramJobj.optString("declareToDate", null)) ? null : authHandler.getDateOnlyFormat().parse(paramJobj.optString("declareToDate"));
                                    declareRefNo = StringUtil.isNullOrEmpty(paramJobj.optString("declareRefNo", null)) ? "" : paramJobj.optString("declareRefNo");
                                } else if (!StringUtil.isNullOrEmpty(paramJobj.optString("deductionReason", null)) && Integer.parseInt(paramJobj.optString("deductionReason")) == 3) {
                                    deductionFromDate = StringUtil.isNullOrEmpty(paramJobj.optString("transportFromDate", null)) ? null : authHandler.getDateOnlyFormat().parse(paramJobj.optString("transportFromDate"));
                                    deductionToDate = StringUtil.isNullOrEmpty(paramJobj.optString("transportToDate", null)) ? null : authHandler.getDateOnlyFormat().parse(paramJobj.optString("transportToDate"));
                                    declareRefNo = StringUtil.isNullOrEmpty(paramJobj.optString("transportRefNo", null)) ? "" : paramJobj.optString("transportRefNo");
                                }
                            }
                        }
                    } else {
                        higherTDSRate = (StringUtil.isNullOrEmpty(paramJobj.optString("higherRate", null)) ? 0.0 : Double.parseDouble(paramJobj.optString("higherRate")));
                    }
                }
            }
            requestParams.put("dtaaApplicable", dtaaApplicable);
            requestParams.put("dtaaFromDate", dtaaFromDate);
            requestParams.put("dtaaToDate", dtaaToDate);
            requestParams.put("certificateNo", certificateNo);
            requestParams.put("deductionFromDate", deductionFromDate);
            requestParams.put("deductionToDate", deductionToDate);
            requestParams.put("declareRefNo", declareRefNo);
            requestParams.put("nonLowerDedutionApplicable", nonLowerDedutionApplicable);
            requestParams.put("deductionReason", deductionReason);
            requestParams.put("dtaaSpecialRate", DTAASpeialRate);
            requestParams.put("lowerRate", lowerRate);
            requestParams.put("higherTDSRate", higherTDSRate);
            // For India Compliace TDS-- End

            if (!StringUtil.isNullOrEmpty(paramJobj.optString("selfBilledFromDate", null))) {
                requestParams.put("selfBilledFromDate", df.parse(paramJobj.optString("selfBilledFromDate")));
            }

            if (!StringUtil.isNullOrEmpty(paramJobj.optString("sezfromdate", null))) {
                requestParams.put("sezfromdate", df.parse(paramJobj.optString("sezfromdate")));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("seztodate", null))) {
                requestParams.put("seztodate", df.parse(paramJobj.optString("seztodate")));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("gstVerifiedDate", null))) {
                requestParams.put("gstVerifiedDate", df.parse(paramJobj.optString("gstVerifiedDate")));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("selfBilledToDate", null))) {
                requestParams.put("selfBilledToDate", df.parse(paramJobj.optString("selfBilledToDate")));
            }
            if (!StringUtil.isNullOrEmpty(sequenceformat) && sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Vendor_ModuleId, vendorNumber, companyid);
                if (!resultList.isEmpty()) {
                    boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                    String formatName = (String) resultList.get(1);
                    if (!isvalidEntryNumber) {
                        throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + vendorNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, Locale.forLanguageTag(paramJobj.getString(Constants.language))));
                    }
                }
            }
            autogen = !sequenceformat.equalsIgnoreCase("NA") ? true : false;
            requestParams.put("autogenerated", autogen);

            KwlReturnObject result = null;
            boolean isgstdetailsupdated=paramJobj.optBoolean("isgstdetailsupdated",false);
            if (StringUtil.isNullOrEmpty(vendorid)) {
                requestParams.put("accountid", mappingaccid);
                result = accVendorDAOobj.addVendor(requestParams);
                auditMsg = " added new vendor ";
                auditID = AuditAction.VENDOR_ADDED;
                isgstdetailsupdated=true;
            } else {
                if (isChildorGrandChildForVendor(vendorid, parentid)) {
                    throw new AccountingException("\"" + accountName + "\" is a parent of \"" + parentName + "\" so can't set \"" + parentName + "\" as a parent.");
                }
//              
                requestParams.put("accountid", mappingaccid);
                result = accVendorDAOobj.updateVendor(requestParams);
                auditMsg = " updated vendor ";
                auditID = AuditAction.VENDOR_UPDATED;
            }

            List ll = result.getEntityList();
            vendor = (Vendor) ll.get(0);
            /**
             * Save Vendor GST history.
             */
            if (vendor.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id) && isgstdetailsupdated) {
                String gstapplieddate = paramJobj.optString("gstapplieddate", null);
                Date applyDate = df.parse(gstapplieddate);
                requestParams.put("applyDate", applyDate);
                requestParams.put("vendorid", vendor.getID());
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
                paramJobj.put(Constants.vendorid, vendor.getID());
                paramJobj.put(Constants.VendorName, vendor.getName());
                saveVendorGSTHistoryAuditTrail(paramJobj);
                
                accVendorDAOobj.saveGstVendorHistory(requestParams);
            }
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_Vendor_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_VendorId);
                customrequestParams.put("modulerecid", vendor.getID());
                customrequestParams.put(Constants.moduleid, Constants.Acc_Vendor_ModuleId);
                customrequestParams.put(Constants.companyKey, companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_Vendor_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    requestParams.put("accid", vendor.getID());
                    requestParams.put("accvendorcustomdataref", vendor.getID());
                    KwlReturnObject accresult = accVendorDAOobj.updateVendor(requestParams);
                }
            }


            String vendorID = vendor.getID();
            String[] vendorCategory = null;
            if (paramJobj.optString("category", null) != null) {
                vendorCategory = paramJobj.optString("category").split(",");
            }

            if (!StringUtil.isNullOrEmpty(vendorID)) {
                accVendorDAOobj.deleteVendorCategoryMappingDtails(vendorID);
            }

            if (vendorCategory != null) {
                for (int j = 0; j < vendorCategory.length; j++) {
                    if (!StringUtil.isNullOrEmpty(vendorID) && !StringUtil.isNullOrEmpty(vendorCategory[j])) {
                        accVendorDAOobj.saveVendorCategoryMapping(vendorID, vendorCategory[j]);
                    }
                }
            }
            //for vendor and agent mapping
            if (!StringUtil.isNullOrEmpty(vendorid)) {
                accVendorDAOobj.deleteVendorAgentMapping(vendorid);
            }
            String[] agents = null;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("agentmapping", null))) {
                agents = paramJobj.optString("agentmapping").split(",");
                if (agents.length > 0) {
                    accVendorDAOobj.saveVendorAgentMapping(vendor, agents);
                }
            }
            //for product mapping--Neeraj D
            if (paramJobj.optString("productmapping", null) != null) {
                if (!StringUtil.isNullOrEmpty(vendorid)) {
                    accVendorCustomerProductDAOobj.deleteVendorProductMapped(vendorid,null);
                }
                String[] productMapping = paramJobj.optString("productmapping").split(",");
                JSONArray jArray = null;
                JSONObject job = null;

                if (!StringUtil.isNullOrEmpty(paramJobj.optString("customJSONString", null))) {
                    jArray = new JSONArray(paramJobj.optString("customJSONString"));
                }
                if (productMapping != null) {
                    for (int j = 0; j < productMapping.length; j++) {
                        String jsonString = "";
                        if (jArray != null && jArray.length() > 0) {
                            for (int cnt = 0; cnt < jArray.length(); cnt++) {
                                job = jArray.getJSONObject(cnt);
                                if (job.has(productMapping[j])) {
                                    // Save Custom data Json of that product 
                                    jsonString = job.get(productMapping[j]).toString();
                                    break;
                                }
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(vendorID) && !StringUtil.isNullOrEmpty(productMapping[j])) {
                            accVendorCustomerProductDAOobj.saveVendorProductMapping(vendorID, productMapping[j], jsonString);
                        }
                    }
                }
            }
            // Save IBG Detail For Vendor
            if (isActivateIBG && ibgDetailsObj != null) {

                HashMap<String, Object> dataParams = new HashMap<String, Object>();
                dataParams.put("ibgDetailsJsonObj", ibgDetailsObj);
                dataParams.put("vendorId", vendorID);
                dataParams.put("companyId", companyid);

                if (DBSbank) {    // If DBS bank data is filled 
                    accVendorControllerServiceObj.saveIBGReceivingBankDetailsJSON(dataParams);
                }
                if (CIMBbank) {   // If CIMB bank data is filled 
                    accVendorControllerServiceObj.saveCIMBReceivingBankDetailsJSON(dataParams);
                }
            }
            Map<String, Object> insertLogParams = new HashMap<String, Object>();
            insertLogParams.put(Constants.reqHeader, (paramJobj.has(Constants.reqHeader) && paramJobj.get(Constants.reqHeader) != null) ? paramJobj.optString(Constants.reqHeader) : paramJobj.optString(Constants.remoteIPAddress));
            insertLogParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
            insertLogParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));

//            auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.optString(Constants.userfullname) + " has"+ auditMsg +  vendor.getName()+" ( "+vendorNumber+" ) ", request, vendor.getID());
            auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.optString(Constants.userfullname) + " has" + auditMsg + vendor.getName() + " ( " + vendorNumber + " ) ", insertLogParams, vendor.getID());
            
            if(Constants.indian_country_id == Integer.parseInt(company.getCountry().getID())){
                if(isTDSapplicableonvendor != vendorForTDSbeforeEdit){
                    auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.optString(Constants.userfullname) + " has changed " + "TDS applicable check from "+isTDSapplicableonvendor+" to " +vendorForTDSbeforeEdit+" for " + vendor.getName() + " ( " + vendorNumber + " ) ", insertLogParams, vendor.getID());
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveVendor : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveVendor : " + ex.getMessage(), ex);
        }
        return vendor;
    }

    public boolean isChildorGrandChildForVendor(String vendorid, String parentid) throws ServiceException {
        try {
            List Result = accaccountDAO.isChildorGrandChildForVendor(parentid);
            Iterator iterator = Result.iterator();
            if (iterator.hasNext()) {
                Object ResultObj = iterator.next();
                Vendor ResultParentac = (Vendor) ResultObj;
                ResultParentac = ResultParentac.getParent();
                if (ResultParentac == null) {
                    return false;
                } else {
                    String Resultparent = ResultParentac.getID();
                    if (Resultparent.equals(vendorid)) {
                        return true;
                    } else {
                        return isChildorGrandChildForVendor(vendorid, Resultparent);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("isChildorGrandChild : " + ex.getMessage(), ex);
        }
        return false;
    }

    private void saveVendorInChildCompanies(JSONObject paramJobj, boolean isEdit, Map<String, Object> parentDataMap, String parentCompanyid, String childCompanyID) throws DataInvalidateException {
        /*
         fetchColumn - column whose value is fetched from database
         dataColumn - column on which we apply condition
         */
        try {
            HashMap<String, Object> FinalDataMap = new HashMap<String, Object>();
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            List list = null;
            //********************default fields processing*********************************************
            int subModuleFlag = 0;
            //Replaced the multiple arguments of getModuleColumnConfig() with single HashMap object
            HashMap<String, Object> params = new HashMap<String, Object> ();
            params.put("moduleId", Constants.Vendor_MODULE_UUID);
            params.put("companyid", childCompanyID);
            params.put("isdocumentimport", "F");
            params.put("subModuleFlag", new Integer(subModuleFlag));
            JSONArray defaultColumnConfigJarray = importHandler.getModuleColumnConfig(params);
            for (int i = 0; i < defaultColumnConfigJarray.length(); i++) {

                JSONObject ColumnConfigJObj = defaultColumnConfigJarray.getJSONObject(i);
                String formfieldname = ColumnConfigJObj.getString("formfieldname");
                if (!StringUtil.isNullOrEmpty(formfieldname)) {
                    if (parentDataMap.containsKey(formfieldname)) {
                        String validateType = ColumnConfigJObj.has("validatetype") ? ColumnConfigJObj.getString("validatetype") : "";
                        Object value = parentDataMap.get(formfieldname);
                        if (validateType.equals("ref")) {
                            String data = value.toString();
                            if (!StringUtil.isNullOrEmpty(data)) {
                                try {
                                    String table = ColumnConfigJObj.getString("refModule");
                                    String fetchColumn = ColumnConfigJObj.getString("refDataColumn");
                                    String dataColumn = ColumnConfigJObj.getString("refFetchColumn");
                                    //get id from name .example - select name from account where id=?
                                    requestParams.put("companyid", parentCompanyid);
                                    list = importHandler.getRefData(requestParams, table, dataColumn, fetchColumn, "", data);
                                    data = (String) list.get(0);
                                    //get id from name .example - select id from account where name=?
                                    requestParams.put("companyid", childCompanyID);
                                    list = importHandler.getRefData(requestParams, table, fetchColumn, dataColumn, "", data);
                                    data = (String) list.get(0);
                                    if (!StringUtil.isNullOrEmpty(data)) {
                                        FinalDataMap.put(formfieldname, data);
                                    }
                                } catch (Exception ex) {
                                    throw new DataInvalidateException("Combo value not found in child company.");
                                }
                            }
                        } else if (validateType.equalsIgnoreCase("date")) {
                            String data = value.toString();
                            if (!StringUtil.isNullOrEmpty(data)) {
                                Date date = paramJobj.optString("creationDate",null) == null ? new Date() : authHandler.getDateOnlyFormat().parse( paramJobj.optString("creationDate"));
                                if (date == null) {
                                    date = new Date();
                                }
                                FinalDataMap.put(formfieldname, date);
                            }
                        } else if (validateType.equalsIgnoreCase("integer")) {
                            String data = value.toString();
                            int numberValue = 0;
                            if (!StringUtil.isNullOrEmpty(data)) {
                                numberValue = StringUtil.isNullOrEmpty(data) ? 0 : Integer.parseInt(data);
                                FinalDataMap.put(formfieldname, numberValue);
                            }
                        } else {
                            String dataVal = value.toString();
                            if (!StringUtil.isNullOrEmpty(dataVal)) {
                                FinalDataMap.put(formfieldname, value);
                            }
                        }

                    }

                }
            }
            //********************default fields processing Ends*********************************************

            //*********************************save singleSelect dropdowns *************************
            KwlReturnObject returnObject = null;
            String masterGroupID = "";
            String data = "";
            String fetchColumn = "name";
            String conditionColumn = "id";

            if (parentDataMap.containsKey("taxId") && !StringUtil.isNullOrEmpty((String) parentDataMap.get("taxId"))) {
                try {
                    returnObject = importHandler.getTaxbyIDorName(parentCompanyid, fetchColumn, conditionColumn, (String) parentDataMap.get("taxId"));
                    data = (String) returnObject.getEntityList().get(0);

                    returnObject = importHandler.getTaxbyIDorName(childCompanyID, conditionColumn, fetchColumn, data);
                    data = (String) returnObject.getEntityList().get(0);
                    FinalDataMap.put("taxId", data);
                } catch (Exception ex) {
                    throw new DataInvalidateException("Combo value not found in child company.");
                }
            }

            fetchColumn = "mst.value";
            conditionColumn = "mst.ID";
            if (parentDataMap.containsKey("title") && !StringUtil.isNullOrEmpty((String) parentDataMap.get("title"))) {
                try {
                    masterGroupID = String.valueOf(6);
                    returnObject = accCustomerDAOobj.getMasterItemByNameorID(parentCompanyid, (String) parentDataMap.get("title"), masterGroupID, fetchColumn, conditionColumn);
                    data = (String) returnObject.getEntityList().get(0);

                    returnObject = accCustomerDAOobj.getMasterItemByNameorID(childCompanyID, data, masterGroupID, conditionColumn, fetchColumn);
                    data = (String) returnObject.getEntityList().get(0);
                    FinalDataMap.put("title", data);
                } catch (Exception ex) {
                    throw new DataInvalidateException("Combo value not found in child company.");
                }
            }

            //*******************save singleSelect dropdowns Ends****************************************
            //****************************Add/Edit child compny's customer************************************
            String parentCompanyVendorID = parentDataMap.containsKey("parentCompanyVendorID") ? (String) parentDataMap.get("parentCompanyVendorID") : "";
            FinalDataMap.put("parentCompanyVendorID", parentCompanyVendorID);
            FinalDataMap.put("companyid", childCompanyID);
            KwlReturnObject result = null;
            String childCustomerID = "";
            Vendor vendor = null;
            try {
                if (!isEdit) {
                    result = accVendorDAOobj.addVendor(FinalDataMap);
                } else {

                    String childCustomerid = parentDataMap.containsKey("childvendorrid") ? (String) parentDataMap.get("childvendorrid") : "";
                    FinalDataMap.put("accid", childCustomerid);
                    result = accVendorDAOobj.updateVendor(FinalDataMap); 
                }
                vendor = (Vendor) result.getEntityList().get(0);
                childCustomerID = vendor.getID();
            } catch (Exception ex) {
                throw new DataInvalidateException("Customer could not be saved.");
            }
            //****************************Add/Edit child compny's customer************************************

            //*********************************save multiselect dropdowns*************************
            if (!StringUtil.isNullOrEmpty(childCustomerID)) {
                  accVendorDAOobj.deleteVendorCategoryMappingDtails(childCustomerID);
            }
            if (parentDataMap.get("category") != null) {
                masterGroupID = "8";
                String category = parentDataMap.get("category").toString();
                String[] vendorCategory = category.split(",");
                for (int i = 0; i < vendorCategory.length; i++) {
                    String value = vendorCategory[i];
                    if (!StringUtil.isNullOrEmpty(value)) {
                        try {
                            returnObject = accCustomerDAOobj.getMasterItemByNameorID(parentCompanyid, value, masterGroupID, fetchColumn, conditionColumn);
                            data = (String) returnObject.getEntityList().get(0);

                            returnObject = accCustomerDAOobj.getMasterItemByNameorID(childCompanyID, data, masterGroupID, conditionColumn, fetchColumn);
                            data = (String) returnObject.getEntityList().get(0);
                            accVendorDAOobj.saveVendorCategoryMapping(vendor.getID(),data);
                        } catch (Exception ex) {
                            throw new DataInvalidateException("Combo value not found in child company.");
                        }
                    }
                }
            }

            if (!StringUtil.isNullOrEmpty(childCustomerID)) {
                    int numRows=accVendorDAOobj.deleteVendorAgentMapping(vendor.getID());//For add/edit previous mappings are deleted
            }
            if (parentDataMap.get("agentmapping") != null) {
                String agentmapping = parentDataMap.get("agentmapping").toString();
                StringBuffer agents= new StringBuffer();
                masterGroupID = "20";
                String[] agentIDs = agentmapping.split(",");
                for (int i = 0; i < agentIDs.length; i++) {
                    String value = agentIDs[i];
                    if (!StringUtil.isNullOrEmpty(value)) {
                        try {
                            returnObject = accCustomerDAOobj.getMasterItemByNameorID(parentCompanyid, value, masterGroupID, fetchColumn, conditionColumn);
                            data = (String) returnObject.getEntityList().get(0);

                            returnObject = accCustomerDAOobj.getMasterItemByNameorID(childCompanyID, data, masterGroupID, conditionColumn, fetchColumn);
                            data = (String) returnObject.getEntityList().get(0);
                            agents.append(data + ",");
                        } catch (Exception ex) {
                            throw new DataInvalidateException("Combo value not found in child company.");
                        }
                    }
                }
                if (agents != null && agents.length() > 0) {
                    String agentStr=agents.substring(0,(agents.length()-1));
                    accVendorDAOobj.saveVendorAgentMapping(vendor, agentStr.split(","));
                }
            }

            if (!StringUtil.isNullOrEmpty(childCustomerID)) {
                 accVendorCustomerProductDAOobj.deleteVendorProductMapped(childCustomerID,null);
            }
            if (parentDataMap.get("productmapping") != null) {
                fetchColumn = "p.name";
                conditionColumn = "p.ID";
                String productString = parentDataMap.get("productmapping").toString();
                String[] productMapping = productString.split(",");
                if (productMapping != null) {
                    for (int j = 0; j < productMapping.length; j++) {
                        String value = productMapping[j];
                        if (!StringUtil.isNullOrEmpty(value)) {
                            try {
                                returnObject = accVendorCustomerProductDAOobj.getProductByNameorID(parentCompanyid, value, masterGroupID, fetchColumn, conditionColumn);
                                data = (String) returnObject.getEntityList().get(0);

                                returnObject = accVendorCustomerProductDAOobj.getProductByNameorID(childCompanyID, data, masterGroupID, conditionColumn, fetchColumn);
                                data = (String) returnObject.getEntityList().get(0);
                                     accVendorCustomerProductDAOobj.saveVendorProductMapping(childCustomerID,data,"");
                            } catch (Exception ex) {
                                throw new DataInvalidateException("Combo value not found in child company.");
                            }
                        }
                    }
                }
            }
             //*********************************save multiselect dropdowns Ends*************************

            //*******************Save Custom Fields Data****************************
            JSONArray jarray = parentDataMap.containsKey("customfield") ? new JSONArray(parentDataMap.get("customfield").toString()) : new JSONArray();
            Map<String, Object> customColumnConfigMap = importHandler.getCustomModuleColumnConfigForSharingMastersData(Constants.Vendor_MODULE_UUID, childCompanyID, false);

            JSONArray childFinalCustomJarray = new JSONArray();
            for (int i = 0; i < jarray.length(); i++) {
                JSONObject customColumnJobj = jarray.getJSONObject(i);
                String parentFieldValue = customColumnJobj.getString("fieldDataVal");
                String parentFieldName = customColumnJobj.getString("fieldname");
                String parentFieldID = customColumnJobj.getString("fieldid");
                int parentXtype = Integer.parseInt(customColumnJobj.getString("xtype"));

                if (customColumnConfigMap.containsKey(parentFieldName)) {
                    JSONObject childCustomConfig = (JSONObject) customColumnConfigMap.get(parentFieldName);
                    int childXtype = childCustomConfig.getInt("xtype");
                    String childFieldID = childCustomConfig.getString("id");
                    if (parentXtype == childXtype) {
                        JSONObject cjobj = new JSONObject();

                        cjobj.put("fieldid", childCustomConfig.getString("id"));
                        cjobj.put("refcolumn_name", "Col" + childCustomConfig.get("refcolnum"));
                        cjobj.put("fieldname", "Custom_" + childCustomConfig.get("columnName"));
                        cjobj.put("xtype", childCustomConfig.getString("xtype"));

                        cjobj.put("Custom_" + childCustomConfig.get("columnName"), "Col" + childCustomConfig.get("colnum"));

                        if (childXtype == 4 || childXtype == 7 || childXtype == 12) {
                            //combo ,multiselect combo,checklist.
                            try {
                                if (parentFieldValue != null) {
                                    String[] fieldComboDataArr = parentFieldValue.toString().split(",");
                                    String fieldComboDataStr = "";

                                    for (int dataArrIndex = 0; dataArrIndex < fieldComboDataArr.length; dataArrIndex++) {
                                        String value = fieldComboDataArr[dataArrIndex];
                                        if (!StringUtil.isNullOrEmpty(value)) {

                                            String CustomFetchColumn = "value";
                                            list = importHandler.getCustomComboValue(value, parentFieldID, CustomFetchColumn);

                                            value = list.get(0).toString();
                                            CustomFetchColumn = "id";
                                            list = importHandler.getCustomComboID(value, childFieldID, CustomFetchColumn);
                                            if (list != null && !list.isEmpty()) {
                                                fieldComboDataStr += list.get(0).toString() + ",";
                                            }
                                        }
                                    }

                                    if (!StringUtil.isNullOrEmpty(fieldComboDataStr)) {
                                        String comboids = fieldComboDataStr.substring(0, fieldComboDataStr.length() - 1);
                                        cjobj.put("fieldDataVal", comboids);
                                        cjobj.put("Col" + childCustomConfig.get("colnum"), comboids);
                                    } else {
                                        cjobj.put("fieldDataVal", "");
                                        cjobj.put("Col" + childCustomConfig.get("colnum"), "");
                                    }
                                } else {
                                    cjobj.put("fieldDataVal", "");
                                    cjobj.put("Col" + childCustomConfig.get("colnum"), "");
                                }
                            } catch (Exception ex) {
                                throw new DataInvalidateException("Combo value not found in child company.");
                            }
                        } else {
                            cjobj.put("fieldDataVal", parentFieldValue);
                            cjobj.put("Col" + childCustomConfig.get("colnum"), parentFieldValue);
                        }

                        childFinalCustomJarray.put(cjobj);
                    }

                }

            }

            if (childFinalCustomJarray.length() > 0) {
                try {
                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    customrequestParams.put("customarray", childFinalCustomJarray);
                    customrequestParams.put("modulename", Constants.Acc_Vendor_modulename);
                    customrequestParams.put("moduleprimarykey", Constants.Acc_VendorId);
                    customrequestParams.put("modulerecid", vendor.getID());
                    customrequestParams.put("moduleid", Constants.Acc_Vendor_ModuleId);
                    customrequestParams.put("companyid", childCompanyID);
                    customrequestParams.put("customdataclasspath", Constants.Acc_Vendor_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        HashMap<String, Object> cRequestParams = new HashMap<String, Object>();
                        cRequestParams.put("accid", vendor.getID());
                        cRequestParams.put("accvendorcustomdataref", vendor.getID());
                        KwlReturnObject accresult = accVendorDAOobj.updateVendor(cRequestParams);
                    }
                } catch (Exception ex) {
                    throw new DataInvalidateException("Error ocurred while saving custom fields");
                }
            }
            //**************************Save Custom Fields Data End***************************************
        } catch (Exception ex) {
              throw new DataInvalidateException("Error ocurred while saving Vendor");
        }
    }

    public Customer saveCustomer(JSONObject paramJobj) throws ServiceException, SessionExpiredException, AccountingException {
        Customer customer = null;
        try {
            boolean mapcustomervendorFlag = !StringUtil.isNullOrEmpty(paramJobj.optString("mapcustomervendor", null)); //true when copy customer checkbox is checked in vendor creation.
            boolean copyflag = paramJobj.optString("copyflag", null) != null; //true when copy customer checkbox is checked in vendor creation.
            String currencyid = (paramJobj.optString("currencyid", null) == null ? paramJobj.optString(Constants.globalCurrencyKey) : paramJobj.optString("currencyid"));
            String companyid = paramJobj.optString(Constants.companyKey);
            String customerid = copyflag ? "" : paramJobj.optString("accid", null);
            String accountName = paramJobj.optString("accname", null);
            String aliasname = paramJobj.optString("aliasname", null);
            String customerNumber = paramJobj.optString("custorvenacccode").trim();
            String sequenceformat = paramJobj.optString("sequenceformatvencus", null);
            int from = Integer.parseInt(paramJobj.optString("fromVenCus", null));
            boolean isEdit = StringUtil.isNullOrEmpty(paramJobj.optString("isEdit", null)) ? false : Boolean.parseBoolean(paramJobj.optString("isEdit"));
            String parentName = paramJobj.optString("parentname", null);
            boolean autogen = false;
            boolean issub = paramJobj.optString("issub", null) != null;

            boolean debitType = StringUtil.getBoolean(paramJobj.optString("debitType"));
            debitType = (copyflag) ? !debitType : debitType;
            double openBalance = StringUtil.getDouble(paramJobj.optString("openbalance"));
            openBalance = debitType ? openBalance : -openBalance;
            String parentid = (copyflag) ? null : paramJobj.optString("parentid", null);
            if (!issub) {
                KwlReturnObject accResult = accaccountDAO.getSundryAccount(companyid, false);
                if (accResult.getEntityList().size() > 0 && accResult.getEntityList().get(0) != null) {
                    parentid = (String) accResult.getEntityList().get(0);
                } else {
                    parentid = null;
                }
            }

            double life = StringUtil.getDouble(paramJobj.optString("life"));
            double salvage = StringUtil.getDouble(paramJobj.optString("salvage"));
            Date creationDate = authHandler.getDateOnlyFormat().parse(paramJobj.optString("creationDate"));
            if (creationDate == null) {
                creationDate = new Date();
            }
            boolean interStateParty = !StringUtil.isNullOrEmpty(paramJobj.optString("interstateparty", null));
            boolean cFromApplicable = !StringUtil.isNullOrEmpty(paramJobj.optString("cformapplicable", null));
            Date vatRegDate = StringUtil.isNullOrEmpty(paramJobj.optString("vatregdate", null)) ? null : authHandler.getDateOnlyFormat().parse(paramJobj.optString("vatregdate"));
            Date cstRegDate = StringUtil.isNullOrEmpty(paramJobj.optString("cstregdate")) ? null : authHandler.getDateOnlyFormat().parse(paramJobj.optString("cstregdate"));

            JSONObject accjson = new JSONObject();
            accjson.put("accountid", customerid);
            accjson.put("acccode", customerNumber);
            accjson.put("name", accountName);
            accjson.put("aliasname", aliasname);
            accjson.put("balance", openBalance);
            accjson.put("parentid", parentid);
//            accjson.put("groupid", Group.ACCOUNTS_RECEIVABLE);
            accjson.put(Constants.companyKey, companyid);
            accjson.put("currencyid", currencyid);
            accjson.put("life", life);
            accjson.put("salvage", salvage);
            boolean intercompanyflag = false;
            if (paramJobj.optString("intercompanyflag", null) != null && (paramJobj.optString("intercompanyflag").equals("on") || paramJobj.optString("intercompanyflag").equals("true"))) {
                intercompanyflag = true;
            }
            accjson.put("intercompanyflag", intercompanyflag);
            if (intercompanyflag) {
                accjson.put("intercompanytype", paramJobj.optString("intercompanytype"));
            }
            accjson.put("creationdate", creationDate);
//            accjson.put("category", paramJobj.optString("category"));

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("accid", customerid);
            if (sequenceformat.equals("NA") || isEdit) {
                requestParams.put("acccode", customerNumber);
            } else {
                requestParams.put("acccode", "");
            }
            requestParams.put("accname", accountName);
            requestParams.put("creationDate", creationDate);
            requestParams.put("aliasname", aliasname);
            requestParams.put("parentid", parentid);
            requestParams.put("issub", paramJobj.optString("issub", null));
            requestParams.put("debitType", paramJobj.optString("debitType", null));
            requestParams.put("openbalance", paramJobj.optString("openbalance", null));
            requestParams.put("title", paramJobj.optString("title", null));
            requestParams.put("currencyid", currencyid);
            requestParams.put("address", paramJobj.optString("address", null));
            requestParams.put("baddress2", paramJobj.optString("baddress2", null));
            requestParams.put("baddress3", paramJobj.optString("baddress3", null));
            requestParams.put("bankaccountno", paramJobj.optString("bankaccountno", null));
            requestParams.put("email", paramJobj.optString("email", null));
            requestParams.put("contactno", paramJobj.optString("contactno", null));
            requestParams.put("contactno2", paramJobj.optString("contactno2", null));
            requestParams.put("fax", paramJobj.optString("fax", null));
            requestParams.put("shippingaddress", copyflag ? paramJobj.optString("address", null) : paramJobj.optString("shippingaddress", null));
            requestParams.put("shippingaddress2", paramJobj.optString("shippingaddress2", null));
            requestParams.put("shippingaddress3", paramJobj.optString("shippingaddress3", null));
            requestParams.put("termid", paramJobj.optString("termid", null));
            requestParams.put("other", paramJobj.optString("other", null));
            requestParams.put("taxno", paramJobj.optString("taxno", null));
            requestParams.put(Constants.companyKey, companyid);
            requestParams.put("country", paramJobj.optString("country", null));
            requestParams.put("creditLimit", paramJobj.optString("limit", null));
            requestParams.put("overseas", !StringUtil.isNullOrEmpty(paramJobj.optString("overseas", null)));
            requestParams.put("mapcustomervendor", mapcustomervendorFlag);
            requestParams.put("createdInVendor", true);//this will be always true, because this customer is created in vendor 
            requestParams.put("servicetaxno", !StringUtil.isNullOrEmpty(paramJobj.optString("servicetaxno", null)) ? paramJobj.optString("servicetaxno") : "");
            requestParams.put("interstateparty", interStateParty);
            requestParams.put("cformapplicable", cFromApplicable);
            requestParams.put("vatregdate", vatRegDate);
            /*
             * CST Registration date in Customer Master required for Form 402
             */
            requestParams.put("cstregdate", cstRegDate);
            requestParams.put("dealertype", !StringUtil.isNullOrEmpty(paramJobj.optString("dealertype", null)) ? paramJobj.optString("dealertype") : "");
            requestParams.put("eccno", !StringUtil.isNullOrEmpty(paramJobj.optString("eccno", null)) ? paramJobj.optString("eccno") : "");
            requestParams.put("defaultnatureofpurchase", !StringUtil.isNullOrEmpty(paramJobj.optString("defaultnatureofpurchase", null)) ? paramJobj.optString("defaultnatureofpurchase") : "");
            requestParams.put("importereccno", !StringUtil.isNullOrEmpty(paramJobj.optString("importereccno", null)) ? paramJobj.optString("importereccno") : "");
            requestParams.put("iecno", !StringUtil.isNullOrEmpty(paramJobj.optString("iecno", null)) ? paramJobj.optString("iecno") : "");
            requestParams.put("range", !StringUtil.isNullOrEmpty(paramJobj.optString("range", null)) ? paramJobj.optString("range") : "");
            requestParams.put("division", !StringUtil.isNullOrEmpty(paramJobj.optString("division", null)) ? paramJobj.optString("division") : "");
            requestParams.put("commissionerate", !StringUtil.isNullOrEmpty(paramJobj.optString("commissionerate", null)) ? paramJobj.optString("commissionerate") : "");
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("companycountry", null)) && paramJobj.optString("companycountry").equals("106")) {
                //NPWP no. is only for Indonesia but in backend it is saved as PAN NO.
                requestParams.put("panno", !StringUtil.isNullOrEmpty(paramJobj.optString("npwp", null)) ? paramJobj.optString("npwp") : "");
            } else {
                requestParams.put("panno", !StringUtil.isNullOrEmpty(paramJobj.optString("panno", null)) ? paramJobj.optString("panno") : "");
            }
            requestParams.put("panstatus", !StringUtil.isNullOrEmpty(paramJobj.optString("panStatusId", null)) ? paramJobj.optString("panStatusId") : "");
            requestParams.put("vattinno", !StringUtil.isNullOrEmpty(paramJobj.optString("csttinno", null)) ? paramJobj.optString("csttinno") : "");
            requestParams.put("csttinno", !StringUtil.isNullOrEmpty(paramJobj.optString("vattinno", null)) ? paramJobj.optString("vattinno") : "");
            requestParams.put("companyRegistrationNumber", paramJobj.optString("companyRegistrationNumber", null));
            requestParams.put("gstRegistrationNumber", paramJobj.optString("gstRegistrationNumber", null));
            /*
             * For India Compliace - GST related fields - START
             */
            requestParams.put("gstin", !StringUtil.isNullOrEmpty(paramJobj.optString("gstin", null)) ? paramJobj.optString("gstin") : "");
            /*
             * For India Compliace - GST related fields - END
             */

            if (!StringUtil.isNullOrEmpty(sequenceformat) && sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Customer_ModuleId, customerNumber, companyid);
                if (!resultList.isEmpty()) {
                    boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                    String formatName = (String) resultList.get(1);
                    if (!isvalidEntryNumber) {
                        throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " <b>" + customerNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, Locale.forLanguageTag(paramJobj.getString("language"))));
                    }
                }
            }
            requestParams.put("autogenerated", !sequenceformat.equalsIgnoreCase("NA") ? true : false);
            String mappingaccid = paramJobj.optString("mappingcusaccid", null);

            KwlReturnObject result;
            if (StringUtil.isNullOrEmpty(customerid)) {
                requestParams.put("accountid", mappingaccid);
                result = accCustomerDAOobj.addCustomer(requestParams);
                auditMsg = " added new customer ";
                auditID = AuditAction.CUSTOMER_ADDED;
            } else {
                if (isChildorGrandChild(customerid, parentid)) {
                    throw new AccountingException("\"" + accountName + "\" is a parent of \"" + parentName + "\" so can't set \"" + parentName + "\" as a parent.");
                }
                requestParams.put("accountid", mappingaccid);
                result = accCustomerDAOobj.updateCustomer(requestParams);
                auditMsg = " updated customer ";
                auditID = AuditAction.CUSTOMER_UPDATED;
            }

            List ll = result.getEntityList();
            customer = (Customer) ll.get(0);

            String customerID = customer.getID();
            String[] customerCategory = paramJobj.optString("category").split(",");

            if (!StringUtil.isNullOrEmpty(customerID)) {
                accCustomerDAOobj.deleteCustomerCategoryMappingDtails(customerID);
            }

            for (int j = 0; j < customerCategory.length; j++) {
                if (!StringUtil.isNullOrEmpty(customerID) && !StringUtil.isNullOrEmpty(customerCategory[j])) {
                    accCustomerDAOobj.saveCustomerCategoryMapping(customerID, customerCategory[j]);
                }
            }
            Map<String, Object> insertLogParams = new HashMap<String, Object>();
            insertLogParams.put(Constants.reqHeader, (paramJobj.has(Constants.reqHeader) && paramJobj.get(Constants.reqHeader) != null) ? paramJobj.optString(Constants.reqHeader) : paramJobj.optString(Constants.remoteIPAddress));
            insertLogParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
            insertLogParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
//            auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.optString(Constants.userfullname) + auditMsg + customer.getName(), request, customer.getID());
            auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.optString(Constants.userfullname) + auditMsg + customer.getName(), insertLogParams, customer.getID());
        } catch (JSONException ex) {
//            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveCustomer : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
//            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveCustomer : " + ex.getMessage(), ex);
        }
        return customer;
    }

    //Function used to delete entries in temporary table
    private void deleteEntryInTemp(Map deleteparam) {
        try {
            String vendorno = deleteparam.get("vendorno").toString();
            String customerno = deleteparam.get("customerno").toString();
            String companyid = deleteparam.get(Constants.companyKey).toString();
            accCommonTablesDAO.deleteTransactionInTemp(vendorno, companyid, Constants.Acc_Vendor_ModuleId);
            if (deleteparam.containsKey("isalsocustomer") && deleteparam.get("isalsocustomer") != null) {
                accCommonTablesDAO.deleteTransactionInTemp(customerno, companyid, Constants.Acc_Customer_ModuleId);
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean isChildorGrandChild(String vendorid, String parentid) throws ServiceException {
        try {
            List Result = accaccountDAO.isChildorGrandChild(parentid);
            Iterator iterator = Result.iterator();
            if (iterator.hasNext()) {
                Object ResultObj = iterator.next();
                Account ResultParentac = (Account) ResultObj;
                ResultParentac = ResultParentac.getParent();
                if (ResultParentac == null) {
                    return false;
                } else {
                    String Resultparent = ResultParentac.getID();
                    if (Resultparent.equals(vendorid)) {
                        return true;
                    } else {
                        return isChildorGrandChild(vendorid, Resultparent);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("isChildorGrandChild : " + ex.getMessage(), ex);
        }
        return false;
    }
    
    
 @Override   
    public void saveVendorAddresses(JSONObject paramJobj) throws ServiceException, SessionExpiredException, AccountingException {
        try {
            String vendorID = "";
            String customerID = "";
            String companyid = paramJobj.optString(Constants.companyKey);
            vendorID = paramJobj.optString("accid", null);
            String addressDetails = paramJobj.optString("addressDetail", "[]");
            JSONArray jArr = new JSONArray(addressDetails);
            String propagationflag = paramJobj.optString("ispropagatetochildcompanyflag", null);
            boolean ispropagatetochildcompanyflag = !StringUtil.isNullOrEmpty(propagationflag) ? Boolean.parseBoolean(propagationflag) : false;
            String parentCompanyVendorID = vendorID;

            HashMap<String, Object> mappingParams = new HashMap<String, Object>();
            mappingParams.put("vendoraccountid", vendorID);
            KwlReturnObject mappingResult = accCusVenMapDAOObj.getCustomerVendorMapping(mappingParams);
            if (mappingResult != null && !mappingResult.getEntityList().isEmpty()) {
                CustomerVendorMapping mapping = (CustomerVendorMapping) mappingResult.getEntityList().get(0);
                if (mapping != null && mapping.getCustomeraccountid() != null) {
                    customerID = mapping.getCustomeraccountid().getID();
                    if (!StringUtil.isNullOrEmpty(customerID)) {
                        KwlReturnObject categoryresult = accountingHandler.getObject(Customer.class.getName(), customerID);
                        Customer customer = (Customer) categoryresult.getEntityList().get(0);
                        if (customer.isCreatedInVendor()) {
                            HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
                            addrRequestParams.put(Constants.customerid, customerID);
                            addrRequestParams.put(Constants.companyKey, companyid);
                            KwlReturnObject addressResult = accountingHandler.getCustomerAddressDetails(addrRequestParams);
                            if (addressResult.getEntityList().isEmpty()) {//if address is not given to vendor which is created with customer. in this case updating vendor address with customer address
                                for (int i = 0; i < jArr.length(); i++) {
                                    HashMap<String, Object> custAddrMap = new HashMap<String, Object>();
                                    JSONObject jobj = jArr.getJSONObject(i);
                                    custAddrMap.put(Constants.customerid, customerID);
                                    custAddrMap.put("aliasName", jobj.optString("aliasName", ""));
                                    custAddrMap.put("address", jobj.optString("address", ""));
                                    custAddrMap.put("county", jobj.optString("county", ""));
                                    custAddrMap.put("city", jobj.optString("city", ""));
                                    custAddrMap.put("state", jobj.optString("state", ""));
                                    custAddrMap.put("stateCode", jobj.optString("stateCode", ""));
                                    custAddrMap.put("country", jobj.optString("country", ""));
                                    custAddrMap.put("postalCode", jobj.optString("postalCode", ""));
                                    custAddrMap.put("phone", jobj.optString("phone", ""));
                                    custAddrMap.put("mobileNumber", jobj.optString("mobileNumber", ""));
                                    custAddrMap.put("fax", jobj.optString("fax", ""));
                                    custAddrMap.put("emailID", jobj.optString("emailID", ""));
                                    custAddrMap.put("recipientName", jobj.optString("recipientName", ""));
                                    custAddrMap.put("contactPerson", jobj.optString("contactPerson", ""));
                                    custAddrMap.put("contactPersonNumber", jobj.optString("contactPersonNumber", ""));
                                    custAddrMap.put("contactPersonDesignation", jobj.optString("contactPersonDesignation", ""));
                                    custAddrMap.put("website", jobj.optString("website", ""));
                                    custAddrMap.put("isBillingAddress", jobj.getBoolean("isBillingAddress"));
                                    custAddrMap.put("isDefaultAddress", jobj.getBoolean("isDefaultAddress"));
                                    KwlReturnObject custAddrobject = accountingHandler.saveCustomerAddressesDetails(custAddrMap, companyid);
                                }
                            }
                        }
                    }
                }
            }

            if (!StringUtil.isNullOrEmpty(vendorID)) {//deleteting previously added address
                KwlReturnObject deleteResult = accountingHandler.deleteVendorAddressDetails(vendorID, companyid);

                if (ispropagatetochildcompanyflag) {
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("parentCompanyVendorID", parentCompanyVendorID);
                    KwlReturnObject result = accVendorDAOobj.getChildVendors(requestParams);
                    List childCompaniesVendorList = result.getEntityList();
                    try {
                        for (Object childObj : childCompaniesVendorList) {
                            Vendor vend = (Vendor) childObj;
                            if (vend != null) {
                                String childCompanyID = vend.getCompany().getCompanyID();
                                String childcompanysvendorid = vend.getID();
                                deleteResult = accountingHandler.deleteVendorAddressDetails(childcompanysvendorid, childCompanyID);
                            }
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            for (int i = 0; i < jArr.length(); i++) {
                HashMap<String, Object> vendAddrMap = new HashMap<String, Object>();
                JSONObject jobj = jArr.getJSONObject(i);
                vendAddrMap.put("vendorid", vendorID);
                vendAddrMap.put("aliasName", jobj.optString("aliasName", ""));
                vendAddrMap.put("address", jobj.optString("address", ""));
                vendAddrMap.put("county", jobj.optString("county", ""));
                vendAddrMap.put("city", jobj.optString("city", ""));
                vendAddrMap.put("state", jobj.optString("state", ""));
                vendAddrMap.put("stateCode", jobj.optString("stateCode", ""));
                vendAddrMap.put("country", jobj.optString("country", ""));
                vendAddrMap.put("postalCode", jobj.optString("postalCode", ""));
                vendAddrMap.put("phone", jobj.optString("phone", ""));
                vendAddrMap.put("mobileNumber", jobj.optString("mobileNumber", ""));
                vendAddrMap.put("fax", jobj.optString("fax", ""));
                vendAddrMap.put("emailID", jobj.optString("emailID", ""));
                vendAddrMap.put("recipientName", jobj.optString("recipientName", ""));
                vendAddrMap.put("contactPerson", jobj.optString("contactPerson", ""));
                vendAddrMap.put("contactPersonNumber", jobj.optString("contactPersonNumber", ""));
                vendAddrMap.put("contactPersonDesignation", jobj.optString("contactPersonDesignation", ""));
                vendAddrMap.put("website", jobj.optString("website", ""));
                vendAddrMap.put("isBillingAddress", jobj.getBoolean("isBillingAddress"));
                vendAddrMap.put("isDefaultAddress", jobj.getBoolean("isDefaultAddress"));
                KwlReturnObject vendAddrobject = accountingHandler.saveVendorAddressesDetails(vendAddrMap, companyid);

                //*************************save address details in child companies ****************************8

                if (ispropagatetochildcompanyflag) {
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("parentCompanyVendorID", parentCompanyVendorID);
                    KwlReturnObject result = accVendorDAOobj.getChildVendors(requestParams);
                    List childCompaniesVendorList = result.getEntityList();
                    try {
                        for (Object childObj : childCompaniesVendorList) {

                            Vendor vend = (Vendor) childObj;
                            if (vend != null) {
                                String childCompanyID = vend.getCompany().getCompanyID();
                                String childcompanysvendorid = vend.getID();
                                vendAddrMap.put("vendorid", childcompanysvendorid);
                                vendAddrobject = accountingHandler.saveVendorAddressesDetails(vendAddrMap, childCompanyID);
                            }
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                //*************************save address details in child companies Ends****************************8
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveCustomer : " + ex.getMessage(), ex);
        }
    }
 
 @Override
    public JSONObject activateDeactivateVendors(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        KwlReturnObject result = null;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.RES_data, paramJobj.optString(Constants.RES_data));
            String vendorActivateDeactivate = paramJobj.optString("activateDeactivateFlag");
            String companyid = paramJobj.optString(Constants.companyKey);
            boolean vendorActivateDeactivateFlag = StringUtil.isNullOrEmpty(vendorActivateDeactivate) ? false : Boolean.parseBoolean(vendorActivateDeactivate);
            requestParams.put("vendorActivateDeactivateFlag", vendorActivateDeactivateFlag);
            requestParams.put("companyid", companyid);
//            requestParams.put("request", paramJobj.toString());
            result = accVendorDAOobj.activateDeactivateVendors(requestParams);
            issuccess = true;
            msg = vendorActivateDeactivateFlag ? messageSource.getMessage("acc.vendor.activate", null, Locale.forLanguageTag(paramJobj.getString("language"))) : messageSource.getMessage("acc.vendor.deactivate", null, Locale.forLanguageTag(paramJobj.getString("language")));
            txnManager.commit(status);
            auditMsg = vendorActivateDeactivateFlag ? "Activated Vendor " : "Deactivated Vendor ";
            for (int i = 0; i < result.getRecordTotalCount(); i++) {
                Vendor vendor = (Vendor) result.getEntityList().get(i);
                Map<String, Object> insertLogParams = new HashMap<String, Object>();
                insertLogParams.put(Constants.reqHeader, (paramJobj.has(Constants.reqHeader) && paramJobj.get(Constants.reqHeader) != null) ? paramJobj.optString(Constants.reqHeader) : paramJobj.optString(Constants.remoteIPAddress));
                insertLogParams.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
                insertLogParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
                auditTrailObj.insertAuditLog(AuditAction.VENDOR_ACTIVATE_DEACTIVATE, "User " + paramJobj.optString(Constants.userfullname) + " has " + auditMsg + "<b>" + vendor.getName() + "</b> ( " + vendor.getAcccode() + " ) ", insertLogParams, vendor.getID());
            }
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
    /**
     * GST Details Changes for Customer vendor. IF customer/ Vendor used in Transaction then show confirm message.
     * GST details - GSTIN Registration Type , Customer/ Vendor Type
     * @param paramsObject
     * @return
     * @throws JSONException
     * @throws ServiceException 
     */
    @Override
    public JSONObject isCustomerVendorUsedInTransacton(JSONObject paramsObject) throws JSONException,ServiceException{
        JSONObject custVendTransactionDetails = new JSONObject();
        boolean isCustomer = paramsObject.optBoolean("isCustomer", false);
        String companyid = paramsObject.optString(Constants.companyKey);
        String custVendId = paramsObject.optString(Constants.accid);
        boolean isUsed = false;
        if(isCustomer){
           isUsed = accCusVenMapDAOObj.isCustomerUsedInTransactions(custVendId, companyid);
        }else{
           isUsed = accCusVenMapDAOObj.isVendorUsedInTransactions(custVendId, companyid);
        }
        custVendTransactionDetails.put("isUsed", isUsed);
        return custVendTransactionDetails;
    }
    /**
     * Function to get Vendor GST fields history data.
     *
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException
     * @throws SessionExpiredException
     * @throws ParseException
     */
    public JSONObject getVendorGSTHistory(JSONObject reqParams) throws ServiceException, JSONException, SessionExpiredException, ParseException {
        Map<String, Object> reqMap = new HashMap();
        if (!StringUtil.isNullOrEmpty(reqParams.optString("returnalldata"))) {
            reqMap.put("returnalldata", reqParams.optBoolean("returnalldata"));
        }
        if (!StringUtil.isNullOrEmpty(reqParams.optString("transactiondate"))) {
            reqMap.put("transactiondate", authHandler.getDateOnlyFormat().parse(reqParams.optString("transactiondate")));
        }
        if (!StringUtil.isNullOrEmpty(reqParams.optString("vendorid"))) {
            reqMap.put("vendorid", reqParams.optString("vendorid"));
        }
        if (!StringUtil.isNullOrEmpty(reqParams.optString("isfortransaction"))) {
            reqMap.put("isfortransaction", reqParams.optBoolean("isfortransaction"));
        }
        JSONObject data = new JSONObject();
        DateFormat df = null;
        try {
            df = authHandler.getOnlyDateFormat();
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accVendorControllerCMNServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        JSONArray jSONArray = new JSONArray();
        JSONObject jSONObject = new JSONObject();
        List<GstVendorHistory> gstVendorHistorys = accVendorDAOobj.getGstVendorHistory(reqMap);
        for (GstVendorHistory gstVendorHistory : gstVendorHistorys) {
            jSONObject = new JSONObject();
            jSONObject.put("id", gstVendorHistory.getVendor().getName());
            jSONObject.put("GSTINRegistrationTypeId", gstVendorHistory.getGSTRegistrationType() != null ? gstVendorHistory.getGSTRegistrationType().getID() : "");
            jSONObject.put("CustomerVendorTypeId", gstVendorHistory.getGSTVendorType()!=null?gstVendorHistory.getGSTVendorType().getID():"");
            jSONObject.put("gstin", gstVendorHistory.getGstin());
            jSONObject.put("applydate", gstVendorHistory.getApplyDate() != null ? df.format(gstVendorHistory.getApplyDate()) : "");
            /**
             * Add data for transaction purpose
             */
            jSONObject.put("uniqueCase", accCustomerDAOobj.getUniqueCase(jSONObject.put("type", gstVendorHistory.getGSTVendorType()!=null && gstVendorHistory.getGSTVendorType().getDefaultMasterItem()!=null?gstVendorHistory.getGSTVendorType().getDefaultMasterItem().getID():"")));
            /**
             * Override GST unique case for Vendor if Vendor GST Registration type is Composition or Composition E-Commerce
             * as a NO-GST
             */
            String defaultMasterItemID = (gstVendorHistory.getGSTRegistrationType() != null && gstVendorHistory.getGSTRegistrationType().getDefaultMasterItem() != null) ? gstVendorHistory.getGSTRegistrationType().getDefaultMasterItem().getID() : "";
            if (defaultMasterItemID.equalsIgnoreCase(Constants.GSTRegType.get(Constants.GSTRegType_Composition)) || defaultMasterItemID.equalsIgnoreCase(Constants.GSTRegType.get(Constants.GSTRegType_Composition_ECommerce))) {
                jSONObject.put("uniqueCase", Constants.NOGST);
            }
            jSONObject.put("GSTINRegTypeDefaultMstrID", gstVendorHistory.getGSTRegistrationType() != null && gstVendorHistory.getGSTRegistrationType().getDefaultMasterItem() != null ? gstVendorHistory.getGSTRegistrationType().getDefaultMasterItem().getID() : "");
            jSONObject.put(IndiaComplianceConstants.CustVenTypeDefaultMstrID, gstVendorHistory.getGSTVendorType() != null ? (gstVendorHistory.getGSTVendorType().getDefaultMasterItem() != null ? gstVendorHistory.getGSTVendorType().getDefaultMasterItem().getID() : "") : "");
            jSONArray.put(jSONObject);
            if(reqParams.optBoolean("returncurrentsingledata")){
                    break;
            }
        }
        /**
         * If GST History not present then take GST data from Vendor
         */
        boolean isfortransaction = reqParams.optBoolean("isfortransaction", false);
        boolean isGSTHistoryDataPresent = true;
        if (isfortransaction) {
            if (gstVendorHistorys.isEmpty()) {
               isGSTHistoryDataPresent = false;
            } else if (jSONArray.length() == 1) {
                /**
                 * If GST History present but empty values GST Registration type, Vendor Type and GSTIN if required
                 */
                JSONObject historyJobj = jSONArray.getJSONObject(0);
                if (StringUtil.isNullOrEmpty(historyJobj.optString("GSTINRegistrationTypeId", ""))) {
                    isGSTHistoryDataPresent = false;
                } else if (StringUtil.isNullOrEmpty(historyJobj.optString("CustomerVendorTypeId", ""))) {
                    isGSTHistoryDataPresent = false;
                } else if (!StringUtil.isNullOrEmpty(historyJobj.optString("GSTINRegTypeDefaultMstrID", "")) && !(historyJobj.optString("GSTINRegTypeDefaultMstrID", "").equalsIgnoreCase(Constants.GSTRegType.get(Constants.GSTRegType_Unregistered))) && StringUtil.isNullOrEmpty(historyJobj.optString("gstin", ""))) {
                    isGSTHistoryDataPresent = false;
                }
            }
        } else if (reqParams.optBoolean("returncurrentsingledata") && gstVendorHistorys.isEmpty()) {
            jSONObject.put("GSTINRegistrationTypeId", "");
            jSONObject.put("CustomerVendorTypeId", "");
            jSONObject.put("gstin", "");
            jSONObject.put("applydate", "");
            jSONArray.put(jSONObject);
        }
        data.put(Constants.IS_GST_HISTORY_PRESENT, isGSTHistoryDataPresent);
        data.put("count", jSONArray.length());
        return data.put("data", jSONArray);
    }
      /**
     * Function to get Vendor's Used history.
     *
     * @param reqMap
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public JSONObject getVendorGSTUsedHistory(JSONObject reqParams) throws ServiceException, JSONException, ParseException {        
        DateFormat df = null;
        df = authHandler.getGlobalDateFormat();
        Map<String, Object> reqMap = new HashMap();
        if (!StringUtil.isNullOrEmpty(reqParams.optString("custvenid"))) {
            reqMap.put("vendorid", reqParams.optString("custvenid"));
        }
        if (!StringUtil.isNullOrEmpty(reqParams.optString("applydate"))) {
            reqMap.put("applyDate", df.parse(reqParams.optString("applydate")));
        }
        if (!StringUtil.isNullOrEmpty(reqParams.optString("companyid"))) {
            reqMap.put("companyid", reqParams.optString("companyid"));
        }
        JSONObject data = new JSONObject();
        JSONArray jSONArray = new JSONArray();
        JSONObject jSONObject = new JSONObject();
        List result=accVendorDAOobj.getGstVendorUsedHistory(reqMap);     
        if(!result.isEmpty()){
            jSONObject.put("isUsedCustomerVendor",true);
        }else{
            jSONObject.put("isUsedCustomerVendor",false);
        }
        jSONArray.put(jSONObject);
        data.put("count", jSONArray.length());
        return data.put("data", jSONArray);
    }
    /**
     * Save vendor GST details audit trail entry
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException
     * @throws SessionExpiredException
     * @throws ParseException 
     */
    public JSONObject saveVendorGSTHistoryAuditTrail(JSONObject paramJObj) throws ServiceException, JSONException, SessionExpiredException, ParseException {
        JSONObject returnJSONObj = new JSONObject();
        
        Map<String, Object> auditRequestParamsForGSTHistory = new HashMap<String, Object>();
        auditRequestParamsForGSTHistory.put(Constants.reqHeader, paramJObj.getString(Constants.reqHeader));
        auditRequestParamsForGSTHistory.put(Constants.remoteIPAddress, paramJObj.getString(Constants.remoteIPAddress));
        auditRequestParamsForGSTHistory.put(Constants.useridKey, paramJObj.getString(Constants.useridKey));
        String userName = paramJObj.optString(Constants.userfullname);
        String vendorID = paramJObj.optString(Constants.vendorid, "");
        String vendorName = paramJObj.optString(Constants.VendorName, "");
        String auditMSGForGSTHistory = "";
        
        /**
         * Get New GST details 
         */
        String newgstin = paramJObj.optString("gstin","");
        String newGSTINRegistrationType = paramJObj.optString("GSTINRegistrationTypeId","");
        String newCustomerVendorType= paramJObj.optString("CustomerVendorTypeId", "");
        if(!StringUtil.isNullOrEmpty(newGSTINRegistrationType)){
            Map<String, Object> map = new HashMap<>();
            map.put("ID", newGSTINRegistrationType);
            Object res = kwlCommonTablesDAOObj.getRequestedObjectFields(MasterItem.class, new String[]{"value"}, map);
            newGSTINRegistrationType = res != null ? (String) res : "";
        }
        if(!StringUtil.isNullOrEmpty(newCustomerVendorType)){
            Map<String, Object> map = new HashMap<>();
            map.put("ID", newCustomerVendorType);
            Object res = kwlCommonTablesDAOObj.getRequestedObjectFields(MasterItem.class, new String[]{"value"}, map);
            newCustomerVendorType = res != null ? (String) res : "";
        }
        DateFormat df = authHandler.getDateOnlyFormat();
        String gstapplieddate = paramJObj.optString("gstapplieddate", null);
        Date applyDate = df.parse(gstapplieddate);
        Map<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("applyDate", applyDate);
        requestParams.put("vendorid", vendorID);
        requestParams.put("returnalldata", true);
        
        String oldgstin = "";
        String oldGSTINRegistrationType = "";
        String oldCustomerVendorType = "";
        SimpleDateFormat sdf = new SimpleDateFormat(Constants.MMMMdyyyy);
        String newAppliedDateString = sdf.format(applyDate);
        String oldAppliedDateString = "";
        /**
         * Check if same GST detail present or not if present then
         * audit trail entry as updated GST details
         */
        List<GstVendorHistory> vendorHistory = accVendorDAOobj.getGstVendorHistory(requestParams);
        if (vendorHistory!=null && !vendorHistory.isEmpty()) {
            for (GstVendorHistory gstVendorHistory : vendorHistory) {
                if (gstVendorHistory != null) {
                    oldgstin = gstVendorHistory.getGstin();
                    oldGSTINRegistrationType =gstVendorHistory.getGSTRegistrationType()!=null ? gstVendorHistory.getGSTRegistrationType().getValue() : "";
                    oldCustomerVendorType = gstVendorHistory.getGSTVendorType()!=null ? gstVendorHistory.getGSTVendorType().getValue() : "";
                    oldAppliedDateString = gstVendorHistory.getApplyDate()!=null ? sdf.format(gstVendorHistory.getApplyDate()) : "";
                }
            }
            Object[] msgparams = new Object[]{userName,vendorName,oldGSTINRegistrationType,oldgstin, oldCustomerVendorType,oldAppliedDateString,
                newGSTINRegistrationType,newgstin, newCustomerVendorType,newAppliedDateString};
            auditMSGForGSTHistory = messageSource.getMessage("acc.save.vendor.gstdetails.update.auditTrail", msgparams, Locale.forLanguageTag(paramJObj.optString(Constants.language)));
        }else{
            /**
             * If New GST details then audit trail entry as added GST details
             */
            Object[] msgparams = new Object[]{userName,vendorName,newGSTINRegistrationType,newgstin,newCustomerVendorType,newAppliedDateString};
            auditMSGForGSTHistory = messageSource.getMessage("acc.save.vendor.gstdetails.add.auditTrail", msgparams, Locale.forLanguageTag(paramJObj.optString(Constants.language)));
        }
        auditTrailObj.insertAuditLog("2218", auditMSGForGSTHistory, auditRequestParamsForGSTHistory, vendorID);

        return returnJSONObj;
    }
    
    public void deleteVendor(JSONObject paramJobj, JSONArray propagatedVendorjarr, boolean propagateTOChildCompaniesFalg, String companyid) throws ServiceException, AccountingException {
        KwlReturnObject result = null;
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            JSONArray jArr = null;
            if (propagateTOChildCompaniesFalg) {
                jArr = propagatedVendorjarr;
            } else {
                requestParams.put(Constants.RES_data, paramJobj.optString(Constants.RES_data));
                jArr = new JSONArray((String) requestParams.get(Constants.RES_data));
            }
            String accountid = "";
            String vendorName = "";
            String vendorCode = "";
            String coaId = "";
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                if (StringUtil.isNullOrEmpty(jobj.getString("accid")) == false) {
                    accountid = jobj.getString("accid");
                    KwlReturnObject vendresult = accountingHandler.getObject(Vendor.class.getName(), accountid);
                    Vendor vendor = (Vendor) vendresult.getEntityList().get(0);
                    vendorName = vendor.getName();
                    vendorCode = vendor.getAcccode();
                    coaId = vendor.getAccount().getID();
                    if (!StringUtil.isNullOrEmpty(accountid)) {//Delete the productvendor mapping
                        accVendorCustomerProductDAOobj.deleteVendorProductMapped(accountid, null);
                    }
                    if (!propagateTOChildCompaniesFalg && jobj.getDouble("openbalance") != 0) {
                        throw new AccountingException(messageSource.getMessage("acc.field.SelectedrecordsishavingtheOpeningBalanceSoitcannotbedeleted", null, Locale.forLanguageTag(paramJobj.optString(Constants.language))));
                    } else {
                        // Check in Journal Entry
                        result = accJournalEntryobj.getJEDfromAccount(accountid, companyid);
                        int count = result.getRecordTotalCount();
                        if (count > 0) {
                            throw new AccountingException("Selected record(s) is currently used in the transaction(s). So it cannot be deleted.");
                        }

                        // Check Product Entry
                        result = accProductObj.getProductfromAccount(accountid, companyid);
                        count = result.getRecordTotalCount();
                        if (count > 0) {
                            throw new AccountingException("Selected record(s) is currently used in the Account Preferences. So it cannot be deleted.");
                        }

                        // Check for Preferances Entry
                        result = accCompanyPreferencesObj.getPreferencesFromAccount(accountid, companyid);
                        count = result.getRecordTotalCount();
                        if (count > 0) {
                            throw new AccountingException("Selected record(s) is currently used in the Product(s). So it cannot be deleted.");
                        }

                        // Check fot Payment Entry
                        result = accPaymentDAOobj.getPaymentMethodFromAccount(accountid, companyid);
                        count = result.getRecordTotalCount();
                        if (count > 0) {
                            throw new AccountingException("Selected record(s) is currently used in the Term(s). So it cannot be deleted.");
                        }

                        // Check for Tax Entry
                        result = accTaxObj.getTaxFromAccount(accountid, companyid);
                        count = result.getRecordTotalCount();
                        if (count > 0) {
                            throw new AccountingException("Selected record(s) is currently used in the Tax(s). So it cannot be deleted.");
                        }

                        result = accVendorDAOobj.getQuotationFromAccount(accountid, companyid);
                        count = result.getRecordTotalCount();
                        if (count > 0) {
                            throw new AccountingException("Selected record(s) is currently used for Vendor Quotations/Asset Vendor Quotations. So it cannot be deleted.");
                        }
                        // Check for GRN Order /Purchase order /Purchase Return /Purchase Invoice/Payment  Entry
                        boolean isused = accCusVenMapDAOObj.isVendorUsedInTransactions(accountid, companyid); //ERP-19783
                        if (isused) {
                            throw new AccountingException("Selected record(s) is currently used in the transaction(s). So it cannot be deleted.");
                        }
                        List childList = new ArrayList(vendor.getChildren());
                        if (childList.size() > 0) {
                            throw new AccountingException("Selected vendor(s) is having child vendor(s). So it cannot be deleted.");
                        }
                        try {
//                        Delete Account
                            HashMap<String, Object> deleteMap = new HashMap<String, Object>();
                            deleteMap.put("vendorId", vendor.getID());
                            deleteMap.put("companyId", companyid);

                            accVendorDAOobj.deleteVendorCategoryMappingDtails(vendor.getID());
                            accVendorDAOobj.deleteVendorAgentMapping(vendor.getID());//delete vendor agent mapping.
                            accVendorDAOobj.updatePreferedVendorinproduct(vendor.getID(), companyid);
                            accVendorDAOobj.deleteCIMBReceivingBankDetails(deleteMap);
                            accVendorDAOobj.deleteIBGReceivingBankDetails(deleteMap);
                            accVendorDAOobj.UpdateCustomerVendorMapping(vendor.getID());
                            if (vendor.getCompany().getCountry().getID().equals("" + Constants.indian_country_id)) {
                                /**
                                 * delete vendor GST Fields history.
                                 */
                                accVendorDAOobj.deleteGstVendorHistory(accountid);
                            }
                            result = accVendorDAOobj.deleteVendor(accountid, companyid);
                            result = accaccountDAO.deleteAccount(accountid, companyid);
                            //change used in if this is last transaction present with mapped acocunt
                            boolean returnSuccess = accaccountDAO.removeEntryFromAccountUsedIn(Constants.Vendor_Default_Account, companyid, coaId);
                        } catch (ServiceException ex) {
                            try {
                                result = accaccountDAO.deleteAccount(accountid, true);
                            } catch (ServiceException x) {
                                throw new AccountingException("Selected record(s) is currently used in the transaction(s).");
                            }
                        }
                    }
                    auditID = AuditAction.VENDOR_DELETED;
                    if (!propagateTOChildCompaniesFalg) {
                        Map<String, Object> auditParamsMap = new HashMap();
                        auditParamsMap.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
                        auditParamsMap.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
                        auditParamsMap.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
                        auditParamsMap.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
                        auditTrailObj.insertAuditLog(auditID, " User " + paramJobj.optString(Constants.userfullname) + " has deleted a Vendor " + vendorName + " (" + vendorCode + ")", auditParamsMap, accountid);
                    }
                }
            }

        } catch (JSONException ex) {
            throw ServiceException.FAILURE("deleteVendor : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteVendor : " + ex.getMessage(), ex);
        }
    }

    /**
     * Code is moved from accVendorControllerCMN.
     * @param paramJobj
     * @return
     * @throws ServiceException
     * @throws AccountingException 
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = {ServiceException.class, AccountingException.class})
    public JSONObject deleteVendor(JSONObject paramJobj) throws ServiceException, AccountingException {
        JSONObject returnJobj = new JSONObject();
        JSONArray propagatedVendorjarr = null;
        boolean propagateToChildCompaniesFlag = false;
        boolean issuccess = false;
        String msg = "";
        try {
            String companyid = paramJobj.optString(Constants.companyid);
            deleteVendor(paramJobj, propagatedVendorjarr, propagateToChildCompaniesFlag, companyid);
            msg = messageSource.getMessage("acc.cus.del", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
            issuccess = true;
            propagateToChildCompaniesFlag = paramJobj.optBoolean("ispropagatetochildcompanyflag", false);
            if (propagateToChildCompaniesFlag) {
                deleteVendorsInChildCompany(paramJobj, propagateToChildCompaniesFlag);
            }
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("accVendorControllerCMNServiceImpl.deleteVendor : " + ex.getMessage(), ex);
        } finally {
            try {
                returnJobj.put(Constants.RES_success, issuccess);
                returnJobj.put(Constants.RES_msg, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                throw ServiceException.FAILURE("accVendorControllerCMNServiceImpl.deleteVendor : " + ex.getMessage(), ex);
            }
        }
        return returnJobj;
    }

    public void deleteVendorsInChildCompany(JSONObject paramJobj, boolean propagateToChildCompaniesFlag) throws ServiceException, AccountingException {
        try {
            JSONArray propagatedVendorjarr = null;
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.RES_data, paramJobj.optString(Constants.RES_data));
            JSONArray jArr = new JSONArray((String) requestParams.get(Constants.RES_data));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject VendorJobj = jArr.getJSONObject(i);
//                    
                String parentCompanyVendorID = VendorJobj.getString("accid");
                String childCompanyName = "";
                String auditID = AuditAction.VENDOR_DELETED;
                HashMap<String, Object> requestParamsPropagatedVendor = new HashMap<String, Object>();
                requestParamsPropagatedVendor.put("parentCompanyVendorID", parentCompanyVendorID);
                KwlReturnObject result = accVendorDAOobj.getChildVendors(requestParamsPropagatedVendor);
                List childCompaniesVendorList = result.getEntityList();

                JSONObject deleteObj = null;
                for (Object childObj : childCompaniesVendorList) {
                    String childcompanysVendorid = "";
                    String childVendorname = "";
                    String ChildVendorCode = "";
                    try {
                        Vendor vendor = (Vendor) childObj;
                        if (vendor != null) {
                            childcompanysVendorid = vendor.getID();
                            childVendorname = vendor.getName();
                            ChildVendorCode = vendor.getAcccode();
                            String childCompanyID = vendor.getCompany().getCompanyID();
                            childCompanyName = vendor.getCompany().getSubDomain();

                            propagatedVendorjarr = new JSONArray();
                            deleteObj = new JSONObject();
                            deleteObj.put("accid", vendor.getID());
                            propagatedVendorjarr.put(deleteObj);
                            deleteVendor(paramJobj, propagatedVendorjarr, propagateToChildCompaniesFlag, childCompanyID);
                            Map<String, Object> auditParamsMap = new HashMap();
                            auditParamsMap.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
                            auditParamsMap.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
                            auditParamsMap.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
                            auditParamsMap.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
                            auditTrailObj.insertAuditLog(auditID, "User " + paramJobj.optString(Constants.userfullname) + " has deleted vendor " + childVendorname + "(" + ChildVendorCode + ")" + " from child company " + childCompanyName, auditParamsMap, childcompanysVendorid);
                        }
                    } catch (Exception ex) {
                        Map<String, Object> auditParamsMap = new HashMap();
                        auditParamsMap.put(Constants.companyKey, paramJobj.optString(Constants.companyKey));
                        auditParamsMap.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
                        auditParamsMap.put(Constants.remoteIPAddress, paramJobj.optString(Constants.remoteIPAddress));
                        auditParamsMap.put(Constants.reqHeader, paramJobj.optString(Constants.reqHeader));
                        auditTrailObj.insertAuditLog(auditID, "vendor " + childVendorname + "(" + ChildVendorCode + ")" + " could not be deleted  from child company " + childCompanyName, auditParamsMap, childcompanysVendorid);
                        throw ServiceException.FAILURE("deleteVendor : " + ex.getMessage(), ex);
                    }
                }
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("deleteVendor : " + ex.getMessage(), ex);
        }
    }
}
