/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.reports;

import com.krawler.common.admin.Company;
import com.krawler.common.admin.ExtraCompanyPreferences;
import com.krawler.common.admin.KWLCurrency;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.*;
import com.krawler.spring.accounting.handler.AccountingHandlerDAO;
import com.krawler.spring.accounting.jasperreports.GeneralLedger;
import com.krawler.spring.accounting.product.PriceValuationStack;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang.mutable.MutableDouble;
import org.springframework.context.MessageSource;

/**
 *
 * @author krawler
 */
public interface AccReportsService {
     public void setMessageSource(MessageSource msg);
     public JSONObject getBalanceSheet(HttpServletRequest request) throws ServiceException, SessionExpiredException;
     
     public double[] getOpeningBalancesWithDate(JSONObject paramJObj, String companyid, Date startDate, Date endDate) throws ServiceException, SessionExpiredException,JSONException;
     
     public JSONObject getInventoryOpeningBalance(HttpServletRequest request, String companyid, Date stDate, Map<String, Object[]> companyMaxDateProductPriceList, Date inventoryOpeningBalanceDate);
     public JSONObject getInventoryOpeningBalance(HttpServletRequest request, String companyid, Date stDate);
     
     public double getSerialNoValuation(HttpServletRequest request,double onhand,String productTypeID,double avgcost, Product product, Calendar startcal, Calendar endcal) throws ServiceException;
     public double getSerialNoInventoryValuation(HttpServletRequest request,double onhand,String productTypeID,double avgcost, Product product, Calendar startcal, Calendar endcal) throws ServiceException;
     public double getAvarageValuation(HttpServletRequest request, double onhand, String productTypeID, double avgcost, Product product, Calendar startcal, Calendar endcal) throws ServiceException,SessionExpiredException, ParseException;
     public double getFIFO(HttpServletRequest request, String productid, Date endDate, double onhand, boolean isLifo,String productTypeID) throws ServiceException;
     public List getClosingStockVal(String productid, HashMap<String, Object> requestParams,Date stDate, Date endDate) throws ServiceException, ParseException;
     public double[] getBalanceSheet(HttpServletRequest request, int nature, JSONArray jArr, Map<String, Object> advSearchAttributes) throws ServiceException, SessionExpiredException,JSONException;
    
     public double[] formatGroupDetails(JSONObject paramJObj, String companyid, Group group, Date startDate, Date endDate, int level, boolean isBalanceSheet, JSONArray jArr,Date startPreDate,Date endPreDate, Map<String, Object> advSearchAttributes) throws ServiceException, SessionExpiredException, ParseException,JSONException;
     public double[] formatAccountDetails(JSONObject paramJobj, Account account, Date startDate, Date endDate, int level, boolean isDebit, boolean isBalanceSheet, JSONArray jArr, DateFormat sdf,Date startPreDate,Date endPreDate,List<GeneralLedger> generalLedgerList, Map<String, Object> advSearchAttributes) throws ServiceException, SessionExpiredException, ParseException,JSONException; 
    
     public double getAccountBalance(JSONObject paramJObj, String accountid, Date startDate, Date endDate, Map<String, Object> advSearchAttributes) throws ServiceException, SessionExpiredException,JSONException;
     public double getAccountBalance(JSONObject paramJObj, HashMap<String, Object> requestParams, String accountid, Date startDate, Date endDate) throws ServiceException;
     public double getAccountClosingBalance(JSONObject paramJObj, HashMap<String, Object> requestParams, String accountid, Date startDate, Date endDate, double accountOpeningBalance, boolean isValidOpeningBalance, Map<String, Object> advSearchAttributes) throws ServiceException;
     public double getAccountBalance(JSONObject paramJObj, HashMap<String, Object> requestParams, String accountid, Date startDate, Date endDate,double accountOpeningBalance, boolean isValidOpeningBalance, Map<String, Object> advSearchAttributes) throws ServiceException ;
     public double getAccountBalanceForCashFlowStatement(JSONObject paramJObj, Account account, Date startDate, Date endDate) throws ServiceException, SessionExpiredException,JSONException;

     public double getAccountBalanceWithOutClosing(JSONObject paramJObj, String accountid, Date startDate, Date endDate, Map<String, Object> advSearchAttributes) throws ServiceException, SessionExpiredException,JSONException;
     public double[] getTrading(JSONObject paramJObj, int nature, JSONArray jArr,boolean isProfitLoss, Map<String, Object> advSearchAttributes) throws ServiceException, SessionExpiredException,JSONException;
     public double[] getProfitLoss(JSONObject paramJObj, int nature, JSONArray jArr,boolean isProfitLoss, Map<String, Object> advSearchAttributes) throws ServiceException, SessionExpiredException,JSONException;
     
     public HashMap<String,Date> getStartAndEndFinancialDate(CompanyAccountPreferences companyAccountPreferences,int year) throws ServiceException, SessionExpiredException;
     public double getAccountBalanceMerged(HttpServletRequest request, String accountid, Date startDate, Date endDate, boolean eliminateflag) throws ServiceException, SessionExpiredException;
     public double getAccountBalanceMerged(HttpServletRequest request, HashMap<String, Object> requestParams, String accountid, Date startDate, Date endDate, boolean eliminateflag) throws ServiceException ;
     public double getAccountClosingBalanceMerged(HttpServletRequest request, HashMap<String, Object> requestParams, String accountid, Date startDate, Date endDate, boolean eliminateflag) throws ServiceException;
     public double getAccountBalanceDateWiseMerged(HttpServletRequest request, Account account, Date startDate, Date endDate,boolean  ytdFlag, boolean eliminateflag,HashMap<String,Double> openPeriodAmounts) throws ServiceException, SessionExpiredException;
     public double getAccountClosingBalanceDateWiseMerged(HttpServletRequest request, Account account, Date startDate, Date endDate,boolean  ytdFlag, boolean eliminateflag,HashMap<String,Double> openPeriodAmounts) throws ServiceException, SessionExpiredException;    
     public double getAccountClosingBalanceDateWiseMerged(JSONObject paramJobj, Account account, Date startDate, Date endDate,boolean  ytdFlag, boolean eliminateflag,HashMap<String,Double> openPeriodAmounts) throws ServiceException, SessionExpiredException;
     
     public double getOpeningBalanceBalanceSheet(HttpServletRequest request) throws JSONException,ServiceException;
     
     public void generateIBGFile(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException, AccountingException;
     public JSONObject getExportBalanceSheetJSONforStandardCSV(HttpServletRequest request,JSONObject jobj, int flag, int toggle,boolean periodView);
     public JSONObject getJournalEntryJsonMerged(HashMap<String, Object> requestParams, List list, JSONArray jArr) throws ServiceException;
     public String getInParamFromList(List<String> list);
     public Map<String, JSONArray> getJournalEntryDetailsMap(HashMap<String, Object> request) throws ServiceException;
     public Map<String, BillingGoodsReceipt> getBillingGoodsReceiptMap(HashMap<String, Object> requestParams);
     public Map<String, Object[]> getBillingCreditNoteMap(HashMap<String, Object> requestParams);
     public Map<String, Object[]> getBillingDebitNoteMap(HashMap<String, Object> requestParams);
     public Map<String, Object[]> getBillingPaymentReceivedMap(HashMap<String, Object> requestParams);
     public Map<String, Object[]> getBillingPaymentMadeMap(HashMap<String, Object> requestParams);
     public Map<String, Invoice> getInvoiceMap(HashMap<String, Object> requestParams);
     public Map<String, DeliveryOrder> getDOMap(HashMap<String, Object> requestParams);
     public Map<String, GoodsReceipt> getGoodsReceiptMap(HashMap<String, Object> requestParams);
     public Map<String, Object[]> getCreditNoteMap(HashMap<String, Object> requestParams);
     public Map<String, Object[]> getDebitNoteMap(HashMap<String, Object> requestParams);
     public Map<String, Object[]> creditNoteMapVendor(HashMap<String, Object> requestParams);
     public Map<String, Object[]> debitNoteMapCustomer(HashMap<String, Object> requestParams);
     public Map<String, Object[]> getPaymentReceivedMap(HashMap<String, Object> requestParams);
     public Map<String, Object[]> getPaymentMadeMap(HashMap<String, Object> requestParams);
     public Map<String, AssetDepreciationDetail> getAssetDepreciationMap(HashMap<String, Object> requestParams);
     public JSONArray getBankBookSummay(HttpServletRequest request, List list) ;
     
     public double getTotalAccountBalance(Account account, double totalAccountBalance,JSONObject paramJObj) throws ServiceException, ParseException,JSONException;
     public JSONObject addNetBalanceJson(HttpServletRequest request, Account account, double balance, KWLCurrency currency);
     public JSONObject addAccountGroupJson(Account account);
     public JSONObject getLedger(HttpServletRequest request) throws ServiceException, SessionExpiredException;
     
     public JSONObject getLedger(JSONObject requestJobj) throws ServiceException, SessionExpiredException;
     
     public void exportBankBook(HttpServletRequest request, HttpServletResponse response);
     
     public double calculateProfitAndLoss(JSONObject paramJobj, Date startDate, Date endDate, Date startPreDate, Date endPreDate, String accname1,
            boolean isPeriod, boolean isOpening, String companyid, Map<String, Object> advSearchAttributes);
     
     public JSONArray getDimensionsReportJson(HttpServletRequest request, List list) ;
     public double getAccountBalanceLedger(HashMap<String, Object> requestParams, String accountid, Date startDate, Date endDate) throws ServiceException, SessionExpiredException;
     public double getAccountBalanceWithOutClosingLedger(HashMap<String, Object> requestParams, String accountid, Date startDate, Date endDate) throws ServiceException, SessionExpiredException;
     public double getAccountBalanceLedger(HashMap<String, Object> requestParams, String accountid, Date startDate, Date endDate, double accountOpeningBalance, boolean isValidOpeningBalance) throws ServiceException;
     public double getAccountClosingBalanceLedger(HashMap<String, Object> requestParams, String accountid, Date startDate, Date endDate, double accountOpeningBalance, boolean isValidOpeningBalance) throws ServiceException;
     public double getAccountBalanceMergedLedger(HashMap<String, Object> requestParams, String accountid, Date startDate, Date endDate, boolean eliminateflag) throws ServiceException, SessionExpiredException;
     public double getAccountBalanceMergedLedgerForExport(HashMap<String, Object> requestParams, String accountid, Date startDate, Date endDate, boolean eliminateflag) throws ServiceException ;
     public double getAccountClosingBalanceMergedLedger(HashMap<String, Object> requestParams, String accountid, Date startDate, Date endDate, boolean eliminateflag) throws ServiceException;
     public double getTotalAccountBalanceLedger(Account account, double totalAccountBalance, HashMap<String, Object> requestParams) throws ServiceException, ParseException;
     
     public double getParentOpeningBalance(Account account, double totalAccountBalance, HttpServletRequest request, Date startDate, Date endDate) throws ServiceException, ParseException,JSONException;
     
     public double getParentOpeningBalanceLedger(Account account, double totalAccountBalance, HashMap<String, Object> requestParams, Date startDate, Date endDate) throws ServiceException, ParseException;
     public JSONObject getLedgerForExport(HashMap<String, Object> requestParams, String accountid, Map<String, BillingInvoice> billingInvoiceMapGL, Map<String, BillingGoodsReceipt> billingGrMapGL, Map<String, Object[]> billingCreditNoteMapGL, Map<String, Object[]> billingDebitNoteMapGL, Map<String, Object[]> billingPaymentReceivedMapGL, Map<String, Object[]> billingPaymentMadeMapGL, Map<String, Invoice> invoiceMapGL, Map<String, GoodsReceipt> grMapGL, Map<String, Object[]> creditNoteMapGL, Map<String, Object[]> creditNoteMapVendorGL, Map<String, Object[]> debitNoteMapGL, Map<String, Object[]> debitNoteMapCustomerGL, Map<String, Object[]> paymentReceivedMapGL, Map<String, Object[]> paymentMadeMapGL, Map<String, GoodsReceipt> fixedAssetgrMapGL, Map<String, Invoice> fixedAssetInvoiceMapGL, Map<String, Integer> jeDetailPaymentTypeMapGL, Map<String, Integer> jeDetailReceiptTypeMapGL) throws ServiceException, SessionExpiredException;
     public JSONObject addNetBalanceJsonLedger(HashMap<String, Object> requestParams, Account account, double balance, KWLCurrency currency);
     public KwlReturnObject getDimensionsReport(Map<String, Object> requestParams) throws ServiceException;
     public double[] calculateOpenigAndClosingStock(HttpServletRequest request, CompanyAccountPreferences pref, ExtraCompanyPreferences extrapref, Map<String, Object[]> companyMaxDateProductPriceList, Date inventoryOpeningBalanceDate, String companyid, Date startDate, Date endDate, Map<String, Object> advSearchAttributes);

     public Date getDateForExcludePreviousYearBalanceFilter(JSONObject paramJobj, Date startDate); 
    public JSONObject getLedgerDetails(JSONObject paramJobj) throws ServiceException;

    public String getSearchJsonByModule(HashMap<String, Object> requestParams) throws SessionExpiredException, UnsupportedEncodingException;
    public Map<String, Invoice> getInvoiceMapNew(HashMap<String, Object> requestParams);
    public Map<String, GoodsReceipt> getGoodsReceiptMapNew(HashMap<String, Object> requestParams);
    public void createJEDetailPaymentTypeMapNew(Payment tempp, Map<String, Integer> jeDetailPaymentTypeMap, String companyid) throws ServiceException;
    public void createJEDetailReceiptTypeMapNew(Receipt tempr, Map<String, Integer> jeDetailReceiptTypeMap, String companyid) throws ServiceException;
    public void clearMaps(Map<String, BillingInvoice> billingInvoiceMapGL, Map<String, BillingGoodsReceipt> billingGrMapGL, Map<String, Object[]> billingCreditNoteMapGL, Map<String, Object[]> billingDebitNoteMapGL, Map<String, Object[]> billingPaymentReceivedMapGL, Map<String, Object[]> billingPaymentMadeMapGL, Map<String, Invoice> invoiceMapGL, Map<String, GoodsReceipt> grMapGL, Map<String, Object[]> creditNoteMapGL, Map<String, Object[]> creditNoteMapVendorGL, Map<String, Object[]> debitNoteMapGL, Map<String, Object[]> debitNoteMapCustomerGL, Map<String, Object[]> paymentReceivedMapGL, Map<String, Object[]> paymentMadeMapGL, Map<String, GoodsReceipt> fixedAssetgrMapGL, Map<String, Invoice> fixedAssetInvoiceMapGL, Map<String, Invoice> cashSalesGL, Map<String, GoodsReceipt> cashPurchaseGL, Map<String, Integer> jeDetailPaymentTypeMapGL, Map<String, Integer> jeDetailReceiptTypeMapGL);
    public JSONObject getLedgerForGL(HttpServletRequest request, Map<String, BillingInvoice> billingInvoiceMapGL, Map<String, BillingGoodsReceipt> billingGrMapGL, Map<String, Object[]> billingCreditNoteMapGL, Map<String, Object[]> billingDebitNoteMapGL, Map<String, Object[]> billingPaymentReceivedMapGL, Map<String, Object[]> billingPaymentMadeMapGL, Map<String, Invoice> invoiceMapGL, Map<String, GoodsReceipt> grMapGL, Map<String, Object[]> creditNoteMapGL, Map<String, Object[]> creditNoteMapVendorGL, Map<String, Object[]> debitNoteMapGL, Map<String, Object[]> debitNoteMapCustomerGL, Map<String, Object[]> paymentReceivedMapGL, Map<String, Object[]> paymentMadeMapGL, Map<String, GoodsReceipt> fixedAssetgrMapGL, Map<String, Invoice> fixedAssetInvoiceMapGL, Map<String, Invoice> leaseInvoiceMapGL, Map<String, Integer> jeDetailPaymentTypeMapGL, Map<String, Integer> jeDetailReceiptTypeMapGL) throws ServiceException, SessionExpiredException;
    public void generateIBGFileForCIMBbank(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException, AccountingException;
    public JSONObject getOpeningBalancesWithDateLedger(HttpServletRequest request, String companyid, Date startDate, Date endDate) throws ServiceException, SessionExpiredException;
    public boolean copyCustomLayoutTemplate(Map<String, Object> requestParams);
    public JSONArray getProductPriceReportCustVenJson(HttpServletRequest request,HashMap<String, Object> requestParams) throws ServiceException, SessionExpiredException;
    public JSONObject getOrderedJSONForDimensionBasedCustomLayout(Map<String, Object> requestParams, JSONObject tradingjobj, boolean isPrint) throws ServiceException, SessionExpiredException;
    public JSONObject getPriceListBandReport(Map<String, Object> requestParams, boolean isExport);
    public Map<String,Object> getForexEnrtyDetailData(Map<String, Object> requestParams ) throws ServiceException, SessionExpiredException;
    public JSONObject getDNdataForCustomLineDetailReport(HashMap<String, Object> requestParams, JSONArray DataJArr) ;
    public JSONObject getCNdataForCustomLineDetailReport(HashMap<String, Object> requestParams, JSONArray DataJArr) ;
    public void putLineCustomDetails(JSONObject jSONObject, Map<String, Object> map) throws ServiceException, JSONException;
    public void putGlobalCustomDetails(JSONObject jSONObject, Map<String, Object> map) throws ServiceException, JSONException;

    public double[] calculateOpeningAndClosingStock(JSONObject paramJobj, CompanyAccountPreferences pref, ExtraCompanyPreferences extrapref, String companyid, Date startDate, Date endDate, MutableDouble[] valuationObj, Map<String, Object> advSearchAttributes, Map<String, Map> stockDateMap);
    
    public JSONArray getDataForCostAndSellingPriceReport(List list, JSONObject requestParams, Map<String, PriceValuationStack.Batch> valuationMap);
    public JSONObject getServiceTaxInputCreditSummaryReportJSON(JSONObject dataMap, boolean isExport);
    public JSONObject getForm201CExcelJSON(HashMap dataMap, boolean isExport);
    public JSONObject getTDSChallanControlReportJSON(JSONObject dataMap, boolean isExport);
    public JSONObject getDVATForm31ReportJSON(JSONObject dataMap, boolean isExport);
    public JSONObject getRequestParams(HttpServletRequest request);

    public double getParentOpeningBalance(Account account, double totalAccountBalance, JSONObject params, Date startDate, Date endDate) throws ServiceException, ParseException;
    
    public double[] calculateProfitLossForTrialBalance(JSONObject paramJobj, Date startDate, Date endDate, Date start, boolean isOpeningFlag, boolean periodView, boolean isPeriod, boolean stockValuationFlag, MutableDouble[] valuationObj, Map<String, Object> advSearchAttributes);
    
    public double[] getGroupWiseGLReport(JSONObject requestParams, JSONArray jsonArray,List<GeneralLedger> generalLedgerList);
    
    public List<Map<String,Object>> getMalaysiangstMSICCodes(JSONObject paramobj , JSONObject industryCodes) throws Exception;
    
    public JSONArray getGSTForm5BreakDown(Company company, JSONObject industryCodes) throws Exception ;
    
    public JSONArray getMsicLineBreakUp(Map<String, Object> requestParams, Invoice inv, boolean isMalasianCompany) throws Exception ;
    
    public JSONArray getVatPurchaseRegister(HashMap<String, Object> requestParams) throws ServiceException ;
    
    public JSONArray getVatSalesRegister(HashMap<String, Object> requestParams) throws ServiceException ;
    
    public Map<String, String> getTransactionIndustryCodeMap(Map<String, Object> requestParams) throws Exception ;
 
    public KwlReturnObject getTDSAppliedInvoices(Map<String, Object> requestParams) throws Exception ;
    
    public KwlReturnObject getTDSAppliedDebitNote(Map<String, Object> requestParams) throws Exception ;
    
    public void getInvoicesForTDSNotDeductedReport(Map<String, Object> requestParams, List tdsnotdeducted) throws Exception ;
    
    public Map<String, String> getTransactionIndustryCodeMapForDeliveryOrder(Map<String, Object> requestParams) throws Exception ;
    public double getClosedYearNetProfitAndLoss(Date endDate, CompanyAccountPreferences preferences, ExtraCompanyPreferences extraCompanyPreferences, String companyid);
    public List getChildAccounts(List ll, Account account);
    public double getAccountBalanceMerged(JSONObject requestJobj, HashMap<String, Object> requestParams, String accountid, Date startDate, Date endDate, boolean eliminateflag) throws ServiceException;
    public double getAccountBalanceInOriginalCurrency(JSONObject requestJobj, HashMap<String, Object> requestParams, String accountid, Date startDate, Date endDate) throws ServiceException;
    public JSONObject addNetBalanceJson(JSONObject requestJobj, Account account, double balance, KWLCurrency currency);
    public void generateIBGFileForUOBbank(HttpServletRequest request, HttpServletResponse response) throws SessionExpiredException, ServiceException, AccountingException;
    public JSONObject calculateCustomerAccuredBalance(List customerLedgerList, HashMap<String, Object> requestParams) throws ServiceException, JSONException, SessionExpiredException, ParseException ;
    public boolean isPeriodWiseOpeningTransaction(Date transactionDate, Date startDate) throws SessionExpiredException, ParseException;
    public double getAmountOfTransactionForCusromerSOA(HashMap<String, Object> requestParams) throws SessionExpiredException, ParseException, ServiceException ;
    public JSONObject calculateVendorAccuredBalance(List vendorLedgerList, HashMap<String, Object> requestParams) throws ServiceException, JSONException, SessionExpiredException, ParseException ;
    public double getAmountOfTransactionForVendorSOA(HashMap<String, Object> requestParams) throws SessionExpiredException, ParseException, ServiceException ;
    public String getDocumentStatusForVendorAccountStatement(HashMap<String, Object> requestParams) throws SessionExpiredException, ParseException, ServiceException ;
    public Map<String, Object> getSubLedgerToExport(JSONObject requestParams, JSONArray jsonArray)throws ServiceException,SessionExpiredException;
    public Map<String, Object> getGeneralLedgerToExportPDFSummary(JSONObject requestParams, JSONArray jsonArray)throws ServiceException,SessionExpiredException;
    List exportCustVenProductsPriceJasper(HttpServletRequest request, JSONArray jarr, AccountingHandlerDAO accountingHandlerDAOobj) throws ServiceException  ;
      
    public JSONArray getNatureOfPaymentWiseReport(HashMap requestParams) throws ServiceException ;
    
    public Map<String, Double> getPeriodAccountAmountMap(Map<String,Object> requestParams) throws ServiceException;
    public Map<String, Double> getOpeningAccountAmountMap(Map<String,Object> requestParams) throws ServiceException, JSONException,SessionExpiredException;
    public Map<String, List<Account>> getGroupAccountMap(JSONObject paramJObj) throws JSONException, ServiceException;
    public double[] getTradingAllAccount(JSONObject paramJObj, int nature, JSONArray jArr, Map<String, Double> accAmtMap, Map<String, Object> extraObjects) throws ServiceException, SessionExpiredException, JSONException;
    public double[] getProfitLossAllAccounts(JSONObject paramJObj, int nature, JSONArray jArr, boolean isProfitLoss, Map<String, Double> accAmtMap,Map<String, Object> extraObjects) throws ServiceException, SessionExpiredException, JSONException;
    public JSONObject getBalanceSheetAllAccounts(JSONObject paramJobj, Map<String, Double> accAmtMap) throws JSONException, ServiceException, SessionExpiredException;
    public void updatePayment(String[] paymentIds, String accCompanyId) throws ServiceException;
    public String generateIBGFileForOCBCBank(JSONObject paramJObj) throws SessionExpiredException, ServiceException, AccountingException;
    
    public JSONObject getTrialBalance(JSONObject paramJobj) throws ServiceException, SessionExpiredException;
    public void addPerpetualInventoryJEGlobalLevelCustomData(HashMap<String, Object> requestParams, JournalEntry journalEntry, JSONObject obj) throws ServiceException;
    public void addPerpetualInventoryJELineLevelCustomData(HashMap<String, Object> requestParams, JournalEntryDetail jed, JSONObject obj) throws ServiceException;
    public double[] getSOAVendorAmountFromJEDetail(HashMap<String, Object> requestParams, List<JournalEntryDetail> listJEDetail) throws ServiceException;
    public double[] getSOACustomerAmountFromJEDetail(HashMap<String, Object> requestParams, List<JournalEntryDetail> listJEDetail) throws ServiceException;
   
    /**
     * DESC: This method is used to get grid configuration to the GL Report
     * @param obj
     * @return JSONObject
     * @throws ServiceException 
     */
    public JSONObject getGLRegistryGridInfo(JSONObject obj) throws ServiceException;
 
    public Date calculateStartDateIfBookClosed(Date startDate, boolean stockValuationFlag, String companyid) throws ServiceException, JSONException, SessionExpiredException;
    
    /**
     * Method is used to save GST Form 5 e-Submission details.
     * @param eSubmissionDetails
     * @param requestParam
     * @return
     * @throws ServiceException 
     */
    public JSONObject saveeSubmissionGSTForm5(String eSubmissionDetails,JSONObject requestParam) throws ServiceException;
    
    public JSONObject saveResponseeSubmissionGSTForm5(String eSubmissionresponse,String recordId) throws ServiceException;
    
    public JSONObject getGSTForm5JSONString(JSONArray jsonArr, JSONObject requestParam) throws ServiceException;
    
    public JSONObject gstForm5eSubmissionUpdateStatus(JSONObject requestParam) throws ServiceException;
    
    /**
     * Following method is used to Prepare url for callbackjsp page to redirect user respected jsp page.
     * necessary data fetched from //table//
     * @param state
     * @param scope
     * @param code
     * @return 
     */
    public String getIRASCallbackUrl(String state,String scope,String code)throws ServiceException;
    
    public JSONObject initiateTransactionListingSubmission(JSONArray purchasejArr, JSONArray salesjArr, JSONObject requestParam) throws ServiceException;
    
    public JSONObject initiateTransactionListingreSubmission(HashMap<String, Object> requestParam) throws ServiceException;
    
    /**
     * Method is used for two purpose,
     * 1-> used to save data to proxy server
     * 2-> to get IRAS redirect URL.
     * @param chunkids
     * @param companyid
     * @param callbackURL
     * @param description
     * @param flag
     * @return
     * @throws ServiceException 
     */
    public JSONObject proxyServerEntryAPICall(String chunkids , String companyid, String callbackURL, String description,int flag) throws ServiceException;
    
    /**
     * Method is used to save Transaction Listing e-Submission Details.
     * @param ids
     * @param company
     * @param code
     * @param state
     * @throws ServiceException 
     */
    public void gstTransactionListingDataSubmission(String ids, String company,String code,String state) throws ServiceException ;
}
