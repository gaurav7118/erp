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

package com.krawler.spring.accounting.handler;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.StringUtil;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.model.location.Location;
import com.krawler.inventory.model.location.LocationService;
import com.krawler.inventory.model.store.Store;
import com.krawler.inventory.model.store.StoreService;
import com.krawler.inventory.model.store.StoreType;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.product.accProductController;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.product.productHandler;
import com.krawler.spring.accounting.product.service.AccProductService;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.companyDetails.companyDetailsDAO;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import com.krawler.common.util.Constants;
import com.krawler.common.util.IndiaComplianceConstants;
import com.krawler.common.util.URLUtil;
import static com.krawler.spring.accounting.handler.AccountingManager.getGlobalCurrencyidFromRequest;
import static com.krawler.spring.accounting.tax.TaxConstants.APPLYDATE;
import static com.krawler.spring.accounting.tax.TaxConstants.GST_ACCOUNT_ID;
import static com.krawler.spring.accounting.tax.TaxConstants.GST_ACCOUNT_NAME;
import static com.krawler.spring.accounting.tax.TaxConstants.MASTERTYPEVALUE;
import static com.krawler.spring.accounting.tax.TaxConstants.PERCENT;
import static com.krawler.spring.accounting.tax.TaxConstants.TAXCODE;
import static com.krawler.spring.accounting.tax.TaxConstants.TAXDESCRIPTION;
import static com.krawler.spring.accounting.tax.TaxConstants.TAXNAME;
import static com.krawler.spring.accounting.tax.TaxConstants.TAXTYPE;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import java.io.IOException;
import com.krawler.spring.accounting.uom.accUomDAO;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.support.RequestContextUtils;
import com.krawler.spring.accounting.currency.AccTaxCurrencyExchangeDAO;
import com.krawler.spring.accounting.customreports.AccCustomReportService;
import static com.krawler.spring.accounting.tax.TaxConstants.SALESTAX_ACCOUNT_ID;
import static com.krawler.spring.accounting.tax.TaxConstants.SALESTAX_ACCOUNT_NAME;
import java.io.File;
import java.nio.file.*;

/**
 *
 * @author krawler
 */
public class NewCompanySetupController extends MultiActionController {
    private HibernateTransactionManager txnManager;
    private accAccountDAO accAccountDAOobj;
    private accUomDAO accUomObj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private accCurrencyDAO accCurrencyDAOobj;
    private AccTaxCurrencyExchangeDAO accTaxCurrencyExchangeDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private accTaxDAO accTaxObj;
    /*
     *  syncDataUsingThread its used for access as class object which is implemented by runnable interface  
     */
    
    private SyncDataUsingThread syncDataUsingThread;
    private accPaymentDAO accPaymentDAOobj;
    private accProductDAO accProductObj;
    private companyDetailsDAO companyDetailsDAOobj;
    private StoreService storeService;
    private LocationService locationService;
    private AccProductService accProductService;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private APICallHandlerService apiCallHandlerService;
    private CompanySetupThread companySetupThread;
    private MessageSource messageSource;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private AccCustomReportService accCustomReportService;

    public void setCompanySetupThread(CompanySetupThread companySetupThread) {
        this.companySetupThread = companySetupThread;
    }

    public void setcompanyDetailsDAO(companyDetailsDAO companyDetailsDAOobj) {
        this.companyDetailsDAOobj = companyDetailsDAOobj;
    }
    
    public void setSyncDataUsingThread(SyncDataUsingThread syncDataUsingThread) {
        this.syncDataUsingThread = syncDataUsingThread;
    }
    
    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    public void setAccTaxCurrencyExchangeDAO(AccTaxCurrencyExchangeDAO accTaxCurrencyExchangeDAOobj) {
        this.accTaxCurrencyExchangeDAOobj = accTaxCurrencyExchangeDAOobj;
    }
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    public void setaccTaxDAO (accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public void setAccProductService(AccProductService accProductService) {
        this.accProductService = accProductService;
    }

    public void setStoreService(StoreService storeService) {
        this.storeService = storeService;
    }

    public void setLocationService(LocationService locationService) {
        this.locationService = locationService;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {
        this.apiCallHandlerService = apiCallHandlerService;
    }

    public void setaccUomDAO(accUomDAO accUomObj) {
        this.accUomObj = accUomObj;
    }

    public void setMessageSource(MessageSource ms) {
        this.messageSource = ms;
    }
    public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }
    
    public void setAccCustomReportService(AccCustomReportService accCustomReportService) {
        this.accCustomReportService = accCustomReportService;
    }
    
    public ModelAndView SetupCompany(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "", timezone ="";
        JSONObject curObj = new JSONObject();
        JSONObject columnPrefObj = new JSONObject();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try{
            JSONObject setUpData = new JSONObject(request.getParameter("data"));
            sessionHandlerImpl.updateCurrencyID(request, setUpData.getString("currencyid"));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String countryid = "";
            String stateid = "";
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            KwlReturnObject currencyrec = kwlCommonTablesDAOObj.getObject(KWLCurrency.class.getName(), setUpData.getString("currencyid"));
            KWLCurrency kwlCurrency = (KWLCurrency) currencyrec.getEntityList().get(0);
            boolean mrpActivated = StringUtil.isNullOrEmpty(setUpData.optString("activatemrpmodule", "")) ? false : Boolean.parseBoolean(setUpData.getString("activatemrpmodule"));
            KwlReturnObject comp = kwlCommonTablesDAOObj.getObject(Company.class.getName(), companyid);
            Company company1 = (Company) comp.getEntityList().get(0);
            String comCountry = company1.getCountry().getID();
            String comCurrency = company1.getCurrency().getCurrencyID();
            boolean isMalaysianCompany = false;//comCountry.equalsIgnoreCase("137");
            boolean isUSCompany = false;//Flag for US companies(countryid=244)

            try {
                if(setUpData.has("countryid")) {
                    countryid =  setUpData.getString("countryid");
                }
                if(setUpData.has("stateid")) {
                    //For "Other" state OR if no state is selected, we will consider it as null. 
                    stateid = ( setUpData.getString("stateid").equalsIgnoreCase("1001") || StringUtil.isNullOrEmpty(setUpData.getString("stateid")))  ? null : setUpData.getString("stateid");
                }
                HashMap<String, Object> hmcompany = new HashMap<String, Object>();
                hmcompany.put("companyid", companyid);
            	if(setUpData.has("countryid")) {
                    hmcompany.put("country", setUpData.getString("countryid"));
                    isMalaysianCompany = setUpData.getString("countryid").equalsIgnoreCase("137");
                    isUSCompany = setUpData.getString("countryid").equalsIgnoreCase("244");
                    timezone = kwlCommonTablesDAOObj.getCountryTimezoneID(setUpData.getString("countryid"));
                    hmcompany.put("timezone", timezone);
                }
            	if(setUpData.has("currencyid")) {
                    hmcompany.put("currency", setUpData.getString("currencyid"));
                }
            	if(setUpData.has("stateid")) {
                    hmcompany.put("state", stateid);
                }
                Date modifydate= authHandler.getDateOnlyFormat().parse(authHandler.getConstantDateFormatter(request).format(new Date()));
                hmcompany.put("modifydate", modifydate);
                companyDetailsDAOobj.updateCompany(hmcompany);

                //Update Users timezone associated with specified company.
                if (!StringUtil.isNullOrEmpty(timezone)) {
                    kwlCommonTablesDAOObj.updateCompanyUsersTimezone(companyid);
                }
                HttpSession sessionforCurrency = request.getSession(true);
                sessionforCurrency.setAttribute("currencyid", setUpData.getString("currencyid"));

                curObj.put("Currency", kwlCurrency.getHtmlcode());
                curObj.put("CurrencyName", kwlCurrency.getName());
                curObj.put("CurrencySymbol", kwlCurrency.getSymbol());
                curObj.put("Currencyid", kwlCurrency.getCurrencyID());

            } catch (Exception e) {
            	throw new AccountingException("save Currency, Country & Timezone: "+e.getMessage());
            }

            try {
                JSONObject accjson = new JSONObject();
                accjson.put("accountid", preferences.getDepereciationAccount().getID());
                accjson.put("currencyid", kwlCurrency.getCurrencyID());
                accjson.put("accounttype", Group.ACC_TYPE_PROFITLOSS); //Expense type account
            	accjson.put("mastertypevalue",Group.ACCOUNTTYPE_GL);
                accjson.put("usedin", Constants.Depreciation_Account);
                accAccountDAOobj.updateAccount(accjson);

                accjson = new JSONObject();
                accjson.put("accountid", preferences.getDiscountGiven().getID());
                accjson.put("currencyid", kwlCurrency.getCurrencyID());
                accjson.put("accounttype", Group.ACC_TYPE_PROFITLOSS); //Expense type account
            	accjson.put("mastertypevalue",Group.ACCOUNTTYPE_GL);
                accjson.put("usedin", Constants.Discount_Given);
                accAccountDAOobj.updateAccount(accjson);

                accjson = new JSONObject();
                accjson.put("accountid", preferences.getDiscountReceived().getID());
                accjson.put("currencyid", kwlCurrency.getCurrencyID());
                accjson.put("accounttype", Group.ACC_TYPE_PROFITLOSS); //Expense type account
            	accjson.put("mastertypevalue",Group.ACCOUNTTYPE_GL);
                accjson.put("usedin",Constants.Discount_Received);
                accAccountDAOobj.updateAccount(accjson);

                accjson = new JSONObject();
                accjson.put("accountid", preferences.getCashAccount().getID());
                accjson.put("currencyid", kwlCurrency.getCurrencyID());
                accjson.put("accounttype", Group.ACC_TYPE_BALANCESHEET); //Expense type account
            	accjson.put("mastertypevalue",Group.ACCOUNTTYPE_CASH);
                accjson.put("usedin", Constants.Cash_Account);
                accAccountDAOobj.updateAccount(accjson);

                accjson = new JSONObject();
                accjson.put("accountid", preferences.getForeignexchange().getID());
                accjson.put("currencyid", kwlCurrency.getCurrencyID());
                accjson.put("accounttype", Group.ACC_TYPE_PROFITLOSS); //Expense type account
                accjson.put("mastertypevalue", Group.ACCOUNTTYPE_GL);
                accjson.put("usedin",Constants.Foreign_Exchange);
                accAccountDAOobj.updateAccount(accjson);

//                accjson = new JSONObject();
//                accjson.put("accountid", preferences.getOtherCharges().getID());
//            	accjson.put("currencyid", kwlCurrency.getCurrencyID());
//                accjson.put("accounttype", Group.ACC_TYPE_PROFITLOSS); //Expense type account
//            	accjson.put("mastertypevalue",Group.ACCOUNTTYPE_GL);
//                accjson.put("usedin",Constants.Other_Charges);
//                accAccountDAOobj.updateAccount(accjson);

                accjson = new JSONObject();
                accjson.put("accountid", preferences.getExpenseAccount().getID());
                accjson.put("currencyid", kwlCurrency.getCurrencyID());
                accjson.put("accounttype", Group.ACC_TYPE_PROFITLOSS); //Expense type account
                accjson.put("mastertypevalue", Group.ACCOUNTTYPE_GL);
                accjson.put("usedin",Constants.Salary_Expense_Account);
                accAccountDAOobj.updateAccount(accjson);

                accjson = new JSONObject();
                accjson.put("accountid", preferences.getLiabilityAccount().getID());
                accjson.put("currencyid", kwlCurrency.getCurrencyID());
                accjson.put("accounttype", Group.ACC_TYPE_BALANCESHEET); //Expense type account
            	accjson.put("mastertypevalue",Group.ACCOUNTTYPE_GL);
                accjson.put("usedin", Constants.Salary_Payable_Account);
                accAccountDAOobj.updateAccount(accjson);

                accjson = new JSONObject();
                accjson.put("accountid", preferences.getCustomerdefaultaccount().getID());
                accjson.put("currencyid", kwlCurrency.getCurrencyID());
                accjson.put("accounttype", Group.ACC_TYPE_BALANCESHEET); //Expense type account
            	accjson.put("mastertypevalue",Group.ACCOUNTTYPE_GL);
                accjson.put("usedin",Constants.Customer_Default_Account);
                accAccountDAOobj.updateAccount(accjson);

                accjson = new JSONObject();
                accjson.put("accountid", preferences.getVendordefaultaccount().getID());
                accjson.put("currencyid", kwlCurrency.getCurrencyID());
                accjson.put("accounttype", Group.ACC_TYPE_BALANCESHEET); //Expense type account
            	accjson.put("mastertypevalue",Group.ACCOUNTTYPE_GL);
                accjson.put("usedin", Constants.Vendor_Default_Account);
                accAccountDAOobj.updateAccount(accjson);

                accjson = new JSONObject();
                accjson.put("accountid", preferences.getRoundingDifferenceAccount().getID());
                accjson.put("currencyid", kwlCurrency.getCurrencyID());
                accjson.put("accounttype", Group.ACC_TYPE_PROFITLOSS); //Expense type account
            	accjson.put("mastertypevalue",Group.ACCOUNTTYPE_GL);
                accjson.put("usedin", Constants.Rounding_Off_Difference);
                accAccountDAOobj.updateAccount(accjson);

                HashMap<String, Object> filterParams = new HashMap<String, Object>();//update the currency of all account
                filterParams.put("currencyid", kwlCurrency.getCurrencyID());
                filterParams.put("companyid", companyid);
                accAccountDAOobj.updateAccountCurrency(filterParams);  //update the currency of all account

            } catch (Exception e) {
            	throw new AccountingException("change 5 accounts currencyid in preferences: "+e.getMessage());
            }

            currencyid = sessionHandlerImpl.getCurrencyID(request);
            HashMap accounthm = new HashMap();
//For adding the default group for new company
            JSONArray accGroupJArr = new JSONArray();
            try {
                if (setUpData.getString("addDefaultAccountType").equalsIgnoreCase("Yes")) {
                    String auditMsg = "", auditID = "";
                    JSONArray groupJArr = setUpData.getJSONArray("accGroupDetails");
                    for (int i = 0; i < groupJArr.length(); i++) {
                        JSONObject grJObj = groupJArr.getJSONObject(i);

                        JSONObject groupjson = new JSONObject();
                        groupjson.put("companyid", companyid);
                        groupjson.put("name", grJObj.getString("groupname"));
                        if (!StringUtil.isNullOrEmpty(grJObj.getString("nature"))) {
                            groupjson.put("nature", Integer.parseInt(grJObj.getString("nature")));
                        }
                        groupjson.put("affectgp", grJObj.getString("affectgp") != null ? Boolean.FALSE.parseBoolean(grJObj.getString("affectgp")) :false);
                        if (grJObj.has("isMasterGroup")) {
                            groupjson.put("isMasterGroup", grJObj.getBoolean("isMasterGroup"));
                        }
                        if (!StringUtil.isNullOrEmpty(grJObj.getString("parentid"))) {
                            groupjson.put("parentid", grJObj.getString("parentid"));
                        }
                        groupjson.put("grpOldId", grJObj.getString("groupid"));
                        Group group=addGroups(groupjson,companyid);
                        groupjson.put("newGrpId", group!=null ? group.getID() : "");
                        accGroupJArr.put(groupjson);
                        accounthm.put(grJObj.getString("groupid"), group);

                    }
                }
            } catch (Exception ex) {
                throw new AccountingException("Add Groups: " + ex.getMessage());
            }

            try {
                if (setUpData.has("companyTypeId") && setUpData.has("companyTypeId")) {
                    if (setUpData.getString("addDefaultAccountType").equalsIgnoreCase("Yes")) {
                        if (setUpData.getString("addDefaultAccount").equalsIgnoreCase("Yes")) {
                            KwlReturnObject kresult = accAccountDAOobj.copyAccounts(companyid, currencyid, setUpData.getString("companyTypeId"), setUpData.getString("countryid"), accounthm, setUpData.getString("stateid"), mrpActivated);
                            HashMap hmAcc = new HashMap();
                            if (!kresult.getEntityList().isEmpty()) {
                                hmAcc = (HashMap) kresult.getEntityList().get(0);
                                accMasterItemsDAOobj.mapMasterItemWithAccount(companyid, hmAcc,Constants.NatureofPaymentGroup);
                                KwlReturnObject kresult1 = accPaymentDAOobj.copyPaymentMethods(companyid, hmAcc);
                                if (!kresult1.getEntityList().isEmpty()) {
                                    HashMap hmPaymentMethod = (HashMap) kresult1.getEntityList().get(0);
                                    accPaymentDAOobj.copyIndiaComplianceData(companyid, hmPaymentMethod);
                                }
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                throw new AccountingException("Copy Accounts, Payment Method & India Compliance Data: " + ex.getMessage());
            }

            // Save Currency Details
            try {
                JSONArray currJArr = setUpData.getJSONArray("currencyDetails");
                for (int i = 0; i < currJArr.length(); i++) {
                    JSONObject cJObj = currJArr.getJSONObject(i);

                    Date appDate = authHandler.getDateOnlyFormat(request).parse(StringUtil.DecodeText(cJObj.optString("applydate")));
                    Date toDateVal = authHandler.getDateOnlyFormat(request).parse(StringUtil.DecodeText(cJObj.optString("todate")));
                    Calendar applyDate = Calendar.getInstance();
                    Calendar toDate = Calendar.getInstance();
                    applyDate.setTime(appDate);
                    toDate.setTime(toDateVal);

                    String erid = StringUtil.DecodeText(cJObj.optString("erid"));
                    HashMap<String, Object> filterParams = new HashMap<String, Object>();
                    filterParams.put("erid", erid);
                    filterParams.put("applydate", appDate);
                    filterParams.put("todate", toDateVal);
                    filterParams.put("companyid", companyid);
                    KwlReturnObject result = accCurrencyDAOobj.getExchangeRateDetails(filterParams, false);
                    List list = result.getEntityList();

                    HashMap<String, Object> erdMap = new HashMap<String, Object>();
                    erdMap.put("exchangerate", Double.parseDouble(StringUtil.DecodeText(cJObj.optString("exchangerate"))));
                    erdMap.put("foreigntobaseexchangerate", Double.parseDouble(StringUtil.DecodeText(cJObj.optString("foreigntobaseexchangerate"))));

                    ExchangeRateDetails erd;
                    KwlReturnObject erdresult;
                    if (list.size() <= 0) {
                        erdMap.put("applydate",authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(applyDate.getTime())));
                        erdMap.put("todate",authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(toDate.getTime())));
                        erdMap.put("erid",erid);
                        erdMap.put("companyid",companyid);
                        erdresult = accCurrencyDAOobj.addExchangeRateDetails(erdMap);
                    } else {
                        erd = (ExchangeRateDetails) list.get(0);
                        erdMap.put("erdid",erd.getID());
                        erdresult = accCurrencyDAOobj.updateExchangeRateDetails(erdMap);
                    }
                    erd = (ExchangeRateDetails) erdresult.getEntityList().get(0);

                    if(!StringUtil.isNullOrEmpty(countryid) && countryid.equals(Constants.INDONESIAN_COUNTRYID)){
                        KwlReturnObject CurrencyForTaxResult = accTaxCurrencyExchangeDAOobj.getTaxExchangeRateDetails(filterParams, false);
                        List CurrencyForTaxlist = CurrencyForTaxResult.getEntityList();

                        HashMap<String, Object> CurrencyerdMap = new HashMap<String, Object>();
                        CurrencyerdMap.put("exchangerate", Double.parseDouble(StringUtil.DecodeText(cJObj.optString("exchangerate"))));
                        CurrencyerdMap.put("foreigntobaseexchangerate", Double.parseDouble(StringUtil.DecodeText(cJObj.optString("foreigntobaseexchangerate"))));

                        TaxExchangeRateDetails Currencyerd;
                        KwlReturnObject Currencyerdresult;
                        if (CurrencyForTaxlist.size() <= 0) {
                            CurrencyerdMap.put("applydate", authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(applyDate.getTime())));
                            CurrencyerdMap.put("todate", authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(toDate.getTime())));
                            CurrencyerdMap.put("erid", erid);
                            CurrencyerdMap.put("companyid", companyid);
                            Currencyerdresult = accTaxCurrencyExchangeDAOobj.addTaxExchangeRateDetails(CurrencyerdMap);
                        } else {
                            Currencyerd = (TaxExchangeRateDetails) CurrencyForTaxlist.get(0);
                            erdMap.put("erdid", Currencyerd.getID());
                            Currencyerdresult = accTaxCurrencyExchangeDAOobj.updateTaxExchangeRateDetails(erdMap);
                        }
                        Currencyerd = (TaxExchangeRateDetails) Currencyerdresult.getEntityList().get(0);
                    }
                }
            }catch(Exception ex) {
                throw new AccountingException("Save Currency rates: "+ex.getMessage());
            }



            // Save Tax Details ==> 1.Create Account, 2.Save Tax, 3.Save TaxList
            try {

                JSONArray taxJArr = setUpData.getJSONArray("taxDetails");
                for (int i = 0; i < taxJArr.length(); i++) {
                    JSONObject tJObj = taxJArr.getJSONObject(i);
                    Date appDate = authHandler.getDateOnlyFormat(request).parse(StringUtil.DecodeText(tJObj.optString("applydate")));
                    Account taxAccount = null;
                    if (isMalaysianCompany && !StringUtil.isNullOrEmpty(tJObj.optString("gstaccountid", ""))) {// gstaccountid will contain value only if GST(Input) or GST(Output) account is selected for this GST
                        String gstAccountId = tJObj.getString("gstaccountid");

                        KwlReturnObject gstAccountObj = kwlCommonTablesDAOObj.getObject(Account.class.getName(), gstAccountId);
                        taxAccount = (Account) gstAccountObj.getEntityList().get(0);
                    } else if(isUSCompany && !StringUtil.isNullOrEmpty(tJObj.optString("salestaxaccountid", ""))){
                        /*
                            For US companies mapping only one account(Sales Tax Payable) to all default taxes.
                        */
                        String salestaxaccountid = tJObj.getString("salestaxaccountid");
                        KwlReturnObject salestaxAccountObj = kwlCommonTablesDAOObj.getObject(Account.class.getName(), salestaxaccountid);
                        taxAccount = (Account) salestaxAccountObj.getEntityList().get(0);
                    } else {

                        Group taxGroup = null;
                        if (accounthm.containsKey(Group.OTHER_CURRENT_LIABILITIES)) {
                            taxGroup = (Group) accounthm.get(Group.OTHER_CURRENT_LIABILITIES);
                        } else {
                            KwlReturnObject groupNameObj = kwlCommonTablesDAOObj.getObject(Group.class.getName(), Group.OTHER_CURRENT_LIABILITIES);
                            Group fetchGroup = (Group) groupNameObj.getEntityList().get(0);
                            JSONObject groupjson = new JSONObject();
                            groupjson.put("companyid", companyid);
                            groupjson.put("name", fetchGroup.getName());
                            groupjson.put("nature", fetchGroup.getNature());
                            groupjson.put("affectgp", fetchGroup.isAffectGrossProfit());
                            groupjson.put("grpOldId", fetchGroup.getID());
                            taxGroup = addGroups(groupjson, companyid);
                            accounthm.put(fetchGroup.getID(), taxGroup);
                        }
                        JSONObject accjson = new JSONObject();
                        accjson.put("depaccountid", preferences.getDepereciationAccount().getID());
                        accjson.put("name", StringUtil.DecodeText(tJObj.optString("name")));
                        accjson.put("balance", 0.0);
                        accjson.put("groupid", taxGroup.getID());
                        accjson.put("companyid", companyid);
                        accjson.put("currencyid", currencyid);
                        accjson.put("life", 10.0);
                        accjson.put("salvage", 0.0);
                        accjson.put("creationdate", appDate);
                        accjson.put("accounttype", Group.ACC_TYPE_BALANCESHEET); //fro GST account it should be balancesheet
                        if (tJObj.has("mastertypevalue") && !StringUtil.isNullOrEmpty(tJObj.getString("mastertypevalue"))){
                            accjson.put("mastertypevalue", tJObj.getString("mastertypevalue"));

                        }
                        accjson.put("usedin",Constants.Tax);
                        KwlReturnObject accresult = accAccountDAOobj.addAccount(accjson);
                        taxAccount = (Account) accresult.getEntityList().get(0);
                    }

                    //Create Tax
                    HashMap<String,Object> taxMap = new HashMap<String, Object>();
                    String taxName=StringUtil.DecodeText(tJObj.optString("name"));
                    int taxType=2; //For sales tax type
                    boolean isPurchase=false;
                    /*
                     * TX-E43 renamed as TX-IES
                     * TX-N43 renamed as TX-ES
                     * Added new purchase tax RP,TX-FRS,TX-NC & NP
                     */
                    if(StringUtil.isMalaysianPurchaseTax(taxName)){
                        taxType=1; //For Purchase tax type
                    }
                    taxType=tJObj.getInt("taxtype");
                    taxMap.put("taxid",StringUtil.generateUUID() );
                    taxMap.put("taxname", taxName);
                    taxMap.put("taxdescription", StringUtil.DecodeText(tJObj.optString("description")));
                    taxMap.put("taxcode", StringUtil.DecodeText(tJObj.optString("code")));
                    taxMap.put("taxCodeWithoutPercentage", StringUtil.DecodeText(tJObj.optString("code")));
                    taxMap.put("accountid", taxAccount.getID());
                    taxMap.put("companyid", companyid);
                    taxMap.put("taxtypeid", taxType);
                    taxMap.put("defaulttax",tJObj.optString("defaulttaxid")); //defaultgst table id
                    KwlReturnObject taxresult = accTaxObj.addTax(taxMap);
                    Tax tax = (Tax) taxresult.getEntityList().get(0);

                    //Create taxList
                    HashMap<String, Object> taxListMap = new HashMap<String, Object>();
                    taxListMap.put("taxid", tax.getID());
                    taxListMap.put("applydate", appDate);
                    taxListMap.put("companyid", companyid);
                    taxListMap.put("countryid", countryid);
                    taxListMap.put("stateid", stateid);
                    taxListMap.put("percent", Double.parseDouble(tJObj.getString("percent")));
                    KwlReturnObject taxlistresult = accTaxObj.addTaxList(taxListMap);
                    TaxList taxlist = (TaxList) taxlistresult.getEntityList().get(0);
                }
            }catch(Exception ex) {
                throw new AccountingException("Save Tax Details: "+ex.getMessage());
            }

            // Save Bank Details ==> 1.Create Account, 2.Save Bank
            try {
                JSONArray bankJArr = setUpData.getJSONArray("bankDetails");
                for (int i = 0; i < bankJArr.length(); i++) {
                    JSONObject bJObj = bankJArr.getJSONObject(i);
                    Group bankGroup=null;
                    Date appDate = authHandler.getDateFormatter(request).parse(StringUtil.DecodeText(bJObj.optString("applydate")));
                    if (accounthm.containsKey(Group.BANK_ACCOUNT)) {
                        bankGroup = (Group) accounthm.get(Group.BANK_ACCOUNT);
                    } else {
                        KwlReturnObject groupNameObj = kwlCommonTablesDAOObj.getObject(Group.class.getName(), Group.BANK_ACCOUNT);
                        Group fetchGroup = (Group) groupNameObj.getEntityList().get(0);
                        JSONObject groupjson = new JSONObject();
                        groupjson.put("companyid", companyid);
                        groupjson.put("name", fetchGroup.getName());
                        groupjson.put("nature", fetchGroup.getNature());
                        groupjson.put("affectgp", fetchGroup.isAffectGrossProfit());
                        groupjson.put("grpOldId", fetchGroup.getID());
                        bankGroup = addGroups(groupjson, companyid);
                        accounthm.put(fetchGroup.getID(), bankGroup);
                    }
                    //Create Account
                    JSONObject accjson = new JSONObject();
                    accjson.put("accounttype", StringUtil.DecodeText(bJObj.optString("accounttype")));
                    accjson.put("acccode",StringUtil.DecodeText(bJObj.optString("no")));
                    accjson.put("depaccountid", preferences.getDepereciationAccount().getID());
                    accjson.put("name", StringUtil.DecodeText(bJObj.optString("accountname")));
                    accjson.put("ifsccode", !StringUtil.isNullOrEmpty(bJObj.optString("ifsccode"))?StringUtil.DecodeText(bJObj.optString("ifsccode")):"");
                    accjson.put("micrcode", !StringUtil.isNullOrEmpty(bJObj.optString("micrcode"))?StringUtil.DecodeText(bJObj.optString("micrcode")):"");
                    accjson.put("balance", Double.parseDouble(StringUtil.DecodeText(bJObj.optString("balance"))));
                    accjson.put("groupid", bankGroup.getID());
                    accjson.put("companyid", companyid);
                    accjson.put("currencyid", currencyid);
                    accjson.put("life", 10.0);
                    accjson.put("salvage", 0.0);
                    // ======== Used for INDIA country - (Add in Json)  At SetupWizard Creation/Update ============
                    accjson.put("bankbranchname", !StringUtil.isNullOrEmpty(bJObj.optString("bankbranchname"))?StringUtil.DecodeText(bJObj.optString("bankbranchname")):"");
                    accjson.put("bankbranchaddress", !StringUtil.isNullOrEmpty(bJObj.optString("bankbranchaddress"))?StringUtil.DecodeText(bJObj.optString("bankbranchaddress")):"");
                    accjson.put("branchstate", !StringUtil.isNullOrEmpty(bJObj.optString("branchstate"))?StringUtil.DecodeText(bJObj.optString("branchstate")):"");
                    accjson.put("bsrcode", !StringUtil.isNullOrEmpty(bJObj.optString("bsrcode"))?StringUtil.DecodeText(bJObj.optString("bsrcode")):0);
                    accjson.put("pincode", !StringUtil.isNullOrEmpty(bJObj.optString("pincode"))?StringUtil.DecodeText(bJObj.optString("pincode")):0);
                    accjson.put("accountno", !StringUtil.isNullOrEmpty(bJObj.getString("accountno")) ? StringUtil.DecodeText(bJObj.optString("accountno")) : ""); //Adding account no for Bank type account
                    // ======== ===================================================================== ============
                    accjson.put("creationdate", appDate);
                    if (bJObj.has("mastertypevalue") && !StringUtil.isNullOrEmpty(bJObj.getString("mastertypevalue"))) {
                        accjson.put("mastertypevalue", bJObj.getString("mastertypevalue"));

                    }
                    accjson.put("usedin",Constants.Payment_Method);
                    KwlReturnObject accresult = accAccountDAOobj.addAccount(accjson);
                    Account bankAccount = (Account) accresult.getEntityList().get(0);

                    Map<String, Object> methodMap = new HashMap<>();
                    methodMap.put("methodname", StringUtil.DecodeText(bJObj.optString("accountname")));
                    methodMap.put("accountid", bankAccount.getID());
                    methodMap.put("detailtype", 2); //2:Bank Type
                    methodMap.put("companyid", companyid);

                    KwlReturnObject methodresult = accPaymentDAOobj.addPaymentMethod(methodMap);
                    PaymentMethod pom = (PaymentMethod) methodresult.getEntityList().get(0);
                }
            }catch(Exception ex) {
                throw new AccountingException("Save Bank Details: "+ex.getMessage());
            }

            // Save Lock Year Details
            try {
                JSONArray lockJArr = setUpData.getJSONArray("lockDetails");
                for (int i = 0; i < lockJArr.length(); i++) {
                    JSONObject lJObj = lockJArr.getJSONObject(i);
                    Map<String, Object> yearLockMap = new HashMap<>();
                    yearLockMap.put("yearid", Integer.parseInt(lJObj.getString("name")));
                    yearLockMap.put("islock", "true".equalsIgnoreCase(lJObj.getString("islock")));
                    yearLockMap.put("companyid", companyid);

                    String yearLockid = lJObj.getString("id");
                    KwlReturnObject result;
                    if (StringUtil.isNullOrEmpty(yearLockid)) {
                        result = accCompanyPreferencesObj.addYearLock(yearLockMap);
                    } else {
                        yearLockMap.put("id", yearLockid);
                        result = accCompanyPreferencesObj.updateYearLock(yearLockMap);
                    }
                    YearLock yearlock = (YearLock) result.getEntityList().get(0);
                }
            }catch(Exception ex) {
                throw new AccountingException("Save Lock Year Details: "+ex.getMessage());
            }

            KwlReturnObject cap1 = kwlCommonTablesDAOObj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cap1.getEntityList().get(0);
            DateFormat formatter = (SimpleDateFormat) authHandler.getDateOnlyFormat(request);
            try {
               Boolean withInvUpdate=!StringUtil.isNullOrEmpty(request.getParameter("withInvUpdate"))?Boolean.parseBoolean((String) request.getParameter("withInvUpdate")):true;
               Boolean withInventory=!StringUtil.isNullOrEmpty(request.getParameter("withInventory"))?Boolean.parseBoolean((String) request.getParameter("withInventory")):true;
                boolean isIndianCompany = false;
                HashMap<String, Object> prefMap = new HashMap<String, Object>();
                prefMap.put("setupdone", true);
                prefMap.put("companytype", setUpData.getString("companyTypeId"));

                prefMap.put("enableGST", setUpData.optBoolean("enableGST", false));
                prefMap.put(Constants.isMultiEntity, setUpData.optBoolean(Constants.isMultiEntity, false));
                if (!StringUtil.isNullOrEmpty(setUpData.optString("gstEffectiveDate", ""))) {
                    prefMap.put("gstEffectiveDate", authHandler.getDateOnlyFormat(request).parse(setUpData.getString("gstEffectiveDate")));
                }
                /*
                 *This values are set in case of  isMultiEntity indian_country_id USA_country_id
                */
                
                columnPrefObj.put("gstamountdigitafterdecimal", String.valueOf(Constants.gst_amountdigitafterdecimal));                
                /**
                 * ERP-34919
                 * GST calculation is based on Shipping address of Customer/ Vendor. (For US Only)
                 */
                boolean isGSTCalBasedOnShippingAddress = false;
                if(countryid.equalsIgnoreCase(String.valueOf(Constants.USA_country_id))){
                    isGSTCalBasedOnShippingAddress =true;
                }
               
                /*------"Map taxes at product level" check will ON by default for newly created Malaysian company---------  */
                if (countryid.equalsIgnoreCase(String.valueOf(Constants.malaysian_country_id))) {
                    columnPrefObj.put("mapTaxesAtProductLevel", true);
                }
                
                columnPrefObj.put(Constants.columnPref.GSTCalculationOnShippingAddress.get(), isGSTCalBasedOnShippingAddress);
                if ((setUpData.has(Constants.isMultiEntity) && setUpData.optBoolean(Constants.isMultiEntity, false))) {
                    if(countryid.equalsIgnoreCase(String.valueOf(Constants.indian_country_id)) || countryid.equalsIgnoreCase(String.valueOf(Constants.USA_country_id))){
                        prefMap.put("gstapplicabledate",new Date());
//                        prefMap.put("columnPref", precisionData.toString());
                    }
                }
                
                prefMap.put("fyfrom", formatter.parse(setUpData.getString("yearStartDate")));
                prefMap.put("bbfrom", formatter.parse(setUpData.getString("bookStartDate")));
                prefMap.put("withoutinventory",!withInventory);
                prefMap.put("withinvupdate", withInvUpdate);
                prefMap.put("UomSchemaType", setUpData.getString("UomSchemaType"));
                prefMap.put("isLocationCompulsory", Boolean.parseBoolean(setUpData.getString("isLocationCompulsory")));
                prefMap.put("isWarehouseCompulsory", Boolean.parseBoolean(setUpData.getString("isWarehouseCompulsory")));
                prefMap.put("isRowCompulsory", Boolean.parseBoolean(setUpData.getString("isRowCompulsory")));
                prefMap.put("isRackCompulsory", Boolean.parseBoolean(setUpData.getString("isRackCompulsory")));
                prefMap.put("isBinCompulsory", Boolean.parseBoolean(setUpData.getString("isBinCompulsory")));
                prefMap.put("isBatchCompulsory", Boolean.parseBoolean(setUpData.getString("isBatchCompulsory")));
                prefMap.put("isSerialCompulsory", Boolean.parseBoolean(setUpData.getString("isSerialCompulsory")));
                prefMap.put("productPricingOnBands", Boolean.parseBoolean(setUpData.getString("productPricingOnBands")));
                prefMap.put("productPricingOnBandsForSales", Boolean.parseBoolean(setUpData.getString("productPricingOnBandsForSales")));
                prefMap.put("withouttax1099", (!setUpData.getString("withTax1099").equalsIgnoreCase("Yes")));
                prefMap.put("id", sessionHandlerImpl.getCompanyid(request));
                prefMap.put("autojournalentry", "000000");
                if (setUpData.has("inventoryvaluationtype") && setUpData.get("inventoryvaluationtype") != null && !setUpData.optString("inventoryvaluationtype", "").equalsIgnoreCase("")) {
                    prefMap.put("inventoryvaluationtype", setUpData.get("inventoryvaluationtype"));
                }
                if(!comCountry.equals(setUpData.getString("countryid")))
                    prefMap.put("countryChange", true);
                if(!comCurrency.equals(setUpData.getString("currencyid")))
                    prefMap.put("currencyChange", true);
                KwlReturnObject result = accCompanyPreferencesObj.updatePreferences(prefMap);
                Date fyfrom= authHandler.getDateOnlyFormat(request).parse(setUpData.getString("yearStartDate"));
                accAccountDAOobj.updateAccountCreationDate(fyfrom,companyid);//Updating all accounts creation date with first financial year 

                ExtraCompanyPreferences extraCompanyPreferences=null;
                MRPCompanyPreferences mrpCompanyPreferences=null;
                IndiaComplianceCompanyPreferences indiaCompanyPreferences=null;
                DocumentEmailSettings documentEmailSettings=null;
                Map<String, Object> requestParamsExtra = new HashMap<String, Object>();
                requestParamsExtra.put("id", sessionHandlerImpl.getCompanyid(request));
                prefMap.put("companyid", sessionHandlerImpl.getCompanyid(request));
                prefMap.put("onlyBaseCurrency",StringUtil.isNullOrEmpty(setUpData.getString("onlybasecurrencyflag"))? false : Boolean.parseBoolean(setUpData.getString("onlybasecurrencyflag")));
                prefMap.put("activateToDateforExchangeRates",StringUtil.isNullOrEmpty(setUpData.getString("activateToDateforExchangeRates"))? false : Boolean.parseBoolean(setUpData.getString("activateToDateforExchangeRates")));
                prefMap.put("activateInventoryTab",StringUtil.isNullOrEmpty(setUpData.getString("activateInventory"))? false : Boolean.parseBoolean(setUpData.getString("activateInventory")));
                prefMap.put("activatemrpmodule",StringUtil.isNullOrEmpty(setUpData.getString("activatemrpmodule"))? false : Boolean.parseBoolean(setUpData.getString("activatemrpmodule")));
                prefMap.put("viewallexcludecustomerwithoutsalesperson",true); //ERP-27605 = to enable viewall exclude customer in customer based on salesperson mapping this check is actvated when new company is created. 

                KwlReturnObject resultExtra = accCompanyPreferencesObj.getExtraCompanyPreferences(requestParamsExtra);
                if(!resultExtra.getEntityList().isEmpty()){
                    extraCompanyPreferences = (ExtraCompanyPreferences) resultExtra.getEntityList().get(0);
                }
                if (extraCompanyPreferences == null) {
                    extraCompanyPreferences = new ExtraCompanyPreferences();
                }
                KwlReturnObject mrpresult= accCompanyPreferencesObj.getMRPCompanyPreferences(requestParamsExtra);
                if(!mrpresult.getEntityList().isEmpty()){
                    mrpCompanyPreferences = (MRPCompanyPreferences) mrpresult.getEntityList().get(0);
                }
                if (mrpCompanyPreferences == null) {
                    mrpCompanyPreferences = new MRPCompanyPreferences();
                }
                // India company preferences - Start
                KwlReturnObject indiaresult= accCompanyPreferencesObj.getIndiaComplianceExtraCompanyPreferences(requestParamsExtra);
                if(!indiaresult.getEntityList().isEmpty()){
                    indiaCompanyPreferences = (IndiaComplianceCompanyPreferences) indiaresult.getEntityList().get(0);
                }
                if (indiaCompanyPreferences == null) {
                    indiaCompanyPreferences = new IndiaComplianceCompanyPreferences();
                } 
                // India company preferences - End
                
                // Total Revenue based on account Nature flag is true while ceating new company
                prefMap.put("gstIncomeGroup", true);

                if (setUpData.has("registrationType")) {
                    prefMap.put("registrationType", setUpData.getString("registrationType"));
                }
                if (setUpData.has("GSTIN")) {
                    prefMap.put("GSTIN", setUpData.getString("GSTIN"));
                }
                if (setUpData.has("vatNumber")) {
                    prefMap.put("vatNumber", setUpData.getString("vatNumber"));
                }
                if (setUpData.has("cstNumber")) {
                    prefMap.put("cstNumber", setUpData.getString("cstNumber"));
                }
                if (setUpData.has("panNumber")) {
                    prefMap.put("panNumber", setUpData.getString("panNumber"));
                }
                if (setUpData.has("serviceTaxRegNumber")) {
                    prefMap.put("serviceTaxRegNumber", setUpData.getString("serviceTaxRegNumber"));
                }
                if (setUpData.has("tanNumber")) {
                    prefMap.put("tanNumber", setUpData.getString("tanNumber"));
                }
                if (setUpData.has("eccNumber")) {
                    prefMap.put("eccNumber", setUpData.getString("eccNumber"));
                }
                if (setUpData.has("panNumber")) {
                    // for INDONESIA saving npwp number as pan no. and other country PAN number itlself
                    prefMap.put("panNumber", setUpData.getString("panNumber"));
                }
                if (setUpData.has("countryid") || (setUpData.has(Constants.isMultiEntity) && setUpData.optBoolean(Constants.isMultiEntity, false))) {
                    prefMap.put("country", setUpData.getString(Constants.COUNTRY_ID));
                    isIndianCompany = setUpData.getString(Constants.COUNTRY_ID).equalsIgnoreCase(String.valueOf(Constants.indian_country_id));
                    prefMap.put("isIndianCompany", isIndianCompany);                    
                    if (isIndianCompany || countryid.equals(String.valueOf(Constants.USA_country_id))) {
                        prefMap.put("lineLevelTermFlag", 1);
                        prefMap.put("isNewGSTOnly", true);
                    }  //Some more checks added i.e.  isMultiEntity,USA_country_id 
                }
                try {
                    /*
                     * Inserting Custom Dimension in SI,PI,JE,MP,RP,DN,CN,DO for process multi entity
                     */
                    if ((setUpData.has(Constants.isMultiEntity) && setUpData.optBoolean(Constants.isMultiEntity, false))) {
                        HashMap<String, Object> requestParams = new HashMap<>();
                        requestParams.put(Constants.isMultiEntity, setUpData.optBoolean(Constants.isMultiEntity));
                        requestParams.put(Constants.companyid, companyid);
                         if(countryid.equals(String.valueOf(Constants.indian_country_id)) ){
                            requestParams.put(Constants.COUNTRY_ID, String.valueOf(Constants.indian_country_id));
                            
                         }
                         if(countryid.equals((String.valueOf(Constants.USA_country_id)))){
                             requestParams.put(Constants.COUNTRY_ID, String.valueOf(Constants.USA_country_id));
                         }
                        String subdomain = URLUtil.getDomainName(request);
                        requestParams.put("DefaultValue", subdomain);
                        boolean isDimensionCreated = accAccountDAOobj.insertDefaultCustomeFields(requestParams);
                        prefMap.put(Constants.isDimensionCreated, isDimensionCreated);
                    }
                } catch (Exception ex) {
                    prefMap.put(Constants.isDimensionCreated, false);
                    throw new AccountingException("Error while Creating Entity Dimension Fields : " + ex.getMessage());
                }
               
                //By default activating product as typeahead type and allow paging to it
                prefMap.put("ProductSelectionType", "1");
                columnPrefObj.put(Constants.columnPref.productPaging.get(),true);
                prefMap.put("columnPref", columnPrefObj.toString());
                resultExtra = accCompanyPreferencesObj.addOrUpdateExtraPreferences(prefMap);
                mrpresult = accCompanyPreferencesObj.addOrUpdateMRPPreferences(prefMap);
                indiaresult = accCompanyPreferencesObj.addOrUpdateIndiaComplianceExtraPreferences(prefMap);
                /*
                 Activate at PM side
                 */
                if (!StringUtil.isNullOrEmpty(setUpData.getString("activatemrpmodule"))) {
                    boolean activatemrpmodule = Boolean.parseBoolean(setUpData.getString("activatemrpmodule"));
                    if (activatemrpmodule) {
                        JSONObject userData = new JSONObject();
                        userData.put("userid", sessionHandlerImpl.getUserid(request));
                        userData.put("companyid", companyid);
                        userData.put("enablemrp", activatemrpmodule);
                        activateMRPInPM(userData);
                    }
                }
                //To Add Document Email Setting 
                KwlReturnObject resultDocument = accCompanyPreferencesObj.getDocumentEmailSettings(requestParamsExtra);
                if(!resultDocument.getEntityList().isEmpty()){
                    documentEmailSettings = (DocumentEmailSettings) resultDocument.getEntityList().get(0);
                }
                if (documentEmailSettings == null) {
                    documentEmailSettings = new DocumentEmailSettings();
                }
                resultDocument = accCompanyPreferencesObj.addOrUpdateDocumentEmailSettings(prefMap);

                /*To map customizereportheader ID in customizereportmapping for default hidden fields */
                accAccountDAOobj.mapDefaultHiddenFields(companyid);

                //Setup Default store and location
                Store store = new Store("DS", "Default Store", "", StoreType.WAREHOUSE, company);
                storeService.addStore(company.getCreator(), store);
                Location location = new Location(company, "Default Location");
                Set stores = new HashSet<Store>();
                stores.add(store);
                location.setStores(stores);
                location.setId(StringUtil.generateUUID());
                locationService.addLocation(company.getCreator(), location);

            }catch(Exception ex) {
                throw new AccountingException("Save Preferences: "+ex.getMessage());
            }
            try {
                /*
                 * Copy Master Items  for line level terms
                 */
                HashMap<String, Object> defaultValueMap = new HashMap<String, Object>();
                defaultValueMap.put("companyid", companyid);
                defaultValueMap.put("userid", sessionHandlerImpl.getUserid(request));
                defaultValueMap.put("country", countryid);
                defaultValueMap.put("state", stateid);
                accMasterItemsDAOobj.copyMasterItemsCountrySpecifics(defaultValueMap);
            } catch (Exception ex) {
                throw new AccountingException("Error while Copying default terms: "+ex.getMessage());
            }
            
            
           /*
            * Copy default TDS rate for INDIA Country Only
            */

            try {
                if (countryid != null && Constants.indian_country_id == Integer.parseInt(countryid)) {
                    HashMap<String, Object> defaultValueMap = new HashMap<String, Object>();
                    defaultValueMap.put("companyid", companyid);
                    accMasterItemsDAOobj.copyDefaultTDSRates(defaultValueMap);
                }
            } catch (Exception ex) {
                throw new AccountingException("Error while copying default TDS rate: "+ex.getMessage());
            }
            
            try {
                /*
                 * Copy Terms for INDIA/ US Country Only
                 */
                if (setUpData.has("countryid") && setUpData.getString("countryid").equalsIgnoreCase("105") || Constants.USA_country_id == Integer.parseInt(countryid)) {
                    HashMap<String, Object> defaultValueMap = new HashMap<String, Object>();
                    defaultValueMap.put("companyid", companyid);
                    defaultValueMap.put("userid", sessionHandlerImpl.getUserid(request));
                    defaultValueMap.put("country", countryid);
                    defaultValueMap.put("state", stateid);
                    accAccountDAOobj.copyDefaultTerms(defaultValueMap, setUpData);
                }
            } catch (Exception ex) {
                throw new AccountingException("Error while Copying default terms: "+ex.getMessage());
            }
            try {
                /*
                 * Copy UOM for INDIA Country Only
                 */
                if (setUpData.has("countryid") && setUpData.getString("countryid").equalsIgnoreCase("105")) {
                    HashMap<String, Object> defaultValueMap = new HashMap<String, Object>();
                    defaultValueMap.put("companyid", companyid);
                    defaultValueMap.put("userid", sessionHandlerImpl.getUserid(request));
                    defaultValueMap.put("country", countryid);
                    defaultValueMap.put("state", stateid);
                    accUomObj.copyUOM(companyid, defaultValueMap);
                }
            } catch (Exception ex) {
                throw new AccountingException("Error while Copying default UOM : "+ex.getMessage());
            }

            try {
                
               // Instead of being for india country only, this code will be executed for all countries and will create Default Dimensions which are set as !isMultiEntity in defaultcustomfields
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("companyid", companyid);
                    requestParams.put("stateid", setUpData.getString("stateid"));
                    requestParams.put("countryid", setUpData.getString("countryid"));
                    accAccountDAOobj.insertDefaultCustomeFields(requestParams);
                
            } catch (Exception ex) {
                throw new AccountingException("Error while Creating default Custom Fields : "+ex.getMessage());
            }
            try {
                /*
                 * Add defalt Master items for 'Product Tax Class' INDIA Country Only
                 */
                if (setUpData.has("countryid") && setUpData.getString("countryid").equalsIgnoreCase(String.valueOf(Constants.indian_country_id))) {
                    HashMap<String, Object> defaultValueMap = new HashMap<String, Object>();
                    defaultValueMap.put("companyid", companyid);
                    defaultValueMap.put("userid", sessionHandlerImpl.getUserid(request));
                    defaultValueMap.put("country", countryid);
                    accAccountDAOobj.addMasterItemsToProductTaxClass(defaultValueMap, setUpData);
                }
            } catch (Exception ex) {
                throw new AccountingException("Error while Copying default Product Tax class: "+ex.getMessage());
            }
            /*
                 * Create Default E-Way Bill Report INDIA Country Only
            */
            
             try {
                if (setUpData.has("countryid") && (setUpData.getString("countryid").equalsIgnoreCase(String.valueOf(Constants.indian_country_id))) || setUpData.getString("countryid").equalsIgnoreCase(String.valueOf(Constants.INDONESIAN_COUNTRY_ID))) {
                    HashMap<String, Object> defaultValueMap = new HashMap<String, Object>();
                    defaultValueMap.put("companyid", companyid);
                    defaultValueMap.put("userId", sessionHandlerImpl.getUserid(request));
                    defaultValueMap.put("country", countryid);
                    defaultValueMap.put("isEdit", false);
                    accCustomReportService.setUpCustomReportsDefaultsForNewCompany(defaultValueMap);
                    
                }
            } catch (Exception ex) {
                throw new AccountingException("Error while Creating Default Custom Reports: "+ex.getMessage());
            }
            /*
             * Following changes are done for the ticket - ERP-4335 
             * For malaysian country, Edit Transaction and Delete Tranasction options are set false by default.
             */
            if(preferences.getCompany().getCountry().getID().equals(Constants.malaysian_country_id+"")){
                preferences.setEditTransaction(false);
                preferences.setDeleteTransaction(false);
            }
            txnManager.commit(status);
//            if (company.getCountry().getID().equals(Constants.INDONESIAN_COUNTRYID)) {
            Map<String, Object> reqMap = new HashMap<>();
            reqMap.put("countryid", company.getCountry().getID());
            reqMap.put(Constants.companyKey, companyid);
            reqMap.put("accGroupDetails", accGroupJArr);
            companySetupThread.add(reqMap);
            Thread t = new Thread(companySetupThread);
            t.start();
//            }
            issuccess = true;
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(NewCompanySetupController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("currency", curObj);
            } catch (JSONException ex) {
                Logger.getLogger(NewCompanySetupController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * Description : Below Method is used to set Default company setup
     * @param <request> used to get default company setup parameters
     * @param <response> used to send respose
     * @return :JSONObject
     */
    public void activateMRPInPM(JSONObject userData) {
        try {
            String accRestURL = URLUtil.buildRestURL("pmURL");
            String endpoint = accRestURL + "company/checks";
            JSONObject resObj = apiCallHandlerService.restGetMethod(endpoint, userData.toString());
        } catch (JSONException ex) {
            Logger.getLogger(NewCompanySetupController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            Logger.getLogger(NewCompanySetupController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public ModelAndView DefaultCompanySetUp(HttpServletRequest request, HttpServletResponse response) {
        /*Variable declaration*/
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String companyid = null;
        JSONObject curObj = new JSONObject();
        HashMap accounthm = new HashMap();
        Date fyfrom = null;
        DateFormat formatter = null;
        HashMap<String, Object> defaultCompSetupMap = null;
        /*Transaction manager for company setup*/
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {

            /*Cast all company setup data in JSONObject*/
            JSONObject setUpData = new JSONObject(request.getParameter("data"));

            /*Generate common Map for Default Company setup */
            defaultCompSetupMap = generateDefaultSetUpCompanyMap(request, setUpData);

            /*Update currency of company in user session*/
            sessionHandlerImpl.updateCurrencyID(request, setUpData.getString("currencyid"));

            /*Set variable values*/
            if (defaultCompSetupMap.containsKey(Constants.companyid) && defaultCompSetupMap.get(Constants.companyid) != null) {
                companyid = (String) defaultCompSetupMap.get(Constants.companyid);
            }
            if (defaultCompSetupMap.containsKey(Constants.df) && defaultCompSetupMap.get(Constants.df) != null) {
                formatter = (DateFormat) defaultCompSetupMap.get(Constants.df);
            }

            KwlReturnObject cap = kwlCommonTablesDAOObj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);

            KwlReturnObject currencyrec = kwlCommonTablesDAOObj.getObject(KWLCurrency.class.getName(), setUpData.getString("currencyid"));
            KWLCurrency kwlCurrency = (KWLCurrency) currencyrec.getEntityList().get(0);

            KwlReturnObject comp = kwlCommonTablesDAOObj.getObject(Company.class.getName(), companyid);
            Company company1 = (Company) comp.getEntityList().get(0);

            /*update company data*/
            companyDetailsDAOobj.updateCompany(defaultCompSetupMap);

            /*Update User Time Zone */
            kwlCommonTablesDAOObj.updateCompanyUsersTimezone(companyid);

            /*Add Default Account for company */
            if (setUpData.getString("addDefaultAccount").equalsIgnoreCase("Yes")) {
                addDefaultAccount(preferences, kwlCurrency);
            }

            /*Upade  Account Currency of Default Account */
            accAccountDAOobj.updateAccountCurrency(defaultCompSetupMap);


            /*Add Default Account Group  for company */
            if (setUpData.getString("addDefaultAccountType").equalsIgnoreCase("Yes")) {
                accounthm = addDefaultAccountGroup(defaultCompSetupMap, setUpData);
            }
             /*Add Default Exchange rate details and Currency for company */
            if (setUpData.has("currencyDetails")) {
                addDefaultCurrencyDetails(defaultCompSetupMap, setUpData);
            }
            /*Add Default Tax to Malaysian GST,Singapure(SGD) and United States company only */
            if (setUpData.has("countryid") && (setUpData.getString("countryid").equalsIgnoreCase("137") || setUpData.getString("countryid").equalsIgnoreCase("203")|| setUpData.getString("countryid").equalsIgnoreCase("106")|| setUpData.getString("countryid").equalsIgnoreCase("244"))) {
                addDefaultTaxDetails(defaultCompSetupMap, preferences, setUpData, accounthm);
            }

            /* Updating all accounts creation date with first financial year */
            fyfrom = formatter.parse(setUpData.getString("yearStartDate"));
            accAccountDAOobj.updateAccountCreationDate(fyfrom, companyid);

            /* To add/upadte  default setting for Company Preferences */
            accCompanyPreferencesObj.updatePreferences(defaultCompSetupMap);

            /* To add default setting for ExtraCompanyPreferences */
            accCompanyPreferencesObj.addOrUpdateExtraPreferences(defaultCompSetupMap);

            /* To add default setting for Document Email settings */
            accCompanyPreferencesObj.addOrUpdateDocumentEmailSettings(defaultCompSetupMap);

            /*To map customizereportheader ID in customizereportmapping for default hidden fields */
            accAccountDAOobj.mapDefaultHiddenFields(companyid);

            /* Setup Default store and location */
            Store store = new Store("DS", "Default Store", "", StoreType.WAREHOUSE, company1);
            storeService.addStore(company1.getCreator(), store);
            Location location = new Location(company1, "Default Location");
            Set stores = new HashSet<Store>();
            stores.add(store);
            location.setStores(stores);
            location.setId(StringUtil.generateUUID());
            locationService.addLocation(company1.getCreator(), location);

            /* Return Default company currency details */
            curObj.put("Currency", kwlCurrency.getHtmlcode());
            curObj.put("CurrencyName", kwlCurrency.getName());
            curObj.put("CurrencySymbol", kwlCurrency.getSymbol());
            curObj.put("Currencyid", kwlCurrency.getCurrencyID());

            /*
             * Following changes are done for the ticket - ERP-4335 
             * For malaysian country, Edit Transaction and Delete Tranasction options are set false by default.
             */
            if (preferences.getCompany().getCountry().getID().equals(Constants.malaysian_country_id + "")) {
                preferences.setEditTransaction(false);
                preferences.setDeleteTransaction(false);
            }
            txnManager.commit(status);
            if (company1.getCountry().getID().equals(Constants.INDONESIAN_COUNTRYID)) {
                Map<String, Object> reqMap = new HashMap<>();
                reqMap.put("countryid", Constants.INDONESIAN_COUNTRYID);
                reqMap.put(Constants.companyKey, companyid);
                companySetupThread.add(reqMap);
                Thread t = new Thread(companySetupThread);
                t.start();
            }
            issuccess = true;
        } catch (AccountingException accExpObj) {
            txnManager.rollback(status);
            msg = "" + accExpObj.getMessage();
            Logger.getLogger(NewCompanySetupController.class.getName()).log(Level.SEVERE, null, accExpObj);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(NewCompanySetupController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("currency", curObj);
            } catch (JSONException ex) {
                Logger.getLogger(NewCompanySetupController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * Description : This Method is used to generate default company setup map
     * @param <request> used to get reuest parameters
     * @param <setUpData> used to get default set up data
     * @return :Map
     */
    public HashMap<String, Object> generateDefaultSetUpCompanyMap(HttpServletRequest request, JSONObject setUpData) throws SessionExpiredException ,ServiceException{
        /*Variable declaration*/
        HashMap<String, Object> defCompSetupMap = new HashMap<String, Object>();
        String companyid = "", currencyid = "";
        try {
             /*Set values to Variable */
            DateFormat formatter = (SimpleDateFormat) authHandler.getDateOnlyFormat(request);
            Boolean withInvUpdate = !StringUtil.isNullOrEmpty(request.getParameter("withInvUpdate")) ? Boolean.parseBoolean((String) request.getParameter("withInvUpdate")) : true;
            Boolean withInventory = !StringUtil.isNullOrEmpty(request.getParameter("withInventory")) ? Boolean.parseBoolean((String) request.getParameter("withInventory")) : true;
            companyid = sessionHandlerImpl.getCompanyid(request);
            currencyid = sessionHandlerImpl.getCurrencyID(request);
            KwlReturnObject comp = kwlCommonTablesDAOObj.getObject(Company.class.getName(), companyid);
            String timezone = kwlCommonTablesDAOObj.getCountryTimezoneID(setUpData.getString("countryid"));
            Company company1 = (Company) comp.getEntityList().get(0);
            String comCountry = company1.getCountry().getID();
            String comCurrency = company1.getCurrency().getCurrencyID();
            String UomSchemaType = "0";

            /* defCompSetupMap Hash map is used to set common parameters for default company setup */
            defCompSetupMap.put(Constants.companyid, companyid);
            defCompSetupMap.put(Constants.currencyKey, currencyid);
            defCompSetupMap.put(Constants.country, setUpData.getString("countryid"));
            defCompSetupMap.put(Constants.globalCurrencyKey, getGlobalCurrencyidFromRequest(request));
            defCompSetupMap.put(Constants.df, authHandler.getDateOnlyFormat(request));  //This format belongs to our global date format[i.e.new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa")]
            defCompSetupMap.put(Constants.userdf, authHandler.getUserDateFormatterWithoutTimeZone(request));
            defCompSetupMap.put("dateonlyformatter", authHandler.getDateOnlyFormatter(request));
            defCompSetupMap.put("fyfrom", formatter.parse(setUpData.getString("yearStartDate")));
            defCompSetupMap.put("bbfrom", formatter.parse(setUpData.getString("bookStartDate")));
            defCompSetupMap.put("UomSchemaType", UomSchemaType);
            defCompSetupMap.put("isLocationCompulsory", true);
            defCompSetupMap.put("isWarehouseCompulsory", true);
            defCompSetupMap.put("isRowCompulsory", false);
            defCompSetupMap.put("isRackCompulsory", false);
            defCompSetupMap.put("isBinCompulsory", false);
            defCompSetupMap.put("isBatchCompulsory", false);
            defCompSetupMap.put("isSerialCompulsory", false);
            defCompSetupMap.put("isProductPricingOnBands", false);
            defCompSetupMap.put("isProductPricingOnBandsForSales", false);
            defCompSetupMap.put("onlyBaseCurrency", false);
            defCompSetupMap.put("activateInventoryTab", true);
            defCompSetupMap.put("activateToDateforExchangeRates", false);
            defCompSetupMap.put("withoutinventory",!withInventory);
            defCompSetupMap.put("withinvupdate", withInvUpdate);
//            if (comCountry.equalsIgnoreCase("244")) {
//                defCompSetupMap.put("withouttax1099", false);
//            } else {
            defCompSetupMap.put("withouttax1099", true);
//            }
            defCompSetupMap.put("autojournalentry", "000000");
            defCompSetupMap.put("id", companyid);
            defCompSetupMap.put("setupdone", true);
            defCompSetupMap.put("companytype", setUpData.getString("companyTypeId"));
            defCompSetupMap.put("enableGST", setUpData.optBoolean("enableGST", false));
            defCompSetupMap.put("timezone", timezone);
            defCompSetupMap.put("currency", setUpData.getString("currencyid"));
            if (!comCountry.equals(setUpData.getString("countryid"))) {
                defCompSetupMap.put("countryChange", true);
            }
            if (!comCurrency.equals(setUpData.getString("currencyid"))) {
                defCompSetupMap.put("currencyChange", true);
            }
            if (!StringUtil.isNullOrEmpty(setUpData.optString("gstEffectiveDate", ""))) {
                defCompSetupMap.put("gstEffectiveDate", authHandler.getDateOnlyFormat(request).parse(setUpData.getString("gstEffectiveDate")));
            }

        }catch (JSONException ex) {
            throw ServiceException.FAILURE("Exception in get default company setup data : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(NewCompanySetupController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return defCompSetupMap;

    }

    /**
     * Description : Below Method is used to add DefaultAccount default company
     * setup
     *
     * @param <preferences> used to get account from CompanyAccountPreferences
     * @param <kwlCurrency> used to get currency details from KWLCurrency
     * @return :void
     */
    public void addDefaultAccount(CompanyAccountPreferences preferences, KWLCurrency kwlCurrency) throws ServiceException, AccountingException {

        try {
            JSONObject accjson = new JSONObject();
            /* Below code is used to set Default accont for company */
            accjson.put("accountid", preferences.getDepereciationAccount().getID());
            accjson.put("currencyid", kwlCurrency.getCurrencyID());
            accjson.put("accounttype", Group.ACC_TYPE_PROFITLOSS); //Expense type account
            accjson.put("mastertypevalue", Group.ACCOUNTTYPE_GL);
            accjson.put("usedin", Constants.Depreciation_Account);
            accAccountDAOobj.updateAccount(accjson);

            accjson = new JSONObject();
            accjson.put("accountid", preferences.getDiscountGiven().getID());
            accjson.put("currencyid", kwlCurrency.getCurrencyID());
            accjson.put("accounttype", Group.ACC_TYPE_PROFITLOSS); //Expense type account
            accjson.put("mastertypevalue", Group.ACCOUNTTYPE_GL);
            accjson.put("usedin", Constants.Discount_Given);
            accAccountDAOobj.updateAccount(accjson);

            accjson = new JSONObject();
            accjson.put("accountid", preferences.getDiscountReceived().getID());
            accjson.put("currencyid", kwlCurrency.getCurrencyID());
            accjson.put("accounttype", Group.ACC_TYPE_PROFITLOSS); //Expense type account
            accjson.put("mastertypevalue", Group.ACCOUNTTYPE_GL);
            accjson.put("usedin", Constants.Discount_Received);
            accAccountDAOobj.updateAccount(accjson);

            accjson = new JSONObject();
            accjson.put("accountid", preferences.getCashAccount().getID());
            accjson.put("currencyid", kwlCurrency.getCurrencyID());
            accjson.put("accounttype", Group.ACC_TYPE_BALANCESHEET); //Expense type account
            accjson.put("mastertypevalue", Group.ACCOUNTTYPE_CASH);
            accjson.put("usedin", Constants.Cash_Account);
            accAccountDAOobj.updateAccount(accjson);

            accjson = new JSONObject();
            accjson.put("accountid", preferences.getForeignexchange().getID());
            accjson.put("currencyid", kwlCurrency.getCurrencyID());
            accjson.put("accounttype", Group.ACC_TYPE_PROFITLOSS); //Expense type account
            accjson.put("mastertypevalue", Group.ACCOUNTTYPE_GL);
            accjson.put("usedin", Constants.Foreign_Exchange);
            accAccountDAOobj.updateAccount(accjson);

            accjson = new JSONObject();
            accjson.put("accountid", preferences.getExpenseAccount().getID());
            accjson.put("currencyid", kwlCurrency.getCurrencyID());
            accjson.put("accounttype", Group.ACC_TYPE_PROFITLOSS); //Expense type account
            accjson.put("mastertypevalue", Group.ACCOUNTTYPE_GL);
            accjson.put("usedin", Constants.Salary_Expense_Account);
            accAccountDAOobj.updateAccount(accjson);

            accjson = new JSONObject();
            accjson.put("accountid", preferences.getLiabilityAccount().getID());
            accjson.put("currencyid", kwlCurrency.getCurrencyID());
            accjson.put("accounttype", Group.ACC_TYPE_BALANCESHEET); //Expense type account
            accjson.put("mastertypevalue", Group.ACCOUNTTYPE_GL);
            accjson.put("usedin", Constants.Salary_Payable_Account);
            accAccountDAOobj.updateAccount(accjson);

            accjson = new JSONObject();
            accjson.put("accountid", preferences.getCustomerdefaultaccount().getID());
            accjson.put("currencyid", kwlCurrency.getCurrencyID());
            accjson.put("accounttype", Group.ACC_TYPE_BALANCESHEET); //Expense type account
            accjson.put("mastertypevalue", Group.ACCOUNTTYPE_GL);
            accjson.put("usedin", Constants.Customer_Default_Account);
            accAccountDAOobj.updateAccount(accjson);

            accjson = new JSONObject();
            accjson.put("accountid", preferences.getVendordefaultaccount().getID());
            accjson.put("currencyid", kwlCurrency.getCurrencyID());
            accjson.put("accounttype", Group.ACC_TYPE_BALANCESHEET); //Expense type account
            accjson.put("mastertypevalue", Group.ACCOUNTTYPE_GL);
            accjson.put("usedin", Constants.Vendor_Default_Account);
            accAccountDAOobj.updateAccount(accjson);

            accjson = new JSONObject();
            accjson.put("accountid", preferences.getRoundingDifferenceAccount().getID());
            accjson.put("currencyid", kwlCurrency.getCurrencyID());
            accjson.put("accounttype", Group.ACC_TYPE_PROFITLOSS); //Expense type account
            accjson.put("mastertypevalue", Group.ACCOUNTTYPE_GL);
            accjson.put("usedin", Constants.Rounding_Off_Difference);
            accAccountDAOobj.updateAccount(accjson);
        } catch (Exception e) {
            throw new AccountingException("change 5 accounts currencyid in preferences: " + e.getMessage());
        }
    }

    /**
     * Description : Below Method is used to add addDefaultAccountGroup for
     * company default company setup
     * @param <defaultCompSetupMap> used to get common setup parameters
     * @param <setUpData> used to get default set up data
     * @return :HashMap
     */
    public HashMap addDefaultAccountGroup(HashMap<String, Object> defaultCompSetupMap, JSONObject setUpData) throws AccountingException, Exception {

        /*Variable declaration*/
        String companyid = "";
        String currencyid = "";
        HashMap accounthm = new HashMap();
        try {
            boolean mrpActivated = StringUtil.isNullOrEmpty(setUpData.optString("activatemrpmodule", "")) ? false : Boolean.parseBoolean(setUpData.getString("activatemrpmodule"));
            if (defaultCompSetupMap.containsKey(Constants.companyid) && defaultCompSetupMap.get(Constants.companyid) != null) {
                companyid = (String) defaultCompSetupMap.get(Constants.companyid);
            }
            if (defaultCompSetupMap.containsKey(Constants.currencyKey) && defaultCompSetupMap.get(Constants.currencyKey) != null) {
                currencyid = (String) defaultCompSetupMap.get(Constants.currencyKey);
            }
            /* Get Default Account Group */
            defaultCompSetupMap.put("defaultgroup", "true");
            KwlReturnObject result = accAccountDAOobj.getGroups(defaultCompSetupMap);

            List ll = result.getEntityList();
            Iterator itr = ll.iterator();
            JSONArray groupJArr = new JSONArray();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                Group group = (Group) row[0];
                JSONObject obj = new JSONObject();
                obj.put("groupid", group.getID());
                obj.put("groupname", group.getName());
                obj.put("mastergroupid", group.getID());
                obj.put("nature", group.getNature());
                obj.put("naturename", (group.getNature() == Constants.Liability) ? "Liability" : (group.getNature() == Constants.Asset) ? "Asset" : (group.getNature() == Constants.Expences) ? "Expences" : (group.getNature() == Constants.Income) ? "Income" : "");
                obj.put("affectgp", group.isAffectGrossProfit());
                obj.put("displayorder", group.getDisplayOrder());
                obj.put("isMasterGroupD", group.isIsMasterGroup());
                obj.put("companyid", (group.getCompany() == null ? null : companyid));
                obj.put("deleted", group.isDeleted());
                Group parentGroup = (Group) row[3];
                if (parentGroup != null) {
                    obj.put("parentid", parentGroup.getID());
                    obj.put("parentname", parentGroup.getName());
                }
                obj.put("level", row[1]);
                obj.put("leaf", row[2]);
                groupJArr.put(obj);
            }
            /* Below code is used to set Default Accont Group  for company */
            for (int i = 0; i < groupJArr.length(); i++) {
                JSONObject grJObj = groupJArr.getJSONObject(i);
                JSONObject groupjson = new JSONObject();
                groupjson.put("companyid", companyid);
                groupjson.put("name", grJObj.getString("groupname"));
                if (!StringUtil.isNullOrEmpty(grJObj.getString("nature"))) {
                    groupjson.put("nature", Integer.parseInt(grJObj.getString("nature")));
                }
                groupjson.put("affectgp", grJObj.getString("affectgp") != null ? Boolean.FALSE.parseBoolean(grJObj.getString("affectgp")) : false);
                if (grJObj.has("isMasterGroup")) {
                    groupjson.put("isMasterGroup", grJObj.getBoolean("isMasterGroup"));
                }
                if (grJObj.has("parentid") && !StringUtil.isNullOrEmpty(grJObj.getString("parentid"))) {
                    groupjson.put("parentid", grJObj.getString("parentid"));
                }
                groupjson.put("grpOldId", grJObj.getString("groupid"));
                Group group = addGroups(groupjson, companyid);
                accounthm.put(grJObj.getString("groupid"), group);

            }

            try {
                if (setUpData.has("companyTypeId")) {
                    if (setUpData.getString("addDefaultAccountType").equalsIgnoreCase("Yes")) {
                        if (setUpData.getString("addDefaultAccount").equalsIgnoreCase("Yes")) {
                            accAccountDAOobj.copyAccounts(companyid, currencyid, setUpData.getString("companyTypeId"), setUpData.getString("countryid"), accounthm,setUpData.getString("stateid"),mrpActivated);
                        }
                    }

                }
            } catch (Exception ex) {
                throw new AccountingException("Copy Accounts: " + ex.getMessage());
            }

        } catch (Exception ex) {
            throw new AccountingException("Add Groups: " + ex.getMessage());

        }
        return accounthm;
    }

    /**
     * Description : Below Method is used to add DefaultCurrencyDetails default
     * company setup
     *
     * @param <defaultCompSetupMap> used to get common setup parameters
     * @param <setUpData> used to get default set up data
     * @return :void
     */
    public void addDefaultCurrencyDetails(HashMap<String, Object> defaultCompSetupMap, JSONObject setUpData) throws AccountingException, Exception {
        /* Variable declaration */
        String companyid = "";
        String countryid = "";
        DateFormat formatter = null;
        try {
            /*Set variable values*/
            if (defaultCompSetupMap.containsKey(Constants.companyid) && defaultCompSetupMap.get(Constants.companyid) != null) {
                companyid = (String) defaultCompSetupMap.get(Constants.companyid);
            }
            if(setUpData.has("countryid")) {
                countryid =  setUpData.getString("countryid") != null ? setUpData.getString("countryid") : null;
            }
            if (defaultCompSetupMap.containsKey("dateonlyformatter") && defaultCompSetupMap.get("dateonlyformatter") != null) {
                formatter = (DateFormat) defaultCompSetupMap.get("dateonlyformatter");
            }
            JSONArray currJArr = setUpData.getJSONArray("currencyDetails");
            for (int i = 0; i < currJArr.length(); i++) {
                JSONObject cJObj = currJArr.getJSONObject(i);
                Date appDate = formatter.parse(StringUtil.DecodeText(cJObj.optString("applydate")));
                Date toDateVal = formatter.parse(StringUtil.DecodeText(cJObj.optString("todate")));
                Calendar applyDate = Calendar.getInstance();
                Calendar toDate = Calendar.getInstance();
                applyDate.setTime(appDate);
                toDate.setTime(toDateVal);
                /* Get Exchange rate Details */
                String erid = StringUtil.DecodeText(cJObj.optString("erid"));
                HashMap<String, Object> filterParams = new HashMap<String, Object>();
                filterParams.put("erid", erid);
                filterParams.put("applydate", appDate);
                filterParams.put("todate", toDateVal);
                filterParams.put("companyid", companyid);
                KwlReturnObject result = accCurrencyDAOobj.getExchangeRateDetails(filterParams, false);
                List list = result.getEntityList();

                HashMap<String, Object> erdMap = new HashMap<String, Object>();
                erdMap.put("exchangerate", Double.parseDouble(StringUtil.DecodeText(cJObj.optString("exchangerate"))));
                erdMap.put("foreigntobaseexchangerate", Double.parseDouble(StringUtil.DecodeText(cJObj.optString("foreigntobaseexchangerate"))));
                 /* set  Exchange rate Details */
                ExchangeRateDetails erd;
                KwlReturnObject erdresult;
                if (list.size() <= 0) {
                    erdMap.put("applydate", authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(applyDate.getTime())));
                    erdMap.put("todate", authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(toDate.getTime())));
                    erdMap.put("erid", erid);
                    erdMap.put("companyid", companyid);
                    erdresult = accCurrencyDAOobj.addExchangeRateDetails(erdMap);
                } else {
                    erd = (ExchangeRateDetails) list.get(0);
                    erdMap.put("erdid", erd.getID());
                    erdresult = accCurrencyDAOobj.updateExchangeRateDetails(erdMap);
                }
                erd = (ExchangeRateDetails) erdresult.getEntityList().get(0);

                if(!StringUtil.isNullOrEmpty(countryid) && countryid.equals(Constants.INDONESIAN_COUNTRYID)){
                    KwlReturnObject CurrencyForTaxResult = accTaxCurrencyExchangeDAOobj.getTaxExchangeRateDetails(filterParams, false);
                    List CurrencyForTaxlist = CurrencyForTaxResult.getEntityList();

                    HashMap<String, Object> CurrencyerdMap = new HashMap<String, Object>();
                    CurrencyerdMap.put("exchangerate", Double.parseDouble(StringUtil.DecodeText(cJObj.optString("exchangerate"))));
                    CurrencyerdMap.put("foreigntobaseexchangerate", Double.parseDouble(StringUtil.DecodeText(cJObj.optString("foreigntobaseexchangerate"))));

                    TaxExchangeRateDetails Currencyerd;
                    KwlReturnObject Currencyerdresult;
                    if (CurrencyForTaxlist.size() <= 0) {
                        CurrencyerdMap.put("applydate", authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(applyDate.getTime())));
                        CurrencyerdMap.put("todate", authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(toDate.getTime())));
                        CurrencyerdMap.put("erid", erid);
                        CurrencyerdMap.put("companyid", companyid);
                        Currencyerdresult = accTaxCurrencyExchangeDAOobj.addTaxExchangeRateDetails(CurrencyerdMap);
                    } else {
                        Currencyerd = (TaxExchangeRateDetails) CurrencyForTaxlist.get(0);
                        erdMap.put("erdid", Currencyerd.getID());
                        Currencyerdresult = accTaxCurrencyExchangeDAOobj.updateTaxExchangeRateDetails(CurrencyerdMap);
                    }
                    Currencyerd = (TaxExchangeRateDetails) Currencyerdresult.getEntityList().get(0);
                }
            }
        } catch (Exception ex) {
            throw new AccountingException("Save Currency rates: " + ex.getMessage());
        }

    }

    /**
     * Description : Below Method is used to add DefaultTaxDetails default
     * company setup
     * @param <defaultCompSetupMap> used to get common setup parameters
     * @param <preferences> used to get default Account from
     * CompanyAccountPreferences
     * @param <accounthm> used to get account
     * @return :void
     */
    public void addDefaultTaxDetails(HashMap<String, Object> defaultCompSetupMap, CompanyAccountPreferences preferences, JSONObject setUpData, HashMap accounthm) throws ServiceException, Exception {
        try {
             /* Declare variable  */
            HashMap<String, Object> dataMap = new HashMap<String, Object>();
            boolean isMalaysianCompany = false;
            boolean isUSCompany = false;
            String companyid = "", currencyid = "";
            String countryid = setUpData.getString("countryid");
            DateFormat formatter = null;
             /* set variable value */
            if (defaultCompSetupMap.containsKey(Constants.companyid) && defaultCompSetupMap.get(Constants.companyid) != null) {
                companyid = (String) defaultCompSetupMap.get(Constants.companyid);
            }
            if (defaultCompSetupMap.containsKey(Constants.currencyKey) && defaultCompSetupMap.get(Constants.currencyKey) != null) {
                currencyid = (String) defaultCompSetupMap.get(Constants.currencyKey);
            }
            if (defaultCompSetupMap.containsKey(Constants.df) && defaultCompSetupMap.get(Constants.df) != null) {
                formatter = (DateFormat) defaultCompSetupMap.get(Constants.df);
            }

            if (countryid != null) {
                if (countryid.equals("137")) {
                    isMalaysianCompany = true;
                }
                /*
                    countryid 244 is set for US companies so setting isUSCompany flag to true
                */
                if (countryid.equals("244")) {
                    isUSCompany = true;
                }
                dataMap.put("countryid", countryid);
            }
            KwlReturnObject result = accTaxObj.getDefaultGSTList(dataMap);
            List list = result.getEntityList();
            /* Get default tax Details */
            JSONArray taxJArr = getDefaultGSTTaxJson(defaultCompSetupMap, list);
            for (int i = 0; i < taxJArr.length(); i++) {
                JSONObject tJObj = taxJArr.getJSONObject(i);
                Date appDate = formatter.parse(StringUtil.DecodeText(tJObj.optString("applydate")));
                Account taxAccount = null;
                if (isMalaysianCompany && !StringUtil.isNullOrEmpty(tJObj.optString("gstaccountid", ""))) {// gstaccountid will contain value only if GST(Input) or GST(Output) account is selected for this GST
                    String gstAccountId = tJObj.getString("gstaccountid");

                    KwlReturnObject gstAccountObj = kwlCommonTablesDAOObj.getObject(Account.class.getName(), gstAccountId);
                    taxAccount = (Account) gstAccountObj.getEntityList().get(0);
                } else if(isUSCompany && !StringUtil.isNullOrEmpty(tJObj.optString("salestaxaccountid", ""))){
                    /*
                        For US companies mapping only one account(Sales Tax Payable) to all default taxes.
                    */
                    String salestaxaccountid = tJObj.getString("salestaxaccountid");
                    KwlReturnObject salestaxAccountObj = kwlCommonTablesDAOObj.getObject(Account.class.getName(), salestaxaccountid);
                    taxAccount = (Account) salestaxAccountObj.getEntityList().get(0);
                } else {

                    Group taxGroup = null;
                    if (accounthm.containsKey(Group.OTHER_CURRENT_LIABILITIES)) {
                        taxGroup = (Group) accounthm.get(Group.OTHER_CURRENT_LIABILITIES);
                    } else {
                        KwlReturnObject groupNameObj = kwlCommonTablesDAOObj.getObject(Group.class.getName(), Group.OTHER_CURRENT_LIABILITIES);
                        Group fetchGroup = (Group) groupNameObj.getEntityList().get(0);
                        JSONObject groupjson = new JSONObject();
                        groupjson.put("companyid", companyid);
                        groupjson.put("name", fetchGroup.getName());
                        groupjson.put("nature", fetchGroup.getNature());
                        groupjson.put("affectgp", fetchGroup.isAffectGrossProfit());
                        groupjson.put("grpOldId", fetchGroup.getID());
                        taxGroup = addGroups(groupjson, companyid);
                        accounthm.put(fetchGroup.getID(), taxGroup);
                    }
                    JSONObject accjson = new JSONObject();
                    accjson.put("depaccountid", preferences.getDepereciationAccount().getID());
                    accjson.put("name", tJObj.getString(TAXNAME));
                    accjson.put("balance", 0.0);
                    accjson.put("groupid", taxGroup.getID());
                    accjson.put("companyid", companyid);
                    accjson.put("currencyid", currencyid);
                    accjson.put("life", 10.0);
                    accjson.put("salvage", 0.0);
                    accjson.put("creationdate", appDate);
                    accjson.put("accounttype", Group.ACC_TYPE_BALANCESHEET); //fro GST account it should be balancesheet
                    if (tJObj.has("mastertypevalue") && !StringUtil.isNullOrEmpty(tJObj.getString("mastertypevalue"))) {
                        accjson.put("mastertypevalue", tJObj.getString("mastertypevalue"));

                    }
                    accjson.put("usedin", Constants.Tax);
                    KwlReturnObject accresult = accAccountDAOobj.addAccount(accjson);
                    taxAccount = (Account) accresult.getEntityList().get(0);
                }

                /* Create Tax  */
                HashMap<String, Object> taxMap = new HashMap<String, Object>();
                String taxName = tJObj.getString(TAXNAME);
                int taxType = 2; //For sales tax type
                boolean isPurchase = false;
                /*
                 * TX-E43 renamed as TX-IES 
                 * TX-N43 renamed as TX-ES
                 * Added new purchase tax RP,TX-FRS,TX-NC & NP
                 */
                if (StringUtil.isMalaysianPurchaseTax(taxName)) {
                    taxType = 1; //For Purchase tax type
                }
                taxType = tJObj.getInt(TAXTYPE);
                taxMap.put("taxid", StringUtil.generateUUID());
                taxMap.put("taxname", taxName);
                taxMap.put("taxdescription", tJObj.getString(TAXDESCRIPTION));
                taxMap.put("taxcode", tJObj.getString(TAXCODE));
                taxMap.put("taxCodeWithoutPercentage", tJObj.getString(TAXCODE));
                taxMap.put("accountid", taxAccount.getID());
                taxMap.put("companyid", companyid);
                taxMap.put("taxtypeid", taxType);
                KwlReturnObject taxresult = accTaxObj.addTax(taxMap);
                Tax tax = (Tax) taxresult.getEntityList().get(0);

                /* Create taxList */
                HashMap<String, Object> taxListMap = new HashMap<String, Object>();
                taxListMap.put("taxid", tax.getID());
                taxListMap.put("applydate", appDate);
                taxListMap.put("companyid", companyid);
                taxListMap.put("percent", Double.parseDouble(tJObj.getString(PERCENT)));
                KwlReturnObject taxlistresult = accTaxObj.addTaxList(taxListMap);
                TaxList taxlist = (TaxList) taxlistresult.getEntityList().get(0);
            }
        } catch (Exception ex) {
            throw new AccountingException("Save Tax Details: " + ex.getMessage());
        }

    }

    /**
     * Description : Below Method is used to get Default GSTTax Json default
     * company setup
     *
     * @param <request> used to get currency id from session
     * @param <list> used to create Tax json
     * @param <gstCountryId> used to get currency id
     * @param <finStartDate> used to get Fianacial Start date
     * @return :JSONArray
     */
    public JSONArray getDefaultGSTTaxJson(HashMap<String, Object> defaultCompSetupMap, List<Object[]> list) throws SessionExpiredException, ServiceException {
         /* Variable declaration  */
        JSONArray jArr = new JSONArray();
        String finStartDate = "";
        String companyid = "";
        String currencyid = "";
        String countryid = "";
        DateFormat formatter = null;
        Date financialStartDate = null;
        try {

            /* Set values to Variable  */
            if (defaultCompSetupMap.containsKey(Constants.companyid) && defaultCompSetupMap.get(Constants.companyid) != null) {
                companyid = (String) defaultCompSetupMap.get(Constants.companyid);
            }
            if (defaultCompSetupMap.containsKey(Constants.currencyKey) && defaultCompSetupMap.get(Constants.currencyKey) != null) {
                currencyid = (String) defaultCompSetupMap.get(Constants.currencyKey);
            }
            if (defaultCompSetupMap.containsKey(Constants.df) && defaultCompSetupMap.get(Constants.df) != null) {
                formatter = (DateFormat) defaultCompSetupMap.get(Constants.df);
            }
            if (defaultCompSetupMap.containsKey("fyfrom") && defaultCompSetupMap.get("fyfrom") != null) {
                financialStartDate = (Date) defaultCompSetupMap.get("fyfrom");
            }
            if (defaultCompSetupMap.containsKey(Constants.country) && defaultCompSetupMap.get(Constants.country) != null) {
                countryid = (String) defaultCompSetupMap.get(Constants.country);
            }
            if (list != null && !list.isEmpty()) {

                String gstOutputAccountId = "";
                String gstInputAccountId = "";
                String gstOutputAccountName = "";
                String gstInputAccountName = "";
                String salesTaxPayableAccountId = "";
                String salesTaxPayableAccountName = "";
                
                boolean isMalasianCountry = false;
                boolean isUSCountry = false;
                if (countryid != null && countryid.equals("137")) {
                    isMalasianCountry = true;
                }
                /*
                    countryid 244 is set for US companies so setting isUSCompany flag to true
                */
                if (countryid != null && countryid.equals("244")) {
                    isUSCountry = true;
                }
                if (isMalasianCountry) {

                    KwlReturnObject accountReturnObject = accAccountDAOobj.getAccountFromName(companyid, Constants.MALAYSIAN_GST_OUTPUT_TAX);
                    List accountResultList = accountReturnObject.getEntityList();
                    if (!accountResultList.isEmpty()) {
                        gstOutputAccountId = ((Account) accountResultList.get(0)).getID();
                        gstOutputAccountName = ((Account) accountResultList.get(0)).getName();
                    }

                    accountReturnObject = accAccountDAOobj.getAccountFromName(companyid, Constants.MALAYSIAN_GST_INPUT_TAX);
                    accountResultList = accountReturnObject.getEntityList();
                    if (!accountResultList.isEmpty()) {
                        gstInputAccountId = ((Account) accountResultList.get(0)).getID();;
                        gstInputAccountName = ((Account) accountResultList.get(0)).getName();;
                    }
                }
                /*
                    For US companies mapping only one account(Sales Tax Payable) to all default taxes.
                */
                if (isUSCountry) {
                    KwlReturnObject accountReturnObject = accAccountDAOobj.getAccountFromName(companyid, Constants.SALES_TAX_PAYABLE);
                    List accountResultList = accountReturnObject.getEntityList();
                    if(!accountResultList.isEmpty()){
                        salesTaxPayableAccountId =  ((Account)accountResultList.get(0)).getID();
                        salesTaxPayableAccountName =  ((Account)accountResultList.get(0)).getName();
                    }
                }
                 /* Create Tax Json */
                for (Object[] row : list) {
                    JSONObject obj = new JSONObject();
                    obj.put(TAXNAME, row[0]);
                    obj.put(TAXDESCRIPTION, row[1]);
                    obj.put(PERCENT, row[3]);
                    obj.put(TAXCODE, row[2]);
                    obj.put(TAXTYPE, row[5]);
                    obj.put(APPLYDATE, formatter.format(financialStartDate));
                    obj.put(MASTERTYPEVALUE, Group.ACCOUNTTYPE_GST);
                    if (isMalasianCountry) {
                        boolean isPurchase = false;
                        int taxType = 2; //For sales tax type
                        obj.put(GST_ACCOUNT_ID, gstOutputAccountId);
                        obj.put(GST_ACCOUNT_NAME, gstOutputAccountName);
                        String taxName = row[0].toString();
                        /*
                         * TX-E43 renamed as TX-IES 
                         * TX-N43 renamed as TX-ES
                         * Added new purchase tax RP,TX-FRS,TX-NC & NP
                         */
                        if (StringUtil.isMalaysianPurchaseTax(taxName)) {
                            taxType = 1; //For Purchase tax type
                            obj.put(GST_ACCOUNT_ID, gstInputAccountId);
                            obj.put(GST_ACCOUNT_NAME, gstInputAccountName);
                        }
                    } else {
                        obj.put(GST_ACCOUNT_ID, "");
                        obj.put(GST_ACCOUNT_NAME, "");
                    }
                    /*
                        Adding sales tax payable accountid in JSON for US companies
                    */
                    if(isUSCountry){
                        obj.put(SALESTAX_ACCOUNT_ID, salesTaxPayableAccountId);
                        obj.put(SALESTAX_ACCOUNT_NAME, salesTaxPayableAccountName);
                    }else{
                        obj.put(SALESTAX_ACCOUNT_ID, "");
                        obj.put(SALESTAX_ACCOUNT_NAME, "");
                    }
                    jArr.put(obj);
                }
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getTaxJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    public Group addGroups(JSONObject groupjson, String companyid) throws ServiceException, JSONException {
        String groupid = "";
        int dispOrder = 0;
        Group existGroup = accAccountDAOobj.getAccountGroup(companyid, groupjson.getString("name"));
        if (existGroup == null) {
            if (StringUtil.isNullOrEmpty(groupid)) {
                KwlReturnObject dspresult = accAccountDAOobj.getMaxGroupDisplayOrder();
                List l = dspresult.getEntityList();
                if (!l.isEmpty() && l.get(0) != null) {
                    dispOrder = (Integer) l.get(0);
                }
                dispOrder++;
                groupjson.put("disporder", dispOrder);
                groupjson.put("groupid", groupid);

            }
            KwlReturnObject kwlReturnObject = accAccountDAOobj.addGroup(groupjson);
            existGroup = (Group) kwlReturnObject.getEntityList().get(0);

        }

        return existGroup;
    }
    public ModelAndView sendAccProducts(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String userId = sessionHandlerImpl.getUserid(request);
            JSONObject inputParamJobj = StringUtil.convertRequestToJsonObject(request);
            HashMap<String, Object> requestParamsMap = new HashMap<String, Object>();
            String cdomain = URLUtil.getDomainName(request);
            inputParamJobj.put("companyid", companyId);
            inputParamJobj.put("userid", userId);
            inputParamJobj.put("cdomain", cdomain);
            inputParamJobj.put("productId", request.getParameter("productid"));
            inputParamJobj.put("loadInventory", request.getParameter("loadInventory"));

            requestParamsMap = productHandler.getProductRequestMap(request);
            syncDataUsingThread.setInputParamJobj(inputParamJobj);
            syncDataUsingThread.setRequestParamsMap(requestParamsMap);
            Thread t = new Thread(syncDataUsingThread);
            t.start();

            msg = messageSource.getMessage("acc.syncproducterptocrm.msg", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            jobj.put("companyexist", true);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("crmManager.insertAccProduct", ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(NewCompanySetupController.class.getName()).log(Level.SEVERE, null, ex);
            }
//            HibernateUtil.closeSession(session);No need to close seesion hibernate manage it automatically
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getProducts(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            /**
             * Get Extra Company pref checks .
             */
            KwlReturnObject extraPref = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            ExtraCompanyPreferences extrareferences = (ExtraCompanyPreferences) extraPref.getEntityList().get(0);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();;
            KwlReturnObject result = accProductObj.getProductTypes(requestParams);
            List list = result.getEntityList();
            JSONArray DataJArr = productHandler.getProductTypesJson(request, list);
            jobj.put("typedata", DataJArr);
            DataJArr=null;//ERP-18753
            requestParams = productHandler.getProductRequestMap(request);
            result = accProductObj.getProducts(requestParams);
            list = result.getEntityList();
            DataJArr = productHandler.getSyncProductsJson(request, list, accProductObj , accAccountDAOobj , accountingHandlerDAOobj,accCurrencyDAOobj,false);
            jobj.put("productdata", DataJArr);
            
            //To move file from Accounting Store to Shared Folder Store once product is shared with other Deskera applications.
            moveFilesFromAccountingToSharedLocation(DataJArr);
            
            /**
             * If subdomain have line level terms as tax then send Company
             * line level terms in JSON, While Sync Product with CRM and its related Checks.
             */
            if(extrareferences!=null){
                jobj.put(IndiaComplianceConstants.ISLINE_LEVELTERM_FLAG, extrareferences.getLineLevelTermFlag());
                jobj.put(IndiaComplianceConstants.ISEXCISEAPPLICABLE, extrareferences.isExciseApplicable());
                jobj.put(IndiaComplianceConstants.ENABLEVATCST, extrareferences.isEnableVatCst());
                if (extrareferences.getLineLevelTermFlag() == 1) {
                    HashMap<String, String> hashMap = new HashMap<String, String>();
                    hashMap.put("salesOrPurchaseFlag","true");
                    hashMap.put(Constants.companyKey,sessionHandlerImpl.getCompanyid(request));
                    jobj.put(IndiaComplianceConstants.COMPANY_LINELEVEL_TERMS, accProductObj.getCompanyTermsJsonArray(hashMap));
                }
            }
            issuccess = true;
            list=null;  //ERP-18753
            result=null;
            DataJArr=null;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accProductController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return  jobj;
    }

    public ModelAndView sendAccProductsToInv(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg="";
        boolean issuccess=false;
        //Session session=null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String inventoryURL = this.getServletContext().getInitParameter("inventoryURL");
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", sessionHandlerImpl.getUserid(request));
            userData.put("companyid", companyid);

            //session=HibernateUtil.getCurrentSession();
            String action = "21";
            JSONObject pjobj=getProducts(request, response);
            userData.put("data", pjobj);
            JSONObject resObj = apiCallHandlerService.callApp(inventoryURL, userData, companyid, action);
            if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                issuccess=resObj.getBoolean("success");
                msg=resObj.getString("msg");
                jobj.put("success", true);
                jobj.put("msg", msg);
                jobj.put("companyexist", resObj.optBoolean("companyexist"));
            }else{
                issuccess=true;
                msg = messageSource.getMessage("acc.data.sync.error.msg", null, RequestContextUtils.getLocale(request));
                jobj.put("success", true);
                jobj.put("msg", msg);
                jobj.put("companyexist",true);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("invManager.insertAccProduct", ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(NewCompanySetupController.class.getName()).log(Level.SEVERE, null, ex);
            }
//            HibernateUtil.closeSession(session);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView sendAccProductsToPOS(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONObject pjobj = new JSONObject();
        String msg="";
        boolean issuccess=false;
        //Session session=null;
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String subdomain=sessionHandlerImpl.getCompanySessionObj(request).getCdomain();
            String posURL = this.getServletContext().getInitParameter("posURL");
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", sessionHandlerImpl.getUserid(request));
            userData.put("companyid", companyid);
            userData.put("subdomain", subdomain);
            //session=HibernateUtil.getCurrentSession();
            String action = "32";
            HashMap<String, Object> requestParams = productHandler.getProductRequestMap(request);
            KwlReturnObject result = accProductObj.getProducts(requestParams);
            List list = result.getEntityList();
            int count = result.getRecordTotalCount();
            int batchCount=1;
            int toIndex=result.getRecordTotalCount();
            int i=1;
            if (count > 20) {
                batchCount = count / 20;
                toIndex=19;
                i=count% 20==0?1:0;
            }
            int fromIndex = 0;
            JSONArray DataJArr = null;
                for (int j=i ; j <= batchCount; j++) {
                    DataJArr = productHandler.getProductsJson(requestParams, list.subList(fromIndex, toIndex), accProductObj, null, accountingHandlerDAOobj,accCurrencyDAOobj, false);

                //  JSONArray DataJArr = getProductsJson(request, list);
                pjobj.put("productdata", DataJArr);
                pjobj.put("totalCount", count);
                pjobj.put("success", issuccess);
                pjobj.put("msg", msg);


                //  JSONObject pjobj=accProductService.getProducts(request, response);
                userData.put("data", pjobj);
                JSONObject resObj = apiCallHandlerService.callApp(posURL, userData, companyid, action);
                if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                    issuccess = resObj.getBoolean("success");
                    msg = resObj.getString("msg");
                    jobj.put("success", true);
                    jobj.put("msg", msg);
                    jobj.put("companyexist", resObj.optBoolean("companyexist"));
                } else {
                    issuccess = true;
                    msg = messageSource.getMessage("acc.PSOProduct.Warning", null, RequestContextUtils.getLocale(request));
                    jobj.put("success", true);
                    jobj.put("msg", msg);
                    jobj.put("companyexist", true);
                    break;
                }
                fromIndex += 20;
                     toIndex+=(count%20!=0 && toIndex+20<count)?20:batchCount==j+1 && toIndex+20>count?count%20:20;
            }

        } catch (Exception ex) {
            throw ServiceException.FAILURE("invManager.insertAccProduct", ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(NewCompanySetupController.class.getName()).log(Level.SEVERE, null, ex);
            }
//            HibernateUtil.closeSession(session);No need to close seesion hibernate manage it automatically
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    /*
     * Below function used to move the Shared files from Accounting Specific folder to Shared Folder
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
}