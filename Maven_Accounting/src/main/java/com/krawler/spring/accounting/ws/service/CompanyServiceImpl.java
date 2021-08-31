/*
 * Copyright (C) 2016 Krawler Information Systems Pvt Ltd
 * All rights reserved.
 *
 */
package com.krawler.spring.accounting.ws.service;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.IndiaComplianceConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.defaultfieldsetup.AccFieldSetupServiceDao;
import com.krawler.esp.handlers.*;
import com.krawler.esp.utils.ConfigReader;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.Customer;
import com.krawler.hql.accounting.EntitybasedLineLevelTermRate;
import com.krawler.hql.accounting.Product;
import com.krawler.hql.accounting.ProductCategoryGstRulesMappping;
import com.krawler.hql.accounting.SequenceFormat;
import com.krawler.hql.accounting.UnitOfMeasure;
import com.krawler.hql.accounting.Vendor;
import com.krawler.hql.accounting.companypreferenceservice.AccCompanyPreferencesService;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.auditTrail.AccAuditTrailServiceCMN;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.entitygst.AccEntityGstDao;
import com.krawler.spring.accounting.entitygst.AccEntityGstService;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.payment.accPaymentImpl;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.term.accTermDAO;
import com.krawler.spring.accounting.uom.accUomDAO;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.*;
import com.krawler.spring.companyDetails.companyDetailsDAO;
import com.krawler.spring.permissionHandler.permissionHandler;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import com.krawler.spring.permissionHandler.permissionHandlerService;
import com.krawler.spring.profileHandler.profileHandlerDAO;
import com.krawler.spring.profileHandler.profileHandlerService;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
import javax.servlet.ServletException;
import org.springframework.context.MessageSource;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author krawler
 */
public class CompanyServiceImpl implements CompanyService {

    private accCompanyPreferencesDAO accCompanyPreferencesObj;

    private profileHandlerDAO profileHandlerDAOObj;

    private AccountingHandlerDAO accountingHandlerDAOobj;

    private kwlCommonTablesDAO kwlCommonTablesDAOObj;

    private accAccountDAO accAccountDAOobj;

    private accPaymentImpl accPaymentDAOobj;

    private accMasterItemsDAO accMasterItemsDAOobj;

    private accTaxDAO accTaxObj;

    private permissionHandlerDAO permissionHandlerDAOObj;

    private accVendorDAO accVendorDAOObj;

    private accCustomerDAO accCustomerDAOobj;

    private accProductDAO accProductObj;

    private WSUtilService wsUtilService;

    private MessageSource messageSource;
    
     private authHandlerDAO authHandlerDAOObj;
     
    private AccFieldSetupServiceDao accFieldSetUpServiceDAOObj;
    
    private AccCommonTablesDAO accCommonTablesDAO;
    
    private accTermDAO accTermObj;
    
    private accUomDAO accUomObj;
    
    private HibernateTransactionManager txnManager;
    
    private companyDetailsDAO companyDetailsDAOObj;
    private APICallHandlerService apiCallHandlerService;
    private profileHandlerService profileHandlerServiceObj;
    private kwlCommonTablesService kwlCommonTablesService;
    private CommonFnControllerService commonFnControllerService;
    private auditTrailDAO auditTrailDAOObj;
    private AccAuditTrailServiceCMN accAuditTrialServiceCMN;
    private InventoryService inventoryService;
    private AccEntityGstDao accEntityGstDao;
    private AccEntityGstService accEntityGstService;
    private AccCompanyPreferencesService accCompanyPreferencesService;
    private permissionHandlerService permissionHandlerServiceObj;
    
    public void setAccCompanyPreferencesService(AccCompanyPreferencesService accCompanyPreferencesService) {
        this.accCompanyPreferencesService = accCompanyPreferencesService;
    }

    public void setAccEntityGstService(AccEntityGstService accEntityGstService) {
        this.accEntityGstService = accEntityGstService;
    }
    
    public void setAuditTrailDAOObj(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailDAOObj = auditTrailDAOObj;
    }
    
    public void setAccAuditTrialServiceCMN(AccAuditTrailServiceCMN accAuditTrialServiceCMN) {
        this.accAuditTrialServiceCMN = accAuditTrialServiceCMN;
    }
    
    public void setAccEntityGstDao(AccEntityGstDao accEntityGstDao) {
        this.accEntityGstDao = accEntityGstDao;
    }
            
    public void setInventoryService(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    public void setTxnManager(HibernateTransactionManager txnManager) {
        this.txnManager = txnManager;
    }

    public void setProfileHandlerServiceObj(profileHandlerService profileHandlerServiceObj) {
        this.profileHandlerServiceObj = profileHandlerServiceObj;
    }
    
    public void setKwlCommonTablesService(kwlCommonTablesService kwlCommonTablesService) {
        this.kwlCommonTablesService = kwlCommonTablesService;
    }
    
    public void setCommonFnControllerService(CommonFnControllerService commonFnControllerService) {
        this.commonFnControllerService = commonFnControllerService;
    }

    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }

    public void setaccTermObj(accTermDAO accTermObj) {
        this.accTermObj = accTermObj;
    }

    public void setaccUomObj(accUomDAO accUomObj) {
        this.accUomObj = accUomObj;
    }

    public void setcompanyDetailsDAO(companyDetailsDAO companyDetailsDAOObj) {
        this.companyDetailsDAOObj = companyDetailsDAOObj;
    }

    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {
        this.apiCallHandlerService = apiCallHandlerService;
    }
        
    public void setAccFieldSetUpServiceDAOObj(AccFieldSetupServiceDao accFieldSetUpServiceDAOObj) {
        this.accFieldSetUpServiceDAOObj = accFieldSetUpServiceDAOObj;
    }

    public void setaccCompanyPreferencesObj(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    
    public void setprofileHandlerDAO(profileHandlerDAO profileHandlerDAOObj) {
        this.profileHandlerDAOObj = profileHandlerDAOObj;
    }

    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj;
    }

    public void setAccAccountDAOobj(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setAccPaymentDAOobj(accPaymentImpl accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }

    public void setAccMasterItemsDAOobj(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }

    public void setAccTaxObj(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }

    public void setPermissionHandlerDAOObj(permissionHandlerDAO permissionHandlerDAOObj) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj;
    }

    public void setaccVendorDAO(accVendorDAO accVendorDAOObj) {
        this.accVendorDAOObj = accVendorDAOObj;
    }

    public void setAccCustomerDAO(accCustomerDAO accCustomerDAOobj) {
        this.accCustomerDAOobj = accCustomerDAOobj;
    }

    public void setaccProductObj(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }

    public void setwsUtilService(WSUtilService wsUtilService) {
        this.wsUtilService = wsUtilService;
    }

    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }
    
    public void setPermissionHandlerServiceObj(permissionHandlerService permissionHandlerServiceObj) {
        this.permissionHandlerServiceObj = permissionHandlerServiceObj;
    }    
        
    @Override
    public JSONObject getSequenceFormat(JSONObject jobj) throws ServiceException, JSONException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        JSONObject result = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean isSuccess = false;
        String companyid = "";
        
        companyid = jobj.getString(Constants.companyKey);
        Map<String,Object> requestParams=new HashMap<String,Object>();
        requestParams.put(Constants.companyKey, companyid);
        if (jobj.has(Constants.moduleid)) {
            requestParams.put(Constants.moduleid, jobj.getInt(Constants.moduleid));
        }

        boolean isFormPanel=false;
        if (jobj.has("isFormPanel")) {
            isFormPanel=jobj.optBoolean("isFormPanel",false);
        }
        
        if (jobj.has(Constants.isdefault) && jobj.get(Constants.isdefault)!=null) {
            requestParams.put(Constants.isdefault, jobj.get(Constants.isdefault));
        }

        KwlReturnObject seqFormatResult = accCompanyPreferencesObj.getSequenceFormatforModuleid(requestParams);
        if (seqFormatResult.getEntityList() != null && seqFormatResult.getEntityList().size() > 0) {
            Iterator itr = seqFormatResult.getEntityList().iterator();
            while (itr.hasNext()) {
                SequenceFormat seqFormat = (SequenceFormat) itr.next();
                JSONObject j = new JSONObject();
                j.put("id", seqFormat.getID());
                j.put("value", seqFormat.isDateBeforePrefix() ? seqFormat.getDateformatinprefix() + seqFormat.getName() : seqFormat.getName());
                j.put("dateFormat", seqFormat.getDateformatinprefix() == null ? "" : seqFormat.getDateformatinprefix().equals("empty") ? "" : seqFormat.getDateformatinprefix());
                j.put("prefix", seqFormat.getPrefix());
                j.put("suffix", seqFormat.getSuffix());
                j.put("numberofdigit", seqFormat.getNumberofdigit());
                j.put("startfrom", seqFormat.getStartfrom());
                j.put("showleadingzero", seqFormat.isShowleadingzero() ? "Yes" : "No");
                j.put("oldflag", false);
                jArr.put(j);
            }
            
        }
        if (((seqFormatResult.getEntityList().isEmpty() || seqFormatResult.getEntityList().size() == 0) && isFormPanel)|| jobj.optBoolean(Constants.isdefaultHeaderMap, false) == true) {
            JSONObject naSeqObj = new JSONObject();
            naSeqObj.put("id", "NA");
            naSeqObj.put("value", "NA");
            naSeqObj.put("oldflag", false);
            jArr.put(naSeqObj);
        }
        isSuccess = true;
        result.put(Constants.RES_TOTALCOUNT, jArr.length());
        result.put(Constants.RES_data, jArr);
        result.put(Constants.RES_success, isSuccess);

        return result;
    }
    
    /**
     * Rest Method to returns user role name, roll id and display name in JSON
     * array And Below method moved from permissionHandlerController.
     *
     * @param jobj
     * @return
     */
    @Override
    public JSONObject getRoles(JSONObject requestJobj) throws ServiceException {
        JSONObject response = new JSONObject();
        try {
            requestJobj = wsUtilService.populateAdditionalInformation(requestJobj);
            response = permissionHandlerServiceObj.getRoles(requestJobj);
            if (!StringUtil.isNullObject(response)) {
                response.put(Constants.RES_success, true);
            } else {
                response.put(Constants.RES_success, false);
            }
        } catch (Exception ex) {
            Logger.getLogger(CompanyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return response;
    }

    /**
     * Reuired parameter in JSONObject companyid, subdomain, companyname,
     * address, city, state, phone, fax, zip, website emailid, currency,
     * country, timezone, image, smtpflow, smtppassword, smtppath, smtppport
     *
     * @param jobj
     * @return
     * @throws JSONException
     * @throws ServiceException
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject updateCompany(JSONObject jobj) throws JSONException, ServiceException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        JSONObject result = new JSONObject();
        boolean isSuccess = false;
        result.put(Constants.RES_success, isSuccess);
        boolean isSetUpDone = false;
        Map<String, Object> requestParams = new HashMap<String, Object>();

        String companyid = jobj.isNull(Constants.companyKey) ? "" : jobj.getString(Constants.companyKey);
        requestParams.put(Constants.companyKey, companyid);
        KwlReturnObject compAccPrefResult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
        CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) compAccPrefResult.getEntityList().get(0);
        if (companyAccountPreferences != null && companyAccountPreferences.isSetupDone()) {
            isSetUpDone = true;
        }
        String subdomain = jobj.isNull(Constants.RES_CDOMAIN) ? "" : jobj.getString(Constants.RES_CDOMAIN);
        String companyname = jobj.isNull("companyname") ? "" : jobj.getString("companyname");
        String address = jobj.isNull("address") ? "" : jobj.getString("address");
        String city = jobj.isNull("city") ? "" : jobj.getString("city");
        String state = jobj.isNull("state") ? "" : jobj.getString("state");
        String phone = jobj.isNull("phone") ? "" : jobj.getString("phone");
        String fax = jobj.isNull("fax") ? "" : jobj.getString("fax");
        String zip = jobj.isNull("zip") ? "" : jobj.getString("zip");
        String website = jobj.isNull("website") ? "" : jobj.getString("website");
        String emailid = jobj.isNull("emailid") ? "" : jobj.getString("emailid");
        String currency = jobj.isNull("currency") ? "" : jobj.getString("currency");
        String country = jobj.isNull("country") ? "" : jobj.getString("country");
        String timezone = jobj.isNull("timezone") ? "" : jobj.getString("timezone");
        String image = jobj.isNull("image") ? "" : jobj.getString("image");
        Integer smtpflow = jobj.optInt("smtpflow");
        String smtppassword = jobj.optString("smtppassword");
        String mailserveraddress = jobj.optString("smtppath");
        String mailserverport = jobj.optString("smtppport");

        if (StringUtil.isNullOrEmpty(companyid) || StringUtil.isNullOrEmpty(subdomain) || StringUtil.isNullOrEmpty(currency)
                || StringUtil.isNullOrEmpty(timezone) || StringUtil.isNullOrEmpty(country) || StringUtil.isNullOrEmpty(companyname)) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
        Company company = (Company) companyResult.getEntityList().get(0);
        if (company == null) {
            throw ServiceException.FAILURE("Company does not exist", "e04", false);
        }

        Map<String, String> methodParam = new HashMap<String, String>();
        methodParam.put(Constants.companyKey, companyid);
        methodParam.put("currency", currency);
        methodParam.put("country", country);
        
        /*
        * For India Country
        */
        if (country.equals("105")) {
            String stateid = profileHandlerDAOObj.getStateidByStateName(state);
            state = stateid;
        }

        if (isSetUpDone && !isCompanyCurrencyandCountrySame(methodParam) && !profileHandlerDAOObj.isTransactionCreated(companyid)) {
            /*
             * Rollback company setup data if setup is already is done
             */
            profileHandlerDAOObj.deleteCompanySetUpData(requestParams);

            /*
             * Update default data as per Country and Currency
             */
                KwlReturnObject kresult = accAccountDAOobj.copyAccounts(companyid, currency, null, null, null, null,false);
                HashMap hmAcc = (HashMap) kresult.getEntityList().get(0);
                accPaymentDAOobj.copyPaymentMethods(companyid, hmAcc);
                accMasterItemsDAOobj.copyMasterItems(companyid, hmAcc);
                kwlCommonTablesDAOObj.evictObject(companyAccountPreferences);
                accCompanyPreferencesObj.setAccountPreferences(companyid, hmAcc, getCurrentDate());
                //accTaxObj.copyTax1099Category(companyid); No Use of this code 
            
        }
        List list = accCompanyPreferencesObj.isAnotherCompanyExistWithSameSubDomain(subdomain, company.getCompanyID());
        if (!list.isEmpty()) {
            throw ServiceException.FAILURE("Company does not exist", "e04", false);
        }

        HashMap<String, Object> companyHashMap = new HashMap<>();
        companyHashMap.put(Constants.companyKey, companyid);
        companyHashMap.put("companyname", companyname);
        companyHashMap.put("subdomain", subdomain);

        if (!isCompanyCurrencyandCountrySame(methodParam) && !profileHandlerDAOObj.isTransactionCreated(companyid)) {
            if (!companyAccountPreferences.isCountryChange()) {
                companyHashMap.put("country", country);
            }
            if (!companyAccountPreferences.isCurrencyChange()) {
                companyHashMap.put("currency", currency);
            }
        }
        companyHashMap.put("timezone", timezone);
        companyHashMap.put("companylogo", image);
        companyHashMap.put("address", address);
        companyHashMap.put("city", city);

        companyHashMap.put("state", state);
        companyHashMap.put("phone", phone);
        companyHashMap.put("fax", fax);
        companyHashMap.put("zip", zip);
        companyHashMap.put("website", website);
        companyHashMap.put("emailid", emailid);

        Date modifydate;
        try {
            modifydate = authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
            companyHashMap.put("modifiedon", modifydate);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(CompanyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ParseException ex) {
            Logger.getLogger(CompanyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }

        //SMTP Configs
        companyHashMap.put("smtpflow", smtpflow);
        companyHashMap.put("smtppassword", smtppassword);
        companyHashMap.put("mailserveraddress", mailserveraddress);
        companyHashMap.put("mailserverport", mailserverport);

        KwlReturnObject cmprtObj = profileHandlerDAOObj.updateCompany(companyHashMap);

        result = new JSONObject();
        isSuccess = true;
        result.put(Constants.RES_success, isSuccess);
        Locale locale = jobj.has("language") ? Locale.forLanguageTag(jobj.getString("language")) : Locale.forLanguageTag("en");
        String msg = messageSource.getMessage("acc.field.companyDetailsSavedSuccessfully", null, locale);
        result.put(Constants.RES_MESSAGE, msg);

        return result;
    }
    
    @Override
    public JSONObject saveCompany(JSONObject jobj) throws JSONException, ServiceException {        
        JSONObject result = new JSONObject();
        if(jobj.has(Constants.RES_CREATE_NEW) && jobj.getBoolean(Constants.RES_CREATE_NEW)){
            try {
                result = createCompany(jobj);
            } catch (SQLException ex) {
                Logger.getLogger(CompanyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                throw ServiceException.FAILURE(ex.getMessage(), ex);
            }
        }
        else{
            result=updateCompany(jobj);
        }
        return result;
    }

    /**
     * Reuired parameter in JSONObject companyid, subdomain
     *
     * @param jobj
     * @return
     * @throws ServiceException
     */
    @Override
    public boolean isCompanyActivated(JSONObject jobj) throws ServiceException, JSONException {
        boolean result = false;
        if (jobj != null) {
            if (!jobj.has(Constants.companyKey)) {
                jobj = wsUtilService.populateAdditionalInformation(jobj);
            }
            result = accCompanyPreferencesObj.isCompanyActivated(jobj.getString(Constants.companyKey));
        }
        return result;
    }
    
  @Override  
     public JSONObject getDefaultFieldsforMobileSetup(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject response = new JSONObject();
        if (paramJobj != null) {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            JSONArray dataArray = accFieldSetUpServiceDAOObj.getMobileFieldsConfig(paramJobj);
            response.put(Constants.RES_data, dataArray);
            response.put(Constants.RES_TOTALCOUNT, dataArray.length());
            response.put(Constants.RES_success, true);
        }
        return response;
    }
    
    @Override
    public JSONObject isCompanyExists(JSONObject jobj) throws ServiceException, JSONException {
        JSONObject response = new JSONObject();
        response.put(Constants.RES_success, "false");
        List list = new ArrayList();
        String sql = "";
        boolean flag = false;
        String param = "";
        if (jobj.has("companyid")) {
            list = accCompanyPreferencesObj.isCompanyExistWithCompanyID(jobj.getString("companyid"));
        } else if (jobj.has("cdomain")) {
            String cdomain = jobj.getString("cdomain");
            list = accCompanyPreferencesObj.isCompanyExistWithSubDomain(cdomain);
        } else {
            flag = true;
        }
        if (!flag) {
            if (list.size() > 0) {
                Long value = (Long)list.get(0);
                if(value > 0){
                    response.put(Constants.RES_success, true);
                }else{
                    response.put(Constants.RES_success, false);
                }
            }
        }
        return response;
    }
    
    @Override
    public JSONObject isUserExists(JSONObject jobj) throws ServiceException, JSONException {
        JSONObject response = new JSONObject();
        response.put(Constants.RES_success, "false");
        String userId = null;
        boolean flag = false;
        if (jobj.has(Constants.useridKey) && !StringUtil.isNullOrEmpty(jobj.getString(Constants.useridKey))) {
            userId = jobj.getString(Constants.useridKey);
        } else {
            String userName = "";
            if (jobj.has("userName")) {
                userName = jobj.getString("userName");
                flag = true;
            } else if (jobj.has("username")) {
                userName = jobj.getString("username");
                flag = true;
            }

        }
        KwlReturnObject kmsg = profileHandlerDAOObj.getUserWithUserName(userId, flag);
        int count = kmsg.getRecordTotalCount();
        if (count > 0) {
            response.put(Constants.RES_success, true);
        } else {
            response.put(Constants.RES_success, false);
        }
        return response;
    }

    /**
     * Reuired parameter in JSONObject subdomain
     *
     * @param jobj
     * @return
     * @throws ServiceException
     * @throws SQLException
     * @throws JSONException
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject deleteCompany(JSONObject jobj) throws ServiceException, JSONException {
        JSONObject result = new JSONObject();
        String subdomain = jobj.optString(Constants.RES_CDOMAIN, null);
        if (subdomain != null) {
            accCompanyPreferencesObj.deleteCompanyData(subdomain);
        } else {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        return result;
    }

    /**
     * Reuired parameter in JSONObject companyid
     *
     * @param cdomain
     * @param request
     * @return
     * @throws ServiceException
     * @throws com.krawler.utils.json.base.JSONObject
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getUserList(JSONObject jobj) throws ServiceException, JSONException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        JSONObject result = new JSONObject();
        result.put(Constants.RES_success, true);

        boolean flag = isCompanyActivated(jobj);
        
        if (jobj.optBoolean(Constants.isdefaultHeaderMap)) {//isdefaultHeaderMap=true for mobile apps(Android & POS)
           result=inventoryService.getStoreList(jobj);
        } else {
            if (flag) {
                JSONArray jArr = new JSONArray();

                KwlReturnObject venResult = accVendorDAOObj.getAllVendorsOfCompany(jobj.getString(Constants.companyKey));
                int totalCount = venResult.getRecordTotalCount();
                Iterator itr = venResult.getEntityList().iterator();

                while (itr.hasNext()) {
                    KwlReturnObject vendorResult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), itr.next().toString());
                    Vendor vendor = (Vendor) vendorResult.getEntityList().get(0);
                    JSONObject obj = new JSONObject();
                    obj.put("vendorID", vendor.getID());
                    obj.put("vendorName", vendor.getName());
                    obj.put("vendorAddress", vendor.getAddress() == null ? "" : vendor.getAddress());
                    obj.put("vendorEmail", vendor.getEmail() == null ? "" : vendor.getEmail());
                    obj.put("vendorContactNo", vendor.getContactNumber() == null ? "" : vendor.getContactNumber());
                    obj.put("vendorDebitTerm", vendor.getDebitTerm() == null ? "" : vendor.getDebitTerm().getID());
                    obj.put("vendorFax", vendor.getFax() == null ? "" : vendor.getFax());
                    jArr.put(obj);
                }
                result.put(Constants.RES_data, jArr);
                result.put(Constants.RES_TOTALCOUNT, totalCount);
            } else {
                result.put(Constants.RES_success, false);
                throw ServiceException.FAILURE("Company is not Active.", "erp26", false);
            }
        }
        
        return result;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getNextAutonumber(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject response = new JSONObject();
        try {
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            if (!paramJobj.has("from") || !paramJobj.has("oldflag") || !paramJobj.has("sequenceformat") || !paramJobj.has(Constants.companyKey)) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
            response = accCompanyPreferencesService.getNextAutoNumber(paramJobj);
            if (response.has(Constants.RES_msg)) {
                response.put(Constants.RES_MESSAGE, response.getString(Constants.RES_msg));
                response.remove(Constants.RES_msg);
            }
        } catch (Exception ex) {
            Logger.getLogger(CompanyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("Next Auto number is not generated. Please check", "erp26", false);
        }
        return response;
    }

    /**
     * Reuired parameter in JSONObject userid or userId, subdomain
     *
     * @param jobj
     * @param request
     * @param response
     * @return
     * @throws com.krawler.common.service.ServiceException
     * @throws com.krawler.utils.json.base.JSONException
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getUserPermissions(JSONObject jobj) throws ServiceException, JSONException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        KwlReturnObject kmsg = null;
        JSONObject result = new JSONObject();
        String userid = (jobj.has("userid") ? jobj.getString("userid") : (jobj.has("userId") ? jobj.getString("userId") : ""));
        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put("userid", userid);
        if (!StringUtil.isNullOrEmpty(userid)) {
            JSONObject permJobj = new JSONObject();
            kmsg = permissionHandlerDAOObj.getActivityFeature();
            permJobj = permissionHandler.getAllPermissionJson(kmsg.getEntityList(), null, kmsg.getRecordTotalCount());

            kmsg = permissionHandlerDAOObj.getUserPermission(requestParams);
            permJobj = permissionHandler.getRolePermissionJson(kmsg.getEntityList(), permJobj);

            requestParams.put(Constants.companyKey,jobj.optString(Constants.companyKey));
            requestParams.put(Constants.isdefaultHeaderMap,jobj.optBoolean(Constants.isdefaultHeaderMap));
            
            kmsg = permissionHandlerDAOObj.getUserPermission(requestParams);
            List<Object[]> rows = kmsg.getEntityList();
            ArrayList jo = new ArrayList();
            JSONObject Perm = permJobj.getJSONObject("Perm");
            for (Object[] row : rows) {
                String keyName = row[0].toString();
                String value = row[1].toString();
                JSONObject keyPerm = Perm.getJSONObject(keyName);
                long perm = Long.parseLong(value);
                JSONObject temp = doOperation(keyPerm, perm);
                temp.put(Constants.permCode,value);
                jo.add(new JSONObject().put(keyName, temp));
            }
            result.put(Constants.RES_data, jo);
            result.put(Constants.RES_TOTALCOUNT, rows.size());
            result.put(Constants.RES_success, true);
        } else {
            throw ServiceException.FAILURE("User not exist.", "e06", false);
        }
        return result;
    }

    private JSONObject doOperation(JSONObject keys, long value) throws JSONException {
        JSONObject newValues = new JSONObject();

        List list = permissionsValues(value);

        int listLen = list.size();
        List<String> strings = new ArrayList<>();
        Iterator iterator = keys.keys();
        while (iterator.hasNext()) {
            strings.add((String) iterator.next());
        }
        String[] keysNames = new String[strings.size()];
        keysNames = strings.toArray(keysNames);
        for (String key : keysNames) {
            int x = 0;

            x = (Integer) keys.get(key);
            int p = countValue(x);
            if (p >= listLen) {
                newValues.put(key, new Boolean("false"));
            } else {
                newValues.put(key, list.get(p));
            }

        }
        return newValues;
    }

    private List permissionsValues(Long val) {
        ArrayList values = new ArrayList();
        if (val == 0) {
            values.add(new Boolean("false"));
        } else {
            while (val > 0) {
                if (val % 2 == 0) {
                    values.add(new Boolean("false"));
                } else {
                    values.add(new Boolean("true"));
                }
                val /= 2;
            }
        }
        return values;
    }

    private int countValue(int val) {
        int cnt = 0;
        while (val != 1) {
            cnt++;
            val /= 2;
        }
        return cnt;
    }

    /**
     * Description : This Method is used to check the existing currency or
     * country is same or not
     *
     * @param <request> used to get request parameters
     * @param <session> used to get Company Object
     * @return :boolean
     */
    private boolean isCompanyCurrencyandCountrySame(Map<String, String> methodParam) throws ServiceException {

        String companyid = "", currency = "", country = "";
        boolean isCurrencyandCountrySame = false;
        companyid = methodParam.get(Constants.companyKey);
        currency = methodParam.get("currency");
        country = methodParam.get("country");

        KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
        Company company = (Company) companyResult.getEntityList().get(0);
        String old_Currency = "", old_Country = "";
        if (!StringUtil.isNullObject(company.getCurrency())) {
            old_Currency = company.getCurrency().getCurrencyID();
        }
        if (!StringUtil.isNullObject(company.getCountry())) {
            old_Country = company.getCountry().getID();
        }
        if (company != null && old_Currency.equalsIgnoreCase(currency) && old_Country.equalsIgnoreCase(country)) {
            isCurrencyandCountrySame = true;
        }
        return isCurrencyandCountrySame;
    }

    private JSONObject getPermissions(String userid) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();
        JSONObject fjobj = new JSONObject();
        JSONObject ujobj = new JSONObject();

        KwlReturnObject kmsg = permissionHandlerDAOObj.getActivityFeature();
        Iterator ite = kmsg.getEntityList().iterator();
        while (ite.hasNext()) {
            Object[] row = (Object[]) ite.next();
            String fName = ((ProjectFeature) row[0]).getFeatureName();
            ProjectActivity activity = (ProjectActivity) row[1];
            if (!fjobj.has(fName)) {
                fjobj.put(fName, new JSONObject());
            }

            JSONObject temp = fjobj.getJSONObject(fName);
            if (activity != null) {
                temp.put(activity.getActivityName(), (int) Math.pow(2, temp.length()));
            }
        }

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put("userid", userid);
        KwlReturnObject krObj = permissionHandlerDAOObj.getUserPermission(requestParams);
        ite = krObj.getEntityList().iterator();
        while (ite.hasNext()) {
            Object[] row = (Object[]) ite.next();
            ujobj.put(row[0].toString(), row[1]);
        }
        jobj.put("Perm", fjobj);
        jobj.put("UPerm", ujobj);

        return jobj;
    }

    /**
     * Reuired parameter in JSONObject userid, emailid, fname, lname, contactno,
     * address, timezone
     *
     * @param jobj
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject editUser(JSONObject jobj) throws ServiceException, JSONException, SessionExpiredException, ParseException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        JSONObject response = new JSONObject();
        String userid = "";
        boolean flag = false;
        if (jobj.has("userid")) {
            userid = StringUtil.serverHTMLStripper(jobj.get("userid").toString());
        } else {
            flag = true;
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        if (!flag) {
            String emailid = jobj.has("emailid") ? jobj.getString("emailid").trim().replace(" ", "+") : "";
            String fname = jobj.has("fname") ? StringUtil.serverHTMLStripper(jobj.get("fname").toString()) : "";
            String lname = jobj.has("lname") ? StringUtil.serverHTMLStripper(jobj.get("lname").toString()) : "";
            emailid = jobj.has("emailid") ? StringUtil.serverHTMLStripper(emailid) : "";
            String contactno = jobj.has("contactno") ? StringUtil.serverHTMLStripper(jobj.get("contactno").toString()) : "";
            String address = jobj.has("address") ? StringUtil.serverHTMLStripper(jobj.get("address").toString()) : "";
            String department = jobj.has("department") ? StringUtil.serverHTMLStripper(jobj.get("department").toString()) : "";
            String designation = jobj.has("designation") ? StringUtil.serverHTMLStripper(jobj.get("designation").toString()) : "";
            String employeeid = jobj.has("emplyoeeid") ? StringUtil.serverHTMLStripper(jobj.get("emplyoeeid").toString()) : "";

            String timezone = jobj.has("timezone") ? StringUtil.serverHTMLStripper(jobj.get("timezone").toString()) : "";

            KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), userid);
            User u = (User) userResult.getEntityList().get(0);
            if (u != null) {
                HashMap<String, Object> userHashMap = new HashMap<>();
                userHashMap.put("userid", u.getUserID());
                if (jobj.has("fname")) {
                    userHashMap.put("firstName", fname);
                }
                if (jobj.has("address")) {
                    userHashMap.put("address", address);
                }
                if (jobj.has("lname")) {
                    userHashMap.put("lastName", lname);
                }
                if (jobj.has("emailid")) {
                    userHashMap.put("emailID", emailid);
                }
                if (jobj.has("contactno")) {
                    userHashMap.put("contactno", contactno);
                }
                if (jobj.has("department")) {
                    userHashMap.put("department", department);
                }
                if (jobj.has("designation")) {
                    userHashMap.put("designation", designation);
                }
                if (jobj.has("emplyoeeid")) {
                    userHashMap.put("employeeid", employeeid);
                }
                if (jobj.has("timezone")) {
                    userHashMap.put("timeZone", timezone);
                }
                Date updatedon = authHandler.getSimpleDateAndTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
                userHashMap.put("updatedon", updatedon);

                KwlReturnObject rtObj = profileHandlerDAOObj.addUser(userHashMap);
                u = (User) rtObj.getEntityList().get(0);

                response.put(Constants.RES_success, true);
                Locale locale = jobj.has("language") ? Locale.forLanguageTag(jobj.getString("language")) : Locale.forLanguageTag("en");
                String msg = messageSource.getMessage("acc.field.UserInformationsavedSuccessfully.", null, locale);
                response.put(Constants.RES_MESSAGE, msg);

            } else {
                throw ServiceException.FAILURE("User not exist.", "e06", false);
            }
        }
        return response;
    }    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">

    /**
     * Reuired parameter in JSONObject companyid, subdomain,
     *
     * @param request
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    @Override
    public JSONObject deactivateCompany(JSONObject jobj) throws ServiceException, JSONException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        JSONObject result = new JSONObject();
        KwlReturnObject resultCompnay = accountingHandlerDAOobj.getObject(Company.class.getName(), jobj.getString(Constants.companyKey));

        if (!StringUtil.isNullObject(resultCompnay.getEntityList()) && resultCompnay.getEntityList().size() > 0) {
            Company company = (Company) resultCompnay.getEntityList().get(0);
            HashMap<String, Object> companyHashMap = new HashMap<>();
            companyHashMap.put(Constants.companyKey, company.getCompanyID());
            companyHashMap.put("isactivated", false);
            KwlReturnObject cmprtObj = profileHandlerDAOObj.updateCompany(companyHashMap);
            if (cmprtObj.getEntityList().size() > 0) {
                result.put(Constants.RES_success, true);
                result.put(Constants.RES_TOTALCOUNT, cmprtObj.getRecordTotalCount());
            }
        }
        return result;
    }

    private Date getCurrentDate() throws ServiceException {
        DateFormat sdf = new SimpleDateFormat("MMMM d, yyyy");
        String timezoneid = StorageHandler.getDefaultTimeZoneID();
        KwlReturnObject kresult = accountingHandlerDAOobj.getObject(KWLTimeZone.class.getName(), timezoneid);
        KWLTimeZone tz = (KWLTimeZone) kresult.getEntityList().get(0);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT" + tz.getDifference()));
        Date curDate = new Date();
        try {
            curDate = sdf.parse(sdf.format(curDate));
        } catch (ParseException ex) {
            Logger.getLogger(CompanyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
        return curDate;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject getUpdates(JSONObject jobj) throws ServiceException, JSONException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        JSONObject result = new JSONObject();
        boolean isSuccess = false;
        result.put(Constants.RES_success, isSuccess);
//        String result = "{\"success\":false}";
        JSONObject jOutput = new JSONObject();
        JSONObject jData = new JSONObject();
//            JSONObject jobj = new JSONObject(request.getParameter(Constants.RES_data));
        String companyid = jobj.isNull(Constants.companyKey) ? "" : jobj.getString(Constants.companyKey);
        String userid = jobj.isNull("userid") ? "" : jobj.getString("userid");
        int offset = jobj.isNull("offset") ? 0 : jobj.getInt("offset");
        int limit = jobj.isNull("limit") ? 5 : jobj.getInt("limit");
        if (StringUtil.isNullOrEmpty(companyid) || StringUtil.isNullOrEmpty(userid)) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }

        KwlReturnObject result1 = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
        Company company = (Company) result1.getEntityList().get(0);
        if (company == null) {
            throw ServiceException.FAILURE("Company does not exist", "e04", false);
        }

        KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), userid);
        User user = (User) userResult.getEntityList().get(0);
        if (user == null) {
            throw ServiceException.FAILURE("User does not exist", "e06", false);
        }

        JSONArray jArr = getUpdatesArray(companyid, userid);
        jData.put("head", "<div style='padding:10px 0 10px 0;font-size:13px;font-weight:bold;color:#10559a;border-bottom:solid 1px #EEEEEE;'>Updates</div>");

        jOutput.append(Constants.RES_data, jData);
        for (int i = offset; i < offset + limit && i < jArr.length(); i++) {
            JSONObject temp = new JSONObject();
            temp.put("update", jArr.getString(i));
            jOutput.append(Constants.RES_data, temp);
        }
        jOutput.append("count", jArr.length());

//            result = "{\"valid\": true, \"success\": true, \"data\":" + jOutput.toString() + "}";
        isSuccess = true;
        result.put("valid", true);
        result.put(Constants.RES_success, isSuccess);
        result.put(Constants.RES_data, jOutput.toString());

        return result;
    }

    private JSONArray getUpdatesArray(String companyid, String userid) throws ServiceException, JSONException {
        JSONArray jArray = new JSONArray();
        ArrayList temp;

        KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), userid);
        User user = (User) userResult.getEntityList().get(0);
        JSONObject perms = getPermissions(user.getUserID());

        temp = getVendorsUpdationInfo(companyid, perms, false);
        for (int i = 0; i < temp.size(); i++) {
            jArray.put(temp.get(i));
        }
        temp = getCustomersUpdationInfo(companyid, perms, false);
        for (int i = 0; i < temp.size(); i++) {
            jArray.put(temp.get(i));
        }
        temp = getProductsBelowROLInfo(companyid, perms, false);
        for (int i = 0; i < temp.size(); i++) {
            jArray.put(temp.get(i));
        }
        ArrayList props = new ArrayList();
        props.add("color=#10559A");
        replaceTag(jArray, "a", "font", props);
        return jArray;
    }

    private ArrayList getVendorsUpdationInfo(String companyID, JSONObject perms, Boolean isDashboard) throws ServiceException, JSONException {
        ArrayList jArray = new ArrayList();

        KwlReturnObject result = accVendorDAOObj.getVendor_Dashboard(companyID, true, "createdOn", 0, 2);
        List list = result.getEntityList();
        Iterator itr = list.iterator();
        String link;
        String vendorID = "";
        while (itr.hasNext()) {
//                KwlReturnObject vendorResult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), itr.next().toString());
            Vendor vendor = (Vendor) itr.next();
            link = vendor.getAccount().getName();
            vendorID = vendor.getAccount().getID();
            if (DashboardHandler.isPermitted(perms, "vendor", "view")) {
                link = DashboardHandler.getLink(link, "callVendorDetails(\"" + vendorID + "\")");
            }
            jArray.add(DashboardHandler.getFormatedAlert("New vendor " + link + " created", "accountingbase updatemsg-vendor", isDashboard));
        }
        KwlReturnObject result1 = accVendorDAOObj.getVendor_Dashboard(companyID, true, "modifiedOn", 0, 2);
        list = result1.getEntityList();
        itr = list.iterator();
        while (itr.hasNext()) {
            Vendor vendor = (Vendor) itr.next();
            link = vendor.getAccount().getName();
            vendorID = vendor.getAccount().getID();
            if (DashboardHandler.isPermitted(perms, "vendor", "view")) {
                link = DashboardHandler.getLink(link, "callVendorDetails(\"" + vendorID + "\")");
            }
            jArray.add(DashboardHandler.getFormatedAlert("Vendor " + link + " modified", "accountingbase updatemsg-vendor", isDashboard));
        }

        return jArray;
    }

    private ArrayList getCustomersUpdationInfo(String companyID, JSONObject perms, Boolean isDashboard) throws ServiceException, JSONException {
        ArrayList jArray = new ArrayList();

        KwlReturnObject result = accCustomerDAOobj.getCustomer_Dashboard(companyID, true, "createdOn", 0, 2);
        List list = result.getEntityList();
        Iterator itr = list.iterator();
        String link;
        String customerID;
        while (itr.hasNext()) {
            Customer customer = (Customer) itr.next();
            customerID = customer.getID();
            link = customer.getAccount().getName();
            if (DashboardHandler.isPermitted(perms, "customer", "view")) {
                link = DashboardHandler.getLink(link, "callCustomerDetails(\"" + customerID + "\")");
            }

            jArray.add(DashboardHandler.getFormatedAlert("New customer " + link + " created", "accountingbase updatemsg-customer", isDashboard));
        }
        KwlReturnObject result1 = accCustomerDAOobj.getCustomer_Dashboard(companyID, false, "modifiedOn", 0, 2);
        list = result1.getEntityList();
        itr = list.iterator();
        while (itr.hasNext()) {
            Customer customer = (Customer) itr.next();
            customerID = customer.getID();
            link = customer.getAccount().getName();
            if (DashboardHandler.isPermitted(perms, "customer", "view")) {
                link = DashboardHandler.getLink(link, "callCustomerDetails(\"" + customerID + "\")");
            }
            jArray.add(DashboardHandler.getFormatedAlert("Customer " + link + " modified", "accountingbase updatemsg-customer", isDashboard));
        }

        return jArray;
    }

    private ArrayList getProductsBelowROLInfo(String companyID, JSONObject perms, Boolean isDashboard) throws ServiceException, JSONException {
        ArrayList jArray = new ArrayList();

        JSONObject joo = getProducts(null, companyID);
        JSONArray jArr = joo.getJSONArray(Constants.RES_data);
        String link;
        String productID;
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject obj = jArr.getJSONObject(i);
            if (obj.getInt("quantity") > obj.getInt("reorderlevel")) {
                continue;
            }
            link = obj.getString("productname");
            productID = obj.getString("productid");
            if (DashboardHandler.isPermitted(perms, "product", "view")) {
                link = DashboardHandler.getLink(link, "callProductDetails(\"" + productID + "\")");
            }
            jArray.add(DashboardHandler.getFormatedAlert("The Product " + link + " is below reorder level (Available quantity:" + obj.getInt("quantity") + " " + obj.getString("uomname") + ")", "accountingbase updatemsg-product", isDashboard));
        }

        return jArray;
    }

    private JSONObject getProducts(String productid, String companyID) throws ServiceException, JSONException {
        JSONObject jobj = new JSONObject();

        KwlReturnObject result1 = accountingHandlerDAOobj.getObject(Company.class.getName(), companyID);
        Company company = (Company) result1.getEntityList().get(0);
        String currencyid = company.getCurrency().getCurrencyID();

        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put("currencyid", currencyid);
        requestParams.put(Constants.companyKey, companyID);
        KwlReturnObject result = accProductObj.getProductsFoRemoteAPI(requestParams);
        Iterator itr = result.getEntityList().iterator();
        JSONArray jArr = new JSONArray();
        while (itr.hasNext()) {
            Object[] row = (Object[]) itr.next();
            Product product = (Product) row[0];
            if (product.getID().equals(productid)) {
                continue;
            }
            JSONObject obj = new JSONObject();
            obj.put("productid", product.getID());
            obj.put("productname", product.getName());
            obj.put("desc", product.getDescription());
            UnitOfMeasure uom = product.getUnitOfMeasure();
            obj.put("uomid", uom == null ? "" : uom.getID());
            obj.put("uomname", uom == null ? "" : uom.getNameEmptyforNA());
            obj.put("precision", uom == null ? 0 : (Integer) uom.getAllowedPrecision());
            obj.put("leadtime", product.getLeadTimeInDays());
            obj.put("reorderlevel", product.getReorderLevel());
            obj.put("reorderquantity", product.getReorderQuantity());
            obj.put("purchaseaccountid", (product.getPurchaseAccount() != null ? product.getPurchaseAccount().getID() : ""));
            obj.put("salesaccountid", (product.getSalesAccount() != null ? product.getSalesAccount().getID() : ""));
            obj.put("purchaseretaccountid", (product.getPurchaseReturnAccount() != null ? product.getPurchaseReturnAccount().getID() : ""));
            obj.put("salesretaccountid", (product.getSalesReturnAccount() != null ? product.getSalesReturnAccount().getID() : ""));
            obj.put("level", 0);
            obj.put("purchaseprice", row[1]);
            obj.put("saleprice", row[2]);
            obj.put("quantity", (row[3] == null ? 0 : row[3]));
            obj.put("initialquantity", (row[4] == null ? 0 : row[4]));
            obj.put("initialprice", (row[5] == null ? 0 : row[5]));
            jArr.put(obj);
            obj.put("leaf", getChildProducts(product, jArr, 0, productid));
        }
        jobj.put(Constants.RES_data, jArr);

        return jobj;
    }

    private boolean getChildProducts(Product product, JSONArray jArr, int level, String productid) throws JSONException, ServiceException {
        boolean leaf = true;
        Iterator<Product> itr = new TreeSet(product.getChildren()).iterator();
        level++;
        String currencyid = product.getCompany().getCurrency().getCurrencyID();
        while (itr.hasNext()) {
            Product child = itr.next();
            if (child.getID().equals(productid) || child.isDeleted()) {
                continue;
            }
            leaf = false;
            JSONObject obj = new JSONObject();
            obj.put("productid", child.getID());
            obj.put("productname", child.getName());
            obj.put("desc", child.getDescription());
            obj.put("uomid", child.getUnitOfMeasure().getID());
            obj.put("uomname", child.getUnitOfMeasure().getNameEmptyforNA());
            obj.put("leadtime", child.getLeadTimeInDays());
            obj.put("reorderlevel", child.getReorderLevel());
            obj.put("reorderquantity", child.getReorderQuantity());
            obj.put("purchaseaccountid", (child.getPurchaseAccount() != null ? child.getPurchaseAccount().getID() : ""));
            obj.put("salesaccountid", (child.getSalesAccount() != null ? child.getSalesAccount().getID() : ""));
            obj.put("purchaseretaccountid", (child.getPurchaseReturnAccount() != null ? child.getPurchaseReturnAccount().getID() : ""));
            obj.put("salesretaccountid", (child.getSalesReturnAccount() != null ? child.getSalesReturnAccount().getID() : ""));
            obj.put("parentid", product.getID());
            obj.put("parentname", product.getName());
            obj.put("level", level);

            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("currencyid", currencyid);
            KwlReturnObject result = accProductObj.getChildProductsFoRemoteAPI(requestParams);
            List list = result.getEntityList();

            if (!list.isEmpty()) {
                Object[] row = (Object[]) list.get(0);
                obj.put("purchaseprice", row[0]);
                obj.put("saleprice", row[1]);
                obj.put("quantity", (row[2] == null ? 0 : row[2]));
            }
            jArr.put(obj);
            obj.put("leaf", getChildProducts(child, jArr, level, productid));
        }
        return leaf;
    }

    private static void replaceTag(JSONArray jArr, String oldTag, String newTag, ArrayList properties) throws JSONException {
        for (int i = 0; i < jArr.length(); i++) {
            String str = jArr.getString(i);
            str = str.replaceAll("<" + oldTag + " [^>]*>", "<" + newTag + (properties != null ? " " + DashboardHandler.joinArrayList(properties, " ") : "") + ">");
            str = str.replaceAll("</" + oldTag + ">", "</" + newTag + ">");
            jArr.put(i, str);
        }
    }

    @Override
    public JSONObject getYearLock(JSONObject jobj) throws ServiceException, JSONException {
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        JSONObject result = new JSONObject();
        JSONArray jArr = new JSONArray();
        JSONObject res = new JSONObject();
        result.put(Constants.RES_success, true);
        String dateFormat = jobj.optString("dateformat", "yyyy-MM-dd");
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);

        String companyID = jobj.getString(Constants.companyKey);
        List list = accCompanyPreferencesObj.getYearID(companyID);
        if (list.size() > 0) {
            KwlReturnObject compAccPrefResult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyID);
            CompanyAccountPreferences cap = (CompanyAccountPreferences) compAccPrefResult.getEntityList().get(0);
            Calendar fyFrom = Calendar.getInstance();
            fyFrom.setTime(cap.getFinancialYearFrom());
            Calendar startdate = Calendar.getInstance();
            Calendar enddate = Calendar.getInstance();
            for (int i = 0; i < list.size(); i++) {
                startdate.setTime(cap.getFinancialYearFrom());
                enddate.setTime(cap.getFinancialYearFrom());
                startdate.set(Calendar.YEAR, (Integer) list.get(i));
                enddate.set(Calendar.YEAR, ((Integer) list.get(i)) + 1);
                enddate.add(Calendar.DATE, -1);
                res = new JSONObject();
                res.put("startdate", sdf.format(startdate.getTime()));
                res.put("enddate", sdf.format(enddate.getTime()));
                jArr.put(res);
            }
        }
        result.put(Constants.RES_TOTALCOUNT, jArr.length());
        result.put(Constants.RES_data, jArr);
        result.put("jarr", jArr);
        return result;

    }

    @Override
    public JSONObject getAccountList(JSONObject jobj) throws ServiceException, JSONException {
        JSONObject result = new JSONObject();
        result.put(Constants.RES_success, true);
        jobj = wsUtilService.populateAdditionalInformation(jobj);
//        if (wsUtilServiceObj.isCompanyExists(jobj)) {
        JSONArray jArr = new JSONArray();
        String companyID = jobj.getString(Constants.companyKey);
        String nature = jobj.has("nature") ? jobj.getString("nature") : "";
        String mastertype = jobj.has("mastertype") ? jobj.getString("mastertype") :null;

        HashMap<String, Object> reqParams = new HashMap<>();
        reqParams.put(Constants.companyKey, companyID);
        reqParams.put("nature", nature);
        reqParams.put("mastertype", mastertype);
        KwlReturnObject accResult = accAccountDAOobj.getAccountsForPM(reqParams);
        List list = accResult.getEntityList();
        int totalCount = accResult.getRecordTotalCount();
        Iterator itr = list.iterator();

        while (itr.hasNext()) {
            Object[] oj = (Object[]) itr.next();
            JSONObject obj = new JSONObject();
            obj.put("accountID", oj[0].toString());
            obj.put("accountName", oj[1].toString());
            if (oj[2] != null) {
                obj.put("accountCode", oj[2].toString());
            } else {
                obj.put("accountCode", "");
            }
            if (oj[3] != null) {
                obj.put("mastertype", oj[3].toString());
            }
            jArr.put(obj);
        }
        result.put(Constants.RES_data, jArr);
        result.put(Constants.RES_TOTALCOUNT, totalCount);
        return result;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject deleteUser(JSONObject jobj) throws ServiceException, JSONException {
//        String result = "{\"success\":false}";
        JSONObject response = new JSONObject();
        User getuser = new User();
        String userid = "";
        boolean flag = false;
        String query = "";
//            JSONObject jobj = new JSONObject(request.getParameter("data"));
        jobj = wsUtilService.populateAdditionalInformation(jobj);
        if (jobj.has("userid")) {
            userid = jobj.getString("userid");
        } else if (jobj.has("username")) {
            userid = jobj.getString("username");
            flag = true;
        }
        if (!flag) {
            String[] uArr = userid.split(",");
            for (int i = 0; i < uArr.length; i++) {
                KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), uArr[i]);
                User u = (User) userResult.getEntityList().get(0);
                if (u != null) {
                    profileHandlerDAOObj.deleteUser(u.getUserID());
                    response.put(Constants.RES_success, true);
                } else {
                    throw ServiceException.FAILURE("User does not exist", "e06", false);
                }
            }
        }
        if (StringUtil.isNullOrEmpty(userid)) {
            throw ServiceException.FAILURE("Mandatory missing", "e01", false);
        }

        return response;
    }
       
    @Override
   @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject verifyLogin(JSONObject paramJobj) throws ServiceException, JSONException {
        if (!paramJobj.has("username")|| !paramJobj.has("pass") || !paramJobj.has(Constants.RES_CDOMAIN)) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        }
        boolean isCompanyExist=wsUtilService.isCompanyExists(paramJobj);
        if (!isCompanyExist) {
            throw ServiceException.FAILURE("Company does not exist", "e04", false);
        }
        JSONObject response = new JSONObject();
        JSONArray jArr = new JSONArray();
         Date finanDate = null, bookdate = null;
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        
        requestParams.put("user", paramJobj.get("username"));
        requestParams.put("pass", paramJobj.get("pass"));
        requestParams.put(Constants.COMPANY_SUBDOMAIN, paramJobj.get(Constants.RES_CDOMAIN));
        
        JSONObject jobj = new JSONObject();
        Company company =null;
        User user=null;
        
        if (!Boolean.parseBoolean(StorageHandler.getStandalone())) {//standalone case
            String platformURL = ConfigReader.getinstance().get(Constants.PLATFORM_URL);
            if (!StringUtil.isNullOrEmpty((String) paramJobj.get("username")) && !StringUtil.isNullOrEmpty((String) paramJobj.get("pass")) && !StringUtil.isNullOrEmpty((String) paramJobj.get(Constants.RES_CDOMAIN)) && !StringUtil.isNullOrEmpty(platformURL)) {
                String companyid = companyDetailsDAOObj.getCompanyid(paramJobj.getString(Constants.RES_CDOMAIN));
                String userid = profileHandlerDAOObj.getUserIdFromUserName((String) paramJobj.get("username"), companyid);
             
                if (!StringUtil.isNullOrEmpty(userid) && !StringUtil.isNullOrEmpty(companyid)) {
                    String authenticatewithApp = "23";//to check username and password
                    JSONObject tempJobj = new JSONObject();
                    tempJobj.put("user", (String) paramJobj.get("username"));
                    tempJobj.put("pass", (String) paramJobj.get("pass"));
                    tempJobj.put(Constants.COMPANY_SUBDOMAIN, (String) paramJobj.get(Constants.RES_CDOMAIN));
                    jobj = apiCallHandlerService.callApp(platformURL, tempJobj,"",authenticatewithApp);

                    if (!jobj.isNull(Constants.RES_success) && jobj.getBoolean(Constants.RES_success)) {
                        requestParams.remove("pass");
                        KwlReturnObject kmsg = authHandlerDAOObj.verifyLogin(requestParams);
                        List<Object[]> loginDetailList = kmsg.getEntityList();
                        if (loginDetailList.size() > 0 && kmsg.isSuccessFlag()) {
                            Object[] row = loginDetailList.get(0);
                            user = (User) row[0];
                            UserLogin userLogin = (UserLogin) row[1];
                            company = (Company) row[2];
                            jobj.put(Constants.username, userLogin.getUserName());
                            jobj.put(Constants.lid, userLogin.getUserID());
                            jobj.put(Constants.useridKey, userLogin.getUserID());
                            jobj.put(Constants.roleid, userLogin.getUser()!=null?(!StringUtil.isNullOrEmpty(userLogin.getUser().getRoleID())?userLogin.getUser().getRoleID():Rolelist.COMPANY_ADMIN):Rolelist.COMPANY_ADMIN);
                        }
                } else {
                    throw ServiceException.FAILURE("UserName or Password is Invalid", "e11", false);
                }
                } else {
                    throw ServiceException.FAILURE("UserName or Password is Invalid", "e11", false);
            }
            }
        } else {
            KwlReturnObject kmsg = authHandlerDAOObj.verifyLogin(requestParams);
            List<Object[]> loginDetailList = kmsg.getEntityList();
            if (loginDetailList.size() > 0 && kmsg.isSuccessFlag()) {
                Object[] row = loginDetailList.get(0);
                user = (User) row[0];
                UserLogin userLogin = (UserLogin) row[1];
                company = (Company) row[2];
                jobj.put(Constants.username, userLogin.getUserName());
                jobj.put(Constants.lid, userLogin.getUserID());
                jobj.put(Constants.useridKey, userLogin.getUserID());
                jobj.put(Constants.roleid, userLogin.getUser()!=null?(!StringUtil.isNullOrEmpty(userLogin.getUser().getRoleID())?userLogin.getUser().getRoleID():Rolelist.COMPANY_ADMIN):Rolelist.COMPANY_ADMIN);
            }
        }
        
        if (company != null && user != null) {
            jobj.put(Constants.companyKey, company.getCompanyID());
            jobj.put(Constants.companyname, company.getCompanyName());
            jobj.put("templateflag", company.getTemplateflag());    //Template Flag for mobile PDF Purpose
            int quantitydigitafterdecimal = 2, amountdigitafterdecimal = 2, unitpricedigitafterdecimal = 2;
            KwlReturnObject accResult = accAccountDAOobj.quotationindecimalforcompany(company.getCompanyID());
            if (accResult.getEntityList().get(0) != null) {
                Object[] decimalcontact = (Object[]) accResult.getEntityList().get(0);
                if (decimalcontact[1] != null) {
                    quantitydigitafterdecimal = (Integer) decimalcontact[1];
                    jobj.put(Constants.quantitydecimalforcompany, quantitydigitafterdecimal);
                }
                if (decimalcontact[2] != null) {//getting amount in decimal value from companyaccpreferences
                    amountdigitafterdecimal = (Integer) decimalcontact[2];
                    jobj.put(Constants.amountdecimalforcompany, amountdigitafterdecimal);

                }
                if (decimalcontact[3] != null) {
                    unitpricedigitafterdecimal = Integer.parseInt(decimalcontact[3].toString());
                    jobj.put(Constants.unitpricedecimalforcompany, unitpricedigitafterdecimal);

                }
            }

            StringBuilder imagePathBuilder =new StringBuilder();
            String baseUrl = com.krawler.common.util.URLUtil.getDomainURL(paramJobj.optString(Constants.RES_CDOMAIN), false);
            imagePathBuilder.append(baseUrl);
            imagePathBuilder.append("video.jsp?id=" + company.getCompanyID() + "_template" + FileUploadHandler.getCompanyImageExt());
            String imagepath = imagePathBuilder.toString();
            jobj.put(Constants.companyImagePath, imagepath);
                                
            KwlReturnObject kresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), company.getCompanyID());
            CompanyAccountPreferences preferences = null;
            if (kresult.getEntityList().size() > 0) {
                preferences = (CompanyAccountPreferences) kresult.getEntityList().get(0);
                if (preferences != null && preferences.getFinancialYearFrom() != null) {
                    finanDate = preferences.getFirstFinancialYearFrom() != null ? preferences.getFirstFinancialYearFrom() : preferences.getFinancialYearFrom();
                    bookdate = preferences.getBookBeginningFrom();
                    jobj.put("financialyeardate", finanDate);
                    jobj.put("bookbeginingdate", bookdate);
                }
            }

            KwlReturnObject ecpresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), company.getCompanyID());
            ExtraCompanyPreferences extrapreferences = null;
            if (ecpresult.getEntityList().size() > 0) {
                extrapreferences = (ExtraCompanyPreferences) ecpresult.getEntityList().get(0);
                if (extrapreferences != null) {
                    jobj.put("allowCustomerCheckInCheckOut", extrapreferences.isAllowCustomerCheckInCheckOut());
                }
            }
            jobj.put("usermailid", user.getEmailID());
            jobj.put(Constants.userfullname, "" + user.getFirstName() + (StringUtil.isNullOrEmpty(user.getLastName()) ? "" : (" " + user.getLastName())));
            jobj.put("userfirstname", user.getFirstName());
            jobj.put("userlastname", user.getLastName());
            String userimagepath = user.getImage() != null ? user.getImage() : "images/store/default.png";
            jobj.put("userprofileimagepath", "/" + userimagepath);
            jobj.put("countryid", company.getCountry().getID());
            jobj.put("countrycode", company.getCountry().getCountryCode());

            KWLTimeZone timeZone = user.getTimeZone();
            if (timeZone == null) {
                timeZone = company.getTimeZone();
            }
            if (timeZone == null) {
                timeZone = (KWLTimeZone) (KWLTimeZone) kwlCommonTablesDAOObj.getClassObject(KWLTimeZone.class.getName(), storageHandlerImpl.getDefaultTimeZoneID());
            }
            jobj.put(Constants.timezoneid, timeZone.getTimeZoneID());
            jobj.put("timeZId", timeZone.getTzID());
            jobj.put("tzdiff", timeZone.getDifference());
            jobj.put("companytzdiff", company.getTimeZone() != null ? company.getTimeZone().getDifference() : timeZone.getDifference());
            jobj.put(Constants.timezonedifference, company.getTimeZone() != null ? company.getTimeZone().getDifference() : timeZone.getDifference());

            Language lang = company.getLanguage();
            if (lang != null) {
                jobj.put(Constants.language, lang.getLanguageCode() + (lang.getCountryCode() != null ? "_" + lang.getCountryCode() : ""));
            } else {
                jobj.put(Constants.language, Constants.RES_DEF_LANGUAGE);
            }
            //Put locale object
            Locale locale = Locale.forLanguageTag(jobj.getString(Constants.language));
            jobj.put(Constants.locale, locale);

            KWLDateFormat dateFormat = user.getDateFormat();
            if (dateFormat == null) {
                dateFormat = (KWLDateFormat) kwlCommonTablesDAOObj.getClassObject(KWLDateFormat.class.getName(), storageHandlerImpl.getDefaultDateFormatID());
            }
            jobj.put("DateFormat", dateFormat.getScriptForm());
            jobj.put(Constants.dateformatid, dateFormat.getFormatID());
            jobj.put(Constants.userdateformat, dateFormat.getJavaForm());

            KWLCurrency currency = company.getCurrency();
            if (currency == null) {
                currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), storageHandlerImpl.getDefaultCurrencyID());
            }
            jobj.put(Constants.globalCurrencyKey, currency.getCurrencyID());
            jobj.put(Constants.timeformat, user.getTimeformat());
            jobj.put(Constants.RES_CDOMAIN, paramJobj.get(Constants.RES_CDOMAIN));

            jArr.put(jobj);
        } //end of null check of user and company   
        
        if (jArr.length() > 0) {
            response.put(Constants.RES_data, jArr);
            response.put(Constants.RES_TOTALCOUNT, jArr.length());
            response.put(Constants.RES_success, true);
        }else{
            throw ServiceException.FAILURE("UserName or Password is Invalid", "e11", false);
        }
        return response;
    }  
    
    private JSONObject activateDeactivateUser(JSONObject paramJobj) throws SQLException, ServiceException, JSONException {
        String msg = "";
        JSONObject response = new JSONObject();
        boolean issuccess = false;
        try {
            String userid = "";
            boolean flag = false;
//            JSONObject jobj = new JSONObject(paramJobj.optString(Constants.data, "[]"));
            if (paramJobj.has("userid")) {
                userid = paramJobj.getString("userid");
            } else if (paramJobj.has("username")) {
                userid = paramJobj.getString("username");
                flag = true;
            }
            if (StringUtil.isNullOrEmpty(userid)) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }

            if (!StringUtil.isNullOrEmpty(userid)) {
//                KwlReturnObject kmsg = profileHandlerDAOObj.getUserWithUserName(userid, flag);
//                List<UserLogin> userList = kmsg.getEntityList();
//                if (userList.size() > 0) {
//                    for (UserLogin userObj : userList) {
//                        userid = userObj.getUserID();
//                    }
//                } else {
//                    throw ServiceException.FAILURE("Missing required field", "e01", false);
//                }
                String[] uids = userid.split(",");
                for (int i = 0; i < uids.length; i++) {
                    KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), uids[i]);
                    User getuser = (User) userResult.getEntityList().get(0);
                    HashMap<String, Object> userHashMap = new HashMap<>();
                    userHashMap.put("userid", uids[i]);
                    if (getuser != null) {
                        userHashMap.put("deleteflag", paramJobj.optInt("deleteflag"));
                        issuccess = true;
                        msg = "User added successfully";
                        KwlReturnObject rtObj = profileHandlerDAOObj.addUser(userHashMap);
                    } else {
                        throw ServiceException.FAILURE("Missing required field", "e01", false);
                    }
                }
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("comapanyServlet.activateDeactivateUser:" + e.getMessage(), e);
        } finally {
            response.put(Constants.RES_success, issuccess);
            response.put(Constants.RES_msg, msg);
        }
        return response;
    }

    @Override
    public JSONObject activateUser(JSONObject paramJobj) throws SQLException, ServiceException, JSONException{
        paramJobj.put("deleteflag", 0);
        return activateDeactivateUser(paramJobj);
    }
   
    @Override
    public JSONObject deactivateUser(JSONObject paramJobj) throws SQLException, ServiceException, JSONException{
        paramJobj.put("deleteflag", 1);
        return activateDeactivateUser(paramJobj);
    }
    
    @Override
    public JSONObject assignRole(JSONObject paramJobj) throws SQLException, ServiceException, JSONException {
        String msg = "";
        JSONObject response = new JSONObject();
        boolean issuccess = false;
        try {
            if(paramJobj.has(Constants.RES_data)){
                JSONArray roleArr = paramJobj.getJSONArray(Constants.RES_data);
                for (int i = 0; i < roleArr.length(); i++) {
                    JSONObject tempObj = roleArr.getJSONObject(i);
                    String userid = tempObj.getString("userid");
                    String roleStr = tempObj.getString("role");
                    String companyid = paramJobj.isNull("companyid") ? "" : paramJobj.getString("companyid");
                    KwlReturnObject kmsg = profileHandlerDAOObj.getUserWithUserName(userid, false);
                    int count = kmsg.getRecordTotalCount();
                    if (count > 0) {
                        String roleid = roleStr.equals("a0") ? Role.COMPANY_ADMIN : (roleStr.equals("a1") ? Role.COMPANY_ADMIN : Role.COMPANY_USER);
                        permissionHandlerDAOObj.assignRoles(userid, roleid);
                        if (!roleid.equals(Role.COMPANY_ADMIN)) {
                            HashMap<String, Object> requestParams = new HashMap<>();
                            requestParams.put("userid", userid);
                            requestParams.put("roleid", roleid);
                            requestParams.put("companyid", companyid);
                            KwlReturnObject krObj = permissionHandlerDAOObj.getUserPermission(requestParams);
                            int j = 0;
                            List<Object[]> rows = krObj.getEntityList();
                            String[] features = new String[krObj.getRecordTotalCount()];
                            String[] permissions = new String[krObj.getRecordTotalCount()];
                            for (Object[] row : rows) {
                                features[i] = row[2].toString();
                                permissions[j] = row[1].toString();
                                j++;
                            }
                            requestParams.put("userid", userid);
                            requestParams.put("roleid", roleid);
                            permissionHandlerDAOObj.setPermissions(requestParams, features, permissions);
                        }
                        issuccess = true;
                        msg = "Role assigned successfully";
                    } else {
                        throw ServiceException.FAILURE("User doesn't exist", "e06", false);
                    }
                }
            }
            else{
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        } finally {
            response.put(Constants.RES_success, issuccess);
            response.put(Constants.RES_msg, msg);
        }
        return response;
    }

    @Override
    public JSONObject createUser(JSONObject paramJobj) throws SQLException, ServiceException, JSONException {
        String msg = "";
        JSONObject response = new JSONObject();
        boolean issuccess = false;
        try {
            
            String pwdText = "";
            JSONObject jobj = paramJobj;
//                    new JSONObject(paramJobj.optString(Constants.data, "[]"));
            String userid = jobj.isNull("userid") ? "" : jobj.getString("userid");
            String companyid = jobj.isNull("companyid") ? "" : jobj.getString("companyid");
            String username = jobj.isNull("username") ? "" : jobj.getString("username");
            String pwd = jobj.isNull("password") ? "" : jobj.getString("password");
            String fname = jobj.isNull("fname") ? "" : jobj.getString("fname");
            String lname = jobj.isNull("lname") ? "" : jobj.getString("lname");
            String emailid = jobj.isNull("emailid") ? "" : jobj.getString("emailid");
            String address = jobj.isNull("address") ? "" : jobj.getString("address");
            String contactno = jobj.isNull("contactno") ? "" : jobj.getString("contactno");
            String department = jobj.isNull("department") ? "" : jobj.getString("department");
            String designation = jobj.isNull("designation") ? "" : jobj.getString("designation");
            String employeeid = jobj.isNull("emplyoeeid") ? "" : jobj.getString("emplyoeeid");

            if (StringUtil.isNullOrEmpty(companyid) || StringUtil.isNullOrEmpty(username) || StringUtil.isNullOrEmpty(fname)
                    || StringUtil.isNullOrEmpty(lname) || StringUtil.isNullOrEmpty(emailid)) {
                throw ServiceException.FAILURE("Missing required field", "e01", false);
            }
            List list = accCompanyPreferencesObj.isCompanyExistWithCompanyID(jobj.getString("companyid"));
            if (list.isEmpty()) {
                throw ServiceException.FAILURE("Company Does not Exist", "e04", false);
            }
            try {
                List usl = profileHandlerDAOObj.getUserExistWithUserID(userid);
                if (usl.size() > 0) {
                    throw ServiceException.FAILURE("User already exist", "e07", false);
                }
            } catch (Exception e) {
            }
            if (jobj.isNull("password")) {
                pwdText = StringUtil.generateNewPassword();
                pwd = StringUtil.getSHA1(pwdText);
            }
            boolean isUserExist = profileHandlerDAOObj.isUserExist(username, companyid);
            if (isUserExist) {
                throw ServiceException.FAILURE("User already exist", "e07", false);
            }
            KwlReturnObject roleListResult = accountingHandlerDAOobj.getObject(Rolelist.class.getName(), Role.COMPANY_USER);
            Rolelist roleList = (Rolelist) roleListResult.getEntityList().get(0);

            KwlReturnObject dateformatObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), "18");
            KWLDateFormat dateFormat = (KWLDateFormat) dateformatObj.getEntityList().get(0);

            HashMap<String, Object> userHashMap = new HashMap<>();
            userHashMap.put("userid", userid);
            userHashMap.put("company", companyid);
            userHashMap.put("firstName", fname);
            userHashMap.put("lastName", lname);
            userHashMap.put("emailID", emailid);
            userHashMap.put("address", address);
            userHashMap.put("contactno", contactno);
            userHashMap.put("department", department);
            userHashMap.put("designation", designation);
            userHashMap.put("employeeid", employeeid);
            userHashMap.put("dateformat", "18");
            userHashMap.put("appid", "3");// ERP Application ID
            userHashMap.put("iscommit", true);
            userHashMap.put("password", pwd);
            userHashMap.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userHashMap.put("role", Role.COMPANY_USER);
            KwlReturnObject rtObj = profileHandlerDAOObj.addUser(userHashMap);
            User user = (User) rtObj.getEntityList().get(0);

            HashMap<String, Object> userLoginHashmap = new HashMap<>();
            userLoginHashmap.put("userName", username);
            userLoginHashmap.put("password", pwd);
            userLoginHashmap.put("user", user);
            userLoginHashmap.put("userid", user.getUserID());
            userLoginHashmap.put("saveStandAloneUserLogin", true);
            String logdate = authHandler.getGlobalDateFormat().format(new Date());
            Date loginDate = authHandler.getGlobalDateFormat().parse(logdate);
            userLoginHashmap.put("lastlogindate", loginDate);
            profileHandlerDAOObj.saveUserLogin(userLoginHashmap);

            HashMap<String, Object> roleusermap = new HashMap<String, Object>();
            roleusermap.put("user", user);
            roleusermap.put("roleid", roleList.getRoleid());
            profileHandlerDAOObj.saveRoleUserMapping(roleusermap);

            if (jobj.has("sendmail") && jobj.getBoolean("sendmail") && !StringUtil.isNullOrEmpty(paramJobj.optString(Constants.url, null))) {
                KwlReturnObject result1 = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) result1.getEntityList().get(0);
                User creater = (User) (company.getCreator());
                String fullnameCreator = StringUtil.getFullName(creater);
                String uri = paramJobj.optString(Constants.url);
                String passwordString = "";
                if (jobj.isNull("password")) {
                    passwordString = "\n\nUsername: " + username + " \nPassword: " + pwdText;
                }
                String msgMailInvite = "Hi %s,\n\n%s has created an account for you at Deskera Accounting.\n\nDeskera Accounting is an Account Management Tool which you'll love using." + passwordString + "\n\nYou can log in at:\n%s\n\n\nSee you on Deskera Accounting\n\n - %s and The Deskera Acconting Team";
                String pmsg = String.format(msgMailInvite, user.getFirstName(), fullnameCreator, uri, fullnameCreator);
                if (jobj.isNull("password")) {
                    passwordString = "		<p>Username: <strong>%s</strong> </p>"
                            + "               <p>Password: <strong>%s</strong></p>";
                }
                String msgMailInviteUsernamePassword = "<html><head><title>Deskera Accounting - Your Deskera Account</title></head><style type='text/css'>"
                        + "a:link, a:visited, a:active {\n"
                        + " 	color: #03C;"
                        + "}\n"
                        + "body {\n"
                        + "	font-family: Arial, Helvetica, sans-serif;"
                        + "	color: #000;"
                        + "	font-size: 13px;"
                        + "}\n"
                        + "</style><body>"
                        + "	<div>"
                        + "		<p>Hi <strong>%s</strong>,</p>"
                        + "		<p>%s has created an account for you at %s.</p>"
                        + "             <p>Deskera Accounting is an Account Management Tool which you'll love using.</p>"
                        + passwordString
                        + "		<p>You can log in to Deskera Acconting at: <a href=%s>%s</a>.</p>"
                        + "		<br/><p>See you on Deskera Accounting!</p><p> - %s and The Deskera Accounting Team</p>"
                        + "	</div></body></html>";
                String htmlmsg = String.format(msgMailInviteUsernamePassword, user.getFirstName(), fullnameCreator, company.getCompanyName(), uri, uri, fullnameCreator);
                try {
                    Map<String, Object> smtpConfigMap = authHandler.getSMTPConfigMap(company);
                    SendMailHandler.postMail(new String[]{user.getEmailID()}, "[Deskera] Welcome to Deskera Accounting", htmlmsg, pmsg, creater.getEmailID(), smtpConfigMap);
                    issuccess = true;
                    msg = "Mail has been sent successfully";
                } catch (MessagingException e) {
                    throw ServiceException.FAILURE("Error while sending mail", "e05", false);
                }
            }
            issuccess = true;
        } catch (Exception e) {
            throw ServiceException.FAILURE("Missing required field", "e01", false);
        } finally {
            response.put(Constants.RES_success, issuccess);
            response.put(Constants.RES_msg, msg);
        }
        return response;
    }
    
    /**
     * Method used to parepare JSON required for audit trail rest method.
     *
     * @param paramJobj
     * @return
     * @throws JSONException
     * @throws ServiceException
     * @throws SessionExpiredException
     * @throws ParseException
     */
    @Override
    public JSONObject getAuditTrails(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException, ParseException {
        JSONObject response = new JSONObject();
        JSONObject jObj = new JSONObject();
        KwlReturnObject kmsg = null;
        jObj = wsUtilService.populateAdditionalInformation(paramJobj);

        if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.username))) {
            paramJobj.put(Constants.useridKey, jObj.opt(Constants.useridKey));
        } else {
            response.put(Constants.RES_success, false);
            response.put(Constants.RES_MESSAGE, "username is missing");
        }

        /**
         * If Here Sitll userid is null then in else part shows username does
         * not exist message.
         */
        if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.useridKey))) {
            KwlReturnObject userclass = accountingHandlerDAOobj.getObject(User.class.getName(), paramJobj.optString(Constants.useridKey));
            User user = (User) userclass.getEntityList().get(0);
            String username = user.getUserLogin().getUserName();
            boolean isUserExists = profileHandlerDAOObj.isUserExist(username, paramJobj.optString(Constants.companyid));
            SimpleDateFormat df = new SimpleDateFormat(Constants.yyyyMMdd);
            if (isUserExists) {

                /**
                 * For getting username of Administrator.
                 */
                KwlReturnObject returnObject = accountingHandlerDAOobj.getObject(Company.class.getName(), paramJobj.optString(Constants.companyid));
                Company company = (Company) returnObject.getEntityList().get(0);
                User creator = company.getCreator();
                returnObject = accountingHandlerDAOobj.getObject(UserLogin.class.getName(), creator.getUserID());
                UserLogin userlogin = (UserLogin) returnObject.getEntityList().get(0);
                String admin = userlogin.getUserName();

                /**
                 * If user is not deleted then deleteFlag is 0 otherwise
                 * deleteFlag is 1.
                 *
                 */
                int deleteFlag = user.getDeleteflag();
                if (deleteFlag == 0) {
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    int start = paramJobj.optInt("start", 0);
                    requestParams.put("start", start);
                    int limit = paramJobj.optInt("limit", 15);
                    requestParams.put("limit", limit);
                    requestParams.put("companyid", paramJobj.optString("companyid", ""));
                    if (paramJobj.has("groupid")) {
                        requestParams.put("groupid", paramJobj.optString("groupid", null));
                    }
                    if ((paramJobj.has("fromdate") && !StringUtil.isNullOrEmpty(paramJobj.optString("fromdate")))) {
                        Date sDate = df.parse(paramJobj.opt("fromdate").toString());
                        requestParams.put("stDate", sDate);
                    }
                    if (paramJobj.has("todate") && !StringUtil.isNullOrEmpty(paramJobj.optString("todate"))) {
                        Date eDate = df.parse(paramJobj.opt("todate").toString());
                        requestParams.put("eDate", eDate);
                    }

                    /**
                     * Checking whether username equal to administrator or not.
                     * if equal then further condition is added in method
                     * getAuditData() method. ref userid check in method
                     * getAuditData().
                     */
                    if (!admin.equals(username)) {
                        requestParams.put(Constants.useridKey, paramJobj.optString(Constants.useridKey));
                    }

                    /**
                     * Call to getAuditData gives Audit trail data.
                     */
                    kmsg = auditTrailDAOObj.getAuditData(requestParams);

                    JSONObject paramObj = new JSONObject();
                    paramObj.put(Constants.timezonedifference, jObj.optString(Constants.timezonedifference));
                    paramObj.put(Constants.userdateformat, jObj.optString("userdateform"));
                    paramObj.put(Constants.username, paramJobj.optString(Constants.username));
                    /**
                     * call to getAuditJSONData gives JSON data of audit trail.
                     */
                    JSONObject auditTraildata = accAuditTrialServiceCMN.getAuditJSONData(kmsg.getEntityList(), paramObj, kmsg.getRecordTotalCount());
                    JSONArray jArray = auditTraildata.optJSONArray("data");
                    JSONArray responseArray = new JSONArray();
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject getAuditJson = jArray.getJSONObject(i);
                        JSONObject prepareResponse = new JSONObject();
                        prepareResponse.put("timestamp", getAuditJson.opt("timestamp"));
                        prepareResponse.put("transactionid", getAuditJson.opt("id"));
                        prepareResponse.put("details", getAuditJson.opt("details"));
                        prepareResponse.put("ipaddr", getAuditJson.opt("ipaddr"));
                        prepareResponse.put("user", getAuditJson.opt("username"));
                        responseArray.put(prepareResponse);
                    }
                    response.put(Constants.RES_success, true);
                    response.put(Constants.data, responseArray);
                } else {
                    response.put(Constants.RES_success, false);
                    response.put(Constants.RES_MESSAGE, "User is delete user.");
                }
            } else {
                response.put(Constants.RES_success, false);
                response.put(Constants.RES_MESSAGE, "User does NOT exist.");
            }
        } else {
            /**
             * If here userid is Still null or empty then User does not exist
             * because already check is applied for userid in
             * auditTrailDetails() method
             *
             */
            response.put(Constants.RES_success, false);
            response.put(Constants.RES_MESSAGE, "username does NOT exist");
        }
        return response;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject createCompany(JSONObject paramObj) throws SQLException, ServiceException, JSONException{
        String result = "failure";
        JSONObject response = new JSONObject();
        response.put(Constants.RES_success, false);
//        paramObj = wsUtilService.populateAdditionalInformation(paramObj);
            String companyid = paramObj.isNull("companyid") ? "" : paramObj.getString("companyid");
            String lname = paramObj.isNull("lname") ? "" : paramObj.getString("lname");
            String userid = paramObj.isNull("userid") ? "" : paramObj.getString("userid");
            String subdomain = paramObj.isNull("subdomain") ? "" : paramObj.getString("subdomain");
            String userid2 = paramObj.isNull("username") ? "" : paramObj.getString("username");
            String emailid2 = paramObj.isNull("emailid") ? "" : paramObj.getString("emailid");
            String password = paramObj.isNull("password") ? "" : paramObj.getString("password");
            String companyname = paramObj.isNull("companyname") ? "" : paramObj.getString("companyname");
            String fname = paramObj.isNull("fname") ? "" : paramObj.getString("fname");
            int referralkey = paramObj.isNull(Constants.REFERRALKEY) ? 0 : paramObj.getInt(Constants.REFERRALKEY);
            String currency = paramObj.isNull("currency") ? StorageHandler.getDefaultCurrencyID() : paramObj.getString("currency");
            String country = paramObj.isNull("country") ? "244" : paramObj.getString("country");
            String timezone = paramObj.isNull("timezone") ? "23" : paramObj.getString("timezone");
            if (StringUtil.isNullOrEmpty(companyname) || StringUtil.isNullOrEmpty(userid2)
                    || StringUtil.isNullOrEmpty(fname) || StringUtil.isNullOrEmpty(emailid2)) {
                throw ServiceException.FAILURE("Mandatory missing", "e01", false);
            }
            String pwdtext = "";
            if (paramObj.isNull("password")) {
                pwdtext = StringUtil.generateNewPassword();
            try {
                password = StringUtil.getSHA1(pwdtext);
            } catch (UnsupportedEncodingException ex) {
                Logger.getLogger(CompanyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchAlgorithmException ex) {
                Logger.getLogger(CompanyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            }
            if (!(StringUtil.isNullOrEmpty(userid2) || StringUtil.isNullOrEmpty(emailid2))) {
                emailid2 = emailid2.replace(" ", "+");
//            try {
                result = signupCompany( companyid, userid, userid2, password, emailid2, companyname, fname, subdomain, lname, currency, country, timezone, referralkey, paramObj);
//                result = companyProfileObj.signupCompany( companyid, userid, userid2, password, emailid2, companyname, fname, subdomain, lname, currency, country, timezone, referralkey);
//            } catch (SessionExpiredException ex) {
//                Logger.getLogger(CompanyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
//            }
                if (result.equalsIgnoreCase("success")) {
                    response.put(Constants.RES_success, true);
                } else {
                    response.put(Constants.RES_success, false);
                }
            }
            return response;
    }
@Transactional(propagation = Propagation.REQUIRED)
    private String signupCompany(String companyid, String userid, String id, String password, String emailid, String companyname,
            String fname, String subdomain, String lname, String currencyid, String countryid, String timezoneid, int referralkey, JSONObject paramObj)
            throws ServiceException {
        String result = "failure";

        try {
            Company company = null;
            UserLogin userLogin = null;
            User user = null;
            List<Company> companyList = new ArrayList<>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            HashMap<String, Object> companyRequestParams = new HashMap<>();
            filter_names.add("c.subDomain");
            filter_params.add(subdomain);
            companyRequestParams.put("filter_names", filter_names);
            companyRequestParams.put("filter_values", filter_params);
            KwlReturnObject companyListObj = accCommonTablesDAO.getCompany(companyRequestParams);
            if (companyListObj.getEntityList().size() > 0) {
                companyList = companyListObj.getEntityList();
            }

            Iterator itr11 = companyList.iterator();
            if (itr11.hasNext()) {
                Company oldcompany = (Company) itr11.next();
                HashMap<String, Object> companyHashMap = new HashMap<>();
                companyHashMap.put("companyid", oldcompany.getCompanyID());
                companyHashMap.put("subdomain", "old_" + oldcompany.getSubDomain());
                KwlReturnObject cmprtObj = profileHandlerDAOObj.updateCompany(companyHashMap);
            }
            if (!StringUtil.isNullOrEmpty(userid)) {
                KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), userid);
                user = (User) userResult.getEntityList().get(0);
                if (user != null) {
                    return "failure";
                }
            }
            KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            company = (Company) companyResult.getEntityList().get(0);
            if (company != null) {
                return "failure";
            }

            KwlReturnObject countryResult = accountingHandlerDAOobj.getObject(Country.class.getName(), countryid);
            Country country = (Country) countryResult.getEntityList().get(0);

            KwlReturnObject currencyResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) currencyResult.getEntityList().get(0);

            KwlReturnObject roleListResult = accountingHandlerDAOobj.getObject(Rolelist.class.getName(), Role.COMPANY_ADMIN);
            Rolelist roleList = (Rolelist) roleListResult.getEntityList().get(0);
            
            KwlReturnObject tzResult = accountingHandlerDAOobj.getObject(KWLTimeZone.class.getName(), timezoneid);
            KWLTimeZone tz = (KWLTimeZone) tzResult.getEntityList().get(0);
            
            KwlReturnObject dfResult = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), "2");
            KWLDateFormat df = (KWLDateFormat) dfResult.getEntityList().get(0);
            
            company = new Company();
            user = new User();
            userLogin = new UserLogin();
            user.setUserID(userid);
            
            company.setCompanyID(companyid);
            
            company.setAddress(paramObj.optString("address", ""));
            company.setDeleted(0);
            Date curdate = authHandler.getDateOnlyFormat().parse(authHandler.getConstantDateFormatter(null).format(new Date()));
            company.setCreatedOn(curdate);
            company.setModifiedOn(curdate);
            company.setSubDomain(subdomain);
            company.setCompanyName(companyname);
            company.setCountry(country);
            company.setTimeZone(tz);
            company.setEmailID(emailid);
            company.setCurrency(currency);
            company.setActivated(true);
            company.setSwitchpref(1);
            company.setStoreinvoiceamountdue(true);
            company.setReferralkey(referralkey);
            company.setCity(paramObj.optString("city",""));
            company.setPhoneNumber(paramObj.optString("phone",""));
            company.setFaxNumber(paramObj.optString("fax",""));
            company.setZipCode(paramObj.optString("zip",""));
            company.setIsSelfService(paramObj.optInt("selfservice",0)); // ERP-39090 Maintain selfservice flag at DB level to distinguish self service companies.
            String statename = paramObj.optString("state","");
            if(!StringUtil.isNullOrEmpty(statename)){
                String stateid = profileHandlerDAOObj.getStateidByStateName(statename);
                State state = (State) kwlCommonTablesDAOObj.getClassObject(State.class.getName(), stateid);
                if(state!=null){
                    company.setState(state);
                }
            }

            userLogin.setUser(user);
            user.setRoleID(Role.COMPANY_ADMIN);
            
            RoleUserMapping rmapping = new RoleUserMapping();
            rmapping.setRoleId(roleList);
            rmapping.setUserId(user);
            kwlCommonTablesDAOObj.saveObj(rmapping);
            
            user.setDateFormat(df);//yyyy-mm-dd
            user.setFirstName(fname);
            user.setTimeZone(tz);
            user.setLastName(lname);
            user.setEmailID(emailid);
            user.setAddress("");
            user.setCompany(company);
//            userLogin.setUserID(userid);
            userLogin.setUserName(id);
            userLogin.setPassword(password);

            kwlCommonTablesDAOObj.saveObj(company);
            kwlCommonTablesDAOObj.saveObj(user);
            kwlCommonTablesDAOObj.saveObj(userLogin);
            
            KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(Company.class.getName(),companyid);
            company = (Company) cap.getEntityList().get(0);
            cap = kwlCommonTablesDAOObj.getObject(User.class.getName(),userid);
            user = (User) cap.getEntityList().get(0);
//            user.setCompany(company);
            company.setCreator(user);
//            user.setCompany(company);
//            company.setCreator(user);
            kwlCommonTablesDAOObj.saveorUpdateObj(company);
//            status = txnManager.getTransaction(def);
//            txnManager.commit(status);
//            request.setAttribute("currencyid", currencyid);
           
            setupNewCompany(subdomain, company, user, currencyid);
            result = "success";
        } catch (Exception e) {
            e.printStackTrace();
            result = "failure";
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return result;
    }
    @Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_UNCOMMITTED)
    private void setupNewCompany(String subdomain, Company company, User companyAdmin, String currencyid) throws SessionExpiredException, JSONException, ServiceException {
        try {
            String companyid = company.getCompanyID();
            accTermObj.copyTerms(companyid);            
            accUomObj.copyUOM(companyid, new HashMap<String, Object>());
            KwlReturnObject kresult = accAccountDAOobj.copyAccounts(companyid, currencyid, null, null, null, null, false);
            HashMap hmAcc = (HashMap) kresult.getEntityList().get(0);
            accPaymentDAOobj.copyPaymentMethods(companyid, hmAcc);
            accMasterItemsDAOobj.copyMasterItems(companyid, hmAcc);
            accCompanyPreferencesObj.setAccountPreferences(companyid, hmAcc, getCurrentDate());
            accCompanyPreferencesObj.saveDefaultSequenceFormat(company);
            accTaxObj.copyTax1099Category(companyid);
        } catch (Exception e) {
            Logger.getLogger(CompanyServiceImpl.class.getName()).log(Level.SEVERE, e.getMessage());
            JSONObject companydomain = new JSONObject();
            companydomain.put(Constants.RES_CDOMAIN, subdomain);
            deleteCompany(companydomain);        
        }
    }      
    
    @Override
    public JSONObject getAllUserDetails(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject result = new JSONObject();
        try {
            result = profileHandlerServiceObj.getAllUserDetails(paramJobj);
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public JSONObject getAllDateFormats(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject result = new JSONObject();
        try {
            result = kwlCommonTablesService.getAllDateFormats(paramJobj);
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return result;
    }
    
    @Override
    public JSONObject getAllTimeZones(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject result = new JSONObject();
        try {
            result =kwlCommonTablesService.getAllTimeZones(paramJobj);
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public JSONObject saveUsers(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject result = new JSONObject();
        try {
            HashMap hm = null;
            HashMap<String, Object> requestMap = commonFnControllerService.generateMapJSON(paramJobj);
            result = commonFnControllerService.saveUsers(requestMap, paramJobj,hm);
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return result;
    }
    
    @Override
    public JSONObject getUrls(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject result = new JSONObject();
        try {
            if (paramJobj.has("appsurl") && paramJobj.optString("appsurl").equalsIgnoreCase(Constants.PLATFORM_URL)) {
                String platformURL = ConfigReader.getinstance().get(Constants.PLATFORM_URL);
                result.put(Constants.PLATFORM_URL, platformURL);
                result.put(Constants.RES_success, true);
            }

            if (paramJobj.has("appsurl") && paramJobj.optString("appsurl").equalsIgnoreCase("crmURL")) {
                String crmURL = ConfigReader.getinstance().get("crmURL");
                result.put("crmURL", crmURL);
                result.put(Constants.RES_success, true);
            }

            if (paramJobj.has("appsurl") && paramJobj.optString("appsurl").equalsIgnoreCase("eclaimURL")) {
                String eclaimURL = ConfigReader.getinstance().get("eclaimURL");
                result.put("eclaimURL", eclaimURL);
                result.put(Constants.RES_success, true);
            }

            if (paramJobj.has("appsurl") && paramJobj.optString("appsurl").equalsIgnoreCase("lmsURL")) {
                String lmsURL = ConfigReader.getinstance().get("lmsURL");
                result.put("lmsURL", lmsURL);
                result.put(Constants.RES_success, true);
            }
            if (paramJobj.has("appsurl") && paramJobj.optString("appsurl").equalsIgnoreCase("accURL")) {
                String accURL = ConfigReader.getinstance().get("accURL");
                result.put("accURL", accURL);
                result.put(Constants.RES_success, true);
            }
            if (paramJobj.has("appsurl") && paramJobj.optString("appsurl").equalsIgnoreCase("pmURL")) {
                String pmURL = ConfigReader.getinstance().get("pmURL");
                result.put("pmURL", pmURL);
                result.put(Constants.RES_success, true);
            }

        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return result;
    }
    /**
     * Get GST Configuration : 
     * 1) Master Data : FiledParams, FieldComboData 
     * 2) Company Line Level Term : LineLevelTerm 
     * 3) Get Product Term Entity Base Rate : EntityBasedLineLevelTerm 
     * 4) Get Product Category GST Rule Map Details : ProductCategoryGstRulesMappping.
     * @param paramJobj
     * @return 
     * @throws com.krawler.common.service.ServiceException
     * @throws com.krawler.utils.json.base.JSONException
     */
    @Override
    public JSONObject getGSTConfiguration(JSONObject paramJobj) throws ServiceException, JSONException {
        JSONObject result = new JSONObject();
        JSONObject data = new JSONObject();
        JSONObject masterData = new JSONObject();
        JSONArray entityBasedLineLevelTermRate = new JSONArray();
        JSONArray productCategoryGSTRuleMap = new JSONArray();
        JSONArray companyLineLevelTermsArr = new JSONArray();
        try {
            KwlReturnObject kwlReturnObject  = null;
            String companyid = "";
            Map<String, Object> reqParams = new HashMap();
            if (paramJobj.has(Constants.companyid)) {
                companyid = paramJobj.optString(Constants.companyid, "");
            }
            reqParams.put(Constants.companyid, companyid);
            
            /**
             * Get Master Data : FieldParams and FieldComboData.
             */
            KwlReturnObject kresult = accAccountDAOobj.getMasterDataForGSTFields(paramJobj);
            masterData = accEntityGstService.fetchMasterDataForGSTFields(kresult.getEntityList(),reqParams);
            result.put(Constants.GST_MASTERDATA, masterData);

            /**
             * Get Company Line Level Term.
             */
            HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put(Constants.SALESORPURCHASE_FLAG, "true");
            hashMap.put(Constants.TERM_TYPE, String.valueOf(Constants.GST_TERM_TYPE));
            hashMap.put(Constants.companyid, companyid);
            companyLineLevelTermsArr = accProductObj.getCompanyTermsJsonArray(hashMap);
            result.put(IndiaComplianceConstants.COMPANY_LINELEVEL_TERMS, companyLineLevelTermsArr);
            /**
             * Get Product Term Entity Base Rate. 
             */
            reqParams.put(Constants.SALESORPURCHASE_FLAG, true);
            kwlReturnObject = accEntityGstDao.getEntitybasedLineLevelTermRate(reqParams);
            if (kwlReturnObject.getEntityList() != null && kwlReturnObject.getEntityList().size() > 0 && kwlReturnObject.getEntityList().get(0) != null) {
                ArrayList<EntitybasedLineLevelTermRate> productTermDetail = (ArrayList<EntitybasedLineLevelTermRate>) kwlReturnObject.getEntityList();
                entityBasedLineLevelTermRate = accEntityGstService.fetchEntityBasedLineLevelTermRate(productTermDetail, reqParams);
            }
            result.put(Constants.GST_ENTITYBASED_LINELEVEL_TERMRATE, entityBasedLineLevelTermRate);
            /**
             * Get Product Category GST Rule Map Details.
             */
            kwlReturnObject = accEntityGstDao.getProductCatgoryGSTRuleMap(reqParams);
            if (kwlReturnObject.getEntityList() != null && kwlReturnObject.getEntityList().size() > 0 && kwlReturnObject.getEntityList().get(0) != null) {
                ArrayList<ProductCategoryGstRulesMappping> prodCatGSTRuleMap = (ArrayList<ProductCategoryGstRulesMappping>) kwlReturnObject.getEntityList();
                productCategoryGSTRuleMap = accEntityGstService.fetchProductCategoryGSTRuleMapDetails(prodCatGSTRuleMap, reqParams);
            }
            result.put(Constants.GST_PRODUCTCATEGORY_GSTRULES_MAPPPINGS, productCategoryGSTRuleMap);
            data.put(Constants.RES_data, result);
            data.put(Constants.RES_success, true);
        } catch (ServiceException | JSONException e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return data;
    }
    
    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject saveDeskeraProxyDetails(JSONObject jobj) throws JSONException, ServiceException {
        JSONObject result = new JSONObject();
        try {
            String id = jobj.getString("requestid");
            String companyid = jobj.getString("companyid");
            String description = jobj.getString("description");
            String callBackURL = jobj.getString("callBackURL");
            String dataIds = jobj.getString("dataIds");
            Date date = new Date();
            if (!StringUtil.isNullOrEmpty(dataIds) && !StringUtil.isNullOrEmpty(id)) {
                DeskeraProxyDetails dpd = new DeskeraProxyDetails();
                dpd.setID(id);
                dpd.setDescription(description);
                dpd.setCallBackURL(callBackURL);
                dpd.setParameters(dataIds);
                dpd.setCompanyid(companyid);
                dpd.setStatus(0);//0 - pending response on callback URL | 1 - successfull response 
                dpd.setUpdatedOn(date);
                kwlCommonTablesDAOObj.saveObj(dpd);
                result.append("success", true);
                result.append("requestid", id);
            }
        } catch (Exception e) {
            e.printStackTrace();
            result.append("success", false);
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return result;
    }
    /**
     * Rest service method to save password policy.
     * @param paramJobj
     * @return
     * @throws JSONException
     * @throws ServiceException 
     */
    @Override
    public JSONObject saveOrUpdatePasswordPolicy(JSONObject paramJobj) throws JSONException, ServiceException {
        JSONObject response = new JSONObject();
        String msg = "";
        try {
            paramJobj.put(Constants.useridKey, paramJobj.optString("loginuserid"));
            paramJobj = wsUtilService.populateAdditionalInformation(paramJobj);
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("policyid"))) {
                response = profileHandlerServiceObj.saveOrUpdatePasswordPolicy(paramJobj);
            } else {
                response.put(Constants.RES_success, false);
                response.put(Constants.RES_MESSAGE, messageSource.getMessage("acc.field.Insufficientdata", null, (Locale) paramJobj.get(Constants.locale)));
            }
        } catch (Exception ex) {
            response.put(Constants.RES_MESSAGE, ex.getMessage());
            response.put(Constants.RES_success, false);
            Logger.getLogger(CompanyServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return response;
    }
}