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
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesCMN;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.costCenter.AccCostCenterDAO;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignerConstants;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.entitygst.AccEntityGstService;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.groupcompany.AccGroupCompanyDAO;
import com.krawler.spring.accounting.handler.*;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.product.productHandler;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.common.fieldManagerDAO;
import com.krawler.spring.companyDetails.companyDetailsDAO;
import com.krawler.spring.profileHandler.profileHandlerDAO;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.context.MessageSource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class WSUtilServiceImpl implements WSUtilService {

    private accCompanyPreferencesDAO accCompanyPreferencesDAOObj;
    private companyDetailsDAO companyDetailsDAOObj;
    private profileHandlerDAO profileHandlerDAOObj;
    private accCurrencyDAO accCurrencyDAOObj;
    private MessageSource messageSource;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accProductDAO accProductObj;
    private accAccountDAO accAccountDAOobj;
    private AccGroupCompanyDAO accGroupCompanyDAO;
    private MasterService masterService;
    private accTaxDAO accTaxObj;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private AccEntityGstService accEntityGstService;
    private accCustomerDAO accCustomerDAOobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private accInvoiceDAO accInvoiceDAOobj;
    private AccCostCenterDAO accCostCenterObj;
    private accGoodsReceiptDAO accGoodsReceiptDAO;
    private accCreditNoteDAO accCreditNoteDAOobj;
    private accDebitNoteDAO accDebitNoteobj;
    private fieldManagerDAO fieldManagerDAOobj;
    private fieldDataManager fieldDataManagercntrl;
    
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    
     public void setFieldManagerDAO(fieldManagerDAO fieldManagerDAOobj) {
        this.fieldManagerDAOobj = fieldManagerDAOobj;
    }
     public void setaccCreditNoteDAO(accCreditNoteDAO accCreditNoteDAOobj) {
        this.accCreditNoteDAOobj = accCreditNoteDAOobj;
    }

    public void setAccGoodsReceiptDAO(com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO accGoodsReceiptDAO) {
        this.accGoodsReceiptDAO = accGoodsReceiptDAO;
    }
    
    public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteobj) {
        this.accDebitNoteobj = accDebitNoteobj;
    }
    
    
    public void setaccCostCenterDAO(AccCostCenterDAO accCostCenterDAOObj) {
        this.accCostCenterObj = accCostCenterDAOObj;
    }
    
    public void setaccCompanyPreferencesObj(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }
    public void setAccEntityGstService(AccEntityGstService accEntityGstService) {
        this.accEntityGstService = accEntityGstService;
    }
    public void setaccCustomerDAO(accCustomerDAO accCustomerDAOobj) {
        this.accCustomerDAOobj = accCustomerDAOobj;
    }
    
    public void setMasterService(MasterService masterService) {
        this.masterService = masterService;
    }
    
    public void setaccGroupCompanyDAO(AccGroupCompanyDAO accGroupCompanyDAO) {
        this.accGroupCompanyDAO = accGroupCompanyDAO;
    }
    
     public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    
    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }
     
    public void setmessageSource(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesDAOObj) {
        this.accCompanyPreferencesDAOObj = accCompanyPreferencesDAOObj;
    }

    public void setcompanyDetailsDAO(companyDetailsDAO companyDetailsDAOObj) {
        this.companyDetailsDAOObj = companyDetailsDAOObj;
    }

    public void setprofileHandlerDAO(profileHandlerDAO profileHandlerDAOObj) {
        this.profileHandlerDAOObj = profileHandlerDAOObj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOObj) {
        this.accCurrencyDAOObj = accCurrencyDAOObj;
    }
    
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    
    @Override    
    public JSONObject getErrorResponse(String errorCode, JSONObject jobj, String errorMessage) {
        JSONObject response = new JSONObject();
        try {
           
            if (StringUtil.isNullOrEmpty(errorCode)) {
                response.put(Constants.RES_MESSAGE, errorMessage);
            } else if (errorCode.equals(ServiceException.FAILURE)) {
                response.put(Constants.RES_MESSAGE, errorMessage);
                response.put(Constants.RES_ERROR_CODE, errorCode);
            } else {
                String language = Constants.RES_DEF_LANGUAGE;
                if (jobj != null && jobj.has("language") && jobj.getString("language") != null) {
                    language = jobj.getString("language");
                }
                Object[] paramValues = null;
                if (errorCode != null && errorCode.contains("{") && errorCode.contains("}")) {
                    String paramValue = errorCode.substring(errorCode.indexOf("{") + 1, errorCode.indexOf("}"));
                    errorCode = errorCode.substring(0, errorCode.indexOf("{"));
                    List<String> params = new ArrayList<String>();
                    for (String param : paramValue.split(";")) {
                        params.add(param);
                    }
                    paramValues = params.toArray();
                }
                response.put(Constants.RES_MESSAGE, messageSource.getMessage(errorCode, paramValues, Locale.forLanguageTag(language)));
                response.put(Constants.RES_ERROR_CODE, errorCode);
            }
            response.put(Constants.RES_success, false);
            
        } catch (JSONException ex1) {
            Logger.getLogger(WSUtilServiceImpl.class.getName()).log(Level.SEVERE, null, ex1);
        }
        return response;
    }

    public boolean isCompanyExists(JSONObject jobj) throws ServiceException {
        List list = new ArrayList();
        try {
            if (jobj.has(Constants.companyKey)) {
                list = accCompanyPreferencesDAOObj.isCompanyExistWithCompanyID(jobj.getString(Constants.companyKey));
            } else if (jobj.has(Constants.RES_CDOMAIN)) {
                list = accCompanyPreferencesDAOObj.isCompanyExistWithSubDomain(jobj.getString(Constants.RES_CDOMAIN));
            } else {
                return false;
            }

            if (list.size() > 0) {
                Iterator it = list.iterator();
                while (it.hasNext()) {
                    Long l = (Long) it.next();
                    if (l == 0) {
                        return false;
                    }
                }
                return true;
            }

        } catch (JSONException e) {
            throw ServiceException.FAILURE("Company does not exist", "e04", false);
        }
        return false;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public JSONObject populateAdditionalInformation(JSONObject jobj) throws ServiceException, JSONException {
        if(!((jobj.has(Constants.RES_CDOMAIN) && !StringUtil.isNullOrEmpty(jobj.getString(Constants.RES_CDOMAIN))) || (jobj.has(Constants.companyKey) && !StringUtil.isNullOrEmpty(jobj.getString(Constants.companyKey))))){
            throw ServiceException.FAILURE("Mandatory missing", "e01", false);
        }
        if (!jobj.has(Constants.language) || StringUtil.isNullOrEmpty(jobj.getString(Constants.language))) {
            jobj.put(Constants.language, Constants.RES_DEF_LANGUAGE);
        }

        Locale locale = Locale.forLanguageTag(jobj.getString(Constants.language));
        jobj.put(Constants.locale, locale);

        if (!isCompanyExists(jobj)) {
            throw ServiceException.FAILURE("Company does not exist", "e04", false);
        }
        String companyId = null;
        String userId=null;
        
        if(jobj.has(Constants.COMPANY_SUBDOMAIN) && !StringUtil.isNullOrEmpty(jobj.getString(Constants.COMPANY_SUBDOMAIN))){
            jobj.put(Constants.RES_CDOMAIN, jobj.getString(Constants.COMPANY_SUBDOMAIN));
        }
            
        if (jobj.has(Constants.companyKey) && !StringUtil.isNullOrEmpty(jobj.getString(Constants.companyKey))) {
            companyId = jobj.getString(Constants.companyKey);
        } else {
            companyId = companyDetailsDAOObj.getCompanyid(jobj.getString(Constants.RES_CDOMAIN));
            jobj.put(Constants.companyKey, companyId);
        }
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put(Constants.companyKey, companyId);
        ArrayList filter_names = new ArrayList();
        filter_names.add(Constants.companyKey);
        KwlReturnObject companyResponse = companyDetailsDAOObj.getCompanyInformation(requestParams, filter_names, null);
        if (companyResponse != null && companyResponse.getEntityList() != null && !companyResponse.getEntityList().isEmpty()) {
            Company company = (Company) companyResponse.getEntityList().get(0);
            if (company != null && company.getCurrency() != null) {
                jobj.put(Constants.globalCurrencyKey, company.getCurrency().getCurrencyID());
                jobj.put(Constants.creatoridKey, company.getCreator().getUserID());
                jobj.put(Constants.creatorUserName, StringUtil.getFullName(company.getCreator()));
                if (jobj.has(Constants.country) && !StringUtil.isNullOrEmpty(jobj.getString(Constants.country))) {
                    jobj.put(Constants.country,jobj.optString(Constants.country) );
                } else {
                    jobj.put(Constants.country, (company.getCountry() != null && !StringUtil.isNullOrEmpty(company.getCountry().getID())) ? company.getCountry().getID() : "");
                }
            }
        }
        
        ExtraCompanyPreferences extraCompanyPreferencesObj = null;
        Map<String, Object> requestParamsExtra = new HashMap<String, Object>();
        requestParamsExtra.put("id", companyId);
        KwlReturnObject resultExtra = accCurrencyDAOObj.getExtraCompanyPreferencestoCheckBaseCurrency(requestParamsExtra);
        if (!resultExtra.getEntityList().isEmpty()) {
            extraCompanyPreferencesObj = (ExtraCompanyPreferences) resultExtra.getEntityList().get(0);
        }
        boolean isOnlyBaceCurrencyflag = false;
        if (extraCompanyPreferencesObj != null) {
            if (extraCompanyPreferencesObj.isOnlyBaseCurrency()) {
                isOnlyBaceCurrencyflag = true;
            }
        }
         jobj.put("isOnlyBaceCurrencyflag",isOnlyBaceCurrencyflag);
        

        if ((jobj.has("userName") && !jobj.has(Constants.lid) && !StringUtil.isNullOrEmpty(jobj.getString("userName")))
                || (jobj.has("username") && !jobj.has(Constants.lid) && !StringUtil.isNullOrEmpty(jobj.getString("username"))) || (jobj.has(Constants.useridKey) && !StringUtil.isNullOrEmpty(jobj.getString(Constants.useridKey)))) {

            if (jobj.has(Constants.useridKey) && !StringUtil.isNullOrEmpty(jobj.getString(Constants.useridKey))) {
                userId=jobj.getString(Constants.useridKey);
            } else {
                String userName = "";
                if (jobj.has("userName")) {
                    userName = jobj.getString("userName");
                } else if (jobj.has("username")) {
                    userName = jobj.getString("username");
                }
                 userId= profileHandlerDAOObj.getUserIdFromUserName(userName, jobj.getString(Constants.companyKey));
                if (!StringUtil.isNullOrEmpty(userId)) {
                    jobj.put(Constants.lid, userId);
                    jobj.put(Constants.useridKey, userId);
                }
//                else {
//                    throw ServiceException.FAILURE("User does not exist", "e06", false);
//                }
            }
            KwlReturnObject userclass = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
            User user = (User) userclass.getEntityList().get(0);
	if(user!=null){
            jobj.put(Constants.username,user.getFirstName());
            jobj.put(Constants.usermailId, user.getEmailID());
            jobj.put(Constants.userfullname, StringUtil.getFullName(user));
            jobj.put(Constants.dateformatid, (user.getDateFormat() == null ? "":user.getDateFormat().getFormatID()));
	   jobj.put("userdateform", (user.getDateFormat() != null ? user.getDateFormat().getJavaForm(): "yyyy-MM-dd"));
	    jobj.put(Constants.roleid, (!StringUtil.isNullOrEmpty(user.getRoleID()))? user.getRoleID() :"");
	}
            KwlReturnObject tzdiffResult = accountingHandlerDAOobj.getObject(KWLTimeZone.class.getName(), storageHandlerImpl.getDefaultTimeZoneID());
            KWLTimeZone timeZone =(KWLTimeZone) tzdiffResult.getEntityList().get(0);
            jobj.put(Constants.timezonedifference, (timeZone == null ? "":timeZone.getDifference()));
        }
        if ((jobj.has("currencyCode") && !jobj.has(Constants.currencyKey) && !StringUtil.isNullOrEmpty(jobj.getString("currencyCode")))
                || (jobj.has("currencycode") && !jobj.has(Constants.currencyKey) && !StringUtil.isNullOrEmpty(jobj.getString("currencycode")))) {
            String currencyCode = "";
            if (jobj.has("currencyCode")) {
                currencyCode = jobj.getString("currencyCode");
            } else if (jobj.has("currencycode")) {
                currencyCode = jobj.getString("currencycode");
            }
            KwlReturnObject response = accCurrencyDAOObj.getCurrencyFromCode(currencyCode);
            if (response != null && response.getEntityList() != null && !response.getEntityList().isEmpty()) {
                KWLCurrency currency = (KWLCurrency) response.getEntityList().get(0);
                jobj.put(Constants.currencyKey, currency.getCurrencyID());

            } else {
                throw ServiceException.FAILURE("Currency does not exist", "erp23", false);
            }
        }
        if (jobj.has(Constants.RES_DATEFORMAT)) {
            jobj.put(Constants.userdateformat, jobj.getString(Constants.RES_DATEFORMAT));
        }
        if (jobj.has(Constants.userdateformat)) {
            jobj.put(Constants.userdateformat, jobj.getString(Constants.userdateformat));
        }
        if (jobj.has(Constants.timezonedifference)) {
            jobj.put(Constants.timezonedifference, jobj.getString(Constants.timezonedifference));
        }
        String defaultIp = Constants.defaultIp;
        if (jobj.has(Constants.remoteIPAddress) && !StringUtil.isNullOrEmpty(jobj.getString(Constants.remoteIPAddress))) {
            jobj.put(Constants.remoteIPAddress, jobj.getString(Constants.remoteIPAddress));
            jobj.put(Constants.reqHeader, jobj.getString(Constants.remoteIPAddress));
        } else {
            jobj.put(Constants.remoteIPAddress, defaultIp);
            jobj.put(Constants.reqHeader, defaultIp);
        }
        return jobj;
    }
    
  @Override  
    public JSONArray createJSONForCustomField(String customField, String companyid, int moduleid) throws JSONException {
        JSONArray customJArray = new JSONArray(customField);
        JSONArray returnArray = new JSONArray();
        try {

            for (int i = 0; i < customJArray.length(); i++) {
                JSONObject jobj = customJArray.getJSONObject(i);
                HashMap<String, Object> invFieldParamsMap = new HashMap<>();

                invFieldParamsMap.put(Constants.filter_names, Arrays.asList(Constants.fieldlabel, Constants.companyid, Constants.moduleid));
                String fieldLabel = jobj.optString(Constants.fieldlabel);
                if (!StringUtil.isNullOrEmpty(fieldLabel)) {
                    invFieldParamsMap.put(Constants.filter_values, Arrays.asList(fieldLabel, companyid, moduleid));
                }
                KwlReturnObject fieldIdparam = accAccountDAOobj.getFieldParamsIds(invFieldParamsMap);
                String fieldId = "";
                if (fieldIdparam.getEntityList() != null && !fieldIdparam.getEntityList().isEmpty() && fieldIdparam.getEntityList().get(0) != null) {
                    fieldId = (String) fieldIdparam.getEntityList().get(0);
                    jobj.put(Constants.Acc_custom_fieldId, fieldId);
                    returnArray.put(jobj);
                }
                
            }
            if (returnArray.length() == 0) {
                returnArray = customJArray;
            }
            
        } catch (Exception ex) {
            Logger.getLogger(WSUtilServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return returnArray;
    }
    
 /*
  * @Description:Function used to replace the global fields and line level fields value with  their respective jsonobject keys.
  * @Modules: Sales Order,Sales Return, Sales Invoice, Receive Payment Against Invoice and Credit Note
  * @Usage: Squats & Multi Group of Companies (flag=Constants.isMultiGroupCompanyFlag).
  * @param: JSONObject paramJObj
  */   
    @Override
    public JSONObject populateMastersInformation(JSONObject paramJObj) throws ServiceException {
        try {
            String destinationCompanyid = paramJObj.getString(Constants.companyKey);
            paramJObj = manipulateGlobalLevelFieldsNew(paramJObj, destinationCompanyid);
            paramJObj = manipulateLineLevelFieldsNew(paramJObj, destinationCompanyid);

        } catch (ServiceException e) {
            throw e;
        } catch (Exception e) {
            throw ServiceException.FAILURE("Exception occurred while populating masters information", "erp23", false);
        }
        return paramJObj;
    }

  /*
     * @Description:Replace the ids of Global Level Fields.Fields like Customer,Term,Tax,External Currency Rate,Invoice Terms,Payment Details.
     * @param: JSONObject paramJObj
     */
   @Override 
    public JSONObject manipulateGlobalLevelFieldsNew(JSONObject paramJObj, String destinationCompanyid) throws ServiceException, ParseException {
        String globalcurrency = "", sourceCompany = "", sourceModule = "", destinationCompany = "", destinationModule = "";
        JSONObject globalFieldJson = paramJObj;
        Map<String, String> requestParams = new HashMap<>();
        try {
            String countryid = "0";
            KwlReturnObject companyresult = accountingHandlerDAOobj.getObject(Company.class.getName(), destinationCompanyid);
            Company companyObj = (Company) companyresult.getEntityList().get(0);
            if (companyObj.getCountry() != null) {
                countryid = companyObj.getCountry().getID();
            }
            String moduleid = paramJObj.optString(Constants.moduleid);//receive payment module id
            
                //Fetching termname and saving in destination subdomain 
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("termid", null)) || !StringUtil.isNullOrEmpty(paramJObj.optString("terms", null))) {
                String termid = null;
                if (!StringUtil.isNullOrEmpty(paramJObj.optString("termid", null))) {
                    termid = paramJObj.optString("termid");
                } else if (!StringUtil.isNullOrEmpty(paramJObj.optString("terms", null))) {
                    termid = paramJObj.optString("terms");
                }

                if (!StringUtil.isNullOrEmpty(termid)) {
                    KwlReturnObject olddebittermresult = accountingHandlerDAOobj.getObject(Term.class.getName(), termid);
                    Term term = (Term) olddebittermresult.getEntityList().get(0);
                    if (term != null) {
                        String termname = term.getTermname();

                        if (!StringUtil.isNullOrEmpty(termname)) {
                            paramJObj.put("termvalue", termname);
                        }
                    } else {
                        throw ServiceException.FAILURE("Term with 'termid' does not exist", "erp41{" + paramJObj.optString("termid") + "}", false);
                    }
                }
            }
            
            //Group of Companies Respective CODE
            if (paramJObj.optBoolean(Constants.isMultiGroupCompanyFlag)) { 
                sourceCompany = paramJObj.optString(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN);
                sourceModule = paramJObj.optString(GroupCompanyProcessMapping.SOURCE_MODULE);
                destinationModule = paramJObj.optString(GroupCompanyProcessMapping.DESTINATION_MODULE);
                destinationCompany = paramJObj.optString(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN);


                    if (!StringUtil.isNullOrEmpty(paramJObj.optString("statuscombo", null))) {
                        KwlReturnObject reasonResult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), paramJObj.optString("statuscombo"));
                        MasterItem statusObj = (MasterItem) reasonResult.getEntityList().get(0);
                        if (statusObj != null) {
                            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                            filter_names.add("masterGroup.ID");
                            filter_params.add(MasterItem.DeliverStatus_Combo);// for saving status for DO
                            filter_names.add("company.companyID");
                            filter_params.add(destinationCompanyid);
                            filter_names.add("value");
                            filter_params.add(statusObj.getValue());
                            filterRequestParams.put("filter_names", filter_names);
                            filterRequestParams.put("filter_params", filter_params);
                            KwlReturnObject cntResult = accMasterItemsDAOobj.getMasterItems(filterRequestParams);
                            int count = cntResult.getRecordTotalCount();

                            if (cntResult != null && cntResult.getEntityList() != null && !cntResult.getEntityList().isEmpty() && cntResult.getEntityList().get(0) != null && count == 1) {
                                statusObj = (MasterItem) cntResult.getEntityList().get(0);
                                if (statusObj != null) {
                                    paramJObj.put("statuscombo", statusObj.getID());
                                }

                            } else if (count < 1) {//else it will masteritem and then put into the masteritem value
                                filterRequestParams = new HashMap<String, Object>();
                                filterRequestParams.put(Constants.companyKey, destinationCompanyid);
                                filterRequestParams.put("name", statusObj.getValue());
                                filterRequestParams.put("groupid", MasterItem.DeliverStatus_Combo);
                                cntResult = accMasterItemsDAOobj.addMasterItem(filterRequestParams);
                                if (cntResult.getEntityList() != null && !cntResult.getEntityList().isEmpty() && cntResult.getEntityList().get(0) != null) {
                                    MasterItem masterItem = (MasterItem) cntResult.getEntityList().get(0);
                                    paramJObj.put("statuscombo", masterItem.getID());
                                }
                            }
                        }
                    } //end of statuscombo 

                    if (!StringUtil.isNullOrEmpty(paramJObj.optString("costcenter", null))) {
                        KwlReturnObject costCenterResult = accountingHandlerDAOobj.getObject(CostCenter.class.getName(), paramJObj.optString("costcenter"));
                        CostCenter csObj = (CostCenter) costCenterResult.getEntityList().get(0);
                        if (csObj != null) {
                            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                            KwlReturnObject cntResult = accCostCenterObj.checkUniqueCostCenter(csObj.getID(),csObj.getCcid(), csObj.getName(), destinationCompanyid);
                            int count = cntResult.getRecordTotalCount();

                            if (cntResult != null && cntResult.getEntityList() != null && !cntResult.getEntityList().isEmpty() && cntResult.getEntityList().get(0) != null && count == 1) {
                                csObj = (CostCenter) cntResult.getEntityList().get(0);
                                if (csObj != null) {
                                    paramJObj.put("costcenter", csObj.getID());
                                }

                            } else if (count < 1) {  //saving cost center in destination subdomain 
                                filterRequestParams = new HashMap<String, Object>();
                                filterRequestParams.put("Ccid", csObj.getCcid());
                                filterRequestParams.put("Name", csObj.getName());
                                filterRequestParams.put("Description", csObj.getDescription());
                                filterRequestParams.put("Company", destinationCompanyid);

                                Object csfieldObj = accCostCenterObj.saveCostCenter(filterRequestParams);
                                if (csfieldObj != null) {
                                    CostCenter costCenterObj = (CostCenter) csfieldObj;
                                    if (costCenterObj != null) {
                                        paramJObj.put("costcenter", costCenterObj.getID());
                                    }
                                }
                            }
                        }
                    } //end of statuscombo 

                companyresult = accountingHandlerDAOobj.getObject(Company.class.getName(), destinationCompanyid); 
                companyObj = (Company) companyresult.getEntityList().get(0);
                if (companyObj != null && companyObj.getCurrency() != null) {
                    globalcurrency = companyObj.getCurrency().getCurrencyID();
                    if (!StringUtil.isNullOrEmpty(globalcurrency)) {
                        paramJObj.put(Constants.globalCurrencyKey, globalcurrency);
                    }
                }

             /**************************************************Customer**************************************************************/   
                Map<String, Object> fieldrequestParams = new HashMap<String, Object>();
                String sourceMasterCode = null;
                
                if (!StringUtil.isNullOrEmpty(paramJObj.optString("vendor", null)) ||(moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Receive_Payment_ModuleId)) && !StringUtil.isNullOrEmpty(paramJObj.optString("accid", null)))) {
                    String vendorid = paramJObj.optString("vendor", null);
                    if (StringUtil.isNullOrEmpty(vendorid)) {
                        vendorid = paramJObj.optString("accid", null);
                    }
                    
                    KwlReturnObject vnresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), vendorid);
                    Vendor vendor = (Vendor) vnresult.getEntityList().get(0);
                    if (vendor != null) {
                        sourceMasterCode = vendor.getAcccode();
                        fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN, sourceCompany);
                        fieldrequestParams.put(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN, destinationCompany);
                        fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_MASTERCODE, sourceMasterCode);//need vendor code
                        KwlReturnObject multiCompanyCVObj = accGroupCompanyDAO.fetchCustomerVendorDetails(fieldrequestParams);
                        List<GroupCompanyCustomerVendorMapping> multiCVObj = multiCompanyCVObj.getEntityList();

                        if (multiCVObj.size() > 0) {
                            for (GroupCompanyCustomerVendorMapping multCustomerVendorObj : multiCVObj) {
                                String destinationMasterCode = multCustomerVendorObj.getDestinationMasterCode();
                                boolean isCustomer = multCustomerVendorObj.isIsSourceCustomer();
                                if (isCustomer) {
                                    if (!StringUtil.isNullOrEmpty(destinationMasterCode)) {
                                        paramJObj.put("customervalue", destinationMasterCode);
                                    } else if (!StringUtil.isNullOrEmpty(multCustomerVendorObj.getDestinationMasterId())) {
                                        paramJObj.put("customer", multCustomerVendorObj.getDestinationMasterId());
                                        paramJObj.put("CustomerName", multCustomerVendorObj.getDestinationMasterId());
                                        paramJObj.put(Constants.customerName, multCustomerVendorObj.getDestinationMasterId());
                                    }
                                   paramJObj.remove("vendor");
                                }//end if iscustomer 
                            }//end of customer vemdor mapping
                        } else {// end of multiCVObj.size()
                            throw ServiceException.FAILURE("Customer is not mapped. Please map the Customer to proceed further.", "erp34{" + "customerName" + "}", false);
                        }
                    }
                }

                
              /**************************************************Global level Product Tax**************************************************************/     
                if (!StringUtil.isNullOrEmpty(paramJObj.optString(Constants.TAXID, null))) {
                    String taxcode = null;
                    KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), paramJObj.optString(Constants.TAXID, null)); // (Tax)session.get(Tax.class, taxid);
                    Tax tax = (Tax) txresult.getEntityList().get(0);
                    if (tax != null) {
                        taxcode = tax.getTaxCode();
                        fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_TAX_CODE, taxcode);
                        fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN, sourceCompany);
                        fieldrequestParams.put(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN, destinationCompany);
                        KwlReturnObject lineTaxObj = accGroupCompanyDAO.fetchTaxMappingDetails(fieldrequestParams);
                        List<GroupCompanyTaxMapping> lineleveltaxObj = lineTaxObj.getEntityList();
                        if (lineleveltaxObj.size() > 0) {
                            for (GroupCompanyTaxMapping taxObj : lineleveltaxObj) {
                                String destinationtaxCode = taxObj.getDestinationTaxCode();
                                if (!StringUtil.isNullOrEmpty(destinationtaxCode)) {
                                    paramJObj.put("taxvalue", destinationtaxCode);
                                } else if (!StringUtil.isNullOrEmpty(taxObj.getDestinationTaxId())) {
                                    paramJObj.put("taxid", taxObj.getDestinationTaxId());
                                }
                            }
                        } else {
                            paramJObj.put("taxid", "");
                            paramJObj.put("taxamount", "0.0");
                        }
                    }else{
                         paramJObj.put("taxid","");
                         paramJObj.put("taxamount","0.0");
                    }
                }// end 

                 /**************************************************Invoice Term Calculations  **************************************************************/     
                if (!StringUtil.isNullOrEmpty(paramJObj.optString("invoicetermsmap", null))) {
                    JSONArray modifiedtermDetailArray = new JSONArray();
                    JSONArray termDetailsJArr = new JSONArray(paramJObj.getString("invoicetermsmap"));
                    for (int cnt = 0; cnt < termDetailsJArr.length(); cnt++) {
                        JSONObject temp = termDetailsJArr.getJSONObject(cnt);
                        fieldrequestParams = new HashMap<String, Object>();
                        fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN, sourceCompany);
                        fieldrequestParams.put(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN, destinationCompany);
//                        fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_TERM_NAME, temp.optString("term"));
                        fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_TERM_NAME,StringUtil.DecodeText(temp.optString("term")));
                        KwlReturnObject multiCompanyTermObj = accGroupCompanyDAO.fetchTermMappingDetails(fieldrequestParams);
                        List<GroupCompanyTermMapping> multitermObj = multiCompanyTermObj.getEntityList();
                        if (multitermObj.size() > 0) {
                            for (GroupCompanyTermMapping termObj : multitermObj) {
                                String destinationtermName = termObj.getDestinationTermName();
                                JSONObject tempTermobj = new JSONObject();
                                tempTermobj.put(Constants.companyKey, destinationCompanyid);
                                tempTermobj.put("term", destinationtermName);
                                tempTermobj.put("isSalesOrPurchase", "true");
                                JSONObject invoicesalestempjson = masterService.getInvoiceTerms(tempTermobj).getJSONObject(0);

                                invoicesalestempjson.put("termpercentage", temp.optString("termpercentage"));
                                if (temp.optString("sign").equalsIgnoreCase("0") && invoicesalestempjson.optString("sign").equalsIgnoreCase("1")) {
                                    invoicesalestempjson.put("termamount", Math.abs(Double.parseDouble(temp.optString("termamount"))));
                                } else if (temp.optString("sign").equalsIgnoreCase("1") && invoicesalestempjson.optString("sign").equalsIgnoreCase("0")) {
                                    invoicesalestempjson.put("termamount", -Math.abs(Double.parseDouble(temp.optString("termamount"))));
                                } else {
                                    invoicesalestempjson.put("termamount", temp.optString("termamount"));
                                }
                                modifiedtermDetailArray.put(invoicesalestempjson);
                            }// end of for (GroupCompanyTermMapping termObj : multitermObj)

                        } 
                    }
                    paramJObj.put("invoicetermsmap", modifiedtermDetailArray.toString());
                }

                    if (!StringUtil.isNullOrEmpty(paramJObj.optString("pmtmethod", null))) {//Cash in Hand value
                    String paymentMethodid = paramJObj.optString("pmtmethod");
                    KwlReturnObject olddebittermresult = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paymentMethodid);
                    PaymentMethod pmtMehodObj = (PaymentMethod) olddebittermresult.getEntityList().get(0);
                    if (pmtMehodObj != null) {
                        paramJObj.put("pmtmethodvalue", pmtMehodObj.getMethodName());
                    }else{
                        paramJObj.put("pmtmethodvalue","Cash");
                        paramJObj.put("paydetail","");
                    }
                }
                
            }//end of isMultiGroupCompanyFlag flag
            
            
            //Get salesperson id from  salesperson name
            if ((paramJObj.has("salespersonvalue") && !StringUtil.isNullOrEmpty(paramJObj.getString("salespersonvalue")))) {
                String salespersonName = paramJObj.getString("salespersonvalue");
                requestParams.put("tableName", "MasterItem");
                requestParams.put("fetchColumn", "ID");
                requestParams.put("condtionColumn", "value");
                requestParams.put("condtionColumnvalue", salespersonName);
                requestParams.put("companyColumn", "company.companyID");
                requestParams.put(Constants.companyKey, destinationCompanyid);

                KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                    paramJObj.put("salesPerson", result.getEntityList().get(0).toString());
                    paramJObj.put("salesperson", result.getEntityList().get(0).toString());
                    paramJObj.put("salesPersonID", result.getEntityList().get(0).toString());
                    paramJObj.remove("salespersonvalue");
                }

            } else if (paramJObj.has("salespersonvalue") && StringUtil.isNullOrEmpty(paramJObj.optString("salespersonvalue", null))) {
                paramJObj.put("salesPerson","");
                paramJObj.put("salesperson", "");
                paramJObj.put("salesPersonID", "");
                paramJObj.remove("salespersonvalue");

            }
            requestParams.clear();
            
            //Get Agent-Id from  Agent-Name
            if ((paramJObj.has("agentvalue") && !StringUtil.isNullOrEmpty(paramJObj.getString("agentvalue")))) {
                String agentName = paramJObj.getString("agentvalue");

                KwlReturnObject result = accMasterItemsDAOobj.getMasterItemByNameorID(destinationCompanyid, agentName, Constants.AGENT_ID, "ID", "value");

                if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                    paramJObj.put("agent",(String) result.getEntityList().get(0));//For GoodsReceipt
                    paramJObj.remove("agentvalue");
                }
                result = null;
            } else if (paramJObj.has("agentvalue") && StringUtil.isNullOrEmpty(paramJObj.optString("agentvalue", null))) {
                paramJObj.put("agent","");//For GoodsReceipt
                paramJObj.remove("agentvalue");
            }
            
            //Get Status-Id from  Status-Name
            if ((paramJObj.has("statusvalue") && !StringUtil.isNullOrEmpty(paramJObj.getString("statusvalue")))) {
                String statusValue = paramJObj.getString("statusvalue");
                String masterGroupId = "";
                if (StringUtil.equal(moduleid, String.valueOf(Constants.Acc_Goods_Receipt_ModuleId))) {
                    masterGroupId = MasterItem.Status_Combo;
                } else if (StringUtil.equal(moduleid, String.valueOf(Constants.Acc_Delivery_Order_ModuleId))) {
                    masterGroupId = MasterItem.DeliverStatus_Combo;
                }

                KwlReturnObject result = accMasterItemsDAOobj.getMasterItemByNameorID(destinationCompanyid, statusValue, masterGroupId, "ID", "value");

                if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                    paramJObj.put("statuscombo",(String) result.getEntityList().get(0));//For GoodsReceipt, DeliveryOrder
                    paramJObj.remove("statusvalue");
                }
                result = null;
            } else if (paramJObj.has("statusvalue") && StringUtil.isNullOrEmpty(paramJObj.optString("statusvalue", null))) {
                paramJObj.put("statuscombo","");//For GoodsReceipt, DeliveryOrder
                paramJObj.remove("statusvalue");
            }
            
            //for salesorder link advance receipts
//            boolean isLinkAdvanceReceipts = paramJObj.optBoolean("isLinkadvancereceipts");
//            if (paramJObj.has("receiptno") && !StringUtil.isNullOrEmpty(paramJObj.getString("receiptno")) ) {
//                String receiptno = paramJObj.getString("receiptno");
//                requestParams.put("tableName", "Receipt");
//                requestParams.put("fetchColumn", "ID");
//                requestParams.put("condtionColumn", "receiptNumber");
//                requestParams.put("condtionColumnvalue", receiptno);
//                requestParams.put("companyColumn", "company.companyID");
//                requestParams.put(Constants.companyKey, destinationCompanyid);
//
//                KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);
//
//                if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
//                    KwlReturnObject rpReturnObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), result.getEntityList().get(0).toString());
//                    Receipt rpObj = (Receipt) rpReturnObj.getEntityList().get(0);
//                    if (rpObj != null && isLinkAdvanceReceipts) {
//                        paramJObj.put("linkedAdvancePaymentId", result.getEntityList().get(0).toString());
//                        paramJObj.put("linkedAdvancePaymentNo", rpObj.getReceiptNumber());
//                    }
//                    paramJObj.remove("receiptno");
//                }
//            } else if (paramJObj.has("salespersonvalue") && StringUtil.isNullOrEmpty(paramJObj.optString("salespersonvalue", null))) {
//                paramJObj.put("salesPerson", "");
//                paramJObj.put("salesperson", "");
//                paramJObj.put("salesPersonID", "");
//                paramJObj.remove("salespersonvalue");
//            }
            
            //Get paymentmethod id from  payment method
            if ((paramJObj.has("pmtmethodvalue") && !StringUtil.isNullOrEmpty(paramJObj.getString("pmtmethodvalue")))) {
                String paymentMethodName = paramJObj.getString("pmtmethodvalue");
                requestParams.put("tableName", "PaymentMethod");
                requestParams.put("fetchColumn", "ID");
                requestParams.put("condtionColumn", "methodName");
                requestParams.put("condtionColumnvalue", paymentMethodName);
                requestParams.put("companyColumn", "company.companyID");
                requestParams.put(Constants.companyKey, destinationCompanyid);

                KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                    paramJObj.put("pmtmethod", result.getEntityList().get(0).toString());
                    paramJObj.put("paymentmethodid", result.getEntityList().get(0).toString());
                    paramJObj.remove("pmtmethodvalue");
                    KwlReturnObject pmresult = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), result.getEntityList().get(0).toString());
                    PaymentMethod payMethod = (PaymentMethod) pmresult.getEntityList().get(0);
                    if (payMethod != null && payMethod.getAccount() != null) {
                        paramJObj.put(Constants.pmtmethodaccountid, payMethod.getAccount().getID());
                    }
                } else {
                    throw ServiceException.FAILURE("Payment Method does not exist", "erp31{" + "pmtmethodvalue" + "}", false);
                }

            } else if (paramJObj.optBoolean(Constants.isMultiGroupCompanyFlag) == false) { //Not checking for Group of Companies
                //Fetch Information from Dummy Table
                HashMap<String, Object> dummyParams = new HashMap<String, Object>();
                if (paramJObj.has(Constants.modulename) && !StringUtil.isNullOrEmpty(paramJObj.optString(Constants.modulename, null))) {
                    dummyParams.put(Constants.modulename, paramJObj.optString(Constants.modulename));
                    dummyParams.put(Constants.companyKey, paramJObj.optString(Constants.companyKey));
                    KwlReturnObject dummyAccountresult = accountingHandlerDAOobj.getAccountidforDummyAccount(dummyParams);

                    JSONObject dummyDetailJson = null;
                    String dummydetailString = null;
                    List<String> configList = dummyAccountresult.getEntityList();
                    if (configList != null && configList.size() > 0) {
                        dummydetailString = (String) configList.get(0);
                        if (!StringUtil.isNullOrEmpty(dummydetailString)) {
                            dummyDetailJson = new JSONObject(dummydetailString);
                            if (dummyDetailJson.has("accountcode") && !StringUtil.isNullOrEmpty(dummyDetailJson.optString("accountcode", null)) && dummyDetailJson.optString("accountcode").equalsIgnoreCase("DummyCode")) {
                                paramJObj.put(Constants.pmtmethodaccountid, dummyDetailJson.optString("accountid"));
                                paramJObj.put("pmtmethod", dummyDetailJson.optString("paymentmethodid"));
                                paramJObj.put("paymentmethodid", dummyDetailJson.optString("paymentmethodid"));
                            }
                        }//end of config list
                    }
                }
            }
            requestParams.clear();
            //get Currency Name
            if ((paramJObj.has("currencyvalue") && !StringUtil.isNullOrEmpty(paramJObj.getString("currencyvalue")))) {
                String currencyName = paramJObj.getString("currencyvalue");
                requestParams.put("tableName", "KWLCurrency");
                requestParams.put("fetchColumn", "currencyID");
                requestParams.put("companyColumn", "currencyCode");
                requestParams.put(Constants.companyKey, currencyName);

                KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                    paramJObj.put("currencyName", result.getEntityList().get(0).toString());
                    paramJObj.put(Constants.currencyKey, result.getEntityList().get(0).toString());
                    paramJObj.remove("currencyvalue");
                }
            }
            requestParams.clear();
            //Get customer id from  customer name
            if ((paramJObj.has("customervalue") && !StringUtil.isNullOrEmpty(paramJObj.getString("customervalue")))) {
                String customerName = paramJObj.getString("customervalue");
                requestParams.put("tableName", "Customer");
                requestParams.put("fetchColumn", "ID");
                requestParams.put("condtionColumn", "acccode");
                requestParams.put("condtionColumnvalue", customerName);
                requestParams.put("companyColumn", "company.companyID");
                requestParams.put(Constants.companyKey, destinationCompanyid);

                KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                    paramJObj.put("customer", result.getEntityList().get(0).toString());
                    paramJObj.put("CustomerName", result.getEntityList().get(0).toString());
                    paramJObj.put(Constants.customerName, result.getEntityList().get(0).toString());
                    paramJObj.put("newcustomerid", result.getEntityList().get(0).toString());
                    paramJObj.put(Constants.customerid, result.getEntityList().get(0).toString());

                    if (countryid.equals(String.valueOf(Constants.indian_country_id))) {
                        HashMap<String, Object> custAddrRequestParams = new HashMap<String, Object>();
                        custAddrRequestParams.put("companyid", destinationCompanyid);
                        custAddrRequestParams.put("customerid", result.getEntityList().get(0).toString());
                        custAddrRequestParams.put("isBillingAddress", "true");
                        Map<String, Object> addressParams = AccountingAddressManager.getDefaultCustomerAddress(custAddrRequestParams, accountingHandlerDAOobj, paramJObj);
                        if(addressParams.containsKey(Constants.BILLING_STATE)&& addressParams.get(Constants.BILLING_STATE)!=null){
                            if (!StringUtil.isNullOrEmpty((String) addressParams.get(Constants.BILLING_STATE))) {
                                paramJObj.put("statevalue",(String) addressParams.get(Constants.BILLING_STATE));
                            }else{
                                paramJObj.put("statevalue",""); 
                            }
                        }
                        KwlReturnObject soresult = accountingHandlerDAOobj.getObject(Customer.class.getName(), result.getEntityList().get(0).toString());
                        Customer customerObj = (Customer) soresult.getEntityList().get(0);
                        String type = customerObj.getGSTCustomerType()!=null?customerObj.getGSTCustomerType().getID():"";
                        /**
                         * Get Master item default for Customer type , To handle
                         * special case
                         */
                        String defaultMasterItemID = "";
                        if (!StringUtil.isNullOrEmpty(type)) {
                            KwlReturnObject retObj = accMasterItemsDAOobj.getMasterItem(type);
                            if (retObj != null && !retObj.getEntityList().isEmpty()) {
                                MasterItem reasonObj = (MasterItem) retObj.getEntityList().get(0);
                                defaultMasterItemID = reasonObj.getDefaultMasterItem() != null ? reasonObj.getDefaultMasterItem().getID() : "";
                            }
                             paramJObj.put("uniqueCase", accCustomerDAOobj.getUniqueCase(paramJObj.put("type",defaultMasterItemID)));
                        }else{
                             paramJObj.put("uniqueCase",0); 
                        } 
                    }
                     boolean isCustomerFlag= Boolean.parseBoolean(paramJObj.optString("iscustomer","false"));
                    //Replacing accid for receive payment against Customer 
                    if (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Receive_Payment_ModuleId)) && (!StringUtil.isNullOrEmpty(paramJObj.optString("accid", null))||isCustomerFlag)) {
                        paramJObj.put("accid", result.getEntityList().get(0).toString());
                    }
                    
                     //for mp against customer put customer id in accid
                    if (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Make_Payment_ModuleId)) && isCustomerFlag) {
                        paramJObj.put("accid", result.getEntityList().get(0).toString());
                    }
                    
                    paramJObj.remove("customervalue");
                } else {
                    throw ServiceException.FAILURE("customerName does not exist", "erp31{" + "customerName" + "}", false);
                }
            }
            
            //Get customer id from  customer name
            if ((paramJObj.has("vendorvalue") && !StringUtil.isNullOrEmpty(paramJObj.getString("vendorvalue")))) {
                String vendorName = paramJObj.getString("vendorvalue");
                requestParams.put("tableName", "Vendor");
                requestParams.put("fetchColumn", "ID");
                requestParams.put("condtionColumn", "acccode");
                requestParams.put("condtionColumnvalue", vendorName);
                requestParams.put("companyColumn", "company.companyID");
                requestParams.put(Constants.companyKey, destinationCompanyid);
                KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                    paramJObj.put(Constants.vendorid, result.getEntityList().get(0).toString());
                    paramJObj.put("VendorName", result.getEntityList().get(0).toString());//For saveGoodsReceipt
                    //Replacing accid for receive payment against Customer 
                    if (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Make_Payment_ModuleId))||moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) {
                        paramJObj.put("accid", result.getEntityList().get(0).toString());
                    }
                    
                    paramJObj.remove("vendorvalue");
                } else {
                    throw ServiceException.FAILURE("customerName does not exist", "erp31{" + "customerName" + "}", false);
                }
            }
            
            requestParams.clear();
            //Get costcenter id from  costcenter name
            if ((paramJObj.has("costcentervalue") && !StringUtil.isNullOrEmpty(paramJObj.getString("costcentervalue")))) {
                String costcenter = paramJObj.getString("costcentervalue");
                requestParams.put("tableName", "CostCenter");
                requestParams.put("fetchColumn", "ID");
                requestParams.put("condtionColumn", "name");
                requestParams.put("condtionColumnvalue", costcenter);
                requestParams.put("companyColumn", "company.companyID");
                requestParams.put(Constants.companyKey, destinationCompanyid);

                KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                    paramJObj.put("costcenterid", result.getEntityList().get(0).toString());
                    paramJObj.put("costCenterId", result.getEntityList().get(0).toString());//For credit note
                    paramJObj.put("costcenter", result.getEntityList().get(0).toString());
                    paramJObj.remove("costcentervalue");
                }
            } else if (paramJObj.has("costcentervalue") && StringUtil.isNullOrEmpty(paramJObj.optString("costcentervalue", null))) {
                paramJObj.put("costcenterid", "");
                paramJObj.put("costcenter", "");
                paramJObj.put("costCenterId", "");//For credit note
                paramJObj.remove("costcentervalue");
            }
            requestParams.clear();
            //Get Tax id from  tax name
            if ((paramJObj.has("taxvalue") && !StringUtil.isNullOrEmpty(paramJObj.getString("taxvalue")))) {
                String taxid = paramJObj.getString("taxvalue");
                requestParams.put("tableName", "Tax");
                requestParams.put("fetchColumn", "ID");
//                requestParams.put("condtionColumn", "name");
                requestParams.put("condtionColumn", "taxCode");
                requestParams.put("condtionColumnvalue", taxid);
                requestParams.put("companyColumn", "company.companyID");
                requestParams.put(Constants.companyKey, destinationCompanyid);

                KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                    paramJObj.put("taxid", result.getEntityList().get(0).toString());
                    paramJObj.remove("taxvalue");
                }
            }

            requestParams.clear();
            //Get term id from  term name
            if ((paramJObj.has("termvalue") && !StringUtil.isNullOrEmpty(paramJObj.getString("termvalue")))) {
                String term = paramJObj.getString("termvalue");
                requestParams.put("tableName", "Term");
                requestParams.put("fetchColumn", "ID");
                requestParams.put("condtionColumn", "termname");
                requestParams.put("condtionColumnvalue", term);
                requestParams.put("companyColumn", "company.companyID");
                requestParams.put(Constants.companyKey, destinationCompanyid);

                KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                    paramJObj.put("termid", result.getEntityList().get(0).toString());
                    paramJObj.put("terms", result.getEntityList().get(0).toString());
                    paramJObj.put("term", result.getEntityList().get(0).toString());
                    paramJObj.remove("termvalue");

                } else {
                    throw ServiceException.FAILURE("Term with 'termvalue' does not exist", "erp41{" + paramJObj.optString("termvalue") + "}", false);
                }
            }

            requestParams.clear();
            //Get sequence format id id from  sequence format  name
            if ((paramJObj.has("sequenceformatvalue") && !StringUtil.isNullOrEmpty(paramJObj.getString("sequenceformatvalue"))) && !("NA".equals(paramJObj.getString("sequenceformatvalue")))) {
                String sequenceformat = paramJObj.getString("sequenceformatvalue");
                requestParams.put("tableName", "SequenceFormat");
                requestParams.put("fetchColumn", "ID");
                requestParams.put("condtionColumn", "name");
                requestParams.put("condtionColumnvalue", sequenceformat);
                requestParams.put("companyColumn", "company.companyID");
                requestParams.put(Constants.companyKey, destinationCompanyid);

                KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                    paramJObj.put(Constants.SEQFORMAT, result.getEntityList().get(0).toString());
                    paramJObj.remove("sequenceformatvalue");
                } else {
                    throw ServiceException.FAILURE("sequenceformat does not exist", "erp31{" + "sequenceformatvalue" + "}", false);
                }
            }

            if ((paramJObj.has("productvalue") && !StringUtil.isNullOrEmpty(paramJObj.getString("productvalue"))) || (paramJObj.has("pid") && !StringUtil.isNullOrEmpty(paramJObj.getString("pid")))) {
                String productvalue = "";
                if (paramJObj.has("productvalue") && !StringUtil.isNullOrEmpty(paramJObj.getString("productvalue"))) {
                    productvalue = paramJObj.getString("productvalue");
                } else {
                    productvalue = paramJObj.getString("pid");
                }

                requestParams.put("tableName", "Product");
                requestParams.put("fetchColumn", "ID");
                requestParams.put("condtionColumn", "productid");
                requestParams.put("condtionColumnvalue", productvalue);
                requestParams.put("companyColumn", "company.companyID");
                requestParams.put(Constants.companyKey, destinationCompanyid);

                KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                    paramJObj.put(Constants.productid, result.getEntityList().get(0).toString());
                    paramJObj.remove("productvalue");
                }
            }
            
            //Calculating External Currency RATE
            if (!paramJObj.has(Constants.externalcurrencyrate) || !StringUtil.isNullOrEmpty(globalcurrency)) {
                Map<String, Object> Params = new HashMap<String, Object>();
                String companycurrency = paramJObj.optString(Constants.globalCurrencyKey);
                String toCurrencyid = paramJObj.optString(Constants.currencyKey);
                if (!StringUtil.isNullOrEmpty(toCurrencyid) && !StringUtil.isNullOrEmpty(companycurrency)) {
                    Params.put("fromcurrencyid", companycurrency);
                    Params.put("tocurrencyid", toCurrencyid);
                    KwlReturnObject result = accCurrencyDAOObj.getCurrencyExchange(Params);
                    List<ExchangeRate> list = result.getEntityList();
                    Params.clear();
                    Params = AccountingManager.getGlobalParamsJson(paramJObj);
                    Params.put("isCurrencyExchangeWindow", false);
                    Date transactiondate = null;
                    String date = null;
                    if (paramJObj.has("billdate") && !StringUtil.isNullOrEmpty(paramJObj.optString("billdate"))) {
                        date = paramJObj.optString("billdate") == null ? null : paramJObj.optString("billdate");
                    }else if (paramJObj.has("creationdate") && !StringUtil.isNullOrEmpty(paramJObj.optString("creationdate"))) {
                        date = paramJObj.optString("creationdate") == null ? null : paramJObj.optString("creationdate");
                    }
                    if (!StringUtil.isNullOrEmpty(date)) {  //ERP-9230 : //Used same date formatter which have used to save currency exchange
                        transactiondate = authHandler.getDateOnlyFormat().parse(date);
                    }
                    if (list != null && !list.isEmpty() && list.size() == 1) {
                        ExchangeRate ER=(ExchangeRate) list.get(0);
                            String erID = ER.getID();
                            KwlReturnObject erdresult = accCurrencyDAOObj.getExcDetailID(Params, null, transactiondate, erID);
                            ExchangeRateDetails erd = (ExchangeRateDetails) erdresult.getEntityList().get(0);
                            if (erd != null) {
                                paramJObj.put(Constants.externalcurrencyrate, erd.getExchangeRate());
                            }
                    }
                } else {
                    paramJObj.put(Constants.externalcurrencyrate, "1");
                }
            }//end of external currency rate   

            if (paramJObj.optBoolean(Constants.isMultiGroupCompanyFlag) && moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Receive_Payment_ModuleId))) { //Group of Companies
                String detailsJsonString = paramJObj.optString("Details", "[{}]");
                JSONArray jSONArray = new JSONArray(detailsJsonString);
                JSONArray modifiedJSONArray = new JSONArray();
                for (int i = 0; i < jSONArray.length(); i++) {
                    JSONObject rpjsonObject = jSONArray.getJSONObject(i);
                    String documentid = rpjsonObject.optString("documentid");
                    HashMap<String, Object> fieldrequestParams = new HashMap();
                    fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_MODULE, String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId));
                    fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID, documentid);
                    fieldrequestParams.put(GroupCompanyProcessMapping.DESTINATION_MODULE, String.valueOf(Constants.Acc_Invoice_ModuleId));
                    KwlReturnObject result = accGroupCompanyDAO.fetchTransactionMappingDetails(fieldrequestParams);
                    List<GroupCompanyTransactionMapping> multiTransObj = result.getEntityList();

                    if (multiTransObj.size() > 0) {
                        for (GroupCompanyTransactionMapping multTMObj : multiTransObj) {
                            String destinationTransactionId = multTMObj.getDestinationTransactionid();
                            KwlReturnObject soresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), destinationTransactionId);
                            Invoice invObj = (Invoice) soresult.getEntityList().get(0);
                            if (invObj != null) {
                                String invoicenumber = invObj.getInvoiceNumber();
                                rpjsonObject.put("documentid", destinationTransactionId);
                                rpjsonObject.put("documentno", invoicenumber);
                                rpjsonObject.put("amountDueOriginal", invObj.getInvoiceamount());
                                rpjsonObject.put("amountDueOriginalSaved", invObj.getInvoiceamount());
                                Date invoicedate = invObj.getCreationDate() != null ? invObj.getCreationDate() : new Date();
//                                Date invoicedate=invObj.getJournalEntry()!=null?invObj.getJournalEntry().getEntryDate():null;
//                                if(StringUtil.isNullObject(invoicedate)){
//                                    invoicedate = invObj.getCreationDate() != null ? invObj.getCreationDate() : new Date();
//                                }
                                DateFormat df = authHandler.getDateOnlyFormat();
                                String invoicecreationdate = df.format(invoicedate);
                                rpjsonObject.put("invoicecreationdate",invoicecreationdate );
                                rpjsonObject.put("amountdue", invObj.getInvoiceamountdue());
                                rpjsonObject.put("exchangeratefortransaction", paramJObj.optString(Constants.externalcurrencyrate, "1"));
                                modifiedJSONArray.put(rpjsonObject);
                            }
                        }
                    }
                }

                if (modifiedJSONArray.length() > 0) {
                    paramJObj.put("Details", modifiedJSONArray);
                }
            }
           
          //Payment Details  
            if (paramJObj.has("paydetail") && !StringUtil.isNullOrEmpty(paramJObj.optString("paydetail", null))) {
                JSONObject paymentJson = new JSONObject(paramJObj.optString("paydetail", "{}"));
                paymentJson.put(Constants.currencyKey, paramJObj.optString(Constants.currencyKey));//used in bank reconciliation for make payment
                if (paramJObj.optBoolean(Constants.isMultiGroupCompanyFlag)) {
                    HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                    filter_names.add("masterGroup.ID");
                    filter_params.add(MasterItem.Bank_Name);// for saving bankname
                    filter_names.add("company.companyID");
                    filter_params.add(paramJObj.optString(Constants.companyKey));
                    filter_names.add("value");
                    String bankname = paymentJson.optString("paymentthrough");//Recive PAYMENT
                    if (StringUtil.isNullOrEmpty(bankname)) {
                        bankname = paymentJson.optString("bankname");  //Cash Sales
                    }
                    filter_params.add(bankname);
                    filterRequestParams.put("filter_names", filter_names);
                    filterRequestParams.put("filter_params", filter_params);
                    KwlReturnObject cntResult = accMasterItemsDAOobj.getMasterItems(filterRequestParams);
                    int count = cntResult.getRecordTotalCount();

                    if (cntResult != null && cntResult.getEntityList() != null && !cntResult.getEntityList().isEmpty() && count == 1) {
                        MasterItem masterItem = (MasterItem) cntResult.getEntityList().get(0);
                        if (masterItem != null) {
                            paymentJson.put("paymentthroughid", masterItem.getID());
                            if (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Invoice_ModuleId))) {
                                paymentJson.put("bankmasteritemid", masterItem.getID());
                            }
                        }

                    } else if (count < 1) {//else it will masteritem and then put into the masteritem value
                        filterRequestParams = new HashMap<String, Object>();
                        filterRequestParams.put(Constants.companyKey, paramJObj.optString(Constants.companyKey));
                        filterRequestParams.put("name", bankname);
                        filterRequestParams.put("groupid", MasterItem.Bank_Name);
                        cntResult = accMasterItemsDAOobj.addMasterItem(filterRequestParams);
                        if (cntResult.getEntityList() != null) {
                            MasterItem masterItem = (MasterItem) cntResult.getEntityList().get(0);
                            paymentJson.put("paymentthroughid", masterItem.getID());
                            if (moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Invoice_ModuleId))) {
                                paymentJson.put("bankmasteritemid", masterItem.getID());
                            }

                        }
                    }
                } else {
                    String postdate=null;
                    if (paymentJson.has("postdate") && !StringUtil.isNullOrEmpty(paymentJson.optString("postdate"))) {
                        postdate = paymentJson.optString("postdate",null) == null ? null : paymentJson.optString("postdate");
                    }
                    if (!StringUtil.isNullOrEmpty(postdate) && !paramJObj.optBoolean(Constants.isMultiGroupCompanyFlag)) {  //ERP-9230 : //Used same date formatter which have used to save currency exchange
                        Date sdate = authHandler.getUserDateFormatterWithoutTimeZone(paramJObj).parse(postdate);
                        DateFormat df = authHandler.getDateOnlyFormat();
                        postdate = df.format(sdate);
                        paymentJson.put("postdate", postdate);
                    }
                    
                    String clearancedate = null;
                    if (paymentJson.has("clearancedate") && !StringUtil.isNullOrEmpty(paymentJson.optString("clearancedate"))) {
                        clearancedate = paymentJson.optString("clearancedate", null) == null ? null : paymentJson.optString("clearancedate");
                    }
                    
                    if (!StringUtil.isNullOrEmpty(clearancedate) && !paramJObj.optBoolean(Constants.isMultiGroupCompanyFlag)) { 
                        Date sdate = authHandler.getUserDateFormatterWithoutTimeZone(paramJObj).parse(clearancedate);
                        DateFormat df = authHandler.getDateOnlyFormat();
                        clearancedate = df.format(sdate);
                        paymentJson.put("clearancedate", clearancedate);
                    }
                    
                    paymentJson.put("chequeno", paymentJson.optString("chequenumber"));
                    paymentJson.put("payDate", paymentJson.optString("postdate"));

                    if (paymentJson.has("paymentstatus") && !StringUtil.isNullOrEmpty(paymentJson.optString("paymentstatus", null))) {
                        paymentJson.put("paymentStatus", paymentJson.optString("paymentstatus"));
                    }

                    if (!paymentJson.has("paymentstatus") || StringUtil.isNullOrEmpty(paymentJson.optString("paymentstatus", null))) {
                        paymentJson.put("paymentStatus", "Uncleared");
                    }
                    
                    requestParams.clear();
                    if ((paymentJson.has("paymentthroughvalue") && !StringUtil.isNullOrEmpty(paymentJson.getString("paymentthroughvalue")))) {
                        paymentJson.put("paymentthrough", paymentJson.optString("paymentthroughvalue"));
                        paymentJson.put("bankname", StringUtil.DecodeText(paymentJson.optString("paymentthroughvalue")));
                        String bankName = paymentJson.optString("paymentthroughvalue");
                        requestParams.put("tableName", "MasterItem");
                        requestParams.put("fetchColumn", "ID");
                        requestParams.put("condtionColumn", "value");
                        requestParams.put("condtionColumnvalue", bankName);
                        requestParams.put("companyColumn", "company.companyID");
                        requestParams.put(Constants.companyKey, destinationCompanyid);

                        KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);
                        if (!moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Make_Payment_ModuleId))) {
                            if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                                paymentJson.put("paymentthroughid", result.getEntityList().get(0).toString());
                                paymentJson.put("bankmasteritemid", result.getEntityList().get(0).toString());
                                paymentJson.remove("paymentthroughvalue");
                            } else {
                                throw ServiceException.FAILURE("Bank Name does not exist", "erp31{" + "paymentthroughvalue" + "}", false);
                            }
                        }
                    }
                }
                paramJObj.put("paydetail", paymentJson);
            }

            requestParams.clear();
            if ((paramJObj.has("accountvalue") && !StringUtil.isNullOrEmpty(paramJObj.optString("accountvalue", null)))) {
                String accountvalue = paramJObj.optString("accountvalue");
                requestParams.put("tableName", "Account");
                requestParams.put("fetchColumn", "ID");
                requestParams.put("condtionColumn", "acccode");
                requestParams.put("condtionColumnvalue", accountvalue);
                requestParams.put("companyColumn", "company.companyID");
                requestParams.put(Constants.companyKey, destinationCompanyid);

                KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                    paramJObj.put("accountid", result.getEntityList().get(0).toString());
                    paramJObj.put("mappingcusaccid", result.getEntityList().get(0).toString());
                    paramJObj.remove("accountvalue");

                } else {
                    throw ServiceException.FAILURE("Account Code does not exist", "erp31{" + "accountvalue" + "}", false);
                }
            } else if (paramJObj.optInt(Constants.modulename, 0) == Constants.Acc_Customer_ModuleId && paramJObj.optBoolean(Constants.isMultiGroupCompanyFlag) == false) {//Not checking for Group of Companies
                //Fetch Information from Dummy Table
                HashMap<String, Object> dummyParams = new HashMap<String, Object>();
                    dummyParams.put(Constants.modulename, String.valueOf(Constants.Acc_Customer_ModuleId));
                dummyParams.put(Constants.companyKey, paramJObj.optString(Constants.companyKey));
                KwlReturnObject dummyAccountresult = accountingHandlerDAOobj.getAccountidforDummyAccount(dummyParams);

                JSONObject dummyDetailJson = null;
                String dummydetailString = null;
                List<String> configList = dummyAccountresult.getEntityList();
                if (configList != null && configList.size() > 0) {
                    dummydetailString = (String) configList.get(0);
                    if (!StringUtil.isNullOrEmpty(dummydetailString)) {
                        dummyDetailJson = new JSONObject(dummydetailString);
                        if (dummyDetailJson.has("accountid") && !StringUtil.isNullOrEmpty(dummyDetailJson.optString("accountid", null))) {
                            paramJObj.put("mappingcusaccid", dummyDetailJson.optString("accountid"));
                            paramJObj.put("accountid", dummyDetailJson.optString("accountid"));
                        }
                    }//end of config list
                } else {
                    Account cogsAccount = null;
                    KwlReturnObject retObj = accAccountDAOobj.getAccountFromName(destinationCompanyid, "Trade Debtors");
                    if (retObj != null && !retObj.getEntityList().isEmpty()) {
                        cogsAccount = (Account) retObj.getEntityList().get(0);
                        paramJObj.put("mappingcusaccid", cogsAccount.getID());
                        paramJObj.put("accountid", cogsAccount.getID());
                    }

                }
            }
            if (!(moduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Receive_Payment_ModuleId)))) {
                //custom field json create on the basis of fieldlabel and fieldvalue
                String customField = paramJObj.optString(Constants.customfield, null);
                if (!StringUtil.isNullOrEmpty(customField) && !StringUtil.isNullOrEmpty(moduleid)) {
                    JSONArray customJArray = createJSONForCustomField(customField, paramJObj.optString(Constants.companyKey), Integer.parseInt(moduleid));
                    customJArray = fieldDataManagercntrl.GetJsonArrayUsingFieldIds(customJArray, Integer.parseInt(moduleid), paramJObj.optString(Constants.companyKey), true);
                    paramJObj.put(Constants.customfield, customJArray);
                }
            }
            
            //Customer Addresses:- Not checking for Group of Companies
            if (paramJObj.has("addressdetails") && !StringUtil.isNullOrEmpty(paramJObj.optString("addressdetails")) && paramJObj.optBoolean(Constants.isMultiGroupCompanyFlag) == false) {
                JSONArray detailArr = new JSONArray(paramJObj.optString("addressdetails", "[{}]"));
                if (detailArr.length() > 0) {
                    JSONArray addDetailResultArr = new JSONArray();
                    for (int i = 0; i < detailArr.length(); i++) {
                        JSONObject addressJson = detailArr.getJSONObject(i);
                        if (addressJson.optBoolean("isBillingAddress")) {//Billing Address
                            addressJson.put("aliasNameID", addressJson.optString("aliasname", "Billing Address1"));
                            addressJson.put("aliasName", addressJson.optString("aliasname", "Billing Address1"));
                        } else {//Shipping Address
                            addressJson.put("aliasNameID", addressJson.optString("aliasname", "Shipping Address1"));
                            addressJson.put("aliasName", addressJson.optString("aliasname", "Shipping Address1"));
                        }
                        addressJson.put("address", addressJson.optString("addressvalue"));
                        addressJson.put("city", addressJson.optString("city"));
                        addressJson.put("state", addressJson.optString("state"));
                        addressJson.put("country", addressJson.optString("country"));
                        addressJson.put("postalCode", addressJson.optString("postalCode"));
                        addressJson.put("phone", addressJson.optString("phone"));
                        addressJson.put("mobileNumber", addressJson.optString("mobileNumber"));
                        addressJson.put("fax", addressJson.optString("fax"));
                        addressJson.put("emailID", addressJson.optString("emailID"));
                        addressJson.put("contactPerson", addressJson.optString("contactPerson"));
                        addressJson.put("recipientName", addressJson.optString("recipientName"));
                        addressJson.put("contactPersonNumber", addressJson.optString("contactPersonNumber"));
                        addressJson.put("contactPersonDesignation", addressJson.optString("contactPersonDesignation"));
                        addressJson.put("website", addressJson.optString("website"));
                        addressJson.put("shippingRoute", addressJson.optString("shippingRoute"));
                        addressJson.put("isDefaultAddress", addressJson.optBoolean("isDefaultAddress", true));
                        addDetailResultArr.put(addressJson);
                    }
                    paramJObj.put("addressDetail", addDetailResultArr.toString());
                    paramJObj.remove("addressdetails");
                }
            }
        } catch (JSONException e) {
            throw ServiceException.FAILURE("Exception occurred while populating masters information", "erp23", false);
        } catch (SessionExpiredException e) {
            throw ServiceException.FAILURE("Exception occurred while populating masters information", "erp23", false);
        }
        return globalFieldJson;
    }

    /*
     * @Description:Replace the ids of Global Level Fields.Fields like Product code,Base UOM,UOM,Tax,Rate Including GST,Base UOM Rate & Base UOM Quantity.
     * @param: JSONObject paramJObj,String destinationcompanyid
     */
    public JSONObject manipulateLineLevelFieldsNew(JSONObject paramJObj, String companyid) throws ServiceException, SessionExpiredException, ParseException {
        JSONObject lineFieldJson = paramJObj;
        double totaltaxamount=0.0;
        String linkedmoduleid = null;//to track linked module
        Set<String> linkedDocumentidsSet = new HashSet<>();
        HashMap<String, Object> fieldrequestParams = new HashMap();
        Map<String, String> requestParams = new HashMap<>();
        boolean gstIncluded = paramJObj.optBoolean("gstIncluded", paramJObj.optBoolean("includingGST", false));
        String moduleid = paramJObj.optString(Constants.moduleid);//for discount moduleid
        KwlReturnObject extracompanyObject = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
        ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracompanyObject.getEntityList().get(0);
        String countryid = "0";
        KwlReturnObject companyresult = accountingHandlerDAOobj.getObject(Company.class.getName(),companyid);
        Company companyObj = (Company) companyresult.getEntityList().get(0);
        if (companyObj.getCountry() != null) {
            countryid =companyObj.getCountry().getID();
        }
        
        try {
            if (lineFieldJson.has(Constants.detail) && !StringUtil.isNullOrEmpty(lineFieldJson.optString(Constants.detail, null))) {
                JSONArray detailArr = new JSONArray(lineFieldJson.optString(Constants.detail, "[{}]"));
                JSONArray detailResultArr = new JSONArray();

                for (int i = 0; i < detailArr.length(); i++) {
                    requestParams.clear();
                    //detailObj holds information about each row at line level
                    JSONObject detailObj = detailArr.getJSONObject(i);
                    detailObj.put(Constants.externalcurrencyrate, lineFieldJson.optString(Constants.externalcurrencyrate, "1"));
                    if (gstIncluded && paramJObj.optBoolean(Constants.isMultiGroupCompanyFlag)==false) {
                        detailObj.put("rateIncludingGst", detailObj.optString("rate", "0"));
                    }
                    
                    //to save podetailsid in sodetailsid we need to calculate podetails on the basis of po billid & productid
                    if (paramJObj.has(Constants.isMultiGroupCompanyFlag) && paramJObj.optBoolean(Constants.isMultiGroupCompanyFlag)) {
                        Map<String, Object> detailParams = new HashMap<String, Object>();
                        String sourcemoduleid = paramJObj.optString(GroupCompanyProcessMapping.SOURCE_MODULE);
                        String destinationmoduleid = paramJObj.optString(GroupCompanyProcessMapping.DESTINATION_MODULE);
                        if (sourcemoduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Purchase_Order_ModuleId)) && destinationmoduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Sales_Order_ModuleId))) {
                            detailParams.put(Constants.productid, detailObj.optString(Constants.productid));
                            detailParams.put("purchaseorder", paramJObj.optString(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID));
                            String detailid = accGroupCompanyDAO.fetchDetailsid(paramJObj, detailParams);
                            detailObj.put("sourcepurchaseorderdetailid", detailid);
                        }

                        if (sourcemoduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Goods_Receipt_ModuleId)) && destinationmoduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Delivery_Order_ModuleId))) {
                            detailParams = new HashMap<String, Object>();
                            detailParams.put(Constants.productid, detailObj.optString(Constants.productid));
                            detailParams.put("goodsreceiptorder", paramJObj.optString(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID));
                            String detailid = accGroupCompanyDAO.fetchDetailsid(paramJObj, detailParams);
                            detailObj.put("sourcegrorderdetailid", detailid);
                        }

                    }
                    
                    if (detailObj.has("desc") && !StringUtil.isNullOrEmpty(detailObj.optString("desc", null))) {
                        detailObj.put("description", detailObj.optString("desc"));
                    }else if(detailObj.has("description") && detailObj.get("description")!=null){
                        detailObj.put("description", detailObj.optString("description"));
                    }
                    double prdiscount = detailObj.has("discount") ? detailObj.optDouble("discount", 0) : detailObj.optDouble("prdiscount", 0);
                    int discountType = detailObj.has("discountType") ? detailObj.optInt("discountType", 1) : detailObj.optInt("discountispercent", 1);
                    if (paramJObj.optBoolean(Constants.isdefaultHeaderMap)) {
                         detailObj.put("discount",prdiscount);
                         detailObj.put("discountType", discountType);
                    }
                    //Get baseuomID from baseUOMNanme
                    requestParams.clear();
                    if ((detailObj.has("baseuomvalue") && !StringUtil.isNullOrEmpty(detailObj.getString("baseuomvalue")))
                            || (detailObj.has("baseuomname") && !StringUtil.isNullOrEmpty(detailObj.getString("baseuomname")))
                            ||(detailObj.has("baseuomid") && paramJObj.optBoolean(Constants.isMultiGroupCompanyFlag,false)==true)) {
                        String baseUOMValue = "";
                        if(paramJObj.optBoolean(Constants.isMultiGroupCompanyFlag,false)&& detailObj.has("baseuomid") && detailObj.get("baseuomid")!=null){
                            KwlReturnObject unitMeasureObject = accountingHandlerDAOobj.getObject(UnitOfMeasure.class.getName(), detailObj.getString("baseuomid"));
                            UnitOfMeasure unitMeasure = (UnitOfMeasure) unitMeasureObject.getEntityList().get(0);
                            if(unitMeasure!=null){
                             baseUOMValue = unitMeasure.getNameEmptyforNA();
                            }
                        }else if ((detailObj.has("baseuomvalue") && !StringUtil.isNullOrEmpty(detailObj.getString("baseuomvalue")))) {
                            baseUOMValue = detailObj.getString("baseuomvalue");
                        } else {
                            baseUOMValue = detailObj.getString("baseuomname");
                        }

                        requestParams.put("tableName", "UnitOfMeasure");
                        requestParams.put("fetchColumn", "ID");
                        requestParams.put("condtionColumn", "name");
                        requestParams.put("condtionColumnvalue", baseUOMValue);
                        requestParams.put("companyColumn", "company.companyID");
                        requestParams.put(Constants.companyKey, companyid);

                        KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                        if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                            detailObj.put("baseuomid", result.getEntityList().get(0).toString());
                            detailObj.remove("baseuomvalue");
                        } else {
                            throw ServiceException.FAILURE("baseUOMValue does not exist", "erp31{" + "baseuomvalue" + "}", false);
                        }
                    }

                    //Get uomID from UOMNanme
                    requestParams.clear();
                    if ((detailObj.has("uomvalue") && !StringUtil.isNullOrEmpty(detailObj.getString("uomvalue"))) || (detailObj.has("uomname") && !StringUtil.isNullOrEmpty(detailObj.getString("uomname")))
                            ||(detailObj.has("uomid") && paramJObj.optBoolean(Constants.isMultiGroupCompanyFlag,false)==true)) {
                        String uomValue = "";
                        
                        if (paramJObj.optBoolean(Constants.isMultiGroupCompanyFlag, false) && detailObj.has("uomid") && detailObj.get("uomid")!=null) {
                            KwlReturnObject unitMeasureObject = accountingHandlerDAOobj.getObject(UnitOfMeasure.class.getName(), detailObj.getString("uomid"));
                            UnitOfMeasure unitMeasure = (UnitOfMeasure) unitMeasureObject.getEntityList().get(0);
                            if (unitMeasure != null) {
                                uomValue = unitMeasure.getNameEmptyforNA();
                            }
                        } else if (detailObj.has("uomvalue") && !StringUtil.isNullOrEmpty(detailObj.getString("uomvalue"))) {
                            uomValue = detailObj.getString("uomvalue");
                        } else {
                            uomValue = detailObj.getString("uomname");
                        }
                        requestParams.put("tableName", "UnitOfMeasure");
                        requestParams.put("fetchColumn", "ID");
                        requestParams.put("condtionColumn", "name");
                        requestParams.put("condtionColumnvalue", uomValue);
                        requestParams.put("companyColumn", "company.companyID");
                        requestParams.put(Constants.companyKey, companyid);

                        KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                        if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                            detailObj.put("uomid", result.getEntityList().get(0).toString());
                            detailObj.put("uomname", result.getEntityList().get(0).toString());
                            detailObj.remove("uomvalue");
                            if (!detailObj.has("baseuomvalue") && paramJObj.optBoolean(Constants.isMultiGroupCompanyFlag,false)==false) {
                                detailObj.put("baseuomid", result.getEntityList().get(0).toString());
                            }

                        } else {
                            throw ServiceException.FAILURE("uomValue does not exist", "erp31{" + "uomvalue" + "}", false);
                        }
                    }

                    //Get product id from  product name & calculating baseuomrate and baseuomquantity
                    /*
                     * Mandatory= uomid
                     */
                    if ((detailObj.has("productvalue") && !StringUtil.isNullOrEmpty(detailObj.getString("productvalue"))) || (detailObj.has("pid") && !StringUtil.isNullOrEmpty(detailObj.getString("pid")))) {
                        String productvalue = "";
                        if (detailObj.has("productvalue") && !StringUtil.isNullOrEmpty(detailObj.getString("productvalue"))) {
                            productvalue = detailObj.getString("productvalue");
                        } else {
                            productvalue = detailObj.getString("pid");
                        }

                        requestParams.put("tableName", "Product");
                        requestParams.put("fetchColumn", "ID");
                        requestParams.put("condtionColumn", "productid");
                        requestParams.put("condtionColumnvalue", productvalue);
                        requestParams.put("companyColumn", "company.companyID");
                        requestParams.put(Constants.companyKey, companyid);

                        KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                        if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                            detailObj.put(Constants.productid, result.getEntityList().get(0).toString());
                            detailObj.remove("productvalue");

                            KwlReturnObject productreturnObj = accountingHandlerDAOobj.getObject(Product.class.getName(), result.getEntityList().get(0).toString());
                            Product productObj = (Product) productreturnObj.getEntityList().get(0);
                            
                            /**
                             * Adding below batch-serial flags into JSON
                             * temporarily as these are used in the method where
                             * scope of productObj is not available 
                             * These keys are reomoved from JSON at the end of method
                             * ERM-335
                             */
                            detailObj.put("isBatchForProduct", productObj.isIsBatchForProduct());
                            detailObj.put("isSerialForProduct", productObj.isIsSerialForProduct());
                            
                            double quantity = detailObj.optDouble("quantity", 1.0);
                            if (detailObj.has("deliveredquantity") && detailObj.get("deliveredquantity") != null) {
                                quantity = detailObj.optDouble("deliveredquantity", 1.0);
                                detailObj.put("dquantity", detailObj.opt("deliveredquantity"));
                            }
                            if (detailObj.has("receivedquantity") && detailObj.get("receivedquantity") != null) {//For GRN
                                quantity = detailObj.optDouble("receivedquantity", 1.0);
                                detailObj.put("dquantity", detailObj.opt("receivedquantity"));
                            }
                            if (detailObj.has("dquantity") && detailObj.get("dquantity") != null) {//When dquantity exists in request JSON
                                quantity = detailObj.optDouble("dquantity", 1.0);
                            }
                                
                            UnitOfMeasure uom = productObj.getUnitOfMeasure();
                            if (uom != null) {
                                detailObj.put("baseuomid", uom.getID());
//                            detailObj.put("baseUOMValue",  uom.getName());
                            }
                            /**
                             * While creating GRN from jsp page we have
                             * baseuomquantity. Using this baseuomquantity we
                             * calculate baseuomrate of purchased product.
                             * ERM-319, ERM-335
                             */
                            if (detailObj.has("baseuomquantity") && detailObj.optDouble("baseuomquantity") != 0d) {
                                double baseuomrate = (detailObj.optDouble("baseuomquantity")) / quantity;
                                detailObj.put("baseuomrate", baseuomrate);
                            } else if (extraCompanyPreferences != null && extraCompanyPreferences.getUomSchemaType() == Constants.UOMSchema) {
                                String uomschematypeid = productObj.getUomSchemaType() != null ? productObj.getUomSchemaType().getID() : "";
                                double baseUOMRate = 1;
                                if (!StringUtil.isNullOrEmpty(uomschematypeid) && productObj.isMultiuom()) {
                                    HashMap<String, Object> prorequestParams = productHandler.getProductRequestMapfromJson(lineFieldJson);
                                    prorequestParams.put("uomschematypeid", uomschematypeid);
                                    prorequestParams.put("currentuomid", detailObj.optString("uomid"));
                                    prorequestParams.put(Constants.companyKey, companyid);
                                    prorequestParams.put("carryin", false);
                                    KwlReturnObject res = accProductObj.getProductBaseUOMRate(prorequestParams);
                                    List list = res.getEntityList();
                                    Iterator itr = list.iterator();
                                    if (itr.hasNext()) {
                                        UOMSchema row = (UOMSchema) itr.next();
                                        if (row != null) {
                                            baseUOMRate = row.getBaseuomrate();
                                        }
                                    }
                                    double basuomquantity = authHandler.calculateBaseUOMQuatity(quantity, baseUOMRate, companyid);
                                    detailObj.put("baseuomrate", baseUOMRate);
                                    detailObj.put("baseuomquantity", basuomquantity);
                                } else {
                                    detailObj.put("baseuomrate", 1);
                                    detailObj.put("baseuomquantity", quantity);
                                }

                            } else if (extraCompanyPreferences != null && extraCompanyPreferences.getUomSchemaType() == Constants.PackagingUOM) {
                                String uomid = detailObj.optString("uomid", null);
                                if (!StringUtil.isNullOrEmpty(uomid)) {
                                    KwlReturnObject uomreturnObj = accountingHandlerDAOobj.getObject(UnitOfMeasure.class.getName(), uomid);
                                    UnitOfMeasure uomObj = (UnitOfMeasure) uomreturnObj.getEntityList().get(0);
                                    if (!StringUtil.isNullObject(productObj.getPackaging())) {
                                        Double basepackaingrate = productObj.getPackaging().getStockUomQtyFactor(uomObj);
                                        detailObj.put("baseuomrate", basepackaingrate);
                                        double basuomquantity = authHandler.calculateBaseUOMQuatity(quantity, basepackaingrate, companyid);
                                        detailObj.put("baseuomquantity", basuomquantity);
                                    } else {
                                        detailObj.put("baseuomrate", 1);
                                        detailObj.put("baseuomquantity", quantity);
                                    }
                                } else {
                                    detailObj.put("baseuomrate", 1);
                                    detailObj.put("baseuomquantity", quantity);
                                }
                            } else {
                                detailObj.put("baseuomrate", 1);
                                detailObj.put("baseuomquantity", quantity);
                            }

                        } else {
                            throw ServiceException.FAILURE("productValue does not exist", "erp31{" + "productValue" + "}", false);
                        }
                    }
                    
                 //This block of code should be after productvalue as we are calculating new productid for multicompany 
                        if (!StringUtil.isNullOrEmpty(paramJObj.optString(GroupCompanyProcessMapping.LinkModule_Combo, null)) && !StringUtil.isNullOrEmpty(paramJObj.optString("linkNumber", null))
                                && paramJObj.has(Constants.isMultiGroupCompanyFlag) && paramJObj.optBoolean(Constants.isMultiGroupCompanyFlag)) {
                            String linkedSourceTransactionModule = null;
                            String linkedDestiantionTransactionModule = null;
                            String destinationmoduletablename = null;
                            String sourcemoduledetailstablename = null;
                            String destinationmoduledetailstablename = null;
                            boolean islinkComboFlag = false;

                            // to calculate linked detailsid
                            Map<String, Object> valuesParams = new HashMap<String, Object>();// to put values
                            String sourcemoduleid = paramJObj.optString(GroupCompanyProcessMapping.SOURCE_MODULE);
                            String destinationmoduleid = paramJObj.optString(GroupCompanyProcessMapping.DESTINATION_MODULE);

                            if (sourcemoduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Goods_Receipt_ModuleId)) && destinationmoduleid.equalsIgnoreCase(String.valueOf(Constants.Acc_Delivery_Order_ModuleId))) {
                                if (paramJObj.optString(GroupCompanyProcessMapping.LinkModule_Combo).equals(Constants.ACC_PURCHASE_ORDER)) {
                                    linkedmoduleid = Constants.SALESORDER;
                                    linkedSourceTransactionModule = String.valueOf(Constants.Acc_Purchase_Order_ModuleId);
                                    linkedDestiantionTransactionModule = String.valueOf(Constants.Acc_Sales_Order_ModuleId);
                                    destinationmoduledetailstablename = "sodetails";
                                    destinationmoduletablename = "salesorder";
                                    sourcemoduledetailstablename = "sourcepodetailsid";
                                    valuesParams.put(Constants.productid, detailObj.optString(Constants.productid));
                                    valuesParams.put(sourcemoduledetailstablename, detailObj.optString("rowid"));//podetailsid
                                    islinkComboFlag = true;
                                }
                            }

                            if (islinkComboFlag) {
   
                                String[] linkNumbers = paramJObj.optString("linkNumber").split(",");
                                for (int io = 0; io < linkNumbers.length; io++) {
                                    String grid = linkNumbers[io];

                                    fieldrequestParams = new HashMap();
                                    fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_MODULE, linkedSourceTransactionModule);
                                    fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_TRANSACTIONID, grid);
                                    fieldrequestParams.put(GroupCompanyProcessMapping.DESTINATION_MODULE, linkedDestiantionTransactionModule);
                                    KwlReturnObject result = accGroupCompanyDAO.fetchTransactionMappingDetails(fieldrequestParams);
                                    List<GroupCompanyTransactionMapping> multiTransObj = result.getEntityList();

                                    if (multiTransObj.size() > 0) {
                                        for (GroupCompanyTransactionMapping multTMObj : multiTransObj) {
                                            String destinationlinkedTransactionId = multTMObj.getDestinationTransactionid();
                                            linkedDocumentidsSet.add(destinationlinkedTransactionId);

                                            valuesParams.put(destinationmoduletablename, destinationlinkedTransactionId);//linked deliveryorder transaction id
                                            String destinationlinkeddetailsid = accGroupCompanyDAO.fetchlinkedDetailsid(valuesParams, destinationmoduledetailstablename, destinationmoduletablename, sourcemoduledetailstablename);
                                            if (!StringUtil.isNullOrEmpty(destinationlinkeddetailsid)) {// if sodetails id is not present for that salesorder
                                                detailObj.put("rowid", destinationlinkeddetailsid);
                                            }
                                        }
                                    } else {
                                        JSONObject response = StringUtil.getErrorResponse("erp40", paramJObj, "Cannot find Sales Order to which Purchase Order was generated. Please select another Purchase Order.", messageSource);
                                        throw ServiceException.FAILURE(response.optString(Constants.RES_MESSAGE), "", false);
                                    }
                                }//end of linknumberslength for loop
                            }//end of islinkcomboflag
                        }//end of linknumber flag & multigroupcompanyflag

                    //Get pricingbandid from pricingbandName
                    requestParams.clear();
                    if ((detailObj.has("pricingbandmastervalue") && !StringUtil.isNullOrEmpty(detailObj.getString("pricingbandmastervalue")))) {
                        String pricingBandMasterValue = detailObj.getString("pricingbandmastervalue");
                        requestParams.put("tableName", "PricingBandMaster");
                        requestParams.put("fetchColumn", "ID");
                        requestParams.put("condtionColumn", "name");
                        requestParams.put("condtionColumnvalue", pricingBandMasterValue);
                        requestParams.put("companyColumn", "company.companyID");
                        requestParams.put(Constants.companyKey, companyid);

                        KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                        if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                            detailObj.put("pricingbandmasterid", result.getEntityList().get(0).toString());
                            detailObj.remove("pricingbandmastervalue");

                        }
                    }

                    //Get Product AccountID from roduct Accountname
                    requestParams.clear();
                    if ((detailObj.has("productaccountvalue") && !StringUtil.isNullOrEmpty(detailObj.getString("productaccountvalue")))) {
                        String productAccountValue = detailObj.getString("productaccountvalue");
                        requestParams.put("tableName", "Account");
                        requestParams.put("fetchColumn", "ID");
                        requestParams.put("condtionColumn", "name");
                        requestParams.put("condtionColumnvalue", productAccountValue);
                        requestParams.put("companyColumn", "company.companyID");
                        requestParams.put(Constants.companyKey, companyid);

                        KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                        if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                            detailObj.put("productaccountid", result.getEntityList().get(0).toString());
                            detailObj.remove("productaccountvalue");

                        } else {
                            throw ServiceException.FAILURE("productAccountValue does not exist", "erp31{" + "productaccountvalue" + "}", false);
                        }
                    }

                    
                    /*
                     * @Description: Reason combo field at line item while converting Purchase Return to Sales Return.If master item is not present for another company then it will add the reason value in database for another company.
                     * Only handled for multigroup companies. Not  for squats.
                     * @param: reason id
                     * return: reason id for another company
                     *                      */
                    if (paramJObj.optBoolean(Constants.isMultiGroupCompanyFlag)==true) {
                        if (!StringUtil.isNullOrEmpty(detailObj.optString("reason", null))) {
                            KwlReturnObject reasonResult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), detailObj.optString("reason"));
                            MasterItem reasonObj = (MasterItem) reasonResult.getEntityList().get(0);
                            if (reasonObj != null) {
                                HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                                filter_names.add("masterGroup.ID");
                                filter_params.add(MasterItem.Reason);// for saving reason
                                filter_names.add("company.companyID");
                                filter_params.add(companyid);
                                filter_names.add("value");
                                filter_params.add(reasonObj.getValue());
                                filterRequestParams.put("filter_names", filter_names);
                                filterRequestParams.put("filter_params", filter_params);
                                KwlReturnObject cntResult = accMasterItemsDAOobj.getMasterItems(filterRequestParams);
                                int count = cntResult.getRecordTotalCount();

                                if (cntResult != null && cntResult.getEntityList() != null && !cntResult.getEntityList().isEmpty() && cntResult.getEntityList().get(0)!=null && count == 1) {
                                    reasonObj=(MasterItem)cntResult.getEntityList().get(0);
                                    if (reasonObj != null) {
                                        detailObj.put("reason",reasonObj.getID());
                                    }
                                    
                                } else if (count < 1) {//else it will masteritem and then put into the masteritem value
                                    filterRequestParams = new HashMap<String, Object>();
                                    filterRequestParams.put(Constants.companyKey,companyid);
                                    filterRequestParams.put("name", reasonObj.getValue());
                                    filterRequestParams.put("groupid", MasterItem.Reason);
                                    cntResult = accMasterItemsDAOobj.addMasterItem(filterRequestParams);
                                    if (cntResult.getEntityList() != null && !cntResult.getEntityList().isEmpty() && cntResult.getEntityList().get(0)!=null ) {
                                        MasterItem masterItem = (MasterItem) cntResult.getEntityList().get(0);
                                        detailObj.put("reason", masterItem.getID());
                                    }
                                }
                            }
                        } //end of reason
                    } else if ((detailObj.has("reasonvalue") && !StringUtil.isNullOrEmpty(detailObj.optString("reasonvalue",null)))) {
                        String reasonName = detailObj.getString("reasonvalue");
                        requestParams.put("tableName", "MasterItem");
                        requestParams.put("fetchColumn", "ID");
                        requestParams.put("condtionColumn", "value");
                        requestParams.put("condtionColumnvalue", reasonName);
                        requestParams.put("companyColumn", "company.companyID");
                        requestParams.put(Constants.companyKey, companyid);

                        KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                        if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                            detailObj.put("reason", result.getEntityList().get(0).toString());
                            detailObj.remove("reasonvalue");
                        }
                    }
                    double quantity = detailObj.optDouble("quantity", 0.0);
                    if (detailObj.has("deliveredquantity") && detailObj.get("deliveredquantity") != null) {
                        quantity = detailObj.optDouble("deliveredquantity", 1.0);
                        detailObj.put("dquantity", detailObj.opt("deliveredquantity"));
                    }
                    if (detailObj.has("receivedquantity") && detailObj.get("receivedquantity") != null) {//For GoodsReceiptOrder
                        quantity = detailObj.optDouble("receivedquantity", 1.0);
                        detailObj.put("dquantity", detailObj.opt("receivedquantity"));
                    }
                    if (detailObj.has("dquantity") && detailObj.get("dquantity") != null) {
                        quantity = detailObj.optDouble("dquantity", 1.0);
                        detailObj.put("dquantity", detailObj.opt("dquantity"));
                    }
                    double baseuomquantity = detailObj.optDouble("baseuomquantity", quantity);
                    double rate = detailObj.optDouble("rate", 0.0);
                    double rowamount = (rate * baseuomquantity);
                    if (gstIncluded) {
                        rate = detailObj.optDouble("rateIncludingGst", 0.0);
                        rowamount = (rate * baseuomquantity);
                    }
                    double rowdiscountvalue = (discountType == 1) ? rowamount * prdiscount / 100 : prdiscount;
                    
                    //Fetching tax id line level
                    if ((detailObj.has("prtaxid") && !StringUtil.isNullOrEmpty(detailObj.optString("prtaxid", null)))
                            || (detailObj.has("producttaxvalue") && !StringUtil.isNullOrEmpty(detailObj.getString("producttaxvalue")))) {
                        String productTaxValue = null;
                        if (detailObj.has("producttaxvalue") && !StringUtil.isNullOrEmpty(detailObj.getString("producttaxvalue"))) {
                            productTaxValue = detailObj.getString("producttaxvalue");
                        } else if (paramJObj.optBoolean(Constants.isMultiGroupCompanyFlag)){
                            KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), detailObj.optString("prtaxid", null)); // (Tax)session.get(Tax.class, taxid);
                            Tax tax = (Tax) txresult.getEntityList().get(0);
                            if (tax != null) {
//                                productTaxValue = tax.getTaxCode();
                                fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_TAX_CODE, tax.getTaxCode());
                                fieldrequestParams.put(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN, lineFieldJson.optString(GroupCompanyProcessMapping.SOURCE_COMPANY_SUBDOMAIN));
                                fieldrequestParams.put(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN, lineFieldJson.optString(GroupCompanyProcessMapping.DESTINATION_COMPANY_SUBDOMAIN));
                                KwlReturnObject lineTaxObj = accGroupCompanyDAO.fetchTaxMappingDetails(fieldrequestParams);
                                List<GroupCompanyTaxMapping> lineleveltaxObj = lineTaxObj.getEntityList();
                                if (lineleveltaxObj.size() > 0) {
                                    for (GroupCompanyTaxMapping taxObj : lineleveltaxObj) {
                                        String destinationtaxCode = taxObj.getDestinationTaxCode();
                                        productTaxValue = destinationtaxCode;
                                    }
                                }
                            }
                        }
                        if (!StringUtil.isNullOrEmpty(productTaxValue)) {
                            requestParams = new HashMap<>();
                            requestParams.put("tableName", "Tax");
                            requestParams.put("fetchColumn", "ID");
                            requestParams.put("condtionColumn", "taxCode");
                            requestParams.put("condtionColumnvalue", productTaxValue);
                            requestParams.put("companyColumn", "company.companyID");
                            requestParams.put(Constants.companyKey, companyid);

                            KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                            if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                                detailObj.put("prtaxid", result.getEntityList().get(0).toString());

                                Map<String, Object> taxParams = new HashMap<String, Object>();;
                                taxParams.put("companyid", companyid);
                                taxParams.put("taxid", result.getEntityList().get(0).toString());

                                //To get tax percent of mapped tax
                                KwlReturnObject taxPresult = accTaxObj.getTax(taxParams);
                                List<Object[]> list = taxPresult.getEntityList();
                                double taxpercent = 0.0;
                                if (list != null && !list.isEmpty()) {
                                    for (Object[] row : list) {
                                        taxpercent = (Double) row[1];
                                    }
                                }
                                double taxamount = 0.0;
                                if (gstIncluded) {
                                    if (taxpercent != 0) {
                                        rate = (rate * 100) / (100 + taxpercent);
                                        detailObj.put("rate", rate);
//                                        double taxamount = baseuomquantity * (rateincludinggst - rate);
                                        taxamount = (taxpercent * rate) / 100;
                                        detailObj.put("taxamount", taxamount);

                                    } else {
                                        detailObj.put("rate", rate);
                                        detailObj.put("taxamount", 0);
                                    }
                                } else {
                                    if (taxpercent != 0) {
                                        rowamount = rowamount - rowdiscountvalue;
                                        taxamount = (taxpercent * rowamount) / 100;
                                        if (!paramJObj.optBoolean(Constants.isForPos)) {
                                            detailObj.put("taxamount", taxamount);
                                        }
                                        
                                    } else {
                                        detailObj.put("taxamount", 0);
                                    }
                                }
                            } else {
                                throw ServiceException.FAILURE("productTaxValue does not exist", "erp31{" + "producttaxvalue" + "}", false);
                            }
                        } else {
                            if (detailObj.has("taxamount")) {
                                detailObj.put("taxamount", 0);
                            }

                            if (detailObj.has("prtaxid")) {
                                detailObj.put("prtaxid", "");
                            }
                        }
                    }
                    if (paramJObj.optBoolean(Constants.isMultiGroupCompanyFlag,false)==false) {
                        detailObj.put("amount", rate * baseuomquantity - rowdiscountvalue);
                    }
                    
                    if (countryid.equals(String.valueOf(Constants.indian_country_id))) {
                        //Get Indian GST
                        requestParams.clear();
                        String statevalue = "";
                        JSONArray dimArray = new JSONArray();
                        JSONObject dimJobj = new JSONObject();
                        if ((paramJObj.has("statevalue") && !StringUtil.isNullOrEmpty(paramJObj.optString("statevalue", null)))) {
                            statevalue = paramJObj.optString("statevalue", null);
                            dimJobj.put("fieldname", "Custom_State");
                            dimJobj.put("gstmappingcolnum", 1);
                            dimJobj.put("dimvalue", statevalue);
                            dimArray.put(dimJobj);
                        }
                        String entityvalue = paramJObj.optString("entityvalue", paramJObj.optString(Constants.RES_CDOMAIN));
                        String productid = detailObj.optString(Constants.productid, null);
                        if (!StringUtil.isNullOrEmpty(entityvalue)) {
                            dimJobj = new JSONObject();
                            dimJobj.put("fieldname", "Custom_Entity");
                            dimJobj.put("gstmappingcolnum", 0);
                            dimJobj.put("dimvalue", entityvalue);
                            dimArray.put(dimJobj);
                        }
                        JSONObject newparamsJobj = new JSONObject();
                        newparamsJobj.put("isSEZIGST", false);
                        newparamsJobj.put("productids", productid);
                        newparamsJobj.put("termSalesOrPurchaseCheck", true);
                        newparamsJobj.put("transactiondate", paramJObj.optString("billdate"));
                        newparamsJobj.put("uniqueCase", paramJObj.optString("uniqueCase"));
                        newparamsJobj.put(Constants.companyKey, paramJObj.optString(Constants.companyKey));
                        newparamsJobj.put("dimArr", dimArray.toString());

                        HashMap<String, Object> rParams = AccountingManager.getGlobalParamsJson(paramJObj);
                        /**
                         * Function to get GST data from services
                         */
                        JSONObject data = accEntityGstService.getGSTForProduct(newparamsJobj, rParams);
                        String linelevel = (data.getJSONArray("prodTermArray") != null && data.getJSONArray("prodTermArray").getJSONObject(0) != null) ? data.getJSONArray("prodTermArray").getJSONObject(0).getString("LineTermdetails") : "[]";
                        detailObj = buildLineLevelTerms(linelevel, detailObj, false);
                        totaltaxamount +=detailObj.optDouble("recTermAmount");
//                    detailObj.put("LineTermdetails",buildLineLevelTerms(data,detailObj));

                    }

                    requestParams.clear();
                    //Get customer id from  customer name
                    if ((detailObj.has("customervalue") && !StringUtil.isNullOrEmpty(detailObj.getString("customervalue")))) {
                        String customerName = detailObj.getString("customervalue");
                        requestParams.put("tableName", "Customer");
                        requestParams.put("fetchColumn", "ID");
//                    requestParams.put("condtionColumn", "name");
                        requestParams.put("condtionColumn", "acccode");
                        requestParams.put("condtionColumnvalue", customerName);
                        requestParams.put("companyColumn", "company.companyID");
                        requestParams.put(Constants.companyKey, companyid);

                        KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                        if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                            detailObj.put("customer", result.getEntityList().get(0).toString());
                            detailObj.remove("customervalue");
                            
                        } else {
                            throw ServiceException.FAILURE("customerValue does not exist", "erp31{" + "customervalue" + "}", false);
                        }
                    }

                    requestParams.clear();
                    //Get Location id from  Location name
                    if ((detailObj.has("locationvalue") && !StringUtil.isNullOrEmpty(detailObj.getString("locationvalue")))) {
                        String locationValue = detailObj.getString("locationvalue");
                        requestParams.put("tableName", "Location");
                        requestParams.put("fetchColumn", "id");
                        requestParams.put("condtionColumn", "name");
                        requestParams.put("condtionColumnvalue", locationValue);
                        requestParams.put("companyColumn", "company.companyID");
                        requestParams.put(Constants.companyKey, companyid);

                        KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                        if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                            detailObj.put("location", result.getEntityList().get(0).toString());
                            detailObj.remove("locationvalue");

                        } else {
                            throw ServiceException.FAILURE("locationValue does not exist", "erp31{" + "locationvalue" + "}", false);
                        }
                    }

                    requestParams.clear();
                    //Get Warehouse id from  Warehouse name
                    if ((detailObj.has("warehousevalue") && !StringUtil.isNullOrEmpty(detailObj.getString("warehousevalue")))) {
                        String wareHouseValue = detailObj.getString("warehousevalue");
                        requestParams.put("tableName", "Store");
                        requestParams.put("fetchColumn", "id");
                        requestParams.put("condtionColumn", "abbreviation");
                        requestParams.put("condtionColumnvalue", wareHouseValue);
                        requestParams.put("companyColumn", "company.companyID");
                        requestParams.put(Constants.companyKey, companyid);

                        KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                        if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                            detailObj.put("warehouse", result.getEntityList().get(0).toString());
                            detailObj.remove("warehousevalue");

                        } else {
                            throw ServiceException.FAILURE("wareHouseValue does not exist", "erp31{" + "warehousevalue" + "}", false);
                        }
                    }
                    /**
                     * Code to process custom fields and Product module's custom fields
                     * Function 'createJSONForCustomField' fetched fieldId (UUID) using fieldlabel, and puts in the Json
                     */
                    String customField = detailObj.optString(Constants.customfield, null);
                    if (!StringUtil.isNullOrEmpty(customField)) {
                        JSONArray customJArray = createJSONForCustomField(customField, companyid, Integer.parseInt(moduleid));
                        customJArray = fieldDataManagercntrl.GetJsonArrayUsingFieldIds(customJArray,Integer.parseInt(moduleid), companyid, true);
                        detailObj.put(Constants.customfield, customJArray);
                    }
                    String productCustomField = detailObj.optString(Constants.productcustomfield, null);
                    if (!StringUtil.isNullOrEmpty(productCustomField)) {
                        JSONArray customJArray = createJSONForCustomField(productCustomField, companyid, Constants.Acc_Product_Master_ModuleId);
                        detailObj.put(Constants.productcustomfield, customJArray);
                    }
                    
                    detailObj = manipulateBatchSerialFields(companyid, detailObj,paramJObj);
                    detailResultArr.put(detailObj);
                }//end of detail for loop
                lineFieldJson.put(Constants.detail, detailResultArr);
            }
            if (totaltaxamount != 0.0) {//FOr Indian companies
                lineFieldJson.put("taxamount", totaltaxamount);
            }
            //if linkedmoduleid is not null and linked document has some value
            if (linkedDocumentidsSet.size() > 0 && !StringUtil.isNullOrEmpty(linkedmoduleid)) {
                StringBuilder linkNumberBuilderString = new StringBuilder();//to track linked documentsid
                for (String linkid : linkedDocumentidsSet) {
                    if (linkNumberBuilderString.length() > 0) {
                        linkNumberBuilderString.append("," + linkid);
                    } else {
                        linkNumberBuilderString.append(linkid);
                    }
                }
                paramJObj.put("linkNumber", linkNumberBuilderString.toString());
                paramJObj.put(GroupCompanyProcessMapping.LinkModule_Combo, linkedmoduleid);
            }
            
        } catch (JSONException e) {
            throw ServiceException.FAILURE("Exception occurred while populating masters information", "erp23", false);
        }
        return lineFieldJson;
    }

    /*
     * @Description:Replace the ids of Global Level Fields.Fields like batch,serial,warehouse,location,row,rack and bin
     * @param: JSONObject lineitemjson,String destinationcompanyid
     */
    public JSONObject manipulateBatchSerialFields(String companyid, JSONObject detailObj,JSONObject paramJObj) throws ServiceException, ParseException, SessionExpiredException {
        JSONObject batchSerialJson = detailObj;
        Map<String, String> requestParams = new HashMap<>();
        try {
            if (batchSerialJson.has("batchdetails") && !StringUtil.isNullOrEmpty(batchSerialJson.getString("batchdetails"))) {
                JSONArray batchSerialResultArr = new JSONArray();
                JSONArray batchDetailArr = new JSONArray(batchSerialJson.getString("batchdetails"));

                for (int j = 0; j < batchDetailArr.length(); j++) {
                    JSONObject batchSerialObj = batchDetailArr.getJSONObject(j);

                    requestParams.clear();
                    /**
                     * Case when only batch is activated for product
                     * Below check is to handle multi-uom functionality 
                     * implemented under ERM-319
                     */
                    if (detailObj.optBoolean("isBatchForProduct") && !detailObj.optBoolean("isSerialForProduct") && batchSerialObj.has("batchquantity") && batchSerialObj.get("batchquantity") != null) {
                        batchSerialObj.put("quantity", batchSerialObj.opt("batchquantity"));
                        batchSerialObj.remove("batchquantity");
                    } else if (detailObj.has("deliveredquantity") && detailObj.get("deliveredquantity") != null) {//For DeliveryOrder
                        batchSerialObj.put("quantity", detailObj.opt("deliveredquantity"));
                    } else if (detailObj.has("receivedquantity") && detailObj.get("receivedquantity") != null) {//For GoodsReceiptOrder
                        batchSerialObj.put("quantity", detailObj.opt("receivedquantity"));
                    } else if (detailObj.has("dquantity") && detailObj.get("dquantity") != null) {
                        batchSerialObj.put("quantity", detailObj.opt("dquantity"));
                    } else if (detailObj.has("quantity") && detailObj.get("quantity") != null) {
                        batchSerialObj.put("quantity", detailObj.opt("quantity"));
                    }

                    if ((batchSerialObj.has("locationvalue") && !StringUtil.isNullOrEmpty(batchSerialObj.getString("locationvalue"))) || (batchSerialObj.has("location") && !StringUtil.isNullOrEmpty(batchSerialObj.getString("location")))) {
                        String locationValue = "";
                        if (batchSerialObj.has("locationvalue") && !StringUtil.isNullOrEmpty(batchSerialObj.getString("locationvalue"))) {
                            locationValue = batchSerialObj.getString("locationvalue");
                        } else {
                            locationValue = batchSerialObj.getString("location");
                            KwlReturnObject locresult = accountingHandlerDAOobj.getObject(Location.class.getName(), locationValue);
                            Location locObj = (Location) locresult.getEntityList().get(0);
                            if (locObj != null) {
                                locationValue = locObj.getName();
                            }
                        }
                        requestParams.put("tableName", "Location");
                        requestParams.put("fetchColumn", "id");
                        requestParams.put("condtionColumn", "name");
                        requestParams.put("condtionColumnvalue", locationValue);
                        requestParams.put("companyColumn", "company.companyID");
                        requestParams.put(Constants.companyKey, companyid);

                        KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                        if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                            batchSerialObj.put("location", result.getEntityList().get(0).toString());
                            batchSerialObj.remove("locationvalue");
                        } else {
                            throw ServiceException.FAILURE("locationValue does not exist", "erp31{" + "locationvalue" + "}", false);
                        }
                    }

                    requestParams.clear();
                    //Get Warehouse id from  Warehouse name
                    if ((batchSerialObj.has("warehousevalue") && !StringUtil.isNullOrEmpty(batchSerialObj.getString("warehousevalue"))) || (batchSerialObj.has("warehouse") && !StringUtil.isNullOrEmpty(batchSerialObj.getString("warehouse")))) {
                        String wareHouseValue = "";
                        if ((batchSerialObj.has("warehousevalue") && !StringUtil.isNullOrEmpty(batchSerialObj.getString("warehousevalue")))) {
                            wareHouseValue = batchSerialObj.getString("warehousevalue");
                        } else {
                            wareHouseValue = batchSerialObj.getString("warehouse");
                            KwlReturnObject locresult = accountingHandlerDAOobj.getObject(Store.class.getName(), wareHouseValue);
                            Store storeObj = (Store) locresult.getEntityList().get(0);
                            if (storeObj != null) {
                                wareHouseValue = storeObj.getAbbreviation();
                            }
                        }
                        requestParams.put("tableName", "Store");
                        requestParams.put("fetchColumn", "id");
                        requestParams.put("condtionColumn", "abbreviation");
                        requestParams.put("condtionColumnvalue", wareHouseValue);
                        requestParams.put("companyColumn", "company.companyID");
                        requestParams.put(Constants.companyKey, companyid);

                        KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                        if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                            batchSerialObj.put("warehouse", result.getEntityList().get(0).toString());
                            batchSerialObj.remove("warehousevalue");
                        } else {
                            requestParams.put("tableName", "InventoryWarehouse");
                            requestParams.put("fetchColumn", "id");
                            requestParams.put("condtionColumn", "name");
                            requestParams.put("condtionColumnvalue", wareHouseValue);
                            requestParams.put("companyColumn", "company.companyID");
                            requestParams.put(Constants.companyKey, companyid);

                            result = accountingHandlerDAOobj.populateMasterInformation(requestParams);
                            if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                                batchSerialObj.put("warehouse", result.getEntityList().get(0).toString());
                                batchSerialObj.remove("warehousevalue");
                            } else {
                                throw ServiceException.FAILURE("wareHouseValue does not exist", "erp31{" + "warehousevalue" + "}", false);
                            }
                        }
                    }

                    requestParams.clear();
                    if ((batchSerialObj.has("productvalue") && !StringUtil.isNullOrEmpty(batchSerialObj.getString("productvalue")))) {
                        String productvalue = batchSerialObj.getString("productvalue");
                        requestParams.put("tableName", "Product");
                        requestParams.put("fetchColumn", "ID");
                        requestParams.put("condtionColumn", "name");
                        requestParams.put("condtionColumnvalue", productvalue);
                        requestParams.put("companyColumn", "company.companyID");
                        requestParams.put(Constants.companyKey, companyid);

                        KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                        if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                            batchSerialObj.put("productid", result.getEntityList().get(0).toString());
                            batchSerialObj.remove("productvalue");

                        } else {
                            throw ServiceException.FAILURE("productValue does not exist", "erp31{" + "productvalue" + "}", false);
                        }
                    }else if(detailObj.has(Constants.productid)&&detailObj.get(Constants.productid)!=null){
                        batchSerialObj.put(Constants.productid,detailObj.optString(Constants.productid));
                    }

                    //Get rowid from rowname
                    requestParams.clear();
                    if ((batchSerialObj.has("rowvalue") && !StringUtil.isNullOrEmpty(batchSerialObj.getString("rowvalue"))) || (batchSerialObj.has("row") && !StringUtil.isNullOrEmpty(batchSerialObj.getString("row")))) {
                        String rowValue = "";

                        if ((batchSerialObj.has("rowvalue") && !StringUtil.isNullOrEmpty(batchSerialObj.getString("rowvalue")))) {
                            rowValue = batchSerialObj.getString("rowvalue");
                        } else {
                            rowValue = batchSerialObj.getString("row");
                            KwlReturnObject locresult = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), rowValue);
                            StoreMaster storeMasterObj = (StoreMaster) locresult.getEntityList().get(0);
                            if (storeMasterObj != null) {
                                rowValue = storeMasterObj.getName();
                            }
                            requestParams.put("tableName", "StoreMaster");
                            requestParams.put("fetchColumn", "id");
                            requestParams.put("condtionColumn", "name");
                            requestParams.put("condtionColumnvalue", rowValue);
                            requestParams.put("companyColumn", "company.companyID");
                            requestParams.put(Constants.companyKey, companyid);

                            KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                            if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                                batchSerialObj.put("row", result.getEntityList().get(0).toString());
                                batchSerialObj.remove("rowvalue");

                            } else {
                                throw ServiceException.FAILURE("rowValue does not exist", "erp31{" + "rowvalue" + "}", false);
                            }
                        }

                        //Get rackid from rack
                        requestParams.clear();
                        if ((batchSerialObj.has("rackvalue") && !StringUtil.isNullOrEmpty(batchSerialObj.getString("rackvalue"))) || (batchSerialObj.has("rack") && !StringUtil.isNullOrEmpty(batchSerialObj.getString("rack")))) {
                            String rackValue = "";

                            if ((batchSerialObj.has("rackvalue") && !StringUtil.isNullOrEmpty(batchSerialObj.getString("rackvalue")))) {
                                rackValue = batchSerialObj.getString("rackvalue");
                            } else {
                                rackValue = batchSerialObj.getString("rack");
                                KwlReturnObject locresult = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), rowValue);
                                StoreMaster storeMasterObj = (StoreMaster) locresult.getEntityList().get(0);
                                if (storeMasterObj != null) {
                                    rowValue = storeMasterObj.getName();
                                }
                            }
                            requestParams.put("tableName", "StoreMaster");
                            requestParams.put("fetchColumn", "id");
                            requestParams.put("condtionColumn", "name");
                            requestParams.put("condtionColumnvalue", rackValue);
                            requestParams.put("companyColumn", "company.companyID");
                            requestParams.put(Constants.companyKey, companyid);

                            KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                            if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                                batchSerialObj.put("rack", result.getEntityList().get(0).toString());
                                batchSerialObj.remove("rackvalue");

                            } else {
                                throw ServiceException.FAILURE("productValue does not exist", "erp31{" + "rackvalue" + "}", false);
                            }
                        }

                        //Get bin from bin name
                        requestParams.clear();
                        if ((batchSerialObj.has("binvalue") && !StringUtil.isNullOrEmpty(batchSerialObj.getString("binvalue"))) || (batchSerialObj.has("bin") && !StringUtil.isNullOrEmpty(batchSerialObj.getString("bin")))) {
                            String binValue = "";
                            if ((batchSerialObj.has("binvalue") && !StringUtil.isNullOrEmpty(batchSerialObj.getString("binvalue")))) {
                                binValue = batchSerialObj.getString("binvalue");
                            } else {
                                binValue = batchSerialObj.getString("rack");
                                KwlReturnObject locresult = accountingHandlerDAOobj.getObject(StoreMaster.class.getName(), rowValue);
                                StoreMaster storeMasterObj = (StoreMaster) locresult.getEntityList().get(0);
                                if (storeMasterObj != null) {
                                    binValue = storeMasterObj.getName();
                                }
                            }
                            requestParams.put("tableName", "StoreMaster");
                            requestParams.put("fetchColumn", "id");
                            requestParams.put("condtionColumn", "name");
                            requestParams.put("condtionColumnvalue", binValue);
                            requestParams.put("companyColumn", "company.companyID");
                            requestParams.put(Constants.companyKey, companyid);

                            KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                            if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                                batchSerialObj.put("bin", result.getEntityList().get(0).toString());
                                batchSerialObj.remove("binvalue");

                            } else {
                                throw ServiceException.FAILURE("productValue does not exist", "erp31{" + "binvalue" + "}", false);
                            }
                        }
                    }
                    
                    /**
                     * code to fetch and put Batch ID using batch-name
                     */
                    requestParams.clear();
                    if (!StringUtil.isNullOrEmpty(batchSerialObj.optString("batchvalue"))) {
                        String batchValue = batchSerialObj.getString("batchvalue");
                        requestParams.put("tableName", "NewProductBatch");
                        requestParams.put("fetchColumn", "id");
                        requestParams.put("condtionColumn", "batchname");
                        requestParams.put("condtionColumnvalue", batchValue);
                        requestParams.put("companyColumn", "company.companyID");
                        requestParams.put(Constants.companyKey, companyid);

                        KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);
                        
                        if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                            batchSerialObj.put("purchasebatchid", result.getEntityList().get(0).toString());
                            batchSerialObj.put("batchname", batchValue);
                        }
                        batchSerialObj.put("batch", batchValue);
                        batchSerialObj.remove("batchvalue");
                    }

                    //Get serialnoid from serial name
                    requestParams.clear();
                    if ((batchSerialObj.has("serialnovalue") && !StringUtil.isNullOrEmpty(batchSerialObj.getString("serialnovalue")))) {
                        if (StringUtil.equal(paramJObj.optString(Constants.moduleid), String.valueOf(Constants.Acc_Goods_Receipt_ModuleId))) {
                            /**
                             * In case of GoodsReceipt, serial number is pushed in the database
                             * therefore, there is no need to look for serial in database
                             */
                            batchSerialObj.put("serialno", batchSerialObj.getString("serialnovalue"));
                            batchSerialObj.put("serialnoid", "");
                            batchSerialObj.remove("serialnovalue");
                        } else {
                            String serialnoValue = batchSerialObj.getString("serialnovalue");
                            requestParams.put("tableName", "NewBatchSerial");
                            requestParams.put("fetchColumn", "id");
                            requestParams.put("condtionColumn", "serialname");
                            requestParams.put("condtionColumnvalue", serialnoValue);
                            requestParams.put("companyColumn", "company.companyID");
                            requestParams.put(Constants.companyKey, companyid);

                            KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                            if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                                batchSerialObj.put("serialnoid", result.getEntityList().get(0).toString());
                                batchSerialObj.remove("serialnovalue");

                            } else {
                                throw ServiceException.FAILURE("serialnoValue does not exist", "erp31{" + "serialnovalue" + "}", false);
                            }
                        }
                    }

                    //Get purchasebatchid from  purchasebatchname
                    requestParams.clear();
                    if ((batchSerialObj.has("purchasebatchvalue") && !StringUtil.isNullOrEmpty(batchSerialObj.getString("purchasebatchvalue"))) || (batchSerialObj.has("purchasebatchid") && !StringUtil.isNullOrEmpty(batchSerialObj.getString("purchasebatchid")))) {
                        String purchasebatchValue = "";
                        if ((batchSerialObj.has("purchasebatchvalue") && !StringUtil.isNullOrEmpty(batchSerialObj.getString("purchasebatchvalue")))) {
                            purchasebatchValue = batchSerialObj.getString("purchasebatchvalue");
                        } else {
                            purchasebatchValue = batchSerialObj.getString("purchasebatchid");
                            KwlReturnObject locresult = accountingHandlerDAOobj.getObject(NewProductBatch.class.getName(), purchasebatchValue);
                            NewProductBatch newBatchObj = (NewProductBatch) locresult.getEntityList().get(0);
                            if (newBatchObj != null) {
                                purchasebatchValue = newBatchObj.getBatchname();
                            }
                        }

                        requestParams.put("tableName", "NewProductBatch");
                        requestParams.put("fetchColumn", "id");
                        requestParams.put("condtionColumn", "batchname");
                        requestParams.put("condtionColumnvalue", purchasebatchValue);
                        requestParams.put("companyColumn", "company.companyID");
                        requestParams.put(Constants.companyKey, companyid);

                        KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                        if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                            batchSerialObj.put("purchasebatchid", result.getEntityList().get(0).toString());
                            batchSerialObj.remove("purchasebatchvalue");

                        } else {
                            throw ServiceException.FAILURE("purchaseBatchValue does not exist", "erp31{" + "purchasebatchvalue" + "}", false);
                        }
                    }

                    //Get purchaseserialid from  purchaseserialname
                    requestParams.clear();
                    if ((batchSerialObj.has("purchaseserialvalue") && !StringUtil.isNullOrEmpty(batchSerialObj.getString("purchaseserialvalue"))) || (batchSerialObj.has("purchaseserialid") && !StringUtil.isNullOrEmpty(batchSerialObj.getString("purchaseserialid")))) {
                        String purchaseSerialValue = "";
                        if ((batchSerialObj.has("purchaseserialvalue") && !StringUtil.isNullOrEmpty(batchSerialObj.getString("purchaseserialvalue")))) {
                            purchaseSerialValue = batchSerialObj.getString("purchaseserialvalue");
                        } else {
                            purchaseSerialValue = batchSerialObj.getString("purchaseserialid");
                            KwlReturnObject locresult = accountingHandlerDAOobj.getObject(NewBatchSerial.class.getName(), purchaseSerialValue);
                            NewBatchSerial newBatchObj = (NewBatchSerial) locresult.getEntityList().get(0);
                            if (newBatchObj != null) {
                                purchaseSerialValue = newBatchObj.getSerialname();

                            }
                        }

                        requestParams.put("tableName", "NewBatchSerial");
                        requestParams.put("fetchColumn", "id");
                        requestParams.put("condtionColumn", "serialname");
                        requestParams.put("condtionColumnvalue", purchaseSerialValue);
                        requestParams.put("companyColumn", "company.companyID");
                        requestParams.put(Constants.companyKey, companyid);

                        KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                        if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                            batchSerialObj.put("purchaseserialid", result.getEntityList().get(0).toString());
                            batchSerialObj.put("serialnoid", result.getEntityList().get(0).toString());
                            batchSerialObj.remove("purchaseserialvalue");

                        } else {
                            throw ServiceException.FAILURE("purchaseSerialValue does not exist", "erp31{" + "purchaseserialvalue" + "}", false);
                        }
                    }

                    //Get packwarehouse from  packwarehouse Name
                    requestParams.clear();
                    if ((batchSerialObj.has("packwarehousevalue") && !StringUtil.isNullOrEmpty(batchSerialObj.getString("packwarehousevalue")))) {
                        String packWarehouseValue = batchSerialObj.getString("packwarehousevalue");
                        requestParams.put("tableName", "Store");
                        requestParams.put("fetchColumn", "id");
                        requestParams.put("condtionColumn", "abbreviation");
                        requestParams.put("condtionColumnvalue", packWarehouseValue);
                        requestParams.put("companyColumn", "company.companyID");
                        requestParams.put(Constants.companyKey, companyid);

                        KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                        if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                            batchSerialObj.put("packwarehouse", result.getEntityList().get(0).toString());
                            batchSerialObj.remove("packwarehousevalue");
                        }
                    }
                    
                    /**
                     * Manufacturing Date
                     * Used in Goods Receipt
                     */
                    String mfgdate = null;
                    if (batchSerialObj.has("mfgdate") && !StringUtil.isNullOrEmpty(batchSerialObj.optString("mfgdate"))) {
                        mfgdate = batchSerialObj.optString("mfgdate") == null ? null : batchSerialObj.optString("mfgdate");
                    }
                    if (!StringUtil.isNullOrEmpty(mfgdate)) {
                        Date sdate = authHandler.getUserDateFormatterWithoutTimeZone(paramJObj).parse(mfgdate);
                        DateFormat df = authHandler.getDateOnlyFormat();
                        mfgdate = df.format(sdate);
                        batchSerialObj.put("mfgdate", mfgdate);
                    }
                    
                    /**
                     * Expiry Date
                     * Used in Goods Receipt
                     */
                    String expirydate = null;
                    if (batchSerialObj.has("expirydate") && !StringUtil.isNullOrEmpty(batchSerialObj.optString("expirydate"))) {//For GoodsReceiptOrder
                        expirydate = batchSerialObj.optString("expirydate") == null ? null : batchSerialObj.optString("expirydate");
                        batchSerialObj.remove("expirydate");
                    }
                    if (!StringUtil.isNullOrEmpty(expirydate)) {
                        Date sdate = authHandler.getUserDateFormatterWithoutTimeZone(paramJObj).parse(expirydate);
                        DateFormat df = authHandler.getDateOnlyFormat();
                        expirydate = df.format(sdate);
                        batchSerialObj.put("expdate", expirydate);
                    }
                    
                    /**
                     * Warranty Start Date
                     */
                    String expstart = null;
                    if (batchSerialObj.has("expstart") && !StringUtil.isNullOrEmpty(batchSerialObj.optString("expstart"))) {
                        expstart = batchSerialObj.optString("expstart") == null ? null : batchSerialObj.optString("expstart");
                    } else if (batchSerialObj.has("warrantystart") && !StringUtil.isNullOrEmpty(batchSerialObj.optString("warrantystart"))) {//For GoodsReceiptOrder
                        expstart = batchSerialObj.optString("warrantystart") == null ? null : batchSerialObj.optString("warrantystart");
                        batchSerialObj.remove("warrantystart");
                    }
                    if (!StringUtil.isNullOrEmpty(expstart)) {  //ERP-9230 : //Used same date formatter which have used to save currency exchange
                        Date sdate = authHandler.getUserDateFormatterWithoutTimeZone(paramJObj).parse(expstart);
                        DateFormat df = authHandler.getDateOnlyFormat();
                        expstart = df.format(sdate);
                        batchSerialObj.put("expstart", expstart);
                    }

                    /**
                     * Warranty End Date
                     */
                    String expend = null;
                    if (batchSerialObj.has("expend") && !StringUtil.isNullOrEmpty(batchSerialObj.optString("expend"))) {
                        expend = batchSerialObj.optString("expend") == null ? null : batchSerialObj.optString("expend");
                    } else if (batchSerialObj.has("warrantyend") && !StringUtil.isNullOrEmpty(batchSerialObj.optString("warrantyend"))) {//For GoodsReceiptOrder
                        expend = batchSerialObj.optString("warrantyend") == null ? null : batchSerialObj.optString("warrantyend");
                        batchSerialObj.remove("warrantyend");
                    }
                    if (!StringUtil.isNullOrEmpty(expend)) {  //ERP-9230 : //Used same date formatter which have used to save currency exchange
                        Date sdate = authHandler.getUserDateFormatterWithoutTimeZone(paramJObj).parse(expend);
                        DateFormat df = authHandler.getDateOnlyFormat();
                        expend = df.format(sdate);
                        batchSerialObj.put("expend", expend);
                    }
                    
                    /**
                     * Code to process custom fields from batch-serial window
                     * Function 'createJSONForCustomField' fetched fieldId (UUID) using fieldlabel, and puts in the Json
                     */
                    String customField = batchSerialObj.optString(Constants.customfield, null);
                    if (!StringUtil.isNullOrEmpty(customField)) {
                        JSONArray customJArray = createJSONForCustomField(customField, companyid, Constants.SerialWindow_ModuleId);
                        batchSerialObj.put(Constants.customfield, customJArray);
                    }

                    batchSerialResultArr.put(batchSerialObj);
                }//end of batch serial detials
                if (batchSerialJson.has("isBatchForProduct")) {
                    batchSerialJson.remove("isBatchForProduct");
                }
                if (batchSerialJson.has("isSerialForProduct")) {
                    batchSerialJson.remove("isSerialForProduct");
                }
                batchSerialJson.put("batchdetails", batchSerialResultArr);
            }
        } catch (JSONException e) {
            throw ServiceException.FAILURE("Exception occurred while populating masters information", "erp23", false);
        }
        return batchSerialJson;
    }    
    
    @Override
    public JSONObject buildLineLevelTerms(String linelevel, JSONObject detailObj, boolean isReceivePayment) throws JSONException {
        JSONObject returnjObj = detailObj;
        double taxamount = 0;
        JSONArray linelevelTermsJSONArray = new JSONArray();

        try {
            if (!StringUtil.isNullOrEmpty(linelevel)) {
                JSONArray jcustomarray = new JSONArray(linelevel);
                for (int dim = 0; dim < jcustomarray.length(); dim++) {
                    JSONObject jSONObject = jcustomarray.optJSONObject(dim);
                    double rowamount = 0.0;
                    if (!isReceivePayment) {
                        double rate = detailObj.optDouble("rate", 0.0);
                        double quantity = detailObj.optDouble("quantity", 0.0);
                        if (detailObj.has("deliveredquantity") && detailObj.get("deliveredquantity") != null) {
                            quantity = detailObj.optDouble("deliveredquantity", 1.0);
                            detailObj.put("dquantity", detailObj.opt("deliveredquantity"));
                        }
                        double baseuomquantity = detailObj.optDouble("baseuomquantity", quantity);

                        rowamount = (rate * baseuomquantity);
                        double prdiscount = detailObj.optDouble("discount", 0);
                        int discountType = detailObj.optInt("discountType", 1);
                        double rowdiscountvalue = (discountType == 1) ? rowamount * prdiscount / 100 : prdiscount;
                        rowamount = rowamount - rowdiscountvalue;
                    } else {
                        rowamount = Double.parseDouble(detailObj.optString("enteramount", "0.0"));
                    }

                    jSONObject.put("assessablevalue", rowamount);
                    int taxtype = jSONObject.optInt("taxtype");
                    double termpercentage = jSONObject.optInt("termpercentage");
                    if (taxtype == 1) {
                        double termamount = rowamount * termpercentage / 100;
                        jSONObject.put("termamount", termamount);
                        taxamount += termamount;
                    } else if (taxtype == 0) {
                        double termamount = rowamount - termpercentage;
                        jSONObject.put("termamount", termamount);
                        taxamount += termamount;
                    }
                    linelevelTermsJSONArray.put(jSONObject);
                }
            }
        } catch (Exception ex) {
        } finally {
            returnjObj.put("LineTermdetails", linelevelTermsJSONArray.toString());
            returnjObj.put("recTermAmount", taxamount);
            returnjObj.put("taxamount", 0);
        }
        return returnjObj;
    }
    
    @Override
    public JSONObject replaceBooleanwithStringValues(JSONObject paramJobj) throws JSONException {
        JSONObject modifiedJson = paramJobj;
        // Replacing boolean values in String
        Iterator<String> keys = modifiedJson.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            if (modifiedJson.get(key) instanceof Boolean) {
                modifiedJson.put(key, String.valueOf(modifiedJson.optBoolean(key)));
            }
        }
        return modifiedJson;
    }
    
    @Override
    public JSONObject getSequenceFormatId(JSONObject paramJobj, String moduleid) throws JSONException, ServiceException {
        JSONObject modifiedJson = paramJobj;
        String modulename = "";
        int module = Integer.parseInt(moduleid);
        if (module == Constants.Acc_Invoice_ModuleId) {
            modulename = "";
        } else if (module == Constants.Acc_Credit_Note_ModuleId) {
            modulename = SequenceFormat.CREDIT_NOTE_MODULENAME;
        }else if (module == Constants.Acc_Purchase_Order_ModuleId) {
            modulename = SequenceFormat.PURCHASE_ORDER_MODULENAME;
        }else if(module == Constants.Acc_Goods_Receipt_ModuleId){
           modulename = SequenceFormat.GOODS_RECEIPT_ORDER_MODULENAME;
        }else if(module == Constants.Acc_Make_Payment_ModuleId){
           modulename = SequenceFormat.MAKE_PAYMENT_MODULENAME;
        }else if(module == Constants.Acc_Receive_Payment_ModuleId){
           modulename = SequenceFormat.RECEIVE_PAYMENT_MODULENAME;
        }else if(module == Constants.Acc_GENERAL_LEDGER_ModuleId){
           modulename = SequenceFormat.JOURNAL_ENTRY_MODULENAME;
        }

        if (!paramJobj.has(Constants.sequenceformat) || StringUtil.isNullOrEmpty(paramJobj.optString(Constants.sequenceformat, null))) {
            String sequenceformatid = null;
            Map<String, Object> sfrequestParams = new HashMap<String, Object>();
            sfrequestParams.put(Constants.companyKey, paramJobj.get(Constants.companyKey));
            sfrequestParams.put("modulename", modulename);
            sfrequestParams.put("isdefaultFormat", true);
            KwlReturnObject seqFormatResult = accCompanyPreferencesObj.getSequenceFormat(sfrequestParams);
            List<SequenceFormat> ll = seqFormatResult.getEntityList();
            if (ll.size() > 0) {
                SequenceFormat format = (SequenceFormat) ll.get(0);
                sequenceformatid = format.getID();
                modifiedJson.put(Constants.sequenceformat, sequenceformatid);
            }
        }//end of sequenceformat
        return modifiedJson;
    }

  @Override  
    public JSONObject manipulateAccountandInvoiceDetails(JSONObject paramJObj) throws ServiceException, ParseException, SessionExpiredException {
        JSONObject requestJSON = paramJObj;
        String companyid = requestJSON.optString(Constants.companyKey);
        Map<String, String> requestParams = new HashMap<>();
        JSONArray modifiedAccountArray = new JSONArray();
        JSONArray invoicedetails =new JSONArray(); 
        int cntype =CreditNote.CREDITNOTE_OTHERWISE;
        if (!StringUtil.isNullOrEmpty(requestJSON.optString(Constants.cntype, null))) {
            cntype = Integer.parseInt(requestJSON.optString(Constants.cntype));
        }
        int count=0;
        try {
            boolean gstIncluded = requestJSON.optBoolean("gstIncluded", requestJSON.optBoolean("includingGST", false));
            JSONArray accountdetails = requestJSON.optJSONArray("accountsdetail");
            
            if (accountdetails.length() > 0) {
                for (int i = 0; i < accountdetails.length(); i++) {
                    JSONObject jobj = accountdetails.getJSONObject(i);
                    count++;
                    jobj.put("dramount", jobj.opt("amount"));
                    jobj.put("debit", jobj.opt("isdebit"));
                    jobj.put("srNoForRow",count);
                    double rate = jobj.optDouble("amount", 0.0);
                    jobj.put("rateIncludingGst", rate);
                    
                    if (jobj.has("accountvalue") && !StringUtil.isNullOrEmpty(jobj.optString("accountvalue", null))) {
                        String accountvalue = jobj.optString("accountvalue");
                        requestParams.put("tableName", "Account");
                        requestParams.put("fetchColumn", "ID");
                        requestParams.put("condtionColumn", "acccode");
                        requestParams.put("condtionColumnvalue", accountvalue);
                        requestParams.put("companyColumn", "company.companyID");
                        requestParams.put(Constants.companyKey, companyid);

                        KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                        if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                            jobj.put("accountid", result.getEntityList().get(0).toString());
                            jobj.remove("accountvalue");

                        } else {
                            //if nulle then searching on the basis of name
                            requestParams.put("condtionColumn", "name");
                            result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                            if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                                jobj.put("accountid", result.getEntityList().get(0).toString());
                                jobj.remove("accountvalue");
                            } else {
                                throw ServiceException.FAILURE("Account Code does not exist", "erp31{" + "accountvalue" + "}", false);
                            }
                        }
                    }

                    requestParams.clear();
                    //Get Tax id from  tax name
                    if (jobj.has("taxvalue") && !StringUtil.isNullOrEmpty(jobj.getString("taxvalue"))) {
                        String taxid = jobj.getString("taxvalue");
                        requestParams.put("tableName", "Tax");
                        requestParams.put("fetchColumn", "ID");
                        requestParams.put("condtionColumn", "taxCode");
                        requestParams.put("condtionColumnvalue", taxid);
                        requestParams.put("companyColumn", "company.companyID");
                        requestParams.put(Constants.companyKey, companyid);
                        KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                        if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                            jobj.put("prtaxid", result.getEntityList().get(0).toString());

                            Map<String, Object> taxParams = new HashMap<String, Object>();;
                            taxParams.put("companyid", companyid);
                            taxParams.put("taxid", result.getEntityList().get(0).toString());

                            //To get tax percent of mapped tax
                            KwlReturnObject taxPresult = accTaxObj.getTax(taxParams);
                            List<Object[]> list = taxPresult.getEntityList();
                            double taxpercent = 0.0;
                            if (list != null && !list.isEmpty()) {
                                for (Object[] row : list) {
                                    taxpercent = (Double) row[1];
                                }
                            }
                            double taxamount = 0.0;
                            
                            if (gstIncluded) {
                                if (taxpercent != 0) {
                                    rate = (rate * 100) / (100 + taxpercent);
                                    taxamount = (taxpercent * rate) / 100;
                                    jobj.put("taxamount", taxamount);
                                    jobj.put("rateIncludingGst", rate);
                                } else {
                                    jobj.put("taxamount", 0);
                                }
                            } else {
                                if (taxpercent != 0) {
                                    taxamount = (taxpercent * rate) / 100;
                                    jobj.put("taxamount", taxamount);
                                } else {
                                    jobj.put("taxamount", 0);
                                }
                            }
                        }
                    }
                    
                    if (jobj.has("reasonvalue") && !StringUtil.isNullOrEmpty(jobj.optString("reasonvalue", null))) {
                        String reasonName = jobj.getString("reasonvalue");
                        requestParams.put("tableName", "MasterItem");
                        requestParams.put("fetchColumn", "ID");
                        requestParams.put("condtionColumn", "value");
                        requestParams.put("condtionColumnvalue", reasonName);
                        requestParams.put("companyColumn", "company.companyID");
                        requestParams.put(Constants.companyKey, companyid);

                        KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                        if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                            jobj.put("reason", result.getEntityList().get(0).toString());
                            jobj.remove("reasonvalue");
                        }
                    }
                    modifiedAccountArray.put(jobj);
                }
                requestJSON.put("details", modifiedAccountArray.toString());
            }
            
            if (cntype == CreditNote.CREDITNOTE_AGAINST_INVOICE && requestJSON.has("invoicedetails") && requestJSON.get("invoicedetails") != null) {
                invoicedetails = requestJSON.optJSONArray("invoicedetails");
                StringBuilder invoiceBuildids=new StringBuilder();
                StringBuilder invoiceamountsBuild=new StringBuilder();
                JSONArray invoiceJarray=new JSONArray();
                if (invoicedetails.length() > 0) {
                    for (int i = 0; i < invoicedetails.length(); i++) {
                        JSONObject jobj = invoicedetails.getJSONObject(i);
                        String invoicenumber = jobj.optString("invoiceno");
                        String invoiceid = accInvoiceDAOobj.getInvoiceId(companyid, invoicenumber);
                        KwlReturnObject soresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceid);
                        Invoice invoiceObj = (Invoice) soresult.getEntityList().get(0);
                        if (invoiceObj != null) {
                            jobj.put(Constants.billid,invoiceObj.getID());
                            invoiceBuildids.append(invoiceid + ",");
                            int discountType = jobj.optInt("discounttype", 0);
                            double enteramount = jobj.optDouble("enteramount", 0);
                            double amountdue = invoiceObj.getInvoiceamountdue();
                            jobj.put("amountdue", amountdue);
                            jobj.put("amountDueOriginal", amountdue);
                            double invoicerowamount = (discountType == 1) ? amountdue * enteramount / 100 : enteramount;
                            jobj.put("dramount", invoicerowamount);
                            jobj.put("typeOfFigure", (discountType == 1)?2:1);//for percentage=2,flat=1
                            jobj.put("typeFigure",enteramount);//enter amount
                            jobj.put("exchangeratefortransaction",requestJSON.opt("externalcurrencyrate") );//exchange rate for transaction
                            jobj.put("invamount", invoicerowamount);
                            invoiceamountsBuild.append(invoicerowamount + ",");
                            invoiceJarray.put(jobj);
                        }
                    }
                    requestJSON.put("invoicedetails", invoiceJarray.toString());
                    String invoiceids = invoiceBuildids.toString();
                    invoiceids = invoiceids.substring(0, (invoiceids.length() - 1));
                    requestJSON.put("invoiceids", invoiceids);
                    String invoiceamounts = invoiceamountsBuild.toString();
                    invoiceamounts = invoiceamounts.substring(0, (invoiceamounts.length() - 1));
                    requestJSON.put("amounts", invoiceamounts);
                }
            }
            
        } catch (JSONException e) {
            throw ServiceException.FAILURE("Exception occurred while populating masters information", "erp23", false);
        }
        return requestJSON;
    }    
  
   @Override  
   //iterate the detail and replace the values for payment and receipt module only.
   
    public JSONObject manipulatePaymentOrReceiptDetails(JSONObject paramJObj, int moduleid) throws ServiceException, ParseException, SessionExpiredException {
        JSONObject requestJSON = paramJObj;
        String companyid = requestJSON.optString(Constants.companyKey);
        Map<String, String> requestParams = new HashMap<>();
        JSONArray modifiedDetialArray = new JSONArray();
        DateFormat df = authHandler.getDateOnlyFormat();
        int count = 0;
        try {
            //this flag is used to differentiate between mp against customer & mp against vendor
            boolean isCustomerFlag= Boolean.parseBoolean(paramJObj.optString("iscustomer","false"));
            //custom field json create on the basis of fieldlabel and fieldvalue
            String customField = paramJObj.optString(Constants.customfield, null);
            if (!StringUtil.isNullOrEmpty(customField)) {
                JSONArray customJArray = createJSONForCustomField(customField, paramJObj.optString(Constants.companyKey), moduleid);
                customJArray = fieldDataManagercntrl.GetJsonArrayUsingFieldIds(customJArray,moduleid, companyid, true);
                paramJObj.put(Constants.customfield, customJArray);
            }
            
            JSONArray details = requestJSON.optJSONArray("details");
            double totalAmount = 0.0;
            if (details.length() > 0) {
                for (int i = 0; i < details.length(); i++) {
                    JSONObject jobj = details.getJSONObject(i);
                    count++;

                    double enteramount=jobj.optDouble("enteramount",0.0);
                    if (jobj.has("documenttype") && jobj.optInt("documenttype") != 0) {
                        jobj.put("type", jobj.optInt("documenttype", 0));
                    }
                    jobj.put(Constants.currencyKey, paramJObj.optString(Constants.currencyKey));//used in bank reconciliation for make payment
                    jobj.put("exchangeratefortransaction", requestJSON.opt("externalcurrencyrate"));//exchange rate for transaction
                    jobj.put("srNoForRow", count++);//exchange rate for transaction
                    jobj.put("currencyidtransaction", requestJSON.opt(Constants.currencyKey));//currencyid for transaction
                    if (moduleid == Constants.Acc_Make_Payment_ModuleId) {
                        jobj.put("debit", "true");
                    } else {
                        jobj.put("debit", "false");
                    }
//                    if (jobj.optInt("type",0) == Constants.AdvancePayment) {
//                        
//                    } else 
                    if (jobj.optInt("type", 0) == Constants.PaymentAgainstInvoice) {
                        String documentno = jobj.optString("documentno");

                        if (!StringUtil.isNullOrEmpty(documentno)) {
                            if ((moduleid == Constants.Acc_Receive_Payment_ModuleId && isCustomerFlag )||(isCustomerFlag && moduleid == Constants.Acc_Make_Payment_ModuleId)) {
                                String invoiceid = accInvoiceDAOobj.getInvoiceId(companyid, documentno);
                                KwlReturnObject soresult = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceid);
                                Invoice invoiceObj = (Invoice) soresult.getEntityList().get(0);
                                if (invoiceObj != null) {
                                    jobj.put("documentid", invoiceObj.getID());
                                    double amountdue = invoiceObj.getInvoiceamountdue();
                                    jobj.put("amountdue", amountdue);
                                    jobj.put("amountDueOriginal", amountdue);
                                    if (moduleid == Constants.Acc_Receive_Payment_ModuleId) {
                                        jobj.put("invoicecreationdate", invoiceObj.getJournalEntry() != null ? df.format(invoiceObj.getJournalEntry().getEntryDate()) : df.format(invoiceObj.getCreationDate()));
                                    }
                                    
                                }else{
                                   throw ServiceException.FAILURE("Invalid invoice number", "", false);
                                }
                            }
                            if ((moduleid == Constants.Acc_Make_Payment_ModuleId && !isCustomerFlag)||(moduleid == Constants.Acc_Receive_Payment_ModuleId && !isCustomerFlag )) {
                                KwlReturnObject returnObj = accGoodsReceiptDAO.getGoodsReceiptCount(documentno, companyid);
                                GoodsReceipt grObj = (GoodsReceipt) returnObj.getEntityList().get(0);
                                if (grObj != null) {
                                    jobj.put("documentid", grObj.getID());
                                    double amountdue = grObj.getInvoiceamountdue();
                                    jobj.put("amountdue", amountdue);
                                    jobj.put("amountDueOriginal", grObj.getInvoiceAmount());
                                }else{
                                   throw ServiceException.FAILURE("Invalid goodsreceipt/vendor invoice number", "", false);
                                }
                            }
                        }

                    } else if (jobj.optInt("type", 0) == Constants.PaymentAgainstCNDN) {
                        String documentno = jobj.optString("documentno");

                        if (!StringUtil.isNullOrEmpty(documentno)) {
                            if (moduleid == Constants.Acc_Make_Payment_ModuleId) {
                                KwlReturnObject doResult = accCreditNoteDAOobj.getCNFromNoteNo(documentno, companyid);
                                CreditNote cnObj = (CreditNote) doResult.getEntityList().get(0);
                                if (cnObj != null) {
                                    jobj.put("documentid", cnObj.getID());
                                    jobj.put("documentno", cnObj.getCreditNoteNumber());
                                    double amountdue = cnObj.getCnamountdue();
                                    jobj.put("amountdue", amountdue);
                                    jobj.put("amountDueOriginal", cnObj.getCnamount());
                                } else {
                                    throw ServiceException.FAILURE("Invalid credit note number", "", false);
                                }
                            }
                            if (moduleid == Constants.Acc_Receive_Payment_ModuleId) {
                                KwlReturnObject dnResult = accDebitNoteobj.getDNFromNoteNo(documentno, companyid);
                                DebitNote dnObj = (DebitNote) dnResult.getEntityList().get(0);
                                if (dnObj != null) {
                                    jobj.put("documentid", dnObj.getID());
                                    jobj.put("documentno", dnObj.getDebitNoteNumber());
                                    double amountdue = dnObj.getDnamountdue();
                                    jobj.put("amountdue", amountdue);
                                    jobj.put("amountDueOriginal", dnObj.getDnamount());
                                     if (moduleid == Constants.Acc_Receive_Payment_ModuleId) {
                                        jobj.put("date", df.format(dnObj.getCreationDate()));
                                    }
                                } else {
                                    throw ServiceException.FAILURE("Invalid credit note number", "", false);
                                }

                            }
                            
                        }
                    } else if (jobj.optInt("type", 0) == Constants.GLPayment) {
                        if (jobj.has("documentno") && !StringUtil.isNullOrEmpty(jobj.optString("documentno", null))) {
                            String accountvalue = jobj.optString("documentno");
                            jobj.put("documentno", accountvalue);
                            requestParams.put("tableName", "Account");
                            requestParams.put("fetchColumn", "ID");
                            requestParams.put("condtionColumn", "acccode");
                            requestParams.put("condtionColumnvalue", accountvalue);
                            requestParams.put("companyColumn", "company.companyID");
                            requestParams.put(Constants.companyKey, companyid);

                            KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                            if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                                jobj.put("documentid", result.getEntityList().get(0).toString());

                            } else {
                                //if null then searching on the basis of name
                                requestParams.put("condtionColumn", "name");
                                result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                                if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                                    jobj.put("documentid", result.getEntityList().get(0).toString());
                                } else {
                                    throw ServiceException.FAILURE("Account does not exist", "erp31{" + "documentno" + "}", false);
                                }
                            }
                        }
                       if (jobj.has("taxvalue") && !StringUtil.isNullOrEmpty(jobj.getString("taxvalue"))) {
                        String taxid = jobj.getString("taxvalue");
                        requestParams.put("tableName", "Tax");
                        requestParams.put("fetchColumn", "ID");
                        requestParams.put("condtionColumn", "taxCode");
                        requestParams.put("condtionColumnvalue", taxid);
                        requestParams.put("companyColumn", "company.companyID");
                        requestParams.put(Constants.companyKey, companyid);
                        KwlReturnObject result = accountingHandlerDAOobj.populateMasterInformation(requestParams);

                            if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                                jobj.put("prtaxid", result.getEntityList().get(0).toString());

                                Map<String, Object> taxParams = new HashMap<String, Object>();;
                                taxParams.put("companyid", companyid);
                                taxParams.put("taxid", result.getEntityList().get(0).toString());

                                //To get tax percent of mapped tax
                                KwlReturnObject taxPresult = accTaxObj.getTax(taxParams);
                                List<Object[]> list = taxPresult.getEntityList();
                                double taxpercent = 0.0;
                                if (list != null && !list.isEmpty()) {
                                    for (Object[] row : list) {
                                        taxpercent = (Double) row[1];
                                    }
                                }
                                double taxamount = 0.0;

                                if (taxpercent != 0) {
                                    taxamount = (taxpercent * jobj.optDouble("enteramount",0.0)) / 100;
                                    jobj.put("taxamount", taxamount);
                                    totalAmount +=taxamount;
//                                    enteramount=enteramount-taxamount;
                                } else {
                                    jobj.put("taxamount", 0);
                                }
                                
                            }
                        }
                    }
                    //Line level custom fields
                    String lineLevelCustomFieldDetails = jobj.optString(Constants.customfield, null);
                    //building json for Custom field on the basis of name and value
                    if (!StringUtil.isNullOrEmpty(lineLevelCustomFieldDetails)) {
                        JSONArray customJArray = createJSONForCustomField(lineLevelCustomFieldDetails, paramJObj.optString(Constants.companyKey), moduleid);
                        customJArray = fieldDataManagercntrl.GetJsonArrayUsingFieldIds(customJArray,moduleid, companyid, false);
                        jobj.put(Constants.customfield, customJArray);
                    }
                    
                    totalAmount += enteramount;
                    modifiedDetialArray.put(jobj);
                }
            }
            requestJSON.put("Details", modifiedDetialArray.toString());
            requestJSON.put("amount", totalAmount);

        } catch (JSONException e) {
            throw ServiceException.FAILURE("Exception occurred while populating masters information", "erp23", false);
        }
        return requestJSON;
    }
   
    //ERP-41214:Show asterisk to unit price and amount 
    //Handled for mobile Apps
    @Override
    public JSONObject getUserPermissionsforUnitPriceAndAmount(JSONObject paramJObj) throws ServiceException{
        JSONObject requestJSON = paramJObj;
        try {
            //User Permission for display Unit Price and Amount
            boolean displayUnitPriceAndAmountInSalesDocument = true;
            boolean displayUnitPriceAndAmountInPurchaseDocument = true;
            long unitPriceAndAmountSalesPermission = 0;
            long unitPriceAndAmountPurchasePermission = 0;
            long unitPriceAndAmountPermission = 0;

            HashMap<String, Object> params = new HashMap<String, Object>();
            params.put("userid", requestJSON.optString(Constants.useridKey));
            JSONObject userPermJobj = accountingHandlerDAOobj.getUserPermissionForFeature(params);
            if (userPermJobj.has("Perm") && userPermJobj.getJSONObject("Perm") != null && userPermJobj.getJSONObject("Perm").length() > 0) {
                JSONObject permJobj = userPermJobj.getJSONObject("Perm");
                if (permJobj.has("unitpriceandamount") && permJobj.getJSONObject("unitpriceandamount") != null && permJobj.getJSONObject("unitpriceandamount").length() > 0) {
                    unitPriceAndAmountSalesPermission = permJobj.getJSONObject("unitpriceandamount").optLong("displayunitpriceandamountinsalesdocument", 0);
                    unitPriceAndAmountPurchasePermission = permJobj.getJSONObject("unitpriceandamount").optLong("displayunitpriceandamountinpurchasedocument", 0);
                }
            }
            if (userPermJobj.has("UPerm") && userPermJobj.getJSONObject("UPerm") != null && userPermJobj.getJSONObject("UPerm").length() > 0) {
                unitPriceAndAmountPermission = userPermJobj.getJSONObject("UPerm").getLong("unitpriceandamount");
            }

            if ((unitPriceAndAmountPermission & unitPriceAndAmountSalesPermission) == unitPriceAndAmountSalesPermission) {
                displayUnitPriceAndAmountInSalesDocument = false;
            }
            if ((unitPriceAndAmountPermission & unitPriceAndAmountPurchasePermission) == unitPriceAndAmountPurchasePermission) {
                displayUnitPriceAndAmountInPurchaseDocument = false;
            }
            requestJSON.put(Constants.displayUnitPriceAndAmountInSalesDocument, !displayUnitPriceAndAmountInSalesDocument);
            requestJSON.put(Constants.displayUnitPriceAndAmountInPurchaseDocument, !displayUnitPriceAndAmountInPurchaseDocument);
        } catch (JSONException e) {
            throw ServiceException.FAILURE("Exception occurred while populating masters information", "erp23", false);
        }
        return requestJSON;
    }
   
     @Override
    public JSONObject buildAdvanceSearchJson(JSONObject paramJObj) throws ServiceException, JSONException {

        JSONObject requestJSON = new JSONObject(paramJObj.toString());
        String companyid = requestJSON.optString(Constants.companyKey);
        JSONObject rootAdvanceSearchJobj = new JSONObject();
        JSONArray advJArray = new JSONArray();
        String moduleid = requestJSON.optString(Constants.moduleid);
        String fieldLabels = requestJSON.optString("advancesearchfieldname");
        String[] fieldLabelArr = fieldLabels.split(",");
        String advancesearchfieldvalue = requestJSON.optString("advancesearchfieldvalue");
        String[] recarr = advancesearchfieldvalue.split(",");

        String searchtext = "";
        String combosearch = "";
        
        //Multiple fieldlabels 
         for (String fieldLabel : fieldLabelArr) {
             //Fetching fieldid on the basis of fieldlabel
             if (!StringUtil.isNullOrEmpty(fieldLabel) && !StringUtil.isNullOrEmpty(advancesearchfieldvalue) && !StringUtil.isNullOrEmpty(moduleid)) {
                 
                 //Not testing for Store customfield
                 if (!fieldLabel.equalsIgnoreCase("Store")) {
                     HashMap<String, Object> invFieldParamsMap = new HashMap<>();

                     //Fetching fieldid on the basis on the basis of Fieldlabel
                     invFieldParamsMap.put(Constants.filter_names, Arrays.asList(Constants.fieldlabel, Constants.companyid, Constants.moduleid));
                     invFieldParamsMap.put(Constants.filter_values, Arrays.asList(fieldLabel, companyid, Integer.parseInt(moduleid)));
                     KwlReturnObject fieldIdparam = accAccountDAOobj.getFieldParamsIds(invFieldParamsMap);
                     String fieldId = "";
                     //FieldParams
                     if (fieldIdparam.getEntityList() != null && !fieldIdparam.getEntityList().isEmpty() && fieldIdparam.getEntityList().get(0) != null) {
                         fieldId = (String) fieldIdparam.getEntityList().get(0);
                         if (!StringUtil.isNullOrEmpty(fieldId)) {
                             paramJObj.put(Constants.isdefaultHeaderMap, true);
                             KwlReturnObject fieldParams = accountingHandlerDAOobj.getObject(FieldParams.class.getName(), fieldId);
                             FieldParams fieldParamsObj = (FieldParams) fieldParams.getEntityList().get(0);

                             if (recarr.length > 0) {
                                 StringBuilder searchTextString = new StringBuilder();
                                 StringBuilder combosearchString = new StringBuilder();
                                 for (String searchFieldValue : recarr) {
                                     searchtext = searchFieldValue;
                                     combosearch = searchFieldValue;
                                     
                                     //if searchfield value is any store then do not create for json for it.Below code is handled.
                                     KwlReturnObject storeRet = accountingHandlerDAOobj.getObject(Store.class.getName(), searchFieldValue);
                                     Store storeObj = (Store) storeRet.getEntityList().get(0);

                                     if (storeObj != null) {
                                         continue;
                                     }
                                     /*
                                      * Field Type->4=dropdown & Field
                                      * Type->7=multiselect dropdown
                                      */
                                     if (fieldParamsObj.getFieldtype() == Constants.SINGLESELECTCOMBO || fieldParamsObj.getFieldtype() == Constants.MULTISELECTCOMBO) {
                                         fieldId = fieldParamsObj.getId();

                                         //Fetching masterfieldIds on the basis on the basis of fieldValues
                                         String fcdid = fieldManagerDAOobj.getIdsUsingParamsValue(fieldId, searchFieldValue);     // get ids for module using values and field id  
                                         if (!StringUtil.isNullOrEmpty(fcdid)) {
                                             KwlReturnObject fieldCmbData = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), fcdid);
                                             FieldComboData fieldComboData = (FieldComboData) fieldCmbData.getEntityList().get(0);

                                             searchTextString.append(fieldComboData.getId() + ",");
                                             combosearchString.append(fieldComboData.getValue() + ",");
                                         }
                                     }
                                 } //end for (String searchFieldValue : recarr) {

                                 //Only in case of combo field
                                 if (searchTextString.length() > 0) {
                                     searchtext = searchTextString.toString();
                                     searchtext = searchtext.substring(0, searchtext.length() - 1);
                                 }

                                 if (combosearch.length() > 0) {
                                     combosearch = combosearchString.toString();
                                     combosearch = combosearch.substring(0, combosearch.length() - 1);
                                 }
                                     JSONObject cntObj = new JSONObject();
                                     cntObj.put("searchText", searchtext);
                                     cntObj.put("search", searchtext);
                                     cntObj.put("combosearch", combosearch);
                                     cntObj.put("column", fieldParamsObj.getId());
                                     cntObj.put("refdbname", "Col" + fieldParamsObj.getColnum());
                                     cntObj.put("xfield", "Col" + fieldParamsObj.getColnum());
                                     cntObj.put(Constants.iscustomcolumn, true);
                                     cntObj.put("iscustomcolumndata", fieldParamsObj.getCustomcolumn() == 1 ? true : false);
                                     cntObj.put("iscustomfield", fieldParamsObj.getCustomfield() == 1 ? true : false);
                                     cntObj.put("isfrmpmproduct", false);
                                     cntObj.put("fieldtype", fieldParamsObj.getFieldtype());
                                     cntObj.put("columnheader", fieldParamsObj.getFieldlabel());

                                     if (fieldParamsObj.getFieldtype() == Constants.SINGLESELECTCOMBO || fieldParamsObj.getFieldtype() == Constants.MULTISELECTCOMBO) {
                                         cntObj.put(Constants.xtype, "select");
                                     }
                                     if ((fieldParamsObj.getFieldtype() == Constants.TEXTFIELD || fieldParamsObj.getFieldtype() == Constants.NUMBERFIELD)) {
                                         cntObj.put(Constants.xtype, "textfield");
                                     }
                                     cntObj.put("isinterval", false);
                                     cntObj.put("interval", false);
                                     cntObj.put("isbefore", "");
                                     cntObj.put("isdefaultfield", false);
                                     cntObj.put(Constants.moduleid, fieldParamsObj.getModuleid());
                                     advJArray.put(cntObj);
                             }//end of if (recarr.length > 0) {
                         }//end of if (!StringUtil.isNullOrEmpty(fieldId))
                     }//end of if (fieldIdparam.getEntityList() != null && !fieldIdparam.getEntityList().isEmpty() && fieldIdparam.getEntityList().get(0) != null) 
                 }
                 
//                 if (advJArray.length() == 0) {//IF field is not custom field- For Store 
                     JSONObject advJobj = new JSONObject();
                     HashMap<String, Object> map = new HashMap<String, Object>();
                     map.put(Constants.filter_names, Arrays.asList("module", "allowAdvanceSearch", "defaultHeader"));
                     map.put(Constants.filter_values, Arrays.asList(moduleid, true, fieldLabel));
                     map.put("order_by", Arrays.asList("defaultHeader"));
                     map.put("order_type", Arrays.asList("asc"));
                     KwlReturnObject headerResult = fieldManagerDAOobj.getDefaultHeaders(map);
                     List<DefaultHeader> headersList = headerResult.getEntityList();

                     if (headersList.size() == 1) {
                         for (DefaultHeader header : headersList) {
                             advJobj.put("column", header.getId());
                         }
                     }
                     if (!StringUtil.isNullOrEmpty(advJobj.optString("column"))) {
                         advJobj.put("isdefaultfield", true);
                         advJobj.put("isfrmpmproduct", false);
                         advJobj.put("iscustomcolumn", true);
                         advJobj.put("moduleid", moduleid);
                         advJobj.put("fieldtype", 4);//Combo
                         advJobj.put("searchText", advancesearchfieldvalue);//storeid
                         advJobj.put("search", advancesearchfieldvalue);//storeid
                         advJobj.put("columnheader", fieldLabel);
                         advJobj.put("iscustomfield", false);
                         advJArray.put(advJobj);
                     }
//                 }//end of if (recarr.length > 0)
             }//end if (!StringUtil.isNullOrEmpty(fieldLabel) && !StringUtil.isNullOrEmpty(advancesearchfieldvalue)) 
         }

        rootAdvanceSearchJobj.put("root", advJArray);
        requestJSON.put(Constants.Acc_Search_Json, rootAdvanceSearchJobj);
        String filterConjuctionCriteria = paramJObj.optString(Constants.Filter_Criteria, "AND");
        requestJSON.put(Constants.Filter_Criteria, filterConjuctionCriteria);
        
        return requestJSON;
    }
     
   @Override  
    public void checkUserActivePeriodRange(String companyid, String userid, Date transactionDate,int moduleid) throws ServiceException {
        try {
            if (transactionDate != null) {
                //Checking Active Days Case where it doesn't allow to edit if the document date doesn't come in active days .
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("userID", userid);
                requestParams.put("companyID", companyid);
                requestParams.put("moduleID", moduleid);
                CompanyPreferencesCMN.checkUserActivePeriodRange(accCompanyPreferencesObj, requestParams, transactionDate);
            }

        } catch (AccountingException | ParseException | SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), "", false);
        }
    }
     
}
