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
package com.krawler.spring.accounting.companypreferances;

import com.krawler.hql.accounting.SequenceFormat;
import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.hql.accounting.*;
import com.krawler.hql.accounting.companypreferenceservice.AccCompanyPreferencesService;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.currency.accCurrencyController;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.esp.utils.ConfigReader;

/**
 *
 * @author krawler
 */
public class accCompanyPreferencesController extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private String successView;
    private auditTrailDAO auditTrailObj;
    private MessageSource messageSource;
    private exportMPXDAOImpl exportDaoObj;
    private accAccountDAO accAccountDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private AccCompanyPreferencesService accCompanyPreferencesService;
    private APICallHandlerService apiCallHandlerService;
    
    public void setAccCompanyPreferencesService(AccCompanyPreferencesService accCompanyPreferencesService) {
        this.accCompanyPreferencesService = accCompanyPreferencesService;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {     //VP
        this.apiCallHandlerService = apiCallHandlerService;
    }
//    public ModelAndView saveCompanyAccountPreferences(HttpServletRequest request, HttpServletResponse response) {
//        JSONObject jobj=new JSONObject();
//        String msg="";
//        boolean issuccess = false;
//
//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setName("CAP_Tx");
//        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
//
//        TransactionStatus status = txnManager.getTransaction(def);
//        try {
//            saveCompanyAccountPreferences(request);
//            issuccess = true;
//            msg = messageSource.getMessage("acc.cp.save", null, RequestContextUtils.getLocale(request));   //"Account Preferences have been saved successfully";
//            txnManager.commit(status);
//        } catch (SessionExpiredException ex) {
//            txnManager.rollback(status);
//            msg = ex.getMessage();
//            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
//        } catch (Exception ex) {
//            txnManager.rollback(status);
//            msg = ""+ex.getMessage();
//            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
//        } finally{
//            try {
//                jobj.put("success", issuccess);
//                jobj.put("msg", msg);
//            } catch (JSONException ex) {
//                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return new ModelAndView("jsonView", "model", jobj.toString());
//    }
//
//    public void saveCompanyAccountPreferences(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
//        try {
//            HashMap<String, Object> prefMap = new HashMap<String, Object>();           
//            prefMap.put("fyfrom", authHandler.getDateFormatter(request).parse(request.getParameter("fyfrom")));
//            prefMap.put("bbfrom", authHandler.getDateFormatter(request).parse(request.getParameter("bbfrom")));
//            prefMap.put("firstfyfrom", authHandler.getDateFormatter(request).parse(request.getParameter("fyfrom")));
//            prefMap.put("withoutinventory", !StringUtil.isNullOrEmpty(request.getParameter("withoutinventory")));
//            prefMap.put("withinvupdate", !StringUtil.isNullOrEmpty(request.getParameter("withinvupdate")));
//            prefMap.put("editTransaction", !StringUtil.isNullOrEmpty(request.getParameter("editTransaction")));
//            prefMap.put("editLinkedTransaction", !StringUtil.isNullOrEmpty(request.getParameter("editLinkedTransaction")));
//            prefMap.put("shipDateConfiguration", !StringUtil.isNullOrEmpty(request.getParameter("shipDateConfiguration")));
//            prefMap.put("unitPriceConfiguration", !StringUtil.isNullOrEmpty(request.getParameter("unitPriceConfiguration")));
//            prefMap.put("deleteTransaction", !StringUtil.isNullOrEmpty(request.getParameter("deleteTransaction")));
//            prefMap.put("DOSettings",Boolean.FALSE.parseBoolean(String.valueOf(request.getParameter("DOSettings"))));
//            prefMap.put("GRSettings",Boolean.FALSE.parseBoolean(String.valueOf(request.getParameter("GRSettings"))));
//            prefMap.put("updateInvLevelCheck", !StringUtil.isNullOrEmpty(request.getParameter("updateInvLevelCheck")));
//            prefMap.put("isQaApprovalFlow", !StringUtil.isNullOrEmpty(request.getParameter("isQaApprovalFlow")));
//            prefMap.put("editso", !StringUtil.isNullOrEmpty(request.getParameter("editso")));
//            prefMap.put("memo", !StringUtil.isNullOrEmpty(request.getParameter("memo")));
//            prefMap.put("showprodserial", !StringUtil.isNullOrEmpty(request.getParameter("showprodserial")));
//            if(!StringUtil.isNullOrEmpty(request.getParameter("withouttax1099")))
//               prefMap.put("withouttax1099", request.getParameter("withouttax1099"));
//            prefMap.put("emailinvoice", !StringUtil.isNullOrEmpty(request.getParameter("emailinvoice")));
//            prefMap.put("companyid", sessionHandlerImpl.getCompanyid(request));
//
//            prefMap.put("discountgiven", request.getParameter("discountgiven"));
//            prefMap.put("discountreceived", request.getParameter("discountreceived"));
//            prefMap.put("shippingcharges", request.getParameter("shippingcharges"));
//            prefMap.put("othercharges", request.getParameter("othercharges"));
//            prefMap.put("cashaccount", request.getParameter("cashaccount"));
//            prefMap.put("foreignexchange", request.getParameter("foreignexchange"));
//            prefMap.put("unrealisedgainloss", request.getParameter("unrealisedgainloss"));
//            prefMap.put("depreciationaccount", request.getParameter("depreciationaccount"));
//         
//            prefMap.put("gstnumber", request.getParameter("gstnumber"));
//            prefMap.put("companyuen", request.getParameter("companyuen"));
//            prefMap.put("iafversion", request.getParameter("iafversion"));
//            prefMap.put("taxNumber", request.getParameter("taxNumber"));
//            prefMap.put("expenseaccount", request.getParameter("expenseaccount"));
//            prefMap.put("customerdefaultacc", request.getParameter("customerdefaultacc"));
//            prefMap.put("vendordefaultacc", request.getParameter("vendordefaultacc"));
//            prefMap.put("liabilityaccount", request.getParameter("liabilityaccount"));
//            prefMap.put("negativestock", request.getParameter("negativestock"));
//            prefMap.put("custcreditcontrol", request.getParameter("custcreditlimit"));
//            prefMap.put("partNumber", !StringUtil.isNullOrEmpty(request.getParameter("partNumber")));
//            prefMap.put("showLeadingZero", request.getParameter("showleadingzero"));
//            prefMap.put("accountWithOrWithoutCode", request.getParameter("accountWithOrWithoutCode"));
//            prefMap.put("custbudgetcontrol", request.getParameter("custMinBudget"));
//            prefMap.put("billaddress",request.getParameter("billaddress")==null?"":request.getParameter("billaddress"));
//            prefMap.put("shipaddress",request.getParameter("shipaddress")==null?"":request.getParameter("shipaddress"));
//            prefMap.put("approvalMail", !StringUtil.isNullOrEmpty(request.getParameter("approvalMail")));
//            prefMap.put("sendmailto", request.getParameter("sendmailto"));
//            prefMap.put("isDeferredRevenueRecognition", !StringUtil.isNullOrEmpty(request.getParameter("isDeferredRevenueRecognition"))?Boolean.parseBoolean(request.getParameter("isDeferredRevenueRecognition")):false);
//            prefMap.put("salesAccount", request.getParameter("salesAccount"));
//            prefMap.put("salesRevenueRecognitionAccount", request.getParameter("salesRevenueRecognitionAccount"));
//            prefMap.put("showAllAccount",!StringUtil.isNullOrEmpty(request.getParameter("showAllAccount")));
//            prefMap.put("stockValuationFlag",!StringUtil.isNullOrEmpty(request.getParameter("stockValuationFlag")));
//            prefMap.put("noOfDaysforValidTillField",!StringUtil.isNullOrEmpty(request.getParameter("noOfDaysforValidTillField"))?request.getParameter("noOfDaysforValidTillField"):0);
//            prefMap.put("recurringDeferredRevenueRecognition",!StringUtil.isNullOrEmpty(request.getParameter("recurringDeferredRevenueRecognition"))?Boolean.parseBoolean(request.getParameter("recurringDeferredRevenueRecognition")):false);
//            
//            CompanyAccountPreferences preferences;
//            Map<String, Object> requestParams = new HashMap<String, Object>();
//            requestParams.put("id", sessionHandlerImpl.getCompanyid(request));
//            KwlReturnObject result = accCompanyPreferencesObj.getCompanyPreferences(requestParams);
//            preferences = (CompanyAccountPreferences) result.getEntityList().get(0);
//            if (preferences == null) {
//                preferences = new CompanyAccountPreferences();
//                result = accCompanyPreferencesObj.addPreferences(prefMap);
//            } else { 
//                prefMap.put("id", sessionHandlerImpl.getCompanyid(request));
//                result = accCompanyPreferencesObj.updatePreferences(prefMap);
//            }
//            preferences = (CompanyAccountPreferences) result.getEntityList().get(0);
//            ExtraCompanyPreferences extraCompanyPreferences=null;
//            Map<String, Object> requestParamsExtra = new HashMap<String, Object>();
//            requestParamsExtra.put("id", sessionHandlerImpl.getCompanyid(request));
//            KwlReturnObject resultExtra = accCompanyPreferencesObj.getExtraCompanyPreferences(requestParamsExtra);
//            if(!resultExtra.getEntityList().isEmpty()){
//                extraCompanyPreferences = (ExtraCompanyPreferences) resultExtra.getEntityList().get(0);
//            }
//            if (extraCompanyPreferences == null) {
//                extraCompanyPreferences = new ExtraCompanyPreferences();
//            } else {
//                prefMap.put("id", sessionHandlerImpl.getCompanyid(request));
//            }
//            resultExtra = accCompanyPreferencesObj.addOrUpdateExtraPreferences(prefMap);
//            extraCompanyPreferences = (ExtraCompanyPreferences) resultExtra.getEntityList().get(0);
//            saveYearLock(request);
//            auditTrailObj.insertAuditLog(AuditAction.COMPANY_ACCOUNT_PREFERENCES_UPDATE, "User "+ sessionHandlerImpl.getUserFullName(request) + " from "+preferences.getCompany().getCompanyName()+" changed company's account preferences", request, preferences.getID());
//        } catch (ParseException ex) {
//            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
//            throw ServiceException.FAILURE("saveCompanyAccountPreferences : "+ex.getMessage(), ex);
//        } catch (SessionExpiredException ex) {
//            throw ServiceException.FAILURE("saveCompanyAccountPreferences : "+ex.getMessage(), ex);
//        }
//    }

    private class accountComp implements Comparator<Account> {
        private boolean sortOnType;
        private boolean directionDesc;

        private accountComp(boolean sortOnType1, boolean direction1) {
            sortOnType = sortOnType1;
            directionDesc = direction1;
        }

        @Override
        public int compare(Account o1, Account o2) {
            String o1_Code = (o1.getAcccode() == null) ? "" : o1.getAcccode();
            String o2_Code = (o2.getAcccode() == null) ? "" : o2.getAcccode();

            if (sortOnType) {
                if (StringUtil.equal(o1.getGroup().getName(), o2.getGroup().getName())) {
                    if (StringUtil.equal(o1_Code, o2_Code)) {
                        return directionDesc ? o2.getName().compareTo(o1.getName()) : o1.getName().compareTo(o2.getName());
                    } else {
                        return directionDesc ? o2_Code.compareTo(o1_Code) : o1_Code.compareTo(o2_Code);
                    }
                } else {
                    return directionDesc ? o2.getGroup().getName().compareTo(o1.getGroup().getName()) : o1.getGroup().getName().compareTo(o2.getGroup().getName());
                }
            } else {
                if (StringUtil.equal(o1_Code, o2_Code)) {
                    return directionDesc ? o2.getName().compareTo(o1.getName()) : o1.getName().compareTo(o2.getName());
                } else {
                    return directionDesc ? o2_Code.compareTo(o1_Code) : o1_Code.compareTo(o2_Code);
                }
            }
        }
    }

    public ModelAndView saveWIPCPAccountsPreferences(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CAP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            saveWIPCPAccountsPreferences(request);
            issuccess = true;
            msg = "WIP/CP Account Preferences have been saved successfully";
            txnManager.commit(status);
        } catch (Exception ex) {
            issuccess = false;
            txnManager.rollback(status);
            msg = "Error While saving WIP/CP Account Preferences.";
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void saveWIPCPAccountsPreferences(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
        try {
            HashMap<String, Object> prefMap = new HashMap<String, Object>();
            String wipAccountPrefix = request.getParameter("wipAccountPrefix");
            String cpAccountPrefix = request.getParameter("cpAccountPrefix");
            String wipAccountType = request.getParameter("wipAccountType");
            String cpAccountType = request.getParameter("cpAccountType");
            prefMap.put("wipAccountPrefix", wipAccountPrefix);
            prefMap.put("cpAccountPrefix", cpAccountPrefix);
            prefMap.put("wipAccountType", wipAccountType);
            prefMap.put("cpAccountType", cpAccountType);
            prefMap.put("company", sessionHandlerImpl.getCompanyid(request));
//            prefMap.put("id", sessionHandlerImpl.getCompanyid(request));

            ExtraCompanyPreferences accountsData = null;
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("id", sessionHandlerImpl.getCompanyid(request));

            KwlReturnObject result = accCompanyPreferencesObj.getExtraCompanyPreferences(requestParams);
            if (result.getEntityList().size() > 0) {
                accountsData = (ExtraCompanyPreferences) result.getEntityList().get(0);
            }
            if (accountsData == null) {
                ExtraCompanyPreferences accounts = accCompanyPreferencesObj.saveExtraCompanyPreferences(prefMap);
            } else {
                prefMap.put("id", sessionHandlerImpl.getCompanyid(request));
                ExtraCompanyPreferences accounts = accCompanyPreferencesObj.updateExtraCompanyPreferences(prefMap);
            }
            auditTrailObj.insertAuditLog(AuditAction.COMPANY_ACCOUNT_PREFERENCES_UPDATE, "User " + sessionHandlerImpl.getUserFullName(request) + " has save WIP/CP Account Preferences", request, "0");
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("saveCompanyAccountPreferences : " + ex.getMessage(), ex);
        }
    }

    public ModelAndView getWIPCPAccountsPreferences(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("id", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject result = accCompanyPreferencesObj.getExtraCompanyPreferences(requestParams);
            ExtraCompanyPreferences accountsData = (ExtraCompanyPreferences) result.getEntityList().get(0);

            JSONObject prefJobj = new JSONObject();
            prefJobj.put("wipAccountPrefix", accountsData.getWipAccountPrefix());
            prefJobj.put("cpAccountPrefix", accountsData.getCpAccountPrefix());
            prefJobj.put("wipAccountType", accountsData.getWipAccountTypeId());
            prefJobj.put("cpAccountType", accountsData.getCpAccountTypeId());

            jobj.put("data", prefJobj);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView ExportAccountsPreferencesSettings(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            String colHeader = "";
            String fileType = request.getParameter("filetype");
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("id", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject result = accCompanyPreferencesObj.getCompanyPreferences(requestParams);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) result.getEntityList().get(0);

            KwlReturnObject resultExtraCmpPre = accCompanyPreferencesObj.getExtraCompanyPreferences(requestParams);
            ExtraCompanyPreferences extraCompanyPreferences = null;
            if (!resultExtraCmpPre.getEntityList().isEmpty()) {
                extraCompanyPreferences = (ExtraCompanyPreferences) resultExtraCmpPre.getEntityList().get(0);
            }
            JSONArray DataJArr = getCompanyPreferencesJson(request, pref, extraCompanyPreferences);
            jobj.put("data", DataJArr);
            exportDaoObj.processRequest(request, response, jobj);
            jobj.put("success", true);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getCompanyPreferencesJson(HttpServletRequest request, CompanyAccountPreferences pref, ExtraCompanyPreferences extraCompanyPreferences) throws JSONException, ServiceException, ClassNotFoundException {
        JSONArray jArr = new JSONArray();
        try {

            KwlReturnObject result = accCompanyPreferencesObj.getCompanyPreferencesFieldForExport();
            List list = result.getEntityList();
            Iterator ite = list.iterator();
            boolean ReturnValue;
            Class noparams[] = {};
            //JSONObject accountPrefSet = new JSONObject(request.getParameter("companyprefset"));
            ModelAndView model = getCompanyAccountPreferences(request, null);
            JSONObject accountPrefSet = new JSONObject(model.getModel().get("model").toString()).getJSONObject("data");
            DateFormat formatter = new SimpleDateFormat("MMM dd, yyyy hh:mm:ss"); //Date format is getting like this in json
            DateFormat formatter1 = new SimpleDateFormat("yyyy-MM-dd"); //default date format
            boolean isRevenueRecognitionManualJE = false;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject company = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company companyList = (Company) company.getEntityList().get(0);
            while (ite.hasNext()) {
                ExportCompanypref expocomppref = (ExportCompanypref) ite.next();
                JSONObject obj = new JSONObject();
                obj.put("Setting_Name", (expocomppref.getDisplayname() != null) ? expocomppref.getDisplayname().replaceAll("\\n", " ").trim() : "");
                obj.put("Customer", "N/A");
                obj.put("Account_Code", "N/A");
                Object Returnobj = accountPrefSet.opt(expocomppref.getDataindex().trim());
                String returnObjinString = String.valueOf(Returnobj);
                if (expocomppref.getDisplayname().equals("Default Mail Sender Settings ")) {
                    if (returnObjinString.equals("1")) {
                        obj.put("Setting_Name", "Default Mail Sender Settings-User Email");
                    } else {
                        obj.put("Setting_Name", "Default Mail Sender Settings-Company Email");
                    }
                }
                if (expocomppref.getDisplayname().equals("Revenue Recognition Account-Revenue recognition on manual JE") && returnObjinString.equals("true")) {
                    isRevenueRecognitionManualJE = true;
                }
                if (expocomppref.getDisplayname().equals("Revenue Recognition Account-Sales Account") && isRevenueRecognitionManualJE) {
                    obj.put("Setting_Name", "Revenue Recognition Account-Advance Sales Account");
                }
                if (expocomppref.getDisplayname().equals("UOM Settings-")) {
                    if (returnObjinString.equals("0")) {
                        obj.put("Setting_Name", "UOM Settings-UOM Schema");
                    } else {
                        obj.put("Setting_Name", "UOM Settings-Packaging UOM");
                    }
                }
                if (expocomppref.getDisplayname().equals("Product Selection setting- ")) {
                    if (returnObjinString.equals("0")) {
                        obj.put("Setting_Name", "Product Selection setting-Show all Products");
                    } else if (returnObjinString.equals("1")) {
                        obj.put("Setting_Name", "Product Selection setting-Show Products on type ahead");
                    } else {
                        obj.put("Setting_Name", "Product Selection setting-Product Id as free text");
                    }
                }
                if (expocomppref.getDisplayname().equals("Accounts With Code")) {
                    if (returnObjinString.equals("true")) {
                        obj.put("Setting_Name", expocomppref.getDisplayname() + "-With Code");
                    } else {
                        obj.put("Setting_Name", expocomppref.getDisplayname() + "-Without Code");
                    }
                }

                if (expocomppref.getValidatetype().equalsIgnoreCase("boolean")) {
                    ReturnValue = Boolean.FALSE.parseBoolean(returnObjinString);
                    obj.put("Setting_Value", ReturnValue ? "Yes" : "No");
                }
                if (expocomppref.getValidatetype().equalsIgnoreCase("int")) {
                    if (expocomppref.getId().equalsIgnoreCase("18")) {
                        obj.put("Setting_Value", "0".equalsIgnoreCase(returnObjinString) == true ? "Flow Diagram View" : "Widget View");
                    } else {
                        obj.put("Setting_Value", "0".equalsIgnoreCase(returnObjinString) == true ? "Ignore" : "1".equalsIgnoreCase(returnObjinString) == true ? "Block" : "Warn");
                    }
                }
                if (expocomppref.getValidatetype().equalsIgnoreCase("int")) {
                    obj.put("Setting_Value", Integer.parseInt(returnObjinString));
                }
                if (expocomppref.getValidatetype().equalsIgnoreCase("string")) {
                    obj.put("Setting_Value", "null".equals(String.valueOf(Returnobj)) ? "" : String.valueOf(Returnobj));
                }
                if (expocomppref.getValidatetype().equalsIgnoreCase("date")) {
                    String sdate = formatter1.format(new SimpleDateFormat("MMMM d, yyyy").parse(returnObjinString));
                    //String sdate = formatter.format(date);
                    obj.put("Setting_Value", "".equalsIgnoreCase(returnObjinString) ? "" : sdate);
                }
                if (expocomppref.getValidatetype().equalsIgnoreCase("ref")) {
                    KwlReturnObject accresult = accountingHandlerDAOobj.getObject(expocomppref.getFullclassname(), String.valueOf(Returnobj));
                    if (accresult.getEntityList().get(0) != null) {
                        Object classobj = accresult.getEntityList().get(0);
                        Class cls = Class.forName(expocomppref.getFullclassname());
                        Method method = cls.getDeclaredMethod(expocomppref.getExportclassgetter(), noparams);
                        Returnobj = method.invoke(classobj, null);
                        obj.put("Setting_Value", String.valueOf(Returnobj));
                    } else {
                        obj.put("Setting_Value", "");
                    }
                }
                if (expocomppref.getDisplayname().equals("Consignment Stock Settings-Default Customer Warehouse ")) {
                    if (!StringUtil.isNullOrEmpty(returnObjinString)) {
                        KwlReturnObject inventorytresult = accountingHandlerDAOobj.getObject(InventoryWarehouse.class.getName(), returnObjinString);
                        if (inventorytresult.getEntityList().get(0) != null) {
                            InventoryWarehouse invresult = (InventoryWarehouse) inventorytresult.getEntityList().get(0);
                            obj.put("Setting_Value", invresult.getName());
                        }
                    } else {
                        obj.put("Setting_Value", "");
                    }
                }
                if (expocomppref.getDisplayname().equals("Consignment Stock Settings-Store for Stock Repair ") || expocomppref.getDisplayname().equals("Consignment Stock Settings-Store for QA Inspection ")) {
                    if (!StringUtil.isNullOrEmpty(returnObjinString)) {
                        KwlReturnObject storeList = accountingHandlerDAOobj.getObject(Store.class.getName(), returnObjinString);
                        Store storeresult = (Store) storeList.getEntityList().get(0);
                        obj.put("Setting_Value", storeresult.getAbbreviation() + "-" + storeresult.getDescription());
                    }
                }
                if (expocomppref.getDisplayname().equals("Generate Barcode Setting-Print Price Type ")) {
                    if (returnObjinString.equals("90")) {
                        obj.put("Setting_Value", "Downward");
                    } else {
                        obj.put("Setting_Value", "Upward");
                    }

                }
                if (expocomppref.getDisplayname().equals("Generate Barcode Setting-Barcode Type ")) {
                    if (returnObjinString.equals("CODE128")) {
                        obj.put("Setting_Value", "Code 128");
                    } else if (returnObjinString.equals("CODE39")) {
                        obj.put("Setting_Value", "Code 39");
                    } else {
                        obj.put("Setting_Value", "DataMatrix");
                    }

                }
                if (expocomppref.getDisplayname().equals("Activate budgeting based on Department-Budgeting frequency type ")) {
                    if (returnObjinString.equals("0")) {
                        obj.put("Setting_Name", expocomppref.getDisplayname() + "(Monthly)");
                        //obj.put("Setting_Value", "0");
                    } else if (returnObjinString.equals("1")) {
                        obj.put("Setting_Name", expocomppref.getDisplayname() + "(Bi-Monthly)");
                        //obj.put("Setting_Value", "1");
                    } else if (returnObjinString.equals("2")) {
                        obj.put("Setting_Name", expocomppref.getDisplayname() + "(Quarterly)");
                        //obj.put("Setting_Value", "2");
                    } else if (returnObjinString.equals("3")) {
                        obj.put("Setting_Name", expocomppref.getDisplayname() + "(Half Yearly)");
                        //obj.put("Setting_Value", "3");
                    } else {
                        obj.put("Setting_Name", expocomppref.getDisplayname() + "(Yearly)");
                        //obj.put("Setting_Value", "4");
                    }

                }
                if (expocomppref.getDisplayname().equals("Activate budgeting based on Department-Budgeting based on ")) {
                    if (returnObjinString.equals("0")) {
                        obj.put("Setting_Name", expocomppref.getDisplayname() + "Department");
                    } else {
                        obj.put("Setting_Name", expocomppref.getDisplayname() + "Department and Product");
                    }
                }
                if (expocomppref.getDisplayname().equals("Control Account Settings-Select Account For Profit Loss ") || expocomppref.getDisplayname().equals("Control Account Settings-Select Account For Opening Stock ") || expocomppref.getDisplayname().equals("Control Account Settings-Select Account For Closing Stock ") || expocomppref.getDisplayname().equals("Control Account Settings-Select Account For Stock in Hand ")) {
                    if (!StringUtil.isNullOrEmpty(returnObjinString)) {
                        KwlReturnObject accountresult = accountingHandlerDAOobj.getObject(Account.class.getName(), returnObjinString);
                        Account accresult = (Account) accountresult.getEntityList().get(0);
                        if (accresult != null) {
                            obj.put("Setting_Value", accresult.getName());
                            obj.put("Account_Code", !StringUtil.isNullOrEmpty(accresult.getAcccode()) ? accresult.getAcccode() : "N/A");
                        } else {
                            obj.put("Setting_Value", "");
                            obj.put("Account_Code", accresult.getAcccode());
                        }
                    } else {
                        obj.put("Setting_Value", "");
                    }
                }
                if (expocomppref.getDisplayname().equals("Asset settings ")) {
                    if (returnObjinString.equals("0")) {
                        obj.put("Setting_Name", expocomppref.getDisplayname() + "-Beginning of the Year of Aquisition (Yearwise)");
                    } else if (returnObjinString.equals("1")) {
                        obj.put("Setting_Name", expocomppref.getDisplayname() + "-Beginning of the Date of Aquisition (Monthwise)");
                    } else {
                        obj.put("Setting_Name", expocomppref.getDisplayname() + "-Beginning of the Month of Aquisition (Monthwise)");
                    }
                }
                if (expocomppref.getDisplayname().equals("Product Selection setting")) {
                    if (returnObjinString.equals("0")) {
                        obj.put("Setting_Name", expocomppref.getDisplayname() + "-Show all Products");
                    } else if (returnObjinString.equals("1")) {
                        obj.put("Setting_Name", expocomppref.getDisplayname() + "-Show Products on type ahead");
                    } else {
                        obj.put("Setting_Name", expocomppref.getDisplayname() + "-Product Id as free text");
                    }
                }
                if (expocomppref.getDisplayname().equals("Minimum Budget Settings") || expocomppref.getDisplayname().equals("Negative Stock Settings") || expocomppref.getDisplayname().equals("Customer Credit Control")) {
                    if (returnObjinString.equals("0")) {
                        obj.put("Setting_Name", expocomppref.getDisplayname() + "-Ignore");
                    } else if (returnObjinString.equals("1")) {
                        obj.put("Setting_Name", expocomppref.getDisplayname() + "-Block");
                    } else {
                        obj.put("Setting_Name", expocomppref.getDisplayname() + "-Warn");
                    }
                }
                if (expocomppref.getDisplayname().equals("Integration Settings-POS Walk-in Customer")) {
                    if (!(returnObjinString.equals("") || returnObjinString.equalsIgnoreCase("null"))) {
                        List returnList = new ArrayList();
                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
                        requestParams.put("companyid", companyid);
                        requestParams.put("customerID", returnObjinString);
                        returnList = accCompanyPreferencesObj.getCustomerNameByID(requestParams);
                        Object[] obj1 = (Object[]) returnList.get(0);
                        String customerCode = obj1[1].toString();
                        String customerName = obj1[0].toString();
                        obj.put("Setting_Value", customerName);
                        obj.put("Customer", customerCode);
                    } else {
                        obj.put("Setting_Value", "");
                        obj.put("Customer", "N/A");
                    }
                }
                if (expocomppref.getDisplayname().equals("Default Purchase Type Selection Setting")) {
                    if (returnObjinString.equals("true")) {
                        obj.put("Setting_Name", expocomppref.getDisplayname() + "-Cash Purchase");
                    } else {
                        obj.put("Setting_Name", expocomppref.getDisplayname() + "-Credit Purchase");
                    }
                }
                if (expocomppref.getDisplayname().equals("Default Sales Type Selection Setting")) {
                    if (returnObjinString.equals("true")) {
                        obj.put("Setting_Name", expocomppref.getDisplayname() + "-Cash Sales");
                    } else {
                        obj.put("Setting_Name", expocomppref.getDisplayname() + "-Credit Sales");
                    }
                }

                if (companyList.getCountry().getID().equals("137")) {
                    if (expocomppref.getDisplayname().equals("Company GST Detail-GST Number")) {
                        obj.put("Setting_Name", "Company GST Detail-Trade Register Number (GST)");
                    } else if (expocomppref.getDisplayname().equals("Company GST Detail-Company UEN")) {
                        obj.put("Setting_Name", "Company GST Detail-Company BRN");
                    } else if (expocomppref.getDisplayname().equals("Company GST Detaile-IAF File Version")) {
                        obj.put("Setting_Name", "Company GST Detail-GAF File Version");
                    }
                }
                jArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getProductsJson : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("ClassNotFoundException : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    public ModelAndView saveCompanyAddressDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("addressDetail", request.getParameter("addressDetail"));
            jobj = accCompanyPreferencesService.saveCompanyAddressDetails(requestParams);
        } catch (Exception ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getCompanyAddressDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            if (!StringUtil.isNullOrEmpty(request.getParameter("isBillingAddress"))) {
                boolean isBillingAddress = Boolean.parseBoolean((String) request.getParameter("isBillingAddress"));
                requestParams.put("isBillingAddress", isBillingAddress);
            }
            jobj = accCompanyPreferencesService.getCompanyAddressDetails(requestParams);
        } catch (Exception ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView ExportSequenceFormat(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            String colHeader = "";
            String fileType = request.getParameter("filetype");
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject result = accCompanyPreferencesObj.getSequenceFormat(requestParams);
            KwlReturnObject result1 = accCompanyPreferencesObj.getChequeSequenceFormatList(requestParams);
            JSONArray DataJArr = getSequenceFormatJsonForExport(request, result.getEntityList(), result1.getEntityList());
            jobj.put("data", DataJArr);
            exportDaoObj.processRequest(request, response, jobj);
            jobj.put("success", true);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getSequenceFormatJsonForExport(HttpServletRequest request, List list, List list1) throws JSONException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            Iterator ite = list.iterator();
            Iterator ite1 = list1.iterator();

            while (ite.hasNext()) {
                SequenceFormat seqfor = (SequenceFormat) ite.next();
                JSONObject obj = new JSONObject();
                String moduleName = getModuleName(seqfor.getModuleid());
                if (!moduleName.equals("")) {
                    obj.put("name", seqfor.getName());
                    obj.put("prefix", seqfor.getPrefix());
                    obj.put("suffix", seqfor.getSuffix());
                    obj.put("numberofdigit", seqfor.getNumberofdigit());
                    obj.put("startfrom", seqfor.getStartfrom());
                    obj.put("showleadingzero", seqfor.isShowleadingzero() ? "Yes" : "No");
                    obj.put("showdateinprefix", seqfor.isDateBeforePrefix() ? "Yes" : "No");
                    obj.put("modulename", getModuleName(seqfor.getModuleid()));
                    obj.put("isdefaultformat", seqfor.isIsdefaultformat() ? "Yes" : "No");
                    obj.put("selecteddateformat", seqfor.getDateformatinprefix());
                    obj.put("accountcode", "");
                    obj.put("bankname", "");
                    jArr.put(obj);
                }
            }
            while (ite1.hasNext()) {
                ChequeSequenceFormat seqfor = (ChequeSequenceFormat) ite1.next();
                JSONObject obj1 = new JSONObject();
                String name = "";
                obj1.put("accountcode", seqfor.getBankAccount().getAcccode());
                obj1.put("bankname", seqfor.getBankAccount().getName());
                obj1.put("prefix", "");
                obj1.put("suffix", "");
                obj1.put("numberofdigit", seqfor.getNumberOfDigits());
                int noOfDigit = seqfor.getNumberOfDigits();
                for (int i = 0; i < noOfDigit; i++) {
                    name += "0";
                }
                obj1.put("name", name);
                obj1.put("startfrom", seqfor.getStartFrom());
                obj1.put("showleadingzero", seqfor.isShowLeadingZero() ? "Yes" : "No");
                obj1.put("showdateinprefix", "");
                obj1.put("modulename", "Cheque Number");
                obj1.put("isdefaultformat", "");
                obj1.put("selecteddateformat", "");
                jArr.put(obj1);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getProductsJson : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("ClassNotFoundException : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    private String getModuleName(int moduleid) {
        String moduleName = "";
        switch (moduleid) {
            case (Constants.Acc_Invoice_ModuleId):
                moduleName = "Invoice/Cash Sales";
                break;
            case (Constants.Acc_GENERAL_LEDGER_ModuleId):
                moduleName = "Journal Entry";
                break;
            case (Constants.Acc_BillingInvoice_ModuleId):
                moduleName = "Billing Invoice";
                break;
            case (Constants.Acc_Cash_Sales_ModuleId):
                moduleName = "Cash Sales";
                break;
            case (Constants.Acc_Cash_Purchase_ModuleId):
                moduleName = "Cash Purchase";
                break;
            case (Constants.Acc_Billing_Cash_Sales_ModuleId):
                moduleName = "Billing Cash Sales";
                break;
            case (Constants.Acc_Vendor_Invoice_ModuleId):
                moduleName = "Purchase Invoice/Cash Purchase";
                break;
            case (Constants.Acc_Debit_Note_ModuleId):
                moduleName = "Debit Note";
                break;
            case (Constants.Acc_Credit_Note_ModuleId):
                moduleName = "Credit Note";
                break;
            case (Constants.Acc_Make_Payment_ModuleId):
                moduleName = "Make Payment";
                break;
            case (Constants.Acc_Receive_Payment_ModuleId):
                moduleName = "Receive Payment";
                break;
            case (Constants.Acc_Product_Master_ModuleId):
                moduleName = "Products & Services";
                break;
            case (Constants.Acc_Sales_Order_ModuleId):
                moduleName = "Sales Order";
                break;
            case (Constants.Acc_Purchase_Order_ModuleId):
                moduleName = "Purchase Order";
                break;
            case (Constants.Acc_Customer_Quotation_ModuleId):
                moduleName = "Customer Quotation";
                break;
            case (Constants.Acc_Vendor_Quotation_ModuleId):
                moduleName = "Vendor Quotation";
                break;
            case (Constants.Acc_Customer_ModuleId):
                moduleName = "Customer";
                break;
            case (Constants.Acc_Vendor_ModuleId):
                moduleName = "Vendor";
                break;
            case (Constants.Acc_Delivery_Order_ModuleId):
                moduleName = "Delivery Order";
                break;
            case (Constants.Acc_Goods_Receipt_ModuleId):
                moduleName = "Goods Receipt Order";
                break;
            case (Constants.Acc_Purchase_Return_ModuleId):
                moduleName = "Purchase Return";
                break;
            case (Constants.Acc_Sales_Return_ModuleId):
                moduleName = "Sales Return";
                break;
            case (Constants.Account_Statement_ModuleId):
                moduleName = "GL Accounts";
                break;
            case (Constants.Acc_RFQ_ModuleId):
                moduleName = "Request For Quotation";
                break;
            case (Constants.Acc_Purchase_Requisition_ModuleId):
                moduleName = "Purchase Requisition";
                break;
            case (Constants.Acc_Contract_Order_ModuleId):
                moduleName = "Contract Order";
                break;

        }
        return moduleName;
    }

    public ModelAndView ExportHideShow(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            String colHeader = "";
            String fileType = request.getParameter("filetype");
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put(Constants.filter_names, Arrays.asList(Constants.company, "hidden"));
            requestParams.put(Constants.filter_values, Arrays.asList(companyid, true));

            KwlReturnObject result = accAccountDAOobj.getCustomizeReportMapping(requestParams);
            JSONArray DataJArr = getExportHideShowJson(request, result.getEntityList());
            jobj.put("data", DataJArr);
            exportDaoObj.processRequest(request, response, jobj);
            jobj.put("success", true);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getExportHideShowJson(HttpServletRequest request, List list) throws JSONException, ServiceException {
        JSONArray jArr = new JSONArray();
        KwlReturnObject resultMapping = null;
        KwlReturnObject result = null;
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            boolean isFormField = true;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            requestParams.put(Constants.filter_names, Arrays.asList(Constants.company, "formField"));
            requestParams.put(Constants.filter_values, Arrays.asList(companyid, isFormField));

            resultMapping = accAccountDAOobj.getCustomizeReportMapping(requestParams);
            List<CustomizeReportMapping> defaultlstMapping = resultMapping.getEntityList();
            HashMap<String, String> customizeReportMappingMap = new HashMap<String, String>();
            HashSet<String> customizeReportMappingSetforReadonly = new HashSet<String>();
            HashSet<String> customizeReportMappingSetforUserMandatory = new HashSet<String>();
            //HashSet<String> customizeReportMappingSetforId =  new HashSet<String>();
            for (CustomizeReportMapping customizeReportMapping : defaultlstMapping) {

                if (customizeReportMapping.isHidden()&& customizeReportMapping.getUser()!=null) {
                    customizeReportMappingMap.put(customizeReportMapping.getDataIndex() + customizeReportMapping.getReportId() + customizeReportMapping.getModuleId(), customizeReportMapping.getUser().getFirstName() + " " + customizeReportMapping.getUser().getLastName());
                }
                if (customizeReportMapping.isReadOnlyField()) {
                    customizeReportMappingSetforReadonly.add(customizeReportMapping.getDataIndex());
                }
                if (customizeReportMapping.isUserManadatoryField()) {
                    customizeReportMappingSetforUserMandatory.add(customizeReportMapping.getDataIndex());
                }
                //}   
            }
            requestParams.clear();
            requestParams.put("isFormField", true);
            result = accAccountDAOobj.getCustomizeReportHeader(requestParams);
            List<CustomizeReportHeader> defaultlst = result.getEntityList();

            for (CustomizeReportHeader customizeReport : defaultlst) {
                JSONObject obj = new JSONObject();
                obj.put("header", customizeReport.getDataHeader());
                obj.put("modulename", getModuleName(customizeReport.getModuleId()));
                if (customizeReportMappingMap.containsKey(customizeReport.getDataIndex() + customizeReport.getReportId() + customizeReport.getModuleId())) {
                    obj.put("user", customizeReportMappingMap.get(customizeReport.getDataIndex() + customizeReport.getReportId() + customizeReport.getModuleId()));
                    obj.put("ishidden", "Yes");
                } else {
                    obj.put("ishidden", "No");
                }
                obj.put("isformfield", customizeReport.isFormField() ? "Yes" : "No");
                obj.put("ismanadatory", customizeReport.isManadatoryField() ? "Yes" : "No");
                if (customizeReportMappingSetforReadonly.contains(customizeReport.getDataIndex())) {
                    obj.put("readonly", "Yes");
                } else {
                    obj.put("readonly", "No");
                }
                if (customizeReportMappingSetforUserMandatory.contains(customizeReport.getDataIndex())) {
                    obj.put("isusermandatory", "Yes");
                } else {
                    obj.put("isusermandatory", "No");
                }
                jArr.put(obj);

            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getProductsJson : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("ClassNotFoundException : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    private void getTransactionFormFieldHideShowStatus(String companyid, JSONObject prefJobj) {
        // Putting data for hide show form fields of Customer invoice form.

        try {

            // fetching data for customer invoice module

            JSONArray crmCIArray = new JSONArray();

            int moduleId = Constants.Acc_Invoice_ModuleId;

            KwlReturnObject crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            List<CustomizeReportMapping> crmList = crmResult.getEntityList();


            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("dataHeader", crm.getDataHeader());
                crmCIArray.put(crmObj);
            }

            prefJobj.put("customerInvoice", crmCIArray);

            JSONArray crmProductArray = new JSONArray();
            moduleId = Constants.Acc_Product_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();


            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("isReportField", crm.isReportField());
                crmObj.put("dataHeader", crm.getDataHeader());
                crmProductArray.put(crmObj);
            }

            prefJobj.put("productForm", crmProductArray);


            // fetching data for vendor invoice module

            JSONArray crmVIArray = new JSONArray();

            moduleId = Constants.Acc_Vendor_Invoice_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();


            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("dataHeader", crm.getDataHeader());
                crmVIArray.put(crmObj);
            }

            prefJobj.put("vendorInvoice", crmVIArray);

            // fetching data for Cash Purchase module
            JSONArray crmCPArray = new JSONArray();

            moduleId = Constants.Acc_Cash_Purchase_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();

            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("dataHeader", crm.getDataHeader());
                crmCPArray.put(crmObj);
            }

            prefJobj.put("CP", crmCPArray);

            // fetching data for Cash Sales module

            JSONArray crmCSArray = new JSONArray();

            moduleId = Constants.Acc_Cash_Sales_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();


            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("dataHeader", crm.getDataHeader());
                crmCSArray.put(crmObj);
            }

            prefJobj.put("CS", crmCSArray);


            // fetching data for Purchase Order module

            JSONArray crmPOArray = new JSONArray();

            moduleId = Constants.Acc_Purchase_Order_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();


            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("dataHeader", crm.getDataHeader());
                crmPOArray.put(crmObj);
            }

            prefJobj.put("purchaseOrder", crmPOArray);


            // fetching data for Purchase Order module

            JSONArray crmSOArray = new JSONArray();

            moduleId = Constants.Acc_Sales_Order_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();


            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("dataHeader", crm.getDataHeader());
                crmSOArray.put(crmObj);
            }

            prefJobj.put("salesOrder", crmSOArray);


            // fetching data for Purchase Order module

            JSONArray crmVQArray = new JSONArray();

            moduleId = Constants.Acc_Vendor_Quotation_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();


            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("dataHeader", crm.getDataHeader());
                crmVQArray.put(crmObj);
            }

            prefJobj.put("vendorQuotation", crmVQArray);

            // fetching data for Purchase Order module

            JSONArray crmCQArray = new JSONArray();

            moduleId = Constants.Acc_Customer_Quotation_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();


            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("dataHeader", crm.getDataHeader());
                crmCQArray.put(crmObj);
            }

            prefJobj.put("customerQuotation", crmCQArray);

            // fetching data for Purchase Return module

            JSONArray crmPRArray = new JSONArray();

            moduleId = Constants.Acc_Purchase_Return_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();


            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("dataHeader", crm.getDataHeader());
                crmPRArray.put(crmObj);
            }

            prefJobj.put("purchaseReturn", crmPRArray);

            // fetching data for Sales Return module

            JSONArray crmSRArray = new JSONArray();

            moduleId = Constants.Acc_Sales_Return_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();


            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("dataHeader", crm.getDataHeader());
                crmSRArray.put(crmObj);
            }

            prefJobj.put("salesReturn", crmSRArray);

            // fetching data for Goods Receipt module

            JSONArray crmGRArray = new JSONArray();

            moduleId = Constants.Acc_Goods_Receipt_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();


            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("dataHeader", crm.getDataHeader());
                crmGRArray.put(crmObj);
            }

            prefJobj.put("goodsReceipt", crmGRArray);

            // fetching data for Delivery Order module

            JSONArray crmDOArray = new JSONArray();

            moduleId = Constants.Acc_Delivery_Order_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();


            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("dataHeader", crm.getDataHeader());
                crmDOArray.put(crmObj);
            }

            prefJobj.put("deliveryOrder", crmDOArray);

            // fetching data for Stock Request module

            JSONArray crmStockRequsetArray = new JSONArray();
            moduleId = Constants.Acc_Stock_Request_ModuleId;
            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);
            crmList = crmResult.getEntityList();

            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("dataHeader", crm.getDataHeader());
                crmStockRequsetArray.put(crmObj);
            }

            prefJobj.put("stockRequest", crmStockRequsetArray);

            // fetching data for Sales Contract module

            JSONArray crmSalesContractArray = new JSONArray();
            moduleId = Constants.Acc_Contract_Order_ModuleId;
            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);
            crmList = crmResult.getEntityList();

            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("isReportField", crm.isReportField());
                crmObj.put("dataHeader", crm.getDataHeader());
                crmSalesContractArray.put(crmObj);
            }

            prefJobj.put("salesContract", crmSalesContractArray);

            // fetching data for Lease Contract module

            JSONArray crmLeaseContractArray = new JSONArray();
            moduleId = Constants.Acc_Lease_Contract;
            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);
            crmList = crmResult.getEntityList();

            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("isReportField", crm.isReportField());
                crmObj.put("dataHeader", crm.getDataHeader());
                crmLeaseContractArray.put(crmObj);
            }

            prefJobj.put("leaseContract", crmLeaseContractArray);

             // fetching data for Make Payment module

            JSONArray makePaymentArray = new JSONArray();

            moduleId = Constants.Acc_Make_Payment_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();


            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("isReportField", crm.isReportField());
                crmObj.put("dataHeader", crm.getDataHeader());
                makePaymentArray.put(crmObj);
            }

            prefJobj.put("makePayment", makePaymentArray);

            /*
             * fetching data for Receive Payment module
             */
            JSONArray receivePaymentArray = new JSONArray();

            moduleId = Constants.Acc_Receive_Payment_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();

            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("isReportField", crm.isReportField());
                crmObj.put("dataHeader", crm.getDataHeader());
                receivePaymentArray.put(crmObj);
            }

            prefJobj.put("receivePayment", receivePaymentArray);

            
            /*
             * fetching data for Credit Note module
             */
            JSONArray creditNoteArray = new JSONArray();

            moduleId = Constants.Acc_Credit_Note_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();

            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("isReportField", crm.isReportField());
                crmObj.put("dataHeader", crm.getDataHeader());
                creditNoteArray.put(crmObj);
            }

            prefJobj.put("creditNote", creditNoteArray);
            /*
             * fetching data for Debit Note module
             */
            JSONArray debitNoteArray = new JSONArray();

            moduleId = Constants.Acc_Debit_Note_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();

            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("isReportField", crm.isReportField());
                crmObj.put("dataHeader", crm.getDataHeader());
                debitNoteArray.put(crmObj);
            }

            prefJobj.put("debitNote", debitNoteArray);

            /*
             * fetching data for Customer module
             */
            JSONArray customerArray = new JSONArray();

            moduleId = Constants.Acc_Customer_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();

            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("isReportField", crm.isReportField());
                crmObj.put("dataHeader", crm.getDataHeader());
                customerArray.put(crmObj);
            }

            prefJobj.put("customer", customerArray);

            /*
             * fetching data for Vendor module
             */
            JSONArray vendorArray = new JSONArray();

            moduleId = Constants.Acc_Vendor_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();

            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("isReportField", crm.isReportField());
                crmObj.put("dataHeader", crm.getDataHeader());
                vendorArray.put(crmObj);
            }

            prefJobj.put("vendor", vendorArray);

            /*
             * fetching data for Purchase Requisition module
             */
            JSONArray purchaseRequisitionArray = new JSONArray();

            moduleId = Constants.Acc_Purchase_Requisition_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();

            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("isReportField", crm.isReportField());
                crmObj.put("dataHeader", crm.getDataHeader());
                purchaseRequisitionArray.put(crmObj);
            }

            prefJobj.put("purchaseRequisition", purchaseRequisitionArray);

            /*
             * fetching data for RFQ module
             */
            JSONArray rfqArray = new JSONArray();

            moduleId = Constants.Acc_RFQ_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();

            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("isReportField", crm.isReportField());
                crmObj.put("dataHeader", crm.getDataHeader());
                rfqArray.put(crmObj);
            }

            prefJobj.put("requestForQuotation", rfqArray); 
            /*
             * fetching data for Pack module
             */
            JSONArray packArr = new JSONArray();

            moduleId = Constants.Acc_Packing_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();

            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("isReportField", crm.isReportField());
                crmObj.put("dataHeader", crm.getDataHeader());
                packArr.put(crmObj);
            }

            prefJobj.put("packform", packArr);
            /*
             * fetching data for ship  module
             */
            JSONArray shipArr = new JSONArray();

            moduleId = Constants.Acc_Shipping_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();

            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("isReportField", crm.isReportField());
                crmObj.put("dataHeader", crm.getDataHeader());
                shipArr.put(crmObj);
            }

            prefJobj.put("shipform", shipArr);
            
            /*
             * fetching data for Lease Quotation module
             */
            JSONArray leaseQuotationArray = new JSONArray();

            moduleId = Constants.Acc_Lease_Quotation;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();

            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("isReportField", crm.isReportField());
                crmObj.put("dataHeader", crm.getDataHeader());
                leaseQuotationArray.put(crmObj);
            }

            prefJobj.put("leasequotation", leaseQuotationArray);
            
            /*
             * fetching data for Lease Order module
             */
            JSONArray leaseOrderArray = new JSONArray();

            moduleId = Constants.Acc_Lease_Order_ModuleId;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();

            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("isReportField", crm.isReportField());
                crmObj.put("dataHeader", crm.getDataHeader());
                leaseOrderArray.put(crmObj);
            }

            prefJobj.put("leaseorder", leaseOrderArray);
            
             /*
             * fetching data for Lease Order module
             */
            JSONArray leaseDeliveryOrderArray = new JSONArray();

            moduleId = Constants.Acc_Lease_DO;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();

            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("isReportField", crm.isReportField());
                crmObj.put("dataHeader", crm.getDataHeader());
                leaseDeliveryOrderArray.put(crmObj);
            }

            prefJobj.put("leaseDeliveryOrder", leaseDeliveryOrderArray);
            
            /*
             * fetching data for Lease Invoice module
             */
            JSONArray leaseInvoiceArray = new JSONArray();

            moduleId = Constants.LEASE_INVOICE_MODULEID;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();

            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("isReportField", crm.isReportField());
                crmObj.put("dataHeader", crm.getDataHeader());
                leaseInvoiceArray.put(crmObj);
            }

            prefJobj.put("leaseInvoice", leaseInvoiceArray);
            
            /*
             * fetching data for Lease return module
             */
            JSONArray leaseReturnArray = new JSONArray();

            moduleId = Constants.Acc_Lease_Return;

            crmResult = accCompanyPreferencesObj.getTransactionFormsFieldHideShowProperty(moduleId, companyid);

            crmList = crmResult.getEntityList();

            for (CustomizeReportMapping crm : crmList) {
                JSONObject crmObj = new JSONObject();
                crmObj.put("fieldId", crm.getDataIndex());
                crmObj.put("isHidden", crm.isHidden());
                crmObj.put("isReadOnly", crm.isReadOnlyField());
                crmObj.put("fieldLabelText", crm.getFieldLabelText());
                crmObj.put("isManadatoryField", crm.isManadatoryField());
                crmObj.put("isUserManadatoryField", crm.isUserManadatoryField());
                crmObj.put("isFormField", crm.isFormField());
                crmObj.put("isReportField", crm.isReportField());
                crmObj.put("dataHeader", crm.getDataHeader());
                leaseReturnArray.put(crmObj);
            }

            prefJobj.put("leaseReturn", leaseReturnArray);

        } catch (Exception ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public ModelAndView getExtraCompanyPreferences(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("id", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject result = accCompanyPreferencesObj.getExtraCompanyPreferences(requestParams);
            ExtraCompanyPreferences pref = null;
            if (result.getEntityList().size() > 0) {
                pref = (ExtraCompanyPreferences) result.getEntityList().get(0);
            }

            JSONObject prefJobj = getExtraCompanyPreferences(request, pref);
            jobj.put("data", prefJobj);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private JSONObject getExtraCompanyPreferences(HttpServletRequest request, ExtraCompanyPreferences pref) {
        JSONObject obj = new JSONObject();
        boolean isCPAndWIPAccountsSET = false;
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            if (pref == null) {
                obj.put("isCPAndWIPAccountsSET", isCPAndWIPAccountsSET);
                return obj;
            } else {

                String cpAccountPrefix = pref.getCpAccountPrefix();
                String wipAccountPrefix = pref.getWipAccountPrefix();
                if (!StringUtil.isNullOrEmpty(cpAccountPrefix) && !StringUtil.isNullOrEmpty(wipAccountPrefix)) {
                    isCPAndWIPAccountsSET = true;
                }
                obj.put("isCPAndWIPAccountsSET", isCPAndWIPAccountsSET);
                obj.put("isLMSIntegration", pref != null ? pref.isLMSIntegration() : false);

            }
            if (pref.getActiveFromDate() != null && pref.getActiveToDate() != null) {
                obj.put("fromdate", df.format(pref.getActiveFromDate()));
                obj.put("todate", df.format(pref.getActiveToDate()));
            }
        } catch (JSONException ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return obj;
    }

    public ModelAndView getCompanyAccountPreferences(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {

            String companyid = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);
            String subdomain = URLUtil.getDomainName(request);
            String companyCreatorId = "";
            String loginId = "";
            Boolean isCompanyCreatorLogged = false;
            KwlReturnObject result = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) result.getEntityList().get(0);
            result = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) result.getEntityList().get(0);
            result = accountingHandlerDAOobj.getObject(MRPCompanyPreferences.class.getName(), companyid);
            MRPCompanyPreferences mrpCompanyPreferences = (MRPCompanyPreferences) result.getEntityList().get(0);
            result = accountingHandlerDAOobj.getObject(IndiaComplianceCompanyPreferences.class.getName(), companyid);
            IndiaComplianceCompanyPreferences complianceCompanyPreferences = (IndiaComplianceCompanyPreferences) result.getEntityList().get(0);
            result = accountingHandlerDAOobj.getObject(DocumentEmailSettings.class.getName(), companyid);
            DocumentEmailSettings documentEmailSettings = (DocumentEmailSettings) result.getEntityList().get(0);
            String pmURL = ConfigReader.getinstance().get("pmURL");
            /**
             * Here we are checking Logged user is Company Creator or not.
             * Ticket SDP-9739.
             */
            loginId=sessionHandlerImpl.getUserid(request);
            KwlReturnObject companyObj = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyObj.getEntityList().get(0);
            companyCreatorId = company.getCreator().getUserID();
            if(loginId.equals(companyCreatorId)){
                isCompanyCreatorLogged=true;
            }
            if (!StringUtil.isNullOrEmpty(pmURL)) {
                pmURL += "a/" + subdomain + "/";
            }
//            }
            //below commented code changed the financial year so need to commet this code

//            Calendar systemDate = Calendar.getInstance();
//            Calendar financialYearFromTemp = Calendar.getInstance();
//            financialYearFromTemp.setTime(pref.getFirstFinancialYearFrom()!=null?pref.getFirstFinancialYearFrom():pref.getFinancialYearFrom());
//            Calendar financialYearFrom = Calendar.getInstance();
//            financialYearFrom.setTime(pref.getFinancialYearFrom());            
//            financialYearFrom.set(Calendar.YEAR,financialYearFrom.get(Calendar.YEAR) + 1);
//            if(systemDate.after(financialYearFrom)){
//            	pref.setFinancialYearFrom(financialYearFrom.getTime());
//                pref.setFirstFinancialYearFrom(financialYearFromTemp.getTime());
//            	accCompanyPreferencesObj.setNewYear(financialYearFrom.getTime(),financialYearFromTemp.getTime(),sessionHandlerImpl.getCompanyid(request));
////            	accCompanyPreferencesObj.setCurrentYear(financialYearFrom.get(Calendar.YEAR),(financialYearFrom.get(Calendar.YEAR) - 1),sessionHandlerImpl.getCompanyid(request));
//            }else if(systemDate.before(financialYearFromTemp)){
//            	financialYearFromTemp.set(Calendar.YEAR,financialYearFromTemp.get(Calendar.YEAR) - 1);
//            	pref.setFinancialYearFrom(financialYearFromTemp.getTime());
//            	accCompanyPreferencesObj.setNewYear(financialYearFrom.getTime(),financialYearFromTemp.getTime(),sessionHandlerImpl.getCompanyid(request));
////            	accCompanyPreferencesObj.setCurrentYear((financialYearFrom.get(Calendar.YEAR) - 1),(financialYearFrom.get(Calendar.YEAR) - 2),sessionHandlerImpl.getCompanyid(request));
//            }
            //Fetch newly created sequence formats
            Map<String, Object> filterParams = new HashMap<String, Object>();
            filterParams.put("companyid", pref.getID());
            KwlReturnObject result1 = accCompanyPreferencesObj.getSequenceFormat(filterParams);
            List ll = result1.getEntityList();

            KwlReturnObject chequeFormatResult = accCompanyPreferencesObj.getChequeSequenceFormatList(filterParams);
            List chequeFormatList = chequeFormatResult.getEntityList();

            if (extraCompanyPreferences != null) {
                request.setAttribute("extraCompanyPreferences", extraCompanyPreferences);
            }
            if (complianceCompanyPreferences != null) {
                request.setAttribute("complianceExtraCompanyPreferences", complianceCompanyPreferences);
            }
            if (mrpCompanyPreferences != null) {
                request.setAttribute("mrpCompanyPreferences", mrpCompanyPreferences);
            }
            if (documentEmailSettings != null) {
                request.setAttribute("documentEmailSettings", documentEmailSettings);
            }

            boolean freezDepreciation = accCompanyPreferencesObj.getDepreciationCount(companyid);
            JSONObject prefJobj = accCompanyPreferencesService.getCompanyAccountPreferences(request, pref, ll, chequeFormatList);
            prefJobj.put("freezDepreciation", freezDepreciation);

            boolean openingDepreciationPosted = accCompanyPreferencesObj.getopeningDepreciationPostedCount(companyid);
            prefJobj.put("openingDepreciationPosted", openingDepreciationPosted);

            prefJobj.put("pmURL", pmURL);
            prefJobj.put("isCompanyCreatorLogged", isCompanyCreatorLogged);
            
            /* Getting Value For Aged Date Filter */
            result = accCompanyPreferencesObj.getAgedDateFilter(userId);
            if (!result.getEntityList().isEmpty() && result.getEntityList().size() > 0) {
                JSONObject DateFiletrJson =  new JSONObject(result.getEntityList().get(0).toString());
                prefJobj.put(Constants.agedPayableDateFilter, DateFiletrJson.optInt(Constants.agedPayableDateFilter,Constants.agedInvoiceDateFilter));  //  Default InvoiceDateFilter
                prefJobj.put(Constants.agedPayableInterval, DateFiletrJson.optInt(Constants.agedPayableInterval,Constants.DefaultIntervalInDays)); // Default Interval Days is 30
                prefJobj.put(Constants.agedPayableNoOfInterval, DateFiletrJson.optInt(Constants.agedPayableNoOfInterval,Constants.DefaultNoOfIntervals)); // Default NoOfInterval is 7
                prefJobj.put(Constants.agedReceivableDateFilter, DateFiletrJson.optInt(Constants.agedReceivableDateFilter,Constants.agedInvoiceDateFilter)); //  Default InvoiceDateFilter 
                prefJobj.put(Constants.agedReceivableInterval, DateFiletrJson.optInt(Constants.agedReceivableInterval,Constants.DefaultIntervalInDays)); // Default Interval Days is 30
                prefJobj.put(Constants.agedReceivableNoOfInterval, DateFiletrJson.optInt(Constants.agedReceivableNoOfInterval,Constants.DefaultNoOfIntervals)); // Default NoOfInterval is 7
            } else {  // in Case No Prefernces are save For User (For Ageing as well as SOA)
                prefJobj.put(Constants.agedPayableDateFilter, Constants.agedInvoiceDateFilter);  //  Default InvoiceDateFilter         
                prefJobj.put(Constants.agedPayableInterval, Constants.DefaultIntervalInDays); // Default Interval Days is 30
                prefJobj.put(Constants.agedPayableNoOfInterval, Constants.DefaultNoOfIntervals); // Default NoOfInterval is 7
                prefJobj.put(Constants.agedReceivableDateFilter,Constants.agedInvoiceDateFilter); //  Default InvoiceDateFilter
                prefJobj.put(Constants.agedReceivableInterval, Constants.DefaultIntervalInDays); // Default Interval Days is 30
                prefJobj.put(Constants.agedReceivableNoOfInterval, Constants.DefaultNoOfIntervals); // Default NoOfInterval is 7
            }
            Date finanDate = pref.getFirstFinancialYearFrom() != null ? pref.getFirstFinancialYearFrom() : pref.getFinancialYearFrom();
            prefJobj.put("isBookClosed", accCompanyPreferencesObj.isBookClosed(finanDate, companyid));
            /**
             * **** If Integration with Accounting and Inventory System then
             * set QA Approval Flow Status from Inventory system *****
             */
            if (pref.isInventoryAccountingIntegration()) {
                HashMap<String, Object> prefMap = new HashMap<String, Object>();
                boolean isQAApprovalFlow = getQAApprovalStatus(companyid, sessionHandlerImpl.getUserid(request));
                prefMap.put("id", companyid);
                prefMap.put("isQaApprovalFlow", isQAApprovalFlow);
                result = accCompanyPreferencesObj.updatePreferences(prefMap);
                if (!result.getEntityList().isEmpty()) {
                    CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) result.getEntityList().get(0);
                    prefJobj.put(CompanyPreferencesConstants.QAAPPROVALFLOW, companyAccountPreferences.isQaApprovalFlow());
                } else {
                    prefJobj.put(CompanyPreferencesConstants.QAAPPROVALFLOW, isQAApprovalFlow);
                }
            }
            getTransactionFormFieldHideShowStatus(companyid, prefJobj);
//            setCurrencyExchangeRateValueInSession(request);
            int mappedCompaniesCount = accCompanyPreferencesObj.getMappedCompaniesCount(companyid);
            jobj.put("consolidateFlag", mappedCompaniesCount > 0 ? true : false);
            jobj.put("data", prefJobj);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    //ERP-31958: Get Next AUto NUmber
    public ModelAndView getNextAutoNumber(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = accCompanyPreferencesService.getNextAutoNumber(paramJobj);
        } catch (Exception exception) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, exception);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public Map<String, Object> saveChequeSequenceFormat(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
        Map<String, Object> chequeMap = new HashMap<>();
        int numberofdigit = 0;
        int chequeEndNumber = 0;
        String bankAccountId = null;
        ChequeSequenceFormat chequeSequenceFormat = null;
        boolean isChecked = false, isdefault = false;
        if (!StringUtil.isNullObject(request.getParameter("numberofdigit"))) {
            numberofdigit = Integer.parseInt(request.getParameter("numberofdigit"));
        }
        if (!StringUtil.isNullObject(request.getParameter("chequeEndNumber"))) {
            chequeEndNumber = Integer.parseInt(request.getParameter("chequeEndNumber"));
        }
        if (!StringUtil.isNullObject(request.getParameter("bankAccountId"))) {
            bankAccountId = request.getParameter("bankAccountId");
        }
        BigInteger startfrom = new BigInteger("1");
        if (!StringUtil.isNullOrEmpty(request.getParameter("startfrom"))) {
            startfrom = new BigInteger(request.getParameter("startfrom"));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("isChecked"))) {
            isChecked = StringUtil.getBoolean(request.getParameter("isChecked"));
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("isdefault"))) {
            isdefault = StringUtil.getBoolean(request.getParameter("isdefault"));
        }
        String mode = request.getParameter("mode");
            String prefix = request.getParameter("prefix");
            String suffix = request.getParameter("suffix");
            String dateFormatinPrefix = request.getParameter("selecteddateformat");
            String selecteddateformatafterprefix = request.getParameter("selecteddateformatafterprefix");
            boolean isdefaultformat = StringUtil.getBoolean(request.getParameter("isdefaultformat"));
            boolean isshowdateinprefix = StringUtil.getBoolean(request.getParameter("showdateinprefix"));
            boolean showdateafterprefix = StringUtil.getBoolean(request.getParameter("showdateafterprefix"));
            boolean showdateaftersuffix = StringUtil.getBoolean(request.getParameter("showdateaftersuffix"));
            boolean resetcounter = StringUtil.getBoolean(request.getParameter("resetcounter"));
            String selectedsuffixdateformat = request.getParameter("selectedsuffixdateformat");

        String companyId = sessionHandlerImpl.getCompanyid(request);
        boolean isduplicate = false;

        // Check any sequence format is available for selected bank if available then update it
        HashMap<String, Object> requestParams = new HashMap<String, Object>();

        String id = request.getParameter("id");

        String format = "";
        for (int i = 0; i < numberofdigit; i++) {
            format += "0";
        }

        requestParams.put("prefix", prefix);
        requestParams.put("suffix", suffix);
        prefix = dateFormatinPrefix + prefix + selecteddateformatafterprefix;    //SDP-3810 
        suffix = suffix + selectedsuffixdateformat;
        format = prefix + format + suffix;
        boolean showleadingzero = StringUtil.getBoolean(request.getParameter("showleadingzero"));

        requestParams.put("numberofdigit", numberofdigit);
        requestParams.put("companyid", companyId);
        requestParams.put("startfrom", startfrom);
        requestParams.put("showleadingzero", showleadingzero);
        requestParams.put("bankAccountId", bankAccountId);
//        requestParams.put("chequeEndNumber", chequeEndNumber);
        requestParams.put("name", format);
//        requestParams.put("name", startfrom + bankAccountId + format);
        requestParams.put("isdefault", isdefault);
        requestParams.put("isdefaultformat", isdefaultformat);
        requestParams.put("isshowdateinprefix", isshowdateinprefix);
        requestParams.put("dateFormatinPrefix", dateFormatinPrefix);
        requestParams.put("isshowdateafterprefix", showdateafterprefix);
        requestParams.put("dateFormatAfterPrefix", selecteddateformatafterprefix);
        requestParams.put("showdateaftersuffix", showdateaftersuffix);
        requestParams.put("selectedsuffixdateformat", selectedsuffixdateformat);
        requestParams.put("resetcounter", resetcounter);

        if (StringUtil.isNullOrEmpty(id)) {
            KwlReturnObject seqFormat = accCompanyPreferencesObj.getChequeSequenceFormatList(requestParams);
            requestParams.put("isChecked", isChecked);
            if (seqFormat.getRecordTotalCount() == 0) {
                requestParams.put("chequeEndNumber", chequeEndNumber);
                chequeSequenceFormat = accCompanyPreferencesObj.saveChequeSequenceFormat(requestParams);
            } else {
                isduplicate = true;
            }
        } else {
            requestParams.put("isChecked", isChecked);
            requestParams.put("id", id);
            chequeSequenceFormat = accCompanyPreferencesObj.updateChequeSequenceFormat(requestParams);
        }

        // get All the cheque sequence format for this company and return those
        format = getAllChequeSequenceFormatsForCompany(companyId);

//        returnList.add(format);
        chequeMap.put("format", format);
        chequeMap.put("isduplicate", isduplicate);
        return chequeMap;

    }

    private String getAllChequeSequenceFormatsForCompany(String companyId) throws ServiceException {
        String returnFormat = "";

        Map<String, Object> filterParams = new HashMap<String, Object>();
        filterParams.put("companyid", companyId);

        KwlReturnObject chequeFormatResult = accCompanyPreferencesObj.getChequeSequenceFormatList(filterParams);
        List chequeFormatList = chequeFormatResult.getEntityList();

        Iterator chequeFormatListitr = chequeFormatList.iterator();
        String chequeSequenceFormatValue = "";
        while (chequeFormatListitr.hasNext()) {
            String chequeNumberFormat = CompanyPreferencesCMN.getChequeNumberFormat((ChequeSequenceFormat) chequeFormatListitr.next());
            chequeSequenceFormatValue += chequeNumberFormat + ",";
        }
        returnFormat = chequeSequenceFormatValue.equalsIgnoreCase("") ? "" : (chequeSequenceFormatValue.endsWith(",") ? chequeSequenceFormatValue.substring(0, chequeSequenceFormatValue.length() - 1) : chequeSequenceFormatValue);

        return returnFormat;
    }

    //  need to save data in extra company preferences
    public ModelAndView saveChequeSequenceFormat(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        Boolean isduplicate = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CAP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);

        try {
            Map<String, Object> chequeSequenceMap = saveChequeSequenceFormat(request);
            String format = (String) chequeSequenceMap.get("format");
            isduplicate = (Boolean) chequeSequenceMap.get("isduplicate");
            jobj.put("name", format);
            jobj.put("isduplicate", isduplicate);
            issuccess = true;
            msg = messageSource.getMessage("acc.sequence.format.save", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("isduplicate", isduplicate);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());

    }

    public ModelAndView saveFixedAssetSetting(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        KwlReturnObject kwlObj;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CAP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            jobj = saveFixedAssetSetting(request);
            msg = messageSource.getMessage("acc.companypreferences.asset.assetSettingSave.successMsg", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());

    }

    /**
     * Method to save the Allow Zero quantity settings from system preferences
     */
    public ModelAndView saveAllowZeroQtyForProduct(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kwlObj;
        String msg = "";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CAP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {

            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = accCompanyPreferencesObj.saveAllowZeroQtyForProduct(paramJobj);
            msg = messageSource.getMessage("acc.companypreferences.AllowZeroQuantity.successMsg", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            txnManager.commit(status);
        } catch (JSONException | SessionExpiredException ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView saveIndianGSTSettings(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject params = StringUtil.convertRequestToJsonObject(request);
            jobj = accCompanyPreferencesService.saveIndianGSTSettings(params);
            issuccess = true;
            msg = messageSource.getMessage("acc.IndianGSTSettings.successMsg", null, RequestContextUtils.getLocale(request));
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public JSONObject saveFixedAssetSetting(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            boolean assetSetingActivation = false;
            boolean allowToPostOpeningDepreciation = false;
            int depreciationCalculationType = 0, depreciationCalculationBasedOn = 0;

            if (!StringUtil.isNullObject(request.getParameter("assetSetingActivation"))) {
                assetSetingActivation = Boolean.parseBoolean(request.getParameter("assetSetingActivation"));
            }

            if (!StringUtil.isNullObject(request.getParameter("depreciationCalculationType"))) {
                depreciationCalculationType = Integer.parseInt(request.getParameter("depreciationCalculationType"));
            }

            if (!StringUtil.isNullObject(request.getParameter("depreciationCalculationBasedOn"))) {
                depreciationCalculationBasedOn = Integer.parseInt(request.getParameter("depreciationCalculationBasedOn"));
            }

            if (!StringUtil.isNullObject(request.getParameter("allowToPostOpeningDepreciation"))) {
                allowToPostOpeningDepreciation = Boolean.parseBoolean(request.getParameter("allowToPostOpeningDepreciation"));
            }

            String companyId = sessionHandlerImpl.getCompanyid(request);

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("company", companyId);
            requestParams.put("assetSetingActivation", assetSetingActivation);
            requestParams.put("allowToPostOpeningDepreciation", allowToPostOpeningDepreciation);
            requestParams.put("depreciationCalculationType", depreciationCalculationType);
            requestParams.put("depreciationCalculationBasedOn", depreciationCalculationBasedOn);

            KwlReturnObject prefRes = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);

            ExtraCompanyPreferences companyPreferences = null;

            if (prefRes != null && !prefRes.getEntityList().isEmpty()) {
                companyPreferences = (ExtraCompanyPreferences) prefRes.getEntityList().get(0);
                requestParams.put("id", companyId);
                companyPreferences = accCompanyPreferencesObj.updateExtraCompanyPreferences(requestParams);
            } else {
                companyPreferences = accCompanyPreferencesObj.saveExtraCompanyPreferences(requestParams);
            }

            jobj.put("ActivateFixedAssetModule", companyPreferences.isAssetSetingActivation());
            jobj.put("depreciationCalculationType", companyPreferences.getAssetDepreciationCalculationType());
            jobj.put("depreciationCalculationBasedOn", companyPreferences.getAssetDepreciationCalculationBasedOn());
            jobj.put("allowToPostOpeningDepreciation", companyPreferences.isAllowToPostOpeningDepreciation());

        } catch (JSONException ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveFixedAssetSetting : " + ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveFixedAssetSetting : " + ex.getMessage(), ex);
        }
        return jobj;
    }

    public ModelAndView saveControlAccountsSettings(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        KwlReturnObject kwlObj;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CAP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            jobj = saveControlAccountsSettings(request);
            msg = messageSource.getMessage("acc.companypreferences.controlAccountSettingsSuccessMsg", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());

    }

    public JSONObject saveControlAccountsSettings(HttpServletRequest request) throws SessionExpiredException, ServiceException, AccountingException {
        JSONObject jobj = new JSONObject();
        try {
            String profitLossAccountId = "", openingStockAccountId = "", closingStockAccountId = "", stockInHandAccountId = "";

            if (!StringUtil.isNullObject(request.getParameter("profitLossAccountId"))) {
                profitLossAccountId = request.getParameter("profitLossAccountId").equals("None") ? "" : request.getParameter("profitLossAccountId");
            }
            if (!StringUtil.isNullObject(request.getParameter("openingStockAccountId"))) {
                openingStockAccountId = request.getParameter("openingStockAccountId").equals("None") ? "" : request.getParameter("openingStockAccountId");
            }
            if (!StringUtil.isNullObject(request.getParameter("closingStockAccountId"))) {
                closingStockAccountId = request.getParameter("closingStockAccountId").equals("None") ? "" : request.getParameter("closingStockAccountId");
            }
            if (!StringUtil.isNullObject(request.getParameter("stockInHandAccountId"))) {
                stockInHandAccountId = request.getParameter("stockInHandAccountId").equals("None") ? "" : request.getParameter("stockInHandAccountId");
            }

            String companyId = sessionHandlerImpl.getCompanyid(request);
            if (StringUtil.isNullOrEmpty(profitLossAccountId)) {
                JSONObject requestParams = new JSONObject();
                requestParams.put(Constants.companyKey, companyId);
                boolean isBookClose = accCompanyPreferencesObj.isBookClose(requestParams);
                if (isBookClose) {
                    throw new AccountingException(messageSource.getMessage("acc.compref.savecontrolaccount.map.previous.year.accumulated.profitloss", null, RequestContextUtils.getLocale(request)));
                }
            }
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("company", companyId);
            requestParams.put("profitLossAccountId", profitLossAccountId);
            requestParams.put("openingStockAccountId", openingStockAccountId);
            requestParams.put("closingStockAccountId", closingStockAccountId);
            requestParams.put("stockInHandAccountId", stockInHandAccountId);

            KwlReturnObject prefRes = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            ExtraCompanyPreferences companyPreferences = null;

            if (prefRes != null && !prefRes.getEntityList().isEmpty()) {
                companyPreferences = (ExtraCompanyPreferences) prefRes.getEntityList().get(0);
                requestParams.put("id", companyId);
                companyPreferences = accCompanyPreferencesObj.updateExtraCompanyPreferences(requestParams);
            } else {
                companyPreferences = accCompanyPreferencesObj.saveExtraCompanyPreferences(requestParams);
            }

            KwlReturnObject venresult = null;
            if (!StringUtil.isNullOrEmpty(companyPreferences.getProfitLossAccountId())) {
                venresult = accCompanyPreferencesObj.getAccountObjectById(companyPreferences.getProfitLossAccountId(), sessionHandlerImpl.getCompanyid(request));
                Account accountProfitAndLoss = (Account) venresult.getEntityList().get(0);
                accountProfitAndLoss.setControlAccounts(true);
                String usedin = accountProfitAndLoss.getUsedIn();
                accountProfitAndLoss.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.NetProfitLossAccountName));
            }

            if (!StringUtil.isNullOrEmpty(companyPreferences.getOpeningStockAccountId())) {
                venresult = accCompanyPreferencesObj.getAccountObjectById(companyPreferences.getOpeningStockAccountId(), sessionHandlerImpl.getCompanyid(request));
                Account openingStockAccount = (Account) venresult.getEntityList().get(0);
                openingStockAccount.setControlAccounts(true);
                String usedin = openingStockAccount.getUsedIn();
                openingStockAccount.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.OpeningStock));
            }

            if (!StringUtil.isNullOrEmpty(companyPreferences.getClosingStockAccountId())) {
                venresult = accCompanyPreferencesObj.getAccountObjectById(companyPreferences.getClosingStockAccountId(), sessionHandlerImpl.getCompanyid(request));
                Account closingStockAccount = (Account) venresult.getEntityList().get(0);
                closingStockAccount.setControlAccounts(true);
                String usedin = closingStockAccount.getUsedIn();
                closingStockAccount.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.ClosingStock));
            }

            if (!StringUtil.isNullOrEmpty(companyPreferences.getStockInHandAccountId())) {
                venresult = accCompanyPreferencesObj.getAccountObjectById(companyPreferences.getStockInHandAccountId(), sessionHandlerImpl.getCompanyid(request));
                Account stockInHandAccount = (Account) venresult.getEntityList().get(0);
                stockInHandAccount.setControlAccounts(true);
                String usedin = stockInHandAccount.getUsedIn();
                stockInHandAccount.setUsedIn(StringUtil.getUsedInValue(usedin, Constants.StockInHand));
            }

            jobj.put("profitLossAccountId", companyPreferences.getProfitLossAccountId() == null ? "" : companyPreferences.getProfitLossAccountId());
            jobj.put("openingStockAccountId", companyPreferences.getOpeningStockAccountId() == null ? "" : companyPreferences.getOpeningStockAccountId());
            jobj.put("closingStockAccountId", companyPreferences.getClosingStockAccountId() == null ? "" : companyPreferences.getClosingStockAccountId());
            jobj.put("stockInHandAccountId", companyPreferences.getStockInHandAccountId() == null ? "" : companyPreferences.getStockInHandAccountId());
        } catch (JSONException ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveFixedAssetSetting : " + ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveFixedAssetSetting : " + ex.getMessage(), ex);
        }
        return jobj;
    }

    public ModelAndView getManualJePostSettingData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        String msg = "";
        boolean issuccess = false;
        JSONObject obj = new JSONObject();
        Account account = null;
        try {
            String start = request.getParameter(Constants.start);
            String limit = request.getParameter(Constants.limit);
            HashMap<String, Object> requestMap = new HashMap<String, Object>();
            requestMap.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            requestMap.put("currencyid", request.getParameter(Constants.globalCurrencyKey) != null ? request.getParameter(Constants.globalCurrencyKey) : sessionHandlerImpl.getCurrencyID(request));
            requestMap.put(Constants.df, authHandler.getDateOnlyFormat(request));  //This format belongs to our global date format[i.e.new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa")]
            requestMap.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(request)); //This format holds users date format.
            requestMap.put("controlAccounts", true);
            if (request.getParameter("dir") != null && !StringUtil.isNullOrEmpty(request.getParameter("dir"))
                    && request.getParameter("sort") != null && !StringUtil.isNullOrEmpty(request.getParameter("sort"))) {
                requestMap.put("dir", request.getParameter("dir"));
                requestMap.put("sort", request.getParameter("sort"));
            }
            KwlReturnObject result = accAccountDAOobj.getAccountsForCombo(requestMap);
            List list = result.getEntityList();

            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Object[] objArr =(Object[]) itr.next();
                String accountid = (String) objArr[0];
                result = accountingHandlerDAOobj.getObject(Account.class.getName(), accountid);
                account = (Account) result.getEntityList().get(0);
                if (!StringUtil.isNullOrEmpty(account.getUsedIn())) {
                    obj = new JSONObject();
                    obj.put("accname", (!StringUtil.isNullOrEmpty(account.getName()) ? account.getName() : "") + (!StringUtil.isNullOrEmpty(account.getAcccode()) ? "(" + account.getAcccode() + ")" : ""));
                    obj.put("accountid", account.getID());
                    obj.put("acccode", account.getAcccode());
                    obj.put("purpose", account.getUsedIn());
                    obj.put("haveToPostJe", account.isWantToPostJe());
                    jArr.put(obj);
                }
            }
            JSONArray pagedJson = jArr;
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            jobj.put("data", pagedJson);
            jobj.put("count", jArr.length());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveManualJePostSettingData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        String msg = "";
        boolean issuccess = false;
        JSONObject obj = new JSONObject();
        try {
            String auditmsg = "", auditmsg1 = "", auditmsg2 = "";
            Account account = null;
            jArr = new JSONArray(request.getParameter("data"));
            for (int i = 0; i < jArr.length(); i++) {
                obj = jArr.getJSONObject(i);
                if (!StringUtil.isNullOrEmpty(StringUtil.DecodeText(obj.optString("accountid")))) {
                    KwlReturnObject accresult = accountingHandlerDAOobj.getObject(Account.class.getName(), StringUtil.DecodeText(obj.optString("accountid")));
                    if (accresult != null && !accresult.getEntityList().isEmpty() && accresult.getEntityList().get(0) != null) {
                        account = (Account) accresult.getEntityList().get(0);
                        account.setWantToPostJe(obj.getBoolean("haveToPostJe"));
                        String accountAuditmsg = (!StringUtil.isNullOrEmpty(account.getName()) ? account.getName() : "") + (!StringUtil.isNullOrEmpty(account.getAcccode()) ? "(" + account.getAcccode() + ")" : "");
                        if (obj.getBoolean("haveToPostJe")) {
                            auditmsg1 += accountAuditmsg + ", ";
                        } else {
                            auditmsg2 += accountAuditmsg + ", ";
                        }
                    }
                }
            }

            auditmsg1 = auditmsg1.substring(0, Math.max(0, auditmsg1.length() - 2));
            auditmsg2 = auditmsg2.substring(0, Math.max(0, auditmsg2.length() - 2));
            if (!StringUtil.isNullOrEmpty(auditmsg1) && !StringUtil.isNullOrEmpty(auditmsg2)) {
                auditmsg += ("Manual JE can be posted in " + auditmsg1 + " and " + "Manual JE can not be posted in " + auditmsg2);
            } else if (!StringUtil.isNullOrEmpty(auditmsg1)) {
                auditmsg += ("Manual JE can be posted in " + auditmsg1);
            } else if (!StringUtil.isNullOrEmpty(auditmsg2)) {
                auditmsg += ("Manual JE cannot be posted in " + auditmsg2);
            }
            if (!StringUtil.isNullOrEmpty(auditmsg)) {
                auditTrailObj.insertAuditLog(AuditAction.Allow_To_Post_Manual_JE, "User " + sessionHandlerImpl.getUserFullName(request) + " has Changed the Control Account Setting as:  " + auditmsg, request, AuditAction.Allow_To_Post_Manual_JE);
            }
            msg = messageSource.getMessage("acc.manualJEPostSetting.success", null, RequestContextUtils.getLocale(request));
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveSequenceFormat(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        String formatName = "",sequenceFormatid="";//    ERP-18433
        KwlReturnObject kwlObj;
        SequenceFormat seqFormat;
        boolean isDuplicate = false,matchedTransactionswithsequenceformat=false;
        boolean updateflag = false;
        boolean issuccess = false;
        boolean isColumnMaxSizeMsg = false;
        int moduleid = 0,totalmatchedtransactions=0;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CAP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String mode = request.getParameter("mode");
            String prefix = request.getParameter("prefix");
            String suffix = request.getParameter("suffix");
            String dateFormatinPrefix = request.getParameter("selecteddateformat");
            String selecteddateformatafterprefix = request.getParameter("selecteddateformatafterprefix");
            int numberofdigit = Integer.parseInt(request.getParameter("numberofdigit"));
            int startfrom = 1;
            if (!StringUtil.isNullOrEmpty(request.getParameter("startfrom"))) {
                startfrom = Integer.parseInt(request.getParameter("startfrom"));
            }
            boolean showleadingzero = StringUtil.getBoolean(request.getParameter("showleadingzero"));
            boolean isdefaultformat = StringUtil.getBoolean(request.getParameter("isdefaultformat"));
            boolean isshowdateinprefix = StringUtil.getBoolean(request.getParameter("showdateinprefix"));
            boolean showdateafterprefix = StringUtil.getBoolean(request.getParameter("showdateafterprefix"));
            boolean isChecked = StringUtil.getBoolean(request.getParameter("isChecked"));
            boolean showdateaftersuffix = StringUtil.getBoolean(request.getParameter("showdateaftersuffix"));
            boolean resetcounter = StringUtil.getBoolean(request.getParameter("resetcounter"));
            String selectedsuffixdateformat = request.getParameter("selectedsuffixdateformat");
            matchedTransactionswithsequenceformat = request.getParameter("matchedTransactionswithsequenceformat")!=null?StringUtil.getBoolean(request.getParameter("matchedTransactionswithsequenceformat")):false;
            boolean isForSave=false;
            int columnMaxSize = 50;
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            
            String module = request.getParameter("module");
            requestParams.put("prefix", prefix);
            requestParams.put("suffix", suffix);
            requestParams.put("numberofdigit", numberofdigit);
            requestParams.put("startfrom", startfrom);
            requestParams.put("showleadingzero", showleadingzero);
            requestParams.put("isdefaultformat", isdefaultformat);
            requestParams.put("isshowdateinprefix", isshowdateinprefix);
            requestParams.put("dateFormatinPrefix", dateFormatinPrefix);
            requestParams.put("isshowdateafterprefix", showdateafterprefix);
            requestParams.put("dateFormatAfterPrefix", selecteddateformatafterprefix);
            requestParams.put("showdateaftersuffix", showdateaftersuffix);
            requestParams.put("selectedsuffixdateformat", selectedsuffixdateformat);
            requestParams.put("resetcounter", resetcounter);
            //requestParams.put("isChecked", isChecked);
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            if (!StringUtil.isNullOrEmpty(request.getParameter("custom"))) {
                requestParams.put("custom", request.getParameter("custom"));
            }
            String format = "";
            for (int i = 0; i < numberofdigit; i++) {
                format += "0";
            }

            /*
             * If user select Date after prefix then this date format will be part of Prefix. Becuse we cannot add this date format in the middle of Prefix & Number digit.
             * Because prefix+number+suffix becomes NAME. And at the time data retrival we get Name & then append prefix/suffix to it.
             */
            prefix = prefix + selecteddateformatafterprefix;    //SDP-3810 

            format = prefix + format + suffix;
            requestParams.put("name", format);
            if (format.length() > columnMaxSize) {
                isColumnMaxSizeMsg = true;
            }
            if (!StringUtil.isNullOrEmpty(format)) {
                if (mode.equalsIgnoreCase("autojournalentry")) {
                    moduleid = Constants.Acc_GENERAL_LEDGER_ModuleId;
                }
                if (mode.equalsIgnoreCase("autoinvoice")) {
                    moduleid = Constants.Acc_Invoice_ModuleId;
                }
                if (mode.equalsIgnoreCase("autocreditmemo")) {
                    moduleid = Constants.Acc_Credit_Note_ModuleId;
                }

                if (mode.equalsIgnoreCase("autoreceipt")) {
                    moduleid = Constants.Acc_Receive_Payment_ModuleId;
                }
                if (mode.equalsIgnoreCase("autoSecurityNo")) {
                    moduleid = Constants.Acc_SecurityGateEntry_ModuleId;
                }

                if (mode.equalsIgnoreCase("autogoodsreceipt")) {
                    moduleid = Constants.Acc_Vendor_Invoice_ModuleId;
                }

                if (mode.equalsIgnoreCase("autodebitnote")) {
                    moduleid = Constants.Acc_Debit_Note_ModuleId;
                }

                if (mode.equalsIgnoreCase("autopayment")) {
                    moduleid = Constants.Acc_Make_Payment_ModuleId;
                }

                if (mode.equalsIgnoreCase("autoso")) {
                    moduleid = Constants.Acc_Sales_Order_ModuleId;
                }
                if (mode.equalsIgnoreCase("autocontract")) {
                    moduleid = Constants.Acc_Contract_Order_ModuleId;
                }

                if (mode.equalsIgnoreCase("autopo")) {
                    moduleid = Constants.Acc_Purchase_Order_ModuleId;
                }

                if (mode.equalsIgnoreCase("autocashsales")) {
                    moduleid = Constants.Acc_Cash_Sales_ModuleId;
                }

                if (mode.equalsIgnoreCase("autocashpurchase")) {
                    moduleid = Constants.Acc_Cash_Purchase_ModuleId;
                }

                if (mode.equalsIgnoreCase("autobillinginvoice")) {
                    moduleid = Constants.Acc_BillingInvoice_ModuleId;
                }

                if (mode.equalsIgnoreCase("autobillingreceipt")) {
                    moduleid = Constants.Acc_BillingReceive_Payment_ModuleId;
                }

                if (mode.equalsIgnoreCase("autobillingcashsales")) {
                    moduleid = Constants.Acc_Billing_Cash_Sales_ModuleId;
                }

                if (mode.equalsIgnoreCase("autobillinggoodsreceipt")) {
                    moduleid = Constants.Acc_Vendor_BillingInvoice_ModuleId;
                }

                if (mode.equalsIgnoreCase("autobillingdebitnote")) {
                    moduleid = Constants.Acc_BillingDebit_Note_ModuleId;
                }

                if (mode.equalsIgnoreCase("autobillingcreditmemo")) {
                    moduleid = Constants.Acc_BillingCredit_Note_ModuleId;
                }

                if (mode.equalsIgnoreCase("autobillingpayment")) {
                    moduleid = Constants.Acc_BillingMake_Payment_ModuleId;
                }

                if (mode.equalsIgnoreCase("autobillingso")) {
                    moduleid = Constants.Acc_BillingSales_Order_ModuleId;
                }

                if (mode.equalsIgnoreCase("autobillingpo")) {
                    moduleid = Constants.Acc_BillingPurchase_Order_ModuleId;
                }

                if (mode.equalsIgnoreCase("autobillingcashpurchase")) {
                    moduleid = Constants.Acc_BillingCash_Purchase_ModuleId;
                }

                if (mode.equalsIgnoreCase("autorequisition")) {
                    moduleid = Constants.Acc_Purchase_Requisition_ModuleId;
                }

                if (mode.equalsIgnoreCase("autorequestforquotation")) {
                    moduleid = Constants.Acc_RFQ_ModuleId;
                }

                if (mode.equalsIgnoreCase("autovenquotation")) {
                    moduleid = Constants.Acc_Vendor_Quotation_ModuleId;
                    module=getModuleName(moduleid);                     //Add module for audit trail entry
                }

                if (mode.equalsIgnoreCase("autoquotation")) {
                    moduleid = Constants.Acc_Customer_Quotation_ModuleId;
                    module=getModuleName(moduleid);                     //Add module for audit trail entry
                }

                if (mode.equalsIgnoreCase("autodo")) {
                    moduleid = Constants.Acc_Delivery_Order_ModuleId;
                }

                if (mode.equalsIgnoreCase("autogro")) {
                    moduleid = Constants.Acc_Goods_Receipt_ModuleId;
                }

                if (mode.equalsIgnoreCase("autosr")) {
                    moduleid = Constants.Acc_Sales_Return_ModuleId;
                }

                if (mode.equalsIgnoreCase("autopr")) {
                    moduleid = Constants.Acc_Purchase_Return_ModuleId;
                }

                if (mode.equalsIgnoreCase("autoproductid")) {
                    moduleid = Constants.Acc_Product_Master_ModuleId;
                }

                if (mode.equalsIgnoreCase("autocustomerid")) {
                    moduleid = Constants.Acc_Customer_ModuleId;
                }

                if (mode.equalsIgnoreCase("autovendorid")) {
                    moduleid = Constants.Acc_Vendor_ModuleId;
                }

                if (mode.equalsIgnoreCase("autosalesbaddebtclaimid")) {
                    moduleid = Constants.SALES_BAD_DEBT_CLAIM_ModuleId;
                }
                if (mode.equalsIgnoreCase("autosalesbaddebtrecoverid")) {
                    moduleid = Constants.SALES_BAD_DEBT_RECOVER_ModuleId;
                }
                if (mode.equalsIgnoreCase("autopurchasebaddebtclaimid")) {
                    moduleid = Constants.PURCHASE_BAD_DEBT_CLAIM_ModuleId;
                }
                if (mode.equalsIgnoreCase("autopurchasebaddebtrecoverid")) {
                    moduleid = Constants.PURCHASE_BAD_DEBT_RECOVER_ModuleId;
                }
                if (mode.equalsIgnoreCase("autobuildassembly")) {
                    moduleid = Constants.Acc_Build_Assembly_Product_ModuleId;
                }
                if (mode.equalsIgnoreCase("autoassetgroup")) {
                    moduleid = Constants.Acc_FixedAssets_AssetsGroups_ModuleId;
                }
                if (mode.equalsIgnoreCase("autounbuildassembly")) {
                    moduleid = Constants.Acc_Unbuild_Assembly_Product_ModuleId;
                }
                if (mode.equalsIgnoreCase("autoreconcilenumber")) { //Reconcile Sequence Format
                    moduleid = Constants.Acc_ReconcileNumber_ModuleId;
                }
                if (mode.equalsIgnoreCase("autounreconcilenumber")) {   //Unreconcile Sequence Format
                    moduleid = Constants.Acc_UnReconcileNumber_ModuleId;
                }
                if (mode.equalsIgnoreCase("autoloanrefnumber")) {
                    moduleid = Constants.Acc_Loan_Management_ModuleId;
                }
                if (mode.equalsIgnoreCase("automachineid")) {
                    moduleid = Constants.MRP_Machine_Management_ModuleId;
                }
                if (mode.equalsIgnoreCase("autolabour")) {
                    moduleid = Constants.Labour_Master;
                }
                if (mode.equalsIgnoreCase("automrpcontract")) {
                    moduleid = Constants.MRP_Contract;
                }
                if (mode.equalsIgnoreCase("autoroutecode")) {
                    moduleid = Constants.MRP_RouteCode;
                }
                if (mode.equalsIgnoreCase("autojobwork")) {
                    moduleid = Constants.MRP_JOB_WORK_MODULEID;
                }
                if (mode.equalsIgnoreCase("autoworkcentre")) {
                    moduleid = Constants.MRP_WORK_CENTRE_MODULEID;
                }
                if (mode.equalsIgnoreCase("autoworkorder")) {
                    moduleid = Constants.MRP_WORK_ORDER_MODULEID;
                }
                if (mode.equalsIgnoreCase("autoRG23EntryNumber")) {
                    moduleid = Constants.Dealer_Excise_RG23DEntry_No;
                }
                if (mode.equalsIgnoreCase("autodimensionnumber")) {
                    moduleid = Constants.Acc_Dimension_ModuleId;
                }
                /**
                 * Set module id of Packing Delivery Order
                 */
                if (mode.equalsIgnoreCase("autopackingdo")) {
                    moduleid = Constants.Acc_PackingDO_ModuleId;
                }
                /**
                 * Set module id of Shipping Delivery Order
                 */
                if (mode.equalsIgnoreCase("autoshippingdo")) {
                    moduleid = Constants.Acc_ShippingDO_ModuleId;
                }
                /**
                 * Set module id of Job Work Out Order
                 */
                if (mode.equalsIgnoreCase("autojwo")) {
                    moduleid = Constants.JOB_WORK_OUT_ORDER_MODULEID;
                }

                requestParams.put("moduleid", moduleid);
                requestParams.put("modulename", mode);
                String id = request.getParameter("id");
                JSONArray matchedtransactionArray = accCompanyPreferencesObj.checkTransactionmatchedwithSequenceFormat(requestParams);
                if (matchedtransactionArray.length() > 0 && !matchedTransactionswithsequenceformat) {
                    matchedTransactionswithsequenceformat = true;
                    totalmatchedtransactions = matchedtransactionArray.length();
                } else {
                    if (!isColumnMaxSizeMsg) {
                        isForSave=true;
                        if (StringUtil.isNullOrEmpty(id)) {
                            kwlObj = accCompanyPreferencesObj.getSequenceFormat(requestParams);
                            requestParams.put("isChecked", isChecked);
                            if (kwlObj.getRecordTotalCount() == 0) {
                                seqFormat = accCompanyPreferencesObj.saveSequenceFormat(requestParams);
                                issuccess = true;
                                sequenceFormatid = seqFormat.getID();
                            } else {
                                issuccess = true;
                                isDuplicate = true;
                            }
                        } else {
                            requestParams.put("isChecked", isChecked);
                            requestParams.put("id", id);
                            seqFormat = accCompanyPreferencesObj.updateSequenceFormat(requestParams);
                            issuccess = true;
                            sequenceFormatid = id;
                            updateflag=true;
                            //    isDuplicate = true;
                        }
                    }
                }
                formatName = format;//    ERP-18433
                if (isshowdateinprefix && showdateaftersuffix) {
                    formatName = dateFormatinPrefix + format + selectedsuffixdateformat;
                } else if (isshowdateinprefix) {
                    formatName = dateFormatinPrefix + format;
                } else if (showdateaftersuffix) {
                    formatName = format + selectedsuffixdateformat;
                }
                jobj.put("name", formatName);//Shown Default format if date before prefix selected.
            }

            if (isColumnMaxSizeMsg) {
                msg = messageSource.getMessage("acc.sequence.format.maxSizeMsg", null, RequestContextUtils.getLocale(request));   //"";
            } else {
                msg = messageSource.getMessage("acc.sequence.format.save", null, RequestContextUtils.getLocale(request));   //"Lock has been Updated successfully";
            }
            if(isForSave){
                if (updateflag) {
                    auditTrailObj.insertAuditLog(AuditAction.SEQUENCE_FORMATE_ADDED, "User " + sessionHandlerImpl.getUserFullName(request) + " has updated sequence format " + formatName + " for " + module, request, "" + moduleid);//    ERP-18433
                } else {
                    auditTrailObj.insertAuditLog(AuditAction.SEQUENCE_FORMATE_ADDED, "User " + sessionHandlerImpl.getUserFullName(request) + " has added sequence format " + formatName + " for " + module, request, "" + moduleid);
                }
            }
            
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("isduplicate", isDuplicate);
                jobj.put(Constants.moduleid, moduleid);
                jobj.put(Constants.SEQUENCEFORMATID, sequenceFormatid);
                jobj.put("matchedTransactionswithsequenceformat", matchedTransactionswithsequenceformat);
                jobj.put("totalmatchedTransactions",totalmatchedtransactions );
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView deleteChequeSequenceFormat(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CAP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {

            msg = messageSource.getMessage("acc.sequence.format.save", null, RequestContextUtils.getLocale(request));   //"Lock has been Updated successfully";

            deleteChequeSequenceFormat(request);
            txnManager.commit(status);
            issuccess = true;
//            jobj.put("updatedSequenceFormat", sequenceFormatStr);            
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deleteChequeSequenceFormat(HttpServletRequest request) throws SessionExpiredException {
        try {
            String deleteChequeSequenceFormat = "";
            String id = request.getParameter("id");
            String companyId = sessionHandlerImpl.getCompanyid(request);

            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            dataMap.put("id", id);

            int delRows = accCompanyPreferencesObj.deleteChequeSequenceFormat(id, companyId);
        } catch (ServiceException ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Added @Transactional instead of txnmanager - ERP-32983. Moved code
     * containing business logic to the AccCompanyPreferencesServiceImpl No any
     * changes other than this has been done in code.
     * @param request
     * @param response
     * @return
     */
    public ModelAndView deleteSequenceFormat(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false; 
        try {
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            jobj=accCompanyPreferencesService.deleteSequenceFormat(requestJobj);
            issuccess = jobj.optBoolean(Constants.RES_success, false);
            msg = jobj.optString(Constants.RES_msg);
        } catch (SessionExpiredException ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try{
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch(JSONException ex){
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        } 
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getChequeSequenceFormatStore(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            paramJobj.put("isEdit", request.getParameter("isEdit"));
            paramJobj.put("isFromPaymentModule", request.getParameter("isFromPaymentModule"));
            JSONArray retArr = getChequeSequenceFormatStore(paramJobj);
            jobj.put("data", retArr);
            jobj.put("count", retArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());

    }

    public JSONArray getChequeSequenceFormatStore(JSONObject paramJobj) throws SessionExpiredException {
        JSONArray retArr = new JSONArray();
        try {
            String companyId = paramJobj.getString(Constants.companyKey);
            String paymentMethodAccountId = paramJobj.optString("paymentMethodAccountId", "");
            boolean isAllowNA = paramJobj.optBoolean("isAllowNA", false);
            Map<String, Object> reqestParams = new HashMap<String, Object>();
            reqestParams.put("companyid", companyId);
            if (!StringUtil.isNullOrEmpty(paymentMethodAccountId)) {
                reqestParams.put("bankAccountId", paymentMethodAccountId);
            }
            boolean isEdit = paramJobj.optBoolean("isEdit", false);
            boolean isFromPaymentModule = paramJobj.optBoolean("isFromPaymentModule", false);
            if (isFromPaymentModule) {
                reqestParams.put("isChecked", true);    //to show only checked records in combo
            }

            KwlReturnObject retObj = accCompanyPreferencesObj.getChequeSequenceFormatList(reqestParams);
            List list = retObj.getEntityList();
            if (!list.isEmpty()) {
                Iterator it = list.iterator();
                while (it.hasNext()) {
                    JSONObject returnObject = new JSONObject();
                    ChequeSequenceFormat sequenceFormat = (ChequeSequenceFormat) it.next();
                    returnObject.put("id", sequenceFormat.getId());
                    returnObject.put("bankName", (sequenceFormat.getBankAccount() != null) ? sequenceFormat.getBankAccount().getName() : "");
                    returnObject.put("numberofdigit", sequenceFormat.getNumberOfDigits());
                    returnObject.put("startfrom", sequenceFormat.getStartFrom());
                    returnObject.put("showleadingzero", sequenceFormat.isShowLeadingZero());
                    returnObject.put("accid", sequenceFormat.getBankAccount().getID());
                    returnObject.put("chequeEndNumber", sequenceFormat.getChequeEndNumber());
                    returnObject.put("isChecked", sequenceFormat.isIsactivate());
                    returnObject.put("isdefault", sequenceFormat.isIsdefault());
                    returnObject.put("prefix", sequenceFormat.getPrefix());
                    returnObject.put("suffix", sequenceFormat.getSuffix());
                    returnObject.put("showdateinprefix", sequenceFormat.isDateBeforePrefix() ? "Yes" : "No");
                    returnObject.put("showdateafterprefix", sequenceFormat.isDateAfterPrefix() ? "Yes" : "No");
//                    returnObject.put("oldflag", false);
                    returnObject.put("resetcounter", sequenceFormat.isResetCounter() ? "Yes" : "No");
                    returnObject.put("selecteddateformat", sequenceFormat.getDateformatinprefix());
                    returnObject.put("selecteddateformatafterprefix", sequenceFormat.getDateformatafterprefix());
                    returnObject.put("showdateaftersuffix", sequenceFormat.isShowDateFormatAfterSuffix() ? "Yes" : "No");
                    returnObject.put("dateformataftersuffix", sequenceFormat.getDateFormatAfterSuffix() == null ? "" : sequenceFormat.getDateFormatAfterSuffix());

                    if (isFromPaymentModule && !isEdit) {
                        HashMap<String, Object> dataMap = new HashMap<>();

                        dataMap.put("companyId", companyId);
                        dataMap.put("bankAccountId", sequenceFormat.getBankAccount().getID());
                        dataMap.put("sequenceformatid", sequenceFormat.getId());
                        dataMap.put("chequeEndNumber", sequenceFormat.getChequeEndNumber());

                        boolean isChequeSequenceFormatShouldLoad = isChequeSequenceFormatShouldLoad(dataMap);

                        /* Check is not allowed for Edit case as we show used sequence Format */
                        if (!isChequeSequenceFormatShouldLoad) {
                            continue;
                        }
                    }

                    // getting sequence format for per bank account
//                    String seqFormat = getChequeSequenceFormat(sequenceFormat);
                    returnObject.put("value", sequenceFormat.getName());
                    retArr.put(returnObject);
                }
            }
            if (isAllowNA) {
                JSONObject jNA = new JSONObject();
                jNA.put("id", "NA");
                jNA.put("value", "NA");
                retArr.put(jNA);
            }
        } catch (JSONException ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retArr;
    }

    private boolean isChequeSequenceFormatShouldLoad(HashMap requestParams) {
        boolean isChequeSequenceFormatShouldLoad = false;
        /* getting sequence format for per bank account */

        try {
            HashMap<String, Object> dataMap = new HashMap<>();
            String companyId = (String) requestParams.get("companyId");
            String bankAccountId = (String) requestParams.get("bankAccountId");
            String sequenceformatid = (String) requestParams.get("sequenceformatid");
            BigInteger chequeEndNumber = (BigInteger) requestParams.get("chequeEndNumber");

            dataMap.put("companyId", companyId);
            dataMap.put("bankAccountId", bankAccountId);
            dataMap.put("sequenceformatid", sequenceformatid);

            /* Fetching Max Sequence no generated from this particular Sequence Format ID, bank account wise*/
            KwlReturnObject cqresult = accCompanyPreferencesObj.getMaxChequeSequenceNumber(dataMap);
            List returnList = cqresult.getEntityList();
            BigInteger maxSequenceNumber = new BigInteger("0");
            if (!returnList.isEmpty()) {
                if (returnList.get(0) != null) {
                    maxSequenceNumber = (BigInteger) returnList.get(0);
                }
            }

            /* If Max Sequence no generated from this particular Sequence Format ID is  less than Cheque End number
             Then We show this Sequence Format in Cheque Sequence Format Combo of Payment
             */
            if (chequeEndNumber.longValue() > maxSequenceNumber.longValue()) {
                isChequeSequenceFormatShouldLoad = true;
            }

        } catch (ServiceException ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return isChequeSequenceFormatShouldLoad;
    }

    private String getChequeSequenceFormat(ChequeSequenceFormat sequenceFormat) {
        String format = "";
        if (sequenceFormat != null) {
            int numberofdigit = sequenceFormat.getNumberOfDigits();
            for (int i = 0; i < numberofdigit; i++) {
                format += "0";
            }
        }
        return format;
    }

    public ModelAndView getSequenceFormatStore(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String mode = request.getParameter("mode");
            String isAllowNAStr = request.getParameter("isAllowNA");
            String isEditStr = request.getParameter("isEdit");
            CompanyAccountPreferences preferences;
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("id", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject result = accCompanyPreferencesObj.getCompanyPreferences(requestParams);
            preferences = (CompanyAccountPreferences) result.getEntityList().get(0);

            JSONArray jArr = new JSONArray();

            JSONObject jNA = new JSONObject();

            jNA.put("id", "NA");
            jNA.put("value", "NA");
            jNA.put("oldflag", false);
            boolean isAllNA = true;
            boolean isEdit = false;
            if (!StringUtil.isNullOrEmpty(isAllowNAStr)) {
                isAllNA = Boolean.parseBoolean(isAllowNAStr);
            }
            if (!StringUtil.isNullOrEmpty(isEditStr)) {
                isEdit = Boolean.parseBoolean(isEditStr);
            }

            String formatArr[] = null;
            if (!StringUtil.isNullOrEmpty(mode)) {
                if (mode.equalsIgnoreCase("autojournalentry")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getJournalEntryNumberFormat())) {
                        formatArr = preferences.getJournalEntryNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autoinvoice")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getInvoiceNumberFormat())) {
                        formatArr = preferences.getInvoiceNumberFormat().split(",");

                    }
                }

                if (mode.equalsIgnoreCase("autocreditmemo")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getCreditNoteNumberFormat())) {
                        formatArr = preferences.getCreditNoteNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autoreceipt")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getReceiptNumberFormat())) {
                        formatArr = preferences.getReceiptNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autogoodsreceipt")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getGoodsReceiptNumberFormat())) {
                        formatArr = preferences.getGoodsReceiptNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autodebitnote")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getDebitNoteNumberFormat())) {
                        formatArr = preferences.getDebitNoteNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autopayment")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getPaymentNumberFormat())) {
                        formatArr = preferences.getPaymentNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autoso")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getSalesOrderNumberFormat())) {
                        formatArr = preferences.getSalesOrderNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autopo")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getPurchaseOrderNumberFormat())) {
                        formatArr = preferences.getPurchaseOrderNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autocashsales")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getCashSaleNumberFormat())) {
                        formatArr = preferences.getCashSaleNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autocashpurchase")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getCashPurchaseNumberFormat())) {
                        formatArr = preferences.getCashPurchaseNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autobillinginvoice")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getBillingInvoiceNumberFormat())) {
                        formatArr = preferences.getBillingInvoiceNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autobillingreceipt")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getBillingReceiptNumberFormat())) {
                        formatArr = preferences.getBillingReceiptNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autobillingcashsales")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getBillingCashSaleNumberFormat())) {
                        formatArr = preferences.getBillingCashSaleNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autobillinggoodsreceipt")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getBillingGoodsReceiptNumberFormat())) {
                        formatArr = preferences.getBillingGoodsReceiptNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autobillingdebitnote")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getBillingDebitNoteNumberFormat())) {
                        formatArr = preferences.getBillingDebitNoteNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autobillingcreditmemo")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getBillingCreditNoteNumberFormat())) {
                        formatArr = preferences.getBillingCreditNoteNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autobillingpayment")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getBillingPaymentNumberFormat())) {
                        formatArr = preferences.getBillingPaymentNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autobillingso")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getBillingSalesOrderNumberFormat())) {
                        formatArr = preferences.getBillingSalesOrderNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autobillingpo")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getBillingPurchaseOrderNumberFormat())) {
                        formatArr = preferences.getBillingPurchaseOrderNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autobillingcashpurchase")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getBillingCashPurchaseNumberFormat())) {
                        formatArr = preferences.getBillingCashPurchaseNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autorequisition")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getRequisitionNumberFormat())) {
                        formatArr = preferences.getRequisitionNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autorequestforquotation")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getRfqNumberFormat())) {
                        formatArr = preferences.getRfqNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autovenquotation")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getVenQuotationNumberFormat())) {
                        formatArr = preferences.getVenQuotationNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autoquotation")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getQuotationNumberFormat())) {
                        formatArr = preferences.getQuotationNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autodo")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getDeliveryOrderNumberFormat())) {
                        formatArr = preferences.getDeliveryOrderNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autogro")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getGoodsReceiptOrderNumberFormat())) {
                        formatArr = preferences.getGoodsReceiptOrderNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autosr")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getSalesReturnNumberFormat())) {
                        formatArr = preferences.getSalesReturnNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autopr")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getPurchaseReturnNumberFormat())) {
                        formatArr = preferences.getPurchaseReturnNumberFormat().split(",");
                    }
                }

                if (mode.equalsIgnoreCase("autoproductid")) {
                    if (!StringUtil.isNullOrEmpty(preferences.getProductidNumberFormat())) {
                        formatArr = preferences.getProductidNumberFormat().split(",");
                    }
                }

//                if(mode.equalsIgnoreCase("autocustomercode")){
//                    if(!StringUtil.isNullOrEmpty(preferences.getCustomerCodeFormat())){
//                        formatArr = preferences.getCustomerCodeFormat().split(",");
//                    }
//                }
//                
//                if(mode.equalsIgnoreCase("autovendorcode")){
//                    if(!StringUtil.isNullOrEmpty(preferences.getVendorCodeFormat())){
//                        formatArr = preferences.getVendorCodeFormat().split(",");
//                    }
//                }
                if (formatArr != null) {

                    for (String format : formatArr) {
                        JSONObject j = new JSONObject();
                        if (!StringUtil.isNullOrEmpty(format)) {
                            j.put("id", format);
                            j.put("value", format);
                            j.put("oldflag", true);
                            jArr.put(j);
                        }

                    }

                }

                Map<String, Object> filterParams = new HashMap<String, Object>();
                filterParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
                filterParams.put("modulename", mode);
                if (!StringUtil.isNullOrEmpty(request.getParameter("masterid")) && !request.getParameter("masterid").equals("all")) {
                    filterParams.put("masterid", request.getParameter("masterid"));
                }
                if (!isEdit) {
                    filterParams.put("isChecked", true);    //to show only checked records in combo
                }
                Map<String, Object> seqNumberMap = new HashMap<>();
                KwlReturnObject fieldparam = null;
                KwlReturnObject result1 = accCompanyPreferencesObj.getSequenceFormat(filterParams);
                Iterator itr = result1.getEntityList().iterator();
                while (itr.hasNext()) {
                    SequenceFormat seqFormat = (SequenceFormat) itr.next();
                    JSONObject j = new JSONObject();
                    j.put("id", seqFormat.getID());
                    String formatName = seqFormat.getName();
                    if (seqFormat.isDateBeforePrefix() && seqFormat.isShowDateFormatAfterSuffix()) {
                        formatName = seqFormat.getDateformatinprefix() + seqFormat.getName() + seqFormat.getDateFormatAfterSuffix();
                    } else if (seqFormat.isDateBeforePrefix()) {
                        formatName = seqFormat.getDateformatinprefix() + seqFormat.getName();
                    } else if (seqFormat.isShowDateFormatAfterSuffix()) {
                        formatName = seqFormat.getName() + seqFormat.getDateFormatAfterSuffix();
                    }
                    j.put("value", formatName);
                    j.put("prefix", seqFormat.getPrefix());
                    j.put("suffix", seqFormat.getSuffix());
                    j.put("numberofdigit", seqFormat.getNumberofdigit());
                    j.put("startfrom", seqFormat.getStartfrom());
                    j.put("showleadingzero", seqFormat.isShowleadingzero() ? "Yes" : "No");
                    j.put("showdateinprefix", seqFormat.isDateBeforePrefix() ? "Yes" : "No");
                    j.put("showdateafterprefix", seqFormat.isDateAfterPrefix() ? "Yes" : "No");
                    j.put("oldflag", false);
                    j.put("isdefaultformat", seqFormat.isIsdefaultformat() ? "Yes" : "No");
                    j.put("resetcounter", seqFormat.isResetCounter() ? "Yes" : "No");
                    j.put("selecteddateformat", seqFormat.getDateformatinprefix());
                    j.put("selecteddateformatafterprefix", seqFormat.getDateformatafterprefix());
                    j.put("isChecked", seqFormat.isIsactivate());
                    j.put("showdateaftersuffix", seqFormat.isShowDateFormatAfterSuffix() ? "Yes" : "No");
                    j.put("dateformataftersuffix", seqFormat.getDateFormatAfterSuffix() == null ? "" : seqFormat.getDateFormatAfterSuffix());
                    if (filterParams.containsKey("masterid")) {
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(filterParams.get("masterid").toString(), StaticValues.AUTONUM_DIMENSION, seqFormat.getID(), false, Calendar.getInstance().getTime());
                        j.put("nextno", seqNumberMap.get(Constants.AUTO_ENTRYNUMBER).toString());
                    }
                    if (seqFormat.getCustom() == null) {
                        j.put("custom", "");
                        j.put("customid", "");
                    } else {
                        fieldparam = accountingHandlerDAOobj.getObject(FieldParams.class.getName(), seqFormat.getCustom());
                        FieldParams fp = (FieldParams) fieldparam.getEntityList().get(0);
                        j.put("custom", fp.getFieldlabel());
                        j.put("customid", fp.getId());
                    }
                    jArr.put(j);
                }
            }
            if (isAllNA) {
                jArr.put(jNA);
            }
            jobj.put("data", jArr);
            jobj.put("count", jArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public int getAutoGenNumberStartFrom(int from, String companyId) {
        int startfrom = 1;
        try {
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("id", companyId);
            KwlReturnObject result = accCompanyPreferencesObj.getCompanyPreferences(requestParams);
            CompanyAccountPreferences pref = (CompanyAccountPreferences) result.getEntityList().get(0);
            switch (from) {
                case StaticValues.AUTONUM_JOURNALENTRY:
                    startfrom = pref.getJournalEntryNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_SALESORDER:
                    startfrom = pref.getSalesOrderNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_INVOICE:
                    startfrom = pref.getInvoiceNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_CASHSALE:
                    startfrom = pref.getCashSaleNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_CREDITNOTE:
                    startfrom = pref.getCreditNoteNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_RECEIPT:
                    startfrom = pref.getReceiptNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_PURCHASEORDER:
                    startfrom = pref.getPurchaseOrderNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_GOODSRECEIPT:
                    startfrom = pref.getGoodsReceiptNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_CASHPURCHASE:
                    startfrom = pref.getCashPurchaseNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_DEBITNOTE:
                    startfrom = pref.getDebitNoteNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_PAYMENT:
                    startfrom = pref.getPaymentNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_BILLINGINVOICE:
                    startfrom = pref.getBillingInvoiceNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_BILLINGRECEIPT:
                    startfrom = pref.getBillingReceiptNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_BILLINGCASHSALE:
                    startfrom = pref.getBillingCashSaleNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_BILLINGGOODSRECEIPT:
                    startfrom = pref.getBillingGoodsReceiptNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_BILLINGPAYMENT:
                    startfrom = pref.getBillingPaymentNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_BILLINGCASHPURCHASE:
                    startfrom = pref.getBillingCashPurchaseNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_BILLINGPURCHASEORDER:
                    startfrom = pref.getBillingPurchaseOrderNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_BILLINGSALESORDER:
                    startfrom = pref.getBillingSalesOrderNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_BILLINGDEBITNOTE:
                    startfrom = pref.getBillingDebitNoteNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_BILLINGCREDITNOTE:
                    startfrom = pref.getBillingCreditNoteNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_QUOTATION:
                    startfrom = pref.getQuotationNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_VENQUOTATION:
                    startfrom = pref.getVenQuotationNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_REQUISITION:
                    startfrom = pref.getRequisitionNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_RFQ:
                    startfrom = pref.getRfqNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_PRODUCTID:
                    startfrom = pref.getProductidNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_DELIVERYORDER:
                    startfrom = pref.getDeliveryOrderNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_GOODSRECEIPTORDER:
                    startfrom = pref.getGoodsReceiptOrderNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_SALESRETURN:
                    startfrom = pref.getSalesReturnNumberFormatStartFrom();
                    break;
                case StaticValues.AUTONUM_PURCHASERETURN:
                    startfrom = pref.getPurchaseReturnNumberFormatStartFrom();
                    break;
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return startfrom;
    }

    public ModelAndView getAutoGenNumberStartFromValue(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String mode = request.getParameter("mode");
            int autogenStartFrom = 1;
            CompanyAccountPreferences preferences;
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("id", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject result = accCompanyPreferencesObj.getCompanyPreferences(requestParams);
            preferences = (CompanyAccountPreferences) result.getEntityList().get(0);

            if (!StringUtil.isNullOrEmpty(mode)) {
                if (mode.equalsIgnoreCase("autojournalentry")) {
                    autogenStartFrom = preferences.getJournalEntryNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autoinvoice")) {
                    autogenStartFrom = preferences.getInvoiceNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autocreditmemo")) {
                    autogenStartFrom = preferences.getCreditNoteNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autoreceipt")) {
                    autogenStartFrom = preferences.getReceiptNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autogoodsreceipt")) {
                    autogenStartFrom = preferences.getGoodsReceiptNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autodebitnote")) {
                    autogenStartFrom = preferences.getDebitNoteNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autopayment")) {
                    autogenStartFrom = preferences.getPaymentNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autoso")) {
                    autogenStartFrom = preferences.getSalesOrderNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autopo")) {
                    autogenStartFrom = preferences.getPurchaseOrderNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autocashsales")) {
                    autogenStartFrom = preferences.getCashSaleNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autocashpurchase")) {
                    autogenStartFrom = preferences.getCashPurchaseNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autobillinginvoice")) {
                    autogenStartFrom = preferences.getBillingInvoiceNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autobillingreceipt")) {
                    autogenStartFrom = preferences.getBillingReceiptNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autobillingcashsales")) {
                    autogenStartFrom = preferences.getBillingCashSaleNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autobillinggoodsreceipt")) {
                    autogenStartFrom = preferences.getBillingGoodsReceiptNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autobillingdebitnote")) {
                    autogenStartFrom = preferences.getBillingDebitNoteNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autobillingcreditmemo")) {
                    autogenStartFrom = preferences.getBillingCreditNoteNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autobillingpayment")) {
                    autogenStartFrom = preferences.getBillingPaymentNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autobillingso")) {
                    autogenStartFrom = preferences.getBillingSalesOrderNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autobillingpo")) {
                    autogenStartFrom = preferences.getBillingPurchaseOrderNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autobillingcashpurchase")) {
                    autogenStartFrom = preferences.getBillingCashPurchaseNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autorequisition")) {
                    autogenStartFrom = preferences.getRequisitionNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autorequestforquotation")) {
                    autogenStartFrom = preferences.getRfqNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autovenquotation")) {
                    autogenStartFrom = preferences.getVenQuotationNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autoquotation")) {
                    autogenStartFrom = preferences.getQuotationNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autodo")) {
                    autogenStartFrom = preferences.getDeliveryOrderNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autogro")) {
                    autogenStartFrom = preferences.getGoodsReceiptOrderNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autosr")) {
                    autogenStartFrom = preferences.getSalesReturnNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autopr")) {
                    autogenStartFrom = preferences.getPurchaseReturnNumberFormatStartFrom();
                }

                if (mode.equalsIgnoreCase("autoproductid")) {
                    autogenStartFrom = preferences.getProductidNumberFormatStartFrom();
                }
            }
            jobj.put("autogenNumStartFrom", autogenStartFrom);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCurrencyController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getYearLock(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        int finanyear = 0, bookingyear = 0, backfiveyears = 0;
        KwlReturnObject result;
        try {
            Calendar FinYearCal = Calendar.getInstance();
            Calendar BookYearCal = Calendar.getInstance();
            SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getDateOnlyFormat();
            if (request.getParameter("CurrentFinancialYear") != null) {
                FinYearCal.setTime(formatter.parse(request.getParameter("CurrentFinancialYear")));
                finanyear = FinYearCal.get(Calendar.YEAR);
            }
            Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("CurrentFinancialYear", finanyear);
            requestParams.put("CurrentFinanYear", formatter.parse(request.getParameter("CurrentFinancialYear")));
            requestParams.put("CurrentBookingYear", formatter.parse(request.getParameter("CurrentBookingYear")));
            Calendar date = Calendar.getInstance();
            Date currentyear = authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(date.getTime()));
            requestParams.put("CurrentYear", currentyear);
            int year = date.get(Calendar.YEAR);
            if (request.getParameter("CurrentBookingYear") != null) {
                BookYearCal.setTime(formatter.parse(request.getParameter("CurrentBookingYear")));
                bookingyear = BookYearCal.get(Calendar.YEAR);
                backfiveyears = bookingyear - 5;
                requestParams.put("Backfiveyears", backfiveyears);
            }
            requestParams.put("CurrentServerYear", year);
            if (request.getParameter("CurrentFinancialYear") == null) {
                result = accCompanyPreferencesObj.getYearLock(requestParams);

            } else {//checking the flag to check whether to insert the year or check lock period if financial year is changed backwards  

                for (int i = backfiveyears; i < year + 5; i++) {//else part to insert the yearid in year lock if not present put entry in table year lock
                    requestParams.put("yearid", i);
                    accCompanyPreferencesObj.checkYearLockpresentindb(requestParams);//getting all the details of the grid values
                }
                result = accCompanyPreferencesObj.getYearLockforPreferences(requestParams);//getting all the details of the grid values
                if (result != null && result.getEntityList().size() != 0) {
                    JSONArray DataJArr = CompanyPreferencesCMN.getYearLockJson(accCompanyPreferencesObj, requestParams, result.getEntityList());
                    jobj.put("data", DataJArr);
                    jobj.put("count", result.getRecordTotalCount());
                    issuccess = true;
                } else {
                    JSONArray DataJArr = CompanyPreferencesCMN.getYearLockJson(accCompanyPreferencesObj, requestParams, result.getEntityList());
                    jobj.put("data", DataJArr);
                    jobj.put("count", 0);
                    issuccess = true;
                }
            }
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /*
     * This function returns Company level QA Approval flow from Inventory
     * Management System. @param companyid @param userid @return String @throws
     * ServiceException
     *
     */
    private boolean getQAApprovalStatus(String companyid, String userid) {
        boolean isQAApprovalFlow = false;
        //Session session=null;
        try {
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", userid);
            userData.put("companyid", companyid);

            //session = HibernateUtil.getCurrentSession();
            String url = this.getServletContext().getInitParameter("inventoryURL");
            JSONObject resObj = apiCallHandlerService.callApp(url, userData, companyid, "20");
            if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                if (!resObj.isNull("isIncludeQAapprovalFlow") && resObj.has("isIncludeQAapprovalFlow")) {
                    isQAApprovalFlow = resObj.getBoolean("isIncludeQAapprovalFlow");
                }
            }

        } catch (Exception ex) {
            Logger.getLogger(CompanyPreferencesCMN.class.getName()).log(Level.SEVERE, "CompanyPreferencesCMN.getQAApprovalStatus", ex);
        }
//        finally {
//            HibernateUtil.closeSession(session);
//        }
        return isQAApprovalFlow;
    }
    //Neeraj Dwivedi-Checking for any transaction in CompanyPreferences when the financial year is changed to value less than the saved actual value

    public ModelAndView checktransactionforbookbeginningdate(HttpServletRequest request, HttpServletResponse response) throws AccountingException, ServiceException, SessionExpiredException, ParseException, JSONException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        Date transactiondate = null, originalbookbdate = null;
        try {
            SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getGlobalDateFormat();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            transactiondate = formatter.parse(request.getParameter("transactiondate"));
            originalbookbdate = formatter.parse(request.getParameter("originalbookbdate"));
            HashMap<String, Object> erdMap = new HashMap<String, Object>();
            erdMap.put("transactiondate", transactiondate);
            erdMap.put("originalbookbdate", originalbookbdate);
            erdMap.put("companyid", companyid);
            KwlReturnObject result = accCompanyPreferencesObj.checktransactionforbookbeginningdate(erdMap);

            List list = result.getEntityList();
            if (list.size() > 0) {
                issuccess = false;
                throw new AccountingException("You cannot change Book Beginning Date as transaction has already been made.");
            } else {
                issuccess = true;
                msg = "";
            }
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    //Neeraj Dwivedi--checking the lock period for previous five years till current year while changing financial year or book beginning date 
    public ModelAndView checkpreviousyearlock(HttpServletRequest request, HttpServletResponse response) throws AccountingException, ServiceException, SessionExpiredException, ParseException, JSONException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        Date finanDate = null, bookdate = null;
        Map<String, Object> requestParams = new HashMap<String, Object>();
        JSONArray jArr = new JSONArray();
        List<YearLock> list = new ArrayList<YearLock>();
        Calendar FinYearCal = Calendar.getInstance();
        Calendar BookYearCal = Calendar.getInstance();
        int finanyear = 0, bookingyear = 0, backfiveyears = 0;

        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Map<String, Object> filterParams = new HashMap<String, Object>();
            filterParams.put("id", companyid);
            KwlReturnObject kresult = accCompanyPreferencesObj.getCompanyPreferences(filterParams);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) kresult.getEntityList().get(0);

            if (preferences.getFinancialYearFrom() != null) {
                finanDate = preferences.getFirstFinancialYearFrom() != null ? preferences.getFirstFinancialYearFrom() : preferences.getFinancialYearFrom();
                bookdate = preferences.getBookBeginningFrom();
                BookYearCal.setTime(bookdate);
                bookingyear = BookYearCal.get(Calendar.YEAR);
                backfiveyears = bookingyear - 5;
                requestParams.put("Backfiveyears", backfiveyears);
            }
            FinYearCal.setTime(finanDate);
            int financialyear = FinYearCal.get(Calendar.YEAR);
            requestParams.put("yearid", financialyear);

            Calendar caldate = Calendar.getInstance();
            Date currentyear = authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(caldate.getTime()));
            int year = caldate.get(Calendar.YEAR);
            requestParams.put("companyid", companyid);
            requestParams.put("CurrentFinancialYear", financialyear);
            requestParams.put("CurrentFinanYear", finanDate);
            requestParams.put("CurrentBookingYear", bookingyear);
            requestParams.put("CurrentYear", currentyear);
            requestParams.put("CurrentServerYear", year);
            requestParams.put("previousfiveyears", "true");
            KwlReturnObject yearresult = accCompanyPreferencesObj.getYearLockforPreferences(requestParams);
            if (yearresult != null && yearresult.getEntityList().size() != 0) {
                list = yearresult.getEntityList();
            }

            jArr = CompanyPreferencesCMN.getYearLockJson(accCompanyPreferencesObj, requestParams, list);

            if (jArr.length() > 0) {
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jsonobj = jArr.getJSONObject(i);

                    String islockboolean = jsonobj.getString("islock");
                    if (islockboolean.equals("true")) {
                        throw new AccountingException("Some financial years have been locked. Please unlock to proceed.");
                    } else {
                        continue;
                    }
                }
            }

        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView setUserActiveDays(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String users = request.getParameter("users");
            String[] usersArr = users.split(",");
            JSONArray jArr = new JSONArray(request.getParameter("detail"));
            boolean isAllUser = false;

            for (int userCnt = 0; userCnt < usersArr.length; userCnt++) {
                String userID = usersArr[userCnt];
                if (!StringUtil.isNullOrEmpty(userID)) {
                    if (userID.equalsIgnoreCase("All")) {
                        isAllUser = true;
                    }
                    accCompanyPreferencesObj.deleteUserActiveDaysDetails(userID, isAllUser, companyid);

                    for (int moduleCnt = 0; moduleCnt < jArr.length() - 1; moduleCnt++) {
                        JSONObject jArrObj = jArr.getJSONObject(moduleCnt);
                        int days = jArrObj.getInt("days");
                        int moduleid = jArrObj.getInt("moduleid");

                        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
                        requestParams.put("userID", userID);
                        requestParams.put("activeDays", days);
                        requestParams.put("moduleID", moduleid);
                        requestParams.put("companyID", companyid);
                        requestParams.put("isAllUser", isAllUser);

                        accCompanyPreferencesObj.setUserActiveDays(requestParams);
                    }
                }

            }
            issuccess = true;
            auditTrailObj.insertAuditLog(AuditAction.ACTIVE_DAYS_PERIOD, "User " + sessionHandlerImpl.getUserFullName(request) + " has updated Active Days Period", request, "254");
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getUserActiveDaysDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("userID", request.getParameter("userID"));
            requestParams.put("companyID", sessionHandlerImpl.getCompanyid(request));

            KwlReturnObject result = accCompanyPreferencesObj.getUserActiveDaysDetails(requestParams);
            List list = result.getEntityList();
            JSONArray DataJArr = getUserActiveDaysDetailsJson(request, list);
            int count = result.getRecordTotalCount();

            jobj.put("data", DataJArr);
            jobj.put("totalCount", count);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getUserActiveDaysDetailsJson(HttpServletRequest request, List<UserActiveDaysDetails> list) throws JSONException, ServiceException {
        JSONArray jArr = new JSONArray();

        for (UserActiveDaysDetails uadDetails : list) {
            JSONObject obj = new JSONObject();
            obj.put("userID", uadDetails.getUser().getUserID());
            obj.put("moduleid", uadDetails.getModuleID());
            obj.put("days", uadDetails.getActiveDays());
            jArr.put(obj);
        }
        return jArr;
    }

    public ModelAndView saveBudgetSetting(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        KwlReturnObject kwlObj;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CAP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            jobj = saveBudgetSetting(request);
            msg = messageSource.getMessage("acc.msg.budgetsettingsaved", null, RequestContextUtils.getLocale(request));  //"Budget Setting has been saved successfully";
            issuccess = true;
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());

    }

    public JSONObject saveBudgetSetting(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        try {
            boolean budgetSetingActivation = false;
            int budgetingType = 0;
            int frequencyType = 0;
            int warnblockType = 0;

            if (!StringUtil.isNullObject(request.getParameter("activatebudgetingforPR"))) {
                budgetSetingActivation = Boolean.parseBoolean(request.getParameter("activatebudgetingforPR"));
            }

            if (!StringUtil.isNullObject(request.getParameter("budgetType"))) {
                budgetingType = Integer.parseInt(request.getParameter("budgetType"));
            }
            if (!StringUtil.isNullObject(request.getParameter("budgetFreqType"))) {
                frequencyType = Integer.parseInt(request.getParameter("budgetFreqType"));
            }
            if (!StringUtil.isNullObject(request.getParameter("budgetwarnblock"))) {
                warnblockType = Integer.parseInt(request.getParameter("budgetwarnblock"));
            }
            String companyId = sessionHandlerImpl.getCompanyid(request);

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("company", companyId);
            requestParams.put("activatebudgetingforPR", budgetSetingActivation);
            requestParams.put("budgetType", budgetingType);
            requestParams.put("budgetFreqType", frequencyType);
            requestParams.put("budgetwarnblock", warnblockType);

            KwlReturnObject prefRes = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);

            ExtraCompanyPreferences companyPreferences = null;

            if (prefRes != null && !prefRes.getEntityList().isEmpty()) {
                companyPreferences = (ExtraCompanyPreferences) prefRes.getEntityList().get(0);
                requestParams.put("id", companyId);
                companyPreferences = accCompanyPreferencesObj.updateExtraCompanyPreferences(requestParams);
            } else {
                companyPreferences = accCompanyPreferencesObj.saveExtraCompanyPreferences(requestParams);
            }

            jobj.put("activatebudgetingforPR", companyPreferences.isActivatebudgetingforPR());
            jobj.put("budgetType", companyPreferences.getBudgetType());
            jobj.put("budgetFreqType", companyPreferences.getBudgetFreqType());
            jobj.put("budgetwarnblock", companyPreferences.getBudgetwarnblock());

        } catch (JSONException ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveBudgetSetting : " + ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveBudgetSetting : " + ex.getMessage(), ex);
        }
        return jobj;
    }

    public ModelAndView checkOpeningTransactionsForFirstFinancialYearDate(HttpServletRequest request, HttpServletResponse response) throws AccountingException, ServiceException, SessionExpiredException, ParseException, JSONException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        Date transactiondate = null;
        try {
            SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getGlobalDateFormat();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            transactiondate = formatter.parse(request.getParameter("transactiondate"));
            HashMap<String, Object> reqParams = new HashMap<String, Object>();
            reqParams.put("transactiondate", transactiondate);
            reqParams.put("companyid", companyid);
            KwlReturnObject result = accCompanyPreferencesObj.checkOpeningTransactionsForFirstFinancialYearDate(reqParams);

            List list = result.getEntityList();
            if (list.size() > 0) {
                issuccess = true;
                msg = messageSource.getMessage("acc.msg.existsSomeOpeningTransactions", null, RequestContextUtils.getLocale(request));
            } else {
                issuccess = true;
                msg = "";
            }
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView updateOpeningTransactionDates(HttpServletRequest request, HttpServletResponse response) throws AccountingException, ServiceException, SessionExpiredException, ParseException, JSONException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        Date transactiondate = null, bbdate = null;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CAP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
//            SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getGlobalDateFormat();  
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String transactionDate = request.getParameter("transactiondate");
            transactiondate = df.parse(request.getParameter("transactiondate"));
            bbdate = df.parse(request.getParameter("bbdate"));
            HashMap<String, Object> reqParams = new HashMap<String, Object>();
            reqParams.put("transactiondate", transactiondate);
            reqParams.put("bbdate", bbdate);
            reqParams.put("companyid", companyid);
            reqParams.put("dateformat", df);
            JSONObject retJObj = accCompanyPreferencesObj.updateOpeningTransactionDates(reqParams);
            if (retJObj.length() > 0) {
                JSONArray jarr = retJObj.optJSONArray("openingSI");
                if (jarr != null) {
                    for (int index = 0; index < jarr.length(); index++) {
                        JSONObject jObj = jarr.optJSONObject(index);
                        String documentNo = jObj.optString("documentNo", "");
                        auditTrailObj.insertAuditLog(AuditAction.OPENING_BALANCE_UPDATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has updated a Opening Balance Sales Invoice " + documentNo + " transaction date to " + transactionDate, request, documentNo);
                    }
                }

                jarr = retJObj.optJSONArray("openingSO");
                if (jarr != null) {
                    for (int index = 0; index < jarr.length(); index++) {
                        JSONObject jObj = jarr.optJSONObject(index);
                        String documentNo = jObj.optString("documentNo", "");
                        auditTrailObj.insertAuditLog(AuditAction.OPENING_BALANCE_UPDATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has updated a Opening Balance Sales Order " + documentNo + " transaction date to " + transactionDate, request, documentNo);
                    }
                }

                jarr = retJObj.optJSONArray("openingCN");
                if (jarr != null) {
                    for (int index = 0; index < jarr.length(); index++) {
                        JSONObject jObj = jarr.optJSONObject(index);
                        String documentNo = jObj.optString("documentNo", "");
                        auditTrailObj.insertAuditLog(AuditAction.OPENING_BALANCE_UPDATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has updated a Opening Balance Credit Note " + documentNo + " transaction date to " + transactionDate, request, documentNo);
                    }
                }

                jarr = retJObj.optJSONArray("openingReceipt");
                if (jarr != null) {
                    for (int index = 0; index < jarr.length(); index++) {
                        JSONObject jObj = jarr.optJSONObject(index);
                        String documentNo = jObj.optString("documentNo", "");
                        auditTrailObj.insertAuditLog(AuditAction.OPENING_BALANCE_UPDATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has updated a Opening Balance Receipt " + documentNo + " transaction date to " + transactionDate, request, documentNo);
                    }
                }

                jarr = retJObj.optJSONArray("openingPI");
                if (jarr != null) {
                    for (int index = 0; index < jarr.length(); index++) {
                        JSONObject jObj = jarr.optJSONObject(index);
                        String documentNo = jObj.optString("documentNo", "");
                        auditTrailObj.insertAuditLog(AuditAction.OPENING_BALANCE_UPDATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has updated a Opening Balance Purchase Invoice " + documentNo + " transaction date to " + transactionDate, request, documentNo);
                    }
                }

                jarr = retJObj.optJSONArray("openingPO");
                if (jarr != null) {
                    for (int index = 0; index < jarr.length(); index++) {
                        JSONObject jObj = jarr.optJSONObject(index);
                        String documentNo = jObj.optString("documentNo", "");
                        auditTrailObj.insertAuditLog(AuditAction.OPENING_BALANCE_UPDATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has updated a Opening Balance Purchase Order " + documentNo + " transaction date to " + transactionDate, request, documentNo);
                    }
                }

                jarr = retJObj.optJSONArray("openingDN");
                if (jarr != null) {
                    for (int index = 0; index < jarr.length(); index++) {
                        JSONObject jObj = jarr.optJSONObject(index);
                        String documentNo = jObj.optString("documentNo", "");
                        auditTrailObj.insertAuditLog(AuditAction.OPENING_BALANCE_UPDATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has updated a Opening Balance Debit Note " + documentNo + " transaction date to " + transactionDate, request, documentNo);
                    }
                }

                jarr = retJObj.optJSONArray("openingPayment");
                if (jarr != null) {
                    for (int index = 0; index < jarr.length(); index++) {
                        JSONObject jObj = jarr.optJSONObject(index);
                        String documentNo = jObj.optString("documentNo", "");
                        auditTrailObj.insertAuditLog(AuditAction.OPENING_BALANCE_UPDATED, "User " + sessionHandlerImpl.getUserFullName(request) + " has updated a Opening Balance Payment " + documentNo + " transaction date to " + transactionDate, request, documentNo);
                    }
                }
            }
            issuccess = true;
            msg = messageSource.getMessage("acc.field.Openingtransactiondateshasbeenupdatedsuccessfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            issuccess = false;
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            issuccess = false;
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView addReportToWidgetView(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CAP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            addReportToWidgetView(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.widgetview.add", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (Exception ex) {
            issuccess = false;
            txnManager.rollback(status);
            msg = "Error While adding report into widget View.";
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void addReportToWidgetView(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            String reportid = request.getParameter("reportid");
            requestParams.put("reportid", reportid);
            requestParams.put("company", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject result = accCompanyPreferencesObj.addReportToWidgetView(requestParams);

        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("addReportToWidgetView : " + ex.getMessage(), ex);
        }
    }

    public ModelAndView removeReportFromWidgetView(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CAP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            removeReportFromWidgetView(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.widgetview.remove", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (Exception ex) {
            issuccess = false;
            txnManager.rollback(status);
            msg = "Error While removing report from widget View.";
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void removeReportFromWidgetView(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            String reportid = request.getParameter("reportid");
            requestParams.put("reportid", reportid);
            requestParams.put("company", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject result = accCompanyPreferencesObj.removeReportFromWidgetView(requestParams);

        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("addReportToWidgetView : " + ex.getMessage(), ex);
        }
    }

    public ModelAndView saveSMTPAuthenticationDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put("companyid", request.getParameter("companyid"));
            requestParams.put("smtppath", request.getParameter("smtppath"));
            requestParams.put("smtpport", request.getParameter("smtpport"));
            requestParams.put("smtppassword", request.getParameter("smtppassword"));
            requestParams.put("smtpusername", request.getParameter("smtpusername"));
            jobj = accCompanyPreferencesService.saveSMTPAuthenticationDetails(requestParams);
        } catch (Exception ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getSMTPAuthenticationDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            HashMap<String, Object> requestParams = new HashMap<>();
            if (!StringUtil.isNullOrEmpty(request.getParameter("companyid"))) {
                requestParams.put("companyid", request.getParameter("companyid"));
            }
            jobj = accCompanyPreferencesService.getSMTPAuthenticationDetails(requestParams);
        } catch (Exception ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * @param request
     * @param response
     * @return ModelAndView
     * @throws com.krawler.hql.accounting.AccountingException
     * @throws com.krawler.common.session.SessionExpiredException
     * @description Function to check whether any transaction is made which has
     * affected inventory/stock
     */
    public ModelAndView checkTransactionPresentForManufacturingModule(HttpServletRequest request, HttpServletResponse response) throws AccountingException, SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        boolean isPerpetualValuationMethod = false;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Map<String, Object> reqMap = new HashMap<>();
            reqMap.put("companyid", companyid);
            KwlReturnObject result = accCompanyPreferencesObj.checkTransactionsForManufacturingModule(reqMap);
            List list = result.getEntityList();
            
            if (list.size() > 0) {
                issuccess = false;
                KwlReturnObject extrapre = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid); //ERP-39340
                CompanyAccountPreferences pref = (CompanyAccountPreferences) extrapre.getEntityList().get(0);
                if (pref.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD) {
                    isPerpetualValuationMethod = true;
                }
                throw new AccountingException("You cannot activate/deactivate manufacturing module as transaction has already been made.");
            } else {
                issuccess = true;
                isPerpetualValuationMethod = false;
                msg = "";
            }
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (AccountingException ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("isPerpetualValuationMethod", isPerpetualValuationMethod);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
}
