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
package com.krawler.spring.authHandler;
import com.krawler.common.admin.*;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.spring.accounting.salesorder.accSalesOrderController;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.companyDetails.companyDetailsDAO;
import com.krawler.spring.permissionHandler.permissionHandler;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import com.krawler.spring.profileHandler.profileHandlerDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.propertyeditors.LocaleEditor;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;
import com.krawler.common.admin.AuditAction;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.esp.utils.ConfigReader;
import com.krawler.hql.accounting.AccountingException;
import com.krawler.hql.accounting.CompanyAccountPreferences;
import com.krawler.hql.accounting.PaymentMethod;
import com.krawler.spring.accounting.goodsreceipt.accGoodsReceiptDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderDAO;
import com.krawler.spring.accounting.salesorder.accSalesOrderDAO;
import com.krawler.spring.common.CommonFnControllerService;
import java.util.*;

/**
 *
 * @author Karthik
 */
public class authHandlerController extends MultiActionController {

    private authHandlerDAO authHandlerDAOObj;
    private sessionHandlerImpl sessionHandlerImplObj;
    private profileHandlerDAO profileHandlerDAOObj;
    private permissionHandlerDAO permissionHandlerDAOObj;
    private companyDetailsDAO companyDetailsDAOObj;
    private String successView;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private auditTrailDAO auditTrailObj;
    private accPaymentDAO accPaymentDAOobj;
    private accInvoiceDAO accInvoiceDAOObj;
    private CommonFnControllerService commonFnControllerService;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private accSalesOrderDAO accSalesOrderDAOobj;
    private accPurchaseOrderDAO accPurchaseOrderobj;
    private accJournalEntryDAO accJournalEntryobj;
    
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }

    public void setpermissionHandlerDAO(permissionHandlerDAO permissionHandlerDAOObj1) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj1;
    }

    public void setsessionHandlerImpl(sessionHandlerImpl sessionHandlerImplObj1) {
        this.sessionHandlerImplObj = sessionHandlerImplObj1;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setprofileHandlerDAO(profileHandlerDAO profileHandlerDAOObj1) {
        this.profileHandlerDAOObj = profileHandlerDAOObj1;
    }

    public void setcompanyDetailsDAO(companyDetailsDAO companyDetailsDAOObj1) {
        this.companyDetailsDAOObj = companyDetailsDAOObj1;
    }
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj){
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setAccPaymentDAOobj(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }

    public void setCommonFnControllerService(CommonFnControllerService commonFnControllerService) {
        this.commonFnControllerService = commonFnControllerService;
    }
    public void setAccInvoiceDAOObj(accInvoiceDAO accInvoiceDAOObj) {
        this.accInvoiceDAOObj = accInvoiceDAOObj;
    }
    public void setAccountingHandlerDAOobj(AccountingHandlerDAO accountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = accountingHandlerDAOobj;
    }

    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }

    public void setAccSalesOrderDAOobj(accSalesOrderDAO accSalesOrderDAOobj) {
        this.accSalesOrderDAOobj = accSalesOrderDAOobj;
    }
    public void setaccPurchaseOrderDAO(accPurchaseOrderDAO accPurchaseOrderobj) {
        this.accPurchaseOrderobj = accPurchaseOrderobj;
    }
    public void setAccJournalEntryobj(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }
    public ModelAndView verifyLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException {
        return verifyUserLogin(request, response, null, null, null, null);
    }
    
    public ModelAndView verifyUserLogin(HttpServletRequest request, HttpServletResponse response, String user, String pass, String login, String subdomain) throws ServletException {
        JSONObject jobj = new JSONObject();
        JSONObject rjobj = new JSONObject();
        JSONObject ujobj = new JSONObject();
        KwlReturnObject kmsg = null;
        KwlReturnObject kmsg1 = null;
        String result = "";
        String userid = "";
        String companyid = "";
        HashMap<String, Object> requestParams2 = null;
        JSONObject obj = null, jret = new JSONObject();
        boolean isvalid = false;
        boolean isValidUser = false;
        boolean isIPhoneRequest = false;
        try {
            String userSessionId = UUID.randomUUID().toString();
            if(StringUtil.isNullOrEmpty(user) && StringUtil.isNullOrEmpty(pass) && StringUtil.isNullOrEmpty(login) && StringUtil.isNullOrEmpty(subdomain)){
                user = request.getParameter("u");
                pass = request.getParameter("p");
                login = request.getParameter("blank");
                if (user == null && pass == null && request.getAttribute("user") != null && request.getAttribute("pwd") != null) {
                    user = request.getAttribute("user").toString();
                    pass = request.getAttribute("pwd").toString();
                }
                subdomain = URLUtil.getDomainName(request);
            }else{
                isIPhoneRequest = true;
            }

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("user", StringUtil.checkForNull(user));
            requestParams.put("pass", StringUtil.checkForNull(pass));
            requestParams.put("subdomain", StringUtil.checkForNull(subdomain));
            if (!isIPhoneRequest && StringUtil.isNullOrEmpty(login)) {
                kmsg = authHandlerDAOObj.verifyLogin(requestParams);
                jobj = getVerifyLoginJson(kmsg.getEntityList(), request);
                jobj.put(Constants.userSessionId, userSessionId);
                if (jobj.has("success") && (jobj.get("success").equals(true))) {
                    obj = new JSONObject();
                    companyid = jobj.getString("companyid");
                    userid = jobj.getString("lid");
                    jobj.put("companyPreferences", "");

                    requestParams2 = new HashMap<String, Object>();
                    requestParams2.put("userid", userid);
                    kmsg = permissionHandlerDAOObj.getUserPermission(requestParams2);
                    Iterator ite = kmsg.getEntityList().iterator();
                    JSONArray jarr = new JSONArray();
                    while (ite.hasNext()) {
                        JSONObject jo = new JSONObject();
                        Object[] roww = (Object[]) ite.next();
                        jo.put(roww[0].toString(), roww[1]);
                        jarr.put(jo);
                    }
                    jobj.put("perms", jarr);

                    sessionHandlerImplObj.createUserSession(request, jobj);
                    setLocale(request, response, jobj.optString("language",null));
                    requestParams.put("userloginid", StringUtil.checkForNull(userid));
                    String logdate = authHandler.getDateFormatter(request).format(new Date());
                    Date loginDate = authHandler.getGlobalDateFormat().parse(logdate);
                    requestParams.put("lastlogindate", loginDate);
                    profileHandlerDAOObj.saveUserLogin(requestParams);
                    isvalid = true;
                } else {
                    jobj = new JSONObject();
                    jobj.put("success", false);
                    jobj.put("reason", "noaccess");
                    jobj.put("message", "Authentication failed");
                    isvalid = false;
                }

            } else {
                String username = "";
                if(StringUtil.isNullOrEmpty(user)){
                    username = request.getRemoteUser();
                }else{
                    username = user;
                }
                if (!StringUtil.isNullOrEmpty(username)) {
                    boolean toContinue = true;
                    if (sessionHandlerImplObj.validateSessionFromRequest(request, response)) {
                        String companyid_session = sessionHandlerImpl.getCompanyidFromRequest(request);
                        String subdomainFromSession = companyDetailsDAOObj.getSubDomain(companyid_session);
                        String usernameFromSession = sessionHandlerImpl.getUserNameFromRequest(request);
                        System.out.println("Is SSO MultiCompany Check- ");
                        System.out.println("Parent subdomain - "+subdomainFromSession);
                        System.out.println("Child subdomain - "+subdomain);
                        System.out.println("Parent username - "+usernameFromSession);
                        System.out.println("Child username - "+username);
                        if (username.equalsIgnoreCase(usernameFromSession) && !subdomain.equalsIgnoreCase(subdomainFromSession) 
                                && companyDetailsDAOObj.IsChildCompany(subdomainFromSession, subdomain)) {//SSO change for multi company. Allow to login if username is same and subdomain is different.
                            toContinue = true;
                        } else if (!subdomain.equalsIgnoreCase(subdomainFromSession)) {
                            result = "alreadyloggedin";
                            toContinue = false;
                        }
                    }
                    if(toContinue){
//                jbj = DBCon.AuthUser(username, subdomain);
                        requestParams = new HashMap<String, Object>();
                        requestParams.put("user", username);
                        requestParams.put("subdomain", subdomain);
                        kmsg = authHandlerDAOObj.verifyLogin(requestParams);
                        jobj = getVerifyLoginJson(kmsg.getEntityList(), request);
                    if (jobj.has("success")) {
//                    sessionbean.createUserSession(request, jbj);
                            requestParams2 = new HashMap<String, Object>();
                            requestParams2.put("userid", jobj.get("lid"));//userid
                            kmsg = permissionHandlerDAOObj.getUserPermission(requestParams2);

                            Iterator ite = kmsg.getEntityList().iterator();
                            JSONArray jarr = new JSONArray();
                            while (ite.hasNext()) {
                                JSONObject jo = new JSONObject();
                                Object[] roww = (Object[]) ite.next();
                                jo.put(roww[0].toString(), roww[1]);
                                jarr.put(jo);
                            }
                            jobj.put("perms", jarr);
                            jobj.put("companyPreferences", "");
                            jobj.put(Constants.userSessionId, userSessionId);
                            sessionHandlerImplObj.createUserSession(request, jobj);
                            profileHandlerDAOObj.saveUserLastLogin(requestParams2);
                        setLocale(request, response, jobj.optString("language",null));
                            isValidUser = true;
                        } else {
                            result = "noaccess";
                        }
                    }
                } else {
                    if (sessionHandlerImpl.isValidSession(request, response)) {
                        companyid = sessionHandlerImpl.getCompanyid(request);
                        String companyName = sessionHandlerImpl.getCompanyName(request);
                        username = sessionHandlerImpl.getUserName(request);
                        String childcompanyids = getChildCompanyids(companyid);
                        jobj.put("companyids", childcompanyids);
                        jobj.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
                        jobj.put("companyid", companyid);
                        jobj.put("company", companyName);
                        jobj.put("username", username);
                        jobj.put("subdomain", subdomain);				// subdomain for mailto support link on dashboard
                        isValidUser = true;
                    } else {
                        result = "timeout";
                    }
                }

                if (isValidUser) {
                    userid = sessionHandlerImpl.getUserid(request);
                    companyid = sessionHandlerImpl.getCompanyid(request);
                    insertLoginEntryInAudittrail(userid,request);
                    jobj.put("fullname", profileHandlerDAOObj.getUserFullName(userid));
                    jobj.put("lid", userid);
                    jobj.put("callwith", sessionHandlerImpl.getUserCallWith(request));
                    jobj.put("companyPreferences", "");
                    String childcompanyids = getChildCompanyids(companyid);
                    jobj.put("companyids", childcompanyids);
                    jobj.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
                    Company company = (Company) kwlCommonTablesDAOObj.getClassObject(Company.class.getName(), companyid);
                    Constants.InvoiceAmountDueFlag = company.isStoreinvoiceamountdue();
                    jobj.put("templateflag", company.getTemplateflag());
                    jobj.put("countryid", company.getCountry().getID());
                    jobj.put("countrycode", company.getCountry().getCountryCode());
                    jobj.put("stateid", company.getState()!=null? company.getState().getID():"");
                    jobj.put("isSelfService", company.getIsSelfService());
                    
                    boolean isUSServer = Boolean.valueOf(ConfigReader.getinstance().get("isUSServer"));
                    jobj.put("isUSServer", isUSServer);
                    
                    if (company.getReferralkey() == 1) {
                        jobj.put("defaultReferralKeyflag", true);
                    } else {
                        jobj.put("defaultReferralKeyflag", false);
                    }
                    
                    //Setting unique session id for user 
                    

                    request.getSession().setAttribute(Constants.userSessionId, userSessionId);
                    
                    String browsertz = request.getParameter("browsertz")!=null ? request.getParameter("browsertz") : "";// Store browser timezone to convert custom long value to date 
                    sessionHandlerImpl.getCompanySessionObj(request).setBrowsertz(browsertz);
                    
                    requestParams2 = new HashMap<String, Object>();
                    requestParams2.put("timezoneid", sessionHandlerImpl.getTimeZoneID(request));
                    requestParams2.put("dateformatid", sessionHandlerImpl.getDateFormatID(request));
                    requestParams2.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
                    JSONObject prefJson = new JSONObject();
                    kmsg = authHandlerDAOObj.getPreferences(requestParams2);
                    prefJson = getPreferencesJson(kmsg.getEntityList(), request);
                    jobj.put("preferences", prefJson.getJSONArray("data").get(0));
                    jobj.put("accpref", prefJson.getJSONArray("data").get(0));

                    if (!permissionHandlerDAOObj.isSuperAdmin(userid, companyid)) {
                        JSONObject permJobj = new JSONObject();
                        kmsg = permissionHandlerDAOObj.getActivityFeature();
                        permJobj = permissionHandler.getAllPermissionJson(kmsg.getEntityList(), request, kmsg.getRecordTotalCount());

                        requestParams2 = new HashMap<String, Object>();
                        requestParams2.put("userid", userid);
                        kmsg = permissionHandlerDAOObj.getUserPermission(requestParams2);
                        permJobj = permissionHandler.getRolePermissionJson(kmsg.getEntityList(), permJobj);
                        CompanyAccountPreferences companyaccPreferemce = (CompanyAccountPreferences) kwlCommonTablesDAOObj.getClassObject(CompanyAccountPreferences.class.getName(), companyid);
                        if(companyaccPreferemce!=null) {
                            jobj.put("viewDashboard", companyaccPreferemce.getviewDashboard());
                            jobj.put("theme", companyaccPreferemce.getTheme());
                        }
                        ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject(ExtraCompanyPreferences.class.getName(), companyid);
                        if (extraCompanyPreferences != null) {
                            jobj.put("DashBoardImageFlag", extraCompanyPreferences.isDashBordImageFlag());
                            jobj.put("accountpayableManagementFlag", extraCompanyPreferences.isAccountpayableManagementFlag());
                            jobj.put("securityGateEntryFlag", extraCompanyPreferences.isSecurityGateEntryFlag());
                            jobj.put("accountsreceivablesalesFlag", extraCompanyPreferences.isAccountsreceivablesalesFlag());
                            jobj.put("masterManagementFlag", extraCompanyPreferences.isMasterManagementFlag());
                            jobj.put("syncAllFromLMSFlag", extraCompanyPreferences.isLMSIntegration());
                            jobj.put("leaseManagementFlag", extraCompanyPreferences.isLeaseManagementFlag());
                            jobj.put("consignmentPurchaseManagementFlag", extraCompanyPreferences.isConsignmentPurchaseManagementFlag());
                            jobj.put("consignmentSalesManagementFlag", extraCompanyPreferences.isConsignmentSalesManagementFlag());
                            jobj.put("assetManagementFlag", extraCompanyPreferences.isAssetManagementFlag());
                            jobj.put("CompanyVATNumber", extraCompanyPreferences.getVatNumber());
                            jobj.put("CompanyCSTNumber", extraCompanyPreferences.getCstNumber());
                            jobj.put("isExciseApplicable", extraCompanyPreferences.isExciseApplicable());  //Check Is Excise is applicable at login time
                            jobj.put("isTDSApplicable", extraCompanyPreferences.isTDSapplicable());  //Check Is TDS is applicable at login time
                            if(extraCompanyPreferences != null && extraCompanyPreferences.getCompany().getCountry().getID().equals(Constants.INDONESIAN_COUNTRYID)){
                                jobj.put("CompanyNPWPNumber", extraCompanyPreferences.getPanNumber());
                            }else{
                                jobj.put("CompanyPANNumber", extraCompanyPreferences.getPanNumber());
                            }
                            jobj.put("CompanyServiceTaxRegNumber", extraCompanyPreferences.getServiceTaxRegNo());
                            jobj.put("CompanyTANNumber", extraCompanyPreferences.getTanNumber());
                            jobj.put("CompanyECCNumber", extraCompanyPreferences.getEccNumber());
                            jobj.put("activateMRPManagementFlag", extraCompanyPreferences.isActivateMRPModule());
                            jobj.put("jobWorkInFlowFlag", extraCompanyPreferences.isJobworkrecieverflow());
                            jobj.put("activateInventoryTab", extraCompanyPreferences.isActivateInventoryTab());
                            jobj.put("isNewGSTOnly", extraCompanyPreferences.isIsNewGST());                            
                        }
                        jobj.put("perm", permJobj);

                         JSONArray UserReoprtJson=new JSONArray();
                         kmsg = permissionHandlerDAOObj.getReportUserList(userid,companyid);
                        Iterator ite1 = kmsg.getEntityList().iterator();
                        while (ite1.hasNext()) {
                            Object[] roww = (Object[]) ite1.next();
                            UserReoprtJson.put(roww[0].toString());
                        }

                        jobj.put("UserReportPerm", UserReoprtJson);
                    } else {
                        jobj.put("deskeraadmin", true);
                    }


                    // Transaction price amendment permission

                    JSONObject pricrPermJson = new JSONObject();

                    AmendingPrice amendingPrice = (AmendingPrice) kwlCommonTablesDAOObj.getClassObject(AmendingPrice.class.getName(), userid);
                    if (amendingPrice != null) {
                        JSONObject amendPerm = new JSONObject();
                        amendPerm.put("invoice", !amendingPrice.isCInvoice());
                        amendPerm.put("goodsReceipt", !amendingPrice.isVInvoice());
                        amendPerm.put("salesOrder", !amendingPrice.isSalesOrder());
                        amendPerm.put("purchaseOrder", !amendingPrice.isPurchaseOrder());
                        amendPerm.put("vendorQuotation", !amendingPrice.isVendorQuotation());
                        amendPerm.put("customerQuotation", !amendingPrice.isCustomerQuotation());
                        amendPerm.put("BlockAmendingPrice", amendingPrice.isBlockAmendingPrice());
                        pricrPermJson.put("priceEditPerm", amendPerm);
                    } else {
                        JSONObject amendPerm = new JSONObject();
                        amendPerm.put("invoice", true);
                        amendPerm.put("goodsReceipt", true);
                        amendPerm.put("salesOrder", true);
                        amendPerm.put("purchaseOrder", true);
                        amendPerm.put("vendorQuotation", true);
                        amendPerm.put("customerQuotation", true);
                        amendPerm.put("BlockAmendingPrice",false);
                        pricrPermJson.put("priceEditPerm", amendPerm);
                    }

                    jobj.put("priceEditPerm", pricrPermJson);                    
                    JSONObject roleJson = new JSONObject();
                    /**
                     * ERP-ERP-41687 Change in Method parameter Old Parameter
                     * companyid new parameter JSONObject.
                     */
                    kmsg = permissionHandlerDAOObj.getRoleList(jobj);
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
                    if(ite.hasNext()) {
                        Object[] row = (Object[]) ite.next();
                        ujobj.put("roleid", row[0].toString());
                    }
                    roleJson.put("Role", rjobj);
                    roleJson.put("URole", ujobj);
                    jobj.put("role", roleJson);
                        jobj.put("base_url", URLUtil.getPageURL(request,""));
                    isvalid = true;
                } else {
                    jobj.put("success", false);
                    jobj.put("reason", result);
                    isvalid = false;
                }
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        } finally {
            try {
                jret.put("valid", isvalid);
                jret.put("data", jobj);
            } catch (JSONException ex) {
                Logger.getLogger(authHandlerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", "model", jret.toString());
    }

    public JSONObject getVerifyLoginJson(List ll, HttpServletRequest request) {
        JSONObject jobj = new JSONObject();
        try {
            Iterator ite = ll.iterator();
            if (ite.hasNext()) {
                Object[] row = (Object[]) ite.next();
                User user = (User) row[0];
                UserLogin userLogin = (UserLogin) row[1];
                Company company = (Company) row[2];
                jobj.put("success", true);
                jobj.put("lid", userLogin.getUserID());
                jobj.put("username", userLogin.getUserName());
                jobj.put("companyid", company.getCompanyID());
                jobj.put("subdomain", company.getSubDomain());		// subdomain for mailto support link on dashboard
                jobj.put("company", company.getCompanyName());
                jobj.put("templateflag", company.getTemplateflag());
                jobj.put("countryid", company.getCountry().getID());
                jobj.put("countrycode", company.getCountry().getCountryCode());
                Language lang=company.getLanguage();
                if(lang!=null)
                	jobj.put("language", lang.getLanguageCode()+(lang.getCountryCode()!=null?"_"+lang.getCountryCode():""));
                jobj.put("roleid", user.getRoleID());
                jobj.put("callwith", user.getCallwith());
                jobj.put("timeformat", user.getTimeformat());
                jobj.put("usermailid", user.getEmailID());
                jobj.put("userfullname", "" + user.getFirstName() + (StringUtil.isNullOrEmpty(user.getLastName())?"":(" "+ user.getLastName())));
                KWLTimeZone timeZone = user.getTimeZone();
                if (timeZone == null) {
                    timeZone = company.getTimeZone();
                }
                if (timeZone == null) {
                    timeZone = (KWLTimeZone) (KWLTimeZone) kwlCommonTablesDAOObj.getClassObject(KWLTimeZone.class.getName(), storageHandlerImpl.getDefaultTimeZoneID());
                }
                jobj.put("timezoneid", timeZone.getTimeZoneID());
                jobj.put("timeZId", timeZone.getTzID());
                jobj.put("tzdiff", timeZone.getDifference());
                jobj.put("companytzdiff", company.getTimeZone()!=null?company.getTimeZone().getDifference():timeZone.getDifference());
                KWLDateFormat dateFormat = user.getDateFormat();
                if (dateFormat == null) {
                    dateFormat = (KWLDateFormat) kwlCommonTablesDAOObj.getClassObject(KWLDateFormat.class.getName(), storageHandlerImpl.getDefaultDateFormatID());
                }
                jobj.put("DateFormat", dateFormat.getScriptForm());
                jobj.put("dateformatid", dateFormat.getFormatID());
                jobj.put("userdateformat", dateFormat.getJavaForm());
                KWLCurrency currency = company.getCurrency();
                if (currency == null) {
                    currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject(KWLCurrency.class.getName(), storageHandlerImpl.getDefaultCurrencyID());
                }
                jobj.put("currencyid", currency.getCurrencyID());
                jobj.put("currencysymbol", currency.getSymbol()); // To get Currency Symbol
                jobj.put("superuser", user.getRoleID());
                String childcompanyids = getChildCompanyids(company.getCompanyID());
                jobj.put("companyids", childcompanyids);
                jobj.put("gcurrencyid", currency.getCurrencyID());
            } else {
                jobj.put("failure", true);
                jobj.put("success", false);
            }
            if(!ll.isEmpty()&&ll.size()>4){
                RoleUserMapping roleUserMapping=(RoleUserMapping)ll.get(4);
                jobj.put("roleUserMappingId",roleUserMapping.getId());
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }

    public String getChildCompanyids(String companyid) {
        String childCompanyIds = companyid+",";
        try {
            List ll = companyDetailsDAOObj.getChildCompanies(companyid);
            Iterator itr = ll.iterator();
            while(itr.hasNext()) {                    
                Object[] arr = (Object[]) itr.next();
                childCompanyIds += arr[0].toString()+",";
            }
            childCompanyIds = childCompanyIds.substring(0, childCompanyIds.length()-1);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return childCompanyIds;
    }

    public JSONObject getPreferencesJson(List ll, HttpServletRequest request) {
        JSONObject jobj = new JSONObject();
        JSONObject retJobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        String dateformat = "";
        try {
            String timeformat = sessionHandlerImplObj.getUserTimeFormat(request);

            KWLTimeZone timeZone = (KWLTimeZone) ll.get(0);
            KWLDateFormat dateFormat = (KWLDateFormat) ll.get(1);
            KWLCurrency currency = (KWLCurrency) ll.get(2);

            jobj.put("Timezone", timeZone.getName());
            jobj.put("Timezoneid", timeZone.getTimeZoneID());
            jobj.put("Timezonediff", timeZone.getDifference());
            if (timeformat.equals("1")) {
                dateformat = dateFormat.getScriptForm().replace('H', 'h');
                if (!dateformat.equals(dateFormat.getScriptForm())) {
                    dateformat += " T";
                }
            } else {
                dateformat = dateFormat.getScriptForm();
            }
            jobj.put("DateFormat", dateformat);
            jobj.put("DateFormatid", dateFormat.getFormatID());
            jobj.put("seperatorpos", dateFormat.getScriptSeperatorPosition());
            jobj.put("Currency", currency.getHtmlcode());
            jobj.put("CurrencyName", currency.getName());
            jobj.put("CurrencySymbol", currency.getSymbol());
            jobj.put("Currencyid", currency.getCurrencyID());
            jarr.put(jobj);

            retJobj.put("data", jarr);
            retJobj.put("success", true);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return retJobj;
    }

    public ModelAndView getPreferences(HttpServletRequest request, HttpServletResponse response)
            throws ServletException {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("timezoneid", sessionHandlerImpl.getTimeZoneID(request));
            requestParams.put("dateformatid", sessionHandlerImpl.getDateFormatID(request));
            requestParams.put("currencyid", sessionHandlerImpl.getCurrencyID(request));
  
            kmsg = authHandlerDAOObj.getPreferences(requestParams);
            jobj = getPreferencesJson(kmsg.getEntityList(), request);
            auditTrailObj.insertAuditLog(AuditAction.PROFILE_CHANGED, "User " + sessionHandlerImpl.getUserFullName(request) + " has updated profile ", request,"43"); 
        } catch (Exception e) {
        System.out.println(e.getMessage());
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    // Locale Implementation
    protected void setLocale(HttpServletRequest request, HttpServletResponse response, String newLocale) {
        if (newLocale != null) {
            LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
            if (localeResolver == null) {
                Logger.getLogger(authHandlerController.class.getName()).log(Level.SEVERE, null, "No LocaleResolver found: not in a DispatcherServlet request?");
                return;
            }
            LocaleEditor localeEditor = new LocaleEditor();
            localeEditor.setAsText(newLocale);
            localeResolver.setLocale(request, response, (Locale) localeEditor.getValue());
        }
    }

    /*
     * a method to insert a login entry in to audit trail
     */
    private void insertLoginEntryInAudittrail(String userid, HttpServletRequest request) throws Exception{
                    Map<String, Object> auditRequestParams = new HashMap<String, Object>();
                    auditRequestParams.put(Constants.reqHeader, request.getHeader(Constants.reqHeader));
                    auditRequestParams.put(Constants.remoteIPAddress, request.getRemoteAddr());
                    auditRequestParams.put(Constants.useridKey, sessionHandlerImpl.getUserid(request));
                    String msg = "User "+sessionHandlerImpl.getUserFullName(request) +" has logged in successfully";
                    auditTrailObj.insertAuditLog(AuditAction.LOG_IN_SUCCESS, msg, auditRequestParams,userid);
    }
    
    public ModelAndView manageModuleTemplates(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            jobj = StringUtil.convertRequestToJsonObject(request);
            jobj = commonFnControllerService.getModuleTemplate(jobj);
            deleteModuleTemplates(request);
            msg = "Templates have been deleted successfully.";
            issuccess=true;
        } catch (AccountingException | SessionExpiredException | JSONException | ServiceException ex) {
            msg = ex.getMessage();
            issuccess=false;
            Logger.getLogger(authHandlerController.class.getName()).log(Level.SEVERE, null, ex);
        }finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(authHandlerController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deleteModuleTemplates(HttpServletRequest request) throws JSONException, SessionExpiredException, AccountingException,ServiceException {
        int delCount = 0;
        KwlReturnObject resultObject;
        //boolean linkedTransaction = false, isFixedAsset = false, isLeaseFixedAsset = false, isConsignment = false, auditcheck = false;
        String companyId = sessionHandlerImpl.getCompanyid(request);
        String templateId = request.getParameter("templateId");
        String moduleName = request.getParameter("moduleName");
        String templateName = request.getParameter("templateName");
        String billid = request.getParameter("billid");
        String moduleid = request.getParameter("moduleid");
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put(Constants.companyKey, companyId);
        try {
            if (!StringUtil.isNullOrEmpty(companyId)) {
                if (!StringUtil.isNullOrEmpty(billid) && !StringUtil.isNullOrEmpty(moduleid)) {
                    int module = Integer.parseInt(moduleid);
                    if (Constants.Acc_Invoice_ModuleId == module || Constants.Acc_Cash_Sales_ModuleId== module) {
                        requestParams.put("invoiceid", billid);
                        accInvoiceDAOObj.deleteInvoicePermanent(requestParams);
                    } else if (Constants.Acc_Vendor_Invoice_ModuleId == module || Constants.Acc_Cash_Purchase_ModuleId==module) {
                        requestParams.put("greceiptid", billid);
                        accGoodsReceiptobj.deleteGoodsReceiptPermanent(requestParams);
                    } else if (Constants.Acc_Sales_Order_ModuleId == module) {
                        requestParams.put("soid", billid);
                        accSalesOrderDAOobj.deleteSalesOrdersPermanent(requestParams);
                    } else if (Constants.Acc_Purchase_Order_ModuleId == module) {
                        requestParams.put("poid", billid);
                        accPurchaseOrderobj.deletePurchaseOrdersPermanent(requestParams);
                    } else if (Constants.Acc_Customer_Quotation_ModuleId == module) {
                        requestParams.put("qid", billid);
                        accSalesOrderDAOobj.deleteQuotationsPermanent(requestParams);
                    } else if (Constants.Acc_GENERAL_LEDGER_ModuleId == module) {
                        accJournalEntryobj.deleteJournalEntryPermanent(billid, companyId);
                    }
//                    else if (Constants.Acc_Vendor_Quotation_ModuleId == module) {
//                        requestParams.put("qid", billid);
//                        accPurchaseOrderobj.deleteQuotationsPermanent(requestParams);
//                    } 
                }
                resultObject = accountingHandlerDAOobj.deleteModuleTemplates(companyId, templateId);
                auditTrailObj.insertAuditLog(AuditAction.TEMPLATE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted " + moduleName + " template " + templateName, request, "12");
            }
        } catch (ServiceException ex) {
            Logger.getLogger(authHandlerController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
}
