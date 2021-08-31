/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.vendor;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.admin.VendorAddressDetails;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.fieldDataManager;

/**
 *
 * @author Atul
 */
public class accVendorControllerServiceImpl implements accVendorControllerService, MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private accVendorDAO accVendorDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private String successView;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private MessageSource messageSource;
    
    private fieldDataManager fieldDataManagercntrl;
    
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccVendorDAO(accVendorDAO accVendorDAOobj) {
        this.accVendorDAOobj = accVendorDAOobj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    @Override
    public IBGReceivingBankDetails saveIBGReceivingBankDetails(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        IBGReceivingBankDetails receivingBankDetails = null;
        try {
            HashMap<String, Object> requestMap = getIBGReceivingBankDetailsRequestParamsMap(request);

            KwlReturnObject returnObject = accVendorDAOobj.saveIBGReceivingBankDetails(requestMap);

            if (!returnObject.getEntityList().isEmpty()) {
                receivingBankDetails = (IBGReceivingBankDetails) returnObject.getEntityList().get(0);
            }

        } catch (ServiceException ex) {
            Logger.getLogger(accVendorControllerServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE("saveIBGReceivingBankDetails : " + ex.getMessage(), ex);
        }

        return receivingBankDetails;
    }

    @Override
    public HashMap<String, Object> getIBGReceivingBankDetailsRequestParamsMap(HttpServletRequest request) throws SessionExpiredException {

        HashMap<String, Object> requestParams = new HashMap<String, Object>();

        requestParams.put("receivingBankDetailId", request.getParameter("receivingBankDetailId"));

        requestParams.put("receivingBankCode", request.getParameter("receivingBankCode"));

        requestParams.put("receivingBankName", request.getParameter("receivingBankName"));

        requestParams.put("receivingBranchCode", request.getParameter("receivingBranchCode"));

        requestParams.put("receivingAccountNumber", request.getParameter("receivingAccountNumber"));

        requestParams.put("receivingAccountName", request.getParameter("receivingAccountName"));

        requestParams.put("vendorId", request.getAttribute("vendorId")!=null?(String)request.getAttribute("vendorId"): request.getParameter("vendor"));

        requestParams.put("masterItemId", request.getAttribute("masterItem") != null ? (String)request.getAttribute("masterItem") : request.getParameter("masterItem"));

        requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));

        return requestParams;
    }

    @Override
    public JSONArray getIBGReceivingBankDetailsForVendor(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONArray returnArray = new JSONArray();
        try {
            String vendorId = request.getAttribute("vendorId") != null ? (String)request.getAttribute("vendorId") :  request.getParameter("vendorId");
            String companyId = sessionHandlerImpl.getCompanyid(request);

            HashMap<String, Object> requestParams = getIBGReceivingBankDetailsRequestParamsMap(request);

            KwlReturnObject returnObject = accVendorDAOobj.getIBGReceivingBankDetails(requestParams);

            if (returnObject != null && !returnObject.getEntityList().isEmpty()) {
                returnArray = getIBGReceivingBankDetails(returnObject.getEntityList());
            }

        } catch (ServiceException ex) {
            Logger.getLogger(accVendorControllerServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE("getIBGReceivingBankDetailsForVendor : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accVendorControllerServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE("getIBGReceivingBankDetailsForVendor : " + ex.getMessage(), ex);
        }
        return returnArray;
    }

    public JSONArray getIBGReceivingBankDetails(List IBGReceivingBankDetailsList) throws JSONException {
        JSONArray returnArray = new JSONArray();
        if (!IBGReceivingBankDetailsList.isEmpty()) {
            Iterator it = IBGReceivingBankDetailsList.iterator();
            while (it.hasNext()) {
                IBGReceivingBankDetails receivingBankDetails = (IBGReceivingBankDetails) it.next();

                JSONObject jobj = new JSONObject();

                jobj.put("ibgId", receivingBankDetails.getId());
                jobj.put("receivingBankCode", receivingBankDetails.getReceivingBankCode());
                jobj.put("receivingBankName", receivingBankDetails.getReceivingBankName());
                jobj.put("receivingBranchCode", receivingBankDetails.getReceivingBranchCode());
                jobj.put("receivingAccountNumber", receivingBankDetails.getReceivingAccountNumber());
                jobj.put("receivingAccountName", receivingBankDetails.getReceivingAccountName());

                returnArray.put(jobj);
            }
        }

        return returnArray;
    }

    @Override
    public void deleteIBGReceivingBankDetails(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        try {
            HashMap<String, Object> deleteMap = getIBGReceivingBankDetailsRequestParamsMap(request);
            KwlReturnObject returnObject = accVendorDAOobj.deleteIBGReceivingBankDetails(deleteMap);
            System.out.println("Total IBGReceivingBankDetails deleted are : " + returnObject.getRecordTotalCount());
        } catch (ServiceException ex) {
            Logger.getLogger(accVendorControllerServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE("Selected IBG-Receiving Bank Details is used in transaction(s) so it cannot be delete", ex);
        }
    }

    @Override
    public IBGReceivingBankDetails saveIBGReceivingBankDetailsJSON(HashMap<String, Object> ibgMap) {
        IBGReceivingBankDetails receivingBankDetails = null;
        try {

            JSONObject ibgObj = (JSONObject) ibgMap.get("ibgDetailsJsonObj");

            String companyId = (String) ibgMap.get("companyId");

            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            String receivingBankDetailId = ibgObj.getString("receivingBankDetailId");
            String bankCode = ibgObj.getString("receivingBankCode");
            String bankName = ibgObj.getString("receivingBankName");
            String branchCode = ibgObj.getString("receivingBranchCode");
            String receivingAccNumber = ibgObj.getString("receivingAccountNumber");
            String receivingAccName = ibgObj.getString("receivingAccountName");

            dataMap.put("receivingBankDetailId", receivingBankDetailId);
            dataMap.put("receivingBankCode", bankCode);
            dataMap.put("receivingBankName", bankName);
            dataMap.put("receivingBranchCode", branchCode);
            dataMap.put("receivingAccountNumber", receivingAccNumber);
            dataMap.put("receivingAccountName", receivingAccName);

            if (ibgMap.containsKey("vendorId") && ibgMap.get("vendorId") != null) {
                String vendorId = (String) ibgMap.get("vendorId");
                dataMap.put("vendorId", vendorId);
            } else if (ibgMap.containsKey("masterItemId")) {
                String masterItemId = (String) ibgMap.get("masterItemId");
                dataMap.put("masterItemId", masterItemId);
            }

            dataMap.put("companyId", companyId);

            KwlReturnObject returnObject = accVendorDAOobj.saveIBGReceivingBankDetails(dataMap);

            if (!returnObject.getEntityList().isEmpty()) {
                receivingBankDetails = (IBGReceivingBankDetails) returnObject.getEntityList().get(0);
            }
            

        } catch (ServiceException ex) {
            Logger.getLogger(accVendorControllerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(accVendorControllerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return receivingBankDetails;
    }
    @Override
    public void deleteCIMBReceivingBankDetails(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        try {
            HashMap<String, Object> deleteMap = getCIMBReceivingBankDetailsRequestParamsMap(request);
            KwlReturnObject returnObject = accVendorDAOobj.deleteCIMBReceivingBankDetails(deleteMap);
        } catch (ServiceException ex) {
            Logger.getLogger(accVendorControllerServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE("Selected CIMB-Receiving Bank Details is used in transaction(s) so it cannot be delete", ex);
        }
    }

    @Override
    public CIMBReceivingDetails saveCIMBReceivingBankDetailsJSON(HashMap<String, Object> ibgMap) {
        CIMBReceivingDetails receivingBankDetails = null;
        try {

            JSONObject ibgObj = (JSONObject) ibgMap.get("ibgDetailsJsonObj");

            String companyId = (String) ibgMap.get("companyId");

            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            String receivingBankDetailId = ibgObj.optString("cimbReceivingBankDetailId","");
            String collectionAccName = ibgObj.getString("collectionAccName");
            String collectionAccNo = ibgObj.getString("collectionAccNo");
            String giroBICCode = ibgObj.getString("giroBICCode");
            String refNumber = ibgObj.optString("refNumber");

            dataMap.put("receivingBankDetailId", receivingBankDetailId);
            dataMap.put("collectionAccName", collectionAccName);
            dataMap.put("collectionAccNo", collectionAccNo);
            dataMap.put("giroBICCode", giroBICCode);
            dataMap.put("refNumber", refNumber);

            if (ibgMap.containsKey("vendorId") && ibgMap.get("vendorId") != null) {
                String vendorId = (String) ibgMap.get("vendorId");
                dataMap.put("vendorId", vendorId);
            } else if (ibgMap.containsKey("masterItemId")) {
                String masterItemId = (String) ibgMap.get("masterItemId");
                dataMap.put("masterItemId", masterItemId);
            }

            dataMap.put("companyId", companyId);

            KwlReturnObject returnObject = accVendorDAOobj.saveCIMBReceivingBankDetails(dataMap);

            if (!returnObject.getEntityList().isEmpty()) {
                receivingBankDetails = (CIMBReceivingDetails) returnObject.getEntityList().get(0);
            }
            
        } catch (ServiceException ex) {
            Logger.getLogger(accVendorControllerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(accVendorControllerServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return receivingBankDetails;
    }
    
    public CIMBReceivingDetails saveCIMBReceivingBankDetails(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        CIMBReceivingDetails receivingBankDetails = null;
        try {
            HashMap<String, Object> requestMap = getCIMBReceivingBankDetailsRequestParamsMap(request);

            String vendorId = requestMap.containsKey("vendorId")&&requestMap.get("vendorId")!=null?requestMap.get("vendorId").toString():"";
            if(!StringUtil.isNullOrEmpty(vendorId)){
                KwlReturnObject vendorResult = accountingHandlerDAOobj.getObject(Vendor.class.getName(),vendorId);
                Vendor vendor = (Vendor) vendorResult.getEntityList().get(0);
                vendor.setIbgActivated(true);
            }
            
            String masterItemId = (requestMap.containsKey("masterItemId")&&requestMap.get("masterItemId")!=null)?requestMap.get("masterItemId").toString():"";
            if(!StringUtil.isNullOrEmpty(masterItemId)){
                KwlReturnObject masterItemIdResult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(),masterItemId);
                MasterItem masterItem = (MasterItem) masterItemIdResult.getEntityList().get(0);
                masterItem.setIbgActivated(true);
            }
            KwlReturnObject returnObject = accVendorDAOobj.saveCIMBReceivingBankDetails(requestMap);

            if (!returnObject.getEntityList().isEmpty()) {
                receivingBankDetails = (CIMBReceivingDetails) returnObject.getEntityList().get(0);
            }

        } catch (ServiceException ex) {
            Logger.getLogger(accVendorControllerServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE("saveCIMBReceivingBankDetails : " + ex.getMessage(), ex);
        }

        return receivingBankDetails;
    }

    @Override
    public HashMap<String, Object> getCIMBReceivingBankDetailsRequestParamsMap(HttpServletRequest request) throws SessionExpiredException {

        HashMap<String, Object> requestParams = new HashMap<String, Object>();

        requestParams.put("cimbReceivingBankDetailId", request.getParameter("cimbReceivingBankDetailId"));
        
        requestParams.put("collectionAccNo", request.getParameter("collectionAccNo"));

        requestParams.put("collectionAccName", request.getParameter("collectionAccName"));

        requestParams.put("giroBICCode", request.getParameter("giroBICCode"));

        requestParams.put("refNumber", request.getParameter("refNumber"));
        
        requestParams.put("emailForGiro", request.getParameter("emailForGiro"));

        requestParams.put("vendorId", request.getAttribute("vendorId")!=null?(String)request.getAttribute("vendorId"): request.getParameter("vendor"));

        requestParams.put("masterItemId", request.getAttribute("masterItem") != null ? (String)request.getAttribute("masterItem") : request.getParameter("masterItem"));

        requestParams.put("companyId", sessionHandlerImpl.getCompanyid(request));

        return requestParams;
    }
    
    @Override
    public JSONArray getCIMBReceivingBankDetailsForVendor(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONArray returnArray = new JSONArray();
        try {
            String vendorId = request.getAttribute("vendorId") != null ? (String)request.getAttribute("vendorId") :  request.getParameter("vendorId");
            String companyId = sessionHandlerImpl.getCompanyid(request);

            HashMap<String, Object> requestParams = getCIMBReceivingBankDetailsRequestParamsMap(request);

            KwlReturnObject returnObject = accVendorDAOobj.getCIMBReceivingBankDetails(requestParams);

            if (returnObject != null && !returnObject.getEntityList().isEmpty()) {
                HashMap<String, Object> requestParamsForPayment = AccountingManager.getGlobalParams(request);
                returnArray = getCIMBReceivingBankDetails(returnObject.getEntityList(),requestParamsForPayment);
            }

        } catch (ServiceException ex) {
            Logger.getLogger(accVendorControllerServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE("getCIMBReceivingBankDetailsForVendor : " + ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accVendorControllerServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE("getCIMBReceivingBankDetailsForVendor : " + ex.getMessage(), ex);
        }
        return returnArray;
    }

    public JSONArray getCIMBReceivingBankDetails(List CIMBReceivingBankDetailsList, HashMap<String,Object> requestParamsForPayment) throws JSONException, ServiceException {
        JSONArray returnArray = new JSONArray();
        if (!CIMBReceivingBankDetailsList.isEmpty()) {
            Iterator it = CIMBReceivingBankDetailsList.iterator();
            while (it.hasNext()) {
                CIMBReceivingDetails receivingBankDetails = (CIMBReceivingDetails) it.next();

                JSONObject jobj = new JSONObject();

                jobj.put("cimbReceivingBankDetailId", receivingBankDetails.getId());
                jobj.put("collectionAccNo", receivingBankDetails.getCollectionAccountNumber());
                jobj.put("collectionAccName", receivingBankDetails.getCollectionAccountName());
                jobj.put("giroBICCode", receivingBankDetails.getGiroBICCode());
                jobj.put("refNumber", receivingBankDetails.getReferenceNumber());
                jobj.put("emailForGiro", receivingBankDetails.getEmailForGiro());
                jobj.put("bankType", 2);
                
                requestParamsForPayment.put("cimbReceivingBankDetailId", receivingBankDetails.getId());                
                KwlReturnObject result = accVendorDAOobj.getPaymentsWithCimb(requestParamsForPayment);
                List list = result.getEntityList();
                if(!list.isEmpty()){
                    jobj.put("usedInPayment", true);
                }
                returnArray.put(jobj);
            }
        }

        return returnArray;
    }

    @Override
    public JSONObject saveOCBCReceivingBankDetails(JSONObject paramsObj) throws ServiceException {
        JSONObject jSONObject = new JSONObject();
        try {
            KwlReturnObject result = accVendorDAOobj.saveOCBCReceivingBankDetails(paramsObj);
            jSONObject.put(Constants.RES_msg, result.getMsg());
            jSONObject.put(Constants.RES_success, result.isSuccessFlag());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorControllerServiceImpl.saveOCBCReceivingBankDetails:" + ex.getMessage(), ex);
        }
        return jSONObject;
    }

    @Override
    public JSONObject getOCBCReceivingBankDetails(JSONObject paramsObj) throws ServiceException {
        JSONObject jSONObject = new JSONObject();
        try {
            KwlReturnObject result = accVendorDAOobj.getOCBCReceivingBankDetails(paramsObj);
            List<OCBCReceivingDetails> list = result.getEntityList();
            JSONArray dataArr = new JSONArray();
            for (OCBCReceivingDetails receivingDetails : list) {
                JSONObject jObj = new JSONObject();
                jObj.put(Constants.OCBC_IBGDetailId, receivingDetails.getId());
                jObj.put(Constants.OCBC_BankCode, receivingDetails.getBankCode());
                jObj.put(Constants.OCBC_VendorAccountNumber, receivingDetails.getAccountNumber());
                jObj.put(Constants.OCBC_UltimateCreditorName, receivingDetails.getUltimateCreditorName());
                jObj.put(Constants.OCBC_UltimateDebtorName, receivingDetails.getUltimateDebtorName());
                jObj.put(Constants.OCBC_SendRemittanceAdviceVia, receivingDetails.getRemittanceAdviceVia());
                jObj.put(Constants.OCBC_RemittanceAdviceSendDetails, receivingDetails.getRemittanceAdviceSendDetails());
                dataArr.put(jObj);
            }
            JSONArray pagedJson = dataArr;
            String start = paramsObj.optString(Constants.start);
            String limit = paramsObj.optString(Constants.limit);
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            jSONObject.put(Constants.RES_data, pagedJson);
            jSONObject.put(Constants.RES_count, dataArr.length());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorControllerServiceImpl.getOCBCReceivingBankDetailsForVendor:" + ex.getMessage(), ex);
        }
        return jSONObject;
    }
    
    @Override
    public JSONObject deleteOCBCReceivingBankDetails(JSONObject paramsObj) throws ServiceException {
        JSONObject jSONObject = new JSONObject();
        try {
            KwlReturnObject result = accVendorDAOobj.deleteOCBCReceivingBankDetails(paramsObj);
            jSONObject.put(Constants.RES_msg, result.getMsg());
            jSONObject.put(Constants.RES_success, result.isSuccessFlag());
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorControllerServiceImpl.getOCBCReceivingBankDetailsForVendor:" + ex.getMessage(), ex);
        }
        return jSONObject;
    }
    
    public static HashMap<String, Object> getVendorRequestMapJSON(JSONObject paramJobj) throws SessionExpiredException, JSONException, ServiceException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParamsJson(paramJobj);
        
         String[] groups = new String[100];
        if (paramJobj.has("group") && paramJobj.get("group") != null) {
            String groupString = (String) paramJobj.get("group");
            groups = groupString.split(",");
        }
        String[] groupsAfterAdding = groups;

        requestParams.put("group", groupsAfterAdding);
        requestParams.put("ignore", paramJobj.optString("ignore",null));
        requestParams.put("ignorecustomers", paramJobj.optString("ignorecustomers",null));
        requestParams.put("ignorevendors", paramJobj.optString("ignorevendors",null));
        if (paramJobj.optString("accountid") != null && !StringUtil.isNullOrEmpty(paramJobj.optString("accountid",null))) {
            requestParams.put("accountid", paramJobj.optString("accountid"));
        }
        requestParams.put("deleted", paramJobj.optString("deleted",null));
        requestParams.put("getSundryVendor", paramJobj.optString("getSundryVendor",null));
        requestParams.put("nondeleted", paramJobj.optString("nondeleted",null));
        if (paramJobj.optString("query",null) != null && !StringUtil.isNullOrEmpty(paramJobj.optString("query",null))) {
            requestParams.put("ss", paramJobj.optString("query"));
        } else if (paramJobj.optString("ss",null) != null && !StringUtil.isNullOrEmpty(paramJobj.optString("ss",null))) {
            requestParams.put("ss", paramJobj.optString("ss"));
        }
        if (paramJobj.optString("notinquery", null) != null && !StringUtil.isNullOrEmpty(paramJobj.optString("notinquery", null))) {
            requestParams.put("notinquery", paramJobj.optString("notinquery"));
        }         

        if (StringUtil.isNullOrEmpty(paramJobj.optString("filetype",null))) {
            if (paramJobj.optString("start",null) != null) {
                requestParams.put("start", paramJobj.optString("start"));
            }
            if (paramJobj.optString("limit",null) != null) {
                requestParams.put("limit", paramJobj.optString("limit"));
            }
        }

        if (paramJobj.optString("comboCurrencyid",null) != null) {
            requestParams.put("comboCurrencyid", paramJobj.optString("comboCurrencyid"));
        }
        if (paramJobj.optString("receivableAccFlag",null) != null && !StringUtil.isNullOrEmpty(paramJobj.optString("receivableAccFlag",null))) {
            requestParams.put("receivableAccFlag", paramJobj.optString("receivableAccFlag"));
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("dir",null)) && !StringUtil.isNullOrEmpty(paramJobj.optString("sort",null))) {
            requestParams.put("dir", paramJobj.optString("dir"));
            requestParams.put("sort", paramJobj.optString("sort"));
        }
        requestParams.put("currencyid", paramJobj.optString(Constants.globalCurrencyKey));
        requestParams.put(Constants.vendorid, paramJobj.optString(Constants.vendorid,null));
        requestParams.put(Constants.Acc_Search_Json, paramJobj.optString(Constants.Acc_Search_Json,null));
        requestParams.put(Constants.Filter_Criteria, paramJobj.optString(Constants.Filter_Criteria,null));
        requestParams.put(Constants.moduleid, paramJobj.optString(Constants.moduleid,null));
        if (paramJobj.optString("isIBGVendors") != null && !StringUtil.isNullOrEmpty(paramJobj.optString("isIBGVendors",null))) {
            requestParams.put("isIBGVendors", paramJobj.optString("isIBGVendors",null));
            requestParams.put("bankType", paramJobj.optString("bankType",null));

        }
        if (paramJobj.optString("activeDormantFlag",null) != null && !StringUtil.isNullOrEmpty(paramJobj.optString("activeDormantFlag",null))) {
            requestParams.put("activeDormantFlag", paramJobj.optString("activeDormantFlag"));
        }
        /*
         * Vendor ids for exporting selected Vendors
         */
        String exportvendorids = paramJobj.optString("exportcustvenids",null);
        if (!StringUtil.isNullOrEmpty(exportvendorids)) {
            requestParams.put("exportvendors", exportvendorids.substring(0, exportvendorids.length() - 1));
        }
        return requestParams;
    }
    
  @Override  
    public JSONObject getVendorsForCombo(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        String vendorEmailId = "";
        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Object> requestParams = getVendorRequestMapJSON(paramJobj);

            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", paramJobj.optString(Constants.companyKey));
            if (extraPref != null && extraPref.isEnablesalespersonAgentFlow()) {
                int permCode = paramJobj.optInt(Constants.PermCode_Customer);
                if (!((permCode & Constants.VENDOR_VIEWALL_PERMCODE) == Constants.VENDOR_VIEWALL_PERMCODE)) {
                    /*
                     * when (permCode & Constants.VENDOR_VIEWALL_PERMCODE) ==
                     * Constants.VENDOR_VIEWALL_PERMCODE is true then user has
                     * permission to view all vendors documents,so at that time
                     * there is need to filter record according to user&agent.
                     */
                    String userId = paramJobj.optString(Constants.useridKey);
                    requestParams.put("userid", userId);
                    requestParams.put("enablesalespersonagentflow", extraPref.isEnablesalespersonAgentFlow());
                }
            }
            requestParams.put("customervendorsortingflag", extraPref.isCustomerVendorSortingFlag());
            KwlReturnObject result = accVendorDAOobj.getVendorsForCombo(requestParams);
            String excludeaccountid = (String) requestParams.get("accountid");
            String includeaccountid = (String) requestParams.get("includeaccountid");
            String includeparentid = (String) requestParams.get("includeparentid");

            String currencyid = (String) requestParams.get("currencyid");
            KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.KWLCurrency", currencyid);

            boolean receivableAccFlag = paramJobj.optString("receivableAccFlag", null) != null ? Boolean.parseBoolean(paramJobj.optString("receivableAccFlag")) : false;
            List list  = result.getEntityList();
            //params to send to get billing address
            HashMap<String, Object> addressParams = new HashMap<String, Object>();
            addressParams.put("isDefaultAddress", true);
            addressParams.put("isBillingAddress", true); //true to get billing address

            for(Object vendor : list) {
                Object[] row = (Object[]) vendor;
                
                if (excludeaccountid != null && row[0] != null && row[0].equals(excludeaccountid)) {
                    continue;
                }
                if ((includeparentid != null && row[0] != null && (!row[0].equals(includeparentid) || (row[1] != null && !row[1].equals(includeparentid))))) {
                    continue;
                } else if ((includeaccountid != null && row[0] != null && !row[0].equals(includeaccountid))) {
                    continue;
                }

                JSONObject obj = new JSONObject();
                obj.put("accid", StringUtil.isNullObject(row[0]) ? "" : row[0].toString());
                obj.put("acccode", StringUtil.isNullObject(row[2]) ? "" : row[2].toString());
                obj.put("vattinno", StringUtil.isNullObject(row[3]) ? "" : row[3].toString());
                obj.put("csttinno", StringUtil.isNullObject(row[4]) ? "" : row[4].toString());
                
                if(extraPref != null && extraPref.getCompany().getCountry().getID().equals("106")){
                    // In backend, NPWP saved as PAN number.
                    obj.put("npwp", StringUtil.isNullObject(row[5]) ? "" : row[5].toString());
                }else{
                    obj.put("panno", StringUtil.isNullObject(row[5]) ? "" : row[5].toString());
                }
                obj.put("vendorbranch", StringUtil.isNullObject(row[6]) ? "" : row[6].toString());
                obj.put("servicetaxno", StringUtil.isNullObject(row[7]) ? "" : row[7].toString());
                obj.put("tanno", StringUtil.isNullObject(row[8]) ? "" : row[8].toString());
                obj.put("eccno", StringUtil.isNullObject(row[9]) ? "" : row[9].toString());
                obj.put("residentialstatus", StringUtil.isNullObject(row[10]) ? "" : row[10].toString());
                obj.put("natureOfPayment", StringUtil.isNullObject(row[11]) ? "" : row[11].toString());
                obj.put("deductionReason", StringUtil.isNullObject(row[12]) ? "" : row[12].toString());
                MasterItem masterItem2 = null;
                if (row[11] != null && !StringUtil.isNullOrEmpty(row[11].toString())) {
                    KwlReturnObject catresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), row[11].toString());
                    if(!catresult.getEntityList().isEmpty()) {
                        masterItem2 = (MasterItem) catresult.getEntityList().get(0);
                    }
                    obj.put("natureOfPaymentname", masterItem2.getCode() +" - "+ masterItem2.getValue());//INDIAN Company for TDS Calculation
                } else {
                    obj.put("natureOfPaymentname", "");
                }
                obj.put("tdsInterestPayableAccount", StringUtil.isNullObject(row[14]) ? "" : row[14].toString());
                obj.put("accname", StringUtil.isNullObject(row[15]) ? "" : row[15].toString());
                obj.put("aliasname", StringUtil.isNullObject(row[16]) ? "" : row[16].toString());
                obj.put("rmcdApprovalNumber", StringUtil.isNullObject(row[17]) ? "" : row[17].toString());// For Malasian company
                obj.put("accountid", StringUtil.isNullObject(row[18]) ? "" : row[18].toString());
                obj.put("groupname", StringUtil.isNullObject(row[46]) ? "" : row[46]);
                obj.put("currencyid", StringUtil.isNullObject(row[19]) ? "" : row[19].toString());
                obj.put("currencysymbol", StringUtil.isNullObject(row[42]) ? "" : row[42]);
                obj.put("currencyname", StringUtil.isNullObject(row[43]) ? "" : row[43]);
                obj.put("taxId", StringUtil.isNullObject(row[20]) ? "" : row[20].toString());
                obj.put("mappedAccountTaxId", StringUtil.isNullObject(row[48]) ? "" : row[48].toString());
                obj.put("selfBilledFromDate", row[21] !=null ? authHandler.getUserDateFormatterWithoutTimeZone(paramJobj).format(row[21].toString()) : "");
                obj.put("selfBilledToDate",row[22] !=null ? authHandler.getUserDateFormatterWithoutTimeZone(paramJobj).format(row[22].toString()) : "");
                obj.put("hasAccess", StringUtil.isNullObject(row[23]) ? "" : row[23].toString());
                obj.put("isactivate", StringUtil.isNullObject(row[23]) ? "" : row[23].toString());
                obj.put("masteragent", StringUtil.isNullObject(row[24]) ? "" : row[24].toString());
                obj.put("deducteetype", StringUtil.isNullObject(row[25]) ? "" : row[25].toString());//INDIAN Company for TDS Calculation in Make Payment
                obj.put("residentialstatus", StringUtil.isNullObject(row[10]) ? "" : row[10].toString());//INDIAN Company for TDS Calculation in Make Payment
                MasterItem masterItem = null;
                if (row[25] != null && !StringUtil.isNullOrEmpty(row[25].toString())) {
                    KwlReturnObject catresult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), row[25].toString());
                    if(!catresult.getEntityList().isEmpty()) {
                        masterItem = (MasterItem) catresult.getEntityList().get(0);
                        obj.put("deducteetypename", masterItem.getValue());//INDIAN Company for TDS Calculation in Make Payment
                    }
                } else {
                    obj.put("deducteetypename", "");
                }
                obj.put("interstateparty", StringUtil.isNullObject(row[26]) ? "" : row[26].toString());//INDIAN Company for TDS Calculation in Make Payment
                obj.put("cformapplicable", StringUtil.isNullObject(row[27]) ? "" : row[27].toString());//INDIAN Company for TDS Calculation in Make Payment
                obj.put("isTDSapplicableonvendor", StringUtil.isNullObject(row[28]) ? "" : row[28].toString());//INDIAN Company for TDS Calculation in Make Payment
                obj.put("gtaapplicable", StringUtil.isNullObject(row[29]) ? "" : row[29].toString());
                obj.put("commissionerate", StringUtil.isNullObject(row[30]) ? "" : row[30].toString());
                obj.put("division", StringUtil.isNullObject(row[31]) ? "" : row[31].toString());
                obj.put("range", StringUtil.isNullObject(row[32]) ? "" : row[32].toString());
                obj.put("iecnumber", StringUtil.isNullObject(row[33]) ? "" : row[33].toString());
                obj.put("minPriceValueForVendor", StringUtil.isNullObject(row[34]) ? "" : row[34].toString());
                obj.put("mappedPaidToId", StringUtil.isNullObject(row[35]) ? "" : row[35].toString());
                
                String companyid = paramJobj.optString(Constants.companyKey);
                KwlReturnObject comp = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) comp.getEntityList().get(0);
                JSONObject object = new JSONObject();
                if (company.getCountry() != null && Integer.parseInt(company.getCountry().getID()) == Constants.indian_country_id || extraPref.isExciseApplicable()) {
                    HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
                    addrRequestParams.put("vendorid", StringUtil.isNullObject(row[0]) ? "" : row[0].toString());
                    addrRequestParams.put("companyid", companyid);
                    addrRequestParams.put("isBillingAddress", true);//only billing address   
                    KwlReturnObject addressResult = accountingHandlerDAOobj.getVendorAddressDetails(addrRequestParams);
                    if (!addressResult.getEntityList().isEmpty()) {
                        List<VendorAddressDetails> vasList = addressResult.getEntityList();
                        if (vasList.size() > 0) {
                            VendorAddressDetails vas = (VendorAddressDetails) vasList.get(0);
                            String fullAddress = "";
                            if (!StringUtil.isNullOrEmpty(vas.getAddress())) {
                                fullAddress += vas.getAddress() + ", ";
                            }
                            if (!StringUtil.isNullOrEmpty(vas.getCity())) {
                                fullAddress += vas.getCity() + ", ";
                            }
                            if (!StringUtil.isNullOrEmpty(vas.getState())) {
                                fullAddress += vas.getState() + ", ";
                            }
                            if (!StringUtil.isNullOrEmpty(vas.getCountry())) {
                                fullAddress += vas.getCountry() + ", ";
                            }
                            if (!StringUtil.isNullOrEmpty(fullAddress)) {
                                fullAddress = fullAddress.substring(0, fullAddress.length() - 2);
                            }
                            obj.put("addressExciseBuyer", fullAddress);
                            obj.put("billingState", vas.getState() != null ? vas.getState() : "");
                            object.put("billingState", vas.getState());
                            object.put("billingCity", vas.getCity());
                        }
                    }
                }
                if (!receivableAccFlag) {                    
                    obj.put("billto", StringUtil.isNullObject(row[36]) ? "" : row[36].toString());
                    obj.put("email", StringUtil.isNullObject(row[37]) ? "" : row[37].toString());
                    obj.put("termdays", StringUtil.isNullObject(row[44]) ? "" : row[44].toString());
                    obj.put("termid", StringUtil.isNullObject(row[45]) ? "" : row[45].toString());
                    obj.put("deleted", StringUtil.isNullObject(row[47]) ? "" : row[47].toString());
                }
                
                obj.put("masterReceivedForm", StringUtil.isNullObject(row[38]) ? "" : row[38].toString());
                obj.put("paymentCriteria", StringUtil.isNullObject(row[39]) ? "" : row[39].toString());
                obj.put("defaultnatureofpurchase", StringUtil.isNullObject(row[40]) ? "" : row[40].toString());
                obj.put("manufacturertype",StringUtil.isNullObject(row[41]) ? "" : row[41].toString());
 
                addressParams.put("companyid", companyid);
                addressParams.put("vendorid", StringUtil.isNullObject(row[0]) ? "" : row[0].toString());
                VendorAddressDetails vendorAddressDetail = accountingHandlerDAOobj.getVendorAddressObj(addressParams);
                vendorEmailId = vendorAddressDetail != null ? vendorAddressDetail.getEmailID() : "";
                obj.put("billingEmail", vendorEmailId);

                /**
                 * ERP-32829 code for vendor address for GST
                 */
                addressParams.put("isBillingAddress", false);//only billing address   
                vendorAddressDetail = accountingHandlerDAOobj.getVendorAddressObj(addressParams);
                JSONArray currentAddressDetailrec = new JSONArray();
                if (vendorAddressDetail != null) {
                    object.put("vendorShippingState", vendorAddressDetail.getState());
                    object.put("vendorShippingCity", vendorAddressDetail.getCity());
                    object.put("vendorShippingCountry", vendorAddressDetail.getCountry());
                }
                currentAddressDetailrec.put(object);
                obj.put("currentAddressDetailrec", currentAddressDetailrec);
                /**
                 * Address - Dimension mapping
                 */
                object.put(Constants.companyKey, companyid);
                currentAddressDetailrec = fieldDataManagercntrl.getAddressDimensionMapping(object);
                obj.put("addressMappingRec", currentAddressDetailrec);
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            issuccess = false;
            msg = ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            issuccess = false;
            msg = "accVendorController.getVendorsForCombo : " + ex.getMessage();
            Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accVendorController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
    
}
