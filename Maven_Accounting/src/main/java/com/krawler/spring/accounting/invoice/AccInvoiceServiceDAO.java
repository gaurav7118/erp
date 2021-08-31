/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.krawler.spring.accounting.invoice;

import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.hql.accounting.*;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import com.lowagie.text.DocumentException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.context.MessageSource;

/**
 *
 * @author krawler
 */
public interface AccInvoiceServiceDAO {

    public JSONObject getCustomerAgedReceivableMerged(HttpServletRequest request,boolean exportCustomerAged,boolean isAgedReceivables) throws ServiceException, JSONException, SessionExpiredException, ParseException;
    public JSONObject getCustomerAgedReceivableMerged(JSONObject request,boolean exportCustomerAged,boolean isAgedReceivables) throws ServiceException, JSONException, SessionExpiredException, ParseException;
    
    public JSONArray getSalesPersonAgedSummary(HttpServletRequest request) throws ServiceException, JSONException, SessionExpiredException, ParseException, UnsupportedEncodingException;
    
    public JSONArray getSalesPersonAgedDetail(HttpServletRequest request,JSONObject requestOBJ) throws ServiceException, JSONException, SessionExpiredException, ParseException, UnsupportedEncodingException;

    public double getInvDisountOnAmt(String id, double withoutTAmt, boolean isWithoutInventory) throws ServiceException;

    public JSONArray getTermDetails(String invoiceid);
    
    public Map<String, JSONArray>  getInvoiceTermMapDetailList(String invoiceId, List<InvoiceTermsMap> InvoiceTermsList);

    public void setMessageSource(MessageSource msg);
    
    public JSONArray getInvoiceJsonMerged(HttpServletRequest request, List<Object[]> list, JSONArray jArr) throws SessionExpiredException, ServiceException, JSONException;
     
    public HashMap getDODetailsCustomFieldMap(Map requestParams) throws SessionExpiredException, ServiceException,ParseException;
    
    public JSONObject getDeliveryOrderRows(HttpServletRequest request, HashMap fieldMap) throws SessionExpiredException, ServiceException,ParseException;
    
//    public String getNewBatchJson(Product product, HttpServletRequest request, String documentid) throws ServiceException, SessionExpiredException, JSONException;

    public String getDeliveryReturnStatus(Set<DeliveryOrderDetail> orderDetail,String salesreturnId,boolean isconsignment,boolean soReopen) throws ServiceException, SessionExpiredException, JSONException;
     
    public double getDeliveryOrderDetailStatusFORSR(DeliveryOrderDetail sod) throws ServiceException;

    public double getDeliveryOrderDetailStatus(DeliveryOrderDetail sod) throws ServiceException;
    
    public void getASsetDetailsJson(DeliveryOrderDetail row, String companyid, JSONObject obj, DateFormat df, CompanyAccountPreferences preferences, HttpServletRequest request, Contract contract) throws JSONException, ServiceException, SessionExpiredException;
    
    public HashMap<String, Object> getDeliveryOrdersMap (HttpServletRequest request) throws SessionExpiredException;
    
    public HashMap<String, Object> getDeliveryOrdersMapJSON(JSONObject paramJobj) throws JSONException, ServiceException, SessionExpiredException;
    
     public JSONArray getDeliveryOrdersJsonMerged(JSONObject paramJobj, List list) throws ServiceException;
    
    public JSONArray getBadDebtClaimedInvoicesJson(HttpServletRequest request) throws SessionExpiredException, ServiceException;
    
    public JSONArray getRecoveredBadDebtInvoices(HttpServletRequest request) throws SessionExpiredException, ServiceException;
    
    public JSONArray getBadDebtInvoices(HashMap<String, Object> badMaps) throws SessionExpiredException, ServiceException, JSONException;

    public JSONArray getMonthlyCustomerAgedReceivableMerged(HttpServletRequest request) throws ServiceException, JSONException, SessionExpiredException, ParseException;
    
    public JSONArray getMonthlyCustomerAgedReceivableMerged(JSONObject paramJobj) throws ServiceException, JSONException, SessionExpiredException, ParseException;

    public void exportMonthlyAgedReceivableSummarized(HttpServletRequest request, HttpServletResponse response, JSONObject jobj) throws DocumentException, ServiceException, IOException;

    public void exportMonthlyAgedReceivableDetails(HttpServletRequest request, HttpServletResponse response, JSONObject jobj) throws DocumentException, ServiceException, IOException;
    
    public KwlReturnObject getNewBatchSerialForConsignmentLoan(Product product, HttpServletRequest request, String documentid) throws ServiceException, SessionExpiredException, JSONException;
    
    public Map<String,Object[]> getBatchSerialForConsignmentLoan(List<Object[]> list,String companyID) throws ServiceException, SessionExpiredException, JSONException;
    
    public Map<String,Object[]> getBatchSerialDetailsForReturnReport(List<Object[]> list,String companyID) throws ServiceException, SessionExpiredException, JSONException;
    
    public JSONArray getDeliveryOrdersJson(JSONObject paramJobj, List list) throws ServiceException;
    
    public double getNewBatchRemainingQuantity(String locationid, String warehouseid, String companyid, String productid,  String purchasebatchid, String moduleID, boolean isEdit,  String documentid)  throws ServiceException ;

    public String deleteRecurringInvoiceRule(HashMap<String, Object> requestParams)throws ServiceException,SessionExpiredException ;

    public JSONObject getAgedReportSummaryBasedOnDimension(HttpServletRequest request)throws ServiceException;

    public JSONObject getAgedReportSummaryGoupingOnInvoiceDimension(HttpServletRequest request) throws ServiceException;
    
    public JSONObject getCustomerPartyLedgerSummary(HttpServletRequest request) throws ServiceException, JSONException, SessionExpiredException, ParseException;

    public JSONObject getAgedReceivableDetailBasedOnDimension(HttpServletRequest request)throws ServiceException;
    
    public JSONObject getSalesPersonCommissionDimensionReport(HttpServletRequest request, boolean isExport);

    public List unlinkCreditNoteFromInvoice(HttpServletRequest request, List<CreditNoteDetail> details,String cnid) throws ServiceException, SessionExpiredException;
    public List unlinkReceiptFromInvoice(HttpServletRequest request, List<LinkDetailReceipt> details, String paymentid) throws ServiceException, SessionExpiredException ;
    
     public JSONObject getAgedReportSummaryBasedOnCustomer(HttpServletRequest request)throws ServiceException;
     
     public JSONArray getPaymentTermSalesCommissionDetailReportJson(HttpServletRequest request, List<Invoice> invList) throws SessionExpiredException, ServiceException;

    public JSONObject getCustomerAgedReceivableBasedOnCustomerDimensdion(HttpServletRequest request, boolean exportCustomerAged,boolean isAgedReceivables)throws ServiceException, JSONException, SessionExpiredException, ParseException;

    public JSONObject getCustomerAgedReceivableBasedOnDocumentsDimension(HttpServletRequest request, boolean exportCustomerAged,boolean isAgedReceivables)throws ServiceException, JSONException, SessionExpiredException, ParseException;

    public Map<String, List<Object[]>> getBatchDetailsMap(Map<String, Object> requestParams);
    
    public JSONArray getInvoiceDetailsForMonthlySalesReport(HttpServletRequest request, List<Object[]> list, JSONArray jArr) throws SessionExpiredException, ServiceException;
    
    public JSONObject getColumnModelForWorkCentreReport(boolean isExport);
    
    public JSONObject getColumnModelForJobOrderReport(boolean isExport);
    
    public JSONObject getColumnModelForWorkCentreList(boolean isExport);

    public JSONArray getInvoiceJsonMergedJson(JSONObject paramJobj, List<Object[]> list, JSONArray jArr) throws SessionExpiredException, ServiceException;
    
    public JSONArray getInvoiceRows(JSONObject paramJobj, String[] invoices,Map<String, Object> extraAttributesMap) throws ServiceException, SessionExpiredException;
    
    public JSONArray getDetailExcelJsonInvoice(JSONObject paramJobj, Map<String, Object> requestParams, JSONArray DataJArr)  throws JSONException, SessionExpiredException, ServiceException,ParseException;
    /**
     * Function will be called only for delivery order flow to fetch product
     * quantity remained for invoice in DO
     */
    public double getInvoiceQuantityForDO(InvoiceDetail ivDetail) throws ServiceException;
    /**
     * Method to fetch list of available quantities of a product-batch in inventory
     * @param requestJobj
     * @return
     * @throws ServiceException 
     */
    public List getProductBatchQuantityList(JSONObject requestJobj) throws ServiceException;
    /**
     * Method to fetch available quantity of a product-batch in inventory
     * @param requestJobj
     * @return
     * @throws ServiceException
     * @throws JSONException 
     */
    public JSONObject getProductBatchQuantity(JSONObject requestJobj) throws ServiceException, JSONException;
    
    @Deprecated
    public HashMap applyCreditNotes(HashMap requestParams, Invoice invoice) throws ServiceException ;
    
    public HashMap applyCreditNotes_Modified(HashMap requestParams, InvoiceDetail temp) throws ServiceException;
    
    public double applyInvDisount(InvoiceDetail invdetail, double withoutDTAmt) throws ServiceException;
    
    public String getNewBatchJson(Product product, JSONObject paramJobj, String documentid) throws ServiceException, SessionExpiredException, JSONException ;
    
    public JSONObject deleteInvoiceJson(JSONObject paramJObj) throws JSONException, ServiceException;
    
    /**
     * Description : This method is used to build the Json data of Daily Sales Report
     * @param request, requestParams
     * @return JSONObject
     * @throws ServiceException 
     */
    public JSONObject getDailySalesReportDetails(HttpServletRequest request, Map<String, Object> requestParams)throws ServiceException;
    
    public JSONObject getColumnModelForMonthlySalesReport(HashMap<String,Object> requestParams, JSONArray jArr)throws ServiceException;
    
    public JSONObject getColumnModelForCustomerMonthlySalesReport(HashMap<String,Object> requestParams, JSONArray jArr)throws ServiceException;
    
    public List getMonthList(String startDateStr, String endDateStr, String datePattern) throws JSONException;
    
    public JSONArray getExciseComputationReportDetails(Map<String, Object> requestParams,JSONArray jArr)throws ServiceException;
    
    public JSONArray getServiceTaxComputationReportDetails(Map<String, Object> requestParams,JSONArray jArr)throws ServiceException;
    
    public Map<String, JSONArray> getColumnModuleForAPAR(Map<String, Object> requestParams) throws ServiceException;

    public Map<String, JSONArray> getColumnModuleForMonthlyTradingAndProfitLossReport(Map<String, Object> requestParams) throws ServiceException;
    
    public Map<String, JSONArray> getColumnModuleForDeliveryOrder(Map<String, Object> requestParams) throws ServiceException;
      
    public JSONArray getSalesReturnJson(JSONObject paramJobj, List<Object[]> list) throws ServiceException;
    
    public JSONArray getSalesReturnDetailsJson(JSONObject paramJobj, List<Object[]> list,DateFormat dfWithoutSeconds) throws ServiceException;
    
    public JSONObject deleteSalesReturnJson(JSONObject paramJObj) throws JSONException, ServiceException;
    
    public JSONObject getSalesReturnRows(JSONObject paramJobj) throws SessionExpiredException, ServiceException, UnsupportedEncodingException;
    
    public JSONObject deleteSalesReturnPermanent(JSONObject paramJobj) throws SessionExpiredException, AccountingException, ServiceException, JSONException;
    
    public JSONObject deleteSalesReturn(JSONObject paramJobj) throws SessionExpiredException, AccountingException, ServiceException, JSONException;
    
    public JSONObject deleteSalesReturnTemporaryJson(JSONObject paramJobj) throws SessionExpiredException ;
    
    public JSONObject getSalesAnalysis_TopCustomers_Report(JSONObject paramJobj)throws SessionExpiredException, ServiceException;
    
    public JSONObject getSalesAnalysis_TopProducts_Report(JSONObject paramJobj) throws SessionExpiredException, ServiceException;
    
    public JSONObject getSalesAnalysis_TopAgents_Report(JSONObject paramJobj) throws SessionExpiredException, ServiceException;
    
    public JSONObject getSalesByCustomerJson(JSONObject paramJobj, List list, boolean isSalesAnalysis) throws SessionExpiredException, ServiceException;
    
    public JSONObject getPaymentDetailsForInvoice(JSONObject paramJobj) throws SessionExpiredException, ServiceException;
    
    public JSONObject getConsolidationAgedReceivable(JSONObject paramsJobj)throws ServiceException, JSONException, SessionExpiredException, ParseException;
    
    public JSONObject getDeliveryOrdersMerged(JSONObject paramJobj);
    
    public JSONObject getUnprintedDeliveryOrdersChartJSON(JSONObject paramJobj);
    
    public JSONObject getDeliveryOrderRowsForShippingDoList(JSONObject reqParams) throws SessionExpiredException, ServiceException;
    
    public JSONObject getDeliveryOrderRowsForShippingDoDetails(Map<String, Object> requestParams) throws  ServiceException;
    
    public JSONObject getShippingDeliveryOrder(JSONObject params,Map<String,Object> requestMap) throws ServiceException, JSONException;

    public Map<String, Object> getCheckListSOAgainstDO(Map requestParams);
    
    public Map<String, Double> getClosedQtyForLoan(String companyID) throws ServiceException;
    
    public Set<String> getUPSTrackingNumberFromDoDetails(Set<String> deliveryOrdeDetailIDs)throws ServiceException;
    
    //delete single Invoice temporarily
    public String deleteInvoice(String linkedTransaction, JSONObject requestJobj, JSONObject jobj, CompanyAccountPreferences preferences, boolean isFixedAsset, boolean isLeaseFixedAsset, boolean isConsignment, String companyid) throws ServiceException, AccountingException, SessionExpiredException, JSONException;
    /* if pick pack is on for delivery order*/
    public String getDOBatchJsonUsingIST(Map<String, Object> requestParams) throws ServiceException,JSONException;
    //delete single Invoice permanently
    public String deleteInvoicePermanent(String linkedTransaction, JSONObject jobj, JSONObject requestJobj, String companyid, boolean isFixedAsset, boolean isLeaseFixedAsset, boolean isConsignment, boolean auditcheck, int countryid, CompanyAccountPreferences preferences) throws ServiceException, AccountingException, SessionExpiredException, ParseException, JSONException;
    
    public HashMap getCustomerAgedReceivableMap(HttpServletRequest request, boolean isAgedReceivables) throws SessionExpiredException,  UnsupportedEncodingException, ServiceException;
    
    public JSONArray getAgeingJson(Map<String, Object> invoiceRequestParams,JSONObject params) throws ServiceException, JSONException, SessionExpiredException;
    
    public JSONArray getAgeingInvoiceJson(Map<String, Object> invoiceRequestParams,JSONObject params) throws ServiceException, JSONException, SessionExpiredException;
    
    public void getInvoiceCustomDataForPayment(HashMap<String, Object> request, JSONObject obj, Invoice invoice, JournalEntry je) throws ServiceException;
    
    public double getExchangeRateForSpecificCurrency(JSONObject requestJobj) throws JSONException, ServiceException, ParseException;
    
    public JSONObject generateGRNFromMultipleDO(JSONObject paramJObj) throws ServiceException, SessionExpiredException, JSONException;
    public JSONObject getCustomFeild(JSONObject obj, Invoice invoice, JSONObject paramJobj, HashMap replaceFieldMap, HashMap customFieldMap, HashMap customDateFieldMap, HashMap FieldMap, HashMap replaceFieldMapRows, HashMap customFieldMapRows, HashMap customDateFieldMapRows, HashMap fieldMapRows) throws ServiceException, JSONException, SessionExpiredException;
    
    public JSONObject getInvoices(JSONObject paramJobj);
   
    public JSONArray getAllKnockOffJson(Map<String, Object> invoiceRequestParams) throws ServiceException, JSONException;
}
