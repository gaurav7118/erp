/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.companypreferences;

import com.krawler.accounting.integration.common.IntegrationConstants;
import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
import com.krawler.esp.handlers.SendMailHandler;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.account.accAccountHandler;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesController;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.accounting.reports.AccReportsService;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.spring.storageHandler.storageHandlerImpl;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.esp.utils.ConfigReader;
import com.krawler.hql.accounting.companypreferenceservice.AccCompanyPreferencesService;
import com.krawler.hql.accounting.companypreferenceservice.AccCompanyPreferencesServiceImpl;
import com.krawler.inventory.model.configuration.InventoryConfig;
import com.krawler.inventory.model.configuration.InventoryConfigService;
import com.krawler.inventory.model.store.Store;
import com.krawler.spring.accounting.account.accAccountModuleService;
import java.io.*;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.MessagingException;
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

/**
 *
 * @author krawler
 */
public class accCompanyPreferencesControllerCMN extends MultiActionController implements MessageSourceAware {

    private HibernateTransactionManager txnManager;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private String successView;
    private auditTrailDAO auditTrailObj;
    private MessageSource messageSource;
    AccReportsService accReportsService;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    public ImportHandler importHandler;
    private accAccountDAO accAccountDAOobj;
    private accAccountModuleService accAccountModuleServiceObj;
    private ImportDAO importDao;
    private authHandlerDAO authHandlerDAOObj;
    private APICallHandlerService apiCallHandlerService; 
    private InventoryConfigService invConfigService;
    private AccCompanyPreferencesControllerCMNService accCompanyPreferencesControllerCMNService;
    private YearEndClosingProcess yearEndClosingProcess;
    
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
    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj){
        this.auditTrailObj = auditTrailDAOObj;
    }

    public void setAccReportsService(AccReportsService accReportsService) {
        this.accReportsService = accReportsService;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }
    
    public void setConfigService(InventoryConfigService configService) {
        this.invConfigService = configService;
    }

    public void setAccCompanyPreferencesControllerCMNService(AccCompanyPreferencesControllerCMNService accCompanyPreferencesControllerCMNService) {
        this.accCompanyPreferencesControllerCMNService = accCompanyPreferencesControllerCMNService;
    }

    public void setYearEndClosingProcess(YearEndClosingProcess yearEndClosingProcess) {
        this.yearEndClosingProcess = yearEndClosingProcess;
    }

    @Override
    public void setMessageSource(MessageSource ms) {
            this.messageSource=ms;
            accReportsService.setMessageSource(ms);
    }
    
    public void setAccAccountModuleServiceObj(accAccountModuleService accAccountModuleServiceObj) {
        this.accAccountModuleServiceObj = accAccountModuleServiceObj;
    }
    
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    public void setauthHandlerDAO(authHandlerDAO authHandlerDAOObj1) {
        this.authHandlerDAOObj = authHandlerDAOObj1;
    }
     public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }
    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }
    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {     
        this.apiCallHandlerService = apiCallHandlerService;
    }
    
    
    public ModelAndView saveCompanyAccountPreferences(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CAP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            saveCompanyAccountPreferences(request);
            issuccess = true;
            msg = messageSource.getMessage("acc.cp.save", null, RequestContextUtils.getLocale(request));   //"Account Preferences have been saved successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    public void saveCompanyAccountPreferences(HttpServletRequest request) throws ServiceException, AccountingException, SessionExpiredException {
        try {
            SimpleDateFormat formatter = (SimpleDateFormat) authHandler.getDateOnlyFormat();    //ERP-13711
            String  companyID = sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> prefMap = new HashMap<String, Object>();
            KwlReturnObject comp = accountingHandlerDAOobj.getObject(Company.class.getName(), sessionHandlerImpl.getCompanyid(request));
            Company company = (Company) comp.getEntityList().get(0);
            prefMap.put("fyfrom", formatter.parse(request.getParameter("fyfrom"))); //Date ll be save in form of 'MMMM d, yyyy 00:00:00'
            prefMap.put("bbfrom", formatter.parse(request.getParameter("bbfrom")));
            prefMap.put("firstfyfrom", formatter.parse(request.getParameter("firstfyfrom")));
            prefMap.put("withoutinventory", !StringUtil.isNullOrEmpty(request.getParameter("withoutinventory")));
            prefMap.put("withinvupdate", !StringUtil.isNullOrEmpty(request.getParameter("withinvupdate")));
            prefMap.put("editTransaction", !StringUtil.isNullOrEmpty(request.getParameter("editTransaction")));
            prefMap.put("editLinkedTransactionQuantity", !StringUtil.isNullOrEmpty(request.getParameter("editLinkedTransactionQuantity")));
            prefMap.put("editLinkedTransactionPrice", !StringUtil.isNullOrEmpty(request.getParameter("editLinkedTransactionPrice")));
            prefMap.put("autoPopulateMappedProduct", !StringUtil.isNullOrEmpty(request.getParameter("autoPopulateMappedProduct")));
            prefMap.put("columnPref", request.getParameter("columnPref") != null ? request.getParameter("columnPref") : "");
            prefMap.put("shipDateConfiguration", !StringUtil.isNullOrEmpty(request.getParameter("shipDateConfiguration")));
             prefMap.put("unitPriceInDO", !StringUtil.isNullOrEmpty(request.getParameter("unitPriceInDO")));
            prefMap.put("unitPriceInGR", !StringUtil.isNullOrEmpty(request.getParameter("unitPriceInGR")));
            prefMap.put("unitPriceInSR", !StringUtil.isNullOrEmpty(request.getParameter("unitPriceInSR")));
            prefMap.put("unitPriceInPR", !StringUtil.isNullOrEmpty(request.getParameter("unitPriceInPR")));
            prefMap.put("openPOandSO", !StringUtil.isNullOrEmpty(request.getParameter("openPOandSO")));
            prefMap.put("showAddressonPOSOSave", !StringUtil.isNullOrEmpty(request.getParameter("showAddressonPOSOSave")));
            prefMap.put("isAutoSaveAndPrintChkBox", !StringUtil.isNullOrEmpty(request.getParameter("isAutoSaveAndPrintChkBox")));
            prefMap.put("isshowmarginbutton", !StringUtil.isNullOrEmpty(request.getParameter("isShowMarginButton")));
            prefMap.put("customervendorsortingflag", request.getParameter("customervendorsortingflag"));
            prefMap.put("accountsortingflag", request.getParameter("accountsortingflag"));
            prefMap.put("manyCreditDebit", !StringUtil.isNullOrEmpty(request.getParameter("manyCreditDebit")));
            prefMap.put("deleteTransaction", !StringUtil.isNullOrEmpty(request.getParameter("deleteTransaction")));
            prefMap.put("DOSettings",Boolean.FALSE.parseBoolean(String.valueOf(request.getParameter("DOSettings"))));
            prefMap.put("GRSettings",Boolean.FALSE.parseBoolean(String.valueOf(request.getParameter("GRSettings"))));
            prefMap.put("updateInvLevelCheck", !StringUtil.isNullOrEmpty(request.getParameter("updateInvLevelCheck")));
            prefMap.put("isQaApprovalFlow", !StringUtil.isNullOrEmpty(request.getParameter("isQaApprovalFlow")));
            prefMap.put("isQaApprovalFlowInDO", !StringUtil.isNullOrEmpty(request.getParameter("isQaApprovalFlowInDO")));
            prefMap.put("isCustShipAddressInPurchase", !StringUtil.isNullOrEmpty(request.getParameter("isCustShipAddressInPurchase")));
            prefMap.put("editso", !StringUtil.isNullOrEmpty(request.getParameter("editso")));
            prefMap.put("memo", !StringUtil.isNullOrEmpty(request.getParameter("memo")));
            prefMap.put("vatNumber", !StringUtil.isNullOrEmpty(request.getParameter("companyvattinno"))?request.getParameter("companyvattinno"):"");
            prefMap.put("cstNumber", !StringUtil.isNullOrEmpty(request.getParameter("companycsttinno"))?request.getParameter("companycsttinno"):"");
            if(!StringUtil.isNullOrEmpty(request.getParameter("companycountryid")) && request.getParameter("companycountryid").equals(Constants.INDONESIAN_COUNTRYID)){
                prefMap.put("panNumber", !StringUtil.isNullOrEmpty(request.getParameter("companynpwpno"))?request.getParameter("companynpwpno"):"");
            }else{
                prefMap.put("panNumber", !StringUtil.isNullOrEmpty(request.getParameter("companypanno"))?request.getParameter("companypanno"):"");
            }
            prefMap.put("returncode", !StringUtil.isNullOrEmpty(request.getParameter("returncode"))?request.getParameter("returncode"):"");
            prefMap.put("cashoutaccforPOS", request.getParameter("cashoutaccountforpos"));
            if (!StringUtil.isNullOrEmpty(request.getParameter("dateOfRegistration"))) {
                prefMap.put("dateofregistration", authHandler.getDateOnlyFormat(request).parse(request.getParameter("dateOfRegistration")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("CSTRegDate"))) {
                prefMap.put("cstregistrationdate", authHandler.getDateOnlyFormat(request).parse(request.getParameter("CSTRegDate")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("enablevatcst"))) {
                prefMap.put("enablevatcst", Boolean.parseBoolean(request.getParameter("enablevatcst")));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("assessmentcircle"))){
                prefMap.put("assessmentcircle",request.getParameter("assessmentcircle"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("division"))){
                prefMap.put("division", request.getParameter("division"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("areacode"))){
                prefMap.put("areacode", request.getParameter("areacode"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("importexportcode"))){
                prefMap.put("importexportcode", request.getParameter("importexportcode"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("authorizedby"))){
                prefMap.put("authorizedby", request.getParameter("authorizedby"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("authrizedperson"))){
                prefMap.put("authorizedperson", request.getParameter("authrizedperson"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("statusordesignation"))){
                prefMap.put("statusordesignation", request.getParameter("statusordesignation"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("place"))){
                prefMap.put("place", request.getParameter("place"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("vattincomposition"))){
                prefMap.put("vattincomposition", request.getParameter("vattincomposition"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("vattinregular"))){
                prefMap.put("vattinregular", request.getParameter("vattinregular"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("localsalestaxnumber"))){
                prefMap.put("localsalestaxnumber", request.getParameter("localsalestaxnumber"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("interstatesalestaxnumber"))){
                prefMap.put("interstatesalestaxnumber", request.getParameter("interstatesalestaxnumber"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("typeofdealer"))){
                prefMap.put("typeofdealer", request.getParameter("typeofdealer"));
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("bankid"))){
                prefMap.put("bankid", request.getParameter("bankid"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("applicabilityofvat"))) {
                prefMap.put("applicabilityofvat", authHandler.getDateOnlyFormat(request).parse(request.getParameter("applicabilityofvat")));
            }
            prefMap.put("companytdsinterestrate", !StringUtil.isNullOrEmpty(request.getParameter("companytdsinterestrate")) ? Double.parseDouble(request.getParameter("companytdsinterestrate")) : 0);
            prefMap.put("serviceTaxRegNumber", !StringUtil.isNullOrEmpty(request.getParameter("companyservicetaxno"))?request.getParameter("companyservicetaxno"):"");
            prefMap.put("tanNumber", !StringUtil.isNullOrEmpty(request.getParameter("companytanno"))?request.getParameter("companytanno"):"");
            prefMap.put("eccNumber", !StringUtil.isNullOrEmpty(request.getParameter("companyeccno"))?request.getParameter("companyeccno"):"");
//            prefMap.put("AllowToMapAccounts", !StringUtil.isNullOrEmpty(request.getParameter("AllowToMapAccounts")));
            prefMap.put("showprodserial", !StringUtil.isNullOrEmpty(request.getParameter("showprodserial")));
            prefMap.put("isLocationCompulsory", !StringUtil.isNullOrEmpty(request.getParameter("isLocationCompulsory")));
            prefMap.put("isWarehouseCompulsory", !StringUtil.isNullOrEmpty(request.getParameter("isWarehouseCompulsory")));
            prefMap.put("isRowCompulsory", !StringUtil.isNullOrEmpty(request.getParameter("isRowCompulsory")));
            prefMap.put("isRackCompulsory", !StringUtil.isNullOrEmpty(request.getParameter("isRackCompulsory")));
            prefMap.put("isBinCompulsory", !StringUtil.isNullOrEmpty(request.getParameter("isBinCompulsory")));
            prefMap.put("isBatchCompulsory", !StringUtil.isNullOrEmpty(request.getParameter("isBatchCompulsory")));
            prefMap.put("isSerialCompulsory", !StringUtil.isNullOrEmpty(request.getParameter("isSerialCompulsory")));
            prefMap.put("isAutoFillBatchDetails", !StringUtil.isNullOrEmpty(request.getParameter("isAutoFillBatchDetails")));
            if(!StringUtil.isNullOrEmpty(request.getParameter("withouttax1099")))
               prefMap.put("withouttax1099", request.getParameter("withouttax1099"));
            prefMap.put("emailinvoice", !StringUtil.isNullOrEmpty(request.getParameter("emailinvoice")));
            prefMap.put("companyid", sessionHandlerImpl.getCompanyid(request));

            prefMap.put("discountgiven", request.getParameter("discountgiven"));
            prefMap.put("discountreceived", request.getParameter("discountreceived"));
            prefMap.put("shippingcharges", request.getParameter("shippingcharges"));
//            prefMap.put("othercharges", request.getParameter("othercharges"));
            prefMap.put("cashaccount", request.getParameter("cashaccount"));
            prefMap.put("foreignexchange", request.getParameter("foreignexchange"));
            prefMap.put("unrealisedgainloss", request.getParameter("unrealisedgainloss"));
            prefMap.put("depreciationaccount", request.getParameter("depreciationaccount"));
            prefMap.put("invoicesWriteOffAccount", request.getParameter("invoicesWriteOffAccount")!=null ? request.getParameter("invoicesWriteOffAccount"):"");
            prefMap.put("receiptWriteOffAccount", request.getParameter("receiptWriteOffAccount")!=null ? request.getParameter("receiptWriteOffAccount"):"");
            prefMap.put("roundingDifferenceAccount", request.getParameter("roundingDifferenceAccount") != null ? request.getParameter("roundingDifferenceAccount"): "");
            prefMap.put("adjustmentAccountPayment", request.getParameter("adjustmentAccountPayment") != null ? request.getParameter("adjustmentAccountPayment"): "");
            prefMap.put("adjustmentAccountReceipt", request.getParameter("adjustmentAccountReceipt") != null ? request.getParameter("adjustmentAccountReceipt"): "");
            prefMap.put("wastageDefaultAccount", request.getParameter("wastageDefaultAccount") != null ? request.getParameter("wastageDefaultAccount") : "");
            
            prefMap.put("gstnumber", request.getParameter("gstnumber"));
            if (!StringUtil.isNullOrEmpty(request.getParameter("enableGST"))) {
                prefMap.put("enableGST", Boolean.parseBoolean(request.getParameter("enableGST")));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("gstEffectiveDate"))) {
                prefMap.put("gstEffectiveDate", authHandler.getDateOnlyFormat(request).parse(request.getParameter("gstEffectiveDate")));
            }
            prefMap.put("industryCode", request.getParameter("industryCode"));
            prefMap.put("updateStockAdjustmentPrice",Boolean.FALSE.parseBoolean(String.valueOf(request.getParameter("updateStockAdjustmentPrice"))));
            prefMap.put("companyuen", request.getParameter("companyuen"));
            prefMap.put("iafversion", request.getParameter("iafversion"));
            prefMap.put("taxNumber", request.getParameter("taxNumber"));
            prefMap.put("expenseaccount", request.getParameter("expenseaccount"));
            prefMap.put("customerdefaultacc", request.getParameter("customerdefaultacc"));
            prefMap.put("vendordefaultacc", request.getParameter("vendordefaultacc"));
            prefMap.put("liabilityaccount", request.getParameter("liabilityaccount"));
            prefMap.put("negativestock", request.getParameter("negativestock"));
            prefMap.put("includeAmountInLimitSI", !StringUtil.isNullOrEmpty(request.getParameter("includeAmountInLimitSI")) ?Boolean.parseBoolean(request.getParameter("includeAmountInLimitSI")):true);
            prefMap.put("includeAmountInLimitPI", !StringUtil.isNullOrEmpty(request.getParameter("includeAmountInLimitPI")) ?Boolean.parseBoolean(request.getParameter("includeAmountInLimitPI")):true);
            prefMap.put("includeAmountInLimitSO", !StringUtil.isNullOrEmpty(request.getParameter("includeAmountInLimitSO")) ?Boolean.parseBoolean(request.getParameter("includeAmountInLimitSO")):true);
            prefMap.put("includeAmountInLimitPO", !StringUtil.isNullOrEmpty(request.getParameter("includeAmountInLimitPO")) ?Boolean.parseBoolean(request.getParameter("includeAmountInLimitPO")):true);
            prefMap.put("custcreditcontrol", request.getParameter("custcreditlimit"));
            prefMap.put("custcreditcontrolorder", request.getParameter("custcreditlimitorder"));
            prefMap.put("vendorcreditcontrolorder", request.getParameter("vendorcreditlimitorder"));
            prefMap.put("vendorCreditControl", request.getParameter("vendorCreditControl"));
            prefMap.put("chequeNoDuplicate", request.getParameter("chequeNoDuplicate"));
            prefMap.put("partNumber", !StringUtil.isNullOrEmpty(request.getParameter("partNumber")));
            prefMap.put("showLeadingZero", request.getParameter("showleadingzero"));
            prefMap.put("dependentField", !StringUtil.isNullOrEmpty(request.getParameter("dependentField")));
            prefMap.put("accountWithOrWithoutCode", request.getParameter("accountWithOrWithoutCode"));
            prefMap.put("custbudgetcontrol", request.getParameter("custMinBudget"));
            prefMap.put("viewDashboard", request.getParameter("viewDashboard"));
            prefMap.put("theme",request.getParameter("theme")==null?"":request.getParameter("theme"));
            prefMap.put("billaddress",request.getParameter("billaddress")==null?"":request.getParameter("billaddress"));
            prefMap.put("shipaddress",request.getParameter("shipaddress")==null?"":request.getParameter("shipaddress"));
            prefMap.put("remitpaymentto",request.getParameter("remitpaymentto")==null?"":request.getParameter("remitpaymentto"));
            prefMap.put("isaddressfromvendormaster",!StringUtil.isNullOrEmpty(request.getParameter("isaddressfromvendormaster")));
            prefMap.put("approvalMail", !StringUtil.isNullOrEmpty(request.getParameter("approvalMail")));
//            prefMap.put("amountInIndianWord", !StringUtil.isNullOrEmpty(request.getParameter("amountInIndianWord")));
            prefMap.put("sendmailto", request.getParameter("sendmailto"));
            prefMap.put("isDeferredRevenueRecognition", !StringUtil.isNullOrEmpty(request.getParameter("isDeferredRevenueRecognition"))?Boolean.parseBoolean(request.getParameter("isDeferredRevenueRecognition")):false);
            prefMap.put("salesAccount", request.getParameter("salesAccount"));
            prefMap.put("loandisbursementaccount", request.getParameter("loandisbursementaccount"));
            prefMap.put("loaninterestaccount", request.getParameter("loaninterestaccount"));
            prefMap.put("gstaccountforbaddebt", request.getParameter("gstaccountforbaddebt"));
            prefMap.put("gstbaddebtreleifaccount", request.getParameter("gstbaddebtreleifaccount"));//This a/c is for sales Releif (previously existing)
            prefMap.put("gstbaddebtrecoveraccount", request.getParameter("gstbaddebtrecoveraccount"));//This a/c is for sales Recover (previously existing)
            prefMap.put("gstbaddebtreleifpurchaseaccount", request.getParameter("gstbaddebtreleifpurchaseaccount")); //ERP-10400 This a/c is for purchase releif
            prefMap.put("gstbaddebtrecoverpurchaseaccount", request.getParameter("gstbaddebtrecoverpurchaseaccount"));//ERP-10400 This a/c is for purchase recover
            prefMap.put("gstbaddebtsuspenseaccount", request.getParameter("gstbaddebtsuspenseaccount"));
            prefMap.put("inputtaxadjustmentaccount", request.getParameter("inputtaxadjustmentaccount"));
            prefMap.put("taxCgaMalaysian", request.getParameter("taxCgaMalaysian"));
            prefMap.put("outputtaxadjustmentaccount", request.getParameter("outputtaxadjustmentaccount"));
            prefMap.put("freeGiftJEAccount", request.getParameter("freeGiftJEAccount"));
            prefMap.put("salesRevenueRecognitionAccount", request.getParameter("salesRevenueRecognitionAccount"));
            prefMap.put("showAllAccount",!StringUtil.isNullOrEmpty(request.getParameter("showAllAccount")));
            prefMap.put("showChildAccountsInTb",!StringUtil.isNullOrEmpty(request.getParameter("showChildAccountsInTb")));
            prefMap.put("showChildAccountsInGl",!StringUtil.isNullOrEmpty(request.getParameter("showChildAccountsInGl")));
            prefMap.put("showChildAccountsInPnl",!StringUtil.isNullOrEmpty(request.getParameter("showChildAccountsInPnl")));
            prefMap.put("showChildAccountsInBS",!StringUtil.isNullOrEmpty(request.getParameter("showChildAccountsInBS")));
            
            prefMap.put("showallaccountsinbs",!StringUtil.isNullOrEmpty(request.getParameter("showallaccountsinbs")));
            prefMap.put("showAllAccountInGl",!StringUtil.isNullOrEmpty(request.getParameter("showAllAccountInGl")));
            prefMap.put("showAllAccountsInPnl",!StringUtil.isNullOrEmpty(request.getParameter("showAllAccountsInPnl")));            
            prefMap.put("isnegativestockforlocwar",!StringUtil.isNullOrEmpty(request.getParameter("isnegativestockforlocwar")));            
            prefMap.put("isAllowQtyMoreThanLinkedDoc",!StringUtil.isNullOrEmpty(request.getParameter("isAllowQtyMoreThanLinkedDoc")));            
            prefMap.put("isAllowQtyMoreThanLinkedDocCross",!StringUtil.isNullOrEmpty(request.getParameter("isAllowQtyMoreThanLinkedDocCross")));            
            prefMap.put("productPriceinMultipleCurrency",!StringUtil.isNullOrEmpty(request.getParameter("productPriceinMultipleCurrency")));
            prefMap.put("stockValuationFlag",!StringUtil.isNullOrEmpty(request.getParameter("stockValuationFlag")));
            prefMap.put("isSalesOrderCreatedForCustomer",!StringUtil.isNullOrEmpty(request.getParameter("isSalesOrderCreatedForCustomer")));
            prefMap.put("isOutstandingInvoiceForCustomer",!StringUtil.isNullOrEmpty(request.getParameter("isOutstandingInvoiceForCustomer")));
            prefMap.put("isMinMaxOrdering",!StringUtil.isNullOrEmpty(request.getParameter("isMinMaxOrdering")));
            prefMap.put("blockPOcreationwithMinValue",!StringUtil.isNullOrEmpty(request.getParameter("blockPOcreationwithMinValue")));
            prefMap.put("leaseManagementFlag",!StringUtil.isNullOrEmpty(request.getParameter("leaseManagementFlag")));
            prefMap.put("consignmentSalesManagementFlag",!StringUtil.isNullOrEmpty(request.getParameter("consignmentSalesManagementFlag")));
            prefMap.put("consignmentPurchaseManagementFlag",!StringUtil.isNullOrEmpty(request.getParameter("consignmentPurchaseManagementFlag")));
            prefMap.put("systemManagementFlag",!StringUtil.isNullOrEmpty(request.getParameter("systemManagementFlag")));
            prefMap.put("masterManagementFlag",!StringUtil.isNullOrEmpty(request.getParameter("masterManagementFlag")));
            prefMap.put("generalledgerManagementFlag",!StringUtil.isNullOrEmpty(request.getParameter("generalledgerManagementFlag")));
            prefMap.put("accountsreceivablesalesFlag",!StringUtil.isNullOrEmpty(request.getParameter("accountsreceivablesalesFlag")));
            prefMap.put("accountpayableManagementFlag",!StringUtil.isNullOrEmpty(request.getParameter("accountpayableManagementFlag")));
            prefMap.put("securityGateEntryFlag",!StringUtil.isNullOrEmpty(request.getParameter("securityGateEntryFlag")));
            prefMap.put("assetManagementFlag",!StringUtil.isNullOrEmpty(request.getParameter("assetManagementFlag")));
            prefMap.put("statutoryManagementFlag",!StringUtil.isNullOrEmpty(request.getParameter("statutoryManagementFlag")));
            prefMap.put("miscellaneousManagementFlag",!StringUtil.isNullOrEmpty(request.getParameter("miscellaneousManagementFlag")));
            prefMap.put("onlyBaseCurrency",!StringUtil.isNullOrEmpty(request.getParameter("onlyBaseCurrency")));
            prefMap.put("packingdolist",!StringUtil.isNullOrEmpty(request.getParameter("packingdolist")));
            prefMap.put("versionslist",!StringUtil.isNullOrEmpty(request.getParameter("versionslist")));       
            prefMap.put("activateProductComposition",!StringUtil.isNullOrEmpty(request.getParameter("activateProductComposition")));
            prefMap.put("noOfDaysforValidTillField",!StringUtil.isNullOrEmpty(request.getParameter("noOfDaysforValidTillField"))?request.getParameter("noOfDaysforValidTillField"):-1);
            prefMap.put("recurringDeferredRevenueRecognition",!StringUtil.isNullOrEmpty(request.getParameter("recurringDeferredRevenueRecognition"))?Boolean.parseBoolean(request.getParameter("recurringDeferredRevenueRecognition")):false);
            prefMap.put("showAutoGeneratedChequeNumber",!StringUtil.isNullOrEmpty(request.getParameter("showAutoGeneratedChequeNumber")));
            prefMap.put("activateIBG", !StringUtil.isNullOrEmpty(request.getParameter("activateIBG")));
            prefMap.put("activateIBGCollection", !StringUtil.isNullOrEmpty(request.getParameter("activateIBGCollection")));
            prefMap.put("uobendtoendid", request.getParameter("uobendtoendid"));
            prefMap.put("uobpurposecode", request.getParameter("uobpurposecode"));
            prefMap.put("activateSalesContrcatManagement", !StringUtil.isNullOrEmpty(request.getParameter("activateSalesContrcatManagement")));
            prefMap.put("activateLoanManagementFlag", !StringUtil.isNullOrEmpty(request.getParameter("activateLoanManagementFlag")));
            prefMap.put("salesInvoiceGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("salesInvoiceGenerationMail")));
            prefMap.put("purchaseInvoiceGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("purchaseInvoiceGenerationMail")));
            prefMap.put("recurringInvoiceMail", !StringUtil.isNullOrEmpty(request.getParameter("recurringInvoiceMail")));
            prefMap.put("purchaseReqGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("purchaseReqGenerationMail")));
            prefMap.put("purchaseReqUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("purchaseReqUpdationMail")));
            prefMap.put("vendorQuotationGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("vendorQuotationGenerationMail")));
            prefMap.put("vendorQuotationUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("vendorQuotationUpdationMail")));
            prefMap.put("purchaseOrderGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("purchaseOrderGenerationMail")));
            prefMap.put("purchaseOrderUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("purchaseOrderUpdationMail")));
            prefMap.put("goodsReceiptGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("goodsReceiptGenerationMail")));
            prefMap.put("goodsReceiptUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("goodsReceiptUpdationMail")));
            prefMap.put("purchaseReturnGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("purchaseReturnGenerationMail")));
            prefMap.put("purchaseReturnUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("purchaseReturnUpdationMail")));
            prefMap.put("vendorPaymentGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("vendorPaymentGenerationMail")));
            prefMap.put("vendorPaymentUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("vendorPaymentUpdationMail")));
            prefMap.put("debitNoteGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("debitNoteGenerationMail")));
            prefMap.put("debitNoteUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("debitNoteUpdationMail")));
            //sales side
            prefMap.put("customerQuotationGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("customerQuotationGenerationMail")));
            prefMap.put("customerQuotationUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("customerQuotationUpdationMail")));
            prefMap.put("salesOrderGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("salesOrderGenerationMail")));
            prefMap.put("salesOrderUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("salesOrderUpdationMail")));
            prefMap.put("deleveryOrderGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("deleveryOrderGenerationMail")));
            prefMap.put("deleveryOrderUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("deleveryOrderUpdationMail")));
            prefMap.put("salesReturnGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("salesReturnGenerationMail")));
            prefMap.put("salesReturnUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("salesReturnUpdationMail")));
            prefMap.put("receiptGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("receiptGenerationMail")));
            prefMap.put("receiptUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("receiptUpdationMail")));
            prefMap.put("creditNoteGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("creditNoteGenerationMail")));
            prefMap.put("creditNoteUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("creditNoteUpdationMail")));
            
            //Lease Fixed Asset
            prefMap.put("leaseQuotationGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("leaseQuotationGenerationMail")));
            prefMap.put("leaseQuotationUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("leaseQuotationUpdationMail")));
            prefMap.put("leaseOrderGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("leaseOrderGenerationMail")));
            prefMap.put("leaseOrderUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("leaseOrderUpdationMail")));
            prefMap.put("leaseDeliveryOrderGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("leaseDeliveryOrderGenerationMail")));
            prefMap.put("leaseDeliveryOrderUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("leaseDeliveryOrderUpdationMail")));
            prefMap.put("leaseReturnGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("leaseReturnGenerationMail")));
            prefMap.put("leaseReturnUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("leaseReturnUpdationMail")));
            prefMap.put("leaseInvoiceGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("leaseInvoiceGenerationMail")));
            prefMap.put("leaseInvoiceUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("leaseInvoiceUpdationMail")));
            prefMap.put("leaseContractGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("leaseContractGenerationMail")));
            prefMap.put("leaseContractUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("leaseContractUpdationMail")));
            
            //Consignment Stock Sales Module 
            
            prefMap.put("consignmentReqGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("consignmentReqGenerationMail")));
            prefMap.put("consignmentReqUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("consignmentReqUpdationMail")));
            prefMap.put("consignmentDOGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("consignmentDOGenerationMail")));
            prefMap.put("consignmentDOUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("consignmentDOUpdationMail")));
            prefMap.put("consignmentInvoiceGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("consignmentInvoiceGenerationMail")));
            prefMap.put("consignmentInvoiceUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("consignmentInvoiceUpdationMail")));
            prefMap.put("consignmentReturnGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("consignmentReturnGenerationMail")));
            prefMap.put("consignmentReturnUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("consignmentReturnUpdationMail")));
            
            //Consignment Stock Purchase  Module 
            
            prefMap.put("consignmentPReqGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("consignmentPReqGenerationMail")));
            prefMap.put("consignmentPReqUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("consignmentPReqUpdationMail")));
            prefMap.put("consignmentPDOGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("consignmentPDOGenerationMail")));
            prefMap.put("consignmentPDOUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("consignmentPDOUpdationMail")));
            prefMap.put("consignmentPInvoiceGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("consignmentPInvoiceGenerationMail")));
            prefMap.put("consignmentPInvoiceUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("consignmentPInvoiceUpdationMail")));
            prefMap.put("consignmentPReturnGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("consignmentPReturnGenerationMail")));
            prefMap.put("consignmentPReturnUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("consignmentPReturnUpdationMail")));
            
            //Asset Module 
            
            prefMap.put("assetPurchaseReqGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("assetPurchaseReqGenerationMail")));
            prefMap.put("assetPurchaseReqUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("assetPurchaseReqUpdationMail")));
            prefMap.put("assetVendorQuotationGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("assetVendorQuotationGenerationMail")));
            prefMap.put("assetVendorQuotationUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("assetVendorQuotationUpdationMail")));
            prefMap.put("assetPurchaseOrderGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("assetPurchaseOrderGenerationMail")));
            prefMap.put("assetPurchaseOrderUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("assetPurchaseOrderUpdationMail")));
            prefMap.put("assetPurchaseInvoiceGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("assetPurchaseInvoiceGenerationMail")));
            prefMap.put("assetPurchaseInvoiceUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("assetPurchaseInvoiceUpdationMail")));
            prefMap.put("assetGoodsReceiptGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("assetGoodsReceiptGenerationMail")));
            prefMap.put("assetGoodsReceiptUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("assetGoodsReceiptUpdationMail")));
            prefMap.put("assetPurchaseReturnGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("assetPurchaseReturnGenerationMail")));
            prefMap.put("assetPurchaseReturnUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("assetPurchaseReturnUpdationMail")));
            prefMap.put("assetDisposalInvoiceGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("assetDisposalInvoiceGenerationMail")));
            prefMap.put("assetDisposalInvoiceUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("assetDisposalInvoiceUpdationMail")));
            prefMap.put("assetDeliveryOrderGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("assetDeliveryOrderGenerationMail")));
            prefMap.put("assetDeliveryOrderUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("assetDeliveryOrderUpdationMail")));
            prefMap.put("assetSalesReturnGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("assetSalesReturnGenerationMail")));
            prefMap.put("assetSalesReturnUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("assetSalesReturnUpdationMail")));
            
            prefMap.put("salesInvoiceUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("salesInvoiceUpdationMail")));
            prefMap.put("purchaseInvoiceUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("purchaseInvoiceUpdationMail")));
            prefMap.put("consignmentRequestApproval", !StringUtil.isNullOrEmpty(request.getParameter("consignmentRequestApproval")));
            prefMap.put("qtyBelowReorderLevelMail", !StringUtil.isNullOrEmpty(request.getParameter("qtyBelowReorderLevelMail")));
            prefMap.put("RFQGenerationMail", !StringUtil.isNullOrEmpty(request.getParameter("RFQGenerationMail")));
            prefMap.put("RFQUpdationMail", !StringUtil.isNullOrEmpty(request.getParameter("RFQUpdationMail")));
            prefMap.put("activateDDTemplateFlow", !StringUtil.isNullOrEmpty(request.getParameter("activateDDTemplateFlow")));
            prefMap.put("activateDDInsertTemplateLink", !StringUtil.isNullOrEmpty(request.getParameter("activateDDInsertTemplateLink")));
            
            prefMap.put("DashBoardImageFlag",Boolean.FALSE.parseBoolean(request.getParameter("DashBoardImage")));
            prefMap.put("UomSchemaType", request.getParameter("UomSchemaType"));
            prefMap.put("deliveryPlanner", !StringUtil.isNullOrEmpty(request.getParameter("deliveryPlanner")));
            prefMap.put("autoPopulateFieldsForDeliveryPlanner", !StringUtil.isNullOrEmpty(request.getParameter("autoPopulateFieldsForDeliveryPlanner")));
            prefMap.put("priceConfigurationAlert", !StringUtil.isNullOrEmpty(request.getParameter("priceConfigurationAlert")));
            prefMap.put("retainExchangeRate", !StringUtil.isNullOrEmpty(request.getParameter("retainExchangeRate")));
            prefMap.put("viewDetailsPerm", !StringUtil.isNullOrEmpty(request.getParameter("viewDetailsPerm")));
            prefMap.put("activateCRMIntegration", !StringUtil.isNullOrEmpty(request.getParameter("activateCRMIntegration")));
            prefMap.put("activateLMSIntegration", !StringUtil.isNullOrEmpty(request.getParameter("activateLMSIntegration")));
            prefMap.put("activateGroupCompanyIntegration", !StringUtil.isNullOrEmpty(request.getParameter("activateGroupCompanyIntegration")));
            prefMap.put("isPOSIntegration", !StringUtil.isNullOrEmpty(request.getParameter("isPOSIntegration")));
            prefMap.put("isCloseRegisterMultipleTimes",!StringUtil.isNullOrEmpty(request.getParameter("isCloseRegisterMultipleTimes"))?Boolean.parseBoolean(request.getParameter("isCloseRegisterMultipleTimes")):false);
            prefMap.put("allowCustVenCodeEditing",!StringUtil.isNullOrEmpty(request.getParameter("allowCustVenCodeEditing")));
            prefMap.put("isInventoryIntegration", !StringUtil.isNullOrEmpty(request.getParameter("isInventoryIntegration")));
//            prefMap.put("activateInventoryTab", !StringUtil.isNullOrEmpty(request.getParameter("activateInventoryTab")));
            prefMap.put("activateCycleCount", !StringUtil.isNullOrEmpty(request.getParameter("activateCycleCount")));
            prefMap.put("activateQAApprovalFlow", !StringUtil.isNullOrEmpty(request.getParameter("activateQAApprovalFlow")));
            prefMap.put("inspectionStore",request.getParameter("inspectionStore"));
            prefMap.put("repairStore", request.getParameter("repairStore"));
            prefMap.put("packinglocation", request.getParameter("packinglocation"));
            prefMap.put("packingstore", request.getParameter("packingstore"));
            prefMap.put("vendorjoborderstore", request.getParameter("vendorjoborderstore"));
            prefMap.put("interloconpick", !StringUtil.isNullOrEmpty(request.getParameter("interloconpick")));
            prefMap.put("pickpackship", !StringUtil.isNullOrEmpty(request.getParameter("pickpackship")));
            
            /**
             * Avalara Integration
             * Value of Avalara Integration flag is saved under column 'columnPref' which stores a stringified JSON
             */
            boolean isAvalaraIntegration = !StringUtil.isNullOrEmpty(request.getParameter(IntegrationConstants.avalaraIntegration));
            try {
                if (isAvalaraIntegration) {
                    JSONObject paramsJobj = new JSONObject();
                    paramsJobj.put(Constants.companyKey, companyID);
                    accAccountModuleServiceObj.insertFieldsForAvalara(paramsJobj);//To create custom fields and dimensions for Avalara Integration
                }
            } catch (Exception ex) {
                throw new AccountingException(messageSource.getMessage("acc.integration.errorCreatingCustomFields", null, RequestContextUtils.getLocale(request)) + " : " + ex.getMessage());
            }
            
            prefMap.put("showPivotInCustomReports", !StringUtil.isNullOrEmpty(request.getParameter("showPivotInCustomReports")));
            prefMap.put("sendimportmailto", request.getParameter("sendimportmailto"));
            prefMap.put("useremails", request.getParameter("useremails"));
            prefMap.put("customerForPOS", request.getParameter("customerForPOS")==null?"":request.getParameter("customerForPOS"));
            prefMap.put("vendorForPOS", request.getParameter("vendorForPOS")==null?"":request.getParameter("vendorForPOS"));
            prefMap.put("generateBarcodeParm", !StringUtil.isNullOrEmpty(request.getParameter("generateBarcodeParm")));
            prefMap.put("barcodetype", request.getParameter("barcodetype"));
            prefMap.put("SKUFieldParm", !StringUtil.isNullOrEmpty(request.getParameter("SKUFieldParm")));
            prefMap.put("SKUFieldRename", StringUtil.isNullOrEmpty(request.getParameter("SKUFieldRename"))?"":request.getParameter("SKUFieldRename"));
            prefMap.put("productPricingOnBands", !StringUtil.isNullOrEmpty(request.getParameter("productPricingOnBands")));
            prefMap.put("productPricingOnBandsForSales", !StringUtil.isNullOrEmpty(request.getParameter("productPricingOnBandsForSales")));
            prefMap.put("bandsWithSpecialRateForSales", !StringUtil.isNullOrEmpty(request.getParameter("bandsWithSpecialRateForSales")) ? Boolean.parseBoolean(request.getParameter("bandsWithSpecialRateForSales")) : false);
            prefMap.put("barcodeDPI", request.getParameter("barcdDpi"));
            prefMap.put("barcodeHeight", request.getParameter("barcdHeight"));
            prefMap.put("pricePrintType", request.getParameter("pricePrintType"));
            prefMap.put("barcdTopMargin", request.getParameter("barcdTopMargin"));
            prefMap.put("barcdLeftMargin", request.getParameter("barcdLeftMargin"));
            prefMap.put("barcdLabelHeight", request.getParameter("barcdLabelHeight"));            
            prefMap.put("priceTranslateX", request.getParameter("priceTranslateX"));
            prefMap.put("priceTranslateY", request.getParameter("priceTranslateY"));
            prefMap.put("priceFontSize", request.getParameter("priceFontSize"));
            prefMap.put("pricePrefix", request.getParameter("pricePrefix")==null?"":request.getParameter("pricePrefix"));
            prefMap.put("generateBarcodeWithPriceParm", !StringUtil.isNullOrEmpty(request.getParameter("generateBarcodeWithPriceParm")));
            
            //Save data relating with the Barcode Product Name
            prefMap.put("pnamePrintType", request.getParameter("pnamePrintType"));
            prefMap.put("pnameTranslateX", request.getParameter("pnameTranslateX"));
            prefMap.put("pnameTranslateY", request.getParameter("pnameTranslateY"));
            prefMap.put("pnameFontSize", request.getParameter("pnameFontSize"));
            prefMap.put("pnamePrefix", request.getParameter("pnamePrefix")==null?"":request.getParameter("pnamePrefix"));
            prefMap.put("generateBarcodeWithPnameParm", !StringUtil.isNullOrEmpty(request.getParameter("generateBarcodeWithPnameParm")));
            
            //Save data relating with the Barcode along with Product ID
            prefMap.put("pidPrintType", request.getParameter("pidPrintType"));
            prefMap.put("pidTranslateX", request.getParameter("pidTranslateX"));
            prefMap.put("pidTranslateY", request.getParameter("pidTranslateY"));
            prefMap.put("pidFontSize", request.getParameter("pidFontSize"));
            prefMap.put("pidPrefix", request.getParameter("pidPrefix")==null?"":request.getParameter("pidPrefix"));
            prefMap.put("generateBarcodeWithPidParm", !StringUtil.isNullOrEmpty(request.getParameter("generateBarcodeWithPidParm")));
            
            
            //Save data relating with the Barcode along with Product Max Retail Price
            prefMap.put("mrpPrintType", request.getParameter("mrpPrintType"));
            prefMap.put("mrpTranslateX", request.getParameter("mrpTranslateX"));
            prefMap.put("mrpTranslateY", request.getParameter("mrpTranslateY"));
            prefMap.put("mrpFontSize", request.getParameter("mrpFontSize"));
            prefMap.put("mrpPrefix", request.getParameter("mrpPrefix")==null?"":request.getParameter("mrpPrefix"));
            prefMap.put("generateBarcodeWithMrpParm", !StringUtil.isNullOrEmpty(request.getParameter("generateBarcodeWithMrpParm")));
            
            prefMap.put("isAutoRefershReportonSave", !StringUtil.isNullOrEmpty(request.getParameter("isAutoRefershReportonSave")) ? Boolean.parseBoolean(request.getParameter("isAutoRefershReportonSave")) : false);
            prefMap.put("ProductSelectionType", request.getParameter("ProductSelectionType"));
            prefMap.put("defaultmailsenderFlag", request.getParameter("defaultmailsenderFlag"));
            prefMap.put("custvenloadtype", request.getParameter("custvenloadtype"));
            prefMap.put("proddiscripritchtextboxflag", request.getParameter("proddiscripritchtextboxflag"));
            prefMap.put("productsortingflag", request.getParameter("productsortingflag"));
            prefMap.put("downloadglprocessflag", request.getParameter("downloadglprocessflag"));
            prefMap.put("downloadDimPLprocessflag", request.getParameter("downloadDimPLprocessflag"));
            prefMap.put("downloadSOAprocessflag", request.getParameter("downloadSOAprocessflag"));
            prefMap.put("isMovementWarehouseMapping", !StringUtil.isNullOrEmpty(request.getParameter("isMovementWarehouseMapping")));
            prefMap.put("SalesSelectionType", request.getParameter("SalesSelectionType"));
            prefMap.put("PurchaseSelectionType", request.getParameter("PurchaseSelectionType"));
            prefMap.put("InvoiceTermsSetting", request.getParameter("InvoiceTermsSetting"));
            prefMap.put("activateProfitMargin",request.getParameter("activateProfitMargin"));
            prefMap.put("activateToBlockSpotRate",!StringUtil.isNullOrEmpty(request.getParameter("activateToBlockSpotRate")));
            prefMap.put("hierarchicalDimensions",!StringUtil.isNullOrEmpty(request.getParameter("hierarchicalDimensions")));
            prefMap.put("activateimportForJE",request.getParameter("activateimportForJE"));
            prefMap.put("activateCRblockingWithoutStock",request.getParameter("activateCRblockingWithoutStock"));
            prefMap.put("activatefromdateToDate",request.getParameter("activatefromdateToDate"));
            prefMap.put("isDuplicateItems", !StringUtil.isNullOrEmpty(request.getParameter("isDuplicateItems")));//Duplicate Products
            prefMap.put("isFilterProductByCustomerCategory", !StringUtil.isNullOrEmpty(request.getParameter("isFilterProductByCustomerCategory")));
            prefMap.put("closedStatusforDo", !StringUtil.isNullOrEmpty(request.getParameter("closedStatusforDo")));
            prefMap.put("defaultWarehouse", request.getParameter("defaultWarehouse"));
            prefMap.put("liabilityAccountForLMS",request.getParameter("liabilityAccountForLMS"));
            prefMap.put("showVendorUpdate",!StringUtil.isNullOrEmpty(request.getParameter("showVendorUpdate")));
            prefMap.put("showCustomerUpdate",!StringUtil.isNullOrEmpty(request.getParameter("showCustomerUpdate")));
            prefMap.put("showProductUpdate",!StringUtil.isNullOrEmpty(request.getParameter("showProductUpdate")));
            prefMap.put("isBaseUOMRateEdit",!StringUtil.isNullOrEmpty(request.getParameter("isBaseUOMRateEdit")) ?Boolean.parseBoolean(request.getParameter("isBaseUOMRateEdit")):false);
            prefMap.put("allowZeroUntiPriceForProduct",!StringUtil.isNullOrEmpty(request.getParameter("allowZeroUntiPriceForProduct")) ?Boolean.parseBoolean(request.getParameter("allowZeroUntiPriceForProduct")):false);
            prefMap.put("requestApprovalFlow", !StringUtil.isNullOrEmpty(request.getParameter("requestApprovalFlow")));
            prefMap.put("autoPopulateDeliveredQuantity",!StringUtil.isNullOrEmpty(request.getParameter("autoPopulateDeliveredQuantity")));
            prefMap.put("defaultTemplateLogoFlag",!StringUtil.isNullOrEmpty(request.getParameter("defaultTemplateLogoFlag")));
            prefMap.put("enableLinkToSelWin", !StringUtil.isNullOrEmpty(request.getParameter("enableLinkToSelWin")));
            prefMap.put("showBulkInvoices", !StringUtil.isNullOrEmpty(request.getParameter("showBulkInvoices")));
            prefMap.put("showBulkInvoicesFromSO", !StringUtil.isNullOrEmpty(request.getParameter("showBulkInvoicesFromSO")));//putting  setting for Bulk invoice from SO in map 
            prefMap.put("showBulkDOFromSO", !StringUtil.isNullOrEmpty(request.getParameter("showBulkDOFromSO")));//putting  setting for Bulk invoice from SO in map 
            prefMap.put("enablesalespersonAgentFlow",!StringUtil.isNullOrEmpty(request.getParameter("enablesalespersonAgentFlow")) ?Boolean.parseBoolean(request.getParameter("enablesalespersonAgentFlow")):false);
            prefMap.put("viewallexcludecustomerwithoutsalesperson",!StringUtil.isNullOrEmpty(request.getParameter("viewallexcludecustomerwithoutsalesperson")) ?Boolean.parseBoolean(request.getParameter("viewallexcludecustomerwithoutsalesperson")):false);
            prefMap.put("BuildAssemblyApprovalFlow",!StringUtil.isNullOrEmpty(request.getParameter("BuildAssemblyApprovalFlow")) ?Boolean.parseBoolean(request.getParameter("BuildAssemblyApprovalFlow")):false);
            prefMap.put("isPRmandatory",!StringUtil.isNullOrEmpty(request.getParameter("isPRmandatory")) ?Boolean.parseBoolean(request.getParameter("isPRmandatory")):false);
            prefMap.put("splitOpeningBalanceAmount", !StringUtil.isNullOrEmpty(request.getParameter("splitOpeningBalanceAmount")));
            prefMap.put("defaultsequenceformatforrecinv",!StringUtil.isNullOrEmpty(request.getParameter("defaultsequenceformatforrecinv")) ?Boolean.parseBoolean(request.getParameter("defaultsequenceformatforrecinv")):false);
            prefMap.put("pickaddressfrommaster",!StringUtil.isNullOrEmpty(request.getParameter("pickaddressfrommaster")) ?Boolean.parseBoolean(request.getParameter("pickaddressfrommaster")):false);
            prefMap.put("gstIncomeGroup",!StringUtil.isNullOrEmpty(request.getParameter("gstIncomeGroup")) ?Boolean.parseBoolean(request.getParameter("gstIncomeGroup")):false);
            prefMap.put("paymentMethodAsCard",!StringUtil.isNullOrEmpty(request.getParameter("paymentMethodAsCard")) ?Boolean.parseBoolean(request.getParameter("paymentMethodAsCard")):false);
            prefMap.put("jobOrderItemFlow",!StringUtil.isNullOrEmpty(request.getParameter("jobOrderItemFlow")) ?Boolean.parseBoolean(request.getParameter("jobOrderItemFlow")):false);
            prefMap.put("productsearchingflag", !StringUtil.isNullOrEmpty(request.getParameter("productsearchingflag")) ? request.getParameter("productsearchingflag") : 1);
            prefMap.put("usersVisibilityFlow",!StringUtil.isNullOrEmpty(request.getParameter("usersVisibilityFlow")) ?Boolean.parseBoolean(request.getParameter("usersVisibilityFlow")):false);
            prefMap.put("usersspecificinfoFlow",!StringUtil.isNullOrEmpty(request.getParameter("usersspecificinfoFlow")) ?Boolean.parseBoolean(request.getParameter("usersspecificinfoFlow")):false);
            prefMap.put("jobWorkOutFlow",!StringUtil.isNullOrEmpty(request.getParameter("jobWorkOutFlow")) ?Boolean.parseBoolean(request.getParameter("jobWorkOutFlow")):false);
            prefMap.put("salesCommissionReportMode", request.getParameter("salesCommissionReportMode"));
            prefMap.put("salesorderreopen", !StringUtil.isNullOrEmpty(request.getParameter("salesorderreopen")) ?Boolean.parseBoolean(request.getParameter("salesorderreopen")):false);
            prefMap.put("isActiveLandingCostOfItem", !StringUtil.isNullOrEmpty(request.getParameter("isActiveLandingCostOfItem")) ?Boolean.parseBoolean(request.getParameter("isActiveLandingCostOfItem")):false);//ERP-20637
            prefMap.put("propagatetochildcompanies",!StringUtil.isNullOrEmpty(request.getParameter("propagatetochildcompanies")) ?Boolean.parseBoolean(request.getParameter("propagatetochildcompanies")):false);
            prefMap.put("activateWastageCalculation", !StringUtil.isNullOrEmpty(request.getParameter("activateWastageCalculation")) ? Boolean.parseBoolean(request.getParameter("activateWastageCalculation")) : false);
            prefMap.put("calculateproductweightmeasurment", !StringUtil.isNullOrEmpty(request.getParameter("calculateproductweightmeasurment")) ? Boolean.parseBoolean(request.getParameter("calculateproductweightmeasurment")) : false);
            prefMap.put("carryForwardPriceForCrossLinking", !StringUtil.isNullOrEmpty(request.getParameter("carryForwardPriceForCrossLinking")) ? Boolean.parseBoolean(request.getParameter("carryForwardPriceForCrossLinking")) : false);
            prefMap.put("isCurrencyCode",request.getParameter("isCurrencyCode"));
            prefMap.put("loggedInUserId", sessionHandlerImpl.getUserid(request));
            prefMap.put("negativeStockSO", request.getParameter("negativestockso"));
            prefMap.put("negativestockformulaso", request.getParameter("negativestockformulaso"));
            prefMap.put("negativestockformulasi", request.getParameter("negativestockformulasi"));
            prefMap.put("negativeStockSICS", request.getParameter("negativestocksics"));
            prefMap.put("negativeStockPR", request.getParameter("negativestockpr"));
            prefMap.put("showzeroamountasblank", !StringUtil.isNullOrEmpty(request.getParameter("showzeroamountasblank")));
            prefMap.put("showaccountcodeinfinancialreport", !StringUtil.isNullOrEmpty(request.getParameter("showaccountcodeinfinancialreport")));
            prefMap.put("badDebtProcessingPeriod", StringUtil.isNullOrEmpty(request.getParameter("badDebtProcessingPeriod"))?"":Integer.parseInt(request.getParameter("badDebtProcessingPeriod")));
            prefMap.put("badDebtProcessingPeriodType", StringUtil.isNullOrEmpty(request.getParameter("badDebtProcessingPeriodType"))?"":Integer.parseInt(request.getParameter("badDebtProcessingPeriodType")));
            prefMap.put("gstSubmissionPeriod", StringUtil.isNullOrEmpty(request.getParameter("gstSubmissionPeriod"))?"":Integer.parseInt(request.getParameter("gstSubmissionPeriod")));
            /* --------------------------- Indain Company For TDS Flow (ERP-20931)---------------------- */
            prefMap.put("isTDSapplicable", !StringUtil.isNullOrEmpty(request.getParameter("isTDSapplicable")));            
            prefMap.put("isSTapplicable", !StringUtil.isNullOrEmpty(request.getParameter("isSTApplicable")));            
            prefMap.put("deductortype", !StringUtil.isNullOrEmpty(request.getParameter("deductortype"))?request.getParameter("deductortype"):0);
            prefMap.put("headofficetanno", !StringUtil.isNullOrEmpty(request.getParameter("headofficetanno"))?request.getParameter("headofficetanno"):"");
            if(!StringUtil.isNullOrEmpty(request.getParameter("registrationType"))){
                prefMap.put("registrationType",request.getParameter("registrationType")); 
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("manufacturerType"))){
                 prefMap.put("manufacturerType", request.getParameter("manufacturerType"));
            }
            prefMap.put("unitname", !StringUtil.isNullOrEmpty(request.getParameter("unitname"))?request.getParameter("unitname"):"");
            prefMap.put("exciseTariffdetails", !StringUtil.isNullOrEmpty(request.getParameter("exciseTariffdetails")));
            prefMap.put("tariffName", !StringUtil.isNullOrEmpty(request.getParameter("tariffName"))?request.getParameter("tariffName"):"");
            prefMap.put("HSNCode", !StringUtil.isNullOrEmpty(request.getParameter("HSNCode"))?request.getParameter("HSNCode"):"");
            prefMap.put("reportingUOM", !StringUtil.isNullOrEmpty(request.getParameter("reportingUOM"))?request.getParameter("reportingUOM"):"");
            prefMap.put("exciseMethod", !StringUtil.isNullOrEmpty(request.getParameter("excisemethod"))?request.getParameter("excisemethod"):"");
            if (!StringUtil.isNullOrEmpty(request.getParameter("exciserate"))) {
                prefMap.put("exciseRate", Double.parseDouble(request.getParameter("exciserate")));
            }
            prefMap.put("excisejurisdictiondetails", !StringUtil.isNullOrEmpty(request.getParameter("excisejurisdictiondetails")));
            prefMap.put("exciseMultipleUnit", !StringUtil.isNullOrEmpty(request.getParameter("exciseMultipleUnit")));
            prefMap.put("commissioneratecode", !StringUtil.isNullOrEmpty(request.getParameter("commissioneratecode"))?request.getParameter("commissioneratecode"):"");
            prefMap.put("commissioneratename", !StringUtil.isNullOrEmpty(request.getParameter("commissioneratename"))?request.getParameter("commissioneratename"):"");
            prefMap.put("serviceTaxRegNumber", !StringUtil.isNullOrEmpty(request.getParameter("servicetaxregno"))?request.getParameter("servicetaxregno"):"");
            prefMap.put("divisioncode", !StringUtil.isNullOrEmpty(request.getParameter("divisioncode"))?request.getParameter("divisioncode"):"");
            prefMap.put("rangecode", !StringUtil.isNullOrEmpty(request.getParameter("rangecode"))?request.getParameter("rangecode"):"");
            prefMap.put("tdsincometaxcircle", !StringUtil.isNullOrEmpty(request.getParameter("tdsincometaxcircle"))?request.getParameter("tdsincometaxcircle"):"");
            prefMap.put("tdsrespperson", !StringUtil.isNullOrEmpty(request.getParameter("tdsrespperson"))?request.getParameter("tdsrespperson"):"");
            prefMap.put("tdsresppersonfathersname", !StringUtil.isNullOrEmpty(request.getParameter("tdsresppersonfathersname"))?request.getParameter("tdsresppersonfathersname"):"");
            // TDS Fields-Start
            if (company.getCountry() != null && Integer.parseInt(company.getCountry().getID()) == Constants.indian_country_id) {
                prefMap.put("isAddressChanged", !StringUtil.isNullOrEmpty(request.getParameter("isAddressChanged")));
                prefMap.put("resposiblePersonAddress", !StringUtil.isNullOrEmpty(request.getParameter("resposiblePersonAddress"))?request.getParameter("resposiblePersonAddress"):"");
                prefMap.put("resposiblePersonTeleNumber", !StringUtil.isNullOrEmpty(request.getParameter("resposiblePersonTeleNumber"))?request.getParameter("resposiblePersonTeleNumber"):"");
                prefMap.put("resposiblePersonMobNumber", !StringUtil.isNullOrEmpty(request.getParameter("resposiblePersonMobNumber"))?request.getParameter("resposiblePersonMobNumber"):"");
                prefMap.put("resposiblePersonEmail", !StringUtil.isNullOrEmpty(request.getParameter("resposiblePersonEmail"))?request.getParameter("resposiblePersonEmail"):"");
                prefMap.put("resposiblePersonPostal", !StringUtil.isNullOrEmpty(request.getParameter("resposiblePersonPostal"))?request.getParameter("resposiblePersonPostal"):"");
                prefMap.put("resposiblePersonPAN", !StringUtil.isNullOrEmpty(request.getParameter("resposiblePersonPAN"))?request.getParameter("resposiblePersonPAN"):"");
                prefMap.put("resposiblePersonHasAddressChanged", !StringUtil.isNullOrEmpty(request.getParameter("resposiblePersonHasAddressChanged")));
                prefMap.put("resposiblePersonstate", !StringUtil.isNullOrEmpty(request.getParameter("resposiblePersonstate"))?request.getParameter("resposiblePersonstate"):"");
                prefMap.put("assessmentYear", !StringUtil.isNullOrEmpty(request.getParameter("AssessmentYear"))?request.getParameter("AssessmentYear"):"");
                prefMap.put("CINnumber", !StringUtil.isNullOrEmpty(request.getParameter("CINnumber"))?request.getParameter("CINnumber"):"");
                
                // GST-Start
                prefMap.put("isGSTApplicable", !StringUtil.isNullOrEmpty(request.getParameter("isGSTApplicable")));
                if(!StringUtil.isNullOrEmpty(request.getParameter("showIndiaCompanyPreferencesTab")) && Boolean.parseBoolean(request.getParameter("showIndiaCompanyPreferencesTab")) ){
                    prefMap.put("GSTIN", !StringUtil.isNullOrEmpty(request.getParameter("GSTIN"))?request.getParameter("GSTIN"):"");
                }
                // GST-End
            }
            // TDS Fields-End
            prefMap.put("tdsresppersondesignation", !StringUtil.isNullOrEmpty(request.getParameter("tdsresppersondesignation"))?request.getParameter("tdsresppersondesignation"):"");
            String mrpFlag=request.getParameter("activatemrpmodule");
            prefMap.put("activatemrpmodule", !StringUtil.isNullOrEmpty(request.getParameter("activatemrpmodule")));
            prefMap.put("inventoryvaluationtype", request.getParameter("inventoryvaluationtype"));
            prefMap.put("isExciseApplicable", !StringUtil.isNullOrEmpty(request.getParameter("isExciseApplicable")));            
            prefMap.put("excisecommissioneratecode", !StringUtil.isNullOrEmpty(request.getParameter("excisecommissioneratecode"))?request.getParameter("excisecommissioneratecode"):"");
            prefMap.put("excisecommissioneratename", !StringUtil.isNullOrEmpty(request.getParameter("excisecommissioneratename"))?request.getParameter("excisecommissioneratename"):"");
            prefMap.put("excisedivisioncode", !StringUtil.isNullOrEmpty(request.getParameter("excisedivisioncode"))?request.getParameter("excisedivisioncode"):"");
            prefMap.put("exciserangecode", !StringUtil.isNullOrEmpty(request.getParameter("exciserangecode"))?request.getParameter("exciserangecode"):"");
            
            
            prefMap.put("interStatePurAccCformID", !StringUtil.isNullOrEmpty(request.getParameter("interStatePurAccCformID"))?request.getParameter("interStatePurAccCformID"):"");
            prefMap.put("interStatePurAccID", !StringUtil.isNullOrEmpty(request.getParameter("interStatePurAccID"))?request.getParameter("interStatePurAccID"):"");
            prefMap.put("interStatePurAccReturnCformID", !StringUtil.isNullOrEmpty(request.getParameter("interStatePurAccReturnCformID"))?request.getParameter("interStatePurAccReturnCformID"):"");
            prefMap.put("interStatePurReturnAccID", !StringUtil.isNullOrEmpty(request.getParameter("interStatePurReturnAccID"))?request.getParameter("interStatePurReturnAccID"):"");
            prefMap.put("interStateSalesAccCformID", !StringUtil.isNullOrEmpty(request.getParameter("interStateSalesAccCformID"))?request.getParameter("interStateSalesAccCformID"):"");
            prefMap.put("interStateSalesAccID", !StringUtil.isNullOrEmpty(request.getParameter("interStateSalesAccID"))?request.getParameter("interStateSalesAccID"):"");
            prefMap.put("interStateSalesAccReturnCformID", !StringUtil.isNullOrEmpty(request.getParameter("interStateSalesAccReturnCformID"))?request.getParameter("interStateSalesAccReturnCformID"):"");
            prefMap.put("interStateSalesReturnAccID", !StringUtil.isNullOrEmpty(request.getParameter("interStateSalesReturnAccID"))?request.getParameter("interStateSalesReturnAccID"):"");
            prefMap.put("salesaccountidcompany", !StringUtil.isNullOrEmpty(request.getParameter("salesaccountidcompany"))?request.getParameter("salesaccountidcompany"):"");
            prefMap.put("salesretaccountidcompany", !StringUtil.isNullOrEmpty(request.getParameter("salesretaccountidcompany"))?request.getParameter("salesretaccountidcompany"):"");
            prefMap.put("purchaseretaccountidcompany", !StringUtil.isNullOrEmpty(request.getParameter("purchaseretaccountidcompany"))?request.getParameter("purchaseretaccountidcompany"):"");
            prefMap.put("purchaseaccountidcompany", !StringUtil.isNullOrEmpty(request.getParameter("purchaseaccountidcompany"))?request.getParameter("purchaseaccountidcompany"):"");
            
            /*VAT Accounts*/
            prefMap.put("vatPayableAcc", !StringUtil.isNullOrEmpty(request.getParameter("vatPayableAcc"))?request.getParameter("vatPayableAcc"):"");
            prefMap.put("vatInCreditAvailAcc", !StringUtil.isNullOrEmpty(request.getParameter("vatInCreditAvailAcc"))?request.getParameter("vatInCreditAvailAcc"):"");
            prefMap.put("CSTPayableAcc", !StringUtil.isNullOrEmpty(request.getParameter("CSTPayableAcc"))?request.getParameter("CSTPayableAcc"):"");
            prefMap.put("paymentMethod", !StringUtil.isNullOrEmpty(request.getParameter("pmtMethod"))?request.getParameter("pmtMethod"):"");
            /*Excise Accounts*/
            prefMap.put("excisePayableAcc", !StringUtil.isNullOrEmpty(request.getParameter("excisePayableAcc"))?request.getParameter("excisePayableAcc"):"");
            prefMap.put("exciseDutyAdvancePaymentaccount", !StringUtil.isNullOrEmpty(request.getParameter("exciseDutyAdvancePaymentaccount"))?request.getParameter("exciseDutyAdvancePaymentaccount"):null);
            /*Service Tax Accounts*/
            prefMap.put("STPayableAcc", !StringUtil.isNullOrEmpty(request.getParameter("STPayableAcc"))?request.getParameter("STPayableAcc"):"");
            prefMap.put("STAdvancePaymentaccount", !StringUtil.isNullOrEmpty(request.getParameter("STAdvancePaymentaccount"))?request.getParameter("STAdvancePaymentaccount"):"");
            prefMap.put("gtakkcpaybleaccount", !StringUtil.isNullOrEmpty(request.getParameter("GTAKKCPaybleAccount"))?request.getParameter("GTAKKCPaybleAccount"):"");
            prefMap.put("gtasbcpaybleaccount", !StringUtil.isNullOrEmpty(request.getParameter("GTASBCPaybleAccount"))?request.getParameter("GTASBCPaybleAccount"):"");
            /*-----------------------------------------------------------------------------------------*/
            
            prefMap.put("negativeValueIn", !StringUtil.isNullOrEmpty(request.getParameter("negativeValueIn"))?Integer.parseInt(request.getParameter("negativeValueIn")):2);
            prefMap.put("customdutyaccount", !StringUtil.isNullOrEmpty(request.getParameter("customdutyaccount")) ? request.getParameter("customdutyaccount") : "");
            prefMap.put("igstaccount", !StringUtil.isNullOrEmpty(request.getParameter("igstaccount")) ? request.getParameter("igstaccount") : "");
            if (!StringUtil.isNullOrEmpty(request.getParameter("gstdeactivationdate"))) {
                prefMap.put("gstdeactivationdate", authHandler.getDateOnlyFormat(request).parse(request.getParameter("gstdeactivationdate")));
            }
            
            /******************* MRP module Settings **************************************/
            prefMap.put("autoGenPurchaseType", request.getParameter("autoGenPurchaseType"));
            prefMap.put("woInventoryUpdateType", request.getParameter("woInventoryUpdateType"));
            prefMap.put("mrpProductComponentType", request.getParameter("mrpProductComponentType"));
            
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.isMultiEntity))) {
                prefMap.put(Constants.isMultiEntity, Boolean.parseBoolean(request.getParameter(Constants.isMultiEntity)));
            }
            // "gafversion" is for Malaysian company.
            if (!StringUtil.isNullOrEmpty(request.getParameter("gafversion"))) {
                prefMap.put("iafversion", request.getParameter("gafversion"));
            }
            
            prefMap.put("allowCustomerCheckInCheckOut",Boolean.FALSE.parseBoolean(String.valueOf(request.getParameter("allowCustomerCheckInCheckOut"))));
            
            try {
                /*
                * Inserting Custom Dimension in SI,PI,JE,MP,RP,DN,CN,DO for process multi entity 
                * if Custom Dimension is not created from company setup
                */
                if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.isMultiEntity)) && Boolean.parseBoolean(request.getParameter(Constants.isMultiEntity)) && !Boolean.parseBoolean(request.getParameter(Constants.isDimensionCreated))) {
                    HashMap<String, Object> requestParams = new HashMap<>();
                    requestParams.put(Constants.isMultiEntity, Boolean.parseBoolean(request.getParameter(Constants.isMultiEntity)));
                    requestParams.put(Constants.companyid, companyID);
                    String subdomain = URLUtil.getDomainName(request);
                    requestParams.put("DefaultValue", subdomain);
                    String countryId=(String)company.getCountry().getID()!=null?(String)company.getCountry().getID():"";
                    requestParams.put("countryid",countryId);
                    boolean isDimensionCreated = accAccountDAOobj.insertDefaultCustomeFields(requestParams);
                    prefMap.put(Constants.isDimensionCreated, isDimensionCreated);
                }
            } catch (Exception ex) {
                prefMap.put(Constants.isDimensionCreated, false);
                throw new AccountingException("Error while Creating Entity Dimension Fields : " + ex.getMessage());
            }
            
            if (!StringUtil.isNullOrEmpty(request.getParameter("inventoryvaluationtype")) && Integer.parseInt(request.getParameter("inventoryvaluationtype")) == Constants.PERPETUAL_VALUATION_METHOD) {
                prefMap.put("inventoryaccountid", !StringUtil.isNullOrEmpty(request.getParameter("inventoryaccountid")) ? request.getParameter("inventoryaccountid") : "");
                prefMap.put("cogsaccountid", !StringUtil.isNullOrEmpty(request.getParameter("cogsaccountid")) ? request.getParameter("cogsaccountid") : "");
                prefMap.put("stockadjustmentaccountid", !StringUtil.isNullOrEmpty(request.getParameter("stockadjustmentaccountid")) ? request.getParameter("stockadjustmentaccountid") : "");
            }
            //get flag for Enable Cash Receive Return fields
            if (!StringUtil.isNullOrEmpty(request.getParameter("enableCashReceiveReturn"))) {
                prefMap.put("enableCashReceiveReturn", Boolean.parseBoolean(request.getParameter("enableCashReceiveReturn")));
            }
            /**
             *  Save or Update Company preferences 
             */
            CompanyAccountPreferences preferences;
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("id", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject result = accCompanyPreferencesObj.getCompanyPreferences(requestParams);
            preferences = (CompanyAccountPreferences) result.getEntityList().get(0);
            if (preferences == null) {
                preferences = new CompanyAccountPreferences();
                result = accCompanyPreferencesObj.addPreferences(prefMap);
            } else { 
                prefMap.put("id", sessionHandlerImpl.getCompanyid(request));
                result = accCompanyPreferencesObj.updatePreferences(prefMap);
            }
            if(!StringUtil.isNullOrEmpty(request.getParameter("activateQAApprovalFlow"))){
                saveInventoryConfiguration(request);
            }
            preferences = (CompanyAccountPreferences) result.getEntityList().get(0);
            
            /**
             *  Save or Update ExtraCompany preferences 
             */
            ExtraCompanyPreferences extraCompanyPreferences=null;
            Map<String, Object> requestParamsExtra = new HashMap<String, Object>();
            requestParamsExtra.put("id", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject resultExtra = accCompanyPreferencesObj.getExtraCompanyPreferences(requestParamsExtra);
            if(!resultExtra.getEntityList().isEmpty()){
                extraCompanyPreferences = (ExtraCompanyPreferences) resultExtra.getEntityList().get(0);
            }
            if (extraCompanyPreferences == null) {
                extraCompanyPreferences = new ExtraCompanyPreferences();
            } else {
                prefMap.put("id", sessionHandlerImpl.getCompanyid(request));
            }
            boolean isMRPActivationSettingChanged = false;
            if (!StringUtil.isNullOrEmpty(mrpFlag)) {
                if (true != extraCompanyPreferences.isActivateMRPModule()) {
                    isMRPActivationSettingChanged = true;
                }
            } else {
                if (false != extraCompanyPreferences.isActivateMRPModule()) {
                    isMRPActivationSettingChanged = true;
                }
            }
            /*
            If CRM integration flag changed
            */
            String crmFlag=request.getParameter("activateCRMIntegration");
            boolean isCRMActivationSettingChanged = false;
            if (!StringUtil.isNullOrEmpty(crmFlag)) {
                if (true != extraCompanyPreferences.isActivateCRMIntegration()) {
                    isCRMActivationSettingChanged = true;
                }
            } else {
                if (false != extraCompanyPreferences.isActivateCRMIntegration()) {
                    isCRMActivationSettingChanged = true;
                }
            }
            Boolean isCloseRegister=extraCompanyPreferences.isIsCloseRegisterMultipleTimes();
            resultExtra = accCompanyPreferencesObj.addOrUpdateExtraPreferences(prefMap);
            extraCompanyPreferences = (ExtraCompanyPreferences) resultExtra.getEntityList().get(0);
            
            /**
             *  Save or Update MRPCompany preferences 
             */
            MRPCompanyPreferences mrpCompanyPreferences=null;
            Map<String, Object> requestParamsMRP = new HashMap<>();
            requestParamsExtra.put("id", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject MRPresult = accCompanyPreferencesObj.getMRPCompanyPreferences(requestParamsMRP);
            if(!MRPresult.getEntityList().isEmpty()){
                mrpCompanyPreferences = (MRPCompanyPreferences) MRPresult.getEntityList().get(0);
            }
            if (mrpCompanyPreferences == null) {
                mrpCompanyPreferences = new MRPCompanyPreferences();
            } else {
                prefMap.put("id", sessionHandlerImpl.getCompanyid(request));
            }
            resultExtra = accCompanyPreferencesObj.addOrUpdateMRPPreferences(prefMap);
            mrpCompanyPreferences = (MRPCompanyPreferences) resultExtra.getEntityList().get(0);
            
            if (company.getCountry() != null && Integer.parseInt(company.getCountry().getID()) == Constants.indian_country_id) {
                IndiaComplianceCompanyPreferences complianceCompanyPreferences = null;
                Map<String, Object> requestParamsCompliance = new HashMap<>();
                requestParamsCompliance.put("id", sessionHandlerImpl.getCompanyid(request));
                KwlReturnObject Complianceresult = accCompanyPreferencesObj.getIndiaComplianceExtraCompanyPreferences(requestParamsCompliance);
                if (!Complianceresult.getEntityList().isEmpty()) {
                    complianceCompanyPreferences = (IndiaComplianceCompanyPreferences) Complianceresult.getEntityList().get(0);
                }
                if (complianceCompanyPreferences == null) {
                    complianceCompanyPreferences = new IndiaComplianceCompanyPreferences();
                } else {
                    prefMap.put("id", sessionHandlerImpl.getCompanyid(request));
                }
                resultExtra = accCompanyPreferencesObj.addOrUpdateIndiaComplianceExtraPreferences(prefMap);
                complianceCompanyPreferences = (IndiaComplianceCompanyPreferences) resultExtra.getEntityList().get(0);
            }
            if (extraCompanyPreferences.isIsPOSIntegration() && extraCompanyPreferences.isIsCloseRegisterMultipleTimes() != isCloseRegister) {
                String subdomain = sessionHandlerImpl.getCompanySessionObj(request).getCdomain();
                String posURL = this.getServletContext().getInitParameter("posURL");
                String action = "37";
//                Session session = HibernateUtil.getCurrentSession();
                JSONObject resObj = new JSONObject();
                JSONObject userData = new JSONObject();
                try {
                    userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
                    userData.put("userid", sessionHandlerImpl.getUserid(request));
                    userData.put("iscommit", true);
                    userData.put("companyid", extraCompanyPreferences.getId());
                    userData.put("subdomain", subdomain);
                    userData.put("isClosedRegisterMultipleTimes", extraCompanyPreferences.isIsCloseRegisterMultipleTimes());

                } catch (JSONException ex) {
                    Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                }
//                session = HibernateUtil.getCurrentSession();;
                resObj = apiCallHandlerService.callApp(posURL, userData,  extraCompanyPreferences.getId(), action);
            }
            //To Add or Update the Document mail Settings 
            DocumentEmailSettings documentEmailSettings=null;
            Map<String, Object> requestParamsDoc = new HashMap<String, Object>();
            requestParamsDoc.put("id", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject resultDocument = accCompanyPreferencesObj.getDocumentEmailSettings(requestParamsDoc);
            if(!resultDocument.getEntityList().isEmpty()){
                documentEmailSettings = (DocumentEmailSettings) resultDocument.getEntityList().get(0);
            }
            if (documentEmailSettings == null) {
                documentEmailSettings = new DocumentEmailSettings();
            } else {
                prefMap.put("id", sessionHandlerImpl.getCompanyid(request));
            }
            resultDocument = accCompanyPreferencesObj.addOrUpdateDocumentEmailSettings(prefMap);
            documentEmailSettings = (DocumentEmailSettings) resultDocument.getEntityList().get(0);
            
            request.setAttribute("preferences", preferences);
            saveYearLock(request,extraCompanyPreferences, preferences);
            Map<String, Object> map = new HashMap();
            map.put("companyid", extraCompanyPreferences.getId());
            map.put("userid", sessionHandlerImpl.getUserid(request));
            if (isMRPActivationSettingChanged) {
                map.put("mrppm", extraCompanyPreferences.isActivateMRPModule());
                SendMRPActivationRequest(map);
            }
            
            /**
             * Below function is used to send isQaApprovalFlowInMRP activation req to PM.
             */
            boolean isQaApprovalFlowInMRP = false;
            if (extraCompanyPreferences.isActivateMRPModule()) {
                map.put("mrppm", extraCompanyPreferences.isActivateMRPModule());
                if (prefMap.containsKey("columnPref")) {
                    if (!StringUtil.isNullOrEmpty(prefMap.get("columnPref").toString())) {
                        JSONObject columnPrefNew = null;
                        try {
                            columnPrefNew = new JSONObject(prefMap.get("columnPref").toString());
                        } catch (JSONException ex) {
                            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        if (columnPrefNew.has("isQaApprovalFlowInMRP")) {
                            isQaApprovalFlowInMRP = columnPrefNew.optBoolean("isQaApprovalFlowInMRP", false);
                        }
                    }
                }
                map.put("isQaApprovalFlowInMRP", isQaApprovalFlowInMRP);
                SendQaApprovalFlowActivationRequest(map);
            }
            
            if (!Boolean.parseBoolean(StorageHandler.getStandalone()) && isCRMActivationSettingChanged) {
                map.put("isCRMEnable", extraCompanyPreferences.isActivateCRMIntegration());
                sendCRMActivationRequestToRemoteApp(map);
            }
            
            JSONObject paramsJson = new JSONObject();
            //ERP-25134 - Convert Request Params into JSON Obeject
            paramsJson = getKeyValueFormationOfHttpServletRequestParams(request);
            request.setAttribute("companyprefdetails", paramsJson.toString());
            auditTrailObj.insertAuditLog(AuditAction.COMPANY_ACCOUNT_PREFERENCES_UPDATE, "User " + sessionHandlerImpl.getUserFullName(request) + " from " + preferences.getCompany().getCompanyName() + " changed company's account preferences", request, preferences.getID());
            
            /*
                ERP-25134 - Below code has written by considering deprecation of above method auditTrailObj.insertAuditLog(--).
                Un-comment below code only when above method gets removed / commented here.            
            */
//            Map<String, Object> requestMap = new HashMap();
//            requestMap.put("userid", sessionHandlerImpl.getUserid(request));
//            requestMap.put("prdjsondtls", request.getParameter("detail"));
//            String ipaddr = "";
//            if (StringUtil.isNullOrEmpty(request.getHeader("x-real-ip"))) {
//                ipaddr = request.getRemoteAddr();
//            } else {
//                ipaddr = request.getHeader("x-real-ip");
//            }
//            requestMap.put("remoteAddress", ipaddr);
//            requestMap.put("companyprefdetails", paramsJson.toString());
//            auditTrailObj.insertAuditLog(AuditAction.COMPANY_ACCOUNT_PREFERENCES_UPDATE, "User " + sessionHandlerImpl.getUserFullName(request) + " from " + preferences.getCompany().getCompanyName() + " changed company's account preferences", requestMap, preferences.getID());
        } catch (ParseException ex) {
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveCompanyAccountPreferences : " + ex.getMessage(), ex);
        }
    }
    
    /*
        ERP-25134 - Below method has used to convert request parameters into Map as KEY:VALUE pair.
        Later convert this MAP into JSON Object and return it for further process.
    */
    public JSONObject getKeyValueFormationOfHttpServletRequestParams(HttpServletRequest request) throws ServiceException {
        JSONObject paramsJson = new JSONObject();
        Map paramsMap = new HashMap();
        String key = "", value = "";
        try {
            paramsMap = request.getParameterMap();
            Iterator mapit = paramsMap.keySet().iterator();
            while (mapit.hasNext()) {
                key = (String) mapit.next();
                value = ((String[]) paramsMap.get(key))[0];
                paramsJson.put(key, value);
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE(e.getMessage(), e);
        }
        return paramsJson;
    }

    public JSONObject SendMRPActivationRequest(Map<String, Object> map) {
        JSONObject jSONObject = new JSONObject();
        jSONObject = sendMRPActivationRequestToRemoteApp(map);
        return jSONObject;
    }
    
    public JSONObject SendQaApprovalFlowActivationRequest(Map<String, Object> map) {
        JSONObject jSONObject = new JSONObject();
        jSONObject = SendQaApprovalFlowActivationRequestToRemoteApp(map);
        return jSONObject;
    }
    
    public ModelAndView checkYearEndClosingCheckList(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean success = false;
        StringBuilder msg = new StringBuilder();
        try {
            JSONObject requestJSON = StringUtil.convertRequestToJsonObject(request);
            requestJSON.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject compaccprefKwlReturnObject = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), requestJSON.optString(Constants.companyKey));
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) compaccprefKwlReturnObject.getEntityList().get(0);
            if (extraCompanyPreferences != null && StringUtil.isNullOrEmpty(extraCompanyPreferences.getProfitLossAccountId())) {
                msg.append(messageSource.getMessage("acc.compref.map.previous.year.accumulated.profitloss", null, RequestContextUtils.getLocale(request)));
            } else {
                JSONObject dataObject = accCompanyPreferencesControllerCMNService.checkYearEndClosingCheckList(requestJSON);
                jobj.put("data", dataObject);
            }
            success = true;
        } catch (JSONException | SessionExpiredException | ParseException | ServiceException ex) {
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            success = false;
        } finally {
            try {
                jobj.put("success", success);
                jobj.put("msg", msg.toString());
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    /**
     * Method to check- Sales trasnactions or Purchase transaction are present or not.
     * 
     */
    public ModelAndView checkSalesorPurchaseTransactionsPresent(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean success = false;
        boolean isTransacionsPresent = false;
        StringBuilder msg = new StringBuilder();
        try {
            JSONObject requestJSON = StringUtil.convertRequestToJsonObject(request);
            requestJSON.put(Constants.companyKey, sessionHandlerImpl.getCompanyid(request));
            boolean isSalesSide = requestJSON.optBoolean("issaleside", false);
            if (isSalesSide) {
                isTransacionsPresent = accCompanyPreferencesControllerCMNService.isSalesSideTransactionPresent(requestJSON);
            } else {
                isTransacionsPresent = accCompanyPreferencesControllerCMNService.isPurchaseSideTransactionPresent(requestJSON);
            }
            success = true;
        } catch (JSONException | SessionExpiredException | ParseException | ServiceException ex) {
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            success = false;
        } finally {
            try {
                jobj.put("istransacionstpresent", isTransacionsPresent);
                jobj.put("success", success);
                jobj.put("msg", msg.toString());
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public JSONObject sendMRPActivationRequestToRemoteApp(Map<String, Object> map) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            /*
             Below function is used to send activation req to PM
             */
            String userid = "";
            String companyid = "";
            boolean activate=false;
            if (map.containsKey("userid")) {
                userid = (String) map.get("userid");
            }
            if (map.containsKey("companyid")) {
                companyid = (String) map.get("companyid");
            }
            if (map.containsKey("mrppm")) {
                activate = (Boolean) map.get("mrppm");
            }
            String accRestURL = URLUtil.buildRestURL("pmURL");
            JSONObject userData = new JSONObject();
            userData.put("userid", userid);
            userData.put("companyid", companyid);
            userData.put("enablemrp", activate);
            String endpoint = accRestURL + "company/checks?request=" + userData.toString();
            JSONObject resObj = apiCallHandlerService.restGetMethod(endpoint, userData.toString());
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
    
    public JSONObject SendQaApprovalFlowActivationRequestToRemoteApp(Map<String, Object> map) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {
            /**
             * Below function is used to send activation req to PM.
             */
            String userid = "";
            String companyid = "";
            boolean MRPactivate=false;
            boolean isQaApprovalFlowInMRP=false;

            if (map.containsKey("userid")) {
                userid = (String) map.get("userid");
            }
            if (map.containsKey("companyid")) {
                companyid = (String) map.get("companyid");
            }
            if (map.containsKey("mrppm")) {
                MRPactivate = (Boolean) map.get("mrppm");
            }
            if (map.containsKey("isQaApprovalFlowInMRP")) {
                isQaApprovalFlowInMRP = (Boolean) map.get("isQaApprovalFlowInMRP");
            }
            
            JSONObject userData = new JSONObject();
            userData.put("userid", userid);
            userData.put("companyid", companyid);
            userData.put("enablemrp", MRPactivate);
            userData.put("isQaApprovalFlowInMRP", isQaApprovalFlowInMRP);
            
            String accRestURL = URLUtil.buildRestURL("pmURL");
            String endpoint = accRestURL + "company/qaapprovalflowinmrp";
            JSONObject resObj = apiCallHandlerService.restGetMethod(endpoint, userData.toString());    
            
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
    /*
             Below function is used to send activation req to CRM
     */

    public JSONObject sendCRMActivationRequestToRemoteApp(Map<String, Object> map) {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean success = false;
        try {

            String userid = "";
            String companyid = "";
            boolean activate = false;
            if (map.containsKey("userid")) {
                userid = (String) map.get("userid");
            }
            if (map.containsKey("companyid")) {
                companyid = (String) map.get("companyid");
            }
            if (map.containsKey("isCRMEnable")) {
                activate = (Boolean) map.get("isCRMEnable");
            }
            JSONObject resObj = new JSONObject();
            JSONObject userData = new JSONObject();
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", userid);
            userData.put("iscommit", true);
            userData.put("companyid", companyid);
            userData.put("category", "integrationDeskeraApps");
            userData.put("deskeracrmflowcheckfield", activate);
            String crmURL = this.getServletContext().getInitParameter("crmURL");
            String action = "219";
            resObj = apiCallHandlerService.callApp(crmURL, userData, companyid, action);
            success = true;
        } catch (Exception ex) {
            msg = ex.getMessage();
        } finally {
            try {
                jobj.put(Constants.RES_success, success);
                jobj.put(Constants.RES_msg, msg == null ? "null" : msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
    public ModelAndView saveYearLock(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj=new JSONObject();
        String msg="";
        boolean issuccess = false;

        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("CAP_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject extraKwlReturnObject = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraKwlReturnObject.getEntityList().get(0);
            KwlReturnObject compaccprefKwlReturnObject = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) compaccprefKwlReturnObject.getEntityList().get(0);
            saveYearLock(request, extraCompanyPreferences, companyAccountPreferences);
            issuccess = true;
            msg = messageSource.getMessage("acc.cp.lock", null, RequestContextUtils.getLocale(request));   //"Lock has been Updated successfully";
            txnManager.commit(status);
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = ""+ex.getMessage();
            Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
        } finally{
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }

    private void saveYearLock(HttpServletRequest request, ExtraCompanyPreferences extraCompanyPreferences, CompanyAccountPreferences companyAccountPreferences) throws ServiceException, SessionExpiredException, ParseException {
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            JSONObject requestJSON = StringUtil.convertRequestToJsonObject(request);
            StringBuilder  reversalYearList = new StringBuilder();
            StringBuilder  closedYearList = new StringBuilder();
            Map<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            Integer maxYear = null;
            /* Get reversed year list */
            for (int i = jArr.length() - 1; i >= 0; i--) {
                JSONObject jobj = jArr.getJSONObject(i);
                Map<String, Object> yearLockMap = new HashMap<>();
                int yearId = Integer.parseInt(StringUtil.DecodeText(jobj.optString("name")));
                yearLockMap.put("yearid", yearId);
                yearLockMap.put("islock", "true".equalsIgnoreCase(StringUtil.DecodeText(jobj.optString("islock"))));
                yearLockMap.put("companyid", companyid);
                String yearLockid = StringUtil.DecodeText(jobj.optString("id"));
                if ((!jobj.optString("islock").equals("false")) && !(jobj.optBoolean(Constants.CHECKLIST_DOCUMENT_REVALUATION_COMPLETED, false) || jobj.optBoolean(Constants.CHECKLIST_ADJUSTMENT_FOR_TRANSACTIONS_COMPLETED, false) || jobj.optBoolean(Constants.CHECKLIST_INVENTORY_ADJUSTMENT_COMPLETED, false) || jobj.optBoolean(Constants.CHECKLIST_ASSET_DEPRECIATION_COMPLETED, false))) {
                    throw new AccountingException(messageSource.getMessage("acc.compref.closebook.checklist.all.items.needs.tobe.checked", null, RequestContextUtils.getLocale(request)));
                }
                if (!StringUtil.isNullOrEmpty(yearLockid)) {
                    KwlReturnObject yearLockResult = accountingHandlerDAOobj.getObject(YearLock.class.getName(), yearLockid);
                    if (!yearLockResult.getEntityList().isEmpty()) {
                        YearLock yearLockObj = (YearLock) yearLockResult.getEntityList().get(0);
                        if ((yearLockObj.isIsLock() && yearLockMap.containsKey("islock") && !(Boolean) yearLockMap.get("islock"))) {
                            if (reversalYearList.length() == 0) {
                                reversalYearList.append(yearLockObj.getYearid());
                            } else {
                                reversalYearList.append(",").append(yearLockObj.getYearid());
                            }
                        }
                        if (!yearLockObj.isIsLock() && yearLockMap.containsKey("islock") && (Boolean) yearLockMap.get("islock")) {
                            if (closedYearList.length() == 0) {
                                closedYearList .append(yearLockObj.getYearid());
                            } else {
                                closedYearList.append(",").append(yearLockObj.getYearid());
                            }
                        }
                    }
                }
                if ((!jobj.optString("islock").equals("false"))) {
                    if (maxYear == null || maxYear < yearId) {
                        maxYear = yearId;
                    }
                }
            }
            /* Check if user has reversed any book closing */
            if (!StringUtil.isNullOrEmpty(reversalYearList.toString()) && reversalYearList.length() > 0) {
                String[] reversalYears = reversalYearList.toString().split(",");
                for (String reversalYear : reversalYears) {
                    JSONObject checkReversalJSON = new JSONObject();
                    checkReversalJSON.put("yearid", reversalYear);
                    checkReversalJSON.put("gt", true);
                    checkReversalJSON.put(Constants.companyKey, companyid);
                    boolean isBookClosed = accCompanyPreferencesObj.isBookClose(checkReversalJSON);
                    if (isBookClosed) {
                        throw new AccountingException("Reversal of financial book(s) need to be done sequentially.");
                    }
                }
            }
            /**
             * Set Activate Date Range
             */
            Calendar startFinYearCal = Calendar.getInstance();
            Calendar currentYearCal = Calendar.getInstance();
            startFinYearCal.setTime(companyAccountPreferences.getFirstFinancialYearFrom() != null ? companyAccountPreferences.getFirstFinancialYearFrom() : companyAccountPreferences.getFinancialYearFrom());
            Calendar endFinYearCal = Calendar.getInstance();

            endFinYearCal.set(Calendar.DAY_OF_MONTH, startFinYearCal.get(Calendar.DAY_OF_MONTH) - 1);
            endFinYearCal.set(Calendar.MONTH, startFinYearCal.get(Calendar.MONTH));
            endFinYearCal.set(Calendar.YEAR, currentYearCal.get(Calendar.YEAR) + 1);
            
            if (maxYear != null) {
                startFinYearCal.set(Calendar.YEAR, maxYear + 1);
            }
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).info("Start Date : " + startFinYearCal.getTime());
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).info("End Date : " + endFinYearCal.getTime());
            if (!StringUtil.isNullOrEmpty(closedYearList.toString()) || !StringUtil.isNullOrEmpty(reversalYearList.toString())) {
                HashMap<String, Object> params = new HashMap<>();
                params.put("companyid", companyid);
                params.put("company", companyid);
                params.put("fromdate", authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(startFinYearCal.getTime())));
                /**
                 * Set ActiveToDate only in following cases: # ActiveToDate is
                 * not set # If ActiveFromDate to be set is greater
                 * than or equal to ActiveToDate
                 */
                if (extraCompanyPreferences.getActiveToDate() == null || (extraCompanyPreferences.getActiveToDate() != null && startFinYearCal.getTime().compareTo(extraCompanyPreferences.getActiveToDate()) >= 0)) {
                    params.put("todate", authHandler.getDateOnlyFormat().parse(authHandler.getDateOnlyFormat().format(endFinYearCal.getTime())));
                }
                accAccountDAOobj.addActiveDateRange(params);
            }
            /* 
             *Calculate and store the closing balances in tables using separate thread 
             */
            Map<String, Object> requestMap = new HashMap<>();
            requestMap.put("companyAccountPreferences", companyAccountPreferences);
            requestMap.put("extraCompanyPreferences", extraCompanyPreferences);
            requestJSON.put("fullName", sessionHandlerImpl.getUserFullName(request));
            requestMap.put("requestJSON", requestJSON);
            requestMap.put("jsonArray", jArr);
            requestMap.put("requestParams", requestParams);
            requestMap.put(Constants.companyKey, companyid);
            requestMap.put(Constants.companyKey, companyid);
            yearEndClosingProcess.add(requestMap);
            Thread t = new Thread(yearEndClosingProcess);
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).info("Year End Closing Process for Company ID " + companyid + " to be started.");
            t.start();
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).info("Year End Closing Process for Company ID " + companyid + " has been started.");
        } catch (JSONException ex) {
            Logger.getLogger(AccCompanyPreferencesControllerCMNServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveYearLock : " + ex.getMessage(), ex);
        } catch (AccountingException ex) {
            Logger.getLogger(AccCompanyPreferencesControllerCMNServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), "erp24", false);
        } catch (SessionExpiredException | ServiceException | NumberFormatException ex) {
            Logger.getLogger(AccCompanyPreferencesControllerCMNServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("saveYearLock : " + ex.getMessage(), ex);
        }
    }

    public ModelAndView addActiveDateRange(HttpServletRequest request,HashMap<String, Object> requestParams) {
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
            requestParams.put("companyid", companyid);
            requestParams.put("company", sessionHandlerImpl.getCompanyid(request));
            kmsg = accAccountDAOobj.addActiveDateRange(requestParams);
            issuccess = true;
            txnManager.commit(status);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            txnManager.rollback(status);
        } finally{
            try {
                jobj.put("success", issuccess);
//                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
 public ModelAndView ImportSequenceFormat(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        HashMap<String,Object> requestparams = new HashMap<>();
        requestparams.put("locale", RequestContextUtils.getLocale(request));
        try {
            String doAction = request.getParameter("do");
            System.out.println("A(( " + doAction + " start : " + new Date());
            
                JSONObject datajobj = new JSONObject();
            if (doAction.compareToIgnoreCase("getMapCSV") == 0) {
                datajobj = importHandler.getMappingCSVHeader(request);
                JSONArray jSONArray = datajobj.getJSONArray("Header");
                validateHeadersSequenceFormat(jSONArray,requestparams);
                
                jobj = importSequenceFormat(request, datajobj);
                issuccess = true;
            }
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();

            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException e) {
                Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }

            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
      public void validateHeadersSequenceFormat(JSONArray validateJArray,HashMap<String,Object>requestparams) throws AccountingException, ServiceException {
          Locale locale = null;
          if (requestparams.containsKey("locale")) {
              locale = (Locale) requestparams.get("locale");
          }
        try {

            List<String> list = new ArrayList<String>();
            list.add("Name");
            list.add("Prefix");
            list.add("Suffix");
            list.add("Number of digit");
            list.add("Start from");
            list.add("Show leading zero");
            list.add("Module name");
            list.add("Is Default format");
        
            List<String> fileHeaderList = new ArrayList<String>();

            for (int i = 0; i < validateJArray.length(); i++) {
                String header = validateJArray.getJSONObject(i).getString("header").trim();
                fileHeaderList.add(header);
            }

            for (String manadatoryField : list) {
                if (!fileHeaderList.contains(manadatoryField)) {
                    throw new AccountingException(manadatoryField + " "+messageSource.getMessage("acc.field.columnisnotavailabeinfile", null, locale));
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public JSONObject importSequenceFormat(HttpServletRequest request, JSONObject jobj) throws AccountingException, IOException, SessionExpiredException, JSONException {
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
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String userId = sessionHandlerImpl.getUserid(request);
        String fileName = jobj.getString("name");

        JSONObject returnObj = new JSONObject();

        try {
            fileInputStream = new FileInputStream(jobj.getString("FilePath"));
            br = new BufferedReader(new InputStreamReader(fileInputStream));
            String record = "";
            int cnt = 0;
            
            StringBuilder failedRecords = new StringBuilder();
            
            JSONArray jSONArray = jobj.getJSONArray("Header");
            List headArrayList = new ArrayList();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                headArrayList.add(jSONObject.get("header"));
                }

            failedRecords.append(createCSVrecord(headArrayList.toArray()) + "\"Error Message\"");
            
            while ((record = br.readLine()) != null) {
              if (cnt != 0) {
                    String[] recarr = record.split(",");
                    try {
                                         
                           String Name = recarr[0].trim();
                            if (!StringUtil.isNullOrEmpty(Name)) {
                                Name = Name.replaceAll("\"", "");
                                Name=Name.trim();
                            } else {
                                throw new AccountingException("Name of Sequence Format is not Available");
                            }
                            String Prefix = recarr[1].trim();
                            if (!StringUtil.isNullOrEmpty(Prefix)) {
                                Prefix = Prefix.replaceAll("\"", "");
                                Prefix=Prefix.trim();
                            } 
                            String Suffix = recarr[2].trim();
                            if (!StringUtil.isNullOrEmpty(Suffix)) {
                                Suffix = Suffix.replaceAll("\"", "");
                                Suffix=Suffix.trim();
                            }
                            String NumberOfDigits = recarr[3].trim();
                            if (!StringUtil.isNullOrEmpty(NumberOfDigits)) {
                                NumberOfDigits = NumberOfDigits.replaceAll("\"", "");
                                NumberOfDigits=NumberOfDigits.trim();
                            } else {
                                throw new AccountingException("Number of Digits is not Available");
                            }
                            String StartsFrom = recarr[4].trim();
                            if (!StringUtil.isNullOrEmpty(StartsFrom)) {
                                StartsFrom = StartsFrom.replaceAll("\"", "");
                                StartsFrom=StartsFrom.trim();
                            } else {
                                throw new AccountingException("Starts From is not Available");
                            }
                            String ShowLeadingZeros = recarr[5].trim();
                            if (!StringUtil.isNullOrEmpty(ShowLeadingZeros)) {
                                ShowLeadingZeros = ShowLeadingZeros.replaceAll("\"", "");
                                ShowLeadingZeros = ShowLeadingZeros.trim();
                            } else {
                                throw new AccountingException("Show Leading Zeros is not Available");
                            }
                            String moduleName = recarr[6].trim();
                            if (!StringUtil.isNullOrEmpty(moduleName)) {
                                moduleName = moduleName.replaceAll("\"", "");
                                moduleName = moduleName.trim();
                            } else {
                                throw new AccountingException("Module Name  is not Available");
                            }
                            String  IsDefaultformat = recarr[7].trim();
                            if (!StringUtil.isNullOrEmpty(IsDefaultformat)) {
                                IsDefaultformat = IsDefaultformat.replaceAll("\"", "");
                                IsDefaultformat=IsDefaultformat.trim();
                            } else {
                                throw new AccountingException("Show Leading Zeros is not Available");
                            }
                            String  IsdatebeforePrefix = "";
//                            String  IsdatebeforePrefix = recarr[8].trim();
//                            if (!StringUtil.isNullOrEmpty(IsdatebeforePrefix)) {
//                                IsdatebeforePrefix = IsdatebeforePrefix.replaceAll("\"", "");
//                                IsdatebeforePrefix=IsdatebeforePrefix.trim();
//                            } 
                        int columnMaxSize = 50;
                        if (!moduleName.equals("Cheque Number")) {
                            HashMap<String, Object> requestParams = new HashMap<String, Object>();
                            requestParams.put("name", Name);
                            requestParams.put("prefix", Prefix);
                            requestParams.put("suffix", Suffix);
                            requestParams.put("numberofdigit", Integer.parseInt(NumberOfDigits));
                            requestParams.put("startfrom", Integer.parseInt(StartsFrom));
                            requestParams.put("showleadingzero", "Yes".equalsIgnoreCase(ShowLeadingZeros) ? true : false);
                            requestParams.put("isshowdateinprefix", "Yes".equalsIgnoreCase(IsdatebeforePrefix) ? true : false);
                            requestParams.put("companyid", companyid);
                            boolean isColumnMaxSizeMsg = false;
                            if (Name.length() > columnMaxSize) {
                                isColumnMaxSizeMsg = true;
                            }
                            JSONObject ModuleJSON = getModuleIDandModuleName(moduleName);
                            requestParams.put("moduleid", ModuleJSON.getInt("moduleid"));
                            requestParams.put("modulename", ModuleJSON.getString("modulename"));
                            if (!isColumnMaxSizeMsg) {
                                KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(requestParams);
                                if (kwlObj.getRecordTotalCount() == 0) {
                                    SequenceFormat seqFormat = accCompanyPreferencesObj.saveSequenceFormat(requestParams);
                                    issuccess = true;
                                } else {
                                    throw new AccountingException("This sequence format is already available in system");
                                }
                            }

                            if (isColumnMaxSizeMsg) {
                                String msgexp = messageSource.getMessage("acc.sequence.format.maxSizeMsg", null, RequestContextUtils.getLocale(request));
                                throw new AccountingException(msgexp);
                            }
                        } else {
                             String accountCode=recarr[9].trim();
                             if (!StringUtil.isNullOrEmpty(accountCode)) {
                                accountCode = accountCode.replaceAll("\"", "");
                                accountCode=accountCode.trim();
                            } 
                            String bankName = recarr[10].trim();
                            if (!StringUtil.isNullOrEmpty(bankName)) {
                                bankName = bankName.replaceAll("\"", "");
                                bankName = bankName.trim();
                            }
                            HashMap<String, Object> requestParams = new HashMap<String, Object>();
                            requestParams.put("numberofdigit", Integer.parseInt(NumberOfDigits));
                            requestParams.put("startfrom", Integer.parseInt(StartsFrom));
                            requestParams.put("showleadingzero", "Yes".equalsIgnoreCase(ShowLeadingZeros) ? true : false);
                            requestParams.put("companyid", companyid);
                            requestParams.put("accountcode", accountCode);
                            requestParams.put("bankname", bankName);
                             boolean isColumnMaxSizeMsg = false;
                            if (Name.length() > columnMaxSize) {
                                isColumnMaxSizeMsg = true;
                            }
                             if (!isColumnMaxSizeMsg) {
                                KwlReturnObject kwlObj1 = accCompanyPreferencesObj.getAccountList(requestParams);
                                if (kwlObj1.getRecordTotalCount() != 0) {
                                 List<Account> accountList = kwlObj1.getEntityList();
                                String id = accountList.get(0).getID();
                                requestParams.put("bankAccountId",id);
                                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getChequeSequenceFormatList(requestParams);
//                                    List<ChequeSequenceFormat> chequeSequenceFormatList = kwlObj.getEntityList();
//                                    String id1 = chequeSequenceFormatList.get(0).getBankAccount().getID();
                                    if (kwlObj.getRecordTotalCount() == 0) {
                                            ChequeSequenceFormat seqFormat = accCompanyPreferencesObj.saveChequeSequenceFormat(requestParams);
                                            issuccess = true;

                                    } else {
                                        throw new AccountingException("This sequence format is already available in system");
                                    }
                                } else {
                                    throw new AccountingException("This account is not  available in this company");
                                }
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
                cnt++;
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

            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            fileInputStream.close();
            br.close();

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
                logDataMap.put("Module",Constants.Account_Preferences_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);
                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }
   
     public String createCSVrecord(Object[] listArray) {
        String rec = "";
        for (int i = 0; i < listArray.length; i++) {    //Discard columns id at index 0 and isvalid,invalidColumns, validationlog at last 3 indexes.
           rec += "\"" + (listArray[i] == null ? "" : listArray[i].toString().replaceAll("\"", "")) + "\",";
        }
        return rec;
    }
    public static String getActualFileName(String storageName) {
        String ext = storageName.substring(storageName.lastIndexOf("."));
        String actualName = storageName.substring(0, storageName.lastIndexOf("_"));
        actualName = actualName + ext;
        return actualName;
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
   public HashMap<String,Object> getCustomzeHeader(List<CustomizeReportHeader> list) {
        HashMap<String,Object> customizeHeaderMap=new HashMap<String, Object>();
        try {
             if(list != null && !list.isEmpty()){
                  for (CustomizeReportHeader customizeReportHeader : list) {
                      JSONObject jobj=new JSONObject();
                      jobj.put("dataIndex",customizeReportHeader.getDataIndex());
                      jobj.put("reportid",customizeReportHeader.getReportId());
                      jobj.put("isLineField",customizeReportHeader.isLineField());
                      customizeHeaderMap.put(customizeReportHeader.getDataHeader()+customizeReportHeader.getModuleId()+customizeReportHeader.isFormField(),jobj);
                  }
             }
        } catch (Exception ex) {
            System.out.println("\nEMPTY LIST" + ex);
        }
        return customizeHeaderMap;
    }
   public HashMap<String,JSONObject> getSaveCompanyPrefmap(List<ExportCompanypref> list) {
        HashMap<String,JSONObject> ExportCompanyprefMap=new HashMap<String, JSONObject>();
        try {
             if(list != null && !list.isEmpty()){
                  for (ExportCompanypref exportCompanypref : list) {
                      JSONObject companyPrefFieldsDetails=new JSONObject();
                      companyPrefFieldsDetails.put("fullclassname",exportCompanypref.getFullclassname());
                      companyPrefFieldsDetails.put("validatetype",exportCompanypref.getValidatetype());
                      companyPrefFieldsDetails.put("isaccountfield",exportCompanypref.isAccountfield());
                      companyPrefFieldsDetails.put("importclassgetter",exportCompanypref.getImportclassgetter());
                      companyPrefFieldsDetails.put("namepasstosavefunction",exportCompanypref.getNamepasstosavefunction());
                      String displayName=exportCompanypref.getDisplayname().trim();
                      ExportCompanyprefMap.put(displayName,companyPrefFieldsDetails);
                  }
             }
        } catch (Exception ex) {
            System.out.println("\nEMPTY LIST" + ex);
        }
        return ExportCompanyprefMap;
    }
     public HashMap<String,String> getGroupIDbyGroupName(List<Group> list) {
        HashMap<String,String> ExportGroupMap=new HashMap<String, String>();
        try {
             if(list != null && !list.isEmpty()){
                 Iterator ite=list.iterator();
                  while(ite.hasNext()) {
                      Object[] row = (Object[]) ite.next();
                      Group group = (Group) row[0];
                      ExportGroupMap.put(group.getName(),group.getID());
                  }
             }
        } catch (Exception ex) {
            System.out.println("\nEMPTY LIST" + ex);
        }
        return ExportGroupMap;
    }
    public HashMap<String,String> getAccountsIDByname(List<Account> list) {
        HashMap<String,String> accountMap=new HashMap<String, String>();
        try {
             if(list != null && !list.isEmpty()){
                  for (Account account : list) {
                      accountMap.put(account.getName(),account.getID());
                  }
             }
        } catch (Exception ex) {
            System.out.println("\nEMPTY LIST" + ex);
        }
        return accountMap;
    }
   public HashMap<String,Object> getUserIDBYName(List<User> list) {
        HashMap<String,Object> customizeHeaderMap=new HashMap<String, Object>();
        try {
             if(list != null && !list.isEmpty()){
                  for (User user : list) {
                     customizeHeaderMap.put(user.getFirstName()+" "+user.getLastName(),user.getUserID());
                  }
             }
        } catch (Exception ex) {
            System.out.println("\nEMPTY LIST" + ex);
        }
        return customizeHeaderMap;
    }
   public ModelAndView ImporthideshowTransactionalFields(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        HashMap<String, Object> requestParams = new HashMap<>();
        requestParams.put("locale", RequestContextUtils.getLocale(request));
        try {
            String doAction = request.getParameter("do");
            System.out.println("A(( " + doAction + " start : " + new Date());
            
                JSONObject datajobj = new JSONObject();
            if (doAction.compareToIgnoreCase("getMapCSV") == 0) {
                datajobj = importHandler.getMappingCSVHeader(request);
                JSONArray jSONArray = datajobj.getJSONArray("Header");
                ValidateHeadersTransactionalFields(jSONArray,requestParams);
                
                jobj = ImporthideshowTransactionalFields(request, datajobj);
                issuccess = true;
            }
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();

            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException e) {
                Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }

            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
      public void ValidateHeadersTransactionalFields(JSONArray validateJArray,HashMap<String,Object>requestParams) throws AccountingException, ServiceException {
          Locale locale = null;
          if (requestParams.containsKey("locale")) {
              locale = (Locale) requestParams.get("locale");
          }
        try {

            List<String> list = new ArrayList<String>();
            list.add("Header");
            list.add("Module Name");
            list.add("User");
            list.add("Is Hidden");
            list.add("Is Form Field");
            list.add("System Mandatory");
            list.add("Is Mandatory");
            list.add("Read Only");

            List<String> fileHeaderList = new ArrayList<String>();

            for (int i = 0; i < validateJArray.length(); i++) {
                String header = validateJArray.getJSONObject(i).getString("header").trim();
                fileHeaderList.add(header);
            }

            for (String manadatoryField : list) {
                if (!fileHeaderList.contains(manadatoryField)) {
                    throw new AccountingException(manadatoryField + " "+messageSource.getMessage("acc.field.columnisnotavailabeinfile", null, locale));
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public JSONObject ImporthideshowTransactionalFields(HttpServletRequest request, JSONObject jobj) throws AccountingException, IOException, SessionExpiredException, JSONException {
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
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String userId = sessionHandlerImpl.getUserid(request);
        String fileName = jobj.getString("name");

        JSONObject returnObj = new JSONObject();

        try {
            fileInputStream = new FileInputStream(jobj.getString("FilePath"));
            br = new BufferedReader(new InputStreamReader(fileInputStream));
            String record = "";
            int cnt = 0;

            StringBuilder failedRecords = new StringBuilder();

            JSONArray jSONArray = jobj.getJSONArray("Header");
            List headArrayList = new ArrayList();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                headArrayList.add(jSONObject.get("header"));
            }

            failedRecords.append(createCSVrecord(headArrayList.toArray()) + "\"Error Message\"");
            HashMap<String,Object> requestCustomize=new HashMap<String,Object>();
            requestCustomize.put("importHideShow", true);
            KwlReturnObject customizeReportHeadersResult = accAccountDAOobj.getCustomizeReportHeader(requestCustomize);
            HashMap<String,Object> CustomizeHeaderMap=getCustomzeHeader(customizeReportHeadersResult.getEntityList());
            requestCustomize.clear();
            requestCustomize.put("companyid",companyid);
            KwlReturnObject userResult = accAccountDAOobj.getUsersByCompanyid(requestCustomize);
            HashMap<String,Object> userMap=getUserIDBYName(userResult.getEntityList());
            while ((record = br.readLine()) != null) {
                if (cnt != 0) {
                    String[] recarr = record.split(",");
                    try {

                        String Header = recarr[0].trim();
                        if (!StringUtil.isNullOrEmpty(Header)) {
                            Header = Header.replaceAll("\"", "");
                                Header=Header.trim();
                        } else {
                            throw new AccountingException("Header is not Available");
                        }
                        String moduleName = recarr[1].trim();
                        if (!StringUtil.isNullOrEmpty(moduleName)) {
                            moduleName = moduleName.replaceAll("\"", "");
                            moduleName = moduleName.trim();
                        } else {
                            throw new AccountingException("Module Name  is not Available");
                        }
                        String ishidden = recarr[3].trim();
                        if (!StringUtil.isNullOrEmpty(ishidden)) {
                            ishidden = ishidden.replaceAll("\"", "");
                            ishidden = ishidden.trim();
//                                if(ishidden.equalsIgnoreCase("No")){ // there is no need to check whether field is hidden or not
//                                    continue;
//                                }
                        }
                        String IsManadatory = recarr[5].trim(); // this is System Mandatory field in UI
                        if (!StringUtil.isNullOrEmpty(IsManadatory)) {
                            IsManadatory = IsManadatory.replaceAll("\"", "");
                            IsManadatory = IsManadatory.trim();
                                if(IsManadatory.equalsIgnoreCase("Yes") && ishidden.equalsIgnoreCase("Yes")){
                                throw new AccountingException("Cannot Hide Manadatory Fields");
                            }
                        }
//                            String User = recarr[2].trim(); // there is no need to check whetther user is present or not
//                            if (!StringUtil.isNullOrEmpty(User)) {
//                                User = User.replaceAll("\"", "");
//                                User = User.trim();
//                            } else {
//                                throw new AccountingException("User Name is not Available");
//                            }
                        String IsFormField = recarr[4].trim();
                        if (!StringUtil.isNullOrEmpty(IsFormField)) {
                            IsFormField = IsFormField.replaceAll("\"", "");
                            IsFormField = IsFormField.trim();
                        } else {
                            throw new AccountingException("Is Form Field is not Available");
                        }
                        String IsUserMandatory = recarr[6].trim(); // this is Is Mandatory field in UI
                        if (!StringUtil.isNullOrEmpty(IsUserMandatory)) {
                            IsUserMandatory = IsUserMandatory.replaceAll("\"", "");
                            IsUserMandatory = IsUserMandatory.trim();
                        } else {
                            throw new AccountingException("Is Form Field is not Available");
                        }
                        String IsReadOnly = recarr[7].trim();
                        if (!StringUtil.isNullOrEmpty(IsReadOnly)) {
                            IsReadOnly = IsReadOnly.replaceAll("\"", "");
                            IsReadOnly = IsReadOnly.trim();
                        } else {
                            throw new AccountingException("Is Form Field is not Available");
                        }
                        HashMap<String, Object> requestParams = new HashMap<String, Object>();
                        JSONObject dataJson = new JSONObject();
                        dataJson.put("isManadatoryField", "Yes".equalsIgnoreCase(IsManadatory) ? true : false);
                        dataJson.put("fieldname", Header);
                        dataJson.put("hidecol", "Yes".equalsIgnoreCase(ishidden) ? true : false);
                        dataJson.put("isreadonlycol", "Yes".equalsIgnoreCase(IsReadOnly) ? true : false);
                        dataJson.put("isUserManadatoryField", "Yes".equalsIgnoreCase(IsUserMandatory) ? true : false);

//                        if(userMap.containsKey(User)){
//                             requestParams.put("userId",  userMap.get(User));
//                        }else{
//                             throw new AccountingException("Invalid User Name");
//                        }
                        JSONObject moduleidJSON = getModuleIDandModuleName(moduleName);
                        int moduleid = 0;
                        try {
                            moduleid = moduleidJSON.getInt("moduleid");
                        } catch (Exception ex) {
                            throw new AccountingException("Invalid Module Name");
                        }
                        boolean isformfield = "Yes".equalsIgnoreCase(IsFormField) ? true : false;
                        String checkDataHeader = Header + moduleid + isformfield;
                        if (CustomizeHeaderMap.containsKey(checkDataHeader)) {
                            JSONObject customizeHeaderJson = (JSONObject) CustomizeHeaderMap.get(checkDataHeader);
                            dataJson.put("fieldDataIndex", customizeHeaderJson.get("dataIndex"));
                            requestParams.put("isLineField", customizeHeaderJson.get("isLineField"));
                            requestParams.put("reportId", customizeHeaderJson.get("reportid"));
                            if (customizeHeaderJson.get("isLineField").equals(true)) {
                                if ("Yes".equalsIgnoreCase(IsUserMandatory)) {
                                    throw new AccountingException("you cannot do mandatory as it is Line Field");
                                } else {
                                    if ("Yes".equalsIgnoreCase(IsManadatory) && ("Yes".equalsIgnoreCase(ishidden) || "Yes".equalsIgnoreCase(IsReadOnly))) {
                                        
                                        throw new AccountingException("you cannot do any field Mandatory as it is  System Mandatory Line Field");
                                    }
                                }

                            } else {
                                if ("Yes".equalsIgnoreCase(IsManadatory)) {
                                    throw new AccountingException("you cannot do any field Mandatory as it is System Mandatory Default Field");
                                }
                            }
                        } else {
                            dataJson.put("fieldDataIndex", Header);
                            requestParams.put("reportId", 1);
                        }
                        JSONArray jsonArr = new JSONArray();
                        jsonArr.put(dataJson);
                        requestParams.put("moduleId", moduleid);
                        requestParams.put("isFormField", "Yes".equalsIgnoreCase(IsFormField) ? true : false);
                        requestParams.put("companyId", companyid);
                        requestParams.put("userId", userId);
                        requestParams.put("data", jsonArr);
                        requestParams.put("callfromImport", true);
                        KwlReturnObject resSave = accAccountDAOobj.saveCustomizedReportFields(requestParams);
                 
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
                cnt++;
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
                msg = messageSource.getMessage("acc.field.Imported", null, RequestContextUtils.getLocale(request))+ " " + success + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (success > 1 ? "s" : "") + " "+messageSource.getMessage("acc.field.successfully.", null, RequestContextUtils.getLocale(request));
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

            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            fileInputStream.close();
            br.close();

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
                logDataMap.put("Module",Constants.Account_Preferences_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);
                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }
   public ModelAndView ImportAccountPrefSettings(HttpServletRequest request, HttpServletResponse response) throws ServiceException {
        JSONObject jobj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        HashMap<String,Object> requestParams = new HashMap<>();
        requestParams.put("locale", RequestContextUtils.getLocale(request));
        try {
            String doAction = request.getParameter("do");
            System.out.println("A(( " + doAction + " start : " + new Date());
            
                JSONObject datajobj = new JSONObject();
                datajobj.put("locale", RequestContextUtils.getLocale(request));
                if (doAction.compareToIgnoreCase("getMapCSV") == 0) {
                    datajobj = importHandler.getMappingCSVHeader(request);
                    JSONArray jSONArray = datajobj.getJSONArray("Header");
                    ValidateHeadersAccountPrefSettings(jSONArray,requestParams);

                    jobj = ImportAccountPrefSettings(request, datajobj);
                    issuccess = true;
               }
        } catch (Exception ex) {
            issuccess = false;
            msg = "" + ex.getMessage();

            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException e) {
                Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, e.getMessage(), e);
            }

            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        return new ModelAndView("jsonView_ex", "model", jobj.toString());
    }
      public void ValidateHeadersAccountPrefSettings(JSONArray validateJArray,HashMap<String,Object>requestParams) throws AccountingException, ServiceException {
          Locale locale = null;
          if (requestParams.containsKey("locale")) {
              locale = (Locale) requestParams.get("locale");
          }
        try {

            List<String> list = new ArrayList<String>();
            list.add("Setting Name");
            list.add("Setting Value");
            list.add("Customer");
            list.add("Account Code");

            List<String> fileHeaderList = new ArrayList<String>();

            for (int i = 0; i < validateJArray.length(); i++) {
                String header = validateJArray.getJSONObject(i).getString("header").trim();
                fileHeaderList.add(header);
            }

            for (String manadatoryField : list) {
                if (!fileHeaderList.contains(manadatoryField)) {
                    throw new AccountingException(manadatoryField+"" + " "+messageSource.getMessage("acc.field.columnisnotavailabeinfile", null, locale));
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public JSONObject ImportAccountPrefSettings(HttpServletRequest request, JSONObject jobj) throws AccountingException, IOException, SessionExpiredException, JSONException {
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
        String companyid = sessionHandlerImpl.getCompanyid(request);
        String userId = sessionHandlerImpl.getUserid(request);
        String fileName = jobj.getString("name");

        JSONObject returnObj = new JSONObject();

        try {
            String dateFormat = null, dateFormatId = request.getParameter("dateFormat");
            if (!StringUtil.isNullOrEmpty(dateFormatId)) {

                KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(KWLDateFormat.class.getName(), dateFormatId);
                KWLDateFormat kdf = (KWLDateFormat) kdfObj.getEntityList().get(0);

                dateFormat = kdf != null ? kdf.getJavaForm() : null;
            }
            DateFormat df = new SimpleDateFormat(dateFormat);

            fileInputStream = new FileInputStream(jobj.getString("FilePath"));
            br = new BufferedReader(new InputStreamReader(fileInputStream));
            String record = "";
            int cnt = 0;

            StringBuilder failedRecords = new StringBuilder();

            JSONArray jSONArray = jobj.getJSONArray("Header");
            List headArrayList = new ArrayList();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                headArrayList.add(jSONObject.get("header"));
            }

            failedRecords.append(createCSVrecord(headArrayList.toArray()) + "\"Error Message\"");
            HashMap<String, Object> getacconts = accAccountHandler.getRequestMap(request);
            getacconts.put("ignorecustomers", request.getParameter("ignorecustomers"));
            getacconts.put("ignorevendors", request.getParameter("ignorevendors"));
            getacconts.put("ignoreCashAccounts", request.getParameter("ignoreCashAccounts"));
            getacconts.put("ignoreBankAccounts", request.getParameter("ignoreBankAccounts"));
            getacconts.put("ignoreGSTAccounts", request.getParameter("ignoreGSTAccounts"));
            KwlReturnObject accountresult = accAccountDAOobj.getAccountsForCombo(getacconts);
            HashMap<String, String> accontMap = getAccountsIDByname(accountresult.getEntityList());
            KwlReturnObject exportresult = accCompanyPreferencesObj.getCompanyPreferencesFieldForExport();
            HashMap<String, JSONObject> ExportMap = getSaveCompanyPrefmap(exportresult.getEntityList());
            HashMap<String, Object> getgroups = AccountingManager.getGlobalParams(request);
            KwlReturnObject groupresult = accAccountDAOobj.getGroups(getgroups);
            HashMap<String, String> groupMap = getGroupIDbyGroupName(groupresult.getEntityList());
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            boolean isAdvanceSalesAccount = false;
            boolean isSalesAccountPresent = false;
            boolean isAdvanceSalesAccountPresent = false;
            boolean isRevenueRecognitionAccountPresent = false;
            boolean isIgnoreall = false;
            int countForGst = 0;
            boolean enableGstFlag = true;
            KwlReturnObject company = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company companyList=(Company)company.getEntityList().get(0);
            while ((record = br.readLine()) != null) {
                if (cnt != 0) {
                    String[] recarr = record.split(",");
                    try {

                        String settingname = recarr[0].trim();
                        if (!StringUtil.isNullOrEmpty(settingname)) {
                            settingname = settingname.replaceAll("\"", "");
                            settingname = settingname.trim();
                        } else {
                            throw new AccountingException("Setting Name not Available");
                        }
                        String settingvalue = recarr[1].trim();
                        if (!StringUtil.isNullOrEmpty(settingvalue)) {
                            settingvalue = settingvalue.replaceAll("\"", "");
                            settingvalue = settingvalue.trim();
                        }
                        String customer = recarr[2].trim();
                        if (!StringUtil.isNullOrEmpty(customer)) {
                            customer = customer.replaceAll("\"", "");
                            customer = customer.trim();
                        }
                        String accountCode = recarr[3].trim();
                        if (!StringUtil.isNullOrEmpty(accountCode)) {
                            accountCode = accountCode.replaceAll("\"", "");
                            accountCode = accountCode.trim();
                        }
                        if (settingname.equalsIgnoreCase("Asset settings -Beginning of the Year of Aquisition (Yearwise)") || settingname.equalsIgnoreCase("Asset settings -Beginning of the Date of Aquisition (Monthwise)") || settingname.equalsIgnoreCase("Asset settings -Beginning of the Month of Aquisition (Monthwise)")) {
                            settingname = "Asset settings";
                        }
                        if (settingname.equalsIgnoreCase("UOM Settings-UOM Schema") || settingname.equalsIgnoreCase("UOM Settings-Packaging UOM")) {
                            settingname = "UOM Settings-";
                        }
                        if (settingname.equalsIgnoreCase("Product Selection setting-Show all Products") || settingname.equalsIgnoreCase("Product Selection setting-Show Products on type ahead") || settingname.equalsIgnoreCase("Product Selection setting-Product Id as free text")) {
                            settingname = "Product Selection setting";
                        }
                        if (settingname.equalsIgnoreCase("Generate Barcode Setting-Barcode Type")) {
                            if (settingvalue.equalsIgnoreCase("Code 128")) {
                                settingvalue = "CODE128";
                            } else if (settingvalue.equalsIgnoreCase("Code 39")) {
                                settingvalue = "CODE39";
                            } else {
                                settingvalue = "DATAMATRICS";
                            }
                        }
                        if (settingname.equalsIgnoreCase("Generate Barcode Setting-Print Price Type")) {
                            if (settingvalue.equalsIgnoreCase("Downward")) {
                                settingvalue = "90";
                            } else {
                                settingvalue = "-90";
                            }
                        }
                        if (settingname.equalsIgnoreCase("Default Mail Sender Settings-User Email") || settingname.equalsIgnoreCase("Default Mail Sender Settings-Company Email")) {
                            settingname = "Default Mail Sender Settings";
                        }
                        if (settingname.equalsIgnoreCase("Consignment Stock Settings-Default Customer Warehouse")) {
//                            String customer = recarr[2].trim();
                            HashMap<String, Object> requestParamsForInventory = new HashMap<String, Object>();
                            requestParamsForInventory.put("wareHouseName", settingvalue);
                            //requestParamsForInventory.put("customer", customer);
                            requestParamsForInventory.put("companyid", companyid);
                            if (!StringUtil.isNullOrEmpty(settingvalue)) {
                                KwlReturnObject inventorytresult = accAccountDAOobj.getWarehouseIDByName(requestParamsForInventory);
                                if (!inventorytresult.getEntityList().isEmpty()) {
                                    InventoryWarehouse invresult = (InventoryWarehouse) inventorytresult.getEntityList().get(0);
                                    settingvalue = invresult.getId();
                                } else {
                                    throw new AccountingException("Warehouse is not present in this System");
                                }
                            }
                        }
                        if (settingname.equalsIgnoreCase("Inventory Settings-Activate Inventory Integration") && (settingvalue.equalsIgnoreCase("Yes") || settingvalue.equalsIgnoreCase("No"))) {
                          KwlReturnObject storeMasterResult = accAccountDAOobj.getStoreMasterData(companyid); //checking is row/rack/bin present for login company
                            if (!storeMasterResult.getEntityList().isEmpty()) {
                                throw new AccountingException("As Row/Rack/Bin is present in this system So you cannot Import Activate Inventory Integration");
                            } else {
                                KwlReturnObject storeResult = accAccountDAOobj.getStoreForIsDefaultNot(companyid); //getting Warehouse except default
                                if (!storeResult.getEntityList().isEmpty()) {
                                    throw new AccountingException("As Warehouse is present in this system So you cannot Import Activate Inventory Integration");
                                } else {
                                    KwlReturnObject locationResult = accAccountDAOobj.getLocationForIsDefaultNot(companyid); //getting Location except default
                                    if (!locationResult.getEntityList().isEmpty()) {
                                        throw new AccountingException("As Location is present in this system So you cannot Import Activate Inventory Integration");
                                    }
                                }
                            }
                        }
                        if (settingname.equalsIgnoreCase("Consignment Stock Settings-Store for Stock Repair") || settingname.equalsIgnoreCase("Consignment Stock Settings-Store for QA Inspection")) {
                            if (!StringUtil.isNullOrEmpty(settingvalue)) {
                                String[] storeArr = settingvalue.split("-");
                                HashMap<String, Object> requestParamsForStore = new HashMap<String, Object>();
                                requestParamsForStore.put("storeName", storeArr[0]);
                                requestParamsForStore.put("companyid", companyid);
                                KwlReturnObject storeresult = accAccountDAOobj.getStoreIDByName(requestParamsForStore);
                                if (!storeresult.getEntityList().isEmpty()) {
                                    com.krawler.inventory.model.store.Store storeList = (com.krawler.inventory.model.store.Store) storeresult.getEntityList().get(0);
                                    settingvalue = storeList.getId();
                                } else {
                                    throw new AccountingException("Store is not present in this System");
                                }

                            }
                        }
                        if (settingname.equalsIgnoreCase("Integration Settings-POS Walk-in Customer")) {
                            if (!StringUtil.isNullOrEmpty(customer)) {
                                HashMap<String, Object> requestParamsForCustomer = new HashMap<String, Object>();
                                requestParamsForCustomer.put("customercode", customer);
                                requestParamsForCustomer.put("companyid", companyid);
                                KwlReturnObject customerresult = accAccountDAOobj.getCustomerNameByCustomerCode(requestParamsForCustomer);
                                if (!customerresult.getEntityList().isEmpty()) {
                                    Customer customerobj = (Customer) customerresult.getEntityList().get(0);
                                    settingvalue = customerobj.getID();
                                } else {
                                    throw new AccountingException("Customer is not present in this System");
                                }
                            }
                        }
                        if (settingname.equalsIgnoreCase("Integration Settings-Activate CRM Integration") || settingname.equalsIgnoreCase("Integration Settings-Activate LMS Integration")) {
                            JSONObject appdata = new JSONObject();
                            JSONObject appData = new JSONObject();
                            boolean isCRMSubscribed=false;
                            boolean isLMSSubscribed=false;
                            int i=0;
                            //Session session = HibernateUtil.getCurrentSession();
                            String userid = sessionHandlerImpl.getUserid(request);
                            String currencyid = sessionHandlerImpl.getCurrencyID(request);
                            JSONObject resObj = new JSONObject();
//                            String platformURL = this.getServletContext().getInitParameter("platformURL");
                            String platformURL = URLUtil.buildRestURL("platformURL");
                            platformURL = platformURL + "company/partnerlink";                            
                            appData.put("companyid", companyid);
                            appData.put("userid", sessionHandlerImpl.getUserid(request));
                            appData.put("subdomain", URLUtil.getDomainName(request));
                            appData.put("appid", "3");
                            appdata = apiCallHandlerService.restGetMethod(platformURL, appData.toString());
//                            appdata = apiCallHandlerService.callApp(platformURL, appData, companyid, "15");
                            JSONArray jarr = appdata.optJSONArray("subscribedapplist");
                            if (jarr != null && jarr.length() > 0) {
                                for (i = 0; i < jarr.length(); i++) {
                                    if (jarr.getJSONObject(i).getString("appid").equals("2")) {
                                        isCRMSubscribed=true;
                                        request.setAttribute("appid", 2);
//                                        String crmURL = this.getServletContext().getInitParameter("crmURL");
//                                        String action = "219";
                                        String crmURL = URLUtil.buildRestURL(Constants.crmURL);
                                        crmURL = crmURL + "company/companycurrancy";                                        
                                        JSONObject userData = new JSONObject();
                                        userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
                                        userData.put("userid", userid);
                                        userData.put("iscommit", true);
                                        userData.put("companyid", companyid);
                                        resObj = apiCallHandlerService.restGetMethod(crmURL, userData.toString());
//                                        resObj = apiCallHandlerService.callApp(crmURL, userData, companyid, action);
                                        String appCurrencyid = resObj.getString("currencyid");
                                        if(resObj.has("success") && !resObj.getString("success").equals("true") && !appCurrencyid.equalsIgnoreCase(currencyid)){
                                            throw new AccountingException("CRM application has different currency. So you cannot activate CRM integration");  
                                        }
                                    } 

                                    if (jarr.getJSONObject(i).getString("appid").equals("5")) {
                                        isLMSSubscribed=true;
                                        request.setAttribute("appid", 5);
//                                        String lmsURL = this.getServletContext().getInitParameter("lmsURL");
//                                        String action = "38";
                                        String lmsURL = URLUtil.buildRestURL("lmsURL");
                                        lmsURL = lmsURL + "company/currency";                                        
                                        JSONObject userData = new JSONObject();
                                        userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
                                        userData.put("userid", userid);
                                        userData.put("iscommit", true);
                                        userData.put("companyid", companyid);
                                        resObj = apiCallHandlerService.restGetMethod(lmsURL, userData.toString());
//                                        resObj = apiCallHandlerService.callApp(lmsURL, userData, companyid, action);
                                        String appCurrencyid = resObj.getString("currencyid");
                                        if(resObj.has("success") && !resObj.getString("success").equals("true") && !appCurrencyid.equalsIgnoreCase(currencyid)){
                                            throw new AccountingException("LMS application has different currency. So you cannot activate LMS integration");  
                                        }
                                    }
                                }
                                if((i==jarr.length() && !isCRMSubscribed) && !isLMSSubscribed){
                                    throw new AccountingException("CRM/LMS Integeration is not subscribed yet"); 
                                }                               
                            }else{
                               throw new AccountingException("CRM/LMS Integeration is not subscribed yet"); 
                            }
                        }
                        if (settingname.equalsIgnoreCase("Revenue Recognition Account-Advance Sales Account")) {
                            settingname = "Revenue Recognition Account-Sales Account";
                            isAdvanceSalesAccount = true;
                        }
                        if (settingname.equalsIgnoreCase("Minimum Budget Settings-Ignore") || settingname.equalsIgnoreCase("Minimum Budget Settings-Warn") || settingname.equalsIgnoreCase("Minimum Budget Settings-Block")) {
                            settingname = "Minimum Budget Settings";
                        }
                        if (settingname.equalsIgnoreCase("Customer Credit Control-Ignore") || settingname.equalsIgnoreCase("Customer Credit Control-Warn") || settingname.equalsIgnoreCase("Customer Credit Control-Block")) {
                            settingname = "Customer Credit Control";
                        }
                         if (settingname.equalsIgnoreCase("Negative Stock Settings-Ignore") || settingname.equalsIgnoreCase("Negative Stock Settings-Warn") || settingname.equalsIgnoreCase("Negative Stock Settings-Block")) {
                            settingname = "Negative Stock Settings";
                        
                        }
                         if(settingname.equalsIgnoreCase("Negative Stock Setting-Activate Negative Stock For Location Warehouse")){
                              if(settingvalue.equalsIgnoreCase("yes")){
                              KwlReturnObject kdfObj = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
                              CompanyAccountPreferences kdf = (CompanyAccountPreferences) kdfObj.getEntityList().get(0);
                              boolean isBatch=kdf.isIsBatchCompulsory();
                              boolean isSerialNo=kdf.isIsSerialCompulsory(); 
                              if(isSerialNo || isBatch ){
                                 throw new AccountingException("As Batch/Serial No is Activated for this company So you cannot activate Location warehouse setting");  
                              }
                            }   
                         }
                         if (settingname.equalsIgnoreCase("Default Purchase Type Selection Setting-Cash Purchase") || settingname.equalsIgnoreCase("Default Purchase Type Selection Setting-Credit Purchase")) {
                            settingname = "Default Purchase Type Selection Setting";
                        }
                          if (settingname.equalsIgnoreCase("Default Sales Type Selection Setting-Cash Sales") || settingname.equalsIgnoreCase("Default Sales Type Selection Setting-Credit Sales")) {
                            settingname = "Default Sales Type Selection Setting";
                        }
                        if (settingname.equalsIgnoreCase("Accounts With Code-With Code") || settingname.equalsIgnoreCase("Accounts With Code-Without Code")) {
                            settingname = "Accounts With Code";
                        }                       
                        if (settingname.equalsIgnoreCase("Activate budgeting based on Department-Budgeting based on Department") || settingname.equalsIgnoreCase("Activate budgeting based on Department-Budgeting based on Department and Product")) {
                            settingname="Activate budgeting based on Department-Budgeting based on";
                        }
                        if (settingname.equalsIgnoreCase("Activate budgeting based on Department-Budgeting frequency type (Monthly)") || settingname.equalsIgnoreCase("Activate budgeting based on Department-Budgeting frequency type (Bi-Monthly)") || settingname.equalsIgnoreCase("Activate budgeting based on Department-Budgeting frequency type (Quarterly)") || settingname.equalsIgnoreCase("Activate budgeting based on Department-Budgeting frequency type (Half Yearly)") || settingname.equalsIgnoreCase("Activate budgeting based on Department-Budgeting frequency type (Yearly)")){
                            settingname="Activate budgeting based on Department-Budgeting frequency type";
                        }
                        if (settingname.equalsIgnoreCase("Asset settings -Beginning of the Year of Aquisition (Yearwise") || settingname.equalsIgnoreCase("Asset settings -Beginning of the Date of Aquisition (Monthwise)") || settingname.equalsIgnoreCase("Asset settings -Beginning of the Month of Aquisition (Monthwise)")){
                            settingname="Asset settings";
                        }
                        if (companyList.getCountry().getID().equals("137")) {
                            if (settingname.equalsIgnoreCase("Company GST Detail-Enable GST") && settingvalue.equalsIgnoreCase("No")) {
                                enableGstFlag = false;
                            }
                            if (settingname.equalsIgnoreCase("Company GST Detail-Trade Register Number (GST)")) {
                                settingname = "Company GST Detail-GST Number";
                                countForGst++;
                            } else if (settingname.equalsIgnoreCase("Company GST Detail-Company BRN")) {
                                settingname = "Company GST Detail-Company UEN";
                                countForGst++;
                            } else if (settingname.equalsIgnoreCase("Company GST Detail-GAF File Version")) {
                                settingname = "Company GST Detaile-IAF File Version";
                                countForGst++;
                            }
                        }
                        if (settingname.equalsIgnoreCase("Control Account Settings-Select Account For Profit Loss") || settingname.equalsIgnoreCase("Control Account Settings-Select Account For Opening Stock") || settingname.equalsIgnoreCase("Control Account Settings-Select Account For Closing Stock") || settingname.equalsIgnoreCase("Control Account Settings-Select Account For Stock in Hand")) {
                            HashMap<String, Object> requestParamsForAccount = new HashMap<String, Object>();
                            if (accountCode.equalsIgnoreCase("N/A") && !StringUtil.isNullOrEmpty(settingvalue)) {
                                requestParamsForAccount.put("accountCode", "");
                                requestParamsForAccount.put("accountName", settingvalue);
                            } else if (StringUtil.isNullOrEmpty(settingvalue) && !StringUtil.isNullOrEmpty(accountCode)) {
                                requestParamsForAccount.put("accountCode", accountCode);
                                requestParamsForAccount.put("accountName", "");
                            } else {
                                requestParamsForAccount.put("accountCode", accountCode);
                                requestParamsForAccount.put("accountName", settingvalue);
                            }
                            requestParamsForAccount.put("companyid", companyid);
                            KwlReturnObject controlAccountResult = accAccountDAOobj.getAccountIDByCode(requestParamsForAccount);
                            if (!controlAccountResult.getEntityList().isEmpty()) {
                                Account contolAccount = (Account) controlAccountResult.getEntityList().get(0);
                                settingvalue = contolAccount.getID();
                            } else {
                                throw new AccountingException("Account is not present in this System");
                            }

                        }
                        if (ExportMap.containsKey(settingname)) {
                            JSONObject companyPrefFieldsDetails = ExportMap.get(settingname);
                            if ("boolean".equalsIgnoreCase(companyPrefFieldsDetails.getString("validatetype"))) {
                                if ("Yes".equalsIgnoreCase(settingvalue) || "No".equalsIgnoreCase(settingvalue)) {
                                    boolean value = "Yes".equalsIgnoreCase(settingvalue) ? true : false;
                                    requestParams.put(companyPrefFieldsDetails.getString("namepasstosavefunction"), value);
                                } else {
                                    throw new AccountingException("Value Should be 'Yes' or 'No'");
                                }
                            } else if ("int".equalsIgnoreCase(companyPrefFieldsDetails.getString("validatetype"))) {
                                if ((companyPrefFieldsDetails.getString("namepasstosavefunction").equalsIgnoreCase("viewDashboard"))) {
                                    if ("Flow Diagram View".equalsIgnoreCase(settingvalue) || "Widget View".equalsIgnoreCase(settingvalue)) {
                                        requestParams.put(companyPrefFieldsDetails.getString("namepasstosavefunction"), "Flow Diagram View".equalsIgnoreCase(settingvalue) ? "0" : "1");
                                    } else {
                                        throw new AccountingException("Value Should be 'Flow Diagram View' of 'Widget View'");
                                    }
                                }
//                                if (companyPrefFieldsDetails.getString("namepasstosavefunction").equalsIgnoreCase("negativestock") || companyPrefFieldsDetails.getString("namepasstosavefunction").equalsIgnoreCase("custbudgetcontrol") || companyPrefFieldsDetails.getString("namepasstosavefunction").equalsIgnoreCase("custcreditcontrol")) {
//                                    if ("Ignore".equalsIgnoreCase(settingvalue) || "Block".equalsIgnoreCase(settingvalue) || "Warn".equalsIgnoreCase(settingvalue)) {
//                                        requestParams.put(companyPrefFieldsDetails.getString("namepasstosavefunction"), "Ignore".equalsIgnoreCase(settingvalue) ? "0" : "Block".equalsIgnoreCase(settingvalue) == true ? "1" : "2");
//                                    } else {
//                                        throw new AccountingException("Value should be 'Ignore' or 'Block' or 'Warn'");
//                                    }
//                                } 
                               if (settingname.equalsIgnoreCase("Product Selection setting") || settingname.equalsIgnoreCase("Default Mail Sender Settings") || settingname.equalsIgnoreCase("Customer Credit Control") || settingname.equalsIgnoreCase("Negative Stock Settings")) {
                                    requestParams.put(companyPrefFieldsDetails.getString("namepasstosavefunction"), settingvalue);
                                } else {
                                    requestParams.put(companyPrefFieldsDetails.getString("namepasstosavefunction"), Integer.parseInt(settingvalue));
                                }
                            } else if ("date".equalsIgnoreCase(companyPrefFieldsDetails.getString("validatetype"))) {
                                try {
                                    requestParams.put(companyPrefFieldsDetails.getString("namepasstosavefunction"), "".equalsIgnoreCase(settingvalue) ? "" : df.parse(settingvalue));
                                } catch (Exception ex) {
                                    throw new AccountingException("Invalid Date Format");
                                }
                            } else if ("string".equalsIgnoreCase(companyPrefFieldsDetails.getString("validatetype"))) {
                                requestParams.put(companyPrefFieldsDetails.getString("namepasstosavefunction"), settingvalue);
                            } else if ("ref".equalsIgnoreCase(companyPrefFieldsDetails.getString("validatetype"))) {
                                if (companyPrefFieldsDetails.getBoolean("isaccountfield")) {
                                    if (!isAdvanceSalesAccount && (settingname.equalsIgnoreCase("Revenue Recognition Account-Sales Account") || settingname.equalsIgnoreCase("Revenue Recognition Account-Sales Revenue Recognition Account"))) {
                                        getacconts.put("ignorecustomers", true);
                                        getacconts.put("ignorevendors", true);
                                        getacconts.put("ignoreCashAccounts", true);
                                        getacconts.put("ignoreBankAccounts", true);
                                        getacconts.put("ignoreGSTAccounts", true);
                                        getacconts.put("nature", new String[]{"3"});
                                        KwlReturnObject accountresult1 = accAccountDAOobj.getAccountsForCombo(getacconts);
                                        HashMap<String, String> accontMap1 = getAccountsIDByname(accountresult1.getEntityList());
                                        if (accontMap1.containsKey(settingvalue)) {
                                            requestParams.put(companyPrefFieldsDetails.getString("namepasstosavefunction"), accontMap1.get(settingvalue));
                                        } else {
                                            if (settingname.equalsIgnoreCase("Revenue Recognition Account-Sales Account")) {
                                                isSalesAccountPresent = true;
                                            } else {
                                                isRevenueRecognitionAccountPresent = true;
                                            }
                                            requestParams.remove("salesAccount");
                                            requestParams.remove("salesRevenueRecognitionAccount");
                                            requestParams.remove("isDeferredRevenueRecognition");
                                            requestParams.remove("recurringDeferredRevenueRecognition");
                                            throw new AccountingException("As Account is Not Available in system So all settings for Revenue recognition is getting ignored");
                                        }
                                    } else {
                                        if (accontMap.containsKey(settingvalue)) {
                                            requestParams.put(companyPrefFieldsDetails.getString("namepasstosavefunction"), accontMap.get(settingvalue));
                                        } else {
                                            if (isAdvanceSalesAccount) {
                                                isAdvanceSalesAccountPresent = true;
                                                requestParams.remove("salesAccount");
                                                requestParams.remove("salesRevenueRecognitionAccount");
                                                requestParams.remove("isDeferredRevenueRecognition");
                                                requestParams.remove("recurringDeferredRevenueRecognition");
                                                throw new AccountingException("As Account is Not Available in system So all settings for Revenue recognition is getting ignored");
                                            } else {
                                                throw new AccountingException("Account is Not Available in system");
                                            }
                                        }
                                    }


                                } else {
                                    if (groupMap.containsKey(settingvalue)) {
                                        requestParams.put(companyPrefFieldsDetails.getString("namepasstosavefunction"), groupMap.get(settingvalue));
                                    } else {
                                        throw new AccountingException("Group is Not Available in system");
                                    }
                                }
                            } else {
                                throw new AccountingException("Setting Name is not Valid");
                            }
                                if (countForGst==3 && !enableGstFlag) {
                                requestParams.remove("enableGST");
                                requestParams.remove("gstnumber");
                                requestParams.remove("taxNumber");
                                requestParams.remove("companyuen");
                                requestParams.remove("iafversion");
                                recarr[0]="Company GST Detail-Enable GST";
                                recarr[1]="No";
                                recarr[2]="N/A";
                                recarr[2]="N/A";
                                throw new AccountingException("As Enable Gst value is 'No' in Exported file So all settings of company Gst Detail is ignored");
                            }
                        }
                    } catch (Exception ex) {
                        if ((isAdvanceSalesAccountPresent || isSalesAccountPresent || isRevenueRecognitionAccountPresent) && !isIgnoreall) {
                            failed += 4;
                            isIgnoreall = true;
                        }else if(countForGst==3 && !enableGstFlag){
                           failed+=5; 
                        } else {
                            if (!(isAdvanceSalesAccountPresent || isSalesAccountPresent || isRevenueRecognitionAccountPresent)) {
                                failed++;
                            }
                        }
                        String errorMsg = ex.getMessage(), invalidColumns = "";
                        try {
                            JSONObject errorLog = new JSONObject(errorMsg);
                            errorMsg = errorLog.getString("errorMsg");
                            invalidColumns = errorLog.getString("invalidColumns");
                        } catch (JSONException jex) {
                        }
                        failedRecords.append("\n" + createCSVrecord(recarr) + "\"" + errorMsg.replaceAll("\"", "") + "\"");
                    }
                    if ((isAdvanceSalesAccountPresent || isSalesAccountPresent || isRevenueRecognitionAccountPresent) && !isIgnoreall) {
                        total += 4;
                        isIgnoreall = true;
                    } else {
                        if (!(isAdvanceSalesAccountPresent || isSalesAccountPresent || isRevenueRecognitionAccountPresent)) {
                            total++;
                        }
                    }
                }
                cnt++;
            }
            if (isSalesAccountPresent || isAdvanceSalesAccountPresent || isRevenueRecognitionAccountPresent) { //If account is not present in system and in import file radio button present after combo
                requestParams.remove("salesAccount");
                requestParams.remove("salesRevenueRecognitionAccount");
                requestParams.remove("isDeferredRevenueRecognition");
                requestParams.remove("recurringDeferredRevenueRecognition");
            }
            requestParams.put("id", companyid);
            KwlReturnObject companyPrefres = accCompanyPreferencesObj.updatePreferences(requestParams);
            KwlReturnObject resultExtra = accCompanyPreferencesObj.addOrUpdateExtraPreferences(requestParams);
            ExtraCompanyPreferences companyPreferences = accCompanyPreferencesObj.updateExtraCompanyPreferences(requestParams);
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
                msg = messageSource.getMessage("acc.field.Imported", null, RequestContextUtils.getLocale(request))+ " " + success + " "+ messageSource.getMessage("acc.field.record", null, RequestContextUtils.getLocale(request)) + (success > 1 ? "s" : "") + " "+messageSource.getMessage("acc.field.successfully.", null, RequestContextUtils.getLocale(request));
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

            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw new AccountingException("Error While Importing Records.");
        } finally {
            fileInputStream.close();
            br.close();

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
                logDataMap.put("Module", Constants.Account_Preferences_ModuleId);
                logDataMap.put("ImportDate", new Date());
                logDataMap.put("User", userId);
                logDataMap.put("Company", companyid);
                importDao.saveImportLog(logDataMap);
                txnManager.commit(lstatus);
            } catch (Exception ex) {
                txnManager.rollback(lstatus);
                Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }

            try {
                returnObj.put("success", issuccess);
                returnObj.put("msg", msg);
                returnObj.put("totalrecords", total);
                returnObj.put("successrecords", total - failed);
                returnObj.put("failedrecords", failed);
                returnObj.put("filename", getActualFileName(fileName));
            } catch (JSONException ex) {
                Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnObj;
    }
      public static JSONObject getModuleIDandModuleName(String modulename){
          JSONObject jobj=new JSONObject();
          modulename=modulename.trim();
          try{
            if(!StringUtil.isNullOrEmpty(modulename)){
                    if(modulename.equalsIgnoreCase("Invoice/Cash Sales")){
                        jobj.put("moduleid",Constants.Acc_Invoice_ModuleId);
                        jobj.put("modulename","autoinvoice");
                        return jobj;
                    }   
                    if(modulename.equalsIgnoreCase("Cash Sales")){
                        jobj.put("moduleid",Constants.Acc_Cash_Sales_ModuleId);
                        jobj.put("modulename","autocashsales");
                        return jobj;
                    }   
                    if(modulename.equalsIgnoreCase("Cash Purchase")){
                        jobj.put("moduleid",Constants.Acc_Cash_Purchase_ModuleId);
                        jobj.put("modulename","autocashpurchase");
                        return jobj;
                    }   
                    if(modulename.equalsIgnoreCase("Purchase Invoice/Cash Purchase")||modulename.equalsIgnoreCase("Vendor Invoice/Cash Purchase")){
                        jobj.put("moduleid",Constants.Acc_Vendor_Invoice_ModuleId);
                        jobj.put("modulename","autogoodsreceipt");
                        return jobj;
                    }
                    if(modulename.equalsIgnoreCase("Debit Note")){
                        jobj.put("moduleid",Constants.Acc_Debit_Note_ModuleId);
                        jobj.put("modulename","autodebitnote");
                        return jobj;
                    }  
                    
                    if(modulename.equalsIgnoreCase("Credit Note")){
                        jobj.put("moduleid",Constants.Acc_Credit_Note_ModuleId);
                        jobj.put("modulename","autocreditmemo");
                        return jobj;
                    }   
                    if(modulename.equalsIgnoreCase("Make Payment")){
                        jobj.put("moduleid",Constants.Acc_Make_Payment_ModuleId);
                        jobj.put("modulename","autopayment");
                        return jobj;
                    }   
                    if(modulename.equalsIgnoreCase("Receive Payment")){
                        jobj.put("moduleid",Constants.Acc_Receive_Payment_ModuleId);
                        jobj.put("modulename","autoreceipt");
                        return jobj;
                    }
                    if(modulename.equalsIgnoreCase("Journal Entry")){
                        jobj.put("moduleid",Constants.Acc_GENERAL_LEDGER_ModuleId);
                        jobj.put("modulename","autojournalentry");
                        return jobj;
                    }  
                   if(modulename.equalsIgnoreCase("Products & Services")){
                        jobj.put("moduleid",Constants.Acc_Product_Master_ModuleId);
                        jobj.put("modulename","autoproductid");
                        return jobj;
                    }   
                    if(modulename.equalsIgnoreCase("Purchase Order")){
                        jobj.put("moduleid",Constants.Acc_Purchase_Order_ModuleId);
                        jobj.put("modulename","autopo");
                        return jobj;
                    }   
                    if(modulename.equalsIgnoreCase("Sales Order")){
                        jobj.put("moduleid",Constants.Acc_Sales_Order_ModuleId);
                        jobj.put("modulename","autoso");
                        return jobj;
                    }
                    if(modulename.equalsIgnoreCase("Customer Quotation")){
                        jobj.put("moduleid",Constants.Acc_Customer_Quotation_ModuleId);
                        jobj.put("modulename","autoquotation");
                        return jobj;
                    }  

                   if(modulename.equalsIgnoreCase("Vendor Quotation")){
                        jobj.put("moduleid",Constants.Acc_Vendor_Quotation_ModuleId);
                        jobj.put("modulename","autovenquotation");
                        return jobj;
                    }
                    if(modulename.equalsIgnoreCase("Delivery Order")){
                        jobj.put("moduleid",Constants.Acc_Delivery_Order_ModuleId);
                        jobj.put("modulename","autodo");
                        return jobj;
                    }   
                    if(modulename.equalsIgnoreCase( "Goods Receipt Order")){
                        jobj.put("moduleid",Constants.Acc_Goods_Receipt_ModuleId);
                        jobj.put("modulename","autogro");
                        return jobj;
                    }
                    if(modulename.equalsIgnoreCase("Sales Return")){
                        jobj.put("moduleid",Constants.Acc_Sales_Return_ModuleId);
                        jobj.put("modulename","autosr");
                        return jobj;
                    }  
                    if(modulename.equalsIgnoreCase("Purchase Return")){
                        jobj.put("moduleid",Constants.Acc_Purchase_Return_ModuleId);
                        jobj.put("modulename","autopr");
                        return jobj;
                    }   
                     if(modulename.equalsIgnoreCase("Vendor")){
                        jobj.put("moduleid",Constants.Acc_Vendor_ModuleId);
                        jobj.put("modulename","autovendorid");
                        return jobj;
                    }   
                    if(modulename.equalsIgnoreCase( "Customer")){
                        jobj.put("moduleid",Constants.Acc_Customer_ModuleId);
                        jobj.put("modulename","autocustomerid");
                        return jobj;
                    }
                    if(modulename.equalsIgnoreCase("Purchase Requisition")){
                        jobj.put("moduleid",Constants.Acc_Purchase_Requisition_ModuleId);
                        jobj.put("modulename","autorequisition");
                        return jobj;
                    }  
                    if(modulename.equalsIgnoreCase("Request For Quotation")){
                        jobj.put("moduleid",Constants.Acc_RFQ_ModuleId);
                        jobj.put("modulename","autorequestforquotation");
                        return jobj;
                    }  
                    if(modulename.equalsIgnoreCase("GL Accounts")){
                        jobj.put("moduleid",Constants.Account_Statement_ModuleId);
                        jobj.put("modulename","");
                        return jobj;
                    }  
                    if(modulename.equalsIgnoreCase("FA Delivery Order")){
                        jobj.put("moduleid",Constants.Acc_FixedAssets_DeliveryOrder_ModuleId);
                        jobj.put("modulename","autofado");
                        return jobj;
                    }   
                    if(modulename.equalsIgnoreCase( "FA Purchase Invoice")){
                        jobj.put("moduleid",Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId);
                        jobj.put("modulename","autofagoodsreceipt");
                        return jobj;
                    }
                    if(modulename.equalsIgnoreCase("FA Goods Receipt")){
                        jobj.put("moduleid",Constants.Acc_FixedAssets_GoodsReceipt_ModuleId);
                        jobj.put("modulename","autofagro");
                        return jobj;
                    }  
                    if(modulename.equalsIgnoreCase("FA Disposal Invoice")){
                        jobj.put("moduleid",Constants.Acc_FixedAssets_DisposalInvoice_ModuleId);
                        jobj.put("modulename","autofainvoice");
                        return jobj;
                    }  
                    if(modulename.equalsIgnoreCase("FA Assets Group")){
                        jobj.put("moduleid",Constants.Acc_FixedAssets_AssetsGroups_ModuleId);
                        jobj.put("modulename","autofaassetsgroupid");
                        return jobj;
                    } 
                    if(modulename.equalsIgnoreCase("Contract Order")){
                        jobj.put("moduleid",Constants.Acc_Contract_Order_ModuleId);
                        jobj.put("modulename","autocontract");
                        return jobj;
                    } 
                    if (modulename.equalsIgnoreCase("Serial Window")) {
                        jobj.put("moduleid", Constants.SerialWindow_ModuleId);
                        jobj.put("modulename", "");
                        return jobj;
                    }
                    if (modulename.equalsIgnoreCase("Consignment Request")) {
                        jobj.put("moduleid", Constants.Acc_ConsignmentRequest_ModuleId);
                        jobj.put("modulename", "");
                        return jobj;
                    }
                    if (modulename.equalsIgnoreCase("Inventory Window")) {
                        jobj.put("moduleid", Constants.Inventory_ModuleId);
                        jobj.put("modulename", "");
                        return jobj;
                    }
                    if (modulename.equalsIgnoreCase("Lease Order")) {
                      jobj.put("moduleid", Constants.Acc_Lease_Order_ModuleId);
                      jobj.put("modulename", "");
                      return jobj;
                   }
                    if (modulename.equalsIgnoreCase("RG 23D Entry Number")) { //ERP-29471:Update Sample file for Import Sequence format new add module
                      jobj.put("moduleid", Constants.Dealer_Excise_RG23DEntry_No);
                      jobj.put("modulename", "");
                      return jobj;
                   }    
              }
            
            } catch (JSONException ex) {
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
          return jobj;
    }
    public ModelAndView getOpeningBalanceBalanceSheet(HttpServletRequest request,HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        try {
            CompanyAccountPreferences preferences;
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("id", sessionHandlerImpl.getCompanyid(request));
            KwlReturnObject result = accCompanyPreferencesObj.getCompanyPreferences(requestParams);
            preferences = (CompanyAccountPreferences) result.getEntityList().get(0);

            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = null;
            if (!companyResult.getEntityList().isEmpty()) {
                company = (Company) companyResult.getEntityList().get(0);
            }
            JSONArray jArr = new JSONArray(request.getParameter("data"));
            HashMap<String, Double> amountHashMap = new HashMap<String, Double>();
            for (int i = 0 ; i < jArr.length(); i++) {
                JSONObject jSONObject=jArr.getJSONObject(i);
                 if (Boolean.parseBoolean(jSONObject.getString("islock"))) {
                    int year = Integer.parseInt(StringUtil.DecodeText(jSONObject.optString("name")));
                    HashMap<String, Date> startEndDateHashMap = accReportsService.getStartAndEndFinancialDate(preferences, year);
                    DateFormat dateFormat = authHandler.getDateOnlyFormat(request);
                    request.setAttribute("netProfitClosesFlag", true);
                    request.setAttribute("stdate", dateFormat.format(new Date(1970)));
                    request.setAttribute("enddate", dateFormat.format(startEndDateHashMap.get("endDate")));
                    request.setAttribute("mode", 66);
                    request.setAttribute("nondeleted", true);
                    double amount = accReportsService.getOpeningBalanceBalanceSheet(request);
                    jobj.put("data", amount);
                    break;
                } else {
                    jobj.put("data", 0);
                }
            }
                jobj.put("success", true);
        }  catch (ServiceException ex) {
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView getSubscribedAppInformation(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        boolean iscurrencySame = true;
        String msg = "";
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            //Session session = HibernateUtil.getCurrentSession();
            String userid = sessionHandlerImpl.getUserid(request);
            JSONObject resObj = new JSONObject();
            JSONObject userData = new JSONObject();
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", userid);
            userData.put("iscommit", true);
            userData.put("companyid", companyid);

            int appID = Integer.parseInt(request.getParameter("appid"));
            switch (appID) {
                case 2: //For CRM
//                    String crmURL = this.getServletContext().getInitParameter("crmURL");
//                    String action = "219";
                    String crmURL = URLUtil.buildRestURL(Constants.crmURL);
                    crmURL = crmURL + "company/companycurrancy";
                    resObj = apiCallHandlerService.restGetMethod(crmURL, userData.toString());
//                    resObj = apiCallHandlerService.callApp(crmURL, userData, companyid, action);
                    break;
                case 5: //LMS
//                    String lmsURL = this.getServletContext().getInitParameter("lmsURL");
//                    action = "38";
                    String lmsURL = URLUtil.buildRestURL("lmsURL");
                    lmsURL = lmsURL + "company/currency";                    
                    resObj = apiCallHandlerService.restGetMethod(lmsURL, userData.toString());
//                    resObj = apiCallHandlerService.callApp(lmsURL, userData, companyid, action);
                    break;
            }
            if (resObj.has("success") && resObj.optBoolean("success", false)) {
                String appCurrencyid = resObj.getString("currencyid");
                if (appCurrencyid.equalsIgnoreCase(currencyid)) {
                    iscurrencySame = true;
                } else {
                    iscurrencySame = false; 
                }
            }
            issuccess = true;
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            jobj.put("iscurrencysame", iscurrencySame);
            jobj.put("success", issuccess);
            jobj.put("msg", msg);
        } catch (JSONException ex) {
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView checkSecurityGateFunctionalityisusedornot(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        boolean issuccess = false;
        boolean isCheckSecurityGate = false;
        String msg = "";
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            isCheckSecurityGate=accountingHandlerDAOobj.checkSecurityGateFunctionalityisusedornot(companyid);
            if(isCheckSecurityGate){
                issuccess=true;
            }
            
        }catch (Exception ex) {
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            jobj.put("isCheckSecurityGate", isCheckSecurityGate);
            jobj.put("success", issuccess);
            jobj.put("msg", msg);
        } catch (JSONException ex) {
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView IsLineLevelTaxUsedInTransaction(HttpServletRequest request, HttpServletResponse response) {
        JSONArray jSONArray = new JSONArray();
        JSONObject jobj = new JSONObject();
        boolean isUsed = false;
        boolean issuccess = false;
        boolean iscurrencySame = true;
        String msg = "";
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String termtype = !StringUtil.isNullOrEmpty(request.getParameter("taxtype"))?request.getParameter("taxtype"):"";
            String termname = !StringUtil.isNullOrEmpty(request.getParameter("taxname"))?request.getParameter("taxname"):"";
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyId", companyid);
            requestParams.put("termtype", termtype);
            requestParams.put("termname", termname);
            Map<String,Object> Map = accCompanyPreferencesObj.getTermSummary(requestParams);
            //Is Used in Product.
            List productTermsMaplist = (List) Map.get("productTermsMaplist");
            if (productTermsMaplist.size() > 0) {
                isUsed = true;
                for(Object Obj : productTermsMaplist){
                    JSONObject jSONObject = new JSONObject();
                    ProductTermsMap ptmObj = (ProductTermsMap) Obj;
                    jSONObject.put("TermName", ptmObj.getTerm().getTerm());
                    jSONObject.put("UsedIn", "Product");
                    jSONObject.put("UsedInNumber", ptmObj.getProduct().getName());
                    jSONArray.put(jSONObject);
                }
            }
            //Is Used in Goods Receipt.
            List goodsReceiptDetailTermsMaplist = (List) Map.get("goodsReceiptDetailTermsMaplist");
            if (goodsReceiptDetailTermsMaplist.size() > 0) {
                isUsed = true;
                for(Object Obj : goodsReceiptDetailTermsMaplist){
                    JSONObject jSONObject = new JSONObject();
                    ReceiptDetailTermsMap rdtmObj = (ReceiptDetailTermsMap) Obj;
                    jSONObject.put("TermName", rdtmObj.getTerm().getTerm());
                    jSONObject.put("UsedIn", "Goods Receipt");
                    jSONObject.put("UsedInNumber", rdtmObj.getGoodsreceiptdetail().getGoodsReceipt().getGoodsReceiptNumber());
                    jSONArray.put(jSONObject);
                }
            }
            //Is Used in Invoice.
            List InvoiceDetailTermsMaplist = (List) Map.get("InvoiceDetailTermsMaplist");
            if (InvoiceDetailTermsMaplist.size() > 0) {
                isUsed = true;
                for(Object Obj : InvoiceDetailTermsMaplist){
                    JSONObject jSONObject = new JSONObject();
                    InvoiceDetailTermsMap idtmObj = (InvoiceDetailTermsMap) Obj;
                    jSONObject.put("TermName", idtmObj.getTerm().getTerm());
                    jSONObject.put("UsedIn", "Invoice");
                    jSONObject.put("UsedInNumber", idtmObj.getInvoicedetail().getInvoice().getInvoiceNumber());
                    jSONArray.put(jSONObject);
                }
            }
            
            //Is Used in Goods Receipt Order.
            List goodsReceiptOrderDetailTermMaplist = (List) Map.get("goodsReceiptOrderDetailTermMaplist");
            if (goodsReceiptOrderDetailTermMaplist.size() > 0) {
                isUsed = true;
                for(Object Obj : goodsReceiptOrderDetailTermMaplist){
                    JSONObject jSONObject = new JSONObject();
                    ReceiptOrderDetailTermMap rodtmObj = (ReceiptOrderDetailTermMap) Obj;
                    jSONObject.put("TermName", rodtmObj.getTerm().getTerm());
                    jSONObject.put("UsedIn", "Goods Receipt Order");
                    jSONObject.put("UsedInNumber", rodtmObj.getGrodetail().getGrOrder().getGoodsReceiptOrderNumber());
                    jSONArray.put(jSONObject);
                }
            }
            //Is Used in Delivery Order.
            List deliveryOrderDetailTermMaplist = (List) Map.get("deliveryOrderDetailTermMaplist");
            if (deliveryOrderDetailTermMaplist.size() > 0) {
                isUsed = true;
                for(Object Obj : deliveryOrderDetailTermMaplist){
                    JSONObject jSONObject = new JSONObject();
                    DeliveryOrderDetailTermMap dodtmObj = (DeliveryOrderDetailTermMap) Obj;
                    jSONObject.put("TermName", dodtmObj.getTerm().getTerm());
                    jSONObject.put("UsedIn", "Delivery Order");
                    jSONObject.put("UsedInNumber", dodtmObj.getDodetail().getDeliveryOrder().getDeliveryOrderNumber());
                    jSONArray.put(jSONObject);
                }
            }
            //Is Used in Purchase Order.
            List purchaseOrderDetailTermMaplist = (List) Map.get("purchaseOrderDetailTermMaplist");
            if (purchaseOrderDetailTermMaplist.size() > 0) {
                isUsed = true;
                for(Object Obj : purchaseOrderDetailTermMaplist){
                    JSONObject jSONObject = new JSONObject();
                    PurchaseOrderDetailsTermMap podtmObj = (PurchaseOrderDetailsTermMap) Obj;
                    jSONObject.put("TermName", podtmObj.getTerm().getTerm());
                    jSONObject.put("UsedIn", "Purchase Order");
                    jSONObject.put("UsedInNumber", podtmObj.getPodetails().getPurchaseOrder().getPurchaseOrderNumber());
                    jSONArray.put(jSONObject);
                }
            }
            //Is Used in Sales Order.
            List salesOrderDetailTermMaplist = (List) Map.get("salesOrderDetailTermMaplist");
            if (salesOrderDetailTermMaplist.size() > 0) {
                isUsed = true;
                for(Object Obj : salesOrderDetailTermMaplist){
                    JSONObject jSONObject = new JSONObject();
                    SalesOrderDetailTermMap sodtmObj = (SalesOrderDetailTermMap) Obj;
                    jSONObject.put("TermName", sodtmObj.getTerm().getTerm());
                    jSONObject.put("UsedIn", "Sales Order");
                    jSONObject.put("UsedInNumber", sodtmObj.getSalesOrderDetail().getSalesOrder().getSalesOrderNumber());
                    jSONArray.put(jSONObject);
                }
            }
            //Is Used in Purcahse Return.
            List purchaseReturnDetailTermMaplist = (List) Map.get("purchaseReturnDetailTermMaplist");
            if (purchaseReturnDetailTermMaplist.size() > 0) {
                isUsed = true;
                for(Object Obj : purchaseReturnDetailTermMaplist){
                    JSONObject jSONObject = new JSONObject();
                    PurchaseReturnDetailsTermMap prdtmObj = (PurchaseReturnDetailsTermMap) Obj;
                    jSONObject.put("TermName", prdtmObj.getTerm().getTerm());
                    jSONObject.put("UsedIn", "Purchase Return");
                    jSONObject.put("UsedInNumber", prdtmObj.getPurchasereturndetail().getPurchaseReturn().getPurchaseReturnNumber());
                    jSONArray.put(jSONObject);
                }
            }
            //Is Used in Sales Return.
            List salesReturnDetailTermMaplist = (List) Map.get("salesReturnDetailTermMaplist");
            if (salesReturnDetailTermMaplist.size() > 0) {
                isUsed = true;
                for(Object Obj : salesReturnDetailTermMaplist){
                    JSONObject jSONObject = new JSONObject();
                    SalesReturnDetailsTermMap srdtmObj = (SalesReturnDetailsTermMap) Obj;
                    jSONObject.put("TermName", srdtmObj.getTerm().getTerm());
                    jSONObject.put("UsedIn", "Sales Return");
                    jSONObject.put("UsedInNumber", srdtmObj.getSalesreturndetail().getSalesReturn().getSalesReturnNumber());
                    jSONArray.put(jSONObject);
                }
            }
            //Is Used in Vendor Quotation.
            List vendorQuotationDetailsTermMaplist = (List) Map.get("vendorQuotationDetailsTermMaplist");
            if (vendorQuotationDetailsTermMaplist.size() > 0) {
                isUsed = true;
                for(Object Obj : vendorQuotationDetailsTermMaplist){
                    JSONObject jSONObject = new JSONObject();
                    VendorQuotationDetailsTermMap vqdtmObj = (VendorQuotationDetailsTermMap) Obj;
                    jSONObject.put("TermName", vqdtmObj.getTerm().getTerm());
                    jSONObject.put("UsedIn", "Vendor Quotation");
                    jSONObject.put("UsedInNumber", vqdtmObj.getVendorquotationdetails().getVendorquotation().getQuotationNumber());
                    jSONArray.put(jSONObject);
                }
            }
            //Is Used in Customer Quotation.
            List quotationDetailTermMaplist = (List) Map.get("quotationDetailTermMaplist");
            if (quotationDetailTermMaplist.size() > 0) {
                isUsed = true;
                for(Object Obj : quotationDetailTermMaplist){
                    JSONObject jSONObject = new JSONObject();
                    QuotationDetailTermMap cqdtmObj = (QuotationDetailTermMap) Obj;
                    jSONObject.put("TermName", cqdtmObj.getTerm().getTerm());
                    jSONObject.put("UsedIn", "Customer Quotation");
                    jSONObject.put("UsedInNumber", cqdtmObj.getQuotationDetail().getQuotation().getQuotationNumber());
                    jSONArray.put(jSONObject);
                }
            }
            
            issuccess = true;
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            jobj.put("success", issuccess);
            jobj.put("UsedInArray", jSONArray);
            jobj.put("isUsed", isUsed);
            jobj.put("msg", msg);
        } catch (JSONException ex) {
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    public ModelAndView IsLineLevelTermEdit(HttpServletRequest request, HttpServletResponse response) {
        JSONArray jSONArray = new JSONArray();
        JSONObject jobj = new JSONObject();
        boolean isUsed = false;
        boolean issuccess = false;
        boolean iscurrencySame = true;
        String msg = "";
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String termid = !StringUtil.isNullOrEmpty(request.getParameter("termid"))?request.getParameter("termid"):"";
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyId", companyid);
            requestParams.put("termid", termid);
            Map<String,Object> Map = accCompanyPreferencesObj.getTermUsedIn(requestParams);
            //Is Used in Product.
            /*List productTermsMaplist = (List) Map.get("productTermsMaplist");
            if (productTermsMaplist.size() > 0) {
                isUsed = true;
                for(Object Obj : productTermsMaplist){
                    JSONObject jSONObject = new JSONObject();
                    ProductTermsMap ptmObj = (ProductTermsMap) Obj;
                    jSONObject.put("TermName", ptmObj.getTerm().getTerm());
                    jSONObject.put("UsedIn", "Product");
                    jSONObject.put("UsedInNumber", ptmObj.getProduct().getName());
                    jSONArray.put(jSONObject);
                }
            }*/
            //Is Used in Goods Receipt.
            List goodsReceiptDetailTermsMaplist = (List) Map.get("goodsReceiptDetailTermsMaplist");
            if (goodsReceiptDetailTermsMaplist.size() > 0) {
                isUsed = true;
                for(Object Obj : goodsReceiptDetailTermsMaplist){
                    JSONObject jSONObject = new JSONObject();
                    ReceiptDetailTermsMap rdtmObj = (ReceiptDetailTermsMap) Obj;
                    jSONObject.put("TermName", rdtmObj.getTerm().getTerm());
                    jSONObject.put("UsedIn", "Goods Receipt");
                    jSONObject.put("UsedInNumber", rdtmObj.getGoodsreceiptdetail().getGoodsReceipt().getGoodsReceiptNumber());
                    jSONArray.put(jSONObject);
                }
            }
            //Is Used in Invoice.
            List InvoiceDetailTermsMaplist = (List) Map.get("InvoiceDetailTermsMaplist");
            if (InvoiceDetailTermsMaplist.size() > 0) {
                isUsed = true;
                for(Object Obj : InvoiceDetailTermsMaplist){
                    JSONObject jSONObject = new JSONObject();
                    InvoiceDetailTermsMap idtmObj = (InvoiceDetailTermsMap) Obj;
                    jSONObject.put("TermName", idtmObj.getTerm().getTerm());
                    jSONObject.put("UsedIn", "Invoice");
                    jSONObject.put("UsedInNumber", idtmObj.getInvoicedetail().getInvoice().getInvoiceNumber());
                    jSONArray.put(jSONObject);
                }
            }
            
            //Is Used in Goods Receipt Order.
            List goodsReceiptOrderDetailTermMaplist = (List) Map.get("goodsReceiptOrderDetailTermMaplist");
            if (goodsReceiptOrderDetailTermMaplist.size() > 0) {
                isUsed = true;
                for(Object Obj : goodsReceiptOrderDetailTermMaplist){
                    JSONObject jSONObject = new JSONObject();
                    ReceiptOrderDetailTermMap rodtmObj = (ReceiptOrderDetailTermMap) Obj;
                    jSONObject.put("TermName", rodtmObj.getTerm().getTerm());
                    jSONObject.put("UsedIn", "Goods Receipt Order");
                    jSONObject.put("UsedInNumber", rodtmObj.getGrodetail().getGrOrder().getGoodsReceiptOrderNumber());
                    jSONArray.put(jSONObject);
                }
            }
            //Is Used in Delivery Order.
            List deliveryOrderDetailTermMaplist = (List) Map.get("deliveryOrderDetailTermMaplist");
            if (deliveryOrderDetailTermMaplist.size() > 0) {
                isUsed = true;
                for(Object Obj : deliveryOrderDetailTermMaplist){
                    JSONObject jSONObject = new JSONObject();
                    DeliveryOrderDetailTermMap dodtmObj = (DeliveryOrderDetailTermMap) Obj;
                    jSONObject.put("TermName", dodtmObj.getTerm().getTerm());
                    jSONObject.put("UsedIn", "Delivery Order");
                    jSONObject.put("UsedInNumber", dodtmObj.getDodetail().getDeliveryOrder().getDeliveryOrderNumber());
                    jSONArray.put(jSONObject);
                }
            }
            //Is Used in Purchase Order.
            List purchaseOrderDetailTermMaplist = (List) Map.get("purchaseOrderDetailTermMaplist");
            if (purchaseOrderDetailTermMaplist.size() > 0) {
                isUsed = true;
                for(Object Obj : purchaseOrderDetailTermMaplist){
                    JSONObject jSONObject = new JSONObject();
                    PurchaseOrderDetailsTermMap podtmObj = (PurchaseOrderDetailsTermMap) Obj;
                    jSONObject.put("TermName", podtmObj.getTerm().getTerm());
                    jSONObject.put("UsedIn", "Purchase Order");
                    jSONObject.put("UsedInNumber", podtmObj.getPodetails().getPurchaseOrder().getPurchaseOrderNumber());
                    jSONArray.put(jSONObject);
                }
            }
            //Is Used in Sales Order.
            List salesOrderDetailTermMaplist = (List) Map.get("salesOrderDetailTermMaplist");
            if (salesOrderDetailTermMaplist.size() > 0) {
                isUsed = true;
                for(Object Obj : salesOrderDetailTermMaplist){
                    JSONObject jSONObject = new JSONObject();
                    SalesOrderDetailTermMap sodtmObj = (SalesOrderDetailTermMap) Obj;
                    jSONObject.put("TermName", sodtmObj.getTerm().getTerm());
                    jSONObject.put("UsedIn", "Sales Order");
                    jSONObject.put("UsedInNumber", sodtmObj.getSalesOrderDetail().getSalesOrder().getSalesOrderNumber());
                    jSONArray.put(jSONObject);
                }
            }
            //Is Used in Purcahse Return.
            List purchaseReturnDetailTermMaplist = (List) Map.get("purchaseReturnDetailTermMaplist");
            if (purchaseReturnDetailTermMaplist.size() > 0) {
                isUsed = true;
                for(Object Obj : purchaseReturnDetailTermMaplist){
                    JSONObject jSONObject = new JSONObject();
                    PurchaseReturnDetailsTermMap prdtmObj = (PurchaseReturnDetailsTermMap) Obj;
                    jSONObject.put("TermName", prdtmObj.getTerm().getTerm());
                    jSONObject.put("UsedIn", "Purchase Return");
                    jSONObject.put("UsedInNumber", prdtmObj.getPurchasereturndetail().getPurchaseReturn().getPurchaseReturnNumber());
                    jSONArray.put(jSONObject);
                }
            }
            //Is Used in Sales Return.
            List salesReturnDetailTermMaplist = (List) Map.get("salesReturnDetailTermMaplist");
            if (salesReturnDetailTermMaplist.size() > 0) {
                isUsed = true;
                for(Object Obj : salesReturnDetailTermMaplist){
                    JSONObject jSONObject = new JSONObject();
                    SalesReturnDetailsTermMap srdtmObj = (SalesReturnDetailsTermMap) Obj;
                    jSONObject.put("TermName", srdtmObj.getTerm().getTerm());
                    jSONObject.put("UsedIn", "Sales Return");
                    jSONObject.put("UsedInNumber", srdtmObj.getSalesreturndetail().getSalesReturn().getSalesReturnNumber());
                    jSONArray.put(jSONObject);
                }
            }
            //Is Used in Vendor Quotation.
            List vendorQuotationDetailsTermMaplist = (List) Map.get("vendorQuotationDetailsTermMaplist");
            if (vendorQuotationDetailsTermMaplist.size() > 0) {
                isUsed = true;
                for(Object Obj : vendorQuotationDetailsTermMaplist){
                    JSONObject jSONObject = new JSONObject();
                    VendorQuotationDetailsTermMap vqdtmObj = (VendorQuotationDetailsTermMap) Obj;
                    jSONObject.put("TermName", vqdtmObj.getTerm().getTerm());
                    jSONObject.put("UsedIn", "Vendor Quotation");
                    jSONObject.put("UsedInNumber", vqdtmObj.getVendorquotationdetails().getVendorquotation().getQuotationNumber());
                    jSONArray.put(jSONObject);
                }
            }
            //Is Used in Customer Quotation.
            List quotationDetailTermMaplist = (List) Map.get("quotationDetailTermMaplist");
            if (quotationDetailTermMaplist.size() > 0) {
                isUsed = true;
                for(Object Obj : quotationDetailTermMaplist){
                    JSONObject jSONObject = new JSONObject();
                    QuotationDetailTermMap cqdtmObj = (QuotationDetailTermMap) Obj;
                    jSONObject.put("TermName", cqdtmObj.getTerm().getTerm());
                    jSONObject.put("UsedIn", "Customer Quotation");
                    jSONObject.put("UsedInNumber", cqdtmObj.getQuotationDetail().getQuotation().getQuotationNumber());
                    jSONArray.put(jSONObject);
                }
            }
            
            issuccess = true;
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            jobj.put("success", issuccess);
            jobj.put("UsedInArray", jSONArray);
            jobj.put("isUsed", isUsed);
            jobj.put("msg", msg);
        } catch (JSONException ex) {
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    public ModelAndView IsTDSUsedInTransaction(HttpServletRequest request, HttpServletResponse response) {
        JSONArray jSONArray = new JSONArray();
        JSONObject jobj = new JSONObject();
        boolean isUsed = false;
        boolean issuccess = false;
        boolean iscurrencySame = true;
        String msg = "";
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String termtype = !StringUtil.isNullOrEmpty(request.getParameter("taxtype")) ? request.getParameter("taxtype") : "";
            String termname = !StringUtil.isNullOrEmpty(request.getParameter("taxname")) ? request.getParameter("taxname") : "";
            Map<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyId", companyid);
            requestParams.put("termtype", termtype);
            requestParams.put("termname", termname);
            KwlReturnObject srresult = accCompanyPreferencesObj.isTDSUsedInTransactions(requestParams);
            List<GoodsReceipt> invoiceList = srresult.getEntityList();
            //Once Goods Receipts are created, don't allow user to uncheck TDS Flow.
            if (invoiceList.size() > 0) {//Check Whether Used in Goods Receipt.
                isUsed = true;
                JSONObject jSONObj = new JSONObject();
                jSONObj.put("UsedIn", "Goods Receipt");
                jSONObj.put("UsedInNumber", "");
                jSONArray.put(jSONObj);
            } else {//Check Whether Used in Make Payment.
                Map<String, Object> Map = accCompanyPreferencesObj.getTDSSummary(requestParams);
                List TDSMaplist = (List) Map.get("TDSMaplist");
                if (TDSMaplist.size() > 0) {
                    isUsed = true;
                    for (Object Obj : TDSMaplist) {
                        JSONObject jSONObject = new JSONObject();
                        TdsDetails tdsObj = (TdsDetails) Obj;
                        jSONObject.put("TDSPayableAccount", tdsObj.getTdspayableaccount().getAccountName());
                        if (tdsObj.getPaymentdetail() != null) {
                            jSONObject.put("UsedIn", "Invoice Payment");
                            jSONObject.put("UsedInNumber", tdsObj.getPaymentdetail().getPayment().getPaymentNumber());
                        } else if (tdsObj.getAdvanceDetail() != null) {
                            jSONObject.put("UsedIn", "Advance Payment");
                            jSONObject.put("UsedInNumber", tdsObj.getAdvanceDetail().getPayment().getPaymentNumber());
                        } else if (tdsObj.getCreditnotepaymentdetail() != null) {
                            jSONObject.put("UsedIn", "Credit Note Payment");
                            jSONObject.put("UsedInNumber", tdsObj.getCreditnotepaymentdetail().getPayment().getPaymentNumber());
                        } else if (tdsObj.getPaymentdetailotherwise() != null) {
                            jSONObject.put("UsedIn", "Payment Detail Otherwise");
                            jSONObject.put("UsedInNumber", tdsObj.getPaymentdetailotherwise().getPayment().getPaymentNumber());
                        }
                        jSONArray.put(jSONObject);
                    }
                }
            }
            issuccess = true;
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            jobj.put("success", issuccess);
            jobj.put("UsedInArray", jSONArray);
            jobj.put("isUsed", isUsed);
            jobj.put("msg", msg);
        } catch (JSONException ex) {
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView("jsonView", "model", jobj.toString());
    }
    
    /**
     * Description : This Method is used to Check Whether Excise Unit is Used in Transaction.
     * @param request
     * @param response
     */
    public ModelAndView IsExciseUnitUsedInTransaction(HttpServletRequest request, HttpServletResponse response) {
        JSONArray jSONArray = new JSONArray();
        JSONObject jobj = new JSONObject();
        boolean isUsed = false;
        boolean issuccess = false;
        String msg = "";
        try {
            HashMap<String, Object> hashMap = new HashMap<String, Object>();
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String moduleIDs = Constants.Acc_Invoice_ModuleId + "," + Constants.Acc_Cash_Sales_ModuleId + "," + Constants.Acc_Vendor_Invoice_ModuleId + "," + Constants.Acc_Cash_Purchase_ModuleId;
            hashMap.put("moduleId", moduleIDs);//Acc_Cash_Sales_ModuleId//Acc_Cash_Purchase_ModuleId//Acc_Vendor_Invoice_ModuleId
            hashMap.put("companyid", companyid);
            hashMap.put("CheckExciseUnit", true); // Added Unit ID if it is present in request
            /**
             * checks whether template is Used in Sales Invoice, Cash Sales,
             * Purchase Invoice, Cash Purchase.
             */
            KwlReturnObject templateResult = accountingHandlerDAOobj.getModuleTemplates(hashMap);
            int nocount = templateResult.getRecordTotalCount();
            if (nocount > 0) {
                isUsed = true;
//                    JSONObject jSONObj = new JSONObject();
//                    jSONObj.put("UsedIn", "");
//                    jSONObj.put("UsedInNumber", "");
//                    jSONArray.put(jSONObj);
            }
            issuccess = true;
        } catch (SessionExpiredException ex) {
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            jobj.put("success", issuccess);
            jobj.put("UsedInArray", jSONArray);
            jobj.put("isUsed", isUsed);
            jobj.put("msg", msg);
        } catch (JSONException ex) {
            Logger.getLogger(accCompanyPreferencesControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new ModelAndView(Constants.jsonView, Constants.model, jobj.toString());
    }
    
    public void saveInventoryConfiguration(HttpServletRequest request) throws ServiceException, SessionExpiredException {

        String companyId = sessionHandlerImpl.getCompanyid(request);
        String userId = sessionHandlerImpl.getUserid(request);

        KwlReturnObject jeresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
        Company company = (Company) jeresult.getEntityList().get(0);

        jeresult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
        User user = (User) jeresult.getEntityList().get(0);
        try { //ERM-691 creating extra pref object to pass repair store and its default location set in system control
            ExtraCompanyPreferences extraCompanyPreferences;
            KwlReturnObject resultExtra = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyId);
            extraCompanyPreferences = (ExtraCompanyPreferences) resultExtra.getEntityList().get(0);
            JSONObject columnpref = null;
            if (!StringUtil.isNullOrEmpty(extraCompanyPreferences.getColumnPref())) {
                columnpref = new JSONObject(extraCompanyPreferences.getColumnPref());
            }

            String enableStockOutApprovalFlow = request.getParameter("stockInQAApproval");
            String enableInterStoreApprovalFlow = request.getParameter("interStoreQAApproval");
            String enableStockRequestReturnApprovalFlow = request.getParameter("stockRequestReturnQAApproval");
            
            //ERM-691 displaying audit trail messages when user sets a Scrap/QA/Repair warehouse from the system control
            String inspectionStore = request.getParameter("inspectionStore");
            String repairStore = request.getParameter("repairStore");
            String scrapStore = request.getParameter("scrapStore");
            Map<String,Object> requestParams = new HashMap<>();
            requestParams.put(Constants.useridKey,user.getUserID());
            requestParams.put(Constants.companyKey,companyId);
            requestParams.put(Constants.remoteIPAddress,request.getRemoteAddr());
            
            Store store = null;  //store object for displaying audit trail messages regarding which store was updated
            
            if (!StringUtil.isNullOrEmpty(inspectionStore) && !inspectionStore.equalsIgnoreCase(extraCompanyPreferences.getInspectionStore())) {
                KwlReturnObject storekwl = accountingHandlerDAOobj.getObject(Store.class.getName(), inspectionStore);
                store = (Store) (storekwl.getEntityList().isEmpty()?null:storekwl.getEntityList().get(0));
                String auditMessage = "User " + user.getFullName() + " has updated the QA store ";
                if(store!=null){
                  auditMessage += "to " + store.getFullName();
                }
                auditTrailObj.insertAuditLog(AuditAction.INVENTORY_CONFIG, auditMessage, requestParams, "0");
            }
            if (!StringUtil.isNullOrEmpty(repairStore) && !repairStore.equalsIgnoreCase(extraCompanyPreferences.getRepairStore())) {
                KwlReturnObject storekwl = accountingHandlerDAOobj.getObject(Store.class.getName(), repairStore);
                store = (Store) (storekwl.getEntityList().isEmpty()?null:storekwl.getEntityList().get(0));
                String auditMessage = "User " + user.getFullName() + " has updated the repair store ";
                if(store!=null){
                  auditMessage += "to " + store.getFullName();
                }
                auditTrailObj.insertAuditLog(AuditAction.INVENTORY_CONFIG, auditMessage, requestParams, "0");
            }
            if (!StringUtil.isNullOrEmpty(scrapStore) && columnpref != null && !scrapStore.equalsIgnoreCase(columnpref.optString("scrapStore", ""))) {
                KwlReturnObject storekwl = accountingHandlerDAOobj.getObject(Store.class.getName(), scrapStore);
                store = (Store) (storekwl.getEntityList().isEmpty()?null:storekwl.getEntityList().get(0));
                String auditMessage = "User " + user.getFullName() + " has updated the scrap store ";
                if(store!=null){
                  auditMessage += "to " + store.getFullName();
                }
                auditTrailObj.insertAuditLog(AuditAction.INVENTORY_CONFIG, auditMessage, requestParams, "0");
            }

            InventoryConfig config = invConfigService.getConfigByCompany(company);

            if (config != null) {
                boolean oldEnableStockOutApprovalFlow = config.isEnableStockoutApprovalFlow();
                boolean oldenableInterStoreApprovalFlow = config.isEnableISTReturnApprovalFlow();
                boolean oldEnableStockRequestReturnApprovalFlow = config.isEnableSRReturnApprovalFlow();

                //stock-in QA approval
                boolean stockOutApprovalFlow = false;
                if ("on".equalsIgnoreCase(enableStockOutApprovalFlow)) {
                    stockOutApprovalFlow = true;
                }
                config.setEnableStockoutApprovalFlow(stockOutApprovalFlow);
                if (config.isEnableStockoutApprovalFlow() != oldEnableStockOutApprovalFlow) {
                    String auditMessage = "User " + user.getFullName() + " " + (config.isEnableStockoutApprovalFlow() ? "enabled" : "disabled") + " the QA Inspection Flow for Stock Adjustment module.";
                    auditTrailObj.insertAuditLog(AuditAction.INVENTORY_CONFIG, auditMessage, request, "0");
                }

                //interstore QA approval
                boolean interStoreApprovalFlow = false;
                if ("on".equalsIgnoreCase(enableInterStoreApprovalFlow)) {
                    interStoreApprovalFlow = true;
                }
                config.setEnableISTReturnApprovalFlow(interStoreApprovalFlow);
                if (config.isEnableISTReturnApprovalFlow() != oldenableInterStoreApprovalFlow) {
                    String auditMessage = "User " + user.getFullName() + " " + (config.isEnableISTReturnApprovalFlow() ? "enabled" : "disabled") + " the QA Inspection Flow for Inter Store Stock Transfer module.";
                    auditTrailObj.insertAuditLog(AuditAction.INVENTORY_CONFIG, auditMessage, request, "0");
                }

                //stock Request Return QA approval
                boolean stockRequestReturnApprovalFlow = false;
                if ("on".equalsIgnoreCase(enableStockRequestReturnApprovalFlow)) {
                    stockRequestReturnApprovalFlow = true;
                }
                config.setEnableSRReturnApprovalFlow(stockRequestReturnApprovalFlow);
                if (config.isEnableSRReturnApprovalFlow() != oldEnableStockRequestReturnApprovalFlow) {
                    String auditMessage = "User " + user.getFullName() + " " + (config.isEnableSRReturnApprovalFlow() ? "enabled" : "disabled") + " the QA Inspection Flow for Stock Request module.";
                    auditTrailObj.insertAuditLog(AuditAction.INVENTORY_CONFIG, auditMessage, request, "0");
                }
                invConfigService.addConfig(user, config);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveInventoryConfiguration: " + ex.getMessage(), ex);
        }

    }

}
