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

import com.krawler.common.admin.*;
import com.krawler.common.admin.DefaultHeader;
import com.krawler.common.admin.FieldComboData;
import com.krawler.common.admin.FieldParams;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.admin.Modules;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.FieldConstants;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.common.util.StringUtil;
import com.krawler.customFieldMaster.fieldDataManager;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.depreciation.accDepreciationDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.uom.accUomDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.CommonFnController;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.krawler.common.admin.AuditAction;
import com.krawler.common.util.*;
import com.krawler.esp.utils.ConfigReader;
import com.krawler.spring.accounting.accountservice.AccAccountService;
import com.krawler.spring.accounting.companypreferences.accCompanyPreferencesControllerCMN;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.masteritems.accMasterItemsDAO;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.common.kwlCommonTablesDAO;
import java.text.DateFormat;
import java.text.ParseException;
import com.krawler.spring.accounting.invoice.accInvoiceCMN;
import com.krawler.spring.accounting.invoice.accInvoiceController;
import static com.krawler.spring.accounting.tax.TaxConstants.ACCOUNTID;
import static com.krawler.spring.accounting.tax.TaxConstants.ACCOUNTNAME;
import static com.krawler.spring.accounting.tax.TaxConstants.COMPANYID;
import static com.krawler.spring.accounting.tax.TaxConstants.COUNT;
import static com.krawler.spring.accounting.tax.TaxConstants.DATA;
import static com.krawler.spring.accounting.tax.TaxConstants.MSG;
import static com.krawler.spring.accounting.tax.TaxConstants.SUCCESS;
import static com.krawler.spring.accounting.tax.TaxConstants.TAXID;
import static com.krawler.spring.accounting.tax.TaxConstants.TAXTYPEID;
import com.krawler.spring.accounting.tax.accTaxController;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.fieldManagerDAO;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.permissionHandler.permissionHandlerDAO;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import static com.krawler.spring.accounting.currency.CurrencyContants.EXCHANGERATE;
import static com.krawler.spring.accounting.currency.CurrencyContants.FROMCURRENCY;
import static com.krawler.spring.accounting.currency.CurrencyContants.FROMCURRENCYID;
import static com.krawler.spring.accounting.currency.CurrencyContants.HTMLCODE;
import static com.krawler.spring.accounting.currency.CurrencyContants.ID;
import static com.krawler.spring.accounting.currency.CurrencyContants.NEWEXCHANGERATE;
import static com.krawler.spring.accounting.currency.CurrencyContants.SYMBOL;
import static com.krawler.spring.accounting.currency.CurrencyContants.TOCURRENCY;
import static com.krawler.spring.accounting.currency.CurrencyContants.TOCURRENCYID;
import static com.krawler.spring.accounting.currency.CurrencyContants.TODATE;
import static com.krawler.spring.accounting.currency.CurrencyContants.FOREIGNTOBASEEXCHANGERATE;
import static com.krawler.spring.accounting.handler.AccountingManager.getGlobalCurrencyidFromRequest;
import com.krawler.spring.accounting.handler.CommonFunctions;
import static com.krawler.spring.accounting.tax.TaxConstants.APPLYDATE;
import static com.krawler.spring.accounting.tax.TaxConstants.GST_ACCOUNT_ID;
import static com.krawler.spring.accounting.tax.TaxConstants.GST_ACCOUNT_NAME;
import static com.krawler.spring.accounting.tax.TaxConstants.MASTERTYPEVALUE;
import static com.krawler.spring.accounting.tax.TaxConstants.PERCENT;
import static com.krawler.spring.accounting.tax.TaxConstants.TAXCODE;
import static com.krawler.spring.accounting.tax.TaxConstants.TAXDESCRIPTION;
import static com.krawler.spring.accounting.tax.TaxConstants.TAXNAME;
import static com.krawler.spring.accounting.tax.TaxConstants.TAXTYPE;
import java.io.*;
import java.text.*;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.hql.accounting.masteritems.service.AccMasterItemsService;
import com.krawler.inventory.model.stockout.StockAdjustmentCustomData;
import com.krawler.spring.accounting.customer.accCustomerController;
import com.krawler.spring.accounting.handler.CompanySetupThread;
import com.krawler.spring.accounting.masteritems.accMasterItemsController;
import com.krawler.spring.accounting.product.productHandler;
import com.krawler.spring.companyDetails.companyDetailsDAO;
import com.krawler.spring.mrp.WorkOrder.WorkOrderCustomData;
import com.krawler.spring.mrp.contractmanagement.MRPContractCustomData;
import com.krawler.spring.mrp.jobwork.JobWorkCustomData;
import com.krawler.spring.mrp.labormanagement.LabourCustomData;
import com.krawler.spring.mrp.machinemanagement.MachineCustomData;
import com.krawler.spring.mrp.routingmanagement.RoutingTemplateCustomData;
import com.krawler.spring.mrp.workcentremanagement.WorkCentreCustomData;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;
import org.springframework.web.servlet.support.RequestContextUtils;
import com.krawler.spring.accounting.handler.AccountingHandlerService;

/**
 *
 * @author krawler
 */
public class accAccountControllerCMN extends MultiActionController implements MessageSourceAware{
    private HibernateTransactionManager txnManager;
    private accAccountDAO accAccountDAOobj;
    private accProductDAO accProductObj;
    private accUomDAO accUOMObj;
    private accJournalEntryDAO accJournalEntryobj;
    private accPaymentDAO accPaymentDAOobj;
    private accTaxDAO accTaxObj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private String successView;
    private accCurrencyDAO accCurrencyDAOobj;
    private accDepreciationDAO accDepreciationDAOobj;
    private MessageSource messageSource;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private AccountMapHandler accountMapHandler;
    private auditTrailDAO auditTrailObj;
    private accMasterItemsDAO accMasterItemsDAOobj;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private fieldDataManager fieldDataManagercntrl;
    private accVendorDAO accVendorDAOobj;
    private accCustomerDAO accCustomerDAOobj;
    private accInvoiceCMN accInvoiceCommon;
    private AccAccountService accAccountService;
    public ImportHandler importHandler;
    private ImportDAO importDao;
    private accCusVenMapDAO accCusVenMapDAOObj;
    private accAccountControllerCMNService accAccountControllerCMNServiceObj;
    private fieldManagerDAO fieldManagerDAOobj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private permissionHandlerDAO permissionHandlerDAOObj;
    private importMasterconfig importmasterConfig;
    private APICallHandlerService apiCallHandlerService;
    private companyDetailsDAO companyDetailsDAOObj;
    private CompanySetupThread companySetupThread;
    private AccMasterItemsService accMasterItemsService;
    private String auditMsg="",auditID="";
    private com.krawler.spring.common.fieldDataManager fieldDataManagerNew;
    private AccountingHandlerService accountingHandlerServiceObj;

    @Override
    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }
        
    public void setImportmasterConfig(importMasterconfig importmasterConfig) {
        this.importmasterConfig = importmasterConfig;
    }

   public void setaccMasterItemsDAO(accMasterItemsDAO accMasterItemsDAOobj) {
        this.accMasterItemsDAOobj = accMasterItemsDAOobj;
    }
    public void setaccDepreciationDAO(accDepreciationDAO accDepreciationDAOobj) {
        this.accDepreciationDAOobj = accDepreciationDAOobj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj) {
        this.auditTrailObj = auditTrailDAOObj;
    }
        
	public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    public void setAccountMapHandler(AccountMapHandler accountMapHandler) {
        this.accountMapHandler = accountMapHandler;
    }
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public void setaccUomDAO(accUomDAO accUOMObj){
        this.accUOMObj=accUOMObj;
    } 
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }
    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }
    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }    
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    public void setAccInvoiceCommon(accInvoiceCMN accInvoiceCommon) {
        this.accInvoiceCommon = accInvoiceCommon;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }   
     public void setaccVendorDAO(accVendorDAO accVendorDAOobj) {
        this.accVendorDAOobj = accVendorDAOobj;
    }
      public void setaccCustomerDAO(accCustomerDAO accCustomerDAOobj) {
        this.accCustomerDAOobj = accCustomerDAOobj;
    }
    public void setAccAccountService(AccAccountService accAccountService) {
        this.accAccountService = accAccountService;
    }
    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }
    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }  
    public void setAccCusVenMapDAOObj(accCusVenMapDAO accCusVenMapDAOObj) {
        this.accCusVenMapDAOObj = accCusVenMapDAOObj;
    }
      
    public void setaccAccountControllerCMNServiceObj(accAccountControllerCMNService accAccountControllerCMNServiceObj) {
        this.accAccountControllerCMNServiceObj = accAccountControllerCMNServiceObj;
    }
    public void setFieldManagerDAO(fieldManagerDAO fieldManagerDAOobj) {
        this.fieldManagerDAOobj = fieldManagerDAOobj;
    }
    public void setaccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }
    
    public void setpermissionHandlerDAO(permissionHandlerDAO permissionHandlerDAOObj1) {
        this.permissionHandlerDAOObj = permissionHandlerDAOObj1;
    }
    
    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {
        this.apiCallHandlerService = apiCallHandlerService;
    }
    public void setcompanyDetailsDAO(companyDetailsDAO companyDetailsDAOObj1) {
        this.companyDetailsDAOObj = companyDetailsDAOObj1;
    }
    
    public void setCompanySetupThread(CompanySetupThread companySetupThread) {
        this.companySetupThread = companySetupThread;
    }
    
     public void setAccMasterItemsService(AccMasterItemsService accMasterItemsService) {
        this.accMasterItemsService = accMasterItemsService;
    }

    public void setFieldDataManagerNew(com.krawler.spring.common.fieldDataManager fieldDataManagerNew) {
        this.fieldDataManagerNew = fieldDataManagerNew;
    }
    
    public void setAccountingHandlerServiceObj(AccountingHandlerService accountingHandlerServiceObj) {
        this.accountingHandlerServiceObj = accountingHandlerServiceObj;
    }

//      public ModelAndView getAccountsForCombo(HttpServletRequest request, HttpServletResponse response) {
//        JSONObject jobj = new JSONObject();
//        JSONArray jArr=new JSONArray();
//        boolean issuccess = true;
//        boolean isCOA=false;
//        boolean headerAdded=false;
//        boolean consolidateAccMapFlag=false;
//        boolean levelFlag=false;
//        //Flag to put child accounts in rec.
//        boolean childAccountsFlag=false;
//        boolean isFixedAsset=false;        
//        String msg = "";
//        try{
//            String start = request.getParameter(Constants.start);
//            String limit = request.getParameter(Constants.limit);
//            consolidateAccMapFlag =Boolean.parseBoolean((String)request.getParameter("consolidateAccMapFlag"));
//            levelFlag =Boolean.parseBoolean((String)request.getParameter("levelFlag"));
//            childAccountsFlag = request.getParameter("childAccountsFlag") != null ?Boolean.parseBoolean((String)request.getParameter("childAccountsFlag")):false;
//            String companyid = sessionHandlerImpl.getCompanyid(request);
//            HashMap<String, Object> requestParams = accAccountHandler.getRequestMap(request);
//            requestParams.put("templateid", request.getParameter("templateid"));//  Custom Layout - filter accounts if already mapped in the selected template
//            requestParams.put("ignorePaging",true);// ERP-13570
//             requestParams.put("companyid", sessionHandlerImpl.getCompanyid(request));
//            requestParams.put("userid", sessionHandlerImpl.getUserid(request));
//            String query = "";
//            String customdatajoin = "";
//            String usercondition = "";
//              /**
//             * This Function will use when Users Visibility Feature is Enable
//             * Append user condition while querying data
//             */
//            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", requestParams.get("companyid").toString());
//            if (extraPref != null && extraPref.isUsersVisibilityFlow()) {
//                KwlReturnObject object = accountingHandlerDAOobj.getObject(User.class.getName(), requestParams.get("userid").toString());
//                User user = object.getEntityList().size() > 0 ? (User) object.getEntityList().get(0) : null;
//                if (!AccountingManager.isCompanyAdmin(user)) {
//                    /**
//                     * if Users visibility enable and current user is not admin
//                     */
//                    Map<String, Object> reqMap = new HashMap();
//                    requestParams.put("isUserVisibilityFlow", true);
//                    reqMap.put("companyid", requestParams.get("companyid").toString());
//                    reqMap.put("userid", requestParams.get("userid").toString());
//                    reqMap.put("jointable", "acd");
//                    reqMap.put("moduleid", Constants.Account_Statement_ModuleId);
//                    String custcondition = fieldManagerDAOobj.appendUsersCondition(reqMap);
//                    if (!StringUtil.isNullOrEmpty(custcondition)) {
//                        /**
//                         * If mapping found with dimension
//                         */
//                        usercondition = " and (" + custcondition + ")";
//                        requestParams.put("appendusercondtion", usercondition);
//                    } else {
//                        /**
//                         * If no Mapping found for current user then return
//                         * function call
//                         */
//                        
//                        JSONArray pagedJson1 = jArr;
//                        jobj.put("data", pagedJson1);
//                        jobj.put("totalCount", jArr);
//
//                        jobj.put("success", issuccess);
//                        jobj.put("msg", msg);
//                        return new ModelAndView("jsonView", "model", jobj.toString());
//                    }
//                }
//            }
//            KwlReturnObject result = accAccountDAOobj.getAccountsForCombo(requestParams);
//            List list = result.getEntityList();
//            ArrayList resultlist = new ArrayList();
//            if(requestParams.containsKey("COA")){isCOA =Boolean.parseBoolean((String)requestParams.get("COA"));}
//            if(requestParams.containsKey("headerAdded")){headerAdded =Boolean.parseBoolean((String)requestParams.get("headerAdded"));}
//            if(requestParams.containsKey("isFixedAsset")){isFixedAsset =Boolean.parseBoolean((String)requestParams.get("isFixedAsset"));}
//            boolean ignoreCustomers=requestParams.get("ignorecustomers")!=null;
//            boolean ignoreVendors=requestParams.get("ignorevendors")!=null;
//            boolean ignoreTransactionFlag=request.getParameter("ignoreTransactionFlag")!=null?Boolean.parseBoolean(request.getParameter("ignoreTransactionFlag")):true;
//            boolean isIBGAccount = request.getParameter("isIBGAccount")!=null?Boolean.parseBoolean(request.getParameter("isIBGAccount")):true;
//            boolean isForBS_PL_to_GL = false;
//            boolean isForPaymentReceipt = request.getParameter("isForPaymentReceipt")!=null?Boolean.parseBoolean(request.getParameter("isForPaymentReceipt")):false;
//            boolean isForJE = request.getParameter("isForJE")!=null?Boolean.parseBoolean(request.getParameter("isForJE")):false;
//            if(!StringUtil.isNullOrEmpty(request.getParameter("isForBS_PL_to_GL"))){
//                isForBS_PL_to_GL=Boolean.parseBoolean(request.getParameter("isForBS_PL_to_GL"));
//            }
//            String excludeaccountid = (String) requestParams.get("accountid");
//            String includeaccountid = (String) requestParams.get("includeaccountid");
//            String includeparentid = (String) requestParams.get("includeparentid");
//            String customerCpath = ConfigReader.getinstance().get("Customer");
//            String vendorCpath = ConfigReader.getinstance().get("Vendor");
//            
//            boolean isCustomers=requestParams.get("isCustomer")!=null?Boolean.parseBoolean(request.getParameter("isCustomer")):true;
//            boolean isVendors=requestParams.get("isVendor")!=null?Boolean.parseBoolean(request.getParameter("isVendor")):true;
////            boolean deleted =Boolean.parseBoolean((String)requestParams.get("deleted"));
////            boolean nondeleted =Boolean.parseBoolean((String)requestParams.get("nondeleted"));
//            String currencyid=(String)requestParams.get("currencyid");
//            KWLCurrency currency = (KWLCurrency)kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.KWLCurrency", currencyid);
//            
//            Iterator itr = list.iterator();
////            int level=0;
//            ExtraCompanyPreferences extrapref = null;
//            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
//            extrapref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
//            String countryid = extrapref != null ? extrapref.getCompany().getCountry().getID() : null;
//            /*
//             * get Accounts custom data for Make Payment/Receive Payment only.
//             * ERP-32814
//             */
//            Map<String, Object> variableMap = new HashMap<>();
//            HashMap<String, String> replaceFieldMap = new HashMap<>();
//            HashMap<String, String> customFieldMap = new HashMap<>();
//            HashMap<String, String> customDateFieldMap = new HashMap<>();
//            HashMap<String, Integer> FieldMap = null;
//            if (isForPaymentReceipt) {
//                HashMap<String, Object> fieldrequestParams = new HashMap<>();
//                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
//                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Account_Statement_ModuleId, 0));
//                FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
//            }
//            while (itr.hasNext()) {
//                Object listObj = itr.next();
//                Account account = (Account) listObj;
//                if(excludeaccountid!=null&&account.getID().equals(excludeaccountid)) continue;
//                if((includeparentid!=null&&(!account.getID().equals(includeparentid)||(account.getParent()!=null&&!account.getParent().getID().equals(includeparentid))))) continue;
//                else if((includeaccountid!=null&&!account.getID().equals(includeaccountid))) continue;
//                if (extrapref != null ? (!extrapref.isExciseApplicable() && account.getName().equals("PLA (Personal Ledger Account) ")) : false) {
//                    continue;
//                }
//                if (!StringUtil.isNullOrEmpty(countryid) ? (countryid.equalsIgnoreCase("105") && (account.getName().equals("GST(Output)") || account.getName().equals("GST(Input)"))) : false) {
//                    continue;
//                }
////                Object c = kwlCommonTablesDAOObj.getClassObject(customerCpath, account.getID());
////                if(ignoreCustomers&&account.getGroup().getID().equals(Group.ACCOUNTS_RECEIVABLE)){
////                    if(c!=null)continue;
////                }
////
////                Object v = kwlCommonTablesDAOObj.getClassObject(vendorCpath, account.getID());
////                if(ignoreVendors&&account.getGroup().getID().equals(Group.ACCOUNTS_PAYABLE)){
////                    if(v!=null)continue;
////                }
////                
////                if ((c != null || v != null) && isCOA) {
////                    continue;
////                }
////Here we have added !isCOA check because acccounts not display when its used in JE or its whose account balance is not zero
////When we want select this account in COA form as parent account then this is not available in COA form                 
////                if (!consolidateAccMapFlag && headerAdded && !isFixedAsset && ignoreTransactionFlag && !isForBS_PL_to_GL && !isCOA) {
////                    KwlReturnObject Count = accAccountDAOobj.getJEDTrasactionfromAccount(account.getID(), companyid);
////                    int jedCount = Count.getRecordTotalCount();
////                    // calculation of opening balance                
////                    double openbalance = accInvoiceCommon.getOpeningBalanceOfAccount(request,account,false,null);
////                    double openingBal=openbalance;//account.getOpeningBalance();
////                    if (!(jedCount==0 && openingBal==0)) {
////                        continue;
////                    }
////                }                 
//                JSONObject obj = new JSONObject();
//                obj.put("accid", account.getID());
//                obj.put("accname", (!StringUtil.isNullOrEmpty(account.getName()))?account.getName():(!StringUtil.isNullOrEmpty(account.getAcccode())?account.getAcccode():""));
//                obj.put("accdesc", StringUtil.isNullOrEmpty(account.getDescription())?"":account.getDescription());
//                obj.put("accountpersontype", 0);
//                obj.put("mappedaccountid", account.getID());
//                obj.put("acctaxcode", (!StringUtil.isNullOrEmpty(account.getTaxid())) ? account.getTaxid() : "");
//                obj.put("masterTypeValue", account.getMastertypevalue());
//                obj.put("groupid", account.getGroup().getID());
//                obj.put("mastergroupid", (account.getGroup()!=null)?account.getGroup().getID():"");
//                obj.put("groupname", account.getGroup().getName());
//                obj.put("creationDate", authHandler.getUserDateFormatterWithoutTimeZone(request).format(account.getCreationDate()));
//                obj.put("acccode", account.getAcccode());
//                obj.put("accnamecode", (!StringUtil.isNullOrEmpty(account.getAcccode())?"["+account.getAcccode()+"] "+account.getName():account.getName()));
//                obj.put("nature", account.getGroup().getNature());
//                obj.put("naturename",(account.getGroup().getNature()==Constants.Liability)?"Liability":(account.getGroup().getNature()==Constants.Asset)?"Asset":(account.getGroup().getNature()==Constants.Expences)?"Expences":(account.getGroup().getNature()==Constants.Income)?"Income":"");
//                obj.put("currencyid",(account.getCurrency()==null?currency.getCurrencyID(): account.getCurrency().getCurrencyID()));
//                obj.put("currencysymbol",(account.getCurrency()==null?currency.getCurrencyID(): account.getCurrency().getSymbol()));
//                obj.put("currencyname",(account.getCurrency()==null?currency.getName(): account.getCurrency().getName()));
//                obj.put("currencycode",(account.getCurrency()==null?currency.getCurrencyCode(): account.getCurrency().getCurrencyCode()));
//                obj.put("prtaxid", StringUtil.isNullOrEmpty(account.getTaxid()) ? "" : account.getTaxid());
//                obj.put("deleted", account.isDeleted());
//                obj.put("aliascode", account.getAliascode()==null?"":account.getAliascode());
//                obj.put("accounttype", account.getAccounttype());
//                obj.put("mastertypevalue", account.getMastertypevalue());
//                obj.put("hasAccess", account.isActivate());
//                obj.put("isactivate", account.isActivate());
//                obj.put("groupid", account.getGroup().getID()==null?"":account.getGroup().getID());
//                obj.put("accounttype", account.getAccounttype());
//                obj.put("natureOfPayment", account.getNatureOfPayment());
//                obj.put("typeofpayment", account.getTypeOfPayment());
//                obj.put("parentid", account.getParent() != null ? account.getParent().getID():"");
//                if(account.getAcccode()!=null)
//                    obj.put("acccode", account.getAcccode());
//                else
//                    obj.put("acccode","");
////                if (c != null || v != null) {
////                    obj.put("isOnlyAccount", "false");
////                } else {
//                    obj.put("isOnlyAccount", "true");
////                }
//                if(levelFlag) {
//                    int level = 0;                    
//                    level = getAccountLevel(account, level);
//                    obj.put("level", level);
//                }
//                
//                if (childAccountsFlag) {
//                    //put array of child accounts ids in rec.
//                    if (account.getChildren() != null) {
//                        List<Account> childList = new ArrayList(account.getChildren());
//                        JSONArray childArr = new JSONArray();
//                        for (Account child : childList) {
//                            childArr.put(child.getID());
//                        }
//                        obj.put("childArr", childArr);
//                    }
//                }
//                
//                if (account.isIBGBank() && isIBGAccount) {
//                    if (account.getIbgBankType() == Constants.DBS_BANK_Type) {   // For DBS bank only
//                        KwlReturnObject ibgDetailResult = accAccountDAOobj.getIBGDetailsForAccount(account.getID(), companyid);
//                        IBGBankDetails ibgDetails = (IBGBankDetails) ibgDetailResult.getEntityList().get(0);
//                        obj.put("dailyBankLimit", ibgDetails.getBankDailyLimit());
//                    }
//                    obj.put("ibgbanktype", account.getIbgBankType());
//                }
//                if(account.getMastertypevalue()==4 && (isForPaymentReceipt || isForJE)){     // GST Account
//                    int taxResultCount=0;
//                    KwlReturnObject taxResult = accAccountDAOobj.getTaxesFromAccountId(account.getID(),companyid);
//                    taxResultCount=taxResult.getEntityList().size();
//                    // Following changes are done for ERP-15044 
//                    if(taxResultCount == 0){
//                        continue;
//                    }
//                    if(taxResultCount==1){
//                        Object object[]= (Object[])taxResult.getEntityList().get(0);
//                        KwlReturnObject resultForTaxObject = accountingHandlerDAOobj.getObject(Tax.class.getName(), object[0].toString());
//                        Tax taxObject = (Tax)resultForTaxObject.getEntityList().get(0);
//                        obj.put("isOneToManyTypeOfTaxAccount", false);
//                        obj.put("appliedGst", taxObject.getID());
//                    } else {
//                        KwlReturnObject taxObjectresult= accountingHandlerDAOobj.getObject(Tax.class.getName(), account.getID());
//                        List taxObjectResultList = taxObjectresult.getEntityList();
//                        if(taxObjectResultList.isEmpty() || taxObjectResultList.get(0)==null){
//                            obj.put("isOneToManyTypeOfTaxAccount", true);
//                        } else {
//                            Object object[]= (Object[])taxResult.getEntityList().get(0);
//                            KwlReturnObject resultForTaxObject = accountingHandlerDAOobj.getObject(Tax.class.getName(), object[0].toString());
//                            Tax taxObject = (Tax)resultForTaxObject.getEntityList().get(0);
//                            obj.put("isOneToManyTypeOfTaxAccount", false);
//                            obj.put("appliedGst", taxObject.getID());
//                        }
//                    }
//                }
//                
//                if(!StringUtil.isNullOrEmpty(account.getUsedIn())){ 
//                    obj.put("haveToPostJe", !account.isWantToPostJe());
//                    obj.put("usedIn",account.getUsedIn());                 
//                } 
//
//                if (isForPaymentReceipt) {
//                    KwlReturnObject idcustresult = accountingHandlerDAOobj.getObject(AccountCustomData.class.getName(), account.getID());
//                    if (idcustresult.getEntityList().size() > 0) {
//                        AccountCustomData jeCustom = (AccountCustomData) idcustresult.getEntityList().get(0);
//                        AccountingManager.setCustomColumnValues(jeCustom, FieldMap, replaceFieldMap, variableMap);
//                        JSONObject params = new JSONObject();
//                        params.put(Constants.companyKey, companyid);
//                        params.put(Constants.isLink, true);
//                        params.put(Constants.linkModuleId, requestParams.get(Constants.requestModuleId));
//                        fieldDataManagerNew.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
//                    }
//                }
////                obj.put("depreciationaccount", account.getDepreciationAccont()==null?"":account.getDepreciationAccont().getID());
////                obj.put("openbalance", account.getOpeningBalance());
////                Account parentAccount = (Account) row[6];
////                if(parentAccount!=null){
////                    obj.put("parentid", parentAccount.getID());
////                    obj.put("parentname", parentAccount.getName());
////                }
////                obj.put("level", row[3]);
////                obj.put("leaf", row[4]);
////                obj.put("presentbalance", account.getPresentValue());
////                obj.put("life", account.getLife());
////                obj.put("salvage", account.getSalvage());
////                obj.put("posted", row[7]);
////                obj.put("creationDate", authHandler.getDateFormatter(request).format(account.getCreationDate()));
////                obj.put("categoryid", account.getCategory()==null?"":account.getCategory().getID());
//                jArr.put(obj);
//            }
//            
//         if(!ignoreCustomers){   
//            result = accAccountDAOobj.getCustomerForCombo(requestParams);
//            List ls = result.getEntityList();
//            Iterator<Object[]> itr1 = ls.iterator();
//            while (itr1.hasNext()) {
//                Object[] row = (Object[]) itr1.next();
//                String customerid = (String) row[0].toString();
//                String customername = (String) row[1].toString();
//                String accountid = (String) row[2].toString();
//                String customercode = (row[3]!=null)?(String) row[3].toString():"";
//                Account account = (Account) kwlCommonTablesDAOObj.getClassObject(Account.class.getName(), accountid);
//
//                if (excludeaccountid != null && customerid.equals(excludeaccountid)) {
//                    continue;
//                }
//
//                JSONObject obj = new JSONObject();
//                obj.put("accid", customerid);
//                obj.put("accname", customername);
//                obj.put("groupid", account.getGroup().getID());
//                obj.put("accountpersontype", 1);
//                obj.put("mappedaccountid", account.getID());
//                obj.put("mastergroupid", (account.getGroup()!=null)?account.getGroup().getID():"");
//                obj.put("groupname", account.getGroup().getName());
//                obj.put("acccode", customercode);
//                obj.put("accnamecode", (!StringUtil.isNullOrEmpty(account.getAcccode()) ? "[" + account.getAcccode() + "] " + account.getName() : account.getName()));
//                obj.put("nature", account.getGroup().getNature());
//                obj.put("naturename", (account.getGroup().getNature() == Constants.Liability) ? "Liability" : (account.getGroup().getNature() == Constants.Asset) ? "Asset" : (account.getGroup().getNature() == Constants.Expences) ? "Expences" : (account.getGroup().getNature() == Constants.Income) ? "Income" : "");
//                obj.put("currencyid", (account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getCurrencyID()));
//                obj.put("currencysymbol", (account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getSymbol()));
//                obj.put("creationDate", authHandler.getUserDateFormatterWithoutTimeZone(request).format(account.getCreationDate()));
//                obj.put("currencyname", (account.getCurrency() == null ? currency.getName() : account.getCurrency().getName()));
//                obj.put("currencycode", (account.getCurrency() == null ? currency.getCurrencyCode() : account.getCurrency().getCurrencyCode()));
//                obj.put("deleted", account.isDeleted());
//                obj.put("aliascode", account.getAliascode() == null ? "" : account.getAliascode());
//
//                jArr.put(obj);
//            }
//         }
//          if(!ignoreVendors){  
//            result = accAccountDAOobj.getVendorForCombo(requestParams);
//            List ls  = result.getEntityList();
//            Iterator<Object[]> itr1 = ls.iterator();
//            while (itr1.hasNext()) {
//                Object[] row = (Object[]) itr1.next();
//                String vendorid = (String) row[0].toString();
//                String vendorname = (String) row[1].toString();
//                String accountid = (String) row[2].toString();
//                 String vendorcode = (row[3]!=null)?(String) row[3].toString():"";
//                Account account = (Account) kwlCommonTablesDAOObj.getClassObject(Account.class.getName(), accountid);
//
//                if (excludeaccountid != null && vendorid.equals(excludeaccountid)) {
//                    continue;
//                }
//
//                JSONObject obj = new JSONObject();
//                obj.put("accid", vendorid);
//                obj.put("accname", vendorname);
//                obj.put("mappedaccountid", account.getID());
//                obj.put("groupid", account.getGroup().getID());
//                obj.put("mastergroupid", (account.getGroup()!=null)?account.getGroup().getID():"");
//                obj.put("groupname", account.getGroup().getName());
//                obj.put("acccode", vendorcode);
//                obj.put("accountpersontype", 2);
//                obj.put("accnamecode", (!StringUtil.isNullOrEmpty(account.getAcccode()) ? "[" + account.getAcccode() + "] " + account.getName() : account.getName()));
//                obj.put("nature", account.getGroup().getNature());
//                obj.put("naturename", (account.getGroup().getNature() == Constants.Liability) ? "Liability" : (account.getGroup().getNature() == Constants.Asset) ? "Asset" : (account.getGroup().getNature() == Constants.Expences) ? "Expences" : (account.getGroup().getNature() == Constants.Income) ? "Income" : "");
//                obj.put("currencyid", (account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getCurrencyID()));
//                obj.put("currencysymbol", (account.getCurrency() == null ? currency.getCurrencyID() : account.getCurrency().getSymbol()));
//                obj.put("creationDate", authHandler.getUserDateFormatterWithoutTimeZone(request).format(account.getCreationDate()));
//                obj.put("currencyname", (account.getCurrency() == null ? currency.getName() : account.getCurrency().getName()));
//                obj.put("currencycode", (account.getCurrency() == null ? currency.getCurrencyCode() : account.getCurrency().getCurrencyCode()));
//                obj.put("deleted", account.isDeleted());
//                obj.put("aliascode", account.getAliascode() == null ? "" : account.getAliascode());
//
//                jArr.put(obj);
//            }
//          }  
//          
//           JSONArray pagedJson = jArr;
//            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
//                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
//            }
//            jobj.put("data", pagedJson);
//            jobj.put("totalCount", jArr.length());
//        } catch (SessionExpiredException ex) {
//            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
//            issuccess = false;
//            msg = ex.getMessage();
//        } catch(Exception ex) {
//            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
//            issuccess = false;
//            msg = ""+ex.getMessage();
//        } finally {
//            try {
//                jobj.put("success", issuccess);
//                jobj.put("msg", msg);
//            } catch (JSONException ex) {
//                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
//        return new ModelAndView("jsonView", "model", jobj.toString());
//    }
      
    public ModelAndView getAccountsForCombo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = true;
        boolean levelFlag = false;
        //Flag to put child accounts in rec.
        boolean childAccountsFlag = false;
        boolean isFixedAsset = false;
        String msg = "";
        System.out.println("***************************************************a");
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(new Date());

        try {
            String start = request.getParameter(Constants.start);
            String limit = request.getParameter(Constants.limit);
            levelFlag = Boolean.parseBoolean((String) request.getParameter("levelFlag"));
            childAccountsFlag = request.getParameter("childAccountsFlag") != null ? Boolean.parseBoolean((String) request.getParameter("childAccountsFlag")) : false;
            boolean isForPaymentReceipt = request.getParameter("isForPaymentReceipt") != null ? Boolean.parseBoolean(request.getParameter("isForPaymentReceipt")) : false;
            boolean isIBGAccount = request.getParameter("isIBGAccount") != null ? Boolean.parseBoolean(request.getParameter("isIBGAccount")) : true;
            boolean isForJE = request.getParameter("isForJE") != null ? Boolean.parseBoolean(request.getParameter("isForJE")) : false;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> requestParams = accAccountHandler.getRequestMap(request);
            requestParams.put("templateid", request.getParameter("templateid"));//  Custom Layout - filter accounts if already mapped in the selected template
            requestParams.put("ignorePaging", true);// ERP-13570
            requestParams.put("companyid", companyid);
            requestParams.put("userid", sessionHandlerImpl.getUserid(request));
            boolean ignoreCustomers = requestParams.get("ignorecustomers") != null;
            boolean ignoreVendors = requestParams.get("ignorevendors") != null;
            String usercondition = "";
            String excludeaccountid = (String) requestParams.get("accountid");
            String includeaccountid = (String) requestParams.get("includeaccountid");
            String includeparentid = (String) requestParams.get("includeparentid");
            /**
             * This Function will use when Users Visibility Feature is Enable
             * Append user condition while querying data
             */
//            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", requestParams.get("companyid").toString());
            ExtraCompanyPreferences extraPref = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            extraPref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            String countryid = extraPref != null ? extraPref.getCompany().getCountry().getID() : null;
            /*
             * get Accounts custom data for Make Payment/Receive Payment only.
             * ERP-32814
             */
            Map<String, Object> variableMap = new HashMap<>();
            HashMap<String, String> replaceFieldMap = new HashMap<>();
            HashMap<String, String> customFieldMap = new HashMap<>();
            HashMap<String, String> customDateFieldMap = new HashMap<>();
            HashMap<String, Integer> FieldMap = null;
            if (isForPaymentReceipt) {
                HashMap<String, Object> fieldrequestParams = new HashMap<>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Account_Statement_ModuleId, 0));
                FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            }
            if (extraPref != null && extraPref.isUsersVisibilityFlow()) {
                KwlReturnObject object = accountingHandlerDAOobj.getObject(User.class.getName(), requestParams.get("userid").toString());
                User user = object.getEntityList().size() > 0 ? (User) object.getEntityList().get(0) : null;
                if (!AccountingManager.isCompanyAdmin(user)) {
                    /**
                     * if Users visibility enable and current user is not admin
                     */
                    Map<String, Object> reqMap = new HashMap();
                    requestParams.put("isUserVisibilityFlow", true);
                    reqMap.put("companyid", requestParams.get("companyid").toString());
                    reqMap.put("userid", requestParams.get("userid").toString());
                    reqMap.put("jointable", "acd");
                    reqMap.put("moduleid", Constants.Account_Statement_ModuleId);

                    String custcondition = fieldManagerDAOobj.appendUsersCondition(reqMap);
                    if (!StringUtil.isNullOrEmpty(custcondition)) {
                        /**
                         * If mapping found with dimension
                         */
                        usercondition = " and (" + custcondition + ")";
                        requestParams.put("appendusercondtion", usercondition);
                    } else {
                        /**
                         * If no Mapping found for current user then return
                         * function call
                         */

                        JSONArray pagedJson1 = jArr;
                        jobj.put("data", pagedJson1);
                        jobj.put("totalCount", jArr);

                        jobj.put("success", issuccess);
                        jobj.put("msg", msg);
                        return new ModelAndView("jsonView", "model", jobj.toString());
                    }
                }
            }
            if (extraPref != null) {
                requestParams.put("accountsortingflag", extraPref.isAccountSortingFlag());
            }
            KwlReturnObject result = accAccountDAOobj.getAccountsForCombo(requestParams);
            List ls = result.getEntityList();
            Iterator<Object[]> itr = ls.iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();

                JSONObject obj = new JSONObject();
                String accountid = (String) row[0].toString();//accountid
                String accountgroupname = row[6] != null ? (String) row[6].toString() : "";
                String accountname = (row[2] != null ? (String) row[2].toString() : "");
                obj.put("accid", accountid);
                obj.put("mappedaccountid", accountid);
                obj.put("accname", row[1] != null ? (String) row[1].toString() : (row[2] != null ? (String) row[2].toString() : ""));//accountname
                obj.put("acccode", row[2] != null ? (String) row[2].toString() : "");//accountcode
                obj.put("accdesc", row[3] != null ? (String) row[3].toString() : "");//accountdescription
                obj.put("accnamecode", (row[1] != null && row[2] != null && !StringUtil.isNullOrEmpty(row[2].toString()) ? "[" + row[2].toString() + "] " + row[1].toString() : row[1].toString()));
                obj.put("acctaxcode", row[4] != null ? (String) row[4].toString() : "");//account tax code
                obj.put("groupid", row[5] != null ? (String) row[5].toString() : "");//account group id
                obj.put("mastergroupid", (row[5] != null) ? row[5].toString() : "");
                obj.put("groupname", accountgroupname);//account group name
                String hasAccess=(String) row[7].toString();
                obj.put("hasAccess", row[7] != null ? hasAccess.equalsIgnoreCase("T")?true:false : "");//hasAccess
                if (row[8] != null && !StringUtil.isNullOrEmpty(row[8].toString())) {
                    obj.put("haveToPostJe", row[9] != null ? !("T".equals(row[9].toString())) : false);
                    obj.put("usedIn", row[8] != null ? (String) row[8].toString() : "");//used in 
                }
                int groupnature = (int) ((row[20] != null) ? (Integer) row[20] : "");
                int masterTypevalue = (int) (row[11] != null ? (Integer) row[11] : "");
                String parentaccountid= row[18] != null ? (String) row[18].toString() : "";
                obj.put("creationDate", row[10] != null ? authHandler.getUserDateFormatterWithoutTimeZone(request).format(row[10]) : "");//creation date
                obj.put("mastertypevalue", masterTypevalue);//masterTypeValue
                obj.put("masterTypeValue", masterTypevalue);//masterTypeValue
                obj.put("currencyid", row[12] != null ? (String) row[12].toString() : "");//currencyid
                obj.put("prtaxid", row[13] != null ? (String) row[13].toString() : "");//acount tax id
                obj.put("aliascode", row[14] != null ? (String) row[14].toString() : "");//acount alias code
                obj.put("accounttype", row[15] != null ? (String) row[15].toString() : "");//acount type
                obj.put("parentid",parentaccountid );//parentid
                obj.put("deleted", row[19] != null ? Boolean.parseBoolean(row[19].toString()) : false);//Account is deleted
                obj.put("nature", groupnature);//account group nature
                obj.put("currencysymbol", row[21] != null ? (String) row[21].toString() : "");//currencysymbol
                obj.put("currencycode", row[22] != null ? (String) row[22].toString() : "");//currencycode
                obj.put("currencyname", row[23] != null ? (String) row[23].toString() : "");//currencyname
                obj.put("naturename", (groupnature == Constants.Liability) ? "Liability" : (groupnature == Constants.Asset) ? "Asset" : (groupnature == Constants.Expences) ? "Expences" : (groupnature == Constants.Income) ? "Income" : "");
                obj.put("accountpersontype", 0);
                obj.put("isOnlyAccount", "true");
                boolean isIBGBank = (row[24] == null || row[24].toString().equals("F")) ? false : true;//isIBGBank
                int IbgBankType = (int) ((row[25] != null) ? (Integer) row[25] : 0);//IbgBankType
                boolean isaccActivate = (row[26] == null || row[26].toString().equals("F")) ? false : true;//isaccActivate
                obj.put("isactivate", isaccActivate);
                if (excludeaccountid != null && accountid.equals(excludeaccountid)) {
                    continue;
                }
                if ((includeparentid != null && (!accountid.equals(includeparentid) || (parentaccountid != null && !parentaccountid.equals(includeparentid))))) {
                    continue;
                } else if ((includeaccountid != null && !accountid.equals(includeaccountid))) {
                    continue;
                }
                if (extraPref != null ? (!extraPref.isExciseApplicable() && accountname.equals("PLA (Personal Ledger Account) ")) : false) {
                    continue;
                }
                if (!StringUtil.isNullOrEmpty(countryid) ? (countryid.equalsIgnoreCase("105") && (accountname.equals("GST(Output)") || accountname.equals("GST(Input)"))) : false) {
                    continue;
                }
                
                if (levelFlag || childAccountsFlag) {
                    KwlReturnObject retObj = accountingHandlerDAOobj.getObject(Account.class.getName(), row[0].toString());
                    Account account = (Account) retObj.getEntityList().get(0);

                    if (levelFlag) {
                        int level = 0;
                        level = getAccountLevel(account, level);
                        obj.put("level", level);
                    }

                    if (childAccountsFlag) {
                        //put array of child accounts ids in rec.
                        if (account.getChildren() != null) {
                            List<Account> childList = new ArrayList(account.getChildren());
                            JSONArray childArr = new JSONArray();
                            for (Account child : childList) {
                                childArr.put(child.getID());
                            }
                            obj.put("childArr", childArr);
                        }
                    }
                }
                if (isIBGBank && isIBGAccount) {
                    if (IbgBankType == Constants.DBS_BANK_Type) {   // For DBS bank only
                        KwlReturnObject ibgDetailResult = accAccountDAOobj.getIBGDetailsForAccountSQL(accountid, companyid);
                        List<Double> lst = ibgDetailResult.getEntityList();
                        if (lst != null && !lst.isEmpty()) {
                            for (Double object : lst) {
                                obj.put("dailyBankLimit", object);
                            }
                        }
                    }
                    obj.put("ibgbanktype", IbgBankType);
                }
                if (masterTypevalue == 4 && (isForPaymentReceipt || isForJE)) {     // GST Account
                    int taxResultCount = 0;
                    KwlReturnObject taxResult;
                    if (extraPref != null && extraPref.isIsNewGST()) {
                        taxResult = accAccountDAOobj.getTaxesAndTermsUsingAccountId(row[0].toString(), companyid);
                    } else {
                        /*
                        * The params send through hashmap taxFromAccountParams to add new params and make function reusable.
                        */
                        HashMap<String, Object> taxFromAccountParams = new HashMap();
                        taxFromAccountParams.put("accountid",row[0].toString());
                        taxFromAccountParams.put("companyid",companyid);
                        taxResult = accAccountDAOobj.getTaxesFromAccountId(taxFromAccountParams);
                    }
                    taxResultCount = taxResult.getEntityList().size();
                    // Following changes are done for ERP-15044 
                    if (taxResultCount == 0) {
                        continue;
                    }
                    if (taxResultCount == 1) {
                    Object object[] = (Object[]) taxResult.getEntityList().get(0);
//                        KwlReturnObject resultForTaxObject = accountingHandlerDAOobj.getObject(Tax.class.getName(), object[0].toString());
//                        Tax taxObject = (Tax) resultForTaxObject.getEntityList().get(0);
                        obj.put("isOneToManyTypeOfTaxAccount", false);
                        obj.put("appliedGst", object[0]!=null?(String) object[0].toString():"");
                    } else {
                        KwlReturnObject taxObjectresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), row[0].toString());
                        List taxObjectResultList = taxObjectresult.getEntityList();
                        if (taxObjectResultList.isEmpty() || taxObjectResultList.get(0) == null) {
                            obj.put("isOneToManyTypeOfTaxAccount", true);
                        } else {
                            Object object[] = (Object[]) taxResult.getEntityList().get(0);
//                            KwlReturnObject resultForTaxObject = accountingHandlerDAOobj.getObject(Tax.class.getName(), object[0].toString());
//                            Tax taxObject = (Tax) resultForTaxObject.getEntityList().get(0);
                            obj.put("isOneToManyTypeOfTaxAccount", false);
                            obj.put("appliedGst", object[0]!=null?(String) object[0].toString():"");
                        }
                    }
                }

                if (isForPaymentReceipt) {
                    KwlReturnObject idcustresult = accountingHandlerDAOobj.getObject(AccountCustomData.class.getName(), row[0].toString());
                    if (idcustresult.getEntityList().size() > 0) {
                        AccountCustomData jeCustom = (AccountCustomData) idcustresult.getEntityList().get(0);
                        AccountingManager.setCustomColumnValues(jeCustom, FieldMap, replaceFieldMap, variableMap);
                        JSONObject params = new JSONObject();
                        params.put(Constants.companyKey, companyid);
                        params.put(Constants.isLink, true);
                        params.put(Constants.linkModuleId, requestParams.get(Constants.requestModuleId));
                        fieldDataManagerNew.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                    }
                }
                jArr.put(obj);
            }
            if (!ignoreCustomers) {
                result = accAccountDAOobj.getCustomerForCombo(requestParams);
                List custls = result.getEntityList();
                if(custls!=null && !custls.isEmpty()){
                    Iterator<Object[]> itr1 = custls.iterator();
                    while (itr1.hasNext()) {
                        Object[] row = (Object[]) itr1.next();
                        String customerid = (String) row[0].toString();
                        String customername = (String) row[1].toString();
                        String accountid = (String) row[2].toString();
                        String customercode = (row[3] != null) ? (String) row[3].toString() : "";
                        String customercurrencyid = (row[4] != null) ? (String) row[4].toString() : "";
                        String customercurrencysymbol = (row[5] != null) ? (String) row[5].toString() : "";
                        String customercurrencycode = (row[6] != null) ? (String) row[6].toString() : "";
                        String customercurrencyname = (row[7] != null) ? (String) row[7].toString() : "";
                        boolean deleted = (row[8] != null) ? Boolean.parseBoolean(row[8].toString()) : false;
                        String acccode = (row[9] != null) ? (String) row[9].toString() : "";
                        String groupid = (row[10] != null) ? (String) row[10].toString() : "";
                        String groupname = (row[11] != null) ? (String) row[11].toString() : "";
                        int groupnature = (int) ((row[12] != null) ? (Integer) row[12] : "");
                        Date acccreationdate = (Date) ((row[13] != null) ? (Date) row[13] : "");
//                    Account account = (Account) kwlCommonTablesDAOObj.getClassObject(Account.class.getName(), accountid);

                        if (excludeaccountid != null && customerid.equals(excludeaccountid)) {
                            continue;
                        }

                        JSONObject obj = new JSONObject();
                        obj.put("accid", customerid);
                        obj.put("accname", customername);
                        obj.put("groupid", groupid);
                        obj.put("accountpersontype", 1);
                        obj.put("mappedaccountid", accountid);
                        obj.put("mastergroupid", groupid);
                        obj.put("groupname", groupname);
                        obj.put("acccode", customercode);
//                    obj.put("accnamecode", groupname);
                        obj.put("accnamecode", (!StringUtil.isNullOrEmpty(groupname) ? "[" + groupname + "] " + groupname : groupname));
                        obj.put("nature", groupnature);
                        obj.put("naturename", (groupnature == Constants.Liability) ? "Liability" : (groupnature == Constants.Asset) ? "Asset" : (groupnature == Constants.Expences) ? "Expences" : (groupnature == Constants.Income) ? "Income" : "");
                        obj.put("currencyid", customercurrencyid);
                        obj.put("currencysymbol", customercurrencysymbol);
                        obj.put("creationDate", authHandler.getUserDateFormatterWithoutTimeZone(request).format(acccreationdate));
                        obj.put("currencyname", customercurrencyname);
                        obj.put("currencycode", customercurrencycode);
                        obj.put("deleted", deleted);
                        obj.put("aliascode", acccode);

                        jArr.put(obj);
                    }
                }
            }
            if (!ignoreVendors) {
                result = accAccountDAOobj.getVendorForCombo(requestParams);
                List venls = result.getEntityList();
                if(venls!=null && !venls.isEmpty()){
                    Iterator<Object[]> itr1 = venls.iterator();
                    while (itr1.hasNext()) {
                        Object[] row = (Object[]) itr1.next();
                        String vendorid = (String) row[0].toString();
                        String vendorname = (String) row[1].toString();
                        String accountid = (String) row[2].toString();
                        String vendorcode = (row[3] != null) ? (String) row[3].toString() : "";
                        String vendorcurrencyid = (row[4] != null) ? (String) row[4].toString() : "";
                        String vendorcurrencysymbol = (row[5] != null) ? (String) row[5].toString() : "";
                        String vendorcurrencycode = (row[6] != null) ? (String) row[6].toString() : "";
                        String vendorcurrencyname = (row[7] != null) ? (String) row[7].toString() : "";
                        String deleted = (row[8] != null) ? (String) row[8].toString() : "";
                        String acccode = (row[9] != null) ? (String) row[9].toString() : "";
                        String groupid = (row[10] != null) ? (String) row[10].toString() : "";
                        String groupname = (row[11] != null) ? (String) row[11].toString() : "";
                        int groupnature = (int) ((row[12] != null) ? (Integer) row[12] : "");
                        Date acccreationdate = (Date) ((row[13] != null) ? (Date) row[13] : "");
//                    Account account = (Account) kwlCommonTablesDAOObj.getClassObject(Account.class.getName(), accountid);

                        if (excludeaccountid != null && vendorid.equals(excludeaccountid)) {
                            continue;
                        }

                        JSONObject obj = new JSONObject();
                        obj.put("accid", vendorid);
                        obj.put("accname", vendorname);
                        obj.put("mappedaccountid", accountid);
                        obj.put("groupid", groupname);
                        obj.put("mastergroupid", groupid);
                        obj.put("groupname", groupname);
                        obj.put("acccode", vendorcode);
                        obj.put("accountpersontype", 2);
//                    obj.put("accnamecode", acccode);
                        obj.put("accnamecode", (!StringUtil.isNullOrEmpty(groupname) ? "[" + groupname + "] " + groupname : groupname));
                        obj.put("nature", groupnature);
                        obj.put("naturename", (groupnature == Constants.Liability) ? "Liability" : (groupnature == Constants.Asset) ? "Asset" : (groupnature == Constants.Expences) ? "Expences" : (groupnature == Constants.Income) ? "Income" : "");
                        obj.put("currencyid", vendorcurrencyid);
                        obj.put("currencysymbol", vendorcurrencysymbol);
                        obj.put("creationDate", authHandler.getUserDateFormatterWithoutTimeZone(request).format(acccreationdate));
                        obj.put("currencyname", vendorcurrencyname);
                        obj.put("currencycode", vendorcurrencycode);
                        obj.put("deleted", deleted);
                        obj.put("aliascode", acccode);

                        jArr.put(obj);
                    }
                }
            }
            JSONArray pagedJson = jArr;
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }
            jobj.put("data", pagedJson);
            jobj.put("totalCount", jArr.length());
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
        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(new Date());
        long diff = cal2.getTimeInMillis() - cal1.getTimeInMillis();
        System.out.println("Method End Seconds = " + diff / 1000 % 60 + " | Milli Seconds = " + diff);
        System.out.println("***************************************************");
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
      
//    public ModelAndView getAccountsForComboOptimized(HttpServletRequest request, HttpServletResponse response) {
    public ModelAndView getAccountsIdNameForCombo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = true;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = accAccountHandler.getRequestMap(request);
            String companyId = (String) requestParams.get(Constants.companyKey);
            requestParams.put("templateid", request.getParameter("templateid"));//  Custom Layout - filter accounts if already mapped in the selected template
            requestParams.put("ignorePaging", true);// ERP-13570
            requestParams.put("companyid", companyId);
            requestParams.put("userid", sessionHandlerImpl.getUserid(request));
            ExtraCompanyPreferences extraPref = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            extraPref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
        
           /**
             * This Function will use when Users Visibility Feature is Enable
             * Append user condition while querying data
             */
            String usercondition="";
            if (extraPref != null && extraPref.isUsersVisibilityFlow()) {
                KwlReturnObject object = accountingHandlerDAOobj.getObject(User.class.getName(), requestParams.get("userid").toString());
                User user = object.getEntityList().size() > 0 ? (User) object.getEntityList().get(0) : null;
                if (!AccountingManager.isCompanyAdmin(user)) {
                    /**
                     * if Users visibility enable and current user is not admin
                     */
                    Map<String, Object> reqMap = new HashMap();
                    requestParams.put("isUserVisibilityFlow", true);
                    reqMap.put("companyid", requestParams.get("companyid").toString());
                    reqMap.put("userid", requestParams.get("userid").toString());
                    reqMap.put("jointable", "acd");
                    reqMap.put("moduleid", Constants.Account_Statement_ModuleId);

                    String custcondition = fieldManagerDAOobj.appendUsersCondition(reqMap);
                    if (!StringUtil.isNullOrEmpty(custcondition)) {
                        /**
                         * If mapping found with dimension
                         */
                        usercondition = " and (" + custcondition + ")";
                        requestParams.put("appendusercondtion", usercondition);
                    } else {
                        /**
                         * If no Mapping found for current user then return
                         * function call
                         */

                        JSONArray pagedJson1 = jArr;
                        jobj.put("data", pagedJson1);
                        jobj.put("totalCount", jArr);

                        jobj.put("success", issuccess);
                        jobj.put("msg", msg);
                        return new ModelAndView("jsonView", "model", jobj.toString());
                    }
                }
            }
            
            if (extraPref != null) {
                requestParams.put("accountsortingflag", extraPref.isAccountSortingFlag());
            }
            KwlReturnObject result = accAccountDAOobj.getAccountsForCombo(requestParams);
            List ls = result.getEntityList();
            Iterator<Object[]> itr = ls.iterator();
            while (itr.hasNext()) {
                Object[] row = (Object[]) itr.next();
                    
                JSONObject obj = new JSONObject();
                int groupnature = (int) ((row[20] != null) ? (Integer) row[20] : "");
                obj.put("accid", (String) row[0].toString());
                obj.put("accname", row[1] != null && !StringUtil.isNullOrEmpty(row[1].toString()) ? (String) row[1].toString() : (row[2] != null ? (String) row[2].toString() : ""));
                obj.put("acccode", row[2] != null ? (String) row[2].toString() : "");
                obj.put("accdesc", row[3] != null ? (String) row[3].toString() : "");
                obj.put("acctaxcode", row[4] != null ? (accAccountDAOobj.isTaxActivated(companyId, (String) row[4]) ? (String) row[4] : "") : "");
                obj.put("groupid", row[5] != null ? (String) row[5].toString() : "");
                obj.put("groupname", row[6] != null ? (String) row[6].toString() : "");
                obj.put("nature", groupnature);//account group nature
                obj.put("hasAccess", row[7]!=null ? (row[7].toString()).equalsIgnoreCase("T") : true);
                if (row[8]!=null && !StringUtil.isNullOrEmpty(row[8].toString())) {
                    obj.put("haveToPostJe", row[9] != null ? !("T".equals(row[9].toString())) : false);
                    obj.put("usedIn", row[8]!=null ? (String) row[8].toString():"");
                }

                jArr.put(obj);                    
            }
            
            
            jobj.put("data", jArr);
            jobj.put("totalCount", jArr.length());
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
      
    public ModelAndView getAccountsForJE(HttpServletRequest request, HttpServletResponse response) {
        Calendar c1 = Calendar.getInstance();
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = true;
        String msg = "";
        try {
            int totalCount = 0;
            boolean levelFlag = request.getParameter("levelFlag") != null ? Boolean.parseBoolean(request.getParameter("levelFlag").toString()) : false;
            boolean childAccountsFlag = request.getParameter("childAccountsFlag") != null ? Boolean.parseBoolean((String) request.getParameter("childAccountsFlag")) : false;
            boolean isForPaymentReceipt = request.getParameter("isForPaymentReceipt") != null ? Boolean.parseBoolean(request.getParameter("isForPaymentReceipt")) : false;
            boolean isForJE = request.getParameter("isForJE") != null ? Boolean.parseBoolean(request.getParameter("isForJE")) : false;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> requestParams = accAccountHandler.getRequestMap(request);
            requestParams.put("templateid", request.getParameter("templateid"));//  Custom Layout - filter accounts if already mapped in the selected template
            requestParams.put("companyid", companyid);
            requestParams.put("userid", sessionHandlerImpl.getUserid(request));
            requestParams.put("start", request.getParameter(Constants.start));
            requestParams.put("limit", request.getParameter(Constants.limit));
            requestParams.put("recordids", request.getParameter("recordids"));
            if (!StringUtil.isNullOrEmpty(request.getParameter("defaultaccountid"))) {
                /**
                 * Return account based on default account id.
                 */
                requestParams.put("defaultaccountid", request.getParameter("defaultaccountid"));
            }
            requestParams.put("query", request.getParameter("query"));
            String usercondition = "";
            String excludeaccountid = (String) requestParams.get("accountid");
            String includeaccountid = (String) requestParams.get("includeaccountid");
            String includeparentid = (String) requestParams.get("includeparentid");
            /**
             * This Function will use when Users Visibility Feature is Enable
             * Append user condition while querying data
             */
            ExtraCompanyPreferences extraPref = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), sessionHandlerImpl.getCompanyid(request));
            extraPref = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
            String countryid = extraPref != null ? extraPref.getCompany().getCountry().getID() : null;
            /*
             * get Accounts custom data for Make Payment/Receive Payment only.
             * ERP-32814
             */
            Map<String, Object> variableMap = new HashMap<>();
            HashMap<String, String> replaceFieldMap = new HashMap<>();
            HashMap<String, String> customFieldMap = new HashMap<>();
            HashMap<String, String> customDateFieldMap = new HashMap<>();
            HashMap<String, Integer> FieldMap = null;
            if (isForPaymentReceipt) {
                HashMap<String, Object> fieldrequestParams = new HashMap<>();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Account_Statement_ModuleId, 0));
                FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            }
            if (extraPref != null && extraPref.isUsersVisibilityFlow()) {
                KwlReturnObject object = accountingHandlerDAOobj.getObject(User.class.getName(), requestParams.get("userid").toString());
                User user = object.getEntityList().size() > 0 ? (User) object.getEntityList().get(0) : null;
                if (!AccountingManager.isCompanyAdmin(user)) {
                    /**
                     * if Users visibility enable and current user is not Admin
                     */
                    Map<String, Object> reqMap = new HashMap();
                    requestParams.put("isUserVisibilityFlow", true);
                    reqMap.put("companyid", requestParams.get("companyid").toString());
                    reqMap.put("userid", requestParams.get("userid").toString());
                    reqMap.put("jointable", "acd");
                    reqMap.put("moduleid", Constants.Account_Statement_ModuleId);

                    String custcondition = fieldManagerDAOobj.appendUsersCondition(reqMap);
                    if (!StringUtil.isNullOrEmpty(custcondition)) {
                        /**
                         * If mapping found with dimension
                         */
                        usercondition = " and (" + custcondition + ")";
                        requestParams.put("appendusercondtion", usercondition);
                    } else {
                        /**
                         * If no Mapping found for current user then return
                         * function call
                         */
                        JSONArray pagedJson1 = jArr;
                        jobj.put("data", pagedJson1);
                        jobj.put("totalCount", jArr);

                        jobj.put("success", issuccess);
                        jobj.put("msg", msg);
                        return new ModelAndView("jsonView", "model", jobj.toString());
                    }
                }
            }
            if (extraPref != null) {
                requestParams.put("accountsortingflag", extraPref.isAccountSortingFlag());
            }
            KwlReturnObject result = accAccountDAOobj.getAccountsForJE(requestParams);
            totalCount = result.getRecordTotalCount();
            List<Object[]> ls = result.getEntityList();
            for(Object[] row : ls) {
                JSONObject obj = new JSONObject();
                String accountid = row[0] != null ? row[0].toString() : "";
                String accountname = row[2] != null ? row[2].toString() : "";
                int masterTypeValue = (row[4] != null && !row[4].toString().equals("")) ? Integer.parseInt(row[4].toString()) : 0;
                
                obj.put("accid", accountid);
                obj.put("acccode", row[1] != null ? row[1].toString() : "");
                obj.put("accname", accountname);
                obj.put("mappedaccountid", row[3] != null ? row[3].toString() : "");
                obj.put("masterTypeValue", masterTypeValue);
                obj.put("hasAccess", (row[5] != null && !row[5].toString().equals("")) ? row[5].toString().equalsIgnoreCase("T") ? true : false : false);
                if (row[6] != null && !StringUtil.isNullOrEmpty(row[6].toString())) {
                    obj.put("usedIn", row[6] != null ? row[6].toString() : "");
                    obj.put("haveToPostJe", (row[7] != null && !row[7].toString().equals("")) ? row[7].toString().equalsIgnoreCase("T") ? false : true : true);
                }
                obj.put("groupname", row[8] != null ? row[8].toString() : "");
                obj.put("currencyid", row[9] != null ? row[9].toString() : "");
                obj.put("currencysymbol", row[10] != null ? row[10].toString() : "");
                obj.put("accountpersontype", row[11] != null ? row[11].toString() : "");
                
                if (excludeaccountid != null && accountid.equals(excludeaccountid)) {
                    continue;
                }
                if (includeparentid != null && !accountid.equals(includeparentid)) {
                    continue;
                } else if ((includeaccountid != null && !accountid.equals(includeaccountid))) {
                    continue;
                }
                if (extraPref != null ? (!extraPref.isExciseApplicable() && accountname.equals("PLA (Personal Ledger Account) ")) : false) {
                    continue;
                }
                if (!StringUtil.isNullOrEmpty(countryid) ? (countryid.equalsIgnoreCase("105") && (accountname.equals("GST(Output)") || accountname.equals("GST(Input)"))) : false) {
                    continue;
                }
                
                if (levelFlag || childAccountsFlag) {
                    KwlReturnObject retObj = accountingHandlerDAOobj.getObject(Account.class.getName(), row[0].toString());
                    Account account = (Account) retObj.getEntityList().get(0);

                    if (levelFlag) {
                        int level = 0;
                        level = getAccountLevel(account, level);
                        obj.put("level", level);
                    }

                    if (childAccountsFlag) {
                        if (account.getChildren() != null) {
                            List<Account> childList = new ArrayList(account.getChildren());
                            JSONArray childArr = new JSONArray();
                            for (Account child : childList) {
                                childArr.put(child.getID());
                            }
                            obj.put("childArr", childArr);
                        }
                    }
                }
                
                if (masterTypeValue == 4 && (isForPaymentReceipt || isForJE)) {     // GST Account
                    int taxResultCount = 0;
                    KwlReturnObject taxResult;
                    if (extraPref != null && extraPref.isIsNewGST()) {
                        taxResult = accAccountDAOobj.getTaxesAndTermsUsingAccountId(row[0].toString(), companyid);
                    } else {
                        /*
                        * The params send through hashmap taxFromAccountParams to add new params and make function reusable.
                        */
                        HashMap<String, Object> taxFromAccountParams = new HashMap();
                        taxFromAccountParams.put("accountid",row[0].toString());
                        taxFromAccountParams.put("companyid",companyid);                        
                        taxResult = accAccountDAOobj.getTaxesFromAccountId(taxFromAccountParams);
                    }
                    taxResultCount = taxResult.getEntityList().size();
                    if (taxResultCount == 0) {
                        continue;
                    }
                    if (taxResultCount == 1) {
                    Object object[] = (Object[]) taxResult.getEntityList().get(0);
                        obj.put("isOneToManyTypeOfTaxAccount", false);
                        obj.put("appliedGst", object[0]!=null?(String) object[0].toString():"");
                    } else {
                        KwlReturnObject taxObjectresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), row[0].toString());
                        List taxObjectResultList = taxObjectresult.getEntityList();
                        if (taxObjectResultList.isEmpty() || taxObjectResultList.get(0) == null) {
                            obj.put("isOneToManyTypeOfTaxAccount", true);
                        } else {
                            Object object[] = (Object[]) taxResult.getEntityList().get(0);
                            obj.put("isOneToManyTypeOfTaxAccount", false);
                            obj.put("appliedGst", object[0]!=null?(String) object[0].toString():"");
                        }
                    }
                }

                if (isForPaymentReceipt) {
                    KwlReturnObject idcustresult = accountingHandlerDAOobj.getObject(AccountCustomData.class.getName(), row[0].toString());
                    if (idcustresult.getEntityList().size() > 0) {
                        AccountCustomData jeCustom = (AccountCustomData) idcustresult.getEntityList().get(0);
                        AccountingManager.setCustomColumnValues(jeCustom, FieldMap, replaceFieldMap, variableMap);
                        JSONObject params = new JSONObject();
                        params.put(Constants.companyKey, companyid);
                        params.put(Constants.isLink, true);
                        params.put(Constants.linkModuleId, requestParams.get(Constants.requestModuleId));
                        fieldDataManagerNew.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                    }
                }
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("totalCount", totalCount);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch (Exception ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        Calendar c2 = Calendar.getInstance();
        System.out.println("total-> "+(c2.getTimeInMillis()-c1.getTimeInMillis()));
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
      
      public int getAccountLevel(Account account, int level) throws ServiceException {
        if(account.getParent() != null) {
            level++;
            level = getAccountLevel(account.getParent(), level);
        }
        return level;
    }

    /**
     * Added @Transactional instead of txnmanager - ERP-32983. Moved code
     * containing business logic to the accAccountControllerCMNServiceImpl No
     * any changes other than this has been done in code.
     * @param request
     * @param response
     * @return
     */
    public ModelAndView deleteAccount(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        try {
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            JSONObject tempJobj = accAccountControllerCMNServiceObj.deleteAccount(requestJobj);
            issuccess = tempJobj.optBoolean(Constants.RES_success, false);
            msg = tempJobj.optString(Constants.RES_msg);
        } catch (AccountingException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
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
    
    public ModelAndView activateDeactivateAccounts(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        KwlReturnObject result = null;
        String accCode = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("data", request.getParameter("data"));
            String coaActivateDeactivate = request.getParameter("coaActivateDeactivateFlag");
            String companyid=sessionHandlerImpl.getCompanyid(request);
            boolean coaActivateDeactivateFlag = StringUtil.isNullOrEmpty(coaActivateDeactivate)?false:Boolean.parseBoolean(coaActivateDeactivate);
            requestParams.put("coaActivateDeactivateFlag", coaActivateDeactivateFlag);
            requestParams.put("companyid", companyid);
            Map<String, String> usedIn = new HashMap<>();
            if (coaActivateDeactivateFlag) {
                /**
                 * Call for deactivate account.
                 */
                usedIn = checkAccountUsedInModule(requestParams);//check for account is used or not
                requestParams.put("usedIn", usedIn);
            }
            result = accAccountDAOobj.activateDeactivateAccounts(requestParams);  
            issuccess = result.isSuccessFlag();        
            msg = result.getMsg();
            txnManager.commit(status);
            
            auditMsg = coaActivateDeactivateFlag ? "Deactivated Account " : "Activated Account ";
            for (int i = 0; i < result.getRecordTotalCount(); i++) {
                Account account = (Account) result.getEntityList().get(i);
                accCode = (!StringUtil.isNullOrEmpty(account.getAcccode())) ? " ( " + account.getAcccode() + " ) " : "";
                auditTrailObj.insertAuditLog(AuditAction.ACCOUNT_ACTIVATE_DEACTIVATE, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + auditMsg + "<b>" + account.getName() + "</b>" + accCode, request, account.getID());
            }
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            issuccess = false;
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public void activateDeactivateAccounts(HashMap request) throws AccountingException, ServiceException {
        KwlReturnObject result = null;
        try{
            result = accAccountDAOobj.activateDeactivateAccounts(request);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("activateDeactivateAccounts : " + ex.getMessage(), ex);
        }
    }
    
    public ModelAndView saveAccountMapPnL(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = true;
        boolean isCommitEx = false;
        String templateid = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            templateid = saveAccountMapPnL(request);
            msg = (templateid.equalsIgnoreCase("Duplicate"))? messageSource.getMessage("acc.field.CustomLayoutTemplatenamealreadyexistsPleaseenteranothername", null, RequestContextUtils.getLocale(request)) : messageSource.getMessage("acc.field.CustomLayoutTemplateupdatedsuccessfully", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                isCommitEx = false;
            }
        } catch (Exception ex) {
            if(!isCommitEx){
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = ""+ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("duplicate", (templateid.equalsIgnoreCase("Duplicate"))?1:0);
                jobj.put("defaultPresent", (templateid.equalsIgnoreCase("Default"))?1:0); //flag to check default present or not
                jobj.put("templateid", templateid);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public String saveAccountMapPnL(HttpServletRequest request) throws ServiceException {

        Templatepnl templatepnl = null;
        String templateid = request.getParameter("templateid");
        try {
            String companyid=sessionHandlerImpl.getCompanyid(request);
            boolean isAdminSubdomain = authHandler.isAdminSubDomain(request);
            String countryid = request.getParameter(Constants.COUNTRY_ID);
            String templatename = request.getParameter("templatename");
            String templatetitle = request.getParameter("templatetitle");
            // Flag to decide whether replace default or check for default. If true then validate otherwise replace.
            boolean isValidateDefault = (!StringUtil.isNullOrEmpty(request.getParameter("validateDefault"))) ? Boolean.valueOf(request.getParameter("validateDefault")) : false ;
            boolean isDefault = (!StringUtil.isNullOrEmpty(request.getParameter("isDefault"))) ? Boolean.valueOf(request.getParameter("isDefault")) : false ;
            String templateheading = !StringUtil.isNullOrEmpty(request.getParameter("templateheading"))?request.getParameter("templateheading"):"" ;
            int templatetype = Integer.parseInt(request.getParameter("templatetype"));

//            String[] incomeaccounts = request.getParameterValues("assetArray");
//            String[] expenseaccounts = request.getParameterValues("liabilityArray");

//            boolean isDelete = Boolean.parseBoolean(request.getParameter("deletemode"));

            if (accAccountDAOobj.checkNameAlreadyExists(templateid, templatename, companyid, countryid, isAdminSubdomain, templatetype)) {
                return "Duplicate";//For Duplicate
            }
            if (isDefault) {
                if (isValidateDefault) {
                    // Checking default present or not
                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("companyid", companyid);
                    requestParams.put("isDefault", true);
                    requestParams.put("templatetype", templatetype);
                    KwlReturnObject result = accAccountDAOobj.getPnLTemplates(requestParams);
                    if(result.getEntityList() !=null && !result.getEntityList().isEmpty()){
                        return "Default";//For Default
                    }
                } else {
                    // reseting old default template
                    accAccountDAOobj.updateDefaultTemplate(companyid, templatetype);
                }
            }
            //Update template information
            if(!StringUtil.isNullOrEmpty(templateid)){
                accAccountDAOobj.deleteAccountMapPnL(templateid, companyid);

                Map<String, Object> PnLTemplate = new HashMap<String, Object>();

                PnLTemplate.put("id", templateid);
                PnLTemplate.put("name", templatename);
                PnLTemplate.put("templatetitle", templatetitle);
                PnLTemplate.put("templateheading", templateheading);
                PnLTemplate.put("companyid", companyid);
                PnLTemplate.put("status", 1);
                PnLTemplate.put("isDefault", isDefault);
                KwlReturnObject result = accAccountDAOobj.updatePnLTemplate(PnLTemplate);

                templatepnl = (Templatepnl) result.getEntityList().get(0);
//                isEdit = true;
                auditMsg = " has updated ";
            } else {
                Map<String, Object> PnLTemplate = new HashMap<String, Object>();

                PnLTemplate.put("name", templatename);
                int templatecode = accAccountDAOobj.getMaxTemplateId(companyid, isAdminSubdomain, countryid);
                PnLTemplate.put("templateid", templatecode);
                PnLTemplate.put("templatetitle", templatetitle);
                PnLTemplate.put("templateheading", templateheading);
                PnLTemplate.put("templatetype", templatetype);
                PnLTemplate.put("companyid", companyid);
                PnLTemplate.put(Constants.COUNTRY_ID, countryid);
                PnLTemplate.put("status", 1);
                PnLTemplate.put("isDefault", isDefault);
                if (!isAdminSubdomain) {
                KwlReturnObject result = accAccountDAOobj.updatePnLTemplate(PnLTemplate);
                templatepnl = (Templatepnl) result.getEntityList().get(0);
                templateid = templatepnl.getID();
                auditMsg = " has added ";
                } else {
                    KwlReturnObject result = accAccountDAOobj.updateDefaultPnLTemplate(PnLTemplate);
                    DefaultTemplatePnL defaultTemplatePnL = (DefaultTemplatePnL) result.getEntityList().get(0);
                    templateid = defaultTemplatePnL.getID();
                }
            }


//            //Update mapping information
//            for(int i=0; i < incomeaccounts.length; i++){
//                String accountid = incomeaccounts[i];
//                HashMap<String, Object> PnLAccMap = new HashMap<String, Object>();
//                PnLAccMap.put("templateid", templatepnl.getTemplateid());
//                PnLAccMap.put("isincome", 0);
//                PnLAccMap.put("accountid", accountid);
//                PnLAccMap.put("companyid", companyid);
//                
//                accAccountDAOobj.saveAccountMapPnL(PnLAccMap);
//                accAccountDAOobj.updateAccountTemplateCode(accountid, companyid);
//            }
//            
//            for(int i=0; i < expenseaccounts.length; i++){
//                String accountid = expenseaccounts[i];
//                HashMap<String, Object> PnLAccMap = new HashMap<String, Object>();
//                PnLAccMap.put("templateid", templatepnl.getTemplateid());
//                PnLAccMap.put("isincome", 1);
//                PnLAccMap.put("accountid", expenseaccounts[i]);
//                PnLAccMap.put("companyid", companyid);
//                
//                accAccountDAOobj.saveAccountMapPnL(PnLAccMap);
//                accAccountDAOobj.updateAccountTemplateCode(accountid, companyid);
//            }

//            HashMap<String, Object> requestParamsForThread = new HashMap<String, Object>();
//            requestParamsForThread.put("templateid", templatepnl.getID());
//            requestParamsForThread.put("companyid", companyid);
////            requestParamsForThread.put("isedit", isEdit);
//            accountMapHandler.add(requestParamsForThread);
//            if(!accountMapHandler.isWorking){
//                Thread t = new Thread(accountMapHandler);
//                t.start();
//            }
            auditTrailObj.insertAuditLog(AuditAction.CUSTOMTEMPLATE_ADDEDIT, "User " + sessionHandlerImpl.getUserFullName(request) + auditMsg+ " custom layout template : "+templatename, request, templateid ); 
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("saveAccountMapPnL : "+ex.getMessage(), ex);
        }
        return templateid;
    }

    public ModelAndView saveLayoutGroup(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = true;
        boolean isCommitEx = false;
        int duplicate = 0;
        int createflag=0;
        String groupname = request.getParameter("groupname");
        String groupid = request.getParameter("groupid");
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
         String auditaction=AuditAction.GROUP_UPDATED;
         String auditmsg=" has updated GROUP " ;
        if (groupid=="") {
          createflag=1;
          auditaction=AuditAction.GROUP_CREATED;
          auditmsg=" has added GROUP " ;
        }
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            duplicate = saveLayoutGroup(request);
            msg = (duplicate == 1) ? messageSource.getMessage("acc.field.GroupnamealreadyexistsPleaseenteranothername", null, RequestContextUtils.getLocale(request)):(createflag==1)?messageSource.getMessage("acc.field.Groupsavedsuccessfully", null, RequestContextUtils.getLocale(request)) :messageSource.getMessage("acc.field.Groupupdatedsuccessfully", null, RequestContextUtils.getLocale(request));
            issuccess = true;
            try {
                txnManager.commit(status);
                    auditTrailObj.insertAuditLog(auditaction, "User " + sessionHandlerImpl.getUserFullName(request) +auditmsg + groupname, request, groupid);  
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
                jobj.put("duplicate", duplicate);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public int saveLayoutGroup(HttpServletRequest request) throws ServiceException {

        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);

            String groupname = request.getParameter("groupname");
            String groupid = request.getParameter("groupid");
            String templateid = request.getParameter("templateid");
            int showtotal = request.getParameter("showtotal")!=null?Boolean.parseBoolean(request.getParameter("showtotal"))?1:0:0;
            int showchild = request.getParameter("showchild")!=null?Boolean.parseBoolean(request.getParameter("showchild"))?1:0:1;
            int showchildacc = request.getParameter("showchildacc")!=null?Boolean.parseBoolean(request.getParameter("showchildacc"))?1:0:1;
            boolean excludeChildBalances= request.getParameter("excludeChildBalances")!=null?Boolean.parseBoolean(request.getParameter("excludeChildBalances")):false;
            boolean isAdminSubdomain = authHandler.isAdminSubDomain(request);
            if(isAdminSubdomain && accAccountDAOobj.checkDefaultLayoutGroupNameAlreadyExists(groupid, groupname, companyid, templateid)) {
                return 1;//For Duplicate
            }else if (accAccountDAOobj.checkLayoutGroupNameAlreadyExists(groupid, groupname, companyid, templateid)) {
                return 1;//For Duplicate
            }

            boolean issub = StringUtil.getBoolean(request.getParameter("subgroup"));
            String parentid=request.getParameter("parentid");
            if(!issub){
                parentid=null;
            }

            int nature = Integer.parseInt(request.getParameter("nature"));
            int sequence = Integer.parseInt(request.getParameter("sequence"));
            int oldSequence = !StringUtil.isNullOrEmpty(request.getParameter("oldSequence")) ?Integer.parseInt(request.getParameter("oldSequence")):-1;
            int numberofrows = Integer.parseInt(request.getParameter("addBlankRowBefore"));
            
            if(oldSequence!=sequence){
            if (oldSequence != -1 && !StringUtil.isNullOrEmpty(request.getParameter("oldSequence"))) {
                if (isAdminSubdomain) {
                    accAccountDAOobj.updateDefaultLayoutGroupExistingSequnceNo(oldSequence+1, companyid, templateid, "substraction");
                } else {
                    accAccountDAOobj.updateExistingSequnceNo(oldSequence+1, companyid, templateid, "substraction",groupid);
                }
            }
            if(isAdminSubdomain){
                accAccountDAOobj.updateDefaultLayoutGroupExistingSequnceNo(sequence, companyid, templateid,"addition");
            }else{
            accAccountDAOobj.updateExistingSequnceNo(sequence, companyid, templateid,"addition",groupid);
            }
            }
            HashMap<String, Object> map = new HashMap<String, Object>();

            if (!StringUtil.isNullOrEmpty(groupid)) {
                map.put("id", groupid);
            }
            
            map.put("name", groupname);
            map.put("nature", nature);
            map.put("sequence", sequence);
            map.put("companyid", companyid);
            map.put("templateid", templateid);
            map.put("parentid", parentid);
            map.put("showtotal", showtotal);
            map.put("showchild", showchild);
            map.put("showchildacc", showchildacc);
            map.put("excludeChildBalances", excludeChildBalances);
            map.put("addBlankRowBefore", numberofrows);

            if(isAdminSubdomain){
                KwlReturnObject result = accAccountDAOobj.saveDefaultLayoutGroup(map);
                
                DefaultLayoutGroup group = (DefaultLayoutGroup) result.getEntityList().get(0);
                
                String[] accountsArray = request.getParameterValues("accountsArray");
                boolean accountsArrayFlag = request.getParameter("accountsArrayFlag")!=null?Boolean.parseBoolean(request.getParameter("accountsArrayFlag")):false;            
                if (nature == 5) {
                    accAccountDAOobj.deleteDefaultLayoutGroupsofTotalGroup(group.getID());
                    if(accountsArrayFlag) {
                        for (int i = 0; i < accountsArray.length; i++) {
                            String[] groupArr = accountsArray[i].split("_");
                            HashMap<String, Object> groupAccMap = new HashMap<String, Object>();
                            groupAccMap.put("groupidtotal", group.getID());
                            groupAccMap.put("groupid", groupArr[0]);
                            groupAccMap.put("action", groupArr[1]);

                            accAccountDAOobj.saveDefaultLayoutGroupMapForGroupTotal(groupAccMap);
                        }
                    }
                } else {
                    accAccountDAOobj.deleteDefaultLayoutGroupAccount(group.getID());
                    if(accountsArrayFlag) {
                        for (int i = 0; i < accountsArray.length; i++) {
                            HashMap<String, Object> groupAccMap = new HashMap<String, Object>();
                            groupAccMap.put("groupid", group.getID());
                            groupAccMap.put("accountid", accountsArray[i]);//accountname, groupname
//                            groupAccMap.put("companyid", companyid);

                            accAccountDAOobj.saveDefaultLayoutGroupAccountMap(groupAccMap);
                        }
                    }
                }
            }else{
            KwlReturnObject result = accAccountDAOobj.saveLayoutGroup(map);


            LayoutGroup group = (LayoutGroup) result.getEntityList().get(0);

            String[] accountsArray = request.getParameterValues("accountsArray");
            boolean accountsArrayFlag = request.getParameter("accountsArrayFlag")!=null?Boolean.parseBoolean(request.getParameter("accountsArrayFlag")):false;            
            if (nature == 5) {
                accAccountDAOobj.deleteLayoutGroupsofTotalGroup(group.getID(), companyid);
                if(accountsArrayFlag) {
                    for (int i = 0; i < accountsArray.length; i++) {
                        String[] groupArr = accountsArray[i].split("_");
                        HashMap<String, Object> groupAccMap = new HashMap<String, Object>();
                        groupAccMap.put("groupidtotal", group.getID());
                        groupAccMap.put("groupid", groupArr[0]);
                        groupAccMap.put("action", groupArr[1]);

                        accAccountDAOobj.saveLayoutGroupMapForGroupTotal(groupAccMap);
                    }
                }
            } else {
                accAccountDAOobj.deleteLayoutGroupAccount(group.getID(), companyid);
                if(accountsArrayFlag) {
                    for (int i = 0; i < accountsArray.length; i++) {
                        HashMap<String, Object> groupAccMap = new HashMap<String, Object>();
                        groupAccMap.put("groupid", group.getID());
                        groupAccMap.put("accountid", accountsArray[i]);
                        groupAccMap.put("companyid", companyid);

                        accAccountDAOobj.saveLayoutGroupAccountMap(groupAccMap);
                    }
                }
            }
            }
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("saveLayoutGroup : " + ex.getMessage(), ex);
        }
        return 0;
    }

    public ModelAndView syncDefaultCustomLayout(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = true;
//        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
//        def.setName("Account_Tx");
//        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        
        try {
            String subdomainlist = !StringUtil.isNullOrEmpty(request.getParameter("subdomainlist")) ? request.getParameter("subdomainlist") : "";
            String countryid = !StringUtil.isNullOrEmpty(request.getParameter("countryid")) ? request.getParameter("countryid") : "";
            String synctemplateid = !StringUtil.isNullOrEmpty(request.getParameter("synctemplateid")) ? request.getParameter("synctemplateid") : "";
            boolean isAdminSubdomain = authHandler.isAdminSubDomain(request);
            
            String[] subdomainArr = subdomainlist.split(",");
            for(int i=0 ; i<subdomainArr.length ; i++){
                /*TODO - For Sync : Delete existing records and then add new
                */
                
                Map<String, Object> reqMap = new HashMap<>();
                reqMap.put("countryid", countryid);
                reqMap.put(Constants.companyKey, subdomainArr[i]);
                reqMap.put("isAdminSubdomain", isAdminSubdomain);
                reqMap.put("synctemplateid", synctemplateid);
                
                companySetupThread.add(reqMap);
                Thread t = new Thread(companySetupThread);
                t.start();
            }
            
            msg = "Sync Layout completed successfully.";
            issuccess = true;
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    /**
     * Added @Transactional instead of txnmanager - ERP-32983. Moved code
     * containing business logic to the AccAccountServiceImpl No any changes
     * other than this has been done in code.
     * @param request
     * @param response
     * @return
     */
    public ModelAndView deleteCustomTemplate(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            boolean isAdminSubdomain = authHandler.isAdminSubDomain(request);
            JSONObject requestJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = accAccountService.deleteCustomTemplate(requestJobj, isAdminSubdomain);
            issuccess = jobj.optBoolean(Constants.RES_success, false);
            msg = jobj.optString(Constants.RES_msg);
        } catch (SessionExpiredException ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try{
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch(JSONException ex){
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * @Author Neeraj
     * @param accountID
     * @return ReturnAccountChilds "The list incuding the parent and their children and grandchildren and so on"
     * @throws ServiceException
     */
    public List<String> scanChildAccounts(String accountID) throws ServiceException{
    	try{
            List<String> ReturnAccountChilds = new ArrayList<String>();
            ReturnAccountChilds.add(accountID);
            boolean flag = true;
            List<String> AccountChilds = new ArrayList<String>();
            AccountChilds.add(accountID);
    		while(flag == true){
    			String str = (String)AccountChilds.get(0);
                List<?> Result = accAccountDAOobj.isChildforDelete(str);
    			if(Result != null){	
                    Iterator<?> resultIterator = Result.iterator();
    				while(resultIterator.hasNext()){
                        Object ResultObj = resultIterator.next();
                        Account account = (Account) ResultObj;
                        String Child = account.getID();
                        AccountChilds.add(Child);
                        ReturnAccountChilds.add(Child);
                    }
                }
                AccountChilds.remove(0);
    			if(AccountChilds.isEmpty() == true){
                    flag = false;
                }
            }
            return ReturnAccountChilds;
        }
    	catch(Exception ex){
    		throw ServiceException.FAILURE("scanChildAccounts : "+ex.getMessage(), ex);
    }
    }

    /**
     * @Author Neeraj
     * @param request	" The Account ID for which total balance sheet value is to be found out"
     * @param response	
     * @return netAssetValue   "Net Balance Sheet value of a Fixed Asset or  any other Account"
     */
    public ModelAndView getNetAssetValue(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        String msg=""; 
        double netAssetValue = 0,JEamount = 0; 
        Double JEamount1;
        boolean issuccess = false;
        String companyid = "";
        KwlReturnObject kwlReturnObject = null, kwlBaseCurrencyrate;
//        DecimalFormat decimalFormat = new DecimalFormat("0.00");

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
		try{
            companyid = sessionHandlerImpl.getCompanyid(request);;
            String FixedAssetId = request.getParameter("fixedAssetID");
            String currencyID;
            Date dt = new Date();
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", companyid);
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));




            kwlReturnObject = accJournalEntryobj.getAccountBalance(FixedAssetId, null, null);

            Iterator<?> netValue =  kwlReturnObject.getEntityList().iterator();

            while(netValue.hasNext()){
                Object[] row = (Object[]) netValue.next();
                JournalEntryDetail journalEntryDetail = (JournalEntryDetail) row[1];
                currencyID = (String)row[2];

                boolean currencyEqual = currencyID.equalsIgnoreCase(sessionHandlerImpl.getCurrencyID(request));
                JEamount = journalEntryDetail.getAmount();
                JEamount1 = JEamount;
                if(!currencyEqual){
                    kwlBaseCurrencyrate = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, JEamount1, currencyID, dt, 0);
                	JEamount = (Double)kwlBaseCurrencyrate.getEntityList().get(0);

                }


                if(journalEntryDetail.isDebit()){
                    netAssetValue = netAssetValue + JEamount;
                }else{
                    netAssetValue = netAssetValue - JEamount;
                }
            }

            issuccess = true;
            msg = messageSource.getMessage("acc.field.NetFixedAssetValuehasbeenfetchedsuccessfully", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("netAssetValue", Double.parseDouble(authHandler.formattedAmount(netAssetValue, companyid)));
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    /**
     * @Author Neeraj
     * @param request
     * @param response 
     */
    public ModelAndView removeAsset(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
		try{
            String FixedAssetId = request.getParameter("fixedAssetID");
			String companyid=sessionHandlerImpl.getCompanyid(request);

            accAccountDAOobj.deleteAccount(FixedAssetId, true);

            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("id", request.getParameter("fixedAssetID"));
            requestParams.put("companyid", companyid);
            if(Boolean.parseBoolean(request.getParameter("isWriteOff"))){
                requestParams.put("isSale", false);
                requestParams.put("isWriteOff", true);
            }else{
                requestParams.put("isSale", true);
                requestParams.put("isWriteOff", false);
            }
            requestParams.put("deleteJe", request.getParameter("deleteJe"));

            accDepreciationDAOobj.addAssetDetail(requestParams);

            issuccess = true;
            msg = messageSource.getMessage("acc.main.fadel", null, RequestContextUtils.getLocale(request));   //"Fixed Asset has been removed successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
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

            ArrayList filter_names = new ArrayList(),filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("INcompany.companyID"); //add custom field enhancement
            filter_params.add(sessionHandlerImpl.getCompanyid(request));
            filter_names.add("ISdeleted");
            filter_params.add(false);
            if(request.getParameter("group")!=null){
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
            jobj= getAccountsByCategoryJson(request, result.getEntityList());

            jobj.put("count", result.getRecordTotalCount());
        } catch (Exception ex) {
            issuccess = false;
            msg = ""+ex.getMessage();
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

    public JSONObject getAccountsByCategoryJson(HttpServletRequest request, List list) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr=new JSONArray();
        double netBookValue = 0;
        try{
            Iterator itr = list.iterator();
            while (itr.hasNext()) {
                Account account = (Account) itr.next();
                double accountOpeningBalance = accInvoiceCommon.getOpeningBalanceOfAccount(request, account,false,null);
                JSONObject obj = new JSONObject();
                obj.put("accid", account.getID());
                obj.put("accname", account.getName());
                obj.put("groupid", account.getGroup().getID());
                obj.put("groupname", account.getGroup().getName());
                obj.put("nature", account.getGroup().getNature());
                obj.put("openbalance", accountOpeningBalance);
//                obj.put("depreciationaccount", account.getDepreciationAccont()==null?"":account.getDepreciationAccont().getID());

                obj.put("currencyid",(account.getCurrency()==null?"": account.getCurrency().getCurrencyID()));
                obj.put("currencysymbol",(account.getCurrency()==null?"": account.getCurrency().getSymbol()));
                obj.put("currencyname",(account.getCurrency()==null?"": account.getCurrency().getName()));
                obj.put("presentbalance", account.getPresentValue());
                obj.put("custminbudget", account.getCustMinBudget());
                obj.put("life", account.getLife());
                obj.put("salvage", account.getSalvage());
                obj.put("deleted", account.isDeleted());
                obj.put("creationDate", authHandler.getUserDateFormatterWithoutTimeZone(request).format(account.getCreationDate()));
                obj.put("category", account.getCategory()==null?"":account.getCategory().getValue());
                obj.put("categoryid", account.getCategory()==null?"":account.getCategory().getID());

                KwlReturnObject accountBalance = accJournalEntryobj.getAccountBalance(account.getID(),null,null);
                List resultList = accountBalance.getEntityList();
                Iterator<Object> iterator = resultList.iterator();
                netBookValue = 0;
                while(iterator.hasNext()){
                    Object[] temp = (Object[]) iterator.next();
                    netBookValue = netBookValue + Double.parseDouble(temp[0].toString());
                }
                obj.put("netBookValue", netBookValue);
                jArr.put(obj);
            }
            jobj.put("data", jArr);
        } catch (JSONException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getAccountsByCategoryJson : "+ex.getMessage(), ex);
        }
        return jobj;
    }

        public ModelAndView editCurrency(HttpServletRequest request, HttpServletResponse response){
    	JSONObject jobj=new JSONObject();
        boolean issuccess = false;
        String msg = "";
        try{
            String accid = request.getParameter("accid");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            jobj.put("currencyEdit", false);

            KwlReturnObject Count = accJournalEntryobj.getJEDfromAccount(accid, companyid);
            int jedCount = Count.getRecordTotalCount();

            if(jedCount <= 0){
                jobj.put("currencyEdit", true);
                msg = messageSource.getMessage("acc.field.Account'sCurrencycanbeedited", null, RequestContextUtils.getLocale(request));
            }else{

                msg = messageSource.getMessage("acc.field.AccountsCurrencycannotbeeditedasitisusedintransactionsalready", null, RequestContextUtils.getLocale(request));
            }
            issuccess = true;
        } catch (Exception ex) {
            msg = ""+ex.getMessage();
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getProductFieldParams(List list, JSONObject jresult , boolean isForProductMasterOnly) throws JSONException {

        /*
         isForProductMasterOnly  = Used for Fields which are present for Product Master module Not for Other Product and Services modules i.e. Related modules
        
         */
        AccProductCustomData accProductCustomData = null;
        Iterator ite = list.iterator();
        while (ite.hasNext()) {
            FieldParams tmpcontyp = (FieldParams) ite.next();
            JSONObject jobj = new JSONObject();
            jobj.put("fieldname", tmpcontyp.getFieldname());
//            try {
//
//              custumObjresult = accountingHandlerDAOobj.getObject(AccProductCustomData.class.getName(), jeId);
//            } catch (Exception e) {
//            }
//
//            accProductCustomData = (AccProductCustomData) custumObjresult.getEntityList().get(0);
//            if (accProductCustomData != null) {
//                String coldata = accProductCustomData.getCol(tmpcontyp.getColnum());
//                if (!StringUtil.isNullOrEmpty(coldata)) {
//                    jobj.put("fieldData", coldata);
//                }
//
//            }


            jobj.put("fieldlabel", tmpcontyp.getFieldlabel());
            jobj.put("isessential", tmpcontyp.getIsessential());
            jobj.put("maxlength", tmpcontyp.getMaxlength());
            jobj.put("validationtype", tmpcontyp.getValidationtype());
            jobj.put("fieldid", tmpcontyp.getId());
            jobj.put("moduleid", tmpcontyp.getModuleid());
            jobj.put("modulename", isForProductMasterOnly?getModuleName(Constants.Only_ProductMaster_ModuleId):getModuleName(tmpcontyp.getModuleid()));       // 1000 = Default i.e. Product Master
            jobj.put("fieldtype", tmpcontyp.getFieldtype());
            jobj.put("iseditable", tmpcontyp.getIseditable());
            jobj.put("comboid", tmpcontyp.getComboid());
            jobj.put("comboname", tmpcontyp.getComboname());
            jobj.put("moduleflag", tmpcontyp.getModuleflag());
            jobj.put("relatedmoduleid", tmpcontyp.getRelatedmoduleid());
            jobj.put("refcolumn_number", Constants.Custom_Column_Prefix + tmpcontyp.getRefcolnum());
            jobj.put("column_number", Constants.Custom_Column_Prefix + tmpcontyp.getColnum());
            jobj.put("sendnotification", tmpcontyp.getsendNotification());
            jobj.put("notificationdays", tmpcontyp.getnotificationDays());
            jobj.put("iscustomfield", tmpcontyp.getCustomfield() == 1 ? true : false);
            jobj.put("iscustomcolumn", tmpcontyp.getCustomcolumn() == 1 ? true : false);
            jobj.put("sequence", tmpcontyp.getSequence());
            jobj.put("isForProductMasterOnly", isForProductMasterOnly);
            jobj.put("relatedModuleIsAllowEditid", tmpcontyp.getRelatedModuleIsAllowEdit());
            jresult.append("data", jobj);
        }



        return jresult;
    }
    public ModelAndView getFieldParamsForCombo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject obj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        KwlReturnObject result = null;
        JSONObject jresult = new JSONObject();
        String name = request.getParameter("name");
        int lineitem = StringUtil.isNullOrEmpty(request.getParameter(Constants.customcolumn)) ? 0 : Integer.parseInt(request.getParameter(Constants.customcolumn));
        String[] moduleidarray = request.getParameterValues(Constants.moduleidarray);
        String commaSepratedModuleids = "";
        if (moduleidarray != null) {
            for (int i = 0; i < moduleidarray.length; i++) {
                if (!StringUtil.isNullOrEmpty(moduleidarray[i])) {
                    commaSepratedModuleids += moduleidarray[i] + ",";
                }
            }
            if (commaSepratedModuleids.length() > 1) {
                commaSepratedModuleids = commaSepratedModuleids.substring(0, commaSepratedModuleids.length() - 1);
            }
        }
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.INmoduleid, Constants.customcolumn));
            requestParams.put(Constants.filter_values, Arrays.asList(companyid, commaSepratedModuleids, lineitem));
            requestParams.put("customfield", 0);
            if (!StringUtil.isNullOrEmpty(request.getParameter("fieldtype"))) {
                /**
                 * Return fields for particular Field type i.e.Drop down= 4.
                 */
                int fieldtype=Integer.parseInt(request.getParameter("fieldtype"));
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.INmoduleid, Constants.customcolumn, "fieldtype"));
                requestParams.put(Constants.filter_values, Arrays.asList(companyid, commaSepratedModuleids, lineitem, fieldtype));
                requestParams.put("customfield", 1);
            }


            requestParams.put("isGroupby", true);
            if(!StringUtil.isNullOrEmpty(name)){
                requestParams.put("name", name);
            }
                
            result = accAccountDAOobj.getFieldParamsForCombo(requestParams);
            List lst = result.getEntityList();

            JSONArray DataJArr = getFieldParamsForCombo(request, lst,requestParams);
            obj.put("data", DataJArr);
//            jobj.put("count", DataJArr.length());
            obj.put("totalCount", result.getRecordTotalCount());
            issuccess = true;
            System.out.println("End Time" + new Date());
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                obj.put("success", issuccess);
                obj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", obj.toString());
    }

    public JSONObject getFieldParamsCommaSepValues(List list,JSONObject obj) throws JSONException, ServiceException, SessionExpiredException {
        Iterator itr = list.iterator();
        JSONArray jArr = new JSONArray();
        String moduleidstr="";
        String parentidstr="";
        while (itr.hasNext()) {
            Object[] row = (Object[]) itr.next();
            String id = row[2].toString();
            String moduleid = row[3].toString();
            if (StringUtil.isNullOrEmpty(moduleidstr)) {
                moduleidstr =  moduleid;
            } else {
                moduleidstr +=","+ moduleid;
            }
            if (StringUtil.isNullOrEmpty(parentidstr)) {
                parentidstr = id;
            } else {
                 parentidstr +=","+ id;
            }
        }
         obj.put("moduleidstr", moduleidstr);
            obj.put("parentidstr", parentidstr);
        return obj;
    }
    public String getChildString(HashMap<String, Object> requestParams) throws JSONException, ServiceException, SessionExpiredException {
        KwlReturnObject result = accAccountDAOobj.getFieldParams(requestParams);
        List list = result.getEntityList();
        Iterator itr = list.iterator();
        String childId = "";
        while (itr.hasNext()) {
            FieldParams tmpcontyp = (FieldParams) itr.next();
            String id = Constants.Custom_Record_Prefix + tmpcontyp.getFieldlabel();
            if (StringUtil.isNullOrEmpty(childId)) {
                childId = id;
            } else {
                childId += "," + id;
            }
        }
        return childId;
    }
    /**
     * @Info Get all the leaves (Hierarchical childs ) of parent
     * @param requestParams
     * @param childIdList
     * @return
     * @throws JSONException
     * @throws ServiceException
     * @throws SessionExpiredException 
     */
    public List<String> getChildIds(HashMap<String, Object> requestParams, List childIdList) throws JSONException, ServiceException, SessionExpiredException {
        KwlReturnObject result = accAccountDAOobj.getFieldParams(requestParams);
        List list = result.getEntityList();
        Iterator itr = list.iterator();
        while (itr.hasNext()) {
            FieldParams field = (FieldParams) itr.next();
            String id = field.getId();
            requestParams.put("parentid", id);
            childIdList.add(id);
            getChildIds(requestParams, childIdList);
        }
        return childIdList;
    }
    public JSONArray getFieldParamsForCombo(HttpServletRequest request, List list, HashMap<String, Object> requestParams) throws JSONException, ServiceException, SessionExpiredException {
        Iterator itr = list.iterator();
        JSONArray jArr = new JSONArray();
        while (itr.hasNext()) {
            Object[] row = (Object[]) itr.next();
            String id = row[0].toString();
            String fieldname = row[1].toString();
            boolean isEssential = StringUtil.isNullOrEmpty(row[4].toString())?false:(row[4].toString().equals("0")?false:true);
            JSONObject obj = new JSONObject();
            obj.put("fieldlabel", fieldname);
            obj.put("fieldid", id);
            obj.put("isessential", isEssential);
            requestParams.put("fieldlabel", fieldname);
            requestParams.put("isGroupby", false);
            KwlReturnObject result = accAccountDAOobj.getFieldParamsForCombo(requestParams);
            List lst = result.getEntityList();
            obj= getFieldParamsCommaSepValues(lst,obj);
            jArr.put(obj);
        }
        return jArr;
    }
    public ModelAndView getFieldParams(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        KwlReturnObject result = null;
        JSONObject jresult = new JSONObject();
        boolean isforgstrulemapping = !StringUtil.isNullOrEmpty(request.getParameter("isforgstrulemapping")) ? Boolean.parseBoolean(request.getParameter("isforgstrulemapping")) : false;
        int lineitem = StringUtil.isNullOrEmpty(request.getParameter(Constants.customcolumn)) ? 0 : Integer.parseInt(request.getParameter(Constants.customcolumn)) ;
        boolean globallevelfields = !StringUtil.isNullOrEmpty(request.getParameter("globallevelfields")) ? Boolean.parseBoolean(request.getParameter("globallevelfields")) : false;
        boolean linelevelfields = !StringUtil.isNullOrEmpty(request.getParameter("linelevelfields")) ? Boolean.parseBoolean(request.getParameter("linelevelfields")) : false;
        String module = request.getParameter(Constants.moduleid);
        String isAvoidRedundent = request.getParameter("isAvoidRedundent");
        boolean AvoidRedundent=StringUtil.isNullOrEmpty(isAvoidRedundent)?false:Boolean.parseBoolean(isAvoidRedundent);
        String ReturnAllFields = request.getParameter("isReturnAllFields");
        boolean isReturnAllFields=StringUtil.isNullOrEmpty(ReturnAllFields)?false:Boolean.parseBoolean(ReturnAllFields);
        boolean ignoreDefaultFields=StringUtil.isNullOrEmpty(request.getParameter("ignoreDefaultFields"))?false:Boolean.parseBoolean(request.getParameter("ignoreDefaultFields"));
        Integer moduleid = -1;
        int reportid = !StringUtil.isNullOrEmpty(request.getParameter("reportid")) ? Integer.parseInt(request.getParameter("reportid")) : -1;
        String jeId = request.getParameter("jeId");
        boolean isOpeningTransaction=Boolean.FALSE.parseBoolean(request.getParameter("isOpeningTransaction"));    
        boolean isautopopulatedata=Boolean.FALSE.parseBoolean(request.getParameter("isautopopulatedata"));
        int fetchdataid=StringUtil.isNullOrEmpty(request.getParameter("fetchdataid"))?0:Integer.parseInt(request.getParameter("fetchdataid"));
        String[] moduleidarray = request.getParameterValues(Constants.moduleidarray);
        boolean isForProductCustomFieldHistoryCombo = false;
        boolean isAdvanceSearch=false; //used to find wheather request came fom advance search or not.
        isAdvanceSearch = request.getParameter("isAdvanceSearch")!=null?Boolean.parseBoolean(request.getParameter("isAdvanceSearch")):false;
        boolean customerCustomFieldFlag = request.getParameter("customerCustomFieldFlag")!=null?Boolean.parseBoolean(request.getParameter("customerCustomFieldFlag")):false;
        boolean vendorCustomFieldFlag = request.getParameter("vendorCustomFieldFlag")!=null?Boolean.parseBoolean(request.getParameter("vendorCustomFieldFlag")):false;
        boolean isMultiEntity = (!StringUtil.isNullOrEmpty(request.getParameter(Constants.isMultiEntity))) ? Boolean.parseBoolean(request.getParameter(Constants.isMultiEntity)) :false;
        boolean isAddressFieldSearch = (!StringUtil.isNullOrEmpty(request.getParameter("isAddressFieldSearch"))) ? Boolean.parseBoolean(request.getParameter("isAddressFieldSearch")) :false;
        String groupname = request.getParameter("groupname");
        String dimvalue=request.getParameter("dimvalue");
        /*
          Here retriving custom data for multientity fields
        */
        String MEDCustomDataId="";
        if (isforgstrulemapping) {
            String companyid = "";
            try {
                if (!StringUtil.isNullOrEmpty(groupname) && !StringUtil.isNullOrEmpty(dimvalue)) {
                    companyid = sessionHandlerImpl.getCompanyid(request);
                    MEDCustomDataId = fieldDataManagerNew.getValuesForLinkRecords(Integer.parseInt(module), companyid, "Custom_" + groupname.replaceAll("\\*", ""), dimvalue, 0);
                    if (MEDCustomDataId != "") {
                        jeId = MEDCustomDataId;
                    }
                }
            } catch (SessionExpiredException ex) {
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (!StringUtil.isNullOrEmpty(request.getParameter("isForProductCustomFieldHistoryCombo"))) {
            isForProductCustomFieldHistoryCombo = Boolean.parseBoolean(request.getParameter("isForProductCustomFieldHistoryCombo"));
        }
        String commaSepratedModuleids = "";
        if (moduleidarray != null) {
            for (int i = 0; i < moduleidarray.length; i++) {
                if (!StringUtil.isNullOrEmpty(moduleidarray[i])) {
                    commaSepratedModuleids += moduleidarray[i] + ",";
                }
            }
            if (commaSepratedModuleids.trim().endsWith(",")) {
                commaSepratedModuleids = commaSepratedModuleids.substring(0, commaSepratedModuleids.length() - 1);
            }
        }
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        try {
            Date currentDate = authHandler.getDateOnlyFormat(request).parse(authHandler.getDateOnlyFormat(request).format(new Date()));
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateFormatter(request);
            Integer colcount = 1;
            boolean isCustomDetailReport = false;
            boolean isLinedetailReport = Boolean.FALSE.parseBoolean(request.getParameter("isLinedetailReport"));
            HashMap<String, Object> paramsMap = new HashMap<>();
            paramsMap.put("id", companyid);
            boolean isNewGST = (Boolean) kwlCommonTablesDAOObj.getRequestedObjectFields(ExtraCompanyPreferences.class, new String[]{"isNewGST"}, paramsMap);
            if (StringUtil.isNullOrEmpty(commaSepratedModuleids) && StringUtil.isNullOrEmpty(module)) {
                requestParams.put(Constants.filter_names, lineitem==1?Arrays.asList(Constants.companyid,Constants.customcolumn):Arrays.asList(Constants.companyid));
                requestParams.put(Constants.filter_values, lineitem==1?Arrays.asList(companyid,lineitem):Arrays.asList(companyid));
            } else if (StringUtil.isNullOrEmpty(commaSepratedModuleids)) {
                moduleid = Integer.parseInt(module);
                if (moduleid > 99 || reportid == Constants.CUSTOMER_REVENUE_REPORT) { // Added module >100 for Report like ledger, Balance sheet etc
                    if (reportid == Constants.SALES_PERSON_COMMISSION_DIMENSION_REPORT) {
                        isCustomDetailReport = Boolean.FALSE.parseBoolean(request.getParameter("isCustomDetailReport"));
                        requestParams.put("isCustomDetailReport", isCustomDetailReport);
                        requestParams.put("reportid", reportid);
                        requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.customcolumn));
                        requestParams.put(Constants.filter_values, Arrays.asList(companyid, lineitem));
                    } else if (moduleid == 101 || moduleid == 100 || moduleid == Constants.Acc_Ledger_ModuleId || reportid == Constants.CUSTOMER_REVENUE_REPORT) {
                        isCustomDetailReport = Boolean.FALSE.parseBoolean(request.getParameter("isCustomDetailReport"));
                        requestParams.put("isCustomDetailReport", isCustomDetailReport);
                        requestParams.put("isLinedetailReport", isLinedetailReport);
                        requestParams.put("reportid", reportid);
                        requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid));
                        requestParams.put(Constants.filter_values, Arrays.asList(companyid));
                    } else if (isMRPModule(moduleid) || moduleid == Constants.Acc_Stock_Request_ModuleId || moduleid == Constants.Acc_InterStore_ModuleId || moduleid == Constants.Acc_InterLocation_ModuleId || moduleid == Constants.Acc_CycleCount_ModuleId) {
                        requestParams.put(Constants.filter_names, lineitem == 1 ? Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn) : Arrays.asList(Constants.companyid, Constants.moduleid));
                        requestParams.put(Constants.filter_values, lineitem == 1 ? Arrays.asList(companyid, moduleid, lineitem) : Arrays.asList(companyid, moduleid));
                    } else {
                        requestParams.put(Constants.filter_names, linelevelfields ? Arrays.asList(Constants.companyid, Constants.moduleid) : Arrays.asList(Constants.companyid, Constants.customcolumn));
                        requestParams.put(Constants.filter_values, linelevelfields ? Arrays.asList(companyid, moduleid) : Arrays.asList(companyid, lineitem));
                    }
                } else {
                    requestParams.put(Constants.filter_names, lineitem == 1 ? Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn) : Arrays.asList(Constants.companyid, Constants.moduleid));
                    requestParams.put(Constants.filter_values, lineitem == 1 ? Arrays.asList(companyid, moduleid, lineitem) : Arrays.asList(companyid, moduleid));
                }
            } else {
                if(commaSepratedModuleids.contains(""+Constants.Acc_FixedAssets_Details_ModuleId) && commaSepratedModuleids.contains(""+Constants.Acc_FixedAssets_AssetsGroups_ModuleId) && linelevelfields){
                    requestParams.put("reportid", reportid);
                    requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.INmoduleid));
                    requestParams.put(Constants.filter_values, Arrays.asList(companyid, commaSepratedModuleids));
                }else{
                    requestParams.put(Constants.filter_names, lineitem==1?Arrays.asList(Constants.companyid, Constants.INmoduleid,Constants.customcolumn):Arrays.asList(Constants.companyid, Constants.INmoduleid));
                    requestParams.put(Constants.filter_values, lineitem==1?Arrays.asList(companyid, commaSepratedModuleids,lineitem):Arrays.asList(companyid, commaSepratedModuleids));
                }
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("iscustomfield"))){
                requestParams.put("customfield",1);
            }
            
            if(!StringUtil.isNullOrEmpty(request.getParameter("iscustomdimension")) && ! request.getParameter("iscustomdimension").equalsIgnoreCase("false")){
                requestParams.put("customdimension",0);
            }
            
            /*
            Used to filter in report -  Budget vs Cost report
            
            iscustomcolumn = TRUE , For line level custom/dimension fields
            iscustomcolumn = FALSE , For Global level custom/dimension fields
            */
            if(!StringUtil.isNullOrEmpty(request.getParameter("iscustomcolumn"))){// 
                if(! request.getParameter("iscustomcolumn").equalsIgnoreCase("false")){
                    requestParams.put("iscustomcolumn",1);
                }else{
                    requestParams.put("iscustomcolumn",0);
                }
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("isActivated"))){                
                requestParams.put("isActivated",Integer.parseInt(request.getParameter("isActivated")));
            }
            boolean splitOpeningBalance=false;
            if (!StringUtil.isNullOrEmpty(request.getParameter("splitOpeningBalance"))) {
                splitOpeningBalance = Boolean.parseBoolean(request.getParameter("splitOpeningBalance"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("excludeModule")) && !splitOpeningBalance){      
                requestParams.put("excludeModule",Integer.parseInt(request.getParameter("excludeModule")));
            }
            if(reportid == Constants.SalesCommissionSchemaReport){
                requestParams.put("isForSalesSchema", true);
            }
            Integer moduleidint=0;
            if(!StringUtil.isNullOrEmpty(module)){
                moduleidint = Integer.parseInt(module);
            }
//            requestParams.put("checkForParent",true);//check if parent-child relation exist.
//            result = accAccountDAOobj.getFieldParams(requestParams);
//            requestParams.remove("checkForParent");
//            List checkForParentList=result.getEntityList();
//            if(checkForParentList !=null && checkForParentList.size() == 0 ){
            requestParams.put("AvoidRedundent", AvoidRedundent);
            requestParams.put(Constants.isMultiEntity, isMultiEntity);
            if (moduleidint == 100 || moduleidint == 101 || moduleidint == Constants.Acc_Ledger_ModuleId) {
                AvoidRedundent = true;
            }
            if(isCustomDetailReport || reportid==Constants.CUSTOMER_REVENUE_REPORT){
                requestParams.put("order_by", Arrays.asList("moduleid", "sequence"));
                requestParams.put("order_type", Arrays.asList("asc", "asc"));
            }else{
                requestParams.put("order_by", Arrays.asList("sequence"));
                requestParams.put("order_type", Arrays.asList("asc"));
            }
            if(reportid==Constants.CUSTOMER_REVENUE_REPORT){
                String recInModule = "("+Constants.Acc_Invoice_ModuleId+","+Constants.Acc_FixedAssets_DisposalInvoice_ModuleId+","
                        +Constants.Acc_ConsignmentInvoice_ModuleId+","+Constants.LEASE_INVOICE_MODULEID+")";
                requestParams.put("recInModule", recInModule);
            }
            
//            }
            List lst=null;
            if ( moduleidint==100 || moduleidint == 101 || moduleidint==Constants.Acc_Ledger_ModuleId || reportid==Constants.CUSTOMER_REVENUE_REPORT || AvoidRedundent) {//Used this query to club the same name dimension/Custom fields
                result = accAccountDAOobj.getFieldParamsUsingSql(requestParams);
                lst = result.getEntityList();
            } else {
                requestParams.put("globallevelfields", globallevelfields);
                requestParams.put("linelevelfields", linelevelfields);
                requestParams.put("isforgstrulemapping", isforgstrulemapping);  // this value passsed in case of multientity==true
                result = accAccountDAOobj.getFieldParams(requestParams);
                lst = result.getEntityList();  
                //Sorting based on parent
                lst = accAccountDAOobj.sortOnParent(lst);
            }

            colcount = lst.size();
            if (isAdvanceSearch && !isCustomDetailReport && reportid!=Constants.CUSTOMER_REVENUE_REPORT && reportid!=Constants.SalesCommissionSchemaReport ) {
                JSONObject jSONObject = new JSONObject();
                if(lst.size()>0){
                    getModuleNameForAdvanceSearch(colcount, jresult, moduleid);
                }
            }
                
            AccJECustomData accBillInvCustomData = null;
            AccProductCustomData accProductCustomData = null;
            AccountCustomData accountCustomData = null;
            CustomerCustomData accCustomerCustomData = null;
            VendorCustomData accVendorCustomData = null;
            DeliveryOrderCustomData deliveryOrderCustomData= null;
            KwlReturnObject custumObjresult = null;
            Iterator ite = lst.iterator();
            int currentmoduleid=-1, nextmoduleid=-1;//To add module name in search field drop down only once
            while (ite.hasNext()) {
                FieldParams tmpcontyp = null;
                if (moduleidint==100 ||moduleidint == 101 || moduleidint==Constants.Acc_Ledger_ModuleId || reportid==Constants.CUSTOMER_REVENUE_REPORT ||AvoidRedundent) {
                    Object[] temp = (Object[]) ite.next();
                    KwlReturnObject fieldParamObj = null;
                    try {
                        fieldParamObj = accountingHandlerDAOobj.getObject(FieldParams.class.getName(), temp[1].toString());
                        tmpcontyp = (FieldParams) fieldParamObj.getEntityList().get(0);
                    } catch (ServiceException ex) {
                        Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                } else {
                    tmpcontyp = (FieldParams) ite.next();
                }
                if (isForProductCustomFieldHistoryCombo && !(tmpcontyp.getFieldtype() == 1 || tmpcontyp.getFieldtype() == 2)) {
                    continue;
                }
                if(isCustomDetailReport || reportid==Constants.CUSTOMER_REVENUE_REPORT){
                    currentmoduleid = tmpcontyp.getModuleid();
                    if(currentmoduleid!=nextmoduleid){
                        nextmoduleid=currentmoduleid;
                        getModuleNameForAdvanceSearch(colcount, jresult, tmpcontyp.getModuleid());
                    }
                }
                
                JSONObject jobj = new JSONObject();
                jobj.put("fieldname", tmpcontyp.getFieldname());
                jobj.put("sequence",tmpcontyp.getSequence());
                String defaultvalue = tmpcontyp.getDefaultValue();
                String fieldDisplayData = "";
                String Defaultcombodata = "";

                if (!StringUtil.isNullOrEmpty(defaultvalue)) {
                    if (tmpcontyp.getFieldtype() == 4 || tmpcontyp.getFieldtype() == 7) {
                        HashMap<String, Object> comborequestParams = new HashMap<String, Object>();
                        if (tmpcontyp.isIsAutoPopulateDefaultValue()) {
                            jobj.put("_Values", defaultvalue);
                        }
                        String[] combovalues = defaultvalue.split(",");
                        for (int cnt = 0; cnt < combovalues.length; cnt++) {

                            String trimArray = combovalues[cnt].trim();
                            if (!StringUtil.isNullOrEmpty(trimArray)) {

                                comborequestParams.put("Fieldid", tmpcontyp.getId());
                                comborequestParams.put("Value", combovalues[cnt]);
                                KwlReturnObject comboData = accAccountDAOobj.getfieldcombodata(comborequestParams);

                                FieldComboData fc = null;
                                // check default value is same as current combo value if yes add Id it to default value
                                if (comboData != null && comboData.getRecordTotalCount()>0) {
                                    fc = (FieldComboData) comboData.getEntityList().get(0);
                                    Defaultcombodata = Defaultcombodata + fc.getId() + ",";
                                    fieldDisplayData = fieldDisplayData + fc.getValue() + ",";//ERP-28585 -[PM/PR] - Entry form containing mandatory custom field/dimension default value are not showing.
                                }
                            }
                        }

                        if (Defaultcombodata.length() > 0) {
                            defaultvalue = Defaultcombodata.substring(0, Defaultcombodata.length() - 1);
                        } else if(defaultvalue.equals(Constants.NONE)){
                            /*
                             *if default value is None
                             */
                            defaultvalue = Constants.NONEID;
                            fieldDisplayData = Constants.NONE;
                        }else{
                            defaultvalue = "";
                        }
                    }
                    if (tmpcontyp.isIsAutoPopulateDefaultValue() && StringUtil.isNullOrEmpty(jeId)) {
                        /*
                         * For MP & RP used setValForRemoteStore() to set combo values.
                         * ERP-28585
                         */
                        if ((tmpcontyp.getModuleid() == Constants.Acc_Make_Payment_ModuleId || tmpcontyp.getModuleid() == Constants.Acc_Receive_Payment_ModuleId) || (fetchdataid == Constants.Acc_Make_Payment_ModuleId || fetchdataid == Constants.Acc_Receive_Payment_ModuleId)) {
                            if (fieldDisplayData.length() > 0 && !fieldDisplayData.equals(Constants.NONE)) {
                                fieldDisplayData = fieldDisplayData.substring(0, fieldDisplayData.length() - 1);
                            }
                            jobj.put("fieldDisplayData", fieldDisplayData);
                            jobj.put("id_Values", defaultvalue);//ERP-28585
                        }
                        jobj.put("fieldData", defaultvalue);
                    }
                }
                moduleid = -1;
                if(!StringUtil.isNullOrEmpty(module))
                { moduleid = Integer.parseInt(module);}
                if (!StringUtil.isNullOrEmpty(jeId)) {
                    try {
                        if(moduleid ==34){
                            custumObjresult = accountingHandlerDAOobj.getObject(AccountCustomData.class.getName(), jeId);
                       } else if(moduleid<30 || moduleid==Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId || moduleid==Constants.Acc_FixedAssets_Purchase_Order_ModuleId || moduleid==Constants.JOB_WORK_OUT_ORDER_MODULEID) {
                            switch (moduleid) {
                               case 18 :
                               case Constants.JOB_WORK_OUT_ORDER_MODULEID:
                               case Constants.Acc_FixedAssets_Purchase_Order_ModuleId :
                                    custumObjresult = accountingHandlerDAOobj.getObject(PurchaseOrderCustomData.class.getName(), jeId);
                                    break;
                               case 20 : 
                                    custumObjresult = accountingHandlerDAOobj.getObject(SalesOrderCustomData.class.getName(), jeId);
                                    break;
                               case 22 : 
                                    custumObjresult = accountingHandlerDAOobj.getObject(QuotationCustomData.class.getName(), jeId);
                                    break;
                               case 23 : 
                               case Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId :
                                    custumObjresult = accountingHandlerDAOobj.getObject(VendorQuotationCustomData.class.getName(), jeId);
                                    break;
                               case 24 : 
                                    custumObjresult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), jeId);
                                    break;
                               case 25 : 
                                    custumObjresult = accountingHandlerDAOobj.getObject(CustomerCustomData.class.getName(), jeId);
                                    break;
                               case 26 : 
                                    custumObjresult = accountingHandlerDAOobj.getObject(VendorCustomData.class.getName(), jeId);
                                    break;
                               case 27 : 
                                    custumObjresult = accountingHandlerDAOobj.getObject(DeliveryOrderCustomData.class.getName(), jeId);
                                    break;
                               case 28 : 
                                    custumObjresult = accountingHandlerDAOobj.getObject(GoodsReceiptOrderCustomData.class.getName(), jeId);
                                    break;
                               case 29 : 
                                    custumObjresult = accountingHandlerDAOobj.getObject(SalesReturnCustomData.class.getName(), jeId);
                                    break;
                                default:
                                    if (isOpeningTransaction) {
                                        switch (moduleid) {
                                            case 2:
                                                custumObjresult = accountingHandlerDAOobj.getObject(OpeningBalanceInvoiceCustomData.class.getName(), jeId);
                                                break;
                                            case 6:
                                                custumObjresult = accountingHandlerDAOobj.getObject(OpeningBalanceVendorInvoiceCustomData.class.getName(), jeId);
                                                break;
                                            case 10:
                                                custumObjresult = accountingHandlerDAOobj.getObject(OpeningBalanceDebitNoteCustomData.class.getName(), jeId);
                                                break;
                                            case 12:
                                                custumObjresult = accountingHandlerDAOobj.getObject(OpeningBalanceCreditNoteCustomData.class.getName(), jeId);
                                                break;
                                            case 14:
                                                custumObjresult = accountingHandlerDAOobj.getObject(OpeningBalanceMakePaymentCustomData.class.getName(), jeId);
                                                break;
                                            case 16:
                                                custumObjresult = accountingHandlerDAOobj.getObject(OpeningBalanceReceiptCustomData.class.getName(), jeId);
                                                break;
                                        }
                                    }else{
                                         String []journalEntryID = jeId.split(",");//ERP-6276    In edit of RP,dimension and custom field value does not reflect as Multiple Journal Entry For some records is posted 
                                         custumObjresult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), journalEntryID[0]);
                                    }
                            }
                        } else if (moduleid == 31 || moduleid==Constants.Acc_ConsignmentPurchaseReturn_ModuleId || moduleid==Constants.Acc_FixedAssets_Purchase_Return_ModuleId){
                            custumObjresult = accountingHandlerDAOobj.getObject(PurchaseReturnCustomData.class.getName(), jeId);
                        } else if(moduleid==32 || moduleid==Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId){
                            custumObjresult = accountingHandlerDAOobj.getObject(PurchaseRequisitionCustomData.class.getName(), jeId);
                        }else if(moduleid==33 || moduleid==Constants.Acc_FixedAssets_RFQ_ModuleId){
                            custumObjresult = accountingHandlerDAOobj.getObject(RFQCustomData.class.getName(), jeId);
                        }else if(moduleid==36){
                            custumObjresult = accountingHandlerDAOobj.getObject(SalesOrderCustomData.class.getName(), jeId);
                        }else if(moduleid==35){
                            custumObjresult = accountingHandlerDAOobj.getObject(ContractCustomData.class.getName(), jeId);
                        }else if(moduleid== Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId || moduleid== Constants.Acc_FixedAssets_DisposalInvoice_ModuleId ){
                            custumObjresult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), jeId);
                        }else if(moduleid== Constants.Acc_FixedAssets_GoodsReceipt_ModuleId  ){
                            custumObjresult = accountingHandlerDAOobj.getObject(GoodsReceiptOrderCustomData.class.getName(), jeId);
                        }else if(moduleid== Constants.Acc_FixedAssets_DeliveryOrder_ModuleId ){
                            custumObjresult = accountingHandlerDAOobj.getObject(DeliveryOrderCustomData.class.getName(), jeId);
                        } else if(moduleid== Constants.Acc_ConsignmentVendorRequest_ModuleId ){
                            custumObjresult = accountingHandlerDAOobj.getObject(PurchaseOrderCustomData.class.getName(), jeId);
                        }else if(moduleid== Constants.Acc_ConsignmentRequest_ModuleId ){
                            custumObjresult = accountingHandlerDAOobj.getObject(SalesOrderCustomData.class.getName(), jeId);
                        }else if(moduleid== Constants.Acc_ConsignmentDeliveryOrder_ModuleId ){
                            custumObjresult = accountingHandlerDAOobj.getObject(DeliveryOrderCustomData.class.getName(), jeId);
                        }else if(moduleid== Constants.Acc_Consignment_GoodsReceiptOrder_ModuleId ){
                            custumObjresult = accountingHandlerDAOobj.getObject(GoodsReceiptOrderCustomData.class.getName(), jeId);
                        }else if(moduleid== Constants.Acc_ConsignmentSalesReturn_ModuleId || moduleid== Constants.Acc_FixedAssets_Sales_Return_ModuleId){
                            custumObjresult = accountingHandlerDAOobj.getObject(SalesReturnCustomData.class.getName(), jeId);
                        }else if(moduleid== Constants.Acc_ConsignmentInvoice_ModuleId || moduleid==Constants.Acc_Consignment_GoodsReceipt_ModuleId){
                            custumObjresult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), jeId);
                        }else if(moduleid== Constants.LEASE_INVOICE_MODULEID){
                            custumObjresult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), jeId);
                        }else if(moduleid== Constants.Acc_Lease_Contract){
                            custumObjresult = accountingHandlerDAOobj.getObject(ContractCustomData.class.getName(), jeId);
                        }else if(moduleid== Constants.Acc_Lease_Quotation){
                            custumObjresult = accountingHandlerDAOobj.getObject(QuotationCustomData.class.getName(), jeId);
                        }else if(moduleid== Constants.Acc_Lease_DO){
                            custumObjresult = accountingHandlerDAOobj.getObject(DeliveryOrderCustomData.class.getName(), jeId);
                        }else if(moduleid== Constants.Acc_Lease_Return){
                            custumObjresult = accountingHandlerDAOobj.getObject(SalesReturnCustomData.class.getName(), jeId);
                        }else if(moduleid== Constants.Labour_Master){
                            custumObjresult = accountingHandlerDAOobj.getObject(LabourCustomData.class.getName(), jeId);
                        }else if(moduleid== Constants.MRP_WORK_CENTRE_MODULEID){
                            custumObjresult = accountingHandlerDAOobj.getObject(WorkCentreCustomData.class.getName(), jeId);
                        }else if(moduleid== Constants.MRP_Machine_Management_ModuleId){
                            custumObjresult = accountingHandlerDAOobj.getObject(MachineCustomData.class.getName(), jeId);
                        }else if(moduleid== Constants.MRP_WORK_ORDER_MODULEID){
                            custumObjresult = accountingHandlerDAOobj.getObject(WorkOrderCustomData.class.getName(), jeId);
                        } else if (moduleid == Constants.VENDOR_JOB_WORKORDER_MODULEID) {
                            custumObjresult = accountingHandlerDAOobj.getObject(SalesOrderCustomData.class.getName(), jeId);
                        } else if (moduleid == Constants.MRP_Contract) {
                            custumObjresult = accountingHandlerDAOobj.getObject(MRPContractCustomData.class.getName(), jeId);
                        }else if(moduleid== Constants.MRP_RouteCode){
                            custumObjresult = accountingHandlerDAOobj.getObject(RoutingTemplateCustomData.class.getName(), jeId);
                        }else if(moduleid== Constants.MRP_JOB_WORK_MODULEID){
                            custumObjresult = accountingHandlerDAOobj.getObject(JobWorkCustomData.class.getName(), jeId);
                        }else if(moduleid== Constants.Acc_SecurityGateEntry_ModuleId){   //newly added module Security Gate Entry
                            custumObjresult = accountingHandlerDAOobj.getObject(SecurityGateEntryCustomData.class.getName(), jeId);
                        }else if(moduleid== Constants.Acc_Multi_Entity_Dimension_MODULEID){                               
                            custumObjresult = accountingHandlerDAOobj.getObject(MultiEntityDimesionCustomData.class.getName(), jeId);
                        }else if (moduleid == Constants.Inventory_Stock_Adjustment_ModuleId) {
                            custumObjresult = accountingHandlerDAOobj.getObject(StockAdjustmentCustomData.class.getName(), jeId);
                        }else {
                            custumObjresult = accountingHandlerDAOobj.getObject(AccProductCustomData.class.getName(), jeId);
                        }
                        
                        /*if(moduleid==25 || moduleid==26){
                        custumObjresult = accountingHandlerDAOobj.getObject(AccountCustomData.class.getName(), jeId);
                        }  else if(moduleid == 27) {
                        custumObjresult = accountingHandlerDAOobj.getObject(DeliveryOrderCustomData.class.getName(), jeId); 
                        } else{ 
                        if(moduleid<30)
                        custumObjresult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), jeId);
                        else
                        custumObjresult = accountingHandlerDAOobj.getObject(AccProductCustomData.class.getName(), jeId);
                        } */
                    } catch (Exception e) {
                        Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, e);
                    }
                    if(moduleid == 25){
                     accCustomerCustomData = (CustomerCustomData) custumObjresult.getEntityList().get(0);
                        if (accCustomerCustomData != null) {
                            String coldata = accCustomerCustomData.getCol(tmpcontyp.getColnum());
                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                jobj.put("fieldData", coldata);
                                if (isautopopulatedata && (tmpcontyp.getFieldtype() == 4 || tmpcontyp.getFieldtype() == 7 || tmpcontyp.getFieldtype() == 12)) {

                                    String[] coldataArray = coldata.split(",");
                                    String Coldata = "";
                                    for (int countArray = 0; countArray < coldataArray.length; countArray++) {
                                        Coldata += "'" + coldataArray[countArray] + "',";
                                    }
                                    Coldata = Coldata.substring(0, Coldata.length() - 1);
                                    String ColValue = accAccountDAOobj.getfieldcombodatabyids(Coldata);
                                    jobj.put("_Values", ColValue);
                                    if (fetchdataid == 14 || fetchdataid == 16) {
                                        HashMap<String, Object> reqParams = new HashMap<String, Object>();
                                        reqParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                                        reqParams.put(Constants.filter_values, Arrays.asList(companyid, fetchdataid, tmpcontyp.getFieldlabel()));
                                        reqParams.put("order_by", Arrays.asList("sequence"));
                                        reqParams.put("order_type", Arrays.asList("asc"));
                                        reqParams.put("isActivated", 1);
                                        result = accAccountDAOobj.getFieldParams(reqParams);
                                        List<FieldParams> list = result.getEntityList();
                                        if (list.size() > 0) {
                                            String fieldId = "";
                                            for (FieldParams fieldParams : list) {
                                                fieldId = fieldParams.getId();
                                            }
                                            String id = fieldManagerDAOobj.getIdsUsingParamsValue(fieldId, ColValue);
                                            jobj.put("id_Values", id);
                                        }
                                    }
                                }
                            }
                        }
                    } else  if(moduleid == 26){
                     accVendorCustomData = (VendorCustomData) custumObjresult.getEntityList().get(0);
                        if (accVendorCustomData != null) {
                            String coldata = accVendorCustomData.getCol(tmpcontyp.getColnum());
                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                jobj.put("fieldData", coldata);
                                if (isautopopulatedata && (tmpcontyp.getFieldtype() == 4 || tmpcontyp.getFieldtype() == 7 || tmpcontyp.getFieldtype() == 12)) {
                                    String[] coldataArray = coldata.split(",");
                                    String Coldata = "";
                                    for (int countArray = 0; countArray < coldataArray.length; countArray++) {
                                        Coldata += "'" + coldataArray[countArray] + "',";
                                    }
                                    Coldata = Coldata.substring(0, Coldata.length() - 1);
                                    String ColValue = accAccountDAOobj.getfieldcombodatabyids(Coldata);
                                    jobj.put("_Values", ColValue);
                                    if (fetchdataid == 14 || fetchdataid == 16) {
                                        HashMap<String, Object> reqParams = new HashMap<String, Object>();
                                        reqParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                                        reqParams.put(Constants.filter_values, Arrays.asList(companyid, fetchdataid, tmpcontyp.getFieldlabel()));
                                        reqParams.put("order_by", Arrays.asList("sequence"));
                                        reqParams.put("order_type", Arrays.asList("asc"));
                                        reqParams.put("isActivated", 1);
                                        result = accAccountDAOobj.getFieldParams(reqParams);
                                        List<FieldParams> list = result.getEntityList();
                                        if (list.size() > 0) {
                                            String fieldId = "";
                                            for (FieldParams fieldParams : list) {
                                                fieldId = fieldParams.getId();
                                            }
                                            String id = fieldManagerDAOobj.getIdsUsingParamsValue(fieldId, ColValue);
                                            jobj.put("id_Values", id);
                                        }
                                    }
                                }
                            }
                        }
                    }else if (moduleid ==34) {
                        accountCustomData = (AccountCustomData) custumObjresult.getEntityList().get(0);
                        if (accountCustomData != null) {
                            String coldata = accountCustomData.getCol(tmpcontyp.getColnum());
                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                jobj.put("fieldData", coldata);
                            }else{
                                jobj.put("fieldData", !StringUtil.isNullOrEmpty(defaultvalue) ? defaultvalue : "");
                            }
                        }
                    } else if(moduleid == 27 || moduleid==51){
                        deliveryOrderCustomData = (DeliveryOrderCustomData) custumObjresult.getEntityList().get(0);
                        if (deliveryOrderCustomData != null) {
                            String coldata = deliveryOrderCustomData.getCol(tmpcontyp.getColnum());
                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                jobj.put("fieldData", coldata);
                            }
                        }
                    } else {
                        if (moduleid < 30 || moduleid==Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId || moduleid==Constants.Acc_FixedAssets_Purchase_Order_ModuleId || moduleid==Constants.JOB_WORK_OUT_ORDER_MODULEID) {
                            if(moduleid==18 || moduleid==Constants.Acc_FixedAssets_Purchase_Order_ModuleId || moduleid==Constants.JOB_WORK_OUT_ORDER_MODULEID) {
                                PurchaseOrderCustomData purchaseOrderCustomData = (PurchaseOrderCustomData) custumObjresult.getEntityList().get(0);
                                if (purchaseOrderCustomData != null) {
                                    String coldata = purchaseOrderCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        if (tmpcontyp.getFieldtype() == 3 && !StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))) { //ERP-21781
                                            // String dateColData = accAccountDAOobj.getBrowserSpecificCustomDateLongValue(coldata, sessionHandlerImpl.getBrowserTZ(request));
                                            jobj.put("fieldData", coldata);
                                        } else {
                                            jobj.put("fieldData", coldata);
                                        }
                                    }
                                }
                            } else if(moduleid==20) {
                                SalesOrderCustomData salesOrderCustomData = (SalesOrderCustomData) custumObjresult.getEntityList().get(0);
                                if (salesOrderCustomData != null) {
                                    String coldata = salesOrderCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        if (tmpcontyp.getFieldtype() == 3 && !StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))) { //ERP-21781
                                           // String dateColData = accAccountDAOobj.getBrowserSpecificCustomDateLongValue(coldata, sessionHandlerImpl.getBrowserTZ(request));
                                            jobj.put("fieldData", coldata);
                                        } else {
                                            jobj.put("fieldData", coldata);
                                        }
                                        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), coldata);
                                        FieldComboData fieldData = (FieldComboData) curresult.getEntityList().get(0);
                                        if (fieldData != null) {
                                            jobj.put("fieldName", fieldData.getValue());
                                        }
                                        if ((tmpcontyp.getFieldtype() == 7 || tmpcontyp.getFieldtype() == 12)) {
                                            String[] coldataArray = coldata.split(",");
                                            String Coldata = "";
                                            for (int countArray = 0; countArray < coldataArray.length; countArray++) {
                                                Coldata += "'" + coldataArray[countArray] + "',";
                                            }
                                            Coldata = Coldata.substring(0, Coldata.length() - 1);
                                            String ColValue = accAccountDAOobj.getfieldcombodatabyids(Coldata);
                                            jobj.put("fieldName", ColValue);
                                        }
                                    }
                                }
                            } else if(moduleid==22) {
                                QuotationCustomData quotationCustomData = (QuotationCustomData) custumObjresult.getEntityList().get(0);
                                if (quotationCustomData != null) {
                                    String coldata = quotationCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jobj.put("fieldData", coldata);
                                    }
                                }
                            } else if(moduleid==23 || moduleid==Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId) {
                                VendorQuotationCustomData vendorQuotationCustomData = (VendorQuotationCustomData) custumObjresult.getEntityList().get(0);
                                if (vendorQuotationCustomData != null) {
                                    String coldata = vendorQuotationCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jobj.put("fieldData", coldata);
                                    }
                                }
                            } else if(moduleid==27) {
                                deliveryOrderCustomData = (DeliveryOrderCustomData) custumObjresult.getEntityList().get(0);
                                if (deliveryOrderCustomData != null) {
                                    String coldata = deliveryOrderCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jobj.put("fieldData", coldata);
                                    }
                                }
                            } else if(moduleid==28) {
                                GoodsReceiptOrderCustomData goodsReceiptOrderCustomData = (GoodsReceiptOrderCustomData) custumObjresult.getEntityList().get(0);
                                if (goodsReceiptOrderCustomData != null) {
                                    String coldata = goodsReceiptOrderCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jobj.put("fieldData", coldata);
                                    }
                                }
                            } else if(moduleid==29) {
                                SalesReturnCustomData salesReturnCustomData = (SalesReturnCustomData) custumObjresult.getEntityList().get(0);
                                if (salesReturnCustomData != null) {
                                    String coldata = salesReturnCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jobj.put("fieldData", coldata);
                                    }
                                }
                            } else if(moduleid==2 && isOpeningTransaction) {
                                OpeningBalanceInvoiceCustomData openingBalanceInvoiceCustomData = (OpeningBalanceInvoiceCustomData) custumObjresult.getEntityList().get(0);
                                if (openingBalanceInvoiceCustomData != null) {
                                    String coldata = openingBalanceInvoiceCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jobj.put("fieldData", coldata);
                                    }
                                }
                            } else if(moduleid==6 && isOpeningTransaction) {
                                OpeningBalanceVendorInvoiceCustomData openingBalanceVendorInvoiceCustomData = (OpeningBalanceVendorInvoiceCustomData) custumObjresult.getEntityList().get(0);
                                if (openingBalanceVendorInvoiceCustomData != null) {
                                    String coldata = openingBalanceVendorInvoiceCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jobj.put("fieldData", coldata);
                                    }
                                }
                            } else if(moduleid==16 && isOpeningTransaction) {
                                OpeningBalanceReceiptCustomData openingBalanceReceiptCustomData = (OpeningBalanceReceiptCustomData) custumObjresult.getEntityList().get(0);
                                if (openingBalanceReceiptCustomData != null) {
                                    String coldata = openingBalanceReceiptCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jobj.put("fieldData", coldata);
                                        if (tmpcontyp.getFieldtype() == 4) {
                                            FieldComboData fieldComboData = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), coldata);
                                            if (fieldComboData != null) {
                                                jobj.put("fieldDisplayData", fieldComboData.getValue() != null ? fieldComboData.getValue() : "");
                                            }
                                        }
                                    }
                                }
                            }else if(moduleid==10 && isOpeningTransaction) {
                                OpeningBalanceDebitNoteCustomData openingBalanceDebitNoteCustomData = (OpeningBalanceDebitNoteCustomData) custumObjresult.getEntityList().get(0);
                                if (openingBalanceDebitNoteCustomData != null) {
                                    String coldata = openingBalanceDebitNoteCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jobj.put("fieldData", coldata);
                                    }
                                }
                            }else if(moduleid==12 && isOpeningTransaction) {
                                OpeningBalanceCreditNoteCustomData openingBalanceCreditNoteCustomData = (OpeningBalanceCreditNoteCustomData) custumObjresult.getEntityList().get(0);
                                if (openingBalanceCreditNoteCustomData != null) {
                                    String coldata = openingBalanceCreditNoteCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jobj.put("fieldData", coldata);
                                    }
                                }
                            }else if(moduleid==14 && isOpeningTransaction) {
                                OpeningBalanceMakePaymentCustomData openingBalanceMakePaymentCustomData = (OpeningBalanceMakePaymentCustomData) custumObjresult.getEntityList().get(0);
                                if (openingBalanceMakePaymentCustomData != null) {
                                    String coldata = openingBalanceMakePaymentCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jobj.put("fieldData", coldata);
                                         if(tmpcontyp.getFieldtype()==4) {
                                            FieldComboData fieldComboData = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), coldata);
                                            if (fieldComboData != null) {
                                                jobj.put("fieldDisplayData", fieldComboData.getValue() != null ? fieldComboData.getValue() : "");
                                            }
                                        }
                                    }
                                }
                            }else {
                                accBillInvCustomData = (AccJECustomData) custumObjresult.getEntityList().get(0);
                                if (accBillInvCustomData != null) {
                                    String coldata = accBillInvCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        if (tmpcontyp.getFieldtype() == 3 && !StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))) { //ERP-21781 / ERP-21982
                                           //String dateColData = accAccountDAOobj.getBrowserSpecificCustomDateLongValue(coldata, sessionHandlerImpl.getBrowserTZ(request));
                                            jobj.put("fieldData", coldata);
                                        } else {
                                            jobj.put("fieldData", coldata);
                                        }
                                        if(tmpcontyp.getFieldtype()==4) {
                                            FieldComboData fieldComboData = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), coldata);
                                            if (fieldComboData != null) {
                                                jobj.put("fieldDisplayData", fieldComboData.getValue() != null ? fieldComboData.getValue() : "");
                                            }
                                        }
                                    }
                                }
                            }
                        }else if(moduleid==32 || moduleid==Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId) {
                                PurchaseRequisitionCustomData purchaseRequisitionCustomData = (PurchaseRequisitionCustomData) custumObjresult.getEntityList().get(0);
                                if (purchaseRequisitionCustomData != null) {
                                    String coldata = purchaseRequisitionCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jobj.put("fieldData", coldata);
                                    }
                                }
                        }else if(moduleid==33 || moduleid==Constants.Acc_FixedAssets_RFQ_ModuleId) {
                                RFQCustomData rfqCustomData = (RFQCustomData) custumObjresult.getEntityList().get(0);
                                if (rfqCustomData != null) {
                                    String coldata = rfqCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jobj.put("fieldData", coldata);
                                    }
                                }
                        }else if (moduleid == 31 || moduleid==59 || moduleid==Constants.Acc_FixedAssets_Purchase_Return_ModuleId) {
                            PurchaseReturnCustomData purchaseReturnCustomData = (PurchaseReturnCustomData) custumObjresult.getEntityList().get(0);
                            if (purchaseReturnCustomData != null) {
                                String coldata = purchaseReturnCustomData.getCol(tmpcontyp.getColnum());
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    jobj.put("fieldData", coldata);
                                }
                            }
                        }else if(moduleid==35 || moduleid==Constants.Acc_Lease_Contract) {
                                ContractCustomData ContractCustomData = (ContractCustomData) custumObjresult.getEntityList().get(0);
                                if (ContractCustomData != null) {
                                    DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                                    Date dateFromDB=null;
                                    String coldata = ContractCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jobj.put("fieldData", coldata);
                                        jobj.put("fieldName",coldata);//ERP-24144
                                        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(),coldata);
                                        FieldComboData fieldData = (FieldComboData) curresult.getEntityList().get(0);
                                        if(fieldData != null){
                                            jobj.put("fieldName",fieldData.getValue());
                                        }
                                        if (tmpcontyp.getFieldtype() == 3) {
                                            SimpleDateFormat ft = new SimpleDateFormat("MM/dd/yyyy");
                                            try {
                                                dateFromDB = defaultDateFormat.parse(coldata);
                                                coldata = ft.format(dateFromDB);
                                            } catch (ParseException p) {

                                            }
                                            jobj.put("fieldName", coldata);
                                        }
                                        if ((tmpcontyp.getFieldtype() == 7 || tmpcontyp.getFieldtype() == 12)) {
                                            String[] coldataArray = coldata.split(",");
                                            String Coldata = "";
                                            for (int countArray = 0; countArray < coldataArray.length; countArray++) {
                                                Coldata += "'" + coldataArray[countArray] + "',";
                                            }
                                            Coldata = Coldata.substring(0, Coldata.length() - 1);
                                            String ColValue = accAccountDAOobj.getfieldcombodatabyids(Coldata);
                                            jobj.put("fieldName", ColValue);
                                        }
                                    }
                                }
                          }else if(moduleid==Constants.Acc_SecurityGateEntry_ModuleId) {  // retrieving data for Security Gate Entry module 
                                SecurityGateEntryCustomData securityGateEntryCustomData = (SecurityGateEntryCustomData) custumObjresult.getEntityList().get(0);
                                if (securityGateEntryCustomData != null) {
                                    String coldata = securityGateEntryCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        if (tmpcontyp.getFieldtype() == 3 && !StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))) { 
                                           jobj.put("fieldData", coldata);
                                        } else {
                                            jobj.put("fieldData", coldata);
                                        }
                                        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), coldata);
                                        FieldComboData fieldData = (FieldComboData) curresult.getEntityList().get(0);
                                        if (fieldData != null) {
                                            jobj.put("fieldName", fieldData.getValue());
                                        }
                                        if ((tmpcontyp.getFieldtype() == 7 || tmpcontyp.getFieldtype() == 12)) {
                                            String[] coldataArray = coldata.split(",");
                                            String Coldata = "";
                                            for (int countArray = 0; countArray < coldataArray.length; countArray++) {
                                                Coldata += "'" + coldataArray[countArray] + "',";
                                            }
                                            Coldata = Coldata.substring(0, Coldata.length() - 1);
                                            String ColValue = accAccountDAOobj.getfieldcombodatabyids(Coldata);
                                            jobj.put("fieldName", ColValue);
                                        }
                                    }
                                }
                            } else if(moduleid==Constants.Acc_Multi_Entity_Dimension_MODULEID) {  // retrieving data for MultiEntityDimesionCustomData
                                MultiEntityDimesionCustomData multiEntityDimesionCustomData = (MultiEntityDimesionCustomData) custumObjresult.getEntityList().get(0);
                                if (multiEntityDimesionCustomData != null) {
                                    String coldata = multiEntityDimesionCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        if (tmpcontyp.getFieldtype() == 3 && !StringUtil.isNullOrEmpty(sessionHandlerImpl.getBrowserTZ(request))) { 
                                           jobj.put("fieldData", coldata);
                                        } else {
                                            jobj.put("fieldData", coldata);
                                        }
                                        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), coldata);
                                        FieldComboData fieldData = (FieldComboData) curresult.getEntityList().get(0);
                                        if (fieldData != null) {
                                            jobj.put("fieldName", fieldData.getValue());
                                        }
                                        if ((tmpcontyp.getFieldtype() == 7 || tmpcontyp.getFieldtype() == 12)) {
                                            String[] coldataArray = coldata.split(",");
                                            String Coldata = "";
                                            for (int countArray = 0; countArray < coldataArray.length; countArray++) {
                                                Coldata += "'" + coldataArray[countArray] + "',";
                                            }
                                            Coldata = Coldata.substring(0, Coldata.length() - 1);
                                            String ColValue = accAccountDAOobj.getfieldcombodatabyids(Coldata);
                                            jobj.put("fieldName", ColValue);
                                        }
                                    }
                                }
                            }else if( moduleid==36) {
                                SalesOrderCustomData salesOrderCustomData = (SalesOrderCustomData) custumObjresult.getEntityList().get(0);
                                if (salesOrderCustomData != null) {
                                    String coldata = salesOrderCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jobj.put("fieldData", coldata);
                                        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(),coldata);
                                        FieldComboData fieldData = (FieldComboData) curresult.getEntityList().get(0);
                                        if(fieldData != null){
                                            jobj.put("fieldName",fieldData.getValue());
                                        }
                                        if ((tmpcontyp.getFieldtype() == 7 || tmpcontyp.getFieldtype() == 12)) {
                                            String[] coldataArray = coldata.split(",");
                                            String Coldata = "";
                                            for (int countArray = 0; countArray < coldataArray.length; countArray++) {
                                                Coldata += "'" + coldataArray[countArray] + "',";
                                            }
                                            Coldata = Coldata.substring(0, Coldata.length() - 1);
                                            String ColValue = accAccountDAOobj.getfieldcombodatabyids(Coldata);
                                            jobj.put("fieldName", ColValue);
                                        }
                                    }
                                }
                            }else if(moduleid==Constants.Acc_FixedAssets_DeliveryOrder_ModuleId ||moduleid==Constants.Acc_Lease_DO ) {
                                deliveryOrderCustomData = (DeliveryOrderCustomData) custumObjresult.getEntityList().get(0);
                                if (deliveryOrderCustomData != null) {
                                    String coldata = deliveryOrderCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jobj.put("fieldData", coldata);
                                    }
                                }
                            } else if(moduleid==Constants.Acc_FixedAssets_GoodsReceipt_ModuleId ) {
                                GoodsReceiptOrderCustomData goodsReceiptOrderCustomData = (GoodsReceiptOrderCustomData) custumObjresult.getEntityList().get(0);
                                if (goodsReceiptOrderCustomData != null) {
                                    String coldata = goodsReceiptOrderCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jobj.put("fieldData", coldata);
                                    }
                                }
                            }else if(moduleid == Constants.Acc_FixedAssets_DisposalInvoice_ModuleId || moduleid == Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId){
                                accBillInvCustomData = (AccJECustomData) custumObjresult.getEntityList().get(0);
                                if (accBillInvCustomData != null) {
                                    String coldata = accBillInvCustomData.getCol(tmpcontyp.getColnum());
                                    if (!StringUtil.isNullOrEmpty(coldata)) {
                                        jobj.put("fieldData", coldata);
                                    }
                                }
                            }else if (moduleid == 30 || moduleid == Constants.Acc_FixedAssets_AssetsGroups_ModuleId) {
                            accProductCustomData = (AccProductCustomData) custumObjresult.getEntityList().get(0);
                            if (accProductCustomData != null) {
                                String coldata = accProductCustomData.getCol(tmpcontyp.getColnum());
                                Object fieldValueObject = getProductCustomFieldValue(tmpcontyp.getId(), accProductCustomData.getProductId(), companyid, currentDate);
                                String latestValue = "";
                                if (fieldValueObject != null) {
                                    latestValue = (String) fieldValueObject;
                                }
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    if (latestValue.equalsIgnoreCase(coldata) || StringUtil.isNullOrEmpty(latestValue)) {
                                        jobj.put("fieldData", coldata);
                                    } else {
                                        jobj.put("fieldData", latestValue);
                                    }
                                }
                            }
                        } else if (moduleid == 50) {
                            SalesOrderCustomData salesOrderCustomData = (SalesOrderCustomData) custumObjresult.getEntityList().get(0);
                            if (salesOrderCustomData != null) {
                                String coldata = salesOrderCustomData.getCol(tmpcontyp.getColnum());
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    jobj.put("fieldData", coldata);
                                }
                            }
                        } else if (moduleid == 57) {
                            GoodsReceiptOrderCustomData goodsReceiptOrderCustomData = (GoodsReceiptOrderCustomData) custumObjresult.getEntityList().get(0);
                            if (goodsReceiptOrderCustomData != null) {
                                String coldata = goodsReceiptOrderCustomData.getCol(tmpcontyp.getColnum());
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    jobj.put("fieldData", coldata);
                                }
                            }
                        } else if (moduleid == 53 || moduleid==Constants.Acc_Lease_Return || moduleid==Constants.Acc_FixedAssets_Sales_Return_ModuleId) {
                            SalesReturnCustomData salesReturnCustomData = (SalesReturnCustomData) custumObjresult.getEntityList().get(0);
                            if (salesReturnCustomData != null) {
                                String coldata = salesReturnCustomData.getCol(tmpcontyp.getColnum());
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    jobj.put("fieldData", coldata);
                                }
                            }
                        } else if (moduleid == 63) {
                            PurchaseOrderCustomData purchaseOrderCustomData = (PurchaseOrderCustomData) custumObjresult.getEntityList().get(0);
                            if (purchaseOrderCustomData != null) {
                                String coldata = purchaseOrderCustomData.getCol(tmpcontyp.getColnum());
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    jobj.put("fieldData", coldata);
                                }
                            }
                        } else if (moduleid == 52 || moduleid == 58) {
                            accBillInvCustomData = (AccJECustomData) custumObjresult.getEntityList().get(0);
                            if (accBillInvCustomData != null) {
                                String coldata = accBillInvCustomData.getCol(tmpcontyp.getColnum());
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    jobj.put("fieldData", coldata);
                                    if (tmpcontyp.getFieldtype() == 4) {
                                        FieldComboData fieldComboData = (FieldComboData) kwlCommonTablesDAOObj.getClassObject(FieldComboData.class.getName(), coldata);
                                        if (fieldComboData != null) {
                                            jobj.put("fieldDisplayData", fieldComboData.getValue() != null ? fieldComboData.getValue() : "");
                                        }
                                    }
                                }
                            }
                        } else if (moduleid == Constants.Acc_Lease_Quotation) {
                            QuotationCustomData quotationCustomData = (QuotationCustomData) custumObjresult.getEntityList().get(0);
                            if (quotationCustomData != null) {
                                String coldata = quotationCustomData.getCol(tmpcontyp.getColnum());
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    jobj.put("fieldData", coldata);
                                }
                            }
                        } else if (moduleid == 93) {
                           accBillInvCustomData=(AccJECustomData) custumObjresult.getEntityList().get(0);
                            if (accBillInvCustomData != null) {
                                String coldata = accBillInvCustomData.getCol(tmpcontyp.getColnum());
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    jobj.put("fieldData", coldata);
                                }
                            }
                            
                        } else if (moduleid == Constants.Labour_Master) {
                            LabourCustomData labourCustomData = (LabourCustomData) custumObjresult.getEntityList().get(0);
                            if (labourCustomData != null) {
                                String coldata = labourCustomData.getCol(tmpcontyp.getColnum());
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    jobj.put("fieldData", coldata);
                                }
                            }
                        } else if (moduleid == Constants.MRP_WORK_CENTRE_MODULEID) {
                            WorkCentreCustomData workCentreCustomData = (WorkCentreCustomData) custumObjresult.getEntityList().get(0);
                            if (workCentreCustomData != null) {
                                String coldata = workCentreCustomData.getCol(tmpcontyp.getColnum());
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    jobj.put("fieldData", coldata);
                                }
                            }
                        } else if (moduleid == Constants.MRP_Machine_Management_ModuleId) {
                            MachineCustomData machineCustomData = (MachineCustomData) custumObjresult.getEntityList().get(0);
                            if (machineCustomData != null) {
                                String coldata = machineCustomData.getCol(tmpcontyp.getColnum());
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    jobj.put("fieldData", coldata);
                                }
                            }
                        } else if (moduleid == Constants.MRP_WORK_ORDER_MODULEID) {
                            WorkOrderCustomData workOrderCustomData = (WorkOrderCustomData) custumObjresult.getEntityList().get(0);
                            if (workOrderCustomData != null) {
                                String coldata = workOrderCustomData.getCol(tmpcontyp.getColnum());
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    jobj.put("fieldData", coldata);
                                }
                            }
                        } else if (moduleid == Constants.VENDOR_JOB_WORKORDER_MODULEID) {
                            SalesOrderCustomData workOrderCustomData = (SalesOrderCustomData) custumObjresult.getEntityList().get(0);
                            if (workOrderCustomData != null) {
                                String coldata = workOrderCustomData.getCol(tmpcontyp.getColnum());
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    jobj.put("fieldData", coldata);
                                }
                            }
                        } else if (moduleid == Constants.MRP_Contract) {
                            MRPContractCustomData mrpContractCustomData = (MRPContractCustomData) custumObjresult.getEntityList().get(0);
                            if (mrpContractCustomData != null) {
                                String coldata = mrpContractCustomData.getCol(tmpcontyp.getColnum());
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    jobj.put("fieldData", coldata);
                                }
                            }
                        } else if (moduleid == Constants.MRP_RouteCode) {
                            RoutingTemplateCustomData routingTemplateCustomData = (RoutingTemplateCustomData) custumObjresult.getEntityList().get(0);
                            if (routingTemplateCustomData != null) {
                                String coldata = routingTemplateCustomData.getCol(tmpcontyp.getColnum());
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    jobj.put("fieldData", coldata);
                                }
                            }
                        } else if (moduleid == Constants.MRP_JOB_WORK_MODULEID) {
                            JobWorkCustomData jobWorkCustomData = (JobWorkCustomData) custumObjresult.getEntityList().get(0);
                            if (jobWorkCustomData != null) {
                                String coldata = jobWorkCustomData.getCol(tmpcontyp.getColnum());
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    jobj.put("fieldData", coldata);
                                }
                            }
                        }else if (moduleid == Constants.Inventory_Stock_Adjustment_ModuleId) {
                            StockAdjustmentCustomData stockadjCustomData = (StockAdjustmentCustomData) custumObjresult.getEntityList().get(0);
                            if (stockadjCustomData != null) {
                                String coldata = stockadjCustomData.getCol(tmpcontyp.getColnum());
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    jobj.put("fieldData", coldata);
                                }
                            }
                        }else {
                            accProductCustomData = (AccProductCustomData) custumObjresult.getEntityList().get(0);
                            if (accProductCustomData != null) {
                                String coldata = accProductCustomData.getCol(tmpcontyp.getColnum());
                                if (!StringUtil.isNullOrEmpty(coldata)) {
                                    jobj.put("fieldData", coldata);
                                }
                            }
                        }
                  }
                }
                if (StringUtil.isNullOrEmpty(module)) {
                    if (lineitem == 0) {
                        if (!StringUtil.isNullOrEmpty(request.getParameter("customfieldlableflag"))) {  //[mayur B] is custom field flag
                            jobj.put("fieldlabel", tmpcontyp.getFieldlabel());
                        } else {
                            jobj.put("fieldlabel", tmpcontyp.getFieldlabel() + "(" + getModuleName(tmpcontyp.getModuleid()) + ")");
                        }
                    } else {
                        jobj.put("fieldlabel", tmpcontyp.getFieldlabel());
                    }
                } else {
                    if (moduleid > 99) {
                        if(moduleid==100 || moduleid==101 || moduleid==102 || isMRPModule(moduleid) || moduleid == Constants.Acc_Stock_Request_ModuleId || moduleid == Constants.Acc_InterStore_ModuleId || moduleid ==Constants.Acc_InterLocation_ModuleId || moduleid == Constants.Acc_CycleCount_ModuleId)
                            jobj.put("fieldlabel", tmpcontyp.getFieldlabel());
                        else
                        jobj.put("fieldlabel", tmpcontyp.getFieldlabel() + "(" + getModuleName(tmpcontyp.getModuleid()) + ")");
                    } else {
                        jobj.put("fieldlabel", tmpcontyp.getFieldlabel());
                    }
                }
                if(AvoidRedundent){
                        jobj.put("fieldlabel", tmpcontyp.getFieldlabel());
                }
                if(tmpcontyp.getFieldtype() == 12){
                     try {
                            JSONArray checkListArray = new JSONArray();
                            String fieldid = tmpcontyp.getId();
                            HashMap<String, Object> checkListRequestParams = new HashMap<String, Object>();
                            checkListRequestParams.put(Constants.filter_names, Arrays.asList(FieldConstants.Crm_fieldid, FieldConstants.Crm_deleteflag));
                            checkListRequestParams.put(Constants.filter_values, Arrays.asList(fieldid, 0));
                            ArrayList order_by = new ArrayList();
                            ArrayList order_type = new ArrayList();
                            order_by.add("itemsequence");
                            order_type.add("asc");
                            checkListRequestParams.put("order_by", order_by);
                            checkListRequestParams.put("order_type", order_type);
                            result = accAccountDAOobj.getCustomCombodata(checkListRequestParams);
                            List checklst = result.getEntityList();
                            Iterator checkite = checklst.iterator();

                            while (checkite.hasNext()) {
                                Object[] row = (Object[]) checkite.next();
                                FieldComboData checkfield = (FieldComboData) row[0];
                                JSONObject jobjTemp = new JSONObject();
                                jobjTemp.put(FieldConstants.Crm_id, checkfield.getId());
                                jobjTemp.put(FieldConstants.Crm_name, checkfield.getValue());
                                jobjTemp.put("fieldsetid", fieldid);
                                checkListArray.put(jobjTemp);
                            }
                            jobj.put("checkList",checkListArray.toString());
                        } catch (Exception ex) {
                            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                        }
                }
                jobj.put("isessential", tmpcontyp.getIsessential());
                jobj.put("autopopulatedefaultvalue", tmpcontyp.isIsAutoPopulateDefaultValue());
                jobj.put("maxlength", tmpcontyp.getMaxlength());
                jobj.put("validationtype", tmpcontyp.getValidationtype());
                jobj.put("fieldid", tmpcontyp.getId());
                jobj.put("moduleid", tmpcontyp.getModuleid());
                jobj.put(Constants.isMultiEntity,tmpcontyp.isFieldOfGivenGSTConfigType(Constants.isformultientity));
                jobj.put(Constants.isForKnockOff, tmpcontyp.isIsForKnockOff());
                
                /*
                  Check will be sent true if field is activated for Product Master i.e Product & Services.
                */
                if (reportid == Constants.inventoryValuation || reportid == Constants.stock_Ledger || reportid == Constants.DefaultBalanceSheetReportId || reportid == Constants.dimensionBasedBalanceSheet || reportid == Constants.dimensionBasedProfitLoss || moduleid == 101 || moduleid == 102) {
                    HashMap<String, Object> requestParam = new HashMap<String, Object>();
                    requestParam.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                    requestParam.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, tmpcontyp.getFieldlabel()));
                    requestParam.put("isActivated", 1);
                    result = accAccountDAOobj.getFieldParams(requestParam);
                    List<FieldParams> list = result.getEntityList();
                    if (list.size() > 0) {
                        jobj.put("isForProductMasterSearch", true);
                    }
                }
                
                if (!AvoidRedundent || isCustomDetailReport) {
                    jobj.put("modulename", getModuleName(tmpcontyp.getModuleid()));
                } else {
                    jobj.put("modulename", "");
                }
                jobj.put("fieldtooltip", tmpcontyp.getFieldtooltip()==null?"":tmpcontyp.getFieldtooltip());
                jobj.put("fieldtype", tmpcontyp.getFieldtype());
                jobj.put("iseditable", tmpcontyp.getIseditable());
                jobj.put("comboid", tmpcontyp.getComboid());
                /**
                 * ERP-32829 
                 * For GST related calculation  
                 */
                jobj.put("gstmappingcolnum", tmpcontyp.getGSTMappingColnum());
                jobj.put("isforgstrulemapping", tmpcontyp.isFieldOfGivenGSTConfigType(Constants.IsForGSTRuleMapping));
                if (isNewGST) {
                    jobj.put("hideField", isFieldHiddenForGST(tmpcontyp));
                } else {
                    jobj.put("hideField", false);//ERP-39536
                }
                jobj.put("gstConfigType",tmpcontyp.getGSTConfigType());
                /**
                 * ERP-34235
                 * Hide field Product category from GUI
                 * Hide field HSN/ SAC code from GUI
                 */
                if((tmpcontyp.getFieldlabel().equals(Constants.GSTProdCategory) || tmpcontyp.getFieldlabel().equals(Constants.HSN_SACCODE)) && (tmpcontyp.getModuleid()!=Constants.Acc_Product_Master_ModuleId && tmpcontyp.getModuleid()!=Constants.Acc_FixedAssets_AssetsGroups_ModuleId )){
                    jobj.put("hideField", true);
                }
                jobj.put("isformultientity", tmpcontyp.isFieldOfGivenGSTConfigType(Constants.isformultientity));
                jobj.put("comboname", tmpcontyp.getComboname());
                jobj.put("moduleflag", tmpcontyp.getModuleflag());
                jobj.put("parentid", tmpcontyp.getParentid());
                try {
                     requestParams.put("parentid",tmpcontyp.getId());
                    jobj.put("childstr", getChildString(requestParams));
                } catch (ServiceException ex) {
                    Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                jobj.put("refcolumn_number", Constants.Custom_Column_Prefix + tmpcontyp.getRefcolnum());
                jobj.put("column_number", Constants.Custom_Column_Prefix + tmpcontyp.getColnum());
                jobj.put("sendnotification", tmpcontyp.getsendNotification());
                jobj.put("notificationdays", tmpcontyp.getnotificationDays());
                jobj.put("iscustomfield", tmpcontyp.getCustomfield() == 1 ? true : false);
                jobj.put("iscustomcolumn", tmpcontyp.getCustomcolumn()==0 ? false : true);
//                jobj.put("iscustomcolumn", (tmpcontyp.getCustomcolumn() == 0 || (tmpcontyp.getCustomcolumn() == 1 && tmpcontyp.getCustomfield() == 0)) ? false : true);
                jobj.put("isdefaultfield", false);
                jobj.put("comboremotemode", tmpcontyp.isFieldOfGivenGSTConfigType(Constants.IsForGSTRuleMapping)?false:moduleid == Constants.Acc_Make_Payment_ModuleId || moduleid == Constants.Acc_Receive_Payment_ModuleId ? true : false);
                if (isReturnAllFields) {
                    jresult.append("data", jobj);
                } else {
                    if (lineitem != 1 && tmpcontyp.getCustomcolumn() == 1 && tmpcontyp.getCustomfield() == 1 && !isAdvanceSearch) {        // dont put line level custom fields
                        if (moduleid == Constants.Account_Statement_ModuleId || moduleid == Constants.Acc_Vendor_ModuleId ||moduleid == Constants.Acc_Product_Master_ModuleId || moduleid == Constants.Acc_Customer_ModuleId || isOpeningTransaction) {         //put line level custom fields only in case of GL Account, Vendor, Customer
                            if(reportid!=Constants.dayEndCollectionReport ||moduleid != Constants.Acc_FixedAssets_Details_ModuleId){
                                jresult.append("data", jobj);
                            }
                    }
                    } else if ((reportid == Constants.CREDIT_NOTE_WITH_ACCOUNT|| reportid==Constants.Sales_By_Service_ProductDetail || reportid==Constants.customerRegistryReport || reportid==Constants.vendorRegistryReport || reportid==Constants.dayEndCollectionReport) && tmpcontyp.getCustomcolumn() == 1) {
                    } else {
                        jresult.append("data", jobj);
                    }
                }
                if ((moduleid == Constants.Acc_Ledger_ModuleId || moduleid == 101) && !isCustomDetailReport) {
                    HashMap<String, Object> reqParams = new HashMap<String, Object>();
                    reqParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.fieldlabel));
                    reqParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, tmpcontyp.getFieldlabel()));
                    reqParams.put("order_by", Arrays.asList("sequence"));
                    reqParams.put("order_type", Arrays.asList("asc"));
                    reqParams.put("isActivated",1);
                    result = accAccountDAOobj.getFieldParams(reqParams);
                    lst = result.getEntityList();
                    if(lst.size()>0){
                        FieldParams  fieldParams = (FieldParams)lst.get(0);
                        if(fieldParams!=null){
                            jobj.put("isfrmpmproduct", true);
                        }else{
                            jobj.put("isfrmpmproduct", false);
                        }
                    }
                }
                
            }
            if (!StringUtil.isNullOrEmpty(commaSepratedModuleids) && !AvoidRedundent) {
                if (reportid != Constants.dayEndCollectionReport && reportid!=Constants.ACC_FIXED_ASSET_DETAILS_REPORTID && reportid!=Constants.ACC_FIXED_DISPOSED_ASSET_REPORTID 
                    && reportid!=Constants.ACC_FIXED_DEPRECIATION_DETAILS_REPORTID) {
                    requestParams.clear();
                    requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    requestParams.put(Constants.filter_values, Arrays.asList(companyid, 30));
                    requestParams.put("order_by", Arrays.asList("sequence"));
                    requestParams.put("order_type", Arrays.asList("asc"));
                    requestParams.put("isActivated", 1);
                    result = accAccountDAOobj.getFieldParams(requestParams);
                    lst = result.getEntityList();
                    colcount += lst.size();
                    boolean isForProductMasterOnly = true;
                    jresult = getProductFieldParams(lst, jresult, isForProductMasterOnly);
                }
            }
            
            if (moduleid == Constants.Acc_FixedAssets_Details_ModuleId && reportid!=Constants.ACC_FIXED_ASSET_SUMMARY_REPORTID) {
                    requestParams.clear();
                    requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    requestParams.put(Constants.filter_values, Arrays.asList(companyid, 121));
                    requestParams.put("order_by", Arrays.asList("sequence"));
                    requestParams.put("order_type", Arrays.asList("asc"));
                    requestParams.put("isActivated", 1);
                    requestParams.put("iscustomfield", 0);
                    requestParams.put("linelevelfields", linelevelfields);
                    result = accAccountDAOobj.getFieldParams(requestParams);
                    lst = result.getEntityList();
                    colcount += lst.size();
                    boolean isForProductMasterOnly = false;
                    if(!linelevelfields){
                        jresult = new JSONObject();
                    }
                    jresult = getProductFieldParams(lst, jresult, isForProductMasterOnly);
                }
            
            
            if (isAdvanceSearch) { //only for Advance serch case in Report List 
                if(customerCustomFieldFlag){
                    //Get custom fields for Customer module
                    HashMap<String, Object> reqParams = new HashMap<String, Object>();
                    reqParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    reqParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Customer_ModuleId));
                    reqParams.put("order_by", Arrays.asList("sequence"));
                    reqParams.put("order_type", Arrays.asList("asc"));
                    reqParams.put("isActivated", 1);
                    getAdvanceSearchColumnJSON(reqParams, colcount, jresult, Constants.Acc_Customer_ModuleId);
                    if (reqParams.containsKey("colcount")) {
                        colcount = reqParams.get("colcount") != null ? Integer.parseInt(reqParams.get("colcount").toString()) : colcount;
                    }
                }
                if(vendorCustomFieldFlag){
                    //Get custom fields for Vendor module
                    HashMap<String, Object> reqParams = new HashMap<String, Object>();
                    reqParams.clear();
                    reqParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    reqParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Vendor_ModuleId));
                    reqParams.put("order_by", Arrays.asList("sequence"));
                    reqParams.put("order_type", Arrays.asList("asc"));
                    reqParams.put("isActivated", 1);
                    getAdvanceSearchColumnJSON(reqParams, colcount, jresult, Constants.Acc_Vendor_ModuleId);
                    if (reqParams.containsKey("colcount")) {
                        colcount = reqParams.get("colcount") != null ? Integer.parseInt(reqParams.get("colcount").toString()) : colcount;
                    }
                }
                /*
                Fetch Invoice Custom Fields in Payment
                */
                if (moduleid == Constants.Acc_Make_Payment_ModuleId) {
                    HashMap<String, Object> reqParams = new HashMap<String, Object>();
                    reqParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                    reqParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Vendor_Invoice_ModuleId, 0));
                    reqParams.put("order_by", Arrays.asList("sequence"));
                    reqParams.put("order_type", Arrays.asList("asc"));
                    reqParams.put("isActivated", 1);
                    getAdvanceSearchColumnJSON(reqParams, colcount, jresult, Constants.Acc_Vendor_Invoice_ModuleId);
                } else if (moduleid == Constants.Acc_Receive_Payment_ModuleId) {
                    HashMap<String, Object> reqParams = new HashMap<String, Object>();
                    reqParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                    reqParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Invoice_ModuleId, 0));
                    reqParams.put("order_by", Arrays.asList("sequence"));
                    reqParams.put("order_type", Arrays.asList("asc"));
                    reqParams.put("isActivated", 1);
                    getAdvanceSearchColumnJSON(reqParams, colcount, jresult, Constants.Acc_Invoice_ModuleId);
                }
                //Get custom fields for Product & Services module i.e. Related Modules
                if (!AvoidRedundent && !linelevelfields && !globallevelfields) {
                    if (reportid != Constants.dayEndCollectionReport||moduleid != Constants.Acc_FixedAssets_Details_ModuleId) {
                        HashMap<String, Object> reqParams = new HashMap<String, Object>();
                        if (moduleid != 30 && !isAssetModule(moduleid)) {  // dont put product custom field in product report and in asset modules itself
                            reqParams.clear();
                            reqParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                            reqParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId));
                            reqParams.put("order_by", Arrays.asList("sequence"));
                            reqParams.put("order_type", Arrays.asList("asc"));
                            reqParams.put("isActivated", 1);
                            reqParams.put("relatedmoduleid", moduleid);
                            getAdvanceSearchColumnJSON(reqParams, colcount, jresult, Constants.Acc_Product_Master_ModuleId);

                            // Get Custom fields for Product Master only
                            reqParams.clear();
                            reqParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                            reqParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId));
                            reqParams.put("order_by", Arrays.asList("sequence"));
                            reqParams.put("order_type", Arrays.asList("asc"));
                            reqParams.put("isActivated", 1);
                            reqParams.put("isForProductMasterOnly", true);
                            getAdvanceSearchColumnJSON(reqParams, colcount, jresult, Constants.Acc_Product_Master_ModuleId);
                        }
                    }
                }
                if (!ignoreDefaultFields) {
                    /*
                    Replace moduleid for masters
                    */
                    module=replaceMasterModuleId(module);
                    HashMap<String, Object> map = new HashMap<String, Object>();
                    map.put(Constants.filter_names, Arrays.asList("module", "allowAdvanceSearch"));
                    map.put(Constants.filter_values, Arrays.asList(module, true));
                    map.put("order_by", Arrays.asList("defaultHeader"));
                    map.put("order_type", Arrays.asList("asc"));
                    if(module.equals(String.valueOf(Constants.Acc_Invoice_ModuleId)) && !StringUtil.isNullOrEmpty(request.getParameter("isCustomReportBuilder")))
                    {
                        map.put("inDefault","defaultHeader");
                        String dHeader="'"+Constants.PRODUCTCATEGORY+"' , '"+Constants.CUSTOMER_CATEGORY_MODULE_NAME+"'";
                        map.put("defaultHeader",dHeader);
                    }
                    KwlReturnObject headerResult = accAccountDAOobj.getDefaultHeaders(map);
                    List<DefaultHeader> headers = headerResult.getEntityList();
                    colcount+=headers.size();
                    if (headers.size() > 0) {
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("fieldid", "NA");
                        jSONObject.put("fieldlabel", "----------[Other Fields]----------");
                        jresult.append("data", jSONObject);
                        colcount++;
                    }
                
                    for (DefaultHeader header : headers) { //these are header of main module
                        JSONObject jobj = new JSONObject();
                        jobj.put("fieldid", header.getId());
                        jobj.put("fieldtype", header.getXtype()!=null?Integer.parseInt(header.getXtype()):1); //TODO advance serch on combo,number,date fields
                        jobj.put("fieldname", header.getDbcolumnname());
                        jobj.put("fieldlabel", header.getDefaultHeader());
                        jobj.put("iscustomcolumn", false);
                        jobj.put("iscustomfield", false);
                        jobj.put("isdefaultfield", true);
                        jobj.put("moduleid", header.getModule().getId());
                        jobj.put("modulename", header.getModule().getModuleName());
                        jresult.append("data", jobj);
                    }
                    /*
                    Provide Advance Search on Sales Person in Payment Module
                    */
                    if (moduleid == Constants.Acc_Make_Payment_ModuleId) {
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("fieldid", "NA");
                        jSONObject.put("fieldlabel", "----------[Other Fields/Vendor Invoice]----------");
                        jresult.append("data", jSONObject);
                        colcount++;
                        JSONObject jobj = new JSONObject();
                        jobj.put("fieldid", "1234");
                        jobj.put("fieldtype", 4); 
                        jobj.put("fieldname", "agent");
                        jobj.put("fieldlabel", "Agent");
                        jobj.put("iscustomcolumn", false);
                        jobj.put("iscustomfield", false);
                        jobj.put("isdefaultfield", true);
                        jobj.put("moduleid", Constants.Acc_Make_Payment_ModuleId);
                        jobj.put("modulename", "Vendor Invoice");
                        colcount++;
                        jresult.append("data", jobj);
                    } else if (moduleid == Constants.Acc_Receive_Payment_ModuleId) {
                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("fieldid", "NA");
                        jSONObject.put("fieldlabel", "----------[Other Fields/Customer Invoice]----------");
                        jresult.append("data", jSONObject);
                        colcount++;
                        JSONObject jobj = new JSONObject();
                        jobj.put("fieldid", "1234");
                        jobj.put("fieldtype", 4); 
                        jobj.put("fieldname", "agent");
                        jobj.put("fieldlabel", "Sales Person");
                        jobj.put("iscustomcolumn", false);
                        jobj.put("iscustomfield", false);
                        jobj.put("isdefaultfield", true);
                        jobj.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                        jobj.put("modulename", "Customer Invoice");
                        jresult.append("data", jobj);
                        colcount++;
                    }
                    KwlReturnObject joinModuleResult = accAccountDAOobj.getDefaultHeadersModuleJoinReference(module);
                    List<DefaultHeaderModuleJoinReference> joinReferences = joinModuleResult.getEntityList();
                    for (DefaultHeaderModuleJoinReference dhmjr : joinReferences) {
                        String refMoule = dhmjr.getRefModule();
                        HashMap<String, Object> map1 = new HashMap<String, Object>();
                        if(isAdvanceSearch && moduleid==Constants.Acc_Delivery_Order_ModuleId){
                            map1.put(Constants.filter_names, Arrays.asList("module", "allowAdvanceSearch", "INxtype", "NOTINdefaultHeader"));
                            map1.put(Constants.filter_values, Arrays.asList(refMoule, true, "'1','4'", "'"+Constants.SALES_PERSON_LABEL+"'"));
                        }else{
                            map1.put(Constants.filter_names, Arrays.asList("module", "allowAdvanceSearch", "INxtype"));
                            map1.put(Constants.filter_values, Arrays.asList(refMoule, true, "'1','4'"));
                        }
                        KwlReturnObject headerResult1 = accAccountDAOobj.getDefaultHeaders(map1);
                        List<DefaultHeader> headers1 = headerResult1.getEntityList();
                        colcount+=headers1.size();
                        for (DefaultHeader header : headers1) { //these are header of reference module
                            JSONObject jobj = new JSONObject();
                            jobj.put("fieldid", header.getId());
                            jobj.put("fieldtype", header.getXtype()!=null?Integer.parseInt(header.getXtype()):1); //TODO advance serch on combo,number,date fields
                            jobj.put("fieldname", header.getDbcolumnname());
                            jobj.put("fieldlabel", header.getDefaultHeader());
                            jobj.put("iscustomcolumn", false);
                            jobj.put("iscustomfield", false);
                            jobj.put("isdefaultfield", true);
                            jobj.put("moduleid", header.getModule().getId());
                            jobj.put("modulename", header.getModule().getModuleName());
    //                        jresult.append("data", jobj);
                        }
                    }
                }
                /**
                 * Load address fields for advance search where isAddressFieldSearch flag is set to true
                 */
                if (isAddressFieldSearch) {
                    JSONObject job = new JSONObject();
                    job.put("fieldid", "NA");
                    job.put("fieldlabel", "---------------[Address Fields]--------------");
                    jresult.append("data", job);
                    String addressfields="Address,City,State,Country,Postal Code";
                    String fielids="address,city,state,country,postalcode";
                    String addressfieldsarr[]=addressfields.split(",");
                    String fielidsarr[]=fielids.split(",");
                    for (int i=0;i<addressfieldsarr.length;i++) { 
                        JSONObject jobj = new JSONObject();
                        jobj.put("fieldid", fielidsarr[i]);
                        jobj.put("fieldtype", 1); 
                        jobj.put("fieldname", "Address_"+addressfieldsarr[i]);
                        jobj.put("fieldlabel", addressfieldsarr[i]);
                        jobj.put("iscustomfield", false);
                        jobj.put("isdefaultfield", false);
                        jobj.put("iscustomcolumn", false);
                        jobj.put("modulename", "Address_Fields");
                        jresult.append("data", jobj);
                    }
                }
        
                /**
                 * For adding Text type Range filters in Advance Search
                 */
                Map reqParams = new HashMap();
                if (reportid != -1) {
                    reqParams.put("reportOrModuleId", reportid);
                } else if (moduleid != -1) {
                    reqParams.put("reportOrModuleId", moduleid);
                }
                KwlReturnObject rangeTextFilters = accAccountDAOobj.getTextRangeFilterFields(reqParams);
                if (rangeTextFilters != null) {
                    List<RangeTextFiltersForAdvanceSearch> rangeTextFiltersList = rangeTextFilters.getEntityList();
                    if (rangeTextFiltersList != null && rangeTextFiltersList.size() > 0) {

                        JSONObject job = new JSONObject();
                        job.put("fieldid", "NA");
                        job.put("fieldlabel", "---------------[Master Module Fields]--------------");
                        jresult.append("data", job);
                        for (RangeTextFiltersForAdvanceSearch rtffas : rangeTextFiltersList) {
                            JSONObject jobj = new JSONObject();
                            jobj.put("fieldid", rtffas.getDefaultheaderid());
                            jobj.put("fieldtype", rtffas.getFieldtype()); //TODO advance serch on combo,number,date fields
                            jobj.put("fieldname", rtffas.getFieldname());
                            jobj.put("fieldlabel", rtffas.getFieldlabel());
                            jobj.put("iscustomcolumn", false);
                            jobj.put("iscustomfield", false);
                            jobj.put("isdefaultfield", true);
                            jobj.put("moduleid", rtffas.getModuleid());
                            jobj.put("modulename",rtffas.getModulename());
                            jobj.put("isRangeSearchField", true);
                            jresult.append("data", jobj);
                        }
                    }
                }
            }
//            if(jresult.has("data")){
//                JSONArray jArr = sortJsonArrayOnSequence(jresult.getJSONArray("data"));
//                jresult.put("data", jArr);
//            }            
            
            if (colcount == 0) {
                jresult.put("data", new com.krawler.utils.json.JSONArray());
            }
            jresult.put("valid", true);
        } catch (ParseException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
        }finally {
            try {
                if (result == null) {
                       jresult.put("success", false);
                } else {
                    jresult.put("success", result.isSuccessFlag());
                } 
                jresult.put("msg", "");
            } catch (JSONException ex) {
                Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jresult.toString());
    }
    /**
     *
     * @param fp
     * @return
     * @Desc : Function to decide which field should be hidden in transaction
     * form
     * @throws ServiceException
     */
    private boolean isFieldHiddenForGST(FieldParams fp) throws ServiceException, JSONException {
        String companyid = fp.getCompanyid();
        HashMap<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("id", companyid);
        Object columnPref = kwlCommonTablesDAOObj.getRequestedObjectFieldsInCollection(ExtraCompanyPreferences.class, new String[]{"columnPref"}, paramsMap);
        if (fp.isFieldOfGivenGSTConfigType(Constants.IsForGSTRuleMapping) && !(fp.getModuleid()==Constants.GSTModule)) {
            return true;
        } else if (fp.isFieldOfGivenGSTConfigType(Constants.isformultientity)) {
            KwlReturnObject kwlReturnObject = accAccountDAOobj.getFieldComboDatabyFieldID(fp.getId(), fp.getCompanyid());
            if (kwlReturnObject.getEntityList().size() <= 1) {
                return true;
            } else {
                return false;
            }
            /*
             * Code for hide/show customfields,dimensions related to E-way bill payment in India GST ERM-1108
             */
        } else if (fp.isFieldOfGivenGSTConfigType(Constants.isEWayRelatedFields)) { // returns EWayGSTConfigType=8,if true
            boolean isWayActivated = false;
            if (columnPref != null) {
                JSONArray columnPrefJObjArr = new JSONArray(columnPref.toString());
                isWayActivated = columnPrefJObjArr.getJSONObject(0).optBoolean("activateEWayBill", false);
            }
            /*
             * In company preferences ,If E-way is not activated then returns true i.e. hide field
             */
            if (!isWayActivated) {
                return true;
            } else {
                return false;
            }

        } else {
            return false;
        }
    }
    
    public String replaceMasterModuleId(String module) {
        if (module.equalsIgnoreCase("" + Constants.Acc_Vendor_ModuleId)) {
            return  Constants.Vendor_MODULE_UUID;
        }
        if (module.equalsIgnoreCase("" + Constants.Acc_Customer_ModuleId)) {
            return Constants.CUSTOMER_MODULE_UUID;
        }
        if (module.equalsIgnoreCase("" + Constants.Account_Statement_ModuleId) || module.equalsIgnoreCase("" + Constants.Acc_Ledger_ModuleId)) {
            return Constants.Account_ModuleId;
        }
        return module;
    }
    public boolean isMRPModule(int moduleid) {
        if (moduleid == Constants.Labour_Master || moduleid == Constants.MRP_WORK_CENTRE_MODULEID || moduleid == Constants.MRP_Machine_Management_ModuleId || 
                moduleid == Constants.MRP_WORK_ORDER_MODULEID || moduleid == Constants.MRP_Contract || moduleid == Constants.MRP_RouteCode || moduleid == Constants.MRP_JOB_WORK_MODULEID
                || moduleid==Constants.VENDOR_JOB_WORKORDER_MODULEID || moduleid==Constants.JOB_WORK_OUT_ORDER_MODULEID || moduleid==Constants.MRP_JOB_WORK_IN_MODULEID || moduleid==Constants.Acc_SecurityGateEntry_ModuleId || moduleid==Constants.Acc_Multi_Entity_Dimension_MODULEID) {
            return true;
        }
        return false;
    }

    public boolean isAssetModule(int moduleid) {
        if (moduleid == Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId || moduleid == Constants.Acc_FixedAssets_RFQ_ModuleId
                || moduleid == Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId || moduleid == Constants.Acc_FixedAssets_Purchase_Order_ModuleId
                || moduleid == Constants.Acc_FixedAssets_Purchase_Return_ModuleId || moduleid == Constants.Acc_FixedAssets_Sales_Return_ModuleId
                || moduleid == Constants.Acc_FixedAssets_DisposalInvoice_ModuleId || moduleid == Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId
                || moduleid == Constants.Acc_FixedAssets_DeliveryOrder_ModuleId || moduleid == Constants.Acc_FixedAssets_GoodsReceipt_ModuleId) {
            return true;
        } else {
            return false;
        }
    }
    public void getAdvanceSearchColumnJSON(HashMap<String, Object> reqParams, int colcount, JSONObject jresult, int moduleid){
        KwlReturnObject result = null;
        JSONObject jSONObject = new JSONObject();
        try {
            result = accAccountDAOobj.getFieldParams(reqParams);
            
            if (Constants.Acc_Product_Master_ModuleId == moduleid) {
                result = accAccountDAOobj.getFieldParams(reqParams);
                List lst = result.getEntityList();
                boolean isForProductMasterOnly = false;
                /*
                 isForProductMasterOnly  = Used for Fields which are present for Product Master module Not for Other Product and Services modules i.e. Related modules
        
                 */
                if (reqParams.containsKey("isForProductMasterOnly")) {
                    isForProductMasterOnly = reqParams.get("isForProductMasterOnly") != null ? Boolean.parseBoolean(reqParams.get("isForProductMasterOnly").toString()) : false;
                }
                if (lst.size() > 0) {
                    if (isForProductMasterOnly) {
                        int Default = Constants.Only_ProductMaster_ModuleId;
                        getModuleNameForAdvanceSearch(colcount, jresult, Default);         // 1000 = Default i.e. Product Master
                        colcount += lst.size();
                        jresult = getProductFieldParams(lst, jresult, isForProductMasterOnly);
                    } else {
                        getModuleNameForAdvanceSearch(colcount, jresult, moduleid);
                        colcount += lst.size();
                        jresult = getProductFieldParams(lst, jresult, isForProductMasterOnly);
                    }
                }
            }else{
                List<FieldParams> fieldparams = result.getEntityList();
                colcount += fieldparams.size();
                if (fieldparams.size() > 0) {
                    getModuleNameForAdvanceSearch(colcount, jresult, moduleid);
                }
                for (FieldParams fieldParams : fieldparams) {
                    JSONObject jobj = new JSONObject();
                    jobj.put("fieldid", fieldParams.getId());
                    jobj.put("fieldtype", fieldParams.getFieldtype());
                    jobj.put("fieldname", fieldParams.getFieldname());
                    jobj.put("fieldlabel", fieldParams.getFieldlabel());
                    jobj.put("iscustomcolumn", fieldParams.getCustomcolumn() == 0 ? false : true);
                    jobj.put("iscustomfield", fieldParams.getCustomfield() == 1 ? true : false);
                    jobj.put("isdefaultfield", false);
                    jobj.put("moduleid", fieldParams.getModuleid());
                    jobj.put("modulename", getModuleName(fieldParams.getModuleid()));
                    jobj.put("refcolumn_number", Constants.Custom_Column_Prefix + fieldParams.getRefcolnum());
                    jobj.put("column_number", Constants.Custom_Column_Prefix + fieldParams.getColnum());
                    jresult.append("data", jobj);
                }
            }
            reqParams.put("colcount", colcount);
        } catch (JSONException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void getModuleNameForAdvanceSearch(int colcount, JSONObject jresult, int moduleid){
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("fieldid", "NA");
            jSONObject.put("fieldlabel", !StringUtil.isNullOrEmpty(getModuleName(moduleid)) ? "--------["+getModuleName(moduleid)+" Custom Fields]--------" : "--------[Dimensions]--------");
            jresult.append("data", jSONObject);
            colcount++;
        } catch (JSONException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static JSONArray sortJsonArrayOnSequence(JSONArray array) throws JSONException {
        List<JSONObject> jsons = new ArrayList<JSONObject>();
        try {
            for (int i = 0; i < array.length(); i++) {
                jsons.add(array.getJSONObject(i));
            }
            Collections.sort(jsons, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject lhs, JSONObject rhs) {
                    String lid = "", rid = "";
                    boolean iscustomfield = false;
                    try {
                        //ERP-2503 : Order Dimensions in the entry screen by dimension name
                        //ERP-12664 : Order Custom Fields in the entry screen by sequence number
                        iscustomfield = Boolean.parseBoolean(lhs.getString("iscustomfield"));
                        if(iscustomfield && lhs.has("sequence")){
                            lid = lhs.getString("sequence");
                            rid = rhs.getString("sequence");
                        }
                    } catch (JSONException ex) {
                        Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return lid.compareTo(rid);
                }
            });
        } catch (JSONException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
        }
        return new JSONArray(jsons);
    }    
    
    public ModelAndView getGlobalCustomDateFields(HttpServletRequest request, HttpServletResponse response) {
        KwlReturnObject result = null;
        JSONObject jresult = new JSONObject();
        int module = Integer.parseInt(request.getParameter(Constants.moduleid));
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        try {
            if(module==Constants.Acc_Invoice_ModuleId){
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.dueDate)));
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Invoice_Date)));
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Email_Button_From_Report))); //ERP-13603
            }else if(module==Constants.Acc_Vendor_Invoice_ModuleId){
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.dueDate)));
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Invoice_Date)));
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Email_Button_From_Report))); //ERP-13603
            }else if(module==Constants.Acc_GENERAL_LEDGER_ModuleId){
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.JE_Date)));
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Email_Button_From_Report))); //ERP-13603
            }else if(module==Constants.Acc_Sales_Order_ModuleId){
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.SO_Date)));
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.ON_APPROVAL_EMAIL)));
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.ON_REJECTION_EMAIL)));
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Email_Button_From_Report))); //SDP-1293
            }else if(module==Constants.Acc_Purchase_Order_ModuleId){
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.PO_Date)));
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Email_Button_From_Report))); //SDP-1293
            }else if(module==Constants.Acc_Delivery_Order_ModuleId){
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.DO_Date)));
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.DOEXp_Date)));
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Email_Button_From_Report)));
            }else if(module==Constants.Acc_Goods_Receipt_ModuleId){
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.GRO_Date)));
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.GROEXp_Date)));
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.GR_DO_Sr_Check_Date)));
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Email_Button_From_Report)));
           } else if(module==Constants.Acc_Sales_Return_ModuleId){
               jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.SR_Date)));
               jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Email_Button_From_Report)));
            } else if(module==Constants.Acc_Purchase_Return_ModuleId){
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.PR_Date)));
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Email_Button_From_Report)));
            } else if(module==Constants.Acc_Customer_ModuleId){
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.CUST_CREATION_Date)));
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Email_Button_From_Report))); 
            } else if(module==Constants.Acc_Vendor_ModuleId){
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.VEND_CREATION_Date)));
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.VEND_Self_Billed_Approval_Expiry_Date)));
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Email_Button_From_Report))); 
            } else if(module==Constants.Acc_Product_Master_ModuleId){
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Product_Purchase_Date)));
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Product_Expiry_Date)));
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Product_QA_Inspection_Rejection))); 
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Product_QA_Inspection_Approval))); 
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Email_Button_From_Report))); //ERP-13603
            } else if(module==Constants.Acc_Contract_Order_ModuleId){
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.CONTRACT_EXPIRY_DATE)));
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Email_Button_From_Report)));  //ERP-13603
            } else if(module==Constants.Asset_Maintenance_ModuleId){
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Asset_Schedule_Start_Date)));
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Asset_Schedule_End_Date)));
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Email_Button_From_Report))); //ERP-13603
            }else if(module==Constants.Account_Statement_ModuleId){
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Email_Button_From_Report))); 
            }else if(module==Constants.CONSIGNMENT_SALES_MODULE){
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentSales_Request_Creation))); 
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentSales_Request_Edition))); 
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentSales_Request_Approval))); 
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentSales_DO_Creation))); 
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentSales_Return_Creation)));
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentSales_DueDate_Passed)));
            }else if(module==Constants.CONSIGNMENT_PURCHASE_MODULE){
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentPurchase_Request_Creation))); 
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentPurchase_Request_Edition))); 
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentPurchase_GR_Creation))); 
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentPurchase_Invoice_Creation))); 
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.ConsignmentPurchase_Return_Creation))); 
            }else if(module==Constants.Acc_Make_Payment_ModuleId){//For Make Payment
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Email_Button_From_Report))); 
            } else if(module==Constants.Acc_Receive_Payment_ModuleId){//For Receipt Payment
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Email_Button_From_Report))); 
            } else if(module==Constants.Acc_Customer_Quotation_ModuleId){//For Customer Quotation
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Email_Button_From_Report)));
            }  else if(module==Constants.Acc_Vendor_Quotation_ModuleId){//For Vendor Quotation
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Email_Button_From_Report)));
            } else if(module==Constants.Acc_Purchase_Requisition_ModuleId){ //For purchase requisition
                jresult.append("data", new JSONObject(Constants.staticGlobalDateFields.get(Constants.Email_Button_From_Report)));
            }            
            
            /*
             *  get custom date field only
             */
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Integer colcount = 1;
            requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, "fieldtype"));
            requestParams.put(Constants.filter_values, Arrays.asList(companyid, module, Constants.FIELDID_DATE));
            result = accAccountDAOobj.getFieldParams(requestParams);
            List lst = result.getEntityList();
            colcount = lst.size();
            Iterator ite = lst.iterator();
            while (ite.hasNext()) {
                FieldParams tmpcontyp = null;
                tmpcontyp = (FieldParams) ite.next();
                JSONObject jobj = new JSONObject();
                jobj.put("fieldlabel", tmpcontyp.getFieldlabel());
                jobj.put("fieldid", tmpcontyp.getId());
                jobj.put("islineitem", tmpcontyp.getCustomcolumn()==1 ? true : false);
                jresult.append("data", jobj);
            }
            
        } catch (SessionExpiredException ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jresult.put("success", result.isSuccessFlag());
                jresult.put("msg", "");
            } catch (JSONException ex) {
                Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jresult.toString());
    }
    
public Object getProductCustomFieldValue(String fieldId, String productId, String companyId, Date transactionDate) {
        Object returnObject = null;
        try {
            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
            customrequestParams.put("fieldId", fieldId);
            customrequestParams.put("productId", productId);
            customrequestParams.put("companyId", companyId);
            customrequestParams.put("transactionDate", transactionDate);
            KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(FieldParams.class.getName(), fieldId);
            FieldParams fieldParams = (FieldParams) custumObjresult.getEntityList().get(0);
            if (fieldParams != null && fieldParams.getFieldtype() == 1 || fieldParams.getFieldtype() == 2) {
                KwlReturnObject result = accProductObj.getProductCustomFieldValue(customrequestParams);

                List list = result.getEntityList();
                Iterator itr = list.iterator();
                if (itr.hasNext()) {
                    returnObject = itr.next();
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
        } catch(Exception e){
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, e);
        }
        return returnObject;
    }


    public ModelAndView getTransactionFormFields(HttpServletRequest request, HttpServletResponse response) {
//        KwlReturnObject result = null;
//        KwlReturnObject resultMapping = null;
//        JSONObject jresult = new JSONObject();
//        try {
//            String moduleId = request.getParameter(Constants.moduleid);
//            String reportId = request.getParameter("reportId") != null ? request.getParameter("reportId") : "";
//            String companyid = sessionHandlerImpl.getCompanyid(request);
//            String userId = sessionHandlerImpl.getUserid(request);
//            
//            boolean isFormField = true; 
//
//            HashMap<String, Object> requestParams = new HashMap<String, Object>();
//
//            if (!StringUtil.isNullOrEmpty(moduleId)) {
//                requestParams.put(Constants.filter_names, Arrays.asList(Constants.company, Constants.user_userID, Constants.moduleid,"formField"));
//                requestParams.put(Constants.filter_values, Arrays.asList(companyid, userId, Integer.parseInt(moduleId),isFormField));
//            }
//            // Get Custom Fields
//            resultMapping = accAccountDAOobj.getCustomizeReportMapping(requestParams);
//            List<CustomizeReportMapping> defaultlstMapping = resultMapping.getEntityList();
//            Set<String> customizeReportMappingSet = new HashSet();
//            for (CustomizeReportMapping customizeReportMapping : defaultlstMapping) {
//                if (customizeReportMapping.isHidden()) {
//                    customizeReportMappingSet.add(customizeReportMapping.getDataIndex());
//                }
//            }
//
//            requestParams.clear();
//            requestParams.put("reportId", reportId);
//            requestParams.put("moduleId", moduleId);
//            requestParams.put("companyid", companyid);
//            requestParams.put("userId", userId);
//            requestParams.put("isFormField", true);
//            result = accAccountDAOobj.getCustomizeReportHeader(requestParams);
//            List<CustomizeReportHeader> defaultlst = result.getEntityList();
//
//
//            for (CustomizeReportHeader customizeReportHeader : defaultlst) {
//                JSONObject jobj = new JSONObject();
//                jobj.put("id", customizeReportHeader.getId());
//                jobj.put("fieldname", customizeReportHeader.getDataHeader());
//                jobj.put("isFormField", customizeReportHeader.isFormField());
//                jobj.put("isManadatoryField", customizeReportHeader.isManadatoryField());
//                jobj.put("fieldDataIndex", customizeReportHeader.getDataIndex());
//                jobj.put("columntype", "Default Field(s)");
//                if (customizeReportMappingSet.contains(customizeReportHeader.getDataIndex())) {
//                    jobj.put("hidecol", true);
//                } else {
//                    jobj.put("hidecol", false);
//                }
//
//                jresult.append("data", jobj);
//            }
//            jresult.put("count", jresult.getJSONArray("data").length());
//        } catch (SessionExpiredException ex) {
//            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
//        } catch (JSONException ex) {
//            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
//        } catch(Exception ex){
//            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
//        } finally {
//            try {
//                jresult.put("success", result.isSuccessFlag());
//                jresult.put("msg", "");
//            } catch (JSONException ex) {
//                Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
//            }
//        }
        return accAccountService.getTransactionFormFields(request, response);
    }

    public ModelAndView getModuleFields(HttpServletRequest request, HttpServletResponse response) {
        KwlReturnObject result = null;
        KwlReturnObject resultMapping = null;
        JSONObject jresult = new JSONObject();
        try {
            String moduleId = request.getParameter(Constants.moduleid);
            String reportId = request.getParameter("reportId")!=null?request.getParameter("reportId"):"";
            boolean isOrderCustOrDimFields = request.getParameter("isOrderCustOrDimFields")!=null?Boolean.parseBoolean(request.getParameter("isOrderCustOrDimFields")):false;
            boolean isFormField = false;
           
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String userId=sessionHandlerImpl.getUserid(request);
            Set<String> customizeReportMappingSet = new HashSet();
            Set<String> productandServiceSet = new HashSet();
            if(!isOrderCustOrDimFields){
                if (!StringUtil.isNullOrEmpty(moduleId)) {
                    requestParams.put(Constants.filter_names, Arrays.asList(Constants.company, Constants.moduleid,Constants.formField,Constants.isForProductandService));
                    requestParams.put(Constants.filter_values, Arrays.asList(companyid, Integer.parseInt(moduleId),isFormField,false));
                }
                // Get Custom Fields
                resultMapping = accAccountDAOobj.getCustomizeReportMapping(requestParams);
                List<CustomizeReportMapping> defaultlstMapping = resultMapping.getEntityList();
                for(CustomizeReportMapping customizeReportMapping:defaultlstMapping){
                    if(customizeReportMapping.isHidden())
                        customizeReportMappingSet.add(customizeReportMapping.getDataIndex());
                }
            }
            
            requestParams.clear();
            if (!StringUtil.isNullOrEmpty(moduleId)) {
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                requestParams.put(Constants.filter_values, Arrays.asList(companyid, Integer.parseInt(moduleId)));
            }
            requestParams.put("order_by", Arrays.asList("sequence"));
            requestParams.put("order_type", Arrays.asList("asc"));
            result = accAccountDAOobj.getFieldParams(requestParams);
            List<FieldParams> customlst = result.getEntityList();
            for(FieldParams tmpcontyp : customlst) {
                if(isOrderCustOrDimFields && tmpcontyp.getCustomfield()==0 && tmpcontyp.getParent()!=null){
                    continue;//For Ordering of Custom/Dimension fields, in case fo Dimension fields no need to set sequence for childs.
                }
                JSONObject jobj = new JSONObject();
                jobj.put("id", tmpcontyp.getId());
                jobj.put("fieldname", tmpcontyp.getFieldlabel());
                if (tmpcontyp.getCustomcolumn() == 1 && Integer.parseInt(moduleId) != Constants.Acc_Customer_ModuleId && Integer.parseInt(moduleId) != Constants.Acc_Vendor_ModuleId) {
                    jobj.put("columntype", messageSource.getMessage("acc.master.configuration.LineItem", null, RequestContextUtils.getLocale(request)));
                }else{
                    if(tmpcontyp.getCustomfield()==1){
                        jobj.put("columntype", messageSource.getMessage("acc.master.configuration.CustomField", null, RequestContextUtils.getLocale(request)));
                    }else{
                        jobj.put("columntype", messageSource.getMessage("acc.master.configuration.DimensionField", null, RequestContextUtils.getLocale(request)));
                    }
                }
                jobj.put("fieldDataIndex", tmpcontyp.getFieldlabel());
                jobj.put("isForProductandServices", false);
                jobj.put("sequence", tmpcontyp.getSequence());
                if(customizeReportMappingSet.contains(tmpcontyp.getFieldlabel())){
                    jobj.put("hidecol", true);
                }else{
                    jobj.put("hidecol", false);
                }
                jresult.append("data", jobj);
            }
            
            requestParams.clear();
            if (!StringUtil.isNullOrEmpty(moduleId)) {
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
                requestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, 0));
                requestParams.put(Constants.relatedModuleId, Integer.parseInt(moduleId));
            }
            requestParams.put("order_by", Arrays.asList("sequence"));
            requestParams.put("order_type", Arrays.asList("asc"));
            result = accAccountDAOobj.getFieldParams(requestParams);
            List<FieldParams> fieldList = result.getEntityList();
            if (fieldList.size() > 0) {
                requestParams.clear();
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.company, Constants.moduleid, "formField", "isForProductandService"));
                requestParams.put(Constants.filter_values, Arrays.asList(companyid, Integer.parseInt(moduleId), isFormField, true));
                // Get Custom Fields
                resultMapping = accAccountDAOobj.getCustomizeReportMapping(requestParams);
                List<CustomizeReportMapping> defaultlstMapping = resultMapping.getEntityList();

                for (CustomizeReportMapping customizeReportMapping : defaultlstMapping) {
                    if (customizeReportMapping.isHidden()) {
                        productandServiceSet.add(customizeReportMapping.getDataIndex());
                    }
                }
            }
            for (FieldParams tmpcontyp : fieldList) {
                if (isOrderCustOrDimFields && tmpcontyp.getCustomfield() == 0 && tmpcontyp.getParent() != null) {
                    continue;//For Ordering of Custom/Dimension fields, in case fo Dimension fields no need to set sequence for childs.
                }
                JSONObject jobj = new JSONObject();
                jobj.put("id", tmpcontyp.getId());
                jobj.put("fieldname", tmpcontyp.getFieldlabel());
//                if (tmpcontyp.getCustomcolumn() == 1 && Integer.parseInt(moduleId) != Constants.Acc_Customer_ModuleId && Integer.parseInt(moduleId) != Constants.Acc_Vendor_ModuleId) {
                jobj.put("columntype", messageSource.getMessage("acc.dimension.module.7", null, RequestContextUtils.getLocale(request)));
                jobj.put("fieldDataIndex", tmpcontyp.getFieldlabel());
                jobj.put("isForProductandServices", true);
                jobj.put("sequence", tmpcontyp.getSequence());
                if (productandServiceSet.contains(tmpcontyp.getFieldlabel())) {
                    jobj.put("hidecol", true);
                } else {
                    jobj.put("hidecol", false);
                }
                jresult.append("data", jobj);
            }

            if(!isOrderCustOrDimFields){
                // Get Default Fields
                requestParams.clear();
                requestParams.put("reportId", reportId);
                requestParams.put("moduleId", moduleId);
                requestParams.put("companyid", companyid);
                requestParams.put("userId", userId);
                requestParams.put("isFormField", false);
                result = accAccountDAOobj.getCustomizeReportHeader(requestParams);
                List<CustomizeReportHeader> defaultlst = result.getEntityList();


                for(CustomizeReportHeader customizeReportHeader : defaultlst) {
                    JSONObject jobj = new JSONObject();
                    jobj.put("id", customizeReportHeader.getId());
                    jobj.put("fieldname", customizeReportHeader.getDataHeader());
                    jobj.put("isFormField", customizeReportHeader.isFormField());
                    jobj.put("isManadatoryField", customizeReportHeader.isManadatoryField());
                    jobj.put("isUserManadatoryField", customizeReportHeader.isUserManadatoryField());
                    jobj.put("fieldDataIndex", customizeReportHeader.getDataIndex());
                    jobj.put("columntype", "Default Field(s)");
                    if(customizeReportMappingSet.contains(customizeReportHeader.getDataIndex()))
                        jobj.put("hidecol", true);
                    else
                        jobj.put("hidecol", false);

                    jresult.append("data", jobj);
                }
            }
            jresult.put("count", jresult.getJSONArray("data").length());
        } catch (SessionExpiredException ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jresult.put("success", result.isSuccessFlag());
                jresult.put("msg", "");
            } catch (JSONException ex) {
                Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jresult.toString());
    }
    
    public ModelAndView saveCustomizedReportFields(HttpServletRequest request, HttpServletResponse response) { 
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = true;
        boolean isCommitEx = false;
        
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            saveCustomizedReportFields(request);
            issuccess = true;
            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                isCommitEx = false;
            }
        } catch (Exception ex) {
            if(!isCommitEx){
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = ""+ex.getMessage();
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
       
    }
    
    
    
    public String saveCustomizedReportFields(HttpServletRequest request) throws ServiceException, JSONException {
        
        Templatepnl templatepnl = null;
        String templateid = request.getParameter("templateid");
        boolean isOrderCustOrDimFields = request.getParameter("isOrderCustOrDimFields")!=null?Boolean.parseBoolean(request.getParameter("isOrderCustOrDimFields")):false;
        try {
            String companyId=sessionHandlerImpl.getCompanyid(request);
            String userId=sessionHandlerImpl.getUserid(request);
             KwlReturnObject result = null;
//            String fieldname = request.getParameter("fieldname");
//            String fieldDataIndex = request.getParameter("fieldDataIndex");
            String data = request.getParameter("data");
            
            JSONArray jSONArray = new JSONArray(data);
            
            String moduleId = request.getParameter(Constants.moduleid);
            String reportId = request.getParameter("reportId")!=null?request.getParameter("reportId"):"";
            boolean isFormField = request.getParameter("isFormField")!=null?Boolean.parseBoolean(request.getParameter("isFormField")):false;
            boolean isLineField = request.getParameter("isLineField")!=null?Boolean.parseBoolean(request.getParameter("isLineField")):false;
            boolean isReportField = request.getParameter("isReportField")!=null?Boolean.parseBoolean(request.getParameter("isReportField")):false;
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("moduleId", moduleId);
            requestParams.put("reportId", reportId);
            requestParams.put("isFormField", isFormField);
            requestParams.put("isLineField", isLineField);
            requestParams.put("isReportField", isReportField);
            requestParams.put("companyId", companyId);
            requestParams.put("userId", userId);
            requestParams.put("data", jSONArray);
            requestParams.put("isOrderCustOrDimFields", isOrderCustOrDimFields);
             result = accAccountDAOobj.saveCustomizedReportFields(requestParams);
             String cid="";
             if (result.getEntityList().size() > 0) {
             CustomizeReportMapping crm = (CustomizeReportMapping) result.getEntityList().get(0);
                cid=crm.getId();
             }
             auditTrailObj.insertAuditLog(AuditAction.ORDERING_CUSTOM_FIELDS_DIMENSION, "User " + sessionHandlerImpl.getUserFullName(request)+" has updated Ordering of Custom/Dimensions fields", request,cid );
            
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE("saveCustomizedReportFields : "+ex.getMessage(), ex);
        }
        return templateid;
    }
    
    
       public ModelAndView getCustomizedReportFields(HttpServletRequest request, HttpServletResponse response) {
        KwlReturnObject result = null;
        JSONObject jresult = new JSONObject();
        try {
            String moduleId = request.getParameter(Constants.moduleid);
            String reportId = request.getParameter("reportId")!=null?request.getParameter("reportId"):"";
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            String companyid = sessionHandlerImpl.getCompanyid(request);
             String userId=sessionHandlerImpl.getUserid(request);
             boolean isLineField=false;
             boolean isFormField = false;
             if(!StringUtil.isNullOrEmpty(request.getParameter("isLineField"))){
                 isLineField=Boolean.parseBoolean(request.getParameter("isLineField"));
             }
             if(!StringUtil.isNullOrEmpty(request.getParameter("isFormField"))){
                 isFormField=Boolean.parseBoolean(request.getParameter("isFormField"));
             }
//            if (!StringUtil.isNullOrEmpty(moduleId)) {
//                requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
//                requestParams.put(Constants.filter_values, Arrays.asList(companyid,Integer.parseInt(moduleId)));
//            }
//            // Get Custom Fields
//            result = accAccountDAOobj.getFieldParams(requestParams);
//            List<FieldParams> customlst = result.getEntityList();
//            for(FieldParams tmpcontyp : customlst) {
//                JSONObject jobj = new JSONObject();
//                jobj.put("id", tmpcontyp.getId());
//                jobj.put("fieldname", tmpcontyp.getFieldname());
//                jobj.put("columntype", "Custom Field");
//                jresult.append("data", jobj);
//            }
            
            // Get Default Fields
            requestParams.clear();
             if (!StringUtil.isNullOrEmpty(moduleId)) {
                requestParams.put(Constants.filter_names, Arrays.asList(Constants.company, Constants.moduleid,"formField"));
                requestParams.put(Constants.filter_values, Arrays.asList(companyid, Integer.parseInt(moduleId),isFormField));
            }
            result = accAccountDAOobj.getCustomizeReportMapping(requestParams);
            List<CustomizeReportMapping> defaultlst = result.getEntityList();
            for(CustomizeReportMapping customizeReportMapping : defaultlst) {
                JSONObject jobj = new JSONObject();
//                jobj.put("id", customizeReportHeader.getId());
                jobj.put("hidecol", customizeReportMapping.isHidden());
                jobj.put("isreadonlycol", customizeReportMapping.isReadOnlyField());
                jobj.put("fieldlabeltext", customizeReportMapping.getFieldLabelText());
                jobj.put("fieldname", customizeReportMapping.getDataHeader());
                jobj.put("fieldDataIndex", customizeReportMapping.getDataIndex());
                jobj.put("isForProductandService", customizeReportMapping.isIsForProductandService());
                jresult.append("data", jobj);
            }
            jresult.put("count", defaultlst.size()>0?jresult.getJSONArray("data").length():0);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jresult.put("success", result.isSuccessFlag());
                jresult.put("msg", "");
            } catch (JSONException ex) {
                Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jresult.toString());
    }
   
    public ModelAndView getCustomizedProductMasterFieldsTOShowAtLineLevel(HttpServletRequest req, HttpServletResponse res) {

        JSONObject jobj = new JSONObject();
        JSONArray jarr = new JSONArray();
        KwlReturnObject result = null;
        try {
            JSONObject tempObj = null;
            String moduelArr[] = req.getParameterValues("moduleArr");
            Map<String, Object> ProductFieldsRequestParams = new HashMap();
            ProductFieldsRequestParams.put("companyid", sessionHandlerImpl.getCompanyid(req));
            for (int i = 0; i < moduelArr.length; i++) {
            ProductFieldsRequestParams.put("moduleid", Integer.parseInt(moduelArr[i]));
                List ll = CommonFunctions.getproductmastersFieldsToShowLineLevel(ProductFieldsRequestParams, accountingHandlerDAOobj);
                jarr = new JSONArray();
                for (Object obj : ll) {
                    DefaultHeader dh = (DefaultHeader) obj;
                    tempObj = new JSONObject();
                    tempObj.put("header", dh.getDefaultHeader());
                    tempObj.put("fieldname", dh.getDataIndex());
                    tempObj.put("fieldtype", 1);

                    jarr.put(tempObj);
                }
                jobj.put(moduelArr[i], jarr);
            }
        } catch (Exception ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView getCustomCombodata(HttpServletRequest request, HttpServletResponse response) {
        KwlReturnObject result = null;
         JSONObject jresult = new JSONObject();
        String fieldid = request.getParameter(FieldConstants.Crm_fieldid);
        String flag = request.getParameter(FieldConstants.Crm_flag);
        String companyid =""; 
        String userlogin="";
        String user="";
        String roleid="";
        boolean isFormPanel=false;
        boolean needNoneValue=true;
        String parentid="";
        String searchString=StringUtil.isNullOrEmpty(request.getParameter("query"))?"":request.getParameter("query");
        if(!StringUtil.isNullOrEmpty(request.getParameter("isFormPanel")))
            isFormPanel =Boolean.parseBoolean(request.getParameter("isFormPanel"));
        if(!StringUtil.isNullOrEmpty(request.getParameter("parentid")))
            parentid =request.getParameter("parentid");
        String jsonview = flag != null ? "jsonView" : "jsonView-ex";
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        try {
            //fetch user specific information from custom combodata ref ERM-91
            userlogin=sessionHandlerImpl.getUserid(request);
            user=sessionHandlerImpl.getUserName(request);
            roleid=sessionHandlerImpl.getRole(request);
            companyid=sessionHandlerImpl.getCompanyid(request);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject(ExtraCompanyPreferences.class.getName(), companyid);
            Integer colcount = 1;
            requestParams.put(Constants.filter_names, Arrays.asList(FieldConstants.Crm_fieldid, FieldConstants.Crm_deleteflag));
            requestParams.put(Constants.filter_values, Arrays.asList(fieldid, 0));
           if(!StringUtil.isNullOrEmpty(request.getParameter("parentid")))  
                  requestParams.put("parentid",parentid);
            if(!StringUtil.isNullOrEmpty(searchString)){
                requestParams.put(Constants.ss,searchString);
                String noneString1="none";
                /*
                 Below code is used to put 'None' value  in Dimension combo or not while searching case
                */
                if (noneString1.indexOf(searchString.toLowerCase()) >= 0) {
                    needNoneValue = true;
                } else {
                    needNoneValue = false;
                }
            }
            ArrayList order_by = new ArrayList();
            ArrayList order_type = new ArrayList();
            order_by.add("itemsequence");
            order_by.add("value");
            order_type.add(" ");
            order_type.add("asc");
            requestParams.put("order_by", order_by);
            requestParams.put("order_type", order_type);
            requestParams.put("roleid",roleid);
            requestParams.put("userid",userlogin);
            requestParams.put("fieldid",fieldid);
            requestParams.put("usersVisibilityFlow",extraCompanyPreferences.isUsersVisibilityFlow());
            requestParams.put("usersspecificinfoFlow",extraCompanyPreferences.isusersspecificinfoFlow());
                result = accAccountDAOobj.getCustomCombodata(requestParams);
            List lst = result.getEntityList();
            colcount = lst.size();
            Iterator ite = lst.iterator();
            if(isFormPanel && needNoneValue ){
                JSONObject jobjTemp = new JSONObject();
                jobjTemp.put(FieldConstants.Crm_id, "1234");
                jobjTemp.put(FieldConstants.Crm_name, "None");
                /**
                 * For INDIA and US don't add None entry in default dimension
                 * FOR US -  State , City , County, Product Tax Class, Entity
                 * FOr INDIA  -  State , Product Tax Class, HSN/SAC Code, Entity
                 * Ticket - ERP-38317
                 */
                if (extraCompanyPreferences != null && extraCompanyPreferences.isIsNewGST()) {
                    Map<String, Object> map = new HashMap<>();
                    Object res = null;
                    map.put(Constants.companyid, companyid);
                    map.put("id", fieldid);
                    res = kwlCommonTablesDAOObj.getRequestedObjectFields(FieldParams.class, new String[]{"GSTConfigType"}, map);
                    int GSTConfigType = res != null ? (Integer) res : 0;
                    if(GSTConfigType == 0){
                        jresult.append(Constants.data, jobjTemp);
                    }
                } else {
                    jresult.append(Constants.data, jobjTemp);
                }
            }
            while (ite.hasNext()) {
                Object[] row = (Object[]) ite.next();
                FieldComboData tmpcontyp = (FieldComboData) row[0];
                JSONObject jobjTemp = new JSONObject();
                jobjTemp.put(FieldConstants.Crm_id, tmpcontyp.getId());
                if (tmpcontyp.isActivatedeactivatedimensionvalue()) {
                    jobjTemp.put("hasAccess", true);
                } else {
                    jobjTemp.put("hasAccess", false);
                }
                jobjTemp.put("activatedeactivatedimension", tmpcontyp.isActivatedeactivatedimensionvalue());
                jobjTemp.put("itemdescription", tmpcontyp.getItemdescription());
                try {
                    //Get distributed opening balance for COA
                    getDistibuitedOpeningBalanceForCOA(request, jobjTemp, tmpcontyp);
                } catch (ServiceException ex) {
                    Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                }
                jobjTemp.put(FieldConstants.Crm_name, tmpcontyp.getValue());
                FieldComboData parentItem = (FieldComboData) row[3];
                if (parentItem != null) {
                    jobjTemp.put("parentid", parentItem.getId());
                    jobjTemp.put("parentname", parentItem.getValue());
                }
                jobjTemp.put("level", row[1]);
                jobjTemp.put("leaf", row[2]);
                

                jresult.append(Constants.data, jobjTemp);
            }
            /**
             * IF No field combo data then add Empty JSOn array
             */
            if (jresult !=null && !jresult.has(Constants.data)) {
                jresult.put(Constants.data, new com.krawler.utils.json.JSONArray());
            }
//            jresult.put("valid", true);
            jresult.put(Constants.RES_success, result.isSuccessFlag());
        } catch (JSONException ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(CommonFnController.class.getName()).log(Level.SEVERE, null, ex);
         }
        return new ModelAndView(jsonview, Constants.model, jresult.toString());
    }
    
    public void getDistibuitedOpeningBalanceForCOA(HttpServletRequest request, JSONObject jobjTemp, FieldComboData tmpcontyp) throws ServiceException {
        String accountid="";
        try{
            String companyid;
            companyid = sessionHandlerImpl.getCompanyid(request);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject(ExtraCompanyPreferences.class.getName(), companyid);
            if(extraCompanyPreferences.isSplitOpeningBalanceAmount() && !StringUtil.isNullOrEmpty(request.getParameter("accountid"))){
                accountid =request.getParameter("accountid");
                Account account = (Account) kwlCommonTablesDAOObj.getClassObject(Account.class.getName(), accountid);
                HashMap<String, Object> requestparams = new HashMap<String, Object>();
                requestparams.put("accountid", accountid);
                requestparams.put("comboid", tmpcontyp.getId());
                KwlReturnObject res = accAccountDAOobj.getDistributedOpeningBalance(requestparams);
                List<Object[]> ll = res.getEntityList();
                if(ll.size()>0){ // Opening Balance is distrributed among the Dimensions
                    for(Object[] obj : ll) {
                        jobjTemp.put("distributedopeningbalanace", Math.abs((Double)obj[1]));
                        jobjTemp.put("field_id", obj[2]);                       
                        double bal = (Double) obj[1];
                        if (bal != 0) {
                            if (bal > 0) {
                                jobjTemp.put("debitType", true);
                            } else {
                                jobjTemp.put("debitType", false);
                            }
                        }else{
                            if(account.getGroup().getNature() ==1 || account.getGroup().getNature() ==2) { // Nature is Asset or Expense
                                jobjTemp.put("debitType", true);
                            }else{ // Nature is Income or Liability
                                jobjTemp.put("debitType", false);
                            }
                        }
                        if(account!=null){
                            jobjTemp.put("currencysymbol", account.getCurrency()!=null ? account.getCurrency().getSymbol() : "");
                        }
                    }
                }else{ // If Opening Balances are not distributed among the Dimensions 
                    if (account != null) {
                        jobjTemp.put("nature", account.getGroup().getNature());
                        jobjTemp.put("currencysymbol", account.getCurrency() != null ? account.getCurrency().getSymbol() : "");
                        if (account.getGroup().getNature() == 1 || account.getGroup().getNature() == 2) { // Nature is Asset or Expense
                            jobjTemp.put("debitType", true);
                        } else { // Nature is Income or Liability
                            jobjTemp.put("debitType", false);
                        }
                    }
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } 
    }

    public ModelAndView editfield(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject resultJson = new JSONObject();
        KwlReturnObject result = null;
        KwlReturnObject updateResponse = null;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        TransactionStatus status = txnManager.getTransaction(def);
        String leadFieldId = "";
        String moduleFieldId = "";
        String modulename = "";

        try {
            ArrayList<String> ll = new ArrayList<String>();
            String fieldlabel = request.getParameter("fieldlabel");
            String masterid = request.getParameter("masterid");

            HashMap<String, Object> requestParam = new HashMap<String, Object>();
            requestParam.put("filter_names", Arrays.asList("id"));
            requestParam.put("filter_values", Arrays.asList(masterid));
            result = accAccountDAOobj.getFieldParams(requestParam);
            String getfieldLabel = ((FieldParams) result.getEntityList().get(0)).getFieldlabel().toString();

            int lineitem = ((FieldParams) result.getEntityList().get(0)).getCustomcolumn();
            Boolean editfield = true;
            String columnExistInModules = Constants.stringInitVal;
            String companyid = ((FieldParams) result.getEntityList().get(0)).getCompanyid().toString();
            Integer moduleid = ((FieldParams) result.getEntityList().get(0)).getModuleid();
            String moduleName = getModuleName(moduleid);

            HashMap<String, Object> dupCheckRequestParams = new HashMap<String, Object>(); //Duplicate name checking for different name
            dupCheckRequestParams.put(Constants.filter_names, Arrays.asList(Constants.fieldlabel, Constants.companyid, Constants.moduleid, Constants.customcolumn));
            dupCheckRequestParams.put(Constants.filter_values, Arrays.asList(fieldlabel, companyid, moduleid, lineitem));

            KwlReturnObject resultDupCheck = accAccountDAOobj.getFieldParams(dupCheckRequestParams);

            if (resultDupCheck.getEntityList().size() > 0) {
                editfield = false;
                columnExistInModules += moduleName + ", ";
            }

            if (getfieldLabel.equals(fieldlabel)) { //for same name in edit header
                editfield = true;
            }

            if (editfield) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                    requestParams.put("id", masterid);
                    requestParams.put("fieldlabel", fieldlabel);
                    updateResponse = accAccountDAOobj.updateCustomfield(requestParams);
                    ll.add(updateResponse.toString());

                txnManager.commit(status);
                resultJson.put("sucess", ll);
//                resultJson.put(Constants.moduleid, moduleArr);
            } else {
                resultJson.put(Constants.RES_success, Constants.RES_msg);
                resultJson.put("title", "Alert");
//                if (columnExistInModules.length() > 0) {
//                    columnExistInModules = columnExistInModules.trim();
//                    columnExistInModules = columnExistInModules.substring(0, columnExistInModules.length() - 1);
//                }
                resultJson.put(Constants.RES_msg, messageSource.getMessage("acc.master.configuration.Cannoteditnew", null, RequestContextUtils.getLocale(request)) + columnExistInModules);
                resultJson.put("duplicateflag", true);
                txnManager.rollback(status);
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }

        return new ModelAndView("jsonView", "model", resultJson.toString());
    }
 public ModelAndView ImportCustomFields(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        try {
            String doAction = request.getParameter("do");
            System.out.println("A(( " + doAction + " start : " + new Date());
            
                JSONObject datajobj = new JSONObject();
            if (doAction.compareToIgnoreCase("getMapCSV") == 0) {
                datajobj = importHandler.getMappingCSVHeader(request);
                JSONArray jSONArray = datajobj.getJSONArray("Header");
                ValidateHeadersCustomFields(jSONArray);
                
                jobj = ImportCustomFields(request, datajobj);
                issuccess = true;
                
                List childCompaniesList = companyDetailsDAOObj.getChildCompanies(sessionHandlerImpl.getCompanyid(request));
                for (Object childObj : childCompaniesList) {
                    try {
                        Object[] childdataOBj = (Object[]) childObj;
                        datajobj.put("propagatetoChildCompanies", true);
                        datajobj.put("childcompanyid", (String) childdataOBj[0]); 
                        datajobj.put("childCompanyName", (String) childdataOBj[1]);
                        ImportCustomFields(request, datajobj);
                    } catch (Exception ex) {
                        Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                    }
                }
            }
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();

            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException e) {
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }

            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
      public void ValidateHeadersCustomFields(JSONArray validateJArray) throws AccountingException, ServiceException {
        try {

            List<String> list = new ArrayList<String>();
            list.add("Field Name");
            list.add("Max Length");
            list.add("Is Essential Field");
            list.add("Field Type");
            list.add("Combo data");
            list.add("Module Name");
            list.add("Is Editable");
            list.add("Send Notification");
            list.add("Notification Days");
            list.add("Field For Project");
            list.add("Field For Task");
            list.add("Custom Field-'Yes'/Dimension-'No'");
            list.add("Custom Column");
            list.add("Related Module");
            list.add("Parent");
            list.add("Default Value");
            
            List<String> fileHeaderList = new ArrayList<String>();

            for (int i = 0; i < validateJArray.length(); i++) {
                String header = validateJArray.getJSONObject(i).getString("header").trim();
                fileHeaderList.add(header);
            }

            for (String manadatoryField : list) {
                if (!fileHeaderList.contains(manadatoryField)) {
                    throw new AccountingException(manadatoryField + " column is not available in file");
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public JSONObject ImportCustomFields(HttpServletRequest request, JSONObject jobj) throws AccountingException, IOException, SessionExpiredException, JSONException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        boolean commitedEx = false;
        boolean issuccess = true;
        String msg = "";
        FileInputStream fileInputStream = null;
        BufferedReader br = null;
        int total = 0, failed = 0;
        String companyid ="";
        String userId = sessionHandlerImpl.getUserid(request);
        String fileName = jobj.getString("name");
        String delimiterType = request.getParameter("delimiterType");
        CsvReader csvReader = null;
        JSONObject returnObj = new JSONObject();

        try {
            String ChildCompanyName="";
            boolean propagateToChildCompanies = false;
            if (jobj.has("propagatetoChildCompanies") && jobj.getBoolean("propagatetoChildCompanies")) {
                propagateToChildCompanies = true;
                companyid = jobj.has("childcompanyid") ? jobj.getString("childcompanyid") : "";
                ChildCompanyName=jobj.has("childCompanyName") ? jobj.getString("childCompanyName") : "";;

            } else {
                companyid = sessionHandlerImpl.getCompanyid(request);
            }
            String dateFormat=null, dateFormatId = request.getParameter("dateFormat");
            if(!StringUtil.isNullOrEmpty(dateFormatId)){
                
                KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);
                
                dateFormat = kdf!=null ? kdf.getJavaForm() : null;
            }
            DateFormat df = new SimpleDateFormat(dateFormat);
            
            fileInputStream = new FileInputStream(jobj.getString("FilePath"));
            //br = new BufferedReader(new InputStreamReader(fileInputStream));
           // String record = "";
            int cont = 0;
            
            StringBuilder failedRecords = new StringBuilder();
            
            JSONArray jSONArray = jobj.getJSONArray("Header");
            List headArrayList = new ArrayList();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                headArrayList.add(jSONObject.get("header"));
            }
            csvReader = new CsvReader(new InputStreamReader(fileInputStream), delimiterType);
            failedRecords.append(createCSVrecord(headArrayList.toArray()) + "\"Error Message\"");
            while (csvReader.readRecord()) {
              if (cont != 0) {
                   
                  String[] recarr = csvReader.getValues();          
                  
                    try {
                         
                           String fieldlabel = recarr[0].trim();
                            if (!StringUtil.isNullOrEmpty(fieldlabel)) {
                                fieldlabel = fieldlabel.replaceAll("\"", "");
                                fieldlabel= fieldlabel.trim();
                                fieldlabel= fieldlabel.replaceAll("&nbsp;"," ");//Relacing HTML Code &nbsp; with corresponding " ". ERP-29949
                            } else {
                                throw new AccountingException("Field Name is not Available");
                            }
                            String maxlength = recarr[1].trim();
                            if (!StringUtil.isNullOrEmpty(maxlength)) {
                                maxlength = maxlength.replaceAll("\"", "");
                                maxlength = maxlength.trim();
                            }
                            String isessential = recarr[2].trim();
                            if (!StringUtil.isNullOrEmpty(isessential)) {
                                isessential = isessential.replaceAll("\"", "");
                                isessential = isessential.trim();
                            } else {
                                throw new AccountingException("Is Essential Field  is not Available");
                            }
                            if(checkForValidation(isessential)){
                                throw new AccountingException("Please give valid  Value - Value should be 'Yes' or 'No' ");
                            }
                            String fieldtype = recarr[3].trim();
                            if (!StringUtil.isNullOrEmpty(fieldtype)) {
                                fieldtype = fieldtype.replaceAll("\"", "");
                                fieldtype = fieldtype.trim();
                            } else {
                                throw new AccountingException("Field Type is not Available");
                            }
                            String combodata = recarr[4].trim();
                            if (!StringUtil.isNullOrEmpty(combodata)) {
                                combodata = combodata.replaceAll("\"", "");
                                combodata = combodata.trim();
                            } 
                            String modulename = recarr[5].trim();
                            if (!StringUtil.isNullOrEmpty(modulename)) {
                                modulename = modulename.replaceAll("\"", "");
                                modulename = modulename.trim();
                            } else {
                                throw new AccountingException("Module Name is not Available");
                            }
                            String iseditable = recarr[6].trim();
                            if (!StringUtil.isNullOrEmpty(iseditable)) {
                                iseditable = iseditable.replaceAll("\"", "");
                                iseditable = iseditable.trim();
                            } else {
                                throw new AccountingException("Is Editable is not Available");
                            }
                            if(checkForValidation(iseditable)){
                                throw new AccountingException("Please give valid  Value - Value should be 'Yes' or 'No' ");
                            }
                            String sendnotification = recarr[7].trim();
                            if (!StringUtil.isNullOrEmpty(sendnotification)) {
                                sendnotification = sendnotification.replaceAll("\"", "");
                                sendnotification=sendnotification.trim();
                            } else {
                                throw new AccountingException("Send Notification is not Available");
                            }
                            if(checkForValidation(sendnotification)){
                                throw new AccountingException("Please give valid  Value - Value should be 'Yes' or 'No' ");
                            }
                            String notificationdays = recarr[8].trim();
                            if (!StringUtil.isNullOrEmpty(notificationdays)) {
                                notificationdays = notificationdays.replaceAll("\"", "");
                                notificationdays = notificationdays.trim();
                                if(!StringUtil.isNullOrEmpty(notificationdays)){
                                    try{
                                            Integer.parseInt(notificationdays);
                                    }catch(Exception ex){
                                            throw new AccountingException("Please give valid Default Value - Value should be numeric");
                                    }
                                }
                            } 
                            String isforproject = recarr[9].trim();
                            if (!StringUtil.isNullOrEmpty(isforproject)) {
                                isforproject = isforproject.replaceAll("\"", "");
                                isforproject = isforproject.trim();
                             } else {
                                throw new AccountingException("Field For Project  is not Available");
                            }
                            if(checkForValidation(isforproject)){
                                throw new AccountingException("Please give valid  Value - Value should be 'Yes' or 'No' ");
                            }
                            String isfortask = recarr[10].trim();
                            if (!StringUtil.isNullOrEmpty(isfortask)) {
                                isfortask = isfortask.replaceAll("\"", "");
                                isfortask = isfortask.trim();
                              } else {
                                throw new AccountingException("Field For Task is not Available");
                            }
                            if(checkForValidation(isfortask)){
                                throw new AccountingException("Please give valid  Value - Value should be 'Yes' or 'No' ");
                            }
                            String iscustomfield = recarr[11].trim();
                            if (!StringUtil.isNullOrEmpty(iscustomfield)) {
                                iscustomfield = iscustomfield.replaceAll("\"", "");
                                iscustomfield = iscustomfield.trim();
                            } else {
                                throw new AccountingException("Custom Field-'Yes'/Dimension-'No' is not Available");
                            }
                            if(checkForValidation(iscustomfield)){
                                throw new AccountingException("Please give valid  Value - Value should be 'Yes' or 'No' ");
                            }
                            String iscustomcolumn = recarr[12].trim();
                            if (!StringUtil.isNullOrEmpty(iscustomcolumn)) {
                                iscustomcolumn = iscustomcolumn.replaceAll("\"", "");
                                iscustomcolumn = iscustomcolumn.trim();
                            } else {
                                throw new AccountingException("Custom Column is not Available");
                            }
                            if(checkForValidation(iscustomcolumn)){
                                throw new AccountingException("Please give valid  Value - Value should be 'Yes' or 'No' ");
                            }
                            
                            fieldtype = getXType(fieldtype);
                            if (StringUtil.isNullOrEmpty(fieldtype)) {
                                throw new AccountingException("Please give valid field type");
                            }
                            if ("12".equalsIgnoreCase(fieldtype) && "Yes".equalsIgnoreCase(isessential)) { // check ckeck list
                                throw new AccountingException("Cannot create Check List as Mandatory field.");
                            }
                            if ("12".equalsIgnoreCase(fieldtype) && "Yes".equalsIgnoreCase(iscustomcolumn)) { // check ckeck list
                                throw new AccountingException("Cannot create Check List as Line Item field.");
                            }
                            
                            String relatedmoduleids = recarr[13].trim();
                            if (!StringUtil.isNullOrEmpty(relatedmoduleids)) {
                                relatedmoduleids = relatedmoduleids.replaceAll("\"", "");
                                relatedmoduleids=relatedmoduleids.trim();
                            } 
                            String parentname = recarr[14].trim();
                            if (!StringUtil.isNullOrEmpty(parentname)) {
                                parentname = parentname.replaceAll("\"", "");
                                parentname = parentname.trim();
                            } else {
                                throw new AccountingException("Parent is not Available");
                            } 
                            String defaultval="";
                            if("Yes".equalsIgnoreCase(isessential)){
                                try{
                                    defaultval = recarr[15].trim();
                                }catch(Exception ex){
                                     throw new AccountingException("Please Give Default Value for Mandatory field");
                                }
                                if (!StringUtil.isNullOrEmpty(defaultval)) {
                                    defaultval = defaultval.replaceAll("\"", "");
                                    defaultval = defaultval.trim();
                                }
                                if(StringUtil.isNullOrEmpty(defaultval))       
                                 throw new AccountingException("Please Give Default Value for Mandatory field");
                            }
                            HashMap<String, Object> requesttoprocess = new HashMap<String, Object>(); 
                            
                            if("1".equalsIgnoreCase(fieldtype)){ // text field
                                if(StringUtil.isNullOrEmpty(maxlength)) {
                                    throw new AccountingException("Max Length  is not Available");
                                }
                                try{
                                    Integer.parseInt(maxlength);
                                }catch(Exception ex){
                                    throw new AccountingException("Please give valid  Value - Value should be numeric");
                                }
                            }
                            if("6".equalsIgnoreCase(fieldtype) && "Yes".equalsIgnoreCase(isessential)){ // check ckeckbox
                                 throw new AccountingException("Cannot create Check box as Mandatory field");
                            }
                            if("Yes".equalsIgnoreCase(iscustomfield) && ("Yes".equalsIgnoreCase(isfortask) || "Yes".equalsIgnoreCase(isforproject))){ // is for task and is for project is only for dimention
                                throw new AccountingException("Cannot use custom field for project or payment milestone");
                            }
                            if("Yes".equalsIgnoreCase(isfortask) && "Yes".equalsIgnoreCase(isforproject)){
                                 throw new AccountingException("Cannot use same dimension  for project and  payment milestone");
                            }
                            if("Yes".equalsIgnoreCase(isfortask) && "No".equalsIgnoreCase(parentname)){
                                 throw new AccountingException("Should have parent for this dimention as it is use for  payment milestone");
                            }
//                            if("Yes".equalsIgnoreCase(iscustomcolumn) && "Yes".equalsIgnoreCase(isessential)){ 
//                                throw new AccountingException("Can not create lineitem as Mandatory field");
//                            }
                            
                            if("Yes".equalsIgnoreCase(isessential)){
                                if(fieldtype.equals("4") || fieldtype.equals("7")   || fieldtype.equals("8") || fieldtype.equals("12")){
                                    String[] combodataarr=combodata.split(";");
                                    boolean check=true;
                                    for(int count=0 ; count < combodataarr.length ; count ++){
                                        if(combodataarr[count].trim().equalsIgnoreCase(defaultval)){
                                            check=false;
                                            break;
                                        }
                                    }
                                    if(check){
                                        throw new AccountingException("Please give valid Default Value - Value should be present in combodata");
                                    }
                                }else if(fieldtype.equals("2")){ // number field
                                    try{
                                        Integer.parseInt(defaultval);
                                    }catch(Exception ex){
                                        throw new AccountingException("Please give valid Default Value - Value should be numeric");
                                    }
                                }else if(fieldtype.equals("3")){ // date field
                                     try{
                                        df.parse(defaultval);
                                    }catch(Exception ex){
                                        throw new AccountingException("Please give valid Default Value - Value should be in selected format");
                                    }
                                }
                            }
                            if(!fieldtype.equals("4") && "No".equalsIgnoreCase(iscustomfield)){
                                 throw new AccountingException("Field type of dimention should be 'Combo Box'");
                            }
                            if("Yes".equalsIgnoreCase(iscustomfield) &&  !"No".equalsIgnoreCase(parentname)){ 
                                throw new AccountingException("Parent-Child functionality is only for dimensions");
                            }
                            requesttoprocess.put("fieldlabel",fieldlabel);
                            requesttoprocess.put("maxlength",maxlength);
                            requesttoprocess.put("isessential",isessential);
                            requesttoprocess.put("fieldType",fieldtype);
                            requesttoprocess.put("iseditable", "Yes".equalsIgnoreCase(iseditable) ? true : false);
                            requesttoprocess.put("sendnotification",sendnotification);
                            requesttoprocess.put("notificationDays",notificationdays);
                            requesttoprocess.put("isforproject",isforproject);
                            requesttoprocess.put("isfortask",isfortask);
                            requesttoprocess.put("iscustomfield",iscustomfield);
                            requesttoprocess.put("lineitem",iscustomcolumn);
                            requesttoprocess.put("companyid",companyid);
                            requesttoprocess.put("combodata",combodata);
                            requesttoprocess.put("defaultval",defaultval);
                        
                            JSONObject resultJson = new JSONObject();
                            KwlReturnObject kmsg = null, fresult = null;
                            FieldParams fp = null;
                            String leadFieldId = "";
                            String moduleFieldId = "";
                            int maxlenthvalue=50;
                         
                            int lineitem ="Yes".equalsIgnoreCase(iscustomcolumn) ? 1 : 0;
                            int isCustomField = "Yes".equalsIgnoreCase(iscustomfield) ? 1 : 0;// if false then it is dimension field
                            boolean isEdit = "Yes".equalsIgnoreCase(iseditable) ? true : false;
                            if("1".equalsIgnoreCase(fieldtype)){ // text field
                                maxlenthvalue = Integer.parseInt(maxlength);
                            }
                            String columnExistInModules = Constants.stringInitVal;
                            ArrayList moduleArr = new ArrayList();
                            String relatedModuleId="";
                            boolean createField = true;
                            String[] modulenamearr=modulename.split(";");
                            for(int count=0;count <modulenamearr.length ; count ++){
                                JSONObject moduleid=accCompanyPreferencesControllerCMN.getModuleIDandModuleName(modulenamearr[count].trim());
                                 try{
                                        moduleid.getInt("moduleid");
                                    }catch(Exception ex){
                                        throw new AccountingException("Please give valid Module Name");
                                    }
                                    if("6".equalsIgnoreCase(fieldtype)){ // check ckeckbox
                                        if(moduleid.getInt("moduleid") == Constants.Acc_Product_Master_ModuleId) {
                                            throw new AccountingException("Cannot create Check box as in Product Master");
                                        }
                                    }
                                    if("Yes".equalsIgnoreCase(iscustomcolumn)){ // cannot create line items in product and services
                                        if(moduleid.getInt("moduleid") == Constants.Acc_Product_Master_ModuleId) {
                                            throw new AccountingException("Cannot create line item  in Product Master");
                                        }
                                    }
                                moduleArr.add(moduleid.getInt("moduleid"));
                            }
                            String[] relatedmodulearr=relatedmoduleids.split(";");
                            if(!StringUtil.isNullOrEmpty(relatedmoduleids)){
                                for(int count=0;count <relatedmodulearr.length ; count ++){
                                    JSONObject moduleid=accCompanyPreferencesControllerCMN.getModuleIDandModuleName(relatedmodulearr[count].trim());
                                    try{
                                        moduleid.getInt("moduleid");
                                    }catch(Exception ex){
                                        throw new AccountingException("Please give valid Module Name");
                                    }
                                    relatedModuleId +="," + moduleid.getInt("moduleid");
                                }
                            }
                            for (int cnt = 0; cnt < moduleArr.size(); cnt++) {//Duplicate Name check
                                Integer moduleid = Integer.parseInt(moduleArr.get(cnt).toString());
                                String moduleName = getModuleName(moduleid);

                                HashMap<String, Object> dupCheckRequestParams = new HashMap<String, Object>();
                                dupCheckRequestParams.put(Constants.filter_names, Arrays.asList(Constants.fieldlabel,Constants.companyid, Constants.moduleid));
                                dupCheckRequestParams.put(Constants.filter_values, Arrays.asList(fieldlabel,companyid, moduleid));

                                KwlReturnObject resultDupCheck = accAccountDAOobj.getFieldParams(dupCheckRequestParams);

                                if (resultDupCheck.getEntityList().size() > 0) {
                                    createField = false;
                                    columnExistInModules += moduleName + ", ";
                                }
                            }
                            if(!createField){
                                throw new AccountingException("Custom Field Already exist in module "+columnExistInModules);
                            }
                            if(!StringUtil.isNullOrEmpty(relatedModuleId)){
                                relatedModuleId=relatedModuleId.substring(1,relatedModuleId.length());
                            }
                            HashSet fieldIds= new HashSet();
                            String values = "";
                            String oldValue="";
                            if(createField)
                            {
                                HashMap<String, Object> exisValueRequestParams = new HashMap<String, Object>();
                                exisValueRequestParams.put(Constants.filter_names, Arrays.asList(Constants.fieldlabel, Constants.companyid, "customcolumn", "customfield"));
                                exisValueRequestParams.put(Constants.filter_values, Arrays.asList(fieldlabel, companyid, lineitem, isCustomField));

                                KwlReturnObject resultExsistValueCheck = accAccountDAOobj.getFieldParams(exisValueRequestParams);
                                List list = resultExsistValueCheck.getEntityList();
                                KwlReturnObject allValue = accMasterItemsDAOobj.getValueIfFieldInOtherModule(list, "fieldid");
                                List ls = allValue.getEntityList();
                                Iterator it = ls.iterator();
                                values = combodata;
                                oldValue = "";
                                String columnExistInModule = "";
                                if(fieldtype.equals("4"))
                                    while(it.hasNext())
                                {
                                        Object temp[] = (Object[]) it.next();
                                        if(fieldIds.add(temp[2].toString()));
                                        {
                                            if (columnExistInModule != "") {
                                            columnExistInModule = columnExistInModule + ", ";
                                            } 
                                            columnExistInModule = columnExistInModule + getModuleName(Integer.parseInt(temp[1].toString()));
                                        }
                                } 
                                allValue  = accMasterItemsDAOobj.getValueIfFieldInOtherModule(list,"value");
                                ls = allValue.getEntityList();
                                it = ls.iterator();
                                while(it.hasNext())
                                {
                                    Object temp[] = (Object[]) it.next();
                                    if (oldValue != "") {
                                    oldValue = oldValue + ";";
                                } 
                                oldValue = oldValue + temp[0].toString();
                                resultJson.put("sms", " Column/Dimension name '" + fieldlabel + "' already present in " + columnExistInModule + " with value " + oldValue + ". New value will be included in same dimension with the existing one ");
                                }
                                if (values.replace(";", "").contains(oldValue.replace(";", ""))) {
                                    createField = true;
                                }
                                resultJson.put("values1", oldValue + ";" + values.toString());

                        }
                        if (createField) {
//                            if (!fieldIds.isEmpty()) {
//                                accMasterItemsDAOobj.insertNewValues(fieldIds, values, oldValue);
//                            }
                            HashMap<Integer, HashMap<String, Object>> modulerequestParams = new HashMap<Integer, HashMap<String, Object>>();
                            HashMap<String, Object> requestParams = null;
                            ArrayList<String> ll = new ArrayList<String>();
                            int count = 0;
                            for (int cnt = 0; cnt < moduleArr.size(); cnt++) {//Create new field
                                count++;
                                Integer moduleid = Integer.parseInt(moduleArr.get(cnt).toString());
                                String parentid = "";
                                if (!StringUtil.isNullOrEmpty(parentname) && !"No".equalsIgnoreCase(parentname)) {
                                    parentid = accMasterItemsDAOobj.getParentFieldId(companyid, parentname.trim(), moduleid);
                                    if(StringUtil.isNullOrEmpty(parentid)){
                                         throw new AccountingException("Please give valid Parent- Parent Should be present in system");
                                    }
                                }
                                String relatedmoduleid = "";
                                if (moduleid == Constants.Acc_Product_Master_ModuleId) {
                                    relatedmoduleid = relatedModuleId;
                                }
                                requestParams = processrequestforImport(requesttoprocess, moduleArr.get(cnt).toString(), relatedmoduleid, parentid);
                                modulerequestParams.put(moduleid, requestParams);
                            }
                            if (moduleArr.size() > 0) {
                                /*
                                 *Batch update existing records with default value in thread
                                 */
                                accAccountService.UpdateExistingRecordsWithDefaultValue(modulerequestParams, moduleArr, companyid);
                                ll.add(requestParams.get("response").toString());
                            }
                            JSONObject maxlimitObj = (JSONObject) requestParams.get("response");
                            String maxlimitmsg = maxlimitObj.get("msg").toString();
                            if(maxlimitmsg.contains("Cannot")){
                                throw new AccountingException("Maximum Custom field limit is reached for "+maxlimitObj.getString("moduleName"));
                            }
                            String action = "added";
                            if (isEdit == true) {
                                action = "updated";
                            }
                            String auditaction=(action.equalsIgnoreCase("added")?AuditAction.DIMENTION_ADDED:AuditAction.DIMENTION_UPDATED);
                            String fieldType = "dimension";
                            if (isCustomField == 1) {
                                if (lineitem == 1) {
                                    fieldType = "custom column";
                                    auditaction=(action.equalsIgnoreCase("added")?AuditAction.CUSTOM_COLUMN_ADDED:AuditAction.CUSTOM_COLUMN_UPDATED);
                                } else {
                                    fieldType = "custom field";
                                    auditaction=(action.equalsIgnoreCase("added")?AuditAction.CUSTOM_FIELD_ADDED:AuditAction.CUSTOM_FIELD_UPDATED);
                                }

                            }
                            if (propagateToChildCompanies) {
                                 auditTrailObj.insertAuditLog(auditaction, "User " + sessionHandlerImpl.getUserFullName(request) + " has propagated(added) custom  column " + fieldlabel + " to child company " + ChildCompanyName, request, "0");
                            } else {
                                auditTrailObj.insertAuditLog(auditaction, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + " " + fieldType + " " + fieldlabel, request, "0");
                            }
                            resultJson.put("sucess", ll);
                            resultJson.put(Constants.moduleid, moduleArr);

                        } else {
                            resultJson.put(Constants.RES_success, Constants.RES_msg);
                            resultJson.put("title", "Alert");
                            if (columnExistInModules.length() > 0) {
                                columnExistInModules = columnExistInModules.trim();
                                columnExistInModules = columnExistInModules.substring(0, columnExistInModules.length() - 1);
                            }
                            resultJson.put(Constants.RES_msg, messageSource.getMessage("acc.master.configuration.Cannotaddnew", null, RequestContextUtils.getLocale(request)) + columnExistInModules);
                            resultJson.put("duplicateflag", true);

                        }

                          
                    } catch (Exception ex) {
                        failed++;
                        String errorMsg = ex.getMessage(), invalidColumns = "";
                        try {
                            JSONObject errorLog = new JSONObject(errorMsg);
                            errorMsg = errorLog.getString("errorMsg");
                            invalidColumns = errorLog.getString("invalidColumns");
                        } catch (JSONException jex) {
                        }
                        failedRecords.append("\n" + createCSVrecord(recarr) + "\"" + errorMsg.replaceAll("\"", "") + "\"");
                    }
                    total++;
                }
                cont++;
            }
            if (failed > 0) {
                createFailureFiles(fileName, failedRecords, ".csv");
            }

            int success = total - failed;
            if (total == 0) {
                msg = messageSource.getMessage("acc.field.Emptyfile", null, RequestContextUtils.getLocale(request));
            } else if (success == 0) {
                msg = messageSource.getMessage("acc.rem.169", null, RequestContextUtils.getLocale(request));
            } else if (success == total) {
                msg = messageSource.getMessage("acc.rem.168", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.field.Imported", null, RequestContextUtils.getLocale(request))+ " " + success + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (success > 1 ? "s" : "") + messageSource.getMessage("acc.field.successfully.", null, RequestContextUtils.getLocale(request));
                msg += (failed == 0 ? "." : " "+messageSource.getMessage("acc.field.andfailedtoimport", null, RequestContextUtils.getLocale(request))+ " " + failed + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (failed > 1 ? "s" : "") + ".");
            }

            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                commitedEx = true;
                throw ex;
            }
        } catch (Exception ex) {
            if (!commitedEx) { //if exception occurs during commit then dont call rollback
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            fileInputStream.close();
            csvReader.close();

            DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
            ldef.setName("import_Tx");
            ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus lstatus = txnManager.getTransaction(ldef);

            try {
                //Insert Integration log
                HashMap<String, Object> logDataMap = new HashMap<String, Object>();
                logDataMap.put("FileName", getActualFileName(fileName));
                logDataMap.put("StorageName", fileName);
                logDataMap.put("Log", msg);
                logDataMap.put("Type", "csv");
                logDataMap.put("FailureFileType", failed>0?"csv":"");
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", failed);
                logDataMap.put("Module",Constants.Master_Configuration_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);
                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
            } catch (JSONException ex) {
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }
    
   public ModelAndView ImportCustomFieldsData(HttpServletRequest request, HttpServletResponse response) throws ServiceException, IOException {       
        JSONObject jobj = new JSONObject();
        String msg = "";
        long rowcount = 0;
        int importLimit = 1000;
        CsvReader csvReader = null;
        FileInputStream fileInputStream = null;
        boolean issuccess = false;
        String companyid = "";
        try {
            String doAction = request.getParameter("do");
            System.out.println("A(( " + doAction + " start : " + new Date());
            companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            JSONObject datajobj = new JSONObject();
            if (doAction.compareToIgnoreCase("getMapCSV") == 0) {
                datajobj = importHandler.getMappingCSVHeader(request);
                JSONArray jSONArray = datajobj.getJSONArray("Header");
                fileInputStream = new FileInputStream(datajobj.getString("FilePath"));
                csvReader = new CsvReader(new InputStreamReader(fileInputStream));
                while (csvReader.readRecord()) {
                    rowcount++;
                }
                ValidateHeadersCustomFieldsData(jSONArray);
                if (importLimit <= rowcount) {
                    String userId = sessionHandlerImpl.getUserid(request);
                    String importfile = request.getParameter("titleMsg").trim();
                    requestParams.put("companyid", companyid);
                    requestParams.put("userId", userId);
                    requestParams.put("titleMsg", importfile);
                    requestParams.put("jobj", datajobj);
                    requestParams.put(Constants.globalCurrencyKey, sessionHandlerImpl.getCurrencyID(request));
                    requestParams.put(Constants.df, authHandler.getDateFormatter(request));  //This format belongs to our global date format[i.e.new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa")]
                    requestParams.put(Constants.userdf, authHandler.getUserDateFormatter(request)); //This format holds users date format.
                    importmasterConfig.add(requestParams);
                    if (!importmasterConfig.isIsworking()) {
                        Thread t = new Thread(importmasterConfig);
                        t.start();
                    }
                    jobj.put("success", true);
                } else {                   

                    jobj = ImportCustomFieldsData(request, datajobj);

                    ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.ExtraCompanyPreferences", companyid);
                    if (extraPref != null && extraPref.isPropagateToChildCompanies()) {
                        try {
                            List childCompaniesList = companyDetailsDAOObj.getChildCompanies(companyid);
                            requestParams.put("allowropagatechildcompanies", extraPref.isPropagateToChildCompanies());
                            requestParams.put("childcompanylist", childCompaniesList);
                            requestParams.put("parentcompanyID", companyid);
                            requestParams.put("userId", sessionHandlerImpl.getUserid(request));
                            requestParams.put("titleMsg", request.getParameter("titleMsg").trim());
                            requestParams.put("jobj", datajobj);
                            requestParams.put(Constants.globalCurrencyKey, sessionHandlerImpl.getCurrencyID(request));
                            requestParams.put(Constants.df, authHandler.getDateFormatter(request));  //This format belongs to our global date format[i.e.new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa")]
                            requestParams.put(Constants.userdf, authHandler.getUserDateFormatter(request)); //This format holds users date format.
                            importmasterConfig.add(requestParams);
                            if (!importmasterConfig.isIsworking()) {
                                Thread t = new Thread(importmasterConfig);
                                t.start();
                            }
                        } catch (Exception ex) {
                            Logger.getLogger(accCustomerController.class.getName()).log(Level.SEVERE, null, ex);
                        }

                    }

                }
                issuccess = true;
            }
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();

            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException e) {
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            } finally {
                csvReader.close();
                fileInputStream.close();
            }

            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }       
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
      public void ValidateHeadersCustomFieldsData(JSONArray validateJArray) throws AccountingException, ServiceException {
        try {

            List<String> list = new ArrayList<String>();
            list.add("Master items");
            list.add("Custom fields/Dimension name");
            list.add("Item parent");
            list.add("Parent dimension");
            list.add("Parent dimension value");
            list.add("Is master group item");
            List<String> fileHeaderList = new ArrayList<String>();

            for (int i = 0; i < validateJArray.length(); i++) {
                String header = validateJArray.getJSONObject(i).getString("header").trim();
                fileHeaderList.add(header);
            }

            for (String manadatoryField : list) {
                if (!fileHeaderList.contains(manadatoryField)) {
                    throw new AccountingException(manadatoryField + " column is not available in file");
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
      
    public JSONObject ImportCustomFieldsData(HttpServletRequest request, JSONObject jobj) throws AccountingException, IOException, SessionExpiredException, JSONException {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("import_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        boolean commitedEx = false;
        boolean issuccess = true;
        CsvReader csvReader=null;
        String msg = "";
        FileInputStream fileInputStream = null;
        BufferedReader br = null;
        int total = 0, failed = 0;
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String userId = sessionHandlerImpl.getUserid(request);
        String fileName = jobj.getString("name");
        String importfile=request.getParameter("titleMsg").trim();
        boolean bothcd=importfile.equalsIgnoreCase("Default Fields and Custom Fields/Dimension data")?true:false;
        boolean defaultcd=importfile.equalsIgnoreCase("Default Fields data")?true:false;
        boolean customefielddimention=importfile.equalsIgnoreCase("Custom Fields/Dimension data")?true:false;
        if(bothcd){
            defaultcd=true;
            customefielddimention=true;
        }
        JSONObject returnObj = new JSONObject();

        try {
                        
            fileInputStream = new FileInputStream(jobj.getString("FilePath"));
            csvReader = new CsvReader(new InputStreamReader(fileInputStream));
            //br = new BufferedReader(new InputStreamReader(fileInputStream));
            String record = "";
            int cont = 0;
            
            StringBuilder failedRecords = new StringBuilder();
         
            JSONArray jSONArray = jobj.getJSONArray("Header");
            List headArrayList = new ArrayList();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                headArrayList.add(jSONObject.get("header"));
            }
            
            failedRecords.append(createCSVrecord(headArrayList.toArray()) + "\"Error Message\"");
            
//            Map<String, List<String>> mapfields = new HashMap<String, List<String>>();
            Map<String, Object> dimensionParamMap = new HashMap<String, Object>();
            Set<String> dimensionSet = null;
            String customdimension="";
             while (csvReader.readRecord()) {                 
              if (cont != 0) {
                    record=csvReader.getRawRecord();
                    String[] recarr = record.split(",");
                    try {                          
                            
                            String master_item = recarr[0].trim();
                            if (!StringUtil.isNullOrEmpty(master_item)) {
                                master_item = master_item.replaceAll("\"", "");
                                master_item=master_item.trim();
                            } else {
                                throw new AccountingException("Master item is not Available");
                            }
                            String custom_field_dimension_name = recarr[1].trim();
                            if (!StringUtil.isNullOrEmpty(custom_field_dimension_name)) {
                                custom_field_dimension_name = custom_field_dimension_name.replaceAll("\"", "");
                                custom_field_dimension_name = custom_field_dimension_name.trim();
                            }else {
                                throw new AccountingException(" Custom fields/Dimension name is not Available");
                            }
                            String item_parent = recarr[2].trim();
                            if (!StringUtil.isNullOrEmpty(item_parent)) {
                                item_parent = item_parent.replaceAll("\"", "");
                                item_parent = item_parent.trim();
                            } 
                            
                            String parent_dimension = recarr[3].trim();
                            if (!StringUtil.isNullOrEmpty(parent_dimension)) {
                                parent_dimension = parent_dimension.replaceAll("\"", "");
                                parent_dimension = parent_dimension.trim();
                            }
                            String parent_dimension_value = recarr[4].trim();
                            if (!StringUtil.isNullOrEmpty(parent_dimension_value)) {
                                parent_dimension_value = parent_dimension_value.replaceAll("\"", "");
                                parent_dimension_value = parent_dimension_value.trim();
                            } 
                            String is_master_group_item = recarr[5].trim();
                            if (!StringUtil.isNullOrEmpty(is_master_group_item)) {
                                is_master_group_item = is_master_group_item.replaceAll("\"", "");
                                is_master_group_item = is_master_group_item.trim();
                            } 
                            if(checkForValidation(is_master_group_item)){
                                throw new AccountingException("Please give valid  Value - Value should be 'Yes' or 'No' ");
                            }
                           dimensionParamMap.put(Constants.companyKey, companyid);
                           dimensionParamMap.put("dimension", custom_field_dimension_name);
                            if(StringUtil.isNullOrEmpty(customdimension)){                                
                                dimensionSet = new HashSet<String>(accMasterItemsDAOobj.getUniqueDimensionValues(dimensionParamMap)); 
                                customdimension = custom_field_dimension_name;
                            }
                            
                            String item_description = null;
                            if (customefielddimention && recarr.length>=7) { //ie. if item description column is present 
                                item_description= recarr[6].trim();
                                if (!StringUtil.isNullOrEmpty(item_description)) {
                                    item_description = item_description.replaceAll("\"", "");
                                    item_description = item_description.trim();
                                }
                            }
                            
//                            if("No".equalsIgnoreCase(item_parent) && "No".equalsIgnoreCase(parent_dimension_value) && "No".equalsIgnoreCase(is_master_group_item)){
//                                throw new AccountingException("No Mapping");
//                            }
                            HashMap<String, String> getMastergroupidByName=getMasterGroupMap();
                            //HashMap<String, String> getMastergroupidByName=getMasterGroupMap();
                           if(defaultcd){
                            if("Yes".equalsIgnoreCase(is_master_group_item)){
                                    KwlReturnObject result = null;
                                    if(!getMastergroupidByName.containsKey(custom_field_dimension_name)){
                                        throw new AccountingException("Please give valid  Master Group Name");
                                    }
                                    boolean isPresent = false;
                                    HashMap requestParam = AccountingManager.getGlobalParams(request);
                                    requestParam.put("name",master_item);
                                    requestParam.put("groupid",getMastergroupidByName.get(custom_field_dimension_name));
                                    requestParam.put("companyid",companyid);
                                    HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                                    filter_names.add("masterGroup.groupName");
                                    filter_params.add(custom_field_dimension_name);
                                    filter_names.add("company.companyID");
                                    filter_params.add(companyid);
                                    filter_names.add("value");
                                    filter_params.add(master_item);
                                    filterRequestParams.put("filter_names", filter_names);
                                    filterRequestParams.put("filter_params", filter_params);
                                    KwlReturnObject cntResult = accMasterItemsDAOobj.getMasterItems(filterRequestParams);
                                    int count = cntResult.getRecordTotalCount();
                                    if (count >= 1) {
                                        isPresent = true;
                                    }
                                    if(!"No".equalsIgnoreCase(item_parent) && !StringUtil.isNullOrEmpty(item_parent)){
                                        HashMap<String, Object> parentfilterRequestParams = new HashMap<String, Object>();
                                        ArrayList parentfilter_names = new ArrayList(), parentfilter_params = new ArrayList();
                                        parentfilter_names.add("masterGroup.groupName");
                                        parentfilter_params.add(custom_field_dimension_name);
                                        parentfilter_names.add("company.companyID");
                                        parentfilter_params.add(companyid);
                                        parentfilter_names.add("value");
                                        parentfilter_params.add(item_parent);
                                        parentfilterRequestParams.put("filter_names", filter_names);
                                        parentfilterRequestParams.put("filter_params", filter_params);
                                        KwlReturnObject checkifparentpresent = accMasterItemsDAOobj.getMasterItems(parentfilterRequestParams);
                                        try{
                                            MasterItem mi=(MasterItem)checkifparentpresent.getEntityList().get(0);
                                            requestParam.put("parentid",mi.getID());
                                        }catch(Exception e){
                                             throw new AccountingException("Parent Master item is not present");
                                        }
                                    }
                                    if (isPresent) {
                                            throw new AccountingException("Master item entry for " + master_item  + " already exists.");
                                    } else {
                                        result = accMasterItemsDAOobj.addMasterItem(requestParam);
                                    }
                            }
                            else if(!bothcd){
                                throw new AccountingException("Please give valid  Master Group item ");
                            }
                           }
                        if(customefielddimention){ 
                                   if("No".equalsIgnoreCase(is_master_group_item)){                                       
                                    HashMap<String, String> externalparentchild = new HashMap<String, String>();
                                    HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                                    filter_names.add("field.company.companyID");
                                    filter_params.add(companyid);
                                    filter_names.add("field.fieldlabel");
                                    filter_params.add(custom_field_dimension_name);
                                    filter_names.add("value");
                                    filter_params.add(master_item);
                                    filterRequestParams.put("filter_names", filter_names);
                                    filterRequestParams.put("filter_params", filter_params);
                                    HashMap<String, Object> requestParams = new HashMap<String, Object>();
                                    requestParams.put("filter_names", Arrays.asList("companyid"));
                                    requestParams.put("filter_values", Arrays.asList(sessionHandlerImpl.getCompanyid(request)));
                                    requestParams.put("search_values", custom_field_dimension_name);
                                    KwlReturnObject customefielddimentionresult = accMasterItemsDAOobj.getFieldParamsUsingSql(requestParams);
                                    if(customefielddimentionresult.getRecordTotalCount()<=0){
                                        throw new AccountingException("Please Give valid Custom fields/Dimension name");
                                    }
                                   if(!dimensionSet.add(recarr[0])){
                                           throw new AccountingException("Master item entry for " + master_item  + " already exists.");
                                    }
                                   
                                    KwlReturnObject cntResult=null; 
                                   if(!StringUtil.isNullOrEmpty(item_parent) && !"no".equalsIgnoreCase(item_parent)){
                                        HashMap<String, Object> filterRequestParams1 = new HashMap<String, Object>();
                                        HashMap<String, String> parentchild = new HashMap<String, String>();
                                        ArrayList filter_names1 = new ArrayList(), filter_params1 = new ArrayList();
                                        filter_names1.add("field.company.companyID");
                                        filter_params1.add(companyid);
                                        filter_names1.add("field.fieldlabel");
                                        filter_params1.add(custom_field_dimension_name);
                                        filter_names1.add("value");
                                        filter_params1.add(item_parent);
                                        filterRequestParams1.put("filter_names", filter_names1);
                                        filterRequestParams1.put("filter_params", filter_params1);
                                        cntResult = accMasterItemsDAOobj.getMasterItemsForCustom(filterRequestParams1); // get first parents
                                        if(cntResult.getRecordTotalCount() <= 0){
                                            throw new AccountingException("Please Give valid Item parent name");
                                        }
                                        List<FieldComboData> lstFieldComboData=cntResult.getEntityList();
                                        if(cntResult.getEntityList() != null && !cntResult.getEntityList().isEmpty()&&"no".equalsIgnoreCase(parent_dimension_value)){
                                            Object obj1[]= (Object[])customefielddimentionresult.getEntityList().get(0);
                                            String ModuleId[]=obj1[2].toString().split(",");
                                            int i=0;
                                            for(FieldComboData fcd:lstFieldComboData){
                                                    parentchild.put(fcd.getFieldid(),fcd.getId());
                                                    HashMap requestParam = AccountingManager.getGlobalParams(request);
                                                    requestParam.put("name",master_item);
                                                    requestParam.put("itemdescription",item_description);
                                                    requestParam.put("groupid", ModuleId[i++]);
                                                    requestParam.put("parentid", parentchild.get(fcd.getFieldid()));
                                                    KwlReturnObject result = accMasterItemsDAOobj.addUpdateMasterCustomItem(requestParam, false);
                                                   }
                                        }
                                    }
                                    
                            if(!StringUtil.isNullOrEmpty(parent_dimension_value) && !"no".equalsIgnoreCase(parent_dimension_value)){
                                        HashMap<String, Object> filterRequestParams1 = new HashMap<String, Object>();
                                        String[] parentdimenstionvalue=parent_dimension_value.split(";");
                                        boolean check=false;
                                        String parentValue="";
                                        Object obj1[]= (Object[])customefielddimentionresult.getEntityList().get(0);
                                        for(int count=0 ; count < parentdimenstionvalue.length ; count ++ ){
                                             ArrayList filter_names1 = new ArrayList(), filter_params1 = new ArrayList();
                                            filter_names1.add("field.company.companyID");
                                            filter_params1.add(companyid);
                                            filter_names1.add("field.fieldlabel");
                                            filter_params1.add(parent_dimension);
                                            filter_names1.add("value");
                                            filter_params1.add(parentdimenstionvalue[count]);
                                            HashMap<String, Object> requestParams1 = new HashMap<String, Object>();    
                                            requestParams1.put("filter_names", Arrays.asList("companyid"));
                                            requestParams1.put("filter_values", Arrays.asList(sessionHandlerImpl.getCompanyid(request)));
                                            requestParams1.put("search_values", parent_dimension);
                                            KwlReturnObject parentcustomefielddimentionresult = accMasterItemsDAOobj.getFieldParamsUsingSql(requestParams1);//check parent dimention
                                           if(parentcustomefielddimentionresult.getRecordTotalCount() <= 0){
                                                throw new AccountingException("Please give valid parent dimension");
                                            }
                                            
                                            HashMap<String, Object> filterRequestParams2 = new HashMap<String, Object>();
                                            ArrayList filter_names2 = new ArrayList(), filter_params2 = new ArrayList();
                                            FieldParams parentFieldParams = (FieldParams) kwlCommonTablesDAOObj.getClassObject(FieldParams.class.getName(), obj1[1].toString());
                                            String grouptid=parentFieldParams.getParentid();
                                            filter_names2.add("field.id");
                                            filter_params2.add(grouptid);
                                            filterRequestParams2.put("filter_names", filter_names2);
                                            filterRequestParams2.put("filter_params", filter_params2);
                                            KwlReturnObject cntResult1 = accMasterItemsDAOobj.getMasterItemsForCustomHire(filterRequestParams2);    // get external parent dimesion
                                            List list = cntResult1.getEntityList();
                                            Iterator itr = list.iterator();
                                            String parentid=null;
                                            while(itr.hasNext()){
                                             Object[] row = (Object[]) itr.next();
                                             FieldComboData fieldComboData = (FieldComboData) row[0];
                                             if(fieldComboData.getValue().equalsIgnoreCase(parentdimenstionvalue[count])){  //Find parent dimension value
                                                 check=true;
                                                parentid=fieldComboData.getId();
                                                parentValue+=parentid+",";
                                             }
                                            }
                                            if(!check){
                                               throw new AccountingException("Please give valid parent dimension value");
                                             }
                                         }
                                            //List<FieldComboData> lstFieldComboData = cntResult1.getEntityList();
                                            HashMap requestParam = AccountingManager.getGlobalParams(request);
                                            String recordID = null;
                                            String parentValueStr="";
                                            if (!StringUtil.isNullOrEmpty(item_parent) && "no".equalsIgnoreCase(item_parent)) {
                                                String ModuleId[] = obj1[2].toString().split(",");
                                              for (int i = 0; i < ModuleId.length; i++) {
                                                  requestParam.put("name", master_item);
                                                  requestParam.put("itemdescription",item_description);
                                                    requestParam.put("groupid", ModuleId[i]);
                                                    KwlReturnObject result = accMasterItemsDAOobj.addUpdateMasterCustomItem(requestParam, false);
                                                    FieldComboData fieldComboData = ((FieldComboData) result.getEntityList().get(0));
                                                    recordID = fieldComboData.getId();
                                                    if (!StringUtil.isNullOrEmpty(recordID)) {
                                                        HashMap<String, Object> extparentdimension = new HashMap<String, Object>();
                                                        extparentdimension.put("chieldValueId", recordID);
                                                        accMasterItemsDAOobj.deleteMasterCustomItemMapping(recordID);
//                                                        String parentValueArray[] = parentValue.split(",");
                                                         parentValueStr = accMasterItemsService.getParentItemsForMap(fieldComboData.getField(), parentValue);
                                                        if (!parentValueStr.isEmpty()) {
                                                            String parentValueArray[] = parentValueStr.split(",");
                                                            for (int cnt = 0; cnt < parentValueArray.length; cnt++) {
                                                                extparentdimension.put("parentValueid", parentValueArray[cnt]);
                                                                accMasterItemsDAOobj.addUpdateMasterCustomItemMapping(extparentdimension);
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            else{
                                                HashMap<String, String> parentchild = new HashMap<String, String>();
                                                List<FieldComboData> lstFieldComboData1=cntResult.getEntityList();
                                                int i=0;
                                                if(cntResult.getEntityList() != null && !cntResult.getEntityList().isEmpty()){
                                                String ModuleId[] = obj1[2].toString().split(",");
                                                    for(FieldComboData fcd:lstFieldComboData1){
                                                    parentchild.put(fcd.getFieldid(),fcd.getId());
                                                    HashMap requestParam1 = AccountingManager.getGlobalParams(request);
                                                    requestParam1.put("name",master_item);
                                                    requestParam.put("itemdescription",item_description);
                                                    requestParam1.put("groupid",ModuleId[i++]);
                                                    requestParam1.put("parentid", parentchild.get(fcd.getFieldid()));
                                                    KwlReturnObject result = accMasterItemsDAOobj.addUpdateMasterCustomItem(requestParam1, false);
                                                    FieldComboData fieldComboData = ((FieldComboData) result.getEntityList().get(0));
                                                    recordID = fieldComboData.getId();
                                                        if (!StringUtil.isNullOrEmpty(recordID)) {
                                                            HashMap<String, Object> extparentdimension = new HashMap<String, Object>();
                                                            extparentdimension.put("chieldValueId", recordID);
                                                            accMasterItemsDAOobj.deleteMasterCustomItemMapping(recordID);
//                                                            String parentValueArray[] = parentValue.split(",");
                                                            parentValueStr = accMasterItemsService.getParentItemsForMap(fieldComboData.getField(), parentValue);
                                                            if (!parentValueStr.isEmpty()) {
                                                                String parentValueArray[] = parentValueStr.split(",");
                                                                for (int cnt = 0; cnt < parentValueArray.length; cnt++) {
                                                                    extparentdimension.put("parentValueid", parentValueArray[cnt]);
                                                                    accMasterItemsDAOobj.addUpdateMasterCustomItemMapping(extparentdimension);
                                                                }
                                                            }
                                                    }
                                                
                                                }
                                               }
                                            }
                                          
                                        //}
                                    }                                    
                                if("no".equalsIgnoreCase(parent_dimension_value)&&"no".equalsIgnoreCase(item_parent)){
                                    HashMap requestParam = AccountingManager.getGlobalParams(request);
                                    Object obj1[]= (Object[])customefielddimentionresult.getEntityList().get(0);
                                    String ModuleId[]=obj1[2].toString().split(",");
                                    for (int i = 0; i < ModuleId.length; i++) {
                                    requestParam.put("name",master_item);
                                    requestParam.put("itemdescription",item_description);
                                    requestParam.put("groupid", ModuleId[i]);
                                    KwlReturnObject result = accMasterItemsDAOobj.addUpdateMasterCustomItem(requestParam, false); 
                                    }
                                }

                            } else if (!bothcd) {
                                throw new AccountingException("Please give valid  Master Group item ");
                            }
                        }
                    } catch (Exception ex) {
                        failed++;
                        String errorMsg = ex.getMessage(), invalidColumns = "";
                        try {
                            JSONObject errorLog = new JSONObject(errorMsg);
                            errorMsg = errorLog.getString("errorMsg");
                            invalidColumns = errorLog.getString("invalidColumns");
                        } catch (JSONException jex) {
                        }
                        failedRecords.append("\n" + createCSVrecord(recarr) + "\"" + errorMsg.replaceAll("\"", "") + "\"");
                    }
                    total++;
                }
                cont++;
            }
            if (failed > 0) {
                createFailureFiles(fileName, failedRecords, ".csv");
            }
            
            int success = total - failed;
            if (total == 0) {
                msg = messageSource.getMessage("acc.field.Emptyfile", null, RequestContextUtils.getLocale(request));
            } else if (success == 0) {
                msg = messageSource.getMessage("acc.rem.169", null, RequestContextUtils.getLocale(request));
            } else if (success == total) {
                msg = messageSource.getMessage("acc.rem.168", null, RequestContextUtils.getLocale(request));
            } else {
                msg = messageSource.getMessage("acc.field.Imported", null, RequestContextUtils.getLocale(request))+ " " + success + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (success > 1 ? "s" : "") + messageSource.getMessage("acc.field.successfully.", null, RequestContextUtils.getLocale(request));
                msg += (failed == 0 ? "." : " "+messageSource.getMessage("acc.field.andfailedtoimport", null, RequestContextUtils.getLocale(request))+ " " + failed + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (failed > 1 ? "s" : "") + ".");
            }
            
            try {
                txnManager.commit(status);
            } catch (Exception ex) {
                commitedEx = true;
                throw ex;
            }
        } catch (Exception ex) {
            if (!commitedEx) { //if exception occurs during commit then dont call rollback
                txnManager.rollback(status);
            }
            issuccess = false;
            msg = "" + ex.getMessage();

            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            fileInputStream.close();
            csvReader.close();
           // br.close();
            
            DefaultTransactionDefinition ldef = new DefaultTransactionDefinition();
            ldef.setName("import_Tx");
            ldef.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
            TransactionStatus lstatus = txnManager.getTransaction(ldef);

            try {
                //Insert Integration log
                HashMap<String, Object> logDataMap = new HashMap<String, Object>();
                logDataMap.put("FileName", getActualFileName(fileName));
                logDataMap.put("StorageName", fileName);
                logDataMap.put("Log", msg);
                logDataMap.put("Type", "csv");
                logDataMap.put("FailureFileType", failed>0?"csv":"");
                logDataMap.put("TotalRecs", total);
                logDataMap.put("Rejected", failed);
                logDataMap.put("Module",Constants.Master_Configuration_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);
                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
            } catch (JSONException ex) {
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return returnObj;
    }   
     public HashMap getMasterGroupMap() throws ServiceException {
        HashMap MasterGroupMap = new HashMap();
        KwlReturnObject mastergroupresult = accMasterItemsDAOobj.getMasterGroups();
        List<MasterGroup> mastergroupList = mastergroupresult.getEntityList();
        for(MasterGroup mg:mastergroupList){
                MasterGroupMap.put(mg.getGroupName(), mg.getID());
            }
        return MasterGroupMap;
    }
     public String createCSVrecord(Object[] listArray) {
        String rec = "";
        for (int i = 0; i < listArray.length; i++) {    //Discard columns id at index 0 and isvalid,invalidColumns, validationlog at last 3 indexes.
           rec += "\"" + (listArray[i] == null ? "" : listArray[i].toString().replaceAll("\"", "")) + "\",";
        }
        return rec;
    }
     private String getXType(String fieldTypeName){
        if(fieldTypeName.equalsIgnoreCase("Text Field")){
            return "1";
        }else if(fieldTypeName.equalsIgnoreCase("Numeric Field")){
            return "2";
        }else if(fieldTypeName.equalsIgnoreCase("Date")){
            return "3";
        }else if(fieldTypeName.equalsIgnoreCase("Drop Down")){
            return "4";
        }else if(fieldTypeName.equalsIgnoreCase("Time Field")){
            return "5";
        }else if(fieldTypeName.equalsIgnoreCase("Multi-Select Drop Down")){
            return "7";
        }/*else if(fieldTypeName.equalsIgnoreCase("Drop Down")){
            return "8";
        }*/else if(fieldTypeName.equalsIgnoreCase("Autono")){
            return "9";
        }else if(fieldTypeName.equalsIgnoreCase("Check Box")){
            return "11";
        } else if (fieldTypeName.equalsIgnoreCase("Check List")) {
            return "12";
        } else if (fieldTypeName.equalsIgnoreCase("Text Area")) {
            return "13";
        }else{
            return "";
        }
     }
    public static String getActualFileName(String storageName) {
        String ext = storageName.substring(storageName.lastIndexOf("."));
        String actualName = storageName.substring(0, storageName.lastIndexOf("_"));
        actualName = actualName + ext;
        return actualName;
    }
    public boolean checkForValidation(String storageName) {
        if("Yes".equalsIgnoreCase(storageName) || "No".equalsIgnoreCase(storageName)){
               return false;
        }
        return true;
    }
    public void createFailureFiles(String filename, StringBuilder failedRecords, String ext) {
        String destinationDirectory;
        try {
            destinationDirectory = storageHandlerImpl.GetDocStorePath() + "importplans";
            if (StringUtil.isNullOrEmpty(ext)) {
                ext = filename.substring(filename.lastIndexOf("."));
            }
            filename = filename.substring(0, filename.lastIndexOf("."));

            java.io.FileOutputStream failurefileOut = new java.io.FileOutputStream(destinationDirectory + "/" + filename + ImportLog.failureTag + ext);
            failurefileOut.write(failedRecords.toString().getBytes());
            failurefileOut.flush();
            failurefileOut.close();
        } catch (Exception ex) {
            System.out.println("\nError file write [success/failed] " + ex);
        }
    }
    public HashMap<String, Object> processrequestforImport(HashMap<String, Object> request, String moduelId,String relatedmoduleid,String parentid) throws SessionExpiredException, JSONException, ServiceException {
        HashMap<String, Object> requestParams;
        Integer moduleid = Integer.parseInt(moduelId);
        String companyid = String.valueOf(request.get("companyid"));
        Integer fieldtype = Integer.parseInt(request.get("fieldType").toString());
        
        int moduleflag = 0;
        HashMap<String, Object> colParams = null;
        HashMap<String, Object> RefcolParams = null;
        String maxlength = "";
        if (fieldtype == 7) {// multiselect
            RefcolParams = getcolumn_number(companyid, moduleid, fieldtype, moduleflag);
            if (!Boolean.parseBoolean((String) RefcolParams.get("success"))) {
                colParams = RefcolParams;
            } else {
                colParams = getcolumn_number(companyid, moduleid, 1, moduleflag);
            }
            maxlength = "1000";
        } else {
            colParams = getcolumn_number(companyid, moduleid, fieldtype, moduleflag);
            if (fieldtype == 2) { // number fields
                maxlength = "15";
            } else if (fieldtype == 3) { // date
                maxlength = "50";
            } else if (fieldtype == 4) { // dropdown
                maxlength = "50";
            } else if (fieldtype == 5) { // timefield
                maxlength = "25";
            } else if (fieldtype == 7){
                maxlength = "1000";
            } else if (fieldtype == 8) { // reference dropdown
                maxlength = "255";
            } else if (fieldtype == 9) { // auto number
                maxlength = "150";
            }
        }
        if (Boolean.parseBoolean((String) colParams.get("success"))) {

            requestParams = new HashMap<String, Object>();

            String fieldlabel = String.valueOf(request.get("fieldlabel"));
            String editable = String.valueOf(request.get("iseditable"));

            Integer fieldmaxlen = 12;
            if (!StringUtil.isNullOrEmpty(request.get("maxlength").toString())) {
                maxlength = request.get("maxlength").toString();
            }
            if (!StringUtil.isNullOrEmpty(maxlength)) {
                fieldmaxlen = Integer.parseInt(maxlength);
            }

            Integer validationtype = 0;


            int sendNotification ="Yes".equalsIgnoreCase(request.get("sendnotification").toString()) ? 1 : 0;
            String notificationDays = !StringUtil.isNullOrEmpty(request.get("notificationDays").toString()) ? request.get("notificationDays").toString(): "";
            String isessential ="Yes".equalsIgnoreCase(request.get("isessential").toString()) ?  "1" : "0";
            int isCustomField = "Yes".equalsIgnoreCase(request.get("iscustomfield").toString()) ? 1 : 0;// if false then it is dimension field
            int lineitem ="Yes".equalsIgnoreCase(request.get("lineitem").toString()) ? 1 : 0;
            String combodata = request.get("combodata").toString();


            int essential = 0;
            boolean allowmapping = false;

                if (moduleflag == 1 && fieldtype == 8) {
                    allowmapping = false;
                } else if (fieldtype == 9) {
                    allowmapping = false;
                } else {
                    allowmapping = true;
                }
                 if (((!StringUtil.isNullOrEmpty(isessential)) && isessential.equals("0")) || fieldtype == 9) {// if field is auto no then no need to mark as mandatory
                    essential = 0;
                } else if (!StringUtil.isNullOrEmpty(isessential)) {
                    essential = 1;
                }
            
            requestParams.put("Maxlength", fieldmaxlen);
            requestParams.put("Isessential", essential);
            requestParams.put("sendNotification", sendNotification);
            requestParams.put("notificationDays", notificationDays);
            requestParams.put("Fieldtype", fieldtype);

            requestParams.put("isforproject", "Yes".equalsIgnoreCase(request.get("isforproject").toString())? 1 : 0);
            
            requestParams.put("Validationtype", validationtype);
            requestParams.put("Customregex", "");
            requestParams.put("Fieldname", Constants.Custom_Record_Prefix + fieldlabel);

            requestParams.put("Fieldlabel", fieldlabel);
            requestParams.put("Companyid", companyid);
            requestParams.put("Moduleid", moduleid);
            requestParams.put("Customfield", isCustomField);
            requestParams.put("Customcolumn", lineitem);
            if (!StringUtil.isNullOrEmpty(parentid)) {
                requestParams.put(Constants.parentmoduleid, parentid);
            }
            if (!StringUtil.isNullOrEmpty(relatedmoduleid)) {
                requestParams.put(Constants.relatedmoduleid, relatedmoduleid);
            }
            requestParams.put("Iseditable", editable);
            String RefModule = null;
            String RefDataColumn = null;
            String RefFetchColumn = null;
            String comboid = "";

            requestParams.put("Comboname", "");
            requestParams.put("Comboid", comboid);
            requestParams.put("Moduleflag", 0);

            requestParams.put("Colnum", colParams.get("column_number"));
            String Refcolumn_number = "0";
            String refcolumnname = null;
            if (fieldtype == 7 && RefcolParams != null) {
                requestParams.put("Refcolnum", RefcolParams.get("column_number"));
                refcolumnname = Constants.Custom_column_Prefix + RefcolParams.get("column_number");
                Refcolumn_number = RefcolParams.get("column_number").toString();
            }
            requestParams.put("isfortask","Yes".equalsIgnoreCase(request.get("isfortask").toString())? 1 : 0);

            JSONObject resultJson = new JSONObject();
            KwlReturnObject kmsg = null;
            FieldParams fp = null;
            kmsg = accAccountDAOobj.insertfield(requestParams);
            resultJson.put("success", kmsg.isSuccessFlag());
            requestParams.put("success", kmsg.isSuccessFlag() ? 1 : 0);
            if (kmsg.isSuccessFlag()) {
                fp = (FieldParams) kmsg.getEntityList().get(0);
                resultJson.put("ID", fp.getId());
                resultJson.put("msg", kmsg.getMsg());
                String defaultvalue = request.get("defaultval").toString();
                defaultvalue = insertfieldcombodata(combodata, fp.getId(), defaultvalue,new HashMap<String, HashMap<String,Object>>());
                requestParams.put("defaultvalue","");
                String colname = Constants.Custom_column_Prefix + colParams.get("column_number");
                String column_name = Constants.Custom_Column_Prefix + requestParams.get("Colnum");

            } else {
                resultJson.put("msg", "Error Processing request");
            }
            requestParams.put("response", resultJson);


        } else {
            return colParams;
        }
        return requestParams;
    }

    public ModelAndView insertfield(HttpServletRequest request, HttpServletResponse response) {
        JSONObject resultJson = new JSONObject();
        KwlReturnObject kmsg = null, fresult = null;
        FieldParams fp = null;
        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        TransactionStatus status = txnManager.getTransaction(def);
        String leadFieldId = "",fieldtooltip="";;
        String moduleFieldId = "";
        String modulename = "";
        String addressField = "";        
        KwlReturnObject addressFieldIdForDimensionUpdate=null;
        String msg = "";
        boolean issuccess = false;
        int maxlength=50;
        boolean isForeClaim = false;
        try {
            int lineitem = StringUtil.isNullOrEmpty(request.getParameter("lineitem")) ? 0 : 1;
            int isCustomField = Boolean.parseBoolean(request.getParameter("iscustomfield")) ? 1 : 0;// if false then it is dimension field
            int type = Integer.parseInt(request.getParameter("fieldType"));
            //IsForGSTRuleMapping flg is trues if field is created for GST
            boolean IsForGSTRuleMapping = !StringUtil.isNullOrEmpty(request.getParameter(Constants.IsForGSTRuleMapping)) ? Boolean.parseBoolean(request.getParameter(Constants.IsForGSTRuleMapping)) : false;
            if(!StringUtil.isNullOrEmpty(request.getParameter("addressField"))){
              addressField=request.getParameter("addressField");
            }
            boolean isEdit =  Boolean.parseBoolean(request.getParameter("isEdit")) ? true : false;
            String fieldlabel = request.getParameter("fieldlabel");
            fieldlabel = fieldlabel.trim().replaceAll("\\s+"," ");                  //Remove all white spaces in between the String.
            if(request.getParameter("fieldtooltip")!=null){
            fieldtooltip= request.getParameter("fieldtooltip");
            fieldtooltip = StringUtil.isNullOrEmpty(fieldtooltip)?"":fieldtooltip.trim().replaceAll("\\s+"," ");
            }
             if (request.getParameter("maxlength") != null && !StringUtil.isNullOrEmpty(request.getParameter("maxlength"))) {
                maxlength = Integer.parseInt(request.getParameter("maxlength"));
            }
            String defaultval = !StringUtil.isNullOrEmpty(request.getParameter("defaultval")) ? request.getParameter("defaultval") : "";
            boolean isAutoPopulateDefaultValue = !StringUtil.isNullOrEmpty(request.getParameter(Constants.ISAUTOPOPULATEDEFAULTVALUE)) ? Boolean.parseBoolean(request.getParameter(Constants.ISAUTOPOPULATEDEFAULTVALUE)) : false;
            boolean isForSalesCommission = !StringUtil.isNullOrEmpty(request.getParameter(Constants.ISFORSALESCOMMISSION)) ? Boolean.parseBoolean(request.getParameter(Constants.ISFORSALESCOMMISSION)) : false;
            boolean isForKnockOff = !StringUtil.isNullOrEmpty(request.getParameter(Constants.isForKnockOff)) ? Boolean.parseBoolean(request.getParameter(Constants.isForKnockOff)) : false;
            String ModuleIDs=request.getParameter("moduleID");
            String columnExistInModules = Constants.stringInitVal;
            boolean differentTypeExist = false;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            ArrayList moduleArr = new ArrayList();
            ArrayList parentArr = new ArrayList();
            String relatedModuleId="";
            int relatedmoduleisallowedit=Constants.DONOT_ALLOW_TO_EDIT_PRODUCT_CUSTOMFIELD;         //Setting default to 0 as if the check is not selected on UI side it sends null value so setting it false ERM-177 / ERP-34804
            boolean createField = true;
            if (request.getParameter("columncreationinvoice") != null) {
                moduleArr.add(Constants.Acc_Invoice_ModuleId);
                     if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationinvoiceparent"))) {
                        parentArr.add(request.getParameter("columncreationinvoiceparent"));
                    }
                //                    moduleArr.add(Constants.Acc_BillingInvoice_ModuleId);
            }
            if (request.getParameter("columncreationveninvoice") != null) {
                moduleArr.add(Constants.Acc_Vendor_Invoice_ModuleId);
                       if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationveninvoiceparent"))) {
                        parentArr.add(request.getParameter("columncreationveninvoiceparent"));
                    }
                //                    moduleArr.add(Constants.Acc_Vendor_BillingInvoice_ModuleId);
            }
            if (request.getParameter("columncreationdebitnote") != null) {
                moduleArr.add(Constants.Acc_Debit_Note_ModuleId);
                         if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationdebitnoteparent"))) {
                        parentArr.add(request.getParameter("columncreationdebitnoteparent"));
                    }
                //                    moduleArr.add(Constants.Acc_Vendor_BillingInvoice_ModuleId);
            }
            if (request.getParameter("columncreationcreditnote") != null) {
                moduleArr.add(Constants.Acc_Credit_Note_ModuleId);
                          if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationcreditnoteparent"))) {
                        parentArr.add(request.getParameter("columncreationcreditnoteparent"));
                    }  
                //                    moduleArr.add(Constants.Acc_Vendor_BillingInvoice_ModuleId);
            }
            if (request.getParameter("columncreationmakepayment") != null) {
                moduleArr.add(Constants.Acc_Make_Payment_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationmakepaymentparent"))) {
                    parentArr.add(request.getParameter("columncreationmakepaymentparent"));
                }
                //                    moduleArr.add(Constants.Acc_Vendor_BillingInvoice_ModuleId);
            }
            if (request.getParameter("columncreationreceivepayment") != null) {
                moduleArr.add(Constants.Acc_Receive_Payment_ModuleId);
               if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationreceivepaymentparent"))) {
                    parentArr.add(request.getParameter("columncreationreceivepaymentparent"));
                }  
                //                    moduleArr.add(Constants.Acc_Vendor_BillingInvoice_ModuleId);
            }
            if (request.getParameter("columncreationgeneraletry") != null) {
                moduleArr.add(Constants.Acc_GENERAL_LEDGER_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationgeneraletryparent"))) {
                    parentArr.add(request.getParameter("columncreationgeneraletryparent"));
                }
            }

            if (request.getParameter("columncreationpurchaseorderid") != null) {
                moduleArr.add(Constants.Acc_Purchase_Order_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationpurchaseorderparent"))) {
                    parentArr.add(request.getParameter("columncreationpurchaseorderparent"));
            }
                }
            if (request.getParameter("columncreationsalesorderid") != null) {
                moduleArr.add(Constants.Acc_Sales_Order_ModuleId);
                    if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationsalesorderparent"))) {
                    parentArr.add(request.getParameter("columncreationsalesorderparent"));
            }
                }
            if (request.getParameter("columncreationcustomerquotationid") != null) {
                moduleArr.add(Constants.Acc_Customer_Quotation_ModuleId);
                      if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationcustomerquotationparent"))) {
                    parentArr.add(request.getParameter("columncreationcustomerquotationparent"));
            }
                }
            if (request.getParameter("columncreationvendorquotationid") != null) {
                moduleArr.add(Constants.Acc_Vendor_Quotation_ModuleId);
                        if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationvendorquotationparent"))) {
                    parentArr.add(request.getParameter("columncreationvendorquotationparent"));
                     }
            }
            if (request.getParameter("columncreationpurchaserequisition") != null) {
                moduleArr.add(Constants.Acc_Purchase_Requisition_ModuleId);
                  if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationpurchaserequisitionparent"))) {
                    parentArr.add(request.getParameter("columncreationpurchaserequisitionparent"));
                }
            }
            if (request.getParameter("columncreationcustomer") != null) {
                moduleArr.add(Constants.Acc_Customer_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationcustomerparent"))) {
                    parentArr.add(request.getParameter("columncreationcustomerparent"));
                }
            }
            if (request.getParameter("columncreationvendor") != null) {
                moduleArr.add(Constants.Acc_Vendor_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationvendorparent"))) {
                    parentArr.add(request.getParameter("columncreationvendorparent"));
                }
            }
            if (request.getParameter("columncreationdeliveryorder") != null) {
                moduleArr.add(Constants.Acc_Delivery_Order_ModuleId);
                   if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationdeliveryorderparent"))) {
                    parentArr.add(request.getParameter("columncreationdeliveryorderparent"));
            }
                }
            if (request.getParameter("columncreationrequestforquotation") != null) {
                moduleArr.add(Constants.Acc_RFQ_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationrequestforquotationparent"))) {
                    parentArr.add(request.getParameter("columncreationrequestforquotationparent"));
                }
            }
            if (request.getParameter("columncreationgoodsreceipt") != null) {
                moduleArr.add(Constants.Acc_Goods_Receipt_ModuleId);
                   
                   if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationgoodsreceiptparent"))) {
                    parentArr.add(request.getParameter("columncreationgoodsreceiptparent"));
            }
                }
            if (request.getParameter("columncreationsalesreturn") != null) {
                moduleArr.add(Constants.Acc_Sales_Return_ModuleId);
                   
                   if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationsalesreturnparent"))) {
                    parentArr.add(request.getParameter("columncreationsalesreturnparent"));
            }
                }
                if (request.getParameter("columncreationpurchasereturn") != null) {
                    moduleArr.add(Constants.Acc_Purchase_Return_ModuleId);
                    if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationpurchasereturnparent"))) {
                        parentArr.add(request.getParameter("columncreationpurchasereturnparent"));
                    }
                }

            if (request.getParameter("columncreationcustomerquotation") != null) {
                moduleArr.add(Constants.Acc_Customer_Quotation_ModuleId);
                      if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationcustomerquotationparent"))) {
                    parentArr.add(request.getParameter("columncreationcustomerquotationparent"));
            }
                }

            if (request.getParameter("columncreationvendorquotation") != null) {
                moduleArr.add(Constants.Acc_Vendor_Quotation_ModuleId);
                   if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationvendorquotationparent"))) {
                    parentArr.add(request.getParameter("columncreationvendorquotationparent"));
            }
            }  
            if (request.getParameter("columncreationfixedassetsdisposalinvoice") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_DisposalInvoice_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetsdisposalinvoiceparent"))) {
                    parentArr.add(request.getParameter("columncreationfixedassetsdisposalinvoiceparent"));
                }
            }

            if (request.getParameter("columncreationfixedassetspurchaseinvoice") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetspurchaseinvoiceparent"))) {
                    parentArr.add(request.getParameter("columncreationfixedassetspurchaseinvoiceparent"));
                }
            }
            if (request.getParameter("columncreationfixedassetsgoodsreceipt") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_GoodsReceipt_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetsgoodsreceiptparent"))) {
                    parentArr.add(request.getParameter("columncreationfixedassetsgoodsreceiptparent"));
                }
            }

            if (request.getParameter("columncreationfixedassetsdeliveryorder") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_DeliveryOrder_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetsdeliveryorderparent"))) {
                    parentArr.add(request.getParameter("columncreationfixedassetsdeliveryorderparent"));
                }
            }
            if (request.getParameter("columncreationfixedassetsgroups") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_AssetsGroups_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetsgroupsparent"))) {
                    parentArr.add(request.getParameter("columncreationfixedassetsgroupsparent"));
                }
            }
            if (request.getParameter("columncreationfixedassetspurchaserequisition") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetspurchaserequisitionparent"))) {
                    parentArr.add(request.getParameter("columncreationfixedassetspurchaserequisitionparent"));
                }
            }
            if (request.getParameter("columncreationfixedassetsrfq") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_RFQ_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetsrfqparent"))) {
                    parentArr.add(request.getParameter("columncreationfixedassetsrfqparent"));
                }
            }
            if (request.getParameter("columncreationfixedassetsvendorquotation") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetsvendorquotationparent"))) {
                    parentArr.add(request.getParameter("columncreationfixedassetsvendorquotationparent"));
                }
            }
            if (request.getParameter("columncreationfixedassetspurchaseorder") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_Purchase_Order_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetspurchaseorderparent"))) {
                    parentArr.add(request.getParameter("columncreationfixedassetspurchaseorderparent"));
                }
            }

            if (request.getParameter("columncreationleaseorder") != null) {
                moduleArr.add(Constants.Acc_Lease_Order_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationleaseorderparent"))) {
                    parentArr.add(request.getParameter("columncreationleaseorderparent"));
                }
            }
           if (request.getParameter("columncreationcontract") != null) {
                moduleArr.add(Constants.Acc_Contract_Order_ModuleId);
               if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationcontractparent"))) {
                   parentArr.add(request.getParameter("columncreationcontractparent"));
               }
            }
            if (request.getParameter("columncreationproduct") != null) {
                moduleArr.add(Constants.Acc_Product_Master_ModuleId);                
                /*
                 add to assign parentid in field params for product module
                */
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationproductparent"))) {
                   parentArr.add(request.getParameter("columncreationproductparent"));
             }
            }

            if (request.getParameter("AccountStatement") != null) {
                moduleArr.add(Constants.Account_Statement_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("AccountStatementParent"))) {
                    parentArr.add(request.getParameter("AccountStatementParent"));
                }
            }

            if (request.getParameter("columncreationserial") != null) {
                moduleArr.add(Constants.SerialWindow_ModuleId);
                parentArr.add("");
            }
            if (request.getParameter("columncreationinventory") != null) {
                moduleArr.add(Constants.Inventory_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationstockissueparent"))) {
                    parentArr.add(request.getParameter("columncreationstockissueparent"));
                }
            }
            if (request.getParameter("columnCreationLeaseInvoice") != null) {
                moduleArr.add(Constants.LEASE_INVOICE_MODULEID);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columnCreationLeaseInvoiceparent"))) {
                    parentArr.add(request.getParameter("columnCreationLeaseInvoiceparent"));
                }
            }
            
            if (request.getParameter("columncreationconsignmentrequest") != null) {
                moduleArr.add(Constants.Acc_ConsignmentRequest_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationconsignmentrequestparent"))) {
                    parentArr.add(request.getParameter("columncreationconsignmentrequestparent"));
                }
            }
            if (request.getParameter("columncreationconsignmentinvoice") != null) {
                moduleArr.add(Constants.Acc_ConsignmentInvoice_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationconsignmentinvoiceparent"))) {
                    parentArr.add(request.getParameter("columncreationconsignmentinvoiceparent"));
                }
            }
            if (request.getParameter("columncreationconsignmentdo") != null) {
                moduleArr.add(Constants.Acc_ConsignmentDeliveryOrder_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationconsignmentdoparent"))) {
                    parentArr.add(request.getParameter("columncreationconsignmentdoparent"));
                }
            }
            if (request.getParameter("columncreationconsignmentsalesreturn") != null) {
                moduleArr.add(Constants.Acc_ConsignmentSalesReturn_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationconsignmentsalesreturnparent"))) {
                    parentArr.add(request.getParameter("columncreationconsignmentsalesreturnparent"));
                }
            }
            if (request.getParameter("columncreationconsignmentvendorrequest") != null) {
                moduleArr.add(Constants.Acc_ConsignmentVendorRequest_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationconsignmentvendorrequestparent"))) {
                    parentArr.add(request.getParameter("columncreationconsignmentvendorrequestparent"));
                }
            }
            if (request.getParameter("columncreationconsignmentpurchaseinvoice") != null) {
                moduleArr.add(Constants.Acc_Consignment_GoodsReceipt_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationconsignmentpurchaseinvoiceparent"))) {
                    parentArr.add(request.getParameter("columncreationconsignmentpurchaseinvoiceparent"));
                }
            }
            if (request.getParameter("columncreationconsignmentgr") != null) {
                moduleArr.add(Constants.Acc_Consignment_GoodsReceiptOrder_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationconsignmentgrparent"))) {
                    parentArr.add(request.getParameter("columncreationconsignmentgrparent"));
                }
            }
            if (request.getParameter("columncreationconsignmentpurchasereturn") != null) {
                moduleArr.add(Constants.Acc_ConsignmentPurchaseReturn_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationconsignmentpurchasereturnparent"))) {
                    parentArr.add(request.getParameter("columncreationconsignmentpurchasereturnparent"));
                }
            }
            if (request.getParameter("columncreationleasecontract") != null) {
                moduleArr.add(Constants.Acc_Lease_Contract);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationleasecontractparent"))) {
                    parentArr.add(request.getParameter("columncreationleasecontractparent"));
                }
            }
            if (request.getParameter("columncreationleasequotation") != null) {
                moduleArr.add(Constants.Acc_Lease_Quotation);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationleasequotationparent"))) {
                    parentArr.add(request.getParameter("columncreationleasequotationparent"));
                }
            }
            if (request.getParameter("columncreationleasedo") != null) {
                moduleArr.add(Constants.Acc_Lease_DO);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationleasedoparent"))) {
                    parentArr.add(request.getParameter("columncreationleasedoparent"));
                }
            }
            if (request.getParameter("columncreationleasereturn") != null) {
                moduleArr.add(Constants.Acc_Lease_Return);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationleasereturnparent"))) {
                    parentArr.add(request.getParameter("columncreationleasereturnparent"));
                }
            }
            if (request.getParameter("columncreationfixedassetspurchasereturn") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_Purchase_Return_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetspurchasereturnparent"))) {
                    parentArr.add(request.getParameter("columncreationfixedassetspurchasereturnparent"));
                }
            }
            if (request.getParameter("columncreationfixedassetssalesreturn") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_Sales_Return_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetssalesreturnparent"))) {
                    parentArr.add(request.getParameter("columncreationfixedassetssalesreturnparent"));
                }
            }
            if (request.getParameter("columncreationfixedassetdetails") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_Details_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetdetailsparent"))) {
                    parentArr.add(request.getParameter("columncreationfixedassetdetailsparent"));
                }
            }
            if (request.getParameter("columncreationinvoiceform") != null) {
                relatedModuleId ="," + String.valueOf(Constants.Acc_Invoice_ModuleId);
                //                    moduleArr.add(Constants.Acc_BillingInvoice_ModuleId);
            }
            if (request.getParameter("columncreationveninvoiceform") != null) {
//                if (StringUtil.isNullOrEmpty(relatedModuleId)) {
//                    moduleArr.add(Constants.Acc_Vendor_Invoice_ModuleId);
//                } else {
                    relatedModuleId += "," + String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId);
//                }
            }
            if (request.getParameter("columncreationsalesorderform") != null) {
//                if (StringUtil.isNullOrEmpty(relatedModuleId)) {
//                    moduleArr.add(Constants.Acc_Sales_Order_ModuleId);
//                    relatedModuleId = String.valueOf(Constants.Acc_Sales_Order_ModuleId);
//                } else {
                    relatedModuleId += "," + String.valueOf(Constants.Acc_Sales_Order_ModuleId);
//                }
            }
            if (request.getParameter("columncreationpurchaseorderform") != null) {
//                if (StringUtil.isNullOrEmpty(relatedModuleId)) {
//                    moduleArr.add(Constants.Acc_Purchase_Order_ModuleId);
//                    relatedModuleId = String.valueOf(Constants.Acc_Purchase_Order_ModuleId);
//                } else {
                    relatedModuleId += "," + String.valueOf(Constants.Acc_Purchase_Order_ModuleId);
//                }
            }
            if (request.getParameter("columncreationdeliveryorderform") != null) {
//                if (StringUtil.isNullOrEmpty(relatedModuleId)) {
//                    moduleArr.add(Constants.Acc_Delivery_Order_ModuleId);
//                    relatedModuleId = String.valueOf(Constants.Acc_Delivery_Order_ModuleId);
//                } else {
                    relatedModuleId += "," + String.valueOf(Constants.Acc_Delivery_Order_ModuleId);
//                }
              }
            
              if (request.getParameter("columncreationgrorderform") != null) {    //add custom field enhancement
//                  if (StringUtil.isNullOrEmpty(relatedModuleId)) {
//                      moduleArr.add(Constants.Acc_Goods_Receipt_ModuleId);
//                  } else {
                      relatedModuleId += "," + String.valueOf(Constants.Acc_Goods_Receipt_ModuleId);
//                  }
              }
              if (request.getParameter("columncreationcustomerquotationform") != null) {
//                  if (StringUtil.isNullOrEmpty(relatedModuleId)) {
//                      moduleArr.add(Constants.Acc_Customer_Quotation_ModuleId);
//                  } else {
                      relatedModuleId += "," + String.valueOf(Constants.Acc_Customer_Quotation_ModuleId);
//                  }
              }
              if (request.getParameter("columncreationvendorquotationform") != null) {
//                  if (StringUtil.isNullOrEmpty(relatedModuleId)) {
//                      moduleArr.add(Constants.Acc_Vendor_Quotation_ModuleId);
//                  } else {
                      relatedModuleId += "," + String.valueOf(Constants.Acc_Vendor_Quotation_ModuleId);
//                  }
              }
              if (request.getParameter("columncreationpurchaserequisitionform") != null) {
//                  if (StringUtil.isNullOrEmpty(relatedModuleId)) {
//                      moduleArr.add(Constants.Acc_Purchase_Requisition_ModuleId);
//                  } else {
                      relatedModuleId += "," + String.valueOf(Constants.Acc_Purchase_Requisition_ModuleId);
//                  }
              }
              if (request.getParameter("columncreationpurchasereturnform") != null) {
                      relatedModuleId += "," + String.valueOf(Constants.Acc_Purchase_Return_ModuleId);
              }
              if (request.getParameter("columncreationsalesreturnform") != null) {
                      relatedModuleId += "," + String.valueOf(Constants.Acc_Sales_Return_ModuleId);
              }
              
             
//              if (request.getParameter("columncreationpoform") != null) {
//                  if (StringUtil.isNullOrEmpty(relatedModuleId)) {
//                      moduleArr.add(Constants.Acc_Purchase_Order_ModuleId);
//                  } else {
//                      relatedModuleId += "," + String.valueOf(Constants.Acc_Purchase_Order_ModuleId);
//                  }
//              }
              if (request.getParameter("columncreationpoform") != null) {
                relatedModuleId += "," + String.valueOf(Constants.Acc_Purchase_Order_ModuleId);
             }
//                if (request.getParameter("columncreationcontact") != null) {
//                    moduleArr.add(Constants.Crm_contact_moduleid);
//                }
//                if (request.getParameter("columncreationproduct") != null) {
//                    moduleArr.add(Constants.Crm_product_moduleid);
//                }
//                if (request.getParameter("columncreationopportunity") != null) {
//                    moduleArr.add(Constants.Crm_opportunity_moduleid);
//                }
//                if (request.getParameter("columncreationcase") != null) {
//                    moduleArr.add(Constants.Crm_case_moduleid);
//                }
// code for  inventory modules
               if (request.getParameter("columncreationstockadjustment") != null) {
                moduleArr.add(Constants.Inventory_Stock_Adjustment_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationstockadjustmentparent"))) {
                    parentArr.add(request.getParameter("columncreationstockadjustmentparent"));
                }
            }
               if (request.getParameter("columncreationstockrequest") != null) {
                moduleArr.add(Constants.Acc_Stock_Request_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationstockrequestparent"))) {
                    parentArr.add(request.getParameter("columncreationstockrequestparent"));
                }
            }
               if (request.getParameter("columnCreationIST") != null) {
                moduleArr.add(Constants.Acc_InterStore_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columnCreationISTparent"))) {
                    parentArr.add(request.getParameter("columnCreationISTparent"));
                }
            }
               if (request.getParameter("columncreationILT") != null) {
                moduleArr.add(Constants.Acc_InterLocation_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationILTparent"))) {
                    parentArr.add(request.getParameter("columncreationILTparent"));
                }
            }
            if (request.getParameter("columncreationcyclec") != null) {
                moduleArr.add(Constants.Acc_CycleCount_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationcyclecparent"))) {
                    parentArr.add(request.getParameter("columncreationcyclecparent"));
                }
            }
            if (request.getParameter("columncreationlabour") != null) {
                moduleArr.add(Constants.Labour_Master);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationlabourparent"))) {
                    parentArr.add(request.getParameter("columncreationlabourparent"));
                }
            }
            if (request.getParameter("columncreationWorkCenter") != null) {
                moduleArr.add(Constants.MRP_WORK_CENTRE_MODULEID);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationWorkCenterparent"))) {
                    parentArr.add(request.getParameter("columncreationWorkCenterparent"));
                }
            }
            if (request.getParameter("columncreationMachineMaster") != null) {
                moduleArr.add(Constants.MRP_Machine_Management_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationMachineMasterparent"))) {
                    parentArr.add(request.getParameter("columncreationMachineMasterparent"));
                }
            }
            if (request.getParameter("columncreationWorkOrder") != null) {
                moduleArr.add(Constants.MRP_WORK_ORDER_MODULEID);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationWorkOrderparent"))) {
                    parentArr.add(request.getParameter("columncreationWorkOrderparent"));
                }
            }
            if (request.getParameter("columncreationvendorjobworkorder") != null) {
                moduleArr.add(Constants.VENDOR_JOB_WORKORDER_MODULEID);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationvendorjobworkorderparent"))) {
                    parentArr.add(request.getParameter("columncreationvendorjobworkorderparent"));
                }
            }
            if (request.getParameter("columncreationMasterContract") != null) {
                moduleArr.add(Constants.MRP_Contract);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationMasterContractparent"))) {
                    parentArr.add(request.getParameter("columncreationMasterContractparent"));
                }
            }
            if (request.getParameter("columncreationRoutingTemplate") != null) {
                moduleArr.add(Constants.MRP_RouteCode);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationRoutingTemplateparent"))) {
                    parentArr.add(request.getParameter("columncreationRoutingTemplateparent"));
                }
            }
            if (request.getParameter("columncreationjobwork") != null) {
                moduleArr.add(Constants.MRP_JOB_WORK_MODULEID);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationjobworkparent"))) {
                    parentArr.add(request.getParameter("columncreationjobworkparent"));
                }
            }
            if (request.getParameter("columncreationsecgateentry") != null) {
                moduleArr.add(Constants.Acc_SecurityGateEntry_ModuleId);  
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationsecgateentryparent"))) {
                    parentArr.add(request.getParameter("columncreationsecgateentryparent"));
                }
            }
            if (request.getParameter(Constants.CustomField_ColumnCreationMultiEntityDimension) != null) {
                moduleArr.add(Constants.Acc_Multi_Entity_Dimension_MODULEID);  
                if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.CustomField_ColumnCreationMultiEntityDimensionParent))) {
                    parentArr.add(request.getParameter(Constants.CustomField_ColumnCreationMultiEntityDimensionParent));
                }
            }
            if (request.getParameter(Constants.CustomField_ColumnCreationJobWorkOut) != null) {
                moduleArr.add(Constants.JOB_WORK_OUT_ORDER_MODULEID);  
                if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.CustomField_ColumnCreationJobWorkOutParent))) {
                    parentArr.add(request.getParameter(Constants.CustomField_ColumnCreationJobWorkOutParent));
                }
            }
                        
                 HashMap<String, Object> gstRefColParams = null;
                //this is block is added to get next col nummber for GST custom fields
                int GSTMappingColnum = 0;
                if (Boolean.parseBoolean(request.getParameter(Constants.IsForGSTRuleMapping))) {
                    gstRefColParams = accAccountDAOobj.getMaxGSTMappingColumn(companyid);
                    if ((boolean) gstRefColParams.get("success")) {
                        GSTMappingColnum = (int) gstRefColParams.get("colNum");
                        if (!isEdit) {
                            // increamented only in Create Case, Colnum will not be increased no matter How many times edited 
                            GSTMappingColnum++;
                        }
                        request.setAttribute("GSTMappingColnum", GSTMappingColnum);
                    }
                }
            
            /**
             * Do not allow field with the same name but different field type.
             */
            HashMap<String, Object> dupfieldParams = new HashMap<String, Object>();
            dupfieldParams.put(Constants.filter_names, Arrays.asList(Constants.fieldlabel, Constants.companyid));
            dupfieldParams.put(Constants.filter_values, Arrays.asList(fieldlabel, companyid));
            KwlReturnObject duplicateField = accAccountDAOobj.getFieldParams(dupfieldParams);
            if (duplicateField != null && duplicateField.getEntityList() != null && duplicateField.getEntityList().size() > 0) {
                for (int i = 0; i < duplicateField.getEntityList().size(); i++) {
                    fp = (FieldParams) duplicateField.getEntityList().get(i);
                    if (fp.getFieldtype() != type) {
                        createField = false;
                        differentTypeExist = true;
                    }
                    break;
                }
            }

            if (!differentTypeExist) {
                for (int cnt = 0; cnt < moduleArr.size(); cnt++) {//Duplicate Name check
                    Integer moduleid = Integer.parseInt(moduleArr.get(cnt).toString());
                    String moduleName = getModuleName(moduleid);

                    HashMap<String, Object> dupCheckRequestParams = new HashMap<String, Object>();
                    dupCheckRequestParams.put(Constants.filter_names, Arrays.asList(Constants.fieldlabel, Constants.companyid, Constants.moduleid));
                    dupCheckRequestParams.put(Constants.filter_values, Arrays.asList(fieldlabel, companyid, moduleid));

                    if ("1".equals(request.getParameter("isforeclaim"))) {   //refer ticket ERP-17187
                        //Restrict to create multiple dimension when dimension already created with check isforeclaim=1
                        KwlReturnObject result = accAccountDAOobj.findDimensionForEclaim(companyid);
                        if (!isEdit) {
                            if (result.getRecordTotalCount() > 0) {
                                throw new AccountingException(messageSource.getMessage("acc.master.isforeClaim", new Object[]{}, RequestContextUtils.getLocale(request)));
                            }
                        }
                        isForeClaim = true;
                        dupCheckRequestParams.clear();
                        dupCheckRequestParams.put(Constants.filter_names, Arrays.asList(Constants.fieldlabel, Constants.companyid, Constants.moduleid, Constants.isforeclaim));
                        dupCheckRequestParams.put(Constants.filter_values, Arrays.asList(fieldlabel, companyid, moduleid, 1));   //int value 1 required
                    }

                    KwlReturnObject resultDupCheck = accAccountDAOobj.getFieldParams(dupCheckRequestParams);

                    if (resultDupCheck.getEntityList().size() > 0) {
                        createField = false;
                        columnExistInModules += moduleName + ", ";
                    }
                }
            }

            if(!StringUtil.isNullOrEmpty(relatedModuleId)){
                 relatedModuleId=relatedModuleId.substring(1,relatedModuleId.length());
            }
            /**
             * Below check is implemented to check weather the User had enabled
             * Allow to edit Products Custom field in various documents where
             * product can be used if it is enabled the setting value 0 in
             * databases. ERP-34804 / ERM-177.
             */
            if (request.getParameter("relatedModuleIsAllowEdit") != null && StringUtil.equalIgnoreCase(request.getParameter("relatedModuleIsAllowEdit"), "on")) {
                relatedmoduleisallowedit = Constants.ALLOW_TO_EDIT_PRODUCT_CUSTOMFIELD;
            }
            if(isEdit){
                    if(!StringUtil.isNullOrEmpty(ModuleIDs)){
                        String[] ModuleIDArray= ModuleIDs.split(",");
                        String ModuleIds="";
                        String ModuleIdsNew="";
                        for(int Count=0;Count < ModuleIDArray.length; Count++ )
                        {
                            ModuleIds+="'"+ModuleIDArray[Count]+"'";
                            ModuleIdsNew+=ModuleIDArray[Count];
                            if(!(Count== ModuleIDArray.length-1))
                            {
                                ModuleIds+=",";
                                ModuleIdsNew+=",";
                            }
                        }
                        KwlReturnObject fieldlabelDupCheck = accAccountDAOobj.getFieldParamsforEdit(fieldlabel,ModuleIds,companyid);
                        
                        if (fieldlabelDupCheck.getEntityList().size() > 0) {
                            createField = false;
                            for(int i=0;i<fieldlabelDupCheck.getEntityList().size();i++) {
                                fp = (FieldParams) fieldlabelDupCheck.getEntityList().get(i);
                                Integer moduleid = fp.getModuleid();
                                String moduleName = getModuleName(moduleid);
                                columnExistInModules += moduleName + ", ";
                            }
                        } else {            
//                            below fuction took more time to execute and dont need more hence commented
//                            String defaultvalue = request.getParameter("defaultval");
//                            String combodata = request.getParameter("combodata");
//                            for(int Count=0;Count < ModuleIDArray.length; Count++ ) {
//                                insertfieldcombodata(combodata, ModuleIDArray[Count], defaultvalue);
//                            }
                            KwlReturnObject Upadated = accAccountDAOobj.updateCustomColumnmfield(fieldlabel,ModuleIds,lineitem,maxlength,fieldtooltip, defaultval, isAutoPopulateDefaultValue, isForSalesCommission, isForKnockOff);
                            if(!StringUtil.isNullOrEmpty(relatedModuleId)){
                              int Upadate = accAccountDAOobj.updateCustomProductfield(ModuleIds,relatedModuleId,companyid);
                              if(Upadate != 0){
                                  relatedModuleId="";
                              }
                           }
                            /**
                             * Updating the value of Related Module is allowed to edit or not in case of editing the custom or dimension field.
                             * ERP-34804 / ERM-177.
                             */
                              int Upadate = accAccountDAOobj.updateCustomProductfieldIsAllowedToEdit(ModuleIds,relatedmoduleisallowedit,companyid);
                        }
                        String splitModuleId[]=ModuleIdsNew.split(",");
                        for(int Count=0;Count < splitModuleId.length; Count++ )
                        {
                            FieldParams parentFieldParams = (FieldParams) kwlCommonTablesDAOObj.getClassObject(FieldParams.class.getName(), splitModuleId[Count]);
                            String parentId = getParentForModule(parentFieldParams.getModuleid(), request);
                            if (!StringUtil.isNullOrEmpty(parentId)) {
                               KwlReturnObject Upadated = accAccountDAOobj.updateDimensionParent(fieldlabel, splitModuleId[Count], lineitem, parentId);
                            }

                        }  
                    }
            }  
            HashSet fieldIds= new HashSet();
            String values = "";
            String oldValue="";
            if(createField)
                {
                HashMap<String, Object> exisValueRequestParams = new HashMap<String, Object>();
                exisValueRequestParams.put(Constants.filter_names, Arrays.asList(Constants.fieldlabel,Constants.companyid,"customcolumn", "customfield"));
                exisValueRequestParams.put(Constants.filter_values, Arrays.asList(fieldlabel,companyid, lineitem, isCustomField));
                
                KwlReturnObject resultExsistValueCheck = accAccountDAOobj.getFieldParams(exisValueRequestParams);
                addressFieldIdForDimensionUpdate=resultExsistValueCheck; // Object Copied, to get fieldParams ID to use in addressfielddimensionmapping transactions.
                List list =resultExsistValueCheck.getEntityList();
               KwlReturnObject allValue  = accMasterItemsDAOobj.getValueIfFieldInOtherModule(list,"fieldid");
               List ls = allValue.getEntityList();
               Iterator it = ls.iterator();
                values = request.getParameter("combodata");
                oldValue="";
               String columnExistInModule="" ;
               if(request.getParameter("fieldType").equals("4"))
                while(it.hasNext())
               {
                     Object temp[] = (Object[]) it.next();
                     if(fieldIds.add(temp[2].toString()));
                     {
                        if (columnExistInModule != "") {
                           columnExistInModule = columnExistInModule + ", ";
                        } 
                        columnExistInModule = columnExistInModule + getModuleName(Integer.parseInt(temp[1].toString()));
                     }
               } 
                allValue  = accMasterItemsDAOobj.getValueIfFieldInOtherModule(list,"value");
                ls = allValue.getEntityList();
                it = ls.iterator();
               while(it.hasNext())
               {
                        Object temp[] = (Object[]) it.next();
                         if (oldValue != "") {
                           oldValue = oldValue + ";";
                        } 
                        oldValue = oldValue + temp[0].toString();
//                        createField = false;
                        resultJson.put("sms", " Column/Dimension name '" + fieldlabel + "' already present in " + columnExistInModule + " with value " + oldValue + ". New value will be included in same dimension with the existing one ");
                  }
               if(values.replace(";", "").contains(oldValue.replace(";", "")))
               {
                   createField=true;
               }
                if(isEdit){
                    resultJson.put("values1",  oldValue);
                }    
                else{
                     resultJson.put("values1",  oldValue+";"+values.toString());
                }
                    
                }
            if (createField) {
//               if(!fieldIds.isEmpty())
//               accMasterItemsDAOobj.insertNewValues( fieldIds,values,oldValue);
                HashMap<Integer, HashMap<String, Object>> modulerequestParams = new HashMap<Integer, HashMap<String, Object>>();
                HashMap<String, Object> requestParams = null;
                ArrayList<String> ll = new ArrayList<String>();
                int count = 0;
                for (int cnt = 0; cnt < moduleArr.size(); cnt++) {//Create new field
                    count++;
                    Integer moduleid = Integer.parseInt(moduleArr.get(cnt).toString());
                    String parentid = "";
                    if(!parentArr.isEmpty() && cnt < parentArr.size() && parentArr.get(cnt)!=null){
                        parentid=parentArr.get(cnt).toString();
                    }
                    String relatedmoduleid="";
                    if(moduleid==Constants.Acc_Product_Master_ModuleId){
                        relatedmoduleid=relatedModuleId;
                    }
                    requestParams = processrequest(request, moduleArr.get(cnt).toString(),relatedmoduleid,parentid,false,"","");
                    modulerequestParams.put(moduleid, requestParams);
                    requestParams.put(Constants.iscustomcolumn, lineitem);
                }
                if (moduleArr.size() > 0) {
                    /*
                     *Batch update existing records with default value in thread
                     */
                    accAccountService.UpdateExistingRecordsWithDefaultValue(modulerequestParams, moduleArr, companyid);
                    ll.add(requestParams.get("response").toString());
                }
                String action="added";
                if(isEdit==true)
                {
                    action="updated";
                }
                String fieldType="dimension";
                String auditaction=(action.equalsIgnoreCase("added")?AuditAction.DIMENTION_ADDED:AuditAction.DIMENTION_UPDATED);
                if(isCustomField==1)
                        {
                            if(lineitem==1)
                            {
                                fieldType="custom column";
                                auditaction=(action.equalsIgnoreCase("added")?AuditAction.CUSTOM_COLUMN_ADDED:AuditAction.CUSTOM_COLUMN_UPDATED);
                            }
                            else
                            {
                                fieldType="custom field";
                                 auditaction=(action.equalsIgnoreCase("added")?AuditAction.CUSTOM_FIELD_ADDED:AuditAction.CUSTOM_FIELD_UPDATED);
                            }
                            
                        }           
                auditTrailObj.insertAuditLog(auditaction, "User " + sessionHandlerImpl.getUserFullName(request) + " has "+action+" "+fieldType+" "+fieldlabel, request, "0");
                txnManager.commit(status);
                resultJson.put("sucess", ll);
                resultJson.put(Constants.moduleid, moduleArr);
                
             /**
              * Following code is written to Save address field Mapped with Dimension for GST calculations
              */   
                if (modulerequestParams.containsKey(Constants.GSTModule) || (addressFieldIdForDimensionUpdate.getEntityList().size()>0)) {
                    HashMap<String, Object> requestParamsForAddressMapping = null;
                    requestParamsForAddressMapping = modulerequestParams.get(Constants.GSTModule);
                    if (IsForGSTRuleMapping && !StringUtil.isNullOrEmpty(addressField)) {
                        HashMap<String, Object> addressMap = new HashMap<String, Object>();
                        if (requestParamsForAddressMapping != null && requestParamsForAddressMapping.get("response") != null) {
                            JSONObject result = new JSONObject((String) requestParams.get("response").toString());
                            addressMap.put("fieldId", result.getString("ID"));
                        } else {
                            if (addressFieldIdForDimensionUpdate != null) {
                                List<FieldParams> list = addressFieldIdForDimensionUpdate.getEntityList();                                
                                for (FieldParams fieldParams : list) {
                                    if (fieldParams.getModuleid() == Constants.GSTModule) {
                                        addressMap.put("fieldId", fieldParams.getId());
                                        break;
                                    }
                                }
                            }
                        }
//                    requestParams.put("Company", companyid);
                        addressMap.put("addressField", addressField);
                        addressMap.put("isEdit", isEdit);
                        addressMap.put("company", companyid);
                        accAccountControllerCMNServiceObj.saveOrUpdateAddressFieldForGSTDimension(addressMap);
                    }
                }
//*****************************************Propagate masteritem In child companies**************************
            ExtraCompanyPreferences pref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject(ExtraCompanyPreferences.class.getName(),sessionHandlerImpl.getCompanyid(request));
            boolean propagateTOChildCompaniesFalg = pref.isPropagateToChildCompanies();
            
            if (propagateTOChildCompaniesFalg) {
            
                try {
                    String parentcompanyid = sessionHandlerImpl.getCompanyid(request);
                    if (!isEdit) {
                        List childCompaniesList = companyDetailsDAOObj.getChildCompanies(parentcompanyid);

                        for (Object childObj : childCompaniesList) {
                            
                            Object[] childdataOBj = (Object[]) childObj;
                             String childCompanyID = (String) childdataOBj[0];
                             String parentFieldparmId="";
                            for (int cnt = 0; cnt < moduleArr.size(); cnt++) {
                                try {
                                    Integer moduleid = Integer.parseInt(moduleArr.get(cnt).toString());
                                    if (modulerequestParams.containsKey(moduleid)) {
                                        requestParams = modulerequestParams.get(moduleid);
                                        JSONObject jobj = (JSONObject) requestParams.get("response");
                                        parentFieldparmId=jobj.getString("ID");
                                    PropagateCustomFieldAndDimensions(propagateTOChildCompaniesFalg,request,parentFieldparmId,childCompanyID,"");
                                    
                            
                                    }
                                } catch (Exception ex) {
                                     Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                    }else{
                        
                          String[] ModuleIDArray= ModuleIDs.split(","); 
                        for (int i = 0; i < ModuleIDArray.length; i++) {

                            HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                            filter_names.add("propagatedfieldparamID.id");
                            filter_params.add(ModuleIDArray[i]);

                            filterRequestParams.put("filter_names", filter_names);
                            filterRequestParams.put("filter_values", filter_params);
                            KwlReturnObject kwl = accMasterItemsDAOobj.getFieldParams(filterRequestParams);
                            List childList = kwl.getEntityList();
                            String fieldidsString="";
                            FieldParams fm=null;
                            for (Object ob : childList) {
                                 fm=(FieldParams)ob;   
                               fieldidsString+=fm.getId()+",";
                                
                            }
                            fieldidsString=fieldidsString.substring(0, (fieldidsString.length()-1));
                            PropagateCustomFieldAndDimensions(propagateTOChildCompaniesFalg,request,"",fm.getCompanyid(),fieldidsString);
                        }
                      
                          
                    
                    }
                } catch (Exception ex) {
                   Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
             //*****************************************Propagate masteritem In child companies Ends Here**************************

            } else {
                resultJson.put(Constants.RES_success, Constants.RES_msg);
                resultJson.put("title", "Alert");
                if (columnExistInModules.length() > 0) {
                    columnExistInModules = columnExistInModules.trim();
                    columnExistInModules = columnExistInModules.substring(0, columnExistInModules.length() - 1);
                }
                String CannotaddnewForeClaim = messageSource.getMessage("acc.master.configuration.CannotaddnewForeClaim", null, RequestContextUtils.getLocale(request));
                String Cannotaddnew = messageSource.getMessage("acc.master.configuration.Cannotaddnew", null, RequestContextUtils.getLocale(request));
                String alreadyExist = messageSource.getMessage("acc.master.configuration.alreadyexist", null, RequestContextUtils.getLocale(request));
                if (differentTypeExist) {
                    msg = alreadyExist;
                } else {
                    msg = (isForeClaim ? CannotaddnewForeClaim : Cannotaddnew) + columnExistInModules;
                }
                resultJson.put("duplicateflag", true);
                txnManager.rollback(status);
            }
             issuccess = true;
        } catch (SessionExpiredException ex) {
            logger.warn(ex.getMessage(), ex);
            msg = "" + ex.getMessage();
            txnManager.rollback(status);
        } catch (JSONException ex) {
            logger.warn(ex.getMessage(), ex);
            msg = "" + ex.getMessage();
            txnManager.rollback(status);
        } catch (Exception ex) {
            logger.warn(ex.getMessage(), ex);
            msg = "" + ex.getMessage();
            txnManager.rollback(status);
        } finally {
            try {
                resultJson.put("success", issuccess);
                resultJson.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", resultJson.toString());
    }
        
    public void PropagateCustomFieldAndDimensions(boolean propagateTOChildCompaniesFalg,HttpServletRequest request, String parentFieldParamID,String childCompanyID,String fieldidsString) {
        JSONObject resultJson = new JSONObject();
        KwlReturnObject kmsg = null, fresult = null;
        FieldParams fp = null;
        //Create transaction
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("JE_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
        TransactionStatus status = txnManager.getTransaction(def);
        String leadFieldId = "",fieldtooltip="";;
        String moduleFieldId = "";
        String modulename = "";
        int maxlength=50;
        boolean isForeClaim = false;
        try {
            int lineitem = StringUtil.isNullOrEmpty(request.getParameter("lineitem")) ? 0 : 1;
            int isCustomField = Boolean.parseBoolean(request.getParameter("iscustomfield")) ? 1 : 0;// if false then it is dimension field
            boolean isEdit =  Boolean.parseBoolean(request.getParameter("isEdit")) ? true : false;
            String fieldlabel = request.getParameter("fieldlabel");
            fieldlabel = fieldlabel.trim().replaceAll("\\s+"," ");                  //Remove all white spaces in between the String.
            if(request.getParameter("fieldtooltip")!=null){
            fieldtooltip= request.getParameter("fieldtooltip");
            fieldtooltip = StringUtil.isNullOrEmpty(fieldtooltip)?"":fieldtooltip.trim().replaceAll("\\s+"," ");
            }
             if (request.getParameter("maxlength") != null && !StringUtil.isNullOrEmpty(request.getParameter("maxlength"))) {
                maxlength = Integer.parseInt(request.getParameter("maxlength"));
            }
            String defaultval = !StringUtil.isNullOrEmpty(request.getParameter("defaultval")) ? request.getParameter("defaultval") : "";
            boolean isAutoPopulateDefaultValue = !StringUtil.isNullOrEmpty(request.getParameter(Constants.ISAUTOPOPULATEDEFAULTVALUE)) ? Boolean.parseBoolean(request.getParameter(Constants.ISAUTOPOPULATEDEFAULTVALUE)) : false;
            boolean isForSalesCommission = !StringUtil.isNullOrEmpty(request.getParameter(Constants.ISFORSALESCOMMISSION)) ? Boolean.parseBoolean(request.getParameter(Constants.ISFORSALESCOMMISSION)) : false;
            boolean isForKnockOff = !StringUtil.isNullOrEmpty(request.getParameter(Constants.isForKnockOff)) ? Boolean.parseBoolean(request.getParameter(Constants.isForKnockOff)) : false;
            
            String ModuleIDs=fieldidsString; //************************************************
            String columnExistInModules = Constants.stringInitVal;
            String companyid = childCompanyID;//******************************************************************
            ArrayList moduleArr = new ArrayList();
            ArrayList parentArr = new ArrayList();
            String relatedModuleId="";
            int relatedmoduleisallowedit=Constants.DONOT_ALLOW_TO_EDIT_PRODUCT_CUSTOMFIELD;         //Setting default to 0 as if the check is not selected on UI side it sends null value so setting it false ERM-177 / ERP-34804
            boolean createField = true;
            if (request.getParameter("columncreationinvoice") != null) {
                moduleArr.add(Constants.Acc_Invoice_ModuleId);
                     if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationinvoiceparent"))) {
                        parentArr.add(request.getParameter("columncreationinvoiceparent"));
                    }
                //                    moduleArr.add(Constants.Acc_BillingInvoice_ModuleId);
            }
            if (request.getParameter("columncreationveninvoice") != null) {
                moduleArr.add(Constants.Acc_Vendor_Invoice_ModuleId);
                       if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationveninvoiceparent"))) {
                        parentArr.add(request.getParameter("columncreationveninvoiceparent"));
                    }
                //                    moduleArr.add(Constants.Acc_Vendor_BillingInvoice_ModuleId);
            }
            if (request.getParameter("columncreationdebitnote") != null) {
                moduleArr.add(Constants.Acc_Debit_Note_ModuleId);
                         if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationdebitnoteparent"))) {
                        parentArr.add(request.getParameter("columncreationdebitnoteparent"));
                    }
                //                    moduleArr.add(Constants.Acc_Vendor_BillingInvoice_ModuleId);
            }
            if (request.getParameter("columncreationcreditnote") != null) {
                moduleArr.add(Constants.Acc_Credit_Note_ModuleId);
                          if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationcreditnoteparent"))) {
                        parentArr.add(request.getParameter("columncreationcreditnoteparent"));
                    }  
                //                    moduleArr.add(Constants.Acc_Vendor_BillingInvoice_ModuleId);
            }
            if (request.getParameter("columncreationmakepayment") != null) {
                moduleArr.add(Constants.Acc_Make_Payment_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationmakepaymentparent"))) {
                    parentArr.add(request.getParameter("columncreationmakepaymentparent"));
                }
                //                    moduleArr.add(Constants.Acc_Vendor_BillingInvoice_ModuleId);
            }
            if (request.getParameter("columncreationreceivepayment") != null) {
                moduleArr.add(Constants.Acc_Receive_Payment_ModuleId);
               if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationreceivepaymentparent"))) {
                    parentArr.add(request.getParameter("columncreationreceivepaymentparent"));
                }  
                //                    moduleArr.add(Constants.Acc_Vendor_BillingInvoice_ModuleId);
            }
            if (request.getParameter("columncreationgeneraletry") != null) {
                moduleArr.add(Constants.Acc_GENERAL_LEDGER_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationgeneraletryparent"))) {
                    parentArr.add(request.getParameter("columncreationgeneraletryparent"));
                }
            }

            if (request.getParameter("columncreationpurchaseorderid") != null) {
                moduleArr.add(Constants.Acc_Purchase_Order_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationpurchaseorderparent"))) {
                    parentArr.add(request.getParameter("columncreationpurchaseorderparent"));
            }
                }
            if (request.getParameter("columncreationsalesorderid") != null) {
                moduleArr.add(Constants.Acc_Sales_Order_ModuleId);
                    if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationsalesorderparent"))) {
                    parentArr.add(request.getParameter("columncreationsalesorderparent"));
            }
                }
            if (request.getParameter("columncreationsecgateentry") != null) {
                moduleArr.add(Constants.Acc_SecurityGateEntry_ModuleId);  
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationsecgateentryparent"))) {
                    parentArr.add(request.getParameter("columncreationsecgateentryparent"));
                }
            }
            if (request.getParameter("columncreationcustomerquotationid") != null) {
                moduleArr.add(Constants.Acc_Customer_Quotation_ModuleId);
                      if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationcustomerquotationparent"))) {
                    parentArr.add(request.getParameter("columncreationcustomerquotationparent"));
            }
                }
            if (request.getParameter("columncreationvendorquotationid") != null) {
                moduleArr.add(Constants.Acc_Vendor_Quotation_ModuleId);
                        if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationvendorquotationparent"))) {
                    parentArr.add(request.getParameter("columncreationvendorquotationparent"));
                     }
            }
            if (request.getParameter("columncreationpurchaserequisition") != null) {
                moduleArr.add(Constants.Acc_Purchase_Requisition_ModuleId);
                  if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationpurchaserequisitionparent"))) {
                    parentArr.add(request.getParameter("columncreationpurchaserequisitionparent"));
                }
            }
            if (request.getParameter("columncreationcustomer") != null) {
                moduleArr.add(Constants.Acc_Customer_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationcustomerparent"))) {
                    parentArr.add(request.getParameter("columncreationcustomerparent"));
                }
            }
            if (request.getParameter("columncreationvendor") != null) {
                moduleArr.add(Constants.Acc_Vendor_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationvendorparent"))) {
                    parentArr.add(request.getParameter("columncreationvendorparent"));
                }
            }
            if (request.getParameter("columncreationdeliveryorder") != null) {
                moduleArr.add(Constants.Acc_Delivery_Order_ModuleId);
                   if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationdeliveryorderparent"))) {
                    parentArr.add(request.getParameter("columncreationdeliveryorderparent"));
            }
                }
            if (request.getParameter("columncreationrequestforquotation") != null) {
                moduleArr.add(Constants.Acc_RFQ_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationrequestforquotationparent"))) {
                    parentArr.add(request.getParameter("columncreationrequestforquotationparent"));
                }
            }
            if (request.getParameter("columncreationgoodsreceipt") != null) {
                moduleArr.add(Constants.Acc_Goods_Receipt_ModuleId);
                   
                   if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationgoodsreceiptparent"))) {
                    parentArr.add(request.getParameter("columncreationgoodsreceiptparent"));
            }
                }
            if (request.getParameter("columncreationsalesreturn") != null) {
                moduleArr.add(Constants.Acc_Sales_Return_ModuleId);
                   
                   if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationsalesreturnparent"))) {
                    parentArr.add(request.getParameter("columncreationsalesreturnparent"));
            }
                }
                if (request.getParameter("columncreationpurchasereturn") != null) {
                    moduleArr.add(Constants.Acc_Purchase_Return_ModuleId);
                    if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationpurchasereturnparent"))) {
                        parentArr.add(request.getParameter("columncreationpurchasereturnparent"));
                    }
                }

            if (request.getParameter("columncreationcustomerquotation") != null) {
                moduleArr.add(Constants.Acc_Customer_Quotation_ModuleId);
                      if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationcustomerquotationparent"))) {
                    parentArr.add(request.getParameter("columncreationcustomerquotationparent"));
            }
                }

            if (request.getParameter("columncreationvendorquotation") != null) {
                moduleArr.add(Constants.Acc_Vendor_Quotation_ModuleId);
                   if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationvendorquotationparent"))) {
                    parentArr.add(request.getParameter("columncreationvendorquotationparent"));
            }
            }  
            if (request.getParameter("columncreationfixedassetsdisposalinvoice") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_DisposalInvoice_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetsdisposalinvoiceparent"))) {
                    parentArr.add(request.getParameter("columncreationfixedassetsdisposalinvoiceparent"));
                }
            }

            if (request.getParameter("columncreationfixedassetspurchaseinvoice") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetspurchaseinvoiceparent"))) {
                    parentArr.add(request.getParameter("columncreationfixedassetspurchaseinvoiceparent"));
                }
            }
            if (request.getParameter("columncreationfixedassetsgoodsreceipt") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_GoodsReceipt_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetsgoodsreceiptparent"))) {
                    parentArr.add(request.getParameter("columncreationfixedassetsgoodsreceiptparent"));
                }
            }

            if (request.getParameter("columncreationfixedassetsdeliveryorder") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_DeliveryOrder_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetsdeliveryorderparent"))) {
                    parentArr.add(request.getParameter("columncreationfixedassetsdeliveryorderparent"));
                }
            }
            if (request.getParameter("columncreationfixedassetsgroups") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_AssetsGroups_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetsgroupsparent"))) {
                    parentArr.add(request.getParameter("columncreationfixedassetsgroupsparent"));
                }
            }
            if (request.getParameter("columncreationfixedassetspurchaserequisition") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetspurchaserequisitionparent"))) {
                    parentArr.add(request.getParameter("columncreationfixedassetspurchaserequisitionparent"));
                }
            }
            if (request.getParameter("columncreationfixedassetsrfq") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_RFQ_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetsrfqparent"))) {
                    parentArr.add(request.getParameter("columncreationfixedassetsrfqparent"));
                }
            }
            if (request.getParameter("columncreationfixedassetsvendorquotation") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetsvendorquotationparent"))) {
                    parentArr.add(request.getParameter("columncreationfixedassetsvendorquotationparent"));
                }
            }
            if (request.getParameter("columncreationfixedassetspurchaseorder") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_Purchase_Order_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetspurchaseorderparent"))) {
                    parentArr.add(request.getParameter("columncreationfixedassetspurchaseorderparent"));
                }
            }

            if (request.getParameter("columncreationleaseorder") != null) {
                moduleArr.add(Constants.Acc_Lease_Order_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationleaseorderparent"))) {
                    parentArr.add(request.getParameter("columncreationleaseorderparent"));
                }
            }
           if (request.getParameter("columncreationcontract") != null) {
                moduleArr.add(Constants.Acc_Contract_Order_ModuleId);
               if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationcontractparent"))) {
                   parentArr.add(request.getParameter("columncreationcontractparent"));
               }
            }
            if (request.getParameter("columncreationproduct") != null) {
                moduleArr.add(Constants.Acc_Product_Master_ModuleId);
                parentArr.add("");
            }

            if (request.getParameter("AccountStatement") != null) {
                moduleArr.add(Constants.Account_Statement_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("AccountStatementParent"))) {
                    parentArr.add(request.getParameter("AccountStatementParent"));
                }
            }

            if (request.getParameter("columncreationserial") != null) {
                moduleArr.add(Constants.SerialWindow_ModuleId);
                parentArr.add("");
            }
            if (request.getParameter("columncreationinventory") != null) {
                moduleArr.add(Constants.Inventory_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationstockissueparent"))) {
                    parentArr.add(request.getParameter("columncreationstockissueparent"));
                }
            }
            if (request.getParameter("columnCreationLeaseInvoice") != null) {
                moduleArr.add(Constants.LEASE_INVOICE_MODULEID);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columnCreationLeaseInvoiceparent"))) {
                    parentArr.add(request.getParameter("columnCreationLeaseInvoiceparent"));
                }
            }
            
            if (request.getParameter("columncreationconsignmentrequest") != null) {
                moduleArr.add(Constants.Acc_ConsignmentRequest_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationconsignmentrequestparent"))) {
                    parentArr.add(request.getParameter("columncreationconsignmentrequestparent"));
                }
            }
            if (request.getParameter("columncreationconsignmentinvoice") != null) {
                moduleArr.add(Constants.Acc_ConsignmentInvoice_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationconsignmentinvoiceparent"))) {
                    parentArr.add(request.getParameter("columncreationconsignmentinvoiceparent"));
                }
            }
            if (request.getParameter("columncreationconsignmentdo") != null) {
                moduleArr.add(Constants.Acc_ConsignmentDeliveryOrder_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationconsignmentdoparent"))) {
                    parentArr.add(request.getParameter("columncreationconsignmentdoparent"));
                }
            }
            if (request.getParameter("columncreationconsignmentsalesreturn") != null) {
                moduleArr.add(Constants.Acc_ConsignmentSalesReturn_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationconsignmentsalesreturnparent"))) {
                    parentArr.add(request.getParameter("columncreationconsignmentsalesreturnparent"));
                }
            }
            if (request.getParameter("columncreationconsignmentvendorrequest") != null) {
                moduleArr.add(Constants.Acc_ConsignmentVendorRequest_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationconsignmentvendorrequestparent"))) {
                    parentArr.add(request.getParameter("columncreationconsignmentvendorrequestparent"));
                }
            }
            if (request.getParameter("columncreationconsignmentpurchaseinvoice") != null) {
                moduleArr.add(Constants.Acc_Consignment_GoodsReceipt_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationconsignmentpurchaseinvoiceparent"))) {
                    parentArr.add(request.getParameter("columncreationconsignmentpurchaseinvoiceparent"));
                }
            }
            if (request.getParameter("columncreationconsignmentgr") != null) {
                moduleArr.add(Constants.Acc_Consignment_GoodsReceiptOrder_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationconsignmentgrparent"))) {
                    parentArr.add(request.getParameter("columncreationconsignmentgrparent"));
                }
            }
            if (request.getParameter("columncreationconsignmentpurchasereturn") != null) {
                moduleArr.add(Constants.Acc_ConsignmentPurchaseReturn_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationconsignmentpurchasereturnparent"))) {
                    parentArr.add(request.getParameter("columncreationconsignmentpurchasereturnparent"));
                }
            }
            if (request.getParameter("columncreationleasecontract") != null) {
                moduleArr.add(Constants.Acc_Lease_Contract);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationleasecontractparent"))) {
                    parentArr.add(request.getParameter("columncreationleasecontractparent"));
                }
            }
            if (request.getParameter("columncreationleasequotation") != null) {
                moduleArr.add(Constants.Acc_Lease_Quotation);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationleasequotationparent"))) {
                    parentArr.add(request.getParameter("columncreationleasequotationparent"));
                }
            }
            if (request.getParameter("columncreationleasedo") != null) {
                moduleArr.add(Constants.Acc_Lease_DO);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationleasedoparent"))) {
                    parentArr.add(request.getParameter("columncreationleasedoparent"));
                }
            }
            if (request.getParameter("columncreationleasereturn") != null) {
                moduleArr.add(Constants.Acc_Lease_Return);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationleasereturnparent"))) {
                    parentArr.add(request.getParameter("columncreationleasereturnparent"));
                }
            }
            if (request.getParameter("columncreationfixedassetspurchasereturn") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_Purchase_Return_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetspurchasereturnparent"))) {
                    parentArr.add(request.getParameter("columncreationfixedassetspurchasereturnparent"));
                }
            }
            if (request.getParameter("columncreationfixedassetssalesreturn") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_Sales_Return_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetssalesreturnparent"))) {
                    parentArr.add(request.getParameter("columncreationfixedassetssalesreturnparent"));
                }
            }
            if (request.getParameter("columncreationfixedassetdetails") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_Details_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetdetailsparent"))) {
                    parentArr.add(request.getParameter("columncreationfixedassetdetailsparent"));
                }
            }
            if (request.getParameter("columncreationinvoiceform") != null) {
                relatedModuleId ="," + String.valueOf(Constants.Acc_Invoice_ModuleId);
                //                    moduleArr.add(Constants.Acc_BillingInvoice_ModuleId);
            }
            if (request.getParameter("columncreationveninvoiceform") != null) {
//                if (StringUtil.isNullOrEmpty(relatedModuleId)) {
//                    moduleArr.add(Constants.Acc_Vendor_Invoice_ModuleId);
//                } else {
                    relatedModuleId += "," + String.valueOf(Constants.Acc_Vendor_Invoice_ModuleId);
//                }
            }
            if (request.getParameter("columncreationsalesorderform") != null) {
//                if (StringUtil.isNullOrEmpty(relatedModuleId)) {
//                    moduleArr.add(Constants.Acc_Sales_Order_ModuleId);
//                    relatedModuleId = String.valueOf(Constants.Acc_Sales_Order_ModuleId);
//                } else {
                    relatedModuleId += "," + String.valueOf(Constants.Acc_Sales_Order_ModuleId);
//                }
            }
            if (request.getParameter("columncreationpurchaseorderform") != null) {
//                if (StringUtil.isNullOrEmpty(relatedModuleId)) {
//                    moduleArr.add(Constants.Acc_Purchase_Order_ModuleId);
//                    relatedModuleId = String.valueOf(Constants.Acc_Purchase_Order_ModuleId);
//                } else {
                    relatedModuleId += "," + String.valueOf(Constants.Acc_Purchase_Order_ModuleId);
//                }
            }
            if (request.getParameter("columncreationdeliveryorderform") != null) {
//                if (StringUtil.isNullOrEmpty(relatedModuleId)) {
//                    moduleArr.add(Constants.Acc_Delivery_Order_ModuleId);
//                    relatedModuleId = String.valueOf(Constants.Acc_Delivery_Order_ModuleId);
//                } else {
                    relatedModuleId += "," + String.valueOf(Constants.Acc_Delivery_Order_ModuleId);
//                }
              }
              
              if (request.getParameter("columncreationgrorderform") != null) {    //add custom field enhancement
//                  if (StringUtil.isNullOrEmpty(relatedModuleId)) {
//                      moduleArr.add(Constants.Acc_Goods_Receipt_ModuleId);
//                  } else {
                      relatedModuleId += "," + String.valueOf(Constants.Acc_Goods_Receipt_ModuleId);
//                  }
              }
              if (request.getParameter("columncreationcustomerquotationform") != null) {
//                  if (StringUtil.isNullOrEmpty(relatedModuleId)) {
//                      moduleArr.add(Constants.Acc_Customer_Quotation_ModuleId);
//                  } else {
                      relatedModuleId += "," + String.valueOf(Constants.Acc_Customer_Quotation_ModuleId);
//                  }
              }
              if (request.getParameter("columncreationvendorquotationform") != null) {
//                  if (StringUtil.isNullOrEmpty(relatedModuleId)) {
//                      moduleArr.add(Constants.Acc_Vendor_Quotation_ModuleId);
//                  } else {
                      relatedModuleId += "," + String.valueOf(Constants.Acc_Vendor_Quotation_ModuleId);
//                  }
              }
              if (request.getParameter("columncreationpurchaserequisitionform") != null) {
//                  if (StringUtil.isNullOrEmpty(relatedModuleId)) {
//                      moduleArr.add(Constants.Acc_Purchase_Requisition_ModuleId);
//                  } else {
                      relatedModuleId += "," + String.valueOf(Constants.Acc_Purchase_Requisition_ModuleId);
//                  }
              }
              if (request.getParameter("columncreationpurchasereturnform") != null) {
                      relatedModuleId += "," + String.valueOf(Constants.Acc_Purchase_Return_ModuleId);
              }
              if (request.getParameter("columncreationsalesreturnform") != null) {
                      relatedModuleId += "," + String.valueOf(Constants.Acc_Sales_Return_ModuleId);
              }
              
              
//              if (request.getParameter("columncreationpoform") != null) {
//                  if (StringUtil.isNullOrEmpty(relatedModuleId)) {
//                      moduleArr.add(Constants.Acc_Purchase_Order_ModuleId);
//                  } else {
//                      relatedModuleId += "," + String.valueOf(Constants.Acc_Purchase_Order_ModuleId);
//                  }
//              }
              if (request.getParameter("columncreationpoform") != null) {
                    relatedModuleId += "," + String.valueOf(Constants.Acc_Purchase_Order_ModuleId);
              }
//                if (request.getParameter("columncreationcontact") != null) {
//                    moduleArr.add(Constants.Crm_contact_moduleid);
//                }
//                if (request.getParameter("columncreationproduct") != null) {
//                    moduleArr.add(Constants.Crm_product_moduleid);
//                }
//                if (request.getParameter("columncreationopportunity") != null) {
//                    moduleArr.add(Constants.Crm_opportunity_moduleid);
//                }
//                if (request.getParameter("columncreationcase") != null) {
//                    moduleArr.add(Constants.Crm_case_moduleid);
//                }
// code for  inventory modules
               if (request.getParameter("columncreationstockadjustment") != null) {
                moduleArr.add(Constants.Inventory_Stock_Adjustment_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationstockadjustmentparent"))) {
                    parentArr.add(request.getParameter("columncreationstockadjustmentparent"));
                }
            }
               if (request.getParameter("columncreationstockrequest") != null) {
                moduleArr.add(Constants.Acc_Stock_Request_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationstockrequestparent"))) {
                    parentArr.add(request.getParameter("columncreationstockrequestparent"));
                }
            }
               if (request.getParameter("columnCreationIST") != null) {
                moduleArr.add(Constants.Acc_InterStore_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columnCreationISTparent"))) {
                    parentArr.add(request.getParameter("columnCreationISTparent"));
                }
            }
               if (request.getParameter("columncreationILT") != null) {
                moduleArr.add(Constants.Acc_InterLocation_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationILTparent"))) {
                    parentArr.add(request.getParameter("columncreationILTparent"));
                }
            }
            if (request.getParameter("columncreationcyclec") != null) {
                moduleArr.add(Constants.Acc_CycleCount_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationcyclecparent"))) {
                    parentArr.add(request.getParameter("columncreationcyclecparent"));
                }
            }
            if (request.getParameter("columncreationlabour") != null) {
                moduleArr.add(Constants.Labour_Master);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationlabourparent"))) {
                    parentArr.add(request.getParameter("columncreationlabourparent"));
                }
            }
            if (request.getParameter("columncreationWorkCenter") != null) {
                moduleArr.add(Constants.MRP_WORK_CENTRE_MODULEID);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationWorkCenterparent"))) {
                    parentArr.add(request.getParameter("columncreationWorkCenterparent"));
                }
            }
            if (request.getParameter("columncreationMachineMaster") != null) {
                moduleArr.add(Constants.MRP_Machine_Management_ModuleId);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationMachineMasterparent"))) {
                    parentArr.add(request.getParameter("columncreationMachineMasterparent"));
                }
            }
            if (request.getParameter("columncreationWorkOrder") != null) {
                moduleArr.add(Constants.MRP_WORK_ORDER_MODULEID);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationWorkOrderparent"))) {
                    parentArr.add(request.getParameter("columncreationWorkOrderparent"));
                }
            }
            if (request.getParameter("columncreationvendorjobworkorder") != null) {
                moduleArr.add(Constants.VENDOR_JOB_WORKORDER_MODULEID);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationvendorjobworkorderparent"))) {
                    parentArr.add(request.getParameter("columncreationvendorjobworkorderparent"));
                }
            }
            if (request.getParameter("columncreationMasterContract") != null) {
                moduleArr.add(Constants.MRP_Contract);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationMasterContractparent"))) {
                    parentArr.add(request.getParameter("columncreationMasterContractparent"));
                }
            }
            if (request.getParameter("columncreationRoutingTemplate") != null) {
                moduleArr.add(Constants.MRP_RouteCode);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationRoutingTemplateparent"))) {
                    parentArr.add(request.getParameter("columncreationRoutingTemplateparent"));
                }
            }
            if (request.getParameter("columncreationjobwork") != null) {
                moduleArr.add(Constants.MRP_JOB_WORK_MODULEID);
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationjobworkparent"))) {
                    parentArr.add(request.getParameter("columncreationjobworkparent"));
                }
            }
            for (int cnt = 0; cnt < moduleArr.size(); cnt++) {//Duplicate Name check
                Integer moduleid = Integer.parseInt(moduleArr.get(cnt).toString());
                String moduleName = getModuleName(moduleid);
                               
                HashMap<String, Object> dupCheckRequestParams = new HashMap<String, Object>();
                dupCheckRequestParams.put(Constants.filter_names, Arrays.asList(Constants.fieldlabel,Constants.companyid, Constants.moduleid));
                dupCheckRequestParams.put(Constants.filter_values, Arrays.asList(fieldlabel,companyid, moduleid));
                
                if("1".equals(request.getParameter("isforeclaim"))) {   //refer ticket ERP-17187
                    isForeClaim = true;
                    dupCheckRequestParams.clear();
                    dupCheckRequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.isforeclaim));
                    dupCheckRequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 1));   //int value 1 required
                }
                  
                KwlReturnObject resultDupCheck = accAccountDAOobj.getFieldParams(dupCheckRequestParams);
                
                if (resultDupCheck.getEntityList().size() > 0) {
                    createField = false;
                    columnExistInModules += moduleName + ", ";
                  }
            }
            if(!StringUtil.isNullOrEmpty(relatedModuleId)){
                 relatedModuleId=relatedModuleId.substring(1,relatedModuleId.length());
            }
            /**
             * Below check is implemented to check weather the User had enabled Allow to edit Products Custom field in various documents where product can be used if it is enabled the setting value 0 in databases.
             * ERP-34804 / ERM-177.
             */
            if (request.getParameter("relatedModuleIsAllowEdit") != null && StringUtil.equalIgnoreCase(request.getParameter("relatedModuleIsAllowEdit"), "on")) {
                                relatedmoduleisallowedit = Constants.ALLOW_TO_EDIT_PRODUCT_CUSTOMFIELD;
            }
            if(isEdit){
                    if(!StringUtil.isNullOrEmpty(ModuleIDs)){
                        String[] ModuleIDArray= ModuleIDs.split(",");
                        String ModuleIds="";
                        String ModuleIdsNew="";
                        for(int Count=0;Count < ModuleIDArray.length; Count++ )
                        {
                            ModuleIds+="'"+ModuleIDArray[Count]+"'";
                            ModuleIdsNew+=ModuleIDArray[Count];
                            if(!(Count== ModuleIDArray.length-1))
                            {
                                ModuleIds+=",";
                                ModuleIdsNew+=",";
                            }
                        }
                        KwlReturnObject fieldlabelDupCheck = accAccountDAOobj.getFieldParamsforEdit(fieldlabel,ModuleIds,companyid);
                        
                        if (fieldlabelDupCheck.getEntityList().size() > 0) {
                            createField = false;
                            for(int i=0;i<fieldlabelDupCheck.getEntityList().size();i++) {
                                fp = (FieldParams) fieldlabelDupCheck.getEntityList().get(i);
                                Integer moduleid = fp.getModuleid();
                                String moduleName = getModuleName(moduleid);
                                columnExistInModules += moduleName + ", ";
                            }
                        } else {            
//                            below fuction took more time to execute and dont need more hence commented
//                            String defaultvalue = request.getParameter("defaultval");
//                            String combodata = request.getParameter("combodata");
//                            for(int Count=0;Count < ModuleIDArray.length; Count++ ) {
//                                insertfieldcombodata(combodata, ModuleIDArray[Count], defaultvalue);
//                            }
                            KwlReturnObject Upadated = accAccountDAOobj.updateCustomColumnmfield(fieldlabel,ModuleIds,lineitem,maxlength,fieldtooltip,defaultval,isAutoPopulateDefaultValue,isForSalesCommission, isForKnockOff);
                            if(!StringUtil.isNullOrEmpty(relatedModuleId)){
                              int Upadate = accAccountDAOobj.updateCustomProductfield(ModuleIds,relatedModuleId,companyid);
                              if(Upadate != 0){
                                  relatedModuleId="";
                              }
                           }
                            /**
                             * Updating the value of Related Module is allowed to edit or not in case of editing the custom or dimension field.
                             * ERP-34804 / ERM-177.
                             */
                              int Upadate = accAccountDAOobj.updateCustomProductfieldIsAllowedToEdit(ModuleIds,relatedmoduleisallowedit,companyid);
                        }
                        String splitModuleId[]=ModuleIdsNew.split(",");
                        for(int Count=0;Count < splitModuleId.length; Count++ )
                        {
                            FieldParams parentFieldParams = (FieldParams) kwlCommonTablesDAOObj.getClassObject(FieldParams.class.getName(), splitModuleId[Count]);
                            String parentId = getParentForModule(parentFieldParams.getModuleid(), request);
                            if (!StringUtil.isNullOrEmpty(parentId)) {
                               KwlReturnObject Upadated = accAccountDAOobj.updateDimensionParent(fieldlabel, splitModuleId[Count], lineitem, parentId);
                            }

                        }  
                    }
            }  
            HashSet fieldIds= new HashSet();
            String values = "";
            String oldValue="";
            if(createField)
                {
                HashMap<String, Object> exisValueRequestParams = new HashMap<String, Object>();
                exisValueRequestParams.put(Constants.filter_names, Arrays.asList(Constants.fieldlabel,Constants.companyid,"customcolumn", "customfield"));
                exisValueRequestParams.put(Constants.filter_values, Arrays.asList(fieldlabel,companyid, lineitem, isCustomField));
                
                KwlReturnObject resultExsistValueCheck = accAccountDAOobj.getFieldParams(exisValueRequestParams);
                List list =resultExsistValueCheck.getEntityList();
               KwlReturnObject allValue  = accMasterItemsDAOobj.getValueIfFieldInOtherModule(list,"fieldid");
               List ls = allValue.getEntityList();
               Iterator it = ls.iterator();
                values = request.getParameter("combodata");
                oldValue="";
               String columnExistInModule="" ;
               if(request.getParameter("fieldType").equals("4"))
                while(it.hasNext())
               {
                     Object temp[] = (Object[]) it.next();
                     if(fieldIds.add(temp[2].toString()));
                     {
                        if (columnExistInModule != "") {
                           columnExistInModule = columnExistInModule + ", ";
                        } 
                        columnExistInModule = columnExistInModule + getModuleName(Integer.parseInt(temp[1].toString()));
                     }
               } 
                allValue  = accMasterItemsDAOobj.getValueIfFieldInOtherModule(list,"value");
                ls = allValue.getEntityList();
                it = ls.iterator();
               while(it.hasNext())
               {
                        Object temp[] = (Object[]) it.next();
                         if (oldValue != "") {
                           oldValue = oldValue + ";";
                        } 
                        oldValue = oldValue + temp[0].toString();
//                        createField = false;
                        resultJson.put("sms", " Column/Dimension name '" + fieldlabel + "' already present in " + columnExistInModule + " with value " + oldValue + ". New value will be included in same dimension with the existing one ");
                  }
               if(values.replace(";", "").contains(oldValue.replace(";", "")))
               {
                   createField=true;
               }
                if(isEdit){
                    resultJson.put("values1",  oldValue);
                }    
                else{
                     resultJson.put("values1",  oldValue+";"+values.toString());
                }
                    
                }
            if (createField) {
//               if(!fieldIds.isEmpty())
//               accMasterItemsDAOobj.insertNewValues( fieldIds,values,oldValue);
                HashMap<Integer, HashMap<String, Object>> modulerequestParams = new HashMap<Integer, HashMap<String, Object>>();
                HashMap<String, Object> requestParams = null;
                ArrayList<String> ll = new ArrayList<String>();
                int count = 0;
                for (int cnt = 0; cnt < moduleArr.size(); cnt++) {//Create new field
                    count++;
                    Integer moduleid = Integer.parseInt(moduleArr.get(cnt).toString());
                    String parentid = "";
                    if(!parentArr.isEmpty() && parentArr.get(cnt)!=null){
                        parentid=parentArr.get(cnt).toString();
                    }
                    String relatedmoduleid="";
                    if(moduleid==Constants.Acc_Product_Master_ModuleId){
                        relatedmoduleid=relatedModuleId;
                    }
                    requestParams = processrequest(request, moduleArr.get(cnt).toString(),relatedmoduleid,parentid,propagateTOChildCompaniesFalg,childCompanyID,parentFieldParamID);//*********************************************
                    modulerequestParams.put(moduleid, requestParams);
                }
                if (moduleArr.size() > 0) {
                    /*
                     *Batch update existing records with default value in thread
                     */
                    accAccountService.UpdateExistingRecordsWithDefaultValue(modulerequestParams, moduleArr, companyid);
                    ll.add(requestParams.get("response").toString());
                }
                String action="added";
                if(isEdit==true)
                {
                    action="updated";
                }
                String fieldType="dimension";
                String auditaction=(action.equalsIgnoreCase("added")?AuditAction.DIMENTION_ADDED:AuditAction.DIMENTION_UPDATED);
                if(isCustomField==1)
                        {
                            if(lineitem==1)
                            {
                                fieldType="custom column";
                                auditaction=(action.equalsIgnoreCase("added")?AuditAction.CUSTOM_COLUMN_ADDED:AuditAction.CUSTOM_COLUMN_UPDATED);
                            }
                            else
                            {
                                fieldType="custom field";
                                 auditaction=(action.equalsIgnoreCase("added")?AuditAction.CUSTOM_FIELD_ADDED:AuditAction.CUSTOM_FIELD_UPDATED);
                            }
                            
                        }           
                auditTrailObj.insertAuditLog(auditaction, "User " + sessionHandlerImpl.getUserFullName(request) + " has "+action+" "+fieldType+" "+fieldlabel +" in child company", request, "0");
                txnManager.commit(status);
                resultJson.put("sucess", ll);
                resultJson.put(Constants.moduleid, moduleArr);

            } else {
                resultJson.put(Constants.RES_success, Constants.RES_msg);
                resultJson.put("title", "Alert");
                if (columnExistInModules.length() > 0) {
                    columnExistInModules = columnExistInModules.trim();
                    columnExistInModules = columnExistInModules.substring(0, columnExistInModules.length() - 1);
                }
                String CannotaddnewForeClaim = messageSource.getMessage("acc.master.configuration.CannotaddnewForeClaim", null, RequestContextUtils.getLocale(request));
                String Cannotaddnew = messageSource.getMessage("acc.master.configuration.Cannotaddnew", null, RequestContextUtils.getLocale(request));
                resultJson.put(Constants.RES_msg, (isForeClaim ? CannotaddnewForeClaim : Cannotaddnew) + columnExistInModules);
                resultJson.put("duplicateflag", true);
                txnManager.rollback(status);
            }

        } catch (SessionExpiredException ex) {
            logger.warn(ex.getMessage(), ex);
            txnManager.rollback(status);
        } catch (JSONException ex) {
            logger.warn(ex.getMessage(), ex);
            txnManager.rollback(status);
        } catch (Exception ex) {
            logger.warn(ex.getMessage(), ex);
            txnManager.rollback(status);
        }
    }
    public ModelAndView saveCustomFieldActivation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject obj = new JSONObject();        
        boolean sucess = false;
        String msg="";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("IC_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            saveCustomFieldActivation(request);
            sucess = true;
            msg="You have save changes sucessfully.";
            txnManager.commit(status);
        }catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            logger.warn(ex.getMessage(), ex);
            txnManager.rollback(status);
        } finally {
            try {
                obj.put("sucess", sucess);              
                obj.put("msg", msg);              
            } catch (JSONException ex) {
                Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", obj.toString());
    }

    public void saveCustomFieldActivation(HttpServletRequest request) throws ServiceException, SessionExpiredException{
        try {
            ArrayList moduleArr = new ArrayList();
            String fieldLabel = "",id = "";
            String ModuleIDs = request.getParameter("moduleID");
            moduleArr=getCustomFieldModulesArray(request);
                                   
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            String [] moduleids=ModuleIDs.split(",");
            for(int i=0;i<moduleids.length;i++){
                List<String> childIdList = new ArrayList<String>();
                KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(FieldParams.class.getName(), moduleids[i]);                
                FieldParams fieldParams=(FieldParams)custumObjresult.getEntityList().get(0);
                fieldLabel = fieldParams.getFieldlabel();
                id = fieldParams.getId();
                requestParams.put(Constants.filter_names,  Arrays.asList( Constants.moduleid, Constants.companyid));
                requestParams.put(Constants.filter_values,  Arrays.asList( fieldParams.getModuleid(),fieldParams.getCompanyid()));
                requestParams.put("parentid", fieldParams.getId());
                
                /**
                 * Get all the leaves of selected parent field. 
                 */
                childIdList = getChildIds(requestParams,childIdList);
                Iterator<String> ChildsArrayiterator = childIdList.iterator();
                  
                if(fieldParams!=null){
                   int activation=0;
                   if(moduleArr.contains(fieldParams.getModuleid())) {
                       activation=1;
                   }                    
                    KwlReturnObject Upadated = accAccountDAOobj.updateCustomFieldActivation(activation,moduleids[i]);
                    
                    /**
                     *  Iterate every child and set Activate/Deactivate based on Parent.
                     */
                    while (ChildsArrayiterator.hasNext()) {
                        Object child = ChildsArrayiterator.next();
                        KwlReturnObject childUpdated = accAccountDAOobj.updateCustomFieldActivation(activation, child.toString());
                    }
                }                                
            }
            auditTrailObj.insertAuditLog(AuditAction.ACTIVATE_DEACTIVATE_CUSTOM_FIELD_DIMENSION, "User " + sessionHandlerImpl.getUserFullName(request) + " has updated Dimension/Custom field : <b>" + fieldLabel + "</b>", request, id);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
    private ArrayList getCustomFieldModulesArray(HttpServletRequest request) {
         ArrayList moduleArr = new ArrayList();
            if (request.getParameter("columncreationinvoice") != null) {
                moduleArr.add(Constants.Acc_Invoice_ModuleId);
            }
            if (request.getParameter("columncreationveninvoice") != null) {
                moduleArr.add(Constants.Acc_Vendor_Invoice_ModuleId);
            }
            if (request.getParameter("columncreationdebitnote") != null) {
                moduleArr.add(Constants.Acc_Debit_Note_ModuleId);
            }
            if (request.getParameter("columncreationcreditnote") != null) {
                moduleArr.add(Constants.Acc_Credit_Note_ModuleId);
            }
            if (request.getParameter("columncreationmakepayment") != null) {
                moduleArr.add(Constants.Acc_Make_Payment_ModuleId);
            }
            if (request.getParameter("columncreationreceivepayment") != null) {
                moduleArr.add(Constants.Acc_Receive_Payment_ModuleId);
            }
            if (request.getParameter("columncreationgeneraletry") != null) {
                moduleArr.add(Constants.Acc_GENERAL_LEDGER_ModuleId);
            }
            if (request.getParameter("columncreationpurchaseorderid") != null) {
                moduleArr.add(Constants.Acc_Purchase_Order_ModuleId);
            }
            if (request.getParameter("columncreationsalesorderid") != null) {
                moduleArr.add(Constants.Acc_Sales_Order_ModuleId);
            }
            if (request.getParameter("columncreationcustomerquotationid") != null) {
                moduleArr.add(Constants.Acc_Customer_Quotation_ModuleId);
            }

            if (request.getParameter("columncreationvendorquotationid") != null) {
                moduleArr.add(Constants.Acc_Vendor_Quotation_ModuleId);
            }
            if (request.getParameter("columncreationpurchaserequisition") != null) {
                moduleArr.add(Constants.Acc_Purchase_Requisition_ModuleId);
            }
            if (request.getParameter("columncreationcustomer") != null) {
                moduleArr.add(Constants.Acc_Customer_ModuleId);
            }
            if (request.getParameter("columncreationvendor") != null) {
                moduleArr.add(Constants.Acc_Vendor_ModuleId);
            }
            if (request.getParameter("columncreationdeliveryorder") != null) {
                moduleArr.add(Constants.Acc_Delivery_Order_ModuleId);
            }
            if (request.getParameter("columncreationrequestforquotation") != null) {
                moduleArr.add(Constants.Acc_RFQ_ModuleId);
            }
            if (request.getParameter("columncreationgoodsreceipt") != null) {
                moduleArr.add(Constants.Acc_Goods_Receipt_ModuleId);
            }
            if (request.getParameter("columncreationsalesreturn") != null) {
                moduleArr.add(Constants.Acc_Sales_Return_ModuleId);
            }
            if (request.getParameter("columncreationpurchasereturn") != null) {
                moduleArr.add(Constants.Acc_Purchase_Return_ModuleId);
            }

            if (request.getParameter("columncreationcustomerquotation") != null) {
                moduleArr.add(Constants.Acc_Customer_Quotation_ModuleId);
            }

            if (request.getParameter("columncreationvendorquotation") != null) {
                moduleArr.add(Constants.Acc_Vendor_Quotation_ModuleId);
            }
            if (request.getParameter("columncreationfixedassetsdisposalinvoice") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_DisposalInvoice_ModuleId);
            }

            if (request.getParameter("columncreationfixedassetspurchaseinvoice") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId);
            }
            if (request.getParameter("columncreationfixedassetsgoodsreceipt") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_GoodsReceipt_ModuleId);
            }

            if (request.getParameter("columncreationfixedassetsdeliveryorder") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_DeliveryOrder_ModuleId);
            }
            if (request.getParameter("columncreationfixedassetsgroups") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_AssetsGroups_ModuleId);
            }
            if (request.getParameter("columncreationfixedassetspurchaserequisition") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId);
            }
            if (request.getParameter("columncreationfixedassetsrfq") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_RFQ_ModuleId);
            }
            if (request.getParameter("columncreationfixedassetsvendorquotation") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId);
            }
            if (request.getParameter("columncreationfixedassetspurchaseorder") != null) {
                moduleArr.add(Constants.Acc_FixedAssets_Purchase_Order_ModuleId);
            }

            if (request.getParameter("columncreationleaseorder") != null) {
                moduleArr.add(Constants.Acc_Lease_Order_ModuleId);
            }
            if (request.getParameter("columncreationcontract") != null) {
                moduleArr.add(Constants.Acc_Contract_Order_ModuleId);
            }
            if (request.getParameter("columncreationproduct") != null) {
                moduleArr.add(Constants.Acc_Product_Master_ModuleId);
            }

            if (request.getParameter("AccountStatement") != null) {
                moduleArr.add(Constants.Account_Statement_ModuleId);
            }
            if (request.getParameter("columncreationconsignmentrequest") != null) {
                moduleArr.add(Constants.Acc_ConsignmentRequest_ModuleId);
            }
            if (request.getParameter("columncreationconsignmentinvoice") != null) {
                moduleArr.add(Constants.Acc_ConsignmentInvoice_ModuleId);
            }
            if (request.getParameter("columncreationconsignmentdo") != null) {
                moduleArr.add(Constants.Acc_ConsignmentDeliveryOrder_ModuleId);
            }
            if (request.getParameter("columncreationconsignmentsalesreturn") != null) {
                moduleArr.add(Constants.Acc_ConsignmentSalesReturn_ModuleId);
            }
            if (request.getParameter("columncreationconsignmentvendorrequest") != null) {
                moduleArr.add(Constants.Acc_ConsignmentVendorRequest_ModuleId);
            }
            if (request.getParameter("columncreationconsignmentpurchaseinvoice") != null) {
                moduleArr.add(Constants.Acc_Consignment_GoodsReceipt_ModuleId);
            }
            if (request.getParameter("columncreationconsignmentgr") != null) {
                moduleArr.add(Constants.Acc_Consignment_GoodsReceiptOrder_ModuleId);
            }
            if (request.getParameter("columncreationconsignmentpurchasereturn") != null) {
                moduleArr.add(Constants.Acc_ConsignmentPurchaseReturn_ModuleId);
            }
        if (request.getParameter("columncreationleasecontract") != null) {
            moduleArr.add(Constants.Acc_Lease_Contract);
        }

        if (request.getParameter("columncreationleasequotation") != null) {
            moduleArr.add(Constants.Acc_Lease_Quotation);
        }
        if (request.getParameter("columnCreationLeaseInvoice") != null) {
            moduleArr.add(Constants.LEASE_INVOICE_MODULEID);
        }
        if (request.getParameter("columncreationleasedo") != null) {
            moduleArr.add(Constants.Acc_Lease_DO);
        }
        if (request.getParameter("columncreationleasereturn") != null) {
            moduleArr.add(Constants.Acc_Lease_Return);
        }
        if (request.getParameter("columncreationfixedassetdetails") != null) {
            moduleArr.add(Constants.Acc_FixedAssets_Details_ModuleId);
        }
        if (request.getParameter("columncreationlabour") != null) {
            moduleArr.add(Constants.Labour_Master);
        }
        if (request.getParameter("columncreationWorkCenter") != null) {
            moduleArr.add(Constants.MRP_WORK_CENTRE_MODULEID);
        }
        if (request.getParameter("columncreationMachineMaster") != null) {
            moduleArr.add(Constants.MRP_Machine_Management_ModuleId);
        }
        if (request.getParameter("columncreationWorkOrder") != null) {
            moduleArr.add(Constants.MRP_WORK_ORDER_MODULEID);
        }
        if (request.getParameter("columncreationvendorjobworkorder") != null) {
            moduleArr.add(Constants.VENDOR_JOB_WORKORDER_MODULEID);
        }
        if (request.getParameter("columncreationcyclec") != null) {
            moduleArr.add(Constants.Acc_CycleCount_ModuleId);
        }
        if (request.getParameter("columncreationMasterContract") != null) {
            moduleArr.add(Constants.MRP_Contract);
        }
        if (request.getParameter("columncreationRoutingTemplate") != null) {
            moduleArr.add(Constants.MRP_RouteCode);
        }
        if (request.getParameter("columncreationjobwork") != null) {
            moduleArr.add(Constants.MRP_JOB_WORK_MODULEID);
        }
        if (request.getParameter("columncreationinventory") != null) {
            moduleArr.add(Constants.Inventory_ModuleId);
        }
        if (request.getParameter("columncreationstockrequest") != null) {
            moduleArr.add(Constants.Acc_Stock_Request_ModuleId);
        }
        if (request.getParameter("columncreationstockadjustment") != null) {
            moduleArr.add(Constants.Acc_Stock_Adjustment_ModuleId);
        }
        if (request.getParameter("columnCreationIST") != null) {
            moduleArr.add(Constants.Acc_InterStore_ModuleId);
        }
        if (request.getParameter("columncreationILT") != null) {
            moduleArr.add(Constants.Acc_InterLocation_ModuleId);
        }
        if (request.getParameter("columncreationcyclec") != null) {
            moduleArr.add(Constants.Acc_CycleCount_ModuleId);
        }
          return moduleArr;
    }
    
     public String getParentForModule(int moduleid, HttpServletRequest request) {// flag value - IF false then its for dimension otherwise for custom column
        String parentId = null;
        switch (moduleid) {
            case 2:
                
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationinvoiceparent"))) {
                    parentId = request.getParameter("columncreationinvoiceparent");
                }
                break;
            case 4:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationinvoiceparent"))) {
                    parentId = request.getParameter("columncreationinvoiceparent");
                }
                break;
            case 6:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationveninvoiceparent"))) {
                    parentId = request.getParameter("columncreationveninvoiceparent");
                }
                break;
            case 8:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationveninvoiceparent"))) {
                    parentId = request.getParameter("columncreationveninvoiceparent");
                }
                break;
            case 10:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationdebitnoteparent"))) {
                    parentId = request.getParameter("columncreationdebitnoteparent");
                }

                break;
            case 12:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationdebitnoteparent"))) {
                    parentId = request.getParameter("columncreationdebitnoteparent");
                }

                break;
            case 14:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationmakepaymentparent"))) {
                    parentId = request.getParameter("columncreationmakepaymentparent");
                }

                break;
            case 16:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationreceivepaymentparent"))) {
                    parentId = request.getParameter("columncreationreceivepaymentparent");
                }
                
                break;
            case 18:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationpurchaseorderparent"))) {
                    parentId = request.getParameter("columncreationpurchaseorderparent");
                }
                
                break;
            case 20:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationsalesorderparent"))) {
                    parentId = request.getParameter("columncreationsalesorderparent");
                }
                
                break;
            case Constants.Acc_SecurityGateEntry_ModuleId :
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationsecgateentryparent"))) {
                    parentId = request.getParameter("columncreationsecgateentryparent");
                }
                break;
            case 22:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationcustomerquotationparent"))) {
                    parentId = request.getParameter("columncreationcustomerquotationparent");
                }
                
                break;
            case 23:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationvendorquotationparent"))) {
                    parentId = request.getParameter("columncreationvendorquotationparent");
                }
                
                break;
            case 27:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationdeliveryorderparent"))) {
                    parentId = request.getParameter("columncreationdeliveryorderparent");
                }
                
                break;
            case 28:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationgoodsreceiptparent"))) {
                    parentId = request.getParameter("columncreationgoodsreceiptparent");
                }
                
                break;
            case 29:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationsalesreturnparent"))) {
                    parentId = request.getParameter("columncreationsalesreturnparent");
                }
                
                break;
            case 30:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationpurchasereturnparent"))) {
                    parentId = request.getParameter("columncreationpurchasereturnparent");
                }
             break;
            case 33:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationrequestforquotationparent"))) {
                    parentId = request.getParameter("columncreationrequestforquotationparent");
                }
                break;
            case 34:
                if (!StringUtil.isNullOrEmpty(request.getParameter("AccountStatementParent"))) {
                    parentId = request.getParameter("AccountStatementParent");
                }
                break;
            case 38:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetsdisposalinvoiceparent"))) {
                    parentId = request.getParameter("columncreationfixedassetsdisposalinvoiceparent");
                }

                break;
            case 39:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetspurchaseinvoiceparent"))) {
                    parentId = request.getParameter("columncreationfixedassetspurchaseinvoiceparent");
                }

                break;
            case 40:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetsgoodsreceiptparent"))) {
                    parentId = request.getParameter("columncreationfixedassetsgoodsreceiptparent");
                }

                break;
            case 41:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetsdeliveryorderparent"))) {
                    parentId = request.getParameter("columncreationfixedassetsdeliveryorderparent");
                }
                break;
            case 42:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetsgroupsparent"))) {
                    parentId = request.getParameter("columncreationfixedassetsgroupsparent");
                }
                break;
            case 87:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetspurchaserequisitionparent"))) {
                    parentId = request.getParameter("columncreationfixedassetspurchaserequisitionparent");
                }
                break;
            case 88:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetsrfqparent"))) {
                    parentId = request.getParameter("columncreationfixedassetsrfqparent");
                }
                break;
            case 89:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetsvendorquotationparent"))) {
                    parentId = request.getParameter("columncreationfixedassetsvendorquotationparent");
                }
                break;
            case 90:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationfixedassetspurchaseorderparent"))) {
                    parentId = request.getParameter("columncreationfixedassetspurchaseorderparent");
                }
                break;
            case 50:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationconsignmentrequestparent"))) {
                    parentId = request.getParameter("columncreationconsignmentrequestparent");
                }

                break;
            case 52:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationconsignmentinvoiceparent"))) {
                    parentId = request.getParameter("columncreationconsignmentinvoiceparent");
                }
                break;
            case 51:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationconsignmentdoparent"))) {
                    parentId = request.getParameter("columncreationconsignmentdoparent");
                }
                break;
            case 53:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationconsignmentsalesreturnparent"))) {
                    parentId = request.getParameter("columncreationconsignmentsalesreturnparent");
                }
                break;
            case 63:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationconsignmentvendorrequestparent"))) {
                    parentId = request.getParameter("columncreationconsignmentvendorrequestparent");
                }
                break;
            case 58:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationconsignmentpurchaseinvoiceparent"))) {
                    parentId = request.getParameter("columncreationconsignmentpurchaseinvoiceparent");
                }
                break;
            case 57:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationconsignmentgrparent"))) {
                    parentId = request.getParameter("columncreationconsignmentgrparent");
                }
                break;
            case 59:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationconsignmentpurchasereturnparent"))) {
                    parentId = request.getParameter("columncreationconsignmentpurchasereturnparent");
                }
                break;
            case 36:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationleaseorderparent"))) {
                    parentId = request.getParameter("columncreationleaseorderparent");
                }
                break;
            case 64:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationleasecontractparent"))) {
                    parentId = request.getParameter("columncreationleasecontractparent");
                }
                break;
            case 65:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationleasequotationparent"))) {
                    parentId = request.getParameter("columncreationleasequotationparent");
                }
                break;
            case 93:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columnCreationLeaseInvoiceparent"))) {
                    parentId = request.getParameter("columnCreationLeaseInvoiceparent");
                }
                break;
            case 67:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationleasedoparent"))) {
                    parentId = request.getParameter("columncreationleasedoparent");
                }
                break;
            case 68:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationleasereturnparent"))) {
                    parentId = request.getParameter("columncreationleasereturnparent");
                }
                break;
            case Constants.Labour_Master:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationlabourparent"))) {
                    parentId = request.getParameter("columncreationlabourparent");
                }
                break;
            case Constants.MRP_WORK_CENTRE_MODULEID:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationWorkCenterparent"))) {
                    parentId = request.getParameter("columncreationWorkCenterparent");
                }
                break;
            case Constants.MRP_Machine_Management_ModuleId:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationMachineMasterparent"))) {
                    parentId = request.getParameter("columncreationMachineMasterparent");
                }
                break;
            case Constants.MRP_JOB_WORK_MODULEID:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationjobworkparent"))) {
                    parentId = request.getParameter("columncreationjobworkparent");
                }
                break;
            case Constants.MRP_WORK_ORDER_MODULEID:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationWorkOrderparent"))) {
                    parentId = request.getParameter("columncreationWorkOrderparent");
                }
                break;
            case Constants.VENDOR_JOB_WORKORDER_MODULEID:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationvendorjobworkorderparent"))) {
                    parentId = request.getParameter("columncreationvendorjobworkorderparent");
                }
                break;
            case Constants.Acc_CycleCount_ModuleId:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationcyclecparent"))) {
                    parentId = request.getParameter("columncreationcyclecparent");
                }
                break;
            case Constants.MRP_Contract:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationMasterContractparent"))) {
                    parentId = request.getParameter("columncreationMasterContractparent");
                }
                break;
            case Constants.MRP_RouteCode:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationRoutingTemplateparent"))) {
                    parentId = request.getParameter("columncreationRoutingTemplateparent");
                }
                break;
            case Constants.Acc_Multi_Entity_Dimension_MODULEID:
                if (!StringUtil.isNullOrEmpty(request.getParameter("columncreationmultientitydimensionparent"))) {
                    parentId = request.getParameter("columncreationmultientitydimensionparent");
                }
                break;    
        }
        return parentId;
    }
    
     
     public ModelAndView getVenorCustomerForCombo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONArray jArr=new JSONArray();
        boolean issuccess = true;
        boolean isCOA=false;
        boolean headerAdded=false;
        boolean consolidateAccMapFlag=false;
        boolean levelFlag=false;
        boolean isFixedAsset=false;
        String msg = "";
        try{
            consolidateAccMapFlag =Boolean.parseBoolean((String)request.getParameter("consolidateAccMapFlag"));
            levelFlag =Boolean.parseBoolean((String)request.getParameter("levelFlag"));
            String companyid=sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> requestParams = accAccountHandler.getRequestMap(request);
            requestParams.put("templateid", request.getParameter("templateid"));
            String excludeaccountid = (String) requestParams.get("accountid");
            String includeaccountid = (String) requestParams.get("includeaccountid");
            String includeparentid = (String) requestParams.get("includeparentid");
            String currencyid=(String)requestParams.get("currencyid");
            KWLCurrency currency = (KWLCurrency)kwlCommonTablesDAOObj.getClassObject("com.krawler.common.admin.KWLCurrency", currencyid);
            KwlReturnObject result = accVendorDAOobj.getVendorsForCombo(requestParams);
            
            boolean receivableAccFlag=request.getParameter("receivableAccFlag")!=null?Boolean.parseBoolean(request.getParameter("receivableAccFlag")):false;
            List list = result.getEntityList();
//            int level=0;
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
                obj.put("acccode", StringUtil.isNullObject(row[2]) ? "" : row[2]);
                obj.put("accid", StringUtil.isNullObject(row[0]) ? "" : row[0]);
                obj.put("accname", StringUtil.isNullObject(row[15]) ? "" : row[15]);
                obj.put("accountid", StringUtil.isNullObject(row[18]) ? "" : row[18]);
                obj.put("isVendor", true);
                obj.put("groupname", StringUtil.isNullObject(row[46]) ? "" : row[46]);
                obj.put("currencyid", StringUtil.isNullObject(row[19]) ? "" : row[18]);
                obj.put("currencysymbol", StringUtil.isNullObject(row[42]) ? "" : row[42]);
                obj.put("currencyname", StringUtil.isNullObject(row[43]) ? "" : row[43]);
                obj.put("taxId", StringUtil.isNullObject(row[20]) ? "" : row[20]);
//                obj.put("level", row[3]);
                if (!receivableAccFlag) {
                    obj.put("billto", StringUtil.isNullObject(row[36]) ? "" : row[36]);
                    obj.put("email", StringUtil.isNullObject(row[37]) ? "" : row[37]);
                    obj.put("termdays", StringUtil.isNullObject(row[44]) ? "" : row[44]);
                    obj.put("deleted", StringUtil.isNullObject(row[47]) ? "" : row[47]);
                }

                jArr.put(obj);
            }
            
            Map<String, Object> customerBillingAddressDetailsMap = null;
            if(!receivableAccFlag){
                HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
                addrRequestParams.put("isBillingAddress", true);
                addrRequestParams.put("isDefaultAddress", true);
                customerBillingAddressDetailsMap = accountingHandlerServiceObj.getCustomerAddressDetailsMap(addrRequestParams);
            }
            
            result = accCustomerDAOobj.getCustomersForCombo(requestParams);

            list = result.getEntityList();
            
            for (Object customer : list) {
                Object[] row = (Object[]) customer;
                if (excludeaccountid != null && row[0] != null && row[0].equals(excludeaccountid)) {
                    continue;
                }
                if ((includeparentid != null && row[0] != null && (!row[0].equals(includeparentid) || (row[1] != null && !row[1].equals(includeparentid))))) {
                    continue;
                } else if ((includeaccountid != null && row[0] != null && !row[0].equals(includeaccountid))) {
                    continue;
                }

                JSONObject obj = new JSONObject();
                obj.put("accid", row[0] != null ? row[0] : "");
                obj.put("acccode", row[2] != null ? row[2] : "");
                obj.put("accountid", row[4] != null ? row[4] : "");
                obj.put("accname", row[5] != null ? row[5] : "");
                obj.put("isVendor", false);
                obj.put("currencyid", row[33] != null ? row[33] : "");
                obj.put("currencysymbol", row[34] != null ? row[34] : "");
                obj.put("currencyname", row[35] != null ? row[35] : "");
                obj.put("taxId", row[7] != null ? row[7] : "");
                if(!receivableAccFlag){
                    obj.put("masterSalesPerson", row[19] != null ? row[19] : "");
                    obj.put("groupname", row[36] != null ? row[36] : "");
                    obj.put("termdays", row[37] != null ? row[37] : "");
                    
                    if(customerBillingAddressDetailsMap != null && row[0] != null && customerBillingAddressDetailsMap.containsKey(row[0].toString())) {
                        Object[] addressDetails = (Object[]) customerBillingAddressDetailsMap.get(row[0].toString());
                        obj.put("billto", addressDetails[0]);
                    }
                    obj.put("email", row[21] != null ? row[21] : "");
                    obj.put("deleted", row[40] != null ? row[40] : "");
                }
                jArr.put(obj);
            }
            
            
            
            jobj.put("data", jArr);
            jobj.put("totalCount", result.getRecordTotalCount());
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ex.getMessage();
        } catch(Exception ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = ""+ex.getMessage();
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
      public int getCustLevel(Customer customer, int level) throws ServiceException {
        if(customer.getParent() != null) {
            level++;
            level = getCustLevel(customer.getParent(), level);
        }
        return level;
    }
      public int getVendorLevel(Vendor vendor, int level) throws ServiceException {
        if(vendor.getParent() != null) {
            level++;
            level = getVendorLevel(vendor.getParent(), level);
        }
        return level;
    }
        public String getMasterGroupID(String groupID) throws ServiceException{
    	Map<String,List<String>> masterGroupList=new HashMap<String, List<String>>();
        masterGroupList.put("Asset",Constants.assetGroupList);
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
                      if(aString.equalsIgnoreCase(groupID)){
                          return value.get(0);
                      }
//                      i++;
//                      System.out.println(i+"-> key : " + key + " value : " + aString);
                  }
              }
              Group group =(Group) kwlCommonTablesDAOObj.getClassObject(Group.class.getName(),groupID);
              if(group.getParent()!=null){
                  return getMasterGroupID(group.getParent().getID());
              }else{
                  Group parentGroup=null;
                  if(group.getNature()==Constants.Asset){
                      parentGroup =(Group) kwlCommonTablesDAOObj.getClassObject(Group.class.getName(),Constants.assetGroupList.get(0));
                  }else if(group.getNature()==Constants.Expences){
                      parentGroup =(Group) kwlCommonTablesDAOObj.getClassObject(Group.class.getName(),Constants.expenseGroupList.get(0));
                  }else if(group.getNature()==Constants.Income){
                      parentGroup =(Group) kwlCommonTablesDAOObj.getClassObject(Group.class.getName(),Constants.incomeGroupList.get(0));
                  }else{
                      parentGroup =(Group) kwlCommonTablesDAOObj.getClassObject(Group.class.getName(),Constants.liabilityGroupList.get(0));
                  }          
                  group.setParent(parentGroup);
                  List<Group> list=accAccountDAOobj.updateParentGroup(group);
                  if(!list.isEmpty()){
                      Group updatedGroup=list.get(0);
                      return getMasterGroupID(updatedGroup.getParent().getID());
                  }else{
                      return String.valueOf(group.getNature());              
              }              
              }              
          } catch (Exception ex) {              
              throw ServiceException.FAILURE("getMasterGroupID : " + ex.getMessage(), ex);              
          }    	
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
                moduleName = "Customer Master";
                break;
            case (Constants.Acc_Vendor_ModuleId):
                moduleName = "Vendor Master";
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
            case (Constants.Acc_Lease_Order_ModuleId):
                moduleName = "Lease Order";
                break; 
            case (Constants.Acc_Contract_Order_ModuleId):
                moduleName = "Contract Order";
                break;  
            case (Constants.Acc_FixedAssets_DisposalInvoice_ModuleId):
                moduleName = "FA Disposal Invoice";
                break;
            case (Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId):
                moduleName = "FA Purchase Invoice";
                break;    
            case (Constants.Acc_FixedAssets_GoodsReceipt_ModuleId):
                moduleName = "FA Goods Receipt";
                break; 
            case (Constants.Acc_FixedAssets_DeliveryOrder_ModuleId):
                moduleName = "FA Delivery Order";
                break; 
            case (Constants.Acc_FixedAssets_AssetsGroups_ModuleId):
                moduleName = "FA Assets Group";
                break;
            case (Constants.Acc_FixedAssets_Sales_Return_ModuleId):
                moduleName = "FA Sales Return";
                break;
            case (Constants.SerialWindow_ModuleId):
                moduleName = "Serial Window";
                break; 
            case (Constants.Inventory_ModuleId):
                moduleName = "Inventory Window";
                break; 
            case (Constants.Acc_ConsignmentRequest_ModuleId):
                moduleName = "Consignment Request";
                break;
            case (Constants.Acc_ConsignmentDeliveryOrder_ModuleId):
                moduleName = "Consignment DeliveryOrder";
                break;
            case (Constants.Acc_ConsignmentInvoice_ModuleId):
                moduleName = "Consignment Invoice";
                break;
            case (Constants.Acc_ConsignmentSalesReturn_ModuleId):
                moduleName = "Consignment SalesReturn";
                break;
            case (Constants.Acc_Consignment_GoodsReceiptOrder_ModuleId):
                moduleName = "Consignment Goods Receipt Order";//no dependency in other modules.
                break;
            case (Constants.Acc_Consignment_GoodsReceipt_ModuleId):
                moduleName = "Consignment GoodsReceipt";
                break;
            case (Constants.Acc_ConsignmentPurchaseReturn_ModuleId):
                moduleName = "Consignment PurchaseReturn";
                break;
            case (Constants.Acc_ConsignmentVendorRequest_ModuleId):
                moduleName = "Consignment VendorRequest";
                break;                 
            case (Constants.LEASE_INVOICE_MODULEID):
                moduleName = "Lease Invoice";
                break;                 
            case (Constants.Acc_FixedAssets_PurchaseRequisition_ModuleId):
                moduleName = "FA Purchase Requisition";
                break;             
            case (Constants.Acc_FixedAssets_RFQ_ModuleId):
                moduleName = "FA RFQ";
                break;             
            case (Constants.Acc_FixedAssets_Vendor_Quotation_ModuleId):
                moduleName = "FA Vendor Quotation";
                break;                 
            case (Constants.Acc_FixedAssets_Purchase_Order_ModuleId):
                moduleName = "FA Purchase Order";
                break; 
            case (Constants.Acc_Lease_Contract):
                moduleName = "Lease Contract";
                break;
            case (Constants.Acc_Lease_Quotation):
                moduleName = "Lease Quotation";
                break;
            case (Constants.Acc_Lease_DO):
                moduleName = "Lease Delivery Order";
                break;
            case (Constants.Acc_Lease_Return):
                moduleName = "Lease Return";
                break;
            case (Constants.Acc_FixedAssets_Details_ModuleId):
                moduleName = "FA Details";
                break;
            case (Constants.Only_ProductMaster_ModuleId):
                moduleName = "Product Master";
                break;
            case (Constants.Inventory_Stock_Adjustment_ModuleId):
                moduleName = "Stock Adjustment";
                break;
            case (Constants.Acc_Stock_Request_ModuleId):
                moduleName = "Stock Request";
                break;
            case (Constants.Acc_InterStore_ModuleId):
                moduleName = "Inter Store Transfer";
                break;
            case (Constants.Acc_InterLocation_ModuleId):
                moduleName = "Inter Location Transfer";
                break;
            case (Constants.Labour_Master):
                moduleName = "Labour";
                break;
            case (Constants.MRP_WORK_CENTRE_MODULEID):
                moduleName = "Work Center Master";
                break;
            case (Constants.MRP_Machine_Management_ModuleId):
                moduleName = "Machine Master";
                break;
            case (Constants.MRP_WORK_ORDER_MODULEID):
                moduleName = "Work Order";
                break;
            case (Constants.VENDOR_JOB_WORKORDER_MODULEID):
                moduleName = "Vendor Job Work Order";
                break;
            case (Constants.MRP_Contract):
                moduleName = "Master Contract";
                break;
            case (Constants.MRP_RouteCode):
                moduleName = "Routing Template";
                break;
            case (Constants.MRP_JOB_WORK_MODULEID):
                moduleName = "Job Work";
                break;
            case(Constants.Acc_SecurityGateEntry_ModuleId):
                moduleName="Security Gate Entry";
                break;
            case(Constants.Acc_Multi_Entity_Dimension_MODULEID):
                moduleName=Constants.Title_MultiEntityDimension;
                break;    
            case(Constants.JOB_WORK_OUT_ORDER_MODULEID):
                moduleName=Constants.JOBWORK_OUT_FLOW;
                break;
        }
        return moduleName;
    }

    private HashMap<String, Object> processrequest(HttpServletRequest request, String moduelId,String relatedmoduleid,String parentid,boolean propagateTOChildCompaniesFalg, String ChildCompanyid,String parentFieldParamID) throws SessionExpiredException, JSONException, ServiceException {
        HashMap<String, Object> requestParams;
        Integer moduleid = Integer.parseInt(moduelId);
        String companyid="";
        String parentCompniesFieldparamid="";
        if (propagateTOChildCompaniesFalg) {
            companyid = ChildCompanyid;
            parentCompniesFieldparamid=parentFieldParamID;
        } else {
            companyid = sessionHandlerImpl.getCompanyid(request);
        }
        Integer fieldtype = Integer.parseInt(request.getParameter("fieldType"));
        int moduleflag = 0;
        if (!StringUtil.isNullOrEmpty(request.getParameter("moduleflag"))) {
            moduleflag = Integer.parseInt(request.getParameter("moduleflag"));
        }
        HashMap<String, Object> colParams = null;
        HashMap<String, Object> RefcolParams = null;
        String maxlength = "";
        boolean isEdit =  Boolean.parseBoolean(request.getParameter("isEdit")) ? true : false;
        if (fieldtype == 7) {// multiselect
            RefcolParams = getcolumn_number(companyid, moduleid, fieldtype, moduleflag);
            if (!Boolean.parseBoolean((String) RefcolParams.get("success"))) {
                colParams = RefcolParams;
            } else {
                //  colnumber accessed as per normal field
                colParams = getcolumn_number(companyid, moduleid, 1, moduleflag);
            }
            maxlength = "1000";
        } else {
            colParams = getcolumn_number(companyid, moduleid, fieldtype, moduleflag);
            if (fieldtype == 2) { // number fields
                maxlength = "15";
            } else if (fieldtype == 3) { // date
                maxlength = "50";
            } else if (fieldtype == 4) { // dropdown
                maxlength = "100";
            } else if (fieldtype == 5) { // timefield
                maxlength = "25";
            } else if (fieldtype == 7){
                maxlength = "1000";
            } else if (fieldtype == 8) { // reference dropdown
                maxlength = "255";
            } else if (fieldtype == 9) { // auto number
                maxlength = "150";
            } else if (fieldtype == 13) { // auto number
                maxlength = "1000";
            }
        }
        if (Boolean.parseBoolean((String) colParams.get("success"))) {

            requestParams = new HashMap<String, Object>();
            String fieldtooltip="";
            int GSTMappingColnum=0;
            int GSTConfigType=0;
            if(request.getAttribute("GSTMappingColnum")!= null){
                GSTMappingColnum=(int)request.getAttribute("GSTMappingColnum");
            }
            GSTConfigType = Boolean.parseBoolean(request.getParameter(Constants.IsForGSTRuleMapping)) ? Constants.GST_CONFIG_ISFORGST : 0;
            String fieldlabel = request.getParameter("fieldlabel");
            fieldlabel = fieldlabel.trim().replaceAll("\\s+"," ");                  //Remove all white spaces in between the String.
            if(request.getParameter("fieldtooltip")!=null){
            fieldtooltip= request.getParameter("fieldtooltip");
            fieldtooltip = StringUtil.isNullOrEmpty(fieldtooltip)?"":fieldtooltip.trim().replaceAll("\\s+"," ");
            }
            String formulae = request.getParameter("rules");
            String editable = request.getParameter("iseditable");
            
            Integer fieldmaxlen = 12;
            if (request.getParameter("maxlength") != null && !StringUtil.isNullOrEmpty(request.getParameter("maxlength"))) {
                maxlength = request.getParameter("maxlength");
            }
            if (!StringUtil.isNullOrEmpty(maxlength)) {
                fieldmaxlen = Integer.parseInt(maxlength);
            }

            Integer validationtype = 0;


            int sendNotification = (!com.krawler.common.util.StringUtil.isNullOrEmpty(request.getParameter("sendNotification")) && "1".equals(request.getParameter("sendNotification"))) ? 1 : 0;
            String notificationDays = (!com.krawler.common.util.StringUtil.isNullOrEmpty(request.getParameter("notificationDays"))) ? request.getParameter("notificationDays") : "";
            String isessential = request.getParameter("isessential");
            String customregex = request.getParameter("customregex");
            int isCustomField = Boolean.parseBoolean(request.getParameter("iscustomfield")) ? 1 : 0;// if false then it is dimension field
            boolean IsForGSTRuleMapping = !StringUtil.isNullOrEmpty(request.getParameter(Constants.IsForGSTRuleMapping)) ? Boolean.parseBoolean(request.getParameter(Constants.IsForGSTRuleMapping)) : false; //if true then field is for GST
            int lineitem = StringUtil.isNullOrEmpty(request.getParameter("lineitem")) ? 0 : 1;
            String combodata = request.getParameter("combodata");
            boolean isForSalesCommission = !StringUtil.isNullOrEmpty(request.getParameter(Constants.ISFORSALESCOMMISSION)) ? Boolean.parseBoolean(request.getParameter(Constants.ISFORSALESCOMMISSION)) : false;
            boolean isAutoPopulateDefaultValue = !StringUtil.isNullOrEmpty(request.getParameter(Constants.ISAUTOPOPULATEDEFAULTVALUE)) ? Boolean.parseBoolean(request.getParameter(Constants.ISAUTOPOPULATEDEFAULTVALUE)) : false;
            boolean isForKnockOff = !StringUtil.isNullOrEmpty(request.getParameter(Constants.isForKnockOff)) ? Boolean.parseBoolean(request.getParameter(Constants.isForKnockOff)) : false;
            if (isForKnockOff) {
                if (moduleid == Constants.Acc_Make_Payment_ModuleId || moduleid == Constants.Acc_Receive_Payment_ModuleId || moduleid == Constants.Acc_GENERAL_LEDGER_ModuleId) {
                    lineitem = 1;//isForKnockOff Custom field/Dimension created at line level in MP,RP and JE (ERP-32814)
                } else {
                    lineitem = 0;
                }
            }
            int essential = 0;
            boolean allowmapping = false;
            if (StringUtil.isNullOrEmpty(formulae)) {
                if (moduleflag == 1 && fieldtype == 8) {
                    allowmapping = false;
                } else if (fieldtype == 9) {
                    allowmapping = false;
                } else {
                    allowmapping = true;
                }
                 if (((!StringUtil.isNullOrEmpty(isessential)) && isessential.equals("0")) || fieldtype == 9) {// if field is auto no then no need to mark as mandatory
                    essential = 0;
                } else if (!StringUtil.isNullOrEmpty(isessential)) {
                    essential = 1;
                }
            }
            if (!StringUtil.isNullOrEmpty(parentCompniesFieldparamid)) {
                requestParams.put("PropagatedfieldparamID", parentCompniesFieldparamid);
            }
            requestParams.put("Maxlength", fieldmaxlen);
            requestParams.put("Isessential", essential);
            requestParams.put("sendNotification", sendNotification);
            requestParams.put("notificationDays", notificationDays);
            requestParams.put("Fieldtype", fieldtype);

            requestParams.put("isforproject", (request.getParameter("isforproject") !=null)? Integer.parseInt(request.getParameter("isforproject")) : 0);
            requestParams.put("Isforeclaim", (request.getParameter("isforeclaim") !=null)? Integer.parseInt(request.getParameter("isforeclaim")) : 0);
            if(request.getParameter("mapWithFieldType") !=null){
                try{
                    requestParams.put("mapwithtype",Integer.parseInt(request.getParameter("mapWithFieldType")));
                }catch(Exception ex){
                    requestParams.put("mapwithtype",1);
                }
            }
            
            requestParams.put("Validationtype", validationtype);
            requestParams.put("Customregex", customregex);
            requestParams.put("Fieldname", Constants.Custom_Record_Prefix + fieldlabel);

            requestParams.put("Fieldlabel", fieldlabel);
            requestParams.put("Fieldtooltip", fieldtooltip);
            requestParams.put("Companyid", companyid);
            requestParams.put("Moduleid", moduleid);
            requestParams.put("Customfield", isCustomField);
            requestParams.put("Customcolumn", lineitem);
//            requestParams.put("IsForGSTRuleMapping", IsForGSTRuleMapping);
            requestParams.put("GSTMappingColnum", GSTMappingColnum);
            requestParams.put("GSTConfigType", GSTConfigType);            
            requestParams.put("IsActivated", 1);//For newly created default activation will be 1
            requestParams.put("IsForSalesCommission", isForSalesCommission);
            requestParams.put("IsAutoPopulateDefaultValue", isAutoPopulateDefaultValue);//SDP-5276 - To auto-populate default value in document entry form.
            requestParams.put("IsForKnockOff", isForKnockOff);//ERP-32814 : ERM-88 Forward Invoice dimension data to its knock off document level
            if (!StringUtil.isNullOrEmpty(parentid)) {
//                FieldParams parentFieldParams = (FieldParams) kwlCommonTablesDAOObj.getClassObject(FieldParams.class.getName(), parentid);
                requestParams.put(Constants.parentmoduleid, parentid);
            }
            if (!StringUtil.isNullOrEmpty(relatedmoduleid)) {
                requestParams.put(Constants.relatedmoduleid, relatedmoduleid);
            }
            int relatedmoduleisallowedit=Constants.DONOT_ALLOW_TO_EDIT_PRODUCT_CUSTOMFIELD;         //Setting default to 0 as if the check is not selected on UI side it sends null value so setting it false ERM-177 / ERP-34804
            /**
             * Below check is implemented to check weather the User had enabled
             * Allow to edit Products Custom field in various documents where
             * product can be used if it is enabled the setting value 0 in
             * databases. ERP-34804 / ERM-177.
             */
            if (request.getParameter("relatedModuleIsAllowEdit") != null && StringUtil.equalIgnoreCase(request.getParameter("relatedModuleIsAllowEdit"), "on")) {
                relatedmoduleisallowedit = Constants.ALLOW_TO_EDIT_PRODUCT_CUSTOMFIELD;
            }
            requestParams.put(Constants.RELATED_MODULE_IS_ALLOW_EDIT, (relatedmoduleisallowedit));
            requestParams.put("Iseditable", editable);
            String RefModule = null;
            String RefDataColumn = null;
            String RefFetchColumn = null;
            String comboid = "";
//            if (fieldtype == 8) {//Reference Module
//                comboid = request.getParameter("comboid");
//                requestParams.put("Comboname", request.getParameter("comboname"));
//                requestParams.put("Comboid", comboid);
//                requestParams.put("Moduleflag", Integer.parseInt(request.getParameter("moduleflag")));
//
//                if (request.getParameter("moduleflag").equals("0")) {
//                    RefModule = "DefaultMasterItem";
//                    RefDataColumn = "value";
//                    RefFetchColumn = "id";
//                } else if (request.getParameter("comboname").equals("Account")) {
//                    RefModule = "CrmAccount";
//                    RefDataColumn = "accountname";
//                    RefFetchColumn = "accountid";
//                } else if (request.getParameter("comboname").equals("Product")) {
//                    RefModule = "CrmProduct";
//                    RefDataColumn = "productname";
//                    RefFetchColumn = "productid";
//                } else if (request.getParameter("comboname").equals("Contact")) {
//                    RefModule = "CrmContact";
//                    RefDataColumn = "lastname";
//                    RefFetchColumn = "contactid";
//                } else if (request.getParameter("comboname").equals("Case")) {
//                    RefModule = "CrmCase";
//                    RefDataColumn = "subject";
//                    RefFetchColumn = "caseid";
//                } else if (request.getParameter("comboname").equals("Opportunity")) {
//                    RefModule = "CrmOpportunity";
//                    RefDataColumn = "oppname";
//                    RefFetchColumn = "oppid";
//                } else if (request.getParameter("comboname").equals("Lead")) {
//                    RefModule = "CrmLead";
//                    RefDataColumn = "lastname";
//                    RefFetchColumn = "leadid";
//                } else if (request.getParameter("comboname").equals("Users")) {
//                    RefModule = "User";
//                    RefDataColumn = "lastName";
//                    RefFetchColumn = "userID";
//                }
//            } else {
            requestParams.put("Comboname", "");
            requestParams.put("Comboid", comboid);
            requestParams.put("Moduleflag", 0);
//            }

            if (fieldtype == 9) {//auto number
                requestParams.put("Startingnumber", Integer.parseInt(request.getParameter("startingnumber")));
                requestParams.put("Prefix", request.getParameter("prefix"));
                requestParams.put("Suffix", request.getParameter("suffix"));
            }
            requestParams.put("Colnum", colParams.get("column_number"));
            String Refcolumn_number = "0";
            String refcolumnname = null;
            if (fieldtype == 7 && RefcolParams != null) {
                requestParams.put("Refcolnum", RefcolParams.get("column_number"));
                refcolumnname = Constants.Custom_column_Prefix + RefcolParams.get("column_number");
                Refcolumn_number = RefcolParams.get("column_number").toString();
            }
            requestParams.put("isfortask", (request.getParameter("isfortask") !=null)? 1 : 0);
            if (fieldtype == 3 && !(StringUtil.isNullOrEmpty(request.getParameter("defaultval")))) {
//                //Long longDate=Long.parseLong(request.getParameter("defaultval"));
//                Date dateInDateFormat=new Date(Long.parseLong(request.getParameter("defaultval")));
                DateFormat df=new SimpleDateFormat(Constants.MMMMdyyyy);
                String dateInStringFormat="";
                try {
                    Date dateInDateFormat = df.parse(request.getParameter("defaultval"));
                    dateInStringFormat = df.format(dateInDateFormat);
                    
                } catch (ParseException ex) {
                    dateInStringFormat=request.getParameter("defaultval");
                    Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                }
               requestParams.put("DefaultValue", dateInStringFormat);

            } else {
                requestParams.put("DefaultValue", request.getParameter("defaultval"));
            }
            JSONObject resultJson = new JSONObject();
            KwlReturnObject kmsg = null,kmsg1=null;
            HashMap<String, HashMap<String,Object>> requestParams1 =new HashMap<>();
            FieldParams fp = null;
            List list=null;
            String itemDesc="";
            String itemVal="";
           
             /*
              Below code block is used to get Item description and execute only Drop down case
             */
            if ((fieldtype == 4 || fieldtype == 7) && isEdit) {
                HashMap<String,Object> masterconfigurations =null;
                char dimVal='T';
                kmsg1 = accAccountDAOobj.getfieldcomboItemDesc(requestParams);
                if (kmsg1 != null && kmsg1.getEntityList().size() > 0) {
                    list = kmsg1.getEntityList();
                    Iterator listItr = list.iterator();
                    while (listItr.hasNext()) {
                        boolean acivateDeactivatemasterfield=true;
                        Object object[] = (Object[]) listItr.next();
                        itemVal = (String) object[0];
                        itemDesc = (String) object[1];
                        dimVal = (char) object[2];
                        if (dimVal=='F') {
                            acivateDeactivatemasterfield = false;
                        } 
                        masterconfigurations = new HashMap<>();
                        masterconfigurations.put("Itemdescription",itemDesc);
                        masterconfigurations.put("Activatedeactivatedimensionvalue",acivateDeactivatemasterfield);
                        requestParams1.put(itemVal, masterconfigurations);
                    }
                }
            }
            
            kmsg = accAccountDAOobj.insertfield(requestParams);
            resultJson.put("success", kmsg.isSuccessFlag());
            requestParams.put("success", kmsg.isSuccessFlag() ? 1 : 0);
            if (kmsg.isSuccessFlag()) {
                fp = (FieldParams) kmsg.getEntityList().get(0);
                resultJson.put("ID", fp.getId());
                resultJson.put("msg", kmsg.getMsg());
                String defaultvalue = request.getParameter("defaultval");
                defaultvalue = insertfieldcombodata(combodata, fp.getId(), defaultvalue,requestParams1);
                requestParams.put("defaultvalue", defaultvalue);
                String colname = Constants.Custom_column_Prefix + colParams.get("column_number");
                String column_name = Constants.Custom_Column_Prefix + requestParams.get("Colnum");
                //  resultJson.put("headerInfo", createDefaultHeadrEntry(allowmapping, Refcolumn_number, column_name, essential, companyid, comboid, RefModule, RefDataColumn, RefFetchColumn, fieldtype, moduleid, fieldmaxlen, fieldlabel, fp.getId(), editable));
//                if (!StringUtil.isNullOrEmpty(formulae)) {       //this used for to set formula
//                    setCustomColumnFormulae(request,true);
//                }

            } else {
                resultJson.put("msg", "Error Processing request");
            }
            requestParams.put("response", resultJson);


        } else {
            return colParams;
        }
        return requestParams;
    }

    private HashMap<String, Object> getcolumn_number(String companyid, Integer moduleid, Integer fieldtype, int moduleflag) throws SessionExpiredException, JSONException {
        KwlReturnObject result = null;
        JSONObject jobj = new JSONObject();
        boolean Notreachedlimit = true;
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        try {
            Integer custom_column_start = 0, Custom_Column_limit = 0;

            switch (fieldtype) {
                case Constants.TEXTFIELD: //text field
                case Constants.NUMBERFIELD: //Number field
                case Constants.TIMEFIELD:
                case Constants.AUTONUMBER://  auto number
                case 6:
                case Constants.TEXTAREA: // Text Area
                case Constants.RICHTEXTAREA: // Rich Text Area
                case Constants.FIELDSET:
                    custom_column_start = Constants.Custom_Column_Normal_start;
                    Custom_Column_limit = Constants.Custom_Column_Normal_limit;
                    requestParams.put("filter_names", Arrays.asList("companyid", "moduleid", "INfieldtype", ">colnum", "<=colnum"));
                    requestParams.put("filter_values", Arrays.asList(companyid, moduleid, "1,2,5,6,7,9,12,13,15", custom_column_start, custom_column_start + Custom_Column_limit));
                    break;
                    
                case Constants.DATEFIELD: //Date 
                    custom_column_start = Constants.Custom_Column_Date_start;
                    Custom_Column_limit = Constants.Custom_Column_Date_limit;
                    requestParams.put("filter_names", Arrays.asList("companyid", "moduleid", "INfieldtype", ">colnum", "<=colnum"));
                    requestParams.put("filter_values", Arrays.asList(companyid, moduleid, "3", custom_column_start, custom_column_start + Custom_Column_limit));
                    break;
                    
                case Constants.CHECKBOX:
                    custom_column_start = Constants.Custom_Column_Check_start;
                    Custom_Column_limit = Constants.Custom_Column_Check_limit;
                    requestParams.put("filter_names", Arrays.asList("companyid", "moduleid", "INfieldtype", ">colnum", "<=colnum"));
                    requestParams.put("filter_values", Arrays.asList(companyid, moduleid, "11", custom_column_start, custom_column_start + Custom_Column_limit));
                    break;

                case Constants.SINGLESELECTCOMBO:
                case Constants.MULTISELECTCOMBO:
                    custom_column_start = Constants.Custom_Column_Combo_start;
                    Custom_Column_limit = Constants.Custom_Column_Combo_limit;
                    requestParams.put("filter_names", Arrays.asList("companyid", "moduleid", "INfieldtype"));
                    requestParams.put("filter_values", Arrays.asList(companyid, moduleid, "4,7"));
                    break;
                case Constants.REFERENCECOMBO:
                    if (moduleflag == 1) {
                        custom_column_start = Constants.Custom_Column_User_start;
                        Custom_Column_limit = Constants.Custom_Column_User_limit;
                    } else {
                        custom_column_start = Constants.Custom_Column_Master_start;
                        Custom_Column_limit = Constants.Custom_Column_Master_limit;
                    }

                    requestParams.put("filter_names", Arrays.asList("companyid", "moduleid", "fieldtype", "moduleflag"));
                    requestParams.put("filter_values", Arrays.asList(companyid, moduleid, fieldtype, moduleflag));
                    break;
            }
            Integer colcount = 1;

            result = accAccountDAOobj.getFieldParams(requestParams);
            List lst = result.getEntityList();
            colcount = lst.size();
            if (colcount == Custom_Column_limit) {
                jobj.put("success", "msg");
                jobj.put("title", "Alert");
                jobj.put("msg", "Cannot add new field. Maximum custom field limit reached.");
                jobj.put("moduleName", getModuleName(moduleid));
                Notreachedlimit = false;
            }
            if (Notreachedlimit) {
                Iterator ite = lst.iterator();
                int[] countchk = new int[Custom_Column_limit + 1];
                while (ite.hasNext()) {
                    FieldParams tmpcontyp = (FieldParams) ite.next();

                    // check added to refer to reference column in case of multiselect combo field instead of refering to column number field
                    if ((fieldtype == 4 || fieldtype == 7) && tmpcontyp.getFieldtype() == 7) {//FieldComboData as drop-down.  Start from col1
                        countchk[tmpcontyp.getRefcolnum() - custom_column_start] = 1;
                    } else {
                        countchk[tmpcontyp.getColnum() - custom_column_start] = 1;
                    }
                }
                for (int i = 1; i <= Custom_Column_limit; i++) {
                    if (countchk[i] == 0) {
                        colcount = i;
                        break;
                    }
                }
            }
            requestParams.put("response", jobj);
            requestParams.put("column_number", colcount + custom_column_start);
            requestParams.put("success", Notreachedlimit ? "True" : "false");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return requestParams;
    }

    public String insertfieldcombodata(String combodata, String fieldid, String defaultvalue,HashMap<String, HashMap<String,Object>> requestParams1) throws ServiceException {
        boolean isdefaultvalue = !StringUtil.isNullOrEmpty(defaultvalue);
        JSONArray syncArray = new JSONArray();
        if (!StringUtil.isNullOrEmpty(combodata)) {
            //@somnath default value map to fetch default values id which will insert in next step
            HashMap combohash = new HashMap();
            if (isdefaultvalue) {
                String[] combodefaultvalues = defaultvalue.split(",");
                for (int cnt1 = 0; cnt1 < combodefaultvalues.length; cnt1++) {
                    String trimval = combodefaultvalues[cnt1].trim();
                    if (!StringUtil.isNullOrEmpty(trimval)) {
                        combohash.put(combodefaultvalues[cnt1], 1);
                    }
                }
            }
            //@somnath default value map end
            String Defaultcombodata = "";

            String[] combovalues = combodata.split(";");
            for (int cnt = 0; cnt < combovalues.length; cnt++) {
                String trimArray = combovalues[cnt].trim();
                if (!StringUtil.isNullOrEmpty(trimArray)) {
                    HashMap<String, Object> comborequestParams = new HashMap<String, Object>();
                    HashMap<String, Object> masterconfiguration = new HashMap<String, Object>();
                    comborequestParams.put("Fieldid", fieldid);
                    comborequestParams.put("Value", combovalues[cnt]);
                    if (requestParams1 != null && requestParams1.size() > 0) {
                        /*
                         * If field combo value present in requestParams1 then
                         * get Item description of that fieldcombo data
                         */
                        masterconfiguration = requestParams1.get(combovalues[cnt]);
                        if (requestParams1.containsKey(combovalues[cnt]) && masterconfiguration.containsKey("Itemdescription")) {
                            comborequestParams.put("Itemdescription", masterconfiguration.get("Itemdescription"));
                        }
                        if (requestParams1.containsKey(combovalues[cnt]) && masterconfiguration.containsKey("Activatedeactivatedimensionvalue")) {
                            comborequestParams.put("Activatedeactivatedimensionvalue", (Boolean) masterconfiguration.get("Activatedeactivatedimensionvalue"));
                        }
                    } else {//if we create new dimention then by default all master items will be activated
                        comborequestParams.put("Activatedeactivatedimensionvalue", true);
                    }
                    KwlReturnObject resultkmsg = accAccountDAOobj.getfieldcombodata(comborequestParams);
                    if (resultkmsg.getEntityList().isEmpty()) {
                        KwlReturnObject kmsg = accAccountDAOobj.insertfieldcombodata(comborequestParams);
                        
                        FieldComboData fc = null;
                        fc = (FieldComboData) kmsg.getEntityList().get(0);
//                        try {
//                            JSONObject syncObj = new JSONObject();
//                            syncObj.put("erpid", fc.getId());
//                            syncObj.put("name", fc.getValue());
//                            syncArray.put(syncObj);
//                        } catch (JSONException ex) {
//                            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
//                        }
                        
                        // check default value is same as current combo value if yes add Id it to default value
                        if (isdefaultvalue && combohash.containsKey(combovalues[cnt])) {
                            Defaultcombodata = Defaultcombodata + fc.getId() + ",";
                        }
                    }
                }
            }

//            FieldParams dimention = (FieldParams) kwlCommonTablesDAOObj.getClassObject(FieldParams.class.getName(), fieldid);
//            if (dimention != null && dimention.getIsforeclaim()==1) {
//                HashMap<String, Object> params = new HashMap<String, Object>();
//                params.put("ids", syncArray);
//                params.put("companyid", dimention.getCompanyid());
//                syncCostCentersCreatedToEclaim(params);//Code implemented below this function
//            }
            //@somnath remove "," from default value which is inserted at the end in case of multiselect combo
            if (isdefaultvalue && Defaultcombodata.length() > 0) {
                defaultvalue = Defaultcombodata.substring(0, Defaultcombodata.length() - 1);
            } else {
                defaultvalue = "";
            }
        }
        return defaultvalue;
    }

    /*public void syncCostCentersCreatedToEclaim(HashMap<String, Object> params) {
        Session session = null;
        try {
            String companyid = (String) params.get("companyid");
            JSONArray syncArray = (JSONArray) params.get("ids");

            //Fetched data from Deskera eClaim
            String action = "804";
            String eclaimURL = this.getServletContext().getInitParameter("eclaimURL");
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("companyid", companyid);
            userData.put("erpcostcenterids", syncArray);
            session = HibernateUtil.getCurrentSession();

//            JSONObject jobj = APICallHandler.callApp(session, eclaimURL, userData, companyid, action);
             JSONArray jArray = jobj.getJSONArray("data");
            for(int i=0; i<jArray.length(); i++) {
                
            }
        } catch (JSONException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            HibernateUtil.closeSession(session);
        }
    }*/

    public String insertfieldcombodataInCsaeInvSync(String combodata, String CombodataIds, String fieldid, String defaultvalue) throws ServiceException {
        boolean isdefaultvalue = !StringUtil.isNullOrEmpty(defaultvalue);
        if (!StringUtil.isNullOrEmpty(combodata)) {
            //@somnath default value map to fetch default values id which will insert in next step
            HashMap combohash = new HashMap();
            if (isdefaultvalue) {
                String[] combodefaultvalues = defaultvalue.split(",");
                for (int cnt1 = 0; cnt1 < combodefaultvalues.length; cnt1++) {
                    String trimval = combodefaultvalues[cnt1].trim();
                    if (!StringUtil.isNullOrEmpty(trimval)) {
                        combohash.put(combodefaultvalues[cnt1], 1);
                    }
                }
            }
            
            String[] idsOfComboData = CombodataIds.split(";");
            
            //@somnath default value map end
            String Defaultcombodata = "";

            String[] combovalues = combodata.split(";");
            for (int cnt = 0; cnt < combovalues.length; cnt++) {
                String trimArray = combovalues[cnt].trim();
                if (!StringUtil.isNullOrEmpty(trimArray)) {
                    HashMap<String, Object> comborequestParams = new HashMap<String, Object>();
                    comborequestParams.put("Fieldid", fieldid);
                    comborequestParams.put("Value", combovalues[cnt]);
                    comborequestParams.put("comboFieldDataIdInCaseOfInvSync", idsOfComboData[cnt]);
                    KwlReturnObject kmsg = accAccountDAOobj.insertfieldcombodata(comborequestParams);
                    FieldComboData fc = null;
                    // check default value is same as current combo value if yes add Id it to default value
                    if (isdefaultvalue && combohash.containsKey(combovalues[cnt])) {
                        fc = (FieldComboData) kmsg.getEntityList().get(0);
                        Defaultcombodata = Defaultcombodata + fc.getId() + ",";
                    }
                }
            }

            //@somnath remove "," from default value which is inserted at the end in case of multiselect combo
            if (isdefaultvalue && Defaultcombodata.length() > 0) {
                defaultvalue = Defaultcombodata.substring(0, Defaultcombodata.length() - 1);
            } else {
                defaultvalue = "";
            }
        }
        return defaultvalue;
    }

    private HashMap<String, Object> createDefaultHeadrEntry(boolean allowmapping, String Refcolumn_number, String column_name, Integer isessential, String companyid, String comboid, String RefModule, String RefDataColumn, String RefFetchColumn, Integer fieldtype, Integer moduleid, Integer fieldmaxlen, String fieldlabel, String fieldid, String editable) throws SessionExpiredException, JSONException, ServiceException {
        HashMap<String, Object> requestParams = new HashMap<String, Object>();

        requestParams.put("Customflag", true);
        requestParams.put("DefaultHeader", fieldlabel);
        requestParams.put("Recordname", Constants.Custom_Record_Prefix + fieldlabel);
        requestParams.put("Dbcolumnrefname", Refcolumn_number);
        requestParams.put("Dbcolumnname", column_name);
        requestParams.put("PojoMethodName", Constants.Custom_Record_Prefix + fieldlabel);
        requestParams.put("Pojoheadername", fieldid + "");
        requestParams.put("MaxLength", fieldmaxlen);
        requestParams.put("Editable", editable);

        HashMap<String, Object> moduleParams = new HashMap<String, Object>();
        moduleParams.put("filter_names", Arrays.asList("moduleName"));
        moduleParams.put("filter_values", Arrays.asList(getModuleName(moduleid)));
        KwlReturnObject kmsg = accAccountDAOobj.getModules(moduleParams);
        String modulename = "";
        if (kmsg.isSuccessFlag()) {
            Modules modObj = (Modules) kmsg.getEntityList().get(0);
            modulename = modObj.getModuleName();
            requestParams.put("ModuleName", modulename);
            requestParams.put("Module", modObj.getId());
        }

        requestParams.put("Xtype", fieldtype + "");
        requestParams.put("Configid", "0");
        requestParams.put("HbmNotNull", false);
        requestParams.put("AllowImport", allowmapping);
        requestParams.put("AllowMapping", allowmapping);
        String ValidateType = returnValidateType(fieldtype);
        if (fieldtype == 8) {
            requestParams.put("RefModule_PojoClassName", RefModule);
            requestParams.put("RefDataColumn_HbmName", RefDataColumn);
            requestParams.put("RefFetchColumn_HbmName", RefFetchColumn);
            requestParams.put("Configid", comboid);
        }
        requestParams.put("ValidateType", ValidateType);
//            df.setMandatory(isessential==0?false:true);

        boolean iseditable = true;
        if (!StringUtil.isNullOrEmpty(editable)) {
            iseditable = Boolean.parseBoolean(editable);
        }
        requestParams.put("Mandatory", isessential == 0 ? false : true);
        requestParams.put("Editable", iseditable);
        kmsg = accAccountDAOobj.insertdefaultheader(requestParams);
        JSONObject resultJson = new JSONObject();
        requestParams.clear();
        resultJson.put("success", kmsg.isSuccessFlag());
        if (kmsg.isSuccessFlag()) {
            DefaultHeader dh = (DefaultHeader) kmsg.getEntityList().get(0);
            requestParams.put("Defaultheader", dh.getId());
            requestParams.put("Company", companyid);
            requestParams.put("NewHeader", "");
            requestParams.put("Mandotory", isessential == 0 ? false : true);
            requestParams.put("Required", false);
            requestParams.put("ModuleName", modulename);
            requestParams.put("Editable", iseditable);
            kmsg = accAccountDAOobj.insertcolumnheader(requestParams);
        }
        return requestParams;
    }

    private String returnValidateType(int fieldtype) {

        String ValidateType = "";
        if (fieldtype == 1 || fieldtype == 9) {
            ValidateType = "string";
        } else if (fieldtype == 2) {
            ValidateType = "double";
        } else if (fieldtype == 3) {
            ValidateType = "date";
        } else if (fieldtype == 4) {
            ValidateType = "dropdown";
        } else if (fieldtype == 5) {
            ValidateType = "time";
        } else if (fieldtype == 7) {
            ValidateType = "multiselect";
        } else if (fieldtype == 8) {
            ValidateType = "refdropdown";
        }
        return ValidateType;
    }
    
    public ModelAndView saveeClaimCostCentersAsDimention(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONObject jsonobj;
        try {
            boolean iseClaim=Boolean.parseBoolean(request.getParameter("iseClaim"));
            if(iseClaim){
                  String ccnames = saveeClaimCostCentersAsDimention(request);
                  if(!ccnames.equalsIgnoreCase("")){
                      ccnames = " except "+ccnames;
                  }
                  issuccess = true;
                  msg = messageSource.getMessage("acc.field.DataSyncedsuccessfullyfromDeskeraeClaim", null, RequestContextUtils.getLocale(request)) + ccnames+".";
            }
            txnManager.commit(status);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView saveProjectMasterItemForCustom(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONObject jsonobj;
        try {
            boolean isProject=Boolean.TRUE.parseBoolean(request.getParameter("isProject"));
            if(isProject){
                  saveProjectMasterItemForCustom(request);
                  issuccess = true;
                  msg = messageSource.getMessage("acc.field.DataSyncedsuccessfullyfromDeskeraPM", null, RequestContextUtils.getLocale(request));
            }else{
                 jsonobj=saveTaskMasterItemForCustom(request);
                 if(jsonobj.getBoolean("Noparent")){
                      msg =jsonobj.getString("msg");
                 }else{
                      msg = messageSource.getMessage("acc.field.DataSyncedsuccessfullyfromDeskeraPM", null, RequestContextUtils.getLocale(request));
                 }
                 issuccess = true;
            }
            txnManager.commit(status);

        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
      public JSONObject saveTaskMasterItemForCustom(HttpServletRequest request) throws ServiceException {
        JSONObject jsonobj = new JSONObject();
        String msg="";
        String feildLables="";
        HashSet<String> feildSet = new HashSet<String>();
        String Projectids="";
        boolean NoProject=false; // Use to check if projectid from PM is not exitst in parent combo
        boolean NoParent=false; // Use to Check if Combo is having parent or not
        boolean flag=false;
        boolean NoParentmsg=false;
        KwlReturnObject resultExtra=null;
        //Session session=null;
        try {
            KwlReturnObject result = null;
            KwlReturnObject resultForProject = null;
            boolean CheckForExist=true;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyId = sessionHandlerImpl.getCurrencyID(request);
            String ProjectComboId="";
            KwlReturnObject extracompanyprefObjresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(),companyid);
            ExtraCompanyPreferences extracompanyobj = (ExtraCompanyPreferences)extracompanyprefObjresult.getEntityList().get(0);
            
            //Fetched data from Deskera PM
//            String action = "108";
//            String pmURL = this.getServletContext().getInitParameter("pmURL");
            String pmURL = URLUtil.buildRestURL("pmURL");
            pmURL = pmURL + "task/projectmilestones";
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("companyid", companyid);
            jsonobj.put("Noparent",false);
            String partialorfull=request.getParameter("isPartial");
            if("partial".equalsIgnoreCase(partialorfull) && extracompanyobj != null && extracompanyobj.getLastsyncwithpm() != null){
                userData.put("lastsyncdate", extracompanyobj.getLastsyncwithpm());
            }
            Date syncdate=new Date();
             //session = HibernateUtil.getCurrentSession();
            
            JSONObject jobj = apiCallHandlerService.restGetMethod(pmURL, userData.toString());
//            JSONObject jobj = apiCallHandlerService.callApp(pmURL, userData, companyid, action);
//
            JSONArray jArray = jobj.getJSONArray("data");
            
//            JSONArray jArray =new JSONArray("[{'projectid':'P1','taskid':'T1','taskName':'DESIGN'},{'projectid':'P2','taskid':'T2','taskName':'CODE'},{'projectid':'P1','taskid':'T4','taskName':'DOCUMENTATION'}]");
            //Fetched Project field params     
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("filter_names", Arrays.asList("companyid", "isfortask"));
            requestParams.put("filter_values", Arrays.asList(companyid, 1));
            
            result = accMasterItemsDAOobj.getFieldParams(requestParams);


            List list = result.getEntityList();
            Iterator itr = list.iterator();
            //Loop on fields params having isfortask true
            while (itr.hasNext()) {
                FieldParams fieldParams = (FieldParams) itr.next();
                if(fieldParams.getParent() == null){  //Check if Combo doesn't having parent
                    feildSet.add(fieldParams.getFieldlabel());
                    jsonobj.put("Noparent",true);
                    NoParent=true;
                    continue;
                }
                //Loop to set combo values 
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject jobjData = jArray.getJSONObject(i);
                    CheckForExist=true;
                    String projectid = (String) jobjData.get("projectid");
                    String taskid = (String) jobjData.get("taskid");
                    String taskName = (String) jobjData.get("taskName");

                    //Check for duplicate. If not present then insert.
                    HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();

                    filter_names.add("taskid");
                    filter_names.add("projectid");
                    filter_names.add("field.id");

                    filter_params.add(taskid);
                    filter_params.add(projectid);
                    filter_params.add(fieldParams.getId());
                    filterRequestParams.put("filter_names", filter_names);
                    filterRequestParams.put("filter_params", filter_params);
                    KwlReturnObject cntResult = accMasterItemsDAOobj.getMasterItemsForCustom(filterRequestParams);

                    List listItems = cntResult.getEntityList();
                    Iterator itrItems = listItems.iterator();
                    HashMap requestParam = new HashMap();

                    requestParam.put("name", taskName);
                    requestParam.put("groupid", fieldParams.getId());
                    requestParam.put("projectid", projectid);
                    requestParam.put("taskid", taskid);
                    requestParam.put("activatedeactivateflg", true);
                    if (itrItems.hasNext()) {
                        FieldComboData item = (FieldComboData) itrItems.next();
                        requestParam.put("id", item.getId());
                        CheckForExist=false;
                    }
                    result = accMasterItemsDAOobj.addUpdateMasterCustomItem(requestParam,false);
                    if (CheckForExist) {
                        FieldComboData FCD=(FieldComboData)result.getEntityList().get(0);
                        HashMap<String, String> ProjectTask = new HashMap<String, String>();
                        ProjectTask.put("projectid", projectid);
                        ProjectTask.put("fieldid", fieldParams.getParentid());
                        ProjectTask.put("taskFeildComboId", FCD.getId());
                        NoProject=accMasterItemsDAOobj.SaveProjectTaskMapping(ProjectTask); 
                        if(NoProject){
                            NoParentmsg=true;
                            if(!flag)
                            Projectids+=projectid+"- Task : "+taskName+",";
                        }
                     }
                }
                flag=true;
            }
        HashMap<String, Object> extprefMap = new HashMap<String, Object>();
        extprefMap.put("id",companyid);
        extprefMap.put("mslastsyncwithpm",syncdate);
        resultExtra = accCompanyPreferencesObj.addOrUpdateExtraPreferences(extprefMap); 
     
        if(feildSet.size()>0){
           feildLables=feildSet.toString();
           feildLables=feildLables.substring(1, feildLables.length());
        }
        
        if(NoParentmsg){
             msg+="There is No Projects with id " +Projectids;
             msg=msg.substring(0,msg.length()-1);
        }
        if(NoParent){
             msg+=" Please Set Parent For Combo " +feildLables;
        }
        if(!StringUtil.isNullOrEmpty(msg)){
            msg=msg.substring(0,msg.length()-1);
        }
        jsonobj.put("msg",msg);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
//        finally{
//            HibernateUtil.closeSession(session);
//        }
        return jsonobj;
    }
    
    public void saveProjectMasterItemForCustom(HttpServletRequest request) throws ServiceException {
           
        //Session session=null;
        try {
            KwlReturnObject result = null;
            KwlReturnObject resultExtra=null;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyId = sessionHandlerImpl.getCurrencyID(request);
            KwlReturnObject extracompanyprefObjresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(),companyid);
            ExtraCompanyPreferences extracompanyobj = (ExtraCompanyPreferences)extracompanyprefObjresult.getEntityList().get(0);
             
            //Fetched data from Deskera PM
//            String action = "106";
//            String pmURL = this.getServletContext().getInitParameter("pmURL");
            String pmURL = URLUtil.buildRestURL("pmURL");
            pmURL = pmURL + "project/company-projects";            
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("companyid", companyid);
            String partialorfull=request.getParameter("isPartial");
            if("partial".equalsIgnoreCase(partialorfull) && extracompanyobj != null && extracompanyobj.getLastsyncwithpm() != null){
                userData.put("lastsyncdate", extracompanyobj.getLastsyncwithpm());
            }
            Date syncdate=new Date();
            //session = HibernateUtil.getCurrentSession();

            JSONObject jobj = apiCallHandlerService.restGetMethod(pmURL, userData.toString());
//            JSONObject jobj = apiCallHandlerService.callApp(pmURL, userData, companyid, action);
   
            JSONArray jArray = jobj.getJSONArray("data");
            
           
            //Fetched Project field params     
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("filter_names", Arrays.asList("companyid", "isforproject"));
            requestParams.put("filter_values", Arrays.asList(companyid, 1));


//            requestParams.put("filter_names", Arrays.asList("isforproject"));
//            requestParams.put("filter_values", Arrays.asList(1));
            result = accMasterItemsDAOobj.getFieldParams(requestParams);


            List list = result.getEntityList();
            Iterator itr = list.iterator();
            //Loop on fields params having isProject true
            while (itr.hasNext()) {
                FieldParams fieldParams = (FieldParams) itr.next();
                //Loop to set combo values 
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject jobjData = jArray.getJSONObject(i);


                    String projectid = (String) jobjData.get("projectId");
                    String projectname = (String) jobjData.get("projectName");
                    String projectdescription = jobjData.optString("projectDescription");

                    //Check for duplicate. If not present then insert.
                    HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();

                    filter_names.add("projectid");
                    filter_names.add("field.id");

                    filter_params.add(projectid);
                    filter_params.add(fieldParams.getId());
                    filterRequestParams.put("filter_names", filter_names);
                    filterRequestParams.put("filter_params", filter_params);
                    KwlReturnObject cntResult = accMasterItemsDAOobj.getMasterItemsForCustom(filterRequestParams);

                    List listItems = cntResult.getEntityList();
                    Iterator itrItems = listItems.iterator();
                    HashMap requestParam = new HashMap();

                    requestParam.put("name", projectname);
                    requestParam.put("groupid", fieldParams.getId());
                    requestParam.put("projectid", projectid);
                    requestParam.put("itemdescription", projectdescription);
                    requestParam.put("activatedeactivateflg", true);
                    if (itrItems.hasNext()) {
                        FieldComboData item = (FieldComboData) itrItems.next();
                        requestParam.put("id", item.getId());
                    }
                    result = accMasterItemsDAOobj.addUpdateMasterCustomItem(requestParam,false);
                    
                    
                    // creating WIP AND CP accounts for each project.
                    ExtraCompanyPreferences companyPreferences = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject(ExtraCompanyPreferences.class.getName(), companyid);
                    if (companyPreferences != null) {
                        if (!StringUtil.isNullOrEmpty(companyPreferences.getCpAccountPrefix()) && !StringUtil.isNullOrEmpty(companyPreferences.getWipAccountPrefix())) {
                            // CP Accounts creation
                            boolean isCPAccountExist = false;
                            String cpAccountName = companyPreferences.getCpAccountPrefix() + "_" + projectname;
                            KwlReturnObject cpReturnObject = accAccountDAOobj.getAccountFromName(companyid, cpAccountName);
                            List cpAccountResultList = cpReturnObject.getEntityList();
                            if (cpAccountResultList.size() > 0) {
                                isCPAccountExist = true;
                            }
                            if (!isCPAccountExist) {
                                saveAccount(request, cpAccountName, companyid, currencyId, true);
                            }

                            // WIP Account creation

                            boolean isWIPAccountExist = false;
                            String wipAccountName = companyPreferences.getWipAccountPrefix() + "_" + projectname;
                            KwlReturnObject wipReturnObject = accAccountDAOobj.getAccountFromName(companyid, wipAccountName);
                            List wipAccountResultList = wipReturnObject.getEntityList();
                            if (wipAccountResultList.size() > 0) {
                                isWIPAccountExist = true;
                            }
                            if (!isWIPAccountExist) {
                                saveAccount(request, wipAccountName, companyid, currencyId, false);
                            }

                        }
                    }
                    

//                    int count = cntResult.getRecordTotalCount();

//                    if (count > 0) {
//                        
//                    } else {
//                        HashMap requestParam = new HashMap();
//
//                        requestParam.put("name", projectname);
//                        requestParam.put("groupid", fieldParams.getId());
//                        requestParam.put("projectid", projectid);
//                        result = accMasterItemsDAOobj.addUpdateMasterCustomItem(requestParam);
//                    }

                }
            }
           
            HashMap<String, Object> extprefMap = new HashMap<String, Object>();
            extprefMap.put("id",companyid);
            extprefMap.put("lastsyncwithpm",syncdate);
            resultExtra = accCompanyPreferencesObj.addOrUpdateExtraPreferences(extprefMap);
            
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
//       finally {
//            HibernateUtil.closeSession(session);
//
//        }
    }
    
    public String saveeClaimCostCentersAsDimention(HttpServletRequest request) throws ServiceException {
        String cclistmsg = "";
        String auditID = "";
        //Session session = null;
        try {
            List cclist = new ArrayList();
            KwlReturnObject result = null;
            String companyid = sessionHandlerImpl.getCompanyid(request);

            //Fetched data from Deskera eClaim
            String action = "18";
//            String eclaimURL = this.getServletContext().getInitParameter("eclaimURL");
            String eclaimURL = URLUtil.buildRestURL("eclaimURL");
            eclaimURL = eclaimURL + "claim/cost-center";            
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("companyid", companyid);
            //session = HibernateUtil.getCurrentSession();

            JSONObject jobj = apiCallHandlerService.restGetMethod(eclaimURL, userData.toString());
//            JSONObject jobj = apiCallHandlerService.callApp(eclaimURL, userData, companyid, action);
            JSONArray jArray = jobj.getJSONArray("data");

            //Fetched eClaim field params     
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("filter_names", Arrays.asList("companyid", "isforeclaim"));
            requestParams.put("filter_values", Arrays.asList(companyid, 1));

            result = accMasterItemsDAOobj.getFieldParams(requestParams);

            List list = result.getEntityList();
            Iterator itr = list.iterator();
            //Loop on fields params having isClaim true
            while (itr.hasNext()) {
                FieldParams fieldParams = (FieldParams) itr.next();
                //Loop to set combo values 
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject jobjData = jArray.getJSONObject(i);
                    
                    String appuiid = "";
                    String eclaimid = jobjData.get("id").toString();
                    String projectname = ((String) jobjData.get("costcenterid")).trim();    //To avoid leading & trailing white spaces
                    String projectDescription = ((String) jobjData.get("name")).trim();     //To avoid leading & trailing white spaces
                    if (jobjData.has("appuiid")) {
                        appuiid = (String) jobjData.get("appuiid");
                    }
                    //====================================================================================//
                    //Avoid duplicate entries - ERP-29487
                    /* 
                      Suppose, ERP & eClaim has same master item/cost center name & user try to sync it then there were possibility that we get
                      * duplicate entries with same name in ERP & eClaim. Here we check whether any master item present with the same name as the cost center
                      * from eClaim, if we get such record we will not add this entry to ERP. In this scenario, cost center from eClaim & master item from ERP
                      * will be consider as different entries even though their names are identical.
                     */
                    HashMap<String, Object> dupparams = new HashMap<String, Object>();
                    ArrayList filter_keys = new ArrayList(), filter_values = new ArrayList();
                    filter_keys.add("value");
                    filter_values.add(projectDescription);
                    filter_keys.add("fieldid");
                    filter_values.add(fieldParams.getId());
                    filter_keys.add("isforeclaim");
                    filter_values.add(1);
                    filter_keys.add("field.companyid");
                    filter_values.add(fieldParams.getCompanyid());
                    dupparams.put("filter_names", filter_keys);
                    dupparams.put("filter_params", filter_values);
                    KwlReturnObject result2 = accMasterItemsDAOobj.getMasterItemsForCustom(dupparams);
                    Iterator mitrItr = result2.getEntityList().iterator();
                    if (mitrItr.hasNext()) {
                        if (!cclist.contains(projectDescription)) {
                            cclist.add(projectDescription);
                        }
                        continue;            //If ERP & eClaim having the same Cost Center name then do not add this Cost center any of the side.            
                    }
                    //====================================================================================//
                    
                    //Check for duplicate. If not present then insert.
                    HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                    
//                  filter_names.add("eclaimid");
                    filter_names.add("id");

//                  filter_params.add(eclaimid);
                    filter_params.add(appuiid);
                    filterRequestParams.put("filter_names", filter_names);
                    filterRequestParams.put("filter_params", filter_params);
                    KwlReturnObject cntResult = accMasterItemsDAOobj.getMasterItemsForCustom(filterRequestParams);
                    
                    List listItems = cntResult.getEntityList();
                    Iterator itrItems = listItems.iterator();
                    HashMap requestParam = new HashMap();
                    
                    requestParam.put("name", projectname);
                    requestParam.put("itemdescription", projectDescription);
                    requestParam.put("groupid", fieldParams.getId());
                    requestParam.put("eclaimid", eclaimid);
                    requestParam.put("activatedeactivateflg", true);    //ERP-29487 : Status of master item at ERP side should be by default "Activate"
                    if (itrItems.hasNext()) {
                        FieldComboData item = (FieldComboData) itrItems.next();
                       // requestParam.put("id", item.getId());
			String itemName = item.getValue();
                        HashMap<String, Object> params = new HashMap<String, Object>();
                        ArrayList filter_key = new ArrayList(), filter_value = new ArrayList();
                        filter_key.add("value");
                        filter_value.add(itemName);
                        filter_key.add("fieldid");
                        filter_value.add(fieldParams.getId());
                        filter_key.add("isforeclaim");
                        filter_value.add(1);
                        filter_key.add("field.companyid");
                        filter_value.add(fieldParams.getCompanyid());
                        params.put("filter_names", filter_key);
                        params.put("filter_params", filter_value);
                        KwlReturnObject itemresult = accMasterItemsDAOobj.getMasterItemsForCustom(params);
                        Iterator mitrItems =  itemresult.getEntityList().iterator();
                        if (mitrItems.hasNext()) {
                            FieldComboData comboitem = (FieldComboData) mitrItems.next();
                            requestParam.put("id", comboitem.getId());                            
                        }
                        result = accMasterItemsDAOobj.addUpdateMasterCustomItem(requestParam, false);
                    } else if(!StringUtil.isNullOrEmpty(appuiid)){
                        requestParam.put("id", appuiid);
			result = accMasterItemsDAOobj.addUpdateMasterCustomItem(requestParam, false);
                    }
                    //result = accMasterItemsDAOobj.addUpdateMasterCustomItem(requestParam, false);     
                }
            }
            for(int i=0; i<cclist.size(); i++){
                if(!StringUtil.isNullOrEmpty(cclistmsg)){
                    cclistmsg += ", ";
                }
                cclistmsg += ("<b>"+(String)cclist.get(i)+"</b>"); //List of Cost Center which I have not synced   -   ERP-29487
            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
//        finally {
//            HibernateUtil.closeSession(session);
//        }
        return cclistmsg;
    }
    
     public ModelAndView syncCustomFieldDataFromOtherProjects(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("MI_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        JSONObject jsonobj;
        try {
            int mapWithFieldType=Integer.parseInt(request.getParameter("mapWithFieldType"));
            syncCustomFieldDataFromOtherProjects(request,mapWithFieldType);
            issuccess = true;
            msg = messageSource.getMessage("acc.field.DataSyncedsuccessfullyfromDeskeraLMS", null, RequestContextUtils.getLocale(request));
            txnManager.commit(status);

        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    
    public void syncCustomFieldDataFromOtherProjects(HttpServletRequest request,int mapWithFieldType) throws ServiceException {
            //Session session =null;
        try {
            KwlReturnObject result = null;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyId = sessionHandlerImpl.getCurrencyID(request);
            KwlReturnObject extracompanyprefObjresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(),companyid);
            ExtraCompanyPreferences extracompanyobj = (ExtraCompanyPreferences)extracompanyprefObjresult.getEntityList().get(0);
             
            //Fetched data from Deskera LMS
//            String action = "32";
//            String lmsURL = this.getServletContext().getInitParameter("lmsURL");
            String lmsURL = URLUtil.buildRestURL("lmsURL");
            lmsURL = lmsURL + "academic/dimention";            
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("companyid", companyid);
            userData.put("mapWithFieldType", mapWithFieldType);
     
            //session = HibernateUtil.getCurrentSession();

            JSONObject jobj = apiCallHandlerService.restGetMethod(lmsURL, userData.toString());
//            JSONObject jobj = apiCallHandlerService.callApp(lmsURL, userData, companyid, action);
   
            JSONArray jArray = jobj.getJSONArray("data");
            
           
            //Fetched Project field params     
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("filter_names", Arrays.asList("companyid", "mapwithtype"));
            requestParams.put("filter_values", Arrays.asList(companyid, mapWithFieldType));

            result = accMasterItemsDAOobj.getFieldParams(requestParams);


            List list = result.getEntityList();
            Iterator itr = list.iterator();
            //Loop on fields params having isProject true
            while (itr.hasNext()) {
                FieldParams fieldParams = (FieldParams) itr.next();
                //Loop to set combo values 
                for (int i = 0; i < jArray.length(); i++) {
                    JSONObject jobjData = jArray.getJSONObject(i);


                    String mapwithfieldid = (String) jobjData.get("id");
                    String lmsfieldname = (String) jobjData.get("name");
                    if(StringUtil.isNullOrEmpty(lmsfieldname)){
                        continue;
                    }
                    //Check for duplicate. If not present then insert.
                    HashMap<String, Object> filterRequestParams = new HashMap<String, Object>();
                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();

                    filter_names.add("id");
                    filter_names.add("field.id");

                    filter_params.add(mapwithfieldid+"-"+fieldParams.getModuleid());
                    filter_params.add(fieldParams.getId());
                    filterRequestParams.put("filter_names", filter_names);
                    filterRequestParams.put("filter_params", filter_params);
                    KwlReturnObject cntResult = accMasterItemsDAOobj.getMasterItemsForCustom(filterRequestParams);

                    List listItems = cntResult.getEntityList();
                    Iterator itrItems = listItems.iterator();
                    HashMap requestParam = new HashMap();
                        
                    requestParam.put("name", lmsfieldname);
                    requestParam.put("groupid", fieldParams.getId());
                    requestParam.put("mapwithfieldid", mapwithfieldid+"-"+fieldParams.getModuleid());
                    if (itrItems.hasNext()) {
                        FieldComboData item = (FieldComboData) itrItems.next();
                        requestParam.put("id", item.getId());
                    }
                    result = accMasterItemsDAOobj.addUpdateMasterCustomItem(requestParam,false);
                }
            }
           
            
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
//        finally {
//            HibernateUtil.closeSession(session);
//
//        }
    }
    
    private Account saveAccount(HttpServletRequest request, String accountName, String companyId, String currencyId, boolean isCpAccount) {
        Account account = null;
        try {
            DateFormat dateFormat = authHandler.getGlobalDateFormat();
            Date creationDate = new Date();
            ExtraCompanyPreferences companyPreferences = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject(ExtraCompanyPreferences.class.getName(), companyId);
            if (companyPreferences != null) {
                String accountPrefixName = "";
                String typeId = "";
                if (isCpAccount) {
                    accountPrefixName = companyPreferences.getCpAccountPrefix();
                    typeId = companyPreferences.getCpAccountTypeId();
                } else {
                    accountPrefixName = companyPreferences.getWipAccountPrefix();
                    typeId = companyPreferences.getWipAccountTypeId();
                }
                KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
                Company company = (Company) custumObjresult.getEntityList().get(0);
                if (company.getCreator() != null) {
                    creationDate = authHandler.getUserNewDate(null, company.getCreator().getTimeZone()!=null?company.getCreator().getTimeZone().getDifference() : company.getTimeZone().getDifference());
                }
                String auditID = AuditAction.ACCOUNT_CREATED;
                JSONObject accjson = new JSONObject();

                accjson.put("name", accountName);
                accjson.put("balance", 0.0);
                accjson.put("minbudget", 0.0);
                accjson.put("groupid", typeId);
                accjson.put("companyid", companyId);
                accjson.put("currencyid", currencyId);
                accjson.put("life", 10);
                accjson.put("budget", 0.0);
                accjson.put("taxid", "");
                accjson.put("creationdate", creationDate);
                accjson.put("eliminateflag", false);

                KwlReturnObject accresult = accAccountDAOobj.addAccount(accjson);
                account = (Account) accresult.getEntityList().get(0);

                auditTrailObj.insertAuditLog(auditID, "Account : " + accountName + " has been created through Project Management Integration.", request, account.getID());

            }
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return account;
    }
    
    

    public ModelAndView deleteInvoiceSalesTerms(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            deleteInvoiceSalesTerms(request);
            txnManager.commit(status);
            msg = messageSource.getMessage("acc.field.Termdeletedsuccessfully", null, RequestContextUtils.getLocale(request));
            issuccess = true;
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void deleteInvoiceSalesTerms(HttpServletRequest request) throws SessionExpiredException, AccountingException, ServiceException {
        JSONObject resObj=new JSONObject();
        //Session session=null;
        String termid = request.getParameter("termid");
        String term = request.getParameter("term");
        String companyid = sessionHandlerImpl.getCompanyid(request);
        boolean isTermUsed = false;
        String moduleNames = "";
        // check term is used in formula or not
        KwlReturnObject result = accAccountDAOobj.findTermUsedInFormula(termid);
        if(result.getRecordTotalCount()>0) {
            isTermUsed = true;
            moduleNames += "Term Formula, ";
        }
        // check term is used in Tax or not
        result = accAccountDAOobj.findTermUsedInTax(termid);
        if(result.getRecordTotalCount()>0) {
            isTermUsed = true;
            moduleNames += "Tax, ";
        }
        
        boolean isSalesOrPurchase =StringUtil.isNullOrEmpty(request.getParameter("isSalesOrPurchase"))?false:Boolean.parseBoolean(request.getParameter("isSalesOrPurchase"));
        if (isSalesOrPurchase) {
            // check term is used in any Sales Invoices
            result = accAccountDAOobj.findTermUsedInTransaction(termid);
            if (result.getRecordTotalCount() > 0) {
                isTermUsed = true;
                moduleNames += "Sales Invoice, ";
            }
            // check term is used in any Customer Quotation
            result = accAccountDAOobj.findTermUsedInQuotation(termid);
            if (result.getRecordTotalCount() > 0) {
                isTermUsed = true;
                moduleNames += "Customer Quotation, ";
            }
            // check term is used in any SO
            result = accAccountDAOobj.findTermUsedInSO(termid);
            if (result.getRecordTotalCount() > 0) {
                isTermUsed = true;
                moduleNames += "Sales Order, ";
            }
        } else {
            // check term is used in any Purchase Invoices
            result = accAccountDAOobj.findTermUsedInPI(termid);
            if (result.getRecordTotalCount() > 0) {
                isTermUsed = true;
                moduleNames += "Purchase Invoice, ";
            }
            // check term is used in any Vendor Quotation
            result = accAccountDAOobj.findTermUsedInVQ(termid);
            if (result.getRecordTotalCount() > 0) {
                isTermUsed = true;
                moduleNames += "Vendor Quotation, ";
            }
            // check term is used in any SO
            result = accAccountDAOobj.findTermUsedInPO(termid);
            if (result.getRecordTotalCount() > 0) {
                isTermUsed = true;
                moduleNames += "Purchase Order, ";
            }
        }
        
        // check term is used in any DO
        result = accAccountDAOobj.findTermUsedInDO(termid);
        if (result.getRecordTotalCount() > 0) {
            isTermUsed = true;
            moduleNames += "Delivery Order, ";
        }
        // check term is used in any GRO
        result = accAccountDAOobj.findTermUsedInGRO(termid);
        if (result.getRecordTotalCount() > 0) {
            isTermUsed = true;
            moduleNames += "Goods Receipt, ";
        }
        
        if(isTermUsed) {
            moduleNames = moduleNames.substring(0, moduleNames.length()-2);
            throw new AccountingException(messageSource.getMessage("acc.field.SelectedTermisusedinModules.SocannotbeDeleted", new Object[]{moduleNames}, RequestContextUtils.getLocale(request)));
        }

        ExtraCompanyPreferences cmpPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject(ExtraCompanyPreferences.class.getName(), companyid);
        if (cmpPref.isActivateCRMIntegration() && isSalesOrPurchase) {
            try {
//                String crmURL = this.getServletContext().getInitParameter("crmURL");
                JSONObject userData = new JSONObject();
                userData.put("iscommit", true);
                userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
                userData.put("companyid", companyid);
                userData.put("termid", termid);
                //session = HibernateUtil.getCurrentSession();
//                String action = "223";
                String crmURL = URLUtil.buildRestURL(Constants.crmURL);
                crmURL = crmURL + "master/invoiceterm";
                resObj = apiCallHandlerService.restDeleteMethod(crmURL, userData.toString());
//                resObj = apiCallHandlerService.callApp(crmURL, userData, companyid, action);
                if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                    deleteInvoiceSalesTermsCommonCode(request, termid, term, isSalesOrPurchase);
                } else {
                    String msg = resObj.has("msg") ? resObj.getString("msg") : "Error Occurred at server side.";
                    throw new AccountingException(msg);
                }
            } catch (JSONException e) {
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, e);
            }
        } else {
            deleteInvoiceSalesTermsCommonCode(request, termid, term, isSalesOrPurchase);
        }
    }
    
    public void deleteInvoiceSalesTermsCommonCode(HttpServletRequest request, String termid, String term, boolean isSalesOrPurchase) throws ServiceException, SessionExpiredException {
        HashMap<String, Object> hm = new HashMap();
        hm.put("id", termid);
        hm.put("deleted", 1);
        KwlReturnObject kwlreturn = accAccountDAOobj.saveInvoiceTerm(hm);

        String actionStr = "purchase term";
        String auditaction = AuditAction.PURCHASE_TERM_DELETED;
        if (isSalesOrPurchase == true) {
            actionStr = "sales term";
            auditaction = AuditAction.SALES_TERM_DELETED;
        }
        auditTrailObj.insertAuditLog(auditaction, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted " + actionStr + " " + term, request, termid);
    }
    
    public String getCustomFieldJson(String customcolumndetails) {
        JSONArray returnArr = new JSONArray();
        String ReturnString = "";
        try {
            JSONArray customColumnsArray = new JSONArray(customcolumndetails);
            for (int i = 0; i < customColumnsArray.length(); i++) {
//                FieldParams fieldParams = 
                
                JSONObject returnObject = new JSONObject();
                JSONObject customColumn = customColumnsArray.getJSONObject(i);
                KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(FieldParams.class.getName(), customColumn.optString("fieldid"));
                FieldParams fieldParams = (FieldParams)custumObjresult.getEntityList().get(0);
                returnObject.put("refcolumn_name", customColumn.optString("refcolumn_number"));
                returnObject.put("fieldname", customColumn.optString("fieldname"));
                returnObject.put("xtype", customColumn.optInt("fieldtype"));
                if(fieldParams!=null){
                    returnObject.put("col"+fieldParams.getColnum(), customColumn.optString("fieldData"));
                    returnObject.put(customColumn.optString("fieldname"), "col"+fieldParams.getColnum());
                }else{
                    returnObject.put(customColumn.optString("column_number"), customColumn.optString("fieldData"));
                    returnObject.put(customColumn.optString("fieldname"), customColumn.optString("column_number"));
                }
                
                returnArr.put(returnObject);
            }
            ReturnString = returnArr.toString();
        } catch (ServiceException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ReturnString;
    }
    
    public void saveShelfLocationData(String shelfLocationDetails, String companyId) {
        HashMap<String, Object> valueMap = null;
        try {
            JSONArray shelfLocationArray = new JSONArray(shelfLocationDetails);
            for (int i = 0; i < shelfLocationArray.length(); i++) {
                valueMap = new HashMap<String, Object>();
                JSONObject shelfLocations = shelfLocationArray.getJSONObject(i);
                String shelfLocationId = shelfLocations.optString("shelfLocationId");
                String shelfLocationValue = shelfLocations.optString("shelfLocationValue");
                valueMap.put("shelfLocationId", shelfLocationId);
                valueMap.put("shelfLocationValue", shelfLocationValue);
                valueMap.put("companyId", companyId);
                KwlReturnObject returnObject = accProductObj.saveShelfLocationData(valueMap);
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (JSONException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    
    /**
     * Firstly delete all shelf locations and then save these when coming from inventory.
     * @param shelfLocationDetails
     * @param companyId 
     */
    public void saveShelfLocations(String shelfLocationDetails, String companyId){
        try {
            int deletedRows = accProductObj.deleteShelfLacation(companyId);
            saveShelfLocationData(shelfLocationDetails, companyId);
        } catch (ServiceException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
    }
    
    
    public ModelAndView getInvProducts(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONObject resObj=new JSONObject();
        //Session session =null;
        String msg = "";
        boolean issuccess = false;
        boolean isShelfLocationSaved=false;//This flag is used for save shelf locations only one time.
        try {
            boolean iterate = false;
            int o = 0;
            int l = 100;
            do{
            String companyid = sessionHandlerImpl.getCompanyid(request);
            Date newUserDate = new Date();
            KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) custumObjresult.getEntityList().get(0);
            if (company.getCreator() != null) {
               newUserDate = authHandler.getUserNewDate(null, company.getCreator().getTimeZone()!=null?company.getCreator().getTimeZone().getDifference() : company.getTimeZone().getDifference());
            }
            String currencyid=sessionHandlerImpl.getCurrencyID(request);
            String inventoryURL = this.getServletContext().getInitParameter("inventoryURL");
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", sessionHandlerImpl.getUserid(request));
            userData.put("companyid", companyid);
            userData.put("start", o);
            userData.put("limit", l);
            //session = HibernateUtil.getCurrentSession();
            String action = "701";
            //userData.put("data", pjobj);                                    
            int totalRows = 0;                    
            try{
                resObj = apiCallHandlerService.callApp(inventoryURL, userData, companyid, action);
            }catch(Exception ex){            
            }    
//            finally{
//                session.close();
//            }    
            if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                
                
                JSONArray productsArr = resObj.getJSONArray("data");
                if(productsArr.length()>0){                   
                    totalRows = productsArr.length();
                }
                if(totalRows >= l){
                    o+=l;
                    iterate = true;
                }else{
                    iterate = false;
                }
                if(productsArr.length()>0){
                                   
                //Create transaction
                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                def.setName("JE_Tx");
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
                 TransactionStatus status = txnManager.getTransaction(def);  
                
                    if (!isShelfLocationSaved) { // inserting shelf Locations For Products firstly.                        
                        JSONArray shelfLocationDetailsArr = resObj.getJSONArray("shelfLocationDetails");
                        try {
                            String shelfLocationDetails = shelfLocationDetailsArr.toString();
                            saveShelfLocations(shelfLocationDetails, companyid);
                        } catch (Exception e) {
                            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, e.getMessage(), e);
                        }
                        isShelfLocationSaved = true;
                    } 
                    
                HashMap<String, Object> defaultAccountMap=new HashMap<String, Object>();  
                defaultAccountMap.put("companyid", companyid);
                DefaultsForProduct defaultsForProduct = accProductObj.getDefaultAccountsForProduct(defaultAccountMap);
                for (int i = 0; i < productsArr.length(); i++) {
                    JSONObject productObject = productsArr.getJSONObject(i);
                    try {
                        KwlReturnObject productresult;
                        String auditMsg = "added";
                        String auditID = AuditAction.PRODUCT_CREATION;
                        
                        String productCode = productObject.optString("productCode");
                        String productName = productObject.optString("productName");
                        //String sellingUOMid=(productObject.isNull("sellingUOM"))?"":productObject.optString("sellingUOM");
                        //String sellingUOMname=(productObject.isNull("sellingUOMName"))?"":(productObject.getString("sellingUOMName").equals("-"))?"":productObject.optString("sellingUOMName");
                        //String purchaseUOMid=(productObject.isNull("purchasingUOM"))?"":productObject.optString("purchasingUOM");
                        //String purchaseUOMname=(productObject.isNull("purchasingUOMName"))?"":(productObject.getString("purchasingUOMName").equals("-"))?"":productObject.optString("purchasingUOMName");
                        String stockUOMid=(productObject.isNull("stockUOM"))?"":productObject.optString("stockUOM");
                        String stockUOMname=(productObject.isNull("stockUOMName"))?"":(productObject.getString("stockUOMName").equals("-"))?"":productObject.optString("stockUOMName");
                        //String stocksalesuomvalue=(productObject.isNull("sellingStock"))?"1":productObject.optString("sellingStock");
                        String shelfLocationId=(productObject.isNull("shelfLocationId"))?"":productObject.optString("shelfLocationId");
                        String DefaultLocation = (productObject.isNull("orderLocationId")) ? "" : productObject.optString("orderLocationId","");
                        String DefaultWarehouse=(productObject.isNull("orderStoreId"))?"":productObject.optString("orderStoreId","");
                        
                        //String stockpurchaseuomvalue=(productObject.isNull("purchasingStock"))?"1":productObject.optString("purchasingStock");
                        boolean isQAenable=(!productObject.isNull("isQAenable") && productObject.optBoolean("isQAenable", false)==true)?true:false;
                        
                        String supplierpartnumber=(productObject.isNull("supplierpartnumber"))?"":(StringUtil.isNullOrEmpty(productObject.optString("supplierpartnumber","")))?"":productObject.getString("supplierpartnumber");
                        String partnumber=(productObject.isNull("partnumber"))?"":(StringUtil.isNullOrEmpty(productObject.optString("partnumber","")))?"":productObject.getString("partnumber");
                        String customerpartnumber=(productObject.isNull("customerpartnumber"))?"":(StringUtil.isNullOrEmpty(productObject.optString("customerpartnumber","")))?"":productObject.getString("customerpartnumber");

                        String customcolumndetails = productObject.getString("customcolumndetails");

                        Date appDate = new Date();
                        HashMap<String, Object> productMap = new HashMap<String, Object>();
                        productMap.put("parentid", null);

                       
//                      productMap.put("uomid", defaultsForProduct.getUnitOfMeasure().getID());
                        productMap.put("purchaseaccountid", defaultsForProduct.getPaccount().getID());
                        productMap.put("salesaccountid", defaultsForProduct.getSaccount().getID());
                        productMap.put("purchaseretaccountid", defaultsForProduct.getPaccount().getID());
                        productMap.put("salesretaccountid", defaultsForProduct.getSaccount().getID());
                        productMap.put("leadtime", 0);
                        productMap.put("warrantyperiod", 0);
                        productMap.put("warrantyperiodsal", 0);
                        productMap.put("reorderlevel", 0.0);
                        productMap.put("reorderquantity", 0.0);
                        productMap.put("companyid", companyid);
                        productMap.put("syncable", false);
                        productMap.put("multiuom", true);
                        productMap.put("name", productName);
                        //productMap.put("stockpurchaseuomvalue",stockpurchaseuomvalue);
                        //productMap.put("stocksalesuomvalue",stocksalesuomvalue);
                        productMap.put("supplier",supplierpartnumber);
                        productMap.put("coilcraft",partnumber);                        
                        productMap.put("interplant",customerpartnumber);
                        productMap.put("shelfLocationId",shelfLocationId);
                        productMap.put("isQAenable", isQAenable);
                        productMap.put("shelfLocationId",shelfLocationId);
                        productMap.put("location", DefaultLocation);
                        productMap.put("warehouse",DefaultWarehouse);
                        productMap=setUOMforProduct(productMap,stockUOMid,stockUOMname,"uomid",companyid);
                        //productMap=setUOMforProduct(productMap,purchaseUOMid,purchaseUOMname,"purchaseuom",companyid);
                       // productMap=setUOMforProduct(productMap,sellingUOMid,sellingUOMname,"salesuom",companyid);
                        
                        String productid = "";
                        KwlReturnObject proObject = accProductObj.getProduct(productCode, companyid);
                        if (!proObject.getEntityList().isEmpty()) {
                            Product productObj = (Product) proObject.getEntityList().get(0);
                            productid = productObj.getID();
                        }

                        Product product=null;
                        if (StringUtil.isNullOrEmpty(productid)) {
                            productMap.put("name", productName);

                            productMap.put("producttype", defaultsForProduct.getProducttype().getID());
                            productMap.put("productid", productCode);
                            
                            productresult = accProductObj.addProduct(productMap);
                            product = (Product) productresult.getEntityList().get(0);
                            HashMap<String, Object> purchasePriceMap = new HashMap<String, Object>();
                            purchasePriceMap.put("price", productObject.optDouble("productPrice"));
                            purchasePriceMap.put("productid", product.getID());
                            purchasePriceMap.put("companyid", companyid);
                            purchasePriceMap.put("carryin", true);
                            purchasePriceMap.put("applydate", appDate);  
                            purchasePriceMap.put("currencyid", currencyid);  
                            purchasePriceMap.put("uomid", product.getUnitOfMeasure().getID());
                            accProductObj.addPriceList(purchasePriceMap);

                            HashMap<String, Object> sellingPriceMap = new HashMap<String, Object>();
                            sellingPriceMap.put("price", productObject.optDouble("sellingPrice"));
                            sellingPriceMap.put("productid", product.getID());
                            sellingPriceMap.put("companyid", companyid);
                            sellingPriceMap.put("carryin", false);
                            sellingPriceMap.put("applydate", appDate);       
                            purchasePriceMap.put("currencyid", currencyid);  
                            sellingPriceMap.put("uomid", product.getUnitOfMeasure().getID());
                            accProductObj.addPriceList(sellingPriceMap);
                           
                            JSONArray customColumnArray=productObject.getJSONArray("customcolumndetails");
                            System.out.println(product.getName());
                            customColumnArray=insertfield(customColumnArray,companyid);
                        } else {

                            auditMsg = "updated";
                            auditID = AuditAction.PRODUCT_UPDATION;
                            productMap.put("id", productid);
                            productresult = accProductObj.updateProduct(productMap);
                            product = (Product) productresult.getEntityList().get(0);
                            if (Boolean.parseBoolean(request.getParameter("editQuantity")) == true) {        // Update initial quantity while product edit 
                                double quantity = jobj.optDouble("quantity");
                                JSONObject inventoryjson = new JSONObject();
                                inventoryjson.put("productid", productid);
                                inventoryjson.put("quantity", quantity);
                                inventoryjson.put("description", "Inventory Opened");
                                inventoryjson.put("carryin", true);
                                inventoryjson.put("defective", false);
                                inventoryjson.put("newinventory", true);
                                inventoryjson.put("companyid", companyid);
                                inventoryjson.put("updatedate", newUserDate);
                                accProductObj.updateInitialInventory(inventoryjson);
                            }
                           
                            product = (Product) productresult.getEntityList().get(0);
                            HashMap<String, Object> purchasePriceMap = new HashMap<String, Object>();
                            purchasePriceMap.put("price", productObject.optDouble("productPrice"));
                            purchasePriceMap.put("productid", product.getID());
                            purchasePriceMap.put("companyid", companyid);
                            purchasePriceMap.put("carryin", true);
                            purchasePriceMap.put("applydate", appDate);  
                            purchasePriceMap.put("currencyid", currencyid);  
                            purchasePriceMap.put("uomid", product.getUnitOfMeasure().getID());
                            accProductObj.addPriceList(purchasePriceMap);

                            HashMap<String, Object> sellingPriceMap = new HashMap<String, Object>();
                            sellingPriceMap.put("price", productObject.optDouble("sellingPrice"));
                            sellingPriceMap.put("productid", product.getID());
                            sellingPriceMap.put("companyid", companyid);
                            sellingPriceMap.put("carryin", false);
                            sellingPriceMap.put("applydate", appDate);   
                            sellingPriceMap.put("currencyid", currencyid);  
                            sellingPriceMap.put("uomid", product.getUnitOfMeasure().getID());
                            accProductObj.addPriceList(sellingPriceMap);
                           
                            JSONArray customColumnArray=productObject.getJSONArray("customcolumndetails");
                            System.out.println(product.getName());
                            customColumnArray=insertfield(customColumnArray,companyid);
                        }
                        
//                        accProductObj.deleteProductCustomData(product.getID());
                        System.out.println(product.getName());
                        
                        String customfield = getCustomFieldJson(customcolumndetails);
                        if (!StringUtil.isNullOrEmpty(customfield)) {
                            JSONArray jcustomarray = new JSONArray(customfield);
                            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                            customrequestParams.put("customarray", jcustomarray);
                            customrequestParams.put("modulename", Constants.Acc_Product_modulename);
                            customrequestParams.put("moduleprimarykey", Constants.Acc_Productid);
                            customrequestParams.put("modulerecid", product.getID());
                            customrequestParams.put("moduleid", Constants.Acc_Product_Master_ModuleId);
                            customrequestParams.put("companyid", companyid);
                            productMap.put("id", product.getID());
                            customrequestParams.put("customdataclasspath", Constants.Acc_Product_custom_data_classpath);
                            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
//                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
//                                productMap.put("accproductcustomdataref", product.getID());
//                                productresult = accProductObj.updateProduct(productMap);
//                            }
                        }
                       
//                        if (!defaultsForProduct.getProducttype().getName().equalsIgnoreCase(Producttype.SERVICE)) {
//                            int interval = 30;
//                            int tolerance = 0;
////                cyclecount.makeProductCyclecountEntry(session,product,productid,interval,tolerance);
//                            HashMap<String, Object> cycleParams = new HashMap<String, Object>();
//                            cycleParams.put("productid", product.getID());
//                            cycleParams.put("interval", interval);
//                            cycleParams.put("tolerance", tolerance);
//                            accProductObj.saveProductCycleCount(cycleParams);
//                        }

                        Inventory inventory;
                        double quantity;
                        try {
                            quantity = jobj.optDouble("quantity");
                        } catch (Exception e) {
                            quantity = 0;
                        }
                        if (quantity > 0) {
                            if (StringUtil.isNullOrEmpty(productid)) {
//                    makeNewInventory(session, request, product, quantity, "Inventory Opened", true, false);
                                JSONObject inventoryjson = new JSONObject();
                                inventoryjson.put("productid", product.getID());
                                inventoryjson.put("quantity", quantity);
                                inventoryjson.put("description", "Inventory Opened");
                                inventoryjson.put("carryin", true);
                                inventoryjson.put("defective", false);
                                inventoryjson.put("newinventory", true);
                                inventoryjson.put("companyid", companyid);
                                inventoryjson.put("updatedate", newUserDate);
                                KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
                                inventory = (Inventory) invresult.getEntityList().get(0);

//                    updateAssemblyInventory(session, request, quantity, "", "Inventory Opened", product);
                                HashMap<String, Object> assemblyParams = getAssemblyRequestParams(request);
                                assemblyParams.put("memo", "Inventory Opened");
                                assemblyParams.put("refno", "");
                                assemblyParams.put("buildproductid", product.getID());
                                accProductObj.updateAssemblyInventory(assemblyParams);
                            } else {
//                    Company company = (Company) session.get(Company.class, AuthHandler.getCompanyid(request));
//                    String selQuery = "select id from Inventory   where product.ID =? and newinv='T' and company.companyID=?";
//                    List list = HibernateUtil.executeQuery(session, selQuery, new Object[]{productid, company.getCompanyID()});
                                HashMap<String, Object> inventoryFilter = new HashMap<String, Object>();
                                inventoryFilter.put("productid", productid);
                                inventoryFilter.put("companyid", companyid);
                                KwlReturnObject result = accProductObj.getInventoryEntry(inventoryFilter);
                                List list = result.getEntityList();

                                if (list.isEmpty()) {

                                    JSONObject inventoryjson = new JSONObject();
                                    inventoryjson.put("productid", product.getID());
                                    inventoryjson.put("quantity", quantity);
                                    inventoryjson.put("description", "Inventory Opened");
                                    inventoryjson.put("carryin", true);
                                    inventoryjson.put("defective", false);
                                    inventoryjson.put("newinventory", true);
                                    inventoryjson.put("companyid", companyid);
                                    inventoryjson.put("updatedate", newUserDate);
                                    KwlReturnObject invresult = accProductObj.addInventory(inventoryjson);
                                    inventory = (Inventory) invresult.getEntityList().get(0);

                                    HashMap<String, Object> assemblyParams = getAssemblyRequestParams(request);
                                    assemblyParams.put("memo", "Inventory Opened");
                                    assemblyParams.put("refno", "");
                                    assemblyParams.put("buildproductid", product.getID());
                                    accProductObj.updateAssemblyInventory(assemblyParams);
                                }                               
                            }
                        }
                        String pDescription = StringUtil.isNullOrEmpty(product.getDescription()) ? "" : " (" + product.getDescription() + ")";
                        auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " " + auditMsg + " product " + product.getName() + pDescription, request, product.getID());
                        if (auditMsg.equals("added")) {
                            auditTrailObj.insertAuditLog(AuditAction.PRODUCT_CREATION, "User " + sessionHandlerImpl.getUserFullName(request) + " added Product \"" + product.getName() + "\" in Inventory", request, product.getID());
                        }
                        } catch (NumberFormatException ex) {
                            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                            txnManager.rollback(status);
                        } catch (SessionExpiredException ex) {
                                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                                txnManager.rollback(status);
                       } catch (JSONException ex) {
                                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                                txnManager.rollback(status);
                       } catch (Exception ex) {
                                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                                txnManager.rollback(status);           
                       }
                   
                    }
                issuccess = resObj.getBoolean("success");
                msg = messageSource.getMessage("acc.field.Inventoryproductsaresyncedsuccessfully", null, RequestContextUtils.getLocale(request));
                jobj.put("success", true);
                jobj.put("msg", msg);
                txnManager.commit(status);
                }               
            }           
            }while(iterate);
        } catch (Exception ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
            //HibernateUtil.closeSession(session);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView getLMSCourcesAsProducts(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONObject resObj=new JSONObject();
        //Session session =null;
        String msg = "";
        boolean issuccess = false;
        boolean isShelfLocationSaved=false;//This flag is used for save shelf locations only one time.
        try {
            boolean iterate = false;
            int o = 0;
            int l = 100;
            do{
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid=sessionHandlerImpl.getCurrencyID(request);
//            String lmsURL = this.getServletContext().getInitParameter("lmsURL");
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", sessionHandlerImpl.getUserid(request));
            userData.put("companyid", companyid);
            userData.put("start", o);
            userData.put("limit", l);
            //session = HibernateUtil.getCurrentSession();
//            String action = "29";
            String lmsURL = URLUtil.buildRestURL("lmsURL");
            lmsURL = lmsURL + "academic/fee-types";            
            //userData.put("data", pjobj);                                    
            int totalRows = 0;                    
            try{
                resObj = apiCallHandlerService.restGetMethod(lmsURL, userData.toString());
//                resObj = apiCallHandlerService.callApp(lmsURL, userData, companyid, action);
            }catch(Exception ex){
            }   
//            finally{
//                session.close();
//            }    
            if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                
                
                JSONArray productsArr = resObj.getJSONArray("data");
                if(productsArr.length()>0){                   
                    totalRows = productsArr.length();
                }
                if(totalRows >= l){
                    o+=l;
                    iterate = true;
                }else{
                    iterate = false;
                }
                if(productsArr.length()>0){
                
                     //Create transaction
                DefaultTransactionDefinition def = new DefaultTransactionDefinition();
                def.setName("JE_Tx");
                def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
                def.setIsolationLevel(TransactionDefinition.ISOLATION_READ_UNCOMMITTED);
                TransactionStatus status = txnManager.getTransaction(def);  
                try {                   
                 
                HashMap<String, Object> defaultAccountMap=new HashMap<String, Object>();  
                defaultAccountMap.put("companyid", companyid);
                defaultAccountMap.put("productType", "Service");
                DefaultsForProduct defaultsForProduct = accProductObj.getDefaultAccountsForProduct(defaultAccountMap);
                String revenueaccount="";
                String liabilityAccount="";
                Account liabilityAcc=null;
                Account revenueacc=null;
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                ExtraCompanyPreferences preferences = (ExtraCompanyPreferences) cap.getEntityList().get(0);
                    if (preferences.isRecurringDeferredRevenueRecognition()) {
                        KwlReturnObject cap1 = accountingHandlerDAOobj.getObject(Account.class.getName(), preferences.getSalesAccount());
                        Account salesaccount = (Account) cap1.getEntityList().get(0);
                        defaultsForProduct.setSaccount(salesaccount);
                        KwlReturnObject cap2 = accountingHandlerDAOobj.getObject(Account.class.getName(), preferences.getSalesRevenueRecognitionAccount());
                        revenueacc = (Account) cap2.getEntityList().get(0);
                        revenueaccount = revenueacc.getID();
                        KwlReturnObject cap3 = accountingHandlerDAOobj.getObject(Account.class.getName(), preferences.getLiabilityAccountForLMS());
                        liabilityAcc = (Account) cap3.getEntityList().get(0);
                        
                        if(liabilityAcc == null) {//if liability account not set for lms
                            throw new AccountingException("Please set Deposit(LMS) Account for LMS in System Preferences");
                        }
                        liabilityAccount = liabilityAcc.getID();
                    } else {
                        throw new AccountingException("Please set Advance sales and Revenue Recognition Account for LMS");
                    }
                    
                KwlReturnObject comResult= accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
                Company company = (Company) comResult.getEntityList().get(0);
                SimpleDateFormat sdf1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");    
                for (int i = 0; i < productsArr.length(); i++) {
                    JSONObject productObject = productsArr.getJSONObject(i);
                    
                        KwlReturnObject productresult;
                        String auditMsg = "added";
                        String auditID = AuditAction.PRODUCT_CREATION;
                        
//                        Date newdate=authHandler.getDateWithTimeFormat().parse(authHandler.getConstantDateFormatter().format(new Date()));
                        Date newdate=new Date();
                        String userdiff=company.getCreator().getTimeZone()!=null?company.getCreator().getTimeZone().getDifference() : company.getTimeZone().getDifference();
                        sdf1.setTimeZone(TimeZone.getTimeZone("GMT"+userdiff));
                        Date newcreatedate=authHandler.getDateWithTimeFormat().parse(sdf1.format(newdate));
                        
                        String productCode = productObject.optString("productCode","");
                        String productName = productObject.optString("productName","");
                        String description = productObject.optString("description","");
                        String DefaultLocation = (productObject.isNull("orderLocationId")) ? "" : productObject.optString("orderLocationId","");
                        String DefaultWarehouse=(productObject.isNull("orderStoreId"))?"":productObject.optString("orderStoreId","");
                        boolean isQAenable=(!productObject.isNull("isQAenable") && productObject.optBoolean("isQAenable", false)==true)?true:false;
                        if (productCode.equalsIgnoreCase("3") && liabilityAcc!=null) {
                            defaultsForProduct.setSaccount(liabilityAcc);
                        } else if (productCode.equalsIgnoreCase("2") && revenueacc!=null) {
                            defaultsForProduct.setSaccount(revenueacc);
                        }
//                        Date appDate = new Date();
                        HashMap<String, Object> productMap = new HashMap<String, Object>();
                        productMap.put("parentid", null);
                        if(defaultsForProduct.getPaccount()==null || defaultsForProduct.getSaccount()==null){
                           throw new AccountingException(" Purchases and Sales account not present.");                        
                        }
                        productMap.put("purchaseaccountid", defaultsForProduct.getPaccount().getID());
                        productMap.put("salesaccountid", defaultsForProduct.getSaccount().getID());
                        productMap.put("purchaseretaccountid", defaultsForProduct.getPaccount().getID());
                        productMap.put("salesretaccountid", defaultsForProduct.getSaccount().getID());
                        if(!StringUtil.isNullOrEmpty(revenueaccount)){
                            productMap.put("salesRevenueRecognitionAccountid", revenueaccount);
                        }
                        productMap.put("leadtime", 0);
                        productMap.put("currencyid",currencyid); 
                        productMap.put("warrantyperiod", 0);
                        productMap.put("warrantyperiodsal", 0);
                        productMap.put("reorderlevel", 0.0);
                        productMap.put("reorderquantity", 0.0);
                        productMap.put("companyid", companyid);
                        productMap.put("syncable", false);
//                        productMap.put("multiuom", true);
                        productMap.put("name", productName);
                        productMap.put("location", DefaultLocation);
                        productMap.put("warehouse", DefaultWarehouse);
                        ValuationMethod valMethod = ValuationMethod.FIFO;
                        if ((productMap.get("valuationmethod")) != null) {
                            int valuationMethod = Integer.parseInt(productMap.get("valuationmethod").toString());
                            for (ValuationMethod st : ValuationMethod.values()) {
                                if (st.ordinal() == valuationMethod) {
                                    valMethod = st;
                                    break;
                                }
                            }
                        }
                        productMap.put("valuationmethod", valMethod);
                        
                        String productid = "";
                        KwlReturnObject proObject = accProductObj.getProduct(productCode, companyid);
                        if (!proObject.getEntityList().isEmpty()) {
                            Product productObj = (Product) proObject.getEntityList().get(0);
                            productid = productObj.getID();
                        }

                        Product product=null;
                        if (StringUtil.isNullOrEmpty(productid)) {
                            productMap.put("name", productName);
                            productMap.put("desc", description);

                            productMap.put("producttype", defaultsForProduct.getProducttype().getID());
                            if (defaultsForProduct.getProducttype().getID().equals(Producttype.SERVICE)) {
                                KwlReturnObject uomreturnObj = accProductObj.getUOMByName("N/A", companyid);
                                UnitOfMeasure uom = null;
                                if (uomreturnObj != null && !uomreturnObj.getEntityList().isEmpty()) {
                                    uom = (UnitOfMeasure) uomreturnObj.getEntityList().get(0);
                                }
                                productMap.put("uomid", uom.getID());
                            } else {
                                productMap.put("uomid", defaultsForProduct.getUnitOfMeasure().getID());
                            }
                                productMap.put("productid", productCode);
                            
                            productresult = accProductObj.addProduct(productMap);
                            product = (Product) productresult.getEntityList().get(0);
                            HashMap<String, Object> purchasePriceMap = new HashMap<String, Object>();
                            purchasePriceMap.put("price", productObject.optDouble("productPrice",0.0));
                            purchasePriceMap.put("productid", product.getID());
                            purchasePriceMap.put("companyid", companyid);
                            purchasePriceMap.put("carryin", true);
                            purchasePriceMap.put("applydate", newcreatedate);  
                            purchasePriceMap.put("currencyid", currencyid);  
                            purchasePriceMap.put("uomid", product.getUnitOfMeasure().getID());
                            accProductObj.addPriceList(purchasePriceMap);

                            HashMap<String, Object> sellingPriceMap = new HashMap<String, Object>();
                            sellingPriceMap.put("price", productObject.optDouble("sellingPrice",0.0));
                            sellingPriceMap.put("productid", product.getID());
                            sellingPriceMap.put("companyid", companyid);
                            sellingPriceMap.put("carryin", false);
                            sellingPriceMap.put("applydate", newcreatedate);       
                            purchasePriceMap.put("currencyid", currencyid);  
                            sellingPriceMap.put("uomid", product.getUnitOfMeasure().getID());
                            accProductObj.addPriceList(sellingPriceMap);
                           
                        } else {

                            auditMsg = "updated";
                            auditID = AuditAction.PRODUCT_UPDATION;
                            productMap.put("id", productid);
                            productresult = accProductObj.updateProduct(productMap);
                            product = (Product) productresult.getEntityList().get(0);
                           
                            HashMap<String, Object> purchasePriceMap = new HashMap<String, Object>();
                            purchasePriceMap.put("price", productObject.optDouble("productPrice",0.0));
                            purchasePriceMap.put("productid", product.getID());
                            purchasePriceMap.put("companyid", companyid);
                            purchasePriceMap.put("carryin", true);
                            purchasePriceMap.put("applydate", newcreatedate);  
                            purchasePriceMap.put("currencyid", currencyid);  
                            purchasePriceMap.put("uomid", product.getUnitOfMeasure().getID());
                            accProductObj.addPriceList(purchasePriceMap);

                            HashMap<String, Object> sellingPriceMap = new HashMap<String, Object>();
                            sellingPriceMap.put("price", productObject.optDouble("sellingPrice",0.0));
                            sellingPriceMap.put("productid", product.getID());
                            sellingPriceMap.put("companyid", companyid);
                            sellingPriceMap.put("carryin", false);
                            sellingPriceMap.put("applydate", newcreatedate);   
                            sellingPriceMap.put("currencyid", currencyid); 
                            sellingPriceMap.put("uomid", product.getUnitOfMeasure().getID());
                            accProductObj.addPriceList(sellingPriceMap);
                           
                        }

                        String pDescription = StringUtil.isNullOrEmpty(product.getDescription()) ? "" : " (" + product.getDescription() + ")";
                        auditTrailObj.insertAuditLog(auditID, "User " + sessionHandlerImpl.getUserFullName(request) + " " + auditMsg + " product " + product.getName() + pDescription, request, product.getID());
                        if (auditMsg.equals("added")) {
                            auditTrailObj.insertAuditLog(AuditAction.PRODUCT_CREATION, "User " + sessionHandlerImpl.getUserFullName(request) + " added Product \"" + product.getName() + "\" in Inventory", request, product.getID());
                        }
                       
                   
                   }
                } catch (NumberFormatException ex) {
                    Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                    txnManager.rollback(status);
                } catch (SessionExpiredException ex) {
                        Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                        txnManager.rollback(status);
                } catch (JSONException ex) {
                        Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                        txnManager.rollback(status);    
                } catch (Exception ex) {
                        Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                        txnManager.rollback(status); 
                        msg=""+ex.getMessage();
                        throw ServiceException.FAILURE(ex.getMessage(), ex);
                }
            issuccess = resObj.getBoolean("success");
            msg = messageSource.getMessage("acc.field.lmsproductsaresyncedsuccessfully", null, RequestContextUtils.getLocale(request));
            jobj.put("success", true);
            jobj.put("msg", msg);
            txnManager.commit(status);
                }               
            }           
            }while(iterate);
         
        } catch (Exception ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
//            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
//            HibernateUtil.closeSession(session);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    private HashMap<String, Object> setUOMforProduct(HashMap<String, Object> productMap,String uomId,String uomName,String uomType,String companyid) throws ServiceException,SessionExpiredException{
        if(!StringUtil.isNullOrEmpty(uomId) && !StringUtil.isNullOrEmpty(uomName)){
            
            HashMap<String,Object> uomMap = new HashMap<String, Object>();
            uomMap.put("uomname", uomName);            
            uomMap.put("companyid", companyid);
            
            //Check UOM with same name exist in Accounting System
            KwlReturnObject kwlReturnObject=accUOMObj.getUnitOfMeasure(uomMap);
            if(kwlReturnObject.getEntityList().isEmpty()){
                uomMap.put("uomtype", uomName);
                uomMap.put("precision",0);
                uomMap.put("inventoryReferId", uomId);
                KwlReturnObject returnObject=accUOMObj.addUoM(uomMap);                
                UnitOfMeasure measure=(UnitOfMeasure)returnObject.getEntityList().get(0);
                productMap.put(uomType,measure.getID());
            }else{
                UnitOfMeasure unitOfMeasure=(UnitOfMeasure)kwlReturnObject.getEntityList().get(0);
                HashMap<String,Object> uomUpdateMap = new HashMap<String, Object>();
                uomUpdateMap.put("uomid", unitOfMeasure.getID());            
                uomUpdateMap.put("inventoryReferId", uomId);
                uomUpdateMap.put("companyid", companyid);
                accUOMObj.updateUoM(uomUpdateMap);
                productMap.put(uomType,unitOfMeasure.getID());
            }
        }
        return  productMap;
    }
   
    private HashMap<String, Object> getAssemblyRequestParams(HttpServletRequest request) throws ServiceException, SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
        requestParams.put("assembly", request.getParameter("assembly"));
        requestParams.put("applydate", request.getParameter("applydate"));
        requestParams.put("quantity", Double.parseDouble(request.getParameter("quantity")==null?"0":request.getParameter("quantity").toString()));
        return requestParams;
    }
   
    private JSONArray insertfield(JSONArray customColumnArray, String companyId)  throws SessionExpiredException,JSONException, ServiceException {

        for (int count = 0; count < customColumnArray.length(); count++) {
           
                JSONObject customColumnObject = customColumnArray.getJSONObject(count);

                JSONObject resultJson = new JSONObject();
                KwlReturnObject kmsg = null, fresult = null;
                FieldParams fp = null;
                String leadFieldId = "";
                String moduleFieldId = "";
                String modulename = "";



                int lineitem = (!customColumnObject.isNull("iscustomcolumn") && customColumnObject.optBoolean("iscustomcolumn")) ? 1 : 0;

                String columnExistInModules = Constants.stringInitVal;
                String companyid = companyId;
                ArrayList moduleArr = new ArrayList();
                boolean createField = true;
                boolean updateField = false;
//                    if (request.getParameter("columncreationinvoice") != null) {
                moduleArr.add(Constants.Acc_Product_Master_ModuleId);
//        //                    moduleArr.add(Constants.Acc_BillingInvoice_ModuleId);
//                    }
//                         if (request.getParameter("columncreationveninvoice") != null) {
//                    moduleArr.add(Constants.Acc_Vendor_Invoice_ModuleId);
//    //                    moduleArr.add(Constants.Acc_Vendor_BillingInvoice_ModuleId);
//                }
//                if (request.getParameter("columncreationdebitnote") != null) {
//                    moduleArr.add(Constants.Acc_Debit_Note_ModuleId);
//    //                    moduleArr.add(Constants.Acc_Vendor_BillingInvoice_ModuleId);
//                }
//                if (request.getParameter("columncreationcreditnote") != null) {
//                    moduleArr.add(Constants.Acc_Credit_Note_ModuleId);
//    //                    moduleArr.add(Constants.Acc_Vendor_BillingInvoice_ModuleId);
//                }
//                if (request.getParameter("columncreationmakepayment") != null) {
//                    moduleArr.add(Constants.Acc_Make_Payment_ModuleId);
//    //                    moduleArr.add(Constants.Acc_Vendor_BillingInvoice_ModuleId);
//                }
//                if (request.getParameter("columncreationreceivepayment") != null) {
//                    moduleArr.add(Constants.Acc_Receive_Payment_ModuleId);
//    //                    moduleArr.add(Constants.Acc_Vendor_BillingInvoice_ModuleId);
//                }
//                if (request.getParameter("columncreationgeneraletry") != null) {
//                     moduleArr.add(Constants.Acc_GENERAL_LEDGER_ModuleId);
//    //                    moduleArr.add(Constants.Acc_BillingInvoice_ModuleId);
//                }
////                if (request.getParameter("columncreationcontact") != null) {
////                    moduleArr.add(Constants.Crm_contact_moduleid);
////                }
////                if (request.getParameter("columncreationproduct") != null) {
////                    moduleArr.add(Constants.Crm_product_moduleid);
////                }
////                if (request.getParameter("columncreationopportunity") != null) {
////                    moduleArr.add(Constants.Crm_opportunity_moduleid);
////                }
////                if (request.getParameter("columncreationcase") != null) {
////                    moduleArr.add(Constants.Crm_case_moduleid);
////                }

                for (int cnt = 0; cnt < moduleArr.size(); cnt++) {//Duplicate Name check
                    Integer moduleid = Integer.parseInt(moduleArr.get(cnt).toString());
                    String moduleName = getModuleName(moduleid);
                    String fieldlabel = (!customColumnObject.isNull("fieldlabel")) ? customColumnObject.optString("fieldlabel", "") : "";

                    HashMap<String, Object> dupCheckRequestParams = new HashMap<String, Object>();
                    dupCheckRequestParams.put(Constants.filter_names, Arrays.asList(Constants.fieldlabel, Constants.companyid, Constants.moduleid, Constants.customcolumn));
                    dupCheckRequestParams.put(Constants.filter_values, Arrays.asList(fieldlabel, companyid, moduleid, lineitem));

                    KwlReturnObject resultDupCheck = accAccountDAOobj.getFieldParams(dupCheckRequestParams);

                    if (resultDupCheck.getEntityList().size() > 0) {
                        createField = false;
                        columnExistInModules += moduleName + ", ";
                    }
                }
                
                KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
                
                KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(FieldParams.class.getName(), customColumnObject.optString("fieldid"));
                FieldParams fieldParams = (FieldParams)custumObjresult.getEntityList().get(0);
                if(createField&&fieldParams!=null&&preferences.isInventoryAccountingIntegration()){
                    createField = false;
                    updateField = true;
                }

                if (createField) {
                    HashMap<Integer, HashMap<String, Object>> modulerequestParams = new HashMap<Integer, HashMap<String, Object>>();
                    HashMap<String, Object> requestParams = null;
                    ArrayList<String> ll = new ArrayList<String>();
                    int count1 = 0;
                    for (int cnt = 0; cnt < moduleArr.size(); cnt++) {//Create new field
                        count1++;
                        Integer moduleid = Integer.parseInt(moduleArr.get(cnt).toString());
                        requestParams = processrequest(moduleArr.get(cnt).toString(),companyid,customColumnObject);
                        modulerequestParams.put(moduleid, requestParams);
                    }
                    if (moduleArr.size() > 0) {
                        /*
                         *Batch update existing records with default value in thread
                         */
                        accAccountService.UpdateExistingRecordsWithDefaultValue(modulerequestParams, moduleArr, companyid);
                        ll.add(requestParams.get("response").toString());
                    } 
                }else if(updateField){
                    updateField(companyid,customColumnObject);
                }
           
        }
        return customColumnArray;
    }
    
    private boolean updateField(String companyId,JSONObject customColumnObject){
        boolean success = false;
        try {
            String fieldlabel = (!customColumnObject.isNull("fieldlabel")) ? customColumnObject.optString("fieldlabel", "") : "";
            String fieldid = (!customColumnObject.isNull("fieldid")) ? customColumnObject.optString("fieldid", "") : "";
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("fieldId", fieldid);
            requestParams.put("Fieldlabel", fieldlabel);
            requestParams.put("companyId", companyId);
            success = accAccountDAOobj.updateField(requestParams);
        } catch (ServiceException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return success;
    }
    
   
    private HashMap<String, Object> processrequest(String moduelId,String companyId,JSONObject customColumnObject) throws SessionExpiredException,JSONException, ServiceException {
        HashMap<String, Object> requestParams;
        Integer moduleid = Integer.parseInt(moduelId);
        String companyid = companyId;
        Integer fieldtype = (!customColumnObject.isNull("fieldtype")) ? customColumnObject.optInt("fieldtype",0) :0;
        int moduleflag = 0;
        if (!StringUtil.isNullOrEmpty((!customColumnObject.isNull("moduleflag")?customColumnObject.optString("moduleflag"):""))) {
            moduleflag = customColumnObject.optInt("moduleflag",0);
        }
        HashMap<String, Object> colParams = null;
        HashMap<String, Object> RefcolParams = null;
        String maxlength = "";
        if (fieldtype == 7) {// multiselect
            RefcolParams = getcolumn_number(companyid, moduleid, fieldtype, moduleflag);
            if (!Boolean.parseBoolean((String) RefcolParams.get("success"))) {
                colParams = RefcolParams;
            } else {
                //  colnumber accessed as per normal field
                colParams = getcolumn_number(companyid, moduleid, 1, moduleflag);
            }
            maxlength = "1000";
        } else {
            colParams = getcolumn_number(companyid, moduleid, fieldtype, moduleflag);
            if (fieldtype == 2) { // number fields
                maxlength = "15";
            } else if (fieldtype == 3) { // date
                maxlength = "50";
            } else if (fieldtype == 4) { // dropdown
                maxlength = "50";
            } else if (fieldtype == 5) { // timefield
                maxlength = "25";
            } else if (fieldtype == 8) { // reference dropdown
                maxlength = "255";
            } else if (fieldtype == 9) { // auto number
                maxlength = "150";
            } else if (fieldtype == 13) { // Text Area
                maxlength = "1000";
            }else if (fieldtype == 15) { // Rich Text Area
                maxlength = "2000";
            }
        }
        if (Boolean.parseBoolean((String) colParams.get("success"))) {

            requestParams = new HashMap<String, Object>();
           
            String fieldlabel = (!customColumnObject.isNull("fieldlabel")) ? customColumnObject.optString("fieldlabel", "") : "";
            String fieldtooltip = (!customColumnObject.isNull("fieldtooltip")) ? customColumnObject.optString("fieldtooltip", "") : "";
            String fieldid = (!customColumnObject.isNull("fieldid")) ? customColumnObject.optString("fieldid", "") : "";
//            String formulae = request.getParameter("rules");
            String formulae = "";
            String editable = (!customColumnObject.isNull("iseditable")) ? customColumnObject.optString("iseditable", "") : "";

            Integer fieldmaxlen = 12;
            if (!StringUtil.isNullOrEmpty((!customColumnObject.isNull("maxlength")?customColumnObject.optString("maxlength"):""))) {
                maxlength = customColumnObject.optString("maxlength","");
            }
            if (!StringUtil.isNullOrEmpty(maxlength)) {
                fieldmaxlen = Integer.parseInt(maxlength);
            }

            Integer validationtype = 0;

            int sendNotification = (!customColumnObject.isNull("sendnotification")) ? customColumnObject.optInt("sendnotification",0) :0;
            String notificationDays = (!customColumnObject.isNull("notificationdays")) ? customColumnObject.optString("notificationdays", "") : "";
            String isessential = (!customColumnObject.isNull("isessential")) ? (customColumnObject.optInt("isessential")==1)?"true":"false" : "false";
//            String customregex = request.getParameter("customregex");
             String customregex = "";
            int isCustomField = (!customColumnObject.isNull("iscustomfield") && customColumnObject.optBoolean("iscustomfield")) ? 1 : 0;// if false then it is dimension field
            int lineitem = (!customColumnObject.isNull("iscustomcolumn") && customColumnObject.optBoolean("iscustomcolumn")) ? 1 : 0;
//            String combodata = request.getParameter("combodata");
             String combodata = (!customColumnObject.isNull("combodata")) ? customColumnObject.optString("combodata", "") : "";
             String combodataIds = (!customColumnObject.isNull("combodataIds")) ? customColumnObject.optString("combodataIds", "") : "";

            int essential = 0;
            boolean allowmapping = false;
            if (StringUtil.isNullOrEmpty(formulae)) {
                if (moduleflag == 1 && fieldtype == 8) {
                    allowmapping = false;
                } else if (fieldtype == 9) {
                    allowmapping = false;
                } else {
                    allowmapping = true;
                }
                if ((!com.krawler.common.util.StringUtil.isNullOrEmpty(isessential) && isessential.equals("false")) || fieldtype == 9) {// if field is auto no then no need to mark as mandatory
                    essential = 0;
                } else if (!com.krawler.common.util.StringUtil.isNullOrEmpty(isessential)) {
                    essential = 1;
                }
            }
            requestParams.put("Maxlength", fieldmaxlen);
            requestParams.put("Isessential", essential);
            requestParams.put("sendNotification", sendNotification);
            requestParams.put("notificationDays", notificationDays);
            requestParams.put("Fieldtype", fieldtype);
           
//            requestParams.put("isforproject", (request.getParameter("isforproject") !=null)? 1 : 0);
            requestParams.put("isforproject",  0);
            requestParams.put("Validationtype", validationtype);
            requestParams.put("Customregex", customregex);
            requestParams.put("Fieldname", Constants.Custom_Record_Prefix + fieldlabel);

            requestParams.put("Fieldlabel", fieldlabel);
            KwlReturnObject cap = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) cap.getEntityList().get(0);
            if(preferences.isInventoryAccountingIntegration()){// in case of acc-inv integration only.
                requestParams.put("fieldIdInCaseAccInvIntegration", fieldid);
            }
            requestParams.put("Companyid", companyid);
            requestParams.put("Moduleid", moduleid);
            requestParams.put("Customfield", isCustomField);
            requestParams.put("Customcolumn", lineitem);

            requestParams.put("Iseditable", editable);
            String RefModule = null;
            String RefDataColumn = null;
            String RefFetchColumn = null;
            String comboid = "";
//            if (fieldtype == 8) {//Reference Module
//                comboid = request.getParameter("comboid");
//                requestParams.put("Comboname", request.getParameter("comboname"));
//                requestParams.put("Comboid", comboid);
//                requestParams.put("Moduleflag", Integer.parseInt(request.getParameter("moduleflag")));
//
//                if (request.getParameter("moduleflag").equals("0")) {
//                    RefModule = "DefaultMasterItem";
//                    RefDataColumn = "value";
//                    RefFetchColumn = "id";
//                } else if (request.getParameter("comboname").equals("Account")) {
//                    RefModule = "CrmAccount";
//                    RefDataColumn = "accountname";
//                    RefFetchColumn = "accountid";
//                } else if (request.getParameter("comboname").equals("Product")) {
//                    RefModule = "CrmProduct";
//                    RefDataColumn = "productname";
//                    RefFetchColumn = "productid";
//                } else if (request.getParameter("comboname").equals("Contact")) {
//                    RefModule = "CrmContact";
//                    RefDataColumn = "lastname";
//                    RefFetchColumn = "contactid";
//                } else if (request.getParameter("comboname").equals("Case")) {
//                    RefModule = "CrmCase";
//                    RefDataColumn = "subject";
//                    RefFetchColumn = "caseid";
//                } else if (request.getParameter("comboname").equals("Opportunity")) {
//                    RefModule = "CrmOpportunity";
//                    RefDataColumn = "oppname";
//                    RefFetchColumn = "oppid";
//                } else if (request.getParameter("comboname").equals("Lead")) {
//                    RefModule = "CrmLead";
//                    RefDataColumn = "lastname";
//                    RefFetchColumn = "leadid";
//                } else if (request.getParameter("comboname").equals("Users")) {
//                    RefModule = "User";
//                    RefDataColumn = "lastName";
//                    RefFetchColumn = "userID";
//                }
//            } else {
            requestParams.put("Comboname", "");
            requestParams.put("Comboid", comboid);
            requestParams.put("Moduleflag", 0);
//            }

            if (fieldtype == 9) {//auto number
//                requestParams.put("Startingnumber", Integer.parseInt(request.getParameter("startingnumber")));
//                requestParams.put("Prefix", request.getParameter("prefix"));
//                requestParams.put("Suffix", request.getParameter("suffix"));
            }
            requestParams.put("Colnum", colParams.get("column_number"));
            String Refcolumn_number = "0";
            String refcolumnname = null;
            if (fieldtype == 7 && RefcolParams != null) {
                requestParams.put("Refcolnum", RefcolParams.get("column_number"));
                refcolumnname = Constants.Custom_column_Prefix + RefcolParams.get("column_number");
                Refcolumn_number = RefcolParams.get("column_number").toString();
            }


            JSONObject resultJson = new JSONObject();
            KwlReturnObject kmsg = null;
            FieldParams fp = null;
            kmsg = accAccountDAOobj.insertfield(requestParams);
            resultJson.put("success", kmsg.isSuccessFlag());
            requestParams.put("success", kmsg.isSuccessFlag() ? 1 : 0);
            if (kmsg.isSuccessFlag()) {
                fp = (FieldParams) kmsg.getEntityList().get(0);
                resultJson.put("ID", fp.getId());
                resultJson.put("msg", kmsg.getMsg());
//                String defaultvalue = request.getParameter("defaultval");
                String defaultvalue = (!customColumnObject.isNull("defaultval")) ? customColumnObject.optString("defaultval", "") : "";
                if(preferences.isInventoryAccountingIntegration()){  // only in case of accounting inventory integration
                    insertfieldcombodataInCsaeInvSync(combodata, combodataIds, fp.getId(), defaultvalue);
                }else{
                    defaultvalue = insertfieldcombodata(combodata, fp.getId(), defaultvalue, new HashMap<String, HashMap<String,Object>> ());
                }
                
                requestParams.put("defaultvalue", defaultvalue);
                String colname = Constants.Custom_column_Prefix + colParams.get("column_number");
                String column_name = Constants.Custom_Column_Prefix + requestParams.get("Colnum");
              //  resultJson.put("headerInfo", createDefaultHeadrEntry(allowmapping, Refcolumn_number, column_name, essential, companyid, comboid, RefModule, RefDataColumn, RefFetchColumn, fieldtype, moduleid, fieldmaxlen, fieldlabel, fp.getId(), editable));
//                if (!StringUtil.isNullOrEmpty(formulae)) {       //this used for to set formula
//                    setCustomColumnFormulae(request,true);
//                }

            } else {
                resultJson.put("msg", "Error Processing request");
            }
            requestParams.put("response", resultJson);


        } else {
            return colParams;
        }
        return requestParams;
    } 
    public ModelAndView saveTermsForTax(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            TaxTermsMapping intTerm = saveTermsForTax(request);
            issuccess = true;
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
    
    public TaxTermsMapping saveTermsForTax(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        TaxTermsMapping invTerm = null;
        try {
            boolean isSalesOrPurchase = StringUtil.isNullOrEmpty(request.getParameter("isSalesOrPurchase")) ? false : Boolean.parseBoolean(request.getParameter("isSalesOrPurchase"));
            String term = request.getParameter("term");
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String taxId = request.getParameter("taxId");
            JSONArray taxArray = new JSONArray(taxId);
            JSONArray termArray = new JSONArray(term);
            List<String> currentlyUsedTerms = new ArrayList<String>();
            String taxids = "";
            // flag to check tax is used in any transaction or not.
            for (int i = 0; i < taxArray.length(); i++) {
                taxids += "'" + taxArray.getString(i) + "',";
                taxids = taxids.substring(0, taxids.length() - 1);
                
                KwlReturnObject taxResult = null;
                boolean isTaxUsed = false;            // flag to check tax is used in any transaction or not.
                if (isSalesOrPurchase) {
                    //check use in CS/SI
                    taxResult = accAccountDAOobj.findTaxUsedInCSSI(taxids);
                    if (taxResult.getRecordTotalCount() > 0) {
                        isTaxUsed = true;
            }
                    //check use in Customer Quotation
                    taxResult = accAccountDAOobj.findTaxUsedInCQ(taxids);
                    if (taxResult.getRecordTotalCount() > 0) {
                        isTaxUsed = true;
                    }
                //check use in CS/SI
                    taxResult = accAccountDAOobj.findTaxUsedInSO(taxids);
                    if (taxResult.getRecordTotalCount() > 0) {
                        isTaxUsed = true;
                    }
                } else {
                    //check use in CP/PI
                    taxResult = accAccountDAOobj.findTaxUsedInCPPI(taxids);
                    if (taxResult.getRecordTotalCount() > 0) {
                        isTaxUsed = true;
                    }
                    //check use in Vendor Quotation
                    taxResult = accAccountDAOobj.findTaxUsedInVQ(taxids);
                    if (taxResult.getRecordTotalCount() > 0) {
                        isTaxUsed = true;
                    }
                    //check use in CS/SI
                    taxResult = accAccountDAOobj.findTaxUsedInPO(taxids);
                    if (taxResult.getRecordTotalCount() > 0) {
                        isTaxUsed = true;
                    }
                }

                if (isTaxUsed) {                     // check tax if it is used in any transaction, then go ahead
                    //Old selected sales term value
                    List<String> selectedArraylist = new ArrayList<String>();
                    List list = accCusVenMapDAOObj.getTerms(taxArray.getString(i));
                    Iterator itr = list.iterator();
                    while (itr.hasNext()) {
                        InvoiceTermsSales invoiceTermsSales = (InvoiceTermsSales) kwlCommonTablesDAOObj.getClassObject(InvoiceTermsSales.class.getName(), itr.next().toString());
                        if (invoiceTermsSales != null) {
                            selectedArraylist.add(invoiceTermsSales.getId());
                        }
                    }

                    //To Check Term is used or not for previously selected terms
                    for (int j = 0; j < selectedArraylist.size(); j++) {
                        String termid = selectedArraylist.get(j);
                        boolean isTermUsed = false;         // flag to check term is used in any transaction or not.
                        // check term is used in formula or not
                        KwlReturnObject result = accAccountDAOobj.findTermUsedInFormula(termid);
                if (result.getRecordTotalCount() > 0) {
                            isTermUsed = true;
                }
                        if (isSalesOrPurchase) {
                            // check term is used in any Sales Invoices
                            result = accAccountDAOobj.findTermUsedInTransaction(termid);
                if (result.getRecordTotalCount() > 0) {
                                isTermUsed = true;
                }
                            // check term is used in any Customer Quotation
                            result = accAccountDAOobj.findTermUsedInQuotation(termid);
                if (result.getRecordTotalCount() > 0) {
                                isTermUsed = true;
                }
                            // check term is used in any SO
                            result = accAccountDAOobj.findTermUsedInSO(termid);
                            if (result.getRecordTotalCount() > 0) {
                                isTermUsed = true;
                            }
            } else {
                            // check term is used in any Purchase Invoices
                            result = accAccountDAOobj.findTermUsedInPI(termid);
                if (result.getRecordTotalCount() > 0) {
                                isTermUsed = true;
                }
                            // check term is used in any Vendor Quotation
                            result = accAccountDAOobj.findTermUsedInVQ(termid);
                if (result.getRecordTotalCount() > 0) {
                                isTermUsed = true;
                }
                            // check term is used in any SO
                            result = accAccountDAOobj.findTermUsedInPO(termid);
                if (result.getRecordTotalCount() > 0) {
                                isTermUsed = true;
                            }
                        }
                        // check term is used in any DO
                        result = accAccountDAOobj.findTermUsedInDO(termid);
                        if (result.getRecordTotalCount() > 0) {
                            isTermUsed = true;
                        }
                        // check term is used in any GRO
                        result = accAccountDAOobj.findTermUsedInGRO(termid);
                        if (result.getRecordTotalCount() > 0) {
                            isTermUsed = true;
                        }

                        if (isTermUsed) {
                            currentlyUsedTerms.add(termid);  // check term if it is used in any transaction, pushed into new array.
                        }
                    }
                }
            }

            //New selected sales term value
            List<String> termArraylist = new ArrayList<String>();
            for (int j = 0; j < termArray.length(); j++) {
                termArraylist.add(termArray.getString(j));
            }

            for (int p = 0; p < currentlyUsedTerms.size(); p++) {
                if (!termArraylist.contains(currentlyUsedTerms.get(p))) {
                    throw new AccountingException(messageSource.getMessage("acc.field.SelectedTaxisusedinTransaction.Socannotbeupdated", null, RequestContextUtils.getLocale(request)));
                }
            }
           
            ExtraCompanyPreferences cmpPref = (ExtraCompanyPreferences) kwlCommonTablesDAOObj.getClassObject(ExtraCompanyPreferences.class.getName(), companyid);
            if (cmpPref.isActivateCRMIntegration() && isSalesOrPurchase) {
                try {
                    //Session session = null;
                    JSONObject resObj = new JSONObject();
//                    String crmURL = this.getServletContext().getInitParameter("crmURL");
                    JSONObject userData = new JSONObject();
                    userData.put("iscommit", true);
                    userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
                    userData.put("userid", sessionHandlerImpl.getUserid(request));
                    userData.put("companyid", companyid);
                    userData.put("termarray", termArray);
                    userData.put("taxarray", taxArray);
                    //session = HibernateUtil.getCurrentSession();
//                    String action = "224";
                    String crmURL = URLUtil.buildRestURL(Constants.crmURL);
                    crmURL = crmURL + "master/taxterm";
                    resObj = apiCallHandlerService.restPostMethod(crmURL, userData.toString());
//                    resObj = apiCallHandlerService.callApp(crmURL, userData, companyid, action);
                    if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                        saveTermForTaxCommonCode(request, taxArray, termArray, companyid);
                    } else {
                        String msg = resObj.has("msg") ? resObj.getString("msg") : "Error Occurred at server side.";
                        throw new AccountingException(msg);
                    }
                } catch (JSONException e) {
                    Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, e);
                }
            } else {
                saveTermForTaxCommonCode(request, taxArray, termArray, companyid);
            }
        } catch (JSONException ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        } 
            return invTerm;
    }
    
    public void saveTermForTaxCommonCode(HttpServletRequest request, JSONArray taxArray, JSONArray termArray, String companyid) throws JSONException, ServiceException, SessionExpiredException {
        for (int i = 0; i < taxArray.length(); i++) {
            String tax = taxArray.getString(i);
            List list = accCusVenMapDAOObj.deleteTermForTax(tax);
            for (int j = 0; j < termArray.length(); j++) {
                HashMap<String, Object> hm = new HashMap();
                hm.put("companyid", companyid);
                hm.put("userId", sessionHandlerImpl.getUserid(request));
                String s = termArray.getString(j);
                String s1 = taxArray.getString(i);
                hm.put("term", s);
                hm.put("tax", s1);
                if (!StringUtil.isNullOrEmpty(s)) {
                    KwlReturnObject kwlreturn = accCusVenMapDAOObj.saveTermForTax(hm);
                    List<TaxTermsMapping> li = kwlreturn.getEntityList();
                }

            }
        }
    }
    
    public ModelAndView getTax(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "",start=null,limit=null;
        try {
            start=request.getParameter(Constants.start);
            limit=request.getParameter(Constants.limit);                    
            JSONArray DataJArr = accAccountControllerCMNServiceObj.getTaxJson(request);
            
            JSONArray pagedJson = DataJArr; //ERP-13650 [SJ]               
                if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                    pagedJson = StringUtil.getPagedJSON(DataJArr, Integer.parseInt(start), Integer.parseInt(limit));
                }
            jobj.put(DATA, pagedJson);
            jobj.put(COUNT, DataJArr.length());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ServiceException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }   
    
    public ModelAndView getProductCustomFieldsToShow(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess= true;
        String msg="";
        try {
            JSONArray dataArray = new JSONArray();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String customFieldQuery = "select customcolumninfo from showcustomcolumninreport where moduleid = ? and companyid = ?";
            List<String> customFieldinfoList = null;
            customFieldinfoList = accCommonTablesDAO.executeSQLQuery(customFieldQuery, new Object[]{Constants.Acc_Product_Master_ModuleId,companyid});
            HashMap<String, JSONArray> productCustomData = new HashMap<String, JSONArray>();
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = null;
            
            if (customFieldinfoList.size() > 0) {
                String jsonString = customFieldinfoList.get(0);
                JSONArray productCustomFields = new JSONArray(jsonString);
                String fieldIds = "";
                for(int jCnt=0; jCnt<productCustomFields.length();jCnt++) {
                   fieldIds = fieldIds.concat("'").concat(productCustomFields.getJSONObject(jCnt).getString("fieldid")).concat("',");
                }
                if(!StringUtil.isNullOrEmpty(fieldIds)) {
                    fieldIds = fieldIds.substring(0,fieldIds.length()-1);
                }
                HashMap<String, Object> fieldrequestParams = new HashMap();
                fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, "INid"));
                fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Product_Master_ModuleId, fieldIds));
                FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            }
            
            if(replaceFieldMap.size()>0) {
                for (Map.Entry<String, String> varEntry : replaceFieldMap.entrySet()) {
                    JSONObject fieldInfo = new JSONObject();
                    fieldInfo.put("dataindex", varEntry.getKey());
                    fieldInfo.put("columnname", varEntry.getKey().replaceAll("Custom_", ""));
                    dataArray.put(fieldInfo);
                }
            }
            jobj.put("data", dataArray);
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView getTaxesFromAccountId(HttpServletRequest request, HttpServletResponse response) throws ServiceException{
        JSONObject jobj = new JSONObject();
        boolean issuccess= true;
        String msg="";
        try{            
            String companyId = sessionHandlerImpl.getCompanyid(request);
            String accountId = StringUtil.isNullOrEmpty(request.getParameter("accountId").toString())?"":request.getParameter("accountId");
            /*
             * The params send through hashmap taxFromAccountParams to add new params and make function reusable.
             */            
            HashMap<String, Object> taxFromAccountParams = new HashMap();
            taxFromAccountParams.put("companyid",companyId);
            taxFromAccountParams.put("accountid",accountId);            
            
            /*
             *ERP-40242 : Show only activated taxes in create and copy case and all taxes in edit cases
             */
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.includeDeactivatedTax))) {
                taxFromAccountParams.put(Constants.includeDeactivatedTax, Boolean.parseBoolean(request.getParameter(Constants.includeDeactivatedTax)));
            }

            KwlReturnObject taxResult = accAccountDAOobj.getTaxesFromAccountId(taxFromAccountParams);
            List list = taxResult.getEntityList();
            int count = list.size();
            JSONArray dataArray = getTaxJson(request,list);
            jobj.put(DATA, dataArray);
            jobj.put("totalCount", count);
            issuccess = true;
        } catch(Exception e){
            msg = e.getMessage();
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("saveInvoiceTerm : " + e.getMessage(), e);
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }    
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONArray getTaxJson(HttpServletRequest request, List<Object[]> list) throws SessionExpiredException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            if (list != null && !list.isEmpty()) {
                for (Object[] row : list) {                    
                    KwlReturnObject taxResult = accountingHandlerDAOobj.getObject(Tax.class.getName(), row[0].toString());
                    Tax tax = (Tax) taxResult.getEntityList().get(0);
                    JSONObject obj = new JSONObject();
                    obj.put(TAXID, tax.getID());
                    obj.put(TAXNAME, tax.getName());
                    obj.put(TAXCODE, tax.getTaxCode());
                    obj.put(ACCOUNTID,tax.getAccount().getID());
                    obj.put(ACCOUNTNAME, tax.getAccount().getName());
                    obj.put(TAXTYPEID,tax.getTaxtype());
                    obj.put(COMPANYID, tax.getCompany().getCompanyID());
                    obj.put("taxTypeName", tax.getTaxtype() == 2 ? "Sales" : "Purchase");
                    obj.put(Constants.HAS_ACCESS, tax.isActivated());
                    jArr.put(obj);
                }          
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getTaxJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
//       public static String getTableName(int moduleid) {
//        String module = "";
//        switch (moduleid) {
//            case 1:
//                module = Constants.Crm_account_pojo;
//                break;
//            case 2:
//                module = Constants.Crm_lead_pojo;
//                break;
//            case 3:
//                module = Constants.Crm_case_pojo;
//                break;
//            case 4:
//                module = Constants.Crm_product_pojo;
//                break;
//            case 5:
//                module = Constants.Crm_opportunity_pojo;
//                break;
//            case 6:
//                module = Constants.Crm_contact_pojo;
//                break;
//        }
//        return module;
//    }
//    public static String getmoduledataTableName(int moduleid) {
//        String module = "";
//        switch (moduleid) {
//            case 1:
//                module = Constants.Crm_account_custom_data_pojo;
//                break;
//            case 2:
//                module = Constants.Crm_lead_custom_data_pojo;
//                break;
//            case 3:
//                module = Constants.Crm_case_custom_data_pojo;
//                break;
//            case 4:
//                module = Constants.Crm_product_custom_data_pojo;
//                break;
//            case 5:
//                module = Constants.Crm_opportunity_custom_data_pojo;
//                break;
//            case 6:
//                module = Constants.Crm_contact_custom_data_pojo;
//                break;
//        }
//        return module;
//    }
//    public static String getPrimarycolumn(int moduleid) {
//        String module = "";
//        switch (moduleid) {
//            case 1:
//                module = Constants.Crm_accountid;
//                break;
//            case 2:
//                module = Constants.Crm_leadid;
//                break;
//            case 3:
//                module = Constants.Crm_caseid;
//                break;
//            case 4:
//                module = Constants.Crm_productid;
//                break;
//            case 5:
//                module = Constants.Crm_opportunityid;
//                break;
//            case 6:
//                module = Constants.Crm_contactid;
//                break;
//        }
//        return module;
//    }
//    public static String getModuleCustomTableName(int moduleid) {
//        String module = "";
//        switch (moduleid) {
//            case 1:
//                module = Constants.CRM_CUSTOM_ACCOUNT_TABLE;
//                break;
//            case 2:
//                module = Constants.CRM_CUSTOM_LEAD_TABLE;
//                break;
//            case 3:
//                module = Constants.CRM_CUSTOM_CASE_TABLE;
//                break;
//            case 4:
//                module = Constants.CRM_CUSTOM_PRODUCT_TABLE;
//                break;
//            case 5:
//                module = Constants.CRM_CUSTOM_OPPORTUNITY_TABLE;
//                break;
//            case 6:
//                module = Constants.CRM_CUSTOM_CONTACT_TABLE;
//                break;
//        }
//        return module;
//    }
//    public static String getmoduledataRefName(int moduleid) {
//        String module = "";
//        switch (moduleid) {
//            case 1:
//                module = Constants.Crm_account_pojo_ref;
//                break;
//            case 2:
//                module = Constants.Crm_lead_pojo_ref;
//                break;
//            case 3:
//                module = Constants.Crm_case_pojo_ref;
//                break;
//            case 4:
//                module = Constants.Crm_product_pojo_ref;
//                break;
//            case 5:
//                module = Constants.Crm_opportunity_pojo_ref;
//                break;
//            case 6:
//                module = Constants.Crm_contact_pojo_ref;
//                break;
//        }
//        return module;
//    }
//    public static String getDefaultValue(int xtype) {
//        String module = "";
//        switch (xtype) {
//            case 1:
//                module = Constants.TextField_default;
//                break;
//            case 2:
//                module = Constants.NumberField_default;
//                break;
//            case 3:
//                module = new Date().toString();
//                break;
//            case 5:
//                module = Constants.TimeField_default;
//                break;
//        }
//        return module;
//    } 
   
    public ModelAndView getAllQAApproveRejectPermittedUserList(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
//        Session session = null;
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
//            session = HibernateUtil.getCurrentSession();

//            ArrayList params = new ArrayList();
//            String SELECT_USER_INFO = " from User u where u.company.companyID = ?  and u.deleteflag=?";
//            params.add(companyId);
//            params.add(0);
//            List list = HibernateUtil.executeQuery(session, SELECT_USER_INFO, params.toArray());
            List list = accCommonTablesDAO.getActiveUsersOfCompany(companyId);
            int count = list.size();

            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            
            while (itr.hasNext()) {

                User user = (User) itr.next();
                KwlReturnObject kmsg = null;
                JSONObject jobj2 = new JSONObject();

                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("userid", user.getUserID());
                requestParams.put("roleid", user.getRoleID());
                kmsg = permissionHandlerDAOObj.getUserPermission(requestParams);
                jobj2 = getRolePermissionJson(kmsg.getEntityList(),kmsg.getRecordTotalCount());
                int uperm = 0;
                int perm = 2; // for qaview it is 2^0=1 ,qaapprovereject it is 2^1=2
                int arrLength = jobj2.getJSONArray("data").length();
                for (int i = 0; i < arrLength; i++) {
                    JSONObject res = jobj2.getJSONArray("data").getJSONObject(i);
                    String featureid = res.getString("featureid");
                    if (featureid.equals("98c6d3ec64fb4dbb97dfa269e599ffb6")) {  // featureid for qa 
                        uperm = res.getInt("permission");
                        break;
                    }

                }
                if ((perm & uperm) == perm) {
                    UserLogin ul = user.getUserLogin();
                    JSONObject obj = new JSONObject();

                    obj.put("userid", user.getUserID());
                    obj.put("username", ul.getUserName());
                    obj.put("fname", user.getFirstName());
                    obj.put("lname", user.getLastName());
                    obj.put("image", user.getImage());
                    obj.put("emailid", user.getEmailID());
                    obj.put("lastlogin", (ul.getLastActivityDate() == null ? "" : authHandler.getDateFormatter(request).format(ul.getLastActivityDate())));
                    obj.put("aboutuser", user.getAboutUser());
                    obj.put("address", user.getAddress());
                    obj.put("contactno", user.getContactNumber());
                    obj.put("formatid", (user.getDateFormat() == null ? "" : user.getDateFormat().getFormatID()));
                    obj.put("tzid", (user.getTimeZone() == null ? "" : user.getTimeZone().getTimeZoneID()));

                    jArr.put(obj);

                }
                jobj.put("data", jArr);
                jobj.put("count", count);

                issuccess = true;
            }  
        }catch (Exception e) {
            msg = e.getMessage();
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("getAllQAApproveRejectPermittedUserList : " + e.getMessage(), e);
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
//                if(session != null){
//                    session.close();
//                }
            } catch (JSONException ex) {
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public ModelAndView getAllConsignmentRequestApproveRejectPermittedUserList(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
//        Session session = null;
        try {
            String companyId = sessionHandlerImpl.getCompanyid(request);
//            session = HibernateUtil.getCurrentSession();
//
//            ArrayList params = new ArrayList();
//            String SELECT_USER_INFO = " from User u where u.company.companyID = ?  and u.deleteflag=?";
//            params.add(companyId);
//            params.add(0);

//            List list = HibernateUtil.executeQuery(session, SELECT_USER_INFO, params.toArray());
            List list = accCommonTablesDAO.getActiveUsersOfCompany(companyId);
            int count = list.size();

            Iterator itr = list.iterator();
            JSONArray jArr = new JSONArray();
            
            while (itr.hasNext()) {

                User user = (User) itr.next();
                KwlReturnObject kmsg = null;
                JSONObject jobj2 = new JSONObject();

                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("userid", user.getUserID());
                requestParams.put("roleid", user.getRoleID());
                kmsg = permissionHandlerDAOObj.getUserPermission(requestParams);
                jobj2 = getRolePermissionJson(kmsg.getEntityList(),kmsg.getRecordTotalCount());
                int uperm = 0;
                int perm = 2; // for crview it is 2^0=1 ,crapprovereject it is 2^1=2
                int arrLength = jobj2.getJSONArray("data").length();
                for (int i = 0; i < arrLength; i++) {
                    JSONObject res = jobj2.getJSONArray("data").getJSONObject(i);
                    String featureid = res.getString("featureid");
                    if (featureid.equals("25178b7fbdc144c3ad53fc476f7f8eb8")) {  // featureid for Stock Request Approval 
                        uperm = res.getInt("permission");
                        break;
                    }

                }
                if ((perm & uperm) == perm) {
                    UserLogin ul = user.getUserLogin();
                    JSONObject obj = new JSONObject();

                    obj.put("userid", user.getUserID());
                    obj.put("username", ul.getUserName());
                    obj.put("fname", user.getFirstName());
                    obj.put("lname", user.getLastName());
                    obj.put("image", user.getImage());
                    obj.put("emailid", user.getEmailID());
                    obj.put("lastlogin", (ul.getLastActivityDate() == null ? "" : authHandler.getDateFormatter(request).format(ul.getLastActivityDate())));
                    obj.put("aboutuser", user.getAboutUser());
                    obj.put("address", user.getAddress());
                    obj.put("contactno", user.getContactNumber());
                    obj.put("formatid", (user.getDateFormat() == null ? "" : user.getDateFormat().getFormatID()));
                    obj.put("tzid", (user.getTimeZone() == null ? "" : user.getTimeZone().getTimeZoneID()));

                    jArr.put(obj);

                }
                jobj.put("data", jArr);
                jobj.put("count", count);

                issuccess = true;
            }  
        }catch (Exception e) {
            msg = e.getMessage();
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("getAllConsignmentRequestApproveRejectPermittedUserList : " + e.getMessage(), e);
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
//                if(session != null){
//                    session.close();
//                }
            } catch (JSONException ex) {
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public JSONObject getRolePermissionJson(List ll, int totalSize) {
        JSONArray jarr = new JSONArray();
        JSONObject jobj = new JSONObject();
        ArrayList featurelist = new ArrayList();

        try {
            Iterator ite = ll.iterator();
            while (ite.hasNext()) {
                Object[] row = (Object[]) ite.next();
                if (!featurelist.contains(row[2])) {
                    featurelist.add(row[2]);

                    JSONObject obj = new JSONObject();
                    obj.put("permission", row[1]);
                    obj.put("featureid", row[2]);

                    jarr.put(obj);
                }
            }
            jobj.put("data", jarr);
            jobj.put("count", totalSize);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return jobj;
    }
    
    public ModelAndView saveMasterItemDataSequence(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String sequenceData="";
            String groupid="";
            String companyid = sessionHandlerImpl.getCompanyid(request);
            if (!StringUtil.isNullOrEmpty(request.getParameter("sequenceData"))) {
                sequenceData = request.getParameter("sequenceData");
                groupid = !StringUtil.isNullOrEmpty(request.getParameter("groupid")) ? request.getParameter("groupid") : "";
                JSONArray sequenceDataArray = new JSONArray(sequenceData);
                saveMasterItemDataSequence(sequenceDataArray, groupid, companyid);
                issuccess =true;
                msg = messageSource.getMessage("acc.masterConfig.sequenceSavedSuccessfully", null, RequestContextUtils.getLocale(request));
            }
            txnManager.commit(status);
        }catch (Exception e) {
            txnManager.rollback(status);
            msg = e.getMessage();
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, e);
            throw ServiceException.FAILURE("saveMasterItemDataSequence : " + e.getMessage(), e);
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
     /**
     * Description : Below Method is used to get Default company setup 
     * @param <request> used to get default company setup parameters
     * @param <response> used to send respose
     * @return :JSONObject
     */
     public ModelAndView getDefaultCompanySetUpData(HttpServletRequest request, HttpServletResponse response) {
        JSONObject finalJsonObj = new JSONObject();
        boolean issuccess = true;
        Iterator itr=null;
        List resultList=null;
        String msg = "";
        try {
            String companyType ="";
            String countryid = "";
            String stateid = "";
            
            HashMap<String, Object> requestParams=generateDefaultSetUpCompanyMap(request);
            
            if (requestParams.containsKey("companyType") && requestParams.get("companyType") != null) {
                companyType = (String) requestParams.get("companyType");
            }
            if (requestParams.containsKey("countryid") && requestParams.get("countryid") != null) {
                countryid = (String) requestParams.get("countryid");
            }
            if (requestParams.containsKey("stateid") && requestParams.get("stateid") != null) {
                stateid = (String) requestParams.get("stateid");
            }
          
            /* Get Default Account  */
            boolean isAdminSubdomain = false;
            String[] nature = null;
            KwlReturnObject result = accAccountDAOobj.getDefaultAccount(companyType, countryid,stateid,isAdminSubdomain,nature);
            resultList = result.getEntityList();
            getDefaultAccountJson(resultList,finalJsonObj);
            
            
            /* Get Default Account Groups  */
            result=null;
            resultList=null;
            result = accAccountDAOobj.getGroups(requestParams);
            resultList = result.getEntityList();
            getDefaultAccountGroupJson(requestParams,resultList,finalJsonObj);
            
            /* Get Default Currency with Exchage rate details */
            result=null;
            resultList=null;
            result = accCurrencyDAOobj.getDefaultCurrencyExchange(requestParams);
            resultList = result.getEntityList();
            getDefaultCurrencyExchangeJson(requestParams, resultList,finalJsonObj);
            
            /* Get Default GST Tax */
            /*
             * Added countryid of United States(244) for loading Default taxes
             */
            if (countryid.equalsIgnoreCase("137") || countryid.equalsIgnoreCase("106") || countryid.equalsIgnoreCase("203")|| countryid.equalsIgnoreCase("244")) {
                result = accTaxObj.getDefaultGSTList(requestParams);
                resultList = result.getEntityList();
                getDefaultGSTTaxJson(requestParams,resultList,finalJsonObj);
            }
            
        }catch (Exception ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            issuccess = false;
            msg = "" + ex.getMessage();
        } finally {
            try {
                finalJsonObj.put("success", issuccess);
                finalJsonObj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", finalJsonObj.toString());
    }
     
     /**
     * Description : Below Method is used to generate Default SetUp Company Map 
     * @param <request> used to get default company setup parameters
     * @return :HashMap
     */
     public HashMap<String, Object> generateDefaultSetUpCompanyMap(HttpServletRequest request) throws SessionExpiredException ,ServiceException{
        /*Variable declaration*/
        HashMap<String, Object> defCompSetupMap = new HashMap<String, Object>();
        String companyid = "", currencyid = "";
        try {
             /*Set values to Variable */
            companyid = sessionHandlerImpl.getCompanyid(request);
            currencyid = sessionHandlerImpl.getCurrencyID(request);
            /* defCompSetupMap Hash map is used to set common parameters for default company setup */
            defCompSetupMap.put(Constants.companyid, companyid);
            defCompSetupMap.put(Constants.currencyKey, currencyid);
            defCompSetupMap.put(Constants.globalCurrencyKey, getGlobalCurrencyidFromRequest(request));
            defCompSetupMap.put(Constants.df, authHandler.getDateFormatter(request));  //This format belongs to our global date format[i.e.new SimpleDateFormat("MMMM d, yyyy hh:mm:ss aa")]
            defCompSetupMap.put(Constants.userdf, authHandler.getUserDateFormatter(request));
            defCompSetupMap.put("dateonlyformatter", authHandler.getDateOnlyFormatter(request));
            defCompSetupMap.put("defaultgroup", "true");
            defCompSetupMap.put("companyType", "defaultaccount");
            defCompSetupMap.put("countryid", request.getParameter("countryid"));
            defCompSetupMap.put("financialYrStartDate", request.getParameter("financialYrStartDate"));
            

        }catch (Exception ex) {
            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
        }

        return defCompSetupMap;

    }
     /**
     * Description : Below Method is used to get DefaultAccount Json 
     * @param <resList> used to get default company setup parameters
     * @param <finalJsonObj> used to put data in final JSON 
     * @return :void
     */
     public void getDefaultAccountJson(List resList,JSONObject finalJsonObj)throws ServiceException{

        JSONObject jobj = new JSONObject();
        Iterator itr = resList.iterator();
        JSONArray jArr = new JSONArray();
        try {
            while (itr.hasNext()) {
                DefaultAccount dAccount = (DefaultAccount) itr.next();
                JSONObject obj = new JSONObject();
                obj.put("id", dAccount.getID());
                obj.put("name", dAccount.getName());
                obj.put("groupname", dAccount.getGroup().getName());
                obj.put("companytype", dAccount.getCompanyType());
                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("totalCount", jArr.length());
            finalJsonObj.put("defaultaccount", jobj);

        } catch (Exception ex) {
            throw ServiceException.FAILURE("getDefaultAccountJson : " + ex.getMessage(), ex);
        }

    }
     /**
     * Description : Below Method is used to get DefaultAccount Group Json 
     * @param <requestParams> used to get default company setup parameters
     * @param <resList> used to get default account group
     * @param <finalJsonObj> used to put data in final JSON 
     * @return :void
     */
     public void getDefaultAccountGroupJson(HashMap<String, Object> requestParams,List resList,JSONObject finalJsonObj)throws  ServiceException{
        JSONObject jobj = new JSONObject();
        Iterator itr = resList.iterator();
        JSONArray jArr = new JSONArray();
        String companyid="";
        if(requestParams.containsKey("companyid")){
            companyid=(String)requestParams.get("companyid");
        }
        try {
              while (itr.hasNext()) {
               Object[] row = (Object[]) itr.next();
                Group group = (Group) row[0];
                JSONObject obj = new JSONObject();
                obj.put("groupid", group.getID());
                obj.put("groupname", group.getName());
                obj.put("mastergroupid", group.getID());
                obj.put("nature", group.getNature());
                obj.put("naturename", (group.getNature() == Constants.Liability) ? "Liability" : (group.getNature() == Constants.Asset) ? "Asset" : (group.getNature() == Constants.Expences) ? "Expences" : (group.getNature() == Constants.Income) ? "Income" : "");
                  if (group.isAffectGrossProfit()) {
                      obj.put("affectgp", "Yes");
                  } else {
                      obj.put("affectgp", "No");
                  }
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
                jArr.put(obj);
              }
            jobj.put("data", jArr);
            jobj.put("totalCount", jArr.length());
           finalJsonObj.put("defaultaccountgroup", jobj);

        } catch (Exception ex) {
            throw ServiceException.FAILURE("getDefaultAccountGroupJson : " + ex.getMessage(), ex);
        }
    }
     /**
     * Description : Below Method is used to get Default CurrencyExchange Json 
     * @param <requestParams> used to get default company setup parameters
     * @param <list> used to get Default Currency Exchange
     * @param <finalJsonObj> used to put data in final JSON 
     * @return :void
     */
     public void getDefaultCurrencyExchangeJson(HashMap<String, Object> requestParams, List<Object[]> list, JSONObject finalJsonObj) throws SessionExpiredException, ServiceException {
        JSONArray jArr = new JSONArray();
        JSONObject jobj = new JSONObject();
        DateFormat formatter = null;
        String financialYrStartDate = "";
        Date financialStartDate = null;
        if (requestParams.containsKey(Constants.df) && requestParams.get(Constants.df) != null) {
            formatter = (DateFormat) requestParams.get(Constants.df);
        }
        if (requestParams.containsKey("financialYrStartDate") && requestParams.get("financialYrStartDate") != null) {
            financialYrStartDate = (String) requestParams.get("financialYrStartDate");
        }

        try {
            if (!StringUtil.isNullOrEmpty(financialYrStartDate)) {
                financialStartDate = formatter.parse(financialYrStartDate);
            }
            if (list != null && !list.isEmpty()) {
                for (Object[] row : list) {
                    DefaultExchangeRate er = (DefaultExchangeRate) row[0];
                    DefaultExchangeRateDetails erd = (DefaultExchangeRateDetails) row[1];
                    JSONObject obj = new JSONObject();
                    obj.put(ID, er.getID());
                    if (er.getFromCurrency().getCurrencyID().equals(er.getToCurrency().getCurrencyID())) {
                        obj.put(APPLYDATE, formatter.format(new Date(1, 1, 1)));
                        obj.put(TODATE, formatter.format(new Date(1, 1, 31)));
                    } else {
                        obj.put(APPLYDATE, formatter.format(financialStartDate));
                        obj.put(TODATE, formatter.format(financialStartDate));
                    }
                    obj.put(EXCHANGERATE, erd.getExchangeRate());
                    obj.put(FOREIGNTOBASEEXCHANGERATE, erd.getForeignToBaseExchangeRate());
                    obj.put(NEWEXCHANGERATE, erd.getExchangeRate());
                    obj.put(FROMCURRENCY, er.getFromCurrency().getName());
                    obj.put(SYMBOL, er.getToCurrency().getSymbol());
                    obj.put(HTMLCODE, er.getToCurrency().getHtmlcode());
                    obj.put(TOCURRENCY, er.getToCurrency().getName());
                    obj.put(TOCURRENCYID, er.getToCurrency().getCurrencyID());
                    obj.put(FROMCURRENCYID, er.getFromCurrency().getCurrencyID());
                    jArr.put(obj);
                }
                jobj.put("data", jArr);
                jobj.put("totalCount", jArr.length());
                finalJsonObj.put("defaultexchangerate", jobj);

            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getDefaultCurrencyExchangeJson : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("getDefaultCurrencyExchangeJson : " + ex.getMessage(), ex);
        }

    }
     /**
     * Description : Below Method is used to get Default GSTTax Json 
     * @param <requestParams> used to get default company setup parameters
     * @param <list> used to get default account group
     * @param <finalJsonObj> used to put data in final JSON 
     * @return :void
     */
    public void  getDefaultGSTTaxJson(HashMap<String, Object> requestParams, List<Object[]> list,JSONObject finalJsonObj ) throws SessionExpiredException, ServiceException {
        JSONArray jArr = new JSONArray();
        JSONObject jobj = new JSONObject();
        String companyId="";
        String countryid="";
        String financialYrStartDate="";
        DateFormat formatter = null;
        if (requestParams.containsKey(Constants.companyid) && requestParams.get(Constants.companyid) != null) {
                companyId = (String) requestParams.get(Constants.companyid);
            }
        if (requestParams.containsKey("countryid") && requestParams.get("countryid") != null) {
                countryid = (String) requestParams.get("countryid");
            }
        if (requestParams.containsKey("financialYrStartDate") && requestParams.get("financialYrStartDate") != null) {
                financialYrStartDate = (String) requestParams.get("financialYrStartDate");
            }
        if (requestParams.containsKey(Constants.df) && requestParams.get(Constants.df) != null) {
                formatter = (DateFormat) requestParams.get(Constants.df);
            }
        try {
            if (list != null && !list.isEmpty()) {
                
               
                String gstOutputAccountId = "";
                String gstInputAccountId = "";
                String gstOutputAccountName = "";
                String gstInputAccountName = "";
                
                Date financialStartDate = null;
                
                boolean isMalasianCountry = false;
                if (countryid != null && countryid.equals("137")) {
                    isMalasianCountry = true;
                }
                 if (!StringUtil.isNullOrEmpty(financialYrStartDate)) {
                        financialStartDate = formatter.parse(financialYrStartDate);
                    }
                if (isMalasianCountry) {
//                    if (!StringUtil.isNullOrEmpty(financialYrStartDate)) {
//                        financialStartDate = formatter.parse(financialYrStartDate);
//                    }
                    KwlReturnObject accountReturnObject = accAccountDAOobj.getAccountFromName(companyId, Constants.MALAYSIAN_GST_OUTPUT_TAX);
                    List accountResultList = accountReturnObject.getEntityList();
                    if(!accountResultList.isEmpty()){
                        gstOutputAccountId =  ((Account)accountResultList.get(0)).getID();
                        gstOutputAccountName =  ((Account)accountResultList.get(0)).getName();
                    }
                    
                    accountReturnObject = accAccountDAOobj.getAccountFromName(companyId, Constants.MALAYSIAN_GST_INPUT_TAX);
                    accountResultList = accountReturnObject.getEntityList();
                    if(!accountResultList.isEmpty()){
                        gstInputAccountId = ((Account)accountResultList.get(0)).getID();;
                        gstInputAccountName = ((Account)accountResultList.get(0)).getName();;
                    }
                }
                
                for (Object[] row : list) {
                    JSONObject obj = new JSONObject();
                    obj.put(TAXNAME, row[0]);
                    obj.put(TAXDESCRIPTION, row[1]);
                    obj.put(PERCENT, row[3]);
                    obj.put(TAXCODE, row[2]);
                    obj.put(TAXTYPE, row[5]);
                    obj.put(APPLYDATE, formatter.format(financialStartDate));
                    obj.put(MASTERTYPEVALUE, Group.ACCOUNTTYPE_GST);
                    if(isMalasianCountry){
                        boolean isPurchase=false;
                        int taxType=2; //For sales tax type
                        obj.put(GST_ACCOUNT_ID, gstOutputAccountId);
                            obj.put(GST_ACCOUNT_NAME, gstOutputAccountName);
                        String taxName = row[0].toString();
                        /*
                         * TX-E43 renamed as TX-IES
                         * TX-N43 renamed as TX-ES
                         * Added new purchase tax RP,TX-FRS,TX-NC & NP
                         */
                        if(StringUtil.isMalaysianPurchaseTax(taxName)){
                            taxType=1; //For Purchase tax type
                            obj.put(GST_ACCOUNT_ID, gstInputAccountId);
                            obj.put(GST_ACCOUNT_NAME, gstInputAccountName);
                        }
                    }else{
                        obj.put(GST_ACCOUNT_ID, "");
                        obj.put(GST_ACCOUNT_NAME, "");
                    }
                    jArr.put(obj);
                }
            jobj.put("data", jArr);
            jobj.put("totalCount", jArr.length());
            finalJsonObj.put("defaultgsttax", jobj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getTaxJson : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("getTaxJson : " + ex.getMessage(), ex);
        }
    } 
    
    public List saveMasterItemDataSequence(JSONArray jsonArray, String groupid, String companyid) {
        JSONObject jobj = new JSONObject();
        List resultList = new ArrayList();
        try{
            resultList=accAccountDAOobj.saveMasterItemDataSequence(jsonArray, groupid, companyid);
        } catch(Exception ex){
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return resultList;
    }
    public ModelAndView saveTermsAsTaxForIndia(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("Account_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            
            saveTermsAsTaxForIndia(request);
            issuccess = true;
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
    public void saveTermsAsTaxForIndia(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException, JSONException, UnsupportedEncodingException {
        LineLevelTerms invTerm = null;
        boolean isDuplicateTerm = false;
        try {
            JSONArray termArr = null;
            List<HashMap<String, Object>> storeDate = new ArrayList<HashMap<String, Object>>();
            String companyid = sessionHandlerImpl.getCompanyid(request), creator = sessionHandlerImpl.getUserid(request), isSalesOrPurchase = "", term = "" ,action="";
            KwlReturnObject result01 = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences preferences = (ExtraCompanyPreferences) result01.getEntityList().get(0);
            if (!StringUtil.isNullOrEmpty(request.getParameter("data"))) {
                    action="updated";
                    termArr = new JSONArray(StringUtil.DecodeText(request.getParameter("data")));
                    for (int i = 0; i < termArr.length(); i++) {
                        JSONObject termData = termArr.getJSONObject(i);
                        HashMap<String, Object> termMap = new HashMap();
                        termMap.put("companyid", companyid);
                        termMap.put("creationdate", new Date());
                        if (termData.has("termid") && !StringUtil.isNullOrEmpty(termData.getString("termid"))) {
                            termMap.put("id", termData.getString("termid"));
                        }
                        if (termData.has("term") && !StringUtil.isNullOrEmpty(termData.getString("term"))) {
                            String termName = StringUtil.DecodeText(termData.optString("term"));
                            termMap.put("term", termName);
                            term += termName + (termArr.length() == (i - 1) ? "" : ",");
                        }
                        if (termData.has("isSalesOrPurchase") && !StringUtil.isNullOrEmpty(termData.getString("isSalesOrPurchase"))) {
                            termMap.put("salesOrPurchaseFlag", termData.getString("isSalesOrPurchase"));
                            isSalesOrPurchase = termData.getString("isSalesOrPurchase");
                        }
                        if (termData.has("accountid") && !StringUtil.isNullOrEmpty(termData.getString("accountid"))) {
                            termMap.put("accountid", termData.getString("accountid"));
                        }else if (termData.has("glaccount") && !StringUtil.isNullOrEmpty(termData.getString("glaccount"))) {
                            termMap.put("accountid", termData.getString("glaccount"));
                        }
                        if (termData.has("payableaccountid") && !StringUtil.isNullOrEmpty(termData.getString("payableaccountid"))) {
                            termMap.put("payableaccountid", termData.getString("payableaccountid"));
                        }
                        if (!StringUtil.isNullOrEmpty(termData.optString("creditnotavailedaccount"))) {
                            termMap.put("creditnotavailedaccount", termData.optString("creditnotavailedaccount"));
                        }
                        if (termData.has("masteritem") && !StringUtil.isNullOrEmpty(termData.getString("masteritem"))) {
                            termMap.put("masteritem", termData.getString("masteritem"));
                        }
                        if (termData.has("formula") && !StringUtil.isNullOrEmpty(termData.getString("formula"))) {
                            termMap.put("formula", termData.getString("formulaids"));
                        }
                        if (termData.has("category") && !StringUtil.isNullOrEmpty(termData.getString("category"))) {
                            termMap.put("category", Integer.parseInt(termData.getString("category")));
                        }
                        if (termData.has("includegst") && !StringUtil.isNullOrEmpty(termData.getString("includegst"))) {
                            termMap.put("includegst", Integer.parseInt(termData.getString("includegst")));
                        }
                        if (termData.has("proft") && !StringUtil.isNullOrEmpty(termData.getString("proft"))) {
                            termMap.put("proft", Integer.parseInt(termData.getString("proft")));
                        }
                        if (termData.has("sign") && !StringUtil.isNullOrEmpty(termData.getString("sign"))) {
                            termMap.put("sign", Integer.parseInt(termData.getString("sign")));
                        }
                        if (termData.has("suppressamount") && !StringUtil.isNullOrEmpty(termData.getString("suppressamount"))) {
                            termMap.put("suppressamount", Integer.parseInt(termData.getString("suppressamount")));
                        }
                        if (termData.has("formulaids") && !StringUtil.isNullOrEmpty(termData.getString("formulaids"))) {
                            termMap.put("formulaids", termData.getString("formulaids"));
                        }
                        if (termData.has("percent") && !StringUtil.isNullOrEmpty(termData.getString("percent"))) {
                            termMap.put("percent", Double.parseDouble(termData.getString("percent")));
                        }
                        if (termData.has("termtype") && !StringUtil.isNullOrEmpty(termData.getString("termtype"))) {
                            termMap.put("termtype", Integer.parseInt(termData.getString("termtype")));
                            termMap.put("termsequence", getTermSequece(Integer.parseInt(termData.getString("termtype"))));
                        }
                        if (termData.has("isDefault") && !StringUtil.isNullOrEmpty(termData.getString("isDefault"))) {
                            termMap.put("isDefault", termData.getString("isDefault"));
                        }
                        if (termData.has("taxtype") && !StringUtil.isNullOrEmpty(termData.getString("taxtype"))) {
                            termMap.put("taxtype", Integer.parseInt(termData.getString("taxtype")));
                            if (termData.has("taxvalue") && !StringUtil.isNullOrEmpty(termData.getString("taxvalue"))) {
                                if (Integer.parseInt(termData.getString("taxtype")) == 0) { // If Flat
                                    termMap.put("termamount", Double.parseDouble(termData.getString("taxvalue")));
                                } else {
                                    termMap.put("percent", Double.parseDouble(termData.getString("taxvalue")));
                                }
                            }
                        }
                        if (termData.has("purchasevalueorsalevalue") && !StringUtil.isNullOrEmpty(termData.getString("purchasevalueorsalevalue"))) {
                            termMap.put("purchasevalueorsalevalue", Double.parseDouble(termData.getString("purchasevalueorsalevalue")));
                        }
                        if (termData.has("deductionorabatementpercent") && !StringUtil.isNullOrEmpty(termData.getString("deductionorabatementpercent"))) {
                            termMap.put("deductionorabatementpercent", Double.parseDouble(termData.getString("deductionorabatementpercent")));
                        }
                        if (termData.has("purchasevalueorsalevalue") && !StringUtil.isNullOrEmpty(termData.getString("purchasevalueorsalevalue"))) {
                            termMap.put("purchasevalueorsalevalue", Double.parseDouble(termData.getString("purchasevalueorsalevalue")));
                        }
                        if (termData.has("deductionorabatementpercent") && !StringUtil.isNullOrEmpty(termData.getString("deductionorabatementpercent"))) {
                            termMap.put("deductionorabatementpercent", Double.parseDouble(termData.getString("deductionorabatementpercent")));
                        }
                        if (termData.has("isadditionaltax") && !StringUtil.isNullOrEmpty(termData.getString("isadditionaltax"))) {
                            termMap.put("isAdditionalTax", Boolean.parseBoolean((String) termData.get("isadditionaltax")));
                        }
                        if (termData.has("includeInTDSCalculation") && !StringUtil.isNullOrEmpty(termData.getString("includeInTDSCalculation"))) {
                            termMap.put("includeInTDSCalculation", Boolean.parseBoolean((String) termData.get("includeInTDSCalculation")));
                        }
                        storeDate.add(termMap);
                    }
                if (term.length() > 1) {
                    term = term.substring(0, term.length() - 1);
                }
            } else {
                action="added";
                HashMap<String, Object> termMap = new HashMap();
                termMap.put("companyid", companyid);
                termMap.put("creationdate", new Date());
                if (!StringUtil.isNullOrEmpty(request.getParameter("id"))) {
                    termMap.put("id", request.getParameter("id"));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("term"))) {
                    termMap.put("term", request.getParameter("term"));
                    term = request.getParameter("term");
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("isSalesOrPurchase"))) {
                    termMap.put("salesOrPurchaseFlag", request.getParameter("isSalesOrPurchase"));
                    isSalesOrPurchase = request.getParameter("isSalesOrPurchase");
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("accountid"))) {
                    termMap.put("accountid", request.getParameter("accountid"));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("payableaccountid"))) {
                    termMap.put("payableaccountid", request.getParameter("payableaccountid"));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("creditnotavailedaccount"))) {
                    termMap.put("creditnotavailedaccount", request.getParameter("creditnotavailedaccount"));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("masteritem"))) {
                    termMap.put("masteritem", request.getParameter("masteritem"));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("formula"))) {
                    termMap.put("formula", request.getParameter("formula"));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("category"))) {
                    termMap.put("category", Integer.parseInt(request.getParameter("category")));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("includegst"))) {
                    termMap.put("includegst", Integer.parseInt(request.getParameter("includegst")));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("proft"))) {
                    termMap.put("proft", Integer.parseInt(request.getParameter("proft")));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("sign"))) {
                    termMap.put("sign", Integer.parseInt(request.getParameter("sign")));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("suppressamount"))) {
                    termMap.put("suppressamount", Integer.parseInt(request.getParameter("suppressamount")));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("formulaids"))) {
                    termMap.put("formulaids", request.getParameter("formulaids"));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("percent"))) {
                    termMap.put("percent", Double.parseDouble(request.getParameter("percent")));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("termtype"))) {
                    termMap.put("termtype", Integer.parseInt(request.getParameter("termtype")));
                    termMap.put("termsequence", getTermSequece(Integer.parseInt(request.getParameter("termtype"))));
                    if (Integer.parseInt(request.getParameter("termtype")) == 3 && !StringUtil.isNullOrEmpty(request.getParameter("formType"))) {
                       termMap.put("formType", request.getParameter("formType"));
                    }else{
                       termMap.put("formType", "1");// Default without form
                    }
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("isDefault"))) {
                    termMap.put("isDefault", "true");
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("IsOtherTermTaxable"))) {
                    termMap.put("IsOtherTermTaxable", request.getParameter("IsOtherTermTaxable"));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("isAdditionalTax"))) {
                    termMap.put("isAdditionalTax",request.getParameter("isAdditionalTax").equals("on"));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("includeInTDSCalculation"))) {
                    termMap.put("includeInTDSCalculation",request.getParameter("includeInTDSCalculation").equals("on"));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("taxtype"))) {
                    termMap.put("taxtype", Integer.parseInt(request.getParameter("taxtype")));
                    if (!StringUtil.isNullOrEmpty(request.getParameter("taxvalue"))) {
                        if (Integer.parseInt(request.getParameter("taxtype")) == 0) { // If Flat
                            termMap.put("termamount", Double.parseDouble(request.getParameter("taxvalue")));
                        } else {
                            termMap.put("percent", Double.parseDouble(request.getParameter("taxvalue")));
                        }
                    }
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("purchasevalueorsalevalue"))) {
                    termMap.put("purchasevalueorsalevalue", Double.parseDouble(request.getParameter("purchasevalueorsalevalue")));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("deductionorabatementpercent"))) {
                    termMap.put("deductionorabatementpercent", Double.parseDouble(request.getParameter("deductionorabatementpercent")));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("purchasevalueorsalevalue"))) {
                    termMap.put("purchasevalueorsalevalue", Double.parseDouble(request.getParameter("purchasevalueorsalevalue")));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("deductionorabatementpercent"))) {
                    termMap.put("deductionorabatementpercent", Double.parseDouble(request.getParameter("deductionorabatementpercent")));
                }
                if (!StringUtil.isNullOrEmpty(creator)) {
                    termMap.put("creator", creator);
                }
                storeDate.add(termMap);
            }

            for (HashMap<String, Object> termMap : storeDate) {
                if (StringUtil.isNullOrEmpty(request.getParameter("data"))) {
                    isDuplicateTerm = accAccountDAOobj.isDuplicateLineLevelTerm(termMap); //To check duplicate sales term in Master Configuration Sales Term Window.
                    if (isDuplicateTerm) {
                        String msg=messageSource.getMessage("acc.master.invoice.OutputTerm", null, RequestContextUtils.getLocale(request));
                        if(!StringUtil.isNullOrEmpty(isSalesOrPurchase)){
                            boolean isSales=Boolean.parseBoolean(isSalesOrPurchase);
                            if(!isSales){
                                msg=messageSource.getMessage("acc.master.invoice.InputTerm", null, RequestContextUtils.getLocale(request));
                            }
                        }
                        throw new AccountingException(msg + " '<b>" + term + "</b>" + messageSource.getMessage("acc.field.alreadyexists.", null, RequestContextUtils.getLocale(request))); //Sales Term 'X' already exist
                    }
                }
                KwlReturnObject kwlreturn = accAccountDAOobj.saveIndianTermsCompanyLevel(termMap);
                if (termArr!=null && termMap!=null && termMap.size() > 0 && !StringUtil.isNullOrEmpty(request.getParameter("data"))) { // Update Product term only for account and fourmula and percentage/Flat
                    accAccountDAOobj.updateIndianTermsProductLevel(termMap.get("id").toString(),termMap);
                }
                if (StringUtil.isNullOrEmpty(request.getParameter("data"))) {
                    List<LineLevelTerms> li = kwlreturn.getEntityList();
                    LineLevelTerms invTermSave = li.get(0);
                    //termMap.put("id", invTermSave.getId());
                    String productsList = request.getParameter("productList");
                    //boolean isApplyOnExistingProduct = StringUtil.isNullOrEmpty(request.getParameter("isApplyOnExistingProduct")) ? false : Boolean.parseBoolean(request.getParameter("isApplyOnExistingProduct"));
                    boolean isApplyOnAllExistingProduct = false;
                    if (!StringUtil.isNullOrEmpty(request.getParameter("isApplyOnAllExistingProduct")) && StringUtil.isNullOrEmpty(productsList)) {
                        isApplyOnAllExistingProduct = Boolean.parseBoolean(request.getParameter("isApplyOnAllExistingProduct"));
                    }
                    ArrayList<String> ListProduct = new ArrayList<String>();
                    if (!StringUtil.isNullOrEmpty(productsList)) {
                        JSONArray termsArr = new JSONArray(productsList);
                        for (int cnt = 0; cnt < termsArr.length(); cnt++) {
                            JSONObject temp = termsArr.getJSONObject(cnt);
                            if (temp.has("productid")) {
                                ListProduct.add(temp.getString("productid"));
                            }
                        }
                    }
                    HashMap<String, Object> requestParams = productHandler.getProductRequestMap(request);
                    requestParams.put(Constants.PRODUCT_SEARCH_FLAG,preferences.getProductSearchingFlag());
                    KwlReturnObject result = accProductObj.getProductsForCombo(requestParams);
                    List list = result.getEntityList();
                    Iterator itr = list.iterator();
                    User userObj = null;
                    if (!StringUtil.isNullOrEmpty(creator)) {
                        KwlReturnObject prodresult = accProductObj.getObject(User.class.getName(), creator);
                        userObj = (User) prodresult.getEntityList().get(0);
                    }
                    while (itr.hasNext()) {
                        Product product = (Product) itr.next();
                        if (isApplyOnAllExistingProduct) {
                            termMap.put("isDefault", "true");
                        } else {
                            if (productsList.contains(product.getID())) {
                                termMap.put("isDefault", "true");
                            } else {
                                termMap.put("isDefault", "false");
                            }
                        }
                        termMap.put("product", product);
                        termMap.put("createdOn", "");
                        Account accountObj = null;
                        String termAccount = termMap.get("accountid").toString();
                        if (!StringUtil.isNullOrEmpty(termAccount)) {
                            KwlReturnObject prodresult = accProductObj.getObject(Account.class.getName(), termAccount);
                            accountObj = (Account) prodresult.getEntityList().get(0);
                        }
                        termMap.put("account", accountObj);
                        termMap.put("creator", userObj);
                        if (!StringUtil.isNullOrEmpty(request.getParameter("taxvalue"))) {
                            termMap.put("percentage", Double.parseDouble(request.getParameter("taxvalue")));
                        }
                        String termid = invTermSave.getId();
                        LineLevelTerms termObj = null;
                        if (!StringUtil.isNullOrEmpty(termid)) {
                            KwlReturnObject prodresult = accProductObj.getObject(LineLevelTerms.class.getName(), termid);
                            termObj = (LineLevelTerms) prodresult.getEntityList().get(0);
                        }
                        termMap.put("term", termObj);
                        accProductObj.saveProductTermsMap(termMap);
                    }
                }
            }
            action+= " purchase term";
            String auditaction = AuditAction.PURCHASE_TERM_ADDED;
            if (isSalesOrPurchase.equals("true")) {
                action+= " sales term";
                auditaction = AuditAction.SALES_TERM_ADDED;
            }
            if (!StringUtil.isNullOrEmpty(term)) {
                auditTrailObj.insertAuditLog(auditaction, "User " + sessionHandlerImpl.getUserFullName(request) + " has " + action + " " + term, request, companyid);
            }
        } catch (ServiceException ex) {
//            Logger.getLogger(accAccountController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveInvoiceTerm : " + ex.getMessage(), ex);
        }
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
    /**
     * 
     * @param request
     * @param response
     * @return  = It Return Custom Field JSON for Report for Particular Report using report Id
     */
    public ModelAndView getAgedCustomFieldsToShow(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            JSONArray dataArray = new JSONArray();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            int reportId = 0;
            if (!StringUtil.isNullOrEmpty(request.getParameter("reportId"))) {
                reportId = Integer.parseInt(request.getParameter("reportId"));
            }
            HashMap hashMap = new HashMap();
            hashMap.put("companyId", companyid);
            hashMap.put("reportId", reportId);
            KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(hashMap);
            List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
            List arrayList = new ArrayList();
            for (CustomizeReportMapping customizeReportMapping : customizeReportList) {
                String column = "Custom_" + customizeReportMapping.getDataIndex();
                if (!arrayList.contains(customizeReportMapping.getDataIndex())) {
                    JSONObject fieldInfo = new JSONObject();
                    fieldInfo.put("dataindex", "Custom_" + customizeReportMapping.getDataIndex());
                    fieldInfo.put("columnname", customizeReportMapping.getDataIndex());
                    dataArray.put(fieldInfo);
                    arrayList.add(customizeReportMapping.getDataIndex());
                }
            }
            jobj.put("data", dataArray);
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
             
    //TODO : Distributed Opening Balance for COA is obtained from getCustomCombodata() method.
    //       Need to get above thing from separate Ajax by using below method.
    /*
    public ModelAndView getDistributedOpeningBalance(HttpServletRequest request, HttpServletResponse response) {
        KwlReturnObject result = null;
        JSONObject jresult = new JSONObject();
        String accountid = "";
        String fieldid = "";
        String jsonview = "jsonView";
        HashMap<String, Object> requestParams = new HashMap<String, Object>();
        try {
            if(!StringUtil.isNullOrEmpty(request.getParameter("accountid"))){
                accountid =request.getParameter("accountid");
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("fieldid"))){
                fieldid =request.getParameter("fieldid");
            }
            requestParams.put("accountid", accountid);
            requestParams.put("fieldid", fieldid);
            result = accAccountDAOobj.getDistributedOpeningBalance(requestParams);
            List lst = result.getEntityList();
            Iterator ite = lst.iterator();
            while (ite.hasNext()) {
                Object[] row = (Object[]) ite.next();
                JSONObject jobjTemp = new JSONObject();
                jobjTemp.put("comboid", row[0]);
                jobjTemp.put("openingbal", row[1]);
                jobjTemp.put("field", row[2]);
                
                jresult.append(Constants.data, jobjTemp);
            }
            jresult.put(Constants.RES_success, result.isSuccessFlag());
        } catch (JSONException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(jsonview, Constants.model, jresult.toString());
    }
    */

    public ModelAndView getDimensionsForCombo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = getDimensionsForComboJSON(paramJobj);
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONObject getDimensionsForComboJSON(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            HashMap<String, Object> requestParams = new HashMap<>();
            requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.customfield,Constants.moduleid));
            requestParams.put(Constants.filter_values, Arrays.asList(paramJobj.getString(Constants.companyKey), 0,paramJobj.optInt(Constants.moduleid)));
//            requestParams.put(Constants.moduleid, paramJobj.optInt(Constants.moduleid));
            requestParams.put("isActivated", 1); // For Activated Field Params
            KwlReturnObject result = accAccountDAOobj.getFieldParams(requestParams);
            List<FieldParams> fieldParamsList = result.getEntityList();
            
            JSONArray dataArr = new JSONArray();
            for (FieldParams fieldParams : fieldParamsList) {
                JSONObject fieldParamJobj = new JSONObject();
                fieldParamJobj.put("id", fieldParams.getId());
                fieldParamJobj.put("name", fieldParams.getFieldlabel());
                
                dataArr.put(fieldParamJobj);
            }
            
            jobj.put("data", dataArr);
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return jobj;
    }
    
    public ModelAndView getDimensionValuesForCombo(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            JSONObject paramJobj = StringUtil.convertRequestToJsonObject(request);
            jobj = getDimensionValuesForComboJSON(paramJobj);
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public JSONObject getDimensionValuesForComboJSON(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = true;
        String msg = "";
        try {
            HashMap<String, Object> filterRequestParams = new HashMap<>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("field.id");
            filter_params.add(paramJobj.getString("groupid"));
            order_by.add("value");
            order_type.add("asc");
            filterRequestParams.put("filter_names", filter_names);
            filterRequestParams.put("filter_params", filter_params);
            filterRequestParams.put("order_by", order_by);
            filterRequestParams.put("order_type", order_type);
            KwlReturnObject result = accMasterItemsDAOobj.getMasterItemsForCustomHire(filterRequestParams);
            
            List<Object[]> fieldComboDataList = result.getEntityList();
            
            JSONArray dataArr = new JSONArray();
            for (Object[] fieldComboDataObjArr : fieldComboDataList) {
                FieldComboData fieldComboData = (FieldComboData) fieldComboDataObjArr[0];
                
                JSONObject fieldParamJobj = new JSONObject();
                fieldParamJobj.put("id", fieldComboData.getId());
                fieldParamJobj.put("name", fieldComboData.getValue());
                
                dataArr.put(fieldParamJobj);
            }
            
            jobj.put("data", dataArr);
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();
        } finally {
            try {
                jobj.put(SUCCESS, issuccess);
                jobj.put(MSG, msg);
            } catch (JSONException ex) {
                Logger.getLogger(accTaxController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return jobj;
    }
    
    /**
     * Method used to check whether the account is used before deactivating the
     * account.
     *
     * @param request
     * @return Map<String,String> Collection of account ID and usedId module.
     */
    private Map checkAccountUsedInModule(Map request) {
        Map<String, String> accountUsedIn = new HashMap<>();
        String companyid = (String) request.get("companyid");
        try {
            JSONArray jArr = new JSONArray((String) request.get("data"));
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                String accountid = jobj.getString("accid");
                if (!StringUtil.isNullOrEmpty(accountid)) {
                    KwlReturnObject object = accountingHandlerDAOobj.getObject(Account.class.getName(), accountid);
                    Account account = object.getEntityList().size() > 0 ? (Account) object.getEntityList().get(0) : null;
                    StringBuilder usedIn = new StringBuilder();
                    // Check Product Entry
                    KwlReturnObject result = accProductObj.getProductfromAccount(accountid, companyid);
                    int count = result.getRecordTotalCount();
                    if (count > 0) {
                        usedIn.append("Product");
                    }
                    // Check for Preferances Entry
                    result = accCompanyPreferencesObj.getPreferencesFromAccount(accountid, companyid);
                    count = result.getRecordTotalCount();
                    if (count > 0) {
                        if (usedIn.toString().length() > 0) {
                            usedIn.append(",");
                        }
                        usedIn.append("Company Preferances");
                    }
                    // Check Extra Preferences
                    result = accCompanyPreferencesObj.getExtraPreferencesFromAccount(accountid, companyid);
                    count = result.getRecordTotalCount();
                    if (count > 0) {
                        if (usedIn.toString().length() > 0) {
                            usedIn.append(",");
                        }
                        usedIn.append("Extra Company Preferences");
                    }
                    // Check Free Gift
                    result = accCompanyPreferencesObj.getAccountUsedForFreeGift(accountid, companyid);
                    count = result.getRecordTotalCount();
                    if (count > 0) {
                        if (usedIn.toString().length() > 0) {
                            usedIn.append(",");
                        }
                        usedIn.append("Free Gift");
                    }
                    // Check for Tax Entry
                    result = accTaxObj.getTaxFromAccount(accountid, companyid);
                    count = result.getRecordTotalCount();
                    if (count > 0) {
                        if (usedIn.toString().length() > 0) {
                            usedIn.append(",");
                        }
                        usedIn.append("Tax Entry");
                    }

                    //Check Whether Account used in Customer
                    HashMap<String, Object> reqParams = new HashMap<>();
                    reqParams.put("companyid", companyid);
                    reqParams.put("accontid", accountid);
                    result = accAccountDAOobj.getCustomerForCombo(reqParams);
                    count = result.getRecordTotalCount();
                    if (count > 0) {
                        if (usedIn.toString().length() > 0) {
                            usedIn.append(",");
                        }
                        usedIn.append("Customer");
                    }
//                    
                    //Check Whether Account used in Vendor
                    reqParams.clear();
                    reqParams.put(Constants.companyKey, companyid);
                    reqParams.put("accountid", accountid);
                    reqParams.put("isAccActivateDeactivate", true);
                    result = accAccountDAOobj.getVendorForCombo(reqParams);
                    count = result.getRecordTotalCount();
                    if (count > 0) {
                        if (usedIn.toString().length() > 0) {
                            usedIn.append(",");
                        }
                        usedIn.append("Vendor");
                    }

                    /*
                     Check Whether Account is used in Payment Metod.
                    */
                    result = accPaymentDAOobj.getPaymentMethodFromAccount(accountid, companyid);
                    count = result.getRecordTotalCount();
                    if (count > 0) {
                        if (usedIn.toString().length() > 0) {
                            usedIn.append(",");
                        }
                        usedIn.append("Payment Method");
                    }
                    // Check is Account previouly or currently used in control accounts.
                    if (account.isControlAccounts()) {
                        if (usedIn.toString().length() > 0) {
                            usedIn.append(",");
                        }
                        usedIn.append("Control Account(s)");
                    }
                    if (!StringUtil.isNullOrEmpty(usedIn.toString())) {
                        accountUsedIn.put(accountid, usedIn.toString());
                    }
                }
            }
        } catch (JSONException | ServiceException ex) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return accountUsedIn;
    }
    /**
     * get all limited accounts of particular master form account combo
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView getLimitedAccountsOfMasterForm(HttpServletRequest request, HttpServletResponse response){
        List<String> list = null;
        JSONObject jobj = new JSONObject();
        try {
            jobj.put("success", false);
            HashMap<String, Object> requestParams = StringUtil.convertRequestToMapObject(request);
            list = accAccountDAOobj.getLimitedAccountsOfMasterForm(requestParams);
            
            String accounts = "";
            for(String accId : list){
                accounts += accId + ",";
            }
            if(!StringUtil.isNullOrEmpty(accounts)){
                accounts = accounts.substring(0, accounts.length()-1);
            }
            jobj.put("accounts", accounts);
            jobj.put("success", true);
            jobj.put("totalCount", list.size());
        } catch (Exception e) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
        
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     * save all selected accounts of particular master form account combo
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView saveLimitedAccounts(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj = new JSONObject();
        try {
            jobj.put("success", false);
            HashMap<String, Object> requestParams = StringUtil.convertRequestToMapObject(request);
            JSONObject returnJobj = accAccountDAOobj.saveLimitedAccounts(requestParams);
            
            jobj.put("success", returnJobj.optBoolean("success", false));
            jobj.put("accountids", returnJobj.optString("accountids", ""));//accounts which are already mapped for transactions
            jobj.put("msg", returnJobj.optString("msg", ""));
        } catch (Exception e) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     * get all limited accounts for account combo
     * @param request
     * @param response
     * @return 
     */
    public ModelAndView getLimitedAccountsForCombo(HttpServletRequest request, HttpServletResponse response){
        JSONObject jobj = new JSONObject();
        try {
            jobj.put("success", false);
            HashMap<String, Object> requestParams = StringUtil.convertRequestToMapObject(request);
            List list = accAccountDAOobj.getLimitedAccountsForCombo(requestParams);
            
            JSONArray accJArr = new JSONArray();
            Iterator itr = list.iterator();
            while(itr.hasNext()){
                JSONObject accJobj = new JSONObject();
                Object[] obj = (Object[]) itr.next();
                accJobj.put("accountid", obj[0]);
                accJobj.put("accountname", obj[1]);
                accJobj.put("accountcode", obj[2]);
                accJobj.put("groupname", obj[3]);
                
                accJArr.put(accJobj);
            }
            
            jobj.put("data", accJArr);
            jobj.put("totalCount", list.size());
            jobj.put("success", true);
        } catch (Exception e) {
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, e);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    
     /*------------Get custom field for Products -----------------*/
    public ModelAndView getCustomFieldAsPerModuleId(HttpServletRequest request, HttpServletResponse response) {
      
        JSONArray jarray =new JSONArray();
        JSONObject datajobj = new JSONObject();
        boolean issuccess = false;
        String msg = "";
        String view = "jsonView_ex";
        try {

            String companyid = sessionHandlerImpl.getCompanyid(request);
            int moduleid = Integer.parseInt((String)request.getParameter("moduleid"));
            HashMap<String, Object> requestParams = new HashMap<String, Object>();

            requestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            requestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid));

            /*Get Asset Summary Details*/
            KwlReturnObject customDataresult = accAccountDAOobj.getFieldParamsUsingSql(requestParams);
            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {

                List list = customDataresult.getEntityList();
                Iterator itr = list.iterator();

                while (itr.hasNext()) {
                    JSONObject jobj = new JSONObject();
                    Object[] obj = (Object[]) itr.next();
                    jobj.put("fieldname", obj[0]);
                    jobj.put("fieldid", obj[1]);
                    jobj.put("columnnumber","col"+ obj[2]);
                    jobj.put("fieldtype", obj[3]);
                    jarray.put(jobj);
                    

                }
            }
           datajobj.put(Constants.RES_data, jarray);
           datajobj.put("totalCount", customDataresult.getEntityList().size());
            
            issuccess = true;
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                datajobj.put("success", issuccess);
                datajobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accAccountControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", datajobj.toString());
    }
}
