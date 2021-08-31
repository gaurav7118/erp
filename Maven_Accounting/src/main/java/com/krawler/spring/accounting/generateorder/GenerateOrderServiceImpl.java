/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.generateorder;

import com.krawler.common.admin.BillingShippingAddresses;
import com.krawler.common.admin.Company;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.FieldParams;
import com.krawler.common.admin.NewProductBatch;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.CustomerProductMapping;
import com.krawler.hql.accounting.Invoice;
import com.krawler.hql.accounting.InvoiceDetail;
import com.krawler.hql.accounting.ProductAssembly;
import com.krawler.hql.accounting.PurchaseOrder;
import com.krawler.hql.accounting.PurchaseOrderDetail;
import com.krawler.hql.accounting.SalesOrder;
import com.krawler.hql.accounting.SalesOrderDetail;
import com.krawler.hql.accounting.VendorProductMapping;
import com.krawler.spring.accounting.account.accVendorCustomerProductDAO;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.invoice.AccInvoiceServiceDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.productmodule.service.AccProductModuleService;
import com.krawler.spring.accounting.ws.service.TransactionService;
import com.krawler.spring.accounting.ws.service.WSUtilService;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.mrp.WorkOrder.AccWorkOrderServiceDAO;
import com.krawler.spring.mrp.WorkOrder.WorkOrder;
import com.krawler.spring.mrp.WorkOrder.WorkOrderComponentDetails;
import com.krawler.spring.mrp.WorkOrder.WorkOrderDAOImpl;
import com.krawler.spring.mrp.WorkOrder.WorkOrderLabourMapping;
import com.krawler.spring.mrp.WorkOrder.WorkOrderMachineMapping;
import com.krawler.spring.mrp.WorkOrder.WorkOrderWorkCenterMapping;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author krawler
 */
public class GenerateOrderServiceImpl implements GenerateOrderService {
    
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private TransactionService transactionService;
    private WSUtilService wsUtilService;
    private AccInvoiceServiceDAO accInvoiceServiceDAO;
    private accJournalEntryDAO accJournalEntryobj;
    private accVendorCustomerProductDAO accVendorCustomerProductDAOobj;
    private AccProductModuleService accProductModuleService;
    private WorkOrderDAOImpl workOrderDAOobj;
    private AccWorkOrderServiceDAO accWorkOrderServiceDAOObj;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private static final String SEPARATOR = ",";
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private accProductDAO productDAOObj;
    
    public void setProductDAOObj(accProductDAO productDAOObj) {
        this.productDAOObj = productDAOObj;
    }
    
    public void setaccCompanyPreferencesObj(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    public void setAccWorkOrderServiceDAOObj(AccWorkOrderServiceDAO accWorkOrderServiceDAOObj) {
        this.accWorkOrderServiceDAOObj = accWorkOrderServiceDAOObj;
    }

    public void setAccMasterItemsDAOobj(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }
    
    public void setWorkOrderDAOobj(WorkOrderDAOImpl workOrderDAOobj) {
        this.workOrderDAOobj = workOrderDAOobj;
    }
    
    public void setwsUtilService(WSUtilService wSUtilService) {
        this.wsUtilService = wSUtilService;
    }

    public void setaccInvoiceServiceDAO(AccInvoiceServiceDAO accInvoiceServiceDAO) {
        this.accInvoiceServiceDAO = accInvoiceServiceDAO;
    }

    public void settransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }
    
    public void setaccJournalEntryobj(accJournalEntryDAO accJournalEntryobj){
        this.accJournalEntryobj=accJournalEntryobj;
    }

    public void setaccVendorCustomerProductDAOobj(accVendorCustomerProductDAO accVendorCustomerProductDAOobj){
        this.accVendorCustomerProductDAOobj=accVendorCustomerProductDAOobj;
    }
    
    public void setaccProductModuleService(AccProductModuleService accProductModuleService){
        this.accProductModuleService=accProductModuleService;
    }
    
    @Override
    public JSONObject validateLinkDocNumber(JSONObject requestJobj) throws ServiceException, JSONException, AccountingException {
        JSONObject returnJobj = new JSONObject();
        boolean isSuccess = false;
        String msg = "";
        String docTypeValue = requestJobj.optString("docTypeValue");
        String linkDocTypeValue = requestJobj.optString("linkDocTypeValue");
        String linkDocNumberValue = requestJobj.optString("linkDocNumberValue");
        String companyid = requestJobj.optString(Constants.companyKey);
        /**
         *If sequence format is not set while creating GRN/DO
         */
        Map<String, Object> filterParams = new HashMap<String, Object>();
        filterParams.put(Constants.companyKey, companyid);
        filterParams.put("isdefaultFormat", true);
        List seqformat = null;
        if (docTypeValue.equalsIgnoreCase("GR")) {
            filterParams.put("modulename", "autogro");
            KwlReturnObject result1 = accCompanyPreferencesObj.getSequenceFormat(filterParams);
            seqformat = result1.getEntityList();
        } else if (docTypeValue.equalsIgnoreCase("DO")) {
            filterParams.put("modulename", "autodo");
            KwlReturnObject result1 = accCompanyPreferencesObj.getSequenceFormat(filterParams);
            seqformat = result1.getEntityList();
        }
        if ((seqformat ==  null || seqformat.isEmpty()) && !linkDocTypeValue.equalsIgnoreCase("WO")) {
            if (docTypeValue.equalsIgnoreCase("GR")) {
                throw new AccountingException("Default Sequence format is not set for GRN. Please add the default sequence format.");
            } else if (docTypeValue.equalsIgnoreCase("DO")) {
                throw new AccountingException("Default Sequence format is not set for DO. Please add the default sequence format.");
            }
        } else {
        Map<String, String> requestMap = new HashMap();
        requestMap.put("fetchColumn", "ID");
        requestMap.put("companyColumn", "company.companyID");
        requestMap.put(Constants.companyKey, companyid);
        requestMap.put("condtionColumnvalue", linkDocNumberValue);
        KwlReturnObject result;
        if (StringUtil.equal(linkDocTypeValue, "PO")) {
            requestMap.put("tableName", "PurchaseOrder");
            requestMap.put("condtionColumn", "purchaseOrderNumber");
        } else if (StringUtil.equal(linkDocTypeValue, "SO")) {
            requestMap.put("tableName", "SalesOrder");
            requestMap.put("condtionColumn", "salesOrderNumber");
        } else if (StringUtil.equal(linkDocTypeValue, "SI")) {
            requestMap.put("tableName", "Invoice");
            requestMap.put("condtionColumn", "invoiceNumber");
        } else if (StringUtil.equal(linkDocTypeValue, "WO")) {
            requestMap.put("tableName", "WorkOrder");
            requestMap.put("condtionColumn", "workOrderID");
        }
        result = accountingHandlerDAOobj.populateMasterInformation(requestMap);
        String linkDocId = "";
        if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
            linkDocId = (String) result.getEntityList().get(0);
            
            /**
             * This Check states that if link document is of type
             * 'WO'(workorder) and status of workorder is other than 'Planned'.
             */
            boolean isLinkedDocValid = true;
            if (StringUtil.equal(linkDocTypeValue, "WO")) {
                result = accountingHandlerDAOobj.getObject(WorkOrder.class.getName(), linkDocId);
                WorkOrder woObj = (WorkOrder) result.getEntityList().get(0);
                String workOrderStatus = StringUtil.isNullObject(woObj.getWorkOrderStatus()) ? "" : woObj.getWorkOrderStatus().getValue();
                if (!workOrderStatus.equals("Planned")) {
                    msg += "Work Order : " + woObj.getWorkOrderID() + " is already In Process/Closed/Released/Built.";
                    isLinkedDocValid = false;
                    isSuccess = false;
                }
            } else if (StringUtil.equal(linkDocTypeValue, "PO")) {
                /**
                 * If linked document is PO then checks whether the PO is
                 * pending for approval/is expense type/is a template.
                 */
                result = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), linkDocId);
                PurchaseOrder poObj = (PurchaseOrder) result.getEntityList().get(0);
                if (poObj.getApprovestatuslevel() != 11) {
                    msg += "Purcahse Order : " + linkDocNumberValue + " is pending for Approval.";
                    isLinkedDocValid = false;
                    isSuccess = false;
                } else if (poObj.isIsExpenseType()) {
                    msg += "Purcahse Order : " + linkDocNumberValue + " is expense type.";
                    isLinkedDocValid = false;
                    isSuccess = false;
                } else if (poObj.getIstemplate() != 0) {
                    msg += "Purcahse Order : " + linkDocNumberValue + " is a template.";
                    isLinkedDocValid = false;
                    isSuccess = false;
                }

            } else if (StringUtil.equal(linkDocTypeValue, "SO")) {
                /**
                 * If linked document is SO then checks whether the SO is
                 * pending for approval/is draft SP/is a template.
                 */
                result = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), linkDocId);
                SalesOrder soObj = (SalesOrder) result.getEntityList().get(0);
                if (soObj.getApprovestatuslevel() != 11) {
                    msg += "Sales Order : " + linkDocNumberValue + " is pending for Approval.";
                    isLinkedDocValid = false;
                    isSuccess = false;
                } else if (soObj.isIsDraft()) {
                    msg += "Sales Order : " + linkDocNumberValue + " is draft SO.";
                    isLinkedDocValid = false;
                    isSuccess = false;
                } else if (soObj.getIstemplate() != 0) {
                    msg += "Sales Order : " + linkDocNumberValue + " is a template.";
                    isLinkedDocValid = false;
                    isSuccess = false;
                }

            } else if (StringUtil.equal(linkDocTypeValue, "SI")) {
                /**
                 * If linked document is PO then checks whether the PO is
                 * pending for approval/is draft SI/is a template.
                 */
                result = accountingHandlerDAOobj.getObject(Invoice.class.getName(), linkDocId);
                Invoice siObj = (Invoice) result.getEntityList().get(0);
                if (siObj.getApprovestatuslevel() != 11) {
                    msg += "Sales Invoice : " + linkDocNumberValue + " is pending for Approval.";
                    isLinkedDocValid = false;
                    isSuccess = false;
                } else if (siObj.isDraft()) {
                    msg += "Sales Invoice : " + linkDocNumberValue + " is draft SI.";
                    isLinkedDocValid = false;
                    isSuccess = false;
                } else if (siObj.getIstemplate() != 0) {
                    msg += "Sales Invoice : " + linkDocNumberValue + " is a template.";
                    isLinkedDocValid = false;
                    isSuccess = false;
                }

            }

            if (isLinkedDocValid && !StringUtil.isNullOrEmpty(linkDocId)) {
                JSONObject linkedDocJobj = getLinkedDocDetails(linkDocId, linkDocTypeValue);
                JSONArray linkedDocRows = linkedDocJobj.getJSONArray(Constants.detail);
                if (linkedDocRows.length() != 0) {
                    returnJobj.put(Constants.RES_data, linkedDocJobj);
                    isSuccess = true;
                    msg = "Transaction exists! Product details have been added in the form.";
                } else {
                    isSuccess = false;
                    msg = "Transaction exists! But there are no products with non-zero quantity.";
                }
            }
        } else {
            msg = "No transaction could be found in system with entered document number.";
        }
        returnJobj.put(Constants.billid, isSuccess ? linkDocId : null);
        returnJobj.put(Constants.RES_success, isSuccess);
        returnJobj.put(Constants.RES_msg, msg);
        }
        return returnJobj;
    }

    private JSONObject getLinkedDocDetails(String linkDocId, String linkDocTypeValue) throws ServiceException, JSONException {
        JSONObject returnJobj = new JSONObject();
        KwlReturnObject result;
        if (StringUtil.equal(linkDocTypeValue, "PO")) {
            result = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), linkDocId);
            PurchaseOrder linkDoc = (PurchaseOrder) result.getEntityList().get(0);
            returnJobj = createJsonFromPO(linkDoc);
        } else if (StringUtil.equal(linkDocTypeValue, "SO")) {
            result = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), linkDocId);
            SalesOrder linkDoc = (SalesOrder) result.getEntityList().get(0);
            returnJobj = createJsonFromSO(linkDoc);
        } else if (StringUtil.equal(linkDocTypeValue, "SI")) {
            result = accountingHandlerDAOobj.getObject(Invoice.class.getName(), linkDocId);
            Invoice linkDoc = (Invoice) result.getEntityList().get(0);
            returnJobj = createJsonFromSI(linkDoc);
        } else if (StringUtil.equal(linkDocTypeValue, "WO")) {
            result = accountingHandlerDAOobj.getObject(WorkOrder.class.getName(), linkDocId);
            WorkOrder linkDoc = (WorkOrder) result.getEntityList().get(0);
            returnJobj = createJsonFromWO(linkDoc);
        }
        return returnJobj;
    }

    /**
     * createJsonFromWO is used to prepare JsonObject for startworkorder JSP
     * page to display BOM data in table.
     *
     * @param doc
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    private JSONObject createJsonFromWO(WorkOrder doc) throws JSONException, ServiceException {
        KwlReturnObject result = null;
        JSONObject returnJobj = new JSONObject();
        returnJobj.put(Constants.billid, doc.getID());
        Map<String, Object> requestparams = new HashMap<String, Object>();
        requestparams.put("bills", doc.getID());
        requestparams.put(Constants.companyKey, doc.getCompany().getCompanyID());
        requestparams.put("isForCompAvailablity", true); //to Avoid finalproduct 
        result = workOrderDAOobj.getWorkOrderComponentDetails(requestparams);
        JSONArray linesJarr = new JSONArray();
        for (int i = 0; i < result.getEntityList().size(); i++) {
            JSONObject lineJobj = new JSONObject();
            WorkOrderComponentDetails WOCompDetail = null;
            WOCompDetail = (WorkOrderComponentDetails) result.getEntityList().get(i);
            lineJobj.put(Constants.productid, WOCompDetail.getProduct().getID());
            lineJobj.put("productname", WOCompDetail.getProduct().getName());
            lineJobj.put(Constants.quantity, WOCompDetail.getRequiredQuantity());
            lineJobj.put("pid", WOCompDetail.getProduct().getProductid());
            lineJobj.put("producttype", WOCompDetail.getProduct().getProducttype().getName());
            linesJarr.put(lineJobj);
        }
        returnJobj.put(Constants.detail, linesJarr);
        return returnJobj;
    }

    private JSONObject createJsonFromPO(PurchaseOrder doc) throws JSONException {
        JSONObject returnJobj = new JSONObject();
        returnJobj.put(Constants.billid, doc.getID());
        returnJobj.put(Constants.billno, doc.getPurchaseOrderNumber());
        Set<PurchaseOrderDetail> rows = doc.getRows();
        JSONArray linesJarr = new JSONArray();
        for (PurchaseOrderDetail row : rows) {
            /**
             * If PurchaseOrderDetail contains products with quantity zero then
             * such products won't be added in linesJarr
             */
            if (row.getBalanceqty() > 0d) {
                JSONObject lineJobj = new JSONObject();
                lineJobj.put(Constants.productid, row.getProduct().getID());
                lineJobj.put("pid", row.getProduct().getProductid());
                lineJobj.put("productname", row.getProduct().getName());
                lineJobj.put(Constants.quantity, row.getBalanceqty());
                linesJarr.put(lineJobj);
            }
        }
        returnJobj.put(Constants.detail, linesJarr);
        return returnJobj;
    }

    private JSONObject createJsonFromSO(SalesOrder doc) throws JSONException {
        JSONObject returnJobj = new JSONObject();
        returnJobj.put(Constants.billid, doc.getID());
        returnJobj.put(Constants.billno, doc.getSalesOrderNumber());
        Set<SalesOrderDetail> rows = doc.getRows();
        JSONArray linesJarr = new JSONArray();
        for (SalesOrderDetail row : rows) {
            /**
             * If SalesOrderDetail contains products with quantity zero then
             * such products won't be added in linesJarr
             */
            if (row.getBalanceqty() > 0d) {
                JSONObject lineJobj = new JSONObject();
                lineJobj.put(Constants.productid, row.getProduct().getID());
                lineJobj.put("pid", row.getProduct().getProductid());
                lineJobj.put("productname", row.getProduct().getName());
                lineJobj.put(Constants.quantity, row.getBalanceqty());
                linesJarr.put(lineJobj);
            }
        }
        returnJobj.put(Constants.detail, linesJarr);
        return returnJobj;
    }

    private JSONObject createJsonFromSI(Invoice doc) throws JSONException, ServiceException {
        JSONObject returnJobj = new JSONObject();
        returnJobj.put(Constants.billid, doc.getID());
        returnJobj.put(Constants.billno, doc.getInvoiceNumber());
        Set<InvoiceDetail> rows = doc.getRows();
        JSONArray linesJarr = new JSONArray();
        for (InvoiceDetail row : rows) {
            /**
             * If InvoiceDetail contains products with quantity zero then such
             * products won't be added in linesJarr
             */
            if (accInvoiceServiceDAO.getInvoiceQuantityForDO(row) > 0d) {
                JSONObject lineJobj = new JSONObject();
                lineJobj.put(Constants.productid, row.getInventory().getProduct().getID());
                lineJobj.put("pid", row.getInventory().getProduct().getProductid());
                lineJobj.put("productname", row.getInventory().getProduct().getName());
                lineJobj.put(Constants.quantity, accInvoiceServiceDAO.getInvoiceQuantityForDO(row));
                linesJarr.put(lineJobj);
            }
        }
        returnJobj.put(Constants.detail, linesJarr);
        return returnJobj;
    }


    @Override
    public JSONObject validateAndSaveDoc(JSONObject requestJobj) throws ServiceException,JSONException, SessionExpiredException, ParseException, AccountingException {
        JSONObject returnJobj = new JSONObject();
        JSONObject responseJobj = new JSONObject();
        JSONObject linkedDocInfoJobj = validateLinkDocNumber(requestJobj);
        String linkDocId = (String) linkedDocInfoJobj.optString(Constants.billid, null);
        if (!StringUtil.isNullOrEmpty(linkDocId)) {
            String companyid = requestJobj.optString("companyid");
            String docTypeValue = requestJobj.optString("docTypeValue");
            String linkDocTypeValue = requestJobj.optString("linkDocTypeValue");
            String linkDocNumberValue = requestJobj.optString("linkDocNumberValue");
            String scannedBarcodesValue = requestJobj.optString("scannedBarcodesValue");
            String[] scannedBarcodesArr = scannedBarcodesValue.split(", ");
            KwlReturnObject result;
            if (StringUtil.equal(linkDocTypeValue, "PO")) {
                result = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), linkDocId);
                PurchaseOrder linkDoc = (PurchaseOrder) result.getEntityList().get(0);
                responseJobj = createGoodSReceiptLinkedToPO(linkDoc, scannedBarcodesArr, companyid, requestJobj);
            } else if (StringUtil.equal(linkDocTypeValue, "SO")) {
                result = accountingHandlerDAOobj.getObject(SalesOrder.class.getName(), linkDocId);
                SalesOrder linkDoc = (SalesOrder) result.getEntityList().get(0);
                responseJobj = createDeiveryOrderLinkedToSO(linkDoc, scannedBarcodesArr, companyid, requestJobj);
            } else if (StringUtil.equal(linkDocTypeValue, "SI")) {
                result = accountingHandlerDAOobj.getObject(Invoice.class.getName(), linkDocId);
                Invoice linkDoc = (Invoice) result.getEntityList().get(0);
                responseJobj = createDeiveryOrderLinkedToSI(linkDoc, scannedBarcodesArr, companyid, requestJobj);
            } else if (StringUtil.equal(linkDocTypeValue, "WO")) {
                result = accountingHandlerDAOobj.getObject(WorkOrder.class.getName(), linkDocId);
                WorkOrder linkDoc = (WorkOrder) result.getEntityList().get(0);
                responseJobj = blockQuantityAndStartWorkOrder(linkDoc, scannedBarcodesArr, companyid, requestJobj);

            }
        }
        returnJobj.put(Constants.RES_data, responseJobj);
        returnJobj.put(Constants.RES_success, responseJobj.optBoolean(Constants.RES_success, false));
        returnJobj.put(Constants.RES_msg, responseJobj.optString(Constants.RES_MESSAGE));
        return returnJobj;
    }

    private JSONObject createDeiveryOrderLinkedToSO(SalesOrder linkDoc, String[] barcodesArr, String companyid, JSONObject requestJobj) throws ServiceException, JSONException, SessionExpiredException, ParseException, AccountingException {
        JSONObject paramsJobj = createRequestJsonForDONew(linkDoc, barcodesArr, companyid, requestJobj);
        try {
            return transactionService.saveDeliveryOrder(paramsJobj);
        } catch (ServiceException ex) {
            Logger.getLogger(GenerateOrderController.class.getName()).log(Level.SEVERE, null, ex);
            return wsUtilService.getErrorResponse(ex.getCode(), paramsJobj, ex.getMessage());
        }
    }

    private JSONObject createRequestJsonForDONew(SalesOrder linkDoc, String[] barcodesArr, String companyid, JSONObject requestJobj) throws JSONException, ServiceException, AccountingException {
        JSONObject returnJobj = createGlobalFieldsJsonForDO(linkDoc, companyid, requestJobj);
        KwlReturnObject kwlObj = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
        ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) kwlObj.getEntityList().get(0);
        JSONArray linesJarr = new JSONArray();
        String customerid = linkDoc.getCustomer().getID();
        Set<SalesOrderDetail> rowSetFromLinkDoc = linkDoc.getRows();
        JSONObject barcodesFieldsDetailsJobj = processBarcodes(barcodesArr);
        /**
         * This resolves the issue - Wrong message is provided if SO/SI contains
         * products with zero quantity.This creates the rowSet from
         * rowSetFromLinkDoc having quantity more than zero.
         */
        Set<SalesOrderDetail> rowSet = new HashSet<SalesOrderDetail>();
        for (SalesOrderDetail row : rowSetFromLinkDoc) {
            Double balQty = row.getBalanceqty();
            if (balQty != 0d) {
                rowSet.add(row);
            }
        }
        /**
         * When same product is added multiple times in SO then on the basis of
         * product id balance quantity is added.
         */
        Map<String, Double> newBalanceQtyMap = new HashMap<>();
        for (SalesOrderDetail row : rowSet) {
            if (newBalanceQtyMap.containsKey(row.getProduct().getID())) {
                newBalanceQtyMap.put(row.getProduct().getID(), newBalanceQtyMap.get(row.getProduct().getID()) + row.getBalanceqty());
            } else {
                newBalanceQtyMap.put(row.getProduct().getID(), row.getBalanceqty());
            }
        }
        Iterator itr = barcodesFieldsDetailsJobj.keys();
        String exceptionMsg = "";
        String pid = null;
        while (itr.hasNext()) {
            String crossReference = (String) itr.next();
            JSONObject paramsJobj = new JSONObject();
            paramsJobj.put("customerid", customerid);
            paramsJobj.put("jsonstringFilter", crossReference);
            paramsJobj.put("fieldLabel", "Cross Reference");
            paramsJobj.put(Constants.companyKey, companyid);
            JSONObject productsJobj = getProductsMappedToCustomer(paramsJobj);
            JSONArray productsJarr = productsJobj.optJSONArray(Constants.RES_data) != null ? productsJobj.optJSONArray(Constants.RES_data) : new JSONArray();
            if (productsJarr.length() == 1) {
                JSONObject productJobj = productsJarr.optJSONObject(0);
                boolean isProductExistsInLinkedDoc = false;
                for (SalesOrderDetail row : rowSet) {
                    pid=row.getProduct().getProductid();
                    if (StringUtil.equal(row.getProduct().getID(), productJobj.optString("productid"))) {
                        isProductExistsInLinkedDoc = true;
                        Double bqty = newBalanceQtyMap.get(row.getProduct().getID());
                        JSONArray barcodeFieldsDetailsJarr = barcodesFieldsDetailsJobj.optJSONArray(crossReference);
                        /**
                         * Barcodes scanned should be less than or equal to balance quantity in sales order.
                         * Restrict scanning of barcodes if more than ordered packets/cases/combos.
                         */
                        if (!extraCompanyPreferences.isIsAllowQtyMoreThanLinkedDoc() && barcodeFieldsDetailsJarr.length() > bqty) {
                            exceptionMsg += "Scanned quantity of product " + productJobj.optString("pid") + " corresponding to cross reference " + crossReference + " exceeds the quantity available in linked document " + linkDoc.getSalesOrderNumber() + ". " + "\n";
                        } else if (barcodeFieldsDetailsJarr.length() != 0) {
                            try {
                                JSONObject lineJobj = createLineDetailJsonForDONew(productJobj, row, barcodeFieldsDetailsJarr, extraCompanyPreferences);
                                /**
                                 * When same product is added multiple times in
                                 * SO, batches that are not consumed are added
                                 * in newBarcodeFieldsDetailsJarr and then
                                 * barcodesFieldsDetailsJobj is updated for the
                                 * same cross reference.
                                 */
                                double dquantity = lineJobj.optDouble("deliveredquantity");
                                JSONArray newBarcodeFieldsDetailsJarr = new JSONArray();
                                for (int index = barcodeFieldsDetailsJarr.length() - 1; index >= dquantity; index--) {
                                    newBarcodeFieldsDetailsJarr.put(barcodeFieldsDetailsJarr.optJSONObject(index));
                                }
                                barcodesFieldsDetailsJobj.put(crossReference, newBarcodeFieldsDetailsJarr);
                                linesJarr.put(lineJobj);
                            } catch (AccountingException ex) {
                                if (!StringUtil.isNullObject(ex.getMessage())) {
                                    exceptionMsg += ex.getMessage() + "\n";
                                }
                            }
                        }
//                        rowSet.remove(row);
//                        break;
                    }
                }
                if (!isProductExistsInLinkedDoc) {
                    exceptionMsg += "Cross reference " + crossReference + " does not correspond to any of the products from liked doc " + linkDoc.getSalesOrderNumber() + ". " + "\n";
                }
            } else if (productsJarr.length() == 0) {
                exceptionMsg += "Product with cross reference " + crossReference + " could not be found. " + "\n";
            } else {
                exceptionMsg += "More then one products found with cross reference " + crossReference + ". " + "\n";
            }
        }
        if (!StringUtil.isNullOrEmpty(exceptionMsg)) {
            throw new AccountingException(exceptionMsg);
        }
        returnJobj.put("deliveryorderdetail", linesJarr);
        return returnJobj;
    }

    private JSONObject createGlobalFieldsJsonForDO(SalesOrder linkDoc, String companyid, JSONObject requestJobj) throws JSONException, ServiceException {
        JSONObject returnJobj = new JSONObject();
        KwlReturnObject kwlObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
        Company company = (Company) kwlObj.getEntityList().get(0);
        returnJobj.put("cdomain", company.getSubDomain());
        returnJobj.put(Constants.useridKey, requestJobj.optString(Constants.useridKey));
        returnJobj.put(Constants.userid, requestJobj.optString(Constants.userid));
        returnJobj.put("billdate", new SimpleDateFormat(Constants.yyyyMMdd).format(new Date()));
        returnJobj.put("currencyvalue", linkDoc.getCurrency() != null ? linkDoc.getCurrency().getCurrencyCode() : company.getCurrency().getCurrencyCode());
        returnJobj.put("customervalue", linkDoc.getCustomer().getAcccode());
        returnJobj.put("shipdate", linkDoc.getShipdate());
        returnJobj.put("discount", linkDoc.getDiscount());
        returnJobj.put("taxvalue", linkDoc.getTax() != null ? linkDoc.getTax().getTaxCode() : null);
        returnJobj.put("includeprotax", false);
        returnJobj.put("subTotal", linkDoc.getTotalamount());
        returnJobj.put("taxamount", linkDoc.getTax() != null ? 0 : 0);
        returnJobj.put("termvalue", linkDoc.getTerm() != null ? linkDoc.getTerm().getTermname() : null);
        returnJobj.put("isEdit", false);
        returnJobj.put("userdateformat", Constants.yyyyMMdd);
        returnJobj.put("costcentervalue", linkDoc.getCostcenter() != null ? linkDoc.getCostcenter().getName() : null);
        returnJobj.put("salespersonvalue", linkDoc.getSalesperson() != null ? linkDoc.getSalesperson().getCode() : null);
        returnJobj.put("shipvia", linkDoc.getShipvia());
        returnJobj.put("fob", linkDoc.getFob());
        returnJobj.put("memo", linkDoc.getMemo());
        returnJobj.put(Constants.userfullname, requestJobj.optString(Constants.userfullname));
        returnJobj.put(Constants.companyKey, companyid);
        returnJobj.put(Constants.reqHeader, requestJobj.optString(Constants.reqHeader));
        requestJobj.put(Constants.remoteIPAddress, requestJobj.optString(Constants.remoteIPAddress));
        /**
         * AddressDetails
         */
        BillingShippingAddresses jobj=linkDoc.getBillingShippingAddresses();
        returnJobj=createJsonForAddress(jobj,false,returnJobj);
        
        /**
         * Link details
         */
        returnJobj.put("linkNumber", linkDoc.getID());
        returnJobj.put("fromLinkCombo", Constants.SALESORDER);
        
        return returnJobj;
    }

    private JSONObject createLineDetailJsonForDONew(JSONObject productJobj, SalesOrderDetail row, JSONArray barcodeFieldsDetailsJarr, ExtraCompanyPreferences extraCompanyPreferences) throws JSONException, ServiceException, AccountingException {
        JSONObject returnJobj = new JSONObject();
        String warehouseid = productJobj.optString("warehouse");
        String locationid = productJobj.optString("location");
        String companyid = row.getCompany().getCompanyID();

        /**
         * Get Location
         */
        String location = getLocationName(locationid, companyid);

        /**
         * Get Warehouse
         */
        String warehouse = getWarehouseName(warehouseid, companyid);
        String exceptionMsg = "";
        returnJobj.put("productvalue", productJobj.optString("pid"));
        returnJobj.put("rate", row.getRate());
        double quantity = row.getBalanceqty();
        returnJobj.put("quantity", quantity);
        double dquantity = barcodeFieldsDetailsJarr.length() > row.getBalanceqty() ? row.getBalanceqty() : barcodeFieldsDetailsJarr.length();
        returnJobj.put("deliveredquantity", dquantity);
        double totalRowQuantityInBarcodes = getTotalQuantityInBarcodes(barcodeFieldsDetailsJarr, dquantity);
        returnJobj.put("baseuomquantity", totalRowQuantityInBarcodes);
        returnJobj.put("uomvalue", row.getUom().getName());
        returnJobj.put("discountType", row.getDiscountispercent());
        returnJobj.put("discount", row.getDiscount());
        returnJobj.put("producttaxvalue", row.getTax() != null ? row.getTax().getTaxCode() : null);
        returnJobj.put("desc", row.getDescription());
        returnJobj.put("taxamount", row.getRowTaxAmount());
        Map<String, Integer> nameAndCount = new HashMap<>();
        if (productJobj.optBoolean("isBatchForProduct") || productJobj.optBoolean("isSerialForProduct")) {
            JSONArray batchDetailsJarr = new JSONArray();
            for (int i = 0; i < dquantity; i++) {
                /**
                 * When we provide same batch while creating DO then this checks
                 * the quantity for the batch is available in inventory.
                 */
                String currentBatch = barcodeFieldsDetailsJarr.getJSONObject(i).optString("batchNumber");
                double qtyInArray = quantityInArray(barcodeFieldsDetailsJarr, currentBatch);
                double availableQuantityOfBatch = getAvailableQuantityOfBatch(productJobj.optString("productid"), currentBatch, warehouse, location);
//                if (!extraCompanyPreferences.isIsnegativestockforlocwar() && qtyInArray > availableQuantityOfBatch) {
                if (qtyInArray > availableQuantityOfBatch) {
                    Integer count = nameAndCount.get(currentBatch);
                    if (count == null) {
                        nameAndCount.put(currentBatch, 1);
                        exceptionMsg += "Quantity of batch " + currentBatch + " of Product with cross reference " + barcodeFieldsDetailsJarr.getJSONObject(i).getString("crossReference") + " is exceeding the available quantity in stock.\n";
                    }
                } else {
                    batchDetailsJarr = createBatchSerialDetailsJsonForDONew(batchDetailsJarr, barcodeFieldsDetailsJarr.getJSONObject(i), productJobj, extraCompanyPreferences, warehouse, location);
                }
            }
            returnJobj.put("batchdetails", batchDetailsJarr);
        }
        if (!StringUtil.isNullOrEmpty(exceptionMsg)) {
            throw new AccountingException(exceptionMsg);
        }

        /**
         * Link details
         */
        returnJobj.put("billid", row.getSalesOrder().getID());
        returnJobj.put("billno", row.getSalesOrder().getSalesOrderNumber());
        returnJobj.put("rowid", row.getID());

        return returnJobj;
    }

    private JSONArray createBatchSerialDetailsJsonForDONew(JSONArray batchSerialDetailsJarr, JSONObject barcodeFieldsDetailsJobj, JSONObject productJobj, ExtraCompanyPreferences extraCompanyPreferences,String warehouse,String location) throws JSONException, ServiceException, AccountingException {
        JSONObject tempJobj = new JSONObject();
            tempJobj.put("batchquantity", barcodeFieldsDetailsJobj.getInt("weight"));
            tempJobj.put("warehousevalue", warehouse);
            tempJobj.put("locationvalue", location);
            if (productJobj.optBoolean("isBatchForProduct")) {
                tempJobj.put("purchasebatchvalue", barcodeFieldsDetailsJobj.getString("batchNumber"));
                tempJobj.put("mfgdate", getDateFromString(barcodeFieldsDetailsJobj.getString("packDate")));
            }
            if (productJobj.optBoolean("isSerialForProduct")) {
                if (!productJobj.optBoolean("isBatchForProduct")) {
                    tempJobj.put("batchvalue", "");
                }
                tempJobj.put("purchaseserialvalue", barcodeFieldsDetailsJobj.getString("batchNumber"));
            }
            batchSerialDetailsJarr.put(tempJobj);
        return batchSerialDetailsJarr;
    }

    private JSONObject createDeiveryOrderLinkedToSI(Invoice linkDoc, String[] barcodesArr, String companyid, JSONObject requestJobj) throws ServiceException, JSONException, SessionExpiredException, ParseException, AccountingException {
        JSONObject paramsJobj = createRequestJsonForDONew(linkDoc, barcodesArr, companyid, requestJobj);
        return transactionService.saveDeliveryOrder(paramsJobj);
    }

    private JSONObject createRequestJsonForDONew(Invoice linkDoc, String[] barcodesArr, String companyid, JSONObject requestJobj) throws JSONException, ServiceException, AccountingException {
        JSONObject returnJobj = createGlobalFieldsJsonForDO(linkDoc, companyid, requestJobj);
        KwlReturnObject kwlObj = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
        ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) kwlObj.getEntityList().get(0);
        JSONArray linesJarr = new JSONArray();
        String customerid = linkDoc.getCustomer().getID();
        Set<InvoiceDetail> rowSetFromLinkDoc = linkDoc.getRows();
        JSONObject barcodesFieldsDetailsJobj = processBarcodes(barcodesArr);
        /**
         * This resolves the issue - Wrong message is provided if SO/SI contains
         * products with zero quantity.This creates the rowSet from
         * rowSetFromLinkDoc having quantity more than zero.
         */
        Set<InvoiceDetail> rowSet = new HashSet<InvoiceDetail>();
        for (InvoiceDetail row : rowSetFromLinkDoc) {
            Double balQty = accInvoiceServiceDAO.getInvoiceQuantityForDO(row);
            if (balQty != 0d) {
                rowSet.add(row);
            }
        }
        /**
         * When same product is added multiple times in SI then on the basis of
         * product id balance quantity is added.
         */
        Map<String, Double> newBalanceQtyMap = new HashMap<>();
        for (InvoiceDetail row : rowSet) {
            if (newBalanceQtyMap.containsKey(row.getInventory().getProduct().getID())) {
                newBalanceQtyMap.put(row.getInventory().getProduct().getID(), newBalanceQtyMap.get(row.getInventory().getProduct().getID()) + accInvoiceServiceDAO.getInvoiceQuantityForDO(row));
            } else {
                newBalanceQtyMap.put(row.getInventory().getProduct().getID(), accInvoiceServiceDAO.getInvoiceQuantityForDO(row));
            }
        }
        Iterator itr = barcodesFieldsDetailsJobj.keys();
        String exceptionMsg = "";
        String pid = null;
        while (itr.hasNext()) {
            String crossReference = (String) itr.next();
            JSONObject paramsJobj = new JSONObject();
            paramsJobj.put("customerid", customerid);
            paramsJobj.put("jsonstringFilter", crossReference);
            paramsJobj.put("fieldLabel", "Cross Reference");
            paramsJobj.put(Constants.companyKey, companyid);
            JSONObject productsJobj = getProductsMappedToCustomer(paramsJobj);
            JSONArray productsJarr = productsJobj.optJSONArray(Constants.RES_data) != null ? productsJobj.optJSONArray(Constants.RES_data) : new JSONArray();
            if (productsJarr.length() == 1) {
                JSONObject productJobj = productsJarr.optJSONObject(0);
                boolean isProductExistsInLinkedDoc = false;
                for (InvoiceDetail row : rowSet) {
                    pid = row.getInventory().getProduct().getName();
                    if (StringUtil.equal(row.getInventory().getProduct().getID(), productJobj.optString("productid"))) {
                        isProductExistsInLinkedDoc = true;
                        Double bqty = newBalanceQtyMap.get(row.getInventory().getProduct().getID());
                        JSONArray barcodeFieldsDetailsJarr = barcodesFieldsDetailsJobj.optJSONArray(crossReference);
                        /**
                         * Barcodes scanned should be less than or equal to balance quantity in sales invoice.
                         * Restrict scanning of barcodes if more than ordered packets/cases/combos.
                         */
                        if (!extraCompanyPreferences.isIsAllowQtyMoreThanLinkedDoc() && barcodeFieldsDetailsJarr.length() > bqty) {
                            exceptionMsg += "Scanned quantity of product " + productJobj.optString("pid") + " corresponding to cross reference " + crossReference + " exceeds the quantity available in linked document " + linkDoc.getInvoiceNumber() + ". " + "\n";
                        } else if (barcodeFieldsDetailsJarr.length() != 0) {
                            try {
                                JSONObject lineJobj = createLineDetailJsonForDONew(productJobj, row, barcodeFieldsDetailsJarr, extraCompanyPreferences);
                                /**
                                 * When same product is added multiple times in
                                 * SI, batches that are not consumed are added
                                 * in newBarcodeFieldsDetailsJarr and then
                                 * barcodesFieldsDetailsJobj is updated for the
                                 * same cross reference.
                                 */
                                double dquantity = lineJobj.optDouble("deliveredquantity");
                                JSONArray newBarcodeFieldsDetailsJarr = new JSONArray();
                                for (int index = barcodeFieldsDetailsJarr.length()-1; index >= dquantity; index--) {
                                    newBarcodeFieldsDetailsJarr.put(barcodeFieldsDetailsJarr.optJSONObject(index));
                                }
                                barcodesFieldsDetailsJobj.put(crossReference, newBarcodeFieldsDetailsJarr);
                                linesJarr.put(lineJobj);
                            } catch (AccountingException ex) {
                                if (!StringUtil.isNullObject(ex.getMessage())) {
                                    exceptionMsg += ex.getMessage() + "\n";
                                }
                            }
                        }
//                        rowSet.remove(row);
//                        break;
                    }
                }
                if (!isProductExistsInLinkedDoc) {
                    exceptionMsg += "Cross reference " + crossReference + " does not correspond to any of the products from liked doc " + linkDoc.getInvoiceNumber() + ". " + "\n";
                }
            } else if (productsJarr.length() == 0) {
                exceptionMsg += "Product with cross reference " + crossReference + " could not be found. " + "\n";
            } else {
                exceptionMsg += "More then one products found with cross reference " + crossReference + ". " + "\n";
            }
        }
        if (!StringUtil.isNullOrEmpty(exceptionMsg)) {
            throw new AccountingException(exceptionMsg);
        }
        returnJobj.put("deliveryorderdetail", linesJarr);
        return returnJobj;
    }

    private JSONObject createGlobalFieldsJsonForDO(Invoice linkDoc, String companyid, JSONObject requestJobj) throws JSONException, ServiceException {
        JSONObject returnJobj = new JSONObject();
        KwlReturnObject kwlObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
        Company company = (Company) kwlObj.getEntityList().get(0);
        returnJobj.put("cdomain", company.getSubDomain());
        returnJobj.put(Constants.useridKey, requestJobj.optString(Constants.useridKey));
        returnJobj.put(Constants.userid, requestJobj.optString(Constants.userid));
        returnJobj.put("billdate", new SimpleDateFormat(Constants.yyyyMMdd).format(new Date()));
        returnJobj.put("currencyvalue", linkDoc.getCurrency() != null ? linkDoc.getCurrency().getCurrencyCode() : company.getCurrency().getCurrencyCode());
        returnJobj.put("customervalue", linkDoc.getCustomer().getAcccode());
        returnJobj.put("shipdate", linkDoc.getShipDate());
        returnJobj.put("discount", linkDoc.getDiscount());
        returnJobj.put("taxvalue", linkDoc.getTax() != null ? linkDoc.getTax().getTaxCode() : null);
        returnJobj.put("includeprotax", false);
        returnJobj.put("subTotal", linkDoc.getExcludingGstAmount());
        returnJobj.put("taxamount", linkDoc.getTax() != null ? 0 : 0);
        returnJobj.put("termvalue", linkDoc.getTermid() != null ? linkDoc.getTermid().getTermname() : null);
        returnJobj.put("isEdit", false);
        returnJobj.put("userdateformat", Constants.yyyyMMdd);
        returnJobj.put("salespersonvalue", linkDoc.getMasterSalesPerson() != null ? linkDoc.getMasterSalesPerson().getCode() : null);
        returnJobj.put("shipvia", linkDoc.getShipvia());
        returnJobj.put("fob", linkDoc.getFob());
        returnJobj.put("memo", linkDoc.getMemo());
        returnJobj.put(Constants.userfullname, requestJobj.optString(Constants.userfullname));
        returnJobj.put(Constants.companyKey, companyid);
        returnJobj.put(Constants.reqHeader, requestJobj.optString(Constants.reqHeader));
        requestJobj.put(Constants.remoteIPAddress, requestJobj.optString(Constants.remoteIPAddress));
        /**
         * Address Details
         */
        BillingShippingAddresses jobj=linkDoc.getBillingShippingAddresses();
        returnJobj=createJsonForAddress(jobj,false,returnJobj);
        
        /**
         * Link details
         */
        returnJobj.put("linkNumber", linkDoc.getID());
        returnJobj.put("fromLinkCombo", Constants.CUSTOMER_INVOICE);

        return returnJobj;
    }

    private JSONObject createLineDetailJsonForDONew(JSONObject productJobj, InvoiceDetail row, JSONArray barcodeFieldsDetailsJarr, ExtraCompanyPreferences extraCompanyPreferences) throws JSONException, ServiceException, AccountingException {
        JSONObject returnJobj = new JSONObject();
        String warehouseid = productJobj.optString("warehouse");
        String locationid = productJobj.optString("location");
        String companyid = row.getCompany().getCompanyID();

        /**
         * Get Location
         */
        String location = getLocationName(locationid, companyid);

        /**
         * Get Warehouse
         */
        String warehouse = getWarehouseName(warehouseid, companyid);
        String exceptionMsg = "";
        returnJobj.put("productvalue", productJobj.optString("pid"));
        returnJobj.put("rate", row.getRate());
        double quantity = accInvoiceServiceDAO.getInvoiceQuantityForDO(row);
        returnJobj.put("quantity", quantity);
        double dquantity = barcodeFieldsDetailsJarr.length() > accInvoiceServiceDAO.getInvoiceQuantityForDO(row) ? accInvoiceServiceDAO.getInvoiceQuantityForDO(row) : barcodeFieldsDetailsJarr.length();
        returnJobj.put("deliveredquantity", dquantity);
        double totalRowQuantityInBarcodes = getTotalQuantityInBarcodes(barcodeFieldsDetailsJarr, dquantity);
        returnJobj.put("baseuomquantity", totalRowQuantityInBarcodes);
        returnJobj.put("uomvalue", row.getInventory().getUom().getName());
        returnJobj.put("discountType", (row.getDiscount() != null ? row.getDiscount().isInPercent() : null));
        returnJobj.put("discount", (row.getDiscount() != null ? row.getDiscount().getDiscount() : null));
        returnJobj.put("producttaxvalue", row.getTax() != null ? row.getTax().getTaxCode() : null);
        returnJobj.put("desc", row.getDescription());
        returnJobj.put("taxamount", row.getRowTaxAmount());
        Map<String, Integer> nameAndCount = new HashMap<>();
        if (productJobj.optBoolean("isBatchForProduct") || productJobj.optBoolean("isSerialForProduct")) {
            JSONArray batchDetailsJarr = new JSONArray();
            for (int i = 0; i < dquantity; i++) {
                /**
                 * When we provide same batch while creating DO then this checks
                 * for quantity for the batch is available in inventory.
                 */
                String currentBatch = barcodeFieldsDetailsJarr.getJSONObject(i).optString("batchNumber");
                double qtyInArray = quantityInArray(barcodeFieldsDetailsJarr, currentBatch);
                double availableQuantityOfBatch = getAvailableQuantityOfBatch(productJobj.optString("productid"), currentBatch, warehouse, location);
//                if (!extraCompanyPreferences.isIsnegativestockforlocwar() && qtyInArray > availableQuantityOfBatch) {
                if (qtyInArray > availableQuantityOfBatch) {
                    Integer count = nameAndCount.get(currentBatch);
                    if (count == null) {
                        nameAndCount.put(currentBatch, 1);
                        exceptionMsg+="Quantity of batch " + currentBatch +" of Product with cross reference " + barcodeFieldsDetailsJarr.getJSONObject(i).getString("crossReference") +" is exceeding the available quantity in stock.\n";
                    }
                }
                else {
                    batchDetailsJarr = createBatchSerialDetailsJsonForDONew(batchDetailsJarr, barcodeFieldsDetailsJarr.getJSONObject(i), productJobj, extraCompanyPreferences, warehouse, location);
            }
            }
            returnJobj.put("batchdetails", batchDetailsJarr);
        }
        if (!StringUtil.isNullOrEmpty(exceptionMsg)) {
            throw new AccountingException(exceptionMsg);
        }

        /**
         * Link details
         */
        returnJobj.put("billid", row.getInvoice().getID());
        returnJobj.put("billno", row.getInvoice().getInvoiceNumber());
        returnJobj.put("rowid", row.getID());

        return returnJobj;
    }

    private JSONObject createGoodSReceiptLinkedToPO(PurchaseOrder linkDoc, String[] barcodesArr, String companyid, JSONObject requestJobj) throws ServiceException, SessionExpiredException, JSONException, ParseException, AccountingException {
        JSONObject paramsJobj = createRequestJsonForGRNew(linkDoc, barcodesArr, companyid, requestJobj);
        return transactionService.saveGoodsReceipt(paramsJobj);
    }

    private JSONObject createRequestJsonForGRNew(PurchaseOrder linkDoc, String[] barcodesArr, String companyid, JSONObject requestJobj) throws JSONException, ServiceException, AccountingException {
        JSONObject returnJobj = createGlobalFieldsJsonForGR(linkDoc, companyid, requestJobj);
        String bsaid=linkDoc.getBillingShippingAddresses().getID();
        KwlReturnObject kwlObj = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
        ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) kwlObj.getEntityList().get(0);
        /**
         * In System Controls, if restrict duplicate batch check is enable then
         * it restrict batch number if it is available in inventory.
         */
        String columnPref = extraCompanyPreferences.getColumnPref();
        JSONObject columnPrefJSON = new JSONObject(columnPref);
        boolean restrictDuplicate = columnPrefJSON.has("restrictDuplicateBatch") ? columnPrefJSON.optBoolean("restrictDuplicateBatch") : false;
        JSONObject barcodesFieldsDetailsJobj = processBarcodes(barcodesArr);
        if (restrictDuplicate) {
            checkDuplicateBatchInInventory(barcodesArr, companyid);
            /**
             * If scanned barcodes contains duplicate batches for same cross
             * reference.
             */
            Iterator itr1 = barcodesFieldsDetailsJobj.keys();
            String exceptionMsg1 = "";
            String repeatedBatchNo = "";
            while (itr1.hasNext()) {
                String crossReference = (String) itr1.next();
                repeatedBatchNo = duplicatesInArray(barcodesFieldsDetailsJobj.optJSONArray(crossReference));
                if (!StringUtil.isNullOrEmpty(repeatedBatchNo)) {
                    exceptionMsg1 += repeatedBatchNo + " for CR " + crossReference + SEPARATOR;
                }
            }
            if (!StringUtil.isNullOrEmpty(exceptionMsg1)) {
                exceptionMsg1 = exceptionMsg1.substring(0, exceptionMsg1.length() - SEPARATOR.length());
                exceptionMsg1 = "Scanned Barcodes contain duplicate Batch " + exceptionMsg1 + ".\n";
                throw new AccountingException(exceptionMsg1);
            }
        }
        JSONArray linesJarr = new JSONArray();
        String vendorid = linkDoc.getVendor().getID();
        Set<PurchaseOrderDetail> rowSetFromLinkDoc = linkDoc.getRows();
        
        /**
         * This resolves the issue - Wrong message is provided if PO contains
         * products with zero quantity.This creates the rowSet from
         * rowSetFromLinkDoc having quantity more than zero.
         */
        Set<PurchaseOrderDetail> rowSet = new HashSet<PurchaseOrderDetail>();
        for (PurchaseOrderDetail row : rowSetFromLinkDoc) {
            double balQty = row.getBalanceqty();
            if (balQty != 0d) {
                rowSet.add(row);
            }
        }
        /**
         * When same product is added multiple times in PO then on the basis of
         * product id balance quantity is added.
         */
        Map<String, Double> newBalanceQtyMap = new HashMap<>();
        for (PurchaseOrderDetail row : rowSet) {
            if (newBalanceQtyMap.containsKey(row.getProduct().getID())) {
                newBalanceQtyMap.put(row.getProduct().getID(), newBalanceQtyMap.get(row.getProduct().getID()) + row.getBalanceqty());
            } else {
                newBalanceQtyMap.put(row.getProduct().getID(), row.getBalanceqty());
            }
        }
        Iterator itr = barcodesFieldsDetailsJobj.keys();
        String exceptionMsg = "";
        String pid = null;
        while (itr.hasNext()) {
            String crossReference = (String) itr.next();
            JSONObject paramsJobj = new JSONObject();
            paramsJobj.put("vendorid", vendorid);
            paramsJobj.put("jsonstringFilter", crossReference);
            paramsJobj.put("fieldLabel", "Cross Reference");
            paramsJobj.put(Constants.companyKey, companyid);
            JSONObject productsJobj = getProductsMappedToVendor(paramsJobj);
            JSONArray productsJarr = productsJobj.optJSONArray(Constants.RES_data) != null ? productsJobj.optJSONArray(Constants.RES_data) : new JSONArray();
            if (productsJarr.length() == 1) {
                JSONObject productJobj = productsJarr.optJSONObject(0);
                boolean isProductExistsInLinkedDoc = false;
                for (PurchaseOrderDetail row : rowSet) {
                    pid = row.getProduct().getName();
                    if (StringUtil.equal(row.getProduct().getID(), productJobj.optString("productid"))) {
                        isProductExistsInLinkedDoc = true;
                        Double bqty = newBalanceQtyMap.get(row.getProduct().getID());
                        JSONArray barcodeFieldsDetailsJarr = barcodesFieldsDetailsJobj.optJSONArray(crossReference);
                        /**
                         * Barcodes scanned should be less than or equal to balance quantity in purchase order.
                         * Restrict scanning of barcodes if more than ordered packets/cases/combos.
                         */
                        if (!extraCompanyPreferences.isIsAllowQtyMoreThanLinkedDoc() && barcodeFieldsDetailsJarr.length() > bqty) {
                            exceptionMsg += "Scanned quantity of product " + productJobj.optString("pid") + " corresponding to cross reference " + crossReference + " exceeds the quantity available in linked document " + linkDoc.getPurchaseOrderNumber() + ". " + "\n";
                        } else if (barcodeFieldsDetailsJarr.length() != 0) {
                            JSONObject lineJobj = createLineDetailJsonForGRNew(productJobj, row, barcodeFieldsDetailsJarr);
                            /**
                             * When same product is added multiple times in PO,
                             * batches that are not consumed are added in
                             * newBarcodeFieldsDetailsJarr and then
                             * barcodesFieldsDetailsJobj is updated for the same
                             * cross reference.
                             */
                            double dquantity = lineJobj.optDouble("receivedquantity");
                            JSONArray newBarcodeFieldsDetailsJarr = new JSONArray();
                            for (int index = barcodeFieldsDetailsJarr.length() - 1; index >= dquantity; index--) {
                                newBarcodeFieldsDetailsJarr.put(barcodeFieldsDetailsJarr.optJSONObject(index));
                            }
                            barcodesFieldsDetailsJobj.put(crossReference, newBarcodeFieldsDetailsJarr);
                            linesJarr.put(lineJobj);
                        }
//                        rowSet.remove(row);
//                        break;
                    }
                }
                if (!isProductExistsInLinkedDoc) {
                    exceptionMsg += "Cross reference " + crossReference + " does not correspond to any of the products from liked doc " + linkDoc.getPurchaseOrderNumber() + ". " + "\n";
                }
            } else if (productsJarr.length() == 0) {
                exceptionMsg += "Product with cross reference " + crossReference + " could not be found. " + "\n";
            } else {
                exceptionMsg += "More then one products found with cross reference " + crossReference + ". " + "\n";
            }
        }
        if (!StringUtil.isNullOrEmpty(exceptionMsg)) {
            throw new AccountingException(exceptionMsg);
        }
        returnJobj.put("goodsreceiptdetail", linesJarr);
        return returnJobj;
    }

    /**
     * Checks is batch available in inventory
     *
     * @param barcodesArr
     * @param companyid
     * @throws JSONException
     * @throws AccountingException
     * @throws ServiceException
     */
    private void checkDuplicateBatchInInventory(String[] barcodesArr, String companyid) throws JSONException, AccountingException, ServiceException {
        String invalidBarcodesStr = "";
        String duplicateBatch = "";
        for (String barcodeValue : barcodesArr) {
            JSONObject barcodeFieldsJobj = getFieldsFromBarcodeJson(barcodeValue);
            if (barcodeFieldsJobj.length() == 4) {
                String batchNumber = barcodeFieldsJobj.getString("batchNumber");
                HashMap<String, Object> requestParams = new HashMap<>();
                requestParams.put("batchname", batchNumber);
                boolean isBatchPresent = false;
                KwlReturnObject jeresult = null;
                requestParams.put("company", companyid);
                isBatchPresent = productDAOObj.IsBatchUsedInOutTransaction(requestParams);
                if (isBatchPresent) {
                    duplicateBatch += (StringUtil.isNullOrEmpty(duplicateBatch) ? batchNumber : (", " + batchNumber));
                }
            } else {
                invalidBarcodesStr += (StringUtil.isNullOrEmpty(invalidBarcodesStr) ? barcodeValue : (", " + barcodeValue));
            }
        }
        if (!StringUtil.isNullOrEmpty(invalidBarcodesStr)) {
            throw new AccountingException("Barcode(s) " + invalidBarcodesStr + " is/are invalid.\n");
        } else if (!StringUtil.isNullOrEmpty(duplicateBatch)) {
            throw new AccountingException("Batch number(s) " + duplicateBatch + " is/are already present in inventory.\n");
        }
    }

    private JSONObject createGlobalFieldsJsonForGR(PurchaseOrder linkDoc, String companyid, JSONObject requestJobj) throws JSONException, ServiceException{
        JSONObject returnJobj = new JSONObject();
        KwlReturnObject kwlObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
        Company company = (Company) kwlObj.getEntityList().get(0);
        returnJobj.put("cdomain", company.getSubDomain());
        returnJobj.put(Constants.useridKey, requestJobj.optString(Constants.useridKey));
        returnJobj.put(Constants.userid, requestJobj.optString(Constants.userid));
        returnJobj.put("billdate", new SimpleDateFormat(Constants.yyyyMMdd).format(new Date()));
        returnJobj.put("currencyvalue", linkDoc.getCurrency() != null ? linkDoc.getCurrency().getCurrencyCode() : company.getCurrency().getCurrencyCode());
        returnJobj.put("vendorvalue", linkDoc.getVendor().getAcccode());
        returnJobj.put("discount", linkDoc.getDiscount());
        returnJobj.put("taxvalue", linkDoc.getTax() != null ? linkDoc.getTax().getTaxCode() : null);
        returnJobj.put("termvalue", linkDoc.getTerm() != null ? linkDoc.getTerm().getTermname() : null);
        returnJobj.put("shipdate", linkDoc.getShipdate());
        returnJobj.put("userdateformat", Constants.yyyyMMdd);
        returnJobj.put("costcentervalue", linkDoc.getCostcenter() != null ? linkDoc.getCostcenter().getName() : null);
        returnJobj.put("agentvalue", linkDoc.getMasteragent() != null ? linkDoc.getMasteragent().getCode() : null);
        returnJobj.put("includeprotax", false);
        returnJobj.put("shipvia", linkDoc.getShipvia());
        returnJobj.put("fob", linkDoc.getFob());
        returnJobj.put("memo", linkDoc.getMemo());
        returnJobj.put("supplierinvoicenumber", linkDoc.getSupplierInvoiceNo());
        returnJobj.put("gstincluded", linkDoc.isGstIncluded());
        returnJobj.put("applytaxtoterms", linkDoc.isApplyTaxToTerms());
        returnJobj.put(Constants.userfullname, requestJobj.optString(Constants.userfullname));
        returnJobj.put(Constants.companyKey, companyid);
        returnJobj.put(Constants.reqHeader, requestJobj.optString(Constants.reqHeader));
        requestJobj.put(Constants.remoteIPAddress, requestJobj.optString(Constants.remoteIPAddress));
        /**
         * Address Details
         */
        BillingShippingAddresses jobj=linkDoc.getBillingShippingAddresses();
        returnJobj=createJsonForAddress(jobj,true,returnJobj); 
        
        /**
         * Link details
         */
        returnJobj.put("linkNumber", linkDoc.getID());
        returnJobj.put("fromLinkCombo", Constants.ACC_PURCHASE_ORDER);

        return returnJobj;
    }
    
    /**
     * vendor is true only when we are creating GRN.
     * @param jobj
     * @param vendor
     * @param returnJobj
     * @return
     * @throws JSONException 
     */
    private JSONObject createJsonForAddress(BillingShippingAddresses jobj,boolean vendor,JSONObject returnJobj)throws JSONException
    {   
        returnJobj.put(Constants.BILLING_ADDRESS, jobj.getBillingAddress());
        returnJobj.put(Constants.BILLING_COUNTRY, jobj.getBillingCountry());
        returnJobj.put(Constants.BILLING_STATE, jobj.getBillingState());
        returnJobj.put(Constants.BILLING_COUNTY, jobj.getBillingCounty());
        returnJobj.put(Constants.BILLING_CITY, jobj.getBillingCity());
        returnJobj.put(Constants.BILLING_POSTAL, jobj.getBillingPostal());
        returnJobj.put(Constants.BILLING_EMAIL, jobj.getBillingEmail());
        returnJobj.put(Constants.BILLING_FAX, jobj.getBillingFax());
        returnJobj.put(Constants.BILLING_MOBILE, jobj.getBillingMobile());
        returnJobj.put(Constants.BILLING_PHONE, jobj.getBillingPhone());
        returnJobj.put(Constants.BILLING_RECIPIENT_NAME, jobj.getBillingRecipientName());
        returnJobj.put(Constants.BILLING_CONTACT_PERSON, jobj.getBillingContactPerson());
        returnJobj.put(Constants.BILLING_CONTACT_PERSON_NUMBER, jobj.getBillingContactPersonNumber());
        returnJobj.put(Constants.BILLING_CONTACT_PERSON_DESIGNATION, jobj.getBillingContactPersonDesignation());
        returnJobj.put(Constants.BILLING_WEBSITE, jobj.getBillingWebsite());
        returnJobj.put(Constants.BILLING_ADDRESS_TYPE, jobj.getBillingAddressType());
        returnJobj.put(Constants.SHIPPING_ADDRESS, jobj.getShippingAddress());
        returnJobj.put(Constants.SHIPPING_COUNTRY, jobj.getShippingCountry());
        returnJobj.put(Constants.SHIPPING_STATE, jobj.getShippingState());
        returnJobj.put(Constants.SHIPPING_COUNTY, jobj.getShippingCounty());
        returnJobj.put(Constants.SHIPPING_CITY, jobj.getShippingCity());
        returnJobj.put(Constants.SHIPPING_EMAIL, jobj.getShippingEmail());
        returnJobj.put(Constants.SHIPPING_FAX, jobj.getShippingFax());
        returnJobj.put(Constants.SHIPPING_MOBILE, jobj.getShippingMobile());
        returnJobj.put(Constants.SHIPPING_PHONE, jobj.getShippingPhone());
        returnJobj.put(Constants.SHIPPING_POSTAL, jobj.getShippingPostal());
        returnJobj.put(Constants.SHIPPING_CONTACT_PERSON_NUMBER, jobj.getShippingContactPersonNumber());
        returnJobj.put(Constants.SHIPPING_CONTACT_PERSON_DESIGNATION, jobj.getShippingContactPersonDesignation());
        returnJobj.put(Constants.SHIPPING_WEBSITE, jobj.getShippingWebsite());
        returnJobj.put(Constants.SHIPPING_CONTACT_PERSON, jobj.getShippingContactPerson());
        returnJobj.put(Constants.SHIPPING_RECIPIENT_NAME, jobj.getShippingRecipientName());
        returnJobj.put(Constants.SHIPPING_ROUTE, jobj.getShippingRoute());
        returnJobj.put(Constants.SHIPPING_ADDRESS_TYPE, jobj.getShippingAddressType());
        if(vendor)
        {
            returnJobj.put(Constants.VENDCUST_SHIPPING_ADDRESS, jobj.getVendcustShippingAddress());
            returnJobj.put(Constants.VENDCUST_SHIPPING_STATE, jobj.getVendcustShippingState());
            returnJobj.put(Constants.VENDCUST_SHIPPING_COUNTRY, jobj.getVendcustShippingCountry());
            returnJobj.put(Constants.VENDCUST_SHIPPING_COUNTY, jobj.getVendcustShippingCounty());
            returnJobj.put(Constants.VENDCUST_SHIPPING_CITY, jobj.getVendcustShippingCity());
            returnJobj.put(Constants.VENDCUST_SHIPPING_EMAIL, jobj.getVendcustShippingEmail());
            returnJobj.put(Constants.VENDCUST_SHIPPING_FAX, jobj.getVendcustShippingFax());
            returnJobj.put(Constants.VENDCUST_SHIPPING_POSTAL, jobj.getVendcustShippingPostal());
            returnJobj.put(Constants.VENDCUST_SHIPPING_MOBILE, jobj.getVendcustShippingMobile());
            returnJobj.put(Constants.VENDCUST_SHIPPING_PHONE, jobj.getVendcustShippingPhone());
            returnJobj.put(Constants.VENDCUST_SHIPPING_RECIPIENT_NAME, jobj.getVendcustShippingRecipientName());
            returnJobj.put(Constants.VENDCUST_SHIPPING_CONTACT_PERSON_NUMBER, jobj.getVendcustShippingContactPersonNumber());
            returnJobj.put(Constants.VENDCUST_SHIPPING_CONTACT_PERSON_DESIGNATION, jobj.getVendcustShippingContactPersonDesignation());
            returnJobj.put(Constants.VENDCUST_SHIPPING_WEBSITE, jobj.getVendcustShippingWebsite());
            returnJobj.put(Constants.VENDCUST_SHIPPING_CONTACT_PERSON, jobj.getVendcustShippingContactPerson());
            returnJobj.put(Constants.VENDCUST_SHIPPING_ADDRESS_TYPE, jobj.getVendcustShippingAddressType());
        }
        return returnJobj;
    }
    
    private String getLocationName(String locationid, String companyid) throws ServiceException {
        String locationName = "";
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("tableName", "Location");
        requestParams.put("fetchColumn", "name");
        requestParams.put("condtionColumn", "id");
        requestParams.put("condtionColumnvalue", locationid);
        requestParams.put("companyColumn", "company.companyID");
        requestParams.put(Constants.companyKey, companyid);
        KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);
        if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
            locationName = (String) result.getEntityList().get(0);
        }
        return locationName;
    }

    private String getWarehouseName(String warehouseid, String companyid) throws ServiceException {
        String warehouseName = "";
        Map<String, String> requestParams = new HashMap<>();
        requestParams.put("tableName", "Store");
        requestParams.put("fetchColumn", "abbreviation");
        requestParams.put("condtionColumn", "id");
        requestParams.put("condtionColumnvalue", warehouseid);
        requestParams.put("companyColumn", "company.companyID");
        requestParams.put(Constants.companyKey, companyid);
        KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);
        if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
            warehouseName = (String) result.getEntityList().get(0);
        }
        return warehouseName;
    }
    
    private JSONObject createLineDetailJsonForGRNew(JSONObject productJobj, PurchaseOrderDetail row, JSONArray barcodeFieldsDetailsJarr) throws JSONException, ServiceException {
        JSONObject returnJobj = new JSONObject();
        String warehouseid = productJobj.optString("warehouse");
        String locationid = productJobj.optString("location");
        String companyid = row.getCompany().getCompanyID();

        /**
         * Get Location
         */
        String location = getLocationName(locationid, companyid);

        /**
         * Get Warehouse
         */
        String warehouse = getWarehouseName(warehouseid, companyid);
        returnJobj.put("productvalue", productJobj.optString("pid"));
        returnJobj.put("rate", row.getRate());
        double quantity = row.getBalanceqty();
        returnJobj.put("quantity", quantity);
        double dquantity = barcodeFieldsDetailsJarr.length() > row.getBalanceqty() ? row.getBalanceqty() : barcodeFieldsDetailsJarr.length();
        returnJobj.put("receivedquantity", dquantity);
        double totalRowQuantityInBarcodes = getTotalQuantityInBarcodes(barcodeFieldsDetailsJarr, dquantity);
        returnJobj.put("baseuomquantity", totalRowQuantityInBarcodes);
        returnJobj.put("uomvalue", row.getUom().getName());
        returnJobj.put("discountType", row.getDiscountispercent());
        returnJobj.put("discount", row.getDiscount());
        returnJobj.put("producttaxvalue", row.getTax() != null ? row.getTax().getTaxCode() : null);
        returnJobj.put("desc", row.getDescription());
        if (productJobj.optBoolean("isBatchForProduct") || productJobj.optBoolean("isSerialForProduct")) {
            JSONArray batchDetailsJarr = new JSONArray();
            for (int i = 0; i < dquantity; i++) {
                batchDetailsJarr = createBatchSerialDetailsJsonForGRNew(batchDetailsJarr, barcodeFieldsDetailsJarr.getJSONObject(i), productJobj.optBoolean("isBatchForProduct"), productJobj.optBoolean("isSerialForProduct"), warehouse, location);
            }
            returnJobj.put("batchdetails", batchDetailsJarr);
        }

        /**
         * Link details
         */
        returnJobj.put("billid", row.getPurchaseOrder().getID());
        returnJobj.put("billno", row.getPurchaseOrder().getPurchaseOrderNumber());
        returnJobj.put("rowid", row.getID());

        return returnJobj;
    }

    private JSONArray createBatchSerialDetailsJsonForGRNew(JSONArray batchSerialDetailsJarr, JSONObject barcodeFieldsDetailsJobj, boolean isBatch, boolean isSerial,String warehouse,String location) throws JSONException {
        JSONObject tempJobj = new JSONObject();
        tempJobj.put("batchquantity", barcodeFieldsDetailsJobj.getInt("weight"));
        tempJobj.put("warehousevalue", warehouse);
        tempJobj.put("locationvalue", location);
        if (isBatch) {
            tempJobj.put("batchvalue", barcodeFieldsDetailsJobj.getString("batchNumber"));
            tempJobj.put("mfgdate", getDateFromString(barcodeFieldsDetailsJobj.getString("packDate")));
        }
        if (isSerial) {
            tempJobj.put("serialnovalue", barcodeFieldsDetailsJobj.getString("batchNumber"));
            tempJobj.put("warrantystart", getDateFromString(barcodeFieldsDetailsJobj.getString("packDate")));
        }
        batchSerialDetailsJarr.put(tempJobj);
        return batchSerialDetailsJarr;
    }

    private JSONObject processBarcodes(String[] barcodesArr) throws JSONException, AccountingException {
        JSONObject returnJobj = new JSONObject();
        String invalidBarcodesStr = "";
        String zeroQuantityBarcodes = "";
        List<String> processedCrossReferences = new ArrayList();
        for (String barcodeValue : barcodesArr) {
            JSONObject barcodeFieldsJobj = getFieldsFromBarcodeJson(barcodeValue);
            if (barcodeFieldsJobj.length() == 4) {
                String crossReference = barcodeFieldsJobj.getString("crossReference");
                /**
                 * If barcode is with zero quantity alert is provided.
                 */
                String weight = barcodeFieldsJobj.optString("weight");
                if (StringUtil.equal(weight, "000000")) {
                    if (!processedCrossReferences.contains(crossReference)) {
                        zeroQuantityBarcodes += (StringUtil.isNullOrEmpty(zeroQuantityBarcodes)) ? crossReference : (", " + crossReference);
                        processedCrossReferences.add(crossReference);
                    }
                } else {
                JSONArray barcodeFieldsJarr = returnJobj.optJSONArray(crossReference) != null ? returnJobj.optJSONArray(crossReference) : new JSONArray();
                barcodeFieldsJarr.put(barcodeFieldsJobj);
                returnJobj.put(crossReference, barcodeFieldsJarr);
                }
            } else {
                invalidBarcodesStr += (StringUtil.isNullOrEmpty(invalidBarcodesStr) ? barcodeValue : (", " + barcodeValue));
            }
        }
        if (!StringUtil.isNullOrEmpty(invalidBarcodesStr)) {
            throw new AccountingException("Barcode(s) " + invalidBarcodesStr + " is/are invalid.\n");
        } else if (!StringUtil.isNullOrEmpty(zeroQuantityBarcodes)) {
            throw new AccountingException("Barcode(s) with cross reference(s)" + zeroQuantityBarcodes + " contains zero quantity.\n");
        }
        return returnJobj;
    }

    private JSONObject getFieldsFromBarcodeJson(String barcodeValue) throws JSONException, AccountingException {
        JSONObject returnJobj = new JSONObject();
        if (barcodeValue != null) {
            barcodeValue = barcodeValue.trim();
            /**
             * Following Code Apply validation on whole Scanned barcode by using regex.
             */
            if (barcodeValue.matches("^\\([0-9]{2}\\)[0-9]{14}\\([0-9]{4}\\)[0-9]{6}\\([0-9]{2}\\)[0-9]{6}\\([0-9]{2}\\)[0-9]{12}$")) {
            String crossReference = barcodeValue.substring(4, 18);
            returnJobj.put("crossReference", crossReference);
            String weight = barcodeValue.substring(24, 30);
            returnJobj.put("weight", weight);
            String packDate = barcodeValue.substring(34, 40);
            returnJobj.put("packDate", packDate);
            String batchNumber = barcodeValue.substring(44, 56);
            returnJobj.put("batchNumber", batchNumber);
        }
        }
        return returnJobj;
    }

    private String getDateFromString(String date) {
        String returnDateStr;
        DateFormat df = new SimpleDateFormat(Constants.yyyyMMdd);
        if (!StringUtil.isNullOrEmpty(date) && date.length() == 6) {
            DateFormat df1 = new SimpleDateFormat("yyMMdd");
            try {
                returnDateStr = df.format(df1.parse(date));
            } catch (ParseException ex) {
                returnDateStr = df.format(new Date());
            }
        } else {
            returnDateStr = df.format(new Date());
        }
        return returnDateStr;
    }
    
    private double getTotalQuantityInBarcodes(JSONArray barcodeFieldsDetailsJarr, double quantity) throws JSONException {
        double totalQuantity = 0;
        for (int i = 0; i < quantity; i++) {
            totalQuantity += barcodeFieldsDetailsJarr.getJSONObject(i).getDouble("weight");
        }
        return totalQuantity;
    }
    
    private double getAvailableQuantityOfBatch(String productid, String batchName, String warehouseName, String locationName) throws JSONException, ServiceException {
        JSONObject paramsJobj = new JSONObject();
        paramsJobj.put("productid",productid);
        paramsJobj.put("batchname",batchName);
        paramsJobj.put("warehouseName",warehouseName);
        paramsJobj.put("locationName",locationName);
        JSONObject jobj = accInvoiceServiceDAO.getProductBatchQuantity(paramsJobj);
        return (jobj != null ? jobj.optDouble("totalProductBatchQty") : 0);
    }

    /**
     * Fetch products mapped to a customer
     * @param paramJobj
     * @return
     * @throws ServiceException
     * @throws SessionExpiredException
     * @throws ParseException
     * @throws JSONException 
     */
    public JSONObject getProductsMappedToCustomer(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject returnObj = new JSONObject();
        String msg = "";
        JSONArray productsJarr = new JSONArray();
        KwlReturnObject kwlObj;
        String customerid = paramJobj.optString("customerid");
        String companyid = paramJobj.optString(Constants.companyKey);
        String ss = paramJobj.optString("ss");
        /**
         * If products are to be filtered on value of a custom field, then 'isCustomFieldFilter' flag becomes true
         * fieldLabel -> Label of custom field on which data is to be filtered
         * jsonstringFilter -> Value to be matched in filter
         */
        String jsonstringFilter = paramJobj.optString("jsonstringFilter");
        String fieldLabel = paramJobj.optString("fieldLabel");
        boolean isCustomFieldFilter = !StringUtil.isNullOrEmpty(fieldLabel) && !StringUtil.isNullOrEmpty(jsonstringFilter);
        
        String customFieldId = null;//Field to store ID of custom field fetched from database
        if (isCustomFieldFilter) {
            HashMap<String, Object> requestParam = new HashMap<String, Object>();
            requestParam.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.fieldlabel, Constants.moduleid));
            requestParam.put(Constants.filter_values, Arrays.asList(companyid, fieldLabel, String.valueOf(Constants.Acc_Product_Master_ModuleId)));
            kwlObj = accJournalEntryobj.getFieldParameters(requestParam);
            List<FieldParams> fieldParamsList = kwlObj != null ? kwlObj.getEntityList() : null;
            FieldParams fieldParams = fieldParamsList != null && !fieldParamsList.isEmpty() ? fieldParamsList.get(0) : null;
            customFieldId =  fieldParams != null ? fieldParams.getId() : null;
        }
        kwlObj = accVendorCustomerProductDAOobj.getProductsByCustomer(customerid, ss, jsonstringFilter);
        List<CustomerProductMapping> cpmList = kwlObj != null ? kwlObj.getEntityList() : null;
        if (cpmList != null && !cpmList.isEmpty()) {
            for (CustomerProductMapping cpm : cpmList) {
                if (cpm != null) {
                    
                    /**
                     * Put product into JSONArray only if either there is no custom field filter or the product matches custom field filter
                     */
                    if (!isCustomFieldFilter) {
                        productsJarr.put(accProductModuleService.createProductJsonObject(cpm.getProducts()));
                    } else {
                        JSONArray customFieldValuesJarr = !StringUtil.isNullOrEmpty(cpm.getJsonstring()) ? new JSONArray(cpm.getJsonstring()) : new JSONArray();
                        /**
                         * Check whether product matches the custom field filter
                         */
                        boolean isProductMatched = false;
                        for (int i = 0; i < customFieldValuesJarr.length(); i++) {
                            JSONObject tempJobj = customFieldValuesJarr.optJSONObject(i);
                            if (tempJobj != null && StringUtil.equal(tempJobj.optString(customFieldId, null), jsonstringFilter)) {
                                isProductMatched = true;
                                break;
                            }
                        }
                        if (isProductMatched) {
                            productsJarr.put(accProductModuleService.createProductJsonObject(cpm.getProducts()));
                        }
                    }
                }
            }
        }
        if (productsJarr.length() == 0) {
            if (isCustomFieldFilter) {
                msg = "No matching product found with the entered data. Please make sure that you are entering the value in correct field.";
            } else {
                msg = "No matching product found with the entered data.";
            }
        } else if (productsJarr.length() > 1 && isCustomFieldFilter) {
            msg = "More than one products match the entered data.";
        } else {
            msg = Constants.RES_success;
        }
        returnObj.put(Constants.RES_data, productsJarr);
        returnObj.put(Constants.RES_TOTALCOUNT, productsJarr.length());
        returnObj.put(Constants.RES_success, true);
        returnObj.put(Constants.RES_msg, msg);
        return returnObj;
    }
    
    /**
     * Fetch products mapped to a vendor
     * @param paramJobj
     * @return
     * @throws ServiceException
     * @throws SessionExpiredException
     * @throws ParseException
     * @throws JSONException 
     */
    public JSONObject getProductsMappedToVendor(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject returnObj = new JSONObject();
        String msg = "";
        JSONArray productsJarr = new JSONArray();
        KwlReturnObject kwlObj;
        String vendorid = paramJobj.optString("vendorid");
        String companyid = paramJobj.optString(Constants.companyKey);
        String ss = paramJobj.optString("ss");
        /**
         * If products are to be filtered on value of a custom field, then 'isCustomFieldFilter' flag becomes true
         * fieldLabel -> Label of custom field on which data is to be filtered
         * jsonstringFilter -> Value to be matched in filter
         */
        String jsonstringFilter = paramJobj.optString("jsonstringFilter");
        String fieldLabel = paramJobj.optString("fieldLabel");
        boolean isCustomFieldFilter = !StringUtil.isNullOrEmpty(fieldLabel) && !StringUtil.isNullOrEmpty(jsonstringFilter);
        
        String customFieldId = null;//Field to store ID of custom field fetched from database
        if (isCustomFieldFilter) {
            HashMap<String, Object> requestParam = new HashMap<String, Object>();
            requestParam.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.fieldlabel, Constants.moduleid));
            requestParam.put(Constants.filter_values, Arrays.asList(companyid, fieldLabel, String.valueOf(Constants.Acc_Product_Master_ModuleId)));
            kwlObj = accJournalEntryobj.getFieldParameters(requestParam);
            List<FieldParams> fieldParamsList = kwlObj != null ? kwlObj.getEntityList() : null;
            FieldParams fieldParams = fieldParamsList != null && !fieldParamsList.isEmpty() ? fieldParamsList.get(0) : null;
            customFieldId =  fieldParams != null ? fieldParams.getId() : null;
        }
        kwlObj = accVendorCustomerProductDAOobj.getProductsByVendor(vendorid, ss, jsonstringFilter);
        List<VendorProductMapping> vpmList = kwlObj != null ? kwlObj.getEntityList() : null;
        if (vpmList != null && !vpmList.isEmpty()) {
            for (VendorProductMapping vpm : vpmList) {
                if (vpm != null) {
                    
                    /**
                     * Put product into JSONArray only if either there is no custom field filter or the product matches custom field filter
                     */
                    if (!isCustomFieldFilter) {
                        productsJarr.put(accProductModuleService.createProductJsonObject(vpm.getProducts()));
                    } else {
                        JSONArray customFieldValuesJarr = !StringUtil.isNullOrEmpty(vpm.getJsonstring()) ? new JSONArray(vpm.getJsonstring()) : new JSONArray();
                        /**
                         * Check whether product matches the custom field filter
                         */
                        boolean isProductMatched = false;
                        for (int i = 0; i < customFieldValuesJarr.length(); i++) {
                            JSONObject tempJobj = customFieldValuesJarr.optJSONObject(i);
                            if (tempJobj != null && StringUtil.equal(tempJobj.optString(customFieldId, null), jsonstringFilter)) {
                                isProductMatched = true;
                                break;
                            }
                        }
                        if (isProductMatched) {
                            productsJarr.put(accProductModuleService.createProductJsonObject(vpm.getProducts()));
                        }
                    }
                }
            }
        }
        if (productsJarr.length() == 0) {
            if (isCustomFieldFilter) {
                msg = "No matching product found with the entered data. Please make sure that you are entering the value in correct field.";
            } else {
                msg = "No matching product found with the entered data.";
            }
        } else if (productsJarr.length() > 1 && isCustomFieldFilter) {
            msg = "More than one products match the entered data.";
        } else {
            msg = Constants.RES_success;
        }
        returnObj.put(Constants.RES_data, productsJarr);
        returnObj.put(Constants.RES_TOTALCOUNT, productsJarr.length());
        returnObj.put(Constants.RES_success, true);
        returnObj.put(Constants.RES_msg, msg);
        return returnObj;
    }
    
    /**
     * Following Method is used to Prepare JsonObject for saving WorkOrder and
     * then call saveAndStartWorkOrder method to save and start WorkOrder
     *
     * @param linkDoc
     * @param barcodesArr
     * @param companyid
     * @param requestJobj
     * @return
     * @throws ServiceException
     * @throws JSONException
     * @throws AccountingException
     * @throws SessionExpiredException
     */
    private JSONObject blockQuantityAndStartWorkOrder(WorkOrder linkDoc, String[] barcodesArr, String companyid, JSONObject requestJobj) throws ServiceException, JSONException, AccountingException, SessionExpiredException {
        JSONObject paramsJobj = createRequestJsonForWONew(linkDoc, barcodesArr, companyid, requestJobj);
        return saveAndStartWorkOrder(paramsJobj);
}

    /**
     * Following Method used to first save and then start work order.
     *
     * @param paramsJobj
     * @return
     * @throws JSONException
     * @throws AccountingException
     * @throws ServiceException
     */
    public JSONObject saveAndStartWorkOrder(JSONObject paramsJobj) throws JSONException, AccountingException, ServiceException {
        JSONObject returnJobj = new JSONObject();
        KwlReturnObject kmsg = null;
        JSONArray paramsJsonArray = new JSONArray();
        paramsJsonArray.put(paramsJobj);
        JSONObject jsonforWorkOrder = new JSONObject();
        jsonforWorkOrder.put(Constants.data, paramsJsonArray);
        String companyid = paramsJobj.optString(Constants.companyKey, null);
        String userid = paramsJobj.optString(Constants.useridKey, null);
        String userfullname = paramsJobj.optString(Constants.userfullname, null);
        String reqHeader = paramsJobj.optString(Constants.reqHeader, null);
        String prdjsondetls = paramsJobj.optString(Constants.detail, null);
        String remoteAddress = paramsJobj.optString(Constants.remoteIPAddress, null);
        String companyPrefDetails = paramsJobj.optString("companyPreferences", null);
        Map<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.data, jsonforWorkOrder);
        requestParams.put(Constants.isEdit, true);
        requestParams.put("isBOMChanged", false);
        requestParams.put("isMassCreate", false);
        requestParams.put(Constants.companyKey, companyid);
        requestParams.put(Constants.useridKey, userid);
        requestParams.put("requestcontextutilsobj", Locale.forLanguageTag(paramsJobj.optString(Constants.language)));
        requestParams.put("details", paramsJobj.optString("details", null));
        requestParams.put(Constants.userfullname, userfullname);
        requestParams.put(Constants.reqHeader, reqHeader);
        requestParams.put(Constants.remoteIPAddress, remoteAddress);
        requestParams.put("companyprefdetails", companyPrefDetails);
        requestParams.put("prdjsondtls", prdjsondetls);
        /**
         * By calling to saveWorkOrder here workorder is saved with blocked
         * quantity equal to scanned barcodes quantity.
         */
        kmsg = accWorkOrderServiceDAOObj.saveWorkOrder(requestParams);

        String msg = "";
        boolean isSuccess = false;
        WorkOrder workorder = (WorkOrder) kmsg.getEntityList().get(1);
        if (StringUtil.isNullObject(workorder)) {
            msg = "Work Order not Saved Successfully";
            isSuccess = false;
        } else {
            HashMap<String, Object> reqParamToStartWO = new HashMap<>();
            String woid = (String) paramsJobj.opt("id");
            JSONObject responseJobj = new JSONObject();
            JSONArray jarrStart = new JSONArray();
            jarrStart.put(woid);
            reqParamToStartWO.put("companyId", companyid);
            reqParamToStartWO.put(Constants.useridKey, userid);
            reqParamToStartWO.put("woidArr", jarrStart);
            reqParamToStartWO.put(Constants.Acc_flag, 1);
            reqParamToStartWO.put(Constants.reqHeader, reqHeader);
            reqParamToStartWO.put("prdjsondtls", prdjsondetls);
            reqParamToStartWO.put(Constants.remoteIPAddress, remoteAddress);
            reqParamToStartWO.put("companyprefdetails", companyPrefDetails);
            reqParamToStartWO.put(Constants.userfullname, userfullname);
            /**
             * Here 'Planned' Status of Work Order is converted to 'In Process'
             * by Calling this method.
             */
            responseJobj = accWorkOrderServiceDAOObj.changeStatustoInProcess(reqParamToStartWO);
            isSuccess = true;
            msg = "Work Order " + paramsJobj.optString("workorderid") + " Started Successfully.";
            returnJobj.put(Constants.RES_data, responseJobj);
        }
        returnJobj.put(Constants.RES_MESSAGE, msg);
        returnJobj.put(Constants.RES_success, isSuccess);
        return returnJobj;
    }

    /**
     * @desc Following method is used to validate scanned barcodes and then
     * validates to start work order or not.
     * @param requestMap
     * @param linkDoc
     * @param barcodesFieldsDetailsJobj
     * @return
     * @throws ServiceException
     * @throws JSONException
     * @throws AccountingException
     * @throws SessionExpiredException
     */
    public JSONObject canStartWorkOrder(HashMap<String, Object> requestMap, WorkOrder linkDoc, JSONObject barcodesFieldsDetailsJobj) throws ServiceException, JSONException, AccountingException, SessionExpiredException {
        String exceptionMsg = "";
        JSONObject jsonResponse = new JSONObject();

        Map<String, Boolean> isBlocked = new HashMap();

        String customerid = StringUtil.isNullObject(linkDoc.getCustomer()) ? "" : linkDoc.getCustomer().getID();
        String companyid = (String) requestMap.get("companyid");
        Set<WorkOrderComponentDetails> rowSet = linkDoc.getComponentDetails();
        Iterator itr = barcodesFieldsDetailsJobj.keys();
        while (itr.hasNext()) {
            String crossReference = (String) itr.next();
            String repeatedBatchNo = duplicatesInArray(barcodesFieldsDetailsJobj.optJSONArray(crossReference));
            JSONObject paramsJobj = new JSONObject();
            paramsJobj.put(Constants.customerid, customerid);
            paramsJobj.put("jsonstringFilter", crossReference);
            paramsJobj.put("fieldLabel", "Cross Reference");
            paramsJobj.put(Constants.companyKey, companyid);
            JSONObject productsJobj = getProductsMappedToCustomer(paramsJobj);
            JSONArray productsJarr = productsJobj.optJSONArray(Constants.RES_data) != null ? productsJobj.optJSONArray(Constants.RES_data) : new JSONArray();
            if (productsJarr.length() == 1) {
                JSONObject productJobj = productsJarr.optJSONObject(0);
                JSONArray barcodeFieldsDetailsJarr = barcodesFieldsDetailsJobj.optJSONArray(crossReference);
                boolean isProductExistsInLinkedDoc = false;
                for (WorkOrderComponentDetails row : rowSet) {
                    if (StringUtil.equal(row.getProduct().getID(), productJobj.optString(Constants.productid))) {
                        isProductExistsInLinkedDoc = true;
                        /**
                         * This checks if Scanned Barcode is equal to Required
                         * quantity of Product.
                         */
                        if (barcodeFieldsDetailsJarr.length() != row.getRequiredQuantity()) {
                            double scannedQuantity = barcodeFieldsDetailsJarr.length();
                            double requiredQuntity = row.getRequiredQuantity();
                            if (requiredQuntity > scannedQuantity) {
                                exceptionMsg += "Scanned quantity of product " + productJobj.optString("pid") + " is less than quantity in work order.\n";
                            } else {
                                exceptionMsg += "Scanned quantity of product " + productJobj.optString("pid") + " exceeds quantity in work order.\n";
                            }
                        } else {
                            isBlocked.put(productJobj.optString(Constants.productid), Boolean.TRUE);
                            }
                        }
                    }
                if (!isProductExistsInLinkedDoc) {
                    exceptionMsg += "Product " + productJobj.optString("pid") + " corresponding to cross reference " + crossReference + " does not exist in linked document " + linkDoc.getWorkOrderID() + ". " + "\n";
                }
            } else if (productsJarr.length() == 0) {
                exceptionMsg += "Product with cross reference " + crossReference + " could not be found.\n";
            } else {
                exceptionMsg += "More then one products found with cross reference " + crossReference + ".\n";
            }
            if (!StringUtil.isNullOrEmpty(repeatedBatchNo)) {
                exceptionMsg += " Scanned Barcodes contain duplicate Batch " + repeatedBatchNo + " for CR " + crossReference + ".\n";
            }
        }

        if (StringUtil.isNullOrEmpty(exceptionMsg)) {
            HashMap<String, Object> dataMap = new HashMap<>();
            dataMap.put(Constants.productid, linkDoc.getProductID().getID());
            dataMap.put(Constants.bomDetailId, linkDoc.getBomid().getID());
            dataMap.put(Constants.isBlocked, isBlocked);
            dataMap.put(Constants.currencyKey, requestMap.get(Constants.currencyKey));
            dataMap.put(Constants.isManageQuantity, "true");
            jsonResponse = validateWorkOrder(dataMap);
        } else {
            jsonResponse.put("msg", exceptionMsg);
            jsonResponse.put(Constants.isValidToStart, false);
        }
        return jsonResponse;
    }

    /**
     * @desc this method validates work order weather to start or not.
     * @param dataMap(productid, bomDetailId, isBlocked, currencyKey,
     * isManageQuantity)
     * @return
     * @throws ServiceException
     * @throws JSONException
     * @throws AccountingException
     * @throws SessionExpiredException
     */
    private JSONObject validateWorkOrder(HashMap<String, Object> dataMap) throws ServiceException, JSONException, AccountingException, SessionExpiredException {
        HashMap<String, Object> isBlocked = (HashMap) dataMap.get(Constants.isBlocked);
        if (isBlocked.containsKey((String) dataMap.get(Constants.productid))) {
            JSONObject jobj = new JSONObject();
            jobj.put(Constants.isValidToStart, true);
            return jobj;
        }
        if (dataMap.containsKey(Constants.bomDetailId) && !StringUtil.isNullOrEmpty((String) dataMap.get(Constants.bomDetailId))) {

            KwlReturnObject result = productDAOObj.getAssemblyItems(dataMap);
            List list = result.getEntityList();
            Iterator ite = list.iterator();
            while (ite.hasNext()) {
                Object[] row = (Object[]) ite.next();
                ProductAssembly passembly = (ProductAssembly) row[0];
                HashMap<String, Object> childDataMap = new HashMap(dataMap);
                childDataMap.put(Constants.productid, passembly.getSubproducts().getID());
                childDataMap.put(Constants.bomDetailId, (passembly.getSubbom() != null ? passembly.getSubbom().getID() : null));
                JSONObject jObj = validateWorkOrder(childDataMap);
                if (!jObj.optBoolean(Constants.isValidToStart)) {
                    JSONObject jobj = new JSONObject();
                    jobj.put(Constants.isValidToStart, false);
                    return jobj;
                }
            }
        } else {
            JSONObject jobj = new JSONObject();
            jobj.put(Constants.isValidToStart, false);
            return jobj;
        }
        JSONObject jobj = new JSONObject();
        jobj.put(Constants.isValidToStart, true);
        return jobj;
    }

    /**
     * Following Method is used to prepare Request JsonObj for workorder.
     *
     * @param linkDoc
     * @param barcodesArr
     * @param companyid
     * @param requestJobj
     * @return
     * @throws ServiceException
     * @throws JSONException
     * @throws AccountingException
     * @throws SessionExpiredException
     */
    private JSONObject createRequestJsonForWONew(WorkOrder linkDoc, String[] barcodesArr, String companyid, JSONObject requestJobj) throws ServiceException, JSONException, AccountingException, SessionExpiredException {
        JSONObject returnJobj = createGlobalFieldsJsonForWO(linkDoc, companyid, requestJobj);
        JSONArray linesJarr = new JSONArray();
        String customerid = StringUtil.isNullObject(linkDoc.getCustomer()) ? "" : linkDoc.getCustomer().getID();
        Set<WorkOrderComponentDetails> rowSet = linkDoc.getComponentDetails();
        String exceptionMsg = "";
        JSONObject barcodesFieldsDetailsJobj = processBarcodes(barcodesArr);
        KwlReturnObject result = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
        Company company = (Company) result.getEntityList().get(0);
        String currencyid = company.getCurrency().getCurrencyID();
        String finalProductid = linkDoc.getProductID().getProductid();
        String bomid = linkDoc.getBomid().getID();
        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("currencyid", currencyid);
        requestMap.put("productid", finalProductid);
        requestMap.put("companyid", companyid);
        requestMap.put("bomid", bomid);

        /**
         * The flag, which indicates whether Work Order should start or not.
         */
        JSONObject canStartWO = canStartWorkOrder(requestMap, linkDoc, barcodesFieldsDetailsJobj);
        boolean isValidToStart = canStartWO.optBoolean(Constants.isValidToStart, false);
        String msg = canStartWO.optString("msg", "");
        if (StringUtil.isNullOrEmpty(msg) && !isValidToStart) {
            exceptionMsg += "All required barcodes have not been scanned.";
        } else {
            exceptionMsg += msg;
        }
        /**
         * Check for Work Order which indicates to start or not.
         */
        if (isValidToStart) {
            Iterator itr = barcodesFieldsDetailsJobj.keys();
            while (itr.hasNext()) {
                String crossReference = (String) itr.next();
//            String repeatedBatchNo = duplicatesInArray(barcodesFieldsDetailsJobj.optJSONArray(crossReference));
                JSONObject paramsJobj = new JSONObject();
                paramsJobj.put(Constants.customerid, customerid);
                paramsJobj.put("jsonstringFilter", crossReference);
                paramsJobj.put("fieldLabel", "Cross Reference");
                paramsJobj.put(Constants.companyKey, companyid);
                JSONObject productsJobj = getProductsMappedToCustomer(paramsJobj);
                JSONArray productsJarr = productsJobj.optJSONArray(Constants.RES_data) != null ? productsJobj.optJSONArray(Constants.RES_data) : new JSONArray();
                if (productsJarr.length() == 1) {
                    JSONObject productJobj = productsJarr.optJSONObject(0);
                    JSONArray barcodeFieldsDetailsJarr = barcodesFieldsDetailsJobj.optJSONArray(crossReference);
//                boolean isProductExistsInLinkedDoc = false;
                    for (WorkOrderComponentDetails row : rowSet) {
                        if (StringUtil.equal(row.getProduct().getID(), productJobj.optString(Constants.productid))) {
//                        isProductExistsInLinkedDoc = true;
//                        /**
//                         * This checks if Scanned Barcode is equal to Required
//                         * quantity of Product.
//                         */
//                        if (barcodeFieldsDetailsJarr.length() != row.getRequiredQuantity()) {
//                            double scannedQuantity = barcodeFieldsDetailsJarr.length();
//                            double requiredQuntity = row.getRequiredQuantity();
//                            if (requiredQuntity > scannedQuantity) {
//                                exceptionMsg += "Scanned quantity of product " + productJobj.optString("pid") + " is less than quantity in work order.\n";
//                            } else {
//                                exceptionMsg += "Scanned quantity of product " + productJobj.optString("pid") + " exceeds quantity in work order.\n";
//                            }
//
//                        } else {
                            try {
                                JSONObject lineJobj = createLineDetailJsonForWO(productJobj, row, barcodeFieldsDetailsJarr, companyid, barcodeFieldsDetailsJarr.length());
                                linesJarr.put(lineJobj);
                            } catch (AccountingException ex) {
                                exceptionMsg += ex.getMessage() != null ? ex.getMessage() : "";
                            }
//                        }
                        }
                    }
//                if (!isProductExistsInLinkedDoc) {
//                    exceptionMsg += "Product " + productJobj.optString("pid") + " corresponding to cross reference " + crossReference + " does not exist in linked document " + linkDoc.getWorkOrderID() + ". " + "\n";
//                }
                }
//            else if (productsJarr.length() == 0) {
//                exceptionMsg += "Product with cross reference " + crossReference + " could not be found.\n";
//            } else {
//                exceptionMsg += "More then one products found with cross reference " + crossReference + ".\n";
//            } 
//            if (!StringUtil.isNullOrEmpty(repeatedBatchNo)) {
//                exceptionMsg += " Scanned Barcodes contain duplicate Batch " + repeatedBatchNo + " for CR " + crossReference + ".\n";
//            }
            }
        }

        if (!StringUtil.isNullOrEmpty(exceptionMsg)) {
            throw new AccountingException(exceptionMsg);
        }
        returnJobj.put("details", linesJarr);
        return returnJobj;
    }
    /**
     * Calculate weight for the provided batch in barcode array.
     *
     * @param barcodeFieldsDetailsJarr
     * @param batch
     * @return
     */
    public double quantityInArray(JSONArray barcodeFieldsDetailsJarr, String batch) {
        double qty = 0.0;
        for (int i = 0; i < barcodeFieldsDetailsJarr.length(); i++) {
            JSONObject batchJsonObj = barcodeFieldsDetailsJarr.optJSONObject(i);
            String jsonBatchNumber = batchJsonObj.optString("batchNumber");
            if (jsonBatchNumber.equalsIgnoreCase(batch)) {
                qty += batchJsonObj.optDouble("weight");
            }
        }
        return qty;
    }

    /**
     * Following Method return String of duplicate batchNumber in Scanned
     * Barcode under CrossReference.
     *
     * @param barcodeFieldsDetailsJarr
     * @return
     */
    public String duplicatesInArray(JSONArray barcodeFieldsDetailsJarr) {
        Map<String, Integer> nameAndCount = new HashMap<>();
        StringBuilder repeatBN = new StringBuilder();
        for (int i = 0; i < barcodeFieldsDetailsJarr.length(); i++) {
            JSONObject batchJsonObj = barcodeFieldsDetailsJarr.optJSONObject(i);
            String jsonBatchNumber = batchJsonObj.optString("batchNumber");
            Integer count = nameAndCount.get(jsonBatchNumber);
            if (count == null) {
                nameAndCount.put(jsonBatchNumber, 1);
            } else {
                nameAndCount.put(jsonBatchNumber, ++count);
            }
        }
        Set<Map.Entry<String, Integer>> entrySet = nameAndCount.entrySet();
        for (Map.Entry<String, Integer> entry : entrySet) {
            if (entry.getValue() > 1) {
                repeatBN.append(entry.getKey());
                repeatBN.append(SEPARATOR);
            }
        }
        String repeatedBN = "";
        if (!StringUtil.isNullOrEmpty(repeatBN.toString())) {
            repeatedBN = repeatBN.toString();
            repeatedBN = repeatedBN.substring(0, repeatedBN.length() - SEPARATOR.length());
        }
        return repeatedBN;
    }

    /**
     * Following mthod used to prepare JsonObj of line detail Json for work
     * order.
     *
     * @param productJobj
     * @param row
     * @param barcodeFieldsDetailsJarr
     * @param extraCompanyPreferences
     * @param totalRowQuantityInBarcode
     * @return
     * @throws JSONException
     * @throws ServiceException
     * @throws AccountingException
     */
    private JSONObject createLineDetailJsonForWO(JSONObject productJobj, WorkOrderComponentDetails row, JSONArray barcodeFieldsDetailsJarr, String companyid, int totalRowQuantityInBarcode) throws JSONException, ServiceException, AccountingException, SessionExpiredException {
        JSONObject returnJobj = new JSONObject();
        DateFormat df = authHandler.getDateOnlyFormat();
        String exceptionMsg = "";
        returnJobj.put(Constants.Acc_id, row.getID());
        returnJobj.put("wodetailid", row.getID());
        returnJobj.put(Constants.productid, productJobj.optString("productid"));
        returnJobj.put("productname", productJobj.optString("productname"));
        returnJobj.put("desc", productJobj.optString("desc"));//Check This
        returnJobj.put(Constants.type, productJobj.optString("type"));

        returnJobj.put("availablequantity", row.getAvailableQuantity());
        returnJobj.put("requiredquantity", row.getRequiredQuantity());
        returnJobj.put("minpercentquantity", row.getMinpercent());
        String parentproductid = StringUtil.isNullObject(row.getParentProduct()) ? "" : row.getParentProduct().getID();
        returnJobj.put(Constants.parentProductId, parentproductid);
        returnJobj.put("isLocationForProduct", productJobj.optBoolean("isLocationForProduct"));
        returnJobj.put("isWarehouseForProduct", productJobj.optBoolean("isWarehouseForProduct"));
        returnJobj.put("isBatchForProduct", productJobj.optBoolean("isBatchForProduct"));
        returnJobj.put("isSerialForProduct", productJobj.optBoolean("isSerialForProduct"));
        returnJobj.put("isRowForProduct", productJobj.optBoolean("isRowForProduct"));
        returnJobj.put("isRackForProduct", productJobj.optBoolean("isRackForProduct"));
        returnJobj.put("isBinForProduct", productJobj.optBoolean("isBinForProduct"));
        returnJobj.put("initialpurchaseprice", row.getInitialPurchasePrice());
        returnJobj.put("purchaseprice", row.getInitialPurchasePrice());
        returnJobj.put("location", productJobj.optString("location"));
        returnJobj.put("blockquantity", totalRowQuantityInBarcode);
        returnJobj.put("warehouse", productJobj.optString("warehouse"));
        returnJobj.put("shortfallquantity", 0);

        /**
         * Here we have to get value from newbatchproduct Which further used to
         * save workorder;
         */
        HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
        filter_names.add("company.companyID");
        filter_params.add(companyid);
        filter_names.add("product");
        filter_params.add(productJobj.optString("productid"));
        filter_names.add("batchname");
        String crossReference = "";
        JSONArray batchJsonArray = new JSONArray();
        for (int i = 0; i < barcodeFieldsDetailsJarr.length(); i++) {
            JSONObject batchJsonObj = barcodeFieldsDetailsJarr.optJSONObject(i);
            crossReference = batchJsonObj.optString("crossReference");
            String batchNumber = batchJsonObj.optString("batchNumber");
            filter_params.add(batchNumber);
            filterRequestParams.put(Constants.filterNamesKey, filter_names);
            filterRequestParams.put(Constants.filterParamsKey, filter_params);
            KwlReturnObject newBatchResult = accMasterItemsDAOobj.getNewBatches(filterRequestParams, false, false);
            filter_params.remove(batchNumber);
            if (!newBatchResult.getEntityList().isEmpty()) {
                NewProductBatch productBatchInfo = (NewProductBatch) newBatchResult.getEntityList().get(0);
                /**
                 * Checks whether balance quantity is zero and if yes gives
                 * exception message.
                 */
                double dueQty = productBatchInfo.getQuantitydue();
                double lockQty = 0.0;
                lockQty = productBatchInfo.getLockquantity();
                double balqty = dueQty - lockQty;
                if (balqty > 0) {
                    JSONObject batchDetailsJobj = new JSONObject();
                    batchDetailsJobj.put(Constants.Acc_id, productBatchInfo.getId());

                    String warehouse = StringUtil.isNullObject(productBatchInfo.getWarehouse()) ? "" : productBatchInfo.getWarehouse().getId();
                    batchDetailsJobj.put("warehouse", warehouse);

                    String location = StringUtil.isNullObject(productBatchInfo.getLocation()) ? "" : productBatchInfo.getLocation().getId();
                    batchDetailsJobj.put("location", location);

                    String newBatchProductrow = StringUtil.isNullObject(productBatchInfo.getRow()) ? "" : productBatchInfo.getRow().getId();
                    batchDetailsJobj.put("row", newBatchProductrow);

                    String rack = StringUtil.isNullObject(productBatchInfo.getRack()) ? "" : productBatchInfo.getRack().getId();
                    batchDetailsJobj.put("rack", rack);

                    String bin = StringUtil.isNullObject(productBatchInfo.getBin()) ? "" : productBatchInfo.getBin().getId();
                    batchDetailsJobj.put("bin", bin);
                    batchDetailsJobj.put(Constants.QUENTITY, 1);

                    String mfgdate = (StringUtil.isNullObject(productBatchInfo.getMfgdate()) ? "" : df.format(productBatchInfo.getMfgdate()));
                    batchDetailsJobj.put("mfgdate", mfgdate);

                    String expdate = (StringUtil.isNullObject(productBatchInfo.getExpdate()) ? "" : df.format(productBatchInfo.getExpdate()));
                    batchDetailsJobj.put("expdate", expdate);
                    batchDetailsJobj.put("purchasebatchid", productBatchInfo.getId());
                    batchDetailsJobj.put("batchname", productBatchInfo.getBatchname());
                    batchDetailsJobj.put("lockquantity", productBatchInfo.getLockquantity());

                    /**
                     * for serial document mapping In our case All these three
                     * purchaseserialid , expstart , expend are null
                     */
                    batchDetailsJobj.put("purchaseserialid", "");
                    batchDetailsJobj.put("expstart", "");
                    batchDetailsJobj.put("expend", "");

                    /**
                     * Following are the keys which are used to show Block details in manage quantity
                     * window after workorder starts. 
                     */
                    batchDetailsJobj.put("serialno","");
                    batchDetailsJobj.put("serialnoid","");
                    batchDetailsJobj.put("isreadyonly",false);
                    batchDetailsJobj.put("isserialusedinDO","");
                    batchDetailsJobj.put("modified","");
                    batchDetailsJobj.put("packlocation","");
                    batchDetailsJobj.put("packwarehouse","");
                    batchDetailsJobj.put("reusablecount","");
                    batchDetailsJobj.put("skufield","");
                    batchDetailsJobj.put("stocktype","");
                    batchDetailsJobj.put("attachment","");
                    batchDetailsJobj.put("attachmentids","");
                    batchDetailsJobj.put("batch",productBatchInfo.getBatchname());
                    batchDetailsJobj.put("balance",balqty);
                    
                    batchDetailsJobj.put(Constants.customfield, "");
                    batchDetailsJobj.put(Constants.DOCUMENTID, row.getID());
                    batchDetailsJobj.put(Constants.avlQuantity, row.getAvailableQuantity());
                    batchDetailsJobj.put("documentbatchid", productBatchInfo.getId());
                    batchDetailsJobj.put("wastageQuantityType", productBatchInfo.getWastageQuantityType());
                    batchDetailsJobj.put("wastageQuantity", productBatchInfo.getWastageQuantity());
                    batchDetailsJobj.put(Constants.productid, productJobj.optString("productid"));
                    batchJsonArray.put(batchDetailsJobj);
                } else {
                    exceptionMsg += "Quantity for Batch " + batchNumber + " for CR " + crossReference + " is unavailable in inventory.\n";
                }
            } else {
                exceptionMsg += "Batch " + batchNumber + " for Cross Reference " + crossReference + " could not be found.\n";
            }
        }

        if (!StringUtil.isNullOrEmpty(exceptionMsg)) {
            throw new AccountingException(exceptionMsg);
        }
        returnJobj.put("batchdetails", batchJsonArray.toString());

        return returnJobj;
    }

    /**
     * Following method is used to prepare jsonObj of global details for
     * workorder.
     *
     * @param linkDoc
     * @param companyid
     * @param requestJobj
     * @return
     * @throws ServiceException
     * @throws JSONException
     * @throws SessionExpiredException
     */
    private JSONObject createGlobalFieldsJsonForWO(WorkOrder linkDoc, String companyid, JSONObject requestJobj) throws ServiceException, JSONException, SessionExpiredException {
        JSONObject returnJobj = new JSONObject();
        DateFormat df = authHandler.getDateOnlyFormat();

        returnJobj.put(Constants.Acc_id, linkDoc.getID());
        returnJobj.put("workordername", linkDoc.getWorkOrderName());
        returnJobj.put("workorderid", linkDoc.getWorkOrderID());
        returnJobj.put(Constants.productid, linkDoc.getProductID().getID());
        returnJobj.put(Constants.QUENTITY, linkDoc.getQuantity());
        returnJobj.put("workorderdate", df.format(linkDoc.getWorkOrderDate()));
        returnJobj.put("dateofdelivery", df.format(linkDoc.getDateOfDelivery()));
        returnJobj.put("workordertype", linkDoc.getWorkOrderType().getID());

        String orderWarehouse = StringUtil.isNullObject(linkDoc.getOrderWarehouse()) ? "" : linkDoc.getOrderWarehouse().getId();
        returnJobj.put("orderWarehouse", orderWarehouse);

        String orderLocation = StringUtil.isNullObject(linkDoc.getOrderLocation()) ? "" : linkDoc.getOrderLocation().getId();
        returnJobj.put("orderLocation", orderLocation);

        returnJobj.put("routingtype", linkDoc.getRoutingMasterType());

        String workorderStatus = StringUtil.isNullObject(linkDoc.getWorkOrderStatus()) ? "" : linkDoc.getWorkOrderStatus().getID();
        returnJobj.put("workorderstatus", workorderStatus);

        String customer = StringUtil.isNullObject(linkDoc.getCustomer()) ? "" : linkDoc.getCustomer().getID();
        returnJobj.put("customer", customer);

        returnJobj.put(Constants.projectId, linkDoc.getProjectId());

        String materailid = StringUtil.isNullObject(linkDoc.getBomid()) ? "" : linkDoc.getBomid().getID();
        returnJobj.put("materialid", materailid);

        returnJobj.put(Constants.isEdit, true);

        String routeTempID = StringUtil.isNullObject(linkDoc.getRouteTemplate()) ? "" : linkDoc.getRouteTemplate().getId();
        returnJobj.put("routetemplateid", routeTempID);
        returnJobj.put("seqformat_oldflag", false);

        String routeTempName = StringUtil.isNullObject(linkDoc.getRouteTemplate()) ? "" : linkDoc.getRouteTemplate().getName();
        returnJobj.put("routetemplatename", routeTempName);

        /**
         * 1 or 2 is given to fromLinkCombo if Sales Order and Sales Contract is
         * selected in WO form 0 for None of them and accordingly 'linkDocNo' is
         * selected from WorkOrder's object linkDoc
         *
         */
        if (StringUtil.isNullObject(linkDoc.getSalesContractID()) && !StringUtil.isNullObject(linkDoc.getSalesOrder())) {
            returnJobj.put("linkDocNo", linkDoc.getSalesOrder().getID());
            returnJobj.put("fromLinkCombo", 1);
        } else if (!StringUtil.isNullObject(linkDoc.getSalesContractID()) && StringUtil.isNullObject(linkDoc.getSalesOrder())) {
            returnJobj.put("linkDocNo", linkDoc.getSalesContractID().getID());
            returnJobj.put("fromLinkCombo", 2);
        } else {
            returnJobj.put("linkDocNo", "");
            returnJobj.put("fromLinkCombo", 0);
        }

        if (StringUtil.isNullObject(linkDoc.getSeqformat())) {
            returnJobj.put("sequenceFormat", "NA");
            returnJobj.put(Constants.SEQFORMAT, "NA");
        } else {
            returnJobj.put("sequenceFormat", linkDoc.getSeqformat().getID());
            returnJobj.put(Constants.SEQFORMAT, linkDoc.getSeqformat().getID());
        }

        /**
         * ********************************
         * Concatinating all Workcenter id's
         */
        Set<WorkOrderWorkCenterMapping> workcenterids = linkDoc.getWorkcentermapping();
        StringBuilder workcenteridBuilder = new StringBuilder();
        for (WorkOrderWorkCenterMapping id : workcenterids) {
            workcenteridBuilder.append(id.getWorkcentreid().getID());
            workcenteridBuilder.append(SEPARATOR);
        }
        String workcenterid = "";
        if (!StringUtil.isNullOrEmpty(workcenteridBuilder.toString())) {
            workcenterid = workcenteridBuilder.toString();
            workcenterid = workcenterid.substring(0, workcenterid.length() - SEPARATOR.length());
        }

        returnJobj.put("workcentreid", workcenterid);

        /**
         * ********************************
         * Concatinating all Machine id's
         */
        Set<WorkOrderMachineMapping> machineids = linkDoc.getMachinemapping();
        StringBuilder machineidBuilder = new StringBuilder();
        for (WorkOrderMachineMapping id : machineids) {
            machineidBuilder.append(id.getMachineid().getID());
            machineidBuilder.append(SEPARATOR);
        }
        String machineid = "";
        if (!StringUtil.isNullOrEmpty(machineidBuilder.toString())) {
            machineid = machineidBuilder.toString();
            machineid = machineid.substring(0, machineid.length() - SEPARATOR.length());
        }
        returnJobj.put("machineid", machineid);

        /**
         * ********************************
         * Concatnating all Labour id's
         */
        Set<WorkOrderLabourMapping> labourids = linkDoc.getLabourmapping();
        StringBuilder labouridBuilder = new StringBuilder();
        for (WorkOrderLabourMapping id : labourids) {
            labouridBuilder.append(id.getLabourid().getID());
            labouridBuilder.append(SEPARATOR);
        }
        String labourid = "";
        if (!StringUtil.isNullOrEmpty(labouridBuilder.toString())) {
            labourid = labouridBuilder.toString();
            labourid = labourid.substring(0, labourid.length() - SEPARATOR.length());
        }
        returnJobj.put("labourid", labourid);

        returnJobj.put(Constants.companyKey, companyid);
        returnJobj.put(Constants.useridKey, requestJobj.optString(Constants.useridKey));
        returnJobj.put(Constants.userfullname, requestJobj.optString(Constants.userfullname));
        returnJobj.put(Constants.reqHeader, requestJobj.optString(Constants.reqHeader));
        requestJobj.put(Constants.remoteIPAddress, requestJobj.optString(Constants.remoteIPAddress));
        requestJobj.put("companyPreferences", requestJobj.optString("companyPreferences"));
        return returnJobj;
    }

}
