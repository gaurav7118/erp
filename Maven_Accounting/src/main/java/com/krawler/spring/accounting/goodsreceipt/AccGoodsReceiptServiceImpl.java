package com.krawler.spring.accounting.goodsreceipt;

import com.krawler.common.admin.*;
import com.krawler.common.service.ServiceException;
import com.krawler.common.session.SessionExpiredException;
import com.krawler.common.util.Constants;
import static com.krawler.common.util.Constants.GSTRegType_Unregistered;
import com.krawler.common.util.StringUtil;
import com.krawler.hql.accounting.*;
import com.krawler.inventory.exception.InventoryException;
import com.krawler.inventory.model.ist.ISTDetail;
import com.krawler.inventory.model.ist.InterStoreTransferRequest;
import com.krawler.inventory.model.stock.StockService;
import com.krawler.inventory.model.stockmovement.StockMovementService;
import com.krawler.spring.accounting.account.accAccountDAO;
import com.krawler.spring.accounting.bankreconciliation.accBankReconciliationDAO;
import com.krawler.spring.accounting.creditnote.accCreditNoteDAO;
import com.krawler.spring.accounting.currency.accCurrencyDAO;
import com.krawler.spring.accounting.debitnote.accDebitNoteDAO;
import com.krawler.spring.accounting.debitnote.accDebitNoteService;
import com.krawler.spring.accounting.goodsreceipt.service.accGoodsReceiptModuleService;
import com.krawler.spring.accounting.handler.*;
import com.krawler.spring.accounting.invoice.*;
import com.krawler.spring.accounting.jasperreports.*;
import com.krawler.spring.accounting.product.accProductDAO;
import com.krawler.spring.accounting.purchaseorder.accPurchaseOrderDAO;
import com.krawler.spring.accounting.vendor.accVendorDAO;
import com.krawler.spring.authHandler.authHandler;
import com.krawler.spring.common.KwlReturnObject;
import com.krawler.spring.sessionHandler.sessionHandlerImpl;
import com.krawler.utils.json.base.JSONArray;
import com.krawler.utils.json.base.JSONException;
import com.krawler.utils.json.base.JSONObject;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.context.MessageSource;
import com.krawler.spring.accounting.journalentry.accJournalEntryDAO;
import com.krawler.spring.accounting.payment.accPaymentDAO;
import com.krawler.spring.accounting.receipt.accReceiptDAO;
import com.krawler.spring.accounting.reports.AccReportsService;
import com.krawler.spring.accounting.tax.accTaxDAO;
import com.krawler.spring.accounting.vendorpayment.accVendorPaymentDAO;
import com.krawler.spring.auditTrailModule.auditTrailDAO;
import com.krawler.spring.authHandler.authHandlerDAO;
import com.krawler.spring.common.*;
import com.lowagie.text.DocumentException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.JRPdfExporterParameter;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.Period;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.orm.hibernate3.HibernateTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import com.krawler.spring.accounting.handler.AccDashboardServiceImpl;

/**
 *
 * @author krawler
 */
public class AccGoodsReceiptServiceImpl implements AccGoodsReceiptServiceDAO {
    private HibernateTransactionManager txnManager;
    private accGoodsReceiptDAO accGoodsReceiptobj;
    private accInvoiceDAO accInvoiceDAOobj;
    private accVendorPaymentDAO accVendorPaymentobj;
    private accJournalEntryDAO accJournalEntryobj;
    private accProductDAO accProductObj;
    private accDebitNoteDAO accDebitNoteobj;
    private accCurrencyDAO accCurrencyDAOobj;
    private accVendorDAO accVendorDAOobj;
    private accTaxDAO accTaxObj;
    private AccountingHandlerDAO accountingHandlerDAOobj;
    private String successView;
    private kwlCommonTablesDAO kwlCommonTablesDAOObj;
    private accGoodsReceiptCMN accGoodsReceiptCommon;
    private MessageSource messageSource;
    private accAccountDAO accAccountDAOobj;
    private authHandlerDAO authHandlerDAOObj;
    private accReceiptDAO accReceiptDAOobj;
    private accCreditNoteDAO accCreditNoteDAOobj;
    private AccGoodsReceiptServiceHandler accGoodsReceiptServiceHandler;
    private auditTrailDAO auditTrailObj;
    private accPaymentDAO accPaymentDAOobj;
    private fieldDataManager fieldDataManagercntrl;
    private AccReportsService accReportsService;
    private StockService stockService;
    private StockMovementService stockMovementService;
    private accPurchaseOrderDAO accPurchaseOrderobj;
    private accDebitNoteService accDebitNoteService;
    private accBankReconciliationDAO accBankReconciliationObj;
    private accGoodsReceiptModuleService accGoodsReceiptModuleService;
    private AccDashboardServiceImpl accDashboardServiceImpl;
    
    public void setaccBankReconciliationDAO(accBankReconciliationDAO accBankReconciliationObj) {
        this.accBankReconciliationObj = accBankReconciliationObj;
    }
    
    public void setaccDebitNoteService(accDebitNoteService accDebitNoteService) {
        this.accDebitNoteService = accDebitNoteService;
    }

    public void setStockMovementService(StockMovementService stockMovementService) {
        this.stockMovementService = stockMovementService;
    }

    public void setaccInvoiceDAO(accInvoiceDAO accInvoiceDAOobj) {
        this.accInvoiceDAOobj = accInvoiceDAOobj;
    }

    public void setaccPurchaseOrderDAO(accPurchaseOrderDAO accPurchaseOrderobj) {
        this.accPurchaseOrderobj = accPurchaseOrderobj;
    }
        
    public void setFieldDataManager(fieldDataManager fieldDataManagercntrl) {
        this.fieldDataManagercntrl = fieldDataManagercntrl;
    }
    public void setaccPaymentDAO(accPaymentDAO accPaymentDAOobj) {
        this.accPaymentDAOobj = accPaymentDAOobj;
    }

    public void setauditTrailDAO(auditTrailDAO auditTrailDAOObj){
        this.auditTrailObj = auditTrailDAOObj;
    }
       
    public void setaccReceiptDAO(accReceiptDAO accReceiptDAOobj) {
        this.accReceiptDAOobj = accReceiptDAOobj;
    }
     
    public void setaccCreditNoteDAO(accCreditNoteDAO accCreditNoteDAOobj) {
        this.accCreditNoteDAOobj = accCreditNoteDAOobj;
    }
    
    public void setAccGoodsReceiptServiceHandler(AccGoodsReceiptServiceHandler accGoodsReceiptServiceHandler) {
        this.accGoodsReceiptServiceHandler = accGoodsReceiptServiceHandler;
    }
    
    public void setaccAccountDAO(accAccountDAO accAccountDAOobj) {
        this.accAccountDAOobj = accAccountDAOobj;
    }
    
    @Override
    public void setMessageSource(MessageSource msg) {
            this.messageSource = msg;
    }

    public void setkwlCommonTablesDAO(kwlCommonTablesDAO kwlCommonTablesDAOObj1) {
        this.kwlCommonTablesDAOObj = kwlCommonTablesDAOObj1;
    }
    public void setaccGoodsReceiptCMN(accGoodsReceiptCMN accGoodsReceiptCommon) {
        this.accGoodsReceiptCommon = accGoodsReceiptCommon;
    }

    public void setTxnManager(HibernateTransactionManager txManager) {
        this.txnManager = txManager;
    }
    public void setaccGoodsReceiptDAO(accGoodsReceiptDAO accGoodsReceiptobj) {
        this.accGoodsReceiptobj = accGoodsReceiptobj;
    }
    public void setaccVendorPaymentDAO(accVendorPaymentDAO accVendorPaymentobj) {
        this.accVendorPaymentobj = accVendorPaymentobj;
    }
    public void setaccJournalEntryDAO(accJournalEntryDAO accJournalEntryobj) {
        this.accJournalEntryobj = accJournalEntryobj;
    }
    public void setaccProductDAO(accProductDAO accProductObj) {
        this.accProductObj = accProductObj;
    }
    public void setaccDebitNoteDAO(accDebitNoteDAO accDebitNoteobj) {
        this.accDebitNoteobj = accDebitNoteobj;
    }
    public void setaccCurrencyDAO(accCurrencyDAO accCurrencyDAOobj) {
        this.accCurrencyDAOobj = accCurrencyDAOobj;
    }
    public void setaccVendorDAO(accVendorDAO accVendorDAOobj) {
        this.accVendorDAOobj = accVendorDAOobj;
    }
    public void setaccTaxDAO(accTaxDAO accTaxObj) {
        this.accTaxObj = accTaxObj;
    }
    public void setaccountingHandlerDAO(AccountingHandlerDAO AccountingHandlerDAOobj) {
        this.accountingHandlerDAOobj = AccountingHandlerDAOobj;
    }

    public String getSuccessView() {
        return successView;
    }
    public void setSuccessView(String successView) {
        this.successView = successView;
    }

    public void setAuthHandlerDAOObj(authHandlerDAO authHandlerDAOObj) {
        this.authHandlerDAOObj = authHandlerDAOObj;
    }
 
    public void setAccReportsService(AccReportsService accReportsService) {
        this.accReportsService = accReportsService;
    }
    public void setStockService(StockService stockService) {
        this.stockService = stockService;
    }
    public void setAccGoodsReceiptModuleService(accGoodsReceiptModuleService accGoodsReceiptModuleService) {
        this.accGoodsReceiptModuleService = accGoodsReceiptModuleService;
    }
    
    public void setAccDashboardServiceImpl(AccDashboardServiceImpl accDashboardServiceImpl) {
        this.accDashboardServiceImpl = accDashboardServiceImpl;
    }
    
    @Override
    public HashMap getVendorAgedPayableMap(HttpServletRequest request, HashMap<String, Object> requestParams) throws SessionExpiredException,  UnsupportedEncodingException,   ServiceException{
//        String companyid = sessionHandlerImpl.getCompanyid(request);
//        HashMap goodsReceiptRequestParams = AccGoodsReceiptServiceHandler.getGoodsReceiptRequestMap(request);
        try {
//            goodsReceiptRequestParams.put("companyid", companyid);
//            boolean isCallFromDD = false;
//            if (request.getAttribute("isCallFromDD") != null) {
//                isCallFromDD = Boolean.parseBoolean(request.getAttribute("isCallFromDD").toString());
//            }
//            String transactionId = request.getAttribute("transactionId") == null ? "" : request.getAttribute("transactionId").toString();
//            if (isCallFromDD) {
//                goodsReceiptRequestParams.put("custVendorID", transactionId);
//            }
//            boolean isAged = (!StringUtil.isNullOrEmpty(request.getParameter("isAged"))) ? Boolean.parseBoolean(request.getParameter("isAged")) : false;
//            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.moduleid))) {
//                /*
//                 remove searchjson while export Statement of account
//                 */
//                int module = Integer.parseInt(request.getParameter(Constants.moduleid));
//                if (module == Constants.Acc_Vendor_ModuleId) {
//                    goodsReceiptRequestParams.remove(Constants.Acc_Search_Json);
//                }
//            }
//            
//            
//            boolean isAdvanceSearch = false;
//            String Searchjson = "";
//            String invoiceSearchJson = "";
//            String receiptSearchJson = "";
//            String cnSearchJson = "";
//            String dnSearchJson = "";
//            String makePaymentSearchJson = "";
//            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
//            if (goodsReceiptRequestParams.containsKey(Constants.Filter_Criteria) && goodsReceiptRequestParams.get(Constants.Filter_Criteria) != null) {
//                if (goodsReceiptRequestParams.get(Constants.Filter_Criteria).toString().equalsIgnoreCase("OR")) {
//                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
//                }
//            }
//            if (isAged && goodsReceiptRequestParams.containsKey(Constants.Acc_Search_Json) && goodsReceiptRequestParams.get(Constants.Acc_Search_Json) != null) {
//                Searchjson = goodsReceiptRequestParams.get(Constants.Acc_Search_Json).toString();
//                if (!StringUtil.isNullOrEmpty(Searchjson)) {
//                    isAdvanceSearch = true;
//                    goodsReceiptRequestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
//
//                    HashMap<String, Object> reqPar1 = new HashMap<>();
//                    reqPar1.put(Constants.companyKey, goodsReceiptRequestParams.get(Constants.companyKey));
//                    reqPar1.put(Constants.Acc_Search_Json, Searchjson);
//                    reqPar1.put(Constants.Filter_Criteria, filterConjuctionCriteria);
//                    reqPar1.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);
//                    invoiceSearchJson = fieldDataManagercntrl.getSearchJsonByModuleID(reqPar1);
//                    reqPar1.remove(Constants.moduleid);
//                    reqPar1.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
//                    receiptSearchJson = fieldDataManagercntrl.getSearchJsonByModuleID(reqPar1);
//                    reqPar1.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
//                    dnSearchJson = fieldDataManagercntrl.getSearchJsonByModuleID(reqPar1);
//                    reqPar1.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
//                    cnSearchJson = fieldDataManagercntrl.getSearchJsonByModuleID(reqPar1);
//                    reqPar1.put(Constants.moduleid, Constants.Acc_Make_Payment_ModuleId);
//                    makePaymentSearchJson = fieldDataManagercntrl.getSearchJsonByModuleID(reqPar1);
//                }
//            }
//            
//            goodsReceiptRequestParams.put("invoiceSearchJson", invoiceSearchJson);
//            goodsReceiptRequestParams.put("receiptSearchJson", receiptSearchJson);
//            goodsReceiptRequestParams.put("cnSearchJson", cnSearchJson);
//            goodsReceiptRequestParams.put("dnSearchJson", dnSearchJson);
//            goodsReceiptRequestParams.put("makePaymentSearchJson", makePaymentSearchJson);
//            goodsReceiptRequestParams.put("isAdvanceSearch", isAdvanceSearch);
//
//            if (goodsReceiptRequestParams.containsKey(Constants.start)) { //Removing paging here because it is applying on each vendor data (CN/DN etc). Due to this complete amount due not showing in AP Summary.  
//                goodsReceiptRequestParams.remove(Constants.start);
//            }
//            if (goodsReceiptRequestParams.containsKey(Constants.limit)) {
//                goodsReceiptRequestParams.remove(Constants.limit);
//            }
            Calendar oneDayBeforeCal1 = Calendar.getInstance();
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            Calendar cal8 = Calendar.getInstance();
            Calendar cal9 = Calendar.getInstance();
            Calendar cal10 = Calendar.getInstance();

//            String curDateString = (!StringUtil.isNullOrEmpty(request.getParameter("curdate"))) ? request.getParameter("curdate") : ((!StringUtil.isNullOrEmpty(request.getParameter("stdate"))) ? request.getParameter("stdate") : request.getParameter(Constants.REQ_startdate));
            String curDateString = !(StringUtil.isNullOrEmpty(request.getParameter(Constants.asOfDate))) ? request.getParameter(Constants.asOfDate) : request.getParameter(Constants.curdate);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            Date curDate = df.parse(curDateString);
            requestParams.put("df", df);
            int duration = request.getParameter("duration") == null ? 0 : Integer.parseInt(request.getParameter("duration"));
            int noOfInterval = StringUtil.isNullOrEmpty(request.getParameter("noOfInterval"))? 7 : Integer.parseInt(request.getParameter("noOfInterval"));
            
            oneDayBeforeCal1.setTime(curDate);
            cal1.setTime(curDate);
            cal2.setTime(curDate);
            cal3.setTime(curDate);
            cal4.setTime(curDate);
            cal5.setTime(curDate);
            cal6.setTime(curDate);
            cal7.setTime(curDate);
            cal8.setTime(curDate);
            cal9.setTime(curDate);
            cal10.setTime(curDate);
            oneDayBeforeCal1.add(Calendar.DAY_OF_YEAR, -1);
            cal2.add(Calendar.DAY_OF_YEAR, -duration);
            cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
            cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
            cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
            cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
            cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
            cal8.add(Calendar.DAY_OF_YEAR, -(duration * 7));
            cal9.add(Calendar.DAY_OF_YEAR, -(duration * 8));
            cal10.add(Calendar.DAY_OF_YEAR, -(duration * 9));

            Date oneDayBeforeCal1Date = null;
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;
            Date cal8Date = null;
            Date cal9Date = null;
            Date cal10Date = null;

            String oneDayBeforeCal1String = df.format(oneDayBeforeCal1.getTime());
            oneDayBeforeCal1Date = df.parse(oneDayBeforeCal1String);

            String cal1String = df.format(cal1.getTime());
            cal1Date = df.parse(cal1String);

            String cal2String = df.format(cal2.getTime());
            cal2Date = df.parse(cal2String);

            String cal3String = df.format(cal3.getTime());
            cal3Date = df.parse(cal3String);

            String cal4String = df.format(cal4.getTime());
            cal4Date = df.parse(cal4String);

            String cal5String = df.format(cal5.getTime());
            cal5Date = df.parse(cal5String);

            String cal6String = df.format(cal6.getTime());
            cal6Date = df.parse(cal6String);

            String cal7String = df.format(cal7.getTime());
            cal7Date = df.parse(cal7String);

            String cal8String = df.format(cal8.getTime());
            cal8Date = df.parse(cal8String);

            String cal9String = df.format(cal9.getTime());
            cal9Date = df.parse(cal9String);

            String cal10String = df.format(cal10.getTime());
            cal10Date = df.parse(cal10String);
            
//            goodsReceiptRequestParams.put("cntype", null);
//            goodsReceiptRequestParams.put("isAgedSummary", true);
//            if (isAgedReceivables) {
//                goodsReceiptRequestParams.put("isAgedReceivables", true);
//            }
//
//            if (isAdvanceSearch) {
//                goodsReceiptRequestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
//                goodsReceiptRequestParams.put(Constants.Acc_Search_Json, invoiceSearchJson);
//                goodsReceiptRequestParams.put(Constants.moduleid, Constants.Acc_Invoice_ModuleId);
//            }
//            requestParams.put("isBadDebtInvoices", !StringUtil.isNullOrEmpty(request.getParameter("isBadDebtInvoices")) ? Boolean.parseBoolean(request.getParameter("isBadDebtInvoices")) : false);
//            requestParams.put("ignorezero", !StringUtil.isNullOrEmpty(request.getParameter("ignorezero")) ? Boolean.parseBoolean(request.getParameter("ignorezero")) : false);
//            requestParams.put("report", !StringUtil.isNullOrEmpty(request.getParameter("report")) ? Boolean.parseBoolean(request.getParameter("report")) : false);
//            requestParams.put("isSOA", request.getAttribute("isSOA") != null ? (Boolean) request.getAttribute("isSOA") : false);
            requestParams.put("oneDayBeforeCal1", oneDayBeforeCal1);
            requestParams.put("cal1", cal1);
            requestParams.put("cal2", cal2);
            requestParams.put("cal3", cal3);
            requestParams.put("cal4", cal4);
            requestParams.put("cal5", cal5);
            requestParams.put("cal6", cal6);
            requestParams.put("cal7", cal7);
            requestParams.put("cal8", cal8);
            requestParams.put("cal9", cal9);
            requestParams.put("cal10", cal10);
            requestParams.put("cal1Date", cal1Date);
            requestParams.put("cal2Date", cal2Date);
            requestParams.put("cal3Date", cal3Date);
            requestParams.put("cal4Date", cal4Date);
            requestParams.put("cal5Date", cal5Date);
            requestParams.put("cal6Date", cal6Date);
            requestParams.put("cal7Date", cal7Date);
            requestParams.put("cal8Date", cal8Date);
            requestParams.put("cal9Date", cal9Date);
            requestParams.put("cal10Date", cal10Date);
            requestParams.put("oneDayBeforeCal1Date", oneDayBeforeCal1Date);
//            requestParams.put("onlyamountdue", !StringUtil.isNullOrEmpty(request.getParameter("onlyamountdue")) ? Boolean.parseBoolean(request.getParameter("onlyamountdue")) : false);
//            requestParams.put("globalCurrencyID", AccountingManager.getGlobalCurrencyidFromRequest(request));
//            requestParams.put("requestCompanyID", AccountingManager.getCompanyidFromRequest(request));
        } catch (ParseException ex) {
            Logger.getLogger(AccInvoiceServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return requestParams;
    }
    
    
    @Override
    public JSONArray getVendorAgedPayableMerged(HttpServletRequest request, HashMap requestParams) throws SessionExpiredException, ServiceException {
        JSONObject jObj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            requestParams.put("companyid", companyid);
            JSONObject paramObj = StringUtil.convertRequestToJsonObject(request);
            String baseCurrencyId = sessionHandlerImpl.getCurrencyID(request);
//            String curDateString = (!StringUtil.isNullOrEmpty(request.getParameter(GoodsReceiptCMNConstants.CURDATE))) ? request.getParameter(GoodsReceiptCMNConstants.CURDATE) : (request.getParameter("stdate") != null ? request.getParameter("stdate") : request.getParameter("startdate"));
            String curDateString = !(StringUtil.isNullOrEmpty(request.getParameter(Constants.asOfDate))) ? request.getParameter(Constants.asOfDate) : request.getParameter(Constants.curdate);
            // code for DocumentDesigner start
            String templatesubtype = StringUtil.isNullOrEmpty(request.getParameter("templatesubtype")) ? Constants.TEMPLATE_SUBTYPE_SOA : request.getParameter("templatesubtype");
            boolean isCallFromDD = false;
            if ( request.getAttribute("isCallFromDD") != null ) {
                isCallFromDD =  Boolean.parseBoolean(request.getAttribute("isCallFromDD").toString());
            }
            String transactionId = request.getAttribute("transactionId")==null? "" : request.getAttribute("transactionId").toString();
            if(isCallFromDD){
                requestParams.put("custVendorID", transactionId);
            }
            //report id flag
            boolean isAged = (!StringUtil.isNullOrEmpty(request.getParameter("isAged"))) ? Boolean.parseBoolean(request.getParameter("isAged")) : false;
            boolean isExportReport = (!StringUtil.isNullOrEmpty(request.getParameter("isExportReport"))) ? Boolean.parseBoolean(request.getParameter("isExportReport")) : false;
            //flag for chart
            String chartType = "";
            if(!StringUtil.isNullOrEmpty(request.getParameter(Constants.CHART_TYPE))) {
                chartType = request.getParameter(Constants.CHART_TYPE);
            }
            // code for DocumentDesigner end
            DateFormat df = authHandler.getDateOnlyFormat(request);
            Date curDate = df.parse(curDateString);
            int invoiceCount=0,jsonCount=0,openingCount=0,openingJSONCount=0;
            int duration = request.getParameter(GoodsReceiptCMNConstants.DURATION) == null ? 0 : Integer.parseInt(request.getParameter(GoodsReceiptCMNConstants.DURATION));
            int noOfInterval = Constants.DefaultNoOfIntervals;
            if(requestParams.containsKey("noOfInterval") && requestParams.get("noOfInterval") != null) {
               noOfInterval = requestParams.get("noOfInterval").toString().equals("") ? Constants.DefaultNoOfIntervals : Integer.parseInt(requestParams.get("noOfInterval").toString());
            }
            boolean isdistibutive = StringUtil.getBoolean(request.getParameter(GoodsReceiptCMNConstants.ISDISTRIBUTIVE));
            boolean showCustVendorWithZeroAmounts = StringUtil.isNullOrEmpty(request.getParameter("showCustVendorWithZeroAmounts"))?false:StringUtil.getBoolean(request.getParameter("showCustVendorWithZeroAmounts"));
//            boolean withinvent    ory = StringUtil.getBoolean(request.getParameter(GoodsReceiptCMNConstants.WITHINVENTORY));
            int datefilter = StringUtil.getInteger(request.getParameter("datefilter"));// 0 = Invoice Due date OR 1 = Invoice date
            boolean isCurrencyDetails = request.getParameter("isCurrencyDetails") != null ? Boolean.parseBoolean(request.getParameter("isCurrencyDetails")) : false;
            if (request.getAttribute("isCurrencyDetails") != null) {
                isCurrencyDetails = (Boolean) request.getAttribute("isCurrencyDetails");
            }
            DateFormat dateFormat=authHandler.getDateOnlyFormat();
            Date startDate = null;
            if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null) {
                startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? dateFormat.parse(Constants.opening_Date) : dateFormat.parse(requestParams.get(Constants.REQ_startdate).toString());
            }
            double amountdue1 = 0;
            double amountdue2 = 0;
            double amountdue3 = 0;
            double amountdue4 = 0;
            double amountdue5 = 0;
            double amountdue6 = 0;
            double amountdue7 = 0;
            double amountdue8 = 0;
            double amountdue9 = 0;
            double amountdue10 = 0;
            double amountdue11 = 0;
//            double accruedbalance = 0;

            double amountdue1Base = 0;
            double amountdue2Base = 0;
            double amountdue3Base = 0;
            double amountdue4Base = 0;
            double amountdue5Base = 0;
            double amountdue6Base = 0;
            double amountdue7Base = 0;
            double amountdue8Base = 0;
            double amountdue9Base = 0;
            double amountdue10Base = 0;
            double amountdue11Base = 0;
//            double accruedbalanceBase = 0;

            double amountdueinbase1 = 0;
            double amountdueinbase2 = 0;
            double amountdueinbase3 = 0;
            double amountdueinbase4 = 0;
            double amountdueinbase5 = 0;
            double amountdueinbase6 = 0;
            double amountdueinbase7 = 0;
            double amountdueinbase8 = 0;
            double amountdueinbase9 = 0;
            double amountdueinbase10 = 0;
            double amountdueinbase11 = 0;
//            double accruedbalanceinbase = 0;

            double totalinbase=0;
            Calendar oneDayBeforeCal1 = Calendar.getInstance();
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            Calendar cal8 = Calendar.getInstance();
            Calendar cal9 = Calendar.getInstance();
            Calendar cal10 = Calendar.getInstance();
            oneDayBeforeCal1.setTime(curDate);
            cal1.setTime(curDate);
            cal2.setTime(curDate);
            cal3.setTime(curDate);
            cal4.setTime(curDate);
            cal5.setTime(curDate);
            cal6.setTime(curDate);
            cal7.setTime(curDate);
            cal8.setTime(curDate);
            cal9.setTime(curDate);
            cal10.setTime(curDate);
            oneDayBeforeCal1.add(Calendar.DAY_OF_YEAR, -1);
            cal2.add(Calendar.DAY_OF_YEAR, -duration);
            cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
            cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
            cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
            cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
            cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
            cal8.add(Calendar.DAY_OF_YEAR, -(duration * 7));
            cal9.add(Calendar.DAY_OF_YEAR, -(duration * 8));
            cal10.add(Calendar.DAY_OF_YEAR, -(duration * 9));

            Date oneDayBeforeCal1Date = null;
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;
            Date cal8Date = null;
            Date cal9Date = null;
            Date cal10Date = null;

            String oneDayBeforeCal1String = dateFormat.format(oneDayBeforeCal1.getTime());
            oneDayBeforeCal1Date = dateFormat.parse(oneDayBeforeCal1String);

            String cal1String = dateFormat.format(cal1.getTime());
            cal1Date = dateFormat.parse(cal1String);

            String cal2String = dateFormat.format(cal2.getTime());
            cal2Date = dateFormat.parse(cal2String);

            String cal3String = dateFormat.format(cal3.getTime());
            cal3Date = dateFormat.parse(cal3String);

            String cal4String = dateFormat.format(cal4.getTime());
            cal4Date = dateFormat.parse(cal4String);

            String cal5String = dateFormat.format(cal5.getTime());
            cal5Date = dateFormat.parse(cal5String);

            String cal6String = dateFormat.format(cal6.getTime());
            cal6Date = dateFormat.parse(cal6String);

            String cal7String = dateFormat.format(cal7.getTime());
            cal7Date = dateFormat.parse(cal7String);

            String cal8String = dateFormat.format(cal8.getTime());
            cal8Date = dateFormat.parse(cal8String);

            String cal9String = dateFormat.format(cal9.getTime());
            cal9Date = dateFormat.parse(cal9String);

            String cal10String = dateFormat.format(cal10.getTime());
            cal10Date = dateFormat.parse(cal10String);

//            Calendar customizeSumryFrom = Calendar.getInstance();
//            Calendar customizeSumryTo = Calendar.getInstance();
            Calendar customizeSumryFrom1 = Calendar.getInstance();
            Calendar customizeSumryTo1 = Calendar.getInstance();
            Calendar customizeSumryFrom2 = Calendar.getInstance();
            Calendar customizeSumryTo2 = Calendar.getInstance();
            Calendar customizeSumryFrom3 = Calendar.getInstance();
            Calendar customizeSumryTo3 = Calendar.getInstance();
            Calendar customizeSumryFrom4 = Calendar.getInstance();
            Calendar customizeSumryTo4 = Calendar.getInstance();
            Calendar customizeSumryFrom5 = Calendar.getInstance();
            Calendar customizeSumryTo5 = Calendar.getInstance();
            Calendar customizeSumryFrom6 = Calendar.getInstance();
            Calendar customizeSumryTo6 = Calendar.getInstance();
            Calendar customizeSumryFrom7 = Calendar.getInstance();
            Calendar customizeSumryTo7 = Calendar.getInstance();
            boolean customizedSumryReportFlag = false;
            Map<String, String> amountDueMap = new HashMap<String, String>();
            boolean exportAgedPayables=false;
            boolean isDetailedXls = false;                   // SDP-3687 Aged Payable detailed Xls Currency wise
            boolean isDetailedPDF = false;
            if (requestParams.containsKey("exportAgedReceivables") && requestParams.get("exportAgedReceivables") != null) {
                exportAgedPayables=Boolean.parseBoolean(requestParams.get("exportAgedReceivables").toString());
            }
            boolean isAgedPayables=false;
            if (requestParams.containsKey("isAgedPayables") && requestParams.get("isAgedPayables") != null) {
                isAgedPayables=Boolean.parseBoolean(requestParams.get("isAgedPayables").toString());
            }
            if (requestParams.containsKey("detailedXls") && requestParams.get("detailedXls") != null) {
                isDetailedXls=Boolean.parseBoolean(requestParams.get("detailedXls").toString());
            }
            if (requestParams.containsKey("detailedPDF") && requestParams.get("detailedPDF") != null) {
                isDetailedPDF=(Boolean)requestParams.get("detailedPDF");
            }
            
            String linedetails = isDetailedXls?"_line":"";
            String customizedSumryReportFlagStr = request.getParameter("customizedSummaryReportFlag");
            if (!StringUtil.isNullOrEmpty(customizedSumryReportFlagStr)) {
                customizedSumryReportFlag = Boolean.parseBoolean(customizedSumryReportFlagStr);
                String fromDuration = null;
                String toDuration = null;
                List<String> fromDurationArr = new ArrayList<String>();// request.getParameter("fromDuration").split(",");
                List<String> toDurationArr = new ArrayList<String>();;// request.getParameter("toDuration").split(",");
//                String fromDurationArr[]= request.getParameter("fromDuration").split(",");
//                String toDurationArr[] = request.getParameter("toDuration").split(",");
                JSONArray fromDurationjson = new JSONArray(request.getParameter("fromDuration"));
                JSONArray toDurationjson = new JSONArray(request.getParameter("toDuration"));
                //String fromDurationArr1[]= fromDurationjson.toString().split(",");
                for (int i = 0; i < fromDurationjson.length(); i++) {
                    JSONObject object1 = new JSONObject(fromDurationjson.getString(i));
                    fromDurationArr.add(object1.getString("id"));
                    amountDueMap.put(object1.getString("amountdueindex"), object1.getString("amountdue"));
                    JSONObject object2 = new JSONObject(toDurationjson.getString(i));
                    toDurationArr.add(object2.getString("id"));


                }
                for (int i = 0; i < fromDurationArr.size(); i++) {
                    fromDuration = fromDurationArr.get(i);
                    toDuration = toDurationArr.get(i);
                    switch (i + 1) {
                        case 1:
                            if (!StringUtil.isNullOrEmpty(fromDuration) && !StringUtil.isNullOrEmpty(toDuration)) {
                                int fromDur = Integer.parseInt(fromDuration);
                                int toDur = Integer.parseInt(toDuration);
                                customizeSumryFrom1.setTime(curDate);
                                customizeSumryTo1.setTime(curDate);
                                customizeSumryFrom1.add(Calendar.DAY_OF_YEAR, -fromDur);
                                customizeSumryTo1.add(Calendar.DAY_OF_YEAR, -toDur);
                            }
                            break;
                        case 2:
                            if (!StringUtil.isNullOrEmpty(fromDuration) && !StringUtil.isNullOrEmpty(toDuration)) {
                                int fromDur = Integer.parseInt(fromDuration);
                                int toDur = Integer.parseInt(toDuration);
                                customizeSumryFrom2.setTime(curDate);
                                customizeSumryTo2.setTime(curDate);
                                customizeSumryFrom2.add(Calendar.DAY_OF_YEAR, -fromDur);
                                customizeSumryTo2.add(Calendar.DAY_OF_YEAR, -toDur);
                            }
                            break;
                        case 3:
                            if (!StringUtil.isNullOrEmpty(fromDuration) && !StringUtil.isNullOrEmpty(toDuration)) {
                                int fromDur = Integer.parseInt(fromDuration);
                                int toDur = Integer.parseInt(toDuration);
                                customizeSumryFrom3.setTime(curDate);
                                customizeSumryTo3.setTime(curDate);
                                customizeSumryFrom3.add(Calendar.DAY_OF_YEAR, -fromDur);
                                customizeSumryTo3.add(Calendar.DAY_OF_YEAR, -toDur);
                            }
                            break;
                        case 4:
                            if (!StringUtil.isNullOrEmpty(fromDuration) && !StringUtil.isNullOrEmpty(toDuration)) {
                                int fromDur = Integer.parseInt(fromDuration);
                                int toDur = Integer.parseInt(toDuration);
                                customizeSumryFrom4.setTime(curDate);
                                customizeSumryTo4.setTime(curDate);
                                customizeSumryFrom4.add(Calendar.DAY_OF_YEAR, -fromDur);
                                customizeSumryTo4.add(Calendar.DAY_OF_YEAR, -toDur);
                            }
                            break;
                        case 5:
                            if (!StringUtil.isNullOrEmpty(fromDuration) && !StringUtil.isNullOrEmpty(toDuration)) {
                                int fromDur = Integer.parseInt(fromDuration);
                                int toDur = Integer.parseInt(toDuration);
                                customizeSumryFrom5.setTime(curDate);
                                customizeSumryTo5.setTime(curDate);
                                customizeSumryFrom5.add(Calendar.DAY_OF_YEAR, -fromDur);
                                customizeSumryTo5.add(Calendar.DAY_OF_YEAR, -toDur);
                            }
                            break;
                        case 6:
                            if (!StringUtil.isNullOrEmpty(fromDuration) && !StringUtil.isNullOrEmpty(toDuration)) {
                                int fromDur = Integer.parseInt(fromDuration);
                                int toDur = Integer.parseInt(toDuration);
                                customizeSumryFrom6.setTime(curDate);
                                customizeSumryTo6.setTime(curDate);
                                customizeSumryFrom6.add(Calendar.DAY_OF_YEAR, -fromDur);
                                customizeSumryTo6.add(Calendar.DAY_OF_YEAR, -toDur);
                            }
                            break;
                        case 7:
                            if (!StringUtil.isNullOrEmpty(fromDuration) && !StringUtil.isNullOrEmpty(toDuration)) {
                                int fromDur = Integer.parseInt(fromDuration);
                                int toDur = Integer.parseInt(toDuration);
                                customizeSumryFrom7.setTime(curDate);
                                customizeSumryTo7.setTime(curDate);
                                customizeSumryFrom7.add(Calendar.DAY_OF_YEAR, -fromDur);
                                customizeSumryTo7.add(Calendar.DAY_OF_YEAR, -toDur);
                            }
                            break;


                    }
                }

            }

            Date customizeSumryFrom1Date = null;
            Date customizeSumryTo1Date = null;
            Date customizeSumryFrom2Date = null;
            Date customizeSumryTo2Date = null;
            Date customizeSumryFrom3Date = null;
            Date customizeSumryTo3Date = null;
            Date customizeSumryFrom4Date = null;
            Date customizeSumryTo4Date = null;
            Date customizeSumryFrom5Date = null;
            Date customizeSumryTo5Date = null;
            Date customizeSumryFrom6Date = null;
            Date customizeSumryTo6Date = null;
            Date customizeSumryFrom7Date = null;
            Date customizeSumryTo7Date = null;

            String customizeSumryFrom1String = dateFormat.format(customizeSumryFrom1.getTime());
            customizeSumryFrom1Date = dateFormat.parse(customizeSumryFrom1String);
            String customizeSumryTo1String = dateFormat.format(customizeSumryTo1.getTime());
            customizeSumryTo1Date = dateFormat.parse(customizeSumryTo1String);

            String customizeSumryFrom2String = dateFormat.format(customizeSumryFrom2.getTime());
            customizeSumryFrom2Date = dateFormat.parse(customizeSumryFrom2String);
            String customizeSumryTo2String = dateFormat.format(customizeSumryTo2.getTime());
            customizeSumryTo2Date = dateFormat.parse(customizeSumryTo2String);

            String customizeSumryFrom3String = dateFormat.format(customizeSumryFrom3.getTime());
            customizeSumryFrom3Date = dateFormat.parse(customizeSumryFrom3String);
            String customizeSumryTo3String = dateFormat.format(customizeSumryTo3.getTime());
            customizeSumryTo3Date = dateFormat.parse(customizeSumryTo3String);

            String customizeSumryFrom4String = dateFormat.format(customizeSumryFrom4.getTime());
            customizeSumryFrom4Date = dateFormat.parse(customizeSumryFrom4String);
            String customizeSumryTo4String = dateFormat.format(customizeSumryTo4.getTime());
            customizeSumryTo4Date = dateFormat.parse(customizeSumryTo4String);

            String customizeSumryFrom5String = dateFormat.format(customizeSumryFrom5.getTime());
            customizeSumryFrom5Date = dateFormat.parse(customizeSumryFrom5String);
            String customizeSumryTo5String = dateFormat.format(customizeSumryTo5.getTime());
            customizeSumryTo5Date = dateFormat.parse(customizeSumryTo5String);

            String customizeSumryFrom6String = dateFormat.format(customizeSumryFrom6.getTime());
            customizeSumryFrom6Date = dateFormat.parse(customizeSumryFrom6String);
            String customizeSumryTo6String = dateFormat.format(customizeSumryTo6.getTime());
            customizeSumryTo6Date = dateFormat.parse(customizeSumryTo6String);

            String customizeSumryFrom7String = dateFormat.format(customizeSumryFrom7.getTime());
            customizeSumryFrom7Date = dateFormat.parse(customizeSumryFrom7String);
            String customizeSumryTo7String = dateFormat.format(customizeSumryTo7.getTime());
            customizeSumryTo7Date = dateFormat.parse(customizeSumryTo7String);

            //Advance Search releated common code  
            boolean isAdvanceSearch = false;
            String Searchjson = "";
            String invoiceSearchJson = "";
            String receiptSearchJson = "";
            String cnSearchJson = "";
            String dnSearchJson = "";
            String makePaymentSearchJson = "";
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (requestParams.containsKey(Constants.Filter_Criteria) && requestParams.get(Constants.Filter_Criteria) != null) {
                if (requestParams.get(Constants.Filter_Criteria).toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            if (isAged && requestParams.containsKey(Constants.Acc_Search_Json) && requestParams.get(Constants.Acc_Search_Json) != null) {
                Searchjson = requestParams.get(Constants.Acc_Search_Json).toString();
                if (!StringUtil.isNullOrEmpty(Searchjson)) {
                    isAdvanceSearch = true;
                    requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));

                    HashMap<String, Object> reqPar1 = new HashMap<>();
                    reqPar1.put(Constants.companyKey, requestParams.get(Constants.companyKey));
                    reqPar1.put(Constants.Acc_Search_Json, Searchjson);
                    reqPar1.put(Constants.Filter_Criteria, filterConjuctionCriteria);
                    reqPar1.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);
                    invoiceSearchJson = fieldDataManagercntrl.getSearchJsonByModuleID(reqPar1);
                    reqPar1.remove(Constants.moduleid);
                    reqPar1.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                    receiptSearchJson = fieldDataManagercntrl.getSearchJsonByModuleID(reqPar1);
                    reqPar1.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
                    dnSearchJson = fieldDataManagercntrl.getSearchJsonByModuleID(reqPar1);
                    reqPar1.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                    cnSearchJson = fieldDataManagercntrl.getSearchJsonByModuleID(reqPar1);
                    reqPar1.put(Constants.moduleid, Constants.Acc_Make_Payment_ModuleId);
                    makePaymentSearchJson = fieldDataManagercntrl.getSearchJsonByModuleID(reqPar1);
                }
            }

            KwlReturnObject result = accVendorDAOobj.getVendorAndCurrencyDetailsForAgedPayable(requestParams);
            if (requestParams.containsKey("start")) { //Removing paging here because it is applying on each vendor data (CN/DN etc). Due to this complete amount due not showing in AP Summary.  
                requestParams.remove("start");
            }
            if (requestParams.containsKey("limit")) {
                requestParams.remove("limit");
            }
            requestParams.put("oneDayBeforeCal1", oneDayBeforeCal1);
            requestParams.put("cal1", cal1);
            requestParams.put("cal2", cal2);
            requestParams.put("cal3", cal3);
            requestParams.put("cal4", cal4);
            requestParams.put("cal5", cal5);
            requestParams.put("cal6", cal6);
            requestParams.put("cal7", cal7);
            requestParams.put("cal8", cal8);
            requestParams.put("cal9", cal9);
            requestParams.put("cal10", cal10);
            if (isAgedPayables) {
                requestParams.put("isAgedPayables", isAgedPayables);
            }
            double totalAmountDueInBase1 = 0.0;
            double totalAmountDueInBase2 = 0.0;
            double totalAmountDueInBase3 = 0.0;
            double totalAmountDueInBase4 = 0.0;
            double totalAmountDueInBase5 = 0.0;
            double totalAmountDueInBase6 = 0.0;
            double totalAmountDueInBase7 = 0.0;
            double totalAmountDueInBase8 = 0.0;
            double totalAmountDueInBase9 = 0.0;
            double totalAmountDueInBase10 = 0.0;
            double totalAmountDueInBase11 = 0.0;
                requestParams.put("cntype", null);
                requestParams.put("isAgedSummary", true);
//
                if (isAdvanceSearch) {
                    requestParams.put(Constants.Acc_Search_Json, invoiceSearchJson);
                    requestParams.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);
                    requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
                }
                JSONArray allTransaction = new JSONArray();
                result = accGoodsReceiptobj.getGoodsReceiptsMerged(requestParams);
                invoiceCount+=result.getEntityList().size();
                if(result!=null && !result.getEntityList().isEmpty()){
                    allTransaction = accGoodsReceiptServiceHandler.getGoodsReceiptsJsonForAgedPayables(requestParams, result.getEntityList(), allTransaction, accountingHandlerDAOobj, accCurrencyDAOobj, accGoodsReceiptobj, accAccountDAOobj, accGoodsReceiptCommon, accTaxObj);
                    jsonCount+=allTransaction.length();
                }
                if (isAdvanceSearch) {
                    requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
                }
                JSONArray OBJArryInvoice = new JSONArray();
                result = accGoodsReceiptobj.getOpeningBalanceInvoices(requestParams);
                openingCount+=result.getEntityList().size();
                if (result != null && result.getEntityList()!=null && !result.getEntityList().isEmpty()) {
                    OBJArryInvoice = AccGoodsReceiptServiceHandler.getAgedPayablesOpeningBalanceInvoiceJson(requestParams, result.getEntityList(), OBJArryInvoice, accCurrencyDAOobj, accountingHandlerDAOobj, accGoodsReceiptCommon);
                    openingJSONCount+=OBJArryInvoice.length();
                    for (int i = 0; i < OBJArryInvoice.length(); i++) {
                        allTransaction.put(OBJArryInvoice.get(i));
                    }
                }

                if (!templatesubtype.equals(Constants.TEMPLATE_SUBTYPE_SOI)) {
                    if (isAdvanceSearch) {
                        requestParams.put(Constants.Acc_Search_Json, dnSearchJson);
                        requestParams.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
                        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
                    }
                    JSONArray OBJArryDebitNote = new JSONArray();
                    result = accDebitNoteobj.getOpeningBalanceDNs(requestParams);
                    if (result != null &&  result.getEntityList()!=null && !result.getEntityList().isEmpty()) {
                    OBJArryDebitNote = AccGoodsReceiptServiceHandler.getAgedOpeningBalanceDebitNoteJson(requestParams, result.getEntityList(), OBJArryDebitNote, accCurrencyDAOobj, accountingHandlerDAOobj, accReceiptDAOobj,accDebitNoteobj,accAccountDAOobj, fieldDataManagercntrl);
                        for (int i = 0; i < OBJArryDebitNote.length(); i++) {
                            allTransaction.put(OBJArryDebitNote.get(i));
                        }
                    }
                }

                if (!templatesubtype.equals(Constants.TEMPLATE_SUBTYPE_SOI)) {
                    if (isAdvanceSearch) {
                        requestParams.put(Constants.Acc_Search_Json, cnSearchJson);
                        requestParams.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
                    }
                    JSONArray OBJArryCreditNote = new JSONArray();
                    result = accCreditNoteDAOobj.getOpeningBalanceVendorCNs(requestParams);
                    if (result != null && result.getEntityList()!=null &&  !result.getEntityList().isEmpty()) {
                    OBJArryCreditNote = AccGoodsReceiptServiceHandler.getAgedOpeningBalanceCreditNoteJson(requestParams, result.getEntityList(), OBJArryCreditNote, accCurrencyDAOobj, accountingHandlerDAOobj, accPaymentDAOobj,accVendorPaymentobj,accDebitNoteobj,accAccountDAOobj, fieldDataManagercntrl);
                        for (int i = 0; i < OBJArryCreditNote.length(); i++) {
                            allTransaction.put(OBJArryCreditNote.get(i));
                        }
                    }
                }

                if (!templatesubtype.equals(Constants.TEMPLATE_SUBTYPE_SOI)) {
                    if (isAdvanceSearch) {
                        requestParams.put(Constants.Acc_Search_Json, makePaymentSearchJson);
                        requestParams.put(Constants.moduleid, Constants.Acc_Make_Payment_ModuleId);
                        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
                    }
                    JSONArray OBJArryPayment = new JSONArray();
                    result = accVendorPaymentobj.getOpeningBalancePayments(requestParams);
                    if (result != null &&  result.getEntityList()!=null && !result.getEntityList().isEmpty()) {
                        OBJArryPayment = AccGoodsReceiptServiceHandler.getAgedOpeningBalancePaymentJson(requestParams, result.getEntityList(), OBJArryPayment, accCurrencyDAOobj, accountingHandlerDAOobj, accVendorPaymentobj, accAccountDAOobj, fieldDataManagercntrl);
                        for (int i = 0; i < OBJArryPayment.length(); i++) {
                            allTransaction.put(OBJArryPayment.get(i));
                        }
                    }
                }

                if (!templatesubtype.equals(Constants.TEMPLATE_SUBTYPE_SOI)) {
                    if (isAdvanceSearch) {
                        requestParams.put(Constants.Acc_Search_Json, dnSearchJson);
                        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
                    }
                    JSONArray DebitNotejArr = new JSONArray();//This is used for getting DN gainst vendor and otherwise 
                    result = accDebitNoteobj.getDebitNoteMerged(requestParams);
                    if (result != null && result.getEntityList()!=null &&  !result.getEntityList().isEmpty()) {
                    DebitNotejArr = AccGoodsReceiptServiceHandler.getDebitNotesMergedJson(requestParams, result.getEntityList(), DebitNotejArr, accCurrencyDAOobj, accountingHandlerDAOobj, accReceiptDAOobj,accDebitNoteobj,accAccountDAOobj, fieldDataManagercntrl, accJournalEntryobj);
                        for (int i = 0; i < DebitNotejArr.length(); i++) {
                            allTransaction.put(DebitNotejArr.get(i));
                        }
                    }
                }

                if (!templatesubtype.equals(Constants.TEMPLATE_SUBTYPE_SOI)) {
                    if (isAdvanceSearch) {
                        requestParams.put(Constants.Acc_Search_Json, cnSearchJson);
                        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
                    }
                    requestParams.put("cntype", 4);//This is used for getting Credit note against vendor 
                    JSONArray CreditNotejArr = new JSONArray();
                    result = accCreditNoteDAOobj.getCreditNoteMerged(requestParams);
                    if (result != null && result.getEntityList()!=null &&  !result.getEntityList().isEmpty()) {
                    CreditNotejArr = AccGoodsReceiptServiceHandler.getCreditNotesMergedJson(requestParams, result.getEntityList(), CreditNotejArr, accCurrencyDAOobj, accountingHandlerDAOobj, accPaymentDAOobj,accVendorPaymentobj,accDebitNoteobj,accAccountDAOobj, fieldDataManagercntrl, accJournalEntryobj);
                        for (int i = 0; i < CreditNotejArr.length(); i++) {
                            allTransaction.put(CreditNotejArr.get(i));
                        }
                    }
                }

                if (!templatesubtype.equals(Constants.TEMPLATE_SUBTYPE_SOI)) {
                    if (isAdvanceSearch) {
                        requestParams.put(Constants.Acc_Search_Json, makePaymentSearchJson);
                        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
                    }
                    JSONArray makePaymentJArr = new JSONArray();
                    requestParams.put("allAdvPayment", true); // needs only Advance type record so that putted true
                    requestParams.put("paymentWindowType", 1);//Payment to Vendor record
                    result = accVendorPaymentobj.getPayments(requestParams);
                    if (result != null && result.getEntityList()!=null &&  !result.getEntityList().isEmpty()) {
                        makePaymentJArr = AccGoodsReceiptServiceHandler.getPaymentsJson(requestParams, result.getEntityList(), makePaymentJArr, accCurrencyDAOobj, accountingHandlerDAOobj, accVendorPaymentobj);
                        for (int i = 0; i < makePaymentJArr.length(); i++) {
                            allTransaction.put(makePaymentJArr.get(i));
                        }
                    }
                }
                
                if (!templatesubtype.equals(Constants.TEMPLATE_SUBTYPE_SOI)) {
                    if (isAdvanceSearch) {
                        requestParams.put(Constants.Acc_Search_Json, receiptSearchJson);
                        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
                    }
                    if (!isAgedPayables && isCurrencyDetails) { //need to put requestParams for this condition
                        requestParams.put("isAgedPayables", true);
                    }
                    requestParams.put("allAdvPayment", true); // needs only refund type record so that putted true
                    requestParams.put("paymentWindowType", 2);//Receipt to Vendor record
                    JSONArray receivePaymentJArr = new JSONArray();
                    result = accReceiptDAOobj.getReceipts(requestParams);
                    receivePaymentJArr = AccInvoiceServiceHandler.getReceiptsJson(requestParams, result.getEntityList(), receivePaymentJArr, accountingHandlerDAOobj, authHandlerDAOObj, accCurrencyDAOobj, accReceiptDAOobj, request, accAccountDAOobj, fieldDataManagercntrl, accJournalEntryobj);
                    for (int i = 0; i < receivePaymentJArr.length(); i++) {
                        allTransaction.put(receivePaymentJArr.get(i));
                    }
                  requestParams.remove("allAdvPayment");
                requestParams.remove("paymentWindowType");
                if (!isAgedPayables && isCurrencyDetails) { //need to remove requestParams for this condition as we have putted above
                    requestParams.remove("isAgedPayables");
                }
                }
                String personInfo=null;
                String personID = null;
                String personName = null;
                String personcode = null;
                String aliasname = "";
                String currencySymbol=null;
                String currencyid=null;
                boolean isDN=false;
                boolean isMP=false;    
                String currencyidVen = null;
                String currencyName = null;
                String currencySymbolVen = null;
                String vendorDebitTerm = "";
                String sort = "";
                String dir = "";
                Map<String, JSONArray> jArrMap;
                if (!StringUtil.isNullOrEmpty(request.getParameter("dir")) && !StringUtil.isNullOrEmpty(request.getParameter("sort"))) {
                    dir = request.getParameter("dir");
                    sort = request.getParameter("sort");
                }
            if (sort.equals("code") && !(sort.equals(""))) {
                jArrMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(allTransaction, InvoiceConstants.code, dir);
            } else {
                jArrMap = AccountingManager.getSortedArrayMapBasedOnJSONAttribute(allTransaction, InvoiceConstants.personinfo, dir);
            }
                 
                for (String key : jArrMap.keySet()) {

                    JSONArray invjarr = jArrMap.get(key);
                    if (invjarr != null && invjarr.length() > 0) {
                        Vendor vendor = null;
                        amountdue1 = amountdue2 = amountdue3 = amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                        amountdue1Base = amountdue2Base = amountdue3Base = amountdue4Base = amountdue5Base = amountdue6Base = amountdue7Base = amountdue8Base = amountdue9Base = amountdue10Base = amountdue11Base = 0;
                        personID = invjarr.getJSONObject(0).getString(GoodsReceiptCMNConstants.PERSONID);
                        personInfo = invjarr.getJSONObject(0).getString(GoodsReceiptCMNConstants.PERSONINFO);
                        String code = invjarr.getJSONObject(0).optString(GoodsReceiptCMNConstants.CODE,"");
                        if(vendor == null){
                            KwlReturnObject kwlObj = kwlCommonTablesDAOObj.getObject(Vendor.class.getName(), personID);
                            vendor = (Vendor) kwlObj.getEntityList().get(0);
                            currencyidVen = vendor.getAccount().getCurrency().getCurrencyID();
                            currencySymbolVen = vendor.getAccount().getCurrency().getSymbol();
                            currencyName = vendor.getAccount().getCurrency().getName();
                        }

                        // Get exchange rate of base to vendor currency
                        double vendorCurrencyExchangeRate = 1;

                        String gCurrencyID = AccountingManager.getGlobalCurrencyidFromRequest(request);
                        String vendorCurrencyID = vendor.getCurrency().getCurrencyID();
                        String vendorAgentName[] = accVendorDAOobj.getMultiAgents(vendor.getID());
                        if (gCurrencyID.equals(vendorCurrencyID)) {
                            vendorCurrencyExchangeRate = 1;
                        } else {
                            HashMap<String, Object> currencyParams = new HashMap<>();
                            currencyParams.put("fromcurrencyid", gCurrencyID);
                            currencyParams.put("tocurrencyid", vendorCurrencyID);
                            currencyParams.put(Constants.companyid, companyid);
                            currencyParams.put("transactiondate", new Date());
                            KwlReturnObject exchResult = accCurrencyDAOobj.getCurrencyExchange(currencyParams);
                            ExchangeRate er = (ExchangeRate) exchResult.getEntityList().get(0);
                            KwlReturnObject erdresult = accCurrencyDAOobj.getExcDetailID(currencyParams, vendorCurrencyID, vendor.getCreatedOn(), er.getID());
                            if (erdresult.getEntityList() != null && !erdresult.getEntityList().isEmpty() && erdresult.getEntityList().size() > 1) {
                                ExchangeRateDetails erd = (ExchangeRateDetails) erdresult.getEntityList().get(0);
                                if (erd != null) {
                                    vendorCurrencyExchangeRate = erd.getExchangeRate();
                                }
                            }
                        }

                        if (!isCurrencyDetails) {
                            for (int i = 0; i < invjarr.length(); i++) {
                                JSONObject invobj = invjarr.getJSONObject(i);
                                personID = invobj.getString(GoodsReceiptCMNConstants.PERSONID);
                                personName = invobj.getString(GoodsReceiptCMNConstants.PERSONNAME);
                                personcode = invobj.optString(GoodsReceiptCMNConstants.CODE, "");
                                aliasname = invobj.has(GoodsReceiptCMNConstants.ALIASNAME) ? (invobj.getString(GoodsReceiptCMNConstants.ALIASNAME) != null ? invobj.getString(GoodsReceiptCMNConstants.ALIASNAME) : "") : "";
                                currencySymbol = invobj.getString(GoodsReceiptCMNConstants.CURRENCYSYMBOL);
                                currencyid = invobj.getString(GoodsReceiptCMNConstants.CURRENCYID);
                                vendorDebitTerm = invobj.optString(GoodsReceiptCMNConstants.CustomerCreditTerm, "");
                                isDN = invobj.has("isDN") ? invobj.getBoolean("isDN") : false;
                                isMP = invobj.has("isMP") ? invobj.getBoolean("isMP") : false;
                                Date dueDate = null;
                                if (!StringUtil.isNullOrEmpty(invobj.getString(GoodsReceiptCMNConstants.DUEDATE))) {
                                    dueDate = authHandler.getDateOnlyFormat(request).parse(invobj.getString(GoodsReceiptCMNConstants.DUEDATE));
                                }

                                if (datefilter == 0 || datefilter == Constants.agedDueDate0to30Filter) {
                                    dueDate = authHandler.getDateOnlyFormat(request).parse(invobj.getString(GoodsReceiptCMNConstants.DUEDATE));
                                } else {
                                    dueDate = authHandler.getDateOnlyFormat(request).parse(invobj.getString(GoodsReceiptCMNConstants.DATE));
                                }

                                double amountdue = invobj.has(GoodsReceiptCMNConstants.AMOUNTDUE) ? invobj.getDouble(GoodsReceiptCMNConstants.AMOUNTDUE) : 0;
                                double amountdueinbase = invobj.has(GoodsReceiptCMNConstants.AMOUNTDUEINBASE) ? invobj.getDouble(GoodsReceiptCMNConstants.AMOUNTDUEINBASE) : 0;
                                double externalcurrencyrate = invobj.getDouble(GoodsReceiptCMNConstants.EXTERNALCURRENCYRATE);

                                boolean isopeningBalanceTransaction = invobj.optBoolean(GoodsReceiptCMNConstants.IsopeningBalanceTransaction, false);
                                boolean isConversionRateFromCurrencyToBase = invobj.optBoolean(GoodsReceiptCMNConstants.IsConversionRateFromCurrencyToBase, false);
                                if (baseCurrencyId.equals(currencyidVen)) {
                                    amountdue = amountdueinbase;
                                } else {
                                    KwlReturnObject bAmt = null;
                                    if (isopeningBalanceTransaction && isConversionRateFromCurrencyToBase) {
                                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, currencyid, currencyidVen, df.parse(invobj.getString(GoodsReceiptCMNConstants.DATE)), externalcurrencyrate);
                                    } else {
                                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, currencyid, currencyidVen, df.parse(invobj.getString(GoodsReceiptCMNConstants.DATE)), externalcurrencyrate);
                                    }
                                    amountdue = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                                }

                                if (isDN || isMP) {
                                    if (!isopeningBalanceTransaction) {
                                        amountdue = -amountdue;
                                        amountdueinbase = -amountdueinbase;
                                    }
                                }

                                if (customizedSumryReportFlag) {
                                    if ((dueDate.after(customizeSumryTo1Date) || dueDate.equals(customizeSumryTo1Date)) && (dueDate.before(customizeSumryFrom1Date) || dueDate.equals(customizeSumryFrom1Date))) {
                                        amountdue1 += amountdue;
                                        amountdue1Base += amountdueinbase;
                                    } else if ((dueDate.after(customizeSumryTo2Date) || dueDate.equals(customizeSumryTo2Date)) && (dueDate.before(customizeSumryFrom2Date) || dueDate.equals(customizeSumryFrom2Date))) {
                                        amountdue2 += amountdue;
                                        amountdue2Base += amountdueinbase;
                                    } else if ((dueDate.after(customizeSumryTo3Date) || dueDate.equals(customizeSumryTo3Date)) && (dueDate.before(customizeSumryFrom3Date) || dueDate.equals(customizeSumryFrom3Date))) {
                                        amountdue3 += amountdue;
                                        amountdue3Base += amountdueinbase;
                                    } else if ((dueDate.after(customizeSumryTo4Date) || dueDate.equals(customizeSumryTo4Date)) && (dueDate.before(customizeSumryFrom4Date) || dueDate.equals(customizeSumryFrom4Date))) {
                                        amountdue4 += amountdue;
                                        amountdue4Base += amountdueinbase;
                                    } else if ((dueDate.after(customizeSumryTo5Date) || dueDate.equals(customizeSumryTo5Date)) && (dueDate.before(customizeSumryFrom5Date) || dueDate.equals(customizeSumryFrom5Date))) {
                                        amountdue5 += amountdue;
                                        amountdue5Base += amountdueinbase;
                                    } else if ((dueDate.after(customizeSumryTo6Date) || dueDate.equals(customizeSumryTo6Date)) && (dueDate.before(customizeSumryFrom6Date) || dueDate.equals(customizeSumryFrom6Date))) {
                                        amountdue6 += amountdue;
                                        amountdue6Base += amountdueinbase;
                                    } else if ((dueDate.after(customizeSumryTo7Date) || dueDate.equals(customizeSumryTo7Date)) && (dueDate.before(customizeSumryFrom7Date) || dueDate.equals(customizeSumryFrom7Date))) {
                                        amountdue7 += amountdue;
                                        amountdue7Base += amountdueinbase;
                                    }
                                } else {
                                    if (isdistibutive) {

//                                        if (startDate != null && dueDate.before(startDate)) {
//                                            accruedbalance += amountdue;
//                                            accruedbalanceBase += amountdueinbase;
//                                        } else 
                                            
                                        if (dueDate.after(oneDayBeforeCal1Date)) {
                                            if (dueDate.equals(cal1Date) && (datefilter == Constants.agedDueDate0to30Filter || datefilter == Constants.agedInvoiceDate0to30Filter)) {
                                                amountdue2 += amountdue;
                                                amountdue2Base += amountdueinbase;
                                            } else {
                                                amountdue1 += amountdue;
                                                amountdue1Base += amountdueinbase;
                                            }
                                        } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) {
                                            amountdue2 += amountdue;
                                            amountdue2Base += amountdueinbase;
                                        } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) {
                                            amountdue3 += amountdue;
                                            amountdue3Base += amountdueinbase;
                                        } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) {
                                            amountdue4 += amountdue;
                                            amountdue4Base += amountdueinbase;
                                        } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) {
                                            amountdue5 += amountdue;
                                            amountdue5Base += amountdueinbase;
                                        } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) {
                                            amountdue6 += amountdue;
                                            amountdue6Base += amountdueinbase;
                                        } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) {
                                            amountdue7 += amountdue;
                                            amountdue7Base += amountdueinbase;
                                        } else if ((cal8Date.before(dueDate) || cal8Date.equals(dueDate)) && cal7Date.after(dueDate)) {
                                            amountdue8 += amountdue;
                                            amountdue8Base += amountdueinbase;
                                        } else if ((cal9Date.before(dueDate) || cal9Date.equals(dueDate)) && cal8Date.after(dueDate)) {
                                            amountdue9 += amountdue;
                                            amountdue9Base += amountdueinbase;
                                        } else if ((cal10Date.before(dueDate) || cal10Date.equals(dueDate)) && cal9Date.after(dueDate)) {
                                            amountdue10 += amountdue;
                                            amountdue10Base += amountdueinbase;
                                        }  else {
                                            amountdue11 += amountdue;
                                            amountdue11Base += amountdueinbase;
                                        }
                                        
                                        switch(noOfInterval){
                                            case 2:
                                                amountdue3 += amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                                amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                                amountdue3Base += amountdue4Base + amountdue5Base + amountdue6Base + amountdue7Base + amountdue8Base + amountdue9Base + amountdue10Base + amountdue11Base;
                                                amountdue4Base = amountdue5Base = amountdue6Base = amountdue7Base = amountdue8Base = amountdue9Base = amountdue10Base = amountdue11Base = 0;
                                                break;
                                            case 3:
                                                amountdue4 += amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                                amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                                amountdue4Base += amountdue5Base + amountdue6Base + amountdue7Base + amountdue8Base + amountdue9Base + amountdue10Base + amountdue11Base;
                                                amountdue5Base = amountdue6Base = amountdue7Base = amountdue8Base = amountdue9Base = amountdue10Base = amountdue11Base = 0;
                                                break;
                                            case 4:
                                                amountdue5 += amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                                amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                                amountdue5Base += amountdue6Base + amountdue7Base + amountdue8Base + amountdue9Base + amountdue10Base + amountdue11Base;
                                                amountdue6Base = amountdue7Base = amountdue8Base = amountdue9Base = amountdue10Base = amountdue11Base = 0;
                                                break;
                                            case 5:
                                                amountdue6 += amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                                amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                                amountdue6Base += amountdue7Base + amountdue8Base + amountdue9Base + amountdue10Base + amountdue11Base;
                                                amountdue7Base = amountdue8Base = amountdue9Base = amountdue10Base = amountdue11Base = 0;
                                                break;
                                            case 6:
                                                amountdue7 += amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                                amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                                amountdue7Base += amountdue8Base + amountdue9Base + amountdue10Base + amountdue11Base;
                                                amountdue8Base = amountdue9Base = amountdue10Base = amountdue11Base = 0;
                                                break;
                                            case 7:
                                                amountdue8 += amountdue9 + amountdue10 + amountdue11;
                                                amountdue9 = amountdue10 = amountdue11 = 0;
                                                amountdue8Base += amountdue9Base + amountdue10Base + amountdue11Base;
                                                amountdue9Base = amountdue10Base = amountdue11Base = 0;
                                                break;
                                            case 8:
                                                amountdue9 += amountdue10 + amountdue11;
                                                amountdue10 = amountdue11 = 0;
                                                amountdue9Base += amountdue10Base + amountdue11Base;
                                                amountdue10Base = amountdue11Base = 0;
                                                break;
                                            case 9:
                                                amountdue10 += amountdue11;
                                                amountdue11 = 0;
                                                amountdue10Base += amountdue11Base;
                                                amountdue11Base = 0;
                                                break;
                                        }
                                    } else {
//                                        if (startDate != null && dueDate.before(startDate)) {
////                                            accruedbalance += amountdue;
////                                            accruedbalanceBase += amountdueinbase;
//                                        } else 
                                        if (dueDate.after(oneDayBeforeCal1Date) && (dueDate.before(cal1Date) || dueDate.equals(cal1Date))) {
                                            amountdue1 += amountdue;
                                            amountdue1Base += amountdueinbase;
                                        } else if (dueDate.after(cal2Date) || dueDate.equals(cal2Date)) {
                                            amountdue2 += amountdue;
                                            amountdue2Base += amountdueinbase;
                                        } else if (dueDate.after(cal3Date) || dueDate.equals(cal3Date)) {
                                            amountdue3 += amountdue;
                                            amountdue3Base += amountdueinbase;
                                        } else {
                                            amountdue4 += amountdue;
                                            amountdue4Base += amountdueinbase;
                                        }
                                    }
                                }
                            }
                            if (invjarr.length() > 0) {
                                jObj = new JSONObject();
                                jObj.put(GoodsReceiptCMNConstants.PERSONID, personID);
                                jObj.put(GoodsReceiptCMNConstants.PERSONINFO, personInfo);
                                jObj.put(GoodsReceiptCMNConstants.PERSONNAME, personName);
                                jObj.put(GoodsReceiptCMNConstants.CODE,personcode);
                                jObj.put(GoodsReceiptCMNConstants.ALIASNAME, aliasname);
                                jObj.put(GoodsReceiptCMNConstants.CURRENCYID, currencyidVen);
                                jObj.put(GoodsReceiptCMNConstants.CURRENCYNAME, currencyName);
                                jObj.put(GoodsReceiptCMNConstants.CustomerCreditTerm, vendorDebitTerm);
                                jObj.put("salespersonname", vendorAgentName[1]);
                                jObj.put(GoodsReceiptCMNConstants.ExchangeRate, vendorCurrencyExchangeRate);
                                if (!customizedSumryReportFlag) {
                                    jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE1, Double.valueOf(authHandler.formattedAmount(amountdue1, companyid)));
                                    jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE2, Double.valueOf(authHandler.formattedAmount(amountdue2, companyid)));
                                    jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE3, Double.valueOf(authHandler.formattedAmount(amountdue3, companyid)));
                                    jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE4, Double.valueOf(authHandler.formattedAmount(amountdue4, companyid)));
                                    jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE5, Double.valueOf(authHandler.formattedAmount(amountdue5, companyid)));
                                    jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE6, Double.valueOf(authHandler.formattedAmount(amountdue6, companyid)));
                                    jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE7, Double.valueOf(authHandler.formattedAmount(amountdue7, companyid)));
                                    jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE8, Double.valueOf(authHandler.formattedAmount(amountdue8, companyid)));
                                    jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE9, Double.valueOf(authHandler.formattedAmount(amountdue9, companyid)));
                                    jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE10, Double.valueOf(authHandler.formattedAmount(amountdue10, companyid)));
                                    jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE11, Double.valueOf(authHandler.formattedAmount(amountdue11, companyid)));
//                                    jObj.put(GoodsReceiptCMNConstants.ACCRUEDBALANCE, Double.valueOf(authHandler.formattedAmount(accruedbalance, companyid)));
                                    jObj.put(GoodsReceiptCMNConstants.CURRENCYSYMBOL, currencySymbolVen);
                                } else {
                                    if (amountDueMap.containsKey("amountdue1")) {
                                        jObj.put(amountDueMap.get("amountdue1"), Double.valueOf(authHandler.formattedAmount(amountdue1, companyid)));
                                    }
                                    if (amountDueMap.containsKey("amountdue2")) {
                                        jObj.put(amountDueMap.get("amountdue2"), Double.valueOf(authHandler.formattedAmount(amountdue2, companyid)));
                                    }
                                    if (amountDueMap.containsKey("amountdue3")) {
                                        jObj.put(amountDueMap.get("amountdue3"), Double.valueOf(authHandler.formattedAmount(amountdue3, companyid)));
                                    }
                                    if (amountDueMap.containsKey("amountdue4")) {
                                        jObj.put(amountDueMap.get("amountdue4"), Double.valueOf(authHandler.formattedAmount(amountdue4, companyid)));
                                    }
                                    if (amountDueMap.containsKey("amountdue5")) {
                                        jObj.put(amountDueMap.get("amountdue5"), Double.valueOf(authHandler.formattedAmount(amountdue5, companyid)));
                                    }
                                    if (amountDueMap.containsKey("amountdue6")) {
                                        jObj.put(amountDueMap.get("amountdue6"), Double.valueOf(authHandler.formattedAmount(amountdue6, companyid)));
                                    }
                                    if (amountDueMap.containsKey("amountdue7")) {
                                        jObj.put(amountDueMap.get("amountdue7"), Double.valueOf(authHandler.formattedAmount(amountdue7, companyid)));
                                    }
                                    if (amountDueMap.containsKey("amountdue8")) {
                                        jObj.put(amountDueMap.get("amountdue8"), Double.valueOf(authHandler.formattedAmount(amountdue8, companyid)));
                                    }
                                }
                                jObj.put("amountdueinbase1", Double.valueOf(authHandler.formattedAmount(amountdue1Base, companyid)));
                                jObj.put("amountdueinbase2", Double.valueOf(authHandler.formattedAmount(amountdue2Base, companyid)));
                                jObj.put("amountdueinbase3", Double.valueOf(authHandler.formattedAmount(amountdue3Base, companyid)));
                                jObj.put("amountdueinbase4", Double.valueOf(authHandler.formattedAmount(amountdue4Base, companyid)));
                                jObj.put("amountdueinbase5", Double.valueOf(authHandler.formattedAmount(amountdue5Base, companyid)));
                                jObj.put("amountdueinbase6", Double.valueOf(authHandler.formattedAmount(amountdue6Base, companyid)));
                                jObj.put("amountdueinbase7", Double.valueOf(authHandler.formattedAmount(amountdue7Base, companyid)));
                                jObj.put("amountdueinbase8", Double.valueOf(authHandler.formattedAmount(amountdue8Base, companyid)));
                                jObj.put("amountdueinbase9", Double.valueOf(authHandler.formattedAmount(amountdue9Base, companyid)));
                                jObj.put("amountdueinbase10", Double.valueOf(authHandler.formattedAmount(amountdue10Base, companyid)));
                                jObj.put("amountdueinbase11", Double.valueOf(authHandler.formattedAmount(amountdue11Base, companyid)));
//                                jObj.put("accruedbalanceinbase", Double.valueOf(authHandler.formattedAmount(accruedbalanceBase, companyid)));

                                double amountdue = 0.0;
                                double amountdueBase = 0.0;
                                amountdue = Double.valueOf(authHandler.formattedAmount((amountdue1 + amountdue2 + amountdue3 + amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11), companyid));
                                amountdueBase = Double.valueOf(authHandler.formattedAmount((amountdue1Base + amountdue2Base + amountdue3Base + amountdue4Base + amountdue5Base + amountdue6Base + amountdue7Base + amountdue8Base + amountdue9Base + amountdue10Base + amountdue11Base), companyid));
                                jObj.put(GoodsReceiptCMNConstants.TOTAL, amountdue);
                                jObj.put("totalinbase", amountdueBase);
                                totalinbase += amountdueBase;
                                if (StringUtil.equalIgnoreCase(chartType, Constants.BAR_CHART) && isAged) {
                                    totalAmountDueInBase1 += amountdue1Base;
                                    totalAmountDueInBase2 += amountdue2Base;
                                    totalAmountDueInBase3 += amountdue3Base;
                                    totalAmountDueInBase4 += amountdue4Base;
                                    totalAmountDueInBase5 += amountdue5Base;
                                    totalAmountDueInBase6 += amountdue6Base;
                                    totalAmountDueInBase7 += amountdue7Base;
                                    totalAmountDueInBase8 += amountdue8Base;
                                    totalAmountDueInBase9 += amountdue9Base;
                                    totalAmountDueInBase10 += amountdue10Base;
                                    totalAmountDueInBase11 += amountdue11Base;
                                }
                                if (customizedSumryReportFlag && !showCustVendorWithZeroAmounts && amountdue == 0) {
                                    continue;
                                } else if (StringUtil.equalIgnoreCase(chartType, Constants.BAR_CHART) && isAged) {
                                    continue;
                                }
                                jArr.put(jObj);
                            }
                        }
                        if (isCurrencyDetails || isDetailedXls || isDetailedPDF) {
                            JSONObject currencyObj = new JSONObject();
                            for (int i = 0; i < invjarr.length(); i++) {
                                amountdue1 = amountdue2 = amountdue3 = amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                amountdueinbase1 = amountdueinbase2 = amountdueinbase3 = amountdueinbase4 = amountdueinbase5 = amountdueinbase6 = amountdueinbase7 = amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                JSONObject invobj = invjarr.getJSONObject(i);
                                JSONObject putObj = new JSONObject();
                                if (i == 0) {
                                    personName = (invobj.has("personname")) ? invobj.getString("personname") : "";
                                    aliasname = (invobj.has("aliasname")) ? invobj.getString("aliasname") : "";
                                    currencySymbol = (invobj.has("currencysymbol")) ? invobj.getString("currencysymbol") : "";
                                    currencyid = (invobj.has("currencyid")) ? invobj.getString("currencyid") : "";
                                    currencyName = (invobj.has("currencyname")) ? invobj.getString("currencyname") : "";
                                    isDN = invobj.has("isDN") ? invobj.getBoolean("isDN") : false;
                                    isMP = invobj.has("isMP") ? invobj.getBoolean("isMP") : false;
                                    Date dueDate = new Date();
                                    if (datefilter == 0 || datefilter == Constants.agedDueDate0to30Filter) {
                                        dueDate = authHandler.getDateOnlyFormat(request).parse(invobj.getString("duedate"));
                                    } else {
                                        dueDate = authHandler.getDateOnlyFormat(request).parse(invobj.getString("date"));
                                    }
                                    double amountdue = invobj.getDouble("amountdue");
                                    double amountdueinbase = invobj.getDouble("amountdueinbase");
                                    boolean isopeningBalanceTransaction = invobj.optBoolean(GoodsReceiptCMNConstants.IsopeningBalanceTransaction, false);

                                    if (isDN || isMP) {
                                        if (!isopeningBalanceTransaction) {
                                            amountdue = -amountdue;
                                            amountdueinbase = -amountdueinbase;
                                        }
                                    }

                                    if (customizedSumryReportFlag) {
                                        if ((dueDate.after(customizeSumryTo1Date) || dueDate.equals(customizeSumryTo1Date)) && (dueDate.before(customizeSumryFrom1Date) || dueDate.equals(customizeSumryFrom1Date))) {
                                            amountdue1 += amountdue;
                                            amountdueinbase1 += amountdueinbase;
                                        } else if ((dueDate.after(customizeSumryTo2Date) || dueDate.equals(customizeSumryTo2Date)) && (dueDate.before(customizeSumryFrom2Date) || dueDate.equals(customizeSumryFrom2Date))) {
                                            amountdue2 += amountdue;
                                            amountdueinbase2 += amountdueinbase;
                                        } else if ((dueDate.after(customizeSumryTo3Date) || dueDate.equals(customizeSumryTo3Date)) && (dueDate.before(customizeSumryFrom3Date) || dueDate.equals(customizeSumryFrom3Date))) {
                                            amountdue3 += amountdue;
                                            amountdueinbase3 += amountdueinbase;
                                        } else if ((dueDate.after(customizeSumryTo4Date) || dueDate.equals(customizeSumryTo4Date)) && (dueDate.before(customizeSumryFrom4Date) || dueDate.equals(customizeSumryFrom4Date))) {
                                            amountdue4 += amountdue;
                                            amountdueinbase4 += amountdueinbase;
                                        } else if ((dueDate.after(customizeSumryTo5Date) || dueDate.equals(customizeSumryTo5Date)) && (dueDate.before(customizeSumryFrom5Date) || dueDate.equals(customizeSumryFrom5Date))) {
                                            amountdue5 += amountdue;
                                            amountdueinbase5 += amountdueinbase;
                                        } else if ((dueDate.after(customizeSumryTo6Date) || dueDate.equals(customizeSumryTo6Date)) && (dueDate.before(customizeSumryFrom6Date) || dueDate.equals(customizeSumryFrom6Date))) {
                                            amountdue6 += amountdue;
                                            amountdueinbase6 += amountdueinbase;
                                        } else if ((dueDate.after(customizeSumryTo7Date) || dueDate.equals(customizeSumryTo7Date)) && (dueDate.before(customizeSumryFrom7Date) || dueDate.equals(customizeSumryFrom7Date))) {
                                            amountdue7 += amountdue;
                                            amountdueinbase7 += amountdueinbase;
                                        }

                                    } else {
                                        if (isdistibutive) {
//                                            if (startDate != null && dueDate.before(startDate)) {
//                                                accruedbalance += amountdue;
//                                                accruedbalanceinbase += amountdueinbase;
//                                            } else 
                                            if (dueDate.after(oneDayBeforeCal1Date)) {
                                                if (dueDate.equals(cal1Date) && (datefilter == Constants.agedDueDate0to30Filter || datefilter == Constants.agedInvoiceDate0to30Filter)){
                                                    amountdueinbase2 += amountdueinbase;
                                                    amountdue2 += amountdue;
                                                } else {
                                                    amountdueinbase1 += amountdueinbase;
                                                    amountdue1 += amountdue;
                                                }
                                            } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) {
                                                amountdue2 += amountdue;
                                                amountdueinbase2 += amountdueinbase;
                                            } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) {
                                                amountdue3 += amountdue;
                                                amountdueinbase3 += amountdueinbase;
                                            } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) {
                                                amountdue4 += amountdue;
                                                amountdueinbase4 += amountdueinbase;
                                            } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) {
                                                amountdue5 += amountdue;
                                                amountdueinbase5 += amountdueinbase;
                                            } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) {
                                                amountdue6 += amountdue;
                                                amountdueinbase6 += amountdueinbase;
                                            } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) {
                                                amountdue7 += amountdue;
                                                amountdueinbase7 += amountdueinbase;
                                            } else if ((cal8Date.before(dueDate) || cal8Date.equals(dueDate)) && cal7Date.after(dueDate)) {
                                                amountdue8 += amountdue;
                                                amountdueinbase8 += amountdueinbase;
                                            } else if ((cal9Date.before(dueDate) || cal9Date.equals(dueDate)) && cal8Date.after(dueDate)) {
                                                amountdue9 += amountdue;
                                                amountdueinbase9 += amountdueinbase;
                                            } else if ((cal10Date.before(dueDate) || cal10Date.equals(dueDate)) && cal9Date.after(dueDate)) {
                                                amountdue10 += amountdue;
                                                amountdueinbase10 += amountdueinbase;
                                            } else {
                                                amountdue11 += amountdue;
                                                amountdueinbase11 += amountdueinbase;
                                            }
                                            
                                            switch(noOfInterval){
                                                case 2:
                                                    amountdue3 += amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                                    amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                                    amountdueinbase3 += amountdueinbase4 + amountdueinbase5 + amountdueinbase6 + amountdueinbase7 + amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                                    amountdueinbase4 = amountdueinbase5 = amountdueinbase6 = amountdueinbase7 = amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                                    break;
                                                case 3:
                                                    amountdue4 += amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                                    amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                                    amountdueinbase4 += amountdueinbase5 + amountdueinbase6 + amountdueinbase7 + amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                                    amountdueinbase5 = amountdueinbase6 = amountdueinbase7 = amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                                    break;
                                                case 4:
                                                    amountdue5 += amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                                    amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                                    amountdueinbase5 += amountdueinbase6 + amountdueinbase7 + amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                                    amountdueinbase6 = amountdueinbase7 = amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                                    break;
                                                case 5:
                                                    amountdue6 += amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                                    amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                                    amountdueinbase6 += amountdueinbase7 + amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                                    amountdueinbase7 = amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                                    break;
                                                case 6:
                                                    amountdue7 += amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                                    amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                                    amountdueinbase7 += amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                                    amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                                    break;
                                                case 7:
                                                    amountdue8 += amountdue9 + amountdue10 + amountdue11;
                                                    amountdue9 = amountdue10 = amountdue11 = 0;
                                                    amountdueinbase8 += amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                                    amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                                    break;
                                                case 8:
                                                    amountdue9 += amountdue10 + amountdue11;
                                                    amountdue10 = amountdue11 = 0;
                                                    amountdueinbase9 += amountdueinbase10 + amountdueinbase11;
                                                    amountdueinbase10 = amountdueinbase11 = 0;
                                                    break;
                                                case 9:
                                                    amountdue10 += amountdue11;
                                                    amountdue11 = 0;
                                                    amountdueinbase10 += amountdueinbase11;
                                                    amountdueinbase11 = 0;
                                                    break;
                                            }
                                            
                                        } else {
//                                            if (startDate != null && dueDate.before(startDate)) {
//                                                accruedbalance += amountdue;
//                                                accruedbalanceinbase += amountdueinbase;
//                                            } else 
                                            if (dueDate.after(oneDayBeforeCal1Date) && (dueDate.before(cal1Date) || dueDate.equals(cal1Date))) {
                                                amountdue1 += amountdue;
                                                amountdueinbase1 += amountdueinbase;
                                            } else if (dueDate.after(cal2Date) || dueDate.equals(cal2Date)) {
                                                amountdue2 += amountdue;
                                                amountdueinbase2 += amountdueinbase;
                                            } else if (dueDate.after(cal3Date) || dueDate.equals(cal3Date)) {
                                                amountdue3 += amountdue;
                                                amountdueinbase3 += amountdueinbase;
                                            } else {
                                                amountdue4 += amountdue;
                                                amountdueinbase4 += amountdueinbase;
                                            }
                                        }
                                    }
                                    putObj.put("personid", personID);
                                    putObj.put("personname", personName);
                                    putObj.put("aliasname", aliasname);
                                    putObj.put("currencysymbol", currencySymbol);
                                    putObj.put("currencyid", currencyid);
                                    putObj.put("currencyname", currencyName);
                                    putObj.put("salespersonname", vendorAgentName[1]);
                                    
                                    putObj.put("amountdue1", amountdue1);
                                    putObj.put("amountdue2", amountdue2);
                                    putObj.put("amountdue3", amountdue3);
                                    putObj.put("amountdue4", amountdue4);
                                    putObj.put("amountdue5", amountdue5);
                                    putObj.put("amountdue6", amountdue6);
                                    putObj.put("amountdue7", amountdue7);
                                    putObj.put("amountdue8", amountdue8);
                                    putObj.put("amountdue9", amountdue9);
                                    putObj.put("amountdue10", amountdue10);
                                    putObj.put("amountdue11", amountdue11);

                                    putObj.put("amountdueinbase1", amountdueinbase1);
                                    putObj.put("amountdueinbase2", amountdueinbase2);
                                    putObj.put("amountdueinbase3", amountdueinbase3);
                                    putObj.put("amountdueinbase4", amountdueinbase4);
                                    putObj.put("amountdueinbase5", amountdueinbase5);
                                    putObj.put("amountdueinbase6", amountdueinbase6);
                                    putObj.put("amountdueinbase7", amountdueinbase7);
                                    putObj.put("amountdueinbase8", amountdueinbase8);
                                    putObj.put("amountdueinbase9", amountdueinbase9);
                                    putObj.put("amountdueinbase10", amountdueinbase10);
                                    putObj.put("amountdueinbase11", amountdueinbase11);

                                    
//                                    putObj.put("accruedbalance", accruedbalance);
//                                    putObj.put("accruedbalanceinbase", accruedbalanceinbase);
                                    currencyObj.put(currencyid, putObj);
                                } else {
                                    personID = (invobj.has("personid")) ? invobj.getString("personid") : "";
                                    personName = (invobj.has("personname")) ? invobj.getString("personname") : "";
                                    aliasname = (invobj.has("aliasname")) ? invobj.getString("aliasname") : "";
                                    currencySymbol = (invobj.has("currencysymbol")) ? invobj.getString("currencysymbol") : "";
                                    currencyid = (invobj.has("currencyid")) ? invobj.getString("currencyid") : "";
                                    currencyName = (invobj.has("currencyname")) ? invobj.getString("currencyname") : "";
                                    isDN = invobj.has("isDN") ? invobj.getBoolean("isDN") : false;
                                    isMP = invobj.has("isMP") ? invobj.getBoolean("isMP") : false;
                                    if (currencyObj.has(currencyid)) {
                                        JSONObject addObj = currencyObj.getJSONObject(currencyid);
                                        if (isdistibutive) {
                                            amountdue1 = addObj.getDouble("amountdue1");
                                            amountdue2 = addObj.getDouble("amountdue2");
                                            amountdue3 = addObj.getDouble("amountdue3");
                                            amountdue4 = addObj.getDouble("amountdue4");
                                            amountdue5 = addObj.getDouble("amountdue5");
                                            amountdue6 = addObj.getDouble("amountdue6");
                                            amountdue7 = addObj.getDouble("amountdue7");
                                            amountdue8 = addObj.getDouble("amountdue8");
                                            amountdue9 = addObj.getDouble("amountdue9");
                                            amountdue10 = addObj.getDouble("amountdue10");
                                            amountdue11 = addObj.getDouble("amountdue11");

                                            amountdueinbase1 = addObj.getDouble("amountdueinbase1");
                                            amountdueinbase2 = addObj.getDouble("amountdueinbase2");
                                            amountdueinbase3 = addObj.getDouble("amountdueinbase3");
                                            amountdueinbase4 = addObj.getDouble("amountdueinbase4");
                                            amountdueinbase5 = addObj.getDouble("amountdueinbase5");
                                            amountdueinbase6 = addObj.getDouble("amountdueinbase6");
                                            amountdueinbase7 = addObj.getDouble("amountdueinbase7");
                                            amountdueinbase8 = addObj.getDouble("amountdueinbase8");
                                            amountdueinbase9 = addObj.getDouble("amountdueinbase9");
                                            amountdueinbase10 = addObj.getDouble("amountdueinbase10");
                                            amountdueinbase11 = addObj.getDouble("amountdueinbase11");
                                        } else {
                                            amountdue1 = addObj.getDouble("amountdue1");
                                            amountdue2 = addObj.getDouble("amountdue2");
                                            amountdue3 = addObj.getDouble("amountdue3");
                                            amountdueinbase1 = addObj.getDouble("amountdueinbase1");
                                            amountdueinbase2 = addObj.getDouble("amountdueinbase2");
                                            amountdueinbase3 = addObj.getDouble("amountdueinbase3");
                                        }
                                    }
                                    Date dueDate = new Date();
                                    if (datefilter == 0 || datefilter == Constants.agedDueDate0to30Filter) {
                                        dueDate = authHandler.getDateOnlyFormat(request).parse(invobj.getString("duedate"));
                                    } else {
                                        dueDate = authHandler.getDateOnlyFormat(request).parse(invobj.getString("date"));
                                    }
                                    double amountdue = invobj.getDouble("amountdue");
                                    double amountdueinbase = invobj.getDouble("amountdueinbase");
                                    boolean isopeningBalanceTransaction = invobj.optBoolean(GoodsReceiptCMNConstants.IsopeningBalanceTransaction, false);
                                    if (isDN || isMP) {
                                        if (!isopeningBalanceTransaction) {
                                            amountdue = -amountdue;
                                            amountdueinbase = -amountdueinbase;
                                        }
                                    }

                                    if (customizedSumryReportFlag) {
                                        if ((dueDate.after(customizeSumryTo1Date) || dueDate.equals(customizeSumryTo1Date)) && (dueDate.before(customizeSumryFrom1Date) || dueDate.equals(customizeSumryFrom1Date))) {
                                            amountdue1 += amountdue;
                                            amountdueinbase1 += amountdueinbase;
                                        } else if ((dueDate.after(customizeSumryTo2Date) || dueDate.equals(customizeSumryTo1Date)) && (dueDate.before(customizeSumryFrom2Date) || dueDate.equals(customizeSumryFrom2Date))) {
                                            amountdue2 += amountdue;
                                            amountdueinbase2 += amountdueinbase;
                                        } else if ((dueDate.after(customizeSumryTo3Date) || dueDate.equals(customizeSumryTo3Date)) && (dueDate.before(customizeSumryFrom3Date) || dueDate.equals(customizeSumryFrom3Date))) {
                                            amountdue3 += amountdue;
                                            amountdueinbase3 += amountdueinbase;
                                        } else if ((dueDate.after(customizeSumryTo4Date) || dueDate.equals(customizeSumryTo4Date)) && (dueDate.before(customizeSumryFrom4Date) || dueDate.equals(customizeSumryFrom4Date))) {
                                            amountdue4 += amountdue;
                                            amountdueinbase4 += amountdueinbase;
                                        } else if ((dueDate.after(customizeSumryTo5Date) || dueDate.equals(customizeSumryTo5Date)) && (dueDate.before(customizeSumryFrom5Date) || dueDate.equals(customizeSumryFrom5Date))) {
                                            amountdue5 += amountdue;
                                            amountdueinbase5 += amountdueinbase;
                                        } else if ((dueDate.after(customizeSumryTo6Date) || dueDate.equals(customizeSumryTo6Date)) && (dueDate.before(customizeSumryFrom6Date) || dueDate.equals(customizeSumryFrom6Date))) {
                                            amountdue6 += amountdue;
                                            amountdueinbase6 += amountdueinbase;
                                        } else if ((dueDate.after(customizeSumryTo7Date) || dueDate.equals(customizeSumryTo7Date)) && (dueDate.before(customizeSumryFrom7Date) || dueDate.equals(customizeSumryFrom7Date))) {
                                            amountdue7 += amountdue;
                                            amountdueinbase7 += amountdueinbase;
                                        }

                                    } else {
                                        if (isdistibutive) {
//                                            if (startDate != null && dueDate.before(startDate)) {
//                                                accruedbalance += amountdue;
//                                                accruedbalanceinbase += amountdueinbase;
//                                            }else
                                            if (dueDate.after(oneDayBeforeCal1Date)) {
                                                if (dueDate.equals(cal1Date) && (datefilter == Constants.agedDueDate0to30Filter || datefilter == Constants.agedInvoiceDate0to30Filter)) {
                                                    amountdue2 += amountdue;
                                                    amountdueinbase2 += amountdueinbase;
                                                } else {
                                                    amountdue1 += amountdue;
                                                    amountdueinbase1 += amountdueinbase;
                                                }
                                            } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) {
                                                amountdue2 += amountdue;
                                                amountdueinbase2 += amountdueinbase;
                                            } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) {
                                                amountdue3 += amountdue;
                                                amountdueinbase3 += amountdueinbase;
                                            } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) {
                                                amountdue4 += amountdue;
                                                amountdueinbase4 += amountdueinbase;
                                            } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) {
                                                amountdue5 += amountdue;
                                                amountdueinbase5 += amountdueinbase;
                                            } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) {
                                                amountdue6 += amountdue;
                                                amountdueinbase6 += amountdueinbase;
                                            } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) {
                                                amountdue7 += amountdue;
                                                amountdueinbase7 += amountdueinbase;
                                            } else if ((cal8Date.before(dueDate) || cal8Date.equals(dueDate)) && cal7Date.after(dueDate)) {
                                                amountdue8 += amountdue;
                                                amountdueinbase8 += amountdueinbase;
                                            } else if ((cal9Date.before(dueDate) || cal9Date.equals(dueDate)) && cal8Date.after(dueDate)) {
                                                amountdue9 += amountdue;
                                                amountdueinbase9 += amountdueinbase;
                                            } else if ((cal10Date.before(dueDate) || cal10Date.equals(dueDate)) && cal9Date.after(dueDate)) {
                                                amountdue10 += amountdue;
                                                amountdueinbase10 += amountdueinbase;
                                            } else {
                                                amountdue11 += amountdue;
                                                amountdueinbase11 += amountdueinbase;
                                            }
                                            
                                            switch(noOfInterval){
                                                case 2:
                                                    amountdue3 += amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                                    amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                                    amountdueinbase3 += amountdueinbase4 + amountdueinbase5 + amountdueinbase6 + amountdueinbase7 + amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                                    amountdueinbase4 = amountdueinbase5 = amountdueinbase6 = amountdueinbase7 = amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                                    break;
                                                case 3:
                                                    amountdue4 += amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                                    amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                                    amountdueinbase4 += amountdueinbase5 + amountdueinbase6 + amountdueinbase7 + amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                                    amountdueinbase5 = amountdueinbase6 = amountdueinbase7 = amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                                    break;
                                                case 4:
                                                    amountdue5 += amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                                    amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                                    amountdueinbase5 += amountdueinbase6 + amountdueinbase7 + amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                                    amountdueinbase6 = amountdueinbase7 = amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                                    break;
                                                case 5:
                                                    amountdue6 += amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                                    amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                                    amountdueinbase6 += amountdueinbase7 + amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                                    amountdueinbase7 = amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                                    break;
                                                case 6:
                                                    amountdue7 += amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                                    amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                                    amountdueinbase7 += amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                                    amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                                    break;
                                                case 7:
                                                    amountdue8 += amountdue9 + amountdue10 + amountdue11;
                                                    amountdue9 = amountdue10 = amountdue11 = 0;
                                                    amountdueinbase8 += amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                                    amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                                    break;
                                                case 8:
                                                    amountdue9 += amountdue10 + amountdue11;
                                                    amountdue10 = amountdue11 = 0;
                                                    amountdueinbase9 += amountdueinbase10 + amountdueinbase11;
                                                    amountdueinbase10 = amountdueinbase11 = 0;
                                                    break;
                                                case 9:
                                                    amountdue10 += amountdue11;
                                                    amountdue11 = 0;
                                                    amountdueinbase10 += amountdueinbase11;
                                                    amountdueinbase11 = 0;
                                                    break;
                                            }
                                            
                                        } else {
//                                            if (startDate != null && dueDate.before(startDate)) {
//                                                accruedbalance += amountdue;
//                                                accruedbalanceinbase += amountdueinbase;
//                                            } else 
                                            if (dueDate.after(oneDayBeforeCal1Date) && (dueDate.before(cal1Date) || dueDate.equals(cal1Date))) {
                                                amountdue1 += amountdue;
                                                amountdueinbase1 += amountdueinbase;
                                            } else if (dueDate.after(cal2Date) || dueDate.equals(cal2Date)) {
                                                amountdue2 += amountdue;
                                                amountdueinbase2 += amountdueinbase;
                                            } else if (dueDate.after(cal3Date) || dueDate.equals(cal3Date)) {
                                                amountdue3 += amountdue;
                                                amountdueinbase3 += amountdueinbase;
                                            } else {
                                                amountdue4 += amountdue;
                                                amountdueinbase4 += amountdueinbase;
                                            }
                                        }
                                    }
                                    putObj.put("personid", personID);
                                    putObj.put("personname", personName);
                                    putObj.put("aliasname", aliasname);
                                    putObj.put("currencysymbol", currencySymbol);
                                    putObj.put("currencyid", currencyid);
                                    putObj.put("currencyname", currencyName);
                                    putObj.put("salespersonname", vendorAgentName[1]);

                                    putObj.put("amountdue1", amountdue1);
                                    putObj.put("amountdue2", amountdue2);
                                    putObj.put("amountdue3", amountdue3);
                                    putObj.put("amountdue4", amountdue4);
                                    putObj.put("amountdue5", amountdue5);
                                    putObj.put("amountdue6", amountdue6);
                                    putObj.put("amountdue7", amountdue7);
                                    putObj.put("amountdue8", amountdue8);
                                    putObj.put("amountdue9", amountdue9);
                                    putObj.put("amountdue10", amountdue10);
                                    putObj.put("amountdue11", amountdue11);

                                    putObj.put("amountdueinbase1", amountdueinbase1);
                                    putObj.put("amountdueinbase2", amountdueinbase2);
                                    putObj.put("amountdueinbase3", amountdueinbase3);
                                    putObj.put("amountdueinbase4", amountdueinbase4);
                                    putObj.put("amountdueinbase5", amountdueinbase5);
                                    putObj.put("amountdueinbase6", amountdueinbase6);
                                    putObj.put("amountdueinbase7", amountdueinbase7);
                                    putObj.put("amountdueinbase8", amountdueinbase8);
                                    putObj.put("amountdueinbase9", amountdueinbase9);
                                    putObj.put("amountdueinbase10", amountdueinbase10);
                                    putObj.put("amountdueinbase11", amountdueinbase11);

//                                    putObj.put("accruedbalance", accruedbalance);
//                                    putObj.put("accruedbalanceinbase", accruedbalanceinbase);
                                    currencyObj.put(currencyid, putObj);
                                }
                            }
                            Iterator itr = currencyObj.keys();
                            while (itr.hasNext()) {
                                JSONObject getObj = currencyObj.getJSONObject(itr.next().toString());
                                jObj = new JSONObject();
                                if (getObj.has("personid")) {
                                    jObj.put("personid", getObj.getString("personid"));
                                }
                                if (getObj.has("personname")) {
                                    jObj.put("personname" + linedetails, getObj.getString("personname"));
                                }
                                if (getObj.has("aliasname")) {
                                    jObj.put("aliasname" + linedetails, getObj.getString("aliasname"));
                                }
                                jObj.put("amountdue1" + linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue1"), companyid)));
                                jObj.put("amountdue2" + linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue2"), companyid)));
                                jObj.put("amountdue3" + linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue3"), companyid)));
                                jObj.put("amountdue4" + linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue4"), companyid)));
                                jObj.put("amountdue5" + linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue5"), companyid)));
                                jObj.put("amountdue6" + linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue6"), companyid)));
                                jObj.put("amountdue7" + linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue7"), companyid)));
                                jObj.put("amountdue8" + linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue8"), companyid)));
                                jObj.put("amountdue9" + linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue9"), companyid)));
                                jObj.put("amountdue10" + linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue10"), companyid)));
                                jObj.put("amountdue11" + linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue11"), companyid)));
//                                jObj.put("accruedbalance" + linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("accruedbalance"), companyid)));
                                jObj.put("currencysymbol" + linedetails, getObj.getString("currencysymbol"));
                                jObj.put("currencyid" + linedetails, getObj.getString("currencyid"));
                                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), getObj.getString("currencyid"));
                                KWLCurrency currency = (KWLCurrency) objItr.getEntityList().get(0);
                                jObj.put("currencyCode" + linedetails, currency.getCurrencyCode());
                                jObj.put("currencyname" + linedetails, getObj.getString("currencyname"));
                                jObj.put("salespersonname", vendorAgentName[1]);
                                jObj.put("personinfo", personInfo);
                                jObj.put("code", code);
                                double amountdue = 0.0;

                                amountdue = Double.valueOf(authHandler.formattedAmount((getObj.getDouble("amountdue1") + getObj.getDouble("amountdue2") + getObj.getDouble("amountdue3") + getObj.getDouble("amountdue4") + getObj.getDouble("amountdue5") + getObj.getDouble("amountdue6") + getObj.getDouble("amountdue7") + getObj.getDouble("amountdue8") + getObj.getDouble("amountdue9") + getObj.getDouble("amountdue10") + getObj.getDouble("amountdue11")), companyid));

                                /**
                                 * Hidded Total column from AP and AR when we expand record.SDP-13193
                                 */
                                if (isExportReport) {
                                    jObj.put("total" + linedetails, amountdue);
                                }
                                double amountdueinbase = 0.0;

                                amountdueinbase = Double.valueOf(authHandler.formattedAmount((getObj.getDouble("amountdueinbase1") + getObj.getDouble("amountdueinbase2") + getObj.getDouble("amountdueinbase3") + getObj.getDouble("amountdueinbase4") + getObj.getDouble("amountdueinbase5") + getObj.getDouble("amountdueinbase6") + getObj.getDouble("amountdueinbase7") + getObj.getDouble("amountdueinbase8") + getObj.getDouble("amountdueinbase9") + getObj.getDouble("amountdueinbase10") + getObj.getDouble("amountdueinbase11")), companyid));
                                
                                jObj.put("totalinbase" + linedetails, amountdueinbase);
                                if (!isDetailedXls) {
                                    totalinbase += amountdueinbase;
                                }
                                if (customizedSumryReportFlag && !showCustVendorWithZeroAmounts && amountdue == 0) {
                                    continue;
                                }
                                jArr.put(jObj);
                            }
                        }
                    }
                }
                if(exportAgedPayables || isDetailedXls){ //  Used for export CSV- Add totalinbase
                    JSONObject jtotal = new JSONObject();
                    if (customizedSumryReportFlag) {
                        jtotal.put("personname", "Total");
                        jtotal.put("total", totalinbase);
                    } else {
                        jtotal.put("total", "Total");
                        jtotal.put("totalinbase", totalinbase);
                    }
                    jArr.put(jtotal);
                }

            boolean statementOfAccountsFlag = false;
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.statementOfAccountsFlag))) {
                statementOfAccountsFlag = Boolean.parseBoolean(request.getParameter(Constants.statementOfAccountsFlag).toString());
            }

            if (!statementOfAccountsFlag) { // 
                paramObj.put(Constants.agedPayableDateFilter, datefilter);
                paramObj.put(Constants.agedPayableInterval, duration);
                paramObj.put(Constants.agedPayableNoOfInterval, noOfInterval);
            } 
                accDashboardServiceImpl.saveUserPreferencesOptions(paramObj);
                
                System.out.println("Invoice Count: "+invoiceCount);
                System.out.println("JSON Count: " + jsonCount);
                System.out.println("opening Invoice Count: "+openingCount);
                System.out.println("opening JSON Count: " + openingJSONCount);                
                if (StringUtil.equalIgnoreCase(chartType, Constants.BAR_CHART) && isAged) {
                    double[] totalAmountDueInBase = {totalAmountDueInBase1, totalAmountDueInBase2, totalAmountDueInBase3, totalAmountDueInBase4, totalAmountDueInBase5, totalAmountDueInBase6, totalAmountDueInBase7, totalAmountDueInBase8};
                    jArr = getAgedPayableReceivableBarChartJson(request, companyid, totalAmountDueInBase);
                } else if(StringUtil.equalIgnoreCase(chartType, Constants.PIE_CHART) && isAged) {
                    jArr = getAgedPayableReceivablePieChartJson(companyid, jArr);
                }
        } catch (JSONException | ParseException | ServiceException | UnsupportedEncodingException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getVendorAgedPayable : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    @Override
    public JSONArray getAgedPayableReceivableBarChartJson(HttpServletRequest request, String companyid, double[] totalAmountDueInBase) throws JSONException{
            JSONArray jArr = new JSONArray();
        try {
            String[] columnHeaders = new String[totalAmountDueInBase.length];
            if(!StringUtil.isNullOrEmpty(request.getParameter("columnHeaders"))) {
                columnHeaders = request.getParameter("columnHeaders").split(",");
            }
            
            for (int i = 0; i < columnHeaders.length; i++) {
                JSONObject temp = new JSONObject();
                temp.put("period", columnHeaders[i]);
                temp.put("total", authHandler.round(totalAmountDueInBase[i], companyid));
                jArr.put(temp);
            }
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    
    @Override
    public JSONArray getAgedPayableReceivablePieChartJson(String companyid, JSONArray jArr) throws JSONException {
        try {
            JSONArray tempArr = AccountingManager.sortJsonArrayOnIntegerValues(jArr, "totalinbase", "DESC");
            jArr = new JSONArray();
            double total = 0.0;
            for (int i = 0; i < tempArr.length(); i++) {
                if (i < Constants.MAX_LIMIT_FOR_PIE) {
                    JSONObject temp = new JSONObject();
                    temp.put("customername", tempArr.optJSONObject(i).optString("personname", ""));
                    double totalinbase = tempArr.optJSONObject(i).optDouble("totalinbase", 0.0);
                    if(totalinbase <= 0) {
                        break;
                    } else {
                        temp.put("total", authHandler.round(totalinbase, companyid));
                    }
                    jArr.put(temp);
                } else {
                    total += tempArr.optJSONObject(i).optDouble("totalinbase", 0.0);
                }
            }
            if (total > 0.0) {
                JSONObject temp = new JSONObject();
                temp.put("customername", "Others");
                temp.put("total", authHandler.round(total, companyid));
                jArr.put(temp);
            }
        } catch (Exception ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    /**
     * 
     * @param request
     * @param requestParams
     * @return
     * @throws SessionExpiredException
     * @throws ServiceException 
     * @DESC  :  Aged Report in Parent Child Hierarchy
     */
 public JSONArray getParentChildVendorAgedPayableMerged(HttpServletRequest request, HashMap requestParams) throws SessionExpiredException, ServiceException {
        JSONObject jObj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            requestParams.put("companyid", companyid);
//            String curDateString = (!StringUtil.isNullOrEmpty(request.getParameter(GoodsReceiptCMNConstants.CURDATE))) ? request.getParameter(GoodsReceiptCMNConstants.CURDATE) : (request.getParameter("stdate") != null ? request.getParameter("stdate") : request.getParameter("startdate"));
            String curDateString = !(StringUtil.isNullOrEmpty(request.getParameter(Constants.asOfDate))) ? request.getParameter(Constants.asOfDate) : request.getParameter(Constants.curdate);
            String templatesubtype = StringUtil.isNullOrEmpty(request.getParameter("templatesubtype")) ? Constants.TEMPLATE_SUBTYPE_SOA : request.getParameter("templatesubtype");
            boolean isCallFromDD = false;
            if (request.getAttribute("isCallFromDD") != null) {
                isCallFromDD = Boolean.parseBoolean(request.getAttribute("isCallFromDD").toString());
            }
            String transactionId = request.getAttribute("transactionId") == null ? "" : request.getAttribute("transactionId").toString();
            if (isCallFromDD) {
                requestParams.put("custVendorID", transactionId);
            }
            boolean isExportReport = (!StringUtil.isNullOrEmpty(request.getParameter("isExportReport"))) ? Boolean.parseBoolean(request.getParameter("isExportReport")) : false;
            DateFormat df = authHandler.getDateOnlyFormat(request);
            Date curDate = df.parse(curDateString);
            int invoiceCount = 0, jsonCount = 0, openingCount = 0, openingJSONCount = 0;
            int duration = request.getParameter(GoodsReceiptCMNConstants.DURATION) == null ? 0 : Integer.parseInt(request.getParameter(GoodsReceiptCMNConstants.DURATION));
            int noOfInterval = Constants.DefaultNoOfIntervals;
            if(requestParams.containsKey("noOfInterval") && requestParams.get("noOfInterval") != null) {
               noOfInterval = requestParams.get("noOfInterval").toString().equals("") ? Constants.DefaultNoOfIntervals : Integer.parseInt(requestParams.get("noOfInterval").toString());
            }
            boolean isdistibutive = StringUtil.getBoolean(request.getParameter(GoodsReceiptCMNConstants.ISDISTRIBUTIVE));
            boolean showCustVendorWithZeroAmounts = StringUtil.isNullOrEmpty(request.getParameter("showCustVendorWithZeroAmounts")) ? false : StringUtil.getBoolean(request.getParameter("showCustVendorWithZeroAmounts"));
            int datefilter = StringUtil.getInteger(request.getParameter("datefilter"));// 0 = Invoice Due date OR 1 = Invoice date
            boolean isCurrencyDetails = request.getParameter("isCurrencyDetails") != null ? Boolean.parseBoolean(request.getParameter("isCurrencyDetails")) : false;
            if (request.getAttribute("isCurrencyDetails") != null) {
                isCurrencyDetails = (Boolean) request.getAttribute("isCurrencyDetails");
            }
            DateFormat dateFormat = authHandler.getDateOnlyFormat();
            Date startDate = null;
            if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null) {
                startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? dateFormat.parse(Constants.opening_Date) : dateFormat.parse(requestParams.get(Constants.REQ_startdate).toString());
            }
            
            double amountdue1 = 0;
            double amountdue2 = 0;
            double amountdue3 = 0;
            double amountdue4 = 0;
            double amountdue5 = 0;
            double amountdue6 = 0;
            double amountdue7 = 0;
            double amountdue8 = 0;
            double amountdue9 = 0;
            double amountdue10 = 0;
            double amountdue11 = 0;
//            double accruedbalance = 0;

            double amountdue1Base = 0;
            double amountdue2Base = 0;
            double amountdue3Base = 0;
            double amountdue4Base = 0;
            double amountdue5Base = 0;
            double amountdue6Base = 0;
            double amountdue7Base = 0;
            double amountdue8Base = 0;
            double amountdue9Base = 0;
            double amountdue10Base = 0;
            double amountdue11Base = 0;
//            double accruedbalanceBase = 0;

            double amountdueinbase1 = 0;
            double amountdueinbase2 = 0;
            double amountdueinbase3 = 0;
            double amountdueinbase4 = 0;
            double amountdueinbase5 = 0;
            double amountdueinbase6 = 0;
            double amountdueinbase7 = 0;
            double amountdueinbase8 = 0;
            double amountdueinbase9 = 0;
            double amountdueinbase10 = 0;
            double amountdueinbase11 = 0;
//            double accruedbalanceinbase = 0;

            double totalinbase = 0;
            Calendar oneDayBeforeCal1 = Calendar.getInstance();
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            Calendar cal8 = Calendar.getInstance();
            Calendar cal9 = Calendar.getInstance();
            Calendar cal10 = Calendar.getInstance();
            oneDayBeforeCal1.setTime(curDate);
            cal1.setTime(curDate);
            cal2.setTime(curDate);
            cal3.setTime(curDate);
            cal4.setTime(curDate);
            cal5.setTime(curDate);
            cal6.setTime(curDate);
            cal7.setTime(curDate);
            cal8.setTime(curDate);
            cal9.setTime(curDate);
            cal10.setTime(curDate);
            oneDayBeforeCal1.add(Calendar.DAY_OF_YEAR, -1);
            cal2.add(Calendar.DAY_OF_YEAR, -duration);
            cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
            cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
            cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
            cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
            cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
            cal8.add(Calendar.DAY_OF_YEAR, -(duration * 7));
            cal9.add(Calendar.DAY_OF_YEAR, -(duration * 8));
            cal10.add(Calendar.DAY_OF_YEAR, -(duration * 9));

            Date oneDayBeforeCal1Date = null;
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;
            Date cal8Date = null;
            Date cal9Date = null;
            Date cal10Date = null;

            String oneDayBeforeCal1String = dateFormat.format(oneDayBeforeCal1.getTime());
            oneDayBeforeCal1Date = dateFormat.parse(oneDayBeforeCal1String);

            String cal1String = dateFormat.format(cal1.getTime());
            cal1Date = dateFormat.parse(cal1String);

            String cal2String = dateFormat.format(cal2.getTime());
            cal2Date = dateFormat.parse(cal2String);

            String cal3String = dateFormat.format(cal3.getTime());
            cal3Date = dateFormat.parse(cal3String);

            String cal4String = dateFormat.format(cal4.getTime());
            cal4Date = dateFormat.parse(cal4String);

            String cal5String = dateFormat.format(cal5.getTime());
            cal5Date = dateFormat.parse(cal5String);

            String cal6String = dateFormat.format(cal6.getTime());
            cal6Date = dateFormat.parse(cal6String);

            String cal7String = dateFormat.format(cal7.getTime());
            cal7Date = dateFormat.parse(cal7String);

            String cal8String = dateFormat.format(cal8.getTime());
            cal8Date = dateFormat.parse(cal8String);

            String cal9String = dateFormat.format(cal9.getTime());
            cal9Date = dateFormat.parse(cal9String);

            String cal10String = dateFormat.format(cal10.getTime());
            cal10Date = dateFormat.parse(cal10String);

            Calendar customizeSumryFrom1 = Calendar.getInstance();
            Calendar customizeSumryTo1 = Calendar.getInstance();
            Calendar customizeSumryFrom2 = Calendar.getInstance();
            Calendar customizeSumryTo2 = Calendar.getInstance();
            Calendar customizeSumryFrom3 = Calendar.getInstance();
            Calendar customizeSumryTo3 = Calendar.getInstance();
            Calendar customizeSumryFrom4 = Calendar.getInstance();
            Calendar customizeSumryTo4 = Calendar.getInstance();
            Calendar customizeSumryFrom5 = Calendar.getInstance();
            Calendar customizeSumryTo5 = Calendar.getInstance();
            Calendar customizeSumryFrom6 = Calendar.getInstance();
            Calendar customizeSumryTo6 = Calendar.getInstance();
            Calendar customizeSumryFrom7 = Calendar.getInstance();
            Calendar customizeSumryTo7 = Calendar.getInstance();
            boolean customizedSumryReportFlag = false;
            Map<String, String> amountDueMap = new HashMap<String, String>();
            boolean exportAgedPayables = false;
            boolean isDetailedXls = false;                   // SDP-3687 Aged Payable detailed Xls Currency wise
            if (requestParams.containsKey("exportAgedReceivables") && requestParams.get("exportAgedReceivables") != null) {
                exportAgedPayables = Boolean.parseBoolean(requestParams.get("exportAgedReceivables").toString());
            }
            boolean isAgedPayables = false;
            if (requestParams.containsKey("isAgedPayables") && requestParams.get("isAgedPayables") != null) {
                isAgedPayables = Boolean.parseBoolean(requestParams.get("isAgedPayables").toString());
            }
            if (requestParams.containsKey("detailedXls") && requestParams.get("detailedXls") != null) {
                isDetailedXls = Boolean.parseBoolean(requestParams.get("detailedXls").toString());
            }
            String linedetails = isDetailedXls ? "_line" : "";
            String customizedSumryReportFlagStr = request.getParameter("customizedSummaryReportFlag");
            if (!StringUtil.isNullOrEmpty(customizedSumryReportFlagStr)) {
                customizedSumryReportFlag = Boolean.parseBoolean(customizedSumryReportFlagStr);
                String fromDuration = null;
                String toDuration = null;
                List<String> fromDurationArr = new ArrayList<String>();// request.getParameter("fromDuration").split(",");
                List<String> toDurationArr = new ArrayList<String>();;// request.getParameter("toDuration").split(",");
                JSONArray fromDurationjson = new JSONArray(request.getParameter("fromDuration"));
                JSONArray toDurationjson = new JSONArray(request.getParameter("toDuration"));
                for (int i = 0; i < fromDurationjson.length(); i++) {
                    JSONObject object1 = new JSONObject(fromDurationjson.getString(i));
                    fromDurationArr.add(object1.getString("id"));
                    amountDueMap.put(object1.getString("amountdueindex"), object1.getString("amountdue"));
                    JSONObject object2 = new JSONObject(toDurationjson.getString(i));
                    toDurationArr.add(object2.getString("id"));
                }
                for (int i = 0; i < fromDurationArr.size(); i++) {
                    fromDuration = fromDurationArr.get(i);
                    toDuration = toDurationArr.get(i);
                    switch (i + 1) {
                        case 1:
                            if (!StringUtil.isNullOrEmpty(fromDuration) && !StringUtil.isNullOrEmpty(toDuration)) {
                                int fromDur = Integer.parseInt(fromDuration);
                                int toDur = Integer.parseInt(toDuration);
                                customizeSumryFrom1.setTime(curDate);
                                customizeSumryTo1.setTime(curDate);
                                customizeSumryFrom1.add(Calendar.DAY_OF_YEAR, -fromDur);
                                customizeSumryTo1.add(Calendar.DAY_OF_YEAR, -toDur);
                            }
                            break;
                        case 2:
                            if (!StringUtil.isNullOrEmpty(fromDuration) && !StringUtil.isNullOrEmpty(toDuration)) {
                                int fromDur = Integer.parseInt(fromDuration);
                                int toDur = Integer.parseInt(toDuration);
                                customizeSumryFrom2.setTime(curDate);
                                customizeSumryTo2.setTime(curDate);
                                customizeSumryFrom2.add(Calendar.DAY_OF_YEAR, -fromDur);
                                customizeSumryTo2.add(Calendar.DAY_OF_YEAR, -toDur);
                            }
                            break;
                        case 3:
                            if (!StringUtil.isNullOrEmpty(fromDuration) && !StringUtil.isNullOrEmpty(toDuration)) {
                                int fromDur = Integer.parseInt(fromDuration);
                                int toDur = Integer.parseInt(toDuration);
                                customizeSumryFrom3.setTime(curDate);
                                customizeSumryTo3.setTime(curDate);
                                customizeSumryFrom3.add(Calendar.DAY_OF_YEAR, -fromDur);
                                customizeSumryTo3.add(Calendar.DAY_OF_YEAR, -toDur);
                            }
                            break;
                        case 4:
                            if (!StringUtil.isNullOrEmpty(fromDuration) && !StringUtil.isNullOrEmpty(toDuration)) {
                                int fromDur = Integer.parseInt(fromDuration);
                                int toDur = Integer.parseInt(toDuration);
                                customizeSumryFrom4.setTime(curDate);
                                customizeSumryTo4.setTime(curDate);
                                customizeSumryFrom4.add(Calendar.DAY_OF_YEAR, -fromDur);
                                customizeSumryTo4.add(Calendar.DAY_OF_YEAR, -toDur);
                            }
                            break;
                        case 5:
                            if (!StringUtil.isNullOrEmpty(fromDuration) && !StringUtil.isNullOrEmpty(toDuration)) {
                                int fromDur = Integer.parseInt(fromDuration);
                                int toDur = Integer.parseInt(toDuration);
                                customizeSumryFrom5.setTime(curDate);
                                customizeSumryTo5.setTime(curDate);
                                customizeSumryFrom5.add(Calendar.DAY_OF_YEAR, -fromDur);
                                customizeSumryTo5.add(Calendar.DAY_OF_YEAR, -toDur);
                            }
                            break;
                        case 6:
                            if (!StringUtil.isNullOrEmpty(fromDuration) && !StringUtil.isNullOrEmpty(toDuration)) {
                                int fromDur = Integer.parseInt(fromDuration);
                                int toDur = Integer.parseInt(toDuration);
                                customizeSumryFrom6.setTime(curDate);
                                customizeSumryTo6.setTime(curDate);
                                customizeSumryFrom6.add(Calendar.DAY_OF_YEAR, -fromDur);
                                customizeSumryTo6.add(Calendar.DAY_OF_YEAR, -toDur);
                            }
                            break;
                        case 7:
                            if (!StringUtil.isNullOrEmpty(fromDuration) && !StringUtil.isNullOrEmpty(toDuration)) {
                                int fromDur = Integer.parseInt(fromDuration);
                                int toDur = Integer.parseInt(toDuration);
                                customizeSumryFrom7.setTime(curDate);
                                customizeSumryTo7.setTime(curDate);
                                customizeSumryFrom7.add(Calendar.DAY_OF_YEAR, -fromDur);
                                customizeSumryTo7.add(Calendar.DAY_OF_YEAR, -toDur);
                            }
                            break;
                    }
                }

            }

            Date customizeSumryFrom1Date = null;
            Date customizeSumryTo1Date = null;
            Date customizeSumryFrom2Date = null;
            Date customizeSumryTo2Date = null;
            Date customizeSumryFrom3Date = null;
            Date customizeSumryTo3Date = null;
            Date customizeSumryFrom4Date = null;
            Date customizeSumryTo4Date = null;
            Date customizeSumryFrom5Date = null;
            Date customizeSumryTo5Date = null;
            Date customizeSumryFrom6Date = null;
            Date customizeSumryTo6Date = null;
            Date customizeSumryFrom7Date = null;
            Date customizeSumryTo7Date = null;

            String customizeSumryFrom1String = dateFormat.format(customizeSumryFrom1.getTime());
            customizeSumryFrom1Date = dateFormat.parse(customizeSumryFrom1String);
            String customizeSumryTo1String = dateFormat.format(customizeSumryTo1.getTime());
            customizeSumryTo1Date = dateFormat.parse(customizeSumryTo1String);

            String customizeSumryFrom2String = dateFormat.format(customizeSumryFrom2.getTime());
            customizeSumryFrom2Date = dateFormat.parse(customizeSumryFrom2String);
            String customizeSumryTo2String = dateFormat.format(customizeSumryTo2.getTime());
            customizeSumryTo2Date = dateFormat.parse(customizeSumryTo2String);

            String customizeSumryFrom3String = dateFormat.format(customizeSumryFrom3.getTime());
            customizeSumryFrom3Date = dateFormat.parse(customizeSumryFrom3String);
            String customizeSumryTo3String = dateFormat.format(customizeSumryTo3.getTime());
            customizeSumryTo3Date = dateFormat.parse(customizeSumryTo3String);

            String customizeSumryFrom4String = dateFormat.format(customizeSumryFrom4.getTime());
            customizeSumryFrom4Date = dateFormat.parse(customizeSumryFrom4String);
            String customizeSumryTo4String = dateFormat.format(customizeSumryTo4.getTime());
            customizeSumryTo4Date = dateFormat.parse(customizeSumryTo4String);

            String customizeSumryFrom5String = dateFormat.format(customizeSumryFrom5.getTime());
            customizeSumryFrom5Date = dateFormat.parse(customizeSumryFrom5String);
            String customizeSumryTo5String = dateFormat.format(customizeSumryTo5.getTime());
            customizeSumryTo5Date = dateFormat.parse(customizeSumryTo5String);

            String customizeSumryFrom6String = dateFormat.format(customizeSumryFrom6.getTime());
            customizeSumryFrom6Date = dateFormat.parse(customizeSumryFrom6String);
            String customizeSumryTo6String = dateFormat.format(customizeSumryTo6.getTime());
            customizeSumryTo6Date = dateFormat.parse(customizeSumryTo6String);

            String customizeSumryFrom7String = dateFormat.format(customizeSumryFrom7.getTime());
            customizeSumryFrom7Date = dateFormat.parse(customizeSumryFrom7String);
            String customizeSumryTo7String = dateFormat.format(customizeSumryTo7.getTime());
            customizeSumryTo7Date = dateFormat.parse(customizeSumryTo7String);

            requestParams.put("isCurrencyDetails", isCurrencyDetails);
            boolean avoidrecursive = isCurrencyDetails;

            if (requestParams.containsKey("custVendorID")) {
                String customerid = (String) requestParams.get("custVendorID");
                if (!StringUtil.isNullOrEmpty(customerid)) {
                    if (!customerid.equalsIgnoreCase("All") && !customerid.equals("")) {
                        if (!isCurrencyDetails) {
                            avoidrecursive = true;
                        }
                    }

                }
            }
                        //Advance Search releated common code  
            boolean isAdvanceSearch = false;
            String Searchjson = "";
            String invoiceSearchJson = "";
            String receiptSearchJson = "";
            String cnSearchJson = "";
            String dnSearchJson = "";
            String makePaymentSearchJson = "";
            String filterConjuctionCriteria = com.krawler.common.util.Constants.and;
            if (requestParams.containsKey(Constants.Filter_Criteria) && requestParams.get(Constants.Filter_Criteria) != null) {
                if (requestParams.get(Constants.Filter_Criteria).toString().equalsIgnoreCase("OR")) {
                    filterConjuctionCriteria = com.krawler.common.util.Constants.or;
                }
            }
            if (requestParams.containsKey(Constants.Acc_Search_Json) && requestParams.get(Constants.Acc_Search_Json) != null) {
                Searchjson = requestParams.get(Constants.Acc_Search_Json).toString();
                if (!StringUtil.isNullOrEmpty(Searchjson)) {
                    isAdvanceSearch = true;
                    requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
                    
                    HashMap<String, Object> reqPar1 = new HashMap<>();
                    reqPar1.put(Constants.companyKey, requestParams.get(Constants.companyKey));
                    reqPar1.put(Constants.Acc_Search_Json, Searchjson);
                    reqPar1.put(Constants.Filter_Criteria, filterConjuctionCriteria);
                    reqPar1.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);
                    invoiceSearchJson = fieldDataManagercntrl.getSearchJsonByModuleID(reqPar1);
                    reqPar1.remove(Constants.moduleid);
                    reqPar1.put(Constants.moduleid, Constants.Acc_Receive_Payment_ModuleId);
                    receiptSearchJson = fieldDataManagercntrl.getSearchJsonByModuleID(reqPar1);
                    reqPar1.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
                    dnSearchJson = fieldDataManagercntrl.getSearchJsonByModuleID(reqPar1);
                    reqPar1.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                    cnSearchJson = fieldDataManagercntrl.getSearchJsonByModuleID(reqPar1);
                    reqPar1.put(Constants.moduleid, Constants.Acc_Make_Payment_ModuleId);
                    makePaymentSearchJson = fieldDataManagercntrl.getSearchJsonByModuleID(reqPar1);
                }
            }
            /**
             * Get Only Parent Vendors
             */
            KwlReturnObject result = accVendorDAOobj.getVendorAndCurrencyDetailsForParentAgedPayable(requestParams);
            requestParams.put("currencyid", sessionHandlerImpl.getCompanyid(request));
            /**
             * Return list with Child
             */
            ArrayList list = accVendorDAOobj.getVendorArrayList(result.getEntityList(), requestParams, avoidrecursive, false);

            if (requestParams.containsKey("start")) { //Removing paging here because it is applying on each vendor data (CN/DN etc). Due to this complete amount due not showing in AP Summary.  
                requestParams.remove("start");
            }
            if (requestParams.containsKey("limit")) {
                requestParams.remove("limit");
            }
            requestParams.put("oneDayBeforeCal1", oneDayBeforeCal1);
            requestParams.put("cal1", cal1);
            requestParams.put("cal2", cal2);
            requestParams.put("cal3", cal3);
            requestParams.put("cal4", cal4);
            requestParams.put("cal5", cal5);
            requestParams.put("cal6", cal6);
            requestParams.put("cal7", cal7);
            requestParams.put("cal8", cal8);
            requestParams.put("cal9", cal9);
            requestParams.put("cal10", cal10);
            if (isAgedPayables) {
                requestParams.put("isAgedPayables", isAgedPayables);
            }
            Iterator itr1 = list.iterator();
            while (itr1.hasNext()) {
                Object[] row = (Object[]) itr1.next();
                String tempstring = "";
                String productsCode = "";
                String productName = "";
                Vendor vendor = (Vendor) row[1];
                Object venid = vendor.getID();
                requestParams.put(GoodsReceiptCMNConstants.VENDORID, venid);
                requestParams.put(GoodsReceiptCMNConstants.ACCID, venid);
                requestParams.put("cntype", null);
                requestParams.put("isAgedSummary", true);
                JSONArray invjarr = new JSONArray();
                if (isAdvanceSearch) {
                    requestParams.put(Constants.Acc_Search_Json, invoiceSearchJson);
                    requestParams.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);
                    requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
                }
                result = accGoodsReceiptobj.getGoodsReceiptsMerged(requestParams);
                invoiceCount += result.getEntityList().size();
                if (result != null && !result.getEntityList().isEmpty()) {
                    invjarr = accGoodsReceiptServiceHandler.getGoodsReceiptsJsonForAgedPayables(requestParams, result.getEntityList(), invjarr, accountingHandlerDAOobj, accCurrencyDAOobj, accGoodsReceiptobj, accAccountDAOobj, accGoodsReceiptCommon, accTaxObj);
                    jsonCount += invjarr.length();
                }
                JSONArray OBJArryInvoice = new JSONArray();
                result = accGoodsReceiptobj.getOpeningBalanceInvoices(requestParams);
                openingCount += result.getEntityList().size();
                if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                    OBJArryInvoice = AccGoodsReceiptServiceHandler.getAgedPayablesOpeningBalanceInvoiceJson(requestParams, result.getEntityList(), OBJArryInvoice, accCurrencyDAOobj, accountingHandlerDAOobj, accGoodsReceiptCommon);
                    openingJSONCount += OBJArryInvoice.length();
                    for (int i = 0; i < OBJArryInvoice.length(); i++) {
                        invjarr.put(OBJArryInvoice.get(i));
                    }
                }

                if (!templatesubtype.equals(Constants.TEMPLATE_SUBTYPE_SOI)) {
                    if (isAdvanceSearch) {
                        requestParams.put(Constants.Acc_Search_Json, dnSearchJson);
                        requestParams.put(Constants.moduleid, Constants.Acc_Debit_Note_ModuleId);
                        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
                    }
                    JSONArray OBJArryDebitNote = new JSONArray();
                    result = accDebitNoteobj.getOpeningBalanceDNs(requestParams);
                    if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        OBJArryDebitNote = AccGoodsReceiptServiceHandler.getAgedOpeningBalanceDebitNoteJson(requestParams, result.getEntityList(), OBJArryDebitNote, accCurrencyDAOobj, accountingHandlerDAOobj, accReceiptDAOobj, accDebitNoteobj, accAccountDAOobj, fieldDataManagercntrl);
                        for (int i = 0; i < OBJArryDebitNote.length(); i++) {
                            invjarr.put(OBJArryDebitNote.get(i));
                        }
                    }
                }

                if (!templatesubtype.equals(Constants.TEMPLATE_SUBTYPE_SOI)) {
                    if (isAdvanceSearch) {
                        requestParams.put(Constants.Acc_Search_Json, cnSearchJson);
                        requestParams.put(Constants.moduleid, Constants.Acc_Credit_Note_ModuleId);
                        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
                    }
                    JSONArray OBJArryCreditNote = new JSONArray();
                    result = accCreditNoteDAOobj.getOpeningBalanceVendorCNs(requestParams);
                    if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        OBJArryCreditNote = AccGoodsReceiptServiceHandler.getAgedOpeningBalanceCreditNoteJson(requestParams, result.getEntityList(), OBJArryCreditNote, accCurrencyDAOobj, accountingHandlerDAOobj, accPaymentDAOobj, accVendorPaymentobj, accDebitNoteobj, accAccountDAOobj, fieldDataManagercntrl);
                        for (int i = 0; i < OBJArryCreditNote.length(); i++) {
                            invjarr.put(OBJArryCreditNote.get(i));
                        }
                    }
                }

                if (!templatesubtype.equals(Constants.TEMPLATE_SUBTYPE_SOI)) {
                    if (isAdvanceSearch) {
                        requestParams.put(Constants.Acc_Search_Json, makePaymentSearchJson);
                        requestParams.put(Constants.moduleid, Constants.Acc_Make_Payment_ModuleId);
                        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
                    }
                    JSONArray OBJArryPayment = new JSONArray();
                    result = accVendorPaymentobj.getOpeningBalancePayments(requestParams);
                    if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        OBJArryPayment = AccGoodsReceiptServiceHandler.getAgedOpeningBalancePaymentJson(requestParams, result.getEntityList(), OBJArryPayment, accCurrencyDAOobj, accountingHandlerDAOobj, accVendorPaymentobj, accAccountDAOobj, fieldDataManagercntrl);
                        for (int i = 0; i < OBJArryPayment.length(); i++) {
                            invjarr.put(OBJArryPayment.get(i));
                        }
                    }
                }

                if (!templatesubtype.equals(Constants.TEMPLATE_SUBTYPE_SOI)) {
                    if (isAdvanceSearch) {
                        requestParams.put(Constants.Acc_Search_Json, dnSearchJson);
                        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
                    }
                    JSONArray DebitNotejArr = new JSONArray();//This is used for getting DN gainst vendor and otherwise 
                    result = accDebitNoteobj.getDebitNoteMerged(requestParams);
                    if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        DebitNotejArr = AccGoodsReceiptServiceHandler.getDebitNotesMergedJson(requestParams, result.getEntityList(), DebitNotejArr, accCurrencyDAOobj, accountingHandlerDAOobj, accReceiptDAOobj, accDebitNoteobj, accAccountDAOobj, fieldDataManagercntrl, accJournalEntryobj);
                        for (int i = 0; i < DebitNotejArr.length(); i++) {
                            invjarr.put(DebitNotejArr.get(i));
                        }
                    }
                }

                if (!templatesubtype.equals(Constants.TEMPLATE_SUBTYPE_SOI)) {
                    if (isAdvanceSearch) {
                        requestParams.put(Constants.Acc_Search_Json, cnSearchJson);
                        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
                    }
                    requestParams.put("cntype", 4);//This is used for getting Credit note against vendor 
                    JSONArray CreditNotejArr = new JSONArray();
                    result = accCreditNoteDAOobj.getCreditNoteMerged(requestParams);
                    if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        CreditNotejArr = AccGoodsReceiptServiceHandler.getCreditNotesMergedJson(requestParams, result.getEntityList(), CreditNotejArr, accCurrencyDAOobj, accountingHandlerDAOobj, accPaymentDAOobj, accVendorPaymentobj, accDebitNoteobj, accAccountDAOobj, fieldDataManagercntrl, accJournalEntryobj);
                        for (int i = 0; i < CreditNotejArr.length(); i++) {
                            invjarr.put(CreditNotejArr.get(i));
                        }
                    }
                }

                if (!templatesubtype.equals(Constants.TEMPLATE_SUBTYPE_SOI)) {
                    if (isAdvanceSearch) {
                        requestParams.put(Constants.Acc_Search_Json, makePaymentSearchJson);
                        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
                    }
                    JSONArray makePaymentJArr = new JSONArray();
                    requestParams.put("allAdvPayment", true); // needs only Advance type record so that putted true
                    requestParams.put("paymentWindowType", 1);//Payment to Vendor record
                    result = accVendorPaymentobj.getPayments(requestParams);
                    if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        makePaymentJArr = AccGoodsReceiptServiceHandler.getPaymentsJson(requestParams, result.getEntityList(), makePaymentJArr, accCurrencyDAOobj, accountingHandlerDAOobj, accVendorPaymentobj);
                        for (int i = 0; i < makePaymentJArr.length(); i++) {
                            invjarr.put(makePaymentJArr.get(i));
                        }
                    }
                }

                if (!templatesubtype.equals(Constants.TEMPLATE_SUBTYPE_SOI)) {
                    if (isAdvanceSearch) {
                        requestParams.put(Constants.Acc_Search_Json, receiptSearchJson);
                        requestParams.put(Constants.Filter_Criteria, request.getParameter(Constants.Filter_Criteria));
                    }
                    if (!isAgedPayables && isCurrencyDetails) { //need to put requestParams for this condition
                        requestParams.put("isAgedPayables", true);
                    }
                    requestParams.put("allAdvPayment", true); // needs only refund type record so that putted true
                    requestParams.put("paymentWindowType", 2);//Receipt to Vendor record
                    JSONArray receivePaymentJArr = new JSONArray();
                    result = accReceiptDAOobj.getReceipts(requestParams);
                    receivePaymentJArr = AccInvoiceServiceHandler.getReceiptsJson(requestParams, result.getEntityList(), receivePaymentJArr, accountingHandlerDAOobj, authHandlerDAOObj, accCurrencyDAOobj, accReceiptDAOobj, request, accAccountDAOobj, fieldDataManagercntrl, accJournalEntryobj);
                    for (int i = 0; i < receivePaymentJArr.length(); i++) {
                        invjarr.put(receivePaymentJArr.get(i));
                    }
                    requestParams.remove("allAdvPayment");
                    requestParams.remove("paymentWindowType");
                    if (!isAgedPayables && isCurrencyDetails) { //need to remove requestParams for this condition as we have putted above
                        requestParams.remove("isAgedPayables");
                    }
                }
                if (invjarr.length() < 1) {
                    continue;
                }
                amountdue1 = amountdue2 = amountdue3 = amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                amountdue1Base = amountdue2Base = amountdue3Base = amountdue4Base = amountdue5Base = amountdue6Base = amountdue7Base = amountdue8Base = amountdue9Base = amountdue10Base = amountdue11Base = 0;
                String personID = null;
                String personName = null;
                String aliasname = "";
                String currencySymbol = null;
                String currencyid = null;
                boolean isDN = false;
                boolean isMP = false;
                String currencyidVen = vendor.getCurrency().getCurrencyID();
                String currencyName = vendor.getCurrency().getName();
                String currencySymbolVen = vendor.getCurrency().getSymbol();

                if (!isCurrencyDetails) {
                    for (int i = 0; i < invjarr.length(); i++) {
                        JSONObject invobj = invjarr.getJSONObject(i);
                        personID = invobj.getString(GoodsReceiptCMNConstants.PERSONID);
                        personName = invobj.getString(GoodsReceiptCMNConstants.PERSONNAME);
                        aliasname = invobj.has(GoodsReceiptCMNConstants.ALIASNAME) ? (invobj.getString(GoodsReceiptCMNConstants.ALIASNAME) != null ? invobj.getString(GoodsReceiptCMNConstants.ALIASNAME) : "") : "";
                        currencySymbol = invobj.getString(GoodsReceiptCMNConstants.CURRENCYSYMBOL);
                        currencyid = invobj.getString(GoodsReceiptCMNConstants.CURRENCYID);

                        isDN = invobj.has("isDN") ? invobj.getBoolean("isDN") : false;
                        isMP = invobj.has("isMP") ? invobj.getBoolean("isMP") : false;
                        Date dueDate = null;
                        if (!StringUtil.isNullOrEmpty(invobj.getString(GoodsReceiptCMNConstants.DUEDATE))) {
                            dueDate = authHandler.getDateOnlyFormat(request).parse(invobj.getString(GoodsReceiptCMNConstants.DUEDATE));
                        }

                        if (datefilter == 0 || datefilter == Constants.agedDueDate0to30Filter) {
                            dueDate = authHandler.getDateOnlyFormat(request).parse(invobj.getString(GoodsReceiptCMNConstants.DUEDATE));
                        } else {
                            dueDate = authHandler.getDateOnlyFormat(request).parse(invobj.getString(GoodsReceiptCMNConstants.DATE));
                        }

                        double amountdue = invobj.has(GoodsReceiptCMNConstants.AMOUNTDUE) ? invobj.getDouble(GoodsReceiptCMNConstants.AMOUNTDUE) : 0;
                        double amountdueinbase = invobj.has(GoodsReceiptCMNConstants.AMOUNTDUEINBASE) ? invobj.getDouble(GoodsReceiptCMNConstants.AMOUNTDUEINBASE) : 0;
                        double externalcurrencyrate = invobj.getDouble(GoodsReceiptCMNConstants.EXTERNALCURRENCYRATE);

                        boolean isopeningBalanceTransaction = invobj.optBoolean(GoodsReceiptCMNConstants.IsopeningBalanceTransaction, false);
                        boolean isConversionRateFromCurrencyToBase = invobj.optBoolean(GoodsReceiptCMNConstants.IsConversionRateFromCurrencyToBase, false);

                        KwlReturnObject bAmt = null;
                        if (isopeningBalanceTransaction && isConversionRateFromCurrencyToBase) {
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, currencyid, currencyidVen, df.parse(invobj.getString(GoodsReceiptCMNConstants.DATE)), externalcurrencyrate);
                        } else {
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, currencyid, currencyidVen, df.parse(invobj.getString(GoodsReceiptCMNConstants.DATE)), externalcurrencyrate);
                        }
                        amountdue = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

                        if (isDN || isMP) {
                            if (!isopeningBalanceTransaction) {
                                amountdue = -amountdue;
                                amountdueinbase = -amountdueinbase;
                            }
                        }

                        if (customizedSumryReportFlag) {
                            if ((dueDate.after(customizeSumryTo1Date) || dueDate.equals(customizeSumryTo1Date)) && (dueDate.before(customizeSumryFrom1Date) || dueDate.equals(customizeSumryFrom1Date))) {
                                amountdue1 += amountdue;
                                amountdue1Base += amountdueinbase;
                            } else if ((dueDate.after(customizeSumryTo2Date) || dueDate.equals(customizeSumryTo2Date)) && (dueDate.before(customizeSumryFrom2Date) || dueDate.equals(customizeSumryFrom2Date))) {
                                amountdue2 += amountdue;
                                amountdue2Base += amountdueinbase;
                            } else if ((dueDate.after(customizeSumryTo3Date) || dueDate.equals(customizeSumryTo3Date)) && (dueDate.before(customizeSumryFrom3Date) || dueDate.equals(customizeSumryFrom3Date))) {
                                amountdue3 += amountdue;
                                amountdue3Base += amountdueinbase;
                            } else if ((dueDate.after(customizeSumryTo4Date) || dueDate.equals(customizeSumryTo4Date)) && (dueDate.before(customizeSumryFrom4Date) || dueDate.equals(customizeSumryFrom4Date))) {
                                amountdue4 += amountdue;
                                amountdue4Base += amountdueinbase;
                            } else if ((dueDate.after(customizeSumryTo5Date) || dueDate.equals(customizeSumryTo5Date)) && (dueDate.before(customizeSumryFrom5Date) || dueDate.equals(customizeSumryFrom5Date))) {
                                amountdue5 += amountdue;
                                amountdue5Base += amountdueinbase;
                            } else if ((dueDate.after(customizeSumryTo6Date) || dueDate.equals(customizeSumryTo6Date)) && (dueDate.before(customizeSumryFrom6Date) || dueDate.equals(customizeSumryFrom6Date))) {
                                amountdue6 += amountdue;
                                amountdue6Base += amountdueinbase;
                            } else if ((dueDate.after(customizeSumryTo7Date) || dueDate.equals(customizeSumryTo7Date)) && (dueDate.before(customizeSumryFrom7Date) || dueDate.equals(customizeSumryFrom7Date))) {
                                amountdue7 += amountdue;
                                amountdue7Base += amountdueinbase;
                            }
                        } else {
                            if (isdistibutive) {

//                                if (startDate != null && dueDate.before(startDate)) {//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
//                                    accruedbalance += amountdue;
//                                    accruedbalanceBase += amountdueinbase;
//                                } else 
                                if (dueDate.after(oneDayBeforeCal1Date)) {
                                    if (dueDate.equals(cal1Date) && (datefilter == Constants.agedDueDate0to30Filter || datefilter == Constants.agedInvoiceDate0to30Filter)) {
                                        amountdue2 += amountdue;
                                        amountdue2Base += amountdueinbase;
                                    } else {
                                        amountdue1 += amountdue;
                                        amountdue1Base += amountdueinbase;
                                    }
                                } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) {
                                    amountdue2 += amountdue;
                                    amountdue2Base += amountdueinbase;
                                } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) {
                                    amountdue3 += amountdue;
                                    amountdue3Base += amountdueinbase;
                                } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) {
                                    amountdue4 += amountdue;
                                    amountdue4Base += amountdueinbase;
                                } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) {
                                    amountdue5 += amountdue;
                                    amountdue5Base += amountdueinbase;
                                } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) {
                                    amountdue6 += amountdue;
                                    amountdue6Base += amountdueinbase;
                                } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) {
                                    amountdue7 += amountdue;
                                    amountdue7Base += amountdueinbase;
                                } else if ((cal8Date.before(dueDate) || cal8Date.equals(dueDate)) && cal7Date.after(dueDate)) {
                                    amountdue8 += amountdue;
                                    amountdue8Base += amountdueinbase;
                                } else if ((cal9Date.before(dueDate) || cal9Date.equals(dueDate)) && cal8Date.after(dueDate)) {
                                    amountdue9 += amountdue;
                                    amountdue9Base += amountdueinbase;
                                } else if ((cal10Date.before(dueDate) || cal10Date.equals(dueDate)) && cal9Date.after(dueDate)) {
                                    amountdue10 += amountdue;
                                    amountdue10Base += amountdueinbase;
                                } else {
                                    amountdue11 += amountdue;
                                    amountdue11Base += amountdueinbase;
                                }
                                
                                switch(noOfInterval){
                                    case 2:
                                        amountdue3 += amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                        amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                        amountdue3Base += amountdue4Base + amountdue5Base + amountdue6Base + amountdue7Base + amountdue8Base + amountdue9Base + amountdue10Base + amountdue11Base;
                                        amountdue4Base = amountdue5Base = amountdue6Base = amountdue7Base = amountdue8Base = amountdue9Base = amountdue10Base = amountdue11Base = 0;
                                        break;
                                    case 3:
                                        amountdue4 += amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                        amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                        amountdue4Base += amountdue5Base + amountdue6Base + amountdue7Base + amountdue8Base + amountdue9Base + amountdue10Base + amountdue11Base;
                                        amountdue5Base = amountdue6Base = amountdue7Base = amountdue8Base = amountdue9Base = amountdue10Base = amountdue11Base = 0;
                                        break;
                                    case 4:
                                        amountdue5 += amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                        amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                        amountdue5Base += amountdue6Base + amountdue7Base + amountdue8Base + amountdue9Base + amountdue10Base + amountdue11Base;
                                        amountdue6Base = amountdue7Base = amountdue8Base = amountdue9Base = amountdue10Base = amountdue11Base = 0;
                                        break;
                                    case 5:
                                        amountdue6 += amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                        amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                        amountdue6Base += amountdue7Base + amountdue8Base + amountdue9Base + amountdue10Base + amountdue11Base;
                                        amountdue7Base = amountdue8Base = amountdue9Base = amountdue10Base = amountdue11Base = 0;
                                        break;
                                    case 6:
                                        amountdue7 += amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                        amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                        amountdue7Base += amountdue8Base + amountdue9Base + amountdue10Base + amountdue11Base;
                                        amountdue8Base = amountdue9Base = amountdue10Base = amountdue11Base = 0;
                                        break;
                                    case 7:
                                        amountdue8Base += amountdue9Base + amountdue10Base + amountdue11Base;
                                        amountdue9Base = amountdue10Base = amountdue11Base = 0;
                                        amountdue8Base += amountdue9Base + amountdue10Base + amountdue11Base;
                                        amountdue9Base = amountdue10Base = amountdue11Base = 0;
                                        break;
                                    case 8:
                                        amountdue9 += amountdue10 + amountdue11;
                                        amountdue10 = amountdue11 = 0;
                                        amountdue9Base += amountdue10Base + amountdue11Base;
                                        amountdue10Base = amountdue11Base = 0;
                                        break;
                                    case 9:
                                        amountdue10 += amountdue11;
                                        amountdue11 = 0;
                                        amountdue10Base += amountdue11Base;
                                        amountdue11Base = 0;
                                        break;
                                }
                                
                            } else {
//                                if (startDate != null && dueDate.before(startDate)) {//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
//                                    accruedbalance += amountdue;
//                                    accruedbalanceBase += amountdueinbase;
//                                } else 
                                if (dueDate.after(oneDayBeforeCal1Date) && (dueDate.before(cal1Date) || dueDate.equals(cal1Date))) {
                                    amountdue1 += amountdue;
                                    amountdue1Base += amountdueinbase;
                                } else if (dueDate.after(cal2Date) || dueDate.equals(cal2Date)) {
                                    amountdue2 += amountdue;
                                    amountdue2Base += amountdueinbase;
                                } else if (dueDate.after(cal3Date) || dueDate.equals(cal3Date)) {
                                    amountdue3 += amountdue;
                                    amountdue3Base += amountdueinbase;
                                } else {
                                    amountdue4 += amountdue;
                                    amountdue4Base += amountdueinbase;
                                }
                            }
                        }
                    }
                    if (invjarr.length() > 0) {
                        jObj = new JSONObject();
                        jObj.put(GoodsReceiptCMNConstants.PERSONID, personID);
                        jObj.put(GoodsReceiptCMNConstants.PERSONNAME, personName);
                        jObj.put(GoodsReceiptCMNConstants.ALIASNAME, aliasname);
                        jObj.put(GoodsReceiptCMNConstants.CURRENCYID, currencyidVen);
                        jObj.put(GoodsReceiptCMNConstants.CURRENCYNAME, currencyName);
                        jObj.put("level", row[2]);
                        jObj.put("leaf", row[3]);
                        if (!customizedSumryReportFlag) {
                            jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE1, Double.valueOf(authHandler.formattedAmount(amountdue1, companyid)));
                            jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE2, Double.valueOf(authHandler.formattedAmount(amountdue2, companyid)));
                            jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE3, Double.valueOf(authHandler.formattedAmount(amountdue3, companyid)));
                            jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE4, Double.valueOf(authHandler.formattedAmount(amountdue4, companyid)));
                            jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE5, Double.valueOf(authHandler.formattedAmount(amountdue5, companyid)));
                            jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE6, Double.valueOf(authHandler.formattedAmount(amountdue6, companyid)));
                            jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE7, Double.valueOf(authHandler.formattedAmount(amountdue7, companyid)));
                            jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE8, Double.valueOf(authHandler.formattedAmount(amountdue8, companyid)));
                            jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE9, Double.valueOf(authHandler.formattedAmount(amountdue9, companyid)));
                            jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE10, Double.valueOf(authHandler.formattedAmount(amountdue10, companyid)));
                            jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE11, Double.valueOf(authHandler.formattedAmount(amountdue11, companyid)));
//                            jObj.put(GoodsReceiptCMNConstants.ACCRUEDBALANCE, Double.valueOf(authHandler.formattedAmount(accruedbalance, companyid)));
                            jObj.put(GoodsReceiptCMNConstants.CURRENCYSYMBOL, currencySymbolVen);
                        } else {
                            if (amountDueMap.containsKey("amountdue1")) {
                                jObj.put(amountDueMap.get("amountdue1"), Double.valueOf(authHandler.formattedAmount(amountdue1, companyid)));
                            }
                            if (amountDueMap.containsKey("amountdue2")) {
                                jObj.put(amountDueMap.get("amountdue2"), Double.valueOf(authHandler.formattedAmount(amountdue2, companyid)));
                            }
                            if (amountDueMap.containsKey("amountdue3")) {
                                jObj.put(amountDueMap.get("amountdue3"), Double.valueOf(authHandler.formattedAmount(amountdue3, companyid)));
                            }
                            if (amountDueMap.containsKey("amountdue4")) {
                                jObj.put(amountDueMap.get("amountdue4"), Double.valueOf(authHandler.formattedAmount(amountdue4, companyid)));
                            }
                            if (amountDueMap.containsKey("amountdue5")) {
                                jObj.put(amountDueMap.get("amountdue5"), Double.valueOf(authHandler.formattedAmount(amountdue5, companyid)));
                            }
                            if (amountDueMap.containsKey("amountdue6")) {
                                jObj.put(amountDueMap.get("amountdue6"), Double.valueOf(authHandler.formattedAmount(amountdue6, companyid)));
                            }
                            if (amountDueMap.containsKey("amountdue7")) {
                                jObj.put(amountDueMap.get("amountdue7"), Double.valueOf(authHandler.formattedAmount(amountdue7, companyid)));
                            }
                            if (amountDueMap.containsKey("amountdue8")) {
                                jObj.put(amountDueMap.get("amountdue8"), Double.valueOf(authHandler.formattedAmount(amountdue8, companyid)));
                            }
                        }
                        jObj.put("amountdueinbase1", Double.valueOf(authHandler.formattedAmount(amountdue1Base, companyid)));
                        jObj.put("amountdueinbase2", Double.valueOf(authHandler.formattedAmount(amountdue2Base, companyid)));
                        jObj.put("amountdueinbase3", Double.valueOf(authHandler.formattedAmount(amountdue3Base, companyid)));
                        jObj.put("amountdueinbase4", Double.valueOf(authHandler.formattedAmount(amountdue4Base, companyid)));
                        jObj.put("amountdueinbase5", Double.valueOf(authHandler.formattedAmount(amountdue5Base, companyid)));
                        jObj.put("amountdueinbase6", Double.valueOf(authHandler.formattedAmount(amountdue6Base, companyid)));
                        jObj.put("amountdueinbase7", Double.valueOf(authHandler.formattedAmount(amountdue7Base, companyid)));
                        jObj.put("amountdueinbase8", Double.valueOf(authHandler.formattedAmount(amountdue8Base, companyid)));
                        jObj.put("amountdueinbase9", Double.valueOf(authHandler.formattedAmount(amountdue9Base, companyid)));
                        jObj.put("amountdueinbase10", Double.valueOf(authHandler.formattedAmount(amountdue10Base, companyid)));
                        jObj.put("amountdueinbase11", Double.valueOf(authHandler.formattedAmount(amountdue11Base, companyid)));
//                        jObj.put("accruedbalanceinbase", Double.valueOf(authHandler.formattedAmount(accruedbalanceBase, companyid)));
                        double amountdue = Double.valueOf(authHandler.formattedAmount((amountdue1 + amountdue2 + amountdue3 + amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11), companyid));
                        double amountdueBase = Double.valueOf(authHandler.formattedAmount((amountdue1Base + amountdue2Base + amountdue3Base + amountdue4Base + amountdue5Base + amountdue6Base + amountdue7Base + amountdue8Base + amountdue9Base + amountdue10Base + amountdue11Base), companyid));
                        jObj.put(GoodsReceiptCMNConstants.TOTAL, amountdue);
                        jObj.put("totalinbase", amountdueBase);
                        totalinbase += amountdueBase;
                        if (customizedSumryReportFlag && !showCustVendorWithZeroAmounts && amountdue == 0) {
                            continue;
                        }
                        jArr.put(jObj);
                    }
                }
                if (isCurrencyDetails || isDetailedXls) {
                    JSONObject currencyObj = new JSONObject();
                    for (int i = 0; i < invjarr.length(); i++) {
                        amountdue1 = amountdue2 = amountdue3 = amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                        amountdueinbase1 = amountdueinbase2 = amountdueinbase3 = amountdueinbase4 = amountdueinbase5 = amountdueinbase6 = amountdueinbase7 = amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                        JSONObject invobj = invjarr.getJSONObject(i);
                        JSONObject putObj = new JSONObject();
                        if (i == 0) {
                            personID = (invobj.has("personid")) ? invobj.getString("personid") : "";
                            personName = (invobj.has("personname")) ? invobj.getString("personname") : "";
                            aliasname = (invobj.has("aliasname")) ? invobj.getString("aliasname") : "";
                            currencySymbol = (invobj.has("currencysymbol")) ? invobj.getString("currencysymbol") : "";
                            currencyid = (invobj.has("currencyid")) ? invobj.getString("currencyid") : "";
                            currencyName = (invobj.has("currencyname")) ? invobj.getString("currencyname") : "";
                            isDN = invobj.has("isDN") ? invobj.getBoolean("isDN") : false;
                            isMP = invobj.has("isMP") ? invobj.getBoolean("isMP") : false;
                            Date dueDate = new Date();
                            if (datefilter == 0 || datefilter == Constants.agedDueDate0to30Filter) {
                                dueDate = authHandler.getDateOnlyFormat(request).parse(invobj.getString("duedate"));
                            } else {
                                dueDate = authHandler.getDateOnlyFormat(request).parse(invobj.getString("date"));
                            }
                            double amountdue = invobj.getDouble("amountdue");
                            double amountdueinbase = invobj.getDouble("amountdueinbase");
                            boolean isopeningBalanceTransaction = invobj.optBoolean(GoodsReceiptCMNConstants.IsopeningBalanceTransaction, false);

                            if (isDN || isMP) {
                                if (!isopeningBalanceTransaction) {
                                    amountdue = -amountdue;
                                    amountdueinbase = -amountdueinbase;
                                }
                            }

                            if (customizedSumryReportFlag) {
                                if ((dueDate.after(customizeSumryTo1Date) || dueDate.equals(customizeSumryTo1Date)) && (dueDate.before(customizeSumryFrom1Date) || dueDate.equals(customizeSumryFrom1Date))) {
                                    amountdue1 += amountdue;
                                    amountdueinbase1 += amountdueinbase;
                                } else if ((dueDate.after(customizeSumryTo2Date) || dueDate.equals(customizeSumryTo2Date)) && (dueDate.before(customizeSumryFrom2Date) || dueDate.equals(customizeSumryFrom2Date))) {
                                    amountdue2 += amountdue;
                                    amountdueinbase2 += amountdueinbase;
                                } else if ((dueDate.after(customizeSumryTo3Date) || dueDate.equals(customizeSumryTo3Date)) && (dueDate.before(customizeSumryFrom3Date) || dueDate.equals(customizeSumryFrom3Date))) {
                                    amountdue3 += amountdue;
                                    amountdueinbase3 += amountdueinbase;
                                } else if ((dueDate.after(customizeSumryTo4Date) || dueDate.equals(customizeSumryTo4Date)) && (dueDate.before(customizeSumryFrom4Date) || dueDate.equals(customizeSumryFrom4Date))) {
                                    amountdue4 += amountdue;
                                    amountdueinbase4 += amountdueinbase;
                                } else if ((dueDate.after(customizeSumryTo5Date) || dueDate.equals(customizeSumryTo5Date)) && (dueDate.before(customizeSumryFrom5Date) || dueDate.equals(customizeSumryFrom5Date))) {
                                    amountdue5 += amountdue;
                                    amountdueinbase5 += amountdueinbase;
                                } else if ((dueDate.after(customizeSumryTo6Date) || dueDate.equals(customizeSumryTo6Date)) && (dueDate.before(customizeSumryFrom6Date) || dueDate.equals(customizeSumryFrom6Date))) {
                                    amountdue6 += amountdue;
                                    amountdueinbase6 += amountdueinbase;
                                } else if ((dueDate.after(customizeSumryTo7Date) || dueDate.equals(customizeSumryTo7Date)) && (dueDate.before(customizeSumryFrom7Date) || dueDate.equals(customizeSumryFrom7Date))) {
                                    amountdue7 += amountdue;
                                    amountdueinbase7 += amountdueinbase;
                                }

                            } else {
                                if (isdistibutive) {
//                                    if (startDate != null && dueDate.before(startDate)) {//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
//                                        accruedbalance += amountdue;
//                                        accruedbalanceinbase += amountdueinbase;
//                                    } else 
                                    if (dueDate.after(oneDayBeforeCal1Date)) {
                                        if (dueDate.equals(cal1Date) && (datefilter == Constants.agedDueDate0to30Filter || datefilter == Constants.agedInvoiceDate0to30Filter)) {
                                            amountdueinbase2 += amountdueinbase;
                                            amountdue2 += amountdue;
                                        } else {
                                            amountdueinbase1 += amountdueinbase;
                                            amountdue1 += amountdue;
                                        }
                                    } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) {
                                        amountdue2 += amountdue;
                                        amountdueinbase2 += amountdueinbase;
                                    } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) {
                                        amountdue3 += amountdue;
                                        amountdueinbase3 += amountdueinbase;
                                    } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) {
                                        amountdue4 += amountdue;
                                        amountdueinbase4 += amountdueinbase;
                                    } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) {
                                        amountdue5 += amountdue;
                                        amountdueinbase5 += amountdueinbase;
                                    } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) {
                                        amountdue6 += amountdue;
                                        amountdueinbase6 += amountdueinbase;
                                    } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) {
                                        amountdue7 += amountdue;
                                        amountdueinbase7 += amountdueinbase;
                                    } else if ((cal8Date.before(dueDate) || cal8Date.equals(dueDate)) && cal7Date.after(dueDate)) {
                                        amountdue8 += amountdue;
                                        amountdueinbase8 += amountdueinbase;
                                    } else if ((cal9Date.before(dueDate) || cal9Date.equals(dueDate)) && cal8Date.after(dueDate)) {
                                        amountdue9 += amountdue;
                                        amountdueinbase9 += amountdueinbase;
                                    } else if ((cal10Date.before(dueDate) || cal10Date.equals(dueDate)) && cal9Date.after(dueDate)) {
                                        amountdue10 += amountdue;
                                        amountdueinbase10 += amountdueinbase;
                                    } else {
                                        amountdue11 += amountdue;
                                        amountdueinbase11 += amountdueinbase;
                                    }
                                    
                                    switch(noOfInterval){
                                        case 2:
                                            amountdue3 += amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                            amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                            amountdueinbase3 += amountdueinbase4 + amountdueinbase5 + amountdueinbase6 + amountdueinbase7 + amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                            amountdueinbase4 = amountdueinbase5 = amountdueinbase6 = amountdueinbase7 = amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                            break;
                                        case 3:
                                            amountdue4 += amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                            amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                            amountdueinbase4 += amountdueinbase5 + amountdueinbase6 + amountdueinbase7 + amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                            amountdueinbase5 = amountdueinbase6 = amountdueinbase7 = amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                            break;
                                        case 4:
                                            amountdue5 += amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                            amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                            amountdueinbase5 += amountdueinbase6 + amountdueinbase7 + amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                            amountdueinbase6 = amountdueinbase7 = amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                            break;
                                        case 5:
                                            amountdue6 += amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                            amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                            amountdueinbase6 += amountdueinbase7 + amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                            amountdueinbase7 = amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                            break;
                                        case 6:
                                            amountdue7 += amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                            amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                            amountdueinbase7 += amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                            amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                            break;
                                        case 7:
                                            amountdue8 += amountdue9 + amountdue10 + amountdue11;
                                            amountdue9 = amountdue10 = amountdue11 = 0;
                                            amountdueinbase8 += amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                            amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                            break;
                                        case 8:
                                            amountdue9 += amountdue10 + amountdue11;
                                            amountdue10 = amountdue11 = 0;
                                            amountdueinbase9 += amountdueinbase10 + amountdueinbase11;
                                            amountdueinbase10 = amountdueinbase11 = 0;
                                            break;
                                        case 9:
                                            amountdue10 += amountdue11;
                                            amountdue11 = 0;
                                            amountdueinbase10 += amountdueinbase11;
                                            amountdueinbase11 = 0;
                                            break;
                                    }
                                    
                                } else {
//                                    if (startDate != null && dueDate.before(startDate)) {//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
//                                        accruedbalance += amountdue;
//                                        accruedbalanceinbase += amountdueinbase;
//                                    } else 
                                    if (dueDate.after(oneDayBeforeCal1Date) && (dueDate.before(cal1Date) || dueDate.equals(cal1Date))) {
                                        amountdue1 += amountdue;
                                        amountdueinbase1 += amountdueinbase;
                                    } else if (dueDate.after(cal2Date) || dueDate.equals(cal2Date)) {
                                        amountdue2 += amountdue;
                                        amountdueinbase2 += amountdueinbase;
                                    } else if (dueDate.after(cal3Date) || dueDate.equals(cal3Date)) {
                                        amountdue3 += amountdue;
                                        amountdueinbase3 += amountdueinbase;
                                    } else {
                                        amountdue4 += amountdue;
                                        amountdueinbase4 += amountdueinbase;
                                    }
                                }
                            }
                            putObj.put("personid", personID);
                            putObj.put("personname", personName);
                            putObj.put("aliasname", aliasname);
                            putObj.put("currencysymbol", currencySymbol);
                            putObj.put("currencyid", currencyid);
                            putObj.put("currencyname", currencyName);
                            putObj.put("amountdue1", amountdue1);
                            putObj.put("amountdue2", amountdue2);
                            putObj.put("amountdue3", amountdue3);
                            putObj.put("amountdue4", amountdue4);
                            putObj.put("amountdue5", amountdue5);
                            putObj.put("amountdue6", amountdue6);
                            putObj.put("amountdue7", amountdue7);
                            putObj.put("amountdue8", amountdue8);
                            putObj.put("amountdue9", amountdue9);
                            putObj.put("amountdue10", amountdue10);
                            putObj.put("amountdue11", amountdue11);
//                            putObj.put(GoodsReceiptCMNConstants.ACCRUEDBALANCE, accruedbalance);
                            putObj.put("amountdueinbase1", amountdueinbase1);
                            putObj.put("amountdueinbase2", amountdueinbase2);
                            putObj.put("amountdueinbase3", amountdueinbase3);
                            putObj.put("amountdueinbase4", amountdueinbase4);
                            putObj.put("amountdueinbase5", amountdueinbase5);
                            putObj.put("amountdueinbase6", amountdueinbase6);
                            putObj.put("amountdueinbase7", amountdueinbase7);
                            putObj.put("amountdueinbase8", amountdueinbase8);
                            putObj.put("amountdueinbase9", amountdueinbase9);
                            putObj.put("amountdueinbase10", amountdueinbase10);
                            putObj.put("amountdueinbase11", amountdueinbase11);
//                            putObj.put("accruedbalanceinbase", accruedbalanceinbase);
                            currencyObj.put(currencyid, putObj);
                        } else {
                            personID = (invobj.has("personid")) ? invobj.getString("personid") : "";
                            personName = (invobj.has("personname")) ? invobj.getString("personname") : "";
                            aliasname = (invobj.has("aliasname")) ? invobj.getString("aliasname") : "";
                            currencySymbol = (invobj.has("currencysymbol")) ? invobj.getString("currencysymbol") : "";
                            currencyid = (invobj.has("currencyid")) ? invobj.getString("currencyid") : "";
                            currencyName = (invobj.has("currencyname")) ? invobj.getString("currencyname") : "";
                            isDN = invobj.has("isDN") ? invobj.getBoolean("isDN") : false;
                            isMP = invobj.has("isMP") ? invobj.getBoolean("isMP") : false;
                            if (currencyObj.has(currencyid)) {
                                JSONObject addObj = currencyObj.getJSONObject(currencyid);
                                if (isdistibutive) {
                                    amountdue1 = addObj.getDouble("amountdue1");
                                    amountdue2 = addObj.getDouble("amountdue2");
                                    amountdue3 = addObj.getDouble("amountdue3");
                                    amountdue4 = addObj.getDouble("amountdue4");
                                    amountdue5 = addObj.getDouble("amountdue5");
                                    amountdue6 = addObj.getDouble("amountdue6");
                                    amountdue7 = addObj.getDouble("amountdue7");
                                    amountdue8 = addObj.getDouble("amountdue8");
                                    amountdue9 = addObj.getDouble("amountdue9");
                                    amountdue10 = addObj.getDouble("amountdue10");
                                    amountdue11 = addObj.getDouble("amountdue11");
//                                    accruedbalance = addObj.getDouble(GoodsReceiptCMNConstants.ACCRUEDBALANCE);
                                    amountdueinbase1 = addObj.getDouble("amountdueinbase1");
                                    amountdueinbase2 = addObj.getDouble("amountdueinbase2");
                                    amountdueinbase3 = addObj.getDouble("amountdueinbase3");
                                    amountdueinbase4 = addObj.getDouble("amountdueinbase4");
                                    amountdueinbase5 = addObj.getDouble("amountdueinbase5");
                                    amountdueinbase6 = addObj.getDouble("amountdueinbase6");
                                    amountdueinbase7 = addObj.getDouble("amountdueinbase7");
                                    amountdueinbase8 = addObj.getDouble("amountdueinbase8");
                                    amountdueinbase9 = addObj.getDouble("amountdueinbase9");
                                    amountdueinbase10 = addObj.getDouble("amountdueinbase10");
                                    amountdueinbase11 = addObj.getDouble("amountdueinbase11");
//                                    accruedbalanceinbase = addObj.getDouble("accruedbalanceinbase");
                                } else {
                                    amountdue1 = addObj.getDouble("amountdue1");
                                    amountdue2 = addObj.getDouble("amountdue2");
                                    amountdue3 = addObj.getDouble("amountdue3");
//                                    accruedbalance = addObj.getDouble(GoodsReceiptCMNConstants.ACCRUEDBALANCE);
                                    amountdueinbase1 = addObj.getDouble("amountdueinbase1");
                                    amountdueinbase2 = addObj.getDouble("amountdueinbase2");
                                    amountdueinbase3 = addObj.getDouble("amountdueinbase3");
//                                    accruedbalanceinbase = addObj.getDouble("accruedbalanceinbase");
                                }
                            }
                            Date dueDate = new Date();
                            if (datefilter == 0 || datefilter == Constants.agedDueDate0to30Filter) {
                                dueDate = authHandler.getDateOnlyFormat(request).parse(invobj.getString("duedate"));
                            } else {
                                dueDate = authHandler.getDateOnlyFormat(request).parse(invobj.getString("date"));
                            }
                            double amountdue = invobj.getDouble("amountdue");
                            double amountdueinbase = invobj.getDouble("amountdueinbase");
                            boolean isopeningBalanceTransaction = invobj.optBoolean(GoodsReceiptCMNConstants.IsopeningBalanceTransaction, false);
                            if (isDN || isMP) {
                                if (!isopeningBalanceTransaction) {
                                    amountdue = -amountdue;
                                    amountdueinbase = -amountdueinbase;
                                }
                            }

                            if (customizedSumryReportFlag) {
                                if ((dueDate.after(customizeSumryTo1Date) || dueDate.equals(customizeSumryTo1Date)) && (dueDate.before(customizeSumryFrom1Date) || dueDate.equals(customizeSumryFrom1Date))) {
                                    amountdue1 += amountdue;
                                    amountdueinbase1 += amountdueinbase;
                                } else if ((dueDate.after(customizeSumryTo2Date) || dueDate.equals(customizeSumryTo1Date)) && (dueDate.before(customizeSumryFrom2Date) || dueDate.equals(customizeSumryFrom2Date))) {
                                    amountdue2 += amountdue;
                                    amountdueinbase2 += amountdueinbase;
                                } else if ((dueDate.after(customizeSumryTo3Date) || dueDate.equals(customizeSumryTo3Date)) && (dueDate.before(customizeSumryFrom3Date) || dueDate.equals(customizeSumryFrom3Date))) {
                                    amountdue3 += amountdue;
                                    amountdueinbase3 += amountdueinbase;
                                } else if ((dueDate.after(customizeSumryTo4Date) || dueDate.equals(customizeSumryTo4Date)) && (dueDate.before(customizeSumryFrom4Date) || dueDate.equals(customizeSumryFrom4Date))) {
                                    amountdue4 += amountdue;
                                    amountdueinbase4 += amountdueinbase;
                                } else if ((dueDate.after(customizeSumryTo5Date) || dueDate.equals(customizeSumryTo5Date)) && (dueDate.before(customizeSumryFrom5Date) || dueDate.equals(customizeSumryFrom5Date))) {
                                    amountdue5 += amountdue;
                                    amountdueinbase5 += amountdueinbase;
                                } else if ((dueDate.after(customizeSumryTo6Date) || dueDate.equals(customizeSumryTo6Date)) && (dueDate.before(customizeSumryFrom6Date) || dueDate.equals(customizeSumryFrom6Date))) {
                                    amountdue6 += amountdue;
                                    amountdueinbase6 += amountdueinbase;
                                } else if ((dueDate.after(customizeSumryTo7Date) || dueDate.equals(customizeSumryTo7Date)) && (dueDate.before(customizeSumryFrom7Date) || dueDate.equals(customizeSumryFrom7Date))) {
                                    amountdue7 += amountdue;
                                    amountdueinbase7 += amountdueinbase;
                                }

                            } else {
                                if (isdistibutive) {
//                                    if (startDate != null && dueDate.before(startDate)) {//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
//                                        accruedbalance += amountdue;
//                                        accruedbalanceinbase += amountdueinbase;
//                                    } else 
                                    if (dueDate.after(oneDayBeforeCal1Date)) {
                                        if (dueDate.equals(cal1Date) && (datefilter == Constants.agedDueDate0to30Filter || datefilter == Constants.agedInvoiceDate0to30Filter)) {
                                            amountdueinbase2 += amountdueinbase;
                                            amountdue2 += amountdue;
                                        } else {
                                            amountdueinbase1 += amountdueinbase;
                                            amountdue1 += amountdue;
                                        }
                                    } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) {
                                        amountdue2 += amountdue;
                                        amountdueinbase2 += amountdueinbase;
                                    } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) {
                                        amountdue3 += amountdue;
                                        amountdueinbase3 += amountdueinbase;
                                    } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) {
                                        amountdue4 += amountdue;
                                        amountdueinbase4 += amountdueinbase;
                                    } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) {
                                        amountdue5 += amountdue;
                                        amountdueinbase5 += amountdueinbase;
                                    } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) {
                                        amountdue6 += amountdue;
                                        amountdueinbase6 += amountdueinbase;
                                    } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) {
                                        amountdue7 += amountdue;
                                        amountdueinbase7 += amountdueinbase;
                                    } else if ((cal8Date.before(dueDate) || cal8Date.equals(dueDate)) && cal7Date.after(dueDate)) {
                                        amountdue8 += amountdue;
                                        amountdueinbase8 += amountdueinbase;
                                    } else if ((cal9Date.before(dueDate) || cal9Date.equals(dueDate)) && cal8Date.after(dueDate)) {
                                        amountdue9 += amountdue;
                                        amountdueinbase9 += amountdueinbase;
                                    } else if ((cal10Date.before(dueDate) || cal10Date.equals(dueDate)) && cal9Date.after(dueDate)) {
                                        amountdue10 += amountdue;
                                        amountdueinbase10 += amountdueinbase;
                                    } else {
                                        amountdue11 += amountdue;
                                        amountdueinbase11 += amountdueinbase;
                                    }
                                    
                                    
                                    switch(noOfInterval){
                                        case 2:
                                            amountdue3 += amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                            amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                            amountdueinbase3 += amountdueinbase4 + amountdueinbase5 + amountdueinbase6 + amountdueinbase7 + amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                            amountdueinbase4 = amountdueinbase5 = amountdueinbase6 = amountdueinbase7 = amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                            break;
                                        case 3:
                                            amountdue4 += amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                            amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                            amountdueinbase4 += amountdueinbase5 + amountdueinbase6 + amountdueinbase7 + amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                            amountdueinbase5 = amountdueinbase6 = amountdueinbase7 = amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                            break;
                                        case 4:
                                            amountdue5 += amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                            amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                            amountdueinbase5 += amountdueinbase6 + amountdueinbase7 + amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                            amountdueinbase6 = amountdueinbase7 = amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                            break;
                                        case 5:
                                            amountdue6 += amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                            amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                            amountdueinbase6 += amountdueinbase7 + amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                            amountdueinbase7 = amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                            break;
                                        case 6:
                                            amountdue7 += amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                            amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                            amountdueinbase7 += amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                            amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                            break;
                                        case 7:
                                            amountdue8 += amountdue9 + amountdue10 + amountdue11;
                                            amountdue9 = amountdue10 = amountdue11 = 0;
                                            amountdueinbase8 += amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                            amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                            break;
                                        case 8:
                                            amountdue9 += amountdue10 + amountdue11;
                                            amountdue10 = amountdue11 = 0;
                                            amountdueinbase9 += amountdueinbase10 + amountdueinbase11;
                                            amountdueinbase10 = amountdueinbase11 = 0;
                                            break;
                                        case 9:
                                            amountdue10 += amountdue11;
                                            amountdue11 = 0;
                                            amountdueinbase10 += amountdueinbase11;
                                            amountdueinbase11 = 0;
                                            break;
                                    }
                                } else {
//                                    if (startDate != null && dueDate.before(startDate)) {//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
//                                        accruedbalance += amountdue;
//                                        accruedbalanceinbase += amountdueinbase;
//                                    } else 
                                    if (dueDate.after(oneDayBeforeCal1Date) && (dueDate.before(cal1Date) || dueDate.equals(cal1Date))) {
                                        amountdue1 += amountdue;
                                        amountdueinbase1 += amountdueinbase;
                                    } else if (dueDate.after(cal2Date) || dueDate.equals(cal2Date)) {
                                        amountdue2 += amountdue;
                                        amountdueinbase2 += amountdueinbase;
                                    } else if (dueDate.after(cal3Date) || dueDate.equals(cal3Date)) {
                                        amountdue3 += amountdue;
                                        amountdueinbase3 += amountdueinbase;
                                    } else {
                                        amountdue4 += amountdue;
                                        amountdueinbase4 += amountdueinbase;
                                    }
                                }
                            }
                            Vendor parentVendor = (Vendor) row[5];
                            if (parentVendor != null) {
                                putObj.put("parentid", parentVendor.getID());
                                putObj.put("parentname", parentVendor.getName());
                            } else if (vendor.getParent() != null) {
                                putObj.put("parentid", vendor.getParent().getID());
                                putObj.put("parentname", vendor.getParent().getName());
                            }
                            putObj.put("level", row[2]);
                            putObj.put("leaf", row[3]);
                            putObj.put("personid", personID);
                            putObj.put("personname", personName);
                            putObj.put("aliasname", aliasname);
                            putObj.put("currencysymbol", currencySymbol);
                            putObj.put("currencyid", currencyid);
                            putObj.put("currencyname", currencyName);
                            putObj.put("amountdue1", amountdue1);
                            putObj.put("amountdue2", amountdue2);
                            putObj.put("amountdue3", amountdue3);
                            putObj.put("amountdue4", amountdue4);
                            putObj.put("amountdue5", amountdue5);
                            putObj.put("amountdue6", amountdue6);
                            putObj.put("amountdue7", amountdue7);
                            putObj.put("amountdue8", amountdue8);
                            putObj.put("amountdue9", amountdue9);
                            putObj.put("amountdue10", amountdue10);
                            putObj.put("amountdue11", amountdue11);
//                            putObj.put(GoodsReceiptCMNConstants.ACCRUEDBALANCE, accruedbalance);
                            putObj.put("amountdueinbase1", amountdueinbase1);
                            putObj.put("amountdueinbase2", amountdueinbase2);
                            putObj.put("amountdueinbase3", amountdueinbase3);
                            putObj.put("amountdueinbase4", amountdueinbase4);
                            putObj.put("amountdueinbase5", amountdueinbase5);
                            putObj.put("amountdueinbase6", amountdueinbase6);
                            putObj.put("amountdueinbase7", amountdueinbase7);
                            putObj.put("amountdueinbase8", amountdueinbase8);
                            putObj.put("amountdueinbase9", amountdueinbase9);
                            putObj.put("amountdueinbase10", amountdueinbase10);
                            putObj.put("amountdueinbase11", amountdueinbase11);
//                            putObj.put("accruedbalanceinbase", accruedbalanceinbase);
                            currencyObj.put(currencyid, putObj);
                        }
                    }
                    Iterator itr = currencyObj.keys();
                    while (itr.hasNext()) {
                        JSONObject getObj = currencyObj.getJSONObject(itr.next().toString());
                        jObj = new JSONObject();
                        if (getObj.has("personid")) {
                            jObj.put("personid", getObj.getString("personid"));
                        }
                        if (getObj.has("personname")) {
                            jObj.put("personname" + linedetails, getObj.getString("personname"));
                        }
                        if (getObj.has("aliasname")) {
                            jObj.put("aliasname" + linedetails, getObj.getString("aliasname"));
                        }
                        if (getObj.has("level")) {
                            jObj.put("level" + linedetails, getObj.getString("level"));
                        }
                        if (getObj.has("leaf")) {
                            jObj.put("leaf" + linedetails, getObj.getString("leaf"));
                        }

                        jObj.put("amountdue1" + linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue1"), companyid)));
                        jObj.put("amountdue2" + linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue2"), companyid)));
                        jObj.put("amountdue3" + linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue3"), companyid)));
                        jObj.put("amountdue4" + linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue4"), companyid)));
                        jObj.put("amountdue5" + linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue5"), companyid)));
                        jObj.put("amountdue6" + linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue6"), companyid)));
                        jObj.put("amountdue7" + linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue7"), companyid)));
                        jObj.put("amountdue8" + linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue8"), companyid)));
                        jObj.put("amountdue9" + linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue9"), companyid)));
                        jObj.put("amountdue10" + linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue10"), companyid)));
                        jObj.put("amountdue11" + linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue11"), companyid)));
//                        jObj.put(GoodsReceiptCMNConstants.ACCRUEDBALANCE + linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble(GoodsReceiptCMNConstants.ACCRUEDBALANCE), companyid)));
                        jObj.put("currencysymbol" + linedetails, getObj.getString("currencysymbol"));
                        jObj.put("currencyid" + linedetails, getObj.getString("currencyid"));
                        KwlReturnObject objItr = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), getObj.getString("currencyid"));
                        KWLCurrency currency = (KWLCurrency) objItr.getEntityList().get(0);
                        jObj.put("currencyCode" + linedetails, currency.getCurrencyCode());
                        jObj.put("currencyname" + linedetails, getObj.getString("currencyname"));
                        double amountdue = Double.valueOf(authHandler.formattedAmount((getObj.getDouble("amountdue1") + getObj.getDouble("amountdue2") + getObj.getDouble("amountdue3") + getObj.getDouble("amountdue4") + getObj.getDouble("amountdue5") + getObj.getDouble("amountdue6") + getObj.getDouble("amountdue7") + getObj.getDouble("amountdue8") + getObj.getDouble("amountdue9") + getObj.getDouble("amountdue10") + getObj.getDouble("amountdue11")), companyid));
                        /**
                         * Hidding Total column from AP and AR when we expand record.
                         */
                        if (isExportReport) {
                            jObj.put("total" + linedetails, amountdue);
                        }
                        double amountdueinbase = Double.valueOf(authHandler.formattedAmount((getObj.getDouble("amountdueinbase1") + getObj.getDouble("amountdueinbase2") + getObj.getDouble("amountdueinbase3") + getObj.getDouble("amountdueinbase4") + getObj.getDouble("amountdueinbase5") + getObj.getDouble("amountdueinbase6") + getObj.getDouble("amountdueinbase7") + getObj.getDouble("amountdueinbase8") + getObj.getDouble("amountdueinbase9") + getObj.getDouble("amountdueinbase10") + getObj.getDouble("amountdueinbase11")), companyid));
                        jObj.put("totalinbase" + linedetails, amountdueinbase);
                        if (!isDetailedXls) {
                            totalinbase += amountdueinbase;
                        }
                        if (customizedSumryReportFlag && !showCustVendorWithZeroAmounts && amountdue == 0) {
                            continue;
                        }
                        jArr.put(jObj);
                    }
                }
            }
            if (exportAgedPayables || isDetailedXls) { //  Used for export CSV- Add totalinbase
                JSONObject jtotal = new JSONObject();
                if (customizedSumryReportFlag) {
                    jtotal.put("personname", "Total");
                    jtotal.put("total", totalinbase);
                } else {
                    jtotal.put("total", "Total");
                    jtotal.put("totalinbase", totalinbase);
                }
                jArr.put(jtotal);
            }
            System.out.println("Invoice Count: " + invoiceCount);
            System.out.println("JSON Count: " + jsonCount);
            System.out.println("opening Invoice Count: " + openingCount);
            System.out.println("opening JSON Count: " + openingJSONCount);
        } catch (JSONException | ParseException | ServiceException | UnsupportedEncodingException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getVendorAgedPayable : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    @Override
    public JSONArray getVendorAgedPayableMerged(JSONObject request, HashMap requestParams) throws SessionExpiredException, ServiceException {
        JSONObject jObj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            String companyid = request.optString("companyid");
            String gCurrencyId = request.optString(Constants.globalCurrencyKey);
            requestParams.put("companyid", companyid);
//            String curDateString = (!StringUtil.isNullOrEmpty(request.optString(GoodsReceiptCMNConstants.CURDATE))) ? request.optString(GoodsReceiptCMNConstants.CURDATE) : (request.optString("stdate") != null ? request.optString("stdate") : request.optString("startdate"));
            String curDateString = !(StringUtil.isNullOrEmpty(request.optString(Constants.asOfDate))) ? request.optString(Constants.asOfDate) : request.optString(Constants.curdate);
            String templatesubtype = StringUtil.isNullOrEmpty(request.optString("templatesubtype")) ? Constants.TEMPLATE_SUBTYPE_SOA : request.optString("templatesubtype");
            DateFormat df = authHandler.getDateOnlyFormat( );
            Date curDate = df.parse(curDateString);
            int invoiceCount=0,jsonCount=0,openingCount=0,openingJSONCount=0;
            int duration = request.optString(GoodsReceiptCMNConstants.DURATION) == null ? 0 : Integer.parseInt(request.optString(GoodsReceiptCMNConstants.DURATION));
            int noOfInterval = request.has("noOfInterval") && !StringUtil.isNullOrEmpty(request.getString("noOfInterval"))? request.getInt("noOfInterval") : 7;
            boolean isdistibutive = StringUtil.getBoolean(request.optString(GoodsReceiptCMNConstants.ISDISTRIBUTIVE));
            boolean showCustVendorWithZeroAmounts = StringUtil.isNullOrEmpty(request.optString("showCustVendorWithZeroAmounts"))?false:StringUtil.getBoolean(request.optString("showCustVendorWithZeroAmounts"));
//            boolean withinvent    ory = StringUtil.getBoolean(request.getParameter(GoodsReceiptCMNConstants.WITHINVENTORY));
            int datefilter = StringUtil.getInteger(request.optString("datefilter"));// 0 = Invoice Due date OR 1 = Invoice date
            boolean isCurrencyDetails = request.optString("isCurrencyDetails") != null ? Boolean.parseBoolean(request.optString("isCurrencyDetails")) : false;
            if (request.optString("isCurrencyDetails") != null) {
                isCurrencyDetails =  request.optBoolean("isCurrencyDetails");
            }
            Date startDate = null;
            if (requestParams.containsKey(Constants.REQ_startdate) && requestParams.get(Constants.REQ_startdate) != null) {
                startDate = (requestParams.get(Constants.REQ_startdate).toString()).equals("") ? df.parse(Constants.opening_Date) : df.parse(requestParams.get(Constants.REQ_startdate).toString());
            }
            double amountdue1 = 0;
            double amountdue2 = 0;
            double amountdue3 = 0;
            double amountdue4 = 0;
            double amountdue5 = 0;
            double amountdue6 = 0;
            double amountdue7 = 0;
            double amountdue8 = 0;
            double amountdue9 = 0;
            double amountdue10 = 0;
            double amountdue11 = 0;
//            double accruedbalance = 0;

            double amountdue1Base = 0;
            double amountdue2Base = 0;
            double amountdue3Base = 0;
            double amountdue4Base = 0;
            double amountdue5Base = 0;
            double amountdue6Base = 0;
            double amountdue7Base = 0;
            double amountdue8Base = 0;
            double amountdue9Base = 0;
            double amountdue10Base = 0;
            double amountdue11Base = 0;
//            double accruedbalanceBase = 0;

            double amountdueinbase1 = 0;
            double amountdueinbase2 = 0;
            double amountdueinbase3 = 0;
            double amountdueinbase4 = 0;
            double amountdueinbase5 = 0;
            double amountdueinbase6 = 0;
            double amountdueinbase7 = 0;
            double amountdueinbase8 = 0;
            double amountdueinbase9 = 0;
            double amountdueinbase10 = 0;
            double amountdueinbase11 = 0;
//            double accruedbalanceinbase = 0;

            double totalinbase=0;
            Calendar oneDayBeforeCal1 = Calendar.getInstance();
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            Calendar cal8 = Calendar.getInstance();
            Calendar cal9 = Calendar.getInstance();
            Calendar cal10 = Calendar.getInstance();
            oneDayBeforeCal1.setTime(curDate);
            cal1.setTime(curDate);
            cal2.setTime(curDate);
            cal3.setTime(curDate);
            cal4.setTime(curDate);
            cal5.setTime(curDate);
            cal6.setTime(curDate);
            cal7.setTime(curDate);
            cal8.setTime(curDate);
            cal9.setTime(curDate);
            cal10.setTime(curDate);
            oneDayBeforeCal1.add(Calendar.DAY_OF_YEAR, -1);
            cal2.add(Calendar.DAY_OF_YEAR, -duration);
            cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
            cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
            cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
            cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
            cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
            cal8.add(Calendar.DAY_OF_YEAR, -(duration * 7));
            cal9.add(Calendar.DAY_OF_YEAR, -(duration * 8));
            cal10.add(Calendar.DAY_OF_YEAR, -(duration * 9));

            Date oneDayBeforeCal1Date = null;
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;
            Date cal8Date = null;
            Date cal9Date = null;
            Date cal10Date = null;

            String oneDayBeforeCal1String = authHandler.getDateOnlyFormat().format(oneDayBeforeCal1.getTime());
            oneDayBeforeCal1Date = authHandler.getDateOnlyFormat().parse(oneDayBeforeCal1String);

            String cal1String = authHandler.getDateOnlyFormat().format(cal1.getTime());
            cal1Date = authHandler.getDateOnlyFormat().parse(cal1String);

            String cal2String = authHandler.getDateOnlyFormat().format(cal2.getTime());
            cal2Date = authHandler.getDateOnlyFormat().parse(cal2String);

            String cal3String = authHandler.getDateOnlyFormat().format(cal3.getTime());
            cal3Date = authHandler.getDateOnlyFormat().parse(cal3String);

            String cal4String = authHandler.getDateOnlyFormat().format(cal4.getTime());
            cal4Date = authHandler.getDateOnlyFormat().parse(cal4String);

            String cal5String = authHandler.getDateOnlyFormat().format(cal5.getTime());
            cal5Date = authHandler.getDateOnlyFormat().parse(cal5String);

            String cal6String = authHandler.getDateOnlyFormat().format(cal6.getTime());
            cal6Date = authHandler.getDateOnlyFormat().parse(cal6String);

            String cal7String = authHandler.getDateOnlyFormat().format(cal7.getTime());
            cal7Date = authHandler.getDateOnlyFormat().parse(cal7String);

            String cal8String = authHandler.getDateOnlyFormat().format(cal8.getTime());
            cal8Date = authHandler.getDateOnlyFormat().parse(cal8String);

            String cal9String = authHandler.getDateOnlyFormat().format(cal9.getTime());
            cal9Date = authHandler.getDateOnlyFormat().parse(cal9String);

            String cal10String = authHandler.getDateOnlyFormat().format(cal10.getTime());
            cal10Date = authHandler.getDateOnlyFormat().parse(cal10String);

//            Calendar customizeSumryFrom = Calendar.getInstance();
//            Calendar customizeSumryTo = Calendar.getInstance();
            Calendar customizeSumryFrom1 = Calendar.getInstance();
            Calendar customizeSumryTo1 = Calendar.getInstance();
            Calendar customizeSumryFrom2 = Calendar.getInstance();
            Calendar customizeSumryTo2 = Calendar.getInstance();
            Calendar customizeSumryFrom3 = Calendar.getInstance();
            Calendar customizeSumryTo3 = Calendar.getInstance();
            Calendar customizeSumryFrom4 = Calendar.getInstance();
            Calendar customizeSumryTo4 = Calendar.getInstance();
            Calendar customizeSumryFrom5 = Calendar.getInstance();
            Calendar customizeSumryTo5 = Calendar.getInstance();
            Calendar customizeSumryFrom6 = Calendar.getInstance();
            Calendar customizeSumryTo6 = Calendar.getInstance();
            Calendar customizeSumryFrom7 = Calendar.getInstance();
            Calendar customizeSumryTo7 = Calendar.getInstance();
            boolean customizedSumryReportFlag = false;
            Map<String, String> amountDueMap = new HashMap<String, String>();
            boolean exportAgedPayables=false;
            boolean isDetailedXls = false;                   // SDP-3687 Aged Payable detailed Xls Currency wise
            if (requestParams.containsKey("exportAgedReceivables") && requestParams.get("exportAgedReceivables") != null) {
                exportAgedPayables=Boolean.parseBoolean(requestParams.get("exportAgedReceivables").toString());
            }
            boolean isAgedPayables=false;
            if (requestParams.containsKey("isAgedPayables") && requestParams.get("isAgedPayables") != null) {
                isAgedPayables=Boolean.parseBoolean(requestParams.get("isAgedPayables").toString());
            }
            if (requestParams.containsKey("detailedXls") && requestParams.get("detailedXls") != null) {
                isDetailedXls=Boolean.parseBoolean(requestParams.get("detailedXls").toString());
            }
            boolean isVendorCurrExport=false;
            if (requestParams.containsKey("isVendorCurrExport") && requestParams.get("isVendorCurrExport") != null) {
                isVendorCurrExport=Boolean.parseBoolean(requestParams.get("isVendorCurrExport").toString());
            }
            
            String linedetails = isDetailedXls?"_line":"";
            String customizedSumryReportFlagStr = request.optString("customizedSummaryReportFlag");
            if (!StringUtil.isNullOrEmpty(customizedSumryReportFlagStr)) {
                customizedSumryReportFlag = Boolean.parseBoolean(customizedSumryReportFlagStr);
                String fromDuration = null;
                String toDuration = null;
                List<String> fromDurationArr = new ArrayList<String>();// request.getParameter("fromDuration").split(",");
                List<String> toDurationArr = new ArrayList<String>();;// request.getParameter("toDuration").split(",");
//                String fromDurationArr[]= request.getParameter("fromDuration").split(",");
//                String toDurationArr[] = request.getParameter("toDuration").split(",");
                JSONArray fromDurationjson = new JSONArray(request.optString("fromDuration"));
                JSONArray toDurationjson = new JSONArray(request.optString("toDuration"));
                //String fromDurationArr1[]= fromDurationjson.toString().split(",");
                for (int i = 0; i < fromDurationjson.length(); i++) {
                    JSONObject object1 = new JSONObject(fromDurationjson.getString(i));
                    fromDurationArr.add(object1.getString("id"));
                    amountDueMap.put(object1.getString("amountdueindex"), object1.getString("amountdue"));
                    JSONObject object2 = new JSONObject(toDurationjson.getString(i));
                    toDurationArr.add(object2.getString("id"));


                }
                for (int i = 0; i < fromDurationArr.size(); i++) {
                    fromDuration = fromDurationArr.get(i);
                    toDuration = toDurationArr.get(i);
                    switch (i + 1) {
                        case 1:
                            if (!StringUtil.isNullOrEmpty(fromDuration) && !StringUtil.isNullOrEmpty(toDuration)) {
                                int fromDur = Integer.parseInt(fromDuration);
                                int toDur = Integer.parseInt(toDuration);
                                customizeSumryFrom1.setTime(curDate);
                                customizeSumryTo1.setTime(curDate);
                                customizeSumryFrom1.add(Calendar.DAY_OF_YEAR, -fromDur);
                                customizeSumryTo1.add(Calendar.DAY_OF_YEAR, -toDur);
                            }
                            break;
                        case 2:
                            if (!StringUtil.isNullOrEmpty(fromDuration) && !StringUtil.isNullOrEmpty(toDuration)) {
                                int fromDur = Integer.parseInt(fromDuration);
                                int toDur = Integer.parseInt(toDuration);
                                customizeSumryFrom2.setTime(curDate);
                                customizeSumryTo2.setTime(curDate);
                                customizeSumryFrom2.add(Calendar.DAY_OF_YEAR, -fromDur);
                                customizeSumryTo2.add(Calendar.DAY_OF_YEAR, -toDur);
                            }
                            break;
                        case 3:
                            if (!StringUtil.isNullOrEmpty(fromDuration) && !StringUtil.isNullOrEmpty(toDuration)) {
                                int fromDur = Integer.parseInt(fromDuration);
                                int toDur = Integer.parseInt(toDuration);
                                customizeSumryFrom3.setTime(curDate);
                                customizeSumryTo3.setTime(curDate);
                                customizeSumryFrom3.add(Calendar.DAY_OF_YEAR, -fromDur);
                                customizeSumryTo3.add(Calendar.DAY_OF_YEAR, -toDur);
                            }
                            break;
                        case 4:
                            if (!StringUtil.isNullOrEmpty(fromDuration) && !StringUtil.isNullOrEmpty(toDuration)) {
                                int fromDur = Integer.parseInt(fromDuration);
                                int toDur = Integer.parseInt(toDuration);
                                customizeSumryFrom4.setTime(curDate);
                                customizeSumryTo4.setTime(curDate);
                                customizeSumryFrom4.add(Calendar.DAY_OF_YEAR, -fromDur);
                                customizeSumryTo4.add(Calendar.DAY_OF_YEAR, -toDur);
                            }
                            break;
                        case 5:
                            if (!StringUtil.isNullOrEmpty(fromDuration) && !StringUtil.isNullOrEmpty(toDuration)) {
                                int fromDur = Integer.parseInt(fromDuration);
                                int toDur = Integer.parseInt(toDuration);
                                customizeSumryFrom5.setTime(curDate);
                                customizeSumryTo5.setTime(curDate);
                                customizeSumryFrom5.add(Calendar.DAY_OF_YEAR, -fromDur);
                                customizeSumryTo5.add(Calendar.DAY_OF_YEAR, -toDur);
                            }
                            break;
                        case 6:
                            if (!StringUtil.isNullOrEmpty(fromDuration) && !StringUtil.isNullOrEmpty(toDuration)) {
                                int fromDur = Integer.parseInt(fromDuration);
                                int toDur = Integer.parseInt(toDuration);
                                customizeSumryFrom6.setTime(curDate);
                                customizeSumryTo6.setTime(curDate);
                                customizeSumryFrom6.add(Calendar.DAY_OF_YEAR, -fromDur);
                                customizeSumryTo6.add(Calendar.DAY_OF_YEAR, -toDur);
                            }
                            break;
                        case 7:
                            if (!StringUtil.isNullOrEmpty(fromDuration) && !StringUtil.isNullOrEmpty(toDuration)) {
                                int fromDur = Integer.parseInt(fromDuration);
                                int toDur = Integer.parseInt(toDuration);
                                customizeSumryFrom7.setTime(curDate);
                                customizeSumryTo7.setTime(curDate);
                                customizeSumryFrom7.add(Calendar.DAY_OF_YEAR, -fromDur);
                                customizeSumryTo7.add(Calendar.DAY_OF_YEAR, -toDur);
                            }
                            break;


                    }
                }

            }

            Date customizeSumryFrom1Date = null;
            Date customizeSumryTo1Date = null;
            Date customizeSumryFrom2Date = null;
            Date customizeSumryTo2Date = null;
            Date customizeSumryFrom3Date = null;
            Date customizeSumryTo3Date = null;
            Date customizeSumryFrom4Date = null;
            Date customizeSumryTo4Date = null;
            Date customizeSumryFrom5Date = null;
            Date customizeSumryTo5Date = null;
            Date customizeSumryFrom6Date = null;
            Date customizeSumryTo6Date = null;
            Date customizeSumryFrom7Date = null;
            Date customizeSumryTo7Date = null;

            String customizeSumryFrom1String = authHandler.getDateOnlyFormat().format(customizeSumryFrom1.getTime());
            customizeSumryFrom1Date = authHandler.getDateOnlyFormat().parse(customizeSumryFrom1String);
            String customizeSumryTo1String = authHandler.getDateOnlyFormat().format(customizeSumryTo1.getTime());
            customizeSumryTo1Date = authHandler.getDateOnlyFormat().parse(customizeSumryTo1String);

            String customizeSumryFrom2String = authHandler.getDateOnlyFormat().format(customizeSumryFrom2.getTime());
            customizeSumryFrom2Date = authHandler.getDateOnlyFormat().parse(customizeSumryFrom2String);
            String customizeSumryTo2String = authHandler.getDateOnlyFormat().format(customizeSumryTo2.getTime());
            customizeSumryTo2Date = authHandler.getDateOnlyFormat().parse(customizeSumryTo2String);

            String customizeSumryFrom3String = authHandler.getDateOnlyFormat().format(customizeSumryFrom3.getTime());
            customizeSumryFrom3Date = authHandler.getDateOnlyFormat().parse(customizeSumryFrom3String);
            String customizeSumryTo3String = authHandler.getDateOnlyFormat().format(customizeSumryTo3.getTime());
            customizeSumryTo3Date = authHandler.getDateOnlyFormat().parse(customizeSumryTo3String);

            String customizeSumryFrom4String = authHandler.getDateOnlyFormat().format(customizeSumryFrom4.getTime());
            customizeSumryFrom4Date = authHandler.getDateOnlyFormat().parse(customizeSumryFrom4String);
            String customizeSumryTo4String = authHandler.getDateOnlyFormat().format(customizeSumryTo4.getTime());
            customizeSumryTo4Date = authHandler.getDateOnlyFormat().parse(customizeSumryTo4String);

            String customizeSumryFrom5String = authHandler.getDateOnlyFormat().format(customizeSumryFrom5.getTime());
            customizeSumryFrom5Date = authHandler.getDateOnlyFormat().parse(customizeSumryFrom5String);
            String customizeSumryTo5String = authHandler.getDateOnlyFormat().format(customizeSumryTo5.getTime());
            customizeSumryTo5Date = authHandler.getDateOnlyFormat().parse(customizeSumryTo5String);

            String customizeSumryFrom6String = authHandler.getDateOnlyFormat().format(customizeSumryFrom6.getTime());
            customizeSumryFrom6Date = authHandler.getDateOnlyFormat().parse(customizeSumryFrom6String);
            String customizeSumryTo6String = authHandler.getDateOnlyFormat().format(customizeSumryTo6.getTime());
            customizeSumryTo6Date = authHandler.getDateOnlyFormat().parse(customizeSumryTo6String);

            String customizeSumryFrom7String = authHandler.getDateOnlyFormat().format(customizeSumryFrom7.getTime());
            customizeSumryFrom7Date = authHandler.getDateOnlyFormat().parse(customizeSumryFrom7String);
            String customizeSumryTo7String = authHandler.getDateOnlyFormat().format(customizeSumryTo7.getTime());
            customizeSumryTo7Date = authHandler.getDateOnlyFormat().parse(customizeSumryTo7String);


            KwlReturnObject result = accVendorDAOobj.getVendorAndCurrencyDetailsForAgedPayable(requestParams);
//            Iterator itrcust = result.getEntityList().iterator();
            List<Object[]> vendorList = result.getEntityList();
            if (requestParams.containsKey("start")) { //Removing paging here because it is applying on each vendor data (CN/DN etc). Due to this complete amount due not showing in AP Summary.  
                requestParams.remove("start");
            }
            if (requestParams.containsKey("limit")) {
                requestParams.remove("limit");
            }
            requestParams.put("oneDayBeforeCal1", oneDayBeforeCal1);
            requestParams.put("cal1", cal1);
            requestParams.put("cal2", cal2);
            requestParams.put("cal3", cal3);
            requestParams.put("cal4", cal4);
            requestParams.put("cal5", cal5);
            requestParams.put("cal6", cal6);
            requestParams.put("cal7", cal7);
            requestParams.put("cal8", cal8);
            requestParams.put("cal9", cal9);
            requestParams.put("cal10", cal10);
            if (isAgedPayables) {
                requestParams.put("isAgedPayables", isAgedPayables);
            }
            for (Object[] obj:vendorList) {
                Object venid = obj[0];
                requestParams.put(GoodsReceiptCMNConstants.VENDORID, venid);
                requestParams.put(GoodsReceiptCMNConstants.ACCID, venid);
                requestParams.put("cntype", null);
                requestParams.put("isAgedSummary", true);
//
                JSONArray invjarr = new JSONArray();
                result = accGoodsReceiptobj.getGoodsReceiptsMerged(requestParams);
                invoiceCount+=result.getEntityList().size();
                if(result!=null && !result.getEntityList().isEmpty()){
                    invjarr = accGoodsReceiptServiceHandler.getGoodsReceiptsJsonForAgedPayables(requestParams, result.getEntityList(), invjarr, accountingHandlerDAOobj, accCurrencyDAOobj, accGoodsReceiptobj, accAccountDAOobj, accGoodsReceiptCommon, accTaxObj);
                    jsonCount+=invjarr.length();
                }
                JSONArray OBJArryInvoice = new JSONArray();
                result = accGoodsReceiptobj.getOpeningBalanceInvoices(requestParams);
                openingCount+=result.getEntityList().size();
                if (result != null && result.getEntityList()!=null && !result.getEntityList().isEmpty()) {
                    OBJArryInvoice = AccGoodsReceiptServiceHandler.getAgedPayablesOpeningBalanceInvoiceJson(requestParams, result.getEntityList(), OBJArryInvoice, accCurrencyDAOobj, accountingHandlerDAOobj, accGoodsReceiptCommon);
                    openingJSONCount+=OBJArryInvoice.length();
                    for (int i = 0; i < OBJArryInvoice.length(); i++) {
                        invjarr.put(OBJArryInvoice.get(i));
                    }
                }

                if (!templatesubtype.equals(Constants.TEMPLATE_SUBTYPE_SOI)) {
                    JSONArray OBJArryDebitNote = new JSONArray();
                    result = accDebitNoteobj.getOpeningBalanceDNs(requestParams);
                    if (result != null &&  result.getEntityList()!=null && !result.getEntityList().isEmpty()) {
                    OBJArryDebitNote = AccGoodsReceiptServiceHandler.getAgedOpeningBalanceDebitNoteJson(requestParams, result.getEntityList(), OBJArryDebitNote, accCurrencyDAOobj, accountingHandlerDAOobj, accReceiptDAOobj,accDebitNoteobj,accAccountDAOobj, fieldDataManagercntrl);
                        for (int i = 0; i < OBJArryDebitNote.length(); i++) {
                            invjarr.put(OBJArryDebitNote.get(i));
                        }
                    }
                }

                if (!templatesubtype.equals(Constants.TEMPLATE_SUBTYPE_SOI)) {
                    JSONArray OBJArryCreditNote = new JSONArray();
                    result = accCreditNoteDAOobj.getOpeningBalanceVendorCNs(requestParams);
                    if (result != null && result.getEntityList()!=null &&  !result.getEntityList().isEmpty()) {
                    OBJArryCreditNote = AccGoodsReceiptServiceHandler.getAgedOpeningBalanceCreditNoteJson(requestParams, result.getEntityList(), OBJArryCreditNote, accCurrencyDAOobj, accountingHandlerDAOobj, accPaymentDAOobj,accVendorPaymentobj,accDebitNoteobj,accAccountDAOobj, fieldDataManagercntrl);
                        for (int i = 0; i < OBJArryCreditNote.length(); i++) {
                            invjarr.put(OBJArryCreditNote.get(i));
                        }
                    }
                }

                if (!templatesubtype.equals(Constants.TEMPLATE_SUBTYPE_SOI)) {
                    JSONArray OBJArryPayment = new JSONArray();
                    result = accVendorPaymentobj.getOpeningBalancePayments(requestParams);
                    if (result != null &&  result.getEntityList()!=null && !result.getEntityList().isEmpty()) {
                        OBJArryPayment = AccGoodsReceiptServiceHandler.getAgedOpeningBalancePaymentJson(requestParams, result.getEntityList(), OBJArryPayment, accCurrencyDAOobj, accountingHandlerDAOobj, accVendorPaymentobj, accAccountDAOobj, fieldDataManagercntrl);
                        for (int i = 0; i < OBJArryPayment.length(); i++) {
                            invjarr.put(OBJArryPayment.get(i));
                        }
                    }
                }

                if (!templatesubtype.equals(Constants.TEMPLATE_SUBTYPE_SOI)) {
                    JSONArray DebitNotejArr = new JSONArray();//This is used for getting DN gainst vendor and otherwise 
                    result = accDebitNoteobj.getDebitNoteMerged(requestParams);
                    if (result != null && result.getEntityList()!=null &&  !result.getEntityList().isEmpty()) {
                    DebitNotejArr = AccGoodsReceiptServiceHandler.getDebitNotesMergedJson(requestParams, result.getEntityList(), DebitNotejArr, accCurrencyDAOobj, accountingHandlerDAOobj, accReceiptDAOobj,accDebitNoteobj,accAccountDAOobj, fieldDataManagercntrl, accJournalEntryobj);
                        for (int i = 0; i < DebitNotejArr.length(); i++) {
                            invjarr.put(DebitNotejArr.get(i));
                        }
                    }
                }

                if (!templatesubtype.equals(Constants.TEMPLATE_SUBTYPE_SOI)) {
                    requestParams.put("cntype", 4);//This is used for getting Credit note against vendor 
                    JSONArray CreditNotejArr = new JSONArray();
                    result = accCreditNoteDAOobj.getCreditNoteMerged(requestParams);
                    if (result != null && result.getEntityList()!=null &&  !result.getEntityList().isEmpty()) {
                    CreditNotejArr = AccGoodsReceiptServiceHandler.getCreditNotesMergedJson(requestParams, result.getEntityList(), CreditNotejArr, accCurrencyDAOobj, accountingHandlerDAOobj, accPaymentDAOobj,accVendorPaymentobj,accDebitNoteobj,accAccountDAOobj, fieldDataManagercntrl, accJournalEntryobj);
                        for (int i = 0; i < CreditNotejArr.length(); i++) {
                            invjarr.put(CreditNotejArr.get(i));
                        }
                    }
                }

                if (!templatesubtype.equals(Constants.TEMPLATE_SUBTYPE_SOI)) {
                    JSONArray makePaymentJArr = new JSONArray();
                    requestParams.put("allAdvPayment", true); // needs only Advance type record so that putted true
                    requestParams.put("paymentWindowType", 1);//Payment to Vendor record
                    result = accVendorPaymentobj.getPayments(requestParams);
                    if (result != null && result.getEntityList()!=null &&  !result.getEntityList().isEmpty()) {
                        makePaymentJArr = AccGoodsReceiptServiceHandler.getPaymentsJson(requestParams, result.getEntityList(), makePaymentJArr, accCurrencyDAOobj, accountingHandlerDAOobj, accVendorPaymentobj);
                        for (int i = 0; i < makePaymentJArr.length(); i++) {
                            invjarr.put(makePaymentJArr.get(i));
                        }
                    }
                }
                
                if (!templatesubtype.equals(Constants.TEMPLATE_SUBTYPE_SOI)) {
                    if (!isAgedPayables && isCurrencyDetails) { //need to put requestParams for this condition
                        requestParams.put("isAgedPayables", true);
                    }
                    requestParams.put("allAdvPayment", true); // needs only refund type record so that putted true
                    requestParams.put("paymentWindowType", 2);//Receipt to Vendor record
                    JSONArray receivePaymentJArr = new JSONArray();
                    result = accReceiptDAOobj.getReceipts(requestParams);
                    receivePaymentJArr = AccInvoiceServiceHandler.getReceiptsJson(requestParams, result.getEntityList(), receivePaymentJArr, accountingHandlerDAOobj, authHandlerDAOObj, accCurrencyDAOobj, accReceiptDAOobj, request, accAccountDAOobj, fieldDataManagercntrl, accJournalEntryobj);
                    for (int i = 0; i < receivePaymentJArr.length(); i++) {
                        invjarr.put(receivePaymentJArr.get(i));
                    }
                    requestParams.remove("allAdvPayment");
                    requestParams.remove("paymentWindowType");
                    if (!isAgedPayables && isCurrencyDetails) { //need to remove requestParams for this condition as we have putted above
                        requestParams.remove("isAgedPayables");
                    }
                }
                if (invjarr.length() < 1) {
                    continue;
                }
                amountdue1 = amountdue2 = amountdue3 = amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                amountdue1Base = amountdue2Base = amountdue3Base = amountdue4Base = amountdue5Base = amountdue6Base = amountdue7Base = amountdue8Base = amountdue9Base = amountdue10Base = amountdue11Base = 0;
                String personID = null;
                String personName = null;
                String aliasname = "";
                String currencySymbol=null;
                String currencyid=null;
                boolean isDN=false;
                boolean isMP=false;    
//                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Vendor.class.getName(), venid.toString());
//                Vendor vendor = (Vendor) objItr.getEntityList().get(0);
//                String currencyidVen = vendor.getAccount().getCurrency().getCurrencyID();
//                String currencySymbolVen = vendor.getAccount().getCurrency().getSymbol();
//                String currencyName = vendor.getAccount().getCurrency().getName();
                    String currencyidVen = obj[1].toString();  // Vendor Account Currency ID
                    String currencyName = obj[2].toString();   // Vendor Account Currency Name
                    String currencySymbolVen = obj[3].toString(); // Vendor Account Currency Symbol
                    String vendorCurrId = obj[4].toString();  // Vendor Currency ID
                    
                    if (!isCurrencyDetails) {
                        for (int i = 0; i < invjarr.length(); i++) {
                            JSONObject invobj = invjarr.getJSONObject(i);
                            personID = invobj.getString(GoodsReceiptCMNConstants.PERSONID);
                            personName = invobj.getString(GoodsReceiptCMNConstants.PERSONNAME);
                        aliasname = invobj.has(GoodsReceiptCMNConstants.ALIASNAME)?(invobj.getString(GoodsReceiptCMNConstants.ALIASNAME)!=null?invobj.getString(GoodsReceiptCMNConstants.ALIASNAME):""):"";
                            currencySymbol = invobj.getString(GoodsReceiptCMNConstants.CURRENCYSYMBOL);
                            currencyid = invobj.getString(GoodsReceiptCMNConstants.CURRENCYID);

                            isDN = invobj.has("isDN") ? invobj.getBoolean("isDN") : false;
                            isMP = invobj.has("isMP") ? invobj.getBoolean("isMP") : false;
                            Date dueDate = null;
                            if (!StringUtil.isNullOrEmpty(invobj.getString(GoodsReceiptCMNConstants.DUEDATE))) {
                                dueDate = authHandler.getDateOnlyFormat( ).parse(invobj.getString(GoodsReceiptCMNConstants.DUEDATE));
                            }

                            if (datefilter == 0 || datefilter == Constants.agedDueDate0to30Filter) {
                                dueDate = authHandler.getDateOnlyFormat( ).parse(invobj.getString(GoodsReceiptCMNConstants.DUEDATE));
                            } else {
                                dueDate = authHandler.getDateOnlyFormat( ).parse(invobj.getString(GoodsReceiptCMNConstants.DATE));
                            }

                            double amountdue = invobj.has(GoodsReceiptCMNConstants.AMOUNTDUE) ? invobj.getDouble(GoodsReceiptCMNConstants.AMOUNTDUE) : 0;
                            double amountdueinbase = invobj.has(GoodsReceiptCMNConstants.AMOUNTDUEINBASE) ? invobj.getDouble(GoodsReceiptCMNConstants.AMOUNTDUEINBASE) : 0;
                            double externalcurrencyrate = invobj.getDouble(GoodsReceiptCMNConstants.EXTERNALCURRENCYRATE);

                            boolean isopeningBalanceTransaction = invobj.optBoolean(GoodsReceiptCMNConstants.IsopeningBalanceTransaction, false);
                            boolean isConversionRateFromCurrencyToBase = invobj.optBoolean(GoodsReceiptCMNConstants.IsConversionRateFromCurrencyToBase, false);

                            if (isVendorCurrExport) {

                                double custCurrToBaseExchRate = 1 / accCurrencyDAOobj.getCurrencyToBaseRate(requestParams, vendorCurrId, dueDate);
                                if (accCurrencyDAOobj.getCurrencyToBaseRate(requestParams, vendorCurrId, dueDate) == 0.0) {
                                    custCurrToBaseExchRate = externalcurrencyrate;
                                }
                                if (gCurrencyId.equals(vendorCurrId)) {
                                    custCurrToBaseExchRate = 1;
                                } else if (currencyid.equals(vendorCurrId)) {
                                    custCurrToBaseExchRate = 1/externalcurrencyrate;
                                }
                                if (isopeningBalanceTransaction && isConversionRateFromCurrencyToBase) {
                                    custCurrToBaseExchRate = 1 / custCurrToBaseExchRate;
                                }
                                
                                KwlReturnObject CustCurrencyTotalAmount = accCurrencyDAOobj.getBaseToCurrencyAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountdueinbase, vendorCurrId, dueDate, custCurrToBaseExchRate);
                                amountdueinbase = (Double) CustCurrencyTotalAmount.getEntityList().get(0);  //Amount In Vendor Currency.
                            } else {

                                KwlReturnObject bAmt = null;
                                if (isopeningBalanceTransaction && isConversionRateFromCurrencyToBase) {
                                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, currencyid, currencyidVen, df.parse(invobj.getString(GoodsReceiptCMNConstants.DATE)), externalcurrencyrate);
                                } else {
                                    bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, currencyid, currencyidVen, df.parse(invobj.getString(GoodsReceiptCMNConstants.DATE)), externalcurrencyrate);
                                }
                                amountdue = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                            }

                            if (isDN || isMP) {
                                if (!isopeningBalanceTransaction) {
                                    amountdue = -amountdue;
                                    amountdueinbase = -amountdueinbase;
                                }
                            }

                            if (customizedSumryReportFlag) {
                                if ((dueDate.after(customizeSumryTo1Date) || dueDate.equals(customizeSumryTo1Date)) && (dueDate.before(customizeSumryFrom1Date) || dueDate.equals(customizeSumryFrom1Date))) {
                                    amountdue1 += amountdue;
                                    amountdue1Base += amountdueinbase;
                                } else if ((dueDate.after(customizeSumryTo2Date) || dueDate.equals(customizeSumryTo2Date)) && (dueDate.before(customizeSumryFrom2Date) || dueDate.equals(customizeSumryFrom2Date))) {
                                    amountdue2 += amountdue;
                                    amountdue2Base += amountdueinbase;
                                } else if ((dueDate.after(customizeSumryTo3Date) || dueDate.equals(customizeSumryTo3Date)) && (dueDate.before(customizeSumryFrom3Date) || dueDate.equals(customizeSumryFrom3Date))) {
                                    amountdue3 += amountdue;
                                    amountdue3Base += amountdueinbase;
                                } else if ((dueDate.after(customizeSumryTo4Date) || dueDate.equals(customizeSumryTo4Date)) && (dueDate.before(customizeSumryFrom4Date) || dueDate.equals(customizeSumryFrom4Date))) {
                                    amountdue4 += amountdue;
                                    amountdue4Base += amountdueinbase;
                                } else if ((dueDate.after(customizeSumryTo5Date) || dueDate.equals(customizeSumryTo5Date)) && (dueDate.before(customizeSumryFrom5Date) || dueDate.equals(customizeSumryFrom5Date))) {
                                    amountdue5 += amountdue;
                                    amountdue5Base += amountdueinbase;
                                } else if ((dueDate.after(customizeSumryTo6Date) || dueDate.equals(customizeSumryTo6Date)) && (dueDate.before(customizeSumryFrom6Date) || dueDate.equals(customizeSumryFrom6Date))) {
                                    amountdue6 += amountdue;
                                    amountdue6Base += amountdueinbase;
                                } else if ((dueDate.after(customizeSumryTo7Date) || dueDate.equals(customizeSumryTo7Date)) && (dueDate.before(customizeSumryFrom7Date) || dueDate.equals(customizeSumryFrom7Date))) {
                                    amountdue7 += amountdue;
                                    amountdue7Base += amountdueinbase;
                                }
                            } else {
                                if (isdistibutive) {

//                                    if (startDate != null && dueDate.before(startDate)) {//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
//                                        accruedbalance += amountdue;
//                                        accruedbalanceBase += amountdueinbase;
//                                    } else
                                    if (dueDate.after(oneDayBeforeCal1Date)) {
                                        if (dueDate.equals(cal1Date) && (datefilter == Constants.agedDueDate0to30Filter || datefilter == Constants.agedInvoiceDate0to30Filter)) {
                                            amountdue2 += amountdue;
                                            amountdue2Base += amountdueinbase;
                                        } else {
                                            amountdue1 += amountdue;
                                            amountdue1Base += amountdueinbase;
                                        }
                                    } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) {
                                        amountdue2 += amountdue;
                                        amountdue2Base += amountdueinbase;
                                    } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) {
                                        amountdue3 += amountdue;
                                        amountdue3Base += amountdueinbase;
                                    } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) {
                                        amountdue4 += amountdue;
                                        amountdue4Base += amountdueinbase;
                                    } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) {
                                        amountdue5 += amountdue;
                                        amountdue5Base += amountdueinbase;
                                    } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) {
                                        amountdue6 += amountdue;
                                        amountdue6Base += amountdueinbase;
                                    } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) {
                                        amountdue7 += amountdue;
                                        amountdue7Base += amountdueinbase;
                                    } else if ((cal8Date.before(dueDate) || cal8Date.equals(dueDate)) && cal7Date.after(dueDate)) {
                                        amountdue8 += amountdue;
                                        amountdue8Base += amountdueinbase;
                                    } else if ((cal9Date.before(dueDate) || cal9Date.equals(dueDate)) && cal8Date.after(dueDate)) {
                                        amountdue9 += amountdue;
                                        amountdue9Base += amountdueinbase;
                                    } else if ((cal10Date.before(dueDate) || cal10Date.equals(dueDate)) && cal9Date.after(dueDate)) {
                                        amountdue10 += amountdue;
                                        amountdue10Base += amountdueinbase;
                                    } else {
                                        amountdue11 += amountdue;
                                        amountdue11Base += amountdueinbase;
                                    }
                                    
                                    switch(noOfInterval){
                                        case 2:
                                            amountdue3 += amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                            amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                            amountdue3Base += amountdue4Base + amountdue5Base + amountdue6Base + amountdue7Base + amountdue8Base + amountdue9Base + amountdue10Base + amountdue11Base;
                                            amountdue4Base = amountdue5Base = amountdue6Base = amountdue7Base = amountdue8Base = amountdue9Base = amountdue10Base = amountdue11Base = 0;
                                            break;
                                        case 3:
                                            amountdue4 += amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                            amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                            amountdue4Base += amountdue5Base + amountdue6Base + amountdue7Base + amountdue8Base + amountdue9Base + amountdue10Base + amountdue11Base;
                                            amountdue5Base = amountdue6Base = amountdue7Base = amountdue8Base = amountdue9Base = amountdue10Base = amountdue11Base = 0;
                                            break;
                                        case 4:
                                            amountdue5 += amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                            amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                            amountdue5Base += amountdue6Base + amountdue7Base + amountdue8Base + amountdue9Base + amountdue10Base + amountdue11Base;
                                            amountdue6Base = amountdue7Base = amountdue8Base = amountdue9Base = amountdue10Base = amountdue11Base = 0;
                                            break;
                                        case 5:
                                            amountdue6 += amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                            amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                            amountdue6Base += amountdue7Base + amountdue8Base + amountdue9Base + amountdue10Base + amountdue11Base;
                                            amountdue7Base = amountdue8Base = amountdue9Base = amountdue10Base = amountdue11Base = 0;
                                            break;
                                        case 6:
                                            amountdue7 += amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                            amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                            amountdue7Base += amountdue8Base + amountdue9Base + amountdue10Base + amountdue11Base;
                                            amountdue8Base = amountdue9Base = amountdue10Base = amountdue11Base = 0;
                                            break;
                                        case 7:
                                            amountdue8Base += amountdue9Base + amountdue10Base + amountdue11Base;
                                            amountdue9Base = amountdue10Base = amountdue11Base = 0;
                                            amountdue8Base += amountdue9Base + amountdue10Base + amountdue11Base;
                                            amountdue9Base = amountdue10Base = amountdue11Base = 0;
                                            break;
                                        case 8:
                                            amountdue9 += amountdue10 + amountdue11;
                                            amountdue10 = amountdue11 = 0;
                                            amountdue9Base += amountdue10Base + amountdue11Base;
                                            amountdue10Base = amountdue11Base = 0;
                                            break;
                                        case 9:
                                            amountdue10 += amountdue11;
                                            amountdue11 = 0;
                                            amountdue10Base += amountdue11Base;
                                            amountdue11Base = 0;
                                            break;
                                    }
                                    
                                    
                                } else {
//                                    if (startDate != null && dueDate.before(startDate)) {//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
//                                        accruedbalance += amountdue;
//                                        accruedbalanceBase += amountdueinbase;
//                                    } else 
                                    if (dueDate.after(oneDayBeforeCal1Date) && (dueDate.before(cal1Date) || dueDate.equals(cal1Date))) {
                                        amountdue1 += amountdue;
                                        amountdue1Base += amountdueinbase;
                                    } else if (dueDate.after(cal2Date) || dueDate.equals(cal2Date)) {
                                        amountdue2 += amountdue;
                                        amountdue2Base += amountdueinbase;
                                    } else if (dueDate.after(cal3Date) || dueDate.equals(cal3Date)) {
                                        amountdue3 += amountdue;
                                        amountdue3Base += amountdueinbase;
                                    } else {
                                        amountdue4 += amountdue;
                                        amountdue4Base += amountdueinbase;
                                    }
                                }
                            }
                        }
                        if (invjarr.length() > 0) {
                            jObj = new JSONObject();
                            jObj.put(GoodsReceiptCMNConstants.PERSONID, personID);
                            jObj.put(GoodsReceiptCMNConstants.PERSONNAME, personName);
                            jObj.put(GoodsReceiptCMNConstants.ALIASNAME, aliasname);
                            jObj.put(GoodsReceiptCMNConstants.CURRENCYID, currencyidVen);
                            jObj.put(GoodsReceiptCMNConstants.CURRENCYNAME, currencyName);
                            if (!customizedSumryReportFlag) {
                                jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE1, Double.valueOf(authHandler.formattedAmount(amountdue1, companyid)));
                                jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE2, Double.valueOf(authHandler.formattedAmount(amountdue2, companyid)));
                                jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE3, Double.valueOf(authHandler.formattedAmount(amountdue3, companyid)));
                                jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE4, Double.valueOf(authHandler.formattedAmount(amountdue4, companyid)));
                                jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE5, Double.valueOf(authHandler.formattedAmount(amountdue5, companyid)));
                                jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE6, Double.valueOf(authHandler.formattedAmount(amountdue6, companyid)));
                                jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE7, Double.valueOf(authHandler.formattedAmount(amountdue7, companyid)));
                                jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE8, Double.valueOf(authHandler.formattedAmount(amountdue8, companyid)));
                                jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE9, Double.valueOf(authHandler.formattedAmount(amountdue9, companyid)));
                                jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE10, Double.valueOf(authHandler.formattedAmount(amountdue10, companyid)));
                                jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE11, Double.valueOf(authHandler.formattedAmount(amountdue11, companyid)));
//                                jObj.put(GoodsReceiptCMNConstants.ACCRUEDBALANCE, Double.valueOf(authHandler.formattedAmount(accruedbalance, companyid)));
                                jObj.put(GoodsReceiptCMNConstants.CURRENCYSYMBOL, currencySymbolVen);
                            } else {
                                if (amountDueMap.containsKey("amountdue1")) {
                                    jObj.put(amountDueMap.get("amountdue1"), Double.valueOf(authHandler.formattedAmount(amountdue1, companyid)));
                                }
                                if (amountDueMap.containsKey("amountdue2")) {
                                    jObj.put(amountDueMap.get("amountdue2"), Double.valueOf(authHandler.formattedAmount(amountdue2, companyid)));
                                }
                                if (amountDueMap.containsKey("amountdue3")) {
                                    jObj.put(amountDueMap.get("amountdue3"), Double.valueOf(authHandler.formattedAmount(amountdue3, companyid)));
                                }
                                if (amountDueMap.containsKey("amountdue4")) {
                                    jObj.put(amountDueMap.get("amountdue4"), Double.valueOf(authHandler.formattedAmount(amountdue4, companyid)));
                                }
                                if (amountDueMap.containsKey("amountdue5")) {
                                    jObj.put(amountDueMap.get("amountdue5"), Double.valueOf(authHandler.formattedAmount(amountdue5, companyid)));
                                }
                                if (amountDueMap.containsKey("amountdue6")) {
                                    jObj.put(amountDueMap.get("amountdue6"), Double.valueOf(authHandler.formattedAmount(amountdue6, companyid)));
                                }
                                if (amountDueMap.containsKey("amountdue7")) {
                                    jObj.put(amountDueMap.get("amountdue7"), Double.valueOf(authHandler.formattedAmount(amountdue7, companyid)));
                                }
                                if (amountDueMap.containsKey("amountdue8")) {
                                    jObj.put(amountDueMap.get("amountdue8"), Double.valueOf(authHandler.formattedAmount(amountdue8, companyid)));
                                }
                            }
                            jObj.put("amountdueinbase1", Double.valueOf(authHandler.formattedAmount(amountdue1Base, companyid)));
                            jObj.put("amountdueinbase2", Double.valueOf(authHandler.formattedAmount(amountdue2Base, companyid)));
                            jObj.put("amountdueinbase3", Double.valueOf(authHandler.formattedAmount(amountdue3Base, companyid)));
                            jObj.put("amountdueinbase4", Double.valueOf(authHandler.formattedAmount(amountdue4Base, companyid)));
                            jObj.put("amountdueinbase5", Double.valueOf(authHandler.formattedAmount(amountdue5Base, companyid)));
                            jObj.put("amountdueinbase6", Double.valueOf(authHandler.formattedAmount(amountdue6Base, companyid)));
                            jObj.put("amountdueinbase7", Double.valueOf(authHandler.formattedAmount(amountdue7Base, companyid)));
                            jObj.put("amountdueinbase8", Double.valueOf(authHandler.formattedAmount(amountdue8Base, companyid)));
                            jObj.put("amountdueinbase9", Double.valueOf(authHandler.formattedAmount(amountdue9Base, companyid)));
                            jObj.put("amountdueinbase10", Double.valueOf(authHandler.formattedAmount(amountdue10Base, companyid)));
                            jObj.put("amountdueinbase11", Double.valueOf(authHandler.formattedAmount(amountdue11Base, companyid)));
//                            jObj.put("accruedbalanceinbase", Double.valueOf(authHandler.formattedAmount(accruedbalanceBase, companyid)));
                            double amountdue = Double.valueOf(authHandler.formattedAmount((amountdue1 + amountdue2 + amountdue3 + amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11), companyid));
                            double amountdueBase = Double.valueOf(authHandler.formattedAmount((amountdue1Base + amountdue2Base + amountdue3Base + amountdue4Base + amountdue5Base + amountdue6Base + amountdue7Base + amountdue8Base + amountdue9Base + amountdue10Base + amountdue11Base), companyid));
                            jObj.put(GoodsReceiptCMNConstants.TOTAL, amountdue);
                            jObj.put("totalinbase", amountdueBase);
                        totalinbase+=amountdueBase;
                        if(customizedSumryReportFlag && !showCustVendorWithZeroAmounts && amountdue==0){
                            continue;
                        }
                            jArr.put(jObj);
                        }
                    } 
                    if (isCurrencyDetails || isDetailedXls)
                    {
                        JSONObject currencyObj = new JSONObject();
                        for (int i = 0; i < invjarr.length(); i++) {
                            amountdue1 = amountdue2 = amountdue3 = amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                            amountdueinbase1 = amountdueinbase2 = amountdueinbase3 = amountdueinbase4 = amountdueinbase5 = amountdueinbase6 = amountdueinbase7 = amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                            JSONObject invobj = invjarr.getJSONObject(i);
                            JSONObject putObj = new JSONObject();
                            if (i == 0) {
                                personID = (invobj.has("personid")) ? invobj.getString("personid") : "";
                                personName = (invobj.has("personname")) ? invobj.getString("personname") : "";
                                aliasname = (invobj.has("aliasname")) ? invobj.getString("aliasname") : "";
                                currencySymbol = (invobj.has("currencysymbol")) ? invobj.getString("currencysymbol") : "";
                                currencyid = (invobj.has("currencyid")) ? invobj.getString("currencyid") : "";
                                currencyName = (invobj.has("currencyname")) ? invobj.getString("currencyname") : "";
                                isDN = invobj.has("isDN") ? invobj.getBoolean("isDN") : false;
                                isMP = invobj.has("isMP") ? invobj.getBoolean("isMP") : false;
                                Date dueDate = new Date();
                                if (datefilter == 0 || datefilter == Constants.agedDueDate0to30Filter) {
                                    dueDate = authHandler.getDateOnlyFormat( ).parse(invobj.getString("duedate"));
                                } else {
                                    dueDate = authHandler.getDateOnlyFormat( ).parse(invobj.getString("date"));
                                }
                                double amountdue = invobj.getDouble("amountdue");
                                double amountdueinbase = invobj.getDouble("amountdueinbase");
                                boolean isopeningBalanceTransaction = invobj.optBoolean(GoodsReceiptCMNConstants.IsopeningBalanceTransaction, false);

                                if (isDN || isMP) {
                                    if (!isopeningBalanceTransaction) {
                                        amountdue = -amountdue;
                                        amountdueinbase = -amountdueinbase;
                                    }
                                }

                                if (customizedSumryReportFlag) {
                                    if ((dueDate.after(customizeSumryTo1Date) || dueDate.equals(customizeSumryTo1Date)) && (dueDate.before(customizeSumryFrom1Date) || dueDate.equals(customizeSumryFrom1Date))) {
                                        amountdue1 += amountdue;
                                        amountdueinbase1 += amountdueinbase;
                                    } else if ((dueDate.after(customizeSumryTo2Date) || dueDate.equals(customizeSumryTo2Date)) && (dueDate.before(customizeSumryFrom2Date) || dueDate.equals(customizeSumryFrom2Date))) {
                                        amountdue2 += amountdue;
                                        amountdueinbase2 += amountdueinbase;
                                    } else if ((dueDate.after(customizeSumryTo3Date) || dueDate.equals(customizeSumryTo3Date)) && (dueDate.before(customizeSumryFrom3Date) || dueDate.equals(customizeSumryFrom3Date))) {
                                        amountdue3 += amountdue;
                                        amountdueinbase3 += amountdueinbase;
                                    } else if ((dueDate.after(customizeSumryTo4Date) || dueDate.equals(customizeSumryTo4Date)) && (dueDate.before(customizeSumryFrom4Date) || dueDate.equals(customizeSumryFrom4Date))) {
                                        amountdue4 += amountdue;
                                        amountdueinbase4 += amountdueinbase;
                                    } else if ((dueDate.after(customizeSumryTo5Date) || dueDate.equals(customizeSumryTo5Date)) && (dueDate.before(customizeSumryFrom5Date) || dueDate.equals(customizeSumryFrom5Date))) {
                                        amountdue5 += amountdue;
                                        amountdueinbase5 += amountdueinbase;
                                    } else if ((dueDate.after(customizeSumryTo6Date) || dueDate.equals(customizeSumryTo6Date)) && (dueDate.before(customizeSumryFrom6Date) || dueDate.equals(customizeSumryFrom6Date))) {
                                        amountdue6 += amountdue;
                                        amountdueinbase6 += amountdueinbase;
                                    } else if ((dueDate.after(customizeSumryTo7Date) || dueDate.equals(customizeSumryTo7Date)) && (dueDate.before(customizeSumryFrom7Date) || dueDate.equals(customizeSumryFrom7Date))) {
                                        amountdue7 += amountdue;
                                        amountdueinbase7 += amountdueinbase;
                                    }

                                } else {
                                    if (isdistibutive) {
//                                        if (startDate != null && dueDate.before(startDate)) {//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
//                                            accruedbalance += amountdue;
//                                            accruedbalanceinbase += amountdueinbase;
//                                        } else
                                        if (dueDate.after(oneDayBeforeCal1Date)) {
                                            if (dueDate.equals(cal1Date) && (datefilter == Constants.agedDueDate0to30Filter || datefilter == Constants.agedInvoiceDate0to30Filter)) {
                                                amountdueinbase2 += amountdueinbase;
                                                amountdue2 += amountdue;
                                            } else {
                                                amountdueinbase1 += amountdueinbase;
                                                amountdue1 += amountdue;
                                            }
                                        } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) {
                                            amountdue2 += amountdue;
                                            amountdueinbase2 += amountdueinbase;
                                        } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) {
                                            amountdue3 += amountdue;
                                            amountdueinbase3 += amountdueinbase;
                                        } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) {
                                            amountdue4 += amountdue;
                                            amountdueinbase4 += amountdueinbase;
                                        } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) {
                                            amountdue5 += amountdue;
                                            amountdueinbase5 += amountdueinbase;
                                        } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) {
                                            amountdue6 += amountdue;
                                            amountdueinbase6 += amountdueinbase;
                                        } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) {
                                            amountdue7 += amountdue;
                                            amountdueinbase7 += amountdueinbase;
                                        } else if ((cal8Date.before(dueDate) || cal8Date.equals(dueDate)) && cal7Date.after(dueDate)) {
                                            amountdue8 += amountdue;
                                            amountdueinbase8 += amountdueinbase;
                                        } else if ((cal9Date.before(dueDate) || cal9Date.equals(dueDate)) && cal8Date.after(dueDate)) {
                                            amountdue9 += amountdue;
                                            amountdueinbase9 += amountdueinbase;
                                        } else if ((cal10Date.before(dueDate) || cal10Date.equals(dueDate)) && cal9Date.after(dueDate)) {
                                            amountdue10 += amountdue;
                                            amountdueinbase10 += amountdueinbase;
                                        } else {
                                            amountdue11 += amountdue;
                                            amountdueinbase11 += amountdueinbase;
                                        }
                                        
                                        switch(noOfInterval){
                                            case 2:
                                                amountdue3 += amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                                amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                                amountdueinbase3 += amountdueinbase4 + amountdueinbase5 + amountdueinbase6 + amountdueinbase7 + amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                                amountdueinbase4 = amountdueinbase5 = amountdueinbase6 = amountdueinbase7 = amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                                break;
                                            case 3:
                                                amountdue4 += amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                                amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                                amountdueinbase4 += amountdueinbase5 + amountdueinbase6 + amountdueinbase7 + amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                                amountdueinbase5 = amountdueinbase6 = amountdueinbase7 = amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                                break;
                                            case 4:
                                                amountdue5 += amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                                amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                                amountdueinbase5 += amountdueinbase6 + amountdueinbase7 + amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                                amountdueinbase6 = amountdueinbase7 = amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                                break;
                                            case 5:
                                                amountdue6 += amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                                amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                                amountdueinbase6 += amountdueinbase7 + amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                                amountdueinbase7 = amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                                break;
                                            case 6:
                                                amountdue7 += amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                                amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                                amountdueinbase7 += amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                                amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                                break;
                                            case 7:
                                                amountdue8 += amountdue9 + amountdue10 + amountdue11;
                                                amountdue9 = amountdue10 = amountdue11 = 0;
                                                amountdueinbase8 += amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                                amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                                break;
                                            case 8:
                                                amountdue9 += amountdue10 + amountdue11;
                                                amountdue10 = amountdue11 = 0;
                                                amountdueinbase9 += amountdueinbase10 + amountdueinbase11;
                                                amountdueinbase10 = amountdueinbase11 = 0;
                                                break;
                                            case 9:
                                                amountdue10 += amountdue11;
                                                amountdue11 = 0;
                                                amountdueinbase10 += amountdueinbase11;
                                                amountdueinbase11 = 0;
                                                break;
                                        }
                                        
                                    } else {
//                                        if (startDate != null && dueDate.before(startDate)) {//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
//                                            accruedbalance += amountdue;
//                                            accruedbalanceinbase += amountdueinbase;
//                                        } else 
                                        if (dueDate.after(oneDayBeforeCal1Date) && (dueDate.before(cal1Date) || dueDate.equals(cal1Date))) {
                                            amountdue1 += amountdue;
                                            amountdueinbase1 += amountdueinbase;
                                        } else if (dueDate.after(cal2Date) || dueDate.equals(cal2Date)) {
                                            amountdue2 += amountdue;
                                            amountdueinbase2 += amountdueinbase;
                                        } else if (dueDate.after(cal3Date) || dueDate.equals(cal3Date)) {
                                            amountdue3 += amountdue;
                                            amountdueinbase3 += amountdueinbase;
                                        } else {
                                            amountdue4 += amountdue;
                                            amountdueinbase4 += amountdueinbase;
                                        }
                                    }
                                }
                                putObj.put("personid", personID);
                                putObj.put("personname", personName);
                                putObj.put("aliasname", aliasname);
                                putObj.put("currencysymbol", currencySymbol);
                                putObj.put("currencyid", currencyid);
                                putObj.put("currencyname", currencyName);
                                putObj.put("amountdue1", amountdue1);
                                putObj.put("amountdue2", amountdue2);
                                putObj.put("amountdue3", amountdue3);
                                putObj.put("amountdue4", amountdue4);
                                putObj.put("amountdue5", amountdue5);
                                putObj.put("amountdue6", amountdue6);
                                putObj.put("amountdue7", amountdue7);
                                putObj.put("amountdue8", amountdue8);
                                putObj.put("amountdue9", amountdue9);
                                putObj.put("amountdue10", amountdue10);
                                putObj.put("amountdue11", amountdue11);
//                                putObj.put(GoodsReceiptCMNConstants.ACCRUEDBALANCE, accruedbalance);
                                putObj.put("amountdueinbase1", amountdueinbase1);
                                putObj.put("amountdueinbase2", amountdueinbase2);
                                putObj.put("amountdueinbase3", amountdueinbase3);
                                putObj.put("amountdueinbase4", amountdueinbase4);
                                putObj.put("amountdueinbase5", amountdueinbase5);
                                putObj.put("amountdueinbase6", amountdueinbase6);
                                putObj.put("amountdueinbase7", amountdueinbase7);
                                putObj.put("amountdueinbase8", amountdueinbase8);
                                putObj.put("amountdueinbase9", amountdueinbase9);
                                putObj.put("amountdueinbase10", amountdueinbase10);
                                putObj.put("amountdueinbase11", amountdueinbase11);
//                                putObj.put("accruedbalanceinbase", accruedbalanceinbase);
                                currencyObj.put(currencyid, putObj);
                            } else {
                                personID = (invobj.has("personid")) ? invobj.getString("personid") : "";
                                personName = (invobj.has("personname")) ? invobj.getString("personname") : "";
                                aliasname = (invobj.has("aliasname")) ? invobj.getString("aliasname") : "";
                                currencySymbol = (invobj.has("currencysymbol")) ? invobj.getString("currencysymbol") : "";
                                currencyid = (invobj.has("currencyid")) ? invobj.getString("currencyid") : "";
                                currencyName = (invobj.has("currencyname")) ? invobj.getString("currencyname") : "";
                                isDN = invobj.has("isDN") ? invobj.getBoolean("isDN") : false;
                                isMP = invobj.has("isMP") ? invobj.getBoolean("isMP") : false;
                                if (currencyObj.has(currencyid)) {
                                    JSONObject addObj = currencyObj.getJSONObject(currencyid);
                                    if (isdistibutive) {
                                        amountdue1 = addObj.getDouble("amountdue1");
                                        amountdue2 = addObj.getDouble("amountdue2");
                                        amountdue3 = addObj.getDouble("amountdue3");
                                        amountdue4 = addObj.getDouble("amountdue4");
                                        amountdue5 = addObj.getDouble("amountdue5");
                                        amountdue6 = addObj.getDouble("amountdue6");
                                        amountdue7 = addObj.getDouble("amountdue7");
                                        amountdue8 = addObj.getDouble("amountdue8");
                                        amountdue9 = addObj.getDouble("amountdue9");
                                        amountdue10 = addObj.getDouble("amountdue10");
                                        amountdue11 = addObj.getDouble("amountdue11");
//                                        accruedbalance = addObj.getDouble(GoodsReceiptCMNConstants.ACCRUEDBALANCE);
                                        amountdueinbase1 = addObj.getDouble("amountdueinbase1");
                                        amountdueinbase2 = addObj.getDouble("amountdueinbase2");
                                        amountdueinbase3 = addObj.getDouble("amountdueinbase3");
                                        amountdueinbase4 = addObj.getDouble("amountdueinbase4");
                                        amountdueinbase5 = addObj.getDouble("amountdueinbase5");
                                        amountdueinbase6 = addObj.getDouble("amountdueinbase6");
                                        amountdueinbase7 = addObj.getDouble("amountdueinbase7");
                                        amountdueinbase8 = addObj.getDouble("amountdueinbase8");
                                        amountdueinbase9 = addObj.getDouble("amountdueinbase9");
                                        amountdueinbase10 = addObj.getDouble("amountdueinbase10");
                                        amountdueinbase11 = addObj.getDouble("amountdueinbase11");
//                                        accruedbalanceinbase = addObj.getDouble("accruedbalanceinbase");
                                    } else {
                                        amountdue1 = addObj.getDouble("amountdue1");
                                        amountdue2 = addObj.getDouble("amountdue2");
                                        amountdue3 = addObj.getDouble("amountdue3");
//                                        accruedbalance = addObj.getDouble(GoodsReceiptCMNConstants.ACCRUEDBALANCE);
                                        amountdueinbase1 = addObj.getDouble("amountdueinbase1");
                                        amountdueinbase2 = addObj.getDouble("amountdueinbase2");
                                        amountdueinbase3 = addObj.getDouble("amountdueinbase3");
//                                        accruedbalanceinbase = addObj.getDouble("accruedbalanceinbase");
                                    }
                                }
                                Date dueDate = new Date();
                                if (datefilter == 0 || datefilter == Constants.agedDueDate0to30Filter) {
                                    dueDate = authHandler.getDateOnlyFormat( ).parse(invobj.getString("duedate"));
                                } else {
                                    dueDate = authHandler.getDateOnlyFormat( ).parse(invobj.getString("date"));
                                }
                                double amountdue = invobj.getDouble("amountdue");
                                double amountdueinbase = invobj.getDouble("amountdueinbase");
                                boolean isopeningBalanceTransaction = invobj.optBoolean(GoodsReceiptCMNConstants.IsopeningBalanceTransaction, false);
                                if (isDN || isMP) {
                                    if (!isopeningBalanceTransaction) {
                                        amountdue = -amountdue;
                                        amountdueinbase = -amountdueinbase;
                                    }
                                }

                                if (customizedSumryReportFlag) {
                                    if ((dueDate.after(customizeSumryTo1Date) || dueDate.equals(customizeSumryTo1Date)) && (dueDate.before(customizeSumryFrom1Date) || dueDate.equals(customizeSumryFrom1Date))) {
                                        amountdue1 += amountdue;
                                        amountdueinbase1 += amountdueinbase;
                                    } else if ((dueDate.after(customizeSumryTo2Date) || dueDate.equals(customizeSumryTo1Date)) && (dueDate.before(customizeSumryFrom2Date) || dueDate.equals(customizeSumryFrom2Date))) {
                                        amountdue2 += amountdue;
                                        amountdueinbase2 += amountdueinbase;
                                    } else if ((dueDate.after(customizeSumryTo3Date) || dueDate.equals(customizeSumryTo3Date)) && (dueDate.before(customizeSumryFrom3Date) || dueDate.equals(customizeSumryFrom3Date))) {
                                        amountdue3 += amountdue;
                                        amountdueinbase3 += amountdueinbase;
                                    } else if ((dueDate.after(customizeSumryTo4Date) || dueDate.equals(customizeSumryTo4Date)) && (dueDate.before(customizeSumryFrom4Date) || dueDate.equals(customizeSumryFrom4Date))) {
                                        amountdue4 += amountdue;
                                        amountdueinbase4 += amountdueinbase;
                                    } else if ((dueDate.after(customizeSumryTo5Date) || dueDate.equals(customizeSumryTo5Date)) && (dueDate.before(customizeSumryFrom5Date) || dueDate.equals(customizeSumryFrom5Date))) {
                                        amountdue5 += amountdue;
                                        amountdueinbase5 += amountdueinbase;
                                    } else if ((dueDate.after(customizeSumryTo6Date) || dueDate.equals(customizeSumryTo6Date)) && (dueDate.before(customizeSumryFrom6Date) || dueDate.equals(customizeSumryFrom6Date))) {
                                        amountdue6 += amountdue;
                                        amountdueinbase6 += amountdueinbase;
                                    } else if ((dueDate.after(customizeSumryTo7Date) || dueDate.equals(customizeSumryTo7Date)) && (dueDate.before(customizeSumryFrom7Date) || dueDate.equals(customizeSumryFrom7Date))) {
                                        amountdue7 += amountdue;
                                        amountdueinbase7 += amountdueinbase;
                                    }

                                } else {
                                    if (isdistibutive) {
//                                        if (startDate != null && dueDate.before(startDate)) {//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
//                                            accruedbalance += amountdue;
//                                            accruedbalanceinbase += amountdueinbase;
//                                        } else 
                                        if (dueDate.after(oneDayBeforeCal1Date)) {
                                            if (dueDate.equals(cal1Date) && (datefilter == Constants.agedDueDate0to30Filter || datefilter == Constants.agedInvoiceDate0to30Filter)) {
                                                amountdueinbase2 += amountdueinbase;
                                                amountdue2 += amountdue;
                                            } else {
                                                amountdueinbase1 += amountdueinbase;
                                                amountdue1 += amountdue;
                                            }
                                        } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) {
                                            amountdue2 += amountdue;
                                            amountdueinbase2 += amountdueinbase;
                                        } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) {
                                            amountdue3 += amountdue;
                                            amountdueinbase3 += amountdueinbase;
                                        } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) {
                                            amountdue4 += amountdue;
                                            amountdueinbase4 += amountdueinbase;
                                        } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) {
                                            amountdue5 += amountdue;
                                            amountdueinbase5 += amountdueinbase;
                                        } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) {
                                            amountdue6 += amountdue;
                                            amountdueinbase6 += amountdueinbase;
                                        } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) {
                                            amountdue7 += amountdue;
                                            amountdueinbase7 += amountdueinbase;
                                        } else if ((cal8Date.before(dueDate) || cal8Date.equals(dueDate)) && cal7Date.after(dueDate)) {
                                            amountdue8 += amountdue;
                                            amountdueinbase8 += amountdueinbase;
                                        } else if ((cal9Date.before(dueDate) || cal9Date.equals(dueDate)) && cal8Date.after(dueDate)) {
                                            amountdue9 += amountdue;
                                            amountdueinbase9 += amountdueinbase;
                                        } else if ((cal10Date.before(dueDate) || cal10Date.equals(dueDate)) && cal9Date.after(dueDate)) {
                                            amountdue10 += amountdue;
                                            amountdueinbase10 += amountdueinbase;
                                        } else {
                                            amountdue11 += amountdue;
                                            amountdueinbase11 += amountdueinbase;
                                        }
                                        
                                        switch(noOfInterval){
                                            case 2:
                                                amountdue3 += amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                                amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                                amountdueinbase3 += amountdueinbase4 + amountdueinbase5 + amountdueinbase6 + amountdueinbase7 + amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                                amountdueinbase4 = amountdueinbase5 = amountdueinbase6 = amountdueinbase7 = amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                                break;
                                            case 3:
                                                amountdue4 += amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                                amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                                amountdueinbase4 += amountdueinbase5 + amountdueinbase6 + amountdueinbase7 + amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                                amountdueinbase5 = amountdueinbase6 = amountdueinbase7 = amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                                break;
                                            case 4:
                                                amountdue5 += amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                                amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                                amountdueinbase5 += amountdueinbase6 + amountdueinbase7 + amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                                amountdueinbase6 = amountdueinbase7 = amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                                break;
                                            case 5:
                                                amountdue6 += amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                                amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                                amountdueinbase6 += amountdueinbase7 + amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                                amountdueinbase7 = amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                                break;
                                            case 6:
                                                amountdue7 += amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                                amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                                amountdueinbase7 += amountdueinbase8 + amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                                amountdueinbase8 = amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                                break;
                                            case 7:
                                                amountdue8 += amountdue9 + amountdue10 + amountdue11;
                                                amountdue9 = amountdue10 = amountdue11 = 0;
                                                amountdueinbase8 += amountdueinbase9 + amountdueinbase10 + amountdueinbase11;
                                                amountdueinbase9 = amountdueinbase10 = amountdueinbase11 = 0;
                                                break;
                                            case 8:
                                                amountdue9 += amountdue10 + amountdue11;
                                                amountdue10 = amountdue11 = 0;
                                                amountdueinbase9 += amountdueinbase10 + amountdueinbase11;
                                                amountdueinbase10 = amountdueinbase11 = 0;
                                                break;
                                            case 9:
                                                amountdue10 += amountdue11;
                                                amountdue11 = 0;
                                                amountdueinbase10 += amountdueinbase11;
                                                amountdueinbase11 = 0;
                                                break;
                                        }
                                    } else {
//                                        if (startDate != null && dueDate.before(startDate)) {//In Aged Report amountdue goes in Accruade Balance for those transaction whose creation/due date is previous to Start date will goes into the accrued balance, likes opening balance 
//                                            accruedbalance += amountdue;
//                                            accruedbalanceinbase += amountdueinbase;
//                                        } else 
                                        if (dueDate.after(oneDayBeforeCal1Date) && (dueDate.before(cal1Date) || dueDate.equals(cal1Date))) {
                                            amountdue1 += amountdue;
                                            amountdueinbase1 += amountdueinbase;
                                        } else if (dueDate.after(cal2Date) || dueDate.equals(cal2Date)) {
                                            amountdue2 += amountdue;
                                            amountdueinbase2 += amountdueinbase;
                                        } else if (dueDate.after(cal3Date) || dueDate.equals(cal3Date)) {
                                            amountdue3 += amountdue;
                                            amountdueinbase3 += amountdueinbase;
                                        } else {
                                            amountdue4 += amountdue;
                                            amountdueinbase4 += amountdueinbase;
                                        }
                                    }
                                }
                                putObj.put("personid", personID);
                                putObj.put("personname", personName);
                                putObj.put("aliasname", aliasname);
                                putObj.put("currencysymbol", currencySymbol);
                                putObj.put("currencyid", currencyid);
                                putObj.put("currencyname", currencyName);
                                putObj.put("amountdue1", amountdue1);
                                putObj.put("amountdue2", amountdue2);
                                putObj.put("amountdue3", amountdue3);
                                putObj.put("amountdue4", amountdue4);
                                putObj.put("amountdue5", amountdue5);
                                putObj.put("amountdue6", amountdue6);
                                putObj.put("amountdue7", amountdue7);
                                putObj.put("amountdue8", amountdue8);
                                putObj.put("amountdue9", amountdue9);
                                putObj.put("amountdue10", amountdue10);
                                putObj.put("amountdue11", amountdue11);
//                                putObj.put(GoodsReceiptCMNConstants.ACCRUEDBALANCE, accruedbalance);
                                putObj.put("amountdueinbase1", amountdueinbase1);
                                putObj.put("amountdueinbase2", amountdueinbase2);
                                putObj.put("amountdueinbase3", amountdueinbase3);
                                putObj.put("amountdueinbase4", amountdueinbase4);
                                putObj.put("amountdueinbase5", amountdueinbase5);
                                putObj.put("amountdueinbase6", amountdueinbase6);
                                putObj.put("amountdueinbase7", amountdueinbase7);
                                putObj.put("amountdueinbase8", amountdueinbase8);
                                putObj.put("amountdueinbase9", amountdueinbase9);
                                putObj.put("amountdueinbase10", amountdueinbase10);
                                putObj.put("amountdueinbase11", amountdueinbase11);
//                                putObj.put("accruedbalanceinbase", accruedbalanceinbase);
                                currencyObj.put(currencyid, putObj);
                            }
                        }
                        Iterator itr = currencyObj.keys();
                        while (itr.hasNext()) {
                            JSONObject getObj = currencyObj.getJSONObject(itr.next().toString());
                            jObj = new JSONObject();
                            if (getObj.has("personid")) {
                                jObj.put("personid", getObj.getString("personid"));
                            }
                            if (getObj.has("personname")) {
                                jObj.put("personname"+linedetails, getObj.getString("personname"));
                            }
                            if (getObj.has("aliasname")) {
                                jObj.put("aliasname"+linedetails, getObj.getString("aliasname"));
                            }
                            jObj.put("amountdue1"+linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue1"), companyid)));
                            jObj.put("amountdue2"+linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue2"), companyid)));
                            jObj.put("amountdue3"+linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue3"), companyid)));
                            jObj.put("amountdue4"+linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue4"), companyid)));
                            jObj.put("amountdue5"+linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue5"), companyid)));
                            jObj.put("amountdue6"+linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue6"), companyid)));
                            jObj.put("amountdue7"+linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue7"), companyid)));
                            jObj.put("amountdue8"+linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue8"), companyid)));
                            jObj.put("amountdue9"+linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue9"), companyid)));
                            jObj.put("amountdue10"+linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue10"), companyid)));
                            jObj.put("amountdue11"+linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble("amountdue11"), companyid)));
//                            jObj.put(GoodsReceiptCMNConstants.ACCRUEDBALANCE + linedetails, Double.valueOf(authHandler.formattedAmount(getObj.getDouble(GoodsReceiptCMNConstants.ACCRUEDBALANCE), companyid)));
                            jObj.put("currencysymbol"+linedetails, getObj.getString("currencysymbol"));
                            jObj.put("currencyid"+linedetails, getObj.getString("currencyid"));
                            KwlReturnObject objItr = accountingHandlerDAOobj.getObject(KWLCurrency.class.getName(), getObj.getString("currencyid"));
                            KWLCurrency currency = (KWLCurrency) objItr.getEntityList().get(0);
                            jObj.put("currencyCode"+linedetails,currency.getCurrencyCode());
                            jObj.put("currencyname"+linedetails, getObj.getString("currencyname"));
                            double amountdue = Double.valueOf(authHandler.formattedAmount((getObj.getDouble("amountdue1") + getObj.getDouble("amountdue2") + getObj.getDouble("amountdue3") + getObj.getDouble("amountdue4") + getObj.getDouble("amountdue5") + getObj.getDouble("amountdue6") + getObj.getDouble("amountdue7") + getObj.getDouble("amountdue8") + getObj.getDouble("amountdue9") + getObj.getDouble("amountdue10") + getObj.getDouble("amountdue11")), companyid));
                            jObj.put("total"+linedetails, amountdue);
                            double amountdueinbase = Double.valueOf(authHandler.formattedAmount((getObj.getDouble("amountdueinbase1") + getObj.getDouble("amountdueinbase2") + getObj.getDouble("amountdueinbase3") + getObj.getDouble("amountdueinbase4") + getObj.getDouble("amountdueinbase5") + getObj.getDouble("amountdueinbase6") + getObj.getDouble("amountdueinbase7") + getObj.getDouble("amountdueinbase8") + getObj.getDouble("amountdueinbase9") + getObj.getDouble("amountdueinbase10") + getObj.getDouble("amountdueinbase11")), companyid));
                            jObj.put("totalinbase"+linedetails, amountdueinbase);
                            if(!isDetailedXls)
                        totalinbase+=amountdueinbase;
                        if(customizedSumryReportFlag && !showCustVendorWithZeroAmounts && amountdue==0){
                            continue;
                        }
                            jArr.put(jObj);
                        }
                    }
                }
                if(exportAgedPayables || isDetailedXls){ //  Used for export CSV- Add totalinbase
                    JSONObject jtotal = new JSONObject();
                    if (customizedSumryReportFlag) {
                        jtotal.put("personname", "Total");
                        jtotal.put("total", totalinbase);
                    } else {
                        jtotal.put("total", "Total");
                        jtotal.put("totalinbase", totalinbase);
                    }
                    jArr.put(jtotal);
                }
                System.out.println("Invoice Count: "+invoiceCount);
                System.out.println("JSON Count: " + jsonCount);
                System.out.println("opening Invoice Count: "+openingCount);
                System.out.println("opening JSON Count: " + openingJSONCount);
        } catch (JSONException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getVendorAgedPayable : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getVendorAgedPayable : " + ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getVendorAgedPayable : " + ex.getMessage(), ex);
        }
        return jArr;
    }
     
    @Override
    public JSONObject getVendorAgedPayablebasedonDimensions(HttpServletRequest request, HashMap<String, Object> requestParams) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            String curDateString = (!StringUtil.isNullOrEmpty(request.getParameter(GoodsReceiptCMNConstants.CURDATE))) ? request.getParameter(GoodsReceiptCMNConstants.CURDATE) : (request.getParameter("stdate") != null ? request.getParameter("stdate") : request.getParameter("startdate"));
            DateFormat df = authHandler.getDateOnlyFormat(request);
            Date curDate = df.parse(curDateString);
            int invoiceCount = 0, jsonCount = 0, openingCount = 0, openingJSONCount = 0;
            int duration = request.getParameter(GoodsReceiptCMNConstants.DURATION) == null ? 0 : Integer.parseInt(request.getParameter(GoodsReceiptCMNConstants.DURATION));
            boolean isdistibutive = StringUtil.getBoolean(request.getParameter(GoodsReceiptCMNConstants.ISDISTRIBUTIVE));
            int datefilter = StringUtil.getInteger(request.getParameter("datefilter"));// 0 = Invoice Due date OR 1 = Invoice date

            int noOfInterval = Constants.DefaultNoOfIntervals;
            if(requestParams.containsKey("noOfInterval") && requestParams.get("noOfInterval") != null) {
               noOfInterval = requestParams.get("noOfInterval").toString().equals("") ? Constants.DefaultNoOfIntervals : Integer.parseInt(requestParams.get("noOfInterval").toString());
            }

            
            JSONObject commData = new JSONObject();
            JSONObject jMeta = new JSONObject();
            JSONArray jarrColumns = new JSONArray();
            JSONArray jarrRecords = new JSONArray();
            JSONObject jobjTemp = new JSONObject();

            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), sessionHandlerImpl.getCompanyid(request));
            Company company = (Company) cmpresult.getEntityList().get(0);
            String baseCurrencyName = "";
            if (company != null && company.getCurrency() != null) {
                baseCurrencyName = company.getCurrency().getName();
            }
            int reportId = 816;

            String customizeHeader[] = request.getParameterValues("arr");
            HashMap hashMap=new HashMap();
            hashMap.put("companyId", company.getCompanyID());
            hashMap.put("reportId", reportId);
            KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(hashMap);
            List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
              List customFieldList = new ArrayList();

            double amountdue1 = 0;
            double amountdue2 = 0;
            double amountdue3 = 0;
            double amountdue4 = 0;
            double amountdue5 = 0;
            double amountdue6 = 0;
            double amountdue7 = 0;
            double amountdue8 = 0;
            double amountdue9 = 0;
            double amountdue10 = 0;
            double amountdue11 = 0;

            double amountdue1Base = 0;
            double amountdue2Base = 0;
            double amountdue3Base = 0;
            double amountdue4Base = 0;
            double amountdue5Base = 0;
            double amountdue6Base = 0;
            double amountdue7Base = 0;
            double amountdue8Base = 0;
            double amountdue9Base = 0;
            double amountdue10Base = 0;
            double amountdue11Base = 0;

            double amountdueinbase1 = 0;
            double amountdueinbase2 = 0;
            double amountdueinbase3 = 0;
            double amountdueinbase4 = 0;
            double amountdueinbase5 = 0;
            double amountdueinbase6 = 0;
            double amountdueinbase7 = 0;
            double amountdueinbase8 = 0;
            double amountdueinbase9 = 0;
            double amountdueinbase10 = 0;
            double amountdueinbase11 = 0;

            double totalinbase = 0;
            Calendar oneDayBeforeCal1 = Calendar.getInstance();
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            Calendar cal8 = Calendar.getInstance();
            Calendar cal9 = Calendar.getInstance();
            Calendar cal10 = Calendar.getInstance();
            oneDayBeforeCal1.setTime(curDate);
            cal1.setTime(curDate);
            cal2.setTime(curDate);
            cal3.setTime(curDate);
            cal4.setTime(curDate);
            cal5.setTime(curDate);
            cal6.setTime(curDate);
            cal7.setTime(curDate);
            cal8.setTime(curDate);
            cal9.setTime(curDate);
            cal10.setTime(curDate);
            oneDayBeforeCal1.add(Calendar.DAY_OF_YEAR, -1);
            cal2.add(Calendar.DAY_OF_YEAR, -duration);
            cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
            cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
            cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
            cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
            cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
            cal8.add(Calendar.DAY_OF_YEAR, -(duration * 7));
            cal9.add(Calendar.DAY_OF_YEAR, -(duration * 8));
            cal10.add(Calendar.DAY_OF_YEAR, -(duration * 9));
            
            Date oneDayBeforeCal1Date = null;
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;
            Date cal8Date = null;
            Date cal9Date = null;
            Date cal10Date = null;

            String oneDayBeforeCal1String = authHandler.getDateOnlyFormat().format(oneDayBeforeCal1.getTime());
            oneDayBeforeCal1Date = authHandler.getDateOnlyFormat().parse(oneDayBeforeCal1String);

            String cal1String = authHandler.getDateOnlyFormat().format(cal1.getTime());
            cal1Date = authHandler.getDateOnlyFormat().parse(cal1String);

            String cal2String = authHandler.getDateOnlyFormat().format(cal2.getTime());
            cal2Date = authHandler.getDateOnlyFormat().parse(cal2String);

            String cal3String = authHandler.getDateOnlyFormat().format(cal3.getTime());
            cal3Date = authHandler.getDateOnlyFormat().parse(cal3String);

            String cal4String = authHandler.getDateOnlyFormat().format(cal4.getTime());
            cal4Date = authHandler.getDateOnlyFormat().parse(cal4String);

            String cal5String = authHandler.getDateOnlyFormat().format(cal5.getTime());
            cal5Date = authHandler.getDateOnlyFormat().parse(cal5String);

            String cal6String = authHandler.getDateOnlyFormat().format(cal6.getTime());
            cal6Date = authHandler.getDateOnlyFormat().parse(cal6String);

            String cal7String = authHandler.getDateOnlyFormat().format(cal7.getTime());
            cal7Date = authHandler.getDateOnlyFormat().parse(cal7String);

            String cal8String = authHandler.getDateOnlyFormat().format(cal8.getTime());
            cal8Date = authHandler.getDateOnlyFormat().parse(cal8String);

            String cal9String = authHandler.getDateOnlyFormat().format(cal9.getTime());
            cal9Date = authHandler.getDateOnlyFormat().parse(cal7String);

            String cal10String = authHandler.getDateOnlyFormat().format(cal10.getTime());
            cal10Date = authHandler.getDateOnlyFormat().parse(cal10String);
         
            Map<String, String> amountDueMap = new HashMap<String, String>();
            boolean exportAgedPayables = false;
            if (requestParams.containsKey("exportAgedReceivables") && requestParams.get("exportAgedReceivables") != null) {
                exportAgedPayables = Boolean.parseBoolean(requestParams.get("exportAgedReceivables").toString());
            }
            boolean isAgedPayables = false;
            if (requestParams.containsKey("isAgedPayables") && requestParams.get("isAgedPayables") != null) {
                isAgedPayables = Boolean.parseBoolean(requestParams.get("isAgedPayables").toString());
            }
            String filterConjuctionCriteria="and";
            if (requestParams.containsKey(Constants.Filter_Criteria) && requestParams.get(Constants.Filter_Criteria) != null) {
                filterConjuctionCriteria =requestParams.get(Constants.Filter_Criteria).toString();
            }
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_Vendor_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            KwlReturnObject result = accVendorDAOobj.getVendorAndCurrencyDetailsForAgedPayable(requestParams);
            List<Object[]> vendorList = result.getEntityList();
            if (requestParams.containsKey("start")) { //Removing paging here because it is applying on each vendor data (CN/DN etc). Due to this complete amount due not showing in AP Summary.  
                requestParams.remove("start");
            }
            if (requestParams.containsKey("limit")) {
                requestParams.remove("limit");
            }
            requestParams.put("oneDayBeforeCal1", oneDayBeforeCal1);
            requestParams.put("cal1", cal1);
            requestParams.put("cal2", cal2);
            requestParams.put("cal3", cal3);
            requestParams.put("cal4", cal4);
            requestParams.put("cal5", cal5);
            requestParams.put("cal6", cal6);
            requestParams.put("cal7", cal7);
            requestParams.put("cal8", cal8);
            requestParams.put("cal9", cal9);
            requestParams.put("cal10", cal10);
            if (isAgedPayables) {
                requestParams.put("isAgedPayables", isAgedPayables);
            }
            for (Object[] obj : vendorList) {
                Object venid = obj[0];
                requestParams.put(GoodsReceiptCMNConstants.VENDORID, venid);
                requestParams.put(GoodsReceiptCMNConstants.ACCID, venid);
                requestParams.put("cntype", null);
                requestParams.put("isAgedSummary", true);
                requestParams.put(Constants.Filter_Criteria, filterConjuctionCriteria);
//
                JSONArray invjarr = new JSONArray();
                result = accGoodsReceiptobj.getGoodsReceiptsMerged(requestParams);
                invoiceCount += result.getEntityList().size();
                if (result != null && !result.getEntityList().isEmpty()) {
                    invjarr = accGoodsReceiptServiceHandler.getGoodsReceiptsJsonForAgedPayables(requestParams, result.getEntityList(), invjarr, accountingHandlerDAOobj, accCurrencyDAOobj, accGoodsReceiptobj, accAccountDAOobj, accGoodsReceiptCommon, accTaxObj);
                    jsonCount += invjarr.length();
                }
                JSONArray OBJArryInvoice = new JSONArray();
                result = accGoodsReceiptobj.getOpeningBalanceInvoices(requestParams);
                openingCount += result.getEntityList().size();
                if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                    OBJArryInvoice = AccGoodsReceiptServiceHandler.getAgedPayablesOpeningBalanceInvoiceJson(requestParams, result.getEntityList(), OBJArryInvoice, accCurrencyDAOobj, accountingHandlerDAOobj, accGoodsReceiptCommon);
                    openingJSONCount += OBJArryInvoice.length();
                    for (int i = 0; i < OBJArryInvoice.length(); i++) {
                        invjarr.put(OBJArryInvoice.get(i));
                    }
                }

                if (invjarr.length() < 1) {
                    continue;
                }
                amountdue1 = amountdue2 = amountdue3 = amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                amountdue1Base = amountdue2Base = amountdue3Base = amountdue4Base = amountdue5Base = amountdue6Base = amountdue7Base = amountdue8Base = amountdue9Base = amountdue10Base = amountdue11Base = 0;
                String personID = null;
                String personName = null;
                String aliasname = "";
                String currencySymbol = null;
                String currencyid = null;
                String currencyidVen = obj[1].toString();
                String currencyName = obj[2].toString();
                String currencySymbolVen = obj[3].toString();

                for (int i = 0; i < invjarr.length(); i++) {
                    JSONObject invobj = invjarr.getJSONObject(i);
                    personID = invobj.getString(GoodsReceiptCMNConstants.PERSONID);
                    personName = invobj.getString(GoodsReceiptCMNConstants.PERSONNAME);
                    aliasname = invobj.has(GoodsReceiptCMNConstants.ALIASNAME) ? (invobj.getString(GoodsReceiptCMNConstants.ALIASNAME) != null ? invobj.getString(GoodsReceiptCMNConstants.ALIASNAME) : "") : "";
                    currencySymbol = invobj.getString(GoodsReceiptCMNConstants.CURRENCYSYMBOL);
                    currencyid = invobj.getString(GoodsReceiptCMNConstants.CURRENCYID);

                    Date dueDate = null;
                    if (!StringUtil.isNullOrEmpty(invobj.getString(GoodsReceiptCMNConstants.DUEDATE))) {
                        dueDate = authHandler.getDateOnlyFormat(request).parse(invobj.getString(GoodsReceiptCMNConstants.DUEDATE));
                    }

                    if (datefilter == 0 || datefilter == Constants.agedDueDate0to30Filter) {
                        dueDate = authHandler.getDateOnlyFormat(request).parse(invobj.getString(GoodsReceiptCMNConstants.DUEDATE));
                    } else {
                        dueDate = authHandler.getDateOnlyFormat(request).parse(invobj.getString(GoodsReceiptCMNConstants.DATE));
                    }

                    double amountdue = invobj.has(GoodsReceiptCMNConstants.AMOUNTDUE) ? invobj.getDouble(GoodsReceiptCMNConstants.AMOUNTDUE) : 0;
                    double amountdueinbase = invobj.has(GoodsReceiptCMNConstants.AMOUNTDUEINBASE) ? invobj.getDouble(GoodsReceiptCMNConstants.AMOUNTDUEINBASE) : 0;
                    double externalcurrencyrate = invobj.getDouble(GoodsReceiptCMNConstants.EXTERNALCURRENCYRATE);

                    boolean isopeningBalanceTransaction = invobj.optBoolean(GoodsReceiptCMNConstants.IsopeningBalanceTransaction, false);
                    boolean isConversionRateFromCurrencyToBase = invobj.optBoolean(GoodsReceiptCMNConstants.IsConversionRateFromCurrencyToBase, false);

                    KwlReturnObject bAmt = null;
                    if (isopeningBalanceTransaction && isConversionRateFromCurrencyToBase) {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(requestParams, amountdue, currencyid, currencyidVen, df.parse(invobj.getString(GoodsReceiptCMNConstants.DATE)), externalcurrencyrate);
                    } else {
                        bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(requestParams, amountdue, currencyid, currencyidVen, df.parse(invobj.getString(GoodsReceiptCMNConstants.DATE)), externalcurrencyrate);
                    }
                    amountdue = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);

                    if (isdistibutive) {
                        if (dueDate.after(oneDayBeforeCal1Date)) {
                            if (dueDate.equals(cal1Date) && (datefilter == Constants.agedDueDate0to30Filter || datefilter == Constants.agedInvoiceDate0to30Filter)) {
                                amountdue2 += amountdue;
                                amountdue2Base += amountdueinbase;
                            } else {
                                amountdue1 += amountdue;
                                amountdue1Base += amountdueinbase;
                            }
                        } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) {
                            amountdue2 += amountdue;
                            amountdue2Base += amountdueinbase;
                        } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) {
                            amountdue3 += amountdue;
                            amountdue3Base += amountdueinbase;
                        } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) {
                            amountdue4 += amountdue;
                            amountdue4Base += amountdueinbase;
                        } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) {
                            amountdue5 += amountdue;
                            amountdue5Base += amountdueinbase;
                        } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) {
                            amountdue6 += amountdue;
                            amountdue6Base += amountdueinbase;
                        } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) {
                            amountdue7 += amountdue;
                            amountdue7Base += amountdueinbase;
                        } else if ((cal8Date.before(dueDate) || cal8Date.equals(dueDate)) && cal7Date.after(dueDate)) {
                            amountdue8 += amountdue;
                            amountdue8Base += amountdueinbase;
                        } else if ((cal9Date.before(dueDate) || cal9Date.equals(dueDate)) && cal8Date.after(dueDate)) {
                            amountdue9 += amountdue;
                            amountdue9Base += amountdueinbase;
                        } else if ((cal10Date.before(dueDate) || cal10Date.equals(dueDate)) && cal9Date.after(dueDate)) {
                            amountdue10 += amountdue;
                            amountdue10Base += amountdueinbase;
                        } else {
                            amountdue11 += amountdue;
                            amountdue11Base += amountdueinbase;
                        }
                        
                        switch(noOfInterval){
                            case 2:
                                amountdue3 += amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                amountdue3Base += amountdue4Base + amountdue5Base + amountdue6Base + amountdue7Base + amountdue8Base + amountdue9Base + amountdue10Base + amountdue11Base;
                                amountdue4Base = amountdue5Base = amountdue6Base = amountdue7Base = amountdue8Base = amountdue9Base = amountdue10Base = amountdue11Base = 0;
                                break;
                            case 3:
                                amountdue4 += amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                amountdue4Base += amountdue5Base + amountdue6Base + amountdue7Base + amountdue8Base + amountdue9Base + amountdue10Base + amountdue11Base;
                                amountdue5Base = amountdue6Base = amountdue7Base = amountdue8Base = amountdue9Base = amountdue10Base = amountdue11Base = 0;
                                break;
                            case 4:
                                amountdue5 += amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                amountdue5Base += amountdue6Base + amountdue7Base + amountdue8Base + amountdue9Base + amountdue10Base + amountdue11Base;
                                amountdue6Base = amountdue7Base = amountdue8Base = amountdue9Base = amountdue10Base = amountdue11Base = 0;
                                break;
                            case 5:
                                amountdue6 += amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                amountdue6Base += amountdue7Base + amountdue8Base + amountdue9Base + amountdue10Base + amountdue11Base;
                                amountdue7Base = amountdue8Base = amountdue9Base = amountdue10Base = amountdue11Base = 0;
                                break;
                            case 6:
                                amountdue7 += amountdue8 + amountdue9 + amountdue10 + amountdue11;
                                amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                                amountdue7Base += amountdue8Base + amountdue9Base + amountdue10Base + amountdue11Base;
                                amountdue8Base = amountdue9Base = amountdue10Base = amountdue11Base = 0;
                                break;
                            case 7:
                                amountdue8Base += amountdue9Base + amountdue10Base + amountdue11Base;
                                amountdue9Base = amountdue10Base = amountdue11Base = 0;
                                amountdue8Base += amountdue9Base + amountdue10Base + amountdue11Base;
                                amountdue9Base = amountdue10Base = amountdue11Base = 0;
                                break;
                            case 8:
                                amountdue9 += amountdue10 + amountdue11;
                                amountdue10 = amountdue11 = 0;
                                amountdue9Base += amountdue10Base + amountdue11Base;
                                amountdue10Base = amountdue11Base = 0;
                                break;
                            case 9:
                                amountdue10 += amountdue11;
                                amountdue11 = 0;
                                amountdue10Base += amountdue11Base;
                                amountdue11Base = 0;
                                break;
                        }
                        
                    } else {
                        if (dueDate.after(oneDayBeforeCal1Date) && (dueDate.before(cal1Date) || dueDate.equals(cal1Date))) {
                            amountdue1 += amountdue;
                            amountdue1Base += amountdueinbase;
                        } else if (dueDate.after(cal2Date) || dueDate.equals(cal2Date)) {
                            amountdue2 += amountdue;
                            amountdue2Base += amountdueinbase;
                        } else if (dueDate.after(cal3Date) || dueDate.equals(cal3Date)) {
                            amountdue3 += amountdue;
                            amountdue3Base += amountdueinbase;
                        } else {
                            amountdue4 += amountdue;
                            amountdue4Base += amountdueinbase;
                        }
                    }
                }
                if (invjarr.length() > 0) {
                    JSONObject custJobj = new JSONObject();
                    JSONObject jObj = new JSONObject();
                    jObj.put(GoodsReceiptCMNConstants.PERSONID, personID);
                    jObj.put(GoodsReceiptCMNConstants.PERSONNAME, personName);
                    jObj.put(GoodsReceiptCMNConstants.ALIASNAME, aliasname);
                    jObj.put(GoodsReceiptCMNConstants.CURRENCYID, currencyidVen);
                    jObj.put(GoodsReceiptCMNConstants.CURRENCYNAME, currencyName);
                    jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE1, Double.valueOf(authHandler.formattedAmount(amountdue1, companyid)));
                    jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE2, Double.valueOf(authHandler.formattedAmount(amountdue2, companyid)));
                    jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE3, Double.valueOf(authHandler.formattedAmount(amountdue3, companyid)));
                    jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE4, Double.valueOf(authHandler.formattedAmount(amountdue4, companyid)));
                    jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE5, Double.valueOf(authHandler.formattedAmount(amountdue5, companyid)));
                    jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE6, Double.valueOf(authHandler.formattedAmount(amountdue6, companyid)));
                    jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE7, Double.valueOf(authHandler.formattedAmount(amountdue7, companyid)));
                    jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE8, Double.valueOf(authHandler.formattedAmount(amountdue8, companyid)));
                    jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE9, Double.valueOf(authHandler.formattedAmount(amountdue9, companyid)));
                    jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE10, Double.valueOf(authHandler.formattedAmount(amountdue10, companyid)));
                    jObj.put(GoodsReceiptCMNConstants.AMOUNTDUE11, Double.valueOf(authHandler.formattedAmount(amountdue11, companyid)));
                    jObj.put(GoodsReceiptCMNConstants.CURRENCYSYMBOL, currencySymbolVen);
                    jObj.put("amountdueinbase1", Double.valueOf(authHandler.formattedAmount(amountdue1Base, companyid)));
                    jObj.put("amountdueinbase2", Double.valueOf(authHandler.formattedAmount(amountdue2Base, companyid)));
                    jObj.put("amountdueinbase3", Double.valueOf(authHandler.formattedAmount(amountdue3Base, companyid)));
                    jObj.put("amountdueinbase4", Double.valueOf(authHandler.formattedAmount(amountdue4Base, companyid)));
                    jObj.put("amountdueinbase5", Double.valueOf(authHandler.formattedAmount(amountdue5Base, companyid)));
                    jObj.put("amountdueinbase6", Double.valueOf(authHandler.formattedAmount(amountdue6Base, companyid)));
                    jObj.put("amountdueinbase7", Double.valueOf(authHandler.formattedAmount(amountdue7Base, companyid)));
                    jObj.put("amountdueinbase8", Double.valueOf(authHandler.formattedAmount(amountdue8Base, companyid)));
                    jObj.put("amountdueinbase9", Double.valueOf(authHandler.formattedAmount(amountdue9Base, companyid)));
                    jObj.put("amountdueinbase10", Double.valueOf(authHandler.formattedAmount(amountdue10Base, companyid)));
                    jObj.put("amountdueinbase11", Double.valueOf(authHandler.formattedAmount(amountdue11Base, companyid)));
                    double amountdue = Double.valueOf(authHandler.formattedAmount((amountdue1 + amountdue2 + amountdue3 + amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11), companyid));
                    double amountdueBase = Double.valueOf(authHandler.formattedAmount((amountdue1Base + amountdue2Base + amountdue3Base + amountdue4Base + amountdue5Base + amountdue6Base + amountdue7Base + amountdue8Base + amountdue9Base + amountdue10Base + amountdue11Base), companyid));
                    jObj.put(GoodsReceiptCMNConstants.TOTAL, amountdue);
                    jObj.put("totalinbase", amountdueBase);
                    totalinbase += amountdueBase;
                    
                    String customFieldMapValues = "";
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    KwlReturnObject custumObjresult = accountingHandlerDAOobj.getObject(VendorCustomData.class.getName(), personID);
                    replaceFieldMap = new HashMap<String, String>();
                    if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                        VendorCustomData jeDetailCustom = (VendorCustomData) custumObjresult.getEntityList().get(0);
                        if (jeDetailCustom != null) {
                            AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                            JSONObject params = new JSONObject();
                            params.put("isExport", true);
                            fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, custJobj, params);
                        }
                    }
                    for (CustomizeReportMapping customizeReportMapping : customizeReportList) {
                        String column = "Custom_" + customizeReportMapping.getDataIndex();

                        if (custJobj.has(column)) {
                            customFieldMapValues = custJobj.getString(column);
                            if (!customFieldList.contains(customizeReportMapping.getDataIndex())) {
//
                                jobjTemp = new JSONObject();
                                jobjTemp.put("name", customizeReportMapping.getDataIndex());
                                jarrRecords.put(jobjTemp);

                                jobjTemp = new JSONObject();
                                jobjTemp.put("header", customizeReportMapping.getDataHeader());
                                jobjTemp.put("dataIndex", customizeReportMapping.getDataIndex());
                                jobjTemp.put("width", 150);
                                jobjTemp.put("pdfwidth", 150);
                                jobjTemp.put("custom", "true");
                                jarrColumns.put(jobjTemp);

                                customFieldList.add(customizeReportMapping.getDataIndex());

                            }
                            jObj.put(customizeReportMapping.getDataIndex(), customFieldMapValues);
                        } else {

                            if (!customFieldList.contains(customizeReportMapping.getDataIndex())) {
                                jobjTemp = new JSONObject();
                                jobjTemp.put("name", customizeReportMapping.getDataIndex());
                                jarrRecords.put(jobjTemp);

                                jobjTemp = new JSONObject();
                                jobjTemp.put("header", customizeReportMapping.getDataHeader());
                                jobjTemp.put("dataIndex", customizeReportMapping.getDataIndex());
                                jobjTemp.put("width", 150);
                                jobjTemp.put("pdfwidth", 150);
                                jobjTemp.put("custom", "true");
                                jarrColumns.put(jobjTemp);
                                customFieldList.add(customizeReportMapping.getDataIndex());
                            }

                        }

                    }
                    jArr.put(jObj);
                }
            }

            int count = jArr.length();
            JSONArray pagedJson = jArr;
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }

            //***********ColumnModel and rec******************************* 
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "currencysymbol");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "currencyid");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "currencyname");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "personname");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "Vendor Name");
            jobjTemp.put("dataIndex", "personname");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jobjTemp.put("align", "left");
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "currencyname");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "Currency");
            jobjTemp.put("dataIndex", "currencyname");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jobjTemp.put("align", "right");
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "amountdue1");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "Current");
            jobjTemp.put("dataIndex", "amountdue1");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jobjTemp.put("align", "right");
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "amountdue2");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", 1 + "-" + duration + " days");
            jobjTemp.put("dataIndex", "amountdue2");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jobjTemp.put("align", "right");
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "amountdue3");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", duration + 1 + "-" + duration * 2 + " days");
            jobjTemp.put("dataIndex", "amountdue3");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jobjTemp.put("align", "right");
            jarrColumns.put(jobjTemp);

            if (isdistibutive) {
                jobjTemp = new JSONObject();
                jobjTemp.put("name", "amountdue4");
                jarrRecords.put(jobjTemp);

                jobjTemp = new JSONObject();
                jobjTemp.put("header", duration * 2 + 1 + "-" + duration * 3 + " days");
                jobjTemp.put("dataIndex", "amountdue4");
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jobjTemp.put("sortable", true);
                jobjTemp.put("align", "right");
                jarrColumns.put(jobjTemp);

                jobjTemp = new JSONObject();
                jobjTemp.put("name", "amountdue5");
                jarrRecords.put(jobjTemp);

                jobjTemp = new JSONObject();
                jobjTemp.put("header", duration * 3 + 1 + "-" + duration * 4 + " days");
                jobjTemp.put("dataIndex", "amountdue5");
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jobjTemp.put("sortable", true);
                jobjTemp.put("align", "right");
                jarrColumns.put(jobjTemp);

                jobjTemp = new JSONObject();
                jobjTemp.put("name", "amountdue6");
                jarrRecords.put(jobjTemp);

                jobjTemp = new JSONObject();
                jobjTemp.put("header", duration * 4 + 1 + "-" + duration * 5 + " days");
                jobjTemp.put("dataIndex", "amountdue6");
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jobjTemp.put("sortable", true);
                jobjTemp.put("align", "right");
                jarrColumns.put(jobjTemp);

                jobjTemp = new JSONObject();
                jobjTemp.put("name", "amountdue7");
                jarrRecords.put(jobjTemp);

                jobjTemp = new JSONObject();
                jobjTemp.put("header", duration * 5 + 1 + "-" + duration * 6 + " days");
                jobjTemp.put("dataIndex", "amountdue7");
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jobjTemp.put("sortable", true);
                jobjTemp.put("align", "right");
                jarrColumns.put(jobjTemp);

                jobjTemp = new JSONObject();
                jobjTemp.put("name", "amountdue8");
                jarrRecords.put(jobjTemp);

                jobjTemp = new JSONObject();
                jobjTemp.put("header", ">" + duration * 6 + " days");
                jobjTemp.put("dataIndex", "amountdue8");
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jobjTemp.put("sortable", true);
                jobjTemp.put("align", "right");
                jarrColumns.put(jobjTemp);
            } else {
                jobjTemp = new JSONObject();
                jobjTemp.put("name", "amountdue4");
                jarrRecords.put(jobjTemp);

                jobjTemp = new JSONObject();
                jobjTemp.put("header", ">" + duration * 3 + " days");
                jobjTemp.put("dataIndex", "amountdue4");
                jobjTemp.put("width", 150);
                jobjTemp.put("pdfwidth", 150);
                jobjTemp.put("sortable", true);
                jobjTemp.put("align", "right");
                jarrColumns.put(jobjTemp);
            }

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "total");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "Total");
            jobjTemp.put("dataIndex", "total");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jobjTemp.put("align", "right");
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "totalinbase");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "Total in Base Currency(" + baseCurrencyName + ")");
            jobjTemp.put("dataIndex", "totalinbase");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jobjTemp.put("align", "right");
            jarrColumns.put(jobjTemp);

           //***********ColumnModel and rec******************************* 
            //***********final jobj***************************************
            commData.put("success", true);
            commData.put("coldata", pagedJson);
            commData.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            commData.put("totalCount", jArr.length());
            jMeta.put("fields", jarrRecords);
            commData.put("metaData", jMeta);
            JSONArray jcom = new JSONArray();
            jcom.put(commData);
            jobj.put("valid", true);

            if (exportAgedPayables) {
                jobj.put("data", pagedJson);
            } else {
                jobj.put("data", commData);
            }
            //***********final jobj***************************************
            return jobj;
        } catch (JSONException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getVendorAgedPayablebasedonDimensions : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getVendorAgedPayablebasedonDimensions : " + ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getVendorAgedPayablebasedonDimensions : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(AccGoodsReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getVendorAgedPayablebasedonDimensions : " + ex.getMessage(), ex);
        }
    }


    @Override
    public JSONObject getVendorAgedPayableDetailedbasedonDimensions(HttpServletRequest request, HashMap<String, Object> requestParams) throws ServiceException {
        JSONObject jobj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
//            String curDateString = (!StringUtil.isNullOrEmpty(request.getParameter(GoodsReceiptCMNConstants.CURDATE))) ? request.getParameter(GoodsReceiptCMNConstants.CURDATE) : (request.getParameter("stdate") != null ? request.getParameter("stdate") : request.getParameter("startdate"));
            String curDateString = !(StringUtil.isNullOrEmpty(request.getParameter(Constants.asOfDate))) ? request.getParameter(Constants.asOfDate) : request.getParameter(Constants.curdate);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            Date curDate = df.parse(curDateString);
            int invoiceCount = 0, jsonCount = 0, openingCount = 0, openingJSONCount = 0;
            int duration = request.getParameter(GoodsReceiptCMNConstants.DURATION) == null ? 0 : Integer.parseInt(request.getParameter(GoodsReceiptCMNConstants.DURATION));
            int noOfInterval = Constants.DefaultNoOfIntervals;
            if(requestParams.containsKey("noOfInterval") && requestParams.get("noOfInterval") != null) {
               noOfInterval = requestParams.get("noOfInterval").toString().equals("") ? Constants.DefaultNoOfIntervals : Integer.parseInt(requestParams.get("noOfInterval").toString());
            }
            boolean isdistibutive = StringUtil.getBoolean(request.getParameter(GoodsReceiptCMNConstants.ISDISTRIBUTIVE));
            int datefilter = StringUtil.getInteger(request.getParameter("datefilter"));// 0 = Invoice Due date OR 1 = Invoice date

            JSONObject commData = new JSONObject();
            JSONObject jMeta = new JSONObject();
            JSONArray jarrColumns = new JSONArray();
            JSONArray jarrRecords = new JSONArray();
            JSONObject jobjTemp = new JSONObject();

            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), sessionHandlerImpl.getCompanyid(request));
            Company company = (Company) cmpresult.getEntityList().get(0);
            String baseCurrencyName = "";
            if (company != null && company.getCurrency() != null) {
                baseCurrencyName = company.getCurrency().getName();
            }
            int reportId = 817;

            String customizeHeader[] = request.getParameterValues("arr");
            HashMap hashMap=new HashMap();
            hashMap.put("companyId", company.getCompanyID());
            hashMap.put("reportId", reportId);
            KwlReturnObject customizeReportResult = accountingHandlerDAOobj.getCustomizeReportViewMappingField(hashMap);
            List<CustomizeReportMapping> customizeReportList = customizeReportResult.getEntityList();
            List customFieldList = new ArrayList();

            double amountdue1 = 0;
            double amountdue2 = 0;
            double amountdue3 = 0;
            double amountdue4 = 0;
            double amountdue5 = 0;
            double amountdue6 = 0;
            double amountdue7 = 0;
            double amountdue8 = 0;
            double amountdue9 = 0;
            double amountdue10 = 0;
            double amountdue11 = 0;


            double totalinbase = 0;
            Calendar oneDayBeforeCal1 = Calendar.getInstance();
            Calendar cal1 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            Calendar cal8 = Calendar.getInstance();
            Calendar cal9 = Calendar.getInstance();
            Calendar cal10 = Calendar.getInstance();
            oneDayBeforeCal1.setTime(curDate);
            cal1.setTime(curDate);
            cal2.setTime(curDate);
            cal3.setTime(curDate);
            cal4.setTime(curDate);
            cal5.setTime(curDate);
            cal6.setTime(curDate);
            cal7.setTime(curDate);
            cal8.setTime(curDate);
            cal9.setTime(curDate);
            cal10.setTime(curDate);
            oneDayBeforeCal1.add(Calendar.DAY_OF_YEAR, -1);
            cal2.add(Calendar.DAY_OF_YEAR, -duration);
            cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
            cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
            cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
            cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
            cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
            cal8.add(Calendar.DAY_OF_YEAR, -(duration * 7));
            cal9.add(Calendar.DAY_OF_YEAR, -(duration * 8));
            cal10.add(Calendar.DAY_OF_YEAR, -(duration * 9));
            
            
            Date oneDayBeforeCal1Date = null;
            Date cal1Date = null;
            Date cal2Date = null;
            Date cal3Date = null;
            Date cal4Date = null;
            Date cal5Date = null;
            Date cal6Date = null;
            Date cal7Date = null;
            Date cal8Date = null;
            Date cal9Date = null;
            Date cal10Date = null;

            String oneDayBeforeCal1String = authHandler.getDateOnlyFormat().format(oneDayBeforeCal1.getTime());
            oneDayBeforeCal1Date = authHandler.getDateOnlyFormat().parse(oneDayBeforeCal1String);

            String cal1String = authHandler.getDateOnlyFormat().format(cal1.getTime());
            cal1Date = authHandler.getDateOnlyFormat().parse(cal1String);

            String cal2String = authHandler.getDateOnlyFormat().format(cal2.getTime());
            cal2Date = authHandler.getDateOnlyFormat().parse(cal2String);

            String cal3String = authHandler.getDateOnlyFormat().format(cal3.getTime());
            cal3Date = authHandler.getDateOnlyFormat().parse(cal3String);

            String cal4String = authHandler.getDateOnlyFormat().format(cal4.getTime());
            cal4Date = authHandler.getDateOnlyFormat().parse(cal4String);

            String cal5String = authHandler.getDateOnlyFormat().format(cal5.getTime());
            cal5Date = authHandler.getDateOnlyFormat().parse(cal5String);

            String cal6String = authHandler.getDateOnlyFormat().format(cal6.getTime());
            cal6Date = authHandler.getDateOnlyFormat().parse(cal6String);

            String cal7String = authHandler.getDateOnlyFormat().format(cal7.getTime());
            cal7Date = authHandler.getDateOnlyFormat().parse(cal7String);

            String cal8String = authHandler.getDateOnlyFormat().format(cal8.getTime());
            cal8Date = authHandler.getDateOnlyFormat().parse(cal8String);

            String cal9String = authHandler.getDateOnlyFormat().format(cal9.getTime());
            cal9Date = authHandler.getDateOnlyFormat().parse(cal9String);

            String cal10String = authHandler.getDateOnlyFormat().format(cal10.getTime());
            cal10Date = authHandler.getDateOnlyFormat().parse(cal10String);

            Map<String, String> amountDueMap = new HashMap<String, String>();
            boolean exportAgedPayables = false;
            if (requestParams.containsKey("exportAgedReceivables") && requestParams.get("exportAgedReceivables") != null) {
                exportAgedPayables = Boolean.parseBoolean(requestParams.get("exportAgedReceivables").toString());
            }
            boolean isAgedPayables = false;
            if (requestParams.containsKey("isAgedPayables") && requestParams.get("isAgedPayables") != null) {
                isAgedPayables = Boolean.parseBoolean(requestParams.get("isAgedPayables").toString());
            }

            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Object> fieldrequestParams = new HashMap();
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(sessionHandlerImpl.getCompanyid(request), Constants.Acc_Vendor_Invoice_ModuleId));
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            KwlReturnObject result = null;
            if (requestParams.containsKey("start")) { //Removing paging here because it is applying on each vendor data (CN/DN etc). Due to this complete amount due not showing in AP Summary.  
                requestParams.remove("start");
            }
            if (requestParams.containsKey("limit")) {
                requestParams.remove("limit");
            }
            requestParams.put("oneDayBeforeCal1", oneDayBeforeCal1);
            requestParams.put("cal1", cal1);
            requestParams.put("cal2", cal2);
            requestParams.put("cal3", cal3);
            requestParams.put("cal4", cal4);
            requestParams.put("cal5", cal5);
            requestParams.put("cal6", cal6);
            requestParams.put("cal7", cal7);
            requestParams.put("cal8", cal8);
            requestParams.put("cal9", cal9);
            requestParams.put("cal10", cal10);
            if (isAgedPayables) {
                requestParams.put("isAgedPayables", isAgedPayables);
            }
            requestParams.put("cntype", null);
            requestParams.put("isAgedSummary", true);
//
            JSONArray invjarr = new JSONArray();
            result = accGoodsReceiptobj.getGoodsReceiptsMerged(requestParams);
            if (result != null && !result.getEntityList().isEmpty()) {
                invjarr = accGoodsReceiptServiceHandler.getGoodsReceiptsJsonForAgedPayables(requestParams, result.getEntityList(), invjarr, accountingHandlerDAOobj, accCurrencyDAOobj, accGoodsReceiptobj, accAccountDAOobj, accGoodsReceiptCommon, accTaxObj);
            }
            JSONArray OBJArryInvoice = new JSONArray();
            result = accGoodsReceiptobj.getOpeningBalanceInvoices(requestParams);
            openingCount += result.getEntityList().size();
            if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                OBJArryInvoice = AccGoodsReceiptServiceHandler.getAgedPayablesOpeningBalanceInvoiceJson(requestParams, result.getEntityList(), OBJArryInvoice, accCurrencyDAOobj, accountingHandlerDAOobj, accGoodsReceiptCommon);
                openingJSONCount += OBJArryInvoice.length();
                for (int i = 0; i < OBJArryInvoice.length(); i++) {
                    invjarr.put(OBJArryInvoice.get(i));
                }
            }

            if (invjarr.length() > 0) {
                for (int i = 0; i < invjarr.length(); i++) {
                    amountdue1 = amountdue2 = amountdue3 = amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                    JSONObject custJobj = new JSONObject();
                    JSONObject invobj = invjarr.getJSONObject(i);

                    KwlReturnObject custumObjresult1 = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), (invobj.has("billid")) ? invobj.getString("billid") : "");
                    GoodsReceipt inv = (GoodsReceipt) custumObjresult1.getEntityList().get(0);
                    String journalentryid = inv.getJournalEntry() != null ? inv.getJournalEntry().getID() : "";
                    String companyid = inv.getCompany() != null ? inv.getCompany().getCompanyID() : "";
                    Date invoiceDate = authHandler.getDateOnlyFormat(request).parse(invobj.getString("date"));

                    Date dueDate = new Date();
                    if (datefilter == 0 || datefilter == Constants.agedDueDate0to30Filter) {
                        dueDate = authHandler.getDateOnlyFormat(request).parse(invobj.getString("duedate"));
                    } else {
                        dueDate = authHandler.getDateOnlyFormat(request).parse(invobj.getString("date"));
                    }
                    double amountdue = invobj.getDouble("amountdue");
                    double amountdueinbase = invobj.getDouble("amountdueinbase");
                    Date date = null;
                    if (!invobj.getString("date").equals("")) {
                        date = df.parse(invobj.getString("date"));
                    }
                    if (dueDate.after(oneDayBeforeCal1Date)) {
                        if (dueDate.equals(cal1Date) && (datefilter == Constants.agedDueDate0to30Filter || datefilter == Constants.agedInvoiceDate0to30Filter)) {
                            amountdue2 = authHandler.round(amountdue, companyid);  // 0-30 Days
                        } else {
                            amountdue1 = authHandler.round(amountdue, companyid); // Current
                        }
                    } else if ((cal2Date.before(dueDate) || cal2Date.equals(dueDate)) && cal1Date.after(dueDate)) { // 1-30
                        amountdue2 = authHandler.round(amountdue, companyid);
                    } else if ((cal3Date.before(dueDate) || cal3Date.equals(dueDate)) && cal2Date.after(dueDate)) { // 31-60
                        amountdue3 = authHandler.round(amountdue, companyid);
                    } else if ((cal4Date.before(dueDate) || cal4Date.equals(dueDate)) && cal3Date.after(dueDate)) { // 61-90
                        amountdue4 = authHandler.round(amountdue, companyid);
                    } else if ((cal5Date.before(dueDate) || cal5Date.equals(dueDate)) && cal4Date.after(dueDate)) { // 91-120
                        amountdue5 = authHandler.round(amountdue, companyid);
                    } else if ((cal6Date.before(dueDate) || cal6Date.equals(dueDate)) && cal5Date.after(dueDate)) { // 121-150
                        amountdue6 = authHandler.round(amountdue, companyid);
                    } else if ((cal7Date.before(dueDate) || cal7Date.equals(dueDate)) && cal6Date.after(dueDate)) { // 151-180
                        amountdue7 = authHandler.round(amountdue, companyid);
                    } else if ((cal8Date.before(dueDate) || cal8Date.equals(dueDate)) && cal7Date.after(dueDate)) { // 181-210
                        amountdue8 = authHandler.round(amountdue, companyid);
                    } else if ((cal9Date.before(dueDate) || cal9Date.equals(dueDate)) && cal8Date.after(dueDate)) { // 211-240
                        amountdue9 = authHandler.round(amountdue, companyid);
                    } else if ((cal10Date.before(dueDate) || cal10Date.equals(dueDate)) && cal9Date.after(dueDate)) { // 241-270
                        amountdue10 = authHandler.round(amountdue, companyid);
                    } else {                                                                                          // > 270           
                        amountdue11 = authHandler.round(amountdue, companyid);
                    }
                    
                    
                    switch(noOfInterval){
                        case 2:
                            amountdue3 += amountdue4 + amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                            amountdue4 = amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                            break;
                        case 3:
                            amountdue4 += amountdue5 + amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                            amountdue5 = amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                            break;
                        case 4:
                            amountdue5 += amountdue6 + amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                            amountdue6 = amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                            break;
                        case 5:
                            amountdue6 += amountdue7 + amountdue8 + amountdue9 + amountdue10 + amountdue11;
                            amountdue7 = amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                            break;
                        case 6:
                            amountdue7 += amountdue8 + amountdue9 + amountdue10 + amountdue11;
                            amountdue8 = amountdue9 = amountdue10 = amountdue11 = 0;
                            break;
                        case 7:
                            amountdue8 += amountdue9 + amountdue10 + amountdue11;
                            amountdue9 = amountdue10 = amountdue11 = 0;
                            break;
                        case 8:
                            amountdue9 += amountdue10 + amountdue11;
                            amountdue10 = amountdue11 = 0;
                            break;
                        case 9:
                            amountdue10 += amountdue11;
                            amountdue11 = 0;
                            break;
                    }

                    
                    JSONObject jObj = new JSONObject();
                    jObj.put("amountdueinbase", Double.valueOf(authHandler.formattedAmount(amountdueinbase, companyid)));
                    jObj.put("amountdue", Double.valueOf(authHandler.formattedAmount(amountdue, companyid)));
                    jObj.put("amountdue1", Double.valueOf(authHandler.formattedAmount(amountdue1, companyid)));
                    jObj.put("amountdue2", Double.valueOf(authHandler.formattedAmount(amountdue2, companyid)));
                    jObj.put("amountdue3", Double.valueOf(authHandler.formattedAmount(amountdue3, companyid)));
                    jObj.put("amountdue4", Double.valueOf(authHandler.formattedAmount(amountdue4, companyid)));
                    jObj.put("amountdue5", Double.valueOf(authHandler.formattedAmount(amountdue5, companyid)));
                    jObj.put("amountdue6", Double.valueOf(authHandler.formattedAmount(amountdue6, companyid)));
                    jObj.put("amountdue7", Double.valueOf(authHandler.formattedAmount(amountdue7, companyid)));
                    jObj.put("amountdue8", Double.valueOf(authHandler.formattedAmount(amountdue8, companyid)));
                    jObj.put("amountdue9", Double.valueOf(authHandler.formattedAmount(amountdue9, companyid)));
                    jObj.put("amountdue10", Double.valueOf(authHandler.formattedAmount(amountdue10, companyid)));
                    jObj.put("amountdue11", Double.valueOf(authHandler.formattedAmount(amountdue11, companyid)));
                    jObj.put("currencysymbol", (invobj.has("currencysymbol")) ? invobj.getString("currencysymbol") : "");
                    jObj.put("currencyid", (invobj.has("currencyid")) ? invobj.getString("currencyid") : "");
                    jObj.put("currencyname", (invobj.has("currencyname")) ? invobj.getString("currencyname") : "");

                    jObj.put("billNo", (invobj.has("billno")) ? invobj.getString("billno") : "");
                    jObj.put("personname", (invobj.has("personname")) ? invobj.getString("personname") : "");
                    jObj.put("billDate", authHandler.getUserDateFormatterWithoutTimeZone(request).format(invoiceDate));
                    jObj.put("memo", (invobj.has("memo")) ? invobj.getString("memo") : "");

                    String customFieldMapValues = "";
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    HashMap<String, Object> invDetailRequestParams = new HashMap<String, Object>();
                    ArrayList Detailfilter_names = new ArrayList(), Detailfilter_params = new ArrayList();
                    Detailfilter_names.add("companyid");
                    Detailfilter_params.add(companyid);
                    Detailfilter_names.add("journalentryId");
                    Detailfilter_params.add(journalentryid);
                    Detailfilter_names.add("moduleId");
                    Detailfilter_params.add(Constants.Acc_Vendor_Invoice_ModuleId + "");
                    invDetailRequestParams.put("filter_names", Detailfilter_names);
                    invDetailRequestParams.put("filter_params", Detailfilter_params);
                    KwlReturnObject idcustresult = accJournalEntryobj.getJournalEntryCustomData(invDetailRequestParams);
                    if (idcustresult.getEntityList().size() > 0) {
                        AccJECustomData jeCustom = (AccJECustomData) idcustresult.getEntityList().get(0);
                        AccountingManager.setCustomColumnValues(jeCustom, FieldMap, replaceFieldMap, variableMap);
                        JSONObject params = new JSONObject();
                        params.put("companyid", companyid);
                        params.put("isExport", true);
                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, custJobj, params);
                    }

                    for (CustomizeReportMapping customizeReportMapping : customizeReportList) {
                        String column = "Custom_" + customizeReportMapping.getDataIndex();

                        if (custJobj.has(column)) {
                            customFieldMapValues = custJobj.getString(column);
                            if (!customFieldList.contains(customizeReportMapping.getDataIndex())) {
//
                                jobjTemp = new JSONObject();
                                jobjTemp.put("name", customizeReportMapping.getDataIndex());
                                jarrRecords.put(jobjTemp);

                                jobjTemp = new JSONObject();
                                jobjTemp.put("header", customizeReportMapping.getDataHeader());
                                jobjTemp.put("dataIndex", customizeReportMapping.getDataIndex());
                                jobjTemp.put("width", 150);
                                jobjTemp.put("pdfwidth", 150);
                                jobjTemp.put("custom", "true");
                                jarrColumns.put(jobjTemp);

                                customFieldList.add(customizeReportMapping.getDataIndex());

                            }
                            jObj.put(customizeReportMapping.getDataIndex(), customFieldMapValues);
                        } else {

                            if (!customFieldList.contains(customizeReportMapping.getDataIndex())) {
                                jobjTemp = new JSONObject();
                                jobjTemp.put("name", customizeReportMapping.getDataIndex());
                                jarrRecords.put(jobjTemp);

                                jobjTemp = new JSONObject();
                                jobjTemp.put("header", customizeReportMapping.getDataHeader());
                                jobjTemp.put("dataIndex", customizeReportMapping.getDataIndex());
                                jobjTemp.put("width", 150);
                                jobjTemp.put("pdfwidth", 150);
                                jobjTemp.put("custom", "true");
                                jarrColumns.put(jobjTemp);
                                customFieldList.add(customizeReportMapping.getDataIndex());
                            }

                        }

                    }
                    jArr.put(jObj);
                }
            }

            int count = jArr.length();
            JSONArray pagedJson = jArr;
            String start = request.getParameter("start");
            String limit = request.getParameter("limit");
            if (!StringUtil.isNullOrEmpty(start) && !StringUtil.isNullOrEmpty(limit)) {
                pagedJson = StringUtil.getPagedJSON(pagedJson, Integer.parseInt(start), Integer.parseInt(limit));
            }

            //***********ColumnModel and rec******************************* 
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "currencysymbol");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "currencyid");
            jarrRecords.put(jobjTemp);
            jobjTemp = new JSONObject();
            jobjTemp.put("name", "currencyname");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "billNo");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "Invoice No");
            jobjTemp.put("dataIndex", "billNo");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jobjTemp.put("align", "center");
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "billDate");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "Invoice Date");
            jobjTemp.put("dataIndex", "billDate");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jobjTemp.put("align", "center");
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "personname");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "Vendor Name");
            jobjTemp.put("dataIndex", "personname");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jobjTemp.put("align", "left");
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "memo");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "Remark");
            jobjTemp.put("dataIndex", "memo");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jobjTemp.put("align", "left");
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "currencyname");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "Currency");
            jobjTemp.put("dataIndex", "currencyname");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jobjTemp.put("align", "right");
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "amountdue");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "Amount Due");
            jobjTemp.put("dataIndex", "amountdue");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jobjTemp.put("align", "right");
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "amountdue2");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", 1 + "-" + duration + " days");
            jobjTemp.put("dataIndex", "amountdue2");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jobjTemp.put("align", "right");
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "amountdue3");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", duration + 1 + "-" + duration * 2 + " days");
            jobjTemp.put("dataIndex", "amountdue3");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jobjTemp.put("align", "right");
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "amountdue4");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", duration * 2 + 1 + "-" + duration * 3 + " days");
            jobjTemp.put("dataIndex", "amountdue4");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jobjTemp.put("align", "right");
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "amountdue5");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", duration * 3 + 1 + "-" + duration * 4 + " days");
            jobjTemp.put("dataIndex", "amountdue5");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jobjTemp.put("align", "right");
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "amountdue6");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", duration * 4 + 1 + "-" + duration * 5 + " days");
            jobjTemp.put("dataIndex", "amountdue6");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jobjTemp.put("align", "right");
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "amountdue7");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", duration * 5 + 1 + "-" + duration * 6 + " days");
            jobjTemp.put("dataIndex", "amountdue7");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jobjTemp.put("align", "right");
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "amountdue8");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", ">" + duration * 6 + " days");
            jobjTemp.put("dataIndex", "amountdue8");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jobjTemp.put("align", "right");
            jarrColumns.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("name", "amountdueinbase");
            jarrRecords.put(jobjTemp);

            jobjTemp = new JSONObject();
            jobjTemp.put("header", "Total in Base Currency(" + baseCurrencyName + ")");
            jobjTemp.put("dataIndex", "amountdueinbase");
            jobjTemp.put("width", 150);
            jobjTemp.put("pdfwidth", 150);
            jobjTemp.put("sortable", true);
            jobjTemp.put("align", "right");
            jarrColumns.put(jobjTemp);

            //***********ColumnModel and rec******************************* 
            //***********final jobj***************************************
            commData.put("success", true);
            commData.put("coldata", pagedJson);
            commData.put("columns", jarrColumns);
            jMeta.put("totalProperty", "totalCount");
            jMeta.put("root", "coldata");
            commData.put("totalCount", jArr.length());
            jMeta.put("fields", jarrRecords);
            commData.put("metaData", jMeta);
            JSONArray jcom = new JSONArray();
            jcom.put(commData);
            jobj.put("valid", true);

            if (exportAgedPayables) {
                jobj.put("data", pagedJson);
            } else {
                jobj.put("data", commData);
            }
            //***********final jobj***************************************
            return jobj;
        } catch (JSONException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getVendorAgedPayableDetailedbasedonDimensions : " + ex.getMessage(), ex);
        } catch (ParseException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getVendorAgedPayableDetailedbasedonDimensions : " + ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getVendorAgedPayableDetailedbasedonDimensions : " + ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(AccGoodsReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getVendorAgedPayableDetailedbasedonDimensions : " + ex.getMessage(), ex);
        }
    }

    @Override
    public JSONArray getBadDebtClaimedInvoicesJson(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            HashMap<String, Object> badMaps = new HashMap<String, Object>();
            badMaps.put("companyid", companyid);
            badMaps.put("badDebtType", 0);
            badMaps.put("DateFormat", df);
            badMaps.put(Constants.ss, request.getParameter(Constants.ss));

            if (!StringUtil.isNullOrEmpty(request.getParameter("startdate")) && !StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                Date startDate = df.parse(request.getParameter("startdate"));
                Date endDate = df.parse(request.getParameter("enddate"));

                badMaps.put("claimedFromDate", startDate);
                badMaps.put("claimedToDate", endDate);
            }


            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.start)) && !StringUtil.isNullOrEmpty(request.getParameter(Constants.limit))) {
                badMaps.put(Constants.start, request.getParameter(Constants.start));
                badMaps.put(Constants.limit, request.getParameter(Constants.limit));
            }
            
            String searchJson = request.getParameter(Constants.Acc_Search_Json);
            String filterConjuctionCriteria = request.getParameter(Constants.Filter_Criteria);

            if (!StringUtil.isNullOrEmpty(searchJson) && !StringUtil.isNullOrEmpty(filterConjuctionCriteria)) {
                badMaps.put(Constants.Acc_Search_Json, searchJson);
                badMaps.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);
                badMaps.put(Constants.Filter_Criteria, filterConjuctionCriteria);
                badMaps.put(Constants.Acc_Search_Json, accReportsService.getSearchJsonByModule(badMaps));
            }

            jArr = getBadDebtInvoices(badMaps);

        } catch (JSONException | ServiceException | ParseException |UnsupportedEncodingException ex) {
            Logger.getLogger(AccGoodsReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getBadDebtClaimedInvoicesJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    @Override
    public JSONArray getBadDebtInvoices(HashMap<String, Object> badMaps) throws SessionExpiredException, ServiceException, JSONException {
        JSONArray jArr = new JSONArray();

        DateFormat df = (DateFormat) badMaps.get("DateFormat");
        int badDebtType = (Integer) badMaps.get("badDebtType");

        KwlReturnObject result = accGoodsReceiptobj.getBadDebtPurchaseInvoiceMappingForGoodsReceipt(badMaps);

        List list = result.getEntityList();
        if (!list.isEmpty()) {
            Iterator it = list.iterator();
            boolean isOpeningBalanceInvoice=false;
            while (it.hasNext()) {
                BadDebtPurchaseInvoiceMapping invoiceMapping = (BadDebtPurchaseInvoiceMapping) it.next();
                JSONObject obj = new JSONObject();
                obj.put("invoiceNumber", (invoiceMapping.getGoodsReceipt() != null) ? invoiceMapping.getGoodsReceipt().getGoodsReceiptNumber() : "");
                obj.put("invoiceid", (invoiceMapping.getGoodsReceipt() != null) ? invoiceMapping.getGoodsReceipt().getID() : "");
                obj.put("billid", (invoiceMapping.getGoodsReceipt() != null) ? invoiceMapping.getGoodsReceipt().getID() : "");
                obj.put("type", Constants.VENDOR_INVOICE);
                obj.put("transactionid", invoiceMapping.getBadDebtSeqNumber());//ERP-28376
                obj.put("journalentryid", (invoiceMapping.getJournalEntry() != null) ? invoiceMapping.getJournalEntry().getID() : "");
                obj.put("entryno", (invoiceMapping.getJournalEntry() != null) ? invoiceMapping.getJournalEntry().getEntryNumber() : "");
                if(invoiceMapping.getGoodsReceipt()!=null){
                     invoiceMapping.getGoodsReceipt().getGoodsReceiptNumber();
                     isOpeningBalanceInvoice = invoiceMapping.getGoodsReceipt().isIsOpeningBalenceInvoice();
                     obj.put(InvoiceConstants.currencysymbol, isOpeningBalanceInvoice ?invoiceMapping.getGoodsReceipt().getCurrency().getSymbol() :invoiceMapping.getGoodsReceipt().getJournalEntry().getCurrency().getSymbol());
                     obj.put(Constants.currencyKey, (invoiceMapping.getGoodsReceipt() != null) ? invoiceMapping.getGoodsReceipt().getCurrency().getCurrencyID():invoiceMapping.getGoodsReceipt().getJournalEntry().getCurrency().getCurrencyID());
                     obj.put("currencycode", (invoiceMapping.getGoodsReceipt() != null) ?invoiceMapping.getGoodsReceipt().getCurrency().getCurrencyCode() :invoiceMapping.getGoodsReceipt().getCurrency().getCurrencyCode());
                }
                if (badDebtType == 0) {// Claim
                    obj.put("date", (invoiceMapping.getBadDebtClaimedDate() != null) ? df.format(invoiceMapping.getBadDebtClaimedDate()) : "");
                    obj.put("amount", invoiceMapping.getBadDebtAmtClaimed());
                    obj.put("taxAmount", invoiceMapping.getBadDebtGSTAmtClaimed());
                    /*
                     * isPartiallyRecovered is used for identifying the invoices which are paid partially or fully after the claiming 
                     */
                    if(invoiceMapping.getBadDebtAmtClaimed() != invoiceMapping.getGoodsReceipt().getClaimAmountDue()){
                        obj.put("isPartiallyRecovered", true);
                    }
                } else {
                    obj.put("date", (invoiceMapping.getBadDebtRecoveredDate() != null) ? df.format(invoiceMapping.getBadDebtRecoveredDate()) : "");
                    obj.put("amount", invoiceMapping.getBadDebtAmtRecovered());
                    obj.put("taxAmount", invoiceMapping.getBadAmtDebtGSTAmtRecovered());
                }
                if(!StringUtil.isNullOrEmpty(invoiceMapping.getPaymentId())){
                    KwlReturnObject objresult = accountingHandlerDAOobj.getObject(Payment.class.getName(), invoiceMapping.getPaymentId());
                    Payment payment = (Payment) objresult.getEntityList().get(0);
                    obj.put("paymentno", payment.getPaymentNumber());
                    obj.put("paymentid", payment.getID());
                }
                jArr.put(obj);
            }
        }
        return jArr;
    }
    
    @Override
    public JSONArray getRecoveredBadDebtInvoices(HttpServletRequest request) throws SessionExpiredException, ServiceException {
        JSONArray jArr = new JSONArray();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            DateFormat df = authHandler.getDateOnlyFormat(request);
            HashMap<String, Object> badMaps = new HashMap<String, Object>();
            badMaps.put("companyid", companyid);
            badMaps.put("badDebtType", 1);
            badMaps.put("DateFormat", df);
            badMaps.put(Constants.ss, request.getParameter(Constants.ss));

            if (!StringUtil.isNullOrEmpty(request.getParameter("startdate")) && !StringUtil.isNullOrEmpty(request.getParameter("enddate"))) {
                Date startDate = df.parse(request.getParameter("startdate"));
                Date endDate = df.parse(request.getParameter("enddate"));

                badMaps.put("recoveredFromDate", startDate);
                badMaps.put("recoveredToDate", endDate);
            }
            
            String searchJson = request.getParameter(Constants.Acc_Search_Json);
            String filterConjuctionCriteria = request.getParameter(Constants.Filter_Criteria);
            if (!StringUtil.isNullOrEmpty(searchJson) && !StringUtil.isNullOrEmpty(filterConjuctionCriteria)) {
                badMaps.put(Constants.Acc_Search_Json, searchJson);
                badMaps.put(Constants.Filter_Criteria, filterConjuctionCriteria);
                badMaps.put(Constants.moduleid, Constants.Acc_Vendor_Invoice_ModuleId);
                badMaps.put(Constants.Acc_Search_Json, accReportsService.getSearchJsonByModule(badMaps));
            }
            
            if (!StringUtil.isNullOrEmpty(request.getParameter(Constants.start)) && !StringUtil.isNullOrEmpty(request.getParameter(Constants.limit))) {
                badMaps.put(Constants.start, request.getParameter(Constants.start));
                badMaps.put(Constants.limit, request.getParameter(Constants.limit));
            }

            jArr = getBadDebtInvoices(badMaps);

        } catch (SessionExpiredException | ParseException | UnsupportedEncodingException | ServiceException | JSONException ex) {
            Logger.getLogger(AccGoodsReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getBadDebtClaimedInvoicesJson : " + ex.getMessage(), ex);
        }
        return jArr;
    }
    
    /**
    * This Method Uses HttpServletRequest and this method
    * was called from service layer forcing to send request
    * object to Service Layer
    * @deprecated 
    */
    @Override
    public JSONArray getMonthlyVendorAgedPayableMerged(HttpServletRequest request) throws ServiceException, JSONException, SessionExpiredException, ParseException {
        JSONObject jObj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            HashMap<String, Object> invoiceRequestParams = new HashMap<String, Object>();
            String fileType = request.getParameter("filetype");
            invoiceRequestParams.put(GoodsReceiptCMNConstants.COMPANYID, sessionHandlerImpl.getCompanyid(request));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.GCURRENCYID, sessionHandlerImpl.getCurrencyID(request));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.DATEFORMAT, authHandler.getDateOnlyFormat(request));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.START, request.getParameter(GoodsReceiptCMNConstants.START));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.LIMIT, request.getParameter(GoodsReceiptCMNConstants.LIMIT));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.SS, request.getParameter(GoodsReceiptCMNConstants.SS));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.ACCID, request.getParameter(GoodsReceiptCMNConstants.ACCID));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.CASHONLY, request.getParameter(GoodsReceiptCMNConstants.CASHONLY));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.CREDITONLY, request.getParameter(GoodsReceiptCMNConstants.CREDITONLY));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.IGNOREZERO, request.getParameter(GoodsReceiptCMNConstants.IGNOREZERO));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.CURDATE, request.getParameter(GoodsReceiptCMNConstants.CURDATE));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.PERSONGROUP, request.getParameter(GoodsReceiptCMNConstants.PERSONGROUP));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.ISAGEDGRAPH, request.getParameter(GoodsReceiptCMNConstants.ISAGEDGRAPH));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.VENDORID, request.getParameter(GoodsReceiptCMNConstants.VENDORID));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.NONDELETED, request.getParameter(GoodsReceiptCMNConstants.NONDELETED));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.DURATION, request.getParameter(GoodsReceiptCMNConstants.DURATION));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.ISDISTRIBUTIVE, request.getParameter(GoodsReceiptCMNConstants.ISDISTRIBUTIVE));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.WITHINVENTORY, request.getParameter(GoodsReceiptCMNConstants.WITHINVENTORY));
            invoiceRequestParams.put(Constants.df, authHandler.getDateOnlyFormat(request));
            invoiceRequestParams.put(Constants.REQ_startdate, request.getParameter(Constants.REQ_startdate));
            invoiceRequestParams.put(Constants.REQ_enddate, request.getParameter(Constants.REQ_enddate));
            invoiceRequestParams.put("pendingapproval", (request.getParameter("pendingapproval") != null) ? Boolean.parseBoolean(request.getParameter("pendingapproval")) : false);
            invoiceRequestParams.put("istemplate", (request.getParameter("istemplate") != null) ? Integer.parseInt(request.getParameter("istemplate")) : 0);
            invoiceRequestParams.put("datefilter", request.getParameter("datefilter"));
            invoiceRequestParams.put("custVendorID", request.getParameter("custVendorID"));
            invoiceRequestParams.put("asofdate", request.getParameter("asofdate"));
            invoiceRequestParams.put("isAged", request.getParameter("isAged"));
            DateFormat df = authHandler.getDateOnlyFormat(request);
            int datefilter = StringUtil.getInteger(request.getParameter("datefilter"));// 0 = Invoice Due date OR 1 = Invoice date
            boolean isSummary = false;
            boolean isCallFromDD = false;
            ExtraCompanyPreferences extraCompanyPreferences = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            
            if (request.getParameter("isSummary") != null) {
                isSummary = Boolean.parseBoolean(request.getParameter("isSummary"));
            } else if (request.getAttribute("isSummary") != null) {
                isSummary = Boolean.parseBoolean(request.getAttribute("isSummary").toString());
            }
            if (request.getAttribute("isCallFromDD") != null) {
                isCallFromDD = Boolean.parseBoolean(request.getAttribute("isCallFromDD").toString());
            }
            String transactionId = request.getAttribute("transactionId")==null? "" : request.getAttribute("transactionId").toString();
            if(isCallFromDD){
                invoiceRequestParams.put("custVendorID", transactionId);
            }
            if (isCallFromDD) {// Document designer 0 = Invoice Due date 
                datefilter = 1;
                invoiceRequestParams.put("datefilter", datefilter);
            }
            final DateTimeFormatter dtf = DateTimeFormat.forPattern("MMMM, yyyy");
            LocalDate localStartDate = null;
            LocalDate templocalStartDate = null;
            try {
                localStartDate = dtf.parseLocalDate(request.getParameter("startdate"));
                templocalStartDate = dtf.parseLocalDate(request.getParameter("startdate"));
            } catch(Exception ex) {
                localStartDate  = dtf.parseLocalDate(request.getAttribute("startdate").toString());
                templocalStartDate  = dtf.parseLocalDate(request.getAttribute("startdate").toString());
            }
            LocalDate localEndDate = null;
            LocalDate TemplocalEndDate = null;
            try {
                localEndDate = dtf.parseLocalDate(request.getParameter("enddate"));
                TemplocalEndDate = dtf.parseLocalDate(request.getParameter("enddate"));
            } catch(Exception ex) {
                localEndDate = dtf.parseLocalDate(request.getAttribute("enddate").toString());
                TemplocalEndDate = dtf.parseLocalDate(request.getAttribute("enddate").toString());
            }
            Date startDate = localStartDate.toDate();
            Date endDate = localEndDate.toDate();
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            int startMonth = cal.get(Calendar.MONTH);
            int startYear = cal.get(Calendar.YEAR);
            cal.setTime(endDate);
            int endMonth = cal.get(Calendar.MONTH);
            int endYear = cal.get(Calendar.YEAR);
            boolean isOneMonth = false;
            int monthCount = 0;
            SimpleDateFormat df1 = new SimpleDateFormat();
            DateTime date1 = localEndDate.toDateTime(LocalTime.MIDNIGHT);
            // including whole day 24*60*60-1
            date1 = date1.plusSeconds(86399);
            endDate = date1.dayOfMonth().withMaximumValue().toDate();
            Calendar startcal = Calendar.getInstance();
//            startcal.setTime(new Date(df1.format(startDate)));
            startcal.setTime(startDate);
            String startcalString = authHandler.getDateOnlyFormat().format(startcal.getTime());
            invoiceRequestParams.put(Constants.REQ_startdate, startcalString);
            
            invoiceRequestParams.put("MonthlyAgeingStartDate", (Date) startcal.getTime());
//            startcal.setTime(new Date(df1.format(endDate)));
            startcal.setTime(endDate);
            Date startcalDate = null;
            startcalString = authHandler.getDateOnlyFormat().format(startcal.getTime());
            try {
                startcalDate = authHandler.getDateOnlyFormat().parse(startcalString);
            } catch (ParseException ex) {
                startcalDate = startcal.getTime();
                Logger.getLogger(AccGoodsReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

            invoiceRequestParams.put(Constants.REQ_enddate, startcalString);
            invoiceRequestParams.put(InvoiceConstants.curdate, startcalString);
//            invoiceRequestParams.put("MonthlyAgeingEndDate", startcalDate);
//            invoiceRequestParams.put("isMonthlyAgeingReport", true);
//            invoiceRequestParams.put("MonthlyAgeingCurrDate", startcalDate);
//            request.setAttribute("MonthlyAgeingCurrDate", startcalDate);
            TemplocalEndDate = new LocalDate(startcalDate);
            // if user selects same month & year for both start & end fields, we get the last date of the month to populate into the endDate
            if (startMonth == endMonth && startYear == endYear) {
                isOneMonth = true;
                localEndDate = new LocalDate(endDate);
                monthCount = 1;
            }

            if (!isOneMonth) // just a trick to include the last month as well
            {
                localEndDate = localEndDate.plus(Period.months(1));
            }

            while (localStartDate.isBefore(localEndDate)) {
                localStartDate = localStartDate.plus(Period.months(1));
                monthCount++;
            }

            try {
                localStartDate = dtf.parseLocalDate(request.getParameter("startdate"));
                localEndDate = dtf.parseLocalDate(request.getParameter("enddate"));
            } catch (Exception ex) {
                localStartDate = dtf.parseLocalDate(request.getAttribute("startdate").toString());
                localEndDate = dtf.parseLocalDate(request.getAttribute("enddate").toString());
            }
            if (!isOneMonth) // just a trick to include the last month as well
            {
                localEndDate = localEndDate.plus(Period.months(1));
            } else {
                localEndDate = new LocalDate(endDate);
            }

            KwlReturnObject custresult = accVendorDAOobj.getVendorForAgedPayable(invoiceRequestParams);
            List vendorList = custresult.getEntityList();
//            if (invoiceRequestParams.containsKey("start")) { //Removing paging here because it is applying on each vendor data (CN/DN etc). Due to this complete amount due not showing in AP Summary.  
//                invoiceRequestParams.remove("start");
//            }
//            if (invoiceRequestParams.containsKey("limit")) {
//                invoiceRequestParams.remove("limit");
//            }
            Calendar oneDayBeforeCal1 = Calendar.getInstance();
            Calendar cal11 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            int duration = (invoiceRequestParams.containsKey(GoodsReceiptCMNConstants.DURATION) && invoiceRequestParams.get(GoodsReceiptCMNConstants.DURATION) != null) ? Integer.parseInt(invoiceRequestParams.get(GoodsReceiptCMNConstants.DURATION).toString()) : 30;
            Date curDate = null;
            String curDateString = "";
            if (invoiceRequestParams.get(GoodsReceiptCMNConstants.CURDATE) != null) {//Added for aged payable/receivable
                curDateString = (String) invoiceRequestParams.get(GoodsReceiptCMNConstants.CURDATE);
                if (invoiceRequestParams.get("MonthlyAgeingCurrDate") != null) {
                    curDate = (Date) invoiceRequestParams.get("MonthlyAgeingCurrDate");
                } else {
                    curDate = df.parse(curDateString);
                }
                cal11.setTime(curDate);
                cal2.setTime(curDate);
                cal3.setTime(curDate);
                cal4.setTime(curDate);
                cal5.setTime(curDate);
                cal6.setTime(curDate);
                cal7.setTime(curDate);
                oneDayBeforeCal1.add(Calendar.DAY_OF_YEAR, -1);     //Need to verify in multiple cases, then only take action on it
                cal2.add(Calendar.DAY_OF_YEAR, -duration);
                cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
                cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
                cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
                cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
            }
            invoiceRequestParams.put("oneDayBeforeCal1", oneDayBeforeCal1);
            invoiceRequestParams.put("cal1", cal11);
            invoiceRequestParams.put("cal2", cal2);
            invoiceRequestParams.put("cal3", cal3);
            invoiceRequestParams.put("cal4", cal4);
            invoiceRequestParams.put("cal5", cal5);
            invoiceRequestParams.put("cal6", cal6);
            invoiceRequestParams.put("cal7", cal7);
            if (vendorList != null && !vendorList.isEmpty()) {
                for (Object venid : vendorList) {
                    JSONObject summaryObj = new JSONObject();
                    JSONArray invjarr = new JSONArray();
                    String personID = null;
                    String personName = null;
                    String amountdueInBase = null;
                    String currencyid = null;
                    boolean isDN = false;
                    boolean isMP = false;
//                Object venid = itrcust.next();
                    invoiceRequestParams.put(GoodsReceiptCMNConstants.VENDORID, venid);
                    invoiceRequestParams.put(GoodsReceiptCMNConstants.ACCID, venid);
                    invoiceRequestParams.put("cntype", null);
                    invoiceRequestParams.put("isAgedSummary", true);
                    invoiceRequestParams.put("isAgedPayables", true); 
                    if (extraCompanyPreferences != null) {
                        invoiceRequestParams.put(Constants.isNewGST, extraCompanyPreferences.isIsNewGST());
                    }
                   
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Vendor.class.getName(), venid.toString());
                    Vendor vendor = (Vendor) objItr.getEntityList().get(0);
                    String currencyidCust = vendor.getAccount().getCurrency().getCurrencyID();
                    String currencySymbolCust = vendor.getAccount().getCurrency().getSymbol();
                    String currencyNameCust = vendor.getAccount().getCurrency().getName();

                    KwlReturnObject result = accGoodsReceiptobj.getGoodsReceiptsMerged(invoiceRequestParams);
                    if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        invjarr = accGoodsReceiptServiceHandler.getGoodsReceiptsJsonForMonthlyAgedPayables(invoiceRequestParams, result.getEntityList(), invjarr, accountingHandlerDAOobj, accCurrencyDAOobj, accGoodsReceiptobj, accAccountDAOobj, accGoodsReceiptCommon, accTaxObj);
                    }
                    
                    result = accGoodsReceiptobj.getOpeningBalanceInvoices(invoiceRequestParams);
                    if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        JSONArray OBJArryInvoice = new JSONArray();
                        OBJArryInvoice = AccGoodsReceiptServiceHandler.getAgedOpeningBalanceInvoiceJson(invoiceRequestParams, result.getEntityList(), OBJArryInvoice, accCurrencyDAOobj, accountingHandlerDAOobj, accGoodsReceiptCommon, accAccountDAOobj, fieldDataManagercntrl);
                        for (int i = 0; i < OBJArryInvoice.length(); i++) {
                            invjarr.put(OBJArryInvoice.get(i));
                        }
                    }
                    JSONArray OBJArryDebitNote = new JSONArray();
                    result = accDebitNoteobj.getOpeningBalanceDNs(invoiceRequestParams);
                    if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        OBJArryDebitNote = AccGoodsReceiptServiceHandler.getAgedOpeningBalanceDebitNoteJson(invoiceRequestParams, result.getEntityList(), OBJArryDebitNote, accCurrencyDAOobj, accountingHandlerDAOobj, accReceiptDAOobj, accDebitNoteobj,accAccountDAOobj, fieldDataManagercntrl);
                        for (int i = 0; i < OBJArryDebitNote.length(); i++) {
                            invjarr.put(OBJArryDebitNote.get(i));
                        }
                    }
                    JSONArray OBJArryCreditNote = new JSONArray();
                    result = accCreditNoteDAOobj.getOpeningBalanceVendorCNs(invoiceRequestParams);
                    if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        OBJArryCreditNote = AccGoodsReceiptServiceHandler.getAgedOpeningBalanceCreditNoteJson(invoiceRequestParams, result.getEntityList(), OBJArryCreditNote, accCurrencyDAOobj, accountingHandlerDAOobj, accPaymentDAOobj, accVendorPaymentobj,accDebitNoteobj,accAccountDAOobj, fieldDataManagercntrl);
                        for (int i = 0; i < OBJArryCreditNote.length(); i++) {
                            invjarr.put(OBJArryCreditNote.get(i));
                        }
                    }
                    JSONArray OBJArryPayment = new JSONArray();
                    result = accVendorPaymentobj.getOpeningBalancePayments(invoiceRequestParams);
                    if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        OBJArryPayment = AccGoodsReceiptServiceHandler.getAgedOpeningBalancePaymentJson(invoiceRequestParams, result.getEntityList(), OBJArryPayment, accCurrencyDAOobj, accountingHandlerDAOobj, accVendorPaymentobj, accAccountDAOobj, fieldDataManagercntrl);
                        for (int i = 0; i < OBJArryPayment.length(); i++) {
                            invjarr.put(OBJArryPayment.get(i));
                        }
                    }
                    JSONArray DebitNotejArr = new JSONArray();//This is used for getting DN gainst vendor and otherwise 
                    result = accDebitNoteobj.getDebitNoteMerged(invoiceRequestParams);
                    if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        DebitNotejArr = AccGoodsReceiptServiceHandler.getDebitNotesMergedJson(invoiceRequestParams, result.getEntityList(), DebitNotejArr, accCurrencyDAOobj, accountingHandlerDAOobj, accReceiptDAOobj, accDebitNoteobj,accAccountDAOobj, fieldDataManagercntrl, accJournalEntryobj);
                        for (int i = 0; i < DebitNotejArr.length(); i++) {
                            invjarr.put(DebitNotejArr.get(i));
                        }
                    }
                    invoiceRequestParams.put("cntype", 4);//This is used for getting Credit note against vendor 
                    JSONArray CreditNotejArr = new JSONArray();
                    result = accCreditNoteDAOobj.getCreditNoteMerged(invoiceRequestParams);
                    if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        CreditNotejArr = AccGoodsReceiptServiceHandler.getCreditNotesMergedJson(invoiceRequestParams, result.getEntityList(), CreditNotejArr, accCurrencyDAOobj, accountingHandlerDAOobj, accPaymentDAOobj, accVendorPaymentobj,accDebitNoteobj,accAccountDAOobj, fieldDataManagercntrl, accJournalEntryobj);
                        for (int i = 0; i < CreditNotejArr.length(); i++) {
                            invjarr.put(CreditNotejArr.get(i));
                        }
                    }
                    invoiceRequestParams.remove("cntype");
                    JSONArray makePaymentJArr = new JSONArray();
                    invoiceRequestParams.put("allAdvPayment", true); // needs only Advance type record so that putted true
                    invoiceRequestParams.put("paymentWindowType", 1);//Payment to Vendor record
                    result = accVendorPaymentobj.getPayments(invoiceRequestParams);
                    if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        makePaymentJArr = AccGoodsReceiptServiceHandler.getPaymentsJson(invoiceRequestParams, result.getEntityList(), makePaymentJArr, accCurrencyDAOobj, accountingHandlerDAOobj, accVendorPaymentobj);
                        for (int i = 0; i < makePaymentJArr.length(); i++) {
                            invjarr.put(makePaymentJArr.get(i));
                        }
                    }
                    
                    invoiceRequestParams.put("allAdvPayment", true); // needs only refund type record so that putted true
                    invoiceRequestParams.put("paymentWindowType", 2);//Receipt to Vendor record
                    JSONArray receivePaymentJArr = new JSONArray();
                    result = accReceiptDAOobj.getReceipts(invoiceRequestParams);
                    receivePaymentJArr = AccInvoiceServiceHandler.getReceiptsJson(invoiceRequestParams, result.getEntityList(), receivePaymentJArr, accountingHandlerDAOobj, authHandlerDAOObj, accCurrencyDAOobj, accReceiptDAOobj, request, accAccountDAOobj, fieldDataManagercntrl, accJournalEntryobj);
                    for (int i = 0; i < receivePaymentJArr.length(); i++) {
                        invjarr.put(receivePaymentJArr.get(i));
                    }
                    invoiceRequestParams.remove("allAdvPayment");
                    invoiceRequestParams.remove("paymentWindowType");
                
                    
                    for (int i = 0; i < invjarr.length(); i++) {
                        JSONObject invobj = invjarr.getJSONObject(i);
                        personID = (invobj.has("personid")) ? invobj.getString("personid") : "";
                        personName = (invobj.has("personname")) ? invobj.getString("personname") : "";
                        amountdueInBase = invobj.getString("amountdueinbase");
                        currencyid = (invobj.has("currencyid")) ? invobj.getString("currencyid") : "";
                        isDN = invobj.has("isDN") ? invobj.getBoolean("isDN") : false;
                        isMP = invobj.has("isMP") ? invobj.getBoolean("isMP") : false;
                        jObj.put("personid", personID);
                        jObj.put("amountdueinbase", amountdueInBase);
                        jObj.put("personname", personName);
                        Date dueDate = new Date();
                        if (datefilter == Constants.agedDueDate1to30Filter || datefilter == Constants.agedDueDate0to30Filter) {
                            dueDate = authHandler.getDateOnlyFormat(request).parse(invobj.getString("duedate"));
                        } else {
                            dueDate = authHandler.getDateOnlyFormat(request).parse(invobj.getString("date"));
                        }
                        double amountdue = invobj.getDouble("amountdue");
                        double amountdueinbase = invobj.getDouble("amountdueinbase");
                        double externalcurrencyrate = invobj.getDouble(GoodsReceiptCMNConstants.EXTERNALCURRENCYRATE);
//System.out.println("DOC -"+amountdue+" BASE-"+amountdueInBase);
                        boolean isopeningBalanceTransaction = invobj.optBoolean(GoodsReceiptCMNConstants.IsopeningBalanceTransaction, false);
                        boolean isConversionRateFromCurrencyToBase = invobj.optBoolean("isConversionRateFromCurrencyToBase", false);

                        KwlReturnObject bAmt = null;
                        if (isopeningBalanceTransaction && isConversionRateFromCurrencyToBase) {// if Transaction is opening balance Transaction and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(invoiceRequestParams, amountdue, currencyid, currencyidCust, df.parse(invobj.getString("date")), externalcurrencyrate);
                        } else {
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(invoiceRequestParams, amountdue, currencyid, currencyidCust, df.parse(invobj.getString("date")), externalcurrencyrate);
                        }
                        if (isSummary) {//------------------------In Case Of Summary Amount Due is in Vendor Currency
                            amountdue = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                        }
                        if ((isDN || isMP) && !invobj.optBoolean("isOpeningBalanceTransaction", false)) {
                            amountdueinbase = -amountdueinbase;
                            amountdue = -amountdue;
                        }
                        
                        double totalinbase = 0;
                        DateTime date = templocalStartDate.toDateTime(LocalTime.MIDNIGHT);
                        DateTime lastDateOfMonth = date.dayOfMonth().withMaximumValue();
                        Date tempDate = new Date();
                        Date startDate1 = templocalStartDate.toDate();
                        Calendar cal1 = Calendar.getInstance();
                        Date d = new Date();
                        df1 = new SimpleDateFormat();
                        df1.applyPattern("E MMM dd 12:30:00 Z yyyy");
                        cal1.setTime(new Date(df1.format(startDate1)));
                        Date cal1Date = null;
                        String cal1String = authHandler.getDateOnlyFormat().format(cal1.getTime());
                        try {
                            cal1Date = authHandler.getDateOnlyFormat().parse(cal1String);
                        } catch (ParseException ex) {
                            cal1Date = cal1.getTime();
                            Logger.getLogger(AccGoodsReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        tempDate = cal1Date;
                        localStartDate = new LocalDate(tempDate);
                        Date endDate1 = lastDateOfMonth.toDate();
                        Calendar endcal = Calendar.getInstance();
                        d = new Date();
                        endcal.setTime(new Date(df1.format(endDate1)));
                        Date endcalDate = null;
                        String endcalString = authHandler.getDateOnlyFormat().format(endcal.getTime());
                        try {
                            endcalDate = authHandler.getDateOnlyFormat().parse(endcalString);
                        } catch (ParseException ex) {
                            endcalDate = endcal.getTime();
                            Logger.getLogger(AccGoodsReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        localEndDate = new LocalDate(endcalDate);
                        int cntamountduecount = 0;
                        LocalDate dueDate1 = new LocalDate(dueDate);
                        invobj.put(GoodsReceiptCMNConstants.AMOUNTDUEINBASE, authHandler.round(amountdueinbase, companyid));
                        boolean isOpening = invobj.has(InvoiceConstants.isopeningBalanceTransaction) ? invobj.optBoolean(InvoiceConstants.isopeningBalanceTransaction) : false;
                        /*
                         * If start date is greater than invoice date or due
                         * date then treat that record as opening record.
                         */
                        if (startDate != null && dueDate.before(startDate)) {
                            isOpening = true;
                        }

                        if (isOpening) {
                            double tempamount = 0;
                            double tempamount1 = 0;
                            double tempamountinbase = 0;
                            tempamount += amountdue;
                            tempamount1 += amountdue;
                            tempamountinbase += amountdueinbase;
                            if (invobj.has("opening")) {
                                tempamount += invobj.optDouble("opening");
                            }
                            invobj.put("opening", tempamount);
                            if (summaryObj.has("opening")) {
                                tempamount1 += summaryObj.optDouble("opening");
                            }
                            summaryObj.put("opening", tempamount1);
                            if (summaryObj.has("openinginbase")) {
                                tempamountinbase += summaryObj.optDouble("openinginbase");
                            }
                            summaryObj.put("openinginbase", tempamountinbase);
                            if (!isSummary) {
//                                double open = 0;
//                                if (invobj.has("opening")) {
//                                    open = invobj.optDouble("opening");
//                                }
                                invobj.put("total",amountdueinbase);
                                jArr.put(invobj);
                            }
                        } else {
                            while (localStartDate.isBefore(localEndDate) && localStartDate.isBefore(TemplocalEndDate)) {
                                double tempamount = 0;
                                double tempamount1 = 0;
                                double tempamountinbase = 0;
                                double tempamountinbase1 = 0;
                                if ((dueDate1.isAfter(localStartDate) || dueDate1.equals(localStartDate)) && (dueDate1.isBefore(localEndDate) || dueDate1.equals(localEndDate))) {
                                    tempamount += amountdue;
                                    tempamount1 += amountdue;
                                    tempamountinbase += amountdueinbase;
                                    tempamountinbase1 += amountdueinbase;
                                }
                                if (invobj.has("amountdue_" + cntamountduecount)) {
                                    tempamount += invobj.optDouble("amountdue_" + cntamountduecount);
                                }
                                invobj.put("amountdue_" + cntamountduecount, tempamount);

                                if (invobj.has("amountdueinbase_" + cntamountduecount)) {
                                    tempamountinbase1 += invobj.optDouble("amountdueinbase_" + cntamountduecount);
                                }
                                invobj.put("amountdueinbase_" + cntamountduecount, tempamountinbase1);

                                if (summaryObj.has("amountdue_" + cntamountduecount)) {
                                    tempamount1 += summaryObj.optDouble("amountdue_" + cntamountduecount);
                                }
                                summaryObj.put("amountdue_" + cntamountduecount, tempamount1);
                                if (summaryObj.has("amountdueinbase_" + cntamountduecount)) {
                                    tempamountinbase += summaryObj.optDouble("amountdueinbase_" + cntamountduecount);
                                }
                                summaryObj.put("amountdueinbase_" + cntamountduecount, tempamountinbase);
                                
                                if (!isSummary) {
                                    if ((dueDate1.isAfter(localStartDate) || dueDate1.equals(localStartDate)) && (dueDate1.isBefore(localEndDate) || dueDate1.equals(localEndDate))) {
                                        if (invobj.has("amountdueinbase_" + cntamountduecount)) {
                                            totalinbase += invobj.optDouble("amountdueinbase_" + cntamountduecount);
                                        }
                                        invobj.put("total", totalinbase);
                                        jArr.put(invobj);
                                    }
                                }
                                
                                date = localStartDate.toDateTime(LocalTime.MIDNIGHT);
                                date = date.plusMonths(1);
                                DateTime firstDateOfMonth = date.dayOfMonth().withMinimumValue();
                                lastDateOfMonth = date.dayOfMonth().withMaximumValue();
                                tempDate = new Date();
                                startDate1 = firstDateOfMonth.toDate();
                                cal1 = Calendar.getInstance();
                                d = new Date();
                                cal1.setTime(new Date(df1.format(startDate1)));

                                try {
                                    cal1String = authHandler.getDateOnlyFormat().format(cal1.getTime());
                                    cal1Date = authHandler.getDateOnlyFormat().parse(cal1String);
                                } catch (ParseException ex) {
                                    cal1Date = cal1.getTime();
                                    Logger.getLogger(AccGoodsReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                                }

                                tempDate = cal1Date;
                                localStartDate = new LocalDate(tempDate);
                                endDate1 = lastDateOfMonth.toDate();
                                endcal = Calendar.getInstance();
                                d = new Date();
                                endcal.setTime(new Date(df1.format(endDate1)));

                                try {
                                    endcalString = authHandler.getDateOnlyFormat().format(endcal.getTime());
                                    endcalDate = authHandler.getDateOnlyFormat().parse(endcalString);
                                } catch (ParseException ex) {
                                    endcalDate = endcal.getTime();
                                    Logger.getLogger(AccGoodsReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                localEndDate = new LocalDate(endcalDate);
                                cntamountduecount++;
                            }
                        }

//                        if (!isSummary) {
//                            double totalinbase = 0;
//                            double open = 0;
//                            if (invobj.has("opening")) {
//                                open = invobj.optDouble("opening");
//                            }
//                            invobj.put("total", 0);
//                            for (int cnt = 0; cnt < monthCount; cnt++) {
//                                if (invobj.has("amountdueinbase_" + cnt)) {
//                                    totalinbase += invobj.optDouble("amountdueinbase_" + cnt);
//                                }
//                            }
//                            invobj.put("total", totalinbase + open);
//                            jArr.put(invobj);
//                        }
                    }
                    //-------------------------------------Insert Summary Object Data For Vendor Here-------------------
                    if (invjarr.length() > 0 && isSummary) {
                        summaryObj.put("personid", personID);
                        summaryObj.put("personname", personName);
                        summaryObj.put("currencysymbol", currencySymbolCust);
                        summaryObj.put("currencyid", currencyidCust);
                        summaryObj.put("currencyname", currencyNameCust);
                        double total = 0;
                        double opening = 0;
                        if (summaryObj.has("opening")) {
                            opening = summaryObj.optDouble("opening");
                        }
                        double openinginbase = 0;
                        if (summaryObj.has("openinginbase")) {
                            openinginbase = summaryObj.optDouble("openinginbase");
                        }
                        summaryObj.put("total", 0);
                        StringBuilder sb = null;
                        if (isCallFromDD) {
                            for (int cnt = (monthCount - 1); cnt >= 0; cnt--) {
                                sb = new StringBuilder();
                                sb.append("amountdue_");
                                sb.append(cnt);
                                if (summaryObj.has(sb.toString())) {
                                    total += summaryObj.optDouble(sb.toString());
                                    summaryObj.put(sb.toString(), summaryObj.optDouble(sb.toString()));
                                } else {
                                    summaryObj.put(sb.toString(), 0.0);
                                }
                            }
                        } else {
                            for (int cnt = 0; cnt < monthCount; cnt++) {
                                if (summaryObj.has("amountdue_" + cnt)) {
                                    total += summaryObj.optDouble("amountdue_" + cnt);
                                    summaryObj.put("amountdue_" + cnt, summaryObj.optDouble("amountdue_" + cnt));
                                }
                            }
                        }
                        summaryObj.put("total", total + opening);
                        double totalinbase = 0;
                        summaryObj.put("totalinbase", 0);
                        if (isCallFromDD) {
                            for (int cnt = (monthCount - 1); cnt >= 0; cnt--) {
                                sb = new StringBuilder();
                                sb.append("amountdueinbase_");
                                sb.append(cnt);
                                if (summaryObj.has(sb.toString())) {
                                    totalinbase += summaryObj.optDouble(sb.toString());
                                } else {
                                    summaryObj.put(sb.toString(), 0.0);
                                }
                            }
                        } else {
                            for (int cnt = 0; cnt < monthCount; cnt++) {
                                if (summaryObj.has("amountdueinbase_" + cnt)) {
                                    totalinbase += summaryObj.optDouble("amountdueinbase_" + cnt);
                                }
                            }
                        }
                        summaryObj.put("totalinbase", totalinbase + openinginbase);
                        jArr.put(summaryObj);
                    }
                }
            }

            JSONObject monthArrayObject = new JSONObject();
            //----------------------------- the last object would be the months array--------------------------
            localStartDate = new LocalDate(startDate);
            localEndDate = new LocalDate(endDate);
            JSONArray monthArray = new JSONArray();
            JSONObject monthObj;
            ArrayList monthList = new ArrayList();
            while (localStartDate.isBefore(localEndDate)) {
                String monthName = localStartDate.toString("MMM yyyy");
                localStartDate = localStartDate.plus(Period.months(1));
                monthList.add(monthName);
            }
            for (int i = 0; i < monthList.size(); i++) {
                monthObj = new JSONObject();
                monthObj.put("monthname", monthList.get(i));
                monthArray.put(monthObj);
            }
            if (!StringUtil.equal(fileType, "csv") && !StringUtil.equal(fileType, "xls") && !StringUtil.equal(fileType, "print")) {
                monthArrayObject.put("months", monthArray);
                jArr.put(monthArrayObject);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccGoodsReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    @Override
    public JSONArray getMonthlyVendorAgedPayableMerged(JSONObject requestJobj) throws ServiceException, JSONException, SessionExpiredException, ParseException {
        JSONObject jObj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            String companyid = requestJobj.optString("companyid");
            HashMap<String, Object> invoiceRequestParams = new HashMap<String, Object>();
            String fileType = requestJobj.optString("filetype","");
            invoiceRequestParams.put(GoodsReceiptCMNConstants.COMPANYID, requestJobj.optString(Constants.companyKey,""));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.GCURRENCYID, requestJobj.optString(Constants.globalCurrencyKey,""));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.DATEFORMAT, authHandler.getDateOnlyFormat());
            invoiceRequestParams.put(GoodsReceiptCMNConstants.START, requestJobj.optString(GoodsReceiptCMNConstants.START,""));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.LIMIT, requestJobj.optString(GoodsReceiptCMNConstants.LIMIT,""));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.SS, requestJobj.optString(GoodsReceiptCMNConstants.SS,""));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.ACCID, requestJobj.optString(GoodsReceiptCMNConstants.ACCID,""));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.CASHONLY, requestJobj.optString(GoodsReceiptCMNConstants.CASHONLY,""));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.CREDITONLY, requestJobj.optString(GoodsReceiptCMNConstants.CREDITONLY,""));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.IGNOREZERO, requestJobj.optString(GoodsReceiptCMNConstants.IGNOREZERO,""));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.CURDATE, requestJobj.optString(GoodsReceiptCMNConstants.CURDATE,""));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.PERSONGROUP, requestJobj.optString(GoodsReceiptCMNConstants.PERSONGROUP,""));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.ISAGEDGRAPH, requestJobj.optString(GoodsReceiptCMNConstants.ISAGEDGRAPH,""));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.VENDORID, requestJobj.optString(GoodsReceiptCMNConstants.VENDORID,""));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.NONDELETED, requestJobj.optString(GoodsReceiptCMNConstants.NONDELETED,""));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.DURATION, requestJobj.optString(GoodsReceiptCMNConstants.DURATION,""));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.ISDISTRIBUTIVE, requestJobj.optString(GoodsReceiptCMNConstants.ISDISTRIBUTIVE,""));
            invoiceRequestParams.put(GoodsReceiptCMNConstants.WITHINVENTORY, requestJobj.optString(GoodsReceiptCMNConstants.WITHINVENTORY,""));
            invoiceRequestParams.put(Constants.df, authHandler.getDateOnlyFormat());
            invoiceRequestParams.put(Constants.REQ_startdate, requestJobj.optString(Constants.REQ_startdate,""));
            invoiceRequestParams.put(Constants.REQ_enddate, requestJobj.optString(Constants.REQ_enddate,""));
            invoiceRequestParams.put("pendingapproval", requestJobj.optBoolean("pendingapproval",false));
            invoiceRequestParams.put("istemplate", requestJobj.optInt("istemplate",0));
            invoiceRequestParams.put("datefilter", requestJobj.optString("datefilter",""));
            invoiceRequestParams.put("custVendorID", requestJobj.optString("custVendorID",""));
            invoiceRequestParams.put("asofdate", requestJobj.optString("asofdate",""));
            invoiceRequestParams.put("isAged", requestJobj.optString("isAged",""));
            DateFormat df = authHandler.getDateOnlyFormat();
            int datefilter = requestJobj.optInt("datefilter",-1);// 0 = Invoice Due date OR 1 = Invoice date
            boolean isSummary = false;
            boolean isCallFromDD = false;
            if (!StringUtil.isNullOrEmpty(requestJobj.optString("isSummary",""))) {
                isSummary = requestJobj.optBoolean("isSummary",false);
            } 
//            else if (request.getAttribute("isSummary","") != null) {
//                isSummary = Boolean.parseBoolean(request.getAttribute("isSummary","").toString());
//            }
            if (!StringUtil.isNullOrEmpty(requestJobj.optString("isCallFromDD",""))) {
                isCallFromDD = requestJobj.optBoolean("isCallFromDD",false);
            }
            String transactionId = requestJobj.optString("transactionId","");
            if(isCallFromDD){
                invoiceRequestParams.put("custVendorID", transactionId);
            }
            if (isCallFromDD) {// Document designer 0 = Invoice Due date 
                datefilter = 0;
            }
            final DateTimeFormatter dtf = DateTimeFormat.forPattern("MMMM, yyyy");
            LocalDate localStartDate = null;
            LocalDate templocalStartDate = null;
//            try {
//                localStartDate = dtf.parseLocalDate(request.getParameter("startdate",""));
//                templocalStartDate = dtf.parseLocalDate(request.getParameter("startdate",""));
//            } catch(Exception ex) {
//                localStartDate  = dtf.parseLocalDate(request.getAttribute("startdate").toString());
//                templocalStartDate  = dtf.parseLocalDate(request.getAttribute("startdate").toString());
//            }
            localStartDate = dtf.parseLocalDate(requestJobj.optString("startdate",""));
            templocalStartDate = dtf.parseLocalDate(requestJobj.optString("startdate",""));
            LocalDate localEndDate = null;
            LocalDate TemplocalEndDate = null;
//            try {
//                localEndDate = dtf.parseLocalDate(request.getParameter("enddate",""));
//                TemplocalEndDate = dtf.parseLocalDate(request.getParameter("enddate",""));
//            } catch(Exception ex) {
//                localEndDate = dtf.parseLocalDate(request.getAttribute("enddate","").toString());
//                TemplocalEndDate = dtf.parseLocalDate(request.getAttribute("enddate","").toString());
//            }
            localEndDate = dtf.parseLocalDate(requestJobj.optString("enddate","").toString());
            TemplocalEndDate = dtf.parseLocalDate(requestJobj.optString("enddate","").toString());
            Date startDate = localStartDate.toDate();
            Date endDate = localEndDate.toDate();
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            int startMonth = cal.get(Calendar.MONTH);
            int startYear = cal.get(Calendar.YEAR);
            cal.setTime(endDate);
            int endMonth = cal.get(Calendar.MONTH);
            int endYear = cal.get(Calendar.YEAR);
            boolean isOneMonth = false;
            int monthCount = 0;
            SimpleDateFormat df1 = new SimpleDateFormat();
            DateTime date1 = localEndDate.toDateTime(LocalTime.MIDNIGHT);
            // including whole day 24*60*60-1
            date1 = date1.plusSeconds(86399);
            endDate = date1.dayOfMonth().withMaximumValue().toDate();
            Calendar startcal = Calendar.getInstance();
            startcal.setTime(new Date(df1.format(startDate)));
            invoiceRequestParams.put(Constants.REQ_startdate, "" + (startcal.getTimeInMillis()));
            invoiceRequestParams.put("MonthlyAgeingStartDate", (Date) startcal.getTime());
            startcal.setTime(new Date(df1.format(endDate)));
            Date startcalDate = null;
            String startcalString = authHandler.getDateOnlyFormat().format(startcal.getTime());
            try {
                startcalDate = authHandler.getDateOnlyFormat().parse(startcalString);
            } catch (ParseException ex) {
                startcalDate = startcal.getTime();
                Logger.getLogger(AccGoodsReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }

            invoiceRequestParams.put(Constants.REQ_enddate, "" + (startcal.getTimeInMillis()));
            invoiceRequestParams.put(InvoiceConstants.curdate, "" + (startcal.getTimeInMillis()));
            invoiceRequestParams.put("MonthlyAgeingEndDate", startcalDate);
            invoiceRequestParams.put("isMonthlyAgeingReport", true);
            invoiceRequestParams.put("MonthlyAgeingCurrDate", startcalDate);
            requestJobj.put("MonthlyAgeingCurrDate", startcalDate);
            TemplocalEndDate = new LocalDate(startcalDate);
            // if user selects same month & year for both start & end fields, we get the last date of the month to populate into the endDate
            if (startMonth == endMonth && startYear == endYear) {
                isOneMonth = true;
                localEndDate = new LocalDate(endDate);
                monthCount = 1;
            }

            if (!isOneMonth) // just a trick to include the last month as well
            {
                localEndDate = localEndDate.plus(Period.months(1));
            }

            while (localStartDate.isBefore(localEndDate)) {
                localStartDate = localStartDate.plus(Period.months(1));
                monthCount++;
            }

//            try {
//                localStartDate = dtf.parseLocalDate(request.getParameter("startdate"));
//                localEndDate = dtf.parseLocalDate(request.getParameter("enddate"));
//            } catch (Exception ex) {
//                localStartDate = dtf.parseLocalDate(request.getAttribute("startdate").toString());
//                localEndDate = dtf.parseLocalDate(request.getAttribute("enddate").toString());
//            }
            localStartDate = dtf.parseLocalDate(requestJobj.optString("startdate",""));
            localEndDate = dtf.parseLocalDate(requestJobj.optString("enddate",""));
            
            if (!isOneMonth) // just a trick to include the last month as well
            {
                localEndDate = localEndDate.plus(Period.months(1));
            } else {
                localEndDate = new LocalDate(endDate);
            }

            KwlReturnObject custresult = accVendorDAOobj.getVendorForAgedPayable(invoiceRequestParams);
            List vendorList = custresult.getEntityList();
            if (invoiceRequestParams.containsKey("start")) { //Removing paging here because it is applying on each vendor data (CN/DN etc). Due to this complete amount due not showing in AP Summary.  
                invoiceRequestParams.remove("start");
            }
            if (invoiceRequestParams.containsKey("limit")) {
                invoiceRequestParams.remove("limit");
            }
            Calendar oneDayBeforeCal1 = Calendar.getInstance();
            Calendar cal11 = Calendar.getInstance();
            Calendar cal2 = Calendar.getInstance();
            Calendar cal3 = Calendar.getInstance();
            Calendar cal4 = Calendar.getInstance();
            Calendar cal5 = Calendar.getInstance();
            Calendar cal6 = Calendar.getInstance();
            Calendar cal7 = Calendar.getInstance();
            int duration = (invoiceRequestParams.containsKey(GoodsReceiptCMNConstants.DURATION) && invoiceRequestParams.get(GoodsReceiptCMNConstants.DURATION) != null) ? Integer.parseInt(invoiceRequestParams.get(GoodsReceiptCMNConstants.DURATION).toString()) : 30;
            Date curDate = null;
            String curDateString = "";
            if (invoiceRequestParams.get(GoodsReceiptCMNConstants.CURDATE) != null) {//Added for aged payable/receivable
                curDateString = (String) invoiceRequestParams.get(GoodsReceiptCMNConstants.CURDATE);
                if (invoiceRequestParams.get("MonthlyAgeingCurrDate") != null) {
                    curDate = (Date) invoiceRequestParams.get("MonthlyAgeingCurrDate");
                } else {
                    curDate = df.parse(curDateString);
                }
                cal11.setTime(curDate);
                cal2.setTime(curDate);
                cal3.setTime(curDate);
                cal4.setTime(curDate);
                cal5.setTime(curDate);
                cal6.setTime(curDate);
                cal7.setTime(curDate);
                oneDayBeforeCal1.add(Calendar.DAY_OF_YEAR, -1);     //Need to verify in multiple cases, then only take action on it
                cal2.add(Calendar.DAY_OF_YEAR, -duration);
                cal3.add(Calendar.DAY_OF_YEAR, -(duration * 2));
                cal4.add(Calendar.DAY_OF_YEAR, -(duration * 3));
                cal5.add(Calendar.DAY_OF_YEAR, -(duration * 4));
                cal6.add(Calendar.DAY_OF_YEAR, -(duration * 5));
                cal7.add(Calendar.DAY_OF_YEAR, -(duration * 6));
            }
            invoiceRequestParams.put("oneDayBeforeCal1", oneDayBeforeCal1);
            invoiceRequestParams.put("cal1", cal11);
            invoiceRequestParams.put("cal2", cal2);
            invoiceRequestParams.put("cal3", cal3);
            invoiceRequestParams.put("cal4", cal4);
            invoiceRequestParams.put("cal5", cal5);
            invoiceRequestParams.put("cal6", cal6);
            invoiceRequestParams.put("cal7", cal7);
            if (vendorList != null && !vendorList.isEmpty()) {
                for (Object venid : vendorList) {
                    JSONObject summaryObj = new JSONObject();
                    JSONArray invjarr = new JSONArray();
                    String personID = null;
                    String personName = null;
                    String amountdueInBase = null;
                    String currencyid = null;
                    boolean isDN = false;
                    boolean isMP = false;
//                Object venid = itrcust.next();
                    invoiceRequestParams.put(GoodsReceiptCMNConstants.VENDORID, venid);
                    invoiceRequestParams.put(GoodsReceiptCMNConstants.ACCID, venid);
                    invoiceRequestParams.put("cntype", null);
                    invoiceRequestParams.put("isAgedSummary", true);
                
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Vendor.class.getName(), venid.toString());
                    Vendor vendor = (Vendor) objItr.getEntityList().get(0);
                    String currencyidCust = vendor.getAccount().getCurrency().getCurrencyID();
                    String currencySymbolCust = vendor.getAccount().getCurrency().getSymbol();
                    String currencyNameCust = vendor.getAccount().getCurrency().getName();

                    KwlReturnObject result = accGoodsReceiptobj.getGoodsReceiptsMerged(invoiceRequestParams);
                    if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        invjarr = accGoodsReceiptServiceHandler.getGoodsReceiptsJsonForMonthlyAgedPayables(invoiceRequestParams, result.getEntityList(), invjarr, accountingHandlerDAOobj, accCurrencyDAOobj, accGoodsReceiptobj, accAccountDAOobj, accGoodsReceiptCommon, accTaxObj);
                    }
                    
                    result = accGoodsReceiptobj.getOpeningBalanceInvoices(invoiceRequestParams);
                    if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        JSONArray OBJArryInvoice = new JSONArray();
                        OBJArryInvoice = AccGoodsReceiptServiceHandler.getAgedOpeningBalanceInvoiceJson(invoiceRequestParams, result.getEntityList(), OBJArryInvoice, accCurrencyDAOobj, accountingHandlerDAOobj, accGoodsReceiptCommon, accAccountDAOobj, fieldDataManagercntrl);
                        for (int i = 0; i < OBJArryInvoice.length(); i++) {
                            invjarr.put(OBJArryInvoice.get(i));
                        }
                    }
                    JSONArray OBJArryDebitNote = new JSONArray();
                    result = accDebitNoteobj.getOpeningBalanceDNs(invoiceRequestParams);
                    if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        OBJArryDebitNote = AccGoodsReceiptServiceHandler.getAgedOpeningBalanceDebitNoteJson(invoiceRequestParams, result.getEntityList(), OBJArryDebitNote, accCurrencyDAOobj, accountingHandlerDAOobj, accReceiptDAOobj, accDebitNoteobj,accAccountDAOobj, fieldDataManagercntrl);
                        for (int i = 0; i < OBJArryDebitNote.length(); i++) {
                            invjarr.put(OBJArryDebitNote.get(i));
                        }
                    }
                    JSONArray OBJArryCreditNote = new JSONArray();
                    result = accCreditNoteDAOobj.getOpeningBalanceVendorCNs(invoiceRequestParams);
                    if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        OBJArryCreditNote = AccGoodsReceiptServiceHandler.getAgedOpeningBalanceCreditNoteJson(invoiceRequestParams, result.getEntityList(), OBJArryCreditNote, accCurrencyDAOobj, accountingHandlerDAOobj, accPaymentDAOobj, accVendorPaymentobj,accDebitNoteobj,accAccountDAOobj, fieldDataManagercntrl);
                        for (int i = 0; i < OBJArryCreditNote.length(); i++) {
                            invjarr.put(OBJArryCreditNote.get(i));
                        }
                    }
                    JSONArray OBJArryPayment = new JSONArray();
                    result = accVendorPaymentobj.getOpeningBalancePayments(invoiceRequestParams);
                    if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        OBJArryPayment = AccGoodsReceiptServiceHandler.getAgedOpeningBalancePaymentJson(invoiceRequestParams, result.getEntityList(), OBJArryPayment, accCurrencyDAOobj, accountingHandlerDAOobj, accVendorPaymentobj, accAccountDAOobj, fieldDataManagercntrl);
                        for (int i = 0; i < OBJArryPayment.length(); i++) {
                            invjarr.put(OBJArryPayment.get(i));
                        }
                    }
                     JSONArray DebitNotejArr = new JSONArray();//This is used for getting DN gainst vendor and otherwise 
                    result = accDebitNoteobj.getDebitNoteMerged(invoiceRequestParams);
                    if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        DebitNotejArr = AccGoodsReceiptServiceHandler.getDebitNotesMergedJson(invoiceRequestParams, result.getEntityList(), DebitNotejArr, accCurrencyDAOobj, accountingHandlerDAOobj, accReceiptDAOobj, accDebitNoteobj,accAccountDAOobj, fieldDataManagercntrl, accJournalEntryobj);
                        for (int i = 0; i < DebitNotejArr.length(); i++) {
                            invjarr.put(DebitNotejArr.get(i));
                        }
                    }
                    invoiceRequestParams.put("cntype", 4);//This is used for getting Credit note against vendor 
                    JSONArray CreditNotejArr = new JSONArray();
                    result = accCreditNoteDAOobj.getCreditNoteMerged(invoiceRequestParams);
                    if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        CreditNotejArr = AccGoodsReceiptServiceHandler.getCreditNotesMergedJson(invoiceRequestParams, result.getEntityList(), CreditNotejArr, accCurrencyDAOobj, accountingHandlerDAOobj, accPaymentDAOobj, accVendorPaymentobj,accDebitNoteobj,accAccountDAOobj, fieldDataManagercntrl, accJournalEntryobj);
                        for (int i = 0; i < CreditNotejArr.length(); i++) {
                            invjarr.put(CreditNotejArr.get(i));
                        }
                    }
                    invoiceRequestParams.remove("cntype");
                    JSONArray makePaymentJArr = new JSONArray();
                    invoiceRequestParams.put("allAdvPayment", true); // needs only Advance type record so that putted true
                    invoiceRequestParams.put("paymentWindowType", 1);//Payment to Vendor record
                    result = accVendorPaymentobj.getPayments(invoiceRequestParams);
                    if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        makePaymentJArr = AccGoodsReceiptServiceHandler.getPaymentsJson(invoiceRequestParams, result.getEntityList(), makePaymentJArr, accCurrencyDAOobj, accountingHandlerDAOobj, accVendorPaymentobj);
                        for (int i = 0; i < makePaymentJArr.length(); i++) {
                            invjarr.put(makePaymentJArr.get(i));
                        }
                    }
                    
                    invoiceRequestParams.put("allAdvPayment", true); // needs only refund type record so that putted true
                    invoiceRequestParams.put("paymentWindowType", 2);//Receipt to Vendor record
                    JSONArray receivePaymentJArr = new JSONArray();
                    result = accReceiptDAOobj.getReceipts(invoiceRequestParams);
                    receivePaymentJArr = AccInvoiceServiceHandler.getReceiptsJson(invoiceRequestParams, result.getEntityList(), receivePaymentJArr, accountingHandlerDAOobj, authHandlerDAOObj, accCurrencyDAOobj, accReceiptDAOobj, requestJobj, accAccountDAOobj, fieldDataManagercntrl, accJournalEntryobj);
                    for (int i = 0; i < receivePaymentJArr.length(); i++) {
                        invjarr.put(receivePaymentJArr.get(i));
                    }
                    invoiceRequestParams.remove("allAdvPayment");
                    invoiceRequestParams.remove("paymentWindowType");
                
                    
                    for (int i = 0; i < invjarr.length(); i++) {
                        JSONObject invobj = invjarr.getJSONObject(i);
                        personID = (invobj.has("personid")) ? invobj.getString("personid") : "";
                        personName = (invobj.has("personname")) ? invobj.getString("personname") : "";
                        amountdueInBase = invobj.getString("amountdueinbase");
                        currencyid = (invobj.has("currencyid")) ? invobj.getString("currencyid") : "";
                        isDN = invobj.has("isDN") ? invobj.getBoolean("isDN") : false;
                        isMP = invobj.has("isMP") ? invobj.getBoolean("isMP") : false;
                        jObj.put("personid", personID);
                        jObj.put("amountdueinbase", amountdueInBase);
                        jObj.put("personname", personName);
                        Date dueDate = new Date();
                        if (datefilter == Constants.agedDueDate1to30Filter || datefilter == Constants.agedDueDate0to30Filter) {
                            dueDate = authHandler.getDateOnlyFormat().parse(invobj.getString("duedate"));
                        } else {
                            dueDate = authHandler.getDateOnlyFormat().parse(invobj.getString("date"));
                        }
                        double amountdue = invobj.getDouble("amountdue");
                        double amountdueinbase = invobj.getDouble("amountdueinbase");
                        double externalcurrencyrate = invobj.getDouble(GoodsReceiptCMNConstants.EXTERNALCURRENCYRATE);
//System.out.println("DOC -"+amountdue+" BASE-"+amountdueInBase);
                        boolean isopeningBalanceTransaction = invobj.optBoolean(GoodsReceiptCMNConstants.IsopeningBalanceTransaction, false);
                        boolean isConversionRateFromCurrencyToBase = invobj.optBoolean("isConversionRateFromCurrencyToBase", false);

                        KwlReturnObject bAmt = null;
                        if (isopeningBalanceTransaction && isConversionRateFromCurrencyToBase) {// if Transaction is opening balance Transaction and Conversion rate is taken from user is Currency to base then following method will be called.
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModifiedAccordingToCurrencyToBaseExchangeRate(invoiceRequestParams, amountdue, currencyid, currencyidCust, df.parse(invobj.getString("date")), externalcurrencyrate);
                        } else {
                            bAmt = accCurrencyDAOobj.getOneCurrencyToOtherModified(invoiceRequestParams, amountdue, currencyid, currencyidCust, df.parse(invobj.getString("date")), externalcurrencyrate);
                        }
                        if (isSummary) {//------------------------In Case Of Summary Amount Due is in Vendor Currency
                            amountdue = authHandler.round((Double) bAmt.getEntityList().get(0), companyid);
                        }
                        if (isDN || isMP) {
                            amountdueinbase = -amountdueinbase;
                            amountdue = -amountdue;
                        }

                        double totalinbase = 0;
                        DateTime date = templocalStartDate.toDateTime(LocalTime.MIDNIGHT);
                        DateTime lastDateOfMonth = date.dayOfMonth().withMaximumValue();
                        Date tempDate = new Date();
                        Date startDate1 = templocalStartDate.toDate();
                        Calendar cal1 = Calendar.getInstance();
                        Date d = new Date();
                        df1 = new SimpleDateFormat();
                        df1.applyPattern("E MMM dd 12:30:00 Z yyyy");
                        cal1.setTime(new Date(df1.format(startDate1)));
                        Date cal1Date = null;
                        String cal1String = authHandler.getDateOnlyFormat().format(cal1.getTime());
                        try {
                            cal1Date = authHandler.getDateOnlyFormat().parse(cal1String);
                        } catch (ParseException ex) {
                            cal1Date = cal1.getTime();
                            Logger.getLogger(AccGoodsReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        tempDate = cal1Date;
                        localStartDate = new LocalDate(tempDate);
                        Date endDate1 = lastDateOfMonth.toDate();
                        Calendar endcal = Calendar.getInstance();
                        d = new Date();
                        endcal.setTime(new Date(df1.format(endDate1)));
                        Date endcalDate = null;
                        String endcalString = authHandler.getDateOnlyFormat().format(endcal.getTime());
                        try {
                            endcalDate = authHandler.getDateOnlyFormat().parse(endcalString);
                        } catch (ParseException ex) {
                            endcalDate = endcal.getTime();
                            Logger.getLogger(AccGoodsReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        localEndDate = new LocalDate(endcalDate);
                        int cntamountduecount = 0;
                        LocalDate dueDate1 = new LocalDate(dueDate);
                        boolean isOpening = invobj.has(InvoiceConstants.isopeningBalanceTransaction) ? invobj.optBoolean(InvoiceConstants.isopeningBalanceTransaction) : false;
                        if (isOpening) {
                            double tempamount = 0;
                            double tempamount1 = 0;
                            double tempamountinbase = 0;
                            tempamount += amountdue;
                            tempamount1 += amountdue;
                            tempamountinbase += amountdueinbase;
                            if (invobj.has("opening")) {
                                tempamount += invobj.optDouble("opening");
                            }
                            invobj.put("opening", tempamount);
                            if (summaryObj.has("opening")) {
                                tempamount1 += summaryObj.optDouble("opening");
                            }
                            summaryObj.put("opening", tempamount1);
                            if (summaryObj.has("openinginbase")) {
                                tempamountinbase += summaryObj.optDouble("openinginbase");
                            }
                            summaryObj.put("openinginbase", tempamountinbase);
                            
                            if (!isSummary) {
                                double open = 0;
                                if (invobj.has("opening")) {
                                    open = invobj.optDouble("opening");
                                }
                                invobj.put("total", open);
                                jArr.put(invobj);
                            }
                            
                        } else {
                            while (localStartDate.isBefore(localEndDate) && localStartDate.isBefore(TemplocalEndDate)) {
                                double tempamount = 0;
                                double tempamount1 = 0;
                                double tempamountinbase = 0;
                                double tempamountinbase1 = 0;
                                if ((dueDate1.isAfter(localStartDate) || dueDate1.equals(localStartDate)) && (dueDate1.isBefore(localEndDate) || dueDate1.equals(localEndDate))) {
                                    tempamount += amountdue;
                                    tempamount1 += amountdue;
                                    tempamountinbase += amountdueinbase;
                                    tempamountinbase1 += amountdueinbase;
                                }
                                if (invobj.has("amountdue_" + cntamountduecount)) {
                                    tempamount += invobj.optDouble("amountdue_" + cntamountduecount);
                                }
                                invobj.put("amountdue_" + cntamountduecount, tempamount);

                                if (invobj.has("amountdueinbase_" + cntamountduecount)) {
                                    tempamountinbase1 += invobj.optDouble("amountdueinbase_" + cntamountduecount);
                                }
                                invobj.put("amountdueinbase_" + cntamountduecount, tempamountinbase1);

                                if (summaryObj.has("amountdue_" + cntamountduecount)) {
                                    tempamount1 += summaryObj.optDouble("amountdue_" + cntamountduecount);
                                }
                                summaryObj.put("amountdue_" + cntamountduecount, tempamount1);
                                if (summaryObj.has("amountdueinbase_" + cntamountduecount)) {
                                    tempamountinbase += summaryObj.optDouble("amountdueinbase_" + cntamountduecount);
                                }
                                summaryObj.put("amountdueinbase_" + cntamountduecount, tempamountinbase);
                                if (!isSummary) {
                                    if ((dueDate1.isAfter(localStartDate) || dueDate1.equals(localStartDate)) && (dueDate1.isBefore(localEndDate) || dueDate1.equals(localEndDate))) {
                                        if (invobj.has("amountdueinbase_" + cntamountduecount)) {
                                            totalinbase += invobj.optDouble("amountdueinbase_" + cntamountduecount);
                                        }
                                        invobj.put("total", totalinbase);
                                        jArr.put(invobj);
                                    }
                                }
                                
                                date = localStartDate.toDateTime(LocalTime.MIDNIGHT);
                                date = date.plusMonths(1);
                                DateTime firstDateOfMonth = date.dayOfMonth().withMinimumValue();
                                lastDateOfMonth = date.dayOfMonth().withMaximumValue();
                                tempDate = new Date();
                                startDate1 = firstDateOfMonth.toDate();
                                cal1 = Calendar.getInstance();
                                d = new Date();
                                cal1.setTime(new Date(df1.format(startDate1)));

                                try {
                                    cal1String = authHandler.getDateOnlyFormat().format(cal1.getTime());
                                    cal1Date = authHandler.getDateOnlyFormat().parse(cal1String);
                                } catch (ParseException ex) {
                                    cal1Date = cal1.getTime();
                                    Logger.getLogger(AccGoodsReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                                }

                                tempDate = cal1Date;
                                localStartDate = new LocalDate(tempDate);
                                endDate1 = lastDateOfMonth.toDate();
                                endcal = Calendar.getInstance();
                                d = new Date();
                                endcal.setTime(new Date(df1.format(endDate1)));

                                try {
                                    endcalString = authHandler.getDateOnlyFormat().format(endcal.getTime());
                                    endcalDate = authHandler.getDateOnlyFormat().parse(endcalString);
                                } catch (ParseException ex) {
                                    endcalDate = endcal.getTime();
                                    Logger.getLogger(AccGoodsReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                                }
                                localEndDate = new LocalDate(endcalDate);
                                cntamountduecount++;
                            }
                        }

//                        if (!isSummary) {
//                            double totalinbase = 0;
//                            double open = 0;
//                            if (invobj.has("opening")) {
//                                open = invobj.optDouble("opening");
//                            }
//                            invobj.put("total", 0);
//                            for (int cnt = 0; cnt < monthCount; cnt++) {
//                                if (invobj.has("amountdueinbase_" + cnt)) {
//                                    totalinbase += invobj.optDouble("amountdueinbase_" + cnt);
//                                }
//                            }
//                            invobj.put("total", totalinbase + open);
//                            jArr.put(invobj);
//                        }
                    }
                    //-------------------------------------Insert Summary Object Data For Vendor Here-------------------
                    if (invjarr.length() > 0 && isSummary) {
                        summaryObj.put("personid", personID);
                        summaryObj.put("personname", personName);
                        summaryObj.put("currencysymbol", currencySymbolCust);
                        summaryObj.put("currencyid", currencyidCust);
                        summaryObj.put("currencyname", currencyNameCust);
                        double total = 0;
                        double opening = 0;
                        if (summaryObj.has("opening")) {
                            opening = summaryObj.optDouble("opening");
                        }
                        double openinginbase = 0;
                        if (summaryObj.has("openinginbase")) {
                            openinginbase = summaryObj.optDouble("openinginbase");
                        }
                        summaryObj.put("total", 0);
                        StringBuilder sb = null;
                        if (isCallFromDD) {
                            for (int cnt = (monthCount - 1); cnt >= 0; cnt--) {
                                sb = new StringBuilder();
                                sb.append("amountdue_");
                                sb.append(cnt);
                                if (summaryObj.has(sb.toString())) {
                                    total += summaryObj.optDouble(sb.toString());
                                    summaryObj.put(sb.toString(), summaryObj.optDouble(sb.toString()));
                                } else {
                                    summaryObj.put(sb.toString(), 0.0);
                                }
                            }
                        } else {
                            for (int cnt = 0; cnt < monthCount; cnt++) {
                                if (summaryObj.has("amountdue_" + cnt)) {
                                    total += summaryObj.optDouble("amountdue_" + cnt);
                                    summaryObj.put("amountdue_" + cnt, summaryObj.optDouble("amountdue_" + cnt));
                                }
                            }
                        }
                        summaryObj.put("total", total + opening);
                        double totalinbase = 0;
                        summaryObj.put("totalinbase", 0);
                        if (isCallFromDD) {
                            for (int cnt = (monthCount - 1); cnt >= 0; cnt--) {
                                sb = new StringBuilder();
                                sb.append("amountdueinbase_");
                                sb.append(cnt);
                                if (summaryObj.has(sb.toString())) {
                                    totalinbase += summaryObj.optDouble(sb.toString());
                                } else {
                                    summaryObj.put(sb.toString(), 0.0);
                                }
                            }
                        } else {
                            for (int cnt = 0; cnt < monthCount; cnt++) {
                                if (summaryObj.has("amountdueinbase_" + cnt)) {
                                    totalinbase += summaryObj.optDouble("amountdueinbase_" + cnt);
                                }
                            }
                        }
                        summaryObj.put("totalinbase", totalinbase + openinginbase);
                        jArr.put(summaryObj);
                    }
                }
            }

            JSONObject monthArrayObject = new JSONObject();
            //----------------------------- the last object would be the months array--------------------------
            localStartDate = new LocalDate(startDate);
            localEndDate = new LocalDate(endDate);
            JSONArray monthArray = new JSONArray();
            JSONObject monthObj;
            ArrayList monthList = new ArrayList();
            while (localStartDate.isBefore(localEndDate)) {
                String monthName = localStartDate.toString("MMM yyyy");
                localStartDate = localStartDate.plus(Period.months(1));
                monthList.add(monthName);
            }
            for (int i = 0; i < monthList.size(); i++) {
                monthObj = new JSONObject();
                monthObj.put("monthname", monthList.get(i));
                monthArray.put(monthObj);
            }
            if (!StringUtil.equal(fileType, "csv") && !StringUtil.equal(fileType, "xls") && !StringUtil.equal(fileType, "print")) {
                monthArrayObject.put("months", monthArray);
                jArr.put(monthArrayObject);
            }
        } catch (Exception ex) {
            Logger.getLogger(AccGoodsReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    
    public Map<String, Object> getMonthlyAgedSummarizedSubReport(HttpServletRequest request, JSONObject jobj,boolean  isSummary) throws ServiceException, JSONException, SessionExpiredException {
        ArrayList<MonthlyTradingProfitLoss> monthlyTradingProfitLossList = new ArrayList<MonthlyTradingProfitLoss>();
        HashMap<String, MonthlyTradingProfitLoss> monthlyTradingProfitLossMap = new HashMap<String, MonthlyTradingProfitLoss>();
        Map<String, Object> monthlyprolossMap = new HashMap<String, Object>();
        KWLCurrency currency = null;
        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yy");
        double finaltotal = 0;
        double finaltotalinBase = 0;
        String companyid = sessionHandlerImpl.getCompanyid(request);
        try {
            JSONArray jArr = jobj.getJSONArray("data");
            for (int count = 0; count < jArr.length() - 1; count++) {
                double totalInTransactionCurr=0;
                JSONObject tempobj = jArr.getJSONObject(count);
                String personName = tempobj.getString("personname");
                MonthlyTradingProfitLoss monthlyTradingProfitLoss = new MonthlyTradingProfitLoss();
                monthlyTradingProfitLoss.setAccName2(personName);
                if (tempobj.has("billno")) {
                    String documentNumber = tempobj.getString("billno");
                    monthlyTradingProfitLoss.setDocnumber(documentNumber);
                }
                if (tempobj.has("date")) {
                    String billDate = df.format(Date.parse(tempobj.getString("date")));
                         monthlyTradingProfitLoss.setBilldate(billDate);
                }
                if (tempobj.has("duedate")) {
                    String dueDate = df.format(Date.parse(tempobj.getString("duedate")));
                     monthlyTradingProfitLoss.setDuedate(dueDate);
                }
                if(tempobj.has("type")){
                   String type = tempobj.getString("type"); 
                   monthlyTradingProfitLoss.setType(type);
                }
                if (tempobj.has("currencyname")) {
                    monthlyTradingProfitLoss.setCurrency(tempobj.optString("currencyname"));
                }
                if (tempobj.has("opening")) {
                    monthlyTradingProfitLoss.setAmount_0(authHandler.formattedCommaSeparatedAmount(tempobj.optDouble("opening"),companyid));
                    totalInTransactionCurr += tempobj.optDouble("opening");
                }   
                if (tempobj.has("amountdue_0")) {
                    monthlyTradingProfitLoss.setAmount_1(authHandler.formattedCommaSeparatedAmount(tempobj.optDouble("amountdue_0"),companyid));
                    totalInTransactionCurr += tempobj.optDouble("amountdue_0",0);
                }
                if (tempobj.has("amountdue_1")) {
                    monthlyTradingProfitLoss.setAmount_2(authHandler.formattedCommaSeparatedAmount(tempobj.optDouble("amountdue_1"),companyid));
                    totalInTransactionCurr += tempobj.optDouble("amountdue_1",0);
                }
                if (tempobj.has("amountdue_2")) {
                    monthlyTradingProfitLoss.setAmount_3(authHandler.formattedCommaSeparatedAmount(tempobj.optDouble("amountdue_2"),companyid));
                    totalInTransactionCurr += tempobj.optDouble("amountdue_2",0);
                }
                if (tempobj.has("amountdue_3")) {
                    monthlyTradingProfitLoss.setAmount_4(authHandler.formattedCommaSeparatedAmount(tempobj.optDouble("amountdue_3"),companyid));
                    totalInTransactionCurr += tempobj.optDouble("amountdue_3",0);
                }
                if (tempobj.has("amountdue_4")) {
                    monthlyTradingProfitLoss.setAmount_5(authHandler.formattedCommaSeparatedAmount(tempobj.optDouble("amountdue_4"),companyid));
                    totalInTransactionCurr += tempobj.optDouble("amountdue_4",0);
                }
                if (tempobj.has("amountdue_5")) {
                    monthlyTradingProfitLoss.setAmount_6(authHandler.formattedCommaSeparatedAmount(tempobj.optDouble("amountdue_5"),companyid));
                    totalInTransactionCurr += tempobj.optDouble("amountdue_5",0);
                }
                if (tempobj.has("amountdue_6")) {
                    monthlyTradingProfitLoss.setAmount_7(authHandler.formattedCommaSeparatedAmount(tempobj.optDouble("amountdue_6"),companyid));
                    totalInTransactionCurr += tempobj.optDouble("amountdue_6",0);
                }
                if (tempobj.has("amountdue_7")) {
                    monthlyTradingProfitLoss.setAmount_8(authHandler.formattedCommaSeparatedAmount(tempobj.optDouble("amountdue_7"),companyid));
                    totalInTransactionCurr += tempobj.optDouble("amountdue_7",0);
                }
                if (tempobj.has("amountdue_8")) {
                    monthlyTradingProfitLoss.setAmount_9(authHandler.formattedCommaSeparatedAmount(tempobj.optDouble("amountdue_8"),companyid));
                    totalInTransactionCurr += tempobj.optDouble("amountdue_8",0);
                }
                if (tempobj.has("amountdue_9")) {
                    monthlyTradingProfitLoss.setAmount_10(authHandler.formattedCommaSeparatedAmount(tempobj.optDouble("amountdue_9"),companyid));
                    totalInTransactionCurr += tempobj.optDouble("amountdue_9",0);
                }
                if (tempobj.has("amountdue_10"))  {
                    monthlyTradingProfitLoss.setAmount_11(authHandler.formattedCommaSeparatedAmount(tempobj.optDouble("amountdue_10"),companyid));
                    totalInTransactionCurr += tempobj.optDouble("amountdue_10",0);
                }
                if (tempobj.has("amountdue_11")) {
                    monthlyTradingProfitLoss.setAmount_12(authHandler.formattedCommaSeparatedAmount(tempobj.optDouble("amountdue_11"),companyid));
                    totalInTransactionCurr += tempobj.optDouble("amountdue_11",0);
                }
                if (!isSummary) {                        // for Total Column in Report View
                    monthlyTradingProfitLoss.setAmount_13(authHandler.formattedCommaSeparatedAmount(totalInTransactionCurr, companyid));
                }
                if (tempobj.has("total") && isSummary) {  // for Total Column in Summary View
                    monthlyTradingProfitLoss.setAmount_13(authHandler.formattedCommaSeparatedAmount(tempobj.optDouble("total"), companyid));
                    finaltotalinBase += tempobj.optDouble("total");
                }
                 if (tempobj.has("total") && !isSummary) { // for TotalInBaseCurrency Column In Report View
                    monthlyTradingProfitLoss.setAmount_14_double(tempobj.optDouble("total"));
                    finaltotalinBase += tempobj.optDouble("total");       
                }
                 if (tempobj.has("totalinbase")) {      //for TotalInBaseCurrency Column In Summary View
                    monthlyTradingProfitLoss.setAmount_14(authHandler.formattedCommaSeparatedAmount(tempobj.optDouble("totalinbase"),companyid));
                    finaltotal += tempobj.optDouble("totalinbase");
                }
//                if (tempobj.has("amountdueinbase")) {// in case of the details report it needs amountdue in base
//                    monthlyTradingProfitLoss.setAmount_14(tempobj.optDouble("amountdueinbase"));
//                    finaltotal += tempobj.optDouble("amountdueinbase");
//                }
                monthlyTradingProfitLossList.add(monthlyTradingProfitLoss);
            }
            monthlyprolossMap.put("finaltotal", isSummary?authHandler.formattedCommaSeparatedAmount(finaltotal,companyid):authHandler.formattedCommaSeparatedAmount(finaltotalinBase,companyid));
            monthlyprolossMap.put("MonthlyAgeingSummarizedSubReportData", new JRBeanCollectionDataSource(monthlyTradingProfitLossList));
        } catch (Exception ex) {
            Logger.getLogger(AccGoodsReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getMonthlyBalanceSheetJasper : " + ex.getMessage(), ex);
        }
        return monthlyprolossMap;
    }
    
    @Override
    public void exportMonthlyAgedPayableSummarized(HttpServletRequest request, HttpServletResponse response, JSONObject jobj) throws DocumentException, ServiceException, IOException {
         Map<String, Object> financeDetailsMap = new HashMap<String, Object>();
        String view = "MonthlyAgedReceivable";
        FinanceDetails financeDetails = new FinanceDetails();
        ArrayList<FinanceDetails> financeDetailsList = new ArrayList<FinanceDetails>();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            financeDetails.setName(company.getCompanyName());
            financeDetails.setEmail(company.getEmailID() != null ? company.getEmailID() : "");
            financeDetails.setFax(company.getFaxNumber() != null ? company.getFaxNumber() : "");
            financeDetails.setPhone(company.getPhoneNumber() != null ? company.getPhoneNumber() : "");
            financeDetails.setCurrencyinword(company.getCurrency() != null ? company.getCurrency().getName() : "");
            SimpleDateFormat df = new SimpleDateFormat(Constants.ddMMyyyy);
            String endDate1 = request.getParameter("enddate");
            String startDate1 = request.getParameter("startdate");
            String date = "From " + startDate1 + " To " + endDate1;
            financeDetails.setReportname("Monthly Ageing Analysis -Vendor-Summarised");
            financeDetails.setCustven("Vendor Name");
            financeDetails.setDateRange(date);
           
            if (!StringUtil.isNullOrEmpty(request.getParameter("asofdate")) && !request.getParameter("asofdate").equals("undefined")) {
                String asOfDate = df.format(authHandler.getGlobalDateFormat().parse(request.getParameter("asofdate")));
                financeDetails.setAsOfDate(asOfDate);
            }

            if (!StringUtil.isNullOrEmpty(request.getParameter("datefilter")) && !request.getParameter("datefilter").equals("undefined")) {
                String agedOn = "";
                if (Integer.parseInt(request.getParameter("datefilter")) == Constants.agedDueDate1to30Filter) {
                    agedOn += Constants.agedDueDate1to30Days;
                } else if (Integer.parseInt(request.getParameter("datefilter")) == Constants.agedInvoiceDateFilter) {
                    agedOn += Constants.agedInvoiceDate;
                } else if (Integer.parseInt(request.getParameter("datefilter")) == Constants.agedInvoiceDate0to30Filter) {
                    agedOn += Constants.agedInvoiceDate0to30;
                } else {
                    agedOn += Constants.agedDueDate0to30Days;
                }
                financeDetails.setAged_On(agedOn);
            }
                         
            JSONArray jmonth = jobj.getJSONArray("months");
            for (int i = 0; i < jmonth.length(); i++) {
                JSONObject getmon = jmonth.getJSONObject(i);
                switch (i) {
                    case 0:
                        financeDetails.setMonth_0(getmon.getString("monthname"));
                        break;
                    case 1:
                        financeDetails.setMonth_1(getmon.getString("monthname"));
                        break;
                    case 2:
                        financeDetails.setMonth_2(getmon.getString("monthname"));
                        break;
                    case 3:
                        financeDetails.setMonth_3(getmon.getString("monthname"));
                        break;
                    case 4:
                        financeDetails.setMonth_4(getmon.getString("monthname"));
                        break;
                    case 5:
                        financeDetails.setMonth_5(getmon.getString("monthname"));
                        break;
                    case 6:
                        financeDetails.setMonth_6(getmon.getString("monthname"));
                        break;
                    case 7:
                        financeDetails.setMonth_7(getmon.getString("monthname"));
                        break;
                    case 8:
                        financeDetails.setMonth_8(getmon.getString("monthname"));
                        break;
                    case 9:
                        financeDetails.setMonth_9(getmon.getString("monthname"));
                        break;
                    case 10:
                        financeDetails.setMonth_10(getmon.getString("monthname"));
                        break;
                    case 11:
                        financeDetails.setMonth_11(getmon.getString("monthname"));
                        break;
                }
            }
            financeDetails.setTotal("Total");
            financeDetails.setTotalinbase("Total In Base Currency");
            financeDetailsMap = getMonthlyAgedSummarizedSubReport(request, jobj,true);
            financeDetailsList.add(financeDetails);
            financeDetailsMap.put("datasource", new JRBeanCollectionDataSource(financeDetailsList));
            financeDetailsMap.put("basecurr", company.getCurrency() != null ? company.getCurrency().getName() : "");
            InputStream inputStreamSubReport = new FileInputStream(request.getSession().getServletContext().getRealPath("jrxml") + "/MonthlyAgeingSummarizedSubReport.jrxml");
            JasperDesign jasperDesignSubReport = JRXmlLoader.load(inputStreamSubReport);
            JasperReport jasperReportSubReport = JasperCompileManager.compileReport(jasperDesignSubReport);
            financeDetailsMap.put("MonthlyAgeingSummarizedSubReport", jasperReportSubReport);

            InputStream inputStream = new FileInputStream(request.getSession().getServletContext().getRealPath("jrxml") + "/MonthlyAgeingSummarized.jrxml");
            JasperDesign jasperDesign = JRXmlLoader.load(inputStream);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

            JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(financeDetailsList);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, financeDetailsMap, beanColDataSource);
            response.setHeader("Content-Disposition", "attachment;filename=" + "MonthlyAgedPayable_v1.pdf");
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
        } catch (Exception ex) {
            Logger.getLogger(AccGoodsReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
     public void exportMonthlyAgedPayableDetails(HttpServletRequest request, HttpServletResponse response, JSONObject jobj) throws DocumentException, ServiceException, IOException {
        Map<String, Object> financeDetailsMap = new HashMap<String, Object>();
        String view = "MonthlyAgedPayable";
        FinanceDetails financeDetails = new FinanceDetails();
        ArrayList<FinanceDetails> financeDetailsList = new ArrayList<FinanceDetails>();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            financeDetails.setName(company.getCompanyName());
            financeDetails.setEmail(company.getEmailID() != null ? company.getEmailID() : "");
            financeDetails.setFax(company.getFaxNumber() != null ? company.getFaxNumber() : "");
            financeDetails.setPhone(company.getPhoneNumber() != null ? company.getPhoneNumber() : "");
            financeDetails.setCurrencyinword(company.getCurrency() != null ? company.getCurrency().getName() : "");
            SimpleDateFormat df = new SimpleDateFormat(Constants.ddMMyyyy);
            String endDate1 = request.getParameter("enddate");
            String startDate1 = request.getParameter("startdate");
            String date = "From " + startDate1 + " To " + endDate1;
            financeDetails.setReportname("Monthly Ageing Analysis -Vendor-Details");
            financeDetails.setCustven("Vendor Name");
            financeDetails.setDateRange(date);
            if (!StringUtil.isNullOrEmpty(request.getParameter("asofdate")) && !request.getParameter("asofdate").equals("undefined")) {
                String asOfDate = df.format(authHandler.getGlobalDateFormat().parse(request.getParameter("asofdate")));
                financeDetails.setAsOfDate(asOfDate);
            }
            if (!StringUtil.isNullOrEmpty(request.getParameter("datefilter")) && !request.getParameter("datefilter").equals("undefined")) {
                String agedOn = "";
                if (Integer.parseInt(request.getParameter("datefilter")) == Constants.agedDueDate1to30Filter) {
                    agedOn += Constants.agedDueDate1to30Days;
                } else if (Integer.parseInt(request.getParameter("datefilter")) == Constants.agedInvoiceDateFilter) {
                    agedOn += Constants.agedInvoiceDate;
                } else if (Integer.parseInt(request.getParameter("datefilter")) == Constants.agedInvoiceDate0to30Filter) {
                    agedOn += Constants.agedInvoiceDate0to30;
                } else {
                    agedOn += Constants.agedDueDate0to30Days;
                }
                financeDetails.setAged_On(agedOn);
            }
            JSONArray jmonth = jobj.getJSONArray("months");
            for (int i = 0; i < jmonth.length(); i++) {
                JSONObject getmon = jmonth.getJSONObject(i);
                switch (i) {
                    case 0:
                        financeDetails.setMonth_0(getmon.getString("monthname"));
                        break;
                    case 1:
                        financeDetails.setMonth_1(getmon.getString("monthname"));
                        break;
                    case 2:
                        financeDetails.setMonth_2(getmon.getString("monthname"));
                        break;
                    case 3:
                        financeDetails.setMonth_3(getmon.getString("monthname"));
                        break;
                    case 4:
                        financeDetails.setMonth_4(getmon.getString("monthname"));
                        break;
                    case 5:
                        financeDetails.setMonth_5(getmon.getString("monthname"));
                        break;
                    case 6:
                        financeDetails.setMonth_6(getmon.getString("monthname"));
                        break;
                    case 7:
                        financeDetails.setMonth_7(getmon.getString("monthname"));
                        break;
                    case 8:
                        financeDetails.setMonth_8(getmon.getString("monthname"));
                        break;
                    case 9:
                        financeDetails.setMonth_9(getmon.getString("monthname"));
                        break;
                    case 10:
                        financeDetails.setMonth_10(getmon.getString("monthname"));
                        break;
                    case 11:
                        financeDetails.setMonth_11(getmon.getString("monthname"));
                        break;
                }
            }
            financeDetails.setTotal("Total");
            financeDetails.setTotalinbase("Total In Base Currency");
            financeDetailsMap = getMonthlyAgedSummarizedSubReport(request, jobj,false);
            financeDetailsList.add(financeDetails);
            financeDetailsMap.put("datasource", new JRBeanCollectionDataSource(financeDetailsList));
            financeDetailsMap.put("basecurr", company.getCurrency() != null ? company.getCurrency().getName() : "");
            InputStream inputStreamSubReport = new FileInputStream(request.getSession().getServletContext().getRealPath("jrxml") + "/MonthlyAgeingReportSubReport.jrxml");
            JasperDesign jasperDesignSubReport = JRXmlLoader.load(inputStreamSubReport);
            JasperReport jasperReportSubReport = JasperCompileManager.compileReport(jasperDesignSubReport);
            financeDetailsMap.put("MonthlyAgeingSummarizedSubReport", jasperReportSubReport);

            InputStream inputStream = new FileInputStream(request.getSession().getServletContext().getRealPath("jrxml") + "/MonthlyAgeingReport.jrxml");
            JasperDesign jasperDesign = JRXmlLoader.load(inputStream);
            JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

            JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(financeDetailsList);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, financeDetailsMap, beanColDataSource);
            response.setHeader("Content-Disposition", "attachment;filename=" + "MonthlyAgedPayable_v1.pdf");
            JRPdfExporter exp = new JRPdfExporter();
            exp.setParameter(JRExporterParameter.JASPER_PRINT, jasperPrint);
            exp.setParameter(JRPdfExporterParameter.OUTPUT_STREAM, response.getOutputStream());
            exp.exportReport();
        } catch (Exception ex) {
            Logger.getLogger(AccGoodsReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public JSONArray getVendorPartyLedgerSummary(HttpServletRequest request, HashMap requestParams) throws SessionExpiredException, ServiceException {
        JSONObject jObj = new JSONObject();
        JSONArray jArr = new JSONArray();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            requestParams.put("companyid",companyid);
            String curDateString = (!StringUtil.isNullOrEmpty(request.getParameter(GoodsReceiptCMNConstants.CURDATE))) ? request.getParameter(GoodsReceiptCMNConstants.CURDATE) : (request.getParameter("stdate") != null ? request.getParameter("stdate") : request.getParameter("startdate"));
            DateFormat df = authHandler.getDateOnlyFormat(request);
            boolean checkForEx = false;
            if (StringUtil.getBoolean(request.getParameter("checkforex"))) {
                checkForEx = StringUtil.getBoolean(request.getParameter("checkforex"));
            }
            double d_open_amount_base = 0;
            double c_open_amount_base = 0;
            double c_amount_base = 0;
            double d_amount_base = 0;
            double balance_base = 0;
            double amountinbase = 0;
            KwlReturnObject result = accVendorDAOobj.getVendorForAgedPayable(requestParams);
            Iterator itrcust = result.getEntityList().iterator();

            if (requestParams.containsKey("start")) { //Removing paging here because it is applying on each vendor data (CN/DN etc). Due to this complete amount due not showing in AP Summary.  
                requestParams.remove("start");
            }
            if (requestParams.containsKey("limit")) {
                requestParams.remove("limit");
            }
            String start = "";
            int previousStart = 0;
            int limit = 0;
            String firstCustomerId = "";
            String lastCustomerId = "";
            int skipRecords = 1;
            if (!checkForEx) {
                if (!StringUtil.isNullOrEmpty(request.getParameter("start"))) {
                    start = request.getParameter("start");
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("limit"))) {
                    limit = Integer.parseInt(request.getParameter("limit"));
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("lastcustomerid"))) {
                    lastCustomerId = request.getParameter("lastcustomerid");
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("fCustomerId"))) {
                    if (Integer.parseInt(start) == 0) {
                        firstCustomerId = "";
                    } else {
                        firstCustomerId = request.getParameter("fCustomerId");
                    }
                }
                if (!StringUtil.isNullOrEmpty(request.getParameter("previousStart"))) {
                    previousStart = Integer.parseInt(request.getParameter("previousStart"));
                }
            }
            while (itrcust.hasNext()) {
                String personID = null;
                String personName = null;
                String aliasname = "";
                String currencySymbol = null;
                String currencyid = null;
                String code = "";
                boolean isDN = false;
                boolean isMP = false;
                d_open_amount_base = c_open_amount_base = 0;
                d_amount_base = c_amount_base = 0;
                balance_base = 0;
                Object venid = itrcust.next();
                requestParams.put(GoodsReceiptCMNConstants.VENDORID, venid);
                requestParams.put(GoodsReceiptCMNConstants.ACCID, venid);
                requestParams.put("cntype", null);
                requestParams.put("isAgedSummary", true);
                KwlReturnObject objItr = accountingHandlerDAOobj.getObject(Vendor.class.getName(), venid.toString());
                Vendor vendor = (Vendor) objItr.getEntityList().get(0);
                String currencyidVen = vendor.getAccount().getCurrency().getCurrencyID();
                String currencySymbolVen = vendor.getAccount().getCurrency().getSymbol();
                String currencyName = vendor.getAccount().getCurrency().getName();
                JSONArray invjarr = new JSONArray();
                result = accGoodsReceiptobj.getGoodsReceiptsMerged(requestParams);
                invjarr = accGoodsReceiptServiceHandler.getGoodsReceiptsJsonMerged(requestParams, result.getEntityList(), invjarr, accountingHandlerDAOobj, accCurrencyDAOobj, accGoodsReceiptobj, accAccountDAOobj, accGoodsReceiptCommon, accTaxObj);

                JSONArray OBJArryInvoice = new JSONArray();
                result = accGoodsReceiptobj.getOpeningBalanceInvoices(requestParams);
                OBJArryInvoice = AccGoodsReceiptServiceHandler.getAgedOpeningBalanceInvoiceJson(requestParams, result.getEntityList(), OBJArryInvoice, accCurrencyDAOobj, accountingHandlerDAOobj, accGoodsReceiptCommon, accAccountDAOobj, fieldDataManagercntrl);
                for (int i = 0; i < OBJArryInvoice.length(); i++) {
                    invjarr.put(OBJArryInvoice.get(i));
                }

                JSONArray OBJArryDebitNote = new JSONArray();
                result = accDebitNoteobj.getOpeningBalanceDNs(requestParams);
                OBJArryDebitNote = AccGoodsReceiptServiceHandler.getAgedOpeningBalanceDebitNoteJson(requestParams, result.getEntityList(), OBJArryDebitNote, accCurrencyDAOobj, accountingHandlerDAOobj, accReceiptDAOobj,accDebitNoteobj,accAccountDAOobj, fieldDataManagercntrl);
                for (int i = 0; i < OBJArryDebitNote.length(); i++) {
                    invjarr.put(OBJArryDebitNote.get(i));
                }

                JSONArray OBJArryCreditNote = new JSONArray();
                result = accCreditNoteDAOobj.getOpeningBalanceVendorCNs(requestParams);
                OBJArryCreditNote = AccGoodsReceiptServiceHandler.getAgedOpeningBalanceCreditNoteJson(requestParams, result.getEntityList(), OBJArryCreditNote, accCurrencyDAOobj, accountingHandlerDAOobj, accPaymentDAOobj,accVendorPaymentobj,accDebitNoteobj,accAccountDAOobj, fieldDataManagercntrl);
                for (int i = 0; i < OBJArryCreditNote.length(); i++) {
                    invjarr.put(OBJArryCreditNote.get(i));
                }

                JSONArray OBJArryPayment = new JSONArray();
                result = accVendorPaymentobj.getOpeningBalancePayments(requestParams);
                OBJArryPayment = AccGoodsReceiptServiceHandler.getAgedOpeningBalancePaymentJson(requestParams, result.getEntityList(), OBJArryPayment, accCurrencyDAOobj, accountingHandlerDAOobj, accVendorPaymentobj, accAccountDAOobj, fieldDataManagercntrl);
                for (int i = 0; i < OBJArryPayment.length(); i++) {
                    invjarr.put(OBJArryPayment.get(i));
                }

                JSONArray DebitNotejArr = new JSONArray();//This is used for getting DN gainst vendor and otherwise 
                result = accDebitNoteobj.getDebitNoteMerged(requestParams);
                DebitNotejArr = AccGoodsReceiptServiceHandler.getDebitNotesMergedJson(requestParams, result.getEntityList(), DebitNotejArr, accCurrencyDAOobj, accountingHandlerDAOobj, accReceiptDAOobj,accDebitNoteobj,accAccountDAOobj, fieldDataManagercntrl, accJournalEntryobj);
                for (int i = 0; i < DebitNotejArr.length(); i++) {
                    invjarr.put(DebitNotejArr.get(i));
                }

                requestParams.put("cntype", 4);//This is used for getting Credit note against vendor 
                JSONArray CreditNotejArr = new JSONArray();
                result = accCreditNoteDAOobj.getCreditNoteMerged(requestParams);
                CreditNotejArr = AccGoodsReceiptServiceHandler.getCreditNotesMergedJson(requestParams, result.getEntityList(), CreditNotejArr, accCurrencyDAOobj, accountingHandlerDAOobj, accPaymentDAOobj, accVendorPaymentobj,accDebitNoteobj,accAccountDAOobj, fieldDataManagercntrl, accJournalEntryobj);
                for (int i = 0; i < CreditNotejArr.length(); i++) {
                    invjarr.put(CreditNotejArr.get(i));
                }
                requestParams.remove("cntype");
                
                JSONArray makePaymentJArr = new JSONArray();
                requestParams.put("paymentWindowType", 1);//Payment to Vendor record
                result = accVendorPaymentobj.getPayments(requestParams);
                makePaymentJArr = AccGoodsReceiptServiceHandler.getPaymentsJson(requestParams, result.getEntityList(), makePaymentJArr, accCurrencyDAOobj, accountingHandlerDAOobj, accVendorPaymentobj);
                for (int i = 0; i < makePaymentJArr.length(); i++) {
                    invjarr.put(makePaymentJArr.get(i));
                }

                requestParams.put("allAdvPayment", true); // needs only refund type record so that putted true
                requestParams.put("paymentWindowType", 2);//Receipt to Vendor record
                JSONArray receivePaymentJArr = new JSONArray();
                result = accReceiptDAOobj.getReceipts(requestParams);
                receivePaymentJArr = AccInvoiceServiceHandler.getReceiptsJson(requestParams, result.getEntityList(), receivePaymentJArr, accountingHandlerDAOobj, authHandlerDAOObj, accCurrencyDAOobj, accReceiptDAOobj, request, accAccountDAOobj, fieldDataManagercntrl, accJournalEntryobj);
                for (int i = 0; i < receivePaymentJArr.length(); i++) {
                    invjarr.put(receivePaymentJArr.get(i));
                }
                requestParams.remove("allAdvPayment");
                requestParams.remove("paymentWindowType");
                
                for (int i = 0; i < invjarr.length(); i++) {
                    JSONObject invobj = invjarr.getJSONObject(i);
                    personID = invobj.getString(GoodsReceiptCMNConstants.PERSONID);
                    personName = invobj.getString(GoodsReceiptCMNConstants.PERSONNAME);
                    aliasname = invobj.has(GoodsReceiptCMNConstants.ALIASNAME) ? (invobj.getString(GoodsReceiptCMNConstants.ALIASNAME) != null ? invobj.getString(GoodsReceiptCMNConstants.ALIASNAME) : "") : "";
                    currencySymbol = invobj.getString(GoodsReceiptCMNConstants.CURRENCYSYMBOL);
                    currencyid = invobj.getString(GoodsReceiptCMNConstants.CURRENCYID);
                    amountinbase = invobj.getDouble("amountinbase");
                    code = (invobj.has("code")) ? invobj.getString("code") : "";
                    isDN = invobj.has("isDN") ? invobj.getBoolean("isDN") : false;
                    isMP = invobj.has("isMP") ? invobj.getBoolean("isMP") : false;

                    boolean isopeningBalanceTransaction = invobj.optBoolean(GoodsReceiptCMNConstants.IsopeningBalanceTransaction, false);

                    if (isDN || isMP) {
                        if (isopeningBalanceTransaction) {
                            d_open_amount_base += amountinbase;
                        } else {
                            d_amount_base += amountinbase;
                        }
                    } else {
                        if (isopeningBalanceTransaction) {
                            c_open_amount_base += amountinbase;
                        } else {
                            c_amount_base += amountinbase;
                        }
                    }
                }
                if (invjarr.length() > 0) {
                    jObj = new JSONObject();
                    jObj.put(GoodsReceiptCMNConstants.PERSONID, personID);
                    jObj.put(GoodsReceiptCMNConstants.PERSONNAME, personName);
                    jObj.put(GoodsReceiptCMNConstants.ALIASNAME, aliasname);
                    jObj.put(GoodsReceiptCMNConstants.CURRENCYID, currencyidVen);
                    jObj.put(GoodsReceiptCMNConstants.CURRENCYNAME, currencyName);
                    jObj.put("code", code);
                    jObj.put("d_open_amount_base", Double.valueOf(authHandler.formattedAmount(d_open_amount_base, companyid)));
                    jObj.put("c_open_amount_base", Double.valueOf(authHandler.formattedAmount(c_open_amount_base, companyid)));
                    jObj.put("d_amount_base", Double.valueOf(authHandler.formattedAmount(d_amount_base, companyid)));
                    jObj.put("c_amount_base", Double.valueOf(authHandler.formattedAmount(c_amount_base, companyid)));
                    double total = (d_open_amount_base + d_amount_base) - (c_open_amount_base + c_amount_base);
                    balance_base = Double.valueOf(authHandler.formattedAmount(total, companyid));
                    jObj.put("balance_base", balance_base);
                    jArr.put(jObj);
                }
            }
        } catch (JSONException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getVendorAgedPayable : " + ex.getMessage(), ex);
        } catch (ServiceException ex) {
            Logger.getLogger(accGoodsReceiptController.class.getName()).log(Level.SEVERE, null, ex);
            throw ServiceException.FAILURE("getVendorAgedPayable : " + ex.getMessage(), ex);
        }
        return jArr;
    }

    @Override
    public List unlinkDeditNoteFromPurchaseInvoice(HttpServletRequest request, List<DebitNoteDetail> details, String dnid) throws ServiceException, SessionExpiredException {
        List result = new ArrayList();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject cnKWLObj = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), dnid);
            DebitNote dn = (DebitNote) cnKWLObj.getEntityList().get(0);
            String dnnumber = dn.getDebitNoteNumber();
            boolean allInvoicesUnlinked = false;
            Set<DebitNoteDetail> dnd = dn.getRows();
            if (dnd != null && dnd.size() == details.size()) {
                allInvoicesUnlinked = true;
            }
            double dnExternalCurrencyRate = 1d;
            boolean isopeningBalanceDN = dn.isIsOpeningBalenceDN();
            Date dnCreationDate = null;
            dnCreationDate = dn.getCreationDate();
            if (isopeningBalanceDN) {
                dnExternalCurrencyRate = dn.getExchangeRateForOpeningTransaction();
            } else {
//                dnCreationDate = dn.getJournalEntry().getEntryDate();
                dnExternalCurrencyRate = dn.getJournalEntry().getExternalCurrencyRate();
            }
            Double totalAmountUsedByInvoices = 0.0;
            String unlinkedDetailIDs = "";
            //update invoice amount due after unlinking
            for (DebitNoteDetail debitNoteDetail : details) {
                if (debitNoteDetail.getGoodsReceipt() != null && !debitNoteDetail.getGoodsReceipt().isNormalInvoice() && debitNoteDetail.getGoodsReceipt().isIsOpeningBalenceInvoice()) {
                    double amountPaid = debitNoteDetail.getDiscount().getAmountinInvCurrency();
                    totalAmountUsedByInvoices += debitNoteDetail.getDiscount().getDiscount();
                    GoodsReceipt invObj = debitNoteDetail.getGoodsReceipt();
                    HashMap<String, Object> requestParams = new HashMap();
                    requestParams.put(Constants.companyid, companyid);
                    requestParams.put("gcurrencyid", invObj.getCompany().getCurrency().getCurrencyID());
                    double externalCurrencyRate = 0d;
                    externalCurrencyRate = invObj.getExchangeRateForOpeningTransaction();
                    String fromcurrencyid = invObj.getCurrency().getCurrencyID();
                    KwlReturnObject bAmt = null;
                    if (invObj.isConversionRateFromCurrencyToBase()) {
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, amountPaid, fromcurrencyid, invObj.getCreationDate(), externalCurrencyRate);
                    } else {
                        bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountPaid, fromcurrencyid, invObj.getCreationDate(), externalCurrencyRate);
                    }
                    double totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);
                    double invoiceAmountDue = invObj.getOpeningBalanceAmountDue();
                    invoiceAmountDue += amountPaid;
                     Map<String, Object> greceipthm = new HashMap<String, Object>();
                    greceipthm.put("grid", invObj.getID());
                    greceipthm.put("companyid", companyid);
                    greceipthm.put("openingBalanceAmountDue", invoiceAmountDue);
                    greceipthm.put(Constants.openingBalanceBaseAmountDue, invObj.getOpeningBalanceBaseAmountDue() + totalBaseAmountDue);
                    if (invoiceAmountDue != 0) {
                        greceipthm.put("amountduedate", "");
                    }
                    accGoodsReceiptobj.updateGoodsReceipt(greceipthm);
                    unlinkedDetailIDs = unlinkedDetailIDs.concat("'").concat(debitNoteDetail.getID()).concat("',");
                } else if (debitNoteDetail.getGoodsReceipt() != null && debitNoteDetail.getGoodsReceipt().isNormalInvoice() && !debitNoteDetail.getGoodsReceipt().isIsOpeningBalenceInvoice()) {
                    double amountPaid = debitNoteDetail.getDiscount().getAmountinInvCurrency();
                    totalAmountUsedByInvoices += debitNoteDetail.getDiscount().getDiscount();
                    GoodsReceipt invoice = debitNoteDetail.getGoodsReceipt();
                    double invoiceAmountDue = invoice.getInvoiceamountdue();
                    invoiceAmountDue += amountPaid;
                    KwlReturnObject bAmt = null;
                    HashMap<String, Object> requestParams = new HashMap();
                    requestParams.put(Constants.companyid, companyid);
                    requestParams.put(Constants.globalCurrencyKey, invoice.getCompany().getCurrency().getCurrencyID());
                    double externalCurrencyRate = 0d;
                    externalCurrencyRate = invoice.getJournalEntry() != null ? invoice.getJournalEntry().getExternalCurrencyRate() : 1;
                    String fromcurrencyid = invoice.getCurrency().getCurrencyID();
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, amountPaid, fromcurrencyid, invoice.getCreationDate(), externalCurrencyRate);
                    double totalBaseAmountDue = authHandler.round((Double) bAmt.getEntityList().get(0),companyid);
                    double invoiceAmountDueInBase = invoice.getInvoiceAmountDueInBase();
                    invoiceAmountDueInBase += totalBaseAmountDue;
                    Map<String, Object> greceipthm = new HashMap<String, Object>();
                    greceipthm.put("grid", invoice.getID());
                    greceipthm.put("companyid", companyid);
                    greceipthm.put(Constants.invoiceamountdue, invoiceAmountDue);
                    greceipthm.put(Constants.invoiceamountdueinbase, invoiceAmountDueInBase);
                    if (invoiceAmountDue != 0) {
                        greceipthm.put("amountduedate", "");
                    }
                   accGoodsReceiptobj.updateGoodsReceipt(greceipthm);
                    unlinkedDetailIDs = unlinkedDetailIDs.concat("'").concat(debitNoteDetail.getID()).concat("',");
                }
            }

            // Update debit note details
            HashMap<String, Object> credithm = new HashMap();
            Double cnAmountDue = dn.getOpeningBalanceAmountDue();
            cnAmountDue = cnAmountDue + totalAmountUsedByInvoices;
            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            requestParams.put(Constants.companyid, sessionHandlerImpl.getCompanyid(request));
            requestParams.put("gcurrencyid", sessionHandlerImpl.getCurrencyID(request));
            String fromcurrencyid = dn.getCurrency().getCurrencyID();
            KwlReturnObject bAmt = null;
            if (isopeningBalanceDN && dn.isConversionRateFromCurrencyToBase()) {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, cnAmountDue, fromcurrencyid, dnCreationDate, dnExternalCurrencyRate);
            } else {
                bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, cnAmountDue, fromcurrencyid, dnCreationDate, dnExternalCurrencyRate);
            }
            double totalBaseAmountDue = (Double) bAmt.getEntityList().get(0);

            credithm.put("dnid", dn.getID());
            credithm.put("dnamountdue", cnAmountDue);
            credithm.put("openingBalanceAmountDue", cnAmountDue);
            credithm.put(Constants.openingBalanceBaseAmountDue, totalBaseAmountDue);
            credithm.put("openflag", (cnAmountDue) <= 0 ? false : true);
            if (!StringUtil.isNullOrEmpty(unlinkedDetailIDs)) {
                accDebitNoteobj.deleteSelectedLinkedInvoices(dn.getID(), "", companyid,unlinkedDetailIDs.substring(0, unlinkedDetailIDs.length() - 1));
            }
            // If all invoices linked to DN are un-linked, created one entry for DN details 
            HashSet<DebitNoteDetail> dndetails = new HashSet<DebitNoteDetail>();
            if (allInvoicesUnlinked) {
                getDNDetails(dndetails, companyid);
                for (DebitNoteDetail dndetail : dndetails) {
                    dndetail.setDebitNote(dn);
                }
                credithm.put("dnid", dn.getID());
                credithm.put("dndetails", dndetails);
            }
     
            /*
             * Deleting linking information of Debit Note while unlinking transaction
             */
            accDebitNoteobj.deleteLinkingInformationOfDN(credithm);
            accDebitNoteobj.updateDebitNote(credithm);
            result.add(dnnumber);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(AccGoodsReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    
    private void getDNDetails(HashSet<DebitNoteDetail> dndetails, String companyId) throws ServiceException {
        KwlReturnObject result = accountingHandlerDAOobj.getObject(Company.class.getName(), companyId);
        Company company = (Company) result.getEntityList().get(0);
        DebitNoteDetail row = new DebitNoteDetail();
        String CreditNoteDetailID = StringUtil.generateUUID();
        row.setID(CreditNoteDetailID);
        row.setSrno(1);
        row.setTotalDiscount(0.00);
        row.setCompany(company);
        row.setMemo("");
        dndetails.add(row);
    }

    @Override
    public List unlinkPaymentFromInvoice(HttpServletRequest request, List<LinkDetailPayment> details, String paymentid) throws ServiceException, SessionExpiredException {
        List result = new ArrayList();
        try {
            String companyid = sessionHandlerImpl.getCompanyid(request);
            KwlReturnObject cmpresult = accountingHandlerDAOobj.getObject(Company.class.getName(), companyid);
            Company company = (Company) cmpresult.getEntityList().get(0);
            KwlReturnObject receiptKWLObj = accountingHandlerDAOobj.getObject(Payment.class.getName(), paymentid);
            Payment receipt = (Payment) receiptKWLObj.getEntityList().get(0);
            String paymentno = receipt.getPaymentNumber();
            double receiptexternalCurrencyRate = 1d;
            boolean isopeningBalanceMP = receipt.isIsOpeningBalencePayment();
            Date mpCreationDate = null;
            mpCreationDate = receipt.getCreationDate();
            if (isopeningBalanceMP) {
                receiptexternalCurrencyRate = receipt.getExchangeRateForOpeningTransaction();
            } else {
//                mpCreationDate = receipt.getJournalEntry().getEntryDate();
                receiptexternalCurrencyRate = receipt.getJournalEntry().getExternalCurrencyRate();
            }
            String fromcurrencyid = receipt.getCurrency().getCurrencyID();
            String unlinkedDetailIDs = "";
            double sumOfTotalAmount = 0;
            List<GoodsReceipt> invoiceList = new ArrayList<GoodsReceipt>();
//            List<LinkDetailReceipt> details = accReceiptDAOobj.getDeletedLinkedReceiptInvoices(receipt, linkedDetailInvoice, companyid);
            for (LinkDetailPayment receiptDetail : details) {
                GoodsReceipt invoice = receiptDetail.getGoodsReceipt();
                boolean isOpeningInvoice = invoice.isIsOpeningBalenceInvoice();
                if (invoice.isNormalInvoice()) {
                    double amountdue = invoice.getInvoiceamountdue();
                    /*
                     * set status flag for amount due
                     */
                    double amountdueforstatus = amountdue + receiptDetail.getAmountInGrCurrency();
                    if (authHandler.round(amountdueforstatus, companyid) <= 0) {
                        invoice.setIsOpenPayment(false);
                    } else {
                        invoice.setIsOpenPayment(true);
                    }
                    invoice.setInvoiceamountdue(amountdue + receiptDetail.getAmountInGrCurrency());
                    if ((amountdue + receiptDetail.getAmountInGrCurrency()) != 0) {
                        invoice.setAmountDueDate(null);
                    }
                } else if (isOpeningInvoice) {
                    double amountdue = invoice.getOpeningBalanceAmountDue();
                    /*
                     * set status flag for opening invoices
                     */
                    double amountdueforstatus = amountdue + receiptDetail.getAmountInGrCurrency();
                    if (authHandler.round(amountdueforstatus, companyid) <= 0) {
                        invoice.setIsOpenPayment(false);
                    } else {
                        invoice.setIsOpenPayment(true);
                    }
                    invoice.setOpeningBalanceAmountDue(amountdue + receiptDetail.getAmountInGrCurrency());
                    if ((amountdue + receiptDetail.getAmountInGrCurrency()) != 0) {
                        invoice.setAmountDueDate(null);
                    }
                }
                
                double externalCurrencyRateForLinking = 1d;
                externalCurrencyRateForLinking = receiptDetail.getExchangeRateForTransaction();
                sumOfTotalAmount += receiptDetail.getAmount();

                //Converting amount in invoice currency
                double ammountInGRCurrency;
                if (externalCurrencyRateForLinking != 0) {
                    ammountInGRCurrency = authHandler.round(receiptDetail.getAmount() / externalCurrencyRateForLinking, companyid);
                } else {
                    ammountInGRCurrency = receiptDetail.getAmountInGrCurrency();
                }

                //Converting Invoice amount in Base currency accrding to invoice exchange rate
//                Date invoiceCreationdate = isOpeningInvoice ? invoice.getCreationDate() : invoice.getJournalEntry().getEntryDate();
                Date invoiceCreationdate = invoice.getCreationDate();
                double externalCurrencyRate = isOpeningInvoice ? invoice.getExchangeRateForOpeningTransaction() : invoice.getJournalEntry().getExternalCurrencyRate();
                HashMap<String, Object> requestParams = new HashMap();
                requestParams.put(Constants.companyid, companyid);
                requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());
                KwlReturnObject bAmt = null;
                
                if (isOpeningInvoice && invoice.isConversionRateFromCurrencyToBase()) {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, ammountInGRCurrency, invoice.getCurrency().getCurrencyID(), invoiceCreationdate, externalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, ammountInGRCurrency, invoice.getCurrency().getCurrencyID(), invoiceCreationdate, externalCurrencyRate);
                }
                double amountReceivedConvertedInInvoiceBaseCurrency = authHandler.round((Double) bAmt.getEntityList().get(0),companyid);
                
                if (isOpeningInvoice) {
                    invoice.setOpeningBalanceBaseAmountDue(invoice.getOpeningBalanceBaseAmountDue() + amountReceivedConvertedInInvoiceBaseCurrency);
                } else {
                    invoice.setInvoiceAmountDueInBase(invoice.getInvoiceAmountDueInBase() + amountReceivedConvertedInInvoiceBaseCurrency);
                }
                
                invoiceList.add(invoice);
                // check if Forex/Gain Loss JE generated. If yes then need to delete JE too
                if (receiptDetail.getLinkedGainLossJE() != null && !receiptDetail.getLinkedGainLossJE().isEmpty()) {
                    deleteJEArray(receiptDetail.getLinkedGainLossJE(), companyid);
                }
                /**
                 * Deleting Realised Gain/Loss JE posted when linking Invoice with Payment. ERP - 39601
                 */
                if (receiptDetail != null && !StringUtil.isNullOrEmpty(receiptDetail.getRevalJeId())) {
                    accJournalEntryobj.deleteJEDtails(receiptDetail.getRevalJeId(), companyid);
                    accJournalEntryobj.deleteJE(receiptDetail.getRevalJeId(), companyid);
                }
                if (receiptDetail != null && !StringUtil.isNullOrEmpty(receiptDetail.getRevalJeIdPayment())) {
                    accJournalEntryobj.deleteJEDtails(receiptDetail.getRevalJeIdPayment(), companyid);
                    accJournalEntryobj.deleteJE(receiptDetail.getRevalJeIdPayment(), companyid);
                }
                if (!StringUtil.isNullOrEmpty(receiptDetail.getLinkedGSTJE())) {
                    /**
                     * Delete tax adjustment journal entry in purchase invoice
                     * is unlinked from payment.
                     */
                    deleteJEArray(receiptDetail.getLinkedGSTJE(), companyid);
                }
                unlinkedDetailIDs = unlinkedDetailIDs.concat("'").concat(receiptDetail.getID()).concat("',");
            }
            if (sumOfTotalAmount != 0 && receipt.getAdvanceDetails() != null && !receipt.getAdvanceDetails().isEmpty()) {
                for (AdvanceDetail advanceDetail : receipt.getAdvanceDetails()) {
                    double linkedAmountDue = advanceDetail.getAmountDue();
                    advanceDetail.setAmountDue(linkedAmountDue + sumOfTotalAmount);
                    List<Object> objectList = new ArrayList<Object>();
                    objectList.add((Object) advanceDetail);
                    accAccountDAOobj.saveOrUpdateAll(objectList);
                }
            } else if (sumOfTotalAmount != 0 && isopeningBalanceMP) {
                HashMap<String, Object> requestParams = new HashMap();
                requestParams.put(Constants.companyid, companyid);
                requestParams.put("gcurrencyid", company.getCurrency().getCurrencyID());
                KwlReturnObject bAmt = null;
                if (isopeningBalanceMP && receipt.isConversionRateFromCurrencyToBase()) {// if Receipt is opening balance Receipt and Conversion rate is taken from user is Currency to base then following method will be called.
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmountAccordingToCurrencyToBaseExchangeRate(requestParams, sumOfTotalAmount, fromcurrencyid, mpCreationDate, receiptexternalCurrencyRate);
                } else {
                    bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(requestParams, sumOfTotalAmount, fromcurrencyid, mpCreationDate, receiptexternalCurrencyRate);
                }
                double amountPaymentConvertedInBaseCurrency = (Double) bAmt.getEntityList().get(0);
                receipt.setOpeningBalanceAmountDue(sumOfTotalAmount + receipt.getOpeningBalanceAmountDue());
                receipt.setOpeningBalanceBaseAmountDue(amountPaymentConvertedInBaseCurrency + receipt.getOpeningBalanceBaseAmountDue());
                List<Object> objectList = new ArrayList<Object>();
                objectList.add((Object) receipt);
                accAccountDAOobj.saveOrUpdateAll(objectList);
            }
            if (!invoiceList.isEmpty()) {
                List<Object> objectList = new ArrayList<Object>(invoiceList);
                accAccountDAOobj.saveOrUpdateAll(objectList);
            }
            accVendorPaymentobj.deleteSelectedLinkedPaymentInvoices(receipt.getID(), "", companyid, unlinkedDetailIDs.substring(0, unlinkedDetailIDs.length()-1));
            result.add(paymentno);
        } catch (SessionExpiredException ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        } catch (Exception ex) {
            Logger.getLogger(AccGoodsReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
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
   
                    result = accJournalEntryobj.deleteJournalEntryDetailRow(jed.getID(), companyid);
                }
                result = accJournalEntryobj.permanentDeleteJournalEntry(oldjeid, companyid);
                KwlReturnObject jedresult1 = accJournalEntryobj.deleteJECustomData(oldjeid);
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE(ex.getMessage(), ex);
        }
    }
      /**
       * 
       * @param reqParams
       * @Desc : Get Job Work Out Ingredient Details
       * @return
       * @throws JSONException
       * @throws ServiceException 
       */
      public JSONObject getJobWorkOutIngradientDetails(JSONObject reqParams) throws JSONException, ServiceException {
        JSONObject returnObj = new JSONObject();
        String joborderdetail = reqParams.optString("joborderdetail");
        String companyid = reqParams.optString("companyid");
        /**
         * Get Sub Product BOM Qty
         */
        KwlReturnObject object = accProductObj.getSubAssemblyProduct(reqParams);
        List<ProductAssembly> productAssemblys = object.getEntityList();
        JSONObject qtyObj = new JSONObject();
        for (ProductAssembly assembly : productAssemblys) {
            qtyObj.put(assembly.getSubproducts().getID(), assembly.getQuantity());
        }
        JSONArray jSONArray = new JSONArray();
        Map<String, Object> reqMap = new HashMap();
        reqMap.put("companyid", companyid);
        reqMap.put("jobworkorderdetail", joborderdetail);
        reqMap.put("isjoborder", true);
        /**
         * Get IST details for Job Order Product
         */
        KwlReturnObject returnObject = accGoodsReceiptobj.getJobOrderSubgredients(reqMap);
        List<InterStoreTransferRequest> storeTransferRequests = returnObject.getEntityList();
        Set<ISTDetail> iSTDetails = null;
        for (InterStoreTransferRequest interStoreTransferRequest : storeTransferRequests) {
            JSONObject obj = new JSONObject();
            /**
             * Get Used Qty for IST
             */
            double usedqty=accGoodsReceiptobj.getSumofChallanUsedQuantity(interStoreTransferRequest.getId());
            Product product = interStoreTransferRequest.getProduct();
            obj.put("productid", interStoreTransferRequest.getProduct().getID());
            obj.put("assembleqty", qtyObj.optDouble(interStoreTransferRequest.getProduct().getID()));
            obj.put("productname", interStoreTransferRequest.getProduct().getName());
            obj.put("challannumber", interStoreTransferRequest.getChallanNumber().getChallanNumber());
            obj.put("istId", interStoreTransferRequest.getId());
            obj.put("balancequantity", interStoreTransferRequest.getAcceptedQty()-usedqty);
            obj.put("location", product.getLocation().getId());
            obj.put("warehouse", product.getWarehouse().getId());
            obj.put("warrantyperiod", product.getWarrantyperiod());
            obj.put("warrantyperiodsal", product.getWarrantyperiodsal());
            obj.put("isLocationForProduct", product.isIslocationforproduct());
            obj.put("isWarehouseForProduct", product.isIswarehouseforproduct());
            obj.put("isRowForProduct", product.isIsrowforproduct());
            obj.put("isRackForProduct", product.isIsrackforproduct());
            obj.put("isBinForProduct", product.isIsbinforproduct());
            obj.put("isBatchForProduct", product.isIsBatchForProduct());
            obj.put("isSKUForProduct", product.isIsSKUForProduct());
            obj.put("isSerialForProduct", product.isIsSerialForProduct());

            /**
             * Get Batch details from IST
             */
            /**
             * Get IST Details
             */
            iSTDetails = interStoreTransferRequest.getIstDetails();
            JSONArray batchdetails = new JSONArray();
            for (ISTDetail iSTDetail : iSTDetails) {
                JSONObject jSONObject = new JSONObject();
                jSONObject.put("location", iSTDetail.getDeliveredLocation() != null ? iSTDetail.getDeliveredLocation().getId() : "");
                jSONObject.put("locationName", iSTDetail.getDeliveredLocation() != null ? iSTDetail.getDeliveredLocation().getName() : "");
                jSONObject.put("wastageQuantityType", 0);
                jSONObject.put("batchName", iSTDetail.getBatchName());
                jSONObject.put("expend", "");
                jSONObject.put("expdate", "");
                jSONObject.put("avlquantity", "");
                jSONObject.put("rack", iSTDetail.getDeliveredRack()!= null ? iSTDetail.getDeliveredRack().getId() : "");
                jSONObject.put("rackName", iSTDetail.getDeliveredRack() != null ? iSTDetail.getDeliveredRack().getName() : "");
                jSONObject.put("warehouse", interStoreTransferRequest.getToStore().getId());
                jSONObject.put("storeName", interStoreTransferRequest.getToStore().getFullName());
                jSONObject.put("avialblequantity", authHandler.roundQuantity(iSTDetail.getDeliveredQuantity()-usedqty, interStoreTransferRequest.getCompany().getCompanyID()));
                jSONObject.put("id", "");
                jSONObject.put("skufield", "");
                jSONObject.put("wastageQuantity", 0);
                jSONObject.put("balance", 0);
                jSONObject.put("serialnoid", "");
                jSONObject.put("productid", interStoreTransferRequest.getProduct().getID());
                jSONObject.put("quantity", authHandler.roundQuantity(interStoreTransferRequest.getOrderedQty(), interStoreTransferRequest.getCompany().getCompanyID()));
                jSONObject.put("stocktype", 1);
                jSONObject.put("serialno", "");
                jSONObject.put("serialNames", iSTDetail.getDeliveredSerialNames());
                jSONObject.put("purchasebatchid", "");
                if (product.isIsBatchForProduct()) {
                    NewProductBatch batchObj = stockService.getERPProductBatch(product, interStoreTransferRequest.getToStore(), iSTDetail.getDeliveredLocation(), iSTDetail.getDeliveredRow(), iSTDetail.getDeliveredRack(), iSTDetail.getDeliveredBin(), iSTDetail.getBatchName());
                    jSONObject.put("purchasebatchid", batchObj != null ? batchObj.getId() : "");
                    jSONObject.put("avialblequantity", authHandler.roundQuantity(batchObj.getQuantitydue(), interStoreTransferRequest.getCompany().getCompanyID()));
                } else {
                    NewProductBatch batchObj = stockService.getERPProductBatch(product, interStoreTransferRequest.getToStore(), iSTDetail.getDeliveredLocation(), iSTDetail.getDeliveredRow(), iSTDetail.getDeliveredRack(), iSTDetail.getDeliveredBin(), "");
                    jSONObject.put("purchasebatchid", batchObj != null ? batchObj.getId() : "");
                    jSONObject.put("avialblequantity", authHandler.roundQuantity(batchObj.getQuantitydue(), interStoreTransferRequest.getCompany().getCompanyID()));
                }
                jSONObject.put("expstart", "");
                jSONObject.put("isreadyonly", false);
                jSONObject.put("purchaseserialid", "");
                jSONObject.put("mfgdate", "");
                jSONObject.put("asset", "");
                jSONObject.put("row", iSTDetail.getDeliveredRow() != null ? iSTDetail.getDeliveredRow().getId() : "");
                jSONObject.put("rowName", iSTDetail.getDeliveredRow() != null ? iSTDetail.getDeliveredRow().getName() : "");
                jSONObject.put("batch", iSTDetail.getBatchName());
//                    jSONObject.put("documentid", dodId);
                jSONObject.put("bin", iSTDetail.getDeliveredBin() != null ? iSTDetail.getDeliveredBin().getId() : "");
                jSONObject.put("binName", iSTDetail.getDeliveredBin() != null ? iSTDetail.getDeliveredBin().getName() : "");
                batchdetails.put(jSONObject);
            }
            obj.put("batchdetails", batchdetails.toString());
            jSONArray.put(obj);
        }
        jSONArray=sortJsonArrayOnProduct(jSONArray);
        returnObj.put("data", jSONArray);
        return returnObj;
    }
      /**
       * 
       * @param array
       * @return
       * @Desc : Sort product
       * @throws JSONException 
       */
    public JSONArray sortJsonArrayOnProduct(JSONArray array) throws JSONException {
        List<JSONObject> jsons = new ArrayList<JSONObject>();
        try {
            for (int i = 0; i < array.length(); i++) {
                jsons.add(array.getJSONObject(i));
            }
            Collections.sort(jsons, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject lhs, JSONObject rhs) {
                    String lid = "", rid = "";
                    try {
                        lid = lhs.getString("productid");
                        rid = rhs.getString("productid");
                    } catch (JSONException ex) {
                        Logger.getLogger(AccGoodsReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    return lid.compareTo(rid);
                }
            });
        } catch (JSONException ex) {
            Logger.getLogger(AccGoodsReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(AccGoodsReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new JSONArray(jsons);
    }

    /**
     *
     * @param request
     * @param obj
     * @param goodsReceipt
     * @param je
     * @throws ServiceException
     */
    @Override
    public void getGoodsReceiptCustomDataForPayment(HashMap<String, Object> request, JSONObject obj, GoodsReceipt goodsReceipt, JournalEntry je) throws ServiceException {
        try {
            String companyid = (String) request.get(Constants.companyKey);
            boolean isFixedAsset = request.containsKey(Constants.isFixedAsset) ? (Boolean) request.get(Constants.isFixedAsset) : false;
            int moduleid = isFixedAsset ? Constants.Acc_FixedAssets_PurchaseInvoice_ModuleId : Constants.Acc_Vendor_Invoice_ModuleId;
            DateFormat userDateFormat=null;
            if(request.containsKey(Constants.userdf) && request.get(Constants.userdf)!= null){
                userDateFormat=(DateFormat)request.get(Constants.userdf);
            }
            HashMap<String, Object> fieldrequestParams = new HashMap();
            HashMap<String, String> customFieldMap = new HashMap<>();
            HashMap<String, String> customDateFieldMap = new HashMap<>();
            HashMap<String, String> replaceFieldMap = new HashMap<>();
            KwlReturnObject custumObjresult = null;

            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyKey, Constants.moduleid, Constants.customcolumn));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList(companyid, moduleid, 0));
            HashMap<String, Integer> FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            if (goodsReceipt.isNormalInvoice()) {
                Map<String, Object> variableMap = new HashMap<>();
                custumObjresult = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), je.getID());
                replaceFieldMap = new HashMap<>();
                if (custumObjresult != null && custumObjresult.getEntityList().size() > 0) {
                    AccJECustomData jeDetailCustom = (AccJECustomData) custumObjresult.getEntityList().get(0);
                    if (jeDetailCustom != null) {
                        AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                        JSONObject params = new JSONObject();
                        params.put(Constants.companyKey, companyid);
                        params.put(Constants.isLink, true);
                        params.put(Constants.userdf,userDateFormat);
                        if (request.containsKey(Constants.requestModuleId) && request.get(Constants.requestModuleId) != null) {
                            params.put(Constants.linkModuleId, request.get(Constants.requestModuleId));
                        }
                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                    }
                }
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("AccGoodsReceiptServiceImpl.getGoodsReceiptCustomDataForPayment : " + ex.getMessage(), ex);
        }
    }
    
    @Override
    public JSONObject  deletePurchaseReturnPermanentJSON(JSONObject paramJobj) throws SessionExpiredException {
        JSONObject jobj = new JSONObject();
        String msg = "", msgtext = "";
        boolean issuccess = false;
        boolean isConsignment = false;
        String linkedTransaction = "";
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("SO_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);
        List list = new ArrayList();
        double totalPOBalQty = 0;
        boolean isPOClosed = false;
        HashMap<String, Object> requestParams = new HashMap<>();
        String companyid = paramJobj.optString(Constants.companyKey);
        StringBuffer productIds = new StringBuffer();
        TransactionStatus status = txnManager.getTransaction(def);
        try {
            if (!StringUtil.isNullOrEmpty(paramJobj.optString("isConsignment",null))) {
                isConsignment = Boolean.parseBoolean(paramJobj.optString("isConsignment"));
            }
            boolean isFixedAsset = paramJobj.optString("isFixedAsset",null) != null ? Boolean.parseBoolean(paramJobj.optString("isFixedAsset")) : false;
//            list = deletePurchaseReturnPermanent(request);
            list = deletePurchaseReturnPermanent(paramJobj);
            if (list != null && !list.isEmpty()) {
                linkedTransaction = (String) list.get(0);
            }
            txnManager.commit(status);
            issuccess = true;
            msgtext = (isFixedAsset) ? "acc.field.assetPurchaseReturnHasBeenDeletedSuccessfully" : (isConsignment ? "acc.Consignment.purchaseReturnhasbeendeletedsuccessfully" : "acc.field.PurchaseReturnhasbeendeletedsuccessfully");
            if (StringUtil.isNullOrEmpty(linkedTransaction)) {
                msg = messageSource.getMessage(msgtext, null, Locale.forLanguageTag(paramJobj.getString("language")));
            } else {
                msgtext = "acc.field.PurchaseReturnexcept";
                msg = messageSource.getMessage(msgtext, null, Locale.forLanguageTag(paramJobj.getString("language"))) + " " + linkedTransaction.substring(0, linkedTransaction.length() - 2) + " " + messageSource.getMessage("acc.field.hasbeendeletedsuccessfully", null, Locale.forLanguageTag(paramJobj.optString("language")));
            }

            if (list != null && list.size() > 0) {
                productIds = (StringBuffer) list.get(1);
            }
        } catch (SessionExpiredException ex) {
            txnManager.rollback(status);
            msg = ex.getMessage();
            Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + ex.getMessage();
            Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put("success", issuccess);
                jobj.put("msg", msg);
                jobj.put("productIds", productIds);
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
   
   
      public List deletePurchaseReturnPermanent(JSONObject paramJObj) throws SessionExpiredException, AccountingException, ServiceException {
        String linkedTransaction = "";
        StringBuffer productIds = new StringBuffer();
        List resultList = new ArrayList();
        Set<String> poids = new HashSet<String>();
        try {
            boolean isFixedAsset = false;
            if (!StringUtil.isNullOrEmpty(paramJObj.optString("isFixedAsset",null))) {
                isFixedAsset = Boolean.parseBoolean(paramJObj.optString("isFixedAsset"));
            }
            String audtmsg = "";
            if (isFixedAsset) {
                audtmsg = " Asset ";
            } else {
                audtmsg = " ";
            }
            JSONArray jArr = new JSONArray(paramJObj.optString("data","[{}]"));
            String companyid = paramJObj.optString(Constants.companyKey);
            String prid = "", prno = "";
            PurchaseReturn purchaseReturn1 = null;
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);

            boolean isnegativestockforlocwar = false;
            ExtraCompanyPreferences extraCompanyPreferences = null;
            KwlReturnObject extraprefresult = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            extraCompanyPreferences = extraprefresult != null ? (ExtraCompanyPreferences) extraprefresult.getEntityList().get(0) : null;
            isnegativestockforlocwar = extraCompanyPreferences.isIsnegativestockforlocwar();

            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                prid = StringUtil.DecodeText(jobj.optString("billid"));
                prno = jobj.optString("billno");

                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("prid", prid);
                requestParams.put("companyid", companyid);
                requestParams.put("prno", prno);
                requestParams.put("isnegativestockforlocwar", isnegativestockforlocwar);

                if (isFixedAsset) {
                    accGoodsReceiptobj.deleteAssetDetailsLinkedWithPurchaseReturn(requestParams);
                }

                KwlReturnObject res = accountingHandlerDAOobj.getObject(PurchaseReturn.class.getName(), prid);
                purchaseReturn1 = (PurchaseReturn) res.getEntityList().get(0);
                if (!purchaseReturn1.isIsdeletable()) {
                    linkedTransaction += prno + ", ";
                    continue;
                }
                if (extraCompanyPreferences != null && (extraCompanyPreferences.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD)) { // delete JE temporary
                    KwlReturnObject result = accGoodsReceiptobj.getProductsFromPurchaseReturn(prid, companyid);
                    if (result != null && result.getEntityList() != null && !result.getEntityList().isEmpty()) {
                        List productList = result.getEntityList();
                        if (productList != null && !productList.isEmpty()) {
                            for (Object object : productList) {
                                String productid = (String) object;
                                if (productIds.indexOf(productid) == -1) {
                                    productIds.append(productid).append(",");
                                }
                            }
                        }
                    }
                }
                if (!StringUtil.isNullOrEmpty(prid)) {  //check if its sales return edit case 
                    //check Debit node is made from this Purchase return
                    String debitNoteId = accGoodsReceiptobj.getDebitNoteIdFromPRId(prid, companyid);
                    if (!StringUtil.isNullOrEmpty(debitNoteId)) {
                        requestParams.put("dnid", debitNoteId);
                    }
                }
                if (requestParams.containsKey("dnid") && requestParams.get("dnid") != null) {
                    //Check whether linked DN is paid partially/fully
                    boolean isNoteLinkedWithPayment = accDebitNoteService.isNoteLinkedWithPayment((String) requestParams.get("dnid"));
                    boolean isNoteLinkedWithAdvancePayment = accDebitNoteService.isNoteLinkedWithAdvancePayment((String) requestParams.get("dnid"));
                    if (isNoteLinkedWithPayment || isNoteLinkedWithAdvancePayment) {
                        linkedTransaction += prno + ", ";
                        continue;
                    }
                }
                if (preferences.isInventoryAccountingIntegration() && preferences.isWithInvUpdate()) {

                    String action = "17";
                    boolean isDirectUpdateInvFlag = false;
                    if (preferences.isUpdateInvLevel()) {
                        isDirectUpdateInvFlag = true;
                        action = "19";//Direct Inventory Update action
                    }

                    JSONArray productArray = new JSONArray();


                    Set<PurchaseReturnDetail> purchaseReturnDetails = purchaseReturn1.getRows();
                    for (PurchaseReturnDetail purchaseReturnDetail : purchaseReturnDetails) {
                        JSONObject productObject = new JSONObject();
                        productObject.put("itemUomId", purchaseReturnDetail.getInventory().getUom().getID());
                        productObject.put("itemBaseUomRate", purchaseReturnDetail.getInventory().getBaseuomrate());
                        productObject.put("itemQuantity", purchaseReturnDetail.getInventory().getBaseuomquantity());
                        productObject.put("quantity", purchaseReturnDetail.getInventory().getQuantity());
                        //productObject.put("itemQuantity", purchaseReturnDetail.getInventory().getQuantity());
                        productObject.put("itemCode", purchaseReturnDetail.getInventory().getProduct().getProductid());
                        if (isDirectUpdateInvFlag) {
                            productObject.put("storeid", purchaseReturnDetail.getInvstoreid());
                            productObject.put("locationid", purchaseReturnDetail.getInvlocid());
                        }
                        productArray.put(productObject);
                    }
                    if (productArray.length() > 0) {

                        String sendDateFormat = "yyyy-MM-dd";
                        DateFormat dateformat = new SimpleDateFormat(sendDateFormat);
                        Date date = purchaseReturn1.getOrderDate();
                        String stringDate = dateformat.format(date);

                        JSONObject jSONObject = new JSONObject();
                        jSONObject.put("deliveryDate", stringDate);
                        jSONObject.put("dateFormat", sendDateFormat);
                        jSONObject.put("details", productArray);
                        jSONObject.put("orderNumber", purchaseReturn1.getPurchaseReturnNumber());
                        jSONObject.put("companyId", companyid);
                        jSONObject.put("purchasing", true);

                        String url = paramJObj.optString(Constants.inventoryURL);
                        CommonFnController cfc = new CommonFnController();
//                        cfc.updateInventoryLevel(request, jSONObject, url, action);
                        cfc.updateInventoryLevel(paramJObj, jSONObject, url, action);
                    }
                }
                boolean isLinkedWithVI = false;
                boolean isLinkedWithGR = false;
                String linkids = "";
                Set<PurchaseReturnDetail> purchaseReturnDetails = purchaseReturn1.getRows();
                Iterator itr = purchaseReturnDetails.iterator();
                while (itr.hasNext()) {
                    String poID = "";
                    PurchaseReturnDetail row = (PurchaseReturnDetail) itr.next();
                    if (row.getGrdetails() != null) {
                        if (linkids.indexOf(row.getGrdetails().getGrOrder().getID()) == -1) {
                            linkids += row.getGrdetails().getGrOrder().getID() + ",";
                        }
                        isLinkedWithGR = true;
                    } else if (row.getVidetails() != null) {
                        if (linkids.indexOf(row.getVidetails().getGoodsReceipt().getID()) == -1) {
                            linkids += row.getVidetails().getGoodsReceipt().getID() + ",";
                        }
                        isLinkedWithVI = true;
                    }
                    /*
                     * PO->GR->PR
                     */
                    if (row.getGrdetails() != null && row.getGrdetails().getPodetails() != null) {
                        poID = row.getGrdetails().getPodetails().getPurchaseOrder().getID();
                    } else if (row.getGrdetails() != null && row.getGrdetails().getVidetails() != null && row.getGrdetails().getVidetails().getPurchaseorderdetail() != null) {  /*
                         * PO->PI->GR->PR
                         */
                        poID = row.getGrdetails().getVidetails().getPurchaseorderdetail().getPurchaseOrder().getID();
                    }
                    poids.add(poID);//add PurchaseOrder Ids in set
                }
                if (linkids.length() > 0) {
                    requestParams.put("linkIDs", linkids.substring(0, linkids.length() - 1));
                }
                requestParams.put("isLinkedWithVI", isLinkedWithVI);
                requestParams.put("isLinkedWithGR", isLinkedWithGR);
                accGoodsReceiptobj.updatePOBalanceQtyAfterPR(prid, companyid);
                stockMovementService.removeStockMovementByReferenceId(purchaseReturn1.getCompany(), purchaseReturn1.getID());

                /*
                 * Purpose to Delete linking information Purchase Return while
                 * deleting
                 */
                accGoodsReceiptobj.deleteLinkingInformationOfPR(requestParams);
                Set<String> invoiceIDSet = new HashSet<>();
                if (requestParams.containsKey("dnid") && requestParams.get("dnid") != null) {
                    /*
                     * Before deleting DebitNoteDetail Keeping id of Goodsrceipt
                     * utlized in DN
                     */
                    String debitNoteID = (String) requestParams.get("dnid");
                    KwlReturnObject dnObj = accountingHandlerDAOobj.getObject(DebitNote.class.getName(), debitNoteID);
                    DebitNote debitNote = (DebitNote) dnObj.getEntityList().get(0);
                    if (debitNote.getApprovestatuslevel() == 11 && !debitNote.isDeleted()) {//does not need to delete Rounding JE if dn temporrarly deleted.
                        for (DebitNoteDetail cnd : debitNote.getRows()) {
                            if (cnd.getGoodsReceipt() != null) {
                                invoiceIDSet.add(cnd.getGoodsReceipt().getID());
                            }
                        }
                    }
                    accDebitNoteService.updateOpeningInvoiceAmountDue((String) requestParams.get("dnid"), companyid);
                    accDebitNoteobj.deleteLinkingInformationOfDN(requestParams);
                    accGoodsReceiptobj.deleteDebitNotesPermanent(requestParams);
                }

                //Delete Rouding JEs if created against PI
                String roundingJENo = "";
                String roundingIDs = "";
                if (!invoiceIDSet.isEmpty()) {
                    String invIDs = "";
                    for (String invID : invoiceIDSet) {
                        invIDs = invID + ",";
                    }
                    if (!StringUtil.isNullOrEmpty(invIDs)) {
                        invIDs = invIDs.substring(0, invIDs.length() - 1);
                    }
                    KwlReturnObject jeResult = accJournalEntryobj.getRoundingJournalEntryByGRIds(invIDs, companyid);
                    List<JournalEntry> jeList = jeResult.getEntityList();
                    for (JournalEntry roundingJE : jeList) {
                        roundingJENo = roundingJE.getEntryNumber() + ",";
                        roundingIDs = roundingJE.getID() + ",";
                        accDebitNoteService.deleteJEArray(roundingJE.getID(), companyid);
                    }

                    if (!StringUtil.isNullOrEmpty(roundingJENo)) {
                        roundingJENo = roundingJENo.substring(0, roundingJENo.length() - 1);
                    }
                    if (!StringUtil.isNullOrEmpty(roundingIDs)) {
                        roundingIDs = roundingIDs.substring(0, roundingIDs.length() - 1);
                    }
                }

                accGoodsReceiptobj.deletePurchasesBatchSerialDetails(requestParams);
                requestParams.put("isMRPModuleActivated", extraCompanyPreferences.isActivateMRPModule());
                requestParams.put("isPerpetualValuationActivated", (preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD));
                if (extraCompanyPreferences != null && (extraCompanyPreferences.isActivateMRPModule() || preferences.getInventoryValuationType() == Constants.PERPETUAL_VALUATION_METHOD)) {
                    if (purchaseReturn1.getInventoryJE() != null) { // delete inventory JE
                        JournalEntry inventoryJE = purchaseReturn1.getInventoryJE();
                        requestParams.put("inventoryjeid", inventoryJE.getID());
                    }
                }
                accGoodsReceiptobj.deletePurchaseReturnPermanent(requestParams);
                
                Map<String, Object> auditRequestParams = new HashMap<String, Object>();
                auditRequestParams.put(Constants.reqHeader, paramJObj.getString(Constants.reqHeader));
                auditRequestParams.put(Constants.remoteIPAddress, paramJObj.getString(Constants.remoteIPAddress));
                auditRequestParams.put(Constants.useridKey, paramJObj.getString(Constants.useridKey));
                
                auditTrailObj.insertAuditLog("87", "User " + paramJObj.optString(Constants.userfullname) + " has deleted" + audtmsg + "Purchase Return Permanently " + prno, auditRequestParams, prid);
            }
            resultList.add(0, linkedTransaction);
            resultList.add(1, productIds);
            resultList.add(2, poids);
        } catch (InventoryException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, Locale.forLanguageTag(paramJObj.optString("language"))));
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, Locale.forLanguageTag(paramJObj.optString("language"))));
        }
        return resultList;
    }
      
  @Override    
    public JSONObject deleteGoodsReceiptPermanentJSON(JSONObject paramJobj) {
        JSONObject jobj = new JSONObject();
        String msg = "", msgtext = "", channelName = "", countryid = "";
        boolean issuccess = false;
        boolean isFixedAsset = false;
        boolean isConsignment = false;
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("isFixedAsset", null))) {
            isFixedAsset = Boolean.parseBoolean(paramJobj.optString("isFixedAsset"));
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("isConsignment", null))) {
            isConsignment = Boolean.parseBoolean(paramJobj.optString("isConsignment"));
        }
        if (!StringUtil.isNullOrEmpty(paramJobj.optString("countryid", null))) {
            countryid = paramJobj.optString("countryid");
        }
        msgtext = (isFixedAsset) ? "acc.fgr.del" : (isConsignment ? "acc.consignment.veninv.del" : "acc.gr.del");
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setName("R_Tx");
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRED);

        TransactionStatus status = txnManager.getTransaction(def);
        try {
//            String linkedTransaction=deleteGoodsReceiptPermanent(request);
            String linkedTransaction = deleteGoodsReceiptPermanent(paramJobj);
            txnManager.commit(status);
            issuccess = true;
            if (StringUtil.isNullOrEmpty(linkedTransaction)) {
                msg = messageSource.getMessage(msgtext, null, Locale.forLanguageTag(paramJobj.optString("language")));   //"Vendor Invoice(s) has been deleted successfully";
            } else {
                msgtext = (isFixedAsset) ? "acc.field.AquiredInvoicesexcept" : (isConsignment ? "acc.consignment.venInvoicesexcept" : "acc.field.VendorInvoicesexcept");
                msg = messageSource.getMessage(msgtext, null, Locale.forLanguageTag(paramJobj.optString("language"))) + " " + linkedTransaction.substring(0, linkedTransaction.length() - 2) + " " + messageSource.getMessage("acc.field.deletedsuccessfully", null, Locale.forLanguageTag(paramJobj.optString("language"))) + " " + messageSource.getMessage("acc.field.usedintransactionorlockingperiod", null, Locale.forLanguageTag(paramJobj.optString("language")));
            }
            if (!StringUtil.isNullOrEmpty(countryid) && countryid.equals(String.valueOf(Constants.indian_country_id))) {
                channelName = "/VendorInvoiceAndCashPurchaseReport/gridAutoRefresh";
            }
        } catch (Exception ex) {
            txnManager.rollback(status);
            msg = "" + (ex.getMessage() != null ? ex.getMessage() : ex.getCause().getMessage());
            Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                jobj.put(Constants.RES_success, issuccess);
                jobj.put(Constants.RES_msg, msg);
                if (issuccess && !StringUtil.isNullOrEmpty(channelName)) {
                    jobj.put(Constants.channelName, channelName);
                }
            } catch (JSONException ex) {
                Logger.getLogger(accGoodsReceiptControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return jobj;
    }
  
    public String deleteGoodsReceiptPermanent(JSONObject paramJobj) throws ServiceException, AccountingException, SessionExpiredException, ParseException {
        String linkedTransaction = "";
        try {
            JSONArray jArr = new JSONArray(paramJobj.optString("data", "[{}]"));
            boolean isFixedAsset = false;

            if (!StringUtil.isNullOrEmpty(paramJobj.optString("isFixedAsset", null))) {
                isFixedAsset = Boolean.parseBoolean(paramJobj.optString("isFixedAsset"));
            }
            boolean isConsignment = false;

            if (!StringUtil.isNullOrEmpty(paramJobj.optString("isConsignment", null))) {
                isConsignment = Boolean.parseBoolean(paramJobj.optString("isConsignment"));
            }
            HashMap<String, Object> reconcileMap = new HashMap<>();
            String companyid = paramJobj.optString(Constants.companyKey);
            String greceiptno = "", journalentryid = "", entryno = "", greceiptid = "", isexpenseinv = "";
            KwlReturnObject capresult = accountingHandlerDAOobj.getObject(CompanyAccountPreferences.class.getName(), companyid);
            CompanyAccountPreferences preferences = (CompanyAccountPreferences) capresult.getEntityList().get(0);
            int countryid = preferences.getCompany().getCountry() != null ? Integer.parseInt(preferences.getCompany().getCountry().getID()) : 0;
            for (int i = 0; i < jArr.length(); i++) {
                JSONObject jobj = jArr.getJSONObject(i);
                greceiptid = jobj.optString("billid");
                KwlReturnObject res = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), greceiptid);
                GoodsReceipt goodsReceipt = (GoodsReceipt) res.getEntityList().get(0);
                entryno = goodsReceipt.getJournalEntry().getEntryNumber();//jobj.getString("entryno");
                greceiptno = goodsReceipt.getGoodsReceiptNumber();//jobj.getString("billno");
                journalentryid = goodsReceipt.getJournalEntry().getID();//jobj.getString("journalentryid");
                isexpenseinv = goodsReceipt.isIsExpenseType() + "";//jobj.getString("isexpenseinv");
                String lockPeriodStr = "";
                Date entryDateForLock = null;
                DateFormat dateFormatForLock = authHandler.getDateOnlyFormat();
//              if(jobj.has("date")){
                entryDateForLock = goodsReceipt.getJournalEntry().getEntryDate();//dateFormatForLock.parse(jobj.getString("date"));
//              }
		PayDetail paydetail = goodsReceipt.getPayDetail();
                HashMap<String, Object> requestParams = new HashMap<String, Object>();
                requestParams.put("greceiptid", greceiptid);
                requestParams.put("companyid", companyid);
                requestParams.put("entryno", entryno);
                requestParams.put("journalentryid", journalentryid);
                requestParams.put("isexpenseinv", isexpenseinv);
                requestParams.put("isFixedAsset", isFixedAsset);
                requestParams.put("trasactionNo", greceiptno);
                if (entryDateForLock != null) {
                    requestParams.put("entrydate", entryDateForLock);
                    requestParams.put("df", dateFormatForLock);
                }
                KwlReturnObject result;
                if (!StringUtil.isNullOrEmpty(greceiptid)) {

                    result = accDebitNoteobj.getDNDetailsFromGReceipt(greceiptid, companyid);
                    List<String> list = result.getEntityList();
                    if (!list.isEmpty()) {
                        //throw new AccountingException(messageSource.getMessage("acc.nee.62", null, Locale.forLanguageTag(paramJobj.optString("language"))));
                        linkedTransaction += "<a onclick='linkinfo(\"" + goodsReceipt.getID() + "\",\"false\",\"Vendor\",\"" + greceiptno + "\",\"true\",\"" + isFixedAsset + "\",\"6\")'href='#'>" + greceiptno + "</a> ,";
                        continue;
                    }
                    result = accDebitNoteobj.getDNDetailsFromGReceiptOtherwise(greceiptid, companyid);   //while deleting GR check wether it is used in debit note otherwise
                    list = result.getEntityList();
                    if (!list.isEmpty()) {
                        //throw new AccountingException(messageSource.getMessage("acc.nee.62", null, Locale.forLanguageTag(paramJobj.optString("language"))));
                        linkedTransaction += "<a onclick='linkinfo(\"" + goodsReceipt.getID() + "\",\"false\",\"Vendor\",\"" + greceiptno + "\",\"true\",\"" + isFixedAsset + "\",\"6\")'href='#'>" + greceiptno + "</a> ,";
                        continue;
                    }

                    result = accGoodsReceiptobj.getGRFromGRInvoice(greceiptid, companyid);
                    list = result.getEntityList();
                    if (!list.isEmpty()) {
                        linkedTransaction += "<a onclick='linkinfo(\"" + goodsReceipt.getID() + "\",\"false\",\"Vendor\",\"" + greceiptno + "\",\"true\",\"" + isFixedAsset + "\",\"6\")'href='#'>" + greceiptno + "</a> ,";
                        continue;
                        //throw new AccountingException(messageSource.getMessage("acc.nee.68", null, Locale.forLanguageTag(paramJobj.optString("language"))));
                    }

                    result = accVendorPaymentobj.getPaymentsFromGReceipt(greceiptid, companyid);
                    list = result.getEntityList();
                    if (!list.isEmpty()) {
                        linkedTransaction += "<a onclick='linkinfo(\"" + goodsReceipt.getID() + "\",\"false\",\"Vendor\",\"" + greceiptno + "\",\"true\",\"" + isFixedAsset + "\",\"6\")'href='#'>" + greceiptno + "</a> ,";
                        continue;
                        //throw new AccountingException(messageSource.getMessage("acc.nee.63", null, Locale.forLanguageTag(paramJobj.optString("language"))));
                    }
                    result = accGoodsReceiptobj.getAdvancePaymentsLinkedWithInvoice(greceiptid, companyid);
                    list = result.getEntityList();
                    if (!list.isEmpty()) {
                        linkedTransaction += "<a onclick='linkinfo(\"" + goodsReceipt.getID() + "\",\"false\",\"Vendor\",\"" + greceiptno + "\",\"true\",\"" + isFixedAsset + "\",\"6\")'href='#'>" + greceiptno + "</a> ,";
                        continue;
                    }

                    result = accGoodsReceiptobj.getPRFromGReceipt(greceiptid, companyid);
                    list = result.getEntityList();
                    if (!list.isEmpty()) {
                        linkedTransaction += "<a onclick='linkinfo(\"" + goodsReceipt.getID() + "\",\"false\",\"Vendor\",\"" + greceiptno + "\",\"true\",\"" + isFixedAsset + "\",\"6\")'href='#'>" + greceiptno + "</a> ,";
                        continue;
                        //throw new AccountingException(messageSource.getMessage("acc.nee.72", null, Locale.forLanguageTag(paramJobj.optString("language"))));
                    }

                    result = accGoodsReceiptobj.getConsignmentNumberFromGReceipt(greceiptid, companyid);   //while deleting GR check wether it is used in Consignment Cost
                    list = result.getEntityList();
                    if (list != null && !list.isEmpty()) {
                        //throw new AccountingException(messageSource.getMessage("acc.nee.74", null, Locale.forLanguageTag(paramJobj.optString("language"))));
                        linkedTransaction += "<a onclick='linkinfo(\"" + goodsReceipt.getID() + "\",\"false\",\"Vendor\",\"" + greceiptno + "\",\"true\",\"" + isFixedAsset + "\",\"6\")'href='#'>" + greceiptno + "</a> ,";
                        continue;
                    }

                    result = accCreditNoteDAOobj.getCreditNoteLinkedWithInvoice(greceiptid, companyid);         //to check wether invoice is linked to credit note against vendor only for malaysian country if yes do not allow user to delete it ERP-27284 / ERP-28249
                    List<String> linkedInvList = result.getEntityList();
                    if (!linkedInvList.isEmpty()) {
                        linkedTransaction += "<a onclick='linkinfo(\"" + goodsReceipt.getID() + "\",\"false\",\"Vendor\",\"" + greceiptno + "\",\"true\",\"" + isFixedAsset + "\",\"6\")'href='#'>" + greceiptno + "</a> ,";
                        continue;
                    }
                    
                    result = accGoodsReceiptobj.getDebitNoteForOverchargedLinkedWithInvoice(greceiptid, companyid);
                    List dnOverchargeList = result.getEntityList();
                    if (dnOverchargeList != null && !dnOverchargeList.isEmpty()) {
                        linkedTransaction += "<a onclick='linkinfo(\"" + goodsReceipt.getID() + "\",\"false\",\"Vendor\",\"" + greceiptno + "\",\"true\",\"" + isFixedAsset + "\",\"6\")'href='#'>" + greceiptno + "</a> ,";
                        continue;
                    }

                    // Check if invoice has been claimed or recovered then it should not be delete
                    // for Malasian Company
                    HashMap<String, Object> badMaps = new HashMap<String, Object>();
                    badMaps.put("companyid", companyid);
                    badMaps.put("invoiceid", greceiptid);

                    result = accGoodsReceiptobj.getBadDebtPurchaseInvoiceMappingForGoodsReceipt(badMaps);
                    list = result.getEntityList();
                    if (!list.isEmpty()) {
                        linkedTransaction += "<a onclick='linkinfo(\"" + goodsReceipt.getID() + "\",\"false\",\"Vendor\",\"" + greceiptno + "\",\"true\",\"6\")'href='#'>" + greceiptno + "</a> ,";
                        continue;
                    }
                    /**
                     * Method to check the payment is Reconciled or not
                     * according to its JE id
                     */
                    reconcileMap.put("jeid", journalentryid);
                    reconcileMap.put("companyid", companyid);
                    boolean isReconciledFlag = accBankReconciliationObj.isRecordReconciled(reconcileMap);
                    if (isReconciledFlag) {
                        throw new AccountingException(messageSource.getMessage("acc.reconcilation.Cannotdeletepayment", null, Locale.forLanguageTag(paramJobj.optString("language"))) + " " + "<b>" + greceiptno + "</b>" + " " + messageSource.getMessage("acc.reconcilation.asitisreconciled", null, Locale.forLanguageTag(paramJobj.optString("language"))));
                    }
                    accBankReconciliationObj.deleteUnReconciliationRecords(reconcileMap);
                    HashMap<String, Object> doRequestParams = new HashMap<String, Object>();
                    ArrayList filter_names = new ArrayList(), filter_params = new ArrayList();
                    filter_names.add("goodsReceipt.ID");
                    filter_params.add(goodsReceipt.getID());
                    doRequestParams.put("filter_names", filter_names);
                    doRequestParams.put("filter_params", filter_params);
                    if (!isexpenseinv.equals("true")) {
                        KwlReturnObject podresult = accGoodsReceiptobj.getGoodsReceiptDetails(doRequestParams);
                        Iterator itr = podresult.getEntityList().iterator();
                        while (itr.hasNext()) {
                            GoodsReceiptDetail row = (GoodsReceiptDetail) itr.next();
                            if (row.getPurchaseorderdetail() != null) {
                                String linkid = row.getPurchaseorderdetail().getPurchaseOrder().getID();
                                if (!StringUtil.isNullOrEmpty(linkid)) {
                                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(PurchaseOrder.class.getName(), linkid);
                                    PurchaseOrder purchaseOrder = (PurchaseOrder) rdresult.getEntityList().get(0);
                                    HashMap hMap = new HashMap();
                                    hMap.put("purchaseOrder", purchaseOrder);
                                    hMap.put("value", "0");
                                    accGoodsReceiptobj.updatePOLinkflag(hMap);
                                
                                /*--If dropship PI is deleted then PO linked with same PI should not be manually closed now---- */
                                if (row.getPurchaseorderdetail().getPurchaseOrder().isIsDropshipDocument()) {
                                    row.getPurchaseorderdetail().getPurchaseOrder().setIsPOClosed(false);
                                }
                                
                            }
                            } else if (row.getGoodsReceiptOrderDetails() != null) {
                                String linkid = row.getGoodsReceiptOrderDetails().getGrOrder().getID();
                                if (!StringUtil.isNullOrEmpty(linkid)) {
                                    KwlReturnObject rdresult = accountingHandlerDAOobj.getObject(GoodsReceiptOrder.class.getName(), linkid);
                                    GoodsReceiptOrder goodsReceiptOrder = (GoodsReceiptOrder) rdresult.getEntityList().get(0);
                                    HashMap hMap = new HashMap();
                                    hMap.put("goodsReceiptOrder", goodsReceiptOrder);
                                    hMap.put("isOpenInPI", true);
                                    accGoodsReceiptobj.updateGRLinkflag(hMap);
                                }
                            }
                        }
                    } else if (isexpenseinv.equals("true")) {
                        /**
                         * when Deleting Expense Purchase
                         * Invoice linked with 'Expense PO' need to update Balance
                         * Amount and unlink flag for all linked Expense PO.
                         */
                        double totalBalAmount = 0.0;
                        double totalGRAmount = 0.0;
                        PurchaseOrder purchaseOrder = null;
                        HashMap<Object, Double> map =new HashMap<>();
                        KwlReturnObject exGRDresult = accGoodsReceiptobj.getExpenseGRDetails(doRequestParams);
                        List<ExpenseGRDetail> podlist = exGRDresult.getEntityList();
                        for (ExpenseGRDetail row : podlist) {
                            ExpensePODetail expPOD = row.getExpensePODetail();
                            if (expPOD != null) {
                                totalBalAmount = expPOD.getBalAmount() + row.getAmount();
                                expPOD.setBalAmount(totalBalAmount);
                                expPOD.getPurchaseOrder().setIsOpen(true);
                                purchaseOrder = expPOD.getPurchaseOrder();
                                totalGRAmount = row.getAmount();
                                if (map.containsKey(purchaseOrder)) {
                                    totalGRAmount = (Double) map.get(purchaseOrder) + row.getAmount();
                                    map.put(purchaseOrder, totalGRAmount);
                                } else {
                                    map.put(purchaseOrder, totalGRAmount);
                                }
                            }
                        }
                        /**
                         * for Partially case we need to iterate all Expense PO
                         * linked with Expense PI and set link flag.
                         */
                        for (Map.Entry<Object, Double> entrySet : map.entrySet()) {
                            PurchaseOrder po = (PurchaseOrder) entrySet.getKey();
                            double totalGRamount = (Double) entrySet.getValue();
                            if(po.getTotalamount() == totalGRamount){
                                po.setLinkflag(1);
                            }
                        }
                    }
                    
                    if (preferences.isInventoryAccountingIntegration() && !preferences.isWithInvUpdate()) {

                        String action = "17";
                        boolean isDirectUpdateInvFlag = false;
                        if (preferences.isUpdateInvLevel()) {
                            isDirectUpdateInvFlag = true;
                            action = "19";//Direct Inventory Update action
                        }


                        JSONArray productArray = new JSONArray();


                        Set<GoodsReceiptDetail> goodsReceiptDetails = goodsReceipt.getRows();
                        for (GoodsReceiptDetail goodsReceiptDetail : goodsReceiptDetails) {
                            JSONObject productObject = new JSONObject();
                            productObject.put("itemUomId", goodsReceiptDetail.getInventory().getUom().getID());
                            productObject.put("itemBaseUomRate", goodsReceiptDetail.getInventory().getBaseuomrate());
                            productObject.put("itemQuantity", goodsReceiptDetail.getInventory().getBaseuomquantity() * (-1));
                            productObject.put("quantity", goodsReceiptDetail.getInventory().getQuantity() * (-1));
                            //productObject.put("itemQuantity", goodsReceiptDetail.getInventory().getQuantity()*(-1));
                            productObject.put("itemCode", goodsReceiptDetail.getInventory().getProduct().getProductid());
                            if (isDirectUpdateInvFlag) {
                                productObject.put("storeid", goodsReceiptDetail.getInvstoreid());
                                productObject.put("locationid", goodsReceiptDetail.getInvlocid());
                            }
                            productArray.put(productObject);
                        }
                        if (productArray.length() > 0) {

                            String sendDateFormat = "yyyy-MM-dd";
                            DateFormat dateformat = new SimpleDateFormat(sendDateFormat);
//                            Date date = goodsReceipt.getJournalEntry().getEntryDate();
                            Date date = goodsReceipt.getCreationDate();
                            String stringDate = dateformat.format(date);

                            JSONObject jSONObject = new JSONObject();
                            jSONObject.put("deliveryDate", stringDate);
                            jSONObject.put("dateFormat", sendDateFormat);
                            jSONObject.put("details", productArray);
                            jSONObject.put("orderNumber", goodsReceipt.getGoodsReceiptNumber());
                            jSONObject.put("companyId", companyid);
                            jSONObject.put("purchasing", true);

                            String url = paramJobj.optString(Constants.inventoryURL);
                            CommonFnController cfc = new CommonFnController();
//                            cfc.updateInventoryLevel(request, jSONObject, url, action);
                            cfc.updateInventoryLevel(paramJobj, jSONObject, url, action);
                        }
                    }

                    // Check if goods receipt has been used in TAX Payment For India Company 
                    if (countryid == Constants.indian_country_id) {
                        /*
                         * TDS Payment is done or not at Goods Receipt's each line level, If TDS payment is done we will not allow to delete 
                         *
                         */
                         result = accGoodsReceiptobj.getGoodsReceiptTDSPayment(greceiptid, companyid);
                         list = result.getEntityList();
                         if (list != null && !list.isEmpty()) {
                             linkedTransaction += greceiptno + ", ";
                             continue;
                         }
                        
                        result = accGoodsReceiptobj.getTaxPaymentFromGoodsReciept(greceiptid, companyid);
                        list = result.getEntityList();
                        if (!list.isEmpty()) {
                            linkedTransaction += greceiptno + " ,";
                            continue;
                        }
                        /* Delete RCM Un-Registered Journal Entry details table data on delete Puurchase Invoice
                         * && goodsReceipt.isGtaapplicable()
                         */
                        if (goodsReceipt != null && goodsReceipt.getVendor() != null) {
                            if (goodsReceipt.getVendor().getGSTRegistrationType() != null && goodsReceipt.getVendor().getGSTRegistrationType().getDefaultMasterItem() != null) {
                                String DefaultMasterItemId = goodsReceipt.getVendor().getGSTRegistrationType().getDefaultMasterItem().getID();
                                if (DefaultMasterItemId.equals(Constants.GSTRegType.get(Constants.GSTRegType_Unregistered))) {
                                    JSONObject URDjobj = new JSONObject();
                                    URDjobj.put("receiptID", goodsReceipt.getID());
                                    URDjobj.put(Constants.companyid, companyid);
                                    accGoodsReceiptobj.deleteURDVendorRCMPurchaseInvoice(URDjobj);
                                    /**
                                     * Modify All Journal Entry details if daily
                                     * limit cross on particular Bill date
                                     */
                                    if (Constants.isRCMPurchaseURD5KLimit && entryDateForLock != null) {
                                        paramJobj.put("billdate", dateFormatForLock.format(entryDateForLock));
                                        paramJobj.put("companyid", companyid);
                                        paramJobj.put(Constants.df, dateFormatForLock);
                                        paramJobj.put("invoiceAmount", 0);
                                        paramJobj.put("GRNNumber", goodsReceipt.getGoodsReceiptNumber());
                                        accGoodsReceiptModuleService.modifyURDVendorRCMPurchaseInvoiceJEDetails(paramJobj);
                                    }
                                }
                            }
                        }
                        // For Dealer Excise Details - Mapped with supplier details
                        KwlReturnObject grDetailsRes = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), greceiptid);
                        GoodsReceipt goodsReceiptDetail = (GoodsReceipt) grDetailsRes.getEntityList().get(0);
                        if (goodsReceiptDetail.isIsExciseInvoice()) {
                            Set<GoodsReceiptDetail> rows = goodsReceiptDetail.getRows();
                            for (GoodsReceiptDetail goodsReceiptDetailsRow : rows) {
                                result = accGoodsReceiptobj.getSupplierExciseDetailsMapping(goodsReceiptDetailsRow.getID(), companyid);   //while deleting GR check wether it is used in Consignment Cost
                                list = result.getEntityList();
                                if (list != null && !list.isEmpty()) {
                                    linkedTransaction += greceiptno + ", ";
                                    break;
                                    //throw new AccountingException(messageSource.getMessage("acc.nee.74", null, Locale.forLanguageTag(paramJobj.optString("language"))));
                                }
                            }
                            if (!StringUtil.isNullOrEmpty(linkedTransaction)) {
                                continue;
                            }
                        }
                        if (goodsReceipt.getTotalAdvanceTDSAdjustmentAmt() > 0) {//Only if Advance TDS amount is adjusted with GoodsReceipt.
                            HashMap paramsHM = new HashMap();
                            paramsHM.put("companyid", companyid);
                            paramsHM.put("goodsreceiptid", greceiptid);
                            paramsHM.put("isUsed", false);//To Update is UsedFlag in Advance Payment.
                            //Before Delete, unflag the TDSUsed flag from AdvancePayment.
                            accGoodsReceiptobj.updateAdvancePaymentTDSUsedFlag(paramsHM);
                            //To Delete GoodsReceiptPaymentMapping rows against selected Goods Receipt
                            boolean isDeleteGoodsReceiptPaymentMapping = accGoodsReceiptobj.deleteGoodsReceiptPaymentMapping(greceiptid);
                            // Delere Line level mapping with Advance
                            accGoodsReceiptobj.deleteTDSAdvancePaymentMapping(greceiptid,companyid, goodsReceipt.isIsExpenseType());
                        }
                    }

                    accGoodsReceiptobj.deleteLinkingInformationOfPI(requestParams);//deleting linking information of invoice from PO,VQ,GR.
                        
                    accGoodsReceiptobj.deleteGoodsReceiptsLandedInvoice(greceiptid, companyid);//deleting Landed Invoice.

                    String reconsilationID = "";
                    String unReconsilationID = "";

                    //Deleting  BankReconciliationDetail
                    KwlReturnObject reconsiledDetails = accBankReconciliationObj.getBRfromJE(journalentryid, companyid, true);
                    if (reconsiledDetails.getRecordTotalCount() > 0) {
                        List<BankReconciliationDetail> brd = reconsiledDetails.getEntityList();
                        for (BankReconciliationDetail reconciliation : brd) {
                            accBankReconciliationObj.permenantDeleteBankReconciliationDetail(reconciliation.getID(), companyid);
                            reconsilationID = reconciliation.getBankReconciliation().getID();
                        }
                    }

                    //Deleting  BankUnreconciliationDetail
                    KwlReturnObject unReconsiledDetails = accBankReconciliationObj.getBankUnReconsiledfromJE(journalentryid, companyid, true);
                    if (unReconsiledDetails.getRecordTotalCount() > 0) {
                        List<BankUnreconciliationDetail> brd = unReconsiledDetails.getEntityList();
                        for (BankUnreconciliationDetail reconciliation : brd) {
                            accBankReconciliationObj.permenantDeleteBankUnReconciliationDetail(reconciliation.getID(), companyid);
                            unReconsilationID = reconciliation.getBankReconciliation().getID();
                        }
                    }
                    if (!StringUtil.isNullOrEmpty(reconsilationID)) {
                        accBankReconciliationObj.deleteBankReconciliation(reconsilationID, companyid);
                    }
                    if (!StringUtil.isNullOrEmpty(unReconsilationID)) {
                        accBankReconciliationObj.deleteBankReconciliation(unReconsilationID, companyid);
                    }

                    result = accJournalEntryobj.permanentDeleteJournalEntryDetailReval(greceiptid, companyid);
                    result = accJournalEntryobj.permanentDeleteJournalEntryReval(greceiptid, companyid);
                    
                    /*
                     * Delete JE and Mapping Entry of TDS Exemption Limit
                     * 
                     */
                    result = accJournalEntryobj.getTDSJEEntryMapping(greceiptid, companyid);
                    list = result.getEntityList();
                    if (list != null && !list.isEmpty()) {
                        for (String mappingid : list) {
                            KwlReturnObject mappingResult = accJournalEntryobj.getJEEntryFromMapping(mappingid, companyid);
                            List<String> listMapping = mappingResult.getEntityList();
                            accJournalEntryobj.deleteJournalEntryTDSMapping(mappingid, companyid);
                            if (listMapping != null && !listMapping.isEmpty()) {
                                for (String jeid : listMapping) {
                                    accJournalEntryobj.permanentDeleteJournalEntryDetailTDSMapping(jeid, companyid);
                                    accJournalEntryobj.permanentDeleteJournalEntryTDSMapping(jeid, companyid);
                                }
                            }
                        }
                    }
                    
                    /**
                     * Check if transaction date falls under Accounting Locking Period. return if 'lockPeriodStr' is not empty.
                     */
                        lockPeriodStr = beforeDeleteGoodsReceiptPermanent(requestParams);    // ERP-38423
//                        accGoodsReceiptobj.deleteGoodsReceiptPermanent(requestParams);
                    
                    if(!StringUtil.isNullOrEmpty(lockPeriodStr)){
                        linkedTransaction += lockPeriodStr;
                        continue;
                    }
                    //Delete the Payment Details associated with Cash Purchase only
                    if (paydetail != null) {
                        HashMap<String, Object> paydetailMap = new HashMap<>();
                        paydetailMap.put("companyid", companyid);
                        paydetailMap.put("chequeid", paydetail.getCheque() != null ? paydetail.getCheque().getID() : "");
                        paydetailMap.put("cardid", paydetail.getCard() != null ? paydetail.getCard().getID() : "");
                        paydetailMap.put("paydetailid", paydetail.getID());
                        accBankReconciliationObj.deleteCashPayDetails(paydetailMap);
                    }

                    boolean iscash = StringUtil.isNullOrEmpty(paramJobj.optString("incash", null)) ? false : Boolean.parseBoolean(paramJobj.optString("incash"));
                    StringBuffer journalEntryMsg = new StringBuffer();
                    if (!StringUtil.isNullOrEmpty(entryno)) {
                        journalEntryMsg.append(" along with the JE No. " + entryno);
                    }
                    String audtmsg = "";
                    if (isFixedAsset) {
                        audtmsg = " has deleted Asset Acquired Invoice Permanently ";
                    } else if (isConsignment) {
                        audtmsg = " has deleted Consignment Purchase Invoice Permanently ";
                    } else {
                        audtmsg = " has deleted Vendor Invoice Permanently "; //ERP-18017
                    }
                    Map<String, Object> auditRequestParams = new HashMap<String, Object>();
                    auditRequestParams.put(Constants.reqHeader, paramJobj.getString(Constants.reqHeader));
                    auditRequestParams.put(Constants.remoteIPAddress, paramJobj.getString(Constants.remoteIPAddress));
                    auditRequestParams.put(Constants.useridKey, paramJobj.getString(Constants.useridKey));
                    if (!iscash) {
//                        auditTrailObj.insertAuditLog(com.krawler.accounting.utils.AuditAction.INVOICE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + audtmsg + greceiptno+journalEntryMsg.toString(), request, greceiptid);
                        auditTrailObj.insertAuditLog(AuditAction.INVOICE_DELETED, "User " + paramJobj.optString(Constants.userfullname) + audtmsg + greceiptno + journalEntryMsg.toString(), auditRequestParams, greceiptid);
                    } else {
//                        auditTrailObj.insertAuditLog(com.krawler.accounting.utils.AuditAction.CASH_PURCHASE_DELETED, "User " + sessionHandlerImpl.getUserFullName(request) + " has deleted Cash Purchase Permanently " + greceiptno+journalEntryMsg.toString(), request, greceiptid);
                        auditTrailObj.insertAuditLog(AuditAction.CASH_PURCHASE_DELETED, "User " + paramJobj.optString(Constants.userfullname) + " has deleted Cash Purchase Permanently " + greceiptno + journalEntryMsg.toString(), auditRequestParams, greceiptid);
                    }
                }
            }
        } catch (JSONException ex) {
            throw new AccountingException(messageSource.getMessage("acc.common.excp1", null, Locale.forLanguageTag(paramJobj.optString("language"))));
        }
        return linkedTransaction;
    }
     
    
    /**
     * @Info Check Accounting Locking period before deleting record.
     * @param requestParams
     * @return
     * @throws ServiceException
     * @throws AccountingException
     */
    public String beforeDeleteGoodsReceiptPermanent(HashMap<String, Object> requestParams) throws ServiceException, AccountingException {
        String linkedTransaction = "";
        String lockedPeriodStr = "";
        String transactionNo = requestParams.get("trasactionNo") != null ? (String) requestParams.get("trasactionNo") : "";
        String transactionId = requestParams.get("greceiptid") != null ? (String) requestParams.get("greceiptid") : "";
        try {
            accGoodsReceiptobj.deleteGoodsReceiptPermanent(requestParams);
        } catch (AccountingException ex) {
            if (!StringUtil.isNullOrEmpty(transactionNo)) {
                linkedTransaction += "<a onclick='linkinfo(\"" + transactionId + "\",\"false\",\"Vendor\",\"" + transactionNo + "\",\"true\",\"6\")'href='#'>" + transactionNo + "</a> ,";
            }
        }
        return linkedTransaction;
    }
    
    public JSONArray getGoodsReceiptListForLinking(HashMap<String, Object> requestParams) throws JSONException, ServiceException{
        List list = accGoodsReceiptobj.getGoodsReceiptListForLinking(requestParams);
        boolean doflag = requestParams.containsKey("doflag")?  (Boolean)requestParams.get("doflag") : false;
        List rejectedList = null;
        if(doflag){
            rejectedList = accGoodsReceiptobj.getGoodsReceiptDOLinkingList(requestParams);
        }
        JSONArray jArr = new JSONArray();
        JSONObject obj = null;
        for (int i = 0; i < list.size(); i++) {
            Object[] data = (Object[])list.get(i);
            String billid = (String)data[0];
            if(rejectedList!=null && !rejectedList.isEmpty() && rejectedList.contains(billid)){
                continue;
            }
            obj = new JSONObject();
            
            obj.put("billid", data[0]);
            obj.put("billno", data[1]);
            obj.put("transectionno", data[1]);
            
            if (!StringUtil.isNullOrEmpty(billid)) {
                KwlReturnObject grDetailsRes = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), billid);
                GoodsReceipt grObj = (GoodsReceipt) grDetailsRes.getEntityList().get(0);
                if (grObj != null) {
                    obj.put(Constants.IsRoundingAdjustmentApplied, grObj.isIsRoundingAdjustmentApplied());
                    /**
                     * Get Merchant Exporter Check
                     */
                    obj.put(Constants.isMerchantExporter, grObj.isIsMerchantExporter());
                }
            }
            
            try {
                /**
                 * Put transaction date for India linking case.
                 */
                obj.put("date", data[2] != null ? authHandler.getDateOnlyFormat().format(data[2]) : "");
            } catch (SessionExpiredException ex) {
                Logger.getLogger(AccGoodsReceiptServiceImpl.class.getName()).log(Level.SEVERE, null, ex);
            }
            jArr.put(obj);
        }
        return jArr;
    }
    
    public void getCustmDataForPurchaseInvoice(HashMap<String, Object> request, JSONArray jArr, String companyid, HashMap replaceFieldMap, HashMap customFieldMap, HashMap customDateFieldMap, HashMap FieldMap, HashMap replaceFieldMapRows, HashMap customFieldMapRows, HashMap customDateFieldMapRows, HashMap fieldMapRows) throws JSONException, ServiceException, SessionExpiredException {

            //Custom field details Maps for Global data
        for (int i = 0; i < jArr.length(); i++) {
            JSONObject transObj = jArr.getJSONObject(i);

            if (transObj.has("type") && !StringUtil.isNullOrEmpty(transObj.getString("type")) && "Purchase Invoice".equals(transObj.getString("type"))) {
                    String jeid = transObj.getString("journalentryid");

                    if (!StringUtil.isNullOrEmpty(transObj.optString(jeid, ""))) {
                        boolean isExport = (request.get("isExport") == null) ? false : (Boolean) request.get("isExport");
                        Map<String, Object> variableMap = new HashMap<String, Object>();
                        KwlReturnObject kwlObj = accountingHandlerDAOobj.getObject(AccJECustomData.class.getName(), jeid);
                        replaceFieldMap = new HashMap<String, String>();
                        if (kwlObj != null && kwlObj.getEntityList().size() > 0) {
                            AccJECustomData jeDetailCustom = (AccJECustomData) kwlObj.getEntityList().get(0);
                            if (jeDetailCustom != null) {
                                AccountingManager.setCustomColumnValues(jeDetailCustom, FieldMap, replaceFieldMap, variableMap);
                                JSONObject params = new JSONObject();
                                params.put("companyid", companyid);
                                if (!isExport) {
                                    isExport = (request.get("isAgedPayables") == null) ? false : (Boolean) request.get("isAgedPayables");
                                }
                                params.put("isExport", isExport);
                                if (request.containsKey("browsertz") && request.get("browsertz") != null) {
                                    params.put("browsertz", request.get("browsertz").toString());
                                }
                                fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, transObj, params);
                            }
                        }
                        /*
                         *  Line level Custom field code. (run only in case of chkl)  
                         */
                        if (Constants.AGED_PAYABE_LINE_CD_COMP_LIST.contains(request.get(Constants.COMPANY_SUBDOMAIN))) {
                            
                            kwlObj = accountingHandlerDAOobj.getObject(GoodsReceipt.class.getName(), transObj.getString("billid"));
                            if (kwlObj != null && kwlObj.getEntityList() != null && !kwlObj.getEntityList().isEmpty()) {
                            GoodsReceipt gReceipt = (GoodsReceipt) kwlObj.getEntityList().get(0);
                            
                            ArrayList<String> rowidList = new ArrayList();
                            Set<GoodsReceiptDetail> goodsReceiptDetails = gReceipt.getRows();
                            if (gReceipt.isIsExpenseType()) {
                                Set<ExpenseGRDetail> expenseGRDetails = gReceipt.getExpenserows();
                                for (ExpenseGRDetail expGReceiptDetail : expenseGRDetails) {
                                    rowidList.add(expGReceiptDetail.getID());
                                }
                            } else if (gReceipt.isNormalInvoice() && goodsReceiptDetails != null && !goodsReceiptDetails.isEmpty()) {
                                for (GoodsReceiptDetail row : goodsReceiptDetails) {
                                    rowidList.add(row.getID());
                                }
                            }

                            Map<String, List<Object>> linelabelDataMap = new LinkedHashMap();
                            for (String rowID : rowidList) {
                                JSONObject customObject = new JSONObject();
                                Map<String, Object> variableMapRows = new HashMap<String, Object>();
                                HashMap<String, Object> invDetailsRequestParams = new HashMap<String, Object>();
                                ArrayList Detailfilter_names = new ArrayList();
                                ArrayList Detailfilter_params = new ArrayList();
                                Detailfilter_names.add(Constants.Acc_JEDetail_recdetailId);
                                Detailfilter_params.add(rowID);
                                invDetailsRequestParams.put(Constants.filterNamesKey, Detailfilter_names);
                                invDetailsRequestParams.put(Constants.filterParamsKey, Detailfilter_params);
                                KwlReturnObject idcustdetailresult = accInvoiceDAOobj.getInvoiceDetailsCustomData(invDetailsRequestParams);
                                if (idcustdetailresult.getEntityList().size() > 0) {
                                    AccJEDetailCustomData jeDetailCustom = (AccJEDetailCustomData) idcustdetailresult.getEntityList().get(0);
                                    AccountingManager.setCustomColumnValues(jeDetailCustom, fieldMapRows, replaceFieldMapRows, variableMapRows);
                                    if (jeDetailCustom != null) {
                                        JSONObject params = new JSONObject();
                                        params.put(Constants.isExport, false);
                                        params.put(Constants.isForReport, true);
                                        fieldDataManagercntrl.getLineLevelCustomData(variableMapRows, customFieldMapRows, customDateFieldMapRows, customObject, params);

                                        if (customObject.length() > 0) {
                                            Iterator<String> keys = customObject.keys();
                                            while (keys.hasNext()) {
                                                String key = (String) keys.next();
                                                if (!key.equals("allCustomFieldKeyValuePairString")) {// no need to go for key allCustomFieldKeyValuePairString
                                                    if (linelabelDataMap.containsKey(key)) {
                                                        if (!linelabelDataMap.get(key).contains(customObject.get(key))) {
                                                            linelabelDataMap.get(key).add(customObject.get(key));
                                                        }
                                                    } else {
                                                        List<Object> dataList = new ArrayList<>();
                                                        dataList.add(customObject.get(key));
                                                        linelabelDataMap.put(key, dataList);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            for (Map.Entry<String, List<Object>> entry : linelabelDataMap.entrySet()) {
                                String commaSeperatedValue = "";
                                String key = entry.getKey();
                                List<Object> data = entry.getValue();
                                for (Object dataObj : data) {
                                    if (dataObj != null) {
                                        if (StringUtil.isNullOrEmpty(commaSeperatedValue)) {
                                            commaSeperatedValue = dataObj.toString();
                                        } else {
                                            commaSeperatedValue += "," + dataObj.toString();
                                        }
                                    }
                                }
                                transObj.put(key, commaSeperatedValue);
                            }
                        }
                    }
                }
            }
        }

    }
    
    @Override
    public JSONArray getPurchaseReturnJson(Map request, List list) throws ServiceException {
        JSONArray jArr = new JSONArray();
        try {
//            HashMap<String, Object> requestParams = AccountingManager.getGlobalParams(request);
            double quantity = 0;
            double amount = 0,amountwithouttax=0;
//            String companyid=sessionHandlerImpl.getCompanyid(request);
            String companyid = request.get("companyid").toString();
            String vendorEmailId="";
            boolean isLineLevelTermFlag = false;//To Check Whether LinelevelTerms are applicable or not.
//            boolean isConsignment=(StringUtil.isNullOrEmpty(request.getParameter("isConsignment")))?false:Boolean.parseBoolean(request.getParameter("isConsignment"));
            boolean isConsignment = request.get("isConsignment") != null ? Boolean.parseBoolean(request.get("isConsignment").toString()) : false;
//            boolean isFixedAsset=(StringUtil.isNullOrEmpty(request.getParameter("isFixedAsset")))?false:Boolean.parseBoolean(request.getParameter("isFixedAsset"));
            boolean isFixedAsset = request.get("isConsignment") != null ? Boolean.parseBoolean(request.get("isFixedAsset").toString()) : false;
            HashMap<String, String> replaceFieldMap = new HashMap<String, String>();
            HashMap<String, String> customFieldMap = new HashMap<String, String>();
            HashMap<String, String> customDateFieldMap = new HashMap<String, String>();
            HashMap<String, Integer> FieldMap = null;
            HashMap<String, Object> fieldrequestParams = new HashMap();
//            JSONObject paramJobj=StringUtil.convertRequestToJsonObject(request);
            DateFormat userDateFormat=null;
            if (request.containsKey(Constants.userdateformat)) {
                userDateFormat = new SimpleDateFormat(String.valueOf(request.get(Constants.userdateformat)));
            }
            fieldrequestParams.put(Constants.filter_names, Arrays.asList(Constants.companyid, Constants.moduleid));
            fieldrequestParams.put(Constants.filter_values, Arrays.asList( companyid, isConsignment?Constants.Acc_ConsignmentPurchaseReturn_ModuleId:isFixedAsset?Constants.Acc_FixedAssets_Purchase_Return_ModuleId:Constants.Acc_Purchase_Return_ModuleId));
            FieldMap = accAccountDAOobj.getFieldParamsCustomMap(fieldrequestParams, replaceFieldMap, customFieldMap, customDateFieldMap);
            KwlReturnObject extracap = accountingHandlerDAOobj.getObject(ExtraCompanyPreferences.class.getName(), companyid);
            ExtraCompanyPreferences extraCompanyPreferences = (ExtraCompanyPreferences) extracap.getEntityList().get(0);
            if(extraCompanyPreferences != null && extraCompanyPreferences.getLineLevelTermFlag()==1){
                isLineLevelTermFlag = true;//If LineLevelTerms are applicable, then update the flag.
            }
            int countryid = 0;
            if(extraCompanyPreferences != null && extraCompanyPreferences.getCompany().getCountry() != null){
                countryid = Integer.parseInt(extraCompanyPreferences.getCompany().getCountry().getID());
            }
            Iterator itr = list.iterator();
            //params to send to get billing address
            HashMap<String, Object> addressParams = new HashMap<String, Object>();
            addressParams.put("companyid", companyid);
            addressParams.put("isDefaultAddress", true); //always true to get defaultaddress
            addressParams.put("isBillingAddress", true); //true to get billing address
            while (itr.hasNext()) {
                //SalesOrder salesOrder=(SalesOrder)itr.next();
                Object [] oj;
                String orderid = null;
                if(request.containsKey("isReportBuilder") && Boolean.parseBoolean(request.get("isReportBuilder").toString())){
                    orderid = (String) itr.next();
                } else {
                    oj = (Object[]) itr.next();
                    orderid = oj[0].toString();
                }
                
                    KwlReturnObject objItr = accountingHandlerDAOobj.getObject(PurchaseReturn.class.getName(), orderid);
                    PurchaseReturn purchaseReturn = (PurchaseReturn) objItr.getEntityList().get(0);
                    DebitNote debitNote = null;
                    if (purchaseReturn.isIsNoteAlso()) {
                       KwlReturnObject idresult = accDebitNoteobj.getDebitNoteIdFromPRId(purchaseReturn.getID(), companyid);
                       if (!(idresult.getEntityList().isEmpty())) {
                           debitNote = (DebitNote) idresult.getEntityList().get(0);
                       }
                    }
                    Vendor vendor=purchaseReturn.getVendor();
                    JSONObject obj = new JSONObject();
                     //*** Attachments Documents SJ[ERP-16331] 
                    HashMap<String, Object> hashMap = new HashMap<String, Object>();
                    hashMap.put("invoiceID", purchaseReturn.getID());
                    hashMap.put("companyid", purchaseReturn.getCompany().getCompanyID());
                    KwlReturnObject object = accInvoiceDAOobj.getinvoiceDocuments(hashMap);
                    int attachemntcount = object.getRecordTotalCount();
                    obj.put("attachment", attachemntcount);
                    //*** Attachments Documents SJ[ERP-16331] 
                    obj.put("billid", purchaseReturn.getID());
                    obj.put("companyid", purchaseReturn.getCompany().getCompanyID());
                    obj.put("companyname", purchaseReturn.getCompany().getCompanyName());
                    obj.put("withoutinventory", false);
                    obj.put("personid", vendor.getID());
                    obj.put("billno", purchaseReturn.getPurchaseReturnNumber());
                    obj.put("date", authHandler.getDateOnlyFormat().format(purchaseReturn.getOrderDate()));
                    obj.put(Constants.HAS_ACCESS, vendor.isActivate());
                    obj.put("posttext", purchaseReturn.getPostText()==null?"":purchaseReturn.getPostText());
                    obj.put("personname", vendor.getName());
                    obj.put("aliasname", vendor.getAliasname());
                    obj.put("personemail", vendor.getEmail());
//                    MasterItem gstRegistrationType = vendor != null ? vendor.getGSTRegistrationType() : null;
//                    if (gstRegistrationType != null && gstRegistrationType.getDefaultMasterItem() != null) {
//                        obj.put("GSTINRegTypeDefaultMstrID", gstRegistrationType.getDefaultMasterItem().getID());
//                    }
                    
                    /**
                     * Put GST document history.
                     */
                    if (purchaseReturn.getCompany().getCountry().getID().equalsIgnoreCase("" + Constants.indian_country_id)) {
                        obj.put("refdocid", purchaseReturn.getID());
                        fieldDataManagercntrl.getGSTDocumentHistory(obj);

                    }
                    addressParams.put("vendorid", vendor.getID());
                    VendorAddressDetails vendorAddressDetail = accountingHandlerDAOobj.getVendorAddressObj(addressParams);
                    vendorEmailId = vendorAddressDetail != null ? vendorAddressDetail.getEmailID() : "";
                    obj.put("billingEmail",vendorEmailId);
                    obj.put("memo", purchaseReturn.getMemo());
                    obj.put("externalcurrencyrate", purchaseReturn.getExternalCurrencyRate());
                    obj.put("costcenterid", purchaseReturn.getCostcenter()==null?"":purchaseReturn.getCostcenter().getID());
                    obj.put("costcenterName", purchaseReturn.getCostcenter()==null?"":purchaseReturn.getCostcenter().getName());
                    obj.put("taxid", purchaseReturn.getTax() != null ? purchaseReturn.getTax().getID() : "");
                    obj.put("shipdate", purchaseReturn.getShipdate()==null? "" : authHandler.getDateOnlyFormat().format(purchaseReturn.getShipdate()));
                    obj.put("shipvia", purchaseReturn.getShipvia()==null?"":purchaseReturn.getShipvia());
                    obj.put("fob", purchaseReturn.getFob()==null?"":purchaseReturn.getFob());
                    obj.put("isfavourite", purchaseReturn.isFavourite());
                    obj.put("isprinted", purchaseReturn.isPrinted());
                    obj.put("deleted", purchaseReturn.isDeleted());
                    obj.put("isdeletable", purchaseReturn.isIsdeletable());
                    obj.put("currencyid", (purchaseReturn.getCurrency() == null ? "" : purchaseReturn.getCurrency().getCurrencyID()));
                    obj.put("currencysymbol", (purchaseReturn.getCurrency() == null ? "" : purchaseReturn.getCurrency().getSymbol()));
                    obj.put("currencycode", (purchaseReturn.getCurrency() == null ? "" : purchaseReturn.getCurrency().getCurrencyCode()));
                    obj.put("sequenceformatid", purchaseReturn.getSeqformat()!=null?purchaseReturn.getSeqformat().getID():"NA");
                    obj.put("discountamountinbase", purchaseReturn.getDiscountinbase());
                    obj.put("isConsignment", purchaseReturn.isIsconsignment());
                    obj.put("isNoteAlso", purchaseReturn.isIsNoteAlso());
                    obj.put("gstIncluded", purchaseReturn.isGstIncluded());
                    obj.put("isapplytaxtoterms", purchaseReturn.isApplyTaxToTerms());
                    obj.put("cndnsequenceformatid", debitNote!=null?(debitNote.getSeqformat()!=null?debitNote.getSeqformat().getID():""):"");
                    obj.put("cndnnumber", debitNote!=null?debitNote.getDebitNoteNumber():"");
                    obj.put("entryno", (debitNote!=null && debitNote.getJournalEntry()!=null)?debitNote.getJournalEntry().getEntryNumber():"");
                    obj.put("journalentryid", (debitNote!=null && debitNote.getJournalEntry()!=null)?debitNote.getJournalEntry().getID():"");
                    if(countryid == Constants.indian_country_id){
                        obj.put("formtypeid", purchaseReturn.getFormtype());
                        obj.put("isInterstateParty", purchaseReturn.getVendor() != null ? purchaseReturn.getVendor().isInterstateparty():false);
                        obj.put(Constants.MVATTRANSACTIONNO, purchaseReturn.getMvatTransactionNo()!=null?purchaseReturn.getMvatTransactionNo():"");  
                        obj.put("ewayapplicable", purchaseReturn.isEwayapplicable()); // Get EWAY applicable Check - Used for INDIA only ERM-1108
                    }
                    if(purchaseReturn.getModifiedby()!=null){
                            obj.put("lasteditedby",StringUtil.getFullName(purchaseReturn.getModifiedby()));
                    }
                    double totalTermAmount = 0;
                    double totalTermTaxAmount = 0;
                    List purchaseReturnTermMapList = kwlCommonTablesDAOObj.getSummationOfTermAmtAndTermTaxAmt(Constants.purchasereturntermsmap, purchaseReturn.getID());
                    if(purchaseReturnTermMapList != null && !purchaseReturnTermMapList.isEmpty()){
                        Iterator termItr = purchaseReturnTermMapList.iterator();
                        while (termItr.hasNext()) {
                            Object[] termObj = (Object[]) termItr.next();
                            /* 
                            * [0] : Sum of termamount  
                            * [1] : Sum of termamountinbase 
                            * [2] : Sum of termTaxamount 
                            * [3] : Sum of termTaxamountinbase 
                            * [4] : Sum of termamountexcludingtax 
                            * [5] : Sum of termamountexcludingtaxinbase
                            */ 
                            if (purchaseReturn.isGstIncluded()) {
                                if(termObj[4] != null){
                                    totalTermAmount += authHandler.round((Double) termObj[4],companyid);
                                }
                            } else {
                                if(termObj[0] != null){
                                    totalTermAmount += authHandler.round((Double) termObj[0],companyid);
                                }
                            }
                            if(termObj[2] != null){
                                totalTermTaxAmount += authHandler.round((Double) termObj[2],companyid);
                            }
                        }
                    }
                    totalTermAmount = authHandler.round(totalTermAmount, companyid);
                    totalTermTaxAmount = authHandler.round(totalTermTaxAmount, companyid);
                    
                    JournalEntry inventoryJE = purchaseReturn.getInventoryJE();
                    obj.put("inventoryjeid", (inventoryJE != null ? inventoryJE.getID() : ""));
                    obj.put("inventoryentryno", (inventoryJE != null ? inventoryJE.getEntryNumber() : ""));
                    obj.put(Constants.SUPPLIERINVOICENO, purchaseReturn.getSupplierInvoiceNo() != null ? purchaseReturn.getSupplierInvoiceNo() : "");
                    Set<PurchaseReturnDetail> doRows = purchaseReturn.getRows();
                    amount = 0;
                    amountwithouttax=0;
                    double ordertaxamount =0 , rowOtherTermNonTaxableAmount = 0d;
                    boolean includeprotax = false;
                    double subtotal = 0d;
                    double productTotalAmount = 0d;
                    double totalDiscount=0.0;
                    if (doRows != null && !doRows.isEmpty()){
                        for ( PurchaseReturnDetail temp: doRows){
                            quantity = temp.getInventory().getQuantity();
                            double rowAmt = 0;
                            double disc = 0;
                            if(purchaseReturn.isGstIncluded()){
                                rowAmt = temp.getRateincludegst() * quantity;
                            }else{
                                rowAmt = temp.getRate() * quantity;
                            }
                            productTotalAmount += authHandler.round(rowAmt,companyid);
                            
                            
                            if(temp.getDiscountispercent() == 1){
                                disc = authHandler.round(rowAmt*temp.getDiscount()/100, companyid);
                            }else{
                                disc = temp.getDiscount();
                            }
                            totalDiscount+=authHandler.round(disc, companyid);
                            rowAmt = rowAmt-disc;
                            if(purchaseReturn.isGstIncluded()){
                                amountwithouttax+= temp.getRate() * quantity;
                            }else{
                                amountwithouttax+=rowAmt;
                            }
                            // getting tax also
                            
                            double taxAmt = temp.getRowTaxAmount();
                            if (isLineLevelTermFlag) {
                                taxAmt += authHandler.round(temp.getRowtermamount(), companyid);
                                amount += authHandler.round(temp.getOtherTermNonTaxableAmount(),companyid);
                                // Append OtherTermNonTaxableAmount for rach row.
                                rowOtherTermNonTaxableAmount += temp.getOtherTermNonTaxableAmount();
                            }
                            ordertaxamount+=taxAmt; // line level tax
                            if(!purchaseReturn.isGstIncluded()){
                                rowAmt+=taxAmt;
                                amount+=rowAmt;
                            }else{
                                amount+=rowAmt;
                            }
                            if (temp.getTax() != null) {
                                includeprotax = true;
                            }
                        }
                    }
                    if(purchaseReturn.isGstIncluded()){
                        subtotal= authHandler.round(productTotalAmount-totalDiscount-ordertaxamount, companyid);
                    }else{
                        subtotal= authHandler.round(productTotalAmount-totalDiscount, companyid);
                    }
                    obj.put("productTotalAmount", productTotalAmount);
                    obj.put("subtotal", subtotal);
                    obj.put("termdetails", getPurchaseReturnTermDetails(purchaseReturn.getID()));
                    obj.put("termamount", totalTermAmount);
                    obj.put("amountBeforeTax", subtotal + totalTermAmount);  
                    double taxPercent = 0;
                    if (purchaseReturn.getTax() != null) { //global level tax
                        KwlReturnObject taxresult = accTaxObj.getTaxPercent(companyid, purchaseReturn.getOrderDate(), purchaseReturn.getTax().getID());
                        taxPercent = (Double) taxresult.getEntityList().get(0);
                        ordertaxamount = (taxPercent == 0 ? 0 : authHandler.round((amount * taxPercent / 100), companyid));
                        amount += ordertaxamount;
                    }
                    obj.put("totaltaxamount", ordertaxamount + totalTermTaxAmount);
                    if (isLineLevelTermFlag) {
                        // If LineLevelTerm is applicable then add the value in JSON Object.
                        obj.put("OtherTermNonTaxableAmount", rowOtherTermNonTaxableAmount);
                    }
                    obj.put("includeprotax", includeprotax);
                    obj.put("amount", authHandler.round(amount + totalTermAmount + totalTermTaxAmount,companyid));
                    obj.put("amountwithouttax", authHandler.round(amountwithouttax,companyid));
                    if(purchaseReturn.getCurrency()!=null){
                        KwlReturnObject bAmt = accCurrencyDAOobj.getCurrencyToBaseAmount(request, amount + totalTermAmount + totalTermTaxAmount, purchaseReturn.getCurrency().getCurrencyID(), purchaseReturn.getOrderDate(),purchaseReturn.getExternalCurrencyRate());
                        obj.put("amountinbase", authHandler.round((Double) bAmt.getEntityList().get(0),companyid));
                    }
                    Map<String, Object> variableMap = new HashMap<String, Object>();
                    PurchaseReturnCustomData purchaseReturnCustomData = (PurchaseReturnCustomData)purchaseReturn.getPurchaseReturnCustomData();
                    AccountingManager.setCustomColumnValues(purchaseReturnCustomData, FieldMap, replaceFieldMap,variableMap);
                    
                    if (purchaseReturnCustomData != null) {
                        boolean isExport = (request.get("isExport") == null) ? false : true;
                        JSONObject params = new JSONObject();
                        params.put("companyid", companyid);
                        params.put("isExport", isExport);
                        params.put(Constants.userdf, userDateFormat);
                        if (!StringUtil.isNullOrEmpty(request.get("browsertz").toString())) {
                            params.put("browsertz", request.get("browsertz").toString());
                        }
                        fieldDataManagercntrl.addCustomData(variableMap, customFieldMap, customDateFieldMap, obj, params);
                    }
                    jArr.put(obj);
            
            }
        } catch (Exception ex) {
            throw ServiceException.FAILURE("getPurchaseReturnJson : "+ex.getMessage(), ex);
        }
        return jArr;
    }
    
    public JSONArray getPurchaseReturnTermDetails(String id) {
        JSONArray jArr = new JSONArray();
        try {
            HashMap<String, Object> requestParam = new HashMap();
            requestParam.put("purchasereturn", id);
            KwlReturnObject curresult = accInvoiceDAOobj.getPRTermMap(requestParam);
            if (curresult != null) {
                List<PurchaseReturnTermsMap> termMap = curresult.getEntityList();
                for (PurchaseReturnTermsMap purchaseReturnTermMap : termMap) {
                    InvoiceTermsSales mt = purchaseReturnTermMap.getTerm();
                    JSONObject jsonobj = new JSONObject();
                    jsonobj.put("id", mt.getId());
                    jsonobj.put("term", mt.getTerm());
                    jsonobj.put("glaccount", mt.getAccount().getID());
                    jsonobj.put("sign", mt.getSign());
                    jsonobj.put("formula", mt.getFormula());
                    jsonobj.put("termpercentage", purchaseReturnTermMap.getPercentage());
                    jsonobj.put("termamount", purchaseReturnTermMap.getTermamount());
                    jsonobj.put("termamountinbase", purchaseReturnTermMap.getTermamountinbase());
                    jsonobj.put("termtaxamount", purchaseReturnTermMap.getTermtaxamount());
                    jsonobj.put("termtaxamountinbase", purchaseReturnTermMap.getTermtaxamountinbase());
                    jsonobj.put("termAmountExcludingTax", purchaseReturnTermMap.getTermAmountExcludingTax());
                    jsonobj.put("termAmountExcludingTaxInBase", purchaseReturnTermMap.getTermAmountExcludingTaxInBase());
                    jsonobj.put("termtax", purchaseReturnTermMap.getTermtax() != null ? purchaseReturnTermMap.getTermtax().getID() : "");
                    jsonobj.put("linkedtaxname", purchaseReturnTermMap.getTermtax() != null ? purchaseReturnTermMap.getTermtax().getName() : "");
                    if (purchaseReturnTermMap.getTermtax() != null) {
                        jsonobj.put("linkedtaxpercentage", accInvoiceDAOobj.getPercentageFromTaxid(purchaseReturnTermMap.getTermtax().getID(), mt.getCompany().getCompanyID()));
                    } else {
                        jsonobj.put("linkedtaxpercentage", 0);
                    }
                    jArr.put(jsonobj);
                }
            }
        } catch (ServiceException ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        } catch (JSONException ex) {
            Logger.getLogger(accInvoiceControllerCMN.class.getName()).log(Level.SEVERE, null, ex);
        }
        return jArr;
    }
    /**
     * Function to get ITC reversal invoices.
     *
     * @param reqParams
     * @return
     * @throws ServiceException
     * @throws JSONException
     */
    public JSONObject isAllITCReversal(JSONObject reqParams) throws ServiceException, JSONException {
        JSONObject returnObj = new JSONObject();
        returnObj.put("isvalidselection", true);
        String documentids = reqParams.optString("documentids");
        String typeArr[] = documentids.split(",");
        List l = accGoodsReceiptobj.isAllITCReversal(reqParams);
        if (l.size() != typeArr.length) {
            returnObj.put("invaliddoc", l.toArray().toString());
            returnObj.put("isvalidselection", false);
        }
        return returnObj;
    }
}
