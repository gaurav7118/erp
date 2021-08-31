/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.receipt;

import com.krawler.common.admin.AuditAction;
import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.CommonIndonesianNumberToWords;
import com.krawler.common.util.Constants;
import com.krawler.common.util.StringUtil;
import com.krawler.common.util.URLUtil;
//import com.krawler.customFieldMaster.fieldDataManager;
import com.krawler.esp.handlers.StorageHandler;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.RemoteAPI.remoteAPIController;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.bankreconciliation.accBankReconciliationDAO;
import com.krawler.spring.accounting.companypreferances.CompanyPreferencesConstants;
import com.krawler.spring.accounting.companypreferances.accCompanyPreferencesDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.customer.accCustomerDAO;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.handler.AccountingManager;
import com.krawler.spring.accounting.invoice.InvoiceConstants;
import com.krawler.spring.accounting.invoice.accInvoiceCMN;
import com.krawler.spring.accounting.invoice.accInvoiceDAO;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.loan.accLoanDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.common.fieldDataManager;
import com.krawler.spring.exportFunctionality.exportMPXDAOImpl;
import com.krawler.spring.importFunctionality.ImportDAO;
import com.krawler.spring.importFunctionality.ImportHandler;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.web.servlet.support.RequestContextUtils;
import com.krawler.esp.handlers.APICallHandlerService;
import com.krawler.spring.accounting.customDesign.CustomDesignDAO;
import com.krawler.spring.accounting.customDesign.CustomDesignHandler;
import com.krawler.spring.accounting.customDesign.CustomDesignerConstants;
import com.krawler.spring.accounting.handler.CommonFunctions;
import com.krawler.spring.accounting.multiLevelApprovalRule.AccMultiLevelApprovalDAO;
import com.krawler.spring.accounting.receivepayment.service.AccReceivePaymentModuleService;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentControllerCMN;
import com.krawler.spring.common.AccCommonTablesDAO;
import com.krawler.spring.common.kwlCommonTablesDAO;
import com.krawler.spring.exportFuctionality.ExportRecordHandler;
import org.jsoup.Jsoup;

/**
 *
 * @author krawler
 */
public class AccReceiptServiceImpl implements AccReceiptServiceDAO {

    private static final String LMS_Receipt_Get_Request_Action_number = "39";
    private static final String LMS_Receipt_Update_Request_Action_number = "40";
    private AccReceiptServiceDAO accReceiptServiceDAO;
    private HibernateTransactionManager txnManager;
    private accReceiptDAO accReceiptDAOobj;
    private accJournalEntryDAO accJournalEntryobj;
    private accPaymentDAO accPaymentDAOobj;
    private accTaxDAO accTaxObj;
    private accCurrencyDAO accCurrencyobj;
    private accCompanyPreferencesDAO accCompanyPreferencesObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private String successView;
    private accAccountDAO accAccountDAOobj;
    private auditTrailDAO auditTrailObj;
    private fieldDataManager fieldDataManagercntrl;
    private exportMPXDAOImpl exportDaoObj;
    private accBankReconciliationDAO accBankReconciliationObj;
    private accInvoiceDAO accInvoiceDAOObj;
    private MessageSource messageSource;
    public ImportHandler importHandler;
    private ImportDAO importDao;
    public accCustomerDAO accCustomerDAOObj;
    private accInvoiceCMN accInvoiceCommon;
    private accVendorPaymentDAO accVendorPaymentDAO;
    private EnglishNumberToWords EnglishNumberToWordsOjb = new EnglishNumberToWords();
    private CommonIndonesianNumberToWords IndonesianNumberToWordsOjb = new CommonIndonesianNumberToWords();
    private accLoanDAO accLoanDAOobj;
    private APICallHandlerService apiCallHandlerService;  
    private CustomDesignDAO customDesignDAOObj;
    private AccCommonTablesDAO accCommonTablesDAO;
    private AccMultiLevelApprovalDAO accMultiLevelApprovalDAOObj;
    private AccReceivePaymentModuleService accReceivePaymentModuleService;
    //  private EnglishNumberToWords EnglishNumberToWordsOjb = new EnglishNumberToWords();
    private kwlCommonTablesDAO kwlCommonTablesDAO;

    public void setAccReceivePaymentModuleService(AccReceivePaymentModuleService accReceivePaymentModuleService) {
        this.accReceivePaymentModuleService = accReceivePaymentModuleService;
    }
    
    public void setAccInvoiceCommon(accInvoiceCMN accInvoiceCommon) {
        this.accInvoiceCommon = accInvoiceCommon;
    }
    
    public void setAccVendorPaymentDAO(accVendorPaymentDAO accVendorPaymentDAO) {
        this.accVendorPaymentDAO = accVendorPaymentDAO;
    }
    
    public void setMessageSource(MessageSource msg) {
        this.messageSource = msg;
    }

    public void setAccReceiptServiceDAO(AccReceiptServiceDAO accReceiptServiceDAO) {
        this.accReceiptServiceDAO = accReceiptServiceDAO;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }

    public void setAccCustomerDAO(accCustomerDAO accCustomerDAOObj) {
        this.accCustomerDAOObj = accCustomerDAOObj;
    }

    public void setimportHandler(ImportHandler importHandler) {
        this.importHandler = importHandler;
    }

    public void setimportDAO(ImportDAO importDao) {
        this.importDao = importDao;
    }

    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }

    public void setaccReceiptDAO(accReceiptDAO accReceiptDAOobj) {
        this.accReceiptDAOobj = accReceiptDAOobj;
    }

    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }

    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }

    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyobj) {
        this.accCurrencyobj = accCurrencyobj;
    }

    public void setaccCompanyPreferencesDAO(accCompanyPreferencesDAO accCompanyPreferencesObj) {
        this.accCompanyPreferencesObj = accCompanyPreferencesObj;
    }

    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
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

    public void setaccBankReconciliationDAO(accBankReconciliationDAO accBankReconciliationObj) {
        this.accBankReconciliationObj = accBankReconciliationObj;
    }

    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }

    public void setAccInvoiceDAO(accInvoiceDAO accInvoiceDAOObj) {
        this.accInvoiceDAOObj = accInvoiceDAOObj;
    }

    public void setAccLoanDAOobj(accLoanDAO accLoanDAOobj) {
        this.accLoanDAOobj = accLoanDAOobj;
    }
    
    public void setApiCallHandlerService(APICallHandlerService apiCallHandlerService) {     
        this.apiCallHandlerService = apiCallHandlerService;
    }
    
    public void setcustomDesignDAO(CustomDesignDAO customDesignDAOObj) {
        this.customDesignDAOObj = customDesignDAOObj;
    }
    
    public void setAccCommonTablesDAO(AccCommonTablesDAO accCommonTablesDAO) {
        this.accCommonTablesDAO = accCommonTablesDAO;
    }
   
    /**
     * @param accMultiLevelApprovalDAOObj the accMultiLevelApprovalDAOObj to set
     */
    public void setAccMultiLevelApprovalDAOObj(AccMultiLevelApprovalDAO accMultiLevelApprovalDAOObj) {
        this.accMultiLevelApprovalDAOObj = accMultiLevelApprovalDAOObj;
    }
    
    public void setKwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAO) {
        this.kwlCommonTablesDAO = kwlCommonTablesDAO;
    }
    
    @Override
    //Neeraj-D : Moved from accReceiptControllerCMN to AccReceiptServiceImpl
    public JSONObject getAdvanceCustomerPaymentForRefunds(JSONObject paramJobj) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        List<ReceiptAdvanceDetail> advanceDetailList = new ArrayList<ReceiptAdvanceDetail>();
        boolean issuccess = false;
        String msg = "";
        try {
            DateFormat df = authHandler.getDateOnlyFormat();
            HashMap<String, Object> requestParams = getReceiptRequestMapJSON(paramJobj);
            String start = (String) requestParams.get("start");
            String limit = (String) requestParams.get("limit");
            String companyid = paramJobj.optString(Constants.companyKey);
            KwlReturnObject companyResult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) companyResult.getEntityList().get(0);
            String countryID = company != null && company.getCountry() != null ? company.getCountry().getID() : "";
            String accid = paramJobj.optString("accid", null) != null ? paramJobj.optString("accid") : "";
            requestParams.put("accid", accid);
            String currencyFilterForTrans = "";
            KWLCurrency currencyFilter = null;
            currencyFilterForTrans = (paramJobj.optString("currencyfilterfortrans", null) == null) ? "" : paramJobj.optString("currencyfilterfortrans");
            if (!StringUtil.isNullOrEmpty(currencyFilterForTrans)) {         //ERM-736
                requestParams.put("currencyfilterfortrans", currencyFilterForTrans);
            }
            if (!StringUtil.isNullOrEmpty(currencyFilterForTrans)) {
                KwlReturnObject currencyFilterResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyFilterForTrans);
                currencyFilter = (KWLCurrency) currencyFilterResult.getEntityList().get(0);
            }
         
            if (StringUtil.isNullOrEmpty(paramJobj.optString("isReceipt", null))) { // results in true when 'isReceipt' is not present in the request params means payment method currency and payment currency both are different
                requestParams.put("applyFilterOnCurrency", true); //when 'isReceipt' is absent, based on 'applyFilterOnCurrency' flag condition for loading only payment currency documents will be loaded     
            }
            
            requestParams.put(Constants.start, start);
            requestParams.put(Constants.limit, limit);
            // isRefundLinking flag is used while linking advance payment to refund
            boolean isRefundLinking = (!StringUtil.isNullOrEmpty(paramJobj.optString("isRefundLinking", null))) ? Boolean.parseBoolean(paramJobj.optString("isRefundLinking")) : false;
            KwlReturnObject result = null;
            result = accReceiptDAOobj.getReceiptAdvanceAmountDueDetails(requestParams);
            advanceDetailList = result.getEntityList();
            for (ReceiptAdvanceDetail advanceDetail : advanceDetailList) {
                JSONObject obj = new JSONObject();
                Receipt re = advanceDetail.getReceipt();
                String currencyid = re.getCurrency().getCurrencyID();
                if (re.getCustomer() != null) {
                    obj.put("accountid", re.getCustomer().getID());
                    obj.put("accountnames", re.getCustomer().getName());
                    obj.put("accname", re.getCustomer().getAccount() == null ? "" : re.getCustomer().getAccount().getName());
                    obj.put("acccode", re.getCustomer().getAccount() == null ? "" : re.getCustomer().getAccount().getAcccode());
                }

                obj.put("billno", re.getReceiptNumber());
                obj.put("billid", advanceDetail.getId());
                 obj.put("paymentid", re.getID());
                obj.put("date", df.format(re.getCreationDate()));
                obj.put("documentno", re.getReceiptNumber());

                double amountdue = advanceDetail.getAmountDue();
                double amountDueOriginal = advanceDetail.getAmountDue();
                double taxAmount = advanceDetail.getTaxamount();
                double externalCurrencyRate = re.getExternalCurrencyRate();
                obj.put("totalamount", advanceDetail.getAmount());

                /**
                 * amountdue in advance receipt is inclusive of tax(termamount),
                 * so we need to subtract termamount from amountdue while
                 * showing receipt advance in payment against customer(Refund).
                 * This is only for Indian Country where GST is applied.
                 */
                if (countryID.equalsIgnoreCase("" + Constants.indian_country_id)) {
                    /**
                     * For Indian country , Tax amount should be deducted from
                     * amountdue.
                     */
                    obj.put("amountDueOriginal", advanceDetail.getAmountDue());
                    obj.put("amountDueOriginalSaved", advanceDetail.getAmountDue());
                } else {
                    obj.put("amountDueOriginal", advanceDetail.getAmountDue());
                    obj.put("amountDueOriginalSaved", advanceDetail.getAmountDue());
                }
                obj.put("amount", advanceDetail.getAmount());
                obj.put("currencyid", currencyid);
                obj.put("currencysymbol", re.getCurrency().getSymbol());
                obj.put("currencyidtransaction", currencyid);
                obj.put("currencysymboltransaction", re.getCurrency().getSymbol());
                if (!StringUtil.isNullOrEmpty(currencyFilterForTrans) && !StringUtil.isNullOrEmpty(currencyid) && !currencyFilterForTrans.equals(currencyid)) {
//                    KwlReturnObject bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, currencyid, currencyFilterForTrans, re.getJournalEntry().getEntryDate(), externalCurrencyRate);
                    KwlReturnObject bAmt = accCurrencyobj.getOneCurrencyToOtherModified(requestParams, amountdue, currencyid, currencyFilterForTrans, re.getCreationDate(), externalCurrencyRate);
                    amountdue = (Double) bAmt.getEntityList().get(0);
                    obj.put("currencyidpayment", currencyFilterForTrans);
                    obj.put("currencysymbolpayment", (currencyFilter == null ? re.getCurrency().getSymbol() : currencyFilter.getSymbol()));
                    if (countryID.equalsIgnoreCase("" + Constants.indian_country_id)) {
//                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, taxAmount, currencyid, currencyFilterForTrans, re.getJournalEntry().getEntryDate(), externalCurrencyRate);
                        bAmt = accCurrencyobj.getOneCurrencyToOtherModified(requestParams, taxAmount, currencyid, currencyFilterForTrans, re.getCreationDate(), externalCurrencyRate);
                        taxAmount = (Double) bAmt.getEntityList().get(0);
                    }
                }
                if (countryID.equalsIgnoreCase("" + Constants.indian_country_id)) {
                    /**
                     * For Indian country , Tax amount should be deducted from
                     * amountdue.
                     */
                    obj.put("amountdue", authHandler.round(amountdue, companyid));
                } else {
                    obj.put("amountdue", authHandler.round(amountdue, companyid));
                }
                obj.put("exchangeratefortransaction", (amountDueOriginal <= 0 && amountdue <= 0) ? 0 : (amountdue / amountDueOriginal));
                // add keys needed for refund linking
                if (isRefundLinking) {
                    obj.put("documentid", advanceDetail.getId());
                    obj.put("documentType", Constants.AdvancePayment);
                    obj.put("type", "Advance Payment");
                    obj.put("externalcurrencyrate", re.getExternalCurrencyRate());
                    obj.put("currencyidpayment", currencyFilterForTrans);
                    obj.put("currencysymbolpayment", (currencyFilter == null ? re.getCurrency().getSymbol() : currencyFilter.getSymbol()));
                }
                /**
                 * Fetch Term Details for HSN in India case
                 */
                if (countryID.equalsIgnoreCase("" + Constants.indian_country_id)) { // Fetch  term details of Product
                    obj.put("adId", advanceDetail.getId());
                    KwlReturnObject result6 = accReceiptDAOobj.getAdvanceDetailsTerm(obj);
                    if (result6.getEntityList() != null && result6.getEntityList().size() > 0 && result6.getEntityList().get(0) != null) {
                        double recTermAmount = 0.0;
                        ArrayList<ReceiptAdvanceDetailTermMap> productTermDetail = (ArrayList<ReceiptAdvanceDetailTermMap>) result6.getEntityList();
                        JSONArray productTermJsonArry = new JSONArray();
                        for (ReceiptAdvanceDetailTermMap productTermsMapObj : productTermDetail) {
                            JSONObject productTermJsonObj = new JSONObject();
                            double termAmount = (amountdue) * productTermsMapObj.getPercentage() / 100;
                            productTermJsonObj.put("id", productTermsMapObj.getId());
                            productTermJsonObj.put("termid", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getId());
                            productTermJsonObj.put("productentitytermid", productTermsMapObj.getEntitybasedLineLevelTermRate() != null ? productTermsMapObj.getEntitybasedLineLevelTermRate().getId() : "");
                            productTermJsonObj.put("isDefault", productTermsMapObj.isIsGSTApplied());
                            productTermJsonObj.put("term", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getTerm());
                            productTermJsonObj.put("formula", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getFormula());
                            productTermJsonObj.put("formulaids", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getFormula());
                            productTermJsonObj.put("termpercentage", productTermsMapObj.getPercentage());
                            productTermJsonObj.put("originalTermPercentage", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getPercentage()); // For Service Tax Abatemnt calculation
                            productTermJsonObj.put("termamount", termAmount);
                            productTermJsonObj.put("glaccountname", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getAccountName());
                            productTermJsonObj.put("glaccount", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getID());
                            productTermJsonObj.put("IsOtherTermTaxable", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().isOtherTermTaxable());
                            productTermJsonObj.put("sign", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getSign());
                            productTermJsonObj.put("purchasevalueorsalevalue", productTermsMapObj.getPurchaseValueOrSaleValue());
                            productTermJsonObj.put("deductionorabatementpercent", productTermsMapObj.getDeductionOrAbatementPercent());
                            productTermJsonObj.put("assessablevalue", amountdue);
                            productTermJsonObj.put("taxtype", productTermsMapObj.getTaxType());
                            productTermJsonObj.put("taxvalue", productTermsMapObj.getPercentage());
                            productTermJsonObj.put("termtype", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getTermType());
                            productTermJsonObj.put("termsequence", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getTermSequence());
                            productTermJsonObj.put("formType", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getFormType());
                            productTermJsonObj.put("accountid", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getAccount().getID());
                            productTermJsonObj.put("payableaccountid", productTermsMapObj.getEntitybasedLineLevelTermRate().getLineLevelTerms().getPayableAccount().getID());
                            recTermAmount += termAmount;
                            productTermJsonArry.put(productTermJsonObj);
                        }
                        obj.put("LineTermdetails", productTermJsonArry.toString());
                        obj.put("recTermAmount", recTermAmount);
                    }
                }
                //Get Custom Field Data 
                getAdvanceReceiptCustomData(requestParams, advanceDetail, obj);

                jArr.put(obj);
            }
            jobj.put("data", jArr);
            jobj.put("count", advanceDetailList.size());
            issuccess = true;
        } catch (SessionExpiredException ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
            } catch (JSONException ex) {
                Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
    
    
    @Override
    public JSONObject getReceiptFromLMS(HashMap<String, Object> requestParams) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONObject resObj = new JSONObject();
        String msg = "";
        boolean issuccess = false;
        boolean isused = false;
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();       
        def.setName("ReceiptLMS_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status =null;
        try {         
            String companyid =(String) requestParams.get(Constants.companyKey);
            String lmsURL = URLUtil.buildRestURL("lmsURL");
            lmsURL = lmsURL + "financials/receipt";             
            String userid =(String) requestParams.get("userid");            
            String userFullName =(String) requestParams.get("userfullName");            
            JSONObject userData = new JSONObject();
            userData.put("iscommit", true);
            userData.put("remoteapikey", StorageHandler.GetRemoteAPIKey());
            userData.put("userid", userid);
            userData.put("companyid", companyid);
            try {
                resObj = apiCallHandlerService.restGetMethod(lmsURL, userData.toString());
            } catch (Exception ex) {
                Logger.getLogger(AccReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            if (!resObj.isNull("success") && resObj.getBoolean("success")) {
                JSONArray jarr = new JSONArray(resObj.optString("receiptdata"));
                JSONArray array = new JSONArray();               
                for (int i = 0; i < jarr.length(); i++) {
                    JSONObject dataMap = jarr.getJSONObject(i);
                    try {                       
                        boolean isValidReceipt = false;
                        isValidReceipt = checkPaymentAndInvoiceExist(dataMap, companyid);
                        if (isValidReceipt) {
                            status =txnManager.getTransaction(def);
                            Receipt receipt =saveReceiptFromLMS(requestParams, dataMap);
                            JSONObject jobj1 = new JSONObject();
                            jobj1.put("receiptid", receipt.getID());
                            jobj1.put("jeno", receipt.getJournalEntry().getEntryNumber());
                            jobj1.put("jeid", receipt.getJournalEntry().getID());
                            jobj1.put("number", dataMap.optString("no"));//payment number of LMS
                            jobj1.put("tid", dataMap.optString("tid"));//Transaction id of LMS
                            array.put(jobj1);  
                            txnManager.commit(status);
                            auditTrailObj.insertAuditLog(AuditAction.RECEIPT_ADDED, "User " + userFullName + " has added a receipt " + receipt.getReceiptNumber(), requestParams, receipt.getID());                                    
                        }
                    } catch (Exception e) {   
                        txnManager.rollback(status);
                        Logger.getLogger(AccReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, e);
                    }
                }
                
                // Call API To update receipt table at LMS side
                if (array.length() > 0) {
                    try {
                        userData.put("receiptdata", array);
                        JSONObject resObj1 = apiCallHandlerService.restPostMethod(lmsURL, userData.toString());
                    } catch (Exception ex) {
                         Logger.getLogger(AccReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }                   
                    status = txnManager.getTransaction(def);                   
                    auditTrailObj.insertAuditLog(AuditAction.RECEIPT_ADDED, "User " + userFullName+ " has sync "+array.length()+" Receipts from LMS ", requestParams,companyid);
                    txnManager.commit(status);
                }
            }           
            issuccess = true;
            msg = "Receipt Synced Successfully.";
            issuccess = true;
        } catch (Exception ex) {
            Logger.getLogger(AccReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            txnManager.rollback(status);
            isused = true;
            issuccess = false;
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("isused", isused);
            } catch (JSONException ex) {
                Logger.getLogger(AccReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
    
    private boolean checkPaymentAndInvoiceExist(JSONObject dataMap, String companyid) {
        boolean isValidReceipt = true;
        try {

            String entryNumber = dataMap.getString("no");
            KwlReturnObject result = accReceiptDAOobj.getReceiptFromBillNo(entryNumber, companyid);
            int count = result.getRecordTotalCount();
            if (count > 0) { //If count is greater than zero it means this record is already exist so no need need to execute save method for this
                isValidReceipt = false;
                Logger.getLogger(AccReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, "Receipt already exist.");
            } else {
                String detailsJsonString = dataMap.getString("detail");
                JSONArray jSONArray = new JSONArray(detailsJsonString);
                if (jSONArray.length() > 0) {
                    for (int i = 0; i < jSONArray.length(); i++) {
                        JSONObject obj = jSONArray.getJSONObject(i);
                        String invoiceid = obj.getString("billid");
                        KwlReturnObject resultInvoice = accountingHandlerDAOobj.getObject(Invoice.class.getName(), invoiceid);
                        Invoice invoice = (Invoice) resultInvoice.getEntityList().get(0);
                        if (invoice == null) {//This condition true means invoice is not present in ERP So we can not save sucgh receipts
                            isValidReceipt = false;
                            Logger.getLogger(AccReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, "Invoice not present in erp for synced receipt.");
                            break;
                        }
                    }
                } else {//Receipt without invoice can not be saved so making this flag is false
                    isValidReceipt = false;
                    Logger.getLogger(AccReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, "Invoice Details is not present in synced receipt.");
                }
            }
        } catch (Exception ex) {
            isValidReceipt = false;
            Logger.getLogger(AccReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            return isValidReceipt;
        }
    }
    
    public Receipt saveReceiptFromLMS(HashMap<String, Object> requestParams, JSONObject dataMap) throws SessionExpiredException, ServiceException {
        Receipt receipt = null;
        try {
            Receipt editReceiptObject = null;
            receipt = createLMSReceiptObject(requestParams, dataMap, editReceiptObject);

            /*
             * For Malaysian country, while receiving advance payment from
             * customer, GST may or may not be added. This causes problem in
             * edit case that is why we have set tax and tax amount null
             * initially. ERP-9810
             */

            receipt.setTax(null);
            receipt.setTaxAmount(0);
            JournalEntry journalEntry = LMSJournalEntryObject(requestParams, dataMap, editReceiptObject);
            receipt.setJournalEntry(journalEntry);
            List<Receipt> receipts = new ArrayList<Receipt>();
            receipts.add(receipt);
            accJournalEntryobj.saveJournalEntryByObject(journalEntry);
            Set<JournalEntryDetail> details = null;
            accReceiptDAOobj.saveReceiptObject(receipts);
            String detailsJsonString = dataMap.getString("detail");
            JSONArray jSONArray = new JSONArray(detailsJsonString);
            JSONArray jSONArrayAgainstInvoice = new JSONArray();
            JSONArray jSONArrayCNDN = new JSONArray();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                jSONArrayAgainstInvoice.put(jSONObject);
            }
            if (jSONArrayAgainstInvoice.length() > 0) {
                if (details == null) {
                    // Get JE Details for Invoices
                    details = LMSjournalEntryDetailObject(requestParams, dataMap, jSONArrayAgainstInvoice, journalEntry, receipt, Constants.PaymentAgainstInvoice);
                } else {
                    details.addAll(LMSjournalEntryDetailObject(requestParams, dataMap, jSONArrayAgainstInvoice, journalEntry, receipt, Constants.PaymentAgainstInvoice));
                }
                //Save Invoice Details selected in payment
                HashSet receiptDetails = LMSReceiptDetailObject(requestParams, jSONArrayAgainstInvoice, receipt, Constants.PaymentAgainstInvoice);
                receipt.setRows(receiptDetails);
            }

            if (details != null) {
                // Get JE Details for Payment Method 
                details.addAll(LMSJournalEntryDetailPaymentMethodObjects(requestParams, dataMap, jSONArrayCNDN, journalEntry, receipt, Constants.PaymentAgainstCNDN));
            }
            journalEntry.setDetails(details);
            accJournalEntryobj.saveJournalEntryDetailsSet(details);
            PayDetail payDetail = getLMSPayDetailObject(requestParams, dataMap, editReceiptObject, receipt);
            receipt.setPayDetail(payDetail);//as cascaded it will be saved automatically            
        } catch (Exception ex) {
             throw ServiceException.FAILURE("savePayment : " + ex.getMessage(), ex);
        }
        return receipt;
    }

    public Receipt createLMSReceiptObject(HashMap<String, Object> requestParams, JSONObject dataMap, Receipt editReceiptObject) throws SessionExpiredException, ServiceException, AccountingException, JSONException {
        Receipt receiptObject = null;
        try{
            Customer cust = null;
            String sequenceformat = dataMap.optString("sequenceformat", "NA");
            String companyid =(String) requestParams.get(Constants.companyKey);
            String gcurrencyid =(String) requestParams.get(Constants.currencyKey);
            double externalCurrencyRate = dataMap.optDouble("externalcurrencyrate", 1);
            String entryNumber = dataMap.optString("no", "");
            String lmsreceiptid = dataMap.optString("lmsreceiptid", "");
            double PaymentCurrencyToPaymentMethodCurrencyRate = dataMap.optDouble("paymentCurrencyToPaymentMethodCurrencyExchangeRate", 1);
            int receiptType = dataMap.getInt("receipttype");
            int actualReceiptType = dataMap.optInt("actualReceiptType", 1);
            HashMap receipthm = new HashMap();
            boolean ismanydbcr = dataMap.optBoolean("ismanydbcr", false);
            receipthm.put("ismanydbcr", ismanydbcr);
            receipthm.put("receipttype", receiptType);
            receipthm.put("actualReceiptType", actualReceiptType);
            String accountId = dataMap.optString("accid", "");
            String createdby = (String) requestParams.get("createdby");
            String modifiedby = (String) requestParams.get("modifiedby");
            long createdon = System.currentTimeMillis();
            long updatedon = System.currentTimeMillis();
            DateFormat df=new SimpleDateFormat("yyyy-MM-dd");
            String paidToid = dataMap.optString("paidToCmb", "");
            if (!StringUtil.isNullOrEmpty(paidToid)) {
                receipthm.put("paidToCmb", paidToid);
            }
            boolean isCustomer = false;
            if (StringUtil.isNullOrEmpty(accountId)) {
                String acccode = dataMap.optString("acccode", "");
                accountId = accInvoiceDAOObj.getCustomerId(companyid, acccode);
            }
            if (!StringUtil.isNullOrEmpty(accountId)) {
                KwlReturnObject custObj = accountingHandlerDAOobj.getObject(Customer.class.getName(), accountId);
                if (custObj.getEntityList().get(0) != null) {
                    cust = (Customer) custObj.getEntityList().get(0);
                }
                if (cust != null) {
                    isCustomer = true;
                }
            }
            if (isCustomer) {
                receipthm.put("customerId", accountId);
            }
            requestParams.put("accid", accountId);
            receipthm.put("entrynumber", entryNumber);
            receipthm.put("autogenerated", false);
            if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
                List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Receive_Payment_ModuleId, entryNumber, companyid);
                if (!resultList.isEmpty()) {
                    boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                    String formatName = (String) resultList.get(1);
                    if (!isvalidEntryNumber) {
                        throw new AccountingException("Entered document number <b>" + entryNumber + "</b> is belongs to Auto Sequence Format  <b>" + formatName + "</b>. Please select Sequence Format <b>" + formatName + "</b> instead of entering document number manually.");
                    }
                }
            }
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), gcurrencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String currencyid = dataMap.optString("currencyid", currency.getCurrencyID());
            
            if (!StringUtil.isNullOrEmpty(currencyid) && !currencyid.equalsIgnoreCase(gcurrencyid)) {
                throw new AccountingException("Base currency of LMS and ERP not same");
            }
            receipthm.put("currencyid", currencyid);
            receipthm.put("externalCurrencyRate", externalCurrencyRate);
            receipthm.put("memo", dataMap.optString("memo", ""));
            receipthm.put("companyid", company.getCompanyID());
            receipthm.put("createdby", createdby);
            receipthm.put("modifiedby", modifiedby);
            receipthm.put("createdon", createdon);
            receipthm.put("updatedon", updatedon);
            receipthm.put("creationDate", df.parse(dataMap.optString("creationdateStr", "")));
            receipthm.put("isLinkedToClaimedInvoice", dataMap.optBoolean("isLinkedToClaimedInvoice", false));
            receipthm.put("PaymentCurrencyToPaymentMethodCurrencyRate", PaymentCurrencyToPaymentMethodCurrencyRate);
            receipthm.put("lmsreceiptid", lmsreceiptid);
            if (editReceiptObject != null) {
                receipthm.put("receiptid", editReceiptObject.getID());
            }
            receiptObject = accReceiptDAOobj.getReceiptObj(receipthm);
            
        }catch(ParseException ex){
            Logger.getLogger(AccReceiptServiceImpl.class.getName()).log(Level.SEVERE, null,ex);
        } catch (Exception ex) {
            Logger.getLogger(AccReceiptServiceImpl.class.getName()).log(Level.SEVERE, null,ex);
        }
        return receiptObject;
    }

    public JournalEntry LMSJournalEntryObject(HashMap<String, Object> requestParams, JSONObject dataMap, Receipt editReceiptObject) throws SessionExpiredException, ServiceException, AccountingException {
        JournalEntry journalEntry = null;
        try {
            Date billdateVal = null;
            String userid = (String) requestParams.get("userid");
            String companyid = (String) requestParams.get(Constants.companyKey);
            String gcurrencyid = (String) requestParams.get(Constants.currencyKey);
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            DateFormat userdf = (DateFormat) requestParams.get(Constants.userdf);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), gcurrencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String currencyid = dataMap.optString("currencyid", currency.getCurrencyID());
            double PaymentCurrencyToPaymentMethodCurrencyRate = dataMap.optDouble("paymentCurrencyToPaymentMethodCurrencyExchangeRate", 1);
            boolean ismulticurrencypaymentje = dataMap.optBoolean("ismulticurrencypaymentje", false);
            String jeid = "";
            String jeentryNumber = "";
            boolean jeautogenflag = false;
            String jeIntegerPart = "";
            String datePrefix = "";
            String dateAfterPrefix = "";
            String dateSuffix = "";
            String jeSeqFormatId = "";
            double externalCurrencyRate = dataMap.optDouble("externalcurrencyrate", 1);

            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            Map<String, Object> jeDataMap = new HashMap<String, Object>();
            jeDataMap.put(Constants.companyKey,companyid);
            jeDataMap.put(Constants.globalCurrencyKey, gcurrencyid);
            jeDataMap.put(Constants.df, df);  
            jeDataMap.put(Constants.userdf, userdf);
            
            try {
                billdateVal=df.parse(dataMap.getString("creationdate"));
                jeDataMap.put("entrydate", billdateVal);
            } catch (ParseException ex) {
                Calendar cal = Calendar.getInstance();
                long billdateValue = dataMap.getLong("creationdate");
                cal.setTimeInMillis(billdateValue);
                billdateVal = cal.getTime();
                try {
                    billdateVal = df.parse(df.format(billdateVal));
                    jeDataMap.put("entrydate", billdateVal);
                } catch (ParseException ex1) {
                    jeDataMap.put("entrydate", billdateVal);
                }
            }
            
            if (editReceiptObject == null) {
                synchronized (this) {
                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", "autojournalentry");
                    JEFormatParams.put("companyid", companyid);
                    JEFormatParams.put("isdefaultFormat", true);

                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false,billdateVal);
                    jeentryNumber =(String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    jeIntegerPart = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                    datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);
                    dateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);
                    dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);
                    jeSeqFormatId = format.getID();
                    jeautogenflag = true;

                    jeDataMap.put("entrynumber", jeentryNumber);
                    jeDataMap.put("autogenerated", jeautogenflag);
                    jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                    jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
                    jeDataMap.put(Constants.DATEPREFIX, datePrefix);
                    jeDataMap.put(Constants.DATEAFTERPREFIX, dateAfterPrefix);
                    jeDataMap.put(Constants.DATESUFFIX, dateSuffix);
                }
            } else if (editReceiptObject != null && editReceiptObject.getJournalEntry() != null) {
                JournalEntry entry = editReceiptObject.getJournalEntry();
                jeid = editReceiptObject.getJournalEntry().getID();
                jeDataMap.put("entrynumber", entry.getEntryNumber());
                jeDataMap.put("autogenerated", entry.isAutoGenerated());
                jeDataMap.put(Constants.SEQFORMAT, entry.getSeqformat().getID());
                jeDataMap.put(Constants.SEQNUMBER, entry.getSeqnumber());
                jeDataMap.put(Constants.DATEPREFIX, entry.getDatePreffixValue());
                jeDataMap.put(Constants.DATEAFTERPREFIX, entry.getDateAfterPreffixValue());
                jeDataMap.put(Constants.DATESUFFIX, entry.getDateSuffixValue());
            }
            
            if (!StringUtil.isNullOrEmpty(currencyid) && !currencyid.equalsIgnoreCase(gcurrencyid)) {
                HashMap<String, Object> reqMap = new HashMap<String, Object>();
                reqMap.put("gcurrencyid", gcurrencyid);
                reqMap.put("companyid", companyid);
                externalCurrencyRate = accCurrencyobj.getCurrencyToBaseRate(reqMap, currencyid, billdateVal);
                jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            }
            jeDataMap.put("companyid", company.getCompanyID());
            jeDataMap.put("memo", dataMap.optString("memo", ""));
            jeDataMap.put("currencyid", currencyid);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeDataMap.put("PaymentCurrencyToPaymentMethodCurrencyRate", PaymentCurrencyToPaymentMethodCurrencyRate);
            jeDataMap.put("ismulticurrencypaymentje", ismulticurrencypaymentje);
            jeDataMap.put("createdby", userid);
            journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);           
            requestParams.put("externalCurrencyRate", externalCurrencyRate);
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return journalEntry;
    }

    public Set<JournalEntryDetail> LMSjournalEntryDetailObject(HashMap<String, Object> requestParams, JSONObject dataMap, JSONArray detailsJSONArray, JournalEntry journalEntry, Receipt receipt, int type) throws SessionExpiredException, ServiceException, AccountingException {
        double amount = 0;
        StringBuffer billno = new StringBuffer();
        Set jedetails = new HashSet();
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            String accid = (String) requestParams.get("accid");
            String jeid = null;
            if (journalEntry != null) {
                jeid = journalEntry.getID();
            }
            HashMap<String, JSONArray> jcustomarrayMap = new HashMap();
            receipt.setJcustomarrayMap(jcustomarrayMap);
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            String accountId = "";
            boolean isCustomer = true;//Boolean.parseBoolean(request.getParameter("iscustomer").toString());
            if (!StringUtil.isNullOrEmpty(accid)) {
                if (isCustomer) {
                    KwlReturnObject resultCVAccount = accountingHandlerDAOobj.getObject(Customer.class.getName(), accid);
                    if (!resultCVAccount.getEntityList().isEmpty()) {
                        Customer customer = (Customer) resultCVAccount.getEntityList().get(0);
                        accountId = customer.getAccount().getID();
                        receipt.setPaymentWindowType(1);
                    }
                }
            }
            JSONArray jArr = new JSONArray();
            if (detailsJSONArray != null) {
                jArr = detailsJSONArray;
            }
            if (jArr.length() > 0 && type == Constants.PaymentAgainstInvoice) {
                amount = 0;
                for (int i = 0; i < jArr.length(); i++) { // Changed for New Customer/Vendor Removed account dependency
                    JSONObject jobj = jArr.getJSONObject(i);
                    JSONArray jArray = new JSONArray();
                    jArray.put(jobj);
                    KwlReturnObject resultInvoice = accountingHandlerDAOobj.getObject(Invoice.class.getName(), jobj.getString("billid"));
                    Invoice invoice = (Invoice) resultInvoice.getEntityList().get(0);
                    amount += jobj.getDouble("amount");
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", jobj.getDouble("amount"));
                    jedjson.put("accountid", invoice.getAccount() != null ? invoice.getAccount().getID() : accountId);
                    if (jobj.optDouble("gstCurrencyRate", 0.0) != 0.0) {
                        jedjson.put("gstCurrencyRate", jobj.optDouble("gstCurrencyRate", 0.0));
                        journalEntry.setGstCurrencyRate(jobj.optDouble("gstCurrencyRate", 0.0));
                        jedjson.put("paymentType", type);
                    }
                    jedjson.put("debit", false);
                    jedjson.put("jeid", jeid);
                    jedjson.put("description", jobj.optString("description"));
                    JournalEntryDetail jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                    jedetails.add(jed);
//                    accJournalEntryobj.saveJournalEntryDetailsSet(jedetails);
                    if ((!jobj.getString("transectionno").equalsIgnoreCase("undefined")) && (!jobj.getString("transectionno").equalsIgnoreCase(""))) {
                        billno.append(jobj.getString("transectionno") + ",");
                    }
                    jArr.getJSONObject(i).put("rowjedid", jed.getID());

                    if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", "[]"))) {
                        JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                        jcustomarrayMap.put(jed.getID(), jcustomarray);
                    }
                    jArr.getJSONObject(i).put("jedetail", jed.getID());
                }
                if (preferences.getForeignexchange() == null) {
                    throw new AccountingException("Please Set Foreign Exchange account in Account Preference first");
                }
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return jedetails;
    }

    public Set<JournalEntryDetail> LMSJournalEntryDetailPaymentMethodObjects(HashMap<String, Object> requestParams, JSONObject dataMap, JSONArray detailsJSONArray, JournalEntry journalEntry, Receipt receipt, int type) throws SessionExpiredException, ServiceException, AccountingException {
        double amount = 0;
        Set jedetails = new HashSet();
        try {
            JSONObject jedjson = null;
            JournalEntryDetail jed = null;
            Account dipositTo = null;
            String companyid = (String) requestParams.get(Constants.companyKey);
            KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), dataMap.getString("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);
            dipositTo = payMethod.getAccount();
            String jeid = null;
            if (journalEntry != null) {
                jeid = journalEntry.getID();
            }
            amount = dataMap.optDouble("amount", 0);
            if (amount != 0) {
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", companyid);
                //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                jedjson.put("amount", amount);
                jedjson.put("accountid", dipositTo.getID());
                jedjson.put("debit", true);
                jedjson.put("jeid", jeid);
                jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                jedetails.add(jed);
                receipt.setDeposittoJEDetail(jed);
                receipt.setDepositAmount(amount);       // put amount excluding bank charges
            }

        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return jedetails;
    }

    public HashSet LMSReceiptDetailObject(HashMap<String, Object> requestParams, JSONArray jSONArrayAgainstInvoice, Receipt receipt, int type) throws SessionExpiredException, ServiceException, AccountingException {
        HashSet payDetails = null;
        GoodsReceipt goodsReceipt = null;
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            JSONArray jArr = new JSONArray();
            if (jSONArrayAgainstInvoice != null) {
                jArr = jSONArrayAgainstInvoice;
            }
            payDetails = saveReceiptRows(receipt, company, jArr, goodsReceipt, type, requestParams);
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return payDetails;
    }

    public PayDetail getLMSPayDetailObject(HashMap<String, Object> requestParams, JSONObject dataMap, Receipt editReceiptObject, Receipt receipt) throws SessionExpiredException, ServiceException, AccountingException {
        PayDetail pdetail = null;

        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), dataMap.getString("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);
            HashMap receipthm = new HashMap();
            receipthm.put("paymethodid", payMethod.getID());
            receipthm.put("companyid", companyid);
            pdetail = accPaymentDAOobj.saveOrUpdatePayDetail(receipthm);
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return pdetail;
    }

    public class EnglishNumberToWords {

    private final String[] tensNames = {
        "", " Ten", " Twenty", " Thirty", " Forty", " Fifty", " Sixty", " Seventy", " Eighty", " Ninety"
    };
    private final String[] numNames = {
        "", " One", " Two", " Three", " Four", " Five", " Six", " Seven", " Eight", " Nine", " Ten", " Eleven", " Twelve",
        " Thirteen", " Fourteen", " Fifteen", " Sixteen", " Seventeen", " Eighteen", " Nineteen"
    };

    private String convertLessThanOneThousand(int number) {
        String soFar;
        if (number % 100 < 20) {
            soFar = numNames[number % 100];
            number /= 100;
        } else {
            soFar = numNames[number % 10];
            number /= 10;
            soFar = tensNames[number % 10] + soFar;
            number /= 10;
        }
        if (number == 0) {
            return soFar;
        }
        return numNames[number] + " Hundred" + soFar;
    }

    private String convertLessOne(int number, KWLCurrency currency) {
        String soFar;
        String val = currency.getAfterDecimalName();
        if (number % 100 < 20) {
            soFar = numNames[number % 100];
            number /= 100;
        } else {
            soFar = numNames[number % 10];
            number /= 10;
            if (Integer.parseInt(currency.getCurrencyID()) == Constants.CountryUSCurrencyId && !soFar.isEmpty()) {
                soFar = tensNames[number % 10] + "-" + soFar.replaceFirst(" ", "");
            } else {
                soFar = tensNames[number % 10] + soFar;
            }
            number /= 10;
        }
        if (number == 0) {
            return " And " + soFar + " " + val;
        }
        return " And " + numNames[number] + " " + val + soFar;
    }
        private String convertLessThanOneThousandWithHypen(int number) {
            String soFar;
            if (number % 100 < 20) {
                soFar = numNames[number % 100];
                number /= 100;
            } else {
                soFar = numNames[number % 10];
                number /= 10;
                if (!soFar.isEmpty()) {
                    soFar = tensNames[number % 10] + "-" + soFar.replaceFirst(" ", "");
                } else {
                    soFar = tensNames[number % 10] + soFar;
                }
                number /= 10;
            }
            if (number == 0) {
                return soFar;
            }
            return numNames[number] + " Hundred" + soFar;
        }

        public String convert(Double number, KWLCurrency currency, int countryLanguageId) {
            if (number == 0) {
                return "Zero";
            }
                String answer = "";
            if (countryLanguageId == Constants.OtherCountryLanguageId) { // For universal conversion of amount in words. i.e. in Billion,trillion etc
                answer = universalConvert(number, currency);
            } else if (countryLanguageId == Constants.CountryIndiaLanguageId) { // For Indian word format.ie. in lakhs, crores
                answer = indianConvert(number, currency);
            }
            return answer;
        }
        
        public String universalConvert(Double number, KWLCurrency currency) {
            boolean isNegative = false;
            if (number < 0) {
                isNegative = true;
                number = -1 * number;
            }
            String snumber = Double.toString(number);
            String mask = "000000000000.00";
            DecimalFormat df = new DecimalFormat(mask);
            snumber = df.format(number);
            int billions = Integer.parseInt(snumber.substring(0, 3));
            int millions = Integer.parseInt(snumber.substring(3, 6));
            int hundredThousands = Integer.parseInt(snumber.substring(6, 9));
            int thousands = Integer.parseInt(snumber.substring(9, 12));
            int fractions = Integer.parseInt(snumber.substring(13, 15));
            String tradBillions;
            switch (billions) {
                case 0:
                    tradBillions = "";
                    break;
                case 1:
                    tradBillions = convertLessThanOneThousand(billions) + " Billion ";
                    break;
                default:
                    tradBillions = convertLessThanOneThousand(billions) + " Billion ";
            }
            String result = tradBillions;

            String tradMillions;
            switch (millions) {
                case 0:
                    tradMillions = "";
                    break;
                case 1:
                    tradMillions = convertLessThanOneThousand(millions) + " Million ";
                    break;
                default:
                    tradMillions = convertLessThanOneThousand(millions) + " Million ";
            }
            result = result + tradMillions;

            String tradHundredThousands;
            switch (hundredThousands) {
                case 0:
                    tradHundredThousands = "";
                    break;
                case 1:
                    tradHundredThousands = "One Thousand ";
                    break;
                default:
                    tradHundredThousands = convertLessThanOneThousand(hundredThousands) + " Thousand ";
            }
            result = result + tradHundredThousands;
            String tradThousand;
             if (Integer.parseInt(currency.getCurrencyID()) == Constants.CountryUSCurrencyId) {
                tradThousand = convertLessThanOneThousandWithHypen(thousands);
            } else {
                tradThousand = convertLessThanOneThousand(thousands);
            }
            result = result + tradThousand;
            String paises;
            switch (fractions) {
                case 0:
                    paises = "";
                    break;
                default:
                    paises = convertLessOne(fractions, currency);
            }
            if (Integer.parseInt(currency.getCurrencyID()) == Constants.CountryUSCurrencyId) {
                result = result + " Dollars " + paises;
            } else {
                result = result + paises; //to be done later
            }
            result = result.replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
            if (isNegative) {
                result = "Minus " + result;
            }
            return result;
        }
        public String indianConvert(Double number, KWLCurrency currency) {
            boolean isNegative = false;
            if (number < 0) {
                isNegative = true;
                number = -1 * number;
            }
            String snumber = Double.toString(number);
            String mask = "000000000000000.00";  //ERP-17681
            DecimalFormat df = new DecimalFormat(mask);
            snumber = df.format(number);

            int n = Integer.parseInt(snumber.substring(0, 15));
            int fractions = Integer.parseInt(snumber.split("\\.").length != 0 ? snumber.split("\\.")[1] : "0");
            if (n == 0) {
                return "Zero";
            }
            String arr1[] = {"One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"};
            String arr2[] = {"Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"};
            String unit[] = {"Arab", "Crore", "Lakh", "Thousand", "Hundred", ""};
            int factor[] = {1000000000, 10000000, 100000, 1000, 100, 1};
            String answer = "", paises = "";
            if (n < 0) {
                answer = "Minus";
                n = -n;
            }
            int quotient, units, tens;
            for (int i = 0; i < factor.length; i++) {
                quotient = n / factor[i];
                if (quotient > 0) {
                    if (quotient < 20) {
                        answer = answer + " " + arr1[quotient - 1];
                    } else {
                        units = quotient % 10;
                        tens = quotient / 10;
                        if (units > 0) {
                            answer = answer + " " + arr2[tens - 2] + " " + arr1[units - 1];
                        } else {
                            answer = answer + " " + arr2[tens - 2] + " ";
                        }
                    }
                    answer = answer + " " + unit[i];
                }
                n = n % factor[i];
            }
            switch (fractions) {
                case 0:
                    paises = "";
                    break;
                default:
                    paises = convertLessOne(fractions, currency);
            }
            answer = answer + paises; //to be done later
            return answer.trim();
        }
    }

    public JSONObject saveReceipt(HttpServletRequest request, HttpServletResponse response) {
        JSONObject jobj = new JSONObject();
        JSONObject paramJObj = new JSONObject();
        try{Enumeration<String> attributes = request.getAttributeNames();        
        while(attributes.hasMoreElements()){
            String attribute = attributes.nextElement();
            paramJObj.put(attribute, request.getAttribute(attribute));            
        }
        Enumeration<String> parameters = request.getParameterNames(); 
        while(parameters.hasMoreElements()){
            String parameter = parameters.nextElement();
            paramJObj.put(parameter, request.getParameter(parameter));                        
        }
        }
        catch(JSONException e){
            e.printStackTrace();
        }
            
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            paramJObj.put(Constants.companyKey, companyid);
            String userId = sessionHandlerImpl.getUserid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            String userfullName = sessionHandlerImpl.getUserFullName(request);
            paramJObj.put("currencyid", currencyid);
            paramJObj.put("gcurrencyid", currencyid);
            paramJObj.put("lid", userId);
            paramJObj.put("userfullname", userfullName);
            paramJObj.put("language",RequestContextUtils.getLocale(request).getLanguage());
            jobj = saveReceiptJson(paramJObj);
            
        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jobj;
    }

    public JSONObject saveReceiptJson(JSONObject paramJobj) throws JSONException, ServiceException{
        JSONObject jobj = new JSONObject();
        HashMap<String, Object> hashMap = saveCustomerReceiptJson(paramJobj);
        jobj = (JSONObject) hashMap.get("jobj");
        return jobj;
    }

    public HashMap<String, Object> saveCustomerReceipt(HttpServletRequest request, HttpServletResponse response) {
                boolean issuccess = false;
        String msg = "";
        String paymentid = "";
        String jeno="";
        String jeid="";
        String billno = "";
        String advanceamount = "";
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        JSONObject paramJObj = new JSONObject();
        String amountpayment = "", accountaddress = "", accountName = "", JENumBer = "";
        try{Enumeration<String> attributes = request.getAttributeNames();        
        while(attributes.hasMoreElements()){
            String attribute = attributes.nextElement();
            paramJObj.put(attribute, request.getAttribute(attribute));            
        }
        Enumeration<String> parameters = request.getParameterNames(); 
        while(parameters.hasMoreElements()){
            String parameter = parameters.nextElement();
            paramJObj.put(parameter, request.getParameter(parameter));                        
        }
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        HashMap<String, Object> hashMap  = null;
            
        try {
                    String companyid = sessionHandlerImpl.getCompanyid(request);
            String userDateFormat = sessionHandlerImpl.getUserDateFormat(request);
            paramJObj.put(Constants.companyKey, companyid);
            String userId = sessionHandlerImpl.getUserid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            
            paramJObj.put("currencyid", currencyid);
            paramJObj.put("gcurrencyid", currencyid);
            paramJObj.put("lid", userId);
            String userfullName = sessionHandlerImpl.getUserFullName(request);
            paramJObj.put("userfullname", userfullName);
            paramJObj.put("language",RequestContextUtils.getLocale(request).getLanguage());
            hashMap = saveCustomerReceiptJson(paramJObj);
            jobj = (JSONObject) hashMap.get("jobj");
        } catch (Exception ex) {
            msg = "" + ex.getMessage();
            issuccess = false;
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally{
                    try {
                        jobj.put("receiptid", paymentid);
                        jobj.put("jeno", jeno);
                        jobj.put("jeid", jeid);
                        jobj.put("number", paramJObj.optString("no", null));//payment number of LMS
                        jobj.put("tid", paramJObj.optString("tid", null));//Transaction id of LMS
                        jobj.put("success", issuccess);
                        jobj.put("msg", msg);
                        jobj.put("paymentid", paymentid);
                        jobj.put("data", jArr);
                        jobj.put("billno", billno);
                        jobj.put("advanceamount", advanceamount);
                        jobj.put("amount", amountpayment);
                        jobj.put("address", accountaddress);
                        jobj.put("accountName", accountName);
                        jobj.put("receipttype", "");
                    } catch (JSONException ex) {
                        Logger.getLogger(AccReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
        }
        
        
        return hashMap;
    }
    
    public HashMap<String, Object> saveCustomerReceiptJson(JSONObject paramJobj) throws JSONException, ServiceException{
        JSONObject jobj = new JSONObject();
        JSONObject jobjDetails = new JSONObject();
        JSONArray jArr = new JSONArray();
        boolean issuccess = false;
        boolean isChequePrint = false;
        String msg = "";
        String paymentid = "";
        String jeno="";
        String jeid="";
        String billno = "";
        String advanceamount = "";
        KwlReturnObject result = null;
        boolean isEdit = false;
        int receipttype = -1;
        String amountpayment = "", accountaddress = "", accountName = "", JENumBer = "";
        HashMap<String, Object> hashMap = new HashMap<String, Object>();
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("BR_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        TransactionStatus status = txnManager.getTransaction(def);
        try { 
            String companyid = paramJobj.getString(Constants.companyKey);
            Receipt editReceiptObject = null;
            String receiptid = paramJobj.optString("billid",null);
            if (!StringUtil.isNullOrEmpty(receiptid)) {
                KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptid);
                editReceiptObject = (Receipt) receiptObj.getEntityList().get(0);
                
                isEdit = editReceiptObject != null ? true : false;
            }
            Receipt receipt = createReceiptObjectJson(paramJobj, editReceiptObject);
            
            /* For Malaysian country, while receiving advance payment from customer, GST may or may not be added. This causes problem in edit case
             * that is why we have set tax and tax amount null initially.  ERP-9810
             */
            receipt.setTax(null);
            receipt.setTaxAmount(0);
            JournalEntry journalEntry = journalEntryObjectJson(paramJobj, editReceiptObject);
            
            JournalEntry oldJE = editReceiptObject != null ? editReceiptObject.getJournalEntry() : null;
            String oldReevalJE = editReceiptObject != null ? editReceiptObject.getRevalJeId() : null;
            PayDetail oldPayDetail = editReceiptObject != null ? editReceiptObject.getPayDetail() : null;
            JournalEntry oldBankChargeJE = editReceiptObject != null ? editReceiptObject.getJournalEntryForBankCharges() != null ? editReceiptObject.getJournalEntryForBankCharges() : null : null;
            JournalEntry oldBankInterestJE = editReceiptObject != null ? editReceiptObject.getJournalEntryForBankInterest() != null ? editReceiptObject.getJournalEntryForBankInterest() : null : null;
            if (oldJE != null) {
                paramJobj.put("oldjeid", oldJE.getID());
            }
            receipt.setJournalEntry(journalEntry);
            List<Receipt> receipts = new ArrayList<Receipt>();
            receipts.add(receipt);
            accJournalEntryobj.saveJournalEntryByObject(journalEntry);

            if (!StringUtil.isNullOrEmpty(paramJobj.optString("customfield"))) {
                JSONArray jcustomarray = new JSONArray(paramJobj.getString("customfield"));
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", journalEntry.getID());
                customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    KwlReturnObject receiptAccJECustomData = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), journalEntry.getID());
                    AccJECustomData accJECustomData = (AccJECustomData) receiptAccJECustomData.getEntityList().get(0);
                    journalEntry.setAccBillInvCustomData(accJECustomData);
                }
            }
            Set<JournalEntryDetail> details = null;
            accReceiptDAOobj.saveReceiptObject(receipts);
            journalEntry.setTransactionId(receipt.getID());
            journalEntry.setTransactionModuleid(Constants.Acc_Receive_Payment_ModuleId);
            String detailsJsonString = paramJobj.optString("detail",null);
            JSONArray jSONArray = new JSONArray(detailsJsonString);
            JSONArray jSONArrayAdvance = new JSONArray();
            JSONArray jSONArrayLocalAdvance = new JSONArray();
            JSONArray jSONArrayExportAdvance = new JSONArray();
            JSONArray jSONArrayAgainstInvoice = new JSONArray();
            JSONArray jSONArrayCNDN = new JSONArray();
            JSONArray jSONArrayGL = new JSONArray();
            for (int i = 0; i < jSONArray.length(); i++) {
                JSONObject jSONObject = jSONArray.getJSONObject(i);
                jSONArrayAgainstInvoice.put(jSONObject);
            }
            if (jSONArrayAgainstInvoice.length() > 0) {
                if (details == null) {
                    details = journalEntryDetailObjectJson(paramJobj, jSONArrayAgainstInvoice, journalEntry, receipt, Constants.PaymentAgainstInvoice);
                } else {
                    details.addAll(journalEntryDetailObjectJson(paramJobj, jSONArrayAgainstInvoice, journalEntry, receipt, Constants.PaymentAgainstInvoice));
                }
                //Save Invoice Details selected in payment
                HashSet receiptDetails = receiptDetailObjectJson(paramJobj, jSONArrayAgainstInvoice, receipt, Constants.PaymentAgainstInvoice);
                receipt.setRows(receiptDetails);
            }

            if (details != null) {
                int sequencecounter = 0;

                details.addAll(journalEntryDetailCommonObjectsJson(paramJobj, jSONArrayCNDN, journalEntry, receipt, Constants.PaymentAgainstCNDN));
            }
            journalEntry.setDetails(details);
            accJournalEntryobj.saveJournalEntryDetailsSet(details);
            PayDetail payDetail = getPayDetailObjectJson(paramJobj, editReceiptObject, receipt);
            receipt.setPayDetail(payDetail);//as cascaded it will be saved automatically
            String customerName = "";
            String action = "made";
            boolean isCopy = StringUtil.isNullOrEmpty(paramJobj.optString("isCopyReceipt")) ? false : Boolean.parseBoolean(paramJobj.optString("isCopyReceipt","false"));
            if (isEdit == true) {
                action = "updated";
            }
            issuccess = true;
            Map<String, Object> auditTrailMap = new HashMap<String, Object>();
            auditTrailMap.put("userid", paramJobj.getString("lid"));
            auditTrailObj.insertAuditLog(AuditAction.RECEIPT_ADDED, "User " + paramJobj.optString("userfullname") + " has " + action + " a receipt " + receipt.getReceiptNumber(), auditTrailMap, receipt.getID());
            txnManager.commit(status);
            paymentid = receipt.getID();
            jeno = receipt.getJournalEntry().getEntryNumber();
            jeid = receipt.getJournalEntry().getID();
            jobj.put("receiptid", paymentid);
            jobj.put("jeno", jeno);
            jobj.put("jeid", jeid);
            jobj.put("number", paramJobj.optString("no", null));//payment number of LMS
            jobj.put("tid", paramJobj.optString("tid", null));//Transaction id of LMS
            jobj.put("success", issuccess);
            jobj.put("msg", msg);
            jobj.put("paymentid", paymentid);
            jobj.put("data", jArr);
            jobj.put("billno", billno);
            jobj.put("advanceamount", advanceamount);
            jobj.put("amount", amountpayment);
            jobj.put("address", accountaddress);
            jobj.put("accountName", accountName);
            jobj.put("receipttype", receipttype);
            hashMap.put("jobj", jobj);
        } catch (SessionExpiredException ex) {            
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (ServiceException ex) {
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch(JSONException ex){
            txnManager.rollback(status);
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw new JSONException(ex);
        }
        catch (Exception ex) {
            txnManager.rollback(status);            
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } 
        return hashMap;
    }

    public Receipt createReceiptObject(HttpServletRequest request, Receipt editReceiptObject) throws SessionExpiredException, ServiceException, AccountingException {
        Receipt receiptObject = null;
        
        JSONObject paramJObj = new JSONObject();
        try{Enumeration<String> attributes = request.getAttributeNames();        
        while(attributes.hasMoreElements()){
            String attribute = attributes.nextElement();
            paramJObj.put(attribute, request.getAttribute(attribute));            
        }
//        System.out.println("attributes ended");
        Enumeration<String> parameters = request.getParameterNames(); 
        while(parameters.hasMoreElements()){
            String parameter = parameters.nextElement();
            paramJObj.put(parameter, request.getParameter(parameter));                        
        }
        
        String companyid = sessionHandlerImpl.getCompanyid(request);
            paramJObj.put(Constants.companyKey, companyid);
            String userId = sessionHandlerImpl.getUserid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            paramJObj.put("currencyid", currencyid);
            paramJObj.put("lid", userId);            
            String userfullName = sessionHandlerImpl.getUserFullName(request);
            paramJObj.put("userfullname", userfullName);
            paramJObj.put("language",RequestContextUtils.getLocale(request).getLanguage());
            receiptObject = createReceiptObjectJson(paramJObj, editReceiptObject);
        }
        catch(JSONException e){
            Logger.getLogger(AccReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        
        return receiptObject;        
    }
public Receipt createReceiptObjectJson(JSONObject paramJobj, Receipt editReceiptObject) throws SessionExpiredException, ServiceException, AccountingException, JSONException {
         KwlReturnObject result = null;
        List list = new ArrayList();
        Invoice invoice = null;
        Customer cust = null;
        Vendor vend = null;
        Receipt receiptObject = null;
        Account dipositTo = null;
        String sequenceformat = paramJobj.optString("sequenceformat",null) != null ? paramJobj.getString("sequenceformat") : "NA";
        String lmsreceiptid = paramJobj.optString("lmsreceiptid",null);
        String companyid = paramJobj.getString(Constants.companyKey);
        double externalCurrencyRate = StringUtil.isNullOrEmpty(paramJobj.optString("externalcurrencyrate")) ? 1 : StringUtil.getDouble(paramJobj.optString("externalcurrencyrate",null));
        DateFormat df = authHandler.getDateOnlyFormat();
        String entryNumber = paramJobj.optString("no",null);
        String methodid = paramJobj.optString("pmtmethod",null);
        double PaymentCurrencyToPaymentMethodCurrencyRate = StringUtil.isNullOrEmpty(paramJobj.optString("paymentCurrencyToPaymentMethodCurrencyExchangeRate")) ? 1 : StringUtil.getDouble(paramJobj.optString("paymentCurrencyToPaymentMethodCurrencyExchangeRate",null));
        paramJobj.put("methodid", methodid);
        int receiptType = StringUtil.getInteger(paramJobj.optString("receipttype",null));
        int actualReceiptType = StringUtil.getInteger(paramJobj.optString("actualReceiptType",null) != null ? paramJobj.get("actualReceiptType").toString() : "0");
        String payDetailID = null;
        HashMap<String, JSONArray> Map1 = new HashMap();
        String jeentryNumber = null;
        HashMap receipthm = new HashMap();
        boolean ismanydbcr = StringUtil.getBoolean(paramJobj.optString("ismanydbcr","false"));
        receipthm.put("ismanydbcr", ismanydbcr);
        receipthm.put("receipttype", receiptType);
        receipthm.put("actualReceiptType", actualReceiptType);
        double bankCharges = 0;
        double bankInterest = 0;
        String accountId = paramJobj.optString("accid",null);
        String bankChargesAccid = paramJobj.optString("bankChargesCmb",null);
        String bankInterestAccid = paramJobj.optString("bankInterestCmb",null);
        boolean onlyAdvance = StringUtil.getBoolean(paramJobj.optString("onlyAdvance","false"));

        String oldjeid = null;
        String Cardid = null;
        String oldChequeNo = "";

        String createdby = paramJobj.getString("lid");
        String modifiedby = createdby;
        long createdon = System.currentTimeMillis();
        long updatedon = System.currentTimeMillis();

        StringBuffer billno = new StringBuffer();
        String paidToid = paramJobj.optString("paidToCmb",null);
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("paidToCmb")) && !StringUtil.isNullOrEmpty(paidToid)) {
            receipthm.put("paidToCmb", paidToid);
        }
        boolean isCustomer = false;
        boolean isVendor = false;
        if (StringUtil.isNullOrEmpty(accountId)) {
            String acccode = paramJobj.optString("acccode",null);
            accountId = accInvoiceDAOObj.getCustomerId(companyid, acccode);
        }
        if (!StringUtil.isNullOrEmpty(accountId)) {
            KwlReturnObject custObj = accountingHandlerDAOobj.getObject(Customer.class.getName(), accountId);
            if (custObj.getEntityList().get(0) != null) {
                cust = (Customer) custObj.getEntityList().get(0);
            }
            if (cust != null) {
                isCustomer = true;
            }
        }
        if (isCustomer) {
            receipthm.put("customerId", accountId);
        }
        paramJobj.put("accid", accountId);
        boolean bankReconsilationEntry = false;
        boolean bankPayment = false;
        Date clearanceDate = null, startDate = null, endDate = null;
        String bankAccountId = "";
        Map<String, Object> bankReconsilationMap = new HashMap<String, Object>();
        boolean editAdvance = false;
        if (editReceiptObject != null) {// for edit case
            oldjeid = editReceiptObject.getJournalEntry().getID();
            JournalEntry jetemp = editReceiptObject.getJournalEntry();

            if (editReceiptObject.getPayDetail() != null) {
                payDetailID = editReceiptObject.getPayDetail().getID();
                if (editReceiptObject.getPayDetail().getCard() != null) {
                    Cardid = editReceiptObject.getPayDetail().getCard().getID();
                }
                if (editReceiptObject.getPayDetail().getCheque() != null) {
                    Cardid = editReceiptObject.getPayDetail().getCheque().getID();
                    oldChequeNo = editReceiptObject.getPayDetail().getCheque().getChequeNo();
                }
            }
        }
        synchronized (this) { //this block is used to generate auto sequence number if number is not duplicate
            String nextAutoNo = "";
            String nextAutoNoInt = "";
            int count = 0;
            if (editReceiptObject != null) {
                if (sequenceformat.equals("NA")) {
                    if (!entryNumber.equals(editReceiptObject.getReceiptNumber())) {
                        result = accReceiptDAOobj.getReceiptFromBillNo(entryNumber, companyid);
                        receipthm.put("entrynumber", entryNumber);
                        receipthm.put("autogenerated", entryNumber.equals(nextAutoNo));
                        count = result.getRecordTotalCount();
                    }
                    if (count > 0) {
                        throw new AccountingException(messageSource.getMessage("acc.field.Receiptnumber", null, Locale.forLanguageTag(paramJobj.getString("language"))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString("language"))));
                    }
                }
            } else {
                if (!sequenceformat.equals("NA")) {
                    boolean seqformat_oldflag = StringUtil.getBoolean(paramJobj.optString("seqformat_oldflag","false"));
                    if (seqformat_oldflag) {
                        nextAutoNo = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_RECEIPT, sequenceformat);
                    } else {
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_RECEIPT, sequenceformat, seqformat_oldflag, new Date(createdon));
                        nextAutoNo = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                        nextAutoNoInt = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                        String datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                        String dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                        receipthm.put(Constants.SEQFORMAT, sequenceformat);
                        receipthm.put(Constants.SEQNUMBER, nextAutoNoInt);
                        receipthm.put(Constants.DATEPREFIX, datePrefix);
                        receipthm.put(Constants.DATESUFFIX, dateSuffix);
                    }
                    entryNumber = nextAutoNo;
                } else {
                    count = 0;
                    if (sequenceformat.equals("NA")) {
                        result = accReceiptDAOobj.getReceiptFromBillNo(entryNumber, companyid);
                        count = result.getRecordTotalCount();
                        if (count > 0) {
                            throw new AccountingException(messageSource.getMessage("acc.field.Receiptnumber", null, Locale.forLanguageTag(paramJobj.getString("language"))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJobj.getString("language"))));
                        }
                    }
                }
                receipthm.put("entrynumber", entryNumber);
                receipthm.put("autogenerated", entryNumber.equals(nextAutoNo));
            }
        }
        if (sequenceformat.equals("NA")) {//In case of NA checks wheather this number can also be generated by a sequence format or not
            List resultList = accCompanyPreferencesObj.checksEntryNumberForSequenceNumber(Constants.Acc_Receive_Payment_ModuleId, entryNumber, companyid);
            if (!resultList.isEmpty()) {
                boolean isvalidEntryNumber = (Boolean) resultList.get(0);
                String formatName = (String) resultList.get(1);
                if (!isvalidEntryNumber) {
                    throw new AccountingException(messageSource.getMessage("acc.common.enterdocumentnumber", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " <b>" + entryNumber + "</b> " + messageSource.getMessage("acc.common.belongsto", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " <b>" + formatName + "</b>. " + messageSource.getMessage("acc.common.plselectseqformat", null, Locale.forLanguageTag(paramJobj.getString("language"))) + " <b>" + formatName + "</b> " + messageSource.getMessage("acc.common.insteadof", null, Locale.forLanguageTag(paramJobj.getString("language"))));
                }
            }
        }
        KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
        Company company = (Company) cmpresult.getEntityList().get(0);

        KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), paramJobj.getString("currencyid"));
        KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
        String currencyid = (paramJobj.optString("currencyid",null) == null ? currency.getCurrencyID() : paramJobj.getString("currencyid"));

        KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
        CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
        if (!StringUtil.isNullOrEmpty(currencyid) && !currencyid.equalsIgnoreCase(paramJobj.getString("currencyid"))) {
            throw new AccountingException("Base currency of LMS and ERP not same");
//                HashMap<String, Object> requestParams = new HashMap<String, Object>();
//                requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
//                requestParams.put("companyid", companyid);
//                Calendar cal = Calendar.getInstance();
//                long billdateValue = (long) Long.parseLong(request.getParameter("creationdate").toString());
//                cal.setTimeInMillis(billdateValue);
//                Date billdateVal = cal.getTime();
//                externalCurrencyRate = accCurrencyobj.getCurrencyToBaseRate(requestParams, currencyid, billdateVal);
//                receipthm.put("externalCurrencyRate", externalCurrencyRate);
        }
        KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
        ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);
        receipthm.put("currencyid", currencyid);
        receipthm.put("externalCurrencyRate", externalCurrencyRate);
        KwlReturnObject payresult = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paramJobj.optString("pmtmethod",null));
        PaymentMethod payMethod = (PaymentMethod) payresult.getEntityList().get(0);
        receipthm.put("memo", paramJobj.optString("memo",null));
        receipthm.put("companyid", company.getCompanyID());
        receipthm.put("createdby", createdby);
        receipthm.put("creationDate", new Date(createdon));
        receipthm.put("modifiedby", modifiedby);
        receipthm.put("createdon", createdon);
        receipthm.put("updatedon", updatedon);
        receipthm.put("isLinkedToClaimedInvoice", !StringUtil.isNullOrEmpty(paramJobj.optString("isLinkedToClaimedInvoice")) ? Boolean.parseBoolean(paramJobj.optString("isLinkedToClaimedInvoice","false")) : false);
        receipthm.put("PaymentCurrencyToPaymentMethodCurrencyRate", PaymentCurrencyToPaymentMethodCurrencyRate);
        receipthm.put("lmsreceiptid", lmsreceiptid);
        if (editReceiptObject != null) {
            receipthm.put("receiptid", editReceiptObject.getID());
        }
        receiptObject = accReceiptDAOobj.getReceiptObj(receipthm);
        return receiptObject;
    }

    public JournalEntry journalEntryObject(HttpServletRequest request, Receipt editReceiptObject) throws SessionExpiredException, ServiceException, AccountingException {
        JournalEntry journalEntry = null;
        
        
        
        JSONObject paramJObj = new JSONObject();
        try{Enumeration<String> attributes = request.getAttributeNames();        
        while(attributes.hasMoreElements()){
            String attribute = attributes.nextElement();
            paramJObj.put(attribute, request.getAttribute(attribute));            
        }
//        System.out.println("attributes ended");
        Enumeration<String> parameters = request.getParameterNames(); 
        while(parameters.hasMoreElements()){
            String parameter = parameters.nextElement();
            paramJObj.put(parameter, request.getParameter(parameter));                        
        }
        
        String companyid = sessionHandlerImpl.getCompanyid(request);
            paramJObj.put(Constants.companyKey, companyid);
            String userId = sessionHandlerImpl.getUserid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            paramJObj.put("currencyid", currencyid);
            paramJObj.put("lid", userId);
            journalEntry = journalEntryObjectJson(paramJObj, editReceiptObject);
        }
        catch(JSONException e){
            Logger.getLogger(AccReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, e);
        }
        
        return journalEntry;
    }

    
    public JournalEntry journalEntryObjectJson(JSONObject paramJobj, Receipt editReceiptObject) throws SessionExpiredException, ServiceException, AccountingException {
        JournalEntry journalEntry = null;
        try {
            Date billdateVal = null;
            String companyid = paramJobj.getString(Constants.companyKey);
            DateFormat df = authHandler.getDateOnlyFormat();
            String currencyid = paramJobj.getString("currencyid");
            String userid = paramJobj.getString("lid");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (paramJobj.optString("currencyid",null) == null ? currency.getCurrencyID() : paramJobj.getString("currencyid"));
            String methodid = paramJobj.optString("pmtmethod",null);
            double PaymentCurrencyToPaymentMethodCurrencyRate = StringUtil.isNullOrEmpty(paramJobj.optString("paymentCurrencyToPaymentMethodCurrencyExchangeRate")) ? 1 : StringUtil.getDouble(paramJobj.optString("paymentCurrencyToPaymentMethodCurrencyExchangeRate",null));
            boolean ismulticurrencypaymentje = StringUtil.getBoolean(paramJobj.optString("ismulticurrencypaymentje","false"));
            paramJobj.put("methodid", methodid);
            String jeid = null;
            String jeentryNumber = null;
            boolean jeautogenflag = false;
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";
            String jeSeqFormatId = "";
            double externalCurrencyRate = StringUtil.isNullOrEmpty(paramJobj.optString("externalcurrencyrate")) ? 1 : StringUtil.getDouble(paramJobj.optString("externalcurrencyrate",null));

            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            Map<String, Object> jeDataMap = AccountingManager.getGlobalParamsJson(paramJobj);
            
            try {
                billdateVal = df.parse(paramJobj.get("creationdateStr").toString());
                jeDataMap.put("entrydate", billdateVal);
            } catch (ParseException ex) {
                Calendar cal = Calendar.getInstance();
                long billdateValue = (long) Long.parseLong(paramJobj.get("creationdate").toString());
                cal.setTimeInMillis(billdateValue);
                billdateVal = cal.getTime();
                try {
                    billdateVal = df.parse(df.format(billdateVal));
                    jeDataMap.put("entrydate", billdateVal);
                } catch (ParseException ex1) {
                    jeDataMap.put("entrydate", billdateVal);
                }
            }
            
            if (editReceiptObject == null) {
                synchronized (this) {
                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", "autojournalentry");
                    JEFormatParams.put("companyid", companyid);
                    JEFormatParams.put("isdefaultFormat", true);

                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false,billdateVal);
                    jeentryNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    jeIntegerPart = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                    jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);//Date After Prefix Part
                    jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    
                    jeSeqFormatId = format.getID();
                    jeautogenflag = true;

                    jeDataMap.put("entrynumber", jeentryNumber);
                    jeDataMap.put("autogenerated", jeautogenflag);
                    jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
                    jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
                    jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
                    jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
                    jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);
                }
            } else if (editReceiptObject != null && editReceiptObject.getJournalEntry() != null) {
                JournalEntry entry = editReceiptObject.getJournalEntry();
                jeid = editReceiptObject.getJournalEntry().getID();
                jeDataMap.put("entrynumber", entry.getEntryNumber());
                jeDataMap.put("autogenerated", entry.isAutoGenerated());
                jeDataMap.put(Constants.SEQFORMAT, entry.getSeqformat().getID());
                jeDataMap.put(Constants.SEQNUMBER, entry.getSeqnumber());
                jeDataMap.put(Constants.DATEPREFIX, entry.getDatePreffixValue());
                jeDataMap.put(Constants.DATEAFTERPREFIX, entry.getDateAfterPreffixValue());
                jeDataMap.put(Constants.DATESUFFIX, entry.getDateSuffixValue());
            }
            
            if (!StringUtil.isNullOrEmpty(currencyid) && !currencyid.equalsIgnoreCase(paramJobj.getString("currencyid"))) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("gcurrencyid", paramJobj.getString("currencyid"));
                requestParams.put("companyid", companyid);
                externalCurrencyRate = accCurrencyobj.getCurrencyToBaseRate(requestParams, currencyid, billdateVal);
                jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            }
//            jeDataMap.put("entrydate", df.parse(request.getParameter("creationdate")));
            jeDataMap.put("companyid", company.getCompanyID());
            jeDataMap.put("memo", paramJobj.optString("memo",null));
            jeDataMap.put("currencyid", currencyid);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeDataMap.put("PaymentCurrencyToPaymentMethodCurrencyRate", PaymentCurrencyToPaymentMethodCurrencyRate);
            jeDataMap.put("ismulticurrencypaymentje", ismulticurrencypaymentje);
            jeDataMap.put("createdby", userid);
            journalEntry = accJournalEntryobj.getJournalEntry(jeDataMap);
            paramJobj.put("externalCurrencyRate", externalCurrencyRate);
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return journalEntry;
    }

    public Set<JournalEntryDetail> journalEntryDetailObject(HttpServletRequest request, JSONArray detailsJSONArray, JournalEntry journalEntry, Receipt receipt, int type) throws SessionExpiredException, ServiceException, AccountingException {
        
        double amount = 0;
        StringBuffer billno = new StringBuffer();
        Set jedetails = new HashSet();
        JSONObject paramJObj = new JSONObject();
        try{Enumeration<String> attributes = request.getAttributeNames();        
        while(attributes.hasMoreElements()){
            String attribute = attributes.nextElement();
            paramJObj.put(attribute, request.getAttribute(attribute));            
        }
//        System.out.println("attributes ended");
        Enumeration<String> parameters = request.getParameterNames(); 
        while(parameters.hasMoreElements()){
            String parameter = parameters.nextElement();
            paramJObj.put(parameter, request.getParameter(parameter));                        
        }
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        
        jedetails = journalEntryDetailObjectJson(paramJObj, detailsJSONArray, journalEntry, receipt, type);
        return jedetails;
    }

    public Set<JournalEntryDetail> journalEntryDetailObjectJson(JSONObject paramJobj, JSONArray detailsJSONArray, JournalEntry journalEntry, Receipt receipt, int type) throws SessionExpiredException, ServiceException, AccountingException {
        double amount = 0;
        StringBuffer billno = new StringBuffer();
        Set jedetails = new HashSet();
        try {
            Account dipositTo = null;
            double amountDiff = 0;
            boolean rateDecreased = false;
            DateFormat df = authHandler.getDateOnlyFormat();
            String companyid = paramJobj.getString(Constants.companyKey);
            double externalCurrencyRate = StringUtil.isNullOrEmpty(paramJobj.optString("externalcurrencyrate")) ? 1 : StringUtil.getDouble(paramJobj.optString("externalcurrencyrate",null));
            String currencyid = paramJobj.optString("currencyid",null);
            String methodid = paramJobj.optString("pmtmethod",null);
            paramJobj.put("methodid", methodid);

            String jeid = null;
            if (journalEntry != null) {
                jeid = journalEntry.getID();
            }
            HashMap<String, JSONArray> jcustomarrayMap = new HashMap();
            receipt.setJcustomarrayMap(jcustomarrayMap);
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (paramJobj.optString("currencyid",null) == null ? currency.getCurrencyID() : paramJobj.getString("currencyid"));

            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);

            KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paramJobj.optString("pmtmethod",null));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

            dipositTo = payMethod.getAccount();

            String accountId = "";
            boolean isCustomer = true;//Boolean.parseBoolean(request.getParameter("iscustomer").toString());
//            if (!StringUtil.isNullOrEmpty(paramJobj.optString("accid"))) {
//                if (isCustomer) {
//                    KwlReturnObject resultCVAccount = accountingHandlerDAOobj.getObject(Customer.class.getName(), paramJobj.getString("accid"));
//                    if (!resultCVAccount.getEntityList().isEmpty()) {
//                        Customer customer = (Customer) resultCVAccount.getEntityList().get(0);
//                        accountId = customer.getAccount().getID();
//                        receipt.setPaymentWindowType(1);
//                    }
//                }
//            }
            String detail = paramJobj.optString("detail",null);
            JSONArray jArr = new JSONArray();
            if (detailsJSONArray != null) {
                jArr = detailsJSONArray;
            }
            if (jArr.length() > 0 && type == Constants.PaymentAgainstInvoice) {
                amount = 0;
                for (int i = 0; i < jArr.length(); i++) { // Changed for New Customer/Vendor Removed account dependency
                    JSONObject jobj = jArr.getJSONObject(i);
                    JSONArray jArray = new JSONArray();
                    jArray.put(jobj);
                    KwlReturnObject resultInvoice = accountingHandlerDAOobj.getObject(Invoice.class.getName(), jobj.getString("billid"));
                    Invoice invoice = (Invoice) resultInvoice.getEntityList().get(0);
                    double amountDiffforInv = oldReceiptRowsAmount(paramJobj, jArray, currencyid, externalCurrencyRate);
                    rateDecreased = false;
                    if (amountDiffforInv < 0) {
                        rateDecreased = true;
                    }
                    amount += jobj.getDouble("amount");
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", jobj.getDouble("amount") - amountDiffforInv);
                    jedjson.put("accountid", invoice.getAccount() != null ? invoice.getAccount().getID() : accountId);
                    if (jobj.optDouble("gstCurrencyRate", 0.0) != 0.0) {
                        jedjson.put("gstCurrencyRate", jobj.optDouble("gstCurrencyRate", 0.0));
                        journalEntry.setGstCurrencyRate(jobj.optDouble("gstCurrencyRate", 0.0));
                        jedjson.put("paymentType", type);
                    }
                    jedjson.put("forexGainLoss", amountDiffforInv);
                    jedjson.put("debit", false);
                    jedjson.put("jeid", jeid);
                    jedjson.put("description", jobj.optString("description"));
                    JournalEntryDetail jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                    jedetails.add(jed);
//                    accJournalEntryobj.saveJournalEntryDetailsSet(jedetails);
                    String transactionno = null;
                    if(jobj.has("transactionno")){
                        transactionno = jobj.getString("transactionno");
                    }
                    else if(jobj.has("transectionno")){
                        transactionno = jobj.getString("transectionno");
                    }
                    if (transactionno !=null && (!transactionno.equalsIgnoreCase("undefined")) && (!transactionno.equalsIgnoreCase(""))) {
                        billno.append(transactionno + ",");
                    }
                    jArr.getJSONObject(i).put("rowjedid", jed.getID());

                    if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", "[]"))) {
                        JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                        jcustomarrayMap.put(jed.getID(), jcustomarray);
                    }
                    jArr.getJSONObject(i).put("jedetail", jed.getID());
                }

                amountDiff = oldReceiptRowsAmount(paramJobj, jArr, currencyid, externalCurrencyRate);

                if (preferences.getForeignexchange() == null) {
                    throw new AccountingException(messageSource.getMessage("acc.receipt.forex", null, Locale.forLanguageTag(paramJobj.getString("language"))));
                }
                if (amountDiff != 0 && preferences.getForeignexchange() != null && Math.abs(amountDiff) >= 0.000001) {//Math.abs(amountDiff) < .0000001 Added this because in case of revaluation the diff in the rates was less than .0000001 and the amount calculation was going in Exachange rate account. 
                    if (amountDiff < 0) {
                        rateDecreased = true;
                    }
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                    jedjson.put("accountid", preferences.getForeignexchange().getID());
                    if (journalEntry.getGstCurrencyRate() != 0.0) {
                        jedjson.put("forexGainLoss", rateDecreased ? (-1 * amountDiff) : amountDiff);
                        jedjson.put("paymentType", type);
                    }
                    jedjson.put("debit", rateDecreased ? true : false);
                    jedjson.put("jeid", jeid);
                    JournalEntryDetail jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                    jedetails.add(jed);
//                    accJournalEntryobj.saveJournalEntryDetailsSet(jedetails);
                }

            } else {
                amount = Double.parseDouble(paramJobj.optString("amount",null));
            }
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return jedetails;
    }
    
    public HashSet receiptDetailObject(HttpServletRequest request, JSONArray jSONArrayAgainstInvoice, Receipt receipt, int type) throws SessionExpiredException, ServiceException, AccountingException {
        List list = new ArrayList();
        HashSet payDetails = null;
        GoodsReceipt goodsReceipt = null;
        JSONObject paramJObj = new JSONObject();
        try{Enumeration<String> attributes = request.getAttributeNames();        
        while(attributes.hasMoreElements()){
            String attribute = attributes.nextElement();
            paramJObj.put(attribute, request.getAttribute(attribute));            
        }
//        System.out.println("attributes ended");
        Enumeration<String> parameters = request.getParameterNames(); 
        while(parameters.hasMoreElements()){
            String parameter = parameters.nextElement();
            paramJObj.put(parameter, request.getParameter(parameter));                        
        }
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        
        payDetails = receiptDetailObjectJson(paramJObj, jSONArrayAgainstInvoice, receipt, type);
        return payDetails;
    }

    public HashSet receiptDetailObjectJson(JSONObject paramJobj, JSONArray jSONArrayAgainstInvoice, Receipt receipt, int type) throws SessionExpiredException, ServiceException, AccountingException {
        List list = new ArrayList();
        HashSet payDetails = null;
        GoodsReceipt goodsReceipt = null;
        try {
            HashMap<String,Object> requestMap=AccountingManager.getGlobalParamsJson(paramJobj);
            requestMap.put("mapWithFieldType", paramJobj.optString("ismultidebit",null));
            String companyid = paramJobj.getString(Constants.companyKey);
            boolean isMultiDebit = StringUtil.getBoolean(paramJobj.optString("ismultidebit","false"));
            String methodid = paramJobj.optString("pmtmethod",null);
            paramJobj.put("methodid", methodid);
            HashMap<String, JSONArray> Map1 = new HashMap();

            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            JSONArray jArr = new JSONArray();
            if (jSONArrayAgainstInvoice != null) {
                jArr = jSONArrayAgainstInvoice;
            }
            payDetails = saveReceiptRows(receipt, company, jArr, goodsReceipt, type, requestMap);
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return payDetails;
    }

    
    public Set<JournalEntryDetail> journalEntryDetailCommonObjects(HttpServletRequest request, JSONArray detailsJSONArray, JournalEntry journalEntry, Receipt receipt, int type) throws SessionExpiredException, ServiceException, AccountingException {
        double amount = 0;
        double amountIncludingBankCharges = 0;
        Set jedetails = new HashSet();
        try {
            JSONObject jedjson = null;
            JournalEntryDetail jed = null;
            Account dipositTo = null;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            double bankCharges = 0;
            double bankInterest = 0;
            boolean onlyAdvance = StringUtil.getBoolean(request.getParameter("onlyAdvance"));
            String bankChargesAccid = request.getParameter("bankChargesCmb");
            String bankInterestAccid = request.getParameter("bankInterestCmb");
            HashMap<String, JSONArray> Map1 = new HashMap();
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));

            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);

            KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

            dipositTo = payMethod.getAccount();

            String jeid = null;
            if (journalEntry != null) {
                jeid = journalEntry.getID();
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("bankCharges")) && !StringUtil.isNullOrEmpty(bankChargesAccid)) {
                bankCharges = Double.parseDouble(request.getParameter("bankCharges"));
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("bankInterest")) && !StringUtil.isNullOrEmpty(bankInterestAccid)) {
                bankInterest = Double.parseDouble(request.getParameter("bankInterest"));
            }
            amount = Double.parseDouble(request.getParameter("amount"));
            amountIncludingBankCharges = amount;
            //All Fore
            if (amount != 0) {
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", companyid);
                //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                jedjson.put("amount", amount);
                jedjson.put("accountid", dipositTo.getID());
                jedjson.put("debit", true);
                jedjson.put("jeid", jeid);
                jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                jedetails.add(jed);
                receipt.setDeposittoJEDetail(jed);
                receipt.setDepositAmount(amount);       // put amount excluding bank charges
            }

        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return jedetails;
    }

    public Set<JournalEntryDetail> journalEntryDetailCommonObjectsJson(JSONObject paramJobj, JSONArray detailsJSONArray, JournalEntry journalEntry, Receipt receipt, int type) throws SessionExpiredException, ServiceException, AccountingException {
        double amount = 0;
        double amountIncludingBankCharges = 0;
        Set jedetails = new HashSet();
        try {
            JSONObject jedjson = null;
            JournalEntryDetail jed = null;
            Account dipositTo = null;
            String companyid = paramJobj.getString(Constants.companyKey);
            String currencyid = paramJobj.optString("currencyid",null);
            double bankCharges = 0;
            double bankInterest = 0;
            boolean onlyAdvance = StringUtil.getBoolean(paramJobj.optString("onlyAdvance","false"));
            String bankChargesAccid = paramJobj.optString("bankChargesCmb",null);
            String bankInterestAccid = paramJobj.optString("bankInterestCmb",null);
            HashMap<String, JSONArray> Map1 = new HashMap();
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (paramJobj.optString("currencyid",null) == null ? currency.getCurrencyID() : paramJobj.getString("currencyid"));

            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);

            KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paramJobj.optString("pmtmethod",null));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

            dipositTo = payMethod.getAccount();

            String jeid = null;
            if (journalEntry != null) {
                jeid = journalEntry.getID();
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("bankCharges")) && !StringUtil.isNullOrEmpty(bankChargesAccid)) {
                bankCharges = Double.parseDouble(paramJobj.optString("bankCharges",null));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("bankInterest")) && !StringUtil.isNullOrEmpty(bankInterestAccid)) {
                bankInterest = Double.parseDouble(paramJobj.optString("bankInterest",null));
            }
            amount = Double.parseDouble(paramJobj.optString("amount",null));
            amountIncludingBankCharges = amount;
            //All Fore
            if (amount != 0) {
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", companyid);
                //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                jedjson.put("amount", amount);
                jedjson.put("accountid", dipositTo.getID());
                jedjson.put("debit", true);
                jedjson.put("jeid", jeid);
                jed = accJournalEntryobj.getJournalEntryDetails(jedjson);
                jedetails.add(jed);
                receipt.setDeposittoJEDetail(jed);
                receipt.setDepositAmount(amount);       // put amount excluding bank charges
            }

        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return jedetails;
    }
    
    private HashSet saveReceiptRows(Receipt receipt, Company company, JSONArray jArr, GoodsReceipt greceipt, int type, HashMap<String, Object> requestParams) throws JSONException, ServiceException, UnsupportedEncodingException {
        HashSet details = new HashSet();
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            jobj.put("exchangeratefortransaction", 1);
            String companyid = company.getCompanyID();
            ReceiptDetail rd = new ReceiptDetail();
            rd.setSrno(i + 1);
            rd.setID(StringUtil.generateUUID());
            double amountReceived = jobj.getDouble("amount");// amount in receipt currency
            double amountReceivedConverted = jobj.getDouble("amount"); // amount in invoice currency
            rd.setAmount(jobj.getDouble("amount"));
            rd.setCompany(company);
            KwlReturnObject result = accountingHandlerDAOobj.getObject(Invoice.class.getName(), jobj.getString("billid"));
            Invoice invoice = (Invoice) result.getEntityList().get(0);
            rd.setInvoice((Invoice) result.getEntityList().get(0));
            if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(invoice.getCurrency().getCurrencyID()) && !invoice.getCurrency().getCurrencyID().equals(receipt.getCurrency().getCurrencyID())) {
                rd.setExchangeRateForTransaction(Double.parseDouble(jobj.get("exchangeratefortransaction").toString()));
                rd.setFromCurrency(invoice.getCurrency());
                rd.setToCurrency(receipt.getCurrency());
                double adjustedRate = jobj.optDouble("amountdue", 0) / jobj.optDouble("amountDueOriginal", 0);
                amountReceivedConverted = amountReceived / adjustedRate;;
                amountReceivedConverted = authHandler.round(amountReceivedConverted, companyid);
                rd.setAmountInInvoiceCurrency(amountReceivedConverted);
                rd.setAmountDueInInvoiceCurrency(jobj.optDouble("amountDueOriginal", 0));
                rd.setAmountDueInPaymentCurrency(jobj.optDouble("amountdue", 0));
            } else {
                rd.setExchangeRateForTransaction(Double.parseDouble(jobj.get("exchangeratefortransaction").toString()));
                rd.setFromCurrency(invoice.getCurrency());
                rd.setToCurrency(receipt.getCurrency());
                amountReceivedConverted = authHandler.round(amountReceived, companyid);
                rd.setAmountInInvoiceCurrency(amountReceivedConverted);
                rd.setAmountDueInInvoiceCurrency(jobj.optDouble("amountDueOriginal", 0));
                rd.setAmountDueInPaymentCurrency(jobj.optDouble("amountdue", 0));
            }
            rd.setGstCurrencyRate(jobj.optDouble("gstCurrencyRate", 0.0));
            rd.setDescription(StringUtil.DecodeText(jobj.optString("description", "")));
            if (greceipt != null) {
                rd.setGoodsReceipt(greceipt);
            }
            rd.setReceipt(receipt);
            if (jobj.has("rowjedid")) {
                rd.setROWJEDID(jobj.getString("rowjedid"));
            }
            if (jobj.has("srNoForRow")) {
                int srNoForRow = StringUtil.isNullOrEmpty("srNoForRow") ? 0 : Integer.parseInt(jobj.getString("srNoForRow"));
                rd.setSrNoForRow(srNoForRow);
            }
            if (jobj.has("jedetail") && jobj.get("jedetail") != null) {
                KwlReturnObject resJED = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), (String) jobj.get("jedetail"));
                JournalEntryDetail jedObj = (JournalEntryDetail) resJED.getEntityList().get(0);
                rd.setTotalJED(jedObj);
            }
            HashMap<String, JSONArray> jcustomarrayMap = receipt.getJcustomarrayMap();
            JSONArray jcustomarray = new JSONArray();
            if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
            }
            if (!StringUtil.isNullOrEmpty(jobj.optString("customfieldArray", ""))) {
                jcustomarray = null;
                String customfieldArray = jobj.optString("customfieldArray", ""); //Custom Data from other Project
                customfieldArray = StringUtil.DecodeText(customfieldArray);
                if (!StringUtil.isNullOrEmpty(customfieldArray)) {
                    HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                    String mapWithFieldType = (String)requestParams.get("mapWithFieldType");
                    customrequestParams.put("customarray", customfieldArray);
                    customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                    customrequestParams.put("companyid", companyid);
                    customrequestParams.put("mapWithFieldType", mapWithFieldType);
                    jcustomarray = fieldDataManagercntrl.createJSONArrForCustomFieldValueFromOtherSource(customrequestParams);
                }
            }

            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
            customrequestParams.put("customarray", jcustomarray);
            customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
            customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
            customrequestParams.put("modulerecid", rd.getROWJEDID());
            customrequestParams.put("recdetailId", rd.getID());
            customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
            customrequestParams.put("companyid", companyid);
            customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                KwlReturnObject receiptAccJEDCustomData = accountingHandlerDAOobj.getObject(AccJEDetailCustomData.class.getName(), rd.getROWJEDID());
                AccJEDetailCustomData accJEDetailCustomData = (AccJEDetailCustomData) receiptAccJEDCustomData.getEntityList().get(0);
                KwlReturnObject receiptJED = accountingHandlerDAOobj.getObject(JournalEntryDetail.class.getName(), rd.getROWJEDID());
                JournalEntryDetail journalEntryDetail = (JournalEntryDetail) receiptJED.getEntityList().get(0);
                journalEntryDetail.setAccJEDetailCustomData(accJEDetailCustomData);
            }
            details.add(rd);
            double amountReceivedConvertedInBaseCurrency = 0d;
            HashMap<String, Object> requestMap = new HashMap();
            requestMap.put(Constants.companyid, company.getCompanyID());
            requestMap.put("gcurrencyid", company.getCurrency().getCurrencyID());

            /*
             * Amount received against Invoice will be converted to base currency as per spot rate of INvoice 
             */
            double grExternalCurrencyRate = 0d;
            Date grCreationDate = null;
            grCreationDate = invoice.getCreationDate();
            if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                grExternalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
            } else {
//                grCreationDate = invoice.getJournalEntry().getEntryDate();
                grExternalCurrencyRate = invoice.getJournalEntry().getExternalCurrencyRate();
            }
            String fromcurrencyid = invoice.getCurrency().getCurrencyID();
            KwlReturnObject bAmt = null;
            if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                if (invoice.isConversionRateFromCurrencyToBase()) {
                    grExternalCurrencyRate = 1 / grExternalCurrencyRate;
                }
            }
            bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestMap, amountReceivedConverted, fromcurrencyid, grCreationDate, grExternalCurrencyRate);
            amountReceivedConvertedInBaseCurrency = (Double) bAmt.getEntityList().get(0);
            amountReceivedConvertedInBaseCurrency = authHandler.round(amountReceivedConvertedInBaseCurrency, companyid);
            rd.setAmountInBaseCurrency(amountReceivedConvertedInBaseCurrency);
            updateInvoiceAmountDue(invoice, receipt, company, amountReceivedConverted, amountReceivedConvertedInBaseCurrency);
            if (receipt != null) {
                double receiptAmountDue = receipt.getOpeningBalanceAmountDue();
                receiptAmountDue -= amountReceived;
                receipt.setOpeningBalanceAmountDue(receiptAmountDue);
                receipt.setOpeningBalanceBaseAmountDue(receipt.getOpeningBalanceBaseAmountDue() - amountReceivedConvertedInBaseCurrency);
            }
        }
        return details;
    }

    public PayDetail getPayDetailObject(HttpServletRequest request, Receipt editReceiptObject, Receipt receipt) throws SessionExpiredException, ServiceException, AccountingException {
        List list = new ArrayList();
        String oldChequeNo = "";
        PayDetail pdetail = null;

        try {
            String detailsJsonString = request.getParameter("Details");;
//            JSONArray jSONArray = new JSONArray(detailsJsonString);
            Account dipositTo = null;
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormatter(request);
            String currencyid = sessionHandlerImpl.getCurrencyID(request);
            String receiptid = request.getParameter("billid");
            String methodid = request.getParameter("pmtmethod");
            sessionHandlerImpl.updatePaymentMethodID(request, methodid);
            boolean bankReconsilationEntry = false, bankPayment = false;
            Date clearanceDate = null, startDate = null, endDate = null;
            String bankAccountId = "";
            String payDetailID = null;
            JournalEntry oldJE = editReceiptObject != null ? editReceiptObject.getJournalEntry() : null;

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (request.getParameter("currencyid") == null ? currency.getCurrencyID() : request.getParameter("currencyid"));

            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);

            KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), request.getParameter("pmtmethod"));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

            dipositTo = payMethod.getAccount();
            HashMap receipthm = new HashMap();
            receipthm.put("paymethodid", payMethod.getID());
            receipthm.put("companyid", companyid);
            pdetail = accPaymentDAOobj.saveOrUpdatePayDetail(receipthm);
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return pdetail;
    }
    public PayDetail getPayDetailObjectJson(JSONObject paramJobj, Receipt editReceiptObject, Receipt receipt) throws SessionExpiredException, ServiceException, AccountingException {
        List list = new ArrayList();
        String oldChequeNo = "";
        PayDetail pdetail = null;

        try {
            
//            JSONArray jSONArray = new JSONArray(detailsJsonString);
            Account dipositTo = null;
            String companyid = paramJobj.getString(Constants.companyKey);
            DateFormat df = authHandler.getDateOnlyFormat();
            String currencyid = paramJobj.optString("currencyid",null);
            String receiptid = paramJobj.optString("billid",null);
            String methodid = paramJobj.optString("pmtmethod",null);
            paramJobj.put("methodid", methodid);
            boolean bankReconsilationEntry = false, bankPayment = false;
            Date clearanceDate = null, startDate = null, endDate = null;
            String bankAccountId = "";
            String payDetailID = null;
            JournalEntry oldJE = editReceiptObject != null ? editReceiptObject.getJournalEntry() : null;

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            currencyid = (paramJobj.optString("currencyid",null) == null ? currency.getCurrencyID() : paramJobj.getString("currencyid"));

            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

            KwlReturnObject extracapresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracapresult.getEntityList().get(0);

            KwlReturnObject result = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paramJobj.optString("pmtmethod",null));
            PaymentMethod payMethod = (PaymentMethod) result.getEntityList().get(0);

            dipositTo = payMethod.getAccount();
            HashMap receipthm = new HashMap();
            receipthm.put("paymethodid", payMethod.getID());
            receipthm.put("companyid", companyid);
            pdetail = accPaymentDAOobj.saveOrUpdatePayDetail(receipthm);
        } catch (Exception e) {
            throw ServiceException.FAILURE("savePayment : " + e.getMessage(), e);
        }
        return pdetail;
    }
    public List saveReceipt(HttpServletRequest request) throws ServiceException, SessionExpiredException, AccountingException {
        KwlReturnObject result = null;
        Receipt receipt = null;
        String oldjeid = null;
        String Cardid = null;
        String netinword = "";
        double amount = 0;
        List ll = new ArrayList();
        GoodsReceipt greceipt = null;
        Customer cust = null;
        Vendor vend = null;
        String accountId = "";
        String receiptid = "";
        
        JSONObject paramJObj = new JSONObject();
        try{Enumeration<String> attributes = request.getAttributeNames();        
        while(attributes.hasMoreElements()){
            String attribute = attributes.nextElement();
            paramJObj.put(attribute, request.getAttribute(attribute));            
        }
//        System.out.println("attributes ended");
        Enumeration<String> parameters = request.getParameterNames(); 
        while(parameters.hasMoreElements()){
            String parameter = parameters.nextElement();
            paramJObj.put(parameter, request.getParameter(parameter));                        
        }
        }
        catch(JSONException e){
            e.printStackTrace();
        }
        
        
        try {
            Account dipositTo = null;
            double amountDiff = 0;
            boolean rateDecreased = false;
            String sequenceformat = paramJObj.optString("sequenceformat",null) != null ? paramJObj.getString("sequenceformat") : "NA";
            String companyid = paramJObj.optString(Constants.companyKey);
            double externalCurrencyRate = StringUtil.getDouble(paramJObj.optString("externalcurrencyrate",null));
            DateFormat df = authHandler.getDateOnlyFormat();
            String entryNumber = paramJObj.optString("no",null);
            String customfield = paramJObj.optString("customfield",null);
            receiptid = paramJObj.optString("billid",null);

            boolean otherwise = ((paramJObj.optString("otherwise",null) != null) ? Boolean.parseBoolean(paramJObj.optString("otherwise","false")) : false);
            String methodid = paramJObj.optString("pmtmethod",null);
            String advancePaymentIdForCnDn = paramJObj.optString("advancePaymentIdForCnDn",null);
            String mainPaymentForCNDNId = paramJObj.optString("mainPaymentForCNDNId",null);
            String invoiceadvcndntype = paramJObj.optString("invoiceadvcndntype",null);
            paramJObj.put("methodid", methodid);
            boolean isMultiDebit = StringUtil.getBoolean(paramJObj.optString("ismultidebit","false"));
            boolean isAdvancePayment = StringUtil.getBoolean(paramJObj.optString("isadvpayment","false"));
            boolean isadvanceFromVendor = StringUtil.getBoolean(paramJObj.optString("isadvanceFromVendor","false"));
            boolean isCNDN = StringUtil.getBoolean(paramJObj.optString("isCNDN","false"));
            boolean isAgainstDN = StringUtil.getBoolean(paramJObj.optString("isAgainstDN","false"));
            boolean ignoreDuplicateChk = StringUtil.getBoolean(paramJObj.optString("ignoreDuplicateChk","false"));
            int receiptType = StringUtil.getInteger(paramJObj.optString("receipttype",null));
            int actualReceiptType = StringUtil.getInteger(paramJObj.optString("actualReceiptType",null) != null ? paramJObj.getString("actualReceiptType") : "0");
            boolean isReceiptPaymentEdit = (Boolean.parseBoolean((String) paramJObj.optString("isReceiptEdit","false")));
            String drAccDetails = paramJObj.optString("detail");
            String jeid = null;
            boolean jeautogenflag = false;
            String jeSeqFormatId = "";
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";
            String payDetailID = null;
            HashMap<String, JSONArray> Map1 = new HashMap();
            String jeentryNumber = null;
            Date billdateVal = null;
            try {
                billdateVal = df.parse(paramJObj.get("creationdate").toString());
            } catch (ParseException ex) {
                Calendar cal = Calendar.getInstance();
                long billdateValue = (long) Long.parseLong(paramJObj.get("creationdate").toString());
                cal.setTimeInMillis(billdateValue);
                billdateVal = cal.getTime();
                try {
                    billdateVal = df.parse(df.format(billdateVal));
                } catch (ParseException ex1) {
                }
            }
            
            HashMap receipthm = new HashMap();
            receipthm.put("isadvancepayment", isAdvancePayment);
            receipthm.put("isadvanceFromVendor", isadvanceFromVendor);
            if (!StringUtil.isNullOrEmpty(advancePaymentIdForCnDn)) {
                receipthm.put("advancePaymentIdForCnDn", advancePaymentIdForCnDn);
            }
            if (!StringUtil.isNullOrEmpty(mainPaymentForCNDNId)) {
                receipthm.put("mainPaymentForCNDNId", mainPaymentForCNDNId);
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("isEdit"))) {
                receipthm.put("isEdit", Boolean.parseBoolean(paramJObj.optString("isEdit","false")));
            }
            if (!StringUtil.isNullOrEmpty(invoiceadvcndntype) && (actualReceiptType == 0 || actualReceiptType == 1)) {
                receipthm.put("invoiceadvcndntype", Integer.parseInt(invoiceadvcndntype));
            }
            HashMap<Integer, String> paymentHashMap = new HashMap<Integer, String>();
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("datainvoiceadvcndn"))) {
                JSONArray jSONArray = new JSONArray(paramJObj.getString("datainvoiceadvcndn"));
                String paymentid = "";
                for (int i = 0; i < jSONArray.length(); i++) {
                    JSONObject jObject = jSONArray.getJSONObject(i);
                    paymentid = StringUtil.DecodeText(jObject.optString("paymentID"));
                    int invoiceadvcndntypejson = !StringUtil.isNullOrEmpty(jObject.getString("invoiceadvcndntype")) ? Integer.parseInt(jObject.getString("invoiceadvcndntype")) : 0;
                    paymentHashMap.put(invoiceadvcndntypejson, paymentid);
                }
                receipthm.put("paymentHashMap", paymentHashMap);
            }
            boolean ismanydbcr = StringUtil.getBoolean(paramJObj.optString("ismanydbcr","false"));
            receipthm.put("ismanydbcr", ismanydbcr);
            receipthm.put("receipttype", receiptType);
            receipthm.put("actualReceiptType", actualReceiptType);
            double bankCharges = 0;
            double bankInterest = 0;
            accountId = paramJObj.optString("accid",null);
            if (StringUtil.isNullOrEmpty(accountId)) {
                String acccode = paramJObj.optString("acccode",null);
                accountId = accInvoiceDAOObj.getCustomerId(companyid, acccode);
            }
            String bankChargesAccid = paramJObj.optString("bankChargesCmb",null);
            String bankInterestAccid = paramJObj.optString("bankInterestCmb",null);
            boolean onlyAdvance = StringUtil.getBoolean(paramJObj.optString("onlyAdvance","false"));
            String createdby = paramJObj.getString("lid");
            String modifiedby = createdby;
            long createdon = System.currentTimeMillis();
            long updatedon = System.currentTimeMillis();
            String person = "";
            if (receiptType == 1) {
                person = " Against Customer Invoice ";
            }

            StringBuffer billno = new StringBuffer();
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("bankCharges")) && !StringUtil.isNullOrEmpty(bankChargesAccid)) {
                bankCharges = Double.parseDouble(paramJObj.optString("bankCharges",null));
                receipthm.put("bankCharges", bankCharges);
                receipthm.put("bankChargesCmb", bankChargesAccid);
            }
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("bankInterest")) && !StringUtil.isNullOrEmpty(bankInterestAccid)) {
                bankInterest = Double.parseDouble(paramJObj.optString("bankInterest","null"));
                receipthm.put("bankInterest", bankInterest);
                receipthm.put("bankInterestCmb", bankInterestAccid);
            }
            String paidToid = paramJObj.optString("paidToCmb",null);
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("paidToCmb")) && !StringUtil.isNullOrEmpty(paidToid)) {
                receipthm.put("paidToCmb", paidToid);
            }
            if (receiptType == 6) {
                receipthm.put("vendor", accountId);
            } else {
                boolean isCustomer = false;
                KwlReturnObject custObj = accountingHandlerDAOobj.getObject(Customer.class.getName(), accountId);
                if (custObj.getEntityList().get(0) != null) {
                    cust = (Customer) custObj.getEntityList().get(0);
                }
                if (cust != null) {
                    isCustomer = true;
                }

                boolean isVendor = false;
                KwlReturnObject vendObj = accountingHandlerDAOobj.getObject(Vendor.class.getName(), accountId);
                if (vendObj.getEntityList().get(0) != null) {
                    vend = (Vendor) vendObj.getEntityList().get(0);
                }
                if (vend != null) {
                    isVendor = true;
                }

                if (isCustomer) {
                    receipthm.put("customerId", accountId);
                } else if (isVendor) {
                    receipthm.put("vendor", accountId);
                }
            }

            boolean bankReconsilationEntry = false;
            boolean bankPayment = false;
            Date clearanceDate = null, startDate = null, endDate = null;
            String bankAccountId = "";
            Map<String, Object> bankReconsilationMap = new HashMap<String, Object>();
            boolean editAdvance = false;


            if (!StringUtil.isNullOrEmpty(receiptid)) {
                KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receiptid);
                receipt = (Receipt) receiptObj.getEntityList().get(0);
                oldjeid = receipt.getJournalEntry().getID();
                JournalEntry jetemp = receipt.getJournalEntry();
                if (jetemp != null) {
                    jeentryNumber = jetemp.getEntryNumber();   //preserving these data to generate same JE number in edit case                    
                    jeautogenflag = jetemp.isAutoGenerated();
                    jeSeqFormatId = jetemp.getSeqformat() == null ? "" : jetemp.getSeqformat().getID();
                    jeIntegerPart = String.valueOf(jetemp.getSeqnumber());
                    jeDatePrefix = jetemp.getDatePreffixValue();
                    jeDateAfterPrefix = jetemp.getDateAfterPreffixValue();
                    jeDateSuffix = jetemp.getDateSuffixValue();
                }
                if (receipt.getPayDetail() != null) {
                    payDetailID = receipt.getPayDetail().getID();
                    if (receipt.getPayDetail().getCard() != null) {
                        Cardid = receipt.getPayDetail().getCard().getID();
                    }
                    if (receipt.getPayDetail().getCheque() != null) {
                        Cardid = receipt.getPayDetail().getCheque().getID();
                    }
                }
                if (receipt != null) {
                    updateOpeningBalance(receipt, companyid);
                }
                result = accReceiptDAOobj.deleteReceiptDetails(receiptid, companyid);
                result = accReceiptDAOobj.deleteReceiptDetailsOtherwise(receiptid);
                if (receipt != null && receipt.getRevalJeId() != null) {
                    result = accJournalEntryobj.deleteJEDtails(receipt.getRevalJeId(), companyid);
                    result = accJournalEntryobj.deleteJE(receipt.getRevalJeId(), companyid);
                }
                receipthm.put("deposittojedetailid", null);
                receipthm.put("depositamount", 0.0);
                //Delete old entries and insert new entries again from optimized table in edit case.
                accJournalEntryobj.deleteOnEditAccountJEs_optimized(oldjeid);
            }

            synchronized (this) {
                SequenceFormat prevSeqFormat = null;
                String nextAutoNumber = "";
                String nextAutoNoInt = "";
                String datePrefix = "";
                String dateAfterPrefix = "";
                String dateSuffix = "";
                String prevseqnumber = "";

                if (!StringUtil.isNullOrEmpty(receiptid)) {  // for edit case
                    String advanceId = receipt.getAdvanceid() != null ? receipt.getAdvanceid().getID() : "";
                    result = accReceiptDAOobj.getDuplicateForNormalReceipt(entryNumber, companyid, receiptid, advanceId, receipt);
                    int count = result.getRecordTotalCount();
                    if (count > 0 && sequenceformat.equals("NA")) {
                        throw new AccountingException(messageSource.getMessage("acc.field.Paymentnumber", null, Locale.forLanguageTag(paramJObj.getString("language"))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJObj.getString("language"))));
                    } else if (receipt != null && receipt.getSeqformat() != null) {
                        prevSeqFormat = receipt.getSeqformat();
                        prevseqnumber = receipt.getSeqnumber() + "";
                        receipthm.put(Constants.SEQFORMAT, prevSeqFormat.getID());
                        receipthm.put(Constants.SEQNUMBER, prevseqnumber);
                        receipthm.put(Constants.DATEPREFIX, receipt.getDatePreffixValue());
                        receipthm.put(Constants.DATEAFTERPREFIX, receipt.getDateAfterPreffixValue());
                        receipthm.put(Constants.DATESUFFIX, receipt.getDateSuffixValue());
                        nextAutoNumber = entryNumber;
                    }
                } else if (paymentHashMap.containsKey(3) && !paymentHashMap.containsKey(1)) {
                    String cndnId = paymentHashMap.get(3);
                    KwlReturnObject cndnresult = accountingHandlerDAOobj.getObject(Receipt.class.getName(), cndnId);
                    Receipt receiptCnDn = null;
                    int count = 0;
                    if (!cndnresult.getEntityList().isEmpty() && cndnresult.getEntityList().get(0) != null) {
                        receiptCnDn = (Receipt) cndnresult.getEntityList().get(0);
                        result = accReceiptDAOobj.getDuplicateForNormalReceipt(entryNumber, companyid, receiptid, cndnId, receiptCnDn);
                        count = result.getRecordTotalCount();
                    }
                    if (count > 0 && sequenceformat.equals("NA")) {
                        throw new AccountingException(messageSource.getMessage("acc.field.Paymentnumber", null, Locale.forLanguageTag(paramJObj.getString("language"))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJObj.getString("language"))));
                    } else if (receiptCnDn != null && receiptCnDn.getSeqformat() != null) {
                        prevSeqFormat = receiptCnDn.getSeqformat();
                        prevseqnumber = receiptCnDn.getSeqnumber() + "";
                        receipthm.put(Constants.SEQFORMAT, prevSeqFormat.getID());
                        receipthm.put(Constants.SEQNUMBER, prevseqnumber);
                        receipthm.put(Constants.DATEPREFIX, receiptCnDn.getDatePreffixValue());
                        receipthm.put(Constants.DATEAFTERPREFIX, receiptCnDn.getDateAfterPreffixValue());
                        receipthm.put(Constants.DATESUFFIX, receiptCnDn.getDateSuffixValue());
                        nextAutoNumber = entryNumber;
                    }
                } else if (!ignoreDuplicateChk && (actualReceiptType != 0 || (actualReceiptType == 0 && isAdvancePayment))) {//true when advance created along with payment against invoice
                    if (actualReceiptType == 0 && isAdvancePayment && paramJObj.optString("data",null) != null) {
                        JSONArray jSONArray = new JSONArray(paramJObj.getString("data"));
                        JSONObject jSONObject = jSONArray.getJSONObject(0);
                        String advReceiptId = jSONObject.optString("billid", "");
                        if (!StringUtil.isNullOrEmpty(advReceiptId)) {
                            KwlReturnObject receiptObj = accountingHandlerDAOobj.getObject(Receipt.class.getName(), advReceiptId);
                            Receipt advreceipt = (Receipt) receiptObj.getEntityList().get(0);
                            if (advreceipt != null && advreceipt.getSeqformat() != null) {
                                prevSeqFormat = advreceipt.getSeqformat();
                                prevseqnumber = advreceipt.getSeqnumber() + "";
                                receipthm.put(Constants.SEQFORMAT, prevSeqFormat.getID());
                                receipthm.put(Constants.SEQNUMBER, prevseqnumber);
                                receipthm.put(Constants.DATEPREFIX, advreceipt.getDatePreffixValue());
                                receipthm.put(Constants.DATEAFTERPREFIX, advreceipt.getDateAfterPreffixValue());
                                receipthm.put(Constants.DATESUFFIX, advreceipt.getDateSuffixValue());
                                nextAutoNumber = entryNumber;

                                JournalEntry jetemp = advreceipt.getJournalEntry();
                                jeentryNumber = jetemp.getEntryNumber();   //preserving these data to generate same JE number in edit case                    
                                jeautogenflag = jetemp.isAutoGenerated();
                                jeSeqFormatId = jetemp.getSeqformat() == null ? "" : jetemp.getSeqformat().getID();
                                jeIntegerPart = String.valueOf(jetemp.getSeqnumber());
                                jeDatePrefix = jetemp.getDatePreffixValue();
                                jeDateAfterPrefix = jetemp.getDateAfterPreffixValue();
                                jeDateSuffix = jetemp.getDateSuffixValue();
                                editAdvance = true;
                            }
                            result = accReceiptDAOobj.getDuplicateForNormalReceipt(entryNumber, companyid, advReceiptId, "", advreceipt);
                        }
                    } else {
                        result = accReceiptDAOobj.getReceiptFromBillNo(entryNumber, companyid);
                    }
                    int count = result.getRecordTotalCount();
                    if (count > 0 && sequenceformat.equals("NA")) {
                        throw new AccountingException(messageSource.getMessage("acc.field.Paymentnumber", null, Locale.forLanguageTag(paramJObj.getString("language"))) + entryNumber + messageSource.getMessage("acc.field.alreadyexists.", null, Locale.forLanguageTag(paramJObj.getString("language"))));
                    }
                }
                if (!sequenceformat.equals("NA") && prevSeqFormat == null && !ignoreDuplicateChk) { //to generate sequence number
                    boolean seqformat_oldflag = StringUtil.getBoolean(paramJObj.optString("seqformat_oldflag","false"));
                    if (seqformat_oldflag) {
                        nextAutoNumber = accCompanyPreferencesObj.getNextAutoNumber(companyid, StaticValues.AUTONUM_RECEIPT, sequenceformat);
                    } else {
                        Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                        seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_RECEIPT, sequenceformat, seqformat_oldflag, billdateVal);
                        nextAutoNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                        nextAutoNoInt = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                        datePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                        dateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);//Date After Prefix Part
                        dateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                        
                        receipthm.put(Constants.SEQFORMAT, sequenceformat);
                        receipthm.put(Constants.SEQNUMBER, nextAutoNoInt);
                        receipthm.put(Constants.DATEPREFIX, datePrefix);
                        receipthm.put(Constants.DATEAFTERPREFIX, dateAfterPrefix);
                        receipthm.put(Constants.DATESUFFIX, dateSuffix);
                    }
                    entryNumber = nextAutoNumber;
                }
                if (!sequenceformat.equals("NA") && ignoreDuplicateChk) {//case of creating advance with normal
                    result = accReceiptDAOobj.getCurrentSeqNumberForAdvance(sequenceformat, companyid);
                    nextAutoNoInt = !(result.getEntityList().isEmpty()) ? (result.getEntityList().get(0) + "") : "0";
                    receipthm.put(Constants.SEQNUMBER, nextAutoNoInt);
                    receipthm.put(Constants.SEQFORMAT, sequenceformat);
                    nextAutoNumber = entryNumber;
                }
                receipthm.put("entrynumber", entryNumber);
                receipthm.put("autogenerated", nextAutoNumber.equals(entryNumber));
            }

            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);

            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), paramJObj.getString("currencyid"));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String currencyid = (paramJObj.optString("currencyid") == null ? currency.getCurrencyID() : paramJObj.getString("currencyid"));

            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);


            receipthm.put("currencyid", currencyid);
            receipthm.put("externalCurrencyRate", externalCurrencyRate);
            KwlReturnObject payresult = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), paramJObj.optString("pmtmethod",null));
            //        KwlReturnObject payresult = accountingHandlerDAOobj.getObject(PaymentMethod.class.getName(), "402880d748b0a4710148b0bfe688001e");
            PaymentMethod payMethod = (PaymentMethod) payresult.getEntityList().get(0);

            dipositTo = payMethod.getAccount();
            HashMap pdetailhm = new HashMap();
            pdetailhm.put("paymethodid", payMethod.getID());
            pdetailhm.put("companyid", company.getCompanyID());
            if (payMethod.getDetailType() != PaymentMethod.TYPE_CASH) {

                JSONObject obj = new JSONObject(paramJObj.getString("paydetail"));
                if (payMethod.getDetailType() == PaymentMethod.TYPE_BANK) {
                    bankPayment = true;
                    bankReconsilationEntry = obj.getString("paymentStatus") != null ? obj.getString("paymentStatus").equals("Cleared") : false;
                    if (bankReconsilationEntry) {
                        bankAccountId = paramJObj.optString("bankaccid",null);
                        startDate = df.parse(paramJObj.getString("startdate"));
                        endDate = df.parse(paramJObj.getString("enddate"));
                        clearanceDate = df.parse(obj.getString("clearanceDate"));
                        bankReconsilationMap.put("bankAccountId", bankAccountId);
                        bankReconsilationMap.put("startDate", startDate);
                        bankReconsilationMap.put("endDate", endDate);
                        bankReconsilationMap.put("clearanceDate", clearanceDate);
                        bankReconsilationMap.put("endingAmount", 0.0);
                        bankReconsilationMap.put("companyId", companyid);
                    }
                    HashMap chequehm = new HashMap();
                    Map<String, Object> seqchequehm = new HashMap<>();
                    obj.put(Constants.companyKey, companyid);
                    String chequesequenceformat =  obj.optString("sequenceformat");
                    /**
                     * getNextChequeNumber method to generate next sequence number using
                     * sequence format,also saving the dateprefix and datesuffix in cheque table.
                     */
                    if (!StringUtil.isNullOrEmpty(chequesequenceformat) && !chequesequenceformat.equals("NA")) {
                        seqchequehm = accCompanyPreferencesObj.getNextChequeNumber(obj);
                    }

                    if (seqchequehm.containsKey(Constants.AUTO_ENTRYNUMBER)) {
                        chequehm.put("chequeno", (String) seqchequehm.get(Constants.AUTO_ENTRYNUMBER));
                    }
                    if (seqchequehm.containsKey(Constants.SEQNUMBER)) {
                        chequehm.put("sequenceNumber", (String) seqchequehm.get(Constants.SEQNUMBER));
                    }
                    if (seqchequehm.containsKey(Constants.DATEPREFIX)) {
                        chequehm.put(Constants.DATEPREFIX, (String) seqchequehm.get(Constants.DATEPREFIX));
                    }
                    if (seqchequehm.containsKey(Constants.DATEAFTERPREFIX)) {
                        chequehm.put(Constants.DATEAFTERPREFIX, (String) seqchequehm.get(Constants.DATEAFTERPREFIX));
                    }
                    if (seqchequehm.containsKey(Constants.DATESUFFIX)) {
                        chequehm.put(Constants.DATESUFFIX, (String) seqchequehm.get(Constants.DATESUFFIX));
                    }
                    chequehm.put("chequeno", obj.optString("chequeno"));
                    chequehm.put("description", StringUtil.DecodeText(obj.optString("description")));
                    chequehm.put("bankname", StringUtil.DecodeText(obj.optString("bankname")));
                    chequehm.put("duedate", df.parse(obj.getString("payDate")));
                    chequehm.put("bankmasteritemid", obj.getString("bankmasteritemid"));
                    chequehm.put("companyId", companyid);
                    chequehm.put("bankAccount", (payMethod.getAccount() != null) ? payMethod.getAccount().getID() : "");
                    chequehm.put("createdFrom", 2);
                    KwlReturnObject cqresult = accPaymentDAOobj.addCheque(chequehm);
                    Cheque cheque = (Cheque) cqresult.getEntityList().get(0);
                    pdetailhm.put("chequeid", cheque.getID());
                } else if (payMethod.getDetailType() == PaymentMethod.TYPE_CARD) {
                    HashMap cardhm = new HashMap();
                    cardhm.put("cardno", obj.getString("cardno"));
                    cardhm.put("nameoncard", obj.getString("nameoncard"));
                    cardhm.put("expirydate", df.parse(obj.getString("expirydate")));
                    cardhm.put("cardtype", obj.getString("cardtype"));
                    cardhm.put("refno", obj.getString("refno"));
                    KwlReturnObject cdresult = accPaymentDAOobj.addCard(cardhm);
                    Card card = (Card) cdresult.getEntityList().get(0);
                    pdetailhm.put("cardid", card.getID());
                }
            }
            KwlReturnObject pdresult = null;
            if (!StringUtil.isNullOrEmpty(receiptid) && !StringUtil.isNullOrEmpty(payDetailID)) {
                pdetailhm.put("paydetailid", payDetailID);
            }
            pdresult = accPaymentDAOobj.addPayDetail(pdetailhm);
            PayDetail pdetail = (PayDetail) pdresult.getEntityList().get(0);
            receipthm.put("paydetailsid", pdetail.getID());

            receipthm.put("memo", paramJObj.optString("memo",null));
            receipthm.put("companyid", company.getCompanyID());

            Map<String, Object> jeDataMap = AccountingManager.getGlobalParamsJson(paramJObj);
            if (StringUtil.isNullOrEmpty(oldjeid) && !editAdvance) {
                synchronized (this) {
                    HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                    JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                    JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                    JEFormatParams.put("companyid", companyid);
                    JEFormatParams.put("isdefaultFormat", true);

                    KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                    SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                    Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                    seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false,billdateVal);
                    jeentryNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                    jeIntegerPart = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                    jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                    jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);//Date After Prefix Part
                    jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part
                    
                    jeSeqFormatId = format.getID();
                    jeautogenflag = true;
                }
            }
            jeDataMap.put("entrynumber", jeentryNumber);
            jeDataMap.put("autogenerated", jeautogenflag);
            jeDataMap.put(Constants.SEQFORMAT, jeSeqFormatId);
            jeDataMap.put(Constants.SEQNUMBER, jeIntegerPart);
            jeDataMap.put(Constants.DATEPREFIX, jeDatePrefix);
            jeDataMap.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
            jeDataMap.put(Constants.DATESUFFIX, jeDateSuffix);

            jeDataMap.put("entrydate", billdateVal);
            if (!StringUtil.isNullOrEmpty(currencyid) && !currencyid.equalsIgnoreCase(paramJObj.getString("currencyid"))) {
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("gcurrencyid", paramJObj.getString("currencyid"));
                requestParams.put("companyid", companyid);
                externalCurrencyRate = accCurrencyobj.getCurrencyToBaseRate(requestParams, currencyid, billdateVal);
                receipthm.put("externalCurrencyRate", externalCurrencyRate);
            }
            //  jeDataMap.put("entrydate", df.parse(request.getParameter("creationdate")));
            jeDataMap.put("companyid", company.getCompanyID());
            jeDataMap.put("memo", paramJObj.optString("memo",null));
            jeDataMap.put("currencyid", currencyid);
            jeDataMap.put("transactionId", receipt.getID());
            jeDataMap.put("transactionModuleid", Constants.Acc_Receive_Payment_ModuleId);
            HashSet jedetails = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            jeDataMap.put("jeid", jeid);
            String detail = paramJObj.optString("detail",null);
            JSONArray jArr = new JSONArray();
            if (!StringUtil.isNullOrEmpty(detail)) {
                jArr = new JSONArray(detail);
            }
            if (jArr.length() > 0 && !isMultiDebit) {
                amount = 0;
                for (int i = 0; i < jArr.length(); i++) { // Changed for New Customer/Vendor Removed account dependency
                    JSONObject jobj = jArr.getJSONObject(i);
                    JSONArray jArray = new JSONArray();
                    jArray.put(jobj);
                    double amountDiffforInv = oldReceiptRowsAmount(paramJObj, jArray, currencyid, externalCurrencyRate);
                    rateDecreased = false;
                    if (amountDiffforInv < 0) {
                        rateDecreased = true;
                    }
                    amount += jobj.getDouble("payment");
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", jobj.getDouble("payment") - amountDiffforInv);
                    //    jedjson.put("accountid", jobj.get("accountid"));
                    jedjson.put("accountid", cust.getAccount().getID());
                    jedjson.put("debit", false);
                    jedjson.put("jeid", jeid);
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                    jArr.getJSONObject(i).put("rowjedid", jed.getID());

                    if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                        JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                        Map1.put(jed.getID(), jcustomarray);
                    }
                    if (!StringUtil.isNullOrEmpty(jobj.optString("customfieldArray", ""))) {
                        JSONArray jcustomarray = null;
                        String customfieldArray = jobj.optString("customfieldArray", ""); //Custom Data from other Project
                        customfieldArray =StringUtil.DecodeText(customfieldArray);
                        if (!StringUtil.isNullOrEmpty(customfieldArray)) {
                            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                            String mapWithFieldType = paramJObj.optString("mapWithFieldType",null);
                            customrequestParams.put("customarray", customfieldArray);
                            customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                            customrequestParams.put("companyid", companyid);
                            customrequestParams.put("mapWithFieldType", mapWithFieldType);
                            jcustomarray = fieldDataManagercntrl.createJSONArrForCustomFieldValueFromOtherSource(customrequestParams);
                        }
                        Map1.put(jed.getID(), jcustomarray);
                    }
                    if ((!jobj.getString("billno").equalsIgnoreCase("undefined")) && (!jobj.getString("billno").equalsIgnoreCase(""))) {
                        billno.append(jobj.getString("billno") + ",");
                    }
                }

                amountDiff = oldReceiptRowsAmount(paramJObj, jArr, currencyid, externalCurrencyRate);

                if (preferences.getForeignexchange() == null) {
                    throw new AccountingException(messageSource.getMessage("acc.receipt.forex", null, Locale.forLanguageTag(paramJObj.getString("language"))));
                }
                if (amountDiff != 0 && preferences.getForeignexchange() != null && Math.abs(amountDiff) >= 0.000001) {//Math.abs(amountDiff) < .0000001 Added this because in case of revaluation the diff in the rates was less than .0000001 and the amount calculation was going in Exachange rate account. 
                    if (amountDiff < 0) {
                        rateDecreased = true;
                    }
                    JSONObject jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", rateDecreased ? (-1 * amountDiff) : amountDiff);
                    jedjson.put("accountid", preferences.getForeignexchange().getID());
                    jedjson.put("debit", rateDecreased ? true : false);
                    jedjson.put("jeid", jeid);
                    KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }

            } else {
                amount = Double.parseDouble(paramJObj.optString("amount",null));
            }
            JSONObject jedjson = null;
            KwlReturnObject jedresult = null;
            JournalEntryDetail jed = null;
            JournalEntryDetail JEdetailID = null;
            boolean taxExist = false;
            List receiptOtherwiseList = new ArrayList();
            HashMap receiptdetailotherwise = new HashMap();
            if (isMultiDebit) {
                JSONArray drAccArr = new JSONArray(drAccDetails);
                for (int i = 0; i < drAccArr.length(); i++) {
                    JSONObject jobj = drAccArr.getJSONObject(i);
                    boolean isdebit = jobj.has("isdebit") ? Boolean.parseBoolean(jobj.getString("isdebit")) : false;
                    jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    jedjson.put("amount", Double.parseDouble(jobj.getString("dramount")));
                    jedjson.put("accountid", cust.getAccount().getID());
                    jedjson.put("debit", isdebit);//false);
                    jedjson.put("jeid", jeid);
                    jedjson.put("description", jobj.getString("description"));
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    JEdetailID = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(JEdetailID);
                    if ((!jobj.getString("billno").equalsIgnoreCase("undefined")) && (!jobj.getString("billno").equalsIgnoreCase(""))) {
                        billno.append(jobj.getString("billno") + ",");
                    }
                    double rowtaxamount = 0;
                    //taxExist=StringUtil.isNullOrEmpty(rowtaxid)?false:true;
                    if (receiptType == 2 || receiptType == 9) {//Otherwise for receive Payment
                        ReceiptDetailOtherwise receiptDetailOtherwise = null;
                        String rowtaxid = jobj.getString("prtaxid");
                        KwlReturnObject txresult = accountingHandlerDAOobj.getObject(Tax.class.getName(), rowtaxid); // (Tax)session.get(Tax.class, taxid);
                        Tax rowtax = (Tax) txresult.getEntityList().get(0);
                        if (rowtax == null || rowtaxid.equalsIgnoreCase("-1")) {
                            receiptdetailotherwise.put("amount", Double.parseDouble(jobj.getString("dramount")));
                            receiptdetailotherwise.put("taxjedid", "");
                            receiptdetailotherwise.put("tax", rowtaxid.equalsIgnoreCase("-1") ? "None" : "");
                            receiptdetailotherwise.put("accountid", jobj.getString("accountid"));
                            receiptdetailotherwise.put("isdebit", isdebit);
                            receiptdetailotherwise.put("taxamount", rowtaxamount);
                            receiptdetailotherwise.put("description", jobj.getString("description"));
                            result = accReceiptDAOobj.saveReceiptDetailOtherwise(receiptdetailotherwise);
                            receiptdetailotherwise.clear();
//                                    ReceiptDetailOtherwise receiptDetailOtherwise=null;
                            receiptDetailOtherwise = (ReceiptDetailOtherwise) result.getEntityList().get(0);
                            receiptOtherwiseList.add(receiptDetailOtherwise.getID());
                        } else {
                            rowtaxamount = Double.parseDouble(jobj.getString("taxamount"));
                            jedjson = new JSONObject();
                            jedjson.put("srno", jedetails.size() + 1);
                            jedjson.put("companyid", company.getCompanyID());
                            jedjson.put("amount", authHandler.formattedAmount(rowtaxamount, companyid));
                            jedjson.put("accountid", rowtax.getAccount().getID());
                            jedjson.put("debit", isdebit);//false);
                            jedjson.put("jeid", jeid);
                            jedjson.put("description", jobj.getString("description"));
                            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                            jedetails.add(jed);
                            receipthm.put("taxentryid", jed.getID());

                            receiptdetailotherwise.put("amount", Double.parseDouble(jobj.getString("dramount")));
                            receiptdetailotherwise.put("taxjedid", jed.getID());
                            receiptdetailotherwise.put("tax", rowtax.getID());
                            receiptdetailotherwise.put("accountid", jobj.getString("accountid"));
                            receiptdetailotherwise.put("isdebit", isdebit);
                            receiptdetailotherwise.put("taxamount", rowtaxamount);
                            receiptdetailotherwise.put("description", jobj.getString("description"));
                            result = accReceiptDAOobj.saveReceiptDetailOtherwise(receiptdetailotherwise);
                            receiptdetailotherwise.clear();
                            receiptDetailOtherwise = (ReceiptDetailOtherwise) result.getEntityList().get(0);
                            receiptOtherwiseList.add(receiptDetailOtherwise.getID());
                            HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                            if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                                JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                                customrequestParams.put("customarray", jcustomarray);
                                customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                                customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                                customrequestParams.put("modulerecid", jed.getID());
                                customrequestParams.put("recdetailId", receiptDetailOtherwise.getID());
                                customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                                customrequestParams.put("companyid", companyid);
                                customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                    JSONObject tempJObj = new JSONObject();
                                    tempJObj.put("accjedetailcustomdata", jed.getID());
                                    tempJObj.put("jedid", jed.getID());
                                    jedresult = accJournalEntryobj.updateJournalEntryDetails(tempJObj);

                                }
                            }
                        }

                        HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                        if (!StringUtil.isNullOrEmpty(jobj.optString("customfield", ""))) {
                            JSONArray jcustomarray = new JSONArray(jobj.optString("customfield", "[]"));
                            customrequestParams.put("customarray", jcustomarray);
                            customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                            customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                            customrequestParams.put("modulerecid", JEdetailID.getID());
                            customrequestParams.put("recdetailId", receiptDetailOtherwise.getID());
                            customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                            customrequestParams.put("companyid", companyid);
                            customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                            KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                            if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                                JSONObject tempJObj = new JSONObject();
                                tempJObj.put("accjedetailcustomdata", JEdetailID.getID());
                                tempJObj.put("jedid", JEdetailID.getID());
                                jedresult = accJournalEntryobj.updateJournalEntryDetails(tempJObj);

                            }
                        }


                    }
                }
            }

            if (bankCharges != 0) {
                if (!isAdvancePayment || (isAdvancePayment && onlyAdvance)) {
                    amount -= bankCharges;
                    jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                    jedjson.put("amount", bankCharges);
                    jedjson.put("accountid", bankChargesAccid);
                    jedjson.put("debit", true);
                    jedjson.put("jeid", jeid);
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
            }

            if (bankInterest != 0) {
                if (!isAdvancePayment || (isAdvancePayment && onlyAdvance)) {
                    amount -= bankInterest;
                    jedjson = new JSONObject();
                    jedjson.put("srno", jedetails.size() + 1);
                    jedjson.put("companyid", companyid);
                    //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                    jedjson.put("amount", bankInterest);
                    jedjson.put("accountid", bankInterestAccid);
                    jedjson.put("debit", true);
                    jedjson.put("jeid", jeid);
                    jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                    jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                    jedetails.add(jed);
                }
            }

            if (amount != 0) {
                jedjson = new JSONObject();
                jedjson.put("srno", jedetails.size() + 1);
                jedjson.put("companyid", companyid);
                //(If currency USD and base currency SGD, inv rate 0.81 n pay rate 0.80 then amount = 1234.57, amountDiff = -15.24, depositAmt = 1250 SGD)
                jedjson.put("amount", amount);
                jedjson.put("accountid", dipositTo.getID());
                jedjson.put("debit", true);
                jedjson.put("jeid", jeid);
                jedresult = accJournalEntryobj.addJournalEntryDetails(jedjson);
                jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
                jedetails.add(jed);

                receipthm.put("deposittojedetailid", jed.getID());
                receipthm.put("depositamount", amount);
            }
            jeDataMap.put("jedetails", jedetails);
            jeDataMap.put("externalCurrencyRate", externalCurrencyRate);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);//Add Journal entry details
            journalEntry = (JournalEntry) jeresult.getEntityList().get(0);

            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", journalEntry.getID());
                customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    jeDataMap.put("accjecustomdataref", journalEntry.getID());
                    jeresult = accJournalEntryobj.saveJournalEntry(jeDataMap);
                }
            }

            receipthm.put("journalentryid", journalEntry.getID());
            if (!StringUtil.isNullOrEmpty(receiptid)) {
                receipthm.put("receiptid", receipt.getID());
            }

            result = accReceiptDAOobj.saveReceipt(receipthm);
            receipt = (Receipt) result.getEntityList().get(0);
            receipthm.put("receiptid", receipt.getID());

            HashSet receiptDetails = new HashSet();
            if (!isMultiDebit) {
                receiptDetails = saveReceiptRows(receipt, company, jArr, greceipt);
            }
            receipthm.put("receiptdetails", receiptDetails);
            receipthm.put("createdby", createdby);
            receipthm.put("modifiedby", modifiedby);
            receipthm.put("createdon", createdon);
            receipthm.put("updatedon", updatedon);
            receipthm.put("paymentWindowType", 1);      //hardcoded 1 because Payment make against invoice at LMS side
            result = accReceiptDAOobj.saveReceipt(receipthm);
            Iterator itr1 = receipt.getRows().iterator();
            while (itr1.hasNext()) {
                ReceiptDetail payd = (ReceiptDetail) itr1.next();

                JSONArray jcustomarray = Map1.get(payd.getROWJEDID());
                HashMap<String, Object> customrequestParams = new HashMap<String, Object>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                customrequestParams.put("modulerecid", payd.getROWJEDID());
                customrequestParams.put("recdetailId", payd.getID());
                customrequestParams.put("moduleid", Constants.Acc_Receive_Payment_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    JSONObject tempJObj = new JSONObject();
                    tempJObj.put("accjedetailcustomdata", payd.getROWJEDID());
                    tempJObj.put("jedid", payd.getROWJEDID());
                    jedresult = accJournalEntryobj.updateJournalEntryDetails(tempJObj);

                }
            }
            receipt = (Receipt) result.getEntityList().get(0);
            if (receiptType == 2 || receiptType == 9) {
                for (int i = 0; i < receiptOtherwiseList.size(); i++) {
                    receiptdetailotherwise.put("receipt", receipt.getID());
                    receiptdetailotherwise.put("receiptotherwise", receiptOtherwiseList.get(i));
                    result = accReceiptDAOobj.saveReceiptDetailOtherwise(receiptdetailotherwise);
                    receiptdetailotherwise.clear();
                }
            }
            if (bankReconsilationEntry) {
                bankReconsilationMap.put("clearingamount", amount);
                bankReconsilationMap.put("currencyid", currencyid);
                bankReconsilationMap.put("details", jArr);
                bankReconsilationMap.put("ismultidebit", isMultiDebit);
                bankReconsilationMap.put("receipt", receipt);
                if (!StringUtil.isNullOrEmpty(oldjeid)) {
                    bankReconsilationMap.put("oldjeid", oldjeid);
                }
                HashMap<String, Object> globalParams = AccountingManager.getGlobalParamsJson(paramJObj);
                saveBankReconsilation(bankReconsilationMap, globalParams);
            }
            if (bankPayment && !bankReconsilationEntry && !StringUtil.isNullOrEmpty(oldjeid)) {
                bankReconsilationMap.put("oldjeid", oldjeid);
                bankReconsilationMap.put("companyId", companyid);
                deleteBankReconcilation(bankReconsilationMap);
            }

            if (isAdvancePayment && !StringUtil.isNullOrEmpty(paramJObj.optString("mainpaymentid"))) {//Link advance payments id with main payment id
                receipthm.clear();
                receipthm.put("receiptid", paramJObj.optString("mainpaymentid",null));
                receipthm.put("advanceid", receipt.getID());
                receipthm.put("advanceamount", paramJObj.optString("advanceamt",null) != null ? Double.parseDouble(paramJObj.get("advanceamt").toString()) : 0);
                result = accReceiptDAOobj.saveReceipt(receipthm);
            }

            if (isCNDN) {
                String AccDetailsarrStr = paramJObj.optString("detailForCNDN",null);
                JSONArray drAccArr = new JSONArray(AccDetailsarrStr);

                if (!isAgainstDN) {
                    String paymentId = receipt.getID();
                    KwlReturnObject dnhistoryresult = accReceiptDAOobj.getCustomerDnPaymenyHistory("", 0.0, 0.0, paymentId);
                    List<DebitNotePaymentDetails> dnHistoryList = dnhistoryresult.getEntityList();
                    for (DebitNotePaymentDetails dnpd : dnHistoryList) {
                        String dnnoteid = dnpd.getDebitnote().getID()!=null?dnpd.getDebitnote().getID():"";
                        Double dnpaidamount = dnpd.getAmountPaid();
                        KwlReturnObject cnjedresult = accReceiptDAOobj.updateDnAmount(dnnoteid, -dnpaidamount);
                        KwlReturnObject opencnjedresult = accReceiptDAOobj.updateDnOpeningAmountDue(dnnoteid, -dnpaidamount);
                    }
                }

                for (int i = 0; i < drAccArr.length(); i++) {
                    JSONObject jobj = drAccArr.getJSONObject(i);
                    jedjson = new JSONObject();
                    double paidncamount = Double.parseDouble(jobj.getString("payment"));
                    double amountdue = Double.parseDouble(jobj.getString("amountdue"));
                    String dnnoteid = jobj.getString("noteid");
                    String paymentId = receipt.getID();
                    if ((!jobj.getString("noteno").equalsIgnoreCase("undefined")) && (!jobj.getString("noteno").equalsIgnoreCase(""))) {
                        billno.append(jobj.getString("noteno") + ",");
                    }
                    person = " Against debit note ";
                    if (isAgainstDN) {
                        KwlReturnObject cnjedresult = accReceiptDAOobj.updateCnAmount(dnnoteid, paidncamount);
                        KwlReturnObject cnopeningjedresult = accReceiptDAOobj.updateCnOpeningAmountDue(dnnoteid, paidncamount);
                        cnjedresult = accReceiptDAOobj.saveCustomerCnPaymenyHistory(dnnoteid, paidncamount, amountdue, paymentId);
                    } else {
                        KwlReturnObject cnjedresult = accReceiptDAOobj.updateDnAmount(dnnoteid, paidncamount);
                        KwlReturnObject cnopeningjedresult = accReceiptDAOobj.updateDnOpeningAmountDue(dnnoteid, paidncamount);
                        cnjedresult = accReceiptDAOobj.saveCustomerDnPaymenyHistory(dnnoteid, paidncamount, amountdue, paymentId);
                    }
                }
            }

            //Insert new entries again in optimized table.
            accJournalEntryobj.saveAccountJEs_optimized(jeid);

            //     netinword = EnglishNumberToWordsOjb.convert(Double.parseDouble(String.valueOf(amount)), currency);
            String action = "made";
            boolean isEdit = StringUtil.isNullOrEmpty(paramJObj.optString("isEdit")) ? false : Boolean.parseBoolean(paramJObj.getString("isEdit"));
            boolean isCopy = StringUtil.isNullOrEmpty(paramJObj.optString("isCopyReceipt")) ? false : Boolean.parseBoolean(paramJObj.getString("isCopyReceipt"));
            if (isEdit == true && isCopy == false) {
                action = "updated";
            }
            if (billno.length() > 0) {
                billno.deleteCharAt(billno.length() - 1);
            }
            Map<String, Object> auditTrailMap = new HashMap<String, Object>();
            auditTrailMap.put("userid", paramJObj.getString("lid"));

            auditTrailObj.insertAuditLog(AuditAction.RECEIPT_ADDED, "User " + paramJObj.optString("userfullname",null) + " has " + action + " a receipt " + receipt.getReceiptNumber() + person + billno, auditTrailMap, receipt.getID());
            if (jArr.length() > 0 && !isMultiDebit) {
                double finalAmountReval = 0;
                String basecurrency = paramJObj.getString("currencyid");
                for (int i = 0; i < jArr.length(); i++) {
                    JSONObject jobj = jArr.getJSONObject(i);
                    double ratio = 0;
                    double amountReval = 0;
                    String revalId = null;
                    Date tranDate = null;
                    double exchangeRate = 0.0;
                    double exchangeRateReval = 0.0;
                    //boolean isRealised=false;
                    double amountdue = jobj.getDouble("payment");
                    double exchangeratefortransaction = jobj.optDouble("exchangeratefortransaction", 1.00);
                    HashMap<String, Object> GlobalParams = new HashMap<String, Object>();
                    GlobalParams.put("companyid", paramJObj.getString(Constants.companyKey));
                    GlobalParams.put("gcurrencyid", basecurrency);
                    GlobalParams.put("dateformat", authHandler.getDateOnlyFormat());
                    Date creationDate = null;
                    try {
                        creationDate = df.parse(paramJObj.get("creationdate").toString());
                    } catch (ParseException ex) {
                        Calendar cal = Calendar.getInstance();
                        long billdateValue = (long) Long.parseLong(paramJObj.get("creationdate").toString());
                        cal.setTimeInMillis(billdateValue);
                        billdateVal = cal.getTime();
                        String bddate=df.format(billdateVal);
                        try{
                            billdateVal=df.parse(bddate);
                        }catch(ParseException e){
                            billdateVal=cal.getTime();
                        }
                        creationDate = billdateVal;
                    }
                    result = accountingHandlerDAOobj.getObject(Invoice.class.getName(), jobj.getString("billid"));
                    Invoice invoice = (Invoice) result.getEntityList().get(0);
                    boolean isopeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
                    tranDate = invoice.getCreationDate();
                    if (!invoice.isNormalInvoice()) {
                        exchangeRate = invoice.getExchangeRateForOpeningTransaction();
                        exchangeRateReval = exchangeRate;
                    } else {
                        exchangeRate = invoice.getJournalEntry().getExternalCurrencyRate();
                        exchangeRateReval = exchangeRate;
//                        tranDate = invoice.getJournalEntry().getEntryDate();
                    }

                    HashMap<String, Object> invoiceId = new HashMap<String, Object>();
                    invoiceId.put("invoiceid", invoice.getID());
                    invoiceId.put("companyid", paramJObj.getString(Constants.companyKey));
                    invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
                    result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                    RevaluationHistory revalueationHistory = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                    if (revalueationHistory != null) {
                        exchangeRateReval = revalueationHistory.getEvalrate();
                    }

                    result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), basecurrency);
                    currency = (KWLCurrency) result.getEntityList().get(0);
                    String currid = currency.getCurrencyID();
                    if (invoice.getCurrency() != null) {
                        currid = invoice.getCurrency().getCurrencyID();
                    }
                    //            double oldrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, gr.getJournalEntry().getEntryDate());
                    KwlReturnObject bAmt = null;
                    if (isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, tranDate, exchangeRate);
                    } else {
                        bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, tranDate, exchangeRate);
                    }

                    double oldrate = (Double) bAmt.getEntityList().get(0);
                    //            double newrate = CompanyHandler.getCurrencyToBaseAmount(session, request, 1.0, currid, AuthHandler.getDateFormatter(request).parse(request.getParameter("creationdate")));
                    if (revalueationHistory == null && isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
                    } else {
                        bAmt = accCurrencyobj.getCurrencyToBaseAmount(GlobalParams, 1.0, currid, creationDate, exchangeRateReval);
                    }

                    double newrate = (Double) bAmt.getEntityList().get(0);
                    ratio = oldrate - newrate;
                    if (Math.abs(exchangeratefortransaction - newrate) <= 0.000001) {
                        exchangeratefortransaction = newrate;
                    }
                    double amountdueNew = amountdue / exchangeratefortransaction;
                    amountdueNew = Math.round(amountdueNew * 1000) / 1000d;
                    amountReval = ratio * amountdueNew;
                    finalAmountReval = finalAmountReval + amountReval;
                }
                if (finalAmountReval != 0) {
                    String revaljeid = PostJEFORReevaluation(request, finalAmountReval, companyid, preferences, basecurrency);
                    receipthm.clear();
                    receipthm.put("receiptid", receipt.getID());
                    receipthm.put("revalJeId", revaljeid);
                    result = accReceiptDAOobj.saveReceipt(receipthm);

                }
            }
        } catch (UnsupportedEncodingException ex) {
            try {
                throw ServiceException.FAILURE(messageSource.getMessage("acc.common.excp", null, Locale.forLanguageTag(paramJObj.getString("language"))), ex);
            } catch (JSONException ex1) {
                Logger.getLogger(AccReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex1);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("saveReceipt : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            throw ServiceException.FAILURE("saveReceipt : " + ex.getMessage(), ex);
        }
        ll.add(new String[]{oldjeid, Cardid});
        KwlReturnObject accresult = accountingHandlerDAOobj.getObject(Account.class.getName(), accountId);
        Account account = (Account) accresult.getEntityList().get(0);
        String accountName = "";
        String accountaddress = "";
        String customerName = "";
        String accountid = accountId;
        result = accReceiptDAOobj.getaccountdetailsReceipt(accountid);
        if (result.getRecordTotalCount() > 0) {
            Customer customer = (Customer) result.getEntityList().get(0);
            accountaddress = customer.getBillingAddress();
            customerName = customer.getName();
        }
        if (account != null) {
            accountName = account.getName();
        }
        ll.add(new String[]{"amount", String.valueOf(amount)});
        ll.add(new String[]{"amountinword", netinword});
        ll.add(new String[]{"accountName", customerName});
        ll.add(receipt.getID());
        ll.add(receipt.getReceiptNumber());
        ll.add(String.valueOf(receipt.getAdvanceamount()));
        ll.add(accountaddress);
        ll.add(accountName);
        ll.add(receipt.getJournalEntry().getEntryNumber());
        ll.add(receipt.getReceipttype());
//       ll.add(paymentmethod);
        return (ArrayList) ll;
    }

    private HashSet saveReceiptRows(Receipt receipt, Company company, JSONArray jArr, GoodsReceipt greceipt) throws JSONException, ServiceException {
        HashSet details = new HashSet();
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);
            ReceiptDetail rd = new ReceiptDetail();
            rd.setSrno(i + 1);
            rd.setID(StringUtil.generateUUID());
            double amountReceived = jobj.getDouble("payment");// amount in receipt currency
            double amountReceivedConverted = jobj.getDouble("payment"); // amount in invoice currency
            rd.setAmount(jobj.getDouble("payment"));
            if (!StringUtil.isNullOrEmpty(jobj.optString("exchangeratefortransaction", "").toString()) && !StringUtil.isNullOrEmpty(jobj.optString("currencyidtransaction", "")) && !jobj.optString("currencyidtransaction", "").equals(receipt.getCurrency().getCurrencyID())) {
                rd.setExchangeRateForTransaction(Double.parseDouble(jobj.get("exchangeratefortransaction").toString()));
                KwlReturnObject resultCurrency = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), jobj.getString("currencyidtransaction"));
                KWLCurrency kWLCurrency = (KWLCurrency) resultCurrency.getEntityList().get(0);
                rd.setFromCurrency(kWLCurrency);
                rd.setToCurrency(receipt.getCurrency());
                amountReceivedConverted = amountReceived / Double.parseDouble(jobj.get("exchangeratefortransaction").toString());
            }
            rd.setCompany(company);
            KwlReturnObject result = accountingHandlerDAOobj.getObject(Invoice.class.getName(), jobj.getString("billid"));
            Invoice invoice = (Invoice) result.getEntityList().get(0);
            rd.setInvoice((Invoice) result.getEntityList().get(0));
            if (greceipt != null) {
                rd.setGoodsReceipt(greceipt);
            }
            rd.setReceipt(receipt);
            if (jobj.has("rowjedid")) {
                rd.setROWJEDID(jobj.getString("rowjedid"));
            }
            details.add(rd);

            double amountReceivedConvertedInBaseCurrency = 0d;
            HashMap<String, Object> requestParams = new HashMap();
            requestParams.put(Constants.companyid, company.getCompanyID());
            requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());
            double externalCurrencyRate = 1d;
            boolean isopeningBalanceRCP = receipt.isIsOpeningBalenceReceipt();
            Date rcpCreationDate = null;
            rcpCreationDate = receipt.getCreationDate();
            if (isopeningBalanceRCP) {
                externalCurrencyRate = receipt.getExchangeRateForOpeningTransaction();
            } else {
//                rcpCreationDate = receipt.getJournalEntry().getEntryDate();
                externalCurrencyRate = receipt.getJournalEntry().getExternalCurrencyRate();
            }
            String fromcurrencyid = receipt.getCurrency().getCurrencyID();
            KwlReturnObject bAmt = null;
            if (isopeningBalanceRCP && receipt.isConversionRateFromCurrencyToBase()) {// if Receipt is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountReceived, fromcurrencyid, rcpCreationDate, externalCurrencyRate);
            } else {
                bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, amountReceived, fromcurrencyid, rcpCreationDate, externalCurrencyRate);
            }
            amountReceivedConvertedInBaseCurrency = (Double) bAmt.getEntityList().get(0);

            updateInvoiceAmountDue(invoice, receipt, company, amountReceivedConverted, amountReceivedConvertedInBaseCurrency);
            updateReceiptAmountDue(receipt, company, amountReceived, amountReceivedConvertedInBaseCurrency);
        }
        return details;
    }

    private double oldReceiptRowsAmount(JSONObject paramJobj, JSONArray jArr, String currencyid, double externalCurrencyRate) throws ServiceException, SessionExpiredException {
        double amount = 0, actualAmount = 0;
        DateFormat df = authHandler.getDateOnlyFormat();
        KwlReturnObject result;
        Date billdateVal = null;
        Date creationDate = null;
        try {
            HashMap<String, Object> requestParams = new HashMap<String, Object>();
            requestParams.put("companyid", paramJobj.getString(Constants.companyKey));
            requestParams.put("gcurrencyid", paramJobj.getString("currencyid"));
            //  Date creationDate = authHandler.getDateFormatter(request).parse(request.getParameter("creationdate"));
            try {
                creationDate = df.parse(paramJobj.get("creationdate").toString());
            } catch (ParseException ex) {
                Calendar cal = Calendar.getInstance();
                long billdateValue = (long) Long.parseLong(paramJobj.get("creationdate").toString());
                cal.setTimeInMillis(billdateValue);
                billdateVal = cal.getTime();
                String bddate=df.format(billdateVal);
                try{
                    billdateVal=df.parse(bddate);
                }catch(ParseException e){
                    billdateVal=cal.getTime();
                }
                creationDate = billdateVal;
            }
            for (int i = 0; i < jArr.length(); i++) {
                double ratio = 0;
                JSONObject jobj = jArr.getJSONObject(i);
                Date invoiceCreationDate = new Date();

                double newrate = 0.0;
                boolean revalFlag = false;
                //            Invoice invoice=(Invoice) session.get(Invoice.class, jobj.getString("billid"));
                result = accountingHandlerDAOobj.getObject(Invoice.class.getName(), jobj.getString("billid"));
                Invoice invoice = (Invoice) result.getEntityList().get(0);
                boolean isopeningBalanceInvoice = invoice.isIsOpeningBalenceInvoice();
                double exchangeRate = 0d;
                invoiceCreationDate = invoice.getCreationDate();
                if (!invoice.isNormalInvoice() && invoice.isIsOpeningBalenceInvoice()) {
                    exchangeRate = invoice.getExchangeRateForOpeningTransaction();
                } else {
                    exchangeRate = invoice.getJournalEntry().getExternalCurrencyRate();
//                    invoiceCreationDate = invoice.getJournalEntry().getEntryDate();
                }

                HashMap<String, Object> invoiceId = new HashMap<String, Object>();
                invoiceId.put("invoiceid", invoice.getID());
                invoiceId.put("companyid", paramJobj.getString(Constants.companyKey));
                //invoiceId.put("isRealised", false);//false for geting only non realised invoice id.
//                if (!invoice.isIsOpeningBalenceInvoice()) {
                result = accJournalEntryobj.getRevalInvoiceId(invoiceId);
                RevaluationHistory history = (result.getEntityList().size()) != 0 ? (RevaluationHistory) result.getEntityList().get(0) : null;
                if (history != null) {
                    exchangeRate = history.getEvalrate();
                    newrate = jobj.optDouble("exchangeratefortransaction", 0.0);
                    revalFlag = true;
                }
//                }
//                

                //            KWLCurrency currency = (KWLCurrency)session.get(KWLCurrency.class, AuthHandler.getCurrencyID(request));
                result = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), paramJobj.optString("currencyid",null));
                KWLCurrency currency = (KWLCurrency) result.getEntityList().get(0);
                String currid = currency.getCurrencyID();
                if (invoice.getCurrency() != null) {
                    currid = invoice.getCurrency().getCurrencyID();
                }

                if (currid.equalsIgnoreCase(currencyid)) {
                    if (history == null && isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        result = accCurrencyobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(requestParams, 1.0, currid, currencyid, invoiceCreationDate, exchangeRate);
                    } else {
                        result = accCurrencyobj.getOneCurrencyToOther(requestParams, 1.0, currid, currencyid, invoiceCreationDate, exchangeRate);
                    }
                } else {
                    if (history == null && isopeningBalanceInvoice && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                        result = accCurrencyobj.getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(requestParams, 1.0, currid, currencyid, invoiceCreationDate, exchangeRate);
                    } else {
                        result = accCurrencyobj.getOneCurrencyToOtherModified(requestParams, 1.0, currid, currencyid, invoiceCreationDate, exchangeRate);
                    }
                }
                double oldrate = (Double) result.getEntityList().get(0);
                Double recinvamount = jobj.getDouble("payment");
                boolean isopeningBalanceRecceipt = jobj.optBoolean("isopeningBalanceRecceipt", false);
                boolean isConversionRateFromCurrencyToBase = jobj.optBoolean("isConversionRateFromCurrencyToBase", false);
                if (jobj.optDouble("exchangeratefortransaction", 0.0) != oldrate && jobj.optDouble("exchangeratefortransaction", 0.0) != 0.0 && Math.abs(jobj.optDouble("exchangeratefortransaction", 0.0) - oldrate) >= 0.000001) {
                    newrate = jobj.optDouble("exchangeratefortransaction", 0.0);
                    ratio = oldrate - newrate;
//                        double roundedExchangeRate=Math.round((1/newrate)*1000000)/1000000d;
//                        amount = (recinvamount-(recinvamount*roundedExchangeRate)*oldrate)*roundedExchangeRate;
                    amount = (recinvamount - (recinvamount / newrate) * oldrate) / newrate;
                    KwlReturnObject bAmtActual = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currencyid, creationDate, newrate);
//                         if(ratio>0){
//                            actualAmount += (Double) bAmtActual.getEntityList().get(0);
//                         }else{
//                            actualAmount -= (Double) bAmtActual.getEntityList().get(0);
//                         }
                    actualAmount += (Double) bAmtActual.getEntityList().get(0);
                } else {
                    if (currid.equalsIgnoreCase(currencyid)) {
                        if (isopeningBalanceRecceipt && isConversionRateFromCurrencyToBase) {// if receipt is opening balance receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                            result = accCurrencyobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(requestParams, 1.0, currid, currencyid, invoiceCreationDate, externalCurrencyRate);
                        } else {
                            result = accCurrencyobj.getOneCurrencyToOther(requestParams, 1.0, currid, currencyid, invoiceCreationDate, externalCurrencyRate);
                        }
                    } else {
                        if (isopeningBalanceRecceipt && isConversionRateFromCurrencyToBase) {// if receipt is opening balance receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                            result = accCurrencyobj.getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(requestParams, 1.0, currid, currencyid, invoiceCreationDate, externalCurrencyRate);
                        } else {
                            result = accCurrencyobj.getOneCurrencyToOtherModified(requestParams, 1.0, currid, currencyid, invoiceCreationDate, externalCurrencyRate);
                        }
                    }
                    if (!revalFlag) {
                        newrate = (Double) result.getEntityList().get(0);
                    }
                    if (Math.abs(jobj.optDouble("exchangeratefortransaction", 0.0) - oldrate) >= 0.000001) {
                        ratio = oldrate - newrate;
                    }
                    amount = recinvamount * ratio;
                    KwlReturnObject bAmtActual = null;
                    if (isopeningBalanceRecceipt && isConversionRateFromCurrencyToBase) {// if receipt is opening balance receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                        bAmtActual = accCurrencyobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amount, currencyid, creationDate, externalCurrencyRate);
                    } else {
                        bAmtActual = accCurrencyobj.getBaseToCurrencyAmount(requestParams, amount, currencyid, creationDate, externalCurrencyRate);
                    }
//                        if(ratio>0){
//                            actualAmount += (Double) bAmtActual.getEntityList().get(0);
//                         }else{
//                            actualAmount -= (Double) bAmtActual.getEntityList().get(0);
//                         }
                    actualAmount += (Double) bAmtActual.getEntityList().get(0);
                }

            }

        } catch (JSONException ex) {
            throw ServiceException.FAILURE("oldReceiptRowsAmount : " + ex.getMessage(), ex);
        }
        return (actualAmount);
    }

    public void updateInvoiceAmountDue(Invoice invoice, Receipt receipt, Company company, double amountReceivedForInvoice, double baseAmountReceivedForInvoice) throws JSONException, ServiceException {
        if (invoice != null) {
            double invoiceAmountDue = invoice.getOpeningBalanceAmountDue();
            invoiceAmountDue -= amountReceivedForInvoice;
            JSONObject invjson = new JSONObject();
            invjson.put("invoiceid", invoice.getID());
            invjson.put("companyid", company.getCompanyID());
            invjson.put("openingBalanceAmountDue", invoiceAmountDue);
            invjson.put(Constants.openingBalanceBaseAmountDue, invoice.getOpeningBalanceBaseAmountDue() - baseAmountReceivedForInvoice);
            invjson.put(Constants.invoiceamountdue, invoice.getInvoiceamountdue() - amountReceivedForInvoice);
            invjson.put(Constants.invoiceamountdueinbase, invoice.getInvoiceAmountDueInBase()- baseAmountReceivedForInvoice);
            accInvoiceDAOObj.updateInvoice(invjson, null);
        }
    }

    public void updateReceiptAmountDue(Receipt receipt, Company company, double amountReceived, double baseAmountReceivedConverted) throws JSONException, ServiceException {
        if (receipt != null) {
            double receiptAmountDue = receipt.getOpeningBalanceAmountDue();
            receiptAmountDue -= amountReceived;
            HashMap receipthm = new HashMap();
            receipthm.put("openingBalanceAmountDue", receiptAmountDue);
            if (receipt.isIsOpeningBalenceReceipt()) {
                receipthm.put(Constants.openingBalanceBaseAmountDue, receipt.getOpeningBalanceBaseAmountDue() - baseAmountReceivedConverted);
            }
            receipthm.put("receiptid", receipt.getID());
            receipthm.put("currencyid", receipt.getCurrency().getCurrencyID());
            receipthm.put("companyid", company.getCompanyID());
            accReceiptDAOobj.saveReceipt(receipthm);
        }
    }

    public void updateOpeningBalance(Receipt receipt, String companyId) throws JSONException, ServiceException {
        if (receipt != null) {
            Set<ReceiptDetail> receiptDetailSet = receipt.getRows();
            if (receiptDetailSet != null && !receipt.isDeleted()) { // if receipt already temporary deleted then amountdue already updated. No need to update amountdue again for permament delete.
                Iterator itr = receiptDetailSet.iterator();
                while (itr.hasNext()) {
                    ReceiptDetail row = (ReceiptDetail) itr.next();
                    double discountAmtInInvoiceCurrency = authHandler.round(row.getDiscountAmount() / row.getExchangeRateForTransaction(), companyId);
                    double discountAmount = row.getDiscountAmount();
                    if (!row.getInvoice().isNormalInvoice() && row.getInvoice().isIsOpeningBalenceInvoice()) {
                        double amountPaid = row.getAmount()+discountAmount;
                        Invoice invoice = row.getInvoice();
                        HashMap<String, Object> requestParams = new HashMap();
                        requestParams.put(Constants.companyid, companyId);
                        requestParams.put("gcurrencyid", receipt.getCompany().getCurrency().getCurrencyID());
                        double externalCurrencyRate = 1d;
                        boolean isopeningBalanceRP = receipt.isIsOpeningBalenceReceipt();
                        Date rpCreationDate = null;
                        rpCreationDate = receipt.getCreationDate();
                        if (!receipt.isNormalReceipt() && receipt.isIsOpeningBalenceReceipt()) {
                            externalCurrencyRate = receipt.getExchangeRateForOpeningTransaction();
                        } else {
//                            rpCreationDate = receipt.getJournalEntry().getEntryDate();
                            externalCurrencyRate = receipt.getJournalEntry().getExternalCurrencyRate();
                        }
                        String fromcurrencyid = receipt.getCurrency().getCurrencyID();
                        KwlReturnObject bAmt = null;
                        if (isopeningBalanceRP && receipt.isConversionRateFromCurrencyToBase()) {// if Receipt is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountPaid, fromcurrencyid, rpCreationDate, externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, amountPaid, fromcurrencyid, rpCreationDate, externalCurrencyRate);
                        }
                        double openingbalanceBaseAmountDue = (Double) bAmt.getEntityList().get(0);

                        double invoiceAmountDue = invoice.getOpeningBalanceAmountDue();
                        double invoiceBaseAmountDue = invoice.getOpeningBalanceBaseAmountDue() + openingbalanceBaseAmountDue;
//                        invoiceAmountDue += amountPaid;
                        if (row.getExchangeRateForTransaction() != 0) {
                            invoiceAmountDue += amountPaid / row.getExchangeRateForTransaction();
                        } else {
                            invoiceAmountDue += amountPaid;
                        }
                        JSONObject invjson = new JSONObject();
                        invjson.put("invoiceid", invoice.getID());
                        invjson.put("companyid", companyId);
                        invjson.put("openingBalanceAmountDue", invoiceAmountDue);
                        invjson.put(Constants.openingBalanceBaseAmountDue, invoiceBaseAmountDue);
                        accInvoiceDAOObj.updateInvoice(invjson, null);
                    } else if (row.getInvoice().isNormalInvoice()) {
                        double amountPaid = row.getAmount()+discountAmount;
                        Invoice invoice = row.getInvoice();

                        double invoiceAmountDue = invoice.getInvoiceamountdue();
                        KwlReturnObject bAmt = null;
                        HashMap<String, Object> requestParams = new HashMap();
                        requestParams.put(Constants.companyid, companyId);
                        requestParams.put(Constants.globalCurrencyKey, invoice.getCompany().getCurrency().getCurrencyID());
                        double externalCurrencyRate = 0d;
                        externalCurrencyRate = invoice.getJournalEntry() != null ? invoice.getJournalEntry().getExternalCurrencyRate() : 1;
                        String fromcurrencyid = invoice.getCurrency().getCurrencyID();
                        bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, amountPaid, fromcurrencyid, invoice.getCreationDate(), externalCurrencyRate);
                        double totalBaseAmountDue = authHandler.round((Double) bAmt.getEntityList().get(0),companyId);
                        double invoiceAmountDueInBase = invoice.getInvoiceAmountDueInBase();
                        invoiceAmountDueInBase += totalBaseAmountDue;
//                        invoiceAmountDue += amountPaid;
                        if (row.getExchangeRateForTransaction() != 0) {
                            invoiceAmountDue += amountPaid / row.getExchangeRateForTransaction();
                        } else {
                            invoiceAmountDue += amountPaid;
                        }
                        JSONObject invjson = new JSONObject();
                        invjson.put("invoiceid", invoice.getID());
                        invjson.put("companyid", companyId);
                        invjson.put(Constants.invoiceamountdue, invoiceAmountDue);
                        invjson.put(Constants.invoiceamountdueinbase, invoiceAmountDueInBase);
                        accInvoiceDAOObj.updateInvoice(invjson, null);
                    }
                }
            }
        }
    }

    public void deleteJEArray(String oldjeid, String companyid) throws ServiceException, AccountingException, SessionExpiredException {
        try {      //delete old invoice
            JournalEntryDetail jed = null;
            if (!StringUtil.isNullOrEmpty(oldjeid)) {
                KwlReturnObject result = accJournalEntryobj.getJournalEntryDetail(oldjeid, companyid);
                List list = result.getEntityList();
                Iterator itr = list.iterator();
                while (itr.hasNext()) {
                    jed = (JournalEntryDetail) itr.next();
                    //Sagar - No need to revert entry from optimized table as entries are already reverted from calling main function in edit case.
                    result = accJournalEntryobj.deleteJournalEntryDetailRow(jed.getID(), companyid);
                }
                result = accJournalEntryobj.permanentDeleteJournalEntry(oldjeid, companyid);
            }
        } catch (Exception ex) {
            //Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    public void deleteChequeOrCard(String id, String companyid) throws ServiceException, AccountingException, SessionExpiredException {
        try {
            if (id != null) {
                accPaymentDAOobj.deleteCard(id, companyid);
                accPaymentDAOobj.deleteChequePermanently(id, companyid);
            }
        } catch (Exception ex) {
            //Logger.getLogger(accInvoiceController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }

    private void saveBankReconsilation(Map<String, Object> requestParams, Map<String, Object> globalParams) throws ServiceException, JSONException, UnsupportedEncodingException {
        HashMap<String, Object> brMap = new HashMap<String, Object>();
        KwlReturnObject crresult = accCurrencyobj.getCurrencyToBaseAmount(globalParams, (Double) requestParams.get("clearingamount"), (String) requestParams.get("currencyid"), (Date) requestParams.get("clearanceDate"), 0);
        double clearingAmount = (Double) crresult.getEntityList().get(0);

        if (requestParams.containsKey("oldjeid")) {
            deleteBankReconcilation(requestParams);
        }

        brMap.put("startdate", (Date) requestParams.get("startDate"));
        brMap.put("enddate", (Date) requestParams.get("endDate"));
        brMap.put("clearanceDate", (Date) requestParams.get("clearanceDate"));
        brMap.put("clearingamount", clearingAmount);
        brMap.put("endingamount", (Double) requestParams.get("endingAmount"));
        brMap.put("accountid", (String) requestParams.get("bankAccountId"));
        brMap.put("companyid", (String) requestParams.get("companyId"));
        KwlReturnObject brresult = accBankReconciliationObj.addBankReconciliation(brMap);
        BankReconciliation br = (BankReconciliation) brresult.getEntityList().get(0);
        String brid = br.getID();
        Receipt receipt = null;
        BillingReceipt breceipt = null;
        if (requestParams.containsKey("receipt")) {
            receipt = (Receipt) requestParams.get("receipt");
        } else {
            breceipt = (BillingReceipt) requestParams.get("breceipt");
        }
        JournalEntry entry = receipt != null ? receipt.getJournalEntry() : breceipt.getJournalEntry();
        Set details = entry.getDetails();
        Iterator iter = details.iterator();
        String accountName = "";
        while (iter.hasNext()) {
            JournalEntryDetail d = (JournalEntryDetail) iter.next();
            if (d.isDebit()) {
                continue;
            }
            accountName += d.getAccount().getName() + ", ";
        }
        accountName = accountName.substring(0, Math.max(0, accountName.length() - 2));
        HashSet hs = new HashSet();
        boolean isMultiDebit = Boolean.parseBoolean(requestParams.get("ismultidebit").toString());
        JSONArray jArr = (JSONArray) requestParams.get("details");
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject jobj = jArr.getJSONObject(i);


            KwlReturnObject crresult1 = accCurrencyobj.getCurrencyToBaseAmount(globalParams, (isMultiDebit ? jobj.getDouble("dramount") : jobj.getDouble("payment")), jobj.getString("currencyid"), (Date) requestParams.get("clearanceDate"), 0);
            double amount = (Double) crresult1.getEntityList().get(0);
            HashMap<String, Object> brdMap = new HashMap<String, Object>();
            brdMap.put("companyid", (String) requestParams.get("companyId"));
            brdMap.put("amount", amount);
            brdMap.put("jeid", entry.getID());
            brdMap.put("accountname", accountName);
            brdMap.put("debit", true);
            brdMap.put("brid", brid);
            KwlReturnObject brdresult = accBankReconciliationObj.addBankReconciliationDetail(brdMap);
            BankReconciliationDetail brd = (BankReconciliationDetail) brdresult.getEntityList().get(0);
            hs.add(brd);
        }
    }

    private void deleteBankReconcilation(Map<String, Object> requestParams) throws ServiceException {
        if (requestParams.containsKey("oldjeid")) {
            KwlReturnObject brdresult1 = accBankReconciliationObj.getBRfromJE((String) requestParams.get("oldjeid"), (String) requestParams.get("companyId"), true);
            if (brdresult1.getRecordTotalCount() > 0) {
                BankReconciliation br = null;
                for (int i = 0; i < brdresult1.getEntityList().size(); i++) {
                    BankReconciliationDetail brd = (BankReconciliationDetail) brdresult1.getEntityList().get(i);
                    accBankReconciliationObj.permenantDeleteBankReconciliationDetail(brd.getID(), (String) requestParams.get("companyId"));
                    if (br == null) {
                        br = brd.getBankReconciliation();
                    }
                }
                accBankReconciliationObj.permenantDeleteBankReconciliation(br.getID(), (String) requestParams.get("companyId"));
            }
        }
    }

    public String PostJEFORReevaluation(HttpServletRequest request, double finalAmountReval, String companyid, CompanyAccountPreferences preferences, String basecurrency) {
        String jeid = "";
        try {
            String jeentryNumber = "";
            String jeSeqFormatId = "";
            String jeIntegerPart = "";
            String jeDatePrefix = "";
            String jeDateAfterPrefix = "";
            String jeDateSuffix = "";
            DateFormat df = authHandler.getDateOnlyFormat(request);
            /**
             * added Link Date to Realised JE. while link Advanced Payment to
             * Reevaluated Invoice.
             */
            String creationDate = !StringUtil.isNullObject(request.getParameter("linkingdate")) ? request.getParameter("linkingdate") : request.getParameter("creationdate");
            Date jeCreationDate = StringUtil.isNullOrEmpty(creationDate) ? new Date() : df.parse(creationDate);
            boolean jeautogenflag = false;
            synchronized (this) {
                HashMap<String, Object> JEFormatParams = new HashMap<String, Object>();
                JEFormatParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                JEFormatParams.put("modulename", CompanyPreferencesConstants.AUTOJOURNALENTRY);
                JEFormatParams.put("companyid", companyid);
                JEFormatParams.put("isdefaultFormat", true);

                KwlReturnObject kwlObj = accCompanyPreferencesObj.getSequenceFormat(JEFormatParams);
                SequenceFormat format = (SequenceFormat) kwlObj.getEntityList().get(0);
                Map<String, Object> seqNumberMap = new HashMap<String, Object>();
                seqNumberMap = accCompanyPreferencesObj.getNextAutoNumber_Modified(companyid, StaticValues.AUTONUM_JOURNALENTRY, format.getID(), false, jeCreationDate);
                jeentryNumber = (String)seqNumberMap.get(Constants.AUTO_ENTRYNUMBER);  //next auto generated number
                jeIntegerPart = (String)seqNumberMap.get(Constants.SEQNUMBER);//integer Part
                jeDatePrefix = (String)seqNumberMap.get(Constants.DATEPREFIX);//Date Prefix Part
                jeDateAfterPrefix = (String)seqNumberMap.get(Constants.DATEAFTERPREFIX);//Date After Prefix Part
                jeDateSuffix = (String)seqNumberMap.get(Constants.DATESUFFIX);//Date Suffix Part

                jeSeqFormatId = format.getID();
                jeautogenflag = true;
            }
            boolean creditDebitFlag = true;
            if (finalAmountReval < 0) {
                finalAmountReval = -(finalAmountReval);
                creditDebitFlag = false;
            }
            
            Map<String, Object> jeDataMapReval = AccountingManager.getGlobalParams(request);
            jeDataMapReval.put("entrynumber", jeentryNumber);
            jeDataMapReval.put("autogenerated", jeautogenflag);
            jeDataMapReval.put(Constants.SEQFORMAT, jeSeqFormatId);
            jeDataMapReval.put(Constants.SEQNUMBER, jeIntegerPart);
            jeDataMapReval.put(Constants.DATEPREFIX, jeDatePrefix);
            jeDataMapReval.put(Constants.DATEAFTERPREFIX, jeDateAfterPrefix);
            jeDataMapReval.put(Constants.DATESUFFIX, jeDateSuffix);
            jeDataMapReval.put("entrydate", jeCreationDate);
            jeDataMapReval.put("companyid", companyid);
            //jeDataMapReval.put("memo", "Realised Gain/Loss");
            jeDataMapReval.put("currencyid", basecurrency);
            jeDataMapReval.put("isReval", 2);
            Set jedetailsReval = new HashSet();
            KwlReturnObject jeresult = accJournalEntryobj.saveJournalEntry(jeDataMapReval);//Create Journal entry without JEdetails
            JournalEntry journalEntry = (JournalEntry) jeresult.getEntityList().get(0);
            jeid = journalEntry.getID();
            jeDataMapReval.put("jeid", jeid);
            JSONObject jedjsonreval = new JSONObject();
            jedjsonreval.put("srno", jedetailsReval.size() + 1);
            jedjsonreval.put("companyid", companyid);
            jedjsonreval.put("amount", finalAmountReval);//rateDecreased?(-1*amountDiff):
            jedjsonreval.put("accountid", preferences.getForeignexchange().getID());
            jedjsonreval.put("debit", creditDebitFlag ? true : false);
            jedjsonreval.put("jeid", jeid);
            KwlReturnObject jedresult = accJournalEntryobj.addJournalEntryDetails(jedjsonreval);
            JournalEntryDetail jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetailsReval.add(jed);
            /*
             * Featching Custom field/Dimension Data from Company prefrences.
             */
            String customfield = "";
            String lineleveldimensions = "";
            KwlReturnObject result = accJournalEntryobj.getRevaluationJECustomData(companyid);
            RevaluationJECustomData revaluationJECustomData = (result != null && result.getEntityList().size() > 0  && result.getEntityList().get(0) != null) ? (RevaluationJECustomData) result.getEntityList().get(0) : null;
            if (revaluationJECustomData != null) {
                customfield = revaluationJECustomData.getCustomfield();
                lineleveldimensions = revaluationJECustomData.getLineleveldimensions();
            }

            /*
             * Make dimensions entry
             */
            if (!StringUtil.isNullOrEmpty(lineleveldimensions)) {
                JSONArray jcustomarray = new JSONArray(lineleveldimensions);
                HashMap<String, Object> customrequestParams = new HashMap<>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                customrequestParams.put("modulerecid", jed.getID());
                customrequestParams.put("recdetailId", jed.getID());
                customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    //jed.setAccJEDetailCustomData((AccJEDetailCustomData) hibernateTemplate.get(AccJEDetailCustomData.class, jed.getID()));
                    jedjsonreval = new JSONObject();
                    jedjsonreval.put("accjedetailcustomdata", jed.getID());
                    jedjsonreval.put("jedid", jed.getID());
                    jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjsonreval);
                }
            }
            
            String unrealised_accid = "";
            if (preferences.getUnrealisedgainloss() != null) {
                unrealised_accid = preferences.getUnrealisedgainloss().getID();
            } else {
                throw new AccountingException(messageSource.getMessage("acc.field.NoUnrealisedGain/Lossaccountfound", null, RequestContextUtils.getLocale(request)));
            }
            jedjsonreval = new JSONObject();
            jedjsonreval.put("companyid", companyid);
            jedjsonreval.put("srno", jedetailsReval.size() + 1);
            jedjsonreval.put("amount", finalAmountReval);
            jedjsonreval.put("accountid", unrealised_accid);
            jedjsonreval.put("debit", creditDebitFlag ? false : true);
            jedjsonreval.put("jeid", jeid);
            jedresult = accJournalEntryobj.addJournalEntryDetails(jedjsonreval);
            jed = (JournalEntryDetail) jedresult.getEntityList().get(0);
            jedetailsReval.add(jed);

            /*
             * Make dimensions entry
             */
            if (!StringUtil.isNullOrEmpty(lineleveldimensions)) {
                JSONArray jcustomarray = new JSONArray(lineleveldimensions);
                HashMap<String, Object> customrequestParams = new HashMap<>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JEDetail_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEDetailId);// Constants.Acc_JEDetail_recdetailId
                customrequestParams.put("modulerecid", jed.getID());
                customrequestParams.put("recdetailId", jed.getID());
                customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInvDetail_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    //jed.setAccJEDetailCustomData((AccJEDetailCustomData) hibernateTemplate.get(AccJEDetailCustomData.class, jed.getID()));
                    jedjsonreval = new JSONObject();
                    jedjsonreval.put("accjedetailcustomdata", jed.getID());
                    jedjsonreval.put("jedid", jed.getID());
                    jedresult = accJournalEntryobj.updateJournalEntryDetails(jedjsonreval);
                }
            }
            
            jeDataMapReval.put("jedetails", jedetailsReval);
            jeDataMapReval.put("externalCurrencyRate", 0.0);
            jeresult = accJournalEntryobj.saveJournalEntry(jeDataMapReval);
            /*
             * Make custom field entry
             */
            if (!StringUtil.isNullOrEmpty(customfield)) {
                JSONArray jcustomarray = new JSONArray(customfield);
                HashMap<String, Object> customrequestParams = new HashMap<>();
                customrequestParams.put("customarray", jcustomarray);
                customrequestParams.put("modulename", Constants.Acc_JE_modulename);
                customrequestParams.put("moduleprimarykey", Constants.Acc_JEid);
                customrequestParams.put("modulerecid", jeid);
                customrequestParams.put("moduleid", Constants.Acc_GENERAL_LEDGER_ModuleId);
                customrequestParams.put("companyid", companyid);
                customrequestParams.put("customdataclasspath", Constants.Acc_BillInv_custom_data_classpath);
                KwlReturnObject customDataresult = fieldDataManagercntrl.setCustomData(customrequestParams);
                if (customDataresult != null && customDataresult.getEntityList().size() > 0) {
                    Map<String, Object> customjeDataMap = new HashMap<>();
                    customjeDataMap.put("accjecustomdataref", jeid);
                    customjeDataMap.put("jeid", jeid);
                    customjeDataMap.put("istemplate", journalEntry.getIstemplate());
                    customjeDataMap.put("isReval", journalEntry.getIsReval());
                    accJournalEntryobj.updateCustomFieldJournalEntry(customjeDataMap);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jeid;
    }

  @Override  
    public List<JSONObject> getReceiptJson(JSONObject paramJobj, List list, List<JSONObject> jsonObjectlist) throws SessionExpiredException, ServiceException {
        try {
            HashMap<String, Object> rRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
            HashMap<String, Object> requestParams = new HashMap();
            String companyid = paramJobj.getString(Constants.companyKey);
            String currencyid =paramJobj.getString(Constants.globalCurrencyKey);
            boolean isExport = false;
            if (paramJobj.optString("isExport",null)!=null) {
                isExport = Boolean.parseBoolean(paramJobj.optString("isExport"));
            }
            requestParams.put("companyid", companyid);
            requestParams.put("gcurrencyid", currencyid);
            String fileType = StringUtil.isNullOrEmpty(paramJobj.optString("filetype",null))?"":paramJobj.optString("filetype");
            
            int permCode=paramJobj.optInt("permCode");
            
            KwlReturnObject extraPrefesult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraPref = (ExtraCompanyPreferences) extraPrefesult.getEntityList().get(0);
            String userId = paramJobj.getString(Constants.useridKey);
            
            requestParams.put(Constants.onlydateformat, authHandler.getOnlyDateFormat());
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), paramJobj.optString(Constants.globalCurrencyKey));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat df = authHandler.getDateOnlyFormat();
            
            DateFormat userdf = authHandler.getUserDateFormatterWithoutTimeZone(paramJobj);
            DateFormat onlyDateDF = userdf;
            if(requestParams.containsKey(Constants.onlydateformat)) {
                onlyDateDF = (DateFormat) requestParams.get(Constants.onlydateformat);
            }
            HashMap<String, Object> fieldrequestParamsGlobalLevel = new HashMap();
            HashMap<String, String> customFieldMapGlobalLevel = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMapGlobalLevel = new HashMap<String, String>();
            HashMap<String, String> customRichTextMapGlobalLevel = new HashMap<String, String>();
            fieldrequestParamsGlobalLevel.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParamsGlobalLevel.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Receive_Payment_ModuleId));
            HashMap<String, String> replaceFieldMapGlobalLevel = new HashMap<String, String>();
            HashMap<String, Integer> FieldMapGlobalLevel = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParamsGlobalLevel, replaceFieldMapGlobalLevel, customFieldMapGlobalLevel, customDateFieldMapGlobalLevel, customRichTextMapGlobalLevel);

            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            
            //params to send to get billing address
            HashMap<String, Object> addressParams = new HashMap<String, Object>();
            addressParams.put("companyid", companyid);
            addressParams.put("isDefaultAddress", true); //always true to get defaultaddress
            addressParams.put("isBillingAddress", true); //true to get billing address
            for (Object objectArr :list) {
                JSONArray jArr1 = new JSONArray();
                Object[] row = (Object[]) objectArr;
                Receipt receipt = (Receipt) row[0];
                Account acc = (Account) row[1];
                JSONObject obj = new JSONObject();
                obj.put("withoutinventory", false);
                Vendor vendor = null;
                boolean isLinkedInvoiceClaimed=false;
                if (receipt.getVendor() != null) {
                    KwlReturnObject vendResult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), receipt.getVendor());
                    vendor = (Vendor) vendResult.getEntityList().get(0);
                }
                String address = "";
                String vendorEmailId = "";
                obj.put("isactive", true);
                if (vendor != null) {
                    HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
                    addrRequestParams.put("vendorid", vendor.getID());
                    addrRequestParams.put("companyid", companyid);
                    addrRequestParams.put("isBillingAddress", true);
                    addrRequestParams.put("isDefaultAddress", true);
                    obj.put("billaddress", accountingHandlerDAOobj.getVendorAddress(addrRequestParams)); //document designer
                    VendorAddressDetails vendorAddressDetail = accountingHandlerDAOobj.getVendorAddressObj(addrRequestParams);
                    vendorEmailId = vendorAddressDetail != null ? vendorAddressDetail.getEmailID() : "";
                    obj.put("billingEmail", vendorEmailId);
                    obj.put("isactive", vendor.isActivate()); 
                }
                boolean customesDocumentincludeflag=true;
                boolean customersDocumentDonotIncludeFlag=false;
                Customer customer = receipt.getCustomer();//(Customer) cresult.getEntityList().get(0);
                if (!((permCode & Constants.CUSTOMER_VIEWALL_PERMCODE)==Constants.CUSTOMER_VIEWALL_PERMCODE) && customer != null && extraPref!=null &&extraPref.isEnablesalespersonAgentFlow() && customer.isIsCusotmerAvailableOnlyToSalespersons() && !StringUtil.isNullOrEmpty(userId)) {
                    KwlReturnObject result = accCustomerDAOObj.getMultiSalesPersonIDs(customer.getID());
                    if (result != null && result.getEntityList().size() > 0) {
                        List<SalesPersonMapping> spmlist = result.getEntityList();
                        for (SalesPersonMapping spm : spmlist) {
                            if (spm.getSalesperson().getUser() != null) {
                                if ((spm.getSalesperson().getUser().getUserID().equals(userId))) {
                                    customesDocumentincludeflag = false;
                                } else {
                                    customersDocumentDonotIncludeFlag = true;
                                }
                            }
                        }
                    }
                }
                if (customersDocumentDonotIncludeFlag && customesDocumentincludeflag) {//"customesDocumentincludeflag" flag remain "true" when no one salespersons userid matches with logged in user
                    continue;
                }
                
                obj.put("personemail", customer == null ? "" : customer.getEmail());
                if (customer != null) {
                    addressParams.put("customerid", customer.getID());
                    address = accountingHandlerDAOobj.getCustomerAddress(addressParams);
                } else {

                    address = "";
                }
                if (customer != null) {
                    CustomerAddressDetails customerAddressDetails = accountingHandlerDAOobj.getCustomerAddressobj(addressParams);
                    String customerEmailId = customerAddressDetails != null ? customerAddressDetails.getEmailID() : "";
                    obj.put("billingEmail", customerEmailId);
                    obj.put("isactive", customer.isActivate()); 
                }
                obj.put("address", address);
                String jeNumber=receipt.getJournalEntry().getEntryNumber();
                String jeIds=receipt.getJournalEntry().getID();
                String jeIdEntryDate = onlyDateDF.format(receipt.getJournalEntry().getEntryDate());
                if(receipt.getJournalEntryForBankCharges()!=null){
                    jeNumber+=","+receipt.getJournalEntryForBankCharges().getEntryNumber();
                    jeIds+=","+receipt.getJournalEntryForBankCharges().getID();
                    jeIdEntryDate += "," + onlyDateDF.format(receipt.getJournalEntryForBankCharges().getEntryDate());
                }
                if (receipt.getJournalEntryForBankInterest() != null) {
                    jeNumber += "," + receipt.getJournalEntryForBankInterest().getEntryNumber();
                    jeIds += "," + receipt.getJournalEntryForBankInterest().getID();
                    jeIdEntryDate += "," + onlyDateDF.format(receipt.getJournalEntryForBankInterest().getEntryDate());
                }   
                if (StringUtil.equal(fileType, "print") ||StringUtil.equal(fileType, "pdf") || StringUtil.equal(fileType, "csv")|| StringUtil.equal(fileType, "xls")||	StringUtil.equal(fileType,"detailedXls")) {//code under if condition only for print case. no need to execute for other cases.
                    String usedDocumentNumbers = getDocumentNumbersUsedInReceipt(receipt);
                    obj.put("useddocumentnumber", usedDocumentNumbers);
                }
                HashMap<String,Object> map = new HashMap<>();
                map.put("companyid", receipt.getCompany().getCompanyID());    
                map.put("badDebtType", 0);   
                if(receipt.getLinkDetailReceipts() != null && !receipt.getLinkDetailReceipts().isEmpty()) {
                    Set<LinkDetailReceipt> linkedDetailReceiptList = receipt.getLinkDetailReceipts();
                    for(LinkDetailReceipt ldprow : linkedDetailReceiptList) {
                        if(!StringUtil.isNullOrEmpty(ldprow.getLinkedGainLossJE())) {
                            jeIds += "," + ldprow.getLinkedGainLossJE();
                            KwlReturnObject linkedje = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), ldprow.getLinkedGainLossJE());
                            JournalEntry linkedJEObject = (JournalEntry) linkedje.getEntityList().get(0);
                            jeNumber += "," + linkedJEObject.getEntryNumber();
                            jeIdEntryDate += "," + onlyDateDF.format(linkedJEObject.getEntryDate());
                        }
                        /*
                         * Checking whether at least one invoice linked to payment is claimed. If found, system will not allow to delete/edit such payment.
                         */
                        if((ldprow.getInvoice().getBadDebtType()==1 || ldprow.getInvoice().getBadDebtType() == 2)){
                            map.put("invoiceid", ldprow.getInvoice().getID());
                            KwlReturnObject result = accInvoiceDAOObj.getBadDebtInvoiceMappingForInvoice(map);
                            List<BadDebtInvoiceMapping> maplist = result.getEntityList();                            
                            if (maplist != null && !maplist.isEmpty()) {
                                BadDebtInvoiceMapping mapping = maplist.get(0);
//                                if (!isLinkedInvoiceClaimed && mapping.getBadDebtClaimedDate().getTime() > receipt.getJournalEntry().getEntryDate().getTime()) {
                                if (!isLinkedInvoiceClaimed && mapping.getBadDebtClaimedDate().getTime() > receipt.getCreationDate().getTime()) {
                                    isLinkedInvoiceClaimed = true;
                                    obj.put("isLinkedInvoiceIsClaimed", isLinkedInvoiceClaimed);
                                }
                            }
                        }
                    }
                }
                if (receipt.getLinkDetailReceiptsToDebitNote() != null && !receipt.getLinkDetailReceiptsToDebitNote().isEmpty()) {
                    Set<LinkDetailReceiptToDebitNote> linkedDetailReceiptList = receipt.getLinkDetailReceiptsToDebitNote();
                    for (LinkDetailReceiptToDebitNote ldprow : linkedDetailReceiptList) {
                        if (!StringUtil.isNullOrEmpty(ldprow.getLinkedGainLossJE())) {
                            if (!StringUtil.isNullOrEmpty(ldprow.getLinkedGainLossJE())) {
                                jeIds += "," + ldprow.getLinkedGainLossJE();
                                KwlReturnObject linkedje = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), ldprow.getLinkedGainLossJE());
                                JournalEntry linkedJEObject = (JournalEntry) linkedje.getEntityList().get(0);
                                jeNumber += "," + linkedJEObject.getEntryNumber();
                                jeIdEntryDate += "," + onlyDateDF.format(linkedJEObject.getEntryDate());
                            }
                        }
                    }
                }
                if (receipt.getLinkDetailReceiptsToAdvancePayment() != null && !receipt.getLinkDetailReceiptsToAdvancePayment().isEmpty()) {
                    Set<LinkDetailReceiptToAdvancePayment> linkedDetailReceiptList = receipt.getLinkDetailReceiptsToAdvancePayment();
                    for (LinkDetailReceiptToAdvancePayment ldprow : linkedDetailReceiptList) {
                        if (!StringUtil.isNullOrEmpty(ldprow.getLinkedGainLossJE())) {
                            if (!StringUtil.isNullOrEmpty(ldprow.getLinkedGainLossJE())) {
                                jeIds += "," + ldprow.getLinkedGainLossJE();
                                KwlReturnObject linkedje = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), ldprow.getLinkedGainLossJE());
                                JournalEntry linkedJEObject = (JournalEntry) linkedje.getEntityList().get(0);
                                jeNumber += "," + linkedJEObject.getEntryNumber();
                                jeIdEntryDate += "," + onlyDateDF.format(linkedJEObject.getEntryDate());
                            }
                        }
                    }
                }
                // Checking whether at least one invoice linked to payment is claimed. If found, system will not allow to delete/edit such payment.
                if(receipt.getRows()!=null && !receipt.getRows().isEmpty()){
                    for(ReceiptDetail detail : receipt.getRows()){
                        if(detail.getInvoice().getBadDebtType()==1 || detail.getInvoice().getBadDebtType() == 2){
                            map.put("invoiceid", detail.getInvoice().getID());
                            KwlReturnObject result = accInvoiceDAOObj.getBadDebtInvoiceMappingForInvoice(map);
                            List<BadDebtInvoiceMapping> maplist = result.getEntityList();
                            if (maplist != null && !maplist.isEmpty()) {
                                BadDebtInvoiceMapping mapping = maplist.get(0);
//                                if (!isLinkedInvoiceClaimed && mapping.getBadDebtClaimedDate().getTime() > receipt.getJournalEntry().getEntryDate().getTime()) {
                                if (!isLinkedInvoiceClaimed && mapping.getBadDebtClaimedDate().getTime() > receipt.getCreationDate().getTime()) {
                                    isLinkedInvoiceClaimed = true;
                                    obj.put("isLinkedInvoiceIsClaimed", isLinkedInvoiceClaimed);
                                }
                            }
                        }
                    }
                }
                HashMap<String, Object> hashMap = new HashMap<String, Object>();
                hashMap.put("invoiceID", receipt.getID());
                hashMap.put("companyid", receipt.getCompany().getCompanyID());
                KwlReturnObject object = accInvoiceDAOObj.getinvoiceDocuments(hashMap);
                int attachemntcount = object.getRecordTotalCount();
                obj.put("attachment", attachemntcount);
                obj.put("billid", receipt.getID());
                obj.put("isOpeningBalanceTransaction", receipt.isIsOpeningBalenceReceipt());
                obj.put("isNormalTransaction", receipt.isNormalReceipt());
                obj.put("companyid", receipt.getCompany().getCompanyID());
                obj.put("companyname", receipt.getCompany().getCompanyName());
                obj.put("entryno", jeNumber);
                obj.put("journalentryid",jeIds);
                obj.put("journalentrydate",jeIdEntryDate);
                obj.put("personid", (customer != null) ? customer.getID() : acc.getID());
//                obj.put("customervendorname", (customer!=null)? customer.getName() : (vendor!=null)? vendor.getName():"");
                obj.put("billno", receipt.getReceiptNumber());
                obj.put("isadvancepayment", receipt.isIsadvancepayment());
                obj.put("isadvancefromvendor", receipt.isIsadvancefromvendor());
                obj.put("ismanydbcr", receipt.isIsmanydbcr());
                obj.put("isprinted", receipt.isPrinted());
                obj.put("isEmailSent", receipt.isIsEmailSent());
                obj.put("isDishonouredCheque", receipt.isIsDishonouredCheque());
                obj.put("bankCharges", receipt.getBankChargesAmount());
                obj.put("nonRefundable", receipt.isNonRefundable());
                obj.put("bankChargesCmb", receipt.getBankChargesAccount() != null ? receipt.getBankChargesAccount().getID() : "");
                obj.put("bankInterest", receipt.getBankInterestAmount());
                obj.put("bankInterestCmb", receipt.getBankInterestAccount() != null ? receipt.getBankInterestAccount().getID() : "");
                obj.put("bankChargesAccCode", receipt.getBankChargesAccount() != null ? receipt.getBankChargesAccount().getAcccode()!=null?receipt.getBankChargesAccount().getAcccode():"" : "");
                obj.put("bankChargesAccName", receipt.getBankChargesAccount() != null ? receipt.getBankChargesAccount().getName()!=null?receipt.getBankChargesAccount().getName():"" : "");
                obj.put("bankInterestAccCode", receipt.getBankInterestAccount() != null ? receipt.getBankInterestAccount().getAcccode()!=null?receipt.getBankInterestAccount().getAcccode():"" : "");
                obj.put("bankInterestAccName", receipt.getBankInterestAccount() != null ? receipt.getBankInterestAccount().getName()!=null?receipt.getBankInterestAccount().getName():"" : "");
                obj.put("paidToCmb", receipt.getReceivedFrom() == null ? "" : receipt.getReceivedFrom().getID());
                obj.put("paidto", receipt.getReceivedFrom() != null ? receipt.getReceivedFrom().getValue() : "");  //to show recived from option in grid
                obj.put("paymentwindowtype", receipt.getPaymentWindowType());
                obj.put(Constants.SEQUENCEFORMATID, receipt.getSeqformat() == null ? "" : receipt.getSeqformat().getID());
                boolean advanceUsed = false;
                if (receipt.getAdvanceid() != null && !receipt.getAdvanceid().isDeleted()) {
                    rRequestParams.clear();
                    filter_names.clear();
                    filter_params.clear();
                    filter_names.add("receipt.ID");
                    filter_params.add(receipt.getAdvanceid().getID());
                    rRequestParams.put("filter_names", filter_names);
                    rRequestParams.put("filter_params", filter_params);
                    KwlReturnObject grdresult = accReceiptDAOobj.getReceiptDetails(rRequestParams);
                    advanceUsed = grdresult.getEntityList().size() > 0 ? true : false;
                }

                Receipt receiptObject = null;
                if (receipt.getInvoiceAdvCndnType() == 2 || receipt.getInvoiceAdvCndnType() == 1) {
                    receiptObject = accReceiptDAOobj.getReceiptObject(receipt);
                    if (receiptObject != null) {
                        obj.put("cndnid", receiptObject.getID());
                    }
                } else if (receipt.getInvoiceAdvCndnType() == 3) {
                    obj.put("cndnid", receipt.getID());
                }
                obj.put("invoiceadvcndntype", receipt.getInvoiceAdvCndnType());
                obj.put("cndnAndInvoiceId", !StringUtil.isNullOrEmpty(receipt.getCndnAndInvoiceId()) ? receipt.getCndnAndInvoiceId() : "");
                obj.put("advanceUsed", advanceUsed);
                obj.put("advanceid", (receipt.getAdvanceid() != null && !receipt.getAdvanceid().isDeleted()) ? receipt.getAdvanceid().getID() : "");
                obj.put("advanceamount", receipt.getAdvanceamount());
                obj.put("advanceamounttype", receipt.getAdvanceamounttype());
                JSONObject jObj = extraCompanyPreferences.getColumnPref() != null ? new JSONObject(extraCompanyPreferences.getColumnPref()) : new JSONObject();
                boolean isPostingDateCheck = false;
                if (!StringUtil.isNullObject(jObj) && jObj.has(Constants.IS_POSTING_DATE_CHECK) && jObj.get(Constants.IS_POSTING_DATE_CHECK) != null && jObj.optBoolean(Constants.IS_POSTING_DATE_CHECK, false)) {
                    isPostingDateCheck = true;
                }
                if (isPostingDateCheck) {
                    obj.put("billdate", df.format(receipt.getCreationDate()));//receiptdate
                    obj.put("billdateinUserFormat", userdf.format(receipt.getCreationDate()));//receiptdate
                } else {
                    obj.put("billdate", df.format(receipt.getJournalEntry().getEntryDate()));//receipt je date
                    obj.put("billdateinUserFormat", userdf.format(receipt.getJournalEntry().getEntryDate()));//receipt je date
                }
                rRequestParams.clear();
                filter_names.clear();
                filter_params.clear();
                filter_names.add("receipt.ID");
                filter_params.add(receipt.getID());
                rRequestParams.put("filter_names", filter_names);
                rRequestParams.put("filter_params", filter_params);
                KwlReturnObject pdoresult = accReceiptDAOobj.getReceiptDetailOtherwise(rRequestParams);
                List<ReceiptDetailOtherwise> list1 = pdoresult.getEntityList();
                double amount = 0, totaltaxamount = 0,amountDueInBase=0;
                double linkedAmountDue = 0;
                obj.put("disableOtherwiseLinking", true);
                String recordsHavingAdvancePaymentsAsRefund="";
                String soRecordsHavingAdvancePayments="";
                int paymentsCountHavingAdvancePaymentsAsRefund=0;
                int soCountHavingAdvancePaymentsAsRefund=0;
                if (receipt.getReceiptAdvanceDetails() != null && !receipt.getReceiptAdvanceDetails().isEmpty()) {
                    for (ReceiptAdvanceDetail advanceDetail : receipt.getReceiptAdvanceDetails()) {
                        linkedAmountDue += advanceDetail.getAmountDue();
//                        KwlReturnObject bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, advanceDetail.getAmountDue(), receipt.getCurrency().getCurrencyID(), receipt.getJournalEntry().getEntryDate(), receipt.getJournalEntry().getExternalCurrencyRate());
                        KwlReturnObject bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, advanceDetail.getAmountDue(), receipt.getCurrency().getCurrencyID(), receipt.getCreationDate(), receipt.getJournalEntry().getExternalCurrencyRate());
                        amountDueInBase += (Double) bAmt.getEntityList().get(0);
                        if (linkedAmountDue <= 0) {
                            obj.put("disableOtherwiseLinking", true);
                        } else {
                            obj.put("disableOtherwiseLinking", false);
                        }
                        if (receipt.getCustomer() != null) {
                            List<Object[]> resultList = accReceiptDAOobj.getAdvanceReceiptUsedInRefundPayment(advanceDetail.getId());
                            for (int i = 0; i < resultList.size(); i++) {
                                Object[] objArray = (Object[]) resultList.get(i);
                                String paymentNumber = objArray[0].toString();
                                recordsHavingAdvancePaymentsAsRefund += "#" + paymentNumber + ",";
                                paymentsCountHavingAdvancePaymentsAsRefund++;
                            }
                            resultList = accReceiptDAOobj.getAdvanceReceiptLinkedWithRefundPayment(receipt.getID(), companyid);
                            for (int i = 0; i < resultList.size(); i++) {
                                Object[] objArray = (Object[]) resultList.get(i);
                                String paymentNumber = objArray[1].toString();
                                recordsHavingAdvancePaymentsAsRefund += "#" + paymentNumber + ",";
                                paymentsCountHavingAdvancePaymentsAsRefund++;
                            }
                            JSONObject params = new JSONObject();
                            params.put("receiptid",receipt.getID());
                            resultList = accReceiptDAOobj.getAdvanceReceiptUsedSalesOrder(params);
                            for (int i = 0; i < resultList.size(); i++) {
                                Object[] objArray = (Object[]) resultList.get(i);
                                String paymentNumber = objArray[1].toString();
                                soRecordsHavingAdvancePayments += "#" + paymentNumber + ",";
                                soCountHavingAdvancePaymentsAsRefund++;
                            }
                        }
                    }
                }
                if(paymentsCountHavingAdvancePaymentsAsRefund!=0){
                    obj.put("isAdvancePaymentUsedAsRefund", true);
                    recordsHavingAdvancePaymentsAsRefund=recordsHavingAdvancePaymentsAsRefund.substring(0, recordsHavingAdvancePaymentsAsRefund.length()-1);
                } else {
                    obj.put("isAdvancePaymentUsedAsRefund", false);
                }
                if (soCountHavingAdvancePaymentsAsRefund != 0) {
                    obj.put("isPaymentLinkedToSalesOrder", true);
                    soRecordsHavingAdvancePayments = soRecordsHavingAdvancePayments.substring(0, soRecordsHavingAdvancePayments.length() - 1);
                } else {
                    obj.put("isPaymentLinkedToSalesOrder", false);
                }
                obj.put("recordsHavingAdvancePaymentsAsRefund", recordsHavingAdvancePaymentsAsRefund);
                obj.put("soRecordsHavingAdvancePayments", soRecordsHavingAdvancePayments);
                
                // 'isRefundTransaction' is used to identify transaction is for refund
                if (receipt.getPaymentWindowType() == 2 && receipt.getVendor() != null && receipt.getReceiptAdvanceDetails() != null && !receipt.getReceiptAdvanceDetails().isEmpty()) {
                    obj.put("isRefundTransaction", true);
                    // 'personid' need to fetch advance payment transaction in case of linking to refund transaction
                    obj.put("personid", (vendor != null) ? vendor.getID() : acc.getID());
                } else {
                    obj.put("isRefundTransaction", false);
                }
                amount = receipt.getDepositAmount();
                amount = authHandler.round(amount, companyid);
                
//                 /*
//                 * Invoice used in Receive payment.
//                 */
//                if (!receipt.getRows().isEmpty()) {
//                    obj.put("isLinked", true);
//                } else if (list1 != null && list1.size() > 0) {
//                    /*
//                     * Receive Payment Against GL
//                     */
//                    obj.put("isLinked", false);
//                    obj.put("detailsjarr", jArr1);
//                } else if (!receipt.getDebitNotePaymentDetails().isEmpty()) {
//                    /*
//                     * Debit Note used in Receive Payment
//                     */
//                    obj.put("isLinked", true);
//                } else {
//                    /*
//                     * Receive Payment Against Advance payment
//                     */
//                    obj.put("isLinked", false);
//                    obj.put("detailsjarr", jArr1);
//                }
                
                /*
                 * If Receive payment is link to Debit note,invoice,advance
                 * payment.
                 */
                
                 if (!receipt.getRows().isEmpty()) {
                    obj.put("otherwise", false);
                } else if (list1 != null && list1.size() > 0) {
                    obj.put("otherwise", true);
                    obj.put("detailsjarr", jArr1);
                } else {
                    obj.put("otherwise", true);
                    obj.put("detailsjarr", jArr1);
                }
                /*
                 For Export Case we are showing "Yes" or "No" value because of in grid we are showing "Yes" or "No"-ERP-30607
                 */
                if (isExport) {
                    obj.put("isLinked", "No");
                } else {
                    obj.put("isLinked", false);
                }
                if (receipt.getReceiptAdvanceDetails() != null && !receipt.getReceiptAdvanceDetails().isEmpty()) { // Payment Details - Against Invoice
                    if ((receipt.getLinkDetailReceipts() != null && !receipt.getLinkDetailReceipts().isEmpty()) || (receipt.getLinkDetailReceiptsToDebitNote() != null && !receipt.getLinkDetailReceiptsToDebitNote().isEmpty()) || (receipt.getLinkDetailReceiptsToAdvancePayment() != null && !receipt.getLinkDetailReceiptsToAdvancePayment().isEmpty())) {
                        if (isExport) {
                            obj.put("isLinked", "Yes");
                        } else {
                            obj.put("isLinked", true);
                        }
                    }
                }

                KwlReturnObject result = accReceiptDAOobj.getReceiptCustomerNames(receipt.getCompany().getCompanyID(), receipt.getID());
                List cNameList = result.getEntityList();
                String customerNames = "";
                for (Object object1 : cNameList) {
                    String tempName = URLEncoder.encode((String) object1, "UTF-8");
                    customerNames += tempName;
                    customerNames += ",";
                }
                customerNames = customerNames.substring(0, Math.max(0, customerNames.length() - 1));
                obj.put("receipttype", receipt.getReceipttype());
                obj.put("amount", authHandler.formattedAmount(amount, companyid));
                obj.put("amountInWords",EnglishNumberToWordsOjb.convert(amount, receipt.getCurrency() == null ? currency: receipt.getCurrency(),countryLanguageId)+" Only.");
                 /*
                 * If Advance Receipt mark as Dishonoured  then amount due set as 0
                 */
                if (receipt.getReceiptAdvanceDetails() != null && !receipt.getReceiptAdvanceDetails().isEmpty() && receipt.isIsDishonouredCheque()) {
                    obj.put("paymentamountdue", authHandler.formattedAmount(0, companyid));
                    obj.put("paymentamountdueinbase", authHandler.formattedAmount(0, companyid));
                } else {
                    obj.put("paymentamountdue", authHandler.formattedAmount(linkedAmountDue, companyid));
                    obj.put("paymentamountdueinbase", authHandler.formattedAmount(amountDueInBase, companyid));
                }

                
                String reccurrencyid = (receipt.getCurrency() == null ? currency.getCurrencyID() : receipt.getCurrency().getCurrencyID());
//                KwlReturnObject bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, amount, reccurrencyid, receipt.getJournalEntry().getEntryDate(), receipt.getJournalEntry().getExternalCurrencyRate());
                KwlReturnObject bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, amount, reccurrencyid, receipt.getCreationDate(), receipt.getJournalEntry().getExternalCurrencyRate());
                double amountinbase = (Double) bAmt.getEntityList().get(0);
                customerNames = StringUtil.DecodeText(customerNames);
                obj.put("amountinbase", authHandler.formattedAmount(amountinbase, companyid));
                obj.put("personname", (vendor==null&&customer==null) ? customerNames : ((customer != null) ? customer.getName() : (vendor != null) ? vendor.getName() : ""));
                obj.put("personcode", (vendor==null&&customer==null) ? "" : ((vendor != null) ? (vendor.getAcccode()==null?"":vendor.getAcccode()) : (customer != null) ? (customer.getAcccode()==null?"":customer.getAcccode()) : ""));    //Used decoder to avoid '+' symbol at white/empty space between words. 
                obj.put("accountcode", (vendor==null&&customer==null) ? "" : ((vendor != null) ? (vendor.getAccount()==null?"":(vendor.getAccount().getAccountCode()==null?"":vendor.getAccount().getAccountCode())) : (customer != null) ? (customer.getAccount()==null?"":(customer.getAccount().getAccountCode()==null?"":customer.getAccount().getAccountCode())) : ""));
                obj.put("accountname", (vendor==null&&customer==null) ? "" : ((vendor != null) ? (vendor.getAccount()==null?"":(vendor.getAccount().getAccountName()==null?"":vendor.getAccount().getAccountName())) : (customer != null) ? (customer.getAccount()==null?"":(customer.getAccount().getAccountName()==null?"":customer.getAccount().getAccountName())) : ""));
  
                //refer ticket ERP-10777
                if(vendor==null&&customer==null){
                    obj.put("personaddress", StringUtil.DecodeText(""));
                } else if(vendor != null){
                    addressParams.put("vendorid", vendor.getID());
                    obj.put("personaddress", StringUtil.DecodeText(accountingHandlerDAOobj.getVendorAddress(addressParams)));    //Used decoder to avoid '+' symbol at white/empty space between words. 
                } else {
                    addressParams.put("customerid", customer.getID());
                    obj.put("personaddress", StringUtil.DecodeText(accountingHandlerDAOobj.getCustomerAddress(addressParams)));    //Used decoder to avoid '+' symbol at white/empty space between words.
                }
                
                obj.put("createdby", receipt.getCreatedby()==null ? "": StringUtil.getFullName(receipt.getCreatedby()));
                obj.put("memo", receipt.getMemo());
                if (receipt.isIsDishonouredCheque()) {
                    obj.put("dishonoured","Cancelled/Dishonored");
                }
                obj.put("deleted", receipt.isDeleted());
                obj.put("currencysymbol", (receipt.getCurrency() == null ? currency.getSymbol() : receipt.getCurrency().getSymbol()));
                if(receipt.getExternalCurrencyRate()==0) {
//                    bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, 1.0d, reccurrencyid, receipt.getJournalEntry().getEntryDate(), receipt.getJournalEntry().getExternalCurrencyRate());
                    bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, 1.0d, reccurrencyid, receipt.getCreationDate(), receipt.getJournalEntry().getExternalCurrencyRate());
                    obj.put("externalcurrencyrate", 1/(Double) bAmt.getEntityList().get(0));
                } else {
                    obj.put("externalcurrencyrate", receipt.getExternalCurrencyRate());
                }
                obj.put("currencyid", (receipt.getCurrency() == null ? currency.getCurrencyID() : receipt.getCurrency().getCurrencyID()));
                obj.put("currencycode", (receipt.getCurrency() == null ? currency.getCurrencyCode() : receipt.getCurrency().getCurrencyCode()));
                obj.put("currencyname",(receipt.getCurrency() == null ? currency.getName(): receipt.getCurrency().getName()));
                obj.put("currencysymbol", (receipt.getCurrency() == null ? (currency.getSymbol()==null?"":currency.getSymbol()) : (receipt.getCurrency().getSymbol()==null?"":receipt.getCurrency().getSymbol())));
                obj.put("methodid", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getID()));
                obj.put("detailtype", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getDetailType()));
                obj.put("paymentmethod", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getMethodName()));
                obj.put("paymentaccount", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getAccount().getName()));//document designer
                obj.put("paymentaccountnumber", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getAccount().getName()));//Document Designer
                obj.put("paymentaccountcode", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getAccount().getAcccode()));//Document Designer
                Boolean isApprovalPendingReport = paramJobj.has("ispendingAproval") ? Boolean.parseBoolean(paramJobj.get("ispendingAproval").toString()) : false;
                String userIdForPending = (String) paramJobj.get("userid");
                String userName="";
//                String userName = (String) paramJobj.get("userName");
                if (isApprovalPendingReport) {                    
                    obj = accReceivePaymentModuleService.getReceivePaymentApprovalPendingJsonData(obj, amountinbase, receipt.getApprovestatuslevel(), companyid, userIdForPending, userName);
                } else {
                   if ( receipt.getApprovestatuslevel() == Constants.APPROVED_STATUS_LEVEL) {
                        obj.put("approvalstatusinfo", "Approved");
                    }
                }
                if (receipt.getPayDetail() != null) {
                    try {
                        obj.put("expirydate", (receipt.getPayDetail().getCard() == null ? "" : df.format(receipt.getPayDetail().getCard().getExpiryDate())));
                        obj.put("refcardno", (receipt.getPayDetail().getCard() == null ? "" : (receipt.getPayDetail().getCard().getCardNo() == null ? "" : receipt.getPayDetail().getCard().getCardNo())));
                    } catch (Exception ae) {
                        obj.put("expirydate", "");
                    }
                    obj.put("refdetail", (receipt.getPayDetail().getCard() == null ? (receipt.getPayDetail().getCheque() == null ? "" : receipt.getPayDetail().getCheque().getDescription()) : receipt.getPayDetail().getCard().getCardType()));
                    obj.put("refno", (receipt.getPayDetail().getCard() == null ? (receipt.getPayDetail().getCheque() == null ? "" : receipt.getPayDetail().getCheque().getChequeNo()) : receipt.getPayDetail().getCard().getRefNo()));
                    obj.put("chequedescription", ((receipt.getPayDetail() == null || receipt.getPayDetail().getCheque() == null) ? "" : (receipt.getPayDetail().getCheque().getDescription() != null ? receipt.getPayDetail().getCheque().getDescription() : "")));
                    obj.put("chequenumber", ((receipt.getPayDetail() == null || receipt.getPayDetail().getCheque() == null) ? "" : receipt.getPayDetail().getCheque().getChequeNo()));
                    obj.put("chequedate", receipt.getPayDetail() == null ? "" : (receipt.getPayDetail().getPaymentMethod().getDetailType() == PaymentMethod.TYPE_BANK) ? (receipt.getPayDetail().getCheque() != null ? userdf.format(receipt.getPayDetail().getCheque().getDueDate()) : "") : "");
                    obj.put("refname", (receipt.getPayDetail().getCard() == null ? (receipt.getPayDetail().getCheque() == null ? "" : (receipt.getPayDetail().getCheque().getBankMasterItem() == null ? receipt.getPayDetail().getCheque().getBankName() : receipt.getPayDetail().getCheque().getBankMasterItem().getID())) : receipt.getPayDetail().getCard().getCardHolder()));
                    obj.put("bankname", (receipt.getPayDetail().getCard() == null ? (receipt.getPayDetail().getCheque() == null ? "" : (receipt.getPayDetail().getCheque().getBankMasterItem() == null ? receipt.getPayDetail().getCheque().getBankName() : receipt.getPayDetail().getCheque().getBankMasterItem().getValue())) : receipt.getPayDetail().getCard().getCardHolder()));
                    obj.put("clearancedate", "");
                    obj.put("paymentstatus", false);
                    KwlReturnObject clearanceDate = accBankReconciliationObj.getBRfromJE(receipt.getJournalEntry().getID(), receipt.getCompany().getCompanyID(), false);
                    if (clearanceDate != null && clearanceDate.getEntityList() != null && clearanceDate.getEntityList().size() > 0) {
                        BankReconciliationDetail brd = (BankReconciliationDetail) clearanceDate.getEntityList().get(0);
                        if (brd.getBankReconciliation().getClearanceDate() != null) {
                            obj.put("clearancedate",df.format(brd.getBankReconciliation().getClearanceDate()));
                            obj.put("paymentstatus", true);
                        }
                    }
                }
                Set<ReceiptDetailOtherwise> receiptDetailOtherwise = receipt.getReceiptDetailOtherwises();
                for(ReceiptDetailOtherwise RDO : receiptDetailOtherwise){  // Tax amount of payment against GL is added.
                    if(RDO.getTaxamount() != 0){
//                        totaltaxamount+= RDO.getTaxamount();
                        if (!RDO.isIsdebit()) {
                            totaltaxamount += RDO.getTaxamount();
                        } else {
                            totaltaxamount = totaltaxamount - RDO.getTaxamount();
                        }
                    }
                }
                if(receipt.getTaxAmount()!=0){                    // This will happen only in case of advance payment for Malaysian Country, user has chosen a GST while receiving advance payment and total amount is splitted into mainamount and tax amount
                    totaltaxamount+=receipt.getTaxAmount();
                }
                totaltaxamount = authHandler.round(totaltaxamount, companyid);                
                obj.put("totaltaxamount", authHandler.formattedAmount(totaltaxamount, companyid));
                obj.put("amountBeforeTax", authHandler.formattedAmount(amount-totaltaxamount, companyid));
                obj.put("isWrittenOff", receipt.isIsWrittenOff());
                Map<String, Object> variableMap = new HashMap<String, Object>();
                HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                Detailfilter_names.add("companyid");
                Detailfilter_params.add(receipt.getCompany().getCompanyID());
                Detailfilter_names.add("journalentryId");
                Detailfilter_params.add(receipt.getJournalEntry().getID());
                Detailfilter_names.add("moduleId");
                Detailfilter_params.add(Constants.Acc_Receive_Payment_ModuleId + "");
                invDetailRequestParams.put("filter_names", Detailfilter_names);
                invDetailRequestParams.put("filter_params", Detailfilter_params);
                KwlReturnObject idcustresult =  accReceiptDAOobj.getReciptPaymentGlobalCustomData(invDetailRequestParams);
                if (idcustresult.getEntityList().size() > 0) {
                    AccJECustomData jeCustom = (AccJECustomData) idcustresult.getEntityList().get(0);
                    AccountingManager.setCustomColumnValues(jeCustom, FieldMapGlobalLevel, replaceFieldMapGlobalLevel, variableMap);
                    DateFormat defaultDateFormat=new SimpleDateFormat(Constants.MMMMdyyyy);
                        Date dateFromDB=null;
                    for (Map.Entry<String, Object> varEntry : variableMap.entrySet()) {
                        String coldata = varEntry.getValue().toString();
                        if (customFieldMapGlobalLevel.containsKey(varEntry.getKey())) {                           
                            String value = "";
                            String Ids[] = coldata.split(",");
                            for (int i = 0; i < Ids.length; i++) {
                                KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(FieldComboData.class.getName(), Ids[i]);
                                FieldComboData fieldComboData = (FieldComboData) rdresult.getEntityList().get(0);
                                if (fieldComboData != null) {
                                    if (!isExport && (fieldComboData.getField().getFieldtype() == 12 || fieldComboData.getField().getFieldtype() == 7)) {
                                        value += Ids[i] != null ? Ids[i] + "," : ",";
                                    } else {
                                        value += fieldComboData.getValue() != null ? fieldComboData.getValue() + "," : ",";
                                        obj.put("Dimension_" + fieldComboData.getField().getFieldlabel(), fieldComboData.getValue() != null ? fieldComboData.getValue() : ""); //to differentiate custom field and dimension in sms payment templates.
                                    }

                                }
                            }
                            if (!StringUtil.isNullOrEmpty(value)) {
                                value = value.substring(0, value.length() - 1);
                            }
                            obj.put(varEntry.getKey(), value);
                        } else if (customDateFieldMapGlobalLevel.containsKey(varEntry.getKey())) {
//                            obj.put(varEntry.getKey(), authHandler.getDateFormatter(request).format(Long.parseLong(coldata)));
                            DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                            try {
                                dateFromDB = defaultDateFormat.parse(coldata);
                                coldata = sdf.format(dateFromDB);
                                //This code change is made because date will not be long value now,it will be date in String form refer ERP-32324 
                            } catch (Exception e) {
                            }
                            obj.put(varEntry.getKey(), coldata);

                        } else if (customRichTextMapGlobalLevel.containsKey(varEntry.getKey())) {
//                             coldata = coldata.replaceAll("\\<.*?\\>", "");
                             coldata = Jsoup.parse(coldata).text();
                             obj.put(varEntry.getKey(), coldata);
                             obj.put(varEntry.getKey() + "_Values", coldata);
                        } else {
                            if (!StringUtil.isNullOrEmpty(coldata)) {
                                String[] coldataArray = coldata.split(",");
                                String Coldata = "";
                                for (int countArray = 0; countArray < coldataArray.length; countArray++) {
                                    Coldata += "'" + coldataArray[countArray] + "',";
                                }
                                Coldata = Coldata.substring(0, Coldata.length() - 1);
                                String ColValue = accAccountDAOobj.getfieldcombodatabyids(Coldata);
                                obj.put(varEntry.getKey(), coldata);
                                obj.put(varEntry.getKey() + "_Values", ColValue);
                            }
                        }
                    }
                }
                jsonObjectlist.add(obj);
            }
            //jobj.put("data", jArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getReceiptJson : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getReceiptJson : " + ex.getMessage(), ex);
        }
        return jsonObjectlist;
    }
    public String getDocumentNumbersUsedInReceipt(Receipt receipt) throws ServiceException {
        String usedDocumentNumbers = "";

        //Used Invoice Number
        if (!receipt.getRows().isEmpty()) {
            Set<ReceiptDetail> rows = receipt.getRows();
            for (ReceiptDetail detail : rows) {
                if (detail.getInvoice() != null) {
                    usedDocumentNumbers += detail.getInvoice().getInvoiceNumber() + ", ";
                }
            }
        }

        //Used Debit Note Number
        if (!receipt.getDebitNotePaymentDetails().isEmpty()) {
            Set<DebitNotePaymentDetails> debitNotePaymentDetails = receipt.getDebitNotePaymentDetails();
            for (DebitNotePaymentDetails details : debitNotePaymentDetails) {
                if (details.getDebitnote() != null) {
                    usedDocumentNumbers += details.getDebitnote().getDebitNoteNumber() + ", ";
                }
            }
        }

        //Used Account Number or Code
        if (!receipt.getReceiptDetailOtherwises().isEmpty()) {
            Set<ReceiptDetailOtherwise> receiptDetailOtherwises = receipt.getReceiptDetailOtherwises();
            for (ReceiptDetailOtherwise details : receiptDetailOtherwises) {
                if (details.getAccount() != null) {
                    String acccode = details.getAccount().getAcccode() == null ? details.getAccount().getAccountName() : details.getAccount().getAcccode();
                    usedDocumentNumbers += acccode + ", ";
                }
            }
        }

        //Refund Payment Number
        if (!StringUtil.isNullOrEmpty(receipt.getVendor()) && !receipt.getReceiptAdvanceDetails().isEmpty()) {
            Set<ReceiptAdvanceDetail> advanceDetails = receipt.getReceiptAdvanceDetails();
            for (ReceiptAdvanceDetail advanceDetail : advanceDetails) {
                List<Object[]> resultList = accountingHandlerDAOobj.getPaymentAdvanceDetailsInRefundCase(advanceDetail.getAdvancedetailid());;
                if (resultList.size() > 0) {
                    Object[] objArray = (Object[]) resultList.get(0);
                    usedDocumentNumbers += objArray[0].toString() + ", ";
                }
            }
        }

        //Linked Invoice Number
        if (!receipt.getLinkDetailReceipts().isEmpty()) {
            Set<LinkDetailReceipt> linkDetailReceipts = receipt.getLinkDetailReceipts();
            for (LinkDetailReceipt details : linkDetailReceipts) {
                if (details.getInvoice() != null) {
                    usedDocumentNumbers += details.getInvoice().getInvoiceNumber() + ", ";
                }
            }
        }

        //Linked Debit Note Number
        if (!receipt.getLinkDetailReceiptsToDebitNote().isEmpty()) {
            Set<LinkDetailReceiptToDebitNote> debitNotePaymentDetails = receipt.getLinkDetailReceiptsToDebitNote();
            for (LinkDetailReceiptToDebitNote details : debitNotePaymentDetails) {
                if (details.getDebitnote() != null) {
                    usedDocumentNumbers += details.getDebitnote().getDebitNoteNumber() + ", ";
                }
            }
        }

        usedDocumentNumbers = usedDocumentNumbers.trim();
        if (usedDocumentNumbers.endsWith(",")) {
            usedDocumentNumbers = usedDocumentNumbers.substring(0, usedDocumentNumbers.length() - 1);
        }

        return usedDocumentNumbers;
    }
    
   @Override 
    public List<JSONObject> getOpeningBalanceReceiptJsonForReport(JSONObject paramJobj, List list, List<JSONObject> jsonObjectlist) {
        List<JSONObject> returnList = new ArrayList<JSONObject>();
        try {
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), paramJobj.optString(Constants.globalCurrencyKey));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            DateFormat onlyDateDF = authHandler.getOnlyDateFormat();
            String fileType = StringUtil.isNullOrEmpty(paramJobj.optString("filetype",null))?"":paramJobj.optString("filetype");
            String companyid =  paramJobj.optString(Constants.companyKey);
            DateFormat userdf=authHandler.getUserDateFormatterWithoutTimeZone(paramJobj);
            boolean isExport = false;
            if (paramJobj.optString("isExport",null)!=null) {
                isExport = Boolean.parseBoolean(paramJobj.optString("isExport"));
            }
            
            HashMap<String, Object> fieldrequestParamsGlobalLevel = new HashMap();
            HashMap<String, String> customFieldMapGlobalLevel = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMapGlobalLevel = new HashMap<String, String>();
            fieldrequestParamsGlobalLevel.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParamsGlobalLevel.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Receive_Payment_ModuleId));
            HashMap<String, String> replaceFieldMapGlobalLevel = new HashMap<String, String>();
            HashMap<String, Integer> FieldMapGlobalLevel = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParamsGlobalLevel, replaceFieldMapGlobalLevel, customFieldMapGlobalLevel, customDateFieldMapGlobalLevel);
            
            int countryLanguageId = Constants.OtherCountryLanguageId; // 0
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            if (extraCompanyPreferences.isAmountInIndianWord()) {
                countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
            }
            
            Iterator itr = list.iterator();
            while (itr.hasNext()) {

                Receipt receipt = (Receipt) itr.next();

                JSONObject obj = new JSONObject();
                obj.put("withoutinventory", false);
                obj.put("isOpeningBalanceTransaction", receipt.isIsOpeningBalenceReceipt());
                obj.put("isNormalTransaction", receipt.isNormalReceipt());
                Customer customer = receipt.getCustomer();
                obj.put("personemail", customer == null ? "" : customer.getEmail());
                obj.put("address", customer == null ? "" : customer.getBillingAddress());
                obj.put("billid", receipt.getID());
                obj.put("companyid", receipt.getCompany().getCompanyID());
                obj.put("companyname", receipt.getCompany().getCompanyName());
                obj.put("entryno", "");
                obj.put("journalentryid", "");
                obj.put("personid", customer.getID());
                obj.put("billno", receipt.getReceiptNumber());
                obj.put("isadvancepayment", receipt.isIsadvancepayment());
                obj.put("isadvancefromvendor", false);
                obj.put("ismanydbcr", false);
                obj.put("bankCharges", 0.0);
                obj.put("bankChargesCmb", "");
                obj.put("bankInterest", 0.0);
                obj.put("bankInterestCmb", "");
                obj.put("paidToCmb", "");

                obj.put("advanceUsed", false);
                obj.put("advanceid", "");
                obj.put("advanceamount", 0.0);
                obj.put("advanceamounttype", 0);
                obj.put("billdate", authHandler.getDateOnlyFormat().format(receipt.getCreationDate()));
//                obj.put("receipttype", receipt.getReceipttype());

                obj.put("amount", authHandler.formattedAmount(receipt.getDepositAmount(), companyid));
                obj.put("amountinbase", authHandler.formattedAmount(receipt.getOriginalOpeningBalanceBaseAmount(), companyid));
                obj.put("personname", customer == null ? "" : customer.getName());
                obj.put("memo", "");
                obj.put("deleted", receipt.isDeleted());
                obj.put("currencysymbol", (receipt.getCurrency() == null ? currency.getSymbol() : receipt.getCurrency().getSymbol()));
                obj.put("externalcurrencyrate", receipt.getExternalCurrencyRate());
                obj.put("currencyid", (receipt.getCurrency() == null ? currency.getCurrencyID() : receipt.getCurrency().getCurrencyID()));
                obj.put("methodid", "");
                obj.put("paymentwindowtype", receipt.getPaymentWindowType());
                obj.put("paymentmethod", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getMethodName()));
                obj.put("currencyname",(receipt.getCurrency() == null ? currency.getName(): receipt.getCurrency().getName()));
                obj.put("currencycode", (receipt.getCurrency() == null ? currency.getCurrencyCode() : receipt.getCurrency().getCurrencyCode()));
                obj.put("createdby", receipt.getCreatedby()==null ? "": StringUtil.getFullName(receipt.getCreatedby()));
                obj.put("amountInWords",EnglishNumberToWordsOjb.convert(authHandler.round(receipt.getDepositAmount(), companyid), receipt.getCurrency() == null ? currency: receipt.getCurrency(),countryLanguageId)+" Only.");
                obj.put("personcode", customer == null ? "" : customer.getAcccode());
                obj.put("billdateinUserFormat", userdf.format(receipt.getCreationDate()));//receiptdate
                obj.put("detailtype", (receipt.getPayDetail() == null ? "" : receipt.getPayDetail().getPaymentMethod().getDetailType()));
                String jeNumber = "";
                String jeIds = "";
                String jeIdEntryDate = "";
                if(receipt.getLinkDetailReceipts() != null && !receipt.getLinkDetailReceipts().isEmpty()) {
                    Set<LinkDetailReceipt> linkedDetailReceiptList = receipt.getLinkDetailReceipts();
                    for(LinkDetailReceipt ldprow : linkedDetailReceiptList) {
                        if(!StringUtil.isNullOrEmpty(ldprow.getLinkedGainLossJE())) {
                            jeIds +=ldprow.getLinkedGainLossJE()+",";
                            KwlReturnObject linkedje = accountingHandlerDAOobj.getObject(JournalEntry.class.getName(), ldprow.getLinkedGainLossJE());
                            JournalEntry linkedJEObject = (JournalEntry) linkedje.getEntityList().get(0);
                            jeNumber += linkedJEObject.getEntryNumber()+",";
                            jeIdEntryDate += onlyDateDF.format(linkedJEObject.getEntryDate()) + ",";
                        }
                    }
                }
                if(!StringUtil.isNullOrEmpty(jeIds)) {
                    jeIds = jeIds.substring(0, jeIds.length()-1);
                }
                if(!StringUtil.isNullOrEmpty(jeNumber)) {
                    jeNumber = jeNumber.substring(0, jeNumber.length()-1);
                }
                if(!StringUtil.isNullOrEmpty(jeIdEntryDate)) {
                    jeIdEntryDate = jeIdEntryDate.substring(0, jeIdEntryDate.length()-1);
                }
                if (StringUtil.equal(fileType, "print")||StringUtil.equal(fileType, "pdf") || StringUtil.equal(fileType, "csv")|| StringUtil.equal(fileType, "xls")||StringUtil.equal(fileType,"detailedXls")) {//code under if condition only for print case. no need to execute for other cases.
                    String usedDocumentNumbers = getDocumentNumbersUsedInReceipt(receipt);
                    obj.put("useddocumentnumber", usedDocumentNumbers);
                }
                obj.put("entryno", jeNumber);
                obj.put("isDishonouredCheque", receipt.isIsDishonouredCheque());
                obj.put("journalentryid",jeIds);
                obj.put("journalentrydate",jeIdEntryDate);
                obj.put("isWrittenOff", receipt.isIsWrittenOff());
                obj.put("chequenumber", ((receipt.getPayDetail() == null || receipt.getPayDetail().getCheque() == null) ? "" : receipt.getPayDetail().getCheque().getChequeNo()));
                 /*
                 For Export Case we are showing "Yes" or "No" value because of in grid we are showing "Yes" or "No"-ERP-30607
                 */
                if (isExport) {
                    obj.put("isLinked", "No");
                } else {
                    obj.put("isLinked", false);
                }
                double linkedAmountDue = receipt.getDepositAmount();
                if (((receipt.getLinkDetailReceipts() != null) && (!receipt.getLinkDetailReceipts().isEmpty()) || ((receipt.getLinkDetailReceiptsToDebitNote()!=null) && (!receipt.getLinkDetailReceiptsToDebitNote().isEmpty())) || receipt.isIsWrittenOff())) {
                    linkedAmountDue = getReceiptAmountDue(receipt);
                    if((!receipt.getLinkDetailReceipts().isEmpty()) || (!receipt.getLinkDetailReceiptsToDebitNote().isEmpty())){
                        obj.put("otherwise", false);
                        if (isExport) {
                            obj.put("isLinked", "Yes");
                        } else {
                            obj.put("isLinked", true);
                        }
                    }
                } else {
                    obj.put("otherwise", true);
                }
                obj.put("paymentamountdue", authHandler.formattedAmount(linkedAmountDue, companyid));
                obj.put("paymentamountdueinbase", authHandler.formattedAmount(receipt.getOpeningBalanceBaseAmountDue(), companyid));
                
                // Add Custom field Values.
                Map<String, Object> variableMap = new HashMap<String, Object>();
                HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                Detailfilter_names.add("companyid");
                Detailfilter_params.add(receipt.getCompany().getCompanyID());
                Detailfilter_names.add("OpeningBalanceReceiptId");
                Detailfilter_params.add(receipt.getID());
                Detailfilter_names.add("moduleId");
                Detailfilter_params.add(Constants.Acc_Receive_Payment_ModuleId + "");
                invDetailRequestParams.put("filter_names", Detailfilter_names);
                invDetailRequestParams.put("filter_params", Detailfilter_params);
                KwlReturnObject idcustresult = accJournalEntryobj.getOpeningBalanceReceiptCustomData(invDetailRequestParams);
                if (idcustresult.getEntityList().size() > 0) {
                    OpeningBalanceReceiptCustomData balanceReceiptCustomData = (OpeningBalanceReceiptCustomData) idcustresult.getEntityList().get(0);
                    AccountingManager.setCustomColumnValues(balanceReceiptCustomData, FieldMapGlobalLevel, replaceFieldMapGlobalLevel, variableMap);
                        JSONObject params = new JSONObject();
                        params.put("companyid", companyid);
                        params.put("isExport", isExport);
                        params.put("userdf", userdf);
                        if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.browsertz, null))) {
                            params.put("browsertz", paramJobj.optString(Constants.browsertz, null));
                        }
                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMapGlobalLevel, customDateFieldMapGlobalLevel, obj, params);
                }
                returnList.add(obj);
            }
        } catch (JSONException ex) {
            returnList = null;
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (ServiceException ex) {
            returnList = null;
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        } catch (SessionExpiredException ex) {
            returnList = null;
            Logger.getLogger(accReceiptController.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }
        if (returnList != null) {
            jsonObjectlist.addAll(returnList);
        }
        return jsonObjectlist;
    }

    @Override
    public double getReceiptAmountDue(Receipt receipt) {
        double amount = 0, totaltaxamount = 0, linkedAmountDue = receipt.getDepositAmount();
        try{
            Iterator itrRow1 = receipt.getLinkDetailReceipts().iterator();
            Iterator itrRow2 = receipt.getLinkDetailReceiptsToDebitNote().iterator();
            if (!receipt.getLinkDetailReceipts().isEmpty()) {
                while (itrRow1.hasNext()) {
                    amount += ((LinkDetailReceipt) itrRow1.next()).getAmount();
                }
            }
            if (receipt.getLinkDetailReceiptsToDebitNote() != null && !receipt.getLinkDetailReceiptsToDebitNote().isEmpty()) {
                while (itrRow2.hasNext()) {
                    amount += ((LinkDetailReceiptToDebitNote) itrRow2.next()).getAmount();
                }
            }
            HashMap<String,Object> map = new HashMap<String, Object>();
            map.put("receiptid", receipt.getID());
            map.put("companyid", receipt.getCompany().getCompanyID());
            KwlReturnObject temp = accReceiptDAOobj.getReceiptWriteOffEntries(map);
            List<ReceiptWriteOff> list = temp.getEntityList();
            for(ReceiptWriteOff R : list){
                amount += R.getWrittenOffAmountInReceiptCurrency();
            }
            linkedAmountDue = receipt.getDepositAmount() - amount;
        } catch(Exception ex){
            Logger.getLogger(AccReceiptServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
        }    
            return linkedAmountDue;
    }

     public JSONObject getReceiptRowsJSONNew(JSONObject paramJObj) throws SessionExpiredException, ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray JArr = new JSONArray();
        try {
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), paramJObj.optString(Constants.globalCurrencyKey));
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            double taxPercent = 0;
            String[] receipt = null;
            
            if (paramJObj.optString("bills",null) != null) {
                receipt = (paramJObj.optString("bills")).split(",");
            } else if (paramJObj.optString("bills") != null && !paramJObj.optString("bills").equals("")) {
                receipt = (String[])paramJObj.get("bills");
            }else if (paramJObj.optString("billid",null) != null && !paramJObj.optString("billid").equals("")) {
                receipt = (paramJObj.optString("billid")).split(",");
            }
            int i = 0;
            HashMap requestParams = getReceiptRequestMapJSON(paramJObj);
            requestParams.put("currencyfilterfortrans", (paramJObj.optString("currencyfilterfortrans",null) == null) ? "" : paramJObj.optString("currencyfilterfortrans"));
            HashMap<String, Object> rRequestParams = new HashMap<String, Object>();
            ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
            filter_names.add("receipt.ID");
            order_by.add("srno");
            order_type.add("asc");
            rRequestParams.put("filter_names", filter_names);
            rRequestParams.put("filter_params", filter_params);
            rRequestParams.put("order_by", order_by);
            rRequestParams.put("order_type", order_type);

            while (receipt != null && i < receipt.length) {
                JSONArray innerJArr = new JSONArray();
                KwlReturnObject result = accountingHandlerDAOobj.getObject(Receipt.class.getName(), receipt[i]);
                Receipt re = (Receipt) result.getEntityList().get(0);
                filter_params.clear();
                filter_params.add(re.getID());
                if(!re.isIsOpeningBalenceReceipt()){
                    KwlReturnObject grdresult = accReceiptDAOobj.getReceiptDetails(rRequestParams);
                    Iterator itr = grdresult.getEntityList().iterator();

                    HashMap<String, Object> fieldrequestParams = new HashMap();
                    HashMap<String, String> customFieldMap = new HashMap<String, String>();
                    HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
                    fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
                    fieldrequestParams.put(Constants.filter_values, Arrays.asList(re.getCompany().getCompanyID(), Constants.Acc_Receive_Payment_ModuleId));
                    HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
                    HashMap<String, String> customRichTextMap = new HashMap<String, String>();
                    HashMap<String, Integer> customRefcolMap = new HashMap<String, Integer>();
                    HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap,customRichTextMap,customRefcolMap);
                    JSONObject jSONObject = new JSONObject();

                    getReceiptDetailArray(paramJObj, re, innerJArr, currency, taxPercent, customFieldMap, FieldMap, replaceFieldMap, itr, requestParams, customDateFieldMap,customRichTextMap);
                    if (innerJArr.length() != 0) {
                        jSONObject.put("type", Constants.PaymentAgainstInvoice);
                        jSONObject.put("billid",re.getID());
                        jSONObject.put("paymentWindowType", re.getPaymentWindowType());
                        jSONObject.put("typedata", innerJArr);
                        jSONObject.put("srNoForRow",innerJArr.getJSONObject(0).optString("srNoForRow"));
                        JArr.put(jSONObject);
                    }
                    JSONArray innerJArrGL = new JSONArray();
                    getReceiptGLArray(re, innerJArrGL, currency, taxPercent, customFieldMap,customDateFieldMap, FieldMap, replaceFieldMap,requestParams, customRichTextMap);
                    JSONObject jSONObjectGL = new JSONObject();
                    if (innerJArrGL.length() != 0) {
                        jSONObjectGL.put("type", Constants.GLPayment);
                        jSONObjectGL.put("billid",re.getID());
                        jSONObjectGL.put("paymentWindowType", re.getPaymentWindowType());
                        jSONObjectGL.put("typedata", innerJArrGL);
                        jSONObjectGL.put("srNoForRow",innerJArrGL.getJSONObject(0).optString("srNoForRow"));
                        JArr.put(jSONObjectGL);
                    }
                    JSONArray innerJArrCNDN = new JSONArray();

                    getReceiptCNDNArray(re, innerJArrCNDN, currency, paramJObj, customFieldMap,customDateFieldMap, FieldMap, replaceFieldMap,requestParams, customRichTextMap);
                    JSONObject jSONObjectCNDN = new JSONObject();
                    if (innerJArrCNDN.length() != 0) {
                        jSONObjectCNDN.put("type", Constants.PaymentAgainstCNDN);
                        jSONObjectCNDN.put("billid",re.getID());
                        jSONObjectCNDN.put("paymentWindowType", re.getPaymentWindowType());
                        jSONObjectCNDN.put("typedata", innerJArrCNDN);
                        jSONObjectCNDN.put("srNoForRow",innerJArrCNDN.getJSONObject(0).optString("srNoForRow"));
                        JArr.put(jSONObjectCNDN);
                    }
                    JSONArray innerJArrLoanDetails = new JSONArray();

                    getReceiptLoneDetails(paramJObj,re, innerJArrLoanDetails, currency);

                    JSONObject jSONObjectLoanDetails =new JSONObject();
                    if (innerJArrLoanDetails.length() != 0) {
                       jSONObjectLoanDetails.put("type", Constants.PaymentAgainstLoanDisbursement);
                       jSONObjectLoanDetails.put("billid",re.getID());
                       jSONObjectLoanDetails.put("paymentWindowType", re.getPaymentWindowType());
                       jSONObjectLoanDetails.put("typedata", innerJArrLoanDetails);
                       JArr.put(jSONObjectLoanDetails);
                    }
                    JSONObject jSONObjectAdvance = new JSONObject();
                    JSONArray innerJArrAdvance = new JSONArray();
                    getReceiptAdvanceArray(re, innerJArrAdvance, currency, customFieldMap,customDateFieldMap, FieldMap, replaceFieldMap,requestParams,customRichTextMap);
                    if (innerJArrAdvance.length() != 0) {
                        jSONObjectAdvance.put("type", Constants.AdvancePayment);
                        jSONObjectAdvance.put("billid",re.getID());
                        jSONObjectAdvance.put("paymentWindowType", re.getPaymentWindowType());
                        jSONObjectAdvance.put("typedata", innerJArrAdvance);
                        jSONObjectAdvance.put("srNoForRow",innerJArrAdvance.getJSONObject(0).optString("srNoForRow"));
                        JArr.put(jSONObjectAdvance);
                    }
                    JSONArray innerJArrLinkedAdvance = new JSONArray();

                    getReceiptLinkArray(paramJObj,re, innerJArrLinkedAdvance, currency, taxPercent, customFieldMap, FieldMap, replaceFieldMap, itr, requestParams, customDateFieldMap,customRichTextMap);
                    JSONObject jSONObjectLinkedAdvance =new JSONObject();
                     if (innerJArrLinkedAdvance.length() != 0) {
                        jSONObjectLinkedAdvance.put("type", Constants.AdvanceLinkedWithInvoicePayment);
                        jSONObjectLinkedAdvance.put("billid",re.getID());
                        jSONObjectLinkedAdvance.put("paymentWindowType", re.getPaymentWindowType());
                        jSONObjectLinkedAdvance.put("typedata", innerJArrLinkedAdvance);
                        jSONObjectLinkedAdvance.put("srNoForRow",innerJArrLinkedAdvance.getJSONObject(0).optString("srNoForRow"));
                        JArr.put(jSONObjectLinkedAdvance);
                     }
                    JSONArray innerJArrLinkedAdvanceAgainstDn = new JSONArray();

                    getReceiptLinkToDebitNoteArray(paramJObj,re, innerJArrLinkedAdvanceAgainstDn, currency, taxPercent, customFieldMap, FieldMap, replaceFieldMap, itr, requestParams, customDateFieldMap);
                    JSONObject jSONObjectLinkedAdvanceDN =new JSONObject();
                    if (innerJArrLinkedAdvanceAgainstDn.length() != 0) {
                       jSONObjectLinkedAdvanceDN.put("type", Constants.AdvanceLinkedWithNotePayment);
                       jSONObjectLinkedAdvanceDN.put("billid",re.getID());
                       jSONObjectLinkedAdvanceDN.put("paymentWindowType", re.getPaymentWindowType());
                       jSONObjectLinkedAdvanceDN.put("typedata", innerJArrLinkedAdvanceAgainstDn);
                       jSONObjectLinkedAdvanceDN.put("srNoForRow",innerJArrLinkedAdvanceAgainstDn.getJSONObject(0).optString("srNoForRow"));
                       JArr.put(jSONObjectLinkedAdvanceDN);
                    }
                    // for getting refund linking details against advance payment
                    JSONArray innerJArrLinkedRefundAgainstAdvPayment = new JSONArray();

                    getReceiptLinkToAdvancePaymentArray(paramJObj, re, innerJArrLinkedRefundAgainstAdvPayment, currency, taxPercent, customFieldMap, FieldMap, replaceFieldMap, itr, requestParams, customDateFieldMap);
                    JSONObject jSONObjectLinkedRefundAdvPayment = new JSONObject();
                    if (innerJArrLinkedRefundAgainstAdvPayment.length() != 0) {
                        jSONObjectLinkedRefundAdvPayment.put("type", Constants.RefundPaymentAgainstAdvancePayment);
                        jSONObjectLinkedRefundAdvPayment.put("billid",re.getID());
                        jSONObjectLinkedRefundAdvPayment.put("paymentWindowType", re.getPaymentWindowType());
                        jSONObjectLinkedRefundAdvPayment.put("typedata", innerJArrLinkedRefundAgainstAdvPayment);
                        jSONObjectLinkedRefundAdvPayment.put("srNoForRow",innerJArrLinkedRefundAgainstAdvPayment.getJSONObject(0).optString("srNoForRow"));
                        JArr.put(jSONObjectLinkedRefundAdvPayment);
                    }
                } else{
                    
                    JSONObject jSONObjectOpeningPayment = new JSONObject();
                    jSONObjectOpeningPayment.put("type", 0);
                    jSONObjectOpeningPayment.put("billid", re.getID());
                    jSONObjectOpeningPayment.put("paymentWindowType", re.getPaymentWindowType());
                    jSONObjectOpeningPayment.put("typedata", "[]");
                    jSONObjectOpeningPayment.put("srNoForRow", "1");
                    jSONObjectOpeningPayment.put("enteramount", re.getDepositAmount());
                    
                    JArr.put(jSONObjectOpeningPayment);
                }        
                i++;
            }
            JSONArray sortedArray = new JSONArray();
            sortedArray = sortJson(JArr);
            if (sortedArray.length() == JArr.length()) {
                JArr = sortedArray;
            }
            jobj.put("data", JArr);
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getReceiptRowsJSON : " + ex.getMessage(), ex);
        }
        return jobj;
    }
      public JSONArray sortJson(JSONArray array) throws JSONException {
        List<JSONObject> jsons = new ArrayList<JSONObject>();
        try {
            for (int i = 0; i < array.length(); i++) {
                jsons.add(array.getJSONObject(i));
            }
            Collections.sort(jsons, new Comparator<JSONObject>() {

                @Override
                public int compare(JSONObject ja, JSONObject jb) {
                    double sr1 = 0, sr2 = 0;
                    try {
                        sr1 = Integer.parseInt(ja.optString("srNoForRow", "0"));
                        sr2 = Integer.parseInt(jb.optString("srNoForRow", "0"));
                    } catch (Exception ex) {
                        Logger.getLogger(AccReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (sr1 > sr2) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });

        } catch (JSONException ex) {
            Logger.getLogger(AccReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(AccReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new JSONArray(jsons);
    }  
    private void getReceiptLinkArray(JSONObject paramJobj, Receipt re, JSONArray innerJArrLinkedAdvance, KWLCurrency currency, double taxPercent, HashMap<String, String> customFieldMap, HashMap<String, Integer> FieldMap, HashMap<String, String> replaceFieldMap, Iterator itr, HashMap requestParams, HashMap<String, String> customDateFieldMap, HashMap<String, String> customRichTextMap) throws SessionExpiredException, ServiceException {
        try {
            boolean isReceiptEdit = Boolean.parseBoolean(paramJobj.optString("isReceiptEdit"));
            DateFormat df = authHandler.getDateOnlyFormat();
            DateFormat userdf = authHandler.getUserDateFormatterWithoutTimeZone(paramJobj);
            String companyid = paramJobj.optString("companyid");
            Iterator itrLinkedPayment = re.getLinkDetailReceipts().iterator();
            boolean isExport = false;
            boolean isForReport = false;
            if (requestParams.containsKey(Constants.isExport) && requestParams.get(Constants.isExport) != null) {
                isExport = Boolean.parseBoolean(requestParams.get(Constants.isExport).toString());
            }
            if (requestParams.containsKey(Constants.isForReport) && requestParams.get(Constants.isForReport) != null) {
                isForReport = Boolean.parseBoolean(requestParams.get(Constants.isForReport).toString());
            }
            while (itrLinkedPayment.hasNext()) {
                LinkDetailReceipt row = (LinkDetailReceipt) itrLinkedPayment.next();

                Date invoiceCreationDate = null;
                double invoiceAmount = 0d;

                double rowAmount = (authHandler.round(row.getAmount(), companyid));
                if (row.getInvoice().isNormalInvoice()) {
//                    invoiceCreationDate = row.getInvoice().getJournalEntry().getEntryDate();
                    invoiceAmount = isReceiptEdit ? row.getInvoice().getCustomerEntry().getAmount() : rowAmount;
                } else {// opening balance invoice creation date
                    invoiceAmount = isReceiptEdit ? row.getInvoice().getOriginalOpeningBalanceAmount() : rowAmount;
                }
                invoiceCreationDate = row.getInvoice().getCreationDate();

                JSONObject obj = new JSONObject();
                obj.put("billid", isReceiptEdit ? row.getInvoice().getID() : re.getID());
                obj.put("isopening", row.getInvoice().isIsOpeningBalenceInvoice());
                obj.put("srno", row.getSrno());
                obj.put("srnoforrow", row.getSrno());
                obj.put("rowid", row.getID());
                obj.put("currencysymbol", row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol());
                obj.put("transectionno", row.getInvoice().getInvoiceNumber());
                obj.put("transectionid", row.getInvoice().getID());
                obj.put("amount", invoiceAmount);
                obj.put("enteramount",  row.getAmount());
                obj.put("accountid", (isReceiptEdit ? row.getInvoice().getAccount().getID() : ""));
                obj.put("linkingdate", row.getReceiptLinkDate()!=null ? df.format(row.getReceiptLinkDate()) : "");
                if (row.getInvoice() != null) {
                    obj.put("currencyidtransaction", row.getInvoice().getCurrency() == null ? (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID()) : row.getInvoice().getCurrency().getCurrencyID());
                    obj.put("currencysymbol", row.getInvoice().getCurrency() == null ? (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol()) : row.getInvoice().getCurrency().getSymbol());
                    obj.put("currencysymboltransaction", row.getInvoice().getCurrency() == null ? (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol()) : row.getInvoice().getCurrency().getSymbol());
                } else {
                    obj.put("currencyidtransaction", (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID()));
                    obj.put("currencysymbol", (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol()));
                    obj.put("currencysymboltransaction", (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol()));
                }
                obj.put("duedate", df.format(row.getInvoice().getDueDate()));
                obj.put("creationdate", df.format(invoiceCreationDate));
                obj.put("creationdateinuserformat", userdf.format(invoiceCreationDate));
                if (row.getInvoice().getTax() != null) {
//                    KwlReturnObject perresult = accTaxObj.getTaxPercent(row.getCompany().getCompanyID(), row.getInvoice().getJournalEntry().getEntryDate(), row.getInvoice().getTax().getID());
                    KwlReturnObject perresult = accTaxObj.getTaxPercent(row.getCompany().getCompanyID(), row.getInvoice().getCreationDate(), row.getInvoice().getTax().getID());
                    taxPercent = (Double) perresult.getEntityList().get(0);
                }
                obj.put("taxpercent", taxPercent);
                obj.put("discount", row.getInvoice().getDiscount() == null ? 0 : row.getInvoice().getDiscount().getDiscountValue());
                obj.put("payment", row.getInvoice().getID());

                if (isReceiptEdit) {
                    obj.put("amountpaid", rowAmount);
                    if (row.getFromCurrency() != null && row.getToCurrency() != null) {
                        obj.put("exchangeratefortransaction", row.getExchangeRateForTransaction());
                        obj.put("amountpaidincurrency", authHandler.round(rowAmount / row.getExchangeRateForTransaction(), companyid));
                    } else {
                        double amount = rowAmount;
                        String fromcurrencyid = (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID());
                        String tocurrencyid = (row.getInvoice().getCurrency() == null ? (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID()) : row.getInvoice().getCurrency().getCurrencyID());
                        double exchangeRate = row.getInvoice().isIsOpeningBalenceInvoice() ? row.getInvoice().getExternalCurrencyRate() : row.getInvoice().getJournalEntry().getExternalCurrencyRate();
//                        Date tranDate = row.getInvoice().isIsOpeningBalenceInvoice() ? row.getInvoice().getCreationDate() : row.getInvoice().getJournalEntry().getEntryDate();
                        Date tranDate = row.getInvoice().getCreationDate();
                        KwlReturnObject bAmt = accCurrencyobj.getOneCurrencyToOtherRoundOff(requestParams, amount, fromcurrencyid, tocurrencyid, tranDate, exchangeRate);
                        amount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                        obj.put("exchangeratefortransaction", amount / rowAmount);
                        obj.put("amountpaidincurrency", amount);
                    }
                } else {
                    if (row.getFromCurrency() != null && row.getToCurrency() != null) {
//                        obj.put("amountpaid", rowAmount / row.getExchangeRateForTransaction());
                        obj.put("amountpaid", row.getAmountInInvoiceCurrency());
                    } else {
                        double amount = rowAmount;
                        String fromcurrencyid = (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID());
                        String tocurrencyid = (row.getInvoice().getCurrency() == null ? (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID()) : row.getInvoice().getCurrency().getCurrencyID());
                        double exchangeRate = row.getInvoice().isIsOpeningBalenceInvoice() ? row.getInvoice().getExternalCurrencyRate() : row.getInvoice().getJournalEntry().getExternalCurrencyRate();
//                        Date tranDate = row.getInvoice().isIsOpeningBalenceInvoice() ? row.getInvoice().getCreationDate() : row.getInvoice().getJournalEntry().getEntryDate();
                        Date tranDate = row.getInvoice().getCreationDate();
                        KwlReturnObject bAmt = accCurrencyobj.getOneCurrencyToOtherRoundOff(requestParams, amount, fromcurrencyid, tocurrencyid, tranDate, exchangeRate);
                        amount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                        obj.put("amountpaid", amount);
                    }
                }

                if (isReceiptEdit) {
                    obj.put("amountpaid", rowAmount);
                    if (row.getFromCurrency() != null && row.getToCurrency() != null) {
                        obj.put("exchangeratefortransaction", row.getExchangeRateForTransaction());
                        obj.put("amountpaidincurrency", authHandler.round(rowAmount / row.getExchangeRateForTransaction(), companyid));
                    } else {
                        double amount = rowAmount;
                        String fromcurrencyid = (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID());
                        String tocurrencyid = (row.getInvoice().getCurrency() == null ? (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID()) : row.getInvoice().getCurrency().getCurrencyID());
                        double exchangeRate = row.getInvoice().isIsOpeningBalenceInvoice() ? row.getInvoice().getExternalCurrencyRate() : row.getInvoice().getJournalEntry().getExternalCurrencyRate();
//                        Date tranDate = row.getInvoice().isIsOpeningBalenceInvoice() ? row.getInvoice().getCreationDate() : row.getInvoice().getJournalEntry().getEntryDate();
                        Date tranDate = row.getInvoice().getCreationDate();
                        KwlReturnObject bAmt = accCurrencyobj.getOneCurrencyToOtherRoundOff(requestParams, amount, fromcurrencyid, tocurrencyid, tranDate, exchangeRate);
                        amount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                        obj.put("exchangeratefortransaction", amount / rowAmount);
                        obj.put("amountpaidincurrency", amount);
                    }
                } else {
                    if (row.getFromCurrency() != null && row.getToCurrency() != null) {
                        obj.put("amountpaid", rowAmount / row.getExchangeRateForTransaction());
                    } else {
                        double amount = rowAmount;
                        String fromcurrencyid = (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID());
                        String tocurrencyid = (row.getInvoice().getCurrency() == null ? (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID()) : row.getInvoice().getCurrency().getCurrencyID());
                        double exchangeRate = row.getInvoice().isIsOpeningBalenceInvoice() ? row.getInvoice().getExternalCurrencyRate() : row.getInvoice().getJournalEntry().getExternalCurrencyRate();
//                        Date tranDate = row.getInvoice().isIsOpeningBalenceInvoice() ? row.getInvoice().getCreationDate() : row.getInvoice().getJournalEntry().getEntryDate();
                        Date tranDate = row.getInvoice().getCreationDate();
                        KwlReturnObject bAmt = accCurrencyobj.getOneCurrencyToOtherRoundOff(requestParams, amount, fromcurrencyid, tocurrencyid, tranDate, exchangeRate);
                        amount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                        obj.put("amountpaid", amount);
                    }
                }
                double amountdue = 0.0;
                double amountDueOriginal = 0.0;
                double totalAmount = 0.0;
                if (row.getInvoice().isNormalInvoice()) {
                    if(Constants.InvoiceAmountDueFlag) {
                           List ll = accInvoiceCommon.getInvoiceDiscountAmountInfo(requestParams, row.getInvoice());
                           amountdue = (Double) ll.get(0);
                           amountDueOriginal = (Double) ll.get(3);
                           totalAmount = row.getInvoice().getCustomerEntry().getAmount();
                        } else {
                            amountdue = accInvoiceCommon.getAmountDue(requestParams, row.getInvoice());
                            requestParams.put("amountDueOriginalFlag", true);
                            amountDueOriginal = accInvoiceCommon.getAmountDue(requestParams, row.getInvoice());
                            requestParams.remove("amountDueOriginalFlag");
                            totalAmount = row.getInvoice().getCustomerEntry().getAmount();
                        }
                    
                    
                } else {
                    amountdue = row.getInvoice().getOpeningBalanceAmountDue() * (row.getExchangeRateForTransaction() == 0 ? 1 : row.getExchangeRateForTransaction());
                    amountDueOriginal = row.getInvoice().getOpeningBalanceAmountDue();
                    totalAmount = row.getInvoice().getOriginalOpeningBalanceAmount();
                }

                amountdue = authHandler.round(amountdue, companyid);
                amountDueOriginal = authHandler.round(amountDueOriginal, companyid);
                totalAmount = authHandler.round(totalAmount, companyid);

                if (row.getFromCurrency() != null && row.getToCurrency() != null) {
                    obj.put("amountduenonnegative", (isReceiptEdit ? (amountDueOriginal * row.getExchangeRateForTransaction()) + rowAmount : amountDueOriginal));
                } else {
                    obj.put("amountduenonnegative", (isReceiptEdit ? amountdue + obj.optDouble("amountpaid", 0) : amountdue));
                }
                obj.put("amountDueOriginal", (isReceiptEdit ? amountDueOriginal + obj.optDouble("amountpaidincurrency", 0) : amountDueOriginal));
                obj.put("amountDueOriginalSaved", (isReceiptEdit ? amountDueOriginal + obj.optDouble("amountpaidincurrency", 0) : amountDueOriginal));
                obj.put("amountdue", amountdue);


                obj.put("totalamount", totalAmount);

                // ## Get Custom Field Data 
                Map<String, Object> variableMap = new HashMap<String, Object>();
                HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                Detailfilter_params.add(row.getID());
                invDetailRequestParams.put("filter_names", Detailfilter_names);
                invDetailRequestParams.put("filter_params", Detailfilter_params);
                KwlReturnObject idcustresult = accVendorPaymentDAO.getVendorPaymentCustomData(invDetailRequestParams);
                JSONArray dimensionArr=new JSONArray();
                if (idcustresult.getEntityList().size() > 0) {
                    AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                    AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                    if (jeDetailCustom != null) {
                        JSONObject params = new JSONObject();
                        params.put(Constants.isExport, isExport);
                        params.put(Constants.isForReport, isForReport);
                        fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params, customRichTextMap);
                    }
                }
                obj.put("dimensionArr", dimensionArr);
                obj.put("srNoForRow", row.getSrno());
                innerJArrLinkedAdvance.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("accVendorPaymentController.getPaymentRowsJson : " + ex.getMessage(), ex);
        }
    }
    
    private void getReceiptAdvanceArray(Receipt re, JSONArray innerJArrAdvance, KWLCurrency currency, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap, HashMap<String, Integer> FieldMap, HashMap<String, String> replaceFieldMap,HashMap requestParams,HashMap<String, String> customRichTextMap) throws SessionExpiredException, ServiceException {
        try {
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            for (ReceiptAdvanceDetail advanceDetail : re.getReceiptAdvanceDetails()) {
                JSONObject obj = new JSONObject();
                obj.put("rowid", advanceDetail.getId());
                obj.put("srnoforrow", advanceDetail.getSrNoForRow());
                obj.put("totalamount", advanceDetail.getAmount());
                String companyid=re.getCompany().getCompanyID();
                if (re.isIsDishonouredCheque()) {
                    obj.put("amountdue",0);
                } else {
                    obj.put("amountdue", advanceDetail.getAmountDue());
                }
                obj.put("paidamount", advanceDetail.getAmount());
                obj.put("paidamountOriginal", advanceDetail.getAmount());
                obj.put("description", advanceDetail.getDescription());//document designer
                obj.put("type", Constants.AdvancePayment);
                obj.put("exchangeratefortransaction", 1);
                obj.put("isrefund",false);
                obj.put("currencyidtransaction", (advanceDetail.getReceipt().getCurrency() == null ? currency.getCurrencyID() : advanceDetail.getReceipt().getCurrency().getCurrencyID()));
                obj.put("currencysymbol", (advanceDetail.getReceipt().getCurrency() == null ? currency.getSymbol() : advanceDetail.getReceipt().getCurrency().getSymbol()));
                obj.put("currencysymboltransaction", (advanceDetail.getReceipt().getCurrency() == null ? currency.getSymbol() : advanceDetail.getReceipt().getCurrency().getSymbol()));
                if (re.getCustomer() != null) {
                    obj.put("accountid", re.getCustomer().getID());
                    obj.put("accountname", re.getCustomer().getName());
                    obj.put("accname",  re.getCustomer().getAccount()==null?"":re.getCustomer().getAccount().getName());
                    obj.put("acccode",  re.getCustomer().getAccount()==null?"":re.getCustomer().getAccount().getAcccode());
                    if(advanceDetail.getGST()!=null){      // advanceDetail.getGST() will not be NULL if this is a Malaysian Country and user has chosen the GST for advance receipt
                        obj.put("gstAccountName",  advanceDetail.getGST().getName()!=null?advanceDetail.getGST().getName():"");
                        obj.put("gstAdvancedTaxAmount", advanceDetail.getTaxamount());
                    }
                } else if (!StringUtil.isNullOrEmpty(re.getVendor())) {
                    obj.put("accountid", re.getVendor());
                    KwlReturnObject customerResult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), re.getVendor());
                    Vendor vendor = (Vendor) customerResult.getEntityList().get(0);
                    obj.put("accountname", vendor.getName());
                    obj.put("accname",  vendor.getAccount()==null?"":vendor.getAccount().getName());
                    obj.put("acccode",  vendor.getAccount()==null?"":vendor.getAccount().getAcccode());
                    if(advanceDetail.getAdvancedetailid()!=null) {
                        KwlReturnObject advResult = accountingHandlerDAOobj.getObject(AdvanceDetail.class.getName(), advanceDetail.getAdvancedetailid());
                        AdvanceDetail paymentAdv = (AdvanceDetail) advResult.getEntityList().get(0);
                        Payment payment = paymentAdv.getPayment();
                        KWLCurrency paymentCurrency = payment.getCurrency();
                        double exchangeratefortransaction =  advanceDetail.getExchangeratefortransaction();
                        double enternedAmntOriginal= advanceDetail.getAmount()/exchangeratefortransaction;
                        obj.put("transectionno", payment.getPaymentNumber());
                        obj.put("transectionid", payment.getID());
                        obj.put("totalamount", paymentAdv.getAmount());
                        if (payment.isIsDishonouredCheque()) {
                            obj.put("amountdue", 0);
                        } else {
                            obj.put("amountdue", paymentAdv.getAmountDue());
                        }
                        obj.put("paidamountOriginal", enternedAmntOriginal);
                        obj.put("exchangeratefortransaction", exchangeratefortransaction);
                        obj.put("currencyidtransaction", paymentCurrency.getCurrencyID());
                        obj.put("currencysymbol", paymentCurrency.getSymbol());
                        obj.put("currencysymboltransaction",paymentCurrency.getSymbol());
                        obj.put("isrefund",true);
//                        obj.put("linkingdate",  df.format(re.getJournalEntry().getEntryDate()));
                        obj.put("linkingdate",  df.format(re.getCreationDate()));
                    }
                }
                // ## Get Custom Field Data 
                Map<String, Object> variableMap = new HashMap<String, Object>();
                HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                Detailfilter_params.add(advanceDetail.getId());
                invDetailRequestParams.put("filter_names", Detailfilter_names);
                invDetailRequestParams.put("filter_params", Detailfilter_params);
                boolean isExport = false;
                boolean isForReport = false;
                if (requestParams.containsKey(Constants.isExport) && requestParams.get(Constants.isExport) != null) {
                    isExport = Boolean.parseBoolean(requestParams.get(Constants.isExport).toString());
                }
                if (requestParams.containsKey(Constants.isForReport) && requestParams.get(Constants.isForReport) != null) {
                    isForReport = Boolean.parseBoolean(requestParams.get(Constants.isForReport).toString());
                }
                JSONArray dimensionArr= new JSONArray();
                KwlReturnObject idcustresult =  accVendorPaymentDAO.getVendorPaymentCustomData(invDetailRequestParams);
                if (idcustresult.getEntityList().size() > 0) {
                    AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                    AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                    if (jeDetailCustom != null) {
                        JSONObject params = new JSONObject();
                        params.put(Constants.isExport, isExport);
                        params.put(Constants.isForReport, isForReport);
                        fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params, customRichTextMap);
                    }
                }
                obj.put("dimensionArr",dimensionArr);
                obj.put("srNoForRow", advanceDetail.getSrNoForRow());
                innerJArrAdvance.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getReceiptRowsJSON : " + ex.getMessage(), ex);
        }
    }

    private void getReceiptGLArray(Receipt re, JSONArray innerJArrGL, KWLCurrency currency, double taxPercent, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap, HashMap<String, Integer> FieldMap, HashMap<String, String> replaceFieldMap,HashMap requestParams, HashMap<String, String> customRichTextMap) throws SessionExpiredException, ServiceException {
        try {
             // Comparator for sorting GL records as per sequence in UI
              Set<ReceiptDetailOtherwise> receiptOtherWiseSet = new TreeSet(new Comparator<ReceiptDetailOtherwise>() {

                @Override
                public int compare(ReceiptDetailOtherwise o1, ReceiptDetailOtherwise o2) {
                    if (o1.getSrNoForRow() > o2.getSrNoForRow()) {
                        return 1;
                    } else {
                        return -1;
                    }
                }
            });
            receiptOtherWiseSet.addAll(re.getReceiptDetailOtherwises());
            for (ReceiptDetailOtherwise receiptDetailOtherwise : receiptOtherWiseSet) {
                JSONObject obj = new JSONObject();
                obj.put("rowid", receiptDetailOtherwise.getID());
                obj.put("srnoforrow", receiptDetailOtherwise.getSrNoForRow());
                obj.put("accountid", receiptDetailOtherwise.getAccount().getID());
                obj.put("accountname", receiptDetailOtherwise.getAccount().getName());
                obj.put("accountcode",  receiptDetailOtherwise.getAccount().getAcccode()==null?"":receiptDetailOtherwise.getAccount().getAcccode());
                taxPercent=0.0;
                if (receiptDetailOtherwise.getTax() != null) {
//                    KwlReturnObject perresult = accTaxObj.getTaxPercent(re.getCompany().getCompanyID(), re.getJournalEntry().getEntryDate(), receiptDetailOtherwise.getTax().getID());
                    KwlReturnObject perresult = accTaxObj.getTaxPercent(re.getCompany().getCompanyID(), re.getCreationDate(), receiptDetailOtherwise.getTax().getID());
                    taxPercent = (Double) perresult.getEntityList().get(0);
                }
                obj.put("taxpercent", taxPercent);
                obj.put("description", receiptDetailOtherwise.getDescription());//document designer
                obj.put("totalamount", receiptDetailOtherwise.getAmount());
                obj.put("currencysymbol", receiptDetailOtherwise.getReceipt().getCurrency().getSymbol());
                obj.put("currencyid",  receiptDetailOtherwise.getReceipt().getCurrency().getCurrencyID());
                obj.put("taxamount", receiptDetailOtherwise.getTaxamount());
                obj.put("debit", receiptDetailOtherwise.isIsdebit()); // it is used mainly for Many CN/DN
                if (receiptDetailOtherwise.getTax() != null) {
                    obj.put("taxname", receiptDetailOtherwise.getTax().getName());
                    obj.put("taxcode", receiptDetailOtherwise.getTax().getTaxCode());
                    obj.put("prtaxid", receiptDetailOtherwise.getTax().getID());
                } else {
                    obj.put("taxname", "");
                    obj.put("taxcode", "");
                    obj.put("prtaxid", "");
                }
                obj.put("type", Constants.GLPayment);
                // ## Get Custom Field Data 
                Map<String, Object> variableMap = new HashMap<String, Object>();
                HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                JSONArray dimensionArr=new JSONArray();
                Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                Detailfilter_params.add(receiptDetailOtherwise.getID());
                invDetailRequestParams.put("filter_names", Detailfilter_names);
                invDetailRequestParams.put("filter_params", Detailfilter_params);
                boolean isExport = false;
                boolean isForReport = false;
                if (requestParams.containsKey(Constants.isExport) && requestParams.get(Constants.isExport) != null) {
                    isExport = Boolean.parseBoolean(requestParams.get(Constants.isExport).toString());
                }
                if (requestParams.containsKey(Constants.isForReport) && requestParams.get(Constants.isForReport) != null) {
                    isForReport = Boolean.parseBoolean(requestParams.get(Constants.isForReport).toString());
                }
                KwlReturnObject idcustresult = accVendorPaymentDAO.getVendorPaymentCustomData(invDetailRequestParams);
                if (idcustresult.getEntityList().size() > 0) {
                    AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                    AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);

                    if (jeDetailCustom != null) {
                        JSONObject params = new JSONObject();
                        params.put(Constants.isExport, isExport);
                        params.put(Constants.isForReport, isForReport);//True in row expander case
                        fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params, customRichTextMap);
                    }
                }
                obj.put("dimensionArr",dimensionArr);
                obj.put("srNoForRow", receiptDetailOtherwise.getSrNoForRow());
                innerJArrGL.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getReceiptRowsJSON : " + ex.getMessage(), ex);
        }
    }

    private void getReceiptDetailArray(JSONObject paramJobj, Receipt re, JSONArray innerJArr, KWLCurrency currency, double taxPercent, HashMap<String, String> customFieldMap, HashMap<String, Integer> FieldMap, HashMap<String, String> replaceFieldMap, Iterator itr, HashMap requestParams, HashMap<String, String> customDateFieldMap, HashMap<String, String> customRichTextMap) throws SessionExpiredException, ServiceException {
        try {
            String companyid = paramJobj.optString("companyid");
            boolean isReceiptEdit = Boolean.parseBoolean(paramJobj.optString("isReceiptEdit"));
            if(paramJobj.optString("isReceiptEdit",null) != null){
                isReceiptEdit = Boolean.parseBoolean(paramJobj.optString("isReceiptEdit"));
            }
            DateFormat df = authHandler.getDateOnlyFormat();
            DateFormat userdf = authHandler.getUserDateFormatterWithoutTimeZone(paramJobj);
            boolean isExport = false;
            boolean isForReport = false;
            if (requestParams.containsKey(Constants.isExport) && requestParams.get(Constants.isExport) != null) {
                isExport = Boolean.parseBoolean(requestParams.get(Constants.isExport).toString());
            }
            if (requestParams.containsKey(Constants.isForReport) && requestParams.get(Constants.isForReport) != null) {
                isForReport = Boolean.parseBoolean(requestParams.get(Constants.isForReport).toString());
            }
            while (itr.hasNext()) {
                ReceiptDetail row = (ReceiptDetail) itr.next();

                Date invoiceCreationDate = null;
                double invoiceAmount = 0d;
                double externalCurrencyRate = 0d;

                boolean isopeningBalanceInvoice = row.getInvoice().isIsOpeningBalenceInvoice();
                double rowAmount = (authHandler.round(row.getAmount(), companyid));
                KWLCurrency invoiceCurrency = row.getInvoice() != null ? row.getInvoice().getCurrency() : null;
                boolean isCurrencySame = false;
                if(invoiceCurrency!=null && currency.getCurrencyID().equals(invoiceCurrency.getCurrencyID())) {
                    isCurrencySame = true;
                }
                if (row.getInvoice().isNormalInvoice()) {
//                    invoiceCreationDate = row.getInvoice().getJournalEntry().getEntryDate();
                    invoiceAmount = isReceiptEdit ? row.getInvoice().getCustomerEntry().getAmount() : rowAmount;
                    externalCurrencyRate = row.getInvoice().getJournalEntry().getExternalCurrencyRate();
                } else {// opening balance invoice creation date
                    invoiceAmount = isReceiptEdit ? row.getInvoice().getOriginalOpeningBalanceAmount() : rowAmount;
                    externalCurrencyRate = row.getInvoice().getExchangeRateForOpeningTransaction();
                }
                invoiceCreationDate = row.getInvoice().getCreationDate();

                JSONObject obj = new JSONObject();
//                double exchangeRateForTransaction = row.getExchangeRateForTransaction() != 0 ? row.getExchangeRateForTransaction() : 1;
                obj.put("billid", isReceiptEdit ? row.getInvoice().getID() : re.getID());
                obj.put("isopening", row.getInvoice().isIsOpeningBalenceInvoice());
                obj.put("srno", row.getSrno());
                obj.put("srnoforrow", row.getSrNoForRow());
                obj.put("rowid", row.getID());
                obj.put("currencysymbol", row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol());
                obj.put("transectionno", row.getInvoice().getInvoiceNumber());
                obj.put("transectionid", row.getInvoice().getID());
                obj.put("amount", invoiceAmount);
                obj.put("enteramount",  row.getAmount());
                obj.put("enteramountinbasecurrency",  row.getAmountInBaseCurrency());
                obj.put("description", row.getDescription());//document designer
//                obj.put("discountAmount", authHandler.round((row.getDiscountAmount() / exchangeRateForTransaction), companyid));
                obj.put("discountAmount", row.getDiscountAmountInInvoiceCurrency());
                
                obj.put("accountid", (isReceiptEdit ? row.getInvoice().getAccount().getID() : ""));
                if (row.getInvoice() != null) {
                    obj.put("currencyidtransaction", row.getInvoice().getCurrency() == null ? (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID()) : row.getInvoice().getCurrency().getCurrencyID());
                    obj.put("currencysymbol", row.getInvoice().getCurrency() == null ? (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol()) : row.getInvoice().getCurrency().getSymbol());
                    obj.put("currencysymboltransaction", row.getInvoice().getCurrency() == null ? (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol()) : row.getInvoice().getCurrency().getSymbol());
                } else {
                    obj.put("currencyidtransaction", (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID()));
                    obj.put("currencysymbol", (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol()));
                    obj.put("currencysymboltransaction", (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol()));
                }
                obj.put("duedate", df.format(row.getInvoice().getDueDate()));
//                obj.put("linkingdate", df.format(row.getReceipt().getJournalEntry().getEntryDate()));
                obj.put("linkingdate", df.format(row.getReceipt().getCreationDate()));
                obj.put("creationdate", df.format(invoiceCreationDate));
                obj.put("dateforsort", invoiceCreationDate);
                obj.put("creationdateinuserformat", userdf.format(invoiceCreationDate));
                if (row.getInvoice().getTax() != null) {
//                    KwlReturnObject perresult = accTaxObj.getTaxPercent(row.getCompany().getCompanyID(), row.getInvoice().getJournalEntry().getEntryDate(), row.getInvoice().getTax().getID());
                    KwlReturnObject perresult = accTaxObj.getTaxPercent(row.getCompany().getCompanyID(), row.getInvoice().getCreationDate(), row.getInvoice().getTax().getID());
                    taxPercent = (Double) perresult.getEntityList().get(0);
                }
                obj.put("taxpercent", taxPercent);
                obj.put("discount", row.getInvoice().getDiscount() == null ? row.getInvoice().getDiscountAmount() : row.getInvoice().getDiscount().getDiscountValue());
                obj.put("payment", row.getInvoice().getID());
                obj.put("gstCurrencyRate", row.getGstCurrencyRate());
                if (isReceiptEdit) {
                    obj.put("amountpaid", rowAmount);
                    if (row.getFromCurrency() != null && row.getToCurrency() != null) {
                        obj.put("exchangeratefortransaction", row.getExchangeRateForTransaction());
                        if(row.getAmountInInvoiceCurrency()!=0){
                            obj.put("amountpaidincurrency", authHandler.round(row.getAmountInInvoiceCurrency(), companyid));
                        }else{
                            obj.put("amountpaidincurrency", authHandler.round(rowAmount / row.getExchangeRateForTransaction(), companyid));
                        }
                    } else {
                        double amount = rowAmount;
                        KwlReturnObject bAmt = null;
                        String fromcurrencyid = (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID());
                        String tocurrencyid = (row.getInvoice().getCurrency() == null ? (row.getInvoice().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID()) : row.getInvoice().getCurrency().getCurrencyID());
                        if (isopeningBalanceInvoice && row.getInvoice().isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(requestParams, amount, fromcurrencyid, tocurrencyid, invoiceCreationDate, externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyobj.getOneCurrencyToOtherRoundOff(requestParams, amount, fromcurrencyid, tocurrencyid, invoiceCreationDate, externalCurrencyRate);
                        }
                        amount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                        obj.put("amountpaidincurrency", amount);
                        obj.put("exchangeratefortransaction", amount / row.getAmount());
                    }
                } else {
                    if (row.getFromCurrency() != null && row.getToCurrency() != null) {
                        if(row.getAmountInInvoiceCurrency()!=0){
                            obj.put("amountpaid", authHandler.round(row.getAmountInInvoiceCurrency(), companyid));
                              obj.put("exchangeratefortransaction", row.getExchangeRateForTransaction());//document designer
                        }else{
                            obj.put("amountpaid", authHandler.round(rowAmount / row.getExchangeRateForTransaction(), companyid));
                        }
                    } else {
                        double amount = rowAmount;
                        String fromcurrencyid = (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID());
                        String tocurrencyid = (row.getInvoice().getCurrency() == null ? (row.getInvoice().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID()) : row.getInvoice().getCurrency().getCurrencyID());
                        KwlReturnObject bAmt = null;
                        if (isopeningBalanceInvoice && row.getInvoice().isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyobj.getOneCurrencyToOtherAccordingToCurrencyToBaseExchangeRate(requestParams, amount, fromcurrencyid, tocurrencyid, invoiceCreationDate, externalCurrencyRate);
                        } else {
                            bAmt = accCurrencyobj.getOneCurrencyToOtherRoundOff(requestParams, amount, fromcurrencyid, tocurrencyid, invoiceCreationDate, externalCurrencyRate);
                        }
                        amount = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                        obj.put("amountpaid", amount);
                    }
                }
                double amountdue = 0.0;
                double amountDueOriginal = 0.0;
                double totalAmount = 0.0;
                if (row.getInvoice().isNormalInvoice()) {
                    if (Constants.InvoiceAmountDueFlag) {
                        List ll = accInvoiceCommon.getInvoiceDiscountAmountInfo(requestParams, row.getInvoice());
                        amountdue = (Double) ll.get(0);
                        amountDueOriginal = (Double) ll.get(3);
                        totalAmount = row.getInvoice().getCustomerEntry().getAmount();
                    } else {
                        amountdue = accInvoiceCommon.getAmountDue(requestParams, row.getInvoice());
                        requestParams.put("amountDueOriginalFlag", true);
                        amountDueOriginal = accInvoiceCommon.getAmountDue(requestParams, row.getInvoice());
                        requestParams.remove("amountDueOriginalFlag");
                        totalAmount = row.getInvoice().getCustomerEntry().getAmount();
                    }
                } else {
                    amountDueOriginal = row.getInvoice().getOpeningBalanceAmountDue();
                    amountdue=amountDueOriginal;
                    totalAmount = row.getInvoice().getOriginalOpeningBalanceAmount();
                }

                amountdue = authHandler.round(amountdue, companyid);
                amountDueOriginal = authHandler.round(amountDueOriginal, companyid);
                totalAmount = authHandler.round(totalAmount, companyid);

                if (row.getFromCurrency() != null && row.getToCurrency() != null) {
                    obj.put("amountduenonnegative", (isReceiptEdit ? (amountDueOriginal * row.getExchangeRateForTransaction()) + rowAmount : amountDueOriginal));
                } else {
                    obj.put("amountduenonnegative", (isReceiptEdit ? amountdue + obj.optDouble("amountpaid", 0) : amountdue));
                }
                obj.put("amountDueOriginal", (isReceiptEdit ? amountDueOriginal + obj.optDouble("amountpaidincurrency", 0) : amountDueOriginal));
                obj.put("amountDueOriginalSaved", (isReceiptEdit ? amountDueOriginal + obj.optDouble("amountpaidincurrency", 0) : amountDueOriginal));
                obj.put("amountdue", amountdue);

                obj.put("totalamount", totalAmount);
                obj.put("isCurrencySame", isCurrencySame);
                if (!isCurrencySame) {
                    if (row.getInvoice().isNormalInvoice()) {
                        obj.put("documentAmount", authHandler.round(row.getInvoice().getInvoiceamountdue() * row.getExchangeRateForTransaction(), companyid) + row.getAmount());
                        obj.put("paidAmount", row.getAmount());
                    } else {
                        obj.put("documentAmount", authHandler.round(row.getInvoice().getOpeningBalanceAmountDue() * row.getExchangeRateForTransaction(), companyid) + row.getAmount());
                        obj.put("paidAmount", row.getAmount());
                    }
                }
                //        obj.put("receiptamount", (row.getAmount()));

                // ## Get Custom Field Data 
                Map<String, Object> variableMap = new HashMap<String, Object>();
                HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                Detailfilter_params.add(row.getID());
                invDetailRequestParams.put("filter_names", Detailfilter_names);
                invDetailRequestParams.put("filter_params", Detailfilter_params);
                KwlReturnObject idcustresult = accReceiptDAOobj.getReciptPaymentCustomData(invDetailRequestParams);
                JSONArray dimensionArr=new JSONArray();
                if (idcustresult.getEntityList().size() > 0) {
                    AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                    AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);

                    if (jeDetailCustom != null) {
                        JSONObject params = new JSONObject();
                        params.put(Constants.isExport, isExport);
                        params.put(Constants.isForReport, isForReport);
                        fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params,customRichTextMap);
                    }
                }
                obj.put("dimensionArr", dimensionArr);
                obj.put("srNoForRow", row.getSrNoForRow());
                obj.put(Constants.GST_CURRENCY_RATE, row.getGstCurrencyRate());
                innerJArr.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getReceiptRowsJSON : " + ex.getMessage(), ex);
        }
    }

    private void getReceiptCNDNArray(Receipt re, JSONArray innerJArrCNDN, KWLCurrency currency,JSONObject paramJObj, HashMap<String, String> customFieldMap, HashMap<String, String> customDateFieldMap, HashMap<String, Integer> FieldMap, HashMap<String, String> replaceFieldMap,HashMap requestParams, HashMap<String, String> customRichTextMap) throws SessionExpiredException, ServiceException {
        try {
            KwlReturnObject cndnResult = accReceiptDAOobj.getCustomerDnPayment(re.getID());
            List<DebitNotePaymentDetails> cndnList = cndnResult.getEntityList();
            DateFormat df = authHandler.getUserDateFormatterWithoutTimeZone(paramJObj);
            DateFormat df1 = authHandler.getDateOnlyFormat();
            String companyid = paramJObj.optString("companyid");
            for (DebitNotePaymentDetails dnpd:cndnList) {
                Double cnpaidamount = dnpd.getAmountPaid();
                Double cnPaidAmountPaymentCurrency = dnpd.getPaidAmountInReceiptCurrency();
                Double exchangeratefortransaction = dnpd.getExchangeRateForTransaction();
                String description = dnpd.getDescription()!=null?dnpd.getDescription():"";
                DebitNote debitNote = dnpd.getDebitnote();
                JSONObject obj = new JSONObject();
                obj.put("transectionno", debitNote.getDebitNoteNumber());
                obj.put("srnoforrow", dnpd.getSrno());
                obj.put("transectionid", debitNote.getID());
                obj.put("isopening", debitNote.isIsOpeningBalenceDN());
//                if(debitNote.isNormalDN()){
//                    obj.put("creationdate",  df.format(debitNote.getJournalEntry().getEntryDate()));
//                    obj.put("dateforsort",  debitNote.getJournalEntry().getEntryDate());
//                }else{
//                    obj.put("creationdate",  df.format(debitNote.getCreatedon()));
//                    obj.put("dateforsort",  debitNote.getCreationDate());
//                }
                obj.put("creationdate",  df.format(debitNote.getCreatedon()));
                obj.put("dateforsort",  debitNote.getCreationDate());
//                obj.put("linkingdate", df1.format(dnpd.getReceipt().getJournalEntry().getEntryDate()));
                obj.put("linkingdate", df1.format(dnpd.getReceipt().getCreationDate()));
                if (debitNote.getVendor() != null) {
                    obj.put("accountid", debitNote.getVendor().getID());
                    obj.put("accountname", debitNote.getVendor().getName());
                } else if (debitNote.getCustomer() != null) {
                    obj.put("accountid", debitNote.getCustomer().getID());
                    obj.put("accountname", debitNote.getCustomer().getName());
                }
                if (debitNote != null) {
                    obj.put("currencyidtransaction", debitNote.getCurrency() == null ? (re.getCurrency() == null ? currency.getCurrencyID() : re.getCurrency().getCurrencyID()) : debitNote.getCurrency().getCurrencyID());
                    obj.put("currencysymbol", debitNote.getCurrency() == null ? (re.getCurrency() == null ? currency.getSymbol() : re.getCurrency().getSymbol()) : debitNote.getCurrency().getSymbol());
                    obj.put("currencysymboltransaction", debitNote.getCurrency() == null ? (re.getCurrency() == null ? currency.getSymbol() : re.getCurrency().getSymbol()) : debitNote.getCurrency().getSymbol());

                } else {
                    obj.put("currencyidtransaction", (re.getCurrency() == null ? currency.getCurrencyID() : re.getCurrency().getCurrencyID()));
                    obj.put("currencysymbol", (re.getCurrency() == null ? currency.getSymbol() : re.getCurrency().getSymbol()));
                    obj.put("currencysymboltransaction", (re.getCurrency() == null ? currency.getSymbol() : re.getCurrency().getSymbol()));
                }
                obj.put("isopening", debitNote.isIsOpeningBalenceDN());
                obj.put("totalamount", debitNote.getDnamount());
                obj.put("enteramount", cnPaidAmountPaymentCurrency);
                obj.put("amountdue", debitNote.getDnamountdue());
                obj.put("exchangeratefortransaction", exchangeratefortransaction);//for documentdesigner
                obj.put("newamountdue", authHandler.round((debitNote.getDnamountdue() * exchangeratefortransaction), companyid) + cnPaidAmountPaymentCurrency);//for documentdesigner
                obj.put("description", description);//for documentdesigner
                obj.put("cnpaidamount", cnpaidamount);
                obj.put(Constants.GST_CURRENCY_RATE, dnpd.getGstCurrencyRate());
                obj.put("type", Constants.PaymentAgainstCNDN);
                // ## Get Custom Field Data 
                Map<String, Object> variableMap = new HashMap<String, Object>();
                HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                Detailfilter_params.add(dnpd.getID() != null ? dnpd.getID() : "");
                invDetailRequestParams.put("filter_names", Detailfilter_names);
                invDetailRequestParams.put("filter_params", Detailfilter_params);
                boolean isExport = false;
                boolean isForReport = false;
                if (requestParams.containsKey(Constants.isExport) && requestParams.get(Constants.isExport) != null) {
                    isExport = Boolean.parseBoolean(requestParams.get(Constants.isExport).toString());
                }
                if (requestParams.containsKey(Constants.isForReport) && requestParams.get(Constants.isForReport) != null) {
                    isForReport = Boolean.parseBoolean(requestParams.get(Constants.isForReport).toString());
                }
                KwlReturnObject idcustresult =  accVendorPaymentDAO.getVendorPaymentCustomData(invDetailRequestParams);
                JSONArray dimensionArr=new JSONArray();
                if (idcustresult.getEntityList().size() > 0) {
                    AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustresult.getEntityList().get(0);
                    AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                    if (jeDetailCustom != null) {
                        JSONObject params = new JSONObject();
                        params.put(Constants.isExport, isExport);
                        params.put(Constants.isForReport, isForReport);
                        fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params, customRichTextMap);
                    }
                }
                obj.put("dimensionArr", dimensionArr);
                obj.put("srNoForRow", dnpd.getSrno());
                innerJArrCNDN.put(obj);
            }
        } catch (JSONException ex) {
            throw ServiceException.FAILURE("getReceiptRowsJSON : " + ex.getMessage(), ex);
        }
    }

    private void getReceiptLoneDetails(JSONObject paramJObj, Receipt re, JSONArray innerJArrLinkedAdvance, KWLCurrency currency) throws SessionExpiredException, ServiceException {
        try {
            DateFormat df = authHandler.getDateFormatter(paramJObj);
            DateFormat userdf = authHandler.getUserDateFormatterJson(paramJObj);
            Iterator itrLinkedPayment = re.getReceiptDetailsLoan().iterator();
            Calendar cal = Calendar.getInstance();
            String companyid = paramJObj.optString("companyid");
            String month="";
            int year=0;
            Date endDate=null;
            Map<String,Object> mapForRepaymentDetails = new HashMap<>();
            mapForRepaymentDetails.put("paymentStatus","1");
            mapForRepaymentDetails.put("companyid", companyid);
            mapForRepaymentDetails.put("df", df);
            double totalLoanAmountOfDisbursement=0.0;
            double amountDueOfDisbursement=0.0;
            while (itrLinkedPayment.hasNext()) {
                ReceiptDetailLoan row = (ReceiptDetailLoan) itrLinkedPayment.next();
                RepaymentDetails RD = row.getRepaymentDetail();
                endDate = RD.getEndDate();
                Disbursement disbursement = RD.getDisbursement();
                totalLoanAmountOfDisbursement = disbursement.getLoanAmount();
                amountDueOfDisbursement = disbursement.getLoanAmount();
                Date CreationDate = disbursement.getDisbursementdate();
                double rowAmount = (authHandler.round(row.getAmount(), companyid));
                cal.setTime(endDate);
                month = cal.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.ENGLISH);
                year = cal.get(Calendar.YEAR);
                mapForRepaymentDetails.put("disbursementId", disbursement.getID());
                KwlReturnObject repaymentdetailresult = accLoanDAOobj.getRepaymentSheduleDetails(mapForRepaymentDetails);
                List<RepaymentDetails> RDS = repaymentdetailresult.getEntityList();
                for(RepaymentDetails RPD : RDS){
                    amountDueOfDisbursement -= RPD.getPrinciple();
                }
                amountDueOfDisbursement = authHandler.round(amountDueOfDisbursement, companyid);
                JSONObject obj = new JSONObject();
                obj.put("transectionno", disbursement.getLoanrefnumber());
                obj.put("transectionid", disbursement.getID());
                obj.put("creationdateinuserformat", userdf.format(CreationDate));
                obj.put("instalment", month+"-"+year);
                obj.put("currencysymbol", disbursement.getCurrency().getSymbol());
                obj.put("totalamount", totalLoanAmountOfDisbursement);
                obj.put("enteramount", rowAmount);
                obj.put("amountdue", amountDueOfDisbursement);
//                obj.put("linkingdate", df.format(row.getReceipt().getJournalEntry().getEntryDate()));
                obj.put("linkingdate", df.format(row.getReceipt().getCreationDate()));
                obj.put(Constants.GST_CURRENCY_RATE, row.getGstCurrencyRate());
                innerJArrLinkedAdvance.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccReceiptServiceImpl.getReceiptLinkToDebitNoteArray : " + ex.getMessage(), ex);
        }
    }
    
    private void getReceiptLinkToDebitNoteArray(JSONObject paramJobj, Receipt re, JSONArray innerJArrLinkedAdvance, KWLCurrency currency, double taxPercent, HashMap<String, String> customFieldMap, HashMap<String, Integer> FieldMap, HashMap<String, String> replaceFieldMap, Iterator itr, HashMap requestParams, HashMap<String, String> customDateFieldMap) throws SessionExpiredException, ServiceException {
        try {
            DateFormat df = authHandler.getDateFormatter(paramJobj);
            DateFormat userdf = authHandler.getUserDateFormatterJson(paramJobj);
            String companyid = paramJobj.optString("companyid");
            Iterator itrLinkedPayment = re.getLinkDetailReceiptsToDebitNote().iterator();
            while (itrLinkedPayment.hasNext()) {
                LinkDetailReceiptToDebitNote row = (LinkDetailReceiptToDebitNote) itrLinkedPayment.next();
                DebitNote debitNote = row.getDebitnote();
                Date dnCreationDate = null;
                double exchangeratefortransaction = row.getExchangeRateForTransaction();
//                if (row.getDebitnote().isNormalDN()) {
//                    dnCreationDate = row.getDebitnote().getJournalEntry().getEntryDate();
//                } else {// opening balance invoice creation date
//                    dnCreationDate = row.getDebitnote().getCreationDate();
//                }
                dnCreationDate = row.getDebitnote().getCreationDate();

                JSONObject obj = new JSONObject();
                obj.put("transectionno", debitNote.getDebitNoteNumber());
                obj.put("transectionid", debitNote.getID());
                obj.put("isopening", debitNote.isIsOpeningBalenceDN());
                obj.put("creationdate", df.format(dnCreationDate));
                obj.put("creationdateinuserformat", userdf.format(dnCreationDate));
                obj.put("dateforsort", dnCreationDate);
                obj.put("linkingdate", row.getReceiptLinkDate()!=null ? df.format(row.getReceiptLinkDate()) : "");
                if (debitNote.getCustomer() != null) {
                    obj.put("accountid", debitNote.getCustomer().getID());
                    obj.put("accountname", debitNote.getCustomer().getName());
                }
                if (debitNote != null) {
                    obj.put("currencyidtransaction", row.getDebitnote().getCurrency() == null ? (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID()) : row.getDebitnote().getCurrency().getCurrencyID());
                    obj.put("currencysymbol", row.getDebitnote().getCurrency() == null ? (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol()) : row.getDebitnote().getCurrency().getSymbol());
                    obj.put("currencysymboltransaction", row.getDebitnote().getCurrency() == null ? (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol()) : row.getDebitnote().getCurrency().getSymbol());
                } else {
                    obj.put("currencyidtransaction", (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID()));
                    obj.put("currencysymbol", (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol()));
                    obj.put("currencysymboltransaction", (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol()));
                }
                obj.put("totalamount", debitNote.getDnamount());
                obj.put("enteramount", row.getAmount());
                obj.put("amountdue", debitNote.getDnamountdue());
                obj.put("exchangeratefortransaction", exchangeratefortransaction);//for documentdesigner
                obj.put("newamountdue", authHandler.round((debitNote.getDnamountdue() * exchangeratefortransaction), companyid) + row.getAmount());//for documentdesigner
                obj.put("cnpaidamount", row.getAmountInDNCurrency());
                obj.put("srNoForRow", row.getSrno());
                innerJArrLinkedAdvance.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccReceiptServiceImpl.getReceiptLinkToDebitNoteArray : " + ex.getMessage(), ex);
        }
    }

    @Override
    public HashMap<String, Object> getReceiptRequestMapJSON(JSONObject paramJobj) throws SessionExpiredException {
        HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(paramJobj);
        boolean isExport = false;
        if (paramJobj.optString("isExport", null) != null) {
            isExport = Boolean.parseBoolean(paramJobj.optString("isExport"));
            requestParams.put("isExport", isExport);
        }
        requestParams.put("ss", paramJobj.optString("ss", null));
        requestParams.put("start", paramJobj.optString("start", null));
        requestParams.put("limit", paramJobj.optString("limit", null));
        requestParams.put("deleted", paramJobj.optString("deleted", null));
        requestParams.put("nondeleted", paramJobj.optString("nondeleted", null));
        requestParams.put(Constants.REQ_startdate, paramJobj.optString("stdate", null));
        requestParams.put(Constants.REQ_enddate, paramJobj.optString("enddate", null));
        requestParams.put(Constants.Acc_Search_Json, paramJobj.optString(Constants.Acc_Search_Json, null));
        requestParams.put(Constants.Filter_Criteria, paramJobj.optString(InvoiceConstants.Filter_Criteria, null));
        requestParams.put(Constants.moduleid, paramJobj.optString(Constants.moduleid, null));
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("linknumber"))) {
            requestParams.put("linknumber", paramJobj.optString("linknumber"));
        }
        if (paramJobj.optBoolean("applyFilterOnCurrency", false)) {
            requestParams.put("applyFilterOnCurrency", paramJobj.optString("applyFilterOnCurrency"));
        }
        if (paramJobj.optString("dir") != null && !StringUtil.isNullOrEmpty(paramJobj.optString("dir"))
                && paramJobj.optString("sort") != null && !StringUtil.isNullOrEmpty(paramJobj.optString("sort"))) {
            requestParams.put("dir", paramJobj.optString("dir"));
            requestParams.put("sort", paramJobj.optString("sort"));
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.browsertz, null))) {
            requestParams.put(Constants.browsertz, paramJobj.optString(Constants.browsertz, null));
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString(Constants.requestModuleId))) {
            requestParams.put(Constants.requestModuleId, Integer.parseInt(paramJobj.optString(Constants.requestModuleId)));
        }
        requestParams.put(Constants.isForReport, paramJobj.optString(Constants.isForReport, null));
        requestParams.put(Constants.df, authHandler.getDateOnlyFormat());
        return requestParams;
    }
    @Override
    public JSONObject getInvoiceJSON(Invoice invoice, Map invoiceMap, Map<String, Object> requestParams) {
        JSONObject invobj = new JSONObject();
        try {
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            String gcurrencyid = (String) requestParams.get(Constants.globalCurrencyKey);
            Date linkingdate = (Date) requestParams.get("linkingdate");
            String companyid = (String) requestParams.get("companyid");
            Customer customer = invoice.getCustomer();
            JournalEntry je = invoice.getJournalEntry();
            List<String> idsList = new ArrayList<String>();
            idsList.add(invoice.getID());
            String invid = invoice.getID();
            Map<String, JournalEntryDetail> invoiceCustomerEntryMap = accInvoiceDAOObj.getInvoiceCustomerEntryList(idsList);
            JournalEntryDetail d = invoiceCustomerEntryMap.get(invid);
            Account account = null;
            if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                account = invoice.getCustomer().getAccount();
            } else {
                account = d.getAccount();
            }
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), gcurrencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            String currencyid = (invoice.getCurrency() == null ? currency.getCurrencyID() : invoice.getCurrency().getCurrencyID());
            String currencyFilterForTrans = "";
            if (invoiceMap.containsKey("currencyfilterfortrans") && invoiceMap.containsKey("isReceipt")) {
                currencyFilterForTrans = invoiceMap.get("currencyfilterfortrans") != null ? (String) invoiceMap.get("currencyfilterfortrans") : "";
                KwlReturnObject currencyFilterResult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), currencyFilterForTrans);
                KWLCurrency currencyFilter = (KWLCurrency) currencyFilterResult.getEntityList().get(0);
                invobj.put("currencyidpayment", currencyFilterForTrans);
                invobj.put("currencysymbolpayment", (currencyFilter == null ? currency.getSymbol() : currencyFilter.getSymbol()));
            }
            Date invoiceCreationDate = invoice.getCreationDate();
            Double externalCurrencyRate = 0d;
            Double invoiceOriginalAmount = 0d;
            if (invoice.isIsOpeningBalenceInvoice() && !invoice.isNormalInvoice()) {
                externalCurrencyRate = invoice.getExchangeRateForOpeningTransaction();
                invoiceOriginalAmount = invoice.getOriginalOpeningBalanceAmount();
            }
            if (je != null) {
//                invoiceCreationDate = je.getEntryDate();
                invoiceCreationDate = invoice.getCreationDate();
                externalCurrencyRate = je.getExternalCurrencyRate();
                invoiceOriginalAmount = d.getAmount();
            }
            double currencyToBaseRate = accCurrencyobj.getCurrencyToBaseRate(invoiceMap, currencyid, invoiceCreationDate);
            if (je != null) {
                invobj.put("journalEntryNo", je.getEntryNumber());  //journal entry no
            }
            invobj.put("billid", invoice.getID());
            invobj.put("companyid", invoice.getCompany().getCompanyID());
            invobj.put("companyname", invoice.getCompany().getCompanyName());
            invobj.put("withoutinventory", "");
            invobj.put("transactionNo", invoice.getInvoiceNumber());
            invobj.put("duedate", invoice.getDueDate() != null ? df.format(invoice.getDueDate()) : "");
            invobj.put("linkingdate", df.format(linkingdate));
            invobj.put("mergedCategoryData", "Customer Invoice");  //type of data
            invobj.put("personname", customer.getName());
            invobj.put("personid", customer.getID());
            invobj.put("isOpeningBalanceInvoice", invoice.isIsOpeningBalenceInvoice());
            invobj.put("personid", customer == null ? account.getID() : customer.getID());
            invobj.put("personname", customer.getName());
            invobj.put("personemail", customer == null ? "" : customer.getEmail());
            invobj.put("aliasname", customer == null ? "" : customer.getAliasname());
            invobj.put("accid", account.getID());
            invobj.put("accountid", invoice.getAccount() == null ? "" : invoice.getAccount().getID());
            invobj.put("billno", invoice.getInvoiceNumber());
            invobj.put("transectionno", invoice.getInvoiceNumber());
            invobj.put("isOpeningBalanceTransaction", invoice.isIsOpeningBalenceInvoice());
            invobj.put("isNormalTransaction", invoice.isNormalInvoice());
            invobj.put("currencyid", currencyid);
            invobj.put("currencysymbol", (invoice.getCurrency() == null ? currency.getSymbol() : invoice.getCurrency().getSymbol()));
            invobj.put("currencyidtransaction", currencyid);
            invobj.put("currencysymboltransaction", (invoice.getCurrency() == null ? currency.getSymbol() : invoice.getCurrency().getSymbol()));
            invobj.put("companyaddress", invoice.getCompany().getAddress());
            invobj.put("companyname", invoice.getCompany().getCompanyName());
            invobj.put("oldcurrencyrate", currencyToBaseRate * 1.0);
            invobj.put("billto", invoice.getBillTo());
            invobj.put("shipto", invoice.getShipTo());
            invobj.put("journalentryid", (je != null ? je.getID() : ""));
            invobj.put("porefno", invoice.getPoRefNumber());
            invobj.put("externalcurrencyrate", externalCurrencyRate);
            invobj.put("entryno", (je != null ? je.getEntryNumber() : ""));
            invobj.put("date", df.format(invoiceCreationDate));
            invobj.put("creationdate", df.format(invoiceCreationDate));
            invobj.put("invoicedate", df.format(invoiceCreationDate));
            invobj.put("shipdate", invoice.getShipDate() == null ? "" : df.format(invoice.getShipDate()));
            invobj.put("duedate", df.format(invoice.getDueDate()));
            invobj.put("personname", customer == null ? account.getName() : customer.getName());
            invobj.put("salesPerson", invoice.getMasterSalesPerson() == null ? "" : invoice.getMasterSalesPerson().getID());
            invobj.put("salespersonname", invoice.getMasterSalesPerson() == null ? "" : invoice.getMasterSalesPerson().getValue());
            invobj.put("memo", invoice.getMemo());
            invobj.put("termname", customer == null ? "" : customer.getCreditTerm().getTermname());
            invobj.put("termid", customer == null ? "" : customer.getCreditTerm().getID());
            invobj.put("deleted", invoice.isDeleted());
            invobj.put("taxincluded", invoice.getTax() == null ? false : true);
            invobj.put("taxid", invoice.getTax() == null ? "" : invoice.getTax().getID());
            invobj.put("taxamount", invoice.getTaxEntry() == null ? 0 : invoice.getTaxEntry().getAmount());
            invobj.put("discount", invoice.getDiscount() == null ? 0 : invoice.getDiscount().getDiscountValue());
            invobj.put("ispercentdiscount", invoice.getDiscount() == null ? false : invoice.getDiscount().isInPercent());
            invobj.put("discountval", invoice.getDiscount() == null ? 0 : invoice.getDiscount().getDiscount());
            invobj.put("costcenterid", (je != null ? je.getCostcenter() == null ? "" : je.getCostcenter().getID() : ""));
            invobj.put("costcenterName", (je != null ? je.getCostcenter() == null ? "" : je.getCostcenter().getName() : ""));
            invobj.put("shipvia", invoice.getShipvia() == null ? "" : invoice.getShipvia());
            invobj.put("fob", invoice.getFob() == null ? "" : invoice.getFob());
            invobj.put("isClaimedInvoice", (invoice.getBadDebtType() == 1 || invoice.getBadDebtType() == 2));// for Malasian Company
//            invobj.put("invoicedate", invoice.isIsOpeningBalenceInvoice() ? (invoice.getCreationDate() == null ? "" : df.format(invoice.getCreationDate())) : df.format(je.getEntryDate()));
            invobj.put("invoicedate", invoice.getCreationDate() == null ? "" : df.format(invoice.getCreationDate()));
            if (invoice.getModifiedby() != null) {
                invobj.put("lasteditedby", StringUtil.getFullName(invoice.getModifiedby()));
            }
            double amountinbase = invoiceOriginalAmount;
            if (invoice.isIsOpeningBalenceInvoice() && invoice.isConversionRateFromCurrencyToBase()) {// if invoice is opening balance invoice and Conversion rate is taken from user is Currency to base then following method will be called.
                KwlReturnObject bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, invoiceOriginalAmount, currencyid, invoiceCreationDate, externalCurrencyRate);
                amountinbase = (Double) bAmt.getEntityList().get(0);
            } else if (invoiceOriginalAmount != 0) {
                if (externalCurrencyRate != 0) {
                    amountinbase = invoiceOriginalAmount / externalCurrencyRate;
                } else if (currencyToBaseRate != 0) {
                    amountinbase = invoiceOriginalAmount / currencyToBaseRate;
                }
            }
            invobj.put("amount", authHandler.round(invoiceOriginalAmount, companyid));
            invobj.put("amountinbase", authHandler.round(amountinbase, companyid));
            BillingShippingAddresses addresses = invoice.getBillingShippingAddresses();
            AccountingAddressManager.getTransactionAddressJSON(invobj, addresses, false);
        } catch (Exception ex) {
            Logger.getLogger(AccReceiptServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage());
        } finally {
            return invobj;
        }
    }
    
    public JSONObject getPaymentJSON(Payment payment, Map<String, Object> requestParams) throws JSONException, ServiceException,SessionExpiredException {
        String type = "Payment For Refund";  //type of data;
        int paymentwindowtype = payment.getPaymentWindowType();
        JSONObject paymentObj = new JSONObject();
        DateFormat df = authHandler.getDateOnlyFormat();
        String JeNumber = "";
        Receipt receipt = null;
        if (requestParams.containsKey("JeNumber") && requestParams.get("JeNumber") != null) {
            JeNumber = (String) requestParams.get("JeNumber");
        }
        if (requestParams.containsKey("receipt") && requestParams.get("receipt") != null) {
            receipt = (Receipt) requestParams.get("receipt");
        }
        
        JournalEntry je = payment.getJournalEntry();
        paymentObj.put("billid", payment.getID());
        paymentObj.put("transactionNo", payment.getPaymentNumber());
//                            paymentObj.put("date", (payment.isIsOpeningBalencePayment()) ? (df.format(payment.getCreationDate())) : df.format(payment.getJournalEntry().getEntryDate()));
        paymentObj.put("date", df.format(payment.getCreationDate()));
//                            paymentObj.put("linkingdate", (payment.isIsOpeningBalencePayment()) ? (df.format(payment.getCreationDate())) : df.format(payment.getJournalEntry().getEntryDate()));
        paymentObj.put("linkingdate", df.format(payment.getCreationDate()));
        paymentObj.put("journalEntryNo", JeNumber);  //journal entry no
        paymentObj.put("mergedCategoryData", type);  //type of data
        if (payment.getVendor() != null) {
            Vendor vendor = payment.getVendor();
            paymentObj.put("personname", vendor.getName());
            paymentObj.put("personid", vendor.getID());

        } else {
            String custid = payment.getCustomer();
            KwlReturnObject objItr2 = accountingHandlerDAOobj.getObject(Customer.class.getName(), custid);
            Customer customer = (Customer) objItr2.getEntityList().get(0);
            paymentObj.put("personname", customer.getName());
            paymentObj.put("personid", customer.getID());
        }
        String jeIds = je != null ? je.getID() : "";
        String jeIdEntryDate = (je != null && receipt != null && receipt.getJournalEntry() != null) ? df.format(receipt.getJournalEntry().getEntryDate()) : "";
        paymentObj.put("journalentryid", jeIds);
        paymentObj.put("journalentrydate", jeIdEntryDate);
        paymentObj.put("isadvancepayment", payment.isIsadvancepayment());
        paymentObj.put("ismanydbcr", payment.isIsmanydbcr());
        paymentObj.put("isprinted", payment.isPrinted());
        paymentObj.put("bankCharges", payment.getBankChargesAmount());
        paymentObj.put("bankChargesCmb", payment.getBankChargesAccount() != null ? payment.getBankChargesAccount().getID() : "");
        paymentObj.put("bankChargesAccCode", payment.getBankChargesAccount() != null ? payment.getBankChargesAccount().getAcccode() != null ? payment.getBankChargesAccount().getAcccode() : "" : "");
        paymentObj.put("bankChargesAccName", payment.getBankChargesAccount() != null ? payment.getBankChargesAccount().getName() != null ? payment.getBankChargesAccount().getName() : "" : "");
        paymentObj.put("bankInterestAccCode", payment.getBankInterestAccount() != null ? payment.getBankInterestAccount().getAcccode() != null ? payment.getBankInterestAccount().getAcccode() : "" : "");
        paymentObj.put("bankInterestAccName", payment.getBankInterestAccount() != null ? payment.getBankInterestAccount().getName() != null ? payment.getBankInterestAccount().getName() : "" : "");
        paymentObj.put("bankInterest", payment.getBankInterestAmount());
        paymentObj.put("bankInterestCmb", payment.getBankInterestAccount() != null ? payment.getBankInterestAccount().getID() : "");
        paymentObj.put("paidToCmb", payment.getPaidTo() == null ? "" : payment.getPaidTo().getID());
        paymentObj.put("paidto", payment.getPaidTo() != null ? payment.getPaidTo().getValue() : "");  //to show the paid to option in grid
        paymentObj.put(Constants.SEQUENCEFORMATID, payment.getSeqformat() == null ? "" : payment.getSeqformat().getID());
        paymentObj.put("invoiceadvcndntype", payment.getInvoiceAdvCndnType());
        paymentObj.put("cndnAndInvoiceId", !StringUtil.isNullOrEmpty(payment.getCndnAndInvoiceId()) ? payment.getCndnAndInvoiceId() : "");
        paymentObj.put("advanceid", (payment.getAdvanceid() != null && !payment.getAdvanceid().isDeleted()) ? payment.getAdvanceid().getID() : "");
        paymentObj.put("advanceamount", payment.getAdvanceamount());
        paymentObj.put("receipttype", payment.getReceipttype());
        paymentObj.put("paymentwindowtype", payment.getPaymentWindowType());
        paymentObj.put("billno", payment.getPaymentNumber());
        paymentObj.put("isOpeningBalanceTransaction", payment.isIsOpeningBalencePayment());
        paymentObj.put("isNormalTransaction", payment.isNormalPayment());
        paymentObj.put("companyid", payment.getCompany().getCompanyID());
        paymentObj.put("companyname", payment.getCompany().getCompanyName());
        paymentObj.put("entryno", JeNumber);
        paymentObj.put("journalentryid", jeIds);
        paymentObj.put("journalentrydate", jeIdEntryDate);
        paymentObj.put("isadvancepayment", payment.isIsadvancepayment());
        paymentObj.put("ismanydbcr", payment.isIsmanydbcr());
        paymentObj.put("isprinted", payment.isPrinted());
        paymentObj.put("isDishonouredCheque", payment.isIsDishonouredCheque());
        paymentObj.put("paymentwindowtype", paymentwindowtype);
        paymentObj.put(Constants.SEQUENCEFORMATID, payment.getSeqformat() == null ? "" : payment.getSeqformat().getID());
        return paymentObj;
    }
    
    public JSONObject getDebitNoteJSON(DebitNote debitNote, Map<String, Object> requestParams) {
        JSONObject jSONObject = new JSONObject();
        try {
            DateFormat df = (DateFormat) requestParams.get(Constants.df);
            String gcurrencyid = (String) requestParams.get(Constants.globalCurrencyKey);
            String companyid = (String) requestParams.get(Constants.companyKey);
            Date linkingdate = (Date) requestParams.get("linkingdate");
            KwlReturnObject curresult = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), gcurrencyid);
            KWLCurrency currency = (KWLCurrency) curresult.getEntityList().get(0);
            JournalEntryDetail vendorEntry = debitNote.getVendorEntry();
            JournalEntry je = debitNote.getJournalEntry();
            Customer customer = debitNote.getCustomer();
            Vendor vendor = debitNote.getVendor();
            double amountdue = debitNote.isOtherwise() ? debitNote.getDnamountdue() : 0;
            double amountDueOriginal = debitNote.isOtherwise() ? debitNote.getDnamountdue() : 0;
            Date dnDate = debitNote.getCreationDate();
            double externalCurrencyRate = debitNote.getExchangeRateForOpeningTransaction();
            if (je != null) {
//                dnDate = je.getEntryDate();
                externalCurrencyRate = je.getExternalCurrencyRate();
            }
            String currencyid = debitNote.getCurrency() != null ? debitNote.getCurrency().getCurrencyID() : currency.getCurrencyID();
            jSONObject.put("transactionNo", debitNote.getDebitNoteNumber());
            jSONObject.put("billid", debitNote.getID());
            jSONObject.put("companyid", debitNote.getCompany().getCompanyID());
            jSONObject.put("withoutinventory", false);
//            jSONObject.put("date", (debitNote.isIsOpeningBalenceDN()) ? (df.format(debitNote.getCreationDate())) : df.format(debitNote.getJournalEntry().getEntryDate()));  //date of delivery order
            jSONObject.put("date", df.format(debitNote.getCreationDate()));  //date of delivery order
            jSONObject.put("linkingdate",df.format(linkingdate));
            jSONObject.put("journalEntryNo", (debitNote.isIsOpeningBalenceDN()) ? "" : debitNote.getJournalEntry().getEntryNumber());  //journal entry no
            jSONObject.put("mergedCategoryData", "Debit Note");  //type of data
            if (vendor != null) {
                jSONObject.put("personname", vendor.getName());
                jSONObject.put("personid", vendor.getID());
            } else if (customer != null) {
                jSONObject.put("personname", customer.getName());
                jSONObject.put("personid", customer.getID());
            }
            jSONObject.put("deleted", debitNote.isDeleted());
            jSONObject.put("otherwise", debitNote.isOtherwise());
            jSONObject.put("isprinted", debitNote.isPrinted());
            jSONObject.put("openflag", debitNote.isOpenflag());
            jSONObject.put("isCreatedFromReturnForm", (debitNote.getPurchaseReturn() != null) ? true : false);
            jSONObject.put("cntype", debitNote.getDntype());
            jSONObject.put("costcenterid", debitNote.getCostcenter() == null ? "" : debitNote.getCostcenter().getID());
            jSONObject.put("costcenterName", debitNote.getCostcenter() == null ? "" : debitNote.getCostcenter().getName());
            jSONObject.put("memo", debitNote.getMemo());
            jSONObject.put("journalentryid", debitNote.isIsOpeningBalenceDN() ? "" : je.getID());
            jSONObject.put("isOpeningBalanceTransaction", debitNote.isIsOpeningBalenceDN());
            jSONObject.put("currencysymbol", (debitNote.getCurrency() == null ? currency.getSymbol() : debitNote.getCurrency().getSymbol()));
            jSONObject.put("currencyid", (debitNote.getCurrency() == null ? currency.getCurrencyID() : debitNote.getCurrency().getCurrencyID()));
            jSONObject.put("entryno", debitNote.isIsOpeningBalenceDN() ? "" : je.getEntryNumber());
            jSONObject.put("noteno", debitNote.getDebitNoteNumber());
            jSONObject.put("noteid", debitNote.getID());
            jSONObject.put("exchangeratefortransaction", (amountDueOriginal <= 0 && amountdue <= 0) ? 0 : (amountdue / amountDueOriginal));
            jSONObject.put("accountid", debitNote.getAccount() == null ? "" : debitNote.getAccount().getID());
            jSONObject.put("accountnames", debitNote.getAccount() == null ? "" : debitNote.getAccount().getName());
            jSONObject.put("currencyidtransaction", gcurrencyid);
            jSONObject.put("externalcurrencyrate", debitNote.isIsOpeningBalenceDN() ? debitNote.getExchangeRateForOpeningTransaction() : je.getExternalCurrencyRate());
            jSONObject.put("currencysymboltransaction", (debitNote.getCurrency() == null ? currency.getSymbol() : debitNote.getCurrency().getSymbol()));
            if (debitNote.getDntype() == Constants.DebitNoteAgainstPurchaseInvoice) {
                jSONObject.put("isCopyAllowed", false);
            }
            jSONObject.put("currencycode", (debitNote.getCurrency() == null ? currency.getCurrencyCode() : debitNote.getCurrency().getCurrencyCode()));
            jSONObject.put(Constants.SEQUENCEFORMATID, debitNote.getSeqformat() != null ? debitNote.getSeqformat().getID() : "");
            String reason = "";
            if (!StringUtil.isNullOrEmpty(reason)) {
                jSONObject.put("reason", reason.substring(0, reason.length() - 1));
            } else {
                jSONObject.put("reason", reason);
            }

            double paidAmount = debitNote.isOtherwise() ? debitNote.getDnamount() : vendorEntry.getAmount();
            KwlReturnObject bAmt = null;
            jSONObject.put("amount", paidAmount);
            if (debitNote.isIsOpeningBalenceDN() && debitNote.isConversionRateFromCurrencyToBase()) {
                bAmt = accCurrencyobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, paidAmount, currencyid, dnDate, externalCurrencyRate);
            } else {
                bAmt = accCurrencyobj.getCurrencyToBaseAmount(requestParams, paidAmount, currencyid, dnDate, externalCurrencyRate);
            }
            double amountinbase = (Double) bAmt.getEntityList().get(0);
            jSONObject.put("amountinbase", amountinbase);
            //for custom fields
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = null;
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Debit_Note_ModuleId));
            FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            Map<String, Object> variableMap = new HashMap<String, Object>();
            HashMap<String, Object> cnDetailRequestParams = new HashMap<String, Object>();
            ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
            Detailfilter_names.add("companyid");
            Detailfilter_params.add(debitNote.getCompany().getCompanyID());
            Detailfilter_names.add("journalentryId");
            Detailfilter_params.add(debitNote.isIsOpeningBalenceDN() ? "" : debitNote.getJournalEntry().getID());
            Detailfilter_names.add("moduleId");
            Detailfilter_params.add(Constants.Acc_Debit_Note_ModuleId + "");
            cnDetailRequestParams.put("filter_names", Detailfilter_names);
            cnDetailRequestParams.put("filter_params", Detailfilter_params);
            KwlReturnObject idcustresult = accJournalEntryobj.getJournalEntryCustomData(cnDetailRequestParams);
            if (idcustresult.getEntityList().size() > 0) {
                AccJECustomData jeCustom = (AccJECustomData) idcustresult.getEntityList().get(0);
                AccountingManager.setCustomColumnValues(jeCustom, FieldMap, replaceFieldMap, variableMap);
                JSONObject params = new JSONObject();
                fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, jSONObject, params);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccReceiptServiceImpl.class.getName()).log(Level.SEVERE, ex.getMessage());
        } finally {
            return jSONObject;
        }
    }
    
    /**
     * Description : Method is used to Build Purchase Return record Json
     *
     * @param <jsonarray> Used to build array of Linked documents Purchase
     * Return in Debit Note
     *
     * @param <listcq> contains id of Purchase Return Linked in Selected Debit
     * Note
     * @param <currency> Currency used in documents
     * @param <userdf> Object Of user Date Format
     * @return :JSONArray
     */
    @Override
    public JSONArray getPurchaseReturnJson(JSONArray jsonArray, List listcq, KWLCurrency currency, DateFormat userdf, String companyid) {
        try {
            Iterator itrcq = listcq.iterator();
            while (itrcq.hasNext()) {
                PurchaseReturn purchaseReturn = (PurchaseReturn) itrcq.next();
                JSONObject obj = new JSONObject();

                Vendor vendor = purchaseReturn.getVendor();
                obj.put("billid", purchaseReturn.getID());
                obj.put("companyid", purchaseReturn.getCompany().getCompanyID());
                obj.put("transactionNo", purchaseReturn.getPurchaseReturnNumber()); 
                obj.put("deleted", purchaseReturn.isDeleted());  
                obj.put("date", userdf.format(purchaseReturn.getOrderDate())); 
                obj.put("mergedCategoryData", "Purchase Return"); 
                obj.put("personname", vendor.getName());
                obj.put("billid", purchaseReturn.getID());
                obj.put("taxid", purchaseReturn.getTax() != null ? purchaseReturn.getTax().getID() : "" );
                obj.put("companyid", purchaseReturn.getCompany().getCompanyID());
                obj.put("companyname", purchaseReturn.getCompany().getCompanyName());
                obj.put("personid", vendor.getID());
                obj.put("billno", purchaseReturn.getPurchaseReturnNumber());
                obj.put("date", userdf.format(purchaseReturn.getOrderDate()));
                obj.put("personname", vendor.getName());
                obj.put("memo", purchaseReturn.getMemo());
                obj.put("externalcurrencyrate", purchaseReturn.getExternalCurrencyRate());
                obj.put("costcenterid", purchaseReturn.getCostcenter() == null ? "" : purchaseReturn.getCostcenter().getID());
                obj.put("costcenterName", purchaseReturn.getCostcenter() == null ? "" : purchaseReturn.getCostcenter().getName());
                obj.put("shipdate", purchaseReturn.getShipdate() == null ? "" : userdf.format(purchaseReturn.getShipdate()));
                obj.put("shipvia", purchaseReturn.getShipvia() == null ? "" : purchaseReturn.getShipvia());
                obj.put("fob", purchaseReturn.getFob() == null ? "" : purchaseReturn.getFob());
                obj.put("currencyid", (purchaseReturn.getCurrency() == null ? "" : purchaseReturn.getCurrency().getCurrencyID()));
                obj.put("currencysymbol", (purchaseReturn.getCurrency() == null ? "" : purchaseReturn.getCurrency().getSymbol()));
                obj.put("currencycode", (purchaseReturn.getCurrency() == null ? "" : purchaseReturn.getCurrency().getCurrencyCode()));
                obj.put("sequenceformatid", purchaseReturn.getSeqformat() != null ? purchaseReturn.getSeqformat().getID() : "");

                if (purchaseReturn.getModifiedby() != null) {
                    obj.put("lasteditedby", StringUtil.getFullName(purchaseReturn.getModifiedby()));
                }
                
                 /* Code for dispalying tax amount of PR in view mode from DN linking report*/
                Set<PurchaseReturnDetail> prRows = purchaseReturn.getRows();
                boolean includeprotax = false;
                if (prRows != null && !prRows.isEmpty()) {
                    for (PurchaseReturnDetail temp : prRows) {

                        if (temp.getTax() != null) {
                            includeprotax = true;
                            break;
                        }
                    }
                }
                obj.put("includeprotax", includeprotax);
                jsonArray.put(obj);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            return jsonArray;
        }
    }
    
    /**
     * Description : Below Method is used to get Advance Payment linked to Refund Receipt
     *
     * @param <request> used to get df, userdf from session
     * @param <re> used to get LinkDetailReceiptToAdvancePayment of Receipt
     * @param <innerJArrLinkedAdvance> used to get all data of Advance Payments linked to Refund Receipt of Receipt
     * @param <currency> used to get all base currency id
     * @param <taxPercent> this is in double
     * @param <customFieldMap> this is HashMap
     * @param <FieldMap> this is HashMap
     * @param <replaceFieldMap> this is HashMap
     * @param <itr> this is Iterator
     * @param <requestParams> this is HashMap
     * @param <customDateFieldMap> this is HashMap
     * @return void
     */
    private void getReceiptLinkToAdvancePaymentArray(JSONObject paramJobj, Receipt re, JSONArray innerJArrLinkedAdvance, KWLCurrency currency, double taxPercent, HashMap<String, String> customFieldMap, HashMap<String, Integer> FieldMap, HashMap<String, String> replaceFieldMap, Iterator itr, HashMap requestParams, HashMap<String, String> customDateFieldMap) throws SessionExpiredException, ServiceException {
        try {
            DateFormat df = authHandler.getDateFormatter(paramJobj);
            DateFormat userdf = authHandler.getUserDateFormatterJson(paramJobj);
            String companyid = paramJobj.optString("companyid");
            Set<LinkDetailReceiptToAdvancePayment> rowSet = re.getLinkDetailReceiptsToAdvancePayment();
            for (LinkDetailReceiptToAdvancePayment row : rowSet) {
                KwlReturnObject resultInvoice = accountingHandlerDAOobj.getObject(Payment.class.getName(), row.getPaymentId());
                Payment payment = (Payment) resultInvoice.getEntityList().get(0);
                
                double exchangeratefortransaction = row.getExchangeRateForTransaction();
                double rowAmount = (authHandler.round(row.getAmount(), companyid));
//                Date dnCreationDate = payment.getJournalEntry().getEntryDate();
                Date dnCreationDate = payment.getCreationDate();

                JSONObject obj = new JSONObject();
                obj.put("transectionno", payment.getPaymentNumber());
                obj.put("transectionid", payment.getID());
                obj.put("isopening", payment.isIsOpeningBalencePayment());
                obj.put("creationdate", df.format(dnCreationDate));
                obj.put("creationdateinuserformat", userdf.format(dnCreationDate));
                obj.put("dateforsort", dnCreationDate);
                obj.put("linkingdate", row.getReceiptLinkDate()!=null ? df.format(row.getReceiptLinkDate()) : "");
                if (payment.getVendor() != null) {
                    obj.put("accountid", payment.getVendor().getID());
                    obj.put("accountname", payment.getVendor().getName());
                }
                if (payment != null) {
                    obj.put("currencyidtransaction", payment.getCurrency() == null ? (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID()) : payment.getCurrency().getCurrencyID());
                    obj.put("currencysymbol", payment.getCurrency() == null ? (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol()) : payment.getCurrency().getSymbol());
                    obj.put("currencysymboltransaction", payment.getCurrency() == null ? (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol()) : payment.getCurrency().getSymbol());
                } else {
                    obj.put("currencyidtransaction", (row.getReceipt().getCurrency() == null ? currency.getCurrencyID() : row.getReceipt().getCurrency().getCurrencyID()));
                    obj.put("currencysymbol", (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol()));
                    obj.put("currencysymboltransaction", (row.getReceipt().getCurrency() == null ? currency.getSymbol() : row.getReceipt().getCurrency().getSymbol()));
                }
                
                double amountDue = 0;
                double totalAmount = 0;
                if (payment != null) {
                    Set<AdvanceDetail> advDetailSet = payment.getAdvanceDetails();
                    for (AdvanceDetail advDetail : advDetailSet) {
                        amountDue += advDetail.getAmountDue();
                        totalAmount += advDetail.getAmount();
                    }
                }
                
                obj.put("totalamount", totalAmount);
                obj.put("enteramount", row.getAmount());
                obj.put("amountdue", amountDue);
                obj.put("exchangeratefortransaction", exchangeratefortransaction); // for documentdesigner
                obj.put("newamountdue", authHandler.round((amountDue * exchangeratefortransaction), companyid) + row.getAmount()); // for documentdesigner
                obj.put("paidamount", row.getAmountInPaymentCurrency());
                obj.put("srNoForRow", row.getSrno());
                innerJArrLinkedAdvance.put(obj);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccReceiptServiceImpl.getReceiptLinkToAdvancePaymentArray : " + ex.getMessage(), ex);
        }
    }
     public JSONArray getRPDetailsItemJSONNew(JSONObject paramJobj, String SOID, HashMap<String, Object> paramMap) throws SessionExpiredException, ServiceException, JSONException {

        JSONArray jArr = new JSONArray();
        List<Object> list = new ArrayList<Object>();
        String paymentId = "";
        KwlReturnObject result;
        PdfTemplateConfig config = null;
        Receipt receipt = new Receipt();
        JSONObject summaryData = new JSONObject();
        JSONArray invJSONArr = new JSONArray();
        JSONArray dbCustomJSONArr = new JSONArray();
        double subtotal = 0, totaltaxamount = 0.0d,amountpaid = 0,originalamountduetotal=0.0,totalAmountWithBanckChanges=0.0d,invoiceTotalDiscount = 0.0d;
        
        String GSTExchangeRate = ""; //used for singapore GST where company has singapore country and base curreny other than SGD
        String DOref = "", SOref = "", QouteRef = "", invexchangerate = "";
        String bankchequeno = "", chequebankname = "", invdesc = "", gstin = "", productName ="", HsnSacCode = "";

        //Common appendtext
        String commonAppendtext = "";
        String commonEnterPayment = "";
        String currencysymbol = "", globallevelcustomfields = "", globalleveldimensions = "", createdby = "", updatedby = "";
        String taxamount="", discountAmount="", invoiceNos = "", invoicedates = "", invduedates = "", invtax = "", invdiscount = "",creditAmount = "", debitAmount = "", accountCode = "";
        String cgstAmount = "", cgstPercent = "", sgstAmount = "", sgstPercent = "", utgstAmount = "", utgstPercent = "", igstAmount = "", igstPercent = "", cessAmount = "", cessPercent = "";
        String invoriginalamount = "", inventerpayment = "", invexchagerates = "", invoriginalamountdue = "", invamountdue = "" , invdoctype="",documentStatus="",invdocnumber="",invtaxpercent = "";
        //Card Holder Details
        String cardno = "", cardholder = "", cardreferencenumber = "", cardtype = "", customerOrVendorTitle = "",VATTInnumber = "",CSTTInNumber = "";
        
        String recordIDs = paramJobj.optString("recordids",null) != null ? paramJobj.optString("recordids") : "";
        String recArray[] = recordIDs.split(",");
        int moduleid = Integer.parseInt(paramJobj.optString(Constants.moduleid));
        /*
         * a variable to get debit/credit term of receipt
         */
        String netCreditDebitTerm = "";
        try {
            HashMap<String, Integer> FieldMap = (HashMap<String, Integer>) paramMap.get(Constants.fieldMap);
            HashMap<String, String> replaceFieldMap = (HashMap<String, String>) paramMap.get(Constants.replaceFieldMap);
            HashMap<String, Integer> DimensionFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.dimensionFieldMap);
            HashMap<String, Integer> LineLevelCustomFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.lineLevelCustomFieldMap);
            HashMap<String, Integer> ProductLevelCustomFieldMap = (HashMap<String, Integer>) paramMap.get(Constants.productLevelCustomFieldMap);
            String companyid = paramJobj.optString(Constants.companyKey);
            JSONObject detailsTableData = new JSONObject();
            JSONArray gstTaxSummaryDetailsTableDataArr = new JSONArray();
            if (recArray.length != 0) {
                paymentId = SOID;
                JSONArray DataJArr = new JSONArray();
                JSONObject jobj = new JSONObject();
                int count = 1, rowcnt = 0;
                double taxableValue = 0;
                List<JSONObject> tempList = new ArrayList<JSONObject>();
                HashMap<String, Object> invRequestParams = new HashMap<String, Object>();
                ArrayList filter_names = new ArrayList(), filter_params = new ArrayList(), order_by = new ArrayList(), order_type = new ArrayList();
                         
                int mode = Integer.parseInt(paramJobj.optString("mode"));
                companyid = (paramJobj.optString("companyids",null) != null) ? paramJobj.optString("companyids") :paramJobj.optString(Constants.companyKey);
                String gcurrencyid = (paramJobj.optString(Constants.globalCurrencyKey,null) != null) ? paramJobj.optString(Constants.globalCurrencyKey):paramJobj.optString(Constants.globalCurrencyKey);
                KwlReturnObject resultcompany = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), paramJobj.optString(Constants.companyKey));
                CompanyAccountPreferences companyAccountPreferences = (CompanyAccountPreferences) resultcompany.getEntityList().get(0);
                KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
                ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0);
                int countryLanguageId = Constants.OtherCountryLanguageId; // 0
                if (extraCompanyPreferences.isAmountInIndianWord()) {
                    countryLanguageId = Constants.CountryIndiaLanguageId; //for india id is 1;
                }
                int countryid = 0;
                if (extraCompanyPreferences.getCompany() != null && extraCompanyPreferences.getCompany().getCountry() != null) {
                    countryid = Integer.parseInt(extraCompanyPreferences.getCompany().getCountry().getID());
                }
                /*
                 * permission code
                 */
                int permCode=paramJobj.optInt("permcodecustomer");
                
                HashMap<String, Object> requestParams = getReceiptRequestMapJSON(paramJobj);
                requestParams.put("billid", paymentId);
                paramJobj.put("companyid", companyid);
                paramJobj.put("gcurrencyid", gcurrencyid);
                paramJobj.put("bills", paymentId);
                paramJobj.put("isReceiptEdit", "true");
                /**
                 * Added isForReport true to show Custom field values in DD Print SDP-10387
                 */
                paramJobj.put("isForReport", "true");
                paramJobj.put("permCode", permCode);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);
                result = accReceiptDAOobj.getReceipts(requestParams);
                tempList = getReceiptJson(paramJobj, result.getEntityList(), tempList);
                requestParams.put("dateformat", authHandler.getDateOnlyFormat());
                requestParams.put("bills", paymentId.split(","));
                requestParams.put("isReceiptEdit", "true");
                JSONObject dataarrayobj = getReceiptRowsJSONNew(paramJobj);
                DataJArr = dataarrayobj.getJSONArray("data");
                /**
                 * sort and arrange row data as per sequence in UI
                 */
                JSONObject dataJobj = null;
                HashMap<Integer, JSONObject> mapForSort = new HashMap<Integer, JSONObject>();
                for (int i = 0; i < DataJArr.length(); i++) {
                    dataJobj = DataJArr.getJSONObject(i);
                    JSONArray arr = dataJobj.getJSONArray("typedata");
                    for (int j = 0; j < arr.length(); j++) {
                        JSONObject rowDataObj = arr.getJSONObject(j);
                        int srNoForRow = 0;
                        srNoForRow = rowDataObj.getInt("srnoforrow");
                        int type = dataJobj.getInt("type");
                        if (type != Constants.AdvanceLinkedWithInvoicePayment) {
                            if (type == Constants.GLPayment) {
                                JSONObject tempJobj = new JSONObject();
                                tempJobj.put("type", type);
                                tempJobj.put("typedata", new JSONArray().put(rowDataObj));
                                mapForSort.put(srNoForRow, tempJobj);
                            } else if (type == Constants.PaymentAgainstInvoice) {
                                JSONObject tempJobj = new JSONObject();
                                tempJobj.put("type", type);
                                tempJobj.put("typedata", new JSONArray().put(rowDataObj));
                                mapForSort.put(srNoForRow, tempJobj);
                            } else if (type == Constants.PaymentAgainstCNDN){
                                JSONObject tempJobj = new JSONObject();
                                tempJobj.put("type", type);
                                tempJobj.put("typedata", new JSONArray().put(rowDataObj));
                                mapForSort.put(srNoForRow, tempJobj);
                            }else{
                                mapForSort.put(srNoForRow, dataJobj);
                            }
                        }
                    }
                }
                // create new array of row objects after sorting
                JSONArray tempDataJArr = new JSONArray();
                for (int i = 0; i < mapForSort.size(); i++) {
                    int key = (Integer) mapForSort.keySet().toArray()[i];
                    tempDataJArr.put(mapForSort.get(key));
                }
                DataJArr = tempDataJArr; // replace new arranged array to old array
                
                JSONObject obj = tempList.get(0);
                int paymentwindowtype = (Integer) obj.get("paymentwindowtype");
                
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Receipt.class.getName(), SOID);
                receipt = (Receipt) objItr.getEntityList().get(0);
                /*
                 * getting  debit term from receipt
                 */
                String debitTerm = "";
                if (receipt.getVendor() != null) {
                    KwlReturnObject vendorobjresult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), receipt.getVendor());
                    List vendorobj = vendorobjresult.getEntityList();
                    if (vendorobj.size() > 0 && vendorobj.get(0) != null) {
                        Vendor vendor = (Vendor) vendorobj.get(0);
                        if (vendor != null && vendor.getDebitTerm() != null) {
                            debitTerm = vendor.getDebitTerm().getTermname();
                        }
                    }
                }
                //document currency
                if (receipt != null && receipt.getCurrency() != null && !StringUtil.isNullOrEmpty(receipt.getCurrency().getCurrencyID())) {
                    summaryData.put(CustomDesignerConstants.DOCUMENT_CURRENCY_ID, receipt.getCurrency().getCurrencyID());
                }
                /*
                 * getting debit/credit term from receipt
                 */
                netCreditDebitTerm=receipt.getCustomer()==null?String.valueOf(debitTerm):String.valueOf(receipt.getCustomer().getCreditTerm().getTermname());
                currencysymbol=receipt.getCurrency().getSymbol();
                createdby = receipt.getCreatedby() != null ? receipt.getCreatedby().getFullName() : "";
                updatedby = receipt.getModifiedby() != null ? receipt.getModifiedby().getFullName() : "";
                double externalCurrencyRate = receipt.getExternalCurrencyRate();
                double exchangerate = 0.0;
                if (externalCurrencyRate != 0.0) {
                    exchangerate = 1 / externalCurrencyRate;
                }
                /**
                 * get customer/vendor title (Mr./Mrs.)
                 */
                if (receipt.getCustomer() != null) {
                    customerOrVendorTitle = receipt.getCustomer().getTitle();
                    VATTInnumber = receipt.getCustomer().getVATTINnumber() != null ? receipt.getCustomer().getVATTINnumber() : "";
                    CSTTInNumber = receipt.getCustomer().getCSTTINnumber() != null ? receipt.getCustomer().getCSTTINnumber() : "";
                    gstin = receipt.getCustomer().getGSTIN() != null ? receipt.getCustomer().getGSTIN() : "";
                    int deliveryDate = 0;
                    String deliveryDateVal = "";
                    String driver = "";
                    String vehicleNo = "";
                    String deliveryTime = "";
                    if (extraCompanyPreferences.isDeliveryPlanner()) {
                        deliveryDate = receipt.getCustomer() != null ? receipt.getCustomer().getDeliveryDate() : -1;
                        deliveryDateVal = accInvoiceCommon.getDeliverDayVal(deliveryDate);
                        deliveryTime = (receipt.getCustomer() != null && receipt.getCustomer().getDeliveryTime() != null) ? receipt.getCustomer().getDeliveryTime() : "";
                        driver = (receipt.getCustomer() != null && receipt.getCustomer().getDriver() != null) ? receipt.getCustomer().getDriver().getValue() : "";
                        vehicleNo = (receipt.getCustomer() != null && receipt.getCustomer().getVehicleNo() != null) ? receipt.getCustomer().getVehicleNo().getValue() : "";
                    }
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerDeliveryDate, deliveryDateVal);
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerDeliveryTime, deliveryTime);
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerDriver, driver);
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVehicleNo, vehicleNo);
                } else if (!StringUtil.isNullOrEmpty(receipt.getVendor())) {
                    KwlReturnObject vendorResult = accountingHandlerDAOobj.getObject(Vendor.class.getName(), receipt.getVendor());
                    Vendor vendor = (Vendor) vendorResult.getEntityList().get(0);
                    customerOrVendorTitle = vendor.getTitle();
                    VATTInnumber = vendor.getVATTINnumber() != null ? vendor.getVATTINnumber() : "";
                    CSTTInNumber = vendor.getCSTTINnumber() != null ? vendor.getCSTTINnumber() : "";
                    gstin = vendor.getGSTIN() != null ? vendor.getGSTIN() : "";
                }
                if(!StringUtil.isNullOrEmpty(customerOrVendorTitle)){
                    KwlReturnObject masterItemResult = accountingHandlerDAOobj.getObject(MasterItem.class.getName(), customerOrVendorTitle);
                    MasterItem masterItem = (MasterItem) masterItemResult.getEntityList().get(0);
                    customerOrVendorTitle = masterItem.getValue();
                }
                //             get Company PostText
                KwlReturnObject templateConfig = accCommonTablesDAO.getPDFTemplateRow(companyid, Integer.parseInt(paramJobj.optString("moduleid","0")));
                if (templateConfig.getEntityList().size() > 0) {
                    config = (PdfTemplateConfig) templateConfig.getEntityList().get(0);
                }
                
                for (int i = 0; i < DataJArr.length(); i++) {
                    jobj = DataJArr.getJSONObject(i);
                    int detailtype = (Integer) jobj.get("type");
                    if (detailtype == Constants.PaymentAgainstInvoice) {

                        JSONArray arr = jobj.getJSONArray("typedata");
                        for (int j = 0; j < arr.length(); j++) {
                            JSONObject tempobj = new JSONObject();
                            JSONObject data = arr.getJSONObject(j);
                            String invDesc = "Against Invoice# " + data.getString("transectionno") + " dated " + data.getString("creationdateinuserformat");
                            if (data.getString("totalamount").equals(data.getString("amountpaid"))) {
                                invDesc += " (Full)";
                            }

                            invexchangerate = "1 " + data.getString("currencysymboltransaction") + " = " + accountingHandlerDAOobj.formatDouble(data.optDouble("exchangeratefortransaction", 1.0)) + " " + receipt.getCurrency().getSymbol();

                            //to calculate linking information in Customer Invoice
                            filter_names.clear();
                            filter_names.add("invoice.ID");  //invoice is the database name
                            order_by.add("srno");
                            order_type.add("asc");
                            invRequestParams.put("filter_names", filter_names);
                            invRequestParams.put("filter_params", filter_params);
                            invRequestParams.put("order_by", order_by);
                            invRequestParams.put("order_type", order_type);
                            filter_params.clear();
                            filter_params.add(data.getString("transectionid"));
                            KwlReturnObject idresult = accInvoiceDAOObj.getInvoiceDetails(invRequestParams);
                            Iterator invitr = idresult.getEntityList().iterator();
                            boolean qouteRef = false;
                            boolean dOref = false;
                            boolean soref = false;
                            while (invitr.hasNext()) {
                                rowcnt++;
                                InvoiceDetail invdrow = (InvoiceDetail) invitr.next();
                                if (invdrow.getDeliveryOrderDetail() != null) {
                                    if (DOref.indexOf(invdrow.getDeliveryOrderDetail().getDeliveryOrder().getDeliveryOrderNumber()) == -1) {
                                        DOref += invdrow.getDeliveryOrderDetail().getDeliveryOrder().getDeliveryOrderNumber() + ",";
                                        dOref = true;
                                    }

                                } else if (invdrow.getSalesorderdetail() != null) {
                                    if (SOref.indexOf(invdrow.getSalesorderdetail().getSalesOrder().getSalesOrderNumber()) == -1) {
                                        SOref += invdrow.getSalesorderdetail().getSalesOrder().getSalesOrderNumber() + ",";
                                        soref = true;
                                    }

                                } else if (invdrow.getQuotationDetail() != null) {
                                    if (QouteRef.indexOf(invdrow.getQuotationDetail().getQuotation().getquotationNumber()) == -1) {
                                        QouteRef += invdrow.getQuotationDetail().getQuotation().getquotationNumber() + ",";
                                        qouteRef = true;
                                    }
                                }
                            }

                            if (dOref) {//removing comma
                                DOref = DOref.substring(0, DOref.length() - 1);

                            } else if (soref) {
                                SOref = SOref.substring(0, SOref.length() - 1);
                            } else if (qouteRef) {
                                QouteRef = QouteRef.substring(0, QouteRef.length() - 1);
                            }

                            // ## Get Custom Field Data 

                            for (Map.Entry<String, Integer> field : FieldMap.entrySet()) {//checks when field has any value
                                if (!data.has(field.getKey())) {
                                    summaryData.put(field.getKey(), "");
                                    tempobj.put(field.getKey(), "");
                                } else {
                                    summaryData.put(field.getKey(), data.getString(field.getKey()));
                                    tempobj.put(field.getKey(), data.getString(field.getKey()));
                                }
                            }

                            /*
                             * Putting Line level CustomFields and LIne level Dimensions
                             */
                            HashMap<String, Object> returnvalues = new HashMap<String, Object>();
                            HashMap<String, Object> extraparams = new HashMap<String, Object>();
                            extraparams.put("data", data);
                            extraparams.put("tempobj", tempobj);
                            extraparams.put(CustomDesignerConstants.isCustomfield, "false");
                            returnvalues = CustomDesignHandler.setDimensionCustomfieldValuesforMPRP(DimensionFieldMap, extraparams);//for line level dimensions
                            if (returnvalues.containsKey("tempobj")) {
                                tempobj = (JSONObject) returnvalues.get("tempobj");
                            }
                            extraparams.put(CustomDesignerConstants.isCustomfield, "true");
                            returnvalues = CustomDesignHandler.setDimensionCustomfieldValuesforMPRP(LineLevelCustomFieldMap, extraparams);//for line level customfields
                            if (returnvalues.containsKey("tempobj")) {
                                tempobj = (JSONObject) returnvalues.get("tempobj");
                            }
                            String customcurrencysymbol = accCommonTablesDAO.getCustomCurrencySymbol(data.getString("currencysymboltransaction"), companyid);//Take custom currency symbol
                            tempobj.put(CustomDesignerConstants.RPMP_DocumentType, "Invoice");
                            tempobj.put(CustomDesignerConstants.RPMP_DocumentNumber, data.getString("transectionno"));
                            tempobj.put(CustomDesignerConstants.RPMP_Description, StringUtil.DecodeText(StringUtil.isNullOrEmpty(data.optString("description", "")) ? "" : data.optString("description", "")));
                            tempobj.put(CustomDesignerConstants.RPMP_OriginalAmountDue, authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("amountDueOriginal", "0.0")), companyid));
                            tempobj.put(CustomDesignerConstants.RPMP_ExchangeRate, invexchangerate);
                            tempobj.put(CustomDesignerConstants.RPMP_AmountDue, authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("amountduenonnegative", "0.0")), companyid));
                            tempobj.put(CustomDesignerConstants.RPMP_TAXNAME, "");
                            tempobj.put(CustomDesignerConstants.RPMP_Tax, "");
                            tempobj.put(CustomDesignerConstants.RPMP_TaxAmount, authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("taxamount", "0.0")), companyid));
                            tempobj.put(CustomDesignerConstants.RPMP_EnterPayment, authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("enteramount", "0.0")), companyid));

                            tempobj.put(CustomDesignerConstants.RPMP_InvoiceDate, data.getString("creationdateinuserformat"));
                            tempobj.put(CustomDesignerConstants.RPMP_DueDate, data.getString("duedate"));
                            tempobj.put(CustomDesignerConstants.RPMP_Discount, authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("discount", "0.0")), companyid));
                            //get original amount of invoice
                            tempobj.put(CustomDesignerConstants.RPMP_OriginalAmount, authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("amount", "0.0")), companyid));
                            tempobj.put(CustomDesignerConstants.CustomDesignCurrencySymbol_fieldTypeId, customcurrencysymbol);
                            tempobj.put(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance, invDesc);
                            tempobj.put(CustomDesignerConstants.Common_EnterPayment, authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("enteramount", "0.0")), companyid));
                            tempobj.put(CustomDesignerConstants.CustomDesignSORefNumber_fieldTypeId, SOref);
                            tempobj.put(CustomDesignerConstants.CustomDesignQuoteRefNumber_fieldTypeId, QouteRef);
                            tempobj.put(CustomDesignerConstants.CustomDesignDORefNumber_fieldTypeId, DOref);
                            tempobj.put(CustomDesignerConstants.SrNo, count);
                            tempobj.put(CustomDesignerConstants.RPMP_CreditAmount, "");
                            tempobj.put(CustomDesignerConstants.RPMP_DebitAmount, "");
                            tempobj.put(CustomDesignerConstants.RPMP_ACCOUNT_CODE, "");
                            tempobj.put(CustomDesignerConstants.CustomDesignDiscount_fieldTypeId, data.optDouble("discountAmount", 0));
                            //GST Exchange Rate
                            if (companyAccountPreferences.getCompany().getCountry().getID().equalsIgnoreCase(Constants.SINGAPOREID) && !gcurrencyid.equalsIgnoreCase(Constants.SGDID)) {
                                tempobj.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, data.optDouble(Constants.GST_CURRENCY_RATE, 0.0) != 0.0 ? data.optDouble(Constants.GST_CURRENCY_RATE, 0.0) : "");
                            }
                            tempobj.put(CustomDesignerConstants.DOCUMENTSTATUS, data.optString("totalamount","0").equals(data.optString("amountpaid","0")) ? CustomDesignerConstants.FULL : CustomDesignerConstants.PARTIAL);
                            invJSONArr.put(tempobj);
                            count++;
                            originalamountduetotal += Double.parseDouble(data.optString("amountDueOriginal", "0.0"));  //ERP-19271
                            invoiceTotalDiscount += data.optDouble("discount", 0);
                        }
                    } else if (detailtype == Constants.GLPayment) {
                        JSONArray arr = jobj.getJSONArray("typedata");
                        for (int j = 0; j < arr.length(); j++) {
                            JSONObject tempobj = new JSONObject();
                            JSONObject data = arr.getJSONObject(j);
                            String taxname = "";
                            String taxpercent = "";
                            String invDesc = "Against GL Account " + (data.optString("accountcode", "").equals("") ? "" : data.optString("accountcode", "") + " - ") + data.getString("accountname");
                            taxname = data.optString("taxname", "");
                            taxpercent = data.optString("taxpercent", "")+"%";
                            invexchangerate = "1 " + receipt.getCurrency().getSymbol() + " = 1 "+ receipt.getCurrency().getSymbol();
                            
                            // ## Get Custom Field Data 
                            for (Map.Entry<String, Integer> field : FieldMap.entrySet()) {//checks when field has any value
                                if (!data.has(field.getKey())) {
                                    summaryData.put(field.getKey(), "");
                                    tempobj.put(field.getKey(),"");
                                } else {
                                    summaryData.put(field.getKey(), "");
                                    tempobj.put(field.getKey(), data.getString(field.getKey()));
                                }
                            }
                      
                            /*
                             * Putting Line level CustomFields and LIne level Dimensions
                             */
                            HashMap<String, Object> returnvalues = new HashMap<String, Object>();
                            HashMap<String, Object> extraparams = new HashMap<String, Object>();
                            extraparams.put("data", data);
                            extraparams.put("tempobj", tempobj);
                            extraparams.put(CustomDesignerConstants.isCustomfield, "false");
                            returnvalues = CustomDesignHandler.setDimensionCustomfieldValuesforMPRP(DimensionFieldMap, extraparams);//for line level dimensions
                            if (returnvalues.containsKey("tempobj")) {
                                tempobj = (JSONObject) returnvalues.get("tempobj");
                            }
                            extraparams.put(CustomDesignerConstants.isCustomfield, "true");
                            returnvalues = CustomDesignHandler.setDimensionCustomfieldValuesforMPRP(LineLevelCustomFieldMap, extraparams);//for line level customfields
                            if (returnvalues.containsKey("tempobj")) {
                                tempobj = (JSONObject) returnvalues.get("tempobj");
                            }

                            tempobj.put(CustomDesignerConstants.RPMP_DocumentType, "General Ledger");
                            tempobj.put(CustomDesignerConstants.RPMP_DocumentNumber, StringUtil.isNullOrEmpty(data.getString("accountname")) ? data.getString("accountcode") : data.getString("accountname"));
                            tempobj.put(CustomDesignerConstants.RPMP_Description, StringUtil.DecodeText(StringUtil.isNullOrEmpty(data.optString("description","")) ? "" : data.optString("description","")));
                            tempobj.put(CustomDesignerConstants.RPMP_OriginalAmountDue, authHandler.formattedCommaSeparatedAmount(Double.parseDouble("0.0"), companyid));
                            tempobj.put(CustomDesignerConstants.RPMP_ExchangeRate, invexchangerate);
                            tempobj.put(CustomDesignerConstants.RPMP_AmountDue, authHandler.formattedCommaSeparatedAmount(Double.parseDouble("0.0"), companyid));
                            tempobj.put(CustomDesignerConstants.RPMP_TAXNAME, taxname);
                            tempobj.put(CustomDesignerConstants.RPMP_Tax, taxpercent);
                            tempobj.put(CustomDesignerConstants.RPMP_TaxAmount, authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("taxamount", "0.0")), companyid));
                            if (data.optString("debit", "false").equals("false")) {
                                tempobj.put(CustomDesignerConstants.Common_EnterPayment, authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("totalamount", "0.0")), companyid));
                                tempobj.put(CustomDesignerConstants.RPMP_EnterPayment,Double.parseDouble(data.optString("totalamount", "0.0")));
                                tempobj.put(CustomDesignerConstants.RPMP_CreditAmount, authHandler.formattedCommaSeparatedAmount((Double.parseDouble(data.getString("totalamount").contains("-") ? data.getString("totalamount") : "-" + data.getString("totalamount"))), companyid));
                                tempobj.put(CustomDesignerConstants.RPMP_DebitAmount,"");
                            } else {
                                tempobj.put(CustomDesignerConstants.Common_EnterPayment, "-"+authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("totalamount", "0.0")), companyid));
                                tempobj.put(CustomDesignerConstants.RPMP_EnterPayment,"-"+Double.parseDouble(data.optString("totalamount", "0.0")));
                                tempobj.put(CustomDesignerConstants.RPMP_CreditAmount,"");
                                tempobj.put(CustomDesignerConstants.RPMP_DebitAmount,authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.getString("totalamount")), companyid));
                            }
                            tempobj.put(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance, invDesc);
                            tempobj.put(CustomDesignerConstants.CustomDesignSORefNumber_fieldTypeId, "");
                            tempobj.put(CustomDesignerConstants.CustomDesignQuoteRefNumber_fieldTypeId, "");
                            tempobj.put(CustomDesignerConstants.CustomDesignDORefNumber_fieldTypeId, "");
                            tempobj.put(CustomDesignerConstants.RPMP_InvoiceDate,"");
                            tempobj.put(CustomDesignerConstants.RPMP_DueDate,"");
                            tempobj.put(CustomDesignerConstants.RPMP_Discount,"");
                            tempobj.put(CustomDesignerConstants.RPMP_OriginalAmount,"");
                            tempobj.put(CustomDesignerConstants.CustomDesignCurrencySymbol_fieldTypeId,"");
                            tempobj.put(CustomDesignerConstants.RPMP_ACCOUNT_CODE, data.optString("accountcode", ""));
                            tempobj.put(CustomDesignerConstants.SrNo, count);
                            tempobj.put(CustomDesignerConstants.CustomDesignDiscount_fieldTypeId, 0);
                            tempobj.put(CustomDesignerConstants.DOCUMENTSTATUS, "-");
                            invJSONArr.put(tempobj);
                            count++;
                        }
                    } else if (detailtype == Constants.PaymentAgainstCNDN) {
                        JSONArray arr = jobj.getJSONArray("typedata");
                        for (int j = 0; j < arr.length(); j++) {
                            String taxname="",taxpercent = "";
                            JSONObject tempobj = new JSONObject();
                            JSONObject data = arr.getJSONObject(j);
                            String invDesc = mode == StaticValues.AUTONUM_PAYMENT ? "Against Credit Note# " : "Against Debit Note# ";
                            invDesc += data.getString("transectionno") + " dated " + data.getString("creationdate");
                            if (data.getString("totalamount").equals(data.getString("cnpaidamount"))) {
                                invDesc += " (Full)";
                            }
                            
                            invexchangerate = "1 " +  data.getString("currencysymboltransaction") + " = " + accountingHandlerDAOobj.formatDouble(data.optDouble("exchangeratefortransaction", 1.0)) + " " + receipt.getCurrency().getSymbol();
                                          
                            // ## Get Custom Field Data 
                            for (Map.Entry<String, Integer> field : FieldMap.entrySet()) {//checks when field has any value
                                if (!data.has(field.getKey())) {
                                    summaryData.put(field.getKey(), "");
                                    tempobj.put(field.getKey(),"");
                                } else {
                                    summaryData.put(field.getKey(), "");
                                    tempobj.put(field.getKey(), data.getString(field.getKey()));
                                }
                            }
                            
                            /*
                             * Putting Line level CustomFields and LIne level Dimensions
                             */
                            HashMap<String, Object> returnvalues = new HashMap<String, Object>();
                            HashMap<String, Object> extraparams = new HashMap<String, Object>();
                            extraparams.put("data", data);
                            extraparams.put("tempobj", tempobj);
                            extraparams.put(CustomDesignerConstants.isCustomfield, "false");
                            returnvalues = CustomDesignHandler.setDimensionCustomfieldValuesforMPRP(DimensionFieldMap, extraparams);//for line level dimensions
                            if (returnvalues.containsKey("tempobj")) {
                                tempobj = (JSONObject) returnvalues.get("tempobj");
                            }
                            extraparams.put(CustomDesignerConstants.isCustomfield, "true");
                            returnvalues = CustomDesignHandler.setDimensionCustomfieldValuesforMPRP(LineLevelCustomFieldMap, extraparams);//for line level customfields
                            if (returnvalues.containsKey("tempobj")) {
                                tempobj = (JSONObject) returnvalues.get("tempobj");
                            }

                            tempobj.put(CustomDesignerConstants.RPMP_DocumentType, "Debit Note");
                            tempobj.put(CustomDesignerConstants.RPMP_DocumentNumber, data.optString("transectionno"));
                            tempobj.put(CustomDesignerConstants.RPMP_Description,StringUtil.DecodeText(StringUtil.isNullOrEmpty(data.optString("description", "")) ? "" : data.optString("description", "")));
                            tempobj.put(CustomDesignerConstants.RPMP_OriginalAmountDue, authHandler.formattedCommaSeparatedAmount(Double.parseDouble(data.optString("newamountdue", "0.0")), companyid)); 
                            tempobj.put(CustomDesignerConstants.RPMP_ExchangeRate, invexchangerate);
                            tempobj.put(CustomDesignerConstants.RPMP_AmountDue, authHandler.formattedCommaSeparatedAmount((Double.parseDouble(data.optString("newamountdue", "0.0"))), companyid)); 
                            tempobj.put(CustomDesignerConstants.RPMP_TAXNAME, taxname);
                            tempobj.put(CustomDesignerConstants.RPMP_Tax, taxpercent);
                            tempobj.put(CustomDesignerConstants.RPMP_TaxAmount, authHandler.formattedCommaSeparatedAmount((Double.parseDouble(data.optString("taxamount", "0.0"))), companyid));
                            tempobj.put(CustomDesignerConstants.RPMP_EnterPayment, authHandler.formattedCommaSeparatedAmount((Double.parseDouble(data.optString("enteramount", "0.0"))), companyid));
                            tempobj.put(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance, invDesc);
                            tempobj.put(CustomDesignerConstants.Common_EnterPayment, authHandler.formattedCommaSeparatedAmount((Double.parseDouble(data.optString("enteramount", "0.0"))), companyid));
                            tempobj.put(CustomDesignerConstants.CustomDesignSORefNumber_fieldTypeId, "");
                            tempobj.put(CustomDesignerConstants.CustomDesignQuoteRefNumber_fieldTypeId, "");
                            tempobj.put(CustomDesignerConstants.CustomDesignDORefNumber_fieldTypeId, "");
                            tempobj.put(CustomDesignerConstants.RPMP_InvoiceDate, "");
                            tempobj.put(CustomDesignerConstants.RPMP_DueDate, "");
                            tempobj.put(CustomDesignerConstants.RPMP_Discount, "");
                            tempobj.put(CustomDesignerConstants.RPMP_OriginalAmount, "");
                            tempobj.put(CustomDesignerConstants.CustomDesignCurrencySymbol_fieldTypeId,"");
                            tempobj.put(CustomDesignerConstants.SrNo,count);
                            tempobj.put(CustomDesignerConstants.RPMP_CreditAmount,"");
                            tempobj.put(CustomDesignerConstants.RPMP_DebitAmount,"");
                            tempobj.put(CustomDesignerConstants.RPMP_ACCOUNT_CODE,"");
                            tempobj.put(CustomDesignerConstants.CustomDesignDiscount_fieldTypeId, 0);
                            //GST Exchange Rate
                            if (companyAccountPreferences.getCompany().getCountry().getID().equalsIgnoreCase(Constants.SINGAPOREID) && !gcurrencyid.equalsIgnoreCase(Constants.SGDID)) {
                                tempobj.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, data.optDouble(Constants.GST_CURRENCY_RATE, 0.0) != 0.0 ? data.optDouble(Constants.GST_CURRENCY_RATE, 0.0) : "");
                            }
                            tempobj.put(CustomDesignerConstants.DOCUMENTSTATUS, data.optString("totalamount","0").equals(data.optString("cnpaidamount","0")) ? CustomDesignerConstants.FULL : CustomDesignerConstants.PARTIAL);
                            invJSONArr.put(tempobj);
                            count++;
                            originalamountduetotal += Double.parseDouble(data.optString("newamountdue", "0.0"));  //ERP-19271
                        }

                    } else if (detailtype == Constants.AdvancePayment) {
                        JSONArray arr = jobj.getJSONArray("typedata");
                        for (int j = 0; j < arr.length(); j++) {
                            JSONObject tempobj = new JSONObject();
                            JSONObject data = arr.getJSONObject(j);
                            JSONObject gstTaxSummaryDetailsTableData = new JSONObject();
                            String invDesc = "Advance Amount ";
                            invDesc += (mode == StaticValues.AUTONUM_PAYMENT ? " to " : " from ");
                            if (!data.optString("acccode", "").equals("") && !data.optString("accname", "").equals("")) {
                                invDesc += data.getString("acccode") + "-" + data.getString("accname");
                                if (!data.optString("accountcode", "").equals("") && !data.optString("accountname", "").equals("")) {
                                    invDesc += " (" + data.getString("accountcode") + "-" + data.getString("accountname") + ")";
                                } else if (!data.optString("accountname", "").equals("")) {
                                    invDesc += " (" + data.getString("accountname") + ")";
                                }
                            } else if (!data.optString("accname", "").equals("")) {
                                invDesc += data.getString("accname");
                                if (!data.optString("accountcode", "").equals("") && !data.optString("accountname", "").equals("")) {
                                    invDesc += " (" + data.getString("accountcode") + "-" + data.getString("accountname") + ")";
                                } else if (!data.optString("accountname", "").equals("")) {
                                    invDesc += " (" + data.getString("accountname") + ")";
                                }
                            }   // This will add the account name with code and then account holder name with code  

                            invexchangerate = "1 " +  receipt.getCurrency().getSymbol() + " = 1 "+receipt.getCurrency().getSymbol();
                            
                            // ## Get Custom Field Data 
                            for (Map.Entry<String, Integer> field : FieldMap.entrySet()) {//checks when field has any value
                                if (!data.has(field.getKey())) {
                                    summaryData.put(field.getKey(), "");
                                    tempobj.put(field.getKey(),"");
                                } else {
                                    summaryData.put(field.getKey(), "");
                                    tempobj.put(field.getKey(), data.getString(field.getKey()));
                                }
                            }                          

                            /*
                             * Putting Line level CustomFields and LIne level Dimensions
                             */
                            HashMap<String, Object> returnvalues = new HashMap<String, Object>();
                            HashMap<String, Object> extraparams = new HashMap<String, Object>();
                            extraparams.put("data", data);
                            extraparams.put("tempobj", tempobj);
                            extraparams.put(CustomDesignerConstants.isCustomfield, "false");
                            returnvalues = CustomDesignHandler.setDimensionCustomfieldValuesforMPRP(DimensionFieldMap, extraparams);//for line level dimensions
                            if (returnvalues.containsKey("tempobj")) {
                                tempobj = (JSONObject) returnvalues.get("tempobj");
                            }
                            extraparams.put(CustomDesignerConstants.isCustomfield, "true");
                            returnvalues = CustomDesignHandler.setDimensionCustomfieldValuesforMPRP(LineLevelCustomFieldMap, extraparams);//for line level customfields
                            if (returnvalues.containsKey("tempobj")) {
                                tempobj = (JSONObject) returnvalues.get("tempobj");
                            }
                            
                            if (paymentwindowtype == Constants.Make_Payment_to_Vendor) {
                            tempobj.put(CustomDesignerConstants.RPMP_DocumentType, "Advance /Deposit");
                            }else  if (paymentwindowtype == Constants.Make_Payment_to_Customer) {
                               tempobj.put(CustomDesignerConstants.RPMP_DocumentType, "Refund /Deposit");
                            }
                            /**
                             * GST details
                             */
                            if(extraCompanyPreferences.isIsNewGST()){ // for New gst check 
                                JSONObject gstParams = new JSONObject();
                                gstParams.put("adId", data.optString("rowid", ""));
                                //get tax details of payment received
                                KwlReturnObject recTermMapresult = accReceiptDAOobj.getAdvanceDetailsTerm(gstParams);
                                List<ReceiptAdvanceDetailTermMap> gst = recTermMapresult.getEntityList();
                                //Put 0 as default value in all tax amount and percent fields
                                tempobj.put(CustomDesignerConstants.CGSTPERCENT, 0);
                                tempobj.put(CustomDesignerConstants.CGSTAMOUNT, 0);
                                tempobj.put(CustomDesignerConstants.IGSTPERCENT, 0);
                                tempobj.put(CustomDesignerConstants.IGSTAMOUNT, 0);
                                tempobj.put(CustomDesignerConstants.SGSTPERCENT, 0);
                                tempobj.put(CustomDesignerConstants.SGSTAMOUNT, 0);
                                tempobj.put(CustomDesignerConstants.UTGSTPERCENT, 0);
                                tempobj.put(CustomDesignerConstants.UTGSTAMOUNT, 0);
                                tempobj.put(CustomDesignerConstants.CESSPERCENT, 0);
                                tempobj.put(CustomDesignerConstants.CESSAMOUNT, 0);
                                gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.TAXABLE_VALUE, data.optString("totalamount"));
                                
                                gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTPERCENT, 0);
                                gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTAMOUNT, 0);
                                gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTPERCENT, 0);
                                gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTAMOUNT, 0);
                                gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTPERCENT, 0);
                                gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTAMOUNT, 0);
                                gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTPERCENT, 0);
                                gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTAMOUNT, 0);
                                gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSPERCENT, 0);
                                gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSAMOUNT, 0);
                    
                                //Put value in tax amount and tax percent fields
                                for (ReceiptAdvanceDetailTermMap receiptAdvanceDetailTermMap : gst) {
                                    LineLevelTerms mt = receiptAdvanceDetailTermMap.getEntitybasedLineLevelTermRate().getLineLevelTerms();
                                    if (mt.getTerm().contains(CustomDesignerConstants.CGST)) {
                                        //CGST Tax
                                        tempobj.put(CustomDesignerConstants.CGSTPERCENT, receiptAdvanceDetailTermMap.getPercentage());
                                        tempobj.put(CustomDesignerConstants.CGSTAMOUNT, receiptAdvanceDetailTermMap.getTermamount());
                                        
                                        gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTPERCENT, receiptAdvanceDetailTermMap.getPercentage());
                                        gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CGSTAMOUNT, receiptAdvanceDetailTermMap.getTermamount());
                                    } else if (mt.getTerm().contains(CustomDesignerConstants.IGST)) {
                                        //IGST Tax
                                        tempobj.put(CustomDesignerConstants.IGSTPERCENT, receiptAdvanceDetailTermMap.getPercentage());
                                        tempobj.put(CustomDesignerConstants.IGSTAMOUNT, receiptAdvanceDetailTermMap.getTermamount());
                                        
                                        gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTPERCENT, receiptAdvanceDetailTermMap.getPercentage());
                                        gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.IGSTAMOUNT, receiptAdvanceDetailTermMap.getTermamount());
                                    } else if (mt.getTerm().contains(CustomDesignerConstants.SGST)) {
                                        //SGST Tax
                                        tempobj.put(CustomDesignerConstants.SGSTPERCENT, receiptAdvanceDetailTermMap.getPercentage());
                                        tempobj.put(CustomDesignerConstants.SGSTAMOUNT, receiptAdvanceDetailTermMap.getTermamount());
                                        
                                        gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTPERCENT, receiptAdvanceDetailTermMap.getPercentage());
                                        gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.SGSTAMOUNT, receiptAdvanceDetailTermMap.getTermamount());
                                    } else if (mt.getTerm().contains(CustomDesignerConstants.UTGST)) {
                                        //UTGST Tax
                                        tempobj.put(CustomDesignerConstants.UTGSTPERCENT, receiptAdvanceDetailTermMap.getPercentage());
                                        tempobj.put(CustomDesignerConstants.UTGSTAMOUNT, receiptAdvanceDetailTermMap.getTermamount());
                                        
                                        gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTPERCENT, receiptAdvanceDetailTermMap.getPercentage());
                                        gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.UTGSTAMOUNT, receiptAdvanceDetailTermMap.getTermamount());
                                    } else if (mt.getTerm().contains(CustomDesignerConstants.CESS)) {
                                        //CESS Tax
                                        tempobj.put(CustomDesignerConstants.CESSPERCENT, receiptAdvanceDetailTermMap.getPercentage());
                                        tempobj.put(CustomDesignerConstants.CESSAMOUNT, receiptAdvanceDetailTermMap.getTermamount());
                                        
                                        gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSPERCENT, receiptAdvanceDetailTermMap.getPercentage());
                                        gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.CESSAMOUNT, receiptAdvanceDetailTermMap.getTermamount());
                                    }
                                }
                                
                                /**
                                 * Indian Company case - To get product name from Advance Payment Receipt
                                 */
                                HashMap<String, Object> prefparams = new HashMap<>();
                                prefparams.put("id", data.optString("rowid"));
                                Product product = (Product) kwlCommonTablesDAO.getRequestedObjectFields(ReceiptAdvanceDetail.class, new String[]{"product"}, prefparams);
                                
                                if(!StringUtil.isNullObject(product)) {
                                    tempobj.put(CustomDesignerConstants.ProductName, product.getName());
                                }
                                ExportRecordHandler.setHsnSacProductDimensionField(product, tempobj, companyid, accAccountDAOobj, kwlCommonTablesDAO);
                                gstTaxSummaryDetailsTableData.put(CustomDesignerConstants.HSN_SAC_CODE, tempobj.optString(CustomDesignerConstants.HSN_SAC_CODE, ""));
//                                tempobj.put(CustomDesignerConstants.HSN_SAC_CODE, tempobj.optString(CustomDesignerConstants.HSN_SAC_CODE, ""));
                                gstTaxSummaryDetailsTableDataArr.put(gstTaxSummaryDetailsTableData);
                                
                            }
                            tempobj.put(CustomDesignerConstants.RPMP_DocumentNumber, data.optString("transectionno"));
                            tempobj.put(CustomDesignerConstants.RPMP_Description,StringUtil.DecodeText(StringUtil.isNullOrEmpty(data.optString("description",""))?"":data.optString("description","")));
                            tempobj.put(CustomDesignerConstants.RPMP_OriginalAmountDue,authHandler.formattedCommaSeparatedAmount((Double.parseDouble(data.optString("amountdue", "0.0"))), companyid)); 
                            tempobj.put(CustomDesignerConstants.RPMP_ExchangeRate, invexchangerate);
                            tempobj.put(CustomDesignerConstants.RPMP_AmountDue,  authHandler.formattedCommaSeparatedAmount((Double.parseDouble(data.optString("amountdue", "0.0"))), companyid)); 
                            tempobj.put(CustomDesignerConstants.RPMP_TAXNAME, "");
                            tempobj.put(CustomDesignerConstants.RPMP_Tax, "");
                            tempobj.put(CustomDesignerConstants.RPMP_TaxAmount, authHandler.formattedCommaSeparatedAmount((Double.parseDouble(data.optString("taxamount", "0.0"))), companyid));
                            tempobj.put(CustomDesignerConstants.RPMP_EnterPayment, authHandler.formattedCommaSeparatedAmount((Double.parseDouble(data.optString("totalamount", "0.0"))), companyid)); 
                            tempobj.put(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance, invDesc);
                            tempobj.put(CustomDesignerConstants.Common_EnterPayment, authHandler.formattedCommaSeparatedAmount((Double.parseDouble(data.optString("totalamount", "0.0"))), companyid)); 
                            tempobj.put(CustomDesignerConstants.CustomDesignSORefNumber_fieldTypeId, "");
                            tempobj.put(CustomDesignerConstants.CustomDesignQuoteRefNumber_fieldTypeId, "");
                            tempobj.put(CustomDesignerConstants.CustomDesignDORefNumber_fieldTypeId, "");
                            tempobj.put(CustomDesignerConstants.RPMP_InvoiceDate, "");
                            tempobj.put(CustomDesignerConstants.RPMP_DueDate, "");
                            tempobj.put(CustomDesignerConstants.RPMP_Discount, "");
                            tempobj.put(CustomDesignerConstants.RPMP_OriginalAmount, "");
                            tempobj.put(CustomDesignerConstants.CustomDesignCurrencySymbol_fieldTypeId,"");
                            tempobj.put(CustomDesignerConstants.SrNo,count);
                            tempobj.put(CustomDesignerConstants.RPMP_CreditAmount,"");
                            tempobj.put(CustomDesignerConstants.RPMP_DebitAmount,"");
                            tempobj.put(CustomDesignerConstants.RPMP_ACCOUNT_CODE,"");
                            tempobj.put(CustomDesignerConstants.CustomDesignDiscount_fieldTypeId, 0);
                            tempobj.put(CustomDesignerConstants.DOCUMENTSTATUS,"-");
                            invJSONArr.put(tempobj);
                            count++;
                            originalamountduetotal += Double.parseDouble(data.optString("amountdue", "0.0"));  //ERP-19271
                        }
                        detailsTableData.put("gsttaxsummary", gstTaxSummaryDetailsTableDataArr);
                        detailsTableData.put("isDetailsTableData", true);
                    }
                    
                }
                double bankcharges =0.0;
                if (!obj.optString("bankCharges", "0.0").equals("0.0")) {
                    bankcharges = Double.parseDouble(obj.getString("bankCharges"));
                }
                int detailType = Integer.parseInt(obj.optString("detailtype", "0"));
                if (detailType == PaymentMethod.TYPE_CASH) {
                    summaryData.put(CustomDesignerConstants.Chequeno, bankchequeno);
                    summaryData.put(CustomDesignerConstants.BankName, chequebankname);
                    summaryData.put(CustomDesignerConstants.ChequeDate, "");
                    summaryData.put(CustomDesignerConstants.BankDescription, "");
                    summaryData.put(CustomDesignerConstants.Cheque_Payment_Status, "");
                    summaryData.put(CustomDesignerConstants.Cheque_Payment_ClearanceDate, "");
                    summaryData.put(CustomDesignerConstants.Card_Reference_Number, cardreferencenumber);
                    summaryData.put(CustomDesignerConstants.CardHolderName, cardholder);
                    summaryData.put(CustomDesignerConstants.ChequeDate, "");
                    summaryData.put(CustomDesignerConstants.CardNo, cardno);
                    summaryData.put(CustomDesignerConstants.Card_Type, cardtype);
                    summaryData.put(CustomDesignerConstants.Card_ExpiryDate, "");

                } else if (detailType == PaymentMethod.TYPE_CARD) {

                    summaryData.put(CustomDesignerConstants.Card_Reference_Number, obj.optString("refno", ""));
                    summaryData.put(CustomDesignerConstants.CardHolderName, obj.optString("refname", ""));
                    summaryData.put(CustomDesignerConstants.CardNo,obj.optString("refcardno", ""));
                    summaryData.put(CustomDesignerConstants.Card_Type, cardtype);
                    summaryData.put(CustomDesignerConstants.Card_ExpiryDate, obj.optString("expirydate", ""));
                    summaryData.put(CustomDesignerConstants.Chequeno, bankchequeno);
                    summaryData.put(CustomDesignerConstants.BankName, chequebankname);
                    summaryData.put(CustomDesignerConstants.ChequeDate, "");
                    summaryData.put(CustomDesignerConstants.BankDescription, "");
                    summaryData.put(CustomDesignerConstants.Cheque_Payment_Status, "");
                    summaryData.put(CustomDesignerConstants.Cheque_Payment_ClearanceDate, "");

                } else if (detailType == PaymentMethod.TYPE_BANK) {

                    summaryData.put(CustomDesignerConstants.Chequeno, obj.optString("chequenumber", ""));
                    summaryData.put(CustomDesignerConstants.BankName, obj.optString("bankname", ""));
                    summaryData.put(CustomDesignerConstants.ChequeDate, obj.optString("chequedate", ""));
                    summaryData.put(CustomDesignerConstants.BankDescription, obj.optString("chequedescription", ""));
                    if(StringUtil.isNullOrEmpty(obj.optString("clearancedate", ""))){
                        summaryData.put(CustomDesignerConstants.Cheque_Payment_Status, "Uncleared");
                        summaryData.put(CustomDesignerConstants.Cheque_Payment_ClearanceDate, "");
                    }else{
                       summaryData.put(CustomDesignerConstants.Cheque_Payment_Status, "Cleared");
                       summaryData.put(CustomDesignerConstants.Cheque_Payment_ClearanceDate,obj.optString("clearancedate", ""));
                    }
                    summaryData.put(CustomDesignerConstants.Card_Reference_Number, "");
                    summaryData.put(CustomDesignerConstants.CardHolderName, "");
                    summaryData.put(CustomDesignerConstants.CardNo, "");
                    summaryData.put(CustomDesignerConstants.Card_Type, "");
                    summaryData.put(CustomDesignerConstants.Card_ExpiryDate, "");
                }
                summaryData.put(CustomDesignerConstants.PaymentMethod, obj.optString("paymentmethod", ""));
                summaryData.put(CustomDesignerConstants.PaymentAccount, obj.optString("paymentaccount", ""));
                summaryData.put(CustomDesignerConstants.Bank_AccountNumber, obj.optString("paymentaccountnumber", ""));
                summaryData.put(CustomDesignerConstants.Bank_AccountCode, obj.optString("paymentaccountcode", ""));
                
                jArr.put(invJSONArr);
            // Append comma separated invoice grid values-Against Customer Invoice
                if (invJSONArr != null) {
                    for (int cnt = 0; cnt < invJSONArr.length(); cnt++) {
                        JSONObject jObj = (JSONObject) invJSONArr.get(cnt);
                        if (cnt == 0) {
                            invoicedates = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_InvoiceDate)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_InvoiceDate,""));
                            invduedates = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_DueDate)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_DueDate,""));
                            invtax = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_TAXNAME)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_TAXNAME,""));
                            invtaxpercent = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_Tax)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_Tax,""));
                            invdiscount = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_Discount)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_Discount,""));
                            invoriginalamount = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_OriginalAmount)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_OriginalAmount,""));
                            invoriginalamountdue = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_OriginalAmountDue)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_OriginalAmountDue,""));
                            invexchagerates = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_ExchangeRate)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_ExchangeRate,""));
                            invamountdue = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_AmountDue)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_AmountDue,""));
                            invdoctype = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_DocumentType)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_DocumentType,""));
                            inventerpayment = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_EnterPayment)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_EnterPayment,""));
                            invdocnumber = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_DocumentNumber)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_DocumentNumber,""));
                            commonEnterPayment = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.Common_EnterPayment)) ? "-" : jObj.optString(CustomDesignerConstants.Common_EnterPayment,""));
                            invdesc = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_Description)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_Description,""));
                            productName = (StringUtil.isNullOrEmpty(jObj.optString(CustomDesignerConstants.ProductName)) ? "-" : jObj.optString(CustomDesignerConstants.ProductName,""));
                            HsnSacCode = (StringUtil.isNullOrEmpty(jObj.optString(CustomDesignerConstants.HSN_SAC_CODE)) ? "-" : jObj.optString(CustomDesignerConstants.HSN_SAC_CODE,""));
                            commonAppendtext = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance)) ? "-" : jObj.optString(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance,""));
                            taxamount = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_TaxAmount)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_TaxAmount,""));
                            creditAmount = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_CreditAmount)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_CreditAmount));
                            debitAmount = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_DebitAmount)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_DebitAmount));
                            accountCode = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_ACCOUNT_CODE)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_ACCOUNT_CODE));
                            //get discount amount
                            discountAmount = (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.CustomDesignDiscount_fieldTypeId)) ? "-" : jObj.getString(CustomDesignerConstants.CustomDesignDiscount_fieldTypeId));
                            //Tax related fields
                            cgstPercent = jObj.optString(CustomDesignerConstants.CGSTPERCENT, "0");
                            cgstAmount = jObj.optString(CustomDesignerConstants.CGSTAMOUNT, "0");
                            sgstPercent = jObj.optString(CustomDesignerConstants.SGSTPERCENT, "0");
                            sgstAmount = jObj.optString(CustomDesignerConstants.SGSTAMOUNT, "0");
                            utgstPercent = jObj.optString(CustomDesignerConstants.UTGSTPERCENT, "0");
                            utgstAmount = jObj.optString(CustomDesignerConstants.UTGSTAMOUNT, "0");
                            igstPercent = jObj.optString(CustomDesignerConstants.IGSTPERCENT, "0");
                            igstAmount = jObj.optString(CustomDesignerConstants.IGSTAMOUNT, "0");
                            cessPercent = jObj.optString(CustomDesignerConstants.CESSPERCENT, "0");
                            cessAmount = jObj.optString(CustomDesignerConstants.CESSAMOUNT, "0");
                            GSTExchangeRate = jObj.optString(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, "");
                            documentStatus = jObj.optString(CustomDesignerConstants.DOCUMENTSTATUS, "-");
                        } else {
//                     invoiceNos = invoiceNos + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_CustomerVendorInvoiceNo)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_CustomerVendorInvoiceNo));
                            invoicedates = invoicedates + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_InvoiceDate)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_InvoiceDate,""));
                            invduedates = invduedates + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_DueDate)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_DueDate,""));
                            invtax = invtax + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_TAXNAME)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_TAXNAME,""));
                            invtaxpercent = invtaxpercent + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_Tax)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_Tax,""));
                            invdiscount = invdiscount + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_Discount)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_Discount,""));
                            invoriginalamount = invoriginalamount + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_OriginalAmount)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_OriginalAmount,""));
                            invoriginalamountdue = invoriginalamountdue + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_OriginalAmountDue)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_OriginalAmountDue,""));
                            invexchagerates = invexchagerates + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_ExchangeRate)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_ExchangeRate,""));
                            invamountdue = invamountdue + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_AmountDue)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_AmountDue,""));
                            invdoctype = invdoctype + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_DocumentType)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_DocumentType,""));
                            invdocnumber = invdocnumber + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_DocumentNumber)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_DocumentNumber,""));
                            inventerpayment = inventerpayment + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_EnterPayment)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_EnterPayment,""));
                            commonEnterPayment = commonEnterPayment + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.Common_EnterPayment)) ? "-" : jObj.optString(CustomDesignerConstants.Common_EnterPayment,""));
                            invdesc = invdesc + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_Description)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_Description,""));
                            productName = productName + "," + (StringUtil.isNullOrEmpty(jObj.optString(CustomDesignerConstants.ProductName)) ? "-" : jObj.optString(CustomDesignerConstants.ProductName,""));
                            HsnSacCode = HsnSacCode + "," + (StringUtil.isNullOrEmpty(jObj.optString(CustomDesignerConstants.HSN_SAC_CODE)) ? "-" : jObj.optString(CustomDesignerConstants.HSN_SAC_CODE,""));
                            commonAppendtext = commonAppendtext + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance)) ? "-" : jObj.optString(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance,""));
                            taxamount = taxamount + "," + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_TaxAmount)) ? "-" : jObj.optString(CustomDesignerConstants.RPMP_TaxAmount,""));
                            creditAmount = creditAmount + ", " + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_CreditAmount)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_CreditAmount));
                            debitAmount = debitAmount + ", " + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_DebitAmount)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_DebitAmount));
                            accountCode = accountCode + ", " + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.RPMP_ACCOUNT_CODE)) ? "-" : jObj.getString(CustomDesignerConstants.RPMP_ACCOUNT_CODE));
                            documentStatus = documentStatus + "," + jObj.optString(CustomDesignerConstants.DOCUMENTSTATUS,"-");
                            //get and append discount amount
                            discountAmount = discountAmount + ", " + (StringUtil.isNullOrEmpty(jObj.getString(CustomDesignerConstants.CustomDesignDiscount_fieldTypeId)) ? "-" : jObj.getString(CustomDesignerConstants.CustomDesignDiscount_fieldTypeId));
                            //Tax related fields
                            cgstPercent = cgstPercent + ", " + jObj.optString(CustomDesignerConstants.CGSTPERCENT, "0");
                            cgstAmount = cgstAmount + ", " + jObj.optString(CustomDesignerConstants.CGSTAMOUNT, "0");
                            sgstPercent = sgstPercent + ", " + jObj.optString(CustomDesignerConstants.SGSTPERCENT, "0");
                            sgstAmount = sgstAmount + ", " +jObj.optString(CustomDesignerConstants.SGSTAMOUNT, "0");
                            utgstPercent = utgstPercent + ", " + jObj.optString(CustomDesignerConstants.UTGSTPERCENT, "0");
                            utgstAmount = utgstAmount + ", " + jObj.optString(CustomDesignerConstants.UTGSTAMOUNT, "0");
                            igstPercent = igstPercent + ", " + jObj.optString(CustomDesignerConstants.IGSTPERCENT, "0");
                            igstAmount = igstAmount + ", " + jObj.optString(CustomDesignerConstants.IGSTAMOUNT, "0");
                            cessPercent = cessPercent + ", " + jObj.optString(CustomDesignerConstants.CESSPERCENT, "0");
                            cessAmount = cessAmount + ", " + jObj.optString(CustomDesignerConstants.CESSAMOUNT, "0");
                            if (!StringUtil.isNullOrEmpty(GSTExchangeRate)) {
                                GSTExchangeRate = GSTExchangeRate + "," + jObj.optString(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, "");
                            } else {
                                GSTExchangeRate = jObj.optString(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, "");
                            }
                        }
                        totaltaxamount += jObj.optDouble(CustomDesignerConstants.RPMP_TaxAmount, 0.0);
                    }
                }
                
                String gstAmountInWords = EnglishNumberToWordsOjb.convert(Double.parseDouble(obj.optString("totaltaxamount", "0.00")), receipt.getCurrency(),countryLanguageId) + Constants.ONLY;
                
                /*
                 * Get amount in indonesian words.
                 */
                String indonesianAmountInWords = "";
                if (countryid == Constants.INDONESIAN_COUNTRY_ID) {
                    KWLCurrency indoCurrency = (KWLCurrency) kwlCommonTablesDAO.getClassObject(KWLCurrency.class.getName(), Constants.CountryIndonesianCurrencyId);
                    indonesianAmountInWords = IndonesianNumberToWordsOjb.indonesiaConvert(Double.parseDouble(obj.optString("amount", "0.00")), indoCurrency);
                }
                /*
                 * All Global Section Custom Field and DImensions
                 */
                HashMap<String, Object> returnvalues = new HashMap<String, Object>();
                HashMap<String, Object> extraparams = new HashMap<String, Object>();
                DateFormat df = authHandler.getUserDateFormatterJson(paramJobj);//User Date Formatter
                extraparams.put(Constants.companyid, companyid);
                extraparams.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                extraparams.put(Constants.customcolumn, 0);
                extraparams.put(Constants.customfield, 1);
                extraparams.put(CustomDesignerConstants.isCustomfield, "true");
                extraparams.put("billid", paymentId);
                returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
                if (returnvalues.containsKey("returnValue")) {
                    globallevelcustomfields = (String) returnvalues.get("returnValue");
                }
                if (returnvalues.containsKey("summaryData")) {
                    summaryData = (JSONObject) returnvalues.get("summaryData");
                }
                returnvalues.clear();
                //global level dimensionfields
                extraparams.put(Constants.customcolumn, 0);
                extraparams.put(Constants.customfield, 0);
                extraparams.put(CustomDesignerConstants.isCustomfield, "false");
                returnvalues = ExportRecordHandler.returnGlobalLevelCustomFieldDimensionValues(df, summaryData, accAccountDAOobj, accountingHandlerDAOobj, extraparams);
                if (returnvalues.containsKey("returnValue")) {
                    globalleveldimensions = (String) returnvalues.get("returnValue");
                }
                if (returnvalues.containsKey("summaryData")) {
                    summaryData = (JSONObject) returnvalues.get("summaryData");
                }        
                //Details like company details,base currency
                CommonFunctions.getCommonFieldsForAllModulesSummaryData(summaryData, extraparams, accountingHandlerDAOobj);
                
                summaryData.put("summarydata", true);
                subtotal=Double.parseDouble(obj.optString("amount", "0.00"))-(Double.parseDouble(obj.optString("totaltaxamount", "0.00")))-(Double.parseDouble(obj.optString("bankInterest", "0.00")))-(Double.parseDouble(obj.optString("bankCharges ", "0.00")));
                totalAmountWithBanckChanges = Double.parseDouble(obj.optString("amount", "0.00")) - bankcharges;
                amountpaid=subtotal+(Double.parseDouble(obj.optString("totaltaxamount", "")));
                summaryData.put(CustomDesignerConstants.CustomDesignSubTotal_fieldTypeId, authHandler.formattedAmount(subtotal, companyid));
                summaryData.put(CustomDesignerConstants.Include_GST, obj.optString("totaltaxamount", "0.00"));
                summaryData.put(CustomDesignerConstants.CustomDesignAmountPaid_fieldTypeId,authHandler.formattedAmount(amountpaid, companyid));
                summaryData.put(CustomDesignerConstants.CustomDesignTotalTax_fieldTypeId, authHandler.formattedAmount(totaltaxamount, companyid));
                summaryData.put(CustomDesignerConstants.CustomDesignTotalAmount_fieldTypeId, obj.optString("amount", "0.00"));
                summaryData.put(CustomDesignerConstants.CustomDesignOriginalAmountDueTotal, authHandler.formattedAmount(originalamountduetotal, companyid));    //ERP-19271
                summaryData.put(CustomDesignerConstants.CustomDesignAmountinwords_fieldTypeId, obj.optString("amountInWords", ""));
                summaryData.put(CustomDesignerConstants.CustomDesign_Amount_in_words_Bahasa_Indonesia, indonesianAmountInWords);
                summaryData.put(CustomDesignerConstants.Createdby, createdby);
                summaryData.put(CustomDesignerConstants.Updatedby, updatedby);
                summaryData.put(CustomDesignerConstants.TOTALAMOUNT_WITHBANCKCHARGE, authHandler.formattedAmount(totalAmountWithBanckChanges, companyid));

                summaryData.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, config == null ? "" : config.getPdfPostText());
                summaryData.put(CustomDesignerConstants.RPMP_CustomerVendorName, obj.optString("personname", ""));
                summaryData.put(CustomDesignerConstants.CustomerVendor_AccountCode, obj.optString("personcode", ""));
                summaryData.put(CustomDesignerConstants.CustomerVendor_AccCode, obj.optString("accountcode", ""));
                summaryData.put(CustomDesignerConstants.CustomerVendor_AccName, obj.optString("accountname", ""));
                summaryData.put(CustomDesignerConstants.CustomerVendor_Term, receipt.getCustomer() != null ? (receipt.getCustomer().getCreditTerm() != null ? receipt.getCustomer().getCreditTerm().getTermdays() : "") : "");
                summaryData.put(CustomDesignerConstants.CustomerVendor_Total_Address, obj.optString("personaddress", ""));
                summaryData.put(CustomDesignerConstants.CustomDesignNetCreditTerm_fieldTypeId, netCreditDebitTerm);
                
                String userId = paramJobj.optString(Constants.useridKey);//Current user Details
                KwlReturnObject userresult = accountingHandlerDAOobj.getObject(User.class.getName(), userId);
                User user = (User) userresult.getEntityList().get(0);
                summaryData.put(CustomDesignerConstants.CurrentUserFirstName, !StringUtil.isNullOrEmpty(user.getFirstName()) ? user.getFirstName() : "");
                summaryData.put(CustomDesignerConstants.CurrentUserLastName, !StringUtil.isNullOrEmpty(user.getLastName()) ? user.getLastName() : "");
                summaryData.put(CustomDesignerConstants.CurrentUserFullName, !StringUtil.isNullOrEmpty(user.getFullName()) ? user.getFullName() : "");
                summaryData.put(CustomDesignerConstants.CurrentUserEmail, !StringUtil.isNullOrEmpty(user.getEmailID()) ? user.getEmailID() : "");
                summaryData.put(CustomDesignerConstants.CurrentUserAddress, !StringUtil.isNullOrEmpty(user.getAddress()) ? user.getAddress().replaceAll("\n", "<br>") : "");
                summaryData.put(CustomDesignerConstants.CurrentUserContactNumber, !StringUtil.isNullOrEmpty(user.getContactNumber()) ? user.getContactNumber() : "");
                
                if (paymentwindowtype == Constants.Receive_Payment_from_Customer) {
                    HashMap<String, Object> addrRequestParams = new HashMap<String, Object>();
                    addrRequestParams.put("customerid", receipt.getCustomer().getID());
                    addrRequestParams.put("companyid", companyid);
                    KwlReturnObject addressResult = accountingHandlerDAOobj.getCustomerAddressDetails(addrRequestParams);
                    List<AddressDetails> addressResultList = addressResult.getEntityList();
                    CommonFunctions.getAddressSummaryData(addressResultList, summaryData, companyAccountPreferences, extraCompanyPreferences);
                } else if(paymentwindowtype == Constants.Receive_Payment_from_Vendor){
                    /**
                     * Get Vendor and Company address details when Make Payment to Vendor. ERP-38139
                     */
                    HashMap<String, Object> addrRequestParams = new HashMap();
                    addrRequestParams.put("vendorid", receipt.getVendor());
                    addrRequestParams.put("companyid", companyid);
                    KwlReturnObject addressResult = accountingHandlerDAOobj.getVendorAddressDetails(addrRequestParams);
                    List<AddressDetails> addressResultList = addressResult.getEntityList();
                    CommonFunctions.getAddressSummaryData(addressResultList, summaryData, companyAccountPreferences, extraCompanyPreferences);
                } else if (paymentwindowtype == Constants.Receive_Payment_against_GL_Code) {
                    /**
                     *Get Company address details when Make Payment to GL. ERP-38139
                     */
                    HashMap<String, Object> addrRequestParams = new HashMap();
                    addrRequestParams.put("companyid", companyid);
                    addrRequestParams.put("isDefaultAddress", true);
                    KwlReturnObject addressResult = accountingHandlerDAOobj.getCompanyAddressDetails(addrRequestParams);
                    List<AddressDetails> addressResultList = addressResult.getEntityList();
                    CommonFunctions.getAddressSummaryData(addressResultList, summaryData, companyAccountPreferences, extraCompanyPreferences);
                } else {
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingAddress_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingAddress_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCity_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingState_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingCountry_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPostalCode_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingContactPerson_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingPhoneNo_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorBillingFaxNo_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCity_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingState_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingCountry_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPostalCode_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingPhoneNo_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCustomerVendorShippingFaxNo_fieldTypeId, "");
                    summaryData.put(CustomDesignerConstants.CustomDesignCompAccPrefBillAddress_fieldTypeId, AccountingAddressManager.getCompanyDefaultBillingAddress(companyid, accountingHandlerDAOobj));
                    summaryData.put(CustomDesignerConstants.CustomDesignCompAccPrefShipAddress_fieldTypeId, AccountingAddressManager.getCompanyDefaultShippingAddress(companyid, accountingHandlerDAOobj));
                    summaryData.put(CustomDesignerConstants.RemitPaymentTo, !StringUtil.isNullOrEmpty(extraCompanyPreferences.getRemitpaymentto()) ? extraCompanyPreferences.getRemitpaymentto().replaceAll("<br>", "!##") : "");
                }
                summaryData.put(CustomDesignerConstants.CustomDesignCompanyPostText_fieldTypeId, config == null ? "" : config.getPdfPostText());
                //summaryData.put(CustomDesignerConstants.RPMP_CustomerVendorName, accname);
                //Invoice comma separated values-Against Customer Invoice
                summaryData.put(CustomDesignerConstants.RPMP_CustomerVendorInvoiceNo, invoiceNos);
                summaryData.put(CustomDesignerConstants.RPMP_InvoiceDate, invoicedates);
                summaryData.put(CustomDesignerConstants.RPMP_DueDate, invduedates);
                summaryData.put(CustomDesignerConstants.RPMP_TAXNAME, invtax);
                summaryData.put(CustomDesignerConstants.RPMP_Tax, invtaxpercent);
                summaryData.put(CustomDesignerConstants.RPMP_Discount, invdiscount);
                summaryData.put(CustomDesignerConstants.RPMP_OriginalAmount, invoriginalamount);
                summaryData.put(CustomDesignerConstants.RPMP_OriginalAmountDue, invoriginalamountdue);
                summaryData.put(CustomDesignerConstants.RPMP_ExchangeRate, invexchagerates);
                summaryData.put(CustomDesignerConstants.RPMP_AmountDue, invamountdue);
                summaryData.put(CustomDesignerConstants.RPMP_EnterPayment, inventerpayment);
                summaryData.put(CustomDesignerConstants.RPMP_DocumentType, invdoctype);
                summaryData.put(CustomDesignerConstants.RPMP_DocumentNumber, invdocnumber);
                summaryData.put(CustomDesignerConstants.Common_EnterPayment, commonEnterPayment);
                summaryData.put(CustomDesignerConstants.RPMP_Description, invdesc);
                summaryData.put(CustomDesignerConstants.ProductName, productName);
                summaryData.put("Custom_HSN/SAC Code", HsnSacCode);
                summaryData.put(CustomDesignerConstants.Common_Invoiceno_Accname_creditdebitno_advance, commonAppendtext);
                summaryData.put(CustomDesignerConstants.RPMP_TaxAmount, taxamount);
                String customcurrencysymbol = accCommonTablesDAO.getCustomCurrencySymbol(currencysymbol, companyid);//Take custom currency symbol
                summaryData.put(CustomDesignerConstants.SrNo, 1);
                summaryData.put(CustomDesignerConstants.CustomDesignSORefNumber_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignQuoteRefNumber_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignDORefNumber_fieldTypeId, "");
                summaryData.put(CustomDesignerConstants.CustomDesignCurrencySymbol_fieldTypeId,customcurrencysymbol);
                summaryData.put(CustomDesignerConstants.AllGloballevelCustomfields, globallevelcustomfields);
                summaryData.put(CustomDesignerConstants.AllGloballevelDimensions, globalleveldimensions);
                summaryData.put(CustomDesignerConstants.AllDimensions, "");
                summaryData.put(CustomDesignerConstants.AllLinelevelCustomFields, "");
                summaryData.put(CustomDesignerConstants.RPMP_CreditAmount, creditAmount);
                summaryData.put(CustomDesignerConstants.RPMP_DebitAmount, debitAmount);
                summaryData.put(CustomDesignerConstants.RPMP_ACCOUNT_CODE, accountCode);
                summaryData.put(CustomDesignerConstants.CUSTOMER_OR_VENDOR_TITLE, customerOrVendorTitle);
                summaryData.put(CustomDesignerConstants.CURRENCY_EXCHANGE_RATE, exchangerate);
                summaryData.put(CustomDesignerConstants.CustomerVendor_VAT_TIN_NO,VATTInnumber);
                summaryData.put(CustomDesignerConstants.CustomerVendor_CST_TIN_NO, CSTTInNumber);
                summaryData.put(CustomDesignerConstants.Company_VAT_TIN_NO, extraCompanyPreferences.getVatNumber()!= null ? extraCompanyPreferences.getVatNumber() : "");
                summaryData.put(CustomDesignerConstants.Company_CST_TIN_NO, extraCompanyPreferences.getCstNumber()!= null ? extraCompanyPreferences.getCstNumber() : "");
                summaryData.put(CustomDesignerConstants.CUSTOMER_VENDOR_GSTIN_NUMBER, gstin);
                //Put tax related fields in summaryData for printing as global fields
                summaryData.put(CustomDesignerConstants.CGSTPERCENT, cgstPercent);
                summaryData.put(CustomDesignerConstants.CGSTAMOUNT, cgstAmount);
                summaryData.put(CustomDesignerConstants.SGSTPERCENT, sgstPercent);
                summaryData.put(CustomDesignerConstants.SGSTAMOUNT, sgstAmount);
                summaryData.put(CustomDesignerConstants.UTGSTPERCENT, utgstPercent);
                summaryData.put(CustomDesignerConstants.UTGSTAMOUNT, utgstAmount);
                summaryData.put(CustomDesignerConstants.IGSTPERCENT, igstPercent);
                summaryData.put(CustomDesignerConstants.IGSTAMOUNT, igstAmount);
                summaryData.put(CustomDesignerConstants.CESSPERCENT, cessPercent);
                summaryData.put(CustomDesignerConstants.CESSAMOUNT, cessAmount);
                summaryData.put(CustomDesignerConstants.GSTAmountInWords, gstAmountInWords);
                summaryData.put(CustomDesignerConstants.RPMP_InvoiceTotalDiscount, invoiceTotalDiscount);
                summaryData.put(CustomDesignerConstants.CustomDesignDiscount_fieldTypeId, discountAmount);
                summaryData.put(CustomDesignerConstants.DOCUMENTSTATUS, documentStatus);
                summaryData.put(CustomDesignerConstants.CustomDesignExchangeRate_fieldTypeId, exchangerate);
                //GST Exchange Rate
                if (companyAccountPreferences.getCompany().getCountry().getID().equalsIgnoreCase(Constants.SINGAPOREID) && !gcurrencyid.equalsIgnoreCase(Constants.SGDID)) {
                    summaryData.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, !StringUtil.isNullOrEmpty(GSTExchangeRate) ? GSTExchangeRate : "");
                } else {
                    summaryData.put(CustomDesignerConstants.CustomDesignGSTExchangeRate_fieldTypeId, "");
                }
                
                jArr.put(summaryData);
            }
            

         //getting all the custom fields at line level
            result = customDesignDAOObj.getCustomLineFields(companyid, moduleid);
            list = result.getEntityList();
            for (int cnt = 0; cnt < list.size(); cnt++) {
                HashMap<String, String> map = new HashMap<String, String>();
                Object[] rowcustom = (Object[]) list.get(cnt);
                map.put("Custom_" + rowcustom[2], "{label:'" + rowcustom[2] + "',xtype:'" + rowcustom[1].toString() + "'}");
                dbCustomJSONArr.put(map);
            }
            jArr.put(dbCustomJSONArr);
            jArr.put(detailsTableData);
            
        } catch (Exception ex) {
            Logger.getLogger(accVendorPaymentControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }

        return jArr;
     }
     
   @Override  
     public KwlReturnObject getReceiptList(JSONObject paramJobj){
        List<JSONObject> list = new ArrayList<JSONObject>();
        List<JSONObject> tempList = new ArrayList<JSONObject>();
        String msg = "";
        int count = 0;
        try {
            HashMap<String, Object> requestParams = getReceiptRequestMapJSON(paramJobj);
            boolean contraentryflag = paramJobj.optString("contraentryflag",null) != null;
            boolean isAdvancePayment = paramJobj.optString("advancePayment",null) != null;
            boolean isAdvanceFromVendor = paramJobj.optString("advanceFromVendor",null) != null;
            boolean isPostDatedCheque = paramJobj.optString("isPostDatedCheque",null) != null;
            boolean isDishonouredCheque = paramJobj.optString("isDishonouredCheque",null) != null;
          
            boolean allAdvPayment = paramJobj.optString("allAdvPayment",null) != null;
            boolean unUtilizedAdvPayment = paramJobj.optString("unUtilizedAdvPayment",null) != null;
            boolean partiallyUtilizedAdvPayment = paramJobj.optString("partiallyUtilizedAdvPayment",null) != null;
            boolean fullyUtilizedAdvPayment = paramJobj.optString("fullyUtilizedAdvPayment",null) != null;
            boolean nonorpartiallyUtilizedAdvPayment = paramJobj.optString("nonorpartiallyUtilizedAdvPayment",null) != null;
            
            boolean isGlcode = paramJobj.optString("isGlcode",null) != null;
            String billid = paramJobj.optString("billid",null) != null ? paramJobj.optString("billid") : "";
            boolean onlyOpeningBalanceTransactionsFlag = false;
            boolean hasApprovalAuthority = paramJobj.optString("hasApprovalAuthority",null) != null;
           /*   
            
            get customer id by fetching custVendorID in paramJobj
            
            */
            String Customerid = "";
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("custVendorID"))) {
                Customerid = (String) paramJobj.optString("custVendorID");
            }
              /*
             When check(Drop Down) to include child accounts is disabled then includeExcludeChildCombobox flag will be set as TRUE to include child accounts
             
              includeExcludeChildCombobox, if All = Include all child accounts while fetching parent account data
              includeExcludeChildCombobox, if TRUE = Include all child accounts while fetching parent account data
              includeExcludeChildCombobox, if FALSE = Exclude child acounts while fetching parent account data
             
           
            /*
             * NULL pointer exception Caused due to
               paramJobj.optString("includeExcludeChildCmb").toString().equals("All")
             
             * To handle NULL pointer exception (paramJobj.optString("includeExcludeChildCmb") != null)
             
             */
            boolean includeExcludeChildCmb;
     
            if (paramJobj.optString("includeExcludeChildCmb") != null && paramJobj.optString("includeExcludeChildCmb").toString().equals("All")) {
                includeExcludeChildCmb = true;
            } else {
                includeExcludeChildCmb = paramJobj.optString("includeExcludeChildCmb") != null ? Boolean.parseBoolean(paramJobj.optString("includeExcludeChildCmb")) : false;
            }
            
            /*
             
             fetch payment report value or set defalut value false
             
             */
             
            boolean isPaymentReport = (paramJobj.optString("isPaymentReport") != null) ? Boolean.parseBoolean(paramJobj.optString("isPaymentReport").toString()) : false;
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("onlyOpeningBalanceTransactionsFlag",null))) {
                onlyOpeningBalanceTransactionsFlag = Boolean.parseBoolean(paramJobj.optString("onlyOpeningBalanceTransactionsFlag"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("recordType",null))) {
                requestParams.put("receipttype", paramJobj.optString("recordType"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("paymentWindowType",null))) {
                requestParams.put("paymentWindowType", Integer.parseInt(paramJobj.optString("paymentWindowType")));
            }
            
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("allAdvPayment",null))) {
                allAdvPayment = Boolean.parseBoolean(paramJobj.optString("allAdvPayment"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("unUtilizedAdvPayment",null))) {
                unUtilizedAdvPayment = Boolean.parseBoolean(paramJobj.optString("unUtilizedAdvPayment"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("partiallyUtilizedAdvPayment",null))) {
                partiallyUtilizedAdvPayment = Boolean.parseBoolean(paramJobj.optString("partiallyUtilizedAdvPayment"));
            }
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("fullyUtilizedAdvPayment",null))) {
                fullyUtilizedAdvPayment = Boolean.parseBoolean(paramJobj.optString("fullyUtilizedAdvPayment"));
            }
            if(!StringUtil.isNullOrEmpty(paramJobj.optString("linknumber",null))){
                requestParams.put("linknumber", paramJobj.optString("linknumber"));
            }
            if (paramJobj.optString("dir") != null && !StringUtil.isNullOrEmpty(paramJobj.optString("dir"))
                    && paramJobj.optString("sort") != null && !StringUtil.isNullOrEmpty(paramJobj.optString("sort"))) {
                requestParams.put("dir", paramJobj.optString("dir"));
                requestParams.put("sort", paramJobj.optString("sort"));
            }
            
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("ispendingAproval"))) {
                requestParams.put("ispendingAproval", Boolean.FALSE.parseBoolean(paramJobj.optString("ispendingAproval")));
            }            
            
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("nonorpartiallyUtilizedAdvPayment",null))) {
                nonorpartiallyUtilizedAdvPayment = Boolean.parseBoolean(paramJobj.optString("nonorpartiallyUtilizedAdvPayment"));
            }
            requestParams.put("allAdvPayment", allAdvPayment);
            requestParams.put("unUtilizedAdvPayment", unUtilizedAdvPayment);
            requestParams.put("partiallyUtilizedAdvPayment", partiallyUtilizedAdvPayment);
            requestParams.put("fullyUtilizedAdvPayment", fullyUtilizedAdvPayment);
            requestParams.put("nonorpartiallyUtilizedAdvPayment", nonorpartiallyUtilizedAdvPayment);
            
            requestParams.put("contraentryflag", contraentryflag);
            requestParams.put("isadvancepayment", isAdvancePayment);
            requestParams.put("isadvancefromvendor", isAdvanceFromVendor);
            requestParams.put("isPostDatedCheque", isPostDatedCheque);
            requestParams.put("isDishonouredCheque", isDishonouredCheque);
            requestParams.put("isGlcode", isGlcode);
            requestParams.put("billid", billid);
            requestParams.put("hasApprovalAuthority", hasApprovalAuthority);
           
            /*
             put includeExcludeChildCmb value, ispaymenreport value,custVendorID value in request param
             
             */
            requestParams.put("includeExcludeChildCmb",includeExcludeChildCmb);
            requestParams.put("isPaymentReport",isPaymentReport);
            requestParams.put("custVendorID",Customerid);
            KwlReturnObject userResult = accountingHandlerDAOobj.getObject(User.class.getName(), paramJobj.getString(Constants.useridKey));
            User user = (User) userResult.getEntityList().get(0);
            if (!StringUtil.isNullOrEmpty(user.getDepartment())) {
                requestParams.put("userDepartment", user.getDepartment());
            }
            boolean consolidateFlag = paramJobj.optString("consolidateFlag",null) != null ? Boolean.parseBoolean(paramJobj.optString("consolidateFlag")) : false;
            String[] companyids = (consolidateFlag && paramJobj.optString("companyids",null) != null) ? paramJobj.optString("companyids").split(",") : paramJobj.optString(Constants.companyKey).split(",");
            String gcurrencyid = (consolidateFlag && paramJobj.optString("gcurrencyid",null) != null) ? paramJobj.optString("gcurrencyid") :  paramJobj.optString(Constants.globalCurrencyKey);
            requestParams.put(Constants.onlydateformat, authHandler.getDateOnlyFormat());
            KwlReturnObject result = null;
            KwlReturnObject openingBalanceReceiptsResult = null;
            String companyid = "";
            for (int cnt = 0; cnt < companyids.length; cnt++) {
                companyid = companyids[cnt];
                paramJobj.put("companyid", companyid);
                paramJobj.put("gcurrencyid", gcurrencyid);
                requestParams.put("companyid", companyid);
                requestParams.put("gcurrencyid", gcurrencyid);

                tempList.clear();
                if (onlyOpeningBalanceTransactionsFlag) {
                    // getting opening balance receipts
                    openingBalanceReceiptsResult = accReceiptDAOobj.getAllOpeningBalanceReceipts(requestParams);
                    count=openingBalanceReceiptsResult.getRecordTotalCount();
                    tempList = getOpeningBalanceReceiptJsonForReport(paramJobj, openingBalanceReceiptsResult.getEntityList(), tempList);
                } else {
                    result = accReceiptDAOobj.getReceipts(requestParams);
                    count = result.getRecordTotalCount();
                    tempList = getReceiptJson(paramJobj, result.getEntityList(), tempList);
                }
                list.addAll(tempList);
            }
        } catch (Exception ex) {
            msg = ex.getMessage();
            Logger.getLogger(accReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return new KwlReturnObject(true, null, null, list, count);
    }   
     
    @Override
    public void getAdvanceReceiptCustomData(HashMap<String, Object> requestParams, ReceiptAdvanceDetail advanceDetail, JSONObject obj) throws ServiceException {
        try {
            String companyid = (String) requestParams.get(Constants.companyKey);
            HashMap<String, Object> fieldrequestParams = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<>();
            HashMap<String, String> customDateFieldMap = new HashMap<>();
            Map<String, Object> variableMap = new HashMap<>();

            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, Constants.Acc_Receive_Payment_ModuleId, 1));
            HashMap<String, String> replaceFieldMap = new HashMap<>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMapForRows(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);

            HashMap<String, Object> invDetailRequestParams = new HashMap<>();
            ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
            Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
            Detailfilter_params.add(advanceDetail.getId());
            invDetailRequestParams.put(Constants.filter_names, Detailfilter_names);
            invDetailRequestParams.put(Constants.filter_params, Detailfilter_params);

            KwlReturnObject custumObjresult = accVendorPaymentDAO.getVendorPaymentCustomData(invDetailRequestParams);
            if (custumObjresult.getEntityList().size() > 0) {
                AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) custumObjresult.getEntityList().get(0);
                AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                if (jeDetailCustom != null) {
                    JSONObject params = new JSONObject();
                    params.put(Constants.companyKey, companyid);
                    params.put(Constants.isLink, true);
                    if (requestParams.containsKey(Constants.requestModuleId) && requestParams.get(Constants.requestModuleId) != null) {
                        params.put(Constants.linkModuleId, requestParams.get(Constants.requestModuleId));
                    }
                    fieldDataManagercntrl.getLineLevelCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccReceiptServiceImpl.getAdvanceReceiptCustomData : " + ex.getMessage(), ex);
        }
    }
    
    
    public JSONArray getSalesReceiptKnockOffJSON(Map<String, Object> invoiceRequestParams) throws ServiceException, JSONException{
        JSONArray allTransaction = new JSONArray();
        String companyid = (String) invoiceRequestParams.get(Constants.companyKey);
        List list = accReceiptDAOobj.getSalesReceiptKnockOffTransactions(invoiceRequestParams);
        boolean onlyAmountDue = invoiceRequestParams.containsKey("onlyAmountDue") ? (Boolean) invoiceRequestParams.get("onlyAmountDue") : false;
        for (int i = 0; i < list.size(); i++) {
            Object[] details = (Object[]) list.get(i);
            if(details[3]!=null && (Double)details[3] == 0){
                continue;
            }
            JSONObject obj = new JSONObject();
            double amountdueinbase = (Double) details[3] - (Double) details[5];
            amountdueinbase = authHandler.round(amountdueinbase, companyid);
            double amountdue = (Double) details[2] - (Double) details[4];
            amountdue = authHandler.round(amountdue, companyid);
            obj.put(InvoiceConstants.amountdueinbase, amountdueinbase);
            obj.put("amountdue", authHandler.round(amountdue, companyid));
            obj.put(Constants.billid, details[0]);
            obj.put("isOpeningBalanceTransaction", false);
            obj.put("creationdate", details[8]);
            obj.put(InvoiceConstants.personid, details[16]);
            obj.put("type", Constants.PAYMENT_RECEIVED);
            if(!onlyAmountDue){
                obj.put(Constants.companyKey, companyid);
                obj.put("companyname", details[29]);
                obj.put("customername", details[17]);
                obj.put("customercode", details[19]);
                obj.put(InvoiceConstants.CustomerCreditTerm, details[20]);
                obj.put(InvoiceConstants.aliasname, details[18]);
                obj.put(InvoiceConstants.billno, details[1]);
                obj.put(Constants.currencyKey, details[25]);
                obj.put(InvoiceConstants.currencysymbol, details[27]);
                obj.put(InvoiceConstants.currencyname, details[26]);
                double externalCurrencyRate =  details[24] == null ? 1 : Double.parseDouble( details[24].toString());
                obj.put("externalcurrencyrate",externalCurrencyRate);
                String baseCurrencySymbol = (String)details[31];
                String exchangeRate = "1 "+baseCurrencySymbol+" = "+externalCurrencyRate+" "+obj.getString(InvoiceConstants.currencysymbol);
                obj.put("exchangerate", exchangeRate);
                obj.put("entrydate", details[14]);
                obj.put(Constants.shipdate, details[30]);
                obj.put(Constants.duedate, details[9]);
                obj.put(InvoiceConstants.personname, details[17]);
                obj.put("entryno", details[13]);
                obj.put("salespersonname", details[10]);
                obj.put("memo", details[23]);
                obj.put("salespersoncode", details[11]);
                obj.put("salespersonid", details[12]);
                obj.put("amountduenonnegative", (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));//XX
                obj.put("amount", authHandler.round((Double) details[2], companyid));   //actual invoice amount
                obj.put("creditlimit", details[22]);
                obj.put("creditlimitinbase", details[22]);
            }
            allTransaction.put(obj);
        }
        return allTransaction;
    }
    public JSONArray getOpeningSalesReceiptKnockOffJSON(Map<String, Object> invoiceRequestParams) throws ServiceException, JSONException{
        JSONArray allTransaction = new JSONArray();
        String companyid = (String) invoiceRequestParams.get(Constants.companyKey);
        boolean onlyAmountDue = invoiceRequestParams.containsKey("onlyAmountDue") ? (Boolean) invoiceRequestParams.get("onlyAmountDue") : false;
        List list = accReceiptDAOobj.getOpeningSalesReceiptKnockOffTransactions(invoiceRequestParams);
        for (int i = 0; i < list.size(); i++) {
            Object[] details = (Object[]) list.get(i);
            if(details[3]!=null && (Double)details[3] == 0){
                continue;
            }
            JSONObject obj = new JSONObject();
            double amountdueinbase = (Double) details[3] - (Double) details[5];
            amountdueinbase = authHandler.round(amountdueinbase, companyid);
            double amountdue = (Double) details[2] - (Double) details[4];
            amountdue = authHandler.round(amountdue, companyid);
            obj.put(InvoiceConstants.amountdueinbase, amountdueinbase);
            obj.put("amountdue", authHandler.round(amountdue, companyid));
            obj.put(Constants.billid, details[0]);
            obj.put("isOpeningBalanceTransaction", true);
            obj.put("creationdate", details[8]);
            obj.put(InvoiceConstants.personid, details[16]);
            obj.put("type", Constants.PAYMENT_RECEIVED);
            if(!onlyAmountDue){
                obj.put(Constants.companyKey, companyid);
                obj.put("companyname", details[29]);
                obj.put("customername", details[17]);
                obj.put("customercode", details[19]);
                obj.put(InvoiceConstants.CustomerCreditTerm, details[20]);
                obj.put(InvoiceConstants.aliasname, details[18]);
                obj.put(InvoiceConstants.billno, details[1]);
                obj.put(Constants.currencyKey, details[25]);
                obj.put(InvoiceConstants.currencysymbol, details[27]);
                obj.put(InvoiceConstants.currencyname, details[26]);
                double externalCurrencyRate =  details[24] == null ? 1 : Double.parseDouble( details[24].toString());
                obj.put("externalcurrencyrate",externalCurrencyRate);
                String baseCurrencySymbol = (String)details[31];
                String exchangeRate = "1 "+baseCurrencySymbol+" = "+externalCurrencyRate+" "+obj.getString(InvoiceConstants.currencysymbol);
                obj.put("exchangerate", exchangeRate);
                obj.put("entrydate", details[14]);
                obj.put(Constants.shipdate, details[30]);
                obj.put(Constants.duedate, details[9]);
                obj.put(InvoiceConstants.personname, details[17]);
                obj.put("entryno", details[13]);
                obj.put("salespersonname", details[10]);
                obj.put("memo", details[23]);
                obj.put("salespersoncode", details[11]);
                obj.put("salespersonid", details[12]);
                obj.put("amountduenonnegative", (amountdue <= 0) ? 0 : authHandler.round(amountdue, companyid));//XX
                obj.put("amount", authHandler.round((Double) details[2], companyid));   //actual invoice amount
                obj.put("creditlimitinbase", details[22]);
            }
            allTransaction.put(obj);
        }
        return allTransaction;
    }
    public JSONArray getAllSalesReceiptKnockOffJSON(Map<String, Object> invoiceRequestParams) throws ServiceException, JSONException{
        JSONArray allTransaction = null;
        allTransaction = getSalesReceiptKnockOffJSON(invoiceRequestParams);
        JSONArray arr1 = getOpeningSalesReceiptKnockOffJSON(invoiceRequestParams);
        if (arr1 != null && arr1.length() > 0) {
            for (int i = 0; i < arr1.length(); i++) {
                allTransaction.put(arr1.getJSONObject(i));
            }
        }
        return allTransaction;
    }
}
