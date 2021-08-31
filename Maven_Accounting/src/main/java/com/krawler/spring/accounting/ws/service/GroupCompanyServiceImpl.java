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
package com.krawler.spring.accounting.ws.service;

import com.krawler.common.admin.*;
import com.krawler.hql.accounting.GroupCompanyTransactionMapping;
import com.krawler.hql.accounting.GroupCompanyProcessMapping;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.hql.accounting.invoice.service.AccInvoiceModuleService;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.goodsreceipt.AccGoodsReceiptServiceDAO;
import com.krawler.spring.accounting.goodsreceipt.service.accGoodsReceiptModuleService;
import com.krawler.spring.accounting.groupcompany.*;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.purchaseorder.service.AccPurchaseOrderModuleService;
import com.krawler.spring.accounting.receivepayment.service.AccReceivePaymentModuleService;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderService;
import com.krawler.spring.accounting.vendorpayment.service.AccVendorPaymentModuleService;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author krawler
 */
public class GroupCompanyServiceImpl implements GroupCompanyService,MessageSourceAware {
    private WSUtilService wsUtilService;
    private TransactionService transactionService;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private AccGroupCompanyDAO accGroupCompanyDAO;
    private accInvoiceDAO accInvoiceDAOobj;
    private MessageSource messageSource;
    private AccPurchaseOrderModuleService accPurchaseOrderModuleServiceObj;
    private AccInvoiceModuleService accInvoiceModuleService;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAO;
    private AccVendorPaymentModuleService accVendorPaymentModuleServiceObj;
    private accGoodsReceiptModuleService accGoodsReceiptModuleService;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private accSalesOrderDAO accSalesOrderDAOObj;
    private AccReceivePaymentModuleService accReceivePaymentModuleServiceObj;
    private accSalesOrderService accSalesOrderServiceobj;

    /**
     * @param accReceivePaymentModuleServiceObj the
     * accReceivePaymentModuleServiceObj to set
     */
    public void setAccReceivePaymentModuleServiceObj(AccReceivePaymentModuleService accReceivePaymentModuleServiceObj) {
        this.accReceivePaymentModuleServiceObj = accReceivePaymentModuleServiceObj;
    }
    
    public void setAccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOObj) {
        this.accSalesOrderDAOObj = accSalesOrderDAOObj;
    }
    public void setAccSalesOrderServiceobj(accSalesOrderService accSalesOrderServiceobj) {
        this.accSalesOrderServiceobj = accSalesOrderServiceobj;
    }
    
    public void setAccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setAccGoodsReceiptModuleService(accGoodsReceiptModuleService accGoodsReceiptModuleService) {
        this.accGoodsReceiptModuleService = accGoodsReceiptModuleService;
    }

    /**
     * @param accVendorPaymentModuleServiceObj the
     * accVendorPaymentModuleServiceObj to set
     */
    public void setAccVendorPaymentModuleServiceObj(AccVendorPaymentModuleService accVendorPaymentModuleServiceObj) {
        this.accVendorPaymentModuleServiceObj = accVendorPaymentModuleServiceObj;
    }

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    public void setaccGoodsReceiptServiceDAO(AccGoodsReceiptServiceDAO accGoodsReceiptServiceDAO) {
        this.accGoodsReceiptServiceDAO = accGoodsReceiptServiceDAO;
    }

    
    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }

    public void setAccPurchaseOrderModuleServiceObj(AccPurchaseOrderModuleService accPurchaseOrderModuleServiceObj) {
        this.accPurchaseOrderModuleServiceObj = accPurchaseOrderModuleServiceObj;
    }

    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }

    public void setaccGroupCompanyDAO(AccGroupCompanyDAO accGroupCompanyDAO) {
        this.accGroupCompanyDAO = accGroupCompanyDAO;
    }

    public void setwsUtilService(WSUtilService wsUtilService) {
        this.wsUtilService = wsUtilService;
    }

    public void settransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setaccInvoiceModuleService(AccInvoiceModuleService accInvoiceModuleService) {
        this.accInvoiceModuleService = accInvoiceModuleService;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    private JSONObject jsonCreateSaveSalesorder(JSONObject paramJobj) throws JSONException, ServiceException {
        JSONObject saveSOJson = paramJobj;
        String poid=paramJobj.optString(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID);
        boolean isEdit = StringUtil.isNullOrEmpty(saveSOJson.optString(Constants.isEdit, null)) ? false : Boolean.parseBoolean(saveSOJson.getString(Constants.isEdit));
        boolean isCopy = StringUtil.isNullOrEmpty(saveSOJson.optString("copyInv", null)) ? false : Boolean.parseBoolean(saveSOJson.getString("copyInv"));
        if (isCopy) {
            isEdit = false;
            saveSOJson.put(Constants.isEdit, "false");
        }

        if (saveSOJson.optString(Constants.sequenceformat, "NA").equalsIgnoreCase("NA")) {
            saveSOJson.put("salesOrderNumber", saveSOJson.optString("invoiceNo"));
        } else {
            saveSOJson.remove("salesOrderNumber");
            saveSOJson.remove(Constants.sequenceformat);
        }

        if (!saveSOJson.has(Constants.sequenceformat) || StringUtil.isNullOrEmpty(saveSOJson.optString(Constants.sequenceformat, null))) {
            String sequenceformatid = null;
            Map<String, Object> sfrequestParams = new HashMap<String, Object>();
            sfrequestParams.put(Constants.companyKey, saveSOJson.get(Constants.companyKey));//Destination companyid
            sfrequestParams.put("modulename", "autoso");
            KwlReturnObject seqFormatResult = accCompanyPreferencesObj.getSequenceFormat(sfrequestParams);
            List<SequenceFormat> ll = seqFormatResult.getEntityList();
            if (ll.size() > 0) {
                SequenceFormat format = (SequenceFormat) ll.get(0);
                sequenceformatid = format.getID();
                saveSOJson.put(Constants.sequenceformat, sequenceformatid);
            } else {
                if (!StringUtil.isNullOrEmpty(poid)) {
                    KwlReturnObject rst = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), poid);
                    PurchaseOrder po = (PurchaseOrder) rst.getEntityList().get(0);
                    if (po != null) {
                        saveSOJson.put(Constants.sequenceformat, "NA");
                        saveSOJson.put("salesOrderNumber", po.getPurchaseOrderNumber());
                    }
                }
            }
        }//end of sequenceformat

        saveSOJson.remove(Constants.billid);
        saveSOJson.remove("invoiceid");

        saveSOJson.put("customerPORefNo", saveSOJson.opt("customerporefno"));
        saveSOJson.put("gstIncluded", saveSOJson.opt("includingGST"));
        saveSOJson.put("currencyName", saveSOJson.opt(Constants.currencyKey));
        saveSOJson.put("OrderDate", saveSOJson.opt(Constants.BillDate));

        String customField = saveSOJson.optString(Constants.customfield, null);
        if (!StringUtil.isNullOrEmpty(customField)) {
            JSONArray customJArray = wsUtilService.createJSONForCustomField(customField, saveSOJson.optString(Constants.companyKey), Constants.Acc_Sales_Order_ModuleId);
            saveSOJson.put(Constants.customfield, customJArray);
        }

        /*
         * In edit case getting the salesorder transaction id from
         * multitransactionmapping table and editing salesorder
         */
        if (isEdit) {//when copied po and then isEdit flag is coming as true
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_MODULE, saveSOJson.optString(GroupCompanyProcessMapping.SOURCE_MODULE));
            fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID, poid);
            fieldrequestParams.put(GroupCompanyProcessMapping.DESTINATION_MODULE, saveSOJson.optString(Constants.moduleid));
            KwlReturnObject result = accGroupCompanyDAO.fetchTransactionMappingDetails(fieldrequestParams);
            List<GroupCompanyTransactionMapping> multiTransObj = result.getEntityList();

            if (multiTransObj.size() > 0) {
                for (GroupCompanyTransactionMapping multTMObj : multiTransObj) {
                    String destinationTransactionId = multTMObj.getDestinationTransactionid();
                    KwlReturnObject soresult = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), destinationTransactionId);
                    SalesOrder so = (SalesOrder) soresult.getEntityList().get(0);
                    if (so != null) {

                        if (StringUtil.isNullOrEmpty(saveSOJson.optString("salesOrderNumber", null))) {
                            saveSOJson.put("salesOrderNumber", so.getSalesOrderNumber());
                        }
                        if (!saveSOJson.optString(Constants.sequenceformat, "NA").equalsIgnoreCase("NA")) {
                            saveSOJson.put(Constants.sequenceformat, so.getSeqformat());
                        }
                        saveSOJson.put(Constants.billid, destinationTransactionId);
                    }
                }
            } else {
                throw ServiceException.FAILURE("Some error while creating JSON for Sales Order.", "erp38", false);
            }
        }
        return saveSOJson;
    }

    /*
     * @Description: Convert Purchase Order to Sales Order
     * @param: JSONObject paramJObj 
     * @Mandatory fields: Destination Company and  Destination module
     */
    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public JSONObject convertPOtoSO(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject response = new JSONObject();
        boolean issuccess = false;
        String msg = null;
        try {
            if (!paramJobj.has(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN) || !paramJobj.has(GroupCompanyProcessMapping.DESTINATION_MODULE) || !paramJobj.has(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN) || !paramJobj.has(GroupCompanyProcessMapping.SOURCE_MODULE)) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
            String moduleid = paramJobj.optString(Constants.moduleid);//sales order module id


            if (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Sales_Order_ModuleId))) { //po to so
                JSONObject salesOrderReqJson = wsUtilService.populateAdditionalInformation(paramJobj);
                salesOrderReqJson = jsonCreateSaveSalesorder(salesOrderReqJson);
                boolean isEdit = StringUtil.isNullOrEmpty(salesOrderReqJson.optString(Constants.isEdit, null)) ? false : Boolean.parseBoolean(salesOrderReqJson.getString(Constants.isEdit));
                //isEdit value is changes in jsonCreateSaveSalesorder in case of copy po case
                salesOrderReqJson = wsUtilService.populateMastersInformation(salesOrderReqJson);
                response = transactionService.saveSalesOrder(salesOrderReqJson);
                if (response.has(Constants.RES_success) && response.optBoolean(Constants.RES_success)) {
                    issuccess = true;
                }

                if (issuccess && !isEdit) {//Not to update multitransactionmapping table in case of Edit mode
                    JSONObject requestJSON = new JSONObject();
                    requestJSON.put(GroupCompanyProcessMapping.SOURCE_MODULE, paramJobj.optString(GroupCompanyProcessMapping.SOURCE_MODULE));
                    requestJSON.put(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID, paramJobj.optString(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID));
                    requestJSON.put(GroupCompanyProcessMapping.DESTINATION_MODULE, moduleid);
                    requestJSON.put(GroupCompanyProcessMapping.DESTINATION_TRANSACTIONID, response.optString(Constants.billid));
                    accGroupCompanyDAO.saveDocumentTransactionsid(requestJSON);
                }

                if (issuccess && response.has(Constants.RES_MESSAGE)) {
                    msg = response.getString(Constants.RES_MESSAGE);
                } else {
                    throw ServiceException.FAILURE("Some issue occurred while saving the transaction.", "erp33", false);

                }
            }

        } catch (Exception ex) {
            issuccess = false;
        } finally {
            response.put(Constants.RES_MESSAGE, msg);
            response.put(Constants.RES_success, issuccess);

        }
        return response;
    }

      
    private JSONObject jsonCreateSaveSalesReturn(JSONObject paramJobj) throws JSONException, ServiceException, AccountingException {
        JSONObject saveSRJson = paramJobj;
        String prid=saveSRJson.optString(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID);
        boolean isEdit = StringUtil.isNullOrEmpty(saveSRJson.optString(Constants.isEdit, null)) ? false : Boolean.parseBoolean(saveSRJson.getString(Constants.isEdit));
        boolean isCopy = StringUtil.isNullOrEmpty(saveSRJson.optString("copyInv", null)) ? false : Boolean.parseBoolean(saveSRJson.getString("copyInv"));

        if (isCopy) {
            isEdit = false;
            saveSRJson.put(Constants.isEdit, "false");
        }

        if (!saveSRJson.optString(Constants.sequenceformat, "NA").equalsIgnoreCase("NA")) {
            saveSRJson.remove(Constants.sequenceformat);
            saveSRJson.remove("number");
        }

        if (!saveSRJson.has(Constants.sequenceformat) || StringUtil.isNullOrEmpty(saveSRJson.optString(Constants.sequenceformat, null))) {
            String sequenceformatid = null;
            Map<String, Object> sfrequestParams = new HashMap<String, Object>();
            sfrequestParams.put(Constants.companyKey, saveSRJson.get(Constants.companyKey));
            sfrequestParams.put("modulename", "autosr");
            KwlReturnObject seqFormatResult = accCompanyPreferencesObj.getSequenceFormat(sfrequestParams);
            List<SequenceFormat> ll = seqFormatResult.getEntityList();
            if (ll.size() > 0) {
                SequenceFormat format = (SequenceFormat) ll.get(0);
                sequenceformatid = format.getID();
                saveSRJson.put(Constants.sequenceformat, sequenceformatid);
                if (!isEdit) {
                    boolean seqformat_oldflag = StringUtil.getBoolean(paramJobj.optString("seqformat_oldflag"));
                    String nextAutoNumber = "";
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    if (seqformat_oldflag) {
                        nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(saveSRJson.optString(Constants.companyKey), StaticValues.AUTONUM_SALESRETURN, sequenceformatid);
                        seqNumberMap.put(Constants.AUTO_ENTRYNUMBER, nextAutoNumber);
                    } else {
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(saveSRJson.optString(Constants.companyKey), StaticValues.AUTONUM_SALESRETURN, sequenceformatid, seqformat_oldflag, new Date());
                    }

                    if (seqNumberMap.containsKey("autoentrynumber") && seqNumberMap.get("autoentrynumber") != null) {
                        saveSRJson.put("number", seqNumberMap.get("autoentrynumber"));
                    }
                }

            } else if (!StringUtil.isNullOrEmpty(prid)) {
                KwlReturnObject customerresult = accountingHandlerDAOobj.getObject(PurchaseReturn.class.getName(), prid);
                PurchaseReturn purchaseReturn = (PurchaseReturn) customerresult.getEntityList().get(0);
                if (purchaseReturn != null) {
                    saveSRJson.put(Constants.sequenceformat, "NA");
                    saveSRJson.put("number", purchaseReturn.getPurchaseReturnNumber());
                }
            }
        }//end of sequenceformat

        saveSRJson.remove(Constants.billid);

        Map<String, Object> invoiceParams = new HashMap<>();
        StringBuilder linkNumberBuilderString = new StringBuilder();
        StringBuilder rowDetailBuilderString = new StringBuilder();
        boolean isLinkedDocument=false;
        if (!StringUtil.isNullOrEmpty(saveSRJson.optString("fromLinkCombo", null)) && saveSRJson.optString("fromLinkCombo").equals(Constants.VENDOR_INVOICE) && !StringUtil.isNullOrEmpty(saveSRJson.optString("linkNumber", null))) {

            String[] linkNumbers = saveSRJson.optString("linkNumber").split(",");
            for (int i = 0; i < linkNumbers.length; i++) {
                String grid = linkNumbers[i];
                String linkedDestiantionTransactionModule = String.valueOf(Constants.Acc_Invoice_ModuleId);
                String linkedSourceTransactionModule = String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId);

                HashMap<String, Object> fieldrequestParams = new HashMap();
                fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_MODULE, linkedSourceTransactionModule);
                fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID, grid);
                fieldrequestParams.put(GroupCompanyProcessMapping.DESTINATION_MODULE, linkedDestiantionTransactionModule);
                KwlReturnObject result = accGroupCompanyDAO.fetchTransactionMappingDetails(fieldrequestParams);
                List<GroupCompanyTransactionMapping> multiTransObj = result.getEntityList();

                if (multiTransObj.size() > 0) {
                    for (GroupCompanyTransactionMapping multTMObj : multiTransObj) {
                        String destinationTransactionId = multTMObj.getDestinationTransactionid();
                        if (linkNumberBuilderString.length() > 0) {
                            linkNumberBuilderString.append("," + destinationTransactionId);
                        } else {
                            linkNumberBuilderString.append(destinationTransactionId);
                        }
                        KwlReturnObject soresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), destinationTransactionId);
                        Invoice srObj = (Invoice) soresult.getEntityList().get(0);
                        if (srObj != null) {

                            Set<InvoiceDetail> invDetails = srObj.getRows();
                            if (invDetails != null) {
                                for (InvoiceDetail invdetailObj : invDetails) {
                                    if (rowDetailBuilderString.length() > 0) {
                                        rowDetailBuilderString.append("," +  invdetailObj.getID());
                                    } else {
                                        rowDetailBuilderString.append( invdetailObj.getID());
                                    }
                                    invoiceParams.put(destinationTransactionId, invdetailObj.getID());
                                }
                            }
                        }
                    }

                } else {
                    JSONObject response = StringUtil.getErrorResponse("acc.common.erp53", saveSRJson, "Cannot find Sales Invoice linked document for Vendor Invoice.", messageSource);
                    throw ServiceException.FAILURE(response.optString(Constants.RES_MESSAGE), "", false);
                }
            }
            saveSRJson.put("linkNumber", linkNumberBuilderString.toString());
            saveSRJson.put("fromLinkCombo", Constants.CUSTOMER_INVOICE);
        }

        JSONArray jArray = new JSONArray();
        if (saveSRJson.has(Constants.detail) && !StringUtil.isNullOrEmpty(saveSRJson.optString(Constants.detail, null))) {
            String detail = (String) paramJobj.optString(Constants.detail, "[{}]");
            JSONArray jArr = new JSONArray(detail);
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject detailObj = jArr.getJSONObject(i);
                KwlReturnObject uomreturnObj = accountingHandlerDAOobj.getObject(UnitOfMeasure.class.getName(), detailObj.optString("uomid"));
                UnitOfMeasure uomObj = (UnitOfMeasure) uomreturnObj.getEntityList().get(0);
                if (uomObj != null) {
                    detailObj.put("uomname", uomObj.getNameEmptyforNA());
                }
                detailObj.put("discountType", detailObj.optString("prdiscount"));
                detailObj.put("desc", detailObj.optString("description"));
                jArray.put(detailObj);
            }
        }

        if (jArray.length() > 0) {
            paramJobj.put(Constants.detail, jArray.toString());
        }

        String customField = paramJobj.optString(Constants.customfield, null);
        if (!StringUtil.isNullOrEmpty(customField)) {
            JSONArray customJArray = wsUtilService.createJSONForCustomField(customField, saveSRJson.optString(Constants.companyKey), Constants.Acc_Sales_Return_ModuleId);
            saveSRJson.put(Constants.customfield, customJArray);
        }

        /*
         * In edit case getting the salesorder transaction id from
         * multitransactionmapping table and editing salesorder
         */
        if (isEdit) {//when copied po and then isEdit flag is coming as true
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_MODULE, saveSRJson.optString(GroupCompanyProcessMapping.SOURCE_MODULE));
            fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID, prid);
            fieldrequestParams.put(GroupCompanyProcessMapping.DESTINATION_MODULE, saveSRJson.optString(Constants.moduleid));
            KwlReturnObject result = accGroupCompanyDAO.fetchTransactionMappingDetails(fieldrequestParams);
            List<GroupCompanyTransactionMapping> multiTransObj = result.getEntityList();

            if (multiTransObj.size() > 0) {
                for (GroupCompanyTransactionMapping multTMObj : multiTransObj) {
                    String destinationTransactionId = multTMObj.getDestinationTransactionid();
                    KwlReturnObject soresult = accountingHandlerDAOobj.getObject(SalesReturn.class.getName(), destinationTransactionId);
                    SalesReturn srObj = (SalesReturn) soresult.getEntityList().get(0);
                    if (srObj != null) {

                        if (StringUtil.isNullOrEmpty(saveSRJson.optString("number", null))) {
                            saveSRJson.put("number", srObj.getSalesReturnNumber());
                        }
                        if (!saveSRJson.optString(Constants.sequenceformat, "NA").equalsIgnoreCase("NA")) {
                            saveSRJson.put(Constants.sequenceformat, srObj.getSeqformat());
                        }
                        saveSRJson.put(Constants.billid, destinationTransactionId);
                    }
                }
            } else {
                throw ServiceException.FAILURE("Some error while creating JSON for Sales Order.", "erp38", false);
            }
        }
        return saveSRJson;
    }

    /*
     * @Description: Convert Purchase Return to Sales Return:- Only Normal Case. Not handled for against Credit Note/Debit Note
     * @param: JSONObject paramJobj
     * @Mandatory fields: Destination Company and destination module
     * return JSONObject
     */
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject convertPurchaseReturnToSalesReturn(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject response = new JSONObject();
        boolean issuccess = false;
        String msg = null;
        try {
            if (!paramJobj.has(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN) || !paramJobj.has(GroupCompanyProcessMapping.DESTINATION_MODULE) || !paramJobj.has(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN) || !paramJobj.has(GroupCompanyProcessMapping.SOURCE_MODULE)) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }

            String moduleid = paramJobj.optString(Constants.moduleid);//sales order module id

            if (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Sales_Return_ModuleId))) {//pr to sr
                JSONObject salesReturnReqJson = wsUtilService.populateAdditionalInformation(paramJobj);
                salesReturnReqJson = jsonCreateSaveSalesReturn(paramJobj);
                salesReturnReqJson = wsUtilService.populateMastersInformation(salesReturnReqJson);
                //isEdit value is changes in jsonCreateSaveSalesorder in case of copy po case
                boolean isEdit = StringUtil.isNullOrEmpty(salesReturnReqJson.optString(Constants.isEdit, null)) ? false : Boolean.parseBoolean(salesReturnReqJson.getString(Constants.isEdit));
                response = transactionService.saveSalesReturn(salesReturnReqJson);

                if (response.has(Constants.RES_success) && response.optBoolean(Constants.RES_success)) {
                    issuccess = true;
                    String sequenceformat = salesReturnReqJson.optString(Constants.sequenceformat, null);
                    String destinationcompanyid = salesReturnReqJson.optString(Constants.companyKey);
                    KwlReturnObject customerresult = accountingHandlerDAOobj.getObject(SalesReturn.class.getName(), response.optString(Constants.billid));
                    SalesReturn salesReturn = (SalesReturn) customerresult.getEntityList().get(0);
                    if (salesReturn != null) {
                        if (!sequenceformat.equals("NA") && !isEdit && !StringUtil.isNullOrEmpty(sequenceformat)) {
                            boolean seqformat_oldflag = StringUtil.getBoolean(paramJobj.optString("seqformat_oldflag"));
                            String nextAutoNumber = "";
                            Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                            if (seqformat_oldflag) {
                                nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(destinationcompanyid, StaticValues.AUTONUM_SALESRETURN, sequenceformat);
                                seqNumberMap.put(Constants.AUTO_ENTRYNUMBER, nextAutoNumber);
                            } else {
                                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(destinationcompanyid, StaticValues.AUTONUM_SALESRETURN, sequenceformat, seqformat_oldflag, salesReturn.getOrderDate());
                            }
                            seqNumberMap.put(Constants.DOCUMENTID, response.optString(Constants.billid));
                            seqNumberMap.put(Constants.companyKey, destinationcompanyid);
                            seqNumberMap.put(Constants.SEQUENCEFORMATID, sequenceformat);
                            String billno = accSalesOrderDAOObj.updateSREntryNumberForNewSR(seqNumberMap);
                        }
                    }
                }

                if (issuccess && !isEdit) {//Not to update multitransactionmapping table in case of Edit mode
                    JSONObject requestJSON = new JSONObject();
                    requestJSON.put(GroupCompanyProcessMapping.SOURCE_MODULE, paramJobj.optString(GroupCompanyProcessMapping.SOURCE_MODULE));
                    requestJSON.put(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID, paramJobj.optString(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID));
                    requestJSON.put(GroupCompanyProcessMapping.DESTINATION_MODULE, moduleid);
                    requestJSON.put(GroupCompanyProcessMapping.DESTINATION_TRANSACTIONID, response.optString(Constants.billid));
                    accGroupCompanyDAO.saveDocumentTransactionsid(requestJSON);
                }

                if (issuccess && response.has(Constants.RES_MESSAGE)) {
                    msg = response.getString(Constants.RES_MESSAGE);
                } else {
                    throw ServiceException.FAILURE("Some issue occurred while saving the transaction.", "erp33", false);

                }
            }
        } catch (Exception ex) {
            issuccess = false;
        } finally {
            response.put(Constants.RES_MESSAGE, msg);
            response.put(Constants.RES_success, issuccess);
        }
        return response;
    }

    private JSONObject jsonCreateReceiptPayment(JSONObject paramJobj) throws JSONException, ServiceException {
        JSONObject saveRPJson = paramJobj;
        String rpid=saveRPJson.optString(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID);
        boolean isEdit = !StringUtil.isNullOrEmpty(saveRPJson.optString(Constants.billid, null)) ? true :false;
        boolean isCopy = StringUtil.isNullOrEmpty(saveRPJson.optString("copyInv", null)) ? false : Boolean.parseBoolean(saveRPJson.getString("copyInv"));
        if (isCopy) {
            isEdit = false;
            saveRPJson.put(Constants.isEdit, "false");
        }

        if (!saveRPJson.optString(Constants.sequenceformat, "NA").equalsIgnoreCase("NA")) {
            saveRPJson.remove(Constants.sequenceformat);
        }

        if (!paramJobj.has(Constants.sequenceformat) || StringUtil.isNullOrEmpty(paramJobj.optString(Constants.sequenceformat, null))) {
            String sequenceformatid = null;
            Map<String, Object> sfrequestParams = new HashMap<String, Object>();
            sfrequestParams.put(Constants.companyKey, paramJobj.get(Constants.companyKey));
            sfrequestParams.put("modulename", "autoreceipt");
            KwlReturnObject seqFormatResult = accCompanyPreferencesObj.getSequenceFormat(sfrequestParams);
            List<SequenceFormat> ll = seqFormatResult.getEntityList();
            if (ll.size()>0) {
                SequenceFormat format = (SequenceFormat) ll.get(0);
                sequenceformatid = format.getID();
                paramJobj.put(Constants.sequenceformat, sequenceformatid);
            }  else if (!StringUtil.isNullOrEmpty(rpid)) {
                KwlReturnObject customerresult = accountingHandlerDAOobj.getObject(Payment.class.getName(), rpid);
                Payment payment = (Payment) customerresult.getEntityList().get(0);
                if (payment != null) {
                    saveRPJson.put(Constants.sequenceformat, "NA");
                    saveRPJson.put("no", payment.getPaymentNumber());
                }
            }
        }//end of sequenceformat

        saveRPJson.remove(Constants.billid);
        boolean isCustomer = Boolean.parseBoolean(saveRPJson.optString("iscustomer"));
        saveRPJson.put("iscustomer", Boolean.toString(!isCustomer));// For receive payment against customer

        String customField = saveRPJson.optString(Constants.customfield, null);
        if (!StringUtil.isNullOrEmpty(customField)) {
            JSONArray customJArray = wsUtilService.createJSONForCustomField(customField, saveRPJson.optString(Constants.companyKey), Constants.Acc_Receive_Payment_ModuleId);
            saveRPJson.put(Constants.customfield, customJArray);
        }

        /*
         * In edit case getting the salesorder transaction id from
         * multitransactionmapping table and editing salesorder
         */
        if (isEdit) {//when copied po and then isEdit flag is coming as true
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_MODULE, saveRPJson.optString(GroupCompanyProcessMapping.SOURCE_MODULE));
            fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID, rpid);
            fieldrequestParams.put(GroupCompanyProcessMapping.DESTINATION_MODULE, saveRPJson.optString(Constants.moduleid));
            KwlReturnObject result = accGroupCompanyDAO.fetchTransactionMappingDetails(fieldrequestParams);
            List<GroupCompanyTransactionMapping> multiTransObj = result.getEntityList();

            if (multiTransObj.size() > 0) {
                for (GroupCompanyTransactionMapping multTMObj : multiTransObj) {
                    String destinationTransactionId = multTMObj.getDestinationTransactionid();
                    KwlReturnObject soresult = accountingHandlerDAOobj.getObject(Receipt.class.getName(), destinationTransactionId);
                    Receipt rpObj = (Receipt) soresult.getEntityList().get(0);
                    if (rpObj != null) {
                        if (StringUtil.isNullOrEmpty(saveRPJson.optString("no", null))) {
                            saveRPJson.put("no", rpObj.getReceiptNumber());
                        }
                        if (!saveRPJson.optString(Constants.sequenceformat, "NA").equalsIgnoreCase("NA")) {
                            saveRPJson.put(Constants.sequenceformat, rpObj.getSeqformat());
                        }
                        saveRPJson.put(Constants.billid, destinationTransactionId);
                    }
                }
            } else {
                throw ServiceException.FAILURE("Some error while creating JSON for Receive Payment.", "", false);
            }
        }
        return saveRPJson;
    }

    /*
     * @Description: Convert Make Payment to Receive Payment:- Only Against Invoice case
     * @param: JSONObject paramJobj
     * @Mandatory fields: Destination Company and destination module
     * return JSONObject
     */
    
    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public JSONObject convertMakePaymenttoReceivePayment(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject response = new JSONObject();
        boolean issuccess = false;
        String msg = null;
        String moduleid = paramJobj.optString(Constants.moduleid);//receive payment module id
        String paymentno = paramJobj.optString("no");//receive payment module id
        try {
            if (!paramJobj.has(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN) || !paramJobj.has(GroupCompanyProcessMapping.DESTINATION_MODULE) || !paramJobj.has(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN) || !paramJobj.has(GroupCompanyProcessMapping.SOURCE_MODULE)) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }

            if (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {
                JSONObject receivePaymentReqJson = wsUtilService.populateAdditionalInformation(paramJobj);
                receivePaymentReqJson = jsonCreateReceiptPayment(receivePaymentReqJson);
                receivePaymentReqJson = wsUtilService.populateMastersInformation(receivePaymentReqJson);
                boolean isEdit = StringUtil.isNullOrEmpty(receivePaymentReqJson.optString(Constants.isEdit, null)) ? false : Boolean.parseBoolean(receivePaymentReqJson.getString(Constants.isEdit));
                response = transactionService.saveReceiptPayment(receivePaymentReqJson);

                if (response.has(Constants.RES_success) && response.optBoolean(Constants.RES_success)) {
                    issuccess = true;
                }

                if (issuccess && !isEdit) {//Not to update multitransactionmapping table in case of Edit mode
                    JSONObject requestJSON = new JSONObject();
                    requestJSON.put(GroupCompanyProcessMapping.SOURCE_MODULE, paramJobj.optString(GroupCompanyProcessMapping.SOURCE_MODULE));
                    requestJSON.put(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID, paramJobj.optString(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID));
                    requestJSON.put(GroupCompanyProcessMapping.DESTINATION_MODULE, moduleid);
                    requestJSON.put(GroupCompanyProcessMapping.DESTINATION_TRANSACTIONID, response.optString("paymentid"));
                    accGroupCompanyDAO.saveDocumentTransactionsid(requestJSON);
                }

                if (issuccess && response.has(Constants.RES_MESSAGE)) {
                    msg = response.getString(Constants.RES_MESSAGE);
                } else {
                    accCommonTablesDAO.deleteTransactionInTemp(paymentno, paramJobj.optString(GroupCompanyProcessMapping.SOURCE_COMPANYID), Constants.Acc_Make_Payment_ModuleId);
                    throw ServiceException.FAILURE("Some issue occurred while saving the transaction.", "erp33", false);

                }
            }

        } catch (Exception ex) {
            accCommonTablesDAO.deleteTransactionInTemp(paymentno, paramJobj.optString(GroupCompanyProcessMapping.SOURCE_COMPANYID), Constants.Acc_Make_Payment_ModuleId);
            issuccess = false;
        } finally {
            response.put(Constants.RES_MESSAGE, msg);
            response.put(Constants.RES_success, issuccess);

        }
        return response;
    }

    /*
     * @Description: Convert Debit Note to Credit Note 
     * @param: JSONObject
     * paramJObj @Mandatory fields: Destination Company and destination module
     */
    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public JSONObject convertDebitNotetoCreditNote(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject response = new JSONObject();
        boolean issuccess = false;
        String msg = null;
        try {
            if (!paramJobj.has(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN) || !paramJobj.has(GroupCompanyProcessMapping.DESTINATION_MODULE) || !paramJobj.has(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN) || !paramJobj.has(GroupCompanyProcessMapping.SOURCE_MODULE)) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }

            String destinationModuleid = paramJobj.optString(GroupCompanyProcessMapping.DESTINATION_MODULE);//vendor invoice module id
            String destinationCompany = paramJobj.optString(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN);
            String sourceCompany = paramJobj.optString(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN);
            String sourceModuleid = paramJobj.optString(GroupCompanyProcessMapping.SOURCE_MODULE);
            String sourceDocumentId = paramJobj.optString(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID);

            if (destinationModuleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Invoice_ModuleId))) { //vi to si
                JSONObject  salesInvoiceReqJson = wsUtilService.populateAdditionalInformation(paramJobj);
                salesInvoiceReqJson = jsonCreateInvoice(paramJobj);
                //isEdit value is changes in jsonCreateInvoice in case of copy VI case
                boolean isEdit = StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isEdit, null)) ? false : Boolean.parseBoolean(paramJobj.getString(Constants.isEdit));
                salesInvoiceReqJson = wsUtilService.populateMastersInformation(salesInvoiceReqJson);
                response = transactionService.saveInvoice(salesInvoiceReqJson);

                if (response.has(Constants.RES_success) && response.optBoolean(Constants.RES_success)) {
                    issuccess = true;
                }

                if (issuccess && !isEdit) {//Not to update multitransactionmapping table in case of Edit mode
                    JSONObject requestJSON = new JSONObject();

                    requestJSON.put(GroupCompanyProcessMapping.SOURCE_MODULE, sourceModuleid);
                    requestJSON.put(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID, sourceDocumentId);
                    requestJSON.put(GroupCompanyProcessMapping.DESTINATION_MODULE, destinationModuleid);
                    requestJSON.put(GroupCompanyProcessMapping.DESTINATION_TRANSACTIONID, response.optString("invid"));
                    accGroupCompanyDAO.saveDocumentTransactionsid(requestJSON);
                }

                if (issuccess && response.has(Constants.RES_MESSAGE)) {
                    msg = response.getString(Constants.RES_MESSAGE);
                } else {
                    throw ServiceException.FAILURE("Some issue occurred while saving the transaction.", "erp33", false);

                }
            }
        } catch (Exception ex) {
            issuccess = false;
        } finally {
            response.put(Constants.RES_MESSAGE, msg);
            response.put(Constants.RES_success, issuccess);

        }
        return response;
    }

    private JSONObject jsonCreateInvoice(JSONObject paramJobj) throws JSONException, ServiceException {
        JSONObject invoiceJson = paramJobj;
        String grid=invoiceJson.optString(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID);
        boolean inCash = Boolean.parseBoolean(invoiceJson.optString("incash"));
        boolean isEdit = StringUtil.isNullOrEmpty(invoiceJson.optString(Constants.isEdit, null)) ? false : Boolean.parseBoolean(invoiceJson.getString(Constants.isEdit));
        boolean isCopy = StringUtil.isNullOrEmpty(invoiceJson.optString("copyInv", null)) ? false : Boolean.parseBoolean(invoiceJson.getString("copyInv"));
        invoiceJson.remove(Constants.billid);
        if (isCopy) {
            isEdit = false;
            invoiceJson.put(Constants.isEdit, "false");
        }
        if (invoiceJson.optString(Constants.sequenceformat, "NA").equalsIgnoreCase("NA")) {
            invoiceJson.put("invoiceNumber", invoiceJson.optString("invoiceNo"));
        } else {
            invoiceJson.remove("invoiceNumber");
            invoiceJson.remove(Constants.sequenceformat);
        }


        if (!invoiceJson.has(Constants.sequenceformat) || StringUtil.isNullOrEmpty(invoiceJson.optString(Constants.sequenceformat, null))) {
            String sequenceformatid = null;
            Map<String, Object> sfrequestParams = new HashMap<String, Object>();
            sfrequestParams.put(Constants.companyKey, invoiceJson.get(Constants.companyKey));

            if (inCash) {
                sfrequestParams.put("modulename", "autocashsales");
            } else {
                sfrequestParams.put("modulename", "autoinvoice");
            }
            KwlReturnObject seqFormatResult = accCompanyPreferencesObj.getSequenceFormat(sfrequestParams);
            List<SequenceFormat> ll = seqFormatResult.getEntityList();
            if (ll.size() > 0) {
                SequenceFormat format = (SequenceFormat) ll.get(0);
                sequenceformatid = format.getID();
                invoiceJson.put(Constants.sequenceformat, sequenceformatid);
            } else if (!StringUtil.isNullOrEmpty(grid)) {
                KwlReturnObject invObj = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), grid);
                GoodsReceipt gr = (GoodsReceipt) invObj.getEntityList().get(0);
                if (gr != null) {
                    invoiceJson.put(Constants.sequenceformat, "NA");
                    invoiceJson.put("invoiceNumber",gr.getGoodsReceiptNumber());
                }
            }
        }//end of sequenceformat


        if (invoiceJson.optBoolean("isAutoCreateDO", false) == true) {
            invoiceJson.put(Constants.fromLinkComboAutoDO, Constants.CUSTOMER_INVOICE);
        } else {
            invoiceJson.remove("doid");
        }

        if (invoiceJson.optString(Constants.sequenceformatDo, "NA").equalsIgnoreCase("NA")) {
            invoiceJson.put("deliveryOrderNo", invoiceJson.optString("numberDo"));
        } else {
            invoiceJson.remove("deliveryOrderNo");
            invoiceJson.remove(Constants.sequenceformatDo);
        }

        if (!invoiceJson.has(Constants.sequenceformatDo) || StringUtil.isNullOrEmpty(Constants.sequenceformatDo) && invoiceJson.optBoolean("isAutoCreateDO", false) == true) {
            String sequenceformatid = null;
            Map<String, Object> sfrequestParams = new HashMap<String, Object>();
            sfrequestParams.put(Constants.companyKey, paramJobj.get(Constants.companyKey));
            sfrequestParams.put("modulename", "autodo");
            KwlReturnObject seqFormatResult = accCompanyPreferencesObj.getSequenceFormat(sfrequestParams);
            List<SequenceFormat> ll = seqFormatResult.getEntityList();
            if (ll.size() > 0) {
                SequenceFormat format = (SequenceFormat) ll.get(0);
                sequenceformatid = format.getID();
                paramJobj.put(Constants.sequenceformatDo, sequenceformatid);
            } else if(!StringUtil.isNullOrEmpty(invoiceJson.optString("numberDo",null))) {
                invoiceJson.put("deliveryOrderNo", invoiceJson.optString("numberDo"));
                invoiceJson.put(Constants.sequenceformatDo, "NA");
            }
        }//end of sequenceformat

        // Replacing boolean values in String
        invoiceJson.put("gstIncluded", invoiceJson.optString("includingGST", "false"));
        invoiceJson.put("poRefNumber", invoiceJson.optString("customerporefno"));
        invoiceJson.put("terms", invoiceJson.optString("termid"));
        invoiceJson.put("paymentmethodid", invoiceJson.optString("pmtmethod"));
        invoiceJson.put("currencyName", invoiceJson.optString(Constants.currencyKey));
        invoiceJson.put("dueDate", invoiceJson.optString("duedate"));
        invoiceJson.put(Constants.fromLinkComboAutoDO,Constants.CUSTOMER_INVOICE);//mandatory in case of linking mode

        /*
         * In edit case getting the salesorder transaction id from
         * multitransactionmapping table and editing salesorder
         */
        if (isEdit) {//when copied po and then isEdit flag is coming as true
           invoiceJson.put(Constants.IS_INVOICE_ALLOW_TO_EDIT,"true"); 
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_MODULE, invoiceJson.optString(GroupCompanyProcessMapping.SOURCE_MODULE));
            fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID, invoiceJson.optString(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID));
            fieldrequestParams.put(GroupCompanyProcessMapping.DESTINATION_MODULE, invoiceJson.optString(Constants.moduleid));
            KwlReturnObject result = accGroupCompanyDAO.fetchTransactionMappingDetails(fieldrequestParams);
            List<GroupCompanyTransactionMapping> multiTransObj = result.getEntityList();

            if (multiTransObj.size() > 0) {
                for (GroupCompanyTransactionMapping multTMObj : multiTransObj) {
                    String destinationTransactionId = multTMObj.getDestinationTransactionid();
                    KwlReturnObject soresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), destinationTransactionId);
                    Invoice invoiceObj = (Invoice) soresult.getEntityList().get(0);
                    if (invoiceObj != null) {
                        invoiceJson.put(Constants.billid, destinationTransactionId);
                        if (StringUtil.isNullOrEmpty(invoiceJson.optString("invoiceNumber", null))) {
                            invoiceJson.put("invoiceNumber", invoiceObj.getInvoiceNumber());
                        }
                        if (!invoiceJson.optString(Constants.sequenceformat, "NA").equalsIgnoreCase("NA")) {
                            invoiceJson.put(Constants.sequenceformat, invoiceObj.getSeqformat());
                        }

                        KwlReturnObject doresult = accInvoiceDAOobj.getDOFromOrToInvoices(invoiceObj.getID(), invoiceJson.optString(Constants.companyKey));//destination companyid

                        List<Object[]> listdo = doresult.getEntityList();
                        for (Object[] oj : listdo) {
                            DeliveryOrder deliveryOrder = (DeliveryOrder) oj[0];
                            invoiceJson.put("doid", deliveryOrder.getID());
                            if (!invoiceJson.optString(Constants.sequenceformatDo, "NA").equalsIgnoreCase("NA")) {
                                invoiceJson.put(Constants.sequenceformatDo, deliveryOrder.getSeqformat().getID());
                            }
                            if (StringUtil.isNullOrEmpty(invoiceJson.optString("deliveryOrderNo", null))) {
                                invoiceJson.put("deliveryOrderNo", deliveryOrder.getDeliveryOrderNumber());
                            }
                        }
                    }
                }
            } else {
                throw ServiceException.FAILURE("Some error while creating JSON for Sales Order.", "erp38", false);
            }
        }
        return invoiceJson;
    }

    /*
     * @Description: Convert Purchase Invoice with GRN to Sales Invoice to DO
  * @param: JSONObject paramJObj
  * @Mandatory fields: Destination Company and destination module
     */
    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public JSONObject convertPIwithGRNtoSIwithDO(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject response = new JSONObject();
        boolean issuccess = false;
        String msg = null;
        try {
            if (!paramJobj.has(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN) || !paramJobj.has(GroupCompanyProcessMapping.DESTINATION_MODULE) || !paramJobj.has(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN)||!paramJobj.has(GroupCompanyProcessMapping.SOURCE_MODULE)) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }

            String destinationModuleid = paramJobj.optString(GroupCompanyProcessMapping.DESTINATION_MODULE);//vendor invoice module id
            String destinationCompany = paramJobj.optString(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN);
            String sourceCompany = paramJobj.optString(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN);
            String sourceModuleid = paramJobj.optString(GroupCompanyProcessMapping.SOURCE_MODULE);
            String sourceDocumentId= paramJobj.optString(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID);

            if (destinationModuleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Invoice_ModuleId))) { //vi to si
                JSONObject salesInvoiceReqJson = wsUtilService.populateAdditionalInformation(paramJobj);
                salesInvoiceReqJson = jsonCreateInvoice(salesInvoiceReqJson);
                //isEdit value is changes in jsonCreateInvoice in case of copy VI case
                boolean isEdit = StringUtil.isNullOrEmpty(salesInvoiceReqJson.optString(Constants.isEdit, null)) ? false : Boolean.parseBoolean(salesInvoiceReqJson.getString(Constants.isEdit));
                response = transactionService.saveInvoice(salesInvoiceReqJson);

                if (response.has(Constants.RES_success) && response.optBoolean(Constants.RES_success)) {
                    issuccess = true;
                }

                if (issuccess && !isEdit) {//Not to update multitransactionmapping table in case of Edit mode
                    JSONObject requestJSON = new JSONObject();

                    requestJSON.put(GroupCompanyProcessMapping.SOURCE_MODULE, sourceModuleid);
                    requestJSON.put(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID, sourceDocumentId);
                    requestJSON.put(GroupCompanyProcessMapping.DESTINATION_MODULE, destinationModuleid);
                    requestJSON.put(GroupCompanyProcessMapping.DESTINATION_TRANSACTIONID, response.optString("invid"));
                    accGroupCompanyDAO.saveDocumentTransactionsid(requestJSON);
                }

                if (issuccess && response.has(Constants.RES_MESSAGE)) {
                    msg = response.getString(Constants.RES_MESSAGE);
                } else {
                    throw ServiceException.FAILURE("Some issue occurred while saving the transaction.", "erp33", false);

                }
            }
        } catch (Exception ex) {
            issuccess = false;
        } finally {
            response.put(Constants.RES_MESSAGE, msg);
            response.put(Constants.RES_success, issuccess);

        }
        return response;
    }

    /*
     * @Description: When purchase order is deleted permanently
     * @param: JSONObject paramJobj
     * @Mandatory fields: Destination Company and destination module
     * return: JSONObject
     */

    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public JSONObject deletePurchaseOrderPermanent(JSONObject paramJobj) throws JSONException, ServiceException {
        boolean issuccess = false;
        JSONObject response = new JSONObject();
        StringBuilder msgBuilder = new StringBuilder();

        try {
            if (!paramJobj.has(Constants.data) || paramJobj.getJSONArray(Constants.data).length() < 1) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            boolean isConsignment = false;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isConsignment, null))) {
                isConsignment = Boolean.parseBoolean(paramJobj.optString(Constants.isConsignment));
            }
            boolean isFixedAsset = (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.isFixedAsset, null))) ? Boolean.parseBoolean(paramJobj.optString(Constants.isFixedAsset)) : false;
            String linkedTransaction = accPurchaseOrderModuleServiceObj.deletePurchaseOrdersPermanent(paramJobj);
            issuccess = true;
            if (StringUtil.isNullOrEmpty(linkedTransaction)) {
                if (isFixedAsset) {
                    msgBuilder.append(messageSource.getMessage("acc.field.assetPurchaseOrdersHasBeenDeletedSuccessfully" + linkedTransaction, null, Locale.forLanguageTag(paramJobj.getString("language"))));
                } else {
                    msgBuilder.append(isConsignment ? messageSource.getMessage("acc.venconsignment.order.del", null, Locale.forLanguageTag(paramJobj.getString("language"))) : messageSource.getMessage("acc.po.del" + linkedTransaction, null, Locale.forLanguageTag(paramJobj.getString("language"))));   //"Purchase Order(s) has been deleted successfully";
                }
            } else {
                if (isFixedAsset) {
                    msgBuilder.append(messageSource.getMessage("acc.field.assetPurchaseOrderssExcept", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " " + linkedTransaction.substring(0, linkedTransaction.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsucessfully", null, Locale.forLanguageTag(paramJobj.getString("language"))));
                } else {
                    msgBuilder.append(isConsignment ? (messageSource.getMessage("acc.venfield.consignmentOexcept", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " " + linkedTransaction.substring(0, linkedTransaction.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsucessfully", null, Locale.forLanguageTag(paramJobj.getString("language")))) : (messageSource.getMessage("acc.field.Prchaseorderssexcept", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " " + linkedTransaction.substring(0, linkedTransaction.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsucessfully", null, Locale.forLanguageTag(paramJobj.getString("language")))));   //"Purchase Order(s) has been deleted successfully";
                }
            }
        } catch (Exception ex) {
            issuccess = false;
        } finally {
            response.put(Constants.RES_MESSAGE, msgBuilder.toString());
            response.put(Constants.RES_success, issuccess);
        }
        return response;
    }

    /*
     * @Description:Delete Sales Orders
     */
    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public JSONObject deleteSalesOrders(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject response = new JSONObject();
        if (paramJobj.has(Constants.deletepermanentflag) && paramJobj.optBoolean(Constants.deletepermanentflag)) {
            response = deleteSalesOrdersPermanent(paramJobj);
        } else {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            response = accSalesOrderServiceobj.deleteSalesOrdersTemporary(paramJobj);
        }
        return response;
    }
    
    /*
     * @Description: When purchase order is deleted then salesorder will be deleted. Handled only for permanent delete.
     * @param: JSONObject paramJobj
     * @Mandatory fields: Destination Company and destination module
     * return: JSONObject
     */
    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public JSONObject deleteSalesOrdersPermanent(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject response = transactionService.deleteSalesOrder(paramJobj);
        HashMap<String, Object> fieldrequestParams = new HashMap();
        if (response.has(Constants.RES_success) && response.optBoolean(Constants.RES_success, true)) {
            fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID, paramJobj.optString(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID));
            fieldrequestParams.put(GroupCompanyProcessMapping.DESTINATION_TRANSACTIONID, paramJobj.optString(GroupCompanyProcessMapping.DESTINATION_TRANSACTIONID));
            accGroupCompanyDAO.deleteTransactionMappingRecord(fieldrequestParams);
        }
        return response;
    }

    /*
     * @Description: When purchase return is deleted then salesreturn will be deleted. Handled only for permanent delete. 
     * @param: JSONObject paramJobj
     * @Mandatory fields: Destination Company and destination module
     * return: JSONObject
     */
    
    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public JSONObject deleteSalesReturn(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject response=new JSONObject();
        if (paramJobj.has(Constants.deletepermanentflag) && paramJobj.optBoolean(Constants.deletepermanentflag)) {
            response = transactionService.deleteSalesReturn(paramJobj);
            HashMap<String, Object> fieldrequestParams = new HashMap();
            if (response.has(Constants.RES_success) && response.optBoolean(Constants.RES_success, true)) {
                fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID, paramJobj.optString(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID));
                fieldrequestParams.put(GroupCompanyProcessMapping.DESTINATION_TRANSACTIONID, paramJobj.optString(GroupCompanyProcessMapping.DESTINATION_TRANSACTIONID));
                accGroupCompanyDAO.deleteTransactionMappingRecord(fieldrequestParams);
            }
        } else {
            response=transactionService.deleteSalesReturnTemporary(paramJobj);
        }
        return response;
    }

    /*
     * @Description: When purchase return is deleted then salesreturn will be
     * deleted. Handled only for permanent delete. @param: JSONObject paramJobj
     * @Mandatory fields: Destination Company and destination module return:
     * JSONObject
     */
    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public JSONObject deletePurchaseReturnPermanent(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject response = accGoodsReceiptServiceDAO.deletePurchaseReturnPermanentJSON(paramJobj);
//        JSONObject response = new JSONObject();
        return response;
    }

    /*
     * @Description: When purchase return is deleted. Handled only for permanent delete.
     * @param: JSONObject paramJobj
     * @Mandatory fields: Destination Company and destination module 
     * return: JSONObject
     */
    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public JSONObject deleteMakePaymentPermanent(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
//        JSONObject response = accVendorPaymentModuleServiceObj.deletePaymentForEditJSON(paramJobj);
        JSONObject response = new JSONObject();
        return response;
    }

    /*
     * @Description: When purchase invoice is deleted then sales invoice will be deleted. Handled only for permanent delete. 
     * @param: JSONObject paramJobj
     * @Mandatory fields: Destination Company and destination module 
     * return: JSONObject
     */
    
    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public JSONObject deleteVendorInvoiceandGRN(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, AccountingException {
        JSONObject response = new JSONObject();
        if (paramJobj.optBoolean(Constants.isMultiGroupCompanyFlag)) {
            JSONObject newresponse = new JSONObject(paramJobj.optString("responsekey", "{}"));
            if (newresponse.length() > 0) {
                paramJobj.remove("responsekey");
            }
            String invid = newresponse.optString("invoiceid");
            String billno = newresponse.optString("billno");
            String companyid = paramJobj.optString(GroupCompanyProcessMapping.SOURCE_COMPANYID);
            String gridid = newresponse.optString("groid", null);
            String grno = newresponse.optString("grono", null);

            if (!StringUtil.isNullOrEmpty(gridid)) {
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceiptOrder.class.getName(), gridid);
                GoodsReceiptOrder grObj = (GoodsReceiptOrder) objItr.getEntityList().get(0);
                if (grObj != null) {
                    JSONObject grObjson = paramJobj;
                    grObjson = createDeleteJSON(grObjson, gridid, companyid, grObj.getGoodsReceiptOrderNumber());
                    grObjson = accGoodsReceiptModuleService.deleteGoodsReceiptOrdersPermanentJSON(grObjson);
                    response.put(Constants.RES_success,true);
                }
            }

            if (!StringUtil.isNullOrEmpty(invid)) {
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), invid);
                GoodsReceipt grObj = (GoodsReceipt) objItr.getEntityList().get(0);
                if (grObj != null) {
                    JSONObject invoiceDeleteJson = paramJobj;
                    invoiceDeleteJson = createDeleteJSON(invoiceDeleteJson, invid, companyid, grObj.getGoodsReceiptNumber());
                    invoiceDeleteJson = accGoodsReceiptServiceDAO.deleteGoodsReceiptPermanentJSON(invoiceDeleteJson);
                    response.put(Constants.RES_success,true);
                }
            }
        }
        return response;
    }

    /*
     * @Description: When purchase invoice is deleted then sales invoice will be deleted. Handled only for permanent delete. 
     * @param: JSONObject paramJobj
     * @Mandatory fields: Destination Company and destination module 
     * return: JSONObject
     */
    
    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public JSONObject deleteInvoiceandDO(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException,AccountingException {
        JSONObject response = new JSONObject();
        Map<String, Object> fieldrequestParams = new HashMap<String, Object>();
        String destinationTransactionId = paramJobj.optString(GroupCompanyProcessMapping.DESTINATION_TRANSACTIONID);
        String destinationCompanyid = paramJobj.optString(Constants.companyKey);
        KwlReturnObject soresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), destinationTransactionId);
        Invoice invoiceObj = (Invoice) soresult.getEntityList().get(0);

        if (invoiceObj != null) {
            KwlReturnObject doresult = accInvoiceDAOobj.getDOFromOrToInvoices(invoiceObj.getID(), destinationCompanyid);//destination companyid
            List<Object[]> listdo = doresult.getEntityList();
            for (Object[] oj : listdo) {
                DeliveryOrder deliveryOrder = (DeliveryOrder) oj[0];
                JSONObject deliveryOrderJson = paramJobj;
                deliveryOrderJson = createDeleteJSON(deliveryOrderJson, deliveryOrder.getID(), destinationCompanyid,deliveryOrder.getDeliveryOrderNumber());
                List list = new ArrayList();
                list = accInvoiceModuleService.deleteDeliveryOrdersPermanent(deliveryOrderJson);

            }
            response = transactionService.deleteInvoice(paramJobj);
            if (response.has(Constants.RES_success) && response.optBoolean(Constants.RES_success, true)) {
                fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID, paramJobj.optString(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID));
                fieldrequestParams.put(GroupCompanyProcessMapping.DESTINATION_TRANSACTIONID, paramJobj.optString(GroupCompanyProcessMapping.DESTINATION_TRANSACTIONID));
                accGroupCompanyDAO.deleteTransactionMappingRecord(fieldrequestParams);
            }
        }
        return response;
    }

    private JSONObject createDeleteJSON(JSONObject paramJObj, String billid, String companyid, String billno) throws ServiceException, com.krawler.utils.json.base.JSONException {
        JSONObject deleteSOJson = paramJObj;
        JSONArray deletejArray = new JSONArray();
        JSONObject deleteJson = new JSONObject();
        deleteJson.put(Constants.billid, billid);
        if (!StringUtil.isNullOrEmpty(billno)) {
            deleteJson.put("billno", billno);
        }
        deletejArray.put(deleteJson);
        deleteSOJson.put(Constants.data, deletejArray.toString());
        deleteSOJson.put(Constants.companyKey, companyid);
        deleteSOJson.put(Constants.isdefaultHeaderMap, true);
        deleteSOJson.put(Constants.isMultiGroupCompanyFlag, true);
        return deleteSOJson;
    }

     /*
     * @Description: When Make Payment  is deleted then receivepayment will be deleted. 
     * @param: JSONObject paramJobj
     * @Mandatory fields: Destination Company and destination module 
     * return: JSONObject
     */
    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public JSONObject deleteReceivePayment(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject response = new JSONObject();
        if (paramJobj.has(Constants.deletepermanentflag) && paramJobj.optBoolean(Constants.deletepermanentflag)) {
            try {
                response = accReceivePaymentModuleServiceObj.deleteReceiptForEdit(paramJobj);
            } catch (AccountingException ex) {
                throw ServiceException.FAILURE(ex.getMessage(), "", false);
            }
            HashMap<String, Object> fieldrequestParams = new HashMap();
            if (response.has(Constants.RES_success) && response.optBoolean(Constants.RES_success, true)) {
                fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID, paramJobj.optString(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID));
                fieldrequestParams.put(GroupCompanyProcessMapping.DESTINATION_TRANSACTIONID, paramJobj.optString(GroupCompanyProcessMapping.DESTINATION_TRANSACTIONID));
                accGroupCompanyDAO.deleteTransactionMappingRecord(fieldrequestParams);
            }
        } else {
            response = accReceivePaymentModuleServiceObj.deleteReceiptMerged(paramJobj);
        }
        return response;
    }  
    
        private JSONObject jsonCreateDeliveryOrder(JSONObject paramJobj) throws JSONException, ServiceException {
        JSONObject doJson = paramJobj;
        String grnid = doJson.optString(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID);
        boolean isEdit = StringUtil.isNullOrEmpty(doJson.optString(Constants.isEdit, null)) ? false : Boolean.parseBoolean(doJson.getString(Constants.isEdit));
        boolean isCopy = StringUtil.isNullOrEmpty(doJson.optString("copyInv", null)) ? false : Boolean.parseBoolean(doJson.getString("copyInv"));
        doJson.remove(Constants.billid);
        if (isCopy) {
            isEdit = false;
            doJson.put(Constants.isEdit, "false");
        }
        if (doJson.optString(Constants.sequenceformat, "NA").equalsIgnoreCase("NA")) {
            doJson.put("deliveryOrderNo", doJson.optString("number"));
        } else {
            doJson.remove("deliveryOrderNo");
            doJson.remove(Constants.sequenceformat);
        }
         //if sequenceformat is not present then calculate sequenceformat
        if (!doJson.has(Constants.sequenceformat) || StringUtil.isNullOrEmpty(doJson.optString(Constants.sequenceformat, null))) {
            String sequenceformatid = null;
            Map<String, Object> sfrequestParams = new HashMap<String, Object>();
            sfrequestParams.put(Constants.companyKey, doJson.get(Constants.companyKey));
            sfrequestParams.put("modulename", "autodo");
            sfrequestParams.put("isdefaultFormat", true);
            KwlReturnObject seqFormatResult = accCompanyPreferencesObj.getSequenceFormat(sfrequestParams);
            List<SequenceFormat> ll = seqFormatResult.getEntityList();
            if (ll.size() > 0) {
                SequenceFormat format = (SequenceFormat) ll.get(0);
                sequenceformatid = format.getID();
                doJson.put(Constants.sequenceformat, sequenceformatid);
            } else if (!StringUtil.isNullOrEmpty(grnid)) {// if default sequenceformat is given for GRN but not DO
                KwlReturnObject invObj = accountingHandlerDAOobj.getObject(GoodsReceiptOrder.class.getName(), grnid);
                GoodsReceiptOrder grn = (GoodsReceiptOrder) invObj.getEntityList().get(0);
                if (grn != null) {
                    doJson.put(Constants.sequenceformat, "NA");
                    doJson.put("deliveryOrderNo", grn.getGoodsReceiptOrderNumber());
                }
            }
        }//end of sequenceformat
        doJson.put("gstIncluded", doJson.opt("includingGST"));
        doJson.put("terms", doJson.optString("term"));
        doJson.put(Constants.currencyName, doJson.optString(Constants.currencyKey));

        /*
         * In edit case getting the salesorder transaction id from
         * multitransactionmapping table and editing salesorder
         */
        if (isEdit) {//when copied do and then isEdit flag is coming as true
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_MODULE, doJson.optString(GroupCompanyProcessMapping.SOURCE_MODULE));
            fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID, doJson.optString(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID));
            fieldrequestParams.put(GroupCompanyProcessMapping.DESTINATION_MODULE, doJson.optString(Constants.moduleid));
            KwlReturnObject result = accGroupCompanyDAO.fetchTransactionMappingDetails(fieldrequestParams);
            List<GroupCompanyTransactionMapping> multiTransObj = result.getEntityList();

            if (multiTransObj.size() > 0) {
                for (GroupCompanyTransactionMapping multTMObj : multiTransObj) {
                    String destinationTransactionId = multTMObj.getDestinationTransactionid();
                    KwlReturnObject soresult = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), destinationTransactionId);
                    DeliveryOrder doObj = (DeliveryOrder) soresult.getEntityList().get(0);
                    if (doObj != null) {
                        doJson.put("doid", destinationTransactionId);

                        //if sequenceformat is other than NA then take the already saved do sequenceformat
                        if (!doJson.optString(Constants.sequenceformat, "NA").equalsIgnoreCase("NA")) {
                            doJson.put(Constants.sequenceformat, doObj.getSeqformat() != null ? doObj.getSeqformat() : "NA");
                            doJson.put("deliveryOrderNo", doObj.getDeliveryOrderNumber());
                        }
                    }
                }
            } else {
                throw ServiceException.FAILURE("Delivery Order is not generated for this Goods Receipt Order.", "", false);
            }
        }
        return doJson;
    }

    /*
     * @Description: Convert GRN to DO @param: JSONObject paramJObj @Mandatory
     * fields: Destination Company and destination module
     */
    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public JSONObject convertGRNtoDO(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject response = new JSONObject();
        boolean issuccess = false;
        String msg = null;
        try {
            if (!paramJobj.has(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN) || !paramJobj.has(GroupCompanyProcessMapping.DESTINATION_MODULE) || !paramJobj.has(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN) || !paramJobj.has(GroupCompanyProcessMapping.SOURCE_MODULE)) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }

            String destinationModuleid = paramJobj.optString(GroupCompanyProcessMapping.DESTINATION_MODULE);//vendor invoice module id
            String sourceModuleid = paramJobj.optString(GroupCompanyProcessMapping.SOURCE_MODULE);
            String sourceDocumentId = paramJobj.optString(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID);

            if (destinationModuleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Delivery_Order_ModuleId))) { //grn to do
                JSONObject grnReqJson = wsUtilService.populateAdditionalInformation(paramJobj);
                grnReqJson = jsonCreateDeliveryOrder(grnReqJson);
                //isEdit value changes in jsonCreateDeliveryOrder in case of copy GRN case
                boolean isEdit = StringUtil.isNullOrEmpty(grnReqJson.optString(Constants.isEdit, null)) ? false : Boolean.parseBoolean(grnReqJson.getString(Constants.isEdit));
                grnReqJson = wsUtilService.populateMastersInformation(grnReqJson);
                response = accInvoiceModuleService.saveDeliveryOrderJSON(paramJobj);

                if (response.has(Constants.RES_success) && response.optBoolean(Constants.RES_success)) {
                    issuccess = true;
                    if (response.has(Constants.RES_msg)) {
                        msg = response.getString(Constants.RES_msg);
                    }
                    if (!isEdit) {//Not to update multitransactionmapping table in case of Edit mode
                        JSONObject requestJSON = new JSONObject();
                        requestJSON.put(GroupCompanyProcessMapping.SOURCE_MODULE, sourceModuleid);
                        requestJSON.put(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID, sourceDocumentId);
                        requestJSON.put(GroupCompanyProcessMapping.DESTINATION_MODULE, destinationModuleid);
                        requestJSON.put(GroupCompanyProcessMapping.DESTINATION_TRANSACTIONID, response.optString(Constants.billid));
                        accGroupCompanyDAO.saveDocumentTransactionsid(requestJSON);
                    }
                } else {
                    throw ServiceException.FAILURE("Some issue occurred while saving the transaction.", "erp33", false);
                }
            }
        } catch (Exception ex) {
            issuccess = false;
            Logger.getLogger(WSUtilServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            response.put(Constants.RES_MESSAGE, msg);
            response.put(Constants.RES_success, issuccess);

        }
        return response;
    }

    /*
     * @Description: When grorder is deleted then deliveryorder will be deleted.
     * @param: JSONObject paramJobj
     * @Mandatory fields: Destination Company and destination module 
     * return: JSONObject
     */
    @Override
    @Transactional(isolation = Isolation.READ_UNCOMMITTED)
    public JSONObject deleteDeliveryOrder(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException {
        JSONObject response = new JSONObject();
        response = transactionService.deleteDeliveryOrdersJSON(paramJobj);
        HashMap<String, Object> fieldrequestParams = new HashMap();
        if (paramJobj.has(Constants.deletepermanentflag) && paramJobj.optBoolean(Constants.deletepermanentflag)) {
            if (response.has(Constants.RES_success) && response.optBoolean(Constants.RES_success, true)) {
                fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID, paramJobj.optString(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID));
                fieldrequestParams.put(GroupCompanyProcessMapping.DESTINATION_TRANSACTIONID, paramJobj.optString(GroupCompanyProcessMapping.DESTINATION_TRANSACTIONID));
                accGroupCompanyDAO.deleteTransactionMappingRecord(fieldrequestParams);
            }
        }
        return response;
    }
    
    
}
