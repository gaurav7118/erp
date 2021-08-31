/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.ws.service;

import com.krawler.common.admin.ServerSpecificOptions;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.IndiaComplianceConstants;
import com.krawler.common.util.JSONUtil;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.servlets.RemoteAPI;
import com.krawler.hql.accounting.*;
import com.krawler.hql.accounting.invoice.service.AccInvoiceModuleService;
import com.krawler.hql.accounting.masteritems.service.AccMasterItemsService;
import com.krawler.inventory.model.stockmovement.StockMovement;
import com.krawler.hql.accounting.InvoiceTermsSales;
import com.krawler.inventory.exception.SeqFormatException;
import com.krawler.inventory.model.sequence.ModuleConst;
import com.krawler.inventory.model.sequence.SeqFormat;
import com.krawler.inventory.model.sequence.SeqService;
import com.krawler.spring.accounting.account.accAccountController;
import com.krawler.spring.accounting.account.accAccountControllerCMN;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.costCenter.AccCostCenterDAO;
import com.krawler.spring.accounting.costCenter.service.AccCostCenterService;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customer.accCustomerControllerCMNService;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.customreports.AccCustomReportService;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.handler.NewCompanySetupController;
import com.krawler.spring.accounting.invoice.AccInvoiceServiceDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.product.productHandler;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import static com.krawler.spring.accounting.tax.TaxConstants.TAXID;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.term.service.AccTermService;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldManagerDAO;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import com.krawler.spring.accounting.product.service.AccProductService;
import com.krawler.spring.accounting.productmodule.service.AccProductModuleService;
import com.krawler.spring.accounting.term.accTermDAO;
import com.krawler.spring.accounting.uom.accUomDAO;
import com.krawler.spring.accounting.uom.service.AccUomService;
import com.krawler.spring.common.*;
import com.krawler.spring.mainaccounting.service.AccCustomerMainAccountingService;
import com.krawler.spring.mrp.WorkOrder.WorkOrder;
import com.krawler.spring.mrp.WorkOrder.WorkOrderComponentDetails;
import com.krawler.spring.mrp.workorder.AccWorkOrderServiceDAOCMN;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.sun.jersey.core.header.FormDataContentDisposition;
import java.io.*;
import java.nio.file.*;

/**
 *
 * @author krawler
 */
public class MasterServiceImpl implements MasterService {

    private accProductDAO accProductObj;
    private fieldManagerDAO fieldManagerDAOobj;
    private accAccountControllerCMN accAccountCMNObj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private accTaxDAO accTaxObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accAccountDAO accAccountDAOobj;
    private accSalesOrderDAO accSalesOrderDAOobj;
    private accCustomerDAO accCustomerDAOobj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private accCurrencyDAO accCurrencyDAOobj;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private AccMasterItemsService accMasterItemsService;
    private AccTermService accTermService;
    private MessageSource messageSource;
    private WSUtilService wsUtilService;
    private accVendorDAO accVendorDAOObj;
    private CompanyService companyServiceObj;
    private AccProductService AccProductService;
    private accCustomerControllerCMNService accCustomerControllerCMNServiceObj;
    private AccWorkOrderServiceDAOCMN accWorkOrderServiceDAOCMNObj;
    private AccCostCenterDAO accCostCenterObj;
    private accTermDAO accTermObj;
    private accUomDAO accUomObj;
    private AccUomService accUomService;
    private AccCostCenterService accCostCenterService;
    private FieldManagerService fieldManagerServiceobj;
    private AccCustomReportService accCustomReportService;
    private AccProductModuleService accProductModuleService;
    private AccCustomerMainAccountingService accCustomerMainAccountingService;
    private AccInvoiceServiceDAO accInvoiceServiceDAO;
    private AccInvoiceModuleService accInvoiceModuleService;
    private fieldDataManager fieldDataManagercntrl;
    private SeqService seqService;
    
    public void setaccInvoiceModuleService(AccInvoiceModuleService accInvoiceModuleService) {
        this.accInvoiceModuleService = accInvoiceModuleService;
    }
    
    public void setaccCustomerControllerCMNServiceObj(accCustomerControllerCMNService accCustomerControllerCMNServiceObj) {
        this.accCustomerControllerCMNServiceObj = accCustomerControllerCMNServiceObj;
    }

    public void setcompanyService(CompanyService companyServiceObj) {
        this.companyServiceObj = companyServiceObj;
    }

    public void setFieldManagerServiceobj(FieldManagerService fieldManagerServiceobj) {
        this.fieldManagerServiceobj = fieldManagerServiceobj;
    }
    
    public void setAccCustomerMainAccountingService(AccCustomerMainAccountingService accCustomerMainAccountingService) {
        this.accCustomerMainAccountingService = accCustomerMainAccountingService;
    }
        
    public void setAccProductModuleService(AccProductModuleService accProductModuleService) {
        this.accProductModuleService = accProductModuleService;
    }
    
    public void setAccCustomReportService(AccCustomReportService accCustomReportService) {
        this.accCustomReportService = accCustomReportService;
    }

    public void setaccProductObj(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setaccVendorDAO(accVendorDAO accVendorDAOObj) {
        this.accVendorDAOObj = accVendorDAOObj;
    }

    public void setfieldManagerDAO(fieldManagerDAO fieldManagerDAOobj) {
        this.fieldManagerDAOobj = fieldManagerDAOobj;
    }

    public void setaccAccountCMNObj(accAccountControllerCMN accAccountCMNObj) {
        this.accAccountCMNObj = accAccountCMNObj;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj;
    }

    public void setAccTaxObj(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setAccAccountDAOobj(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setaccSalesOrderDAO(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }

    public void setAccCustomerDAO(accCustomerDAO accCustomerDAOobj) {
        this.accCustomerDAOobj = accCustomerDAOobj;
    }

    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setaccCurrencyDAOobj(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }

    public void setAccMasterItemsDAOobj(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }

    public void setwsUtilService(WSUtilService wsUtilService) {
        this.wsUtilService = wsUtilService;
    }

    public void setaccMasterItemsService(AccMasterItemsService accMasterItemsService) {
        this.accMasterItemsService = accMasterItemsService;
    }

    public void setAccTermService(AccTermService accTermService) {
        this.accTermService = accTermService;
    }

    public void setmessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }
    
    public void setAccProductService(AccProductService AccProductService) {
        this.AccProductService = AccProductService;
    }
    
    public void setAccWorkOrderServiceDAOCMNObj(AccWorkOrderServiceDAOCMN accWorkOrderServiceDAOCMNObj) {
        this.accWorkOrderServiceDAOCMNObj = accWorkOrderServiceDAOCMNObj;
    }
    
    public void setaccCostCenterDAO(AccCostCenterDAO accCostCenterDAOObj) {
        this.accCostCenterObj = accCostCenterDAOObj;
    }

    public void setaccTermDAO(accTermDAO accTermObj) {
        this.accTermObj = accTermObj;
    }

    public void setaccUomDAO(accUomDAO accUomObj) {
        this.accUomObj = accUomObj;
    }

    public void setaccUomService(AccUomService accUomService) {
        this.accUomService = accUomService;
    }

    public void setAccCostCenterService(AccCostCenterService accCostCenterService) {
        this.accCostCenterService = accCostCenterService;
    }
    
    public void setaccInvoiceServiceDAO(AccInvoiceServiceDAO accInvoiceServiceDAO) {
        this.accInvoiceServiceDAO = accInvoiceServiceDAO;
    }
    
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public SeqService getSeqService() {
        return seqService;
    }

    public void setSeqService(SeqService seqService) {
        this.seqService = seqService;
    }
    
@Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getProduct(JSONObject paramsjobj) throws ServiceException, JSONException, SessionExpiredException {
        JSONObject response = new JSONObject();
        paramsjobj = wsUtilService.populateAdditionalInformation(paramsjobj);
        if (paramsjobj.has("contractnumber") || paramsjobj.has("contractid")) {
            response = getProductByContract(paramsjobj);
        } else if (paramsjobj.optBoolean(Constants.reportFlag, false)||(paramsjobj.optBoolean(Constants.isForReport, false)&& paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false))) {
            response = new JSONObject();
            response=accProductModuleService.getProducts(paramsjobj);
            if (paramsjobj.has(Constants.moduleIds) && !StringUtil.isNullOrEmpty(paramsjobj.optString(Constants.moduleIds))) {
                JSONArray ColumnConfigArr = new JSONArray();
                JSONArray ColumnModelConfigArr = new JSONArray();
                ColumnConfigArr = fieldManagerServiceobj.getColumnHeadersConfigList(paramsjobj);
                String moduleid = String.valueOf(Constants.Acc_Product_Master_ModuleId);
                ColumnModelConfigArr = accCustomReportService.getCustomReportMeasureFieldJsonArray(ColumnConfigArr, moduleid, paramsjobj);
                response.put(Constants.RES_METADATA, ColumnModelConfigArr);
            }
        } else if (paramsjobj.has(Constants.isdefaultHeaderMap) && paramsjobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
            response = AccProductService.getProductsJsonForCombo(paramsjobj);
            HashMap<String, Object> requestParams = productHandler.getProductRequestMapfromJson(paramsjobj);
            KwlReturnObject result = accProductObj.getProducts(requestParams);
            int count = result.getRecordTotalCount();
            response.put(Constants.RES_TOTALCOUNT, count);
        } else if (paramsjobj.has("productId") && paramsjobj.has("projectId")) {
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put("productId", paramsjobj.get("productId"));
            requestParams.put("projectId", paramsjobj.get("projectId"));
            requestParams.put(Constants.currencyKey, paramsjobj.get(Constants.globalCurrencyKey));
            requestParams.put(Constants.companyKey, paramsjobj.get(Constants.companyKey));
            requestParams.put("isManageQuantity", paramsjobj.get("isManageQuantity"));
            requestParams.put(Constants.locale, paramsjobj.get(Constants.locale));
            response = getProductsforID(requestParams);
        }else if (paramsjobj.optBoolean(Constants.isForPos)) {//for pos
              response =  AccProductService.getProductsIdNameforCombo(paramsjobj);
        } else  {
            JSONObject obj = new JSONObject();
            obj = getProductTypes(obj);
            obj = getProducts(paramsjobj.getString(Constants.companyKey), obj);
            response.put(Constants.RES_data, obj);
        }
        response.put(Constants.RES_success, true);
        return response;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getInspectionTemplateList(JSONObject jobj) throws ServiceException, JSONException {
        JSONObject response = new JSONObject();
        try {
            jobj = wsUtilService.populateAdditionalInformation(jobj);
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put(Constants.companyKey, jobj.get(Constants.companyKey));
            if (jobj.has(Constants.userid)) {
                requestParams.put(Constants.userid, jobj.get(Constants.userid));
            }
            if(jobj.has("isInspectionAreaList") && jobj.getBoolean("isInspectionAreaList"))
            {
                requestParams.put("isInspectionAreaList", jobj.get("isInspectionAreaList"));
                requestParams.put("templateId",jobj.get("templateId"));
                response = accMasterItemsService.getInspectionAreaList(requestParams);
            
            }else{
            response = accMasterItemsService.getInspectionTemplateList(requestParams);
            }
            response.put(Constants.RES_success, true);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(MasterServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getProductCategory(JSONObject paramsjobj) throws ServiceException, JSONException, SessionExpiredException {
        JSONObject response = new JSONObject();
        JSONObject obj = new JSONObject();
        paramsjobj = wsUtilService.populateAdditionalInformation(paramsjobj);
        obj = accProductModuleService.getProductsByCategory(paramsjobj);
        response.put(Constants.RES_data, obj);
        response.put(Constants.RES_success, true);
        return response;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getWarehouse(JSONObject jobj) throws ServiceException, JSONException ,SessionExpiredException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        Map<String,Object> requestParams =new HashMap<>();
        requestParams.put(Constants.companyKey, jobj.get(Constants.companyKey));
        if(jobj.has(Constants.userid)){
             requestParams.put(Constants.userid, jobj.get(Constants.userid));
        }
        JSONObject response=accMasterItemsService.getWarehouseItems(requestParams);
        response.put(Constants.RES_success, true);
        return response;
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getLocation(JSONObject jobj) throws ServiceException, JSONException ,SessionExpiredException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        Map<String,Object> requestParams =new HashMap<>();
        requestParams.put(Constants.companyKey, jobj.get(Constants.companyKey));
        if(jobj.has("storeid")){
             requestParams.put("storeid", jobj.get("storeid"));
        }
        JSONObject response=accMasterItemsService.getLocationItemsFromStore(requestParams);
        response.put(Constants.RES_success, true);
        return response;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getNewBatches(JSONObject jobj) throws ServiceException, JSONException, SessionExpiredException {
        JSONObject response = new JSONObject();
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        
        if (jobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {//Mobile Apps--to Actual Web -Application Service
            if (jobj != null) {
                if (!jobj.has("warehouse") || !jobj.has("productid") || !jobj.has("ispurchase") || !jobj.has("isSerialForProduct") || !jobj.has("transType")) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }
            }
            response = accMasterItemsService.getNewBatches(jobj);
        } else {
            Map<String, Object> requestParams = new HashMap<>();
            requestParams.put(Constants.companyKey, jobj.get(Constants.companyKey));
            if (jobj.has("isOnlyBatch")) {
                requestParams.put("isOnlyBatch", jobj.get("isOnlyBatch"));
            }
            if (jobj.has("isOnlyStkRprt")) {
                requestParams.put("isOnlyStkRprt", jobj.get("isOnlyStkRprt"));
            }
            if (jobj.has("linkflag")) {
                requestParams.put("linkflag", Boolean.parseBoolean((String) jobj.get("linkflag")));
            }
            if (jobj.has("isEdit")) {
                requestParams.put("isEdit", Boolean.parseBoolean((String) jobj.get("isEdit")));
            }
            if (jobj.has("copyTrans")) {
                requestParams.put("copyTrans", Boolean.parseBoolean((String) jobj.get("copyTrans")));
            }
            if (jobj.has("documentid")) {
                requestParams.put("documentid", jobj.get("documentid"));
            }
            if (jobj.has("location")) {
                requestParams.put("location", jobj.get("location"));
            }
            if (jobj.has("warehouse")) {
                requestParams.put("warehouse", jobj.get("warehouse"));
            }
            if (jobj.has("row") && !StringUtil.isNullOrEmpty((String) jobj.get("row"))) {
                requestParams.put("row", jobj.get("row"));
            }
            if (jobj.has("rack") && !StringUtil.isNullOrEmpty((String) jobj.get("rack"))) {
                requestParams.put("rack", jobj.get("rack"));
            }
            if (jobj.has("bin") && !StringUtil.isNullOrEmpty((String) jobj.get("bin"))) {
                requestParams.put("bin", jobj.get("bin"));
            }
            if (jobj.has("productid")) {
                requestParams.put("productid", jobj.get("productid"));
            }
            if (jobj.has("ispurchase")) {
                requestParams.put("ispurchase", Boolean.parseBoolean((String) jobj.get("ispurchase")));
            }
            if (jobj.has("checkbatchname")) {
                requestParams.put("checkbatchname", jobj.get("checkbatchname"));
            }
            if (jobj.has("isConsignment")) {
                requestParams.put("isConsignment", Boolean.parseBoolean((String) jobj.get("isConsignment")));
            }
            if (jobj.has("transType") && !StringUtil.isNullOrEmpty((String) jobj.get("transType"))) {
                requestParams.put("transType", Integer.parseInt((String) jobj.get("transType")));
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            requestParams.put("df", sdf);
            response = accMasterItemsService.getNewBatches(requestParams);
        }
        response.put(Constants.RES_success, true);
        return response;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getLevels(JSONObject jobj) throws ServiceException, JSONException ,SessionExpiredException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        Map<String,Object> requestParams =new HashMap<>();
        requestParams.put(Constants.companyKey, jobj.get(Constants.companyKey));
        JSONObject response=accMasterItemsService.getLevels(requestParams);
        response.put(Constants.RES_success, true);
        return response;
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getStoreMasters(JSONObject jobj) throws ServiceException, JSONException ,SessionExpiredException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        Map<String,Object> requestParams =new HashMap<>();
        requestParams.put(Constants.companyKey, jobj.get(Constants.companyKey));
        if(jobj.has("transType")){
            requestParams.put("transType", jobj.get("transType"));
        }
        JSONObject response=accMasterItemsService.getStoreMasters(requestParams);
        response.put(Constants.RES_success, true);
        return response;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getNewSerials(JSONObject jobj) throws ServiceException, JSONException, SessionExpiredException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        JSONObject response = new JSONObject();
        if (jobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {//Mobile Apps--to Actual Web -Application
            if (jobj != null) {
                if (!jobj.has("warehouse") || !jobj.has("productid")|| !jobj.has("transType") || !jobj.has("batch")) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }
            }
            response = accMasterItemsService.getNewSerials(jobj);
        } else {
            Map<String, Object> requestParams = new HashMap<>();
            requestParams = JSONUtil.jsonToMap(jobj);
            requestParams.put(Constants.companyKey, jobj.get(Constants.companyKey));
//         batch checkserialname checkbatchname isEdit copyTrans
//         linkflag isblokedinso documentid duplicatecheck fetchPurchasePrice ispurchase isConsignment isForconsignment
//         transType warehouse location row rack bin
            response = accMasterItemsService.getNewSerials(requestParams);
        }
        response.put(Constants.RES_success, true);
        return response;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getBatchRemaningQuantity(JSONObject jobj) throws ServiceException, JSONException, SessionExpiredException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        Map<String, Object> requestParams = new HashMap<>();
        boolean ismultiRecordRequest =false, isManageQty = false;
        if(jobj.has("ismultiRecordRequest")){
            ismultiRecordRequest= jobj.getBoolean("ismultiRecordRequest");
        }
        if(jobj.has("isManageQty")){    //ERP-40524
            isManageQty= jobj.optBoolean("isManageQty", false);
        }
        requestParams.put("isManageQty",isManageQty);
        
        requestParams=JSONUtil.jsonToMap(jobj);
        requestParams.put(Constants.companyKey, jobj.get(Constants.companyKey));  
        requestParams.put("transType",""+Constants.MRP_WORK_ORDER_MODULEID);// Constants.MRP_WORK_ORDER_MODULEID=1105
        JSONObject response = new JSONObject();
        if (ismultiRecordRequest) {
            response = AccProductService.getBatchRemainingQuantityForMultipleRecords(requestParams);
        } else if (jobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {//Mobile Apps--to Actual Web -Application
            if (jobj != null) {
                if (!jobj.has("transType") || !jobj.has("batchdetails") || !jobj.has("isEdit")) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }
            }
            response =  accInvoiceModuleService.getBatchRemainingQuantity(jobj);
        } else {
            response = AccProductService.getBatchRemainingQuantity(requestParams);
        }
        
        response.put(Constants.RES_success, true);
        return response;
    }
    
    private String encodeStringData(Object obj) {
        String encodeobj = obj.toString();
        String finaldata = "";
        try {
            finaldata = encodeobj.replaceAll("%", "%25");

        } catch (Exception e) {
            e.printStackTrace();
        }
        return finaldata;
    }

    private JSONObject getProductTypes(JSONObject obj) throws ServiceException, JSONException {
        HashMap<String, Object> requestParams = new HashMap<>();;
        KwlReturnObject result = accProductObj.getProductTypes(requestParams);
        List list = result.getEntityList();
        Iterator itr = list.iterator();
        JSONArray jArr = new JSONArray();
        while (itr.hasNext()) {
            Producttype ptype = (Producttype) itr.next();
            JSONObject jobj = new JSONObject();
            jobj.put("id", ptype.getID());
            jobj.put("name", ptype.getName());
            jArr.put(jobj);
        }
        obj.put("typedata", jArr);
        obj.put(Constants.RES_TOTALCOUNT, jArr.length());
        return obj;
    }
    
    /**
     * This method is only called when request comes from CRM side
     * 
     * @param companyID
     * @param jobj
     * @return
     * @throws ServiceException
     * @throws JSONException 
     */
    private JSONObject getProducts(String companyID, JSONObject jobj) throws ServiceException, JSONException {

        ArrayList params = new ArrayList();
        params.add(companyID);
        List<Product> list = accProductObj.getSyncableProduct(companyID);
        int count = list.size();
        JSONArray jArr = new JSONArray();

        //To send product custom data
        Date currentDate;
        try {
            currentDate = authHandler.getDateOnlyFormat(null).parse(authHandler.getDateOnlyFormat(null).format(new Date()));

        } catch (SessionExpiredException ex) {
            Logger.getLogger(MasterServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ParseException ex) {
            Logger.getLogger(MasterServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        /**
             * Get Extra Company pref. checks 
        */
        KwlReturnObject extraPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyID);
        ExtraCompanyPreferences extrareferences = (ExtraCompanyPreferences) extraPref.getEntityList().get(0);
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        requestParams.put(Constants.filter_values, Arrays.asList(companyID, Constants.Acc_Product_Master_ModuleId));
        requestParams.put("isActivated", 1);
        requestParams.put("order_by", Arrays.asList("sequence"));
        requestParams.put("order_type", Arrays.asList("asc"));
        KwlReturnObject result = fieldManagerDAOobj.getFieldParams(requestParams);
        List lst = result.getEntityList();
       
        for (Product product : list) {
            
            /**
             * !product.isIsActive() used to restrict deactivated product.
             */
            if (!product.isIsActive()) {
                continue;
            }
                    
            JSONObject obj = new JSONObject();
            obj.put("id", product.getID());
            obj.put("productname", product.getName());

            //======refer ticket ERP-11075 & ERP-11606============
            Date creationDate = new Date(product.getCreatedon());
            Calendar cal = Calendar.getInstance(); // locale-specific
            cal.setTime(creationDate);

            //sent GMT date if fails
            /**
             * Code commented because from rest we will not get client timezone
             * cal.setTimeZone(TimeZone.getTimeZone("GMT" +
             * sessionHandlerImpl.getTimeZoneDifference(request))); // need to
             * add Application TimeZone
             */
            //Instead of client time zone we can use company creator timezoe for rest processing
            KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyID);
            Company company = (Company) companyResult.getEntityList().get(0);
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            String userdiff = company.getCreator().getTimeZone() != null ? company.getCreator().getTimeZone().getDifference() : company.getTimeZone().getDifference();
            cal.setTimeZone(TimeZone.getTimeZone("GMT" + userdiff));        // need to add Application TimeZone

            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long time = cal.getTimeInMillis();
            obj.put("createdon", time);

            try {
                obj.put("desc", URLEncoder.encode(StringUtil.isNullOrEmpty(product.getDescription()) ? "" : product.getDescription(), "UTF-8"));//desc
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(MasterServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                throw ServiceException.FAILURE(ex.getMessage(), ex);
            }
            obj.put("vendor", (product.getVendor() != null ? product.getVendor().getID() : ""));
            obj.put("vendornameid", (product.getVendor() != null ? product.getVendor().getName() : ""));
            obj.put("vendorphoneno", (product.getVendor() != null ? product.getVendor().getContactNumber() : ""));
            obj.put("vendoremail", (product.getVendor() != null ? product.getVendor().getEmail() : ""));
            obj.put("type", (product.getProducttype() != null ? product.getProducttype().getName() : ""));
            obj.put("pid", !StringUtil.isNullOrEmpty(product.getProductid()) ? product.getProductid() : "");
            if (product.getWarrantyperiod() != 0) {
                obj.put("warrantyperiod", product.getWarrantyperiod());
            }
            if (product.getWarrantyperiodsal() != 0) {
                obj.put("warrantyperiodsal", product.getWarrantyperiodsal());
            }

            String currencyId = "";
            if (product.getCurrency() != null) {
                currencyId = product.getCurrency().getCurrencyID();
            }
            String stockUomId = product.getUnitOfMeasure() != null ? product.getUnitOfMeasure().getID() : "";
            boolean excludeInitialPrice = true;
            KwlReturnObject priceResult = accProductObj.getProductPrice(product.getID(), true, null, "", currencyId,stockUomId,excludeInitialPrice);
            if(priceResult.getEntityList().get(0)==null){
                priceResult = accProductObj.getProductPrice(product.getID(), true, null, "", currencyId); // purchasePrice
            }
            
            double purchasePrice = priceResult.getEntityList().get(0) != null ? (Double) priceResult.getEntityList().get(0) : 0;

            priceResult = accProductObj.getProductPrice(product.getID(), false, null, "", currencyId,stockUomId,excludeInitialPrice);
            if(priceResult.getEntityList().get(0)==null){
                priceResult = accProductObj.getProductPrice(product.getID(), false, null, "", currencyId);
            }
            
            double salesPrice = priceResult.getEntityList().get(0) != null ? (Double) priceResult.getEntityList().get(0) : 0;
            obj.put("currentpurchaseprice", purchasePrice);
            obj.put("saleprice", salesPrice);
            
            KwlReturnObject resultObj = accProductObj.getInitialPrice(product.getID(), true);
             Object initialprice = resultObj.getEntityList().size() > 0 ? resultObj.getEntityList().get(0) : "";
            obj.put("purchaseprice", initialprice == null ? 0 : initialprice);

            //====extra fields to be send to map the columns data====
            obj.put("uomname", (product.getPackaging() != null && product.getPackaging().getStockUoM() != null) ? product.getPackaging().getStockUoM().getNameEmptyforNA() : product.getUnitOfMeasure() !=null ? product.getUnitOfMeasure().getNameEmptyforNA():"");
            obj.put("reorderlevel", product.getReorderLevel());
            obj.put("reorderquantity", product.getReorderQuantity());
            obj.put("leadtime", product.getLeadTimeInDays());
            obj.put("parentid", product.getParent() != null ? product.getParent().getProductName() : "");
            obj.put("salesaccountname", product.getSalesAccount() != null ? product.getSalesAccount().getAccountName() : "");
            obj.put("salesretaccountname", product.getSalesReturnAccount() != null ? product.getSalesReturnAccount().getAccountName() : "");
            obj.put("purchaseaccountname", product.getPurchaseAccount() != null ? product.getPurchaseAccount().getAccountName() : "");
            obj.put("purchaseretaccountname", product.getPurchaseReturnAccount() != null ? product.getPurchaseReturnAccount().getAccountName() : "");
            obj.put("quantity", product.getAvailableQuantity());
            obj.put("locationName", product.getLocation() != null ? product.getLocation().getName() : "");
            obj.put("warehouseName", product.getWarehouse() != null ? product.getWarehouse().getName() : "");
            obj.put("currencyName", product.getCurrency() != null ? product.getCurrency().getCurrencyID() : "");
            obj.put("purchaseuom", product.getPurchaseUOM() != null ? product.getPurchaseUOM().getNameEmptyforNA() : "");
            obj.put("salesuom", product.getSalesUOM() != null ? product.getSalesUOM().getNameEmptyforNA() : "");
            obj.put("casinguom", (product.getPackaging() != null && product.getPackaging().getCasingUoM() != null) ? product.getPackaging().getCasingUoM().getNameEmptyforNA() : "");
            obj.put("inneruom", (product.getPackaging() != null && product.getPackaging().getInnerUoM() != null) ? product.getPackaging().getInnerUoM().getNameEmptyforNA() : "");
            obj.put("casinguom_value", product.getPackaging() != null ? product.getPackaging().getCasingUomValue() : 0);
            obj.put("inneruom_value", product.getPackaging() != null ? product.getPackaging().getInnerUomValue() : 0);
            obj.put("stockuom_value", product.getPackaging() != null ? product.getPackaging().getStockUomValue() : 0);
            obj.put("supplier", product.getSupplier());
            obj.put("coilcraft", product.getCoilcraft());
            obj.put("interplant", product.getInterplant());
            obj.put("wipoffset", product.getWIPOffset());
            obj.put("inventoryoffset", product.getInventoryOffset());
            obj.put("hscode", product.getHSCode());
            obj.put("additionalfreetext", product.getAdditionalFreeText());
            obj.put("itemcolor", product.getItemColor());
            obj.put("alternateproduct", product.getAlternateProduct());
            obj.put("purchasemfg", product.getPurchaseMfg());
            obj.put("catalogno", product.getCatalogNo());
            obj.put("barcode", product.getBarcode());
            try {
                obj.put("additionaldesc", URLEncoder.encode(StringUtil.isNullOrEmpty(product.getAdditionalDesc()) ? "" : product.getAdditionalDesc(), "UTF-8"));
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(MasterServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            obj.put("descinforeign", product.getDescInForeign());
            obj.put("licensecode", product.getLicenseCode());
            obj.put("itemgroup", product.getItemGroup());
            obj.put("pricelist", product.getPriceList());
            obj.put("shippingtype", product.getShippingType());
            obj.put("itemsalesvolume", product.getItemSalesVolume());
            obj.put("productweight", product.getProductweight());
            obj.put("productweightperstockuom",  product.getProductWeightPerStockUom());
            obj.put("productweightincludingpakagingperstockuom", product.getProductWeightIncludingPakagingPerStockUom());
            obj.put("productvolumeperstockuom",product.getProductVolumePerStockUom());
            obj.put("productvolumeincludingpakagingperstockuom",product.getProductVolumeIncludingPakagingPerStockUom());
            obj.put("itemsaleswidth", product.getItemSalesWidth());
            obj.put("itemsalesheight", product.getItemSalesHeight());
            obj.put("itemwidth", product.getItemWidth());
            obj.put("itemvolume", product.getItemVolume());
            obj.put("itempurchasewidth", product.getItemPurchaseWidth());
            obj.put("itempurchaselength", product.getItemPurchaseLength());
            obj.put("qaleadtimeindays", product.getQALeadTimeInDays());
            obj.put("reusabilitycount", product.getReusabilityCount());
            obj.put("orderinguom", product.getOrderingUOM() != null ? product.getOrderingUOM().getNameEmptyforNA() : "");
            obj.put("transferuom", product.getTransferUOM() != null ? product.getTransferUOM().getNameEmptyforNA() : "");
            obj.put("itempurchaseheight", product.getItemPurchaseHeight());
            obj.put("itempurchasevolume", product.getItemPurchaseVolume());
            obj.put("itemsaleslength", product.getItemSalesLength());
            obj.put("itemlength", product.getItemLength());
            obj.put("itemheight", product.getItemHeight());
            obj.put("asofdate", product.getAsOfDate()!=null?product.getAsOfDate().getTime():"");
            /**
             * Get Product level Terms if subdomain have line level term check ON and Integration with CRM from ERP
             * Get Product terms mapped with product while creating
             * Only Sales side terms send to CRM side.
             *
             */
            if (extrareferences != null && extrareferences.getLineLevelTermFlag() == 1) {
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put(IndiaComplianceConstants.SALES_OR_PURCHASE_FLAG, true);//For Sales
                hashMap.put(Constants.productid, product.getID());
                JSONArray ProductTermSalesArr = accProductObj.getProductTermsJsonArray(hashMap);
                obj.put("ProductTermSalesMapp", ProductTermSalesArr);
            }
            
            //Send shared Documents related data to CRM
            Map<String, Object> docsParams = new HashMap<String, Object>();
            String recordID = product.getID();
            docsParams.put("recid", recordID);
            docsParams.put(Constants.companyKey, product.getCompany().getCompanyID());
            docsParams.put("module", "30");
            JSONArray docsjson = accProductObj.getProductDocumentsArray(docsParams);
            obj.put("shareddocs", docsjson);
            //=======================================================
            Map<String, Object> customParams = new HashMap<String, Object>();
            customParams.put("fieldList", lst);
            customParams.put(Constants.companyKey, companyID);
            customParams.put("currentDate", currentDate);
            customParams.put("productObj", product);

            JSONArray customJobj = getProductCutomDataJson(customParams);
            obj.put("customdata", customJobj);

            jArr.put(obj);
        }
        jobj.put("productdata", jArr);
        jobj.put(Constants.RES_TOTALCOUNT, jArr.length());

        //To move file from Accounting Store to Shared Folder Store once product is shared with other Deskera applications.
        moveFilesFromAccountingToSharedLocation(jArr);
            
        /**
         * If subdomain have line level terms as tax then send Company
         * line level terms in JSON, While Sync Product with CRM and its related Checks.
         */
        if (extrareferences != null ) {
            jobj.put(IndiaComplianceConstants.ISLINE_LEVELTERM_FLAG, extrareferences.getLineLevelTermFlag());
            jobj.put(IndiaComplianceConstants.ISEXCISEAPPLICABLE, extrareferences.isExciseApplicable());
            jobj.put(IndiaComplianceConstants.ENABLEVATCST, extrareferences.isEnableVatCst());
            if (extrareferences.getLineLevelTermFlag() == 1) {
                HashMap<String, String> hashMap = new HashMap<String, String>();
                hashMap.put("salesOrPurchaseFlag","true");
                hashMap.put(Constants.companyKey,companyID);
                jobj.put(IndiaComplianceConstants.COMPANY_LINELEVEL_TERMS, accProductObj.getCompanyTermsJsonArray(hashMap));
            }
        }
        
        return jobj;
    }

    private JSONArray getProductCutomDataJson(Map<String, Object> customParams) throws JSONException, ServiceException {
        JSONArray customData = new JSONArray();
        List fieldList = (List) customParams.get("fieldList");
        String companyID = (String) customParams.get(Constants.companyKey);
        Date currentDate = (Date) customParams.get("currentDate");
        Product product = (Product) customParams.get("productObj");
        
        //ERP-32324
        KwlReturnObject serverSpecResult = accountingHandlerDAOobj.getObject(ServerSpecificOptions.class.getName(),ServerSpecificOptions.Case_CustomDateTypeChange);
        ServerSpecificOptions serverSpecificOptions=(ServerSpecificOptions)serverSpecResult.getEntityList().get(0);
        boolean isDeployed;
        isDeployed = StringUtil.isAppDeployed("1",serverSpecificOptions);
       
        DateFormat df=new SimpleDateFormat(Constants.MMMMdyyyy);
        Iterator itr = fieldList.iterator();
        while (itr.hasNext()) {
            FieldParams tmpcontyp = (FieldParams) itr.next();
            JSONObject customJobj = new JSONObject();
            customJobj.put("fieldname", tmpcontyp.getFieldname());
            customJobj.put("sequence", tmpcontyp.getSequence());

            customJobj.put("isessential", tmpcontyp.getIsessential());
            customJobj.put("maxlength", tmpcontyp.getMaxlength());
            customJobj.put("validationtype", tmpcontyp.getValidationtype());
            customJobj.put("fieldid", tmpcontyp.getId());
            customJobj.put("moduleid", tmpcontyp.getModuleid());
            customJobj.put("modulename", "\"Products & Services\"");
            customJobj.put("fieldtype", tmpcontyp.getFieldtype());
            customJobj.put("iseditable", tmpcontyp.getIseditable());
            customJobj.put("comboid", tmpcontyp.getComboid());
            customJobj.put("comboname", tmpcontyp.getComboname());
            customJobj.put("refcolumn_number", Constants.Custom_Column_Prefix + tmpcontyp.getRefcolnum());
            customJobj.put("column_number", Constants.Custom_Column_Prefix + tmpcontyp.getColnum());
            customJobj.put("fieldlabel", tmpcontyp.getFieldlabel());

            AccProductCustomData accProductCustomData = (AccProductCustomData) kwlCommonTablesDAOObj.getClassObject(AccProductCustomData.class.getName(), product.getID());
            if (accProductCustomData != null) {
                String coldata = accProductCustomData.getCol(tmpcontyp.getColnum());
                Object fieldValueObject = accAccountCMNObj.getProductCustomFieldValue(tmpcontyp.getId(), accProductCustomData.getProductId(), companyID, currentDate);
                String latestValue = "";
                if (fieldValueObject != null) {
                    latestValue = (String) fieldValueObject;
                }
                if (!StringUtil.isNullOrEmpty(coldata)) {
                    String value = "";
                    if (latestValue.equalsIgnoreCase(coldata) || StringUtil.isNullOrEmpty(latestValue)) {
                        value = coldata;
                    } else {
                        value = latestValue;
                    }

                    if (tmpcontyp.getFieldtype() == 4 || tmpcontyp.getFieldtype() == 7) {//for combo or multi-select sent display values of selected items
                        String[] array = value.split(",", -1);
                        value = "";
                        for (String id : array) {
                            FieldComboData field = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), id);
                            value += field.getValue() + ", ";
                        }
                        customJobj.put("fieldData", value.substring(0, Math.max(0, value.length() - 2)));
                    } else {
                        
                            if (tmpcontyp.getFieldtype() == 3){
                        
                            if (isDeployed) {

                                customJobj.put("fieldData", value);
                            } else {
                                
                                Date customDate;
                                try {
                                    customDate = df.parse(value);
                                    value = Long.toString(customDate.getTime());
                                } catch (ParseException ex) {
                                    Logger.getLogger(MasterServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                customJobj.put("fieldData", value);
                                
                            }
                                                    
                        }else{
                          customJobj.put("fieldData", value);
                        }
                                      
                    }
                }
            }
            customData.put(customJobj);
        }

        return customData;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject saveTax(JSONObject jobjParam) throws ServiceException, AccountingException, SessionExpiredException, UnsupportedEncodingException, JSONException {
        jobjParam = wsUtilService.populateAdditionalInformation(jobjParam);
        JSONObject result = new JSONObject();
        boolean isSuccess = false;
        result.put(Constants.RES_success, isSuccess);
        String alreadyexist = " But Tax(s) ";
        String added = "Tax(s) ";
        boolean duplicate = false;
        String taxname1 = "";
        try {
            String dateformat = jobjParam.optString("dateformat", "");

            SimpleDateFormat sdf = null;
            try {
                if (!StringUtil.isNullOrEmpty(dateformat)) {
                    sdf = new SimpleDateFormat(dateformat);
                } else {
                    sdf = new SimpleDateFormat("yyyy-MM-dd");
                }
            } catch (IllegalArgumentException iex) {
                sdf = new SimpleDateFormat("yyyy-MM-dd");
            }
            JSONArray jobjTaxDetials = jobjParam.getJSONArray("taxdetails");

            String companyID = jobjParam.optString(Constants.companyKey, "");
            KwlReturnObject taxResult = accTaxObj.getAllTaxOfCompany(companyID);
            List<Tax> list = taxResult.getEntityList();
            for (int i = 0; i < jobjTaxDetials.length(); i++) {
                JSONObject jobj = jobjTaxDetials.getJSONObject(i);               
                if (!StringUtil.isNullOrEmpty(jobj.optString("id", ""))) {
                    String taxid = jobj.getString("id");
                    String taxname = jobj.getString("taxname");
                    KwlReturnObject txResult = accountingHandlerDAOobj.getObject(Tax.class.getName(), taxid);
                    Tax taxObj = (Tax) txResult.getEntityList().get(0);
                    if (taxObj != null) {
                        taxname1 = taxObj.getName();
                    } else {
                        for (Tax obj : list) {
                            if (obj.getName().equals(taxname)) {
                                duplicate = true;
                            }
                        }
                    }
                    if ((taxname1 == null ? taxname != null : !taxname.equals(taxname1)) && !duplicate) {
                        KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyID);
                        Company company = (Company) companyResult.getEntityList().get(0);
                        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

                        Date newdate = new Date();
                        String userdiff = company.getCreator().getTimeZone() != null ? company.getCreator().getTimeZone().getDifference() : company.getTimeZone().getDifference();
                        sdf1.setTimeZone(TimeZone.getTimeZone("GMT" + userdiff));
                        Date newcreatedate = authHandler.getDateWithTimeFormat().parse(sdf1.format(newdate));

                        KWLCurrency currid = company.getCurrency();
                        Account account = null;
                        JSONObject accjson = new JSONObject();
                        accjson.put("currencyid", currid.getCurrencyID());
                        accjson.put("name", "Tax");
                        accjson.put("balance", 0.0);
                        accjson.put("budget", 0.0);
                        accjson.put("minbudget", 0.0);
                        accjson.put("eliminateflag", false);
                        accjson.put(Constants.companyKey, company.getCompanyID());
                        accjson.put("groupid", "3");
                        accjson.put("creationdate", newcreatedate);
                        accjson.put("life", 10);
                        accjson.put("salvage", 0);
                        KwlReturnObject accresult = accAccountDAOobj.addAccount(accjson);
                        account = (Account) accresult.getEntityList().get(0);

                        HashMap<String, Object> taxMap = new HashMap<>();
                        taxMap.put("taxid", jobj.getString("id"));
                        taxMap.put("taxcode",  StringUtil.DecodeText(jobj.optString("taxcode").replaceAll("%", "%25")));
                        taxMap.put("taxname", StringUtil.DecodeText(jobj.optString("taxname").replaceAll("%", "%25")));
                        taxMap.put(Constants.companyKey, company.getCompanyID());
                        taxMap.put("accountid", account.getID());
                        taxMap.put("taxCodeWithoutPercentage", StringUtil.DecodeText(jobj.optString("taxcode").replaceAll("%", "%25")));
                        taxMap.put("taxdescription", jobj.optString("taxdescription", "Sales Tax"));
                        taxMap.put("taxtypeid", jobj.optInt("taxtypeid", 2));
                        KwlReturnObject taxresult = accTaxObj.addTax(taxMap);
                        Tax tax = (Tax) taxresult.getEntityList().get(0);

                        Date date = sdf.parse(jobj.getString("applydateStr"));
                        //Create taxList
                        HashMap<String, Object> taxListMap = new HashMap<>();
                        taxListMap.put("applydate", date);
                        taxListMap.put("taxid", tax.getID());
                        taxListMap.put(Constants.companyKey, company.getCompanyID());
                        taxListMap.put("percent", Double.parseDouble(jobj.getString("percent")));
                        KwlReturnObject taxlistresult = accTaxObj.addTaxList(taxListMap);
                        TaxList taxlist = (TaxList) taxlistresult.getEntityList().get(0);

                        added += "<b>" + taxname + "</b>" + ", ";
                    } else {
                        alreadyexist += "<b>" + taxname + "</b>" + ", ";
                    }
                }
            }
        } catch (ParseException ex) {
            Logger.getLogger(RemoteAPI.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        if (added.equals("Tax(s) ")) {
            added = " No Tax(s) are synced with Accounting";
        } else {
            added = added.substring(0, added.length() - 2);
            added += " are successfully synced with Accounting";
        }
        if (alreadyexist.equals(" But Tax(s) ")) {
            alreadyexist = ""; // are already exists on ERP side. If You Want to Sync then please update these taxes and Sync.";
        } else {
            alreadyexist = alreadyexist.substring(0, alreadyexist.length() - 2);
            alreadyexist += " are already exist on ERP side. If You Want to Sync then please update these taxes and Sync.";
        }
//        result = "{\"success\":true, 'msg':'" + added + alreadyexist + "' ,'syncaccounting' : true,\"companyexist\":true}";;
        isSuccess = true;
        result.put(Constants.RES_success, isSuccess);
        result.put(Constants.RES_MESSAGE, added + alreadyexist);
        result.put("syncaccounting", isSuccess);
        result.put("companyexist", isSuccess);
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getProductByContract(JSONObject jobj) throws ServiceException, JSONException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        JSONObject result = new JSONObject();
        JSONArray jarr = new JSONArray();
        String companyid = jobj.getString(Constants.companyKey);
        String contractid = jobj.optString("contractid", "");
        String contractNumber = jobj.optString("contractnumber", "");
        if (!StringUtil.isNullOrEmpty(contractNumber)) {
            KwlReturnObject contract = accSalesOrderDAOobj.getContractCount(contractNumber, companyid,false,null);
            if (!contract.getEntityList().isEmpty()) {
                int contractCount = contract.getRecordTotalCount();
                if (contractCount == 0) {
                    result.put(Constants.RES_success, true);
                    result.put(Constants.RES_MESSAGE, "Contract does not exist.");
                }
                Contract c = (Contract) contract.getEntityList().get(0);
                contractid = c.getID();
            }
        }
        jarr = getContractProductList(companyid, contractid);
        result.put(Constants.RES_success, true);
        result.put(Constants.RES_data, jarr);
        result.put(Constants.RES_TOTALCOUNT, jarr.length());
        return result;
    }

    private JSONArray getContractProductList(String companyid, String contractid) throws ServiceException, JSONException {
        List list = Collections.EMPTY_LIST;
        JSONArray returnArray = new JSONArray();
        KwlReturnObject result = accSalesOrderDAOobj.getContractProductList(contractid, companyid);
        list = result.getEntityList();
        Iterator itr = list.iterator();

        while (itr.hasNext()) {
            Object[] oj = (Object[]) itr.next();
            JSONObject obj = new JSONObject();
            KwlReturnObject productResult = accountingHandlerDAOobj.getObject(Product.class.getName(), oj[1].toString());
            Product product = (Product) productResult.getEntityList().get(0);

            obj.put("productid", oj[1].toString());
            obj.put("productname", oj[0].toString());
            obj.put("quantity", oj[2].toString());
            obj.put("iscontract", oj[4].toString());
            obj.put("isSerialNumberAvailable", product.isIsSerialForProduct());
            obj.put("serials", accSalesOrderDAOobj.getBatchSerialByProductID(oj[1].toString(), contractid));
            
            /**
             * ERP-32957 preparing delivery quantity for particular productid
             * from contractdetails. ref. line No : 1055,65
             */
            String productid = oj[1].toString();
            if (!StringUtil.isNullOrEmpty((String) oj[5]) && StringUtil.equal((String) oj[5], contractid)) {
                List<String> doId = accSalesOrderDAOobj.getDelivereyOrderID(contractid, companyid);
                double dquantity = 0;
                for (String id : doId) {
                    KwlReturnObject doResult = accountingHandlerDAOobj.getObject(DeliveryOrder.class.getName(), id);
                    DeliveryOrder doObj = (DeliveryOrder) doResult.getEntityList().get(0);
                    Set<DeliveryOrderDetail> rows = doObj.getRows();
                    for (DeliveryOrderDetail doDetail : rows) {
                        String proID = doDetail.getProduct().getID();
                        if (proID.equals(productid) && !StringUtil.isNullOrEmpty(proID)) {
                            dquantity = dquantity + doDetail.getDeliveredQuantity();
                        }
                    }
                }

                obj.put("dquantity", dquantity);
            }
            obj.put("isAsset", oj[3].toString());
            returnArray.put(obj);
        }
        return returnArray;
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getProductsforID(Map<String,Object> requestParams) throws ServiceException, JSONException, SessionExpiredException {
        JSONObject response = new JSONObject();
        JSONArray JArr= AccProductService.getProductsForProject(requestParams);
        response.put(Constants.RES_TOTALCOUNT, JArr.length());
        response.put(Constants.RES_data, JArr);
        response.put(Constants.RES_success, true);
        return response;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getCustomers(JSONObject paramsjobj) throws ServiceException, JSONException {
        JSONObject response = new JSONObject();
        paramsjobj = wsUtilService.populateAdditionalInformation(paramsjobj);
        if (paramsjobj.has("checkcustomeraccount") && paramsjobj.getBoolean("checkcustomeraccount")) {
            response = isCustomerAccountExists(paramsjobj);
        } else {
            String companyid = paramsjobj.getString(Constants.companyKey);

            if (paramsjobj.has("partno")) {
                response = getCustomersWithPart(companyid, paramsjobj.getString("partno"), paramsjobj);
            } else if (paramsjobj.has(Constants.isForReport) && paramsjobj.optBoolean(Constants.isForReport, false) == true) {
                try {
                    response = new JSONObject();
                    JSONArray ColumnConfigArr = new JSONArray();
                    JSONArray ColumnModelConfigArr = new JSONArray();
                    response = accCustomerMainAccountingService.getCustomers(paramsjobj);
                    ColumnConfigArr = fieldManagerServiceobj.getColumnHeadersConfigList(paramsjobj);
                    ColumnModelConfigArr = accCustomReportService.getCustomReportMeasureFieldJsonArray(ColumnConfigArr,Constants.CUSTOMER_MODULE_UUID,paramsjobj);
                    response.put(Constants.RES_METADATA, ColumnModelConfigArr);

                } catch (SessionExpiredException ex) {
                    Logger.getLogger(MasterServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    throw ServiceException.FAILURE(ex.getMessage(), ex);
                }
            } else {
                response = getCustomers(companyid, response, paramsjobj);
            }
            response.put(Constants.RES_success, true);
        }
        if (response.has("msg")) {
            response.put(Constants.RES_MESSAGE, response.getString("msg"));
            response.remove("msg");
        }
        return response;
    }

    private JSONObject getCustomers(String companyid, JSONObject jobj, JSONObject paramsobj) throws ServiceException, JSONException {
        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.companyKey, companyid);
        requestParams.put("notinquery", paramsobj.optString("notinquery", null));

        KwlReturnObject result = accCustomerDAOobj.getCustomer(requestParams);
        KwlReturnObject serverSpecResult = accountingHandlerDAOobj.getObject(ServerSpecificOptions.class.getName(), ServerSpecificOptions.Case_CustomDateTypeChange);
        ServerSpecificOptions serverSpecificOptions = (ServerSpecificOptions) serverSpecResult.getEntityList().get(0);
        boolean isDeployed = StringUtil.isAppDeployed("1", serverSpecificOptions); // 1 is CRM Application ID
        int count = result.getRecordTotalCount();
        String start = paramsobj.optString(Constants.start, null);
        String limit = paramsobj.optString(Constants.limit, null);
        // If condition added to avoid extra hit to db for getting customers within range itself
        if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
            requestParams.put("start", start);
            requestParams.put("limit", limit);
        }
        HashMap<String, String> customFieldMap = new HashMap<String, String>();
        DateFormat df = new SimpleDateFormat(Constants.MMMMdyyyy);
        HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
        HashMap<String, Object> fieldrequestParams = new HashMap();
        fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
        fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Customer_ModuleId));
        HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
        HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
        if (paramsobj.has(Constants.ss) && !StringUtil.isNullOrEmpty((String) paramsobj.get(Constants.ss))) {
            String ss = (String) paramsobj.get(Constants.ss);
            requestParams.put(Constants.ss, ss);
            requestParams.put("ss_names", new String[]{"value"});
        }

        if (paramsobj.has(Constants.sortstring) && !StringUtil.isNullOrEmpty(paramsobj.optString(Constants.sortstring))) {
            requestParams.put(Constants.sortstring, paramsobj.optString(Constants.sortstring));
        }
        result = accCustomerDAOobj.getCustomer(requestParams);
        if (paramsobj.optBoolean(Constants.isdefaultHeaderMap) == true) {
            count = result.getRecordTotalCount();
        }
        List<Customer> customerlist = result.getEntityList();
        JSONArray jArr = new JSONArray();
        for (Customer customer : customerlist) {
            JSONObject obj = new JSONObject();
            obj.put(Constants.customerid, customer.getID());
            obj.put(Constants.accid, customer.getID());
            obj.put("customername", StringUtil.isNullOrEmpty(customer.getName()) ? "" : customer.getName());
            obj.put("accname", StringUtil.isNullOrEmpty(customer.getName()) ? "" : customer.getName());
            obj.put(Constants.currencyKey, (customer.getAccount().getCurrency() == null ? "" : customer.getAccount().getCurrency().getCurrencyID()));
            obj.put("creationdate", (customer.getCreatedOn()) != null ? customer.getCreatedOn().getTime() : null); // CRM Needs date value in Long 
            obj.put("creationDate", (customer.getCreatedOn()) != null ? customer.getCreatedOn().getTime() : null); // CRM Needs date value in Long 
            obj.put("aliasname", (customer.getAliasname()) != null ? customer.getAliasname() : null); // CRM Needs date value in Long 
            int termdays = 0;
            String termname = null;
            String termid = null;
            if (customer.getCreditTerm() != null) {
                Term creditTerm = customer.getCreditTerm();
                termdays = creditTerm.getTermdays();
                termname = creditTerm.getTermname();
                termid = creditTerm.getID();
            }
            obj.put("termdays", termdays);
            obj.put("termname", termname);
            obj.put("termid", termid);
            obj.put("mappedsalesperson", (customer.getMappingSalesPerson()) != null ? customer.getMappingSalesPerson().getValue() : null); // CRM Needs date value in Long 
            obj.put("crmaccountid", StringUtil.isNullOrEmpty(customer.getCrmaccountid()) ? "" : customer.getCrmaccountid());
            obj.put(Constants.SEQUENCEFORMATID, customer.getSeqformat() != null ? customer.getSeqformat().getID() : "NA");
            obj.put("customercode", customer.getAcccode() != null ? customer.getAcccode() : null);
            obj.put("acccode", customer.getAcccode() != null ? customer.getAcccode() : null);
            obj.put("isAutoGenerated", customer.isAutoGenerated());
            obj.put("aliasname", customer.getAliasname());
            obj.put("currencysymbol", (customer.getAccount().getCurrency() == null ? "" : customer.getAccount().getCurrency().getSymbol()));
            obj.put("currencyname", (customer.getAccount().getCurrency() == null ? "" : customer.getAccount().getCurrency().getName()));
            obj.put("taxId", customer.getAccount().getTaxid());
            obj.put("interstateparty", customer.isInterstateparty());//INDIAN Company for CST Tax Calculation (Interstateparty)
            obj.put("cformapplicable", customer.isCformapplicable());//INDIAN Company for CST Tax Calculation (Cformapplicable)
            /**
             * GST TAX Calculation for ERP-CRM Integration while Sync Customer.
             */
            String gstcustomertype = customer.getGSTCustomerType() != null ? (customer.getGSTCustomerType().getDefaultMasterItem() != null ? customer.getGSTCustomerType().getDefaultMasterItem().getID() : "") : "";
            String customergstin = customer.getGSTIN() != null ? customer.getGSTIN() : "";
            obj.put("customergstin", customergstin); // GST IN field
            obj.put("gstcustomertype", accCustomerDAOobj.getUniqueCase(obj.put("type", gstcustomertype)));
            obj.put("sezfromdate", customer.getSezFromDate() != null ? customer.getSezFromDate() : "");//GST Tax Calculation
            obj.put("seztodate", customer.getSezToDate() != null ? customer.getSezToDate() : "");//GST Tax Calculation

            JSONArray jSONArray = new JSONArray();
            Map<String, Object> variableMap = new HashMap<String, Object>();

            KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(CustomerCustomData.class.getName(), customer.getID());
//            replaceFieldMap = new HashMap<String, String>();
            if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                CustomerCustomData jeDetailCustom = (CustomerCustomData) custumObjresult.getEntityList().get(0);
                if (jeDetailCustom != null) {
                    AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                    for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                        JSONObject jSONObject = new JSONObject();
                        String fieldId = replaceFieldMap.get(varEntry.getKey());
                        fieldId = fieldId.replaceAll("custom_", "");
                        jSONObject.put("fieldid", fieldId);
                        if (paramsobj.optBoolean(Constants.isdefaultHeaderMap)) { // this is done to convert the customer fieldid to other modulels custom fieldids. 
                            KwlReturnObject fpresult = accountingHandlerDAOobj.getObject(FieldParams.class.getName(), fieldId);
                            FieldParams tmpcontyp = (FieldParams) fpresult.getEntityList().get(0);
                            if (tmpcontyp != null) {
                                String fieldLabel = tmpcontyp.getFieldlabel();
                                int moduleid = paramsobj.optInt(Constants.moduleid, Constants.Acc_Customer_ModuleId);
                                if (moduleid == Constants.Acc_Cash_Sales_ModuleId) {
                                    moduleid = Constants.Acc_Invoice_ModuleId;
                                }
                                if (moduleid != Constants.Acc_Customer_ModuleId) { //checking for other than moduleids
                                    HashMap<String, Object> invFieldParamsMap = new HashMap<>();

                                    invFieldParamsMap.put(Constants.filter_names, Arrays.asList(Constants.fieldlabel, Constants.companyid, Constants.moduleid));
                                    if (!StringUtil.isNullOrEmpty(fieldLabel)) {
                                        invFieldParamsMap.put(Constants.filter_values, Arrays.asList(fieldLabel, companyid, moduleid));
                                    }
                                    KwlReturnObject fieldIdparam = accAccountDAOobj.getFieldParamsIds(invFieldParamsMap);
                                    if (fieldIdparam.getEntityList() != null && !fieldIdparam.getEntityList().isEmpty() && fieldIdparam.getEntityList().get(0) != null) {
                                        fieldId = (String) fieldIdparam.getEntityList().get(0);
                                        jSONObject.put("fieldid", fieldId);
                                        jSONObject.put("fieldname", tmpcontyp.getFieldname());
                                        jSONObject.put("fieldlabel", tmpcontyp.getFieldlabel());
                                    }
                                }
                            }//end of tmpcontyp
                        }

                        String coldata = varEntry.getValue() != null ? varEntry.getValue().toString() : "";
                        String colValue = "";
                        String colDescription = "";
                        if (customFieldMap.containsKey(varEntry.getKey()) && coldata != null) {
                            try {
                                String[] valueData = coldata.split(",");
                                for (String value : valueData) {
                                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), value);
                                    FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                    if (fieldComboData != null) {
                                        String fieldValue = fieldComboData.getValue();
                                        if (paramsobj.optBoolean(Constants.isdefaultHeaderMap)) {
                                            fieldValue = fieldManagerDAOobj.getIdsUsingParamsValue(fieldId, fieldValue);
                                        }
                                        colValue += fieldComboData.getValue() != null ? fieldValue + "," : ",";
                                    }
                                }
                                if (colValue.length() > 1) {
                                    colValue = colValue.substring(0, colValue.length() - 1);
                                }
                                jSONObject.put("fieldData", colValue);
                            } catch (Exception ex) {
                                jSONObject.put("fieldData", coldata);
                            }
                        } else if (customDateFieldMap.containsKey(varEntry.getKey())) {
                            if (isDeployed) {

                                jSONObject.put("fieldData", coldata);
                            } else {
                                Date customDate;
                                try {
                                    customDate = df.parse(coldata);
                                    coldata = Long.toString(customDate.getTime());
                                } catch (ParseException ex) {
                                    Logger.getLogger(MasterServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                jSONObject.put("fieldData", coldata);

                            }

                        } else {
                            jSONObject.put("fieldData", coldata != null ? coldata : "");
                        }
                        jSONArray.put(jSONObject);
                    }
                }
            }
            obj.put("customdata", jSONArray);
            //Below code for customer default address sending

            JSONArray billingAddressArray = new JSONArray();
            JSONArray shippingAddressArray = new JSONArray();
            HashMap<String, Object> addressParams = new HashMap<>();
            addressParams.put(Constants.companyKey, companyid);
            addressParams.put(Constants.customerid, customer.getID());
            if (paramsobj.optBoolean("isDefaultAddress", false) == true) {
                addressParams.put("isDefaultAddress", true);
            }

            KwlReturnObject returnObject = accountingHandlerDAOobj.getCustomerAddressDetails(addressParams);
            List<CustomerAddressDetails> customerAddressDetails = returnObject.getEntityList();
            for (CustomerAddressDetails cad : customerAddressDetails) {
                if (cad != null) {
                    if (cad.isIsBillingAddress()) {
                        JSONObject billingAddrObj = AccountingManager.getAddressJsonObject(cad);
                        billingAddressArray.put(billingAddrObj);
                    } else {
                        JSONObject shippingAddrObj = AccountingManager.getAddressJsonObject(cad);
                        shippingAddressArray.put(shippingAddrObj);
                    }
                }
            }
            obj.put("billingAddress", billingAddressArray);
            obj.put("shippingAddress", shippingAddressArray);
            obj.put("salespersonValue", "");
            obj.put("salesPersonValue", "");
            obj.put("salesperson", "");
            obj.put("salesPerson", "");
            obj.put("defaultsalesperson", "");
            obj.put("defaultsalespersonValue", "");

            String[] multisalesperson = accCustomerMainAccountingService.getMultiSalesPersonIDs(customer.getID());//fetching masteritem mapped to that customer.
            obj.put("salesperson", multisalesperson[0]);
            obj.put("salesPerson", multisalesperson[0]);
            obj.put("salespersonValue", multisalesperson[1]);
            obj.put("salesPersonValue", multisalesperson[1]); //ERP-19693
            obj.put("defaultsalesperson", customer.getMappingSalesPerson() != null ? customer.getMappingSalesPerson().getID() : "");
            obj.put("defaultsalespersonValue", customer.getMappingSalesPerson() != null ? customer.getMappingSalesPerson().getValue() : "");
            obj.put("pricingbandid", customer.getPricingBandMaster() == null ? "" : customer.getPricingBandMaster().getID());
            obj.put("pricingbandname", customer.getPricingBandMaster() == null ? "" : customer.getPricingBandMaster().getName());

            if (!StringUtil.isNullOrEmpty(customer.getTaxid()) && paramsobj.optBoolean(Constants.isdefaultHeaderMap)) {
                obj.put("taxid", customer.getTaxid());
                double rowTaxPercent = 0.0;
                KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), customer.getTaxid());
                Tax rowtax = (Tax) txresult.getEntityList().get(0);
                if (rowtax != null) {

                    KwlReturnObject perresult = accTaxObj.getTaxPercent((String) requestParams.get("companyid"), customer.getCreatedOn(), rowtax.getID());
                    rowTaxPercent = (Double) perresult.getEntityList().get(0);
                    obj.put("taxpercent", rowTaxPercent);
                    obj.put("taxidValue", rowtax.getName());
                }
            } else {
                obj.put("taxid", "");
                obj.put("taxidValue", "");
            }
            jArr.put(obj);
        }
        jobj.put(Constants.RES_TOTALCOUNT, count);
        jobj.put(Constants.RES_data, jArr);
        return jobj;
    }

    public JSONObject getCustomersWithPart(String companyid, String partno, JSONObject obj) throws ServiceException, JSONException {

        if (StringUtil.isNullOrEmpty(partno)) {
            return obj;
        } else {
            partno = "%" + partno + "%";
        }
        KwlReturnObject custResult = accCustomerDAOobj.getCustomerWithPartNumber(partno, companyid);
        Iterator itr = custResult.getEntityList().iterator();
        JSONArray jArr = new JSONArray();
        ArrayList<String> listA = new ArrayList<>();
        while (itr.hasNext()) {
            DeliveryOrderDetail doDetail = (DeliveryOrderDetail) itr.next();
            JSONObject jobj = new JSONObject();
            String id = doDetail.getDeliveryOrder().getCustomer().getAccount().getCrmaccountid();
            if (!listA.contains(id)) {
                listA.add(id);
                jobj.put("name", doDetail.getDeliveryOrder().getCustomer().getName());
                jobj.put("id", id);
                jArr.put(jobj);
            }
        }
        obj.put(Constants.RES_data, jArr);
        return obj;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getAsset(JSONObject jobj) throws ServiceException, JSONException {
        JSONObject result = new JSONObject();
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        if (jobj.has("assetids")) {
            result = getAssetInformation(jobj);
        } else {
            result = getAssetDetails(jobj);
        }
        return result;
    }

    private JSONObject getAssetDetails(JSONObject jobj) throws ServiceException, JSONException {
        JSONObject result = new JSONObject();
        JSONArray jArr = new JSONArray();
        JSONObject obj1;
        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put(Constants.companyKey, jobj.getString(Constants.companyKey));
        requestParams.put("isFixedAsset", true);
        KwlReturnObject productResult = accProductObj.getProducts(requestParams);
        List productList = productResult.getEntityList();
        Iterator itr1 = productList.iterator();
        JSONArray jArr1;
        while (itr1.hasNext()) {
            Object[] o = (Object[]) itr1.next();
//                if(o instanceof Product) {
            Product p = (Product) o[0];
            obj1 = new JSONObject();
            obj1.put("assetgroupname", p.getName());
            obj1.put("assetgroupid", p.getID());

            HashMap<String, Object> assetParams = new HashMap<>();
            assetParams.put("companyId", jobj.getString(Constants.companyKey));
            assetParams.put("invrecord", true);
            assetParams.put("productId", p.getID());
            KwlReturnObject assetDetailsResult = accProductObj.getAssetDetails(assetParams);
            List assetList = assetDetailsResult.getEntityList();
            Iterator itr = assetList.iterator();
            JSONObject obj;
            jArr1 = new JSONArray();
            while (itr.hasNext()) {
                Object innero = itr.next();
                if (innero instanceof AssetDetails) {
                    AssetDetails ad = (AssetDetails) innero;
                    obj = new JSONObject();
                    obj.put("assetid", ad.getId());
                    obj.put("assetname", ad.getAssetId());
                    obj.put("assetquantity", 1);
                    String batchId = ad.getBatch() != null ? ad.getBatch().getId() : "";
                    if (p.isIsSerialForProduct()) {
                        ArrayList param3 = new ArrayList();
                        param3.add(jobj.getString(Constants.companyKey));
                        param3.add(batchId);
                        /*
                         * String query1 = "from BatchSerial bs Where
                         * bs.company.companyID=? and bs.batch.id=?"; // get
                         * serial no's for asset id's List list1 =
                         * HibernateUtil.executeQuery(session, query1,
                         * param3.toArray()); Iterator iter = list1.iterator();
                         *
                         * Iterator iter = batchserialdetails.iterator();
                         */
                        List list1 = new ArrayList();
                        if (!p.isIsBatchForProduct() && !p.isIslocationforproduct() && !p.isIswarehouseforproduct() && p.isIsSerialForProduct()) {
//                            list1 = getOnlySerialDetails(ad.getId(), session);
                            list1 = accCommonTablesDAO.getOnlySerialDetailsForRemoteAPI(ad.getId());
                        } else {
//                            list1 = getBatchSerialDetails(ad.getId(), !p.isIsSerialForProduct(), session);
                            list1 = accCommonTablesDAO.getBatchSerialDetailsForRemoteAPI(ad.getId(), !p.isIsSerialForProduct());
                        }

                        Iterator iter = list1.iterator();
                        while (iter.hasNext()) {
                            Object[] objArr = (Object[]) iter.next();
                            obj.put("assetserialnoid", objArr[7] != null ? (String) objArr[7] : "");
                            obj.put("assetserialno", objArr[8] != null ? (String) objArr[8] : "");
                        }
                        /*
                         * while (iter.hasNext()) { BatchSerial batchSerial =
                         * (BatchSerial) iter.next(); obj.put("assetserialnoid",
                         * batchSerial.getId()); obj.put("assetserialno",
                         * batchSerial.getName()); }
                         */
                        if (list1.isEmpty()) {
                            obj.put("assetserialnoid", "");
                            obj.put("assetserialno", "N/A");
                        }

                    } else {
                        obj.put("assetserialnoid", "");
                        obj.put("assetserialno", "N/A");
                    }
                    jArr1.put(obj);
//                }
                }
                obj1.put("assetgroupmembers", jArr1);

                jArr.put(obj1);
            }
        }
        result.put(Constants.RES_TOTALCOUNT, jArr.length());
        result.put(Constants.RES_data, jArr);
        result.put(Constants.RES_success, true);
        return result;
    }

    private JSONObject getAssetInformation(JSONObject jobject) throws ServiceException, JSONException {
        JSONObject result = new JSONObject();
        JSONArray jArr = new JSONArray();;
        String companyID = jobject.getString(Constants.companyKey);
        JSONArray assetIds = jobject.getJSONArray("assetids");
        JSONObject jobj;

        KwlReturnObject assetList = accProductObj.getAssetDetailsbyAssetIds(companyID, assetIds);
        if (assetList.getEntityList() != null && assetList.getEntityList().size() > 0) {
            Iterator it = assetList.getEntityList().iterator();
            while (it.hasNext()) {
                AssetDetails ad = (AssetDetails) it.next();
                Product p = ad.getProduct();
                jobj = new JSONObject();
                jobj.put("assetid", ad.getId());
                jobj.put("assetname", ad.getAssetId());
                jobj.put("assetquantity", 1);
                jobj.put("assetgroupname", ad.getProduct().getName());
                jobj.put("assetgroupid", ad.getProduct().getID());
                String batchId = ad.getBatch() != null ? ad.getBatch().getId() : "";
                if (!StringUtil.isNullOrEmpty(batchId)) {
                    ArrayList param3 = new ArrayList();
                    param3.add(companyID);
                    param3.add(batchId);
                    List list1 = new ArrayList();
                    if (!p.isIsBatchForProduct() && !p.isIslocationforproduct() && !p.isIswarehouseforproduct() && p.isIsSerialForProduct()) {
//                                list1 = getOnlySerialDetails(ad.getId(), session);
                        list1 = accCommonTablesDAO.getOnlySerialDetailsForRemoteAPI(ad.getId());
                    } else {
//                                list1 = getBatchSerialDetails(ad.getId(), !p.isIsSerialForProduct(), session);
                        list1 = accCommonTablesDAO.getBatchSerialDetailsForRemoteAPI(ad.getId(), !p.isIsSerialForProduct());
                    }

                    Iterator iter = list1.iterator();
                    while (iter.hasNext()) {
                        Object[] objArr = (Object[]) iter.next();
                        jobj.put("assetserialnoid", objArr[7] != null ? (String) objArr[7] : "");
                        jobj.put("assetserialno", objArr[8] != null ? (String) objArr[8] : "");
                    }

                    if (list1.isEmpty()) {
                        jobj.put("assetserialnoid", "");
                        jobj.put("assetserialno", "N/A");
                    }

                } else {
                    jobj.put("assetserialnoid", "");
                    jobj.put("assetserialno", "N/A");
                }
                jArr.put(jobj);

            }
        }
        result.put(Constants.RES_TOTALCOUNT, jArr.length());
        result.put(Constants.RES_data, jArr);
        result.put(Constants.RES_success, true);
        return result;
    }

    @Override
    public JSONObject getTax(JSONObject jobj) throws ServiceException, JSONException, UnsupportedEncodingException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        JSONObject result = new JSONObject();
        String companyID = jobj.getString(Constants.companyKey);
        JSONObject userData = new JSONObject();
        userData.put("iscommit", true);
        userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
        boolean isSales=false;
        boolean isCRM = false;
        boolean getAllTaxFlag=true;
        
        if (jobj.has("isSalesFlag")) {
            isSales = jobj.optBoolean("isSalesFlag", false);
            getAllTaxFlag = false;
        }
        if (jobj.has("isCRM") && jobj.getString("isCRM") != null) {
            isCRM = jobj.optBoolean("isCRM");
        }
        
        JSONArray tjobj = new JSONArray();
        KwlReturnObject taxResult = accTaxObj.getAllTaxOfCompany(companyID);
        List<Tax> list = taxResult.getEntityList();
        for (Tax tax : list) {
            Map<String, Object> filterParams = new HashMap<String, Object>();
            filterParams.put(TAXID, tax.getID());
            KwlReturnObject taxListResult = accTaxObj.getTaxList(filterParams);
            if (taxListResult.getEntityList() != null && taxListResult.getEntityList().size() > 0) {
                TaxList taxlist = (TaxList) taxListResult.getEntityList().get(0);
                if (taxlist != null) {
                        JSONObject jobj1 = new JSONObject();
                        jobj1.put("applydate", taxlist.getApplyDate());
                        jobj1.put("percent", taxlist.getPercent());
                        jobj1.put(Constants.companyKey, tax.getCompany().getCompanyID());
                        jobj1.put("taxname", tax.getName());
                        jobj1.put("taxcode", tax.getTaxCode());
                        jobj1.put("taxid", tax.getID());
                        jobj1.put("taxtype", tax.getTaxtype());
                        jobj1.put("isSalesFlag", tax.getTaxtype() == 2 ? true : false);
                        //Below if check is added to send termid mapped to particulat tax - SDP-9558
                        if (isCRM) {
                            List l = accTaxObj.getTerms(tax.getID());
                            String termid = "";
                            String termname = "";
                            Iterator itr = l.iterator();
                            while (itr.hasNext()) {
                                InvoiceTermsSales invoiceTermsSales = (InvoiceTermsSales) kwlCommonTablesDAOObj.getClassObject(InvoiceTermsSales.class.getName(), itr.next().toString());
                                //   InvoiceTermsSales invoiceTermsSales=(InvoiceTermsSales)itr.next();
                                if (invoiceTermsSales != null) {
                                    termid += invoiceTermsSales.getId() + ",";
                                    termname += invoiceTermsSales.getTerm() + ",";
                                }
                            }
                            if (!StringUtil.isNullOrEmpty(termid)) {
                                termid = termid.substring(0, termid.length() - 1);
                            }
                            if (!StringUtil.isNullOrEmpty(termname)) {
                                termname = termname.substring(0, termname.length() - 1);
                            }
                            jobj1.put("termid", termid);
                            jobj1.put("termname", termname);
                        }
                        if (!getAllTaxFlag) {//if user want either Sales or Purchase Tax
                            if (isSales &&  (tax.getTaxtype()==2 || tax.getTaxtype()==0 )) {//foR CRM sales and both taxed are allow to send
                                tjobj.put(jobj1);
                            } else if(!isSales && tax.getTaxtype()==1 ){
                                tjobj.put(jobj1);
                            }
                        } else {//put all the tax in jsonobject
                            tjobj.put(jobj1);
                        }
                    }
                }
            }
        JSONArray pagedJson = tjobj;
        String start = jobj.optString(Constants.start, null);
        String limit = jobj.optString(Constants.limit, null);
        if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
            pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
        }
        userData.put(Constants.RES_TOTALCOUNT, pagedJson.length());
        
        if ((jobj.optBoolean(Constants.isdefaultHeaderMap) == true) || isCRM) {
            userData.put(Constants.RES_data,pagedJson);
        } else {
            userData.put(Constants.RES_data, URLEncoder.encode(pagedJson.toString(), Constants.DECODE_ENCODE_FORMAT));
        }
        
        userData.put(Constants.RES_success, true);
        result = userData;
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getCurrencyExchange(JSONObject jobj) throws JSONException, ParseException, SessionExpiredException, ServiceException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        JSONObject jobjData = new JSONObject();
        boolean issuccess = false;
        Map<String, Object> requestParams = new HashMap<String, Object>();

        String erpcompanyid = jobj.getString(Constants.companyKey);
        KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), erpcompanyid);
        Company companyObj = (Company) companyResult.getEntityList().get(0);
        DateFormat datef = authHandler.getDateOnlyFormat();
        String erpcurrency = companyObj.getCurrency().getCurrencyID();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if(!jobj.has("fromcurrencyid") && jobj.has("currencyid")){
            requestParams.put("fromcurrencyid", jobj.getString("currencyid"));
        }
        if ((jobj.getString("fromcurrencyid")).equalsIgnoreCase(erpcurrency)) {
            requestParams.put(Constants.companyKey, erpcompanyid);
            String transactionDateinString = jobj.optString("transacationdateStr", "");
            if (!StringUtil.isNullOrEmpty(transactionDateinString)) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(sdf.parse(transactionDateinString));
                String tdate = datef.format(cal.getTime());
                try {
                    Date tradate = datef.parse(tdate);
                    requestParams.put("transacationdate", tradate);
                } catch (ParseException ex) {
                    requestParams.put("transacationdate", cal.getTime());
                }
            }
            KwlReturnObject result = accCurrencyDAOobj.getCurrencyExchange(requestParams);
            List list = result.getEntityList();
            JSONArray jArr = getCurrencyExchangeJson(jobj, list, requestParams);
            jobjData.put(Constants.RES_data, jArr);
            jobjData.put(Constants.RES_TOTALCOUNT, jArr.length());
            issuccess = true;
            jobjData.put(Constants.RES_success, issuccess);
        } else {
            jobjData.put(Constants.RES_success, issuccess);
            jobjData.put(Constants.RES_MESSAGE, "Eclaim Basecurrency is different than ERP Basecurrency. Please check it !!");
        }
        return jobjData;
    }

    private JSONArray getCurrencyExchangeJson(JSONObject jobj, List<ExchangeRate> list, Map<String, Object> requestParams) throws SessionExpiredException, ServiceException, java.text.ParseException, com.krawler.utils.json.base.JSONException {
        JSONArray jArr = new JSONArray();

        Map<String, Object> mapParams = new HashMap<>();
        Date transactiondate = null;
        mapParams.put(Constants.globalCurrencyKey, jobj.getString("fromcurrencyid"));
        mapParams.put(Constants.companyKey, jobj.getString(Constants.companyKey));

        if ((requestParams.get("transacationdate")) != null) {
            transactiondate = (Date) requestParams.get("transacationdate");
        }

        JSONObject obj = new JSONObject();
        if (list != null && !list.isEmpty()) {
            for (ExchangeRate ER : list) {
                String erID = ER.getID();
                KwlReturnObject erdresult = accCurrencyDAOobj.getExcDetailID(mapParams, null, transactiondate, erID);
                ExchangeRateDetails erd = (ExchangeRateDetails) erdresult.getEntityList().get(0);
                obj = new JSONObject();
                if (erd != null) {
                    obj.put("exchangerate", erd.getExchangeRate());
                    obj.put("newexchangerate", erd.getExchangeRate());
                    obj.put("fromcurrency", erd.getExchangeratelink().getFromCurrency().getName());
                    obj.put("currencycode", erd.getExchangeratelink().getToCurrency().getCurrencyCode());
                    obj.put("tocurrency", erd.getExchangeratelink().getToCurrency().getName());
                    obj.put("tocurrencyid", erd.getExchangeratelink().getToCurrency().getCurrencyID());
                    obj.put("fromcurrencyid", erd.getExchangeratelink().getFromCurrency().getCurrencyID());
                    obj.put(Constants.companyKey, erd.getCompany().getCompanyID());
                    jArr.put(obj);
                }
            }
        }

        return jArr;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject saveProjectDetails(JSONObject jobj) throws ServiceException, JSONException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        JSONObject jsonResult = new JSONObject();
        KwlReturnObject result = null;

        if (!jobj.has(Constants.companyKey) || !jobj.has("projects")) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }

        String companyID = jobj.getString(Constants.companyKey);
        JSONArray jArray = jobj.getJSONArray("projects");

        //Fetched Project field params     
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("filter_names", Arrays.asList(Constants.companyKey, "isforproject"));
        requestParams.put("filter_values", Arrays.asList(companyID, 1));

        result = fieldManagerDAOobj.getFieldParams(requestParams);

        List list = result.getEntityList();
        Iterator itr = list.iterator();
        //Loop on fields params having isProject true
        while (itr.hasNext()) {
            FieldParams fieldParams = (FieldParams) itr.next();
            //Loop to set combo values 
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jobjData = jArray.getJSONObject(i);

                if (!jobjData.has("projectid") || !jobjData.has("projectname")) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }
                String projectid = (String) jobjData.get("projectid");
                String projectname = (String) jobjData.get("projectname");

                //Check for duplicate. If not present then insert.
                ArrayList filter_params = new ArrayList();
                filter_params.add(projectid);
                filter_params.add(fieldParams.getId());
                List listItems = accMasterItemsDAOobj.getMasterItemsForRemoteAPI(filter_params, false);

                Iterator itrItems = listItems.iterator();
                HashMap requestParam = new HashMap();

                requestParam.put("name", projectname);
                requestParam.put("groupid", fieldParams.getId());
                requestParam.put("projectid", projectid);
                if (itrItems.hasNext()) {
                    FieldComboData item = (FieldComboData) itrItems.next();
                    requestParam.put("id", item.getId());
                }
                boolean isEdit = false;
                if (jobjData.has("isEdit")) {
                    isEdit = Boolean.parseBoolean(jobjData.get("isEdit").toString());
                }
                result = accMasterItemsDAOobj.addUpdateMasterCustomItem(requestParam, isEdit);
            }
        }
        jsonResult.put(Constants.RES_success, true);
        if (list.size() > 0) {
            Locale locale = jobj.has("language") ? Locale.forLanguageTag(jobj.getString("language")) : Locale.forLanguageTag("en");
            String msg = messageSource.getMessage("acc.field.projectDetailsSavedSuccessfully", null, locale);
            jsonResult.put(Constants.RES_MESSAGE, msg);
        }
        jsonResult.put(Constants.RES_TOTALCOUNT, list.size());
        return jsonResult;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject deleteProjectDetails(JSONObject jobj) throws ServiceException, JSONException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        JSONObject jsonResult = new JSONObject();
        KwlReturnObject result = null;
        boolean isUsed = false;
        jsonResult.put(Constants.RES_success, true);
        if (!jobj.has(Constants.companyKey) || !jobj.has("projects")) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        String companyID = jobj.getString(Constants.companyKey);
        JSONArray jArray = jobj.getJSONArray("projects");
        //Fetched Project field params     
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("filter_names", Arrays.asList(Constants.companyKey, "isforproject"));
        requestParams.put("filter_values", Arrays.asList(companyID, 1));
        result = fieldManagerDAOobj.getFieldParams(requestParams); // get custom field for module

        int deleteCount = 0;
        List list = result.getEntityList();
        Iterator itr = list.iterator();
        JSONArray array = new JSONArray();
        //Loop on fields params having isProject true
        while (itr.hasNext()) {
            FieldParams fieldParams = (FieldParams) itr.next();
            for (int i = 0; i < jArray.length(); i++) {
                JSONObject jobjData = jArray.getJSONObject(i);
                String projectid = (String) jobjData.get("projectid");
                //Check  If  present or not
                ArrayList filter_params = new ArrayList();
                filter_params.add(projectid);
                filter_params.add(fieldParams.getId());
                List listItems = accMasterItemsDAOobj.getMasterItemsForRemoteAPI(filter_params, false);
                Iterator itrItems = listItems.iterator();
                if (itrItems.hasNext()) {
                    FieldComboData item = (FieldComboData) itrItems.next();
                    isUsed = accMasterItemsDAOobj.isUsedMasterCustomItem(item.getId(), fieldParams.getId());
                    if (!isUsed) {
                        KwlReturnObject returnObj = accMasterItemsDAOobj.daleteMasterCustomItem(item.getId());
                        deleteCount++;
                    }
                }
            }
        }
        jsonResult.put("isUsed", isUsed);
        jsonResult.put(Constants.RES_TOTALCOUNT, deleteCount);
        return jsonResult;
    }
    
    @Override
    public JSONObject getUnitOfMeasure(JSONObject paramJobj) throws ServiceException, SessionExpiredException, JSONException {
        JSONObject jobj = new JSONObject();
        if (paramJobj.has(Constants.companyKey) && paramJobj.get(Constants.companyKey) != null) {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(paramJobj);
            KwlReturnObject result = accUomObj.getUnitOfMeasure(requestParams);
            List<UnitOfMeasure> list = result.getEntityList();
            JSONArray DataJArr = accUomService.getUoMJson(paramJobj, list);
            jobj.put(Constants.RES_data, DataJArr);
            jobj.put(Constants.RES_count, DataJArr.length());
            jobj.put(Constants.RES_success, true);
        } else {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        return jobj;
    }
    
    @Override //Web-Application
    public JSONObject getMasterGroups(JSONObject paramJobj) throws ServiceException, SessionExpiredException, JSONException {
        JSONObject jobj = new JSONObject();
        if (paramJobj.has(Constants.companyKey) && paramJobj.get(Constants.companyKey) != null) {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            jobj = accMasterItemsService.getMasterGroups(paramJobj);
        } else {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        return jobj;
    }
    
    @Override //Web-application
    public JSONObject getMasterItemsForCustomFoHire(JSONObject paramJobj) throws ServiceException, SessionExpiredException, JSONException {
        JSONObject jobj = new JSONObject();
        if (paramJobj.has(Constants.companyKey) && paramJobj.get(Constants.companyKey) != null) {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            jobj = accMasterItemsService.getMasterItemsForCustomFoHire(paramJobj);
        } else {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        return jobj;
    }
    
//    @Override //Web-application
//    public JSONObject getMasterItemsForCustomFoHire(JSONObject paramJobj) throws ServiceException, SessionExpiredException, JSONException {
//        JSONObject jobj = new JSONObject();
//        if (paramJobj.has(Constants.companyKey) && paramJobj.get(Constants.companyKey) != null) {
//            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
//            jobj = accMasterItemsService.getMasterItemsForCustomFoHire(paramJobj);
//        } else {
//            throw ServiceException.FAILURE("Missing required field", "e01", false);
//        }
//        return jobj;
//    }
    
    @Override
    public JSONObject getCostCenter(JSONObject paramJobj) throws ServiceException, SessionExpiredException, JSONException {
        JSONObject jobj = new JSONObject();

        if (paramJobj.has(Constants.companyKey) && paramJobj.get(Constants.companyKey) != null) {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(paramJobj);

            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            filter_names.add("company.companyID");
            filter_params.add(requestParams.get(Constants.companyKey));
            requestParams.put(Constants.filterNamesKey, filter_names);
            requestParams.put(Constants.filterParamsKey, filter_params);

            KwlReturnObject result = accCostCenterObj.getCostCenter(requestParams);
            List<CostCenter> list = result.getEntityList();
            JSONArray DataJArr = accCostCenterService.getCostCenterJson(paramJobj, list);
            jobj.put(Constants.RES_data, DataJArr);
            jobj.put(Constants.RES_count, DataJArr.length());
        } 
        jobj.put(Constants.RES_success, true);
        return jobj;
    }
    
    @Override
    public JSONObject getCostCenterFromFieldParams(JSONObject paramJobj) throws ServiceException, SessionExpiredException, JSONException {
        JSONObject jobj = new JSONObject();

        if (paramJobj.has(Constants.companyKey) && paramJobj.get(Constants.companyKey) != null) {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            Map<String, Object> requestParams = new HashMap<String,Object>();
            requestParams.put("companyid", paramJobj.get(Constants.companyKey));
            jobj = accMasterItemsService.getMasterItemsForEclaim(requestParams);
            if(jobj.has("totalCount")){
                jobj.put(Constants.RES_count, jobj.get("totalCount"));                
            }
        } 
        jobj.put(Constants.RES_success, true);
        return jobj;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject deleteCostCenter(JSONObject jobj) throws ServiceException, JSONException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        JSONObject jsonResult = new JSONObject();
        KwlReturnObject result = null;
        boolean isUsed = false;
        jsonResult.put(Constants.RES_success, true);
        if (!jobj.has(Constants.companyKey)) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        String companyID = jobj.getString(Constants.companyKey);
        HashMap<String, Object> filterRequestParams = new HashMap<>();
        ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();

        filter_names.add("isforeclaim");
        filter_params.add(1);
        filter_names.add("company.companyID");
        filter_params.add(companyID);
        filterRequestParams.put("filter_names", filter_names);
        filterRequestParams.put("filter_values", filter_params);
        result = accMasterItemsDAOobj.getFieldParams(filterRequestParams);
        List list = result.getEntityList();
        String masterID = null;
        if (!list.isEmpty()) {
            masterID = ((FieldParams) list.get(0)).getId();
        }

        JSONArray jArr = new JSONArray();
        JSONObject jobdata = new JSONObject();
        JSONObject obj = new JSONObject(jobj.getJSONObject("data").toString());
        String appuiid = StringUtil.isNullOrEmpty(obj.getString("appuiid")) ? "" : obj.getString("appuiid");

        //Check whether the record is exist or not.
        KwlReturnObject comboresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), appuiid);
        FieldComboData fieldComboData = (FieldComboData) comboresult.getEntityList().get(0);
        if (fieldComboData != null) {
            //Check whether the record is used or not
            isUsed = accMasterItemsDAOobj.isUsedMasterCustomItem(appuiid, masterID);
            if (!isUsed) {
                KwlReturnObject delresult = accMasterItemsDAOobj.daleteMasterCustomItem(appuiid);
                jobdata.put("isused", false);
                jobdata.put("isdeleted", delresult.isSuccessFlag());
                jobdata.put("msg", delresult.getMsg());
            } else {
                jobdata.put("isused", true);
                jobdata.put("isdeleted", false);
                jobdata.put("msg", "Cost Center cannot be delete. It is used in transaction.");
            }
        } else {    //Record doesn't exist
            jobdata.put("isused", false);
            jobdata.put("isdeleted", false);
            jobdata.put("msg", "Cost Center is not exist.");
        }

        jArr.put(jobdata);
        jsonResult.put("data", jArr);
        return jsonResult;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject saveTerm(JSONObject jobj) throws ServiceException, JSONException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        JSONObject job = new JSONObject();
        job.put(Constants.RES_success, false);
        JSONArray jArr = jobj.getJSONArray("termdetails");
        String companyid = jobj.getString(Constants.companyKey);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("termdetails", jArr);
        params.put(Constants.companyKey, companyid);
        job = accTermService.saveTerm(params);
        job.put(Constants.RES_success, true);
        return job;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getTerm(JSONObject paramJobj) throws ServiceException {
        JSONObject job = new JSONObject();
        try {
            String cash = "";
            if (!paramJobj.has("cash_Invoice") && !paramJobj.has(Constants.RES_CDOMAIN)) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
            String cdomain = paramJobj.getString(Constants.RES_CDOMAIN);
            paramJobj.put(Constants.RES_CDOMAIN, cdomain);
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            String start = paramJobj.optString(Constants.start, null); 
            String limit = paramJobj.optString(Constants.limit, null);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put(Constants.companyKey, paramJobj.getString(Constants.companyKey));
            cash = paramJobj.optString("cash_Invoice", null) != null ? paramJobj.getString("cash_Invoice").toString() : "false";
            requestParams.put("cash_Invoice", cash);
            KwlReturnObject result = accTermObj.getTerm(requestParams);
            List<Term> list = result.getEntityList();
            JSONArray jArr = getTermJson(list);
            JSONArray pagedJson = jArr;
            
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(jArr, Integer.parseInt(start), Integer.parseInt(limit));
            }
            job.put(Constants.RES_TOTALCOUNT, pagedJson.length());
            job.put(Constants.RES_data, pagedJson);
            job.put(Constants.RES_success, true);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getCostCenterJson : " + ex.getMessage(), ex);
        } catch (ServiceException ex) {
            throw ServiceException.FAILURE("getCostCenterJson : " + ex.getMessage(), ex);
        }
        return job;
    }

    public JSONArray getTermJson(List<Term> list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            for (Term ct:list) {
                JSONObject obj = new JSONObject();
                obj.put("termid", ct.getID());
                obj.put("termname", ct.getTermname());
                obj.put("termdays", ct.getTermdays());
                obj.put("isdefaultcreditterm", ct.isIsdefault());
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getTermJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    @Override
    public JSONArray getInvoiceTerms(JSONObject paramJobj) throws JSONException, SessionExpiredException {
        JSONArray jarr = new JSONArray();
        try {
            HashMap<String, String> termNameID = new HashMap();
            termNameID.put("Basic", "Basic");
            HashMap hashMap = new HashMap();
            hashMap.put("companyid", paramJobj.getString(Constants.companyKey));
            if (paramJobj.has("isSalesOrPurchase")&& paramJobj.get("isSalesOrPurchase") != null) {
                hashMap.put("salesOrPurchaseFlag", paramJobj.getString("isSalesOrPurchase"));
            }
            if (paramJobj.has("term")&& paramJobj.get("term") != null) {
                hashMap.put("term", paramJobj.getString("term"));
            }
            if (paramJobj.has(Constants.ss)&& paramJobj.get(Constants.ss) != null) {
                hashMap.put(Constants.ss, paramJobj.getString(Constants.ss));
            }
            
            hashMap.put("notinquery", paramJobj.optString("notinquery",null));
            KwlReturnObject result = accAccountDAOobj.getInvoiceTerms(hashMap);
            List<InvoiceTermsSales> list = result.getEntityList();
            for (InvoiceTermsSales mt : list) {
                JSONObject jsonobj = new JSONObject();
                jsonobj.put("id", mt.getId());
                jsonobj.put("term", mt.getTerm());
                termNameID.put(mt.getId(), mt.getTerm());
                jsonobj.put("glaccount", mt.getAccount().getID());
                jsonobj.put("sign", mt.getSign());
                jsonobj.put("category", mt.getCategory());
                jsonobj.put("includegst", mt.getIncludegst());
                jsonobj.put("includeprofit", mt.getIncludeprofit());
                jsonobj.put("formula", mt.getFormula());
                jsonobj.put("formulaids", mt.getFormula());
                jsonobj.put("suppressamnt", mt.getSupressamount());
                jsonobj.put("salesorpurchase", mt.isSalesOrPurchase());
                jsonobj.put("creator", mt.getCreator().getUserID());
                jarr.put(jsonobj);
            }
            if (jarr.length() > 0) {
                for (int cnt = 0; cnt < jarr.length(); cnt++) {
                    JSONObject jsonobj = jarr.getJSONObject(cnt);
                    String[] formula = jsonobj.getString("formula").split(",");
                    String formulaName = "";
                    for (int frmCnt = 0; frmCnt < formula.length; frmCnt++) {

                        formulaName = formulaName.concat(termNameID.get(formula[frmCnt])).concat(",");
                    }
                    if (!StringUtil.isNullOrEmpty(formulaName)) {
                        formulaName = formulaName.substring(0, formulaName.length() - 1);
                    }
                    jsonobj.put("formula", formulaName);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return jarr;
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getAllCurrency(JSONObject paramsobj) throws ServiceException, JSONException {
        if(   ((paramsobj.has(Constants.companyKey) && !StringUtil.isNullOrEmpty(Constants.companyKey)) ||  (paramsobj.has(Constants.RES_CDOMAIN) && !StringUtil.isNullOrEmpty(Constants.RES_CDOMAIN))) && paramsobj.optBoolean(Constants.isdefaultHeaderMap,false)==false) {
            return getCompanyCurrency(paramsobj);
        }else {
            return getAllCurrencies(paramsobj);
        }
    }
    
    private JSONObject getAllCurrencies(JSONObject paramsobj) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        HashMap<String, Object> requestParams = new HashMap<>();
        String start = paramsobj.optString(Constants.start, null);
        String limit = paramsobj.optString(Constants.limit, null);
        if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
            requestParams.put(Constants.start, start);
            requestParams.put(Constants.limit, limit);
        }

        if (paramsobj.has(Constants.ss) && !StringUtil.isNullOrEmpty((String) paramsobj.get(Constants.ss))) {
            String ss = (String) paramsobj.get(Constants.ss);
            requestParams.put(Constants.ss, ss);
        }

        KwlReturnObject currencies = kwlCommonTablesDAOObj.getAllCurrencies(requestParams);
        JSONArray currencyArr = new JSONArray();
        if (!StringUtil.isNullObject(currencies.getEntityList())) {
            for (Object obj : currencies.getEntityList()) {
                KWLCurrency kwlCurrency = (KWLCurrency) obj;
                JSONObject currencyJobj = new JSONObject();
                currencyJobj.put("currencyID", kwlCurrency.getCurrencyID());
                currencyJobj.put("currencyCode", kwlCurrency.getCurrencyCode());
                currencyJobj.put("name", kwlCurrency.getName());
                currencyArr.put(currencyJobj);
            }
        }
        jobj.put(Constants.RES_TOTALCOUNT, currencyArr.length());
        jobj.put(Constants.RES_data, currencyArr);
        jobj.put(Constants.RES_success, true);
        return jobj;
    }
    
    public JSONObject getCompanyCurrency(JSONObject paramsobj) throws ServiceException, JSONException {
        JSONObject jobjResult = new JSONObject();
        paramsobj = wsUtilService.populateAdditionalInformation(paramsobj);
        String currencyid = "";
        if(paramsobj.has(Constants.globalCurrencyKey)){
            currencyid = paramsobj.getString(Constants.globalCurrencyKey);
        } else if(paramsobj.has(Constants.currencyKey)){
            currencyid = paramsobj.getString(Constants.currencyKey);
        } else if(paramsobj.has("currencyCode")){
            currencyid = paramsobj.getString("currencyCode");
        }
        
        if(!StringUtil.isNullOrEmpty(currencyid)){
            jobjResult.put("currencyid", currencyid);
            jobjResult.put("success", true);
        }else{
            jobjResult.put("success", false);
        }
        return jobjResult;
    }

    @Override
    public JSONObject getAllCountry() throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject countries = kwlCommonTablesDAOObj.getAllCountries();
        JSONArray countryArr = new JSONArray();
        if (!StringUtil.isNullObject(countries.getEntityList())) {
            for (Object obj : countries.getEntityList()) {
                Country country = (Country) obj;
                JSONObject countryJobj = new JSONObject();
                countryJobj.put("ID", country.getID());
                countryJobj.put("countryCode", country.getCountryCode());
                countryJobj.put("countryName", country.getCountryName());
                countryArr.put(countryJobj);
            }
        }
        jobj.put(Constants.RES_TOTALCOUNT, countryArr.length());
        jobj.put(Constants.RES_data, countryArr);
        jobj.put(Constants.RES_success, true);
        return jobj;
    }

    @Override
    public JSONObject getAllTimeZone() throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject timezones = kwlCommonTablesDAOObj.getAllTimeZones();
        JSONArray tzArr = new JSONArray();
        if (!StringUtil.isNullObject(timezones.getEntityList())) {
            for (Object obj : timezones.getEntityList()) {
                KWLTimeZone kwlTimeZone = (KWLTimeZone) obj;
                JSONObject tzJobj = new JSONObject();
                tzJobj.put("timeZoneID", kwlTimeZone.getTimeZoneID());
                tzJobj.put("difference", kwlTimeZone.getDifference());
                tzJobj.put("name", kwlTimeZone.getName());
                tzJobj.put("sname", kwlTimeZone.getSname());
                tzJobj.put("tzID", kwlTimeZone.getTzID());
                tzArr.put(tzJobj);
            }
        }
        jobj.put(Constants.RES_TOTALCOUNT, tzArr.length());
        jobj.put(Constants.RES_data, tzArr);
        jobj.put(Constants.RES_success, true);
        return jobj;
    }

    @Override
    public JSONObject getAllStates(String countryid) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject states = kwlCommonTablesDAOObj.getAllStates(countryid);
        JSONArray stateArr = new JSONArray();
        if (!StringUtil.isNullObject(states.getEntityList())) {
            for (Object obj : states.getEntityList()) {
                State state = (State) obj;
                JSONObject tzJobj = new JSONObject();
                tzJobj.put("ID", state.getID());
                tzJobj.put("stateCode", state.getStateCode());
                tzJobj.put("stateName", state.getStateName());
                stateArr.put(tzJobj);
            }
        }
        jobj.put(Constants.RES_TOTALCOUNT, stateArr.length());
        jobj.put(Constants.RES_data, stateArr);
        jobj.put(Constants.RES_success, true);
        return jobj;
    }

    @Override
    public JSONObject getDateFormat() throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject dateFormats = kwlCommonTablesDAOObj.getAllDateFormats(new HashMap());
        JSONArray dfArr = new JSONArray();
        if (!StringUtil.isNullObject(dateFormats.getEntityList())) {
            for (Object obj : dateFormats.getEntityList()) {
                KWLDateFormat kwlDateFormat = (KWLDateFormat) obj;
                JSONObject tzJobj = new JSONObject();
                tzJobj.put("formatID", kwlDateFormat.getFormatID());
                tzJobj.put("javaForm", kwlDateFormat.getJavaForm());
                tzJobj.put("name", kwlDateFormat.getName());
                dfArr.put(tzJobj);
            }
        }
        jobj.put(Constants.RES_TOTALCOUNT, dfArr.length());
        jobj.put(Constants.RES_data, dfArr);
        jobj.put(Constants.RES_success, true);
        return jobj;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject saveCustomer(JSONObject paramJobj) throws JSONException, ServiceException {
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        JSONObject result = new JSONObject();
        KwlReturnObject kwlObj;
        result.put(Constants.RES_success, true);
        String message = null;
        
        Customer customer = null;
        boolean isupdatedcustomer=false;// to maintain customer is updated or not.
        if (!paramJobj.has("debitType") && !paramJobj.has("from")) {
            paramJobj.put("debitType", "true");
            paramJobj.put("from", "64");
        }
        
        if (paramJobj.has("customercodevalue") && !StringUtil.isNullOrEmpty(paramJobj.optString("customercodevalue", null))) {// called for rest service for client
            paramJobj.put("acccode", paramJobj.optString("customercodevalue"));
        }
        
        if (paramJobj.has("creditLimit") && !StringUtil.isNullOrEmpty(paramJobj.optString("creditLimit",null))) {
            paramJobj.put("creditLimit", paramJobj.optString("creditLimit"));
        }else{
            paramJobj.put("creditLimit", "0.0");
        }
        
        if (paramJobj.has("activateDeactivateFlag") && paramJobj.get("activateDeactivateFlag") instanceof Boolean) {// called for rest service for client
            paramJobj.put("activateDeactivateFlag", String.valueOf(paramJobj.optBoolean("activateDeactivateFlag")));
        }
        
        //If Account Code already exist then it will not allow user to save record. If customerid is present then it is edit case. It will not check for duplicate check
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("acccode", null)) && StringUtil.isNullOrEmpty(paramJobj.optString(Constants.customerid, null)) ) {
            KwlReturnObject customerResult = accCustomerDAOobj.getCustomerByCode(paramJobj.optString("acccode"), paramJobj.optString(Constants.companyKey), null);
            if (customerResult.getEntityList() != null && customerResult.getRecordTotalCount()!=0 ) {
                customer = (Customer) customerResult.getEntityList().get(0);
                if (!customerResult.getEntityList().isEmpty()) {
                    throw ServiceException.FAILURE("Customer Code already Exist", "erp31", false);
                }
            }
        }
        
        //Update customer
        if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.customerid, null))) {
            isupdatedcustomer=true;
            paramJobj.put("accid", paramJobj.getString(Constants.customerid));
            paramJobj.put(Constants.isEdit, "true");
            paramJobj.put(Constants.isdefaultHeaderMap, true);
            paramJobj.put(Constants.modulename, Constants.Acc_Customer_ModuleId);
            
            paramJobj = wsUtilService.populateMastersInformation(paramJobj);
            
            KwlReturnObject customerResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), paramJobj.getString(Constants.customerid));
            customer = (Customer) customerResult.getEntityList().get(0);

            if (StringUtil.isNullOrEmpty(paramJobj.optString("acccode", null))) {
                paramJobj.put("acccode", customer.getAcccode());
            }
            if (StringUtil.isNullOrEmpty(paramJobj.optString("accname", null))) {
                paramJobj.put("accname", customer.getName());
            }
            
            if (paramJobj.has("acccode") && !StringUtil.isNullOrEmpty(paramJobj.optString("acccode", null)) && StringUtil.isNullOrEmpty(paramJobj.optString(Constants.sequenceformat, null))) {// called for rest service for client
                paramJobj.put("sequenceformat", "NA");
            }
            
             if (!StringUtil.isNullOrEmpty(paramJobj.optString("customernamevalue", null))) {
                paramJobj.put("accname", paramJobj.optString("customernamevalue", null));
            }
             
            if (StringUtil.isNullOrEmpty(paramJobj.optString("termid", null)) && !StringUtil.isNullObject(customer.getCreditTerm())) {
                paramJobj.put("termid", customer.getCreditTerm().getID());
            }
            if (StringUtil.isNullOrEmpty(paramJobj.optString(Constants.currencyKey, null)) && !StringUtil.isNullObject(customer.getCurrency())) {
                paramJobj.put(Constants.currencyKey, customer.getCurrency().getCurrencyID());
            }
            if (StringUtil.isNullOrEmpty(paramJobj.optString("mappingcusaccid", null)) && !StringUtil.isNullObject(customer.getAccount())) {
                paramJobj.put("mappingcusaccid", customer.getAccount().getID());
            }
        }
        
        //Save Customer        
        if (paramJobj.has("customercodevalue") && !StringUtil.isNullOrEmpty(paramJobj.optString("customercodevalue", null)) && !isupdatedcustomer) {// called for rest service for client
            paramJobj.put("accname", paramJobj.optString("customernamevalue"));
            paramJobj.put(Constants.modulename, Constants.Acc_Customer_ModuleId);
            paramJobj.put(Constants.isdefaultHeaderMap, true);

            if (paramJobj.has("customercodevalue") && !StringUtil.isNullOrEmpty(paramJobj.optString("customercodevalue", null)) && StringUtil.isNullOrEmpty(paramJobj.optString(Constants.sequenceformat, null))) {// called for rest service for client
                paramJobj.put("sequenceformat", "NA");
            }
        }
        paramJobj = wsUtilService.populateMastersInformation(paramJobj);
//        if (!StringUtil.isNullOrEmpty(paramJobj.optString("crmaccountid", null))) {
//            paramJobj.put("crmaccountid", paramJobj.optString("crmaccountid"));
//        }
        String exceptionMsg = "";
        if (paramJobj.has(Constants.isdefaultHeaderMap) && paramJobj.optBoolean(Constants.isdefaultHeaderMap) == true) {//isdefaultHeaderMap=true for mobile apps(Android & POS)
            if (!paramJobj.has("mappingcusaccid")) {
                kwlObj = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), paramJobj.getString(Constants.companyKey));
                String defaultCustomerAccountId = null;
                if (kwlObj != null && kwlObj.getEntityList() !=  null && !kwlObj.getEntityList().isEmpty() && kwlObj.getEntityList().get(0) != null) {
                    CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) kwlObj.getEntityList().get(0);
                    defaultCustomerAccountId = companyAccountPreferences.getCustomerdefaultaccount() != null ? companyAccountPreferences.getCustomerdefaultaccount().getID() : null;
                }
                if (!StringUtil.isNullOrEmpty(defaultCustomerAccountId)) {
                    paramJobj.put("mappingcusaccid", defaultCustomerAccountId);
                } else {
                    exceptionMsg += StringUtil.isNullOrEmpty(exceptionMsg) ? "mappingcusaccid" : ", mappingcusaccid";
                }
            }
            if (!paramJobj.has("termid")) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("companyid", paramJobj.getString(Constants.companyKey));
                requestParams.put("isdefault", true);
                kwlObj = accTermObj.getTerm(requestParams);
                List<Term> termList = kwlObj.getEntityList();
                String defaultTermId = null;
                if (kwlObj != null && kwlObj.getEntityList() !=  null && !kwlObj.getEntityList().isEmpty() && kwlObj.getEntityList().get(0) != null && termList.size() == 1) {
                    Term term = termList.get(0);
                    defaultTermId = term.getID();
                }
                if (!StringUtil.isNullOrEmpty(defaultTermId)) {
                    paramJobj.put("termid", defaultTermId);
                } else {
                    exceptionMsg += StringUtil.isNullOrEmpty(exceptionMsg) ? "termid" : ", termid";
                }
            }
            if (!StringUtil.isNullOrEmpty(exceptionMsg)) {
                throw ServiceException.FAILURE("Missing mandatory field(s)", "erp45{" + exceptionMsg + "}", false);
            }
            if (paramJobj.has("termid")) {
                kwlObj = accountingHandlerDAOobj.getObject(Term.class.getName(), paramJobj.getString("termid"));
                if (kwlObj == null || kwlObj.getEntityList() ==  null || kwlObj.getEntityList().isEmpty() || kwlObj.getEntityList().get(0) == null) {
                    throw ServiceException.FAILURE("Term with 'termid' does not exist", "erp41{" + paramJobj.optString("termid") + "}", false);
                }
            }
            if (paramJobj.has("mappingcusaccid")) {
                kwlObj = accountingHandlerDAOobj.getObject(Account.class.getName(), paramJobj.getString("mappingcusaccid"));
                if (kwlObj == null || kwlObj.getEntityList() ==  null || kwlObj.getEntityList().isEmpty() || kwlObj.getEntityList().get(0) == null) {
                    throw ServiceException.FAILURE("Account with 'mappingcusaccid' does not exist", "erp42{" + paramJobj.optString("mappingcusaccid") + "}", false);
                }
            }
            String customField = paramJobj.optString(Constants.customfield, null);
            if (!StringUtil.isNullOrEmpty(customField)) {
                JSONArray customJArray = wsUtilService.createJSONForCustomField(customField, paramJobj.optString(Constants.companyKey), Constants.Acc_Customer_ModuleId);
                paramJobj.put(Constants.customfield, customJArray);
            }
            
            Iterator<String> nameItr = paramJobj.keys();
            Map<String, Object> requestMap = new HashMap<String, Object>();
            while (nameItr.hasNext()) {
                String name = nameItr.next();
                requestMap.put(name, paramJobj.getString(name));
            }
            paramJobj.put(Constants.requestMap, requestMap);
            JSONObject responseJobj = accCustomerControllerCMNServiceObj.saveCustomer(paramJobj);
            if (responseJobj.has(Constants.customerid) && !StringUtil.isNullOrEmpty(responseJobj.optString(Constants.customerid, null))) {
                paramJobj.put("accid", responseJobj.optString(Constants.customerid));
                result.put(Constants.customerid, responseJobj.optString(Constants.customerid));
                
                responseJobj = accCustomerControllerCMNServiceObj.saveCustomerAddresses(paramJobj);
                if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.customerid, null))) {
                    message = messageSource.getMessage("acc.common.erp38", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                } else {
                    message = messageSource.getMessage("acc.common.erp37", null, Locale.forLanguageTag(paramJobj.optString(Constants.language)));
                }
                result.put(Constants.RES_MESSAGE, message);
                result.put(Constants.RES_success, true);
                
                result.put("customercode", paramJobj.optString("customercodevalue"));
                
                if (!StringUtil.isNullOrEmpty(paramJobj.optString("activateDeactivateFlag", null))) {
                    paramJobj = jsonCreateActivateDeactivateCustomer(paramJobj);
                    responseJobj = accCustomerControllerCMNServiceObj.activateDeactivateCustomers(paramJobj);
                }
                
            } else {
                JSONObject response = StringUtil.getErrorResponse("acc.common.erp35", paramJobj, "Some issue occured while saving Customer.Transaction cannot be completed.", messageSource);
                throw ServiceException.FAILURE(response.optString(Constants.RES_MESSAGE), "", false);
            }

        } else if (paramJobj.has("onlycrmaccountid") && paramJobj.getBoolean("onlycrmaccountid")) {
            result = updateCRMAccountIDForCustomer(paramJobj);
        } else {
            result = saveCustomerDetails(paramJobj);
        }
        return result;
    }

    private JSONObject jsonCreateActivateDeactivateCustomer(JSONObject paramJobj) throws JSONException {
        JSONObject returnJson =paramJobj;
        if (!StringUtil.isNullOrEmpty(returnJson.optString(Constants.customerid, null))) {
            JSONObject activateDeactivateJson = new JSONObject();
            JSONArray customerActivateJSONArray = new JSONArray();
            activateDeactivateJson.put("accid", returnJson.optString(Constants.customerid));
            customerActivateJSONArray.put(activateDeactivateJson);
            paramJobj.put(Constants.data, customerActivateJSONArray.toString());
        }
        return returnJson;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject deleteCustomer(JSONObject jobj) throws JSONException, ServiceException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        String companyid = StringUtil.isNullOrEmpty(jobj.getString(Constants.companyKey)) ? "" : (String) jobj.getString(Constants.companyKey);
        HashMap<String, Object> params = new HashMap<>();
        params.put("companyid", companyid);
        JSONObject response = accCustomerControllerCMNServiceObj.deleteCustomer(params, jobj);
        return response;
    }
    
    private JSONObject saveCustomerDetails(JSONObject jobj) throws JSONException, ServiceException {
        String companyid = StringUtil.isNullOrEmpty(jobj.getString(Constants.companyKey)) ? "" : (String) jobj.getString(Constants.companyKey);
        HashMap<String, Object> params = new HashMap<>();
        params.put("companyid", companyid);
        JSONObject response = accCustomerControllerCMNServiceObj.getCustomerFromCRMAccounts(params, jobj);
        return response;
    }

    private JSONObject updateCRMAccountIDForCustomer(JSONObject paramJobj) throws ServiceException, JSONException {
        paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
        if (!paramJobj.has(Constants.RES_data) || paramJobj.getJSONArray(Constants.RES_data).length() < 1) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }

        JSONObject result = new JSONObject();
        JSONArray customerArr = paramJobj.getJSONArray(Constants.RES_data);
        if (customerArr.length() > 0) {
            for (int i = 0; i < customerArr.length(); i++) {
                JSONObject custObj = customerArr.getJSONObject(i);
                if (!custObj.has("erpcustomerid") || !custObj.has("crmaccountid")) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }
            }
        }
        if (customerArr.length() > 0) {
            for (int i = 0; i < customerArr.length(); i++) {
                JSONObject custObj = customerArr.getJSONObject(i);
                if (custObj.has("customerid")) {
                    custObj.put("erpcustomerid", custObj.getString("customerid"));
                }
                if (!custObj.has("erpcustomerid") || !custObj.has("crmaccountid")) {
                    throw ServiceException.FAILURE("Missing required field", "e01", false);
                }
                String erpcustomerid = custObj.isNull("erpcustomerid") ? "" : custObj.getString("erpcustomerid");
                String crmaccountid = custObj.isNull("crmaccountid") ? "" : custObj.getString("crmaccountid");
                KwlReturnObject customerResult = accountingHandlerDAOobj.getObject(Customer.class.getName(), erpcustomerid);
                Customer customer = (Customer) customerResult.getEntityList().get(0);

                if (customer != null) {
                    HashMap<String, Object> customrequestParams = new HashMap<>();
                    customrequestParams.put("accid", customer.getID());
                    customrequestParams.put("crmaccountid", crmaccountid);
                    accCustomerDAOobj.updateCustomer(customrequestParams);
                }
            }
        }
        result.put(Constants.RES_success, true);
        result.put(Constants.RES_MESSAGE, messageSource.getMessage("acc.details.save", null, Locale.forLanguageTag(paramJobj.getString("language"))));

        return result;
    }

    private JSONObject isCustomerAccountExists(JSONObject jobj) throws ServiceException, JSONException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        if (jobj.has("name")) {
            jobj.put("accountname", jobj.getString("name"));
        }
        if (!jobj.has("accountname") || StringUtil.isNullOrEmpty(jobj.getString("accountname"))) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        JSONObject result = new JSONObject();
        result.put("success", false);
        String companyid = jobj.isNull(Constants.companyKey) ? "" : jobj.getString(Constants.companyKey);
        String customerName = jobj.isNull("accountname") ? "" : jobj.getString("accountname");
        String crmAccountId = jobj.isNull("accountid") ? "" : jobj.getString("accountid");
        boolean isVendor = jobj.isNull("isVendor") ? false : jobj.getBoolean("isVendor");
        if (isVendor) {
            KwlReturnObject retObj = accVendorDAOObj.getVendorByName(customerName, companyid);
            if (retObj.getEntityList().isEmpty()) {
                result.put("success", true);
                result.put("duplicateAccount", false);
                result.put("msg", "Vendor with same name doesn't exist.");
            } else {
                result.put("success", true);
                result.put("duplicateAccount", true);
                result.put("msg", "Vendor with same name already exists.");
            }
            if (!result.optBoolean("duplicateAccount")) {
                retObj = accAccountDAOobj.getAllAccountsFromName(companyid, customerName);
                if (retObj.getEntityList().isEmpty()) {
                    result.put("success", true);
                    result.put("duplicateAccount", false);
                    result.put("msg", "Vendor account with same name doesn't exist.");
                } else {
                    result.put("success", true);
                    result.put("duplicateAccount", true);
                    result.put("msg", "Vendor account with same name already exists.");
                }
            }

            if (jobj.has("entryNumber") && !StringUtil.isNullOrEmpty(jobj.optString("entryNumber", ""))) {
                String entryNumber = jobj.getString("entryNumber");
                retObj = accVendorDAOObj.getVendorByCode(entryNumber, companyid);
                if (retObj.getEntityList().isEmpty()) {
                    result.put("success", true);
                    result.put("duplicateCode", false);
                    result.put("msg", "Customer with same code doesn't exist.");
                } else {
                    result.put("success", true);
                    result.put("duplicateCode", true);
                    result.put("msg", "Customer with same code already exists.");
                }
            }
        } else {
            //Check if customer with same name already exists.  ERP-28130
            
//            KwlReturnObject retObj = accCustomerDAOobj.getCustomerByName(customerName, companyid,crmAccountId);
//
//            if (retObj.getEntityList().isEmpty()) {
//                result.put("success", true);
//                result.put("duplicateAccount", false);
//                result.put("msg", "Customer with same name doesn't exist.");
//            } else {
//                result.put("success", true);
//                result.put("duplicateAccount", true);
//                result.put("msg", "Customer with same name already exists.");
//            }
//            if (!result.optBoolean("duplicateAccount")) {
//                retObj = accAccountDAOobj.getAllAccountsFromName(companyid, customerName);
//                if (retObj.getEntityList().isEmpty()) {
//                    result.put("success", true);
//                    result.put("duplicateAccount", false);
//                    result.put("msg", "Customer account with same name doesn't exist.");
//                } else {
//                    result.put("success", true);
//                    result.put("duplicateAccount", true);
//                    result.put("msg", "Customer account with same name already exists.");
//                }
//            }

            if (jobj.has("entryNumber") && !StringUtil.isNullOrEmpty(jobj.optString("entryNumber", ""))) { //this case for converting lead in CRM
                String entryNumber = jobj.getString("entryNumber");
                KwlReturnObject retObj = accCustomerDAOobj.getCustomerByCode(entryNumber, companyid, crmAccountId);
                if (retObj.getEntityList().isEmpty()) {
                    result.put("success", true);
                    result.put("duplicateCode", false);
                    result.put("msg", "Customer with same code doesn't exist.");
                } else {
                    result.put("success", true);
                    result.put("duplicateCode", true);
                    result.put("msg", "Customer with same code already exists.");
                }
            }
        }
        result.put("companyid", companyid);
        return result;
    }

    @Override
    public JSONObject saveProductReplacement(JSONObject jobject) throws JSONException, ServiceException {
        jobject = wsUtilService.populateAdditionalInformation(jobject);
        JSONObject result = new JSONObject();
        String msg = "";
        boolean isCompanyActivated = companyServiceObj.isCompanyActivated(jobject);
        if (isCompanyActivated) {
            String companyID = jobject.getString(Constants.companyKey);
            KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyID);
            Company company = (Company) companyResult.getEntityList().get(0);

            String replacementNumber = jobject.getString("replacemenetno");
            String replacementid = jobject.getString("replacementid");
            String description = StringUtil.DecodeText(jobject.optString("description"));

            // For deleting Replacement entry
            if (!StringUtil.isNullOrEmpty(replacementid)) {
                List list = accSalesOrderDAOobj.deleteProductReplacement(companyID, replacementid, replacementNumber);
                if (!list.isEmpty()) {
                    String linkedTransaction = (list.get(0) != null) ? (String) list.get(0) : "";
                    if (!StringUtil.isNullOrEmpty(linkedTransaction)) {
                        throw ServiceException.FAILURE("Replacement number " + replacementNumber + " is linked with transaction. So cannot be edited.", "erp16{" + replacementNumber + "}", false);
                    }
                }
            }

            // Check whether Replacement Number exist or not
            List ll = accSalesOrderDAOobj.getProductReplacementByReplacementNumber(replacementNumber, companyID);

            if (!ll.isEmpty()) {
                throw ServiceException.FAILURE("Repacement Number you have entered is already available in our database, please enter another number", "erp17", false);
            }

            String contractId = jobject.getString("contractid");
            KwlReturnObject contractResult = accountingHandlerDAOobj.getObject(Contract.class.getName(), contractId);
            Contract contract = (Contract) contractResult.getEntityList().get(0);

            String customertId = jobject.getString("accountid");
            ArrayList custParams = new ArrayList();
            custParams.add(companyID);
            custParams.add(customertId);
            KwlReturnObject resultcheck = accCustomerDAOobj.checkCustomerExist(customertId, companyID);
            List custList = resultcheck.getEntityList();
            Iterator custItr = custList.iterator();
            Customer customer = null;
            while (custItr.hasNext()) {
                customer = (Customer) custItr.next();
            }

            HashMap<String, Object> replacementMap = new HashMap<>();
            replacementMap.put("replacementid", replacementid);
            replacementMap.put("replacementRequestNumber", replacementNumber);
            replacementMap.put("customerId", customer.getID());
            replacementMap.put("contractId", contract.getID());
            replacementMap.put("isSalesContractReplacement", contract.isNormalContract());
            replacementMap.put("companyId", company.getCompanyID());
            replacementMap.put("description",description);

            ProductReplacement productReplacement = new ProductReplacement();

            productReplacement = accSalesOrderDAOobj.buildProductReplacement(productReplacement, replacementMap);

            JSONArray jArr = jobject.getJSONArray("productData");

            Set<ProductReplacementDetail> productReplacementDetails = new HashSet<>();

            // For Checking serial numbers available or not
            String srNumbers = "";
            int count = 0;
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject productDataObject = jArr.getJSONObject(i);
                String productId = productDataObject.getString("productid");
                JSONArray serialNumberJsonArray = productDataObject.getJSONArray("serialNoData");

                KwlReturnObject productResult = accountingHandlerDAOobj.getObject(Product.class.getName(), productId);
                Product product = (Product) productResult.getEntityList().get(0);

                if (product.isIsSerialForProduct()) {// if serial number  enable for product then bellow code will be process

                    for (int j = 0; j < serialNumberJsonArray.length(); j++) {
                        JSONObject serialNoDataObject = serialNumberJsonArray.getJSONObject(j);
                        String serialNumber = serialNoDataObject.getString("serialno");

                        NewBatchSerial batchSerial = accSalesOrderDAOobj.getBatchSerialByName(productId, serialNumber);
                        if (batchSerial == null) {
                            srNumbers += serialNumber + ", ";
                            count++;
                        }
                    }
                }
            }

            if (!StringUtil.isNullOrEmpty(srNumbers)) {
                if (count == 1) {
                    throw ServiceException.FAILURE("Serial number <b>" + srNumbers.substring(0, srNumbers.length() - 2) + "</b> is not available in our records", "erp18{" + srNumbers.substring(0, srNumbers.length() - 2) + "}", false);
                } else {
                    throw ServiceException.FAILURE("Serial number's <b>" + srNumbers.substring(0, srNumbers.length() - 2) + "</b> are not available in our records", "erp19{" + srNumbers.substring(0, srNumbers.length() - 2) + "}", false);
                }

            }
//                    }

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject productDataObject = jArr.getJSONObject(i);
                double replacementQuantity = productDataObject.getDouble("qty");
                String productId = productDataObject.getString("productid");

                KwlReturnObject prodResult = accountingHandlerDAOobj.getObject(Product.class.getName(), productId);
                Product product = (Product) prodResult.getEntityList().get(0);

                // Create Set For ProductReplacementDetail
                ProductReplacementDetail productReplacementDetail = new ProductReplacementDetail();
                productReplacementDetail.setIsAsset(product.isAsset());
                productReplacementDetail.setCompany(company);
                productReplacementDetail.setContract(contract);
                productReplacementDetail.setProduct(product);
                productReplacementDetail.setProductReplacement(productReplacement);
                productReplacementDetail.setReplacementQuantity(replacementQuantity);

                Set<ReplacementProductBatchDetailsMapping> batchDetailsMappings = new HashSet<ReplacementProductBatchDetailsMapping>();
                if (product.isIsSerialForProduct()) {// if serial number  enable for product then bellow code will be process
                    JSONArray serialNumberJsonArray = productDataObject.getJSONArray("serialNoData");
                    for (int j = 0; j < serialNumberJsonArray.length(); j++) {
                        JSONObject serialNoDataObject = serialNumberJsonArray.getJSONObject(j);
                        String serialNumber = serialNoDataObject.getString("serialno");
                        // Creating set for saving serialmapping
                        ReplacementProductBatchDetailsMapping batchDetailsMapping = new ReplacementProductBatchDetailsMapping();
                        NewBatchSerial batchSerial = accSalesOrderDAOobj.getBatchSerialByName(productId, serialNumber);
                        if (batchSerial == null) {
                            throw ServiceException.FAILURE("Serial number " + serialNumber + " is not available in our records", "erp18{" + serialNumber + "}", false);
                        }
                        batchDetailsMapping.setBatchSerial(batchSerial);
                        batchDetailsMapping.setCompany(company);
                        batchDetailsMapping.setProductReplacement(productReplacement);
                        batchDetailsMapping.setProductReplacementDetail(productReplacementDetail);
                        batchDetailsMappings.add(batchDetailsMapping);
                    }
                    productReplacementDetail.setReplacementProductBatchDetailsMappings(batchDetailsMappings);
                }
                productReplacementDetails.add(productReplacementDetail);
            }
            HashMap<String, Object> productReplacementMap = new HashMap<>();
            productReplacementMap.put("productReplacementId", productReplacement.getId());
            productReplacementMap.put("productReplacement", productReplacement);
            productReplacementMap.put("productReplacementDetails", productReplacementDetails);
            accSalesOrderDAOobj.updateProductReplacement(productReplacementMap);

            msg = "Replacement Request " + replacementNumber + " has been submitted successfully.";
            result.put(Constants.RES_MESSAGE, msg);
            result.put(Constants.RES_success, true);

        } else {
            throw ServiceException.FAILURE("Company is not active", "erp26", false);
        }

        return result;
    }

    @Override
    public JSONObject deleteProductReplacement(JSONObject jobject) throws AccountingException, ServiceException, JSONException {
        jobject = wsUtilService.populateAdditionalInformation(jobject);
        JSONObject result = new JSONObject();
        String msg = "";
        String deletedTransaction = "";

        boolean isCompanyActivated = companyServiceObj.isCompanyActivated(jobject);
        if (isCompanyActivated) {
            String companyID = jobject.getString(Constants.companyKey);
            JSONArray replacementids = jobject.getJSONArray("replacementids");
            String linkedTransaction = "";

            for (int i = 0; i < replacementids.length(); i++) {
                String replacementid = (String) replacementids.get(i);
                if (!StringUtil.isNullOrEmpty(replacementid)) {
                    KwlReturnObject obj = accountingHandlerDAOobj.getObject(ProductReplacement.class.getName(), replacementid);
                    if (obj != null && obj.getEntityList()!=null && !obj.getEntityList().isEmpty()) {                        
                        ProductReplacement prodReplace = (ProductReplacement) obj.getEntityList().get(0);
                        List list = accSalesOrderDAOobj.deleteProductReplacement(companyID, replacementid, prodReplace.getReplacementRequestNumber());

                        if (list.get(0) != null && list.get(0) != "") {
                            linkedTransaction += (String) list.get(0) + ", ";
                        }

                        if (list.get(1) != null && list.get(1) != "") {
                            deletedTransaction += (String) list.get(1) + ",";
                        }
                    } else {
                        linkedTransaction += replacementid + ", ";
                    }
                }
            }

            if (StringUtil.isNullOrEmpty(linkedTransaction)) {
                msg = "Replacement(s) has been deleted successfully";
            } else {
                if (replacementids.length() == 1) {
                    msg =  messageSource.getMessage("acc.common.erp54",null,Locale.forLanguageTag(jobject.getString("language")));
                } else {
                    msg = "Replacement(s) except " + linkedTransaction.substring(0, linkedTransaction.length() - 2) + " has been deleted successfully.";
                }
            }
            result.put(Constants.RES_success, true);
            result.put(Constants.RES_MESSAGE, msg);
            result.put("deletedTransaction", deletedTransaction);
        } else {
            throw ServiceException.FAILURE("Company is not active", "erp26", false);
        }

        return result;
    }

    @Override
    public JSONObject saveProductMaintenance(JSONObject jobject) throws ServiceException, JSONException {
        jobject = wsUtilService.populateAdditionalInformation(jobject);
        if (jobject.has("maintenanceNumber")) {
            jobject.put("maintenancenumber", jobject.get("maintenanceNumber"));
        }
        if (!jobject.has("maintenancenumber") || !jobject.has("maintainanceamt") || !jobject.has("accountid") || !jobject.has("contractid")) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        JSONObject result = new JSONObject();
        String msg = "";

        boolean isCompanyActivated = companyServiceObj.isCompanyActivated(jobject);
        if (isCompanyActivated) {
            String companyID = jobject.getString(Constants.companyKey);
            KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyID);
            Company company = (Company) companyResult.getEntityList().get(0);

            String maintenanceNumber = jobject.getString("maintenancenumber");
            String maintainanceid = jobject.getString("maintainanceid");
            double maintainanceamt = jobject.getDouble("maintainanceamt");

            if (!StringUtil.isNullOrEmpty(maintainanceid)) {
                List list = accSalesOrderDAOobj.deleteProductMaintenence(companyID, maintainanceid, maintenanceNumber);
                if (!list.isEmpty()) {
                    String linkedTransaction = (list.get(0) != null) ? (String) list.get(0) : "";

                    if (!StringUtil.isNullOrEmpty(linkedTransaction)) {
                        throw ServiceException.FAILURE("Maintenanace number " + maintenanceNumber + " is linked with transaction. So cannot be edited.", "erp20{" + maintenanceNumber + "}", false);
                    }
                }
            }

            // Check whether Replacement Number exist or not
            List ll = accSalesOrderDAOobj.getProductMaintenanceByReplacementNumber(maintenanceNumber, companyID);

            if (!ll.isEmpty()) {
                throw ServiceException.FAILURE("Maintenance Number you have entered is already available in our database, please enter another number", "erp21", false);
            }

            String contractId = jobject.getString("contractid");

            KwlReturnObject contractResult = accountingHandlerDAOobj.getObject(Contract.class.getName(), contractId);
            Contract contract = (Contract) contractResult.getEntityList().get(0);

            String customertId = jobject.getString("accountid");

            ArrayList custParams = new ArrayList();
            custParams.add(companyID);
            custParams.add(customertId);

            KwlReturnObject resultcheck = accCustomerDAOobj.checkCustomerExist(customertId, companyID);
            List custList = resultcheck.getEntityList();
            Iterator custItr = custList.iterator();
            Customer customer = null;
            while (custItr.hasNext()) {
                customer = (Customer) custItr.next();
            }

            HashMap<String, Object> replacementMap = new HashMap<String, Object>();
            replacementMap.put("maintainanceid", maintainanceid);
            replacementMap.put("maintenanceNumber", maintenanceNumber);
            replacementMap.put("maintainanceamt", maintainanceamt);
            replacementMap.put("customerId", customer.getID());
            replacementMap.put("contractId", contract.getID());
            replacementMap.put("isSalesContractMaintenance", contract.isNormalContract());
            replacementMap.put("companyId", company.getCompanyID());

            KwlReturnObject maintainanceResult = accSalesOrderDAOobj.saveProductMaintenance(replacementMap);
            Maintenance maintenance = (Maintenance) maintainanceResult.getEntityList().get(0);

            msg = "Maintenance Request " + maintenance.getMaintenanceNumber() + " has been saved successfully.";
            result.put(Constants.RES_success, true);
            result.put(Constants.RES_MESSAGE, msg);
            result.put("maintenanceid", maintainanceid);

        } else {
            throw ServiceException.FAILURE("Company is not active", "erp26", false);
        }

        return result;
    }
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject saveInventoryConsumption(JSONObject jobject) throws ServiceException, JSONException,AccountingException {
        jobject = wsUtilService.populateAdditionalInformation(jobject);
        if (!jobject.has("consumptionDetails") || !jobject.has("consumptionDetails")) {
            throw ServiceException.FAILURE("Missing required Data", "e01", false);
        }
        JSONObject result = new JSONObject();
        boolean isCompanyActivated = companyServiceObj.isCompanyActivated(jobject);
        if (isCompanyActivated) {
            String companyID = jobject.getString(Constants.companyKey);
             Map<String, Object> requestParams = new HashMap<>();
             requestParams.put("companyid",companyID);
            KwlReturnObject extracap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyID);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracap.getEntityList().get(0);
            String currencyID=extraCompanyPreferences.getCompany().getCurrency().getCurrencyID();
            KwlReturnObject  mrpcap = accountingHandlerDAOobj.getObject(MRPCompanyPreferences.class.getName(), companyID);
            MRPCompanyPreferences mRPCompanyPreferences = (MRPCompanyPreferences) mrpcap.getEntityList().get(0);
            List<StockMovement> stockMovementsList = new ArrayList<StockMovement>();
            String consumptionDetails = (String) jobject.getString("consumptionDetails");
            String taskObj = (String) jobject.optString("taskObj","");
            String productIds="";
            boolean sendForQAApproval=false;
            String inspectionAreaDetails="",inspectionformInfo="";
            JSONObject taskJobj=new JSONObject(taskObj);
            sendForQAApproval = taskJobj.optBoolean("sendForQAApproval", false);
            requestParams.put("sendForQAApproval",sendForQAApproval);
            /**
             * Task End Date from PM which are mapped at task level.
             */            
            String enddate = taskJobj.getString("enddate"); 
            /**
             * inspection form information from PM which are mapped at task level.
             */
            inspectionformInfo = taskJobj.optString("inspectionformInfo", "");
            
            /**
             * inspection area from PM which are mapped at task level.
             */
            inspectionAreaDetails = taskJobj.optString("inspectionAreaDetails", "");
            try {
                /**
                 * Default Date is current Date.
                 */
                Date taskTransactionDate = new Date();
                if (!StringUtil.isNullOrEmpty(enddate)) {
                    taskTransactionDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(enddate);
                }
                inspectionformInfo = URLDecoder.decode(inspectionformInfo, Constants.ENCODING);
                inspectionAreaDetails = URLDecoder.decode(inspectionAreaDetails,Constants.ENCODING);
                taskTransactionDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(enddate);
                requestParams.put("taskTransactionDate", taskTransactionDate);
                requestParams.put("inspectionformInfo", inspectionformInfo);
                requestParams.put("inspectionAreaDetails", inspectionAreaDetails);
            } catch (Exception ex) {
                Logger.getLogger(MasterServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            
            String interstore_loc_No = "";
            SeqFormat seqFormat = null;
            try {
                if (sendForQAApproval) { // send produced Quantity for QA approval
                    seqFormat = seqService.getDefaultSeqFormat(extraCompanyPreferences.getCompany(), ModuleConst.INTER_STORE_TRANSFER);
                    if (seqFormat != null) {
                        interstore_loc_No = seqService.getNextFormatedSeqNumber(seqFormat);
                        requestParams.put("interstoreSeqNo", interstore_loc_No);
                        requestParams.put("interstoreseqFormat", seqFormat);
                    } else {
                        throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforistnotset", null, Locale.forLanguageTag(jobject.getString(Constants.language))));
                    }
                }
            } catch (SeqFormatException ex) {
                throw new AccountingException(messageSource.getMessage("acc.companypreferences.defaultsequenceformatforistnotset", null, Locale.forLanguageTag(jobject.getString(Constants.language))));
            }
            if (!StringUtil.isNullOrEmpty("consumptionDetails")) {
                JSONArray jArr = new JSONArray(consumptionDetails);
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    if (jobj.has("orderdetailid")) {
                        String workorDetailId = (String) jobj.get("orderdetailid");
                        KwlReturnObject objResult = accountingHandlerDAOobj.getObject(WorkOrderComponentDetails.class.getName(), workorDetailId);
                        WorkOrderComponentDetails workOrderComponentDetails = (WorkOrderComponentDetails) objResult.getEntityList().get(0);
                        
                        String batchdetails = "";
                        if (jobj.has("batchdetails")) {
                             batchdetails = (String) jobj.getString("batchdetails");
                        }     
                        String producedqtydetails = "";
                        if (jobj.has("producedqtydetails")) {
                             producedqtydetails = (String) jobj.getString("producedqtydetails");
                        }
                        int ComponentType=0;
                        if (jobj.has("ComponentType") && !StringUtil.isNullOrEmpty((String) jobj.getString("ComponentType"))) {
                         ComponentType=Integer.parseInt((String) jobj.getString("ComponentType"));
                        }
                        requestParams.put("ComponentType", ComponentType);
                        Product product =workOrderComponentDetails.getProduct();
                        
                        /**
                         * currently execution sequence of product in manage quantity is wrong
                         * product get produce first then its BOM products get Consume which is wrong
                         * correct: BOM products Consume first then  Assembly product produce.
                         * once it done change following productIds code. 
                         */
                        
                        if (!StringUtil.isNullOrEmpty((String) jobj.getString("batchdetails")) || (workOrderComponentDetails.getParentProduct()==null)) {
                            String productid = product.getID();
                            productIds = (productIds.equals("") ? productid : productid + "," + productIds);
                        }
//                        if(product != null ){
//                            boolean isLocationForProduct = product.isIslocationforproduct();
//                            boolean isWarehouseForProduct = product.isIswarehouseforproduct();
//                            boolean isBatchForProduct = product.isIsBatchForProduct();
//                            boolean isSerialForProduct = product.isIsSerialForProduct();
//                            boolean isRowForProduct = product.isIsrowforproduct();
//                            boolean isRackForProduct = product.isIsrackforproduct();
//                            boolean isBinForProduct = product.isIsbinforproduct();
//                            if((isWarehouseForProduct || isLocationForProduct || isBatchForProduct || isSerialForProduct || isRowForProduct || isRackForProduct || isBinForProduct) && StringUtil.isNullOrEmpty(batchdetails)){
//                                  throw ServiceException.FAILURE("Batch serial Details are not valid for task.", "e01", false);
//                            }
//                        }
                        
//                        if (!StringUtil.isNullOrEmpty(producedqtydetails)) {
//                            JSONArray producedArr = new JSONArray(producedqtydetails);
//                            workOrderComponentDetails.setProducedQuantity(producedArr.length());
//                        }
                        
                        /**
                         * update produced quantity and actual quantity in consumption details.
                         */
                        if (!StringUtil.isNullOrEmpty(producedqtydetails) && StringUtil.isNullOrEmpty(batchdetails)) {
                            try {
                                JSONArray produceQtyDetails = new JSONArray(producedqtydetails);
                                if (produceQtyDetails != null && produceQtyDetails.length() > 0) {
                                    double producedQuantity = 0.0d;
                                    for (int producedIndex = 0; producedIndex < produceQtyDetails.length(); producedIndex++) {
                                        producedQuantity +=  ((JSONObject) produceQtyDetails.get(producedIndex)).optDouble(Constants.quantity);
                                    }
                                    jobj.put(WorkOrderComponentDetails.PARAM_ACTUAL_QUANTITY, producedQuantity);
                                    jobj.put(WorkOrderComponentDetails.PARAM_PRODUCED_QUANTITY, producedQuantity);
                                }
                            } catch (JSONException ex) {
                                System.out.println(ex);
                            }
                        }
                        
                        /*
                         *  Deleting Existing Block Quantity on cosumption and Locking Consumed Quantity
                         */
                        /**
                         * get producing inventory according to product in consumption details.
                         */
                        String inventoryid = "";
                        boolean isEditConsumption;
                        if (StringUtil.isNullOrEmpty(batchdetails) && !StringUtil.isNullOrEmpty(producedqtydetails)) {
                            inventoryid = workOrderComponentDetails.getInventoryProduced() != null ? workOrderComponentDetails.getInventoryProduced().getID() : "";
                            /**
                             * check producing edit case only when producing assembly
                             */
                            isEditConsumption = workOrderComponentDetails.getInventoryProduced() != null;
                        } else {
                            inventoryid = workOrderComponentDetails.getInventory() != null ? workOrderComponentDetails.getInventory().getID() : "";
                            /**
                             * edit case check for consumption and 0 quantity consumption
                             */
                            isEditConsumption = (workOrderComponentDetails.getInventory() != null || workOrderComponentDetails.getInventoryReturnedIn() != null || workOrderComponentDetails.getInventoryWasteIn() != null || workOrderComponentDetails.getInventoryRecycleIn() != null);
                        }
                        /**
                         * If any Inventory Entry is already made then, consider as edit case.
                         */
                        String oldProduceQtyDetail = "";
                        /**
                         * get old producedqtydetail to delete its previously created batch while second time MQ.
                         */
                        if (!StringUtil.isNullOrEmpty(inventoryid) && !(ComponentType==2 || ComponentType==3)) {
                            if(!StringUtil.isNullOrEmpty(workOrderComponentDetails.getConsumptionDetails())){
                                JSONObject jObjConsumptionDetails = new JSONObject(workOrderComponentDetails.getConsumptionDetails());
                                oldProduceQtyDetail = jObjConsumptionDetails.optString(WorkOrderComponentDetails.PRODUCE_DETAILS);
                            }
                        }
                        workOrderComponentDetails.setConsumptionDetails(jobj.toString());
                        if(mRPCompanyPreferences != null && mRPCompanyPreferences.getWoInventoryUpdateType() != 0 && !StringUtil.isNullOrEmpty(batchdetails) && !workOrderComponentDetails.isBlockQtyUsed()){
//                        if(!StringUtil.isNullOrEmpty(batchdetails)){
                            HashMap<String, Object> requestParams1 = new HashMap<>();
                            requestParams1.put("workorderdetailid", workOrderComponentDetails.getID());
                            requestParams1.put(Constants.companyKey, companyID);
                            requestParams1.put(Constants.useridKey, jobject.has(Constants.useridKey) ? jobject.get(Constants.useridKey) : "");
                            requestParams1.put("batchdetails",batchdetails);
                            requestParams1.put("productId", workOrderComponentDetails.getProduct()!= null ? workOrderComponentDetails.getProduct().getID() : "");
                            accWorkOrderServiceDAOCMNObj.saveorUpdateLockedBatchDetails(requestParams1);
                        }   
                               
                        
                        /*
                        To save rejected,wasted and recycled quantity in workordercomponentsdetails table in seperate column instead of inside json
                        */
                        double rejectedQuantity=jobj.has("rejectedQuantity") ? jobj.optDouble("rejectedQuantity",0.0d) : 0.0d;                        
                        double wastedQuantity = jobj.has("wasteQuantity") ? jobj.optDouble("wasteQuantity",0.0d) : 0.0d;                        
                        double recycledQuantity = jobj.has("recycleQuantity") ? jobj.optDouble("recycleQuantity",0.0d) : 0.0d; 
                        double returnQuantity = jobj.has("returnQuantity") ? jobj.optDouble("returnQuantity",0.0d) : 0.0d; 
                        
                        workOrderComponentDetails.setRejectedQuantity(rejectedQuantity);
                        workOrderComponentDetails.setWastedQuantity(wastedQuantity);
                        workOrderComponentDetails.setRecycledQuantity(recycledQuantity);
                        workOrderComponentDetails.setReturnQuantity(returnQuantity);
                        workOrderComponentDetails.setTaskId(taskJobj.optString("taskid",""));
                        workOrderComponentDetails.setTaskName(taskJobj.optString("taskname",""));
                        
                        accountingHandlerDAOobj.saveOrUpdateObject(workOrderComponentDetails);
                        /*
                         * Added Check: If quantity blocked is used or not
                         */
                        if (mRPCompanyPreferences != null && mRPCompanyPreferences.getWoInventoryUpdateType() == 0 ) {
                            requestParams.put("workorderdetailid", workOrderComponentDetails.getID());
                            requestParams.put(Constants.companyKey, companyID);
                            requestParams.put(Constants.useridKey, jobject.has(Constants.useridKey) ? jobject.get(Constants.useridKey) : "");
                            requestParams.put("isEditConsumption",isEditConsumption);
                            requestParams.put("productId", workOrderComponentDetails.getProduct()!= null ? workOrderComponentDetails.getProduct().getID() : "");
                            requestParams.put(Constants.oldProduceQtyDetail, oldProduceQtyDetail);
                            accWorkOrderServiceDAOCMNObj.updateTaskLevelInventory(requestParams, workOrderComponentDetails, stockMovementsList);
                            WorkOrder workOrder=workOrderComponentDetails.getWorkOrder();
//                            if (extraCompanyPreferences != null && extraCompanyPreferences.isActivateInventoryTab() && !stockMovementsList.isEmpty()) {
//                                stockMovementService.addOrUpdateBulkStockMovement(workOrder.getCompany(), workOrder.getID(), stockMovementsList);
//                            }
                        }
                    }
                }
            }
            String msg = "Inventory Consumption details has been saved successfully.";
            result.put("productIds",productIds);
            result.put(Constants.companyid,companyID);
            result.put(Constants.currencyKey,currencyID);
          
            result.put(Constants.RES_success, true);
            result.put(Constants.RES_MESSAGE, msg);
        } else {
            throw ServiceException.FAILURE("Company is not active", "erp26", false);
        }
        return result;
    }

    @Override
    public JSONObject deleteProductMaintenance(JSONObject jobject) throws ServiceException, JSONException {
        jobject = wsUtilService.populateAdditionalInformation(jobject);
        JSONObject result = new JSONObject();
        String msg = "";
        String deletedTransaction = "";
        if (!jobject.has("maintainanceids") || jobject.getJSONArray("maintainanceids").length() < 1) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        boolean isCompanyActivated = companyServiceObj.isCompanyActivated(jobject);
        if (isCompanyActivated) {
            String companyID = jobject.getString(Constants.companyKey);
            JSONArray maintainanceids = jobject.getJSONArray("maintainanceids");
            String linkedTransaction = "";

            for (int i = 0; i < maintainanceids.length(); i++) {
                String maintainanceid = (String) maintainanceids.get(i);
                if (!StringUtil.isNullOrEmpty(maintainanceid)) {
//                            Object obj = session.get(Maintenance.class, maintainanceid);
                    KwlReturnObject obj = accountingHandlerDAOobj.getObject(Maintenance.class.getName(), maintainanceid);
                    if (obj != null && obj.getEntityList() != null && !obj.getEntityList().isEmpty()) {
                        Maintenance maintenance = (Maintenance) obj.getEntityList().get(0);
                        List list = accSalesOrderDAOobj.deleteProductMaintenence(companyID, maintainanceid, maintenance.getMaintenanceNumber());
                        if (list.get(0) != null && list.get(0) != "") {
                            linkedTransaction += (String) list.get(0) + ", ";
                        }

                        if (list.get(1) != null && list.get(1) != "") {
                            deletedTransaction += (String) list.get(1) + ",";
                        }
                    } else {
                        linkedTransaction += maintainanceid + ", ";
                    }
                }
            }

            if (StringUtil.isNullOrEmpty(linkedTransaction)) {
                msg = "Maintenance(s) has been deleted successfully";
            } else {
                msg = "Maintenance(s) except " + linkedTransaction.substring(0, linkedTransaction.length() - 2) + " has been deleted successfully.";
            }
            result.put(Constants.RES_MESSAGE, msg);
            result.put(Constants.RES_success, true);
            result.put("deletedTransaction", deletedTransaction);
        } else {
            throw ServiceException.FAILURE("Company is not active", "erp26", false);
        }

        return result;
    }    
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getMasterItems(JSONObject jobj) throws ServiceException, JSONException {
        JSONObject result = new JSONObject();
        JSONArray jArr = new JSONArray();
        if (!jobj.has("companyid") || !jobj.has("groupid")) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        jArr = accMasterItemsService.getMasterItems(jobj);

        result.put(Constants.RES_data, jArr);
        result.put(Constants.RES_TOTALCOUNT, jArr.length());
        result.put(Constants.RES_success, true);
        return result;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getCustomCombodata(JSONObject jobj) throws ServiceException, JSONException {
        KwlReturnObject result = null;
        JSONObject jresult = new JSONObject();
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        JSONArray jArr = new JSONArray();
        jobj = wsUtilService.populateAdditionalInformation(jobj); 
        
        //For POS Outlet
        if (jobj.optBoolean(Constants.isForPos)) {

            HashMap<String, Object> invFieldParamsMap = new HashMap<>();

            invFieldParamsMap.put(Constants.filter_names, Arrays.asList(Constants.fieldlabel, Constants.companyid, Constants.moduleid));
            String fieldLabel = jobj.optString(Constants.fieldlabel);
            if (!StringUtil.isNullOrEmpty(fieldLabel)) {
                invFieldParamsMap.put(Constants.filter_values, Arrays.asList(fieldLabel, jobj.optString(Constants.companyKey), Constants.Acc_Invoice_ModuleId));
            }
            KwlReturnObject fieldIdparam = accAccountDAOobj.getFieldParamsIds(invFieldParamsMap);
            String fieldId = "";
            if (fieldIdparam.getEntityList() != null && !fieldIdparam.getEntityList().isEmpty() && fieldIdparam.getEntityList().get(0) != null) {
                fieldId = (String) fieldIdparam.getEntityList().get(0);
                jobj.put(Constants.Acc_fieldid, fieldId);
            }
        }

        if (!jobj.has(Constants.Acc_fieldid)) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }


        if (jobj.has(Constants.ss) && !StringUtil.isNullOrEmpty((String) jobj.get(Constants.ss))) {
            String ss = (String) jobj.get(Constants.ss);
            requestParams.put(Constants.ss, ss);
            requestParams.put("ss_names", new String[]{"value"});
        }
        if (!StringUtil.isNullOrEmpty(jobj.optString(Constants.Acc_parentid,null))) {
            String parentid = jobj.getString(Constants.Acc_parentid);
            requestParams.put(Constants.Acc_parentid, parentid);
        }
        boolean isFormPanel = false;
        if (!jobj.has("isFormPanel")) {
            isFormPanel = jobj.optBoolean("isFormPanel", false);
        }
        String fieldid = jobj.getString(Constants.Acc_fieldid);
        requestParams.put(Constants.filter_names, Arrays.asList(Constants.Acc_fieldid, Constants.Acc_deleteflag));
        requestParams.put(Constants.filter_values, Arrays.asList(fieldid, 0));
        ArrayList order_by = new ArrayList();
        ArrayList order_type = new ArrayList();
        order_by.add("itemsequence");
        order_by.add("value");
        order_type.add(" ");
        order_type.add("asc");
        requestParams.put("order_by", order_by);
        requestParams.put("order_type", order_type);
        result = accAccountDAOobj.getCustomCombodata(requestParams);
        List<Object[]> lst = result.getEntityList();
        if (isFormPanel) {
            JSONObject jobjTemp = new JSONObject();
            jobjTemp.put(Constants.Acc_id, "1234");
            jobjTemp.put(Constants.Acc_name, "None");
            jArr.put(jobjTemp);
        }
        for (int cnt = 0; cnt < lst.size(); cnt++) {
            Object[] row = lst.get(cnt);
            FieldComboData tmpcontyp = (FieldComboData) row[0];
            JSONObject jobjTemp = new JSONObject();
            jobjTemp.put(Constants.Acc_id, tmpcontyp.getId());
            jobjTemp.put(Constants.Acc_name, tmpcontyp.getValue());
            FieldComboData parentItem = (FieldComboData) row[3];
            if (parentItem != null) {
                jobjTemp.put(Constants.Acc_parentid, parentItem.getId());
                jobjTemp.put(Constants.Acc_parentname, parentItem.getValue());
            }
            jobjTemp.put(Constants.Acc_level, row[1]);
            jobjTemp.put(Constants.Acc_leaf, row[2]);
            jArr.put(jobjTemp);
        }

        jresult.put(Constants.RES_data, jArr);
        jresult.put(Constants.RES_TOTALCOUNT, jArr.length());
        jresult.put(Constants.RES_success, true);
        return jresult;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getDefaultColumns(JSONObject jobj) throws ServiceException, JSONException {
        JSONObject result = new JSONObject();
        JSONArray jArr = new JSONArray();
        
        if (!jobj.has("moduleid")) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put(Constants.moduleid, jobj.optString("moduleid"));
        jArr = accProductModuleService.getDefaultColumns(params).getJSONArray(Constants.RES_data);

        result.put(Constants.RES_data, jArr);
        result.put(Constants.RES_TOTALCOUNT, jArr.length());
        result.put(Constants.RES_success, true);
        return result;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject savePaymentMileStoneDetails(JSONObject requestJobj) throws ServiceException, JSONException {
        requestJobj = wsUtilService.populateAdditionalInformation(requestJobj);
        JSONObject jsonResult = new JSONObject();
        String msg = "";
        String feildLables = "";
        String Projectids = "";
        boolean NoProject = false; // Use to check if projectid from PM is not exitst in parent combo
        boolean NoParent = false; // Use to Check if Combo is having parent or not
        boolean flag = false;
        boolean NoParentmsg = false;
        boolean CheckForExist = true;
        KwlReturnObject result = null;

        if (!requestJobj.has(Constants.companyKey)) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }

        jsonResult.put(Constants.RES_success, true);
//        JSONObject jobj = new JSONObject(requestJobj.optString(Constants.RES_data));
        boolean isCompanyExist = wsUtilService.isCompanyExists(requestJobj);
        if (isCompanyExist) {

            String companyID = requestJobj.getString(Constants.companyKey);
            JSONArray jArray = requestJobj.getJSONArray(Constants.RES_data);

            //Fetched Project field params     
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("filter_names", Arrays.asList("companyid", "isfortask"));
            requestParams.put("filter_values", Arrays.asList(companyID, 1));
            result = fieldManagerDAOobj.getFieldParams(requestParams);

            List<FieldParams> list = result.getEntityList();
            for(FieldParams fieldParams : list) {
                if (fieldParams.getParent() == null) {  //Check if Combo doesn't having parent
                    feildLables += fieldParams.getFieldlabel() + ",";
                    jsonResult.put("Noparent", true);
                    NoParent = true;
                    continue;
                }
                //Loop to set combo values 
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject jobjData = jArray.getJSONObject(i);
                    CheckForExist = true;
                    String projectid = (String) jobjData.get("projectid");
                    String taskid = (String) jobjData.get("taskid");
                    String taskName = (String) jobjData.get("taskName");

                    //Check for duplicate. If not present then insert.
                    ArrayList filter_params = new ArrayList();

                    filter_params.add(projectid);
                    filter_params.add(fieldParams.getId());
                    filter_params.add(taskid);

                    List listItems = accMasterItemsDAOobj.getMasterItemsForRemoteAPI(filter_params, true);
                    Iterator itrItems = listItems.iterator();
                    HashMap requestParam = new HashMap();

                    requestParam.put("name", taskName);
                    requestParam.put("groupid", fieldParams.getId());
                    requestParam.put("projectid", projectid);
                    requestParam.put("taskid", taskid);
                    if (itrItems.hasNext()) {
                        FieldComboData item = (FieldComboData) itrItems.next();
                        requestParam.put("id", item.getId());
                        CheckForExist = false;
                    }
                    result = accMasterItemsDAOobj.addUpdateMasterCustomItem(requestParam, false);
                    if (CheckForExist) {
                        FieldComboData FCD = (FieldComboData) result.getEntityList().get(0);
                        HashMap<String, String> ProjectTask = new HashMap<String, String>();
                        ProjectTask.put("projectid", projectid);
                        ProjectTask.put("fieldid", fieldParams.getParentid());
                        ProjectTask.put("taskFeildComboId", FCD.getId());
                        NoProject = accMasterItemsDAOobj.SaveProjectTaskMapping(ProjectTask);
                        if (NoProject) {
                            NoParentmsg = true;
                            if (!flag) {
                                Projectids += projectid + "- Task : " + taskName + ",";
                            }
                        }
                    }
                }
                flag = true;
            }
        } else {
            jsonResult.put(Constants.RES_success, false);
        }
        if (NoParentmsg) {
            msg += "There is No Projects with id " + Projectids;
            msg = msg.substring(0, msg.length() - 1);
        }
        if (NoParent) {
            msg += " Please Set Parent For Combo " + feildLables;
        }
        if (!StringUtil.isNullOrEmpty(msg)) {
            msg = msg.substring(0, msg.length() - 1);
        }
        jsonResult.put(Constants.RES_msg, msg);
        return jsonResult;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject deleteUploadedFile(JSONObject jobj) throws ServiceException, JSONException {
//        jobj = wsUtilService.populateAdditionalInformation(jobj);
        JSONObject jsonResult = new JSONObject();
        
        String filename = jobj.optString("id");
        String uploadedFileLocation = StorageHandler.GetDocStorePath() + filename.trim();
        
        File file = new File(uploadedFileLocation);
        
        if(file.delete()){
            jsonResult.put(Constants.RES_success, true);
            jsonResult.put(Constants.RES_MESSAGE, "File deleted successfully.");
        }else{
            jsonResult.put(Constants.RES_success, false);
        }
        
        return jsonResult;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject uploadImage(InputStream uploadedInputStream, FormDataContentDisposition fileDetail) throws ServiceException, JSONException {
        JSONObject jsonResult = new JSONObject();
        String filename = fileDetail.getFileName();
        String uuid = StringUtil.generateUUID();
        String ext = "";
        if (filename.contains(".")) {
            ext = filename.substring(filename.lastIndexOf("."));
        }
        filename = uuid + ext;
        String uploadedFileLocation = StorageHandler.GetDocStorePath() + filename;
        String fileUrl = "video.jsp?id="+filename.trim();
        
        // save it
        writeToFile(uploadedInputStream, uploadedFileLocation);

        String output = "File uploaded to : " + uploadedFileLocation;
        jsonResult.put(Constants.RES_success,true);
        jsonResult.put(Constants.RES_msg,output);
        jsonResult.put("filename",filename);
        jsonResult.put("url",fileUrl);

        return jsonResult;
    }

    // save uploaded file to new location
    private void writeToFile(InputStream uploadedInputStream, String uploadedFileLocation)throws ServiceException {

        try {
            OutputStream out = new FileOutputStream(new File(uploadedFileLocation));
            int read = 0;
            byte[] bytes = new byte[1024];

            out = new FileOutputStream(new File(uploadedFileLocation));
            while ((read = uploadedInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
            out.close();
        } catch (IOException e) {
            throw ServiceException.FAILURE("Error occurred while uploading file", "e01", false);
        }

    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public JSONObject getSalesAnalysisChart(JSONObject paramsjobj) throws ServiceException, JSONException, SessionExpiredException {
        JSONObject response = new JSONObject();
        paramsjobj = wsUtilService.populateAdditionalInformation(paramsjobj);
        
        boolean isTopCustomers = paramsjobj.optBoolean("istopcustomers",false);
        boolean isTopProducts = paramsjobj.optBoolean("istopproducts",false);
        boolean isTopAgents = paramsjobj.optBoolean("istopagents",false);
        
        paramsjobj.put("isForChart",true);
        if(paramsjobj.has("limit")){
            paramsjobj.put("countNumber",paramsjobj.getInt("limit"));
            paramsjobj.remove("limit");
        }else{
            paramsjobj.put("countNumber",10);
        }

        if (isTopCustomers) {
            response = accInvoiceServiceDAO.getSalesAnalysis_TopCustomers_Report(paramsjobj);
        } else if (isTopProducts) {
            response = accInvoiceServiceDAO.getSalesAnalysis_TopProducts_Report(paramsjobj);
        } else if (isTopAgents) {
            response = accInvoiceServiceDAO.getSalesAnalysis_TopAgents_Report(paramsjobj);
        }
        response.put(Constants.RES_success, true);
        return response;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject saveCustomerCheckInOut(JSONObject jobj) throws JSONException, ServiceException, SessionExpiredException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);

        if (!jobj.has(Constants.companyKey) && !jobj.has(Constants.customerid)) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        JSONObject result = new JSONObject();
        result.put(Constants.RES_success, true);
        result = accCustomerMainAccountingService.saveCustomerCheckInOut(jobj);
        return result;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getCustomerCheckIn(JSONObject paramsjobj) throws ServiceException, JSONException, SessionExpiredException {
         paramsjobj = wsUtilService.populateAdditionalInformation(paramsjobj);

        if (!paramsjobj.has(Constants.companyKey) && !paramsjobj.has(Constants.customerid)) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        JSONObject result = new JSONObject();
        result.put(Constants.RES_success, true);
        result = accCustomerMainAccountingService.getCustomerCheckIn(paramsjobj);
        return result;
    }
    
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getSalesSummaryReport(JSONObject paramsjobj) throws ServiceException, JSONException, SessionExpiredException {
        JSONObject response = new JSONObject();
        paramsjobj = wsUtilService.populateAdditionalInformation(paramsjobj);
        //ERP-41214:Show asterisk
        paramsjobj = wsUtilService.getUserPermissionsforUnitPriceAndAmount(paramsjobj);
        paramsjobj.put("isForChart", true);
        if (paramsjobj.has("limit")) {
            paramsjobj.put("countNumber", paramsjobj.getInt("limit"));
            paramsjobj.remove("limit");
        }

        response = accInvoiceServiceDAO.getSalesAnalysis_TopCustomers_Report(paramsjobj);
        response.put(Constants.RES_success, true);
        return response;
    }

    /*
     * Below function used to move the Shared files from Accounting Specific
     * folder to Shared Folder
     */
    public void moveFilesFromAccountingToSharedLocation(JSONArray DataJArr) {
        for (int k = 0; k < DataJArr.length(); k++) {
            try {
                JSONObject job = DataJArr.getJSONObject(k);
                JSONArray jsarr = job.getJSONArray("shareddocs");
                for (int j = 0; j < jsarr.length(); j++) {
                    JSONObject jsobj = jsarr.getJSONObject(j);
                    String documentid = jsobj.getString("docid");
                    KwlReturnObject curreslt = accountingHandlerDAOobj.getObject(Docs.class.getName(), documentid);
                    Docs document = (Docs) curreslt.getEntityList().get(0);
                    String sourceFolder = StorageHandler.GetDocStorePath();
                    String targetFolder = StorageHandler.GetSharedDocStorePath();
                    File destinationFolder = new File(targetFolder);
                    if (!destinationFolder.exists()) {  //Create Target folder if it is not exist 
                        destinationFolder.mkdirs();
                    }
                    String ext = "";
                    if (document.getDocname().indexOf('.') != -1) {
                        ext = document.getDocname().substring(document.getDocname().indexOf('.'));
                    }
                    String sourcePath = sourceFolder + documentid + ext;
                    boolean check = new File(sourcePath).exists();    //Check source file is available in ERP folder or not
                    if (!check) {
                        continue;       //Skip if source file is already moved.
                    }
                    Path source = FileSystems.getDefault().getPath(sourcePath);
                    String targetPath = targetFolder + documentid + ext;
                    Path target = FileSystems.getDefault().getPath(targetPath);
                    try {
                        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);    //Available from Java 7
                    } catch (NoSuchFileException nfe) {
                        Logger.getLogger(NewCompanySetupController.class.getName()).log(Level.SEVERE, null, nfe);
                    } catch (IOException e) {
                        Logger.getLogger(NewCompanySetupController.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
            } catch (Exception e) {
                Logger.getLogger(NewCompanySetupController.class.getName()).log(Level.SEVERE, null, e);
            }
        }
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getEntityCustomData(JSONObject reqParams) throws ServiceException, JSONException {
        String companyid = reqParams.optString("companyid");
        JSONObject jobj = new JSONObject();
        try {
            /**
             * Create custom field type wise map for multi entity module
             */
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Multi_Entity_Dimension_MODULEID));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

            /**
             * Need to get all values of entity dimension
             */
            HashMap<String, Object> fieldparams = new HashMap<>();
            fieldparams.put("filedname", "Custom_" + Constants.ENTITY);
            fieldparams.put("moduleid", Constants.Acc_Multi_Entity_Dimension_MODULEID);
            fieldparams.put("companyid", companyid);
            String entityId = fieldManagerDAOobj.getFieldParamsId(fieldparams);

            JSONArray dataArr = new JSONArray();
            KwlReturnObject kwlReturnObject = accAccountDAOobj.getFieldComboDatabyFieldID(entityId, companyid);
            List<FieldComboData> fieldComboDatas = kwlReturnObject.getEntityList();
            for (FieldComboData fieldComboData : fieldComboDatas) {
                /**
                 * Iterate all values of entity and puts its respective data
                 *
                 */
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("entityvalue", fieldComboData.getValue());
                Map<String, Object> variableMap = new HashMap<String, Object>();
                KwlReturnObject curresult = accountingHandlerDAOobj.getObject(MultiEntityDimesionCustomData.class.getName(), fieldComboData.getId());
                MultiEntityDimesionCustomData dimesionCustomData = (MultiEntityDimesionCustomData) curresult.getEntityList().get(0);
                replaceFieldMap = new HashMap<String, String>();
                if (dimesionCustomData != null) {
                    AccountingManager.setCustomColumnValues(dimesionCustomData, FieldMap, replaceFieldMap, variableMap);
                    JSONObject params = new JSONObject();
                    params.put("companyid", companyid);
                    params.put("isExport", true);
                    fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jSONObject, params);
                }
                dataArr.put(jSONObject);
            }
            jobj.put("data", dataArr);
        } catch (Exception ex) {
            Logger.getLogger(MasterServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }
}
