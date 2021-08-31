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
package com.krawler.spring.accounting.account;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.FieldComboData;
import com.krawler.common.admin.FieldParams;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.DataInvalidateException;
import com.krawler.common.util.IndiaComplianceConstants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.customFieldMaster.fieldDataManager;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.esp.utils.ConfigReader;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.companyDetails.companyDetailsDAO;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.profileHandler.profileHandlerDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
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
import com.krawler.esp.handlers.ServerEventManager;
import com.krawler.spring.accounting.masteritems.accMasterItemsController;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import javax.servlet.ServletContext;
import static com.krawler.esp.web.resource.Links.loginpageFull;
//import java.net.URLDecoder;

/**
 *
 * @author krawler
 */
public class accAccountController extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private accAccountDAO accAccountDAOobj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private String successView;
    private auditTrailDAO auditTrailObj;
    private exportMPXDAOImpl exportDaoObj;
    private ImportHandler importHandler;
    private accCurrencyDAO accCurrencyDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private MessageSource messageSource;
    private accMasterItemsDAO accMasterItemsDAOObj;
    private profileHandlerDAO profileHandlerDAOObj;
    private accAccountModuleService controllerService;
    private fieldDataManager fieldDataManager;
    private APICallHandlerService apiCallHandlerService;  
    private companyDetailsDAO companyDetailsDAOObj;

    @Override
    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManager = fieldDataManagercntrl;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setexportMPXDAOImpl(exportMPXDAOImpl exportDaoObj) {
        this.exportDaoObj = exportDaoObj;
    }

    public void setControllerService(accAccountModuleService controllerService) {
        this.controllerService = controllerService;
    }

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
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

    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOObj) {
        this.accMasterItemsDAOObj = accMasterItemsDAOObj;
    }

    public void setprofileHandlerDAO(profileHandlerDAO profileHandlerDAOObj) {
        this.profileHandlerDAOObj = profileHandlerDAOObj;
    }
    
     public void setcompanyDetailsDAO(companyDetailsDAO companyDetailsDAOObj1) {
        this.companyDetailsDAOObj = companyDetailsDAOObj1;
    }
    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {
        this.apiCallHandlerService = apiCallHandlerService;
    }

    public ModelAndView getAccounts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = accAccountHandler.getRequestMap(request);
            KwlReturnObject result = accAccountDAOobj.getAccounts(requestParams);
            List list = result.getEntityList();
            jobj = accAccountHandler.getAccountJson(request, list, accCurrencyDAOobj, false);
            JSONArray jSONArray = jobj.getJSONArray("data");
            for (int count = 0; count < jSONArray.length(); count++) {
                if (jSONArray.getJSONObject(count).has("accid")) {
                    String accId = jSONArray.getJSONObject(count).getString("accid");
                    KwlReturnObject custResult = accountingHandlerDAOobj.getObject("com.krawler.hql.accounting.Customer", accId);
                    KwlReturnObject venResult = accountingHandlerDAOobj.getObject("com.krawler.hql.accounting.Vendor", accId);
                    if (custResult.getEntityList().get(0) != null || venResult.getEntityList().get(0) != null) {
                        jSONArray.getJSONObject(count).put("isOnlyAccount", "false");
                    } else {
                        jSONArray.getJSONObject(count).put("isOnlyAccount", "true");
                    }
                }
            }
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    // method moved to main controller. now it is in no more use.
    public ModelAndView getAccountsForCombo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = true;
        boolean isCOA = false;
        boolean headerAdded = false;
        boolean consolidateAccMapFlag = false;
        boolean levelFlag = false;
        boolean isFixedAsset = false;
        String msg = "";
        try {
            consolidateAccMapFlag = Boolean.parseBoolean((String) request.getParameter("consolidateAccMapFlag"));
            levelFlag = Boolean.parseBoolean((String) request.getParameter("levelFlag"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> requestParams = accAccountHandler.getRequestMap(request);
            requestParams.put("templateid", request.getParameter("templateid"));//  Custom Layout - filter accounts if already mapped in the selected template
            KwlReturnObject result = accAccountDAOobj.getAccountsForCombo(requestParams);
            List list = result.getEntityList();
            ArrayList resultlist = new ArrayList();
            if (requestParams.containsKey("COA")) {
                isCOA = Boolean.parseBoolean((String) requestParams.get("COA"));
            }
            if (requestParams.containsKey("headerAdded")) {
                headerAdded = Boolean.parseBoolean((String) requestParams.get("headerAdded"));
            }
            if (requestParams.containsKey("isFixedAsset")) {
                isFixedAsset = Boolean.parseBoolean((String) requestParams.get("isFixedAsset"));
            }
            boolean ignoreCustomers = requestParams.get("ignorecustomers") != null;
            boolean ignoreVendors = requestParams.get("ignorevendors") != null;
            boolean ignoreTransactionFlag = request.getParameter("ignoreTransactionFlag") != null ? Boolean.parseBoolean(request.getParameter("ignoreTransactionFlag")) : true;

            String excludeaccountid = (String) requestParams.get("accountid");
            String includeaccountid = (String) requestParams.get("includeaccountid");
            String includeparentid = (String) requestParams.get("includeparentid");
            String customerCpath = ConfigReader.getinstance().get("Customer");
            String vendorCpath = ConfigReader.getinstance().get("Vendor");

            boolean isCustomers = requestParams.get("isCustomer") != null ? Boolean.parseBoolean(request.getParameter("isCustomer")) : true;
            boolean isVendors = requestParams.get("isVendor") != null ? Boolean.parseBoolean(request.getParameter("isVendor")) : true;
//            boolean deleted =Boolean.parseBoolean((String)requestParams.get("deleted"));
//            boolean nondeleted =Boolean.parseBoolean((String)requestParams.get("nondeleted"));
            String currencyid = (String) requestParams.get("currencyid");
            KWLCurrency currency = (KWLCurrency) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.KWLCurrency", currencyid);

            Iterator itr = list.iterator();
//            int level=0;
            while (itr.hasNext()) {
//                Object listObj = itr.next();
                Object[] row = (Object[]) itr.next();
                KwlReturnObject retObj = accountingHandlerDAOobj.getObject(Account.class.getName(), row[0].toString());
                Account account = (Account) retObj.getEntityList().get(0);
                if (excludeaccountid != null && account.getID().equals(excludeaccountid)) {
                    continue;
                }
                if ((includeparentid != null && (!account.getID().equals(includeparentid) || (account.getParent() != null && !account.getParent().getID().equals(includeparentid))))) {
                    continue;
                } else if ((includeaccountid != null && !account.getID().equals(includeaccountid))) {
                    continue;
                }

//                Object c = kwlCommonTablesDAOObj.getClassObject(customerCpath, account.getID());
//                if(ignoreCustomers&&account.getGroup().getID().equals(Group.ACCOUNTS_RECEIVABLE)){
//                    if(c!=null)continue;
//                }
//
//                Object v = kwlCommonTablesDAOObj.getClassObject(vendorCpath, account.getID());
//                if(ignoreVendors&&account.getGroup().getID().equals(Group.ACCOUNTS_PAYABLE)){
//                    if(v!=null)continue;
//                }
//                
//                if ((c != null || v != null) && isCOA) {
//                    continue;
//                }

                if (!consolidateAccMapFlag && headerAdded && !isFixedAsset && ignoreTransactionFlag) {
                    KwlReturnObject Count = accAccountDAOobj.getJEDTrasactionfromAccount(account.getID(), companyid);
                    int jedCount = Count.getRecordTotalCount();
                    double openingBal = account.getOpeningBalance();
                    if (!(jedCount == 0 && openingBal == 0)) {
                        continue;
                    }
                }
                JSONObject obj = new JSONObject();
                obj.put("accid", account.getID());
                obj.put("accname", (!StringUtil.isNullOrEmpty(account.getName())) ? account.getName() : (!StringUtil.isNullOrEmpty(account.getAcccode()) ? account.getAcccode() : ""));
                obj.put("accountpersontype", 0);
                obj.put("mappedaccountid", account.getID());
                obj.put("groupid", account.getGroup().getID());
                obj.put("mastergroupid", (account.getGroup() != null) ? account.getGroup().getID() : "");
                obj.put("groupname", account.getGroup().getName());
                obj.put("acccode", account.getAcccode());
                obj.put("accnamecode", (!StringUtil.isNullOrEmpty(account.getAcccode()) ? "[" + account.getAcccode() + "] " + account.getName() : account.getName()));
                obj.put("nature", account.getGroup().getNature());
                obj.put("naturename", (account.getGroup().getNature() == Constants.Liability) ? "Liability" : (account.getGroup().getNature() == Constants.Asset) ? "Asset" : (account.getGroup().getNature() == Constants.Expences) ? "Expences" : (account.getGroup().getNature() == Constants.Income) ? "Income" : "");
                obj.put("currencyid", (account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getCurrencyID()));
                obj.put("currencysymbol", (account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getSymbol()));
                obj.put("currencyname", (account.getCurrency() == null ? currency.getName() : account.getCurrency().getName()));
                obj.put("currencycode", (account.getCurrency() == null ? currency.getCurrencyCode() : account.getCurrency().getCurrencyCode()));
                obj.put("deleted", account.isDeleted());
                obj.put("aliascode", account.getAliascode() == null ? "" : account.getAliascode());
                if (account.getAcccode() != null) {
                    obj.put("acccode", account.getAcccode());
                } else {
                    obj.put("acccode", "");
                }
//                if (c != null || v != null) {
//                    obj.put("isOnlyAccount", "false");
//                } else {
                obj.put("isOnlyAccount", "true");
//                }
                if (levelFlag) {
                    int level = 0;
                    level = getAccountLevel(account, level);
                    obj.put("level", level);
                }
//                obj.put("depreciationaccount", account.getDepreciationAccont()==null?"":account.getDepreciationAccont().getID());
//                obj.put("openbalance", account.getOpeningBalance());
//                Account parentAccount = (Account) row[6];
//                if(parentAccount!=null){
//                    obj.put("parentid", parentAccount.getID());
//                    obj.put("parentname", parentAccount.getName());
//                }
//                obj.put("level", row[3]);
//                obj.put("leaf", row[4]);
//                obj.put("presentbalance", account.getPresentValue());
//                obj.put("life", account.getLife());
//                obj.put("salvage", account.getSalvage());
//                obj.put("posted", row[7]);
//                obj.put("creationDate", authHandler.getDateFormatter(request).format(account.getCreationDate()));
//                obj.put("categoryid", account.getCategory()==null?"":account.getCategory().getID());
                jArr.put(obj);
            }

            if (!ignoreCustomers) {
                result = accAccountDAOobj.getCustomerForCombo(requestParams);
                List ls = result.getEntityList();
                Iterator<Object[]> itr1 = ls.iterator();
                while (itr1.hasNext()) {
                    Object[] row = (Object[]) itr1.next();
                    String customerid = (String) row[0].toString();
                    String customername = (String) row[1].toString();
                    String accountid = (String) row[2].toString();
                    String customercode = (row[3] != null) ? (String) row[3].toString() : "";
                    Account account = (Account) kwlCommonTablesDAOObj.getClassObject(Account.class.getName(), accountid);

                    if (excludeaccountid != null && customerid.equals(excludeaccountid)) {
                        continue;
                    }

                    JSONObject obj = new JSONObject();
                    obj.put("accid", customerid);
                    obj.put("accname", customername);
                    obj.put("groupid", account.getGroup().getID());
                    obj.put("accountpersontype", 1);
                    obj.put("mappedaccountid", account.getID());
                    obj.put("mastergroupid", (account.getGroup() != null) ? account.getGroup().getID() : "");
                    obj.put("groupname", account.getGroup().getName());
                    obj.put("acccode", customercode);
                    obj.put("accnamecode", (!StringUtil.isNullOrEmpty(account.getAcccode()) ? "[" + account.getAcccode() + "] " + account.getName() : account.getName()));
                    obj.put("nature", account.getGroup().getNature());
                    obj.put("naturename", (account.getGroup().getNature() == Constants.Liability) ? "Liability" : (account.getGroup().getNature() == Constants.Asset) ? "Asset" : (account.getGroup().getNature() == Constants.Expences) ? "Expences" : (account.getGroup().getNature() == Constants.Income) ? "Income" : "");
                    obj.put("currencyid", (account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getCurrencyID()));
                    obj.put("currencysymbol", (account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getSymbol()));
                    obj.put("currencyname", (account.getCurrency() == null ? currency.getName() : account.getCurrency().getName()));
                    obj.put("currencycode", (account.getCurrency() == null ? currency.getCurrencyCode() : account.getCurrency().getCurrencyCode()));
                    obj.put("deleted", account.isDeleted());
                    obj.put("aliascode", account.getAliascode() == null ? "" : account.getAliascode());

                    jArr.put(obj);
                }
            }
            if (!ignoreVendors) {
                result = accAccountDAOobj.getVendorForCombo(requestParams);
                List ls = result.getEntityList();
                Iterator<Object[]> itr1 = ls.iterator();
                while (itr1.hasNext()) {
                    Object[] row = (Object[]) itr1.next();
                    String vendorid = (String) row[0].toString();
                    String vendorname = (String) row[1].toString();
                    String accountid = (String) row[2].toString();
                    String vendorcode = (row[3] != null) ? (String) row[3].toString() : "";
                    Account account = (Account) kwlCommonTablesDAOObj.getClassObject(Account.class.getName(), accountid);

                    if (excludeaccountid != null && vendorid.equals(excludeaccountid)) {
                        continue;
                    }

                    JSONObject obj = new JSONObject();
                    obj.put("accid", vendorid);
                    obj.put("accname", vendorname);
                    obj.put("mappedaccountid", account.getID());
                    obj.put("groupid", account.getGroup().getID());
                    obj.put("mastergroupid", (account.getGroup() != null) ? account.getGroup().getID() : "");
                    obj.put("groupname", account.getGroup().getName());
                    obj.put("acccode", vendorcode);
                    obj.put("accountpersontype", 2);
                    obj.put("accnamecode", (!StringUtil.isNullOrEmpty(account.getAcccode()) ? "[" + account.getAcccode() + "] " + account.getName() : account.getName()));
                    obj.put("nature", account.getGroup().getNature());
                    obj.put("naturename", (account.getGroup().getNature() == Constants.Liability) ? "Liability" : (account.getGroup().getNature() == Constants.Asset) ? "Asset" : (account.getGroup().getNature() == Constants.Expences) ? "Expences" : (account.getGroup().getNature() == Constants.Income) ? "Income" : "");
                    obj.put("currencyid", (account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getCurrencyID()));
                    obj.put("currencysymbol", (account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getSymbol()));
                    obj.put("currencyname", (account.getCurrency() == null ? currency.getName() : account.getCurrency().getName()));
                    obj.put("currencycode", (account.getCurrency() == null ? currency.getCurrencyCode() : account.getCurrency().getCurrencyCode()));
                    obj.put("deleted", account.isDeleted());
                    obj.put("aliascode", account.getAliascode() == null ? "" : account.getAliascode());

                    jArr.put(obj);
                }
            }

            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getLayoutGroups(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = true;

        String msg = "";
        try {
            boolean isAdminSubdomain = authHandler.isAdminSubDomain(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String templateid = request.getParameter("templateid");
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyid);
            requestParams.put("templateid", templateid);
            requestParams.put("isAdminSubdomain", isAdminSubdomain);

            KwlReturnObject result = accAccountDAOobj.getNextCustomLayoutSequence(requestParams);
            List list = result.getEntityList();
            int nextSequence = Integer.parseInt(list.get(0).toString());
            jobj.put("nextSequence", nextSequence);

            requestParams.put("isAdminSubdomain", isAdminSubdomain);
            result = accAccountDAOobj.getCustomLayoutGroups(requestParams);
            if (!isAdminSubdomain) {
                List<LayoutGroup> layoutGroups = result.getEntityList();
                for (LayoutGroup group : layoutGroups) {
                JSONObject obj = new JSONObject();
                int sequence = group.getSequence();
                String groupname = group.getName();
                obj.put("groupid", group.getID());
                obj.put("sequence", sequence);
                obj.put("groupname", groupname);
                obj.put("nature", group.getNature());
                obj.put("parentid", group.getParent() != null ? group.getParent().getID() : "");
                obj.put("parentname", group.getParent() != null ? group.getParent().getName() : "");
                obj.put("showtotal", group.getShowtotal() == 1 ? true : false);
                obj.put("showchild", group.getShowchild() == 1 ? true : false);
                obj.put("showchildacc", group.getShowchildacc() == 1 ? true : false);
                obj.put("excludeChildBalances", group.isExcludeChildAccountBalances());
                obj.put("addBlankRowBefore", group.getNumberofrows());
                jArr.put(obj);
            }
            } else {
                List<DefaultLayoutGroup> layoutGroups = result.getEntityList();
                for (DefaultLayoutGroup group : layoutGroups) {
                    JSONObject obj = new JSONObject();
                    int sequence = group.getSequence();
                    String groupname = group.getName();
                    obj.put("groupid", group.getID());
                    obj.put("sequence", sequence);
                    obj.put("groupname", groupname);
                    obj.put("nature", group.getNature());
                    obj.put("parentid", group.getParent() != null ? group.getParent().getID() : "");
                    obj.put("parentname", group.getParent() != null ? group.getParent().getName() : "");
                    obj.put("showtotal", group.getShowtotal() == 1 ? true : false);
                    obj.put("showchild", group.getShowchild() == 1 ? true : false);
                    obj.put("showchildacc", group.getShowchildacc() == 1 ? true : false);
                    obj.put("excludeChildBalances", group.isExcludeChildAccountBalances());
                    jArr.put(obj);
                }
            }
            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getNextCustomLayoutSequence(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = true;

        String msg = "";
        try {
            boolean isAdminSubdomain = authHandler.isAdminSubDomain(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String templateid = request.getParameter("templateid");
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyid);
            requestParams.put("templateid", templateid);
            requestParams.put("isAdminSubdomain", isAdminSubdomain);

            KwlReturnObject result = accAccountDAOobj.getNextCustomLayoutSequence(requestParams);
            List list = result.getEntityList();
            int nextSequence = Integer.parseInt(list.get(0).toString());
            jobj.put("nextSequence", nextSequence);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getLayoutGroupsFortotalgroupmap(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = true;

        String msg = "";
        try {
            boolean isAdminSubdomain = authHandler.isAdminSubDomain(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyid);
            if (!StringUtil.isNullOrEmpty(request.getParameter("totalgroupid"))) {
                requestParams.put("totalgroupid", request.getParameter("totalgroupid"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("sequence"))) {
                requestParams.put("sequence_from", request.getParameter("sequence"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("templateid"))) {
                requestParams.put("templateid", request.getParameter("templateid"));
            }
            KwlReturnObject result = null;
            if(isAdminSubdomain){
                result = accAccountDAOobj.getDefaultLayoutGroupsFortotalgroupmap(requestParams);
                List list = result.getEntityList();
                Iterator itr = list.iterator();
                
                while (itr.hasNext()) {
                    Object listObj = itr.next();
                    DefaultLayoutGroup group = (DefaultLayoutGroup) listObj;

                    JSONObject obj = new JSONObject();
                    int sequence = group.getSequence();
                    String groupname = group.getName();
                    obj.put("groupid", group.getID());
                    obj.put("sequence", sequence);
                    obj.put("groupname", groupname);
                    obj.put("nature", group.getNature());

                    jArr.put(obj);
                }
            }else{
                result = accAccountDAOobj.getLayoutGroupsFortotalgroupmap(requestParams);
                List list = result.getEntityList();
                Iterator itr = list.iterator();
                
                while (itr.hasNext()) {
                    Object listObj = itr.next();
                    LayoutGroup group = (LayoutGroup) listObj;

                    JSONObject obj = new JSONObject();
                    int sequence = group.getSequence();
                    String groupname = group.getName();
                    obj.put("groupid", group.getID());
                    obj.put("sequence", sequence);
                    obj.put("groupname", groupname);
                    obj.put("nature", group.getNature());


                    jArr.put(obj);
                }
            }
            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getMappedLayoutGroupsforgrouptotal(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = true;

        String msg = "";
        try {
            boolean isAdminSubdomain = authHandler.isAdminSubDomain(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyid);
            requestParams.put("totalgroupid", request.getParameter("totalgroupid"));
            KwlReturnObject result = null;
            if(isAdminSubdomain){
                result = accAccountDAOobj.getMappedDefaultLayoutGroupsforgrouptotal(requestParams);
                List list = result.getEntityList();
                Iterator itr = list.iterator();

                while (itr.hasNext()) {
                    Object listObj = itr.next();
                    DefaultGroupMapForTotal group = (DefaultGroupMapForTotal) listObj;

                    if (!StringUtil.equal(group.getAction(), "NULL")) {
                        JSONObject obj = new JSONObject();
                        obj.put("groupid", "plus" + group.getGroupid().getID());
                        obj.put("groupname", "<div style='text-align:center;'><a href='#' style='text-decoration:none;color:#083772'>" + group.getAction() + "</a></div>");
                        obj.put("sequence", group.getGroupid().getSequence());
                        obj.put("ruletype", StringUtil.equal(group.getAction(), "PLUS") ? 1 : 2);
                        jArr.put(obj);
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("groupid", group.getGroupid().getID());
                    obj.put("groupname", group.getGroupid().getName());
                    obj.put("sequence", group.getGroupid().getSequence());
                    obj.put("ruletype", 0);

                    jArr.put(obj);
                }
            }else{
                result = accAccountDAOobj.getMappedLayoutGroupsforgrouptotal(requestParams);
                List list = result.getEntityList();
                Iterator itr = list.iterator();

                while (itr.hasNext()) {
                    Object listObj = itr.next();
                    Groupmapfortotal group = (Groupmapfortotal) listObj;


                    if (!StringUtil.equal(group.getAction(), "NULL")) {
                        JSONObject obj = new JSONObject();
                        obj.put("groupid", "plus" + group.getGroupid().getID());
                        obj.put("groupname", "<div style='text-align:center;'><a href='#' style='text-decoration:none;color:#083772'>" + group.getAction() + "</a></div>");
                        obj.put("sequence", group.getGroupid().getSequence());
                        obj.put("ruletype", StringUtil.equal(group.getAction(), "PLUS") ? 1 : 2);
                        jArr.put(obj);
                    }
                    JSONObject obj = new JSONObject();
                    obj.put("groupid", group.getGroupid().getID());
                    obj.put("groupname", group.getGroupid().getName());
                    obj.put("sequence", group.getGroupid().getSequence());
                    obj.put("ruletype", 0);


                    jArr.put(obj);
                }
            }
            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView deleteLayoutGroup(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = true;
        boolean isCommitEx = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            issuccess = deleteLayoutGroup(request);

            try {
                if (issuccess) {
                    txnManager.commit(status);
                    msg = messageSource.getMessage("acc.field.Groupdeletedsuccessfully", null, RequestContextUtils.getLocale(request));
                } else {
                    txnManager.rollback(status);
                    msg = messageSource.getMessage("acc.field.SelectedGroupiscurrentlyusedasparentgroupSoitcannotbedeleted", null, RequestContextUtils.getLocale(request));
                }
            } catch (Exception ex) {
                msg = messageSource.getMessage("acc.field.Erroroccuredwhiledeletingthegroup", null, RequestContextUtils.getLocale(request));
                isCommitEx = false;
            }
        } catch (Exception ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public boolean deleteLayoutGroup(HttpServletRequest request) throws ServiceException {
        boolean issuccess = true;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String groupid = request.getParameter("groupid");
            String templateid = request.getParameter("templateid");
            int sequence = request.getParameter("sequence") !=null ? Integer.parseInt(request.getParameter("sequence")):null;
            boolean isAdminSubdomain = authHandler.isAdminSubDomain(request);

            if(isAdminSubdomain){
                accAccountDAOobj.deleteDefaultLayoutGroupAccount(groupid);
                accAccountDAOobj.deleteDefaultLayoutGroupsofTotalGroup(groupid);
                accAccountDAOobj.deleteDefaultLayoutGroup(groupid);
            }else{
            accAccountDAOobj.deleteLayoutGroupAccount(groupid, companyid);
            accAccountDAOobj.deleteLayoutGroupsofTotalGroup(groupid, companyid);
            accAccountDAOobj.deleteLayoutGroup(groupid, companyid);
            }
            if (isAdminSubdomain) {
                accAccountDAOobj.updateDefaultLayoutGroupExistingSequnceNo(sequence+1, companyid, templateid, "substraction");
            } else {
                accAccountDAOobj.updateExistingSequnceNo(sequence+1, companyid, templateid, "substraction",groupid);
            }
            
        } catch (Exception ex) {
            issuccess = false;
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return issuccess;
        }

    }

    public ModelAndView getAccountsFormappedPnL(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();

            requestParams.put("templateid", request.getParameter("templateid"));
            requestParams.put("isincome", Integer.parseInt(request.getParameter("isincome")));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject result = accAccountDAOobj.getAccountsFormappedPnL(requestParams);
            List list = result.getEntityList();

            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                PnLAccountMap listObj = (PnLAccountMap) itr.next();
                JSONObject obj = new JSONObject();
                Account account = listObj.getAccount();
                obj.put("accid", account.getID());
                obj.put("accname", account.getName());
                obj.put("acccode", account.getAcccode());
                obj.put("groupname", account.getGroup().getName());
                obj.put("naturename", (account.getGroup().getNature() == Constants.Liability) ? "Liability" : (account.getGroup().getNature() == Constants.Asset) ? "Asset" : (account.getGroup().getNature() == Constants.Expences) ? "Expences" : (account.getGroup().getNature() == Constants.Income) ? "Income" : "");

                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);

        } catch (Exception ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getAccountsForLayoutGroup(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            boolean isAdminSubdomain = authHandler.isAdminSubDomain(request);
            Map<String, Object> requestParams = new HashMap<String, Object>();

            requestParams.put("groupid", request.getParameter("groupid"));
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("isAdminSubdomain", isAdminSubdomain);
            KwlReturnObject result = null;
            List list = null;
            if(isAdminSubdomain){
                result = accAccountDAOobj.getAccountsForDefaultLayoutGroup(requestParams);
                list = result.getEntityList();

            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                    DefaultGroupAccMap listObj = (DefaultGroupAccMap) itr.next();
                    Map<String, Object> requestParamsDAccount = new HashMap<String, Object>();
                    requestParamsDAccount.put("accountname", listObj.getAccountname());
                    requestParamsDAccount.put("groupname", listObj.getGroupname());
                    KwlReturnObject resultDAccount = accAccountDAOobj.getDefaultAccountFromName(requestParamsDAccount);
                    List listDAccount = resultDAccount.getEntityList();
                    
                    Iterator itrDAccount = listDAccount.iterator();
                    if(itrDAccount.hasNext()){
                        JSONObject obj = new JSONObject();
                        DefaultAccount defaultaccount = (DefaultAccount) itrDAccount.next();
                        obj.put("accid", defaultaccount.getID());
                        obj.put("accname", defaultaccount.getName());
//                        obj.put("acccode", defaultaccount.getAcccode());//Need to add this later on
                        obj.put("groupname", defaultaccount.getGroup().getName());
                        obj.put("naturename", (defaultaccount.getGroup().getNature() == Constants.Liability) ? "Liability" : (defaultaccount.getGroup().getNature() == Constants.Asset) ? "Asset" : (defaultaccount.getGroup().getNature() == Constants.Expences) ? "Expences" : (defaultaccount.getGroup().getNature() == Constants.Income) ? "Income" : "");

                        jArr.put(obj);
                    }
                }
            }else{
                result = accAccountDAOobj.getAccountsForLayoutGroup(requestParams);
                list = result.getEntityList();

                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                GroupAccMap listObj = (GroupAccMap) itr.next();
                JSONObject obj = new JSONObject();
                Account account = listObj.getAccount();
                obj.put("accid", account.getID());
                obj.put("accname", account.getName());
                obj.put("acccode", account.getAcccode());
                obj.put("groupname", account.getGroup().getName());
                obj.put("naturename", (account.getGroup().getNature() == Constants.Liability) ? "Liability" : (account.getGroup().getNature() == Constants.Asset) ? "Asset" : (account.getGroup().getNature() == Constants.Expences) ? "Expences" : (account.getGroup().getNature() == Constants.Income) ? "Income" : "");

                jArr.put(obj);
            }
            }
            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);

        } catch (Exception ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);

        } finally {
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getPnLTemplates(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("isdropdown", Boolean.parseBoolean(request.getParameter("isdropdown")));

            if ((request.getParameter("start") != null) && (request.getParameter("limit") != null)) {
                requestParams.put("start", Integer.parseInt(request.getParameter("start")));
                requestParams.put("limit", Integer.parseInt(request.getParameter("limit")));
            }

            if (!StringUtil.isNullOrEmpty(request.getParameter("templatetype"))) {
                requestParams.put("templatetype", Integer.parseInt(request.getParameter("templatetype")));
            }
            boolean isAdminSubdomain = authHandler.isAdminSubDomain(request);
            KwlReturnObject result = null;
            JSONArray jArr = new JSONArray();
            if (!isAdminSubdomain) {
                result = accAccountDAOobj.getPnLTemplates(requestParams);
                List<Templatepnl> templatepnls  = result.getEntityList();
                for (Templatepnl listObj : templatepnls) {
                int templateCode = 0;
                templateCode += Math.pow(2, listObj.getTemplateid());
                JSONObject obj = new JSONObject();
                obj.put("id", listObj.getID());
                obj.put("name", listObj.getName());
                obj.put("templatetitle", listObj.getTemplatetitle());
                obj.put("templateheadings", listObj.getTemplateheading());
                obj.put("templatetype", listObj.getTemplatetype());
                obj.put("templatecode", templateCode);
                obj.put("status", listObj.getStatus());
                obj.put("isdefault", listObj.isDefaultTemplate());
                jArr.put(obj);
            }
            } else {
                result = accAccountDAOobj.getDefaultPnLTemplates(requestParams);
                List<DefaultTemplatePnL> defaultTemplatepnl=result.getEntityList();
                for (DefaultTemplatePnL defaultTemplatePnL : defaultTemplatepnl) {
                    int templateCode = 0;
                    templateCode += Math.pow(2, defaultTemplatePnL.getTemplateid());
                    JSONObject obj = new JSONObject();
                    obj.put("id", defaultTemplatePnL.getID());
                    obj.put("name", defaultTemplatePnL.getName());
                    obj.put("templatetitle", defaultTemplatePnL.getTemplatetitle());
                    obj.put("templatetype", defaultTemplatePnL.getTemplatetype());
                    obj.put("templatecode", templateCode);
                    obj.put("status", defaultTemplatePnL.getStatus());
                    obj.put(Constants.COUNTRY_NAME, defaultTemplatePnL.getCountry() != null ? (StringUtil.isNullOrEmpty(defaultTemplatePnL.getCountry().getCountryName()) ? "" : defaultTemplatePnL.getCountry().getCountryName()) : "");
                    obj.put(Constants.COUNTRY_ID, defaultTemplatePnL.getCountry() != null ? defaultTemplatePnL.getCountry().getID() : "");
                    jArr.put(obj);
                }
            }
            jobj.put("data", jArr);
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getDefaultPnLTemplates(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("isDefault",true);

            boolean isAdminSubdomain = authHandler.isAdminSubDomain(request);
            KwlReturnObject result = null;
            JSONArray jArr = new JSONArray();
            if (!isAdminSubdomain) {
                result = accAccountDAOobj.getPnLTemplates(requestParams);
                List<Templatepnl> templatepnls = result.getEntityList();
                for (Templatepnl listObj : templatepnls) {
                    int templateCode = 0;
                    templateCode += Math.pow(2, listObj.getTemplateid());
                    JSONObject obj = new JSONObject();
                    obj.put("id", listObj.getID());
                    obj.put("name", listObj.getName());
                    obj.put("templatetitle", listObj.getTemplatetitle());
                    obj.put("templateheadings", listObj.getTemplateheading());
                    obj.put("templatetype", listObj.getTemplatetype());
                    obj.put("templatecode", templateCode);
                    obj.put("status", listObj.getStatus());
                    obj.put("isdefault", listObj.isDefaultTemplate());
                    jArr.put(obj);
                }
            }
            jobj.put("data", jArr);
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public int getAccountLevel(Account account, int level) throws ServiceException {
        if (account.getParent() != null) {
            level++;
            level = getAccountLevel(account.getParent(), level);
        }
        return level;
    }

    public ModelAndView saveGroup(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String groupID = null, msg = "";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String groupid = request.getParameter("groupid");
            String isEditString=request.getParameter("isEdit");
            boolean isEdit = false;
            if (!StringUtil.isNullOrEmpty(isEditString)) {
                isEdit = Boolean.parseBoolean(isEditString);
            }
            Group group = saveGroup(request);
            groupID = group.getID();
            issuccess = true;
            if (StringUtil.isNullOrEmpty(groupid)) {
                msg = messageSource.getMessage("acc.acc.add", null, RequestContextUtils.getLocale(request));   //"Account type has been added successfully.";
            } else {
                msg = messageSource.getMessage("acc.acc.update", null, RequestContextUtils.getLocale(request));   //"Account type has been updated successfully.";
            }
            txnManager.commit(status);

            //*****************************************Propagate customer In child companies**************************
            String auditID = "";
            boolean propagateTOChildCompaniesFalg = false;

            if (!StringUtil.isNullOrEmpty(request.getParameter("ispropagatetochildcompanyflag"))) {
                propagateTOChildCompaniesFalg = Boolean.parseBoolean(request.getParameter("ispropagatetochildcompanyflag"));
            }
            if (propagateTOChildCompaniesFalg) {
                try {
                    String parentcompanyid = sessionHandlerImpl.getCompanyid(request);
                    Map<String, Object> parentdataMap = new HashMap<>();
                    Map<String, Object> requestMap = request.getParameterMap();
                    Set set = requestMap.entrySet();
                    for (Object obj : set) {
                        Map.Entry<String, Object> entry = (Map.Entry<String, Object>) obj;
                        String[] value = (String[]) entry.getValue();
                        parentdataMap.put(entry.getKey(), value[0]);
                    }
                    parentdataMap.put("affectgp", request.getParameter("affectgp") != null);
                    String parentcompanygroupId = group.getID();
                    parentdataMap.put("parentCompanyGroupID", parentcompanygroupId);
                    List childCompaniesList = companyDetailsDAOObj.getChildCompanies(parentcompanyid);
                    String childCompanyName = "";
                    if (!isEdit) {
                        auditID = AuditAction.GROUP_CREATED;
                        for (Object childObj : childCompaniesList) {
                            try {
                                status = txnManager.getTransaction(def);
                                Object[] childdataOBj = (Object[]) childObj;
                                String childCompanyID = (String) childdataOBj[0];
                                childCompanyName = (String) childdataOBj[1];
                                saveAccountGroupInChildCompanies(request, isEdit, parentdataMap, parentcompanyid, childCompanyID);
                                txnManager.commit(status);
                                status = null;
                                auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has propagated(added) account group" + group.getName() + " to child company " + childCompanyName, request, group.getName());
                            } catch (Exception ex) {
                                txnManager.rollback(status);
                                auditTrailObj.insertAuditLog(auditID, "Account group " + group.getName() + " could not be propagated(added) to child company " + childCompanyName, request, group.getName());
                            }
                        }
                    } else {
                        auditID = AuditAction.GROUP_UPDATED;
                        ArrayList<String> filter_names = new ArrayList();
                        ArrayList<String> filter_params = new ArrayList();
                        HashMap<String, Object> rRequestParams = new HashMap<String, Object>();

                        filter_names.add("propagatedgroupid.ID");
                        filter_params.add(group.getID());
                        rRequestParams.put("filter_names", filter_names);
                        rRequestParams.put("filter_params", filter_params);

                        KwlReturnObject result = accAccountDAOobj.getGroup(rRequestParams);
                        List childCompaniesGroupList = result.getEntityList();

                        for (Object childObj : childCompaniesGroupList) {
                            String childcompanysGroupId = "";
                            try {
                                Group groupObj = (Group) childObj;
                                if (groupObj != null) {
                                    childcompanysGroupId = groupObj.getID();
                                    childCompanyName = groupObj.getCompany().getSubDomain();
                                    status = txnManager.getTransaction(def);
                                    String childcompanysgroupid = groupObj.getID();
                                    String childCompanyID = groupObj.getCompany().getCompanyID();
                                    childCompanyName = groupObj.getCompany().getSubDomain();
                                    parentdataMap.put("childgrouptid", childcompanysgroupid);
                                    saveAccountGroupInChildCompanies(request, isEdit, parentdataMap, parentcompanyid, childCompanyID);
                                    txnManager.commit(status);
                                    status = null;
                                    auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has propagated(updated) account group " + groupObj.getName() + " to child company " + childCompanyName, request, childcompanysGroupId);
                                }
                            } catch (Exception ex) {
                                txnManager.rollback(status);
                                auditTrailObj.insertAuditLog(auditID, "Account group" + group.getName() + " could not be propagated(udated) to child company " + childCompanyName, request, childcompanysGroupId);
                            }
                        }

                    }
                } catch (Exception ex) {
                    if (status != null) {
                        txnManager.rollback(status);
                        auditTrailObj.insertAuditLog(auditID, "Account " + group.getName() + " could not be propagated(added) to child company " + group.getCompany().getSubDomain(), request, group.getID());
                    }
                }
            }
             //*****************************************Propagate customer In child companies Ends Here**************************

        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("groupID", groupID);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private void saveAccountGroupInChildCompanies(HttpServletRequest request, boolean isEdit, Map<String, Object> parentDataMap, String parentCompanyid, String childCompanyID) throws DataInvalidateException {
        /*
         fetchColumn - column whose value is fetched from database
         dataColumn - column on which we apply condition
         */
        try {
            JSONObject finalJobj = new JSONObject();
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            List list = null;
            //********************default fields processing*********************************************
            int subModuleFlag = 0;
            //Replaced the multiple arguments of getModuleColumnConfig() with single HashMap object
            HashMap<String, Object> params = new HashMap<String, Object> ();
            params.put("moduleId", String.valueOf(Constants.Group_ModuleId));
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
                                        finalJobj.put(formfieldname, data);
                                    }
                                } catch (Exception ex) {
                                    throw new DataInvalidateException("Combo value not found in child company.");
                                }
                            }
                        } else if (validateType.equalsIgnoreCase("date")) {
                            String data = value.toString();
                            if (!StringUtil.isNullOrEmpty(data)) {
                                Date date = request.getParameter("creationDate") == null ? new Date() : authHandler.getDateFormatter(request).parse(request.getParameter("creationDate"));
                                if (date == null) {
                                    date = new Date();
                                }
                                finalJobj.put(formfieldname, date);
                            }
                        } else if (validateType.equalsIgnoreCase("integer")) {
                            String data = value.toString();
                            int numberValue = 0;
                            if (!StringUtil.isNullOrEmpty(data)) {
                                numberValue = StringUtil.isNullOrEmpty(data) ? 0 : Integer.parseInt(data);
                                finalJobj.put(formfieldname, numberValue);
                            }
                        } else {
                            String dataVal = value.toString();
                            if (!StringUtil.isNullOrEmpty(dataVal)) {
                                finalJobj.put(formfieldname, value);
                            }
                        }

                    }

                }
            }
            //********************default fields processing Ends*********************************************

            String data = "";
            String fetchColumn = "name";
            String conditionColumn = "id";
            KwlReturnObject result = null;

            //****************************Add/Edit child compny's customer************************************
            
            String parentCompanyGroupID = parentDataMap.containsKey("parentCompanyGroupID") ? (String) parentDataMap.get("parentCompanyGroupID") : "";
            finalJobj.put("parentCompanyGroupID", parentCompanyGroupID);
            finalJobj.put("companyid", childCompanyID);
            finalJobj.put("grpOldId", "0");
            String childGroupID = "";
            Group group = null;
            try {
                int dispOrder = 0;
                if (!isEdit) {
                    KwlReturnObject dspresult = accAccountDAOobj.getMaxGroupDisplayOrder();
                    List l = dspresult.getEntityList();
                    if (!l.isEmpty() && l.get(0) != null) {
                        dispOrder = (Integer) l.get(0);
                    }
                    dispOrder++;
                    finalJobj.put("disporder", dispOrder);
                    result = accAccountDAOobj.addGroup(finalJobj);
                } else {
                    String childgrouptid = parentDataMap.containsKey("childgrouptid") ? (String) parentDataMap.get("childgrouptid") : "";
                    finalJobj.put("groupid", childgrouptid);
                    
                    KwlReturnObject grpresult = accountingHandlerDAOobj.getObject(Group.class.getName(), childgrouptid);
                    dispOrder = ((Group) grpresult.getEntityList().get(0)).getDisplayOrder();
                    finalJobj.put("disporder", dispOrder);
                    
                    result = accAccountDAOobj.updateGroup(finalJobj);
                }
                group = (Group) result.getEntityList().get(0);
                childGroupID = group.getID();
            } catch (Exception ex) {
                throw new DataInvalidateException("Group could not be saved.");
            }
            //****************************Add/Edit child compny's customer************************************

        } catch (Exception ex) {
            throw new DataInvalidateException("Error ocurred while saving Group");
        }
    }
    public Group saveGroup(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        Group group = null;
        try {
            String auditMsg = "", auditID = "";

            String companyid = sessionHandlerImpl.getCompanyid(request);
            boolean issub = request.getParameter("subtype") != null;
            String parentid = request.getParameter("parentid");
            String grpOldId = "0";
//            if(!issub){
//                parentid=null;
//            }

            JSONObject groupjson = new JSONObject();
            groupjson.put("companyid", companyid);
            groupjson.put("name", request.getParameter("groupname"));
            if (!StringUtil.isNullOrEmpty(request.getParameter("nature"))) {
                groupjson.put("nature", Integer.parseInt(request.getParameter("nature")));
            }
            groupjson.put("affectgp", request.getParameter("affectgp") != null);
            if (!StringUtil.isNullOrEmpty(parentid)) {
                groupjson.put("parentid", parentid);
            }
            groupjson.put("grpOldId", grpOldId);
            groupjson.put("isCostOfGoodsSoldGroup", !StringUtil.isNullOrEmpty(request.getParameter("isCostOfGoodsSoldGroup"))? Boolean.parseBoolean(request.getParameter("isCostOfGoodsSoldGroup")) : false);
            int dispOrder = 0;
            String groupid = request.getParameter("groupid");
            if (StringUtil.isNullOrEmpty(groupid)) {
                auditMsg = "added";
                auditID = AuditAction.GROUP_CREATED;
                KwlReturnObject dspresult = accAccountDAOobj.getMaxGroupDisplayOrder();
                List l = dspresult.getEntityList();
                if (!l.isEmpty() && l.get(0) != null) {
                    dispOrder = (Integer) l.get(0);
                }
                dispOrder++;
                groupjson.put("disporder", dispOrder);

                KwlReturnObject grpresult = accAccountDAOobj.addGroup(groupjson);
                group = (Group) grpresult.getEntityList().get(0);
                accAccountDAOobj.updateChildrenGroup(group);
            } else {
                auditMsg = "updated";
                auditID = AuditAction.GROUP_UPDATED;
                KwlReturnObject grpresult = accountingHandlerDAOobj.getObject(Group.class.getName(), groupid);
                dispOrder = ((Group) grpresult.getEntityList().get(0)).getDisplayOrder();
                groupjson.put("disporder", dispOrder);
                groupjson.put("groupid", groupid);

                grpresult = accAccountDAOobj.updateGroup(groupjson);
                group = (Group) grpresult.getEntityList().get(0);
                accAccountDAOobj.updateChildrenGroup(group);
            }
            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + auditMsg + " group " + group.getName(), request, group.getID());

        } catch (JSONException ex) {
//            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveGroup : " + ex.getMessage(), ex);
        }
        return group;
    }

    public ModelAndView getGroups(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put("groupid", request.getParameter("groupid"));
            String[] groups = request.getParameterValues("group");
            /*
             * Country id is passed in case of Company set up
             */
            if (!StringUtil.isNullOrEmpty(request.getParameter("country"))) {
                String countryid = request.getParameter("country");
                requestParams.put("country", countryid);
            }
            String[] groupsAfterAdding = groups;
            //To do - need to change below logic as per master type.
//            if (groups != null) {
//                List<String> groupsList = new ArrayList<String>(Arrays.asList(groups));
//                Set groupsSet = new HashSet(Arrays.asList(groups));
//            if (groupsSet.contains(Group.ACCOUNTS_PAYABLE)&&!groupsSet.contains(Group.BILLS_PAYABLE)) {
//                groupsList.add(Group.BILLS_PAYABLE);
//            } else if (groupsSet.contains(Group.CURRENT_ASSETS)&&!groupsSet.contains(Group.CASH)) {
//                groupsList.add(Group.CASH);
//            }
//                groupsAfterAdding=groupsList.toArray(new String[groupsList.size()]);
//            }
            requestParams.put("group", groupsAfterAdding);
            requestParams.put("ignore", request.getParameter("ignore"));
            requestParams.put("ignorecustomers", request.getParameter("ignorecustomers"));
            requestParams.put("ignorevendors", request.getParameter("ignorevendors"));
            requestParams.put("nature", request.getParameterValues("nature"));
            requestParams.put("isMasterGroup", request.getParameter("isMasterGroup"));
            requestParams.put("defaultgroup", request.getParameter("defaultgroup"));
            if (!StringUtil.isNullOrEmpty(request.getParameter("dir")) && !StringUtil.isNullOrEmpty(request.getParameter("sort"))) {
                requestParams.put("dir", request.getParameter("dir"));
                requestParams.put("sort", request.getParameter("sort"));
            }
            String isRevaluation = request.getParameter("isRevaluation");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject result = accAccountDAOobj.getGroups(requestParams);
            List ll = result.getEntityList();
            Iterator itr = ll.iterator();

            JSONArray jArr = new JSONArray();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                Group group = (Group) row[0];
                //T0 do - Pandurang need to change this logic
//                 if (!StringUtil.isNullOrEmpty(isRevaluation)) {
//                    if (!(group.getID().equals(Group.ACCOUNTS_RECEIVABLE) || group.getID().equals(Group.ACCOUNTS_PAYABLE) || group.getID().equals(Group.BANK_ACCOUNT))) {
//                        continue;
//                    }
//                }
                
                //Only for indian company Cost of Goods Sold Account is not need - ERP-20822 ----
//                String Country = (request.getParameter("country") != null ? request.getParameter("country") : "");
//                if (Country.equals(""+Constants.indian_country_id) && group.getName().equals("Cost of Goods Sold")) { 
//                    continue;
//                }// Commented this code for ticket -> ERP-24779
                //-------------------------------------------------------------------------------

                JSONObject obj = new JSONObject();
                obj.put("groupid", group.getID());
                obj.put("groupname", group.getName());
                obj.put("mastergroupid", group.getID());
                obj.put("nature", group.getNature());
                obj.put("naturename", (group.getNature() == Constants.Liability) ? "Liability" : (group.getNature() == Constants.Asset) ? "Asset" : (group.getNature() == Constants.Expences) ? "Expences" : (group.getNature() == Constants.Income) ? "Income" : "");
                obj.put("affectgp", group.isAffectGrossProfit());
                obj.put("isCostOfGoodsSoldGroup", group.isCostOfGoodsSoldGroup());
                obj.put("displayorder", group.getDisplayOrder());
                obj.put("isMasterGroupD", group.isIsMasterGroup());
                obj.put("companyid", (group.getCompany() == null ? null : companyid));
                obj.put("deleted", group.isDeleted());
                Group parentGroup = (Group) row[3];
                obj.put("parentid", parentGroup != null ? parentGroup.getID() : "");
                obj.put("parentname", parentGroup != null ? parentGroup.getName() : "");
                obj.put("level", row[1]);
                obj.put("leaf", row[2]);
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = "accAccountController.getGroups : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView exportGroups(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {

            boolean isForExport = true;
            jobj = controllerService.getGroups(request, isForExport);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public String getMasterGroupID(String groupID) throws ServiceException {
        Map<String, List<String>> masterGroupList = new HashMap<String, List<String>>();
        masterGroupList.put("Asset", Constants.assetGroupList);
        masterGroupList.put("Cost of Goods Sold", Constants.costOfGoodsSoldGroupList);
        masterGroupList.put("Equity", Constants.equityGroupList);
        masterGroupList.put("Expense", Constants.expenseGroupList);
        masterGroupList.put("Income", Constants.incomeGroupList);
        masterGroupList.put("Liability", Constants.liabilityGroupList);
        masterGroupList.put("Other Expense", Constants.otherExpenseGroupList);
        masterGroupList.put("Other Income", Constants.otherIncomeGroupList);

        try {
            //            int i=0;  
            for (Map.Entry<String, List<String>> entry : masterGroupList.entrySet()) {
                String key = entry.getKey();
                List<String> value = entry.getValue();
                for (String aString : value) {
                    if (aString.equalsIgnoreCase(groupID)) {
                        return value.get(0);
                    }
//                      i++;
//                      System.out.println(i+"-> key : " + key + " value : " + aString);
                }
            }
            Group group = (Group) kwlCommonTablesDAOObj.getClassObject(Group.class.getName(), groupID);
            if (group.getParent() != null) {
                return getMasterGroupID(group.getParent().getID());
            } else {
                Group parentGroup = null;
                if (group.getNature() == Constants.Asset) {
                    parentGroup = (Group) kwlCommonTablesDAOObj.getClassObject(Group.class.getName(), Constants.assetGroupList.get(0));
                } else if (group.getNature() == Constants.Expences) {
                    parentGroup = (Group) kwlCommonTablesDAOObj.getClassObject(Group.class.getName(), Constants.expenseGroupList.get(0));
                } else if (group.getNature() == Constants.Income) {
                    parentGroup = (Group) kwlCommonTablesDAOObj.getClassObject(Group.class.getName(), Constants.incomeGroupList.get(0));
                } else {
                    parentGroup = (Group) kwlCommonTablesDAOObj.getClassObject(Group.class.getName(), Constants.liabilityGroupList.get(0));
                }
                group.setParent(parentGroup);
                List<Group> list = accAccountDAOobj.updateParentGroup(group);
                if (!list.isEmpty()) {
                    Group updatedGroup = list.get(0);
                    return getMasterGroupID(updatedGroup.getParent().getID());
                } else {
                    return String.valueOf(group.getNature());
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getMasterGroupID : " + ex.getMessage(), ex);
        }
    }

    public ModelAndView getDefaultAccount(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            String companyType = request.getParameter("companyType");
            String countryId = request.getParameter("country");
            boolean isAdminSubdomain = authHandler.isAdminSubDomain(request);
            String[] nature = request.getParameterValues("nature");
            
            String stateId = request.getParameter("state");
            KwlReturnObject result = accAccountDAOobj.getDefaultAccount(companyType, countryId,stateId, isAdminSubdomain, nature);
            List ll = result.getEntityList();
            Iterator itr = ll.iterator();

            JSONArray jArr = new JSONArray();
            if(!isAdminSubdomain){
            while (itr.hasNext()) {
                DefaultAccount dAccount = (DefaultAccount) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", dAccount.getID());
                obj.put("name", dAccount.getName());
                obj.put("groupname", dAccount.getGroup().getName());
                obj.put("companytype", dAccount.getCompanyType());
                jArr.put(obj);
            }
            }else{
                while (itr.hasNext()) {
                    DefaultAccount dAccount = (DefaultAccount) itr.next();
                    JSONObject obj = new JSONObject();
                    obj.put("accid", dAccount.getID());
                    obj.put("accname", dAccount.getName());
                    obj.put("groupname", dAccount.getGroup().getName());
                    obj.put("companytype", dAccount.getCompanyType());
                    jArr.put(obj);
                }
            }
            
            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (Exception ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = "accAccountController.getDefaultAccount : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView deleteGroup(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = true;
        boolean deleted=false;
        boolean propagateTOChildCompaniesFalg=false;
        try {
            String groupid = request.getParameter("groupid");
            boolean isPermDel=false;
            if(!StringUtil.isNullOrEmpty(request.getParameter("isPermDel"))){
                isPermDel=Boolean.parseBoolean(request.getParameter("isPermDel"));
            }
            KwlReturnObject result = accAccountDAOobj.deleteGroup(groupid,isPermDel);
            String acccode = request.getParameter("acccode");
            //Account Type has been deleted successfully
            msg = result.getMsg();
            msg = messageSource.getMessage(msg, null, RequestContextUtils.getLocale(request));
            if (result.isSuccessFlag()) {
                String perma=isPermDel ? " permanently.":"" ;
                auditTrailObj.insertAuditLog(AuditAction.GROUP_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted group " + acccode+perma, request, groupid);


//                msg = messageSource.getMessage("acc.acc.delGroup", null, RequestContextUtils.getLocale(request));
                issuccess = result.isSuccessFlag();
                deleted=true;
                    //*****************************************delete propagated  group from child companies**************************
                
                if (!StringUtil.isNullOrEmpty(request.getParameter("ispropagatetochildcompanyflag"))) {
                    propagateTOChildCompaniesFalg = Boolean.parseBoolean(request.getParameter("ispropagatetochildcompanyflag"));
                }
                String childCompanyName = "";
                if (propagateTOChildCompaniesFalg) {
                    try {
                        String auditID = AuditAction.GROUP_DELETED;
                        
                        ArrayList<String> filter_names = new ArrayList();
                        ArrayList<String> filter_params = new ArrayList();
                        HashMap<String, Object> rRequestParams = new HashMap<String, Object>();

                        filter_names.add("propagatedgroupid.ID");
                        filter_params.add(groupid);
                        rRequestParams.put("filter_names", filter_names);
                        rRequestParams.put("filter_params", filter_params);

                        KwlReturnObject accountReturnObj = accAccountDAOobj.getGroup(rRequestParams);
                        List childCompaniesGroupList = accountReturnObj.getEntityList();

                            for (Object childObj : childCompaniesGroupList) {
                                String childcompanysGroupId = "";
                                String childGroupName = "";
                                try {
                                    Group group = (Group) childObj;
                                    if (group != null) {
                                        childcompanysGroupId = group.getID();
                                        childCompanyName = group.getCompany().getSubDomain();
                                        
                                        result = accAccountDAOobj.deleteGroup(group.getID(),isPermDel);
                                     
                                        auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted account group " + childGroupName + " from child company " + childCompanyName, request, childcompanysGroupId);
                                    }
                                } catch (Exception ex) {
                                    auditTrailObj.insertAuditLog(auditID, "Account group " + childGroupName + " could not be deleted  from child company " + childCompanyName, request, childcompanysGroupId);
                                }
                            }

                    } catch (Exception ex) {
                       
                    }
                }
            //*****************************************delete Propagated account  from child companies**************************
            }
        } catch (ServiceException ex) {
            issuccess = false;
            deleted=false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            issuccess = false;
            deleted=false;
            msg = "accAccountController.deleteGroup : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("deleted", deleted);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveAccount(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String accountID = "", msg = "";
        Account account=null;
        boolean isEdit=false;
        boolean issuccess = true, isCommitEx = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String accid = request.getParameter("accid");
            if (!StringUtil.isNullOrEmpty(accid)) {
                isEdit = true;
            }
            String mode = request.getParameter("isFixedAsset") == null ? "false" : request.getParameter("isFixedAsset");
            boolean isFixedAsset = Boolean.parseBoolean(mode);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            filter_names.add("company.companyID");
            filter_params.add(companyid);
            filter_names.add("ISdeleted");
            filter_params.add(false);
            filter_names.add("acccode");
            filter_params.add(request.getParameter("acccode"));
//            if(isFixedAsset){
//                filter_names.add("groupname");
//                filter_params.add(Group.FIXED_ASSETS);//12:FIXED_ASSET
//            } else {
//                filter_names.add("NOTINgroupname");
//                filter_params.add("'"+Group.ACCOUNTS_RECEIVABLE+"','"+Group.FIXED_ASSETS+"','"+Group.ACCOUNTS_PAYABLE+"'");//10:CUSTOMER, 12:FIXED_ASSET, 13: VENDOR
//            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("accid"))) {
                filter_names.add("!ID");
                filter_params.add(request.getParameter("accid"));
            }
            requestParams.put("filter_names", filter_names);
            requestParams.put("filter_params", filter_params);
//            if (!StringUtil.isNullOrEmpty(request.getParameter("parentid"))) {
//                KwlReturnObject Count = accAccountDAOobj.getJEDTrasactionfromAccount(request.getParameter("parentid"), companyid);
//                jedCount = Count.getRecordTotalCount();
//            }
            KwlReturnObject result = accAccountDAOobj.getAccount(requestParams);
            if (result.getRecordTotalCount() > 0) {
                issuccess = false;
                //    ERP-18013
                //msg = messageSource.getMessage("acc.field.This", null, RequestContextUtils.getLocale(request)) + (!isFixedAsset ? " account name " : " fixed asset name ") + messageSource.getMessage("acc.field.alreadyexistsPleaseenteradifferentname", null, RequestContextUtils.getLocale(request));
                msg = messageSource.getMessage("acc.field.cannotCreateAccount", null, RequestContextUtils.getLocale(request)); //ERP-35222
//            } else if (jedCount > 0 && !isFixedAsset) {
//                issuccess = false;
//                msg = messageSource.getMessage("acc.field.SelectedParentaccountiscurrentlyusedintransactionPleaseselectanotherparentaccount", null, RequestContextUtils.getLocale(request));
            } else {
                account = saveAccount(request);
                accountID = account.getID();
                msg = (!isFixedAsset ? messageSource.getMessage("acc.coa.account", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage("acc.coa.account.Fixed.Asset", null, RequestContextUtils.getLocale(request))) + " " + messageSource.getMessage("acc.field.hasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
            }
            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                isCommitEx = true;
            }
            //*****************************************Propagate customer In child companies**************************
            if (!isCommitEx) {
                String auditID = "";
                boolean propagateTOChildCompaniesFalg = false;

                if (!StringUtil.isNullOrEmpty(request.getParameter("ispropagatetochildcompanyflag"))) {
                    propagateTOChildCompaniesFalg = Boolean.parseBoolean(request.getParameter("ispropagatetochildcompanyflag"));
                }
                if (propagateTOChildCompaniesFalg) {
                    try {
                        String parentcompanyid = companyid;
                        Map<String, Object> parentdataMap = new HashMap<>();
                        Map<String, Object> requestMap = request.getParameterMap();
                        Set set = requestMap.entrySet();
                        for (Object obj : set) {
                            Map.Entry<String, Object> entry = (Map.Entry<String, Object>) obj;
                            String[] value = (String[]) entry.getValue();
                            parentdataMap.put(entry.getKey(), value[0]);
                        }
                        String parentcompanyAccountId = accountID;
                        parentdataMap.put("parentCompanyAccountID", parentcompanyAccountId);
                        List childCompaniesList = companyDetailsDAOObj.getChildCompanies(parentcompanyid);
                        String childCompanyName = "";
                        if (!isEdit) {
                            auditID = AuditAction.ACCOUNT_CREATED;
                            for (Object childObj : childCompaniesList) {
                                try {
                                    status = txnManager.getTransaction(def);
                                    Object[] childdataOBj = (Object[]) childObj;
                                    String childCompanyID = (String) childdataOBj[0];
                                    childCompanyName = (String) childdataOBj[1];
                                    saveAccountInChildCompanies(request, isEdit, parentdataMap, parentcompanyid, childCompanyID);
                                    txnManager.commit(status);
                                    status = null;
                                    auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has propagated(added) account " + account.getAccountName() + " to child company " + childCompanyName, request, account.getAccountName());
                                } catch (Exception ex) {
                                    txnManager.rollback(status);
                                    auditTrailObj.insertAuditLog(auditID, "Account " + account.getAccountName() + " could not be propagated(added) to child company " + childCompanyName, request, account.getAccountName());
                                }
                            }
                        } else {
                            auditID = AuditAction.ACCOUNT_UPDATED;
                            HashMap<String, Object> requestParams1 = new HashMap<String, Object>();
                            requestParams1.put("propagatedAccountID", parentcompanyAccountId);
                            result = accAccountDAOobj.getPropagatedAccounts(requestParams1);
                            List childCompaniesCustomerList = result.getEntityList();

                            for (Object childObj : childCompaniesCustomerList) {
                                try {
                                    Account acc = (Account) childObj;
                                    if (acc != null) {
                                        status = txnManager.getTransaction(def);
                                        String childcompanyscustomerid = acc.getID();
                                        String childCompanyID = acc.getCompany().getCompanyID();
                                        childCompanyName = acc.getCompany().getSubDomain();
                                        parentdataMap.put("childAccountid", childcompanyscustomerid);
                                        saveAccountInChildCompanies(request, isEdit, parentdataMap, parentcompanyid, childCompanyID);
                                        txnManager.commit(status);
                                        status = null;
                                        auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has propagated(updated) account " + account.getAccountName() + " to child company " + childCompanyName, request, account.getAccountName());
                                    }
                                } catch (Exception ex) {
                                    txnManager.rollback(status);
                                    auditTrailObj.insertAuditLog(auditID, "Account " + account.getAccountName() + " could not be propagated(udated) to child company " + childCompanyName, request, account.getAccountName());
                                }
                            }

                        }
                    } catch (Exception ex) {
                        if (status != null) {
                            txnManager.rollback(status);
                            auditTrailObj.insertAuditLog(auditID, "Account " + account.getAccountName() + " could not be propagated(added) to child company " + account.getCompany().getSubDomain(), request, account.getID());
                        }
                    }
                }
            }
             //*****************************************Propagate customer In child companies Ends Here**************************
            
        } catch (Exception ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("accID", accountID);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView getMVATAnnexureCodeForAccount(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = false;
        String msg = "";
        try {
            boolean isCustomer = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isCustomer"))) {
                isCustomer = Boolean.valueOf(request.getParameter("isCustomer"));
            }
            String moduleid = "";
            if (!StringUtil.isNullOrEmpty(request.getParameter("moduleid"))) {
               moduleid =request.getParameter("moduleid");
            }
            boolean isNoteAlso = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isNoteAlso"))) {
                isNoteAlso = Boolean.valueOf(request.getParameter("isNoteAlso"));
            }
            Map<Integer, String> map = new TreeMap<Integer, String>(IndiaComplianceConstants.MVAT_COA_CODES);
            Map<Integer, String> mapPurchasReturn = new TreeMap<Integer, String>(IndiaComplianceConstants.MVAT_COA_CODES_PURCHASE_RETURN);
            Map<Integer, String> mapDebitNote = new TreeMap<Integer, String>(IndiaComplianceConstants.MVAT_COA_CODES_DEBIT_NOTE);
            Map<Integer, String> mapSalesReturn = new TreeMap<Integer, String>(IndiaComplianceConstants.MVAT_COA_CODES_SALES_RETURN);
            Map<Integer, String> mapCreditNote = new TreeMap<Integer, String>(IndiaComplianceConstants.MVAT_COA_CODES_CREDIT_NOTE);
            Map<Integer, String> treeMap = new TreeMap<Integer, String>();
            treeMap.putAll(map);
            treeMap.putAll(mapPurchasReturn);
            treeMap.putAll(mapDebitNote);
            treeMap.putAll(mapSalesReturn);
            treeMap.putAll(mapCreditNote);
            if (moduleid.equals("" + Constants.Acc_Purchase_Return_ModuleId) || moduleid.equals("" + Constants.Acc_Debit_Note_ModuleId)) {
                treeMap.keySet().removeAll(map.keySet());
                if (isNoteAlso) {
                    treeMap.keySet().removeAll(mapPurchasReturn.keySet());
                } else {
                    treeMap.keySet().removeAll(mapDebitNote.keySet());
                }
                treeMap.keySet().removeAll(mapCreditNote.keySet());
                treeMap.keySet().removeAll(mapSalesReturn.keySet());
            }
            if (moduleid.equals("" + Constants.Acc_Sales_Return_ModuleId) || moduleid.equals("" + Constants.Acc_Credit_Note_ModuleId)) {
                treeMap.keySet().removeAll(map.keySet());
                treeMap.keySet().removeAll(mapDebitNote.keySet());
                treeMap.keySet().removeAll(mapPurchasReturn.keySet());
                if (isNoteAlso) {
                   treeMap.keySet().removeAll(mapSalesReturn.keySet()); 
                } else {
                    treeMap.keySet().removeAll(mapCreditNote.keySet()); 
                }
            }
            for (Integer key : treeMap.keySet()) {
                JSONObject MVATCodeobj = new JSONObject();
                MVATCodeobj.put("mvatannexurecode", key.toString());
                MVATCodeobj.put("mvatdescription", treeMap.get(key));
                jArr.put(MVATCodeobj);
            }
            issuccess = true;
            jobj.put("data", jArr);
        } catch (Exception ex) {
            msg = "accAccountController.getMVATAnnexureCodeForAccount : " + ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accMasterItemsController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

     private void saveAccountInChildCompanies(HttpServletRequest request, boolean isEdit, Map<String, Object> parentDataMap, String parentCompanyid, String childCompanyID) throws DataInvalidateException {
        /*
         fetchColumn - column whose value is fetched from database
         dataColumn - column on which we apply condition
         */
        try {
            JSONObject finalJobj = new JSONObject();
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            List list = null;
            //********************default fields processing*********************************************
            int subModuleFlag = 0;
            //Replaced the multiple arguments of getModuleColumnConfig() with single HashMap object
            HashMap<String, Object> params = new HashMap<String, Object> ();
            params.put("moduleId", Constants.Account_ModuleId);
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
                                        finalJobj.put(formfieldname, data);
                                    }
                                } catch (Exception ex) {
                                    throw new DataInvalidateException("Combo value not found in child company.");
                                }
                            }
                        } else if (validateType.equalsIgnoreCase("date")) {
                            String data = value.toString();
                            if (!StringUtil.isNullOrEmpty(data)) {
                                Date date = request.getParameter("creationDate") == null ? new Date() : authHandler.getDateFormatter(request).parse(request.getParameter("creationDate"));
                                if (date == null) {
                                    date = new Date();
                                }
                                finalJobj.put(formfieldname, date);
                            }
                        } else if (validateType.equalsIgnoreCase("integer")) {
                            String data = value.toString();
                            int numberValue = 0;
                            if (!StringUtil.isNullOrEmpty(data)) {
                                numberValue = StringUtil.isNullOrEmpty(data) ? 0 : Integer.parseInt(data);
                                finalJobj.put(formfieldname, numberValue);
                            }
                        } else {
                            String dataVal = value.toString();
                            if (!StringUtil.isNullOrEmpty(dataVal)) {
                                finalJobj.put(formfieldname, value);
                            }
                        }

                    }

                }
            }
            //********************default fields processing Ends*********************************************
            
            String data = "";
            String fetchColumn = "name";
            String conditionColumn = "id";
              KwlReturnObject result = null;
            if (parentDataMap.containsKey("taxid") && !StringUtil.isNullOrEmpty((String) parentDataMap.get("taxid"))) {
                try {
                    result = importHandler.getTaxbyIDorName(parentCompanyid, fetchColumn, conditionColumn, (String) parentDataMap.get("taxid"));
                    data = (String) result.getEntityList().get(0);

                    result = importHandler.getTaxbyIDorName(childCompanyID, conditionColumn, fetchColumn, data);
                    data = (String) result.getEntityList().get(0);
                    finalJobj.put("taxid", data);
                } catch (Exception ex) {
                    throw new DataInvalidateException("Combo value not found in child company.");
                }
            }
           
            //****************************Add/Edit child compny's customer************************************
            boolean debitType = StringUtil.getBoolean(request.getParameter("debitType"));
            double openBalance = StringUtil.getDouble(request.getParameter("openbalance"));
            openBalance = debitType ? openBalance : -openBalance;
            finalJobj.put("balance", openBalance);
            
            String parentCompanyAccountID = parentDataMap.containsKey("parentCompanyAccountID") ? (String) parentDataMap.get("parentCompanyAccountID") : "";
            finalJobj.put("parentCompanyAccountID", parentCompanyAccountID);
            finalJobj.put("companyid", childCompanyID);
            String childAccountID = "";
            Account account = null;
            try {
                if (!isEdit) {
                    result =accAccountDAOobj.addAccount(finalJobj);
                } else {

                    String childAccountid = parentDataMap.containsKey("childAccountid") ? (String) parentDataMap.get("childAccountid") : "";
                    finalJobj.put("accountid", childAccountid);
                    result = accAccountDAOobj.updateAccount(finalJobj);
                }
                account = (Account) result.getEntityList().get(0);
                childAccountID = account.getID();
            } catch (Exception ex) {
                throw new DataInvalidateException("Account could not be saved.");
            }
            //****************************Add/Edit child compny's customer************************************

 

            //*******************Save Custom Fields Data****************************
            JSONArray jarray = parentDataMap.containsKey("customfield") ? new JSONArray(parentDataMap.get("customfield").toString()) : new JSONArray();
            Map<String, Object> customColumnConfigMap = importHandler.getCustomModuleColumnConfigForSharingMastersData(Constants.Account_ModuleId, childCompanyID, false);

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
                    customrequestParams.put("modulename", Constants.Acc_Account_modulename);
                    customrequestParams.put("moduleprimarykey", Constants.Acc_Accountid);
                    customrequestParams.put("modulerecid", account.getID());
                    customrequestParams.put("moduleid", Constants.Account_Statement_ModuleId);
                    customrequestParams.put("companyid", childCompanyID);
                    customrequestParams.put("customdataclasspath", Constants.Acc_Account_custom_data_classpath);
                    KwlReturnObject customDataresult = fieldDataManager.setCustomData(customrequestParams);
                    if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                        finalJobj.put("accaccountcustomdataref", account.getID());
                        finalJobj.put("accountid", account.getID());
                        KwlReturnObject accresult1 = accAccountDAOobj.updateAccount(finalJobj);
                    }
                } catch (Exception ex) {
                    throw new DataInvalidateException("Error ocurred while saving custom fields data");
                }
            }
            //**************************Save Custom Fields Data End***************************************
        } catch (Exception ex) {
              throw new DataInvalidateException("Error ocurred while saving Customer");
        }
    }
    public Account saveAccount(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        Account account;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject(ExtraCompanyPreferences.class.getName(), companyid);
            String currencyid = (request.getParameter("currencyid") == null ? sessionHandlerImpl.getCurrencyID(request) : request.getParameter("currencyid"));
            
            String customfield = request.getParameter("customfield");
            String distributedopeningbalance = extraCompanyPreferences.isSplitOpeningBalanceAmount() ? request.getParameter("distributedopeningbalance") : null;
            String distributeddeletefield = extraCompanyPreferences.isSplitOpeningBalanceAmount() ? request.getParameter("distributeddeletefield") : null;

            boolean isIBGBank = StringUtil.getBoolean(request.getParameter(Constants.IS_IBG_BANK));
            int ibgbanktype = StringUtil.isNullOrEmpty(request.getParameter("ibgbanktype"))?0: Integer.parseInt(request.getParameter("ibgbanktype"));
            boolean issub = StringUtil.getBoolean(request.getParameter("subaccount"));
            boolean debitType = StringUtil.getBoolean(request.getParameter("debitType"));
            double openBalance = StringUtil.getDouble(request.getParameter("openbalance"));
            double custMinBudget = StringUtil.getDouble(request.getParameter("custminbudget"));
            String ifsccode = StringUtil.isNullOrEmpty(request.getParameter("ifsccode"))?"": request.getParameter("ifsccode");
            openBalance = debitType ? openBalance : -openBalance;
            String parentid = request.getParameter("parentid");
            if (!issub) {
                parentid = null;
            }

            double life = StringUtil.getDouble(request.getParameter("life"));
            double salvage = StringUtil.getDouble(request.getParameter("salvage"));
            double budget = StringUtil.getDouble(request.getParameter("budget"));
            String taxid = request.getParameter("taxid");
            int accounttype = request.getParameter("accounttype") != null ? Integer.parseInt(request.getParameter("accounttype")) : 1;
            int mastertypevalue = request.getParameter("mastertypevalue") != null ? Integer.parseInt(request.getParameter("mastertypevalue")) : 1;
            String acccode = request.getParameter("acccode") != null ?request.getParameter("acccode"):"";
            Date creationDate = authHandler.getGlobalDateFormat().parse(request.getParameter("creationDate"));
            String aliascode = request.getParameter("aliascode") == null ? "" : request.getParameter("aliascode");
            if (creationDate == null) {
                creationDate = new Date();
            }
            String depaccid = request.getParameter("depreciationaccount");
            String accountID = request.getParameter("accid");
            String accountName = request.getParameter("accname");
            String accountDescription = StringUtil.isNullOrEmpty(request.getParameter("accdesc"))?"":request.getParameter("accdesc");
            String parentName = request.getParameter("parentname");
            String purchaseType=StringUtil.isNullOrEmpty(request.getParameter("purchasetype"))?"":request.getParameter("purchasetype");
            String salesType=StringUtil.isNullOrEmpty(request.getParameter("salestype"))?"":request.getParameter("salestype");

            JSONObject accjson = new JSONObject();
            if (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) {
                accjson.put("isFixedAsset", request.getParameter("isFixedAsset"));
                accjson.put("department", request.getParameter("department"));
                accjson.put("location", request.getParameter("location"));
                accjson.put("installation", request.getParameter("installation"));
                accjson.put("isdepreciable", request.getParameter("isdepreciable"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("userid"))){
                accjson.put("userId", request.getParameter("userid"));
            }
            //This field only for INDIA compli. maharashtra state
            if(!StringUtil.isNullOrEmpty(request.getParameter("mvatcode"))){
                accjson.put("mvatcode", request.getParameter("mvatcode"));
            }
            accjson.put("accountid", accountID);
            accjson.put("depaccountid", depaccid);
            accjson.put("name", accountName);
            accjson.put("accounttype", accounttype);
            accjson.put("mastertypevalue", mastertypevalue);
            accjson.put("balance", openBalance);
            accjson.put("minbudget", custMinBudget);
            accjson.put("ifsccode", ifsccode);
            accjson.put("parentid", parentid);
            accjson.put("groupid", request.getParameter("groupid"));
            accjson.put("companyid", companyid);
            accjson.put("currencyid", currencyid);
            accjson.put("life", life);
            accjson.put("salvage", salvage);
            accjson.put("budget", budget);
            accjson.put("taxid", taxid);
            accjson.put("acccode", acccode);
            accjson.put("creationdate", creationDate);
            accjson.put("category", request.getParameter("category"));
            accjson.put("costCenterId", request.getParameter("costcenter"));
            accjson.put("eliminateflag", request.getParameter("eliminateflag") != null && request.getParameter("eliminateflag").equals("on") ? true : false);
            accjson.put("aliascode", aliascode);
            accjson.put("accdesc", accountDescription);
            // ======== Used for INDIA country - (Add in Json)  At SetupWizard Creation/Update ============
            accjson.put("purchaseType", purchaseType);
            accjson.put("salesType", salesType);
            accjson.put("bankbranchname", !StringUtil.isNullOrEmpty(request.getParameter("bankbranchname")) ? request.getParameter("bankbranchname") : "");
            accjson.put("accountno", !StringUtil.isNullOrEmpty(request.getParameter("accountno")) ? request.getParameter("accountno") : "");
            accjson.put("bankbranchaddress", !StringUtil.isNullOrEmpty(request.getParameter("bankbranchaddress")) ? request.getParameter("bankbranchaddress") : "");
            accjson.put("branchstate", !StringUtil.isNullOrEmpty(request.getParameter("branchstate")) ? request.getParameter("branchstate") : "");
            accjson.put("bsrcode", !StringUtil.isNullOrEmpty(request.getParameter("bsrcode")) ? request.getParameter("bsrcode") : 0);
            accjson.put("pincode", !StringUtil.isNullOrEmpty(request.getParameter("pincode")) ? request.getParameter("pincode") : 0);
            // ======== ===================================================================== ============
            accjson.put(Constants.IS_IBG_BANK, isIBGBank);
            accjson.put(Constants.IBG_BANK_TYPE, ibgbanktype);
            String auditMsg = "", auditID = "";
            KwlReturnObject accresult;
            if (StringUtil.isNullOrEmpty(accountID)) {
                auditMsg = "added";
                auditID = AuditAction.ACCOUNT_CREATED;
                accresult = accAccountDAOobj.addAccount(accjson);
            } else {
//                if(accAccountDAOobj.isChild(accountID, parentid)){
//                    throw new AccountingException("\""+accountName+"\" is a parent of \""+parentName+"\" so can't set \""+parentName+"\" as a parent.");
                if (isChildorGrandChild(accountID, parentid)) {
                    throw new AccountingException("\"" + accountName + "\" is a parent of \"" + parentName + "\" so can't set \"" + parentName + "\" as a parent.");

                }
                auditMsg = "updated";
                auditID = AuditAction.ACCOUNT_UPDATED;
                accresult = accAccountDAOobj.updateAccount(accjson);

                if (!isIBGBank) {
                    accAccountDAOobj.deleteIBGBankDetail(accountID, companyid);
                }
            }
            account = (Account) accresult.getEntityList().get(0);
            accAccountDAOobj.updateChildrenAccount(account);

            if (!StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset"))) {
                if (StringUtil.isNullOrEmpty(accountID)) {
                    accountID = account.getID();
                }
                String oldDepartmentId = "";
                String oldLocationId = "";
                String oldUserName = "", newUserName = "";
                if (request.getParameter("oldDepartment") != null) {
                    oldDepartmentId = request.getParameter("oldDepartment");
                }
                String oldDeptName = getAssetLocationORDepartmentName(oldDepartmentId);
                if (request.getParameter("oldLocation") != null) {
                    oldLocationId = request.getParameter("oldLocation");
                }
                String oldLocationName = getAssetLocationORDepartmentName(oldLocationId);

                String oldUserId = request.getParameter("oldUser");
                String newUserId = request.getParameter("userid");
                if (!StringUtil.isNullOrEmpty(oldUserId)) {
                    oldUserName = profileHandlerDAOObj.getUserFullName(oldUserId);
                }
                if (!StringUtil.isNullOrEmpty(newUserId)) {
                    newUserName = profileHandlerDAOObj.getUserFullName(newUserId);
                }
                String newDepartmentId = request.getParameter("department");
                String newDeptName = getAssetLocationORDepartmentName(newDepartmentId);
                String newLocationId = request.getParameter("location");
                String newLocationName = getAssetLocationORDepartmentName(newLocationId);
                String userName = sessionHandlerImpl.getUserFullName(request);
                String ipAddr = getIpAddress(request);
                String userId = sessionHandlerImpl.getUserid(request);
                // maintain history for department
                maintainAssetHistory(userId, ipAddr, accountID, userName, account.getName(), oldDepartmentId, newDepartmentId, oldDeptName, newDeptName, "Department");
                // maintain history for Location
                maintainAssetHistory(userId, ipAddr, accountID, userName, account.getName(), oldLocationId, newLocationId, oldLocationName, newLocationName, "Location");
                // maintain history for User
                maintainAssetHistory(userId, ipAddr, accountID, userName, account.getName(), oldUserId, newUserId, oldUserName, newUserName, "User");

            }
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_Account_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_Accountid);
                customrequestParams.put("modulerecid", account.getID());
                customrequestParams.put("moduleid", Constants.Account_Statement_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_Account_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManager.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    accjson.put("accaccountcustomdataref", account.getID());
                    accjson.put("accountid", account.getID());
                    KwlReturnObject accresult1 = accAccountDAOobj.updateAccount(accjson);
                }
            }
            
            if (extraCompanyPreferences.isSplitOpeningBalanceAmount()) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("companyid", companyid);
                requestParams.put("account", account);
                requestParams.put("debitType", debitType);
                if (!StringUtil.isNullOrEmpty(distributedopeningbalance)) {
                    JSONArray distributedopeningbalancearray = new JSONArray(distributedopeningbalance);
                    requestParams.put("distributedopeningbalancearray", distributedopeningbalancearray);
                }
                if(!StringUtil.isNullOrEmpty(distributeddeletefield)){
                    JSONArray distributeddeletefieldarray = new JSONArray(distributeddeletefield);
                    requestParams.put("distributeddeletefieldarray", distributeddeletefieldarray);
                }
                accAccountDAOobj.distributeOpeningBalance(requestParams);
            }
            
            if (isIBGBank) {
                if (ibgbanktype == Constants.DBS_BANK_Type) {  // DBS bank
                    HashMap<String, Object> ibgBankDetailParams = AccountingManager.getIBGBankDetailParams(request);
                    ibgBankDetailParams.put(Constants.Acc_Accountid, account.getID());
                    ibgBankDetailParams.put(Constants.companyid, companyid);

                    if (StringUtil.isNullOrEmpty(request.getParameter(Constants.IBG_BANK_DETAIL_ID))) {
                        accAccountDAOobj.saveOrupdateIBGBankDetail(ibgBankDetailParams);
                    } else {
                        ibgBankDetailParams.put(Constants.IBG_BANK_DETAIL_ID, request.getParameter(Constants.IBG_BANK_DETAIL_ID));
                        accAccountDAOobj.saveOrupdateIBGBankDetail(ibgBankDetailParams);
                    }
                } else if(ibgbanktype == Constants.CIMB_BANK_Type) {     // CIMB Bank
                    HashMap<String, Object> ibgBankDetailParams = AccountingManager.getCIMBBankDetailParams(request);
                    ibgBankDetailParams.put(Constants.Acc_Accountid, account.getID());
                    ibgBankDetailParams.put(Constants.companyid, companyid);

                    if (StringUtil.isNullOrEmpty(request.getParameter(Constants.CIMB_BANK_DETAIL_ID))) {
                        accAccountDAOobj.saveOrupdateCIMBBankDetail(ibgBankDetailParams);
                    } else {
                        ibgBankDetailParams.put(Constants.CIMB_BANK_DETAIL_ID, request.getParameter(Constants.CIMB_BANK_DETAIL_ID));
                        accAccountDAOobj.saveOrupdateCIMBBankDetail(ibgBankDetailParams);
                    }
                } else if(ibgbanktype == Constants.UOB_Bank){
                    HashMap<String, Object> ibgBankDetailParams = AccountingManager.getUOBBankDetailParams(request);
                    ibgBankDetailParams.put(Constants.Acc_Accountid, account.getID());
                    ibgBankDetailParams.put(Constants.companyid, companyid);
                    if (StringUtil.isNullOrEmpty(request.getParameter(Constants.UOB_BANK_DETAIL_ID))) {
                        accAccountDAOobj.saveOrupdateUOBBankDetail(ibgBankDetailParams);
                    } else {
                        ibgBankDetailParams.put(Constants.UOB_BANK_DETAIL_ID, request.getParameter(Constants.UOB_BANK_DETAIL_ID));
                        accAccountDAOobj.saveOrupdateUOBBankDetail(ibgBankDetailParams);
                    }
                } else if(ibgbanktype == Constants.OCBC_BankType){ //OCBC Bank
                    HashMap<String, Object> ibgBankDetailParams = AccountingManager.getOCBCBankDetailsParams(request);
                    ibgBankDetailParams.put(Constants.Acc_Accountid, account.getID());
                    ibgBankDetailParams.put(Constants.companyid, companyid);
                    accAccountDAOobj.saveOrupdateOCBCBankDetail(ibgBankDetailParams);
                }
            }

            auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + auditMsg + " account " + account.getAcccode(), request, account.getID());
        } catch (JSONException ex) {
//            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveAccount : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
//            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveAccount : " + ex.getMessage(), ex);
        }
        return account;
    }

    public ModelAndView saveIBGBankDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = true, isCommitEx = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);

            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.IBG_BANK_DETAIL_ID))) {
                HashMap<String, Object> ibgBankDetailParams = AccountingManager.getIBGBankDetailParams(request);
                ibgBankDetailParams.put(Constants.IBG_BANK_DETAIL_ID, request.getParameter(Constants.IBG_BANK_DETAIL_ID));
                ibgBankDetailParams.put(Constants.Acc_Accountid, request.getParameter(Constants.Acc_Accountid));
                ibgBankDetailParams.put(Constants.companyid, companyid);

                accAccountDAOobj.saveOrupdateIBGBankDetail(ibgBankDetailParams);
            }

            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                isCommitEx = true;
            }
        } catch (Exception ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView saveCIMBBankDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = true, isCommitEx = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);

            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.IBG_BANK_DETAIL_ID))) {
                HashMap<String, Object> ibgBankDetailParams = AccountingManager.getCIMBBankDetailParams(request);
                ibgBankDetailParams.put(Constants.IBG_BANK_DETAIL_ID, request.getParameter(Constants.IBG_BANK_DETAIL_ID));
                ibgBankDetailParams.put(Constants.Acc_Accountid, request.getParameter(Constants.Acc_Accountid));
                ibgBankDetailParams.put(Constants.companyid, companyid);
                accAccountDAOobj.saveOrupdateCIMBBankDetail(ibgBankDetailParams);
            }

            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                isCommitEx = true;
            }
        } catch (Exception ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /*
     * Method for saving acocunt level details for UOB bank
     */
    public ModelAndView saveUOBBankDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = true, isCommitEx = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);

            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.IBG_BANK_DETAIL_ID))) {
                HashMap<String, Object> ibgBankDetailParams = AccountingManager.getUOBBankDetailParams(request);
                ibgBankDetailParams.put(Constants.IBG_BANK_DETAIL_ID, request.getParameter(Constants.IBG_BANK_DETAIL_ID));
                ibgBankDetailParams.put(Constants.Acc_Accountid, request.getParameter(Constants.Acc_Accountid));
                ibgBankDetailParams.put(Constants.companyid, companyid);
                accAccountDAOobj.saveOrupdateUOBBankDetail(ibgBankDetailParams);
            }

            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                isCommitEx = true;
            }
        } catch (Exception ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public String getIpAddress(HttpServletRequest request) {
        String ipaddr = "";
        if (StringUtil.isNullOrEmpty(request.getHeader("x-real-ip"))) {
            ipaddr = request.getRemoteAddr();
        } else {
            ipaddr = request.getHeader("x-real-ip");
        }
        return ipaddr;
    }

    public void maintainAssetHistory(String userId, String ipAddr, String accountId, String userName, String assetName, String oldId, String newId, String oldName, String newName, String valueItem) {
        String historyMesage = "", historyFinalMsg = "", forOrFromAsset = "";
        if (StringUtil.isNullOrEmpty(oldId)) {
            oldId = "";
        }
        if (StringUtil.isNullOrEmpty(newId)) {
            newId = "";
        }
        try {
            if (!oldId.equals(newId)) {
                if (StringUtil.isNullOrEmpty(oldId)) {
                    historyMesage = " has set " + valueItem + " " + newName;
                    forOrFromAsset = " for Asset ";
                } else if (StringUtil.isNullOrEmpty(newId)) {
                    historyMesage = " has removed " + valueItem + " " + newName;
                    forOrFromAsset = " from Asset ";
                } else {
                    historyMesage = " has changed " + valueItem + " from " + oldName + " to " + newName;
                    forOrFromAsset = " for Asset ";
                }
                historyFinalMsg = "User " + userName + " " + historyMesage + forOrFromAsset + assetName;
                accAccountDAOobj.saveAssetHistory(accountId, historyFinalMsg, new Date(), ipAddr, userId);
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }

    public String getAssetLocationORDepartmentName(String masterItemId) {
        MasterItem masterItem = null;
        String MasterItemName = "";
        try {
            if (!StringUtil.isNullOrEmpty(masterItemId)) {

                masterItem = (MasterItem) kwlCommonTablesDAOObj.getClassObject("com.krawler.hql.accounting.MasterItem", masterItemId);
            }
            if (masterItem != null) {
                MasterItemName = masterItem.getValue();
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            return MasterItemName;
        }
    }

    public ModelAndView exportAccounts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String view = "jsonView_ex";
        try {
            HashMap<String, Object> requestParams = accAccountHandler.getRequestMap(request);
            KwlReturnObject result = accAccountDAOobj.getAccounts(requestParams);
            jobj = accAccountHandler.getAccountJson(request, result.getEntityList(), accCurrencyDAOobj, false);
            String fileType = request.getParameter("filetype");
            if (StringUtil.equal(fileType, "print")) {
                String GenerateDate = authHandler.getDateFormatter(request).format(new Date());
                jobj.put("GenerateDate", GenerateDate);
                view = "jsonView-empty";
            }
            exportDaoObj.processRequest(request, response, jobj);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(view, "model", jobj.toString());
    }

    public ModelAndView importGroups(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String channelName="";
        try {
            String eParams = request.getParameter("extraParams");
            JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);
//            extraParams.put("Company", sessionHandlerImpl.getCompanyid(request));
//            extraParams.put("Currency", sessionHandlerImpl.getCurrencyID(request));
//            extraParams.put("Life", 10.0);
//            extraParams.put("Salvage", 0.0);
            String companyid=sessionHandlerImpl.getCompanyid(request);
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences",companyid );
            
            String doAction = request.getParameter("do");
            HashMap<String, Object> requestParams = importHandler.getImportRequestParams(request);
            requestParams.put("extraParams", extraParams);
            requestParams.put("extraObj", null);
            requestParams.put("servletContext", this.getServletContext());
            if (doAction.compareToIgnoreCase("import") == 0 || doAction.compareToIgnoreCase("xlsImport") == 0) {
                channelName = "/AccountGroupReport/gridAutoRefresh";
                System.out.println("A(( Import start : " + new Date());
                String exceededLimit = request.getParameter("exceededLimit");
                if (exceededLimit.equalsIgnoreCase("yes")) { //If file contains records more than 1500 then Import file in background using thread
                    String logId = importHandler.addPendingImportLog(requestParams);
                    requestParams.put("logId", logId);
                    importHandler.add(requestParams);
                    if (!importHandler.isIsWorking()) {
                        Thread t = new Thread(importHandler);
                        t.start();
                    }
                    jobj.put("success", true);
                } else {
                     if (extraPref != null) {
                        requestParams.put("allowropagatechildcompanies", extraPref.isPropagateToChildCompanies());
                    }
                     jobj = importHandler.importFileData(requestParams);

                    if (extraPref != null && extraPref.isPropagateToChildCompanies()) {
                        try {
                            List childCompaniesList = companyDetailsDAOObj.getChildCompanies(companyid);
                            requestParams.put("childcompanylist", childCompaniesList);
                            requestParams.put("extraParams", extraParams);
                            requestParams.put("parentcompanyID", companyid);
                            importHandler.add(requestParams);
                            if (!importHandler.isIsWorking()) {
                                Thread t = new Thread(importHandler);
                                t.start();
                            }
                        } catch (Exception ex) {
                             Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                   
                }
                jobj.put("exceededLimit", exceededLimit);
                System.out.println("A(( Import end : " + new Date());
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                System.out.println("A(( Validation start : " + new Date());
                jobj = importHandler.validateFileData(requestParams);
                System.out.println("A(( Validation end : " + new Date());
                jobj.put("success", true);
            }
            issuccess=true;
        } catch (Exception ex) {
            issuccess=false;
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", "" + ex.getMessage());
            } catch (JSONException jex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, jex);
            }
        } finally{
            if (issuccess && !StringUtil.isNullOrEmpty(channelName)) {
                ServerEventManager.publish(channelName, jobj.toString(), (ServletContext) this.getServletContext());
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView importAccounts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            String eParams = request.getParameter("extraParams");
            JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);
            extraParams.put("Company", sessionHandlerImpl.getCompanyid(request));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            
            boolean updateExistingRecordFlag = false;
            if(!StringUtil.isNullOrEmpty(request.getParameter("updateExistingRecordFlag"))){
                updateExistingRecordFlag = Boolean.FALSE.parseBoolean(request.getParameter("updateExistingRecordFlag"));
            }
             ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", companyid);
             String baseUrl =com.krawler.common.util.URLUtil.getPageURL(request,loginpageFull,extraPref.getCompany().getSubDomain());
//            extraParams.put("Currency", sessionHandlerImpl.getCurrencyID(request));
            extraParams.put("Life", 10.0);
            extraParams.put("Salvage", 0.0);
            SimpleDateFormat sdf=new  SimpleDateFormat("yyyy-MM-dd");
            Date bbDate=null;
            if(extraParams.get("bookBeginningDate")!=null){
                bbDate=sdf.parse(extraParams.get("bookBeginningDate").toString());
            }
            boolean isBookClosed = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isBookClosed"))) {
                isBookClosed = Boolean.parseBoolean(request.getParameter("isBookClosed"));
            }
            String doAction = request.getParameter("do");
            HashMap<String, Object> requestParams = importHandler.getImportRequestParams(request);
            requestParams.put("extraParams", extraParams);
            requestParams.put("extraObj", null);
            requestParams.put("servletContext", this.getServletContext());
            requestParams.put("companyid", companyid);
            requestParams.put("moduleid", Constants.Account_ModuleId);
            requestParams.put("isBookClosed", isBookClosed);
            requestParams.put("locale", RequestContextUtils.getLocale(request));
            int countryId = 0;
            if (extraPref != null) {
                requestParams.put("isCurrencyCode", extraPref.isCurrencyCode());
                requestParams.put("isActivateIBG", extraPref.isActivateIBG());
                if(extraPref.getCompany() != null && extraPref.getCompany().getCountry()!=null && !StringUtil.isNullObject(extraPref.getCompany().getCountry().getID())){
                    countryId = Integer.parseInt(extraPref.getCompany().getCountry().getID());
            }
            }
            requestParams.put("countryid", countryId);
            requestParams.put("bookBeginningDate", bbDate);
            requestParams.put("baseUrl", baseUrl);
            if(updateExistingRecordFlag){
                requestParams.put("allowDuplcateRecord", updateExistingRecordFlag);
                requestParams.put("updateExistingRecordFlag", updateExistingRecordFlag);
            }
            if (doAction.compareToIgnoreCase("import") == 0 || doAction.compareToIgnoreCase("xlsImport") == 0) {
                requestParams.put("updateExistingRecordFlag", updateExistingRecordFlag);
                System.out.println("A(( Import start : " + new Date());
                String exceededLimit = request.getParameter("exceededLimit");
                if (exceededLimit.equalsIgnoreCase("yes")) { //If file contains records more than 1500 then Import file in background using thread
                    String logId = importHandler.addPendingImportLog(requestParams);
                    requestParams.put("logId", logId);
                    importHandler.add(requestParams);
                    if (!importHandler.isIsWorking()) {
                        Thread t = new Thread(importHandler);
                        t.start();
                    }
                    jobj.put("success", true);
                } else {
                   
                    if (extraPref != null) {
                        requestParams.put("allowropagatechildcompanies", extraPref.isPropagateToChildCompanies());
                    }
                    
                    jobj = importHandler.importFileData(requestParams);
                    
                     //*****************This code is written for propagating Accounts to child companies*************************
                    if (extraPref != null && extraPref.isPropagateToChildCompanies()) {
                        try {
                            List childCompaniesList = companyDetailsDAOObj.getChildCompanies(companyid);
                            requestParams.put("childcompanylist", childCompaniesList);
                            requestParams.put("parentcompanyID", companyid);
                            importHandler.add(requestParams);
                            if (!importHandler.isIsWorking()) {
                                Thread t = new Thread(importHandler);
                                t.start();
                            }
                        } catch (Exception ex) {
                             Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }
                    
                //*****************End - propagating Accounts to child companies*************************
                }
                jobj.put("exceededLimit", exceededLimit);
                System.out.println("A(( Import end : " + new Date());
            } else if (doAction.compareToIgnoreCase("validateData") == 0) {
                System.out.println("A(( Validation start : " + new Date());
                jobj = importHandler.validateFileData(requestParams);
                System.out.println("A(( Validation end : " + new Date());
                jobj.put("success", true);
            }
        } catch (Exception ex) {
            try {
                jobj.put("success", false);
                jobj.put("msg", "" + ex.getMessage());
            } catch (JSONException jex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, jex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView importDefaultAccounts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            String eParams = request.getParameter("extraParams");
            JSONObject extraParams = StringUtil.isNullOrEmpty(eParams) ? new JSONObject() : new JSONObject(eParams);
//            extraParams.put("Company", sessionHandlerImpl.getCompanyid(request));
            extraParams.put("Currency", sessionHandlerImpl.getCurrencyID(request));
            extraParams.put("Life", 5.0);
            extraParams.put("PresentValue", 0.0);
            extraParams.put("Salvage", 0.0);

//            jobj = importHandler.importCSVFile(request, extraParams, null);
        } catch (Exception ex) {
            try {
                jobj.put("success", false);
                jobj.put("msg", "" + ex.getMessage());
            } catch (JSONException jex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, jex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getAccountsByCategory(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("start", request.getParameter("start"));
            requestParams.put("limit", request.getParameter("limit"));

            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("company.companyID");
            filter_params.add(sessionHandlerImpl.getCompanyid(request));
            filter_names.add("ISdeleted");
            filter_params.add(false);
            if (request.getParameter("group") != null) {
                filter_names.add("group.ID");
                filter_params.add(request.getParameter("group"));
            }
            requestParams.put("filter_names", filter_names);
            requestParams.put("filter_params", filter_params);
            order_by.add("category");
            order_type.add("desc");
            requestParams.put("order_by", order_by);
            requestParams.put("order_type", order_type);

            KwlReturnObject result = accAccountDAOobj.getAccount(requestParams);
            jobj = getAccountsByCategoryJson(request, result.getEntityList());

            jobj.put("count", result.getRecordTotalCount());
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    // method moved to main controller. now it is in no more use.
    public JSONObject getAccountsByCategoryJson(HttpServletRequest request, List list) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Account account = (Account) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("accid", account.getID());
                obj.put("accname", account.getName());
                obj.put("groupid", account.getGroup().getID());
                obj.put("groupname", account.getGroup().getName());
                obj.put("nature", account.getGroup().getNature());
                obj.put("openbalance", account.getOpeningBalance());
            //    obj.put("depreciationaccount", account.getDepreciationAccont() == null ? "" : account.getDepreciationAccont().getID());

                obj.put("currencyid", (account.getCurrency() == null ? "" : account.getCurrency().getCurrencyID()));
                obj.put("currencysymbol", (account.getCurrency() == null ? "" : account.getCurrency().getSymbol()));
                obj.put("currencyname", (account.getCurrency() == null ? "" : account.getCurrency().getName()));
                obj.put("presentbalance", account.getPresentValue());
                obj.put("custminbudget", account.getCustMinBudget());
                obj.put("life", account.getLife());
                obj.put("salvage", account.getSalvage());
                obj.put("deleted", account.isDeleted());
                obj.put("creationDate", authHandler.getUserDateFormatterWithoutTimeZone(request).format(account.getCreationDate()));
                obj.put("category", account.getCategory() == null ? "" : account.getCategory().getValue());
                obj.put("categoryid", account.getCategory() == null ? "" : account.getCategory().getID());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getAccountsByCategoryJson : " + ex.getMessage(), ex);
        }
        return jobj;
    }

    public boolean isChildorGrandChild(String accountID, String parentid) throws ServiceException {
        try {
            List Result = accAccountDAOobj.isChildorGrandChild(parentid);
            Iterator iterator = Result.iterator();
            if (iterator.hasNext()) {
                Object ResultObj = iterator.next();
                Account ResultParentac = (Account) ResultObj;
                ResultParentac = ResultParentac.getParent();
                if (ResultParentac == null) {
                    return false;
                } else {
                    String Resultparent = ResultParentac.getID();
                    if (Resultparent.equals(accountID)) {
                        return true;
                    } else {
                        return isChildorGrandChild(accountID, Resultparent);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("isChildorGrandChild : " + ex.getMessage(), ex);
        }
        return false;
    }

    public ModelAndView getMonthlyBudget(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobjaccount = new JSONObject();
        JSONArray jArr = new JSONArray();
        String msg = "";
        boolean issuccess = false;
        try {
            String accountid = request.getParameter("accountidstr");
            String accountIdArray[] = accountid.split(",");
            String dimensionvalue = request.getParameter("dimensionvalue");
            String dimensionId = request.getParameter("dimensionid");
            JSONObject paramJObj = StringUtil.convertRequestToJsonObject(request); 
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));

            int year = request.getParameter("year") != null ? Integer.parseInt(request.getParameter("year")) : 1970;
            if (year != 0) {
                requestParams.put("year", year);
            }
         
            // taking filter on the basis of dimension and dimension value
            if (!StringUtil.isNullOrEmpty(dimensionId) && !dimensionId.equalsIgnoreCase(AccountBudget.DIMENSION_ALL)) {
                requestParams.put("dimension", dimensionId);
                if (!StringUtil.isNullOrEmpty(dimensionvalue)) {
                    requestParams.put("dimensionvalue", dimensionvalue);
                }
            }
                if (!StringUtil.isNullOrEmpty(accountid)) {
                    requestParams.put("accountvalue", accountid.substring(0, accountid.lastIndexOf(",")));
                }

                KwlReturnObject result = accAccountDAOobj.getMonthlyBudget(requestParams);
                List<AccountBudget> accountList = result.getEntityList();
                List list = result.getEntityList();

                //if list is there it will fetch the value of dimension details
                if (list.size() > 0) {
                    jArr = getMonthlyBudgetDimensionJson(accountList, paramJObj);
                } else {
                    //else it will call existing funstion if list is not there
                    for (int acounidCnt = 0; acounidCnt < accountIdArray.length; acounidCnt++) {
                        jobjaccount = getMonthlyBudgetJson(list, paramJObj, accountIdArray[acounidCnt]);
                        jArr.put(jobjaccount);
                    }
                }
            jobj.put("data", jArr);
            jobj.put("count", jArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONArray getMonthlyBudgetDimensionJson(List<AccountBudget> list, JSONObject paramJobj) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            if (!list.isEmpty()) {
                for (AccountBudget accbudget : list) {
                    JSONObject obj = new JSONObject();
                    obj.put("id", accbudget.getID());
                    obj.put("accountid", accbudget.getAccount().getID());
                    obj.put("accountname", accbudget.getAccount().getName());
                    obj.put("year", accbudget.getYear());
                    if (!StringUtil.isNullOrEmpty(accbudget.getDimension())) {
                        
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldParams.class.getName(), accbudget.getDimension());
                        FieldParams fieldparamsObj = (FieldParams) rdresult.getEntityList().get(0);
                        if (fieldparamsObj != null) {
                            obj.put("dimensionname", fieldparamsObj.getFieldlabel());
                            obj.put("dimensionid",accbudget.getDimension());
                        } else {
                            obj.put("dimensionname", "");
                            obj.put("dimensionid", "");
                        }
                    } else {
                        obj.put("dimensionname", "");
                        obj.put("dimensionid", "");
                    }
                    if (!StringUtil.isNullOrEmpty(accbudget.getDimensionValue())) {
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), accbudget.getDimensionValue());
                        FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                        if (fieldComboData != null) {
                            obj.put("dimensionvaluename", fieldComboData.getValue());
                            obj.put("dimensionvalueid", accbudget.getDimensionValue());
                        } else {
                            obj.put("dimensionvaluename", "");
                            obj.put("dimensionvalueid", "");
                        }
                    } else {
                        obj.put("dimensionvaluename", "");
                        obj.put("dimensionvalueid", "");
                    }
                    obj.put("jan", (accbudget.getJan() < 0) ? "" : accbudget.getJan());
                    obj.put("feb", (accbudget.getFeb() < 0) ? "" : accbudget.getFeb());
                    obj.put("march", (accbudget.getMarch() < 0) ? "" : accbudget.getMarch());
                    obj.put("april", (accbudget.getApril() < 0) ? "" : accbudget.getApril());
                    obj.put("may", (accbudget.getMay() < 0) ? "" : accbudget.getMay());
                    obj.put("june", (accbudget.getJune() < 0) ? "" : accbudget.getJune());
                    obj.put("july", (accbudget.getJuly() < 0) ? "" : accbudget.getJuly());
                    obj.put("aug", (accbudget.getAug() < 0) ? "" : accbudget.getAug());
                    obj.put("sept", (accbudget.getSept() < 0) ? "" : accbudget.getSept());
                    obj.put("oct", (accbudget.getOct() < 0) ? "" : accbudget.getOct());
                    obj.put("nov", (accbudget.getNov() < 0) ? "" : accbudget.getNov());
                    obj.put("dec", (accbudget.getDecember() < 0) ? "" : accbudget.getDecember());
                    jArr.put(obj);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getMonthlyBudgetDimensionJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public JSONObject getMonthlyBudgetJson(List list, JSONObject paramJObj, String accountId) throws ServiceException {
        JSONObject obj = new JSONObject();
        try {
            if (!list.isEmpty()) {
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    AccountBudget accbudget = (AccountBudget) itr.next();
                    obj.put("id", accbudget.getID());
                    obj.put("accountid", accbudget.getAccount().getID());
                    obj.put("accountname", accbudget.getAccount().getName());
                    obj.put("year", accbudget.getYear());
                    if (!StringUtil.isNullOrEmpty(accbudget.getDimension())) {

                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldParams.class.getName(), accbudget.getDimension());
                        FieldParams fieldparamsObj = (FieldParams) rdresult.getEntityList().get(0);
                        if (fieldparamsObj != null) {
                            obj.put("dimensionname", fieldparamsObj.getFieldlabel());
                            obj.put("dimensionid", accbudget.getDimension());
                        } else {
                            obj.put("dimensionname", "");
                            obj.put("dimensionid", "");
                        }
                    } else {
                        obj.put("dimensionname", "");
                        obj.put("dimensionid", "");
                    }
                    if (!StringUtil.isNullOrEmpty(accbudget.getDimensionValue())) {
                        KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), accbudget.getDimensionValue());
                        FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                        if (fieldComboData != null) {
                            obj.put("dimensionvaluename", fieldComboData.getValue());
                            obj.put("dimensionvalueid", accbudget.getDimensionValue());
                        } else {
                            obj.put("dimensionvaluename", "");
                            obj.put("dimensionvalueid", "");
                        }
                    } else {
                        obj.put("dimensionvaluename", "");
                        obj.put("dimensionvalueid", "");
                    }
                    obj.put("jan", (accbudget.getJan() < 0) ? "" : accbudget.getJan());
                    obj.put("feb", (accbudget.getFeb() < 0) ? "" : accbudget.getFeb());
                    obj.put("march", (accbudget.getMarch() < 0) ? "" : accbudget.getMarch());
                    obj.put("april", (accbudget.getApril() < 0) ? "" : accbudget.getApril());
                    obj.put("may", (accbudget.getMay() < 0) ? "" : accbudget.getMay());
                    obj.put("june", (accbudget.getJune() < 0) ? "" : accbudget.getJune());
                    obj.put("july", (accbudget.getJuly() < 0) ? "" : accbudget.getJuly());
                    obj.put("aug", (accbudget.getAug() < 0) ? "" : accbudget.getAug());
                    obj.put("sept", (accbudget.getSept() < 0) ? "" : accbudget.getSept());
                    obj.put("oct", (accbudget.getOct() < 0) ? "" : accbudget.getOct());
                    obj.put("nov", (accbudget.getNov() < 0) ? "" : accbudget.getNov());
                    obj.put("dec", (accbudget.getDecember() < 0) ? "" : accbudget.getDecember());

                }
            } else {
                String accountName = "";
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                if (!StringUtil.isNullOrEmpty(accountId)) {
                    filter_names.add("ID");
                    filter_params.add(accountId);
                }
                requestParams.put("filter_names", filter_names);
                requestParams.put("filter_params", filter_params);
                KwlReturnObject result = accAccountDAOobj.getAccount(requestParams);
                list = result.getEntityList();

                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    Object listObj = itr.next();
                    Account account = (Account) listObj;
                    accountName = account.getName();
                }
                obj.put("id", AccountBudget.BLANK_ROW_ID);
                obj.put("accountid", accountId);
                obj.put("accountname", accountName);
                obj.put("jan", "");
                obj.put("feb", "");
                obj.put("march", "");
                obj.put("april", "");
                obj.put("may", "");
                obj.put("june", "");
                obj.put("july", "");
                obj.put("aug", "");
                obj.put("sept", "");
                obj.put("oct", "");
                obj.put("nov", "");
                obj.put("dec", "");
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getMonthlyBudgetJson : " + ex.getMessage(), ex);
        }
        return obj;
    }
    
    public ModelAndView getMonthlyForecast(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobjaccount = new JSONObject();
        JSONArray jArr = new JSONArray();
        String msg = "";
        boolean issuccess = false;
        try {
            String accountid = request.getParameter("accountidstr");
            String accountIdArray[] = accountid.split(",");
            for (int acounidCnt = 0; acounidCnt < accountIdArray.length; acounidCnt++) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
                if (!StringUtil.isNullOrEmpty(accountid)) {
                    requestParams.put("accountid", accountIdArray[acounidCnt]);
                }

                KwlReturnObject result = accAccountDAOobj.getMonthlyForecast(requestParams);
                List list = result.getEntityList();

                jobjaccount = getMonthlyForecastJson(list, request, accountIdArray[acounidCnt]);
                jArr.put(jobjaccount);
            }
            jobj.put("data", jArr);
            jobj.put("count", jArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getMonthlyForecastJson(List list, HttpServletRequest request, String accountId) throws ServiceException {
        JSONArray jArr = new JSONArray();
        JSONObject obj = new JSONObject();
        try {
            if (!list.isEmpty()) {
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    AccountForecast accountForecast = (AccountForecast) itr.next();
                    obj.put("id", accountForecast.getID());
                    obj.put("accountid", accountForecast.getAccount().getID());
                    obj.put("accountname", accountForecast.getAccount().getName());
                    obj.put("jan", (accountForecast.getJan() < 0) ? "" : accountForecast.getJan());
                    obj.put("feb", (accountForecast.getFeb() < 0) ? "" : accountForecast.getFeb());
                    obj.put("march", (accountForecast.getMarch() < 0) ? "" : accountForecast.getMarch());
                    obj.put("april", (accountForecast.getApril() < 0) ? "" : accountForecast.getApril());
                    obj.put("may", (accountForecast.getMay() < 0) ? "" : accountForecast.getMay());
                    obj.put("june", (accountForecast.getJune() < 0) ? "" : accountForecast.getJune());
                    obj.put("july", (accountForecast.getJuly() < 0) ? "" : accountForecast.getJuly());
                    obj.put("aug", (accountForecast.getAug() < 0) ? "" : accountForecast.getAug());
                    obj.put("sept", (accountForecast.getSept() < 0) ? "" : accountForecast.getSept());
                    obj.put("oct", (accountForecast.getOct() < 0) ? "" : accountForecast.getOct());
                    obj.put("nov", (accountForecast.getNov() < 0) ? "" : accountForecast.getNov());
                    obj.put("dec", (accountForecast.getDecember() < 0) ? "" : accountForecast.getDecember());

                }
            } else {
                String accountName = "";
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                if (!StringUtil.isNullOrEmpty(accountId)) {
                    filter_names.add("ID");
                    filter_params.add(accountId);
                }
                requestParams.put("filter_names", filter_names);
                requestParams.put("filter_params", filter_params);
                KwlReturnObject result = accAccountDAOobj.getAccount(requestParams);
                list = result.getEntityList();

                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    Object listObj = itr.next();
                    Account account = (Account) listObj;
                    accountName = account.getName();
                }
                obj.put("id", "0");
                obj.put("accountid", accountId);
                obj.put("accountname", accountName);
                obj.put("jan", "");
                obj.put("feb", "");
                obj.put("march", "");
                obj.put("april", "");
                obj.put("may", "");
                obj.put("june", "");
                obj.put("july", "");
                obj.put("aug", "");
                obj.put("sept", "");
                obj.put("oct", "");
                obj.put("nov", "");
                obj.put("dec", "");
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getMonthlyBudgetJson : " + ex.getMessage(), ex);
        }
        return obj;
    }

    public ModelAndView saveUpdateMonthlyBudget(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, JSONException {
        JSONObject jobj = new JSONObject();
        JSONObject jobjaccount = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = true, isCommitEx = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        JSONObject paramJObj = StringUtil.convertRequestToJsonObject(request); 
        String msg = "";
        try {
            String jsonData = paramJObj.optString("jsondata");
            String dimensionvalue = paramJObj.optString("dimensionvalue");
            String dimensionId = paramJObj.optString("dimensionid");
            boolean isDimensionFlag = false;

            //if dimensionvalue is present and dimension id is not All
            if (!StringUtil.isNullOrEmpty(dimensionvalue) && !dimensionId.equalsIgnoreCase(AccountBudget.DIMENSION_ALL)) {
                isDimensionFlag = true;
            }
            
            //audit Trial
            Map<String, Object> auditRequestParams = new HashMap<String, Object>();
            auditRequestParams.put(Constants.reqHeader, paramJObj.getString(Constants.reqHeader));
            auditRequestParams.put(Constants.remoteIPAddress, paramJObj.getString(Constants.remoteIPAddress));
            auditRequestParams.put(Constants.useridKey, paramJObj.getString(Constants.useridKey));
            
            JSONArray jsonArray = null;
            int year = paramJObj.optString("year",null) != null ? Integer.parseInt(paramJObj.optString("year","1970")) : 1970;
            jsonArray = new JSONArray(jsonData);
            HashMap<String, Object> requestParams = new HashMap<String, Object>(); 
            requestParams.put("year", year);
            
            //saving dimension with values if given
            if (isDimensionFlag) {
                String dimensionvalueIDArray[] = dimensionvalue.split(",");
                if (dimensionvalueIDArray.length > 0) {
                    JSONObject returnJObj = saveDimensionValuesMonthlyBudget(paramJObj, jsonArray, auditRequestParams);
                }
            }
            
            if (!isDimensionFlag) {// existing code will be called if no dimension is given
                for (int jsonCnt = 0; jsonCnt < jsonArray.length(); jsonCnt++) {

                    jobjaccount = (JSONObject) jsonArray.get(jsonCnt);
                    //if dimension field is added initially when dimension is given
                    if (!StringUtil.isNullOrEmpty(dimensionId)&& !dimensionId.equalsIgnoreCase("All")&& jsonArray.length()==1 && jobjaccount.optString("id").equalsIgnoreCase(AccountBudget.BLANK_ROW_ID)) {
                        jobjaccount.put("dimension", dimensionId);
                    }
                    KwlReturnObject result = accAccountDAOobj.addMonthlyBudget(jobjaccount, year);
                    auditTrailObj.insertAuditLog(AuditAction.MONTHLYBUDGET, "User " + paramJObj.optString(Constants.userfullname) + " has set monthly budget to account " + jobjaccount.getString("accountname"), auditRequestParams,jobjaccount.getString("id"));
                }
            }

            issuccess = true;
            msg = messageSource.getMessage("acc.field.MonthlyBudgetforaccountaddedsuccessfully", null, RequestContextUtils.getLocale(request));
            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                isCommitEx = true;
            }
        } catch (Exception ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
        
   /*Case: When DImension is selected and then value is inserted */
    public JSONObject saveDimensionBasedMonthlyBudget(JSONObject paramjObj, HashMap<String, Object> requestParams,
            JSONArray jsonArray, String dimensionId, Map<String, Object> auditRequestParams) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = true;
        JSONObject jobjaccount = new JSONObject();
        int year = paramjObj.optString("year", null) != null ? Integer.parseInt(paramjObj.optString("year", "1970")) : 1970;
        try {

            for (int jsonCnt = 0; jsonCnt < jsonArray.length(); jsonCnt++) {
                jobjaccount = (JSONObject) jsonArray.get(jsonCnt);
                JSONObject saveJobj = jobjaccount;
                requestParams = new HashMap<String, Object>();
                requestParams.put("companyid", paramjObj.optString(Constants.companyKey));

                if (!StringUtil.isNullOrEmpty(dimensionId)) {
                    requestParams.put("dimension", dimensionId);
                    saveJobj.put("dimension", dimensionId);
                }


                if (!StringUtil.isNullOrEmpty(jobjaccount.optString("accountid", null))) {
                    requestParams.put("accountid", jobjaccount.optString("accountid"));
                }
                List<AccountBudget> list = new ArrayList<AccountBudget>();
                boolean isAuditTrialEntry = false;
                
                //when record is already present when dimension is given then we need to check whether new audit trial entry needs to be done.
                //If record is not present then it will make an entry in database
                if (!StringUtil.isNullOrEmpty(saveJobj.optString("id", null))) {
                    requestParams.put("id", saveJobj.optString("id"));
                    KwlReturnObject result = accAccountDAOobj.getMonthlyBudget(requestParams);
                    list = result.getEntityList();
                }
                 if (list.isEmpty()) {
                        saveJobj.remove("id");
                        isAuditTrialEntry = true;
                    }

                    KwlReturnObject resultmonthlybudgetObj = accAccountDAOobj.addMonthlyBudget(saveJobj, year);
                    if (!resultmonthlybudgetObj.getEntityList().isEmpty() && resultmonthlybudgetObj.getEntityList().size() > 0) {
                        AccountBudget accbudget = (AccountBudget) resultmonthlybudgetObj.getEntityList().get(0);
                        if (isAuditTrialEntry) {
                            auditTrailObj.insertAuditLog(AuditAction.MONTHLYBUDGET, "User " + paramjObj.optString(Constants.userfullname) + " has set monthly budget to account " + jobjaccount.getString("accountname"), auditRequestParams, accbudget.getID());
                        }
                    }
            }
        } catch (ServiceException ex) {
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            issuccess = false;
            msg = "accAccountController.saveDimensionBasedMonthlyBudget : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
    
    
    //saving into database along with dimension and dimension values
    public JSONObject saveDimensionValuesMonthlyBudget(JSONObject paramjObj,JSONArray jsonArray, Map<String, Object> auditRequestParams) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = true;
        JSONObject jobjaccount = new JSONObject();
        int year = paramjObj.optString("year", null) != null ? Integer.parseInt(paramjObj.optString("year", "1970")) : 1970;
        try {
            Set<String> accountidSet = new HashSet<String>();
            Set<String> dimensionValueSet = new HashSet<String>();
            Set<String> savedDimensionValueSet = new HashSet<String>();//flag to keep track whether any dimension values are saved already or not.
            StringBuilder accountStringBuilder = new StringBuilder();
            String dimensionTempId = null;
            String comboBoxDimensionId = paramjObj.optString("dimensionid");
            //combo box dimension values selected
            String dimensionvalue = paramjObj.optString("dimensionvalue");
            if (!StringUtil.isNullOrEmpty(dimensionvalue)) {
                String dimensionvalueIDArray[] = dimensionvalue.split(",");
                if (dimensionvalueIDArray.length > 0) {
                    for (int j = 0; j < dimensionvalueIDArray.length; j++) {
                        if (!StringUtil.isNullOrEmpty(dimensionvalueIDArray[j].toString())) {
                            dimensionValueSet.add(dimensionvalueIDArray[j].toString());
                        }
                    }
                }
            }
            //Grid dimnsionvalues saved iteration
            for (int jsonCnt = 0; jsonCnt < jsonArray.length(); jsonCnt++) {
                jobjaccount = (JSONObject) jsonArray.get(jsonCnt);
                if (!StringUtil.isNullOrEmpty(jobjaccount.optString("accountid", null))) {
                    accountidSet.add(jobjaccount.optString("accountid"));
                }
                if (!StringUtil.isNullOrEmpty(jobjaccount.optString("dimensionvalueid"))) {
                    savedDimensionValueSet.add(jobjaccount.optString("dimensionvalueid"));
                }
                if (!StringUtil.isNullOrEmpty(jobjaccount.optString("dimensionid"))) {
                    dimensionTempId = jobjaccount.optString("dimensionid");
                }
            }
            String accounValue = null;
            for (String accountString : accountidSet) {
                accountStringBuilder.append(accountString + ",");
            }
            accounValue = accountStringBuilder.toString().substring(0, accountStringBuilder.toString().length() - 1);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", paramjObj.optString(Constants.companyKey));

            if (!StringUtil.isNullOrEmpty(dimensionTempId)) {
                requestParams.put("dimension", dimensionTempId);
            }
            if (!StringUtil.isNullOrEmpty(dimensionvalue)) {
                requestParams.put("dimensionvalue", dimensionvalue);
            }

            if (!StringUtil.isNullOrEmpty(accounValue)) {
                requestParams.put("accountvalue", accounValue);
            }
            
            //fetching all the dimension values record by applying filter dimension id and dimension value
            KwlReturnObject result = accAccountDAOobj.getMonthlyBudget(requestParams);
            List<AccountBudget> accountBudgetList = result.getEntityList();
            Set<String> updatedDimensionValues = new HashSet<>();
            if (!accountBudgetList.isEmpty()) {
                for (AccountBudget ab : accountBudgetList) {
                    for (int jsonCnt = 0; jsonCnt < jsonArray.length(); jsonCnt++) {
                        jobjaccount = (JSONObject) jsonArray.get(jsonCnt);
                        JSONObject saveJobj = jobjaccount;

                        //If two dimension values are selected suppose A & B. Record is only present for A and not for B, then it will update the particular row  dimension A.
                        //It checks whether it has already dimensionvalueid or not. If yes then it updates else it doen't execute
                        //case when more than one dimen values are selected. Out of which one is already present in Grid
                        
                        if (jobjaccount.has("dimensionvalueid") && jobjaccount.get("dimensionvalueid") != null && !StringUtil.isNullOrEmpty(jobjaccount.optString("dimensionvalueid", null))) {
                            String savedDimensionValue = jobjaccount.optString("dimensionvalueid");
                            if (savedDimensionValue.equalsIgnoreCase(ab.getDimensionValue())) {
                                updatedDimensionValues.add(ab.getDimensionValue());
                                saveJobj.put("id", ab.getID());
                                KwlReturnObject resultmonthlybudgetObj = accAccountDAOobj.addMonthlyBudget(saveJobj, year);
                            }
                        }
                    }
                }
            }//end of update of account update

            //keeping track whether some values has been updated or not and removing the already updated dimension values
            if (updatedDimensionValues.size() > 0) {
                dimensionValueSet.removeAll(updatedDimensionValues);
            }
            
            //make an entry for each account for each dimension value
            if (!savedDimensionValueSet.isEmpty()) {
                for (String tempDim : dimensionValueSet) {
                    for (String accountString : accountidSet) {//for each account
                        JSONObject saveAccBud = new JSONObject();
                        saveAccBud.put("dimensionvalue", tempDim);
                        saveAccBud.put("dimension", dimensionTempId);
                        saveAccBud.put("accountid", accountString);
                        saveAccBud.put("jan", "");
                        saveAccBud.put("feb", "");
                        saveAccBud.put("march", "");
                        saveAccBud.put("april", "");
                        saveAccBud.put("may", "");
                        saveAccBud.put("june", "");
                        saveAccBud.put("july", "");
                        saveAccBud.put("aug", "");
                        saveAccBud.put("sept", "");
                        saveAccBud.put("oct", "");
                        saveAccBud.put("nov", "");
                        saveAccBud.put("dec", "");
                        KwlReturnObject resultmonthlybudgetObj = accAccountDAOobj.addMonthlyBudget(saveAccBud, year);
                        if (!resultmonthlybudgetObj.getEntityList().isEmpty() && resultmonthlybudgetObj.getEntityList().size() > 0) {
                            AccountBudget accbudget = (AccountBudget) resultmonthlybudgetObj.getEntityList().get(0);
                            auditTrailObj.insertAuditLog(AuditAction.MONTHLYBUDGET, "User " + paramjObj.optString(Constants.userfullname) + " has set monthly budget to account " + jobjaccount.getString("accountname"), auditRequestParams, accbudget.getID());
                        }
                    }
                }
            }
            
            //when inserting new dimension values initially then it will delete the exisiting dimensionid values that is present in db and update a new entry with dimension values 
            if (savedDimensionValueSet.isEmpty()) {
                boolean isDimensionflag = false;

                HashMap<String, Object> reqParams = new HashMap<String, Object>();
                reqParams.put("companyid", paramjObj.optString(Constants.companyKey));

                if (!StringUtil.isNullOrEmpty(comboBoxDimensionId)) {
                    reqParams.put("dimension", comboBoxDimensionId);
                }
                if (!StringUtil.isNullOrEmpty(accounValue)) {
                    reqParams.put("accountvalue", accounValue);
                }
                result = accAccountDAOobj.getMonthlyBudget(reqParams);
                //checking an entry for dimensionvalue for the selected dimension.If entry is there then it will create a new record
                List<AccountBudget> aBList = result.getEntityList();
                if (!aBList.isEmpty()) {
                    for (AccountBudget ab : aBList) {
                        String dimensionval = ab.getDimensionValue();
                        if (!StringUtil.isNullOrEmpty(dimensionval)) {
                            isDimensionflag = true;
                            break;
                        }
                    }
                }
                //if dimension value is already present and now updating dimension values for that account
                if (!isDimensionflag) {
                    reqParams= new HashMap<String, Object>();
                    if (!StringUtil.isNullOrEmpty(accounValue)) {
                        reqParams.put("accountvalue", accounValue);
                    }
                    KwlReturnObject deleteResult = accAccountDAOobj.deleteMonthlyBudget(null, comboBoxDimensionId, year,reqParams);
                    int numRows = deleteResult.getRecordTotalCount();
                }
            }
            //If record is already present for dimension id in database and now we have selected dimension value and Grid is having only one blank row, then it
            // should delete the entry of dimension id and make an new entry for new dimension values that are selected in combobox 
            if (savedDimensionValueSet.isEmpty() && jsonArray.length() == 1) {
//            if (!savedDimensionValueSet.isEmpty()) {
                for (int jsonCnt = 0; jsonCnt < jsonArray.length(); jsonCnt++) {
                    jobjaccount = (JSONObject) jsonArray.get(jsonCnt);
//                    if (jobjaccount.has("id") && jobjaccount.get("id") != null) {
//                        KwlReturnObject deleteResult = accAccountDAOobj.deleteMonthlyBudget(jobjaccount.optString("id"),null,year);
//                        int numRows = deleteResult.getRecordTotalCount();
//                    }
                    for (String tempDim : dimensionValueSet) {
                        for (String accountString : accountidSet) {
                            JSONObject saveAccBud = jobjaccount;
                            saveAccBud.remove("id");
                            saveAccBud.put("accountid", accountString);
                            if (!StringUtil.isNullOrEmpty(comboBoxDimensionId)) {
                                saveAccBud.put("dimension", comboBoxDimensionId);
                            }
                            saveAccBud.put("dimensionvalue", tempDim);
                            
                            KwlReturnObject resultmonthlybudgetObj = accAccountDAOobj.addMonthlyBudget(saveAccBud, year);
                            if (!resultmonthlybudgetObj.getEntityList().isEmpty() && resultmonthlybudgetObj.getEntityList().size() > 0) {
                                AccountBudget accbudget = (AccountBudget) resultmonthlybudgetObj.getEntityList().get(0);
                                auditTrailObj.insertAuditLog(AuditAction.MONTHLYBUDGET, "User " + paramjObj.optString(Constants.userfullname) + " has set monthly budget to account " + jobjaccount.getString("accountname"), auditRequestParams, accbudget.getID());
                            }
                        }
                    }
                }
            }
        } catch (ServiceException ex) {
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            issuccess = false;
            msg = "accAccountController.saveDimensionBasedMonthlyBudget : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
    
    public ModelAndView deleteMonthlyBudget(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = true;

        try {
            String recordId = request.getParameter("recordId");
            KwlReturnObject result = accAccountDAOobj.deleteMonthlyBudget(recordId,null,AccountBudget.BLANK_YEAR,new HashMap<String, Object>());

            msg = messageSource.getMessage("acc.acc.deleteMonthlyBudget", null, RequestContextUtils.getLocale(request));
            issuccess = result.isSuccessFlag();
        } catch (ServiceException ex) {
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            issuccess = false;
            msg = "accAccountController.deleteMonthlyBudget : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView deleteMonthlyForecast(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = true;

        try {
            String recordId = request.getParameter("recordId");
            KwlReturnObject result = accAccountDAOobj.deleteMonthlyForecast(recordId);

            msg = messageSource.getMessage("acc.acc.deleteMonthlyForecast", null, RequestContextUtils.getLocale(request));
            issuccess = result.isSuccessFlag();
        } catch (ServiceException ex) {
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            issuccess = false;
            msg = "accAccountController.deleteMonthlyForecast : " + ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveUpdateAccountMapping(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobjaccount = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = true, isCommitEx = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);

        String msg = "";
        try {

            String jsonData = request.getParameter("jsondata");
            JSONArray jsonArray = null;
            jsonArray = new JSONArray(jsonData);
            for (int jsonCnt = 0; jsonCnt < jsonArray.length(); jsonCnt++) {

                jobjaccount = (JSONObject) jsonArray.get(jsonCnt);
                KwlReturnObject result = accAccountDAOobj.saveUpdateAccountMapping(jobjaccount);

            }
            issuccess = true;
            msg = messageSource.getMessage("acc.field.Accountmappingforcompanyhasbeensavedsuccessfully", null, RequestContextUtils.getLocale(request));
            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                isCommitEx = true;
            }
        } catch (Exception ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView deleteAccountMapping(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true, isCommitEx = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        String msg = "";
        try {
            String mappingid = request.getParameter("mappingid");
            KwlReturnObject result = accAccountDAOobj.deleteAccountMapping(mappingid);
            issuccess = true;
            msg = messageSource.getMessage("acc.field.Accountmappingforselectrecordhasbeendeletedsuccessfully", null, RequestContextUtils.getLocale(request));
            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                isCommitEx = true;
            }
        } catch (Exception ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveAccountMapPnL(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = true;
        boolean isCommitEx = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            saveAccountMapPnL(request);
            msg = messageSource.getMessage("acc.field.Mappingupdatedsuccessfully", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                isCommitEx = false;
            }
        } catch (Exception ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public Account saveAccountMapPnL(HttpServletRequest request) throws ServiceException {
        Account account = null;
        Templatepnl templatepnl = null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String templateid = request.getParameter("templateid");
            String templatename = request.getParameter("templatename");

            String[] incomeaccounts = request.getParameterValues("assetArray");
            String[] expenseaccounts = request.getParameterValues("liabilityArray");


            //Update template information
            if (!StringUtil.isNullOrEmpty(templateid)) {
                accAccountDAOobj.deleteAccountMapPnL(templateid, companyid);

                Map<String, Object> PnLTemplate = new HashMap<String, Object>();

                PnLTemplate.put("id", templateid);
                PnLTemplate.put("name", templatename);
                PnLTemplate.put("companyid", companyid);
                KwlReturnObject result = accAccountDAOobj.updatePnLTemplate(PnLTemplate);

                templatepnl = (Templatepnl) result.getEntityList().get(0);
            } else {
                Map<String, Object> PnLTemplate = new HashMap<String, Object>();

                PnLTemplate.put("name", templatename);
                boolean isAdminSubdomain = false;
                String countryid = "";
                int templatecode = accAccountDAOobj.getMaxTemplateId(companyid, isAdminSubdomain, countryid);
                PnLTemplate.put("templateid", templatecode);
                PnLTemplate.put("companyid", companyid);
                KwlReturnObject result = accAccountDAOobj.updatePnLTemplate(PnLTemplate);

                templatepnl = (Templatepnl) result.getEntityList().get(0);
            }

            //Update mapping information
            for (int i = 0; i < incomeaccounts.length; i++) {
                String accountid = incomeaccounts[i];
                HashMap<String, Object> PnLAccMap = new HashMap<String, Object>();
                PnLAccMap.put("templateid", templatepnl.getTemplateid());
                PnLAccMap.put("isincome", 0);
                PnLAccMap.put("accountid", accountid);
                PnLAccMap.put("companyid", companyid);

                accAccountDAOobj.saveAccountMapPnL(PnLAccMap);
                accAccountDAOobj.updateAccountTemplateCode(accountid, companyid);
            }

            for (int i = 0; i < expenseaccounts.length; i++) {
                String accountid = expenseaccounts[i];
                HashMap<String, Object> PnLAccMap = new HashMap<String, Object>();
                PnLAccMap.put("templateid", templatepnl.getTemplateid());
                PnLAccMap.put("isincome", 1);
                PnLAccMap.put("accountid", expenseaccounts[i]);
                PnLAccMap.put("companyid", companyid);

                accAccountDAOobj.saveAccountMapPnL(PnLAccMap);
                accAccountDAOobj.updateAccountTemplateCode(accountid, companyid);
            }

        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("saveAccountMapPnL : " + ex.getMessage(), ex);
        }
        return account;
    }

    public ModelAndView getAssetTransferHistory(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        JSONArray storeArray = new JSONArray();
        try {
            storeArray = getAssetTransferHistory(request);
            jobj.put("data", storeArray);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            issuccess = false;
        } catch (JSONException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            issuccess = false;
        } finally {
            try {
                jobj.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getAssetTransferHistory(HttpServletRequest request) throws ServiceException, JSONException, SessionExpiredException {
        JSONArray returnArray = new JSONArray();
        String assetId = request.getParameter("assetId");
        KwlReturnObject resultObject = null;
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        requestParams.put("start", StringUtil.checkForNull(request.getParameter("start")));
        requestParams.put("limit", StringUtil.checkForNull(request.getParameter("limit")));
        requestParams.put("assetId", StringUtil.checkForNull(request.getParameter("assetId")));
        if (!StringUtil.isNullOrEmpty(assetId)) {
            resultObject = resultObject = accAccountDAOobj.getAssetHistory(requestParams);
        }
        Iterator listIterator = resultObject.getEntityList().iterator();
        while (listIterator.hasNext()) {
            JSONObject jObj = new JSONObject();
            AssetHistory assetHistory = (AssetHistory) listIterator.next();
            jObj.put("details", assetHistory.getDetails());
            jObj.put("audittime", authHandler.getDateOnlyFormat(request).format(assetHistory.getAuditTime()));
            jObj.put("ipAddress", assetHistory.getIPAddress());
            jObj.put("user", assetHistory.getUser().getUserLogin().getUserName() + " [ " + sessionHandlerImpl.getUserFullName(request) + " ]");
            returnArray.put(jObj);
        }
        return returnArray;
    }

    public ModelAndView getInvoiceTermsSales(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        JSONArray jarr = new JSONArray();
        try {
            HashMap<String, String> termNameID = new HashMap();
            termNameID.put("Basic", "Basic");
            String companyid = sessionHandlerImpl.getCompanyid(request);            
            HashMap hashMap = new HashMap();
            boolean autoLoadInvoiceTermTaxes = false;
            hashMap.put("companyid", companyid);
            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
            String columnPref = extraCompanyPreferences.getColumnPref();
            JSONObject columnprefObj = new JSONObject();
            if (!StringUtil.isNullOrEmpty(columnPref)) {
                columnprefObj = new JSONObject(columnPref);
            }
            if (columnprefObj.has(Constants.autoLoadInvoiceTermTaxes) && columnprefObj.get(Constants.autoLoadInvoiceTermTaxes) != null && (Boolean) columnprefObj.get(Constants.autoLoadInvoiceTermTaxes) != false) {
                autoLoadInvoiceTermTaxes = true;
            }
            if (request.getParameter("isSalesOrPurchase") != null) {
                hashMap.put("salesOrPurchaseFlag", request.getParameter("isSalesOrPurchase"));
            }
             if (request.getParameter("showActiveDeactiveTerms") != null) {
                hashMap.put("showActiveDeactiveTerms", request.getParameter("showActiveDeactiveTerms"));
            }
            if (request.getParameter("isCopy") != null) {
                hashMap.put("isCopy", request.getParameter("isCopy"));
            }
            KwlReturnObject result = accAccountDAOobj.getInvoiceTerms(hashMap);
            List<InvoiceTermsSales> list = result.getEntityList();
            for (InvoiceTermsSales mt : list) {
                JSONObject jsonobj = new JSONObject();
                /*
                * ERP-40242 : Show only activated taxes in create and copy case and all taxes in edit cases
                */
                HashMap<String, Object> linkedTermTaxParams = new HashMap<>();
                linkedTermTaxParams.put("termid", mt.getId());                
                linkedTermTaxParams.put(Constants.includeDeactivatedTax, false);
                
                jsonobj.put("id", mt.getId());
                if (autoLoadInvoiceTermTaxes) {
                    JSONObject linkedObj = controllerService.getLinkedTermTax(linkedTermTaxParams);
                    jsonobj.put("linkedTaxMapping", linkedObj);
                }
                jsonobj.put("term", mt.getTerm());
                termNameID.put(mt.getId(), mt.getTerm());
                jsonobj.put("glaccount", mt.getAccount().getID());
                jsonobj.put("glaccountname", !StringUtil.isNullOrEmpty(mt.getAccount().getName()) ? mt.getAccount().getName() : "");
                jsonobj.put("accode", !StringUtil.isNullOrEmpty(mt.getAccount().getAcccode()) ? mt.getAccount().getAcccode() : "");
                jsonobj.put("sign", mt.getSign());
                jsonobj.put("category", mt.getCategory());
                jsonobj.put("includegst", mt.getIncludegst());
                jsonobj.put("includeprofit", mt.getIncludeprofit());
                jsonobj.put("formula", mt.getFormula());
                jsonobj.put("formulaids", mt.getFormula());
                jsonobj.put("termformulaids", mt.getFormulaids());
                jsonobj.put("suppressamnt", mt.getSupressamount());
//                jsonobj.put("taxCheck", "true");
                jsonobj.put("termid", mt.getId());
                jsonobj.put("termpercentage", mt.getPercentage());
                jsonobj.put("termtype", mt.getTermType());
                jsonobj.put("purchasevalueorsalevalue", mt.getPurchaseValueOrSaleValue());
                jsonobj.put("deductionorabatementpercent", mt.getDeductionOrAbatementPercent());
                jsonobj.put("taxtype", mt.getTaxType());
                jsonobj.put("taxvalue", mt.getTaxType()==0 ? mt.getTermAmount() : mt.getPercentage());
                jsonobj.put("isDefault", mt.isIsDefault());
                jsonobj.put("termsequence", mt.getTermSequence());
                jsonobj.put("istermused", accAccountDAOobj.checkTermusedInTransaction(mt.getId(),mt.isSalesOrPurchase()));
                jsonobj.put("includeInTDSCalculation", mt.isIncludeInTDSCalculation());
                jsonobj.put("isTermActive", mt.isIsTermActive());
                jsonobj.put("hasAccess", mt.isIsTermActive());

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
            jobj.put("data", jarr);
            jobj.put("count", result.getRecordTotalCount());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            issuccess = false;
        } catch (ServiceException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            issuccess = false;
        } finally {
            try {
                jobj.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getLinkedTermTax(HttpServletRequest request, HttpServletResponse response) {
        JSONObject returnJobj = new JSONObject();
        boolean issuccess = false;
        try {
            String termid = request.getParameter("termid") != null ? request.getParameter("termid"): "" ;
            HashMap<String, Object>  requestParams = new HashMap<>();
            requestParams.put("termid", termid);
            /*
             * ERP-40242 : For malaysian company, only show actiavated taxes and for edit case, show all taxes.
             */
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.includeDeactivatedTax))) {
                requestParams.put(Constants.includeDeactivatedTax, Boolean.parseBoolean(request.getParameter(Constants.includeDeactivatedTax)));
            }

            
            returnJobj = controllerService.getLinkedTermTax(requestParams);
            issuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            issuccess = false;
        } finally {
            try {
                returnJobj.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", returnJobj.toString());
    }
    public ModelAndView getIndianTermsCompanyLevel(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        JSONArray jarr = new JSONArray();
        try {
            HashMap<String, String> termNameID = new HashMap();
            termNameID.put("Basic", "Basic");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap hashMap = new HashMap();
            hashMap.put("companyid", companyid);
            if (request.getParameter("isSalesOrPurchase") != null) {
                hashMap.put("salesOrPurchaseFlag", request.getParameter("isSalesOrPurchase"));
            }
            if (request.getParameter("isAdditionalTax") != null) {
                hashMap.put("isAdditionalTax", request.getParameter("isAdditionalTax"));
            }
            String termType="";
            if (!StringUtil.isNullOrEmpty(request.getParameter("termType"))) {
                termType= request.getParameter("termType").toString();
            }
            boolean isAdditionalTax = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isAdditionalTax"))) {
                isAdditionalTax = Boolean.parseBoolean(request.getParameter("isAdditionalTax"));
            }
            boolean isNewGST = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("isNewGST"))) {
                isNewGST = Boolean.parseBoolean(request.getParameter("isNewGST"));
            }
            KwlReturnObject result = null;
            if (!isNewGST) {
           result = accAccountDAOobj.getIndianTermsCompanyLevel(hashMap);
            List<LineLevelTerms> list = result.getEntityList();
            for (LineLevelTerms mt : list) { 
                JSONObject jsonobj = new JSONObject();
                termNameID.put(mt.getId(), mt.getTerm());
                if (!StringUtil.isNullOrEmpty(request.getParameter("isAdditionalTax"))) {
                    if (isAdditionalTax) {
                        if (!mt.isIsAdditionalTax()) {
                            continue;
                        }
                    } else {
                        if (mt.isIsAdditionalTax()) {
                            continue;
                        }
                    }
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("termType"))) {
                    boolean serviceTax=true;
                    if(termType.equals("4") && (mt.getTermType()==4 ||mt.getTermType()==5 || mt.getTermType()==6)){//serviceTax - Include both cess 
                      serviceTax =false;
                    }
                    if (Integer.parseInt(termType) != mt.getTermType() && serviceTax) {
                        continue;
                    }
                }
                jsonobj.put("id", mt.getId());
                jsonobj.put("term", mt.getTerm());
                
                jsonobj.put("glaccount", mt.getAccount().getID());
                jsonobj.put("glaccountname", !StringUtil.isNullOrEmpty(mt.getAccount().getName()) ? mt.getAccount().getName() : "");
                if (mt.getPayableAccount() != null) {
                    jsonobj.put("payableaccountid", mt.getPayableAccount() != null ? mt.getPayableAccount().getID() : "");
                    jsonobj.put("payableglaccountname", !StringUtil.isNullOrEmpty(mt.getPayableAccount().getName()) ? mt.getPayableAccount().getName() : "");
                }
                if (mt.getCreditNotAvailedAccount() != null) {
                    jsonobj.put("creditnotavailedaccount", mt.getCreditNotAvailedAccount() != null ? mt.getCreditNotAvailedAccount().getID() : "");
                    jsonobj.put("creditnotavailedaccountname", !StringUtil.isNullOrEmpty(mt.getCreditNotAvailedAccount().getName()) ? mt.getCreditNotAvailedAccount().getName() : "");
                }
                jsonobj.put("accode", !StringUtil.isNullOrEmpty(mt.getAccount().getAcccode()) ? mt.getAccount().getAcccode() : "");
                jsonobj.put("sign", mt.getSign());
                jsonobj.put("formula", mt.getFormula());
                jsonobj.put("formulaids", mt.getFormula());
                jsonobj.put("termformulaids", mt.getFormulaids());
                jsonobj.put("termid", mt.getId());
                jsonobj.put("termpercentage", mt.getPercentage());
                jsonobj.put("termtype", mt.getTermType());
                jsonobj.put("purchasevalueorsalevalue", mt.getPurchaseValueOrSaleValue());
                jsonobj.put("deductionorabatementpercent", mt.getDeductionOrAbatementPercent());
                jsonobj.put("taxtype", mt.getTaxType());
                jsonobj.put("taxvalue", mt.getTaxType()==0 ? mt.getTermAmount() : mt.getPercentage());
                jsonobj.put("isDefault", mt.isIsDefault());
                jsonobj.put("termsequence", mt.getTermSequence());
                jsonobj.put("formType", !StringUtil.isNullOrEmpty(mt.getFormType())?mt.getFormType():"1");
                jsonobj.put("isadditionaltax", mt.isIsAdditionalTax());
                jsonobj.put("includeInTDSCalculation", mt.isIncludeInTDSCalculation());
                jsonobj.put("masteritem", mt.getMasteritem() == null ? "" : mt.getMasteritem().getID());
                jsonobj.put("IsOtherTermTaxable", mt.isOtherTermTaxable());
                jsonobj.put("isTermTaxable", mt.isOtherTermTaxable());
                jarr.put(jsonobj);
            }
        }
            if (jarr.length() > 0) {
                for (int cnt = 0; cnt < jarr.length(); cnt++) {
                    JSONObject jsonobj = jarr.getJSONObject(cnt);
                    if(jsonobj.has("formula")){
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
            }
            jobj.put("data", jarr);
            jobj.put("count", result!=null?result.getRecordTotalCount():0);
            issuccess = true;
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            issuccess = false;
        } finally {
            try {
                jobj.put("success", issuccess);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONArray getInvoiceTerms(HttpServletRequest request) throws JSONException, SessionExpiredException {
        JSONArray jarr = new JSONArray();
        try {
            HashMap<String, String> termNameID = new HashMap();
            termNameID.put("Basic", "Basic");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap hashMap = new HashMap();
            hashMap.put("companyid", companyid);
            if (request.getParameter("isSalesOrPurchase") != null) {
                hashMap.put("salesOrPurchaseFlag", request.getParameter("isSalesOrPurchase"));
            }
            KwlReturnObject result = accAccountDAOobj.getInvoiceTerms(hashMap);
            List<InvoiceTermsSales> list = result.getEntityList();
            for (InvoiceTermsSales mt : list) {
                JSONObject jsonobj = new JSONObject();
                jsonobj.put("id", mt.getId());
                jsonobj.put("term", mt.getTerm());
                termNameID.put(mt.getId(), mt.getTerm());
                jsonobj.put("glaccount", mt.getAccount().getID());
                jsonobj.put("accountid", mt.getAccount().getID());
                jsonobj.put("glaccountname", !StringUtil.isNullOrEmpty(mt.getAccount().getName()) ? mt.getAccount().getName() : "");
                jsonobj.put("accode", !StringUtil.isNullOrEmpty(mt.getAccount().getAcccode()) ? mt.getAccount().getAcccode() : "");
                jsonobj.put("sign", mt.getSign());
                jsonobj.put("category", mt.getCategory());
                jsonobj.put("includegst", mt.getIncludegst());
                jsonobj.put("includeprofit", mt.getIncludeprofit());
                jsonobj.put("formula", mt.getFormula());
                jsonobj.put("formulaids", mt.getFormula());
                jsonobj.put("suppressamnt", mt.getSupressamount());
//                jsonobj.put("taxCheck", "true");
                jsonobj.put("isDefault", mt.isIsDefault());
                jsonobj.put("termid", mt.getId());
                jsonobj.put("termformulaids", mt.getFormulaids());
                jsonobj.put("termpercentage", mt.getPercentage());
                jsonobj.put("termtype", mt.getTermType());
                jsonobj.put("termsequence", mt.getTermSequence());
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
        } catch (JSONException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return jarr;
        }
    }

    public ModelAndView saveInvoiceTermsSales(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            InvoiceTermsSales intTerm = saveInvoiceTermsSales(request);
            issuccess = true;
            txnManager.commit(status);
            ExtraCompanyPreferences cmpPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            if (cmpPref.isActivateCRMIntegration() && "true".equals(request.getParameter("isSalesOrPurchase"))) {
                saveInvoiceTermsInCRM(intTerm);
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /*
    *Method to set term status Active/Deactive
    *
    */
       public ModelAndView setInvoiceTermsSalesActive(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
             if(!Boolean.parseBoolean(request.getParameter("isTermActive"))) {
                String termid = request.getParameter("termId");
                boolean isTermUsed = false;
                String moduleNames = "";
                // check term is used in formula or not
                KwlReturnObject result = accAccountDAOobj.findTermUsedInFormula(termid);
                if (result.getRecordTotalCount() > 0) {
                    isTermUsed = true;
                    moduleNames += "Term Formula, ";
                }

                if (isTermUsed) {
                    moduleNames = moduleNames.substring(0, moduleNames.length() - 2);
                    throw new AccountingException(messageSource.getMessage("acc.field.SelectedTermisusedinModules.SocannotbeDeactivated", new Object[]{moduleNames}, RequestContextUtils.getLocale(request)));
                }
            }
            JSONObject paramJObj = StringUtil.convertRequestToJsonObject(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            paramJObj.put("companyid", companyid);
            jobj = controllerService.setInvoiceTermsSalesActive(paramJObj);
            issuccess = true;

        }catch (Exception ex) {
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
  
    public void saveInvoiceTermsInCRM(InvoiceTermsSales intTerm) {
        try {
            String companyid = intTerm.getCompany().getCompanyID();
            //Session session = null;
            JSONArray termDataInArray = new JSONArray();
            JSONObject termdata = new JSONObject();
            termdata.put("creator", intTerm.getCreator().getUserID());
            termdata.put("term", intTerm.getTerm());
            termdata.put("glaccount", intTerm.getAccount().getID());
            termdata.put("formulaids", intTerm.getFormula());
            termdata.put("category", intTerm.getCategory());
            termdata.put("includegst", intTerm.getIncludegst());
            termdata.put("includeprofit", intTerm.getIncludeprofit());
            termdata.put("sign", intTerm.getSign());
            termdata.put("suppressamnt", intTerm.getSupressamount());
            termdata.put("creationdate", intTerm.getCreatedOn());
            termdata.put("id", intTerm.getId());
            termdata.put("salesorpurchase", intTerm.isSalesOrPurchase());
            termDataInArray.put(termdata);

//            String crmURL = this.getServletContext().getInitParameter("crmURL");
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("companyid", companyid);
            userData.put("termdata", termDataInArray);
            //session = HibernateUtil.getCurrentSession();
//            String action = "222";
            String crmURL = URLUtil.buildRestURL(Constants.crmURL);
            crmURL = crmURL + "master/invoiceterm";
            apiCallHandlerService.restPostMethod(crmURL, userData.toString());
//            apiCallHandlerService.callApp(crmURL, userData, companyid, action);  //just given a call
        } catch (Exception e) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public InvoiceTermsSales saveInvoiceTermsSales(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        InvoiceTermsSales invTerm = null;
        boolean isDuplicateTerm = false;
        try {
            String term = request.getParameter("term");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            
            HashMap termMap = new HashMap();
            termMap.put("companyid", companyid);
            termMap.put("term", term);
            if (request.getParameter("isSalesOrPurchase") != null) {
                termMap.put("salesOrPurchaseFlag", request.getParameter("isSalesOrPurchase"));
            }
            isDuplicateTerm = accAccountDAOobj.isDuplicateSalesTerm(termMap); //To check duplicate sales term in Master Configuration Sales Term Window.
            if(isDuplicateTerm){
                throw new AccountingException(messageSource.getMessage("acc.master.invoice.salesterm", null, RequestContextUtils.getLocale(request)) +" '<b>"+term+"</b>"+ messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request))); //Sales Term 'X' already exist
            }
            
            HashMap<String, Object> hm = new HashMap();
            hm.put("companyid", companyid);
            hm.put("userId", sessionHandlerImpl.getUserid(request));
                hm.put("term", term);
            
            String accountid = request.getParameter("accountid");
            hm.put("accountid", accountid);

            String formula = request.getParameter("formula");
            hm.put("formula", formula);

            int category = Integer.parseInt(request.getParameter("category"));
            hm.put("category", category);

            int includegst = Integer.parseInt(request.getParameter("includegst"));
            hm.put("includegst", includegst);
           
            int proft = Integer.parseInt(request.getParameter("proft"));
            hm.put("proft", proft);

            int sign = Integer.parseInt(request.getParameter("sign"));
            hm.put("sign", sign);

            int suppressamount = Integer.parseInt(request.getParameter("suppressamount"));
            hm.put("suppressamount", suppressamount);

            hm.put("creationdate", new Date());

            String id = request.getParameter("id");
            if (!StringUtil.isNullOrEmpty(id)) {
                hm.put("id", id);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("formulaids"))) {
                hm.put("formulaids", request.getParameter("formulaids"));
            } else if(!StringUtil.isNullOrEmpty(formula)){
                hm.put("formulaids", formula);
            }
            String isSalesOrPurchase = request.getParameter("isSalesOrPurchase");
            if (!StringUtil.isNullOrEmpty(isSalesOrPurchase)) {
                hm.put("salesOrPurchaseFlag", Boolean.parseBoolean(isSalesOrPurchase));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("percent"))) {
                hm.put("percent", Double.parseDouble(request.getParameter("percent")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("termtype"))) {
                hm.put("termtype", Integer.parseInt(request.getParameter("termtype")));
                hm.put("termsequence", getTermSequece(Integer.parseInt(request.getParameter("termtype"))));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("taxtype"))) {
                hm.put("taxtype", Integer.parseInt(request.getParameter("taxtype")));
                if (!StringUtil.isNullOrEmpty(request.getParameter("taxvalue"))) {
                    if(Integer.parseInt(request.getParameter("taxtype"))==0){ // If Flat
                        hm.put("termamount", Double.parseDouble(request.getParameter("taxvalue")));
                    } else { // Else Percentage
                        hm.put("percent", Double.parseDouble(request.getParameter("taxvalue")));
                    }
                }
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("purchasevalueorsalevalue"))) {
                hm.put("purchasevalueorsalevalue", Double.parseDouble(request.getParameter("purchasevalueorsalevalue")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("deductionorabatementpercent"))) {
                hm.put("deductionorabatementpercent", Double.parseDouble(request.getParameter("deductionorabatementpercent")));
            }
            String includeInTDSCalculation = request.getParameter("includeInTDSCalculation");
            if (!StringUtil.isNullOrEmpty(includeInTDSCalculation)) {
                hm.put("includeInTDSCalculation", includeInTDSCalculation.equals("on"));
            }
             if (!StringUtil.isNullOrEmpty(request.getParameter("isTermActive"))) {
                boolean isTermActive =StringUtil.equalIgnoreCase(request.getParameter("isTermActive"),"on")?true:false;
                hm.put("isTermActive", isTermActive);
            }
            KwlReturnObject kwlreturn = accAccountDAOobj.saveInvoiceTerm(hm);
            List<InvoiceTermsSales> li = kwlreturn.getEntityList();
            invTerm = li.get(0);
            String action = "purchase term";
            String auditaction=AuditAction.PURCHASE_TERM_ADDED;
            if (isSalesOrPurchase.equals("true")) {
                action = "sales term";
                auditaction =AuditAction.SALES_TERM_ADDED;
            }
            auditTrailObj.insertAuditLog(auditaction, "User " + sessionHandlerImpl.getUserFullName(request) + " has added " + action + " " + term, request, companyid);
        } catch (ServiceException ex) {
//            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveInvoiceTerm : " + ex.getMessage(), ex);
        } 
            return invTerm;
    }
    
    
    public ModelAndView addActiveDateRange(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        KwlReturnObject kmsg = null;
        List list = null;
        boolean issuccess = false;
        //Create transaction 
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CF_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            DateFormat df = authHandler.getDateOnlyFormat(request);
            String companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyid);
            requestParams.put("company", sessionHandlerImpl.getCompanyid(request));
            requestParams.put("fromdate", StringUtil.isNullOrEmptyWithTrim(request.getParameter("fromdate"))? null : df.parse(request.getParameter("fromdate")));
            requestParams.put("todate", StringUtil.isNullOrEmptyWithTrim(request.getParameter("todate"))? null : df.parse(request.getParameter("todate")));

            kmsg = accAccountDAOobj.addActiveDateRange(requestParams);
            issuccess = true;
//            list = kmsg.getEntityList();
            txnManager.commit(status);
             auditTrailObj.insertAuditLog(AuditAction.ACTIVE_DATE_RANCE, "User " + sessionHandlerImpl.getUserFullName(request) + " has saved Active Data Range " , request, companyid ); 
        } catch (Exception e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
        } finally {
            try {
                jobj.put("success", issuccess);
//                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView saveUpdateMonthlyForecast(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobjaccount = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = true, isCommitEx = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);

        String msg = "";
        try {

            String jsonData = request.getParameter("jsondata");
            JSONArray jsonArray = null;
            jsonArray = new JSONArray(jsonData);
            for (int jsonCnt = 0; jsonCnt < jsonArray.length(); jsonCnt++) {

                jobjaccount = (JSONObject) jsonArray.get(jsonCnt);
                KwlReturnObject result = accAccountDAOobj.addMonthlyForecast(jobjaccount);
                auditTrailObj.insertAuditLog(AuditAction.MONTHLYFORECAST, "User " + sessionHandlerImpl.getUserFullName(request) + " has set monthly forecast to account " + jobjaccount.getString("accountname"), request, jobjaccount.getString("id"));
            }
            issuccess = true;
            msg = messageSource.getMessage("acc.field.MonthlyForecastForAccountAddedSuccessfully", null, RequestContextUtils.getLocale(request)); // "Monthly Forecast for account added successfully.";
            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                isCommitEx = true;
            }
        } catch (Exception ex) {
            if (!isCommitEx) {
                txnManager.rollback(status);
            }
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    //update creation date of Accounts when taking back companyaccount preferences 
    public ModelAndView updateCreationDateforAccounts(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, AccountingException, ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true, isCommitEx = false;
        String msg = "";
        try {
            HashMap<String, Object> filterParams = new HashMap<String, Object>();//update the currency of all account
            
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Date appDate = null;
            if (StringUtil.isNullOrEmpty(request.getParameter("applydate"))) {
                throw new AccountingException(messageSource.getMessage("acc.curex.excp1", null, RequestContextUtils.getLocale(request)));
            } else {
                appDate = authHandler.getGlobalDateFormat().parse(request.getParameter("applydate"));
                filterParams.put("applyDate",appDate);
            }
            filterParams.put("companyid", companyid);
            KwlReturnObject result = accAccountDAOobj.updateAccountCurrency(filterParams);
        } catch (ParseException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveApplyDateforExchangeRate : " + ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView saveCompanyTermsSales(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        List<LineLevelTerms> list = new ArrayList<LineLevelTerms>();
        try {
            list = saveCompanyTermsSales(request);
            issuccess = true;
            txnManager.commit(status);
//            ExtraCompanyPreferences cmpPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
//            if (cmpPref.isActivateCRMIntegration() && "true".equals(request.getParameter("isSalesOrPurchase"))) {
//                saveInvoiceTermsInCRM(intTerm);
//            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public List<LineLevelTerms> saveCompanyTermsSales(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        LineLevelTerms invTerm = null;
        List<LineLevelTerms> list = new ArrayList<LineLevelTerms>();
        try {
            JSONArray jSONArray = new JSONArray(request.getParameter("data"));
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jobj = jSONArray.getJSONObject(i);
                HashMap<String, Object> hm = new HashMap();
                if(jobj.has("id") && !StringUtil.isNullOrEmpty(jobj.getString("id"))){
                    hm.put("id", jobj.getString("id"));
                }
                if(jobj.has("sign") && !StringUtil.isNullOrEmpty(jobj.getString("sign"))){
                    hm.put("sign", jobj.getString("sign"));
                }
                if(jobj.has("glaccount") && !StringUtil.isNullOrEmpty(jobj.getString("glaccount"))){
                    hm.put("accountid", jobj.getString("glaccount"));
                }
                if(jobj.has("accountid") && !StringUtil.isNullOrEmpty(jobj.getString("accountid"))){
                    hm.put("accountid", jobj.getString("accountid"));
                }
                if(jobj.has("formulaids") && !StringUtil.isNullOrEmpty(jobj.getString("formulaids"))){
                    hm.put("formulaids", jobj.getString("formulaids"));
                    hm.put("formula", jobj.getString("formulaids"));
                }
//                if(jobj.has("formula") && !StringUtil.isNullOrEmpty(jobj.getString("formula"))){
//                    hm.put("formula", StringUtil.DecodeText(jobj.optString("formula")));
//                }
                if(jobj.has("termpercentage") && !StringUtil.isNullOrEmpty(jobj.getString("termpercentage"))){
                    hm.put("percent", jobj.getString("termpercentage"));
                }
                if(jobj.has("purchasevalueorsalevalue") && !StringUtil.isNullOrEmpty(jobj.getString("purchasevalueorsalevalue"))){
                    hm.put("purchasevalueorsalevalue", jobj.getString("purchasevalueorsalevalue"));
                }
                if(jobj.has("deductionorabatementpercent") && !StringUtil.isNullOrEmpty(jobj.getString("deductionorabatementpercent"))){
                    hm.put("deductionorabatementpercent", jobj.getString("deductionorabatementpercent"));
                }
                if(jobj.has("taxtype") && !StringUtil.isNullOrEmpty(jobj.getString("taxtype"))){
                    hm.put("taxtype", jobj.getInt("taxtype"));
                    if(jobj.has("taxvalue") && !StringUtil.isNullOrEmpty(jobj.getString("taxvalue"))){
                        if(jobj.getInt("taxtype") == 0){ // If Flat
                            hm.put("termamount", jobj.getString("taxvalue"));
                        } else { // Else Percentage
                            hm.put("percent", jobj.getString("taxvalue"));
                        }
                    }
                }
                if(jobj.has("termtype") && !StringUtil.isNullOrEmpty(jobj.getString("termtype"))){
                    hm.put("termtype", jobj.getString("termtype"));
                }
                if(jobj.has("isDefault") && !StringUtil.isNullOrEmpty(jobj.getString("isDefault"))){
                    hm.put("isDefault",Boolean.parseBoolean((String) jobj.get("isDefault")));
                }
                KwlReturnObject kwlreturn = accAccountDAOobj.saveInvoiceTerm(hm);
                List<LineLevelTerms> li = kwlreturn.getEntityList();
                invTerm = li.get(0);
                list.add(invTerm);
            }

        } catch (ServiceException | JSONException ex) {
            throw ServiceException.FAILURE("saveInvoiceTerm : " + ex.getMessage(), ex);
        } 
        return list;
    }
    public int getTermSequece(int termtype){
        int termsequence = 0 ; 
        switch (termtype) {
            case 1:  
                termsequence = 6;
                break;
            case 2:  
                termsequence = 1;
                break;
            case 3:  
                termsequence = 5;
                break;
            case 4:  
                termsequence = 2;
                break;
            case 5:  
                termsequence = 3;
                break;
            case 6:  
                termsequence = 4;
                break;
        }
        return termsequence;
    }
    
    public ModelAndView saveBankAccountMappingDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        List<LineLevelTerms> list = new ArrayList<LineLevelTerms>();
        try {
            JSONObject paramJObj = StringUtil.convertRequestToJsonObject(request);
            jobj = controllerService.saveBankAccountMappingDetails(paramJObj);
            issuccess = true;
            msg=" Deskera account has mapped succesfully.";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            if (ex.getMessage() == null) {
                msg = ex.getCause().getMessage();
            }
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getBankAccountMappingDetails(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject jobjTemp = new JSONObject();
        JSONObject returnObject = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try {
            JSONObject paramJObj = StringUtil.convertRequestToJsonObject(request);
            jobj = controllerService.getBankAccountMappingGridInfo(paramJObj);

            jobjTemp = controllerService.getBankAccountMappingDetails(paramJObj);
            
            JSONObject dataObj = jobjTemp.getJSONObject("data");
            dataObj.put("columns", jobj.getJSONArray("columns"));
            dataObj.put("success", true);
            dataObj.put("metaData", jobj.getJSONObject("metadata"));
            returnObject.put("data", dataObj);
            returnObject.put("valid", true);
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                returnObject.put("success", issuccess);
                returnObject.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView_ex", Constants.model, returnObject.toString());
    }
}
