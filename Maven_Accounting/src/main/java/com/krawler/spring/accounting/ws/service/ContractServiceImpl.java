/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.ws.service;

import com.krawler.common.admin.Docs;
import com.krawler.common.service.ServiceException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.hql.accounting.Contract;
import com.krawler.hql.accounting.DeliveryOrderDetail;
import com.krawler.hql.accounting.Invoice;
import com.krawler.hql.accounting.InvoiceContractMapping;
import com.krawler.hql.accounting.InvoiceDetail;
import com.krawler.hql.accounting.InvoiceDocuments;
import com.krawler.hql.accounting.JournalEntryDetail;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.NewCompanySetupController;
import com.krawler.spring.accounting.invoice.accInvoiceControllerCMN;
import com.krawler.spring.accounting.salesorder.accContractController;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.ModelAndView;

public class ContractServiceImpl implements ContractService {

    private AccCommonTablesDAO accCommonTablesDAO;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accSalesOrderDAO accSalesOrderDAOobj;
    private WSUtilService wsUtilService;

    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }

    public void setwsUtilService(WSUtilService wsUtilService) {
        this.wsUtilService = wsUtilService;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getContractDetails(JSONObject jobj) throws ServiceException, JSONException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        JSONObject result = new JSONObject();
        if (!jobj.has(Constants.companyKey) || (!jobj.has("contractid") && !jobj.has("customerid"))) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        boolean issuccess = false;

        JSONArray jArr = new JSONArray();
        String companyID = jobj.getString(Constants.companyKey);
        DateFormat df = new SimpleDateFormat(jobj.optString("dateformat",authHandler.getDateOnlyFormatPattern()));
        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.companyKey, companyID);

        KwlReturnObject contractResult = null;
        if (jobj.has("contractid")) {
            String contractID = jobj.getString("contractid");
            requestParams.put("contractid", contractID);
            contractResult = accSalesOrderDAOobj.getContractDetails(requestParams);
        } else {
            String customerID = jobj.getString("customerid");
            requestParams.put("customerid", customerID);
            contractResult = accSalesOrderDAOobj.getContractsOfCompany(requestParams);
        }

        List list = contractResult.getEntityList();

        int totalCount = list.size();
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Contract contract = (Contract) itr.next();

            JSONObject obj = new JSONObject();
            obj.put("id", contract.getID());
            obj.put("accountname", (contract.getCustomer() != null) ? contract.getCustomer().getName() : "");
            obj.put("aggreement", contract.getContractNumber());
            obj.put("currencysymbol", contract.getCurrency().getSymbol());

            // for contract expiry date
            KwlReturnObject contractendate = accSalesOrderDAOobj.getContractStrtendDates(contract.getID());
            List<Object[]> contractDateList = contractendate.getEntityList();
            for (Object[] row : contractDateList) {
                obj.put("agreementExpireyDate", row[1] != null ? df.format(row[1]) : null);
            }

            obj.put("renewAgreement", ""); // not added in pojo
            obj.put("terminateAgreement", ""); // not added in pojo
            obj.put("tenureDetails", ""); // not added in pojo
            obj.put("totalAmount", contract.getAmount());

            jArr.put(obj);
        }

        result.put(Constants.RES_data, jArr);
        result.put(Constants.RES_TOTALCOUNT, jArr != null ? jArr.length() : 0);
        issuccess = true;
        result.put(Constants.RES_success, true);
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getContractTermDetails(JSONObject jobj) throws ServiceException, JSONException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        if (!jobj.has(Constants.companyKey) || !jobj.has("contractid")) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        JSONObject result = new JSONObject();
        boolean issuccess = false;

        JSONArray jArr = new JSONArray();
        String companyID = jobj.getString(Constants.companyKey);
        String contractID = jobj.getString("contractid");
        DateFormat df = new SimpleDateFormat(jobj.optString("dateformat",authHandler.getDateOnlyFormatPattern()));
        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.companyKey, companyID);
        requestParams.put("contractid", contractID);
        KwlReturnObject contractResult = accSalesOrderDAOobj.getContractDetails(requestParams);
        List list = contractResult.getEntityList();
        int totalCount = list.size();
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Contract contract = (Contract) itr.next();

            JSONObject obj = new JSONObject();
            obj.put("id", contract.getID());
            obj.put("aggreement", contract.getContractNumber());
            KwlReturnObject contractendate = accSalesOrderDAOobj.getContractStrtendDates(contract.getID());
            List<Object[]> contractDateList = contractendate.getEntityList();
            for (Object[] row : contractDateList) {
                obj.put("from", row[0] != null ? df.format(row[0]) : null);
                obj.put("to", row[1] != null ? df.format(row[1]) : null);
            }

            int status = contract.getCstatus();
            String statusName = "";
            if (status == 1) {
                statusName = "Active";
            } else if (status == 2) {
                statusName = "Terminated";
                continue;                   // dont add terminated contract in the array
            } else if (status == 3) {
                statusName = "Expire";
            } else if (status == 4) {
                statusName = "Renew";
            }
            obj.put("status", statusName);

            String termType = contract.getTermType();
            String termTypeName = "";
            if (!StringUtil.isNullOrEmpty(termType)) {
                if (termType.equals("1")) {
                    termTypeName = "Day";
                } else if (termType.equals("2")) {
                    termTypeName = "Week";
                } else if (termType.equals("3")) {
                    termTypeName = "Month";
                } else if (termType.equals("4")) {
                    termTypeName = "Year";
                }
            }
            obj.put("leaseTerm", contract.getTermValue() + " " + termTypeName);

            obj.put("lastRenewedDate", ""); // not added in pojo
            obj.put("originalEndDate", contract.getOriginalEndDate() == null ? "" : df.format(contract.getOriginalEndDate()));
            obj.put("signInDate", contract.getSignDate() == null ? "" : df.format(contract.getSignDate()));
            obj.put("moveInDate", contract.getMoveDate() == null ? "" : df.format(contract.getMoveDate()));
            obj.put("moveOutDate", contract.getMoveOutDate() == null ? "" : df.format(contract.getMoveOutDate()));

            jArr.put(obj);
        }

        result.put(Constants.RES_data, jArr);
        result.put(Constants.RES_TOTALCOUNT, jArr != null ? jArr.length() : 0);
        issuccess = true;
        result.put(Constants.RES_success, true);
        return result;
    }

    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getContractInvoiceDetails(JSONObject jobj) throws ServiceException, JSONException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        JSONObject result = new JSONObject();
        if(jobj.has("invoicetype")){
            if(jobj.getString("invoicetype").equals("replacement")){
                result = getContractReplacementInvoiceDetails(jobj);
            }else if(jobj.getString("invoicetype").equals("maintenance")){
                result = getContractMaintenanceInvoiceDetails(jobj);
            }
            else{
                throw ServiceException.FAILURE("Invalid invoice type", "erp14", false);
            }
        }
        else{
            result = getContractNormalInvoiceDetails(jobj);
        }
        result.put(Constants.RES_success, true);        
        return result;
    }
    
    private JSONObject getContractNormalInvoiceDetails(JSONObject jobj) throws ServiceException, JSONException {
        if (!jobj.has(Constants.companyKey) || !jobj.has("contractid")) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        JSONObject result = new JSONObject();
        boolean issuccess = false;
        DateFormat df = new SimpleDateFormat(jobj.optString("dateformat",authHandler.getDateOnlyFormatPattern()));
        double amount = 0;
        JSONArray jArr = new JSONArray();
        String companyID = jobj.getString(Constants.companyKey);
        String contractID = jobj.getString("contractid");

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.companyKey, companyID);
        requestParams.put("contractid", contractID);
        KwlReturnObject invResult = accSalesOrderDAOobj.getContractNormalInvoiceDetails(requestParams);
        List<String> list = invResult.getEntityList();
        int totalCount = invResult.getRecordTotalCount();
        Set<String> invoiceSet = new HashSet<>();
        for (String invContrMapID : list) {
            KwlReturnObject invConMapResult = accountingHandlerDAOobj.getObject(InvoiceContractMapping.class.getName(), invContrMapID);
            InvoiceContractMapping invMap = (InvoiceContractMapping) invConMapResult.getEntityList().get(0);
            Invoice inv = (Invoice) invMap.getInvoice();
            amount = 0;
            if (!invoiceSet.contains(inv.getID())) {

                invoiceSet.add(inv.getID());

                JSONObject obj = new JSONObject();
                obj.put("document", inv.getInvoiceNumber());
                obj.put("description", inv.getMemo());
//                obj.put("date", inv.getJournalEntry().getEntryDate() == null ? "" : df.format(inv.getJournalEntry().getEntryDate()));
                obj.put("date", inv.getCreationDate() == null ? "" : df.format(inv.getCreationDate()));
                JournalEntryDetail d = inv.getCustomerEntry();
                amount += d.getAmount();
                obj.put("amount", authHandler.round(amount, companyID));
                obj.put("currencysymbol", inv.getCurrency().getSymbol());

                jArr.put(obj);
            }
        }

        result.put(Constants.RES_data, jArr);
        result.put(Constants.RES_TOTALCOUNT, jArr != null ? jArr.length() : 0);
        issuccess = true;
        return result;
    }

    private JSONObject getContractReplacementInvoiceDetails(JSONObject jobj) throws ServiceException, JSONException {
        if (!jobj.has(Constants.companyKey) || !jobj.has("contractid")) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        JSONObject result = new JSONObject();
        boolean issuccess = false;
        DateFormat df = new SimpleDateFormat(jobj.optString("dateformat",authHandler.getDateOnlyFormatPattern()));
        int amount;
        double quantity = 0;
        JSONArray jArr = new JSONArray();
        String companyID = jobj.getString(Constants.companyKey);
        String contractID = jobj.getString("contractid");

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.companyKey, companyID);
        requestParams.put("contractid", contractID);
        KwlReturnObject invResult = accSalesOrderDAOobj.getContractReplacementInvoiceDetails(requestParams);
        List<String> list = invResult.getEntityList();
        int totalCount = invResult.getRecordTotalCount();

        Set<String> invoiceSet = new HashSet<>();

        for (String invContrMapID : list) {
            KwlReturnObject invConMapResult = accountingHandlerDAOobj.getObject(InvoiceContractMapping.class.getName(), invContrMapID);
            InvoiceContractMapping invMap = (InvoiceContractMapping) invConMapResult.getEntityList().get(0);
            Invoice inv = (Invoice) invMap.getInvoice();

            if (!invoiceSet.contains(inv.getID())) {

                invoiceSet.add(inv.getID());

                JSONObject obj = new JSONObject();
                obj.put("document", inv.getInvoiceNumber());
                obj.put("description", inv.getMemo());
//                obj.put("date", inv.getJournalEntry().getEntryDate() == null ? "" : df.format(inv.getJournalEntry().getEntryDate()));
                obj.put("date", inv.getCreationDate() == null ? "" : df.format(inv.getCreationDate()));

                Set<InvoiceDetail> invRows = inv.getRows();
                amount = 0;
                if (invRows != null && !invRows.isEmpty()) {
                    for (InvoiceDetail temp : invRows) {
                        quantity = temp.getInventory().getQuantity();
                        amount += authHandler.round(temp.getRate() * quantity, companyID);
                    }
                }
                obj.put("amount", authHandler.round(amount, companyID));
                obj.put("currencysymbol", inv.getCurrency().getSymbol());

                jArr.put(obj);
            }
        }

        result.put(Constants.RES_data, jArr);
        result.put(Constants.RES_TOTALCOUNT, jArr != null ? jArr.length() : 0);
        issuccess = true;
        return result;
    }

    private JSONObject getContractMaintenanceInvoiceDetails(JSONObject jobj) throws ServiceException, JSONException {
        if (!jobj.has(Constants.companyKey) || !jobj.has("contractid")) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        JSONObject result = new JSONObject();
        boolean issuccess = false;
        DateFormat df = new SimpleDateFormat(jobj.optString("dateformat",authHandler.getDateOnlyFormatPattern()));
        int amount;
        double quantity = 0;
        JSONArray jArr = new JSONArray();
        String companyID = jobj.getString(Constants.companyKey);
        String contractID = jobj.getString("contractid");

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.companyKey, companyID);
        requestParams.put("contractid", contractID);

        KwlReturnObject invResult = accSalesOrderDAOobj.getContractMaintenanceInvoiceDetails(requestParams);
        List<String> list = invResult.getEntityList();
        int totalCount = invResult.getRecordTotalCount();

        for (String invID : list) {
            KwlReturnObject invConMapResult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invID);
            Invoice inv = (Invoice) invConMapResult.getEntityList().get(0);

            JSONObject obj = new JSONObject();
            obj.put("document", inv.getInvoiceNumber());
            obj.put("description", inv.getMemo());
//            obj.put("date", inv.getJournalEntry().getEntryDate() == null ? "" : df.format(inv.getJournalEntry().getEntryDate()));
            obj.put("date", inv.getCreationDate() == null ? "" : df.format(inv.getCreationDate()));

            Set<InvoiceDetail> invRows = inv.getRows();
            amount = 0;
            if (invRows != null && !invRows.isEmpty()) {
                for (InvoiceDetail temp : invRows) {
                    quantity = temp.getInventory().getQuantity();
                    amount += authHandler.round(temp.getRate() * quantity, companyID);
                }
            }
            obj.put("amount", authHandler.round(amount, companyID));
            obj.put("currencysymbol", inv.getCurrency().getSymbol());

            jArr.put(obj);
        }

        result.put(Constants.RES_data, jArr);
        result.put(Constants.RES_TOTALCOUNT, jArr != null ? jArr.length() : 0);
        issuccess = true;
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getContractNormalDOItem(JSONObject jobj) throws ServiceException, JSONException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        JSONObject result = new JSONObject();
        if (jobj.has("productid") || jobj.has("pid") || jobj.has("doid")) {
            result = getContractNormalDOItemDetailsRow(jobj);
        } else {
            result = getContractNormalDOItemDetails(jobj);
        }
        result.put(Constants.RES_success, true);        
        return result;
    }

    private JSONObject getContractNormalDOItemDetails(JSONObject jobj) throws ServiceException, JSONException {
        if (!jobj.has(Constants.companyKey) || !jobj.has("contractid")) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        JSONObject result = new JSONObject();
        ArrayList params = new ArrayList();
        boolean issuccess = false;

        JSONArray jArr = new JSONArray();
        String companyID = jobj.getString(Constants.companyKey);
        String contractID = jobj.getString("contractid");

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.companyKey, companyID);
        requestParams.put("contractid", contractID);

        KwlReturnObject doResult = accSalesOrderDAOobj.getContractNormalDOItemDetails(requestParams);
        List<String> list = doResult.getEntityList();
        int totalCount = doResult.getRecordTotalCount();

        for (String dodid : list) {
            KwlReturnObject dodResult = accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(), dodid);
            DeliveryOrderDetail doDetail = (DeliveryOrderDetail) dodResult.getEntityList().get(0);

            JSONObject obj = new JSONObject();
            obj.put("pid", (doDetail.getProduct() != null) ? doDetail.getProduct().getID() : "");
            obj.put("productid", obj.getString("pid"));
            obj.put("itemName", (doDetail.getProduct() != null) ? doDetail.getProduct().getName() : "");
            obj.put("doid", (doDetail.getDeliveryOrder() != null) ? doDetail.getDeliveryOrder().getID() : "");
            obj.put("itemCode", (doDetail.getProduct() != null) ? doDetail.getProduct().getProductid() : "");
            obj.put("itemDescription", (doDetail.getProduct() != null) ? doDetail.getProduct().getDescription() : "");
            obj.put("quantity", doDetail.getActualQuantity());
            String uom = doDetail.getUom() != null ? doDetail.getUom().getNameEmptyforNA() : doDetail.getProduct().getUnitOfMeasure() == null ? "" : doDetail.getProduct().getUnitOfMeasure().getNameEmptyforNA();
            obj.put("unitname", uom);

            jArr.put(obj);
        }

        result.put(Constants.RES_data, jArr);
        result.put(Constants.RES_TOTALCOUNT, jArr != null ? jArr.length() : 0);
        issuccess = true;
        return result;
    }

    private JSONObject getContractNormalDOItemDetailsRow(JSONObject jobj) throws ServiceException, JSONException {
        if (!jobj.has(Constants.companyKey) || !jobj.has("contractid") || (!jobj.has("pid") && !jobj.has("productid")) || !jobj.has("doid")) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }

        JSONObject result = new JSONObject();
        boolean issuccess = false;

        if (jobj.has("productid")) {
            jobj.put("pid", jobj.getString("productid"));
        }
        DateFormat df = new SimpleDateFormat(jobj.optString("dateformat",authHandler.getDateOnlyFormatPattern()));

        JSONArray jArr = new JSONArray();
        String companyID = jobj.getString(Constants.companyKey);
        String contractID = jobj.getString("contractid");
        String pid = jobj.getString("pid");
        String doid = jobj.getString("doid");

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.companyKey, companyID);
        requestParams.put("contractid", contractID);
        requestParams.put("pid", pid);
        requestParams.put("doid", doid);

        KwlReturnObject doRowResult = accSalesOrderDAOobj.getContractNormalDOItemDetailsRow(requestParams);
        List<Object[]> list = doRowResult.getEntityList();
        int totalCount = doRowResult.getRecordTotalCount();

        for (Object[] row : list) {

            JSONObject obj = new JSONObject();
            String serialnoid = (row[0] != null) ? (String) row[0] : "";
            obj.put("srid", serialnoid);
            obj.put("srname", (row[1] != null) ? (String) row[1] : "");
            obj.put("batchname", (row[2] != null) ? (String) row[2] : "");
            obj.put("warrentyExpireyDate", (row[3] != null) ? df.format((Date) row[3]) : "");

            if (!StringUtil.isNullOrEmpty(serialnoid)) {
                obj.put("vendorWarrentyDate", (row[3] != null) ? df.format((Date) row[3]) : "");
            }

            jArr.put(obj);
        }

        result.put(Constants.RES_data, jArr);
        result.put(Constants.RES_TOTALCOUNT, jArr != null ? jArr.length() : 0);
        issuccess = true;
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getContractReplacementDOItem(JSONObject jobj) throws ServiceException, JSONException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        JSONObject result = new JSONObject();
        if (jobj.has("productid") || jobj.has("pid") || jobj.has("productReplacementID")) {
            result = getContractReplacementDOItemDetailsRow(jobj);
        } else {
            result = getContractReplacementDOItemDetails(jobj);
        }
        result.put(Constants.RES_success, true);        
        return result;
    }

    private JSONObject getContractReplacementDOItemDetails(JSONObject jobj) throws ServiceException, JSONException {
        if (!jobj.has(Constants.companyKey) || !jobj.has("contractid")) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        JSONObject result = new JSONObject();
        boolean issuccess = false;
        DateFormat df = new SimpleDateFormat(jobj.optString("dateformat",authHandler.getDateOnlyFormatPattern()));
        JSONArray jArr = new JSONArray();
        String companyID = jobj.getString(Constants.companyKey);
        String contractID = jobj.getString("contractid");

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.companyKey, companyID);
        requestParams.put("contractid", contractID);

        KwlReturnObject doResult = accSalesOrderDAOobj.getContractReplacementDOItemDetails(requestParams);
        List<Object[]> list = doResult.getEntityList();
        int totalCount = doResult.getRecordTotalCount();

        for (Object[] objRow : list) {
            String dodid = (String) objRow[1];
            KwlReturnObject dodResult = accountingHandlerDAOobj.getObject(DeliveryOrderDetail.class.getName(), dodid);
            DeliveryOrderDetail doDetail = (DeliveryOrderDetail) dodResult.getEntityList().get(0);

            JSONObject obj = new JSONObject();
            obj.put("productReplacementID", (objRow[0] != null) ? objRow[0] : "");
            obj.put("pid", (doDetail.getProduct() != null) ? doDetail.getProduct().getID() : "");
            obj.put("productid", obj.getString("pid"));
            obj.put("itemName", (doDetail.getProduct() != null) ? doDetail.getProduct().getName() : "");
            obj.put("dateOfReplacement", (doDetail.getDeliveryOrder() != null && doDetail.getDeliveryOrder().getOrderDate()!=null) ? df.format(doDetail.getDeliveryOrder().getOrderDate()) : "");
            obj.put("itemCode", (doDetail.getProduct() != null) ? doDetail.getProduct().getProductid() : "");
            obj.put("itemDescription", (doDetail.getProduct() != null) ? doDetail.getProduct().getDescription() : "");
            obj.put("quantity", doDetail.getActualQuantity());
            String uom = doDetail.getUom() != null ? doDetail.getUom().getNameEmptyforNA() : doDetail.getProduct().getUnitOfMeasure() == null ? "" : doDetail.getProduct().getUnitOfMeasure().getNameEmptyforNA();
            obj.put("unitname", uom);

            jArr.put(obj);
        }

        result.put(Constants.RES_data, jArr);
        result.put(Constants.RES_TOTALCOUNT, jArr != null ? jArr.length() : 0);
        issuccess = true;
        return result;
    }

    private JSONObject getContractReplacementDOItemDetailsRow(JSONObject jobj) throws ServiceException, JSONException {
        if (!jobj.has(Constants.companyKey) || !jobj.has("contractid") || (!jobj.has("pid") && !jobj.has("productid")) || !jobj.has("productReplacementID")) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        JSONObject result = new JSONObject();
        boolean issuccess = false;

        if (jobj.has("productid")) {
            jobj.put("pid", jobj.getString("productid"));
        }
        JSONArray jArr = new JSONArray();
        String companyID = jobj.getString(Constants.companyKey);
        String contractID = jobj.getString("contractid");
        String pid = jobj.getString("pid");
        String productReplacementID = jobj.getString("productReplacementID");
        DateFormat df = new SimpleDateFormat(jobj.optString("dateformat",authHandler.getDateOnlyFormatPattern()));
        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.companyKey, companyID);
        requestParams.put("contractid", contractID);
        requestParams.put("pid", pid);
        requestParams.put("productReplacementID", productReplacementID);

        KwlReturnObject codresult = accSalesOrderDAOobj.getContractReplacementDOItemDetailsRow(requestParams);
        List<Object[]> list = codresult.getEntityList();
        int totalCount = codresult.getRecordTotalCount();

        for (Object[] row : list) {

            JSONObject obj = new JSONObject();
            String serialnoid = (row[0] != null) ? (String) row[0] : "";
            obj.put("srid", serialnoid);
            obj.put("srname", (row[1] != null) ? (String) row[1] : "");
            obj.put("batchname", (row[2] != null) ? (String) row[2] : "");
            obj.put("warrentyExpireyDate", (row[3] != null) ? df.format((Date) row[3]) : "");

            if (!StringUtil.isNullOrEmpty(serialnoid)) {
                Date vendorExpDate = accCommonTablesDAO.getVendorExpDateForSerial(serialnoid, false);
                if (vendorExpDate != null) {
                    obj.put("vendorWarrentyDate", df.format(vendorExpDate));
                }
            }
            jArr.put(obj);
        }

        result.put(Constants.RES_data, jArr);
        result.put(Constants.RES_TOTALCOUNT, jArr != null ? jArr.length() : 0);
        issuccess = true;
        return result;
    }

    private JSONObject getCustomerContractsAgreementDetails(JSONObject jobj) throws ServiceException, JSONException {
        if (!jobj.has("crmaccountid") && !jobj.has("accountid")) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        JSONObject result = new JSONObject();
        ArrayList params = new ArrayList();
        boolean issuccess = false;

        if (jobj.has("crmaccountid")) {
            jobj.put("accountid", jobj.getString("crmaccountid"));
        }
        JSONArray jArr = new JSONArray();
        String companyID = jobj.getString(Constants.companyKey);
        String customerID = jobj.getString("accountid");
        String contractID = jobj.optString("contractid", "");

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.companyKey, companyID);
        requestParams.put("crmaccountid", customerID);
        requestParams.put("contractid", contractID);
        DateFormat sdf = new SimpleDateFormat(jobj.optString("dateformat",authHandler.getDateOnlyFormatPattern()));
        KwlReturnObject contractResult = accSalesOrderDAOobj.getCustomerContractsFromCRMAccountID(requestParams);
        List list = contractResult.getEntityList();
        int totalCount = contractResult.getRecordTotalCount();

        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Contract contract = (Contract) itr.next();

            JSONObject obj = new JSONObject();
            obj.put("contractrecid", contract.getID());
            if (!contract.isNormalContract()) {
                HashMap<String, Object> requestParams1 = new HashMap<>();
                requestParams1.put(Constants.companyKey, companyID);
                requestParams1.put("contractid", contract.getID());
                KwlReturnObject contractDOMappingResult = accSalesOrderDAOobj.getContractFromDOContractMapping(requestParams1);

                if (contractDOMappingResult.getEntityList().isEmpty()) {
                    continue;
                }
            } else {
                //checked if DO is generated for that Contract or not & if not then excluded those contracts from sending to CRM
                Map<String, Object> params1 = new HashMap<>();
                params1.put("contractid", contract.getID());
                KwlReturnObject results1 = accSalesOrderDAOobj.getContractsDO(params1);
                if (results1.getEntityList().isEmpty()) {
                    continue;
                }
            }
            obj.put("contractid", contract.getContractNumber());
            obj.put("contactperson", contract.getContactPerson());
            obj.put("agreementtype", ""); // not added in pojo

            ArrayList contractDateParams = new ArrayList();
            contractDateParams.add(contract.getID());

            KwlReturnObject contractendate = accSalesOrderDAOobj.getContractStrtendDates(contract.getID());
            List<Object[]> contractDateList = contractendate.getEntityList();
            for (Object[] row : contractDateList) {
                if (!StringUtil.isNullOrEmpty(contractID)) {//changed date format for maintanince date to shown in add maintanince service form in CRM
                    
                    obj.put("fromdate", row[0] != null ? sdf.format(row[0]) : null);
                    obj.put("todate", row[1] != null ? sdf.format(row[1]) : null);
                } else {
                    obj.put("fromdate", row[0] != null ? sdf.format(row[0]) : null);
                    obj.put("todate", row[1] != null ? sdf.format(row[1]) : null);
                }

            }

            int status = contract.getCstatus();
            String statusName = "";
            if (status == 1) {
                statusName = "Active";
            } else if (status == 2) {
                statusName = "Terminated";
                continue;
            } else if (status == 3) {
                statusName = "Expire";
            } else if (status == 4) {
                statusName = "Renew";
            }
            obj.put("statusname", statusName);

            String termType = contract.getTermType();
            String termTypeName = "";
            if (!StringUtil.isNullOrEmpty(termType)) {
                if (termType.equals("1")) {
                    termTypeName = "Day";
                } else if (termType.equals("2")) {
                    termTypeName = "Week";
                } else if (termType.equals("3")) {
                    termTypeName = "Month";
                } else if (termType.equals("4")) {
                    termTypeName = "Year";
                }
            }
            obj.put("leasetermname", contract.getTermValue() + " " + termTypeName);
            obj.put("lastrenewdate", ""); // not added in pojo
            obj.put("orgenddate", contract.getOriginalEndDate() == null ? "" : sdf.format(contract.getOriginalEndDate()));
            obj.put("signindate", contract.getSignDate() == null ? "" : sdf.format(contract.getSignDate()));
            obj.put("moveindate", contract.getMoveDate() == null ? "" : sdf.format(contract.getMoveDate()));
            obj.put("moveoutdate", contract.getMoveOutDate() == null ? "" : sdf.format(contract.getMoveOutDate()));
            obj.put("isNormalSalesContract", contract.isNormalContract());

            jArr.put(obj);
        }

        result.put(Constants.RES_data, jArr);
        result.put(Constants.RES_TOTALCOUNT, jArr != null ? jArr.length() : 0);
        issuccess = true;
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getContractAgreementDetails(JSONObject jobj) throws ServiceException, JSONException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        JSONObject result = new JSONObject();
        if (jobj.has("agreementtype")) {
            if (jobj.getString("agreementtype").equals("cost")) {
                result = getCustomerContractsCostAgreementDetails(jobj);
            } else if (jobj.getString("agreementtype").equals("service")) {
                result = getCustomerContractsServiceAgreementDetails(jobj);
            } else {
                throw ServiceException.FAILURE("Invalid agreement type", "erp15", false);
            }
        } else {
            result = getCustomerContractsAgreementDetails(jobj);
        }
        result.put(Constants.RES_success, true);        
        return result;
    }

    private JSONObject getCustomerContractsCostAgreementDetails(JSONObject jobj) throws ServiceException, JSONException {
        if (!jobj.has("crmaccountid") && !jobj.has("accountid")) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        JSONObject result = new JSONObject();
        ArrayList params = new ArrayList();
        boolean issuccess = false;

        if (jobj.has("crmaccountid")) {
            jobj.put("accountid", jobj.getString("crmaccountid"));
        }
        JSONArray jArr = new JSONArray();
        String companyID = jobj.getString(Constants.companyKey);
        String customerID = jobj.getString("accountid");

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.companyKey, companyID);
        requestParams.put("crmaccountid", customerID);

        KwlReturnObject contractResult = accSalesOrderDAOobj.getCustomerContractsFromCRMAccountID(requestParams);
        List list = contractResult.getEntityList();
        int totalCount = contractResult.getRecordTotalCount();
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Contract contract = (Contract) itr.next();

            JSONObject obj = new JSONObject();
            obj.put("contractid", contract.getContractNumber());
            obj.put("leaseamount", contract.getAmount());
            obj.put("currencysymbol", (contract.getCurrency() != null) ? contract.getCurrency().getSymbol() : "");
            obj.put("securitydepos", ""); // not added in pojo

            HashMap<String, Object> requestParams1 = new HashMap<>();
            requestParams1.put(Constants.companyKey, companyID);
            requestParams1.put("contractid", contract.getID());
            KwlReturnObject invoiceResult = accSalesOrderDAOobj.getContractInvoiceDetails(requestParams);
            List invoiceList = invoiceResult.getEntityList();

            Iterator invoiceListItr = invoiceList.iterator();
            double amount = 0;
            double quantity = 0;

            while (invoiceListItr.hasNext()) {
                InvoiceContractMapping InvMap = (InvoiceContractMapping) invoiceListItr.next();
                Invoice inv = (Invoice) InvMap.getInvoice();

                Set<InvoiceDetail> invRows = inv.getRows();
                if (invRows != null && !invRows.isEmpty()) {
                    for (InvoiceDetail temp : invRows) {
                        quantity = temp.getInventory().getQuantity();
                        amount += authHandler.round(temp.getRate() * quantity, companyID);
                    }
                }
            }

            double contractAmount = contract.getAmount();
            double outstandingAmount = contractAmount - amount;
            obj.put("outstandings", outstandingAmount);

            obj.put("monthlyrent", ""); // not added in pojo

            jArr.put(obj);
        }

        result.put(Constants.RES_data, jArr);
        result.put(Constants.RES_TOTALCOUNT, jArr != null ? jArr.length() : 0);
        issuccess = true;
        return result;
    }

    private JSONObject getCustomerContractsServiceAgreementDetails(JSONObject jobj) throws ServiceException, JSONException {
        if ( (!jobj.has("crmaccountid") && !jobj.has("accountid"))) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        JSONObject result = new JSONObject();
        ArrayList params = new ArrayList();
        String query = "";
        boolean issuccess = false;
        DateFormat sdf = new SimpleDateFormat(jobj.optString("dateformat",authHandler.getDateOnlyFormatPattern()));
        if (jobj.has("crmaccountid")) {
            jobj.put("accountid", jobj.getString("crmaccountid"));
        }
        JSONArray jArr = new JSONArray();
        String companyID = jobj.getString(Constants.companyKey);
        String accountid = jobj.getString("accountid");
        String contractid = jobj.optString("contractid", "");

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.companyKey, companyID);
        requestParams.put("crmaccountid", accountid);
        requestParams.put("contractid", contractid);

        KwlReturnObject contractResult = accSalesOrderDAOobj.getCustomerContractsFromCRMAccountID(requestParams);
        List list = contractResult.getEntityList();
        int totalCount = contractResult.getRecordTotalCount();
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            Contract contract = (Contract) itr.next();

            JSONObject obj = new JSONObject();
            obj.put("contractid", contract.getContractNumber());
            obj.put("agreedservices", contract.getAgreedServices());

            KwlReturnObject resultOfNextServiceDate = accSalesOrderDAOobj.getNextServiceDateOfContract(requestParams);
            if (!resultOfNextServiceDate.getEntityList().isEmpty()) {
                Date nextServiceDate = (Date) resultOfNextServiceDate.getEntityList().get(0);
                obj.put("nextservicedate", nextServiceDate == null ? "" : sdf.format(nextServiceDate));
            }

            KwlReturnObject resultOfPreviousServiceDate = accSalesOrderDAOobj.getPreviousServiceDateOfContract(requestParams);
            if (!resultOfPreviousServiceDate.getEntityList().isEmpty()) {
                Date previousServiceDate = (Date) resultOfPreviousServiceDate.getEntityList().get(0);
                obj.put("lastservicedate", previousServiceDate == null ? "" : sdf.format(previousServiceDate));
            }

            obj.put("oncallservices", ""); // not added in pojo
            obj.put("ongoingservices", ""); // not added in pojo

            jArr.put(obj);
        }

        result.put(Constants.RES_data, jArr);
        result.put(Constants.RES_TOTALCOUNT, jArr != null ? jArr.length() : 0);
        issuccess = true;
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getAccountContractDetails(JSONObject jobj) throws ServiceException, JSONException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        if ( (!jobj.has("crmaccountid") && !jobj.has("accountid"))) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        if (jobj.has("crmaccountid")) {
            jobj.put("accountid", jobj.getString("crmaccountid"));
        }
        JSONObject result = new JSONObject();
        boolean issuccess = false;

        JSONArray jArr = new JSONArray();
        String companyID = jobj.getString(Constants.companyKey);
        String accountid = jobj.getString("accountid");
        int limit = 25;
        int start = 0;
        boolean ispaging = jobj.has("start") && jobj.has("limit") && !StringUtil.isNullOrEmpty(jobj.get("start").toString()) && !StringUtil.isNullOrEmpty(jobj.get("limit").toString());
        if (ispaging) {
            start = Integer.parseInt(jobj.get("start").toString());
            limit = Integer.parseInt(jobj.get("limit").toString());
        }

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.companyKey, companyID);
        requestParams.put("crmaccountid", accountid);
        requestParams.put("ss", jobj.optString("ss",null));
        requestParams.put("dir", jobj.optString("dir",null));
        requestParams.put("sort", jobj.optString("sort",null));
        KwlReturnObject contractResult = accSalesOrderDAOobj.getAccountContractDetails(requestParams);
        int totalCount = contractResult.getRecordTotalCount();
        requestParams.put("start", start);
        requestParams.put("limit", limit);
        contractResult = accSalesOrderDAOobj.getAccountContractDetails(requestParams);

        Iterator itr = contractResult.getEntityList().iterator();
        while (itr.hasNext()) {
            Contract contract = (Contract) itr.next();

            JSONObject obj = new JSONObject();
            obj.put("contractrecid", contract.getID());
            obj.put("contractid", contract.getContractNumber());
            obj.put("isNormalSalesContract", contract.isNormalContract());
            obj.put("accountname", (contract.getCustomer() != null) ? contract.getCustomer().getName() : "");
            obj.put("contactperson", contract.getContactPerson());
            obj.put("email", contract.getEmailID());
            obj.put("noofagreedservices", contract.getAgreedServices());
            obj.put("contractamt", contract.getAmount());
            obj.put("accountid", (contract.getCustomer() != null) ? contract.getCustomer().getCrmaccountid() : "");
            obj.put("id", contract.getContractNumber()); // addded for contract combo
            obj.put("name", contract.getContractNumber());
            obj.put("contractstatus", contract.getCstatus() == 1 ? "Active" : (contract.getCstatus() == 2 ? "Terminate" : (contract.getCstatus() == 3 ? "Expire" : "Renew")));
            obj.put("hasAccess", true);
            /*Fetch Attachments Documents*/
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("invoiceID", contract.getID());
            hashMap.put("companyid", contract.getCompany().getCompanyID());
            KwlReturnObject object = accountingHandlerDAOobj.getinvoiceDocuments(hashMap);
            int attachemntcount = object.getRecordTotalCount();
            obj.put("attachment",attachemntcount);

            jArr.put(obj);
        }

        result.put(Constants.RES_data, jArr);
        result.put(Constants.RES_TOTALCOUNT, jArr != null ? jArr.length() : 0);
        issuccess = true;
        result.put(Constants.RES_success, true);        
        return result;
    }
    
    /* Get attach Documnets in CRM  for particular Contract*/
    @Override
    public JSONObject getAttachDocuments(JSONObject jobj) throws ServiceException, JSONException {
        String msg = "";
        Boolean success = false;
        String targetPath = "";
        String ext = "";
        String documentid = "";
        String documentName="";

        JSONArray jSONArray = new JSONArray();
        JSONObject finalJSONObject = new JSONObject();
        int count = 0;
        try {
            String companyID = jobj.getString(Constants.companyKey);
            String invoiceID = jobj.optString("contractid");
            String start = jobj.optString("start");
            String limit = jobj.optString("limit");
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            hashMap.put("invoiceID", invoiceID);
            hashMap.put(Constants.companyKey, companyID);
            hashMap.put("start", start);
            hashMap.put("limit", limit);
            KwlReturnObject object = accountingHandlerDAOobj.getinvoiceDocuments(hashMap);
            String targetFolder = StorageHandler.GetSharedDocStorePath();
            Iterator iterator = object.getEntityList().iterator();
            while (iterator.hasNext()) {
                Object[] obj = (Object[]) iterator.next();
                documentid = obj[2].toString();
                documentName = obj[0].toString();
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("docname", documentName);
                jSONObject.put("docid", documentid);
                jSONObject.put("doctypeid", obj[1]);
                        
                if (documentName.indexOf('.') != -1) {
                    ext = documentName.substring(documentName.indexOf('.'));
                    targetPath = targetFolder + documentid + ext;
                }
                jSONObject.put("fileTargetPath", targetPath);
                jSONArray.put(jSONObject);
                count++;
            }

            finalJSONObject.put("count", count);
            finalJSONObject.put(Constants.data, jSONArray);
            /* Moving file into sharedfolders if Requested Contract file from CRM  */
            moveFilesFromAccountingToSharedLocation(hashMap);
           
            success = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            success = false;
            Logger.getLogger(ContractServiceImpl.class.getName()).log(Level.SEVERE, "saveUsers", ex);
        } finally {
            try {
                jobj.put(Constants.data, finalJSONObject);
                jobj.put("valid", success);
            } catch (com.krawler.utils.json.base.JSONException ex) {
                Logger.getLogger(ContractServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return finalJSONObject;
    } 
    
    
    /*
     * Below function used to move the Shared files from Accounting Specific
     * folder to Shared Folder
     */
    public void moveFilesFromAccountingToSharedLocation(HashMap requestParams) {

        String documentid = "";
        String documentName = "";
        String ext = "";
        KwlReturnObject object = null;
        JSONObject jobj = new JSONObject();

        try {
            object = accountingHandlerDAOobj.getinvoiceDocuments(requestParams);

            Iterator iterator = object.getEntityList().iterator();
            while (iterator.hasNext()) {
                Object[] obj = (Object[]) iterator.next();
                documentid = obj[2].toString();
                documentName = obj[0].toString();
                String sourceFolder = StorageHandler.GetDocStorePath();
                String targetFolder = StorageHandler.GetSharedDocStorePath();
                if (documentName.indexOf('.') != -1) {
                    ext = documentName.substring(documentName.indexOf('.'));

                }
                String sourcePath = sourceFolder + documentid + ext;
                String targetPath = targetFolder + documentid + ext;

                Path source = FileSystems.getDefault().getPath(sourcePath);

                Path target = FileSystems.getDefault().getPath(targetPath);
                try {
                    Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);    //Available from Java 7

                } catch (NoSuchFileException nfe) {
                    Logger.getLogger(accContractController.class.getName()).log(Level.SEVERE, null, nfe);
                } catch (IOException e) {
                    Logger.getLogger(accContractController.class.getName()).log(Level.SEVERE, null, e);
                }

            }

        } catch (Exception e) {
            Logger.getLogger(accContractController.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    /**
     * Description Update startdate and end date
     * @param jobj
     * @return
     * @throws ServiceException
     * @throws JSONException 
     */
    @Override
    public JSONObject saveContractService(JSONObject jobj) throws ServiceException, JSONException {
        JSONObject returnjobj=new JSONObject();
        try {
            String companyID = jobj.getString(Constants.companyKey);
            String activityid = jobj.optString("activityid");
            String startdate = jobj.optString("startdate");
            String enddate = jobj.optString("enddate");
            String contractid = jobj.optString("contractid");
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            DateFormat df = authHandler.getDateOnlyFormat();
            SimpleDateFormat df1= new SimpleDateFormat("MMMM d, yyyy");
            hashMap.put("activityid", activityid);
            hashMap.put(Constants.companyKey, companyID);
            hashMap.put("startdate", df1.parse(startdate));
            hashMap.put("enddate", df1.parse(enddate));
            KwlReturnObject result=accSalesOrderDAOobj.saveContractService(hashMap);
            if(result.getRecordTotalCount()>0){
                returnjobj.put("success", true);
            }else{
                returnjobj.put("success", false);
            }
        } catch (Exception e) {
            Logger.getLogger(accContractController.class.getName()).log(Level.SEVERE, null, e);
        }
        return returnjobj;
    }
}
